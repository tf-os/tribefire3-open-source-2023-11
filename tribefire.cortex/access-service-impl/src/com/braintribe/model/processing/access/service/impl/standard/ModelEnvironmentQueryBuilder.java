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
package com.braintribe.model.processing.access.service.impl.standard;

import static com.braintribe.model.generic.typecondition.TypeConditions.isAssignableTo;
import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.model.generic.typecondition.TypeConditions.or;
import static com.braintribe.model.generic.typecondition.basic.TypeKind.collectionType;
import static com.braintribe.model.generic.typecondition.basic.TypeKind.entityType;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Resource;

class ModelEnvironmentQueryBuilder {

	/* package */ static EntityQuery buildModelEnvironmentQuery(String modelName) {
		// @formatter:off
		EntityQuery query = EntityQueryBuilder
					.from(GmMetaModel.class)
					.where()
						.property(GmMetaModel.name).eq(modelName)
					.tc(createTraversingCriterion())
					.done();
		// @formatter:on
		
		query.setNoAbsenceInformation(true);
		return query;
	}

	private static TraversingCriterion createTraversingCriterion() {
		return TC.create()
			.disjunction()
				.pattern()
					.entity(Resource.T)
					.property(Resource.resourceSource)
				.close()
				.pattern()
					.conjunction()
						.entity()
						.typeCondition(isAssignableTo(IncrementalAccess.T))
					.close()
					.conjunction()
						.property()
						.typeCondition(or(isKind(collectionType), isKind(entityType)) )
					.close()
				.close()
			.close()
			.done();
		
	}
}
