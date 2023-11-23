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
package com.braintribe.model.processing.query.tools;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.braintribe.model.processing.query.tools.traverse.ConditionTraverser;
import com.braintribe.model.processing.query.tools.traverse.ConditionVisitor;
import com.braintribe.model.processing.query.tools.traverse.OperandVisitor;
import com.braintribe.model.processing.query.tools.traverse.OrderingTraverser;
import com.braintribe.model.processing.query.tools.traverse.OrderingVisitor;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SimpleOrdering;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.query.functions.aggregate.AggregateFunction;

/**
 * @author peter.gazdik
 */
public class AggregationAnalyzer implements ConditionVisitor, OperandVisitor, OrderingVisitor {

	// ##########################################
	// ## . . . . . . . . API . . . . . . . . .##
	// ##########################################

	public static AggregationAnalysis analyze(Predicate<Object> evalExcludedTest, SelectQuery query) {
		return new AggregationAnalyzer(evalExcludedTest, query).analyze();
	}

	public static class AggregationAnalysis {
		/** Operands that are part of {@link SelectQuery#getSelections()} */
		public List<AggregateFunction> selectedOperands;

		/** Operands that are part of {@link SelectQuery}'s ordering or having clause */
		public List<Operand> extraOperands = newList();

		/** Flag that describes whether this query needs aggregation */
		public boolean hasAggregation;
	}

	// ##########################################
	// ## . . . . . . . . IMPL . . . . . . . . ##
	// ##########################################

	private final Predicate<Object> evalExcludedCheck;
	private final SelectQuery query;

	private final AggregationAnalysis result = new AggregationAnalysis();

	private AggregationAnalyzer(Predicate<Object> evalExcludedTest, SelectQuery query) {
		this.evalExcludedCheck = evalExcludedTest;
		this.query = query;
	}

	private AggregationAnalysis analyze() {
		examineSelectedOperands();
		examineExtraOperands();
		setHasAggregation();

		return result;
	}

	private void examineSelectedOperands() {
		result.selectedOperands = (List<AggregateFunction>) (List<?>) query.getSelections().stream() //
				.filter(this::isAggregateFunction) //
				.peek(af -> result.hasAggregation = true) //
				.collect(Collectors.toList());
	}

	private void examineExtraOperands() {
		// collect all operands and mark hasAggregation=true if an AggregateFunction is encountered
		ConditionTraverser.traverse(evalExcludedCheck, this, this, query.getHaving());
		OrderingTraverser.traverse(evalExcludedCheck, this, this, query.getOrdering());
	}

	@Override
	public void visit(AggregateFunction af) {
		result.hasAggregation = true;
	}

	@Override
	public boolean visit(ValueComparison condition) {
		visitOperand(condition.getLeftOperand());
		visitOperand(condition.getRightOperand());
		return true;
	}

	@Override
	public boolean visit(SimpleOrdering ordering) {
		visitOperand(ordering.getOrderBy());
		return true;
	}

	private void visitOperand(Object o) {
		if (isOperand(o))
			result.extraOperands.add((Operand) o);
	}

	private boolean isAggregateFunction(Object o) {
		return o instanceof AggregateFunction && !evalExcludedCheck.test(o);
	}

	private boolean isOperand(Object o) {
		return o instanceof Operand && !evalExcludedCheck.test(o);
	}

	private boolean setHasAggregation() {
		return result.hasAggregation |= query.getGroupBy() != null;
	}

}
