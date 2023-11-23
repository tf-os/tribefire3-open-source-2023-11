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

import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.exception.Exceptions;
import com.braintribe.execution.monitoring.ThreadPoolMonitoring;
import com.braintribe.execution.monitoring.ThreadPoolStatistics;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckRequest;
import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.platformreflection.request.GetLicenseInformation;
import com.braintribe.model.platformreflection.tf.License;
import com.braintribe.model.processing.check.api.CheckProcessor;
import com.braintribe.model.processing.lock.api.Locking;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.CollectionTools2;

import tribefire.cortex.leadership.api.LeadershipContext;
import tribefire.cortex.leadership.api.LeadershipListener;
import tribefire.cortex.leadership.api.LeadershipManager;

public class BaseFunctionalityCheckProcessor implements CheckProcessor, LifecycleAware {

	private static final Logger logger = Logger.getLogger(BaseFunctionalityCheckProcessor.class);

	private Locking locking;
	private LeadershipManager leadershipManager;

	private CheckResultEntry lockCheckResultEntry = null;
	private CheckResultEntry leadershipCheckResultEntry = null;
	private ScheduledExecutorService scheduledExecutorService;
	private Future<?> checkFuture;

	protected Evaluator<ServiceRequest> requestEvaluator;

	@Override
	public CheckResult check(ServiceRequestContext requestContext, CheckRequest request) {

		CheckResult response = CheckResult.T.create();

		if (lockCheckResultEntry != null) {
			response.getEntries().add(lockCheckResultEntry);
		}
		if (leadershipCheckResultEntry != null) {
			response.getEntries().add(leadershipCheckResultEntry);
		}
		checkFuture = scheduledExecutorService.submit(this::performConcurrencyChecks);

		Map<String, Future<List<CheckResultEntry>>> futures = new HashMap<>();
		futures.put("Thread Pool", scheduledExecutorService.submit(this::checkThreadPools));

		response.getEntries().addAll(checkLicense());

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

	private List<CheckResultEntry> checkThreadPools() {

		List<ThreadPoolStatistics> statistics = ThreadPoolMonitoring.getStatistics();

		if (statistics != null) {

			CheckResultEntry result = CheckResultEntry.T.create();
			result.setCheckStatus(CheckStatus.ok);
			result.setName("Thread Pools");

			TreeMap<String, String> sortedEntries = new TreeMap<>();

			for (ThreadPoolStatistics stats : statistics) {

				String name = stats.getDescription();
				if (name == null) {
					name = "n/a";
				}

				int pending = stats.getPendingTasksInQueue();

				StringBuilder sb = new StringBuilder();

				sb.append(name + "|");
				sb.append(stats.currentlyRunning() + "|");
				sb.append(stats.totalExecutions() + "|");
				sb.append(stats.averageRunningTimeInMs() + " ms|");
				sb.append(stats.getPoolSize() + "|");
				sb.append(stats.getCorePoolSize() + "|");
				int maximumPoolSize = stats.getMaximumPoolSize();
				if (maximumPoolSize == Integer.MAX_VALUE) {
					sb.append("unbound|");
				} else {
					sb.append(maximumPoolSize + "|");
				}
				sb.append(pending + "|");
				Double averageEnqueuedTimeInMs = stats.getAverageEnqueuedTimeInMs();
				if (averageEnqueuedTimeInMs != null) {
					sb.append(StringTools.prettyPrintMilliseconds(averageEnqueuedTimeInMs, true, ChronoUnit.NANOS) + "|");
				} else {
					sb.append(" |");
				}
				long timeSinceLastExecution = stats.timeSinceLastExecutionInMs();
				String timeSinceLastExecutionString = StringTools.prettyPrintMilliseconds(timeSinceLastExecution, true, ChronoUnit.MILLIS);
				int index = timeSinceLastExecutionString.indexOf(" ");
				if (index != -1) {
					index = timeSinceLastExecutionString.indexOf(" ", index + 1);
					if (index != -1) {
						timeSinceLastExecutionString = timeSinceLastExecutionString.substring(0, index);
					}
				}
				sb.append(timeSinceLastExecutionString);
				sb.append("\n");

				sortedEntries.put(name, sb.toString());
			}

			StringBuilder sb = new StringBuilder();
			sb.append(
					"Name|Active Threads|Total Executions|Average Execution Time|Pool Size|Core Pool Size|Maximum Pool Size|Pending Tasks in Queue|Average Pending Time in Queue|Time Since Last Execution\n");
			sb.append("---|---|---|---|---|---|---|---|---|---\n");

			sortedEntries.values().forEach(l -> sb.append(l));
			result.setDetails(sb.toString());
			result.setDetailsAsMarkdown(true);
			return CollectionTools2.asList(result);

		}

		return Collections.EMPTY_LIST;
	}

	private List<CheckResultEntry> checkLicense() {
		List<CheckResultEntry> result = new ArrayList<>();

		try {
			GetLicenseInformation gli = GetLicenseInformation.T.create();
			License license = gli.eval(requestEvaluator).get();

			CheckResultEntry entry = CheckResultEntry.T.create();
			result.add(entry);
			entry.setName("License");

			if (license != null) {
				boolean active = license.getActive();
				if (active) {

					entry.setDetailsAsMarkdown(true);
					entry.setCheckStatus(CheckStatus.ok);

					StringBuilder sb = new StringBuilder();
					sb.append("---|---\n");
					sb.append("Issued to|" + license.getLicensee() + "\n");
					sb.append("Issued by|" + license.getLicensor() + "\n");

					Date expiryDate = license.getExpiryDate();
					if (expiryDate != null) {

						long exp = expiryDate.getTime();
						long now = System.currentTimeMillis();
						long untilExpInMs = exp - now;
						long thresholdInMs = Numbers.MILLISECONDS_PER_DAY * 30l;
						SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
						String expiryString = sdf.format(expiryDate);
						if (untilExpInMs < thresholdInMs) {
							if (exp < now) {
								sb.append("Expired|" + expiryString + "\n");
								entry.setMessage("License has expired " + expiryString);
								entry.setCheckStatus(CheckStatus.fail);
							} else {
								entry.setMessage("License will expire " + expiryString);
								sb.append("Expiration|" + expiryString + "\n");
								entry.setCheckStatus(CheckStatus.warn);
							}
						} else {
							sb.append("Expiration|" + expiryString + "\n");
						}

					}
					entry.setDetails(sb.toString());

				} else {
					entry.setMessage("Could not find an active license.");
					entry.setCheckStatus(CheckStatus.fail);
				}
			} else {
				entry.setMessage("Could not find a license.");
				entry.setCheckStatus(CheckStatus.fail);
			}
		} catch (Exception e) {
			logger.error("Error while trying to get license information", e);
		}

		return result;
	}

	private void performConcurrencyChecks() {
		checkLockManager();
		checkLeadershipManager();
	}

	protected void checkLockManager() {
		if (locking == null) {
			return;
		}

		CheckResultEntry entry = CheckResultEntry.T.create();
		entry.setName("Lock Check");
		entry.setDetailsAsMarkdown(true);
		StringBuilder sb = new StringBuilder();
		sb.append("---|---\n");
		sb.append("Locking|" + locking.getClass().getSimpleName() + "\n");
		entry.setDetails(sb.toString());

		try {
			String identifier = BaseFunctionalityCheckProcessor.class.getName();

			Lock lock = locking.forIdentifier(identifier).writeLock();
			lock.lock();
			try {
				logger.debug(() -> "Lock acquired. Locking: " + locking);
			} finally {
				lock.unlock();
			}

			entry.setCheckStatus(CheckStatus.ok);

		} catch (Exception e) {
			logger.error("Error while trying to check the lock manager.", e);
			entry.setCheckStatus(CheckStatus.fail);
			entry.setMessage(Exceptions.getRootCause(e).getMessage());
		}

		lockCheckResultEntry = entry;
	}

	protected void checkLeadershipManager() {

		if (leadershipManager == null) {
			return;
		}

		CheckResultEntry entry = CheckResultEntry.T.create();
		entry.setName("Leadership Check");
		entry.setDetailsAsMarkdown(true);
		StringBuilder sb = new StringBuilder();
		sb.append("---|---\n");
		sb.append("Leadership Manager|" + leadershipManager.description() + "\n");
		entry.setDetails(sb.toString());

		String domainId = "base-health-check";
		
		LeadershipListener4Check leadershipListener = new LeadershipListener4Check();
		try {

			leadershipManager.addLeadershipListener(domainId, leadershipListener);
			boolean success = leadershipListener.latch.await(1, TimeUnit.MINUTES);

			if (success) {
				entry.setCheckStatus(CheckStatus.ok);
			} else {
				entry.setCheckStatus(CheckStatus.fail);
				entry.setMessage(
						"Leadership Manager (" + leadershipManager.description() + ") did not assign leadership within the required timeframe");
			}

		} catch (Exception e) {
			logger.error("Error while trying to check the leadership manager.", e);
			entry.setCheckStatus(CheckStatus.fail);
			entry.setMessage(Exceptions.getRootCause(e).getMessage());
		} finally {
			try {
				leadershipManager.removeLeadershipListener(domainId, leadershipListener);
			} catch (Exception e) {
				logger.error("Error while trying to remove the leadership listener.", e);
				if (entry.getCheckStatus() == CheckStatus.ok) {
					entry.setCheckStatus(CheckStatus.fail);
					entry.setMessage(Exceptions.getRootCause(e).getMessage());
				}
			}
		}

		leadershipCheckResultEntry = entry;
	}

	class LeadershipListener4Check implements LeadershipListener {
		CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void onLeadershipGranted(LeadershipContext context) {
			logger.trace(() -> "Got leadership");
			latch.countDown();
		}

		@Override
		public void surrenderLeadership(LeadershipContext context) {
			logger.trace(() -> "Was ordered to surrender leadership");
		}
	}

	@Configurable
	public void setLocking(Locking locking) {
		this.locking = locking;
	}

	@Configurable
	public void setLeadershipManager(LeadershipManager leadershipManager) {
		this.leadershipManager = leadershipManager;
	}

	@Override
	public void postConstruct() {
		checkFuture = scheduledExecutorService.schedule(this::performConcurrencyChecks, 10, TimeUnit.SECONDS);
	}

	@Override
	public void preDestroy() {
		if (checkFuture != null) {
			checkFuture.cancel(true);
		}
	}

	@Required
	public void setScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
		this.scheduledExecutorService = scheduledExecutorService;
	}

	@Configurable
	@Required
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}

}
