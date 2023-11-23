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
package com.braintribe.model.processing.smart.query.planner.context;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static java.util.Collections.emptyList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.query.eval.api.function.QueryFunctionExpert;
import com.braintribe.model.processing.query.planner.condition.ConstantCondition;
import com.braintribe.model.processing.query.planner.context.QueryOperandToValueConverter;
import com.braintribe.model.processing.smart.query.planner.SmartQueryPlannerException;
import com.braintribe.model.processing.smart.query.planner.core.builder.ScalarPlanBuilder;
import com.braintribe.model.processing.smart.query.planner.graph.QueryPlanStructure;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup;
import com.braintribe.model.processing.smart.query.planner.structure.ModelExpert;
import com.braintribe.model.processing.smart.query.planner.tools.CollectionConditionExpert;
import com.braintribe.model.processing.smart.query.planner.tools.ReferenceConverter;
import com.braintribe.model.processing.smartquery.eval.api.SmartConversionExpert;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.ConditionType;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.query.functions.JoinFunction;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.query.functions.aggregate.AggregateFunction;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.model.smartqueryplan.queryfunctions.ResolveDelegateProperty;
import com.braintribe.model.smartqueryplan.queryfunctions.ResolveId;
import com.braintribe.utils.collection.api.MultiMap;

public class SmartQueryPlannerContext implements QueryOperandToValueConverter {

	private final SelectQuery query;
	private final ModelExpert modelExpert;
	private final Map<From, IncrementalAccess> fromMapping;
	private final IncrementalAccess smartDenotation;
	private final ReferenceConverter refConverter;
	private final List<Condition> conjunctionOperands;
	private final OperandSourceResolver operandSourceResolver;
	private final QueryPlanStructure planStructure;
	private final SmartConditionConverter conditionConverter;
	private final CollectionConditionExpert collectionConditionExpert;
	private final SmartOperandConverter operandConverter;
	private final SmartConversionHandler smartConversionHandler;
	private final Set<GenericEntity> evaluationExcludes;
	private final Set<Operand> unmappedSourceRelatedOperands;
	private final OrderAndPagingManager orderAndPagingManager;
	private final CharStringsResolver charStringsResolver;

	private final MultiMap<Source, ValueComparison> allCollectionConditions;

	private final SmartQueryFunctionManager functionManager;

	private int nextFreeTuplePosition;
	private boolean conditionIsFalse;

	public SmartQueryPlannerContext(SelectQuery query, ModelExpert modelExpert, Map<From, IncrementalAccess> fromMapping,
			Map<EntityType<? extends QueryFunction>, QueryFunctionExpert<?>> functionExperts,
			Map<EntityType<? extends SmartConversion>, SmartConversionExpert<?>> conversionExperts) {

		this.query = query;
		this.modelExpert = modelExpert;
		this.fromMapping = fromMapping;
		this.smartDenotation = modelExpert.smartDenotation;

		this.refConverter = new ReferenceConverter(this, modelExpert.accessMapping);

		this.evaluationExcludes = query.getEvaluationExcludes();
		this.unmappedSourceRelatedOperands= newSet();
		this.operandSourceResolver = new OperandSourceResolver(this);
		this.planStructure = new QueryPlanStructure(this);
		this.conditionConverter = new SmartConditionConverter(this);
		this.collectionConditionExpert = new CollectionConditionExpert(this);
		this.operandConverter = new SmartOperandConverter(this);
		this.smartConversionHandler = new SmartConversionHandler(this, conversionExperts);
		this.orderAndPagingManager = new OrderAndPagingManager(query);
		this.charStringsResolver = new CharStringsResolver();
		this.functionManager = new SmartQueryFunctionManager(this, functionExperts);

		PlanStructureInitializer.initialize(query, functionManager, this);

		this.conjunctionOperands = extractConjunctionOperands(extractCondition(query));
		this.allCollectionConditions = collectionConditionExpert.findCollectionConditions(conjunctionOperands);

		OuterJoinAdjuster.run(conjunctionOperands);
	}

	public IncrementalAccess getMapedAccess(From from) {
		return fromMapping.get(from);
	}

	public IncrementalAccess getSmartDenotation() {
		return smartDenotation;
	}

	public boolean needsDistinct() {
		return query.getDistinct();
	}

	private Condition extractCondition(SelectQuery query) {
		Restriction r = query.getRestriction();
		Condition c = r != null ? r.getCondition() : null;

		return c != null ? new SmartConditionNormalizer(this).normalize(c) : null;
	}

