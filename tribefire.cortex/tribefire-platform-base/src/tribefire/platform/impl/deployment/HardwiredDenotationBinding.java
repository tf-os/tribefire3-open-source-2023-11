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

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.braintribe.cfg.Required;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.HardwiredDeployable;
import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.model.processing.deployment.api.ConfigurableComponentInterfaceBindings;
import com.braintribe.model.processing.deployment.api.MutableDeployRegistry;

import tribefire.cortex.module.loading.api.HardwiredDenotationBinder;

public class HardwiredDenotationBinding implements HardwiredDenotationBinder {

	private final Map<HardwiredDeployable, Map<ComponentBinder<?, ?>, Supplier<?>>> bindings = new LinkedHashMap<>();

	private ConfigurableComponentInterfaceBindings interfaceBindings;
	private final Map<String, HardwiredDeployable> externalIdToDeployable = newMap();

	@Required
	public void setInterfaceBindings(ConfigurableComponentInterfaceBindings interfaceBindings) {
		this.interfaceBindings = interfaceBindings;
	}

	public Stream<HardwiredDeployable> deployableStream() {
		return bindings.keySet().stream() //
				.filter(this::isBoundByPlatform);
	}

	/** @return true if given {@link HardwiredDeployable} was bound by the platform and not one of the modules. */
	private boolean isBoundByPlatform(HardwiredDeployable hd) {
		return hd.getModule() == null;
	}

	@Override
	public <D extends HardwiredDeployable> HardwiredComponentBinding<D> bind(D deployable) {
		checkExternalIdUnique(deployable);

		Map<ComponentBinder<?, ?>, Supplier<?>> deployableBindings = bindings.computeIfAbsent(deployable, d -> new HashMap<>());

		return new HardwiredComponentBinding<D>() {
			@Override
			public <T> HardwiredComponentBinding<D> component(ComponentBinder<? super D, T> binder, Supplier<? extends T> expertSupplier) {
				deployableBindings.put(binder, expertSupplier);
				interfaceBindings.registerComponentInterfaces(binder);

				return this;
			}
		};
	}

	private void checkExternalIdUnique(HardwiredDeployable hd) {
		HardwiredDeployable other = externalIdToDeployable.put(hd.getExternalId(), hd);
		if (other != null)
			throw new IllegalStateException("Two different hardwired deployables register with the same externalId: '" + hd.getExternalId()
					+ "'. FIRST: " + deployableDescriptor(other) + ", SECOND: " + deployableDescriptor(hd));
	}

	private static String deployableDescriptor(HardwiredDeployable hd) {
		String origin = hd.getModule() == null ? "platform" : "module " + hd.getModule().getName();
		return hd.getName() + " (" + hd.entityType().getShortName() + " from " + origin + ")";
	}

	public void deploy(MutableDeployRegistry registry) {
		for (Map.Entry<HardwiredDeployable, Map<ComponentBinder<?, ?>, Supplier<?>>> entry : bindings.entrySet()) {
			Deployable deployable = entry.getKey();
			Map<ComponentBinder<?, ?>, Supplier<?>> deployableBindings = entry.getValue();

			ConfigurableDeployedUnit unit = new ConfigurableDeployedUnit();

			for (Map.Entry<ComponentBinder<?, ?>, Supplier<?>> componentEntry : deployableBindings.entrySet()) {
				Object suppliedImplementation = componentEntry.getValue().get();

				ComponentBinder<Deployable, Object> binder = (ComponentBinder<Deployable, Object>) componentEntry.getKey();

				HardwiredDeploymentContext<Deployable, Object> deploymentContext = new HardwiredDeploymentContext<>(suppliedImplementation);
				deploymentContext.setDeployable(deployable);

				Object exposedImplementation = binder.bind(deploymentContext);

				ConfigurableDeployedComponent component = new ConfigurableDeployedComponent(binder, exposedImplementation, suppliedImplementation);

				unit.putDeployedComponent(binder.componentType(), component);
			}

			registry.register(deployable, unit);
		}
	}

}
