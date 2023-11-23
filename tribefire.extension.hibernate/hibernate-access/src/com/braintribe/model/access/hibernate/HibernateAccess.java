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
package com.braintribe.model.access.hibernate;

import static com.braintribe.model.access.hibernate.HibernateAccessTools.deproxy;
import static com.braintribe.model.access.hibernate.HibernateAccessTools.ensureIdsAreGmValues;
import static com.braintribe.model.access.hibernate.HibernateAccessTools.ensureTypeSignatureSelectedProperly;
import static com.braintribe.model.processing.manipulation.basic.tools.ManipulationTools.asManipulation;
import static com.braintribe.model.processing.manipulation.basic.tools.ManipulationTools.asManipulationResponse;
import static com.braintribe.model.processing.query.tools.QueryResults.propertyQueryResult;
import static com.braintribe.model.processing.query.tools.QueryResults.selectQueryResult;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.removeLast;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.persistence.OptimisticLockException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.FlushMode;
import org.hibernate.PessimisticLockException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.dialect.lock.LockingStrategyException;
import org.hibernate.exception.LockAcquisitionException;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.function.TriFunction;
import com.braintribe.exception.AuthorizationException;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.AbstractAccess;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.hibernate.gm.CompositeIdValues;
import com.braintribe.model.access.hibernate.hql.HibernateQueryBuilder;
import com.braintribe.model.access.hibernate.hql.HqlBuilder;
import com.braintribe.model.access.hibernate.hql.SelectHqlBuilder;
import com.braintribe.model.access.hibernate.time.HibernateAccessTiming;
import com.braintribe.model.access.hibernate.time.HibernateAccessTiming.ActionType;
import com.braintribe.model.access.hibernate.tools.HibernateMappingInfoProvider;
import com.braintribe.model.access.hibernate.tools.HibernatePropertyReferenceAnalyzer;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.accessdeployment.hibernate.HibernateLogging;
import com.braintribe.model.accessdeployment.jpa.meta.JpaCompositeId;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.commons.EntityReferenceComparator;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.IdGenerator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EssentialTypes;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.ScalarType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.tools.GmValueCodec;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.persistence.ExecuteNativeQuery;
import com.braintribe.model.persistence.NativePersistenceRequest;
import com.braintribe.model.persistence.NativeQueryParameter;
import com.braintribe.model.processing.manipulation.basic.normalization.Normalizer;
import com.braintribe.model.processing.manipulator.api.PropertyReferenceAnalyzer;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.query.support.QueryAdaptingTools;
import com.braintribe.model.processing.query.tools.PreparedTcs;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.model.usersession.UserSessionType;
import com.braintribe.utils.lcd.CollectionTools2;
import com.braintribe.utils.lcd.CommonTools;
import com.braintribe.utils.lcd.NullSafe;

/**
 * Implements the {@link IncrementalAccess} interface to a Hibernate-managed database persistence layer.
 *
 * <h3>CompositeId</h3>
 * 
 * This access supports mappings of entities using composite-id (composite-key in the SQL DB) via {@link JpaCompositeId}
 * mapping. This mapping must in such a case be set on the id property, and it names the columns (not properties) which
 * together form the id.
 * <p>
 * Current implementation has a limited support of up to 30 columns for the composite-key.
 * <p>
 * Internally, it uses a special serializable object called {@link CompositeIdValues}, which is given to Hibernate and
 * contains all the values for the individual columns. On the outside, the value of the id is a string, and it is in
 * fact a comma-separated concatenation of the GM string representations of the individual values. See
 * {@link GmValueCodec} or {@link ScalarType#instanceToGmString(Object)}.
 * <p>
 * When it comes to creating a new entity, the user has to set the id explicitly himself. For an id consisting of a long
 * and string value, it could look something like this:
 * 
 * {@code
 * 	CompositeIdEntity e = session.create(CompositeIdEntity.T);
 * 	e.setId("123L,'Engineer'");
 *  e.setName("Nikola Tesla");
 *  session.commit();
 * }
 * <p>
 * Note that if the columns that make up the composite-key are also mapped from other properties, and we'd try to set
 * the values to something else than what the id string implies, our values would be ignored. (At least on creation, not
 * sure what would happen on edit).
 * 
 * <h3>Native requests</h3>
 * 
 * For security reasons, {@link NativePersistenceRequest} evaluation is only allowed for an internal user
 * ({@link UserSessionType#internal}).
 * 
 * @author gunther.schenk
 * @author dirk.scheffler
 */
