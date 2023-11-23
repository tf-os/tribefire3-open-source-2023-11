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

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.changeValue;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.compound;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.entityProperty;
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
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.braintribe.common.DeprecatedCode;
import com.braintribe.model.accessapi.ReferencesCandidate;
import com.braintribe.model.accessapi.ReferencesCandidates;
import com.braintribe.model.accessapi.ReferencesRequest;
import com.braintribe.model.accessapi.ReferencesResponse;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.util.EntityReferenceScanner;
import com.braintribe.model.generic.pr.criteria.CriterionType;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.Matcher;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.PropertyTransferCompetence;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.EntityReferenceType;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistry;
import com.braintribe.model.processing.core.expert.impl.ConfigurableGmExpertRegistry;
import com.braintribe.model.processing.findrefs.ReferenceFinder;
import com.braintribe.model.processing.traversing.api.SkipUseCase;
import com.braintribe.model.processing.traversing.engine.GMT;
import com.braintribe.model.processing.traversing.engine.api.customize.ClonerCustomization;
import com.braintribe.model.processing.traversing.engine.api.usecase.AbsentifySkipUseCase;
import com.braintribe.model.processing.traversing.engine.api.usecase.DefaultSkipUseCase;
import com.braintribe.model.processing.traversing.engine.api.usecase.ReferenceSkipUseCase;
import com.braintribe.model.processing.traversing.engine.impl.clone.Cloner;
import com.braintribe.model.processing.traversing.engine.impl.clone.legacy.CloningContextBasedBasicModelWalkerCustomization;
import com.braintribe.model.processing.traversing.engine.impl.clone.legacy.CloningContextBasedClonerCustomization;
import com.braintribe.model.processing.traversing.engine.impl.clone.legacy.GmtCompatibleCloningContext;
import com.braintribe.model.processing.traversing.engine.impl.skip.conditional.TcConfigurableSkipper;
import com.braintribe.model.processing.traversing.engine.impl.walk.BasicModelWalkerCustomization;
import com.braintribe.model.processing.traversing.engine.impl.walk.ModelWalker;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.record.ListRecord;
import com.braintribe.utils.collection.impl.AttributeContexts;

/**
 * 
 * @author dirk.scheffler
 * 
 */
public abstract class AbstractAccess extends AbstractCustomPersistenceRequestProcessingAccess {

	protected static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	protected Map<Class<? extends GenericEntity>, TraversingCriterion> defaultTraversingCriteria;
	protected ReentrantLock defaultTraversingCriteriaLock = new ReentrantLock();
	private String modelName;
	private GmExpertRegistry expertRegistry;
	private final ReferenceFinder referenceFinder = new ReferenceFinder(this);
	private String accessId;
	protected Set<String> partitions;
	protected ReentrantLock partitionsLock = new ReentrantLock();
	/** If partition of an entity is null, this value is used instead in the query result. */
	protected String defaultPartition;

	public void setDefaultTraversingCriteria(Map<Class<? extends GenericEntity>, TraversingCriterion> defaultTraversingCriteria) {
		this.defaultTraversingCriteria = defaultTraversingCriteria;
	}

	public Map<Class<? extends GenericEntity>, TraversingCriterion> getDefaultTraversingCriteria() {
		if (defaultTraversingCriteria == null) {
			defaultTraversingCriteriaLock.lock();
			try {
				if (defaultTraversingCriteria == null) {
					defaultTraversingCriteria = asMap(GenericEntity.class, createDefaultTraversionCriterion());
				}
			} finally {
				defaultTraversingCriteriaLock.unlock();
			}
		}

		return defaultTraversingCriteria;
	}

