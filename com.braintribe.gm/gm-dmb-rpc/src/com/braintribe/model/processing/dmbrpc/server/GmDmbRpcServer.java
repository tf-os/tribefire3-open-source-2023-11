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
package com.braintribe.model.processing.dmbrpc.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.codec.Codec;
import com.braintribe.codec.marshaller.api.EntityVisitorOption;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.exception.AuthorizationException;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.logging.ThreadRenamer;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.processing.dmbrpc.api.GmDmbRpcInvoker;
import com.braintribe.model.processing.dmbrpc.common.ByteArrayInputStreamProviderCallable;
import com.braintribe.model.processing.dmbrpc.common.CallableInputStreamProvider;
import com.braintribe.model.processing.dmbrpc.common.CallableOutputStreamProvider;
import com.braintribe.model.processing.dmbrpc.common.DmbConstants;
import com.braintribe.model.processing.dmbrpc.common.MarshallerCodec;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.rpc.commons.api.GmRpcException;
import com.braintribe.model.processing.rpc.commons.api.RpcConstants;
import com.braintribe.model.processing.rpc.commons.api.RpcHeaders;
import com.braintribe.model.processing.rpc.commons.impl.RpcUnmarshallingStreamManagement;
import com.braintribe.model.processing.rpc.commons.impl.logging.RpcServiceRequestSummaryLogger;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceException;
import com.braintribe.model.processing.service.api.ParentAttributeContextAspect;
import com.braintribe.model.processing.service.api.ResponseConsumerAspect;
import com.braintribe.model.processing.service.api.ServiceRequestSummaryLogger;
import com.braintribe.model.processing.service.api.aspect.RequestedEndpointAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorAddressAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorIdAspect;
import com.braintribe.model.processing.service.api.aspect.SummaryLoggerAspect;
import com.braintribe.model.processing.service.common.FailureCodec;
import com.braintribe.model.processing.service.common.ServiceResults;
import com.braintribe.model.processing.service.commons.NoOpServiceRequestSummaryLogger;
import com.braintribe.model.resource.CallStreamCapture;
import com.braintribe.model.resource.source.TransientSource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.utils.stream.api.StreamPipeFactory;

public class GmDmbRpcServer extends StandardMBean implements GmDmbRpcInvoker, LifecycleAware {

	// configurable
	private String objectName = "com.braintribe.model.processing:type=GmDmbRpcServer";
	private Marshaller marshaller;
	private Codec<Throwable, Failure> failureCodec = FailureCodec.INSTANCE;
	private final String hostAddress = getHostAddress();
	private ThreadRenamer threadRenamer = ThreadRenamer.NO_OP;
	private StreamPipeFactory streamPipeFactory;

	private ClassLoader contextClassLoader;
	private Consumer<Set<String>> requiredTypesReceiver;
	private Evaluator<ServiceRequest> evaluator;
	private Function<ServiceRequest, CmdResolver> metaDataResolverProvider;

	private static final Logger logger = Logger.getLogger(GmDmbRpcServer.class);

	public GmDmbRpcServer() {
		super(GmDmbRpcInvoker.class, false);
	}

	@Configurable
	@Required
	public void setEvaluator(Evaluator<ServiceRequest> evaluator) {
		this.evaluator = evaluator;
	}

	@Configurable
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	@Configurable
	@Required
	public void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	@Configurable
	@Required
	public void setFailureCodec(Codec<Throwable, Failure> failureCodec) {
		Objects.requireNonNull(failureCodec, "failureCodec cannot be set to null");
		this.failureCodec = failureCodec;
	}

	/**
	 * @deprecated Cryptographic capabilities were removed from the RPC layer. This setter is now obsolete and will be
	 *             removed in future version.
	 */
	@SuppressWarnings("unused")
	@Deprecated
	@Configurable
	public void setCryptoContext(com.braintribe.model.processing.rpc.commons.api.crypto.RpcServerCryptoContext cryptoContext) {
		// no-op
	}

