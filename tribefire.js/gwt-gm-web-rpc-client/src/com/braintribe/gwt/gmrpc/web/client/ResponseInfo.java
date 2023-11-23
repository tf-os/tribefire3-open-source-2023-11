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
package com.braintribe.gwt.gmrpc.web.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.braintribe.gwt.fileapi.client.Blob;
import com.braintribe.gwt.genericmodel.client.resource.GwtInputStreamProvider;
import com.braintribe.model.resource.source.TransientSource;

public class ResponseInfo {
	public List<TransientSource> sources = new ArrayList<TransientSource>(); 
	public Map<String, Blob> parts;
	public String responseText;
	public String responseMimeType;
	
	public void transfer() {
		if(parts != null){
			for(TransientSource ts : sources) {
				Blob blob = parts.get(ts.getGlobalId());
				GwtInputStreamProvider inputStreamProvider = new GwtInputStreamProvider(blob);
				ts.setInputStreamProvider(inputStreamProvider);
			}
		}
	}
	
}