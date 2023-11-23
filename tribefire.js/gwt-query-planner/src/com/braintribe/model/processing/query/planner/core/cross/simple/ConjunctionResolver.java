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

import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.concat;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static com.braintribe.utils.lcd.CollectionTools2.newLinkedSet;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.CollectionTools2.union;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.model.processing.query.planner.RuntimeQueryPlannerException;
import com.braintribe.model.processing.query.planner.builder.ConditionBuilder;
import com.braintribe.model.processing.query.planner.builder.TupleSetBuilder;
import com.braintribe.model.processing.query.planner.context.QueryPlannerContext;
import com.braintribe.model.processing.query.planner.core.cross.FromGroup;
import com.braintribe.model.query.From;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.QuerySourceSet;
import com.braintribe.model.queryplan.set.SourceSet;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.set.TupleSetType;
import com.braintribe.utils.lcd.CollectionTools2;

/**
 * 
 */
class ConjunctionResolver {

	CrossJoinOrderResolver resolver;
	QueryPlannerContext context;

	private GroupFromConditionAnalysis analysis;
	private StepDescription stepDescription;
	private FromGroup newGroup;

	Set<FromGroup> groups;
	Set<From> froms;
	Set<Condition> conditions;

	public ConjunctionResolver(CrossJoinOrderResolver resolver, QueryPlannerContext context) {
		this.resolver = resolver;
		this.context = context;
	}

	public FromGroup resolveFor(Set<FromGroup> groups, Set<From> froms, Collection<Condition> conditions) {
		this.groups = newLinkedSet(groups);
		this.froms = newLinkedSet(froms);
		this.conditions = newLinkedSet(conditions);

		preProcessConditions();

		return resolveHelper();
	}

	/** Doing some initial processing of the conditions to make the job of {@link ConditionAnalyzer} easier. */
	private void preProcessConditions() {
		handleSpecialDisjunctionCase();
	}

	/** Using {@link Disjunction2InClauseOptimizer} if it makes sense. */
	private void handleSpecialDisjunctionCase() {
		if (!froms.isEmpty() && !conditions.isEmpty())
			Disjunction2InClauseOptimizer.run(froms, conditions, context);
	}

	private FromGroup resolveHelper() {
		while (true) {
			if (conditions.isEmpty())
				return CrossJoinOrderResolver.cartesianProduct(groups, froms, context);

			analysis = new ConditionAnalyzer(this).analyze();

			doOneStep();

			conditions.removeAll(stepDescription.usedConditions);

			Set<Condition> newGroupConditions = findAllConditionsWithinFroms(conditions, newGroup.froms);
			applyConditionsIfEligible(newGroupConditions);

			froms.removeAll(newGroup.froms);
			groups.add(newGroup);

			conditions.removeAll(newGroupConditions);
		}
	}

	private void doOneStep() {
		if (doIndexJoinStep() || doIntroduceNewSourceStep() || doCombineExistingGroupsStep() || doDisjunctionStep())
			return;

		throw new RuntimeQueryPlannerException("No step found for any use-case.");
	}

	private boolean doIndexJoinStep() {
		// if there is a index join to existing group, use that
		stepDescription = stepForUsageType(ConditionApplicationType.valueJoin);
		if (stepDescription != null) {
			IndexLookupJoinStepDescription lookupJoinStepDescription = (IndexLookupJoinStepDescription) stepDescription;
			newGroup = lookupJoin(lookupJoinStepDescription);
			groups.remove(lookupJoinStepDescription.sourceGroup);
			return true;
		}

		stepDescription = stepForUsageType(ConditionApplicationType.rangeJoin);
		if (stepDescription != null) {
			IndexRangeJoinStepDescription rangeJoinStepDescription = (IndexRangeJoinStepDescription) stepDescription;
			newGroup = rangeJoin(rangeJoinStepDescription);
			groups.remove(rangeJoinStepDescription.sourceGroup);
			return true;
		}

		return false;
	}

	/**
	 * We have to go through those use-cases twice, because in a special case some "singleFrom" might have higher priority than retrieving via
	 * "index". Lets say we have this query: <code>
	 * 	select * from Person p, Company c where p.companyName = c.indexedName and c.indexedValue = 25  
	 * </code>
	 * 
	 * We want to first do the singleFrom for <tt>Person</tt>, and then an index join with <tt>Company</tt>. If we just picked the best "use-case", we
	 * would start with indexSubSet on company (using c.indexedValue = 25), and thus we would not be able to do the indexJoin. So we first try only
	 * sources which are not candidates for index joins, and if we do not have any, we then try all the sources.
	 */
	private boolean doIntroduceNewSourceStep() {
		return doIntroduceNewSourceStep(false) || doIntroduceNewSourceStep(true);
	}

