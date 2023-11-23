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
package tribefire.extension.scheduling.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.ConfigurableCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.processing.lock.api.LockManager;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.ServiceProcessors;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;

import tribefire.extension.scheduling.model.Scheduled;
import tribefire.extension.scheduling.model.action.Action;
import tribefire.extension.scheduling.model.action.NopAction;
import tribefire.extension.scheduling.model.action.ServiceRequestAction;
import tribefire.extension.scheduling.model.api.Cancel;
import tribefire.extension.scheduling.model.api.Cancelled;
import tribefire.extension.scheduling.model.api.GetList;
import tribefire.extension.scheduling.model.api.ListResult;
import tribefire.extension.scheduling.model.api.PurgeRegistry;
import tribefire.extension.scheduling.model.api.RegistryPurged;
import tribefire.extension.scheduling.model.api.Schedule;
import tribefire.extension.scheduling.model.api.ScheduleResult;
import tribefire.extension.scheduling.model.api.SchedulingRequest;
import tribefire.extension.scheduling.model.api.SchedulingResponse;
import tribefire.extension.scheduling.model.api.UpdateRegistry;
import tribefire.extension.scheduling.model.api.UpdateRegistryResult;
import tribefire.extension.scheduling.model.api.action.ScheduledExecution;
import tribefire.extension.scheduling.model.api.action.ScheduledExecutionResult;
import tribefire.extension.scheduling.model.context.Context;
import tribefire.extension.scheduling.util.CleanupTools;
import tribefire.extension.scheduling.util.Commons;

public class SchedulingServiceProcessor implements ServiceProcessor<SchedulingRequest, SchedulingResponse>, LifecycleAware {

	private static final Logger logger = Logger.getLogger(SchedulingServiceProcessor.class);

	private Supplier<PersistenceGmSession> dataSessionSupplier;
	private Evaluator<ServiceRequest> requestEvaluator;

	private Timer timer;

	private ConcurrentHashMap<String, ScheduledTimerTask> timerTasks = new ConcurrentHashMap<>();

	private LockManager lockManager;
	private Lock lock;

	private final ServiceProcessor<ServiceRequest, SchedulingResponse> ddsaDispatcher = ServiceProcessors.dispatcher(config -> {
		config.registerReasoned(UpdateRegistry.T, this::updateRegistry);
		config.registerReasoned(Schedule.T, this::schedule);
		config.registerReasoned(Cancel.T, this::cancel);
		config.registerReasoned(GetList.T, this::list);
		config.registerReasoned(PurgeRegistry.T, this::purge);
	});

	@Override
	public SchedulingResponse process(ServiceRequestContext requestContext, SchedulingRequest request) {
		Instant start = NanoClock.INSTANCE.instant();

		SchedulingResponse result = ddsaDispatcher.process(requestContext, request);
		result.setDurationInMs(Duration.between(start, NanoClock.INSTANCE.instant()).toMillis());
		return result;
	}

	protected Maybe<ScheduleResult> schedule(@SuppressWarnings("unused") ServiceRequestContext requestContext, Schedule request) {
		Date scheduledDate = request.getScheduledDate();

		PersistenceGmSession session = dataSessionSupplier.get();
		ConfigurableCloningContext cloningContext = ConfigurableCloningContext.build().supplyRawCloneWith(session).skipIndentifying(true)
				.skipGlobalId(true).done();

		final Scheduled scheduled;
		lock.lock();
		try {
			scheduled = session.create(Scheduled.T);
			scheduled.setScheduledDate(scheduledDate);
			scheduled.setDescription(request.getDescription());
			scheduled.setAction(Action.T.clone(cloningContext, request.getAction(), StrategyOnCriterionMatch.skip));
			scheduled.setContext(Context.T.clone(cloningContext, request.getContext(), StrategyOnCriterionMatch.skip));

			session.commit();

		} finally {
			lock.unlock();
		}
		createScheduledTimerTask(scheduled);

		ScheduleResult result = ScheduleResult.T.create();
		result.setScheduledId(scheduled.getId());
		return Maybe.complete(result);
	}

	private ScheduledTimerTask createScheduledTimerTask(Scheduled scheduled) {
		ScheduledTimerTask task = new ScheduledTimerTask(scheduled.getId(), this);
		Date scheduledDate = scheduled.getScheduledDate();
		timer.schedule(task, scheduledDate);
		timerTasks.put(scheduled.getId(), task);
		return task;
	}

	private void cancelScheduledTimerTask(String id) {
		ScheduledTimerTask cancelledTask = timerTasks.remove(id);
		if (cancelledTask != null) {
			try {
				cancelledTask.cancel();
			} catch (Exception e) {
				logger.debug(() -> "Could not cancel timer task " + cancelledTask, e);
			}
		}
	}

	protected Maybe<UpdateRegistryResult> updateRegistry(@SuppressWarnings("unused") ServiceRequestContext requestContext, UpdateRegistry request) {
		logger.debug(() -> "Updating timers from Access");

		PersistenceGmSession session = dataSessionSupplier.get();
		final List<Scheduled> list;
		lock.lock();
		try {
			EntityQuery query = EntityQueryBuilder.from(Scheduled.T).tc(Commons.ALL_TC).done();
			list = session.query().entities(query).list();

			Set<String> dbIds = list.stream().map(s -> (String) s.getId()).collect(Collectors.toSet());

			// Cancel local timers that are not in the Access anymore

			Set<String> toCancel = new HashSet<>(timerTasks.keySet());
			toCancel.removeAll(dbIds);
			toCancel.forEach(this::cancelScheduledTimerTask);

			// Delete all timers that have been executed already

			Set<String> alreadyExecuted = list.stream().filter(s -> s.getExecutionDate() != null).map(s -> (String) s.getId())
					.collect(Collectors.toSet());
			alreadyExecuted.forEach(id -> {
				cancelScheduledTimerTask(id);
				CleanupTools.deleteEntityRecursively(() -> session, Scheduled.T, id);
			});

			// Add new timers if they were created on another node

			list.stream().filter(s -> !timerTasks.containsKey(s.getId()) && !alreadyExecuted.contains(s.getId()))
					.forEach(this::createScheduledTimerTask);

			session.commit();
		} finally {
			lock.unlock();
		}
		UpdateRegistryResult result = UpdateRegistryResult.T.create();
		result.setScheduledCount(list.size());
		return Maybe.complete(result);
	}

