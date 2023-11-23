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
package tribefire.platform.impl.deployment;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.deployment.api.DeployedComponent;
import com.braintribe.model.processing.deployment.api.DeployedUnit;
import com.braintribe.model.processing.deployment.api.DeploymentException;

public class ConfigurableDeployedUnit implements DeployedUnit {

	protected Map<EntityType<? extends Deployable>, DeployedComponent> bindingsMap = new IdentityHashMap<>();
	protected boolean proxyUnit = false;
	
	public void setProxyUnit(boolean proxyUnit) {
		this.proxyUnit = proxyUnit;
	}

	@Override
	public <C> C getComponent(EntityType<? extends Deployable> componentType) throws DeploymentException {
		Objects.requireNonNull(componentType, "componentType must not be null");
		
		C component = (C) getDeployedComponent(componentType).exposedImplementation();
		return component;
		
	}

	@Override
	public <C> C findComponent(EntityType<? extends Deployable> componentType) {
		Objects.requireNonNull(componentType, "componentType must not be null");
		
		DeployedComponent deployedComponent = findDeployedComponent(componentType);
		
		C component = (C) (deployedComponent != null? deployedComponent.exposedImplementation(): null);
		return component;
	}
	
	@Override
	public DeployedComponent getDeployedComponent(EntityType<? extends Deployable> componentType) throws DeploymentException {
		
		Objects.requireNonNull(componentType, "componentType must not be null");
		
		DeployedComponent component = findDeployedComponent(componentType);
		
		if (component == null) {
			throw new DeploymentException("Component not found for type "+componentType.getTypeSignature());
		}
		
		return component;
	}
	
	@Override
	public DeployedComponent findDeployedComponent(EntityType<? extends Deployable> componentType) {
		
		Objects.requireNonNull(componentType, "componentType must not be null");
		
		return bindingsMap.get(componentType);
	}
	

	@Override
	public Map<EntityType<? extends Deployable>, DeployedComponent> getComponents() {
		return bindingsMap;
	}

	public void putDeployedComponent(EntityType<? extends Deployable> componentType, DeployedComponent component) {
		bindingsMap.put(componentType, component);
	}
	
	public void put(EntityType<? extends Deployable> componentType, Object component) {
		bindingsMap.put(componentType, new ConfigurableDeployedComponent(null, component, component));
	}
	
}
