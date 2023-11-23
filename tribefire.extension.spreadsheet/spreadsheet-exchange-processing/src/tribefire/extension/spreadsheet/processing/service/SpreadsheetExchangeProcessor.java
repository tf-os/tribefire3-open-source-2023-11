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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.model.extensiondeployment.script.Script;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.basic.IsAnyType;
import com.braintribe.model.processing.accessrequest.api.AbstractDispatchingAccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.DispatchConfiguration;
import com.braintribe.model.processing.core.expert.api.DenotationMap;
import com.braintribe.model.processing.deployment.script.ScriptEngine;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.stream.CountingOutputStream;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;
import com.braintribe.utils.stream.api.StreamPipes;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import tribefire.extension.spreadsheet.model.exchange.api.data.ImportReport;
import tribefire.extension.spreadsheet.model.exchange.api.data.ModelSpreadsheet;
import tribefire.extension.spreadsheet.model.exchange.api.request.ExportModelSpreadsheet;
import tribefire.extension.spreadsheet.model.exchange.api.request.ImportCsvSheet;
import tribefire.extension.spreadsheet.model.exchange.api.request.ImportExcelSheet;
import tribefire.extension.spreadsheet.model.exchange.api.request.ImportSpreadsheet;
import tribefire.extension.spreadsheet.model.exchange.api.request.ImportSpreadsheetRequest;
import tribefire.extension.spreadsheet.model.exchange.api.request.SpreadsheetExchangeRequest;
import tribefire.extension.spreadsheet.processing.importing.common.SpreadsheetImporter;
import tribefire.extension.spreadsheet.processing.importing.csv.CsvImporter;
import tribefire.extension.spreadsheet.processing.importing.excel.ExcelSheetImporter;

public class SpreadsheetExchangeProcessor extends AbstractDispatchingAccessRequestProcessor<SpreadsheetExchangeRequest, Object> {

	private DenotationMap<Script, ScriptEngine<?>> engines;
	private StreamPipeFactory streamPipeFactory;
	
	@Configurable
	public void setStreamPipeFactory(StreamPipeFactory streamPipeFactory) {
		this.streamPipeFactory = streamPipeFactory;
	}
	
	@Required
	public void setEngines(DenotationMap<Script, ScriptEngine<?>> engines) {
		this.engines = engines;
	}
	
	@Override
	protected void configureDispatching(DispatchConfiguration dispatching) {
		dispatching.register(ImportExcelSheet.T, this::importExcelSheet);
		dispatching.register(ImportCsvSheet.T, this::importCsvSheet);
		dispatching.register(ImportSpreadsheet.T, this::importSpreadsheet);
		dispatching.registerStatefulWithContext(ExportModelSpreadsheet.T, c -> new ModelSpreadsheetExportProcessor(streamPipeFactory));
	}
	
	private ImportReport importSpreadsheet(AccessRequestContext<ImportSpreadsheet> context) {
		ImportSpreadsheet originalRequest = context.getOriginalRequest();
		Resource sheet = originalRequest.getSheet();
		
		if (sheet == null)
			throw new IllegalArgumentException("ImportSpreadsheet.sheet must not be empty");
		
		String filename = sheet.getName();
		
		if (filename == null)
			throw new IllegalArgumentException("ImportSpreadsheet.sheet name must not be empty");
		
		
		final EntityType<? extends ImportSpreadsheetRequest> importRequestType;
		
		// detect specific import request type from filename extension
		String extension = FileTools.getExtension(filename).toLowerCase();
		
		switch (extension) {
			case "xlsx":
				importRequestType = ImportExcelSheet.T;
				break;
			case "csv":
				importRequestType = ImportCsvSheet.T;
				break;
			default:
				throw new IllegalArgumentException("Unsupported file extension for import type detection of filename: " + filename);
		}
		
		// create type specific import request and transfer common properties
		ImportSpreadsheetRequest importRequest = importRequestType.create();
		
		for (Property property: ImportSpreadsheetRequest.T.getProperties()) {
			property.setDirect(importRequest, property.getDirect(originalRequest));
		}
		
		// execute specific request
		return importRequest.eval(context).get();
	}
	private ImportReport importExcelSheet(AccessRequestContext<ImportExcelSheet> context) {
		return process(new ExcelSheetImporter(engines), context);
	}
	
	private ImportReport importCsvSheet(AccessRequestContext<ImportCsvSheet> context) {
		return process(new CsvImporter(engines), context);
	}
	
	
	private <T extends ImportSpreadsheetRequest> ImportReport process(SpreadsheetImporter<T> importer, AccessRequestContext<T> context) {
		importer.setStreamPipeFactory(Optional.ofNullable(streamPipeFactory).orElseGet(StreamPipes::simpleFactory));
		return importer.process(context);
	}

}
