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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.api.BatchReceivePolicy;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.ConsumerStats;
import org.apache.pulsar.client.api.Messages;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.SubscriptionType;

import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.UnsatisfiedMaybeTunneling;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;

import tribefire.extension.messaging.connector.api.AbstractConsumerMessagingConnector;
import tribefire.extension.messaging.model.deployment.connector.properties.PulsarProperties;
import tribefire.extension.messaging.model.deployment.event.EventEndpointConfiguration;
import tribefire.extension.messaging.service.reason.connection.PulsarConnectionError;
import tribefire.extension.messaging.service.reason.validation.ArgumentNotSatisfied;

public class PulsarConsumerMessagingConnectorImpl extends AbstractConsumerMessagingConnector {

	private static final Logger logger = Logger.getLogger(PulsarConsumerMessagingConnectorImpl.class);

	private ClassLoader moduleClassLoader;
	private PulsarClient pulsarClient;
	private Consumer<byte[]> pulsarConsumer;

	private PulsarProperties properties;
	private PulsarServerHealthChecker checker;

	public PulsarConsumerMessagingConnectorImpl(EventEndpointConfiguration destinationConfig, ClassLoader moduleClassLoader) {
		if (destinationConfig == null) {
			throw new UnsatisfiedMaybeTunneling(Reasons.build(ArgumentNotSatisfied.T)
					.text(format("Empty configuration is provided for %s!", "PulsarConsumerConnector")).toMaybe());
		}
		this.moduleClassLoader = moduleClassLoader;
		this.properties = PulsarProperties.T.create().apply(destinationConfig);
		this.checker = new PulsarServerHealthChecker().setModuleClassLoader(this.moduleClassLoader);
		construct();
	}

	@Override
	protected List<byte[]> consumerConsume() {
		try {
			List<byte[]> result = new ArrayList<>();
			Messages<byte[]> polled = pulsarConsumer.batchReceive();
			logger.info("Polled: " + polled.size() + " messages from: " + properties.getTopicsToListen());

			for (org.apache.pulsar.client.api.Message<byte[]> m : polled) {
				result.add(m.getData());
			}
			return result;
		} catch (PulsarClientException e) {
			throw new UnsatisfiedMaybeTunneling(Reasons.build(PulsarConnectionError.T).text("Pulsar: Error in polling messages from pulsar")
					.enrich(r -> r.addThrowableInformation(e)).toMaybe());
		}
	}

	@Override
	public void finalizeConsume() {
		if (pulsarConsumer != null) {
			try {
				this.pulsarConsumer.unsubscribe();
				this.pulsarConsumer.close();
			} catch (PulsarClientException e) {
				throw new UnsatisfiedMaybeTunneling(Reasons.build(PulsarConnectionError.T).text("Pulsar: Error in closing consumer")
						.enrich(r -> r.addThrowableInformation(e)).toMaybe());
			}
		}
		if (pulsarClient != null) {
			try {
				this.pulsarClient.close();
			} catch (PulsarClientException e) {
				throw new UnsatisfiedMaybeTunneling(Reasons.build(PulsarConnectionError.T).text("Pulsar: Error in closing client")
						.enrich(r -> r.addThrowableInformation(e)).toMaybe());
			}
		}
	}

	@Override
	public CheckResultEntry actualHealth() {
		CheckResultEntry result = checker.checkServer(properties);
		if (result.getCheckStatus() == CheckStatus.ok) {
			ConsumerStats stats = pulsarConsumer.getStats();
			String metrics = "Consumer:" + "\nNum Bytes Received: " + stats.getNumBytesReceived() + ", Num Msgs Received: "
					+ stats.getNumMsgsReceived();
			result.setDetails(result.getDetails() + "\nProvider metrics: " + metrics);
		}
		return result;
	}

	private void construct() {
		ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			if (this.moduleClassLoader != null) {
				Thread.currentThread().setContextClassLoader(this.moduleClassLoader);
			}
			initializeVariables();
		} catch (PulsarClientException e) {
			throw new UnsatisfiedMaybeTunneling(Reasons.build(PulsarConnectionError.T).text("Pulsar: Error in initialization...")
					.enrich(r -> r.addThrowableInformation(e)).toMaybe());
		} finally {
			Thread.currentThread().setContextClassLoader(origClassLoader);
		}
	}

	private void initializeVariables() throws PulsarClientException {
		//@formatter:off
		pulsarClient = PulsarClient.builder()
				.serviceUrl(properties.getServiceUrls().stream().findFirst().orElse(null))
				.connectionTimeout(properties.getConnectionTimeout(), TimeUnit.SECONDS)
				.operationTimeout(properties.getOperationTimeout(), TimeUnit.SECONDS)
				.build();
		pulsarConsumer = pulsarClient.newConsumer()
				.topics(properties.getTopicsToListen())
				.subscriptionType(SubscriptionType.valueOf(properties.getSubscriptionType()))
				.subscriptionName(properties.getDefaultSubscriptionName())
				.batchReceivePolicy(BatchReceivePolicy.builder()
						                    .timeout(properties.getReceiveTimeout(), TimeUnit.SECONDS)
						                    .maxNumMessages(properties.getMaxNumMessages()).build())
				.subscribe();
		//@formatter:on
	}

	@Override
	public String getExternalId() {
		return properties.getGlobalId();
	}
}
