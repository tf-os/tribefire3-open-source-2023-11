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

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;

import javax.management.MBeanRegistration;

/**
 * <p>
 * A MBean proving access to named BlockingQueue(s) for messages exchange.
 * 
 */
public interface MessagingMBean extends MBeanRegistration {

	public static final String name = "com.braintribe.tribefire:type=MessagingMBean";

	/**
	 * <p>
	 * Establishes a connection to this MBean.
	 * 
	 * <p>
	 * Clients must call this method to ensure that the necessary resources and tasks are properly initialized.
	 * 
	 * @return A correlation connection id.
	 */
	Long connect();

	/**
	 * <p>
	 * Closes a connection to this MBean.
	 * 
	 * <p>
	 * Clients must call this method to ensure that connection specific resources are freed and unnecessary tasks
	 * terminated.
	 * 
	 * @param connectionId
	 *            The id of the connection to be closed
	 */
	void disconnect(Long connectionId);

	/**
	 * <p>
	 * Retrieves the {@link BlockingQueue} for a given destination and subscription.
	 * 
	 * @param destinationType
	 *            The type of the destination
	 * @param destinationName
	 *            The name of the destination
	 * @param subscriptionId
	 *            The id of the consumer's subscription
	 * @return A dedicated {@link BlockingQueue} for the consumer registered under the given subscription id
	 */
	BlockingQueue<? extends Function<String, Object>> getQueue(char destinationType, String destinationName, String subscriptionId);

	/**
	 * <p>
	 * Register a topic consumer.
	 * 
	 * @param destinationName
	 *            The name of the destination
	 * @param subscriptionId
	 *            The id of the consumer's subscription
	 * @return Whether the consumer subscription was successfully registered
	 */
	boolean subscribeTopicConsumer(String destinationName, String subscriptionId);

	/**
	 * <p>
	 * Unregisters a topic consumer.
	 * 
	 * @param destinationName
	 *            The name of the destination
	 * @param subscriptionId
	 *            The id of the consumer's subscription
	 * @return Whether the consumer subscription was successfully unregistered
	 */
	boolean unsubscribeTopicConsumer(String destinationName, String subscriptionId);

	/**
	 * <p>
	 * Publishes a message.
	 * 
	 * @param destinationType
	 *            The type of the destination
	 * @param destinationName
	 *            The name of the destination
	 * @param messageId
	 *            The id of the message
	 * @param message
	 *            The message payload
	 * @param priority
	 *            The message priority
	 * @param expiration
	 *            The message expiration
	 * @param headers
	 *            The message headers
	 * @param properties
	 *            The message properties
	 */
	void sendMessage(char destinationType, String destinationName, String messageId, byte[] message, int priority, long expiration,
			Map<String, Object> headers, Map<String, Object> properties);

}
