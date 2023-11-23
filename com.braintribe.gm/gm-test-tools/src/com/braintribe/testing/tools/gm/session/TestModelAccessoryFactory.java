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
package com.braintribe.testing.tools.gm.session;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;

public class TestModelAccessoryFactory implements ModelAccessoryFactory {

	Map<String, ModelAccessory> accessModelAccessories = new HashMap<>();
	Map<String, ModelAccessory> serviceModelAccessories = new HashMap<>();

	@Override
	public ModelAccessory getForAccess(String accessId) {
		return accessModelAccessories.computeIfAbsent(accessId, k -> {
			throw new IllegalArgumentException("Unknown access id: " + accessId);
		});
	}

	@Override
	public ModelAccessory getForServiceDomain(String serviceDomainId) {
		return serviceModelAccessories.computeIfAbsent(serviceDomainId, k -> {
			throw new IllegalArgumentException("Unknown domain id: " + serviceDomainId);
		});
	}

	public void registerAccessModelAccessory(String domainId, GmMetaModel model) {
		registerModelAccessory(domainId, model, accessModelAccessories);
	}

	public void registerServiceModelAccessory(String domainId, GmMetaModel model) {
		registerModelAccessory(domainId, model, serviceModelAccessories);
	}

	private void registerModelAccessory(String domainId, GmMetaModel model, Map<String, ModelAccessory> map) {
		TestModelAccessory modelAccessory = TestModelAccessory.newModelAccessory(model);
		map.put(domainId, modelAccessory);
	}

}
