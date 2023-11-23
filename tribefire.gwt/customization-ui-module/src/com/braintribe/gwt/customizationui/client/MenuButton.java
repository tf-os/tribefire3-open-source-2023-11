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
package com.braintribe.gwt.customizationui.client;

import java.util.function.Supplier;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.menu.client.HasAsyncIconProvider;
import com.braintribe.gwt.menu.client.HasTooltipProvider;
import com.google.gwt.user.client.ui.Image;
import com.sencha.gxt.widget.core.client.menu.Menu;

public class MenuButton extends Action implements HasAsyncIconProvider, HasTooltipProvider{
	private Menu menu;
	private Supplier<Future<Image>> iconProvider;
	private Supplier<String> tootipProvider;
	
	public MenuButton() {
		this("Menu");		
	}
	
	public MenuButton(String text) {
		setName(text);		
	}
	
	@Configurable @Required
	public void setMenu(Menu menu) {
		this.menu = menu;
	}
	
	private void showPopup(TriggerInfo triggerInfo) {		
		int x = triggerInfo.getAbsolutLeft();
		int y = triggerInfo.getAbsolutTop();
		int offset = triggerInfo.getOffsetHeight() + 14;		
		menu.showAt(x, y + offset);		
		
	}

	@Override
	public void perform(TriggerInfo triggerInfo) {
		showPopup(triggerInfo);
	}

	@Override
	public void setIconProvider(Supplier<Future<Image>> iconProvider) {
		this.iconProvider = iconProvider;
	}
	
	@Override
	public Supplier<Future<Image>> getIconProvider() {
		return iconProvider;
	}
	
	@Override
	public Supplier<String> getTooltipProvider() {
		return tootipProvider;
	}
	
	@Override
	public void setTooltipProvider(Supplier<String> tooltipProvider) {
		this.tootipProvider = tooltipProvider;	
	}
}
