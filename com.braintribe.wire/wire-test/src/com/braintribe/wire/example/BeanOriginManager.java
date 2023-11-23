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
package com.braintribe.wire.example;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.wire.api.scope.InstanceHolder;
import com.braintribe.wire.api.scope.LifecycleListener;

public class BeanOriginManager implements LifecycleListener {
	private Map<Object, InstanceHolder> originMap = new ConcurrentHashMap<>();
	
	@Override
	public void onPostConstruct(InstanceHolder beanHolder, Object bean) {
		originMap.put(bean, beanHolder);
	}
	
	@Override
	public void onPreDestroy(InstanceHolder beanHolder, Object bean) {
		originMap.remove(bean);
	}
	
	public InstanceHolder resolveBeanHolder(Object instance) {
		return originMap.get(instance);
	}
}
