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
package tribefire.extension.messaging.integration.test;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static tribefire.extension.messaging.integration.test.StaticTestVariables.KAFKA_URL;
import static tribefire.extension.messaging.integration.test.StaticTestVariables.PULSAR_SERVICE_URL;
import static tribefire.extension.messaging.integration.test.StaticTestVariables.PULSAR_URL;
import static tribefire.extension.messaging.integration.test.StaticTestVariables.TOPIC;
import static tribefire.extension.messaging.integration.test.util.TestQueryUtil.query;
import static tribefire.extension.messaging.integration.test.util.TestQueryUtil.queryAnDeleteAllProcessWith;
import static tribefire.extension.messaging.integration.test.util.TestQueryUtil.queryAnDeleteDeployable;
import static tribefire.extension.messaging.integration.test.util.TestQueryUtil.queryAndDelete;
import static tribefire.extension.messaging.integration.test.util.TestQueryUtil.queryMetaModel;
import static tribefire.extension.messaging.model.MessagingConnectorType.PULSAR;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.RecordsToDelete;
import org.apache.kafka.common.TopicPartition;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import com.braintribe.model.cortexapi.model.NotifyModelChanged;
import com.braintribe.model.deploymentapi.request.Deploy;
import com.braintribe.model.deploymentapi.request.Undeploy;
import com.braintribe.model.deploymentapi.response.DeployResponse;
import com.braintribe.model.deploymentapi.response.DeploymentResponseMessage;
import com.braintribe.model.extensiondeployment.ServiceProcessor;
import com.braintribe.model.extensiondeployment.meta.AroundProcessWith;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;
import com.braintribe.utils.RandomTools;

import tribefire.extension.messaging.connector.api.AbstractConsumerMessagingConnector;
import tribefire.extension.messaging.connector.api.AbstractProducerMessagingConnector;
import tribefire.extension.messaging.connector.kafka.KafkaConsumerMessagingConnectorImpl;
import tribefire.extension.messaging.connector.kafka.KafkaProducerMessagingConnectorImpl;
import tribefire.extension.messaging.connector.pulsar.PulsarConsumerMessagingConnectorImpl;
import tribefire.extension.messaging.connector.pulsar.PulsarProducerMessagingConnectorImpl;
import tribefire.extension.messaging.integration.test.util.CustomJUnitStopWatch;
import tribefire.extension.messaging.model.Envelop;
import tribefire.extension.messaging.model.InterceptionTarget;
import tribefire.extension.messaging.model.Message;
import tribefire.extension.messaging.model.MessagingConnectorType;
import tribefire.extension.messaging.model.ResourceBinaryPersistence;
import tribefire.extension.messaging.model.comparison.AddEntries;
import tribefire.extension.messaging.model.comparison.DiffType;
import tribefire.extension.messaging.model.comparison.TypesProperties;
import tribefire.extension.messaging.model.comparison.TypesProperty;
import tribefire.extension.messaging.model.conditions.types.TypeComparison;
import tribefire.extension.messaging.model.conditions.types.TypeOperator;
import tribefire.extension.messaging.model.deployment.event.DiffLoader;
import tribefire.extension.messaging.model.deployment.event.EventEndpoint;
import tribefire.extension.messaging.model.deployment.event.EventEndpointConfiguration;
import tribefire.extension.messaging.model.deployment.event.KafkaEndpoint;
import tribefire.extension.messaging.model.deployment.event.PulsarEndpoint;
import tribefire.extension.messaging.model.deployment.event.rule.ConsumerEventRule;
import tribefire.extension.messaging.model.deployment.event.rule.ProducerDiffEventRule;
import tribefire.extension.messaging.model.deployment.event.rule.ProducerEventRule;
import tribefire.extension.messaging.model.deployment.event.rule.ProducerStandardEventRule;
import tribefire.extension.messaging.model.deployment.service.MessagingAspect;
import tribefire.extension.messaging.model.deployment.service.MessagingProcessor;
import tribefire.extension.messaging.model.deployment.service.MessagingWorker;
import tribefire.extension.messaging.model.deployment.service.test.TestGetObjectProcessor;
import tribefire.extension.messaging.model.deployment.service.test.TestReceiveMessagingProcessor;
import tribefire.extension.messaging.model.deployment.service.test.TestUpdateObjectProcessor;
import tribefire.extension.messaging.model.meta.MessagingProperty;
import tribefire.extension.messaging.model.meta.MessagingTypeSignature;
import tribefire.extension.messaging.model.meta.RelatedObjectType;
import tribefire.extension.messaging.model.service.MessagingRequest;
import tribefire.extension.messaging.model.service.admin.FlushProducerConfigurationCache;
import tribefire.extension.messaging.model.service.consume.ProcessConsumedMessage;
import tribefire.extension.messaging.model.service.produce.ProduceMessage;
import tribefire.extension.messaging.model.test.TestObject;
import tribefire.extension.messaging.service.test.model.TestGetObjectRequest;
import tribefire.extension.messaging.service.test.model.TestGetObjectResult;
import tribefire.extension.messaging.service.test.model.TestUpdateRequest;

