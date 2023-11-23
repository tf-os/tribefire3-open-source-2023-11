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
package com.braintribe.model.processing.lock.etcd;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

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
import com.braintribe.util.network.NetworkTools;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.op.Cmp;
import io.etcd.jetcd.op.CmpTarget;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.GetOption.SortOrder;
import io.etcd.jetcd.options.GetOption.SortTarget;
import io.etcd.jetcd.options.PutOption;

public class EtcdLockManager implements LockManager, LifecycleAware {

	private static final Logger logger = Logger.getLogger(EtcdLockManager.class);

	protected static String lockPrefix = "lock/";

	protected Supplier<Client> clientSupplier;

	private long lockTimeout = -1L;

	protected String identifierPrefix = "";

	private Supplier<MessagingSession> messagingSessionProvider;
	private int pollIntervalInMillies = 1000;
	private MessagingSession messagingSession = null;
	private String topicName = "tf-unlock";
	private long topicExpiration = 5000L;
	private Topic topic;
	private MessageProducer messageProducer;

	protected Client client;
	protected KV kvClient;
	private io.etcd.jetcd.Lock lockClient;

	@Override
	public void postConstruct() {
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

	@Override
	public void preDestroy() {
		if (messagingSession != null) {
			try {
				messagingSession.close();
			} catch (MessagingException e) {
				logger.error("error while closing messaging session");
			}
		}
		disconnect();
	}

	protected void connect() {
		if (client == null) {
			synchronized (this) {
				if (client == null) {
					client = clientSupplier.get();

					kvClient = client.getKVClient();
					lockClient = client.getLockClient();
				}
			}
		}
	}

	protected void disconnect() {
		IOTools.closeCloseable(lockClient, logger);
		IOTools.closeCloseable(kvClient, logger);
		IOTools.closeCloseable(client, logger);

		kvClient = null;
		client = null;
	}

	@Override
	public LockBuilder forIdentifier(String id) {
		AbstractLockBuilder alb = new AbstractLockBuilder(id) {
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
					this.callerSignature = className + '.' + methodName + '(' + (lineNumber >= 0 ? lineNumber : "") + ')';
				}
			}
		};
		alb.lockTtl(lockTimeout, TimeUnit.MILLISECONDS);
		return alb;
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
			connect();

			long tryMillies = TimeUnit.MILLISECONDS.convert(time, unit);

			try {

				while (true) {
					long waitMillies = Math.min(tryMillies, pollIntervalInMillies);

					if (logger.isTraceEnabled())
						logger.trace(getLockIdentifierWithPrefix(lockInfo.getIdentifier()) + ": tryInsert. tryMillies=" + tryMillies);

					if (tryInsert()) {
						if (logger.isTraceEnabled())
							logger.trace(getLockIdentifierWithPrefix(lockInfo.getIdentifier()) + ": tryInsert was successful.");
						return true;
					} else {
						if (removeOverdueLock()) {
							if (logger.isTraceEnabled())
								logger.trace(getLockIdentifierWithPrefix(lockInfo.getIdentifier()) + ": removed overdue lock.");
							if (tryInsert()) {
								if (logger.isTraceEnabled())
									logger.trace(getLockIdentifierWithPrefix(lockInfo.getIdentifier())
											+ ": tryInsert after overdue removal was successful.");
								return true;
							}
						}
					}

					if (tryMillies <= 0)
						return false;

					Object monitor = new Object();

					MessageConsumer consumer = listenForUnlockNotification(getLockIdentifierWithPrefix(lockInfo.getIdentifier()), monitor);

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
			}
		}

		protected boolean removeOverdueLock() throws Exception {
			long lockTimeoutMs = lockInfo.getLockTtlMs();
			if (lockTimeoutMs <= 0) {
				return false;
			}

			try {

				boolean success = false;

				String identifier = getLockIdentifierWithPrefix(lockInfo.getIdentifier());

				String responseValue = getResponseValue(get(identifier));
				if (responseValue != null) {
					Map<String, String> map = StringTools.decodeStringMapFromString(responseValue);
					String lockdateString = map.get("lockdate");
					Date lockDate = DateTools.decodeDateTime(lockdateString, DateTools.ISO8601_DATE_WITH_MS_FORMAT);
					long now = System.currentTimeMillis();

					long lockTimestamp = lockDate.getTime();
					if ((lockTimestamp + lockTimeoutMs) < now) {
						if (logger.isTraceEnabled())
							logger.trace(identifier + ": outdated lock, removing");
						remove(identifier);
					}
				}

				return success;
			} catch (Exception e) {
				logger.error("Could not try to delete any overdue lock for identifier: " + lockInfo.getIdentifier(), e);
				return false;
			}
		}

		protected String getLockValue() {
			Map<String, String> map = new HashMap<>();
			map.put("hldrid", lockInfo.getHolderId());
			map.put("csig", lockInfo.getCallerSignature());
			map.put("msig", lockInfo.getMachineSignature());
			map.put("lockdate", DateTools.encode(new Date(), DateTools.ISO8601_DATE_WITH_MS_FORMAT));
			String encodedMap = StringTools.encodeStringMapToString(map);
			return encodedMap;
		}

