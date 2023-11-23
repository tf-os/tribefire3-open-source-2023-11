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
package com.braintribe.model.processing.query.tools;

import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.typecondition.basic.TypeKind;

/**
 * @author peter.gazdik
 */
public interface PreparedTcs {

	// @formatter:off
	TraversingCriterion everythingTc = TC.create().negation().joker().done();

	TraversingCriterion scalarOnlyTc = TC.create()
											.negation()
												.disjunction()
													.property(GenericEntity.id)
													.typeCondition(isKind(TypeKind.scalarType))
												.close()
											.done();
	// @formatter:on

}
