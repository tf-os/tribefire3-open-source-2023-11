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
package com.braintribe.model.processing.smood;

import static com.braintribe.model.generic.builder.vd.VdBuilder.referenceWithNewPartition;
import static com.braintribe.model.generic.manipulation.ManipulationType.CHANGE_VALUE;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.changeValue;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.entityProperty;
import static com.braintribe.model.generic.value.EntityReferenceType.preliminary;
import static com.braintribe.model.processing.manipulation.basic.tools.ManipulationTools.asManipulationRequest;
import static com.braintribe.model.processing.manipulation.basic.tools.ManipulationTools.createInverse;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newLinkedMap;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.gwt.utils.genericmodel.providers.entity.EntityLookupException;
import com.braintribe.gwt.utils.genericmodel.providers.entity.EntityNotFoundException;
import com.braintribe.gwt.utils.genericmodel.providers.entity.EntityProvider;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.accessapi.ReferencesCandidate;
import com.braintribe.model.accessapi.ReferencesCandidates;
import com.braintribe.model.accessapi.ReferencesRequest;
import com.braintribe.model.accessapi.ReferencesResponse;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.commons.EntRefHashingComparator;
import com.braintribe.model.generic.commons.PartitionIgnoringEntRefHashingComparator;
import com.braintribe.model.generic.enhance.EnhancedEntity;
import com.braintribe.model.generic.enhance.ManipulationTrackingPropertyAccessInterceptor;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.ManifestationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.OwnerType;
import com.braintribe.model.generic.pr.criteria.CriterionType;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityVisitor;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.reflection.VdHolder;
import com.braintribe.model.generic.tracking.ManipulationCollector;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.generic.tracking.SimpleManipulationCollector;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.EntityReferenceType;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.findrefs.ReferenceFinder;
import com.braintribe.model.processing.manipulation.basic.tools.ManipulationTools;
import com.braintribe.model.processing.manipulator.api.Manipulator;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.query.eval.api.EvalTupleSet;
import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.function.QueryFunctionAspect;
import com.braintribe.model.processing.query.eval.api.function.QueryFunctionExpert;
import com.braintribe.model.processing.query.eval.api.function.aspect.LocaleQueryAspect;
import com.braintribe.model.processing.query.eval.api.repo.IndexInfo;
import com.braintribe.model.processing.query.eval.api.repo.ReflectiveIndexingRepository;
import com.braintribe.model.processing.query.eval.api.repo.RepositoryInfo;
import com.braintribe.model.processing.query.eval.context.BasicQueryEvaluationContext;
import com.braintribe.model.processing.query.planner.QueryPlanner;
import com.braintribe.model.processing.query.support.QueryAdaptingTools;
import com.braintribe.model.processing.query.support.QueryFunctionTools;
import com.braintribe.model.processing.query.support.QueryResultBuilder;
import com.braintribe.model.processing.query.tools.PreparedQueries;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.model.processing.session.api.notifying.NotifyingGmSession;
import com.braintribe.model.processing.session.api.notifying.interceptors.CollectionEnhancer;
import com.braintribe.model.processing.session.api.notifying.interceptors.ManipulationTracking;
import com.braintribe.model.processing.session.impl.notifying.BasicNotifyingGmSession;
import com.braintribe.model.processing.session.impl.session.collection.CollectionEnhancingPropertyAccessInterceptor;
import com.braintribe.model.processing.smood.api.SmoodInterface;
import com.braintribe.model.processing.smood.id.IntegerIdGenerator;
import com.braintribe.model.processing.smood.id.LongIdGenerator;
import com.braintribe.model.processing.smood.id.UniversallyUniqueIdGenerator;
import com.braintribe.model.processing.smood.manipulation.SmoodChangeValueManipulator;
import com.braintribe.model.processing.smood.manipulation.SmoodDeleteManipulator;
import com.braintribe.model.processing.smood.population.PopulationManager;
import com.braintribe.model.processing.smood.population.index.LookupIndex;
import com.braintribe.model.processing.smood.population.index.MetricIndex;
import com.braintribe.model.processing.smood.tools.DuplicatesRemover;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.queryplan.QueryPlan;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.utils.i18n.I18nTools;

/**
 * <h3>Simple Memory Object-Oriented Database</h3>
 *
 * <h4>Important notes</h4> Smood registers a manipulation listener on it's session and it is very important that this listener is the first one in
 * the chain of listeners. Thus, never add your listener as first once a session is assigned to a Smood instance. The reason is that the Smood's
 * listener might throw an exception, which then leads to that manipulation being undone silently. If your listener would be notified first, it would
 * see the change, but not that it was undone.
 *
 * <h4>Instantiation</h4>
 * 
 * See {@link #Smood(ReadWriteLock)}.
 *
 * <h4>IMPLEMENTATION: Underscore methods</h4>
 * 
 * There is a lot of methods whose name starts with r/w + underscore (i.e.: r_ or w_). This indicates, that the method is accessing data inside smood,
 * which means synchronization is needed, but this method does not do any synchronization by itself but it assumes it was already done by the caller.
 * The r/w also indicates what kind of lock is needed for that method. (So basically every single underscore method is either called by another
 * underscore method, or by a method which is wrapped in a matching lock-related try-finally block).
 * 
 * @author peter.gazdik
 * @author dirk.scheffler
 */
public class Smood implements SmoodInterface, IncrementalAccess, EntityProvider, ReflectiveIndexingRepository {

	private static final Logger logger = Logger.getLogger(Smood.class);
	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	private String accessId;
	private final Map<EntityType<?>, IdGenerator<?>> idGenerators = newMap();
	private GmMetaModel metaModel;
	private ModelOracle modelOracle;
	private CmdResolver cmdResolver;
	private final NotifyingGmSession gmSession;
	private Set<String> partitions;
	private String defaultPartition;
	private boolean ignorePartitions;
	private boolean useGlobalIdAsId;

	private final Map<Class<? extends QueryFunctionAspect<?>>, Supplier<?>> functionAspectProviders = newMap();
	private final Map<EntityType<? extends QueryFunction>, QueryFunctionExpert<?>> functionExperts = QueryFunctionTools.functionExperts(null);
	private final ReferenceFinder referenceFinder = new ReferenceFinder(this);

