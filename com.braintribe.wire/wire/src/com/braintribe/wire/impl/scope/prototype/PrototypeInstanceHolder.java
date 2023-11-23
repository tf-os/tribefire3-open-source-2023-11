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
package com.braintribe.wire.impl.scope.prototype;

import com.braintribe.wire.api.scope.InstanceHolder;
import com.braintribe.wire.api.scope.InstanceHolderSupplier;
import com.braintribe.wire.api.scope.WireScope;
import com.braintribe.wire.api.space.WireSpace;
import com.braintribe.wire.impl.scope.AbstractInstanceHolder;

public class PrototypeInstanceHolder extends AbstractInstanceHolder implements InstanceHolderSupplier {
	public PrototypeInstanceHolder(WireSpace space, WireScope scope, String name) {
		super(space, scope, name);
	}
	
	@Override
	public InstanceHolder getHolder(Object context) {
		return this;
	}
	
	@Override
	public Object get() {
		return null;
	}

	@Override
	public void publish(Object bean) {
		// nop
	}
	
	@Override
	public void onCreationFailure(Throwable t) {
		// nop
	}

	@Override
	public boolean lockCreation() {
		return true;
	}

	@Override
	public void unlockCreation() {
		// nop
	}
	
}
