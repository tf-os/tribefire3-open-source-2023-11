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
package com.braintribe.gwt.gme.constellation.client;

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.localEntityProperty;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.GmContentContext;
import com.braintribe.gwt.gmview.client.GmContentSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmEntityView;
import com.braintribe.gwt.gmview.client.GmListView;
import com.braintribe.gwt.gmview.client.GmSelectionCount;
import com.braintribe.gwt.gmview.client.GmTemplateMetadataViewSupport;
import com.braintribe.gwt.gmview.client.GmViewChangeListener;
import com.braintribe.gwt.gmview.client.GmViewport;
import com.braintribe.gwt.gmview.client.ReloadableGmView;
import com.braintribe.gwt.gmview.ddsarequest.client.DdsaRequestExecution;
import com.braintribe.gwt.gmview.ddsarequest.client.DdsaRequestExecution.RequestExecutionData;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedBorderLayoutContainer;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.gwt.logging.client.Profiling;
import com.braintribe.gwt.logging.client.ProfilingHandle;
import com.braintribe.gwt.qc.api.client.QueryProviderContext;
import com.braintribe.gwt.qc.api.client.QueryProviderView;
import com.braintribe.gwt.qc.api.client.QueryProviderViewListener;
import com.braintribe.model.extensiondeployment.RequestProcessing;
import com.braintribe.model.extensiondeployment.meta.DisplayCount;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.ListType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SetType;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.meta.data.prompt.AutoExpand;
import com.braintribe.model.meta.data.prompt.ColumnDisplay;
import com.braintribe.model.meta.data.prompt.Singleton;
import com.braintribe.model.processing.query.api.stringifier.QuerySelection;
import com.braintribe.model.processing.query.selection.BasicQuerySelectionResolver;
import com.braintribe.model.processing.query.selection.experts.NameAliasResolver;
import com.braintribe.model.processing.session.api.managed.EntityQueryResultConvenience;
import com.braintribe.model.processing.session.api.managed.PropertyQueryResultConvenience;
import com.braintribe.model.processing.session.api.managed.SelectQueryResultConvenience;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.TransientPersistenceGmSession;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedList;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedSet;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.template.Template;
import com.braintribe.model.uiservice.CountData;
import com.braintribe.model.uiservice.GetCount;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;

/**
 * Constellation which wires up a {@link GmContentView} and a {@link QueryProviderView} together.
 * @author michel.docouto
 *
 */
