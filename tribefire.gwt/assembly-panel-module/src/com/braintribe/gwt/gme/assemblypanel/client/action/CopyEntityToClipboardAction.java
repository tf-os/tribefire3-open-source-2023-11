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
package com.braintribe.gwt.gme.assemblypanel.client.action;

import java.util.List;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gme.assemblypanel.client.AssemblyPanel;
import com.braintribe.gwt.gme.assemblypanel.client.LocalizedText;
import com.braintribe.gwt.gme.assemblypanel.client.resources.AssemblyPanelResources;
import com.braintribe.gwt.gmview.client.GmAmbiguousSelectionSupport;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.MapValuePathElement;
import com.braintribe.model.generic.path.ModelPath;

/**
 * Action used for adding the selected entity to the clipboard.
 *
 */
public class CopyEntityToClipboardAction extends ModelAction {
	
	private AssemblyPanel assemblyPanel;
	
	/**
	 * Configures the required {@link AssemblyPanel}.
	 */
	@Required
	public void setAssemblyPanel(AssemblyPanel assemblyPanel) {
		this.assemblyPanel = assemblyPanel;
		assemblyPanel.addSelectionListener(gmSelectionSupport -> {
			List<List<ModelPath>> modelPaths = gmSelectionSupport instanceof GmAmbiguousSelectionSupport
					? ((GmAmbiguousSelectionSupport) gmSelectionSupport).getAmbiguousSelection()
					: gmSelectionSupport.transformSelection(gmSelectionSupport.getCurrentSelection());
			updateState(modelPaths);
		});
	}
	
	public CopyEntityToClipboardAction() {
		setName(LocalizedText.INSTANCE.toClipboard());
		setTooltip(LocalizedText.INSTANCE.toClipboardDescription());
		setIcon(AssemblyPanelResources.INSTANCE.clipboard());
		setHidden(true);
	}
	
	@Override
	public void perform(TriggerInfo triggerInfo) {
		assemblyPanel.addSelectedModelsToClipBoard();
	}

	@Override
	protected void updateVisibility() {
		setHidden(true);
		if (modelPaths == null)
			return;
		
		for (List<ModelPath> singleSelection : modelPaths) {
			boolean entityFound = false;
			for (ModelPath modelPath : singleSelection) {
				if (modelPath.last().getValue() instanceof GenericEntity) {
					if (modelPath.last() instanceof MapValuePathElement
							&& !(((MapValuePathElement) modelPath.last()).getKey() instanceof GenericEntity)) {
						continue;
					}
					entityFound = true;
					break;
				}
			}
			
			if (!entityFound)
				return;
		}
		
		setHidden(false);
	}

}
