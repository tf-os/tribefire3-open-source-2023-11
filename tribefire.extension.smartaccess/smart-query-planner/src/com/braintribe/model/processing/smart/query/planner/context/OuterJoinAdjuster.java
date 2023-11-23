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

import static com.braintribe.utils.lcd.CollectionTools2.acquireSet;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static java.util.Collections.emptySet;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.processing.query.planner.RuntimeQueryPlannerException;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.AbstractJunction;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.FulltextComparison;
import com.braintribe.model.query.conditions.ValueComparison;

/**
 * This class checks whether outer joins which might be present in the query are really needed. If we have conditions on our sources which
 * specify that given joined property must have a value (i.e. something like {@code where joindProperty = :entity}), we can remove the
 * "leftness" of the join - i.e. we turn a left join to an inner join, or outer join to a right join.
 * 
 * Also consider that property having a value implies this for the whole chain which lead to this property, so for query like this:
 * {@code select p from Person p where p.employerCompany.address.street = :value} every single join in this query is really an inner join.
 * 
 * So the rules for declaring a join <tt>J</tt> as directly non-nullable with respect to a condition based on type of <tt>C</tt> are
 * following:
 * <ul>
 * <li>Conjunction: iff J is non-nullable with respect to at least one operand of C</li>
 * <li>Disjunction: iff J is non-nullable with respect to at all operands of C</li>
 * <li>FulltextComparison: iff J is the source inside {@link FulltextComparison#getSource()}</li>
 * <li>ValueComparison: iff J or it's property is one of the operands and the other operands is a constant non-null value or a non-nullable
 * Join</li>
 * <li>Negation: never - the only operand of a negation possible (as the condition is normalized) is a {@link FulltextComparison} or a
 * {@link ValueComparison} with operator being one of {@link Operator#like}, {@link Operator#ilike}, {@link Operator#in} and
 * {@link Operator#contains}. In either case, the negative condition may is fulfilled by a <tt>null</tt> value.</li>
 * </ul>
 * 
 * And we say that a join <tt>J</tt> is non-nullable with respect to condition <tt>C</tt> iff the join itself, or any of the joins reachable
 * from it (alongside the chain defined by {@link Source#getJoins()} property) is directly non-nullable with respect to <tt>C</tt>.
 * 
 * So what this access does is, that it removes the "leftness" of a every join in the query which is non-nullable with respect to the
 * conjunction condition whose operands are given in {@link #run(List)} method.
 */
class OuterJoinAdjuster {

	private final List<Condition> conjunctionOperands;
	private final Map<Join, Set<Join>> joinDependency = newMap();

	private Set<Join> nonNullJoins = Collections.emptySet();

	public static void run(List<Condition> conjunctionOperands) {
		if (conjunctionOperands.isEmpty())
			return;

		new OuterJoinAdjuster(conjunctionOperands).run();
	}

	public OuterJoinAdjuster(List<Condition> conjunctionOperands) {
		this.conjunctionOperands = conjunctionOperands;
	}

	private void run() {
		findNonNullJoins();
		unleftifyNonNullJoins();
	}

	private void findNonNullJoins() {
		nonNullJoins = findNonNullJoins(conjunctionOperands, true);
		addNonNullJoinsBasedOnTwoOperandsComparisons();
	}

	private Set<Join> findNonNullJoinsHelper(Condition condition) {
		switch (condition.conditionType()) {
			case conjunction:
				return findNonNullJoins(junctionOperands(condition), true);
			case disjunction:
				return findNonNullJoins(junctionOperands(condition), false);
			case fulltextComparison:
				return findNonNullJoins((FulltextComparison) condition);
			case negation:
				return Collections.emptySet();
			case valueComparison:
				return findNonNullJoins((ValueComparison) condition);
		}

		throw new RuntimeQueryPlannerException("Unsupported condition: " + condition + " of type: " + condition.conditionType());
	}

	private List<Condition> junctionOperands(Condition condition) {
		return ((AbstractJunction) condition).getOperands();
	}

	private Set<Join> findNonNullJoins(List<Condition> junctionOperands, boolean isConjunction) {
		Set<Join> result = Collections.emptySet();

		for (Condition operand: junctionOperands) {
			Set<Join> operandJoins = findNonNullJoinsHelper(operand);

			if (result.isEmpty()) {
				result = operandJoins;

			} else {
				if (isConjunction)
					result.addAll(operandJoins);
				else
					result.retainAll(operandJoins);
			}

			if (!isConjunction && result.isEmpty())
				/* In case of disjunction, if at any point the current result is empty, we know the final result is empty set. */
				return result;
		}

		return result;
	}

	private Set<Join> findNonNullJoins(FulltextComparison condition) {
		Source source = condition.getSource();
		if (!(source instanceof Join))
			return Collections.emptySet();
		else
			return addNonNullJoinChainEndingWith((Join) source, newSet());
	}

