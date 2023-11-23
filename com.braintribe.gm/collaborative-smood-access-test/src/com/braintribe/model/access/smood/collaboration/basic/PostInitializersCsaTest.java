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
package com.braintribe.model.access.smood.collaboration.basic;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.List;

import org.junit.Test;

import com.braintribe.model.access.smood.collaboration.manager.model.StagedEntity;
import com.braintribe.model.processing.session.api.collaboration.AbstractPersistenceInitializer;
import com.braintribe.model.processing.session.api.collaboration.CollaborativeAccess;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializer;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;
import com.braintribe.model.smoodstorage.stages.StaticStage;

/**
 * @see CollaborativeAccess
 * 
 * @author peter.gazdik
 */
public class PostInitializersCsaTest extends AbstractCollaborativePersistenceTest {

	private static final String SIMPLE_POST_STAGE_NAME = "simplePostStage";
	private static final String SIMPLE_POST_STAGED_ENTITY_ID = "post.staged";

	@Override
	protected List<PersistenceInitializer> postInitializers() {
		return asList(simpleInitializer());
	}

	private PersistenceInitializer simpleInitializer() {
		PersistenceStage postStage = StaticStage.T.create();
		postStage.setName(SIMPLE_POST_STAGE_NAME);

		return new AbstractPersistenceInitializer() {
			@Override
			public void initializeData(PersistenceInitializationContext context) throws ManipulationPersistenceException {
				context.getSession().create(StagedEntity.T, SIMPLE_POST_STAGED_ENTITY_ID);
			}

			@Override
			public PersistenceStage getPersistenceStage() {
				return postStage;
			}
		};
	}

	@Test
	public void entityFromPostInitializerHasCorrectStage() {
		StagedEntity entity = session.findEntityByGlobalId(SIMPLE_POST_STAGED_ENTITY_ID);
		assertEntityStage(entity, SIMPLE_POST_STAGE_NAME);
	}

	/** There was a bug where the new entity would have the stage of the last post initializer, rather than "trunk". */
	@Test
	public void newEntityHasCorrectStage() {
		StagedEntity entity = session.create(StagedEntity.T);
		session.commit();

		assertEntityStage(entity, "trunk");
	}

}
