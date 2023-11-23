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
package tribefire.extension.gcp.templates.api;

import java.util.function.Function;

import com.braintribe.model.deployment.Cartridge;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

public interface GcpBinaryProcessTemplateContextBuilder {

	GcpBinaryProcessTemplateContextBuilder setIdPrefix(String idPrefix);

	GcpBinaryProcessTemplateContextBuilder setJsonCredentials(String jsonCredentials);

	GcpBinaryProcessTemplateContextBuilder setPrivateKeyId(String privateKeyId);

	GcpBinaryProcessTemplateContextBuilder setPrivateKey(String privateKey);

	GcpBinaryProcessTemplateContextBuilder setClientId(String clientId);

	GcpBinaryProcessTemplateContextBuilder setClientEmail(String clientEmail);

	GcpBinaryProcessTemplateContextBuilder setTokenServerUri(String tokenServerUri);

	GcpBinaryProcessTemplateContextBuilder setProjectId(String projectId);

	GcpBinaryProcessTemplateContextBuilder setBucketName(String bucketName);

	GcpBinaryProcessTemplateContextBuilder setPathPrefix(String pathPrefix);

	GcpBinaryProcessTemplateContextBuilder setEntityFactory(Function<EntityType<?>, GenericEntity> entityFactory);

	GcpBinaryProcessTemplateContextBuilder setGcpCartridge(Cartridge gcpCartridge);

	GcpBinaryProcessTemplateContextBuilder setGcpModule(com.braintribe.model.deployment.Module gcpModule);

	GcpBinaryProcessTemplateContextBuilder setLookupFunction(Function<String,? extends GenericEntity> lookupFunction);

	GcpBinaryProcessTemplateContext build();
}