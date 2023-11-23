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
package tribefire.platform.impl.worker.impl;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.DestructionAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.core.commons.IdentificationBuilders;
import com.braintribe.model.processing.securityservice.api.UserSessionScope;
import com.braintribe.model.processing.securityservice.api.UserSessionScoping;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceException;
import com.braintribe.model.processing.worker.api.Worker;
import com.braintribe.model.processing.worker.api.WorkerContext;
import com.braintribe.model.processing.worker.api.WorkerException;
import com.braintribe.model.processing.worker.api.WorkerManager;
import com.braintribe.model.processing.worker.api.WorkerManagerControl;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.utils.RandomTools;

import tribefire.cortex.leadership.api.LeadershipContext;
import tribefire.cortex.leadership.api.LeadershipListener;
import tribefire.cortex.leadership.api.LeadershipManager;

public class BasicWorkerManager implements WorkerManager, WorkerManagerControl, DestructionAware {
	private static final Logger logger = Logger.getLogger(BasicWorkerManager.class);
	private ExecutorService executorService;
	private final Map<Worker, WorkerContextImpl> workers = new ConcurrentHashMap<Worker, WorkerContextImpl>();
	private UserSessionScoping userSessionScoping;
	private Supplier<UserSession> systemUserSessionProvider;

	private Supplier<LeadershipManager> leadershipManagerSupplier;
	private LeadershipManager leadershipManager;
	private ReentrantLock leadershipManagerLock = new ReentrantLock();

	private boolean started = false;
	private final Set<Worker> deferredWorkers = new LinkedHashSet<Worker>();
	private final Object deferLock = new Object();

	@Required
	@Configurable
	public void setSystemUserSessionProvider(Supplier<UserSession> systemUserSessionProvider) {
		this.systemUserSessionProvider = systemUserSessionProvider;
	}

	@Required
	public void setLeadershipManagerSuppier(Supplier<LeadershipManager> leadershipManagerSupplier) {
		this.leadershipManagerSupplier = leadershipManagerSupplier;
	}

	@Required
	@Configurable
	public void setUserSessionScoping(UserSessionScoping userSessionScoping) {
		this.userSessionScoping = userSessionScoping;
	}

	@Required
	@Configurable
	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	protected void start(final WorkerContextImpl workerInfo) {

		synchronized (workerInfo) {
			logger.debug(() -> String.format("starting worker: %s", workerInfo));
			try {
				workerInfo.worker.start(workerInfo);
				logger.debug(() -> String.format("worker started: %s", workerInfo));
			} catch (WorkerException e) {
				logger.error(String.format("error in worker: %s", workerInfo), e);
			}
		}
	}

	@Override
	public void deploy(final Worker worker) throws WorkerException {
		synchronized (deferLock) {
			if (started) {
				deployNow(worker);
			} else {
				deferredWorkers.add(worker);
			}
		}
	}

	private void deployDeferredWorkers() {

		for (Worker worker : deferredWorkers) {
			try {
				deployNow(worker);
			} catch (Exception e) {
				String msg = String.format("cannot deploy worker [%s]", worker);
				logger.error(msg, e);
			}
		}
		deferredWorkers.clear();

	}

	public void deployNow(final Worker worker) throws WorkerException {

		final WorkerContextImpl workerInfo = new WorkerContextImpl();

		String tenantId = TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_TENANT_ID, "");

		workerInfo.identification = IdentificationBuilders.fromInstance(worker.getWorkerIdentification()).outerNamespace(tenantId).namespaceFromType()
				.build();
		workerInfo.candidateId = RandomTools.newStandardUuid();
		workerInfo.worker = worker;
		workers.put(worker, workerInfo);

		logger.debug(() -> "Deploying worker: " + workerInfo);

