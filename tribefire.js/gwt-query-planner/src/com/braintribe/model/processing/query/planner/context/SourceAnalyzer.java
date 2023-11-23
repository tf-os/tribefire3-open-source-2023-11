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

import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.matching.Matcher;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.StandardTraversingContext;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.processing.query.planner.core.property.PropertyJoinAdder;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.Source;

/**
 * Simply builds a {@link SourceAnalysis} for given query.
 */
class SourceAnalyzer {

	private SelectQuery query;
	private Set<GenericEntity> evaluationExcludes;
	private From currentFrom;

	private final SourceAnalysis result = new SourceAnalysis();

	public SourceAnalysis analyzeSources(SelectQuery query) {
		this.query = query;
		this.evaluationExcludes = nullSafe(query.getEvaluationExcludes());

		analyzeSelections();
		analyzeFroms();

		return result;
	}

	// ###################################
	// ## . . . SELECT analysis . . . . ##
	// ###################################

	private void analyzeSelections() {
		JoinAvoidingMatcher matcher = new JoinAvoidingMatcher();

		StandardTraversingContext tc = new StandardTraversingContext();
		tc.setMatcher(matcher);

		BaseType.INSTANCE.traverse(tc, query.getSelections());
		BaseType.INSTANCE.traverse(tc, query.getOrdering());
	}

	/**
	 * This matcher stops at any {@link Source} it encounters. If the source is a {@link Join}, it also remembers it as
	 * a join needed for the selection. These joins are needed for {@link PropertyJoinAdder#addJoins()} method (via
	 * {@link QuerySourceManager#findAllSelectionJoins()}).
	 */
	private class JoinAvoidingMatcher implements Matcher {
		@Override
		public boolean matches(TraversingContext tc) {
			Object peek = tc.getObjectStack().peek();
			if (isSource(peek)) {
				if (peek instanceof Join)
					result.mandatoryJoins.add((Join) peek);

				return true;
			}

			return false;
		}
	}

	private boolean isSource(Object o) {
		return o instanceof Source && !evaluationExcludes.contains(o);
	}

	// ###################################
	// ## . . . . FROM analysis . . . . ##
	// ###################################

	private void analyzeFroms() {
		for (From from : query.getFroms()) {
			currentFrom = from;

			visitSource(from);
			traverse(from.getJoins());
		}
	}

	private void traverse(Set<Join> joins) {
		if (joins == null)
			return;

		for (Join join : joins)
			traverse(join);
	}

	private void traverse(Join join) {
		visitSource(join);

		JoinType joinType = join.getJoinType();
		if (joinType == JoinType.right || joinType == JoinType.full)
			result.rightJoins.add(join);

		traverse(join.getJoins());
	}

	private void visitSource(Source source) {
		result.relevantSources.add(source);
		result.sourceRoot.put(source, currentFrom);
	}

}
