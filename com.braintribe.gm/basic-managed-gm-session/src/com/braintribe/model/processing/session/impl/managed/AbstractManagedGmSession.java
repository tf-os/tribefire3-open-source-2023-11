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
package com.braintribe.model.processing.session.impl.managed;

import java.util.LinkedList;
import java.util.function.Function;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ReferencesRequest;
import com.braintribe.model.accessapi.ReferencesResponse;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.enhance.ManipulationTrackingPropertyAccessInterceptor;
import com.braintribe.model.generic.enhance.VdePropertyAccessInterceptor;
import com.braintribe.model.generic.manipulation.AcquireManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.util.HistorySuspension;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.managed.EntityAccessBuilder;
import com.braintribe.model.processing.session.api.managed.EntityQueryExecution;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.managed.ManipulationApplicationContext;
import com.braintribe.model.processing.session.api.managed.ManipulationApplicationContextBuilder;
import com.braintribe.model.processing.session.api.managed.ManipulationLenience;
import com.braintribe.model.processing.session.api.managed.ManipulationMode;
import com.braintribe.model.processing.session.api.managed.ManipulationReport;
import com.braintribe.model.processing.session.api.managed.MergeBuilder;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.PropertyQueryExecution;
import com.braintribe.model.processing.session.api.managed.QueryExecution;
import com.braintribe.model.processing.session.api.managed.SelectQueryExecution;
import com.braintribe.model.processing.session.api.managed.SessionQueryBuilder;
import com.braintribe.model.processing.session.api.notifying.interceptors.CollectionEnhancer;
import com.braintribe.model.processing.session.api.notifying.interceptors.ManipulationTracking;
import com.braintribe.model.processing.session.api.notifying.interceptors.VdEvaluation;
import com.braintribe.model.processing.session.impl.managed.merging.ContinuableMerger;
import com.braintribe.model.processing.session.impl.notifying.BasicNotifyingGmSession;
import com.braintribe.model.processing.session.impl.session.collection.CollectionEnhancingPropertyAccessInterceptor;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedCollection;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.processing.async.api.AsyncCallback;

/**
 * Note regarding AOP - at this level we configure two {@link PropertyAccessInterceptor}s. They are sorted from outer to
 * the inner most, i.e. the first to handle the access is the one on top, then the one below and after the one on the
 * bottom the actual property is simply set.
 * 
 * <h4>{@link ManipulationTrackingPropertyAccessInterceptor}</h4><br>
 * get - <no effect> <br>
 * set - create the right {@link ChangeValueManipulation}
 * 
 * <h4>{@link CollectionEnhancingPropertyAccessInterceptor}</h4><br>
 * get - in case of a collection property, ensure the returned value is not <tt>null</tt> (when absent, create a
 * collection that is aware it was not loaded yet) (and mark property as not-absent). Also, invoke the setter with this
 * created value!<br>
 * set - in case of a collection property, make sure that a new {@link EnhancedCollection} is set as the actual value.
 * See the actual interceptor for more details.<br>
 *
 * Note that collection enhancing must be deeper than manipulation tracking, cause the enhancer might invoke a setter in
 * the "get" method (when ensuring returned value is not <tt>null</tt>), which we do not want to track of course.
 *
 * @see ManagedGmSession
 */
public abstract class AbstractManagedGmSession extends BasicNotifyingGmSession implements ManagedGmSession {

	private AbstractSessionQueryBuilder accessBuilder;

	private Smood backup;

	protected ModelAccessory modelAccessory;

	private IdentityCompetence identityCompetence;
	
	private final MergeIsolationWorkQueue mergeIsolationWorkQueue = new MergeIsolationWorkQueue();
	
	public AbstractManagedGmSession() {
		backup = new Smood(this, EmptyReadWriteLock.INSTANCE);
		backup.setIgnorePartitions(false);
		interceptors().with(VdEvaluation.class).add(new VdePropertyAccessInterceptor());
		interceptors().with(ManipulationTracking.class).after(VdEvaluation.class).before(CollectionEnhancer.class)
				.add(new ManipulationTrackingPropertyAccessInterceptor());
		interceptors().with(CollectionEnhancer.class).after(VdEvaluation.class).add(new CollectionEnhancingPropertyAccessInterceptor());
	}

