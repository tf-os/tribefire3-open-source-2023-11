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
package com.braintribe.gwt.gmview.client.js.interop;

import com.braintribe.gwt.gmview.client.GmViewport;
import com.braintribe.gwt.gmview.client.GmViewportListener;

import jsinterop.annotations.JsMethod;

/**
 * Interface used for exporting {@link GmViewport} via JsInterop.
 */
@SuppressWarnings("unusable-by-js")
public interface GmViewportInterfaceInterop extends GmViewport {
	
	@Override
	@JsMethod
	void addGmViewportListener(GmViewportListener vl);
	
	@Override
	@JsMethod
	void removeGmViewportListener(GmViewportListener vl);
    
	@Override
	@JsMethod
    boolean isWindowOverlappingFillingSensorArea();
	
	@JsMethod
	public void fireViewportListener(GmViewportListener listener);

}