public abstract class AbstractMessagingTest extends AbstractTribefireQaTest {
	private static final String TEST_STRING = "Crush test dummy";
	private static final String SOURCE_FILE = "infected_source.txt";

	private static final String TEST_EV_CONFIG = "test-event-config";
	private static final String TEST_ENDPT = "test-endpoint";

	private static final String PRODUCER_RULE = "test-producer-rule";
	private static final String CONSUMER_RULE = "test-consumer-rule";

	private static final String TEST_PROCESSOR_ID = "test-processor";
	private static final String TEST_ASPECT_ID = "test-aspect";

	private static final String TEST_GET_PROC_ID = "test-get-proc";
	private static final String TEST_UPD_PROC_ID = "test-upd-proc";

	protected static final String TEST_CONSUMER_ID = "test-consumer";
	private static final String TEST_WORKER_ID = "test-worker";
	private static final String TEST_RECEIVE_PROCESSOR_ID = "test-receive-processor";
	private static final Set<String> DEPLOYABLES_EXTERNAL_IDS = asSet(TEST_CONSUMER_ID, TEST_WORKER_ID, TEST_PROCESSOR_ID, TEST_RECEIVE_PROCESSOR_ID,
			TEST_ASPECT_ID, TEST_GET_PROC_ID, TEST_UPD_PROC_ID);

	private static final String SERVICE_MODEL = "model:tribefire.extension.messaging:messaging-service-model";
	private static final String TEST_SERVICE_MODEL = "model:tribefire.extension.messaging:messaging-test-service-model";
	private static final String CORTEX_SERVICE_MODEL = "model:tribefire.cortex:tribefire-cortex-service-model";

	private static final String PROCESS_WITH_PREFIX = "test-process-with";
	private static final String TEST_COMPARISON = "test-comparison";
	private static final List<String> TEST_OBJS = List.of("ROOT_OBJ", "EMB_OBJ", "LIST1_OBJ", "LIST2_OBJ", "MAP1_OBJ", "MAP2_OBJ");
	private static final String TSMD = "test-TSMD";
	private static final String PPMD = "test-PPMD";
	private static final String PPMD2 = "test-PPMD-2";

	private static final String TP_PROPERTY = "test-TP_PROPERTY";
	private static final String TP_PROPERTIES = "test-TP_PROPERTIES";
	private static final String TYPE_SIGNATURE = "typeSignature";

	protected PersistenceGmSession cortexSession;
	protected com.braintribe.model.deployment.Module module;

	@Rule
	public CustomJUnitStopWatch stopwatch = new CustomJUnitStopWatch();

	// -----------------------------------------------------------------------
	// TEST - SETUP / TEARDOWN
	// -----------------------------------------------------------------------

	@Before
	public void before() {
		PersistenceGmSessionFactory sessionFactory = apiFactory().buildSessionFactory();
		cortexSession = sessionFactory.newSession("cortex");
		module = cortexSession.query().findEntity("module://tribefire.adx.phoenix:adx-aws-module");
		addTestModelsToCortex();
		cleanup();
	}

