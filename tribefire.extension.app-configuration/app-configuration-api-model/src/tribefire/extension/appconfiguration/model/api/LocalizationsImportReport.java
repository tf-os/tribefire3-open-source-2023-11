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
package tribefire.extension.appconfiguration.model.api;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A report providing information about an import of {@link tribefire.extension.appconfiguration.model.AppConfiguration#getLocalizations()
 * localizations}, e.g. from a {@link ImportLocalizationsFromSpreadsheet spreadsheet}.
 */
public interface LocalizationsImportReport extends GenericEntity {

	EntityType<LocalizationsImportReport> T = EntityTypes.T(LocalizationsImportReport.class);

	/** A text providing some statistics such as the number of added or modified entries. */
	String getStatistics();
	void setStatistics(String statistics);

	/** Multiple text messages providing detailed information about all localized values and there status (e.g. added, updated, etc.). */
	List<String> getDetails();
	void setDetails(List<String> details);
}
