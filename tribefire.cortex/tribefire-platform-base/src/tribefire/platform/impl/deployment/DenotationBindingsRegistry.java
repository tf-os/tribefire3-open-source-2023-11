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

import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newLinkedMap;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.model.processing.deployment.api.ComponentBinding;
import com.braintribe.model.processing.deployment.api.ConfigurableComponentInterfaceBindings;
import com.braintribe.model.processing.deployment.api.DenotationTypeBindings;
import com.braintribe.model.processing.deployment.api.DeployedUnit;
import com.braintribe.model.processing.deployment.api.DeploymentException;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.deployment.api.MutableDeploymentContext;
import com.braintribe.model.processing.deployment.api.binding.ComponentBindingBuilder;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.model.processing.deployment.api.binding.ExpertBindingBuilder;
import com.braintribe.model.processing.deployment.api.binding.ModuleBindingBuilder;
import com.braintribe.utils.lcd.StopWatch;

/**
 * {@link ConfigurableComponentInterfaceBindings} functionality was extracted to {@link ComponentInterfaceBindingsRegistry}.
 * 
 * @author dirk.scheffler
 */
public class DenotationBindingsRegistry implements DenotationBindingBuilder, ModuleBindingBuilder, DenotationTypeBindings {

	private static final Logger log = Logger.getLogger(DenotationBindingsRegistry.class);

	private final Map<EntityType<? extends Deployable>, ComponentBinding> proxyComponentBindings = new IdentityHashMap<>();
	private final Map<EntityType<? extends Deployable>, DeployableTypeBindings> denotationMappings = new IdentityHashMap<>();

	private ConfigurableComponentInterfaceBindings interfaceBindings;

	@Required
	public void setInterfaceBindings(ConfigurableComponentInterfaceBindings interfaceBindings) {
		this.interfaceBindings = interfaceBindings;
	}

	@Override
	public Class<?>[] findComponentInterfaces(EntityType<? extends Deployable> componentType) {
		return interfaceBindings.findComponentInterfaces(componentType);
	}

	@Override
	public Class<?>[] getComponentInterfaces(EntityType<? extends Deployable> componentType) throws DeploymentException {
		return interfaceBindings.getComponentInterfaces(componentType);
	}

	@Override
	public <D extends Deployable> ComponentBindingBuilder<D> bind(EntityType<D> deployableType) {
		return bind(deployableType, null);
	}

	@Override
	public <D extends Deployable> ComponentBindingBuilder<D> bind(EntityType<D> deployableType, String deployableExternalId) {
		return bind(deployableType, deployableExternalId, Module.PLATFORM_MODULE_GLOBAL_ID);
	}

	@Override
	public DenotationBindingBuilder bindForModule(String moduleGlobalId) {
		Objects.requireNonNull(moduleGlobalId, "moduleGlobalId cannot be null");

		return new DenotationBindingBuilder() {
			@Override
			public <D extends Deployable> ComponentBindingBuilder<D> bind(EntityType<D> deployableType, String deployableExternalId) {
				return DenotationBindingsRegistry.this.bind(deployableType, deployableExternalId, moduleGlobalId);
			}
			@Override
			public <D extends Deployable> ComponentBindingBuilder<D> bind(EntityType<D> deployableType) {
				return DenotationBindingsRegistry.this.bind(deployableType, null, moduleGlobalId);
			}
		};
	}

	private <D extends Deployable> ComponentBindingBuilder<D> bind(EntityType<D> deployableType, String deployableExternalId, String moduleGlobalId) {
		Objects.requireNonNull(deployableType, "deployableType must not be null");
		return new ConfigComponentBindingsBuilder<D>(deployableType, deployableExternalId, moduleGlobalId);
	}

	class ConfigComponentBindingsBuilder<D extends Deployable> implements ComponentBindingBuilder<D> {

		final EntityType<D> deployableType;
		final BindingEntry bindingEntry;

		public ConfigComponentBindingsBuilder(EntityType<D> deployableType, String deployableExternalId, String moduleGlobalId) {
			this.deployableType = deployableType;

			this.bindingEntry = acquire(deployableType, deployableExternalId, moduleGlobalId);
		}

