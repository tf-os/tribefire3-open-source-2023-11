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
package com.braintribe.gwt.gmview.action.client;

import com.braintribe.model.meta.GmType;
import com.braintribe.model.workbench.WorkbenchAction;

public class GmTypeOrAction {
	
	private GmType type;
	private WorkbenchAction action;
	
	public GmTypeOrAction(GmType type, WorkbenchAction action) {
		this.type = type;
		this.action = action;
	}

	public GmType getType() {
		return type;
	}

	public void setType(GmType type) {
		this.type = type;
	}

	public WorkbenchAction getAction() {
		return action;
	}

	public void setAction(WorkbenchAction action) {
		this.action = action;
	}

}
