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
package com.braintribe.model.processing.mqrpc.server;

import static com.braintribe.transport.messaging.api.MessageProperties.addreseeAppId;
import static com.braintribe.transport.messaging.api.MessageProperties.addreseeNodeId;
import static com.braintribe.transport.messaging.api.MessageProperties.producerAppId;
import static com.braintribe.transport.messaging.api.MessageProperties.producerNodeId;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.common.attribute.AttributeContextBuilder;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.logging.ThreadRenamer;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.EvalException;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Queue;
import com.braintribe.model.messaging.Topic;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.rpc.commons.api.RpcConstants;
import com.braintribe.model.processing.rpc.commons.api.RpcHeaders;
import com.braintribe.model.processing.rpc.commons.impl.logging.RpcServiceRequestSummaryLogger;
import com.braintribe.model.processing.service.api.LocalOnlyAspect;
import com.braintribe.model.processing.service.api.ParentAttributeContextAspect;
import com.braintribe.model.processing.service.api.ResponseConsumerAspect;
import com.braintribe.model.processing.service.api.ServiceRequestSummaryLogger;
import com.braintribe.model.processing.service.api.aspect.EndpointExposureAspect;
import com.braintribe.model.processing.service.api.aspect.IsTrustedAspect;
import com.braintribe.model.processing.service.api.aspect.RequestedEndpointAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorAddressAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorIdAspect;
import com.braintribe.model.processing.service.api.aspect.SummaryLoggerAspect;
import com.braintribe.model.processing.service.common.ServiceResults;
import com.braintribe.model.processing.service.commons.NoOpServiceRequestSummaryLogger;
import com.braintribe.model.processing.worker.api.Worker;
import com.braintribe.model.processing.worker.api.WorkerContext;
import com.braintribe.model.processing.worker.api.WorkerException;
import com.braintribe.model.resource.CallStreamCapture;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.model.service.api.result.StillProcessing;
import com.braintribe.model.service.api.result.Unsatisfied;
import com.braintribe.provider.Holder;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageListener;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;
import com.braintribe.transport.messaging.api.MessagingSessionProvider;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.utils.lcd.LazyInitialized;

/**
 * Message queue driven GM RPC server.
 * <p>
 * As a {@link MessageListener}, this component evaluates {@link ServiceRequest} instances contained in the body of the
 * consumed {@link Message}(s).
 * <p>
 * MQ-driven GM RPC is very limited in comparison to the HTTP-driven counterpart. The transport of binary data through
 * {@link Resource}(s) and {@link CallStreamCapture}(s) is not supported.
 * <p>
 * Multiple instances of this class can be created per application, in such cases, it is recommended to configure the
 * different instances with distinct request destinations.
 * 
 */
public class GmMqRpcServer implements MessageListener, LifecycleAware, Worker {

	// constants
	private static final Logger log = Logger.getLogger(GmMqRpcServer.class);

	// configurable
	private Evaluator<ServiceRequest> requestEvaluator;
	private MessagingSessionProvider messagingSessionProvider;
	private String requestDestinationName;
	private EntityType<? extends Destination> requestDestinationType;
	private InstanceId consumerId;
	private ExecutorService executor;
	private Function<Message, ServiceRequest> requestExtractor = this::extractRequest;
	private ThreadRenamer threadRenamer = ThreadRenamer.NO_OP;
	private boolean trusted;
	private long keepAliveInterval;

	// internals
	private String fromTag;
	private Map<String, Message> processingRequests;
	private DelayQueue<StillProcessingCheck> processingRequestsDelayQueue;
	private volatile boolean shutdown = false;
	private Future<?> stillProcessingTaskFuture;

	private Function<ServiceRequest, CmdResolver> metaDataResolverProvider;

	private String endpointExposure = null;

	// lazy initialized
	private final LazyInitialized<GmMqRpcServerMsg> msg = new LazyInitialized<>(GmMqRpcServerMsg::new);

	private class GmMqRpcServerMsg implements AutoCloseable {
		public final MessagingSession messagingSession;
		public final MessageProducer responseProducer;
		public final MessageConsumer requestConsumer;
		public final boolean enforceLocalOnly;
		public volatile boolean closed = false;
		private ActivityCounter activityCounter = new ActivityCounter();

		public GmMqRpcServerMsg() {
			messagingSession = messagingSessionProvider.provideMessagingSession();
			responseProducer = messagingSession.createMessageProducer();
			// To avoid delivering the request to proxies when the request is multicasted to a topic
			enforceLocalOnly = Topic.T.isAssignableFrom(requestDestinationType);

			requestConsumer = messagingSession.createMessageConsumer(requestDestination());
			requestConsumer.setMessageListener(GmMqRpcServer.this);
		}