		@Override
		public <T> ExpertBindingBuilder<D, T> component(Class<T> componentInterface, Class<?>... additionalComponentInterfaces) {
			return component(deployableType, componentInterface, additionalComponentInterfaces);
		}

		@Override
		public <T> ExpertBindingBuilder<D, T> component(EntityType<? super D> componentType, Class<T> componentInterface,
				Class<?>... additionalComponentInterfaces) {
			return component(ComponentBinder.plainBinder((EntityType<Deployable>) componentType, componentInterface, additionalComponentInterfaces));
		}

		@Override
		public <T> ExpertBindingBuilder<D, T> component(ComponentBinder<? super D, T> componentBinder) {
			requireNonNull(componentBinder, "componentBinder must not be null");

			return new ExpertBindingBuilder<D, T>() {
				@Override
				public ComponentBindingBuilder<D> expert(T expert) {
					requireNonNull(expert, "expert must not be null");
					return expertFactory(c -> expert);
				}

				@Override
				public ComponentBindingBuilder<D> expertSupplier(Supplier<? extends T> supplier) {
					requireNonNull(supplier, "supplier must not be null");
					return expertFactory(c -> supplier.get());
				}

				@Override
				public ComponentBindingBuilder<D> expertFactory(Function<ExpertContext<D>, ? extends T> factory) {
					requireNonNull(factory, "factory must not be null");

					interfaceBindings.registerComponentInterfaces(componentBinder);

					ComponentBinding binding = new ConfigurableComponentBinding<>(componentBinder, factory);
					bindingEntry.addComponentBinding(binding);

					log.debug(() -> "Bound factory to " + bindingEntry.identification + " using " + componentBinder.stringify() + ": " + factory);

					return ConfigComponentBindingsBuilder.this;
				}
			};
		}

	}

	@Override
	public Function<MutableDeploymentContext<?, ?>, DeployedUnit> resolveDeployedUnitSupplier(Deployable deployable) throws DeploymentException {
		Objects.requireNonNull(deployable, "deployable must not be null");

		BindingEntry denotationMapping = resolveMapping(deployable);

		return denotationMapping::deploy;
	}

	// Never returns null
	private BindingEntry resolveMapping(Deployable deployable) throws DeploymentException {
		return getBindingsForTypeOf(deployable) //
				.resolveBindingFor(deployable);
	}

	private DeployableTypeBindings getBindingsForTypeOf(Deployable deployable) {
		EntityType<Deployable> denotationType = deployable.entityType();

		return denotationMappings.computeIfAbsent(denotationType, t -> {
			throw new DeploymentException("The denotation type is not bound: " + denotationType.getTypeSignature());
		});
	}

	/** Returns an <B>unmodifiable set of globalIds</b> of all {@link Module}s which bind given {@link Deployable}. */
	public Set<String> resolveBindingModulesOf(Deployable deployable) {
		DeployableTypeBindings typeBindings = denotationMappings.get(deployable.entityType());
		if (typeBindings == null)
			return emptySet();

		BindingEntry deployableEntry = typeBindings.deployableBindings.get(deployable.getExternalId());
		if (deployableEntry != null)
			return singleton(deployableEntry.mGlobalId);

		return unmodifiableSet(typeBindings.moduleBindings.keySet());
	}

	@Override
	public ComponentBinding findComponentProxyBinding(EntityType<? extends Deployable> componentType) {
		Objects.requireNonNull(componentType, "componentType must not be null");

		return proxyComponentBindings.get(componentType);
	}

	@Override
	public Set<EntityType<? extends Deployable>> boundTypes() {
		return unmodifiableSet(denotationMappings.keySet());
	}

	// package just because of unit tests
	/* package */ BindingEntry acquire(EntityType<? extends Deployable> deployableType, String dExternalId, String mGlobalId) {
		DeployableTypeBindings ddm = denotationMappings.computeIfAbsent(deployableType, k -> new DeployableTypeBindings());

		if (dExternalId != null)
			// This code is reachable from multiple modules, and doesn't throw an exception!
			return ddm.deployableBindings.computeIfAbsent(dExternalId, k -> new BindingEntry(deployableType, dExternalId, mGlobalId));
		else
			return ddm.moduleBindings.computeIfAbsent(mGlobalId, k -> new BindingEntry(deployableType, null, mGlobalId));
	}

