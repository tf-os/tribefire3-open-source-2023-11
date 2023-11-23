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
package com.braintribe.gwt.platform.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class PlatformImpl {

	protected Map<Class<?>, Supplier<?>> factories = new HashMap<Class<?>, Supplier<?>>();

	public <T> T create(Class<T> clazz) {
		Supplier<?> provider = factories.get(clazz);

		try {
			T result = (T) provider.get();
			if (result == null) {
				throw new IllegalStateException("Missing factory for :" + clazz);
			}

			return result;

		} catch (RuntimeException e) {
			throw new IllegalStateException("Error while creating instance for :" + clazz, e);
		}
	}

}
