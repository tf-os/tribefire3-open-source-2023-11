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
package com.braintribe.model.processing.modellergraph.filter.experts;

import com.braintribe.model.modellerfilter.WildcardRelationshipFilter;
import com.braintribe.model.processing.modellergraph.filter.FilterStringifier;
import com.braintribe.model.processing.modellergraph.filter.FilterStringifyContext;

public class WildcardStringifier<T extends WildcardRelationshipFilter> implements FilterStringifier<T> {
	private String name;
	
	public WildcardStringifier(String name) {
		super();
		this.name = name;
	}


	@Override
	public void stringify(FilterStringifyContext context, T relationshipFilter, StringBuilder builder) {
		String expression = relationshipFilter.getWildcardExpression();
		builder.append(name);
		builder.append(" like ");
		builder.append(expression);
	}
}
