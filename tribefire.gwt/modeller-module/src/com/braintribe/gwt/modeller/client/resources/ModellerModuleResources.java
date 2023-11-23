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
package com.braintribe.gwt.modeller.client.resources;

import org.vectomatic.dom.svg.ui.SVGResource;
import org.vectomatic.dom.svg.ui.SVGResource.Validated;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface ModellerModuleResources extends ClientBundle{
	
	public static final ModellerModuleResources INSTANCE = (ModellerModuleResources) GWT.create(ModellerModuleResources.class);
	
	public ImageResource add();
	public ImageResource addToCircle();
	public ImageResource addedToCircle();
	public ImageResource goTo();
	public ImageResource delete();
	public ImageResource remove();
	public ImageResource visibility();
	public ImageResource visible();
	public ImageResource hidden();
	public ImageResource pin();
	
	public ImageResource blank();
	
	public ImageResource next();
	public ImageResource previous();
	
	public ImageResource expanded();
	public ImageResource collapsed();
			
	@Validated(validated = false)
	public SVGResource arrow();
	@Validated(validated = false)
	public SVGResource circle();
	@Validated(validated = false)
	public SVGResource doubleCircle();
	@Validated(validated = false)
	public SVGResource doubleCircleArrow();
	@Validated(validated = false)
	public SVGResource keyCircle();
	@Validated(validated = false)
	public SVGResource valueCircle();
	
	public ImageResource modellerBig();
	public ImageResource modeller();
}
