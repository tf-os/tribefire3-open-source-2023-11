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

import org.vectomatic.dom.svg.ui.SVGResource;
import org.vectomatic.dom.svg.ui.SVGResource.Validated;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource.Import;
import com.google.gwt.resources.client.ImageResource;
import com.sencha.gxt.theme.base.client.grid.RowExpanderDefaultAppearance.RowExpanderStyle;

public interface PropertyPanelResources extends ClientBundle {
	
	public static final PropertyPanelResources INSTANCE = GWT.create(PropertyPanelResources.class);
	
	@Import({RowExpanderStyle.class})
	@Source ("propertyPanel.gss")
	public PropertyPanelCss css();
	
	@Source ("Apply_16x16.png")
	public ImageResource apply();
	@Source ("Back_16x16.png")
	public ImageResource back();
	ImageResource bullet();
	@Source ("Cancel_16x16.png")
	public ImageResource cancel();
	ImageResource changeExisting();
	ImageResource checked();
	ImageResource checkNull();
	ImageResource clear();
	ImageResource clearString();
	ImageResource collapsed();
	ImageResource collapsedArrow();
	ImageResource defaultIcon();
	ImageResource dropdown();
	ImageResource expanded();
	ImageResource expandedArrow();
	@Source("Front_16x16.png")
	public ImageResource front();
	@Source("Add_16x16.png")
	public ImageResource instantiate();
	ImageResource loading();
	@Source ("locked-32.png")
	ImageResource lock();
	ImageResource nullIcon();
	@Source("Open_16x16.png")
	public ImageResource open();
	@Source("Run_16x16.png")
	public ImageResource run();
	ImageResource smallarrow();
	ImageResource unchecked();
	@Source("com/braintribe/gwt/gxt/gxtresources/images/exclamation.svg") @Validated(validated = false)
	SVGResource exclamation();
	@Source("com/braintribe/gwt/gxt/gxtresources/images/info.svg") @Validated(validated = false)
	SVGResource info();
	

}
