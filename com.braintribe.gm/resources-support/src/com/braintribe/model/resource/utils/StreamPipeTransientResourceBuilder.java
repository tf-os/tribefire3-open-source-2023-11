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
package com.braintribe.model.resource.utils;

import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.api.ResourceBuilder;
import com.braintribe.model.resource.api.ResourceWriterBuilder;
import com.braintribe.utils.stream.api.StreamPipeFactory;

/**
 * @author peter.gazdik
 */
public class StreamPipeTransientResourceBuilder implements ResourceBuilder {

	private final StreamPipeFactory streamPipeFactory;

	public StreamPipeTransientResourceBuilder(StreamPipeFactory streamPipeFactory) {
		this.streamPipeFactory = streamPipeFactory;
	}

	@Override
	public ResourceWriterBuilder newResource() {
		return new BasicResourceWriterBuilder(streamPipeFactory).setResourceFactory(Resource::createTransient);
	}

}
