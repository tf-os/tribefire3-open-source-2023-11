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
package com.braintribe.gwt.gxt.gxtresources.orangemenuitem.client;

import com.braintribe.gwt.gxt.gxtresources.orangemenuitem.client.OrangeMenuItemAppearance.OrangeMenuItemStyle;
import com.google.gwt.core.client.GWT;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.theme.blue.client.menu.BlueCheckMenuItemAppearance;

public class OrangeCheckMenuItemAppearance extends BlueCheckMenuItemAppearance {
	
	public interface OrangeCheckMenuItemStyle extends BlueCheckMenuItemStyle {
		//NOP
	}
	
	public interface OrangeCheckMenuItemResources extends BlueCheckMenuItemResources {
		
		@Override
		@Source({"com/sencha/gxt/theme/base/client/menu/Item.gss", "com/sencha/gxt/theme/blue/client/menu/BlueItem.gss", "com/sencha/gxt/theme/base/client/menu/MenuItem.gss",
				"com/sencha/gxt/theme/blue/client/menu/BlueMenuItem.gss", "OrangeMenuItem.gss"})
		OrangeMenuItemStyle style();
		
		@Override
		@Source({"com/sencha/gxt/theme/base/client/menu/CheckMenuItem.gss", "com/sencha/gxt/theme/blue/client/menu/BlueCheckMenuItem.gss", "OrangeCheckMenuItem.gss"})
		OrangeCheckMenuItemStyle checkStyle();
	}
	
	public OrangeCheckMenuItemAppearance() {
		super(GWT.<OrangeCheckMenuItemResources>create(OrangeCheckMenuItemResources.class), GWT.<MenuItemTemplate> create(MenuItemTemplate.class));
	}
	
	@Override
	public void onActivate(XElement parent) {
		super.onActivate(parent);
		parent.addClassName("x-menu-item-active");
	}
	
	@Override
	public void onDeactivate(XElement parent) {
		super.onDeactivate(parent);
		parent.removeClassName("x-menu-item-active");
	}

}