	private void addTestModelsToCortex() {
		GmMetaModel cortexServiceModel = queryMetaModel(cortexSession, CORTEX_SERVICE_MODEL);
		GmMetaModel testServiceModel = queryMetaModel(cortexSession, TEST_SERVICE_MODEL);
		cortexServiceModel.getDependencies().add(testServiceModel);
		notifyModelChange(cortexServiceModel);
	}

	@After
	public void after() {
		cleanup();
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------
	protected void deployProducingSet() {
		deployTestProcessors();
		MessagingProcessor processor = createAndDeployMessagingProcessor();
		createAndDeployAspectAndProcessWith(processor);
		FlushProducerConfigurationCache flushAspect = FlushProducerConfigurationCache.T.create();
		flushAspect.setServiceId(TEST_ASPECT_ID);
		cortexSession.eval(flushAspect).get();

		FlushProducerConfigurationCache flushProcessor = FlushProducerConfigurationCache.T.create();
		flushProcessor.setServiceId(TEST_PROCESSOR_ID);
		cortexSession.eval(flushProcessor).get();
	}

	protected void deployTestProcessors() {
		createAndDeployProcessor(TestUpdateObjectProcessor.T, TEST_UPD_PROC_ID, TEST_UPD_PROC_ID, TestUpdateRequest.T);
		createAndDeployProcessor(TestGetObjectProcessor.T, TEST_GET_PROC_ID, TEST_GET_PROC_ID, TestGetObjectRequest.T);
	}

	protected <C extends AbstractConsumerMessagingConnector> C getConsumer(EventEndpointConfiguration config) {
		if (config.getEventEndpoint().entityType().getShortName().equals(KafkaEndpoint.T.getShortName())) {
			return (C) new KafkaConsumerMessagingConnectorImpl(config);
		} else if (config.getEventEndpoint().entityType().getShortName().equals(PulsarEndpoint.T.getShortName())) {
			return (C) new PulsarConsumerMessagingConnectorImpl(config, ClassLoader.getPlatformClassLoader());
		} else {
			throw new IllegalArgumentException("Unsupported connector type !!!");
		}
	}

	protected <C extends AbstractProducerMessagingConnector> C getProducer(EventEndpointConfiguration config) {
		if (config.getEventEndpoint().entityType().getShortName().equals(KafkaEndpoint.T.getShortName())) {
			return (C) new KafkaProducerMessagingConnectorImpl(config, null);
		} else if (config.getEventEndpoint().entityType().getShortName().equals(PulsarEndpoint.T.getShortName())) {
			return (C) new PulsarProducerMessagingConnectorImpl(config, ClassLoader.getPlatformClassLoader());
		} else {
			throw new IllegalArgumentException("Unsupported connector type !!!");
		}
	}

	protected void createAndDeployWorker() {
		MessagingWorker worker = cortexSession.create(MessagingWorker.T);
		worker.setModule(module);
		worker.setAutoDeploy(true);
		worker.setName("TEST Worker");
		worker.setExternalId(TEST_WORKER_ID);
		cortexSession.commit();

		deploy(worker.getExternalId());
	}

	protected void createConsumerRule(List<EventEndpointConfiguration> endpts) {
		ConsumerEventRule rule = cortexSession.create(ConsumerEventRule.T);
		rule.setId(CONSUMER_RULE);
		rule.setGlobalId(CONSUMER_RULE);
		rule.setName(CONSUMER_RULE);
		rule.setRuleEnabled(true);
		GmEntityType postProcessorType = query(cortexSession, GmEntityType.T, TYPE_SIGNATURE, TestReceiveMessagingProcessor.T.getTypeSignature());
		rule.setPostProcessorType(postProcessorType);
		GmEntityType postProcessorRequestType = query(cortexSession, GmEntityType.T, TYPE_SIGNATURE, ProcessConsumedMessage.T.getTypeSignature());
		rule.setPostProcessorRequestType(postProcessorRequestType);
		rule.setEndpointConfiguration(endpts);
		cortexSession.commit();
	}

	protected void createAndDeployAspectAndProcessWith(MessagingProcessor processor) {
		MessagingAspect aspect = cortexSession.create(MessagingAspect.T);
		aspect.setExternalId(TEST_ASPECT_ID);
		aspect.setName(TEST_ASPECT_ID);
		aspect.setModule(module);
		aspect.setMessagingProcessor(processor);

		cortexSession.commit();
		deploy(aspect.getExternalId());

		AroundProcessWith processWith = cortexSession.create(AroundProcessWith.T);
		processWith.setGlobalId(processWithId(TEST_ASPECT_ID));
		processWith.setProcessor(aspect);
		cortexSession.commit();

		GmMetaModel cortexServiceModel = queryMetaModel(cortexSession, CORTEX_SERVICE_MODEL);
		GmMetaModel serviceModel = queryMetaModel(cortexSession, SERVICE_MODEL);
		BasicModelMetaDataEditor serviceModelEditor = BasicModelMetaDataEditor.create(cortexServiceModel).withEtityFactory(cortexSession::create)
				.done();
		serviceModelEditor.onEntityType(MessagingRequest.T).addMetaData(processWith);
		cortexSession.commit();

		notifyModelChange(serviceModel, cortexServiceModel);
	}

	protected void addDiffMetadata() {
		GmMetaModel cortexServiceModel = queryMetaModel(cortexSession, CORTEX_SERVICE_MODEL);
		GmMetaModel serviceModel = queryMetaModel(cortexSession, SERVICE_MODEL);
		GmMetaModel testServiceModel = queryMetaModel(cortexSession, TEST_SERVICE_MODEL);
		BasicModelMetaDataEditor serviceModelEditor = BasicModelMetaDataEditor.create(testServiceModel).withEtityFactory(cortexSession::create)
				.done();

		MessagingTypeSignature tsMd = cortexSession.create(MessagingTypeSignature.T);
		tsMd.setId(TSMD);
		tsMd.setGlobalId(TSMD);
		tsMd.setIdObjectType(RelatedObjectType.REQUEST);

		MessagingProperty ppMd = cortexSession.create(MessagingProperty.T);
		ppMd.setId(PPMD);
		ppMd.setGlobalId(PPMD);
		ppMd.setLoadedObjectType(TestObject.T.getTypeSignature());
		ppMd.setGetterEntityType(TestGetObjectRequest.T.getTypeSignature());
		cortexSession.commit();

		serviceModelEditor.onEntityType(TestUpdateRequest.T).addMetaData(tsMd);
		serviceModelEditor.onEntityType(TestUpdateRequest.T).addPropertyMetaData(TestUpdateRequest.relatedObjId, ppMd);
		serviceModelEditor.onEntityType(TestGetObjectRequest.T).addPropertyMetaData(TestGetObjectRequest.relatedObjId, ppMd);
		cortexSession.commit();

		notifyModelChange(serviceModel, testServiceModel, cortexServiceModel);
	}

	protected void unbindMetaData() {
		GmMetaModel cortexServiceModel = queryMetaModel(cortexSession, CORTEX_SERVICE_MODEL);
		GmMetaModel testServiceModel = queryMetaModel(cortexSession, TEST_SERVICE_MODEL);
		GmMetaModel serviceModel = queryMetaModel(cortexSession, SERVICE_MODEL);
		BasicModelMetaDataEditor serviceModelEditor = BasicModelMetaDataEditor.create(testServiceModel).withEtityFactory(cortexSession::create)
				.done();

		Set.of(PPMD, TSMD).forEach(v -> serviceModelEditor.onEntityType(TestUpdateRequest.T).removePropertyMetaData(m -> m.getId().equals(v)));
		Set.of(PPMD, TSMD).forEach(v -> serviceModelEditor.onEntityType(TestUpdateRequest.T).removeMetaData(m -> m.getId().equals(v)));
		cortexSession.commit();

		notifyModelChange(testServiceModel, cortexServiceModel);
	}

	protected void cleanup() {
		logger.info("Stating cleanup!");
		long start = System.currentTimeMillis();
		Undeploy undeploy = Undeploy.T.create();
		undeploy.setExternalIds(DEPLOYABLES_EXTERNAL_IDS);
		undeploy.eval(cortexSession).get();
		cortexSession.commit();

		unbindMetaData();

		DEPLOYABLES_EXTERNAL_IDS.forEach(e -> queryAnDeleteDeployable(cortexSession, e));
		queryAnDeleteAllProcessWith(cortexSession, PROCESS_WITH_PREFIX);
		queryAndDelete(cortexSession, EventEndpointConfiguration.T, EventEndpointConfiguration.globalId, TEST_EV_CONFIG);
		queryAndDelete(cortexSession, EventEndpoint.T, EventEndpoint.globalId, TEST_ENDPT);
		queryAndDelete(cortexSession, ProducerEventRule.T, ProducerEventRule.globalId, PRODUCER_RULE);
		queryAndDelete(cortexSession, ConsumerEventRule.T, ConsumerEventRule.globalId, CONSUMER_RULE);
		queryAndDelete(cortexSession, TypeComparison.T, TypeComparison.globalId, TEST_COMPARISON);
		queryAndDelete(cortexSession, TypesProperties.T, TypesProperties.globalId, TP_PROPERTIES);
		queryAndDelete(cortexSession, TypesProperty.T, TypesProperty.globalId, TP_PROPERTY);
		queryAndDelete(cortexSession, MessagingProperty.T, MessagingProperty.globalId, PPMD);
		queryAndDelete(cortexSession, MessagingProperty.T, MessagingProperty.globalId, PPMD2);
		queryAndDelete(cortexSession, MessagingTypeSignature.T, MessagingProperty.globalId, TSMD);
		TEST_OBJS.forEach(o -> queryAndDelete(cortexSession, TestObject.T, TestObject.globalId, o));

		logger.info("Cleanup took: '" + (System.currentTimeMillis() - start) + "'ms");
	}

	protected void cleanKafkaTopic() throws ExecutionException {
		TopicPartition topicPartition = new TopicPartition(TOPIC, 0);
		RecordsToDelete recordsToDelete = RecordsToDelete.beforeOffset(5L);

		Map<TopicPartition, RecordsToDelete> topicPartitionRecordToDelete = new HashMap<>();
		topicPartitionRecordToDelete.put(topicPartition, recordsToDelete);

		// Create AdminClient
		final Properties properties = new Properties();
		properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_URL);

		try (AdminClient adminClient = AdminClient.create(properties)) {
			adminClient.deleteRecords(topicPartitionRecordToDelete).all().get();
		} catch (InterruptedException e) {
			if (e.getMessage().contains("*")) {
				throw new IllegalArgumentException("Topic is not present in Kafka!!!");
			}
		}
	}

