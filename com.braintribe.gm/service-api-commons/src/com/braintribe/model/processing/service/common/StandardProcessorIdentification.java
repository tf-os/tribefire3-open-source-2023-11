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
package com.braintribe.model.processing.service.common;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.service.api.ProcessorIdentification;

public class StandardProcessorIdentification implements ProcessorIdentification {

	private final String serviceId;
	private final EntityType<?> componentType;
	private final String domainId;

	public StandardProcessorIdentification(String serviceId) {
		this(serviceId, null, null);
	}

	public StandardProcessorIdentification(String serviceId, EntityType<?> componentType) {
		this(serviceId, componentType, null);
	}
	
	public StandardProcessorIdentification(String serviceId, EntityType<?> componentType, String domainId) {
		super();
		this.serviceId = serviceId;
		this.componentType = componentType;
		this.domainId = domainId;
	}

	@Override
	public String serviceId() {
		return serviceId;
	}

	@Override
	public <T extends EntityType<?>> T componentType() {
		return (T) componentType;
	}

	@Override
	public String domainId() {
		return domainId;
	}
}
