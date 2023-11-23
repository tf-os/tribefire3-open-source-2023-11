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
package com.braintribe.gwt.querymodeleditor.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface QueryModelEditorResources extends ClientBundle {
	public static final QueryModelEditorResources INSTANCE = GWT.create(QueryModelEditorResources.class);

	/************ Both Panels ************/

	@Source("com/braintribe/gwt/gxt/gxtresources/images/add_16x16.png")
	public ImageResource add();	
	
	@Source("com/braintribe/gwt/gxt/gxtresources/images/add_32x32.png")
	public ImageResource addBig();

	@Source("com/braintribe/gwt/gxt/gxtresources/images/Remove_32x32.png")
	public ImageResource removeBig();
	
	@Source("search_white_transparent.png")
	public ImageResource query();

	@Source("dropdown_icon.png")
	public ImageResource dropDown();

	@Source("arrow-right.png")
	public ImageResource arrowRight();

	@Source("arrow-left.png")
	public ImageResource arrowLeft();

	/******* QueryModelEditorPanel *******/

	@Source("Ascending_16x16.png")
	public ImageResource ascending();

	@Source("Descending_16x16.png")
	public ImageResource descending();

	/*** QueryModelEditorAdvancedPanel ***/

	@Source("feedback_info_valid.png")
	public ImageResource valid();

	@Source("feedback_info_invalid.png")
	public ImageResource invalid();
	
	@Source("actions-delete-small.png")
	public ImageResource delete();

	/********** QueryFormDialog **********/

	@Source("Cancel_16x16.png")
	public ImageResource cancel();
}
