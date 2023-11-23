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
package com.braintribe.model.access;

import static com.braintribe.model.generic.manipulation.util.EntityReferenceScanner.findEntityReferences;
import static com.braintribe.model.processing.manipulation.basic.tools.ManipulationRemotifier.remotify;
import static com.braintribe.model.processing.manipulation.basic.tools.ManipulationTools.asManipulation;
import static com.braintribe.model.processing.manipulation.basic.tools.ManipulationTools.combine;
import static com.braintribe.utils.lcd.CollectionTools2.acquireSet;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;
import static java.util.Collections.emptyMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.CollectionManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.query.eval.api.repo.Repository;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.query.support.IdentityManagedRepository;
import com.braintribe.model.processing.query.support.QueryEvaluator;
import com.braintribe.model.processing.query.support.QueryFunctionTools;
import com.braintribe.model.processing.session.api.managed.ManipulationMode;
import com.braintribe.model.processing.session.api.managed.ManipulationReport;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;
import com.braintribe.model.processing.session.impl.managed.StaticAccessModelAccessory;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.query.conditions.Condition;

/**
 * A base class that can be used to implement an {@link IncrementalAccess}. Subclasses can override following methods
 * according their needs:
 * <ul>
 * <li>{@link #loadPopulation()}</li>
 * <li>{@link #queryPopulation(String)}</li>
 * <li>{@link #queryPopulation(String, Condition)}</li>
 * <li>{@link #queryPopulation(String, Condition, Ordering)}</li>
 * <li>{@link #query(SelectQuery)}</li>
 * <li>{@link #queryEntities(EntityQuery)}</li>
 * <li>{@link #queryProperty(PropertyQuery)}</li>
 * <li>{@link #getEntity(EntityReference)}</li>
 * <li>{@link #loadEntitiesByType(Set, BasicPersistenceGmSession)}</li>
 * <li>{@link #save(AdapterManipulationReport)}</li>
 * </ul>
 * Depending on the capabilities of the accesse's underlying system / implementation, there are different levels of
 * granularity needed. Each default overload of load- and queryPopulation() provides some filtering and selection
 * capacities which have different levels of efficiency and performance. <br>
 * E.g. The plain loadPopulation() method is intended to return absolutely all entities and their referenced entities,
 * which are available via an access. This could be sensible if the accessed persistence store is very simple (e.g.
 * accessing a personal note or todo list system).<br>
 * For more complex accesses (File System, Database,...) it is usually preferable to translate Entity- (and/or Select-)
 * Queries to a format that the delegate system is able to understand eg.:
 * <ul>
 * <li>translating {@link EntityQuery} to SQL</li>
 * <li>overriding {@link #queryEntities(EntityQuery)} to use a filesystem's "dir" or "ls" command</li>
 * <li>parsing generic queries and calling specialized API functions</li>
 * </ul>
 */
public abstract class BasicAccessAdapter extends AbstractAccess {

	private final Logger logger = Logger.getLogger(this.getClass());

	private Supplier<GmMetaModel> metaModelProvider;
	private ModelAccessoryFactory modelAccessoryFactory;

	protected final BasicAccessAdapterRepository repository;
	protected final QueryEvaluator queryEvaluator;

	/**
	 * This is used inside the {@link IdentityManagedRepository}. That is a wrapper around our repository which makes
	 * sure all returned entities are identity managed (i.e. no two instances representing same entity are returned). If
	 * set to <tt>true</tt>, entities returned by the actual implementation will not be cloned, but attached to some
	 * session. Generally, use this iff providing entities which are not now (or later) attached to any session.
	 */
	protected boolean entitiesCanBeAdopted;

	/** @see #setIgnorePartitions(boolean) */
	private boolean ignorePartitions;

	public BasicAccessAdapter() {
		this.ignorePartitions = true;
		this.repository = new BasicAccessAdapterRepository(this);
		this.queryEvaluator = new QueryEvaluator(this, new RepoProvider(), QueryFunctionTools.functionExperts(null));
	}

	private class RepoProvider implements Supplier<Repository> {
		@Override
		public Repository get() throws RuntimeException {
			return newRepository();
		}
	}

	protected Repository newRepository() {
		BasicManagedGmSession session = new BasicManagedGmSession();
		session.setMetaModel(getMetaModel());

		return new IdentityManagedRepository(repository, entitiesCanBeAdopted, session);
	}

