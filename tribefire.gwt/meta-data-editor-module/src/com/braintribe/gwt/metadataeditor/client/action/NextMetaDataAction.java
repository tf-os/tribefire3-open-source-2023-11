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
package com.braintribe.gwt.metadataeditor.client.action;

import java.util.Arrays;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.metadata.client.MetaDataEditorPanelHandler;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.metadataeditor.client.resources.MetaDataEditorResources;

@SuppressWarnings("unusable-by-js")
public class NextMetaDataAction extends ModelAction {

	private MetaDataEditorHistory history = null;

	public NextMetaDataAction() {
		setHidden(true);
		setName(LocalizedText.INSTANCE.next());
		setIcon(MetaDataEditorResources.INSTANCE.next2());
		setHoverIcon(MetaDataEditorResources.INSTANCE.next2());
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
	}

	public void setMetaDataHistory(MetaDataEditorHistory history) {
		 this.history = history;
	}
	
	@Override
	public void configureGmContentView(GmContentView gmContentView) {
		this.gmContentView = gmContentView;
	}
	
	@Override
	protected void updateVisibility() {
		boolean useHidden = true;
		if (gmContentView instanceof MetaDataEditorPanelHandler && history.hasNext())
			useHidden = false;
		
		setHidden(useHidden);
	}

	@Override
	public void perform(TriggerInfo triggerInfo) {
		history.next();		
	}

}
