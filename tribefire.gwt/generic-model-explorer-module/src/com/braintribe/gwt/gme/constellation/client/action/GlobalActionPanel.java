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
import java.util.Map;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.KnownProperties;
import com.braintribe.gwt.gme.constellation.client.expert.GlobalActionsHandler;
import com.braintribe.gwt.gme.constellation.client.expert.GlobalActionsListener;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gme.headerbar.client.HeaderBarActionButton;
import com.braintribe.gwt.gme.headerbar.client.HeaderBarButton;
import com.braintribe.gwt.gme.headerbar.client.HeaderBarButtonListener;
import com.braintribe.gwt.gmview.action.client.ActionWithMenu;
import com.braintribe.gwt.htmlpanel.client.HtmlPanel;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.model.generic.reflection.EntityType;
import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.widget.core.client.menu.Menu;

public class GlobalActionPanel extends HtmlPanel implements GlobalActionsListener {

	private GlobalActionsHandler globalActionsHandler;
	private Map<String, HeaderBarButton> mapHeaderBarButtons = new FastMap<>();
	private int buttonId = 1;

	public GlobalActionPanel() {
		this.setBodyBorder(false);
		this.setBorders(false);
		this.setHeaderVisible(false);
		this.setBodyStyleName("globalActionPanelBody");
		this.addStyleName("globalActionPanel");		
	}

	@Configurable
	public void setGlobalActionsHandler(GlobalActionsHandler globalActionshandler) {
		this.globalActionsHandler = globalActionshandler;
		if (this.globalActionsHandler != null) {
			this.globalActionsHandler.addGlobalActionListener(this);
			this.globalActionsHandler.setDestinationPanelForWorkbenchAction(this);
		}
	}	
	
	@Override
	public void onGlobalActionsPrepared() { 
		if (this.globalActionsHandler != null)
			this.globalActionsHandler.prepareListeners();
		prepareGlobalActions();
	}

	@Override
	public void onEntityTypeChanged(EntityType<?> entityType) {
		//NOP
	}
	
	private void prepareGlobalActions() {
		mapHeaderBarButtons.clear();
		StringBuilder builder = new StringBuilder();
		builder.append("<div class='globalActionPanelGroup'>");
		
		List<GlobalAction> listGlobalAction = globalActionsHandler.getConfiguredActions(false);
		
		for (GlobalAction globalAction : listGlobalAction) {
			Action action = globalAction.getAction();
					
			HeaderBarActionButton button = new HeaderBarActionButton();
			button.setAction(action);
			button.linkActionWithButton();					
			button.addDisabledActionProperty(KnownProperties.PROPERTY_NAME);
			button.addDisabledActionProperty(KnownProperties.PROPERTY_HOVERICON);
			button.addDisabledActionProperty(KnownProperties.PROPERTY_ICON);
			button.addDisabledActionProperty(KnownProperties.PROPERTY_TOOLTIP);
			
			button.setId(action.getId());			
			//if (globalAction.getHoverIcon() != null)
			//	button.setImageUrl(globalAction.getHoverIcon().getSafeUri().asString());
			if (globalAction.getIcon() != null)
				button.setImageUrl(globalAction.getIcon().getSafeUri().asString());
			button.setTooltip(globalAction.getDescription());
			button.setText(action.getName());
			button.setMenuImageUrl(ConstellationResources.INSTANCE.arrowDownSmall().getSafeUri().asString());
			button.setData("action", action);
			
			Menu menu = null;
			if (action instanceof ActionWithMenu) 
				menu = ((ActionWithMenu) action).getActionMenu();
			else 
				menu = globalActionsHandler.getActionMenu(action);
			
			if (menu != null) {
				menu.addStyleName("headerBarMenu");
				menu.setShadow(false);
				button.setMenu(menu);
				button.setUseMenuButton(true);
			}
			
			button.addListener(new HeaderBarButtonListener() {			
				@Override
				public void onMouseOverButton(HeaderBarButton button) {
					//NOP				
				}
				
				@Override
				public void onMouseOutButton(HeaderBarButton button) {
					//NOP				
				}
				
				@Override
				public void onClickButton(HeaderBarButton button) {
					Action action = button.getData("action");
					handleActionClicked(action);
				}
			});
					
			String buttonIdString = "global-action-button-id-" + buttonId; 
			builder.append("<div class='global-action-button' id='").append(buttonIdString).append("'>").append("</div>");
			
			mapHeaderBarButtons.put(buttonIdString, button);									
			buttonId++;
		}
				
		builder.append("</div>");		
		this.setHtml(builder.toString());
		
		for (Map.Entry<String, HeaderBarButton> entry : mapHeaderBarButtons.entrySet()) {
			if (entry.getValue() != null)
				addWidget(entry.getKey(), entry.getValue());
		}	
		
		this.init();				
	}
	
	private static void handleActionClicked(Action action) {
		if (action != null)
			action.perform(null);
	}
	
	public boolean isEmpty() {
		return mapHeaderBarButtons.isEmpty();
	}
}
