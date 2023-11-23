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
package com.braintribe.model.processing.etcd.service;

import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.DestructionAware;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.exception.Exceptions;
import com.braintribe.integration.etcd.EtcdProcessing;
import com.braintribe.model.etcd.resource.EtcdSource;
import com.braintribe.model.processing.resource.enrichment.ResourceEnrichingStreamer;
import com.braintribe.model.processing.resource.streaming.AbstractBinaryProcessor;
import com.braintribe.model.processing.service.api.ServiceProcessorException;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
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
import com.braintribe.utils.IOTools;
import com.braintribe.utils.RandomTools;

import io.etcd.jetcd.Client;

/**
 * {@link BinaryProcessor} implementation that uses etcd as the backend storage.
 * This is only intended for low-volume accesses where performance is of no
 * concern. If the size of the resource exceeds the configurable limit (default:
 * 1 MB), the file will be split into chunks and stored with multiple keys. <br>
 * <br>
 * Warning: the entire content of the file is temporarily in memory. Hence, be
 * aware that OOM exception might happen if used on larger objects.
 * 
 * @author Roman Kurmanowytsch
 */
public class EtcdBinaryProcessor extends AbstractBinaryProcessor implements DestructionAware {

	protected ResourceEnrichingStreamer enrichingStreamer;

	protected String project = "";

	protected EtcdProcessing processing;

	protected int ttlInSeconds = -1;

	protected int chunkSize = 1 * (int) Numbers.MEGABYTE;

	protected Supplier<Client> clientSupplier;

	protected void connect() {
		if (processing == null) {
			synchronized (this) {
				if (processing == null) {

					processing = new EtcdProcessing(clientSupplier);
					processing.connect();
				}
			}
		}
	}

	protected String getKey(String id) {
		return project.concat("/streaming/").concat(id);
	}

	@Override
	protected StreamBinaryResponse stream(ServiceRequestContext context, StreamBinary request,
			StreamBinaryResponse response) {

		connect();

		Objects.requireNonNull(request, "request must not be null");

		final Resource resource = request.getResource();

		Objects.requireNonNull(resource, "request resource must not be null");

		final EtcdSource source = retrieveEtcdSource(resource);

		String sourceId = source.getId();

		context.notifyResponse(response);

		String prefix = getKey(sourceId) + "/";
		byte[] responseValue = processing.getChunkedBytes(prefix);

		context.notifyResponse(response);

		try (OutputStream os = request.getCapture().openStream();
				InputStream in = new ByteArrayInputStream(responseValue)) {

			IOTools.pump(in, os);

		} catch (Exception e) {
			throw unchecked("Failed to stream " + EtcdSource.class.getSimpleName() + " with id " + sourceId, e);
		}

		return response;

	}

	@Override
	protected GetBinaryResponse get(ServiceRequestContext context, GetBinary request, GetBinaryResponse response) {

		connect();

		Objects.requireNonNull(request, "request must not be null");

		final Resource resource = request.getResource();

		Objects.requireNonNull(resource, "request resource must not be null");

		final EtcdSource source = retrieveEtcdSource(resource);

		String sourceId = source.getId();

		String prefix = getKey(sourceId) + "/";
		byte[] responseValue = processing.getChunkedBytes(prefix);

		InputStream in = new ByteArrayInputStream(responseValue);

		Resource callResource = Resource.createTransient(() -> in);

		response.setResource(callResource);

		return response;

	}

	@Override
	protected StoreBinaryResponse store(ServiceRequestContext context, StoreBinary request) {

		connect();

		Objects.requireNonNull(request, "request must not be null");
		Resource resource = requireNonNull(request.getCreateFrom(), "request resource cannot be null");

		Objects.requireNonNull(resource, "request resource must not be null");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		storeStream(context, resource, baos);

		String sourceId = RandomTools.getRandom32CharactersHexString(true);
		final String keyPrefix = getKey(sourceId);
		processing.putChunkedBytes(chunkId -> {
			return keyPrefix.concat("/").concat(chunkId);
		}, baos.toByteArray(), chunkSize, ttlInSeconds);

		// final EtcdSource resourceSource = context.getSession().create(EtcdSource.T);
		final EtcdSource resourceSource = EtcdSource.T.create();
		resourceSource.setId(sourceId);

		Resource managedResource = createResource(null, resource, resourceSource);
		// final PersistenceGmSession resourceSession = request.getPersistResource() ?
		// context.getSession() : null;
		// final Resource managedResource = createResource(resourceSession, resource,
		// resourceSource);

		final StoreBinaryResponse response = StoreBinaryResponse.T.create();
		response.setResource(managedResource);

		return response;

	}

	@Override
	protected DeleteBinaryResponse delete(ServiceRequestContext context, DeleteBinary request) {

		connect();

		Objects.requireNonNull(request, "request must not be null");

		final Resource resource = request.getResource();

		Objects.requireNonNull(resource, "request resource must not be null");

		final EtcdSource source = retrieveEtcdSource(resource);

		String sourceId = source.getId();

		try {

			List<String> keysToDelete = processing.getAllKeysWithPrefix(getKey(sourceId) + "/");
			for (String k : keysToDelete) {
				processing.delete(k);
			}
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not delete resource with id " + sourceId);
		}

		final DeleteBinaryResponse response = DeleteBinaryResponse.T.create();

		return response;

	}

	protected static EtcdSource retrieveEtcdSource(Resource resource) {

		Objects.requireNonNull(resource, "resource must not be null");

		final ResourceSource source = resource.getResourceSource();

		Objects.requireNonNull(source, "resource source must not be null");

		if (source instanceof EtcdSource) {
			return (EtcdSource) source;
		}

		throw new IllegalStateException(EtcdBinaryProcessor.class.getName() + " instances cannot handle " + source);

	}

	protected void storeStream(ServiceRequestContext context, Resource callResource, OutputStream out) {

		// @formatter:off
		try {
			
			if (enrichingStreamer == null) {
				try (InputStream in = callResource.openStream()) {
					IOTools.pump(in, out);
				}
				
			} else {
				enrichingStreamer
					.stream()
						.from(() -> callResource.openStream())
						.to(() -> out)
						.context(context)
						.enriching(callResource);
			}

		} catch (Exception e) {
			throw unchecked("Failed to stream", e);
		}
		// @formatter:on

	}

	@Configurable
	public void setEnrichingStreamer(ResourceEnrichingStreamer enrichingStreamer) {
		this.enrichingStreamer = enrichingStreamer;
	}

	private static RuntimeException unchecked(String message, Exception checked) {

		if (checked instanceof RuntimeException) {
			return (RuntimeException) checked;
		}

		String msg = (message != null) ? message + (checked.getMessage() != null ? ": " + checked.getMessage() : "")
				: checked.getMessage();

		if (checked instanceof IOException) {
			return new UncheckedIOException(msg, (IOException) checked);
		} else {
			return new ServiceProcessorException(checked);
		}

	}

	@Configurable
	@Required
	public void setProject(String project) {
		this.project = project;
	}

	@Configurable
	public void setTtlInSeconds(int ttlInSeconds) {
		this.ttlInSeconds = ttlInSeconds;
	}

	@Configurable
	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	@Override
	public void preDestroy() {
		if (processing != null) {
			processing.preDestroy();
		}
	}

	@Configurable
	@Required
	public void setClientSupplier(Supplier<Client> clientSupplier) {
		this.clientSupplier = clientSupplier;
	}

}
