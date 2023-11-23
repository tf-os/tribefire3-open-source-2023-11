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
package com.braintribe.model.processing.workbench.action.api;

import java.util.List;

import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.workbench.WorkbenchAction;

public interface WorkbenchActionContext<A extends WorkbenchAction> {
	
	public GmSession getGmSession();
	public List<ModelPath> getModelPaths();
	public A getWorkbenchAction();
	public Object getPanel();
	public Folder getFolder();
	
	default ModelPath getRootModelPath() {
		return null;
	}
	
	default boolean isHandleInNewTab() {
		return true;
	}
	
	/**
	 * Used for instances which can set the workbenchAction after it has been created.
	 * @param workbenchAction - the action
	 */
	default void setWorkbenchAction(A workbenchAction) {
		//NOP
	}
	
	default boolean isUseForm() {
		return true;
	}
	
	/**
	 * Configures whether the form should be displayed.
	 * @param useForm - the configuration
	 */
	default void setUseForm(boolean useForm) {
		//NOP
	}

}
