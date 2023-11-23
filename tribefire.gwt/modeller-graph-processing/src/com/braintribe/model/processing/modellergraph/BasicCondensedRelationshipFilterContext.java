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
package com.braintribe.model.processing.modellergraph;

import com.braintribe.model.modellergraph.condensed.CondensedModel;
import com.braintribe.model.modellergraph.condensed.CondensedRelationship;
import com.braintribe.model.modellergraph.condensed.CondensedType;
import com.braintribe.model.processing.modellergraph.filter.CondensedRelationshipContext;


public class BasicCondensedRelationshipFilterContext implements CondensedRelationshipContext {
	private int order;
	private CondensedRelationship relationship;
	private CondensedRelationship originalRelationship;
	
	public BasicCondensedRelationshipFilterContext(int order,
			CondensedRelationship originalRelationship, CondensedRelationship relationship) {
		super();
		this.order = order;
		this.relationship = relationship;
		this.originalRelationship = originalRelationship;
	}

	@Override
	public CondensedModel getModel() {
		return relationship.getToType().getModel();
	}
	
	@Override
	public int getOrder() {
		return order;
	}
	
	@Override
	public CondensedRelationship getRelationship() {
		return relationship;
	}
	
	@Override
	public CondensedType getToType() {
		return relationship.getToType();
	}
	
	@Override
	public CondensedRelationship getOriginalRelationship() {
		return originalRelationship;
	}
}