public class HibernateAccess extends AbstractAccess implements HibernateComponent {

	private static Logger log = Logger.getLogger(HibernateAccess.class);

	protected SessionFactory hibernateSessionFactory;

	private ModelOracle modelOracle;
	private CmdResolver cmdResolver;
	private HibernateMappingInfoProvider mappingInfoProvider;
	private PropertyReferenceAnalyzer referenceAnalyzer;
	private HibernateLogging logging;

	private int loadingLimit = 200;
	private long durationWarningThreshold = 5000L;
	private long durationDebugThreshold = 100L;
	private int deadlockRetryLimit = 5;

	private static final Set<ScalarType> nativeIdTypes = asSet(EssentialTypes.TYPE_INTEGER, EssentialTypes.TYPE_LONG);

	protected Supplier<GmMetaModel> modelSupplier;
	protected GmMetaModel metaModel;

	protected ReentrantLock metaModelSyncLock = new ReentrantLock();
	protected ReentrantLock cmdResolverLock = new ReentrantLock();

	public HibernateAccess() {
		this.registerCustomPersistenceRequestProcessor(NativePersistenceRequest.T, this::processNativePersistenceRequest);
	}

	// ************************************************************************
	// Getter/Setter
	// ************************************************************************

	@Required
	public void setHibernateSessionFactory(SessionFactory sessionFactory) {
		this.hibernateSessionFactory = sessionFactory;
	}

	@Override
	public SessionFactory getSessionFactory() {
		return hibernateSessionFactory;
	}

	public void setModelSupplier(Supplier<GmMetaModel> modelSupplier) {
		this.modelSupplier = modelSupplier;
	}

	public void setLoadingLimit(int loadingLimit) {
		this.loadingLimit = loadingLimit;
	}

	/**
	 * Specifies the maximum number of times the {@link #applyManipulation(ManipulationRequest)} is attempted in case it
	 * fails due to a deadlock.
	 * <p>
	 * As there is now dedicated deadlock exception, we consider any transaction that ends up with a
	 * {@link LockAcquisitionException}, {@link LockingStrategyException}, {@link PessimisticLockException} or, just to be
	 * safe, {@link OptimisticLockException} and {@link javax.persistence.PessimisticLockException}.
	 */
	public void setDeadlockRetryLimit(Integer deadlockRetryLimit) {
		if (deadlockRetryLimit != null)
			this.deadlockRetryLimit = Math.max(deadlockRetryLimit, 1);
	}

	public void setLogging(HibernateLogging logging) {
		this.logging = logging;
	}

	// ************************************************************************
	// Interface Methods
	// ************************************************************************

	@Override
	public GmMetaModel getMetaModel() {
		return metaModel != null ? metaModel : getMetaModelSync();
	}

	private GmMetaModel getMetaModelSync() {
		if (metaModel != null)
			return metaModel;

		if (modelSupplier == null)
			throw new GenericModelException("Unable to initialize the MetaModel. No MetaModel provider is configured to " + this);

		metaModelSyncLock.lock();
		try {
			if (metaModel == null) {
				metaModel = modelSupplier.get();
			}
			return metaModel;
		} finally {
			metaModelSyncLock.unlock();
		}
	}

	// #########################################################
	// ## . . . . . . . . . . . Queries . . . . . . . . . . . ##
	// #########################################################

	@Override
	public SelectQueryResult query(SelectQuery query) throws ModelAccessException {
		try {
			return runInTransaction(query, ActionType.SelectQuery, this::query);

		} catch (RuntimeException e) {
			throw Exceptions.contextualize(e, "Error while evaluating query: " + query.stringify());
		}
	}

	private SelectQueryResult query(SelectQuery query, Session session, HibernateAccessTiming timing) throws ModelAccessException {
		SelectHqlBuilder hqlBuilder = new SelectHqlBuilder(query);

		org.hibernate.query.Query<?> hqlQuery = newHql(session, hqlBuilder);

		List<?> hqlResults = runHqlQuery(query, hqlQuery, timing);
		ensureIdsAreGmValues(query, hqlResults);
		ensureTypeSignatureSelectedProperly(hqlBuilder, hqlResults);

		boolean hasMore = hasMore(query, hqlResults);
		List<Object> results = cloneSelectQueryResult(hqlResults, query, createStandardCloningContext());

		return selectQueryResult(results, hasMore);
	}

