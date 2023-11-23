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
package com.braintribe.model.processing.query.planner.core.property;

import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.CollectionTools2.union;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.braintribe.model.processing.query.planner.RuntimeQueryPlannerException;
import com.braintribe.model.processing.query.planner.builder.TupleSetBuilder;
import com.braintribe.model.processing.query.planner.condition.JoinPropertyType;
import com.braintribe.model.processing.query.planner.context.QueryPlannerContext;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.Source;
import com.braintribe.model.queryplan.set.CartesianProduct;
import com.braintribe.model.queryplan.set.CombinedSet;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.MergeLookupJoin;
import com.braintribe.model.queryplan.set.MergeRangeJoin;
import com.braintribe.model.queryplan.set.OperandSet;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.set.join.IndexLookupJoin;
import com.braintribe.model.queryplan.set.join.IndexRangeJoin;
import com.braintribe.utils.lcd.CollectionTools2;

/**
 * @author peter.gazdik
 */
public class PropertyJoinAdder {

	private final QueryPlannerContext context;
	private final TupleSet tupleSet;
	private final JoinFinder joinFinder;

	public PropertyJoinAdder(QueryPlannerContext context, TupleSet tupleSet) {
		this.context = context;
		this.tupleSet = tupleSet;

		this.joinFinder = new JoinFinder(context);
	}

	public TupleSet addJoins() {
		// add all the joins which are necessary due to existing conditions
		Set<Join> addedJoins = addJoins(tupleSet);

		// add all the other joins (not added in the previous step)
		return ensureJoins(tupleSet, addedJoins, findMandatoryJoins());
	}

	/**
	 * Finds all the joins which are needed even if not used for condition (those for selection and those for FROM which
	 * are collections, because they influence the number of tuples returned)
	 */
	private Set<Join> findMandatoryJoins() {
		// TODO add ordering Joins
		Set<Join> result = newSet();
		result.addAll(context.sourceManager().findAllSelectionJoins());

		for (Join join: context.sourceManager().findAllJoins()) {
			JoinPropertyType type = context.sourceManager().joinPropertyType(join);
			if (type == JoinPropertyType.list || type == JoinPropertyType.set || type == JoinPropertyType.map) {
				result.add(join);
			}
		}

		return result;
	}

	private Set<Join> addJoins(TupleSet tupleSet) {
		switch (tupleSet.tupleSetType()) {
			case cartesianProduct:
				return addJoins((CartesianProduct) tupleSet);

			case concatenation:
			case intersection:
			case union:
				return addJoins((CombinedSet) tupleSet);

			case filteredSet:
				return addJoins((FilteredSet) tupleSet);

			case indexLookupJoin:
				return addJoins((IndexLookupJoin) tupleSet);

			case indexRangeJoin:
				return addJoins((IndexRangeJoin) tupleSet);

			case mergeLookupJoin:
				return addJoins((MergeLookupJoin) tupleSet);

			case mergeRangeJoin:
				return addJoins((MergeRangeJoin) tupleSet);

			case indexRange:
			case indexOrderedSet:
			case indexSubSet:
			case sourceSet:
			case querySourceSet:
			case staticSet:
				return Collections.emptySet();

			case aggregatingProjection:
			case entityJoin:
			case extension:
			case listJoin:
			case mapJoin:
			case orderedSet:
			case pagination:
			case projection:
			case setJoin:
				throw new RuntimeQueryPlannerException("Unexpected tuple-set: " + tupleSet + " of type: " + tupleSet.tupleSetType());
			default:
				throw new RuntimeQueryPlannerException("Unsupported tuple-set: " + tupleSet + " of type: " + tupleSet.tupleSetType());
		}
	}

	private Set<Join> addJoins(CartesianProduct tupleSet) {
		Set<Join> result = newSet();

		for (TupleSet operand: tupleSet.getOperands()) {
			result.addAll(addJoins(operand));
		}

		return result;
	}

	private Set<Join> addJoins(CombinedSet tupleSet) {
		TupleSet firstOperand = tupleSet.getFirstOperand();
		TupleSet secondOperand = tupleSet.getSecondOperand();

		Set<Join> firstResult = addJoins(firstOperand);
		Set<Join> secondResult = addJoins(secondOperand);

		firstOperand = ensureJoins(firstOperand, firstResult, secondResult);
		secondOperand = ensureJoins(secondOperand, secondResult, firstResult);

		tupleSet.setFirstOperand(firstOperand);
		tupleSet.setSecondOperand(secondOperand);

		return union(firstResult, secondResult);
	}

	private Set<Join> addJoins(FilteredSet tupleSet) {
		// TODO <LOW PRIO> POTENTIAL IMPROVEMENT special handling for case when condition is a conjunction
		// To split the filter so we first do filter, then join, then filter, then join...

		Set<Join> joinsToHave = joinFinder.findRequiredJoins(tupleSet.getFilter());
		return addJoins(tupleSet, joinsToHave);
	}

