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
package com.braintribe.model.processing.resource.enrichment;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;

import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.resource.Resource;

/**
 * <p>
 * Enriches {@link Resource} meta data while streaming.
 * 
 */
public interface ResourceEnrichingStreamer {

	ResourceEnrichingStreamerBuilder stream();

	interface ResourceEnrichingStreamerBuilder {

		ResourceEnrichingStreamerBuilder from(Supplier<InputStream> inputSupplier);

		ResourceEnrichingStreamerBuilder to(Supplier<OutputStream> outputSupplier);

		ResourceEnrichingStreamerBuilder context(ServiceRequestContext context);

		ResourceEnrichingStreamerBuilder onlyIfEnriched();

		boolean enriching(Resource resource);

		/**
		 * EXPERIMENTAL!!!
		 * 
		 * This might change without warning, use at your own risk!!!
		 */
		EnrichingResult enriching2(Resource resource);

	}

	/**
	 * EXPERIMENTAL!!!
	 * 
	 * This might change without warning, use at your own risk!!!
	 */
	interface EnrichingResult {
		boolean enriched();

		InputStream inputStream();
	}

}
