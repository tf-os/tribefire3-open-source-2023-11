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

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.action.client.TriggerKnownProperties;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gmview.action.client.LocalizedText;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

@SuppressWarnings("unusable-by-js")
public class ContentMenuAction extends ModelAction {
	
	private Menu menu;
	//private Menu actionsContextMenu;
	//private DefaultGmContentViewActionManager actionManager;

	public ContentMenuAction() {
		setHidden(false);
		setStyleName("ContentMenuAction");
		updateNameAndIcons();
	}	
	
	//public void setActionManager(DefaultGmContentViewActionManager actionManager) {
	//	this.actionManager = actionManager;
	//}		
	
	@Override
	public void configureGmContentView(GmContentView gmContentView) {
		super.configureGmContentView(gmContentView);
		//setHidden(gmContentView == null);
		updateVisibility();
	}	
	
	@Override
	protected void updateVisibility() {
			boolean isHidden = ((gmContentView == null) || (this.menu == null));
			
			if (!isHidden) {
				isHidden = !updateActionMenu(menu);
				/*
				isHidden = true;
				for (Widget widget : menu) {
					
					if (widget instanceof MenuItem)  {
						if (((MenuItem) widget).isVisible()) {
							isHidden = false;
							break;
						}
					} else if (widget.isVisible() && !(widget instanceof SeparatorMenuItem)) {
						isHidden = false;
						break;
					}				 
				}
				*/
			}
			
			setHidden(isHidden, true);
	}

	private boolean updateActionMenu(Menu menu) {
		boolean isMenuVisible = false;
		for (Widget item : menu) {	
			boolean isItemHidden = true;
			if (item instanceof MenuItem) {								
				Action action = ((MenuItem) item).getData("action");
				if (action != null) {
					isItemHidden = action.getHidden();
					item.setVisible(!isItemHidden);
				} else if (((MenuItem) item).getSubMenu() != null) {
					//check when Item have subMenu		
					//check recursive the subMenu Items
					isItemHidden = !updateActionMenu(((MenuItem) item).getSubMenu());
				} 
			} 
			
			isMenuVisible = (isMenuVisible || !isItemHidden);
		}
		
		return isMenuVisible;
	}	
	
	
	@Override
	public void perform(TriggerInfo triggerInfo) {
		if (gmContentView == null)
			return;

		/*
		Widget contextMenu = gmContentView.getContextMenu();		
		if (contextMenu == null) {
			return;
		} else {
		}
		
		if (!(contextMenu instanceof Menu)) {
			return;
		}
		*/
				
		Element element = null;
		
		Object object = triggerInfo.get(TriggerKnownProperties.PROPERTY_CLICKEDELEMENT);
		if (object != null && object instanceof Element)
			element = (Element) object;
		
		if (element != null && menu != null)	
			menu.showAt(element.getAbsoluteLeft(), element.getAbsoluteBottom() + 5);
			//((Menu) contextMenu).showAt(element.getAbsoluteLeft(), element.getAbsoluteBottom()+5);
	}
	
	public void setMenu(Menu menu) {
		this.menu = menu;
	}
	
	public Menu getMenu() {
		return this.menu;
	}
	
	private void updateNameAndIcons() {
		setTooltip(LocalizedText.INSTANCE.actions());
		setIcon(ConstellationResources.INSTANCE.more64());
	}	
}

