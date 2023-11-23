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
package com.braintribe.model.access.collaboration.persistence;

import static com.braintribe.utils.lcd.CollectionTools2.newLinkedMap;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.collaboration.SimplePersistenceInitializer;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

/**
 * @author peter.gazdik
 */
public abstract class ModelsPersistenceInitializer extends SimplePersistenceInitializer {

	private static final Logger log = Logger.getLogger(ModelsPersistenceInitializer.class);

	private boolean checkIfModelsAreAlreadyThere;
	private long nanoStart;

	/**
	 * In case your initializer adds models to those which are already there, use this flag, so that existing entities will be looked up. This makes
	 * the initializer slower, so only use when needed.
	 */
	public void setCheckIfModelsAreAlreadyThere(boolean checkIfModelsAreAlreadyThere) {
		this.checkIfModelsAreAlreadyThere = checkIfModelsAreAlreadyThere;
	}

	@Override
	public void initializeModels(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		nanoStart = System.nanoTime();

		Collection<GmMetaModel> models = getModels();

		CoreModelsSyncingCloningContext cloningContext = new CoreModelsSyncingCloningContext(context.getSession());

		cloneModel(models, cloningContext);

		processSyncedModels(cloningContext, context);
	}

	protected abstract Collection<GmMetaModel> getModels() throws ManipulationPersistenceException;

	private void cloneModel(Collection<GmMetaModel> models, CloningContext cloningContext) throws ManipulationPersistenceException {
		try {
			BaseType.INSTANCE.clone(cloningContext, models, null);

		} catch (RuntimeException e) {
			throw new ManipulationPersistenceException("Unexpected error while cloning core models into persistence.", e);
		}
	}

	private void processSyncedModels(CoreModelsSyncingCloningContext cloningContext, PersistenceInitializationContext context) {
		processSyncedModels(cloningContext.syncedModelsByName, context);
		logSyncedModels(cloningContext);
	}

	@SuppressWarnings("unused")
	protected void processSyncedModels(Map<String, GmMetaModel> syncedModelsByName, PersistenceInitializationContext context) {
		// overridden in cortex
	}

	private void logSyncedModels(CoreModelsSyncingCloningContext cloningContext) {
		long ms = (System.nanoTime() - nanoStart) / 1_000_000;

		long typeCount = cloningContext.syncedModelsByName.values().stream() //
				.flatMap(m -> m.getTypes().stream()) //
				.count();

		List<String> sortedNames = newList(cloningContext.syncedModelsByName.keySet());
		sortedNames.sort(null);

		StringBuilder sb = new StringBuilder();
		sb.append("Following [" + sortedNames.size() + "] core models with [" + typeCount + "] types were cloned into CSA in " + ms + " ms: ");
		for (String name : sortedNames) {
			sb.append("\n");
			sb.append("    ");
			sb.append(name);
		}

		log.info(sb.toString());
	}

	// #############################################
	// ## . . . . . . Cloning Context . . . . . . ##
	// #############################################

	class CoreModelsSyncingCloningContext extends StandardCloningContext {

		protected final Map<String, GmMetaModel> syncedModelsByName = newLinkedMap();
		private final ManagedGmSession session;

		public CoreModelsSyncingCloningContext(ManagedGmSession session) {
			this.session = session;
		}

		@Override
		public <T> T getAssociated(GenericEntity entity) {
			T result = super.getAssociated(entity);

			if (result == null && checkIfModelsAreAlreadyThere)
				result = session.findEntityByGlobalId(entity.getGlobalId());

			return result;
		}

		@Override
		public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
			GenericEntity result = session.create(entityType);

			if (entityType == GmMetaModel.T) {
				GmMetaModel originalModel = (GmMetaModel) instanceToBeCloned;
				GmMetaModel clonedModel = (GmMetaModel) result;
				syncedModelsByName.put(originalModel.getName(), clonedModel);
			}

			return result;
		}

	}

	// #############################################
	// ## . . . . Helper for sub-classes . . . . .##
	// #############################################

	protected static List<GmMetaModel> toGmModels(Stream<String> modelNames) throws ManipulationPersistenceException {
		GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

		// To be sure we also close the stream -CortexModelsPersistenceInitializer.readCortexModelNamesFromStorage()
		try (Stream<String> modelNames1 = modelNames) {
			return modelNames1 //
					.map(typeReflection::getModel) //
					.map(Model::getMetaModel) //
					.map(GmMetaModel.class::cast) //
					.collect(Collectors.toList());
		}
	}
}
