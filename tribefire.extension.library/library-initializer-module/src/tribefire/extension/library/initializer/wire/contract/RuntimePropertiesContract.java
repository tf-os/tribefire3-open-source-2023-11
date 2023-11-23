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
package tribefire.extension.library.initializer.wire.contract;

import com.braintribe.model.library.deployment.service.Profile;
import com.braintribe.wire.api.annotation.Decrypt;
import com.braintribe.wire.api.annotation.Default;

import tribefire.cortex.initializer.support.wire.contract.PropertyLookupContract;

public interface RuntimePropertiesContract extends PropertyLookupContract {

	String LIBRARY_DB_URL(String defaulValue);

	@Default("org.postgresql.Driver")
	String LIBRARY_DB_DRIVER();
	
	@Default("<user>")
	String LIBRARY_DB_USER();

	@Decrypt
	String LIBRARY_DB_PASSWORD_ENC();

	@Default("doesnotexist")
	String LIBRARY_WKHTMLTOPDF();

	@Default("dev")
	Profile LIBRARY_PROFILE();
	
	String LIBRARY_LOCAL_REPOSITORY_PATH(String defaultValue);

	String LIBRARY_REPOSITORY_USERNAME();

	@Decrypt
	String LIBRARY_REPOSITORY_PASSWORD_ENC();

	@Default("0 0 2 * * ?")
	String LIBRARY_NVD_MIRROR_UPDATE_CRONTAB();
	
	String LIBRARY_NVD_MIRROR_PATH(String defaultValue);
	
	String LIBRARY_REPOSITORY_URL();
	String LIBRARY_RAVENHURST_URL();
}
