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

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
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
public class StandardResourceEnrichingStreamer2 implements ResourceEnrichingStreamer, StandardResourceProcessor {

	private MimeTypeDetector detector = PlatformMimeTypeDetector.instance;
	private ResourceSpecificationDetector<?> specificationDetector = null;
	private StreamPipeFactory streamPipeFactory;
	private int consumerBufferSize = 8096;
	private String digestAlgorithm = "MD5";

	@Required
	public void setStreamPipeFactory(StreamPipeFactory streamPipeFactory) {
		this.streamPipeFactory = requireNonNull(streamPipeFactory, "streamPipeFactory cannot be null");
	}

	@Configurable
	public void setMimeTypeDetector(MimeTypeDetector mimeTypeDetector) {
		requireNonNull(mimeTypeDetector, "mimeTypeDetector cannot be set to null");
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

	@Override
	public ResourceEnrichingStreamerBuilder stream() {
		return new StandardInternalResourceEnricherBuilder();
	}

	private class StandardInternalResourceEnricherBuilder implements ResourceEnrichingStreamerBuilder {

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
				return new Enriching(resource, false).go().enriched();
			} catch (IOException e) {
				throw unchecked("Failed to enrich " + resource, e);
			}
		}

		@Override
		public EnrichingResult enriching2(Resource resource) {
			Objects.requireNonNull(resource, "resource must not be null");
			try {
				return new Enriching(resource, true).go();
			} catch (IOException e) {
				throw unchecked("Failed to enrich " + resource, e);
			}
		}

		private class Enriching {

			private final Resource resource;
			private final boolean needsResultIss;

			private final boolean enrichId;
			private final boolean enrichMd5;
			private final boolean enrichMimeType;
			private final boolean enrichSize;
			private final boolean enrichCreated;
			private final boolean enrichCreator;
			private final boolean enrichSpecification;

			private Supplier<InputStream> piss; // pipeInputStreamSupplier
			private Supplier<OutputStream> oss;

			private final SimpleEnrichingResult result;

			public Enriching(Resource resource, boolean needsResultIss) {
				this.resource = resource;
				this.needsResultIss = needsResultIss;

				enrichId = unset(resource.getId());
				enrichMd5 = unset(resource.getMd5());
				enrichMimeType = unset(resource.getMimeType());
				enrichSize = resource.getFileSize() == null;
				enrichCreated = resource.getCreated() == null;
				enrichCreator = unset(resource.getCreator());
				enrichSpecification = resource.getSpecification() == null && specificationDetector != null;

				result = new SimpleEnrichingResult();
				result.enriched = false;
				result.iss = () -> rootInputStream(streamSupplierToReadFrom());
			}

			private Supplier<InputStream> streamSupplierToReadFrom() {
				return piss != null ? piss : inputSupplier;
			}

			public EnrichingResult go() throws IOException {
				requireNonNull(streamPipeFactory, "StreamPipeFactory was not configured on this enricher!");

				handlePropertiesUnrelatedToPayload();

				if (needsPayloadBasedEnriching()) {
					consumeStreamIfDesired();
					return result;
				}

				// enrich while also reading the actual stream

				MessageDigest md = null;

				if (enrichMd5)
					md = createMessageDigest(digestAlgorithm);

				resolvePissAndOss(needsResultIss || enrichSpecification);

				// Here we read the actual resource and store a copy if needed
				try ( //
						OutputStream out = oss.get(); //
						InputStream rootIn = rootInputStream(inputSupplier); //
						WriteOnReadInputStream in = enrichingInputStream(rootIn, out, md); //
				) {
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
						result.enriched = true;
					}

					if (md != null) {
						resource.setMd5(digest(md));
						result.enriched = true;
					}
				}

				String resourceMimeType = resource.getMimeType();
				if (enrichSpecification && resourceMimeType != null) {
					InputStream in = rootInputStream(piss);
					try {

						ResourceSpecification specification = specificationDetector.getSpecification(in, resourceMimeType, gmSession());
						if (specification != null) {
							resource.setSpecification(specification);
							result.enriched = true; // if not yet, now it is enriched ;)
						}

					} finally {
						in.close();
					}
				}

				return result;
			}

			private GmSession gmSession() {
				return (context instanceof AccessRequestContext<?>) ? ((AccessRequestContext<?>) context).getSession() : null;
			}

			private void handlePropertiesUnrelatedToPayload() {
				if (enrichId) {
					if (resource.getGlobalId() != null)
						resource.setId(resource.getGlobalId());
					else
						resource.setId(UUID.randomUUID().toString());

					result.enriched = true;
				}

				if (enrichCreated) {
					resource.setCreated(new Date());
					result.enriched = true;
				}

				if (enrichCreator && context != null) {
					String creator = context.getRequestorUserName();
					if (!unset(creator)) {
						resource.setCreator(creator);
						result.enriched = true;
					}
				}
			}

			private boolean needsPayloadBasedEnriching() {
				return !(enrichMd5 || enrichMimeType || enrichSize || enrichSpecification);
			}

			private void consumeStreamIfDesired() throws IOException {
				if (!onlyIfEnriched) {
					resolvePissAndOss(needsResultIss);
					stream(inputSupplier, oss);
				}
			}

			private void resolvePissAndOss(boolean needsMultipleReads) {
				if (!needsMultipleReads) {
					oss = outputSupplier;

				} else {
					List<OutputStream> outputStreams = newList();

					if (outputSupplier != null)
						outputStreams.add(outputSupplier.get());

					// Buffer the input stream in a pipe when the specification in going to be enriched so it doesn't have to be opened twice
					StreamPipe pipe = streamPipeFactory.newPipe(StandardResourceEnrichingStreamer2.class.getSimpleName());
					outputStreams.add(pipe.openOutputStream());

					oss = () -> new MultiplierOutputStream(outputStreams);
					piss = () -> {
						try {
							return pipe.openInputStream();
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					};
				}
			}

			private void stream(Supplier<InputStream> inputSupplier, Supplier<OutputStream> outputSupplier) throws IOException {
				try ( //
						OutputStream out = outputSupplier != null ? outputSupplier.get() : NoOpOutputStream.instance; //
						InputStream in = rootInputStream(inputSupplier); //
				) {
					IOTools.pump(in, out);
				}
			}

			private InputStream rootInputStream(Supplier<InputStream> inputSupplier) {
				if (inputSupplier == null)
					return resource.openStream();

				InputStream is = inputSupplier.get();
				return is == null ? resource.openStream() : is;
			}

		} // class Enriching

		private WriteOnReadInputStream enrichingInputStream(InputStream inputStream, OutputStream outputStream, MessageDigest md) {
			if (md != null)
				inputStream = new DigestInputStream(inputStream, md);

			return new WriteOnReadInputStream(inputStream, outputStream);
		}

		private long consume(InputStream in) throws IOException {
			final byte[] buffer = new byte[consumerBufferSize];

			int count;
			long totalCount = 0;

			while ((count = in.read(buffer)) != -1) {
				totalCount += count;
			}

			return totalCount;
		}

	}

	private static class NoOpOutputStream extends OutputStream {

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

	private static class SimpleEnrichingResult implements EnrichingResult {
		public boolean enriched;
		public Supplier<InputStream> iss;

		private InputStream is;

		@Override
		public boolean enriched() {
			return enriched;
		}

		@Override
		public InputStream inputStream() {
			if (is == null)
				is = iss.get();
			return is;
		}

	}

}
