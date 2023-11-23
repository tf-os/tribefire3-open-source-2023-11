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
package com.braintribe.model.processing.session.impl.persistence.selectiveinfo;

import static com.braintribe.model.generic.manipulation.ManipulationType.MANIFESTATION;
import static com.braintribe.model.processing.session.impl.persistence.selectiveinfo.SelectiveInformationLoadingTools.needsLoading;
import static com.braintribe.utils.lcd.CollectionTools2.acquireSet;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.manipulation.ManifestationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.managed.SelectQueryResultConvenience;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.QueryResultMergeListener;
import com.braintribe.model.query.QueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.processing.async.api.AsyncCallback;

/**
 * This class makes sure each newly manifested entity has all the properties required for selective information loaded.
 * 
 * NOTE Currently we only support selective information configured via meta-data, as the GM reflection does not provide information about
 * which properties are needed for selective information in case it was defined per {@link SelectiveInformation java annotation}.
 * 
 * NOTE This implementation is not thread safe and can only be used in GWT environment right now. But if needed, it could be adjusted.
 * 
 * @author peter.gazdik
 */
public class SelectiveInformationPropertyLoader implements ManipulationListener, QueryResultMergeListener {

	private final SelectiveInformationPropertyChainResolver propertyChainResolver;
	private final Map<EntityType<?>, Set<GenericEntity>> manifestedEntities = newMap();

	public SelectiveInformationPropertyLoader(PersistenceGmSession session) {
		propertyChainResolver = new SelectiveInformationPropertyChainResolver(session);
	}

	/**
	 * Registers a new instance of {@link SelectiveInformationPropertyLoader} for the session, i.e. it registers it as both
	 * {@link ManipulationListener} and {@link QueryResultMergeListener}.
	 */
	public static void registerFor(PersistenceGmSession session) {
		SelectiveInformationPropertyLoader result = new SelectiveInformationPropertyLoader(session);
		session.listeners().add((ManipulationListener) result);
		session.listeners().add((QueryResultMergeListener) result);
	}

	// ##################################################
	// ## . . . . . . ManipulationListener . . . . . . ##
	// ##################################################

	@Override
	public void noticeManipulation(Manipulation manipulation) {
		if (manipulation.manipulationType() != MANIFESTATION) {
			return;
		}

		onEntityManifested(((ManifestationManipulation) manipulation).getEntity());
	}

	/** Here we remember an entity in case some of the properties needed for the selective information is absent. */
	private void onEntityManifested(GenericEntity entity) {
		EntityType<?> et = entity.entityType();

		List<Property[]> propertyChains = propertyChainResolver.getPropertyChain(et);
		if (propertyChains.isEmpty()) {
			return;
		}

		if (needsLoading(entity, propertyChains)) {
			acquireSet(manifestedEntities, et).add(entity);
		}
	}

	// ##################################################
	// ## . . . . . QueryResultMergeListener . . . . . ##
	// ##################################################

	@Override
	public void onAfterQueryResultMerged(PersistenceGmSession session, QueryResult queryResult) {
		for (Entry<EntityType<?>, Set<GenericEntity>> entry: manifestedEntities.entrySet()) {
			EntityType<?> et = entry.getKey();
			Set<GenericEntity> entities = entry.getValue();

			loadSelectiveInfoPropertiesFor(session, et, entities);
		}

		manifestedEntities.clear();
	}

	private void loadSelectiveInfoPropertiesFor(PersistenceGmSession session, EntityType<?> et, Set<GenericEntity> entities) {
		TraversingCriterion tc = propertyChainResolver.getTc(et);

		// @formatter:off
		SelectQuery query = new SelectQueryBuilder()
					.from(et, "e")
					.where()
						.entity("e").in(referencesFor(entities))
					.tc(tc)
				.done();
		// @formatter:on

		session.query().select(query).result(new AsyncCallback<SelectQueryResultConvenience>() {
			@Override
			public void onSuccess(SelectQueryResultConvenience future) {
				// nothing to do here, we just wanted to run the query
			}

			@Override
			public void onFailure(Throwable t) {
				// nothing to do here, we just wanted to run the query - it failed, but that's life
			}
		});

	}

	private Set<EntityReference> referencesFor(Set<GenericEntity> entities) {
		Set<EntityReference> result = newSet();
		for (GenericEntity entity: entities) {
			result.add(entity.reference());
		}

		return result;
	}

}
