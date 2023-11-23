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
package com.braintribe.model.processing.query.planner.context;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.eval.api.repo.DelegatingRepository;
import com.braintribe.model.processing.query.eval.api.repo.IndexInfo;
import com.braintribe.model.processing.query.eval.api.repo.IndexingRepository;
import com.braintribe.model.processing.query.eval.api.repo.Repository;
import com.braintribe.model.processing.query.planner.core.QueryPlannerCore;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.query.functions.aggregate.AggregateFunction;
import com.braintribe.model.queryplan.index.Index;
import com.braintribe.model.queryplan.set.AggregatingProjection;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.QuerySourceSet;
import com.braintribe.model.queryplan.set.SourceSet;
import com.braintribe.model.queryplan.value.TupleComponent;
import com.braintribe.model.queryplan.value.Value;

/**
 * @author peter.gazdik
 */
public class QueryPlannerContext implements QueryOperandToValueConverter {

	private final SelectQuery query;
	private final Set<GenericEntity> evaluationExcludes;
	private final Repository repository;
	private final IndexingRepository indexingRepository;

	private final QuerySourceManager querySourceManager;

	private final ConditionConverter conditionConverter;
	private final OperandConverter operandConverter;
	private final ConditionSourceResolver conditionSourceResolver;
	private final IndexRepository indexRepository;
	private final QueryFunctionManager functionManager;
	private final QueryOrderingManager orderingManager;
	private final QueryAggregationManager aggregationManager;

	public QueryPlannerContext(SelectQuery query, Repository repository) {
		this.query = query;
		this.evaluationExcludes = query.getEvaluationExcludes();
		this.repository = repository;
		this.indexingRepository = (repository instanceof IndexingRepository) ? (IndexingRepository) repository : null;

		this.querySourceManager = new QuerySourceManager(query);

		this.conditionConverter = new ConditionConverter(this);
		this.operandConverter = new OperandConverter(this);
		this.conditionSourceResolver = new ConditionSourceResolver(this);
		this.indexRepository = new IndexRepository(this);
		this.functionManager = new QueryFunctionManager(this);
		this.orderingManager = new QueryOrderingManager(this, query);
		this.aggregationManager = new QueryAggregationManager(this, query);
	}

	// TODO OPTIMIZE - sometimes the query might such that it is already distinct... (e.g. select p from Person p)
	public boolean needsDistinct() {
		return query.getDistinct();
	}

	public QuerySourceManager sourceManager() {
		return querySourceManager;
	}

	public QueryOrderingManager orderingManager() {
		return orderingManager;
	}

	public QueryAggregationManager aggregationManager() {
		return aggregationManager;
	}

	public SelectQuery query() {
		return query;
	}

	/**
	 * This event is called once we have processed the aggregation of our query, i.e. when building the QueryPlan has built the
	 * {@link AggregatingProjection} in the {@link QueryPlannerCore#buildQueryPlan()}. With this done, every {@link AggregateFunction} operand in our
	 * query has a specific position in the tuple set, and thus every {@link #convertOperand(Object) operand conversion} for such an operand should be
	 * resolved as the corresponding {@link TupleComponent}, rather than {@link com.braintribe.model.queryplan.value.AggregateFunction}.
	 */
	public void noticePostAggregation() {
		operandConverter.noticePostAggregation();
	}

	public IndexInfo getIndexInfo(From from, String propertyName) {
		return getIndexInfo(from.getEntityTypeSignature(), propertyName);
	}

	public IndexInfo getIndexInfo(String signature, String propertyName) {
		return indexingRepository != null ? indexingRepository.provideIndexInfo(signature, propertyName) : null;
	}

	public com.braintribe.model.queryplan.filter.Condition convertCondition(Condition condition) {
		return conditionConverter.convert(condition);
	}

	@Override
	public Value convertOperand(Object operand) {
		return operandConverter.convert(operand);
	}

	public Set<From> getFromsFor(Condition condition) {
		return conditionSourceResolver.resolveFromsFor(condition);
	}

	public Set<From> getFromsForOperand(Object operand) {
		return conditionSourceResolver.resolveFromsForOperand(operand);
	}

	public <T extends Index> T getIndex(From from, IndexInfo indexInfo) {
		return indexRepository.acquireIndex(from, indexInfo);
	}

	public Predicate<Object> evalExclusionCheck() {
		return this::isEvaluationExclude;
	}

	public boolean isStaticValue(Object o) {
		return !(o instanceof Operand) || isEvaluationExclude(o);
	}

	public boolean isEvaluationExclude(Object value) {
		return evaluationExcludes != null && evaluationExcludes.contains(value);
	}

	public Map<Object, Value> noticeQueryFunction(QueryFunction queryFunction) {
		return functionManager.noticeQueryFunction(queryFunction);
	}

	public List<Operand> listOperands(QueryFunction queryFunction) {
		return functionManager.listOperands(queryFunction);
	}

	/**
	 * @return true iff given repository supports retrieving entities using {@link EntityQuery}. In such case, the planner uses {@link QuerySourceSet}
	 *         rather than a composition of {@link FilteredSet} and {@link SourceSet}.
	 */
	public boolean supportsEntityQueryDelegation() {
		return repository instanceof DelegatingRepository;
	}

}
