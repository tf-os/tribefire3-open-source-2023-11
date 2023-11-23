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

import java.util.stream.Collectors;

import com.braintribe.model.access.crud.api.query.ConditionAnalysis;
import com.braintribe.model.access.crud.api.read.EntityReader;
import com.braintribe.model.access.crud.api.read.EntityReadingContext;
import com.braintribe.model.access.crud.api.read.PopulationReader;
import com.braintribe.model.access.crud.api.read.PopulationReadingContext;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.query.conditions.Condition;

/**
 * An implementation of {@link PopulationReader} that takes the id values(referenced in {@link Condition} and provided by the {@link ConditionAnalysis})
 * and delegates them the configured {@link EntityReader}. All resulting entities are collected and
 * finally returned as a result of {@link #findEntities(PopulationReadingContext)}.
 *    
 * @author gunther.schenk
 */
public class IdReaderBridge<T extends GenericEntity> implements PopulationReader<T>, DispatchingPredicates{
	
	private EntityReader<T> target;

	private IdReaderBridge(EntityReader<T> target) {
		this.target = target;
	}

	// ***************************************************************************************************
	// Static Instantiation
	// ***************************************************************************************************

	/**
	 * @return instance of {@link IdReaderBridge} with provided target {@link EntityReader}.
	 */
	public static <T extends GenericEntity> IdReaderBridge<T> instance(EntityReader<T> target) {
		return new IdReaderBridge<>(target);
	}
	
	// ***************************************************************************************************
	// PopulationReader
	// ***************************************************************************************************

	@Override
	public Iterable<T> findEntities(PopulationReadingContext<T> context) {
		EntityType<T> requestedType = context.getRequestedType();
		ConditionAnalysis conditionAnalysis = context.getConditionAnalysis();

		// @formatter:off
		return conditionAnalysis.getComparedIds()
			.stream()
			.map(id->EntityReadingContext.create(requestedType, id))
			.map(target::getEntity)
			.filter(isNotNull())
			.collect(Collectors.toList());
		// @formatter:on
	}

}
