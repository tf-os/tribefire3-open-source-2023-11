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
package com.braintribe.gwt.gme.propertypanel.client.resources;

import com.google.gwt.resources.client.CssResource;

public interface PropertyPanelCss extends CssResource {
	
	//External definitions (in the public propertyPanel.css can be added here if they are important in the code).
	public static String EXTERNAL_PROPERTY_VALUE_COLLECTION_ADD = "propertyPanelPropertyValueCollectionAdd";
	public static String EXTERNAL_PROPERTY_VALUE_OPERATION = "propertyPanelPropertyValueOperation";
	public static String EXTERNAL_PROPERTY_VALUE_ENTITY_ASSIGN = "propertyPanelPropertyValueEntityAssign";
	public static String EXTERNAL_PROPERTY_FLOW_DISPLAY = "propertyPanelPropertyFlowDisplay";
	public static String EXTERNAL_PROPERTY_FLOW_DISPLAY_LINE = "propertyPanelPropertyFlowDisplayLine";
	public static String EXTERNAL_PROPERTY_VALUE_WITH_GRID_LINES = "propertyPanelPropertyValueWithGridLines";
	public static String EXTERNAL_PROPERTY_VALUE_READ_ONLY = "propertyPanelPropertyValueReadOnly";
	public static String EXTERNAL_FLOW_COLLECTION_ELEMENT = "propertyPanelFlowCollectionElement";
	public static String EXTERNAL_PROPERTY_MENU = "propertyPanelPropertyMenu";	
	public static String EXTERNAL_PROPERTY_TEXT = "propertyPanelPropertyText";
	public static String EXTERNAL_PROPERTY_ICONS = "propertyPanelPropertyIcons";
	public static String EXTERNAL_PROPERTY_VALUE_PLACEHOLDER = "propertyPanelValuePlaceholder";
	
	String border();
	String clickableElement();
	String clickableInsideTrigger();
	String clickableTrigger();
	String propertyCollectionItemMenu();
	String propertyEntity();
	String propertyPanelWithoutLines();
	String propertyNameGroup();
	String propertyNameMandatory();
	String propertyNameReadOnly();
	String propertyNameFlow();
	String propertyNameFlowLink();
	String propertyName();
	String propertyNameFirst();
	String propertyNameFlowExpanderCollapsed();
	String propertyNameFlowExpanderExpanded();
	String propertyValueFlow();
	String propertyValue();
	String propertyValueFirst();
	String inheritFont();
	String tableFixedLayout();
	String textOverflowNoWrap();
	String checkNullValue();
	String checkedValue();
	String uncheckedValue();
	String checkNullReadOnlyValue();
	String checkedReadOnlyValue();
	String uncheckedReadOnlyValue();
	String flowListEntry();
	String flowSetEntry();
	String flowMapEntry();
	String flowMapKeyElement();
	String flowMapValueElement();
	String editableBox();
	String entityTypeLabel();
	String entityTitleLabel();
	String entityDescriptionLabel();
	String entityIcon();
	String emptyValue();
	String propertyIcon();
	String entered();
}
