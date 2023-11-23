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

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * WorkbenchAction which can be configured with a {@link ServiceRequest} which will be executed.
 */
public interface ServiceRequestAction extends WorkbenchAction {

	EntityType<ServiceRequestAction> T = EntityTypes.T(ServiceRequestAction.class);
	
	@Description("Specifies the request which will be executed by the action.")
	public ServiceRequest getRequest();
	public void setRequest(ServiceRequest request);
	
	@Description("Specifies the type of the execution. It can be either auto (default), autoEditable or editable. Editable means showing the request properties for edition.")
	public ExecutionType getExecutionType();
	public void setExecutionType(ExecutionType executionType);
	
	@Description("Specifies whether auto paging is automatically configured for the ServiceRequest execution. The ServiceRequest should be a HasPagination.")
	public boolean getAutoPaging();
	public void setAutoPaging(boolean autoPaging);
	
	@Description("Specifies whether the auto paging configuration can be manually changed by users via the UI.")
	public boolean getPagingEditable();
	public void setPagingEditable(boolean pagingEditable);
}

