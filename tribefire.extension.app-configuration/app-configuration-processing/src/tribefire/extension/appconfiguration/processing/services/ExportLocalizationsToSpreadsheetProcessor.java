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
package tribefire.extension.appconfiguration.processing.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.processing.service.api.ReasonedServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;

import tribefire.extension.appconfiguration.model.AppConfiguration;
import tribefire.extension.appconfiguration.model.AppLocalization;
import tribefire.extension.appconfiguration.model.api.ExportLocalizationsToSpreadsheet;
import tribefire.extension.appconfiguration.model.api.LocalizationsSpreadsheet;

/**
 * Processes {@link ExportLocalizationsToSpreadsheet} requests.
 */
public class ExportLocalizationsToSpreadsheetProcessor
		implements ReasonedServiceProcessor<ExportLocalizationsToSpreadsheet, LocalizationsSpreadsheet> {

	private static final Logger logger = Logger.getLogger(ExportLocalizationsToSpreadsheetProcessor.class);

	private Supplier<PersistenceGmSession> sessionSupplier;

	@Configurable
	@Required
	public void setSessionSupplier(Supplier<PersistenceGmSession> sessionSupplier) {
		this.sessionSupplier = sessionSupplier;
	}

	/**
	 * Exports the localizations of the specified {@link AppConfiguration} to a spreadsheet resource.
	 */
	@Override
	public Maybe<? extends LocalizationsSpreadsheet> processReasoned(ServiceRequestContext context, ExportLocalizationsToSpreadsheet request) {
		PersistenceGmSession session = sessionSupplier.get();
		AppConfiguration appConfiguration = AppConfigurationProcessor.getAppConfiguration(request.getAppConfigurationName(), session, true);

		logger.debug("Exporting localizations to spreadsheet.");

		List<AppLocalization> localizations = appConfiguration.getLocalizations();
		AppConfigurationTable localizationsTable = AppLocalizationTableIO.writeLocalizationsToTable(localizations);

		// missing localizations are marked as missing in AppConfiguration, but should just be empty in the sheet.
		localizationsTable = localizationsTable.cloneAndUnmarkEmptyCells();

		byte[] workbookBytes;
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			TableSpreadsheetIO.addTableToNewSheet(workbook, TableSpreadsheetIO.DEFAULT_SHEET_NAME, localizationsTable);
			workbook.write(outputStream);
			workbookBytes = outputStream.toByteArray();
		} catch (IOException e) {
			return Reasons.build(InternalError.T).text("Error while writing to spreadsheet!").enrich(error -> error.setJavaException(e)).toMaybe();
		}

		Resource spreadsheetResource = Resource.createTransient(InputStreamProvider.fromBytes(workbookBytes));
		spreadsheetResource.setName("localizations.xlsx");
		spreadsheetResource.setMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

		LocalizationsSpreadsheet spreadsheet = LocalizationsSpreadsheet.T.create();
		spreadsheet.setSpreadsheet(spreadsheetResource);

		return Maybe.complete(spreadsheet);
	}

}