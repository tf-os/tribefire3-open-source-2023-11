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
package com.braintribe.model.processing.wopi.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.worker.api.Worker;
import com.braintribe.model.processing.worker.api.WorkerContext;
import com.braintribe.model.processing.worker.api.WorkerException;
import com.braintribe.model.wopi.service.integration.ExpireWopiSessions;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;

/**
 * {@link Worker} to expire WopiSession
 * 
 *
 */
public class ExpireWopiSessionWorker implements Worker, Runnable {

	private static final Logger logger = Logger.getLogger(ExpireWopiSessionWorker.class);

	private com.braintribe.model.wopi.service.ExpireWopiSessionWorker deployable;

	private Supplier<PersistenceGmSession> sessionSupplier;

	public static boolean run = true;

	private Future<?> workerFuture;

	@Override
	public void run() {

		try {
			Thread.sleep(deployable.getIntervalInMs());
		} catch (InterruptedException e) {
			logger.info(() -> "Got interrupted. Ceasing expiring 'WopiSession's");
			run = false;
		}

		while (run) {

			Instant start = NanoClock.INSTANCE.instant();
			try {
				logger.debug(() -> "Triggering expiring 'WopiSession's");

				ExpireWopiSessions request = ExpireWopiSessions.T.create();
				request.setContext(deployable.getContext());

				PersistenceGmSession session = sessionSupplier.get();
				request.eval(session).get();

				logger.debug(() -> "Successfully expired 'WopiSession's in " + StringTools.prettyPrintDuration(start, true, ChronoUnit.MILLIS));
			} catch (Exception e) {
				logger.error(
						() -> "Error while trying to expired 'WopiSession's after " + StringTools.prettyPrintDuration(start, true, ChronoUnit.MILLIS),
						e);
			} finally {
				try {
					Thread.sleep(deployable.getIntervalInMs());
				} catch (InterruptedException e) {
					logger.info(() -> "Got interrupted. Ceasing expired 'WopiSession's operations.");
					run = false;
				}
			}
		}

	}

	@Override
	public GenericEntity getWorkerIdentification() {
		return deployable;
	}

	@Override
	public void start(WorkerContext workerContext) throws WorkerException {
		run = true;
		workerFuture = workerContext.submit(this);
	}

	@Override
	public void stop(WorkerContext workerContext) throws WorkerException {
		run = false;
		if (workerFuture != null) {
			workerFuture.cancel(true);
		}
		workerFuture = null;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Configurable
	@Required
	public void setDeployable(com.braintribe.model.wopi.service.ExpireWopiSessionWorker deployable) {
		this.deployable = deployable;
	}
	@Configurable
	@Required
	public void setSessionSupplier(Supplier<PersistenceGmSession> sessionSupplier) {
		this.sessionSupplier = sessionSupplier;
	}

}
