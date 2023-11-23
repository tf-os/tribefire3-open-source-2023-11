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
import static com.braintribe.model.processing.query.planner.core.cross.simple.ConditionAnalysisTools.getIfOperand;
import static com.braintribe.model.processing.query.planner.core.cross.simple.ConditionAnalysisTools.getIfPropertyOperand;
import static com.braintribe.model.processing.query.planner.core.cross.simple.ConditionAnalysisTools.getIndexInfoIfPossible;
import static com.braintribe.model.processing.query.planner.core.cross.simple.ConditionAnalysisTools.isOperatorIndexCandidate;
import static com.braintribe.utils.lcd.CollectionTools2.acquireSet;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.CollectionTools2.size;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.model.processing.query.api.stringifier.QueryStringifier;
import com.braintribe.model.processing.query.eval.api.repo.IndexInfo;
import com.braintribe.model.processing.query.eval.set.EvalMergeLookupJoin;
import com.braintribe.model.processing.query.planner.condition.ConditionNormalizer;
import com.braintribe.model.processing.query.planner.context.QueryPlannerContext;
import com.braintribe.model.processing.query.planner.core.cross.FromGroup;
import com.braintribe.model.processing.query.planner.core.index.StaticIndexKeys;
import com.braintribe.model.processing.query.planner.tools.Bound;
import com.braintribe.model.processing.query.planner.tools.ObjectComparator;
import com.braintribe.model.processing.query.stringifier.BasicQueryStringifier;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.queryplan.set.MergeLookupJoin;

/**
 * 
 */
class IndexJoinAnalyzer {

	private final QueryPlannerContext context;
	private final ConditionAnalyzer conditionAnalyzer;
	private final Set<From> allFroms;

	private final Map<String, IndexRangeJoinStepDescription> indexRangeJoins = newMap();
	private final Map<String, MergeRangeJoinStepDescription> mergeRangeJoins = newMap();

	private final QueryStringifier queryStringifier = BasicQueryStringifier.create();

	public IndexJoinAnalyzer(ConditionAnalyzer conditionAnalyzer, ConjunctionResolver resolver) {
		this.context = conditionAnalyzer.context;
		this.conditionAnalyzer = conditionAnalyzer;

		this.allFroms = resolver.froms;
	}

	public boolean handleIndexJoin(ValueComparison comparison) {
		Set<From> conditionFroms = conditionAnalyzer.fromsForCondition.get(comparison);

		if (!isOperatorIndexCandidate(comparison) || size(conditionFroms) == 0)
			return false;

		Operand leftOperand = getIfOperand(comparison.getLeftOperand(), context);
		Operand rightOperand = getIfOperand(comparison.getRightOperand(), context);

		if (leftOperand == null || rightOperand == null)
			return false;

		boolean result = tryJoin(leftOperand, rightOperand, comparison);
		result = result || tryJoin(rightOperand, leftOperand, comparison);

		return result;
	}

	/** Tries to do the join, and if it fails, it might ask for providing a source via {@link ConditionAnalyzer#introducesSources(Set)}. */
	private boolean tryJoin(Operand srcOperand, Operand joinedOperand, ValueComparison comparison) {
		PropertyOperand joinedProp = getIfPropertyOperand(joinedOperand, context);
		if (joinedProp == null)
			return false;

		From joinedFrom = getIfFrom(joinedProp.getSource());

		if (joinedFrom == null || !allFroms.contains(joinedFrom)) {
			/* I can only do an implicit join if with a From that was not already put into some group (which is e.g. possible if I have more
			 * properties on some source that could be used for join - I can only use one of them) */
			return false;
		}

		Operator operator = comparison.getOperator();
		IndexInfo index = getIndexInfoIfPossible(joinedFrom, joinedProp.getPropertyName(), context);

		if (index == null || (operator != Operator.equal && !index.hasMetric()))
			return false;

		FromGroupAnalysis fgAnalysis = findFromsAndGroupsFor(srcOperand);

		if (!fgAnalysis.froms.isEmpty()) {
			/* we could do the join, but there is no group for the source yet, so let's ask for creating the group */
			conditionAnalyzer.introducesSources(fgAnalysis.froms);

			conditionAnalyzer.joinableFroms.add(joinedFrom);

			return true;
		}

		if (askForCrossProduct(fgAnalysis))
			return true;

		FromGroup sourceGroup = first(fgAnalysis.groups);
		if (operator == Operator.equal) {
			StepDescription step = StepDescription.lookupJoin(index, sourceGroup, srcOperand, joinedFrom, joinedProp, comparison);
			acquireSet(conditionAnalyzer.stepByApplicationType, ConditionApplicationType.valueJoin).add(step);

		} else {
			IndexRangeJoinStepDescription step = acquireIndexRangeJoinStep(index, sourceGroup, joinedFrom, joinedProp, comparison);

			switch (operator) {
				case greater:
					step.addUpper(srcOperand, false);
					break;
				case greaterOrEqual:
					step.addUpper(srcOperand, true);
					break;
				case less:
					step.addLower(srcOperand, false);
					break;
				case lessOrEqual:
					step.addLower(srcOperand, true);
					break;
				default:
					return false;
			}
		}

		return true;
	}