	private boolean doIntroduceNewSourceStep(boolean includeJoinable) {
		stepDescription = stepForUsageType(ConditionApplicationType.staticFrom, includeJoinable);
		if (stepDescription != null) {
			newGroup = staticSourceSet((StaticSourceStepDescription) stepDescription);
			return true;
		}

		stepDescription = stepForUsageType(ConditionApplicationType.indexSubSet, includeJoinable);
		if (stepDescription != null) {
			newGroup = indexSubSet((IndexSubSetStepDescription) stepDescription);
			return true;
		}

		stepDescription = stepForUsageType(ConditionApplicationType.indexRange, includeJoinable);
		if (stepDescription != null) {
			newGroup = indexRange((IndexRangeStepDescription) stepDescription);
			return true;
		}

		stepDescription = stepForUsageType(ConditionApplicationType.indexOrderedSet, includeJoinable);
		if (stepDescription != null) {
			newGroup = indexOrderedSet((IndexOrderedSetStepDescription) stepDescription);
			return true;
		}

		stepDescription = stepForUsageType(ConditionApplicationType.singleFrom, includeJoinable);
		if (stepDescription != null) {
			newGroup = sourceSet((SingleSourceStepDescription) stepDescription);
			return true;
		}

		return false;
	}

	private StepDescription stepForUsageType(ConditionApplicationType type, boolean includeJoinable) {
		Set<StepDescription> steps = analysis.stepByApplicationType.get(type);

		if (CollectionTools2.isEmpty(steps))
			return null;

		if (includeJoinable)
			return first(steps);

		for (StepDescription step : steps) {
			SingleSourceStepDescription sourceStep = (SingleSourceStepDescription) step;
			if (!analysis.isJoinable(sourceStep.from))
				return step;
		}

		return null;
	}

	private boolean doCombineExistingGroupsStep() {
		stepDescription = stepForUsageType(ConditionApplicationType.mergeLookupJoin);
		if (stepDescription != null) {
			Condition condition = condition();
			Set<FromGroup> conditionGroups = analysis.groupsForCondition.get(condition);
			newGroup = mergeLookupJoin((MergeLookupJoinStepDescription) stepDescription);
			groups.removeAll(conditionGroups);
			return true;
		}

		stepDescription = stepForUsageType(ConditionApplicationType.mergeRangeJoin);
		if (stepDescription != null) {
			Condition condition = condition();
			Set<FromGroup> conditionGroups = analysis.groupsForCondition.get(condition);
			newGroup = mergeRangeJoin((MergeRangeJoinStepDescription) stepDescription);
			groups.removeAll(conditionGroups);
			return true;
		}

		stepDescription = stepForUsageType(ConditionApplicationType.crossProductFilter);
		if (stepDescription != null) {
			Condition condition = condition();
			Set<FromGroup> conditionGroups = analysis.groupsForCondition.get(condition);
			newGroup = filteredCartesianProduct(conditionGroups, condition);
			groups.removeAll(conditionGroups);
			return true;
		}

		stepDescription = stepForUsageType(ConditionApplicationType.crossProduct);
		if (stepDescription != null) {
			Set<FromGroup> conditionGroups = ((CrossProductStep) stepDescription).fromGroups;
			newGroup = cartesianProduct(conditionGroups);
			groups.removeAll(conditionGroups);
			return true;
		}

		return false;
	}

	private boolean doDisjunctionStep() {
		stepDescription = stepForUsageType(ConditionApplicationType.disjunction);
		if (stepDescription != null) {
			Condition disjunction = stepDescription.usedConditions.iterator().next();
			newGroup = resolver.resolveFor(groups, froms, disjunction);
			groups.removeAll(CollectionTools2.nullSafe(analysis.groupsForCondition.get(disjunction)));
			return true;
		}

		return false;
	}

	private StepDescription stepForUsageType(ConditionApplicationType type) {
		Set<StepDescription> steps = analysis.stepByApplicationType.get(type);

		return isEmpty(steps) ? null : first(steps);
	}

	private FromGroup mergeLookupJoin(MergeLookupJoinStepDescription step) {
		return new FromGroup( //
				TupleSetBuilder.mergeLookupJoin(step.sourceGroup.tupleSet, step.sourceOperand, step.otherGroup.tupleSet, step.otherOperand, context), //
				union(step.sourceGroup.froms, step.otherGroup.froms), //
				concat(step.sourceGroup.osds, step.otherGroup.osds));
	}

	private FromGroup mergeRangeJoin(MergeRangeJoinStepDescription step) {
		return new FromGroup(//
				TupleSetBuilder.mergeRangeJoin(step.sourceGroup.tupleSet, step.lowerBounds, step.upperBounds, step.otherGroup.tupleSet,
						step.otherOperand, context), //
				union(step.sourceGroup.froms, step.otherGroup.froms), //
				concat(step.sourceGroup.osds, step.otherGroup.osds));
	}

