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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.UnsatisfiedMaybeTunneling;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.traverse.EntityCollector;
import com.braintribe.model.processing.worker.api.Worker;
import com.braintribe.model.processing.worker.api.WorkerContext;
import com.braintribe.model.processing.worker.api.WorkerException;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Resource;

import tribefire.extension.messaging.connector.api.ConsumerMessagingConnector;
import tribefire.extension.messaging.connector.kafka.KafkaConsumerMessagingConnectorImpl;
import tribefire.extension.messaging.connector.pulsar.PulsarConsumerMessagingConnectorImpl;
import tribefire.extension.messaging.model.Envelop;
import tribefire.extension.messaging.model.Message;
import tribefire.extension.messaging.model.deployment.event.EventEndpointConfiguration;
import tribefire.extension.messaging.model.deployment.event.rule.ConsumerEventRule;
import tribefire.extension.messaging.model.deployment.event.rule.EventRule;
import tribefire.extension.messaging.model.deployment.event.rule.ProducerEventRule;
import tribefire.extension.messaging.model.service.admin.FlushCacheMessagingResult;
import tribefire.extension.messaging.model.service.admin.FlushConsumerConfigurationCache;
import tribefire.extension.messaging.model.service.consume.ProcessConsumedMessageResult;
import tribefire.extension.messaging.model.service.consume.ProcessConsumedMessage;
import tribefire.extension.messaging.service.cache.CortexCache;
import tribefire.extension.messaging.service.reason.validation.MandatoryNotSatisfied;

