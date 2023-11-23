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
package com.braintribe.gwt.ioc.gme.client.resources;

import org.vectomatic.dom.svg.ui.ExternalSVGResource.Validated;
import org.vectomatic.dom.svg.ui.SVGResource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface CustomizationResources extends ClientBundle {
	
	public static final CustomizationResources INSTANCE = ((CustomizationResourcesFactory) GWT.create(CustomizationResourcesFactory.class)).getResources();
	
	@Source("com/braintribe/gwt/gxt/gxtresources/images/Show-Log_16x16.png")
	public ImageResource log();
	@Source("com/braintribe/gwt/gxt/gxtresources/images/bwUserManagement.png")
	public ImageResource userManagement();
	@Source("com/braintribe/gwt/gxt/gxtresources/images/bwKey.png")
	public ImageResource key();
	@Source("com/braintribe/gwt/gxt/gxtresources/images/bwMaintenanceTasks.png")
	public ImageResource maintenanceTasks();
	@Source("com/braintribe/gwt/gxt/gxtresources/images/bwVerification.png")
	public ImageResource verification();
	@Source("com/braintribe/gwt/gxt/gxtresources/images/clear.gif")
	public ImageResource clear();
	@Source("com/braintribe/gwt/gxt/gxtresources/images/logo.png")
	public ImageResource logo();
	@Source("com/braintribe/gwt/gxt/gxtresources/images/logoBlack.png")
	public ImageResource logoBlack();
	@Source("com/braintribe/gwt/gxt/gxtresources/images/UploadOrange_32x32.png")
	public ImageResource upload();
	@Source("com/braintribe/gwt/gxt/gxtresources/images/UploadOrange_16x16.png")
	public ImageResource uploadSmall();
	//public ImageResource close();
	@Source("com/braintribe/gwt/gxt/gxtresources/images/user_empty.png")
	public ImageResource user();
	@Source("com/braintribe/gwt/gxt/gxtresources/images/addOrange16x16.png")
	public ImageResource addOrange();
	@Source("com/braintribe/gwt/gxt/gxtresources/images/addOrange32x32.png")
	public ImageResource addOrangeBig();
	@Source("com/braintribe/gwt/gxt/gxtresources/images/More_32x32.png")
	public ImageResource defaultActionIcon();
	@Source("com/braintribe/gwt/gxt/gxtresources/images/Settings_16x16.png")
	public ImageResource settings();
	
	@Source("com/braintribe/gwt/gxt/gxtresources/images/bubble.png")
	public ImageResource bubble();
	
	@Source("com/braintribe/gwt/gxt/gxtresources/images/tf-logo.svg") @Validated(validated = false)
	SVGResource  tfLogo();	
	
	@Source ("customization.gss")
	public CustomizationCss css();
	
//	@Source ("customizationQueryModelEditor.css")
//	public QueryModelEditorCss customizationQueryModelEditorCss();

}
