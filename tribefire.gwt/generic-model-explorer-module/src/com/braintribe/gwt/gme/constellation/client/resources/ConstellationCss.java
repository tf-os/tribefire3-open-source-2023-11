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
package com.braintribe.gwt.gme.constellation.client.resources;

import com.google.gwt.resources.client.CssResource;

public interface ConstellationCss extends CssResource {
	
	//External definitions (in the public constellation.css can be added here if they are important in the code).
	public static String EXTERNAL_HIGHLIGHT_BUTTON = "constellationHighlightButton";
	public static String EXTERNAL_TOOL_BAR_SELECTED = "constellationToolBarSelected";
	
	String bannerImage();
	String centeredText();
	String explorerConstellationCenterBackground();
	String greyBorder();
	String graySmallText();
	String mandatoryLabel();
	String propertyNameLabel();
	String queryConstellationNorth();
	String separatorMargin();
	String toolBarElement();
	String toolBarElementImage();
	String toolBarElementText();
	String toolBarParentStyle();
	String toolBarStyle();
	String toolTip();

}