	protected TraversingCriterion getDefaultTraversingCriterion(Class<? extends GenericEntity> entityClass) {
		Map<Class<? extends GenericEntity>, TraversingCriterion> criteria = getDefaultTraversingCriteria();

		TraversingCriterion criterion = criteria.get(entityClass);

		// fallback logic along the interface class hierarchy
		if (criterion == null) {
			// fallback in superclass hierarchy
			Class<?>[] interfaces = entityClass.getInterfaces();

			for (Class<?> interfaceClass : interfaces) {
				if (interfaceClass != null && GenericEntity.class.isAssignableFrom(interfaceClass)) {
					Class<? extends GenericEntity> checkedSuperClass = (Class<? extends GenericEntity>) interfaceClass;
					criterion = getDefaultTraversingCriterion(checkedSuperClass);
					if (criterion != null)
						break;
				}
			}
		}

		return criterion;
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

	/** @deprecated use {@link #getMatcher(Object, Query)} instead. */
	@Deprecated
	protected TraversingCriterion getTraversingCriterion(Object value, SelectQuery query) {
		DeprecatedCode.logWarn();
		TraversingCriterion tc = query.getTraversingCriterion();
		return tc != null ? tc : getDefaultTcFor(value);
	}

	/** @deprecated use {@link #getMatcher(Query)} instead. */
	@Deprecated
	protected TraversingCriterion getTraversingCriterion(Query request) {
		DeprecatedCode.logWarn();
		TraversingCriterion tc = request.getTraversingCriterion();
		return tc != null ? tc : getDefaultTcFor(request);
	}

	/** @deprecated use {@link #cloneEntityQueryResult(List, Matcher, StandardCloningContext)} */
	@Deprecated
	protected List<GenericEntity> cloneEntityQueryResult(List<GenericEntity> entities, TraversingCriterion traversingCriterion,
			StandardCloningContext cloningContext) {
		DeprecatedCode.logWarn();
		return cloneEntityQueryResult(entities, traversingCriterion, cloningContext, StrategyOnCriterionMatch.partialize);
	}

	protected List<GenericEntity> cloneEntityQueryResult(List<GenericEntity> entities, Matcher matcher, StandardCloningContext cloningContext) {
		return cloneEntityQueryResult(entities, matcher, cloningContext, StrategyOnCriterionMatch.partialize);
	}

	/**
	 * @deprecated use {@link #cloneEntityQueryResult(List, Matcher, StandardCloningContext, StrategyOnCriterionMatch)}
	 */
	@Deprecated
	protected List<GenericEntity> cloneEntityQueryResult(List<GenericEntity> entities, TraversingCriterion traversingCriterion,
			StandardCloningContext cloningContext, StrategyOnCriterionMatch strategyOnCriterionMatch) {
		DeprecatedCode.logWarn();
		return cloneEntityQueryResult(entities, createMatcher(traversingCriterion), cloningContext, strategyOnCriterionMatch);
	}

	protected List<GenericEntity> cloneEntityQueryResult(List<GenericEntity> entities, Matcher matcher, StandardCloningContext cloningContext,
			StrategyOnCriterionMatch strategyOnCriterionMatch) {

		if (tcDescribesDepth() && isGmtCompatible(cloningContext) && matcher instanceof StandardMatcher)
			return cloneBreadthFirst((GmtCompatibleCloningContext) cloningContext, (StandardMatcher) matcher, entities, strategyOnCriterionMatch);

		CollectionType collectionType = typeReflection.getCollectionType(List.class, GenericEntity.T);

		cloningContext.setMatcher(matcher);
		List<GenericEntity> cuttedEntities = collectionType.clone(cloningContext, entities, strategyOnCriterionMatch);
		return cuttedEntities;
	}

	protected List<GenericEntity> cloneEntityQueryResult(List<GenericEntity> entities, EntityQuery query) {
		return cloneEntityQueryResult(entities, getMatcher(query));
	}

	/** @deprecated use {@link #cloneEntityQueryResult(List, Matcher)} */
	@Deprecated
	protected List<GenericEntity> cloneEntityQueryResult(List<GenericEntity> entities, TraversingCriterion traversingCriterion) {
		return cloneEntityQueryResult(entities, traversingCriterion, createStandardCloningContext());
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

	public static abstract class AbstractQueryResultCloningContext extends StandardCloningContext implements PropertyTransferCompetence {

		protected abstract String defaultPartition();

		@Override
		public void transferProperty(EntityType<?> sourceEntityType, GenericEntity sourceEntity, GenericEntity targetEntity, Property property,
				Object propertyValue) throws GenericModelException {

			if (propertyValue == null && property.isPartition())
				propertyValue = defaultPartition();

			property.set(targetEntity, propertyValue);
		}
	}

	protected class QueryResultCloningContext extends AbstractQueryResultCloningContext implements GmtCompatibleCloningContext {
		@Override
		protected String defaultPartition() {
			return AbstractAccess.this.defaultPartition;
		}
	}

	protected List<Object> cloneSelectQueryResult(List<?> results, SelectQuery query, StandardCloningContext cloningContext) {
		return cloneSelectQueryResult(results, query, cloningContext, StrategyOnCriterionMatch.partialize);
	}

	protected List<Object> cloneSelectQueryResult(List<?> results, SelectQuery query, StandardCloningContext cloningContext,
			StrategyOnCriterionMatch strategy) {
		if (tcDescribesDepth() && isGmtCompatible(cloningContext) && query.getTraversingCriterion() != null)
			return cloneBreadthFirst((GmtCompatibleCloningContext) cloningContext, getMatcher(results, query), (List<Object>) results, strategy);

		return transformTuples(results, value -> {
			StandardMatcher matcher = getMatcher(value, query);
			return cloneValue(cloningContext, matcher, value, strategy);
		});
	}

	// for cloning results from other things than SelectQuery, e.g. native HQL
	protected List<Object> cloneTuples(List<?> results, TraversingCriterion tc, StandardCloningContext cloningContext) {
		return transformTuples(results, value -> {
			StandardMatcher matcher = createMatcher(tc, null);
			return cloneValue(cloningContext, matcher, value, StrategyOnCriterionMatch.partialize);
		});
	}

	protected List<Object> transformTuples(List<?> results, Function<Object, Object> cloner) {
		List<Object> clonedResults = newList(results.size());

		// clone and thereby cut results according to traversing criteria

		for (Object row : results) {
			if (row == null) {
				clonedResults.add(null);

			} else if (row.getClass().isArray()) {
				Object values[] = (Object[]) row;
				List<Object> resultRow = new ArrayList<>(values.length);
				for (Object value : values) {
					Object clonedValue = cloner.apply(value);
					resultRow.add(clonedValue);
				}

				ListRecord resultRecord = ListRecord.T.create();
				resultRecord.setValues(resultRow);
				clonedResults.add(resultRecord);

			} else {
				Object clonedValue = cloner.apply(row);
				clonedResults.add(clonedValue);
			}
		}

		return clonedResults;
	}

	private Object cloneValue(StandardCloningContext cloningContext, StandardMatcher matcher, Object value, StrategyOnCriterionMatch strategy) {
		cloningContext.setMatcher(matcher);
		return BaseType.INSTANCE.clone(cloningContext, value, strategy);
	}

	private <T> T cloneBreadthFirst(GmtCompatibleCloningContext cc, StandardMatcher matcher, T result, StrategyOnCriterionMatch strategy) {
		BasicModelWalkerCustomization basicCustomization = new CloningContextBasedBasicModelWalkerCustomization(cc);
		basicCustomization.setAbsenceResolvable(false);
		basicCustomization.setAbsenceTraversable(true);

		ModelWalker modelWalker = new ModelWalker();
		modelWalker.setWalkerCustomization(basicCustomization);
		modelWalker.setBreadthFirst(true);

		TcConfigurableSkipper tcSkipper = new TcConfigurableSkipper();
		tcSkipper.setMatcher(matcher);
		tcSkipper.setSkipUseCase(toSkipUseCase(strategy));

		ClonerCustomization clonerCustomization = new CloningContextBasedClonerCustomization(cc);

		Cloner cloner = new Cloner();
		cloner.setCustomizer(clonerCustomization);

		GMT.traverse() //
				.customWalk(modelWalker) //
				.visitor(tcSkipper) //
				.visitor(cloner) //
				.doFor(result);

		return cloner.getClonedValue();
	}

	private SkipUseCase toSkipUseCase(StrategyOnCriterionMatch strategy) {
		if (strategy == null)
			strategy = StrategyOnCriterionMatch.partialize;

		switch (strategy) {
			default:
			case partialize:
				return AbsentifySkipUseCase.INSTANCE;
			case reference:
				return ReferenceSkipUseCase.INSTANCE;
			case skip:
				return DefaultSkipUseCase.INSTANCE;
		}
	}

	private boolean tcDescribesDepth() {
		return AttributeContexts.peek().findAttribute(TmpQueryResultDepth.class).isPresent();
	}

	private boolean isGmtCompatible(StandardCloningContext cloningContext) {
		return cloningContext instanceof GmtCompatibleCloningContext;
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
	protected Object clonePropertyQueryResult(Property property, Object value, PropertyQuery query, StandardCloningContext cloningContext,
			StrategyOnCriterionMatch strategy) {
		StandardMatcher matcher = getMatcher(value, query);

		if (tcDescribesDepth() && isGmtCompatible(cloningContext) && query.getTraversingCriterion() != null)
			return cloneBreadthFirst((GmtCompatibleCloningContext) cloningContext, matcher, value, strategy);
		else
			return cloneValue(cloningContext, matcher, value, strategy);
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

		int result = p.getPageSize();
		return result > 0 ? result : Integer.MAX_VALUE;
	}

	protected StandardMatcher getMatcher(Object value, Query query) {
		TraversingCriterion tc = query == null ? null : query.getTraversingCriterion();
		if (tc != null)
			return createMatcher(tc, newDefaultPlaceholderLookukp(query));

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

	/** @deprecated use {@link #getMatcher(Query)} or {@link #getMatcher(Object, Query)} */
	@Deprecated
	protected StandardMatcher createMatcher(TraversingCriterion tc) {
		DeprecatedCode.logWarn();
		Function<String, TraversingCriterion> pl = tcName -> "default".equals(tcName) ? getDefaultTraversingCriterion(GenericEntity.class) : null;

		return createMatcher(tc, pl);
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
		return getDefaultTraversingCriterion(getQueriedType(request));
	}

	private Class<? extends GenericEntity> getQueriedType(Query request) {
		if (request instanceof EntityQuery) {
			// Get the entityClass of the request.
			String entityTypeSignature = ((EntityQuery) request).getEntityTypeSignature();
			return getEntityClassOf(entityTypeSignature);
		}

		if (request instanceof PropertyQuery) {
			Property requestedProperty = ((PropertyQuery) request).property();

			EntityType<?> referencedEntityType = getReferencedEntityType(requestedProperty);

			if (referencedEntityType != null)
				return referencedEntityType.getJavaType();
		}

		return GenericEntity.class;
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
			if (traversingContext.getCurrentCriterionType() != CriterionType.PROPERTY)
				return delegateMatcher.matches(traversingContext);

			Stack<Object> os = traversingContext.getObjectStack();
			if (os.isEmpty())
				return delegateMatcher.matches(traversingContext);

			GenericEntity ge = (GenericEntity) os.get(os.size() - 2); // Top of the stack: propertyValue, entity, .....

			if (ge.getId() == null) {
				return false;
			}

			return delegateMatcher.matches(traversingContext);
		}

		/* This method is only used when cloning with GMT for the breadth-first cloning. In that case no container is being
		 * cloned! */
		@Override
		public boolean matches(com.braintribe.model.generic.pr.criteria.matching.TcIterator it) {
			return delegateMatcher.matches(it);
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
		if (type instanceof EntityType<?>)
			return (EntityType<?>) type;

		if (type instanceof CollectionType) {
			GenericModelType elementType = ((CollectionType) type).getCollectionElementType();
			return elementType.isEntity() ? (EntityType<?>) elementType : null;
		}

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
	 * Assumes that the given {@link com.braintribe.model.query.EntityQueryResult} contains exactly one GenericEntity.<br>
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

	@Deprecated // use extractIdsByEntityType
	protected Map<EntityType<GenericEntity>, Set<Object>> extractEntityTypeIds(Set<EntityReference> entityReferences) {
		return (Map<EntityType<GenericEntity>, Set<Object>>) (Map<?, ?>) extractIdsByEntityType(entityReferences);
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
		if (partitions == null) {
			partitionsLock.lock();
			try {
				if (partitions == null) {
					partitions = Collections.unmodifiableSet(Collections.singleton(getAccessId()));
				}
			} finally {
				partitionsLock.unlock();
			}

		}

		return partitions;
	}

	public void setPartitions(Set<String> partitions) {
		this.partitions = partitions;
	}

	/** @deprecated use field {@link #defaultPartition} */
	@Deprecated
	protected String defaultPartition() {
		return defaultPartition;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + getAccessId() + ")" + "@" + Integer.toHexString(hashCode());
	}

}