	protected ProduceMessage simpleMessageRequest() {
		LocalizedString string = LocalizedString.T.create();
		string.putDefault(TEST_STRING);

		Message message = Message.T.create().topic(asSet(TOPIC)).values(Map.of("testMsg", string));

		Envelop envelop = Envelop.T.create();
		envelop.setMessages(asList(message));

		ProduceMessage request = ProduceMessage.T.create();
		request.setEnvelop(envelop);

		return request;
	}

	protected ProduceMessage resourceMessageRequest(boolean isTransientResource, ResourceBinaryPersistence option) {
		Resource resource = isTransientResource ? getTransient() : getFileResource();

		Message message = Message.T.create().topic(asSet(TOPIC)).values(Map.of("testResource", resource)).option(option);

		Envelop envelop = Envelop.T.create();
		envelop.setMessages(Collections.singletonList(message));

		ProduceMessage request = ProduceMessage.T.create();
		request.setEnvelop(envelop);

		return request;
	}

	private Resource getTransient() {
		Resource resource = Resource.T.create();
		resource.setName(SOURCE_FILE);
		resource.setMimeType("text/plain");
		resource.assignTransientSource(() -> new ByteArrayInputStream(TEST_STRING.getBytes()));
		return resource;
	}

	private Resource getFileResource() {
		//@formatter:off
        return cortexSession.resources().create()
                       .name(SOURCE_FILE)
                       .mimeType("text/plain")
                       .store(new ByteArrayInputStream(TEST_STRING.getBytes()));
        //@formatter:on
	}

