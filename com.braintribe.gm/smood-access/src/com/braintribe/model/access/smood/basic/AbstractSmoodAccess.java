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
package com.braintribe.model.access.smood.basic;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.access.AbstractAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.matching.Matcher;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;

public abstract class AbstractSmoodAccess extends AbstractAccess {

	protected Supplier<String> localeProvider;

	protected ReadWriteLock readWriteLock;
	protected Lock readLock;
	protected Lock writeLock;

	@Configurable
	public void setReadWriteLock(ReadWriteLock readWriteLock) {
		this.readWriteLock = readWriteLock;
		this.readLock = readWriteLock.readLock();
		this.writeLock = readWriteLock.writeLock();
	}

	@Configurable
	public void setLocaleProvider(Supplier<String> localeProvider) {
		this.localeProvider = localeProvider;
	}

	@Override
	public SelectQueryResult query(SelectQuery query) {
		SmoodAccessLogging.selectQuery(query);

		readLock.lock();
		try {
			return r_query(query);

		} finally {
			readLock.unlock();
		}
	}

	protected SelectQueryResult r_query(SelectQuery query) {
		SelectQueryResult result = getDatabase().query(query);
		List<Object> clonedResults = cloneSelectQueryResult(result.getResults(), query, createStandardCloningContext(), cloningStrategy(query));
		result.setResults(clonedResults);

		SmoodAccessLogging.selectQueryEvaluationFinished();

		return result;
	}

	@Override
	public EntityQueryResult queryEntities(EntityQuery query) {
		readLock.lock();
		try {
			return r_queryEntities(query);

		} finally {
			readLock.unlock();
		}
	}

	protected EntityQueryResult r_queryEntities(EntityQuery query) {
		EntityQueryResult result = getDatabase().queryEntities(query);

		Matcher matcher = getMatcher(query);

		List<GenericEntity> cloned = cloneEntityQueryResult(result.getEntities(), matcher, createStandardCloningContext(), cloningStrategy(query));

		result.setEntities(cloned);

		return result;
	}

	@Override
	public PropertyQueryResult queryProperty(PropertyQuery query) {
		readLock.lock();
		try {
			return r_queryProperty(query);

		} catch (RuntimeException e) {
			throw Exceptions.contextualize(e, "Error while querying property '" + query.getPropertyName() + "' of: " + query.getEntityReference().getTypeSignature());

		} finally {
			readLock.unlock();
		}
	}

	protected PropertyQueryResult r_queryProperty(PropertyQuery query) {
		PropertyQueryResult result = getDatabase().queryProperty(query);

		Object propertyValue = result.getPropertyValue();

		if (propertyValue != null) {
			Object clonedPropertyValue = clonePropertyQueryResult(null, propertyValue, query, createStandardCloningContext(), cloningStrategy(query));

			result.setPropertyValue(clonedPropertyValue);
		}

		return result;
	}

	private StrategyOnCriterionMatch cloningStrategy(Query request) {
		return (request.getNoAbsenceInformation()) ? StrategyOnCriterionMatch.skip : StrategyOnCriterionMatch.partialize;
	}

	protected abstract Smood getDatabase();

}
