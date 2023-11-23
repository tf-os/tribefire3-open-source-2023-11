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
package com.braintribe.gwt.gme.selectresultpanel.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface SelectResultPanelResources extends ClientBundle {
	
	public static final SelectResultPanelResources INSTANCE = GWT.create(SelectResultPanelResources.class);
	
	@Source ("selectResultPanel.gss")
	public SelectResultPanelCss css();
	
	ImageResource checked();
	ImageResource checkNull();
	ImageResource dropdown();
	ImageResource nullIcon();
	ImageResource unchecked();

}
