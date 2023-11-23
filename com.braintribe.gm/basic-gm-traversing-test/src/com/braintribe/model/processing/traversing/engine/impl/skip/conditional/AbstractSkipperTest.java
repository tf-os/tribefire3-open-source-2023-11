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
package com.braintribe.model.processing.traversing.engine.impl.skip.conditional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;

import com.braintribe.model.processing.traversing.api.GmTraversingContext;
import com.braintribe.model.processing.traversing.api.GmTraversingException;
import com.braintribe.model.processing.traversing.api.GmTraversingSkippingCriteria;
import com.braintribe.model.processing.traversing.api.GmTraversingVisitor;
import com.braintribe.model.processing.traversing.api.path.TraversingModelPathElement;
import com.braintribe.model.processing.traversing.engine.GMT;
import com.braintribe.model.processing.traversing.engine.impl.misc.model.TypeA;
import com.braintribe.model.processing.traversing.engine.impl.misc.model.TypeB;
import com.braintribe.model.processing.traversing.engine.impl.misc.printer.TraversingModelPathElementPrinter;
import com.braintribe.model.processing.traversing.impl.visitors.GmTraversingVisitorAdapter;

public abstract class AbstractSkipperTest {

	protected List<String> visitedNodes = new ArrayList<String>();

	// create a visitor that will keep track of visiting order
	protected final GmTraversingVisitor trackingVisitor = new GmTraversingVisitorAdapter() {
		@Override
		public void onElementEnter(GmTraversingContext context, TraversingModelPathElement pathElement) throws GmTraversingException {
			visitedNodes.add("Enter " + TraversingModelPathElementPrinter.format(pathElement));
		}

		@Override
		public void onElementLeave(GmTraversingContext context, TraversingModelPathElement pathElement) throws GmTraversingException {
			visitedNodes.add("Leave " + TraversingModelPathElementPrinter.format(pathElement));
		}
	};

	private void validateConditionalSkipper(ConditionalSkipper skipper, Object assembly, List<String> depthFirstexpectedNodes,
			List<String> breadthFirstexpectedNodes) throws GmTraversingException {

		validateBreadthFirst(skipper, assembly, breadthFirstexpectedNodes);

		validateDepthFirst(skipper, assembly, depthFirstexpectedNodes);
	}

	private void validateBreadthFirst(ConditionalSkipper skipper, Object assembly, List<String> expectedNodes)
			throws GmTraversingException {
		visitedNodes = new ArrayList<String>();
		GMT.traverse().visitor(skipper).visitor(trackingVisitor).breadthFirstWalk().doFor(assembly);

		for (int i = 0; i < visitedNodes.size(); i++) {
			String expected = i == expectedNodes.size() ? "END_OF_EXPECTED" : expectedNodes.get(i);
			String actual = visitedNodes.get(i);

			if (!expected.equals(actual)) {
				Assert.fail("Wrong node visited on position '" + i + "'. Actual sequence: " + printSequencePrefix(visitedNodes, i) +
						", but we should have ended with: " + expected);
			}
		}

		if (visitedNodes.size() < expectedNodes.size()) {
			Assert.fail("Not enough nodes visited.");
		}
	}

	private String printSequencePrefix(List<String> expectedNodes, int i) {
		StringBuilder sb = new StringBuilder('\'');
		int counter = 0;
		for (String s: expectedNodes) {
			if (counter < i) {
				sb.append(s);
				sb.append(", ");

			} else if (counter == i) {
				sb.append('[');
				sb.append(s);
				sb.append(']');

				return sb.toString();
			}

			sb.append(',');
			counter++;
		}

		throw new RuntimeException("Unexpected end of sequence of nodes.");
	}

	private void validateDepthFirst(ConditionalSkipper skipper, Object assembly, List<String> expectedNodes) throws GmTraversingException {
		visitedNodes = new ArrayList<String>();
		GMT.traverse().visitor(skipper).visitor(trackingVisitor).doFor(assembly);

		assertThat(visitedNodes).isEqualTo(expectedNodes);
	}

