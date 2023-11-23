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
package com.braintribe.model.processing.query.eval.context;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Supplier;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.query.eval.api.EvalTupleSet;
import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.api.continuation.EvaluationStep;
import com.braintribe.model.processing.query.eval.api.function.QueryFunctionAspect;
import com.braintribe.model.processing.query.eval.api.function.QueryFunctionExpert;
import com.braintribe.model.processing.query.eval.api.function.aspect.LocaleQueryAspect;
import com.braintribe.model.processing.query.eval.api.repo.DelegatingRepository;
import com.braintribe.model.processing.query.eval.api.repo.IndexingRepository;
import com.braintribe.model.processing.query.eval.api.repo.Repository;
import com.braintribe.model.processing.query.eval.set.join.AbstractEvalPropertyJoin;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.queryplan.QueryPlan;
import com.braintribe.model.queryplan.TupleComponentPosition;
import com.braintribe.model.queryplan.filter.Condition;
import com.braintribe.model.queryplan.index.Index;
import com.braintribe.model.queryplan.index.MetricIndex;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.set.join.JoinedMapKey;
import com.braintribe.model.queryplan.set.join.MapJoin;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.utils.i18n.I18nTools;

/**
 * 
 */
public class BasicQueryEvaluationContext implements QueryEvaluationContext {

	protected final Repository repository;
	protected final IndexingRepository indexingRepository;
	protected final DelegatingRepository delegatingRepository;
	protected final boolean ignoreFulltextComparisons;
	protected final TupleSet topLevelTupleSet;
	protected final Map<Class<? extends QueryFunctionAspect<?>>, Supplier<?>> queryFunctionAspectProviders;

	protected final Map<Integer, TupleComponentPosition> componentPositionMapping;
	protected final Map<JoinedMapKey, MapJoin> mapJoinMapping;
	protected final int totalComponentsCount;
	protected final int resultComponentsCount;
	protected final TupleSetRepository tupleSetRepository;
	protected final ValueResolver valueResolver;
	protected final ValueTypeResolver valueTypeResolver;
	protected final IndexRepository indexRepository;
	protected final Stack<EvaluationStep> stepStack;
	protected final Stack<Object> valueStack;

	public BasicQueryEvaluationContext(Repository repository, QueryPlan queryPlan,
			Map<EntityType<? extends QueryFunction>, QueryFunctionExpert<?>> queryFunctionExperts,
			Map<Class<? extends QueryFunctionAspect<?>>, Supplier<?>> queryFunctionAspectProviders) {

		this(repository, queryPlan, queryFunctionExperts, queryFunctionAspectProviders, new TupleSetAnalyzer(queryPlan.getTupleSet()));
	}

	protected BasicQueryEvaluationContext(Repository repository, QueryPlan queryPlan,
			Map<EntityType<? extends QueryFunction>, QueryFunctionExpert<?>> queryFunctionExperts,
			Map<Class<? extends QueryFunctionAspect<?>>, Supplier<?>> queryFunctionAspectProviders, TupleSetDescriptor tsDescriptor) {

		this.repository = repository;
		this.indexingRepository = repository instanceof IndexingRepository ? (IndexingRepository) repository : null;
		this.delegatingRepository = repository instanceof DelegatingRepository ? (DelegatingRepository) repository : null;
		this.ignoreFulltextComparisons = delegatingRepository != null && delegatingRepository.supportsFulltextSearch();
		this.topLevelTupleSet = queryPlan.getTupleSet();
		this.queryFunctionAspectProviders = queryFunctionAspectProviders;

		this.componentPositionMapping = tsDescriptor.getComponentPositionMapping();
		this.mapJoinMapping = tsDescriptor.getMapJoinMapping();
		this.totalComponentsCount = tsDescriptor.fullProductComponentsCount();
		this.resultComponentsCount = tsDescriptor.resultComponentsCount();

		this.tupleSetRepository = newTupleSetRepository();
		this.valueResolver = new ValueResolver(this, queryFunctionExperts);
		this.valueTypeResolver = new ValueTypeResolver(this);
		this.indexRepository = newIndexRepository(repository);

		this.stepStack = new Stack<>();
		this.valueStack = new Stack<>();
	}

	protected TupleSetRepository newTupleSetRepository() {
		return new TupleSetRepository(this);
	}

	private IndexRepository newIndexRepository(Repository r) {
		return r instanceof IndexingRepository ? new IndexRepository(this, (IndexingRepository) r) : null;
	}

	@Override
	public int totalComponentsCount() {
		return totalComponentsCount;
	}

	@Override
	public int resultComponentsCount() {
		return resultComponentsCount;
	}

	// #########################################
	// ## . . . . . . Resolution . . . . . . .##
	// #########################################

	@Override
	public Object resolveStaticValue(Object value) {
		if (value instanceof PersistentEntityReference)
			return resolveReference((PersistentEntityReference) value);

		if (value instanceof Collection)
			if (value instanceof Set)
				return resolveCollection((Collection<?>) value, newSet());
			else
				return resolveCollection((Collection<?>) value, newList());

		if (value instanceof Map) {
			Map<Object, Object> map = newMap();

			for (Map.Entry<?, ?> e : ((Map<?, ?>) value).entrySet())
				map.put(resolveStaticValue(e.getKey()), resolveStaticValue(e.getValue()));

			return map;
		}

		if (value instanceof EnumReference)
			return resolveEnumReference((EnumReference) value);

		return value;
	}

