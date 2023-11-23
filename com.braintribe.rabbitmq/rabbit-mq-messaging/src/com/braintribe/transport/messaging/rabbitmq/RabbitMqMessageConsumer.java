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
package com.braintribe.transport.messaging.rabbitmq;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.logging.Logger;
import com.braintribe.model.messaging.Message;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageListener;
import com.braintribe.transport.messaging.api.MessageProperties;
import com.braintribe.transport.messaging.api.MessagingComponentStatus;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.transport.messaging.api.MessagingException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * <p>
 * {@link MessageConsumer} implementation for {@link RabbitMqMessaging}.
 * 
 * @see MessageConsumer
 */
public class RabbitMqMessageConsumer extends RabbitMqMessageHandler implements MessageConsumer {
	
	private MessageListener messageListener;
	
	private MessagingComponentStatus status = MessagingComponentStatus.NEW;
	
	private ConsumptionMode consumptionMode;
	
	private Consumer consumer;
	private String consumerQueueName;
	private String consumerTag;
	private boolean autoAck = false;
	
	private static final Logger log = Logger.getLogger(RabbitMqMessageConsumer.class);
	
	public RabbitMqMessageConsumer() {
		super();
	}
	
	private enum ConsumptionMode {
		SYNC, ASYNC;
	}

	@Override
	public MessageListener getMessageListener() throws MessagingException {
		return messageListener;
	}

	@Override
	public void setMessageListener(MessageListener messageListener) throws MessagingException {
		this.messageListener = messageListener;
		
		//consumption starts whenever a not null message listener is registered to this consumer
		if (this.messageListener != null) {
			startDelegateConsumption(ConsumptionMode.ASYNC);
		}
	}

	@Override
	public Message receive() throws MessagingException {
		return receive(0);
	}

	@Override
	public Message receive(long timeout) throws MessagingException {

		startDelegateConsumption(ConsumptionMode.SYNC);

		BlockingConsumer consumer = (BlockingConsumer) this.consumer;

		while (true) {
			BlockingConsumerMessage delivery;

			try {
				if (timeout > 0) {
					delivery = consumer.receive(timeout);
				} else {
					delivery = consumer.receive();
				}

				if (delivery == null) {
					return null;
				}

			} catch (ShutdownSignalException | ConsumerCancelledException | InterruptedException e) {

				log.trace(() -> "Consumer call on nextDelivery() was interrupted" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);

				return null;

			} catch (Exception e) {
				throw new MessagingException("failed to consume message: " + e.getMessage(), e);
			}

			if (!autoAck) {
				acknowledge(delivery.envelope.getDeliveryTag());
			}

			if (!matchesAddressee(delivery.properties)) {
				log.trace(() -> "Delivery ignored due to addresse headers mismatch: " + delivery.properties);
				continue;
			}

			log.trace(() -> messageLog("Received", delivery));

			Message message = extractMessage(delivery.properties, delivery.body);

			if (message != null) {
				return message;
			}

		}

	}

	@Override
	public void close() throws MessagingException {
		close(true);
	}

	protected synchronized void close(boolean unregisterFromSession) throws MessagingException {
		try {

			if (status == MessagingComponentStatus.OPEN) {

				log.trace(() -> "Stopping consumer [ " + getConsumerTag() + " ] created for " + getRabbitMqDestination());

				getChannel().basicCancel(getConsumerTag());

				log.debug(() -> "Stopped consumer [ " + getConsumerTag() + " ] created for " + getRabbitMqDestination());

				if (unregisterFromSession) {
					getSession().unregisterMessageConsumer(this);
				}

				status = MessagingComponentStatus.CLOSED;

				log.debug(() -> "Consumer [ " + getConsumerTag() + " ] changed to [ " + status.toString().toLowerCase() + " ]  state.");

			} else if (log.isDebugEnabled()) {
				log.debug("Consumer [ " + getConsumerTag() + " ] won't be closed as it is in [ " + status.toString().toLowerCase() + " ]  state.");
			}

		} catch (IOException e) {
			Throwable cause = e.getCause();
			if (cause != null && (cause instanceof ShutdownSignalException || cause instanceof AlreadyClosedException)) {
				log.trace(() -> "Consumer [ " + getConsumerTag() + " ] is already closed or in shutdown", cause);
			} else {
				throw new MessagingException("Failed to cancel consumer [ " + getConsumerTag() + " ]: " + e.getMessage(), e);
			}
		}
	}

