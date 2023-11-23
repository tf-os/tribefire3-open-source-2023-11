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
package com.braintribe.model.access.crud;

import static com.braintribe.model.generic.typecondition.TypeConditions.isAssignableTo;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.BasicAccessAdapter;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.crud.api.CrudExpert;
import com.braintribe.model.access.crud.api.create.EntityCreationContext;
import com.braintribe.model.access.crud.api.create.EntityCreator;
import com.braintribe.model.access.crud.api.delete.EntityDeleter;
import com.braintribe.model.access.crud.api.delete.EntityDeletionContext;
import com.braintribe.model.access.crud.api.query.ConditionAnalyser;
import com.braintribe.model.access.crud.api.query.ConditionAnalysis;
import com.braintribe.model.access.crud.api.read.EntityReader;
import com.braintribe.model.access.crud.api.read.EntityReadingContext;
import com.braintribe.model.access.crud.api.read.PopulationReader;
import com.braintribe.model.access.crud.api.read.PopulationReadingContext;
import com.braintribe.model.access.crud.api.read.PropertyReader;
import com.braintribe.model.access.crud.api.read.PropertyReadingContext;
import com.braintribe.model.access.crud.api.read.QueryContext;
import com.braintribe.model.access.crud.api.update.EntityUpdateContext;
import com.braintribe.model.access.crud.api.update.EntityUpdater;
import com.braintribe.model.access.crud.support.query.BasicConditionAnalyser;
import com.braintribe.model.access.crud.support.query.BasicQueryContext;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.accessapi.ReferencesRequest;
import com.braintribe.model.accessapi.ReferencesResponse;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.JunctionBuilder;
import com.braintribe.model.generic.processing.pr.fluent.PatternBuilder;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.query.eval.api.repo.Repository;
import com.braintribe.model.processing.query.stringifier.BasicQueryStringifier;
import com.braintribe.model.processing.query.support.IdentityManagedRepository;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.query.conditions.Condition;

/**
 * An implementation of the {@link BasicAccessAdapter} that delegates read and write operations
 * to configured {@link CrudExpert} implementations provided by the configured {@link CrudExpertResolver}.
 * 
 * @author gunther.schenk
 */
public class CrudExpertAccess implements IncrementalAccess, InitializationAware {

	private final static Logger logger = Logger.getLogger(CrudExpertAccess.class);
	
	private CrudExpertResolver expertResolver;
	private ConditionAnalyser conditionAnalyser = new BasicConditionAnalyser();
	
	private Runnable initializer;
	private ThreadLocal<QueryContext> queryContext = new ThreadLocal<>();
	private Function<QueryContext, QueryContext> queryPreProcessor = c -> c;
	
	private DelegateAccess delegate = new DelegateAccess();
	
	// ***************************************************************************************************
	// Setter
	// ***************************************************************************************************

	@Required
	@Configurable
	public void setExpertResolver(CrudExpertResolver expertResolver) {
		this.expertResolver = expertResolver;
	}
	
	@Configurable
	public void setConditionAnalyser(ConditionAnalyser conditionAnalyser) {
		this.conditionAnalyser = conditionAnalyser;
	}
	
	@Configurable
	public void setInitializer(Runnable initializer) {
		this.initializer = initializer;
	}
	
	@Configurable
	public void setQueryPreProcessor(Function<QueryContext, QueryContext> queryPreProcessor) {
		this.queryPreProcessor = queryPreProcessor;
	}
	
	@Configurable
	@Required
	public void setMetaModelProvider(Supplier<GmMetaModel> metaModelProvider) {
		this.delegate.setMetaModelProvider(metaModelProvider);
	}

	@Configurable
	public void setModelAccessoryFactory(ModelAccessoryFactory modelAccessoryFactory) {
		this.delegate.setModelAccessoryFactory(modelAccessoryFactory);
	}
	
	@Configurable
	public void setAccessId(String accessId) {
		this.delegate.setAccessId(accessId);
	}
	
	@Configurable
	public void setDefaultTraversingCriteria(Map<Class<? extends GenericEntity>, TraversingCriterion> defaultTraversingCriteria) {
		this.delegate.setDefaultTraversingCriteria(defaultTraversingCriteria);
	}
	
	public DefaultTcConfigurator configureDefaultTc() {
		return this.delegate;
	}
	
