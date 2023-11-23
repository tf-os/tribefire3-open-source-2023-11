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

/**
 * Base type for the {@link TemplateServiceRequestBasedAction} and the {@link TemplateQueryAction}.
 * @author michel.docouto
 */
@Abstract
public interface TemplateServiceRequestBasedAction extends TemplateBasedAction {

	@Description("Specifies whether auto paging is automatically configured for the template actions. The ServiceRequest should be a HasPagination in case of TemplateQueryAction.")
	public boolean getAutoPaging();
	public void setAutoPaging(boolean autoPaging);

	/**
	 * Configures the execution type. If null is set, then we should use the OLD UI.
	 * If something is set, then we use the NEW UI.
	 */
	@Description("Specifies the type of the execution. If something is set, then the Normalized UI is displayed. If auto or autoEditable are set, then the request is automatically executed.")
	public ExecutionType getExecutionType();
	public void setExecutionType(ExecutionType executionType);

}
