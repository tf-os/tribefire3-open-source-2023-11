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
package com.braintribe.gm.marshaller.threshold;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Date;
import java.util.Objects;
import java.util.function.BiConsumer;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resourceapi.persistence.UploadResource;
import com.braintribe.model.resourceapi.persistence.UploadResourceResponse;
import com.braintribe.model.resourceapi.stream.GetBinaryResponse;
import com.braintribe.model.resourceapi.stream.GetResource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.CountingOutputStream;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;
import com.braintribe.utils.stream.api.StreamPipes;

/**
 * ThresholdPeristenceMarshaller uses any other marshaller as {@link #setDelegate(Marshaller) delegate} to do a primary marshalling.
 * The data of that delegate marshalling is being captured to measure the binary size of the data. 
 * The size is then compared with the the configured {@link #setThreshold(long) threshold}.
 * If the size is larger the captured binary data will be stored with {@link UploadResource} in the access configured 
 * with {@link #setAccessId(String)}. After an initial signal header byte for substitute marshalling the created 
 * Resource will be marshalled as substitute with the {@link #setSubstituteResourceMarshaller(Marshaller) substitute marshaller}.
 * If the size is smaller the captured binary data will be directly transferred after an initial signal header byte for payload marshalling.
 * 
 * @author Roman Kurmanowytsch
 * @author Dirk Scheffler
 */
public class ThresholdPersistenceMarshaller implements Marshaller {
	private static final int HEADER_PAYLOAD = 'P';
	private static final int HEADER_SUBSTITUTE = 'S';
	
	private Marshaller delegate;
	private Marshaller substituteResourceMarshaller;
	private StreamPipeFactory streamPipeFactory;
	private long threshold = Numbers.MEGABYTE;
	private String accessId;
	private Evaluator<ServiceRequest> evaluator;
	
	/**
	 * Configures the optional substitute marshaller. If not configured the {@link #setDelegate(Marshaller) delegate marshaller}
	 * will be used for substitute marshalling as well.
	 */
	@Configurable
	public void setSubstituteResourceMarshaller(Marshaller substituteResourceMarshaller) {
		this.substituteResourceMarshaller = substituteResourceMarshaller;
	}
	
	/**
	 * Configures the threshold at which substitute marshalling will be used.
	 * @param threshold
	 */
	@Required
	public void setThreshold(long threshold) {
		this.threshold = threshold;
	}
	
	@Required
	public void setDelegate(Marshaller delegate) {
		this.delegate = delegate;
	}
	
	@Required
	public void setEvaluator(Evaluator<ServiceRequest> evaluator) {
		this.evaluator = evaluator;
	}
	
	@Required
	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}
	
	@Configurable
	public void setStreamPipeFactory(StreamPipeFactory streamPipeFactory) {
		this.streamPipeFactory = streamPipeFactory;
	}
	
	public Marshaller getSubstituteResourceMarshaller() {
		if (substituteResourceMarshaller != null)
			return substituteResourceMarshaller;
		
		return delegate;
	}
	
	public StreamPipeFactory getStreamPipeFactory() {
		if (streamPipeFactory == null) {
			streamPipeFactory = StreamPipes.simpleFactory();
		}

		return streamPipeFactory;
	}
	
	@Override
	public void marshall(OutputStream out, Object value, GmSerializationOptions options) throws MarshallException {
		// TODO: think about zipping
		
		StreamPipe pipe = getStreamPipeFactory().newPipe("threshold-marshalling");
		
		BiConsumer<String, Resource> onThresholdResourceCreated = options.findAttribute(OnThresholdResourceCreated.class) //
				.orElse((a, r) -> { /* noop */ });
		
		long size = 0;
		
		try (CountingOutputStream captureOut = new CountingOutputStream(pipe.acquireOutputStream())) {
			delegate.marshall(captureOut, value, options);
			size = captureOut.getCount();
		} catch (IOException e) {
			throw new MarshallException(e);
		}
		
		if (size > threshold) {
			Resource resource = Resource.createTransient(pipe::openInputStream);
			resource.setFileSize(size);
			resource.setMimeType("application/octet-stream");
			resource.setCreated(new Date());

			UploadResource uploadResource = UploadResource.T.create();
			uploadResource.setDomainId(accessId);
			uploadResource.setResource(resource);
			uploadResource.setUseCase("threshold-marshalling");
			
			UploadResourceResponse response = uploadResource.eval(evaluator).get();
			
			Resource substitute = response.getResource();
			
			onThresholdResourceCreated.accept(accessId, substitute);
			
			Objects.requireNonNull(substitute, "UploadResource failed to deliver a Resource but did not throw an Exception");
			
			try {
				out.write(HEADER_SUBSTITUTE);
				getSubstituteResourceMarshaller().marshall(out, substitute);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		else {
			try {
				out.write(HEADER_PAYLOAD);
				try (InputStream in = pipe.openInputStream()) {
					IOTools.transferBytes(in, out, IOTools.BUFFER_SUPPLIER_64K);
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	@Override
	public Object unmarshall(InputStream in, GmDeserializationOptions options) throws MarshallException {
		try {
			int header = in.read();
			
			switch (header) {
				case HEADER_PAYLOAD:
					return delegate.unmarshall(in, options);
				case HEADER_SUBSTITUTE:
					return unmarshallBySubstitute(in, options);
				default:
					throw new IllegalStateException("Received unexpected header: " + header);
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private Object unmarshallBySubstitute(InputStream in, GmDeserializationOptions options) throws IOException {
		Resource resource = (Resource) getSubstituteResourceMarshaller().unmarshall(in);

		GetResource getResource = GetResource.T.create();
		getResource.setResource(resource);
		getResource.setDomainId(accessId);
		
		try {
			GetBinaryResponse response = getResource.eval(evaluator).get();
			try (InputStream payloadIn = response.getResource().openStream()) {
				return delegate.unmarshall(payloadIn, options);
			}
		}
		catch (RuntimeException e) {
			throw Exceptions.unchecked(e, "Error while unmarshalling transient message data. Could not get substitute resource [" +resource + "] from access [" + accessId + "]. "
					+ "Most likely the resource was cleaned up already.");
		}
			
	}

}