	@Configurable
	public void setThreadRenamer(ThreadRenamer threadRenamer) {
		Objects.requireNonNull(threadRenamer, "threadRenamer cannot be set to null");
		this.threadRenamer = threadRenamer;
	}

	@Override
	public void postConstruct() {
		Thread currentThread = Thread.currentThread();
		contextClassLoader = currentThread.getContextClassLoader();
		try {
			MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
			platformMBeanServer.registerMBean(this, new ObjectName(this.objectName));
			logger.info("registered " + getClass().getSimpleName() + " with the object name:" + objectName);
		} catch (Exception e) {
			throw new GmRpcException("error while registering " + getClass().getSimpleName() + " bean to platform MBeanServer.", e);
		}
	}

	@Override
	public void preDestroy() {
		MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
		try {
			platformMBeanServer.unregisterMBean(new ObjectName(this.objectName));
			logger.info("unregistered " + getClass().getSimpleName() + " with the object name:" + objectName);
		} catch (Exception e) {
			logger.error("error while unregistering " + getClass().getSimpleName() + " with the object name:" + objectName, e);
		}
	}

	@Override
	public Map<String, Callable<InputStream>> call(Map<String, String> requestMetaData, Map<String, Callable<InputStream>> inputs,
			Map<String, Callable<OutputStream>> outputs, Consumer<Map<String, Callable<InputStream>>> resultsConsumer) throws GmRpcException {

		Thread currentThread = Thread.currentThread();
		ClassLoader classLoaderBackup = currentThread.getContextClassLoader();

		// inject the original classloader of the thread that initialized this GmDmbRpcServer
		currentThread.setContextClassLoader(contextClassLoader);

		try {
			return callContextualized(requestMetaData, inputs, outputs, resultsConsumer);
		} finally {

			// reset the backupped classloader of the thread that was set before entering this method
			currentThread.setContextClassLoader(classLoaderBackup);
		}

	}

	protected Map<String, Callable<InputStream>> callContextualized(Map<String, String> requestMetaData, Map<String, Callable<InputStream>> inputs,
			Map<String, Callable<OutputStream>> outputs, Consumer<Map<String, Callable<InputStream>>> resultsConsumer) throws GmRpcException {

		ServiceRequest request = null;
		ServiceResult response = null;

		AttributeContext attributeContext = initializeContext(requestMetaData);

		ServiceRequestSummaryLogger summaryLogger = attributeContext.findAttribute(SummaryLoggerAspect.class)
				.orElse(NoOpServiceRequestSummaryLogger.INSTANCE);

		threadRenamer.push(() -> "from(dmb-rpc@" + hostAddress + ")");
		try {
			request = unmarshallRequest(summaryLogger, inputs, outputs);

			try {
				ResponseMarshaller responseMarshaller = new ResponseMarshaller(summaryLogger, resultsConsumer);

				EvalContext<?> evalContext = request.eval(evaluator).with(ParentAttributeContextAspect.class, attributeContext)
						.with(ResponseConsumerAspect.class, responseMarshaller::marshallEagerlyReturnedValue);

				response = processRequest(evalContext);

				if (responseMarshaller.results != null) {
					return responseMarshaller.results;
				}

			} catch (Throwable t) {
				response = prepareFailure(t);
			}

			Map<String, Callable<InputStream>> results = marshallResponse(summaryLogger, response);

			return results;

		} catch (GmRpcException | AuthorizationException | SecurityServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new GmRpcException("error while handling call: " + e.getMessage(), e);
		} finally {
			threadRenamer.pop();
			summaryLogger.log(this, request);
			summaryLogger.logOneLine(RpcConstants.RPC_LOGSTEP_FINAL, request);
		}

	}

	private class ResponseMarshaller {

		private boolean marshalled = false;

