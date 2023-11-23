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
package com.braintribe.gwt.gmview.action.client;

import com.braintribe.model.meta.GmType;

public class ObjectAndType {
	
	private Object object;
	private GmType type;
	private String description;
	private boolean isServiceRequest;
	
	public ObjectAndType(Object object, GmType type, String description) {
		this.object = object;
		this.type = type;
		this.description = description;
	}
	
	public ObjectAndType(GmType type, String description, boolean isServiceRequest) {
		this.type = type;
		this.description = description;
		this.isServiceRequest = isServiceRequest;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public GmType getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}
	
	public boolean isServiceRequest() {
		return isServiceRequest;
	}

}
