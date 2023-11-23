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

import java.util.Set;

import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.modellerfilter.WildcardEntityTypeFilter;
import com.braintribe.model.processing.modellergraph.filter.CondensedRelationshipContext;
import com.braintribe.model.processing.modellergraph.filter.relationship.AbstractAggregationRelationship;
import com.braintribe.model.processing.modellergraph.filter.relationship.Relationship;

public class WildcardPropertyFilterer extends WildcardFilterer<WildcardEntityTypeFilter> {
	@Override
	protected String[] getValues(CondensedRelationshipContext relationshipContext) {
		Set<GmProperty> aggregations = relationshipContext.getRelationship().getAggregations();
		String[] values = new String[aggregations.size()];
		int i = 0;
		for (GmProperty property: aggregations) {
			values[i++] = property.getName();
		}
		return values;
	}
	
	@Override
	protected String getValue(Relationship relationship) {
		switch (relationship.getRelationshipKind()) {
		case aggregation:
		case inverseAggregation:
			AbstractAggregationRelationship aggregation = (AbstractAggregationRelationship)relationship;
			return aggregation.getProperty().getName();
		default:
			return null;
		}
	}
}