		private final Consumer<Map<String, Callable<InputStream>>> resultsConsumer;
		private final ServiceRequestSummaryLogger summaryLogger;

		private Map<String, Callable<InputStream>> results;

		private ResponseMarshaller(ServiceRequestSummaryLogger summaryLogger, Consumer<Map<String, Callable<InputStream>>> consumer) {
			super();
			this.summaryLogger = summaryLogger;
			this.resultsConsumer = consumer;
		}

		public void marshallEagerlyReturnedValue(Object returnValue) {

			if (marshalled) {
				logger.warn("Response already marshalled, value will be ignored: " + returnValue);
				return;
			}

			ResponseEnvelope response = ServiceResults.envelope(returnValue);

			Map<String, Callable<InputStream>> results;
			try {
				results = marshallResponse(summaryLogger, response);
			} catch (RuntimeException | Error e) {
				throw e;
			} catch (Exception e) {
				throw new GmRpcException(e.getMessage(), e);
			}

			resultsConsumer.accept(results);

			this.results = results;

			marshalled = true;

			if (logger.isTraceEnabled()) {
				logger.trace("Marshalled eagerly: " + response);
			}

		}

	}

	protected ServiceResult processRequest(EvalContext<?> evalContext) throws Throwable {
		try {

			Maybe<?> responsePayload = evalContext.getReasoned();

			ServiceResult response = ServiceResults.fromMaybe(responsePayload);
			return response;

		} catch (UndeclaredThrowableException e) {
			throw e.getCause() != null ? e.getCause() : e;
		}
	}

	protected ServiceRequest unmarshallRequest(ServiceRequestSummaryLogger summaryLogger, Map<String, Callable<InputStream>> inputs,
			Map<String, Callable<OutputStream>> outputs) throws Exception {

		ServiceRequest request = null;
		RpcUnmarshallingStreamManagement streamManagement = new RpcUnmarshallingStreamManagement("gm-dmb-server", streamPipeFactory);

		Callable<InputStream> requestStreamProvidingCallable = inputs.get(DmbConstants.RPC_MAPKEY_REQUEST);

		try {
			summaryLogger.startTimer(RpcConstants.RPC_LOGSTEP_UNMARSHALL_REQUEST);

			InputStream requestIn = requestStreamProvidingCallable.call();

			try {
				request = unmarshallRequest(requestIn, GmDeserializationOptions.deriveDefaults().setRequiredTypesReceiver(requiredTypesReceiver)
						.set(EntityVisitorOption.class, streamManagement.getMarshallingVisitor()).build());
			} finally {
				IOTools.closeCloseable(requestIn, DmbConstants.RPC_MAPKEY_REQUEST + " part input sream", logger);
			}

		} finally {
			summaryLogger.stopTimer(RpcConstants.RPC_LOGSTEP_UNMARSHALL_REQUEST);
		}

		for (TransientSource transientSource : streamManagement.getTransientSources()) {
			Callable<InputStream> streamProvidingCallable = inputs.get(transientSource.getGlobalId());

			if (streamProvidingCallable == null)
				throw new IllegalStateException("Unsatisfied TransientSource with globalId: " + transientSource.getGlobalId());

			InputStreamProvider streamProvider = new CallableInputStreamProvider(streamProvidingCallable);
			transientSource.setInputStreamProvider(streamProvider);
		}

		for (CallStreamCapture callStreamCapture : streamManagement.getCallStreamCaptures()) {
			Callable<OutputStream> streamProvidingCallable = outputs.get(callStreamCapture.getGlobalId());

			if (streamProvidingCallable == null)
				throw new IllegalStateException("Unsatisfied CallStreamCapture with globalId: " + callStreamCapture.getGlobalId());

			CallableOutputStreamProvider streamProvider = new CallableOutputStreamProvider(streamProvidingCallable);
			callStreamCapture.setOutputStreamProvider(streamProvider);
		}

		return request;
	}

