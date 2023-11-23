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

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

/**
 * Specifies the {@link ImportLocalizationsFromSpreadsheet#getMode() import localizations mode}.
 */
public enum LocalizationsImportMode implements EnumBase {
	/** Completely resets the localizations, i.e. deletes all existing localizations before import. */
	reset,
	/** Updates localizations. */
	update;

	public static final EnumType T = EnumTypes.T(LocalizationsImportMode.class);

	@Override
	public EnumType type() {
		return T;
	}
}