	/**
	 * This is here to support a {@link ManagedGmSession} around an existing {@link Smood} instance (SmoodSession in SmoodAcess artifact).
	 * It would make more sense for Smood to implement the {@link ManagedGmSession} interface, but we do it this way for simplicity (less
	 * work to do for us) now. In the future, we might refactor it again.
	 * 
	 * @param ignored
	 *            no purpose, just to distinguish from the default constructor
	 */
	protected AbstractManagedGmSession(boolean ignored) {
		// make sure to call setBackup
	}
	
	protected void setBackup(Smood backup) {
		this.backup = backup;
	}

	public void setModelAccessory(ModelAccessory modelAccessory) {
		this.modelAccessory = modelAccessory;
		this.backup.setCmdResolver(getModelAccessory().getCmdResolver());
	}

	/**
	 * Sets the {@link GmMetaModel} to the this session. This is a simpler counterpart to
	 * {@link #setModelAccessory(ModelAccessory)}, which provides a model and more. Therefore, DO NOT USE THIS METHOD IF
	 * YOU ALSO USE {@link #setModelAccessory(ModelAccessory)}.
	 */
	public void setMetaModel(GmMetaModel metaModel) {
		this.backup.setMetaModel(metaModel);
	}
	
	protected IdentityCompetence getIdentityCompetence() {
		if (identityCompetence == null)
			identityCompetence = createIdentityCompetence();

		return identityCompetence;
	}

	protected IdentityCompetence createIdentityCompetence() {
		return new SmoodSupportedIdentityCompetence(this, getBackup());
	}

	public Smood getBackup() {
		return backup;
	}

	@Override
	public <T extends GenericEntity> T findEntityByGlobalId(String globalId) {
		return query().findEntity(globalId);
	}

	@Override
	public SessionQueryBuilder query() {
		if (accessBuilder == null)
			accessBuilder = new SessionQueryBuilderImpl();

		return accessBuilder;
	}

	protected ModelAccessory createModelAccessory() {
		return null;
	}

	@Override
	public ModelAccessory getModelAccessory() {
		if (modelAccessory == null)
			modelAccessory = createModelAccessory();

		return modelAccessory;
	}

	/**
	 * Looks up the instance, if not found, creates a raw and records a special "acquire" manipulation.
	 */
	@Override
	public <T extends GenericEntity> T acquire(EntityType<T> entityType, String globalId) {
		T result = findEntityByGlobalId(globalId);
		if (result == null)
			result = createForAcquire(entityType, globalId);

		AcquireManipulation manipulation = AcquireManipulation.T.create();
		manipulation.setEntity(result);
		manipulation.setEntityGlobalId(globalId);

		noticeManipulation(manipulation);
		
		return result;
	}

	private <T extends GenericEntity> T createForAcquire(EntityType<T> entityType, String globalId) {
		T entity = entityType.createRaw();
		entity.setGlobalId(globalId);
		entity.attach(this);
		backup.registerEntitySilently(entity);
		// TODO register entity on the smood, without any extra manipulations (i.e. new method, not register)
		return entity;
	}

	@Override
	public void deleteEntity(GenericEntity entity) {
		getBackup().deleteEntity(entity);
	}

	@Override
	public void deleteEntity(GenericEntity entity, DeleteMode deleteMode) {
		getBackup().deleteEntity(entity, deleteMode);
	}
	
	@Override
	public void cleanup() {
		modelAccessory = null;
		backup.close();
		backup = new Smood(this, EmptyReadWriteLock.INSTANCE);
		backup.setIgnorePartitions(false);
		identityCompetence = createIdentityCompetence();
	}

	@Override
	public ManipulationApplicationContextBuilder manipulate() {
		return new BasicManipulationApplicationContext(this);
	}

	@Deprecated
	@Override
	public ManipulationReport applyManipulation(Manipulation manipulation) throws GmSessionException {
		return manipulate().mode(ManipulationMode.REMOTE).apply(manipulation);
	}
	
	public ManipulationReport apply(Manipulation manipulation, ManipulationApplicationContext context) throws GmSessionException {
		return applyManipulation(manipulation, context);
	}

