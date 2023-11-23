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
package com.braintribe.processing.http.client.builder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.processing.http.client.HttpClient;
import com.braintribe.processing.http.client.HttpDateFormatting;
import com.braintribe.processing.http.client.HttpParameter;
import com.braintribe.processing.http.client.HttpRequestContext;
import com.braintribe.processing.http.client.HttpRequestContextBuilder;
import com.braintribe.utils.lcd.CollectionTools2;

public class BasicRequestContextBuilder implements HttpRequestContextBuilder {

	private HttpClient httpClient;

	private String requestPath = DEFAULT_PATH;
	private String requestMethod = HTTP_METHOD_POST;
	private String consumes = DEFAULT_MIME_TYPE;
	private String produces = DEFAULT_MIME_TYPE;

	private Object payload;
	private GenericModelType payloadType;
	private Map<Integer, GenericModelType> responseTypesByCode = CollectionTools2.newMap();
	private Map<Integer, Boolean> useOrgStatusCode = CollectionTools2.newMap();

	private List<HttpParameter> queryParameters = CollectionTools2.newList();
	private List<HttpParameter> headerParameters = CollectionTools2.newList();
	private Set<Integer> successCodes = CollectionTools2.newSet();

	private GenericModelType defaultSuccessResponseType;
	private GenericModelType defaultFailureResponseType;

	private Set<GenericModelType> responseTypes = CollectionTools2.newSet();
	private GmSerializationOptions payloadMarshallingOptions;
	private BiFunction<EntityType<?>, Property, GenericModelType> propertyTypeInference;
	private BiFunction<EntityType<?>, String, Property> responseBodyParameterTranslation;
	private BiFunction<EntityType<?>, String, Property> responseHeaderParameterTranslation;
	private Function<Property, String> requestBodyParameterTranslation;

	private String dateFormat = null;
	private String dateDefaultZone = null;
	private String dateDefaultLocale = null;
	private boolean streamResourceContent = false;

	// ***************************************************************************************************
	// Instantiation
	// ***************************************************************************************************

	private BasicRequestContextBuilder(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public static HttpRequestContextBuilder instance(HttpClient httpClient) {
		return new BasicRequestContextBuilder(httpClient);
	}

	// ***************************************************************************************************
	// Builder
	// ***************************************************************************************************

	@Override
	public HttpRequestContextBuilder requestPath(String requestPath) {
		this.requestPath = requestPath;
		return this;
	}

	@Override
	public HttpRequestContextBuilder requestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
		return this;
	}

	@Override
	public HttpRequestContextBuilder consumes(String consumes) {
		this.consumes = consumes;
		return this;
	}

	@Override
	public HttpRequestContextBuilder produces(String produces) {
		this.produces = produces;
		return this;
	}

	@Override
	public HttpRequestContextBuilder addSuccessCodes(Set<Integer> successCodes) {
		if (this.successCodes == null) {
			this.successCodes = CollectionTools2.newSet();
		}
		this.successCodes.addAll(successCodes);
		return this;
	}

	@Override
	public HttpRequestContextBuilder addSuccessCodes(Integer... successCodes) {
		return addSuccessCodes(new HashSet<>(Arrays.asList(successCodes)));
	}

	@Override
	public HttpRequestContextBuilder addSuccessCode(Integer successCode) {
		return addSuccessCodes(successCode);
	}

	@Override
	public HttpRequestContextBuilder allSuccess() {
		this.successCodes = null;
		return this;
	}

	@Override
	public HttpRequestContextBuilder payload(Object payload) {
		this.payload = payload;
		return this;
	}

	@Override
	public HttpRequestContextBuilder payloadIfEmpty(Object payload) {
		if (this.payload == null) {
			this.payload = payload;
		}
		return this;
	}

	@Override
	public HttpRequestContextBuilder payloadType(GenericModelType payloadType) {
		this.payloadType = payloadType;
		return this;
	}

	@Override
	public HttpRequestContextBuilder addResponseTypes(Map<Integer, GenericModelType> responseTypes) {
		this.responseTypesByCode.putAll(responseTypes);
		this.responseTypes.addAll(responseTypes.values());
		return this;
	}

	@Override
	public HttpRequestContextBuilder addResponseType(Integer responseCode, GenericModelType responseType) {
		this.responseTypesByCode.put(responseCode, responseType);
		this.responseTypes.add(responseType);
		return this;
	}

	@Override
	public HttpRequestContextBuilder addStatusCodeInfo(int responseCode, boolean withOriginalStatusCode) {
		this.useOrgStatusCode.put(responseCode, withOriginalStatusCode);
		return this;
	}

	@Override
	public HttpRequestContextBuilder defaultSuccessResponseType(GenericModelType responseType) {
		this.defaultSuccessResponseType = responseType;
		this.responseTypes.add(responseType);
		return this;
	}

	@Override
	public HttpRequestContextBuilder defaultFailureResponseType(GenericModelType responseType) {
		this.defaultFailureResponseType = responseType;
		this.responseTypes.add(responseType);
		return this;
	}

	@Override
	public HttpRequestContextBuilder addQueryParameters(List<HttpParameter> queryParameters) {
		this.queryParameters.addAll(queryParameters);
		return this;
	}

	@Override
	public HttpRequestContextBuilder addQueryParameter(HttpParameter queryParameter) {
		this.queryParameters.add(queryParameter);
		return this;
	}

	@Override
	public HttpRequestContextBuilder addQueryParameter(String name, String value) {
		this.queryParameters.add(new HttpParameter(name, value));
		return this;
	}

	@Override
	public HttpRequestContextBuilder addHeaderParameters(List<HttpParameter> headerParameters) {
		this.headerParameters.addAll(headerParameters);
		return this;
	}

