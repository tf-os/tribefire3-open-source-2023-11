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
package com.braintribe.model.query.smart.processing.eval.set;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Collection;
import java.util.List;

import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.smartquery.eval.api.SmartQueryEvaluationContext;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;
import com.braintribe.model.smartqueryplan.set.DelegateQuerySet;
import com.braintribe.model.smartqueryplan.set.OperandRestriction;

class DelegateQuerySetAdapter {

	private final SmartQueryEvaluationContext context;

	private DelegateQuerySet clonedDqs;
	private Disjunction clonedMainJoinDisjunction;
	private List<OperandRestriction> clonedJoinRestrictions;

	public static DelegateQuerySet adaptQuerySet(DelegateQueryJoin dqj, Collection<Tuple> mTuples, SmartQueryEvaluationContext context) {
		return new DelegateQuerySetAdapter(dqj, context).adapt(mTuples);
	}

	private DelegateQuerySetAdapter(DelegateQueryJoin originalDqj, SmartQueryEvaluationContext context) {
		this.context = context;

		clonePartsToBeChanged(originalDqj);
	}

	// TODO find a good way to deal with changing the original query - this makes changes to given DQJ
	private void clonePartsToBeChanged(DelegateQueryJoin dqj) {
		// BaseType baseType = GMF.getTypeReflection().getBaseType();

		// StandardCloningContext cc = new StandardCloningContext();
		// clonedDqs = (DelegateQuerySet) baseType.clone(cc, dqj.getQuerySet(), StrategyOnCriterionMatch.reference);
		// clonedMainJoinDisjunction = (Disjunction) baseType.clone(cc, dqj.getJoinDisjunction(), StrategyOnCriterionMatch.reference);
		// clonedJoinRestrictions = (List<OperandRestriction>) baseType.clone(cc, dqj.getJoinRestrictions(),
		// StrategyOnCriterionMatch.reference);
		// counter += cc.getAssociatedObjects().size();

		// clonedDqs.setDelegateAccess(dqj.getQuerySet().getDelegateAccess());

		clonedDqs = dqj.getQuerySet();
		clonedMainJoinDisjunction = dqj.getJoinDisjunction();
		clonedJoinRestrictions = dqj.getJoinRestrictions();
	}

	private DelegateQuerySet adapt(Collection<Tuple> mTuples) {
		List<Condition> adaptedJoinDisjunctionOperands = newList();

		for (Tuple materializedTuple: mTuples) {
			List<Condition> correlationsPerTuple = newList();

			for (OperandRestriction restriction: clonedJoinRestrictions) {
				Object materializedValue = context.resolveValue(materializedTuple, restriction.getMaterializedCorrelationValue());

				if (materializedValue == null) {
					correlationsPerTuple = null;
					break;
				}

				ValueComparison valueComparison = ValueComparison.T.create();

				valueComparison.setLeftOperand(restriction.getQueryOperand());
				valueComparison.setOperator(Operator.equal);
				valueComparison.setRightOperand(materializedValue);

				correlationsPerTuple.add(valueComparison);
			}

			if (correlationsPerTuple == null) {
				continue;
			}

			Conjunction adaptedJoinConjuntions = Conjunction.T.create();
			adaptedJoinConjuntions.setOperands(correlationsPerTuple);

			adaptedJoinDisjunctionOperands.add(adaptedJoinConjuntions);
		}

		if (adaptedJoinDisjunctionOperands.isEmpty()) {
			return null;
		}
		
		clonedMainJoinDisjunction.setOperands(adaptedJoinDisjunctionOperands);

		return clonedDqs;
	}

}
