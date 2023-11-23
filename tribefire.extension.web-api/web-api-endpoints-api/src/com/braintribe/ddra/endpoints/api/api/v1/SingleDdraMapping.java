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
package com.braintribe.ddra.endpoints.api.api.v1;

import com.braintribe.model.ddra.DdraMapping;
import com.braintribe.model.ddra.DdraUrlMethod;
import com.braintribe.model.ddra.endpoints.api.v1.ApiV1DdraEndpoint;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.prototyping.api.PrototypingRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.lcd.LazyInitialized;

/**
 * This entity is meant to be used in the DdraMappings map, it is meant to be immutable to guarantee that DdraMappings
 * is thread-safe.
 */
public class SingleDdraMapping {

	private final String path;
	private final DdraUrlMethod method;
	private final LazyInitialized<EntityType<? extends ServiceRequest>> entityType = new LazyInitialized<>(this::loadRequestType);
	private final String requestTypeSignature;
	private final ServiceRequest transformRequest;
	private final PrototypingRequest requestPrototyping;
	private final ApiV1DdraEndpoint endpointPrototype;
	private final Boolean announceAsMultipart;
	private final boolean hideSerializedRequest;

	private final String defaultProjection;
	private final String defaultMimeType;
	private final String defaultServiceDomain;

	private final String defaultPrettiness;
	private final String defaultDepth;
	private final Boolean defaultStabilizeOrder;
	private final Boolean defaultWriteEmptyProperties;
	private final Boolean defaultWriteAbsenceInformation;
	private final String defaultTypeExplicitness;
	private final Integer defaultEntityRecurrenceDepth;

	private final Boolean defaultDownloadResource;
	private final Boolean defaultSaveLocally;
	private final String defaultResponseFilename;
	private final String defaultResponseContentType;
	private final Boolean defaultUseSessionEvaluation;
	private final Boolean defaultPreserveTransportPayload;

	public SingleDdraMapping(String requestTypeSignature, ServiceRequest transformRequest, DdraMapping mapping) {
		this.requestTypeSignature = requestTypeSignature;
		this.transformRequest = transformRequest;
		this.requestPrototyping = mapping.getRequestPrototyping();
		this.endpointPrototype = mapping.getEndpointPrototype();
		this.path = mapping.getPath();
		this.method = mapping.getMethod();
		this.announceAsMultipart = mapping.getAnnounceAsMultipart();
		this.hideSerializedRequest = mapping.getHideSerializedRequest();

		this.defaultProjection = mapping.getDefaultProjection();
		this.defaultMimeType = mapping.getDefaultMimeType();
		this.defaultServiceDomain = mapping.getDefaultServiceDomain();
		this.defaultPrettiness = mapping.getDefaultPrettiness();
		this.defaultDepth = mapping.getDefaultDepth();
		this.defaultStabilizeOrder = mapping.getDefaultStabilizeOrder();
		this.defaultWriteEmptyProperties = mapping.getDefaultWriteEmptyProperties();
		this.defaultWriteAbsenceInformation = mapping.getDefaultWriteAbsenceInformation();
		this.defaultTypeExplicitness = mapping.getDefaultTypeExplicitness();
		this.defaultEntityRecurrenceDepth = mapping.getDefaultEntityRecurrenceDepth();
		this.defaultDownloadResource = mapping.getDefaultDownloadResource();
		this.defaultResponseFilename = mapping.getDefaultResponseFilename();
		this.defaultResponseContentType = mapping.getDefaultResponseContentType();
		this.defaultSaveLocally = mapping.getDefaultSaveLocally();
		this.defaultUseSessionEvaluation = mapping.getDefaultUseSessionEvaluation();
		this.defaultPreserveTransportPayload = mapping.getPreserveTransportPayload();
	}

	public Boolean getDefaultSaveLocally() {
		return defaultSaveLocally;
	}

	public Boolean getDefaultDownloadResource() {
		return defaultDownloadResource;
	}

	public String getDefaultResponseFilename() {
		return defaultResponseFilename;
	}

