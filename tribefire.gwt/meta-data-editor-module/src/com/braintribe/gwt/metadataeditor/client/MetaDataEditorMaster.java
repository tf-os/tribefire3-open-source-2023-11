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
package com.braintribe.gwt.metadataeditor.client;

import java.util.HashSet;
import java.util.Set;

import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.metadataeditor.client.action.MetaDataEditorHistory;
import com.braintribe.gwt.metadataeditor.client.action.MetaDataEditorHistoryEntry;

public class MetaDataEditorMaster {
	
	private final Set<MetaDataEditorPanel> panelList = new HashSet<MetaDataEditorPanel>();
	public void addPanel(MetaDataEditorPanel panel)	{
		if (!panelList.contains(panel))
			panelList.add(panel);
	}
	
	public void removePanel(MetaDataEditorPanel panel) {
		if (panelList.contains(panel))
			panelList.remove(panel);
	}
	
	public void showHistory(MetaDataEditorHistoryEntry entry) {
		for (MetaDataEditorPanel panel : panelList) {
			if (panel.getContentPath().equals(entry.modelPath)) {
				if ((!panel.isVisible() && (entry.tetherBar != null) && (entry.tetherBarElement != null))) {
					entry.tetherBar.setSelectedThetherBarElement(entry.tetherBarElement);
				} else {				
					panel.showTab(entry.tabType);
				}
				return;
			}
		}
	}
	
	public void addHistory(MetaDataEditorHistory history, GmContentView gmView) {
		if (gmView instanceof MetaDataEditorPanel)
			history.add(((MetaDataEditorPanel) gmView).getContentPath(), ((MetaDataEditorPanel) gmView).getActiveEditor().asWidget(), ((MetaDataEditorPanel) gmView).getActiveEditor().getCaption());
			
	}
}
