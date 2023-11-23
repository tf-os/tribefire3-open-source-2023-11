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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.processing.http.client.HttpParameter;
import com.braintribe.processing.http.client.HttpRequestContext;
import com.braintribe.processing.http.client.HttpResponse;
import com.braintribe.processing.http.client.HttpResponseBuilder;

public class BasicResponseBuilder implements HttpResponseBuilder {

	private Object payload;
	private boolean generic = false;
	private List<HttpParameter> headerParameters = new ArrayList<>();
	private HttpRequestContext context;
	
	// ***************************************************************************************************
	// Instantiation
	// ***************************************************************************************************

	private BasicResponseBuilder(HttpRequestContext context) {
		this.context = context;
	}
	
	public static HttpResponseBuilder instance(HttpRequestContext context) {
		return new BasicResponseBuilder(context);
	}
	
	// ***************************************************************************************************
	// Builder
	// ***************************************************************************************************

	@Override
	public HttpResponseBuilder payload(Object payload) {
		this.payload = payload;
		return this;
	}

	@Override
	public HttpResponseBuilder isGeneric() {
		this.generic = true;
		return this;
	}
	
	@Override
	public HttpResponseBuilder addHeaderParameters(List<HttpParameter> headerParameters) {
		this.headerParameters.addAll(headerParameters);
		return this;
	}

	@Override
	public HttpResponseBuilder addHeaderParameter(HttpParameter headerParameter) {
		this.headerParameters.add(headerParameter);
		return this;
	}

	@Override
	public HttpResponseBuilder addHeaderParameter(String name, String value) {
		this.headerParameters.add(new HttpParameter(name, value));
		return this;
	}

	@Override
	public HttpResponse build() {
		return new HttpResponse() {
			
			@Override
			public <T> T combinedResponse() {
				T payload = payload();
				if (payload instanceof GenericEntity) {
					GenericEntity entityPayload = (GenericEntity) payload;
					EntityType<GenericEntity> entityType = entityPayload.entityType();
					headerParameters().forEach(hp -> {
						Property responseProperty = context.responseHeaderParameterTranslation(entityType, hp.getName());
						if (responseProperty != null && responseProperty.getType() == SimpleType.TYPE_STRING) {
							responseProperty.set(entityPayload, hp.getValue());
						}
					});
				}
				return payload;
			}
			
			@Override
			public <T> T payload() {
				return (T) payload;
			}
			
			@Override
			public Class<?> payloadType() {
				return (payload != null) ? payload.getClass() : null;
			}
			
			@Override
			public boolean isGeneric() {
				return generic;
			}
			
			@Override
			public String headerValue(String name) {
				return headerParameters()
					.filter(p -> name.equals(p.getName()))
					.map(HttpParameter::getValue)
					.findFirst()
					.orElse(null);
			}
			
			@Override
			public Stream<HttpParameter> headerParameters() {
				return headerParameters.stream();
			}
		};
	}
	
	
}
