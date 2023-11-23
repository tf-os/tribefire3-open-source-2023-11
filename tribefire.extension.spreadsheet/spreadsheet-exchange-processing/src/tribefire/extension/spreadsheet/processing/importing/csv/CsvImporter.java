// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.spreadsheet.processing.importing.csv;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.io.input.BOMInputStream;

import com.braintribe.cfg.Configurable;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.logging.Logger;
import com.braintribe.model.extensiondeployment.script.Script;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.core.expert.api.DenotationMap;
import com.braintribe.model.processing.deployment.script.ScriptEngine;
import com.braintribe.utils.IOTools;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvMalformedLineException;

import tribefire.extension.spreadsheet.model.exchange.api.request.ImportCsvSheet;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetCharset;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetDataDelimiter;
import tribefire.extension.spreadsheet.model.reason.IncompleteEntity;
import tribefire.extension.spreadsheet.model.reason.MalformedSpreadsheetContent;
import tribefire.extension.spreadsheet.processing.importing.common.SheetEntityStreamingContext;

public class CsvImporter extends tribefire.extension.spreadsheet.processing.importing.common.SpreadsheetImporter<ImportCsvSheet> {
	private static Logger logger = Logger.getLogger(CsvImporter.class);

	protected String defaultDelimiter = ",";

	public CsvImporter(DenotationMap<Script, ScriptEngine<?>> engines) {
		super(engines);
	}

	@Configurable
	public void setDefaultDelimiter(String defaultDelimiter) {
		this.defaultDelimiter = defaultDelimiter;
	}

	@Override
	protected EntityStreamer streamEntitiesFromSpreadsheet(SheetEntityStreamingContext<ImportCsvSheet> streamContext) throws Exception {
		return new CsvEntityStreamer(streamContext);
	}

	private static class Row {
		String cells[];
		int rowNum;

		public Row(String[] cells, int rowNum) {
			super();
			this.cells = cells;
			this.rowNum = rowNum;
		}
	}

	private class CsvEntityStreamer implements EntityStreamer {

		private SheetEntityStreamingContext<ImportCsvSheet> context;
		private CSVReader reader;
		private List<ColumnInfo> columnInfos;
		private Integer startRow;
		private int maxRows;
		private int rowCount;
		private boolean failed;

		public CsvEntityStreamer(SheetEntityStreamingContext<ImportCsvSheet> context) {
			this.context = context;
			this.startRow = context.getSpreadsheetImport().getStartRow();
			this.maxRows = Optional.ofNullable(context.getSpreadsheetImport().getMaxRows()).orElse(-1);
		}

		private Maybe<String[]> nextRawRow() {
			try {
				if (reader == null)
					open();

				if (maxRows == rowCount || failed)
					return Maybe.complete(null);

				return Maybe.complete(reader.readNext());
			} catch (CsvMalformedLineException e) {
				failed = true;
				return Reasons.build(MalformedSpreadsheetContent.T).text(e.getMessage()).toMaybe();
			} catch (Exception e) {
				throw Exceptions.unchecked(e, "Error while reading csv sheet");
			}
		}

		private synchronized Maybe<Row> nextRow() {
			Maybe<String[]> cellsMaybe = nextRawRow();

			long rowNum = reader.getRecordsRead();

			if (cellsMaybe.isUnsatisfied()) {
				context.notifyRowCount(++rowCount);
				return Maybe.incomplete(new Row(null, (int) rowNum), cellsMaybe.whyUnsatisfied());
			}

			String cells[] = cellsMaybe.get();

			if (cells == null) {
				context.notifyTotalRowCount(rowCount);
				return Maybe.complete(null);
			}

			context.notifyRowCount(++rowCount);
			return Maybe.complete(new Row(cells, (int) rowNum));
		}

		private String getCharset(ImportCsvSheet spreadsheetImport) {
			String charset = spreadsheetImport.getCharset();

			if (charset != null)
				return charset;

			return Optional //
					.ofNullable(context.getCmdrContextBuilder().entityType(context.getImportTargetType()).meta(SpreadsheetCharset.T).exclusive()) //
					.map(SpreadsheetCharset::getCharset) //
					.orElse("UTF-8");
		}

		protected String getDelimiter(ImportCsvSheet spreadsheetImport) {
			String delimiter = getRawDelimiter(spreadsheetImport);
			switch (delimiter) {
				case "\\t":
					return "\t";
				default:
					return delimiter;
			}
		}

