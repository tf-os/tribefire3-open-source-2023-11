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
package com.braintribe.model.processing.session.api.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.api.ResourceReadAccess;

/**
 * Grants access to Resources based on the ResourceModel it support building URLs to existing resources, streaming
 * existing resources and creating new resources
 * 
 * @author dirk.scheffler
 *
 */
public interface ResourceAccess extends ResourceReadAccess {

	ResourceUrlBuilder url(Resource resource);

	ResourceCreateBuilder create();

	ResourceRetrieveBuilder retrieve(Resource resource);
	
	ResourceUpdateBuilder update(Resource resource);

	ResourceDeleteBuilder delete(Resource resource);

	@Override
	default InputStream openStream(Resource resource) throws IOException {
		return retrieve(resource).stream();
	}

	@Override
	default void writeToStream(Resource resource, OutputStream outputStream) throws IOException {
		retrieve(resource).stream(outputStream);
	}

}
