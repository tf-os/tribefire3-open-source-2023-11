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
package com.braintribe.gwt.querymodeleditor.client.panels.editor;

import com.braintribe.model.query.OrderingDirection;
import com.google.gwt.dom.client.ImageElement;

public class QueryOrderItem {
    private String propertyName;
    private String displayName;
    private String orderButtonId;
    private OrderingDirection orderingDirection;
    private ImageElement buttonImageElement;
     
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getOrderButtonId() {
		return orderButtonId;
	}
	public void setOrderButtonId(String orderButtonId) {
		this.orderButtonId = orderButtonId;
	}
	public OrderingDirection getOrderingDirection() {
		return orderingDirection;
	}
	public void setOrderingDirection(OrderingDirection orderingDirection) {
		this.orderingDirection = orderingDirection;
	}
	public ImageElement getButtonImageElement() {
		return buttonImageElement;
	}
	public void setButtonImageElement(ImageElement buttonImageElement) {
		this.buttonImageElement = buttonImageElement;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
     
}
