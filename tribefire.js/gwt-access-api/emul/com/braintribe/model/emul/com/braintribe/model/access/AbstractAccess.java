// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.model.access;

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.changeValue;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.compound;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.entityProperty;
import static com.braintribe.model.generic.typecondition.TypeConditions.isAssignableTo;
import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.model.generic.typecondition.TypeConditions.orTc;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.braintribe.model.accessapi.ReferencesCandidate;
import com.braintribe.model.accessapi.ReferencesCandidates;
import com.braintribe.model.accessapi.ReferencesRequest;
import com.braintribe.model.accessapi.ReferencesResponse;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.util.EntityReferenceScanner;
import com.braintribe.model.generic.pr.criteria.BasicCriterion;
import com.braintribe.model.generic.pr.criteria.CriterionType;
import com.braintribe.model.generic.pr.criteria.PropertyCriterion;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.Matcher;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.EntityReferenceType;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistry;
import com.braintribe.model.processing.core.expert.impl.ConfigurableGmExpertRegistry;
import com.braintribe.model.processing.findrefs.ReferenceFinder;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.record.ListRecord;
import com.braintribe.model.security.acl.AclTcs;

/**
 * 
 * @author dirk.scheffler
 * 
 */
public abstract class AbstractAccess extends AbstractCustomPersistenceRequestProcessingAccess {

	protected static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	protected Map<Class<? extends GenericEntity>, TraversingCriterion> defaultTraversingCriteria;
	private String modelName;
	private GmExpertRegistry expertRegistry;
	private final ReferenceFinder referenceFinder = new ReferenceFinder(this);
	private String accessId;
	protected Set<String> partitions;
	/** If partition of an entity is null, this value is used instead in the query result. */
	protected String defaultPartition;

	public void setDefaultTraversingCriteria(Map<Class<? extends GenericEntity>, TraversingCriterion> defaultTraversingCriteria) {
		this.defaultTraversingCriteria = defaultTraversingCriteria;
	}

	public Map<Class<? extends GenericEntity>, TraversingCriterion> getDefaultTraversingCriteria() {
		if (defaultTraversingCriteria == null)
			loadDefaultTraversingCriteria();

		return defaultTraversingCriteria;
	}

	private synchronized void loadDefaultTraversingCriteria() {
		if (defaultTraversingCriteria == null)
			defaultTraversingCriteria = asMap(GenericEntity.class, createDefaultTraversionCriterion());
	}

