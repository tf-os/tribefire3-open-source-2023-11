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

import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.model.processing.deployment.api.ComponentBinding;
import com.braintribe.model.processing.deployment.api.ExpertContext;

public class ConfigurableComponentBinding<D extends Deployable, T> implements ComponentBinding {

	private final ComponentBinder<D, T> componentBinder;
	private final Function<? extends ExpertContext<?>, ?> valueSupplier;

	public ConfigurableComponentBinding(ComponentBinder<D, T> componentBinder, Supplier<? extends T> valueSupplier) {
		this.componentBinder = componentBinder;
		this.valueSupplier = c -> valueSupplier.get();
	}
	
	public ConfigurableComponentBinding(ComponentBinder<D, T> componentBinder, Function<? extends ExpertContext<?>, ?> factory) {
		this.componentBinder = componentBinder;
		this.valueSupplier = factory;
	}

	@Override
	public EntityType<? extends Deployable> componentType() {
		return componentBinder.componentType();
	}
	
	@Override
	public ComponentBinder<D, T> getComponentBinder() {
		return componentBinder;
	}

	@Override
	public Function<? extends ExpertContext<?>, ?> componentValueSupplier() {
		return valueSupplier;
	}

}