	protected MessagingProcessor createAndDeployMessagingProcessor() {
		logger.info("Messaging Processor initiation");
		return createAndDeployProcessor(MessagingProcessor.T, TEST_PROCESSOR_ID, "TEST Messaging Processor", ProduceMessage.T);
	}

	protected void createAndDeployTestReceiveMessagingProcessor() {
		logger.info("Receive Messaging Processor initiation");
		createAndDeployProcessor(TestReceiveMessagingProcessor.T, TEST_RECEIVE_PROCESSOR_ID, "TEST Receive Messaging Processor",
				ProcessConsumedMessage.T);
	}

	protected <T extends ServiceProcessor, R extends ServiceRequest> T createAndDeployProcessor(EntityType<T> entityType, String externalId,
			String name, EntityType<R> request) {
		T processor = cortexSession.create(entityType);
		processor.setExternalId(externalId);
		processor.setName(name);
		processor.setModule(module);

		cortexSession.commit();

		if (request != null) {
			ProcessWith processWith = cortexSession.create(ProcessWith.T);
			processWith.setGlobalId(processWithId(externalId));
			processWith.setProcessor(processor);
			cortexSession.commit();

			GmMetaModel cortexServiceModel = queryMetaModel(cortexSession, CORTEX_SERVICE_MODEL);
			GmMetaModel testServiceModel = queryMetaModel(cortexSession, TEST_SERVICE_MODEL);
			GmMetaModel serviceModel = queryMetaModel(cortexSession, SERVICE_MODEL);
			BasicModelMetaDataEditor serviceModelEditor = BasicModelMetaDataEditor
					.create(entityType.getShortName().contains("Test") ? testServiceModel : serviceModel).withEtityFactory(cortexSession::create)
					.done();
			serviceModelEditor.onEntityType(request).addMetaData(processWith);
			cortexSession.commit();

			notifyModelChange(testServiceModel, serviceModel, cortexServiceModel);
		}

		deploy(processor.getExternalId());
		return processor;
	}

