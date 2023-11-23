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
package com.braintribe.model.processing.resource.streaming;

import static com.braintribe.exception.Exceptions.unchecked;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resourceapi.base.BinaryRequest;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalRequest;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalResponse;
import com.braintribe.model.resourceapi.stream.GetBinary;
import com.braintribe.model.resourceapi.stream.GetBinaryResponse;
import com.braintribe.model.resourceapi.stream.StreamBinary;
import com.braintribe.model.resourceapi.stream.StreamBinaryResponse;
import com.braintribe.model.resourceapi.stream.range.StreamRange;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.RangeInputStream;

/**
 * Base for {@link BinaryRequest} processors which read from the file system. The store/delete methods are not implemented on this level.
 */
public abstract class AbstractFsBasedBinaryRetriever extends AbstractBinaryProcessor {

	@Override
	public StreamBinaryResponse stream(ServiceRequestContext context, StreamBinary request, StreamBinaryResponse response) {
		Path path = resolvePathForRetrieval(request);

		Supplier<InputStream> inputStreamSupplier = () -> openInputStream(path);

		// set response and cut stream
		inputStreamSupplier = rangifyStream(inputStreamSupplier, request, response, path);

		context.notifyResponse(response);

		try (InputStream is = inputStreamSupplier.get(); OutputStream os = request.getCapture().openStream()) {
			IOTools.pump(is, os);
		} catch (IOException e) {
			throw unchecked(e, "Failed to stream from " + path);
		}

		return response;
	}

	private InputStream openInputStream(Path path) {
		try {
			return new BufferedInputStream(Files.newInputStream(path));
		} catch (Exception e) {
			throw unchecked(e, "Could not open input stream for " + path);
		}
	}

	@Override
	public GetBinaryResponse get(ServiceRequestContext context, GetBinary request, GetBinaryResponse response) {
		Path path = resolvePathForRetrieval(request);

		Supplier<InputStream> directInputStreamSupplier = () -> openInputStream(path);

		// set response and cut stream
		Supplier<InputStream> inputStreamSupplier = rangifyStream(directInputStreamSupplier, request, response, path);
		Resource responseResource = Resource.createTransient(() -> inputStreamSupplier.get());

		Resource resource = request.getResource();

		Long streamSize = response.getRanged() //
				? (Long) (response.getRangeEnd() - response.getRangeStart() + 1) //
				: resource.getFileSize();

		responseResource.setName(resource.getName());
		responseResource.setMimeType(resource.getMimeType());
		responseResource.setFileSize(streamSize);
		response.setResource(responseResource);

		return response;
	}

	private Supplier<InputStream> rangifyStream( //
			Supplier<InputStream> inputStreamSupplier, BinaryRetrievalRequest request, BinaryRetrievalResponse response, Path path) {

		StreamRange streamRange = request.getRange();
		if (streamRange == null)
			return inputStreamSupplier;

		Long start = streamRange.getStart();
		if (start == null)
			return inputStreamSupplier;

		try {
			File file = path.toFile();

			Long requestedEnd = streamRange.getEnd();
			if (requestedEnd == null || requestedEnd < 0) 
				requestedEnd = Long.MAX_VALUE;

			Long end = Math.min(requestedEnd, file.length() - 1);

			response.setRanged(true);
			response.setRangeStart(start);
			response.setRangeEnd(end);
			response.setSize(file.length());

			return () -> {
				try {
					return new RangeInputStream(inputStreamSupplier.get(), start, end + 1);
				} catch (Exception e) {
					throw unchecked(e, "Could not wrap input stream with range: " + start + "-" + end);
				}
			};

		} catch (Exception e) {
			throw new RuntimeException("Could not rangify stream according to " + streamRange, e);
		}

	}

	protected abstract Path resolvePathForRetrieval(BinaryRetrievalRequest request);

}
