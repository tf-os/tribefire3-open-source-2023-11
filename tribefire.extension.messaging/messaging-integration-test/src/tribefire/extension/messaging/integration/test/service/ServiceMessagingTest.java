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
package tribefire.extension.messaging.integration.test.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static tribefire.extension.messaging.integration.test.StaticTestVariables.TOPIC;
import static tribefire.extension.messaging.model.InterceptionTarget.DIFF;
import static tribefire.extension.messaging.model.MessagingConnectorType.KAFKA;
import static tribefire.extension.messaging.model.MessagingConnectorType.PULSAR;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.kafka.common.TopicPartition;
import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;

import tribefire.extension.messaging.connector.api.AbstractConsumerMessagingConnector;
import tribefire.extension.messaging.connector.kafka.KafkaConsumerMessagingConnectorImpl;
import tribefire.extension.messaging.integration.test.AbstractMessagingTest;
import tribefire.extension.messaging.model.InterceptionTarget;
import tribefire.extension.messaging.model.Message;
import tribefire.extension.messaging.model.comparison.ComparisonResult;
import tribefire.extension.messaging.model.comparison.Diff;
import tribefire.extension.messaging.model.comparison.TypesProperties;
import tribefire.extension.messaging.model.deployment.event.DiffLoader;
import tribefire.extension.messaging.model.deployment.event.EventEndpointConfiguration;
import tribefire.extension.messaging.model.test.TestObject;
import tribefire.extension.messaging.service.test.model.TestGetObjectRequest;
import tribefire.extension.messaging.service.test.model.TestUpdateRequest;

public class ServiceMessagingTest extends AbstractMessagingTest {
	private static final Date startDate = new Date();
	/* private static final EntityQuery SELECT_ALL_QUERY =
	 * EntityQueryBuilder.from(Resource.T).where().property("name").ilike("*infected*").done(); private static final
	 * String WRONG_URL = "http://localhost:9999/wrong_url_to_connnect_to"; private static final String
	 * PULSAR_WRONG_CONNECTION_URL_DETAILS =
	 * "java.util.concurrent.CompletionException: org.apache.pulsar.client.admin.internal.http.AsyncHttpConnector$RetryException: Could not complete the operation. Number of retries has been exhausted. Failed reason: Remotely closed"
	 * ; */

	/* private final JsonStreamMarshaller marshaller = new JsonStreamMarshaller(); private final BiPredicate<Boolean,
	 * ResourceBinaryPersistence> shouldBePersistedPredicate = (a, b) -> a && Arrays.asList(TRANSIENT, ALL).contains(b)
	 * || !a && b == ALL; */

	@Test
	@Ignore
	public void diffRuleQuery() {
		deployProducingSet();
		EventEndpointConfiguration endpConfig = getEndpConfig(KAFKA);
		List<TypesProperties> propertiesToInclude = getPropertiesToInclude(TestObject.T, TestObject.name, TestObject.embeddedObject);
		getProducerRule(DIFF, endpConfig, propertiesToInclude, getTypeCondition(), DiffLoader.QUERY);
		addDiffMetadata();

		testDiffOnRenameAction(endpConfig);
	}

	@Test
	@Ignore
	public void diffRuleService() {
		deployProducingSet();
		EventEndpointConfiguration endpConfig = getEndpConfig(KAFKA);
		List<TypesProperties> propertiesToInclude = getPropertiesToInclude(TestObject.T, TestObject.name, TestObject.embeddedObject);
		getProducerRule(DIFF, endpConfig, propertiesToInclude, getTypeCondition(), DiffLoader.SERVICE);
		addDiffMetadata();

		testDiffOnRenameAction(endpConfig);
	}

