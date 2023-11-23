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
package com.braintribe.gwt.browserfeatures.client;

import com.google.gwt.xhr.client.XMLHttpRequest;

/**
 * This is an implementation of the XMLHttpRequest, but for preparing sync calls.
 * @author michel.docouto
 *
 */
public class SyncXMLHttpRequest extends XMLHttpRequest {
	
	protected SyncXMLHttpRequest() {
	}
	
	/**
	 * Creates an XMLHttpRequest object.
	 * 
	 * @return the created object
	 */
	public static native SyncXMLHttpRequest create() /*-{
		// Don't check window.XMLHttpRequest, because it can
		// cause cross-site problems on IE8 if window's URL
		// is javascript:'' .
		if ($wnd.XMLHttpRequest) {
			return new $wnd.XMLHttpRequest();
		} else {
			try {
				return new $wnd.ActiveXObject('MSXML2.XMLHTTP.3.0');
			} catch (e) {
				return new $wnd.ActiveXObject("Microsoft.XMLHTTP");
			}
		}
	}-*/;
	
	public final native String sendSynchronous(String method, String url) /*-{
		this.open(method, url, false);
		this.send(null);
		var serverResponse = this.responseText;
		return serverResponse;
	}-*/;
	
}
