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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.logging.Logger;
import com.braintribe.model.messaging.Queue;
import com.braintribe.model.messaging.Topic;
import com.braintribe.transport.messaging.api.MessagingComponentStatus;
import com.braintribe.transport.messaging.api.MessagingConnection;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;
import com.rabbitmq.client.BlockedListener;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * <p>
 * {@link MessagingConnection} implementation representing a connection to a Rabbit MQ server.
 * 
 */
public class RabbitMqConnection implements MessagingConnection {

	private RabbitMqConnectionProvider connectionProvider;
	private Connection connection;
	private Connection publishingConnection;
	private boolean isAutomaticRecoveryEnabled;
	private Marshaller messageMarshaller;

	private Channel destinationDeclarationChannel;
	private boolean durableDestinations = true;
	private Set<String> declaredQueues = new HashSet<>();
	private Set<String> declaredTopics = new HashSet<>();
	
	private ReentrantLock connectionLock = new ReentrantLock();
	private long connectionLockTimeout = 10L;
	private TimeUnit connectionLockTimeoutUnit = TimeUnit.SECONDS;
	private MessagingComponentStatus status = MessagingComponentStatus.NEW;
	
	private Set<RabbitMqSession> sessions = new HashSet<RabbitMqSession>();
	
	private static final Logger log = Logger.getLogger(RabbitMqConnection.class);
	
	public RabbitMqConnection() {
	}
	
	public void setConnection(Connection connection) {
		LoggingConnectionListener listener = new LoggingConnectionListener("standard");
		connection.addShutdownListener(listener);
		connection.addBlockedListener(listener);
		this.connection = connection;
	}
	
	public void setPublishingConnection(Connection connection) {
		LoggingConnectionListener listener = new LoggingConnectionListener("publishing");
		connection.addShutdownListener(listener);
		connection.addBlockedListener(listener);
		this.publishingConnection = connection;
	}
	
	public void setAutomaticRecoveryEnabled(boolean connectionIsRecoverable) {
		this.isAutomaticRecoveryEnabled = connectionIsRecoverable;
	}
	
	public RabbitMqConnectionProvider getConnectionProvider() {
		return connectionProvider;
	}

	public void setConnectionProvider(RabbitMqConnectionProvider connectionProvider) {
		this.connectionProvider = connectionProvider;
	}
	
	@Override
	public void open() throws MessagingException {
		
		try {
			if (connectionLock.tryLock(connectionLockTimeout, connectionLockTimeoutUnit)) {
				try {
					
					if (status == MessagingComponentStatus.CLOSING || status == MessagingComponentStatus.CLOSED) {
						throw new MessagingException("Connection in unexpected state: "+status.toString().toLowerCase());
					}
					
					if (status == MessagingComponentStatus.OPEN) {
						//opening an already opened connection shall be a no-op
						if (log.isTraceEnabled()) {
							log.trace("open() called in an already opened connection. Connection [ "+connection+" ] already established.");
						}
						return;
					}
					
					if (log.isDebugEnabled()) {
						log.debug("Connection opened: [ "+this+" ]");
					} else if (log.isInfoEnabled()) {
						log.info("Connection opened");
					}
					
					this.status = MessagingComponentStatus.OPEN;
					
				} finally {
					connectionLock.unlock();
				}
			} else {
				throw createLockFailure("open the connection", null);
			}
		} catch (InterruptedException e) {
			throw createLockFailure("open the connection", e);
		}
		
	}

