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
package com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.GXT;
import com.sencha.gxt.fx.client.DragCancelEvent;
import com.sencha.gxt.fx.client.DragEndEvent;
import com.sencha.gxt.fx.client.DragStartEvent;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.DeactivateEvent;

/**
 * Window that is closable (via pressing Esc), and yet doesn't show the close icon.
 * Also, we are fixing the problem when opening a new non modal window when a modal one is in place.
 * Further, it does not bring the window to front when the active one is a {@link NonModalEditorWindow}.
 * Also, prepared for setting resizing = true while dragging.
 * @author michel.docouto
 *
 */
public class ClosableWindow extends Window {
	
	@Override
	protected void onKeyPress(Event event) {
		if (isOnEsc() && event.getKeyCode() == KeyCodes.KEY_ESCAPE) {
			hide();
			return;
		}
		
		super.onKeyPress(event);
	}
	
	@Override
	public void setActive(boolean active) {
		if (!isModal() || active) {
			super.setActive(active);
			return;
		}
		
		/*ModalPanel modalPanel = getModalPanel();
		if (modalPanel != null) {
			ModalPanel.push(modalPanel);
			modalPanel = null;
		}
		hideShadow();*/
		fireEvent(new DeactivateEvent<>(this));
	}
	
	@Override
	public void onBrowserEvent(Event event) {
		switch (event.getTypeInt()) {
			case Event.ONMOUSEDOWN:
				Widget active = manager.getActive();
				if (active instanceof NonModalEditorWindow)
					return;
				break;
		}
		
		super.onBrowserEvent(event);
	}
	
	@Override
	protected void onDragStart(DragStartEvent de) {
		if (GXT.isGecko()) //in FF, we need this so the window is not closed while moving it
			setResizing(true);
		super.onDragStart(de);
	}
	
	@Override
	protected void onDragCancel(DragCancelEvent event) {
		super.onDragCancel(event);
		if (GXT.isGecko()) //in FF, we need this so the window is not closed while moving it
			Scheduler.get().scheduleDeferred(() -> setResizing(false));
	}
	
	@Override
	protected void onDragEnd(DragEndEvent de) {
		super.onDragEnd(de);
		if (GXT.isGecko()) //in FF, we need this so the window is not closed while moving it
			Scheduler.get().scheduleDeferred(() -> setResizing(false));
	}
	
	protected native void setResizing(boolean isResizing) /*-{
		this.@com.sencha.gxt.widget.core.client.Window::resizing = isResizing;
	}-*/;

}
