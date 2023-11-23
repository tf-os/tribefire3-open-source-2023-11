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
package com.braintribe.gwt.simplepropertypanel.client.resources;

import org.vectomatic.dom.svg.ui.SVGResource;
import org.vectomatic.dom.svg.ui.SVGResource.Validated;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;

public interface SimplePropertyPanelResources extends ClientBundle{
	
	public static final SimplePropertyPanelResources INSTANCE = (SimplePropertyPanelResources) GWT.create(SimplePropertyPanelResources.class);
	
	@Source("arrowBold.svg") @Validated(validated = false)
	public SVGResource arrow();
	@Source("circleBold.svg") @Validated(validated = false)
	public SVGResource circle();
	@Source("doubleCircleBold.svg") @Validated(validated = false)
	public SVGResource doubleCircle();
	@Source("doubleCircleArrowBold.svg") @Validated(validated = false)
	public SVGResource doubleCircleArrow();
	@Source("keyCircle.svg") @Validated(validated = false)
	public SVGResource keyCircle();
	@Source("valueCircle.svg") @Validated(validated = false)
	public SVGResource valueCircle();

}
