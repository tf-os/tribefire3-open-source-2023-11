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
package com.braintribe.gwt.gmview.util.client;

import java.util.Date;
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmrpc.api.client.user.ResourceSupport;
import com.braintribe.model.extensiondeployment.RequestProcessing;
import com.braintribe.model.extensiondeployment.meta.Preview;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.session.api.common.GmSessions;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resourceapi.request.GetPreview;
import com.braintribe.model.resourceapi.request.PreviewType;
import com.braintribe.processing.async.api.AsyncCallback;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

public class GmPreviewUtil {
	
	private PersistenceGmSession session;
	private Supplier<String> sessionIdProvider;
	private String baseUrl = "api/v1/";
	private String servicesUrl = "http://localhost:8080/tribefire-services/";
	private DateTimeFormat dtf = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL);
	private boolean useRestEvaluation = true;
	
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}
	
	public void setServicesUrl(String servicesUrl) {
		this.servicesUrl = servicesUrl;
	}
	
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	public void setSessionIdProvider(Supplier<String> sessionIdProvider) {
		this.sessionIdProvider = sessionIdProvider;
	}
	
	public void setUseRestEvaluation(boolean useRestEvaluation) {
		this.useRestEvaluation = useRestEvaluation;
	}
	
	public String previewUrl(GenericEntity entity, String useCase, PreviewType type) {
		Preview preview = getPreview(entity, useCase);
		
		if(preview == null)
			return "";
		
		String typeSignature = entity.type().getTypeSignature();
		String id = entity.getId() == null ? null : entity.getId().toString();		
		RequestProcessing requestProcessing = preview.getRequestProcessing();
		String domainId = GMEUtil.getDomainId(requestProcessing, session);
		String serviceId = GMEUtil.getServiceId(requestProcessing);
		String sessionId = sessionIdProvider.get();
		
		return requestUrl(domainId, serviceId, sessionId, typeSignature , id, type);
	}
	
	public Future<String> previewUrlAsync(GenericEntity entity) {
		return previewUrlAsync(entity, null, PreviewType.STANDARD);
	}
	
	Future<String> future = null;
	
	public Future<String> previewUrlAsync(GenericEntity entity, String useCase, PreviewType type) {
		
		if (entity.getId() == null)
			return new Future<>("");
		
		if (entity.type() == Resource.T) {
			Resource r = (Resource) entity;
			if (r.getMimeType() == null)
				return new Future<>("");
			else {
				if (r.getMimeType().startsWith("image"))
					return new Future<>(session.resources().url(r).asString());	
				else
					return new Future<>("");
			}
		}
		
		Preview preview = getPreview(entity, useCase); //GmSessions.getMetaData(entity).entity(entity).meta(Preview.T).exclusive();
		
		if (preview == null)
			return new Future<>(""); 
		
		String typeSignature = entity.type().getTypeSignature();
		String id = entity.getId().toString();
		RequestProcessing requestProcessing = preview.getRequestProcessing();
		String domainId = GMEUtil.getDomainId(requestProcessing, session);
		String serviceId = GMEUtil.getServiceId(requestProcessing);
		String sessionId = sessionIdProvider.get(); //session.getSessionAuthorization().getSessionId();

		if(useRestEvaluation) {
			return new Future<>(requestUrl(domainId, serviceId, sessionId, typeSignature , id, type));
		}
		else {
			future = new Future<String>();
			
			GetPreview getPreview = GetPreview.T.create();
			getPreview.setInstanceTypeSignature(entity.type().getTypeSignature());
			getPreview.setInstanceId(entity.getId().toString());
			getPreview.setTimestamp(dtf.format(new Date()));
			getPreview.setDomainId(domainId);
			getPreview.setServiceId(serviceId);
			
			getPreview.eval(session).with(ResourceSupport.class, true)
					.get(AsyncCallback.of(r -> future.onSuccess(session.resources().url(r).asString()), future::onFailure));
			return future;			
		}		
	}
	
	public Preview getPreview(GenericEntity entity, String useCase) {
		EntityMdResolver resolver = GmSessions.getMetaData(entity).entity(entity);
		if (useCase != null) {
			resolver.useCase(useCase);
		}
		return resolver.meta(Preview.T).exclusive();
	}
	
	public Preview getPreview(GenericEntity entity) {
		return getPreview(entity,null);
	}
	private String requestUrl(String domainId, String serviceId, String sessionId, String typeSignature, String id, PreviewType type) {
		return servicesUrl + baseUrl + domainId + "/" + GetPreview.T.getTypeSignature() + "?"
				+ addParam("sessionId", sessionId)
				+ addParam("instanceTypeSignature", typeSignature)
				+ addParam("instanceId", id)
				+ addParam("downloadResource", "true")
				+ (serviceId != null ? addParam("serviceId", serviceId) : "")
				+ addParam("timestamp", dtf.format(new Date()))
				+ addParam("previewType", type.name());
	}
	
	private String addParam(String key, String value) {
		if (value == null)
			return "";
		
		return "&" + key + "=" + URL.encodeQueryString(value);
	}

}
