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

import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.collaboration.DataInitializer;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.query.SelectQuery;

import tribefire.cortex.module.loading.ModuleLoader.ModuleEntry;
import tribefire.descriptor.model.ModuleDescriptor;
import tribefire.platform.impl.deployment.DenotationBindingsRegistry;

/**
 * This class manages the automatic assignment of the Deployable's {@link Deployable#getModule() module} property.
 * <p>
 * Let's call a module which initializes cortex as "cortex-module" here.
 * <p>
 * For each cortex-module, we register a special {@link DataInitializer} that runs after the last data initializer bound from this module, which finds
 * all {@link Deployable}s created by this module, and where the deployable's 'module' is <tt>null</tt>, it tries to set it automatically.
 * <p>
 * This assignment is done by examining which modules are capable of deploying given deployable type, and from those, only our cortex-module and its
 * dependencies are considered, as those are the only modules we know fur sure the author of the module knows of, and thus possibly meant them. The
 * assignment is then done if an only if we find exactly one module that fits these constrains.
 * <p>
 * Note that this means we do not ever assign the platform, even if the platform is the only component where the deployable can be deployed. The
 * reason is we currently don't have the information whether this module knows of the platform.
 * 
 * @author peter.gazdik
 */
/* package */ class DeployableModuleAssigning {

	public DenotationBindingsRegistry bindingRegistry;

	private static final Logger log = Logger.getLogger(DeployableModuleAssigning.class);

	public DataInitializer newModuleAssingingInitializer(ModuleEntry deployableOriginModuleEntry) {
		return new ModuleAssingingInitializer(deployableOriginModuleEntry);
	}

	private static final SelectQuery UNASSIGNED_DEPLOYABLES_QUERY = new SelectQueryBuilder() //
			.from(Deployable.T, "d") //
			.where().property("d", Deployable.module).eq(null) //
			.done();

	/**
	 * {@link DataInitializer} specific for a module, which runs in the cortex after this module's cortex initializers,
	 */
	class ModuleAssingingInitializer implements DataInitializer {

		private final ModuleEntry deployableOriginModuleEntry;
		private final Set<String> thisModuleAndItsDepsGlobalIds = newSet();

		public ModuleAssingingInitializer(ModuleEntry deployableOriginModuleEntry) {
			this.deployableOriginModuleEntry = deployableOriginModuleEntry;
		}

		@Override
		public void initialize(PersistenceInitializationContext context) {
			ManagedGmSession session = context.getSession();

			List<Deployable> unassignedDeployables = session.query().select(UNASSIGNED_DEPLOYABLES_QUERY).list();

			unassignedDeployables.stream() //
					.filter(d -> isFromOurModule(context, d)) //
					.forEach(d -> assignModule(d, session));
		}

		/** @return true iff given Deployable was create by an initializer from our module (represented by {@link #deployableOriginModuleEntry}). */
		private boolean isFromOurModule(PersistenceInitializationContext context, Deployable d) {
			return context.getStage(d) == deployableOriginModuleEntry.initializersRegistry.stage;
		}

		private void assignModule(Deployable deployable, ManagedGmSession session) {
			Set<String> moduleGlobalIds = newSet(bindingRegistry.resolveBindingModulesOf(deployable));

			keepOnlyThisModuleAndItsDeps(moduleGlobalIds);

			if (moduleGlobalIds.size() != 1) {
				log.trace(() -> "Will not assign module to " + deployable + " as not exactly 1 module capable of deployment was found. Modules: "
						+ moduleGlobalIds);
				return;
			}

			String moduleGlobalId = first(moduleGlobalIds);
			Module module = session.getEntityByGlobalId(moduleGlobalId);

			log.trace(() -> "Assigning module " + moduleGlobalId + " to " + deployable);

			deployable.setModule(module);
		}

		private void keepOnlyThisModuleAndItsDeps(Set<String> moduleGlobalIds) {
			moduleGlobalIds.retainAll(thisModuleAndItsDeps());
		}

		private Collection<?> thisModuleAndItsDeps() {
			if (thisModuleAndItsDepsGlobalIds.isEmpty())
				addDeps(deployableOriginModuleEntry.descriptor);

			return thisModuleAndItsDepsGlobalIds;
		}

		private void addDeps(ModuleDescriptor md) {
			if (thisModuleAndItsDepsGlobalIds.add(md.getModuleGlobalId()))
				nullSafe(md.getDependedModules()).forEach(this::addDeps);
		}

	}

}
