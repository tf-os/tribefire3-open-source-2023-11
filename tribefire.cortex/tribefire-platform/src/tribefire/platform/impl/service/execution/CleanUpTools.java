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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.execution.persistence.HasTimeToLive;
import com.braintribe.model.execution.persistence.Job;
import com.braintribe.model.execution.persistence.JobState;
import com.braintribe.model.execution.persistence.metadata.ExecutionPersistenceCleanupExclusion;
import com.braintribe.model.execution.persistence.metadata.ExecutionPersistenceTimeToLive;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.meta.oracle.TypeHierarchy;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.traverse.EntityCollector;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resourceapi.persistence.DeleteResource;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;

/**
 * Utility class to cleanup jobs
 *
 */
public class CleanUpTools {

	private static final Logger logger = Logger.getLogger(CleanUpTools.class);

	private final static TraversingCriterion allTc = TC.create().negation().joker().done();

	/**
	 * Cleanup jobs and resources (temporary entries)
	 * 
	 * @param cleanupExecutorService
	 *            executor service
	 * @param sessionSupplier
	 *            session supplier
	 * @param access
	 *            The Access that this cleanup should be performed on
	 * @param lock
	 *            lock for cleanup
	 * @param maxAge
	 *            maximum age of the entries
	 */
	// TODO:cleanup lock and executorService?
	public static void cleanupJobsAndResources(ExecutorService cleanupExecutorService, Supplier<PersistenceGmSession> cortexSessionSupplier,
			Supplier<PersistenceGmSession> sessionSupplier, IncrementalAccess access, Lock lock, final long maxAge) {

		String accessId = access.getExternalId();

		GmMetaModel partialModel = access.getMetaModel();
		if (partialModel == null) {
			throw new IllegalArgumentException("The provided access " + access.getExternalId() + " does not reference a model.");
		}
		GmMetaModel metaModel = loadFullModel(cortexSessionSupplier, partialModel);
		metaModel.deploy();

		PersistenceGmSession session = sessionSupplier.get();
		ModelOracle oracle = new BasicModelOracle(metaModel);
		CmdResolver resolver = CmdResolverImpl.create(oracle).done();

		deleteOutdatedEntities(cleanupExecutorService, lock, () -> getNextBatchMarkedForRemovalOrDefaultExpired(session, accessId, maxAge),
				jobId -> deleteJob(resolver, sessionSupplier, jobId));

		Map<EntityType<?>, ExecutionPersistenceTimeToLive> typesWithMetaData = new HashMap<>();
		Set<EntityType<?>> typesWithHasTimeToLive = new HashSet<>();

		getListOfTypes(access, typesWithMetaData, typesWithHasTimeToLive);

		for (Map.Entry<EntityType<?>, ExecutionPersistenceTimeToLive> entry : typesWithMetaData.entrySet()) {
			deleteOutdatedEntities(cleanupExecutorService, lock, () -> getNextBatchForMetaData(session, accessId, entry.getKey(), entry.getValue()),
					jobId -> deleteJob(resolver, sessionSupplier, jobId));

		}
		for (EntityType<?> type : typesWithHasTimeToLive) {
			deleteOutdatedEntities(cleanupExecutorService, lock, () -> getNextBatchForHasTimeToLive(session, accessId, type),
					jobId -> deleteJob(resolver, sessionSupplier, jobId));

		}

	}

	private static GmMetaModel loadFullModel(Supplier<PersistenceGmSession> cortexSessionSupplier, GmMetaModel metaModel) {
		Instant start = NanoClock.INSTANCE.instant();
		PersistenceGmSession session = cortexSessionSupplier.get();
		EntityQuery query = EntityQueryBuilder.from(GmMetaModel.T).where().property(GmMetaModel.id).eq(metaModel.getId()).tc(allTc).done();
		GmMetaModel fullModel = session.query().entities(query).unique();
		logger.debug(() -> "Loading the full model " + metaModel.getId() + " took " + StringTools.prettyPrintDuration(start, true, null));
		return fullModel;
	}