		private Destination requestDestination() {
			if (Topic.T.isAssignableFrom(requestDestinationType))
				return messagingSession.createTopic(requestDestinationName);

			if (Queue.T.isAssignableFrom(requestDestinationType))
				return messagingSession.createQueue(requestDestinationName);

			throw new IllegalStateException("Destination type is not supported: " + requestDestinationType);
		}

		public void increaseUseCounter() {
			activityCounter.inc();
		}

		public void decreaseUseCounter() {
			activityCounter.dec();
		}

		@Override
		public void close() throws Exception {
			activityCounter.awaitZeroActivity(60_000);
			try {
				messagingSession.close();
				closed = true;
			} catch (MessagingException e) {
				log.error("Failed to close the messaging session", e);
			}
		}

		public boolean isClosed() {
			return closed;
		}
	}

	@Required
	@Configurable
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}

	@Required
	@Configurable
	public void setMessagingSessionProvider(MessagingSessionProvider messagingSessionProvider) {
		this.messagingSessionProvider = messagingSessionProvider;
	}

	@Required
	@Configurable
	public void setRequestDestinationName(String requestDestinationName) {
		this.requestDestinationName = requestDestinationName;
	}

	@Required
	@Configurable
	public void setRequestDestinationType(EntityType<? extends Destination> requestDestinationType) {
		this.requestDestinationType = requestDestinationType;
	}

	@Required
	@Configurable
	public void setConsumerId(InstanceId consumerId) {
		this.consumerId = consumerId;
	}

	@Required
	@Configurable
	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}

	@Configurable
	public void setThreadRenamer(ThreadRenamer threadRenamer) {
		this.threadRenamer = Objects.requireNonNull(threadRenamer, "threadRenamer must not be null");
	}

	@Configurable
	public void setTrusted(boolean trusted) {
		this.trusted = trusted;
	}

	@Configurable
	public void setKeepAliveInterval(long keepAliveInterval) {
		this.keepAliveInterval = keepAliveInterval;
	}

	// ################################################
	// ## . . . . . . . LifecycleAware . . . . . . . ##
	// ################################################

	@Override
	public void postConstruct() {
		if (keepAliveInterval > 0) {
			this.processingRequests = new ConcurrentHashMap<>();
			this.processingRequestsDelayQueue = new DelayQueue<>();
			this.stillProcessingTaskFuture = this.executor.submit(this::stillProcessingSignalTask);
		}

		fromTag = "from(" + requestDestinationName + "/" + consumerId + ")";
	}

	@Override
	public void preDestroy() {
		log.debug(() -> getClass().getSimpleName() + " from " + consumerId + " is getting closed.");

		shutdown = true;

		msg.close();

		if (stillProcessingTaskFuture != null)
			try {
				stillProcessingTaskFuture.cancel(true);
			} catch (Exception e) {
				log.error("Failed to cancel the keep alive task", e);
			}
	}

	// ################################################
	// ## . . . . . . . . . Worker . . . . . . . . . ##
	// ################################################

	@Override
	public void start(WorkerContext workerContext) throws WorkerException {
		start();
	}

	@Override
	public void stop(WorkerContext workerContext) throws WorkerException {
		stop();
	}

	public void start() {
		msg();
	}

	public void stop() {
		msg.close();
	}

	// ################################################
	// ## . . . . . . . MessageListener . . . . . . .##
	// ################################################

	@Override
	public void onMessage(Message requestMessage) throws MessagingException {
		msg().increaseUseCounter();
		GmMqRpcProcessor processor = new GmMqRpcProcessor(requestMessage);
		executor.submit(processor);
	}

	private class GmMqRpcProcessor implements Runnable {

		private final Message requestMessage;
		private final String correlationId;

		private GmMqRpcProcessor(Message requestMessage) {
			this.requestMessage = requestMessage;
			this.correlationId = requestMessage.getCorrelationId();
		}

		@Override
		public void run() {
			try {
				ServiceRequest request = requestExtractor.apply(requestMessage);
				AttributeContext attributeContext = initializeContext(requestMessage.getHeaders());
				ServiceRequestSummaryLogger summaryLogger = attributeContext.findAttribute(SummaryLoggerAspect.class)
						.orElse(NoOpServiceRequestSummaryLogger.INSTANCE);

				threadRenamer.push(() -> fromTag);

				boolean ignoreResponse = requestMessage.getReplyTo() == null;

				boolean sendKeepAlive = !ignoreResponse && keepAliveInterval > 0;
				try {
					if (sendKeepAlive)
						registerProcessingRequest();

					ServiceResult result = evaluate(attributeContext, request, ignoreResponse);

					if (!ignoreResponse) {
						Message responseMessage = createResponseMessage(requestMessage, result);
						sendResponse(responseMessage, requestMessage.getReplyTo());
					}

				} finally {
					if (sendKeepAlive)
						unregisterProcessingRequest();

					threadRenamer.pop();
					summaryLogger.log(this, request);
					summaryLogger.logOneLine(RpcConstants.RPC_LOGSTEP_FINAL, request);
				}

			} catch (Exception e) {
				log.error("Failed to process incoming message: " + requestMessage, e);
			} finally {
				msg().decreaseUseCounter();
			}
		}

		public void registerProcessingRequest() {
			log.trace(() -> "Registering request: " + correlationId);

			processingRequests.put(correlationId, requestMessage);

			try {
				StillProcessingCheck check = new StillProcessingCheck(correlationId);
				processingRequestsDelayQueue.offer(check);
			} catch (RuntimeException | Error e) {
				processingRequests.remove(correlationId);
				throw e;
			}

			log.trace(() -> "Registered request: " + correlationId);
		}

		public void unregisterProcessingRequest() {
			log.trace(() -> "Unregistering request: " + correlationId);
			processingRequests.remove(correlationId);
			log.trace(() -> "Unregistered request: " + correlationId);
		}

	}

	protected ServiceResult evaluate(AttributeContext attributeContext, ServiceRequest serviceRequest, boolean ignoreResponse) {
		try {
			try {
				EvalContext<Object> eval = requestEvaluator.eval(serviceRequest);
				eval.with(ParentAttributeContextAspect.class, attributeContext);

				Holder<Object> responseConsumer = null;

				if (!ignoreResponse) {
					responseConsumer = new Holder<>();
					eval.with(ResponseConsumerAspect.class, responseConsumer);
				}

				if (msg().enforceLocalOnly)
					eval.with(LocalOnlyAspect.class, true);

				Maybe<?> maybe = eval.getReasoned();

				if (maybe.isUnsatisfied()) {
					Unsatisfied unsatisfied = Unsatisfied.from(maybe);
					return unsatisfied;
				}

				Object value = maybe.get();

				if (ignoreResponse || (msg().enforceLocalOnly && LocalOnlyAspect.absentResult == value))
					return null;

				Object eagerValue = responseConsumer.get();

				ServiceResult serviceResult = ServiceResults.envelope(eagerValue == null ? value : eagerValue);
				return serviceResult;

			} catch (EvalException e) {
				throw e.getCause() != null ? e.getCause() : e;
			}

		} catch (Throwable e) {
			String msg = "Evaluation from [" + consumerId + "] failed for request " + serviceRequest + " threw an exception: " + e;
			log.debug(() -> msg);

			if (!ignoreResponse) {
				log.trace(() -> msg, e);
				return ServiceResults.encodeFailure(e);
			}

			log.info(() -> msg + ". This response won't be returned to the caller", e);
		}

		return null;
	}

	private Message createResponseMessage(Message requestMessage, ServiceResult result) throws MessagingException {
		Message responseMessage = msg().messagingSession.createMessage();

		responseMessage.setCorrelationId(requestMessage.getCorrelationId());
		responseMessage.setDestination(requestMessage.getReplyTo());
		responseMessage.setBody(result);

		Map<String, Object> requestProperties = requestMessage.getProperties();
		Map<String, Object> responseProperties = responseMessage.getProperties();

		responseProperties.put(producerAppId.getName(), consumerId.getApplicationId());
		responseProperties.put(producerNodeId.getName(), consumerId.getNodeId());

		Object responseAddreseeAppId = requestProperties.get(producerAppId.getName());
		Object responseAddreseeNodeId = requestProperties.get(producerNodeId.getName());

		if (responseAddreseeAppId != null)
			responseProperties.put(addreseeAppId.getName(), responseAddreseeAppId);

		if (responseAddreseeNodeId != null)
			responseProperties.put(addreseeNodeId.getName(), responseAddreseeNodeId);

		return responseMessage;
	}

	protected AttributeContext initializeContext(Map<String, Object> requestMetaData) {
		String requestedEndpoint = null;
		String requestorAddress = null;
		String requestorId = null;

		if (requestMetaData != null && !requestMetaData.isEmpty()) {
			requestedEndpoint = asString(requestMetaData.get(RpcConstants.RPC_MAPKEY_REQUESTED_ENDPOINT));
			requestorAddress = asString(requestMetaData.get(RpcConstants.RPC_MAPKEY_REQUESTED_ENDPOINT));
			requestorId = asString(requestMetaData.get(RpcHeaders.rpcClientId.getHeaderName()));
		}

		AttributeContext attributeContext = AttributeContexts.peek();

		//@formatter:off
		AttributeContextBuilder builder = attributeContext.derive()
				.set(RequestedEndpointAspect.class, requestedEndpoint)
				.set(RequestorAddressAspect.class, requestorAddress)
				.set(RequestorIdAspect.class, requestorId)
				.set(IsTrustedAspect.class, trusted);
		//@formatter:on

		if (endpointExposure != null) {
			builder.set(EndpointExposureAspect.class, endpointExposure);
		}
		AttributeContext derivedContext = builder.build();

		return derivedContext.derive()
				.set(SummaryLoggerAspect.class, RpcServiceRequestSummaryLogger.getInstance(log, derivedContext, metaDataResolverProvider)).build();
	}

	protected ServiceRequest extractRequest(Message message) {
		return (ServiceRequest) message.getBody();
	}

	private static String asString(Object object) {
		return object == null ? null : object.toString();
	}

	private void stillProcessingSignalTask() {
		try {
			threadRenamer.push(() -> fromTag + ".keepAliveTask");

			log.debug(() -> "Starting still processing signal task");

			while (!shutdown) {
				try {
					StillProcessingCheck check = processingRequestsDelayQueue.poll(1, TimeUnit.SECONDS);

					if (check != null && !shutdown) {
						// Here we check if an expired entry matches a request which is still being processed

						Message requestMessage = processingRequests.get(check.correlationId);
						if (requestMessage != null) {
							log.trace(() -> "Still processing signal will be sent for: " + requestMessage);

							// The expired entry is still processing, so we send a StillProcessing reply.
							try {
								Message responseMessage = createResponseMessage(requestMessage, StillProcessing.T.create());
								sendResponse(requestMessage, responseMessage.getReplyTo());

								log.debug(() -> "Sent still processing signal for: " + requestMessage);

							} catch (Throwable t) {
								log.error("Failed to send still processing signal: " + t, t);
							}

							// The entry needs to be re-enqueued to allow subsequent StillProcessing signals
							try {
								check.reset();
								processingRequestsDelayQueue.offer(check);

								log.trace(() -> "Re-enqueued entry for still processing request: " + requestMessage);

							} catch (Throwable t) {
								log.error("Failed to re-enqueue entry for still processing request: " + requestMessage, t);
							}

						} else {
							// The expired entry is no longer being processed and is just ignored.
							log.trace(() -> "Expired entry is no longer being processed: " + check.correlationId);
						}
					}
				} catch (InterruptedException e) {
					// Expected when shutting down.
				}
			}

			log.debug(() -> "Exiting still processing signal task");

		} finally {
			threadRenamer.pop();
		}
	}

	private void sendResponse(Message message, Destination destination) {
		MessageProducer responseProducer = msg().responseProducer;
		responseProducer.sendMessage(message, destination);
	}

	private GmMqRpcServerMsg msg() {
		return msg.get();
	}

	private class StillProcessingCheck implements Delayed {

		private final String correlationId;
		private long expiration;

		public StillProcessingCheck(String correlationId) {
			this.correlationId = Objects.requireNonNull(correlationId, "correlationId cannot be null");
			reset();
		}

		public void reset() {
			this.expiration = System.currentTimeMillis() + keepAliveInterval;
		}

		@Override
		public int compareTo(Delayed o) {
			if (o == null)
				return 1;

			if (o instanceof StillProcessingCheck)
				return Long.compare(this.expiration, ((StillProcessingCheck) o).expiration);

			return Integer.compare(this.hashCode(), o.hashCode());
		}

		@Override
		public long getDelay(TimeUnit unit) {
			long delay = expiration - System.currentTimeMillis();
			return Objects.requireNonNull(unit, "unit cannot be null").convert(delay, TimeUnit.MILLISECONDS);
		}

	}

	@Configurable
	public void setMetaDataResolverProvider(Function<ServiceRequest, CmdResolver> metaDataResolverProvider) {
		this.metaDataResolverProvider = metaDataResolverProvider;
	}
	@Configurable
	public void setEndpointExposure(String endpointExposure) {
		this.endpointExposure = endpointExposure;
	}

}