	/**
	 * <p>
	 * Stars the delegate consumer's consumption by registering the previously created {@link #consumer} with the
	 * previously prepared {@link #consumerQueueName}.
	 * 
	 * @throws MessagingException
	 *             If the delegate consumer fails to be started
	 */
	protected synchronized void open() throws MessagingException {
		try {

			if (status == MessagingComponentStatus.NEW) {

				log.trace(() -> "Starting " + getRabbitMqDestination() + " consumer with tag [ " + getConsumerTag() + " ] on queue [ "
						+ consumerQueueName + " ] using callback: " + consumer);

				getChannel().basicConsume(consumerQueueName, autoAck, getConsumerTag(), consumer);

				log.debug(() -> "Started " + getRabbitMqDestination() + " consumer with tag [ " + getConsumerTag() + " ] on queue [ "
						+ consumerQueueName + " ] using callback: " + consumer);

				status = MessagingComponentStatus.OPEN;

				log.debug(() -> "Consumer [ " + getConsumerTag() + " ] changed to [ " + status.toString().toLowerCase() + " ]  state.");

			} else if (log.isWarnEnabled()) {
				log.warn("Cannot start consumer in [ " + status.toString().toLowerCase() + " ]  state.");
			}

		} catch (IOException e) {
			throw new MessagingException("Failed to start consumer: " + e.getMessage(), e);
		}
	}
	
	/**
	 * <p>
	 * Acknowledges the message with the given delivery tag.
	 * 
	 * @param deliveryTag
	 *            Delivery tag of the (single) message to be acknowledged
	 * @throws MessagingException
	 *             If the given delivery tag fails to be acknowledged
	 */
	protected synchronized void acknowledge(long deliveryTag) throws MessagingException {
		try {
			getChannel().basicAck(deliveryTag, false);

			log.trace(() -> "Delivery tagged as [ " + deliveryTag + " ] was successfully acknowledged");

		} catch (IOException e) {
			throw new MessagingException("Failed to acknowledge the message with delivery tag [ " + deliveryTag + " ]: " + e.getMessage(), e);
		}
	}
	
	/**
	 * <p>
	 * Starts the consumption for this consumer under the given {@code mode}.
	 * 
	 * <p>
	 * This method is a no-op if this consumer was already initialized under the given {@code mode}.
	 * 
	 * <p>
	 * This method will halt with a {@link MessagingException} if this consumer was already initialized with a different
	 * consumption mode than the given {@code mode}.
	 * 
	 * @param mode
	 *            The {@link ConsumptionMode}, currently supported options are: asynchronous (
	 *            {@link ConsumptionMode#ASYNC}) and synchronous ({@link ConsumptionMode#SYNC})
	 * @throws MessagingException
	 *             If:
	 *             <ul>
	 *             <li>The given {@link ConsumptionMode} is not supported;
	 *             <li>This consumer was already initialized with a different {@link ConsumptionMode};
	 *             <li>The consumption initialization fails;
	 *             </ul>
	 */
	protected synchronized void startDelegateConsumption(ConsumptionMode mode) throws MessagingException {

		if (status == MessagingComponentStatus.NEW) {

			if (mode == ConsumptionMode.ASYNC) {
				initializeAsynchronousConsumer();
			} else if (mode == ConsumptionMode.SYNC) {
				initializeSynchronousConsumer();
			} else {
				throw new IllegalArgumentException("Unknown consumption mode: " + mode);
			}

			open();
			consumptionMode = mode;
		}

		if (consumptionMode != mode) {
			throw new MessagingException("Failed to start consumption in [ " + mode + " ] mode for this consumer as it was already started as [ "
					+ consumptionMode + " ]");
		}

	}
	