		if (worker.isSingleton()) {
			LeadershipListener leadershipListener = new WorkerLeadershipListener(workerInfo);
			workerInfo.leadershipListener = leadershipListener;

			LeadershipManager leadershipManager = getLeadershipManager();
			logger.debug(() -> "Requesting leadership for singleton worker " + workerInfo + " with leadership manager " + leadershipManager);
			try {
				leadershipManager.addLeadershipListener(workerInfo.identification, leadershipListener);
			} catch (RuntimeException e) {
				workers.remove(worker);
				throw new WorkerException(String.format("failed when requesting leadership for singleton worker %s", workerInfo), e);
			}
		} else {
			start(workerInfo);
		}
	}

	@Override
	public void undeploy(Worker worker) throws WorkerException {
		WorkerContextImpl workerInfo = workers.remove(worker);

		logger.debug(() -> "Undeploying worker: " + workerInfo);

		if (workerInfo != null) {
			synchronized (workerInfo) {
				stop(workerInfo);

				if (worker.isSingleton()) {
					try {
						getLeadershipManager().removeLeadershipListener(workerInfo.identification, workerInfo.leadershipListener);
					} catch (RuntimeException e) {
						throw new WorkerException("error while removing leadership listener for worker: " + workerInfo, e);
					}
				}
			}
		} else {
			logger.debug(() -> "No worker info available for worker " + worker);
		}
	}

	private LeadershipManager getLeadershipManager() {
		if (leadershipManager == null)
			loadLeadershipManagerSync();

		return leadershipManager;
	}

	private void loadLeadershipManagerSync() {
		if (leadershipManager == null) {
			leadershipManagerLock.lock();
			try {
				if (leadershipManager == null) {
					leadershipManager = leadershipManagerSupplier.get();
				}
			} finally {
				leadershipManagerLock.unlock();
			}
		}
	}

	protected void stop(WorkerContextImpl workerInfo) {
		synchronized (workerInfo) {
			Worker worker = workerInfo.worker;

			try {
				logger.debug(() -> String.format("stopping worker: %s", workerInfo));
				worker.stop(workerInfo);
			} catch (WorkerException e) {
				logger.error(String.format("error while stopping worker: %s", workerInfo));
			}
		}
	}

	private class WorkerLeadershipListener implements LeadershipListener {

		private final WorkerContextImpl workerInfo;
		private boolean workerStarted = false;

		public WorkerLeadershipListener(WorkerContextImpl workerInfo) {
			this.workerInfo = workerInfo;
		}

		@Override
		public void surrenderLeadership(LeadershipContext context) {
			logger.debug(String.format("received request for surrender of worker: %s", workerInfo));
			workerStarted = false;
			stop(workerInfo);
		}

		@Override
		public void onLeadershipGranted(LeadershipContext context) {
			logger.debug(String.format("leadership was granted for worker %s", workerInfo));
			workerStarted = true;
			start(workerInfo);
		}

		@Override
		public String toString() {
			return "WorkerLeadershipCanidate (workerStarted: " + workerStarted + ") " + workerInfo.toString();
		}

	}

	@Override
	public void preDestroy() {
		WorkerContextImpl infos[] = workers.values().toArray(new WorkerContextImpl[workers.values().size()]);
		for (WorkerContextImpl workerInfo : infos) {
			logger.warn("undeploying worker that was not stopped from externally: " + workerInfo);
			try {
				undeploy(workerInfo.worker);
			} catch (Exception e) {
				logger.warn("Error while automatically undeploying worker: " + workerInfo, e);
			}
		}
		started = false;
	}

	public class WorkerContextImpl implements WorkerContext {
		public LeadershipListener leadershipListener;
		public String candidateId;
		public String identification;
		public Worker worker;
		// public Future<?> future;

		@Override
		public String toString() {
			return String.format("%s[identification=%s,candidateId=%s]", worker.getClass().getSimpleName(), identification, candidateId);
		}

		@Override
		public Future<?> submit(final Runnable task) {
			logger.debug(() -> String.format("worker %s submitted task: %s", this, task));
			final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			return executorService.submit(new Runnable() {
				@Override
				public void run() {
					logger.debug(() -> String.format("task of worker %s started: %s", this, task));
					try {
						UserSessionScope scope = userSessionScoping.forDefaultUser().push();
						ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
						Thread.currentThread().setContextClassLoader(classLoader);
						WorkerExecutionContext wctx = new WorkerExecutionContext("Worker", task);
						wctx.push();
						try {
							task.run();
							logger.debug(() -> String.format("task of worker %s finished: %s", this, task));
						} catch (Throwable e) {
							logger.error(String.format("error in task of worker %s: %s", this, task), e);
						} finally {
							Thread.currentThread().setContextClassLoader(origClassLoader);
							scope.pop();
							wctx.pop();
						}
					} catch (SecurityServiceException e) {
						logger.error(String.format("error when pushing default user session for task of worker %s: %s", this, task), e);
					}
				}
			});
		}

		@Override
		public <T> Future<T> submit(final Callable<T> task) {
			logger.debug(() -> String.format("worker submitted task for %s: %s", this, task));
			final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			return executorService.submit(new Callable<T>() {
				@Override
				public T call() throws Exception {
					logger.debug(() -> String.format("task of worker %s started: %s", this, task));
					ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
					try {
						UserSessionScope scope = userSessionScoping.forDefaultUser().push();
						Thread.currentThread().setContextClassLoader(classLoader);
						WorkerExecutionContext wctx = new WorkerExecutionContext("Worker", task);
						wctx.push();
						try {
							T returnValue = task.call();
							logger.debug(() -> String.format("task of worker %s finished: %s", this, task));
							return returnValue;
						} catch (Throwable e) {
							logger.error(String.format("error in task of worker %s: %s", this, task), e);
							if (e instanceof RuntimeException) {
								throw (RuntimeException) e;
							} else if (e instanceof Error) {
								throw (Error) e;
							} else {
								throw new UndeclaredThrowableException(e);
							}
						} finally {
							Thread.currentThread().setContextClassLoader(origClassLoader);
							scope.pop();
							wctx.pop();
						}

					} catch (SecurityServiceException e) {
						throw new WorkerException(String.format("error when pushing default user session for task of worker %s: %s", this, task), e);
					}
				}
			});
		}

		@Override
		public UserSessionScoping getUserSessionScoping() {
			return userSessionScoping;
		}

		@Override
		public UserSession getSystemUserSession() throws WorkerException {
			try {
				return systemUserSessionProvider.get();
			} catch (RuntimeException e) {
				throw new WorkerException("error while providing system user session", e);
			}
		}
	}

	@Override
	public void start() throws WorkerException {
		synchronized (deferLock) {
			started = true;
			deployDeferredWorkers();
		}
	}

}
