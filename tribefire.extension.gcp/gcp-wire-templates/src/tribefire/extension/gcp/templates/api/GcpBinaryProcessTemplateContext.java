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

import com.braintribe.model.deployment.Cartridge;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.wire.api.scope.InstanceConfiguration;

public interface GcpBinaryProcessTemplateContext {

	String getIdPrefix();

	String getJsonCredentials();

	String getPrivateKeyId();

	String getPrivateKey();

	String getClientId();

	String getClientEmail();

	String getTokenServerUri();

	String getProjectId();

	String getBucketName();

	String getPathPrefix();
	
	Cartridge getGcpCartridge();
	
	com.braintribe.model.deployment.Module getGcpModule();
	
	<T extends GenericEntity> T lookup(String globalId);

	<T extends GenericEntity> T create(EntityType<T> entityType, InstanceConfiguration instanceConfiguration);
	
	static GcpBinaryProcessTemplateContextBuilder builder() {
		return new GcpBinaryProcessTemplateContextImpl();
	}
}