	public FromGroup filteredCartesianProduct(Set<FromGroup> groups, Condition condition) {
		return CrossJoinOrderResolver.filteredCartesianProduct(groups, froms, condition, context);
	}

	private FromGroup cartesianProduct(Set<FromGroup> groups) {
		return CrossJoinOrderResolver.cartesianProduct(groups, Collections.<From> emptySet(), context);
	}

	private FromGroup lookupJoin(IndexLookupJoinStepDescription step) {
		return new FromGroup( //
				TupleSetBuilder.lookupJoin(step.sourceGroup.tupleSet, step.sourceProperty, step.joinedFrom, step.indexInfo, context), //
				getFromsFor(step), //
				step.sourceGroup.osds);
	}

	private FromGroup rangeJoin(IndexRangeJoinStepDescription step) {
		return new FromGroup( //
				TupleSetBuilder.rangeJoin(step.sourceGroup.tupleSet, step.joinedFrom, step.lowerBounds, step.upperBounds, step.indexInfo, context), //
				getFromsFor(step), //
				step.sourceGroup.osds);
	}

	private Set<From> getFromsFor(ImplicitJoinStepDescription step) {
		Set<From> result = newSet();
		result.add(step.joinedFrom);
		result.addAll(step.sourceGroup.froms);

		return result;
	}

	private FromGroup indexSubSet(IndexSubSetStepDescription step) {
		return new FromGroup( //
				TupleSetBuilder.indexSubSet(step.from, step.indexInfo, step.keys, context), //
				asSet(step.from), //
				emptyList());
	}

	private FromGroup indexRange(IndexRangeStepDescription step) {
		return new FromGroup( //
				TupleSetBuilder.indexRange(step.from, step.indexInfo, step.lowerBound(), step.upperBound(), context), //
				asSet(step.from), //
				emptyList());
	}

	private FromGroup indexOrderedSet(IndexOrderedSetStepDescription step) {
		return new FromGroup( //
				step.osd.toIndexOrderedSet(context), //
				asSet(step.from), //
				singletonList(step.osd));
	}

	private FromGroup sourceSet(SingleSourceStepDescription step) {
		return new FromGroup( //
				TupleSetBuilder.sourceSet(step.from, context), //
				asSet(step.from), //
				emptyList());
	}

	private FromGroup staticSourceSet(StaticSourceStepDescription step) {
		return new FromGroup( //
				TupleSetBuilder.staticSourceSet(step.from, step.refsOrEntities, context), //
				asSet(step.from), //
				emptyList());
	}

	private Set<Condition> findAllConditionsWithinFroms(Collection<Condition> conditions, Set<From> froms) {
		return conditions.stream() //
				.filter(condition -> conditionRelatedToGivenFromsOnly(condition, froms)) //
				.collect(Collectors.toSet());
	}

	private boolean conditionRelatedToGivenFromsOnly(Condition condition, Set<From> froms) {
		Set<From> conditionFroms = context.getFromsFor(condition);
		return froms.containsAll(conditionFroms);
	}

	private void applyConditionsIfEligible(Set<Condition> newGroupConditions) {
		if (newGroupConditions.isEmpty())
			return;

		TupleSet tupleSet = newGroup.tupleSet;

		if (tupleSet.tupleSetType() == TupleSetType.filteredSet) {
			FilteredSet filteredSet = (FilteredSet) tupleSet;
			com.braintribe.model.queryplan.filter.Condition newFilter = convertedConjunction(filteredSet.getFilter(), newGroupConditions);
			filteredSet.setFilter(newFilter);
			return;
		}

		if (tupleSet.tupleSetType() == TupleSetType.sourceSet && context.supportsEntityQueryDelegation()) {
			QuerySourceSet qss = TupleSetBuilder.querySourceSet((SourceSet) tupleSet, newList(newGroupConditions));
			tupleSet = TupleSetBuilder.filteredSet(qss, ConditionBuilder.condition(newGroupConditions, context));

		} else {
			tupleSet = TupleSetBuilder.filteredSet(tupleSet, newGroupConditions, context);
		}

		newGroup = new FromGroup(tupleSet, newGroup.froms, newGroup.osds);
	}

	private Condition condition() {
		return stepDescription.usedConditions.iterator().next();
	}

	private com.braintribe.model.queryplan.filter.Condition convertedConjunction(com.braintribe.model.queryplan.filter.Condition converted,
			Set<Condition> newGroupConditions) {

		List<com.braintribe.model.queryplan.filter.Condition> operands = newList();
		operands.add(converted);

		for (Condition c : newGroupConditions)
			operands.add(context.convertCondition(c));

		return operands.size() == 1 ? operands.get(0) : ConditionBuilder.newConjunction(operands);
	}

}
