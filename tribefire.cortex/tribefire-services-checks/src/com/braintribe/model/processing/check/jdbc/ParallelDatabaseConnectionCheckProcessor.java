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
package com.braintribe.model.processing.check.jdbc;

import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.sql.DataSource;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.execution.generic.ContextualizedFuture;
import com.braintribe.execution.virtual.VirtualThreadExecutorBuilder;
import com.braintribe.logging.Logger;
import com.braintribe.logging.ThreadRenamer;
import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;
import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.processing.check.utils.ExceptionUtil;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.thread.impl.ThreadContextScopingImpl;
import com.braintribe.thread.impl.ThreadRenamingContextScope;
import com.braintribe.utils.lcd.StringTools;

public abstract class ParallelDatabaseConnectionCheckProcessor implements LifecycleAware {

	private static Logger logger = Logger.getLogger(ParallelDatabaseConnectionCheckProcessor.class);

	protected ExecutorService service;
	protected boolean createdService = false;
	protected long timeWarnThreshold = 1000L;
	protected ThreadRenamer threadRenamer = new ThreadRenamer(true);
	protected long checkTimeoutInSec = 300;

	private DeployRegistry deployRegistry;

	protected CheckResult performCheck(List<DatabaseConnectionPool> databaseConnectionPools) {
		if (isEmpty(databaseConnectionPools))
			return emptyResult();
		else
			return performCheckFor(databaseConnectionPools);
	}

	private CheckResult performCheckFor(List<DatabaseConnectionPool> databaseConnectionPools) {

		CheckResult result = CheckResult.T.create();

		List<ContextualizedFuture<ConnectionCheckResult, String>> futures = new ArrayList<>();
		CheckStatus overallStatus = CheckStatus.ok;
		StringBuilder sb = new StringBuilder();

		for (DatabaseConnectionPool dcp : databaseConnectionPools) {
			String name = dcp.getName() + " (" + dcp.getExternalId() + ")";

			// Note: there used to be a test whether the driver class was actually in the classloader
			// With the use plugins for JDBC driver, this is no longer a valid test; it was removed therefore

			DataSource dataSource = null;
			Future<ConnectionCheckResult> f = null;
			if (dcp.getDeploymentStatus() == DeploymentStatus.deployed) {
				dataSource = deployRegistry.resolve(dcp, DatabaseConnectionPool.T);

				ConnectionCheckWorker worker = new ConnectionCheckWorker(dataSource, name);

				ThreadContextScopingImpl threadContextScoping = new ThreadContextScopingImpl();
				ThreadRenamingContextScope threadRenamingContextScope = new ThreadRenamingContextScope(threadRenamer, () -> name);
				threadContextScoping.setScopeSuppliers(Collections.singletonList(() -> threadRenamingContextScope));

				f = service.submit(threadContextScoping.bindContext(worker));

			} else {
				ConnectionCheckResult ccr = new ConnectionCheckResult(false, 0, "Connection not deployed: " + dcp.getExternalId());
				f = CompletableFuture.completedFuture(ccr);
			}

			futures.add(new ContextualizedFuture<>(f, name));
		}

		for (ContextualizedFuture<ConnectionCheckResult, String> f : futures) {

			CheckResultEntry cre = CheckResultEntry.T.create();
			result.getEntries().add(cre);

			String message;
			Long time = -1L;
			try {
				ConnectionCheckResult checkResult = f.get(checkTimeoutInSec, TimeUnit.SECONDS);
				time = checkResult.getElapsedTime();
				if (checkResult.isConnectionValid()) {

					if (time > timeWarnThreshold) {
						message = "Connection " + f.getContext() + " is valid, but the check took " + time + " ms. (expected < " + timeWarnThreshold
								+ "ms)";
						cre.setCheckStatus(CheckStatus.warn);
						if (overallStatus != CheckStatus.fail) {
							overallStatus = CheckStatus.warn;
						}
					} else {
						message = "Connection " + f.getContext() + " is valid, the check took " + time + " ms.";
						cre.setCheckStatus(CheckStatus.ok);
					}

				} else {
					message = "Connection " + f.getContext() + " is invalid.";
					cre.setCheckStatus(CheckStatus.fail);
					overallStatus = CheckStatus.fail;
				}

				// Append detailed message from checkResult if available
				String checkDetails = checkResult.getDetails();
				if (!StringTools.isEmpty(checkDetails)) {
					message += " (" + checkDetails + ")";
				}

			} catch (TimeoutException e) {
				logger.debug(() -> "The thread timed out.");
				message = "Connection check timed out after " + checkTimeoutInSec + " seconds.";
				cre.setCheckStatus(CheckStatus.fail);
				overallStatus = CheckStatus.fail;

				f.cancel(true);
			} catch (InterruptedException e) {
				logger.debug(() -> "The thread got interrupted. Stopping the execution and return null.");
				return null;
			} catch (Exception e) {
				logger.error("Error while waiting for result of check " + f.getContext(), e);
				message = ExceptionUtil.getLastMessage(e);
				cre.setCheckStatus(CheckStatus.fail);
				overallStatus = CheckStatus.fail;
			}
			cre.setMessage(message);
			cre.setName(f.getContext());
			cre.setDetails("Checks the connection " + f.getContext());
			sb.append(message + "\n");
		}

		return result;
	}

	private CheckResult emptyResult() {
		CheckResultEntry cre = CheckResultEntry.T.create();
		cre.setCheckStatus(CheckStatus.ok);
		cre.setMessage("No database connections found.");

		CheckResult result = CheckResult.T.create();
		result.getEntries().add(cre);

		return result;
	}

	@Configurable
	public void setService(ExecutorService service) {
		this.service = service;
	}

	@Override
	public void postConstruct() {
		if (service == null) {
			createdService = true;
			service = VirtualThreadExecutorBuilder.newPool().concurrency(10).threadNamePrefix("database-connection-check-")
					.description("Database Connection Check").build();
		}
	}
	@Override
	public void preDestroy() {
		if (createdService && service != null) {
			try {
				service.shutdown();
			} catch (Exception e) {
				logger.error("Error while trying to shutdown executor service,", e);
			}
		}
	}

	@Configurable
	public void setTimeWarnThreshold(Long timeWarnThreshold) {
		if (timeWarnThreshold != null) {
			this.timeWarnThreshold = timeWarnThreshold.longValue();
		}
	}

	@Configurable
	public void setCheckTimeoutInSec(Long checkTimeoutInSec) {
		if (checkTimeoutInSec != null) {
			this.checkTimeoutInSec = checkTimeoutInSec;
		}
	}
	@Required
	public void setDeployRegistry(DeployRegistry deployRegistry) {
		this.deployRegistry = deployRegistry;
	}

}