	private Set<Join> findNonNullJoins(ValueComparison condition) {
		switch (condition.getOperator()) {
			case contains:
			case greater:
			case greaterOrEqual:
			case less:
			case lessOrEqual:
			case ilike:
			case in:
			case like:
				return findForComparisonAssymetricComparison(condition);

			case equal:
				return findForEqualityComparison(condition);

			case notEqual:
				return findForInequalityComparison(condition);
		}

		throw new RuntimeQueryPlannerException("Unsupported operator: " + condition.getOperator());
	}

	/**
	 * In the asymmetric comparison case the join operand is non-nullable iff other operand is non-<tt>null</tt>.
	 */
	private Set<Join> findForComparisonAssymetricComparison(ValueComparison condition) {
		Operand joinOperand = pickJoinOperand(condition.getLeftOperand(), condition.getRightOperand());
		if (joinOperand == null)
			return emptySet();

		Object other = pickOther(joinOperand, condition.getLeftOperand(), condition.getRightOperand());
		if (other == null || other instanceof Operand)
			return emptySet();

		return addNonNullJoinChainEndingWith(extractJoin(joinOperand), newSet());
	}

	/**
	 * In the equality comparison case the join operand is non-nullable iff other operand is non-<tt>null</tt>, or another non-nullable
	 * join. This second condition we cannot verify, so we remember this relationship for later.
	 */
	private Set<Join> findForEqualityComparison(ValueComparison condition) {
		Operand joinOperand = pickJoinOperand(condition.getLeftOperand(), condition.getRightOperand());
		if (joinOperand == null)
			return emptySet();

		Object other = pickOther(joinOperand, condition.getLeftOperand(), condition.getRightOperand());
		if (other == null)
			return emptySet();

		if (other instanceof Operand) {
			if (isJoinOperand(other)) {
				Join j1 = extractJoin(joinOperand);
				Join j2 = extractJoin(other);

				acquireSet(joinDependency, j1).add(j2);
				acquireSet(joinDependency, j2).add(j1);
			}

			return emptySet();
		}

		return addNonNullJoinChainEndingWith(extractJoin(joinOperand), newSet());
	}

	/**
	 * In the inequality case the join operand is non-nullable iff other operand is <tt>null</tt>.
	 */
	private static Set<Join> findForInequalityComparison(ValueComparison condition) {
		Operand joinOperand = pickJoinOperand(condition.getLeftOperand(), condition.getRightOperand());
		if (joinOperand == null)
			return emptySet();

		Object other = pickOther(joinOperand, condition.getLeftOperand(), condition.getRightOperand());
		if (other != null)
			return emptySet();

		return addNonNullJoinChainEndingWith(extractJoin(joinOperand), newSet());
	}

	private static Operand pickJoinOperand(Object left, Object right) {
		if (isJoinOperand(left))
			return (Operand) left;

		if (isJoinOperand(right))
			return (Operand) right;

		return null;
	}

	private static Join extractJoin(Object operand) {
		return (Join) (isJoin(operand) ? operand : (((PropertyOperand) operand).getSource()));
	}

	private static boolean isJoinOperand(Object operand) {
		return isJoin(operand) || (operand instanceof PropertyOperand && isJoin(((PropertyOperand) operand).getSource()));
	}

	private static boolean isJoin(Object operand) {
		return operand instanceof Join;
	}

	private static Object pickOther(Object one, Object o1, Object o2) {
		return one == o1 ? o2 : o1;
	}

	// ############################################################################
	// ## . . Adding non-null joins based on comparisons between two sources . . ##
	// ############################################################################

	private void addNonNullJoinsBasedOnTwoOperandsComparisons() {
		if (joinDependency.isEmpty())
			return;

		Set<Join> nextJoins = newSet(nonNullJoins);

		do {
			Set<Join> currentJoins = nextJoins;
			nextJoins = newSet();

			for (Join currentJoin: currentJoins) {
				Set<Join> newNonNullJoins = joinDependency.remove(currentJoin);
				if (newNonNullJoins == null)
					continue;

				for (Join newNonNullJoin: newNonNullJoins)
					addNonNullJoinChainEndingWith(newNonNullJoin, nextJoins);
			}

			nonNullJoins.addAll(nextJoins);

		} while (!joinDependency.isEmpty() && !nextJoins.isEmpty());
	}

	private static Set<Join> addNonNullJoinChainEndingWith(Join newNonNullJoin, Set<Join> result) {
		while (true) {
			if (!result.add(newNonNullJoin))
				return result;

			Source source = newNonNullJoin.getSource();
			if (!(source instanceof Join))
				return result;

			newNonNullJoin = (Join) source;
		}
	}

	// ############################################################################
	// ## . . . . . . . . . . . . . . Unleftifying . . . . . . . . . . . . . . . ##
	// ############################################################################

	private void unleftifyNonNullJoins() {
		for (Join join: nonNullJoins)
			join.setJoinType(unleftify(join.getJoinType()));
	}

	private static JoinType unleftify(JoinType joinType) {
		if (joinType == null)
			return null;

		switch (joinType) {
			case full:
				return JoinType.right;
			case left:
				return JoinType.inner;
			default:
				return joinType;
		}
	}

}
