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
package com.braintribe.model.processing.dmbrpc.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.RuntimeMBeanException;

import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.common.ExceptionBuilder;
import com.braintribe.exception.AuthorizationException;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.processing.dmbrpc.api.GmDmbRpcInvoker;
import com.braintribe.model.processing.dmbrpc.common.ByteArrayInputStreamProviderCallable;
import com.braintribe.model.processing.dmbrpc.common.CallableInputStreamProvider;
import com.braintribe.model.processing.dmbrpc.common.DmbConstants;
import com.braintribe.model.processing.dmbrpc.common.MarshallerCodec;
import com.braintribe.model.processing.rpc.commons.api.GmRpcException;
import com.braintribe.model.processing.rpc.commons.api.RpcConstants;
import com.braintribe.model.processing.rpc.commons.impl.client.GmRpcClientBase;
import com.braintribe.model.processing.rpc.commons.impl.client.GmRpcClientRequestContext;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceException;
import com.braintribe.model.resource.CallStreamCapture;
import com.braintribe.model.resource.source.TransientSource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.utils.IOTools;

/**
 * This RPC client proxy uses {@link Proxy} and {@link InvocationHandler} to grant expressive access to services via a
 * {@link MBeanServer}
 * 
 * @author dirk.scheffler
 *
 */
public abstract class GmDmbRpcClientBase extends GmRpcClientBase {

	private static Logger logger = Logger.getLogger(GmDmbRpcClientBase.class);
	private static final String defaultObjectName = "com.braintribe.model.processing:type=GmDmbRpcServer";

	private Marshaller marshaller;
	private GmDmbRpcInvoker invoker;
	private String objectName;

	public static final String RPC_LOGSTEP_MBEAN_CALL = "MBean request";

	protected GmDmbRpcClientBase() {
	}

	protected GmDmbRpcClientBase(BasicGmDmbRpcClientConfig config) {
		setConfig(config);
	}

	protected void setConfig(BasicGmDmbRpcClientConfig config) {

		super.setConfig(config);

		this.objectName = config.getObjectName();
		if (this.objectName == null) {
			this.objectName = defaultObjectName;
		}

		try {
			this.invoker = DynamicMBeanProxy.create(GmDmbRpcInvoker.class, new ObjectName(this.objectName));
		} catch (MalformedObjectNameException e) {
			throw new IllegalArgumentException("The MBean object name from the given configuration is invalid: " + this.objectName, e);
		}
		this.marshaller = config.getMarshaller();
	}

	public void close() {
		this.stopProcessing = true;
	}

	@Override
	protected ServiceResult sendRequest(GmRpcClientRequestContext requestContext) {
		try {

			ServiceRequest request = requestContext.getServiceRequest();

			Map<String, Callable<InputStream>> inputs = new HashMap<>();
			Map<String, Callable<OutputStream>> outputs = new HashMap<>();

			Map<String, String> requestMetaData = new HashMap<String, String>();

			MarshallerCodec<ServiceRequest> marshallerCodec = new MarshallerCodec<ServiceRequest>(marshaller, ServiceRequest.class);
			try {
				requestContext.summaryLogger().startTimer(RpcConstants.RPC_LOGSTEP_MARSHALL_REQUEST);
				byte[] encodedRequest = marshallerCodec.encode(request);
				inputs.put(DmbConstants.RPC_MAPKEY_REQUEST, new ByteArrayInputStreamProviderCallable(encodedRequest));
			} finally {
				requestContext.summaryLogger().stopTimer(RpcConstants.RPC_LOGSTEP_MARSHALL_REQUEST);
			}

			for (TransientSource transientSource : marshallerCodec.getTransientSources()) {
				inputs.put(transientSource.getGlobalId(), transientSource::openStream);
			}

			// transfer CallStreamCaptures with adapter to OutputStream Callables (providers)
			for (CallStreamCapture callStreamCapture : marshallerCodec.getCallStreamCaptures()) {
				outputs.put(callStreamCapture.getGlobalId(), new CallStreamCaptureOutputStreamProviderCallable(callStreamCapture));
			}

			Map<String, Callable<InputStream>> results;

			ResultsConsumer resultsConsumer = new ResultsConsumer(requestContext);

			try {
				requestContext.summaryLogger().startTimer(RPC_LOGSTEP_MBEAN_CALL);
				results = invoker.call(requestMetaData, inputs, outputs, resultsConsumer);
			} catch (UndeclaredThrowableException | RuntimeMBeanException e) {
				Throwable remoteCause = e.getCause();
				if (remoteCause == null) {
					throw e;
				}
				if (remoteCause.getClass().getClassLoader() == this.getClass().getClassLoader()) {
					throw remoteCause;
				}
				Throwable localCause = ExceptionBuilder.createException(remoteCause.getClass().getName(), remoteCause.getMessage(), remoteCause);
				throw localCause;
			} finally {
				requestContext.summaryLogger().stopTimer(RPC_LOGSTEP_MBEAN_CALL);
			}

			ServiceResult serviceResult = resultsConsumer.processResultsIfNeeded(results);

			return serviceResult;

		} catch (AuthorizationException | SecurityServiceException e) {
			throw e;
		} catch (Throwable e) {
			throw new GmRpcException("Error while sending request: " + e.getMessage(), e);
		}
	}

