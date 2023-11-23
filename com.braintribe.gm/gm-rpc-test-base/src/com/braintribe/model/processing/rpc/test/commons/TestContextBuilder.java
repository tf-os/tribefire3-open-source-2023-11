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
package com.braintribe.model.processing.rpc.test.commons;

import java.util.HashMap;
import java.util.Map;

public interface TestContextBuilder extends TestContext {
	<V, A extends TestAttribute<? super V>> TestContextBuilder set(Class<A> attribute, V value);
	
	static TestContextBuilder create() {
		return new TestContextBuilder() {
			private Map<Class<? extends TestAttribute<?>>, Object> attributes = new HashMap<>();
			
			@Override
			public <V, A extends TestAttribute<? super V>> TestContextBuilder set(Class<A> attribute, V value) {
				attributes.put(attribute, value);
				return this;
			}
			
			@Override
			public <V, A extends TestAttribute<? super V>> V get(Class<A> attribute) {
				return (V)attributes.get(attribute);
			}
		};
	}
}
