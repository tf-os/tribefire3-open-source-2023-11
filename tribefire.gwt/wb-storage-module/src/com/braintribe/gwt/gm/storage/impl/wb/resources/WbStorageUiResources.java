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
package com.braintribe.gwt.gm.storage.impl.wb.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface WbStorageUiResources extends ClientBundle {
	public static final WbStorageUiResources INSTANCE = GWT.create(WbStorageUiResources.class);

	@Source("Apply.png")
	public ImageResource apply();

	@Source("Cancel.png")
	public ImageResource cancel();

	@Source("Delete.png")
	public ImageResource delete();

	@Source("Blank.png")
	public ImageResource blank();

	@Source("Checked.png")
	public ImageResource checked();

	@Source("Unchecked.png")
	public ImageResource unchecked();
}
