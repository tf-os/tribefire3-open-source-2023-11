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
package tribefire.platform.impl.multicast;

import static com.braintribe.model.processing.bootstrapping.TribefireRuntime.ENVIRONMENT_MULTICAST_PROCESSING_TIMEOUT;
import static com.braintribe.model.processing.bootstrapping.TribefireRuntime.ENVIRONMENT_MULTICAST_PROCESSING_WARNINGTHRESHOLD;
import static com.braintribe.model.processing.bootstrapping.TribefireRuntime.getProperty;
import static java.lang.Long.parseLong;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.DestructionAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Topic;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceProcessorException;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.common.FailureCodec;
import com.braintribe.model.processing.service.common.ServiceResults;
import com.braintribe.model.service.api.ExecuteAuthorized;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.MulticastResponse;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.model.service.api.result.ServiceResultType;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageListener;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessageProperties;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;
import com.braintribe.transport.messaging.api.MessagingSessionProvider;
import com.braintribe.utils.lcd.LazyInitialized;

import tribefire.platform.impl.topology.CartridgeLiveInstances;

/**
 * <p>
 * Processes {@link MulticastRequest} instances, broadcasting them to multiple consumers.
 * 
 */
public class MulticastProcessor implements ServiceProcessor<MulticastRequest, MulticastResponse>, MessageListener, DestructionAware {

	// constants
	private static final Logger log = Logger.getLogger(MulticastProcessor.class);

	// configurable
	private MessagingSessionProvider messagingSessionProvider;
	private String requestTopicName;
	private String responseTopicName;
	private InstanceId senderId;
	private long defaultResponseTimeout = parseLong(getProperty(ENVIRONMENT_MULTICAST_PROCESSING_TIMEOUT, "30000"));
	private long warningResponseTimeout = parseLong(getProperty(ENVIRONMENT_MULTICAST_PROCESSING_WARNINGTHRESHOLD, "120000"));
	private CartridgeLiveInstances liveInstances;
	private Supplier<Map<String, Object>> metaDataProvider;
	private final Map<String, BlockingQueue<Message>> responsesMap = new ConcurrentHashMap<>();
	private boolean localCallOptimizationEnabled = true;

	// lazy initialized
	private final LazyInitialized<MulticastMsg> msg = new LazyInitialized<>(MulticastMsg::new);

	private class MulticastMsg implements AutoCloseable {
		// post initialized
		public final MessagingSession messagingSession;
		public final MessageProducer requestProducer;
		public final MessageConsumer responseConsumer;
		public final Topic requestTopic;
		public final Topic responseTopic;

		public MulticastMsg() {
			try {
				messagingSession = messagingSessionProvider.provideMessagingSession();
				requestTopic = messagingSession.createTopic(requestTopicName);
				responseTopic = messagingSession.createTopic(responseTopicName);
				requestProducer = messagingSession.createMessageProducer(requestTopic);
				responseConsumer = messagingSession.createMessageConsumer(responseTopic);
				responseConsumer.setMessageListener(MulticastProcessor.this);
			} catch (Exception e) {
				throw new RuntimeException("Could not initialize the MulticastProcessor (request topic name: " + requestTopicName
						+ ", response topic name:" + responseTopicName + ")", e);
			}
		}

		@Override
		public void close() throws Exception {
			try {
				messagingSession.close();
			} catch (MessagingException e) {
				log.error("Failed to close the messaging session", e);
			}
		}
	}

	@Required
	@Configurable
	public void setMessagingSessionProvider(MessagingSessionProvider messagingSessionProvider) {
		this.messagingSessionProvider = messagingSessionProvider;
	}

	@Required
	@Configurable
	public void setRequestTopicName(String requestTopicName) {
		this.requestTopicName = requestTopicName;
	}

	@Required
	@Configurable
	public void setResponseTopicName(String responseTopicName) {
		this.responseTopicName = responseTopicName;
	}

	@Required
	@Configurable
	public void setSenderId(InstanceId senderId) {
		this.senderId = senderId;
	}

	@Configurable
	public void setDefaultResponseTimeout(long defaultResponseTimeout) {
		this.defaultResponseTimeout = defaultResponseTimeout; // In milliseconds
	}