	/**
	 * <p>
	 * Unmarshalls a {@link Message} based on the given set of AMQP properties (
	 * {@link com.rabbitmq.client.AMQP.BasicProperties}) and message payload ({@code body}).
	 * 
	 * @param properties
	 *            {@code AMQP.BasicProperties} as received from the broker
	 * @param body
	 *            The marshalled message body
	 * @return The unmarshalled message
	 */
	protected Message extractMessage(AMQP.BasicProperties properties, byte[] body) {

		if (body != null && body.length > 0) {
			try {

				log.trace(() -> "Extracting message received from " + getRabbitMqDestination());

				MessagingContext context = getSession().getConnection().getConnectionProvider().getMessagingContext();
				Message message = context.unmarshallMessage(body);

				log.trace(() -> "Extracted message received from " + getRabbitMqDestination() + ": " + message);

				return message;

			} catch (MarshallException e) {
				log.error("Failed to extract a message from the given payload due to: " + e.getMessage(), e);
			}
		} else {
			log.warn(() -> "Unable to extract a message of type [ " + properties.getContentType() + " ]: " + body);
		}

		return null;

	}

	protected boolean matchesAddressee(AMQP.BasicProperties properties) {

		if (!handlesTopic()) {
			return true;
		}

		Map<String, Object> headers = properties.getHeaders();

		if (headers == null || headers.isEmpty()) {
			return true;
		}

		Object appId, nodeId;

		// @formatter:off
		return !(
					(
						(appId = headers.get(propertyPrefix+MessageProperties.addreseeAppId.getName())) != null && 
						!appId.toString().equals(getApplicationId())
					) || (
						(nodeId = headers.get(propertyPrefix+MessageProperties.addreseeNodeId.getName())) != null && 
						!nodeId.toString().equals(getNodeId())
					)
				);
		// @formatter:on

	}

	/**
	 * <p>
	 * Initializes the {@link #consumerQueueName} against this consumer's Channel {@link #getChannel()} regardless of
	 * the consumption mode (asynchronous or synchronous) and destination type (queue / topic).
	 * 
	 * @throws MessagingException
	 *             If this consumer's {@link #consumerQueueName} fails to be initialized
	 */
	private void prepareConsumerQueue() throws MessagingException {

		if (handlesTopic()) {

			try {

				log.trace(() -> "Declaring a consumer queue for " + getRabbitMqDestination());

				this.consumerQueueName = getChannel().queueDeclare().getQueue();

				log.trace(() -> "Declared a consumer queue [ " + consumerQueueName + " ]. Binding it to " + getRabbitMqDestination());

				getChannel().queueBind(this.consumerQueueName, getRabbitMqDestination().getExchangeName(), getRabbitMqDestination().getRoutingKey());

				log.trace(() -> "Bound consumer queue [ " + consumerQueueName + " ] to " + getRabbitMqDestination());

			} catch (IOException e) {
				throw new MessagingException("error while declaring the queue with Channel.queueDeclare(): " + e.getMessage(), e);
			}

		} else {

			try {
				getChannel().basicQos(1);
			} catch (Exception e) {
				throw new MessagingException("Failed to prepare prefetch for queue consumption", e);
			}

			this.consumerQueueName = getDestination().getName();

		}

	}

	/**
	 * <p>
	 * Initializes consumer in asynchronous mode.
	 * 
	 * @throws MessagingException
	 *             If this consumer fails to be initialized
	 */
	private void initializeAsynchronousConsumer() throws MessagingException {
		prepareConsumerQueue();
		this.consumer = new ListenerConsumer(getChannel());
	}
	
	/**
	 * <p>
	 * Initializes consumer in synchronous mode.
	 * 
	 * @throws MessagingException
	 *             If this consumer fails to be initialized
	 */
	private void initializeSynchronousConsumer() throws MessagingException {
		prepareConsumerQueue();
        this.consumer = new BlockingConsumer(getChannel());
	}
	
	/**
	 * <p>
	 * Returns this consumer's unique tag. Generating one for this consumer instance if it was not yet generated.
	 * 
	 * @return The consumer tag, unique for this consumer
	 */
	private String getConsumerTag() {

		if (consumerTag == null) {
			synchronized (this) {
				if (consumerTag == null) {
					consumerTag = this.getClass().getSimpleName() + "-" + UUID.randomUUID().toString();
					log.debug(() -> "Consumer tag [ " + consumerTag + " ] generated for consumer [ " + this + " ].");
				}
			}
		}

		return consumerTag;
	}

