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
package com.braintribe.model.generic.pr.criteria;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;




public interface NegationCriterion extends TraversingCriterion {

	final EntityType<NegationCriterion> T = EntityTypes.T(NegationCriterion.class);

	// @formatter:off
	void setCriterion(TraversingCriterion criterion);
	TraversingCriterion getCriterion();
	// @formatter:on

	@Override
	default CriterionType criterionType() {
		return CriterionType.NEGATION;
	}
}
