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
package com.braintribe.gwt.gmview.util.client;

import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;

public class GMTypeInstanceBean {
	private static Long ID_COUNTER = 0l;
	
	private GenericModelType genericModelType;
	private Object instance;
	private boolean handleInstantiation;
	private Long id;
	
	public GMTypeInstanceBean(GenericModelType genericModelType, Object instance) {
		setId(ID_COUNTER++);
		this.genericModelType = genericModelType;
		this.instance = instance;
	}
	
	public GMTypeInstanceBean(int index) {
		setId(ID_COUNTER++);
		this.genericModelType = GenericModelTypeReflection.TYPE_INTEGER;
		this.instance = index;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public void setHandleInstantiation(boolean handleInstantiation) {
		this.handleInstantiation = handleInstantiation;
	}
	
	public boolean isHandleInstantiation() {
		return handleInstantiation;
	}
	
	public GenericModelType getGenericModelType() {
		return genericModelType;
	}
	
	public void setGenericModelType(GenericModelType genericModelType) {
		this.genericModelType = genericModelType;
	}
	
	public Object getInstance() {
		return instance;
	}
	
	public void setInstance(Object instance) {
		this.instance = instance;
	}
	
}
