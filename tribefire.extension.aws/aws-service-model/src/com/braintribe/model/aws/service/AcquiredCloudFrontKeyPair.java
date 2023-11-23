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
package com.braintribe.model.aws.service;

import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@SelectiveInformation("CloudFront Key Pair ${keyPairId}")
public interface AcquiredCloudFrontKeyPair extends AwsResult {

	EntityType<AcquiredCloudFrontKeyPair> T = EntityTypes.T(AcquiredCloudFrontKeyPair.class);

	String keyGroupId = "keyGroupId";
	String publicKeyPem = "publicKeyPem";

	@Name("Key Group Id")
	String getKeyGroupId();
	void setKeyGroupId(String keyGroupId);

	@Name("Public Key (PEM)")
	String getPublicKeyPem();
	void setPublicKeyPem(String publicKeyPem);

}