	@Override
	public void close() throws MessagingException {
		
		try {
			if (connectionLock.tryLock(connectionLockTimeout, connectionLockTimeoutUnit)) {
				
				try {
					
					MessagingComponentStatus previousStatus = this.status;
					
					this.status = MessagingComponentStatus.CLOSING;
					
					if (previousStatus == MessagingComponentStatus.CLOSING || previousStatus == MessagingComponentStatus.CLOSED) {
						//closing an already closed connection shall be a no-op
						if (log.isDebugEnabled()) {
							log.debug("No-op close() call. Connection closing already requested. current state: "+previousStatus.toString().toLowerCase());
						}
						return;
					}
					
					if (previousStatus == MessagingComponentStatus.NEW && log.isTraceEnabled()) {
						log.trace("Closing a connection which was not opened. current state: "+previousStatus.toString().toLowerCase());
					}
					
					try {
						
						synchronized (sessions) {
							for (MessagingSession session : sessions) {
								try {
									session.close();
								} catch (Throwable t) {
									log.error("Failed to close session created by this messaging connection: "+session+": "+t.getMessage(), t);
								}
							}
							sessions.clear();
						}

						try {
							if (destinationDeclarationChannel != null) {
								destinationDeclarationChannel.close();
							}
						} catch (Throwable t) {
							log.error("Failed to close Rabbit MQ channel created by this messaging connection: "+destinationDeclarationChannel+": "+t.getMessage(), t);
						}
						
						this.destinationDeclarationChannel = null;

						this.declaredQueues.clear();
						this.declaredTopics.clear();
						
						this.messageMarshaller = null;
						
						this.status = MessagingComponentStatus.CLOSED;
						
						if (log.isDebugEnabled()) {
							log.debug("Connection closed: [ "+this+" ]");
						} else if (log.isInfoEnabled()) {
							log.info("Connection closed");
						}
						
					} finally {
						if (connection != null) {
							try {
								connection.close();
								connection = null;
							} catch (com.rabbitmq.client.AlreadyClosedException e) {
								if (log.isDebugEnabled()) {
									if (log.isTraceEnabled()) {
										log.trace("Connection already closed: "+e.getMessage(), e);
									} else {
										log.debug("Connection already closed: "+e.getMessage());
									}
								}
							} catch (Exception e) {
								log.error("Failed to close connection", e);
							}
						}
						if (publishingConnection != null) {
							try {
								publishingConnection.close();
								publishingConnection = null;
							} catch (com.rabbitmq.client.AlreadyClosedException e) {
								if (log.isDebugEnabled()) {
									if (log.isTraceEnabled()) {
										log.trace("Connection already closed: "+e.getMessage(), e);
									} else {
										log.debug("Connection already closed: "+e.getMessage());
									}
								}
							} catch (Exception e) {
								log.error("Failed to close publishing connection", e);
							}
						}
					}
					
				} finally {
					connectionLock.unlock();
				}
			} else {
				throw createLockFailure("close the connection", null);
			}
		} catch (InterruptedException e) {
			throw createLockFailure("close the connection", e);
		}
		
	}
	
	@Override
	public RabbitMqSession createMessagingSession() throws MessagingException {
		
		if (log.isDebugEnabled()) {
			log.debug("Creating messaging session");
		}
		
		open();
		
		RabbitMqSession session = new RabbitMqSession();
		session.setConnection(this);
		session.open();
		
		synchronized (sessions) {
			assertOpen();
			sessions.add(session);
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Messaging session created [ "+session+" ]");
		}
		
		return session;
	}
	
	protected boolean isAutomaticRecoveryEnabled() {
		return isAutomaticRecoveryEnabled;
	}
	