public class QueryConstellation extends QueryResultsHandlerView
		implements InitializableBean, GmEntityView, DisposableBean, GmSelectionCount, ReloadableGmView {
	private static GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	
	protected static final Logger logger = new Logger(QueryConstellation.class);
	
	private QueryProviderView<GenericEntity> queryProviderView;
	private BorderLayoutData northData;
	private int smallNorthDataSize = 65;
	private Widget currentTopWidget;
	private Set<GenericEntity> entitiesToIgnore;
	private EntityType<?> entityType;
	private GmViewChangeListener viewChangeListener;
	private boolean showQueryView = true;
	private BorderLayoutContainer wrapperNorthPanel;
	private GenericEntity entity;
	private ModelPath rootModelPath;
	private boolean hasMoreDataToLoad = false;
	private boolean performingSearch = false;
	private QueryProviderViewListener queryProviderViewListener;
	private int maxSelectionCount = 1;
	private BasicQuerySelectionResolver basicQuerySelectionResolver;
	private Query lastPerformedQuery;
	private boolean initialized = false;
	private int currentStartIndex;
	private String currentBasicText;
	private boolean currentlyShowingAdvancedMode;
	private boolean isCurrentQueryAdvanced;
	private TransientGmSession transientSession;
	private Supplier<? extends TransientPersistenceGmSession> transientSessionSupplier;
	private Map<EntityType<?>, TraversingCriterion> specialEntityTraversingCriterionMap;
	private boolean performingReload = false;
	
	public QueryConstellation() {
		setBorders(false);
		
		addAttachHandler(event -> {
			if (!event.isAttached())
				queryProviderView.hideForm();
		});
	}
	
	/**
	 * Configures the required {@link QueryProviderView} with which we may edit our Query or Template.
	 */
	@SuppressWarnings("rawtypes")
	@Required
	public void setQueryProviderView(QueryProviderView<? extends GenericEntity> queryProviderView) {
		this.queryProviderView = (QueryProviderView) queryProviderView;
	}
	
	/**
	 * Configures the required {@link TransientGmSession}.
	 */
	@Required
	public void setTransientSession(TransientGmSession transientSession) {
		this.transientSession = transientSession;
	}
	
	/**
	 * Configures the required supplier for {@link TransientGmSession}.
	 */
	@Required
	public void setTransientSessionSupplier(Supplier<? extends TransientPersistenceGmSession> transientSessionSupplier) {
		this.transientSessionSupplier = transientSessionSupplier;
	}
	
	/**
	 * Configures whether the view configured via {@link #setQueryProviderView(QueryProviderView)} should be shown.
	 * Defaults to true.
	 */
	@Configurable
	public void setShowQueryView(boolean showQueryView) {
		this.showQueryView = showQueryView;
	}
	
	/**
	 * Configures entities which have a special TC. It only set when the Query restriction is a value comparison.
	 */
	@Configurable
	public void setSpecialEntityTraversingCriterionMap(Map<EntityType<?>, TraversingCriterion> specialEntityTraversingCriterionMap) {
		this.specialEntityTraversingCriterionMap = specialEntityTraversingCriterionMap;
	}
	
	/**
	 * Set the size of the query editor. Defaults to 65px.
	 */
	@Configurable
	public void setSmallNorthDataSize(int smallNorthDataSize) {
		this.smallNorthDataSize = smallNorthDataSize;
	}
		
	@Override
	public void setMaxSelectCount(int maxSelectionCount) {
		this.maxSelectionCount = maxSelectionCount;
	}
	
	@Override
	public void reloadGmView() {
		performingReload = true;
		performSearch();
		reloadPending = false;
	}
	
	/**
	 * Configures a set of entities to be ignored (excluded) from the search results, in case of EntityQuery.
	 */
	public void configureEntitiesToIgnore(Set<GenericEntity> entitiesToIgnore) {
		this.entitiesToIgnore = entitiesToIgnore;
	}
	
	/**
	 * Configures the required {@link PersistenceGmSession}.
	 */
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		//If this is called prior to initialization, postpone it.
		if (!initialized) {
			Scheduler.get().scheduleDeferred(() -> configureGmSession(gmSession));
			return;
		}
		
		this.gmSession = gmSession;
		
		if (queryProviderView != null)
			queryProviderView.configureGmSession(gmSession);
		
		exchangeCurrentContentView(getDefaultContentView(), null);
		setCenterWidget(wrapperPanel);
		
		queryProviderView.addQueryProviderViewListener(getQueryProviderViewListener());
	}
	
	@Override
	public void intializeBean() throws Exception {
		wrapperPanel = new ExtendedBorderLayoutContainer();
		wrapperPanel.getElement().getStyle().setBackgroundColor("white");
		wrapperPanel.setBorders(false);
		
		northData = new BorderLayoutData(smallNorthDataSize);
		northData.setMaxSize(1000);
		if (showQueryView) {
			wrapperNorthPanel = new ExtendedBorderLayoutContainer();
			wrapperNorthPanel.addStyleName(ConstellationResources.INSTANCE.css().queryConstellationNorth());
			wrapperNorthPanel.getElement().getStyle().setBackgroundColor("white");
			wrapperNorthPanel.setBorders(false);
			
			currentTopWidget = queryProviderView.getWidget();
			wrapperNorthPanel.setCenterWidget(currentTopWidget);
			wrapperPanel.setNorthWidget(wrapperNorthPanel, northData);
		}
		
		initialized = true;
	}
	
	/**
	 * Configures the current top widget.
	 */
	private void configureCurrentTopWidget(Widget currentTopWidget) {
		if (this.currentTopWidget != null)
			wrapperNorthPanel.remove(this.currentTopWidget);
		this.currentTopWidget = currentTopWidget;
		if (currentTopWidget != null)
			wrapperNorthPanel.setCenterWidget(currentTopWidget);
		doLayout();
	}
	
	public QueryProviderView<? extends GenericEntity> getQueryProviderView() {
		return queryProviderView;
	}
	
	/**
	 * Hides the query editor.
	 */
	public void hideQueryEditor() {
		northData.setSize(0);
		wrapperNorthPanel.getElement().getStyle().setVisibility(Visibility.HIDDEN);
	}
	
	/**
	 * Restores the visibility of the query editor.
	 */
	public void restoreQueryEditor() {
		northData.setSize(smallNorthDataSize);
		wrapperNorthPanel.getElement().getStyle().clearVisibility();
	}
	
	/**
	 * Exchanges the QueryProviderView used for editing a Query or Template.
	 */
	public void exchangeCurrentQueryProviderView(QueryProviderView<GenericEntity> queryProviderView, boolean advanced) {
		if (queryProviderView == this.queryProviderView || queryProviderView == null)
			return;
		
		if (this.queryProviderView != null && queryProviderViewListener != null)
			this.queryProviderView.removeQueryProviderViewListener(queryProviderViewListener);
		
		queryProviderView.configureGmSession(gmSession);
		
		queryProviderView.addQueryProviderViewListener(getQueryProviderViewListener());
		
		GenericEntity entityContent;
		if (advanced)
			entityContent = this.queryProviderView.getQueryProviderContext().getQuery(true);
		else {
			entityContent = queryProviderView.getQueryProviderContext().getTemplate();
			if (entityContent == null)
				entityContent = queryProviderView.getQueryProviderContext().getQuery(true);
		}
		
		queryProviderView.setEntityContent(entityContent);
		
		if (showQueryView) {
			configureCurrentTopWidget(queryProviderView.getWidget());
			forceLayout();
		}
		
		queryProviderView.modeQueryProviderViewChanged();

		this.queryProviderView = queryProviderView;
	}
	
	private QueryProviderViewListener getQueryProviderViewListener() {
		if (queryProviderViewListener != null)
			return queryProviderViewListener;
		
		queryProviderViewListener = new QueryProviderViewListener() {
			@Override
			public void onQueryPerform(QueryProviderContext queryProviderContext) {
				performSearch(queryProviderContext);
				if (!currentlyShowingAdvancedMode)
					currentBasicText = queryProviderView.getCurrentQueryText();
				
				isCurrentQueryAdvanced = currentlyShowingAdvancedMode;
			}
			
			@Override
			public void configureEntityType(EntityType<?> entityType, boolean configureEntityTypeForCheck) {
				QueryConstellation.this.configureEntityType(entityType, false);
			}
			
			@Override
			public void onModeChanged(QueryProviderView<GenericEntity> newQueryProviderView, boolean advanced) {
				exchangeCurrentQueryProviderView(newQueryProviderView, advanced);
				currentlyShowingAdvancedMode = advanced;
			}
		};
		
		return queryProviderViewListener;
	}
	
	private void prepareColumnData(String entityTypeSignature) {
		if (!(currentContentView instanceof GmTemplateMetadataViewSupport))
			return;
		
		ColumnDisplay columnDisplay = null;
		
		if (entity instanceof Template) {
			Template template = (Template) entity;
			columnDisplay = GMEMetadataUtil.getTemplateMetaData(template, ColumnDisplay.T, com.braintribe.model.workbench.meta.ColumnDisplay.T);
			
			if (columnDisplay != null)
				logger.debug("ColumnDisplay received from the template");
		}
		
		if (columnDisplay == null && entityTypeSignature != null) {
			columnDisplay = gmSession.getModelAccessory().getMetaData().entityTypeSignature(entityTypeSignature).meta(ColumnDisplay.T).exclusive();
			
			if (columnDisplay != null)
				logger.debug("ColumnDisplay received from the entityType: " + entityTypeSignature);
		}
		
		((GmTemplateMetadataViewSupport) currentContentView).setColumnData(GMEMetadataUtil.prepareColumnData(columnDisplay));
	}
	
	private DisplayCount getDisplayCount(String entityTypeSignature) {
		DisplayCount displayCount = null;
		if (entity instanceof Template) {
			Template template = (Template) entity;
			displayCount = GMEMetadataUtil.getTemplateMetaData(template, DisplayCount.T, null);
		}
		
		if (displayCount == null && entityTypeSignature != null)
			displayCount = gmSession.getModelAccessory().getMetaData().entityTypeSignature(entityTypeSignature).meta(DisplayCount.T).exclusive();
		
		return displayCount;
	}
	
	private void prepareAutoExpand(String entityTypeSignature) {
		if (!(currentContentView instanceof GmTemplateMetadataViewSupport))
			return;
		
		AutoExpand autoExpand = null;
		
		if (entity instanceof Template) {
			Template template = (Template) entity;
			autoExpand = GMEMetadataUtil.getTemplateMetaData(template, AutoExpand.T, null);
		}
		
		if (autoExpand == null && entityTypeSignature != null)
			autoExpand = gmSession.getModelAccessory().getMetaData().entityTypeSignature(entityTypeSignature).meta(AutoExpand.T).exclusive();
		
		((GmTemplateMetadataViewSupport) currentContentView).setAutoExpand(autoExpand);
	}
	
	/**
	 * Executes the current query.
	 */
	public void performSearch() {
		performSearch(queryProviderView.getQueryProviderContext());
	}
	
	public String getCurrentBasicText() {
		return currentBasicText;
	}

	/**
	 * Returns true if the latest query was executed while having the advanced mode on.
	 */
	public boolean isCurrentQueryAdvanced() {
		return isCurrentQueryAdvanced;
	}
	
	protected boolean isFormAvailable() {
		return queryProviderView.isFormAvailable();
	}
	
	protected void showForm() {
		queryProviderView.showForm();
	}

	/***
	 * Perform a search with no loaded query, so getQuery() of the queryProviderContext will be called.
	 */
	private void performSearch(QueryProviderContext queryProviderContext) {
		performSearch(queryProviderContext, null);
	}

	/***
	 * Perform a search with a already loaded query to prevent multiple getQuery() calls of the queryProviderContext.
	 */
	private void performSearch(final QueryProviderContext queryProviderContext, final Query loadedContextQuery) {
		boolean initialQuery = (loadedContextQuery == null);
		Query query = loadedContextQuery;

		if (initialQuery)
			query = prepareInitialQuery(queryProviderContext, initialQuery, false);

		if (query == null) {
			//GlobalState.showWarning(LocalizedText.INSTANCE.invalidQueryMessage());
			return;
		}
		
		if (performingReload) {
			if (query.getRestriction() != null && query.getRestriction().getPaging() != null) {
				int selectedIndex = currentContentView.getFirstSelectedIndex();
				int pageSize = query.getRestriction().getPaging().getPageSize();
				int newSize = ((selectedIndex / pageSize) + 1) * pageSize;
				query.getRestriction().getPaging().setPageSize(newSize);
			}
		}
		
		lastPerformedQuery = query;
		if (query instanceof EntityQuery)
			performEntityQuery(queryProviderContext, initialQuery, query);
		else if (query instanceof PropertyQuery)
			performPropertyQuery(queryProviderContext, initialQuery, query);
		else if (query instanceof SelectQuery)
			performSelectQuery(queryProviderContext, initialQuery, query);
		
		if (performingReload)
			performingReload = false;
	}

	private void performSelectQuery(QueryProviderContext queryProviderContext, boolean initialQuery, Query query) {
		SelectQuery selectQuery = (SelectQuery) query;
		
		GenericModelType collectionElementType = changeViewForSelectQuery(selectQuery);

		if (initialQuery)
			GlobalState.mask(LocalizedText.INSTANCE.performingSelectQuery());

		performingSearch = true;
		ProfilingHandle ph = Profiling.start(getClass(), "Performing select query", true);
		gmSession.query().select(selectQuery).result(AsyncCallback.of(future -> {
			ph.stop();
			
			if (disposed || lastPerformedQuery != selectQuery)
				return; // Ignoring result from a previous query
			
			try {
				handleSelectQueryResult(queryProviderContext, initialQuery, collectionElementType, selectQuery, future);
			} catch (Exception e) {
				handleSelectQueryFailure(e, ph);
			}
		}, e -> handleSelectQueryFailure(e, ph)));
	}
	
	private void handleSelectQueryFailure(Throwable e, ProfilingHandle ph) {
		ph.stop();
		QueryConstellation.this.hasMoreDataToLoad = false;
		QueryConstellation.this.performingSearch = false;

		GlobalState.unmask();
		GlobalState.showError(LocalizedText.INSTANCE.errorPerformingSelectQuery(), e);
		e.printStackTrace();
	}
	
	private void handleSelectQueryResult(QueryProviderContext queryProviderContext, boolean initialQuery, GenericModelType collectionElementType,
			SelectQuery selectQuery, SelectQueryResultConvenience future) throws GmSessionException {
		SelectQueryResult result = future.result();
		hasMoreDataToLoad = result != null ? result.getHasMore() : false;

		GlobalState.unmask();

		queryProviderView.notifyQueryPerformed(result, queryProviderContext);
		CollectionType listType = typeReflection.getListType(collectionElementType);
		List<Object> list;
		if (result == null)
			list = Collections.emptyList();
		else {
			boolean skipCheckEntitiesToIgnore = entitiesToIgnore == null;
			list = result.getResults().stream().filter(object -> skipCheckEntitiesToIgnore || !entitiesToIgnore.contains(object))
					.collect(Collectors.toList());
		}

		ModelPath modelPath = new ModelPath();
		modelPath.add(new RootPathElement(listType, list));

		if (initialQuery) {
			if (!(currentContentView instanceof GmListView))
				currentContentView.setContent(modelPath);
			else {
				if (currentContentView instanceof GmSelectionCount)
					((GmSelectionCount) currentContentView).setMaxSelectCount(maxSelectionCount);

				//Setting null here means that we have no parent element.
				currentContentView.setContent(null);
				((GmListView) currentContentView).addContent(modelPath);
			}

			if (!list.isEmpty())
				selectFirstResult();
			
			handleDisplayCount(selectQuery);
		} else if (currentContentView instanceof GmListView) {
			if (currentContentView instanceof GmSelectionCount)
				((GmSelectionCount) currentContentView).setMaxSelectCount(maxSelectionCount);

			((GmListView) currentContentView).addContent(modelPath);
		}
		
		prepareGmContentContext(queryProviderContext, initialQuery);

		performingSearch = false;
		if (queryProviderContext.isAutoPageEnabled() && currentContentView instanceof GmViewport)
			handleViewPortWindowChanged(((GmViewport) currentContentView), initialQuery);
	}
	
	private void handleDisplayCount(Query query) {
		queryProviderView.setDisplayCountText(null);
		DisplayCount displayCount = getDisplayCount(getEntityTypeSignature(query));
		if (displayCount == null)
			return;
		
		RequestProcessing requestProcessing = displayCount.getRequestProcessing();
		
		GetCount request = GetCount.T.create();
		request.setQuery(query);
		String domainId = GMEUtil.getDomainId(requestProcessing, gmSession);
		String serviceId = GMEUtil.getServiceId(requestProcessing);
		request.setDomainId(domainId);
		request.setServiceId(serviceId);
		
		RequestExecutionData data = new RequestExecutionData(request, gmSession, transientSession, null, transientSessionSupplier, null);
		
		Future<CountData> execution = DdsaRequestExecution.executeRequest(data);
		execution.andThen(countData -> {
			Integer count = countData.getCount();
			String message = countData.getMessage();
			if (message == null || message.isEmpty()) {
				message = I18nTools.getLocalized(displayCount.getMessage());
				if (message == null)
					message = LocalizedText.INSTANCE.entryCount(count);
				else if (count != null)
					message += " " + count;
			}
			
			queryProviderView.setDisplayCountText(message);
		}).onError(e -> logger.error("Error while getting the count data.", e));
	}

	private void prepareGmContentContext(final QueryProviderContext queryProviderContext, final boolean initialQuery) {
		if (!(currentContentView instanceof GmContentSupport))
			return;
		
		GmContentSupport contentSupport = (GmContentSupport) currentContentView;
		GmContentContext context = new GmContentContext();
		context.hasMoreDataToLoad = hasMoreDataToLoad;
		
		Query query = queryProviderContext.getQuery(initialQuery);
		
		if (query != null && query.getRestriction() != null && query.getRestriction().getPaging() != null) {
			Paging paging = query.getRestriction().getPaging();			
			context.currentPageIndex = paging.getStartIndex();
		}
		
		contentSupport.setGmContentContext(context);
	}

	private void performPropertyQuery(QueryProviderContext queryProviderContext, boolean initialQuery, Query query) {
		PropertyQuery propertyQuery = (PropertyQuery) query;
		final String propertyName = propertyQuery.getPropertyName();
		final GenericEntity entity = resolveReference(propertyQuery.getEntityReference());
		
		if (initialQuery)
			GlobalState.mask(LocalizedText.INSTANCE.performingPropertyQuery());

		EntityType<?> propertyQueryEntityType = propertyQuery.entityType();
		final PropertyQuery modifiedPropertyQuery = (PropertyQuery) propertyQueryEntityType.clone(propertyQuery, null, null);

		performingSearch = true;
		ProfilingHandle ph = Profiling.start(getClass(), "Performing property query", true);
		gmSession.query().property(modifiedPropertyQuery).result(AsyncCallback.of(propertyQueryResult -> {
			ph.stop();
			if (disposed || lastPerformedQuery != query)
				return; // Ignoring result from a previous query
			
			try {
				handlePropertyQueryResult(queryProviderContext, initialQuery, propertyName, entity, modifiedPropertyQuery, propertyQueryResult);
			} catch (GmSessionException e) {
				handlePropertyQueryError(e, ph);
			}
		}, e -> handlePropertyQueryError(e, ph)));
	}
	
	private void handlePropertyQueryError(Throwable e, ProfilingHandle ph) {
		ph.stop();
		hasMoreDataToLoad = false;
		performingSearch = false;

		GlobalState.unmask();
		GlobalState.showError(LocalizedText.INSTANCE.errorPerformingPropertyQuery(), e);
		e.printStackTrace();
	}
	
	private void handlePropertyQueryResult(final QueryProviderContext queryProviderContext, final boolean initialQuery, final String propertyName, final GenericEntity entity,
			final PropertyQuery modifiedPropertyQuery, PropertyQueryResultConvenience propertyQueryResult) throws GmSessionException {
		PropertyQueryResult result = propertyQueryResult.result();
		hasMoreDataToLoad = result.getHasMore();

		GlobalState.unmask();

		queryProviderView.notifyQueryPerformed(result, queryProviderContext);
		EntityType<?> entityType = entity.entityType();

		Property property = entityType.getProperty(modifiedPropertyQuery.getPropertyName());
		CollectionType collectionType = (CollectionType) property.getType();

		Object value = result.getPropertyValue();

		if (!(value instanceof Collection)) {
			gmSession.suspendHistory();
			GMEUtil.changeEntityPropertyValue(entity, entity.entityType().getProperty(propertyName), value);
			gmSession.resumeHistory();
		} else {
			Collection<Object> collection = ((Collection<Object>) value);

			if (initialQuery) {
				if (rootModelPath != null) {
					Set<Object> set;
					if (collection instanceof Set)
						set = (Set<Object>) collection;
					else {
						set = new EnhancedSet<>((SetType) collectionType);
						set.addAll(collection);
					}
						

					ModelPathElement last = rootModelPath.last();
					if (last instanceof PropertyRelatedModelPathElement) {
						PropertyRelatedModelPathElement propertyRelatedModelPathElement = (PropertyRelatedModelPathElement) last;
						LocalEntityProperty localEntityProperty = localEntityProperty(propertyRelatedModelPathElement.getEntity(),
								propertyRelatedModelPathElement.getProperty().getName());

						((EnhancedSet<Object>) set).setCollectionOwner(localEntityProperty);
					}

					last.setValue(set);
					currentContentView.setContent(rootModelPath);
				} else if (currentContentView instanceof GmListView) {
					ListType listType = typeReflection.getListType(collectionType.getCollectionElementType());
					List<Object> list;
					if (collection instanceof List)
						list = (List<Object>) collection;
					else {
						list = new EnhancedList<Object>(listType);
						list.addAll(collection);
					}
					
					ModelPath modelPath = new ModelPath();
					modelPath.add(new RootPathElement(listType, list));

					currentContentView.setContent(null);
					((GmListView) currentContentView).addContent(modelPath);
				}
				
				handleDisplayCount(modifiedPropertyQuery);
			} else if (currentContentView instanceof GmListView) {
				Set<Object> set;
				if (collection instanceof Set)
					set = (Set<Object>) collection;
				else {
					set = new EnhancedSet<>((SetType) collectionType);
					set.addAll(collection);
				}

				ModelPath modelPath = new ModelPath();
				modelPath.add(new RootPathElement(collectionType, set));

				((GmListView) currentContentView).addContent(modelPath);
			}

			if (initialQuery && !collection.isEmpty())
				selectFirstResult();
		}
	
		prepareGmContentContext(queryProviderContext, initialQuery);
		
		performingSearch = false;
		if (queryProviderContext.isAutoPageEnabled() && currentContentView instanceof GmViewport)
			handleViewPortWindowChanged(((GmViewport) currentContentView), initialQuery);
	}

	private void performEntityQuery(QueryProviderContext queryProviderContext, boolean initialQuery, Query query) {
		EntityQuery entityQuery = (EntityQuery) query;
		CollectionType collectionType = changeViewForEntityQuery(entityQuery);
		
		if (initialQuery)
			GlobalState.mask(LocalizedText.INSTANCE.performingEntityQuery());

		performingSearch = true;
		ProfilingHandle ph = Profiling.start(getClass(), "Performing entity query", true);
		handleSpecialTC(entityType, query);
		
		gmSession.query().entities((EntityQuery) query).result(AsyncCallback.of(entityQueryResult -> {
			ph.stop();
			if (disposed || lastPerformedQuery != query)
				return; //Ignoring result from a previous query
			try {
				handleEntityQueryResult(queryProviderContext, initialQuery, collectionType, entityQueryResult, query);
			} catch (Exception e) {
				handleEntityQueryError(e, ph);
			}
		}, e -> handleEntityQueryError(e, ph)));
	}
	
	private void handleSpecialTC(EntityType<?> entityType, Query query) {
		if (specialEntityTraversingCriterionMap == null || query.getTraversingCriterion() != null)
			return;
		
		TraversingCriterion tc = specialEntityTraversingCriterionMap.get(entityType);
		if (tc != null)
			query.setTraversingCriterion(tc);
	}

	private void handleEntityQueryError(Throwable e, ProfilingHandle ph) {
		ph.stop();
		hasMoreDataToLoad = false;
		performingSearch = false;

		GlobalState.unmask();
		GlobalState.showError(LocalizedText.INSTANCE.errorPerformingEntityQuery(), e);
		e.printStackTrace();
	}
	
	private void handleEntityQueryResult(QueryProviderContext queryProviderContext, boolean initialQuery, CollectionType listType,
			EntityQueryResultConvenience entityQueryResult, Query query) throws GmSessionException {
		EntityQueryResult result = entityQueryResult.result();
		hasMoreDataToLoad = result != null ? result.getHasMore() : false;

		GlobalState.unmask();

		queryProviderView.notifyQueryPerformed(result, queryProviderContext);
		
		List<Object> list = null;
		if (result == null)
			list = Collections.emptyList();
		else {
			boolean skipCheckEntitiesToIgnore = entitiesToIgnore == null;
			list = result.getEntities().stream().filter(entity -> skipCheckEntitiesToIgnore || !entitiesToIgnore.contains(entity))
					.collect(Collectors.toList());
		}

		ModelPath modelPath = new ModelPath();
		modelPath.add(new RootPathElement(listType, list));

		prepareGmContentContext(queryProviderContext, initialQuery);
		
		if (initialQuery) {
			if (!(currentContentView instanceof GmListView))
				currentContentView.setContent(modelPath);
			else {
				if (currentContentView instanceof GmSelectionCount)
					((GmSelectionCount) currentContentView).setMaxSelectCount(maxSelectionCount);

				//Setting null here means that we have no parent element.
				currentContentView.setContent(null);
				((GmListView) currentContentView).addContent(modelPath);
			}

			selectFirstResult();
			handleDisplayCount(query);
		} else if (currentContentView instanceof GmListView) {
			if (currentContentView instanceof GmSelectionCount)
				((GmSelectionCount) currentContentView).setMaxSelectCount(maxSelectionCount);

			((GmListView) currentContentView).addContent(modelPath);
		}

		performingSearch = false;
		if (queryProviderContext.isAutoPageEnabled() && currentContentView instanceof GmViewport)
			handleViewPortWindowChanged(((GmViewport) currentContentView), initialQuery);
	}

	private Query prepareInitialQuery(final QueryProviderContext queryProviderContext, boolean initialQuery, boolean useLastQueryPageSize) {
		Query query = queryProviderContext.getQuery(initialQuery);
		if (query == null || !queryProviderContext.isAutoPageEnabled())
			return query;
		
		// Create restriction of query if missing
		Restriction restriction = query.getRestriction();
		if (restriction == null) {
			restriction = Restriction.T.create();
			query.setRestriction(restriction);
		}

		// Create paging of query is missing
		Paging paging = restriction.getPaging();
		if (paging == null) {
			paging = Paging.T.create();
			restriction.setPaging(paging);
		} else
			currentStartIndex = paging.getStartIndex();

		// Set auto paging values
		currentStartIndex = initialQuery ? 0 : currentStartIndex + getLastPageSize(queryProviderContext, useLastQueryPageSize);
		paging.setStartIndex(currentStartIndex);
		paging.setPageSize(queryProviderContext.getAutoPageSize());
		
		return query;
	}
	
	private int getLastPageSize(QueryProviderContext queryProviderContext, boolean useLastQueryPageSize) {
		if (useLastQueryPageSize && lastPerformedQuery != null && lastPerformedQuery.getRestriction() != null
				&& lastPerformedQuery.getRestriction().getPaging() != null) {
			return lastPerformedQuery.getRestriction().getPaging().getPageSize();
		}
		
		return queryProviderContext.getAutoPageSize();
	}

	private GenericEntity resolveReference(EntityReference entityReference) {
		try {
			return this.gmSession.queryCache().entity(entityReference).require();
		} catch (Exception e) {
			throw new RuntimeException("Entity not available", e);
		}
	}
	
	@Override
	public void setContent(ModelPath modelPath) {
		setEntityContent((GenericEntity) modelPath.last().getValue(), true);
	}
	
	public void setRootModelPath(ModelPath modelPath) {
		rootModelPath = modelPath;
	}
	
	public void reload() {
		setEntityContent(entity, true);
		performSearch();
	}
	
	/**
	 * Returns the configured query entity.
	 */
	public GenericEntity getEntity() {
		return entity;
	}
	
	private void setEntityContent(GenericEntity entity, boolean configureEntityTypeForQuery) {
		this.entity = entity;
		String entityTypeSignature = getEntityTypeSignature(entity);
		prepareColumnData(entityTypeSignature);
		prepareAutoExpand(entityTypeSignature);
		queryProviderView.setEntityContent(entity);
		
		if (entityTypeSignature != null) {
			configureEntityType(typeReflection.getEntityType(entityTypeSignature), configureEntityTypeForQuery, false);
			
			boolean queryVisible = !gmSession.getModelAccessory().getMetaData().entityType(entityType).useCase(useCase).is(Singleton.T);
			if (!queryVisible)
				configureCurrentTopWidget(null);
		}
		
		fireEntityContentSet();
	}
	
	private void configureEntityType(EntityType<?> entityType, boolean configureEntityTypeForCheck) {
		configureEntityType(entityType, configureEntityTypeForCheck, true);
	}
	
	protected void configureEntityType(EntityType<?> entityType, boolean configureEntityTypeForCheck, boolean prepareEntityQuery) {
		this.entityType = entityType;
		if (configureEntityTypeForCheck && currentContentView instanceof GmListView) {
			typeForCheck = entityType;
			((GmListView) currentContentView).configureTypeForCheck(entityType);
		}
		if (prepareEntityQuery) {
			EntityQuery entityQuery = EntityQuery.T.create();
			entityQuery.setEntityTypeSignature(entityType.getTypeSignature());
			setEntityContent(entityQuery, configureEntityTypeForCheck);
		}
	}
	
	@Override
	protected void handleViewPortWindowChanged(final GmViewport source, final boolean focusEditor) {
		QueryProviderContext queryProviderContext = queryProviderView.getQueryProviderContext();
		if (!queryProviderContext.isAutoPageEnabled() || performingSearch)
			return;
		
		performingSearch = true;
		Scheduler.get().scheduleDeferred(() -> {
			if (!hasMoreDataToLoad || !source.isWindowOverlappingFillingSensorArea()) {
				performingSearch = false;
				return;
			}
			
			Query query = prepareInitialQuery(queryProviderContext, false, true);
			if (query == null) {
				performingSearch = false;
				return;
			}

			// Focus editor
			if (focusEditor)
				queryProviderView.focusEditor();

			// Perform auto page search
			performSearch(queryProviderContext, query);
		});
	}
	
	private String getEntityTypeSignature(GenericEntity query) {
		if (query instanceof Template && ((Template) query).getPrototype() instanceof Query)
			query = (GenericEntity) ((Template) query).getPrototype();
		
		if (query instanceof EntityQuery)
			return ((EntityQuery) query).getEntityTypeSignature();
		
		if (query instanceof SelectQuery) {
			SelectQuery selectQuery = (SelectQuery) query;
			List<QuerySelection> selections = getBasicQuerySelectionResolver().resolve(selectQuery);
			return GMEUtil.getSingleEntityTypeSignatureFromSelectQuery(selections);
		}
		
		return null;
	}
	
	@Override
	protected GmViewChangeListener getViewChangeListener() {
		if (viewChangeListener != null)
			return viewChangeListener;
		
		viewChangeListener = (displayNode, nodeWidth, columnsVisible) -> queryProviderView.onViewChange(displayNode, nodeWidth, columnsVisible);
		
		return viewChangeListener;
	}
	
	private BasicQuerySelectionResolver getBasicQuerySelectionResolver() {
		if (basicQuerySelectionResolver == null) {
			NameAliasResolver nameAliasResolver = new NameAliasResolver();
			nameAliasResolver.setGmSession(getGmSession());
			
			basicQuerySelectionResolver = BasicQuerySelectionResolver.create()
					.aliasMode()
					.custom(nameAliasResolver)
					.shorteningMode()
					.simplified();
		}
		
		return basicQuerySelectionResolver;
	}
	
	@Override
	public void disposeBean() throws Exception {
		super.disposeBean();
		
		if (queryProviderView instanceof DisposableBean)
			((DisposableBean) queryProviderView).disposeBean();
		
		if (entitiesToIgnore != null) {
			entitiesToIgnore.clear();
			entitiesToIgnore = null;
		}
	}
	
}
