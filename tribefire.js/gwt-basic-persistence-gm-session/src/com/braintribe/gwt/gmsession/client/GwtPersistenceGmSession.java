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
package com.braintribe.gwt.gmsession.client;

import static com.braintribe.utils.promise.JsPromiseCallback.promisify;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmrpc.api.client.user.ResourceSupport;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.EvaluatorBasedAccess;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.accessapi.QueryAndSelect;
import com.braintribe.model.accessapi.QueryEntities;
import com.braintribe.model.accessapi.QueryProperty;
import com.braintribe.model.accessapi.ReferencesRequest;
import com.braintribe.model.accessapi.ReferencesResponse;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.manipulation.util.HistorySuspension;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.selector.KnownUseCase;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.ResolutionContextBuilder;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.AccessAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.AccessTypeAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.UseCaseAspect;
import com.braintribe.model.processing.meta.cmd.empty.EmptyModelMdResolver;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.managed.EntityQueryExecution;
import com.braintribe.model.processing.session.api.managed.EntityQueryResultConvenience;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.managed.ManipulationReport;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.model.processing.session.api.managed.PropertyQueryExecution;
import com.braintribe.model.processing.session.api.managed.PropertyQueryResultConvenience;
import com.braintribe.model.processing.session.api.managed.SelectQueryExecution;
import com.braintribe.model.processing.session.api.managed.SelectQueryResultConvenience;
import com.braintribe.model.processing.session.api.persistence.AccessDescriptor;
import com.braintribe.model.processing.session.api.persistence.HasAccessDescriptor;
import com.braintribe.model.processing.session.api.persistence.PersistenceEntityAccessBuilder;
import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;
import com.braintribe.model.processing.session.api.resource.ResourceAccess;
import com.braintribe.model.processing.session.api.resource.ResourceAccessFactory;
import com.braintribe.model.processing.session.api.service.EnvelopeSessionAspect;
import com.braintribe.model.processing.session.impl.managed.AbstractModelAccessory;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;
import com.braintribe.model.processing.session.impl.managed.IdentityCompetence;
import com.braintribe.model.processing.session.impl.managed.QueryParserHelper;
import com.braintribe.model.processing.session.impl.managed.StaticEntityQueryResultConvenience;
import com.braintribe.model.processing.session.impl.managed.StaticPropertyQueryResultConvenience;
import com.braintribe.model.processing.session.impl.managed.StaticSelectQueryResultConvenience;
import com.braintribe.model.processing.session.impl.managed.merging.ContinuableMerger;
import com.braintribe.model.processing.session.impl.persistence.AbstractPersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.auth.BasicSessionAuthorization;
import com.braintribe.model.processing.session.impl.persistence.selectiveinfo.SelectiveInformationPropertyLoader;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.service.api.DispatchableRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.processing.async.api.JsPromise;
import com.braintribe.provider.Holder;
import com.braintribe.utils.lcd.CollectionTools2;

@SuppressWarnings("unusable-by-js")
public class GwtPersistenceGmSession extends AbstractPersistenceGmSession implements HasAccessDescriptor {
	private static Logger logger = Logger.getLogger(GwtPersistenceGmSession.class);
	protected AccessDescriptor accessDescriptor;
	private ResourceAccessFactory<? super AccessDescriptor> resourcesAccessFactory;
	private ResourceAccessFactory<? super AccessDescriptor> modelAccessoryResourcesAccessFactory;
	private ResourceAccess resourcesAccess;
	private Map<Class<? extends SelectorContextAspect<?>>, Supplier<?>> dynamicAspectProviders;
	private QueryResultConvenienceGenerator convenienceGenerator;
	private Evaluator<ServiceRequest> requestEvaluator;
	private Set<String> useCases;
	protected Supplier<String> userNameSupplier;
	protected Supplier<Set<String>> userRolesSupplier;
	protected Supplier<String> sessionIdSupplier;

	private EvaluatorBasedAccess access; // lazy initialized

	public GwtPersistenceGmSession() {
		SelectiveInformationPropertyLoader.registerFor(this);
	}

