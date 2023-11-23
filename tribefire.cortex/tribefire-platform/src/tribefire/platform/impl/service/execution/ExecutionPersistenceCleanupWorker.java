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
package tribefire.platform.impl.service.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.braintribe.cfg.Required;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm._ExecutionPersistenceModel_;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.HardwiredAccess;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.execution.persistence.cleanup.CleanUpJobs;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.worker.api.Worker;
import com.braintribe.model.processing.worker.api.WorkerContext;
import com.braintribe.model.processing.worker.api.WorkerException;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.cortex._WorkbenchModel_;

public class ExecutionPersistenceCleanupWorker implements Worker, Runnable {

	private static final Logger logger = Logger.getLogger(ExecutionPersistenceCleanupWorker.class);

	private GenericEntity workerIdentification;

	private long checkInterval;
	private Supplier<PersistenceGmSession> sessionSupplier;

	public static boolean run = true;

	private Future<?> workerFuture;
	protected Evaluator<ServiceRequest> requestEvaluator;

	@Override
	public void run() {

		logger.debug(() -> "The JobCleanupWorker has been started successfully. Waiting " + checkInterval + " ms before actually starting.");

		try {
			Thread.sleep(checkInterval);
		} catch (InterruptedException e2) {
			logger.debug("Got interrupted. Ceasing operations.");
			run = false;
		}

		while (run) {

			try {
				PersistenceGmSession cortexSession = sessionSupplier.get();
				List<IncrementalAccess> accesses = getJobAccesses(cortexSession);

				for (IncrementalAccess access : accesses) {

					logger.debug(() -> "Triggering clean up for access " + access.getExternalId());
					CleanUpJobs cleanUp = CleanUpJobs.T.create();
					cleanUp.setCleanAll(false);
					cleanUp.setAccess(access);

					try {
						cleanUp.eval(requestEvaluator).get();
					} catch (Exception e) {
						throw Exceptions.contextualize(e, "Error while trying to trigger a cleanup for job in access " + access.getExternalId());
					}
				}

			} catch (Exception e) {
				logger.error("Error while trying to cleanup old/stale jobs.", e);

			} finally {
				try {
					Thread.sleep(checkInterval);
				} catch (InterruptedException e2) {
					logger.debug("Got interrupted. Ceasing operations.");
					run = false;
				}
			}
		}

		logger.debug(() -> "Ceasing operations.");
	}

	private List<IncrementalAccess> getJobAccesses(PersistenceGmSession cortexSession) {

		//@formatter:off
		SelectQuery query = new SelectQueryBuilder()
				.from(IncrementalAccess.T, "a")
				.where()
					.conjunction()
						.property(IncrementalAccess.deploymentStatus).eq(DeploymentStatus.deployed)
						.entitySignature("a").ne(HardwiredAccess.T.getTypeSignature())
					.close()
				.select("a")
				.done();
		//@formatter:on

		List<IncrementalAccess> allAccesses = cortexSession.query().select(query).list();

		List<IncrementalAccess> jobAccessList = new ArrayList<>();
		allAccesses.forEach(a -> {
			GmMetaModel metaModel = a.getMetaModel();
			if (metaModel != null) {
				ModelOracle oracle = new BasicModelOracle(metaModel);
				Stream<GmMetaModel> modelStream = oracle.getDependencies().transitive().includeSelf().asGmMetaModels();
				boolean containsJobModel = modelStream.anyMatch(m -> m.getName().equals(_ExecutionPersistenceModel_.reflection.name()));
				modelStream = oracle.getDependencies().transitive().includeSelf().asGmMetaModels();
				boolean containsWorkbenchModel = modelStream.anyMatch(m -> m.getName().equals(_WorkbenchModel_.reflection.name()));
				if (containsJobModel && !containsWorkbenchModel) {
					jobAccessList.add(a);
				}
			}
		});

		return jobAccessList;
	}

	@Override
	public GenericEntity getWorkerIdentification() {
		return workerIdentification;
	}

	@Override
	public void start(WorkerContext workerContext) throws WorkerException {
		logger.debug(() -> "Starting the JobCleanupWorker");
		run = true;
		workerFuture = workerContext.submit(this);
	}

	@Override
	public void stop(WorkerContext workerContext) throws WorkerException {
		logger.debug(() -> "Stopping the JobCleanupWorker");
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

	@Required
	public void setCheckInterval(long checkInterval) {
		this.checkInterval = checkInterval;
	}
	@Required
	public void setWorkerIdentification(GenericEntity workerIdentification) {
		this.workerIdentification = workerIdentification;
	}
	@Required
	public void setSessionSupplier(Supplier<PersistenceGmSession> sessionSupplier) {
		this.sessionSupplier = sessionSupplier;
	}
	@Required
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}
}
