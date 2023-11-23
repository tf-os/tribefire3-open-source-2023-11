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
package com.braintribe.transport.messaging.etcd;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.braintribe.logging.Logger;
import com.braintribe.model.messaging.etcd.EtcdMessageEnvelope;
import com.braintribe.model.messaging.etcd.EtcdMessaging;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.utils.StringTools;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.common.exception.ClosedClientException;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.options.WatchOption.Builder;
import io.etcd.jetcd.watch.WatchEvent;

public abstract class AbstractListenerWorker implements Runnable {

	private static final Logger logger = Logger.getLogger(AbstractListenerWorker.class);

	protected boolean run = true;

	protected EtcdMessaging providerConfiguration;
	protected Client client;
	protected KV kvClient;
	protected EtcdConnection connection;
	protected MessagingContext messagingContext;

	protected AtomicInteger totalCount = new AtomicInteger(0);

	private Watch.Watcher watcher;
	private Watch watch;

	public AbstractListenerWorker(EtcdMessaging providerConfiguration, MessagingContext messagingContext, Client client, KV kvClient,
			EtcdConnection etcdConnection) {
		this.providerConfiguration = providerConfiguration;
		this.client = client;
		this.kvClient = kvClient;
		this.connection = etcdConnection;
		this.messagingContext = messagingContext;
	}

	@Override
	public void run() {

		String encodedKey = connection.getMessagingKeyPrefix().concat(getDestinationType());

		ByteSequence bsWaitKey = ByteSequence.from(encodedKey, StandardCharsets.UTF_8);
		AtomicLong nextEtcdIndex = new AtomicLong(-1);
		int messagesReceived = 0;

		if (this.getDestinationType().equals("topic")) {
			try {
				nextEtcdIndex.set(connection.getModificationCount(encodedKey));
				if (nextEtcdIndex.longValue() != -1) {
					nextEtcdIndex.incrementAndGet();
				}
			} catch (Exception e1) {
				logger.warn("Error while trying to get the current modification count for key " + encodedKey, e1);
			}
		}

		watch = client.getWatchClient();

		String myWorkerId = UUID.randomUUID().toString();

		try {

			Builder watchBuilder = WatchOption.newBuilder().withNoDelete(true);
			if (nextEtcdIndex.longValue() != -1) {
				watchBuilder.withRevision(nextEtcdIndex.longValue());
			}
			watchBuilder.withRange(EtcdConnection.getRangeEnd(encodedKey)).withProgressNotify(false);
			WatchOption option = watchBuilder.build();

			watcher = watch.watch(bsWaitKey, option, response -> {

				Runnable r = () -> {

					for (WatchEvent event : response.getEvents()) {

						KeyValue keyValue = event.getKeyValue();

						long modRevision = keyValue.getModRevision();
						nextEtcdIndex.set(modRevision + 1);

						String stringKey = Optional.ofNullable(event.getKeyValue().getKey()).map(k -> k.toString(StandardCharsets.UTF_8)).orElse("");
						byte[] valueBytes = Optional.ofNullable(event.getKeyValue().getValue()).map(ByteSequence::getBytes).orElse(null);

						logger.trace(() -> "Received event. Key: " + stringKey + ", modRevision: " + modRevision);

						try {
							if (valueBytes != null) {

								ReceivedMessageContext context = new ReceivedMessageContext(stringKey, valueBytes, messagingContext);
								context.setModRevision(modRevision);

								if (connection.subscribedConsumers(context)) {
									if (messageAcceptable(context)) {
										if (acceptMessage(context, myWorkerId)) {
											totalCount.incrementAndGet();
											connection.messageReceived(context, getDestinationType());
											logger.trace(() -> StringTools.asciiBoxMessage(
													"Worker " + myWorkerId + " received msg " + messagesReceived + " (" + stringKey + ")", -1));
										} else {
											logger.trace(() -> "Event for key " + stringKey + " was not accepted.");
										}
									} else {
										logger.trace(() -> "Message " + context + " is not acceptable.");
									}
								} else {
									logger.trace(() -> "Event for key " + stringKey + " will be dropped as there is no consumer subscribed.");
								}
							} else {
								logger.trace(() -> "Event contained no value bytes for key " + stringKey);
							}
						} catch (Exception e) {

							StringBuilder context = new StringBuilder();
							context.append("String key: ");
							context.append(stringKey);
							context.append("Message: ");
							if (valueBytes == null) {
								context.append("<null>");
							} else if (valueBytes.length == 0) {
								context.append("<empty>");
							} else {
								try {
									String bytesBase64Encoded = Base64.getEncoder().encodeToString(valueBytes);
									context.append("Base64 encoded: [");
									context.append(bytesBase64Encoded);
									context.append(']');
								} catch (Exception e2) {
									logger.debug(() -> "Could not Base64-encode the content of the message.", e2);
								}
								try {
									String letsGiveItATry = new String(valueBytes, "UTF-8");
									String cleaned = StringTools.removeNonPrintableCharacters(letsGiveItATry);
									if (cleaned != null) {
										context.append("\nString content:\n");
										context.append(StringTools.asciiBoxMessage(cleaned, -1));
									}
								} catch (Exception e2) {
									logger.debug(() -> "Could not Base64-encode the content of the message.", e2);
								}
							}

							logger.error("Error while trying to forward message to receiver. Context: " + context.toString(), e);
						}
					}
				};
				Thread.ofVirtual().name("Event handler").start(r);

			});

		} catch (ClosedClientException cce) {
			watch.close();
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected boolean messageAcceptable(ReceivedMessageContext context) {

		if (isExpired(context)) {
			logger.debug(() -> "Message " + context + " has expired.");
			return false;
		}
		if (!matchesAddressee(context)) {
			logger.trace(() -> "Message addressee of " + context + " does not match this address.");
			return false;
		}
		return true;
	}

	protected boolean isExpired(final ReceivedMessageContext context) {

		EtcdMessageEnvelope envelope = context.getEnvelope();

		Long expiration = envelope.getExpiration();

		if (expiration != null && expiration > 0 && expiration < System.currentTimeMillis()) {
			logger.debug(() -> "Consumer received an expired message: " + context);
			return true;
		} else {
			logger.trace(() -> "Consumer received an non-expired message: " + context);
			return false;
		}

	}

	protected boolean matchesAddressee(final ReceivedMessageContext context) {

		EtcdMessageEnvelope envelope = context.getEnvelope();
		String addresseeAppId = envelope.getAddresseeAppId();
		String addresseeNodeId = envelope.getAddresseeNodeId();

		String myAppId = messagingContext.getApplicationId();
		String myNodeId = messagingContext.getNodeId();

		logger.trace(() -> "Received message for node " + addresseeNodeId + ", application " + addresseeAppId + ", local instanceId: " + myNodeId
				+ "@" + myAppId + ": " + context);

		if (addresseeNodeId == null || addresseeNodeId.equals(myNodeId)) {
			if (addresseeAppId == null || addresseeAppId.equals(myAppId)) {
				return true;
			}
		}
		return false;
	}

	protected abstract boolean acceptMessage(ReceivedMessageContext context, String myWorkerId) throws Exception;

	protected abstract String getDestinationType();

	public void stop() {
		run = false;
		if (watcher != null) {
			logger.debug(() -> "Closing watcher.");
			try {
				watcher.close();
			} catch (Exception e) {
				logger.trace(() -> "Error while trying to close watcher.", e);
			}
		}
		if (watch != null) {
			logger.debug(() -> "Closing watch.");
			try {
				watch.close();
			} catch (Exception e) {
				logger.trace(() -> "Error while trying to close watch.", e);
			}
		}
	}
}