	protected ManipulationReport applyManipulation(Manipulation manipulation, ManipulationApplicationContext context) throws GmSessionException {
		ManipulationRequest manipulationRequest = ManipulationRequest.T.create();
		manipulationRequest.setManipulation(manipulation);
		
		try {
			return getBackup() //
					.apply() //
					.generateId(false) //
					.ignoreUnknownEntitiesManipulations(context.getLenience() == ManipulationLenience.ignoreOnUnknownEntity) //
					.manifestUnkownEntities(context.getLenience() == ManipulationLenience.manifestOnUnknownEntity) //
					.ignoreAbsentCollectionManipulations(context.getLenience() == ManipulationLenience.manifestOnUnknownEntity) //
					.localRequest(context.getMode() == ManipulationMode.LOCAL) //
					.request2(manipulationRequest);
			
		} catch (ModelAccessException e) {
			throw new GmSessionException("error while applying manipulation on internal backup", e);
		}
	}
	
	@Override
	public MergeBuilder merge() throws GmSessionException {
		return new MergeBuilder() {
			private boolean adoptUnexposed;
			private boolean suspendHistory;
			private boolean transferTransientProperties;
			private Function<GenericEntity, GenericEntity> envelopeFactory;

			@Override
			public MergeBuilder keepEnvelope(boolean keepEnvelope) {
				this.envelopeFactory = keepEnvelope? e -> e: null;
				return this;
			}
			
			@Override
			public MergeBuilder envelopeFactory(Function<GenericEntity, GenericEntity> envelopeFactory) {
				this.envelopeFactory = envelopeFactory;
				return this;
			}
			
			@Override
			public MergeBuilder adoptUnexposed(boolean adopt) {
				adoptUnexposed = adopt;
				return this;
			}
			
			@Override
			public MergeBuilder suspendHistory(boolean suspend) {
				this.suspendHistory = suspend;
				return this;
			}
			
			@Override
			public MergeBuilder transferTransientProperties(boolean transferTransientProperties) {
				this.transferTransientProperties = transferTransientProperties;
				return this;
			}

			@Override
			public <T> T doFor(T value) throws GmSessionException {
				ContinuableMerger<T> merger = createMerger(getIdentityCompetence(), adoptUnexposed, suspendHistory, envelopeFactory, transferTransientProperties);
				return merger.merge(value);
			}

			@Override
			public <T> void doFor(T data, AsyncCallback<T> asyncCallback) throws GmSessionException {
				ContinuableMerger<T> merger = createMerger(getIdentityCompetence(), adoptUnexposed, suspendHistory, envelopeFactory, transferTransientProperties);
				mergeIsolationWorkQueue.enqueue(merger, asyncCallback, data);
			}
		};
	}

	protected <M> ContinuableMerger<M> createMerger(IdentityCompetence identityCompetence, boolean adoptUnexposed, boolean suspendHistory,
			Function<GenericEntity, GenericEntity> envelopeFactory, boolean transferTransientProperties) {

		HistorySuspension historySuspension = (suspendHistory && this instanceof HistorySuspension) ? (HistorySuspension) this : null;
		return new ContinuableMerger<>(identityCompetence, historySuspension, adoptUnexposed, envelopeFactory, transferTransientProperties);
	}

	private class SessionQueryBuilderImpl extends AbstractSessionQueryBuilder {
		
		@Override
		public <T extends GenericEntity> T findEntity(String globalId) {
			return getBackup().findEntityByGlobalId(globalId);
		}

		@Override
		public SelectQueryExecution select(SelectQuery selectQuery) {
			return new BasicSelectQueryExecution(getBackup(), selectQuery);
		}
		
		@Override
		public SelectQueryExecution select(String selectQueryString) {
			return new StringSelectQueryExecution(getBackup(), selectQueryString);
		}

		@Override
		public EntityQueryExecution entities(EntityQuery entityQuery) {
			return new BasicEntityQueryExecution(getBackup(), entityQuery);
		}
		
		@Override
		public EntityQueryExecution entities(String entityQueryString) {
			return new StringEntityQueryExecution(getBackup(), entityQueryString);
		}

		@Override
		public PropertyQueryExecution property(PropertyQuery propertyQuery) {
			return new BasicPropertyQueryExecution(getBackup(), propertyQuery);
		}

