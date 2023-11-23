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
package com.braintribe.model.processing.resource.server.test.commons;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.braintribe.common.lcd.NotImplementedException;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.generic.session.OutputStreamProvider;
import com.braintribe.model.generic.session.OutputStreamer;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.resource.ResourceAccess;
import com.braintribe.model.processing.session.api.resource.ResourceAccessFactory;
import com.braintribe.model.processing.session.api.resource.ResourceCreateBuilder;
import com.braintribe.model.processing.session.api.resource.ResourceDeleteBuilder;
import com.braintribe.model.processing.session.api.resource.ResourceRetrieveBuilder;
import com.braintribe.model.processing.session.api.resource.ResourceUpdateBuilder;
import com.braintribe.model.processing.session.api.resource.ResourceUrlBuilder;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resource.specification.ResourceSpecification;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalResponse;
import com.braintribe.model.resourceapi.stream.condition.StreamCondition;
import com.braintribe.model.resourceapi.stream.range.StreamRange;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.RangeInputStream;

// TODO: use BasicResourceAccess instead.
public class TestResourceAccessFactory implements ResourceAccessFactory<PersistenceGmSession> {

	private final Map<String, TestResourceData> resourceDataMap = new ConcurrentHashMap<>();

	@Override
	public ResourceAccess newInstance(PersistenceGmSession session) {
		return new TestResourceAccess(session);
	}

	// TODO: use BasicResourceAccess instead.
	private class TestResourceAccess implements ResourceAccess {

		private final PersistenceGmSession session;

		protected TestResourceAccess(PersistenceGmSession session) {
			this.session = session;
		}

		@Override
		public ResourceCreateBuilder create() {

			return new ResourceCreateBuilder() {

				private String name;

				@Override
				public ResourceCreateBuilder useCase(String useCase) {
					// ignored in tests so far.
					return this;
				}

				@Override
				public ResourceCreateBuilder sourceType(EntityType<? extends ResourceSource> sourceType) {
					// ignored in tests so far.
					return this;
				}

				@Override
				public ResourceCreateBuilder name(String resourceName) {
					this.name = resourceName;
					return this;
				}

				@Override
				public Resource store(InputStream inputStream) {
					try {
						return create(inputStream, name);
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}

				@Override
				public Resource store(InputStreamProvider inputStreamProvider) {
					try {
						return store(inputStreamProvider.openInputStream());
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}
				
				@Override
				public Resource store(OutputStreamer streamer) {
					throw new UnsupportedOperationException();
				}

				@Override
				public ResourceCreateBuilder mimeType(String mimeType) {
					return this;
				}

				@Override
				public ResourceCreateBuilder md5(String md5) {
					return this;
				}

				@Override
				public ResourceCreateBuilder specification(ResourceSpecification specification) {
					return this;
				}

				@Override
				public ResourceCreateBuilder tags(Set<String> tags) {
					// ignored in tests so far.
					return this;
				}

				@Override
				public ResourceCreateBuilder creator(String creator) {
					return this;
				}

			};
		}

		@Override
		public ResourceRetrieveBuilder retrieve(Resource resource) {

			return new ResourceRetrieveBuilder() {

				private StreamRange range;

				@Override
				public ResourceRetrieveBuilder condition(StreamCondition condition) {
					// ignored in tests so far.
					return this;
				}

				@Override
				public ResourceRetrieveBuilder onResponse(Consumer<BinaryRetrievalResponse> consumer) {
					// ignored in tests so far.
					return this;
				}

				@Override
				public void stream(OutputStream outputStream) {
					try {
						writeToStream(resource, outputStream, range);
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}

				@Override
				public void stream(OutputStreamProvider outputStreamProvider) {
					try {
						stream(outputStreamProvider.openOutputStream());
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}

				@Override
				public InputStream stream() {
					try {
						return openStream(resource);
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}

				@Override
				public ResourceRetrieveBuilder range(StreamRange range) {
					this.range = range;
					return this;
				}

			};

		}

		@Override
		public ResourceDeleteBuilder delete(Resource resource) {
			throw new UnsupportedOperationException(getClass().getName() + " does not support delete(Resource)");
		}

		@Override
		public ResourceUrlBuilder url(Resource resource) {
			throw new UnsupportedOperationException(getClass().getName() + " does not support url(Resource)");
		}

		@Override
		public InputStream openStream(Resource resource) throws IOException {

			if (resource == null || resource.getId() == null) {
				throw new IOException("Invalid resource: " + resource);
			}

			TestResourceData data = resourceDataMap.get(resource.getId());

			if (data == null) {
				throw new IOException("Resource data not found: " + resource);
			}

			return data.openInputStream();
		}

		@Override
		public void writeToStream(Resource resource, OutputStream outputStream) throws IOException {
			writeToStream(resource, outputStream, null);
		}

		protected void writeToStream(Resource resource, OutputStream outputStream, StreamRange range) throws IOException {
			InputStream in = openStream(resource);
			if (range != null) {
				in = new RangeInputStream(in, range.getStart(), range.getEnd());
			}
			IOTools.pump(in, outputStream);
		}

		private Resource create(InputStream in, String resourceName) throws IOException {

			TestResourceData data = TestResourceDataTools.createResourceData(in);

			return create(data, resourceName);

		}

		protected Resource create(final TestResourceData data, final String resourceName) {

			Resource resource = session.create(Resource.T);
			resource.setId(UUID.randomUUID().toString());
			resource.setMimeType("application/octet");
			resource.setName(resourceName);
			resource.setMd5(data.md5);
			
			session.commit();

			resourceDataMap.put(resource.getId(), data);

			return resource;

		}

		@Override
		public ResourceUpdateBuilder update(Resource resource) {
			throw new NotImplementedException("This test mock of a ResourceAccess dos not support update operations");
		}

	}

}
