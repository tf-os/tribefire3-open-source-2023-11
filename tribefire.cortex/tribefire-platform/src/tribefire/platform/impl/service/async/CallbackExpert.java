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
package tribefire.platform.impl.service.async;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HTTP;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.service.common.FailureCodec;
import com.braintribe.model.service.api.AsynchronousRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.callback.AsynchronousRequestCallback;
import com.braintribe.model.service.api.callback.AsynchronousRequestCallbackCompletionRequest;
import com.braintribe.model.service.api.callback.AsynchronousRequestCallbackRequest;
import com.braintribe.model.service.api.callback.AsynchronousRequestProcessorCallback;
import com.braintribe.model.service.api.callback.AsynchronousRequestRestCallback;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.transport.http.HttpClientProvider;
import com.braintribe.transport.http.ResponseEntityInputStream;
import com.braintribe.transport.http.util.HttpTools;
import com.braintribe.utils.IOTools;

public class CallbackExpert {

	private static final Logger logger = Logger.getLogger(CallbackExpert.class);

	protected Evaluator<ServiceRequest> requestEvaluator;
	private HttpClientProvider httpClientProvider;
	private Marshaller marshaller;
	private CloseableHttpClient httpClient;
	private String contentType = "application/json";

	public void doCallback(AsynchronousRequest asyncRequest, Object result, Throwable t) {

		AsynchronousRequestCallback callback = asyncRequest.getCallback();
		if (callback != null) {
			if (callback instanceof AsynchronousRequestRestCallback) {
				AsynchronousRequestRestCallback cb = (AsynchronousRequestRestCallback) callback;

				doRestCallback(result, t, cb, asyncRequest);

			} else if (callback instanceof AsynchronousRequestProcessorCallback) {
				AsynchronousRequestProcessorCallback cb = (AsynchronousRequestProcessorCallback) callback;

				doProcessorCallback(result, t, cb, asyncRequest);

			} else {
				throw new IllegalArgumentException("The callback " + callback + " is not supported.");
			}
		}
	}

	private void doProcessorCallback(Object result, Throwable t, AsynchronousRequestProcessorCallback cb, AsynchronousRequest asyncRequest) {
		AsynchronousRequestCallbackCompletionRequest request = AsynchronousRequestCallbackCompletionRequest.T.create();
		request.setCustomData(cb.getCallbackProcessorCustomData());
		request.setDomainId(cb.getCallbackProcessorServiceDomain());
		request.setServiceId(cb.getCallbackProcessorId());
		request.setCorrelationId(asyncRequest.getCorrelationId());
		request.setResult(result);
		if (t != null) {
			request.setFailure(FailureCodec.INSTANCE.encode(t));
		}

		request.eval(requestEvaluator).get(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean future) {
				logger.debug(() -> "Callback for AsynchronousRequest " + asyncRequest + " was successful: " + future);
			}
			@Override
			public void onFailure(Throwable t) {
				logger.warn(() -> "Could not do the callback for " + asyncRequest, t);
			}
		});
	}

	private void doRestCallback(Object result, Throwable t, AsynchronousRequestRestCallback cb, AsynchronousRequest asyncRequest) {

		AsynchronousRequestCallbackCompletionRequest request = AsynchronousRequestCallbackCompletionRequest.T.create();
		request.setCustomData(cb.getCustomData());
		request.setServiceId(cb.getEndpointServiceId());
		request.setResult(result);
		request.setDomainId(null);
		request.setCorrelationId(asyncRequest.getCorrelationId());
		if (t != null) {
			request.setFailure(FailureCodec.INSTANCE.encode(t));
		}

		postCallback(request, cb);
	}

	private boolean postCallback(AsynchronousRequestCallbackRequest request, AsynchronousRequestRestCallback cb) {
		String body = null;
		String url = cb.getUrl();
		String correlationId = request.getCorrelationId();

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			marshaller.marshall(baos, request, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build());
			try {
				body = baos.toString("UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new Exception("Could not serialize job: " + request, e);
			}
		} catch (Exception e) {
			logger.warn(() -> "Error while serializing: " + request, e);
			return false;
		}
		CloseableHttpClient client = null;
		CloseableHttpResponse response = null;
		try {
			client = getClient();

			HttpPost post = new HttpPost(url);
			post.addHeader(HTTP.CONTENT_TYPE, contentType);
			if (correlationId != null) {
				post.addHeader("X-TF-Async-Correlation-Id", correlationId);
			}
			post.setEntity(new StringEntity(body));
			response = client.execute(post);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new Exception("Got a non-200 response from " + url + ": " + response);
			}
			ByteArrayOutputStream responseBaos = new ByteArrayOutputStream();
			try (InputStream is = new ResponseEntityInputStream(response)) {
				IOTools.pump(is, responseBaos);
			} catch (Exception e) {
				logger.error("Error while downloading banner from " + url, e);
			}
			if (logger.isDebugEnabled())
				logger.debug("Received from " + url + ": " + responseBaos.toString("UTF-8"));

		} catch (Exception e) {
			String message = "Could not callback " + url + " for request " + request + ": " + e.getMessage();
			logger.warn(() -> message, e);
			return false;
		} finally {
			HttpTools.consumeResponse(url, response);
			if (response != null) {
				try {
					response.close();
				} catch (Exception e) {
					logger.debug(() -> "Error while trying to close HTTP response.", e);
				}
			}
		}

		return true;
	}

	protected CloseableHttpClient getClient() throws Exception {
		if (this.httpClient == null) {
			this.httpClient = this.httpClientProvider.provideHttpClient();
		}
		return this.httpClient;
	}

	@Configurable
	@Required
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}
	@Configurable
	@Required
	public void setHttpClientProvider(HttpClientProvider httpClientProvider) {
		this.httpClientProvider = httpClientProvider;
	}
	@Configurable
	@Required
	public void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;
	}
	@Configurable
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
}
