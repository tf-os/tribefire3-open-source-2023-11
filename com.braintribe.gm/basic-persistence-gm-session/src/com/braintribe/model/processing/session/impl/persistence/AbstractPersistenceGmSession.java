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
package com.braintribe.model.processing.session.impl.persistence;

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.compound;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.localEntityProperty;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.common.attribute.TypeSafeAttribute;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.access.HasAccessId;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.accessapi.AccessDataRequest;
import com.braintribe.model.accessapi.AccessManipulationRequest;
import com.braintribe.model.accessapi.AccessRequest;
import com.braintribe.model.accessapi.HasInducedManipulation;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.accessapi.ReferencesRequest;
import com.braintribe.model.accessapi.ReferencesResponse;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.enhance.EntityFlags;
import com.braintribe.model.generic.eval.DelegatingEvalContext;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.EvalContextAspect;
import com.braintribe.model.generic.eval.EvalException;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.VdHolder;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.EntityReferenceType;
import com.braintribe.model.generic.value.GlobalEntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.manipulation.basic.tools.ManipulationRemotifier;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.managed.EntityQueryExecution;
import com.braintribe.model.processing.session.api.managed.EntityQueryResultConvenience;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.managed.ManipulationLenience;
import com.braintribe.model.processing.session.api.managed.ManipulationMode;
import com.braintribe.model.processing.session.api.managed.ManipulationReport;
import com.braintribe.model.processing.session.api.managed.MergeBuilder;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.model.processing.session.api.managed.PropertyQueryExecution;
import com.braintribe.model.processing.session.api.managed.PropertyQueryResultConvenience;
import com.braintribe.model.processing.session.api.managed.QueryExecution;
import com.braintribe.model.processing.session.api.managed.QueryResultConvenience;
import com.braintribe.model.processing.session.api.managed.SelectQueryExecution;
import com.braintribe.model.processing.session.api.managed.SelectQueryResultConvenience;
import com.braintribe.model.processing.session.api.managed.SessionQueryBuilder;
import com.braintribe.model.processing.session.api.notifying.EntityManipulationListenerRegistry;
import com.braintribe.model.processing.session.api.notifying.ManipulationListenerRegistry;
import com.braintribe.model.processing.session.api.persistence.CommitContext;
import com.braintribe.model.processing.session.api.persistence.CommitListener;
import com.braintribe.model.processing.session.api.persistence.KeepResponseDetached;
import com.braintribe.model.processing.session.api.persistence.PersistenceEntityAccessBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceManipulationListenerRegistry;
import com.braintribe.model.processing.session.api.persistence.PersistenceSessionQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.QueryResultMergeListener;
import com.braintribe.model.processing.session.api.persistence.aspects.SessionAspect;
import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;
import com.braintribe.model.processing.session.api.service.EnvelopeSessionAspect;
import com.braintribe.model.processing.session.api.transaction.Transaction;
import com.braintribe.model.processing.session.impl.managed.AbstractDelegatingQueryResultConvenience;
import com.braintribe.model.processing.session.impl.managed.AbstractManagedGmSession;
import com.braintribe.model.processing.session.impl.managed.AbstractQueryResultConvenience;
import com.braintribe.model.processing.session.impl.managed.IdentityCompetence;
import com.braintribe.model.processing.session.impl.managed.IdentityCompetenceException;
import com.braintribe.model.processing.session.impl.managed.QueryParserHelper;
import com.braintribe.model.processing.session.impl.managed.StaticEntityQueryResultConvenience;
import com.braintribe.model.processing.session.impl.managed.StaticPropertyQueryResultConvenience;
import com.braintribe.model.processing.session.impl.managed.StaticQueryResultConvenience;
import com.braintribe.model.processing.session.impl.managed.StaticSelectQueryResultConvenience;
import com.braintribe.model.processing.session.impl.managed.history.BasicTransaction;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.QueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.resource.CallStreamCapture;
import com.braintribe.model.resource.source.TransientSource;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.service.api.GenericProcessingRequest;
import com.braintribe.model.service.api.HasServiceRequest;
import com.braintribe.model.service.api.HasServiceRequests;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.processing.async.api.AsyncCallback;


public abstract class AbstractPersistenceGmSession extends AbstractManagedGmSession implements PersistenceGmSession, HasAccessId  {
	protected BasicTransaction transaction;
	protected SessionQueryBuilderImpl sessionQueryBuilder = new SessionQueryBuilderImpl();
	protected SessionQueryBuilderImpl detachedQueryBuilder = new SessionQueryBuilderImpl();
	protected String accessId;
	private final List<CommitListener> commitListeners = new ArrayList<>();
	private final List<QueryResultMergeListener> queryResultMergeListeners = new ArrayList<>();
	protected Map<Class<? extends SessionAspect<?>>, Object> sessionAspects = new HashMap<>();  

	private Supplier<? extends PersistenceGmSession> equivalentSessionFactory;
	protected final boolean manifestUnknownEntities = true;
	
	public AbstractPersistenceGmSession() {
		transaction = new BasicTransaction(this);
		listeners().add(transaction);
		
		sessionQueryBuilder = newSessionQueryBuilder(false);
		detachedQueryBuilder = newSessionQueryBuilder(true);
	}
	
	@Configurable
	public void setEquivalentSessionFactory(Supplier<? extends PersistenceGmSession> equivalentSessionFactory) {
		this.equivalentSessionFactory = equivalentSessionFactory;
	}
	
	protected SessionQueryBuilderImpl newSessionQueryBuilder(boolean detached) {
		return new SessionQueryBuilderImpl(detached);
	}
	
	protected abstract IncrementalAccess getIncrementalAccess();
	
