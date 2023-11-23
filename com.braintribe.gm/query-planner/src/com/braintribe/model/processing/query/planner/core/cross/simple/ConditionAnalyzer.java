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

import static com.braintribe.model.processing.query.planner.core.cross.simple.ConditionAnalysisTools.findIndexChainIfPossible;
import static com.braintribe.model.processing.query.planner.core.cross.simple.ConditionAnalysisTools.findSinglePropertyOperand;
import static com.braintribe.model.processing.query.planner.core.cross.simple.ConditionAnalysisTools.findStaticValue;
import static com.braintribe.model.processing.query.planner.core.cross.simple.ConditionAnalysisTools.getIfFrom;
import static com.braintribe.model.processing.query.planner.core.cross.simple.ConditionAnalysisTools.getSingleFromOperand;
import static com.braintribe.model.processing.query.planner.core.cross.simple.ConditionAnalysisTools.isEqualityOperator;
import static com.braintribe.model.processing.query.planner.core.cross.simple.ConditionAnalysisTools.isOperatorIndexCandidate;
import static com.braintribe.model.processing.query.planner.core.cross.simple.ConditionAnalysisTools.toRefOrEntitySet;
import static com.braintribe.utils.lcd.CollectionTools2.acquireLinkedSet;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.CollectionTools2.size;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.processing.query.eval.api.repo.IndexInfo;
import com.braintribe.model.processing.query.planner.RuntimeQueryPlannerException;
import com.braintribe.model.processing.query.planner.condition.ConditionNormalizer;
import com.braintribe.model.processing.query.planner.context.OrderedSourceDescriptor;
import com.braintribe.model.processing.query.planner.context.QueryOrderingManager;
import com.braintribe.model.processing.query.planner.context.QueryPlannerContext;
import com.braintribe.model.processing.query.planner.core.cross.FromGroup;
import com.braintribe.model.processing.query.planner.core.index.IndexKeys;
import com.braintribe.model.processing.query.planner.core.index.ResolvedLookupIndexKeys;
import com.braintribe.model.processing.query.planner.core.index.StaticIndexKeys;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.query.conditions.Negation;
import com.braintribe.model.query.conditions.ValueComparison;

/**
 * 
 */
class ConditionAnalyzer {

	protected final Collection<Condition> conditions;
	protected final QueryPlannerContext context;

	protected final Map<ConditionApplicationType, Set<StepDescription>> stepByApplicationType = newMap();
	protected final Map<String, IndexRangeStepDescription> indexRanges = newMap();
	protected final Map<String, IndexSubSetStepDescription> indexSubSets = newMap();

	protected final Set<From> joinableFroms = newSet();
	protected final Map<From, FromGroup> groupForFrom = newMap();
	protected final Map<From, Set<Condition>> conditionsForFroms = newMap();
	protected final Map<Condition, Set<From>> fromsForCondition = newMap();
	protected final Map<Condition, Set<FromGroup>> groupsForCondition = newMap();

	private final QueryOrderingManager orderingManager;
	private final IndexJoinAnalyzer indexJoinAnalyzer;

	public ConditionAnalyzer(ConjunctionResolver resolver) {
		this.conditions = resolver.conditions;
		this.context = resolver.context;
		this.orderingManager = context.orderingManager();

		this.indexJoinAnalyzer = new IndexJoinAnalyzer(this, resolver);

		initialize(resolver);
	}

	private void initialize(ConjunctionResolver resolver) {
		computeGroupForFromMapping(resolver);

		for (Condition condition : conditions)
			analyzeSources(condition);
	}

	private void computeGroupForFromMapping(ConjunctionResolver resolver) {
		for (FromGroup group : resolver.groups)
			mapFromsForGroup(group);
	}

	private void mapFromsForGroup(FromGroup group) {
		for (From from : group.froms)
			groupForFrom.put(from, group);
	}

	private void analyzeSources(Condition condition) {
		Set<From> conditionFroms = context.getFromsFor(condition);

		for (From from : conditionFroms) {
			FromGroup group = groupForFrom.get(from);

			acquireLinkedSet(conditionsForFroms, from).add(condition);

			if (group == null)
				acquireLinkedSet(fromsForCondition, condition).add(from);
			else
				acquireLinkedSet(groupsForCondition, condition).add(group);
		}
	}