	private Channel getDestinationDeclarationChannel() throws MessagingException, IOException {
		
		boolean created = false;
		
		if (destinationDeclarationChannel == null || (!isAutomaticRecoveryEnabled && !destinationDeclarationChannel.isOpen())) {
			
			try {
				if (connectionLock.tryLock(connectionLockTimeout, connectionLockTimeoutUnit)) {
					
					try {
						
						if (destinationDeclarationChannel == null || (!isAutomaticRecoveryEnabled && !destinationDeclarationChannel.isOpen())) {
							
							if (log.isTraceEnabled()) {
								log.trace("Creating a channel for defining destinations with connection [ "+connection+" ]");
							}
							
							this.destinationDeclarationChannel = connection.createChannel();
							
							if (log.isDebugEnabled()) {
								log.debug("Created a channel for defining destinations [ "+destinationDeclarationChannel+" ] with connection [ "+connection+" ]");
							}
							
						}
						
					} finally {
						connectionLock.unlock();
					}
					
				} else {
					throw createLockFailure("create channel for defining destinations", null);
				}
			} catch (InterruptedException e) {
				throw createLockFailure("create channel for defining destinations", e);
			}
			
		}
		
		if (log.isTraceEnabled() && !created) {
			log.trace("Returning existing channel [ "+destinationDeclarationChannel+" ]");
		}
		
		return destinationDeclarationChannel;
		
	}
	
	/**
	 * <p>
	 * Declares a {@link Queue} as a AMQP queue to the RabbitMQ broker, if the queue wasn't already declared by this
	 * connection.
	 * 
	 * @param queue
	 *            The {@link Queue} to be declared as a AMQP queue to the RabbitMQ broker
	 * @throws MessagingException
	 *             If this connection fails to declare the queue
	 */
	protected void declare(Queue queue) throws MessagingException {

		String name = queue.getName();
		
		if (declaredQueues.contains(name)) {
			if (log.isDebugEnabled()) {
				log.debug("Queue [ "+name+" ] already declared");
			}
			return;
		}
		
		try {
			if (connectionLock.tryLock(connectionLockTimeout, connectionLockTimeoutUnit)) {
				try {
					
					if (declaredQueues.contains(name)) {
						if (log.isTraceEnabled()) {
							log.trace("Queue [ "+name+" ] already declared by another Thread");
						}
						return;
					}
					
					if (log.isTraceEnabled()) {
						log.trace("Obtaining channel in order to declare queue [ "+name+" ]");
					}
					
					Channel queueChannel = getDestinationDeclarationChannel();

					if (log.isTraceEnabled()) {
						log.trace("Declaring queue [ "+name+" ] with channel [ "+queueChannel+" ]");
					}
					
					queueChannel.queueDeclare(name, durableDestinations, false, false, null);
					
					if (log.isDebugEnabled()) {
						log.debug("Declared queue [ "+name+" ] with channel [ "+queueChannel+" ]");
					} else if (log.isInfoEnabled()) {
						log.info("Declared queue [ "+name+" ]");
					}
					
					declaredQueues.add(name);
					
				} catch (Exception e) { 
					throw new MessagingException("Failed to declare queue [ "+name+" ]: "+(e.getMessage() != null ? ": "+e.getMessage() : ""), e);
				} finally {
					connectionLock.unlock();
				}
			} else {
				throw createLockFailure("declare queue [ "+name+" ]", null);
			}
		} catch (InterruptedException e) {
			throw createLockFailure("declare queue [ "+name+" ]", e);
		}
		
	}