	private IndexRangeJoinStepDescription acquireIndexRangeJoinStep(IndexInfo indexInfo, FromGroup sourceGroup, From joinedFrom,
			PropertyOperand joinedProp, Condition condition) {

		String useCase = sourceGroup.id + indexInfo.getIndexId();
		IndexRangeJoinStepDescription step = indexRangeJoins.get(useCase);

		if (step == null) {
			step = StepDescription.indexRangeJoin(indexInfo, sourceGroup, joinedFrom, joinedProp);
			indexRangeJoins.put(useCase, step);
			acquireSet(conditionAnalyzer.stepByApplicationType, ConditionApplicationType.rangeJoin).add(step);
		}

		step.usedConditions.add(condition);

		return step;
	}

	/**
	 * This is not really related to indices, but the eval implementation actually builds an index for the right side, so for every leftValue it can
	 * easily find the corresponding right values.
	 * 
	 * @see MergeLookupJoin
	 * @see EvalMergeLookupJoin
	 */
	public boolean handleMergeIndexJoin(ValueComparison comparison) {
		if (!isOperatorIndexCandidate(comparison) || comparison.getOperator() == Operator.in)
			return false;

		Operand leftOperand = getIfOperand(comparison.getLeftOperand(), context);
		Operand rightOperand = getIfOperand(comparison.getRightOperand(), context);

		if (leftOperand == null || rightOperand == null)
			return false;

		return mergeJoin(leftOperand, rightOperand, comparison);
	}

	private boolean mergeJoin(Operand srcOperand, Operand otherOperand, ValueComparison comparison) {
		/* if either side contains multiple groups, we need to do a cross product */

		FromGroupAnalysis srcAnalysis = findFromsAndGroupsFor(srcOperand);
		if (askForCrossProduct(srcAnalysis))
			return true;

		FromGroupAnalysis otherAnalysis = findFromsAndGroupsFor(otherOperand);
		if (askForCrossProduct(otherAnalysis))
			return true;

		/* if here, we know we can just do the join */
		FromGroup srcGroup = first(srcAnalysis.groups);
		FromGroup otherGroup = first(otherAnalysis.groups);

		Operator operator = comparison.getOperator();

		if (shouldRevertOrderForMergeJoin(srcGroup, otherGroup)) {
			FromGroup gr = srcGroup;
			srcGroup = otherGroup;
			otherGroup = gr;

			Operand op = srcOperand;
			srcOperand = otherOperand;
			otherOperand = op;

			operator = ConditionNormalizer.mirror(operator);
		}

		if (operator == Operator.equal) {
			StepDescription step = StepDescription.mergeLookupJoin(srcGroup, srcOperand, otherGroup, otherOperand, comparison);
			acquireSet(conditionAnalyzer.stepByApplicationType, ConditionApplicationType.mergeLookupJoin).add(step);

		} else {
			MergeRangeJoinStepDescription step = acquireMergeRangeJoinStep(srcGroup, otherGroup, otherOperand, comparison);

			switch (operator) {
				case greater:
					step.addUpper(srcOperand, false);
					break;
				case greaterOrEqual:
					step.addUpper(srcOperand, true);
					break;
				case less:
					step.addLower(srcOperand, false);
					break;
				case lessOrEqual:
					step.addLower(srcOperand, true);
					break;
				default:
					return false;
			}
		}

		return true;
	}