	@Test
	@Ignore
	public void requestInterceptionRulePulsar() {
		deployProducingSet();
		EventEndpointConfiguration endpConfig = getEndpConfig(PULSAR);
		List<TypesProperties> propertiesToInclude = getPropertiesToInclude(TestGetObjectRequest.T, TestGetObjectRequest.globalId,
				TestGetObjectRequest.relatedObjId);
		getProducerRule(InterceptionTarget.REQUEST, endpConfig, propertiesToInclude, getTypeCondition(), null);

		AbstractConsumerMessagingConnector consumer = getConsumer(endpConfig);

		GenericEntity value = executeRequestAndPerformPrimaryTesting(consumer, "request");
		if (value instanceof TestUpdateRequest m) {
			assertTrue(m.getRelatedObjId().startsWith("ROOT_OBJ"));
		} else {
			fail("Message value is of unexpected type: " + value.entityType().getTypeSignature());
		}
	}

	// -----------------------------------------------------------------------
	// DEFAULT METHODS
	// -----------------------------------------------------------------------

	private void testDiffOnRenameAction(EventEndpointConfiguration endpConfig) {
		AbstractConsumerMessagingConnector consumer = getConsumer(endpConfig);
		if (consumer instanceof KafkaConsumerMessagingConnectorImpl k) {
			TopicPartition topicPartition = new TopicPartition(TOPIC, 0);
			k.setOffset(Set.of(topicPartition));
		}

		GenericEntity value = executeRequestAndPerformPrimaryTesting(consumer, "diff");
		if (value instanceof ComparisonResult result) {
			assertTrue(result.getExpectedValuesDiffer());
			List<Diff> expectedDiffs = result.getExpectedDiffs();
			assertEquals(1, expectedDiffs.size());
			Diff diff0 = expectedDiffs.get(0);
			assertEquals("ROOT.name", diff0.getPropertyPath());
			assertEquals("ROOT_OBJ", diff0.getOldValue());
			assertEquals("NEW_NAME", diff0.getNewValue());

			List<Diff> unexpectedDiffs = result.getUnexpectedDiffs();
			assertEquals(40, unexpectedDiffs.size());
		} else {
			fail("Message does not contain expected object: ComparisonResult");
		}
	}

	private GenericEntity executeRequestAndPerformPrimaryTesting(AbstractConsumerMessagingConnector consumer, String messageValueKey) {
		TestObject object = storeTestObject();

		TestUpdateRequest request = TestUpdateRequest.buildRequest(object.getId(), "NEW_NAME");
		request.eval(cortexSession).get();

		List<Message> messages = consumer.consumeMessages();
		consumer.finalizeConsume();
		assertFalse(messages.isEmpty());
		Message message = messages.get(messages.size() - 1);
		assertTrue(message.getTimestamp().after(startDate) && message.getTimestamp().before(new Date()));
		assertTrue(message.getTopics().contains("test"));
		return message.getValues().get(messageValueKey);
	}

	/* @Test //TODO @dmiex this should be, most probably, completely rewritten after the whole thing is functional. kept
	 * for design reasons public void preProcess() { PulsarProducerMessagingConnector producer =
	 * createAndDeployPulsarProducer(false, PULSAR_SERVICE_URL); createAndDeployMessagingProcessor(producer);
	 * createAndDeployAspectAndProcessWith(); simpleMessageRequest().eval(cortexSession).get(); } */

