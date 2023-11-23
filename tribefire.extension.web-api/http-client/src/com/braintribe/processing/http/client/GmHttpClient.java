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
package com.braintribe.processing.http.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Collectors;

import org.apache.commons.io.input.TeeInputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicLineFormatter;
import org.apache.http.util.EntityUtils;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.marshaller.api.CharacterMarshaller;
import com.braintribe.codec.marshaller.api.DateDefaultZoneOption;
import com.braintribe.codec.marshaller.api.DateFormatOption;
import com.braintribe.codec.marshaller.api.DateLocaleOption;
import com.braintribe.codec.marshaller.api.DecodingLenience;
import com.braintribe.codec.marshaller.api.EntityRecurrenceDepth;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.IdentityManagementMode;
import com.braintribe.codec.marshaller.api.IdentityManagementModeOption;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.api.PropertyDeserializationTranslation;
import com.braintribe.codec.marshaller.api.PropertySerializationTranslation;
import com.braintribe.codec.marshaller.api.PropertyTypeInferenceOverride;
import com.braintribe.codec.marshaller.api.TypeExplicitness;
import com.braintribe.codec.marshaller.api.TypeExplicitnessOption;
import com.braintribe.codec.marshaller.common.BasicConfigurableMarshallerRegistry;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.codec.marshaller.url.UrlEncodingMarshaller;
import com.braintribe.exception.HttpException;
import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.transport.http.DefaultHttpClientProvider;
import com.braintribe.transport.http.HttpClientProvider;
import com.braintribe.transport.http.ResponseEntityInputStream;
import com.braintribe.transport.http.util.HttpTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.StopWatch;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;

@SuppressWarnings("deprecation")
public class GmHttpClient implements HttpClient {

	private static final Logger logger = Logger.getLogger(GmHttpClient.class);

	private String baseUrl;
	protected HttpClientProvider httpClientProvider = new DefaultHttpClientProvider();
	private RequestConfig httpRequestConfig = RequestConfig.DEFAULT;
	private MarshallerRegistry marshallerRegistry = defaultMarshallerRegistry();
	private Credentials credentials = null;
	private final BaseType baseType = BaseType.INSTANCE;
	private LogLevel requestLogging;
	private LogLevel responseLogging;
	private Evaluator<ServiceRequest> evaluator;
	private StreamPipeFactory streamPipeFactory;

	// ***************************************************************************************************
	// Setter
	// ***************************************************************************************************

	/**
	 * Default constructor (usually for IOC usage).
	 */
	public GmHttpClient() {
	}

	/**
	 * Convenience constructor (usually for standalone usage).
	 */
	public GmHttpClient(String baseUrl) {
		setBaseUrl(baseUrl);
	}

	// ***************************************************************************************************
	// Setter
	// ***************************************************************************************************

	@Configurable
	public void setBaseUrl(String baseUrl) {
		if (!StringTools.isBlank(baseUrl)) {
			assertValidUri(baseUrl);
			this.baseUrl = baseUrl;
		}
	}

	@Configurable
	public void setHttpClientProvider(HttpClientProvider httpClientProvider) {
		this.httpClientProvider = httpClientProvider;
	}

	@Configurable
	public void setHttpRequestConfig(RequestConfig httpRequestConfig) {
		this.httpRequestConfig = httpRequestConfig;
	}

	@Configurable
	public void setMarshallerRegistry(MarshallerRegistry marshallerRegistry) {
		this.marshallerRegistry = marshallerRegistry;
	}