	@Override
	public SessionQueryBuilder queryCache() {
		return super.query();
	}
	
	@Override
	public SessionQueryBuilder queryDetached() {
		return detachedQueryBuilder;
	}

	@Override
	public PersistenceSessionQueryBuilder query() {
		return sessionQueryBuilder;
	}

	@Override
	public void suspendHistory() {
		transaction.pushHibernation();
	}

	@Override
	public void resumeHistory() {
		transaction.popHibernation();
	}

	@Override
	public Transaction getTransaction() {
		return transaction;
	}
	
	/**
	 * Creates {@link ManipulationRequest} for manipulations done if the current transaction and converts them from local to remote (using )
	 */
	protected ManipulationRequest createManipulationRequest() {
		ManipulationRequest mr = createPlainManipulationRequest();
		if (mr != null) {
			mr.setManipulation(ManipulationRemotifier.remotify(mr.getManipulation()));
		}
		return mr;
	}

	private ManipulationRequest createPlainManipulationRequest() {
		List<Manipulation> manipulations = transaction.getManipulationsDone();
		
		ManipulationRequest manipulationRequest = null; 
		
		switch (manipulations.size()) {
			case 0:
				break;
			case 1:
				manipulationRequest = ManipulationRequest.T.create();
				manipulationRequest.setManipulation(manipulations.get(0));
				break;
			default:
				manipulationRequest = ManipulationRequest.T.create();
				manipulationRequest.setManipulation(compound(new ArrayList<>(manipulations)));
				break;
		}
		
		return manipulationRequest;
	}
	
	protected ManipulationReport processManipulationResponse(ManipulationResponse manipulationResponse) throws GmSessionException {
		suspendHistory();
		try {
			Manipulation inducedManipulation = manipulationResponse.getInducedManipulation();
			
			ManipulationReport result = null;
			if (inducedManipulation != null) {
				result = applyManipulation(inducedManipulation, manipulate().mode(ManipulationMode.REMOTE).lenience(lenience()).context());
			}
			
			transaction.clear();
			return result;
			
		} catch (Exception e) {
			throw new GmSessionException("error while processing manipulation response", e);
			
		} finally {
			resumeHistory();
		}
	}

	private ManipulationLenience lenience() {
		return manifestUnknownEntities ? ManipulationLenience.manifestOnUnknownEntity : ManipulationLenience.ignoreOnUnknownEntity;
	}

	@Override
	public ManipulationResponse commit() throws GmSessionException {
		ManipulationRequest manipulationRequest = createManipulationRequest();			
		return commit(manipulationRequest); 
	}

	protected ManipulationResponse commit(ManipulationRequest manipulationRequest) {
		if (manipulationRequest == null)
			return ManipulationResponse.T.create();

		fireOnBeforeCommit(manipulationRequest.getManipulation());
		ManipulationResponse manipulationResponse = applyRequestOnAccess(manipulationRequest);
		ManipulationReport report = processManipulationResponse(manipulationResponse);

		if (manifestUnknownEntities && report != null)
			refreshManifestedEntities(report.getLenientManifestations());

		fireOnAfterCommit(manipulationRequest.getManipulation(), manipulationResponse.getInducedManipulation());

		return manipulationResponse;
	}
	
	private ManipulationResponse applyRequestOnAccess(ManipulationRequest request) throws ModelAccessException {
		return getIncrementalAccess().applyManipulation(request);
	}

	/**
	 * this default implementation of the async commit method is actually returning a synched result to the callback immediately
	 */
	@Override
	public void commit(AsyncCallback<ManipulationResponse> callback) {
		ManipulationRequest manipulationRequest = createManipulationRequest();
		commit(manipulationRequest, callback);
	}
	
	protected void commit(ManipulationRequest manipulationRequest, AsyncCallback<ManipulationResponse> callback) {
		try {
			callback.onSuccess(commit(manipulationRequest));
		}
		catch (Throwable t) {
			callback.onFailure(t);
		}
	}

	@Override
	public CommitContext prepareCommit() {
		return new BasicCommitContext(this);
	}

	static final int loadingLimit = 100;
	
	private void refreshManifestedEntities(Map<EntityType<?>, Set<GenericEntity>> lenientManifestations) throws GmSessionException {
		for (EntityQuery query: prepareRefreshQueries(lenientManifestations)) {
			this.query().entities(query).list();
		}
	}

	protected List<EntityQuery> prepareRefreshQueries(Map<EntityType<?>, Set<GenericEntity>> lenientManifestations) {
		List<EntityQuery> result = new ArrayList<>();
		
		for (Map.Entry<EntityType<?>, Set<GenericEntity>> entityIdEntry : lenientManifestations.entrySet()) {
			EntityType<?> entityType = entityIdEntry.getKey();
			Set<GenericEntity> entities = entityIdEntry.getValue();

			int idCounter = loadingLimit;
			List<Set<Object>> idBulks = new ArrayList<>(1 + entities.size() / loadingLimit);
			Set<Object> idBulk = null;
			for (GenericEntity entity : entities) {
				if (idCounter == loadingLimit) {
					idCounter = 0;
					
					idBulk = newSet();
					idBulks.add(idBulk);
				}
				
				idBulk.add(entity.getId());
				idCounter++;
			}
			
			// TODO use entity references instead?
			for (Set<Object> bulkIds : idBulks) {
				EntityQuery eq = EntityQueryBuilder.from(entityType).where().property(GenericEntity.id).in(bulkIds).done();
				result.add(eq);
			}
		}
		
		return result;
	}

	@Override
	protected IdentityCompetence createIdentityCompetence() {
		return new PersistenceIdentityCompetence(this, getBackup());
	}
	
