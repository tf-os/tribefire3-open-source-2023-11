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
package com.braintribe.model.processing.resource.streaming.access;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.SocketException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.marshaller.api.HasStringCodec;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.logging.Logger;
import com.braintribe.model.cache.CacheControl;
import com.braintribe.model.cache.CacheType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.generic.session.OutputStreamProvider;
import com.braintribe.model.generic.session.OutputStreamer;
import com.braintribe.model.generic.stream.StreamProviders;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.resource.ResourceCreateBuilder;
import com.braintribe.model.processing.session.api.resource.ResourceDeleteBuilder;
import com.braintribe.model.processing.session.api.resource.ResourceRetrieveBuilder;
import com.braintribe.model.processing.session.api.resource.ResourceUpdateBuilder;
import com.braintribe.model.processing.session.api.resource.ResourceUrlBuilder;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resource.specification.ResourceSpecification;
import com.braintribe.model.resourceapi.persistence.UpdateResource;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalResponse;
import com.braintribe.model.resourceapi.stream.StreamBinaryResponse;
import com.braintribe.model.resourceapi.stream.condition.FingerprintMismatch;
import com.braintribe.model.resourceapi.stream.condition.ModifiedSince;
import com.braintribe.model.resourceapi.stream.condition.StreamCondition;
import com.braintribe.model.resourceapi.stream.range.StreamRange;
import com.braintribe.transport.http.CountingHttpEntity;
import com.braintribe.transport.http.DefaultHttpClientProvider;
import com.braintribe.transport.http.HttpClientProvider;
import com.braintribe.transport.http.ResponseEntityInputStream;
import com.braintribe.transport.http.util.ErrorHelper;
import com.braintribe.transport.http.util.HttpTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;

public class RemoteResourceAccess extends AbstractResourceAccess {

	private static final Logger logger = Logger.getLogger(RemoteResourceAccess.class);

	private MarshallerRegistry marshallerRegistry;

	private StreamPipeFactory streamPipeFactory;
	private CloseableHttpClient cachedClient = null;
	private final ReentrantLock cachedClientLock = new ReentrantLock();
	protected HttpClientProvider httpClientProvider = new DefaultHttpClientProvider();
	private int authorizationMaxRetries;
	private Consumer<Throwable> authorizationFailureListener = authorizationFailure -> {
		if (log.isTraceEnabled()) {
			log.trace("No-op authorization failure listener received: " + authorizationFailure);
		}
	};

	private static final Logger log = Logger.getLogger(RemoteResourceAccess.class);

	RemoteResourceAccess(PersistenceGmSession gmSession) {
		super(gmSession);
	}

	@Configurable
	public void setStreamPipeFactory(StreamPipeFactory streamPipeFactory) {
		this.streamPipeFactory = streamPipeFactory;
	}

	@Configurable
	public void setHttpClientProvider(HttpClientProvider httpClientProvider) {
		this.httpClientProvider = httpClientProvider;
	}

	/**
	 * <p>
	 * Sets the maximum number of request retries upon "unauthorized request" responses from the server.
	 * 
	 * @param authorizationMaxRetries
	 *            The maximum number or retries
	 */
	@Configurable
	public void setAuthorizationMaxRetries(int authorizationMaxRetries) {
		this.authorizationMaxRetries = authorizationMaxRetries;
	}

	@Configurable
	public void setAuthorizationFailureListener(Consumer<Throwable> authorizationFailureListener) {
		Objects.requireNonNull(authorizationFailureListener, "authorizationFailureListener cannot be set to null");
		logger.debug(() -> "Receiving authorization failure listener: " + authorizationFailureListener);
		this.authorizationFailureListener = authorizationFailureListener;
	}

	@Configurable
	public void setMarshallerRegistry(MarshallerRegistry marshallerRegistry) {
		this.marshallerRegistry = marshallerRegistry;
	}

