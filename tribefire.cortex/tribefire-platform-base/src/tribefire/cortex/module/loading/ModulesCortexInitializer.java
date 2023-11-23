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

import static com.braintribe.utils.lcd.CollectionTools2.acquireList;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static tribefire.cortex.module.loading.ModuleLoaderHelper.moduleDenotation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.HardwiredDeployable;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.collaboration.AbstractPersistenceInitializer;
import com.braintribe.model.processing.session.api.collaboration.DataInitializer;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializer;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;
import com.braintribe.model.smoodstorage.stages.StaticStage;

import tribefire.descriptor.model.ComponentDescriptor;
import tribefire.descriptor.model.ModuleDescriptor;

/**
 * {@link PersistenceInitializer} for lots of implicit module-related initialization.
 * <p>
 * This initializer consists of individual {@link DataInitializer}, which are registered in different phases of module loading. It includes
 * <ul>
 * <li>Creation of platform module and assigning it as module of all the platform's {@link HardwiredDeployable}s.</li>
 * <li>Creation of denotation types for regular modules.</li>
 * <li>Extension of cortex model to ensure bound deployable types are available.</li>
 * <li>Creation of {@link HardwiredDeployable} denotation types for all such deployables, and maybe also doing custom initialization for a specific
 * deployable (e.g. creating a HealthCheck for a CheckProcessor).</li>
 * </ul>
 * 
 * This initializer does not set the {@link Deployable#getModule() deployable.module} property, that is done by a custom initializer - see
 * {@link DeployableModuleAssigning}
 * 
 * @author peter.gazdik
 */
/* package */ class ModulesCortexInitializer extends AbstractPersistenceInitializer {

	private final PersistenceStage stage = StaticStage.forName("ModulesCortexInitializer");

	private final List<DataInitializer> dataInitializers = newList();

	private final Map<ComponentDescriptor, CortexComponentInfo> componentInfos = newMap();

	private CortexModelExtendingInitializer cortexExtender;

	private ModuleDescriptor currentModule;

	public void setCurrentModule(ModuleDescriptor currentModule) {
		this.currentModule = currentModule;
	}

	@Override
	public PersistenceStage getPersistenceStage() {
		return stage;
	}

	public void bind(DataInitializer di) {
		dataInitializers.add(di);
	}

	/** Makes sure given type is part of the cortex model, i.e. instances of given type can be stored in cortex. */
	public void ensureInCortex(EntityType<?> type) {
		if (cortexExtender == null)
			bind(cortexExtender = new CortexModelExtendingInitializer());

		cortexExtender.coverType(type, currentModule);
	}

	@Override
	public void initializeData(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		for (DataInitializer di : dataInitializers)
			di.initialize(context);
	}

	public void onBindWireContract(ComponentDescriptor component) {
		acquireComponentInfo(component).bindsWireContracts = true;
	}

	public void onBindHardwiredDeployables(ComponentDescriptor component) {
		acquireComponentInfo(component).bindsHardwiredDeployables = true;
	}

	public void onBindInitializer(ComponentDescriptor component) {
		acquireComponentInfo(component).bindsInitializers = true;
	}

	public void onBindDeployable(ComponentDescriptor component) {
		acquireComponentInfo(component).bindsDeployables = true;
	}

	public Module createModuleDenotation(ManagedGmSession session, ModuleDescriptor moduleDescriptor) {
		Module result = moduleDenotation(session.create(Module.T), moduleDescriptor);

		CortexComponentInfo cci = componentInfos.get(moduleDescriptor);
		if (cci != null) {
			result.setBindsWireContracts(cci.bindsWireContracts);
			result.setBindsHardwired(cci.bindsHardwiredDeployables);
			result.setBindsInitializers(cci.bindsInitializers);
			result.setBindsDeployables(cci.bindsDeployables);
		}

		return result;
	}

	private CortexComponentInfo acquireComponentInfo(ComponentDescriptor component) {
		return componentInfos.computeIfAbsent(component, c -> new CortexComponentInfo());
	}

	static class CortexComponentInfo {
		boolean bindsWireContracts;
		boolean bindsHardwiredDeployables;
		boolean bindsInitializers;
		boolean bindsDeployables;
	}

	static class CortexModelExtendingInitializer implements DataInitializer {

		private final Map<Model, List<EntityType<?>>> coveringModels = newMap();
		private final Map<EntityType<?>, List<ModuleDescriptor>> bindingModules = newMap();

		/* package */ void coverType(EntityType<?> entityType, ModuleDescriptor currentModule) {
			acquireList(coveringModels, entityType.getModel()).add(entityType);
			acquireList(bindingModules, entityType).add(currentModule);
		}

		@Override
		public void initialize(PersistenceInitializationContext ctx) {
			ManagedGmSession session = ctx.getSession();

			GmMetaModel cortexModel = session.findEntityByGlobalId(Model.modelGlobalId("tribefire.cortex:tribefire-cortex-model"));
			List<GmMetaModel> cortexDeps = cortexModel.getDependencies();

			for (Model coveringModel : coveringModels.keySet())
				coverModel(session, cortexDeps, coveringModel);

			// reducing memory footprint
			coveringModels.clear();
			bindingModules.clear();
		}

		private void coverModel(ManagedGmSession session, List<GmMetaModel> cortexDeps, Model model) {
			GmMetaModel gmModel = session.findEntityByGlobalId(model.globalId());
			if (gmModel == null)
				throw new IllegalStateException("GmModel not found in cortex: '" + model.name()
						+ "'. This model thus cannot be added to cortex automatically, but it should, as the context implies these types might occur in cortex: "
						+ typesCoveredBy(model));

			if (!cortexDeps.contains(gmModel))
				cortexDeps.add(gmModel);
		}

		private String typesCoveredBy(Model model) {
			return coveringModels.get(model).stream() //
					.map(this::entitySignatureWithBindingModule) //
					.collect(Collectors.joining(", "));
		}

		private String entitySignatureWithBindingModule(EntityType<?> et) {
			return et.getTypeSignature() + bindingModules(et);  
		}

		private String bindingModules(EntityType<?> et) {
			List<ModuleDescriptor> modules = bindingModules.get(et);
			if (modules == null)
				return "";
			
			return modules.stream() //
					.map(ModuleDescriptor::name) //
					.collect(Collectors.joining(", ", "(from: ", ")"));
					
		}
	}

}
