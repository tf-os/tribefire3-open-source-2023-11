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
package com.braintribe.model.processing.smart.query.planner.core.combination;

import java.util.Collection;
import java.util.List;

import com.braintribe.model.processing.query.planner.RuntimeQueryPlannerException;
import com.braintribe.model.processing.smart.query.planner.context.SmartQueryPlannerContext;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNode;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.SingleAccessGroup;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.query.conditions.FulltextComparison;
import com.braintribe.model.query.conditions.Negation;
import com.braintribe.model.query.conditions.ValueComparison;

/**
 * 
 * TODO work it out a bit more, this is just very very simple and inaccurate
 * 
 * @author peter.gazdik
 */
class GroupSizeEstimator {

	private final SmartQueryPlannerContext context;

	private static final long MAX_SIZE = 10 * 1000;

	public GroupSizeEstimator(SmartQueryPlannerContext context) {
		this.context = context;
	}

	public long estimateSize(SourceNodeGroup group) {
		if (!(group instanceof SingleAccessGroup))
			throw new UnsupportedOperationException("TODO!");

		return estimateSize((SingleAccessGroup) group);
	}

	private long estimateSize(SingleAccessGroup group) {
		if (group.conditions.isEmpty())
			return MAX_SIZE;

		return estimateConjunction(group.conditions);
	}

	private long estimateCondition(Condition c) {
		switch (c.conditionType()) {
			case conjunction:
				return estimateConjunction(((Conjunction) c).getOperands());
			case disjunction:
				return estimateDisjunction(((Disjunction) c).getOperands());
			case fulltextComparison:
				return estimateFulltext((FulltextComparison) c);
			case negation:
				return estimateNegation(((Negation) c).getOperand());
			case valueComparison:
				return estimateValueComparison((ValueComparison) c);
		}

		throw new RuntimeQueryPlannerException("Unsupported condition: " + c + " of type: " + c.conditionType());
	}

	private long estimateConjunction(Collection<Condition> operands) {
		long result = MAX_SIZE;

		for (Condition c: operands)
			result = Math.min(result, estimateCondition(c));

		return result;
	}

	private long estimateDisjunction(Collection<Condition> operands) {
		long result = 0;

		for (Condition c: operands)
			result += estimateCondition(c);

		return Math.min(result, MAX_SIZE);
	}

	private long estimateNegation(Condition operand) {
		return MAX_SIZE - estimateCondition(operand);
		// return 10 - estimateCondition(operand);
	}

	private long estimateValueComparison(ValueComparison c) {
		List<Source> leftSources = context.getSourcesForOperand(c.getLeftOperand());
		List<Source> rightSources = context.getSourcesForOperand(c.getRightOperand());

		if (leftSources.isEmpty())
			return estimateStaticValueComparison(c.getOperator(), rightSources);

		if (rightSources.isEmpty())
			return estimateStaticValueComparison(c.getOperator(), leftSources);

		
		return 5 * Math.min(costOfPathTo(leftSources.get(0)), costOfPathTo(rightSources.get(0)));
	}

	@SuppressWarnings("unused")
	private long estimateStaticValueComparison(Operator operator, List<Source> sources) {
		if (sources.isEmpty())
			// All non-source conditions should be evaluated beforehand
			throw new UnsupportedOperationException("TODO not expected");

		return costOfPathTo(sources.get(0));
	}

	private long estimateFulltext(FulltextComparison c) {
		Source s = c.getSource();

		return costOfPathTo(s) * 3;
	}

	private long costOfPathTo(Source source) {
		SourceNode node = context.planStructure().getSourceNode(source);

		// TODO improve

		if (node.getSourceNodeType().isExplicitJoin())
			return 1;
		else
			return 1000;
	}

}
