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
package com.braintribe.cartridge.common.processing.streaming;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.mimetype.MimeTypeDetector;
import com.braintribe.mimetype.PlatformMimeTypeDetector;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.resource.enrichment.ResourceEnrichingStreamer;
import com.braintribe.model.processing.resource.enrichment.ResourceSpecificationDetector;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.specification.ResourceSpecification;
import com.braintribe.utils.lcd.IOTools;
import com.braintribe.utils.stream.MultiplierOutputStream;
import com.braintribe.utils.stream.WriteOnReadInputStream;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;

/**
 * <p>
 * A standard {@link ResourceEnrichingStreamer} implementation.
 * 
 */
public class StandardResourceEnrichingStreamer implements ResourceEnrichingStreamer, StandardResourceProcessor {

	private MimeTypeDetector detector = PlatformMimeTypeDetector.instance;
	private ResourceSpecificationDetector<?> specificationDetector = null;
	private StreamPipeFactory streamPipeFactory;
	private int consumerBufferSize = 8096;
	private String digestAlgorithm = "MD5";

	@Configurable
	public void setMimeTypeDetector(MimeTypeDetector mimeTypeDetector) {
		Objects.requireNonNull(mimeTypeDetector, "mimeTypeDetector cannot be set to null");
		this.detector = mimeTypeDetector;
	}

	@Configurable
	public void setConsumerBufferSize(int consumerBufferSize) {
		Objects.requireNonNull(consumerBufferSize, "consumerBufferSize cannot be set to null");
		this.consumerBufferSize = consumerBufferSize;
	}

	@Configurable
	public void setDigestAlgorithm(String digestAlgorithm) {
		Objects.requireNonNull(digestAlgorithm, "digestAlgorithm cannot be set to null");
		this.digestAlgorithm = digestAlgorithm;
	}
	
	@Configurable
	public void setSpecificationDetector(ResourceSpecificationDetector<?> specificationDetector) {
		this.specificationDetector = specificationDetector;
	}

	@Configurable
	public void setStreamPipeFactory(StreamPipeFactory streamPipeFactory) {
		this.streamPipeFactory = streamPipeFactory;
	}
	
	@Override
	public ResourceEnrichingStreamerBuilder stream() {
		return new StandardInternalResourceEnricherBuilder();
	}

