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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static tribefire.extension.appconfiguration.processing.services.GeneralTools.list;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Test;

import com.braintribe.gwt.utils.genericmodel.GMCoreTools;
import com.braintribe.testing.test.AbstractTest;

import tribefire.extension.appconfiguration.model.AppLocalization;
import tribefire.extension.appconfiguration.model.api.LocalizationsImportReport;
import tribefire.extension.appconfiguration.processing.services.GeneralTools.SessionlessEntityCreator;

/**
 * Provides tests for {@link ImportLocalizationsFromSpreadsheetProcessor}.
 */
public class ImportLocalizationsFromSpreadsheetProcessorTest extends AbstractTest {

	/**
	 * Imports localizations and verifies that the merged result is correct. Also checks the respective report. Note that
	 * this method tests
	 * {@link ImportLocalizationsFromSpreadsheetProcessor#importLocalizations(List, List, LocalizationsImportReport, tribefire.extension.appconfiguration.processing.services.GeneralTools.EntityCreator)}
	 * and not
	 * {@link ImportLocalizationsFromSpreadsheetProcessor#processReasoned(com.braintribe.model.processing.service.api.ServiceRequestContext, tribefire.extension.appconfiguration.model.api.ImportLocalizationsFromSpreadsheet)}.
	 * Therefore it is not aware of marked/unmarked empty cells and the test cases assume that null values are just ignored.
	 * That's also because the requirement to mark cells (and only in AppConfiguration) was introduced after initial
	 * implementation.
	 */
	@Test
	public void test() {
		// empty lists
		importAndCheck( //
				localizations(list()), //
				localizations(list()), //
				localizations(list()) //
		);

		// nothing to import
		importAndCheck( //
				localizations(list()), //
				localizations( //
						list("ge", "us"), //
						list("key_thing", "ding", "thing") //
				), //
				localizations( //
						list("ge", "us"), //
						list("key_thing", "ding", "thing") //
				) //
		);

		// nothing existing
		importAndCheck( //
				localizations( //
						list("ge", "us"), //
						list("key_thing", "ding", "thing") //
				), //
				localizations(list()), //
				localizations( //
						list("ge", "us"), //
						list("key_thing", "ding", "thing") //
				) //
		);

		// new location
		importAndCheck( //
				localizations( //
						list("fr", "ge", "us"), //
						list("key_thing", "chose", "ding", "thing") //
				), //
				localizations( //
						list("ge", "us"), //
						list("key_thing", "ding", "thing") //
				), //
				localizations( //
						list("fr", "ge", "us"), //
						list("key_thing", "chose", "ding", "thing") //
				) //
		);

		// new location (others skipped)
		importAndCheck( //
				localizations( //
						list("fr"), //
						list("key_thing", "chose") //
				), //
				localizations( //
						list("ge", "us"), //
						list("key_thing", "ding", "thing") //
				), //
				localizations( //
						list("fr", "ge", "us"), //
						list("key_thing", "chose", "ding", "thing") //
				) //
		);

		// new location and key (others skipped)
		importAndCheck( //
				localizations( //
						list("fr"), //
						list("key_thing", "chose"), //
						list("key_number", "nombre") //
				), //
				localizations( //
						list("ge", "us"), //
						list("key_thing", "ding", "thing") //
				), //
				localizations( //
						list("fr", "ge", "us"), //
						list("key_number", "nombre", null, null), //
						list("key_thing", "chose", "ding", "thing") //
				) //
		);

		// single update
		importAndCheck( //
				localizations( //
						list("ge", "us"), //
						list("key_thing", "ding", "thing-new") //
				), //
				localizations( //
						list("ge", "us"), //
						list("key_thing", "ding", "thing") //
				), //
				localizations( //
						list("ge", "us"), //
						list("key_thing", "ding", "thing-new") //
				), //
				report -> {
					assertThat(report.getStatistics()).contains("Localized values: added: 0, updated: 1, unmodified: 1, removed: 0, missing: 0");
				});

		// nulls (interpreted as missing) and empty strings (interpreted as removed)
		importAndCheck( //
				localizations( //
						list("ge", "us"), //
						list("key_thing", null, "") //
				), //
				localizations( //
						list("ge", "us"), //
						list("key_thing", "ding", "thing") //
				), //
				localizations( //
						list("ge", "us"), //
						list("key_thing", "ding", null) //
				), //
				report -> {
					assertThat(report.getStatistics()).contains("Localized values: added: 0, updated: 0, unmodified: 0, removed: 1, missing: 1");
				});

		// statistics test
		importAndCheck( //
				localizations( //
						list("loc1", "loc2", "loc4"), //
						list("key_a", "a-loc1", "a-loc2-updated", "a-loc4"), //
						list("key_b", "", null, "b-loc4"), //
						list("key_d", "d-loc1", "d-loc2", "d-loc4") //
				), //
				localizations( //
						list("loc1", "loc2", "loc3"), //
						list("key_a", "a-loc1", "a-loc2", "a-loc3"), //
						list("key_b", "b-loc1", "b-loc2", "b-loc3"), //
						list("key_c", "c-loc1", "c-loc2", "c-loc3") //
				), //
				localizations( //
						list("loc1", "loc2", "loc3", "loc4"), //
						list("key_a", "a-loc1", "a-loc2-updated", "a-loc3", "a-loc4"), //
						list("key_b", null, "b-loc2", "b-loc3", "b-loc4"), //
						list("key_c", "c-loc1", "c-loc2", "c-loc3", null), //
						list("key_d", "d-loc1", "d-loc2", null, "d-loc4") //
				), //
				report -> {
					assertThat(report.getStatistics()).contains("""
							Existing: 3 keys, 3 locations, 9 localized values
							Imported: 3 keys, 3 locations, 9 localized values
							Merged: 4 keys, 4 locations, 16 localized values
							Localized values: added: 5, updated: 1, unmodified: 1, removed: 1, missing: 8""");
				});
	}

