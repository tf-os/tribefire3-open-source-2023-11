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
package com.braintribe.model.processing.mqrpc.client;

import static com.braintribe.transport.messaging.api.MessageProperties.producerAppId;
import static com.braintribe.transport.messaging.api.MessageProperties.producerNodeId;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Queue;
import com.braintribe.model.messaging.Topic;
import com.braintribe.model.processing.rpc.commons.api.GmRpcException;
import com.braintribe.model.processing.rpc.commons.impl.client.GmRpcClientBase;
import com.braintribe.model.processing.rpc.commons.impl.client.GmRpcClientRequestContext;
import com.braintribe.model.resource.CallStreamCapture;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageListener;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessageProperties;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;
import com.braintribe.transport.messaging.api.MessagingSessionProvider;

/**
 * <p>
 * Base for message queue driven GM RPC clients.
 * 
 * <p>
 * MQ-driven GM RPC is very limited in comparison to the HTTP-driven counterpart. The transport of binary data through
 * {@link Resource}(s) and {@link CallStreamCapture}(s) is not supported. It must be used only in cases where a
 * fan-out approach is required and for punctual requests for which the server must ensure the caller is a trusted peer
 * application.
 * 
 */
public class GmMqRpcClientBase extends GmRpcClientBase implements MessageListener, LifecycleAware {

	// constants
	private static Logger log = Logger.getLogger(GmMqRpcClientBase.class);

	// configurable
	private MessagingSessionProvider messagingSessionProvider;
	private String requestDestinationName;
	private EntityType<? extends Destination> requestDestinationType;
	private boolean ignoreResponses;
	private String responseTopicName;
	private long responseTimeout = 10000L;
	private int retries = 3;

	// post initialized
	private MessagingSession messagingSession;
	private MessageProducer requestProducer;
	private Destination requestDestination;
	private MessageConsumer responseConsumer;
	private Topic responseTopic;
	private Map<String, BlockingQueue<Message>> responsesMap;
	private Object initializationMonitor = new Object();
	private volatile Boolean initialized;

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

	@Configurable
	public void setIgnoreResponses(boolean ignoreResponses) {
		this.ignoreResponses = ignoreResponses;
	}

	@Configurable
	public void setResponseTopicName(String responseTopicName) {
		this.responseTopicName = responseTopicName;
	}

	@Configurable
	public void setResponseTimeout(long responseTimeout) {
		this.responseTimeout = responseTimeout;
	}

	@Configurable
	public void setRetries(int retries) {
		this.retries = retries;
	}

	@Required
	@Configurable
	public void setConfig(BasicGmMqRpcClientConfig config) {
		super.setConfig(config);
		setMessagingSessionProvider(config.getMessagingSessionProvider());
		setRequestDestinationName(config.getRequestDestinationName());
		setRequestDestinationType(config.getRequestDestinationType());
		setIgnoreResponses(config.isIgnoreResponses());
		setResponseTopicName(config.getResponseTopicName());
		setResponseTimeout(config.getResponseTimeout());
		setRetries(config.getRetries());
	}

	public void initialize() {

		if (stopProcessing) {
			throw new IllegalStateException("Client is closed");
		}
		
		if (initialized == null) {
			
			synchronized (initializationMonitor) {

				if (initialized == null) {

					if (stopProcessing) {
						throw new IllegalStateException("Client is closed");
					}
					
					responsesMap = new ConcurrentHashMap<>();

					messagingSession = messagingSessionProvider.provideMessagingSession();

					if (Topic.T.isAssignableFrom(requestDestinationType)) {
						requestDestination = messagingSession.createTopic(this.requestDestinationName);
					} else if (Queue.T.isAssignableFrom(requestDestinationType)) {
						requestDestination = messagingSession.createQueue(this.requestDestinationName);
					} else {
						throw new IllegalStateException("Destination type is not supported: " + requestDestinationType);
					}

					requestProducer = messagingSession.createMessageProducer(requestDestination);

					responseTopic = messagingSession.createTopic(responseTopicName);

					responseConsumer = messagingSession.createMessageConsumer(responseTopic);

					responseConsumer.setMessageListener(this);
					
					initialized  =  true;
					
				}
			}
			
		}

	}

	public void close() {

		this.stopProcessing = true;

		log.debug(() -> getClass().getSimpleName() + " from " + clientInstanceId + " is getting closed.");

		if (Boolean.TRUE.equals(initialized)) {
			synchronized (initializationMonitor) {
				if (Boolean.TRUE.equals(initialized)) {
					if (messagingSession != null) {
						try {
							messagingSession.close();
						} catch (Exception e) {
							log.error("Failed to close the messaging session", e);
						}
					}
				}
				initialized = false;
			}
		}

	}

	@Override
	public void postConstruct() {
		//initializing lazily
		//initialize();
	}

	@Override
	public void preDestroy() {
		close();
	}

