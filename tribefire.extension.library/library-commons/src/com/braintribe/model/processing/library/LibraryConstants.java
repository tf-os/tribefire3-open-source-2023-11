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
package com.braintribe.model.processing.library;

public interface LibraryConstants {

	String LIBRARY_ACCESS_ID = "library.access";

	String CARTRIDGE_GROUPID = "tribefire.extension.library";

	String MODULE_GLOBAL_ID = "module://" + CARTRIDGE_GROUPID + ":library-module";

	String DEPLOYMENT_MODEL_QUALIFIEDNAME = CARTRIDGE_GROUPID + ":library-deployment-model";
	String SERVICE_MODEL_QUALIFIEDNAME = CARTRIDGE_GROUPID + ":library-service-model";
	String DATA_MODEL_QUALIFIEDNAME = CARTRIDGE_GROUPID + ":library-model";

}
