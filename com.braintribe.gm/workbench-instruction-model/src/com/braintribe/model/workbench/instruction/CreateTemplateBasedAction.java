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
package com.braintribe.model.workbench.instruction;

import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.template.meta.TemplateMetaData;

public interface CreateTemplateBasedAction extends WorkbenchInstruction, HasPath {
	
	EntityType<CreateTemplateBasedAction> T = EntityTypes.T(CreateTemplateBasedAction.class);
	
	@Mandatory
	String getActionName();
	void setActionName(String actionName);

	@Mandatory
	GenericEntity getPrototype();
	void setPrototype(GenericEntity prototype);
	
	@Mandatory
	String getActionType();
	void setActionType(String actionType);
	
	Set<String> getIgnoreProperties();
	void setIgnoreProperties(Set<String> ignoreProperties);

	@Initializer("true")
	boolean getIgnoreStandardProperties();
	void setIgnoreStandardProperties(boolean ignoreStandardProperties);
	
	@Initializer("true")
	boolean getBeautifyVariableNames();
	void setBeautifyVariableNames(boolean beautifyVariableNames);
	
	TraversingCriterion getCriterion();
	void setCriterion(TraversingCriterion criterion);
	
	boolean getMultiSelectionSupport();
	void setMultiSelectionSupport(boolean multiSelectionSupport);	

	Set<TemplateMetaData> getTemplateMetaData();
	void setTemplateMetaData(Set<TemplateMetaData> templateMetaData);
	
}
