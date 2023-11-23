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

import java.util.List;
import java.util.Map;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.model.processing.query.tools.AggregationAnalyzer;
import com.braintribe.model.processing.query.tools.AggregationAnalyzer.AggregationAnalysis;
import com.braintribe.model.processing.query.tools.OperandHashingComparator;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.SelectQuery;

/**
 * @author peter.gazdik
 */
public class QueryAggregationManager {

	private final Map<Operand, Integer> operandToPosition = CodingMap.create(OperandHashingComparator.INSTANCE);
	private final AggregationAnalysis aggAnalysis;

	public QueryAggregationManager(QueryPlannerContext context, SelectQuery query) {
		this.aggAnalysis = AggregationAnalyzer.analyze(context.evalExclusionCheck(), query);
	}

	public boolean hasAggregation() {
		return aggAnalysis.hasAggregation;
	}

	public List<Operand> getExtraOperands() {
		return aggAnalysis.extraOperands;
	}

	public void noticeTupleComponentIndex(Object o, int index) {
		if (o instanceof Operand)
			operandToPosition.putIfAbsent((Operand) o, index);
	}

	public Integer findTupleComponentIndexOf(Operand o) {
		return operandToPosition.get(o);
	}

	public int getTupleComponentIndexOf(Operand o) {
		return operandToPosition.computeIfAbsent(o, this::throwCannotFindAfIndexException);
	}

	private Integer throwCannotFindAfIndexException(Operand o) {
		throw new IllegalStateException("Cannot retrieve index of operand '" + o + "' as this doesn't seem to have been selected.");
	}

}
