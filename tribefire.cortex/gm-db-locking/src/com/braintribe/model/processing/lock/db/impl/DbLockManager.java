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
package com.braintribe.model.processing.lock.db.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import javax.sql.DataSource;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Topic;
import com.braintribe.model.processing.lock.api.LockBuilder;
import com.braintribe.model.processing.lock.api.LockManager;
import com.braintribe.model.processing.lock.impl.AbstractLockBuilder;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageListener;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;
import com.braintribe.util.jdbc.DatabaseTypes;
import com.braintribe.util.jdbc.JdbcTools;
import com.braintribe.util.jdbc.JdbcTypeSupport;
import com.braintribe.util.network.NetworkTools;
import com.braintribe.utils.DigestGenerator;
import com.braintribe.utils.IOTools;

@Deprecated
public class DbLockManager implements LockManager, LifecycleAware {
	private static final Logger logger = Logger.getLogger(DbLockManager.class);
	private DataSource dataSource = null;

	private Supplier<MessagingSession> messagingSessionProvider;
	private int pollIntervalInMillies = 100;
	private MessagingSession messagingSession = null;
	private boolean autoUpdateSchema = true;
	private String createTableStatement = null;
	private String topicName = "tf-unlock";
	private long topicExpiration = 5000L;
	private Topic topic;
	private MessageProducer messageProducer;
	private long lockTtl = -1L;

	protected boolean messagingInitialized = false;

	@Required
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Configurable
	public void setTopicExpiration(long topicExpiration) {
		this.topicExpiration = topicExpiration;
	}

	@Configurable
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	@Configurable
	public void setCreateTableStatement(String createTableStatement) {
		this.createTableStatement = createTableStatement;
	}

	@Configurable
	public void setAutoUpdateSchema(boolean autoUpdateSchema) {
		this.autoUpdateSchema = autoUpdateSchema;
	}

	@Configurable
	public void setMessagingSessionProvider(Supplier<MessagingSession> messagingSessionProvider) {
		this.messagingSessionProvider = messagingSessionProvider;
	}

	@Configurable
	public void setPollIntervalInMillies(int pollIntervalInMillies) {
		this.pollIntervalInMillies = pollIntervalInMillies;
	}

	@Override
	public void postConstruct() {

		if (createTableStatement == null) {
			DatabaseTypes dbTypes = JdbcTypeSupport.getDatabaseTypes(dataSource);

			createTableStatement = "create table TF_DSTLCK (id varchar(255) primary key not null, hldrid varchar(255), csig varchar(255), msig varchar(255), lockdate "
					+ dbTypes.getTimestampType() + ")";
		}
		logger.debug(() -> "Create statement: " + createTableStatement);

		if (autoUpdateSchema) {
			try {
				updateSchema();
			} catch (Exception e) {
				throw new RuntimeException("Error while trying to update schema in DB.", e);
			}
		}
	}

	private void initializeMessaging() {
		if (messagingInitialized) {
			return;
		}
		messagingInitialized = true;

		if (messagingSessionProvider != null) {
			try {
				messagingSession = messagingSessionProvider.get();
				topic = messagingSession.createTopic(this.topicName);
				messageProducer = messagingSession.createMessageProducer(topic);
			} catch (MessagingException e) {
				logger.error("error while retrieving messaging components", e);
			}
		}
	}

	protected boolean tableExists(Connection connection) {
		return JdbcTools.tableExists(connection, "TF_DSTLCK") != null;
	}

	public void updateSchema() throws SQLException {
		Connection connection = dataSource.getConnection();

		try {
			if (!tableExists(connection)) {
				logger.debug("Table TF_DSTLCK does not exist.");
				Statement statement = connection.createStatement();
				try {
					logger.debug("Creating table with statement: " + this.createTableStatement);
					statement.executeUpdate(this.createTableStatement);
					logger.debug("Successfully created table TF_DSTLCK.");
				} catch (SQLException e) {
					if (tableExists(connection))
						return;
					else
						throw e;
				} finally {
					statement.close();
				}
			}
		} finally {
			connection.close();
		}
	}

	@Override
	public void preDestroy() {
		if (messageProducer != null) {
			try {
				messageProducer.close();
			} catch (Exception e) {
				logger.warn("error while closing message producer");
			}
		}
		if (messagingSession != null) {
			try {
				messagingSession.close();
			} catch (MessagingException e) {
				logger.warn("error while closing messaging session");
			}
		}
	}