	private boolean shouldRevertOrderForMergeJoin(FromGroup srcGroup, FromGroup otherGroup) {
		return otherGroup.osdIndex < srcGroup.osdIndex;
	}

	private boolean askForCrossProduct(FromGroupAnalysis fgAnalysis) {
		if (fgAnalysis.groups.size() > 1) {
			acquireSet(conditionAnalyzer.stepByApplicationType, ConditionApplicationType.crossProduct)
					.add(StepDescription.crossProduct(fgAnalysis.groups));
			return true;
		}
		return false;
	}

	private MergeRangeJoinStepDescription acquireMergeRangeJoinStep(FromGroup srcGroup, FromGroup otherGroup, Operand otherOperand,
			Condition condition) {

		String useCase = srcGroup.id + "#" + otherGroup.id + ":" + queryStringifier.stringify(otherOperand);
		MergeRangeJoinStepDescription step = mergeRangeJoins.get(useCase);

		if (step == null) {
			step = StepDescription.mergeRangeJoin(srcGroup, otherGroup, otherOperand, condition);
			mergeRangeJoins.put(useCase, step);
			acquireSet(conditionAnalyzer.stepByApplicationType, ConditionApplicationType.mergeRangeJoin).add(step);
		}

		step.usedConditions.add(condition);

		return step;
	}

	private FromGroupAnalysis findFromsAndGroupsFor(Operand operand) {
		FromGroupAnalysis result = new FromGroupAnalysis();

		Set<From> fromsForOperand = context.getFromsForOperand(operand);
		for (From from : fromsForOperand) {
			if (allFroms.contains(from))
				result.froms.add(from);
			else
				result.groups.add(conditionAnalyzer.groupForFrom.get(from));
		}

		return result;
	}

	// ###################################
	// ## . . . . PostProcessing. . . . ##
	// ###################################

	public void completeRangesForIndexRangeJoin() {
		for (Entry<String, IndexRangeJoinStepDescription> entry : indexRangeJoins.entrySet()) {
			IndexRangeJoinStepDescription joinStep = entry.getValue();
			String indexId = joinStep.indexInfo.getIndexId();

			adjustRange(joinStep, conditionAnalyzer.indexRanges.get(indexId));
			adjustRange(joinStep, conditionAnalyzer.indexSubSets.get(indexId));
		}
	}

	private void adjustRange(IndexRangeJoinStepDescription joinStep, IndexRangeStepDescription rangeStep) {
		if (rangeStep == null)
			return;

		if (rangeStep.lowerInclusive != null)
			joinStep.lowerBounds.add(new Bound(rangeStep.lowerBound, rangeStep.lowerInclusive));

		if (rangeStep.upperInclusive != null)
			joinStep.upperBounds.add(new Bound(rangeStep.upperBound, rangeStep.upperInclusive));

		joinStep.usedConditions.addAll(rangeStep.usedConditions);
	}

	private void adjustRange(IndexRangeJoinStepDescription joinStep, IndexSubSetStepDescription indexSubSetStep) {
		if (indexSubSetStep == null || !(indexSubSetStep.keys instanceof StaticIndexKeys)) {
			/* Since we have a range (metric) restriction on our property (which we want to use for a join), we expect this property to have simple
			 * type. That means we only expect StaticIndexKeys as the keys for any related IndexSubSetStepDescription as there cannot be any property
			 * chain starting at this property. */
			return;
		}

		Set<Object> keys = ((StaticIndexKeys) indexSubSetStep.keys).keys;

		// TODO TEST with keys really containing more than 1 value (to see if it works)

		Object min = findMinMax(keys, false);
		Object max = findMinMax(keys, true);

		Bound b = new Bound(min, true);
		joinStep.lowerBounds.add(b);
		b = min == max ? b : new Bound(max, true);
		joinStep.upperBounds.add(b);

		joinStep.usedConditions.addAll(indexSubSetStep.usedConditions);
	}

	private Object findMinMax(Set<Object> values, boolean max) {
		boolean first = true;
		Object result = null;
		for (Object o : values) {
			if (first) {
				result = o;
				first = false;
			} else {
				boolean thisIsBigger = ObjectComparator.compare(o, result) > 0;
				if (thisIsBigger == max) {
					result = 0;
				}
			}
		}

		return result;
	}

	private static class FromGroupAnalysis {
		Set<From> froms = newSet();
		Set<FromGroup> groups = newSet();
	}

}
