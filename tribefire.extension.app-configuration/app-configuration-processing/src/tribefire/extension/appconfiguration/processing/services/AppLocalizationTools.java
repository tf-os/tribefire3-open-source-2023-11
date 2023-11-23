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

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import tribefire.extension.appconfiguration.model.AppLocalization;
import tribefire.extension.appconfiguration.model.AppLocalizationEntry;

/**
 * Provides various helpers which can be useful when working with (lists of) {@link AppLocalization}s.
 */
public class AppLocalizationTools {

	private AppLocalizationTools() {
		// no need to instantiate
	}

	/**
	 * Returns union of the keys of the passed <code>localizations</code>.
	 */
	public static Set<String> keys(List<AppLocalization> localizations) {
		return localizations.stream() //
				.flatMap(localization -> localization.getValues().stream() // flatten to get values from all localizations
						.map(AppLocalizationEntry::getKey)) // get key of each entry
				.distinct().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * Returns the locations of the passed <code>localizations</code>.
	 */
	public static Set<String> locations(List<AppLocalization> localizations) {
		return localizations.stream().map(AppLocalization::getLocation) //
				.distinct().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * Returns the localization for the specified <code>location</code> or <code>null</code>, if none of the <code>localizations</code> match.
	 */
	public static AppLocalization localization(List<AppLocalization> localizations, String location) {
		return localizations.stream().filter(it -> location.equals(it.getLocation())).findAny().orElse(null);
	}

	/**
	 * Checks whether the passed <code>localization</code> contains an entry with the specified <code>key</code>.
	 */
	public static boolean containsKey(AppLocalization localization, String key) {
		return localization.getValues().stream().anyMatch(it -> key.equals(it.getKey()));
	}

	/**
	 * Returns the localized value for the specified <code>key</code> or <code>null</code>, if there is no match.
	 */
	public static String localizedValue(AppLocalization localization, String key) {
		AppLocalizationEntry entry = localization.getValues().stream().filter(it -> key.equals(it.getKey())).findAny().orElse(null);
		return entry != null ? entry.getValue() : null;
	}

	/**
	 * Inserts the <code>localization</code> into the list of <code>localizations</code> being aware of the localization order (i.e. order by
	 * location).
	 */
	public static void insertOrdered(AppLocalization localization, List<AppLocalization> localizations) {
		GeneralTools.insertOrdered(localization, localizations, Comparator.comparing(AppLocalization::getLocation));
	}

	/**
	 * Inserts the <code>entry</code> into the list of <code>entries</code> being aware of the entry order (i.e. order by key).
	 */
	public static void insertOrdered(AppLocalizationEntry entry, List<AppLocalizationEntry> entries) {
		GeneralTools.insertOrdered(entry, entries, Comparator.comparing(AppLocalizationEntry::getKey));
	}

	/**
	 * Returns a table string representation of the passed <code>localizations</code>.
	 */
	public static String toTableString(List<AppLocalization> localizations) {
		return AppLocalizationTableIO.writeLocalizationsToTable(localizations).toMultilineString();
	}
}
