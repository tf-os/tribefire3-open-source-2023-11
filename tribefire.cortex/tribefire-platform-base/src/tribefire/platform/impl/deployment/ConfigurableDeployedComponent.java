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

import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.model.processing.deployment.api.DeployedComponent;

public class ConfigurableDeployedComponent implements DeployedComponent {
	private Object exposedImplementation;
	private Object suppliedImplementation;
	private ComponentBinder<?, ?> binder;

	public ConfigurableDeployedComponent(ComponentBinder<?, ?> binder, Object exposedImplementation, Object suppliedImplementation) {
		super();
		this.binder = binder;
		this.exposedImplementation = exposedImplementation;
		this.suppliedImplementation = suppliedImplementation;
	}

	@Override
	public Object exposedImplementation() {
		return exposedImplementation;
	}

	@Override
	public Object suppliedImplementation() {
		return suppliedImplementation;
	}
	
	@Override
	public ComponentBinder<?, ?> binder() {
		return binder;
	}
	
}
