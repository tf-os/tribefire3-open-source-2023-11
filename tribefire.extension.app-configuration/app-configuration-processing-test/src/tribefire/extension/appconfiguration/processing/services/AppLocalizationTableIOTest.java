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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.braintribe.gwt.utils.genericmodel.GMCoreTools;
import com.braintribe.testing.test.AbstractTest;

import tribefire.extension.appconfiguration.model.AppLocalization;
import tribefire.extension.appconfiguration.model.AppLocalizationEntry;

/**
 * Provides tests for {@link AppLocalizationTableIO}.
 */
public class AppLocalizationTableIOTest extends AbstractTest {

	/**
	 * Creates a list of {@link AppLocalization}s, {@link AppLocalizationTableIO#writeLocalizationsToTable(List) writes} these to a table,
	 * {@link AppLocalizationTableIO#readLocalizationsFromTable(AppConfigurationTable) reads} from that same table and asserts that the lists of
	 * {@link AppLocalization}s are equal.
	 */
	@Test
	public void test() {
		List<AppLocalization> localizations = generateTestLocalizations();
		logger.info("Created test localizations.");

		AppConfigurationTable table = AppLocalizationTableIO.writeLocalizationsToTable(localizations);
		logger.info("Wrote test localizations to table:\n\n" + table.toMultilineString());

		List<AppLocalization> readLocalizations = AppLocalizationTableIO.readLocalizationsFromTable(table);
		logger.info("Read localizations from table.");

		assertThat(GMCoreTools.getDescription(readLocalizations)).isEqualToWithVerboseErrorMessage((GMCoreTools.getDescription(localizations)));
	}

	/**
	 * Generates a few localizations to be used during tests.
	 */
	private static List<AppLocalization> generateTestLocalizations() {
		List<AppLocalization> localizations = new ArrayList<>();

		List<String> locations = List.of("fr", "ge", "us");

		List<String> actions = List.of("action_cancel", "action_do_it");

		locations.forEach(location -> {
			AppLocalization localization = AppLocalization.T.create();
			localizations.add(localization);

			localization.setLocation(location);
			localization.setActive(true);

			actions.forEach(action -> {
				AppLocalizationEntry entry = AppLocalizationEntry.T.create();
				entry.setKey(action);
				entry.setValue("[" + location + " localized value of " + action + "]");
				localization.getValues().add(entry);
			});
		});
		return localizations;
	}
}
