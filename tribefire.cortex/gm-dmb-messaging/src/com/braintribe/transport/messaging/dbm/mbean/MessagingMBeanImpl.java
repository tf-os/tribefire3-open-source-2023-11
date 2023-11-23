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
package com.braintribe.transport.messaging.dbm.mbean;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.DynamicMBean;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.StandardMBean;

import com.braintribe.execution.virtual.VirtualThreadExecutorBuilder;

/**
 * <p>
 * A basic {@link StandardMBean} pseudo-implementation of {@link MessagingMBean}.
 * 
 */
public class MessagingMBeanImpl extends StandardMBean {

	private ObjectName objectName;

	private Map<String, BlockingQueue<MBeanMessage>> queueMessages = new ConcurrentHashMap<>();
	private Map<String, Map<String, BlockingQueue<MBeanMessage>>> topicMessages = new ConcurrentHashMap<>();

	private Long connectionsIdSequence = 0L;
	private Map<Long, Long> openConnections = new HashMap<>();
	private Map<ClassLoader, Integer> openConnectionsPerClassLoader = new HashMap<>();

	private ExecutorService reaperExecutor;
	private Future<Void> reaperExecutorFuture;
	private ClassLoader reaperClassLoader;
	private ThreadFactory reaperExecutorThreadFactory = new NamedPoolThreadFactory(reaperExecutorName);

	private static final long reaperExecutorReturnTimeout = 2;
	private static final TimeUnit reaperExecutorReturnTimeoutUnit = TimeUnit.SECONDS;
	private static final String reaperExecutorName = "tribefire.mbean.messaging.reaper";
	private static final long reaperExecutorInterval = 20 * 60 * 1000;

	private static final long retainedWarningLimit = 100000;

	private static final Logger log = Logger.getLogger(MessagingMBeanImpl.class.getName());

	public MessagingMBeanImpl() throws NotCompliantMBeanException {
		super(DynamicMBean.class);
	}

	@Override
	public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
		ObjectName preRegisteredObjectName = super.preRegister(server, getObjectName());

		if (log.isLoggable(Level.FINER)) {
			log.exiting(this.getClass().getName(), "preRegister(MBeanServer, ObjectName)");
		}

