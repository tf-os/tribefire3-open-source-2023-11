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
// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

import java.util.Arrays;
import java.util.List;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;

/**
 * Action that will copy entity Id to the clipboard as a string. On multiselection the Ids are comma separated
 *
 */
public class CopyIdToClipboardAction extends ModelAction  {

	public CopyIdToClipboardAction() {
		setHidden(true);
		setName(LocalizedText.INSTANCE.copyId());
		setIcon(GmViewActionResources.INSTANCE.clipboardBig());
		setHoverIcon(GmViewActionResources.INSTANCE.clipboardBig());
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
	}
	
	@Override
	protected void updateVisibility() {
		if (modelPaths == null || modelPaths.isEmpty()) {
			setHidden(true, true);
			return;
		}
		
		boolean hidden = true;
		for (List<ModelPath> selection : modelPaths) {
			for (ModelPath modelPath : selection) {
				if (modelPath == null)
					continue;
				
				Object value = modelPath.last().getValue();
				if (value != null && value instanceof GenericEntity) {
					hidden = false;
					break;
				}				
			}
			if (!hidden)
				break;
		}		
		
		setHidden(hidden);		
	}

	@Override
	public void perform(TriggerInfo triggerInfo) {
		if (modelPaths == null || modelPaths.isEmpty())
			return;
		
		String idString = "";
		
		for (List<ModelPath> selection : modelPaths) {
			for (ModelPath modelPath : selection) {
				if (modelPath == null)
					continue;
				
				Object value = modelPath.last().getValue();
				if (value != null && value instanceof GenericEntity) {
					if (!idString.isEmpty())
						idString = idString + ",";
					idString = idString + ((GenericEntity) value).getId();
				}				
			}
		}
		
		ClipboardUtil.copyTextToClipboard(idString);
		GlobalState.showSuccess(LocalizedText.INSTANCE.entityIdCopiedClipboard(idString));
	}
}
