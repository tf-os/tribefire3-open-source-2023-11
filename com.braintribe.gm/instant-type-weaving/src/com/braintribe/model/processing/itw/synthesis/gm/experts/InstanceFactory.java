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
package com.braintribe.model.processing.itw.synthesis.gm.experts;

import java.util.function.Supplier;



public class InstanceFactory<T> implements Supplier<T> {
	private Class<? extends T> fromClass;

	public InstanceFactory(Class<? extends T> fromClass) {
		super();
		this.fromClass = fromClass;
	}

	@Override
	public T get() throws RuntimeException {
		try {
			return postProcess(fromClass.getDeclaredConstructor().newInstance());
		} catch (Exception e) {
			throw new RuntimeException("error while instantiating from class " + fromClass, e);
		}
	}

	protected T postProcess(T instance) throws Exception {
		return instance;
	}
}