	private final Map<GenericEntity, EntityReference> referenceByEntity = newLinkedMap();
	private Map<EntityReference, GenericEntity> entityByReference = CodingMap.create(PartitionIgnoringEntRefHashingComparator.INSTANCE);

	private final ManipulationListenerImpl manipulationListener = new ManipulationListenerImpl();

	private final QueryPlanner queryPlanner;
	private final PopulationManager populationManager;

	private Lock readLock;
	private Lock writeLock;

	/**
	 * A smood instance needs two things to work - a {@link ReadWriteLock} and a {@link NotifyingGmSession}. Only the {@link ReadWriteLock} is
	 * mandatory, if no session is given, a new one is created internally (but note that all entities inside this smood will be attached to that
	 * session).
	 * 
	 * When it comes to the RWLock, the commonly used implementations are {@link com.braintribe.common.lcd.EmptyReadWriteLock} (in case no locking is
	 * needed), {@link com.braintribe.common.MutuallyExclusiveReadWriteLock} (both in PlatformApi) and the java api's
	 * {@link java.util.concurrent.locks.ReentrantReadWriteLock}.
	 */
	public Smood(ReadWriteLock rwLock) {
		this(new BasicNotifyingGmSession(), rwLock);

		this.gmSession.interceptors().with(CollectionEnhancer.class).add(new CollectionEnhancingPropertyAccessInterceptor());
		this.gmSession.interceptors().with(ManipulationTracking.class).before(CollectionEnhancer.class)
				.add(new ManipulationTrackingPropertyAccessInterceptor());
	}

	/** @see #Smood(ReadWriteLock) */
	public Smood(NotifyingGmSession gmSession, ReadWriteLock rwLock) {
		this.gmSession = gmSession;
		this.gmSession.listeners().asCore(true).addFirst(manipulationListener);

		this.readLock = rwLock.readLock();
		this.writeLock = rwLock.writeLock();

		this.queryPlanner = new QueryPlanner(this);

		this.partitions = null;
		this.ignorePartitions = true;

		this.populationManager = new PopulationManager(referenceByEntity.keySet());

		this.setLocaleProvider(I18nTools.localeProvider);
	}

	public void setLock(ReadWriteLock rwLock) {
		this.readLock = rwLock.readLock();
		this.writeLock = rwLock.writeLock();
	}

	/** {@inheritDoc} */
	@Override
	public Set<GenericEntity> getAllEntities() {
		return Collections.unmodifiableSet(referenceByEntity.keySet());
	}

	/** {@inheritDoc} */
	@Override
	public <T extends GenericEntity> Set<T> getEntitiesPerType(EntityType<T> entityType) {
		Collection<T> entities = getEntitiesPerTypeInternal(entityType);
		if (entities instanceof Set) {
			return Collections.unmodifiableSet((Set<T>) entities);
		} else {
			return newSet(entities);
		}
	}

	private <T extends GenericEntity> Collection<T> getEntitiesPerTypeInternal(EntityType<T> entityType) {
		readLock.lock();
		try {
			return r_getEntitiesPerTypeInternal(entityType);
		} finally {
			readLock.unlock();
		}
	}

	private <T extends GenericEntity> Collection<T> r_getEntitiesPerTypeInternal(EntityType<T> entityType) {
		Collection<T> set = populationManager.getEntirePopulation(entityType);

		return set != null ? set : Collections.<T> emptySet();
	}

	public NotifyingGmSession getGmSession() {
		return gmSession;
	}

	public void setLocaleProvider(Supplier<String> localeProvider) {
		if (localeProvider != null)
			addQueryFunctionAspectProvider(LocaleQueryAspect.class, localeProvider);
	}

	public <T, A extends QueryFunctionAspect<? super T>> void addQueryFunctionAspectProvider(Class<A> aspect, Supplier<T> provider) {
		functionAspectProviders.put(aspect, provider);
	}

	public void setPartitions(Set<String> partitions) {
		if (!entityByReference.isEmpty()) {
			/* The reason is obviously that this affects the codec we use for the entityByReference map, depending on whether or not we want to
			 * consider partitions. */
			throw new GenericModelException("Cannot set partitions on a Smood that already contains some data!");
		}

		this.partitions = partitions;

		setIgnorePartitions(partitions == null);
	}

	/**
	 * If defaultPartition is set to a non-null value, the smood considers this to be the correct {@link GenericEntity#partition} value for all the
	 * entities and acts accordingly:
	 * <ul>
	 * <li>A {@link PropertyQuery} for partition property returns this value.</li>
	 * <li>Selection operands referring to the partition property are replaced with this value.</li>
	 * <li>Any partition property reference in any of the conditions is replaced with this value.</li>
	 * <ul>
	 */
	public void setDefaultPartition(String defaultPartition) {
		this.defaultPartition = defaultPartition;
		this.setIgnorePartitions(defaultPartition != null);
	}

	@Override
	public Set<String> getPartitions() {
		if (partitions == null)
			partitions = asSet(getAccessId());

		return partitions;
	}

	/**
	 * When this flag is set to <tt>true</tt> (default), it means the Smood does not care about the {@link GenericEntity#partition} value of the
	 * entity, but uses the default partition instead.
	 * 
	 * TODO investigate if this is needed, maybe comparing defaultPartition to null would be more appropriate.
	 */
	public void setIgnorePartitions(boolean ignorePartitions) {
		this.ignorePartitions = ignorePartitions;
		this.queryPlanner.ignorePartitions(ignorePartitions);

		if (ignorePartitions)
			entityByReference = CodingMap.create(PartitionIgnoringEntRefHashingComparator.INSTANCE);
		else
			entityByReference = CodingMap.create(EntRefHashingComparator.INSTANCE);
	}

	protected boolean getIgnorePartitions() {
		return ignorePartitions;
	}

	public void setUseGlobalIdAsId(boolean useGlobalIdAsId) {
		this.useGlobalIdAsId = useGlobalIdAsId;
	}