	/**
	 * <p>
	 * Common base for blocking and non-blocking {@link DefaultConsumer} implementations.
	 * 
	 */
	private class AbstractConsumer extends DefaultConsumer {

		public AbstractConsumer(Channel channel) {
			super(channel);
		}

		@Override
		public void handleConsumeOk(String incomingTag) {
			super.handleConsumeOk(incomingTag);
			log.trace(() -> "handleConsumeOk(): incoming [ " + incomingTag + " ]. current: [ " + getConsumerTag() + " ]");
		}

		@Override
		public void handleCancelOk(String incomingTag) {
			log.trace(() -> "handleCancelOk(): incoming [ " + incomingTag + " ]. current: [ " + getConsumerTag() + " ]");
		}

		@Override
		public void handleCancel(String incomingTag) throws IOException {
			log.trace(() -> "handleCancel(): incoming [ " + incomingTag + " ]. current: [ " + getConsumerTag() + " ]");
		}

		@Override
		public void handleShutdownSignal(String incomingTag, ShutdownSignalException sig) {
			log.trace(() -> "handleShutdownSignal(): incoming [ " + incomingTag + " ]. current: [ " + getConsumerTag() + " ]", sig);
		}

		@Override
		public void handleRecoverOk(String incomingTag) {
			log.trace(() -> "handleRecoverOk(): incoming [ " + incomingTag + " ]. current: [ " + getConsumerTag() + " ]");
		}

		protected boolean skip(AMQP.BasicProperties properties, long deliveryTag) {

			if (!matchesAddressee(properties)) {
				log.trace(() -> "Message ignored due to addresse headers mismatch: " + properties);
				if (!autoAck) {
					try {
						acknowledge(deliveryTag);
					} catch (MessagingException e) {
						log.error("Failed to ack message ignored due to addresse headers mismatch", e);
					}
				}
				return true;
			}

			return false;

		}

		protected boolean matchesAddressee(AMQP.BasicProperties properties) {

			if (!handlesTopic()) {
				return true;
			}

			Map<String, Object> headers = properties.getHeaders();

			if (headers == null || headers.isEmpty()) {
				return true;
			}

			Object appId, nodeId;

			// @formatter:off
			return !(
						(
							(appId = headers.get(propertyPrefix+MessageProperties.addreseeAppId.getName())) != null && 
							!appId.toString().equals(getApplicationId())
						) || (
							(nodeId = headers.get(propertyPrefix+MessageProperties.addreseeNodeId.getName())) != null && 
							!nodeId.toString().equals(getNodeId())
						)
					);
			// @formatter:on

		}

	}

	/**
	 * <p>
	 * A which {@link Consumer} delivers to the configured {@link MessageListener}.
	 * 
	 */
	private class ListenerConsumer extends AbstractConsumer {

		public ListenerConsumer(Channel channel) {
			super(channel);
		}

		@Override
		public void handleDelivery(String incomingConsumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

			long deliveryTag = envelope.getDeliveryTag();

			if (messageListener == null) {
				log.warn(() -> "Delivery tagged as [ " + deliveryTag + " ] will not be acknowledged as there is no message listener registered");
				return;
			}

			if (skip(properties, deliveryTag)) {
				return;
			}

			log.trace(() -> messageLog("Received", incomingConsumerTag, envelope, properties, body));

			try {
				Message message = extractMessage(properties, body);
				if (message != null) {
					messageListener.onMessage(message);
				}
				if (!autoAck) {
					acknowledge(deliveryTag);
				}
			} catch (MessagingException e) {
				throw new IOException(e);
			}

		}

	}

	/**
	 * <p>
	 * A {@link Consumer} with blocking capabilities as the previously used
	 * {@code com.rabbitmq.client.QueueingConsumer}, which was deprecated and removed.
	 * 
	 */
	private class BlockingConsumer extends AbstractConsumer {

		private final BlockingQueue<BlockingConsumerMessage> queue = new LinkedBlockingQueue<BlockingConsumerMessage>();
		private volatile ShutdownSignalException shutdownException;
		private volatile ConsumerCancelledException cancelledException;

