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

import com.braintribe.model.modellerfilter.DisjunctionRelationshipFilter;
import com.braintribe.model.modellerfilter.RelationshipFilter;
import com.braintribe.model.processing.modellergraph.filter.CondensedRelationshipContext;
import com.braintribe.model.processing.modellergraph.filter.RelationshipFilterer;
import com.braintribe.model.processing.modellergraph.filter.relationship.Relationship;

public class DisjunctionRelationshipFilterer implements RelationshipFilterer<DisjunctionRelationshipFilter> {
	@Override
	public boolean matches(CondensedRelationshipContext relationshipContext, RelationshipFiltererContext filtererContext, DisjunctionRelationshipFilter relationshipFilter) {
		List<RelationshipFilter> operands = relationshipFilter.getOperands();
		
		if (operands != null) {
			for (RelationshipFilter operand: operands) {
				if (filtererContext.matches(relationshipContext, operand)) 
					return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean matches(Relationship relationship, RelationshipFiltererContext filtererContext, DisjunctionRelationshipFilter relationshipFilter) {
		List<RelationshipFilter> operands = relationshipFilter.getOperands();
		
		if (operands != null) {
			for (RelationshipFilter operand: operands) {
				if (filtererContext.matches(relationship, operand)) 
					return true;
			}
		}
		
		return false;
	}
}
