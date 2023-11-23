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
package com.braintribe.wire.impl.contract;

import java.util.function.Supplier;

import com.braintribe.wire.api.WireException;
import com.braintribe.wire.api.space.WireSpace;

public class ManagedSpaceClassSupplier implements Supplier<Class<? extends WireSpace>> {

	private String className;
	
	public ManagedSpaceClassSupplier(String className) {
		super();
		this.className = className;
	}

	@Override
	public Class<? extends WireSpace> get() {
		try {
			return Class.forName(className).asSubclass(WireSpace.class);
		} catch (ClassNotFoundException e) {
			throw new WireException("Error while loading BeanSpace class " + className, e);
		}
	}

}
