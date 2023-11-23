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
package com.braintribe.model.processing.query.smart.eval.set.base;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Arrays;
import java.util.List;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.processing.query.eval.set.base.TupleSetBuilder;
import com.braintribe.model.processing.query.planner.builder.ValueBuilder;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.ConditionType;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.query.smart.processing.SmartQueryEvaluatorRuntimeException;
import com.braintribe.model.queryplan.set.SortCriterion;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.model.smartqueryplan.ScalarMapping;
import com.braintribe.model.smartqueryplan.set.DelegateQueryAsIs;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;
import com.braintribe.model.smartqueryplan.set.DelegateQuerySet;
import com.braintribe.model.smartqueryplan.set.OperandRestriction;
import com.braintribe.model.smartqueryplan.set.OrderedConcatenation;

public class SmartTupleSetBuilder extends TupleSetBuilder {

	public DelegateQueryAsIs delegateQueryAsIs(IncrementalAccess delegateAccess, SelectQuery delegateQuery) {
		DelegateQueryAsIs result = DelegateQueryAsIs.T.create();
		result.setDelegateAccess(delegateAccess);
		result.setDelegateQuery(delegateQuery);

		index = delegateQuery.getSelections().size();

		return result;
	}

	public DelegateQuerySet delegateQuerySet(IncrementalAccess delegateAccess, SelectQuery delegateQuery, List<ScalarMapping> scalarMappings) {
		return delegateQuerySet(delegateAccess, delegateQuery, scalarMappings, null);
	}

	public DelegateQuerySet delegateQuerySet(IncrementalAccess delegateAccess, SelectQuery delegateQuery, List<ScalarMapping> scalarMappings,
			Integer batchSize) {

		DelegateQuerySet result = DelegateQuerySet.T.create();
		result.setDelegateAccess(delegateAccess);
		result.setDelegateQuery(delegateQuery);
		result.setScalarMappings(scalarMappings);
		result.setBatchSize(batchSize);

		return result;
	}

	public DelegateQueryJoin delegateQueryJoin(TupleSet mSet, DelegateQuerySet qSet, Integer... correlationPositions) {
		DelegateQueryJoin dqj = DelegateQueryJoin.T.create();

		dqj.setMaterializedSet(mSet);
		dqj.setQuerySet(qSet);
		dqj.setJoinDisjunction(retrieveMainJoinDisjunction(retrieveMainConjunction(qSet.getDelegateQuery())));
		dqj.setJoinRestrictions(retrieveJoinRestrictions(dqj.getJoinDisjunction(), correlationPositions));

		return dqj;
	}

	private Conjunction retrieveMainConjunction(SelectQuery query) throws SmartQueryEvaluatorRuntimeException {
		Condition condition = query.getRestriction().getCondition();

		if ((condition == null) || (condition.conditionType() != ConditionType.conjunction))
			throw new SmartQueryEvaluatorRuntimeException("malformed delegateQuery, expected main condition to be of Conjunction type");

		return (Conjunction) condition;
	}

	private Disjunction retrieveMainJoinDisjunction(Conjunction conjunction) throws SmartQueryEvaluatorRuntimeException {
		List<Condition> conjunctionOperands = conjunction.getOperands();
		Condition lastCondition = conjunctionOperands.get(conjunctionOperands.size() - 1);

		if (lastCondition.conditionType() != ConditionType.disjunction)
			throw new SmartQueryEvaluatorRuntimeException("malformed delegateQuery, expected last condition of main conjunction to be a Disjunction");

		return (Disjunction) lastCondition;
	}

	public List<OperandRestriction> retrieveJoinRestrictions(Disjunction dqjJoinDisjunction, Integer... materializedCorrelationPositions)
			throws SmartQueryEvaluatorRuntimeException {

		if (dqjJoinDisjunction.getOperands().get(0).conditionType() != ConditionType.conjunction)
			throw new SmartQueryEvaluatorRuntimeException(
					"the size of materializedCorrelationPositions must equal to number of templatedJoinRestrictions : ");

		List<OperandRestriction> joinRestrictions = newList();

		Conjunction templatedJoinRestrictions = (Conjunction) dqjJoinDisjunction.getOperands().get(0);
		List<Condition> joinConditions = templatedJoinRestrictions.getOperands();

		for (int i = 0; i < joinConditions.size(); i++) {
			ValueComparison templatedValueComparison = (ValueComparison) joinConditions.get(i);

			OperandRestriction restriction = OperandRestriction.T.create();
			restriction.setQueryOperand((PropertyOperand) (templatedValueComparison).getLeftOperand());
			restriction.setMaterializedCorrelationValue(ValueBuilder.tupleComponent(materializedCorrelationPositions[i]));
			joinRestrictions.add(restriction);
		}

		return joinRestrictions;
	}

	public OrderedConcatenation orderedConcatenation(TupleSet firstOperand, DelegateQuerySet secondOperand, int tupleSize,
			SortCriterion... sortCriteria) {

		OrderedConcatenation result = OrderedConcatenation.T.create();
		result.setFirstOperand(firstOperand);
		result.setSecondOperand(secondOperand);
		result.setSortCriteria(Arrays.asList(sortCriteria));
		result.setTupleSize(tupleSize);

		return result;
	}

	public ScalarMapping scalarMapping(int tupleIndex) {
		return scalarMapping(TupleSetBuilder.tupleComponent(tupleIndex));
	}

	public ScalarMapping scalarMapping(int tupleIndex, int index) {
		return scalarMapping(TupleSetBuilder.tupleComponent(tupleIndex), index);
	}

	public ScalarMapping scalarMapping(Value value) {
		return scalarMapping(value, null);
	}

	public ScalarMapping scalarMapping(Value value, Integer componentIndex) {
		ScalarMapping mapping = ScalarMapping.T.create();
		mapping.setSourceValue(value);
		mapping.setTupleComponentIndex(componentIndex != null ? componentIndex : index++);

		return mapping;
	}

}
