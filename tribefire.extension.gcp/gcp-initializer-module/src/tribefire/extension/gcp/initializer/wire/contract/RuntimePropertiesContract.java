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
package tribefire.extension.gcp.initializer.wire.contract;

import tribefire.cortex.initializer.support.wire.contract.PropertyLookupContract;
import com.braintribe.wire.api.annotation.Decrypt;
import com.braintribe.wire.api.annotation.Default;

/*
 * For compatibility reasons, this is not using the PropertyDefinitionsContract yet. This will be activated later.
 */
public interface RuntimePropertiesContract extends PropertyLookupContract {

	@Decrypt
	String GCP_JSON_CREDENTIALS();

	String GCP_PRIVATE_KEY_ID();
	@Decrypt
	String GCP_PRIVATE_KEY();
	String GCP_CLIENT_ID();
	String GCP_CLIENT_EMAIL();
	String GCP_TOKEN_SERVER_URI();
	String GCP_PROJECT_ID();
	
	String GCP_STORAGE_BUCKETNAME();

	String GCP_PATH_PREFIX();

	@Default("true")
	boolean GCP_CREATE_DEFAULT_STORAGE_BINARY_PROCESSOR();
}