		@Override
		public PropertyQueryExecution property(String propertyQueryString) {
			return new StringPropertyQueryExecution(getBackup(), propertyQueryString);
		}
		
		@Override
		public QueryExecution abstractQuery(Query query) {
			return new BasicQueryExecution(getBackup(), query);
		}
		
		@Override
		public QueryExecution abstractQuery(String queryString) {
			return new StringQueryExecution(getBackup(), queryString);	
		}

		@Override
		public <T extends GenericEntity> EntityAccessBuilder<T> entity(EntityReference entityReference) {
			return new EntityAccessBuilderImpl<>(entityReference);
		}
		
		@Override
		public <T extends GenericEntity> EntityAccessBuilder<T> entity(T entity) {
			return entity(entity.reference());
		}
	}

	protected class EntityAccessBuilderImpl<T extends GenericEntity> extends AbstractEntityAccessBuilder<T> {
		public EntityAccessBuilderImpl(EntityReference entityReference) {
			super(entityReference);
		}

		@Override
		public T find() throws GmSessionException {
			return getBackup().findEntity(entityReference);
		}

		@Override
		public void find(AsyncCallback<T> asyncCallback) {
			try {
				asyncCallback.onSuccess(find());
			} catch (Throwable t) {
				asyncCallback.onFailure(t);
			}
		}

		@Override
		public void require(AsyncCallback<T> asyncCallback) {
			try {
				asyncCallback.onSuccess(require());
			} catch (Throwable t) {
				asyncCallback.onFailure(t);
			}
		}
		
		@Override
		public ReferencesResponse references() throws GmSessionException {
			try {
				ReferencesRequest request = ReferencesRequest.T.create();
				request.setReference(entityReference);
				return getBackup().getReferences(request);
			} catch (ModelAccessException e) {
				throw new GmSessionException("error while determine dependencies", e);
			}
		}
		
		@Override
		public void references(AsyncCallback<ReferencesResponse> asyncCallback) {
			try {
				ReferencesResponse response = references();
				asyncCallback.onSuccess(response);
			} catch (GmSessionException e) {
				asyncCallback.onFailure(e);
			}
		}
	}

	protected static class SmoodSupportedIdentityCompetence extends AbstractIdentityCompetence {
		private final Smood backup;

		public SmoodSupportedIdentityCompetence(GmSession session, Smood backup) {
			super(session);
			this.backup = backup;
		}

		@Override
		public <T extends GenericEntity> T findExistingEntity(EntityReference entityReference) throws IdentityCompetenceException {
			return backup.findEntity(entityReference);
		}

		@Override
		public boolean wasPropertyManipulated(EntityProperty entityProperty) throws IdentityCompetenceException {
			return false;
		}

		@Override
		public boolean isPreliminarilyDeleted(EntityReference entityReference) throws IdentityCompetenceException {
			return false;
		}
	}

	private static class MergeJob<M> {
		public ContinuableMerger<M> merger;
		public AsyncCallback<M> callback;
		public M data;
		
		public MergeJob(ContinuableMerger<M> merger, AsyncCallback<M> callback, M data) {
			super();
			this.merger = merger;
			this.callback = callback;
			this.data = data;
		}
	}
	
	private class MergeIsolationWorkQueue {
		private final LinkedList<MergeJob<?>> jobs = new LinkedList<>();
		private MergeJob<Object> currentJob;
		
		public <M> void enqueue(ContinuableMerger<M> merger, AsyncCallback<M> callback, M data) {
			MergeJob<M> mergeJob = new MergeJob<>(merger, callback, data);
			jobs.add(mergeJob);
			continueWork();
		}
		
		private void continueWork() {
			if (currentJob == null && !jobs.isEmpty()) {
				currentJob = (MergeJob<Object>) jobs.removeFirst();
				currentJob.merger.merge(currentJob.data, new AsyncCallback<Object>() {
					@Override
					public void onSuccess(Object future) {
						MergeJob<Object> job = currentJob;
						currentJob = null;
						continueWork();
						job.callback.onSuccess(future);
					}
					
					@Override
					public void onFailure(Throwable t) {
						MergeJob<Object> job = currentJob;
						currentJob = null;
						continueWork();
						job.callback.onFailure(t);
					}
				});
			}
		}
	}
	
}
