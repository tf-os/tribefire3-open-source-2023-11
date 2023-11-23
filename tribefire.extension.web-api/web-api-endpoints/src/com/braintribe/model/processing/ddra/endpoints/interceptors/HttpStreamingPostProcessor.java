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
package com.braintribe.model.processing.ddra.endpoints.interceptors;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.braintribe.ddra.endpoints.api.DdraEndpointAspect;
import com.braintribe.model.cache.CacheControl;
import com.braintribe.model.ddra.endpoints.api.v1.ApiV1DdraEndpoint;
import com.braintribe.model.processing.service.api.HttpResponseConfigurerAspect;
import com.braintribe.model.processing.service.api.ServicePostProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.resourceapi.stream.GetBinaryResponse;
import com.braintribe.model.resourceapi.stream.range.RangeResponse;

public class HttpStreamingPostProcessor implements ServicePostProcessor<GetBinaryResponse> {

	@Override
	public Object process(ServiceRequestContext requestContext, GetBinaryResponse response) {
		ApiV1DdraEndpoint ddraEndpoint = requestContext.findAttribute(DdraEndpointAspect.class) //
				.filter(d -> d instanceof ApiV1DdraEndpoint) //
				.map(d -> (ApiV1DdraEndpoint) d) //
				.orElse(null);

		// TODO: There is no way to disable response modification in non-ddra usecases. Find a more generic solution
		if (ddraEndpoint != null && !ddraEndpoint.getDownloadResource())
			return response;

		requestContext.findAttribute(HttpResponseConfigurerAspect.class) //
				.ifPresent(c -> {
					c.applyFor(response, httpResponse -> {
						if (response.getResource() == null) {
							httpResponse.setStatus(304);
						} else {
							setContentRangeHeaders(httpResponse, response);
							setCacheControlHeaders(httpResponse, response.getCacheControl());
						}
					});
				});

		return response;
	}

	private static void setContentRangeHeaders(HttpServletResponse response, RangeResponse streamBinaryResponse) {
		if (streamBinaryResponse.getRanged()) {
			Long size = streamBinaryResponse.getSize();
			String sizeString = (size != null) ? String.valueOf(size) : "*";
			String rangeStart = String.valueOf(streamBinaryResponse.getRangeStart());
			String rangeEnd = String.valueOf(streamBinaryResponse.getRangeEnd());
			String contentRange = "bytes ".concat(rangeStart).concat("-").concat(rangeEnd).concat("/").concat(sizeString);
			response.setHeader("Content-Range", contentRange);
			response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

			if (size != null && size <= Integer.MAX_VALUE)
				response.setContentLength(size.intValue());
		}

		response.setHeader("Accept-Ranges", "bytes");
	}

	private static void setDefaultCacheControlHeaders(HttpServletResponse response) {
		response.setHeader("Cache-Control", "no-store, no-cache");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
	}

	private static void setCacheControlHeaders(HttpServletResponse response, CacheControl cacheControl) {
		if (cacheControl == null) {
			setDefaultCacheControlHeaders(response);
			return;
		}

		List<String> ccParts = new ArrayList<>();

		if (cacheControl.getType() != null) {
			switch (cacheControl.getType()) {
				case noCache:
					ccParts.add("no-cache");
					response.setHeader("Pragma", "no-cache");
					break;
				case noStore:
					ccParts.add("no-store");
					break;
				case privateCache:
					ccParts.add("private");
					break;
				case publicCache:
					ccParts.add("public");
					break;
			}
		}

		if (cacheControl.getMaxAge() != null) {
			ccParts.add("max-age=" + cacheControl.getMaxAge());
		}

		if (cacheControl.getMustRevalidate()) {
			ccParts.add("must-revalidate");
		}

		if (ccParts.isEmpty()) {
			setDefaultCacheControlHeaders(response);
		} else {
			response.setHeader("Cache-Control", String.join(", ", ccParts));
		}

		if (cacheControl.getLastModified() != null) {
			response.setDateHeader("Last-Modified", cacheControl.getLastModified().getTime());
		}

		if (cacheControl.getFingerprint() != null) {
			response.setHeader("ETag", cacheControl.getFingerprint());
		}
	}

}