	protected class PersistenceIdentityCompetence extends SmoodSupportedIdentityCompetence {
		protected PersistenceGmSession persistenceSession;
		
		public PersistenceIdentityCompetence(PersistenceGmSession session, Smood backup) {
			super(session, backup);
			this.persistenceSession = session;
		}

		@Override
		public boolean wasPropertyManipulated(EntityProperty entityProperty) throws IdentityCompetenceException {
			GenericEntity entity = findExistingEntity(entityProperty.getReference());
			LocalEntityProperty localEntityProperty = localEntityProperty(entity, entityProperty.getPropertyName());

			return transaction.wasPropertyManipulated(localEntityProperty);
		}

		@Override
		public boolean isPreliminarilyDeleted(EntityReference entityReference) throws IdentityCompetenceException {
			return transaction.isPreliminarilyDeleted(entityReference);
		}

	}
	
	protected class SessionQueryBuilderImpl implements PersistenceSessionQueryBuilder {
		protected boolean detached = false;
		
		public SessionQueryBuilderImpl() {
			
		}
		
		public SessionQueryBuilderImpl(boolean detached) {
			this.detached = detached;
		}

		@Override
		public <T extends GenericEntity> T findEntity(String globalId) {
			GlobalEntityReference ref = GlobalEntityReference.T.create();
			ref.setRefId(globalId);
			ref.setTypeSignature(GenericEntity.T.getTypeSignature());

			return this.<T>entity(ref).find();
		}

		@Override
		public SelectQueryExecution select(SelectQuery selectQuery) {
			return new SelectQueryExecutionImpl(selectQuery, detached);
		}

		@Override
		public SelectQueryExecution select(String selectQueryString) {
			return new SelectQueryExecutionImpl(QueryParserHelper.parseSelectQuery(selectQueryString), detached);
		}
		
		@Override
		public EntityQueryExecution entities(EntityQuery entityQuery) {
			return new EntityQueryExecutionImpl(entityQuery, detached);
		}

		@Override
		public EntityQueryExecution entities(String entityQueryString) {
			return new EntityQueryExecutionImpl(QueryParserHelper.parseEntityQuery(entityQueryString), detached);
		}
		
		@Override
		public PropertyQueryExecution property(PropertyQuery propertyQuery) {
			return new PropertyQueryExecutionImpl(propertyQuery, detached);
		}

		@Override
		public PropertyQueryExecution property(String propertyQueryString) {
			return new PropertyQueryExecutionImpl(QueryParserHelper.parsePropertyQuery(propertyQueryString), detached);
		}
		
		@Override
		public QueryExecution abstractQuery(Query query) {
			QueryResultConvenience execution = queryExecution(query);
			
			return new QueryExecutionImpl(query, execution);
		}

		private QueryResultConvenience queryExecution(Query query) {
			if (query instanceof EntityQuery)
				return entities((EntityQuery) query);

			if (query instanceof PropertyQuery)
				return property((PropertyQuery) query);

			if (query instanceof SelectQuery)
				return select((SelectQuery) query);

			throw new UnsupportedOperationException("query type not supported: " + query.type());
		}
		
		@Override
		public QueryExecution abstractQuery(String queryString) {
			return abstractQuery(QueryParserHelper.parseQuery(queryString));
		}
		
		@Override
		public <T extends GenericEntity> PersistenceEntityAccessBuilder<T> entity(EntityReference entityReference) {
			if (entityReference == null) {
				throw new IllegalArgumentException("The entity reference must not be null.");
			}
			if (entityReference.getTypeSignature() == null) {
				throw new IllegalArgumentException("The entity reference does not contain a type signature.");
			}
			if (entityReference.getRefId() == null) {
				throw new IllegalArgumentException("The entity reference does not contain a reference Id.");
			}
			return new PersistentEntityAccessBuilder<>(entityReference, detached);
		}
		
		@Override
		public <T extends GenericEntity> PersistenceEntityAccessBuilder<T> entity(EntityType<T> entityType, Object id) {
			return entity(entityType, id, EntityReference.ANY_PARTITION);
		}
		
		@Override
		public <T extends GenericEntity> PersistenceEntityAccessBuilder<T> entity(EntityType<T> entityType, Object id, String partition) {
			PersistentEntityReference entityReference = PersistentEntityReference.T.create();
			entityReference.setTypeSignature(entityType.getTypeSignature());
			entityReference.setRefId(id);
			entityReference.setRefPartition(partition);
			return entity(entityReference);
		}
		
		@Override
		public <T extends GenericEntity> PersistenceEntityAccessBuilder<T> entity(String typeSignature, Object id) {
			return entity(typeSignature, id, EntityReference.ANY_PARTITION);
		}
		
		@Override
		public <T extends GenericEntity> PersistenceEntityAccessBuilder<T> entity(String typeSignature, Object id, String partition) {
			PersistentEntityReference entityReference = PersistentEntityReference.T.create();
			entityReference.setRefId(id);
			entityReference.setTypeSignature(typeSignature);
			entityReference.setRefPartition(partition);
			return entity(entityReference);
		}
		
		@Override
		public <T extends GenericEntity> PersistenceEntityAccessBuilder<T> entity(T entity) {
			return entity(entity.reference());
		}
	}
	
	protected EntityQueryResult postProcess(EntityQueryResult result) throws GmSessionException {
		suspendHistory();
		try {
			if(result != null) {
				result.setEntities(merge().adoptUnexposed(true).doFor(result.getEntities()));
				fireOnAfterQueryResultMerged(result);
			}
			return result;
		}
		finally {
			resumeHistory();
		}
	}
	
