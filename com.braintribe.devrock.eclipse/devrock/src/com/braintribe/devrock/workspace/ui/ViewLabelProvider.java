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
package com.braintribe.devrock.workspace.ui;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.eclipse.model.workspace.Project;
import com.braintribe.devrock.eclipse.model.workspace.WorkingSet;
import com.braintribe.devrock.eclipse.model.workspace.Workspace;

public class ViewLabelProvider extends CellLabelProvider implements IStyledLabelProvider {
	
	
	private UiSupport uiSupport;
	private String uiSupportStylersKey;
	
	private Image workingsetImage;
	private Image workspaceImage;
	private Image projectImage;

	@Configurable @Required
	public void setUiSupport(UiSupport uiSupport) {
		this.uiSupport = uiSupport;				
		setupUiSupport();
	}
	
	@Configurable
	public void setUiSupportStylersKey(String uiSupportStylersKey) {
		this.uiSupportStylersKey = uiSupportStylersKey;
	}
		
	private void setupUiSupport() {		
		//Stylers stylers = uiSupport.stylers(uiSupportStylersKey);
		//stylers.addStandardStylers();		
		workspaceImage = uiSupport.images().addImage("workspace", getClass(), "workspace.png");
		workingsetImage = uiSupport.images().addImage("workingset", getClass(), "BrowseType.gif");
		projectImage = uiSupport.images().addImage("project", getClass(), "project_obj.png");
		
	}
	
	@Override
	public Image getImage(Object arg0) {
		if (arg0 instanceof Workspace) {
			return workspaceImage;
		}
		else if (arg0 instanceof WorkingSet) {
			return workingsetImage;
		}
		else if (arg0 instanceof Project) {
			return projectImage;
		}
		return null;
	}

	@Override
	public StyledString getStyledText(Object arg0) {
		if (arg0 instanceof Workspace) {
			Workspace workspace = (Workspace) arg0;		
			String workspaceName = workspace.getWorkspaceName();			
			return new StyledString( workspaceName != null ? workspaceName : "<unnamed>");
		}
		else if (arg0 instanceof WorkingSet) {
			WorkingSet workingSet = (WorkingSet) arg0;
			return new StyledString( workingSet.getWorkingSetName());
		}
		else if (arg0 instanceof Project) {
			Project project = (Project) arg0;			
			return new StyledString( project.getProjectName());
		}
		return null;
	}

	@Override
	public void update(ViewerCell arg0) {
	}

}
