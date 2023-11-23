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

import java.util.concurrent.Callable;

import com.braintribe.transport.jms.IServer;
import com.braintribe.transport.jms.message.IMessageContext;

public interface IQueueCommunication extends Callable<Boolean> {

	public enum QueueOperation {
		READ, WRITE, READWRITE
	};

	public void setServer(IServer server);

	public IServer getServer();

	public IQueueContext getQueueContext(boolean shared) throws Exception;
	
	public String getQueueName();
	
	public IMessageContext receiveMessage() throws Exception;
}
