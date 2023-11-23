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
package com.braintribe.model.aws.deployment.processor;

import com.braintribe.model.aws.deployment.HasS3Connection;
import com.braintribe.model.cache.HasCacheOptions;
import com.braintribe.model.extensiondeployment.BinaryPersistence;
import com.braintribe.model.extensiondeployment.BinaryRetrieval;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.Pattern;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface S3BinaryProcessor extends BinaryRetrieval, BinaryPersistence, HasS3Connection, HasCacheOptions {

	EntityType<S3BinaryProcessor> T = EntityTypes.T(S3BinaryProcessor.class);

	@Name("Bucket Name")
	@Pattern("^[a-z][a-zA-Z0-9\\-_\\.]+[a-zA-Z0-9]$")
	String getBucketName();
	void setBucketName(String bucketName);

	@Name("Path Prefix")
	String getPathPrefix();
	void setPathPrefix(String pathPrefix);

}
