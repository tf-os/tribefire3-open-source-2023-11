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
package com.braintribe.product.rat.imp.impl.utils;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.managed.EntityQueryExecution;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.utils.lcd.CommonTools;

/**
 * A lot of utility methods for simple queries. <b>still in alpha-mode so better don't use it - everything can
 * change</b>
 */
public class QueryHelper {
	private final PersistenceGmSession session;
	private boolean isStrict;

	public QueryHelper(PersistenceGmSession session) {
		this.session = session;
	}

	public <T extends GenericEntity> T findById(EntityType<T> type, Object id) {
		return entityWithProperty(type, "id", id);
	}

	public <T extends GenericEntity> T findAny(EntityType<T> type) {
		return (T) findAny(EntityQueryBuilder.from(type));
	}

	public List<GenericEntity> allPersistedEntities() {
		return allPersistedEntities(GenericEntity.T);
	}

	public <T extends GenericEntity> List<T> allPersistedEntities(EntityType<T> type) {
		return findAll(EntityQueryBuilder.from(type));
	}

	public <T extends GenericEntity> T findUnique(EntityType<T> type) {

		return findUnique(EntityQueryBuilder.from(type));
	}

	public <T extends GenericEntity> T findAnyWith(EntityType<T> type, Predicate<T> predicate) {
		List<T> result = allPersistedEntities(type);
		return result.stream().filter(predicate).findFirst().orElse(null);
	}

	public <T extends GenericEntity> List<T> findAllWith(EntityType<T> type, Predicate<T> predicate) {
		List<T> result = allPersistedEntities(type);
		return result.stream().filter(predicate).collect(Collectors.toList());
	}

	/**
	 * queries for all entities of given type and returns all that are not contained in oldOnes
	 *
	 * @return the "new ones" of given type
	 */
	public <T extends GenericEntity> List<T> getNew(EntityType<T> type, Collection<T> oldOnes) {
		return allPersistedEntities(type).stream().filter(x -> !oldOnes.contains(x)).collect(Collectors.toList());
	}

	public <T extends GenericEntity> T entityWithProperty(EntityType<T> type, String propertyName, Object propertyValue) {

		return findUnique(EntityQueryBuilder.from(type).where().property(propertyName).eq(propertyValue));
	}

	public <T extends GenericEntity> List<T> entitiesWithProperty(EntityType<T> type, String propertyName, Object propertyValue) {

		return findAll(EntityQueryBuilder.from(type).where().property(propertyName).eq(propertyValue));
	}

	public <T extends GenericEntity> List<T> entitiesWithPropertyLike(EntityType<T> type, String propertyName, String propertyValueRegex) {

		return findAll(EntityQueryBuilder.from(type).where().property(propertyName).like(propertyValueRegex));
	}

	public <T extends GenericEntity> List<T> entitiesWithPropertyIn(EntityType<T> type, String propertyName, Object... propertyValues) {

		return findAll(EntityQueryBuilder.from(type).where().property(propertyName).in(CommonTools.getSet(propertyValues)));

	}

	public EntityQuery traversingAll(EntityQueryBuilder builder) {
		TraversingCriterion tc = TC.create().negation().joker().done();

		return builder.tc().criterion(tc).done();
	}

	public EntityQueryExecution run(EntityQueryBuilder builder) {
		EntityQuery entityQuery = builder.done();// traversingAll(builder);

		// update cache
		// session.query().entities(entityQuery).list();

		// execute query now via cache
		// EntityQueryExecution execution = session.queryCache().entities(entityQuery);

		EntityQueryExecution execution = session.query().entities(entityQuery);
		// NOR TODO: This is triggering a query and the result is thrown away.
		List<?> foundEntities = execution.list();

		if (isStrict && foundEntities.isEmpty()) {
			throw new GmSessionException("Found no entity matching your query but strictly expecting one\n" + entityQuery);
		}

		return execution;
	}

	private <T extends GenericEntity> List<T> findAll(EntityQueryBuilder builder) {
		return run(builder).list();
	}

	private <T extends GenericEntity> T findUnique(EntityQueryBuilder builder) {
		return run(builder).unique();
	}

	public <T extends GenericEntity> T findAny(EntityQueryBuilder builder) {
		return run(builder).first();
	}

	public QueryHelper strictly() {
		QueryHelper strictHelper = new QueryHelper(session);
		strictHelper.isStrict = true;

		return strictHelper;
	}
}
