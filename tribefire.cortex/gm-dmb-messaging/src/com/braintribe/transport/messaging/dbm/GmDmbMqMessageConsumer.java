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
package com.braintribe.transport.messaging.dbm;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.execution.virtual.VirtualThreadExecutorBuilder;
import com.braintribe.logging.Logger;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Topic;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageListener;
import com.braintribe.transport.messaging.api.MessageProperties;
import com.braintribe.transport.messaging.api.MessagingComponentStatus;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.dbm.mbean.MBeanMessageProperty;

/**
 * <p>
 * {@link MessageConsumer} implementation for {@link GmDmbMqMessaging}.
 * 
 * @see MessageConsumer
 */
public class GmDmbMqMessageConsumer extends GmDmbMqMessageHandler implements MessageConsumer {

	private String consumerId;

	private MessageListener messageListener;
	private ReentrantLock messageListenerLock = new ReentrantLock();

	private MessagingComponentStatus status = MessagingComponentStatus.OPEN;

	private ClassLoader consumerCreationClassLoader;

	private ClassLoader listenerSettingClassLoader;

	private ExecutorService asyncListenerExecutor;
	private ThreadFactory asyncListenerExecutorThreadFactory;
	private Future<Boolean> asyncListenerFuture;
	private long asyncListenerTimeout = 2;
	private TimeUnit asyncListenerTimeoutUnit = TimeUnit.SECONDS;

	private static final String threadNamePrefix = "tribefire.mbean.messaging.consumer";

	private static final Logger log = Logger.getLogger(GmDmbMqMessageConsumer.class);

	public GmDmbMqMessageConsumer(String consumerId) throws MessagingException {
		this.consumerId = consumerId;
		this.consumerCreationClassLoader = obtainCurrentContextClassLoader();
	}

	@Override
	public MessageListener getMessageListener() throws MessagingException {
		return this.messageListener;
	}

	@Override
	public void setMessageListener(MessageListener messageListener) throws MessagingException {

		messageListenerLock.lock();
		try {
			if (messageListener == null) { // Setting to null is the equivalent of unsetting the listener for this consumer.
				shutdownAsyncListener();
				this.messageListener = null;
				return;
			}

			if (this.messageListener != null) {
				throw new MessagingException("Message listener already set for this consumer");
			}

			this.messageListener = messageListener;
			listenerSettingClassLoader = obtainCurrentContextClassLoader();

			if (this.messageListener != null) {

				if (asyncListenerExecutor == null) {
					asyncListenerExecutor = VirtualThreadExecutorBuilder.newPool().concurrency(1)
							.description("Dmb Message Consumer (" + getDestination().getName() + ")").build();
				}

				asyncListenerFuture = asyncListenerExecutor.submit(new MessageListenerConsumer());

			}
		} finally {
			messageListenerLock.unlock();
		}

	}

	@Override
	public Message receive() throws MessagingException {
		return receive(0);
	}

