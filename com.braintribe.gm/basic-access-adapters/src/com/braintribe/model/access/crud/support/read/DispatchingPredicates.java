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
package com.braintribe.model.access.crud.support.read;

import java.util.function.Predicate;

import com.braintribe.model.access.crud.api.read.PopulationReadingContext;
import com.braintribe.model.access.crud.api.read.PropertyReadingContext;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

/**
 * A collection of {@link Predicate}'s that might be handy when registering
 * experts for a {@link DispatchingReader} implementation.
 * 
 * @author gunther.schenk
 *
 */
public interface DispatchingPredicates {

	static Predicate<?> ALWAYS_TRUE = c -> true;
	static Predicate<?> IS_NOT_NULL = c -> c != null;

	@SuppressWarnings("unchecked")
	default <T> Predicate<T> isAlwaysTrue() {
		return (Predicate<T>) ALWAYS_TRUE;
	}

	@SuppressWarnings("unchecked")
	default <T> Predicate<T> isNotNull() {
		return (Predicate<T>) IS_NOT_NULL;
	}

	default <T extends GenericEntity> Predicate<PopulationReadingContext<T>> isExclusiveIdCondition() {
		return c -> c.getConditionAnalysis().hasIdComparisonsExclusively();
	}

	default <T extends GenericEntity> boolean isRequestedProperty(PropertyReadingContext<T> context,
			String propertyName) {
		return context.getPropertyName().equals(propertyName);
	}

	default <T extends GenericEntity> Predicate<PopulationReadingContext<T>> isRequestedTypeEqual(
			EntityType<T> comparisonType) {
		return c -> c.getRequestedType() == comparisonType;
	}
}
