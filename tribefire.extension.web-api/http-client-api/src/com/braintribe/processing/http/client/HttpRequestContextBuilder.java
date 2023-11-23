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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.processing.http.client.builder.BasicRequestContextBuilder;

public interface HttpRequestContextBuilder extends HttpConstants {

	static HttpRequestContextBuilder instance(HttpClient httpClient) {
		return BasicRequestContextBuilder.instance(httpClient);
	}

	HttpRequestContextBuilder requestPath(String requestPath);

	HttpRequestContextBuilder requestMethod(String requestMethod);

	HttpRequestContextBuilder consumes(String consumes);

	HttpRequestContextBuilder produces(String produces);

	HttpRequestContextBuilder addSuccessCodes(Set<Integer> successCodes);

	HttpRequestContextBuilder addSuccessCodes(Integer... successCodes);

	HttpRequestContextBuilder addSuccessCode(Integer successCode);

	HttpRequestContextBuilder allSuccess();

	HttpRequestContextBuilder payload(Object payload);

	HttpRequestContextBuilder payloadIfEmpty(Object payload);

	HttpRequestContextBuilder payloadType(GenericModelType payloadType);

	HttpRequestContextBuilder addResponseTypes(Map<Integer, GenericModelType> responseTypes);

	HttpRequestContextBuilder addResponseType(Integer responseCode, GenericModelType responseType);

	HttpRequestContextBuilder defaultSuccessResponseType(GenericModelType responseType);

	HttpRequestContextBuilder defaultFailureResponseType(GenericModelType responseType);

	HttpRequestContextBuilder addQueryParameters(List<HttpParameter> queryParameters);

	HttpRequestContextBuilder addQueryParameter(HttpParameter queryParameter);

	HttpRequestContextBuilder addQueryParameter(String name, String value);

	HttpRequestContextBuilder addHeaderParameters(List<HttpParameter> headerParameters);

	HttpRequestContextBuilder addHeaderParameter(HttpParameter headerParameter);

	HttpRequestContextBuilder addHeaderParameter(String name, String value);

	HttpRequestContextBuilder payloadMarshallingOptions(GmSerializationOptions payloadMarshallingOptions);

	HttpRequestContextBuilder addStatusCodeInfo(int responseCode, boolean withOriginalStatusCode);

	HttpRequestContextBuilder propertyTypeInference(BiFunction<EntityType<?>, Property, GenericModelType> propertyTypeInference);

	HttpRequestContextBuilder dateFormatting(String dateFormat);

	HttpRequestContextBuilder dateFormatting(String dateFormat, String defaultZone, String defaultLocale);

	HttpRequestContextBuilder streamResourceContent(boolean streamResourceContent);

	HttpRequestContextBuilder responseBodyParameterTranslation(BiFunction<EntityType<?>, String, Property> responseBodyPropertyTranslation);

	HttpRequestContextBuilder responseHeaderParameterTranslation(BiFunction<EntityType<?>, String, Property> responseBodyPropertyTranslation);

	HttpRequestContextBuilder requestBodyParameterTranslation(Function<Property, String> requestBodyPropertyTranslation);

	HttpRequestContext build();

}