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

import com.braintribe.gwt.gmview.actionbar.client.GmViewActionProvider;
import com.braintribe.gwt.gmview.client.GmActionSupport;
import com.braintribe.gwt.gmview.client.GmListView;
import com.braintribe.gwt.gmview.client.GmViewport;
import com.braintribe.gwt.gmview.client.GmViewportListener;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * Class needed for being able to export the following classes via JsInterop: {@link GmListView},
 * {@link GmActionSupport}, {@link GmViewActionProvider} and {@link GmViewport}.
 */
@JsType(name = "GmActionListViewport", namespace = InteropConstants.VIEW_NAMESPACE)
@SuppressWarnings("unusable-by-js")
public class GmActionListViewportInterop extends GmActionListViewInterop implements GmViewportInterfaceInterop {
	
	@JsConstructor
	public GmActionListViewportInterop() {
		super();
	}

	@Override
	@JsMethod
	public void addGmViewportListener(GmViewportListener vl) {
		//NOP
	}

	@Override
	@JsMethod
	public void removeGmViewportListener(GmViewportListener vl) {
		//NOP
	}

	@Override
	@JsMethod
	public boolean isWindowOverlappingFillingSensorArea() {
		return false;
	}
	
	@Override
	@JsMethod
	public void fireViewportListener(GmViewportListener listener) {
		listener.onWindowChanged(this);
	}

}
