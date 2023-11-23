// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.model.processing.lock.db.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.classic.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.LockAcquisitionException;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.lock.ApplicationLock;
import com.braintribe.model.lock.ExclusiveLock;
import com.braintribe.model.lock.ExclusiveLockOnSharedLock;
import com.braintribe.model.lock.LockHolder;
import com.braintribe.model.lock.SharedLock;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Queue;
import com.braintribe.model.processing.lock.api.LockBuilder;
import com.braintribe.model.processing.lock.impl.AbstractLockBuilder;
import com.braintribe.model.processing.lock.impl.AbstractLockManager;

import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageListener;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;

public class DbLockManager extends AbstractLockManager {
	private static final String QUEUE_UNLOCK_PREFIX = "unlock-";
	private static final Logger logger = Logger.getLogger(DbLockManager.class);
	private SessionFactory sessionFactory;
	private Supplier<MessagingSession> messagingSessionProvider;
	private int pollIntervalInMillies = 1000;
	private MessagingSession messagingSession = null;

	@Required
	@Configurable
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Configurable
	public void setMessagingSessionProvider(Supplier<MessagingSession> messagingSessionProvider) {
		this.messagingSessionProvider = messagingSessionProvider;
	}

	@Configurable
	public void setPollIntervalInMillies(int pollIntervalInMillies) {
		this.pollIntervalInMillies = pollIntervalInMillies;
	}

	@PostConstruct
	public void initialize() {
		if (messagingSessionProvider != null) {
			try {
				messagingSession = messagingSessionProvider.get();
			} catch (RuntimeException e) {
				logger.error("error while retrieving messaging session");
			}
		}
	}

	@PreDestroy
	public void destroy() {
		if (messagingSession != null) {
			try {
				messagingSession.close();
			} catch (MessagingException e) {
				logger.error("error while closing messaging session");
			}
		}
	}


	@Override
	public LockBuilder forIdentifier(String id) {



		return new AbstractLockBuilder(id) {
			@Override
			public Lock exclusive() {
				ensureSignatures();
				return new DistributedLock(this);
			}

			@Override
			public Lock shared() {
				ensureSignatures();
				return new DbSharedLock(this);
			}

			private void ensureSignatures() {
				if (machineSignature == null) {
					try {
						machineSignature = InetAddress.getLocalHost().getHostAddress();
					} catch (UnknownHostException e) {
						machineSignature = "unkown";
					}
				}
				if (callerSignature == null) {
					StackTraceElement stackTraceElement = new Exception().getStackTrace()[2];
					int lineNumber = stackTraceElement.getLineNumber();
					String className = stackTraceElement.getClassName();
					String methodName = stackTraceElement.getMethodName();
					this.callerSignature = className + '.' + methodName + '(' + (lineNumber >= 0? lineNumber: "") + ')';
				}
			}
			public String getMachineSignature() {
				return machineSignature;
			}

			public String getCallerSignature() {
				return callerSignature;
			}
		}; 
	}

	protected void notifyUnlock(String id) {
		if (messagingSession != null) {
			try {
				Queue queue = messagingSession.createQueue(QUEUE_UNLOCK_PREFIX + id);

				Message message = GMF.createEntity(Message.class);

				MessageProducer messageProducer = messagingSession.createMessageProducer(queue);

				messageProducer.sendMessage(message);

			} catch (MessagingException e) {
				logger.error("error while producing message for a lock");
			}
		}
	}

	protected MessageConsumer listenForUnlockNotification(String id, final Object monitor) {
		if (messagingSession != null) {
			try {
				Queue queue = messagingSession.createQueue(QUEUE_UNLOCK_PREFIX + id);

				final MessageConsumer messageConsumer = messagingSession.createMessageConsumer(queue);
				messageConsumer.setMessageListener(new MessageListener() {

					@Override
					public void onMessage(Message message) throws MessagingException {
						synchronized (monitor) {
							monitor.notify();
						}
					}
				});

				return messageConsumer;

			} catch (MessagingException e) {
				logger.error("error while adding message listener for a lock queue");
				return null;
			}
		}
		return null;
	}


	private abstract class AbstractDbExclusiveLock implements Lock {
		protected AbstractLockBuilder lockInfo;

		public AbstractDbExclusiveLock(AbstractLockBuilder lockInfo) {
			this.lockInfo = lockInfo;
		}