	// ***************************************************************************************************
	// Initializations
	// ***************************************************************************************************
	
	@Override
	public void postConstruct() {
		if (this.initializer != null) {
			this.initializer.run();
		}
	}

	// ***************************************************************************************************
	// Top Level IncrementalAccess methods (delegated) 
	// ***************************************************************************************************

	@Override
	public EntityQueryResult queryEntities(EntityQuery query)  {
		return withQueryContext(query, this.delegate::queryEntities);
	}
	
	@Override
	public PropertyQueryResult queryProperty(PropertyQuery query) {
		return withQueryContext(query, this.delegate::queryProperty);
	}
	
	@Override
	public SelectQueryResult query(SelectQuery query) {
		return withQueryContext(query, this.delegate::query);
	}
	
	@Override
	public ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest) throws ModelAccessException {
		return this.delegate.applyManipulation(manipulationRequest);
	}
	
	@Override
	public String getAccessId() {
		return this.delegate.getAccessId();
	}
	
	@Override
	public GmMetaModel getMetaModel() throws GenericModelException {
		return this.delegate.getMetaModel();
	}
	
	@Override
	public Set<String> getPartitions() throws ModelAccessException {
		return this.delegate.getPartitions();
	}
	
	@Override
	public ReferencesResponse getReferences(ReferencesRequest referencesRequest) throws ModelAccessException {
		return this.delegate.getReferences(referencesRequest);
	}
	
	// ***************************************************************************************************
	// Registration Helpers 
	// ***************************************************************************************************

	@SuppressWarnings("unchecked")
	protected <T extends GenericEntity> EntityReader<T> getEntityReader(EntityType<T> requestedType) {
		return getExpert(EntityReader.class, requestedType);
	}
	
	@SuppressWarnings("unchecked")
	protected <T extends GenericEntity> PopulationReader<T> getPopulationReader(EntityType<T> requestedType) {
		return getExpert(PopulationReader.class, requestedType);
	}
	
	@SuppressWarnings("unchecked")
	protected <T extends GenericEntity> PropertyReader<T,Object> getPropertyReader(EntityType<T> requestedType) {
		return getExpert(PropertyReader.class, requestedType);
	}
	
	@SuppressWarnings("unchecked")
	protected <T extends GenericEntity> EntityCreator<T> getEntityCreator(EntityType<T> requestedType) {
		return getExpert(EntityCreator.class, requestedType);
	}
	
	@SuppressWarnings("unchecked")
	protected <T extends GenericEntity> EntityUpdater<T> getEntityUpdater(EntityType<T> requestedType) {
		return getExpert(EntityUpdater.class, requestedType);
	}

	@SuppressWarnings("unchecked")
	protected < T extends GenericEntity> EntityDeleter<T> getEntityDeleter(EntityType<T> requestedType) {
		return getExpert(EntityDeleter.class, requestedType);
	}
	
	protected <E extends CrudExpert<T>, T extends GenericEntity> E getExpert(Class<E> expertType, EntityType<T> requestedType) {
		return this.expertResolver.getExpert(expertType,requestedType);
	}

	// ***************************************************************************************************
	// Execution Helpers 
	// ***************************************************************************************************

	protected <T,R extends Query> T withQueryContext (R request, Function<R, T> worker) {
		long start = System.currentTimeMillis();
		boolean succeeded = false;
		try {
			QueryContext queryContext = new BasicQueryContext(request);
			queryContext = this.queryPreProcessor.apply(queryContext);
			pushQueryContext(queryContext);
			
			@SuppressWarnings("unchecked")
			T result = worker.apply((R)queryContext.getQuery());
			succeeded = true;
			return result;
		} finally {
			popQueryContext();
			long end = System.currentTimeMillis();
			String statusText = (succeeded) ? "succeeded" : "failed";
			logger.debug("Execution of query: "+BasicQueryStringifier.print(request)+" "+statusText+" after: "+(end-start)+" ms.");
		}
	}

	private void pushQueryContext(QueryContext queryContext) {
		this.queryContext.set(queryContext);
	}

	private void popQueryContext() {
		this.queryContext.remove();
	}
	
	// ***************************************************************************************************
	// Helper Classes 
	// ***************************************************************************************************
	
	public class CrudExpertLazyLoader extends PropertyAccessInterceptor {
		
		private ManagedGmSession session; 		
		
		public CrudExpertLazyLoader(ManagedGmSession session ) {
			this.session = session;
		}
		
		@Override
		public Object getProperty(Property property, GenericEntity entity, boolean isVd) {
			if (property.isAbsent(entity)) {
				Object propertyValue = delegate.getPropertyValue(property, entity);
				//@formatter:off
				Object managedPropertyValue = this.session
						.merge()
						.suspendHistory(true)
						.doFor(propertyValue);
				//@formatter:on
				property.setDirect(entity, managedPropertyValue);
				return managedPropertyValue;
			}
			return super.getProperty(property, entity, isVd);
		}
	}
	
	public interface DefaultTcConfigurator {
		
		DefaultTcConfigurator addExclusionProperty(Property property);
		DefaultTcConfigurator addExclusionProperty(EntityType<?> type, String property);
		DefaultTcConfigurator addExclusionProperties(Set<Property> properties);
		DefaultTcConfigurator addExclusionProperties(EntityType<?> type, Set<String> properties);
		
	}
	
	private class DelegateAccess extends BasicAccessAdapter implements DefaultTcConfigurator {
		
		private Map<EntityType<?>, Set<String>> defaultTcExclusionProperties = new HashMap<>();
		
		// ***************************************************************************************************
		// Constructor
		// ***************************************************************************************************

		public DelegateAccess() {
			super();
			super.entitiesCanBeAdopted = true;
			super.repository.setAssumeIdIsIndexed(true);
		}
		
		// ***************************************************************************************************
		// Configuration
		// ***************************************************************************************************

		@Override
		public DefaultTcConfigurator addExclusionProperties(EntityType<?> type, Set<String> properties) {
			Set<String> registeredProperties = this.defaultTcExclusionProperties.computeIfAbsent(type, t -> new HashSet<>());
			registeredProperties.addAll(properties);
			return this;
		}
		
		@Override
		public DefaultTcConfigurator addExclusionProperties(Set<Property> properties) {
			//@formatter:off
			properties
			 .stream()
			 .forEach(this::addExclusionProperty);
			//@formatter:on
			return this;
		}
		
		@Override
		public DefaultTcConfigurator addExclusionProperty(EntityType<?> type, String property) {
			addExclusionProperties(type, asSet(property));
			return this;
		}
		
		@Override
		public DefaultTcConfigurator addExclusionProperty(Property property) {
			addExclusionProperty(property.getDeclaringType(), property.getName());
			return this;
		}
		
		
		// ***************************************************************************************************
		// Internal setup
		// ***************************************************************************************************

		/**
		 * Overriding to ensure that lazy-loading the enriched {@link ManagedGmSession} session (provided by {@link #createLoadingSession()}
		 * will be used for identity management during a query request. 
		 */
		@Override
		protected Repository newRepository() {
			ManagedGmSession session = createLoadingSession();
			return new IdentityManagedRepository(repository, entitiesCanBeAdopted, session);
		}
		
		/**
		 * returns a {@link ManagedGmSession} enriched with an {@link CrudExpertLazyLoader} interceptor
		 * which takes care of (lazy) loading (based on configured the {@link PropertyReader}'s)
		 */
		protected ManagedGmSession createLoadingSession() {
			BasicManagedGmSession session = new BasicManagedGmSession();
			session.setMetaModel(getMetaModel());
			session.interceptors().add(new CrudExpertLazyLoader(session));
			return session;
		}

		/**
		 * To ensure lazy loading during result cloning we need to prepare the internally used cloning context.
		 */
		@Override
		protected StandardCloningContext createStandardCloningContext() {
			
			return new QueryResultCloningContext() {
				@Override
				public boolean isAbsenceResolvable(Property property, GenericEntity entity, AbsenceInformation absenceInformation) {
					return true; //Let's resolve absent properties during cloning.
				}
				@Override
				public boolean isPropertyValueUsedForMatching(EntityType<?> type, GenericEntity entity, Property property) {
					return false; //Absent properties should be resolved only if the query TC doesn't match. 
				}
			};
		}
		
		@Override
		public Map<Class<? extends GenericEntity>, TraversingCriterion> getDefaultTraversingCriteria() {
			if (super.defaultTraversingCriteria == null) {
				Map<Class<? extends GenericEntity>, TraversingCriterion> standardDefaultTraversingCriteria = super.getDefaultTraversingCriteria();
				TraversingCriterion standardDefaultTraversingCriterion = standardDefaultTraversingCriteria.get(GenericEntity.class);
				if (!this.defaultTcExclusionProperties.isEmpty()) {
					
					//@formatter:off
					JunctionBuilder<JunctionBuilder<TC>> tcBuilder = TC.create()
							.conjunction()
								.criterion(standardDefaultTraversingCriterion)
								.negation()
									.disjunction();
					
					this.defaultTcExclusionProperties
						.entrySet()
						.stream()
						.forEach(e -> {
							PatternBuilder<JunctionBuilder<JunctionBuilder<TC>>> patternBuilder = tcBuilder.pattern();
							patternBuilder.typeCondition(isAssignableTo(e.getKey()));
							JunctionBuilder<PatternBuilder<JunctionBuilder<JunctionBuilder<TC>>>> propertiesBuilder = patternBuilder.disjunction();
							e.getValue()
								.stream()
								.forEach(propertiesBuilder::property);
							propertiesBuilder.close();
							patternBuilder.close();
						});
					//@formatter:on
					
					TraversingCriterion defaultTraversingCriterion = tcBuilder.close().close().done();
					super.defaultTraversingCriteria.put(GenericEntity.class, defaultTraversingCriterion);
				}
			}
			return super.defaultTraversingCriteria;
		}
		
		
		// ***************************************************************************************************
		// CRUD methods (called by super class)
		// ***************************************************************************************************

		@Override
		protected Iterable<GenericEntity> queryPopulation(String requestedTypeSignature, Condition condition, Ordering ordering) {
			EntityType<GenericEntity> requestedType = typeReflection.getEntityType(requestedTypeSignature);
			
			//@formatter:off
			return getPopulationReader(requestedType)
					.findEntities(
						PopulationReadingContext.create(
								requestedType, 
								condition, 
								ordering, 
								analyseCondition(condition, requestedType),
								queryContext.get()));
			//@formatter:on
		}

		@Override
		protected GenericEntity getEntity(EntityReference entityReference) {
			EntityType<GenericEntity> requestedType = typeReflection.getEntityType(entityReference.getTypeSignature());
			Object id = entityReference.getRefId();
			
			//@formatter:off
			return getEntityReader(requestedType)
					.getEntity(
						EntityReadingContext.create(
								requestedType, 
								id,
								queryContext.get()));
			//@formatter:on
		}
		
		protected Object getPropertyValue(Property property, GenericEntity entity) {
			EntityType<GenericEntity> requestedType = entity.entityType();
			String propertyName = property.getName();
			
			//@formatter:off
			return getPropertyReader(requestedType)
					.getPropertyValue(
						PropertyReadingContext.create(
								entity, 
								propertyName,
								queryContext.get()));
			//@formatter:on
		}

		@Override
		protected void save(AdapterManipulationReport context) throws ModelAccessException {
			//@formatter:off
			context
			.getCreatedEntities()
			.stream()
			.forEach(e->createEntity(e,context));
			
			context
			.getUpdatedEntities()
			.stream()
			.forEach(e->updateEntity(e,context));
			
			context
			.getDeletedEntities()
			.stream()
			.forEach(e->deleteEntity(e,context));
			//@formatter:on
		}
		
		// ***************************************************************************************************
		// Helpers
		// ***************************************************************************************************
		
		protected ConditionAnalysis analyseCondition(Condition condition, EntityType<GenericEntity> requestedType) {
			return conditionAnalyser.analyse(condition, requestedType);
		}
		
		protected void deleteEntity(GenericEntity entity, AdapterManipulationReport context) {
			EntityDeleter<GenericEntity> entityUpdater = getEntityDeleter(entity.entityType());
			entityUpdater
				.deleteEntity(
					EntityDeletionContext.create(
							entity, 
							context));
		}

		protected void updateEntity(GenericEntity entity, AdapterManipulationReport context) {
			EntityUpdater<GenericEntity> entityUpdater = getEntityUpdater(entity.entityType());
			entityUpdater
				.updateEntity(
					EntityUpdateContext.create(
							entity, 
							context));
		}

		protected void createEntity(GenericEntity entity, AdapterManipulationReport context) {
			EntityCreator<GenericEntity> entityCreator = getEntityCreator(entity.entityType());
			entityCreator
				.createEntity(
					EntityCreationContext.create(
							entity, 
							context));
		}
	}
}
