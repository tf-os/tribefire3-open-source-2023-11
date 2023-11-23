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
package com.braintribe.gwt.gme.assemblypanel.client;

import com.braintribe.gwt.gme.assemblypanel.client.model.AbstractGenericTreeModel;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmMouseInteractionEvent;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyEvent;
import com.google.gwt.event.shared.GwtEvent;
import com.sencha.gxt.widget.core.client.event.BeforeExpandItemEvent;
import com.sencha.gxt.widget.core.client.event.CellClickEvent;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent;

public class AssemblyPanelMouseInteractionEvent implements GmMouseInteractionEvent {
	
	private GwtEvent<?> gridEvent;
	private AssemblyPanel assemblyPanel;
	private NativeEvent nativeEvent;
	private AbstractGenericTreeModel eventModel;
	
	public AssemblyPanelMouseInteractionEvent(GwtEvent<?> gridEvent, AssemblyPanel assemblyPanel) {
		this.gridEvent = gridEvent;
		this.assemblyPanel = assemblyPanel;
	}
	
	public AssemblyPanelMouseInteractionEvent(NativeEvent nativeEvent, AbstractGenericTreeModel eventModel, AssemblyPanel assemblyPanel) {
		this.nativeEvent = nativeEvent;
		this.eventModel = eventModel;
		this.assemblyPanel = assemblyPanel;
	}

	@Override
	public GmContentView getSource() {
		return assemblyPanel;
	}
	
	public GwtEvent<?> getGridEvent() {
		return gridEvent;
	}

	@Override
	public <T> T getElement() {
		AbstractGenericTreeModel model = null;
		if (gridEvent instanceof CellClickEvent)
			model = (AbstractGenericTreeModel) ((CellClickEvent) gridEvent).getSource().getStore().get(((CellClickEvent) gridEvent).getRowIndex());
		else if (gridEvent instanceof CellDoubleClickEvent)
			model = (AbstractGenericTreeModel) ((CellDoubleClickEvent) gridEvent).getSource().getStore().get(((CellDoubleClickEvent) gridEvent).getRowIndex());
		else if (gridEvent instanceof BeforeExpandItemEvent)
			model = (AbstractGenericTreeModel) ((BeforeExpandItemEvent<?>) gridEvent).getItem();
		else if (eventModel != null)
			model = eventModel;
		
		return model == null ? null : (T) AssemblyUtil.getModelPath(model, assemblyPanel.getContentPath());
	}

	@Override
	public int getNativeButton() {
		int charCode = -1;
		if (gridEvent instanceof KeyEvent)
			charCode = ((KeyEvent<?>) gridEvent).getNativeEvent().getCharCode();
		else if (nativeEvent != null)
			charCode = nativeEvent.getCharCode();
		
		return charCode;
	}

	@Override
	public boolean isAltKeyDown() {
		if (gridEvent instanceof KeyEvent)
			return ((KeyEvent<?>) gridEvent).isAltKeyDown();
		if (nativeEvent != null)
			return nativeEvent.getAltKey();
		return false;
	}

	@Override
	public boolean isCtrlKeyDown() {
		if (gridEvent instanceof KeyEvent)
			return ((KeyEvent<?>) gridEvent).isControlKeyDown();
		if (nativeEvent != null)
			return nativeEvent.getCtrlKey();
		return false;
	}

	@Override
	public boolean isShiftKeyDown() {
		if (gridEvent instanceof KeyEvent)
			return ((KeyEvent<?>) gridEvent).isShiftKeyDown();
		if (nativeEvent != null)
			return nativeEvent.getShiftKey();
		return false;
	}

	@Override
	public boolean isMetaKeyDown() {
		if (gridEvent instanceof KeyEvent)
			return ((KeyEvent<?>) gridEvent).isMetaKeyDown();
		if (nativeEvent != null)
			return nativeEvent.getMetaKey();
		return false;
	}

}
