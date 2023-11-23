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
package com.braintribe.model.access.crud.api.read;

import com.braintribe.model.access.crud.api.DataReadingContext;
import com.braintribe.model.access.crud.api.query.ConditionAnalysis;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.conditions.Condition;

/**
 * A {@link DataReadingContext} implementation provided to
 * {@link PopulationReader} experts containing necessary informations on the
 * expected instances.
 * 
 * @author gunther.schenk
 */
public interface PopulationReadingContext<T extends GenericEntity> extends DataReadingContext<T> {

	/**
	 * @return the actual requested type of the expected instances.
	 */
	EntityType<T> getRequestedType();

	Condition getCondition();

	Ordering getOrdering();

	ConditionAnalysis getConditionAnalysis();
	
	/**
	 * Static helper method to build a new {@link PopulationReadingContext}
	 * instance.
	 */
	static <T extends GenericEntity> PopulationReadingContext<T> create(EntityType<T> requestedType,
			Condition condition, Ordering ordering, ConditionAnalysis conditionAnalysis) {
		return create(requestedType, condition, ordering, conditionAnalysis, null);
	}

	/**
	 * Static helper method to build a new {@link PopulationReadingContext}
	 * instance.
	 */
	static <T extends GenericEntity> PopulationReadingContext<T> create(EntityType<T> requestedType,
			Condition condition, Ordering ordering, ConditionAnalysis conditionAnalysis, QueryContext queryContext) {
		return new PopulationReadingContext<T>() {
			@Override
			public Condition getCondition() {
				return condition;
			}

			@Override
			public Ordering getOrdering() {
				return ordering;
			}

			@Override
			public EntityType<T> getRequestedType() {
				return requestedType;
			}

			@Override
			public ConditionAnalysis getConditionAnalysis() {
				return conditionAnalysis;
			}
			@Override
			public QueryContext getQueryContext() {
				return queryContext;
			}
			
		};
	}

}
