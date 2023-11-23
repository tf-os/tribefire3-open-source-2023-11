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
package com.braintribe.gwt.gme.constellation.client;

import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.path.GmModelPath;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionHandler;
import com.braintribe.model.workbench.ModelLinkAction;

public class ModelLinkActionHandler implements WorkbenchActionHandler<ModelLinkAction> {
	
	private ExplorerConstellation explorerConstellation;
	
	@Required
	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
	}

	@Override
	public void perform(WorkbenchActionContext<ModelLinkAction> workbenchActionContext) {
		GmModelPath gmModelPath = workbenchActionContext.getWorkbenchAction().getPath();
		
		ModelPath modelPath = new ModelPath();
		modelPath.add(new RootPathElement(GMF.getTypeReflection().getType(gmModelPath.getElements().get(0).getTypeSignature()), gmModelPath.getElements().get(0).getValue()));
		explorerConstellation.showEntityVerticalTabElement(modelPath, false, false, false);
	}

}
