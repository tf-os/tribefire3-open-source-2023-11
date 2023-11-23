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
package com.braintribe.wire.impl.scope.referee;

import com.braintribe.wire.api.scope.InstanceHolder;
import com.braintribe.wire.api.scope.InstanceHolderSupplier;
import com.braintribe.wire.api.space.WireSpace;

public class RefereeInstanceHolderSupplier implements InstanceHolderSupplier {
	
	private AggregateScope scope;
	private String name;
	private WireSpace space;
	
	public RefereeInstanceHolderSupplier(WireSpace space, AggregateScope scope, String name) {
		this.space = space;
		this.scope = scope;
		this.name = name;
	}
	
	@Override
	public InstanceHolder getHolder(Object context) {
		return scope.acquireScope().acquire(this, context);
	}
	
	public String getName() {
		return name;
	}
	
	public WireSpace getSpace() {
		return space;
	}
}
