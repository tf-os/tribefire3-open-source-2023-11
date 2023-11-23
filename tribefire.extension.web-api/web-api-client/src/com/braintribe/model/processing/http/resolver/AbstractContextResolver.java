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
package com.braintribe.model.processing.http.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.marshaller.api.PropertyTypeInferenceOverride;
import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.http.meta.HttpConsumes;
import com.braintribe.model.deployment.http.meta.HttpDateFormatting;
import com.braintribe.model.deployment.http.meta.HttpDefaultFailureResponseType;
import com.braintribe.model.deployment.http.meta.HttpDefaultSuccessResponseType;
import com.braintribe.model.deployment.http.meta.HttpMethod;
import com.braintribe.model.deployment.http.meta.HttpParam;
import com.braintribe.model.deployment.http.meta.HttpParamType;
import com.braintribe.model.deployment.http.meta.HttpPath;
import com.braintribe.model.deployment.http.meta.HttpProduces;
import com.braintribe.model.deployment.http.meta.HttpSuccessCodes;
import com.braintribe.model.deployment.http.meta.params.HttpBodyParam;
import com.braintribe.model.deployment.http.meta.params.HttpRequestIsBody;
import com.braintribe.model.deployment.http.meta.params.HttpResourceStreamBodyParam;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.ScalarType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.EntityTypeMetaData;
import com.braintribe.model.meta.data.constraint.TypeSpecification;
import com.braintribe.model.processing.http.HttpContextResolver;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.processing.http.client.HttpClient;
import com.braintribe.processing.http.client.HttpConstants;
import com.braintribe.processing.http.client.HttpParameter;
import com.braintribe.processing.http.client.HttpRequestContext;
import com.braintribe.processing.http.client.HttpRequestContextBuilder;
import com.braintribe.utils.StringTools;

public abstract class AbstractContextResolver implements HttpContextResolver {

	private static final Logger logger = Logger.getLogger(AbstractContextResolver.class);

	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	protected Set<String> resolverUseCases = Collections.singleton("remote-http");

	private int defaultSuccessCode = HttpConstants.HTTP_CODE_OK;

	private static final GenericModelType bodyParametersType = GMF.getTypeReflection().getType("map<string,object>");

	// ***************************************************************************************************
	// Setter
	// ***************************************************************************************************

	@Configurable
	public void setResolverUseCases(Set<String> resolverUseCases) {
		this.resolverUseCases = resolverUseCases;
	}

	@Configurable
	public void setDefaultSuccessCode(int defaultSuccessCode) {
		this.defaultSuccessCode = defaultSuccessCode;
	}

	abstract protected ModelMdResolver getModelResolver(ServiceRequestContext serviceContext, ServiceRequest serviceRequest);
	abstract protected HttpClient getHttpClient(RequestContextResolver contextResolver);

	// ***************************************************************************************************
	// ContextResolver
	// ***************************************************************************************************

