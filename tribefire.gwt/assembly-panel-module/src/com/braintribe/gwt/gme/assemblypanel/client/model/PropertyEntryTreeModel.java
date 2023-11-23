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

import java.util.function.Function;

import com.braintribe.model.generic.reflection.GenericModelException;


public class PropertyEntryTreeModel extends DelegatingTreeModel implements PropertyEntryModelInterface {
	
	protected PropertyEntry propertyEntry;
	private boolean mandatory;
	private Double priority;
	private boolean editable;
	
	protected PropertyEntryTreeModel() {}
	
	public PropertyEntryTreeModel(ObjectAndType objectAndType, Function<ObjectAndType, ? extends AbstractGenericTreeModel> modelFactory)
			throws GenericModelException {
		ObjectAndType subObjectAndType = new ObjectAndType();
		propertyEntry = (PropertyEntry)objectAndType.getObject();
		subObjectAndType.setObject(propertyEntry.getPropertyValue());
		subObjectAndType.setType(propertyEntry.getPropertyType());
		subObjectAndType.setDepth(propertyEntry.getDepth());
		subObjectAndType.setMapAsList(propertyEntry.getMapAsList());
		subObjectAndType.setMaxSize(propertyEntry.getMaxSize());
		delegate = modelFactory.apply(subObjectAndType);
		delegate.setProperty(propertyEntry.getProperty());
	}
	
	@Override
	public <X> X get(String property) {
		return (X)delegate.get(property);
	}
	
	@Override
	public PropertyEntry getPropertyEntry() {
		return propertyEntry;
	}
	
	@Override
	public AbstractGenericTreeModel getPropertyDelegate() {
		return delegate;
	}
	
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
	
	public boolean getMandatory() {
		return mandatory;
	}
	
	public void setPriority(Double priority) {
		this.priority = priority;
	}
	
	public Double getPriority() {
		return priority;
	}
	
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	
	public boolean isEditable() {
		return editable;
	}

}
