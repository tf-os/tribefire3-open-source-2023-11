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
package tribefire.platform.impl.check;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckRequest;
import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Topic;
import com.braintribe.model.platformreflection.db.DatabaseConnectionInfo;
import com.braintribe.model.platformreflection.db.DatabaseConnectionPoolMetrics;
import com.braintribe.model.platformreflection.db.DatabaseInformation;
import com.braintribe.model.processing.check.api.CheckProcessor;
import com.braintribe.model.processing.platformreflection.db.DatabaseInformationProvider;
import com.braintribe.model.processing.platformreflection.db.StandardDatabaseInformationProvider;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessagingSession;
import com.braintribe.transport.messaging.api.MessagingSessionProvider;
import com.braintribe.utils.RandomTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;

public class BaseConnectivityCheckProcessor implements CheckProcessor {

	private static final Logger logger = Logger.getLogger(BaseConnectivityCheckProcessor.class);

	private final CheckResultEntry lockCheckResultEntry = null;
	private final CheckResultEntry leadershipCheckResultEntry = null;
	private ScheduledExecutorService scheduledExecutorService;
	private DatabaseInformationProvider databaseInformationProvider;

	private Supplier<MessagingSessionProvider> messagingSessionProviderSupplier;
	private String messagingTestTopicName = "system-health-check";
	private long messagingWaitTime = 3000L;

	@Override
	public CheckResult check(ServiceRequestContext requestContext, CheckRequest request) {

		CheckResult response = CheckResult.T.create();

		if (lockCheckResultEntry != null) {
			response.getEntries().add(lockCheckResultEntry);
		}
		if (leadershipCheckResultEntry != null) {
			response.getEntries().add(leadershipCheckResultEntry);
		}

		Map<String, Future<List<CheckResultEntry>>> futures = new HashMap<>();
		futures.put("Connection Pools", scheduledExecutorService.submit(this::checkConnectionPools));
		futures.put("Messaging", scheduledExecutorService.submit(this::checkMessaging));

		for (Map.Entry<String, Future<List<CheckResultEntry>>> futureSet : futures.entrySet()) {
			String type = futureSet.getKey();
			Future<List<CheckResultEntry>> future = futureSet.getValue();

			try {
				List<CheckResultEntry> list = future.get(10, TimeUnit.SECONDS);
				if (list != null) {
					response.getEntries().addAll(list);
				}
			} catch (InterruptedException ie) {
				logger.debug(() -> "Got interrupted while waiting for check results.");
				break;
			} catch (TimeoutException te) {
				logger.warn(() -> "Got a timeout while waiting for check results of " + type);
			} catch (Exception e) {
				logger.warn(() -> "Got an error while getting result for " + type, e);
			}
		}

		return response;

	}

