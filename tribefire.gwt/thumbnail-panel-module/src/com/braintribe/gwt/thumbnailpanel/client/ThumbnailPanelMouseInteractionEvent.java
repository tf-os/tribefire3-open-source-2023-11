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

import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmMouseInteractionEvent;
import com.google.gwt.event.dom.client.KeyEvent;
import com.google.gwt.event.shared.GwtEvent;
import com.sencha.gxt.widget.core.client.event.BeforeExpandItemEvent;
import com.sencha.gxt.widget.core.client.event.CellClickEvent;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent;

public class ThumbnailPanelMouseInteractionEvent implements GmMouseInteractionEvent {
	
	private GwtEvent<?> listEvent;
	private ThumbnailPanel thumbnailPanel;
	
	public ThumbnailPanelMouseInteractionEvent(GwtEvent<?> event, ThumbnailPanel thumbnailPanel) {
		this.listEvent = event;
		this.thumbnailPanel = thumbnailPanel;
	}

	@Override
	public GmContentView getSource() {
		return this.thumbnailPanel;
	}

	@Override
	public <T> T getElement() {
		ImageResourceModelData model = null;
		if (listEvent instanceof CellClickEvent)
			model = (ImageResourceModelData) ((CellClickEvent) listEvent).getSource().getStore().get(((CellClickEvent) listEvent).getRowIndex());
		else if (listEvent instanceof CellDoubleClickEvent)
			model = (ImageResourceModelData) ((CellDoubleClickEvent) listEvent).getSource().getStore().get(((CellDoubleClickEvent) listEvent).getRowIndex());
		else if (listEvent instanceof BeforeExpandItemEvent)
			model = (ImageResourceModelData) ((BeforeExpandItemEvent<?>) listEvent).getItem();
		
		if (model != null)
			return (T)thumbnailPanel.getModelPath(model);
		return null;
	}

	@Override
	public int getNativeButton() {
		if (listEvent instanceof KeyEvent)
			((KeyEvent<?>) listEvent).getNativeEvent().getCharCode();
		return -1;
	}

	@Override
	public boolean isAltKeyDown() {
		if (listEvent instanceof KeyEvent)
			return ((KeyEvent<?>) listEvent).isAltKeyDown();
		return false;
	}

	@Override
	public boolean isCtrlKeyDown() {
		if (listEvent instanceof KeyEvent)
			return ((KeyEvent<?>) listEvent).isControlKeyDown();
		return false;
	}

	@Override
	public boolean isShiftKeyDown() {
		if (listEvent instanceof KeyEvent)
			return ((KeyEvent<?>) listEvent).isShiftKeyDown();
		return false;
	}

	@Override
	public boolean isMetaKeyDown() {
		if (listEvent instanceof KeyEvent)
			return ((KeyEvent<?>) listEvent).isMetaKeyDown();
		return false;
	}

}
