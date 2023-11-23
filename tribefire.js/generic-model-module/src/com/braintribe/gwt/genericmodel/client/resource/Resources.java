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

import java.util.Date;

import com.braintribe.gwt.fileapi.client.Blob;
import com.braintribe.gwt.fileapi.client.File;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.TransientSource;

import jsinterop.annotations.JsMethod;

@SuppressWarnings("unusable-by-js")
public interface Resources {

	@JsMethod(namespace = GmCoreApiInteropNamespaces.resources)
	public static Resource fromBlob(Blob blob) {
		String uuid = GMF.platform().newUuid();
		
		GwtInputStreamProvider streamProvider = new GwtInputStreamProvider(blob);

		TransientSource source = TransientSource.T.create();
		source.setGlobalId(uuid);
		source.setInputStreamProvider(streamProvider);
		
		Resource resource = Resource.T.create();
		
		resource.setResourceSource(source);
		source.setOwner(resource);
		
		resource.setFileSize((long)blob.size());
		resource.setMimeType(blob.type());
		resource.setCreated(new Date());
		
		return resource;
	}
	
	@JsMethod(namespace = GmCoreApiInteropNamespaces.resources)
	public static Resource fromFile(File file) {
		Resource resource = fromBlob(file);
		resource.setName(file.getName());
		
		return resource;
	}
}