	protected boolean enrich(Resource resource, Supplier<InputStream> inputSupplier, Supplier<OutputStream> outputSupplier,
			ServiceRequestContext context, boolean onlyIfEnriched) throws IOException {

		boolean enrichId = unset(resource.getId());
		boolean enrichMd5 = unset(resource.getMd5());
		boolean enrichMimeType = unset(resource.getMimeType());
		boolean enrichSize = resource.getFileSize() == null;
		boolean enrichCreated = resource.getCreated() == null;
		boolean enrichCreator = unset(resource.getCreator());
		boolean enrichSpecification = resource.getSpecification() == null && specificationDetector != null;

		if (!(enrichId || enrichMd5 || enrichMimeType || enrichSize || enrichCreated || enrichCreator || enrichSpecification)) {
			if (!onlyIfEnriched) {
				stream(resource, inputSupplier, outputSupplier);
			}
			return false;
		}

		boolean enriched = false;
		
		if (enrichId) {
			if (resource.getGlobalId() != null) {
				resource.setId(resource.getGlobalId());
			} else {
				resource.setId(UUID.randomUUID().toString());
			}
			enriched = true;
		}

		if (enrichCreated) {
			resource.setCreated(new Date());
			enriched = true;
		}

		if (enrichCreator && context != null) {
			String creator = context.getRequestorUserName();
			if (!unset(creator)) {
				resource.setCreator(creator);
				enriched = true;
			}
		}

		if (!(enrichMd5 || enrichMimeType || enrichSize)) {
			if (!onlyIfEnriched) {
				stream(resource, inputSupplier, outputSupplier);
			}
			return enriched;
		}

		MessageDigest md = null;

		if (enrichMd5) {
			md = createMessageDigest(digestAlgorithm);
		}
		
		List<OutputStream> outputStreams = new ArrayList<>();
		
		if (outputSupplier != null) {
			outputStreams.add(outputSupplier.get());
		}
		
		// Buffer the input stream in a pipe when the specification in going to be enriched so it doesn't have to be opened twice
		Supplier<InputStream> specificationEnritchingInputStreamSupplier = inputSupplier;
		if (enrichSpecification && streamPipeFactory != null) {
			StreamPipe pipe = streamPipeFactory.newPipe(this.getClass().getSimpleName());
			outputStreams.add(pipe.openOutputStream());
			specificationEnritchingInputStreamSupplier = () -> {
				try {
					return pipe.openInputStream();
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			};
		}
		
		// @formatter:off
		try (
			OutputStream out = new MultiplierOutputStream(outputStreams); 
			InputStream	rootIn  = rootInputStream(resource, inputSupplier);
			WriteOnReadInputStream in = enrichingInputStream(rootIn, out, md);
		) {
		// @formatter:on

			long size = -1;

			if (enrichMimeType) {
				String mimeType = detector.getMimeType(in, resource.getName());
				resource.setMimeType(mimeType);
				in.consume(new byte[consumerBufferSize]);
				size = in.getWriteCount();
			} else {
				size = consume(in);
			}

			if (enrichSize) {
				resource.setFileSize(size);
			}

			if (md != null) {
				resource.setMd5(digest(md));
			}

			enriched = true;

		}
		
		String resourceMimeType = resource.getMimeType();
		if (enrichSpecification && resourceMimeType != null) {
			
			InputStream	in  = rootInputStream(resource, specificationEnritchingInputStreamSupplier);
			try {
				
				GmSession session = (context instanceof AccessRequestContext<?>) ? ((AccessRequestContext<?>)context).getSession() : null;
				ResourceSpecification specification = specificationDetector.getSpecification(in, resourceMimeType, session);
				if (specification != null) {
					resource.setSpecification(specification);
					enriched = true; // if not yet, now it is enriched ;)
				}
				
			} finally{
				in.close();
			}
			
		}
		
		return enriched;
	}

	protected void stream(Resource resource, Supplier<InputStream> inputSupplier, Supplier<OutputStream> outputSupplier) throws IOException {
		// @formatter:off
		try (
			OutputStream out = outputSupplier != null ? outputSupplier.get() : NoOpOutputStream.instance;
			InputStream in = rootInputStream(resource, inputSupplier);
		) {
			IOTools.pump(in, out);
		}
		// @formatter:on
	}

	protected InputStream rootInputStream(Resource resource, Supplier<InputStream> inputSupplier) {

		if (inputSupplier == null) {
			return resource.openStream();
		}

		InputStream is = inputSupplier.get();

		return is == null ? resource.openStream() : is;

	}

	protected WriteOnReadInputStream enrichingInputStream(InputStream inputStream, OutputStream outputStream, MessageDigest md) {

		if (md != null) {
			inputStream = new DigestInputStream(inputStream, md);
		}

		return new WriteOnReadInputStream(inputStream, outputStream);

	}

	protected long consume(InputStream in) throws IOException {

		final byte[] buffer = new byte[consumerBufferSize];

		int count;
		long totalCount = 0;

		while ((count = in.read(buffer)) != -1) {
			totalCount += count;
		}

		return totalCount;
	}

	protected class StandardInternalResourceEnricherBuilder implements ResourceEnrichingStreamerBuilder {

		private Supplier<InputStream> inputSupplier;
		private Supplier<OutputStream> outputSupplier;
		private ServiceRequestContext context;
		private boolean onlyIfEnriched;
		
		@Override
		public ResourceEnrichingStreamerBuilder from(Supplier<InputStream> inputSupplier) {
			this.inputSupplier = inputSupplier;
			return this;
		}

		@Override
		public ResourceEnrichingStreamerBuilder to(Supplier<OutputStream> outputSupplier) {
			this.outputSupplier = outputSupplier;
			return this;
		}

		@Override
		public ResourceEnrichingStreamerBuilder context(ServiceRequestContext context) {
			this.context = context;
			return this;
		}

		@Override
		public ResourceEnrichingStreamerBuilder onlyIfEnriched() {
			this.onlyIfEnriched = true;
			return this;
		}

		@Override
		public boolean enriching(Resource resource) {
			Objects.requireNonNull(resource, "resource must not be null");
			try {
				return StandardResourceEnrichingStreamer.this.enrich(resource, inputSupplier, outputSupplier, context, onlyIfEnriched);
			} catch (IOException e) {
				throw unchecked("Failed to enrich " + resource, e);
			}
		}

		@Override
		public EnrichingResult enriching2(Resource resource) {
			throw new UnsupportedOperationException(
					"Method 'StandardResourceEnrichingStreamer.StandardInternalResourceEnricherBuilder.enriching2' is not supported!");
		}
	}

	protected static class NoOpOutputStream extends OutputStream {

		private static NoOpOutputStream instance = new NoOpOutputStream();

		@Override
		public void write(int b) throws IOException {
			// Ignore writes.
		}

		@Override
		public void write(byte b[]) throws IOException {
			// Ignore writes.
		}

		@Override
		public void write(byte b[], int off, int len) throws IOException {
			// Ignore writes.
		}

	}

}
