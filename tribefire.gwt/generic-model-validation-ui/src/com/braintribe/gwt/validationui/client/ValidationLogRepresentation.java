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
package com.braintribe.gwt.validationui.client;

import com.braintribe.gwt.gmview.client.EditEntityActionListener;
import com.braintribe.gwt.gmview.client.GmEntityView;
import com.braintribe.gwt.gmview.client.WorkWithEntityActionListener;
import com.braintribe.model.generic.validation.log.ValidationLog;
import com.google.gwt.user.client.ui.IsWidget;

public interface ValidationLogRepresentation extends GmEntityView, IsWidget {
	
	public void addValidationLog(ValidationLog validationLog);		
	public void setValidationLog(ValidationLog validationLog);		
	public void addWorkWithEntityActionListener(WorkWithEntityActionListener workWithEntityActionListener);
	public void setEditEntityActionListener(EditEntityActionListener editEntityActionListener);

}
