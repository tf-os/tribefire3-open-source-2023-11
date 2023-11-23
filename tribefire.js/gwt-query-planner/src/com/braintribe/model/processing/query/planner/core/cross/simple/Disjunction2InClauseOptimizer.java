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
package com.braintribe.model.processing.query.planner.core.cross.simple;

import static com.braintribe.model.processing.query.planner.core.cross.simple.ConditionAnalysisTools.getIfFrom;
import static com.braintribe.model.processing.query.planner.core.cross.simple.ConditionAnalysisTools.getIfPropertyOperand;
import static com.braintribe.model.processing.query.planner.core.cross.simple.ConditionAnalysisTools.getIfStaticValue;
import static com.braintribe.model.processing.query.planner.core.cross.simple.ConditionAnalysisTools.isEqualityOperator;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.model.processing.query.eval.api.repo.IndexInfo;
import com.braintribe.model.processing.query.planner.RuntimeQueryPlannerException;
import com.braintribe.model.processing.query.planner.context.QueryPlannerContext;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.ConditionType;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.query.conditions.ValueComparison;

/**
 * Optimizes a special case of a disjunction. This "optimizer" receives a set of conditions which are in fact operands of a {@link Conjunction} and
 * scans all of them which are in fact {@link Disjunction}s. The algorithm is looking for conjunctions of this type: <code>
 * (p.id=3 and c.id=33) or (p.id=4 and c.id=44) or (p.id=5 and c.id=55)
 * </code>
 * 
 * It tries to identify all the possible index-accesses to data, and if there is some indexed-property being used in every single operand of the
 * disjunction, the condition would be transformed using the IN clause: <code>
 * (p.id in (3, 4, 5)) AND (c.id in (33, 44, 55)) AND ((p.id=3 and c.id=33) or (p.id=4 and c.id=44) or (p.id=5 and c.id=55))
 * </code> (Note that we have to repeat the original condition, except for the most simple case.)
 * 
 * In the case about then, we would add 2 new "IN" conditions to the "conjunctedConditions" Set. If out disjunction consisted of value comparisons
 * only, something like: {@code p.id = 1 OR p.id = 2 OR p.id = 3} then we would completely replace this disjunction with a single IN clause condition.
 * 
 * @see #adaptConjunctedConditions(Disjunction)
 */
class Disjunction2InClauseOptimizer {

	private final Set<From> froms;
	private final Set<Condition> conjunctedConditions;
	private final Set<Disjunction> disjunctions;
	private final QueryPlannerContext context;

	/**
	 * Use case is a string of form "${number}:${indexName}" (e.g. "0:com.bt.Person#indexedName") which identifies the [From, Index] pair. This is
	 * here just to distinguish when referencing the same index for a different From, the initial number will be different.
	 */
	private final Map<String, Set<Object>> staticValuesForUseCase = newMap();
	private final Map<From, Integer> fromCounter = newMap();
	private final Map<String, Operand> operandFor = newMap();
	private final List<Condition> newConditions = newList();

	private boolean disjunctionOnlyHasValueComparisons;

	private PropertyOperand propertyOperand;
	private From from;
	private List<Object> staticValues;

	/* package */ static void run(Set<From> froms, Set<Condition> conjunctedConditions, QueryPlannerContext context) {
		new Disjunction2InClauseOptimizer(froms, conjunctedConditions, context).run();
	}

	private Disjunction2InClauseOptimizer(Set<From> froms, Set<Condition> conjunctedConditions, QueryPlannerContext context) {
		this.froms = froms;
		this.conjunctedConditions = conjunctedConditions;
		this.context = context;

		this.disjunctions = filterDisjunctions();
	}

	private Set<Disjunction> filterDisjunctions() {
		Set<Disjunction> result = newSet();

		for (Condition c : conjunctedConditions)
			if (c.conditionType() == ConditionType.disjunction)
				result.add((Disjunction) c);

		return result;
	}

	private void run() {
		for (Disjunction disjunction : disjunctions)
			if (tryToOptimize(disjunction))
				adaptConjunctedConditions(disjunction);
	}

	private void adaptConjunctedConditions(Disjunction disjunction) {
		conjunctedConditions.addAll(newConditions);
		if (disjunctionOnlyHasValueComparisons)
			conjunctedConditions.remove(disjunction);
	}

