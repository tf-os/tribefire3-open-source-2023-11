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
package com.braintribe.model.processing.gcp.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.gcp.resource.GcpStorageSource;
import com.braintribe.model.processing.gcp.connect.GcpStorage;
import com.braintribe.model.processing.gcp.connect.GcpStorageConnector;
import com.braintribe.model.processing.resource.streaming.AbstractBinaryProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.api.aspect.RequestorAddressAspect;
import com.braintribe.model.processing.session.api.common.GmSessions;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resourceapi.persistence.DeleteBinary;
import com.braintribe.model.resourceapi.persistence.DeleteBinaryResponse;
import com.braintribe.model.resourceapi.persistence.StoreBinary;
import com.braintribe.model.resourceapi.persistence.StoreBinaryResponse;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalResponse;
import com.braintribe.model.resourceapi.stream.GetBinary;
import com.braintribe.model.resourceapi.stream.GetBinaryResponse;
import com.braintribe.model.resourceapi.stream.StreamBinary;
import com.braintribe.model.resourceapi.stream.StreamBinaryResponse;
import com.braintribe.model.resourceapi.stream.range.StreamRange;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.RandomTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.utils.lcd.StopWatch;
import com.braintribe.utils.stream.RangeInputStream;
import com.braintribe.utils.stream.tracking.InputStreamTracker;
import com.braintribe.utils.stream.tracking.OutputStreamTracker;
import com.google.common.io.ByteStreams;

public class GcpStorageBinaryProcessor extends AbstractBinaryProcessor {

	private static Logger logger = Logger.getLogger(GcpStorageBinaryProcessor.class);

	private GcpStorageConnector connector = null;
	private String connectionName;
	private String bucketName;
	protected String pathPrefix = "";

	private InputStreamTracker downloadInputStreamTracker;
	private OutputStreamTracker downloadOutputStreamTracker;

	@Override
	public StreamBinaryResponse stream(ServiceRequestContext context, StreamBinary request, StreamBinaryResponse response) {
		Objects.requireNonNull(request, "request must not be null");

		final Resource resource = request.getResource();
		final StreamRange streamRange = request.getRange();

		return stream(resource, () -> request.getCapture().openStream(), streamRange, context, response);
	}

	protected StreamBinaryResponse stream(Resource resource, Supplier<OutputStream> outputStreamSupplier, StreamRange streamRange,
			ServiceRequestContext context, StreamBinaryResponse response) {

		StopWatch stopWatch = new StopWatch();

		Objects.requireNonNull(resource, "request resource must not be null");

		final GcpStorageSource source = retrieveStorageSource(resource);

		String key = source.getKey();

		response.setSize(resource.getFileSize());

		OutputStream os = null;

		try {

			if (context != null) {
				context.notifyResponse(response);
			}

			stopWatch.intermediate("Response Notification");

			OutputStream rawOs = outputStreamSupplier.get();

			final OutputStream trackedOutputStream;
			String clientAddress = getRequestorAddress();
			if (downloadOutputStreamTracker != null && clientAddress != null) {
				trackedOutputStream = downloadOutputStreamTracker.wrapOutputStream(rawOs, connectionName, clientAddress, resource.getName());
			} else {
				trackedOutputStream = rawOs;
			}

			stopWatch.intermediate("Open stream");

			// set response and cut stream
			Long start = streamRange != null ? streamRange.getStart() : null;
			Long end = streamRange != null ? streamRange.getEnd() : null;
			if (end == null) {
				end = resource.getFileSize() - 1;
			} else if (end >= resource.getFileSize()) {
				end = resource.getFileSize() - 1;
			}
			os = rangifyOutputStream(trackedOutputStream, start, end, response);

			stopWatch.intermediate("Rangify OutputStream");

			String bucketName = source.getBucketName();
			if (bucketName == null) {
				bucketName = this.bucketName;
			}
			logger.debug(() -> "Downloading " + key);

			GcpStorage storage = connector.getStorage();

			stopWatch.intermediate("Create Channels");

			try (ReadableByteChannel reader = storage.openReadChannel(bucketName, key, start)) {
				try (InputStream inputStream = Channels.newInputStream(reader)) {
					Long streamEnd = end;
					if (start != null && start > 0) {
						streamEnd = streamEnd - start;
					}
					try (InputStream rangedInputStream = new RangeInputStream(inputStream, 0L, streamEnd + 1)) {
						IOTools.pump(rangedInputStream, os);
					}
				}
			} catch (IOException ioe) {
				throw Exceptions.unchecked(ioe, "Could not download " + key);
			}

			stopWatch.intermediate("Transfer");

		} finally {
			IOTools.closeCloseableUnchecked(os);
			stopWatch.intermediate("Closed OutputStream");
		}

		logger.debug(() -> "stream: " + stopWatch);

		return response;
	}

