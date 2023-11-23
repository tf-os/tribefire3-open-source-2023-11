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
package com.braintribe.model.access.impls;

import static com.braintribe.model.processing.query.tools.PreparedTcs.everythingTc;

import com.braintribe.model.access.BasicAccessAdapter;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.testing.tools.gm.GmTestTools;

/**
 * Simple {@link SmoodAccess} based implementation of {@link BasicAccessAdapter}.
 * 
 * For queries, it is capable of providing a population based on a typeSignater - see {@link #queryPopulation(String)}.
 * 
 * For manipulations, it clones the edited entities into the underlying smoodAccess, and deletes deleted entities from
 * there.
 * 
 * @author peter.gazdik
 */
public class SimpleSmoodifiedBaaAccess extends BasicAccessAdapter {

	private final GmMetaModel model;
	private final SmoodAccess cache;

	public SimpleSmoodifiedBaaAccess(String accessId, GmMetaModel model) {
		this.model = model;
		this.cache = GmTestTools.newSmoodAccessMemoryOnly(accessId, model);
		this.setAccessId(accessId);
	}

	@Override
	public GmMetaModel getMetaModel() {
		return model;
	}

	@Override
	protected Iterable<GenericEntity> queryPopulation(String typeSignature) throws ModelAccessException {
		EntityQuery query = EntityQueryBuilder.from(typeSignature).tc(everythingTc).done();
		return cache.queryEntities(query).getEntities();
	}

	@Override
	protected void save(AdapterManipulationReport context) throws ModelAccessException {
		PersistenceGmSession session = new BasicPersistenceGmSession(cache);
		MergingCloningContext cc = new MergingCloningContext(session);

		handleCreatedEntities(context, session, cc);
		handleUpdatedEntities(context, session, cc);
		handleDeletedEntities(context, session);
	}

	private void handleCreatedEntities(AdapterManipulationReport context, PersistenceGmSession session, MergingCloningContext cc) {
		for (GenericEntity createdEntity : context.getCreatedEntities())
			if (createdEntity.getPartition() == null)
				createdEntity.setPartition(defaultPartition);

		BaseType.INSTANCE.clone(cc.creating(true), context.getCreatedEntities(), StrategyOnCriterionMatch.skip);
		session.commit();

		for (GenericEntity createdEntity : context.getCreatedEntities()) {
			GenericEntity cloned = cc.getAssociated(createdEntity);
			// we set an id of the entity from the context, so it is returned as induced manipulation
			if (createdEntity.getId() == null)
				createdEntity.setId(cloned.getId());
		}
	}

	private void handleUpdatedEntities(AdapterManipulationReport context, PersistenceGmSession session, MergingCloningContext cc) {
		BaseType.INSTANCE.clone(cc.creating(false), context.getUpdatedEntities(), StrategyOnCriterionMatch.skip);
		session.commit();
	}

	private void handleDeletedEntities(AdapterManipulationReport context, PersistenceGmSession session) {
		for (GenericEntity deletedContextEntity : context.getDeletedEntities()) {
			GenericEntity deletedEntity = session.query().entity(deletedContextEntity).require();
			session.deleteEntity(deletedEntity);
		}

		session.commit();
	}

	static class MergingCloningContext extends StandardCloningContext {

		public final PersistenceGmSession session;
		private boolean creating;

		public MergingCloningContext(PersistenceGmSession session) {
			this.session = session;
		}

		public MergingCloningContext creating(boolean creating) {
			this.creating = creating;
			return this;
		}

		@Override
		public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property, GenericEntity instanceToBeCloned,
				GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {
			return sourceAbsenceInformation == null;
		}

		@Override
		public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
			if (creating)
				return session.createRaw(entityType);
			else
				// the clone of the updated entity taken from our local session
				// instanceToBeCloned is the one attached to the session created in BAA.applyManipulation();
				return session.query().entity(instanceToBeCloned).require();
		}

	}

}
