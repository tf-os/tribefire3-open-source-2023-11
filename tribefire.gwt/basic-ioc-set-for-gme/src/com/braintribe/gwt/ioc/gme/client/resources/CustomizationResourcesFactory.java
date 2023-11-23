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

import com.braintribe.gwt.browserfeatures.client.GWTMetaPropertiesUtil;
import com.braintribe.gwt.browserfeatures.client.GWTMetaPropertiesUtil.IconSet;
import com.google.gwt.core.client.GWT;

public class CustomizationResourcesFactory {
	
	private static final CustomizationResources customizationResources = (CustomizationResources) (GWTMetaPropertiesUtil.getIconSet().equals(IconSet.coloured) ?
			GWT.create(ColouredCustomizationResources.class) : GWT.create(CustomizationResources.class));

	public CustomizationResources getResources() {
		return customizationResources;
	}

}