	@Override
	public EntityQueryResult queryEntities(EntityQuery query) throws ModelAccessException {
		return QueryAdaptingTools.queryEntities(query, this);
	}

	@Override
	public PropertyQueryResult queryProperty(PropertyQuery query) throws ModelAccessException {
		if (query.getRestriction() == null)
			return runInTransaction(query, ActionType.PropertyQuery, this::queryPropertyWithNoCondition);
		else
			return QueryAdaptingTools.queryProperties(query, this);
	}

	private PropertyQueryResult queryPropertyWithNoCondition(PropertyQuery query, Session session, HibernateAccessTiming timing)
			throws ModelAccessException {
		// Get the entityClass of the request.
		Property requestedProperty = query.property();
		GenericModelType propertyType = requestedProperty.getType();

		Object propertyValue = null;
		boolean hasMore = false;

		propertyValue = queryPropertyValueObject(query, requestedProperty, session, timing);
		logWarningIfHasRestriction(query, propertyType);

		Object value = clonePropertyQueryResult(requestedProperty, propertyValue, query);

		return propertyQueryResult(value, hasMore);
	}

	private Object queryPropertyValueObject(PropertyQuery query, Property property, Session session, HibernateAccessTiming timing) {
		if (defaultPartition != null && property.isPartition() && !mappingInfoProvider.isPropertyMapped(property))
			return defaultPartition;

		PersistentEntityReference propertyOwnerRef = query.getEntityReference();
		EntityType<?> entityType = propertyOwnerRef.valueType();
		Object id = propertyOwnerRef.getRefId();
		if (hasCompositeId(entityType.getTypeSignature()))
			id = CompositeIdValues.from(id);

		CriteriaQuery<GenericEntity> q = buildQueryForEntityByIds(session, entityType, Collections.singleton(id));

		timing.setQueryInformation(null, query, null);
		timing.processingStarts();
		GenericEntity entity = session.createQuery(q).uniqueResult();
		timing.processingStopped();

		if (entity == null) {
			log.debug(() -> "No entity found for id: " + propertyOwnerRef.getRefId() + " of type: " + entityType.getTypeSignature());
			return null;
		}

		return property.get(entity);
	}

	private void logWarningIfHasRestriction(PropertyQuery query, GenericModelType propertyType) {
		if (query.getRestriction() != null)
			if (propertyType.isCollection()) // This code is only reachable for a collection iff it elements are simple
				log.warn("A conditional query on collections of primitive values is not supported. Restrictions and Ordering are disabled.");
			else
				log.warn("Conditional property queries are only supported for collection properties. Restrictions and Ordering are disabled.");
	}

	public void doFor(SelectQuery query, Consumer<SelectQueryResult> receiver, Session session) throws ModelAccessException {
		org.hibernate.query.Query<GenericEntity> hqlQuery = newHql(session, new SelectHqlBuilder(query));

		List<GenericEntity> entities = hqlQuery.list();
		boolean hasMore = hasMore(query, entities);

		receiver.accept(selectQueryResult(entities, hasMore));
	}

	private <T, Q extends Query> org.hibernate.query.Query<T> newHql(Session session, HqlBuilder<Q> hqlBuilder) {
		hqlBuilder.setAdaptPagingForHasMore(true);
		hqlBuilder.setSession(session);
		hqlBuilder.setDefaultPartition(defaultPartition);
		hqlBuilder.setMappedEntityIndicator(getMappingInfoProvider()::isEntityMapped);
		hqlBuilder.setMappedPropertyIndicator(getMappingInfoProvider()::isPropertyMapped);
		hqlBuilder.setIdAdjuster(this::adjustId);

		try {
			org.hibernate.query.Query<T> hqlQuery = (org.hibernate.query.Query<T>) hqlBuilder.encode();
			log.trace(() -> "Created HQL for " + hqlBuilder.query.entityType().getShortName() + ": " + hqlQuery.getQueryString());
			return hqlQuery;

		} catch (RuntimeException e) {
			throw new GenericModelException("Error while building HQL query for GM query: " + hqlBuilder.query.stringify(), e);
		}
	}

