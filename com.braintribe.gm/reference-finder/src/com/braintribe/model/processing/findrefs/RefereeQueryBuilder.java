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
package com.braintribe.model.processing.findrefs;

import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.conditions.Condition;

/**
 * The {@link RefereeQueryBuilder} builds entity queries to find all entities where a specific property references an
 * entity
 * 
 * 
 */
public class RefereeQueryBuilder {

	private PropertyConditionCreator propertyConditionCreator = new PropertyConditionCreator();
	private static TraversingCriterion tc = TC.create()
			.pattern()
				
			.close()
		.done();
	
	static {
		// PGA: I don't get it, this TC cannot match anything, the typeCondition is always false.
		tc = TC.create()
			.conjunction()
				.property()				
				.typeCondition(
						TypeConditions.and(
								TypeConditions.isKind(TypeKind.collectionType), 
								TypeConditions.isKind(TypeKind.entityType)))
				.close()
			.done();
	}
	
	public EntityQuery buildQuery(CandidateProperty candidateProperty, EntityReference reference) {
		EntityQuery entityQuery = EntityQuery.T.create();
		entityQuery.setEntityTypeSignature(candidateProperty.getEntityTypeSignature());

		Condition condition = createConditionForProperty(candidateProperty, reference);

		Restriction restriction = Restriction.T.create();
		restriction.setCondition(condition);
		entityQuery.setRestriction(restriction);

		entityQuery.setTraversingCriterion(tc);
		return entityQuery;
	}

	private Condition createConditionForProperty(CandidateProperty property, EntityReference reference) {
		return propertyConditionCreator.createConditionForProperty(property, reference);
	}

	public void setPropertyConditionCreator(PropertyConditionCreator propertyConditionCreator) {
		this.propertyConditionCreator = propertyConditionCreator;
	}
}