	public GroupFromConditionAnalysis analyze() {
		for (Condition condition : conditions)
			analyzeApplicationType(condition);

		cleanupAnalysis();

		return new GroupFromConditionAnalysis(stepByApplicationType, fromsForCondition, groupsForCondition, joinableFroms);
	}

	private void analyzeApplicationType(Condition condition) {
		switch (condition.conditionType()) {
			case conjunction:
				throw new IllegalStateException("Conjunction is not expected here. This was invoked from ConjunctionResolver, right?");

			case disjunction:
				handleApplicationTypeFor((Disjunction) condition);
				return;

			case fulltextComparison:
				From from = fromsForCondition.get(condition).iterator().next();
				acquireLinkedSet(stepByApplicationType, ConditionApplicationType.singleFrom).add(StepDescription.singleSource(from));
				return;

			case negation:
				handleApplicationTypeFor((Negation) condition);
				return;

			case valueComparison:
				handleApplicationTypeFor((ValueComparison) condition);
				return;
		}

		throw new RuntimeQueryPlannerException("Unsupported condition: " + condition + " of type: " + condition.conditionType());
	}

	/**
	 * The special branch was not needed for Smood, but was added so disjunction also works with
	 * {@link ConjunctionResolver#applyConditionsIfEligible}. So when a query comes like
	 * {@code select p from Person p where p.name = 'Peter' or p.name = 'Elon'}, we make sure that we end up delegating this condition to the
	 * repository, in case it supports entity queries.
	 */
	private void handleApplicationTypeFor(Disjunction condition) {
		Set<From> froms = fromsForCondition.get(condition);

		int nFroms = size(froms);
		int nGroups = size(groupsForCondition.get(condition));

		// See javadoc for this method
		if (nFroms == 1 && nGroups == 0) {
			// Maybe review if the special handling of Disjunction wouldn't be better here...
			acquireLinkedSet(stepByApplicationType, ConditionApplicationType.singleFrom).add(StepDescription.singleSource(first(froms)));
			return;
		}

		acquireLinkedSet(stepByApplicationType, ConditionApplicationType.disjunction).add(StepDescription.simple(condition));
	}

	/**
	 * Refer to {@link ConditionNormalizer} to see what can be an operand of {@link Negation}
	 */
	private void handleApplicationTypeFor(Negation condition) {
		Set<From> froms = fromsForCondition.get(condition);
		Set<FromGroup> groups = groupsForCondition.get(condition);

		if (size(froms) + size(groups) > 1)
			acquireLinkedSet(stepByApplicationType, ConditionApplicationType.crossProductFilter).add(StepDescription.simple(condition));
		else
			acquireLinkedSet(stepByApplicationType, ConditionApplicationType.singleFrom).add(StepDescription.singleSource(froms.iterator().next()));
	}

	private void handleApplicationTypeFor(ValueComparison condition) {
		Set<From> froms = fromsForCondition.get(condition);

		int nFroms = size(froms);
		int nGroups = size(groupsForCondition.get(condition));

		/* We have these cases: 1 - condition on single from, 2 - condition on at least 1 from and at least 1 group, 3 - condition on groups only */

		if (nFroms == 1 && nGroups == 0) {
			/* Case 1: Try to find a static source set, e.g. with select p from Person p where p = ref(Person, 1L) */
			if (handleStaticSourceIfEligible(condition))
				return;

			/* Case 2: Try to find an indexed source, e.g. with select p from Person p where p.indexedName = ? */
			if (handleIndexIfEligible(condition))
				return;

			/* Case 3: No fast way available, simply take the entire population */
			introducesSources(froms);
			return;
		}

		if (indexJoinAnalyzer.handleIndexJoin(condition))
			return;

		if (nFroms == 0) {
			/* Case 3: Do a crossProduct with a filter */
			if (indexJoinAnalyzer.handleMergeIndexJoin(condition))
				return;

			acquireLinkedSet(stepByApplicationType, ConditionApplicationType.crossProductFilter).add(StepDescription.simple(condition));

		} else {
			/* Case 2: Must select the From, so we can later do a Cartesian product, i.e. singleFrom (no filter) */
			introducesSources(froms);
		}
	}

