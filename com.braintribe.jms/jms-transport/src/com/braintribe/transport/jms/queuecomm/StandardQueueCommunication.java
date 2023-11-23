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

import java.util.concurrent.BlockingQueue;

import javax.annotation.PreDestroy;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.transport.jms.IServer;
import com.braintribe.transport.jms.message.IMessageContext;
import com.braintribe.transport.jms.message.MessageContext;
import com.braintribe.transport.jms.util.JmsUtil;

/**
 * @author roman.kurmanowytsch
 * @deprecated The way how this implementation handles messages is not safe, as the acknowledgement
 * 	of messages may not work as expected. When acknowledging a message, the JMS server may assume
 *	that *all* messages that have been received are acknowledged, not just the present one.
 *	Since this class reads many messages and puts them into an in-memory buffer, there might be 
 *	data loss. Use this implementation only when the loss of data is acceptable.
 *	Use {@link DirectProcessingQueueCommunication}.
 *	From the IBM MQ manual: The application can acknowledge the receipt of each message individually, 
 *		or it can receive a batch of messages and call the Acknowledge method only for the last message it receives. 
 *		When the Acknowledge method is called all messages received since the last time the method
 *		was called are acknowledged.
 */
@Deprecated
public class StandardQueueCommunication implements IQueueCommunication {

	protected static Logger logger = Logger.getLogger(StandardQueueCommunication.class);

	protected IServer server = null;
	protected String queueName = null;

	protected boolean stopProcessing = false;

	protected long timeout = 5000L;

	protected BlockingQueue<IMessageContext> messageQueue = null;

	protected QueueOperation queueOperation = QueueOperation.READ;
	protected IQueueCommunication replyQueue = null;

	protected IQueueContext queueContext = null;

	protected boolean transactionalSession = false;
	protected int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;

	// Good for testing purposes
	protected boolean waitForSingleMessage = false;
	
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public Boolean call() throws Exception {

		if (this.queueOperation == QueueOperation.WRITE) {
			logger.debug("The queue communication for " + queueName
					+ " is configured to write only. Not receiving any messages. Please change the configuration.");
			return Boolean.FALSE;
		}

		try {
			this.getQueueContext(true);
		} catch (Exception e) {
			logger.error("Could not access queue " + this.queueName, e);
			return Boolean.FALSE;
		}

		while (!this.stopProcessing) {
			try {
				// logger.info("Receiving from "+this.queueName);
				Message message = this.queueContext.receiveMessage(this.timeout);
				if (message != null) {
					if (logger.isTraceEnabled()) {
						logger.trace("Received message from queue " + this.queueName + ": " + message
								+ " (destination: " + this.queueContext.getDestination() + ")");
					}

					IMessageContext mc = new MessageContext(this.queueContext, message);
					mc.setReplyQueue(this.replyQueue);
					this.messageQueue.put(mc);
					
					//Is this just a test?
					if (this.waitForSingleMessage) {
						break;
					}
				}
			} catch (Exception e) {
				JmsUtil.logError(logger, e, "Error while receiving message from queue " + this.queueName);
				synchronized (this) {
					wait(this.timeout);
				}
			}
		}

		return Boolean.TRUE;
	}
	
	/*
	 * Alternative way to receive messages directly.
	 * This makes it unnecessary to run this object in an executor
	 */
	@Override
	public MessageContext receiveMessage() throws Exception {

		MessageContext mc = null;
		while (mc == null) {
			try {
				this.getQueueContext(true);

				// logger.info("Receiving from "+this.queueName);
				Message message = this.queueContext.receiveMessage(-1);
				if (message != null) {
					if (logger.isTraceEnabled()) {
						logger.trace("Received message from queue " + this.queueName + ": " + message
								+ " (destination: " + this.queueContext.getDestination() + ")");
					}

					mc = new MessageContext(this.queueContext, message);
					mc.setReplyQueue(this.replyQueue);
					return mc;
				}
			} catch(InterruptedException ie) {
				throw ie;
			} catch (Exception e) {
				if (e instanceof JMSException) {
					JMSException jmse = (JMSException) e;
					Exception linkede = jmse.getLinkedException();
					if ((linkede != null) && (linkede instanceof InterruptedException)) {
						//interrupted
						throw linkede;
					}
				}
				JmsUtil.logError(logger, e, "Error while receiving message from queue " + this.queueName);
				synchronized (this) {
					wait(this.timeout);
				}
			}
		}
		return mc;
	}

	@Override
	public synchronized IQueueContext getQueueContext(boolean shared) throws Exception {
		if (shared) {
			if (this.queueContext != null) {
				return this.queueContext;
			}
			try {
				this.queueContext = this.server.getQueueContext(this.queueName, this.transactionalSession,
						this.acknowledgeMode);
				this.queueContext.setQueueOperation(this.queueOperation);
				return this.queueContext;
			} catch (Exception e) {
				throw new Exception("Could not access queue " + this.queueName, e);
			}
		} else {
			IQueueContext qc = this.server.getQueueContext(this.queueName, this.transactionalSession, this.acknowledgeMode);
			qc.setQueueOperation(this.queueOperation);
			return qc;
		}

	}

	@Override
	public IServer getServer() {
		return server;
	}

	@Override
	@Required
	public void setServer(IServer server) {
		this.server = server;
	}

	@PreDestroy 
	public void destroy() throws Exception {
		this.stopProcessing = true;
	}

	@Required
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	@Override
	public String getQueueName() {
		return this.queueName;
	}

	@Configurable
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	@Configurable
	public void setMessageQueue(BlockingQueue<IMessageContext> messageQueue) {
		this.messageQueue = messageQueue;
	}

	@Configurable
	public void setQueueOperation(QueueOperation queueOperation) {
		this.queueOperation = queueOperation;
	}

	public IQueueCommunication getReplyQueue() {
		return replyQueue;
	}

	@Configurable
	public void setReplyQueue(IQueueCommunication replyQueue) {
		this.replyQueue = replyQueue;
	}

	@Configurable
	public void setTransactionalSession(boolean transactionalSession) {
		this.transactionalSession = transactionalSession;
	}

	@Configurable
	public void setAcknowledgeMode(int acknowledgeMode) {
		this.acknowledgeMode = acknowledgeMode;
	}

	@Configurable
	public void setWaitForSingleMessage(boolean waitForSingleMessage) {
		this.waitForSingleMessage = waitForSingleMessage;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("StandardQueueCommunication: ");
		sb.append("Server: ("+this.server+")");
		sb.append(", Queue Name: "+this.queueName);
		sb.append(", Queue Context: ("+this.queueContext+")");
		sb.append(", Transactional Session: "+this.transactionalSession);
		sb.append(", Ack Mode: "+this.acknowledgeMode);
		sb.append(", Queue Operation: "+this.queueOperation);
		sb.append(", Reply Queue: ("+this.replyQueue+")");
		return sb.toString();
	}

}
