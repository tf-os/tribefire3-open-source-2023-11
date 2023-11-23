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


public class TabbedGmEntityViewContext {
	
	private String name;
	private String description;
	private GmEntityView entityView;	
	
	public TabbedGmEntityViewContext (String name, String description,GmEntityView entityView) {
		super();
		this.name = name;
		this.description = description;
		this.entityView = entityView;
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
	public GmEntityView getEntityView() {
		return entityView;
	}
	public void setEntityView(GmEntityView entityView) {
		this.entityView = entityView;
	}
}