	private static class DeployableTypeBindings {
		private final Map<String, BindingEntry> deployableBindings = newMap();
		private final Map<String, BindingEntry> moduleBindings = newMap();

		public BindingEntry resolveBindingFor(Deployable deployable) {
			String deployableExternalId = deployable.getExternalId();
			BindingEntry result = deployableBindings.get(deployableExternalId);
			if (result != null)
				return result;

			Module module = deployable.getModule();
			String moduleGlobalId = module == null ? null : module.getGlobalId();
			result = moduleGlobalId == null ? null : moduleBindings.get(moduleGlobalId);

			if (result != null)
				return result;

			if (moduleGlobalId != null)
				throw new DeploymentException("Cannot deploy: " + deployable + " No binding for given deployable (" + deployableExternalId
						+ ") nor it's configured module: " + moduleGlobalId);

			if (module != null)
				throw new IllegalStateException("Cannot deploy: " + deployable + " The configured module has no globalId: " + module);

			if (moduleBindings.isEmpty())
				throw new DeploymentException(
						"Cannot deploy: " + deployable + " No binding found for deployable's externalId, and no module-specific binding exists.");

			if (moduleBindings.size() > 1)
				throw new DeploymentException("Cannot deploy: " + deployable
						+ " Deployable has no module assigned, and more than one module-specific bindings exist, so it is not possible infer the correct one. Relevant modules: "
						+ moduleBindings.keySet());

			return first(moduleBindings.values());
		}
	}

	// package just because of unit tests
	/* package */ static class BindingEntry {

		public final String mGlobalId;
		private final String identification;
		private final Map<EntityType<? extends Deployable>, ComponentBinding> componentBindings = newLinkedMap();

		public BindingEntry(EntityType<? extends Deployable> denotationType, String dExternalId, String mGlobalId) {
			this.mGlobalId = mGlobalId;
			identification = "deployable type: " + denotationType.getTypeSignature() + //
					(dExternalId == null ? "" : ", deployableExternalId: " + dExternalId) + //
					", moduleGlobalId: " + mGlobalId;
		}

		void addComponentBinding(ComponentBinding componentBinding) {
			EntityType<? extends Deployable> componentType = componentBinding.componentType();

			if (componentBindings.putIfAbsent(componentType, componentBinding) != null)
				throw new IllegalStateException("Duplicated mapping for " + identification + " and component type [" + componentType + "]");
		}

		public DeployedUnit deploy(MutableDeploymentContext<?, ?> context) {
			Objects.requireNonNull(context, "deployment context must not be null");

			ConfigurableDeployedUnit deployedUnit = new ConfigurableDeployedUnit();

			for (ComponentBinding binding : componentBindings.values())
				deploy(context, deployedUnit, binding);

			return deployedUnit;
		}

		private void deploy(MutableDeploymentContext<?, ?> c, ConfigurableDeployedUnit deployedUnit, ComponentBinding binding) {
			StopWatch stopWatch = new StopWatch();

			ComponentBinder<Deployable, Object> binder = (ComponentBinder<Deployable, Object>) binding.getComponentBinder();

			MutableDeploymentContext<Deployable, Object> context = (MutableDeploymentContext<Deployable, Object>) c;
			Function<ExpertContext<Deployable>, Object> cvs = (Function<ExpertContext<Deployable>, Object>) binding.componentValueSupplier();

			context.setInstanceToBeBoundSupplier(() -> cvs.apply(context));

			Object boundValue = binder.bind(context);

			deployedUnit.putDeployedComponent(binder.componentType(),
					new ConfigurableDeployedComponent(binder, boundValue, c.getInstanceToBoundIfSupplied()));

			log.debug(() -> {
				Deployable d = c.getDeployable();
				EntityType<?> ct = binder.componentType();
				return "Deployment of " + d.simpleIdentification() + " as " + ct.getShortName() + " took: " + stopWatch.getElapsedTimePretty()
						+ ". Binding: " + identification;
			});
		}
	}

}
