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
package com.braintribe.gwt.gmview.client;

import com.google.gwt.resources.client.ImageResource;

/**
 * Marker for actions which have two states (e.g. collapse/expand, maximize/restore)
 *
 */

public interface DoubleStateAction {
	
	public void setStateIcon1(ImageResource icon);
	public void setStateIcon2(ImageResource icon);
	
	public ImageResource getStateIcon1();
	public ImageResource getStateIcon2();
	
	public void setStateDescription1(String description);
	public void setStateDescription2(String description);
	
	public String getStateDescription1();
	public String getStateDescription2();
	
	public void updateState();
	public Boolean isDefaultState();
}
