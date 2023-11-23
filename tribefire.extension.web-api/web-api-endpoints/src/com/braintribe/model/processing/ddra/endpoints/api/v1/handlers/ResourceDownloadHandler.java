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
package com.braintribe.model.processing.ddra.endpoints.api.v1.handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.ddra.endpoints.api.api.v1.ApiV1EndpointContext;
import com.braintribe.exception.HttpException;
import com.braintribe.model.cache.CacheControl;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resourceapi.request.ResourceDownloadRequest;
import com.braintribe.model.resourceapi.stream.StreamBinaryResponse;
import com.braintribe.model.resourceapi.stream.condition.FingerprintMismatch;
import com.braintribe.model.resourceapi.stream.condition.ModifiedSince;
import com.braintribe.model.resourceapi.stream.condition.StreamCondition;
import com.braintribe.model.resourceapi.stream.range.StreamRange;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.StringTools;

public class ResourceDownloadHandler extends ResourceHandler {

	private final DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

	public static boolean handleRequest(ApiV1EndpointContext context, ServiceRequest service, PersistenceGmSessionFactory userSessionFactory,
			ModelAccessoryFactory modelAccessoryFactory) {

		if (!ResourceDownloadRequest.T.isInstance(service))
			return false;

		return new ResourceDownloadHandler(context, (ResourceDownloadRequest) service, userSessionFactory, modelAccessoryFactory).handle();
	}

	private final ApiV1EndpointContext context;
	private final ResourceDownloadRequest resourceDownloadRequest;

	public ResourceDownloadHandler(ApiV1EndpointContext context, ResourceDownloadRequest downloadRequest,
			PersistenceGmSessionFactory userSessionFactory, ModelAccessoryFactory modelAccessoryFactory) {
		this.context = context;
		this.resourceDownloadRequest = downloadRequest;
		this.sessionFactory = userSessionFactory;
		this.modelAccessoryFactory = modelAccessoryFactory;
	}

	private boolean handle() {
		HttpServletRequest request = context.getRequest();
		resolveIfNoneMatchAndModifiedSinceAndRange(request);

		PersistenceGmSession gmSession = openSession(resourceDownloadRequest.getAccessId());
		Resource resource = retrieveResource(gmSession, resourceDownloadRequest.getResourceId(), resourceDownloadRequest.getAccessId());
		// stream
		stream(context.getResponse(), gmSession, resource);

		return true;
	}

	private void resolveIfNoneMatchAndModifiedSinceAndRange(HttpServletRequest request) {
		resourceDownloadRequest.setIfNoneMatch(request.getHeader("If-None-Match"));
		resolveIfModifiedSince(request);
		parseRangeHeader(request);
	}

	private void resolveIfModifiedSince(HttpServletRequest request) {
		long IfModifiedSince = request.getDateHeader("If-Modified-Since");
		if (IfModifiedSince > -1)
			resourceDownloadRequest.setIfModifiedSince(dateFormat.format(new Date(IfModifiedSince)));
	}

	protected void parseRangeHeader(HttpServletRequest request) {

		String rangeHeader = request.getHeader("Range");
		if (StringTools.isBlank(rangeHeader))
			return;

		int index = rangeHeader.indexOf('=');
		if (index == -1)
			throw new HttpException(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE, "There is no '=' sign.");

		String unit = rangeHeader.substring(0, index).trim();
		if (StringTools.isBlank(unit) || !unit.equalsIgnoreCase("bytes"))
			throw new HttpException(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE, "Only unit 'bytes' is supported.");

		String rangeSpec = rangeHeader.substring(index + 1).trim();
		index = rangeSpec.indexOf('-');
		if (index == -1)
			throw new HttpException(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE, "The range value " + rangeSpec + " does not contain '-'");

		String start = rangeSpec.substring(0, index).trim();
		String end = null;
		if (index < rangeSpec.length() - 1)
			end = rangeSpec.substring(index + 1).trim();

		long startLong = Long.parseLong(start);
		long endLong = -1;
		if (!StringTools.isBlank(end))
			endLong = Long.parseLong(end);

		resourceDownloadRequest.setRangeStart(startLong);
		resourceDownloadRequest.setRangeEnd(endLong);
	}

