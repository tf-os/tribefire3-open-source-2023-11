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
package tribefire.extension.messaging.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.UnsatisfiedMaybeTunneling;
import com.braintribe.logging.Logger;
import com.braintribe.model.extensiondeployment.BinaryPersistence;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;

import tribefire.extension.messaging.connector.api.ProducerMessagingConnector;
import tribefire.extension.messaging.connector.kafka.KafkaProducerMessagingConnectorImpl;
import tribefire.extension.messaging.connector.pulsar.PulsarProducerMessagingConnectorImpl;
import tribefire.extension.messaging.model.Envelop;
import tribefire.extension.messaging.model.Message;
import tribefire.extension.messaging.model.deployment.event.EventEndpointConfiguration;
import tribefire.extension.messaging.model.service.MessagingRequest;
import tribefire.extension.messaging.model.service.MessagingResult;
import tribefire.extension.messaging.model.service.admin.FlushCacheMessagingResult;
import tribefire.extension.messaging.model.service.admin.FlushProducerConfigurationCache;
import tribefire.extension.messaging.model.service.demo.ProduceDemoMessage;
import tribefire.extension.messaging.model.service.demo.ProducedDemoMessage;
import tribefire.extension.messaging.model.service.produce.ProduceMessage;
import tribefire.extension.messaging.model.service.produce.ProduceMessageResult;
import tribefire.extension.messaging.service.cache.CortexCache;
import tribefire.extension.messaging.service.reason.validation.MandatoryNotSatisfied;

public class MessagingProcessor extends AbstractDispatchingServiceProcessor<MessagingRequest, MessagingResult> implements LifecycleAware {

	private static final Logger logger = Logger.getLogger(MessagingProcessor.class);

	private tribefire.extension.messaging.model.deployment.service.MessagingProcessor deployable;
	private PersistenceGmSessionFactory sessionFactory;

	private String externalId;
	private CortexCache<String, ProducerMessagingConnector> cache;

	private ClassLoader moduleClassLoader;

	// -----------------------------------------------------------------------
	// LIFECYCLE AWARE
	// -----------------------------------------------------------------------

	@Override
	public void preDestroy() {
		cache.preDestroy();
	}

	@Override
	public void postConstruct() {
		// nothing so far
	}

	// -----------------------------------------------------------------------
	// DISPATCHING
	// -----------------------------------------------------------------------

	@Override
	protected void configureDispatching(DispatchConfiguration<MessagingRequest, MessagingResult> dispatching) {
		dispatching.register(ProduceMessage.T, this::produceMessage);
		dispatching.register(FlushProducerConfigurationCache.T, this::flushProducers);

		dispatching.register(ProduceDemoMessage.T, this::produceDemoMessage);
	}

	// -----------------------------------------------------------------------
	// SERVICE METHODS
	// -----------------------------------------------------------------------

	public MessagingResult produceMessage(ServiceRequestContext requestContext, ProduceMessage request) {
		logger.info(() -> externalId + "Sending: '" + request + "'...");

		Envelop envelop = request.getEnvelop();
		List<EventEndpointConfiguration> destinations = requestContext.getAspect(MessagingDestination.class)
				.orElseThrow(() -> new IllegalStateException("Should not happen, destination should be provided!"));

		List<Message> messages = createMessages(envelop, new Date(), System.nanoTime());

		//@formatter:off
		ProduceMessageResult result = destinations.stream()
				.map(d -> deliverMessages(requestContext, messages, d))
				.reduce(this::enrichResults)
				.orElseGet(() -> ProduceMessageResult.create(Reason.create("Unexpected error occurred!")));//Can never happen
		//@formatter:on
		logger.info(() -> externalId + "Finished sending: '" + request + "' with result: '" + result + "'");

		return result;
	}

	public MessagingResult flushProducers(@SuppressWarnings("unused") ServiceRequestContext requestContext,
			@SuppressWarnings("unused") FlushProducerConfigurationCache request) {
		this.cache.getAll().forEach(ProducerMessagingConnector::destroy);
		this.cache.invalidateCache();
		logger.info("Processor caches invalidated!!!");
		return FlushCacheMessagingResult.T.create();
	}

	public ProducedDemoMessage produceDemoMessage(@SuppressWarnings("unused") ServiceRequestContext requestContext,
			@SuppressWarnings("unused") ProduceDemoMessage request) {

		logger.info(() -> "ProduceDemoMessage - to be intercepted by the framework");

		ProducedDemoMessage result = ProducedDemoMessage.T.create();
		return result;
	}

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

