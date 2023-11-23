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
package com.braintribe.gwt.metadataeditor.client.view;

import java.util.Comparator;

import com.braintribe.gwt.gme.propertypanel.client.PropertyModel;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.data.display.Group;
import com.braintribe.utils.i18n.I18nTools;

public class MetaDataEditorResolutionModel {
	private static Long lastId = 0l;
	private static Comparator<PropertyModel> priorityComparator;
	private static Comparator<PropertyModel> groupPriorityComparator;
	
	private Group propertyGroup;
	private GenericModelType valueElementType;
	private boolean editable;
	private String propertyName;
	private String flowDisplay;
	private String valueDisplay;
	private boolean password;
	private boolean absent;
	private boolean nullable;
	private boolean baseTyped;
	private String description;
	private GenericEntity parentEntity;
	private EntityType<GenericEntity> parentEntityType;
	private Double groupPriority;
	private String groupName;
	private String displayName;
	private Double priority;
	private boolean mandatory;
	private boolean flow;
	private boolean flowExpanded;
	private Object value;
	private Long id;
	
	public MetaDataEditorResolutionModel() {
		setId(lastId++);
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getId() {
		return this.id;
	}
	
	public void setGroupPriority(Double groupPriority){
		this.groupPriority = groupPriority;
	}
	
	public Double getGroupPriority(){
		return this.groupPriority;
	}
	
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	public String getGroupName() {
		return this.groupName;
	}
	
	public void setPropertyGroup(Group propertyGroup) {
		this.propertyGroup = propertyGroup;
		if (propertyGroup != null) {
			setGroupName(propertyGroup.getLocalizedName() != null ? I18nTools.getLocalized(propertyGroup.getLocalizedName()) : propertyGroup.getName());
		}
	}
	
	public Group getPropertyGroup() {
		return this.propertyGroup;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void setPriority(Double priority) {
		this.priority = priority;
	}
	
	public Double getPriority() {
		return this.priority;
	}
	
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
	
	public boolean getMandatory() {
		return this.mandatory;
	}
	
	public void setFlow(boolean flow) {
		this.flow = flow;
	}
	
	public boolean getFlow() {
		return this.flow;
	}
	
	public void setFlowExpanded(boolean flowExpanded) {
		this.flowExpanded = flowExpanded;
	}
	
	public boolean getFlowExpanded() {
		return this.flowExpanded;
	}
	
	public <X> void setValue(X value) {
		this.value = value;
	}
	
	public <X> X getValue() {
		return (X) this.value;
	}
	
	public void setValueElementType(GenericModelType valueElementType) {
		this.valueElementType = valueElementType;
	}
	
	public GenericModelType getValueElementType() {
		return this.valueElementType;
	}
	
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	
	public boolean isEditable() {
		return this.editable;
	}
	
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	
	public String getPropertyName() {
		return this.propertyName;
	}
	
	public void setFlowDisplay(String flowDisplay) {
		this.flowDisplay = flowDisplay;
	}
	
	public String getFlowDisplay() {
		return this.flowDisplay;
	}
	
	public void setValueDisplay(String valueDisplay) {
		this.valueDisplay = valueDisplay;
	}
	
	public String getValueDisplay() {
		return this.valueDisplay;
	}
	
	public void setPassword(boolean password) {
		this.password = password;
	}
	
	public boolean getPassword() {
		return this.password;
	}
	
	public void setAbsent(boolean absent) {
		this.absent = absent;
	}
	
	public boolean getAbsent() {
		return this.absent;
	}
	
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}
	
	public boolean isNullable() {
		return this.nullable;
	}
	
	public void setBaseTyped(boolean baseTyped) {
		this.baseTyped = baseTyped;
	}
	
	public boolean isBaseTyped() {
		return this.baseTyped;
	}
	
	public GenericEntity getParentEntity() {
		return this.parentEntity;
	}
	
	public void setParentEntity(GenericEntity parentEntity) {
		this.parentEntity = parentEntity;
	}
	
	public EntityType<GenericEntity> getParentEntityType() {
		return this.parentEntityType;
	}
	
	public void setParentEntityType(EntityType<GenericEntity> parentEntityType) {
		this.parentEntityType = parentEntityType;
	}
	
	public static Comparator<PropertyModel> getPriorityComparator(final boolean priorityReverse) {
		if (priorityComparator == null) {
			priorityComparator = new Comparator<PropertyModel>() {
				@Override
				public int compare(PropertyModel o1, PropertyModel o2) {
					int priorityComparison;
					if (o1.getPriority() == null && o2.getPriority() == null)
						priorityComparison = 0;
					else if (o1.getPriority() == null && o2.getPriority() != null)
						priorityComparison = 1;
					else if (o1.getPriority() != null && o2.getPriority() == null)
						priorityComparison = -1;
					else {
						if (priorityReverse)
							priorityComparison = o1.getPriority().compareTo(o2.getPriority());
						else
							priorityComparison = o2.getPriority().compareTo(o1.getPriority());
					}
					if (priorityComparison == 0) {
						if (o1.getFlow() == o2.getFlow())
							return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
						else
							return ((Boolean) o1.getFlow()).compareTo(o2.getFlow());
					}
					return priorityComparison;
				}
			};
		}
		
		return priorityComparator;
	}
	
	public static Comparator<PropertyModel> getGroupPriorityComparator(final boolean priorityReverse) {
		if (groupPriorityComparator == null) {
			groupPriorityComparator = new Comparator<PropertyModel>() {
				@Override
				public int compare(PropertyModel o1, PropertyModel o2) {
					int priorityComparison;
					if (o1.getGroupPriority() == null && o2.getGroupPriority() == null)
						priorityComparison = 0;
					else if (o1.getGroupPriority() == null && o2.getGroupPriority() != null)
						priorityComparison = 1;
					else if (o1.getGroupPriority() != null && o2.getGroupPriority() == null)
						priorityComparison = -1;
					else {
						if (priorityReverse)
							priorityComparison = o1.getGroupPriority().compareTo(o2.getGroupPriority());
						else
							priorityComparison = o2.getGroupPriority().compareTo(o1.getGroupPriority());
					}
					if (priorityComparison == 0) {
						//if (o1.getFlow() == o2.getFlow())
							return o1.getGroupName().compareTo(o2.getGroupName());
						//else
							//return ((Boolean) o1.getFlow()).compareTo(o2.getFlow());
					}
					return priorityComparison;
				}
			};
		}
		
		return groupPriorityComparator;
	}
	
	/*public static class PropertyModelSorter extends StoreSorter<PropertyModel> {
		private boolean priorityReverse;
		
		public PropertyModelSorter(boolean priorityReverse) {
			this.priorityReverse = priorityReverse;
		}
		
		@Override
		public int compare(Store<PropertyModel> store, PropertyModel m1, PropertyModel m2, String property) {
			if (PRIORITY_PROPERTY.equals(property))
				return getPriorityComparator(priorityReverse).compare(m1, m2);
			
			return super.compare(store, m1, m2, property);
		}
		
		
	}*/

}
