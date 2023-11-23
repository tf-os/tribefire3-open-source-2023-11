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

import com.braintribe.model.modellerfilter.InverseAggregationFilter;
import com.braintribe.model.processing.modellergraph.filter.CondensedRelationshipContext;
import com.braintribe.model.processing.modellergraph.filter.RelationshipFilterer;
import com.braintribe.model.processing.modellergraph.filter.relationship.Relationship;
import com.braintribe.model.processing.modellergraph.filter.relationship.RelationshipKind;

public class InverseAggregationFilterer implements RelationshipFilterer<InverseAggregationFilter>{
	
	@Override
	public boolean matches(CondensedRelationshipContext relationshipContext,
			RelationshipFiltererContext filtererContext,
			InverseAggregationFilter relationshipFilter) {
		return !relationshipContext.getRelationship().getInverseAggregations().isEmpty();
	}
	
	@Override
	public boolean matches(Relationship relationship, RelationshipFiltererContext filtererContext, InverseAggregationFilter relationshipFilter) {
		return relationship.getRelationshipKind() == RelationshipKind.inverseAggregation;
	}

}