	@Override
	public Message receive(long timeout) throws MessagingException {

		if (status != MessagingComponentStatus.OPEN) {
			return null;
		}

		long expiration = timeout > 0 ? System.currentTimeMillis() + timeout : 0;

		BlockingQueue<Function<String, Object>> consumerQueue = getConsumerQueue();

		while (true) {

			Function<String, Object> mbeanMessage;
			try {
				mbeanMessage = consumerQueue.poll(500, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				if (log.isDebugEnabled()) {
					// Removed exception from log output. It's just frightening to see exceptions in the log
					// and there is probably no benefit from having the full stacktrace anyway.
					log.debug("" + this.consumerId + ": Polling has been interrupted.");
				}
				return null;
			} catch (Exception e) {
				throw new MessagingException("Failed to poll consumer queue" + (e.getMessage() != null ? " :" + e.getMessage() : ""), e);
			}

			if (mbeanMessage != null) {

				if (status != MessagingComponentStatus.OPEN) {

					if (getDestinationType() == 'q') {
						getConsumerQueue().offer(mbeanMessage);
					}

					return null;
				}

				if (!isExpired(mbeanMessage) && matchesAddressee(mbeanMessage)) {
					Message message = extractMessage(mbeanMessage);
					if (message != null) {
						return message;
					}
				}

			}

			if (status != MessagingComponentStatus.OPEN || (expiration > 0 && expiration < System.currentTimeMillis())) {
				return null;
			} else {
				continue;
			}
		}

	}

	@Override
	public void close() throws MessagingException {

		if (status == MessagingComponentStatus.CLOSED) {
			if (log.isDebugEnabled()) {
				log.debug("Consumer [ " + this + " ] already closed.");
			}
			return;
		}

		status = MessagingComponentStatus.CLOSING;

		try {

			shutdownAsyncListener();

			boolean removed = true;
			if (getDestination() instanceof Topic) {
				removed = getSession().getConnection().getMessagingMBean().unsubscribeTopicConsumer(getDestination().getName(), consumerId);
			}

			if (removed) {
				if (log.isDebugEnabled()) {
					log.debug("Unregistered consumer [ " + this + " ] from MessagingMBean");
				}
			} else {
				if (log.isWarnEnabled()) {
					log.warn("Unregistration of consumer [ " + this + " ] from MessagingMBean failed.");
				}
			}

		} catch (Exception e) {
			throw new MessagingException("Failed to remove consumer [ " + this + " ] from MessagingMBean: " + e.getMessage(), e);
		}

		status = MessagingComponentStatus.CLOSED;

		if (log.isDebugEnabled()) {
			log.debug("Closed consumer [ " + this + " ]");
		}

	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{" + "id=" + this.consumerId + "," + "destination="
				+ (this.getDestination() != null ? this.getDestination().getName() : "") + "," + "status=" + this.status + "}";
	}

	public String getConsumerId() {
		return consumerId;
	}

	@SuppressWarnings("unchecked")
	protected BlockingQueue<Function<String, Object>> getConsumerQueue() {
		return (BlockingQueue<Function<String, Object>>) getSession().getConnection().getMessagingMBean().getQueue(getDestinationType(),
				getDestination().getName(), consumerId);
	}

	protected Message extractMessage(Function<String, Object> mbeanMessage) {

		Message message = null;

		Thread currentThread = Thread.currentThread();
		ClassLoader originalClassLoader = currentThread.getContextClassLoader();

		pushConsumerContextClassLoader(currentThread, originalClassLoader);

		try {
			try {
				byte[] messageData = MBeanMessageProperty.get(mbeanMessage, MBeanMessageProperty.message);
				message = getSession().getMessagingContext().unmarshallMessage(messageData);
				getSession().getMessagingContext().enrichInbound(message);
			} catch (MarshallException e) {
				log.error("Failed to extract " + Message.class.getName() + " from [ " + mbeanMessage + " ]: " + e.getMessage(), e);
			} catch (Exception e) {
				log.error("Failed to get " + Message.class.getName() + " contents from [ " + mbeanMessage + " ]: " + e.getMessage(), e);
			}
		} finally {
			pushOriginalContextClassLoader(currentThread, originalClassLoader);
		}

		return message;

	}

	protected boolean isExpired(Function<String, Object> mbeanMessage) {

		Long expiration = MBeanMessageProperty.get(mbeanMessage, MBeanMessageProperty.expiration);

		if (expiration != null && expiration > 0 && expiration < System.currentTimeMillis()) {
			if (log.isDebugEnabled()) {
				log.debug("Consumer received an expired message: " + mbeanMessage);
			}
			return true;
		} else {
			if (log.isTraceEnabled()) {
				log.trace("Consumer received an non-expired message: " + mbeanMessage);
			}
			return false;
		}

	}

	protected boolean matchesAddressee(Function<String, Object> mbeanMessage) {

		if (getDestinationType() == 'q') {
			return true;
		}

		Map<String, Object> properties = MBeanMessageProperty.get(mbeanMessage, MBeanMessageProperty.properties);

		if (properties == null || properties.isEmpty()) {
			return true;
		}

		String appId, nodeId;

		// @formatter:off
		return !(
					(
						(appId = (String) properties.get(MessageProperties.addreseeAppId.getName())) != null && 
						!appId.equals(getApplicationId())
					) || (
						(nodeId = (String) properties.get(MessageProperties.addreseeNodeId.getName())) != null && 
						!nodeId.equals(getNodeId())
					)
				);
		// @formatter:on

	}

	/**
	 * <p>
	 * Forces the interruption of the asynchronous listener task, if existent.
	 * 
	 * @return {@code true} if there was a running task and that was successfully terminated.
	 */
	protected boolean shutdownAsyncListener() {

		if (asyncListenerFuture != null) {
			messageListenerLock.lock();
			try {
				Boolean asyncListenerResponse;
				try {
					asyncListenerResponse = asyncListenerFuture.get(asyncListenerTimeout, asyncListenerTimeoutUnit);

					if (log.isTraceEnabled()) {
						log.trace("Asynchronous listener returned: " + asyncListenerResponse);
					}

					asyncListenerFuture = null;

					return true;

				} catch (InterruptedException e) {
					if (log.isDebugEnabled()) {
						log.debug("Thread interrupted while waiting asynchronous listener task to complete", e);
					}
				} catch (ExecutionException e) {
					if (log.isDebugEnabled()) {
						log.debug("Thread interrupted while waiting asynchronous listener task to complete", e);
					}
				} catch (TimeoutException e) {
					if (log.isDebugEnabled()) {
						log.debug("Asynchronous listener completition timeout (" + asyncListenerTimeout + " " + asyncListenerTimeoutUnit
								+ ") has been reached", e);
					}
				}
			} finally {
				messageListenerLock.unlock();
			}
		}

		return false;

	}

	/**
	 * <p>
	 * Pushes this consumer's preferable class loader as the current thread's context class loader.
	 * 
	 * @param currentThread
	 *            The Thread to have the context ClassLoader set to this consumer's ClassLoader (
	 *            {@link #getConsumerContextClassLoader()})
	 * @param currentClassLoader
	 *            The Thread's context ClassLoader prior this method call
	 */
	private void pushConsumerContextClassLoader(Thread currentThread, ClassLoader currentClassLoader) {

		ClassLoader consumerClassLoader = getConsumerContextClassLoader();

		if (consumerClassLoader != null && !consumerClassLoader.equals(currentClassLoader)) {

			currentThread.setContextClassLoader(consumerClassLoader);

			if (log.isDebugEnabled()) {
				log.debug(
						"Class loader of current thread did not match the preferable class loader for this consumer. Current thread context class loader is [ "
								+ currentClassLoader + " ] whereas preferable context class loader for this consumer is [ " + consumerClassLoader
								+ " ]. Current thread context class loader after pushing is [ " + currentThread.getContextClassLoader() + " ] ");
			}
		}
	}

	/**
	 * <p>
	 * Pushes back the original context class loader to the current thread.
	 * 
	 * @param currentThread
	 *            The Thread to have the context ClassLoader set to its previous instance, given by
	 *            {@code originalClassLoader}
	 * @param originalClassLoader
	 *            The ClassLoader to be set as the given Thread's ({@code currentThread}) context ClassLoader
	 */
	private static void pushOriginalContextClassLoader(Thread currentThread, ClassLoader originalClassLoader) {

		if (originalClassLoader != null && !originalClassLoader.equals(currentThread.getContextClassLoader())) {

			currentThread.setContextClassLoader(originalClassLoader);

			if (log.isDebugEnabled()) {
				log.debug("Pushed back original thread's context class loader. Current thread context class loader after pushing back is [ "
						+ currentThread.getContextClassLoader() + " ] ");
			}

		}
	}

	/**
	 * <p>
	 * Returns the best suitable {@link ClassLoader} to be pushed to Threads before unmarshalling Messages and before
	 * calling {@link MessageListener#onMessage(Message)} on the registered listener.
	 * 
	 * <p>
	 * If a non-null {@link MessageListener} was set to this {@link MessageConsumer}, the context ClassLoader of the thread
	 * which called {@link MessageConsumer#setMessageListener(MessageListener)} will be used.
	 * 
	 * <p>
	 * Otherwise, the context ClassLoader of the thread which created the {@link MessageConsumer} will be used.
	 * 
	 * @return This consumer's preferable ClassLoader
	 */
	private ClassLoader getConsumerContextClassLoader() {
		return listenerSettingClassLoader == null ? consumerCreationClassLoader : listenerSettingClassLoader;
	}

	/**
	 * Obtains the context ClassLoader of the current Thread, throwing a MessagingException if the retrieval of the context
	 * class loader is not permitted.
	 * 
	 * @return The context ClassLoader of the current Thread
	 * @throws MessagingException
	 *             If the retrieval of the context class loader is not permitted.
	 */
	private static ClassLoader obtainCurrentContextClassLoader() throws MessagingException {
		try {
			return Thread.currentThread().getContextClassLoader();
		} catch (Exception e) {
			throw new MessagingException("Failed to obtain current thread class loader: " + e.getMessage(), e);
		}
	}

	private ThreadFactory getThreadFactory() {
		if (asyncListenerExecutorThreadFactory == null) {
			asyncListenerExecutorThreadFactory = new ConsumerThreadFactory(
					threadNamePrefix + "[" + getDestination().getName() + "][" + consumerId.split("-")[1] + "]");
		}
		return asyncListenerExecutorThreadFactory;
	}

	protected class MessageListenerConsumer implements Callable<Boolean> {

		@Override
		public Boolean call() {

			if (status != MessagingComponentStatus.OPEN) {
				return false;
			}

			while (true) {
				try {
					Message message = receive();

					if (status != MessagingComponentStatus.OPEN || message == null) {
						return false;
					}

					if (messageListener != null) {

						Thread currentThread = Thread.currentThread();
						ClassLoader originalClassLoader = currentThread.getContextClassLoader();
						pushConsumerContextClassLoader(currentThread, originalClassLoader);

						try {
							messageListener.onMessage(message);
						} catch (Exception e) {
							log.error("Error while delivering message " + message + " to the message listener: " + messageListener, e);
						} finally {
							pushOriginalContextClassLoader(currentThread, originalClassLoader);
						}
					}

				} catch (MessagingException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}

	protected static class ConsumerThreadFactory implements ThreadFactory {

		private final String name;

		protected ConsumerThreadFactory(String name) {
			this.name = name;
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = Thread.ofVirtual().unstarted(r);
			t.setName(name);
			if (!t.isDaemon()) {
				t.setDaemon(true);
			}
			if (t.getPriority() != Thread.NORM_PRIORITY) {
				t.setPriority(Thread.NORM_PRIORITY);
			}
			return t;
		}
	}

}