	protected void postProcess(final EntityQueryResult result, final AsyncCallback<EntityQueryResult> callback, boolean detached) throws GmSessionException {
		if (detached) {
			callback.onSuccess(result);

		} else if (result != null) {
			merge().adoptUnexposed(true).suspendHistory(true).doFor(result.getEntities(),new AsyncCallback<List<GenericEntity>>() {
				@Override
				public void onFailure(Throwable t) {
					callback.onFailure(t);					
				}
				
				@Override
				public void onSuccess(List<GenericEntity> future) {
					result.setEntities(future);
					fireOnAfterQueryResultMerged(result);
					callback.onSuccess(result);					
				}
			});
		}
	}
	
	protected PropertyQueryResult postProcess(PropertyQueryResult result) throws GmSessionException {
		try {
			suspendHistory();
			result.setPropertyValue(merge().adoptUnexposed(true).doFor(result.getPropertyValue()));
			fireOnAfterQueryResultMerged(result);
			return result;
		}
		finally {
			resumeHistory();
		}
	}
	
	protected void postProcess(final PropertyQueryResult result, final AsyncCallback<PropertyQueryResult> callback, boolean detached) throws GmSessionException {
		if (detached) {
			callback.onSuccess(result);
		}
		else if(result != null){
			merge().adoptUnexposed(true).suspendHistory(true).doFor(result.getPropertyValue(),new AsyncCallback<Object>() {
				@Override
				public void onFailure(Throwable t) {
					callback.onFailure(t);		
				}
				
				@Override
				public void onSuccess(Object future) {
					result.setPropertyValue(future);
					fireOnAfterQueryResultMerged(result);
					callback.onSuccess(result);
				}
			});
		}
	}
	
	protected SelectQueryResult postProcess(SelectQueryResult result) throws GmSessionException {
		try {
			suspendHistory();
			result.setResults(merge().adoptUnexposed(true).doFor(result.getResults()));
			fireOnAfterQueryResultMerged(result);
			return result;
		}
		finally {
			resumeHistory();
		}
	}
	
	protected void postProcess(final SelectQueryResult result,final AsyncCallback<SelectQueryResult> callback, boolean detached) throws GmSessionException {
		if (detached) {
			callback.onSuccess(result);
		}
		else if(result != null){
			merge().adoptUnexposed(true).suspendHistory(true)
			.doFor(result.getResults(), new AsyncCallback<List<Object>>() {
				@Override
				public void onFailure(Throwable t) {
					callback.onFailure(t);	
				}
				
				@Override
				public void onSuccess(List<Object> future) {
					result.setResults(future);
					fireOnAfterQueryResultMerged(result);
					callback.onSuccess(result);
				}
			});
		}
	}
	
	protected class PersistentEntityAccessBuilder<T extends GenericEntity> extends EntityAccessBuilderImpl<T> implements PersistenceEntityAccessBuilder<T> {
		private TraversingCriterion tc;
		protected boolean detached;

		public PersistentEntityAccessBuilder(EntityReference entityReference, boolean detached) {
			super(entityReference);
			this.detached = detached;
		}

		protected EntityQuery createEntityQuery() {
			EntityQueryBuilder builder = EntityQueryBuilder.from(entityReference.getTypeSignature());

			if (entityReference.referenceType() == EntityReferenceType.global) {
				return builder.where()
						.property(GenericEntity.globalId).eq(entityReference.getRefId())
					.tc(tc)
					.done();

			} else if (getPartitions().size() > 1 && !EntityReference.ANY_PARTITION.equals(entityReference.getRefPartition())) {
				/* Would probably make more sense to make a query like select e from <ref.typeSignature> where e = ref} */
				/* Will probably be done in a future version of TF, don't want to break stuff right now. */

				return builder.where()
						.conjunction()
							.entitySignature(null).eq(entityReference.getTypeSignature())
							.property(GenericEntity.id).eq(entityReference.getRefId())
							.property(GenericEntity.partition).eq(entityReference.getRefPartition())
						.close()
					.tc(tc)
					.done();
				
			} else {
				return builder.where()
						.conjunction()
							.entitySignature(null).eq(entityReference.getTypeSignature())
							.property(GenericEntity.id).eq(entityReference.getRefId())
						.close()
					.tc(tc)
					.done();
			}
		}
		
		protected Set<String> getPartitions() {
			try {
				return getIncrementalAccess().getPartitions();

			} catch (ModelAccessException e) {
				throw new RuntimeException("Error while retrieving partitions for access: " + getAccessId(), e);
			}
		}

		protected T findInCache() throws GmSessionException {
			return super.find();
		}
		
		@Override
		public T find() throws GmSessionException {
			T entity = detached? null: super.find();
			
			if (entity == null && entityReference.referenceType() != EntityReferenceType.preliminary) {
				EntityQuery query = createEntityQuery();
				try {
					EntityQueryResult result = getIncrementalAccess().queryEntities(query);
					
					if (!detached)
						result = postProcess(result);
					
					List<T> list = (List<T>) (result != null ? result.getEntities() : Collections.emptyList());
					
					switch (list.size()) {
						case 0:
							break;
						case 1:
							entity = list.get(0);
							break;
						default:
							throw new GmSessionException("Ambiguous results for reference " + entityReference);
					}
				} catch (ModelAccessException e) {
					throw new GmSessionException("error while querying entities from access", e);
				}
			}
			
			return entity;
		}

		@Override
		public T refresh() throws GmSessionException {
			EntityQuery query = createEntityQuery();
			try {
				EntityQueryResult queryResult = getIncrementalAccess().queryEntities(query);
				
				if (!detached) {
					absentifyNonScalarsInCache();
					queryResult = postProcess(queryResult);
				}

				return getUnique(queryResult.getEntities());
				
			} catch (ModelAccessException e) {
				throw new GmSessionException("error while querying entities from access", e);
			}
		}

