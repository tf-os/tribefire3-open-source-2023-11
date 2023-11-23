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
import com.google.gwt.resources.client.ImageResource;

public class GlobalAction {
	private Action action;
	private ImageResource icon;
	private ImageResource hoverIcon;
	private String description;
	private String knownName;
	
	public GlobalAction(String knownName) {
		this.knownName = knownName;
	}
	
	public Action getAction() {
		return action;
	}
	public void setAction(Action action) {
		this.action = action;
	}
	public ImageResource getIcon() {
		return icon;
	}
	public void setIcon(ImageResource icon) {
		this.icon = icon;
	}
	public ImageResource getHoverIcon() {
		return hoverIcon;
	}
	public void setHoverIcon(ImageResource hoverIcon) {
		this.hoverIcon = hoverIcon;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getKnownName() {
		return knownName;
	}
	
	public void setKnownName(String knownName) {
		this.knownName = knownName;
	}

}
