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
package com.braintribe.wire.impl.scope;

import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.scope.InstanceHolder;
import com.braintribe.wire.api.scope.WireScope;
import com.braintribe.wire.api.space.WireSpace;

public abstract class AbstractInstanceHolder implements InstanceHolder {
	protected WireScope scope;
	private InstanceConfigurationImpl beanConfiguration;
	protected WireSpace space;
	protected String name;
	
	public AbstractInstanceHolder(WireSpace space, WireScope scope, String name) {
		this.space = space;
		this.scope = scope;
		this.name = name;
	}
	
	@Override
	public String name() {
		return name;
	}

	@Override
	public WireScope scope() {
		return scope;
	}
	
	@Override
	public WireSpace space() {
		return space;
	}
	
	@Override
	public void onPostConstruct(Object bean) {
		scope.getContext().onPostConstruct(this, bean);
	}
	
	@Override
	public void onDestroy() {
		if (beanConfiguration != null) {
			Runnable destroyCallback = beanConfiguration.destroyCallback();
			if (destroyCallback != null)
				destroyCallback.run();
		}
		scope.getContext().onPreDestroy(this, get());
	}
	
	@Override
	public InstanceConfiguration config() {
		return beanConfiguration != null? 
				beanConfiguration: 
				(beanConfiguration = new InstanceConfigurationImpl(this));
	}
}