	@Override
	public HttpRequestContext resolve(ServiceRequestContext serviceContext, ServiceRequest serviceRequest) {

		ModelMdResolver modelResolver = getModelResolver(serviceContext, serviceRequest);

		RequestContextResolver resolver = new RequestContextResolver(serviceContext, serviceRequest, modelResolver);
		HttpClient httpClient = getHttpClient(resolver);

		HttpRequestContextBuilder contextBuilder = HttpRequestContextBuilder.instance(httpClient);
		resolver.resolveParameters();
		resolver.resolveResponseTypes(contextBuilder);

		resolver.resolve(resolver::getQueryParameters, contextBuilder::addQueryParameters);
		resolver.resolve(resolver::getHeaderParameters, contextBuilder::addHeaderParameters);
		resolver.resolve(resolver::resolveRequestMethod, contextBuilder::requestMethod);
		resolver.resolve(resolver::resolveRequestIsBody, md -> {
			if (md != null) {
				contextBuilder.payload(serviceRequest);
			}
		});
		resolver.resolve(resolver::resolveRequestPath, contextBuilder::requestPath);
		resolver.resolve(resolver::resolveProduces, contextBuilder::produces);
		resolver.resolve(resolver::resolveConsumes, contextBuilder::consumes);
		resolver.resolve(resolver::resolvePayload, p -> {
			if (p == resolver.bodyParameters) {
				contextBuilder.payloadType(bodyParametersType);
			}
			contextBuilder.streamResourceContent(resolver.streamResourceContent);
			contextBuilder.payloadIfEmpty(p);
		});

		HttpDateFormatting dateFormatting = resolver.resolveDateFormatting();
		if (dateFormatting != null) {
			contextBuilder.dateFormatting(dateFormatting.getDateFormat(), dateFormatting.getDefaultZone(), dateFormatting.getDefaultLocale());
		}

		BiFunction<EntityType<?>, Property, GenericModelType> inferer = serviceContext.getAttribute(PropertyTypeInferenceOverride.class);

		contextBuilder.propertyTypeInference((entityType, property) -> {
			if (inferer != null) {
				GenericModelType propertyType = inferer.apply(entityType, property);
				if (propertyType != null)
					return propertyType;
			}

			//@formatter:off
			TypeSpecification inferredType = 
					modelResolver
						.entityType(entityType)
						.property(property)
						.meta(TypeSpecification.T)
						.exclusive();
			//@formatter:off
			return (inferredType != null) ? typeReflection.getType(inferredType.getType().getTypeSignature()) : property.getType();
		});
		
		contextBuilder.responseBodyParameterTranslation((entityType, parameterName) -> {
			PropertyTranslation propertyTranslation = resolver.responsePropertyTranslations.get(entityType);
			Property property = null;
			if (propertyTranslation != null) {
				property = propertyTranslation.bodyParameters.get(parameterName);
			}
			return property != null ? property : entityType.findProperty(parameterName); 
		});
		
		contextBuilder.requestBodyParameterTranslation(property -> {
			//@formatter:off
			HttpParam md = 
					modelResolver
						.property(property)
						.meta(HttpParam.T)
						.exclusive();
			//@formatter:off
			return resolver.resolveParameterName(property, md);
		});
		
		return contextBuilder.build();
	}
	
	// ***************************************************************************************************
	// Helper
	// ***************************************************************************************************
	
	protected class RequestContextResolver {
		
		protected ServiceRequestContext serviceContext;
		protected ServiceRequest serviceRequest;
		protected ModelMdResolver modelResolver;
		protected EntityMdResolver entityResolver;
		protected EntityType<GenericEntity> requestType;
		
		protected Map<String, Object> pathParameters = new HashMap<>();
		protected Map<String, Object> bodyParameters = new HashMap<>();
		
		protected Map<EntityType<?>, PropertyTranslation> responsePropertyTranslations = new HashMap<>();
		
		protected List<HttpParameter> headerParameters = new ArrayList<>();
		protected List<HttpParameter> queryParameters = new ArrayList<>();
		
		protected boolean streamResourceContent = false;
		
		public RequestContextResolver(ServiceRequestContext serviceContext, ServiceRequest serviceRequest, ModelMdResolver modelResolver) {
			this.serviceContext = serviceContext;
			this.serviceRequest = serviceRequest;
			this.modelResolver = modelResolver;
			this.requestType = this.serviceRequest.entityType();
			this.entityResolver = this.modelResolver.entity(this.serviceRequest);
		}
		
		public List<HttpParameter> getHeaderParameters() {
			return headerParameters;
		}
		
		public List<HttpParameter> getQueryParameters() {
			return queryParameters;
		}
		
		private String resolveRequestPath() {
			String pathTemplate = resolveMd(HttpPath.T, HttpPath::getPath);
			return (pathTemplate != null) ? resolveTemplate(pathTemplate) : null;
		}

		private String resolveTemplate(String pathTemplate) {
			return StringTools.patternFormat(pathTemplate, this.pathParameters);
		}

		private String resolveRequestMethod() {
			return resolveMd(HttpMethod.T, this::resolveMethod);
		}
		
		private Object resolveRequestIsBody() {
			return resolveMd(HttpRequestIsBody.T, md -> {return md;});
		}
		
		private String resolveProduces() {
			return resolveMd(HttpProduces.T, HttpProduces::getMimeType);
		}

		private String resolveConsumes() {
			return resolveMd(HttpConsumes.T, HttpConsumes::getMimeType);
		}
		
		private HttpDateFormatting resolveDateFormatting() {
			return this.entityResolver.meta(com.braintribe.model.deployment.http.meta.HttpDateFormatting.T).exclusive();
		}

		private Object resolvePayload() {
			switch (bodyParameters.size()) {
			case 0: return null;
			case 1: return bodyParameters.values().iterator().next();
			default: 
				return this.bodyParameters;
			}
		}