	@Configurable
	public void setWarningResponseTimeout(long warningResponseTimeout) {
		this.warningResponseTimeout = warningResponseTimeout; // In milliseconds
	}

	@Configurable
	public void setLocalCallOptimizationEnabled(boolean localCallOptimizationEnabled) {
		this.localCallOptimizationEnabled = localCallOptimizationEnabled;
	}

	@Required
	@Configurable
	public void setLiveInstances(CartridgeLiveInstances liveInstances) {
		this.liveInstances = liveInstances;
	}

	@Configurable
	@Required
	public void setMetaDataProvider(Supplier<Map<String, Object>> metaDataProvider) {
		this.metaDataProvider = metaDataProvider;
	}

	private MulticastMsg msg() {
		return msg.get();
	}

	@Override
	public void preDestroy() {
		msg.close();
	}

	@Override
	public MulticastResponse process(ServiceRequestContext requestContext, MulticastRequest request) {

		Objects.requireNonNull(request, "multicast request must not be null");
		Objects.requireNonNull(request.getServiceRequest(), "multicast request has no service request set");

		boolean async = request.getAsynchronous();

		Message requestMessage = createRequestMessage(request, async);

		InstanceId addressee = request.getAddressee();

		String correlationId = requestMessage.getCorrelationId();

		Set<String> expectedInstances = this.liveInstances.liveInstances(addressee);

		boolean trace = log.isTraceEnabled();

		try {
			Message responseMessage = null;
			BlockingQueue<Message> responseHolder = null;
			long start = 0;
			long timeout = 0;
			final Long userTimeout = request.getTimeout();
			final long initialTimeout = userTimeout != null ? userTimeout : defaultResponseTimeout;

			if (!async) {

				timeout = initialTimeout;

				if (timeout > warningResponseTimeout && log.isDebugEnabled()) {
					log.debug("The request [" + correlationId + "] might block for " + timeout + " milliseconds: " + request);
				}

				responseHolder = new LinkedBlockingQueue<>();

				responsesMap.put(correlationId, responseHolder);

				start = System.currentTimeMillis();
			}

			// detect local call
			boolean callLocally = (localCallOptimizationEnabled && expectedInstances.size() == 1
					&& expectedInstances.iterator().next().equals(senderId.stringify()));

			if (callLocally) {
				return processLocally(requestContext, request);
			}

			msg().requestProducer.sendMessage(requestMessage);

			log.trace(() -> "Application [" + senderId + "] sent message [" + correlationId + "]"
					+ (addressee != null ? " addressed to [" + addressee + "]" : "") + " for broadcasting: " + request.getServiceRequest());

			if (async) {
				return null;
			}

			MulticastResponse response = MulticastResponse.T.create();

			if (expectedInstances != null && expectedInstances.isEmpty()) {
				// Tracking of expected instances is enabled, but returned an empty list
				if (trace) {
					log.trace("Application [" + senderId + "] sent message [" + correlationId + "]"
							+ (addressee != null ? " addressed to [" + addressee + "]" : "")
							+ " won't wait for responses as there are no matching expected instances");
				}
				return response;
			}

			if (trace) {
				log.trace("Application [" + senderId + "] sent message [" + correlationId + "]"
						+ (addressee != null ? " addressed to [" + addressee + "]" : "") + " and will expect responses from: " + expectedInstances);
			}

			while ((responseMessage = responseHolder.poll(timeout, TimeUnit.MILLISECONDS)) != null) {

				long now = System.currentTimeMillis();
				timeout -= now - start;
				start = now;

				if (timeout < 0) {
					timeout = 0;
				}

				if (correlationId.equals(responseMessage.getCorrelationId())) {

					InstanceId origin = requireOrigin(responseMessage);

					ServiceResult result = requireServiceResult(responseMessage);

					if (result != null) {

						if (result.resultType() == ServiceResultType.stillProcessing) {

							if (userTimeout == null) {
								// A user-provided timeout is not extended on the basis of received still-processing
								// signals
								timeout = initialTimeout;
							}

							log.debug(() -> "Application [" + senderId + "] received a still-processing signal for message [" + correlationId
									+ "] from [" + origin + "]: " + result);

							continue;

						} else {

							log.trace(() -> "Application [" + senderId + "] received a normal response for message [" + correlationId + "] from ["
									+ origin + "]: " + result);

							response.getResponses().put(origin, result);

						}

					} else {
						log.debug(() -> "Application [" + senderId + "] received an ignore-signal for message [" + correlationId + "] from [" + origin
								+ "]");
					}

					String originTag = instanceTag(origin);

					expectedInstances.remove(originTag);

					if (expectedInstances.isEmpty()) {
						log.trace(() -> "Application [" + senderId + "] received response for [" + correlationId + "] from [" + originTag
								+ "] and will no longer wait for responses");
						break;
					} else if (trace) {
						log.trace("Application [" + senderId + "] received response for [" + correlationId + "] from [" + originTag
								+ "] and will expect responses from: " + expectedInstances);
					}

				}

				responseMessage = null;

				if (timeout == 0) {
					break;
				}

			}

			for (String timedoutInstance : expectedInstances) {
				response.getResponses().put(instanceId(timedoutInstance), timeoutFailure(correlationId, timedoutInstance));
			}

			if (trace) {
				traceRequest(correlationId, request, response, start);
			}

			return response;

		} catch (Throwable e) {

			throw new ServiceProcessorException(
					"Failed to process request [" + correlationId + "]" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);

		} finally {
			if (!async) {
				try {
					responsesMap.remove(correlationId);
				} catch (Exception e) {
					log.error("Failed to remove local registry for request  [" + correlationId + "]", e);
				}
			}
		}

	}

