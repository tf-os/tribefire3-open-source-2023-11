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
package com.braintribe.gwt.gme.constellation.client.action;

import java.util.List;

import com.braintribe.gwt.action.adapter.gxt.client.ButtonActionAdapter;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.gme.constellation.client.GlobalActionsToolBar;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class CancelGlobalActionsToolBar extends GlobalActionsToolBar {
	
	private List<Action> externalActions;
	
	@Configurable
	public void setExternalActions(List<Action> externalActions) {
		this.externalActions = externalActions;
	}
	
	@Override
	public void intializeBean() throws Exception {
		add(prepareToolBar());
	}
	
	private ToolBar prepareToolBar() {
		ToolBar toolBar = new ToolBar();
		toolBar.add(new FillToolItem());
		//toolBar.setAlignment(HorizontalAlignment.RIGHT);
		toolBar.getElement().getStyle().setBackgroundColor("white");
		toolBar.getElement().getStyle().setBackgroundImage("none");
		toolBar.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
		
		if (externalActions != null)
			externalActions.forEach(action -> toolBar.add(createButton(action)));
		
		toolBar.add(createCloseButton());
		
		toolBar.setBorders(false);
		return toolBar;
	}
	
	private TextButton createCloseButton() {
		TextButton button = new TextButton();
		button.setScale(ButtonScale.LARGE);
		button.setIconAlign(IconAlign.TOP);
		button.setText(LocalizedText.INSTANCE.close());
		button.setIcon(ConstellationResources.INSTANCE.cancel());
		button.addSelectHandler(event -> getWindow(CancelGlobalActionsToolBar.this).hide());
		
		return button;
	}
	
	private Window getWindow(Widget widget) {
		if (widget instanceof Window)
			return (Window) widget;
		else
			return getWindow(widget.getParent());
	}
	
	private TextButton createButton(Action action) {
		TextButton button = new TextButton();
		button.setScale(ButtonScale.LARGE);
		button.setIconAlign(IconAlign.TOP);
		ButtonActionAdapter.linkActionToButton(true, action, button);
		return button;
	}

}
