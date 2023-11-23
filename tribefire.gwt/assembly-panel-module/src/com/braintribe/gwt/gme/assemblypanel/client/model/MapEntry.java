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
package com.braintribe.gwt.gme.assemblypanel.client.model;

import com.braintribe.model.generic.reflection.GenericModelType;

public class MapEntry {
	
	private Object key;
	private Object object;
	private String keyString;
	private GenericModelType keyElementType;
	private boolean representsKey;
	
	public MapEntry(Object key, Object object, String keyString, GenericModelType keyElementType, boolean representsKey) {
		this.key = key;
		this.object = object;
		this.keyString = keyString;
		this.keyElementType = keyElementType;
		this.representsKey = representsKey;
	}

	public Object getKey() {
		return key;
	}

	public void setKey(Object key) {
		this.key = key;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public String getKeyString() {
		return keyString;
	}

	public void setKeyString(String keyString) {
		this.keyString = keyString;
	}

	public GenericModelType getKeyElementType() {
		return keyElementType;
	}

	public void setKeyElementType(GenericModelType keyElementType) {
		this.keyElementType = keyElementType;
	}
	
	public boolean getRepresentsKey() {
		return representsKey;
	}
	
	public void setRepresentsKey(boolean representsKey) {
		this.representsKey = representsKey;
	}

}
