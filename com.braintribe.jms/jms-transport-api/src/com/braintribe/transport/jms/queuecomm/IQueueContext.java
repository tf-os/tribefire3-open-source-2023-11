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
package com.braintribe.transport.jms.queuecomm;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.braintribe.transport.jms.IServer;
import com.braintribe.transport.jms.queuecomm.IQueueCommunication.QueueOperation;

public interface IQueueContext {
		public Message receiveMessage(long timeout) throws Exception;

		public void connect() throws Exception;

		public void disconnect();

		public String getQueueName();

		public Session getResponseSession();

		public void reconnect() throws Exception;

		public void reply(Message replyMsg, Destination replyDestination) throws Exception;

		public void send(Message msg, int deliveryMode, int priority, long timeToLive) throws Exception;

		public void send(Message msg) throws Exception;

		public void commit(boolean hideErrors) throws Exception;
		
		public void rollback(boolean hideErrors) throws Exception;
		
		public MapMessage createMapMessage() throws Exception;

		public ObjectMessage createObjectMessage() throws Exception;

		public TextMessage createTextMessage() throws Exception;

		public BytesMessage createBytesMessage() throws Exception;

		public QueueOperation getQueueOperation();
		
		public void setQueueOperation(QueueOperation queueOperation);

		public String getMessageSelector();

		public void setMessageSelector(String messageSelector);
		
		public boolean isTransactionalSession();

		public Destination getDestination();

		public Destination createTemporaryResponseQueue() throws Exception;

		public Destination getDestination(String queueName) throws Exception;
		
		
	/*
	 * Getters and Setters
	 */
		public IServer getServer();

		public void setServer(IServer arg);

		public int getAcknowledgeMode();

		public void setAcknowledgeMode(int arg);

		public void setQueueName(String queueName);

}
