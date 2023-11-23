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
package com.braintribe.model.cortexapi.workbench;

import java.util.Set;

import com.braintribe.model.accessapi.AccessRequest;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.service.api.ServiceRequest;

public interface CreateServiceRequestTemplate extends AccessRequest {

	EntityType<CreateServiceRequestTemplate> T = EntityTypes.T(CreateServiceRequestTemplate.class);

	@Mandatory
	ServiceRequest getTemplateRequest();
	void setTemplateRequest(ServiceRequest templateRequest);

	@Mandatory
	String getActionName();
	void setActionName(String actionName);
	
	@Initializer("'actionbar/more'")
	String getFolderPath();
	void setFolderPath(String folderPath);
	
	Set<String> getIgnoreProperties();
	void setIgnoreProperties(Set<String> ignoreProperties);

	@Initializer("true")
	boolean getIgnoreStandardProperties();
	void setIgnoreStandardProperties(boolean ignoreStandardProperties);
	
	GmEntityType getCriterionType();
	void setCriterionType(GmEntityType criterionType);
	
	boolean getMultiSelectionSupport();
	void setMultiSelectionSupport(boolean multiSelectionSupport);	

	boolean getInstantiationAction();
	void setInstantiationAction(boolean instantiationAction);
	
	String getWorkbenchAccessId();
	void setWorkbenchAccessId(String workbenchAccessId);
	
}