	private Object resolveCollection(Collection<?> originalValues, Collection<Object> resolvedValue) {
		for (Object o : originalValues)
			resolvedValue.add(resolveStaticValue(o));

		return resolvedValue;
	}

	protected Object resolveReference(PersistentEntityReference reference) {
		return repository.resolveReference(reference);
	}

	private Object resolveEnumReference(EnumReference ref) {
		EnumType enumType = GMF.getTypeReflection().getType(ref.getTypeSignature());

		return enumType.getEnumValue(ref.getConstant());
	}

	@Override
	public EvalTupleSet resolveTupleSet(TupleSet tupleSet) {
		return tupleSetRepository.resolveTupleSet(tupleSet);
	}

	@Override
	public <T> T resolveValue(Tuple tuple, Value value) {
		return valueResolver.resolve(tuple, value);
	}

	/** This method might not be needed. See comment at {@link AbstractEvalPropertyJoin} */
	@Override
	public <T extends GenericModelType> T resolveValueType(Value value) {
		return valueTypeResolver.resolveType(value);
	}

	@Override
	public <T> T getFunctionAspect(Class<? extends QueryFunctionAspect<T>> aspect) {
		Supplier<T> supplier = (Supplier<T>) queryFunctionAspectProviders.get(aspect);
		if (supplier == null)
			throw new RuntimeQueryEvaluationException("Aspect not found: " + aspect.getName());

		try {
			return supplier.get();

		} catch (RuntimeException e) {
			throw new RuntimeQueryEvaluationException("Error while providing aspect " + aspect.getName() + ".", e);
		}
	}

	@Override
	public boolean fulfillsCondition(Tuple tuple, Condition condition) {
		return ConditionEvaluator.getInstance().evaluate(tuple, condition, this);
	}

	@Override
	public boolean ignoreFulltextComparisons() {
		return ignoreFulltextComparisons;
	}

	/** We could get rid of this, see {@link BasicQueryEvaluationContext#resolveValueType(Value)} */
	@Override
	public TupleComponentPosition findTupleComponentPosition(int index) {
		return componentPositionMapping.get(index);
	}

	@Override
	public MapJoin findJoinForJoinedMapKey(JoinedMapKey mapKey) {
		return mapJoinMapping.get(mapKey);
	}

	@Override
	public String resolveLocalizedString(LocalizedString ls) {
		if (ls == null)
			return null;

		String locale = getFunctionAspect(LocaleQueryAspect.class);
		return I18nTools.get(ls, locale);
	}

	// #########################################
	// ## . . . . . . Data Access . . . . . . ##
	// #########################################

	@Override
	public Iterable<? extends GenericEntity> getPopulation(String typeSignature) {
		return repository.providePopulation(typeSignature);
	}

	@Override
	public Iterable<? extends GenericEntity> getEntities(String signature, com.braintribe.model.query.conditions.Condition condition,
			Ordering ordering) {
		return delegatingRepository.provideEntities(signature, condition, ordering);
	}

	@Override
	public Iterable<Tuple> getIndexRange(MetricIndex index, Object from, Boolean fromInclusive, Object to, Boolean toInclusive) {
		return indexRepository.resolveMetricIndex(index).getIndexRange(from, fromInclusive, to, toInclusive);
	}

	@Override
	public Iterable<Tuple> getFullRange(MetricIndex index, boolean reverseOrder) {
		return indexRepository.resolveMetricIndex(index).getFullRange(reverseOrder);
	}

	@Override
	public Iterable<Tuple> getAllValuesForIndex(Index index, Object indexValue) {
		return indexRepository.resolveIndex(index).getAllValuesForIndex(indexValue);
	}

	@Override
	public Iterable<Tuple> getAllValuesForIndices(Index index, Collection<?> indexValues) {
		return indexRepository.resolveIndex(index).getAllValuesForIndices(indexValues);
	}

	@Override
	public Collection<? extends GenericEntity> getAllValuesForIndexDirectly(String indexId, Object indexValue) {
		return indexingRepository.getAllValuesForIndex(indexId, indexValue);
	}

	@Override
	public Collection<? extends GenericEntity> getAllValuesForIndicesDirectly(String indexId, Collection<?> indexValues) {
		return indexingRepository.getAllValuesForIndices(indexId, indexValues);
	}

	// #########################################
	// ## . . . . ContinuableIteration . . . .##
	// #########################################

	@Override
	public void pushStep(EvaluationStep step) {
		stepStack.push(step);
	}

	@Override
	public EvaluationStep popStep() {
		return stepStack.pop();
	}

	@Override
	public void pushValue(Value value) {
		valueStack.push(value);
	}

	@Override
	public <T> T popValue() {
		return (T) valueStack.pop();
	}

}
