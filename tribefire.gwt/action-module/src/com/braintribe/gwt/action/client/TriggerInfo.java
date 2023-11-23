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
package com.braintribe.gwt.action.client;

import com.google.gwt.user.client.ui.Widget;

/**
 * This bean can have the properties received when triggering a certain action.
 * @author michel.docouto
 *
 */
public class TriggerInfo extends ActionPropertyHolder implements TriggerKnownProperties {
	
	private Widget widget;
	
	public void setWidget(Widget widget) {
		this.widget = widget;
	}
	
	public Widget getWidget() {
		return widget;
	}
	
	public Integer getAbsolutLeft() {
		return (this.widget == null ? null : this.widget.getAbsoluteLeft());
	}
	
	public Integer getAbsolutTop() {
		return (this.widget == null ? null : this.widget.getAbsoluteTop());
	}
	
	public Integer getOffsetHeight() {
		return (this.widget == null ? null : this.widget.getOffsetHeight());
	}
	
	public Integer getOffsetWidth() {
		return (this.widget == null ? null : this.widget.getOffsetWidth());
	}

}