	@Override
	protected SessionQueryBuilderImpl newSessionQueryBuilder(boolean detached) {
		return new GwtPersistenceSessionQueryBuilder(detached);
	}

	public void setConvenienceGenerator(QueryResultConvenienceGenerator convenienceGenerator) {
		this.convenienceGenerator = convenienceGenerator;
	}

	public QueryResultConvenienceGenerator getConvenienceGenerator() {
		if (convenienceGenerator != null)
			return convenienceGenerator;

		convenienceGenerator = new QueryResultConvenienceGenerator() {
			@Override
			public StaticSelectQueryResultConvenience generateSelectQueryResultConvenience(SelectQuery query, SelectQueryResult result) {
				return new StaticSelectQueryResultConvenience(query, result);
			}

			@Override
			public StaticEntityQueryResultConvenience generateEntityQueryResultConvenience(EntityQuery query, EntityQueryResult result) {
				return new StaticEntityQueryResultConvenience(query, result);
			}

			@Override
			public StaticPropertyQueryResultConvenience generatePropertyQueryResultConvenience(PropertyQuery query, PropertyQueryResult result) {
				return new StaticPropertyQueryResultConvenience(query, result);
			}
		};

		return convenienceGenerator;
	}

	@Required
	public void setUserNameSupplier(Supplier<String> userNameSupplier) {
		this.userNameSupplier = userNameSupplier;
	}

	@Required
	public void setUserRolesSupplier(Supplier<Set<String>> userRolesSupplier) {
		this.userRolesSupplier = userRolesSupplier;
	}

	public void setSessionIdSupplier(Supplier<String> sessionIdSupplier) {
		this.sessionIdSupplier = sessionIdSupplier;
	}

	@Configurable
	public void setDynamicAspectProviders(Map<Class<? extends SelectorContextAspect<?>>, Supplier<?>> dynamicAspectProviders) {
		this.dynamicAspectProviders = dynamicAspectProviders;
	}

	@Configurable
	public void setUseCases(Set<String> useCases) {
		this.useCases = useCases;
	}

	@Configurable
	public void setResourcesAccessFactory(ResourceAccessFactory<? super AccessDescriptor> resourcesAccessFactory) {
		this.resourcesAccessFactory = resourcesAccessFactory;
	}

	@Configurable
	public void setModelAccessoryResourcesAccessFactory(ResourceAccessFactory<? super AccessDescriptor> modelAccessoryResourcesAccessFactory) {
		this.modelAccessoryResourcesAccessFactory = modelAccessoryResourcesAccessFactory;
	}