		private static final int warningThreshold = 100;
		private volatile long lastTake = 0;

		public BlockingConsumer(Channel channel) {
			super(channel);
		}

		@Override
		public void handleShutdownSignal(String incomingTag, ShutdownSignalException sig) {
			super.handleShutdownSignal(incomingTag, sig);
			shutdownException = sig;
			queue.add(BlockingConsumerMessage.poisonPill);
		}

		@Override
		public void handleCancel(String incomingTag) throws IOException {
			super.handleCancel(incomingTag);
			cancelledException = new ConsumerCancelledException();
			queue.add(BlockingConsumerMessage.poisonPill);
		}

		@Override
		public void handleDelivery(String incomingTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

			if (shutdownException != null) {
				throw wrap("Consumer in shutown mode", shutdownException);
			}

			this.queue.add(new BlockingConsumerMessage(envelope, properties, body));

			if (log.isWarnEnabled()) {

				final int size = queue.size();

				if (size > warningThreshold) {
					if (lastTake == 0) {
						log.warn("This consumer has accumulated " + size + " messages, and no consumption is taking place.");
					} else {
						log.warn("This consumer has accumulated " + size + " messages. Last take at " + (new Date(lastTake)));
					}
				}

			}

		}

		public BlockingConsumerMessage receive() throws InterruptedException, ShutdownSignalException, ConsumerCancelledException {
			return receive(queue.take());
		}

		public BlockingConsumerMessage receive(long timeout) throws InterruptedException, ShutdownSignalException, ConsumerCancelledException {
			return receive(queue.poll(timeout, TimeUnit.MILLISECONDS));
		}

		private ShutdownSignalException wrap(String messagePrefix, ShutdownSignalException e) {
			throw new ShutdownSignalException(e.isHardError(), e.isInitiatedByApplication(), e.getReason(), e.getReference(), messagePrefix, e);
		}

		private BlockingConsumerMessage receive(BlockingConsumerMessage message) {

			if (message == BlockingConsumerMessage.poisonPill || message == null && (shutdownException != null || cancelledException != null)) {

				if (message == BlockingConsumerMessage.poisonPill) {
					queue.add(BlockingConsumerMessage.poisonPill);
					if (shutdownException == null && cancelledException == null) {
						throw new IllegalStateException("No cause for poison pill available");
					}
				}

				if (shutdownException != null) {
					throw wrap("Consumer in shutown mode", shutdownException);
				}

				if (cancelledException != null) {
					throw cancelledException;
				}

			}

			lastTake = System.currentTimeMillis();

			return message;

		}

	}

	private static class BlockingConsumerMessage {

		private static final BlockingConsumerMessage poisonPill = new BlockingConsumerMessage();

		private final Envelope envelope;
		private final AMQP.BasicProperties properties;
		private final byte[] body;

		public BlockingConsumerMessage() {
			this(null, null, null);
		}

		public BlockingConsumerMessage(Envelope envelope, BasicProperties properties, byte[] body) {
			super();
			this.envelope = envelope;
			this.properties = properties;
			this.body = body;
		}

	}

	private String messageLog(String context, BlockingConsumerMessage delivery) {
		return messageLog(context, getConsumerTag(), delivery.envelope, delivery.properties, delivery.body);
	}

	private String messageLog(String context, String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {

		StringBuilder sb = new StringBuilder();

		sb.append("\n").append(context).append(" message details:");
		sb.append("\n- consumer tag:  [").append(consumerTag).append("]");
		sb.append("\n- delivery tag:  [").append(envelope.getDeliveryTag()).append("]");
		sb.append("\n- routing key:   [").append(envelope.getRoutingKey()).append("]");
		sb.append("\n- content type:  [").append(properties.getContentType()).append("]");
		sb.append("\n- delivery mode: [").append(properties.getDeliveryMode()).append("]");
		sb.append("\n- priority:      [").append(properties.getPriority()).append("]");
		sb.append("\n- expiration:    [").append(properties.getExpiration()).append("]");
		sb.append("\n- body:          [").append(body != null ? new String(body) : "null").append("]");

		return sb.toString();

	}

}