	// #########################################################
	// ## . . . . . . . . . . Manipulations . . . . . . . . . ##
	// #########################################################

	/**
	 * Applies given manipulations against the database in a single hibernate transaction.
	 * <p>
	 * In case a deadlock is detected, it automatically tries again, up to {@link #setDeadlockRetryLimit(Integer)} number of
	 * times.
	 * 
	 * @throw {@link NotFoundException} in case the manipulations reference an entity that cannot be found in the database.
	 */
	@Override
	public ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest) {
		int attempt = 0;
		while (true) {
			try {
				return _applyManipulation(manipulationRequest);

			} catch (PossibleDeadlockException e) {
				if (++attempt == deadlockRetryLimit) {
					log.debug(() -> "Error while applying manipulations: " + manipulationRequest.getManipulation().stringify());
					log.error("[" + getAccessId() + "] Error while applying manipulations.", e);
					throw new ModelAccessException("Error occured while applying manipulations in access: " + getAccessId(), e.originalException);
				}

				log.debug(() -> "Possible deadlock detected for apply manipulation. Trying again. Error: " + e.originalException.getMessage());
			}
		}
	}

	private ManipulationResponse _applyManipulation(ManipulationRequest manipulationRequest) {
		return runInTransaction(manipulationRequest, ActionType.ApplyManipulation, new ApplyManipulationHandler());
	}

	/**
	 * Deadlocks:<br/>
	 * I have added the refMap when handling creates and deletes to stabilize the order in a desperate attempt to prevent DB
	 * deadlocks. I don't know if the first touch of a give type means a lock is acquired,but what else could it be, right?
	 *
	 * Right now it only happens when creating entities, so if stabilizing the order works, it should fix the problem.
	 * 
	 * That being said, if this is causing it, this is not a fix, as we are separating updates (including deletes) and
	 * creates. If say Transaction 1 creates Type1 and deletes Type2, and Transaction 2 is doing "the opposite", we might
	 * have a deadlock again. If this is even the reason, we might then need to make sure the first contact of any kind
	 * (query / create) is ordered by type signature, not operation type.
	 */
	private class ApplyManipulationHandler implements TriFunction<ManipulationRequest, Session, HibernateAccessTiming, ManipulationResponse> {

		private final HibernateApplyStatistics statistics = new HibernateApplyStatistics();
		private final List<Manipulation> inducedManipulations = newList();

		private HibernateManipulatorContext manipulatorContext;
		private HibernateAccessTiming timing;

		@Override
		public ManipulationResponse apply(ManipulationRequest manipulationRequest, Session session, HibernateAccessTiming timing) {
			this.timing = timing;
			try {
				timing.processingStarts();
				timing.setStatistics(statistics);

				logManipulation(manipulationRequest.getManipulation());

				ManipulationResponse result = apply(manipulationRequest, session);

				timing.processingStopped();
				log.trace(() -> "[" + getAccessId() + "] Successfully applied manipulations. [" + statistics + "]");

				return result;

			} catch (LockAcquisitionException | LockingStrategyException | PessimisticLockException | OptimisticLockException
					| javax.persistence.PessimisticLockException e) {
				/* I don't actually know if all of these can even be thrown, or if they indicate deadlock. I've seen
				 * LockAcquisitionException and OptimisticLockException (not JPA but hibernate one). Either way, to be safe we consider
				 * all these as possible deadlock. Worst case we'll get the same error over and over again. */
				throw new PossibleDeadlockException(e);

			} catch (NotFoundException e) {
				/* "Expected" exceptions are not logged, they do not indicate a problem in HA. Client should handle it himself. */
				throw e;

			} catch (Exception e) {
				log.debug(() -> "Error while applying manipulations: " + manipulationRequest.getManipulation().stringify());
				log.error("[" + getAccessId() + "] Error while applying manipulations. [" + statistics + "]", e);
				throw new ModelAccessException("An error occured while applying manipulations in access: " + getAccessId(), e);
			}
		}

		private ManipulationResponse apply(ManipulationRequest manipulationRequest, Session session) throws Exception {
			applyManipulation(session, manipulationRequest);

			handleDeletedEntities(session);
			handleCreatedEntities(session);

			return createManipulationResponse();
		}

		private void applyManipulation(Session session, ManipulationRequest manipulationRequest) {
			// TODO remove the Normalizer, but make sure the explicit assignment of nulls to id is handled properly
			Manipulation manipulation = Normalizer.normalize(manipulationRequest.getManipulation());

			manipulatorContext = prepareManipulatorContext(session, manipulation);
			manipulatorContext.apply(manipulation);
		}

		private void logManipulation(Manipulation manipulation) {
			try {
				log.trace(() -> "[" + getAccessId() + "] About to process: " + manipulation.stringify());
			} catch (Exception e) {
				log.warn("Unable to log applied manipulation due to an error while stringigying it.", e);
			}
		}

		private HibernateManipulatorContext prepareManipulatorContext(Session session, Manipulation manipulation) {
			Set<EntityReference> entityReferences = scanEntityReferences(manipulation);
			Map<EntityType<?>, Set<Object>> idsByEntityType = extractIdsByEntityType(entityReferences);
			Map<EntityReference, GenericEntity> loadedEntities = loadEntitiesByType(idsByEntityType, session, timing);

			return new HibernateManipulatorContext(loadedEntities, HibernateAccess.this, session, statistics);
		}

		/** @see ApplyManipulationHandler */
		private void handleDeletedEntities(Session session) {
			Map<PersistentEntityReference, GenericEntity> entitiesToDelete = refMap(manipulatorContext.getEntitiesToDelete());

			for (Map.Entry<PersistentEntityReference, GenericEntity> entry : entitiesToDelete.entrySet()) {
				PersistentEntityReference reference = entry.getKey();
				GenericEntity entity = entry.getValue();

				String info = "Delete entity : " + reference;
				log.debug(info);
				long start = System.nanoTime();

				session.delete(entity);

				timing.addManipulationEvent(start, info);
				statistics.increaseDeletions();
				log.debug(() -> "Successfully deleted entity : " + reference);
			}
		}

		/** @see ApplyManipulationHandler */
		private void handleCreatedEntities(Session session) throws Exception {
			// save preliminary entities after they were manipulated (which allows do mandatory stuff before saving)
			Map<PreliminaryEntityReference, GenericEntity> preliminaryReferences = refMap(manipulatorContext.getPreliminaryReferenceMap());

			/* collect all preliminary entity references before we save them so that we know which do NOT have an assigned ID. This
			 * is necessary because save() will create IDs recursively */
			Set<GenericEntity> unassignedIdGenericEntities = preliminaryReferences.values().stream() //
					.filter(e -> e.getId() == null) //
					.collect(Collectors.toSet());

			// post processing regarding id assignment and related induced manipulations
			for (Map.Entry<PreliminaryEntityReference, GenericEntity> entry : preliminaryReferences.entrySet()) {
				PreliminaryEntityReference reference = entry.getKey();
				GenericEntity newEntity = entry.getValue();

				String typeSignature = reference.getTypeSignature();

				if (unassignedIdGenericEntities.contains(newEntity) && !idPropertyIsNative(typeSignature))
					// We found an unassigned id and the id property is not natively created. Thus we try to ensure the
					// id value.
					ensureId(newEntity);

				if (hasCompositeId(typeSignature))
					if (unassignedIdGenericEntities.contains(newEntity))
						throw new UnsupportedOperationException("Cannot persist entity with composite id: " + newEntity
								+ " The id was not set explicitly and that is currently not supported."
								+ " The id must be set by the user and it has to be a comma separated concatenation of the inividual values."
								+ " The format for different types is a `GM string` (see ScalarType.instanceToGmString)");
					else
						newEntity.setId(CompositeIdValues.from(newEntity.getId()));

				String info = "Saving entity: " + reference + ". Instance: " + newEntity;
				log.trace(info);
				Object setId = newEntity.getId();
				long start = System.nanoTime();

				session.save(typeSignature, newEntity);

				timing.addManipulationEvent(start, info);

				if (setId != null && !setId.equals(newEntity.getId()))
					throw new IllegalStateException("User cannot set id as the hibernate mappings imply the id is assinged by the DB. Entity: "
							+ newEntity + ", id set by client: " + setId + ", id generated by DB: " + newEntity.getId());

				statistics.increaseCreations();
				log.trace(() -> "Successfully saved entity: " + reference + ". Instance: " + newEntity);

				// check if the id was not assigned already by the manipulation been sent by the caller
				if (unassignedIdGenericEntities.contains(newEntity))
					// also handle if compositeId
					inducedManipulations.add(createChangeIdManipulation(reference, newEntity.getId()));

				if (newEntity.getPartition() == null) {
					EntityReference persistentReference = HibernateAccessTools.createReference(newEntity);

					// In case property is mapped, we set it here and thus store in the DB; If not mapped, no foul, no
					// harm
					ensurePartition(newEntity);

					inducedManipulations.add(createPartitionAssignmentManipulationForReference(persistentReference));
				}
			}
		}

		private <R extends EntityReference> Map<R, GenericEntity> refMap(Map<R, GenericEntity> refMap) {
			Map<R, GenericEntity> preliminaryReferences = new TreeMap<>(EntityReferenceComparator.INSTANCE);
			preliminaryReferences.putAll(refMap);
			return preliminaryReferences;
		}

		private ManipulationResponse createManipulationResponse() {
			Manipulation inducedManipulation = asManipulation(inducedManipulations);
			return asManipulationResponse(inducedManipulation);
		}

	}

	static class PossibleDeadlockException extends RuntimeException {
		private static final long serialVersionUID = -7149109582173609029L;
		public final RuntimeException originalException;

		public PossibleDeadlockException(RuntimeException e) {
			this.originalException = e;
		}
	}

	// #########################################################
	// ## . . . . . Custom Persistence Requests . . . . . . . ##
	// #########################################################

	private Object processNativePersistenceRequest(ServiceRequestContext context, NativePersistenceRequest request) {
		verifyAuthorization(context);

		if (request instanceof ExecuteNativeQuery)
			return processNativeQuery((ExecuteNativeQuery) request);

		throw new UnsupportedOperationException("HibernateAccess does not support native persistence request of type '"
				+ request.entityType().getTypeSignature() + "'. Request: " + request);
	}

	private List<?> processNativeQuery(ExecuteNativeQuery request) {
		try {
			return runInTransaction(request, ActionType.SelectQuery, this::processNativeQuery);

		} catch (RuntimeException e) {
			throw Exceptions.contextualize(e, "Error while evaluating native HQL query: " + request.getQuery());
		}
	}

	private List<?> processNativeQuery(ExecuteNativeQuery request, Session session, HibernateAccessTiming timing) {
		log.debug("Executing native HQL: " + request.getQuery());

		org.hibernate.query.Query<?> hqlQuery = newHql(request, session);

		List<?> hqlResults = runHqlQuery(null, hqlQuery, timing);
		ensureIdsAreGmValues(hqlResults);

		hqlResults = cloneTuples(hqlResults, resolveTc(request), createStandardCloningContext());

		return hqlResults;
	}

	private org.hibernate.query.Query<?> newHql(ExecuteNativeQuery request, Session session) {
		HibernateQueryBuilder<Object> hqBuilder = new HibernateQueryBuilder<>(session, request.getQuery());

		hqBuilder.setPagination(request.getMaxResults(), request.getFirstResultOffset());

		for (NativeQueryParameter parameter : request.getParameters())
			hqBuilder.setParameter(parameter);

		return hqBuilder.getResult();
	}

	private void verifyAuthorization(ServiceRequestContext context) {
		if (!currentUserIsInternalUser(context))
			throw new AuthorizationException("");
	}

	private boolean currentUserIsInternalUser(ServiceRequestContext context) {
		UserSession us = context.findAspect(UserSessionAspect.class);
		return us != null && us.getType() == UserSessionType.internal;
	}

	private TraversingCriterion resolveTc(ExecuteNativeQuery request) {
		return NullSafe.get(request.getTraversingCriterion(), PreparedTcs.scalarOnlyTc);
	}

	// **************************************************************************
	// Helper Methods.
	// **************************************************************************

	private <T> List<T> runHqlQuery(Query query, org.hibernate.query.Query<T> hqlQuery, HibernateAccessTiming timing) {
		timing.setQueryInformation(hibernateSessionFactory, query, hqlQuery);
		timing.processingStarts();

		List<T> hqlResults = hqlQuery.list();
		timing.processingStopped();

		return hqlResults;
	}

	@Override
	protected StandardCloningContext createStandardCloningContext() {
		return new HibernateCloningContext(defaultPartition);
	}

	protected void ensureId(GenericEntity preliminaryEntity) throws Exception {
		String typeSignature = preliminaryEntity.entityType().getTypeSignature();

		IdGenerator idGenerator = this.getExpertRegistry().findExpert(IdGenerator.class).forInstance(preliminaryEntity);

		if (idGenerator == null) {
			// No explicit idGenerator registered for this entity. Now check for the id property type.
			GenericModelType idType = getCmdResolver().getIdType(typeSignature);
			Class<?> idPropertyType = idType.getJavaType();
			idGenerator = this.getExpertRegistry().findExpert(IdGenerator.class).forType(idPropertyType);
		}

		if (idGenerator == null) {
			log.warn("Id is not a native type, no IdGenerator found and the instance has no value assigned. Instance: " + preliminaryEntity);

		} else {
			Object idValue = idGenerator.generateId(preliminaryEntity);
			log.trace(() -> "Generated id: " + idValue + " for entity of type: " + preliminaryEntity.getClass());
			preliminaryEntity.setId(idValue);
		}
	}

	private boolean idPropertyIsNative(String typeSignature) {
		if (hasCompositeId(typeSignature))
			return false;

		ScalarType idPropertyType = getCmdResolver().getIdType(typeSignature);
		return nativeIdTypes.contains(idPropertyType);
	}

	/** Removes the hasMore indicator from the results collection and returns whether there are more entities to fetch. */
	private boolean hasMore(Query request, List<?> results) {
		if (results.size() <= maxPageSize(request))
			return false;

		removeLast(results);
		return true;
	}

	private Map<EntityReference, GenericEntity> loadEntitiesByType( //
			Map<EntityType<?>, Set<Object>> idsByEntityType, Session session, HibernateAccessTiming timing) {

		Map<EntityReference, GenericEntity> loadedEntities = newMap();

		for (Map.Entry<EntityType<?>, Set<Object>> entityIdEntry : idsByEntityType.entrySet()) {
			EntityType<?> entityType = entityIdEntry.getKey();

			boolean hasCompositeId = hasCompositeId(entityType.getTypeSignature());

			Set<Object> rawIds = entityIdEntry.getValue();
			Collection<Object> ids = decodeIfCompositeIds(rawIds, hasCompositeId);

			List<Set<Object>> idBulks = CollectionTools2.splitToSets(ids, loadingLimit);

			for (Set<Object> bulkIds : idBulks) {
				String info = "Loading: " + bulkIds.size() + " entities of type: " + entityType.getJavaType();
				long start = System.nanoTime();

				log.trace(() -> info);
				CriteriaQuery<GenericEntity> q = buildQueryForEntityByIds(session, entityType, bulkIds);

				List<GenericEntity> loadedEntites = session.createQuery(q).list();
				for (GenericEntity loadedEntity : loadedEntites) {

					GenericEntity entity = deproxy(loadedEntity);
					ensurePartition(entity); // in case partition not mapped, we set it so this entity looks like entities outside
					EntityReference reference = HibernateAccessTools.createReference(entity);

					loadedEntities.put(reference, entity);
				}

				timing.addManipulationEvent(start, info);
			}
		}

		return loadedEntities;
	}

	private void ensurePartition(GenericEntity entity) {
		if (entity.getPartition() == null)
			entity.setPartition(defaultPartition);
	}

	private Collection<Object> decodeIfCompositeIds(Set<Object> rawIds, boolean hasCompositeId) {
		if (!hasCompositeId)
			return rawIds;
		else
			return rawIds.stream().map(CompositeIdValues::from).collect(Collectors.toList());
	}

	private Object adjustId(String typeSignature, Object id) {
		return hasCompositeId(typeSignature) ? CompositeIdValues.from(id) : id;
	}

	private boolean hasCompositeId(String typeSignature) {
		return getMappingInfoProvider().hasCompositeId(typeSignature);
	}

	private CriteriaQuery<GenericEntity> buildQueryForEntityByIds(Session session, EntityType<?> entityType, Set<Object> ids) {
		CriteriaBuilder b = session.getCriteriaBuilder();
		CriteriaQuery<Object> q = b.createQuery();
		Root<?> entityRoot = q.from(entityType.getJavaType());

		return (CriteriaQuery<GenericEntity>) (CriteriaQuery<?>) q //
				.select(entityRoot) //
				.where( //
						entityRoot.get(GenericEntity.id).in(ids));
	}

	// **************************************************************************
	// Helpers
	// **************************************************************************

	private <P, R> R runInTransaction(P request, ActionType actionType, TriFunction<P, Session, HibernateAccessTiming, R> code) {
		HibernateAccessTiming timing = new HibernateAccessTiming(actionType, getDurationWarningThreshold(), getDurationDebugThreshold(), logging);

		try {
			Session session = newSessionWithInitializedConnection(actionType.isReadOnly());
			timing.acquiredSession();

			Transaction transaction = null;
			try {
				log.trace(() -> "Beginning transaction for operation: " + actionType);
				transaction = session.beginTransaction();

				R result = code.apply(request, session, timing);
				timing.resultAvailable();

				if (actionType.isReadOnly()) {
					Transaction t = transaction;
					transaction = null;
					t.rollback();
					log.trace(() -> "Successfully rolled-back read-only transaction for operation: " + actionType);

				} else {
					transaction.commit();
					log.trace(() -> "Successfully committed transaction for operation: " + actionType);
				}

				return result;

				// I'm a simple man. I see 'Lock' in the name of the exception and I add it to this list.
			} catch (LockAcquisitionException | LockingStrategyException | PessimisticLockException | OptimisticLockException
					| javax.persistence.PessimisticLockException e) {

				rollbackTransaction(actionType, transaction, e);
				throw new PossibleDeadlockException(e);

			} catch (RuntimeException e) {
				rollbackTransaction(actionType, transaction, e);
				throw e;

			} finally {
				session.close();
			}

		} finally {
			timing.logTimingInformation();
		}
	}

	private void rollbackTransaction(ActionType actionType, Transaction transaction, RuntimeException e) {
		log.trace(() -> "Transaction failed for operation: " + actionType + ". Error:" + e.getMessage());

		if (transaction != null)
			doRollbackSafe(transaction, e);
	}

	private Session newSessionWithInitializedConnection(boolean readOnly) {
		Session session = hibernateSessionFactory.openSession();
		session.setHibernateFlushMode(readOnly ? FlushMode.MANUAL : FlushMode.COMMIT);

		return session;
	}

	private void doRollbackSafe(Transaction transaction, RuntimeException e) {
		try {
			transaction.rollback();
		} catch (RuntimeException rollbackException) {
			e.addSuppressed(rollbackException);
		}
	}

	@Configurable
	public void setDurationWarningThreshold(Long durationWarningThreshold) {
		this.durationWarningThreshold = CommonTools.getValueOrDefault(durationWarningThreshold, 5000L);
	}
	public long getDurationWarningThreshold() {
		return this.durationWarningThreshold;
	}

	@Configurable
	public void setDurationDebugThreshold(long durationDebugThreshold) {
		this.durationDebugThreshold = durationDebugThreshold;
	}
	public long getDurationDebugThreshold() {
		return durationDebugThreshold;
	}

	protected PropertyReferenceAnalyzer getPropertyReferenceAnalyzer() {
		ensureModelTools();
		return referenceAnalyzer;
	}

	public ModelOracle getModelOracle() {
		ensureModelTools();
		return modelOracle;
	}

	public CmdResolver getCmdResolver() {
		ensureModelTools();
		return cmdResolver;
	}

	private HibernateMappingInfoProvider getMappingInfoProvider() {
		ensureModelTools();
		return mappingInfoProvider;
	}

	private void ensureModelTools() {
		if (cmdResolver == null) {
			cmdResolverLock.lock();
			try {
				if (cmdResolver == null) {
					modelOracle = new BasicModelOracle(this.getMetaModel());
					mappingInfoProvider = new HibernateMappingInfoProvider(hibernateSessionFactory);
					referenceAnalyzer = new HibernatePropertyReferenceAnalyzer(modelOracle, mappingInfoProvider);
					cmdResolver = new CmdResolverImpl(modelOracle);
				}
			} finally {
				cmdResolverLock.unlock();
			}
		}
	}

}
