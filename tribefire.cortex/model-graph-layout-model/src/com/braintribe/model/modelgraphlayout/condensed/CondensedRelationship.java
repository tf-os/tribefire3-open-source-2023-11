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
package com.braintribe.model.modelgraphlayout.condensed;

import java.util.Set;


import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmProperty;


public interface CondensedRelationship extends CondensedModelElement {

	final EntityType<CondensedRelationship> T = EntityTypes.T(CondensedRelationship.class);

	// @formatter:off
	CondensedType getFromType();
	void setFromType(CondensedType fromType);

	CondensedType getToType();
	void setToType(CondensedType toType);

	Set<GmProperty> getAggregations();
	void setAggregations(Set<GmProperty> aggregations);

	Set<GmProperty> getInverseAggregations();
	void setInverseAggregations(Set<GmProperty> inverseAggregations);

	boolean getGeneralization();
	void setGeneralization(boolean generalization);

	boolean getSpecialization();
	void setSpecialization(boolean specialization);

	boolean getMapping();
	void setMapping(boolean mapping);
	// @formatter:on

}
