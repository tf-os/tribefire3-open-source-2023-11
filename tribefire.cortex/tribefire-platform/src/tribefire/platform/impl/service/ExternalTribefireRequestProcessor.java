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
package tribefire.platform.impl.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.platform.service.ExternalTribefireRequest;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.transport.http.HttpClientProvider;
import com.braintribe.transport.http.ResponseEntityInputStream;
import com.braintribe.transport.http.util.HttpTools;
import com.braintribe.utils.IOTools;

/**
 * Experimental service processor that invokes service requests at a remote TFS instance.
 * Don't use this. It's not tested....
 */
public class ExternalTribefireRequestProcessor implements ServiceProcessor<ExternalTribefireRequest, ServiceResult> {

	private static final Logger logger = Logger.getLogger(ExternalTribefireRequestProcessor.class);
	
	protected HttpClientProvider httpClientProvider;

	protected MarshallerRegistry marshallerRegistry;
	private String mimeType = "application/json";
	private CloseableHttpClient httpClient;

	@Override
	public ServiceResult process(ServiceRequestContext requestContext, ExternalTribefireRequest request) {
		
		String tfsUrl = request.getTribefireServicesUrl();
		ServiceRequest serviceRequest = request.getServiceRequest();
		
		String body = null;
		String url = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Marshaller marshaller = marshallerRegistry.getMarshaller(mimeType);
			
			marshaller.marshall(baos, serviceRequest);
			body = baos.toString("UTF-8");
			
			url = tfsUrl;
			if (!url.endsWith("/")) {
				url = url.concat("/");
			}
			url = url.concat("api/v1/").concat(request.domainId()).concat("/").concat(serviceRequest.entityType().getTypeName());
			
		} catch(Exception e) {
			throw Exceptions.unchecked(e, "Error while processing serviceRequest: "+serviceRequest);
		}
		
		CloseableHttpClient client = null;
		CloseableHttpResponse response = null;
		try {
			client = getClient();

			HttpPost post = new HttpPost(url);
			post.setEntity(new StringEntity(body));
			response = client.execute(post);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new Exception("Got a non-200 response from "+url+": "+response);
			}
			ByteArrayOutputStream responseBaos = new ByteArrayOutputStream(); 
			try (InputStream is = new ResponseEntityInputStream(response)) {
				IOTools.pump(is, responseBaos);
			} catch(Exception e) {
				logger.error("Error while downloading banner from "+url, e);
			}
			if (logger.isDebugEnabled()) logger.debug("Received from "+url+": "+responseBaos.toString("UTF-8"));

		} catch(Exception e) {
			String message = "Could not invoke "+url+" for serviceRequest "+serviceRequest;
			throw Exceptions.unchecked(e, message);
		} finally {
			HttpTools.consumeResponse(url, response);
			IOTools.closeCloseable(response, logger);
		}
		
		//"https://localhost:8443/tribefire-services/rest/api/v1/xxxxx/com.braintribe.model.conversion.service.callback.CallbackRequest?serviceId=custom:documents.conversionCallbackProcessor&tfsessionid="+session.getSessionAuthorization().getSessionId()
		return null;
	}

	protected CloseableHttpClient getClient() throws Exception {
		if (this.httpClient == null) {
			this.httpClient = this.httpClientProvider.provideHttpClient();
		}
		return this.httpClient;
	}

	@Required
	public void setHttpClientProvider(HttpClientProvider httpClientProvider) {
		this.httpClientProvider = httpClientProvider;
	}
	@Required
	public void setMarshallerRegistry(MarshallerRegistry marshallerRegistry) {
		this.marshallerRegistry = marshallerRegistry;
	}
	@Configurable
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}


}
