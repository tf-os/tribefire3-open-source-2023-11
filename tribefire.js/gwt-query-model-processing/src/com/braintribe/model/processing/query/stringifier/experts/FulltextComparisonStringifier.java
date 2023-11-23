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
package com.braintribe.model.processing.query.stringifier.experts;

import com.braintribe.model.processing.query.api.stringifier.QueryStringifierRuntimeException;
import com.braintribe.model.processing.query.api.stringifier.experts.Alias;
import com.braintribe.model.processing.query.api.stringifier.experts.Stringifier;
import com.braintribe.model.processing.query.stringifier.BasicQueryStringifierContext;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.FulltextComparison;

public class FulltextComparisonStringifier implements Stringifier<FulltextComparison, BasicQueryStringifierContext> {
	@Override
	public String stringify(FulltextComparison fulltextComparison, BasicQueryStringifierContext context) throws QueryStringifierRuntimeException {
		StringBuilder queryString = new StringBuilder();
		queryString.append("fullText(");

		Source source = fulltextComparison.getSource();
		Alias alias = context.acquireAlias(source);
		if (alias != null) {
			if (context.isReplaceAliasInUse()) {
				queryString.append(context.getReplaceAliasTag());
				if (source != null) {
					context.setDoRemoveReplaceAliasTag(false);
				}
			} else {
				queryString.append(alias.getName());
			}
			queryString.append(", ");
		}
		// Add full-text value
		context.stringifyAndAppend(fulltextComparison.getText(), queryString);

		queryString.append(")");
		return queryString.toString();
	}
}
