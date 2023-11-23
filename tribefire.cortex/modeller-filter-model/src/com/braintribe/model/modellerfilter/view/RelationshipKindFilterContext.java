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
package com.braintribe.model.modellerfilter.view;


import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.modellerfilter.JunctionRelationshipFilter;


public interface RelationshipKindFilterContext extends JunctionRelationshipFilter {

	EntityType<RelationshipKindFilterContext> T = EntityTypes.T(RelationshipKindFilterContext.class);
	
	public void setAbstract(boolean useAbstract);
	public boolean getAbstract();
	
	public void setGeneralization(boolean generalization);
	public boolean getGeneralization();
	
	public void setSpecialization(boolean specialization);
	public boolean getSpecialization();
	
	public void setAggregation(boolean aggregation);
	public boolean getAggregation();
	
	public void setInverseAggregation(boolean inverseAggregation);
	public boolean getInverseAggregation();
	
	public void setMapping(boolean mapping);
	public boolean getMapping();
	
}
