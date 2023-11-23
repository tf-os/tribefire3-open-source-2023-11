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
package com.braintribe.model.generic.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.ScalarType;
import com.braintribe.processing.async.api.AsyncCallback;

public class ProxyContext {
	protected List<DeferredApplier> appliers = new ArrayList<>();
	//private Map<String, EntityType<?>> resolvedProxyTypes = new HashMap<>();
	protected Map<String, ProxyEntityType> proxyEntityTypes = new HashMap<>();
	protected Map<String, ProxyEnumType> proxyEnumTypes = new HashMap<>();

	public void defer(DeferredApplier applier) {
		appliers.add(applier);
	}
	
	public void deferPropertyAssignment(GenericEntity target, Property property, ProxyValue value) {
		if (property.getClass() == ProxyProperty.class) {
			/* casting the entity but do not register as registering is 
			 * only needed for application onto another target and in that
			 * case it would be registered in that very situation
			 */
			ProxyEntity proxyEntity = (ProxyEntity)target;
			
			
			ProxyProperty proxyProperty = (ProxyProperty)property;
			appliers.add(new DeferredProxyPropertyAssigner(proxyEntity, proxyProperty, value));
		}
		else {
			appliers.add(new DeferredPropertyAssigner(target, property, value));
		}

	}
	
	public void deferListInsert(java.util.List<Object> target, ProxyValue value, int position) {
		appliers.add(new DeferredListInserter(target, position, value));
	}

	public void deferCollectionAdd(java.util.Collection<Object> target, ProxyValue value) {
		appliers.add(new DeferredCollectionAdder(target, value));
	}

	public void deferMapPut(java.util.Map<Object,Object> target, ProxyValue key, Object value) {
		appliers.add(new DeferredKeyMapPutter(target, key, value));
	}

	
	public void deferMapPut(java.util.Map<Object,Object> target, Object key, ProxyValue value) {
		appliers.add(new DeferredValueMapPutter(target, key, value));
	}

	
	public void deferMapPut(java.util.Map<Object,Object> target, ProxyValue key, ProxyValue value) {
		appliers.add(new DeferredEntryMapPutter(target, key, value));
	}

	
	public void resolveProxiesAndApply(AsyncCallback<Void> callback) {
		try {
			resolveProxiesAndApply();
		} catch (Exception e) {
			callback.onFailure(e);
			return;
		}
		
		callback.onSuccess(null);
	}
	
	public void resolveProxiesAndApply() {
		for (DeferredApplier applier: appliers) {
			applier.apply();
		}
	}

	public ProxyEntityType getProxyEntityType(String typeSignature) {
		ProxyEntityType proxyEntityType = proxyEntityTypes.get(typeSignature);
		if (proxyEntityType == null) {
			proxyEntityType = new ProxyEntityType(this, typeSignature);
			proxyEntityTypes.put(typeSignature, proxyEntityType);
		}
		return proxyEntityType;
	}
	
	
	public ScalarType getProxyEnumType(String typeSignature) {
		ProxyEnumType proxyEnumType = proxyEnumTypes.get(typeSignature);
		if (proxyEnumType == null) {
			proxyEnumType = new ProxyEnumType(this, typeSignature);
			proxyEnumTypes.put(typeSignature, proxyEnumType);
		}
		return proxyEnumType;
	}
	
	public void onCreateEntity(ProxyEntity proxyEntity) {
		appliers.add(new DeferredEntityInstantiator(proxyEntity));
	}
	
	public void onCreateEnum(ProxyEnum proxyEnum) {
		appliers.add(new DeferredEnumInstantiator(proxyEnum));
	}
	
}