		private void absentifyNonScalarsInCache() {
			T entity = findInCache();
			if (entity == null)
				return;

			suspendHistory();
			try {
				absentifyNonScalars(entity);
				
			} finally {
				resumeHistory();
			}			
		}

		private void absentifyNonScalars(T entity) {
			for (Property property : entity.entityType().getProperties()) {
				if (property.isIdentifier() || property.getType().isScalar())
					continue;
				
				property.set(entity, null); // to notify listeners and remove value from index in the cache
				property.setDirectUnsafe(entity, VdHolder.standardAiHolder);
			}
		}

		private <E> E getUnique(List<?> list) {
			switch (list.size()) {
				case 0:
					throw new NotFoundException("Could not find entity for reference: " + entityReference);
				case 1:
					return (E) list.get(0);
				default:
					throw new GmSessionException("Ambiguous results for reference " + entityReference);
			}
		}
		
		@Override
		public T findLocalOrBuildShallow() throws GmSessionException {
			EntityType<T> entityType = (EntityType<T>) entityReference.valueType();
			Object id = entityReference.getRefId();
			
			T result = getBackup().findEntity(entityType, id);
			if (result == null) {
				result = entityType.create();
				result.setId(id);
				result.setPartition(entityReference.getRefPartition());
				shallowify(result, entityType);

				suspendHistory();
				try {
					attach(result);

				} finally {
					resumeHistory();
				}
			}
			
			return result;
		}

		@Override
		public PersistentEntityAccessBuilder<T> withTraversingCriterion(TraversingCriterion tc) {
			this.tc = tc;
			return this;
		}
		
		@Override
		public void refresh(AsyncCallback<T> asyncCallback) {
			try {
				asyncCallback.onSuccess(refresh());
			}
			catch (Throwable t) {
				asyncCallback.onFailure(t);
			}
		}

		@Override
		public void find(AsyncCallback<T> asyncCallback) {
			try {
				asyncCallback.onSuccess(find());
			}
			catch (Throwable t) {
				asyncCallback.onFailure(t);
			}
		}
		
		@Override
		public void require(AsyncCallback<T> asyncCallback) {
			try {
				asyncCallback.onSuccess(require());
			}
			catch (Throwable t) {
				asyncCallback.onFailure(t);
			}
		}
		
		@Override
		public ReferencesResponse references() throws GmSessionException {
			try {
				ReferencesRequest request = ReferencesRequest.T.create();
				request.setReference(entityReference);
				getIncrementalAccess().getReferences(request);
				return super.references();

			} catch (ModelAccessException e) {
				throw new GmSessionException("error while determine references for " + entityReference, e);
			}
		}
		
		@Override
		public void references(AsyncCallback<ReferencesResponse> asyncCallback) {
			try {
				asyncCallback.onSuccess(references());
			}
			catch (Throwable t) {
				asyncCallback.onFailure(t);
			}
		}
	}
	
	protected class EntityQueryExecutionImpl extends AbstractQueryResultConvenience<EntityQuery, EntityQueryResult, EntityQueryResultConvenience> implements EntityQueryExecution {
		
		protected boolean detached;

		public EntityQueryExecutionImpl(EntityQuery entityQuery, boolean detached) {
			super(entityQuery);
			this.detached = detached;
		}

		@Override
		protected EntityQueryResult resultInternal(EntityQuery query) throws GmSessionException {
			try {
				EntityQueryResult result = getIncrementalAccess().queryEntities(query);
				return detached? result: postProcess(result);

			} catch (ModelAccessException e) {
				throw new GmSessionException("error while querying entities", e);
			}
		}

		@Override
		public void result(AsyncCallback<EntityQueryResultConvenience> callback) {
			try {
				callback.onSuccess(new StaticEntityQueryResultConvenience(getQuery(), result()));
			}
			catch (Throwable t) {
				callback.onFailure(t);
			}
		}
	}
	
	protected class PropertyQueryExecutionImpl extends AbstractQueryResultConvenience<PropertyQuery, PropertyQueryResult, PropertyQueryResultConvenience> implements PropertyQueryExecution {
		
		protected boolean detached;

		public PropertyQueryExecutionImpl(PropertyQuery propertyQuery, boolean detached) {
			super(propertyQuery);
			this.detached = detached;
		}
		
		@Override
		protected PropertyQueryResult resultInternal(PropertyQuery query) throws GmSessionException {
			try {
				PropertyQueryResult result = getIncrementalAccess().queryProperty(query);
				return detached? result: postProcess(result);
			} catch (ModelAccessException e) {
				throw new GmSessionException("error while querying entities", e);
			}
		}
		
		@Override
		public void result(AsyncCallback<PropertyQueryResultConvenience> callback) {
			try {
				callback.onSuccess(new StaticPropertyQueryResultConvenience(getQuery(), result()));
			}
			catch (Throwable t) {
				callback.onFailure(t);
			}
		}

	}
	
	protected class SelectQueryExecutionImpl extends AbstractQueryResultConvenience<SelectQuery, SelectQueryResult, SelectQueryResultConvenience> implements SelectQueryExecution {
		protected boolean detached;

		public SelectQueryExecutionImpl(SelectQuery selectQuery, boolean detached) {
			super(selectQuery);
			this.detached = detached;
		}
		
		@Override
		protected SelectQueryResult resultInternal(SelectQuery query) throws GmSessionException {
			try {
				SelectQueryResult result = getIncrementalAccess().query(query);
				return detached? result: postProcess(result);
			} catch (ModelAccessException e) {
				throw new GmSessionException("error while querying entities", e);
			}
		}
		