	protected void propertyMatchListItem(ConditionalSkipper skipper) throws GmTraversingException {

		Object assembly = getAssembly();
		List<String> depthFirstExpectedNodes;
		List<String> breadthFirstExpectedNodes;

		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipAll);
		breadthFirstExpectedNodes = ConditionalSkippingTraversingResults.getBreadthFirstPropertyMatchListItemWithSkipAllCriteriaEvents();
		depthFirstExpectedNodes = ConditionalSkippingTraversingResults.getDepthFirstPropertyMatchListItemWithSkipAllCriteriaEvents();
		validateConditionalSkipper(skipper, assembly, depthFirstExpectedNodes, breadthFirstExpectedNodes);

		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipWalkFrame);
		breadthFirstExpectedNodes = ConditionalSkippingTraversingResults
				.getBreadthFirstPropertyMatchListItemWithSkipWalkFrameCriteriaEvents();
		depthFirstExpectedNodes = ConditionalSkippingTraversingResults.getDepthFirstPropertyMatchListItemWithSkipWalkFrameCriteriaEvents();
		validateConditionalSkipper(skipper, assembly, depthFirstExpectedNodes, breadthFirstExpectedNodes);

		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipDescendants);
		breadthFirstExpectedNodes = ConditionalSkippingTraversingResults
				.getBreadthFirstPropertyMatchListItemWithSkipDescendantsCriteriaEvents();
		depthFirstExpectedNodes = ConditionalSkippingTraversingResults.getDepthFirstPropertyMatchListItemWithSkipWalkFrameCriteriaEvents();
		validateConditionalSkipper(skipper, assembly, depthFirstExpectedNodes, breadthFirstExpectedNodes);
	}

	protected void propertyNameMatch(ConditionalSkipper skipper) throws GmTraversingException {

		Object assembly = getAssembly();
		List<String> depthFirstExpectedNodes;
		List<String> breadthFirstExpectedNodes;

		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipAll);
		breadthFirstExpectedNodes = ConditionalSkippingTraversingResults.getBreadthFirstPropertyNameMatchWithSkipAllCriteriaEvents();
		depthFirstExpectedNodes = ConditionalSkippingTraversingResults.getDepthFirstPropertyNameMatchWithSkipAllCriteriaEvents();
		validateConditionalSkipper(skipper, assembly, depthFirstExpectedNodes, breadthFirstExpectedNodes);

		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipWalkFrame);
		breadthFirstExpectedNodes = ConditionalSkippingTraversingResults.getBreadthFirstPropertyNameMatchWithSkipWalkFrameCriteriaEvents();
		depthFirstExpectedNodes = ConditionalSkippingTraversingResults.getDepthFirstPropertyNameMatchWithSkipWalkFrameCriteriaEvents();
		validateConditionalSkipper(skipper, assembly, depthFirstExpectedNodes, breadthFirstExpectedNodes);

		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipDescendants);
		breadthFirstExpectedNodes = ConditionalSkippingTraversingResults
				.getBreadthFirstPropertyNameMatchWithSkipDescendantsCriteriaEvents();
		depthFirstExpectedNodes = ConditionalSkippingTraversingResults.getDepthFirstPropertyNameMatchWithSkipDescendantsCriteriaEvents();
		validateConditionalSkipper(skipper, assembly, depthFirstExpectedNodes, breadthFirstExpectedNodes);
	}

	protected void matchesTypeOfMatch(ConditionalSkipper skipper) throws GmTraversingException {

		Object assembly = getAssembly();
		List<String> depthFirstExpectedNodes;
		List<String> breadthFirstExpectedNodes;

		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipAll);
		breadthFirstExpectedNodes = ConditionalSkippingTraversingResults.getBreadthFirstMatchesTypeOfMatchWithSkipAllCriteriaEvents();
		depthFirstExpectedNodes = ConditionalSkippingTraversingResults.getDepthFirstMatchesTypeOfWithSkipAllCriteriaEvents();
//		validateConditionalSkipper(skipper, assembly, depthFirstExpectedNodes, breadthFirstExpectedNodes);

		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipWalkFrame);
		breadthFirstExpectedNodes = ConditionalSkippingTraversingResults.getBreadthFirstMatchesTypeOfMatchWithSkipWalkFrameCriteriaEvents();
		depthFirstExpectedNodes = ConditionalSkippingTraversingResults.getDepthFirstMatchesTypeOfWithSkipWalkFrameCriteriaEvents();