	/**
	 * @deprecated see {@link AbstractAccess#defaultPartition()}
	 */
	@Override
	@Deprecated
	protected String defaultPartition() {
		return getAccessId();
	}

	@Configurable
	@Required
	public void setMetaModelProvider(Supplier<GmMetaModel> metaModelProvider) {
		this.metaModelProvider = metaModelProvider;
	}

	@Configurable
	public void setModelAccessoryFactory(ModelAccessoryFactory modelAccessoryFactory) {
		this.modelAccessoryFactory = modelAccessoryFactory;
	}

	/**
	 * This is similar to Smood's {@link Smood#setIgnorePartitions(boolean) ignorePartitions} property. If set to true
	 * (default), we do not assume that the underlying system is storing partitions, and when returning entities, we set
	 * the value to the {@link #defaultPartition()}. Also, when comparing entities, the partition property is ignored.
	 * 
	 * VERY IMPORTANT NOTE: If set to <tt>false</tt>, the underlying repository cannot be marked that it assumes id is
	 * indexed ( {@link BasicAccessAdapterRepository#setAssumeIdIsIndexed(boolean)} cannot be called with
	 * <tt>true</tt>). The reason is that this setting enables the default id index which resolves entities by id by
	 * delegating to {@link #getEntity(EntityReference)} method. However, when using multiple partitions, this method
	 * would not work, as we do not know the correct partition (in other case we take default partition, of course).
	 */
	public void setIgnorePartitions(boolean ignorePartitions) {
		if (!ignorePartitions && repository.getAssumeIdIsIndexed()) {
			throw new IllegalArgumentException("Cannot set 'ignorePartitions' to false when assuming id property is indexed."
					+ " Note that the default for assuming id being indexed is false, so check your code where you are setting it to true and remove it.");
		}

		this.ignorePartitions = ignorePartitions;
	}

	protected boolean getIgnorePartitions() {
		return ignorePartitions;
	}

	/**
	 * Note, that sub classes overriding this method has to implement the full feature set of {@link SelectQuery} on
	 * their own. Usually that's not needed and sub classes decide to implement one of the "queryPopulation" methods.
	 * 
	 * @see com.braintribe.model.access.IncrementalAccess#query(SelectQuery)
	 */
	@Override
	public SelectQueryResult query(SelectQuery query) throws ModelAccessException {
		// This is a slight adjustment of SmoodAccess.query(SelectQuery) implementation
		try {
			SelectQueryResult result = queryEvaluator.query(query);

			List<Object> clonedResults = cloneSelectQueryResult(result.getResults(), query, createStandardCloningContext(),
					StrategyOnCriterionMatch.partialize);
			result.setResults(clonedResults);

			return result;
		} catch (Exception e) {
			throw new ModelAccessException("Error while performing SelectQuery.", e);
		}
	}

	/**
	 * @see com.braintribe.model.access.IncrementalAccess#queryEntities(com.braintribe.model.query.EntityQuery)
	 */
	@Override
	public EntityQueryResult queryEntities(EntityQuery query) throws ModelAccessException {
		return queryEvaluator.queryEntities(query);
	}

	/**
	 * @see com.braintribe.model.access.IncrementalAccess#queryProperty(com.braintribe.model.query.PropertyQuery)
	 */
	@Override
	public PropertyQueryResult queryProperty(PropertyQuery request) throws ModelAccessException {
		return queryEvaluator.queryProperty(request);
	}

	/**
	 * @see com.braintribe.model.access.ModelAccess#getMetaModel()
	 */
	@Override
	public GmMetaModel getMetaModel() {
		try {
			return metaModelProvider.get();

		} catch (Exception e) {
			throw new GenericModelException("Error while fetching meta model.", e);
		}
	}

	private volatile ModelAccessory modelAccessory;

	public ModelAccessory getModelAccessory() {
		if (modelAccessory == null) {
			ensureModelAccessory();
		}

		return modelAccessory;
	}

	private final Object modelAccessoryLock = new Object();

	private void ensureModelAccessory() {
		synchronized (modelAccessoryLock) {
			if (modelAccessory == null)
				modelAccessory = buildModelAccessory();
		}
	}

	private ModelAccessory buildModelAccessory() {
		if (modelAccessoryFactory != null)
			return modelAccessoryFactory.getForAccess(getAccessId());
		else
			return new StaticAccessModelAccessory(getMetaModel(), getAccessId());
	}