	protected ServiceRequest unmarshallRequest(InputStream in, GmDeserializationOptions options) throws MarshallException {
		try {
			ServiceRequest request = (ServiceRequest) marshaller.unmarshall(in, options);

			// Some marshallers might return null for unknown entity types. (BTT-6981)
			// Here we explicitly provide the caller with this information.
			if (request == null) {
				throw new NullPointerException(marshaller + " unmarshall() call returned null");
			}

			return request;
		} catch (MarshallException e) {
			throw e;
		} catch (Exception e) {
			throw new MarshallException("Failed to unmarshall the request" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
		}
	}

	protected Map<String, Callable<InputStream>> marshallResponse(ServiceRequestSummaryLogger summaryLogger, ServiceResult response)
			throws Exception {

		Map<String, Callable<InputStream>> results = new HashMap<>();

		byte[] encodedResponse = null;

		try {
			summaryLogger.startTimer(RpcConstants.RPC_LOGSTEP_MARSHALL_RESPONSE);
			MarshallerCodec<ServiceResult> marshallerCodec = new MarshallerCodec<>(marshaller, ServiceResult.class);
			encodedResponse = marshallerCodec.encode(response);

			for (TransientSource transientSource : marshallerCodec.getTransientSources()) {
				results.put(transientSource.getGlobalId(), transientSource::openStream);
			}

		} finally {
			summaryLogger.stopTimer(RpcConstants.RPC_LOGSTEP_MARSHALL_RESPONSE);
		}

		results.put(DmbConstants.RPC_MAPKEY_RESPONSE, new ByteArrayInputStreamProviderCallable(encodedResponse));

		return results;
	}

	protected AttributeContext initializeContext(Map<String, String> requestMetaData) {

		AttributeContext attributeContext = AttributeContexts.peek();
		AttributeContext derivedContext = attributeContext.derive().set(RequestedEndpointAspect.class, "dmb-rpc")
				.set(RequestorAddressAspect.class, hostAddress)
				.set(RequestorIdAspect.class, requestMetaData != null ? requestMetaData.get(RpcHeaders.rpcClientId.getHeaderName()) : null).build();

		return derivedContext.derive()
				.set(SummaryLoggerAspect.class, RpcServiceRequestSummaryLogger.getInstance(logger, attributeContext, metaDataResolverProvider))
				.build();
	}

	private Failure prepareFailure(Throwable throwable) {

		if (throwable instanceof AuthorizationException) {
			logger.warn("Unauthorized RPC call" + (throwable.getMessage() != null ? ": " + throwable.getMessage() : ""));
		} else {
			logger.error("RPC processing failed" + (throwable.getMessage() != null ? ": " + throwable.getMessage() : ""), throwable);
		}

		try {
			return failureCodec.encode(throwable);
		} catch (Exception e) {
			logger.error("Failed to convert the exception [ " + throwable + " ] into a " + Failure.T.getTypeSignature(), e);
			Failure failure = Failure.T.create();
			failure.setType(throwable.getClass().getName());
			failure.setMessage(throwable.getMessage());
			return failure;
		}
	}

	private static String getHostAddress() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Failed to retrieve the Internet Protocol address of the local host, using 0:0:0:0:0:0:0:0", e);
			}
			return "0:0:0:0:0:0:0:0";
		}
	}

	public void setRequiredTypesReceiver(Consumer<Set<String>> requiredTypesReceiver) {
		this.requiredTypesReceiver = requiredTypesReceiver;
	}

	@Configurable
	public void setMetaDataResolverProvider(Function<ServiceRequest, CmdResolver> metaDataResolverProvider) {
		this.metaDataResolverProvider = metaDataResolverProvider;
	}

	@Configurable
	@Required
	public void setStreamPipeFactory(StreamPipeFactory streamPipeFactory) {
		this.streamPipeFactory = streamPipeFactory;
	}
}