	private class ResultsConsumer implements Consumer<Map<String, Callable<InputStream>>> {

		private GmRpcClientRequestContext requestContext;
		private ClassLoader contextClassLoader;

		private ResultsConsumer(GmRpcClientRequestContext requestContext) {
			this.requestContext = requestContext;
			contextClassLoader = Thread.currentThread().getContextClassLoader();
		}

		private ServiceResult serviceResult;

		@Override
		public void accept(Map<String, Callable<InputStream>> results) {

			Thread currentThread = Thread.currentThread();
			ClassLoader classLoaderBackup = currentThread.getContextClassLoader();

			// inject the original classloader of the thread that initialized this client request
			currentThread.setContextClassLoader(contextClassLoader);

			try {
				try {
					serviceResult = processResults(requestContext, results);
				} catch (Exception e) {
					throw new GmRpcException("Error while processing the results: " + e.getMessage(), e);
				}
			} finally {
				// reset the backupped classloader of the thread that was set before entering this method
				currentThread.setContextClassLoader(classLoaderBackup);
			}

		}

		public ServiceResult processResultsIfNeeded(Map<String, Callable<InputStream>> results) throws Exception {

			if (serviceResult != null) {
				// eagerly response succeeded, late results are ignored.
				return serviceResult;
			}

			serviceResult = processResults(requestContext, results);

			return serviceResult;

		}

	}

	private ServiceResult processResults(GmRpcClientRequestContext requestContext, Map<String, Callable<InputStream>> results) throws Exception {
		ServiceResult response = null;

		Map<String, InputStreamProvider> streamProviders = new HashMap<>();

		List<TransientSource> transientSources = new ArrayList<>();

		for (Map.Entry<String, Callable<InputStream>> entry : results.entrySet()) {
			String partName = entry.getKey();
			Callable<InputStream> inputStreamProvidingCallable = entry.getValue();

			if (DmbConstants.RPC_MAPKEY_RESPONSE.equals(partName)) {

				InputStream in = inputStreamProvidingCallable.call();

				try {

					requestContext.summaryLogger().startTimer(RpcConstants.RPC_LOGSTEP_UNMARSHALL_RESPONSE);

					response = unmarshallRpcResponse(in, marshaller, transientSources);

				} finally {
					IOTools.closeCloseable(in, "response input stream", requestContext.getClientLogger());
					requestContext.summaryLogger().stopTimer(RpcConstants.RPC_LOGSTEP_UNMARSHALL_RESPONSE);
				}

			} else {
				InputStreamProvider inputStreamProvider = new CallableInputStreamProvider(inputStreamProvidingCallable);
				streamProviders.put(partName, inputStreamProvider);
			}
		}

		for (TransientSource transientSource : transientSources) {
			transientSource.setInputStreamProvider(streamProviders.get(transientSource.getGlobalId()));
		}

		if (response != null) {
			ResponseEnvelope responseEnvelope = response.asResponse();
			if (responseEnvelope != null) {
				requestContext.notifyResponse(responseEnvelope.getResult());
			}
		}

		return response;

	}

	protected void logClientConfiguration(Logger callerLogger, boolean basic) {
		Logger log = (callerLogger == null) ? logger : callerLogger;

		if (log.isDebugEnabled()) {
			String nl = System.lineSeparator();
			StringBuilder sb = new StringBuilder();
			sb.append("Configured ").append(this.toString()).append(nl);
			if (!basic) {
				sb.append("\tService ID:          ").append(serviceId).append(nl);
				sb.append("\tService Interface:   ").append(serviceInterface).append(nl);
			}
			sb.append("\tClient Instance:     ").append(clientInstanceId).append(nl);
			sb.append("\tObject Name:         ").append(objectName).append(nl);
			sb.append("\tMarshaller:          ").append(marshaller).append(nl);
			sb.append("\tMeta Data Provider:  ").append(getMetaDataProvider()).append(nl);
			sb.append("\tFailure Codec:       ").append(getFailureCodec()).append(nl);
			sb.append("\tAuthorization Ctx:   ").append(getAuthorizationContext()).append(nl);
			log.debug(sb.toString());
		}
	}

	protected String getObjectName() {
		return objectName;
	}

	private static class CallStreamCaptureOutputStreamProviderCallable implements Callable<OutputStream> {
		private CallStreamCapture callStreamCapture;

		public CallStreamCaptureOutputStreamProviderCallable(CallStreamCapture callStreamCapture) {
			super();
			this.callStreamCapture = callStreamCapture;
		}

		@Override
		public OutputStream call() throws Exception {
			return callStreamCapture.openStream();
		}
	}

}
