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
package com.braintribe.gwt.templateeditor.client.action;

import java.util.Arrays;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.templateeditor.client.LocalizedText;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.template.Template;

public class EditTemplateScriptAction extends ModelAction{
	
	private ExplorerConstellation explorerConstellation;
	private Template template;
	
	public EditTemplateScriptAction() {
		setHidden(true);
		setName(LocalizedText.INSTANCE.editTemplate());
		setIcon(GmViewActionResources.INSTANCE.edit());
		setHoverIcon(GmViewActionResources.INSTANCE.editBig());
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
	}
	
	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
	}
	
	@Override
	protected void updateVisibility() {
		if (modelPaths == null || modelPaths.size() != 1) {
			setHidden(true);
			return;
		}
		
		for (ModelPath selectedValue : modelPaths.get(0)) {
			ModelPathElement element = selectedValue.get(selectedValue.size() - 1);
			if (element.getValue() instanceof Template) {
				template = element.getValue();
				setHidden(template.getPrototype() == null || template.getScript() == null);
				return;
			}
		}
		
		setHidden(true);
	}

	@Override
	public void perform(TriggerInfo triggerInfo) {
		showTemplateEditorPanel();
	}

	private void showTemplateEditorPanel(){
		ModelPath modelPath = new ModelPath();
		modelPath.add(new RootPathElement(GMF.getTypeReflection().getType(template), template));
		explorerConstellation.showEntityVerticalTabElement(modelPath, null, false, false);
	}
	
}
