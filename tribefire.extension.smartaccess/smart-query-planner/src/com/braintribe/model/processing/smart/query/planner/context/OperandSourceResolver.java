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

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.List;
import java.util.Map;

import com.braintribe.model.processing.query.tools.traverse.OperandTraverser;
import com.braintribe.model.processing.query.tools.traverse.OperandVisitor;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.functions.JoinFunction;
import com.braintribe.model.query.functions.Localize;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.query.functions.aggregate.AggregateFunction;

/**
 * 
 * @author peter.gazdik
 */
class OperandSourceResolver {

	private final SmartQueryPlannerContext context;
	private final SourceFinder sourceFinder;

	private final Map<Object, List<Source>> sourcesForOperand = newMap();

	public OperandSourceResolver(SmartQueryPlannerContext context) {
		this.context = context;
		this.sourceFinder = new SourceFinder();
	}

	public List<Source> getSourcesForOperand(Object operand) {
		List<Source> result = sourcesForOperand.get(operand);

		if (result == null) {
			result = resolveSourcesFor(operand);
			sourcesForOperand.put(operand, result);
		}

		return result;
	}

	private List<Source> resolveSourcesFor(Object operand) {
		return sourceFinder.findSources(operand);
	}

	private class SourceFinder implements OperandVisitor {

		private final OperandTraverser traverser;

		private List<Source> currentSources;

		public SourceFinder() {
			this.traverser = new OperandTraverser(context.evalExclusionCheck(), this);
			this.currentSources = newList();
		}

		public List<Source> findSources(Object operand) {
			currentSources = newList();
			traverser.traverseOperand(operand);

			return currentSources;
		}

		@Override
		public void visit(PropertyOperand operand) {
			visit(operand.getSource());
		}

		@Override
		public void visit(JoinFunction operand) {
			visit(operand.getJoin());
		}

		@Override
		public void visit(Localize operand) {
			traverser.traverseOperand(operand.getLocalizedStringOperand());
		}

		@Override
		public void visit(AggregateFunction operand) {
			throw new UnsupportedOperationException("Method 'GroupComparator.SourceMarker.visit' is not implemented yet!");
		}

		@Override
		public void visit(QueryFunction operand) {
			for (Operand o: context.listOperands(operand))
				traverser.traverseOperand(o);
		}

		@Override
		public void visit(Source operand) {
			currentSources.add(operand);
		}

	}

}