	private static void getListOfTypes(IncrementalAccess access, Map<EntityType<?>, ExecutionPersistenceTimeToLive> typesWithMetaData,
			Set<EntityType<?>> typesWithHasTimeToLive) {
		GmMetaModel metaModel = access.getMetaModel();
		ModelOracle oracle = new BasicModelOracle(metaModel);
		EntityTypeOracle typeOracle = oracle.findEntityTypeOracle(Job.T);

		TypeHierarchy instantiableTypes = typeOracle.getSubTypes().transitive();
		Set<EntityTypeOracle> asEntityTypeOracles = instantiableTypes.asEntityTypeOracles();

		CmdResolver resolver = CmdResolverImpl.create(oracle).done();

		for (EntityTypeOracle eto : asEntityTypeOracles) {

			ExecutionPersistenceTimeToLive ttl = resolver.getMetaData().entityType((EntityType<?>) eto.asType())
					.meta(ExecutionPersistenceTimeToLive.T).exclusive();
			if (ttl != null) {
				typesWithMetaData.put(eto.asType(), ttl);
			}
			EntityType<?> entityType = eto.asType();
			if (HasTimeToLive.T.isAssignableFrom(entityType)) {
				typesWithHasTimeToLive.add(eto.asType());
			}
		}
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	// outdated jobs
	private static void deleteOutdatedEntities(ExecutorService cleanupExecutorService, Lock lock, Supplier<List<String>> idSupplier,
			Function<String, Boolean> deleteFunction) {

		int loopCounter = 0;
		int errorCounter = 0;
		boolean deletedElements = true;
		int severeErrors = 0;

		while (deletedElements && ExecutionPersistenceCleanupWorker.run) {

			loopCounter++;
			if ((loopCounter % 100) == 0) {
				// cool off for a moment
				logger.info(() -> "CleanUp will take a break for a minute");
				try {
					Thread.sleep(Numbers.MILLISECONDS_PER_MINUTE);
				} catch (InterruptedException ie) {
					return;
				}
			}

			deletedElements = false;

			lock.lock();
			try {
				List<String> list = idSupplier.get();

				if (list != null) {
					deletedElements = true;

					List<Future<Boolean>> futures = new ArrayList<>();

					for (String entityId : list) {
						futures.add(cleanupExecutorService.submit(() -> deleteFunction.apply(entityId)));
					}
					for (Future<Boolean> future : futures) {
						Boolean success = future.get();
						if (!success) {
							errorCounter++;
						}
					}
				}

			} catch (Exception e) {
				logger.info(() -> "Error while trying to cleanup old/stale conversion entities.", e);
				severeErrors++;
			} finally {
				lock.unlock();
			}

			if (errorCounter > 1000) {
				logger.error(() -> "There are more than 100 errors during cleaning up outdated entities. Halting the processing now.");
			}
			if (severeErrors > 10) {
				logger.error(() -> "There are more than 10 severe errors during cleaning up outdated entities. Halting the processing now.");
			}
		}

	}

	private static Boolean deleteJob(CmdResolver resolver, Supplier<PersistenceGmSession> sessionSupplier, String jobId) {

		if (jobId == null) {
			return Boolean.TRUE;
		}

		try {
			deleteJobWithDeleteMode(resolver, sessionSupplier, jobId, DeleteMode.ignoreReferences);
		} catch (Exception e) {
			logger.debug(
					() -> "There was an error during the deletion of Job: " + jobId + " (" + e.getMessage() + "). Retrying without ignoreReferences");
			try {
				deleteJobWithDeleteMode(resolver, sessionSupplier, jobId, null);
			} catch (Exception e2) {
				logger.debug(() -> "Error while trying to clean up job " + jobId, e2);
				return Boolean.FALSE;
			}
		}

		return Boolean.TRUE;

	}

	private static boolean deleteJobWithDeleteMode(CmdResolver resolver, Supplier<PersistenceGmSession> sessionSupplier, String jobId,
			DeleteMode deleteMode) {

		PersistenceGmSession session = sessionSupplier.get();
		EntityQuery query = EntityQueryBuilder.from(Job.T).where().property(Job.id).eq(jobId).tc(allTc).done();
		Job job = session.query().entities(query).first();
		if (job == null) {
			return Boolean.TRUE;
		}

		EntityCollector collector = new EntityCollector() {
			@Override
			protected boolean include(GenericModelType type) {
				if (type instanceof EntityType) {
					EntityType<?> entityType = (EntityType<?>) type;
					ExecutionPersistenceCleanupExclusion exclusion = resolver.getMetaData().entityType(entityType)
							.meta(ExecutionPersistenceCleanupExclusion.T).exclusive();
					if (exclusion != null) {
						return false;
					}
				}
				return super.include(type);
			}
			@Override
			protected boolean include(Property property, GenericEntity entity, EntityType<?> entityType) {
				ExecutionPersistenceCleanupExclusion exclusion = resolver.getMetaData().entityType(entityType).property(property)
						.meta(ExecutionPersistenceCleanupExclusion.T).exclusive();
				if (exclusion != null) {
					return false;
				}
				return super.include(property, entity, entityType);
			}
		};
		collector.visit(job);
		Set<GenericEntity> entities = collector.getEntities();

		Set<Resource> resources = entities.stream().filter(ge -> ge instanceof Resource).map(ge -> (Resource) ge).collect(Collectors.toSet());
		resources.forEach(r -> {
			try {
				deleteResourcePhysically(session, r);
			} catch (Exception e) {
				logger.debug(() -> "Error while trying to physically delete Resource " + r, e);
			}
		});

		for (GenericEntity ge : entities) {
			if (deleteMode == null) {
				session.deleteEntity(ge);
			} else {
				session.deleteEntity(ge, deleteMode);
			}
		}
		session.commit();
		return Boolean.TRUE;

	}

	private static void deleteResourcePhysically(PersistenceGmSession session, Resource r) {
		if (r == null) {
			return;
		}
		try {
			DeleteResource deleteResource = DeleteResource.T.create();
			deleteResource.setResource(r);
			deleteResource.eval(session).get();
		} catch (NotFoundException nfe) {
			logger.trace(() -> "Could not find a physical file/entry for resource " + r.getId());
		} catch (Exception e) {
			logger.trace(() -> "Error while invoking DeleteResource for resource " + r.getId(), e);
		}
	}

	private static List<String> getNextBatchForHasTimeToLive(PersistenceGmSession session, String accessId, EntityType<?> entityType) {

		List<String> allIds = new ArrayList<>();

		List<String> listDone = getNextBatchBasedOnTtlAndState(session, accessId, entityType, HasTimeToLive.successCleanupDate, JobState.done);
		if (listDone != null) {
			allIds.addAll(listDone);
		}
		List<String> listPanic = getNextBatchBasedOnTtlAndState(session, accessId, entityType, HasTimeToLive.errorCleanupDate, JobState.panic);
		if (listPanic != null) {
			allIds.addAll(listPanic);
		}
		List<String> listGeneral = getNextBatchBasedOnTtlAndState(session, accessId, entityType, HasTimeToLive.generalCleanupDate, null);
		if (listGeneral != null) {
			allIds.addAll(listGeneral);
		}
		if (allIds.isEmpty()) {
			return null;
		}
		return allIds;
	}
	private static List<String> getNextBatchBasedOnTtlAndState(PersistenceGmSession session, String accessId, EntityType<?> entityType,
			String ttlProperty, JobState state) {
		SelectQuery query = null;
		Date now = new Date();

		if (state != null) {
			//@formatter:off
			query = new SelectQueryBuilder()
					.from(entityType, "j")
						.where()
							.conjunction()
								.property("j", ttlProperty).ne(null)
								.property("j", ttlProperty).lt(now)
								.property("j", Job.state).eq(state)
								.property("j", Job.partition).eq(accessId)
							.close()
						.paging(100, 0)
						.select("j", Job.id)
					.done();
			//@formatter:on
		} else {
			//@formatter:off
			query = new SelectQueryBuilder()
					.from(entityType, "j")
						.where()
							.conjunction()
								.property("j", ttlProperty).ne(null)
								.property("j", ttlProperty).lt(now)
								.property("j", Job.partition).eq(accessId)
							.close()
						.paging(100, 0)
						.select("j", Job.id)
					.done();
			//@formatter:on
		}
		List<String> list = session.query().select(query).list();
		if (list != null && list.isEmpty()) {
			list = null;
		}
		return list;
	}

	private static List<String> getNextBatchForMetaData(PersistenceGmSession session, String accessId, EntityType<?> entityType,
			ExecutionPersistenceTimeToLive ttl) {

		Long successMaxAge = ttl.getSuccessMaxAge();
		Long errorMaxAge = ttl.getErrorMaxAge();
		Long generalMaxAge = ttl.getGeneralMaxAge();

		List<String> allIds = new ArrayList<>();

		if (successMaxAge != null && successMaxAge > 0) {
			List<String> list = getNextBatchBasedOnStateAndMaxAge(session, accessId, entityType, successMaxAge, JobState.done);
			if (list != null) {
				allIds.addAll(list);
			}
		}
		if (allIds.size() < 100 && errorMaxAge != null && errorMaxAge > 0) {
			List<String> list = getNextBatchBasedOnStateAndMaxAge(session, accessId, entityType, errorMaxAge, JobState.panic);
			if (list != null) {
				allIds.addAll(list);
			}
		}
		if (allIds.size() < 100 && generalMaxAge != null && generalMaxAge > 0) {
			List<String> list = getNextBatchBasedOnStateAndMaxAge(session, accessId, entityType, generalMaxAge, null);
			if (list != null) {
				allIds.addAll(list);
			}
		}
		if (allIds.isEmpty()) {
			return null;
		}
		return allIds;
	}
	private static List<String> getNextBatchBasedOnStateAndMaxAge(PersistenceGmSession session, String accessId, EntityType<?> entityType,
			long maxAge, JobState state) {

		GregorianCalendar cal = new GregorianCalendar();
		Date threshold = null;
		if (maxAge < Integer.MAX_VALUE) {
			int maxAgeAsInt = (int) maxAge;
			cal.add(Calendar.MILLISECOND, -maxAgeAsInt);
		} else {
			int maxAgeHours = (int) (maxAge / Numbers.MILLISECONDS_PER_HOUR);
			cal.add(Calendar.HOUR, -maxAgeHours);
		}
		threshold = cal.getTime();

		SelectQuery query = null;

		if (state != null) {
			//@formatter:off
			query = new SelectQueryBuilder()
						.from(entityType, "j")
						.where()
							.conjunction()
								.property("j", Job.lastStatusUpdate).ne(null)
								.property("j", Job.lastStatusUpdate).lt(threshold)
								.property("j", Job.state).eq(state)
								.property("j", Job.partition).eq(accessId)
							.close()
						.paging(100, 0)
						.select("j", Job.id)
					.done();
			//@formatter:on
		} else {
			//@formatter:off
			query = new SelectQueryBuilder()
					.from(entityType, "j")
						.where()
							.conjunction()
								.property("j", Job.lastStatusUpdate).ne(null)
								.property("j", Job.lastStatusUpdate).lt(threshold)
								.property("j", Job.partition).eq(accessId)
							.close()
						.paging(100, 0)
						.select("j", Job.id)
					.done();
			//@formatter:on
		}
		List<String> list = session.query().select(query).list();
		if (list != null && list.isEmpty()) {
			list = null;
		}
		return list;
	}

	private static List<String> getNextBatchMarkedForRemovalOrDefaultExpired(PersistenceGmSession session, String accessId, long maxAge) {

		GregorianCalendar cal = new GregorianCalendar();
		Date threshold = null;
		if (maxAge < Integer.MAX_VALUE) {
			int maxAgeAsInt = (int) maxAge;
			cal.add(Calendar.MILLISECOND, -maxAgeAsInt);
		} else {
			int maxAgeHours = (int) (maxAge / Numbers.MILLISECONDS_PER_HOUR);
			cal.add(Calendar.HOUR, -maxAgeHours);
		}
		threshold = cal.getTime();

		//@formatter:off
		SelectQuery query = new SelectQueryBuilder().from(Job.T, "j")
				.where()
					.conjunction()
						.disjunction()
							.conjunction()
								.property("j", Job.lastStatusUpdate).ne(null)
								.property("j", Job.lastStatusUpdate).lt(threshold)
							.close()
							.property("j", Job.state).eq(JobState.markedForRemoval)
						.close()
						.property("j", Job.partition).eq(accessId)
					.close()
				.paging(100, 0)
				.select("j", Job.id).done();
		//@formatter:on

		List<String> list = session.query().select(query).list();
		if (list != null && list.isEmpty()) {
			list = null;
		}
		return list;
	}

	// Delete all jobs markedForRemoval

	// Check sub-types of Job to see which extends HasTimeToLive
	// Delete all jobs with successCleanupDate set and passed and state done
	// Delete all jobs with errorCleanupDate set and passed and state panic
	// Delete all jobs with otherCleanupDate set and passed and state panic

	// Check Meta-data on sub-types of Job and delete all where no cleanup date is set

	// Delete all jobs older than the default maxAge where no cleanup date is set
}