	protected EventEndpointConfiguration getEndpConfig(MessagingConnectorType type) {
		EventEndpointConfiguration evConfig = cortexSession.create(EventEndpointConfiguration.T);
		evConfig.setTopics(Set.of(TOPIC));
		evConfig.setGlobalId(TEST_EV_CONFIG);
		evConfig.setId(TEST_EV_CONFIG);
		evConfig.setEventEndpoint(createEndpoint(type));
		cortexSession.commit();
		return evConfig;
	}

	private EventEndpoint createEndpoint(MessagingConnectorType type) {
		EntityType<? extends EventEndpoint> entityType = PULSAR == type ? PulsarEndpoint.T : KafkaEndpoint.T;
		EventEndpoint endpt = cortexSession.create(entityType);
		endpt.setName(TEST_ENDPT);
		endpt.setGlobalId(TEST_ENDPT);

		if (endpt instanceof PulsarEndpoint p) {
			p.setAdminUrl(PULSAR_SERVICE_URL);
			p.setConnectionUrl(PULSAR_URL);
		} else {
			endpt.setConnectionUrl(KAFKA_URL);
		}

		cortexSession.commit();
		return endpt;
	}

	protected void getProducerRule(InterceptionTarget ruleType, EventEndpointConfiguration endpConfig, List<TypesProperties> propertiesToInclude,
			TypeComparison comparison, DiffLoader loader) {
		ProducerEventRule evRule;
		if (ruleType == InterceptionTarget.DIFF) {
			Set<TypesProperty> typesProperties = getExtractionTypeProp(TestGetObjectResult.T, TestGetObjectResult.testObject);
			ProducerDiffEventRule r = cortexSession.create(ProducerDiffEventRule.T);
			r.setAddEntries(AddEntries.NONE);
			r.setDiffType(DiffType.ALL);
			r.setListedPropertiesOnly(false);
			r.setDiffLoader(loader);
			if (loader == DiffLoader.SERVICE) {
				r.setExtractionPropertyPaths(typesProperties);
			}
			evRule = r;
		} else {
			evRule = cortexSession.create(ProducerStandardEventRule.T);
		}
		evRule.setId(PRODUCER_RULE);
		evRule.setGlobalId(PRODUCER_RULE);
		evRule.setName(PRODUCER_RULE);
		evRule.setRuleEnabled(true);
		evRule.setInterceptionTarget(ruleType);
		evRule.setFilePersistenceStrategy(ResourceBinaryPersistence.NONE);
		evRule.setEndpointConfiguration(List.of(endpConfig));

		evRule.setFieldsToInclude(propertiesToInclude);
		evRule.setRequestTypeCondition(comparison);
		// evRule.setRequestPropertyCondition(); TODO Add something here???
		evRule.setRequiresUserInfo(true);

		cortexSession.commit();
	}

