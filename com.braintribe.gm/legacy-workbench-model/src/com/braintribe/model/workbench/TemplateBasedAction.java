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
package com.braintribe.model.workbench;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import com.braintribe.model.template.Template;

/**
 * @author gunther.schenk
 */
@Abstract
public interface TemplateBasedAction extends WorkbenchAction {

	EntityType<TemplateBasedAction> T = EntityTypes.T(TemplateBasedAction.class);

	@Description("Specifies the Template used for the request.")
	public Template getTemplate();
	public void setTemplate (Template template);
	
	@Description("Specifies whether we force displaying the form with the request properties.")
	public boolean getForceFormular();
	public void setForceFormular(boolean force);
	
}
