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
package tribefire.platform.impl.streaming;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.resource.enrichment.ResourceEnricher;
import com.braintribe.model.processing.resource.enrichment.ResourceEnrichingStreamer;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resourceapi.enrichment.EnrichResource;
import com.braintribe.model.resourceapi.enrichment.EnrichResourceResponse;

/**
 * <p>
 * A standard {@link ResourceEnrichingStreamer}-based {@link ResourceEnricher}.
 * 
 */
public class StandardResourceEnricher implements ResourceEnricher {

	private ResourceEnrichingStreamer enrichingStreamer;

	@Required
	@Configurable
	public void setEnrichingStreamer(ResourceEnrichingStreamer enrichingStreamer) {
		this.enrichingStreamer = enrichingStreamer;
	}

	@Override
	public EnrichResourceResponse enrich(AccessRequestContext<EnrichResource> context) {

		EnrichResource request = context.getOriginalRequest();

		Resource resource = request.getResource();

		// @formatter:off
		boolean enriched = enrichingStreamer
								.stream()
									.onlyIfEnriched()
									.context(context)
									.enriching(resource);
		// @formatter:on

		EnrichResourceResponse response = EnrichResourceResponse.T.create();

		if (enriched) {
			// response with no Resource means no enrichment took place
			response.setResource(resource);
		}

		return response;

	}

}