	private SmoodChangeValueManipulator changeValueManipulator;
	private SmoodDeleteManipulator deleteManipulator;
	private final SmoodManifestationManipulator manifestationManipulator = new SmoodManifestationManipulator(this);

	protected Manipulator<ChangeValueManipulation> getChangeValueManipulator() {
		if (changeValueManipulator == null)
			changeValueManipulator = new SmoodChangeValueManipulator(ignorePartitions, useGlobalIdAsId);

		return changeValueManipulator;
	}

	protected SmoodDeleteManipulator getDeleteManipulator(DeleteMode deleteMode) {
		if (deleteManipulator != null && metaModel != null)
			// we already have a manipulator and it was created using the current meta-model - reuse it
			return deleteManipulator;

		if (metaModel != null)
			// this implies we already have a stable meta-model, just not a deleteManipulator, so we build one capable of dropping references
			return deleteManipulator = new SmoodDeleteManipulator(this, DeleteMode.dropReferences);

		// we don't have a stable meta-model, we create a delete manipulator based on what kind of deletes it needs to support
		// See also getModelOracleSafe
		return deleteManipulator = new SmoodDeleteManipulator(this, deleteMode);
	}

	protected Manipulator<ManifestationManipulation> getManifestationManipulator() {
		return manifestationManipulator;
	}

	/** {@inheritDoc} */
	@Override
	public void initialize(Object genericModelValue) {
		writeLock.lock();
		try {
			w_initialize(genericModelValue);
		} finally {
			writeLock.unlock();
		}
	}

	protected void w_initialize(Object genericModelValue) {
		/* We had a bug where this "genericModelValue" was a set containing multiple instances of same type with same id. To clean the smood up, one
		 * can uncomment this line. */
		genericModelValue = DuplicatesRemover.removeDuplicates(genericModelValue);

		GenericModelType type = typeReflection.getType(genericModelValue);

		// scan for all entities and register them
		TraversingContext ctx;
		ctx = type.traverse(genericModelValue, null, this::recognizeIdIfEntity);

		for (GenericEntity entity : ctx.getVisitedObjects())
			w_registerEntity(entity, true);
	}

	@Override
	public void initializePopulation(Iterable<GenericEntity> entities, boolean ensureIds) {
		writeLock.lock();
		try {
			w_initializePopulation(entities, ensureIds);
		} finally {
			writeLock.unlock();
		}
	}

	protected void w_initializePopulation(Iterable<GenericEntity> entities, boolean ensureIds) {
		for (GenericEntity entity : entities) {
			if (entity != null)
				w_registerEntity(entity, ensureIds);
			else
				logger.warn("Null entity in initial population found.");
		}
	}

	private void w_recognizeId(GenericEntity entity) {
		if (!useGlobalIdAsId)
			w_recognizeId(entity.entityType(), entity.getId());
	}

