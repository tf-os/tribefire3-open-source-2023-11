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

import java.util.Comparator;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.prompt.VirtualEnum;

/**
 * Model used for the property columns in the tree.
 * 
 * @author michel.docouto
 *
 */
public class TreePropertyModel implements AbsentModel {
	private static Comparator<TreePropertyModel> priorityComparator;

	protected Double priority;
	protected String propertyDisplay;
	protected Property property;
	protected String propertyName;
	protected Object value;
	protected GenericModelType elementType;
	protected GenericEntity parentEntity;
	protected boolean readOnly;
	protected boolean password;
	protected boolean absent;
	protected boolean nullable;
	protected String label;
	protected VirtualEnum virtualEnum;
	protected Property embeddedProperty;
	//protected boolean baseTyped;

	public TreePropertyModel(Property property, GenericEntity parentEntity, Double priority, boolean readOnly, boolean password,
			boolean absent, String propertyDisplay, VirtualEnum virtualEnum/*, boolean baseTyped*/) {
		this.property = property;
		
		if (parentEntity != null) {
			Property foundProperty = parentEntity.entityType().findProperty(property.getName());
			if (foundProperty != null) {
				property = foundProperty;
				this.value = property.get(parentEntity);
			} else
				this.value = null;
		}
		
		this.elementType = property.getType().getActualType(value);
		this.propertyName = property.getName();
		this.parentEntity = parentEntity;
		this.priority = priority;
		this.readOnly = readOnly;
		this.password = password;
		this.absent = absent;
		this.propertyDisplay = propertyDisplay;
		this.nullable = property.isNullable();
		this.virtualEnum = virtualEnum;
		//this.baseTyped = baseTyped;
	}
	
	public static TreePropertyModel createClone(GenericEntity parentEntity, TreePropertyModel model) {
		if (model.getEmbeddedProperty() != null)
			parentEntity = model.getEmbeddedProperty().get(parentEntity);
		
		Property property = parentEntity.entityType().findProperty(model.propertyName);
		if (property == null || !property.getType().equals(model.property.getType()))
			return null;
		
		return new TreePropertyModel(property, parentEntity, model.priority, model.readOnly, model.password, model.absent,
				model.propertyDisplay, model.virtualEnum);
	}

	@Override
	public void setAbsent(boolean absent) {
		this.absent = absent;
	}

	@Override
	public boolean isAbsent() {
		return absent;
	}

	public Property getProperty() {
		return property;
	}
	
	public Property getNormalizedProperty() {
		return property;
	}
	
	public String getPropertyName() {
		return propertyName;
	}
	
	public Object getValue() {
		return value;
	}

	public String getNormalizedPropertyName() {
		return propertyName;
	}
	
	public GenericEntity getParentEntity() {
		return parentEntity;
	}
	
	public boolean isNullable() {
		return nullable;
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}
	
	public GenericModelType getElementType() {
		return elementType;
	}
	
	public boolean isPassword() {
		return password;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public VirtualEnum getVirtualEnum() {
		return virtualEnum;
	}
	
	public void setEmbeddedProperty(Property embeddedProperty) {
		this.embeddedProperty = embeddedProperty;
	}
	
	public Property getEmbeddedProperty() {
		return embeddedProperty;
	}

	public static Comparator<TreePropertyModel> getPriorityComparator(final boolean priorityReverse) {
		if (priorityComparator != null)
			return priorityComparator;
		
		priorityComparator = (o1, o2) -> {
			int priorityComparison;
			if (o1.priority == null && o2.priority == null)
				priorityComparison = 0;
			else if (o1.priority == null && o2.priority != null)
				priorityComparison = -1;
			else if (o1.priority != null && o2.priority == null)
				priorityComparison = 1;
			else {
				if (priorityReverse)
					priorityComparison = o1.priority.compareTo(o2.priority);
				else
					priorityComparison = o2.priority.compareTo(o1.priority);
			}
			
			if (priorityComparison == 0 && o1.propertyDisplay != null)
				return o1.propertyDisplay.compareToIgnoreCase(o2.propertyDisplay);
			
			return priorityComparison;
		};

		return priorityComparator;
	}

}
