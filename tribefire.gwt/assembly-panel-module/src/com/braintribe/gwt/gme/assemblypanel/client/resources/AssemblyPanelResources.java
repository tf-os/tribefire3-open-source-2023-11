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

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface AssemblyPanelResources extends ClientBundle {
	
	public static final AssemblyPanelResources INSTANCE = GWT.create(AssemblyPanelResources.class);
	
	@Source ("assemblyPanel.gss")
	public AssemblyPanelCss css();
	
	@Source ("com/braintribe/gwt/gxt/gxtresources/images/blackMenu2.png")
	public ImageResource blackMenu();

	@Source ("com/braintribe/gwt/gxt/gxtresources/images/changeExisting.png")
	public ImageResource changeExisting();

	@Source ("com/braintribe/gwt/gxt/gxtresources/images/checked.png")
	public ImageResource checked();

	@Source ("com/braintribe/gwt/gxt/gxtresources/images/checkNull.png")
	public ImageResource checkNull();

	@Source ("com/braintribe/gwt/gxt/gxtresources/images/clear.gif")
	public ImageResource clear();
	
	@Source ("com/braintribe/gwt/gxt/gxtresources/images/clearString.png")
	public ImageResource clearString();
	
	@Source("com/braintribe/gwt/gxt/gxtresources/images/Clipboard_16x16.png")
	public ImageResource clipboard();
	
	@Source("com/braintribe/gwt/gxt/gxtresources/images/Clipboard_32x32.png")
	public ImageResource clipboardBig();
	
	@Source("com/braintribe/gwt/gxt/gxtresources/images/nullAction.png")	
	public ImageResource nullAction();
	
	@Source("com/braintribe/gwt/gxt/gxtresources/images/nullIcon.png")		
	public ImageResource nullIcon();
	
	@Source("com/braintribe/gwt/gxt/gxtresources/images/Open_16x16.png")
	public ImageResource open();
	
	@Source("com/braintribe/gwt/gxt/gxtresources/images/radioUnchecked.png")	
	public ImageResource radio();
	
	@Source("com/braintribe/gwt/gxt/gxtresources/images/radioChecked.png")		
	public ImageResource radioChecked();
	
	@Source("com/braintribe/gwt/gxt/gxtresources/images/smallarrow.png")		
	public ImageResource smallarrow();

	@Source("com/braintribe/gwt/gxt/gxtresources/images/unchecked.png")		
	public ImageResource unchecked();
}