	private MulticastResponse processLocally(ServiceRequestContext requestContext, MulticastRequest request) {
		ServiceRequest payloadRequest = request.getServiceRequest();
		if (request.getAsynchronous()) {
			payloadRequest.eval(requestContext).getAttribute(null);
			return null;
		} else {
			ServiceResult serviceResult = null;
			try {
				serviceResult = ServiceResults.envelope(payloadRequest.eval(requestContext).get());
			} catch (Exception e) {
				serviceResult = FailureCodec.INSTANCE.encode(e);
			}

			MulticastResponse response = MulticastResponse.T.create();

			response.getResponses().put(senderId, serviceResult);
			return response;
		}
	}

	protected Message createRequestMessage(MulticastRequest request, boolean async) {
		MulticastMsg msg = msg();

		// We are maybe overwriting a session Id that has been set earlier
		// TODO: Disk and Andre will review meta-data handling anyway
		ServiceRequest serviceRequest = request.getServiceRequest();
		enrichWithMetaData(serviceRequest);

		request.setSender(senderId);

		String correlationId = UUID.randomUUID().toString();

		Message requestMessage;
		try {
			requestMessage = msg.messagingSession.createMessage();
		} catch (MessagingException e) {
			throw new IllegalStateException("Unable to create a message" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
		}

		requestMessage.setCorrelationId(correlationId);
		requestMessage.setDestination(msg.requestTopic);
		if (!async) {
			requestMessage.setReplyTo(msg.responseTopic);
		}

		InstanceId addressee = request.getAddressee();
		if (addressee != null) {
			String addreseeAppId = addressee.getApplicationId();
			String addreseeNodeId = addressee.getNodeId();
			if (addreseeAppId != null || addreseeNodeId != null) {
				Map<String, Object> properties = requestMessage.getProperties();
				if (addreseeAppId != null) {
					properties.put(MessageProperties.addreseeAppId.getName(), addreseeAppId);
				}
				if (addreseeNodeId != null) {
					properties.put(MessageProperties.addreseeNodeId.getName(), addreseeNodeId);
				}
			}
		}

		if (senderId != null) {
			Map<String, Object> properties = requestMessage.getProperties();
			properties.put("sender", senderId.stringify());
		}

		String sessionId = request.getSessionId();
		if (sessionId != null) {
			ExecuteAuthorized ea = ExecuteAuthorized.T.create();
			ea.setServiceRequest(serviceRequest);
			ea.setSessionId(sessionId);
			requestMessage.setBody(ea);
		} else {
			requestMessage.setBody(serviceRequest);
		}

		if (log.isTraceEnabled()) {
			log.trace("Created message [" + correlationId + "] from [" + request.getSender() + "] for broadcasting: " + request.getServiceRequest());
		}

		return requestMessage;

	}

	@Override
	public void onMessage(Message responseMessage) throws MessagingException {

		String correlationId = responseMessage.getCorrelationId();

		if (correlationId == null) {
			log.warn(() -> "Cannot process response for request [" + correlationId + "] as message has no correlation id");
			return;
		}

		try {
			BlockingQueue<Message> blockingQueue = responsesMap.get(correlationId);
			if (blockingQueue == null) {
				log.trace(() -> "Ignoring response for request [" + correlationId + "]. The instance [" + senderId
						+ "] is no longer waiting for responses for this request. Response was: " + responseMessage.getBody());
				return;
			}

			blockingQueue.add(responseMessage);

			log.trace(() -> "Registered response for request [" + correlationId + "]: " + responseMessage);

		} catch (Exception e) {
			log.error("Failed to register response to request  [" + correlationId + "]", e);
		}

	}

	protected ServiceResult requireServiceResult(Message message) {
		ServiceResult response;
		try {
			response = (ServiceResult) message.getBody();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to get the body of the incoming message: " + message, e);
		}
		return response;
	}

	protected InstanceId requireOrigin(Message message) {

		Map<String, Object> properties = message.getProperties();

		String applicationId = requireStringProperty(properties, MessageProperties.producerAppId.getName());
		String nodeId = requireStringProperty(properties, MessageProperties.producerNodeId.getName());

		InstanceId origin = InstanceId.T.create();
		origin.setApplicationId(applicationId);
		origin.setNodeId(nodeId);

		return origin;

	}

	static String requireStringProperty(Map<String, Object> properties, String propertyName) {
		Object value = properties.get(propertyName);
		String strValue;
		if (value == null || (strValue = value.toString().trim()).isEmpty()) {
			throw new IllegalStateException("Response message to multicast request is missing the [ " + propertyName + " ] property.");
		}
		return strValue;
	}

	static String instanceTag(InstanceId instanceId) {
		if (instanceId == null) {
			return "null";
		}
		return instanceId.getApplicationId() + "@" + instanceId.getNodeId();
	}

	static InstanceId instanceId(String instanceTag) {
		if (instanceTag == null) {
			return null;
		}
		int atp = instanceTag.indexOf('@');
		boolean vtp = atp != -1 && atp < instanceTag.length();
		InstanceId instanceId = InstanceId.T.create();
		instanceId.setApplicationId(vtp ? instanceTag.substring(0, atp) : instanceTag);
		instanceId.setNodeId(vtp ? instanceTag.substring(atp + 1) : instanceTag);
		return instanceId;
	}

	static Failure timeoutFailure(String correlationId, String instanceTag) {
		Failure failure = Failure.T.create();
		failure.setType(TimeoutException.class.getName());
		failure.setMessage("No response for message " + correlationId + " was returned by " + instanceTag);
		return failure;
	}

	private void traceRequest(String correlationId, MulticastRequest request, MulticastResponse response, long start) {

		long time = System.currentTimeMillis() - start;
		char t = '\t';
		char n = '\n';
		int results = response.getResponses().size();

		StringBuilder sb = new StringBuilder();
		sb.append("Request [").append(correlationId).append("] was successfully multicasted from ").append(senderId).append(n);
		sb.append(t).append(request.getServiceRequest()).append(n);
		if (results > 0) {
			sb.append(results).append(" result(s) collected in ").append(time).append(" ms:");
			for (Entry<InstanceId, ServiceResult> entry : response.getResponses().entrySet()) {
				sb.append(n).append(t).append(entry.getKey()).append(": ").append(entry.getValue());
			}
		} else {
			sb.append("No result collected in ").append(time).append(" ms");
		}

		log.trace(sb.toString());

	}

	protected void enrichWithMetaData(ServiceRequest request) {
		Map<String, Object> metaData = metaDataProvider.get();
		request.setMetaData(metaData);
	}

}
