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
package com.braintribe.model.processing.ddra.endpoints.api.v1;

import java.util.Set;
import java.util.function.Consumer;

import com.braintribe.model.ddra.DdraMapping;
import com.braintribe.model.ddra.DdraUrlMethod;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.service.api.ServiceRequest;

public interface DdraMappingRegistry {

	DdraMapping create(String path, EntityType<? extends ServiceRequest> requestType);
	
	DdraMapping create(String path, EntityType<? extends ServiceRequest> requestType, Consumer<DdraMapping> configurer);
	
	DdraMapping create(String path, EntityType<? extends ServiceRequest> requestType, DdraUrlMethod method,
			String defaultProjection, String defaultMimeType, String defaultServiceDomain, Set<String> tags);

	DdraMapping create(String path, EntityType<? extends ServiceRequest> requestType,
			DdraUrlMethod method, String defaultProjection, String defaultMimeType, String defaultServiceDomain,
			Set<String> tags, Consumer<DdraMapping> configurer);

	DdraMapping create(String path, EntityType<? extends ServiceRequest> requestType, DdraUrlMethod method);
	
}
