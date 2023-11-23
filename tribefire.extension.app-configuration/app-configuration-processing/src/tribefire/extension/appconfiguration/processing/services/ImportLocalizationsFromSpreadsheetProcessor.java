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

import static tribefire.extension.appconfiguration.processing.services.GeneralTools.countAndName;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.UnreachableCodeException;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gwt.utils.genericmodel.GMCoreTools;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.service.api.ReasonedServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.lcd.CommonTools;

import tribefire.extension.appconfiguration.model.AppConfiguration;
import tribefire.extension.appconfiguration.model.AppLocalization;
import tribefire.extension.appconfiguration.model.AppLocalizationEntry;
import tribefire.extension.appconfiguration.model.api.ImportLocalizationsFromSpreadsheet;
import tribefire.extension.appconfiguration.model.api.LocalizationsImportMode;
import tribefire.extension.appconfiguration.model.api.LocalizationsImportReport;
import tribefire.extension.appconfiguration.processing.services.GeneralTools.EntityCreator;
import tribefire.extension.appconfiguration.processing.services.GeneralTools.SessionEntityCreator;

/**
 * Processes {@link ImportLocalizationsFromSpreadsheet} requests.
 */
public class ImportLocalizationsFromSpreadsheetProcessor
		implements ReasonedServiceProcessor<ImportLocalizationsFromSpreadsheet, LocalizationsImportReport> {

	private static final Logger logger = Logger.getLogger(ImportLocalizationsFromSpreadsheetProcessor.class);

	private Supplier<PersistenceGmSession> sessionSupplier;

	@Configurable
	@Required
	public void setSessionSupplier(Supplier<PersistenceGmSession> sessionSupplier) {
		this.sessionSupplier = sessionSupplier;
	}

	/**
	 * Reads localizations from the specified spreadsheet resource and imports them into of the specified
	 * {@link AppConfiguration}.
	 */
	@Override
	public Maybe<? extends LocalizationsImportReport> processReasoned(ServiceRequestContext context, ImportLocalizationsFromSpreadsheet request) {
		PersistenceGmSession session = sessionSupplier.get();
		AppConfiguration appConfiguration = AppConfigurationProcessor.getAppConfiguration(request.getAppConfigurationName(), session, true);

		logger.debug("Reading localizations from spreadsheet.");

		Resource spreadsheetResource = request.getSpreadsheet();
		if (!spreadsheetResource.isTransient()) {
			spreadsheetResource = session.query().entity(Resource.T, spreadsheetResource.getId()).require();
		}

		Maybe<AppConfigurationTable> maybeTable;
		try (InputStream inputStream = spreadsheetResource.openStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
			maybeTable = TableSpreadsheetIO.readTableFromSheet(workbook, TableSpreadsheetIO.DEFAULT_SHEET_NAME, AppConfigurationTable.class);
		} catch (IOException e) {
			return Reasons.build(InternalError.T).text("Error while reading spreadsheet!").enrich(error -> error.setJavaException(e)).toMaybe();
		}

		Reason reason = maybeTable.whyUnsatisfied();
		if (reason == null) {
			reason = maybeTable.get().assertValid();
		}
		if (reason != null) {
			return Reasons.build(Reason.T).text("Error while importing localizations. Spreadsheet contains invalid data.").cause(reason).toMaybe();
		}

		if (request.getMode().equals(LocalizationsImportMode.reset)) {
			logger.debug("Removing existing localizations before import.");
			appConfiguration.getLocalizations().clear();
		}

		logger.debug("Importing localizations.");

		AppConfigurationTable table = maybeTable.get();

		// missing localizations are just empty cells in the sheet, but they must not be empty in AppConfiguration.
		table = table.cloneAndMarkEmptyCells();

		List<AppLocalization> importedLocalizations = AppLocalizationTableIO.readLocalizationsFromTable(table);
		LocalizationsImportReport report = LocalizationsImportReport.T.create();
		importLocalizations(importedLocalizations, appConfiguration.getLocalizations(), report, new SessionEntityCreator(session), session);

		logger.debug("Imported localizations:\n" + report.getStatistics());

		logger.debug("Updated " + AppConfiguration.T.getShortName() + ":\n" + GMCoreTools.getDescription(appConfiguration));

		session.commit();

		return Maybe.complete(report);
	}

	/**
	 * Imports <code>importedLocalizations</code> into the <code>existingLocalizations</code>, adding respective information
	 * to the <code>report</code>.
	 * 
	 * @param session
	 */
	static void importLocalizations(List<AppLocalization> importedLocalizations, List<AppLocalization> existingLocalizations,
			LocalizationsImportReport report, EntityCreator entityCreator, PersistenceGmSession session) {

		Set<String> importedLocations = AppLocalizationTools.locations(importedLocalizations);
		Set<String> existingLocations = AppLocalizationTools.locations(existingLocalizations);

		Set<String> importedKeys = AppLocalizationTools.keys(importedLocalizations);
		Set<String> existingKeys = AppLocalizationTools.keys(existingLocalizations);

		List<String> locations = GeneralTools.distinctSortedUnion(importedLocations, existingLocations);
		List<String> keys = GeneralTools.distinctSortedUnion(importedKeys, existingKeys);

		Map<ImportStatus, Integer> statistics = new EnumMap<>(ImportStatus.class);
		Arrays.asList(ImportStatus.values()).stream().forEach(status -> statistics.put(status, 0));

		List<String> details = new ArrayList<>();
		for (String location : locations) {
			AppLocalization importedLocalization = AppLocalizationTools.localization(importedLocalizations, location);
			AppLocalization existingLocalization = AppLocalizationTools.localization(existingLocalizations, location);

			if (existingLocalization == null) {
				existingLocalization = entityCreator.create(AppLocalization.T);
				existingLocalization.setLocation(location);
				existingLocalization.setActive(true);
				AppLocalizationTools.insertOrdered(existingLocalization, existingLocalizations);
				if (session != null) {
					session.commit();
				}
			}

			for (String key : keys) {
				ImportStatus status;
				if (importedLocalization == null || !AppLocalizationTools.containsKey(importedLocalization, key)) {
					// location not imported at all and/or key not imported for respective localization
					status = ImportStatus.missing;
				} else {
					String importedValue = AppLocalizationTools.localizedValue(importedLocalization, key);
					if (importedValue == null) {
						throw new UnreachableCodeException("Imported localized value for key '" + key + "' and location '" + location
								+ "' is null. It should not be provided at all!");
					} else {
						status = importLocalizedValue(key, importedLocalization, existingLocalization, entityCreator);
					}
				}

				statistics.put(status, statistics.get(status) + 1);
				details.add(key + " - " + location + ": " + status);
			}
		}

		Collections.sort(details);
		report.getDetails().addAll(details);

		StringBuilder statisticsBuilder = new StringBuilder();
		statisticsBuilder.append("Statistics:\n");

		List.of( //
				List.of("Existing", existingKeys, existingLocations), //
				List.of("Imported", importedKeys, importedLocations), //
				List.of("Merged", keys, locations) //
		).forEach(it -> {
			String itDescription = (String) it.get(0);
			Collection<?> itKeys = (Collection<?>) it.get(1);
			Collection<?> itLocations = (Collection<?>) it.get(2);
			int itLocalizedValuesCount = itKeys.size() * itLocations.size();

			statisticsBuilder.append(itDescription + ": " + countAndName(itKeys, "key") + ", " + countAndName(itLocations, "location") + ", "
					+ countAndName(itLocalizedValuesCount, "localized value") + "\n");
		});

		statisticsBuilder.append("Localized values: " + Arrays.asList(ImportStatus.values()).stream()
				.map(status -> status + ": " + statistics.get(status)).collect(Collectors.joining(", ")) + "\n");

		report.setStatistics(statisticsBuilder.toString());
	}

	/**
	 * Imports the localized value with the specified <code>key</code> from the <code>importedLocalization</code> into the
	 * <code>existingLocalization</code> and returns the respective {@link ImportStatus}.
	 */
	private static ImportStatus importLocalizedValue(String key, AppLocalization importedLocalization, AppLocalization existingLocalization,
			EntityCreator entityCreator) {
		ImportStatus status;

		if (importedLocalization == null || !AppLocalizationTools.containsKey(importedLocalization, key)) {
			// location not imported at all and/or key not imported for respective localization
			status = ImportStatus.missing;
		} else {
			String importedValue = AppLocalizationTools.localizedValue(importedLocalization, key);
			if (importedValue.isEmpty()) {
				// value has explicitly been deleted -> removed respective localized value entry
				existingLocalization.getValues().removeIf(entry -> entry.getKey().equals(key));
				status = ImportStatus.removed;
			} else {

				AppLocalizationEntry entry = existingLocalization.getValues().stream().filter(it -> key.equals(it.getKey())).findAny().orElse(null);
				if (entry != null) {
					if (CommonTools.equalsOrBothNull(importedValue, entry.getValue())) {
						status = ImportStatus.unmodified;
					} else {
						entry.setValue(importedValue);
						status = ImportStatus.updated;
					}

				} else {
					entry = entityCreator.create(AppLocalizationEntry.T);
					entry.setKey(key);
					entry.setValue(importedValue);
					status = ImportStatus.added;
					AppLocalizationTools.insertOrdered(entry, existingLocalization.getValues());
				}
			}
		}
		return status;
	}

	/**
	 * Describes the status of an localized value entry after the import.
	 */
	public enum ImportStatus {
		/** Localized value was added, i.e. didn't exist before the import. */
		added,
		/** Localized value was updated, i.e. it existed before, but the value changed during the import. */
		updated,
		/** Localized value existed before and was not modified during the import. */
		unmodified,
		/** Localized value was removed during the import, i.e. it existed before and now no longer does. */
		removed,
		/**
		 * Localized value was missing in the import, i.e. existed before, was not imported and thus still exists with
		 * unmodified value.
		 */
		missing;
	}
}