	protected TraversingCriterion getDefaultTraversingCriterion(Class<? extends GenericEntity> entityClass) {
		Map<Class<? extends GenericEntity>, TraversingCriterion> criteria = getDefaultTraversingCriteria();
		return criteria.get(entityClass);
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getModelName() {
		return modelName;
	}

	/**
	 * @param expertRegistry
	 *            the expertRegistry to set
	 */
	public void setExpertRegistry(GmExpertRegistry expertRegistry) {
		this.expertRegistry = expertRegistry;
	}

	public GmExpertRegistry getExpertRegistry() {
		if (expertRegistry == null)
			expertRegistry = new ConfigurableGmExpertRegistry();

		return expertRegistry;
	}

	protected List<GenericEntity> cloneEntityQueryResult(List<GenericEntity> entities, Matcher matcher, StandardCloningContext cloningContext) {
		return cloneEntityQueryResult(entities, matcher, cloningContext, StrategyOnCriterionMatch.partialize);
	}

	protected List<GenericEntity> cloneEntityQueryResult(List<GenericEntity> entities, Matcher matcher, StandardCloningContext cloningContext,
			StrategyOnCriterionMatch strategyOnCriterionMatch) {

		CollectionType collectionType = typeReflection.getCollectionType(List.class, GenericEntity.T);

		cloningContext.setMatcher(matcher);
		List<GenericEntity> cuttedEntities = (List<GenericEntity>) collectionType.clone(cloningContext, entities, strategyOnCriterionMatch);
		return cuttedEntities;
	}

	protected List<GenericEntity> cloneEntityQueryResult(List<GenericEntity> entities, EntityQuery query) {
		return cloneEntityQueryResult(entities, getMatcher(query));
	}

	protected List<GenericEntity> cloneEntityQueryResult(List<GenericEntity> entities, Matcher matcher) {
		return cloneEntityQueryResult(entities, matcher, createStandardCloningContext());
	}

	protected EntityQueryResult createEntityQueryResult(EntityQuery query, List<GenericEntity> resultingEntities, boolean hasMore) throws Exception {
		return createEntityQueryResult(query, resultingEntities, hasMore, createStandardCloningContext());
	}

	protected EntityQueryResult createEntityQueryResult(EntityQuery query, List<GenericEntity> resultingEntities, boolean hasMore,
			StandardCloningContext cloningContext) throws Exception {

		Matcher matcher = getMatcher(query);
		List<GenericEntity> cuttedEntities = cloneEntityQueryResult(resultingEntities, matcher, cloningContext);

		EntityQueryResult result = EntityQueryResult.T.create();
		result.setEntities(cuttedEntities);
		result.setHasMore(hasMore);
		return result;
	}

	protected StandardCloningContext createStandardCloningContext() {
		return new QueryResultCloningContext();
	}

	public static abstract class AbstractQueryResultCloningContext extends StandardCloningContext {

		protected abstract String defaultPartition();

		@Override
		public Object postProcessCloneValue(GenericModelType propertyType, Object clonedValue) {
			BasicCriterion bc = getTraversingStack().peek();
			if (bc.criterionType() == CriterionType.PROPERTY && clonedValue == null
					&& GenericEntity.partition.equals(((PropertyCriterion) bc).getPropertyName()))
				return defaultPartition();
			else
				return super.postProcessCloneValue(propertyType, clonedValue);
		}
	}

	protected class QueryResultCloningContext extends AbstractQueryResultCloningContext {
		@Override
		protected String defaultPartition() {
			return AbstractAccess.this.defaultPartition;
		}
	}

	protected List<Object> cloneSelectQueryResult(List<?> results, SelectQuery query, StandardCloningContext cloningContext) {
		return cloneSelectQueryResult(results, query, cloningContext, StrategyOnCriterionMatch.partialize);
	}

	protected List<Object> cloneSelectQueryResult(List<?> results, SelectQuery query, StandardCloningContext cloningContext,
			StrategyOnCriterionMatch strategyOnCriterionMatch) {

		List<Object> clonedResults = newList(results.size());

		// clone and thereby cut results according to traversing criteria

		for (Object row : results) {
			if (row == null) {
				clonedResults.add(null);

			} else if (row.getClass().isArray()) {
				Object values[] = (Object[]) row;
				List<Object> resultRow = new ArrayList<>(values.length);
				for (Object value : values) {
					Matcher matcher = getMatcher(value, query);
					cloningContext.setMatcher(matcher);
					Object clonedValue = BaseType.INSTANCE.clone(cloningContext, value, strategyOnCriterionMatch);
					resultRow.add(clonedValue);
				}

				ListRecord resultRecord = ListRecord.T.create();
				resultRecord.setValues(resultRow);
				clonedResults.add(resultRecord);

			} else {
				Matcher matcher = getMatcher(row, query);
				cloningContext.setMatcher(matcher);
				Object clonedValue = BaseType.INSTANCE.clone(cloningContext, row, strategyOnCriterionMatch);
				clonedResults.add(clonedValue);
			}
		}

		return clonedResults;
	}

	protected List<Object> cloneSelectQueryResult(List<?> hqlResults, SelectQuery query) {
		return cloneSelectQueryResult(hqlResults, query, createStandardCloningContext());
	}

	protected Object clonePropertyQueryResult(Property property, Object value, PropertyQuery request, StandardCloningContext cloningContext) {
		return clonePropertyQueryResult(property, value, request, cloningContext, StrategyOnCriterionMatch.partialize);
	}

	/**
	 * @param property
	 *            queried property
	 */
	protected Object clonePropertyQueryResult(Property property, Object value, PropertyQuery request, StandardCloningContext cloningContext,
			StrategyOnCriterionMatch strategyOnCriterionMatch) {
		cloningContext.setMatcher(getMatcher(value, request));
		return BaseType.INSTANCE.clone(cloningContext, value, strategyOnCriterionMatch);
	}

	protected Object clonePropertyQueryResult(Property property, Object value, PropertyQuery request) {
		return clonePropertyQueryResult(property, value, request, createStandardCloningContext());
	}

	protected Object clonePropertyQueryResult(Object value, PropertyQuery propertyQuery) throws Exception {
		PersistentEntityReference entityReference = propertyQuery.getEntityReference();
		EntityType<GenericEntity> entityType = typeReflection.getType(entityReference.getTypeSignature());
		Property property = entityType.getProperty(propertyQuery.getPropertyName());
		return clonePropertyQueryResult(property, value, propertyQuery, createStandardCloningContext());
	}

	protected PropertyQueryResult createPropertyQueryResult(Property property, Object value, PropertyQuery request, boolean hasMore)
			throws Exception {
		return createPropertyQueryResult(property, value, request, hasMore, createStandardCloningContext());
	}

	protected PropertyQueryResult createPropertyQueryResult(Property property, Object value, PropertyQuery request, boolean hasMore,
			StandardCloningContext cloningContext) throws Exception {
		Object clonedValue = clonePropertyQueryResult(property, value, request, cloningContext);
		PropertyQueryResult propertyQueryResult = PropertyQueryResult.T.create();
		propertyQueryResult.setHasMore(hasMore);
		propertyQueryResult.setPropertyValue(clonedValue);
		return propertyQueryResult;
	}

	protected int maxPageSize(Query request) {
		Restriction r = request.getRestriction();
		if (r == null)
			return Integer.MAX_VALUE;

		Paging p = r.getPaging();
		if (p == null)
			return Integer.MAX_VALUE;

		return p.getPageSize();
	}

	protected Matcher getMatcher(Object value, Query request) {
		TraversingCriterion tc = request.getTraversingCriterion();
		if (tc != null)
			return createMatcher(tc, newDefaultPlaceholderLookukp(request));

		tc = getDefaultTcFor(value);
		if (tc == null)
			return null;

		return createMatcher(tc, null);
	}

	protected Matcher getMatcher(Query request) {
		TraversingCriterion tc = request.getTraversingCriterion();
		if (tc != null)
			return createMatcher(tc, newDefaultPlaceholderLookukp(request));

		tc = getDefaultTcFor(request);
		if (tc == null)
			return null;

		return createMatcher(tc, null);
	}

	private Function<String, TraversingCriterion> newDefaultPlaceholderLookukp(Query request) {
		return tcName -> DEFAULT_TC_NAME.equals(tcName) ? getDefaultTcFor(request) : null;
	}

	protected StandardMatcher createMatcher(TraversingCriterion tc, Function<String, TraversingCriterion> placeholderLookup) {
		StandardMatcher matcher = new StandardMatcher();
		matcher.setCriterion(tc);
		matcher.setPlaceholderLookup(placeholderLookup);
		return new ContainerEntitySkippingMatcher(matcher);
	}

	private TraversingCriterion getDefaultTcFor(Object value) {
		Class<? extends GenericEntity> entityClass = GenericEntity.class;

		if (value instanceof GenericEntity) {
			GenericEntity entity = (GenericEntity) value;
			entityClass = entity.getClass();
		}

		return getDefaultTraversingCriterion(entityClass);
	}

	private TraversingCriterion getDefaultTcFor(Query request) {
		if (request instanceof EntityQuery) {
			// Get the entityClass of the request.
			String entityTypeSignature = ((EntityQuery) request).getEntityTypeSignature();
			Class<? extends GenericEntity> entityClass = getEntityClassOf(entityTypeSignature);

			return getDefaultTraversingCriterion(entityClass);
		}

		if (request instanceof PropertyQuery) {
			Property requestedProperty = ((PropertyQuery) request).property();

			EntityType<?> referencedEntityType = getReferencedEntityType(requestedProperty);

			if (referencedEntityType != null)
				return getDefaultTraversingCriterion(referencedEntityType.getJavaType());
			else
				return null;

		}

		return getDefaultTraversingCriterion(GenericEntity.class);
	}

	/**
	 * Container entities (e.g. ListRecord from the RecordModel artifact) must not be
	 */
	private static class ContainerEntitySkippingMatcher extends StandardMatcher {
		private final StandardMatcher delegateMatcher;

		public ContainerEntitySkippingMatcher(StandardMatcher delegateMatcher) {
			this.delegateMatcher = delegateMatcher;
		}

		@Override
		public boolean matches(TraversingContext traversingContext) {
			if (traversingContext.getCurrentCriterionType() != CriterionType.PROPERTY) {
				return delegateMatcher.matches(traversingContext);
			}

			Stack<Object> os = traversingContext.getObjectStack();
			GenericEntity ge = (GenericEntity) os.get(os.size() - 2); // Top of the stack: propertyValue, entity, .....

			if (ge.getId() == null) {
				return false;
			}

			return delegateMatcher.matches(traversingContext);
		}
	}

	private static TraversingCriterion createDefaultTraversionCriterion() {
		return IncrementalAccesses.createDefaultTraversionCriterion();
	}

	protected Class<? extends GenericEntity> getEntityClassOf(String entityTypeSignature) {
		return getEntityTypeOf(entityTypeSignature).getJavaType();
	}

	/**
	 * Determines the referenced (associated) EntityType either directly from the {@link Property} or indirectly from a
	 * {@link CollectionType}
	 * 
	 * @param property
	 *            The {@link Property} which is referenced {@link EntityType} is needed
	 * @return the {@link EntityType} that is referenced by this {@link Property} or null if no {@link EntityType} is
	 *         referenced by the given {@link Property}
	 */
	protected EntityType<?> getReferencedEntityType(Property property) {
		GenericModelType type = property.getType();
		if (type instanceof EntityType<?>) {
			return (EntityType<?>) type;
		} else if (type instanceof CollectionType) {
			CollectionType collectionType = (CollectionType) type;
			GenericModelType elementType = collectionType.getCollectionElementType();
			if (elementType instanceof EntityType<?>) {
				return (EntityType<?>) elementType;
			} else
				return null;
		} else
			return null;
	}

	protected EntityType<? extends GenericEntity> getEntityTypeOf(String entityTypeSignature) {
		return typeReflection.getType(entityTypeSignature);
	}

	/**
	 * Extracts the {@link EntityType} from the parsed {@link Query}. <br>
	 * Currently {@link com.braintribe.model.query.EntityQuery} and {@link PropertyQuery} are supported.
	 * 
	 * @param query
	 *            The query that should be inspected
	 * @return The entity type referenced in the query.
	 */
	protected EntityType<GenericEntity> getEntityTypeOf(Query query) {
		String typeSignature = null;
		if (query instanceof EntityQuery) {
			typeSignature = ((EntityQuery) query).getEntityTypeSignature();
		} else if (query instanceof PropertyQuery) {
			EntityReference reference = ((PropertyQuery) query).getEntityReference();
			typeSignature = reference.getTypeSignature();
		} else {
			throw new IllegalArgumentException("Unsupported query type: " + query.getClass());
		}

		return typeReflection.getType(typeSignature);
	}

	protected GenericEntity getUniqueResult(EntityQueryResult result) throws ModelAccessException {
		return getUniqueResult(result, false);
	}

	/**
	 * Assumes that the given {@link com.braintribe.model.query.EntityQueryResult} contains exactly one
	 * GenericEntity.<br>
	 * If so, it returns this entity. In all other cases an Exception is thrown.
	 * 
	 * @param result
	 *            The query result that should contain a single element.
	 * @return The unique result that should be available.
	 */
	protected GenericEntity getUniqueResult(EntityQueryResult result, boolean resultMustExist) throws ModelAccessException {
		if (result == null || result.getEntities() == null || result.getEntities().size() == 0) {
			if (resultMustExist)
				throw new ModelAccessException("No entity found.");
			return null;
		}
		if (result.getEntities().size() > 1) {
			throw new ModelAccessException("The query returned moren then one entity.");
		}
		return result.getEntities().get(0);
	}

	@Override
	public ReferencesResponse getReferences(ReferencesRequest referencesRequest) throws ModelAccessException {
		Set<ReferencesCandidate> candidates = referenceFinder.findReferences(referencesRequest.getReference());

		ReferencesCandidates response = ReferencesCandidates.T.create();
		response.setCandiates(candidates);
		return response;
	}

	protected Set<EntityReference> scanEntityReferences(Manipulation manipulation) {
		return EntityReferenceScanner.findEntityReferences(manipulation);
	}

	protected Manipulation createManipulationFromReferenceMap(Map<PreliminaryEntityReference, PersistentEntityReference> referenceMap) {
		if (!referenceMap.isEmpty()) {
			List<Manipulation> manipulations = new ArrayList<>();
			for (Map.Entry<PreliminaryEntityReference, PersistentEntityReference> entry : referenceMap.entrySet()) {
				PreliminaryEntityReference preliminaryReference = entry.getKey();
				PersistentEntityReference persistentReference = entry.getValue();
				Manipulation manipulation = createChangeIdManipulation(preliminaryReference, persistentReference.getRefId());
				manipulations.add(manipulation);
			}

			switch (manipulations.size()) {
				case 0:
					return null;
				case 1:
					return manipulations.get(0);
				default:
					return compound(manipulations);
			}
		} else
			return null;
	}

	protected Manipulation createChangeIdManipulation(PreliminaryEntityReference preliminaryReference, Object newId) {
		EntityProperty entityProperty = entityProperty(preliminaryReference, GenericEntity.id);
		return changeValue(newId, entityProperty);
	}

	public static Map<EntityType<?>, Set<Object>> extractIdsByEntityType(Set<EntityReference> entityReferences) {
		return entityReferences.stream() //
				.filter(ref -> ref.referenceType() == EntityReferenceType.persistent) //
				.collect(groupingBy( //
						EntityReference::valueType, //
						mapping(EntityReference::getRefId, Collectors.toSet())));
	}

	protected ChangeValueManipulation createPartitionAssignmentManipulation(GenericEntity instantiation) {
		return createPartitionAssignmentManipulationForReference(instantiation.reference());
	}

	protected ChangeValueManipulation createPartitionAssignmentManipulationForReference(EntityReference ref) {
		EntityProperty owner = entityProperty(ref, GenericEntity.partition);
		return changeValue(defaultPartition, owner);
	}

	@Override
	public String getAccessId() {
		return accessId;
	}

	public void setAccessId(String accessId) {
		this.accessId = accessId;
		this.defaultPartition = accessId;
	}

	@Override
	public Set<String> getPartitions() throws ModelAccessException {
		if (partitions == null)
			loadPartitions();

		return partitions;
	}

	public void setPartitions(Set<String> partitions) {
		this.partitions = partitions;
	}

	private synchronized void loadPartitions() {
		if (partitions == null)
			partitions = Collections.unmodifiableSet(Collections.singleton(getAccessId()));
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + getAccessId() + ")" + "@" + Integer.toHexString(hashCode());
	}

	public static String getBuildVersion() {
		return "$Build_Version$ $Id: AbstractAccess.java 103069 2018-02-01 17:43:39Z peter.gazdik $";
	}
}