	private List<Condition> extractConjunctionOperands(Condition condition) {
		if (condition instanceof ConstantCondition) {
			this.conditionIsFalse = condition == ConstantCondition.FALSE;

			return emptyList();
		}

		if (condition == null)
			return emptyList();

		if (condition.conditionType() == ConditionType.conjunction)
			return newList(((Conjunction) condition).getOperands());
		else
			return asList(condition);
	}

	public boolean conditionIsFalse() {
		return conditionIsFalse;
	}

	public List<Condition> conjunctionOperands() {
		return conjunctionOperands;
	}

	public List<Source> getSourcesForOperand(Object operand) {
		return operandSourceResolver.getSourcesForOperand(operand);
	}

	public List<String> getCharStrings(SourceNodeGroup group) {
		return charStringsResolver.getCharStrings(group);
	}

	/**
	 * Converts a smart query condition into a query plan condition. This is done for all the conditions which cannot be
	 * delegated to the underlying accesses, in the last step of building the "scalar" query plan (i.e. inside the
	 * {@link ScalarPlanBuilder}).
	 */
	public com.braintribe.model.queryplan.filter.Condition convertCondition(Condition condition) {
		return conditionConverter.convert(condition);
	}

	/**
	 * Converts an operand from the smart query to a corresponding smart value. This is done for final projection (for
	 * query result) and as part of converting the entire {@link Condition}, using {@link #convertCondition(Condition)}.
	 */
	@Override
	public Value convertOperand(Object operand) {
		return operandConverter.convert(operand);
	}

	public Predicate<Object> evalExclusionCheck() {
		return this::isEvaluationExclude;
	}
	
	public boolean isEvaluationExclude(Object value) {
		return evaluationExcludes != null && evaluationExcludes.contains(value);
	}

	public boolean isUnmappedSourceRelatedOperand(Operand operand) {
		if (unmappedSourceRelatedOperands.contains(operand))
			return true;
		
		Source source = extractSource(operand);
		
		if (source != null && unmappedSourceRelatedOperands.contains(source)) {
			unmappedSourceRelatedOperands.add(operand);
			return true;
		}
		
		return false;
	}
	
	private Source extractSource(Operand operand) {
		if (operand instanceof PropertyOperand)
			return ((PropertyOperand) operand).getSource();

		else if (operand instanceof JoinFunction)
			return  ((JoinFunction) operand).getJoin();

		else if (operand instanceof AggregateFunction)
			throw new UnsupportedOperationException("Aggregate functions are not supported in SmartAccesss: " + operand.entityType().getShortName());

		else if (operand instanceof QueryFunction) {
			if (operand instanceof ResolveId)
				return ((ResolveId) operand).getSource();
			else if (operand instanceof ResolveDelegateProperty)
				return ((ResolveDelegateProperty) operand).getSource();
			else
				return null;

		} else if (operand instanceof Source)
			return (Source) operand;
		else
			throw new SmartQueryPlannerException("Unsupported operand: " + operand + " of type: " + operand.getClass().getName());
	}

	public boolean notifyUnmappedJoin(Join join) {
		return unmappedSourceRelatedOperands.add(join);
	}
	
	public ModelExpert modelExpert() {
		return modelExpert;
	}

	public QueryPlanStructure planStructure() {
		return planStructure;
	}

	public OrderAndPagingManager orderAndPaging() {
		return orderAndPagingManager;
	}

	public List<Operand> listOperands(QueryFunction queryFunction) {
		return functionManager.listOperands(queryFunction);
	}

	public Map<Object, Value> getFunctionOperandMappings(QueryFunction operand) {
		return functionManager.getFunctionOperandMappings(operand);
	}

	public int allocateTuplePosition() {
		return nextFreeTuplePosition++;
	}

	public int getNumberOfAllocatedTupleTuplePositions() {
		return nextFreeTuplePosition;
	}

	public PersistentEntityReference localizeReference(PersistentEntityReference smartReference) {
		return refConverter.convertToDelegate(smartReference);
	}

	public MultiMap<Source, ValueComparison> getAllCollectionConditions() {
		return allCollectionConditions;
	}

	public MultiMap<Source, ValueComparison> findCollectionConditions(Collection<Condition> conditions) {
		return collectionConditionExpert.findCollectionConditions(conditions);
	}

	public SmartConversion findConversion(Object operand) {
		return smartConversionHandler.findConversion(operand);
	}

	public Object convertToDelegateValue(Object smartValue, SmartConversion conversion) {
		return conversion == null ? smartValue : smartConversionHandler.convertToDelegateValue(smartValue, conversion);
	}

}
