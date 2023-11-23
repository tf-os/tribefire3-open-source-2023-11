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
package tribefire.extension.spreadsheet.processing.service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.basic.IsAnyType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.accessrequest.api.AbstractStatefulAccessRequestProcessor;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.stream.CountingOutputStream;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;
import com.braintribe.utils.stream.api.StreamPipes;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import tribefire.extension.spreadsheet.model.exchange.api.data.ExportModelColumn;
import tribefire.extension.spreadsheet.model.exchange.api.data.ModelSpreadsheet;
import tribefire.extension.spreadsheet.model.exchange.api.request.ExportModelSpreadsheet;

public class ModelSpreadsheetExportProcessor extends AbstractStatefulAccessRequestProcessor<ExportModelSpreadsheet, ModelSpreadsheet> {

	private StreamPipeFactory streamPipeFactory;

	public ModelSpreadsheetExportProcessor(StreamPipeFactory streamPipeFactory) {
		super();
		this.streamPipeFactory = streamPipeFactory;
	}
	
	@Override
	public ModelSpreadsheet process() {
		PersistenceGmSession systemSession = context().getSystemSession();
		
		ModelAccessory modelAccessory = systemSession.getModelAccessory();
		
		StreamPipeFactory pipeFactory = Optional.ofNullable(streamPipeFactory).orElseGet(StreamPipes::simpleFactory);
		
		StreamPipe pipe = pipeFactory.newPipe("spreadheet-model-export");
		
		StreamInfo streamInfo = new StreamInfo();
		
		ExportModelSpreadsheet request = context().getOriginalRequest();
		
		TypeCondition propertyTypeFilter = Optional.ofNullable(request.getPropertyTypeFilter()).orElseGet(IsAnyType.T::create);
		TypeCondition typeFilter = Optional.ofNullable(request.getTypeFilter()).orElseGet(IsAnyType.T::create);
		
		try (ICSVWriter csvWriter = new CSVWriterBuilder(openUtf8WriterWithBom(streamInfo.openOutputStream(pipe.openOutputStream()))) //
				.withSeparator(request.getDelimiter().charAt(0)).build()) {
		
			ColumnProjection projection = new ColumnProjection(request.getSelect());
			
			csvWriter.writeNext(projection.headerRow(), false);
			
			List<EntityTypeOracle> oracles = modelAccessory.getOracle().getTypes().onlyEntities().asTypeOracles() //
					.map(o -> (EntityTypeOracle)o)
					.filter(o -> typeFilter.matches((EntityType<?>) o.asType()))
					.sorted(this::compareTypes).collect(Collectors.toList());

			for (EntityTypeOracle oracle: oracles) {
				EntityType<?> et = oracle.asType();
				Pair<String, String> packageAndName = splitPackageAndName(et.getTypeSignature());
				
				Iterable<GmProperty> iterable = oracle.getProperties().onlyDeclared().asGmProperties() //
						.filter(p -> propertyTypeFilter.matches(p.getType()))::iterator;
				
				for (GmProperty property: iterable) {
					csvWriter.writeNext(projection.dataRowFrom(property), false);
				}
			}
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		Date now = new Date();
		
		Resource resource = Resource.createTransient(pipe::openInputStream);
		resource.setCreated(now);
		resource.setCreator(context().getRequestorUserName());
		resource.setFileSize(streamInfo.getSize());
		resource.setMd5(streamInfo.getMd5());
		resource.setMimeType("text/csv");
		
		StringBuilder fileNameBuilder = new StringBuilder("model-export-");
		fileNameBuilder.append(systemSession.getAccessId());
		fileNameBuilder.append('-');
		fileNameBuilder.append(formatDateForFilename(now));
		fileNameBuilder.append(".csv");
		
		resource.setName(fileNameBuilder.toString());
		
		ModelSpreadsheet result = ModelSpreadsheet.T.create();
		result.setSheet(resource);
		
		return result;
	}
	
	private static Writer openUtf8WriterWithBom(OutputStream out) throws IOException {
		Writer writer = new OutputStreamWriter(out, "UTF-8");
		
		try {
			writer.write('\ufeff');
			return writer;
		}
		catch (IOException e) {
			writer.close();
			throw e;
		}
	}
	
	private int compareTypes(EntityTypeOracle o1, EntityTypeOracle o2) {
		String m1 = o1.asGmType().getDeclaringModel().getName();
		String m2 = o2.asGmType().getDeclaringModel().getName();
		
		int res = m1.compareTo(m2);
		
		if (res != 0)
			return res;
		
		Pair<String, String> p1 = splitPackageAndName(o1.asType().getTypeSignature());
		Pair<String, String> p2 = splitPackageAndName(o2.asType().getTypeSignature());

		res = p1.first().compareTo(p2.first());
		
		if (res != 0)
			return res;
		
		return p1.second().compareTo(p2.second());
	}
	
	private Pair<String, String> splitPackageAndName(String s) {
		int index = s.lastIndexOf('.');
		
		String packageName = s.substring(0, index);
		String simpleName = s.substring(index + 1);
		
		return Pair.of(packageName, simpleName);
	}
	
	
	private String formatDateForFilename(Date now) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss").withLocale(Locale.getDefault());
		ZonedDateTime dateTime = ZonedDateTime.ofInstant(now.toInstant(), ZoneOffset.systemDefault());
		String formattedDate = formatter.format(dateTime);
		return formattedDate;
	}