	protected Set<TypesProperty> getExtractionTypeProp(EntityType<?> entityType, String property) {
		TypesProperty typesProperty = cortexSession.create(TypesProperty.T);
		typesProperty.setGlobalId(TP_PROPERTY);
		GmEntityType type = query(cortexSession, GmEntityType.T, TYPE_SIGNATURE, entityType.getTypeSignature());
		typesProperty.setEntityType(type);
		typesProperty.setProperty(property);
		cortexSession.commit();
		return Set.of(typesProperty);
	}

	protected List<TypesProperties> getPropertiesToInclude(EntityType<?> typeSignature, String... properties) {
		TypesProperties typeProp = cortexSession.create(TypesProperties.T);
		typeProp.setGlobalId(TP_PROPERTIES);
		GmEntityType type = query(cortexSession, GmEntityType.T, TYPE_SIGNATURE, typeSignature.getTypeSignature());
		typeProp.setEntityType(type);
		typeProp.setProperties(Arrays.stream(properties).collect(Collectors.toSet()));
		cortexSession.commit();
		return List.of(typeProp);
	}

	protected TestObject storeTestObject() {
		TestObject embObj = getTestObject("EMB_OBJ", null, new HashMap<>(), new ArrayList<>());
		TestObject list1Obj = getTestObject("LIST1_OBJ", null, new HashMap<>(), new ArrayList<>());
		TestObject list2Obj = getTestObject("LIST2_OBJ", null, new HashMap<>(), new ArrayList<>());
		TestObject map1Obj = getTestObject("MAP1_OBJ", null, new HashMap<>(), new ArrayList<>());
		TestObject map2Obj = getTestObject("MAP2_OBJ", null, new HashMap<>(), new ArrayList<>());
		return getTestObject("ROOT_OBJ", embObj, Map.of(map1Obj.getName(), map1Obj, map2Obj.getName(), map2Obj), List.of(list1Obj, list2Obj));
	}

	protected TypeComparison getTypeCondition() {
		TypeComparison condition = cortexSession.create(TypeComparison.T);
		condition.setGlobalId(TEST_COMPARISON);
		condition.setId(TEST_COMPARISON);
		condition.setOperator(TypeOperator.like);
		condition.setTypeName(TestUpdateRequest.T.getShortName());
		cortexSession.commit();
		return condition;
	}

	protected void deploy(String... externalIds) {
		Deploy deploy = Deploy.T.create();
		deploy.setExternalIds(Arrays.stream(externalIds).collect(Collectors.toSet()));
		DeployResponse response = deploy.eval(cortexSession).get();
		DeploymentResponseMessage responseMessage = response.getResults().get(0);
		if (!responseMessage.getSuccessful()) {
			throw new RuntimeException(responseMessage.getMessage());
		}
	}

	protected void notifyModelChange(GmMetaModel... models) {
		Arrays.stream(models).forEach(model -> {
			NotifyModelChanged notify = NotifyModelChanged.T.create();
			notify.setModel(model);
			notify.eval(cortexSession).get();
		});
	}

	protected TestObject getTestObject(String name, TestObject embedded, Map<String, TestObject> map, List<TestObject> list) {
		TestObject object = cortexSession.create(TestObject.T);
		object.setName(name);
		object.setId(name + RandomTools.newStandardUuid());
		object.setGlobalId(name);
		object.setEmbeddedObject(embedded);
		object.setObjectMap(map);
		object.setObjectList(list);
		cortexSession.commit();
		return object;
	}

	protected String processWithId(String testId) {
		return PROCESS_WITH_PREFIX + "_" + testId;
	}
}