	/* private */ void introducesSources(Set<From> froms) {
		for (From from : froms) {
			OrderedSourceDescriptor osd = orderingManager.findOsd(from);
			if (osd != null)
				acquireLinkedSet(stepByApplicationType, ConditionApplicationType.indexOrderedSet).add(StepDescription.indexOrderedSet(osd));
			else
				acquireLinkedSet(stepByApplicationType, ConditionApplicationType.singleFrom).add(StepDescription.singleSource(from));
		}
	}

	private boolean handleStaticSourceIfEligible(ValueComparison condition) {
		if (!isEqualityOperator(condition))
			return false;

		From from = getSingleFromOperand(condition, context);
		if (from == null)
			return false;

		Object staticValue = findStaticValue(condition, context);
		if (staticValue == null)
			return false;

		Set<?> refsOrEntities = toRefOrEntitySet(from, staticValue);

		StaticSourceStepDescription step = StepDescription.staticSource(from, refsOrEntities, condition);
		acquireLinkedSet(stepByApplicationType, ConditionApplicationType.staticFrom).add(step);
		return true;
	}

	private boolean handleIndexIfEligible(ValueComparison condition) {
		if (!isOperatorIndexCandidate(condition))
			return false;

		PropertyOperand propertyOperand = findSinglePropertyOperand(condition, context);
		if (propertyOperand == null || propertyOperand.getPropertyName() == null)
			return false;

		Source source = propertyOperand.getSource();
		List<IndexInfo> indexInfos = findIndexChainIfPossible(source, propertyOperand.getPropertyName(), context);
		if (indexInfos.isEmpty())
			return false;

		Object comparedValue = findStaticValue(condition, context);
		if (comparedValue == null)
			return false;

		Operator operator = condition.getOperator();

		if (operator == Operator.equal || operator == Operator.in) {
			boolean first = true;
			IndexKeys indexKeys = null;
			IndexInfo indexInfo = null;

			for (IndexInfo ii : indexInfos) {
				if (first) {
					indexKeys = initialIndexKeys(operator, comparedValue);
					first = false;

				} else {
					indexKeys = new ResolvedLookupIndexKeys(indexInfo, indexKeys);
					source = ((Join) source).getSource();
				}

				indexInfo = ii;
			}

			// we know source is a "From" now, because we have "stepped up" all the way to the From

			IndexSubSetStepDescription step = StepDescription.indexValue(indexInfo, (From) source, indexKeys, condition);
			indexSubSets.put(indexInfo.getIndexId(), step);
			acquireLinkedSet(stepByApplicationType, ConditionApplicationType.indexSubSet).add(step);
			return true;
		}

		if (indexInfos.size() > 1) {
			// TODO CHAIN+RANGE
			// for now, we do not support chains with range conditions
			return false;
		}

		// This is just temporary, here we might want
		From from = getIfFrom(source);

		IndexInfo indexInfo = indexInfos.get(0);

		if (from == null || !indexInfo.hasMetric())
			return false;

		IndexRangeStepDescription step = acquireIndexRangeStep(from, indexInfo, condition);

		switch (operator) {
			case greater:
				step.setLower(comparedValue, false);
				break;
			case greaterOrEqual:
				step.setLower(comparedValue, true);
				break;
			case less:
				step.setUpper(comparedValue, false);
				break;
			case lessOrEqual:
				step.setUpper(comparedValue, true);
				break;
			default:
				return false;
		}

		return true;
	}

	private IndexKeys initialIndexKeys(Operator operator, Object comparedValue) {
		Set<Object> indexKeys = operator == Operator.in ? newSet((Collection<?>) comparedValue) : asSet(comparedValue);
		return new StaticIndexKeys(indexKeys);
	}

	private IndexRangeStepDescription acquireIndexRangeStep(From from, IndexInfo indexInfo, Condition condition) {
		IndexRangeStepDescription step = indexRanges.get(indexInfo.getIndexId());

		if (step == null) {
			step = StepDescription.indexRange(indexInfo, from);
			indexRanges.put(indexInfo.getIndexId(), step);
			acquireLinkedSet(stepByApplicationType, ConditionApplicationType.indexRange).add(step);
		}

		step.usedConditions.add(condition);

		return step;
	}

	/**
	 * Adjusts some cases for the analysis, that are "context" dependent, and cannot be processed right at the moment when given condition is
	 * examined.
	 */
	private void cleanupAnalysis() {
		indexJoinAnalyzer.completeRangesForIndexRangeJoin();
	}

}
