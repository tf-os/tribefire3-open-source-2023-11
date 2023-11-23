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
package tribefire.extension.aws.templates.api;

import java.util.function.Function;

import com.braintribe.model.aws.deployment.S3Region;
import com.braintribe.model.deployment.Cartridge;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

public interface S3BinaryProcessTemplateContextBuilder {

	S3BinaryProcessTemplateContextBuilder setIdPrefix(String idPrefix);

	S3BinaryProcessTemplateContextBuilder setName(String name);

	S3BinaryProcessTemplateContextBuilder setAwsAccessKey(String awsAccessKey);

	S3BinaryProcessTemplateContextBuilder setAwsSecretAccessKey(String awsSecretAccessKey);

	S3BinaryProcessTemplateContextBuilder setHttpConnectionPoolSize(Integer httpConnectionPoolSize);

	S3BinaryProcessTemplateContextBuilder setConnectionAcquisitionTimeout(Long connectionAcquisitionTimeout);

	S3BinaryProcessTemplateContextBuilder setConnectionTimeout(Long connectionTimeout);

	S3BinaryProcessTemplateContextBuilder setSocketTimeout(Long socketTimeout);

	S3BinaryProcessTemplateContextBuilder setUrlOverride(String urlOverride);

	S3BinaryProcessTemplateContextBuilder setCloudFrontBaseUrl(String cloudFrontBaseUrl);
	S3BinaryProcessTemplateContextBuilder setCloudFrontPrivateKey(String cloudFrontPrivateKey);
	S3BinaryProcessTemplateContextBuilder setCloudFrontPublicKey(String cloudFrontPublicKey);
	S3BinaryProcessTemplateContextBuilder setCloudFrontKeyGroupId(String cloudFrontKeyGroupId);

	S3BinaryProcessTemplateContextBuilder setRegion(S3Region region);

	S3BinaryProcessTemplateContextBuilder setPathPrefix(String pathPrefix);

	S3BinaryProcessTemplateContextBuilder setBucketName(String bucketName);

	S3BinaryProcessTemplateContextBuilder setEntityFactory(Function<EntityType<?>, GenericEntity> entityFactory);

	S3BinaryProcessTemplateContextBuilder setAwsCartridge(Cartridge awsCartridge);

	S3BinaryProcessTemplateContextBuilder setAwsModule(com.braintribe.model.deployment.Module awsModule);

	S3BinaryProcessTemplateContextBuilder setLookupFunction(Function<String, ? extends GenericEntity> lookupFunction);

	S3BinaryProcessTemplateContext build();
}