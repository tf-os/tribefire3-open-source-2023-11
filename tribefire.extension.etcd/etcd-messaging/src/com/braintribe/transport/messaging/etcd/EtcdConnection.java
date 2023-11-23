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

import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.braintribe.common.lcd.GenericRuntimeException;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.exception.Exceptions;
import com.braintribe.execution.CountingThreadFactory;
import com.braintribe.execution.ExtendedThreadPoolExecutor;
import com.braintribe.logging.Logger;
import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Topic;
import com.braintribe.model.messaging.etcd.ComposeEtcdMessaging;
import com.braintribe.model.messaging.etcd.EtcdMessaging;
import com.braintribe.transport.messaging.api.MessagingComponentStatus;
import com.braintribe.transport.messaging.api.MessagingConnection;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.Arguments;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.op.Cmp;
import io.etcd.jetcd.op.CmpTarget;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.GetOption.SortOrder;
import io.etcd.jetcd.options.GetOption.SortTarget;
import io.etcd.jetcd.options.PutOption;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

/**
 * <p>
 * {@link MessagingConnection} implementation representing a connection to a Pub/Sub server.
 * 
 * @author roman.kurmanowytsch
 */
public class EtcdConnection implements MessagingConnection {

	private static final Logger logger = Logger.getLogger(EtcdConnection.class);

	public static final String ETCD_CLIENT_CERTIFICATE = "ETCD_CLIENT_CERTIFICATE";

	public final static String messagingPrefix = "tfmsg";

	private MessagingContext messagingContext;
	private EtcdMessaging providerConfiguration;

	private MessagingComponentStatus status = MessagingComponentStatus.NEW;

	protected Set<EtcdMessagingSession> sessions = new HashSet<>();
	protected ReentrantLock sessionsLock = new ReentrantLock();

	private Set<EtcdMessageProducer> messageProducers = new HashSet<>();
	private ReentrantLock messageProducersLock = new ReentrantLock();
	private Set<EtcdMessageConsumer> messageConsumers = new HashSet<>();
	private ReentrantLock messageConsumersLock = new ReentrantLock();

	protected Client client;
	protected KV kvClient;
	protected AbstractListenerWorker listenerQueues;
	protected Thread listenerThreadQueues;
	protected AbstractListenerWorker listenerTopics;
	protected Thread listenerThreadTopics;
	protected static AtomicInteger listenerThreadCounter = new AtomicInteger(0);

	protected Map<String, Set<EtcdMessageConsumer>> destinationConsumers = new HashMap<>();
	private ReentrantLock destinationConsumersLock = new ReentrantLock();

	protected ExtendedThreadPoolExecutor executor = null;

	public EtcdConnection(com.braintribe.model.messaging.etcd.EtcdMessaging providerConfiguration) {
		this.providerConfiguration = providerConfiguration;
	}

