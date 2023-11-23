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
package com.braintribe.gwt.gme.propertypanel.client;

import java.util.Comparator;

import com.braintribe.gwt.gme.propertypanel.client.field.ExtendedInlineField;
import com.braintribe.model.extensiondeployment.meta.DynamicSelectList;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.validation.ValidationKind;
import com.braintribe.model.meta.data.display.Group;
import com.braintribe.model.meta.data.prompt.SimplifiedAssignment;
import com.braintribe.model.meta.data.prompt.VirtualEnum;
import com.braintribe.utils.i18n.I18nTools;

/**
 * Model to be edited/shown by the {@link PropertyPanel}.
 * 
 * @author michel.docouto
 *
 */
public class PropertyModel {
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
	private String placeHolder;
	private boolean usePlaceholder;
	private GenericEntity parentEntity;
	private EntityType<GenericEntity> parentEntityType;
	private Double groupPriority;
	private String groupName;
	private String groupIcon;
	private String displayName;
	private Double priority;
	private boolean mandatory;
	private boolean flow;
	private boolean flowExpanded;
	private Object value;
	private String compoundPropertyName;
	private ExtendedInlineField extendedInlineField;
	private VirtualEnum virtualEnum;
	private boolean hideLabel;
	private boolean referenceable;
	private SimplifiedAssignment simplifiedAssignment;
	private DynamicSelectList dynamicSelectList;
	private boolean inline;
	private ValidationKind validationKind = ValidationKind.none;
	private String validationDescription;
	
	public PropertyModel() {
	}
	
	public void setGroupPriority(Double groupPriority){
		this.groupPriority = groupPriority;
	}
	
	public Double getGroupPriority(){
		return groupPriority;
	}
	
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	public String getGroupName() {
		return groupName;
	}
	
	public void setGroupIcon(String groupIcon) {
		this.groupIcon = groupIcon;
	}
	
	public String getGroupIcon() {
		return groupIcon;
	}
	
	public void setPropertyGroup(Group propertyGroup) {
		this.propertyGroup = propertyGroup;
		if (propertyGroup != null) {
			setGroupName(
					propertyGroup.getLocalizedName() != null ? I18nTools.getLocalized(propertyGroup.getLocalizedName()) : propertyGroup.getName());
		}
	}
	
	public Group getPropertyGroup() {
		return propertyGroup;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setPlaceHolder(String placeHolder) {
		this.placeHolder = placeHolder;
		usePlaceholder = true;
	}
	
	public String getPlaceHolder() {
		return placeHolder;
	}
	
	public void setUsePlaceholder(boolean usePlaceholder) {
		this.usePlaceholder = usePlaceholder;
	}
	
	public boolean isUsePlaceholder() {
		return usePlaceholder;
	}
	
	public void setPriority(Double priority) {
		this.priority = priority;
	}
	
	public Double getPriority() {
		return priority;
	}
	
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
	
	public boolean getMandatory() {
		return mandatory;
	}
	
	public void setFlow(boolean flow) {
		this.flow = flow;
	}
	
	public boolean getFlow() {
		return flow;
	}
	
	public void setFlowExpanded(boolean flowExpanded) {
		this.flowExpanded = flowExpanded;
	}
	
	public boolean getFlowExpanded() {
		return flowExpanded;
	}
	
	public <X> void setValue(X value) {
		this.value = value;
	}
	
	public <X> X getValue() {
		return (X) value;
	}
	
	public void setValueElementType(GenericModelType valueElementType) {
		this.valueElementType = valueElementType;
	}
	
	public GenericModelType getValueElementType() {
		return valueElementType;
	}
	
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	
	public boolean isEditable() {
		return editable;
	}
	
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	
	public String getPropertyName() {
		return propertyName;
	}
	
	public void setCompoundPropertyName(String compoundPropertyName) {
		this.compoundPropertyName = compoundPropertyName;
	}
	
	public String getNormalizedPropertyName() {
		return compoundPropertyName != null ? compoundPropertyName : propertyName;
	}
	
	public void setFlowDisplay(String flowDisplay) {
		this.flowDisplay = flowDisplay;
	}
	
	public String getFlowDisplay() {
		return flowDisplay;
	}
	
	public void setValueDisplay(String valueDisplay) {
		this.valueDisplay = valueDisplay;
	}
	
	public String getValueDisplay() {
		return valueDisplay;
	}
	
	public void setPassword(boolean password) {
		this.password = password;
	}
	
	public boolean getPassword() {
		return password;
	}
	
	public void setAbsent(boolean absent) {
		this.absent = absent;
	}
	
	public boolean getAbsent() {
		return absent;
	}
	
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}
	