	@Configurable
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}

	@Override
	protected Evaluator<ServiceRequest> getRequestEvaluator() {
		return requestEvaluator;
	}

	public void configureAccessDescriptor(AccessDescriptor accessDescriptor) {
		cleanup();
		this.accessDescriptor = accessDescriptor;
	}

	@Override
	public void cleanup() {
		super.cleanup();
		this.accessDescriptor = null;
	}

	public void configureAccessDescriptor(AccessDescriptor accessDescriptor, AsyncCallback<Void> asyncCallback) {
		try {
			configureAccessDescriptor(accessDescriptor);
			asyncCallback.onSuccess(null);
		} catch (Throwable t) {
			asyncCallback.onFailure(t);
		}
	}

	@Override
	public AccessDescriptor getAccessDescriptor() {
		return accessDescriptor;
	}

	@Override
	protected ModelAccessory createModelAccessory() {
		return new GwtPersistenceSessionModelAccessory();
	}

	@Override
	protected <M> ContinuableMerger<M> createMerger(IdentityCompetence identityCompetence, boolean adoptUnexposed, boolean suspendHistory,
			Function<GenericEntity, GenericEntity> envelopeFactory, boolean transferTransientProperties) {

		HistorySuspension historySuspension = (suspendHistory) ? this : null;
		return new GwtContinuableMerger<>(identityCompetence, historySuspension, adoptUnexposed, envelopeFactory, transferTransientProperties);
	}

	@Override
	protected void commit(ManipulationRequest manipulationRequest, AsyncCallback<ManipulationResponse> callback) {
		try {
			if(manipulationRequest == null) {
				callback.onSuccess(ManipulationResponse.T.create());
				return;
			}
				
			fireOnBeforeCommit(manipulationRequest.getManipulation());
	
			com.google.gwt.user.client.rpc.AsyncCallback<ManipulationResponse> adapterCallback = AsyncCallbacks.of( //
					manipulationResponse -> {
						try {
							ManipulationReport report = processManipulationResponse(manipulationResponse);
	
							if (!manifestUnknownEntities || report == null) {
								handleManipulationResponse(manipulationRequest, callback, manipulationResponse);
								return;
							}
	
							refreshManifestedEntities(report.getLenientManifestations(), AsyncCallbacks.of( //
									result -> handleManipulationResponse(manipulationRequest, callback, manipulationResponse),
									e -> callback.onFailure(new GmSessionException("error while refreshing manifested entities", e))));
						} catch (GmSessionException e) {
							callback.onFailure(e);
						}
					}, e -> callback.onFailure(new GmSessionException("error while committing manipulations to persistence", e)));
	
			sendManipulationRequest(manipulationRequest, adapterCallback);
		}catch(Exception ex) {
			callback.onFailure(ex);
		}
	}

	private void handleManipulationResponse(ManipulationRequest manipulationRequest, AsyncCallback<ManipulationResponse> callback,
			ManipulationResponse manipulationResponse) {
		callback.onSuccess(manipulationResponse);
		fireOnAfterCommit(manipulationRequest.getManipulation(), manipulationResponse.getInducedManipulation());
	}

	private void refreshManifestedEntities(Map<EntityType<?>, Set<GenericEntity>> manifestations,
			com.google.gwt.user.client.rpc.AsyncCallback<Void> callback) {

		List<EntityQuery> queries = prepareRefreshQueries(manifestations);
		final Iterator<EntityQuery> it = queries.iterator();

		sendNextEntityQuery(it, callback);
	}

	private void sendNextEntityQuery(final Iterator<EntityQuery> it, final com.google.gwt.user.client.rpc.AsyncCallback<Void> callback) {
		if (!it.hasNext()) {
			callback.onSuccess(null);
			return;
		}

		AsyncCallback<EntityQueryResultConvenience> queryCallback = AsyncCallback.of( //
				result -> sendNextEntityQuery(it, callback), //
				callback::onFailure);

		query().entities(it.next()).result(queryCallback);
	}

	@SuppressWarnings("unusable-by-js")
	protected class GwtPersistenceSessionQueryBuilder extends SessionQueryBuilderImpl {

		public GwtPersistenceSessionQueryBuilder(boolean detached) {
			super(detached);
		}

		@Override
		public SelectQueryExecution select(SelectQuery selectQuery) {
			return new GwtSelectQueryExecutionImpl(selectQuery, detached);
		}

		@Override
		public SelectQueryExecution select(String selectQueryString) {
			return new GwtSelectQueryExecutionImpl(QueryParserHelper.parseSelectQuery(selectQueryString), detached);
		}

		@Override
		public EntityQueryExecution entities(EntityQuery queryCandidate) {
			return new GwtEntityQueryExecutionImpl(queryCandidate, detached);
		}

		@Override
		public EntityQueryExecution entities(String entityQueryString) {
			return new GwtEntityQueryExecutionImpl(QueryParserHelper.parseEntityQuery(entityQueryString), detached);
		}

		@Override
		public PropertyQueryExecution property(PropertyQuery propertyQuery) {
			return new GwtPropertyQueryExecutionImpl(propertyQuery, detached);
		}

		@Override
		public PropertyQueryExecution property(String propertyQueryString) {
			return new GwtPropertyQueryExecutionImpl(QueryParserHelper.parsePropertyQuery(propertyQueryString), detached);
		}

		@Override
		public <T extends GenericEntity> PersistenceEntityAccessBuilder<T> entity(EntityReference entityReference) {
			return new GwtPersistenceEntityAccessBuilder<>(entityReference, detached);
		}
	}

	protected class GwtPersistenceEntityAccessBuilder<T extends GenericEntity> extends PersistentEntityAccessBuilder<T> {
		public GwtPersistenceEntityAccessBuilder(EntityReference entityReference, boolean detached) {
			super(entityReference, detached);
		}

		@Override
		public void find(final AsyncCallback<T> asyncCallback) {
			get(false, asyncCallback);
		}

		public void get(final boolean require, final AsyncCallback<T> asyncCallback) {
			try {
				T entity = detached ? null : findInCache();

				if (entity != null || !(entityReference instanceof PersistentEntityReference)) {
					asyncCallback.onSuccess(entity);
					return;
				}

				EntityQuery query = createEntityQuery();

				com.google.gwt.user.client.rpc.AsyncCallback<EntityQueryResult> adapterCallback = AsyncCallbacks.of( //
						result -> {
							try {
								postProcess(result, AsyncCallback.of( //
										queryResult -> {
											List<T> list = (List<T>) queryResult.getEntities();

											switch (list.size()) {
												case 0:
													if (require) {
														asyncCallback.onFailure(new NotFoundException("could not find "
																+ entityReference.getTypeSignature() + "(id=" + entityReference.getRefId() + ")."));
													} else
														asyncCallback.onSuccess(null);
													break;
												case 1:
													asyncCallback.onSuccess(list.get(0));
													break;
												default:
													asyncCallback.onFailure(
															new GmSessionException("ambiguous results for " + entityReference.getTypeSignature()
																	+ "(id=" + entityReference.getRefId() + "). Ids should be unique."));
													break;
											}
										}, e -> asyncCallback.onFailure(new GmSessionException("error while post processing entityQueryResult", e))),
										detached);
							} catch (Exception e) {
								asyncCallback.onFailure(new GmSessionException("error while querying entities from access", e));
							}
						}, asyncCallback::onFailure);

				sendEntityQuery(query, adapterCallback);
			} catch (GmSessionException e) {
				asyncCallback.onFailure(e);
			}
		}

		@Override
		public void refresh(final AsyncCallback<T> asyncCallback) {
			EntityQuery query = createEntityQuery();

			com.google.gwt.user.client.rpc.AsyncCallback<EntityQueryResult> adapterCallback = AsyncCallbacks.of( //
					result -> {
						try {
							postProcess(result, AsyncCallback.of( //
									queryResult -> {
										List<T> list = (List<T>) queryResult.getEntities();

										switch (list.size()) {
											case 0:
												asyncCallback.onFailure(new NotFoundException("could not find " + entityReference.getTypeSignature()
														+ "(id=" + entityReference.getRefId() + ")."));
												break;
											case 1:
												asyncCallback.onSuccess(list.get(0));
												break;
											default:
												asyncCallback.onFailure(
														new GmSessionException("ambiguous results for " + entityReference.getTypeSignature() + "(id="
																+ entityReference.getRefId() + "). Ids should be unique."));
												break;
										}
									}, e -> asyncCallback.onFailure(new GmSessionException("error while post processing entityQueryResult", e))),
									detached);
						} catch (Exception e) {
							asyncCallback.onFailure(new GmSessionException("error while querying entities from access", e));
						}
					}, asyncCallback::onFailure);

			sendEntityQuery(query, adapterCallback);
		}

		@Override
		public void references(final AsyncCallback<ReferencesResponse> asyncCallback) {
			ReferencesRequest request = ReferencesRequest.T.create();
			request.setReference(entityReference);

			com.google.gwt.user.client.rpc.AsyncCallback<ReferencesResponse> adapterCallback = AsyncCallbacks.of( //
					asyncCallback::onSuccess, asyncCallback::onFailure);

			sendReferencesRequest(request, adapterCallback);
		}

		@Override
		public void require(AsyncCallback<T> asyncCallback) {
			get(true, asyncCallback);
		}
	}

	protected class GwtPropertyQueryExecutionImpl extends PropertyQueryExecutionImpl {
		public GwtPropertyQueryExecutionImpl(PropertyQuery propertyQuery, boolean detached) {
			super(propertyQuery, detached);
		}

		@Override
		public void result(final AsyncCallback<PropertyQueryResultConvenience> callback) {
			com.google.gwt.user.client.rpc.AsyncCallback<PropertyQueryResult> adapterCallback = AsyncCallbacks.of( //
					result -> {
						try {
							postProcess(result, AsyncCallback.of( //
									future -> {
										StaticPropertyQueryResultConvenience resultConvenience = getConvenienceGenerator()
												.generatePropertyQueryResultConvenience(getQuery(), future);
										callback.onSuccess(resultConvenience);
									}, callback::onFailure), detached);
						} catch (GmSessionException e) {
							callback.onFailure(e);
						}
					}, callback::onFailure);

			sendPropertyQuery(getQuery(), adapterCallback);
		}
	}

	protected class GwtEntityQueryExecutionImpl extends EntityQueryExecutionImpl {

		public GwtEntityQueryExecutionImpl(EntityQuery entityQuery, boolean detached) {
			super(entityQuery, detached);
		}

		@Override
		public void result(final AsyncCallback<EntityQueryResultConvenience> callback) {
			com.google.gwt.user.client.rpc.AsyncCallback<EntityQueryResult> adapterCallback = AsyncCallbacks.of( //
					result -> {
						try {
							postProcess(result, AsyncCallback.of(future -> {
								StaticEntityQueryResultConvenience resultConvenience = getConvenienceGenerator()
										.generateEntityQueryResultConvenience(getQuery(), future);
								callback.onSuccess(resultConvenience);
							}, callback::onFailure), detached);
						} catch (GmSessionException e) {
							callback.onFailure(e);
						}
					}, callback::onFailure);

			sendEntityQuery(getQuery(), adapterCallback);
		}
	}

	protected class GwtSelectQueryExecutionImpl extends SelectQueryExecutionImpl {
		public GwtSelectQueryExecutionImpl(SelectQuery selectQuery, boolean detached) {
			super(selectQuery, detached);
		}

		@Override
		public void result(final AsyncCallback<SelectQueryResultConvenience> callback) {
			com.google.gwt.user.client.rpc.AsyncCallback<SelectQueryResult> adapterCallback = AsyncCallbacks.of( //
					result -> {
						try {
							postProcess(result, AsyncCallback.of( //
									future -> {
										StaticSelectQueryResultConvenience resultConvenience = getConvenienceGenerator()
												.generateSelectQueryResultConvenience(getQuery(), future);
										callback.onSuccess(resultConvenience);
									}, callback::onFailure), detached);
						} catch (GmSessionException e) {
							callback.onFailure(e);
						}
					}, callback::onFailure);

			sendSelectQuery(getQuery(), adapterCallback);
		}
	}

	public static Set<String> defaultGmeUseCase() {
		return CollectionTools2.asSet(KnownUseCase.gmeGlobalUseCase.getDefaultValue());
	}

	@SuppressWarnings("unusable-by-js")
	private class GwtPersistenceSessionModelAccessory extends AbstractModelAccessory {
		private CmdResolver cascadingMetaDataResolver;
		private BasicManagedGmSession modelSession;

		@Override
		public CmdResolver getCmdResolver() {
			if (cascadingMetaDataResolver != null)
				return cascadingMetaDataResolver;

			ResolutionContextBuilder rcb = new ResolutionContextBuilder(getOracle());
			rcb.addDynamicAspectProviders(dynamicAspectProviders);
			rcb.addDynamicAspectProvider(UseCaseAspect.class, GwtPersistenceGmSession::defaultGmeUseCase);
			rcb.setSessionProvider(new Holder<>(new Object()));
			rcb.addStaticAspect(AccessAspect.class, accessDescriptor.accessId());
			rcb.addStaticAspect(AccessTypeAspect.class, accessDescriptor.accessDenotationType());
			rcb.addStaticAspect(UseCaseAspect.class, useCases);

			cascadingMetaDataResolver = new CmdResolverImpl(rcb.build());

			return cascadingMetaDataResolver;
		}

		@Override
		public ModelMdResolver getMetaData() {
			if (accessDescriptor == null || accessDescriptor.dataModel() == null)
				return EmptyModelMdResolver.INSTANCE;

			return getCmdResolver().getMetaData();
		}

		@Override
		public ManagedGmSession getModelSession() {
			if (modelSession != null)
				return modelSession;

			modelSession = new BasicManagedGmSession();
			modelSession.setResourcesAccessFactory(session -> modelAccessoryResourcesAccessFactory.newInstance(accessDescriptor));
			modelSession.setModelAccessory(this);

			try {
				modelSession.merge().adoptUnexposed(true).doFor(accessDescriptor.dataModel());
			} catch (GmSessionException e) {
				logger.error("error while filling model session of ModelAccessory", e);
			}

			return modelSession;
		}

		@Override
		public GmMetaModel getModel() {
			return accessDescriptor == null ? null : accessDescriptor.dataModel();
		}

		@Override
		public ModelOracle getOracle() {
			if (modelOracle == null)
				modelOracle = new BasicModelOracle(getModel());

			return modelOracle;
		}
	}

	protected ResourceAccess getResourcesAccess() {
		if (resourcesAccess == null && resourcesAccessFactory != null) {
			resourcesAccess = resourcesAccessFactory.newInstance(accessDescriptor);
		}

		return resourcesAccess;
	}

	@SuppressWarnings("unusable-by-js")
	@Override
	public ResourceAccess resources() {
		ResourceAccess builder = getResourcesAccess();
		if (builder != null)
			return builder;
		else
			throw new UnsupportedOperationException("no resource access configured for the session");
	}

	@Override
	public String getAccessId() {
		return accessDescriptor == null ? null : accessDescriptor.accessId();
	}

	// Originally in sub-type

	protected void sendManipulationRequest(ManipulationRequest request, com.google.gwt.user.client.rpc.AsyncCallback<ManipulationResponse> callback) {
		evalAsync(callback, request);
	}

	protected void sendEntityQuery(EntityQuery query, com.google.gwt.user.client.rpc.AsyncCallback<EntityQueryResult> callback) {
		QueryEntities request = QueryEntities.T.create();
		request.setQuery(query);
		request.setServiceId(accessDescriptor.accessId());

		evalAsync(callback, request);
	}

	protected void sendPropertyQuery(PropertyQuery query, com.google.gwt.user.client.rpc.AsyncCallback<PropertyQueryResult> callback) {
		QueryProperty request = QueryProperty.T.create();
		request.setQuery(query);
		evalAsync(callback, request);
	}

	protected void sendSelectQuery(SelectQuery query, com.google.gwt.user.client.rpc.AsyncCallback<SelectQueryResult> callback) {
		if (accessDescriptor != null) {
			QueryAndSelect request = QueryAndSelect.T.create();
			request.setQuery(query);
			evalAsync(callback, request);
		} else
			callback.onFailure(new IllegalStateException("no access descriptor set"));
	}

	protected void sendReferencesRequest(ReferencesRequest request, com.google.gwt.user.client.rpc.AsyncCallback<ReferencesResponse> callback) {
		evalAsync(callback, request);
	}

	@Override
	protected IncrementalAccess getIncrementalAccess() {
		if (access == null)
			access = new EvaluatorBasedAccess(accessId, requestEvaluator);

		return access;
	}

	@SuppressWarnings("unusable-by-js")
	@Override
	public SessionAuthorization getSessionAuthorization() {
		BasicSessionAuthorization sa = new BasicSessionAuthorization();
		if (userNameSupplier != null && userRolesSupplier != null) {
			sa.setUserName(userNameSupplier.get());
			sa.setUserRoles(userRolesSupplier.get());
		}
		if (sessionIdSupplier != null)
			sa.setSessionId(sessionIdSupplier.get());
		return sa;
	}

	private <T> void evalAsync(com.google.gwt.user.client.rpc.AsyncCallback<T> callback, DispatchableRequest request) {
		request.setServiceId(accessDescriptor.accessId());

		Future<T> future = new Future<>();
		future.load(callback);

		EvalContext<T> evalContext = (EvalContext<T>) request.eval(getRequestEvaluator());
		evalContext.get(future);
	}

	@Override
	public JsPromise<ManipulationResponse> commitAsync() {
		return promisify(super::commit);
	}

	@Override
	public JsPromise<Object> evalAsync(ServiceRequest sr) {
		return promisify(eval(sr).with(ResourceSupport.class, true).with(EnvelopeSessionAspect.class, this)::get);
	}

}