	private List<CheckResultEntry> checkMessaging() {

		List<CheckResultEntry> result = new ArrayList<>();
		if (messagingSessionProviderSupplier == null) {
			logger.debug(() -> "Got no messaging session provider supplier");
			return result;
		}
		MessagingSessionProvider messagingSessionProvider = messagingSessionProviderSupplier.get();
		if (messagingSessionProvider == null) {
			logger.debug(() -> "Got a messaging session provider supplier, but no messaging session provider");
			return result;
		}

		CheckResultEntry entry = CheckResultEntry.T.create();
		entry.setName("Messaging");
		entry.setDetailsAsMarkdown(true);
		result.add(entry);

		Topic testTopic = Topic.T.create();
		testTopic.setName(messagingTestTopicName);

		MessagingSession messagingSession = null;
		MessageConsumer consumer = null;
		MessageProducer createMessageProducer = null;
		ReentrantLock lock = new ReentrantLock();
		Condition newMessageArrived = lock.newCondition();
		try {
			String correlationId = RandomTools.newStandardUuid();
			Message message = Message.T.create();
			message.setBody("Health check with ID " + correlationId);
			message.setCorrelationId(correlationId);
			message.setDestination(testTopic);
			message.setMessageId(correlationId);
			message.setTimeToLive((long) Numbers.MILLISECONDS_PER_MINUTE);

			messagingSession = messagingSessionProvider.provideMessagingSession();

			ConcurrentHashMap<String, Instant> messages = new ConcurrentHashMap<>();
			consumer = messagingSession.createMessageConsumer(testTopic);
			consumer.setMessageListener(m -> {
				String id = m.getCorrelationId();
				messages.put(id, NanoClock.INSTANCE.instant());
				lock.lock();
				try {
					newMessageArrived.signal();
				} finally {
					lock.unlock();
				}
			});

			Instant messageSentInstant = NanoClock.INSTANCE.instant();
			Duration messageTransitDuration = null;

			createMessageProducer = messagingSession.createMessageProducer(testTopic);
			createMessageProducer.sendMessage(message);

			long start = System.currentTimeMillis();
			long end = start + messagingWaitTime;
			long now = start;
			boolean success = false;
			while (now < end) {
				Instant receivedInstant = messages.get(correlationId);
				if (receivedInstant != null) {
					messageTransitDuration = Duration.between(messageSentInstant, receivedInstant);
					if (logger.isDebugEnabled())
						logger.debug("Received own check message after " + StringTools.prettyPrintDuration(messageTransitDuration, true, null));
					success = true;
					break;
				}
				lock.lock();
				try {
					newMessageArrived.await(100, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					logger.debug(() -> "Got interrupted while waiting for the health check message");
					break;
				} finally {
					lock.unlock();
				}
				now = System.currentTimeMillis();
			}

			String desc = messagingSessionProvider.description();

			StringBuilder sb = new StringBuilder();
			sb.append("Name | Message Transit Duration\n");
			sb.append("--- | ---\n");
			sb.append(desc + " | ");
			sb.append(StringTools.prettyPrintDuration(messageTransitDuration, true, null) + "\n");
			entry.setDetails(sb.toString());
			if (success) {
				entry.setCheckStatus(CheckStatus.ok);
			} else {
				entry.setCheckStatus(CheckStatus.fail);
			}
		} catch (Exception e) {
			logger.error("Error while trying to send a message via " + messagingSessionProviderSupplier, e);

			String details = messagingSessionProvider.description();
			entry.setMessage(details + ": " + e.getMessage());
			entry.setCheckStatus(CheckStatus.fail);
		} finally {
			try {
				if (createMessageProducer != null) {
					createMessageProducer.close();
				}
				if (consumer != null) {
					consumer.close();
				}
				if (messagingSession != null) {
					messagingSession.close();
				}
			} catch (Exception e) {
				logger.debug(() -> "Error while closing messaging.", e);
			}
		}

		return result;
	}

	private List<CheckResultEntry> checkConnectionPools() {
		List<CheckResultEntry> result = new ArrayList<>();

		DatabaseInformation databaseInformation = getDatabaseInformationProvider().get();
		if (databaseInformation == null) {
			return result;
		}
		List<DatabaseConnectionInfo> connectionPools = databaseInformation.getConnectionPools();
		if (connectionPools.isEmpty()) {
			return result;
		}

		CheckResultEntry entry = CheckResultEntry.T.create();
		entry.setName("Connection Pools");
		entry.setCheckStatus(CheckStatus.ok);
		entry.setDetailsAsMarkdown(true);
		result.add(entry);

		StringBuilder sb = new StringBuilder();
		sb.append("Name | Idle Connections | Threads Waiting | Active Connections | Total Connections | Max Pool Size\n");
		sb.append("--- | --- | --- | --- | --- | ---\n");

		for (DatabaseConnectionInfo cp : connectionPools) {
			DatabaseConnectionPoolMetrics metrics = cp.getMetrics();

			Integer idleConnections = metrics.getIdleConnections();
			if (idleConnections == null) {
				idleConnections = 0;
			}

			Integer awaiting = metrics.getThreadsAwaitingConnections();
			if (awaiting == null) {
				awaiting = 0;
			}

			Integer active = metrics.getActiveConnections();
			if (active == null) {
				active = 0;
			}

			Integer total = metrics.getTotalConnections();
			if (total == null) {
				total = 0;
			}

			Integer maxPoolSize = cp.getMaximumPoolSize();
			if (maxPoolSize == null) {
				maxPoolSize = 0;
			}

			if (awaiting > 5) {
				if (logger.isDebugEnabled())
					logger.debug("There are " + awaiting + " threads waiting for a connection.");
				// Note: this used to trigger a warning, but it might be legit that there are way more processes
				// than connections in the pool
				entry.setCheckStatus(CheckStatus.ok);
			} else {
				entry.setCheckStatus(CheckStatus.ok);
			}

			sb.append(cp.getName() + "|");
			sb.append(idleConnections + "|");
			sb.append(awaiting + "|");
			sb.append(active + "|");
			sb.append(total + "|");
			sb.append(maxPoolSize + "\n");

		}
		entry.setDetails(sb.toString());
		return result;
	}

	@Required
	public void setScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
		this.scheduledExecutorService = scheduledExecutorService;
	}
	public DatabaseInformationProvider getDatabaseInformationProvider() {
		if (databaseInformationProvider == null) {
			databaseInformationProvider = new StandardDatabaseInformationProvider();
		}
		return databaseInformationProvider;
	}
	@Configurable
	public void setDatabaseInformationProvider(DatabaseInformationProvider databaseInformationProvider) {
		this.databaseInformationProvider = databaseInformationProvider;
	}
	@Configurable
	@Required
	public void setMessagingSessionProviderSupplier(Supplier<MessagingSessionProvider> messagingSessionProvider) {
		this.messagingSessionProviderSupplier = messagingSessionProvider;
	}
	@Configurable
	public void setMessagingTestTopicName(String messagingTestTopicName) {
		this.messagingTestTopicName = messagingTestTopicName;
	}
	@Configurable
	public void setMessagingWaitTime(long messagingWaitTime) {
		this.messagingWaitTime = messagingWaitTime;
	}

}