	/**
	 * <p>
	 * Declares a {@link Topic} as a AMQP topic exchange to the RabbitMQ broker, if the exchange wasn't already declared
	 * by this connection.
	 * 
	 * @param topic
	 *            The {@link Topic} to be declared as a AMQP topic exchange to the RabbitMQ broker
	 * @throws MessagingException
	 *             If this connection fails to declare the topic
	 */
	protected void declare(Topic topic) throws MessagingException {
		
		String name = topic.getName();
		
		if (declaredTopics.contains(name)) {
			if (log.isDebugEnabled()) {
				log.debug("Topic [ "+name+" ] already declared");
			}
			return;
		}
		
		try {
			if (connectionLock.tryLock(connectionLockTimeout, connectionLockTimeoutUnit)) {
				try {
					
					if (declaredTopics.contains(name)) {
						if (log.isTraceEnabled()) {
							log.trace("Topic [ "+name+" ] already declared by another Thread");
						}
						return;
					}
					
					if (log.isTraceEnabled()) {
						log.trace("Obtaining channel in order to declare topic [ "+name+" ]");
					}
					
					Channel topicChannel = getDestinationDeclarationChannel();

					if (log.isTraceEnabled()) {
						log.trace("Declaring topic [ "+name+" ] with channel [ "+topicChannel+" ]");
					}

					topicChannel.exchangeDeclare(name, "topic", durableDestinations, false, false, null);
					
					if (log.isDebugEnabled()) {
						log.debug("Declared topic [ "+name+" ] with channel [ "+topicChannel+" ]");
					} else if (log.isInfoEnabled()) {
						log.info("Declared topic [ "+name+" ]");
					}
					
					declaredTopics.add(name);
					
				} catch (Exception e) { 
					throw new MessagingException("Failed to declare topic [ "+name+" ]: "+(e.getMessage() != null ? ": "+e.getMessage() : ""), e);
				} finally {
					connectionLock.unlock();
				}
			} else {
				throw createLockFailure("declare topic [ "+name+" ]", null);
			}
		} catch (InterruptedException e) {
			throw createLockFailure("declare topic [ "+name+" ]", e);
		}
		
	}
	
	protected Channel createChannel() throws MessagingException {
		
		assertOpen();
		
		try {
			return connection.createChannel();
		} catch (Exception e) {
			throw new MessagingException("Failed to create a channel", e);
		}
		
	}
	
	protected Channel createPublishingChannel() throws MessagingException {
		
		assertOpen();
		
		try {
			return publishingConnection.createChannel();
		} catch (Exception e) {
			throw new MessagingException("Failed to create a publishing channel", e);
		}
		
	}
	
	/**
	 * <p>Asserts that this connection is in a valid state to be used: Already open. not "closing" nor "closed";
	 * 
	 * <p>This method does not try to open a new connection.
	 * 
	 * @throws MessagingException
	 *             If this connection is NOT in a valid state to be used
	 */
	protected void assertOpen() throws MessagingException {
		if (status != MessagingComponentStatus.OPEN) {
			throw new MessagingException("Messaging connection is not opened. Current state: "+status.toString().toLowerCase());
		}
	}
	
	private MessagingException createLockFailure(String operation, Exception cause) {
		return new MessagingException("Failed to "+operation+". Unable to acquire lock after "+connectionLockTimeout+" "+connectionLockTimeoutUnit.toString().toLowerCase(), cause);
	}
	
	private class LoggingConnectionListener implements ShutdownListener, BlockedListener {
		
		private String identifier;
		
		private LoggingConnectionListener(String identifier) {
			this.identifier = "Rabbit MQ "+(identifier != null ? identifier+" " : "")+"connection";
		}

		@Override
		public void handleBlocked(String reason) throws IOException {
			if (log.isInfoEnabled()) {
				log.info(identifier+" blocked. Reason: [ "+reason+" ]");
			}
		}

		@Override
		public void handleUnblocked() throws IOException {
			if (log.isInfoEnabled()) {
				log.info(identifier+" unblocked.");
			}
		}

		@Override
		public void shutdownCompleted(ShutdownSignalException cause) {
			if (cause.isInitiatedByApplication()) {
				if (log.isTraceEnabled()) {
					log.trace("Expected (initiated by the application) shutdown signal of "+identifier+". Cause: "+cause, cause);
				}
			} else {
				if (isAutomaticRecoveryEnabled()) {
					if (log.isTraceEnabled()) {
						log.trace("Expected (from auto recoverable connection) shutdown signal of "+identifier+". Cause: "+cause, cause);
					} else if (log.isInfoEnabled()) {
						log.info("Expected (from auto recoverable connection) shutdown signal of "+identifier+". Cause: "+cause);
					}
				} else {
					log.error("Shutdown signal received from unrecoverable connection. Make sure the "+identifier+" is auto recoverable.", cause);
				}
			}
		}
		
	}

}