		private void resolveParameters() {
			this.serviceRequest.entityType().getProperties()
				.stream()
				.forEach(this::resolveParameter);
		}

		private void resolveParameter(Property p) {
			PropertyMdResolver propertyResolver = this.entityResolver.property(p);
			HttpParam restParam = propertyResolver.meta(HttpParam.T).exclusive();
			
			if (restParam != null) {
				switch (restParam.paramType()) {
				case HEADER:
					resolveParameter(p, restParam, this.headerParameters::add);
					break;
				case QUERY:
					resolveParameter(p, restParam, this.queryParameters::add);
					break;
				case BODY:
					// no value resolving for body parameters. thats the marshallers job.
						Object value = p.get(this.serviceRequest);
						HttpBodyParam bodyParam = (HttpBodyParam) restParam;
						if (value != null || !bodyParam.getIgnoreEmptyValue()) {
							this.bodyParameters.put(resolveParameterName(p, restParam), value); 
						}
						if (bodyParam instanceof HttpResourceStreamBodyParam) {
							streamResourceContent = true;
						}
					break;
				case PATH:
					this.pathParameters.put(resolveParameterName(p, restParam), resolveParameterValue(p, restParam));
					break;
				case UNMAPPED:
					// the property is configured to be unmapped, so we simply ignore it.
					break;
				}
			} else {
				// We add all properties as path parameter if not done already with specific md configuration
				this.pathParameters.put(resolveParameterName(p, null), resolveParameterValue(p, null));
			}
		}

		private void resolveParameter(Property p, HttpParam restParam, Consumer<HttpParameter> consumer) {
			String parameterName = resolveParameterName(p, restParam);
			
			resolveParameterValues(p, restParam)
				.stream()
				.map(v -> new HttpParameter(parameterName, v))
				.forEach(consumer);
			
		}

		private String resolveParameterName(Property p, HttpParam restParam) {
			String paramName = null;
			if (restParam != null) {
				paramName = restParam.getParamName();
			}
			return (paramName != null) ? paramName : p.getName();
		}
		
		private String resolveParameterValue(Property p, HttpParam restParam) {
			Collection<String> values = resolveParameterValues(p, restParam);
			return (values.size() > 0) ? values.iterator().next() : null;
		}
		
		private Collection<String> resolveParameterValues(Property p, @SuppressWarnings("unused") HttpParam restParam) {
			Object value = p.get(this.serviceRequest);
			return (value != null) ? encodeValues(value) : Collections.emptyList(); // TODO: handle encoding
		}
		
