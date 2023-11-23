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
package com.braintribe.gwt.tribefirejs.client.remote;

import java.util.Set;

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.persistence.AccessDescriptor;

import jsinterop.annotations.JsType;

@JsType(namespace = GmCoreApiInteropNamespaces.model)
public interface ModelAccessoryBuilder {
	
	ModelAccessoryBuilder useCase(String useCase);

	ModelAccessoryBuilder useCases(Set<String> useCases);

	ModelAccessoryBuilder access(String accessId, String accessDenotationType);

	ModelAccessoryBuilder accessDescriptor(AccessDescriptor accessDescriptor);
	
	ModelAccessory build();
	
}
