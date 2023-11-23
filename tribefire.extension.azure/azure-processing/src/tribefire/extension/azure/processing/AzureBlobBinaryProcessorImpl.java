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
package tribefire.extension.azure.processing;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.function.Supplier;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
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
import com.braintribe.model.resourceapi.stream.GetBinary;
import com.braintribe.model.resourceapi.stream.GetBinaryResponse;
import com.braintribe.model.resourceapi.stream.StreamBinary;
import com.braintribe.model.resourceapi.stream.StreamBinaryResponse;
import com.braintribe.model.resourceapi.stream.range.StreamRange;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.RandomTools;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.utils.lcd.StopWatch;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.utils.stream.CountingOutputStream;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;
import com.braintribe.utils.stream.tracking.InputStreamTracker;
import com.braintribe.utils.stream.tracking.OutputStreamTracker;

import tribefire.extension.azure.model.resource.AzureBlobSource;

public class AzureBlobBinaryProcessorImpl extends AbstractBinaryProcessor {

	private static Logger logger = Logger.getLogger(AzureBlobBinaryProcessorImpl.class);

	private String storageConnectionString;
	private String containerName;

	private String pathPrefix = "";

	private InputStreamTracker downloadInputStreamTracker;
	private OutputStreamTracker downloadOutputStreamTracker;

	private StreamPipeFactory streamPipeFactory;

	private BlobContainerClient getContainerClient() {
		// Create a BlobServiceClient object which will be used to create a container client
		BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(storageConnectionString).buildClient();

		BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
		if (blobContainerClient == null) {
			// Create the container and return a container client object
			blobContainerClient = blobServiceClient.createBlobContainer(containerName);
		}
		return blobContainerClient;
	}

	@Override
	public StreamBinaryResponse stream(ServiceRequestContext context, StreamBinary request, StreamBinaryResponse response) {

		Objects.requireNonNull(request, "request must not be null");

		Resource resource = request.getResource();
		StreamRange streamRange = request.getRange();

		return stream(resource, () -> request.getCapture().openStream(), streamRange, context, response);

	}

	protected StreamBinaryResponse stream(Resource resource, Supplier<OutputStream> outputStreamSupplier, StreamRange streamRange,
			ServiceRequestContext context, StreamBinaryResponse response) {
		StopWatch stopWatch = new StopWatch();

		Objects.requireNonNull(resource, "request resource must not be null");

		final AzureBlobSource source = retrieveAzureBlobSource(resource);
		final BlobContainerClient containerClient = getContainerClient();

		String key = source.getKey();
		BlobClient blobClient = containerClient.getBlobClient(key);

		long blobSize = blobClient.getProperties().getBlobSize();
		Long start = streamRange != null ? streamRange.getStart() : null;
		Long end = streamRange != null ? streamRange.getEnd() : null;
		if (end == null) {
			end = blobSize - 1;
		} else if (end >= blobSize) {
			end = blobSize - 1;
		}

		if (start != null) {
			response.setRanged(true);
			response.setRangeStart(start);
			response.setRangeEnd(end);
			response.setSize(blobSize);
		}

		OutputStream os = null;

		InputStream rawInputStream = null;
		try {
			if (context != null) {
				context.notifyResponse(response);
			}

			stopWatch.intermediate("Response Notification");

			if (start != null) {
				BlobInputStreamOptions options = new BlobInputStreamOptions();
				options.setRange(new BlobRange(start, (end - start + 1)));
				rawInputStream = blobClient.openInputStream(options);
			} else {
				rawInputStream = blobClient.openInputStream();
			}

			OutputStream rawOs = outputStreamSupplier.get();

			final OutputStream trackedOutputStream;
			String clientAddress = getRequestorAddress();
			if (downloadOutputStreamTracker != null && clientAddress != null) {
				trackedOutputStream = downloadOutputStreamTracker.wrapOutputStream(rawOs, containerName, clientAddress, resource.getName());
			} else {
				trackedOutputStream = rawOs;
			}

			stopWatch.intermediate("Open stream");

			logger.debug(() -> "Downloading " + key);

			IOTools.transferBytes(rawInputStream, trackedOutputStream);

			stopWatch.intermediate("Transfer");

		} finally {
			IOTools.closeCloseableUnchecked(rawInputStream);
			IOTools.closeCloseableUnchecked(os);
			stopWatch.intermediate("Closed OutputStream");
		}

		logger.debug(() -> "stream: " + stopWatch);

		return response;

	}

	protected static AzureBlobSource retrieveAzureBlobSource(Resource resource) {
		Objects.requireNonNull(resource, "resource must not be null");
		final ResourceSource source = resource.getResourceSource();
		Objects.requireNonNull(source, "resource source must not be null");
		if (source instanceof AzureBlobSource abs) {
			return abs;
		}
		throw new IllegalStateException(AzureBlobBinaryProcessorImpl.class.getName() + " instances cannot handle " + source);
	}

	@Override
	public GetBinaryResponse get(ServiceRequestContext context, GetBinary request, GetBinaryResponse response) {
		Objects.requireNonNull(request, "request must not be null");

		Resource resource = request.getResource();
		StreamRange streamRange = request.getRange();

		return get(resource, streamRange, response);
	}

