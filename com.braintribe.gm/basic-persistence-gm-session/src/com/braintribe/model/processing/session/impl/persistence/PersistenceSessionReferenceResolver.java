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
package com.braintribe.model.processing.session.impl.persistence;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.commons.EntRefHashingComparator;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.manipulation.basic.oracle.AbstractReferenceResolver;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;

/**
 * @author peter.gazdik
 */
public class PersistenceSessionReferenceResolver extends AbstractReferenceResolver {

	private final PersistenceGmSession session;
	private final Function<String, TraversingCriterion> tcProvider;

	public PersistenceSessionReferenceResolver(PersistenceGmSession session) {
		this(session, signature -> null);
	}

	public PersistenceSessionReferenceResolver(PersistenceGmSession session, Function<String, TraversingCriterion> tcProvider) {
		this(session, tcProvider, 100);
	}

	public PersistenceSessionReferenceResolver(PersistenceGmSession session, Function<String, TraversingCriterion> tcProvider, int bulkSize) {
		super(bulkSize);

		this.session = session;
		this.tcProvider = tcProvider;
	}

	@Override
	protected Map<PersistentEntityReference, GenericEntity> resolveBulk(String typeSignature, Set<PersistentEntityReference> references) {
		List<GenericEntity> entities = queryEntities(typeSignature, references);

		checkResolvedReferences(references, entities);

		return mapByReference(entities);
	}

	private List<GenericEntity> queryEntities(String typeSignature, Set<PersistentEntityReference> references) {
		EntityQuery query = buildQuery(typeSignature, references);

		return session.query().entities(query).list();
	}

	private EntityQuery buildQuery(String typeSignature, Set<PersistentEntityReference> references) {
		TraversingCriterion tc = tcProvider.apply(typeSignature);

		// @formatter:off
		return EntityQueryBuilder.from(typeSignature)
				.where()
					.entity(EntityQueryBuilder.DEFAULT_SOURCE).in(references)
				.tc(tc)
				.done();
		// @formatter:on
	}

	private void checkResolvedReferences(Set<PersistentEntityReference> refs, List<GenericEntity> entities) {
		if (refs.size() == entities.size())
			return;

		Set<?> resolvedRefs = entities.stream() //
				.map(GenericEntity::reference) //
				.collect(Collectors.toSet());

		Set<EntityReference> unresolvedRefs = CodingSet.create(EntRefHashingComparator.INSTANCE);
		unresolvedRefs.removeAll(resolvedRefs);

		throw new NotFoundException("Could not resolve the following entity references: " + unresolvedRefs);
	}

	private Map<PersistentEntityReference, GenericEntity> mapByReference(List<GenericEntity> entities) {
		Map<PersistentEntityReference, GenericEntity> result = newMap();

		for (GenericEntity entity : entities)
			result.put(entity.reference(), entity);

		return result;
	}

}