	@Override
	public void open() throws MessagingException {
		if (status == MessagingComponentStatus.CLOSING || status == MessagingComponentStatus.CLOSED) {
			throw new MessagingException("Connection in unexpected state: " + status.toString().toLowerCase());
		}
		if (status == MessagingComponentStatus.OPEN) {
			// opening an already opened connection shall be a no-op
			if (logger.isTraceEnabled()) {
				logger.trace("open() called in an already opened connection. Connection already established.");
			}
			return;
		}

		if (executor == null) {
			executor = new ExtendedThreadPoolExecutor(0, Integer.MAX_VALUE, 1L, TimeUnit.MINUTES, new SynchronousQueue<>(),
					new CountingThreadFactory("etcd-connection-threadpool"));
		}

		List<String> endpointUrls = providerConfiguration.getEndpointUrls();
		String username = providerConfiguration.getUsername();
		String password = providerConfiguration.getPassword();

		List<URI> endpointUris = endpointUrls.stream().map(u -> {
			try {
				return new URI(u);
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}).collect(Collectors.toList());

		if (ComposeEtcdMessaging.T.isInstance(providerConfiguration)) {
			ComposeEtcdMessaging composeProviderConfiguration = (ComposeEtcdMessaging) providerConfiguration;
			String authority = composeProviderConfiguration.getAuthority();
			String authorityPrefix = composeProviderConfiguration.getAuthorityPrefix();
			String certificate = composeProviderConfiguration.getCertificate();
			if (CommonTools.isEmpty(certificate)) {
				// read certificate from environment
				certificate = System.getenv(ETCD_CLIENT_CERTIFICATE);
				if (CommonTools.isEmpty(certificate)) {
					throw new IllegalStateException("No certificate is set from the environment ('" + ETCD_CLIENT_CERTIFICATE
							+ "') or via configuration for endpointUrls: '" + StringTools.createStringFromCollection(endpointUrls, ",")
							+ "' username: '" + username + "' authority: '" + authority + "' authorityPrefix: '" + authorityPrefix + "'");
				}
			}
			Arguments.notEmptyWithNames(authority, "'authority' must not be empty", authorityPrefix, "'authorityPrefix' must not be empty", username,
					"'username' must not be empty", password, "'password' must not be empty");

			logger.debug(() -> "Prepare client for authority: '" + authority + "' authorityPrefix: '" + authorityPrefix + "' username: '" + username
					+ "' endpointUrls: '" + StringTools.createStringFromCollection(endpointUrls, ",") + "'");

			SslContext sslContext;

			try {
				try (InputStream certificateInputStream = StringTools.toInputStream(certificate)) {
					//@formatter:off
					sslContext = SslContextBuilder.forClient()
							.trustManager(certificateInputStream)
							.applicationProtocolConfig(new ApplicationProtocolConfig(
									ApplicationProtocolConfig.Protocol.ALPN,
									SelectorFailureBehavior.NO_ADVERTISE,
									SelectedListenerFailureBehavior.ACCEPT,
									ApplicationProtocolNames.HTTP_2,
									ApplicationProtocolNames.HTTP_1_1))
							.build();
					//@formatter:on
				}
			} catch (Exception e) {
				throw new GenericRuntimeException("Could not create SslContext for certificate: '" + certificate + "'", e);
			}

			ByteSequence bsUsername = ByteSequence.from(username, StandardCharsets.UTF_8);
			ByteSequence bsPassword = ByteSequence.from(password, StandardCharsets.UTF_8);

			// pick first endpoint - assume all have same sub domain
			String firstEndpoint = endpointUrls.get(0);
			URL url;
			try {
				url = new URL(firstEndpoint);
			} catch (MalformedURLException e) {
				throw new UncheckedIOException("Could not create URL from first endpoint: '" + firstEndpoint + "'", e);
			}
			String host = url.getHost();
			String subDomain = host.replaceFirst("^.*?\\.", "");

			//@formatter:off
			client = Client.builder()
						.authority(authorityPrefix + subDomain) //works with each portal subdomain but also with the root of the according portal 
						.endpoints(endpointUris)
						.user(bsUsername)
						.password(bsPassword)
						.sslContext(sslContext)
						.maxInboundMessageSize((int) Numbers.MEGABYTE * 100)
					.build();
			//@formatter:on		
		} else {
			if (!StringTools.isBlank(username) && !StringTools.isBlank(password)) {

				ByteSequence bsUsername = ByteSequence.from(username, StandardCharsets.UTF_8);
				ByteSequence bsPassword = ByteSequence.from(password, StandardCharsets.UTF_8);

				// client = Client.builder().endpoints(endpointUris).user(bsUsername).password(bsPassword).build();

				client = Client.builder().maxInboundMessageSize((int) Numbers.MEGABYTE * 100).endpoints(endpointUris).user(bsUsername)
						.password(bsPassword).build();
			} else {
				client = Client.builder().maxInboundMessageSize((int) Numbers.MEGABYTE * 100).endpoints(endpointUris).build();
			}
		}

		kvClient = client.getKVClient();

		this.status = MessagingComponentStatus.OPEN;
	}

	@Override
	public void close() throws MessagingException {
		this.status = MessagingComponentStatus.CLOSING;

		if (executor != null) {
			executor.shutdown();
			executor = null;
		}

		messageProducersLock.lock();
		try {
			while (!messageProducers.isEmpty()) {
				Iterator<EtcdMessageProducer> iterator = messageProducers.iterator();
				EtcdMessageProducer mp = iterator.next();
				iterator.remove();
				try {
					mp.close();
				} catch (Exception e) {
					logger.debug(() -> "Error while closing message producer: " + mp, e);
				}
			}
		} finally {
			messageProducersLock.unlock();
		}
		messageConsumersLock.lock();
		try {
			while (!messageConsumers.isEmpty()) {
				Iterator<EtcdMessageConsumer> iterator = messageConsumers.iterator();
				EtcdMessageConsumer mc = iterator.next();
				iterator.remove();
				try {
					mc.close();
				} catch (Exception e) {
					logger.debug(() -> "Error while closing message consumer: " + mc, e);
				}
			}
		} finally {
			messageConsumersLock.unlock();
		}

		sessionsLock.lock();
		try {
			for (EtcdMessagingSession session : sessions) {
				try {
					session.close();
				} catch (Exception e) {
					logger.error("Error while trying to close session: " + session, e);
				}
			}
		} finally {
			sessionsLock.unlock();
		}

		if (listenerQueues != null) {
			listenerQueues.stop();
			if (listenerThreadQueues != null) {
				try {
					listenerThreadQueues.interrupt();
				} catch (Exception e) {
					logger.trace(() -> "Error while trying to interrupt queue listener thread");
				}
			}
		}
		if (listenerTopics != null) {
			listenerTopics.stop();
			if (listenerThreadTopics != null) {
				try {
					listenerThreadTopics.interrupt();
				} catch (Exception e) {
					logger.trace(() -> "Error while trying to interrupt topic listener thread");
				}
			}
		}

		IOTools.closeCloseable(kvClient, logger);
		IOTools.closeCloseable(client, logger);

		kvClient = null;
		client = null;

		this.status = MessagingComponentStatus.CLOSED;
	}

	@Override
	public MessagingSession createMessagingSession() throws MessagingException {

		open();

		EtcdMessagingSession session = new EtcdMessagingSession(providerConfiguration);
		session.setMessagingContext(messagingContext);
		session.setConnection(this);

		sessionsLock.lock();
		try {
			sessions.add(session);
		} finally {
			sessionsLock.unlock();
		}
		return session;
	}

	public void setMessagingContext(MessagingContext messagingContext) {
		this.messagingContext = messagingContext;
	}

	protected void registerConsumer(EtcdMessageConsumer consumer) {
		messageConsumersLock.lock();
		try {
			if (listenerQueues == null) {
				listenerQueues = new QueueListenerWorker(providerConfiguration, messagingContext, client, kvClient, this);
				listenerThreadQueues = Thread.ofVirtual().name("Etcd Messaging Queue Listener " + listenerThreadCounter.incrementAndGet())
						.start(listenerQueues);
			}
			if (listenerTopics == null) {
				listenerTopics = new TopicListenerWorker(providerConfiguration, messagingContext, client, kvClient, this);
				listenerThreadTopics = Thread.ofVirtual().name("Etcd Messaging Topic Listener " + listenerThreadCounter.incrementAndGet())
						.start(listenerTopics);
			}
			messageConsumers.add(consumer);
		} finally {
			messageConsumersLock.unlock();
		}
	}

	protected void registerProducer(EtcdMessageProducer producer) {
		messageProducersLock.lock();
		try {
			messageProducers.add(producer);
		} finally {
			messageProducersLock.unlock();
		}
	}

	public void unregisterMessageProducer(EtcdMessageProducer messageProducer) {
		messageProducersLock.lock();
		try {
			messageProducers.remove(messageProducer);
		} finally {
			messageProducersLock.unlock();
		}
	}

	public void unregisterMessageConsumer(EtcdMessageConsumer messageConsumer) {
		messageConsumersLock.lock();
		try {
			messageConsumers.remove(messageConsumer);
		} finally {
			messageConsumersLock.unlock();
		}
	}

	protected void put(String key, String value, int ttl) throws Exception {
		ByteSequence bsKey = ByteSequence.from(key, StandardCharsets.UTF_8);
		ByteSequence bsValue = ByteSequence.from(value, StandardCharsets.UTF_8);

		put(bsKey, bsValue, ttl);
	}

	protected void put(String key, byte[] value, int ttl) throws Exception {
		ByteSequence bsKey = ByteSequence.from(key, StandardCharsets.UTF_8);
		ByteSequence bsValue = ByteSequence.from(value);

		put(bsKey, bsValue, ttl);
	}

	protected void put(ByteSequence bsKey, ByteSequence bsValue, int ttl) throws Exception {

		// put the key-value
		if (ttl > 0) {
			LeaseGrantResponse leaseGrantResponse = client.getLeaseClient().grant(ttl).get();
			long leaseId = leaseGrantResponse.getID();
			PutOption putOption = PutOption.newBuilder().withLeaseId(leaseId).build();
			kvClient.put(bsKey, bsValue, putOption).get();
			// kvClient.put(bsKey, bsValue).get();
		} else {
			kvClient.put(bsKey, bsValue).get();
		}

		// get the CompletableFuture
		kvClient.get(bsKey).get();

	}

	protected GetResponse get(String key) throws Exception {

		ByteSequence bsKey = ByteSequence.from(key, StandardCharsets.UTF_8);

		kvClient.get(bsKey).get();

		CompletableFuture<GetResponse> getFuture = kvClient.get(bsKey);

		// get the value from CompletableFuture
		GetResponse response = getFuture.get();

		return response;

	}

	protected static String getResponseValue(GetResponse response) {

		if (response.getKvs().isEmpty()) {
			// key does not exist
			return null;
		}
		String result = response.getKvs().get(0).getValue().toString(StandardCharsets.UTF_8);
		return result;

	}

	protected long getModificationCount(String key) throws Exception {

		ByteSequence bsKey = ByteSequence.from(key, StandardCharsets.UTF_8);
		GetOption getOption = GetOption.newBuilder().withRange(getRangeEnd(key)).build();

		GetResponse getResponse = kvClient.get(bsKey, getOption).get();
		List<KeyValue> kvs = getResponse.getKvs();
		if (kvs != null && !kvs.isEmpty()) {
			return kvs.get(0).getModRevision();
		}

		return -1;
	}

	public static ByteSequence getRangeEnd(String key) {
		int max = key.length() - 1;
		if (max < 0) {
			return ByteSequence.from(new byte[] { 1 });
		}
		String excludeLast = key.substring(0, max);
		String rangeEndString = excludeLast + new String(new char[] { (char) (key.charAt(max) + 1) });
		ByteSequence endKey = ByteSequence.from(rangeEndString, StandardCharsets.UTF_8);
		return endKey;
	}

	@SuppressWarnings("unused")
	protected boolean atomicLockAndDelete(String key, long modRevision, String workerId, int ttl) throws Exception {

		String lockKey = messagingPrefix + "/lock/" + key;

		ByteSequence bsLockKey = ByteSequence.from(lockKey, StandardCharsets.UTF_8);
		ByteSequence bsDelKey = ByteSequence.from(key, StandardCharsets.UTF_8);
		ByteSequence bsValue = ByteSequence.from(workerId, StandardCharsets.UTF_8);

		LeaseGrantResponse leaseGrantResponse = client.getLeaseClient().grant(ttl).get();
		long leaseId = leaseGrantResponse.getID();
		PutOption putOption = PutOption.newBuilder().withLeaseId(leaseId).build();
		// PutOption putOption = PutOption.newBuilder().build();

		GetOption getOption = GetOption.newBuilder().withSortField(SortTarget.MOD).withSortOrder(SortOrder.ASCEND).build();

		DeleteOption delOption = DeleteOption.newBuilder().build();

		Cmp condition = new Cmp(bsLockKey, Cmp.Op.EQUAL, CmpTarget.version(0));
		Op update = Op.put(bsLockKey, bsValue, putOption);
		Op get = Op.get(bsLockKey, getOption);
		Op del = Op.delete(bsDelKey, delOption);

		TxnResponse txnResponse = client.getKVClient().txn().If(condition).Then(update).Then(get).Then(del).commit().get();

		List<GetResponse> responses = txnResponse.getGetResponses();
		if (responses == null || responses.isEmpty()) {
			return false;
		}
		List<KeyValue> kvs = responses.get(0).getKvs();
		ByteSequence newValue = kvs.get(0).getValue();
		String newValueString = newValue.toString(StandardCharsets.UTF_8);

		return workerId.equals(newValueString);
	}

	protected String getMessagingKeyPrefix() {
		StringBuilder sb = new StringBuilder();
		if (!StringTools.isBlank(messagingPrefix)) {
			sb.append(encode(messagingPrefix));
			sb.append('/');
		}
		String projectId = providerConfiguration.getProject();
		if (!StringTools.isBlank(projectId)) {
			sb.append(encode(projectId));
			sb.append('/');
		}
		return sb.toString();
	}
	protected String getDestinationKey(Destination dest) {
		StringBuilder sb = new StringBuilder(getMessagingKeyPrefix());
		if (dest instanceof Topic) {
			sb.append("topic/");
		} else {
			sb.append("queue/");
		}
		String name = dest.getName();
		String encodedName = encode(name);
		sb.append(encodedName);
		return sb.toString();
	}

	protected String encode(String text) {
		try {
			return URLEncoder.encode(text, "UTF-8");
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not encode " + text);
		}
	}

	public boolean subscribedConsumers(ReceivedMessageContext context) {

		logger.trace(() -> "Getting consumers for " + context);

		destinationConsumersLock.lock();
		try {
			Set<EtcdMessageConsumer> set = destinationConsumers.get(context.getDestination());
			if (set != null && !set.isEmpty()) {
				context.setRegisteredConsumers(set);
				return true;
			}
		} finally {
			destinationConsumersLock.unlock();
		}

		return false;
	}

	public void messageReceived(ReceivedMessageContext context, String type) {

		logger.trace(() -> "Received message " + context);

		String destination = context.getDestination();
		Message unmarshalledMessage = context.getUnmarshalledMessage();

		// Prevent ConcurrentModificationException
		Set<EtcdMessageConsumer> consumers = new HashSet<>(context.getRegisteredConsumers());
		for (EtcdMessageConsumer consumer : consumers) {
			logger.trace(() -> "Submitting message " + context + " to executor.");
			executor.submit(() -> consumer.receivedMessage(destination, unmarshalledMessage));
			if (type.equals("queue")) {
				break;
			}
		}
	}

	protected void subscribe(String destinationName, EtcdMessageConsumer consumer) {
		destinationConsumersLock.lock();
		try {
			Set<EtcdMessageConsumer> set = destinationConsumers.computeIfAbsent(destinationName, d -> new HashSet<EtcdMessageConsumer>());
			set.add(consumer);
		} finally {
			destinationConsumersLock.unlock();
		}
	}
	protected void unsubscribe(String destinationName, EtcdMessageConsumer consumer) {
		destinationConsumersLock.lock();
		try {
			Set<EtcdMessageConsumer> set = destinationConsumers.get(destinationName);
			if (set != null) {
				set.remove(consumer);
			}
		} finally {
			destinationConsumersLock.unlock();
		}
	}

	@Override
	public String toString() {
		if (providerConfiguration == null) {
			return "<undefined>";
		} else {
			List<String> endpointUrls = providerConfiguration.getEndpointUrls();
			return StringTools.join(", ", endpointUrls);
		}
	}
}
