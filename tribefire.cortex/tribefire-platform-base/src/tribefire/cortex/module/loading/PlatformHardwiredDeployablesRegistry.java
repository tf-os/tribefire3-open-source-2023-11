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
package tribefire.cortex.module.loading;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static java.util.Objects.requireNonNull;
import static tribefire.cortex.initializer.tools.ServiceDomainSupport.domainInitializer;

import java.util.List;
import java.util.function.Supplier;

import com.braintribe.cartridge.common.processing.accessrequest.InternalizingAccessRequestProcessor;
import com.braintribe.model.accessapi.AccessRequest;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.deployment.HardwiredDeployable;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.extensiondeployment.HardwiredServiceProcessor;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.session.api.collaboration.DataInitializer;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.service.api.PlatformRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.collection.api.MultiMap;
import com.braintribe.utils.collection.impl.HashMultiMap;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.initializer.tools.CommonModelsResolver;
import tribefire.cortex.initializer.tools.ServiceDomainSupport;
import tribefire.cortex.initializer.tools.ServiceDomainSupport.SdCtx;
import tribefire.cortex.initializer.tools.ServiceDomainSupport.SmCtx;
import tribefire.cortex.module.loading.api.HardwiredDenotationBinder;
import tribefire.descriptor.model.ModuleDescriptor;
import tribefire.module.api.HardwiredConfigurationModelBinder;
import tribefire.module.api.HardwiredServiceDomainBinder;
import tribefire.module.wire.contract.HardwiredDeployablesContract;

/**
 * Base implementation for a {@link HardwiredDeployablesContract}.
 * <p>
 * NOTE REGARDING THE NAME it's a contract (extends {@link WireSpace}) so it can be imported in modules' spaces, but functionally it's a registry
 * (offers methods to register stuff), not a space (doesn't offer any beans).
 * 
 * @author peter.gazdik
 */
public abstract class PlatformHardwiredDeployablesRegistry implements HardwiredDeployablesContract {

	// Set by ModuleLoader
	public ModulesCortexInitializer cortexInitializer;

	// Set by creator

	public HardwiredDenotationBinder actualBinder;

	public PersistenceGmSessionFactory requestSessionFactory;
	public PersistenceGmSessionFactory systemSessionFactory;

	private ModuleDescriptor currentModule;
	private Module hardwiredModule;

	private final MultiMap<ModuleDescriptor, HardwiredDeployable> moduleDeployables = new HashMultiMap<>();

	public void setCurrentModule(ModuleDescriptor currentModule) {
		this.currentModule = currentModule;
		this.hardwiredModule = ModuleLoaderHelper.moduleDenotation(Module.T.create(), currentModule);
	}

	protected String moduleName() {
		return currentModule.name();
	}

	protected void bindCortex(DataInitializer dataInitializer) {
		cortexInitializer.bind(dataInitializer);
	}

	// ###########################################################
	// ## . . . . . . . . . Hardwired binding . . . . . . . . . ##
	// ###########################################################

	@Override
	public final <HD extends HardwiredDeployable> tribefire.module.api.HardwiredComponentBinding<HD> bind(HD deployable) {
		requireNonNull(deployable, "Cannot bind 'null'");

		if (deployable.getModule() != null)
			throw new IllegalArgumentException("Cannot bind hardwired deployable: " + deployable + ", as it's 'module' is already assigned."
					+ " For consistency this is not allowed and the module is assigned automatically inside this method. Please do not set the property.");

		deployable.setModule(hardwiredModule);

		return bindInternal(deployable);
	}

	@Override
	public final <T extends PlatformRequest> HardwiredServiceProcessor bindPlatformServiceProcessor(String externalId, String name,
			EntityType<T> requestType, Supplier<ServiceProcessor<? super T, ?>> serviceProcessorSupplier) {

		return spForKnownDomain(externalId, name, "platform", Model.modelGlobalId("tribefire.cortex:configured-tribefire-platform-service-model"),
				requestType, serviceProcessorSupplier);
	}

	@Override
	public final HardwiredServiceDomainBinder bindOnNewServiceDomain(String domainExternalId, String domainName) {
		return new ServiceDomainBinder(domainExternalId, domainName, false);
	}

	@Override
	public final HardwiredServiceDomainBinder bindOnServiceDomain(String domainExternalId, String domainName) {
		return new ServiceDomainBinder(domainExternalId, domainName, true);
	}

