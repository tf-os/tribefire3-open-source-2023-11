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

import java.util.Map;

public class DeferredValueMapPutter implements DeferredApplier {
	private Map<Object,Object> target;
	private Object key;
	private ProxyValue value;
	
	public DeferredValueMapPutter(Map<Object,Object> target, Object key, ProxyValue value) {
		this.target = target;
		this.key = key;
		this.value = value;
	}

	@Override
	public void apply() {
		target.put(key, value.actualValue());
	}
}
