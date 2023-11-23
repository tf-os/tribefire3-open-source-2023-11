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
package com.braintribe.gwt.genericmodel.client.resource;

import java.io.IOException;
import java.io.InputStream;

import com.braintribe.gwt.fileapi.client.Blob;
import com.braintribe.model.generic.session.InputStreamProvider;

public class GwtInputStreamProvider implements InputStreamProvider{
	
	private Blob b;
	
	public GwtInputStreamProvider(Blob b) {
		this.b = b;
	}
	
	@Override
	public InputStream openInputStream() throws IOException {
		throw new UnsupportedOperationException();
	}
	
	public Blob blob() {
		return b;
	}
	
	public native String url() /*-{
		return $wnd.URL.createObjectURL(this.@GwtInputStreamProvider::b);
	}-*/;
}