	private Set<Join> addJoins(IndexLookupJoin tupleSet) {
		Set<Join> joinsToHave = joinFinder.findRequiredJoins(tupleSet.getLookupValue());
		return addJoins(tupleSet, joinsToHave);
	}

	private Set<Join> addJoins(IndexRangeJoin tupleSet) {
		Set<Join> joinsToHave = joinFinder.findRequiredJoins(tupleSet.getRange());
		return addJoins(tupleSet, joinsToHave);
	}

	private Set<Join> addJoins(MergeLookupJoin tupleSet) {
		Set<Join> joinsToHave = joinFinder.findRequiredJoins(tupleSet.getValue());
		Set<Join> addedJoins = addJoins(tupleSet, joinsToHave);

		Set<Join> otherJoinsToHave = joinFinder.findRequiredJoins(tupleSet.getOtherValue());
		Set<Join> otherAddedJoins = addOtherJoins(tupleSet, otherJoinsToHave);

		return union(addedJoins, otherAddedJoins);
	}

	private Set<Join> addJoins(MergeRangeJoin tupleSet) {
		Set<Join> joinsToHave = joinFinder.findRequiredJoins(tupleSet.getRange());
		Set<Join> addedJoins = addJoins(tupleSet, joinsToHave);

		Set<Join> indexJoinsToHave = joinFinder.findRequiredJoins(tupleSet.getIndex());
		Set<Join> indexAddedJoins = addJoins(tupleSet.getIndex(), indexJoinsToHave);

		return union(addedJoins, indexAddedJoins);
	}

	/**
	 * Adds <tt>joinsToHave</tt> to given <tt>tupleSet</tt>. First, it adds all needed joins to the operand of given
	 * <tt>tupleSet</tt>, which might be some of the <tt>joinsToHave</tt> and some other as well. Then it adds all the
	 * joins from <tt>joinsToHave</tt> which were not added yet (via {@link #ensureJoins(TupleSet, Set, Collection)}).
	 * It returns all the joins that were added, i.e. a super-set of <tt>joinsToHave</tt>.
	 */
	private Set<Join> addJoins(OperandSet tupleSet, Set<Join> joinsToHave) {
		TupleSet operand = tupleSet.getOperand();
		Set<Join> addedJoins = addJoins(operand);
		operand = ensureJoins(operand, addedJoins, joinsToHave);
		tupleSet.setOperand(operand);

		return union(addedJoins, joinsToHave);
	}

	private Set<Join> addOtherJoins(MergeLookupJoin tupleSet, Set<Join> joinsToHave) {
		TupleSet operand = tupleSet.getOtherOperand();
		Set<Join> addedJoins = addJoins(operand);
		operand = ensureJoins(operand, addedJoins, joinsToHave);
		tupleSet.setOtherOperand(operand);

		return union(addedJoins, joinsToHave);
	}

	/**
	 * For given {@link TupleSet}, adds all the joins in <tt>joinsToHave</tt> which are not already contained in the
	 * <tt>existingJoins</tt>.
	 */
	private TupleSet ensureJoins(TupleSet ts, Set<Join> existingJoins, Collection<Join> joinsToHave) {
		/* When ensuring the joins, we must make sure we consider all the joins that are already applied, but the
		 * "existingJoins" may only contain the "leaf-joins" for every chain. */
		existingJoins = joinClosure(existingJoins);

		Set<Join> missingJoins = CollectionTools2.substract(joinsToHave, existingJoins);

		for (Join leafJoin: PropertyJoinTools.leafJoins(missingJoins)) {
			List<Join> joinChain = PropertyJoinTools.relativeJoinChain(existingJoins, leafJoin);
			ts = applyJoinChain(ts, joinChain);
		}

		return ts;
	}

	/**
	 * For given set of {@link Join}s, returns a set containing these joins all the joins on the path that stars with
	 * such join and ends with the corresponding {@link From}
	 */
	private Set<Join> joinClosure(Set<Join> joins) {
		Set<Join> result = newSet();

		for (Join join: joins) {
			addJoinChain(join, result);
		}

		return result;
	}

	private void addJoinChain(Join join, Set<Join> result) {
		result.add(join);

		Source source = join.getSource();
		if (source instanceof Join) {
			addJoinChain((Join) source, result);
		}
	}

	private TupleSet applyJoinChain(TupleSet ts, List<Join> joinChain) {
		for (Join join: joinChain) {
			ts = applyJoin(ts, join);
		}

		return ts;
	}

	private TupleSet applyJoin(TupleSet ts, Join join) {
		JoinPropertyType joinPropertyType = context.sourceManager().joinPropertyType(join);

		switch (joinPropertyType) {
			case entity:
			case simple:
				return TupleSetBuilder.entityJoin(ts, join, context);
			case list:
				return TupleSetBuilder.listJoin(ts, join, context);
			case map:
				return TupleSetBuilder.mapJoin(ts, join, context);
			case set:
				return TupleSetBuilder.setJoin(ts, join, context);
		}

		throw new RuntimeQueryPlannerException("Unsupported JoinPropertyTyp: " + joinPropertyType);
	}

}
