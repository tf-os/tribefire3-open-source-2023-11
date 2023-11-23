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
package com.braintribe.model.resource.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.braintribe.model.resource.Resource;

/**
 * Grants access to Resources based on the ResourceModel
 * it support building URLs to existing resources, streaming existing resources and creating new resources   
 * @author dirk.scheffler
 *
 */
@SuppressWarnings("unusable-by-js")
public interface ResourceReadAccess {
	/**
	 * opens a stream to the binary data which is represented by the given resource
	 * @param resource the resource whose binary content is to be streamed
	 * @return the stream that will have the binary data
	 */
	InputStream openStream(Resource resource) throws IOException;
	
	/**
	 * writes the binary data which is represented by the given resource to the given output stream
	 * @param resource the resource whose binary content is to be streamed
	 * @param outputStream the stream which will receive the binary data
	 */
	void writeToStream(Resource resource, OutputStream outputStream) throws IOException;
	

}
