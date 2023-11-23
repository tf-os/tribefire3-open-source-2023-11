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
package com.braintribe.gwt.gme.assemblypanel.client.resources;

import com.google.gwt.resources.client.CssResource;

public interface AssemblyPanelCss extends CssResource {
	
	//External definitions (in the public assemblypanel.css can be added here if they are important in the code).
	public static String EXTERNAL_PROPERTY_VALUE = "assemblyPanelPropertyValue";
	public static String EXTERNAL_PROPERTY_ICON = "assemblyPanelPropertyIcon";
	public static String EXTERNAL_PROPERTY_ICON_GROUP = "assemblyPanelPropertyIconGroup";
	public static String EXTERNAL_PROPERTY_TEXT = "assemblyPanelPropertyText";
	
	String checkedReadOnlyValue();
	String checkedValue();
	String checkNullReadOnlyValue();
	String checkNullValue();
	String collectionElementStyle();
	String editableBox();
	String emphasisStyle();
	String inheritFont();
	String linkStyle();
	String mapKeyAndValueSeparator();
	String moreItemsInSetStyle();
	String pointerCursor();
	String propertyNameStyle();
	String propertyValueWithPadding();
	String tableFixedLayout();
	String tableForTreeWithFixedLayout();
	String textOverflowNoWrap();
	String uncheckedReadOnlyValue();
	String uncheckedValue();

}
