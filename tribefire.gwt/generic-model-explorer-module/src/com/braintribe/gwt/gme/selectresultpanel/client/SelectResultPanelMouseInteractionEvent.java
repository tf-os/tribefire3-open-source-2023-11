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
package com.braintribe.gwt.gme.selectresultpanel.client;

import com.braintribe.gwt.gme.selectresultpanel.client.SelectResultPanel.GridData;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmMouseInteractionEvent;
import com.google.gwt.event.dom.client.KeyEvent;
import com.google.gwt.event.shared.GwtEvent;
import com.sencha.gxt.widget.core.client.event.CellClickEvent;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent;

public class SelectResultPanelMouseInteractionEvent implements GmMouseInteractionEvent {
	
	private GwtEvent<?> gridEvent;
	private SelectResultPanel selectResultPanel;
	
	public SelectResultPanelMouseInteractionEvent(GwtEvent<?> gridEvent, SelectResultPanel selectResultPanel) {
		this.gridEvent = gridEvent;
		this.selectResultPanel = selectResultPanel;
	}

	@Override
	public GmContentView getSource() {
		return selectResultPanel;
	}

	@Override
	public <T> T getElement() {
		GridData model = null;
		int cellIndex = 0;
		if (gridEvent instanceof CellClickEvent) {
			model = (GridData) ((CellClickEvent) gridEvent).getSource().getStore().get(((CellClickEvent) gridEvent).getRowIndex());
			cellIndex = ((CellClickEvent) gridEvent).getCellIndex();
		} else if (gridEvent instanceof CellDoubleClickEvent) {
			model = (GridData) ((CellDoubleClickEvent) gridEvent).getSource().getStore().get(((CellDoubleClickEvent) gridEvent).getRowIndex());
			cellIndex = ((CellDoubleClickEvent) gridEvent).getCellIndex();
		}
		
		if (model != null)
			return (T) selectResultPanel.getModelPath(model, cellIndex);
		return null;
	}

	@Override
	public int getNativeButton() {
		if (gridEvent instanceof KeyEvent)
			((KeyEvent<?>) gridEvent).getNativeEvent().getCharCode();
		return -1;
	}

	@Override
	public boolean isAltKeyDown() {
		if (gridEvent instanceof KeyEvent)
			return ((KeyEvent<?>) gridEvent).isAltKeyDown();
		return false;
	}

	@Override
	public boolean isCtrlKeyDown() {
		if (gridEvent instanceof KeyEvent)
			return ((KeyEvent<?>) gridEvent).isControlKeyDown();
		return false;
	}

	@Override
	public boolean isShiftKeyDown() {
		if (gridEvent instanceof KeyEvent)
			return ((KeyEvent<?>) gridEvent).isShiftKeyDown();
		return false;
	}

	@Override
	public boolean isMetaKeyDown() {
		if (gridEvent instanceof KeyEvent)
			return ((KeyEvent<?>) gridEvent).isMetaKeyDown();
		return false;
	}

}