	/**
	 * @see com.braintribe.model.access.IncrementalAccess#applyManipulation(com.braintribe.model.accessapi.ManipulationRequest)
	 */
	@Override
	public ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest) throws ModelAccessException {
		try {

			BasicPersistenceGmSession session = new BasicPersistenceGmSession();
			session.setIncrementalAccess(this);

			AdapterManipulationReport report = applyLocal(manipulationRequest, session);
			NestedTransaction transaction = session.getTransaction().beginNestedTransaction();

			// store entities to persistence layer and update id properties
			save(report);

			List<Manipulation> inducedManipulations = transaction.getManipulationsDone();

			Map<GenericEntity, EntityReference> instantations = report.getInstantiations();
			Map<GenericEntity, EntityReference> initialReferences = newMap();
			List<Manipulation> partitionAssignments = newList();
			for (Map.Entry<GenericEntity, EntityReference> entry : instantations.entrySet()) {

				GenericEntity newEntity = entry.getKey();
				if (newEntity.getId() == null)
					throw new ModelAccessException("No id assigned on entity: " + newEntity + " (Neither by caller nor internal implementation).");

				initialReferences.put(newEntity, entry.getValue());

				if (newEntity.getPartition() == null)
					partitionAssignments.add(createPartitionAssignmentManipulation(newEntity));
			}

			Manipulation inducedManipulation = asManipulation(inducedManipulations);
			Manipulation remotifiedInducedManipulation = null;
			if (inducedManipulation != null) {
				remotifiedInducedManipulation = remotify(inducedManipulation, initialReferences);
			}

			// adding partitionAssignments to the end of induced manipulations.
			Manipulation partitionAssignmentManipulation = asManipulation(partitionAssignments);
			Manipulation newInducedManipulation = combine(remotifiedInducedManipulation, partitionAssignmentManipulation);

			ManipulationResponse response = ManipulationResponse.T.create();
			response.setInducedManipulation(newInducedManipulation);

			return response;

		} catch (Exception e) {
			throw new ModelAccessException("Error while applying manipulation.", e);
		}
	}

	/**
	 * Store the changes available in the given context to the persistence layer. Ensure that id's created for new
	 * instances are assigned to the id property of the instance.
	 * 
	 * @param report
	 *            info about desired changes
	 * @throws ModelAccessException
	 *             in case something goes wrong
	 */
	protected void save(AdapterManipulationReport report) throws ModelAccessException {
		logger.info("Save not implemented in BasicAccessAdapter. Override this method to persist the changes.");
	}

	/**
	 * <h2>General information</h2>
	 * 
	 * This is a method which is invoked by the {@link BasicAccessAdapterRepository} when retrieving entities. This
	 * method is the top-most method of the following cascade:
	 * <ul>
	 * <li>{@linkplain #queryPopulation(String, Condition, Ordering)}</li>
	 * <li>{@link #queryPopulation(String, Condition)}</li>
	 * <li>{@link #queryPopulation(String)}</li>
	 * <li>{@link #loadPopulation()}</li>
	 * </ul>
	 * 
	 * The idea here is, that the implemented access overrides one of these methods depending on what capabilities the
	 * actual repository (source of the data) has. So if all that repository can do is provide all the entities, the
	 * last method would be overridden, but if the repository also supports at least some conditions (and
	 * type-specification), overriding the second method would be better for performance reasons.
	 * 
	 * <h2>Default implementation</h2>
	 * 
	 * The default implementation of each method simply delegates to the next-in-line method, but might apply some
	 * minimal changes in order to stay conform with the method contract. This method simply delegates to
	 * {@link #queryPopulation(String, Condition)}, and applies sorting according to given <tt>ordering</tt> on the
	 * result from the delegate invocation.
	 * 
	 * @param typeSignature
	 *            (mandatory) specifies the type for returned entities. This method guarantees, that only entities
	 *            assignable to given type are returned
	 * @param condition
	 *            (optional) {@link Condition} to filter a given population. This method guarantees, that every single
	 *            entity of the population, for which given condition holds, will be returned in the result.
	 * @param ordering
	 *            (optional) This method guarantees that entities are sorted according to this ordering
	 */
	protected Iterable<GenericEntity> queryPopulation(String typeSignature, Condition condition, Ordering ordering) throws ModelAccessException {

		Iterable<GenericEntity> population = queryPopulation(typeSignature, condition);

		if (ordering == null) {
			return population;
		} else {
			throw new UnsupportedOperationException("Method 'BasicAccessAdapter.queryPopulation' does not support ordering yet!");
		}
	}

	/**
	 * The default implementation delegates to {@link #queryPopulation(String)} without apply any condition at all.
	 * 
	 * @param typeSignature
	 *            (mandatory) specifies the type for returned entities. This method guarantees, that only entities
	 *            assignable to given type are returned
	 * @param condition
	 *            (optional) {@link Condition} to filter a given population. This method guarantees, that every single
	 *            entity of the population, for which given condition holds, will be returned in the result.
	 * 
	 * @see #queryPopulation(String, Condition, Ordering)
	 */
	protected Iterable<GenericEntity> queryPopulation(String typeSignature, Condition condition) throws ModelAccessException {
		return queryPopulation(typeSignature);
	}

	/**
	 * The default implementation delegates to {@link #loadPopulation()} and simply filters the entities by given type
	 * signature.
	 * 
	 * @param typeSignature
	 *            (mandatory) specifies the type for returned entities. This method guarantees, that only entities
	 *            assignable to given type are returned
	 * 
	 * @see #queryPopulation(String, Condition, Ordering)
	 */
	protected Iterable<GenericEntity> queryPopulation(String typeSignature) throws ModelAccessException {
		return new TypeFilteringIterable(typeSignature, loadPopulation());
	}

	/**
	 * This method is the minimum method that needs to be overridden by sub classes of this class in order support query
	 * functionality. The result is expected as a flat collection of all available entities that exist in the
	 * persistence layer. Typically this method is only used for connecting repositories with limited (or no) query
	 * capabilities and a limited amount of expected data.
	 * 
	 * @throws ModelAccessException
	 *             in case something goes wrong
	 */
	protected Collection<GenericEntity> loadPopulation() throws ModelAccessException {
		logger.info("Loading empty population in BasicAccessAdapter. Override this method to provide actual population. ");
		return new ArrayList<>();
	}

	/**
	 * Resolves given {@link EntityReference}. The order in the returned list corresponds to the order of iteration of
	 * given reference collection
	 */
	protected List<GenericEntity> getEntities(Collection<? extends EntityReference> entityReferences) throws ModelAccessException {
		return entityReferences.stream() //
				.map(this::getEntity) //
				.filter(e -> e != null) //
				.collect(Collectors.toList());
	}

	/** Resolves given {@link EntityReference} */
	protected GenericEntity getEntity(EntityReference entityReference) throws ModelAccessException {
		EntityType<?> entityType = entityReference.valueType();
		String partition = getPartitionOrDefault(entityReference);

		// @formatter:off
		SelectQuery referenceQuery = new SelectQueryBuilder()
					.select("e")
					.from(entityType, "e").where()
					.conjunction()
						.property("e", GenericEntity.id).eq(entityReference.getRefId())
						.property("e", GenericEntity.partition).eq(partition)
					.close()
				.done();
		// @formatter:on

		SelectQueryResult result = queryEvaluator.query(referenceQuery);

		List<?> entities = result.getResults();

		switch (entities.size()) {
			case 1:
				return (GenericEntity) entities.get(0);
			case 0:
				return null;
			default:
				throw new ModelAccessException("More then one instance found for reference: " + entityReference);
		}
	}

	private String getPartitionOrDefault(EntityReference entityReference) {
		String p = entityReference.getRefPartition();
		if (p == null || p.equals(EntityReference.ANY_PARTITION))
			return defaultPartition();
		else
			return p;
	}

	/**
	 * Sub classes can override this method in order to optimize bulk loading of existing entities during save. The
	 * default implementation loads all requested instances by performing a query an id in() comparison for each
	 * distinct EntityType.
	 */
	protected void preLoadEntitiesByType(Set<EntityReference> refs, BasicPersistenceGmSession session) throws Exception {
		loadEntitiesByType(refs, session);
	}

	/**
	 * Deprecated on 28.11.2022 - v 2.0.3
	 * 
	 * @deprecated override {@link #preLoadEntitiesByType(Set, BasicPersistenceGmSession)} instead. The only difference is you don't need to return
	 *             anything. The result wasn't used anyway.
	 */
	@Deprecated
	protected Map<EntityReference, GenericEntity> loadEntitiesByType(Set<EntityReference> refs, BasicPersistenceGmSession session) throws Exception {
		Map<EntityType<?>, Set<Object>> idsByEntityType = extractIdsByEntityType(refs);

		for (Map.Entry<EntityType<?>, Set<Object>> entityIdEntry : idsByEntityType.entrySet()) {
			EntityType<?> entityType = entityIdEntry.getKey();
			Set<Object> ids = entityIdEntry.getValue();

			EntityQuery query = EntityQueryBuilder.from(entityType).where().property(GenericEntity.id).in(ids).tc().negation().joker().done();

			// just need to query against the session to load the entities into it.
			session.query().entities(query).list();

		}

		return emptyMap();
	}

	protected AdapterManipulationReport applyLocal(ManipulationRequest manipulationRequest, BasicPersistenceGmSession session) throws Exception {
		Set<EntityReference> references = findEntityReferences(manipulationRequest.getManipulation());
		loadEntitiesByType(references, session);
		
		/*
		references
			.stream()
			.filter(r -> r.getRefPartition() == null)
			.forEach(r -> r.setRefPartition(getPartitionOrDefault(r)));
		*/
		AdapterManipulationReport report = new AdapterManipulationReport();
		ManipulationListener manipulationListener = newApplyLocalListener(report);

		session.listeners().add(manipulationListener);

		try {
			ManipulationReport manipulationReport = session.manipulate().mode(ManipulationMode.REMOTE).apply(manipulationRequest.getManipulation());

			// make sure we have a mapping from GenericEntity to the
			for (Map.Entry<PreliminaryEntityReference, GenericEntity> instantiationEntry : manipulationReport.getInstantiations().entrySet()) {
				GenericEntity entity = instantiationEntry.getValue();
				report.getInstantiations().computeIfAbsent(entity, e -> instantiationEntry.getKey());
			}

			Set<LocalEntityProperty> entityProperties = session.getTransaction().getManipulatedProperties();
			for (LocalEntityProperty entityProperty : entityProperties) {
				GenericEntity entity = entityProperty.getEntity();
				if (!report.getCreatedEntities().contains(entity)) {
					report.getUpdatedEntities().add(entity);
				}

				Set<Property> touchedProperties = acquireSet(report.getTouchedPropertiesOfEntities(), entity);
				touchedProperties.add(entityProperty.property());
			}

			return report;

		} finally {
			session.listeners().remove(manipulationListener);
		}
	}

	private ManipulationListener newApplyLocalListener(AdapterManipulationReport report) {
		return manipulation -> {
			switch (manipulation.manipulationType()) {
				case INSTANTIATION:
					InstantiationManipulation instantationManipulation = (InstantiationManipulation) manipulation;
					report.getCreatedEntities().add(instantationManipulation.getEntity());
					break;
				case DELETE:
					DeleteManipulation deleteManipulation = (DeleteManipulation) manipulation;
					report.getDeletedEntities().add(deleteManipulation.getEntity());
					break;
				case CHANGE_VALUE:
					ChangeValueManipulation changeManipulation = (ChangeValueManipulation) manipulation;
					Owner owner = changeManipulation.getOwner();
					String propertyName = owner.getPropertyName();

					if (owner instanceof LocalEntityProperty) {
						LocalEntityProperty ep = (LocalEntityProperty) owner;
						GenericEntity entity = ep.getEntity();

						/* Both cases only set the reference in case an id is assigned - in such case the reference will
						 * also be valid in the manipulation response. AdapterManipulationReport.getInstantiations() */
						if (GenericEntity.id.equals(propertyName)) {
							report.getInstantiations().put(entity, entity.reference());
						} else if (GenericEntity.partition.equals(propertyName)) {
							report.getInstantiations().computeIfPresent(entity, (e, oldRef) -> e.reference());
						}
					}
					
					break;
				case ADD:
				case REMOVE:
				case CLEAR_COLLECTION:
					calculateCollectionChanges((CollectionManipulation) manipulation, report);
					break;
				case COMPOUND:
					calculateCollectionChangesFromCompound((CompoundManipulation) manipulation, report);
					break;
				default:
					break;
			}
		};
	}

	private void calculateCollectionChangesFromCompound(CompoundManipulation cMan, AdapterManipulationReport report) {
		for (Manipulation manipulation : cMan.getCompoundManipulationList()) {

			if (manipulation instanceof CollectionManipulation)
				calculateCollectionChanges((CollectionManipulation) manipulation, report);

			else if (manipulation instanceof CompoundManipulation)
				calculateCollectionChangesFromCompound((CompoundManipulation) manipulation, report);
		}
	}

	private void calculateCollectionChanges(CollectionManipulation cMan, AdapterManipulationReport report) {
		Owner owner = cMan.getOwner();
		if (!(owner instanceof LocalEntityProperty))
			throw new RuntimeException("getOwner() of the ClearCollectionManipulation is not instanceof LocalEntityProperty");

		String propertyName = owner.getPropertyName();
		GenericEntity entity = ((LocalEntityProperty) owner).getEntity();

		EntityType<GenericEntity> entityType = entity.entityType();
		Property property = entityType.getProperty(propertyName);
		GenericModelType propertyType = property.getType();
		TypeCode typeCode = propertyType.getTypeCode();

		if (!propertyType.isCollection())
			return; // for now working with sets, lists, maps only

		if (cMan instanceof ClearCollectionManipulation) {
			ClearCollectionManipulation clearMan = (ClearCollectionManipulation) cMan;
			AddManipulation addMan = (AddManipulation) clearMan.getInverseManipulation();
			for (Entry<Object, Object> setItem : addMan.getItemsToAdd().entrySet())
				report.addRemovedElementsForProperty(entity, property, setItem.getValue(), setItem.getKey(), typeCode);

		} else if (cMan instanceof RemoveManipulation) {
			RemoveManipulation remMan = (RemoveManipulation) cMan;
			for (Entry<Object, Object> setItem : remMan.getItemsToRemove().entrySet())
				report.addRemovedElementsForProperty(entity, property, setItem.getValue(), setItem.getKey(), typeCode);

		} else if (cMan instanceof AddManipulation) {
			AddManipulation addMan = (AddManipulation) cMan;
			for (Entry<Object, Object> setItem : addMan.getItemsToAdd().entrySet())
				report.addAddedElementsForProperty(entity, property, setItem.getValue(), setItem.getKey(), typeCode);

		} else {
			logger.warn("Unexpected CollectionManipulation subtype [ " + cMan.getClass().getName() + " ]");
		}
	}

	public interface CollectionEntry {
		void setValue(Object value);
		Object getValue();
	}

	public class ListEntry implements CollectionEntry {
		private Object value;
		private int key;

		@Override
		public void setValue(Object value) {
			this.value = value;
		}

		@Override
		public Object getValue() {
			return this.value;
		}

		public void setKey(int key) {
			this.key = key;
		}

		public int getKey() {
			return this.key;
		}

		public ListEntry(int key, Object value) {
			this.key = key;
			this.value = value;
		}
	}

	public class MapEntry implements CollectionEntry {
		private Object value;
		private Object key;

		@Override
		public void setValue(Object value) {
			this.value = value;
		}

		@Override
		public Object getValue() {
			return this.value;
		}

		public void setKey(Object key) {
			this.key = key;
		}

		public Object getKey() {
			return this.key;
		}

		public MapEntry(Object key, Object value) {
			this.key = key;
			this.value = value;
		}
	}

	/**
	 * The manipulation report holds informations about the entities (and their properties) that should be updated,
	 * deleted or created during applyManipulation().
	 */
	public class AdapterManipulationReport {

		final Set<GenericEntity> createdEntities = newSet();
		final Set<GenericEntity> updatedEntities = newSet();
		final Set<GenericEntity> deletedEntities = newSet();

		final Map<GenericEntity, Set<Property>> touchedPropertiesOfEntities = newMap();

		final Map<GenericEntity, Map<Property, Set<Object>>> removedElementsForPropertyOfTypeSet = newMap();
		final Map<GenericEntity, Map<Property, Set<Object>>> addedElementsForPropertyOfTypeSet = newMap();

		final Map<GenericEntity, Map<Property, List<ListEntry>>> removedElementsForPropertyOfTypeList = newMap();
		final Map<GenericEntity, Map<Property, List<ListEntry>>> addedElementsForPropertyOfTypeList = newMap();

		final Map<GenericEntity, Map<Property, List<MapEntry>>> removedElementsForPropertyOfTypeMap = newMap();
		final Map<GenericEntity, Map<Property, List<MapEntry>>> addedElementsForPropertyOfTypeMap = newMap();

		final Map<GenericEntity, EntityReference> instantiations = newMap();

		public Set<GenericEntity> getCreatedEntities() {
			return createdEntities;
		}

		/**
		 * This is a map used in the applyLocal method. It maps the entities from the "applyLocal" session to the
		 * references valid for the manipulation response. This means that for entities where id assignment was not part
		 * of the manipulation request, the preliminary reference is different than what entity.reference() would return
		 * - cause the original request used different preliminary id.
		 */
		public Map<GenericEntity, EntityReference> getInstantiations() {
			return instantiations;
		}

		public void addRemovedElementsForProperty(GenericEntity entity, Property property, Object item, Object key, TypeCode typeCode) {
			switch (typeCode) {
				case setType:
					addRemovedElementsForPropertyOfTypeSet(entity, property, item);
					break;
				case listType:
					addRemovedElementsForPropertyOfTypeList(entity, property, item, (Integer) key);
					break;
				case mapType:
					addRemovedElementsForPropertyOfTypeMap(entity, property, item, key);
					break;
				default:
					break;
			}
		}

		public void addAddedElementsForProperty(GenericEntity entity, Property property, Object item, Object key, TypeCode typeCode) {
			switch (typeCode) {
				case setType:
					addAddedElementsForPropertyOfTypeSet(entity, property, item);
					break;
				case listType:
					addAddedElementsForPropertyOfTypeList(entity, property, item, (Integer) key);
					break;
				case mapType:
					addAddedElementsForPropertyOfTypeMap(entity, property, item, key);
					break;
				default:
					break;
			}
		}

		public void addRemovedElementsForPropertyOfTypeSet(GenericEntity entity, Property property, Object setItem) {
			boolean wasAddedBefore = itemToSetTrackingStructure(addedElementsForPropertyOfTypeSet, entity, property, setItem, false);
			if (!wasAddedBefore) {
				itemToSetTrackingStructure(removedElementsForPropertyOfTypeSet, entity, property, setItem, true);
			}
		}

		public void addAddedElementsForPropertyOfTypeSet(GenericEntity entity, Property property, Object setItem) {
			boolean wasRemovedBefore = itemToSetTrackingStructure(removedElementsForPropertyOfTypeSet, entity, property, setItem, false);
			if (!wasRemovedBefore) {
				itemToSetTrackingStructure(addedElementsForPropertyOfTypeSet, entity, property, setItem, true);
			}
		}

		private boolean itemToSetTrackingStructure(Map<GenericEntity, Map<Property, Set<Object>>> trackingStructure, GenericEntity entity,
				Property property, Object item, boolean add) {

			Map<Property, Set<Object>> properties = trackingStructure.get(entity);
			if (properties == null) {
				properties = newMap();
				trackingStructure.put(entity, properties);
			}

			Set<Object> itemsSet = properties.get(property);
			if (itemsSet == null) {
				itemsSet = newSet();
				properties.put(property, itemsSet);
			}

			if (add) {
				return itemsSet.add(item);
			} else {
				return itemsSet.remove(item);
			}
		}

		public void addRemovedElementsForPropertyOfTypeList(GenericEntity entity, Property property, Object listItem, int key) {
			boolean wasAddedBefore = itemToListTrackingStructure(addedElementsForPropertyOfTypeList, entity, property, listItem, key, false);
			if (!wasAddedBefore) {
				itemToListTrackingStructure(removedElementsForPropertyOfTypeList, entity, property, listItem, key, true);
			}
		}

		public void addAddedElementsForPropertyOfTypeList(GenericEntity entity, Property property, Object listItem, int key) {
			boolean wasRemovedBefore = itemToListTrackingStructure(removedElementsForPropertyOfTypeList, entity, property, listItem, key, false);
			if (!wasRemovedBefore) {
				itemToListTrackingStructure(addedElementsForPropertyOfTypeList, entity, property, listItem, key, true);
			}
		}

		private boolean itemToListTrackingStructure(Map<GenericEntity, Map<Property, List<ListEntry>>> trackingStructure, GenericEntity entity,
				Property property, Object item, int key, boolean add) {

			Map<Property, List<ListEntry>> properties = trackingStructure.get(entity);
			if (properties == null) {
				properties = newMap();
				trackingStructure.put(entity, properties);
			}

			List<ListEntry> itemsSet = properties.get(property);
			if (itemsSet == null) {
				itemsSet = newList();
				properties.put(property, itemsSet);
			}

			ListEntry entry = new ListEntry(key, item);
			if (add) {
				return itemsSet.add(entry);
			} else {
				return itemsSet.remove(entry);
			}
		}

		public void addRemovedElementsForPropertyOfTypeMap(GenericEntity entity, Property property, Object mapItem, Object key) {
			boolean wasAddedBefore = itemToMapTrackingStructure(addedElementsForPropertyOfTypeMap, entity, property, mapItem, key, false);
			if (!wasAddedBefore) {
				itemToMapTrackingStructure(removedElementsForPropertyOfTypeMap, entity, property, mapItem, key, true);
			}
		}

		public void addAddedElementsForPropertyOfTypeMap(GenericEntity entity, Property property, Object mapItem, Object key) {
			boolean wasRemovedBefore = itemToMapTrackingStructure(removedElementsForPropertyOfTypeMap, entity, property, mapItem, key, false);
			if (!wasRemovedBefore) {
				itemToMapTrackingStructure(addedElementsForPropertyOfTypeMap, entity, property, mapItem, key, true);
			}
		}

		private boolean itemToMapTrackingStructure(Map<GenericEntity, Map<Property, List<MapEntry>>> trackingStructure, GenericEntity entity,
				Property property, Object item, Object key, boolean add) {

			Map<Property, List<MapEntry>> properties = trackingStructure.get(entity);
			if (properties == null) {
				properties = newMap();
				trackingStructure.put(entity, properties);
			}

			List<MapEntry> itemsSet = properties.get(property);
			if (itemsSet == null) {
				itemsSet = newList();
				properties.put(property, itemsSet);
			}

			MapEntry entry = new MapEntry(key, item);

			if (add) {
				return itemsSet.add(entry);
			} else {
				return itemsSet.remove(entry);
			}
		}

		public Set<GenericEntity> getUpdatedEntities() {
			return updatedEntities;
		}

		public Set<GenericEntity> getDeletedEntities() {
			return deletedEntities;
		}

		public Map<GenericEntity, Set<Property>> getTouchedPropertiesOfEntities() {
			return touchedPropertiesOfEntities;
		}

		public Set<Object> getAddedElementsForPropertyOfTypeSet(GenericEntity entity, Property property) {
			Set<Object> set = null;
			Map<Property, Set<Object>> map = addedElementsForPropertyOfTypeSet.get(entity);
			if (map != null) {
				set = map.get(property);
			}

			return nullSafe(set);
		}

		public Set<Object> getRemovedElementsForPropertyOfTypeSet(GenericEntity entity, Property property) {
			Set<Object> set = null;
			Map<Property, Set<Object>> map = removedElementsForPropertyOfTypeSet.get(entity);
			if (map != null) {
				set = map.get(property);
			}

			return nullSafe(set);
		}

		public List<ListEntry> getAddedElementsForPropertyOfTypeList(GenericEntity entity, Property property) {
			List<ListEntry> result = null;
			Map<Property, List<ListEntry>> map = addedElementsForPropertyOfTypeList.get(entity);
			if (map != null) {
				result = map.get(property);
			}

			return nullSafe(result);
		}

		public List<ListEntry> getRemovedElementsForPropertyOfTypeList(GenericEntity entity, Property property) {
			List<ListEntry> result = null;
			Map<Property, List<ListEntry>> map = removedElementsForPropertyOfTypeList.get(entity);
			if (map != null) {
				result = map.get(property);
			}

			return nullSafe(result);
		}

		public List<MapEntry> getAddedElementsForPropertyOfTypeMap(GenericEntity entity, Property property) {
			List<MapEntry> result = null;
			Map<Property, List<MapEntry>> map = addedElementsForPropertyOfTypeMap.get(entity);
			if (map != null) {
				result = map.get(property);
			}

			return nullSafe(result);
		}

		public List<MapEntry> getRemovedElementsForPropertyOfTypeMap(GenericEntity entity, Property property) {
			List<MapEntry> result = null;
			Map<Property, List<MapEntry>> map = removedElementsForPropertyOfTypeMap.get(entity);
			if (map != null) {
				result = map.get(property);
			}

			return nullSafe(result);
		}
	}

	/**
	 * Implementations of {@link BasicAccessAdapter} which support fulltext search MUST return <tt>true</tt>, so that
	 * the general implementation of the default {@link QueryEvaluator} is not used.
	 */
	public boolean supportsFulltextSearch() {
		return false;
	}

}