	protected Maybe<Cancelled> cancel(@SuppressWarnings("unused") ServiceRequestContext requestContext, Cancel request) {
		String id = request.getScheduledId();
		logger.debug(() -> "Cancelling Scheduled with ID " + id);

		PersistenceGmSession session = dataSessionSupplier.get();
		lock.lock();
		try {
			CleanupTools.deleteEntityRecursively(() -> session, Scheduled.T, id);
			cancelScheduledTimerTask(id);
			session.commit();
		} finally {
			lock.unlock();
		}
		return Maybe.complete(Cancelled.T.create());
	}

	protected Maybe<ListResult> list(@SuppressWarnings("unused") ServiceRequestContext requestContext, GetList request) {

		PersistenceGmSession session = dataSessionSupplier.get();
		EntityQuery query = null;
		if (request.getIncludeExecuted()) {
			query = EntityQueryBuilder.from(Scheduled.T).tc(Commons.ALL_TC).done();
		} else {
			query = EntityQueryBuilder.from(Scheduled.T).where().property(Scheduled.executionDate).eq(null).tc(Commons.ALL_TC).done();
		}
		List<Scheduled> list = session.queryDetached().entities(query).list();

		ListResult result = ListResult.T.create();
		result.setList(list);
		return Maybe.complete(result);
	}

	protected Maybe<RegistryPurged> purge(@SuppressWarnings("unused") ServiceRequestContext requestContext, PurgeRegistry request) {

		PersistenceGmSession session = dataSessionSupplier.get();
		final List<String> idList;
		lock.lock();
		try {
			SelectQuery query = new SelectQueryBuilder().from(Scheduled.T, "s").select("s", Scheduled.id).done();
			idList = session.queryDetached().select(query).list();

			idList.forEach(id -> CleanupTools.deleteEntityRecursively(() -> session, Scheduled.T, id));
			timerTasks.keySet().forEach(this::cancelScheduledTimerTask);
			session.commit();
		} finally {
			lock.unlock();
		}
		RegistryPurged result = RegistryPurged.T.create();
		result.setRemovedEntryCount(idList.size());
		return Maybe.complete(result);
	}

	protected void triggerScheduled(String scheduledId) {

		PersistenceGmSession session = dataSessionSupplier.get();
		Lock scheduledLock = lockManager.forIdentifier("scheduled-" + scheduledId).lockTtl(10L, TimeUnit.SECONDS).exclusive();
		scheduledLock.lock();
		try {
			EntityQuery query = EntityQueryBuilder.from(Scheduled.T).where().property(Scheduled.id).eq(scheduledId).tc(Commons.ALL_TC).done();
			Scheduled scheduled = session.query().entities(query).first();
			if (scheduled != null) {
				try {
					scheduled.setExecutionDate(new Date());
					session.commit();
					timerTasks.remove(scheduledId);

					Action action = scheduled.getAction();
					if (action instanceof ServiceRequestAction serviceAction) {

						String domainId = serviceAction.getDomainId();
						if (domainId == null) {
							domainId = session.getAccessId();
						}
						ConfigurableCloningContext cloningContext = ConfigurableCloningContext.build().supplyRawCloneWith(session)
								.skipIndentifying(true).skipGlobalId(true).done();

						ScheduledExecution req = ScheduledExecution.T.create();
						req.setScheduledId(scheduledId);
						req.setDescription(scheduled.getDescription());
						req.setContext(Context.T.clone(cloningContext, scheduled.getContext(), StrategyOnCriterionMatch.skip));
						req.setDomainId(domainId);
						req.eval(requestEvaluator).get(new AsyncCallback<ScheduledExecutionResult>() {
							@Override
							public void onSuccess(ScheduledExecutionResult future) {
								logger.debug(() -> "Successfully called Service Request on execution of " + scheduled);
							}
							@Override
							public void onFailure(Throwable t) {
								logger.debug(() -> "Error while calling Service Request on execution of " + scheduled, t);
							}
						});
					} else if (action instanceof NopAction) {
						logger.debug(() -> "No operation intended for " + scheduled);
					} else {
						throw new IllegalStateException("Unsupported action: " + action.type().getTypeSignature());
					}
					scheduled.setExecutionSuccess(true);
				} catch (Exception e) {
					logger.debug(() -> "Error while executing action on Scheduled: " + scheduled);
					scheduled.setExecutionSuccess(false);
					scheduled.setErrorMessage(StringTools.substringByUtf8BytesLength(e.getMessage(), 4000));
				} finally {
					session.commit();
				}
			}
		} finally {
			scheduledLock.unlock();
		}
	}

	@Configurable
	@Required
	public void setDataSessionSupplier(Supplier<PersistenceGmSession> dataSessionSupplier) {
		this.dataSessionSupplier = dataSessionSupplier;
	}
	@Configurable
	@Required
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}
	@Configurable
	@Required
	public void setLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}

	@Override
	public void postConstruct() {
		this.timer = new Timer(true);
		this.lock = lockManager.forIdentifier("scheduling").lockTtl(10L, TimeUnit.SECONDS).exclusive();
	}

	@Override
	public void preDestroy() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

}
