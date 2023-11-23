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
package com.braintribe.model.processing.cortex.priming;

import static com.braintribe.utils.TimeTracker.startNew;
import static com.braintribe.utils.TimeTracker.stopAndPrint;

import java.io.File;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.access.collaboration.CollaborativeSmoodAccess;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.collaboration.CollaborativeAccess;
import com.braintribe.model.processing.session.api.collaboration.CollaborativeManipulationPersistence;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceAppender;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.EntityManager;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.managed.ManipulationMode;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * @see CortexModelsPersistenceInitializer
 * 
 * @author peter.gazdik
 */
public class CortexModelsPersistenceInitializerTest {

	private CollaborativeSmoodAccess access;

	@Before
	public void setup() {
		startNew("setup");

		startNew("model");
		Model model = GMF.getTypeReflection().getModel("tribefire.cortex:tribefire-sync-model");
		stopAndPrint("model");

		startNew("meta");
		GmMetaModel mm = model.getMetaModel();
		stopAndPrint("meta");

		startNew("smood");
		access = new CollaborativeSmoodAccess();
		access.setReadWriteLock(EmptyReadWriteLock.INSTANCE);
		access.setManipulationPersistence(new Test_NonAppendingMaipulationPersistence());
		access.setMetaModel(mm);
		access.postConstruct();
		stopAndPrint("smood");
		stopAndPrint("setup");
	}

	/** if this doesn't explode (well, throw an exception), it's good */
	@Test
	public void importSmokeTest() throws Exception {
		startNew("init");
		access.getMetaModel();
		stopAndPrint("init");
	}

	/** Here we actually query the data to see that it's there. */
	@Test
	public void importDataOk() throws Exception {
		// @formatter:off
		EntityQuery eq = EntityQueryBuilder
				.from(GmEntityType.T)
				.where()
					.property("typeSignature").eq(SelectQuery.T.getTypeSignature())
				.done();
		// @formatter:on

		EntityQueryResult result = access.queryEntities(eq);

		BtAssertions.assertThat(result.getEntities()).hasSize(1);
	}

	static class Test_NonAppendingMaipulationPersistence extends CortexModelsPersistenceInitializer implements CollaborativeManipulationPersistence {

		@Override
		public Stream<PersistenceStage> getPersistenceStages() {
			return Stream.of(this.getPersistenceStage());
		}

		@Override
		public void initializeModels(PersistenceInitializationContext context) throws ManipulationPersistenceException {
			context.setCurrentPersistenceStage(getPersistenceStage());
			super.initializeModels(context);
		}

		@Override
		public PersistenceAppender getPersistenceAppender() throws ManipulationPersistenceException {
			return new PersistenceAppender() {
				@Override
				public PersistenceStage getPersistenceStage() {
					return Test_NonAppendingMaipulationPersistence.this.getPersistenceStage();
				}
				
				@Override
				public AppendedSnippet[] append(Manipulation manipulation, ManipulationMode mode) {
					throw new RuntimeException("This method should not be invoked.");
				}
				
				@Override
				public void append(Resource[] gmmlResources, EntityManager entityManager) {
					throw new RuntimeException("This method should not be invoked.");
				}
			};
		}

		@Override
		public void onCollaborativeAccessInitialized(CollaborativeAccess csa, ManagedGmSession csaSession) {
			// noop
		}

		@Override
		public void setModelOracle(ModelOracle modelOracle) {
			// noop
		}

		@Override
		public File getStrogaBase() {
			throw new UnsupportedOperationException("Method 'Test_NonAppendingMaipulationPersistence.getStrogaBase' is not supported!");
		}

		@Override
		public PersistenceAppender newPersistenceAppender(String name) {
			throw new UnsupportedOperationException("Method 'Test_NonAppendingMaipulationPersistence.newPersistenceAppender' is not supported!");
		}

		@Override
		public void renamePersistenceStage(String oldName, String newName) {
			throw new UnsupportedOperationException("Method 'Test_NonAppendingMaipulationPersistence.renamePersistenceStage' is not supported!");
		}

		@Override
		public void mergeStage(String source, String target) {
			throw new UnsupportedOperationException("Method 'Test_NonAppendingMaipulationPersistence.mergeStage' is not supported!");
		}

		@Override
		public void reset() {
			throw new UnsupportedOperationException("Method 'Test_NonAppendingMaipulationPersistence.reset' is not supported!");
		}

		@Override
		public Stream<Resource> getResourcesForStage(String name) {
			throw new UnsupportedOperationException("Method 'Test_NonAppendingMaipulationPersistence.getStageResources' is not supported!");
		}

		@Override
		public Stream<Supplier<Set<GenericEntity>>> getModifiedEntitiesForStage(String name) {
			throw new UnsupportedOperationException("Method 'Test_NonAppendingMaipulationPersistence.getModifiedEntitiesForStage' is not supported!");
		}

	}
}