	protected void stream(HttpServletResponse response, PersistenceGmSession gmSession, Resource resource) {
		try {
			OutputStream outputStream = response.getOutputStream();
			StreamCondition condition = streamCondition(resourceDownloadRequest);
			StreamRange range = streamRange(resourceDownloadRequest);

			// @formatter:off
			gmSession.resources()
						.retrieve(resource)
							.condition(condition)
							.onResponse(
								r -> {
									setDownloadHeaders((StreamBinaryResponse) r, response, resource, resourceDownloadRequest);
								}
							)
							.range(range)
							.stream(outputStream);
			// @formatter:on

			outputStream.flush();
		} catch (IOException e) {
			throw new RuntimeException("Failed to write response: " + e.getMessage(), e);
		}

	}

	private StreamCondition streamCondition(ResourceDownloadRequest streamingRequest) {

		if (streamingRequest.getIfNoneMatch() != null) {
			FingerprintMismatch condition = FingerprintMismatch.T.create();
			condition.setFingerprint(streamingRequest.getIfNoneMatch());
			return condition;
		} else if (streamingRequest.getIfModifiedSince() != null) {
			try {
				ModifiedSince condition = ModifiedSince.T.create();
				condition.setDate(dateFormat.parse(streamingRequest.getIfModifiedSince()));
				return condition;
			} catch (ParseException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}

		return null;
	}

	private StreamRange streamRange(ResourceDownloadRequest streamingRequest) {

		long rangeStart = streamingRequest.getRangeStart();
		long rangeEnd = streamingRequest.getRangeEnd();

		if (rangeEnd == rangeStart || rangeStart < 0 || rangeEnd < rangeStart)
			return null;

		StreamRange range = StreamRange.T.create();
		range.setStart(rangeStart);
		range.setEnd(rangeEnd);

		return range;
	}

	private void setDownloadHeaders(StreamBinaryResponse response, HttpServletResponse httpResponse, Resource resource,
			ResourceDownloadRequest streamingRequest) {

		if (response.getNotStreamed())
			httpResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
		else
			setContentHeaders(httpResponse, resource, streamingRequest, response);

		setCacheControlHeaders(httpResponse, response.getCacheControl(), streamingRequest.getNoCache());
	}

	private void setContentHeaders(HttpServletResponse response, Resource resource, ResourceDownloadRequest streamingRequest,
			StreamBinaryResponse streamBinaryResponse) {

		response.setHeader("Content-Disposition", streamingRequest.getDownload() ? "attachment" : "inline");

		String mimeType = null;
		if (streamingRequest.getDownload())
			mimeType = "application/download";
		else
			mimeType = resource.getMimeType();

		response.setContentType(mimeType);

		boolean ranged = streamBinaryResponse.getRanged();
		if (ranged) {
			Long size = streamBinaryResponse.getSize();
			String sizeString = (size != null) ? String.valueOf(size) : "*";
			String rangeStart = String.valueOf(streamBinaryResponse.getRangeStart());
			String rangeEnd = String.valueOf(streamBinaryResponse.getRangeEnd());
			String contentRange = "bytes ".concat(rangeStart).concat("-").concat(rangeEnd).concat("/").concat(sizeString);
			response.setHeader("Content-Range", contentRange);
			response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
		}

	}

	private void setCacheControlHeaders(HttpServletResponse response, CacheControl cacheControl, boolean forceNoCache) {

		if (cacheControl == null || forceNoCache)
			setDefaultCacheControlHeaders(response);
		else {
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

			if (cacheControl.getMaxAge() != null)
				ccParts.add("max-age=" + cacheControl.getMaxAge());

			if (cacheControl.getMustRevalidate())
				ccParts.add("must-revalidate");

			if (ccParts.isEmpty())
				setDefaultCacheControlHeaders(response);
			else
				response.setHeader("Cache-Control", String.join(", ", ccParts));

			if (cacheControl.getLastModified() != null)
				response.setDateHeader("Last-Modified", cacheControl.getLastModified().getTime());

			if (cacheControl.getFingerprint() != null)
				response.setHeader("ETag", cacheControl.getFingerprint());

		}

	}

	private void setDefaultCacheControlHeaders(HttpServletResponse response) {
		response.setHeader("Cache-Control", "no-store, no-cache");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
	}

}