		protected abstract com.braintribe.model.lock.LockWithHolder createLockInstance();
		protected abstract Class<? extends com.braintribe.model.lock.LockWithHolder> getLockClass();

		@Override
		public void lock() {
			while (true) {
				try {
					if (tryLock(Long.MAX_VALUE,TimeUnit.DAYS))
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
				if (tryLock(Long.MAX_VALUE,TimeUnit.DAYS))
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
			Session session = sessionFactory.openSession();


			try {
				while (true) {
					Date date = new Date();
					com.braintribe.model.lock.LockWithHolder lock = createLockInstance();
					lock.setId(lockInfo.getIdentifier());
					lock.setCreationDate(date);

					long waitMillies = Math.min(tryMillies, pollIntervalInMillies);

					try {
						session.beginTransaction();
						session.save(lock);
						session.getTransaction().commit();
						session.beginTransaction();

						LockHolder holder = GMF.createEntity(LockHolder.class);
						holder.setId(lockInfo.getHolderId());
						holder.setCodeSignature(lockInfo.getCallerSignature());
						holder.setCreationDate(date);
						holder.setMachineSignature(lockInfo.getMachineSignature());

						session.save(holder);

						lock.setHolder(holder);
						session.getTransaction().commit();

						return true;

					} catch (LockAcquisitionException lae) {
						session.getTransaction().rollback();
					} catch (ConstraintViolationException e) {
						session.getTransaction().rollback();
					}

					if (tryMillies <= 0)
						return false;

					for (int i=0; i<3; ++i) {
						try {
							session.beginTransaction();
							session.createQuery("update SharedLock set blocked=true where id=?").setParameter(0, lockInfo.getIdentifier()).executeUpdate();
							session.getTransaction().commit();
							break;
						} catch(Exception e) {
							//ignore
						}
					}

					Object monitor = new Object();

					MessageConsumer consumer = listenForUnlockNotification(lockInfo.getIdentifier(), monitor);

					try {
						long startTime = System.currentTimeMillis();
						synchronized(monitor) {
							monitor.wait(waitMillies);
						}
						long endTime = System.currentTimeMillis() - startTime;
						long delta = endTime - startTime;

						tryMillies -= delta;
					}
					finally {
						if (consumer != null) {
							try {
								consumer.close();
							} catch (MessagingException e) {
								logger.error("error while closing message consumer", e);
							}
						}
					}

					session.evict(lock);
				}
			}
			finally {
				session.close();
			}
		}

		@Override
		public void unlock() {
			Session session = sessionFactory.openSession();
			String identifier = lockInfo.getIdentifier();
			String holderId = lockInfo.getHolderId();
			try {
				while (true) {
					try {
						session.beginTransaction();

						int updated = session.createQuery("delete from ExclusiveLock where id=? and holder.id=?")
								.setParameter(0, identifier)
								.setParameter(1, holderId)
								.executeUpdate();

						if (updated == 0) {
							logger.error("There is no exclusive lock with the id [" + identifier + "] hold by a holder with id [" + holderId + "]");
							return;
						}

						session.createQuery("delete from LockHolder where id=?")
						.setParameter(0, holderId)
						.executeUpdate();

						session.getTransaction().commit();
						notifyUnlock(identifier);

						return;
					} catch(LockAcquisitionException lae) {
						//logger.debug("Lock error", lae);
					} catch (HibernateException e) {
						logger.error("error while unlocking " + identifier, e);
						throw e;
					}
				}
			}
			finally {
				session.close();
			}
		}

		@Override
		public Condition newCondition() {
			throw new UnsupportedOperationException();
		}

	}

	private class DbExclusiveLock extends AbstractDbExclusiveLock {
		public DbExclusiveLock(AbstractLockBuilder lockInfo) {
			super(lockInfo);
		}

		protected java.lang.Class<? extends com.braintribe.model.lock.LockWithHolder> getLockClass() {
			return ExclusiveLock.class;
		}

		@Override
		protected com.braintribe.model.lock.LockWithHolder createLockInstance() {
			return GMF.createEntity(ExclusiveLock.class);
		}
	}

	private class DbExclusiveLockOnSharedLock extends AbstractDbExclusiveLock {
		public DbExclusiveLockOnSharedLock(AbstractLockBuilder lockInfo) {
			super(lockInfo);
		}

		protected java.lang.Class<? extends com.braintribe.model.lock.LockWithHolder> getLockClass() {
			return ExclusiveLockOnSharedLock.class;
		}

		@Override
		protected com.braintribe.model.lock.LockWithHolder createLockInstance() {
			return GMF.createEntity(ExclusiveLockOnSharedLock.class);
		}
	}

	private class DbSharedLock implements Lock {

		private AbstractLockBuilder lockInfo;

		public DbSharedLock(AbstractLockBuilder lockInfo) {
			this.lockInfo = lockInfo;
		}

		@Override
		public void lock() {
		}

		@Override
		public void lockInterruptibly() throws InterruptedException {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean tryLock() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
			long tryMillies = TimeUnit.MILLISECONDS.convert(time, unit);
			Session session = sessionFactory.openSession();

			try {
				Date date = new Date();
				SharedLock lock = GMF.createEntity(SharedLock.class);
				lock.setHolders(new HashSet<LockHolder>());
				lock.setCreationDate(date);
				lock.setId(lockInfo.getIdentifier());

				LockHolder holder = GMF.createEntity(LockHolder.class);
				holder.setId(lockInfo.getHolderId());
				holder.setCodeSignature(lockInfo.getCallerSignature());
				holder.setCreationDate(date);
				holder.setMachineSignature(lockInfo.getMachineSignature());

				lock.getHolders().add(holder);


				while (true) {
					lock.setCreationDate(new Date());

					long waitMillies = Math.min(tryMillies, pollIntervalInMillies);

					try {
						session.beginTransaction();
						session.save(lock);
						session.getTransaction().commit();
						return true;
					}
					catch (ConstraintViolationException e) {
						session.getTransaction().rollback();
					}

					Lock superLock = new DbExclusiveLockOnSharedLock(lockInfo);
					superLock.lock(); // TODO: tryLog with timeout because the top level method is with timeout

					try {
						ApplicationLock existingLock = (ApplicationLock)session.get(ApplicationLock.class, lockInfo.getIdentifier());

						if (existingLock != null) {
							if (existingLock instanceof SharedLock) {
								SharedLock sharedLock = (SharedLock)existingLock;

								if (!sharedLock.getBlocked()) {
									sharedLock.getHolders().add(holder);
									session.getTransaction().commit();
									return true;
								}
							}

							superLock.unlock();
							superLock = null;

							if (tryMillies <= 0)
								return false;

							if (existingLock instanceof ExclusiveLock) {
								Object monitor = new Object();

								MessageConsumer consumer = listenForUnlockNotification(lockInfo.getIdentifier(), monitor);

								try {

									long startTime = System.currentTimeMillis();
									monitor.wait(waitMillies);
									long endTime = System.currentTimeMillis() - startTime;
									long delta = endTime - startTime;

									tryMillies -= delta;
								}
								finally {
									if (consumer != null) {
										try {
											consumer.close();
										} catch (MessagingException e) {
											logger.error("error while closing messaging consumer for id " + lockInfo.getIdentifier(), e);
										}
									}
								}
							}
						}
					}
					finally {
						if (superLock != null)
							superLock.unlock();
					}
				}
			}
			finally {
				session.close();
			}
		}

		@Override
		public void unlock() {
			Lock superLock = new DbExclusiveLockOnSharedLock(lockInfo);
			superLock.lock();

			try {
				Session session = sessionFactory.openSession();
				String identifier = lockInfo.getIdentifier();
				String holderId = lockInfo.getHolderId();
				try {
					session.beginTransaction();

					Object[] row = (Object[])session.createQuery("select l, h from SharedLock as l join l.holders as h where l.id=? and h.id=?")
							.setParameter(0, identifier)
							.setParameter(1, holderId)
							.uniqueResult();

					SharedLock lock = (SharedLock)row[0];
					SharedLock holder = (SharedLock)row[1];

					if (lock == null)
						throw new IllegalStateException("there is no shared lock with the id [" + identifier + "] hold by a holder with id [" + holderId + "]");

					lock.getHolders().remove(holder);
					session.delete(holder);

					session.getTransaction().commit();
					session.getTransaction().begin();

					int count = (Integer)session.createQuery("select count(h) from SharedLock as l join l.holders as h").uniqueResult();

					// check reference counter
					if (count == 0) {
						session.delete(lock);
						session.getTransaction().commit();
					}

					notifyUnlock(identifier);
				} catch (HibernateException e) {
					logger.error("error while unlocking " + identifier, e);
					throw e;
				}
				finally {
					session.close();
				}
			}
			finally {
				superLock.unlock();
			}
		}

		@Override
		public Condition newCondition() {
			throw new UnsupportedOperationException("newCondition not supported by this implementation");
		}

	}

	class DistributedLock implements Lock {
		protected AbstractLockBuilder lockInfo;

		public DistributedLock(AbstractLockBuilder lockInfo) {
			this.lockInfo = lockInfo;
		}
		@Override
		public void lock() {
			while (true) {
				try {
					if (tryLock(Long.MAX_VALUE,TimeUnit.DAYS))
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
				if (tryLock(Long.MAX_VALUE,TimeUnit.DAYS))
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
			StatelessSession session = sessionFactory.openStatelessSession();


			try {
				while (true) {
					long waitMillies = Math.min(tryMillies, pollIntervalInMillies);

					Connection connection = session.connection();
					PreparedStatement statement = null;
					try {
						connection.setAutoCommit(false);
						statement = connection.prepareStatement("insert into TF_DSTLCK (id, hldrid, csig, msig, date) values (?,?,?,?,?)");
						statement.setString(1, lockInfo.getIdentifier());
						statement.setString(2, lockInfo.getHolderId());
						statement.setString(3, lockInfo.getCallerSignature());
						statement.setString(4, lockInfo.getMachineSignature());
						statement.setDate(5, new java.sql.Date(System.currentTimeMillis()));
						statement.executeUpdate();
						connection.commit();
						return true;
					} catch (Exception e) {
						if (connection != null) {
							try {
								connection.rollback();
							} catch(Exception ignore) {
							}
						}
					} finally {
						if (statement != null) {
							try {
								statement.close();
							} catch(Exception ignore) {
							}
						}
					}

					if (tryMillies <= 0)
						return false;

					Object monitor = new Object();

					MessageConsumer consumer = listenForUnlockNotification(lockInfo.getIdentifier(), monitor);

					try {
						long startTime = System.currentTimeMillis();
						synchronized(monitor) {
							monitor.wait(waitMillies);
						}
						long endTime = System.currentTimeMillis() - startTime;
						long delta = endTime - startTime;

						tryMillies -= delta;
					}
					finally {
						if (consumer != null) {
							try {
								consumer.close();
							} catch (MessagingException e) {
								logger.error("error while closing message consumer", e);
							}
						}
					}
				}
			}
			finally {
				try {
					session.close();
				} catch(Exception e) {
					logger.error("Could not close session after lock.", e);
				}
			}
		}


		@Override
		public void unlock() {
			StatelessSession session = sessionFactory.openStatelessSession();
			String identifier = lockInfo.getIdentifier();
			String holderId = lockInfo.getHolderId();

			int errorCount = 0;
			int maxErrors = 3;
			try {
				while (true) {
					try {
						session.beginTransaction();

						SQLQuery sqlQuery = session.createSQLQuery("delete from TF_DSTLCK where id=? and hldrid=?");
						sqlQuery.setString(0, identifier);
						sqlQuery.setString(1, holderId);
						int updated = sqlQuery.executeUpdate();

						if (updated == 0) {
							logger.error("There is no exclusive lock with the id [" + identifier + "] hold by a holder with id [" + holderId + "]");
							return;
						}

						session.getTransaction().commit();

						notifyUnlock(identifier);
						return;
					} catch(LockAcquisitionException lae) {
						//logger.debug("Lock error", lae);
						try {
							session.getTransaction().rollback();
						} catch(Exception ignore) {}
					} catch (HibernateException e) {
						try {
							session.getTransaction().rollback();
						} catch(Exception ignore) {}
						errorCount++;
						String msg = "Could not release lock with identifier "+identifier+" and holderId "+holderId+" (attempt: "+errorCount+").";
						if (errorCount == maxErrors) {
							throw new RuntimeException(msg, e);
						} else {
							logger.error(msg, e);
						}
					}

				}
			} finally {
				try {
					session.close();
				} catch(Exception e) {
					logger.error("Could not close session after unlock.", e);
				}
			}
		}

		@Override
		public Condition newCondition() {
			throw new UnsupportedOperationException("newCondition not supported by this implementation");
		}

	}
}
