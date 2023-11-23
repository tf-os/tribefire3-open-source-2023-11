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
package com.braintribe.model.cortex.deployment.cors;

import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface CorsConfiguration extends GenericEntity {

	EntityType<CorsConfiguration> T = EntityTypes.T(CorsConfiguration.class);

	boolean getAllowAnyOrigin();
	void setAllowAnyOrigin(boolean allowAnyOrigin);

	Set<String> getAllowedOrigins();
	void setAllowedOrigins(Set<String> allowedOrigins);

	int getMaxAge();
	void setMaxAge(int maxAge);

	Set<String> getSupportedMethods();
	void setSupportedMethods(Set<String> supportedMethods);

	boolean getSupportAnyHeader();
	void setSupportAnyHeader(boolean supportAnyHeader);

	Set<String> getSupportedHeaders();
	void setSupportedHeaders(Set<String> supportedHeaders);

	Set<String> getExposedHeaders();
	void setExposedHeaders(Set<String> exposedHeaders);

	boolean getSupportsCredentials();
	void setSupportsCredentials(boolean supportsCredentials);

}
