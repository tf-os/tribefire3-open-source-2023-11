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
package com.braintribe.model.processing.query.stringifier.experts.query;

import java.util.List;
import java.util.Set;

import com.braintribe.model.processing.query.api.stringifier.QueryStringifierRuntimeException;
import com.braintribe.model.processing.query.api.stringifier.experts.StringifierContext;
import com.braintribe.model.processing.query.stringifier.BasicQueryStringifierContext;
import com.braintribe.model.processing.query.stringifier.experts.AbstractQueryStringifier;
import com.braintribe.model.query.From;
import com.braintribe.model.query.GroupBy;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.Source;

public class SelectQueryStringifier extends AbstractQueryStringifier<SelectQuery, BasicQueryStringifierContext> {
	@Override
	public String stringify(SelectQuery query, BasicQueryStringifierContext context) throws QueryStringifierRuntimeException {
		// Create StringBuilder and add "select" and "distinct" to query
		final StringBuilder queryString = new StringBuilder(query.getDistinct() ? "select distinct" : "select");

		// Stringify selection of query
		final List<Object> selections = query.getSelections();
		if (selections != null && selections.size() > 0) {
			// Stringify selection operands
			boolean first = true;
			for (Object selection : selections) {
				queryString.append(first ? " " : ", ");
				String selectionString = context.stringify(selection);
				queryString.append(selectionString);
				first = false;
			}
		} else {
			// Tag everything
			queryString.append(" *");
		}

		// Stringify elements of query
		final List<From> froms = query.getFroms();
		if (froms.isEmpty() == false) {
			queryString.append(" from");

			// @formatter:off
			// First stringify froms and ignore joins, then check joins afterwards.
			// Otherwise a insane 'from' 'join' mixed query will be the result.
			// @formatter:on

			// Stringify from elements
			appendFromsTypeSignatures(query, context, queryString);

			// Stringify joins of froms
			appendFromsWithJoins(query, context, queryString);
		}

		appendCondition(query, context, queryString);
		appendOrdering(query, context, queryString);
		appendGroupBy(query, context, queryString);
		appendPaging(query, context, queryString);

		return queryString.toString();
	}

	protected void appendFromsWithJoins(SelectQuery query, BasicQueryStringifierContext context, final StringBuilder queryString) {
		List<From> froms = query.getFroms();

		for (From from : froms) {
			// Stringify join of current from
			appendFrom(from, context, queryString);
		}
	}

	protected void appendSource(Source source, BasicQueryStringifierContext context, final StringBuilder queryString) {
		if (source == null) {
			queryString.append(context.getEmptyAliasName());
			return;
		}

		if (context.isAliasRegisteredFor(source)) {
			context.stringifyAndAppend(source, queryString);
		} else {
			if (source instanceof Join) {
				appendJoin((Join) source, context, queryString);
			} else if (source instanceof From) {
				appendFrom((From) source, context, queryString);
			}
		}
	}

	protected void appendFrom(From from, BasicQueryStringifierContext context, final StringBuilder queryString) {
		Set<Join> joins = from.getJoins();

		for (Join join : joins) {
			appendJoin(join, context, queryString);
		}
	}

	protected void appendJoin(Join join, BasicQueryStringifierContext context, final StringBuilder queryString) {
		queryString.append(" ");

		JoinType joinType = getJoinType(join);
		appendJoinType(joinType, queryString);

		appendSource(join.getSource(), context, queryString);
		final String propertyName = join.getProperty();

		// Add property name if there is one
		if (propertyName != null && propertyName.length() > 0) {
			queryString.append(".");
			queryString.append(context.escapeKeywords(propertyName));
		}

		// Add alias to query part
		queryString.append(" ");
		context.stringifyAndAppend(join, queryString);

		for (Join subJoin : join.getJoins()) {
			appendSource(subJoin, context, queryString);
		}
	}

	protected void appendJoinType(JoinType joinType, StringBuilder queryString) {
		// Check join type
		switch (joinType) {
		case full: {
			// set outer join
			queryString.append("full join ");
			break;
		}
		case left: {
			// set left join
			queryString.append("left join ");
			break;
		}
		case right: {
			// set right join
			queryString.append("right join ");
			break;
		}
		case inner:
		default: {
			// set default join
			queryString.append("join ");
			break;
		}
		}
	}

	private static JoinType getJoinType(Join join) {
		JoinType joinType = join.getJoinType();

		if (joinType == null) {
			joinType = JoinType.inner;
		}

		return joinType;
	}

	protected void appendFromsTypeSignatures(SelectQuery query, BasicQueryStringifierContext context, final StringBuilder queryString) {
		List<From> froms = query.getFroms();

		boolean first = true;
		for (From from : froms) {
			queryString.append(first ? " " : ", ");

			queryString.append(context.getShortening().shorten(context.getTypeSignature(from, "<?>")));
			queryString.append(" ");
			context.stringifyAndAppend(from, queryString); // alias

			first = false;
		}
	}

	protected void appendGroupBy(SelectQuery query, StringifierContext context, final StringBuilder queryString) {
		GroupBy groupBy = query.getGroupBy();

		if (groupBy != null && !groupBy.getOperands().isEmpty()) {
			queryString.append(" group by ");
			context.stringifyAndAppend(groupBy, queryString);
		}
	}
}