//		validateConditionalSkipper(skipper, assembly, depthFirstExpectedNodes, breadthFirstExpectedNodes);

		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipDescendants);
		breadthFirstExpectedNodes = ConditionalSkippingTraversingResults
				.getBreadthFirstMatchesTypeOfMatchWithSkipDescendantsCriteriaEvents();
		depthFirstExpectedNodes = ConditionalSkippingTraversingResults.getDepthFirstMatchesTypeOfMatchWithSkipDescendantsCriteriaEvents();
		validateConditionalSkipper(skipper, assembly, depthFirstExpectedNodes, breadthFirstExpectedNodes);

	}

	protected void negationMatch(ConditionalSkipper skipper) throws GmTraversingException {

		Object assembly = getAssembly();
		List<String> depthFirstExpectedNodes;
		List<String> breadthFirstExpectedNodes;

		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipAll);
		breadthFirstExpectedNodes = ConditionalSkippingTraversingResults.getBreadthFirstNegationMatchWithSkipAllCriteriaEvents();
		depthFirstExpectedNodes = ConditionalSkippingTraversingResults.getDepthFirstNegationWithSkipAllCriteriaEvents();
		validateConditionalSkipper(skipper, assembly, depthFirstExpectedNodes, breadthFirstExpectedNodes);

		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipWalkFrame);
		breadthFirstExpectedNodes = ConditionalSkippingTraversingResults.getBreadthFirstNegationMatchWithSkipAllCriteriaEvents();
		depthFirstExpectedNodes = ConditionalSkippingTraversingResults.getDepthFirstNegationMatchWithSkipWalkFrameCriteriaEvents();
		validateConditionalSkipper(skipper, assembly, depthFirstExpectedNodes, breadthFirstExpectedNodes);

		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipDescendants);
		breadthFirstExpectedNodes = ConditionalSkippingTraversingResults.getBreadthFirstNegationMatchWithSkipDescendantsCriteriaEvents();
		depthFirstExpectedNodes = ConditionalSkippingTraversingResults.getDepthFirstNegationMatchWithSkipDescendantsCriteriaEvents();
		validateConditionalSkipper(skipper, assembly, depthFirstExpectedNodes, breadthFirstExpectedNodes);
	}

	protected void sequenceMatch(ConditionalSkipper skipper) throws GmTraversingException {

		Object assembly = getAssembly();
		List<String> depthFirstExpectedNodes;
		List<String> breadthFirstExpectedNodes;

		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipAll);
		breadthFirstExpectedNodes = ConditionalSkippingTraversingResults.getBreadthFirstSequenceMatchWithSkipAllCriteriaEvents();
		depthFirstExpectedNodes = ConditionalSkippingTraversingResults.getDepthFirstSequenceWithSkipAllCriteriaEvents();
		validateConditionalSkipper(skipper, assembly, depthFirstExpectedNodes, breadthFirstExpectedNodes);

		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipWalkFrame);
		breadthFirstExpectedNodes = ConditionalSkippingTraversingResults.getBreadthFirstSequenceMatchWithSkipWalkFrameCriteriaEvents();
		depthFirstExpectedNodes = ConditionalSkippingTraversingResults.getDepthFirstSequenceMatchWithSkipWalkFrameCriteriaEvents();
		validateConditionalSkipper(skipper, assembly, depthFirstExpectedNodes, breadthFirstExpectedNodes);

		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipDescendants);
		breadthFirstExpectedNodes = ConditionalSkippingTraversingResults.getBreadthFirstSequenceMatchWithSkipDescendantsCriteriaEvents();
		depthFirstExpectedNodes = ConditionalSkippingTraversingResults.getDepthFirstSequenceMatchWithSkipDescendantsCriteriaEvents();
		validateConditionalSkipper(skipper, assembly, depthFirstExpectedNodes, breadthFirstExpectedNodes);
	}

	protected void disjunctionMatch(ConditionalSkipper skipper) throws GmTraversingException {

		Object assembly = getAssembly();
		List<String> depthFirstExpectedNodes;
		List<String> breadthFirstExpectedNodes;

		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipAll);
		breadthFirstExpectedNodes = ConditionalSkippingTraversingResults.getBreadthFirstDisjunctionMatchWithSkipAllCriteriaEvents();
		depthFirstExpectedNodes = ConditionalSkippingTraversingResults.getDepthFirstDisjunctionWithSkipAllCriteriaEvents();
		validateConditionalSkipper(skipper, assembly, depthFirstExpectedNodes, breadthFirstExpectedNodes);

		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipWalkFrame);
		breadthFirstExpectedNodes = ConditionalSkippingTraversingResults.getBreadthFirstDisjunctionMatchWithSkipWalkFrameCriteriaEvents();
		depthFirstExpectedNodes = ConditionalSkippingTraversingResults.getDepthFirstDisjunctionMatchWithSkipWalkFrameCriteriaEvents();
		validateConditionalSkipper(skipper, assembly, depthFirstExpectedNodes, breadthFirstExpectedNodes);

		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipDescendants);
		breadthFirstExpectedNodes = ConditionalSkippingTraversingResults.getBreadthFirstDisjunctionMatchWithSkipDescendantsCriteriaEvents();
		depthFirstExpectedNodes = ConditionalSkippingTraversingResults.getDepthFirstDisjunctionMatchWithSkipDescendantsCriteriaEvents();
		validateConditionalSkipper(skipper, assembly, depthFirstExpectedNodes, breadthFirstExpectedNodes);
	}

	protected void conjunctionMatch(ConditionalSkipper skipper) throws GmTraversingException {

		Object assembly = getAssembly();
		List<String> depthFirstExpectedNodes;
		List<String> breadthFirstExpectedNodes;

		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipAll);
		breadthFirstExpectedNodes = ConditionalSkippingTraversingResults.getBreadthFirstConjunctionMatchWithSkipAllCriteriaEvents();
		depthFirstExpectedNodes = ConditionalSkippingTraversingResults.getDepthFirstConjunctionWithSkipAllCriteriaEvents();
		validateConditionalSkipper(skipper, assembly, depthFirstExpectedNodes, breadthFirstExpectedNodes);

		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipWalkFrame);
		breadthFirstExpectedNodes = ConditionalSkippingTraversingResults.getBreadthFirstConjunctionMatchWithSkipWalkFrameCriteriaEvents();
		depthFirstExpectedNodes = ConditionalSkippingTraversingResults.getDepthFirstConjunctionMatchWithSkipWalkFrameCriteriaEvents();
		validateConditionalSkipper(skipper, assembly, depthFirstExpectedNodes, breadthFirstExpectedNodes);

		skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipDescendants);
		breadthFirstExpectedNodes = ConditionalSkippingTraversingResults.getBreadthFirstConjunctionMatchWithSkipDescendantsCriteriaEvents();
		depthFirstExpectedNodes = ConditionalSkippingTraversingResults.getDepthFirstConjunctionMatchWithSkipDescendantsCriteriaEvents();
		validateConditionalSkipper(skipper, assembly, depthFirstExpectedNodes, breadthFirstExpectedNodes);
	}

	protected Object getAssembly() {
		TypeA a1 = TypeA.T.create();
		TypeA a2 = TypeA.T.create();
		TypeA a3 = TypeA.T.create();
		TypeA a4 = TypeA.T.create();
		TypeA a5 = TypeA.T.create();
		TypeB b1 = TypeB.T.create();
		TypeB b2 = TypeB.T.create();
		TypeB b3 = TypeB.T.create();
		TypeB b4 = TypeB.T.create();
		TypeB b5 = TypeB.T.create();

		a1.setName("a1");
		a2.setName("a2");
		a3.setName("a3");
		a4.setName("a4");
		a5.setName("a5");

		b1.setName("b1");
		b2.setName("b2");
		b3.setName("b3");
		b4.setName("b4");
		b5.setName("b5");

		a1.setSomeB(b1);
		a1.setSomeA(a2);

		b1.setList(Arrays.asList(a3, a4, a5));
		b1.setDate(new Date());

		a2.setSomeB(b2);
		a3.setSomeB(b3);
		a4.setSomeB(b4);
		a5.setSomeB(b5);

		return a1;
	}

}
