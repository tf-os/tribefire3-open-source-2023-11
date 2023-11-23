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
package com.braintribe.gwt.fileapi.client;

import com.google.gwt.xhr.client.XMLHttpRequest;

public class XMLHttpRequest2 extends XMLHttpRequest {
	protected XMLHttpRequest2() {

	}
	
	public static XMLHttpRequest2 create() {
		XMLHttpRequest2 xhr = XMLHttpRequest.create().cast();
		
		return xhr;
	}

	public final native void send(FormData requestData) /*-{
		this.send(requestData);
	}-*/;
	
	public final native XMLHttpRequestUpload getUpload() /*-{
		return this.upload;
	}-*/;
}
