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
package com.braintribe.devrock.importer.scanner.ui;

public interface HasQuickImportTokens {
	static final String MARKER_ARTIFACT = "artifact";
	static final String MARKER_AVAILABLE = "available";
	static final String DATA_AVAILABLITY_STATE = "availablity";
	static final String MARKER_TOOLTIP = "tooltip";
	static final String NO_IMPORT = "cannot be imported as it exists in the workspace or in the current working set";
	static final String AVAILABLE_IMPORT = "can be imported as it doesn't exists in the workspace or in the current working set";

}