	private static GcpStorageSource retrieveStorageSource(Resource resource) {
		Objects.requireNonNull(resource, "resource must not be null");
		final ResourceSource source = resource.getResourceSource();
		Objects.requireNonNull(source, "resource source must not be null");
		if (source instanceof GcpStorageSource) {
			return (GcpStorageSource) source;
		}
		throw new IllegalStateException(GcpStorageBinaryProcessor.class.getName() + " instances cannot handle " + source);
	}

	private OutputStream rangifyOutputStream(OutputStream rawOutoutStream, Long start, Long requestedEnd, StreamBinaryResponse response) {
		OutputStream result = rawOutoutStream;
		try {
			if (start != null) {

				response.setRanged(true);
				response.setRangeStart(start);
				response.setRangeEnd(requestedEnd);
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not rangify stream according to " + start + "-" + requestedEnd, e);
		}
		return result;
	}

	@Override
	public GetBinaryResponse get(ServiceRequestContext context, GetBinary request, GetBinaryResponse response) {
		Objects.requireNonNull(request, "request must not be null");

		final Resource resource = request.getResource();
		final StreamRange streamRange = request.getRange();

		return get(resource, streamRange, response);
	}

	protected GetBinaryResponse get(Resource resource, StreamRange streamRange, GetBinaryResponse response) {

		StopWatch stopWatch = new StopWatch();

		Objects.requireNonNull(resource, "request resource must not be null");

		final GcpStorageSource source = retrieveStorageSource(resource);

		String key = source.getKey();
		String bucketName = source.getBucketName();
		if (bucketName == null) {
			bucketName = this.bucketName;
		}

		Long start = streamRange != null ? streamRange.getStart() : null;
		Long end = streamRange != null ? streamRange.getEnd() : null;
		if (end == null) {
			end = resource.getFileSize() - 1;
		} else if (end >= resource.getFileSize()) {
			end = resource.getFileSize() - 1;
		}

		logger.debug(() -> "Opening " + key);

		stopWatch.intermediate("Preparation");

		GcpStorage storage = connector.getStorage();

		InputStream rawInputStream;
		try {
			rawInputStream = storage.openInputStream(bucketName, key, start);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not open stream for key " + key + " in bucket " + bucketName + " and start " + start);
		}

		final InputStream trackedInputStream;
		String clientAddress = getRequestorAddress();
		if (downloadInputStreamTracker != null && clientAddress != null) {
			trackedInputStream = downloadInputStreamTracker.wrapInputStream(rawInputStream, connectionName, clientAddress, resource.getName());
		} else {
			trackedInputStream = rawInputStream;
		}

		stopWatch.intermediate("Open stream");

		// set response and cut stream
		final InputStream inputStream = rangifyInputStream(trackedInputStream, start, end, resource.getFileSize(), response);

		stopWatch.intermediate("InputStream Rangify");

		long streamSize = response.getRanged() ? end - start + 1 : resource.getFileSize();

		Resource callResource = Resource.createTransient(() -> inputStream);
		callResource.setName(resource.getName());
		callResource.setMimeType(resource.getMimeType());
		callResource.setFileSize(streamSize);
		callResource.setMimeType(resource.getMimeType());

		response.setResource(callResource);

		stopWatch.intermediate("Resource Assigned");

		logger.debug(() -> "get: " + stopWatch);

		return response;
	}

	private String getRequestorAddress() {
		AttributeContext context = AttributeContexts.peek();
		return context.findAttribute(RequestorAddressAspect.class).orElse(null);
	}

	private InputStream rangifyInputStream(InputStream rawInputStream, Long start, Long requestedEnd, Long totalResourceSize,
			BinaryRetrievalResponse response) {
		InputStream result = rawInputStream;
		try {
			if (start != null) {

				result = new RangeInputStream(rawInputStream, 0L, requestedEnd - start + 1);

				response.setRanged(true);
				response.setRangeStart(start);
				response.setRangeEnd(requestedEnd);
				response.setSize(totalResourceSize);
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not rangify stream according to " + start + "-" + requestedEnd, e);
		}
		return result;
	}

	@Override
	public StoreBinaryResponse store(ServiceRequestContext context, StoreBinary request) {
		Objects.requireNonNull(request, "request must not be null");

		final Resource resource = request.getCreateFrom();

		return store(resource);
	}

	// Used in tests
	protected StoreBinaryResponse store(Resource resource, PersistenceGmSession gmSession) {
		StoreBinaryResponse response = store(resource);

		GmSessions.cloneIntoSession(response.getResource(), gmSession);

		return response;
	}

	private StoreBinaryResponse store(Resource resource) {

		StopWatch stopWatch = new StopWatch();

		Objects.requireNonNull(resource, "request resource must not be null");

		String resourceId = resource.getId();
		String relPathId;
		if (resourceId == null) {
			resourceId = RandomTools.newStandardUuid();
			relPathId = resourceId;
		} else {
			relPathId = RandomTools.newStandardUuid();
		}

		String relPath = pathPrefix + relPathId.substring(0, 4) + "/" + relPathId.substring(4, 8) + "/" + relPathId.substring(8, 10) + "/"
				+ resourceId;

		String key = relPath + "/" + sanitize(resource.getName(), 1024 - relPath.length() - 1);

		stopWatch.intermediate("Preparation");

		logger.debug(() -> "Storing " + key);

		Resource managedResource = null;

		GcpStorage storage = connector.getStorage();
		connector.ensureBucket(bucketName);

		stopWatch.intermediate("Ensure Bucket");

		try (InputStream in = resource.openStream()) {

			stopWatch.intermediate("Stream Open");

			WritableByteChannel writer = storage.openWriteChannel(bucketName, key, resource.getMimeType());

			ByteStreams.copy(Channels.newChannel(in), writer);

			writer.close();

			stopWatch.intermediate("Transfer");

		} catch (IOException ioe) {
			throw Exceptions.unchecked(ioe, "Could not upload resource " + resource);
		}

		final GcpStorageSource resourceSource = GcpStorageSource.T.create();
		resourceSource.setId(resourceId);
		resourceSource.setBucketName(bucketName);
		resourceSource.setKey(key);

		managedResource = createResource(null, resource, resourceSource);

		stopWatch.intermediate("Resource Create");

		stopWatch.intermediate("Resource Populate");

		final StoreBinaryResponse response = StoreBinaryResponse.T.create();
		response.setResource(managedResource);

		stopWatch.intermediate("Result Created");

		logger.debug(() -> "store: " + stopWatch);

		return response;

	}

	protected String sanitize(String name, int maxLength) {
		// Applying rules from https://cloud.google.com/storage/docs/naming-objects
		if (name == null) {
			return "null";
		}
		String result = name;

		if (result.equals(".")) {
			return "dot";
		} else if (result.equals("..")) {
			return "dotdot";
		}

		if (result.startsWith(".well-known/acme-challenge/")) {
			result = result.substring(1);
		}

		result = result.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

		//@formatter:off
		// This works, too.... but let's keep it simple
		// result = result.replaceAll("[\\u0000-\\u001f\\/:*?\\\"<>| \\&\\$\\u007f-\\u0084\\u0086-\\u009f\\,@\\=\\;\\+#\\[\\]]", "_"); //
		//@formatter:on

		result = FileTools.truncateFilenameByUtf8BytesLength(result, maxLength);

		return result;
	}

	@Override
	public DeleteBinaryResponse delete(ServiceRequestContext context, DeleteBinary request) {

		StopWatch stopWatch = new StopWatch();

		Objects.requireNonNull(request, "request must not be null");

		final Resource resource = request.getResource();

		Objects.requireNonNull(resource, "request resource must not be null");

		final GcpStorageSource source = retrieveStorageSource(resource);

		String key = source.getKey();
		String bucketName = source.getBucketName();
		if (bucketName == null) {
			bucketName = this.bucketName;
		}

		stopWatch.intermediate("Preparation");

		logger.debug(() -> "Deleting " + key);

		GcpStorage storage = connector.getStorage();
		storage.deleteBlob(bucketName, key);

		stopWatch.intermediate("Deleted");

		logger.debug(() -> "delete: " + stopWatch);

		final DeleteBinaryResponse response = DeleteBinaryResponse.T.create();
		return response;
	}

	@Required
	@Configurable
	public void setConnector(GcpStorageConnector connector) {
		this.connector = connector;
	}
	@Required
	@Configurable
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
	@Configurable
	public void setPathPrefix(String pathPrefix) {
		if (!StringTools.isBlank(pathPrefix)) {
			if (!pathPrefix.endsWith("/")) {
				pathPrefix = pathPrefix + "/";
			}
			this.pathPrefix = pathPrefix;
		}
	}
	@Configurable
	public void setDownloadInputStreamTracker(InputStreamTracker inputStreamTracker) {
		this.downloadInputStreamTracker = inputStreamTracker;
	}
	@Configurable
	public void setDownloadOutputStreamTracker(OutputStreamTracker downloadOutputStreamTracker) {
		this.downloadOutputStreamTracker = downloadOutputStreamTracker;
	}
	@Configurable
	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

}
