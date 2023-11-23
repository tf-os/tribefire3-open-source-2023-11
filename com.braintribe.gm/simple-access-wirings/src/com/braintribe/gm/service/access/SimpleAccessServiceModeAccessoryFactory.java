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
package com.braintribe.gm.service.access;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.cfg.Required;
import com.braintribe.model.access.AccessService;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.impl.managed.StaticAccessModelAccessory;

public class SimpleAccessServiceModeAccessoryFactory implements ModelAccessoryFactory {
	private Map<String, ModelAccessory> factories = new ConcurrentHashMap<>();
	
	private AccessService accessService;
	
	@Required
	public void setAccessService(AccessService accessService) {
		this.accessService = accessService;
	}
	
	@Override
	public ModelAccessory getForAccess(String accessId) {
		return factories.computeIfAbsent(accessId, this::buildModelAccessory);
	}
	
	private ModelAccessory buildModelAccessory(String accessId) {
		GmMetaModel metaModel = accessService.getMetaModel(accessId);
		StaticAccessModelAccessory modelAccessory = new StaticAccessModelAccessory(metaModel, accessId);
		return modelAccessory;
	}
	
	
}
