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

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.modellerfilter.AbstractEntityTypeFilter;
import com.braintribe.model.processing.modellergraph.filter.CondensedRelationshipContext;
import com.braintribe.model.processing.modellergraph.filter.RelationshipFilterer;
import com.braintribe.model.processing.modellergraph.filter.relationship.Relationship;

public class AbstractEntityTypeFilterer implements RelationshipFilterer<AbstractEntityTypeFilter>{
	
	@Override
	public boolean matches(CondensedRelationshipContext relationshipContext,
			RelationshipFiltererContext filtererContext,
			AbstractEntityTypeFilter relationshipFilter) {
		if(relationshipContext.getRelationship().getToType().getGmType() instanceof GmEntityType){
			GmEntityType entityType = (GmEntityType)relationshipContext.getRelationship().getToType().getGmType();
			return Boolean.TRUE.equals(entityType.getIsAbstract());
		}
		return false;
	}
	
	@Override
	public boolean matches(Relationship relationship, RelationshipFiltererContext filtererContext, AbstractEntityTypeFilter relationshipFilter) {
		if(relationship.getToType() instanceof GmEntityType){
			GmEntityType entityType = (GmEntityType)relationship.getToType();
			return entityType.getIsAbstract();
		}
		return false;
	}
}
