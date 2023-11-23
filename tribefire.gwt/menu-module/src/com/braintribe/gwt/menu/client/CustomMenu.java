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
package com.braintribe.gwt.menu.client;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;

/**
 * Component responsible for creating a menu panel.
 */
public class CustomMenu extends VerticalLayoutContainer implements InitializableBean {
	private MenuPanel topPanel;
	private MenuPanel bottomPanel;
	private int itemWidth = 99;
	private int itemHeight = 19;
	private Iterable<? extends Action> topActions;
	private Iterable<? extends Action> bottomActions;
	
	public CustomMenu() {
		topPanel = new MenuPanel();
		bottomPanel = new MenuPanel();
	}
	
	@Override
	public void intializeBean() throws Exception {
		setBorders(false);
		topPanel.setItemHeight(itemHeight);
		topPanel.setItemWidth(itemWidth);
		topPanel.setBorders(false);
		
		bottomPanel.setItemHeight(itemHeight);
		bottomPanel.setItemWidth(itemWidth);
		bottomPanel.setBorders(false);
		
		if (topActions != null)
			topActions.forEach(action -> topPanel.addAction(action));
		
		if (bottomActions != null)
			bottomActions.forEach(action -> bottomPanel.addAction(action));
	}

	public MenuPanel getTopPanel() {
		return topPanel;
	}

	public MenuPanel getBottomPanel() {
		return bottomPanel;
	}
	
	@Configurable
	public void setTopActions(Iterable<? extends Action> actions) {
		this.topActions = actions;
		add(topPanel);
	}
	
	@Configurable
	public void setBottomActions(Iterable<? extends Action> actions) {
		this.bottomActions = actions;
		
		add(bottomPanel);
	}
	
	/**
	 * Configures the width for the created items.
	 * Defaults to 99.
	 */
	@Configurable
	public void setItemWidth(int itemWidth) {
		this.itemWidth = itemWidth;
	}
	
	/**
	 * Configures the height for the created items.
	 * Defaults to 19.
	 */
	@Configurable
	public void setItemHeight(int itemHeight) {
		this.itemHeight = itemHeight;
	}
}
