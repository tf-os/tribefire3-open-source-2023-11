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


public class ValueTreeModel extends AbstractGenericTreeModel {
	
	private GenericModelType valueType;
	
	/**
	 * Instantiates a new ValueTreeModel.
	 */
	public ValueTreeModel(ObjectAndType objectAndType) {
		valueType = objectAndType.getType();
		setModelObject(objectAndType.getObject(), objectAndType.getDepth());
	}

	@Override
	public AbstractGenericTreeModel getDelegate() {
		return this;
	}

	@Override
	public <X extends GenericModelType> X getElementType() {
		return (X) valueType;
	}
	

}