	private void w_recognizeId(EntityType<?> entityType, Object id) {
		if (useGlobalIdAsId || id == null) {
			return;
		}

		try {
			IdGenerator<Object> idGenerator = (IdGenerator<Object>) w_aquireIdGenerator(entityType, id);
			idGenerator.recognizeUsedId(id);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Error while w_recognizeId " + id.toString() + ":" + id.getClass().getName() + " to type '" + entityType.getTypeName() + "'",
					ex);
		}
	}

	@Override
	public void ensureIds() {
		writeLock.lock();
		try {
			w_ensureIds();
		} finally {
			writeLock.unlock();
		}
	}

	private void w_ensureIds() {
		for (GenericEntity entity : referenceByEntity.keySet())
			w_ensureIds(entity);
	}

	private void w_ensureIds(GenericEntity entity) {
		w_ensureGlobalId(entity);
		w_ensurePersistenceId(entity);
	}

	/* package */ void w_updateReferenceMapping(EntityReference oldEntityReference, EntityReference newEntityReference) {
		GenericEntity entity = entityByReference.remove(oldEntityReference);

		GenericEntity otherEntity = entityByReference.get(newEntityReference);
		if (otherEntity != null) {
			/* Being here automatically means entity != otherEntity, cause we retrieve entity using a remove (and assume no value is mapped from
			 * multiple keys) */
			throw new IllegalStateException(
					"Error while updating reference for '" + oldEntityReference + "' as other entity is already registered with new reference: "
							+ newEntityReference + ". Other entity: " + otherEntity + ", new entity: " + entity);
		}

		if (entity != null && newEntityReference != null) {
			entityByReference.put(newEntityReference, entity);
			referenceByEntity.put(entity, newEntityReference);
		}
	}

	private void w_updateReferenceMapping(GenericEntity entity, EntityReference reference) {
		EntityReference oldReference = referenceByEntity.put(entity, reference);
		if (oldReference != null)
			entityByReference.remove(oldReference);

		entityByReference.put(reference, entity);
	}

	@Override
	public void registerEntity(GenericEntity entity, boolean autoGenerateId) {
		writeLock.lock();
		try {
			w_registerEntity(entity, autoGenerateId);
		} finally {
			writeLock.unlock();
		}
	}

	public void registerEntitySilently(GenericEntity entity) {
		writeLock.lock();
		try {
			w_registerEntityInternally(entity);
		} finally {
			writeLock.unlock();
		}
	}

	private void w_registerEntity(GenericEntity entity, boolean autoGenerateId) {
		/* first attach the entity to session, the generate the id, so listeners are also aware of the id change. */
		w_registerEntity(entity);

		if (autoGenerateId)
			w_ensureIds(entity);
	}

	protected void w_registerEntity(GenericEntity entity) {
		if (entity instanceof EnhancedEntity)
			gmSession.attach(entity);
		else
			w_registerEntityInternally(entity);
	}

	private void w_ensurePersistenceId(GenericEntity entity) {
		if (entity.getId() != null)
			return;

		if (useGlobalIdAsId) {
			entity.setId(entity.getGlobalId());
			return;
		}

		EntityType<GenericEntity> entityType = entity.entityType();
		Object id = w_aquireIdGenerator(entityType, null).generateId(entity);
		entity.setId(id);
	}

	private void w_ensureGlobalId(GenericEntity entity) {
		if (entity.getGlobalId() == null)
			entity.setGlobalId(newGlobalId());
	}

	/** {@inheritDoc} */
	@Override
	public void deleteEntity(GenericEntity entity) {
		deleteEntity(entity, DeleteMode.dropReferences);
	}

	/** {@inheritDoc} */
	@Override
	public void deleteEntity(GenericEntity entity, DeleteMode deleteMode) {
		writeLock.lock();
		try {
			w_deleteEntity(entity, deleteMode);
		} finally {
			writeLock.unlock();
		}
	}

	protected void w_deleteEntity(GenericEntity entity, DeleteMode deleteMode) {
		if (!this.referenceByEntity.containsKey(entity))
			throw new GenericModelException("404 Cannot delete entity, it was not found: " + entity);

		getDeleteManipulator(deleteMode) //
				.deleteEntity(entity, deleteMode);
	}

	/** {@inheritDoc} */
	@Override
	public void unregisterEntity(GenericEntity entity) {
		writeLock.lock();
		try {
			w_unregisterEntity(entity);
		} finally {
			writeLock.unlock();
		}
	}

	private void w_unregisterEntity(GenericEntity entity) {
		entityByReference.remove(referenceByEntity.remove(entity));
		populationManager.removeEntity(entity);
	}

	/** {@inheritDoc} */
	@Override
	public <T extends GenericEntity> T getEntity(EntityType<T> entityType, Object id) {
		T entity = findEntity(entityType, id); // this is thread-safe
		if (entity == null)
			throw new NotFoundException("No entity of type " + entityType.getTypeSignature() + " with id " + id + " found!");

		return entity;
	}

	/** {@inheritDoc} */
	@Override
	public <T extends GenericEntity> T getEntity(EntityReference entityReference) {
		readLock.lock();
		try {
			return r_getEntity(entityReference);
		} finally {
			readLock.unlock();
		}
	}

	private <T extends GenericEntity> T r_getEntity(EntityReference entityReference) {
		T entity = r_findEntity(entityReference);
		if (entity == null) {
			if (entityReference instanceof PreliminaryEntityReference)
				throw new IllegalArgumentException("Entity cannot be found for preliminary reference! Reference: "
						+ entityReference.getTypeSignature() + "[" + entityReference.getRefId() + ", " + entityReference.getRefPartition() + "]");

			else
				throw new NotFoundException("No entity of type " + entityReference.getTypeSignature() + "[" + entityReference.getRefId() + ", "
						+ entityReference.getRefPartition() + "] found!");
		}

		return entity;
	}

	/** Evaluates given {@link EntityQuery}. */
	@Override
	public EntityQueryResult queryEntities(EntityQuery query) {
		return QueryAdaptingTools.queryEntities(query, this);
	}

	/** {@inheritDoc} */
	@Override
	public SelectQueryResult query(SelectQuery query) {
		readLock.lock();
		try {
			return r_query(query);
		} finally {
			readLock.unlock();
		}
	}

	protected SelectQueryResult r_query(SelectQuery query) {
		SmoodLogging.selectQuery(query);

		QueryPlan queryPlan = queryPlanner.buildQueryPlan(query);

		SmoodLogging.queryPlan(queryPlan);

		QueryEvaluationContext context = new BasicQueryEvaluationContext(this, queryPlan, functionExperts, functionAspectProviders);
		EvalTupleSet tuples = context.resolveTupleSet(queryPlan.getTupleSet());
		SelectQueryResult result = QueryResultBuilder.buildQueryResult(tuples, context.resultComponentsCount());

		SmoodLogging.selectQueryEvaluationFinished();

		return result;
	}

	/** {@inheritDoc} */
	@Override
	public PropertyQueryResult queryProperty(PropertyQuery query) {
		readLock.lock();
		try {
			return r_queryProperty(query);
		} finally {
			readLock.unlock();
		}
	}

	protected PropertyQueryResult r_queryProperty(PropertyQuery query) {
		PersistentEntityReference entityReference = query.getEntityReference();
		GenericEntity entity = r_getEntity(entityReference);

		Property property = entity.entityType().getProperty(query.getPropertyName());

		Object value = property.get(entity);
		if (value == null)
			return buildNullPropertyResult(property);

		if (hasRestrictionOrOrdering(query))
			return QueryAdaptingTools.queryProperties(query, this);

		return QueryResultBuilder.buildPropertyQueryResult(value, false);
	}

	private PropertyQueryResult buildNullPropertyResult(Property property) {
		Object result = property.isPartition() ? defaultPartition : null;

		return QueryResultBuilder.buildPropertyQueryResult(result, false);
	}

	private boolean hasRestrictionOrOrdering(PropertyQuery query) {
		return query.getRestriction() != null || query.getOrdering() != null;
	}

	/** {@inheritDoc} */
	@Override
	public <T extends GenericEntity> T findEntity(EntityType<T> entityType, Object id) {
		readLock.lock();
		try {
			return r_findEntity(entityType, id);
		} finally {
			readLock.unlock();
		}
	}

	protected <T extends GenericEntity> T r_findEntity(EntityType<T> entityType, Object id) {
		if (id == null)
			return null;

		return populationManager.getIdIndex(entityType).getValue(id);
	}

	/** {@inheritDoc} */
	@Override
	public <T extends GenericEntity> T findEntity(EntityReference reference) {
		readLock.lock();
		try {
			return r_findEntity(reference);
		} finally {
			readLock.unlock();
		}
	}

	protected <T extends GenericEntity> T r_findEntity(EntityReference reference) {
		if (reference.referenceType() == EntityReferenceType.global)
			return r_findEntity(reference.getTypeSignature(), (String) reference.getRefId());
		else
			return (T) entityByReference.get(reference);
	}

	@Override
	public <T extends GenericEntity> T findEntityByGlobalId(String globalId) {
		readLock.lock();
		try {
			return r_findEntity(GenericEntity.T.getTypeSignature(), globalId);
		} finally {
			readLock.unlock();
		}
	}

	private <T extends GenericEntity> T r_findEntity(String typeSignature, String globalId) {
		return populationManager.acquireGlobalIdIndex(typeSignature).getValue(globalId);
	}

	/** {@inheritDoc} */
	@Override
	public ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest) {
		return applyManipulation(manipulationRequest, true); // This is thread-safe
	}

	public ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest, boolean generateId) {
		return applyManipulation(manipulationRequest, generateId, false); // This is thread-safe
	}

	public ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest, boolean generateId,
			boolean ignoreUnknownEntitiesManipulations) {

		return apply() //
				.generateId(generateId) //
				.ignoreUnknownEntitiesManipulations(ignoreUnknownEntitiesManipulations) //
				.request(manipulationRequest) // This is thread-safe
				.getManipulationResponse();
	}

	public ManipulationApplicationBuilder apply() {
		return new ContextBuilder(this);
	}

	public ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest, ContextBuilder context) {
		writeLock.lock();
		try {
			return w_applyManipulationRobust(manipulationRequest, context);
		} finally {
			writeLock.unlock();
		}
	}

	private ManipulationResponse w_applyManipulationRobust(ManipulationRequest manipulationRequest, ContextBuilder context) {
		ManipulationCollector collector = new SimpleManipulationCollector();

		gmSession.listeners().add(collector);

		try {
			return w_applyManipulation(manipulationRequest, context);

		} catch (Exception e) {
			try {
				undoManipulations(collector.getManipulations());

			} catch (Exception undoE) {
				GenericModelException extendedUndoE = new GenericModelException("Error while applying manipulations"
						+ ", followed by an error while cleaning up the state. Original error is added as a suppressed exception.", undoE);
				extendedUndoE.addSuppressed(e);
				throw extendedUndoE;
			}

			throw e;

		} finally {
			gmSession.listeners().remove(collector);
		}
	}

	private void undoManipulations(List<Manipulation> manipulations) {
		if (manipulations.isEmpty())
			return;

		Manipulation inverseManipulation = createInverse(manipulations);
		ManipulationRequest inverseRequest = asManipulationRequest(inverseManipulation);

		apply().localRequest(true).request(inverseRequest);
	}

	protected ManipulationResponse w_applyManipulation(ManipulationRequest manipulationRequest, ContextBuilder context) {
		SmoodManipulatorContext manipulationContext = newManipulationContext(context);

		if (context.ignoreManipulationsReferingToUnknownEntities() && !context.isLocalRequest())
			manipulationContext.setManipulationFilter(this::referencesUnknownEntity);

		ManipulationApplicationListener listener = getListener(context);

		listener.onBeforeRequestApplication();
		manipulationContext.apply(manipulationRequest.getManipulation());
		listener.onAfterRequestApplication();

		ManipulationResponse manipulationResponse = ManipulationResponse.T.create();

		if (context.generateId()) {
			Map<PreliminaryEntityReference, GenericEntity> referenceMap = manipulationContext.getInstantiations();

			Map<EntityReference, GenericEntity> responseReferenceMap = prepareResponseReferenceMap(referenceMap);

			List<Manipulation> inducedManis = newList();

			listener.onBeforeGlobalIdAssignment();
			responseReferenceMap.forEach((reference, entity) -> w_ensureGlobalId(reference, entity, inducedManis));
			listener.onAfterGlobalIdAssignment();

			listener.onBeforePersistenceIdAssignment();
			responseReferenceMap.forEach((reference, entity) -> w_ensurePersistenceId(reference, entity, inducedManis));
			listener.onAfterPersistenceIdAssignment();

			if (!ignorePartitions && defaultPartition != null)
				responseReferenceMap.forEach((reference, entity) -> w_ensurePartition(reference, entity, inducedManis));

			manipulationResponse.setInducedManipulation(ManipulationTools.asManipulation(inducedManis));
		}

		context.setManipulationResponse(manipulationResponse);
		context.setInstantiations(manipulationContext.getInstantiations());
		context.setLenientManifestations(manipulationContext.getManifestations());

		return manipulationResponse;
	}

	private SmoodManipulatorContext newManipulationContext(ContextBuilder context) {
		SmoodManipulatorContext manipulationContext = new SmoodManipulatorContext(this);
		manipulationContext.setIsLocalRequest(context.isLocalRequest());
		manipulationContext.setCheckRefereesOnDelete(context.checkRefereesOnDelete());
		manipulationContext.setManifestUnknownEntities(context.manifestUnknownEntities());
		manipulationContext.setIgnoreAbsentCollectionManipulations(context.ignoreAbsentCollectionManipulations());
		if (context.getInstantiations() != null)
			manipulationContext.getInstantiations().putAll(context.getInstantiations());
		return manipulationContext;
	}

	private static ManipulationApplicationListener getListener(ContextBuilder context) {
		ManipulationApplicationListener listener = context.getManipulationApplicationListener();
		return listener != null ? listener : EmptyManipulationApplicationListener.INSTANCE;
	}

	private static Map<EntityReference, GenericEntity> prepareResponseReferenceMap(Map<PreliminaryEntityReference, GenericEntity> referenceMap) {
		Map<EntityReference, GenericEntity> result = newLinkedMap();

		for (Entry<PreliminaryEntityReference, GenericEntity> entry : referenceMap.entrySet()) {
			EntityReference ref = entry.getKey();
			GenericEntity entity = entry.getValue();

			if (entity.getId() != null)
				ref = entity.reference();
			else if (entity.getPartition() != null)
				ref = referenceWithNewPartition(ref, entity.getPartition());

			result.put(ref, entity);
		}

		return result;
	}

	private void w_ensurePersistenceId(EntityReference reference, GenericEntity preliminaryEntity, List<Manipulation> inducedManis) {
		// check if the id was not assigned already by the manipulation been sent by the caller
		if (reference.referenceType() != preliminary)
			return;

		w_ensurePersistenceId(preliminaryEntity);

		/* we have to manually construct the ChangeValueManipulation for the id change because we cannot rely on the one from the ManipulationTracking
		 * as this uses internal PreliminaryReferences instead of the ones from the caller */

		EntityProperty entityProperty = entityProperty(reference, GenericEntity.id);
		ChangeValueManipulation changeIdManipulation = changeValue(preliminaryEntity.getId(), entityProperty);

		inducedManis.add(changeIdManipulation);
	}

	private void w_ensureGlobalId(EntityReference reference, GenericEntity preliminaryEntity, List<Manipulation> inducedManis) {
		String globalId = preliminaryEntity.getGlobalId();
		if (globalId != null)
			return;

		globalId = newGlobalId();
		preliminaryEntity.setGlobalId(globalId);

		EntityProperty entityProperty = entityProperty(reference, GenericEntity.globalId);
		ChangeValueManipulation changeGlobalIdManipulation = changeValue(globalId, entityProperty);

		inducedManis.add(changeGlobalIdManipulation);
	}

	private void w_ensurePartition(EntityReference reference, GenericEntity preliminaryEntity, List<Manipulation> inducedManis) {
		if (preliminaryEntity.getPartition() != null)
			return;

		if (preliminaryEntity.getId() != null)
			reference = preliminaryEntity.reference();

		preliminaryEntity.setPartition(defaultPartition);

		EntityProperty entityProperty = entityProperty(reference, GenericEntity.partition);
		ChangeValueManipulation changePartitionManipulation = changeValue(defaultPartition, entityProperty);

		inducedManis.add(changePartitionManipulation);
	}

	private IdGenerator<?> w_aquireIdGenerator(EntityType<?> entityType, Object id) {
		if (useGlobalIdAsId)
			return UniversallyUniqueIdGenerator.INSTANCE;
		else
			return idGenerators.computeIfAbsent(entityType, et -> createIdGenerator(et, id));
	}

	private IdGenerator<?> createIdGenerator(EntityType<?> entityType, Object id) {
		GenericModelType idPropertyType = resolveIdPropertyType(entityType, id);

		switch (idPropertyType.getTypeCode()) {
			case integerType:
				return new IntegerIdGenerator();
			case longType:
				return new LongIdGenerator();
			case stringType:
				return UniversallyUniqueIdGenerator.INSTANCE;
			default:
				throw new IllegalArgumentException("Automatic id generation is not supported for id type: " + idPropertyType.getTypeName()
						+ ". Entity type: " + entityType.getTypeSignature() + ", id value: " + id);
		}
	}

	private GenericModelType resolveIdPropertyType(EntityType<?> entityType, Object id) {
		if (id != null)
			return typeReflection.getType(id);

		return cmdResolver != null ? cmdResolver.getIdType(entityType.getTypeSignature()) : SimpleTypes.TYPE_LONG;
	}

	/** Does not invoke methods doing synchronization, so should only be called from within code that's already safe. */
	private void recognizeIdIfEntity(TraversingContext traversingContext) {
		if (traversingContext.getTraversingStack().peek().criterionType() == CriterionType.ENTITY) {
			GenericEntity entity = (GenericEntity) traversingContext.getObjectStack().peek();
			if (entity != null)
				w_recognizeId(entity);
		}
	}

	private static String newGlobalId() {
		return GMF.platform().newUuid();
	}

	/** {@inheritDoc} */
	@Override
	public GenericEntity apply(EntityReference entityReference) throws EntityLookupException, EntityNotFoundException {
		return getEntity(entityReference); // This thread-safe
	}

	@Override
	public GmMetaModel getMetaModel() {
		return metaModel;
	}

	public void setSelfMetaModel(String modelName) {
		setSelfMetaModel(modelName, null);
	}

	public void setSelfMetaModel(String modelName, Supplier<GmMetaModel> bootstrapModelProvider) {
		SelectQuery query = PreparedQueries.modelByName(modelName);

		List<?> models = query(query).getResults();
		GmMetaModel _metaModel = extractSingleMetaModel(modelName, models);

		if (_metaModel == null) {
			if (bootstrapModelProvider == null)
				throw new GenericModelException("No metaModel with name: " + modelName + " found.");

			try {
				_metaModel = bootstrapModelProvider.get();
			} catch (RuntimeException e) {
				throw new GenericModelException("error while providing bootstrap meta model");
			}
		}

		GmMetaModel clonedMetaModel = (GmMetaModel) GmMetaModel.T.clone(_metaModel, null, StrategyOnCriterionMatch.reference);
		setMetaModel(clonedMetaModel);
	}

	private static GmMetaModel extractSingleMetaModel(String modelName, List<?> entities) {
		switch (entities.size()) {
			case 0:
				return null;
			case 1:
				return first(entities);
			default:
				throw new GenericModelException("Found more then one metaModel with name: " + modelName);
		}
	}

	/**
	 * Sets {@link GmMetaModel} and creates new {@link ModelOracle} and {@link CmdResolver}. If those are already available to the caller, consider
	 * using {@link #setCmdResolver(CmdResolver)} instead, for performance and memory efficiency.
	 */
	public void setMetaModel(GmMetaModel metaModel) {
		writeLock.lock();
		try {
			w_setMetaModel(metaModel);
		} finally {
			writeLock.unlock();
		}
	}

	private void w_setMetaModel(GmMetaModel metaModel) {
		this.metaModel = metaModel;
		this.modelOracle = newModelOracle(metaModel);
		this.cmdResolver = newCmdResolver(modelOracle);
		this.populationManager.setCmdResolver(cmdResolver);
		this.deleteManipulator = null;
	}

	/**
	 * This is a better alternative to {@link #setMetaModel(GmMetaModel)}, as it re-uses existing {@link ModelOracle} and {@link CmdResolver}.
	 */
	public void setCmdResolver(CmdResolver cmdResolver) {
		writeLock.lock();
		try {
			w_setCmdResolver(cmdResolver);
		} finally {
			writeLock.unlock();
		}
	}

	protected void w_setCmdResolver(CmdResolver cmdResolver) {
		this.cmdResolver = cmdResolver;
		this.modelOracle = cmdResolver.getModelOracle();
		this.metaModel = modelOracle.getGmMetaModel();
		this.populationManager.setCmdResolver(cmdResolver);
		this.deleteManipulator = null;
	}

	private static ModelOracle newModelOracle(GmMetaModel metaModel) {
		try {
			return metaModel == null ? null : new BasicModelOracle(metaModel);
		} catch (GenericModelException e) {
			logger.error("Error initializing ModelOracle in Smood for metaModel: " + metaModel.getName(), e);
			return null;
		}
	}

	private static CmdResolver newCmdResolver(ModelOracle modelOracle) {
		return modelOracle == null ? null : new CmdResolverImpl(modelOracle);
	}

	public ModelOracle getModelOracle() {
		return modelOracle;
	}

	public CmdResolver getCmdResolver() {
		return cmdResolver;
	}

	/**
	 * This is used when we have a smood without a meta-model, for example when it is a backup of a session. In the future, maybe we should improve
	 * this by not building a new model each time, but replace it only when new items are added to the smood.
	 */
	public ModelOracle getSafeModelOracle() {
		if (modelOracle != null)
			return modelOracle;

		GmMetaModel model = new NewMetaModelGeneration().buildMetaModel("smood:SafeModel-" + System.currentTimeMillis(), getUsedTypes());
		return new BasicModelOracle(model);
	}

	/** {@inheritDoc} */
	@Override
	public Set<EntityType<?>> getUsedTypes() {
		return populationManager.getUsedTypes();
	}

	/** {@inheritDoc} */
	@Override
	public ReferencesResponse getReferences(ReferencesRequest referencesRequest) {
		Set<ReferencesCandidate> candidates = referenceFinder.findReferences(referencesRequest.getReference());

		ReferencesCandidates response = ReferencesCandidates.T.create();
		response.setCandiates(candidates);
		return response;
	}

	private class ManipulationListenerImpl implements ManipulationListener {
		@Override
		public void noticeManipulation(Manipulation manipulation) {
			switch (manipulation.manipulationType()) {
				case CHANGE_VALUE:
					noticeManipulation((ChangeValueManipulation) manipulation);
					break;

				case MANIFESTATION:
					registerEntityInternally(((ManifestationManipulation) manipulation).getEntity());
					break;

				case INSTANTIATION:
					registerEntityInternally(((InstantiationManipulation) manipulation).getEntity());
					break;

				case DELETE:
					unregisterEntity(((DeleteManipulation) manipulation).getEntity());
					break;

				default:
					break;
			}
		}

		private void noticeManipulation(ChangeValueManipulation changeValueManipulation) {
			writeLock.lock();
			try {
				w_noticeManipulation(changeValueManipulation);
			} finally {
				writeLock.unlock();
			}
		}

		private void w_noticeManipulation(ChangeValueManipulation changeValueManipulation) {
			EntityReference entityReference = null;
			GenericEntity entity = null;
			String propertyName = null;

			Owner owner = changeValueManipulation.getOwner();
			if (owner.ownerType() == OwnerType.LOCAL_ENTITY_PROPERTY) {
				LocalEntityProperty lep = (LocalEntityProperty) owner;
				entity = lep.getEntity();
				entityReference = referenceByEntity.get(entity);
				propertyName = lep.getPropertyName();

			} else {
				EntityProperty entityProperty = (EntityProperty) owner;
				entityReference = entityProperty.getReference();
				entity = entityByReference.get(entityReference);
				propertyName = entityProperty.getPropertyName();
			}

			if (GenericEntity.partition.equals(propertyName)) {
				// we need the new reference now that the partition was changed
				EntityReference changedReference = entity.reference();
				w_updateReferenceMapping(entityReference, changedReference);

				return;
			}

			if (GenericEntity.id.equals(propertyName)) {
				// this is an id change so we need to adapt the listener references

				// we need the new reference now that the id was changed
				EntityReference changedReference = entity.reference();
				w_updateReferenceMapping(entityReference, changedReference);

				// update id sequence
				if (changedReference instanceof PersistentEntityReference) {
					Object id = changedReference.getRefId();
					w_recognizeId(entity.entityType(), id);
				}
			}

			Object oldValue = extractOldValue(changeValueManipulation);
			populationManager.onChangeValue(entity, propertyName, oldValue, changeValueManipulation.getNewValue());
		}

		private Object extractOldValue(ChangeValueManipulation changeValueManipulation) {
			Manipulation inverseManipulation = changeValueManipulation.getInverseManipulation();
			if (inverseManipulation.manipulationType() == CHANGE_VALUE) {
				return ((ChangeValueManipulation) inverseManipulation).getNewValue();
			} else {
				return VdHolder.standardAiHolder;
			}
		}
	}

	private void registerEntityInternally(GenericEntity entity) {
		writeLock.lock();
		try {
			w_registerEntityInternally(entity);
		} finally {
			writeLock.unlock();
		}
	}

	private void w_registerEntityInternally(GenericEntity entity) {
		w_validateEntityReferenceIsUnique(entity);
		// validatePartitionIsCompatible(entity);

		EntityReference entityReference = entity.reference();
		w_updateReferenceMapping(entity, entityReference);

		populationManager.registerEntity(entity);

		w_recognizeId(entity);
	}

	// NOTE that this might be deleted once bugs are fixed
	private void w_validateEntityReferenceIsUnique(GenericEntity entity) {
		EntityReference ref = entity.reference();
		GenericEntity other = r_findEntity(ref);
		if (other != null) {
			if (other == entity)
				return;

			if (ref instanceof PersistentEntityReference)
				throw new IllegalStateException("Cannot register entity: " + entity + ". Other instance with same identity was already registered - "
						+ ref + ". Other instance: " + other);
			else
				throw new IllegalStateException("Cannot register entity: " + entity
						+ ". Somehow there already is a preliminary entity with the same identity. Other instance: " + other);
		}
	}

	@SuppressWarnings("unused")
	private void validatePartitionIsCompatible(GenericEntity entity) {
		if (!ignorePartitions && !partitions.contains(entity.getPartition()))
			throw new IllegalStateException(
					"Cannot register entity: " + entity + ". It's partition (" + entity.getPartition() + ") is not one of allowed: " + partitions);
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		gmSession.listeners().remove(manipulationListener);
	}

	// ###########################################################################################
	// ## . . . . . . . . . . . . . Unknown Entity Filter . . . . . . . . . . . . . . . . . . . ##
	// ###########################################################################################

	private boolean referencesUnknownEntity(Manipulation manipulation) {
		if (manipulation.manipulationType() == ManipulationType.COMPOUND)
			return false;

		UnkownPersistentEntityReferenceDetector detector = new UnkownPersistentEntityReferenceDetector();
		Manipulation.T.traverse(manipulation, matcher, detector);
		return detector.foundUnknown;
	}

	private static final StandardMatcher matcher = inverseManipulationMatcher();

	private static StandardMatcher inverseManipulationMatcher() {
		// @formatter:off
		TraversingCriterion tc = TC.create()
				.pattern()
					.typeCondition(TypeConditions.isAssignableTo(Manipulation.T))
					.property("inverseManipulation")
					.close()
				.done();
		// @formatter:on

		StandardMatcher matcher = new StandardMatcher();
		matcher.setCriterion(tc);
		return matcher;
	}

	private class UnkownPersistentEntityReferenceDetector extends EntityVisitor {
		public boolean foundUnknown;

		@Override
		protected void visitEntity(GenericEntity entity, EntityCriterion criterion, TraversingContext traversingContext) {
			if (!foundUnknown && entity instanceof PersistentEntityReference) {
				PersistentEntityReference entityReference = (PersistentEntityReference) entity;
				if (findEntity(entityReference) == null)
					foundUnknown = true;
			}
		}
	}

	// ###########################################################################################
	// ## . . . . . . . . . . . . . . Population Provider . . . . . . . . . . . . . . . . . . . ##
	// ###########################################################################################

	/** {@inheritDoc} */
	@Override
	public Collection<? extends GenericEntity> providePopulation(String typeSignature) {
		EntityType<?> et = typeReflection.getType(typeSignature);
		return getEntitiesPerType(et); // This is thread-safe
	}

	// ###########################################################################################
	// ## . . . . . . . . . . . . . . Index Range Lookup . . . . . . . . . . . . . . . . . . . .##
	// ###########################################################################################

	/** {@inheritDoc} */
	@Override
	public Collection<? extends GenericEntity> getIndexRange(String indexId, Object from, Boolean fromInclusive, Object to, Boolean toInclusive) {
		readLock.lock();
		try {
			return r_getIndexRange(indexId, from, fromInclusive, to, toInclusive);
		} finally {
			readLock.unlock();
		}
	}

	protected Collection<? extends GenericEntity> r_getIndexRange(String indexId, Object from, Boolean fromInclusive, Object to,
			Boolean toInclusive) {
		MetricIndex index = populationManager.getMetricIndex(indexId);

		return index.getRange(from, fromInclusive, to, toInclusive);
	}

	@Override
	public Collection<? extends GenericEntity> getFullRange(String indexId, boolean reverseOrder) {
		readLock.lock();
		try {
			return r_getFullRange(indexId, reverseOrder);
		} finally {
			readLock.unlock();
		}
	}

	private Collection<? extends GenericEntity> r_getFullRange(String indexId, boolean reverseOrder) {
		MetricIndex index = populationManager.getMetricIndex(indexId);

		return index.getFullRange(reverseOrder);
	}

	// ###########################################################################################
	// ## . . . . . . . . . . . . . . . Index Lookup . . . . . . . . . . . . . . . . . . . . . .##
	// ###########################################################################################

	/** {@inheritDoc} */
	@Override
	public GenericEntity getValueForIndex(String indexId, Object indexValue) {
		readLock.lock();
		try {
			return r_getValueForIndex(indexId, indexValue);
		} finally {
			readLock.unlock();
		}
	}

	protected GenericEntity r_getValueForIndex(String indexId, Object indexValue) {
		Iterator<? extends GenericEntity> it = r_getAllValuesForIndex(indexId, indexValue).iterator();
		return it.hasNext() ? it.next() : null;
	}

	/** {@inheritDoc} */
	@Override
	public Set<? extends GenericEntity> getAllValuesForIndices(String indexId, Collection<?> indexValues) {
		readLock.lock();
		try {
			return r_getAllValuesForIndices(indexId, indexValues);
		} finally {
			readLock.unlock();
		}
	}

	protected Set<? extends GenericEntity> r_getAllValuesForIndices(String indexId, Collection<?> indexValues) {
		Set<GenericEntity> result = newSet();

		for (Object value : indexValues)
			result.addAll(r_getAllValuesForIndex(indexId, value));

		return result;
	}

	/** {@inheritDoc} */
	@Override
	public Collection<? extends GenericEntity> getAllValuesForIndex(String indexId, Object indexValue) {
		readLock.lock();
		try {
			return r_getAllValuesForIndex(indexId, indexValue);
		} finally {
			readLock.unlock();
		}
	}

	protected Collection<? extends GenericEntity> r_getAllValuesForIndex(String indexId, Object indexValue) {
		LookupIndex index = populationManager.getLookupIndex(indexId);

		return index.getValues(indexValue);
	}

	// ###########################################################################################
	// ## . . . . . . . . . . . . . . Indexing Repository . . . . . . . . . . . . . . . . . . . ##
	// ###########################################################################################

	/**
	 * One has to be very careful with this one though - the retrieved object is not synchronized but it's content can be modified via other threads
	 * (when they are writing to the Smood). This method is only intended to be used from within context which already owns some lock, e.g. by the
	 * query planner triggered by one of the query methods.
	 *
	 * {@inheritDoc}
	 */
	@Override
	public RepositoryInfo provideRepositoryInfo() {
		return populationManager.repositoryInfo;
	}

	/** {@inheritDoc} */
	@Override
	public IndexInfo provideIndexInfo(String typeSignature, String propertyName) {
		readLock.lock();
		try {
			return r_provideIndexInfo(typeSignature, propertyName);
		} finally {
			readLock.unlock();
		}
	}

	protected IndexInfo r_provideIndexInfo(String typeSignature, String propertyName) {
		return populationManager.provideIndexInfo(typeSignature, propertyName);
	}

	/** {@inheritDoc} */
	@Override
	public GenericEntity resolveReference(EntityReference reference) {
		return findEntity(reference); // this is thread-safe
	}

	@Override
	public String defaultPartition() {
		return defaultPartition;
	}

	@Override
	public String getAccessId() {
		return accessId;
	}

	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

}
