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
package tribefire.extension.aws.initializer.wire.contract;

import com.braintribe.model.aws.deployment.S3Region;
import com.braintribe.wire.api.annotation.Decrypt;
import com.braintribe.wire.api.annotation.Default;

import tribefire.cortex.initializer.support.wire.contract.PropertyLookupContract;

/*
 * For compatibility reasons, this is not using the PropertyDefinitionsContract yet. This will be activated later.
 */
public interface RuntimePropertiesContract extends PropertyLookupContract {

	@Decrypt
	String S3_ACCESS_KEY();
	@Decrypt
	String S3_SECRET_ACCESS_KEY();

	String S3_STORAGE_BUCKETNAME();

	String S3_PATH_PREFIX();

	@Default("eu_central_1")
	S3Region S3_REGION();

	@Default("true")
	boolean S3_CREATE_DEFAULT_STORAGE_BINARY_PROCESSOR();

	Integer S3_HTTP_CONNECTION_POOL_SIZE();

	Long S3_CONNECTION_ACQUISITION_TIMEOUT();

	Long S3_CONNECTION_TIMEOUT();

	Long S3_SOCKET_TIMEOUT();

	String S3_URL_OVERRIDE();

	String S3_CLOUDFRONT_BASE_URL();
	String S3_CLOUDFRONT_KEYGROUP_ID();
	@Decrypt
	String S3_CLOUDFRONT_PUBLIC_KEY();
	@Decrypt
	String S3_CLOUDFRONT_PRIVATE_KEY();
}