	public boolean isNullable() {
		return nullable;
	}
	
	public void setBaseTyped(boolean baseTyped) {
		this.baseTyped = baseTyped;
	}
	
	public boolean isBaseTyped() {
		return baseTyped;
	}
	
	public GenericEntity getParentEntity() {
		return parentEntity;
	}
	
	public void setParentEntity(GenericEntity parentEntity) {
		this.parentEntity = parentEntity;
	}
	
	public EntityType<GenericEntity> getParentEntityType() {
		return parentEntityType;
	}
	
	public void setParentEntityType(EntityType<GenericEntity> parentEntityType) {
		this.parentEntityType = parentEntityType;
	}
	
	public ExtendedInlineField getExtendedInlineField() {
		return extendedInlineField;
	}
	
	public void setExtendedInlineField(ExtendedInlineField extendedInlineField) {
		this.extendedInlineField = extendedInlineField;
	}
	
	public VirtualEnum getVirtualEnum() {
		return virtualEnum;
	}
	
	public void setVirtualEnum(VirtualEnum virtualEnum) {
		this.virtualEnum = virtualEnum;
	}
	
	public boolean isHideLabel() {
		return hideLabel;
	}
	
	public void setHideLabel(boolean hideLabel) {
		this.hideLabel = hideLabel;
	}
	
	public boolean isReferenceable() {
		return referenceable;
	}
	
	public void setReferenceable(boolean referenceable) {
		this.referenceable = referenceable;
	}
	
	public SimplifiedAssignment getSimplifiedAssignment() {
		return simplifiedAssignment;
	}
	
	public void setSimplifiedAssignment(SimplifiedAssignment simplifiedAssignment) {
		this.simplifiedAssignment = simplifiedAssignment;
	}
	
	public DynamicSelectList getDynamicSelectList() {
		return dynamicSelectList;
	}
	
	public void setDynamicSelectList(DynamicSelectList dynamicSelectList) {
		this.dynamicSelectList = dynamicSelectList;
	}
	
	public boolean isInline() {
		return inline;
	}
	
	public void setInline(boolean inline) {
		this.inline = inline;
	}
	
	public static Comparator<PropertyModel> getPriorityComparator() {
		if (priorityComparator != null)
			return priorityComparator;
		
		priorityComparator = (o1, o2) -> {
			int priorityComparison;
			if (o1.getPriority() == null && o2.getPriority() == null)
				priorityComparison = 0;
			else if (o1.getPriority() == null && o2.getPriority() != null)
				priorityComparison = 1;
			else if (o1.getPriority() != null && o2.getPriority() == null)
				priorityComparison = -1;
			else
				priorityComparison = o2.getPriority().compareTo(o1.getPriority());
			
			if (priorityComparison == 0) {
				if (o1.getMandatory() != o2.getMandatory()) 
					return (o1.getMandatory()) ? -1 : 1;
				
				if (o1.getFlow() == o2.getFlow())
					return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
				else
					return ((Boolean) o1.getFlow()).compareTo(o2.getFlow());
			}
			
			return priorityComparison;
		};
		
		return priorityComparator;
	}
	
	public static Comparator<PropertyModel> getGroupPriorityComparator() {
		if (groupPriorityComparator != null)
			return groupPriorityComparator;
		
		groupPriorityComparator = (o1, o2) -> {
			int priorityComparison;
			if (o1.getGroupPriority() == null && o2.getGroupPriority() == null)
				priorityComparison = 0;
			else if (o1.getGroupPriority() == null && o2.getGroupPriority() != null)
				priorityComparison = 1;
			else if (o1.getGroupPriority() != null && o2.getGroupPriority() == null)
				priorityComparison = -1;
			else
				priorityComparison = o2.getGroupPriority().compareTo(o1.getGroupPriority());
			
			if (priorityComparison == 0) {
				//if (o1.getFlow() == o2.getFlow())
					return o1.getGroupName().compareTo(o2.getGroupName());
				//else
					//return ((Boolean) o1.getFlow()).compareTo(o2.getFlow());
			}
			return priorityComparison;
		};
		
		return groupPriorityComparator;
	}

	public ValidationKind getValidationKind() {
		return validationKind;
	}

	public void setValidationKind(ValidationKind validationKind) {
		this.validationKind = validationKind;
	}

	public String getValidationDescription() {
		return validationDescription;
	}

	public void setValidationDescription(String validationDescription) {
		this.validationDescription = validationDescription;
	}
}