	protected GetBinaryResponse get(Resource resource, StreamRange streamRange, GetBinaryResponse response) {

		StopWatch stopWatch = new StopWatch();

		Objects.requireNonNull(resource, "request resource must not be null");

		final AzureBlobSource source = retrieveAzureBlobSource(resource);
		final BlobContainerClient containerClient = getContainerClient();

		String key = source.getKey();
		BlobClient blobClient = containerClient.getBlobClient(key);
		long blobSize = blobClient.getProperties().getBlobSize();

		Long start = streamRange != null ? streamRange.getStart() : null;
		Long end = streamRange != null ? streamRange.getEnd() : null;
		if (end == null || end < 0) {
			end = blobSize - 1;
		} else if (end >= blobSize) {
			end = blobSize - 1;
		}

		stopWatch.intermediate("Preparation");

		logger.debug(() -> "Opening " + key);
		final InputStream rawInputStream;
		if (start != null) {
			BlobInputStreamOptions options = new BlobInputStreamOptions();
			options.setRange(new BlobRange(start, (end - start + 1)));
			rawInputStream = blobClient.openInputStream(options);
		} else {
			rawInputStream = blobClient.openInputStream();
		}

		final InputStream trackedInputStream;
		String clientAddress = getRequestorAddress();
		if (downloadInputStreamTracker != null && clientAddress != null) {
			trackedInputStream = downloadInputStreamTracker.wrapInputStream(rawInputStream, containerName, clientAddress, resource.getName());
		} else {
			trackedInputStream = rawInputStream;
		}

		stopWatch.intermediate("Open stream");

		Long streamSize = response.getRanged() ? (Long) (end - start + 1) : blobSize;

		Resource callResource = Resource.createTransient(() -> trackedInputStream);
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

	@Override
	public StoreBinaryResponse store(ServiceRequestContext context, StoreBinary request) {
		Objects.requireNonNull(request, "request must not be null");

		final Resource resource = request.getCreateFrom();

		return store(resource);
	}

	protected StoreBinaryResponse store(Resource resource) {
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

		stopWatch.intermediate("UploadInformation Gathered");

		final BlobContainerClient containerClient = getContainerClient();

		BlobClient blobClient = containerClient.getBlobClient(key);

		Long fileSize = resource.getFileSize();
		if (fileSize == null || fileSize.longValue() < 0L) {
			try (StreamPipe pipe = streamPipeFactory.newPipe(key);
					CountingOutputStream pos = new CountingOutputStream(pipe.acquireOutputStream());
					InputStream in = resource.openStream()) {
				IOTools.transferBytes(in, pos);
				pos.close();
				fileSize = pos.getCount();
				try (InputStream pin = pipe.openInputStream()) {
					blobClient.upload(pin, fileSize);
				}

			} catch (Exception ioe) {
				String msg = "Could not upload resource '" + resource + "'";
				if (ioe instanceof BlobStorageException bse) {
					BlobErrorCode errorCode = bse.getErrorCode();
					msg = msg + ". Reason: '" + errorCode + "'";
				}

				throw Exceptions.unchecked(ioe, msg);
			}
		} else {
			try (InputStream in = resource.openStream()) {
				blobClient.upload(in, fileSize);
			} catch (Exception ioe) {
				String msg = "Could not upload resource '" + resource + "'";
				if (ioe instanceof BlobStorageException bse) {
					BlobErrorCode errorCode = bse.getErrorCode();
					msg = msg + ". Reason: '" + errorCode + "'";
				}
				throw Exceptions.unchecked(ioe, msg);
			}
		}

		stopWatch.intermediate("Transfer");

		final AzureBlobSource resourceSource = AzureBlobSource.T.create();
		resourceSource.setId(resourceId);
		resourceSource.setKey(key);

		final Resource managedResource = createResource(null, resource, resourceSource);

		stopWatch.intermediate("Resource Create");

		stopWatch.intermediate("Resource Populate");

		final StoreBinaryResponse response = StoreBinaryResponse.T.create();
		response.setResource(managedResource);

		stopWatch.intermediate("Result Created");

		logger.debug(() -> "store: " + stopWatch);

		return response;
	}

	protected String sanitize(String name, int maxLength) {
		// Applying rules from https://docs.aws.amazon.com/AmazonS3/latest/dev/UsingMetadata.html
		// + some more rules
		if (name == null) {
			return "null";
		}
		String result = name;

		if (result.equals(".")) {
			return "dot";
		} else if (result.equals("..")) {
			return "dotdot";
		}

		result = result.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

		result = FileTools.truncateFilenameByUtf8BytesLength(result, maxLength);

		return result;
	}

	// Used in tests
	protected StoreBinaryResponse store(Resource resource, PersistenceGmSession gmSession) {
		StoreBinaryResponse response = store(resource);

		GmSessions.cloneIntoSession(response.getResource(), gmSession);

		return response;
	}

	@Override
	public DeleteBinaryResponse delete(ServiceRequestContext context, DeleteBinary request) {
		Objects.requireNonNull(request, "request must not be null");

		final Resource resource = request.getResource();

		return delete(resource);
	}

	protected DeleteBinaryResponse delete(Resource resource) {
		Objects.requireNonNull(resource, "request resource must not be null");

		final AzureBlobSource source = retrieveAzureBlobSource(resource);
		final BlobContainerClient containerClient = getContainerClient();

		String key = source.getKey();
		BlobClient blobClient = containerClient.getBlobClient(key);

		blobClient.deleteIfExists();

		final DeleteBinaryResponse response = DeleteBinaryResponse.T.create();
		return response;
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
	@Required
	public void setStorageConnectionString(String storageConnectionString) {
		this.storageConnectionString = storageConnectionString;
	}
	@Configurable
	@Required
	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}
	@Configurable
	@Required
	public void setStreamPipeFactory(StreamPipeFactory streamPipeFactory) {
		this.streamPipeFactory = streamPipeFactory;
	}

}