	// Kafka Tests
	/*@Test
	public void stringMsgKafkaSuccess() {
		successTest(KAFKA, () -> createAndDeployKafkaProducer(false), this::createAndDeployKafkaConsumer, false, true, DO_NOT_PERSIST);
	}

	@Test
	public void transientPersistNoneMsgKafkaSuccess() {
		successTest(KAFKA, () -> createAndDeployKafkaProducer(false), this::createAndDeployKafkaConsumer, true, true, DO_NOT_PERSIST);
	}

	@Test
	public void transientPersistTransientMsgKafkaSuccess() {
		successTest(KAFKA, () -> createAndDeployKafkaProducer(false), this::createAndDeployKafkaConsumer, true, true, PERSIST_TRANSIENT);
	}

	@Test
	public void transientPersistAllMsgKafkaSuccess() {
		successTest(KAFKA, () -> createAndDeployKafkaProducer(false), this::createAndDeployKafkaConsumer, true, true, PERSIST_ALL);
	}

	@Test
	public void filePersistNoneMsgKafkaSuccess() {
		successTest(KAFKA, () -> createAndDeployKafkaProducer(false), this::createAndDeployKafkaConsumer, true, false, DO_NOT_PERSIST);
	}

	@Test
	public void filePersistTransientMsgKafkaSuccess() {
		successTest(KAFKA, () -> createAndDeployKafkaProducer(false), this::createAndDeployKafkaConsumer, true, false, PERSIST_TRANSIENT);
	}

	@Test
	public void filePersistAllMsgKafkaSuccess() {
		successTest(KAFKA, () -> createAndDeployKafkaProducer(false), this::createAndDeployKafkaConsumer, true, false, PERSIST_ALL);
	}

	@Test
	public void failToConnectKafka() {
		globalFailUnableToConnectTest(KAFKA, () -> createAndDeployKafkaProducer(true));
	}

	@Test
	public void kafkaHealthCheckSuccess() {
		healthTest(KAFKA, () -> createAndDeployKafkaProducer(false));
		healthTest(KAFKA, this::createAndDeployKafkaConsumer);
	}

	@Test
	@Ignore
	public void kafkaHealthCheckFail(){
		healthTestFail(KAFKA,()-> createAndDeployKafkaProducer(false),null);
	}

	// Pulsar Tests
	@Test
	public void stringMsgPulsarSuccess() {
		successTest(PULSAR, () -> createAndDeployPulsarProducer(false, PULSAR_SERVICE_URL), () -> createAndDeployPulsarConsumer(false, PULSAR_SERVICE_URL),
				true, false, PERSIST_ALL);
	}

	@Test
	public void failToConnectPulsar() {
		globalFailUnableToConnectTest(PULSAR, () -> createAndDeployPulsarProducer(true, PULSAR_SERVICE_URL));
	}

	@Test
	public void pulsarHealthCheckSuccess() {
		healthTest(PULSAR, () -> createAndDeployPulsarProducer(false, PULSAR_SERVICE_URL));
		healthTest(PULSAR, () -> createAndDeployPulsarConsumer(false, PULSAR_SERVICE_URL));
	}

	@Test
	@Ignore //TODO Ignored as currently there is no way to simulate pulsar server failure
	public void pulsarHealthCheckFailureWrongConnectionUrl() {
		// wrongUrl
		healthTestFail(PULSAR, () -> createAndDeployPulsarProducer(false, WRONG_URL), PULSAR_WRONG_CONNECTION_URL_DETAILS);
		healthTestFail(PULSAR, () -> createAndDeployPulsarConsumer(false, WRONG_URL), PULSAR_WRONG_CONNECTION_URL_DETAILS);
	}


	public void globalFailUnableToConnectTest(String connector, Supplier<MessagingProducer> producerSupplier) {
		try {
			producerSupplier.get();
			fail("Performed unable to connect test for " + connector + ". Expected an exception to be thrown, but nothing happened!");
		} catch (RuntimeException t) {
			// void here
		}
	}

	private void healthTest(String connector, Supplier<MessagingConnectorDeployable> supplier) {
		// Deploy a connector
		MessagingConnectorDeployable deployable = supplier.get();

		// Perform healthCheck
		CheckResultEntry resultingCheck = performHealthCheck(connector, deployable);
		assertEquals("Check status fail for " + connector + " " + deployable.getName(), CheckStatus.ok, resultingCheck.getCheckStatus());
		assertEquals("Check result 'name' filed is empty for " + connector + " " + deployable.getName(), deployable.getName(),
				resultingCheck.getName());
		assertNotNull("The result check for " + connector + " " + deployable.getName() + "did not contain details!", resultingCheck.getDetails());
	}

	private void healthTestFail(String connector, Supplier<MessagingConnectorDeployable> supplier, String details) {
		// Deploy a connector
		MessagingConnectorDeployable deployable = supplier.get();

		// Perform healthCheck
		CheckResultEntry resultingCheck = performHealthCheck(connector, deployable);
		assertEquals(CheckStatus.fail, resultingCheck.getCheckStatus());
		assertEquals(connector + " cluster unreachable!", resultingCheck.getMessage());
		assertThat(resultingCheck.getDetails()).startsWith(details);
	}

	private CheckResultEntry performHealthCheck(String connector, MessagingConnectorDeployable deployable) {
		ExtensionBaseHealthCheck healthCheck = ExtensionBaseHealthCheck.T.create();
		healthCheck.setServiceId("messaging.healthzProcessor");
		CheckResult result = healthCheck.eval(cortexSession).get();

		// Get deployed component check result and perform several assertions on it
		//@formatter:off
        return result.getEntries().stream()
                .filter(r -> r.getId().equals(deployable.getExternalId())).findFirst()
                .orElseThrow(() -> new AssertionError("The health check result for " + connector + " " + deployable.getName() + " is not present!"));
        //@formatter:on
	}

	private void successTest(String connector, Supplier<MessagingProducer> producerSupplier, Supplier<MessagingConsumer> consumerSupplier,
			boolean isFileTest, boolean isTransientResource, BinaryPersistenceOption option) {
		// setup
		List<Resource> list = cortexSession.query().entities(SELECT_ALL_QUERY).list();
		logger.info(format("Found %s resources", list.size()));
		list.forEach(cortexSession::deleteEntity);

		if (connector.equals(KAFKA)) {
			clearTopicKafka();
		}

		createAndDeployTestReceiveMessagingProcessor();
		createAndDeployWorker(consumerSupplier.get());
		createAndDeployMessagingProcessor(producerSupplier.get());

		// send msg
		SendMessage simpleMessageRequest = isFileTest ? resourceMessageRequest(isTransientResource, option) : simpleMessageRequest();
		SendMessageResult sendMessageResult = simpleMessageRequest.eval(cortexSession).getReasoned().get();
		String testObj = !isFileTest ? "String" : (isTransientResource ? "Transient" : "FileSystem") + "Resource + option: " + option;
		assertNotNull(format("%s Message sending Failure in %s", testObj, connector), sendMessageResult);

		// extract msg result and clean setup
		String envelopeFromJson = extractContentFromFile(PERSISTED_JSON);
		String errorMsg = format(
				"%s test.%nEither consumer/processor failed to write result into file or and error reading/parsing it. Connector: %s", testObj,
				connector);
		assertNotNull(errorMsg, envelopeFromJson);

		// unmarshall msg and test topic and value
		Envelop unmarshalledObject = (Envelop) marshaller.decode(envelopeFromJson);
		assertEquals(1, unmarshalledObject.getMessages().size());

		Message resultMessage = unmarshalledObject.getMessages().iterator().next();
		assertTrue(resultMessage.getTopics().contains(TOPIC));
		assertNotNull("Message did not contain timestamp. Connector: " + connector, resultMessage.getTimestamp());

		if (isFileTest) {
			assertEquals(option, resultMessage.getBinaryPersistenceOption());

			Resource expected = (Resource) simpleMessageRequest.getEnvelop().getMessages().get(0).getValue();
			Resource actual = (Resource) resultMessage.getValue();
			assertEquals(expected.getName(), actual.getName());
			assertEquals(expected.getMimeType(), actual.getMimeType());
			assertThat(actual.getResourceSource()).usingRecursiveComparison().ignoringActualNullFields()
					.ignoringFields(" inputStreamProvider", " owner. resourceSource. inputStreamProvider", " owner. tags", "runtimeId")
					.isEqualTo(expected.getResourceSource());

			if (shouldBePersistedPredicate.test(isTransientResource, option)) {
				assertEquals(1, resultMessage.getResourceMapping().size());
				String contentFromFile = extractContentFromFile(PERSISTED_FILE);
				assertEquals(TEST_STRING, contentFromFile);
			} else {
				assertTrue(resultMessage.getResourceMapping().isEmpty());
			}
		} else {
			LocalizedString value = (LocalizedString) resultMessage.getValue();
			assertNotNull(value);
			assertThat(value.value()).isEqualTo(TEST_STRING);
		}
	}

	private String extractContentFromFile(String fileName) {
		final AtomicReference<String> content = new AtomicReference<>();
		long start = System.nanoTime();
		await().atMost(30, SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() -> {
			EntityQuery query = EntityQueryBuilder.from(Resource.T).where().property("name").ilike(fileName).done();
			List<Resource> resources = cortexSession.query().entities(query).list();
			if (resources.isEmpty()) {
				logger.info(() -> "Could not load resource, keep trying...");
				return false;
			}

			String string = IOUtils.toString(resources.iterator().next().openStream());
			content.set(string);
			resources.forEach(cortexSession::deleteEntity);
			cortexSession.commit();

			return true;
		});
		logger.info(() -> "Polling for file took: " + (System.nanoTime() - start) / 1000000000 + " secs");
		return content.get();
	}

	// -----------------------------------------------------------------------
	// HELPER
	// -----------------------------------------------------------------------

	private void clearTopicKafka() {
		Properties properties = KafkaProperties.T.create().getKafkaProperties("TEST PROPS");
		try (AdminClient client = AdminClient.create(properties)) {
			client.deleteTopics(singletonList(TOPIC));
			logger.info(() -> "Removed test-topic, so no messages available");
		}
	}

	// Kafka Connectors
	private KafkaConsumerMessagingConnector createAndDeployKafkaConsumer() {
		KafkaConsumerMessagingConnector consumer = cortexSession.create(KafkaConsumerMessagingConnector.T);
		consumer.setExternalId(TEST_CONSUMER_ID);
		consumer.setName("TEST Consumer");
		consumer.setTopicsToListen(singletonList(TOPIC));
		consumer.setModule(module);
		consumer.setServiceUrls(singletonList(KAFKA_URL));
		consumer.setGroupInstanceId(TEST_CONSUMER_ID);
		consumer.setMaxPollIntervalMs(100);
		consumer.setGroupInstanceId("CONSUMER");
		cortexSession.commit();

		deploy(consumer.getExternalId());

		return consumer;
	}

	private KafkaProducerMessagingConnector createAndDeployKafkaProducer(boolean isWrongConnectionUrl) {
		KafkaProducerMessagingConnector producer = cortexSession.create(KafkaProducerMessagingConnector.T);
		producer.setExternalId(TEST_PRODUCER_ID);
		producer.setName("TEST Producer");
		producer.setTopicsToSend(singletonList(TOPIC));
		producer.setModule(module);
		producer.setServiceUrls(singletonList(isWrongConnectionUrl ? WRONG_URL : KAFKA_URL));
		producer.setGroupInstanceId("PRODUCER");

		cortexSession.commit();

		deploy(producer.getExternalId());

		return producer;
	}

	// Pulsar Connectors
	protected PulsarProducerMessagingConnector createAndDeployPulsarProducer(boolean isWrongConnectionUrl, String webPulsarUrl) {
		PulsarProducerMessagingConnector producer = cortexSession.create(PulsarProducerMessagingConnector.T);
		producer.setExternalId(TEST_PRODUCER_ID);
		producer.setName("TEST Producer");
		producer.setTopicsToSend(singletonList(TOPIC));
		producer.setModule(module);
		producer.setServiceUrls(singletonList(isWrongConnectionUrl ? WRONG_URL : PULSAR_URL));
		producer.setOperationTimeout(1);
		producer.setConnectionTimeout(40);
		producer.setWebServiceUrl(webPulsarUrl);

		cortexSession.commit();

		deploy(producer.getExternalId());

		return producer;
	}

	protected PulsarConsumerMessagingConnector createAndDeployPulsarConsumer(boolean isWrongConnectionUrl, String webPulsarUrl) {
		PulsarConsumerMessagingConnector consumer = cortexSession.create(PulsarConsumerMessagingConnector.T);
		consumer.setExternalId(TEST_CONSUMER_ID);
		consumer.setName("TEST Consumer");
		consumer.setTopicsToListen(singletonList(TOPIC));
		consumer.setModule(module);
		consumer.setServiceUrls(singletonList(isWrongConnectionUrl ? WRONG_URL : PULSAR_URL));
		consumer.setOperationTimeout(1);
		consumer.setConnectionTimeout(40);
		consumer.setReceiveTimeout(1);
		consumer.setWebServiceUrl(webPulsarUrl);
		cortexSession.commit();

		deploy(consumer.getExternalId());

		return consumer;
	}*/
}