	private boolean tryToOptimize(Disjunction disjunction) {
		staticValuesForUseCase.clear();
		operandFor.clear();
		newConditions.clear();
		fromCounter.clear();
		disjunctionOnlyHasValueComparisons = true;

		for (Condition c : disjunction.getOperands()) {
			if (!handleDisjunctionOperand(c))
				return false;
		}

		// create new condition
		for (Entry<String, Set<Object>> e : staticValuesForUseCase.entrySet()) {
			Operand propertyOrFrom = operandFor.get(e.getKey());
			Object staticValue = e.getValue();

			ValueComparison newCondition = ValueComparison.T.create();
			newCondition.setLeftOperand(propertyOrFrom);
			newCondition.setRightOperand(staticValue);
			newCondition.setOperator(Operator.in);

			newConditions.add(newCondition);
		}

		return true;
	}

	private boolean handleDisjunctionOperand(Condition c) {
		switch (c.conditionType()) {
			case conjunction:
				disjunctionOnlyHasValueComparisons = false;
				return handleDisjunctionOperands(((Conjunction) c).getOperands());
			case disjunction:
				throw new RuntimeQueryPlannerException("Disjunction not expected as an operand of disjunction.");
			case fulltextComparison:
			case negation:
				return false;
			case valueComparison:
				return handleDisjunctionOperands(Arrays.asList(c));
		}

		return false;
	}

	private boolean handleDisjunctionOperands(List<Condition> operands) {
		List<String> activeUseCases = newList();

		boolean first = staticValuesForUseCase.isEmpty();

		for (Condition c : operands) {
			if (c.conditionType() != ConditionType.valueComparison)
				continue;

			ValueComparison vc = (ValueComparison) c;
			if (!isEqualityOperator(vc) || !loadOperandAndValue(vc))
				continue;

			if (from != null) {
				if (!isFromOk(from))
					continue;

				String useCase = toUseCase(from);
				activeUseCases.add(useCase);
				markUseCase(useCase, from, first);

			} else {
				From from = getIfFrom(propertyOperand.getSource());
				if (!isFromOk(from))
					continue;

				IndexInfo indexInfo = context.getIndexInfo(from, propertyOperand.getPropertyName());
				if (indexInfo == null)
					continue;

				String useCase = toUseCase(from, indexInfo);
				activeUseCases.add(useCase);
				markUseCase(useCase, propertyOperand, first);
			}
		}

		staticValuesForUseCase.keySet().retainAll(activeUseCases);

		return !staticValuesForUseCase.isEmpty();
	}

	private void markUseCase(String useCase, Operand propertyOrFrom, boolean first) {
		addValuesToSet(staticValuesForUseCase, useCase, staticValues, first);
		if (!operandFor.containsKey(useCase))
			operandFor.put(useCase, propertyOrFrom);
	}

	private boolean isFromOk(From from) {
		return froms != null && froms.contains(from);
	}

	private String toUseCase(From from) {
		return "" + fromNumber(from);
	}

	private String toUseCase(From from, IndexInfo indexInfo) {
		return fromNumber(from) + ":" + indexInfo.getIndexId();
	}

	private Integer fromNumber(From from) {
		Integer i = fromCounter.get(from);
		if (i == null) {
			i = fromCounter.size();
			fromCounter.put(from, i);
		}
		return i;
	}

	private boolean loadOperandAndValue(ValueComparison vc) {
		propertyOperand = getIfPropertyOperand(vc.getLeftOperand(), context);
		if (propertyOperand == null) {
			from = ConditionAnalysisTools.getIfFrom(vc.getLeftOperand(), context);
			if (from == null)
				return false;

		} else if (propertyOperand.getPropertyName() == null) {
			from = getIfFrom(propertyOperand.getSource());
			propertyOperand = null;
			if (from == null)
				return false;
		}

		// being here means propertyOperand or from is not null
		staticValues = toStaticValuesIfPossible(vc.getRightOperand());
		return staticValues != null;
	}

	private List<Object> toStaticValuesIfPossible(Object operand) {
		Object result = getIfStaticValue(operand, context);
		if (result == null)
			return null;

		if (result instanceof List)
			return (List<Object>) result;

		if (result instanceof Set)
			return newList((Set<?>) result);

		return Collections.singletonList(result);
	}

	private static <K, E> void addValuesToSet(Map<K, Set<E>> map, K key, List<E> values, boolean createIfNotExists) {
		Set<E> set = map.get(key);

		if (set == null) {
			if (!createIfNotExists)
				return;

			set = newSet();
			map.put(key, set);
		}

		set.addAll(values);
	}

}