	@Override
	public LockBuilder forIdentifier(String id) {

		AbstractLockBuilder alb = new AbstractLockBuilder(truncateId(id)) {
			@Override
			public Lock exclusive() {
				ensureSignatures();
				return new DistributedLock(this);
			}

			@Override
			public Lock shared() {
				ensureSignatures();
				return new DistributedLock(this);
			}

			private void ensureSignatures() {
				if (machineSignature == null) {
					try {
						machineSignature = NetworkTools.getNetworkAddress().getHostAddress();
					} catch (Exception e) {
						machineSignature = "unkown";
					}
				}
				if (callerSignature == null) {
					StackTraceElement stackTraceElement = new Exception().getStackTrace()[2];
					int lineNumber = stackTraceElement.getLineNumber();
					String className = stackTraceElement.getClassName();
					String methodName = stackTraceElement.getMethodName();
					this.callerSignature = className + '.' + methodName + '(' + (lineNumber >= 0 ? lineNumber : "")
							+ ')';
				}
			}
		};
		alb.lockTtl(lockTtl, TimeUnit.MILLISECONDS);
		return alb;
	}

	protected String truncateId(String id) {
		if (id == null) {
			throw new IllegalArgumentException("The identifier of the lock must not be null.");
		}
		if (id.length() > 240) {
			String md5;
			try {
				md5 = DigestGenerator.stringDigestAsString(id, "MD5");
			} catch (Exception e) {
				logger.error("Could not generate an MD5 sum of ID " + id, e);
				md5 = "";
			}
			String cutId = id.substring(0, 200);
			String newId = cutId.concat("#").concat(md5);
			return newId;
		}
		return id;
	}

	protected void notifyUnlock(String id) {
		initializeMessaging();
		if (messagingSession != null && messageProducer != null) {
			try {
				UnlockedMessage unlockedMessage = UnlockedMessage.T.create();
				unlockedMessage.setLockId(id);

				Message message = messagingSession.createMessage();
				message.setBody(unlockedMessage);
				message.setTimeToLive(topicExpiration);

				messageProducer.sendMessage(message);

			} catch (MessagingException e) {
				logger.error("error while producing message for a lock", e);
			}
		}
	}

	protected MessageConsumer listenForUnlockNotification(final String id, final Object monitor) {
		initializeMessaging();
		if (messagingSession != null && messageProducer != null) {
			try {
				final MessageConsumer messageConsumer = messagingSession.createMessageConsumer(topic);
				messageConsumer.setMessageListener(new MessageListener() {

					@Override
					public void onMessage(Message message) throws MessagingException {

						if (message != null) {
							Object bodyObject = message.getBody();
							if (bodyObject instanceof UnlockedMessage) {
								UnlockedMessage unlockedMessage = (UnlockedMessage) bodyObject;
								String messageLockId = unlockedMessage.getLockId();
								if ((messageLockId != null) && (messageLockId.equals(id))) {
									synchronized (monitor) {
										monitor.notify();
									}
								}
							}

						}
					}
				});

				return messageConsumer;

			} catch (MessagingException e) {
				logger.error("error while adding message listener for a lock queue", e);
				return null;
			}
		}
		return null;
	}

	private class DistributedLock implements Lock {
		protected AbstractLockBuilder lockInfo;

		public DistributedLock(AbstractLockBuilder lockInfo) {
			this.lockInfo = lockInfo;
		}

		@Override
		public void lock() {
			while (true) {
				try {
					if (tryLock(Long.MAX_VALUE, TimeUnit.DAYS))
						return;
				} catch (InterruptedException e) {
					// ignore as there is a special lock method that supports interruptibility
					logger.debug("non interruptible lock() call internally catched an InterruptedException");
				}
			}

		}

		@Override
		public void lockInterruptibly() throws InterruptedException {
			while (true) {
				if (tryLock(Long.MAX_VALUE, TimeUnit.DAYS))
					return;
			}
		}