	private ProduceMessageResult enrichResults(ProduceMessageResult accumulator, ProduceMessageResult value) {
		if (value.getHadErrors()) {
			accumulator.setHadErrors(true);
			accumulator.getErrors().addAll(value.getErrors());
		}
		return accumulator;
	}

	private ProduceMessageResult deliverMessages(ServiceRequestContext requestContext, List<Message> messages,
			EventEndpointConfiguration destinationConfig) {
		String connectorId = destinationConfig.getEventEndpoint().getGlobalId();
		ProducerMessagingConnector connector = cache.get(connectorId);
		if (connector != null) {
			logger.info(() -> String.format("Connector %s is present in cache", connectorId));
		} else {
			logger.info(() -> String.format("Connector %s is absent! Registering one!", connectorId));
			connector = registerProducer(destinationConfig);
			cache.put(connector);
		}
		ProduceMessageResult res;
		try {
			messages.forEach(m -> m.setTopics(destinationConfig.getTopics()));
			res = connector.sendMessage(messages, requestContext, sessionFactory.newSession("cortex"));
		} catch (UnsatisfiedMaybeTunneling e) {
			res = ProduceMessageResult.create(e.getMaybe().whyUnsatisfied());
		}
		return res;
	}

	private ProducerMessagingConnector registerProducer(EventEndpointConfiguration destinationConfig) {
		BinaryPersistence resourceTarget = deployable.getResourceTarget();

		// TODO: this needs to be removed - only now for testing puroses
		String binaryPersistenceExternalId = null;
		if (resourceTarget != null) {
			binaryPersistenceExternalId = resourceTarget.getExternalId();
		}

		return switch (destinationConfig.getEventEndpoint().getConnectorType()) {
			case KAFKA -> new KafkaProducerMessagingConnectorImpl(destinationConfig, binaryPersistenceExternalId);
			case PULSAR -> new PulsarProducerMessagingConnectorImpl(destinationConfig, moduleClassLoader);
			default -> throw new UnsatisfiedMaybeTunneling(Reasons.build(MandatoryNotSatisfied.T)
					.text(String.format("Connector %s is not supported!", destinationConfig.getEventEndpoint().getConnectorType())).toMaybe());
		};
	}

	private List<Message> createMessages(Envelop envelop, Date timestamp, Long nanoTimestamp) {
		//@formatter:off
		return envelop.getMessages().stream()
				.map(m -> populateMessages(envelop, timestamp, nanoTimestamp, m))
				.toList();
		//@formatter:on
	}

	private Message populateMessages(Envelop envelop, Date timestamp, Long nanoTimestamp, Message m) {
		m.setTimestamp(actualValue(m.getTimestamp(), envelop.getTimestamp(), timestamp));
		m.setNanoTimestamp(actualValue(m.getNanoTimestamp(), envelop.getNanoTimestamp(), nanoTimestamp));
		m.setContext(actualValue(m.getContext(), envelop.getContext(), deployable.getContext()));

		validate(m.getValues().isEmpty(), "'" + Message.values + "' needs to be set");
		return m;
	}

	private void validate(boolean predicate, String errorMsg) {
		if (predicate) {
			throw new UnsatisfiedMaybeTunneling(Reasons.build(MandatoryNotSatisfied.T).text(errorMsg).toMaybe());
		}
	}

	private <T> T actualValue(T messageValue, T envelopValue, T connectorValue) {
		if (messageValue == null) {// an entity - no collection
			return envelopValue == null ? connectorValue : envelopValue;
		}

		if (messageValue instanceof Collection) {
			addIfNotNull(messageValue, envelopValue);
			addIfNotNull(messageValue, connectorValue);
		}

		return messageValue;
	}

	private <T> void addIfNotNull(T messageValue, T otherValue) {
		Optional.ofNullable(otherValue).ifPresent(v -> ((Collection<Object>) messageValue).addAll((Collection<Object>) v));
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Configurable
	@Required
	public void setDeployable(tribefire.extension.messaging.model.deployment.service.MessagingProcessor deployable) {
		this.deployable = deployable;
		this.externalId = "[" + deployable.getExternalId() + "] ";
	}

	@Required
	@Configurable
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Configurable
	@Required
	public void setModuleClassLoader(ClassLoader moduleClassLoader) {
		this.moduleClassLoader = moduleClassLoader;
	}

	@Configurable
	@Required
	public void setCortexCache(CortexCache<String, ProducerMessagingConnector> cache) {
		this.cache = cache;
	}
}