		protected boolean tryInsert() {

			String lockKey = getLockIdentifierWithPrefix(lockInfo.getIdentifier());
			String lockValue = getLockValue();

			ByteSequence bsLockKey = ByteSequence.from(lockKey, StandardCharsets.UTF_8);
			ByteSequence bsValue = ByteSequence.from(lockValue, StandardCharsets.UTF_8);

			try {
				int ttl = -1;
				PutOption putOption = null;

				if (lockInfo.getLockTtlMs() > 0) {
					ttl = (int) (lockInfo.getLockTtlMs() / 1000);
					if (ttl == 0) {
						ttl = 1;
					}
					LeaseGrantResponse leaseGrantResponse = client.getLeaseClient().grant(ttl).get();
					long leaseId = leaseGrantResponse.getID();
					putOption = PutOption.newBuilder().withLeaseId(leaseId).build();
				} else {
					putOption = PutOption.newBuilder().build();
				}

				GetOption getOption = GetOption.newBuilder().withSortField(SortTarget.MOD).withSortOrder(SortOrder.ASCEND).build();

				Cmp condition = new Cmp(bsLockKey, Cmp.Op.EQUAL, CmpTarget.version(0));
				Op update = Op.put(bsLockKey, bsValue, putOption);
				Op get = Op.get(bsLockKey, getOption);

				TxnResponse txnResponse = kvClient.txn().If(condition).Then(update).Then(get).commit().get();

				List<GetResponse> responses = txnResponse.getGetResponses();
				if (responses == null || responses.isEmpty()) {
					return false;
				}

				if (logger.isTraceEnabled())
					logger.trace(getLockIdentifierWithPrefix(lockInfo.getIdentifier()) + ": Created lock with ttl: " + ttl);

				if (responses.size() > 1) {
					logger.trace(() -> "Unexpected: Got " + responses.size() + " responses for locking.");
				}

				List<KeyValue> kvs = responses.get(0).getKvs();
				ByteSequence newValue = kvs.get(0).getValue();
				String newValueString = newValue.toString(StandardCharsets.UTF_8);

				boolean transactionResultMatches = lockValue.equals(newValueString);

				if (transactionResultMatches) {

					// It has been seen during testing that this transaction is not as safe as it should be
					// Waiting a moment and then checking again if the value is still there
					Thread.sleep(10L);

					GetResponse checkResponse = kvClient.get(bsLockKey, getOption).get();
					List<KeyValue> checkKvs = checkResponse.getKvs();
					ByteSequence checkValueByteSequence = checkKvs.get(0).getValue();
					String checkValue = checkValueByteSequence.toString(StandardCharsets.UTF_8);
					if (!checkValue.equals(newValueString)) {
						logger.trace(() -> "Check value is different: should: " + newValueString + ", actual: " + checkValue);
						return false;
					}
				}

				return transactionResultMatches;

			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug(
							"Lock not obtained due to " + e.getClass().getSimpleName() + "" + (e.getMessage() != null ? ": " + e.getMessage() : ""));
				}
			}

			return false;
		}

		protected GetResponse get(String key) throws Exception {
			ByteSequence bsKey = ByteSequence.from(key, StandardCharsets.UTF_8);
			kvClient.get(bsKey).get();
			CompletableFuture<GetResponse> getFuture = kvClient.get(bsKey);
			// get the value from CompletableFuture
			GetResponse response = getFuture.get();
			return response;

		}
		protected String getResponseValue(GetResponse response) {
			if (response.getKvs().isEmpty()) {
				// key does not exist
				return null;
			}
			String result = response.getKvs().get(0).getValue().toString(StandardCharsets.UTF_8);
			return result;
		}

		private void remove(String key) throws Exception {
			ByteSequence bsKey = ByteSequence.from(key, StandardCharsets.UTF_8);
			kvClient.delete(bsKey).get();
		}

		@Override
		public void unlock() {
			connect();

			String identifier = getLockIdentifierWithPrefix(lockInfo.getIdentifier());
			String holderId = lockInfo.getHolderId();

			int errorCount = 0;
			int maxErrors = 3;

			try {
				while (true) {
					try {

						boolean unlocked = false;

						String responseValue = getResponseValue(get(identifier));
						if (responseValue != null) {
							Map<String, String> map = StringTools.decodeStringMapFromString(responseValue);
							String currentHolder = map.get("hldrid");
							if (currentHolder.equals(holderId)) {
								remove(identifier);
								unlocked = true;
							}
						}

						if (!unlocked) {
							logger.error("There is no exclusive lock with the id [" + identifier + "] hold by a holder with id [" + holderId + "]");
							return;
						}

						notifyUnlock(identifier);
						return;
					} catch (Exception e) {
						errorCount++;
						String msg = "Could not release lock with identifier " + identifier + " and holderId " + holderId + " (attempt: " + errorCount
								+ ").";
						if (errorCount == maxErrors) {
							throw new RuntimeException(msg, e);
						} else {
							logger.error(msg, e);
						}
					}

				}
			} catch (Exception e) {
				throw new RuntimeException("Could not unlock.", e);
			}
		}

		@Override
		public Condition newCondition() {
			throw new UnsupportedOperationException("newCondition not supported by this implementation");
		}

	}

	protected void notifyUnlock(String id) {
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

	protected String getLockIdentifierWithPrefix(String lockIdentifier) {
		return lockPrefix + this.identifierPrefix + lockIdentifier;
	}

	@Configurable
	public void setIdentifierPrefix(String identifierPrefix) {
		this.identifierPrefix = identifierPrefix;
	}

	@Configurable
	public void setLockTimeout(long lockTimeout) {
		this.lockTimeout = lockTimeout;
	}

	@Configurable
	public void setMessagingSessionProvider(Supplier<MessagingSession> messagingSessionProvider) {
		this.messagingSessionProvider = messagingSessionProvider;
	}

	@Configurable
	@Required
	public void setClientSupplier(Supplier<Client> clientSupplier) {
		this.clientSupplier = clientSupplier;
	}

	@Override
	public String description() {
		return "etcd Lock Manager (" + clientSupplier + ")";
	}
}
