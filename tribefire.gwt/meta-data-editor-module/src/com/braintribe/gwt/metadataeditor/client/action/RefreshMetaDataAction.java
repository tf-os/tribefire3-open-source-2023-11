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
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.metadata.client.MetaDataEditorPanelHandler;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.metadataeditor.client.MetaDataEditorPanel;
import com.braintribe.gwt.metadataeditor.client.experts.DeclaredOverviewExpert;
import com.braintribe.gwt.metadataeditor.client.experts.DeclaredPropertyOverviewExpert;
import com.braintribe.gwt.metadataeditor.client.experts.InformationOverviewExpert;
import com.braintribe.gwt.metadataeditor.client.experts.MetaDataEditorBaseExpert;
import com.braintribe.gwt.metadataeditor.client.view.MetaDataEditorProvider;

/**
* This action provides a MetaData refresh
*
*/
public class RefreshMetaDataAction extends ModelAction {
	
	//private String useCase;
		
	/*
	@Configurable
	public void setUseCase(String useCase) {
		this.useCase = useCase;
	}
	*/
	
	public RefreshMetaDataAction() {
		setName(LocalizedText.INSTANCE.refreshMetaData());
		setIcon(GmViewActionResources.INSTANCE.refresh());
		setHoverIcon(GmViewActionResources.INSTANCE.refreshBig());
		setHidden(true);
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
	}

	@Override
	protected void updateVisibility() {
		if (this.gmContentView instanceof MetaDataEditorPanelHandler) {
			MetaDataEditorProvider metaDataEditorProvider = ((MetaDataEditorPanel) this.gmContentView).getEditorProvider();
			if (metaDataEditorProvider != null) {
				MetaDataEditorBaseExpert metaDataExpert = metaDataEditorProvider.getModelExpert();
				if (metaDataExpert instanceof DeclaredOverviewExpert || metaDataExpert instanceof DeclaredPropertyOverviewExpert || metaDataExpert instanceof InformationOverviewExpert) {
					setHidden(false);
					return;
				}
			}			
		}				
		setHidden(true);
	}
	

	@Override
	public void perform(TriggerInfo triggerInfo) {
		GlobalState.mask(LocalizedText.INSTANCE.refreshing());
		handleSuccess();		
	}	
	
	private void handleSuccess() {
		GlobalState.unmask();
		
		if (this.gmContentView instanceof MetaDataEditorPanelHandler) {
			MetaDataEditorProvider metaDataEditorProvider = ((MetaDataEditorPanel) this.gmContentView).getEditorProvider();
			if (metaDataEditorProvider != null) {
				if (metaDataEditorProvider.getMetaDataEditorPanel() != null)
					metaDataEditorProvider.getMetaDataEditorPanel().doRefresh();
				else
					metaDataEditorProvider.doRefresh();
			}
		}
	}	
}
