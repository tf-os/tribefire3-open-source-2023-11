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

import static com.braintribe.utils.lcd.CollectionTools2.computeIfAbsent;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.processing.query.planner.RuntimeQueryPlannerException;
import com.braintribe.model.query.From;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.AbstractJunction;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.FulltextComparison;
import com.braintribe.model.query.conditions.Negation;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.query.functions.JoinFunction;
import com.braintribe.model.query.functions.Localize;
import com.braintribe.model.query.functions.PropertyFunction;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.query.functions.aggregate.AggregateFunction;

/**
 * @see #resolveFromsFor(Condition)
 * 
 * @author peter.gazdik
 */
@SuppressWarnings("deprecation")
class ConditionSourceResolver {

	private final QueryPlannerContext context;

	private final Map<Condition, Set<From>> fromsForCondition = newMap();

	public ConditionSourceResolver(QueryPlannerContext context) {
		this.context = context;
	}

	public Set<From> resolveFromsFor(Condition condition) {
		return computeIfAbsent(fromsForCondition, condition, this::computeFromsFor);
	}

	private Set<From> computeFromsFor(Condition condition) {
		switch (condition.conditionType()) {
			case conjunction:
			case disjunction:
				return fromsFor((AbstractJunction) condition);
			case fulltextComparison:
				return fromsFor((FulltextComparison) condition);
			case negation:
				return resolveFromsFor(((Negation) condition).getOperand());
			case valueComparison:
				return fromsFor((ValueComparison) condition);
		}

		throw new RuntimeQueryPlannerException("Unsupported condition: " + condition + " of type: " + condition.conditionType());
	}

	private Set<From> fromsFor(AbstractJunction condition) {
		Set<From> result = newSet();

		for (Condition operand : condition.getOperands())
			result.addAll(resolveFromsFor(operand));

		return result;
	}

	private Set<From> fromsFor(FulltextComparison condition) {
		From sourceRoot = getSourceRoot(condition.getSource());
		return Collections.singleton(sourceRoot);
	}

	private Set<From> fromsFor(ValueComparison condition) {
		Set<From> result = newSet();

		addFromForOperand(result, condition.getLeftOperand());
		addFromForOperand(result, condition.getRightOperand());

		return result;
	}

	/** I did some tests and there does not seem to be a reason to cache the results of this method (right now). */
	public Set<From> resolveFromsForOperand(Object operand) {
		Set<From> result = newSet();
		addFromForOperand(result, operand);

		return result;
	}

	private void addFromForOperand(Collection<From> result, Object operand) {
		Object source = resolveSourceFor(operand);
		if (source == null)
			return;

		if (source instanceof Source)
			result.add(getSourceRoot((Source) source));
		else
			result.addAll((List<From>) source);
	}

	private Object resolveSourceFor(Object operand) {
		if (context.isStaticValue(operand))
			return null;

		if (operand instanceof PropertyOperand)
			return ((PropertyOperand) operand).getSource();

		if (operand instanceof JoinFunction)
			return ((JoinFunction) operand).getJoin();

		if (operand instanceof Localize)
			return resolveSourceFor(((Localize) operand).getLocalizedStringOperand());

		if (operand instanceof PropertyFunction)
			return ((PropertyFunction) operand).getPropertyOperand().getSource();

		if (operand instanceof AggregateFunction)
			return resolveSourceFor(((AggregateFunction) operand).getOperand());

		if (operand instanceof QueryFunction)
			return resolveSourcesForFunction((QueryFunction) operand);

		if (operand instanceof Source)
			return operand;

		throw new RuntimeQueryPlannerException("Unsupported operand: " + operand + " of type: " + operand.getClass().getName());
	}

	private List<From> resolveSourcesForFunction(QueryFunction function) {
		List<From> result = newList();

		for (Object operand : context.listOperands(function))
			addFromForOperand(result, operand);

		return result;
	}

	private From getSourceRoot(Source source) {
		return context.sourceManager().getSourceRoot(source);
	}

}