		return preRegisteredObjectName;
	}

	@Override
	public void preDeregister() throws Exception {

		super.preDeregister();

		if (log.isLoggable(Level.FINER)) {
			log.exiting(this.getClass().getName(), "preDeregister()");
		}
	}

	@Override
	public void postRegister(Boolean registrationDone) {

		super.postRegister(registrationDone);

		if (log.isLoggable(Level.FINER)) {
			log.exiting(this.getClass().getName(), "postRegister()");
		}
	}

	@Override
	public void postDeregister() {
		super.postDeregister();

		if (log.isLoggable(Level.FINER)) {
			log.exiting(this.getClass().getName(), "postDeregister()");
		}
	}

	@Override
	public Object invoke(String actionName, Object params[], String signature[]) throws MBeanException, ReflectionException {

		switch (actionName) {

			case "connect":
				return connect();

			case "disconnect":
				disconnect((Long) params[0]);
				return null;

			case "getQueue":
				return getQueue((char) params[0], (String) params[1], (String) params[2]);

			case "subscribeTopicConsumer":
				return subscribeTopicConsumer((String) params[0], (String) params[1]);

			case "unsubscribeTopicConsumer":
				return unsubscribeTopicConsumer((String) params[0], (String) params[1]);

			case "sendMessage":
				sendMessage((char) params[0], (String) params[1], (String) params[2], (byte[]) params[3], (int) params[4], (long) params[5],
						(Map<String, Object>) params[6], (Map<String, Object>) params[7]);
				return null;

			default:
				return super.invoke(actionName, params, signature);

		}

	}

	/**
	 * @see MessagingMBean#connect()
	 */
	public Long connect() {

		Long connectionId;

		synchronized (openConnections) {

			connectionId = connectionsIdSequence++;

			openConnections.put(connectionId, System.currentTimeMillis());

			switchExecutorServices(connectionId, true);

			if (log.isLoggable(Level.FINE)) {
				log.fine("Connection [ #" + connectionId + " ] opened. Total active connections: [ " + openConnections.size() + " ] ");
			}

		}

		return connectionId;
	}

	/**
	 * @see MessagingMBean#disconnect(Long)
	 */
	public void disconnect(Long connectionId) {

		synchronized (openConnections) {

			openConnections.remove(connectionId);

			switchExecutorServices(connectionId, false);

			if (log.isLoggable(Level.FINE)) {
				log.fine("Connection[ #" + connectionId + " ] closed. Total active connections: [ " + openConnections.size() + " ] ");
			}

		}

	}

	/**
	 * @see MessagingMBean#getQueue(char, String, String)
	 */
	public BlockingQueue<MBeanMessage> getQueue(char destinationType, String destinationName, String subscriptionId) {
		return requireMessagesQueue(destinationType, destinationName, subscriptionId);
	}

	/**
	 * @see MessagingMBean#subscribeTopicConsumer(String, String)
	 */
	public boolean subscribeTopicConsumer(String destinationName, String subscriptionId) {

		requireTopicMessagesQueue(destinationName, subscriptionId);

		return true;

	}

	/**
	 * @see MessagingMBean#unsubscribeTopicConsumer(String, String)
	 */
	public boolean unsubscribeTopicConsumer(String destinationName, String subscriptionId) {

		Map<String, BlockingQueue<MBeanMessage>> destinationRepo = topicMessages.get(destinationName);

		if (destinationRepo != null) {
			synchronized (topicMessages) {
				topicMessages.remove(subscriptionId);
			}
		}

		return true;
	}

	/**
	 * @see MessagingMBean#sendMessage(char, String, String, byte[], int, long, Map, Map)
	 */
	public void sendMessage(char destinationType, String destinationName, String messageId, byte[] message, int priority, long expiration,
			Map<String, Object> headers, Map<String, Object> properties) {

		MBeanMessage mbeanMessage = new MBeanMessage(destinationType, destinationName, messageId, message, priority, expiration, headers, properties);

		switch (destinationType) {
			case 't':
				sendTopicMessage(destinationName, mbeanMessage);
				break;
			case 'q':
				sendQueueMessage(destinationName, mbeanMessage);
				break;
			default:
				throw new IllegalArgumentException("Invalid destination type " + destinationType);
		}

	}

	protected void sendTopicMessage(String topicName, MBeanMessage mbeanMessage) {

		Map<String, BlockingQueue<MBeanMessage>> destinationRepo = topicMessages.get(topicName);

		if (destinationRepo == null) {
			return;
		}

		for (Map.Entry<String, BlockingQueue<MBeanMessage>> entry : destinationRepo.entrySet()) {
			entry.getValue().add(mbeanMessage);
		}

	}

	protected void sendQueueMessage(String queueName, MBeanMessage mbeanMessage) {
		requireMessagesQueue('q', queueName, null).add(mbeanMessage);
	}

	/**
	 * <p>
	 * Returns the {@link ObjectName} for this MBean registration.
	 * 
	 * @return The {@link ObjectName} for this MBean registration.
	 */
	protected ObjectName getObjectName() throws Exception {

		if (objectName == null) {
			objectName = new ObjectName("com.braintribe.tribefire:type=MessagingMBean");
		}

		return objectName;
	}

	private BlockingQueue<MBeanMessage> requireMessagesQueue(char destinationType, String destinationName, String subscriptionId) {

		switch (destinationType) {
			case 'q':
				return requireQueueMessagesQueue(destinationName);
			case 't':
				return requireTopicMessagesQueue(destinationName, subscriptionId);
			default:
				throw new IllegalArgumentException("Unknown type of destination: " + destinationType);
		}

	}

	private BlockingQueue<MBeanMessage> requireQueueMessagesQueue(String destinationName) {

		BlockingQueue<MBeanMessage> destinationRepo = queueMessages.get(destinationName);

		if (destinationRepo == null) {
			synchronized (queueMessages) {
				destinationRepo = queueMessages.get(destinationName);
				if (destinationRepo == null) {
					destinationRepo = new PriorityBlockingQueue<MBeanMessage>();
					queueMessages.put(destinationName, destinationRepo);
				}
			}
		}

		return destinationRepo;

	}

	private BlockingQueue<MBeanMessage> requireTopicMessagesQueue(String destinationName, String subscriptionId) {

		Map<String, BlockingQueue<MBeanMessage>> destinationRepo = topicMessages.get(destinationName);

		if (destinationRepo == null) {
			synchronized (topicMessages) {
				destinationRepo = topicMessages.get(destinationName);
				if (destinationRepo == null) {
					destinationRepo = new ConcurrentHashMap<String, BlockingQueue<MBeanMessage>>();
					topicMessages.put(destinationName, destinationRepo);
				}
			}
		}

		BlockingQueue<MBeanMessage> subscriptionRepo = destinationRepo.get(subscriptionId);

		if (subscriptionRepo == null) {
			synchronized (destinationRepo) {
				subscriptionRepo = destinationRepo.get(subscriptionId);
				if (subscriptionRepo == null) {
					subscriptionRepo = new PriorityBlockingQueue<MBeanMessage>();
					destinationRepo.put(subscriptionId, subscriptionRepo);
				}
			}
		}

		return subscriptionRepo;

	}

	/**
	 * <p>
	 * Starts, stops or restarts this component {@link ExecutorService} {@link #reaperExecutor}), based on the state of
	 * current connections.
	 * 
	 * <p>
	 * Starts if:
	 * <ul>
	 * <li>A connection is being opened and there is no {@link #reaperExecutor} initialized.</li>
	 * </ul>
	 * 
	 * <p>
	 * Stops if:
	 * <ul>
	 * <li>A connection is being closed and there are no more connections.</li>
	 * </ul>
	 * 
	 * <p>
	 * Restarts if:
	 * <ul>
	 * <li>The connection being closed is the last connection based on the class loader which last initialized
	 * {@link #reaperExecutor}.</li>
	 * </ul>
	 * 
	 */
	private void switchExecutorServices(Long connectionId, boolean opening) {

		// Instantiation and shutdown (shutdownNow()) of executors are solely controlled by this method.

		// Updating openConnectionsPerClassLoader, which keeps track of how many connections were opened by each class
		// loader.

		ClassLoader connectionClassLoader = Thread.currentThread().getContextClassLoader();
		Integer connectionsPerClassLoader = openConnectionsPerClassLoader.get(connectionClassLoader);
		if (connectionsPerClassLoader == null) {
			connectionsPerClassLoader = 0;
		}

		if (opening) {
			connectionsPerClassLoader++;
		} else if (connectionsPerClassLoader > 0) {
			connectionsPerClassLoader--;
		}

		if (connectionsPerClassLoader > 0) {
			openConnectionsPerClassLoader.put(connectionClassLoader, connectionsPerClassLoader);
		} else {
			openConnectionsPerClassLoader.remove(connectionClassLoader);
		}

		if (opening && reaperExecutor == null) {

			// Opening a connection and there is no reaperExecutor. Must start a reaper executor.

			startExecutorService(connectionClassLoader, connectionId, opening);

		} else {

			// Shuts the reaperExecutor down if there are no more connection or if the disconnecting
			// connection was the last one based on the class loader which started the current reaper.

			if (openConnections.isEmpty() || (connectionsPerClassLoader < 1 && reaperClassLoader == connectionClassLoader)) {

				if (reaperExecutor != null) {
					try {
						if (reaperExecutorFuture != null) {
							try {
								reaperExecutorFuture.get(reaperExecutorReturnTimeout, reaperExecutorReturnTimeoutUnit);
							} catch (TimeoutException t) {
								if (log.isLoggable(Level.WARNING)) {
									log.log(Level.WARNING, "Running  \"" + reaperExecutorName + "\" task failed to terminate after "
											+ reaperExecutorReturnTimeout + " " + reaperExecutorReturnTimeoutUnit + ".");
								}
							}
						}

						if (log.isLoggable(Level.FINE)) {
							log.fine(reaperExecutorName + " stopped upon closing of the last connection [ #" + connectionId
									+ " ] using the class loader which initially started it: " + reaperClassLoader);
						}

					} catch (Throwable e) {
						if (log.isLoggable(Level.WARNING)) {
							log.log(Level.WARNING, "Failed to shutdown running \"" + reaperExecutorName + "\" tasks.", e);
						}
					}

					reaperExecutor = null;
					reaperExecutorFuture = null;

				}

				// The disconnecting connection was the last one based on the class loader which started
				// the current reaper, but there are other connections, therefore the reaper must be
				// started based on the class loader of the next active connection.

				if (!openConnections.isEmpty() && !openConnectionsPerClassLoader.isEmpty()) {
					startExecutorService(openConnectionsPerClassLoader.keySet().iterator().next(), connectionId, opening);
				}
			}
		}

	}

	private void startExecutorService(ClassLoader connectionClassLoader, Long connectionId, boolean opening) {
		ClassLoader originalClassLoader = pushClassLoader(connectionClassLoader);
		try {
			reaperExecutor = VirtualThreadExecutorBuilder.newPool().concurrency(1).description("MBean Message Reaper").build();
			reaperExecutorFuture = reaperExecutor.submit(new MBeanMessageReaper());
			reaperClassLoader = connectionClassLoader;

			if (log.isLoggable(Level.FINE)) {
				log.fine(reaperExecutorName + " " + (opening ? "re" : "") + "started upon " + (opening ? "opening" : "closing") + " of connection [ #"
						+ connectionId + " ] using: " + reaperClassLoader);
			}

		} finally {
			pushClassLoader(originalClassLoader);
		}
	}

	/**
	 * <p>
	 * Pushes the given ClassLoader as the context class loader of the current thread, returning the previous
	 * contextClassLoader.
	 * 
	 * @param newLoader
	 *            The ClassLoader to be set as the current Thread context ClassLoader
	 * @return The ClassLoader previously set as the current Thread context ClassLoader
	 */
	private static ClassLoader pushClassLoader(ClassLoader newLoader) {

		ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();

		if (currentLoader != newLoader) {
			Thread.currentThread().setContextClassLoader(newLoader);
		}

		return currentLoader;

	}

	/**
	 * <p>
	 * A thread factory allowing a custom name for the created threads.
	 */
	private static class NamedPoolThreadFactory implements ThreadFactory {

		private final String threadName;

		NamedPoolThreadFactory(String threadName) {
			this.threadName = threadName;
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = Thread.ofVirtual().unstarted(r);
			t.setName(threadName);
			if (!t.isDaemon()) {
				t.setDaemon(true);
			}
			if (t.getPriority() != Thread.NORM_PRIORITY) {
				t.setPriority(Thread.NORM_PRIORITY);
			}
			return t;
		}

	}

	/**
	 * <p>
	 * A message wrapper which uses jdk and simple types only ({@code String}, {@code char}, {@code long} etc..).
	 * 
	 */
	private static class MBeanMessage implements Function<String, Object>, Callable<byte[]>, Comparable<MBeanMessage> {

		char destinationType;
		String destinationName;
		String messageId;
		byte[] message;
		int priority;
		long expiration;
		long producedAt;
		Map<String, Object> headers;
		Map<String, Object> properties;

		public MBeanMessage(char destinationType, String destinationName, String messageId, byte[] message, int priority, long expiration,
				Map<String, Object> headers, Map<String, Object> properties) {
			this.destinationType = destinationType;
			this.destinationName = destinationName;
			this.messageId = messageId;
			this.message = message;
			this.priority = priority;
			this.expiration = expiration;
			this.producedAt = System.currentTimeMillis();
			this.headers = headers;
			this.properties = properties;
		}

		@Override
		public byte[] call() throws Exception {
			return message;
		}

		@Override
		public int compareTo(MBeanMessage o) {

			int c = Long.compare(o.priority, this.priority);

			if (c != 0)
				return c;

			return Long.compare(this.producedAt, o.producedAt);
		}

		public boolean expired() {
			return expiration > 0 && expiration < System.currentTimeMillis();
		}

		@Override
		public String toString() {
			return "Message{id=" + messageId + ", destinationType=" + destinationType + ", destinationName=" + destinationName + ", priority="
					+ priority + ", expiration=" + expiration + "}";
		}

		@Override
		public Object apply(String propertyName) {

			Objects.requireNonNull(propertyName, "Property must not be null");

			MBeanMessageProperty property = MBeanMessageProperty.valueOf(propertyName);

			switch (property) {
				case destinationType:
					return destinationType;
				case destinationName:
					return destinationName;
				case messageId:
					return messageId;
				case message:
					return message;
				case priority:
					return priority;
				case expiration:
					return expiration;
				case expired:
					return expired();
				case producedAt:
					return producedAt;
				case headers:
					return headers;
				case properties:
					return properties;
				default:
					throw new IllegalArgumentException("Unmapped property " + propertyName);
			}

		}

	}

	/**
	 * <p>
	 * A task responsible for deleting queue-addressed messages which expired.
	 * 
	 */
	private class MBeanMessageReaper implements Callable<Void> {

		@Override
		public Void call() {

			if (log.isLoggable(Level.FINER)) {
				log.log(Level.FINER, "Messages reaper initialized");
			}

			try {

				while (true) {

					if (log.isLoggable(Level.FINER)) {
						log.log(Level.FINER, "Scheduled messages reaper to run within " + reaperExecutorInterval + " ms");
					}

					Thread.sleep(reaperExecutorInterval);
					reapExpiredMessages();
				}

			} catch (InterruptedException e) {
				if (log.isLoggable(Level.FINEST)) {
					log.log(Level.FINEST, "Messages reaper thread interrupted.");
				}
			}

			return null;

		}

		protected void reapExpiredMessages() {

			if (log.isLoggable(Level.FINER)) {
				log.log(Level.FINER, "Starting messages reaper");
			}

			try {
				long l = 0;
				int r = 0;
				int s = 0;
				int n = 0;

				if (log.isLoggable(Level.FINE)) {
					l = System.currentTimeMillis();
				}

				for (Map.Entry<String, BlockingQueue<MBeanMessage>> entry : queueMessages.entrySet()) {

					Iterator<MBeanMessage> iter = entry.getValue().iterator();

					while (iter.hasNext()) {
						MBeanMessage mBeanMessage = iter.next();
						if (mBeanMessage.expired()) {
							if (log.isLoggable(Level.FINE)) {
								log.log(Level.FINE, "Undelivered message will be discaded as it has expired: [ " + mBeanMessage + " ]");
							}

							iter.remove();
							r++;

						} else {
							if (mBeanMessage.expiration == 0) {
								n++;
								if (log.isLoggable(Level.FINEST)) {
									log.log(Level.FINEST, "Undelivered message remained retained as it will never expire: [ " + mBeanMessage + " ]");
								}
							} else {
								s++;
								if (log.isLoggable(Level.FINEST)) {
									log.log(Level.FINEST, "Undelivered message is not expired and will be retained: [ " + mBeanMessage + " ]");
								}
							}
						}
					}

				}

				if (log.isLoggable(Level.FINE)) {
					l = System.currentTimeMillis() - l;
					log.log(Level.FINE,
							"Message reaper ran in " + l + " ms checking the expiration in " + (s + r + n) + " retained message(s)"
									+ (r > 0 ? ". " + r + " expired and got discarded" : "")
									+ (s > 0 ? ". " + s + " remained retained and will expire eventually" : "")
									+ (n > 0 ? ". " + n + " remained retained and will never expire" : ""));
				} else if (r > 0 && log.isLoggable(Level.INFO)) {
					log.log(Level.INFO, r + " expired message(s) discarded" + (s > 0 ? ". " + s + " expirable message(s) remained retained" : "")
							+ (n > 0 ? ". " + n + " unexpirable message(s) remained retained" : ""));
				}

				if (n + s > retainedWarningLimit) {
					log.log(Level.WARNING, "There are " + (n + s) + " retained messages, of which "
							+ (s > 0 ? ". " + s + " will eventually expire" : "") + (n > 0 ? ". " + n + " will never expire" : ""));
				}

			} catch (Exception e) {
				if (log.isLoggable(Level.SEVERE)) {
					log.log(Level.SEVERE, "Failed to run expired messages reaper: " + e.getMessage(), e);
				}
			}
		}

	}

}
