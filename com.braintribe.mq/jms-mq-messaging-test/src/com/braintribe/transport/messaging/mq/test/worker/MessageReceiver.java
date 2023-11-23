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
package com.braintribe.transport.messaging.mq.test.worker;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.messaging.Message;
import com.braintribe.transport.messaging.api.MessageConsumer;

public class MessageReceiver implements Runnable {

	protected MessageConsumer messageConsumer = null;
	protected int expectedMessages = 0;
	protected long timeout = 60000L;

	protected List<Message> receivedMessages = new ArrayList<Message>();
	protected Throwable throwable = null;
	protected boolean done = false;

	public static List<Message> receiveMessagesSync(MessageConsumer messageConsumer, int expectedMessages, long timeout) throws Exception {
		MessageReceiver receiver = new MessageReceiver(messageConsumer, expectedMessages, timeout);
		receiver.run();
		receiver.waitFor();

		List<Message> receivedMessages = receiver.getReceivedMessages();
		if (receiver.getThrowable() != null) {
			throw new Exception("Receiver received an exception", receiver.getThrowable());
		} else {
			if (receivedMessages.size() != expectedMessages) {
				throw new AssertionError("Expected "+expectedMessages+" messages but received only "+receivedMessages.size());
			}
		}
		return receivedMessages;
	}

	public static MessageReceiver receiveMessagesAsync(MessageConsumer messageConsumer, int expectedMessages, long timeout) {
		MessageReceiver receiver = new MessageReceiver(messageConsumer, expectedMessages, timeout);
		Thread t = new Thread(receiver, "Receiver");
		t.setDaemon(true);
		t.start();
		return receiver;
	}

	public MessageReceiver(MessageConsumer messageConsumer, int expectedMessages, long timeout) {
		this.messageConsumer = messageConsumer;
		this.expectedMessages = expectedMessages;
		this.timeout = timeout;
	}

	@Override
	public void run() {

		long start = System.currentTimeMillis();

		while (true) {
			if (receivedMessages.size() >= expectedMessages) {
				break;
			}
			if ((System.currentTimeMillis() - start) >= timeout) {
				break;
			}

			try {
				Message message = this.messageConsumer.receive(1000L);
				if (message != null) {
					this.receivedMessages.add(message);
				}
			} catch(Throwable t) {
				this.throwable = t;
				break;
			}
		}
		this.done = true;
		synchronized(this) {
			notify();
		}
	}

	public void waitFor() throws InterruptedException {
		synchronized(this) {
			if (done) {
				return;
			}
			wait(this.timeout);
		}
	}
	public List<Message> getReceivedMessages() {
		return receivedMessages;
	}
	public Throwable getThrowable() {
		return throwable;
	}

}
