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
package com.braintribe.gwt.utils.genericmodel;

import java.util.HashSet;
import java.util.Set;

import com.braintribe.common.lcd.ConfigurationException;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;

/**
 * This {@link EntitiesFinder} is used to find entities based on a set of configured entity types. For more info see
 * method {@link #findEntities(PersistenceGmSession)}.
 *
 */
public class EntityTypeBasedEntitiesFinder extends EntityQueryBasedEntitiesFinder {

	private Set<String> entityTypeSignatures;

	private TraversingCriterion traversingCriterion = TC.create().negation().joker().done();

	public EntityTypeBasedEntitiesFinder() {
		// nothing to do
	}

	public EntityTypeBasedEntitiesFinder(final Set<String> entityTypeSignatures) {
		this.entityTypeSignatures = entityTypeSignatures;
	}

	/**
	 * Iterates through the set of the configured {@link #setEntityTypes(Set) entityTypes}, creates a new
	 * {@link EntityQuery} based on an entityType, finds the respective entities for each entity type and returns the
	 * union of the result sets.
	 *
	 * @throws ConfigurationException
	 *             if the list of the entity types is <code>null</code>.
	 */
	@Override
	public Set<GenericEntity> findEntities(final PersistenceGmSession session) {

		final Set<GenericEntity> foundEntitiesUnion = new HashSet<GenericEntity>();

		if (this.entityTypeSignatures == null) {
			throw new ConfigurationException("The configured set of entity types must not be null!");
		}

		for (final String entityTypeSignature : this.entityTypeSignatures) {
			final EntityQuery entityQuery = EntityQuery.T.create();
			setEntityQuery(entityQuery);
			entityQuery.setEntityTypeSignature(entityTypeSignature);
			entityQuery.setTraversingCriterion(getTraversingCriterion());

			final Set<GenericEntity> foundEntitiesBasedOnEntityType = super.findEntities(session);
			foundEntitiesUnion.addAll(foundEntitiesBasedOnEntityType);
		}
		return foundEntitiesUnion;
	}

	public void setEntityTypes(final Set<Class<? extends GenericEntity>> entityTypes) {
		this.entityTypeSignatures = new HashSet<String>();
		for (final Class<? extends GenericEntity> entityType : entityTypes) {
			this.entityTypeSignatures.add(entityType.getName());
		}
	}

	public void setEntityTypeSignatures(final Set<String> entityTypeSignatures) {
		this.entityTypeSignatures = entityTypeSignatures;
	}

	public TraversingCriterion getTraversingCriterion() {
		return this.traversingCriterion;
	}

	public void setTraversingCriterion(final TraversingCriterion traversingCriterion) {
		this.traversingCriterion = traversingCriterion;
	}
}
