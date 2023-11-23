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

import com.braintribe.model.messaging.Message;

/**
 * <p>
 * Message listeners can be registered to message consumer thus allowing non-blocking asynchronous consumption.
 * 
 * <p>
 * Whenever a new message is consumed, the listener is notified through its {@link #onMessage(Message)} method.
 * 
 * @see MessageConsumer
 * @see MessageConsumer#setMessageListener(MessageListener)
 */
public interface MessageListener {

	/**
	 * <p>
	 * Called when a message is received by the {@link MessageConsumer} to which this listener was registered (
	 * {@link MessageConsumer#setMessageListener(MessageListener)}).
	 * 
	 * @param message
	 *            The received message
	 * @throws MessagingException
	 *             Signals to the consumer a failure while processing the given message. {@link MessageConsumer}
	 *             implementations might take distinguished actions when this happens.
	 */
	void onMessage(Message message) throws MessagingException;

}