	private static class StreamInfo {
		private MessageDigest messageDigest;
		private CountingOutputStream countingOut;
		private OutputStream originalOut;
		private OutputStream enrichedOut;
		
		public StreamInfo() {
		}
		
		public OutputStream openOutputStream(OutputStream out) {
			this.originalOut = out;
			this.countingOut = new CountingOutputStream(out);
			try {
				this.messageDigest = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
			this.enrichedOut = new DigestOutputStream(countingOut, messageDigest);

			return enrichedOut;
		}
		
		public String getMd5() {
			return StringTools.toHex(messageDigest.digest());
		}
		
		public long getSize() {
			return countingOut.getCount();
		}
	}

	
	
	private class ColumnProjection {
		
		private List<Function<GmProperty, String>> projectors;
		private List<ExportModelColumn> columns;
		
		public ColumnProjection(List<ExportModelColumn> columns) {
			if (columns.isEmpty()) {
				columns = Arrays.asList(
						ExportModelColumn.model,
						ExportModelColumn.packageName,
						ExportModelColumn.simpleTypeName,
						ExportModelColumn.propertyName,
						ExportModelColumn.qualifiedPropertyType
					);
			}
			
			this.columns = columns;
			projectors = new ArrayList<>(columns.size());
			
			for (ExportModelColumn column: columns) {
				projectors.add(projector(column));
			}
		}
		
		private Function<GmProperty, String> projector(ExportModelColumn column) {
			switch (column) {
				case model: return p -> p.getDeclaringType().getDeclaringModel().getName();
				case packageName: return p -> splitPackageAndName(p.getDeclaringType().getTypeSignature()).first();
				case propertyName: return GmProperty::getName;
				case qualifiedPropertyType: return p -> p.getType().getTypeSignature();
				case qualifiedTypeName: return p -> p.getDeclaringType().getTypeSignature();
				case simpleTypeName: return p-> splitPackageAndName(p.getDeclaringType().getTypeSignature()).second();
				default:
					throw new IllegalStateException("Unkown column: " + column);
			}
		}
		
		public String[] headerRow() {
			String[] row = new String[columns.size()];
			
			for (int i = 0; i < columns.size(); i++) {
				row[i] = columns.get(i).name();
			}
			
			return row;
		}
		
		public String[] dataRowFrom(GmProperty property) {
			String[] row = new String[columns.size()];

			for (int i = 0; i < projectors.size(); i++) {
				row[i] = projectors.get(i).apply(property);
			}
			
			return row;
		}
	}
}
