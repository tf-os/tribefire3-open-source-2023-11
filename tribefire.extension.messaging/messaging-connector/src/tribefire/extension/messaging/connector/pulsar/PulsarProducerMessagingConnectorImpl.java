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
package tribefire.extension.messaging.connector.pulsar;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.pulsar.client.api.CompressionType;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.ProducerAccessMode;
import org.apache.pulsar.client.api.ProducerStats;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.UnsatisfiedMaybeTunneling;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;

import tribefire.extension.messaging.connector.api.AbstractProducerMessagingConnector;
import tribefire.extension.messaging.model.deployment.connector.properties.PulsarProperties;
import tribefire.extension.messaging.model.deployment.event.EventEndpointConfiguration;
import tribefire.extension.messaging.service.reason.connection.PulsarConnectionError;
import tribefire.extension.messaging.service.reason.validation.ArgumentNotSatisfied;

public class PulsarProducerMessagingConnectorImpl extends AbstractProducerMessagingConnector {
	private static final Logger logger = Logger.getLogger(PulsarProducerMessagingConnectorImpl.class);
	private final Map<String, Producer<byte[]>> producers = new HashMap<>();

	private ClassLoader moduleClassLoader;
	private PulsarClient client;
	private final PulsarProperties properties;
	private final PulsarServerHealthChecker checker;

	public PulsarProducerMessagingConnectorImpl(EventEndpointConfiguration destinationConfig, ClassLoader moduleClassLoader) {
		if (destinationConfig == null) {
			throw new UnsatisfiedMaybeTunneling(Reasons.build(ArgumentNotSatisfied.T)
					.text(format("Empty configuration is provided for %s!", "PulsarProducerConnector")).toMaybe());
		}
		this.moduleClassLoader = moduleClassLoader;
		this.properties = PulsarProperties.T.create().apply(destinationConfig);
		this.checker = new PulsarServerHealthChecker().setModuleClassLoader(this.moduleClassLoader);
		construct();
	}

	@Override
	public void destroy() {
		try {
			this.client.close();
		} catch (PulsarClientException e) {
			throw new UnsatisfiedMaybeTunneling(
					Reasons.build(PulsarConnectionError.T).text("Error in closing").enrich(r -> r.addThrowableInformation(e)).toMaybe());
		}
	}

	@Override
	protected void deliverMessageString(byte[] message, Set<String> topics) {
		topics.forEach(t -> deliverOneTopic(message, t));
	}

	private void deliverOneTopic(byte[] message, String topic) {
		try (Producer<byte[]> producer = ensureNewProducerPerTopic(topic)) {
			MessageId messageId = producer.send(message);
			logger.info(() -> "Sent message '" + messageId + "' to topic: '" + topic + "'!");
		} catch (PulsarClientException e) {
			logger.error("Pulsar Producer " + e.getMessage());
			throw new UnsatisfiedMaybeTunneling(Reasons.build(PulsarConnectionError.T)
					.text("Error in delivering message to pulsar : '" + Arrays.toString(message) + "' with topic: '" + topic + "'")
					.enrich(r -> r.addThrowableInformation(e)).toMaybe());
		}
	}

	@Override
	public CheckResultEntry actualHealth() {
		CheckResultEntry result = checker.checkServer(properties);
		if (result.getCheckStatus() == CheckStatus.ok) {
			Map<Producer<byte[]>, ProducerStats> producerStats = new HashMap<>();
			producers.values().forEach(p -> producerStats.put(p, p.getStats()));
			//@formatter:off
			String metrics = producerStats.keySet().stream()
					.map(p -> getMetrics(producerStats, p))
					.collect(Collectors.joining("; "));
			//@formatter:on
			result.setDetails(result.getDetails() + "\nProvider metrics: " + metrics);
		}
		return result;
	}

	private String getMetrics(Map<Producer<byte[]>, ProducerStats> producerStats, Producer<byte[]> p) {
		return "Producer '" + p.getTopic() + "':\nNum Bytes Sent: " + producerStats.get(p).getNumBytesSent() + ", Num Msgs Sent: "
				+ producerStats.get(p).getNumMsgsSent();
	}

	private void construct() {
		ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			if (this.moduleClassLoader != null) {
				Thread.currentThread().setContextClassLoader(this.moduleClassLoader);
			}
			initializeVariables();
		} catch (PulsarClientException e) {
			throw new UnsatisfiedMaybeTunneling(Reasons.build(PulsarConnectionError.T).text(e.getMessage() + " " + e.getCause())
					.enrich(r -> r.addThrowableInformation(e)).toMaybe());
		} finally {
			Thread.currentThread().setContextClassLoader(origClassLoader);
		}
	}

	private void initializeVariables() throws PulsarClientException {
		//@formatter:off
		logger.info("SERVER_URL=" + properties.getServiceUrls().stream().findFirst().orElse(null));
		client = PulsarClient.builder()
				.serviceUrl(properties.getServiceUrls().stream().findFirst().orElse(null))
				.connectionTimeout(properties.getConnectionTimeout(), TimeUnit.SECONDS)
				.operationTimeout(properties.getOperationTimeout(), TimeUnit.SECONDS)
				.build();
		//@formatter:on
	}

	private Producer<byte[]> ensureNewProducerPerTopic(String topic) throws PulsarClientException {
		Producer<byte[]> producer = producers.get(topic);
		if (producer == null || !producer.isConnected()) {
			Lock lock = null;
			try {
				lock = new ReentrantLock();
				lock.lock();
				producer = producers.get(topic);
				if (producer == null || !producer.isConnected()) {
					//@formatter:off
					producer = client.newProducer()
							.topic(topic)
							.compressionType(CompressionType.valueOf(properties.getCompressionType()))
							.accessMode(ProducerAccessMode.valueOf(properties.getAccessMode()))
							.batchingMaxMessages(properties.getBatchingMaxMessages())
							.create();
					//@formatter:on
					producers.put(topic, producer);

				}
			} finally {
				if (lock != null) {
					lock.unlock();
				}
			}
		}
		return producer;
	}

	public void setModuleClassLoader(ClassLoader moduleClassLoader) {
		this.moduleClassLoader = moduleClassLoader;
	}

	@Override
	public String getExternalId() {
		return properties.getGlobalId();
	}
}
