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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.execution.errorhandler.ErrorHandler;
import com.braintribe.logging.Logger;
import com.braintribe.transport.jms.IServer;
import com.braintribe.transport.jms.message.IMessageContext;
import com.braintribe.transport.jms.message.MessageContext;
import com.braintribe.transport.jms.processor.MessageProcessor;
import com.braintribe.transport.jms.queuecomm.error.FixedIntervalErrorHandler;
import com.braintribe.transport.jms.util.JmsUtil;

public class DirectProcessingQueueCommunication implements IQueueCommunication {

	protected static Logger logger = Logger.getLogger(DirectProcessingQueueCommunication.class);

	protected IServer server = null;
	protected String queueName = null;

	protected boolean stopProcessing = false;

	protected long timeout = 5000L;
	protected ErrorHandler<IQueueContext> queueContextErrorHandler = new FixedIntervalErrorHandler();

	protected MessageProcessor messageProcessor = null;

	protected IQueueContext queueContext = null;

	protected boolean transactionalSession = false;
	protected int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;

	protected QueueOperation queueOperation = QueueOperation.READ;
	protected IQueueCommunication replyQueue = null;

	// Good for testing purposes
	protected boolean waitForSingleMessage = false;

	@Override
	public Boolean call() throws Exception {
		if (this.queueOperation == QueueOperation.WRITE) {
			logger.debug("The queue communication for " + queueName
					+ " is configured to write only. Not receiving any messages. Please change the configuration.");
			return Boolean.FALSE;
		}

		if (this.messageProcessor == null) {
			throw new Exception("There is no message processor configured.");
		}

		try {
			this.getQueueContext(true);
		} catch (Exception e) {
			logger.error("Could not access queue " + this.queueName, e);
			return Boolean.FALSE;
		}

		while (!this.stopProcessing) {
			try {
				Message message = this.queueContext.receiveMessage(this.timeout);

				if (this.queueContextErrorHandler != null) {
					this.queueContextErrorHandler.reset(queueContext);
				}

				if (message != null) {

					String messageInfo = this.getMessageShortInfo(message);

					if (logger.isTraceEnabled()) {
						logger.trace("Received message from queue " + this.queueName + ": " + message
								+ " (destination: " + this.queueContext.getDestination() + ")");
					}
					if (logger.isDebugEnabled()) {
						if (message.getJMSRedelivered()) {
							logger.debug("Received re-delivered message from queue " + this.queueName + ": " + message
									+ " (destination: " + this.queueContext.getDestination() + ")");
						}
					}

					IMessageContext mc = new MessageContext(this.queueContext, message);
					mc.setReplyQueue(this.replyQueue);

					try {
						logger.pushContext(messageInfo);
						this.messageProcessor.processMessageContext(mc);
					} catch(Throwable t) {
						logger.error("Message processor "+this.messageProcessor+" threw an error while processing message context "+mc, t);
					} finally {
						logger.popContext();
					}

					//Is this just a test?
					if (this.waitForSingleMessage) {
						break;
					}
				}
			} catch (Exception e) {
				JmsUtil.logError(logger, e, "Error while receiving message from queue " + this.queueName);
				if (this.queueContextErrorHandler != null) {
					this.queueContextErrorHandler.handleError(e, queueContext);
				} else {
					synchronized (this) {
						wait(this.timeout);
					}
				}
			}
		}

		return Boolean.TRUE;
	}

	protected String getMessageShortInfo(Message message) {
		String jmsMessageId = "";
		try {
			jmsMessageId = message.getJMSMessageID();
		} catch (Exception e) {
			logger.debug("Could not get the JMS Message Id from message "+message, e);
		}
		String jmsMessageCorrelationId = null;
		try {
			jmsMessageCorrelationId = message.getJMSCorrelationID();
		} catch (Exception e) {
			logger.debug("Could not get the JMS Correlation Message Id from message "+message, e);
		}
		StringBuilder sb = new StringBuilder();
		sb.append("JmsId:");
		sb.append(jmsMessageId);
		if ((jmsMessageCorrelationId != null) && (jmsMessageCorrelationId.trim().length() > 0)) {
			sb.append(",JmsCorrelationId:");
			sb.append(jmsMessageCorrelationId);
		}
		return sb.toString();
	}

	@PostConstruct
	public void initialize() throws Exception {
		if (this.queueOperation == null) {
			throw new Exception("The queue operation of queue "+queueName+" is not specified.");
		}
		if (this.queueOperation.equals(QueueOperation.READ)) {
			if (this.messageProcessor == null) {
				logger.debug("The message processor for queue "+queueName+" is not configured although the queue operation is READ.");
			}
		} else if (this.queueOperation.equals(QueueOperation.WRITE)) {
			if (this.messageProcessor != null) {
				logger.warn("Although the queue operation is WRITE for queue "+queueName+", there is a message processor configured.");
			}
		}
	}

	@Override
	public IQueueContext getQueueContext(boolean shared) throws Exception {
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
	public MessageContext receiveMessage() throws Exception {
		MessageContext mc = null;
		while (mc == null) {
			try {
				this.getQueueContext(true);

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

	@PreDestroy 
	public void destroy() throws Exception {
		this.stopProcessing = true;
	}

	@Override
	@Required
	public void setServer(IServer server) {
		this.server = server;
	}

	@Override
	public IServer getServer() {
		return this.server;
	}

	@Required
	public void setQueueName(String queueName) {
		this.queueName = queueName;
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
	public void setQueueOperation(QueueOperation queueOperation) {
		this.queueOperation = queueOperation;
	}

	@Configurable
	public void setReplyQueue(IQueueCommunication replyQueue) {
		this.replyQueue = replyQueue;
	}

	@Configurable
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	@Configurable
	public void setWaitForSingleMessage(boolean waitForSingleMessage) {
		this.waitForSingleMessage = waitForSingleMessage;
	}

	@Configurable
	public void setMessageProcessor(MessageProcessor messageProcessor) {
		this.messageProcessor = messageProcessor;
	}

	@Override
	public String getQueueName() {
		return this.queueContext.getQueueName();
	}

	public ErrorHandler<IQueueContext> getQueueContextErrorHandler() {
		return queueContextErrorHandler;
	}
	@Configurable
	public void setQueueContextErrorHandler(ErrorHandler<IQueueContext> queueContextErrorHandler) {
		this.queueContextErrorHandler = queueContextErrorHandler;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("DirectProcessingQueueCommunication: ");
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
