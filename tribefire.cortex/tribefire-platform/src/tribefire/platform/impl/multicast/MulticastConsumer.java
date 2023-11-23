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

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.EvalException;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Topic;
import com.braintribe.model.processing.securityservice.commons.service.ContextualizedAuthorization;
import com.braintribe.model.processing.service.api.LocalOnlyAspect;
import com.braintribe.model.processing.service.common.ServiceResults;
import com.braintribe.model.service.api.AuthorizableRequest;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageListener;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessageProperties;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;
import com.braintribe.transport.messaging.api.MessagingSessionProvider;
import com.braintribe.utils.collection.impl.AttributeContexts;

/**
 * <p>
 * A {@link MessageListener} which evaluates {@link ServiceRequest} instances wrapped within {@link MulticastRequest}
 * instances received in the {@link Message} body.
 * 
 */
public class MulticastConsumer implements MessageListener, LifecycleAware {

	// constants
	private static final Logger log = Logger.getLogger(MulticastConsumer.class);

	// configurable
	protected ThreadPoolExecutor executor;
	protected Evaluator<ServiceRequest> requestEvaluator;
	protected MessagingSessionProvider messagingSessionProvider;
	protected String requestTopicName;
	protected InstanceId consumerId;

	// post initialized
	protected MessagingSession messagingSession;
	protected MessageConsumer requestConsumer;
	protected MessageProducer responseProducer;

	@Required
	@Configurable
	public void setExecutor(ThreadPoolExecutor executor) {
		this.executor = executor;
	}

	@Required
	@Configurable
	public void setRequestEvaluator(Evaluator<ServiceRequest> evaluator) {
		this.requestEvaluator = evaluator;
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
	public void setConsumerId(InstanceId consumerId) {
		this.consumerId = consumerId;
	}

	@Override
	public void postConstruct() {
		messagingSession = messagingSessionProvider.provideMessagingSession();
		responseProducer = messagingSession.createMessageProducer();
		Topic requestTopic = messagingSession.createTopic(this.requestTopicName);
		requestConsumer = messagingSession.createMessageConsumer(requestTopic);
		requestConsumer.setMessageListener(this);
	}

	@Override
	public void preDestroy() {
		if (messagingSession != null) {
			try {
				messagingSession.close();
			} catch (Exception e) {
				log.error("Failed to close the messaging session", e);
			}
		}
	}

	@Override
	public void onMessage(Message requestMessage) throws MessagingException {
		executor.submit(() -> processRequestMessage(requestMessage));
	}

	protected void processRequestMessage(Message requestMessage) {
		try {

			ServiceRequest serviceRequest = (ServiceRequest) requestMessage.getBody();

			String sessionId = transportSessionId(serviceRequest);

			boolean asynchronous = requestMessage.getReplyTo() == null;

			ServiceResult result = evaluate(serviceRequest, asynchronous, sessionId);

			if (!asynchronous) {

				Message responseMessage = createResponseMessage(requestMessage, result);

				responseProducer.sendMessage(responseMessage, requestMessage.getReplyTo());

			}

		} catch (Exception e) {
			log.error("Failed to process incoming message: " + requestMessage, e);
		}
	}

	protected ServiceResult evaluate(ServiceRequest serviceRequest, boolean asynchronous, String mcSessionId) {

		try {
			try {
				EvalContext<Object> eval = requestEvaluator.eval(serviceRequest);
				eval.with(LocalOnlyAspect.class, true);

				final Object value;
				if (mcSessionId != null) {
					AttributeContext attributeContext = AttributeContexts.peek();
					AttributeContext contextualizedAuthorization = new ContextualizedAuthorization<>(requestEvaluator, attributeContext, mcSessionId)
							.authorize(true);
					AttributeContexts.push(contextualizedAuthorization);
					try {
						value = eval.get();
					} finally {
						AttributeContexts.pop();
					}
				} else {
					value = eval.get();
				}

				if (!asynchronous) {
					if (LocalOnlyAspect.absentResult == value) {
						return null;
					} else {
						ServiceResult serviceResult = ServiceResults.envelope(value);
						return serviceResult;
					}
				}
			} catch (EvalException e) {
				throw e.getCause() != null ? e.getCause() : e;
			}
		} catch (Throwable e) {
			log.error("Evaluation from [" + consumerId + "] failed for request " + serviceRequest, e);
			if (!asynchronous) {
				Failure failure = ServiceResults.encodeFailure(e);
				return failure;
			}
		}

		return null;

	}

	protected String transportSessionId(ServiceRequest sr) {
		if (sr instanceof AuthorizableRequest) {
			AuthorizableRequest ar = (AuthorizableRequest) sr;
			return ar.getSessionId();
		}
		if (sr instanceof MulticastRequest) {
			MulticastRequest multicastRequest = (MulticastRequest) sr;
			String sessionId = multicastRequest.getSessionId();
			if (sessionId != null) {
				ServiceRequest serviceRequest = multicastRequest.getServiceRequest();
				if (serviceRequest.supportsAuthentication()) {
					AuthorizableRequest authorizableRequest = (AuthorizableRequest) serviceRequest;
					if (authorizableRequest.getSessionId() == null) {
						authorizableRequest.setSessionId(sessionId);
					}
				}
			}
			return sessionId;
		}
		return null;
	}

	protected Message createResponseMessage(Message requestMessage, ServiceResult result) throws MessagingException {

		Message message = messagingSession.createMessage();

		message.setCorrelationId(requestMessage.getCorrelationId());
		message.setDestination(requestMessage.getReplyTo());
		message.setBody(result);

		Map<String, Object> properties = message.getProperties();

		properties.put(MessageProperties.producerAppId.getName(), consumerId.getApplicationId());
		properties.put(MessageProperties.producerNodeId.getName(), consumerId.getNodeId());

		String stringifiedInstanceId = (String) requestMessage.getProperties().get("sender");
		if (stringifiedInstanceId != null) {
			InstanceId addressee = InstanceId.parse(stringifiedInstanceId);
			if (addressee != null) {
				String addreseeAppId = addressee.getApplicationId();
				String addreseeNodeId = addressee.getNodeId();
				if (addreseeAppId != null) {
					properties.put(MessageProperties.addreseeAppId.getName(), addreseeAppId);
				}
				if (addreseeNodeId != null) {
					properties.put(MessageProperties.addreseeNodeId.getName(), addreseeNodeId);
				}
			}
		}

		return message;

	}

}