	@Override
	public final HardwiredServiceDomainBinder bindOnExistingServiceDomain(String domainExternalId) {
		return new ServiceDomainBinder(domainExternalId);
	}

	@Override
	public HardwiredConfigurationModelBinder bindOnConfigurationModel(String modelName) {
		return new ConfigurationModelBinder(modelName, null);
	}

	@Override
	public HardwiredConfigurationModelBinder bindOnExistingConfigurationModel(String modelName) {
		return new ConfigurationModelBinder(modelName, true);
	}

	@Override
	public HardwiredConfigurationModelBinder bindOnNewConfigurationModel(String modelName) {
		return new ConfigurationModelBinder(modelName, false);
	}

	protected final <T extends ServiceRequest> HardwiredServiceProcessor spForKnownDomain(String externalId, String name, String domainId,
			String domainModelGlobalId, EntityType<T> requestType, Supplier<ServiceProcessor<? super T, ?>> serviceProcessorSupplier) {

		HardwiredServiceProcessor hardwiredSp = serviceProcessor(externalId, name, serviceProcessorSupplier);

		bindCortex( //
				domainInitializer("module:" + moduleName(), domainId) //
						.withServiceProcessor(requestType, hardwiredSp.getGlobalId()) //
						.forExistingDomain(domainModelGlobalId) //
		);

		return hardwiredSp;
	}

	protected final <T extends ServiceRequest> HardwiredServiceProcessor serviceProcessor(String externalId, String name,
			Supplier<ServiceProcessor<? super T, ?>> spSupplier) {

		HardwiredServiceProcessor deployable = newHd(HardwiredServiceProcessor.T, externalId, name, "service");
		return bindHd(deployable, serviceProcessorBidner(), spSupplier);
	}

	protected final <HD extends HardwiredDeployable> HD newHd(EntityType<HD> deployableType, String externalId, String name, String type) {
		HD deployable = deployableType.create("hardwired:" + type + "/" + externalId);
		deployable.setExternalId(externalId);
		deployable.setName(name);
		deployable.setAutoDeploy(true);
		deployable.setDeploymentStatus(DeploymentStatus.deployed);
		deployable.setModule(hardwiredModule);

		return deployable;
	}

	protected abstract ComponentBinder<com.braintribe.model.extensiondeployment.ServiceProcessor, ServiceProcessor<?, ?>> serviceProcessorBidner();

	protected final <T, HD extends HardwiredDeployable> HD bindHd(HD deployable, ComponentBinder<? super HD, T> binder,
			Supplier<? extends T> xpertSupplier) {
		bindInternal(deployable).component(binder, xpertSupplier);
		return deployable;
	}

	/**
	 * This method should only be used internally from the general {@link #bind(HardwiredDeployable)} method and also from all the expressive binding
	 * methods such as {@link #bindPlatformServiceProcessor(String, String, EntityType, ServiceProcessor)} (not directly, but later down the road).
	 * The reason is that there is no validation being done here and the deployable's module is expected to be set correctly to
	 * {@link #hardwiredModule} by the callers.
	 */
	protected final <HD extends HardwiredDeployable> tribefire.module.api.HardwiredComponentBinding<HD> bindInternal(HD deployable) {
		cortexInitializer.onBindHardwiredDeployables(currentModule);

		moduleDeployables.put(currentModule, deployable);

		return actualBinder.bind(deployable);
	}

	// ###########################################################
	// ## . . . . . . . ServiceProcessors binding . . . . . . . ##
	// ###########################################################

	private class ServiceDomainBinder implements HardwiredServiceDomainBinder {

		private final List<HardwiredServiceProcessor> hardwiredSps = newList();
		private final SdCtx initializerContext;

		// When extending existing domain
		public ServiceDomainBinder(String domainId) {
			this.initializerContext = domainInitializer(moduleName(), domainId);

			bindCortex(initializerContext.forExistingDomain());
		}

		// When creating new domain
		public ServiceDomainBinder(String domainId, String domainName, boolean canExist) {
			this.initializerContext = domainInitializer(moduleName(), domainId);

			bindCortex(initializerContext.forMaybeNewDomain(domainName, canExist));
		}

		@Override
		public <T extends AccessRequest> HardwiredServiceDomainBinder accessRequestProcessor(String externalId, String name,
				EntityType<T> requestType, Supplier<AccessRequestProcessor<? super T, ?>> processorSupplier) {

			return serviceProcessor(externalId, name, requestType, () -> newServiceProcessor(processorSupplier));
		}

