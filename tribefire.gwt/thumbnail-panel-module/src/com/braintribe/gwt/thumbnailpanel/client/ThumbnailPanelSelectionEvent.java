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
package com.braintribe.gwt.thumbnailpanel.client;

import java.util.List;

import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmMouseInteractionEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;

public class ThumbnailPanelSelectionEvent implements GmMouseInteractionEvent {
	
	private SelectionChangedEvent<ImageResourceModelData> event;
	private ThumbnailPanel thumbnailPanel;
	
	public ThumbnailPanelSelectionEvent(SelectionChangedEvent<ImageResourceModelData> event, ThumbnailPanel thumbnailPanel) {
		this.event = event;
		this.thumbnailPanel = thumbnailPanel;
	}

	@Override
	public GmContentView getSource() {
		return this.thumbnailPanel;
	}

	@Override
	public <T> T getElement() {
		ImageResourceModelData model = null;
		List<ImageResourceModelData> models = event.getSelection();
		if (models != null && !models.isEmpty())
			model = models.get(0);
		
		return (T) thumbnailPanel.getModelPath(model);
	}

	@Override
	public int getNativeButton() {
		return 0;
	}

	@Override
	public boolean isAltKeyDown() {
		return false;
	}

	@Override
	public boolean isCtrlKeyDown() {
		return false;
	}

	@Override
	public boolean isShiftKeyDown() {
		return false;
	}

	@Override
	public boolean isMetaKeyDown() {
		return false;
	}

}