		@SuppressWarnings("fallthrough")
		private List<String> encodeValues(Object value) {
			if (value == null) {
				return Collections.emptyList();
			}
			
			List<String> encodedValues = new ArrayList<>();
			
			GenericModelType type = typeReflection.getType(value);
			switch (type.getTypeCode()) {
			case booleanType:
			case dateType:
			case decimalType:
			case doubleType:
			case stringType:
			case floatType:
			case integerType:
			case longType:
			case enumType:
				ScalarType scalarType = (ScalarType) type;
				String encodedValue = scalarType.instanceToString(value);
				encodedValues.add(encodedValue);
				break;
			case setType:
			case listType:
				CollectionType collectionType = (CollectionType) type;
				GenericModelType elementType = collectionType.getCollectionElementType();
				if (elementType.isScalar()) {
					@SuppressWarnings("unchecked")
					Collection<Object> values = (Collection<Object>) value;
					values.stream()
					.map(this::encodeValues)
					.forEach(encodedValues::addAll);
					break;
				}
			default:
				//TODO: we currently do not support anything else then scalar types or collections of scalar. 
				// throw new HttpProcessingException("Unsupported parameter type: "+type.getTypeSignature());
				break;
			}

			return encodedValues;
		}

		
		private void resolveResponseTypes(HttpRequestContextBuilder contextBuilder) {

			GenericModelType evaluatesTo = this.requestType.getEffectiveEvaluatesTo();
			if (evaluatesTo != null) {
				indexPotentialResponseType(evaluatesTo);
			} else {
				logger.warn(() -> "The response type of "+this.requestType+" is null.");
			}
			
			HttpSuccessCodes successCodes = this.entityResolver.meta(HttpSuccessCodes.T).exclusive();
			if (successCodes != null) {
				successCodes.getSuccessCodes().forEach(c -> contextBuilder.addResponseType(c, evaluatesTo));
			} else {

				// Per default we assume the evaluatesTo type as success response type.
				// This could be overridden by according HttpProduces metadata. 
				contextBuilder.addResponseType(defaultSuccessCode, evaluatesTo);
			}

			// If configured adding specified successCodes (will be used to determine wasSuccessful())
			HttpSuccessCodes successCodesMd = this.entityResolver.meta(HttpSuccessCodes.T).exclusive();
			if (successCodesMd != null) {
				contextBuilder.addSuccessCodes(new HashSet<>(successCodesMd.getSuccessCodes()));
			}
			
			// If configured adding default success and failure types.
			HttpDefaultFailureResponseType defaultFailureTypeMd = this.entityResolver.meta(HttpDefaultFailureResponseType.T).exclusive();
			if (defaultFailureTypeMd != null) {
				contextBuilder.defaultFailureResponseType(resolveResponseType(defaultFailureTypeMd.getResponseType()));
			}
			HttpDefaultSuccessResponseType defaultSuccessTypeMd = this.entityResolver.meta(HttpDefaultSuccessResponseType.T).exclusive();
			if (defaultSuccessTypeMd != null) {
				contextBuilder.defaultSuccessResponseType(resolveResponseType(defaultSuccessTypeMd.getResponseType()));
			}
			
			// Adding individually specified response mappings (code to type)
			List<HttpProduces> producesMd = this.entityResolver.meta(HttpProduces.T).list();
			producesMd.stream()
				.forEach(m -> {
					contextBuilder.addResponseType(m.getResponseCode(),resolveResponseType(m.getResponseType()));
					contextBuilder.addStatusCodeInfo(m.getResponseCode(), m.getUseOriginalStatusCode());
				});
			
		}
		
		
		
		
		private void indexPotentialResponseType(GenericModelType responseType) {
			switch (responseType.getTypeCode()) {
			case booleanType:
			case dateType:
			case decimalType:
			case doubleType:
			case floatType:
			case integerType:
			case longType:
			case stringType:
			case enumType:
			case objectType:
				break;
			case entityType:
				EntityType<?> entityType = (EntityType<?>) responseType;
				if (!responsePropertyTranslations.containsKey(entityType)) {
					PropertyTranslation propertyTranslation = new PropertyTranslation();
					responsePropertyTranslations.put(entityType, propertyTranslation);
					
					entityType.getProperties().forEach(p -> {
						//@formatter:off
						HttpParam md = 
								modelResolver
								.entityType(entityType)
								.property(p.getName())
								.meta(HttpParam.T)
								.exclusive();
						//@formatter:off
						String paramName = resolveParameterName(p, md);
						
						if (md == null || md.paramType() == HttpParamType.BODY) {
							propertyTranslation.bodyParameters.put(paramName, p);
						} else if (md.paramType() == HttpParamType.HEADER) {
							propertyTranslation.headerParameters.put(paramName, p);
						}
						
						indexPotentialResponseType(p.getType());
					});
				}
				break;
			case listType:
			case setType:
				CollectionType collectionType = (CollectionType) responseType;
				indexPotentialResponseType(collectionType.getCollectionElementType());
				break;
			case mapType:
				MapType mapType = (MapType) responseType;
				indexPotentialResponseType(mapType.getKeyType());
				indexPotentialResponseType(mapType.getValueType());
				break;
			}
			
		}

		private GenericModelType resolveResponseType(GmType gmType) {
			String typeSignature = "object";
			if (gmType != null) {
				typeSignature = gmType.getTypeSignature();
			}
			GenericModelType responseType = typeReflection.getType(typeSignature);
			indexPotentialResponseType(responseType);
			return responseType;
		}
		
		private <T> void resolve(Supplier<T> supplier, Consumer<T> consumer) {
			T result = supplier.get();
			if (result != null) {
				consumer.accept(result);
			}
		}
		
		private <T extends EntityTypeMetaData, R> R resolveMd(EntityType<T> mdType, Function<T, R> resolver) {
			T md = this.entityResolver.meta(mdType).exclusive();
			return (md != null) ? resolver.apply(md) : null;
		}
		
		private String resolveMethod(HttpMethod md) {
			return md.methodType();
		}
		

		
	}
	
	protected class PropertyTranslation {
		Map<String, Property> bodyParameters = new HashMap<>();
		Map<String, Property> headerParameters = new HashMap<>();

	}
	
	

}