	@Configurable
	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}

	@Configurable
	public void setRequestLogging(LogLevel requestLogging) {
		this.requestLogging = requestLogging;
	}

	@Configurable
	public void setResponseLogging(LogLevel responseLogging) {
		this.responseLogging = responseLogging;
	}

	@Mandatory
	@Configurable
	public void setEvaluator(Evaluator<ServiceRequest> evaluator) {
		this.evaluator = evaluator;
	}

	@Mandatory
	@Configurable
	public void setStreamPipeFactory(StreamPipeFactory streamPipeFactory) {
		this.streamPipeFactory = streamPipeFactory;
	}

	// ***************************************************************************************************
	// RestClient
	// ***************************************************************************************************

	@Override
	public HttpResponse sendRequest(HttpRequestContext context) {
		StopWatch stopWatch = new StopWatch();

		RequestBuilder requestBuilder = requestBuilder(context);
		HttpUriRequest httpRequest = requestBuilder.build();

		URI requestUri = httpRequest.getURI();
		CloseableHttpClient httpClient = getClient();
		CloseableHttpResponse httpResponse = null;
		try {
			logger.trace(() -> "Sending rest request to: " + requestUri.toString());

			addBasicAuthorizationHeader(httpRequest);

			requestLogging(context, requestBuilder, requestUri);

			httpResponse = httpClient.execute(httpRequest);

			if (responseLogging != null) {
				logger.log(responseLogging, "Received response from " + requestUri.toString() + "\n" + summarizeResponse(httpResponse));
			}

			HttpResponseBuilder responseBuilder = HttpResponseBuilder.instance(context);

			Object responsePayload = null;
			GenericModelType responseType = null;
			int code = httpResponse.getStatusLine().getStatusCode();
			if (code == HttpURLConnection.HTTP_NO_CONTENT) {
				responseBuilder.payload(Neutral.NEUTRAL);
			} else {
				StreamPipe pipe = streamPipeFactory.newPipe(requestUri.toString());
				Marshaller responseMarshaller = null;

				try (OutputStream pipeOut = pipe.acquireOutputStream();
						InputStream in = new TeeInputStream(new ResponseEntityInputStream(httpResponse), pipeOut)) {

					responseType = context.responseTypeForCode(code);
					responseMarshaller = getMarshaller(context.produces());

					if (responseType != null && responseMarshaller != null) {

						HttpDateFormatting dateFormatting = context.dateFormatting();

						GmDeserializationOptions options = //
								GmDeserializationOptions //
										.deriveDefaults() //
										.setInferredRootType(responseType) //
										.setDecodingLenience(new DecodingLenience(true)) //
										.set(PropertyTypeInferenceOverride.class, context::propertyTypeInference) //
										.set(PropertyDeserializationTranslation.class, context::responseBodyParameterTranslation) //
										.set(DateFormatOption.class, dateFormatting != null ? dateFormatting.getDateFormat() : null) //
										.set(DateDefaultZoneOption.class, dateFormatting != null ? dateFormatting.getDefaultZone() : null) //
										.set(DateLocaleOption.class, dateFormatting != null ? dateFormatting.getDefaultLocale() : null) //
										.build();

						responsePayload = responseMarshaller.unmarshall(in, options);
						if (responsePayload == null && responseType.isEntity()) {
							logger.debug("Got not payload from the client. Creating an empty " + responseType);
							responsePayload = ((EntityType<?>) responseType).create();
						}

					} else {
						responsePayload = getResponseAsString(in);
						responseBuilder.isGeneric();
					}

					if (responsePayload == null && responseType == Neutral.T) {
						responseBuilder.payload(Neutral.NEUTRAL);
					} else {
						if (!context.wasSuccessful(code) && context.throwExceptionOnErrorCode(code)) {
							HttpException ex = new HttpException(code, "Rest request responded with failure code: " + code);
							ex.withPayload(responsePayload);
							throw ex;
						}

						responseBuilder.payload(responsePayload);
						for (Header responseHeader : httpResponse.getAllHeaders()) {
							responseBuilder.addHeaderParameter(responseHeader.getName(), responseHeader.getValue());
						}
					}

				} finally {
					final String content;
					if (responseMarshaller instanceof CharacterMarshaller) {
						try (InputStream pipeIn = pipe.openInputStream()) {
							content = com.braintribe.utils.IOTools.slurp(pipeIn, "UTF-8");
						}
					} else {
						content = "<binary>";
					}
					if (responseLogging != null) {
						logger.log(responseLogging, () -> "Received body from " + requestUri + ": " + content);
					}
				}
			}

			HttpResponse response = responseBuilder.build();

			long elapsedTime = stopWatch.getElapsedTime();
			responseLogging(code, responsePayload, responseType, context, elapsedTime);

			return response;
		} catch (HttpException e) {
			throw e;
		} catch (Exception e) {
			throw new HttpClientException("Error while sending rest request to " + requestUri.toString() + ": " + e.getMessage(), e);
		} finally {
			HttpTools.consumeResponse(requestUri.toString(), httpResponse);
			HttpTools.closeResponseQuietly(requestUri.toString(), httpResponse);
			logger.trace(() -> "Finished rest request execution to: " + requestUri.toString());
		}
	}

	// ***************************************************************************************************
	// Helper
	// ***************************************************************************************************

	private void addBasicAuthorizationHeader(HttpUriRequest httpRequest) {
		if (this.credentials != null) {
			String encoded = Base64.getEncoder()
					.encodeToString((credentials.getUserPrincipal().getName() + ":" + credentials.getPassword()).getBytes(StandardCharsets.UTF_8));
			httpRequest.setHeader("Authorization", "Basic " + encoded);
		}
	}

	private static String summarizeResponse(org.apache.http.HttpResponse response) {
		if (response == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		StatusLine statusLine = response.getStatusLine();
		if (statusLine != null) {
			String statusLineString = BasicLineFormatter.INSTANCE.formatStatusLine(null, statusLine).toString();
			sb.append(statusLineString);
			sb.append('\n');
		}

		Header[] headers = response.getAllHeaders();
		if (headers != null) {
			for (Header h : headers) {
				if (h != null) {
					String headerString = BasicLineFormatter.INSTANCE.formatHeader(null, h).toString();
					sb.append(headerString);
					sb.append('\n');
				}
			}
		}
		if (sb.length() > 0) {
			sb.append('\n');
		}
		return sb.toString();
	}

	private String getResponseAsString(InputStream in) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		IOTools.pump(in, os);
		return os.toString();
	}

	private RequestBuilder requestBuilder(HttpRequestContext context) {
		//@formatter:off
		RequestBuilder requestBuilder = 
				RequestBuilder
				.create(context.requestMethod())
				.setConfig(this.httpRequestConfig)
				.setUri(buildUri(context))
				.addHeader(HttpConstants.HTTP_HEADER_CONTENTTYPE, context.consumes())
				.addHeader(HttpConstants.HTTP_HEADER_ACCEPT, context.produces());
		//@formatter:on

		context.headerParameters().forEach(p -> requestBuilder.addHeader(p.getName(), p.getValue()));

		Object payload = context.payload();
		if (payload != null) {

			// TODO: Support multiple payload objects with MultiPart

			final HttpEntity body;
			if (payload instanceof Resource && context.streamResourceContent()) {

				Resource resource = (Resource) payload;
				body = new ResourceHttpEntity(resource, evaluator);

				String mimeType = resource.getMimeType();
				if (!StringTools.isBlank(mimeType)) {
					requestBuilder.addHeader(HttpConstants.HTTP_HEADER_CONTENTTYPE, mimeType);
				}

			} else {

				ByteArrayOutputStream os = new ByteArrayOutputStream();
				Marshaller bodyMarshaller = getMarshaller(context.consumes());
				GmSerializationOptions options = getMarshallingOptions(context, payload);
				bodyMarshaller.marshall(os, payload, options);
				byte[] byteArray = os.toByteArray();

				logger.trace(() -> "Request body: " + new String(byteArray));

				body = new ByteArrayEntity(byteArray);

			}
			requestBuilder.setEntity(body);

		} else if (HttpConstants.HTTP_METHOD_POST.equals(context.requestMethod()) || HttpConstants.HTTP_METHOD_PUT.equals(context.requestMethod())) {
			requestBuilder.addHeader("Content-Length", String.valueOf(0));
		}

		return requestBuilder;
	}

	private GmSerializationOptions getMarshallingOptions(HttpRequestContext context, Object payload) {
		GmSerializationOptions marshallingOptions = context.payloadMarshallingOptions();

		if (marshallingOptions == null) {
			GenericModelType rootType = context.payloadType();
			if (rootType == null) {
				baseType.getActualType(payload);
			}

			marshallingOptions = GmSerializationOptions.deriveDefaults() //
					.set(EntityRecurrenceDepth.class, -1) //
					.set(TypeExplicitnessOption.class, TypeExplicitness.never) //
					.set(IdentityManagementModeOption.class, IdentityManagementMode.off) //
					.set(PropertySerializationTranslation.class, context::requestBodyParameterTranslation) //
					.writeAbsenceInformation(false) //
					.writeEmptyProperties(false) //
					.inferredRootType(rootType) //
					.build(); //
		}

		return marshallingOptions;
	}

	private URI buildUri(HttpRequestContext context) {
		String requestUri = buildBaseUri(context);
		try {
			URIBuilder uriBuilder = new URIBuilder(requestUri);

			context.queryParameters().forEach(p -> uriBuilder.setParameter(p.getName(), p.getValue()));

			return uriBuilder.build();
		} catch (URISyntaxException e) {
			throw new HttpClientException("Incorrect URL syntax for rest request to: " + requestUri, e);
		}
	}

	private String buildBaseUri(HttpRequestContext restContext) {
		String baseUri = this.baseUrl;
		String requestPath = restContext.requestPath();
		if (!StringTools.isBlank(requestPath)) {
			baseUri = ensureTrailingSlash(this.baseUrl) + removeStartingSlash(restContext.requestPath());
			if (StringTools.isBlank(this.baseUrl)) {
				baseUri = requestPath;
			} else {
				baseUri = ensureTrailingSlash(this.baseUrl) + removeStartingSlash(restContext.requestPath());
			}
		}
		if (StringTools.isBlank(baseUri)) {
			throw new IllegalStateException("Neither the base URL nor a request path is set for context: " + restContext);
		}
		return baseUri;
	}

	private void assertValidUri(String uri) {
		if (!validateUri(uri)) {
			throw new HttpClientException("Malfromed URL provided: " + uri);
		}
	}
	private boolean validateUri(String uri) {
		try {
			URI.create(uri);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private String ensureTrailingSlash(String text) {
		return (!text.endsWith("/")) ? text + "/" : text;
	}

	private String removeStartingSlash(String text) {
		return (text.startsWith("/")) ? text.substring(1) : text;
	}

	private CloseableHttpClient getClient() {
		try {
			if (credentials != null) {
				CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
				credentialsProvider.setCredentials(AuthScope.ANY, this.credentials);

				return httpClientProvider.provideClientBuilder().setDefaultCredentialsProvider(credentialsProvider).build();
			} else {
				return httpClientProvider.provideHttpClient();
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not create HTTP client.", e);
		}
	}

	private Marshaller getMarshaller(String mimeType) {
		return marshallerRegistry.getMarshaller(mimeType);
	}

	@SuppressWarnings("deprecation")
	private static BasicConfigurableMarshallerRegistry defaultMarshallerRegistry() {
		BasicConfigurableMarshallerRegistry marshallerRegistry = new BasicConfigurableMarshallerRegistry();
		marshallerRegistry.registerMarshaller("application/json", new JsonStreamMarshaller());
		marshallerRegistry.registerMarshaller("application/x-www-form-urlencoded", new UrlEncodingMarshaller());
		return marshallerRegistry;
	}

	// ***************************************************************************************************
	// Custom logging
	// ***************************************************************************************************

	private void requestLogging(HttpRequestContext context, RequestBuilder requestBuilder, URI requestUri) {
		if (requestLogging != null) {
			logger.log(requestLogging, () -> {

				StringBuilder sb = new StringBuilder();
				sb.append("HTTP Request:\n URI: '");
				sb.append(requestUri);
				sb.append("'\n requestPath: '");
				sb.append(context.requestPath());
				sb.append("'\n requestMethod: '");
				sb.append(context.requestMethod());
				sb.append("'\n headerParameters: '");
				sb.append(context.headerParameters().map(a -> a.getName() + ":" + a.getValue()).collect(Collectors.joining(",")));
				sb.append("'\n queryParameters: '");
				sb.append(context.queryParameters().map(a -> a.getName() + ":" + a.getValue()).collect(Collectors.joining(",")));
				sb.append("'\n consumes: '");
				sb.append(context.consumes());
				sb.append("'\n produces: '");
				sb.append(context.produces());

				if (credentials != null) {
					sb.append("'\n user: '");
					sb.append(credentials.getUserPrincipal().getName());
				}

				Object payload = context.payload();
				if (payload != null) {
					sb.append("'\n requestPayload: '");
					sb.append(payload.toString());
				}

				sb.append("'\n requestPayloadAsString: '");
				HttpEntity entity = requestBuilder.getEntity();
				if (entity != null && !(entity instanceof ResourceHttpEntity)) {
					try {
						String body = EntityUtils.toString(entity);
						sb.append(body);
					} catch (Exception e) {
						logger.warn(() -> "Could not stringify HTTP client body for logging - ignore and continue");
					}
				}
				return sb.toString();
			});
		}
	}

	private void responseLogging(int code, Object responsePayload, GenericModelType responseType, HttpRequestContext context, long duration) {
		if (responseLogging != null) {
			if (logger.isLevelEnabled(responseLogging)) {
				StringBuilder sb = new StringBuilder();
				if (responseType != null) {
					sb.append("HTTP Reponse: \n responseType: '");
					sb.append(responseType.getTypeSignature());
				}
				sb.append("'\n code: '");
				sb.append(code);
				if (responsePayload != null) {
					sb.append("'\n responsePayload: '");
					sb.append(responsePayload);

					// TODO: not sure if this check is really necessary
					if (responsePayload instanceof GenericEntity) {
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						Marshaller bodyMarshaller = getMarshaller(context.produces());
						GmSerializationOptions options = GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.mid).build();
						bodyMarshaller.marshall(os, responsePayload, options);

						String responsePayloadAsString = new String(os.toByteArray(), Charset.defaultCharset());

						sb.append("'\n responsePayloadString: '");
						sb.append(responsePayloadAsString);
					}
				}
				sb.append("'\n duration: '");
				sb.append(duration);
				sb.append("ms'");

				String responseAsString = sb.toString();
				logger.log(responseLogging, responseAsString);
			}
		}
	}

}