	/**
	 * Delegates to {@link #importAndCheck(List, List, List, Consumer)} with no additional checker.
	 */
	private void importAndCheck(List<AppLocalization> importedLocalizations, List<AppLocalization> existingLocalizations,
			List<AppLocalization> expectedMergedLocalizations) {
		importAndCheck(importedLocalizations, existingLocalizations, expectedMergedLocalizations, null);
	}

	/**
	 * Imports localizations and verifies the result. The resulting {@link LocalizationsImportReport} is passed to the
	 * <code>checker</code> (if set), which allows for performing additional custom checks.
	 */
	private void importAndCheck(List<AppLocalization> importedLocalizations, List<AppLocalization> existingLocalizations,
			List<AppLocalization> expectedMergedLocalizations, Consumer<LocalizationsImportReport> checker) {
		String importedLocalizationsTableString = AppLocalizationTools.toTableString(importedLocalizations);
		String existingLocalizationsTableString = AppLocalizationTools.toTableString(existingLocalizations);
		String expectedMergedTableString = AppLocalizationTools.toTableString(expectedMergedLocalizations);

		LocalizationsImportReport report = LocalizationsImportReport.T.create();

		// the method will modify existing localizations -> ensure list is modifiable
		List<AppLocalization> actualMergedLocalizations = new ArrayList<>(existingLocalizations);
		ImportLocalizationsFromSpreadsheetProcessor.importLocalizations(importedLocalizations, actualMergedLocalizations, report,
				new SessionlessEntityCreator(), null);

		String actualMergedTableString = AppLocalizationTools.toTableString(actualMergedLocalizations);

		logger.info("");
		logger.info("--------------------------------------------------------------------------------");
		logger.info("Localizations import test:\n" //
				+ "Imported:\n" + importedLocalizationsTableString //
				+ "Existing:\n" + existingLocalizationsTableString //
				+ "Expected merged:\n" + expectedMergedTableString //
				+ "Actual merged:\n" + actualMergedTableString //
				+ "Report statistics:\n" + report.getStatistics() //
		);

		// assert table representations match
		assertThat(actualMergedTableString).isEqualToWithVerboseErrorMessage(expectedMergedTableString);
		// in addition, just to be safe, also assert that GM data matches
		assertThat(GMCoreTools.getDescription(actualMergedLocalizations))
				.isEqualToWithVerboseErrorMessageAndLogging(GMCoreTools.getDescription(expectedMergedLocalizations));

		if (checker != null) {
			// run additional custom checks
			checker.accept(report);
		}
		logger.info("--------------------------------------------------------------------------------");
		logger.info("");
	}

	/**
	 * Returns a list of {@link AppLocalization}s created based on the specified <code>locations</code> (the header row of
	 * the table) and <code>keysAndLocalizedValues</code> (the other rows of the table).
	 */
	@SafeVarargs
	private static List<AppLocalization> localizations(List<String> locations, List<String>... keysAndLocalizedValues) {
		AppConfigurationTable table = new AppConfigurationTable(locations);

		if (keysAndLocalizedValues != null) {
			for (List<String> keyAndLocalizedValues : keysAndLocalizedValues) {
				table.addCellsToNewRow(keyAndLocalizedValues);
			}
		}
		return AppLocalizationTableIO.readLocalizationsFromTable(table);
	}
}
