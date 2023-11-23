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

import java.util.List;

import com.braintribe.model.modellerfilter.JunctionRelationshipFilter;
import com.braintribe.model.modellerfilter.RelationshipFilter;
import com.braintribe.model.processing.modellergraph.filter.FilterStringifier;
import com.braintribe.model.processing.modellergraph.filter.FilterStringifyContext;

public class JunctionRelationshipStringifier<T extends JunctionRelationshipFilter> implements FilterStringifier<T> {
	private String name;
	
	public JunctionRelationshipStringifier(String name) {
		super();
		this.name = name;
	}

	@Override
	public void stringify(FilterStringifyContext context, T relationshipFilter, StringBuilder builder) {
		List<RelationshipFilter> operands = relationshipFilter.getOperands();
		
		if (operands != null && !operands.isEmpty()) {
			for (int i = 0; i < operands.size(); i++) {
				if (i > 0) {
					builder.append(' ');
					builder.append(name);
					builder.append(' ');
				}
				RelationshipFilter operand = operands.get(i);
				if (operand instanceof JunctionRelationshipFilter) {
					builder.append('(');
					context.stringify(operand, builder);
					builder.append(')');
				}
				else
					context.stringify(operand, builder);
			}
		}
		else {
			builder.append("true");
		}
	}

}