		@Override
		public boolean tryLock() {
			try {
				return tryLock(0, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				return false;
			}
		}

		@Override
		public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
			long tryMillies = TimeUnit.MILLISECONDS.convert(time, unit);

			String identification = lockInfo.getIdentifier();
			Thread currentThread = Thread.currentThread();
			String oldThreadName = currentThread.getName();
			currentThread.setName(oldThreadName.concat(" > waiting for lock ").concat(identification)
					.concat(" with timeout ").concat("" + lockInfo.getLockTtlMs()));
			try {
				while (true) {
					long waitMillies = Math.min(tryMillies, pollIntervalInMillies);

					Connection connection = null;
					try {
						connection = dataSource.getConnection();
						connection.setAutoCommit(false);
						if (tryInsert(connection)) {
							return true;
						} else {
							if (removeOverdueLock(connection)) {
								if (tryInsert(connection)) {
									return true;
								}
							}
						}
					} finally {
						IOTools.closeCloseable(connection, logger);
					}

					if (tryMillies <= 0)
						return false;

					Object monitor = new Object();

					MessageConsumer consumer = listenForUnlockNotification(lockInfo.getIdentifier(), monitor);

					try {
						long startTime = System.currentTimeMillis();
						synchronized (monitor) {
							monitor.wait(waitMillies);
						}
						long endTime = System.currentTimeMillis();
						long delta = endTime - startTime;

						tryMillies -= delta;
					} finally {
						if (consumer != null) {
							try {
								consumer.close();
							} catch (MessagingException e) {
								logger.error("error while closing message consumer", e);
							}
						}
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("Could not get lock.", e);
			} finally {
				currentThread.setName(oldThreadName);
			}
		}

		protected boolean removeOverdueLock(Connection connection) throws Exception {
			long lockTimeoutMs = lockInfo.getLockTtlMs();
			if (lockTimeoutMs <= 0) {
				return false;
			}
			PreparedStatement statement = null;
			try {
				statement = connection.prepareStatement("delete from TF_DSTLCK where id = ? and lockdate < ?");
				statement.setString(1, lockInfo.getIdentifier());
				statement.setTimestamp(2, new Timestamp(System.currentTimeMillis() - lockTimeoutMs));
				int count = statement.executeUpdate();
				connection.commit();
				boolean success = (count == 1);
				if (logger.isDebugEnabled())
					logger.debug("Deleting the overdue lock " + lockInfo.getIdentifier() + ": " + success);
				return success;
			} catch (Exception e) {
				if (connection != null) {
					try {
						if (!connection.isClosed()) {
							connection.rollback();
						} else {
							logger.error("Could not rollback transaction as connection is already closed.");
						}
					} catch (Throwable ignore) { /* Ignore */
					}
				}
				logger.error("Could not try to delete any overdue lock for identifier: " + lockInfo.getIdentifier(), e);
				return false;
			} finally {
				IOTools.closeCloseable(statement, logger);
			}
		}

		protected boolean tryInsert(Connection connection) {
			PreparedStatement statement = null;
			try {
				statement = connection.prepareStatement(
						"insert into TF_DSTLCK (id, hldrid, csig, msig, lockdate) values (?,?,?,?,?)");
				statement.setString(1, lockInfo.getIdentifier());
				statement.setString(2, lockInfo.getHolderId());
				statement.setString(3, lockInfo.getCallerSignature());
				statement.setString(4, lockInfo.getMachineSignature());
				statement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
				statement.executeUpdate();
				connection.commit();
				return true;
			} catch (Exception e) {
				if (connection != null) {
					try {
						if (!connection.isClosed()) {
							connection.rollback();
						} else {
							logger.error("Could not rollback transaction as connection is already closed.");
						}
					} catch (Throwable ignore) { /* Ignore */
					}
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Lock not obtained due to " + e.getClass().getSimpleName() + ""
							+ (e.getMessage() != null ? ": " + e.getMessage() : ""));
				}
			} finally {
				IOTools.closeCloseable(statement, logger);
			}
			return false;
		}

		@SuppressWarnings("resource")
		@Override
		public void unlock() {
			String identifier = lockInfo.getIdentifier();
			String holderId = lockInfo.getHolderId();

			int errorCount = 0;
			int maxErrors = 3;

			Connection connection = null;
			PreparedStatement st = null;
			try {
				connection = dataSource.getConnection();
				connection.setAutoCommit(false);
				st = connection.prepareStatement("delete from TF_DSTLCK where id=? and hldrid=?");
				st.setString(1, identifier);
				st.setString(2, holderId);

				while (true) {
					try {
						int updated = st.executeUpdate();

						if (updated == 0) {
							logger.error("There is no exclusive lock with the id [" + identifier
									+ "] hold by a holder with id [" + holderId + "]");
							return;
						}

						connection.commit();

						notifyUnlock(identifier);
						return;
					} catch (Exception e) {
						try {
							connection.rollback();
						} catch (Exception ignore) {
							/* Ignore */}
						errorCount++;
						String msg = "Could not release lock with identifier " + identifier + " and holderId "
								+ holderId + " (attempt: " + errorCount + ").";
						if (errorCount == maxErrors) {
							throw new RuntimeException(msg, e);
						} else {
							logger.error(msg, e);
						}
					}

				}
			} catch (Exception e) {
				throw new RuntimeException("Could not unlock.", e);
			} finally {
				IOTools.closeCloseable(st, logger);
				IOTools.closeCloseable(connection, logger);
			}
		}

		@Override
		public Condition newCondition() {
			throw new UnsupportedOperationException("newCondition not supported by this implementation");
		}

	}

	@Configurable
	public void setLockTtl(long lockTtl) {
		this.lockTtl = lockTtl;
	}

	@Override
	public String description() {
		return "DB Lock Manager";
	}

}
