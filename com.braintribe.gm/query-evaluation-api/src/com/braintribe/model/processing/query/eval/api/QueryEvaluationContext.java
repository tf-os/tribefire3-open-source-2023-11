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
package com.braintribe.model.processing.query.eval.api;

import java.util.Collection;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.query.eval.api.continuation.EvaluationStep;
import com.braintribe.model.processing.query.eval.api.function.QueryFunctionAspect;
import com.braintribe.model.processing.query.eval.api.repo.DelegatingRepository;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.queryplan.TupleComponentPosition;
import com.braintribe.model.queryplan.filter.Condition;
import com.braintribe.model.queryplan.index.Index;
import com.braintribe.model.queryplan.index.MetricIndex;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.set.join.JoinedMapKey;
import com.braintribe.model.queryplan.set.join.MapJoin;
import com.braintribe.model.queryplan.value.Value;

/**
 * 
 */
public interface QueryEvaluationContext {

	/**
	 * Returns total number of tuple components (or tuple dimensions) used while processing the query. This number is based on the
	 * <tt>FROM</tt> clause of the query. This information is used to create the {@link Tuple}s with the right size.
	 */
	int totalComponentsCount();

	/**
	 * Returns number of tuple components of the query result. This number is based on the <tt>SELECT</tt> clause of the query.
	 */
	int resultComponentsCount();

	// #########################################
	// ## . . . . . . Resolution . . . . . . .##
	// #########################################

	/** For example might turn resolve {@link GenericEntity} for given {@link PersistentEntityReference} */
	Object resolveStaticValue(Object value);

	/**
	 * Returns a corresponding {@link EvalTupleSet} for given {@link TupleSet}. Invoking this method multiple times with the same instance
	 * as parameter always results in same instance being returned.
	 */
	EvalTupleSet resolveTupleSet(TupleSet tupleSet);

	/** Returns the actual value represented by given {@link Value} for given {@link Tuple}. */
	<T> T resolveValue(Tuple tuple, Value value);

	/** Returns a value for given {@link QueryFunctionAspect}. */
	<T> T getFunctionAspect(Class<? extends QueryFunctionAspect<T>> aspect);

	/** Returns the type represented by given {@link Value}. */
	<T extends GenericModelType> T resolveValueType(Value value);

	/** Checks whether given {@link Condition} holds for given {@link Tuple}. */
	boolean fulfillsCondition(Tuple tuple, Condition condition);

	/**
	 * Returns true if we are backed by a {@link DelegatingRepository} which takes care of fulltext comparisons, thus we do not want to
	 * evaluate it.
	 */
	boolean ignoreFulltextComparisons();
	
	/** Finds the corresponding {@link TupleComponentPosition} for given index. */
	TupleComponentPosition findTupleComponentPosition(int tupleComponentIndex);

	MapJoin findJoinForJoinedMapKey(JoinedMapKey tupleComponentPosition);

	String resolveLocalizedString(LocalizedString ls);

	// #########################################
	// ## . . . . . . Data Access . . . . . . ##
	// #########################################

	/** Returns all entities of given type from the DB. */
	Iterable<? extends GenericEntity> getPopulation(String typeSignature);

	/**
	 * Returns entities for given type, which might be pre-filtered by given conditions. Context must only implement this method if dealing
	 * with {@link DelegatingRepository}.
	 */
	Iterable<? extends GenericEntity> getEntities(String signature, com.braintribe.model.query.conditions.Condition condition,
			Ordering ordering);

	/** Returns entities by given index and bounds */
	Iterable<Tuple> getIndexRange(MetricIndex index, Object from, Boolean fromInclusive, Object to, Boolean toInclusive);

	/** Returns all entities from given metric index, sorted by this index (or the reverse of this order) */
	Iterable<Tuple> getFullRange(MetricIndex index, boolean reverseOrder);

	/** Returns all entities for given index and indexed-property value */
	Iterable<Tuple> getAllValuesForIndex(Index index, Object indexValue);

	/** Returns all entities for given index and collection of indexed-property values */
	Iterable<Tuple> getAllValuesForIndices(Index index, Collection<?> indexValues);

	/**
	 * Similar to {@link #getAllValuesForIndex(Index, Object)}, but returns the entities from repository directly, without wrapping them
	 * into {@link Tuple}s
	 */
	Collection<? extends GenericEntity> getAllValuesForIndexDirectly(String indexId, Object indexValue);

	/**
	 * Similar to {@link #getAllValuesForIndices(Index, Collection)}, but returns the entities from repository directly, without wrapping
	 * them into {@link Tuple}s
	 */
	Collection<? extends GenericEntity> getAllValuesForIndicesDirectly(String indexId, Collection<?> indexValues);

	// #########################################
	// ## . . . . ContinuableIteration . . . .##
	// #########################################

	void pushStep(EvaluationStep step);

	EvaluationStep popStep();

	void pushValue(Value value);

	<T> T popValue();

}
