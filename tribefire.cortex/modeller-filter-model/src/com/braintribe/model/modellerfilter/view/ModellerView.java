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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;

 @SelectiveInformation("${name} - ${user}")
public interface ModellerView extends GenericEntity {

	EntityType<ModellerView> T = EntityTypes.T(ModellerView.class);
	
	public void setIncludesFilterContext(IncludesFilterContext includesFilterContext);
	public IncludesFilterContext getIncludesFilterContext();
	
	public void setExcludesFilterContext(ExcludesFilterContext excludesFilterContext);
	public ExcludesFilterContext getExcludesFilterContext();
	
	public void setRelationshipKindFilterContext(RelationshipKindFilterContext relationshipKindFilterContext);
	public RelationshipKindFilterContext getRelationshipKindFilterContext();
	
	public void setMetaModel(GmMetaModel metaModel);
	public GmMetaModel getMetaModel();
	
	public void setFocusedType(GmType gmType);
	public GmType getFocusedType();
	
	public void setName(String name);
	public String getName();
	
	public void setUser(String user);
	public String getUser();
	
	public void setSettings(ModellerSettings settings);
	public ModellerSettings getSettings();
	
}