public class MessagingWorker
		implements Worker, Runnable, com.braintribe.model.processing.service.api.ServiceProcessor<FlushConsumerConfigurationCache, GenericEntity> {
	private static final Logger logger = Logger.getLogger(MessagingWorker.class);

	private boolean runInLoop = true;
	private Future<?> workerFuture;

	private tribefire.extension.messaging.model.deployment.service.MessagingWorker deployable;
	private PersistenceGmSessionFactory factory;
	private String externalId;
	private String contextName;

	private CortexCache<String, ConsumerMessagingConnector> connectorsCache;
	private CortexCache<String, ConsumerEventRule> rulesCache;
	private ClassLoader moduleClassLoader;

	// -----------------------------------------------------------------------
	// CLEAR CACHES
	// -----------------------------------------------------------------------
	@Override
	public GenericEntity process(ServiceRequestContext requestContext, FlushConsumerConfigurationCache request) {
		flushCaches();
		return FlushCacheMessagingResult.T.create();
	}

	// ------------------------ Flush consumerEventRule cache handle ------------------------ //
	public void flushCaches() {
		connectorsCache.getAll().forEach(ConsumerMessagingConnector::finalizeConsume);
		connectorsCache.invalidateCache();
		rulesCache.invalidateCache();
		logger.info(() -> "Caches has being wiped!!!");
	}
	// -----------------------------------------------------------------------
	// RUNNABLE
	// -----------------------------------------------------------------------

	@Override
	public void run() {
		logger.info(() -> externalId + "Start consuming messages...");
		while (runInLoop) {
			refreshRulesAndConnectors();
			connectorsCache.getAll().forEach(this::processMessages);
		}

		connectorsCache.getAll().forEach(ConsumerMessagingConnector::finalizeConsume);
		logger.info(() -> externalId + "Finished consuming messages!");
	}

	private void processMessages(ConsumerMessagingConnector connector) {
		List<Message> messages = connector.consumeMessages();
		checkMessageResources(messages);
		String connectorExternalId = connector.getExternalId();
		List<ProcessConsumedMessageResult> postProcessingResult = rulesCache.getAll().stream()
				.filter(r -> r.getEndpointConfiguration().stream().anyMatch(c -> c.getEventEndpoint().getGlobalId().equals(connectorExternalId)))
				.map(ConsumerEventRule::getPostProcessorRequestType).map(type -> createPostProcessingRequestAndEvaluate(type.entityType(), messages))
				.toList();
		postProcessingResult.stream().map(r -> externalId + " Message has being forwarded to corresponding post service: " + r).forEach(logger::info);
	}

	private <T extends ProcessConsumedMessage> ProcessConsumedMessageResult createPostProcessingRequestAndEvaluate(
			EntityType<T> postProcessorRequestType, List<Message> messages) {
		ProcessConsumedMessage request = postProcessorRequestType.create();
		request.setEnvelop(getEnvelop(messages));
		return request.eval(getSession()).get(); //TODO here in case of failure a Reason approach would be appropriate, or an error log without throwing any exception, depends on Gerrys decision.
	}

	private void refreshRulesAndConnectors() {
		if (connectorsCache.getAll().isEmpty()) {
			List<ConsumerEventRule> rules = queryCortexForRules();
			connectorsCache.getAll().forEach(ConsumerMessagingConnector::finalizeConsume);
			createConsumers(rules);
		}
	}

	private List<ConsumerEventRule> queryCortexForRules() {
		String searchPattern = StringUtils.isEmpty(contextName) ? "*" : ("*" + contextName + "*");
		//@formatter:off
		EntityQuery query = EntityQueryBuilder.from(ConsumerEventRule.T)
				                    .where()
				                    .conjunction()
				                    .property(EventRule.ruleEnabled).eq(true)
				                    .property(ProducerEventRule.globalId).like(searchPattern)
				                    .close()
				                    .done();
		//@formatter:on
		return factory.newSession("cortex").query().entities(query).list();
	}

	private void createConsumers(List<ConsumerEventRule> rules) {
		//@formatter:off
		Set<EventEndpointConfiguration> endptConfs = rules.stream()
				                                             .map(EventRule::getEndpointConfiguration)
				                                             .flatMap(List::stream)
				                                             .collect(Collectors.toSet());
		//@formatter:on
		endptConfs.stream().map(this::registerConsumer).forEach(connectorsCache::put);
	}

	private ConsumerMessagingConnector registerConsumer(EventEndpointConfiguration configuration) {
		return switch (configuration.getEventEndpoint().getConnectorType()) {
			case KAFKA -> new KafkaConsumerMessagingConnectorImpl(configuration);
			case PULSAR -> new PulsarConsumerMessagingConnectorImpl(configuration, moduleClassLoader);
			default -> throw new UnsatisfiedMaybeTunneling(Reasons.build(MandatoryNotSatisfied.T)
					.text(String.format("Connector %s is not supported!", configuration.getEventEndpoint().getConnectorType())).toMaybe());
		};
	}

	private void checkMessageResources(List<Message> consumeMessages) {
		Optional.ofNullable(consumeMessages).orElseGet(Collections::emptyList).forEach(this::collectResources);
	}

	private void collectResources(Message message) {
		Optional.ofNullable(message).ifPresent(m -> {
			EntityCollector collector = new EntityCollector();
			collector.visit(message.getValues());
			Set<GenericEntity> entities = collector.getEntities();
			//@formatter:off
			entities.stream()
					.filter(Resource.class::isInstance)
					.map(Resource.class::cast)
					.filter(message::shouldPersist)
					.forEach(r -> bindResource(message, r));
			//@formatter:on
		});
	}

	private void bindResource(Message consumeMessage, Resource r) {
		Map<Object, Resource> resourceMapping = consumeMessage.getResourceMapping();
		// resource was transient - only available in messaging (binary store)
		// resource was not transient - available in messaging (binary store) and still in source access (potentially)
		Resource messagingResource = resourceMapping
				.get(r.isTransient() ? r.getResourceSource().getGlobalId() : Optional.ofNullable(r.getId()).orElseGet(r::getGlobalId));
		Objects.requireNonNull(messagingResource, "messagingResource must be set");
	}

	private Envelop getEnvelop(List<Message> consumeMessages) {
		Envelop envelop = Envelop.T.create();
		envelop.setMessages(consumeMessages);
		return envelop;
	}

	private PersistenceGmSession getSession() {
		return this.factory.newSession("cortex");
	}

	// -----------------------------------------------------------------------
	// WORKER
	// -----------------------------------------------------------------------

	@Override
	public GenericEntity getWorkerIdentification() {
		return deployable;
	}

	@Override
	public void start(WorkerContext workerContext) throws WorkerException {
		logger.info(() -> externalId + "Starting the MessagingConsumer");

		runInLoop = true;
		workerFuture = workerContext.submit(this);
	}

	@Override
	public void stop(WorkerContext workerContext) throws WorkerException {
		logger.info(() -> externalId + "Stopping the MessagingConsumer");

		runInLoop = false;
		if (workerFuture != null) {
			workerFuture.cancel(true);
		}
		workerFuture = null;
		connectorsCache.getAll().forEach(ConsumerMessagingConnector::finalizeConsume);
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Configurable
	@Required
	public void setDeployable(tribefire.extension.messaging.model.deployment.service.MessagingWorker deployable) {
		this.deployable = deployable;
		this.externalId = "[" + deployable.getExternalId() + "] ";
	}

	@Configurable
	@Required
	public void setModuleClassLoader(ClassLoader moduleClassLoader) {
		this.moduleClassLoader = moduleClassLoader;
	}

	@Configurable
	@Required
	public void setFactory(PersistenceGmSessionFactory factory) {
		this.factory = factory;
	}

	@Required
	@Configurable
	public void setContextName(String contextName) {
		this.contextName = contextName;
	}

	@Required
	@Configurable
	public void setRulesCache(CortexCache<String, ConsumerEventRule> rulesCache) {
		this.rulesCache = rulesCache;
	}

	@Required
	@Configurable
	public void setConnectorsCache(CortexCache<String, ConsumerMessagingConnector> connectorsCache) {
		this.connectorsCache = connectorsCache;
	}
}