	public String getDefaultResponseContentType() {
		return defaultResponseContentType;
	}

	public String getDefaultProjection() {
		return defaultProjection;
	}

	public String getDefaultMimeType() {
		return defaultMimeType;
	}

	public DdraUrlMethod getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}

	public Boolean getAnnounceAsMultipart() {
		return announceAsMultipart;
	}

	public boolean getHideSerializedRequest() {
		return hideSerializedRequest;
	}

	/**
	 * @return the request type, this is never {@code null} and is ensured to be a ServiceRequest
	 */
	public EntityType<? extends ServiceRequest> getRequestType() {
		return entityType.get();
	}

	private EntityType<? extends ServiceRequest> loadRequestType() {
		EntityType<?> requestType = requestTypeSignature != null ? EntityTypes.get(requestTypeSignature) : null;
		if (requestType == null || !ServiceRequest.T.isAssignableFrom(requestType)) {
			throw new IllegalStateException("The requestType '" + requestTypeSignature + "' does not correspond to a ServiceRequest.");
		}

		return (EntityType<? extends ServiceRequest>) requestType;
	}

	public ServiceRequest getTransformRequest() {
		return transformRequest;
	}

	public PrototypingRequest getRequestPrototyping() {
		return requestPrototyping;
	}

	public ApiV1DdraEndpoint getEndpointPrototype() {
		return endpointPrototype == null ? null : endpointPrototype.clone(new StandardCloningContext());
	}

	public String getDefaultServiceDomain() {
		return defaultServiceDomain;
	}

	public String getDefaultPrettiness() {
		return defaultPrettiness;
	}

	public String getDefaultDepth() {
		return defaultDepth;
	}

	public Boolean getDefaultStabilizeOrder() {
		return defaultStabilizeOrder;
	}

	public Boolean getDefaultWriteEmptyProperties() {
		return defaultWriteEmptyProperties;
	}

	public Boolean getDefaultWriteAbsenceInformation() {
		return defaultWriteAbsenceInformation;
	}

	public String getDefaultTypeExplicitness() {
		return defaultTypeExplicitness;
	}

	public Integer getDefaultEntityRecurrenceDepth() {
		return defaultEntityRecurrenceDepth;
	}

	public Boolean getDefaultUseSessionEvaluation() {
		return defaultUseSessionEvaluation;
	}

	public Boolean getDefaultPreserveTransportPayload() {
		return defaultPreserveTransportPayload;
	}

	public String getDefaultEndpointParameter(String name) {
		switch (name) {
			case "depth":
				return getDefaultDepth();
			case "projection":
				return getDefaultProjection();
			case "prettiness":
				return getDefaultPrettiness();
			case "entityRecurrenceDepth":
				return this.defaultEntityRecurrenceDepth != null ? String.valueOf(getDefaultEntityRecurrenceDepth()) : null;
			case "mimeType":
				return getDefaultMimeType();
			case "serviceDomain":
				return getDefaultServiceDomain();
			case "typeExplicitness":
				return getDefaultTypeExplicitness();
			case "stabilizeOrder":
				return this.defaultStabilizeOrder != null ? String.valueOf(getDefaultStabilizeOrder()) : null;
			case "writeAbsenceInformation":
				return this.defaultWriteAbsenceInformation != null ? String.valueOf(getDefaultWriteAbsenceInformation()) : null;
			case "writeEmptyProperties":
				return this.defaultWriteEmptyProperties != null ? String.valueOf(getDefaultWriteEmptyProperties()) : null;
			case "responseContentType":
				return getDefaultResponseContentType();
			case "responseFilename":
				return getDefaultResponseFilename();
			case "saveLocally":
				return getDefaultSaveLocally() != null ? String.valueOf(getDefaultSaveLocally()) : null;
			case "downloadResource":
				return getDefaultDownloadResource() != null ? String.valueOf(getDefaultDownloadResource()) : null;
			case "defaultUseSessionEvaluation":
				return getDefaultUseSessionEvaluation() != null ? String.valueOf(getDefaultUseSessionEvaluation()) : null;
			default:
				return null;
		}
	}

}