		@Override
		public void result(AsyncCallback<SelectQueryResultConvenience> callback) {
			try {
				callback.onSuccess(new StaticSelectQueryResultConvenience(getQuery(), result()));
			}
			catch (Throwable t) {
				callback.onFailure(t);
			}
		}
		
	}
	
	protected class QueryExecutionImpl extends AbstractDelegatingQueryResultConvenience implements QueryExecution {
		private final Query query;

		public QueryExecutionImpl(Query query, QueryResultConvenience execution) {
			super(execution);
			this.query = query;
		}

		@Override
		public void result(AsyncCallback<QueryResultConvenience> callback) {
			try {
				callback.onSuccess(new StaticQueryResultConvenience(this.query, result()));
			}
			catch (Throwable t) {
				callback.onFailure(t);
			}
		}
	}	
	
	@Override
	public String getAccessId() {
		return accessId;
	}

	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

	@Override
	public PersistenceManipulationListenerRegistry listeners() {
		return new PersistenceManipulationListenerRegistryImpl();
	}
	
	protected void fireOnBeforeCommit (Manipulation manipulation) {
		CommitListener[] commitListeners = this.commitListeners.toArray(new CommitListener[this.commitListeners.size()]);
		for (CommitListener listener : commitListeners) {
			listener.onBeforeCommit(this, manipulation);
		}
	}
	
	protected void fireOnAfterCommit (Manipulation manipulation, Manipulation inducedManipulation) {
		CommitListener[] commitListeners = this.commitListeners.toArray(new CommitListener[this.commitListeners.size()]);
		for (CommitListener listener : commitListeners) {
			listener.onAfterCommit(this, manipulation, inducedManipulation);
		}
	}
	
	protected void fireOnAfterQueryResultMerged(QueryResult result) {
		QueryResultMergeListener[] listeners = this.queryResultMergeListeners.toArray(new QueryResultMergeListener[this.queryResultMergeListeners.size()]);
		for (QueryResultMergeListener listener : listeners) {
			listener.onAfterQueryResultMerged(this, result);
		}
	}
	
	private class PersistenceManipulationListenerRegistryImpl implements PersistenceManipulationListenerRegistry {

		@Override
		public void add(CommitListener listener) {
			commitListeners.add(listener);
		}

		@Override
		public void remove(CommitListener listener) {
			commitListeners.remove(listener);
		}

		@Override
		public void add(QueryResultMergeListener listener) {
			queryResultMergeListeners.add(listener);
		}

		@Override
		public void remove(QueryResultMergeListener listener) {
			queryResultMergeListeners.remove(listener);
		}

		private boolean isCore;
		
		@Override
		public ManipulationListenerRegistry asCore(boolean isCore) {
			this.isCore = isCore;
			return this;
		}
		
		@Override
		public void add(ManipulationListener listener) {
			AbstractPersistenceGmSession.super.listeners().asCore(isCore).add(listener);
		}

		@Override
		public void addFirst(ManipulationListener listener) {
			AbstractPersistenceGmSession.super.listeners().asCore(isCore).addFirst(listener);
		}
		
		@Override
		public void remove(ManipulationListener listener) {
			AbstractPersistenceGmSession.super.listeners().remove(listener);
		}

		@Override
		public EntityManipulationListenerRegistry entity(GenericEntity entity) {
			return AbstractPersistenceGmSession.super.listeners().entity(entity);
		}

		@Override
		public ManipulationListenerRegistry entityProperty(GenericEntity entity, String property) {
			return AbstractPersistenceGmSession.super.listeners().entityProperty(entity, property);
		}

		@Override
		public ManipulationListenerRegistry entityProperty(LocalEntityProperty entityProperty) {
			return AbstractPersistenceGmSession.super.listeners().entityProperty(entityProperty);
		}

		@Override
		@SuppressWarnings("deprecation")
		public EntityManipulationListenerRegistry entityReference(EntityReference entityReference) {
			return AbstractPersistenceGmSession.super.listeners().entityReference(entityReference);
		}

		@Override
		@SuppressWarnings("deprecation")
		public ManipulationListenerRegistry entityProperty(EntityProperty entityProperty) {
			return AbstractPersistenceGmSession.super.listeners().entityProperty(entityProperty);
		}
	}
	
	@Override
	public <T> T getSessionAspect(Class<SessionAspect<T>> aspectClass) {
		T aspect = (T) this.sessionAspects.get(aspectClass);
		return aspect;
	}

	@Override
	public SessionAuthorization getSessionAuthorization() {
		return null;
	}
	
	@Override
	public void shallowifyInstances() {
		if (!getTransaction().getManipulationsDone().isEmpty()) {
			throw new IllegalStateException("Cannot shallowify instances of the session as there are non-committed changes.");
		}
		
		Set<GenericEntity> allEntities = getBackup().getEntitiesPerType(GenericEntity.T);
		 
		suspendHistory();
		try {
			for (GenericEntity ge: allEntities)
				shallowify(ge);

		} finally {
			resumeHistory();
		}
	}

	private void shallowify(GenericEntity ge) {
		shallowify(ge, ge.entityType());
	}

	private void shallowify(GenericEntity ge, EntityType<?> et) {
		for (Property p: et.getProperties()) {
			if (p.isIdentifying() || !p.isNullable()) {
				continue;
			}

			p.set(ge, null);
			p.setAbsenceInformation(ge, GMF.absenceInformation());
		}
		
		EntityFlags.setShallow(ge, true);
	}
	