	@Override
	public ResourceCreateBuilder create() {

		return new ResourceCreateBuilder() {

			private String name;
			private String creator;
			private EntityType<? extends ResourceSource> sourceType;
			private String useCase;
			private String mimeType;
			private String md5;
			private ResourceSpecification specification;
			private Set<String> tags;

			@Override
			public ResourceCreateBuilder useCase(String useCase) {
				this.useCase = useCase;
				return this;
			}

			@Override
			public ResourceCreateBuilder sourceType(EntityType<? extends ResourceSource> sourceType) {
				this.sourceType = sourceType;
				return this;
			}

			@Override
			public ResourceCreateBuilder name(String resourceName) {
				this.name = resourceName;
				return this;
			}

			@Override
			public ResourceCreateBuilder creator(String creator) {
				this.creator = creator;
				return this;
			}

			@Override
			public ResourceCreateBuilder mimeType(String mimeType) {
				this.mimeType = mimeType;
				return this;
			}

			@Override
			public ResourceCreateBuilder md5(String md5) {
				this.md5 = md5;
				return this;
			}

			@Override
			public ResourceCreateBuilder tags(Set<String> tags) {
				this.tags = tags;
				return this;
			}

			@Override
			public ResourceCreateBuilder specification(ResourceSpecification specification) {
				this.specification = specification;
				return this;
			}

			@Override
			public Resource store(InputStream inputStream) {
				Objects.requireNonNull(inputStream, "inputStream must not be null");
				try {
					CloseableHttpResponse resp = post(inputStream, name, sourceType, useCase, mimeType, md5, specification, creator, tags);

					return create(resp);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}

			@Override
			public Resource store(InputStreamProvider inputStreamProvider) {
				Objects.requireNonNull(inputStreamProvider, "inputStreamProvider must not be null");
				try {
					CloseableHttpResponse resp = post(inputStreamProvider, name, sourceType, useCase, mimeType, md5, specification, creator, tags);

					return create(resp);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}

			@Override
			public Resource store(OutputStreamer streamer) {
				Objects.requireNonNull(streamer, "streamer must not be null");
				try {
					CloseableHttpResponse resp = post(streamer, name, sourceType, useCase, mimeType, md5, specification, creator, tags);

					return create(resp);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}

		};
	}

	@Override
	public ResourceRetrieveBuilder retrieve(Resource resource) {

		return new ResourceRetrieveBuilder() {

			private StreamCondition condition;
			private Consumer<BinaryRetrievalResponse> consumer;
			private StreamRange range;

			@Override
			public ResourceRetrieveBuilder condition(StreamCondition condition) {
				this.condition = condition;
				return this;
			}

			@Override
			public ResourceRetrieveBuilder onResponse(Consumer<BinaryRetrievalResponse> consumer) {
				this.consumer = consumer;
				return this;
			}

			@Override
			public void stream(OutputStream outputStream) {
				Objects.requireNonNull(outputStream, "outputStream must not be null");
				stream(StreamProviders.from(outputStream));
			}

			@Override
			public void stream(OutputStreamProvider outputStreamProvider) {
				Objects.requireNonNull(outputStreamProvider, "outputStreamProvider must not be null");
				try {
					InputStream is = stream();
					if (is != null) {
						try {
							IOTools.pump(is, outputStreamProvider.openOutputStream());
						} finally {
							IOTools.closeCloseable(is, "resource input stream", log);
						}
					}
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}

			@Override
			public InputStream stream() {
				try {
					return retrieve(resource, condition, range, consumer);
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

		return new ResourceDeleteBuilder() {

			private String useCase;

			@Override
			public ResourceDeleteBuilder useCase(String useCase) {
				this.useCase = useCase;
				return this;
			}

			@Override
			public void delete() {
				try {
					RemoteResourceAccess.this.delete(resource, useCase);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}

		};

	}

	@Override
	public InputStream openStream(Resource resource) throws IOException {
		return retrieve(resource, null, null, null);
	}

	@Override
	public void writeToStream(Resource resource, OutputStream outputStream) throws IOException {

		if (resource == null) {
			throw new IllegalArgumentException("resource cannot be null");
		}

		if (outputStream == null) {
			throw new IllegalArgumentException("outputStream cannot be null");
		}

		InputStream is = openStream(resource);
		try {
			IOTools.pump(is, outputStream);
		} finally {
			if (is != null)
				is.close();
		}
	}

	private InputStream retrieve(Resource resource, StreamCondition condition, StreamRange range, Consumer<BinaryRetrievalResponse> responseConsumer)
			throws IOException {

		if (resource == null) {
			throw new IllegalArgumentException("resource cannot be null");
		}

		if (resource.getId() == null) {
			throw new IOException("Cannot open stream to a resource without identity: " + resource);
		}

		CloseableHttpClient client = this.getClient();

		int authRetries = authorizationMaxRetries;

		CloseableHttpResponse response = null;

		try {
			while (true) {

				HttpUriRequest request = createGetRequest(resource, condition, range);
				response = client.execute(request);
				StatusLine status = response.getStatusLine();
				boolean notModified = false;
				int statusCode = status.getStatusCode();

				if (statusCode >= 300) {

					if (statusCode == 304) {
						notModified = true;
					} else {

						IOException requestFailure = ErrorHelper.processErrorResponse(request.getURI().toString(), request.getMethod(), response,
								null);

						if (statusCode == 401) {

							if (authRetries == 0) {
								if (log.isDebugEnabled() && authorizationMaxRetries > 0) {
									log.debug("Reached maximum of [ " + authorizationMaxRetries + " ] request retries upon authoriazation failures");
								}
								throw requestFailure;
							}

							authRetries--;

							try {
								logger.debug(() -> "Notifying authorization failure listener: " + authorizationFailureListener);
								authorizationFailureListener.accept(requestFailure);
							} catch (Exception e) {
								log.warn("Failed to notify listener [ " + authorizationFailureListener + " ] about authoriazation failure [ "
										+ asString(requestFailure) + " ] due to [ " + asString(e) + " ]", e);
							}

							continue;
						}

						throw requestFailure;
					}

				}

				if (responseConsumer != null) {
					responseConsumer.accept(createRetrievalResponse(statusCode, response));
				}

				if (notModified) {
					HttpTools.consumeResponse(response);
					return null; // Returning null as per ResourceAccess spec.
				} else {
					return new ResponseEntityInputStream(response);
				}

			}
		} catch (Exception e) {

			HttpTools.consumeResponse(response, e);
			throw asIOException(e);

		}

	}

	private Resource create(CloseableHttpResponse resp) throws IOException {
		InputStream respIn = new ResponseEntityInputStream(resp);

		try {
			Resource resource = asResource(getMarshaller(resp).unmarshall(respIn));
			if (resource == null) {
				return null;
			}
			PersistenceGmSession session = getPersistenceGmSession();
			if (session == null) {
				return resource;
			}
			return session.merge().suspendHistory(true).doFor(resource);
		} catch (MarshallException e) {
			throw asIOException(e);
		} finally {
			IOTools.closeQuietly(respIn);
			IOTools.closeCloseable(respIn, log);
		}
	}

	private void delete(Resource resource, String useCase) throws IOException {

		if (resource == null) {
			throw new IllegalArgumentException("resource cannot be null");
		}

		if (resource.getId() == null) {
			throw new IOException("Cannot delete a resource without identity: " + resource);
		}

		CloseableHttpClient client = this.getClient();

		int authRetries = authorizationMaxRetries;

		CloseableHttpResponse response = null;

		try {
			while (true) {

				HttpUriRequest request = createDeleteRequest(resource, useCase);
				response = client.execute(request);
				try {
					StatusLine status = response.getStatusLine();

					if (status.getStatusCode() >= 300) {

						IOException requestFailure = ErrorHelper.processErrorResponse(request.getURI().toString(), request.getMethod(), response,
								null);

						if (status.getStatusCode() == 401) {

							if (authRetries == 0) {
								if (log.isDebugEnabled() && authorizationMaxRetries > 0) {
									log.debug("Reached maximum of [ " + authorizationMaxRetries + " ] request retries upon authoriazation failures");
								}
								throw requestFailure;
							}

							authRetries--;

							try {
								authorizationFailureListener.accept(requestFailure);
							} catch (Exception e) {
								log.warn("Failed to notify listener [ " + authorizationFailureListener + " ] about authoriazation failure [ "
										+ asString(requestFailure) + " ] due to [ " + asString(e) + " ]", e);
							}

							continue;
						}

						throw requestFailure;

					}

					HttpTools.consumeResponse(request.getURI().toString(), response);
					return;
				} finally {
					IOTools.closeCloseable(response, log);
				}
			}
		} catch (Exception e) {

			HttpTools.consumeResponse(response, e);
			throw asIOException(e);

		}

	}

	/**
	 * Tries to extract a single {@link Resource} from the unmarshalled object representation
	 * 
	 * @param unmarshalledResponse
	 *            The response from the server
	 * @return The resource
	 */
	private static Resource asResource(Object unmarshalledResponse) {

		if (unmarshalledResponse == null) {
			if (log.isWarnEnabled())
				log.warn("unexpected null unmarshalled http response");
			return null;
		}

		if (unmarshalledResponse instanceof Resource)
			return (Resource) unmarshalledResponse;

		if (unmarshalledResponse instanceof Collection) {
			Collection<?> resources = (Collection<?>) unmarshalledResponse;
			if (resources.isEmpty()) {
				if (log.isWarnEnabled())
					log.warn("unexpected empty collection in unmarshalled http response");
				return null;
			}

			Iterator<?> it = resources.iterator();
			while (it.hasNext()) {
				Object elem = it.next();
				if (elem != null && elem instanceof Resource)
					return (Resource) elem;
			}
		}

		if (log.isWarnEnabled())
			log.warn("unable to convert to Resource. unexpected unmarshalled http response " + unmarshalledResponse);

		return null;
	}

	protected CloseableHttpClient getClient() {
		if (cachedClient == null) {
			cachedClientLock.lock();
			try {
				if (cachedClient == null) {
					long start = log.isTraceEnabled() ? System.currentTimeMillis() : 0;
					try {
						cachedClient = this.httpClientProvider.provideHttpClient();
						log.trace(() -> "Initialized the http client in " + (System.currentTimeMillis() - start) + " ms");
					} catch (Exception e) {
						throw new RuntimeException("Could not create HTTP client with SSL Context", e);
					}
				}
			} finally {
				cachedClientLock.unlock();
			}
		}

		return cachedClient;
	}

	private CloseableHttpResponse post(HttpEntity entity, String fileName, EntityType<? extends ResourceSource> sourceType, String useCase,
			String mimeType, String md5, ResourceSpecification specification, String creator, Set<String> tags) throws IOException {

		CloseableHttpClient client = getClient();

		int authRetries = authorizationMaxRetries;

		CloseableHttpResponse response = null;

		String url = null;
		Instant start = NanoClock.INSTANCE.instant();
		CountingHttpEntity countingEntity = new CountingHttpEntity(entity);

		try {
			while (true) {

				HttpUriRequest request = createPostRequest(countingEntity, fileName, sourceType, useCase, mimeType, md5, specification, creator,
						tags);
				url = request.getURI().toString();

				try {
					response = client.execute(request);
				} catch (SocketException se) {
					authRetries = evaluateAuthorizationFailureRetry(request, null, authRetries, null, se);
					continue;
				}

				StatusLine status = response.getStatusLine();

				if (status.getStatusCode() >= 300) {
					logger.debug(() -> "Received status code " + status.getStatusCode());
					authRetries = evaluateAuthorizationFailureRetry(request, response, authRetries, status, null);
					EntityUtils.consumeQuietly(response.getEntity());
					IOTools.closeCloseable(response, log);
					continue;
				}

				return response;

			}
		} catch (Exception e) {

			if (response != null) {
				try {
					EntityUtils.consume(response.getEntity());
				} catch (Exception ce) {
					if (log.isWarnEnabled()) {
						log.warn("Failed to consume entity [ " + response.getEntity() + " ] upon request failure [ " + asString(e) + " ] due to [ "
								+ asString(ce) + " ]", ce);
					}
				}
			}

			String duration = StringTools.prettyPrintDuration(start, true, ChronoUnit.NANOS);
			String context = "Error while trying to upload to URL " + url + " after " + duration + ". Transferred bytes: "
					+ countingEntity.getCount();

			throw asIOException(e, context);

		}
	}

	private CloseableHttpResponse post(OutputStreamer streamer, String fileName, EntityType<? extends ResourceSource> sourceType, String useCase,
			String mimeType, String md5, ResourceSpecification specification, String creator, Set<String> tags) throws IOException {

		return post(new OutputStreamConsumerHttpEntity(streamer, streamPipeFactory), //
				fileName, sourceType, useCase, mimeType, md5, specification, creator, tags);
	}

	private CloseableHttpResponse post(InputStreamProvider inputStreamProvider, String fileName, EntityType<? extends ResourceSource> sourceType,
			String useCase, String mimeType, String md5, ResourceSpecification specification, String creator, Set<String> tags) throws IOException {

		return post(new InputStreamProviderHttpEntity(inputStreamProvider), //
				fileName, sourceType, useCase, mimeType, md5, specification, creator, tags);
	}

	private CloseableHttpResponse post(InputStream in, String fileName, EntityType<? extends ResourceSource> sourceType, String useCase,
			String mimeType, String md5, ResourceSpecification specification, String creator, Set<String> tags) throws IOException {

		HttpEntity entity = new InputStreamEntity(in, -1);

		if (authorizationMaxRetries > 0) {
			// if authorization failures trigger retries, entity must be repeatable
			entity = new BufferedHttpEntity(entity);
		}

		return post(entity, fileName, sourceType, useCase, mimeType, md5, specification, creator, tags);
	}

	/**
	 * <p>
	 * Determines whether a authorization-failed request will be retried.
	 * 
	 * <p>
	 * This method either:
	 * 
	 * <ul>
	 * <li>Returns the number of retries left, signaling to the caller to retry the request;
	 * <li>Throws an Exception, signaling to the caller to NOT retry the request.
	 * </ul>
	 */
	private int evaluateAuthorizationFailureRetry(HttpUriRequest request, HttpResponse response, final int retriesLeft, StatusLine failedStatus,
			Exception failure) throws Exception {

		IOException requestFailure = null;

		if (failedStatus != null) {

			requestFailure = ErrorHelper.processErrorResponse(request.getURI().toString(), request.getMethod(), response, failure);

			if (failedStatus.getStatusCode() != 401) {
				logger.debug(() -> "Got a non-401 status code. Throwing the exception.");
				throw requestFailure;
			}

		}

		if (failure != null) {

			logger.debug(() -> "authorizationMaxRetries is configured to be " + authorizationMaxRetries);
			if (authorizationMaxRetries < 1) {
				throw failure;
			}

			requestFailure = ErrorHelper.processErrorResponse(request.getURI().toString(), request.getMethod(), response, failure);

		}

		logger.debug(() -> "Retries left: " + retriesLeft + ", authorizationMaxRetries: " + authorizationMaxRetries);
		if (retriesLeft == 0) {
			if (log.isDebugEnabled() && authorizationMaxRetries > 0) {
				log.debug("Reached maximum of [ " + authorizationMaxRetries + " ] request retries upon authoriazation failures");
			}
			throw requestFailure;
		}

		try {
			logger.debug(() -> "Informing authorizationFailureListener" + authorizationFailureListener);
			authorizationFailureListener.accept(requestFailure);
		} catch (Exception e) {
			log.warn("Failed to notify listener [ " + authorizationFailureListener + " ] about authoriazation failure [ " + asString(requestFailure)
					+ " ] due to [ " + asString(e) + " ]", e);
		}

		return (retriesLeft - 1);

	}

	private HttpUriRequest createGetRequest(Resource resource, StreamCondition condition, StreamRange range) throws IOException {

		ResourceUrlBuilder urlBuilder = url(resource);

		String url = getUri(urlBuilder, "GET");

		HttpGet get = new HttpGet(url);

		if (condition != null) {
			if (condition instanceof FingerprintMismatch) {
				FingerprintMismatch fingerprintMismatch = (FingerprintMismatch) condition;
				if (fingerprintMismatch.getFingerprint() != null) {
					get.setHeader("If-None-Match", fingerprintMismatch.getFingerprint());
				}
			} else if (condition instanceof ModifiedSince) {
				ModifiedSince modifiedSince = (ModifiedSince) condition;
				Date modifiedSinceDate = modifiedSince.getDate();
				if (modifiedSinceDate != null) {
					get.setHeader("If-Modified-Since", DateUtils.formatDate(modifiedSinceDate));
				}
			}
		}
		if (range != null) {
			Long start = range.getStart();
			Long end = range.getEnd();
			if (start != null) {
				String startString = "" + start;
				StringBuilder sb = new StringBuilder("bytes=");
				sb.append(startString);
				sb.append('-');
				if (end != null && end >= 0) {
					sb.append("" + end);
				}
				get.setHeader("Range", sb.toString());
			}
		}

		return get;

	}

	private HttpUriRequest createPostRequest(HttpEntity entity, String fileName, EntityType<? extends ResourceSource> sourceType, String useCase,
			String mimeType, String md5, ResourceSpecification specification, String creator, Set<String> tags) throws IOException {

		String encodedSpecification = null;
		String encodedTags = null;

		if (specification != null || (tags != null && !tags.isEmpty())) {
			Marshaller marshaller = marshallerRegistry.getMarshaller("application/json");
			if (marshaller instanceof HasStringCodec) {
				HasStringCodec stringCodec = (HasStringCodec) marshaller;
				if (specification != null) {
					encodedSpecification = stringCodec.getStringCodec().encode(specification);
				}
				if ((tags != null && !tags.isEmpty())) {
					encodedTags = stringCodec.getStringCodec().encode(tags);
				}
			}
		}

		ResourceUrlBuilder urlBuilder = url(null) //
				.fileName(fileName) //
				.useCase(useCase) //
				.mimeType(mimeType) //
				.md5(md5) //
				.creator(creator) //
				.specification(encodedSpecification) //
				.tags(encodedTags);

		if (sourceType != null) {
			urlBuilder.sourceType(sourceType.getTypeSignature());
		}

		String url = getUri(urlBuilder, "POST");

		HttpPost post = new HttpPost(url);
		post.setEntity(entity);
		post.setHeader("Pragma", "no-cache");
		post.setHeader("Cache-Control", "no-cache");

		return post;
	}

	private HttpUriRequest createDeleteRequest(Resource resource, String useCase) throws IOException {

		ResourceUrlBuilder urlBuilder = url(resource).useCase(useCase);

		String url = getUri(urlBuilder, "DELETE");

		return new HttpDelete(url);

	}

	private static String getUri(ResourceUrlBuilder urlBuilder, String targetHttpMethod) throws IOException {

		if (urlBuilder == null) {
			throw new IOException("Unable to assemble uri for http " + targetHttpMethod + " request. No ResourceUrlBuilder available.");
		}

		String url = urlBuilder.asString();

		if (log.isTraceEnabled()) {
			log.trace("Streaming [ " + targetHttpMethod + " ] URL assembled: [ " + url + " ]");
		}

		return url;
	}

	private StreamBinaryResponse createRetrievalResponse(int statusCode, HttpResponse response) {

		CacheControl cacheControl = CacheControl.T.create();

		Header etag = response.getFirstHeader("ETag");
		Header lastMod = response.getFirstHeader("Last-Modified");
		Header[] cacheControls = response.getHeaders("Cache-Control");

		if (etag != null) {
			cacheControl.setFingerprint(etag.getValue());
		}
		if (lastMod != null) {
			cacheControl.setLastModified(DateUtils.parseDate(lastMod.getValue()));
		}
		for (Header header : cacheControls) {
			setCacheControlDirective(cacheControl, header.getValue());
		}

		StreamBinaryResponse retrievalResponse = StreamBinaryResponse.T.create();

		readResponseRange(retrievalResponse, statusCode, response);
		retrievalResponse.setNotStreamed(statusCode == 304);
		retrievalResponse.setCacheControl(cacheControl);

		return retrievalResponse;

	}

	private void readResponseRange(StreamBinaryResponse retrievalResponse, int statusCode, HttpResponse response) {
		if (statusCode != 206) {
			retrievalResponse.setRanged(false);
			return;
		}

		// Partial content
		Header contentRangeHeader = response.getFirstHeader("Content-Range");
		if (contentRangeHeader != null) {
			String contentRange = contentRangeHeader.getValue();
			if (!StringTools.isBlank(contentRange)) {
				contentRange = contentRange.trim();

				int index = contentRange.indexOf(' ');
				if (index != -1) {
					String unit = contentRange.substring(0, index).trim().toLowerCase();
					if (unit.equals("bytes")) {
						String rangeSpec = contentRange.substring(index + 1).trim();
						index = rangeSpec.indexOf('/');

						if (index != -1) {

							retrievalResponse.setRanged(true);

							String rangePart = rangeSpec.substring(0, index).trim();
							String sizePart = rangeSpec.substring(index + 1).trim();

							if (!StringTools.isBlank(sizePart) && !sizePart.equals("*")) {
								retrievalResponse.setSize(Long.parseLong(sizePart));
							}

							if (!StringTools.isBlank(rangePart) && !rangePart.equals("*")) {
								index = rangePart.indexOf('-');

								retrievalResponse.setRangeStart(Long.parseLong(rangePart.substring(0, index).trim()));
								retrievalResponse.setRangeEnd(Long.parseLong(rangePart.substring(index + 1).trim()));
							}
						}
					}
				}
			}
		}
	}

	private void setCacheControlDirective(CacheControl cacheControl, String cacheControlHeader) {

		String[] cacheDirectives = cacheControlHeader.split(",");

		for (String directive : cacheDirectives) {

			directive = directive.toLowerCase().trim();

			if (cacheControl.getMaxAge() == null && directive.startsWith("max-age=")) {
				String age = directive.replace("max-age=", "");
				try {
					cacheControl.setMaxAge(Integer.parseInt(age));
				} catch (NumberFormatException e) {
					log.error("Invalid max-age directive", e);
				}
			} else if (directive.contains("must-revalidate")) {
				cacheControl.setMustRevalidate(true);
			} else {
				switch (directive) {
					case "no-cache":
						cacheControl.setType(CacheType.noCache);
						break;
					case "no-store":
						cacheControl.setType(CacheType.noStore);
						break;
					case "public":
						cacheControl.setType(CacheType.publicCache);
						break;
					case "private":
						cacheControl.setType(CacheType.privateCache);
						break;
				}
			}

		}

	}

	/**
	 * Returns a {@link Marshaller} capable of unmarshalling the content enclosed within the given
	 * {@link HttpResponse}.<br />
	 * <br />
	 * 
	 * There must be a {@link Marshaller} configured on marshallers mapping exactly to {@link HttpResponse}'s mime type, as
	 * no default {@link Marshaller} will be used in this case.
	 * 
	 * @param resp
	 *            The response fropm the server
	 * @return The Marshaller that is able to unmarshal the response
	 */
	private Marshaller getMarshaller(HttpResponse resp) throws IOException {

		String mimeType = getMimeType(resp);

		Marshaller marshaller = marshallerRegistry.getMarshaller(mimeType);

		if (marshaller == null)
			throw new IOException("marshallers registry contains no marshaller for mime type [ " + mimeType + " ]");

		return marshaller;
	}

	/**
	 * Returns the MIME type part from HttpResponse's ContentType. Character set part must not be returned.
	 * 
	 * @param resp
	 *            The response from the server
	 * @return The mime type of the response body
	 */
	private static String getMimeType(HttpResponse resp) {
		return ContentType.getOrDefault(resp.getEntity()).getMimeType();
	}

	private static String asString(Throwable t) {
		return t.getClass().getName() + (t.getMessage() != null ? ": " + t.getMessage() : "");
	}

	@Override
	public ResourceUpdateBuilder update(Resource resourceToUpdate) {
		return new ResourceUpdateBuilder() {

			protected String useCase;
			protected EntityType<? extends ResourceSource> sourceType;
			protected boolean deleteOldResourceSource = true;

			private final Resource resource = resourceToUpdate.clone(new StandardCloningContext());

			@Override
			public ResourceUpdateBuilder deleteOldResourceSource(boolean keep) {
				deleteOldResourceSource = keep;
				return this;
			}

			@Override
			public ResourceUpdateBuilder useCase(String useCase) {
				this.useCase = useCase;
				return this;
			}

			@Override
			public ResourceUpdateBuilder sourceType(EntityType<? extends ResourceSource> sourceType) {
				this.sourceType = sourceType;
				return this;
			}

			@Override
			public ResourceUpdateBuilder name(String resourceName) {
				resource.setName(resourceName);
				return this;
			}
			@Override
			public ResourceUpdateBuilder creator(String creator) {
				resource.setCreator(creator);
				return this;
			}
			@Override
			public ResourceUpdateBuilder mimeType(String mimeType) {
				resource.setMimeType(mimeType);
				return this;
			}
			@Override
			public ResourceUpdateBuilder md5(String md5) {
				resource.setMd5(md5);
				return this;
			}
			@Override
			public ResourceUpdateBuilder tags(Set<String> tags) {
				resource.setTags(tags);
				return this;
			}
			@Override
			public ResourceUpdateBuilder specification(ResourceSpecification specification) {
				resource.setSpecification(specification);
				return this;
			}
			@Override
			public Resource store(InputStream inputStream) {
				Objects.requireNonNull(inputStream, "inputStream must not be null");
				StreamPipe fileBackedPipe = streamPipeFactory.newPipe(getClass().getSimpleName() + "-" + accessId);
				fileBackedPipe.feedFrom(inputStream);
				resource.assignTransientSource(fileBackedPipe::openInputStream);
				return _update();
			}
			@Override
			public Resource store(InputStreamProvider inputStreamProvider) {
				Objects.requireNonNull(inputStreamProvider, "inputStreamProvider must not be null");
				resource.assignTransientSource(inputStreamProvider);
				return _update();
			}
			@Override
			public Resource store(OutputStreamer streamer) {
				Objects.requireNonNull(streamer, "streamer must not be null");
				resource.assignTransientSource(StreamProviders.from(streamer, streamPipeFactory));
				return _update();
			}

			private Resource _update() {
				UpdateResource updateResource = UpdateResource.T.create();
				updateResource.setResource(resource);
				updateResource.setDeleteOldResourceSource(deleteOldResourceSource);
				updateResource.setSourceType(sourceType == null ? null : sourceType.getTypeSignature());
				updateResource.setUseCase(useCase);

				return updateResource.eval(gmSession).get().getResource();
			}
		};
	}

}

class OutputStreamConsumerHttpEntity extends AbstractHttpEntity {

	private final OutputStreamer streamer;
	private StreamPipe pipe;
	private final StreamPipeFactory streamPipeFactory;
	private ReentrantLock pipeLock = new ReentrantLock();

	public OutputStreamConsumerHttpEntity(OutputStreamer streamer, StreamPipeFactory streamPipeFactory) {
		this.streamer = streamer;
		this.streamPipeFactory = streamPipeFactory;
	}

	private StreamPipe getPipe() throws IOException {
		if (pipe == null) {
			pipeLock.lock();
			try {
				if (pipe == null) {
					pipe = streamPipeFactory.newPipe("call-resource-input-pipe");

					try (OutputStream out = pipe.openOutputStream()) {
						streamer.writeTo(out);
					}
				}
			} finally {
				pipeLock.unlock();
			}
		}

		return pipe;
	}

	@Override
	public boolean isRepeatable() {
		return true;
	}

	@Override
	public long getContentLength() {
		return -1;
	}

	@Override
	public InputStream getContent() throws IOException, UnsupportedOperationException {
		return getPipe().openInputStream();
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		streamer.writeTo(outstream);
	}

	@Override
	public boolean isStreaming() {
		return false;
	}

}

class InputStreamProviderHttpEntity extends AbstractHttpEntity {

	private final InputStreamProvider inputStreamProvider;

	public InputStreamProviderHttpEntity(InputStreamProvider inputStreamProvider) {
		this.inputStreamProvider = inputStreamProvider;
	}

	@Override
	public boolean isRepeatable() {
		return true;
	}

	@Override
	public long getContentLength() {
		return -1;
	}

	@Override
	public InputStream getContent() throws IOException, UnsupportedOperationException {
		return inputStreamProvider.openInputStream();
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		try (InputStream in = getContent()) {
			IOTools.transferBytes(in, outstream);
		}
	}

	@Override
	public boolean isStreaming() {
		return false;
	}

}
