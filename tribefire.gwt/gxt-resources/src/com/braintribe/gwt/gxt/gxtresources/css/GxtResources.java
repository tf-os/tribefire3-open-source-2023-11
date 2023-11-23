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
package com.braintribe.gwt.gxt.gxtresources.css;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource.Import;
import com.google.gwt.resources.client.ImageResource;
import com.sencha.gxt.theme.base.client.grid.RowExpanderDefaultAppearance.RowExpanderStyle;

public interface GxtResources extends ClientBundle {
	
	public static final GxtResources INSTANCE = (GxtResources) GWT.create(GxtResources.class);
	
	@Import({RowExpanderStyle.class})
	@Source ("com/braintribe/gwt/gxt/gxtresources/css/gxtResource.gss")
	public GxtResourceCss css();
	
	//Image Source *****************************************************************************************************************************************
	
	@Source ("com/braintribe/gwt/gxt/gxtresources/images/validation-tick-small.png")
	ImageResource apply();
	
	@Source("com/braintribe/gwt/gxt/gxtresources/images/Multiline_16x16.png")
	ImageResource multiLine();
	
	@Source("com/braintribe/gwt/gxt/gxtresources/images/edit.png")
	public ImageResource edit();
	

	/* SVGResource example
	@Source("com/braintribe/gwt/gxt/gxtresources/images/svg/arrow-drop-up-silver.svg") @Validated(validated = false)
	SVGResource  arrowDropUpSilverSvg();
	*/
	
}