	protected Evaluator<ServiceRequest> getRequestEvaluator() {
		throw new UnsupportedOperationException("evaluation not supported by " + getClass());
	}
	
	@Override
	public <T> EvalContext<T> eval(ServiceRequest request) {
		return new SessionEvalContext<>(request);
	}

	@Override
	public PersistenceGmSession newEquivalentSession() {
		if (equivalentSessionFactory != null)
			return equivalentSessionFactory.get();
		else
			return createEquivalentSession();
	}

	/** This method should be implemented specifically by each sub-type. */
	protected PersistenceGmSession createEquivalentSession() {
		throw new UnsupportedOperationException("Method 'AbstractPersistenceGmSession.createEquivalentSession' is not supported!");
	}

	private class SessionEvalContext<R> extends DelegatingEvalContext<R> {
		private final ServiceRequest request;
		private boolean integrate = false;
		private boolean applyManipulation = false;
		private ManagedGmSession envelopeSession;
		private boolean keepResponseDetached;
		
		public SessionEvalContext(ServiceRequest request) {
			super();
			this.request = preProcess(request);
		}

		@Override
		public R get() throws EvalException {
			return postProcess(getDelegate().get());
		}

		@Override
		public void get(AsyncCallback<? super R> callback) {
			getDelegate().get(new AsyncCallback<R>() {
				@Override
				public void onSuccess(R result) {
					try {
						postProcess(result, new AsyncCallback<R>() {
							@Override
							public void onSuccess(R result) {
								callback.onSuccess(result);
							}
							
							@Override
							public void onFailure(Throwable t) {
								callback.onFailure(t);
							}
						});
					}
					catch (Throwable e) {
						callback.onFailure(e);
					}					
				}
				
				@Override
				public void onFailure(Throwable t) {
					callback.onFailure(t);
				}
			});
		}
		
		@Override
		public Maybe<R> getReasoned() {
			Maybe<R> maybe = getDelegate().getReasoned();
			
			if (maybe.isSatisfied()) {
				return Maybe.complete(postProcess(maybe.get()));
			}
			else if (maybe.isIncomplete()) {
				return Maybe.incomplete(postProcess(maybe.value()), maybe.whyUnsatisfied());
			}
			else {
				return maybe;
			}
		}
		
		@Override
		public void getReasoned(AsyncCallback<? super Maybe<R>> callback) {
			getDelegate().getReasoned(new AsyncCallback<Maybe<R>>() {
				@Override
				public void onSuccess(Maybe<R> maybe) {
					if (!maybe.isEmpty()) {
						try {
							postProcess(maybe.value(), new AsyncCallback<R>() {
								@Override
								public void onSuccess(R result) {
									Maybe<R> processedMaybe = maybe.isSatisfied()? //
											Maybe.complete(result): //
											Maybe.incomplete(result, maybe.whyUnsatisfied());
									
									callback.onSuccess(processedMaybe);
								}
								
								@Override
								public void onFailure(Throwable t) {
									callback.onFailure(t);
								}
							});
						}
						catch (Throwable e) {
							callback.onFailure(e);
						}					
					}
					else {
						callback.onSuccess(maybe);;
					}
				}
				
				@Override
				public void onFailure(Throwable t) {
					callback.onFailure(t);
				}
			});
		}
		
		@Override
		public <A extends TypeSafeAttribute<? super V>, V> void setAttribute(Class<A> attribute, V value) {
			if (attribute == EnvelopeSessionAspect.class) {
				envelopeSession = (ManagedGmSession)value;
			}
			else if (attribute == KeepResponseDetached.class) {
				keepResponseDetached = (Boolean)value;
			}

			super.setAttribute(attribute, value);
		}
		
		
		@Override
		public <T, A extends EvalContextAspect<? super T>> EvalContext<R> with(Class<A> aspect, T value) {
			setAttribute(aspect, value);
			return this;
		}
		
		@Override
		protected EvalContext<R> getDelegate() {
			if (delegate == null) {
				Evaluator<ServiceRequest> evaluator = getRequestEvaluator();
				if (evaluator == null) {
					throw new IllegalStateException("No evaluator is configured for "+AbstractPersistenceGmSession.this);
				}
				delegate = (EvalContext<R>) request.eval(evaluator);
			}

			return delegate;
		}

		private void preProcessGeneric(GenericProcessingRequest request) {
			if (request instanceof HasServiceRequest) {
				preProcessNested(((HasServiceRequest) request).getServiceRequest());
			} else if (request instanceof HasServiceRequests) {
				for (ServiceRequest nestedRequest : ((HasServiceRequests) request).getRequests()) {
					preProcessNested(nestedRequest);
				}
			}
		}

		private void preProcessNested(ServiceRequest nestedRequest) {
			if (nestedRequest instanceof AccessRequest) {
				ensureDomainId((AccessRequest) nestedRequest);
			}
			if (nestedRequest instanceof GenericProcessingRequest) {
				preProcessGeneric((GenericProcessingRequest) nestedRequest);
			}
		}

		private String ensureDomainId(AccessRequest request) {
			String accessId = request.getDomainId();
			if (accessId == null) {
				request.setDomainId(accessId = getAccessId());
			}
			return accessId;
		}
		
