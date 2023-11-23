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

import java.util.ArrayList;
import java.util.List;

import com.braintribe.gwt.gme.constellation.client.BrowsingConstellation;
import com.braintribe.gwt.gme.tetherbar.client.TetherBar;
import com.braintribe.gwt.gme.tetherbar.client.TetherBar.TetherBarListener;
import com.braintribe.gwt.gme.tetherbar.client.TetherBarElement;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.WorkWithEntityActionListener;
import com.braintribe.gwt.gmview.metadata.client.MetaDataEditorPanelHandler;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.metadataeditor.client.MetaDataEditorMaster;
import com.braintribe.model.generic.path.ModelPath;
import com.google.gwt.user.client.ui.Widget;

public class MetaDataEditorHistory {
	
	private List<TetherBar> tetherBarlist = new ArrayList<>();
	private MetaDataEditorMaster master = null;
	
	List<MetaDataEditorHistoryEntry> history = new ArrayList<>();
	int index = -1;

	
	public void setMetaDataEditorMaster(MetaDataEditorMaster master) {
		this.master  = master;
	}
	
	public void add(ModelPath modelPath, Widget widget, String type) {
		if(canAdd(modelPath, type) && widget != null) {
			MetaDataEditorHistoryEntry entry = new MetaDataEditorHistoryEntry();
			entry.widget = widget; 
			entry.tabType = type;
			entry.modelPath = modelPath;
			
			WorkWithEntityActionListener listener = GMEUtil.getWorkWithEntityActionListener(widget);
			if (listener != null) {
				entry.listener = listener;
				if (listener instanceof BrowsingConstellation) {
					entry.tetherBar = ((BrowsingConstellation) listener).getTetherBar();
					if (entry.tetherBar != null && !tetherBarlist.contains(entry.tetherBar)) {
						tetherBarlist.add(entry.tetherBar);
						registerTetherListener(entry.tetherBar);
					}
					if (entry.tetherBar != null)
						entry.tetherBarElement = entry.tetherBar.getSelectedElement();
				}
			}
			if(index > -1) 
				history = history.subList(0, index+1);
			history.add(entry);
			index++;
		}
	}
		
	private void registerTetherListener(TetherBar tetherBar) {
		tetherBar.addTetherBarListener(new TetherBarListener() {
			
			@Override
			public void onTetherBarElementsRemoved(List<TetherBarElement> tetherBarElementsRemoved) {
				// NOPE			
			}
			
			@Override
			public void onTetherBarElementSelected(TetherBarElement tetherBarElement) {
				GmContentView gmContentView = tetherBarElement.getContentViewIfProvided();
				if (gmContentView.getView() instanceof MetaDataEditorPanelHandler)					
					master.addHistory(MetaDataEditorHistory.this, gmContentView.getView());				
			}
			
			@Override
			public void onTetherBarElementAdded(TetherBarElement tetherBarElementAdded) {
				// NOPE				
			}
		});
		
	}

	private boolean canAdd(ModelPath modelPath, String type) {
		if (modelPath == null)
			return false;
		
		if(history.size() > 0 && history.size() > index) {
			MetaDataEditorHistoryEntry entry = history.get(index);
			return !((entry.tabType.equalsIgnoreCase(type)) && (entry.modelPath.equals(modelPath)));
		}
		return true;
	}
	
	public void next() {
		if(hasNext()) {
			index += 1;
			MetaDataEditorHistoryEntry entry = history.get(index);
			
			if (master != null)
				master.showHistory(entry);
		}
	}
	
	public void previous(boolean remove) {
		if(hasPrevious()) {
			index -= 1;
			MetaDataEditorHistoryEntry entry = history.get(index);
			
			if(remove) {
				history = history.subList(0, index+1);
			}
			if (master != null)
				master.showHistory(entry);
		}
	}
	
	public boolean hasNext() {
		return index + 1 < history.size();
	}
	
	public boolean hasPrevious() {
		return index - 1 >= 0 && history.size() >= (index - 1);
	}

}
