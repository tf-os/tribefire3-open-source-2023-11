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
package com.braintribe.gwt.logging.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface LoggingResources extends ClientBundle {

	public static LoggingResources INSTANCE = GWT.create(LoggingResources.class);

	@Source("Delete_32x32.png")
	public ImageResource delete();
	@Source("Maximize_32x32.png")
	public ImageResource maximize();
	@Source("Minimize_32x32.png")
	public ImageResource restore();
	@Source("Remove_32x32.png")
	public ImageResource clear();
	@Source("Refresh_32x32.png")
	public ImageResource refresh();
	@Source("Profiling_32x32.png")
	public ImageResource profiling();
}