		protected String getRawDelimiter(ImportCsvSheet spreadsheetImport) {
			String delimiter = spreadsheetImport.getDelimiter();

			if (delimiter != null)
				return delimiter;

			return Optional //
					.ofNullable(
							context.getCmdrContextBuilder().entityType(context.getImportTargetType()).meta(SpreadsheetDataDelimiter.T).exclusive()) //
					.map(SpreadsheetDataDelimiter::getDelimiter) //
					.orElse(defaultDelimiter);
		}

		private void open() throws Exception {
			ImportCsvSheet spreadsheetImport = context.getSpreadsheetImport();
			String delimiter = getDelimiter(spreadsheetImport);
			String charset = getCharset(spreadsheetImport);

			BOMInputStream bomIn = new BOMInputStream(spreadsheetImport.getSheet().openStream());

			if (bomIn.hasBOM())
				charset = bomIn.getBOMCharsetName();

			CSVParser csvParser = new CSVParserBuilder().withSeparator(delimiter.charAt(0)).build();
			reader = new CSVReaderBuilder(new BufferedReader(new InputStreamReader(bomIn, charset))) //
					.withCSVParser(csvParser).build();

			Function<String, String> columnNameAdapter = context.getColumnNameAdapter();
			Map<String, Property> properties = context.getProperties();

			String[] row;

			while ((row = reader.readNext()) != null) {

				Set<String> curNames = new HashSet<String>(properties.keySet());

				List<ColumnInfo> infos = null;
				for (int index = 0; index < row.length; index++) {
					String originalColumnName = row[index];

					if (originalColumnName != null && !originalColumnName.isEmpty()) {
						String columnName = columnNameAdapter.apply(originalColumnName);
						if (curNames.remove(columnName)) {
							if (infos == null)
								infos = new ArrayList<>();

							ColumnInfo info = new ColumnInfo();
							info.cellIndex = index;
							info.property = properties.get(columnName);
							info.columnName = originalColumnName;

							infos.add(info);
						}
					}
				}

				if (infos != null) {// if (curNames.isEmpty()) {
					this.columnInfos = infos;

					if (startRow != null) {
						int c = startRow;

						for (int i = 0; i < c; i++) {
							String[] cells = reader.readNext();
							if (cells == null)
								break;
						}
					}

					return;
				}

			}

			this.columnInfos = Collections.emptyList();
		}

		@Override
		public ParsedEntity next(Supplier<GenericEntity> factory) {
			while (true) {
				Maybe<Row> rowMaybe = nextRow();

				if (rowMaybe.isUnsatisfied()) {
					Row failedRow = rowMaybe.value();
					int rowNum = failedRow != null ? failedRow.rowNum : -1;
					return new ParsedEntity(rowNum, null, extendReason(rowNum, null, rowMaybe.whyUnsatisfied()));
				}

				Row row = rowMaybe.get();
				if (row == null)
					break;

				String[] cells = row.cells;
				GenericEntity entity = null;
				Reason reason = null;

				for (ColumnInfo info : columnInfos) {
					Property property = info.property;

					int cellIndex = info.cellIndex;

					String value = cellIndex < cells.length ? cells[cellIndex] : null;

					if (value == null || value.isEmpty())
						continue;

					try {
						Maybe<Object> convertedPotential = context.convert(value, row.rowNum, info.cellIndex, info.columnName, property);

						if (entity == null)
							entity = factory.get();

						if (convertedPotential.isSatisfied()) {
							property.set(entity, convertedPotential.get());
						} else {
							reason = extendReason(row.rowNum, reason, convertedPotential.whyUnsatisfied());
						}

					} catch (Exception e) {
						throw new IllegalStateException("Could not convert value [" + value + "]  from column [" + info.columnName + "] in row ["
								+ (row.rowNum + 1) + "] for " + property.toString(), e);
					}
				}

				if (entity != null || reason != null) {
					return new ParsedEntity(row.rowNum, entity, reason);
				}
			}

			return null;
		}

		private Reason extendReason(int row, Reason umbrellaReason, Reason reason) {
			if (umbrellaReason == null) {
				umbrellaReason = Reasons.build(IncompleteEntity.T).text("Entity in row [" + (row + 1) + "] could not be successfully imported")
						.toReason();
			}

			umbrellaReason.getReasons().add(reason);

			return umbrellaReason;
		}

		@Override
		public void close() {
			if (reader != null)
				IOTools.closeCloseableUnchecked(reader);
		}
	}

	@Override
	protected String getImportFormatUseCase() {
		return "csv-import";
	}

}
