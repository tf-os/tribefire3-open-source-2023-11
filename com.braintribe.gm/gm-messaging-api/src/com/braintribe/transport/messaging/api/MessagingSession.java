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
package com.braintribe.transport.messaging.api;

import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Queue;
import com.braintribe.model.messaging.Topic;

/**
 * <p>
 * A session is used for creating the main components used for messaging:
 * 
 * <ul>
 * <li>{@link Destination}(s) such as {@link Queue}(s) and {@link Topic}(s);</li>
 * <li>{@link Message}(s) objects;</li>
 * <li>{@link MessageConsumer}(s);</li>
 * <li>{@link MessageProducer}(s).</li>
 * </ul>
 * 
 */
public interface MessagingSession {

	/**
	 * <p>
	 * Opens (or starts) the messaging session.
	 * 
	 * @throws MessagingException
	 *             If the session fails to be opened
	 */
	void open() throws MessagingException;

	/**
	 * <p>
	 * Closes the messaging session, ensuring that {@link MessageProducer}(s) and {@link MessageConsumer}(s) created
	 * through it are also closed.
	 * 
	 * <p>
	 * Closing an already closed session has no effect.
	 * 
	 * @throws MessagingException
	 *             If the session fails to be closed
	 */
	void close() throws MessagingException;

	/**
	 * <p>
	 * Creates a {@link Queue} destination with the given {@code name}.
	 * 
	 * @param name
	 *            The name of the queue destination.
	 * @return A {@link Queue} with the specified name.
	 * @throws MessagingException
	 *             If:
	 *             <ul>
	 *             <li>An error occur while creating the destination in the underlying message broker;
	 *             <li>the given {@code name} argument is not a valid destination name for the underlying message
	 *             broker. {@code null} or empty String(s) are very unlikely to be valid; moreover, messaging
	 *             implementations might have specific limitation regarding length and types of characters allowed in
	 *             destination names.
	 *             </ul>
	 */
	Queue createQueue(String name) throws MessagingException;

	/**
	 * <p>
	 * Creates a {@link Topic} destination with the given {@code name}.
	 * 
	 * @param name
	 *            The name of the topic destination.
	 * @return A {@link Topic} with the specified name.
	 * @throws MessagingException
	 *             If:
	 *             <ul>
	 *             <li>An error occur while creating the destination in the underlying message broker;
	 *             <li>the given {@code name} argument is not a valid destination name for the underlying message
	 *             broker. {@code null} or empty String(s) are very unlikely to be valid; moreover, messaging
	 *             implementations might have specific limitation regarding length and types of characters allowed in
	 *             destination names.
	 *             </ul>
	 */
	Topic createTopic(String name) throws MessagingException;

	/**
	 * <p>
	 * Creates a {@link Message} object.
	 * 
	 * @return A {@link Message}
	 * @throws MessagingException
	 *             If a message fails to be created
	 */
	Message createMessage() throws MessagingException;

	/**
	 * <p>
	 * Creates an unidentified {@link MessageProducer}.
	 * 
	 * @return A {@link MessageProducer} for producing messages to unspecified destination.
	 * @throws MessagingException
	 *             If a message producer fails to be created
	 */
	MessageProducer createMessageProducer() throws MessagingException;

	/**
	 * <p>
	 * Creates an identified {@link MessageProducer} for the given {@link Destination}.
	 * 
	 * @param destination
	 *            {@link Destination} to have messages produced to.
	 * @return A {@link MessageProducer} for producing messages to the specified destination.
	 * @throws MessagingException
	 *             If a message producer fails to be created
	 */
	MessageProducer createMessageProducer(Destination destination) throws MessagingException;

	/**
	 * <p>
	 * Creates a {@link MessageConsumer} for the given {@link Destination}.
	 * 
	 * @param destination
	 *            {@link Destination} to have its messages consumed from.
	 * @return A {@link MessageConsumer} for consuming messages from the specified destination.
	 * @throws MessagingException
	 *             If a message consumer fails to be created
	 */
	MessageConsumer createMessageConsumer(Destination destination) throws MessagingException;

}
