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
package com.braintribe.model.processing.deployment.api;

import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;

public class DelegatingDeployedUnit implements DeployedUnit {
	
	private DeployedUnit delegate;
	private final Supplier<DeployedUnit> delegateSupplier;
	
	public DelegatingDeployedUnit(Supplier<DeployedUnit> delegateSupplier) {
		this.delegateSupplier = delegateSupplier;
	}

	public DeployedUnit getDelegate() {
		if (delegate == null) {
			delegate = delegateSupplier.get();
		}
		return delegate;
	}
	
	@Override
	public <C> C getComponent(EntityType<? extends Deployable> componentType) throws DeploymentException {
		return getDelegate().getComponent(componentType);
	}

	@Override
	public <C> C findComponent(EntityType<? extends Deployable> componentType) {
		return getDelegate().findComponent(componentType);
	}
	
	@Override
	public DeployedComponent findDeployedComponent(EntityType<? extends Deployable> componentType) {
		return getDelegate().findDeployedComponent(componentType);
	}
	
	@Override
	public DeployedComponent getDeployedComponent(EntityType<? extends Deployable> componentType) {
		return getDelegate().getDeployedComponent(componentType);
	}

	@Override
	public Map<EntityType<? extends Deployable>, DeployedComponent> getComponents() {
		return getDelegate().getComponents();
	}

}