		private ServiceRequest preProcess(ServiceRequest request) {

			if (request instanceof GenericProcessingRequest) {
				preProcessGeneric((GenericProcessingRequest)request);
			}

			if (request instanceof AccessRequest) {

				AccessRequest accessRequest = (AccessRequest) request;

				String accessId = ensureDomainId(accessRequest);

				if (accessId != null && accessId.equals(getAccessId())) {
					if (!keepResponseDetached && accessRequest instanceof AccessDataRequest) {
						integrate = true;
					}

					if (accessRequest instanceof AccessManipulationRequest) {
						applyManipulation = true;
					}

					request = detach(accessRequest);
				}
			}

			if (request instanceof AuthorizedRequest) {
				AuthorizedRequest authorizedRequest = (AuthorizedRequest) request;

				String sessionId = authorizedRequest.getSessionId();
				if (sessionId == null) {
					sessionId = getSessionIdFromSessionAuthorization();
					if (sessionId != null)
						authorizedRequest.setSessionId(sessionId);
				}
			}

			return request;
		}

		private String getSessionIdFromSessionAuthorization() {
			SessionAuthorization sa = getSessionAuthorization();
			return sa == null ? null : sa.getSessionId();
		}

		private R postProcess(R result) throws EvalException {
			processManipulationsIfGiven(result);

			MergeBuilder mergeBuilder = prepareMergeBuilder();
			if (mergeBuilder != null) {
				result = mergeBuilder.doFor(result);
			}

			return result;
		}
		
		private void processManipulationsIfGiven(R result) throws EvalException {
			if (applyManipulation) {
				if (result instanceof HasInducedManipulation) {
					HasInducedManipulation hasInducedManipulation = (HasInducedManipulation)result;
					Manipulation inducedManipulation = hasInducedManipulation.getInducedManipulation();
					if (inducedManipulation != null) {
						suspendHistory();
						try {
							applyManipulation(inducedManipulation, manipulate().mode(ManipulationMode.REMOTE).lenience(lenience()).context());
						} catch (GmSessionException e) {
							throw new EvalException("error while applying induced manipulations from service result into session", e);
						} finally {
							resumeHistory();
						}
					}					
				}
			}
		}
		
		private void postProcess(R result, AsyncCallback<R> callback) {
			try {
				processManipulationsIfGiven(result);
			} catch (Exception e) {
				callback.onFailure(e);
				return;
			}
			
			MergeBuilder mergeBuilder = prepareMergeBuilder();
			if (mergeBuilder != null) {
				try {
					mergeBuilder.doFor(result, new AsyncCallback<R>() {
						@Override
						public void onSuccess(R result) {
							callback.onSuccess(result);
						}
						
						@Override
						public void onFailure(Throwable t) {
							callback.onFailure(new EvalException("error while merging service result into session", t));
						}
					});
				} catch (Exception e) {
					callback.onFailure(new EvalException("error while merging service result into session", e));
				}
			}
			else {
				callback.onSuccess(result);
			}
		}
		
		private MergeBuilder prepareMergeBuilder() {
			ManagedGmSession mergeSession = null;
			if (integrate) {
				mergeSession = AbstractPersistenceGmSession.this;
			}
			else if (envelopeSession != null) {
				mergeSession = envelopeSession;
			}
			
			if (mergeSession != null) {
				return mergeSession.merge().adoptUnexposed(false).suspendHistory(true).envelopeFactory(this::createFrom);
			}
			else {
				return null;
			}
		}
		
		private GenericEntity createFrom(GenericEntity entity) {
			GenericEntity clonedEntity = envelopeSession != null? 
					envelopeSession.create(entity.entityType()):
						entity;

			if (entity.hasTransientData()) {
				if (entity instanceof TransientSource) {
					TransientSource transientSource = (TransientSource) entity;
					TransientSource clonedTransientSource = (TransientSource) clonedEntity;
					clonedTransientSource.setInputStreamProvider(transientSource.getInputStreamProvider());
				}
				else if (entity instanceof CallStreamCapture) {
					CallStreamCapture callStreamCapture = (CallStreamCapture)entity;
					CallStreamCapture clonedCallStreamCapture = (CallStreamCapture)clonedEntity;
					clonedCallStreamCapture.setOutputStreamProvider(callStreamCapture.getOutputStreamProvider());
				}
			}
			
			return clonedEntity;
		}

		private AccessRequest detach(AccessRequest accessAwareRequest) {
			return accessAwareRequest.clone(new StandardCloningContext() {
				@Override
				public <T> T getAssociated(GenericEntity entity) {
					T associated = super.getAssociated(entity);
					
					if (associated != null)
						return associated;
					
					if (entity.session() == AbstractPersistenceGmSession.this) {
						EntityType<GenericEntity> entityType = entity.entityType();
						GenericEntity clone = entityType.create();
						
						for (Property property: entityType.getProperties()) {
							GenericModelType type = property.getType();
							if (type.isBase()) {
								Object value = property.get(entity);
								type = type.getActualType(value);
							}
							
							if (type.isSimple()) {
								property.set(clone, property.get(entity));
							}
							else {
								property.setDirectUnsafe(clone, VdHolder.standardAiHolder);
							}
						}

						registerAsVisited(entity, clone);
						return (T)clone;
					}
					else {
						return null;
					}
					
				}
				
				@Override
				public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity entity) {
					GenericEntity clonedEntity = super.supplyRawClone(entityType, entity);
					
					if (entity.hasTransientData()) {
						if (entity instanceof TransientSource) {
							TransientSource transientSource = (TransientSource) entity;
							TransientSource clonedTransientSource = (TransientSource) clonedEntity;
							clonedTransientSource.setInputStreamProvider(transientSource.getInputStreamProvider());
						}
						else if (entity instanceof CallStreamCapture) {
							CallStreamCapture callStreamCapture = (CallStreamCapture)entity;
							CallStreamCapture clonedCallStreamCapture = (CallStreamCapture)clonedEntity;
							clonedCallStreamCapture.setOutputStreamProvider(callStreamCapture.getOutputStreamProvider());
						}
					}

					return clonedEntity;
				}
			});
			
		}
	}
}