	@Override
	protected ServiceResult sendRequest(GmRpcClientRequestContext requestContext) {

		initialize(); // ensure initialization on the first request

		ServiceRequest request = requestContext.getServiceRequest();

		Message requestMessage = createRequestMessage(request);

		String correlationId = requestMessage.getCorrelationId();

		try {
			Message responseMessage = null;
			BlockingQueue<Message> responseHolder = null;
			long start = 0;
			long timeout = 0;
			long retries = 0;

			if (!ignoreResponses) {

				timeout = this.responseTimeout;
				retries = this.retries;

				responseHolder = new LinkedBlockingQueue<>();

				responsesMap.put(correlationId, responseHolder);

				start = System.currentTimeMillis();
			}

			requestProducer.sendMessage(requestMessage);

			log.trace(() -> "Application [" + clientInstanceId + "] sent message [" + correlationId + "] with: " + request);

			if (ignoreResponses) {
				return null;
			}

			while (true) {

				try {
					responseMessage = responseHolder.poll(timeout, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					throw new GmRpcException("Client is closed", e);
				}

				if (responseMessage == null) {

					if (retries == 0) {
						throw new GmRpcException(
								"The request timed out and no response was received for the request [" + correlationId + "] to destination [" + requestDestinationName + "] from response topic ["+responseTopicName+"]: " + request);
					}

					if (log.isDebugEnabled()) {
						log.debug("Application [" + clientInstanceId + "] will retry the message [" + correlationId + "] as no response was received in "
								+ this.responseTimeout + " milliseconds. Remaining retries: " + retries);
					}

					requestProducer.sendMessage(requestMessage);

					log.trace(() -> "Application [" + clientInstanceId + "] re-sent message [" + correlationId + "] with: " + request);

					timeout = this.responseTimeout;
					retries--;

					continue;

				}

				timeout = timeout - (System.currentTimeMillis() - start);
				if (timeout < 0) {
					timeout = 0;
				}

				if (correlationId.equals(responseMessage.getCorrelationId())) {

					InstanceId origin = requireOrigin(responseMessage);

					ServiceResult result = requireServiceResult(responseMessage);

					if (result != null) {

						log.trace(() -> "Application [" + clientInstanceId + "] received a normal response for message [" + correlationId + "] from ["
								+ origin + "]: " + result);

						ResponseEnvelope responseEnvelope = result.asResponse();

						if (responseEnvelope != null) {
							requestContext.notifyResponse(responseEnvelope.getResult());
						}

						return result;

					} else {
						log.trace(() -> "Application [" + clientInstanceId + "] received an ignore-signal for message [" + correlationId + "] from ["
								+ origin + "]");
					}

				}

			}

		} finally {
			if (!ignoreResponses) {
				try {
					responsesMap.remove(correlationId);
				} catch (Exception e) {
					log.error("Failed to remove local registry for request  [" + correlationId + "]", e);
				}
			}
		}

	}

	@Override
	public void onMessage(Message responseMessage) throws MessagingException {

		String correlationId = responseMessage.getCorrelationId();

		if (correlationId == null) {
			log.warn(() -> "Cannot process response for request [" + correlationId + "] as message has no correlation id");
			return;
		}

		try {
			Map<String, BlockingQueue<Message>> responses = responsesMap;

			if (responses == null) {
				log.debug(() -> "Ignoring response for request [" + correlationId + "]. The client is closing.");
				return;
			}

			BlockingQueue<Message> blockingQueue = responses.get(correlationId);
			if (blockingQueue == null) {
				log.debug(() -> "Ignoring response for request [" + correlationId + "]. The instance [" + clientInstanceId
						+ "] is no longer waiting for responses for this request.");
				return;
			}

			blockingQueue.add(responseMessage);

			log.debug(() -> "Registered response for request [" + correlationId + "]: " + responseMessage);

		} catch (Exception e) {
			log.error("Failed to register response to request  [" + correlationId + "]", e);
		}

	}

	@Override
	protected Logger logger() {
		return log;
	}

	protected Message createRequestMessage(ServiceRequest request) {

		String correlationId = UUID.randomUUID().toString();

		Message requestMessage;
		try {
			requestMessage = messagingSession.createMessage();
		} catch (MessagingException e) {
			throw new IllegalStateException("Unable to create a message" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
		}

		requestMessage.setCorrelationId(correlationId);
		requestMessage.setDestination(requestDestination);
		if (!ignoreResponses) {
			requestMessage.setReplyTo(responseTopic);
		}

		Map<String, Object> properties = requestMessage.getProperties();
		properties.put(producerAppId.getName(), clientInstanceId.getApplicationId());
		properties.put(producerNodeId.getName(), clientInstanceId.getNodeId());

		requestMessage.setBody(request);

		log.trace(() -> "Created message [" + correlationId + "] from [" + clientInstanceId + "] to deliver: " + request);

		return requestMessage;

	}

	protected void logClientConfiguration(Logger callerLogger, boolean basic) {
		Logger log = (callerLogger == null) ? GmMqRpcClientBase.log : callerLogger;

		if (log.isDebugEnabled()) {
			String nl = System.lineSeparator();
			StringBuilder sb = new StringBuilder();
			sb.append("Configured ").append(this.toString()).append(nl);
			if (!basic) {
				sb.append("\tService ID:          ").append(serviceId).append(nl);
				sb.append("\tService Interface:   ").append(serviceInterface).append(nl);
			}
			sb.append("\tClient Instance:     ").append(clientInstanceId).append(nl);
			sb.append("\tSession Provider:    ").append(messagingSessionProvider).append(nl);
			sb.append("\tRequest Destination: ").append(requestDestinationName).append(nl);
			sb.append("\tDestination Type:    ").append(requestDestinationType).append(nl);
			sb.append("\tResponse Topic:      ").append(responseTopicName).append(nl);
			sb.append("\tIgnore Responses:    ").append(ignoreResponses).append(nl);
			sb.append("\tRetries:      		  ").append(retries).append(nl);
			sb.append("\tCall Timeout:        ").append(responseTimeout).append(nl);
			sb.append("\tMeta Data Provider:  ").append(getMetaDataProvider()).append(nl);
			sb.append("\tFailure Codec:       ").append(getFailureCodec()).append(nl);
			sb.append("\tAuthorization Ctx:   ").append(getAuthorizationContext()).append(nl);
			log.debug(sb.toString());
		}
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
			throw new IllegalStateException("Response message is missing the [ " + propertyName + " ] property.");
		}
		return strValue;
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

}