	@Override
	public HttpRequestContextBuilder addHeaderParameter(HttpParameter headerParameter) {
		this.headerParameters.add(headerParameter);
		return this;
	}

	@Override
	public HttpRequestContextBuilder addHeaderParameter(String name, String value) {
		this.headerParameters.add(new HttpParameter(name, value));
		return this;
	}

	@Override
	public HttpRequestContextBuilder payloadMarshallingOptions(GmSerializationOptions payloadMarshallingOptions) {
		this.payloadMarshallingOptions = payloadMarshallingOptions;
		return this;
	}

	@Override
	public HttpRequestContextBuilder propertyTypeInference(BiFunction<EntityType<?>, Property, GenericModelType> propertyTypeInference) {
		this.propertyTypeInference = propertyTypeInference;
		return this;
	}

	@Override
	public HttpRequestContextBuilder dateFormatting(String dateFormat) {
		this.dateFormat = dateFormat;
		return this;
	}

	@Override
	public HttpRequestContextBuilder dateFormatting(String dateFormat, String defaultZone, String defaultLocale) {
		this.dateFormat = dateFormat;
		this.dateDefaultZone = defaultZone;
		this.dateDefaultLocale = defaultLocale;
		return this;
	}

	@Override
	public HttpRequestContextBuilder streamResourceContent(boolean streamResourceContent) {
		this.streamResourceContent = streamResourceContent;
		return this;
	}

	@Override
	public HttpRequestContextBuilder responseBodyParameterTranslation(BiFunction<EntityType<?>, String, Property> responseBodyParameterTranslation) {
		this.responseBodyParameterTranslation = responseBodyParameterTranslation;
		return this;
	}

	@Override
	public HttpRequestContextBuilder responseHeaderParameterTranslation(
			BiFunction<EntityType<?>, String, Property> responseHeaderParameterTranslation) {
		this.responseHeaderParameterTranslation = responseHeaderParameterTranslation;
		return this;
	}

	@Override
	public HttpRequestContextBuilder requestBodyParameterTranslation(Function<Property, String> propertyNameSupplier) {
		this.requestBodyParameterTranslation = propertyNameSupplier;
		return this;
	}

	// ***************************************************************************************************
	// RestProcessor
	// ***************************************************************************************************

	@Override
	public HttpRequestContext build() {

		if (successCodes != null && successCodes.isEmpty()) {
			successCodes.addAll(DEFAULT_SUCCESS_CODES);
		}

		return new HttpRequestContext() {

			@Override
			public HttpClient httpClient() {
				return httpClient;
			}

			@Override
			public String requestPath() {
				return requestPath;
			}

			@Override
			public String requestMethod() {
				return requestMethod;
			}

			@Override
			public Stream<HttpParameter> queryParameters() {
				return queryParameters.stream();
			}

			@Override
			public Stream<HttpParameter> headerParameters() {
				return headerParameters.stream();
			}

			@Override
			public String consumes() {
				return consumes;
			}

			@Override
			public GenericModelType responseTypeForCode(int responseCode) {
				GenericModelType responseType = responseTypesByCode.get(responseCode);
				if (responseType == null) {
					responseType = wasSuccessful(responseCode) ? defaultSuccessResponseType : defaultFailureResponseType;
				}
				return responseType;
			}

			@Override
			public Set<GenericModelType> responseTypes() {
				return responseTypes;
			}

			@Override
			public String produces() {
				return produces;
			}
			@Override
			public Object payload() {
				return payload;
			}

			@Override
			public GenericModelType payloadType() {
				return payloadType;
			}

			@Override
			public boolean wasSuccessful(int responseCode) {
				return (successCodes == null) || (successCodes.contains(responseCode));
			}

			@Override
			public boolean throwExceptionOnErrorCode(int responseCode) {
				return useOrgStatusCode.getOrDefault(responseCode, Boolean.FALSE);
			}

			@Override
			public GmSerializationOptions payloadMarshallingOptions() {
				return payloadMarshallingOptions;
			}

			@Override
			public boolean streamResourceContent() {
				return streamResourceContent;
			}

			@Override
			public GenericModelType propertyTypeInference(EntityType<?> entityType, Property property) {
				return (propertyTypeInference != null) ? propertyTypeInference.apply(entityType, property)
						: HttpRequestContext.super.propertyTypeInference(entityType, property);
			}

			@Override
			public Property responseBodyParameterTranslation(EntityType<?> entityType, String propertyName) {
				return (responseBodyParameterTranslation != null) ? responseBodyParameterTranslation.apply(entityType, propertyName)
						: HttpRequestContext.super.responseBodyParameterTranslation(entityType, propertyName);
			}

			@Override
			public Property responseHeaderParameterTranslation(EntityType<?> entityType, String propertyName) {
				return (responseHeaderParameterTranslation != null) ? responseHeaderParameterTranslation.apply(entityType, propertyName)
						: HttpRequestContext.super.responseHeaderParameterTranslation(entityType, propertyName);
			}

			@Override
			public String requestBodyParameterTranslation(Property property) {
				return (requestBodyParameterTranslation != null) ? requestBodyParameterTranslation.apply(property)
						: HttpRequestContext.super.requestBodyParameterTranslation(property);
			}

			@Override
			public HttpDateFormatting dateFormatting() {
				if (dateFormat != null) {
					return new HttpDateFormatting() {

						@Override
						public String getDateFormat() {
							return dateFormat;
						}

						@Override
						public String getDefaultZone() {
							return dateDefaultZone;
						}

						@Override
						public String getDefaultLocale() {
							return dateDefaultLocale;
						}

					};
				}
				return null;
			}

		};
	}

}
