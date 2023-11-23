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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.HasStringCodec;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
import com.braintribe.model.execution.persistence.JobState;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.processing.lock.api.Locking;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.worker.api.Worker;
import com.braintribe.model.processing.worker.api.WorkerContext;
import com.braintribe.model.processing.worker.api.WorkerException;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.service.api.AsynchronousRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.persistence.ServiceRequestJob;
import com.braintribe.utils.lcd.CollectionTools2;

import tribefire.platform.impl.service.async.PersistingAsynchronousServiceProcessor;

public class ExecutionPersistenceScheduler implements Worker, Runnable {

	private static final Logger logger = Logger.getLogger(ExecutionPersistenceScheduler.class);

	public static boolean run = true;
	private Supplier<PersistenceGmSession> sessionSupplier;

	private GenericEntity workerIdentification;

	private Lock lock;
	protected static TraversingCriterion queryTc = TC.create().negation().disjunction().property(ServiceRequestJob.id)
			.property(ServiceRequestJob.globalId).property(ServiceRequestJob.partition).property(ServiceRequestJob.tries).close().done();
	public static Set<JobState> openStates = CollectionTools2.asSet(JobState.enqueued, JobState.pending, JobState.running);

	private long checkInterval;
	private Future<?> workerFuture;

	private int maxTries = 3;
	private long maxInactivityBeforeRetry = -1;

	protected HasStringCodec stringCodec;
	protected PersistingAsynchronousServiceProcessor processor;
	protected Evaluator<ServiceRequest> requestEvaluator;

	@Override
	public void run() {

		PersistenceGmSession session = sessionSupplier.get();

		while (run) {

			try {
				List<ServiceRequestJob> list = getStaleJobs(session);

				if (list != null) {
					for (ServiceRequestJob job : list) {
						logger.info(() -> "Reviving job " + job.getId());
						reviveJob(job);
					}

				} else {
					try {
						Thread.sleep(checkInterval);
					} catch (InterruptedException e) {
						logger.debug("Got interrupted. Ceasing operations.");
						run = false;
					}
				}
			} catch (Exception e) {
				logger.error("Error while trying to find open jobs.", e);
				try {
					Thread.sleep(checkInterval);
				} catch (InterruptedException e2) {
					logger.debug("Got interrupted. Ceasing operations.");
					run = false;
				}
			}
		}
	}

	private void reviveJob(ServiceRequestJob job) {

		String serializedRequest = job.getSerializedRequest();
		AsynchronousRequest asyncRequext = (AsynchronousRequest) stringCodec.getStringCodec().decode(serializedRequest);
		String correlationId = job.getId();

		processor.submitAsyncRequest(null, asyncRequext, correlationId, true);
	}

	private List<ServiceRequestJob> getStaleJobs(PersistenceGmSession session) {

		GregorianCalendar cal = new GregorianCalendar();
		Date threshold = null;
		if (maxInactivityBeforeRetry < Integer.MAX_VALUE) {
			int maxAgeAsInt = (int) maxInactivityBeforeRetry;
			cal.add(Calendar.MILLISECOND, -maxAgeAsInt);
		} else {
			int maxAgeHours = (int) (maxInactivityBeforeRetry / Numbers.MILLISECONDS_PER_HOUR);
			cal.add(Calendar.HOUR, -maxAgeHours);
		}
		threshold = cal.getTime();

		SelectQuery query = new SelectQueryBuilder().from(ServiceRequestJob.T, "j").where().conjunction()
				.property("j", ServiceRequestJob.lastStatusUpdate).ne(null).property("j", ServiceRequestJob.lastStatusUpdate).lt(threshold)
				.property("j", ServiceRequestJob.tries).lt(maxTries).property("j", ServiceRequestJob.state).in(openStates).close().paging(100, 0)
				.tc(queryTc).select("j").done();

		List<ServiceRequestJob> list = null;
		lock.lock();
		try {
			list = session.query().select(query).list();
			if (list != null && !list.isEmpty()) {
				for (ServiceRequestJob job : list) {
					job.setState(JobState.enqueued);
					job.setLastStatusUpdate(new Date());
					Integer tries = job.getTries();
					if (tries == null) {
						tries = 0;
					}
					job.setTries(tries + 1);
				}
				session.commit();
			}
		} finally {
			lock.unlock();
		}

		if (list != null && list.isEmpty()) {
			list = null;
		}
		return list;
	}

	public void stopScheduler() {
		run = false;
	}

	@Override
	public GenericEntity getWorkerIdentification() {
		return workerIdentification;
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
		return false;
	}

	@Required
	public void setSessionSupplier(Supplier<PersistenceGmSession> sessionSupplier) {
		this.sessionSupplier = sessionSupplier;
	}
	@Required
	public void setLocking(Locking locking) {
		this.lock = locking.forIdentifier(ExecutionPersistenceScheduler.class.getName()).writeLock();
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
	public void setMaxInactivityBeforeRetry(long maxInactivityBeforeRetry) {
		this.maxInactivityBeforeRetry = maxInactivityBeforeRetry;
	}
	@Required
	public void setMaxTries(int maxTries) {
		this.maxTries = maxTries;
	}
	@Required
	public void setStringCodec(HasStringCodec stringCodec) {
		this.stringCodec = stringCodec;
	}
	@Required
	public void setProcessor(PersistingAsynchronousServiceProcessor processor) {
		this.processor = processor;
	}
	@Required
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}

}