		private <T extends AccessRequest> InternalizingAccessRequestProcessor<T, ?> newServiceProcessor(
				Supplier<AccessRequestProcessor<? super T, ?>> processorSupplier) {

			return new InternalizingAccessRequestProcessor<>(processorSupplier.get(), requestSessionFactory, systemSessionFactory);
		}

		@Override
		public <T extends ServiceRequest> HardwiredServiceDomainBinder serviceProcessor(String externalId, String name, EntityType<T> requestType,
				Supplier<ServiceProcessor<? super T, ?>> serviceProcessorSupplier) {

			HardwiredServiceProcessor hardwiredSp = PlatformHardwiredDeployablesRegistry.this.serviceProcessor(externalId, name,
					serviceProcessorSupplier);
			hardwiredSps.add(hardwiredSp);
			initializerContext.withServiceProcessor(requestType, hardwiredSp.getGlobalId());

			return modelOf(requestType);
		}

		@Override
		public HardwiredServiceDomainBinder model(String modelName) {
			initializerContext.withModelByName(modelName);
			return this;
		}

		@Override
		public HardwiredServiceDomainBinder modelOf(EntityType<?> entityType) {
			return model(CommonModelsResolver.getModelOf(entityType));
		}

		@Override
		public HardwiredServiceDomainBinder model(Model model) {
			initializerContext.withModels(model);
			return this;
		}

		@Override
		public List<HardwiredServiceProcessor> please() {
			return hardwiredSps;
		}

	}

	// ###########################################################
	// ## . . . . . . . ConfigurationModel binding . . . . . . .##
	// ###########################################################

	private class ConfigurationModelBinder implements HardwiredConfigurationModelBinder {

		private final List<HardwiredServiceProcessor> hardwiredSps = newList();
		private final SmCtx initializerContext;

		public ConfigurationModelBinder(String modelName, Boolean exists) {
			this.initializerContext = ServiceDomainSupport.serviceModelInitializer(moduleName(), modelName);

			bindCortex(initializerContext.forModel(exists));
		}

		@Override
		public <T extends ServiceRequest> ConfigurationModelBinder serviceProcessor(String externalId, String name, EntityType<T> requestType,
				Supplier<ServiceProcessor<? super T, ?>> serviceProcessorSupplier) {

			HardwiredServiceProcessor hardwiredSp = PlatformHardwiredDeployablesRegistry.this.serviceProcessor(externalId, name,
					serviceProcessorSupplier);
			hardwiredSps.add(hardwiredSp);
			initializerContext.withServiceProcessor(requestType, hardwiredSp.getGlobalId());

			return modelOf(requestType);
		}

		private ConfigurationModelBinder modelOf(EntityType<?> entityType) {
			return dependency(CommonModelsResolver.getModelOf(entityType));
		}

		@Override
		public ConfigurationModelBinder dependency(Model model) {
			initializerContext.withModels(model);
			return this;
		}

	}

	// ###########################################################
	// ## . . . . . . . . Cortex initialization . . . . . . . . ##
	// ###########################################################

	/**
	 * This code is expected to be invoked as part of the {@link ModulesCortexInitializer}, and it is called once per module. This imports all the
	 * hardwired deployables into cortex, i.e. it takes the {@link HardwiredDeployable} instances bound with the #bin
	 */
	public final void putHarwiredDeployablesIntoCortex(PersistenceInitializationContext ctx, ModuleDescriptor moduleDescriptor, Module module) {
		for (HardwiredDeployable hd : moduleDeployables.getAll(moduleDescriptor))
			importHd(ctx, module, hd);
	}

	private void importHd(PersistenceInitializationContext ctx, Module module, HardwiredDeployable hd) {
		HardwiredDeployable cortexHd = ctx.getSession().create(hd.entityType());
		cortexHd.setModule(module);

		for (Property p : hd.entityType().getProperties()) {
			Object value = p.getDirect(hd);

			if (p.getType().isEmpty(value))
				continue;

			if (Deployable.module.equals(p.getName()))
				continue;

			if (p.getType().areEntitiesReachable())
				throw new IllegalStateException("Property of hardwired deployable '" + hd.entityType().getShortName() + "#" + p.getName()
						+ "' is not empty. This is not expected, as this property references other entities. Hardwired deployable: " + hd
						+ ", property value: " + value);

			p.set(cortexHd, value);
		}
	}

}
