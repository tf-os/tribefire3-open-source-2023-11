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
package com.braintribe.gwt.gmview.client;

import java.util.function.Supplier;

import com.google.gwt.user.client.ui.Widget;

public class TabbedWidgetContext implements ViewContext {

	private String name;
	private String description;
	private Supplier<? extends Widget> widgetSupplier;
	protected Widget widget;
	private int index;
	private boolean hideDefaultView;
	
	public TabbedWidgetContext(String name, String description, Supplier<? extends Widget> widgetSupplier) {
		this(name, description, widgetSupplier, -1);
	}

	/**
	 * @param index - The index where the item should be placed in the tabPanel. If -1 is set, then the element is put as in the last position.
	 */
	public TabbedWidgetContext(String name, String description, Supplier<? extends Widget> widgetSupplier, int index) {
		this(name, description, widgetSupplier, index, false);
	}
	
	/**
	 * @param index - The index where the item should be placed in the tabPanel. If -1 is set, then the element is put as in the last position.
	 * @param hideDefaultView - true for hiding the default PP.
	 */
	public TabbedWidgetContext(String name, String description, Supplier<? extends Widget> widgetSupplier, int index, boolean hideDefaultView) {
		super();
		this.name = name;
		this.description = description;
		this.widgetSupplier = widgetSupplier;
		this.index = index;
		this.hideDefaultView = hideDefaultView;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Widget getWidget() {
		if (widget == null)
			widget = widgetSupplier.get();
		
		return widget;
	}

	public Widget getWidgetIfProvided() {
		return widget;
	}

	public void setWidget(Widget widget) {
		this.widget = widget;
	}
	
	public int getIndex() {
		return index;
	}
	
	@Override
	public boolean isHideDefaultView() {
		return hideDefaultView;
	}
}
