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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.gwt.gmview.actionbar.client.ActionProviderConfiguration;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionBar;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionProvider;
import com.braintribe.gwt.gmview.client.GmAmbiguousSelectionSupport;
import com.braintribe.gwt.gmview.client.GmCheckListener;
import com.braintribe.gwt.gmview.client.GmCheckSupport;
import com.braintribe.gwt.gmview.client.GmContentSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewActionManager;
import com.braintribe.gwt.gmview.client.GmContentViewContext;
import com.braintribe.gwt.gmview.client.GmContentViewListener;
import com.braintribe.gwt.gmview.client.GmInteractionListener;
import com.braintribe.gwt.gmview.client.GmInteractionSupport;
import com.braintribe.gwt.gmview.client.GmListContentSupplier;
import com.braintribe.gwt.gmview.client.GmListView;
import com.braintribe.gwt.gmview.client.GmMouseInteractionEvent;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.GmSelectionSupport;
import com.braintribe.gwt.gmview.client.GmViewChangeListener;
import com.braintribe.gwt.gmview.client.GmViewport;
import com.braintribe.gwt.gmview.client.GmViewportListener;
import com.braintribe.gwt.gmview.client.QuerySelectionHandler;
import com.braintribe.gwt.gmview.client.ReloadableGmView;
import com.braintribe.gwt.gmview.client.ViewSituationResolver;
import com.braintribe.gwt.gmview.client.js.GmExternalViewInitializationListener;
import com.braintribe.gwt.gmview.client.js.GmExternalViewInitializationSupport;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedBorderLayoutContainer;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.path.EntryPointPathElement;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.processing.query.api.stringifier.QuerySelection;
import com.braintribe.model.processing.query.selection.BasicQuerySelectionResolver;
import com.braintribe.model.processing.query.selection.experts.NameAliasResolver;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.record.ListRecord;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;

/**
 * Abstract class to be extended by views which handles query results.
 * @author michel.docouto
 *
 */
public abstract class QueryResultsHandlerView extends BorderLayoutContainer implements GmSelectionSupport, GmCheckSupport, GmInteractionSupport,
		GmContentSupport, GmAmbiguousSelectionSupport, GmViewActionProvider, GmExternalViewInitializationSupport, GmContentView, GmListContentSupplier, ReloadableGmView, DisposableBean {
	
	private Map<GmContentViewContext, GmContentView> providedContentViews = new HashMap<>();
	protected String useCase;
	private GmContentView defaultContentView;
	private Supplier<? extends GmContentView> defaultContentViewSupplier;
	protected PersistenceGmSession gmSession;
	protected GmContentView currentContentView;
	private GmViewportListener viewportListener;
	private GmSelectionListener selectionListener;
	private Set<GmSelectionListener> selectionListeners;
	private GmCheckListener checkListener;
	private Set<GmCheckListener> checkListeners;
	private GmInteractionListener interactionListener;
	private Set<GmInteractionListener> interactionListeners;
	private Set<GmContentViewListener> contentViewListeners;
	private GmExternalViewInitializationListener initializationListener;
	protected ExtendedBorderLayoutContainer wrapperPanel;
	private GmViewActionBar gmViewActionBar;
	private Supplier<GmViewActionBar> gmViewActionBarSupplier;
	protected GenericModelType typeForCheck;
	protected boolean disposed;
	private BasicQuerySelectionResolver basicQuerySelectionResolver;
	protected Supplier<ViewSituationResolver<GmContentViewContext>> viewSituationResolverSupplier;
	protected boolean reloadPending;
	
	/**
	 * Configures the default {@link GmContentView} that should be used within.
	 * It is a good idea to use a {@link MasterDetailConstellation} here.
	 */
	@Required
	public void setDefaultContentView(Supplier<? extends GmContentView> defaultContentViewSupplier) {
		this.defaultContentViewSupplier = defaultContentViewSupplier;
	}
	
	/**
	 * Configures the required {@link GmViewActionBar} that will be updated if the QueryConstellation current content view changes.
	 */
	@Required
	public void setGmViewActionBar(Supplier<GmViewActionBar> gmViewActionBarSupplier) {
		this.gmViewActionBarSupplier = gmViewActionBarSupplier;
	}
	
	/**
	 * Configures the required {@link ViewSituationResolver} to resolve the correct {@link GmContentView} to be used for a given {@link Query}.
	 * If none may be used, then the one set via {@link #setDefaultContentView(Supplier)} is used.
	 */
	@Required
	public void setViewSituationResolver(Supplier<ViewSituationResolver<GmContentViewContext>> viewSituationResolverSupplier) {
		this.viewSituationResolverSupplier = viewSituationResolverSupplier;
	}
	
	/* GmSelectionSupport methods */
	
	@Override
	public ModelPath getFirstSelectedItem() {
		return currentContentView.getFirstSelectedItem();
	}
	
	@Override
	public List<ModelPath> getCurrentSelection() {
		return currentContentView.getCurrentSelection();
	}
	
	@Override
	public boolean isSelected(Object element) {
		return currentContentView.isSelected(element);
	}
	
	@Override
	public void select(int index, boolean keepExisting) {
		currentContentView.select(index, keepExisting);
	}
	
	@Override
	public boolean select(Element element, boolean keepExisting) {
		return currentContentView.select(element, keepExisting);
	}
	
	@Override
	public boolean selectHorizontal(Boolean next, boolean keepExisting) {
		return currentContentView.selectHorizontal(next, keepExisting);
	}
	
	@Override
	public boolean selectVertical(Boolean next, boolean keepExisting) {
		return currentContentView.selectVertical(next, keepExisting);
	}
	
	@Override
	public GmContentView getView() {
		return currentContentView;
	}
	
	@Override
	public int getFirstSelectedIndex() {
		return currentContentView.getFirstSelectedIndex();
	}
	
	@Override
	public void deselectAll() {
		currentContentView.deselectAll();
	}
	
	@Override
	public void selectRoot(int index, boolean keepExisting) {
		currentContentView.selectRoot(index, keepExisting);
	}
	
	/* End of GmSelectionSupport methods */
	
	/* GmCheckSupport methods */
	
	@Override
	public void addCheckListener(GmCheckListener cl) {
		if (cl != null) {
			if (checkListeners == null)
				checkListeners = new LinkedHashSet<>();
			checkListeners.add(cl);
		}
	}
	
	@Override
	public void removeCheckListener(GmCheckListener cl) {
		if (checkListeners != null) {
			checkListeners.remove(cl);
			if (checkListeners.isEmpty())
				checkListeners = null;
		}
	}
	
	@Override
	public ModelPath getFirstCheckedItem() {
		if (currentContentView instanceof GmCheckSupport)
			return ((GmCheckSupport) currentContentView).getFirstCheckedItem();
		
		return null;
	}
	
	@Override
	public List<ModelPath> getCurrentCheckedItems() {
		if (currentContentView instanceof GmCheckSupport)
			return ((GmCheckSupport) currentContentView).getCurrentCheckedItems();
		
		return null;
	}
	
	@Override
	public boolean isChecked(Object element) {
		if (currentContentView instanceof GmCheckSupport)
			return ((GmCheckSupport) currentContentView).isChecked(element);
		
		return false;
	}
	
	@Override
	public boolean uncheckAll() {
		if (currentContentView instanceof GmCheckSupport)
			return ((GmCheckSupport) currentContentView).uncheckAll();
		
		return false;
	}
	
	/* End of GmCheckSupport methods */
	
	/* GmInteractionSupport methods */
	
	@Override
	public void addInteractionListener(GmInteractionListener il) {
		if (il != null) {
			if (interactionListeners == null)
				interactionListeners = new LinkedHashSet<>();
			interactionListeners.add(il);
		}
	}

	@Override
	public void removeInteractionListener(GmInteractionListener il) {
		if (interactionListeners != null) {
			interactionListeners.remove(il);
			if (interactionListeners.isEmpty())
				interactionListeners = null;
		}
	}
	
	/* End of GmInteractionSupport methods */
	
	/* GmContentViewSupport methods */
	
	@Override
	public void addGmContentViewListener(GmContentViewListener listener) {
		if (listener != null) {
			if (contentViewListeners == null)
				contentViewListeners = new LinkedHashSet<>();
			contentViewListeners.add(listener);
		}
	}
	
	@Override
	public void removeGmContentViewListener(GmContentViewListener listener) {
		if (contentViewListeners != null) {
			contentViewListeners.remove(listener);
			if (contentViewListeners.isEmpty())
				contentViewListeners = null;
		}
	}
	
	/* End of GmContentViewSupport methods */
	
	/* GmAmbiguousSelectionSupport methods */
	
	@Override
	public List<List<ModelPath>> getAmbiguousSelection() {
		if (currentContentView instanceof GmAmbiguousSelectionSupport)
			return ((GmAmbiguousSelectionSupport) currentContentView).getAmbiguousSelection();
		
		return transformSelection(getCurrentSelection());
	}
	
	/* End of GmAmbiguousSelectionSupport methods */
	
	/* GmViewActionProvider methods */
	
	@Override
	public ActionProviderConfiguration getActions() {
		return currentContentView instanceof GmViewActionProvider ? ((GmViewActionProvider) currentContentView).getActions() : null;
	}
	
	@Override
	public boolean isFilterExternalActions() {
		return currentContentView instanceof GmViewActionProvider ? ((GmViewActionProvider) currentContentView).isFilterExternalActions() : false; 
	}
	
	@Override
	public void setActionManager(GmContentViewActionManager actionManager) {
		// NOP
	}
	
	/* End of GmViewActionProvider methods */
	
	/* GmExternalViewInitializationSupport methods */
	
	@Override
	public void addInitializationListener(GmExternalViewInitializationListener listener) {
		if (currentContentView instanceof GmExternalViewInitializationSupport)
			((GmExternalViewInitializationSupport) currentContentView).addInitializationListener(listener);
	}
	
	@Override
	public void removeInitializationListener(GmExternalViewInitializationListener listener) {
		if (currentContentView instanceof GmExternalViewInitializationSupport)
			((GmExternalViewInitializationSupport) currentContentView).removeInitializationListener(listener);
	}
	
	/* End of GmExternalViewInitializationSupport methods */
	
	/* GmContentView methods */
	
	@Override
	public boolean isViewReady() {
		return currentContentView == null ? false : currentContentView.isViewReady();
	}
	
	@Override
	public ModelPath getContentPath() {
		return currentContentView.getContentPath();
	}
	
	@Override
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
	}
	
	@Override
	public PersistenceGmSession getGmSession() {
		return gmSession;
	}
	
	@Override
	public String getUseCase() {
		return useCase;
	}
	
	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		if (sl != null) {
			if (selectionListeners == null)
				selectionListeners = new LinkedHashSet<>();
			selectionListeners.add(sl);
		}
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		if (selectionListeners != null) {
			selectionListeners.remove(sl);
			if (selectionListeners.isEmpty())
				selectionListeners = null;
		}
	}
	
	/* End of GmContentView methods */
	
	/* GmListContentSupplier methods */
	
	@Override
	public List<ModelPath> getContents() {
		if (currentContentView instanceof GmListView)
			return ((GmListView) currentContentView).getAddedModelPaths();
		
		return null;
	}
	
	/* End of GmListContentSupplier methods */
	
	/* ReloadableGmView methods */
	
	@Override
	public boolean isReloadPending() {
		return reloadPending;
	}
	
	@Override
	public void setReloadPending(boolean reloadPending) {
		this.reloadPending = reloadPending;
	}
	
	/* End of ReloadableGmView methods */
	
	/* DisposableBean methods */
	
	@Override
	public void disposeBean() throws Exception {
		exchangeCurrentContentView(null, null);
		
		disposed = true;
		
		if (defaultContentView instanceof DisposableBean)
			((DisposableBean) defaultContentView).disposeBean();
		
		if (providedContentViews != null) {
			for (GmContentView view : providedContentViews.values()) {
				if (view instanceof DisposableBean)
					((DisposableBean) view).disposeBean();
			}
			
			providedContentViews.clear();
		}
		
		if (contentViewListeners != null) {
			contentViewListeners.clear();
			contentViewListeners = null;
		}
		
		if (selectionListeners != null) {
			selectionListeners.clear();
			selectionListeners = null;
		}
		
		if (checkListeners != null) {
			checkListeners.clear();
			checkListeners = null;
		}
		
		if (interactionListeners != null) {
			interactionListeners.clear();
			interactionListeners = null;
		}
	}
	
	/* End of DisposableBean methods */
	
	protected abstract void handleViewPortWindowChanged(GmViewport source, boolean focusEditor);
	
	protected abstract GmViewChangeListener getViewChangeListener();
	
	/**
	 * Prepares and exchanges the view based on the given {@link EntityQuery}.
	 * Returns the collectionType also based on the {@link EntityQuery}.
	 */
	protected CollectionType changeViewForEntityQuery(EntityQuery entityQuery) {
		GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
		EntityType<?> entityType = typeReflection.getEntityType(entityQuery.getEntityTypeSignature());
		CollectionType collectionType = typeReflection.getListType(entityType);

		ViewSituationResolver<GmContentViewContext> viewSituationResolver = viewSituationResolverSupplier.get();
		List<GmContentViewContext> possibleContentViews = viewSituationResolver.getPossibleContentViews(new RootPathElement(collectionType, null));
		provideAndExchangeView(possibleContentViews.isEmpty() ? null : possibleContentViews.get(0), null);
		
		return collectionType;
	}
	
	/**
	 * Prepares and exchanges the view based on the given {@link SelectQuery}.
	 * Returns the collection element Type also based on the {@link SelectQuery}.
	 */
	protected GenericModelType changeViewForSelectQuery(SelectQuery selectQuery) {
		List<QuerySelection> selections = getBasicQuerySelectionResolver().resolve(selectQuery);
		EntityType<?> entityType = GMEUtil.getSingleEntityTypeFromSelections(selections, GMF.getTypeReflection());
		
		GenericModelType collectionElementType;
		if (entityType != null)
			collectionElementType = entityType;
		else
			collectionElementType = GMF.getTypeReflection().getBaseType();

		ModelPathElement modelPathElement;
		if (selections.size() > 1 || entityType == null)
			modelPathElement = new EntryPointPathElement(ListRecord.T, null);
		else
			modelPathElement = new RootPathElement(GMF.getTypeReflection().getListType(collectionElementType), null);

		ViewSituationResolver<GmContentViewContext> viewSituationResolver = viewSituationResolverSupplier.get();
		List<GmContentViewContext> possibleContentViews = viewSituationResolver.getPossibleContentViews(modelPathElement);
		provideAndExchangeView(possibleContentViews.isEmpty() ? null : possibleContentViews.get(0), selections);
		
		return collectionElementType;
	}
	
	protected void fireEntityContentSet() {
		if (contentViewListeners != null)
			contentViewListeners.forEach(l -> l.onContentSet(this));
	}
	
	protected GmContentView getDefaultContentView() {
		if (defaultContentView != null)
			return defaultContentView;
		
		defaultContentView = defaultContentViewSupplier.get();
		return defaultContentView;
	}
	
	protected void exchangeCurrentContentView(GmContentView contentView, List<QuerySelection> querySelectionList) {
		if (currentContentView == contentView) {
			if (currentContentView instanceof QuerySelectionHandler)
				((QuerySelectionHandler) currentContentView).configureQuerySelectionList(querySelectionList);
			return;
		}
		
		boolean configureActionBar = false;
		if (currentContentView != null) {
			if (currentContentView instanceof GmViewport)
				((GmViewport) currentContentView).removeGmViewportListener(getViewportListener());
			currentContentView.setContent(null);
			currentContentView.removeSelectionListener(getSelectionListener());
			if (currentContentView instanceof GmCheckSupport)
				((GmCheckSupport) currentContentView).removeCheckListener(getCheckListener());
			if (currentContentView instanceof GmInteractionSupport)
				((GmInteractionSupport) currentContentView).removeInteractionListener(getInteractionListener());
			if (currentContentView instanceof GmContentSupport && getViewChangeListener() != null)
				((GmContentSupport) currentContentView).removeGmViewChangeListener(getViewChangeListener());
			if (currentContentView instanceof GmExternalViewInitializationSupport)
				((GmExternalViewInitializationSupport) currentContentView).removeInitializationListener(getExternalViewInitializationListener());						
			if (currentContentView instanceof Widget)
				wrapperPanel.remove((Widget) currentContentView);
			
			configureActionBar = true;
		}
		
		currentContentView = contentView;
		
		if (currentContentView != null) {
			currentContentView.configureUseCase(useCase);
			currentContentView.configureGmSession(this.gmSession);
			if (currentContentView instanceof GmViewport)
				((GmViewport) currentContentView).addGmViewportListener(getViewportListener());
			if (currentContentView instanceof QuerySelectionHandler)
				((QuerySelectionHandler) currentContentView).configureQuerySelectionList(querySelectionList);
			currentContentView.addSelectionListener(getSelectionListener());
			if (currentContentView instanceof GmCheckSupport)
				((GmCheckSupport) currentContentView).addCheckListener(getCheckListener());
			if (currentContentView instanceof GmInteractionSupport)
				((GmInteractionSupport) currentContentView).addInteractionListener(getInteractionListener());
			if (currentContentView instanceof GmContentSupport && getViewChangeListener() != null)
				((GmContentSupport) currentContentView).addGmViewChangeListener(getViewChangeListener());
			if (currentContentView instanceof Widget)
				wrapperPanel.setCenterWidget((Widget) currentContentView);
			if (currentContentView instanceof GmListView)
				((GmListView) currentContentView).configureTypeForCheck(typeForCheck);			
			if (currentContentView instanceof GmExternalViewInitializationSupport)
				((GmExternalViewInitializationSupport) currentContentView).addInitializationListener(getExternalViewInitializationListener());			
		}
		
		if (configureActionBar) {
			if (contentView instanceof GmViewActionProvider)
				getGmViewActionBar().prepareActionsForView((GmViewActionProvider) contentView);
			else if (contentView != null)
				getGmViewActionBar().prepareActionsForView(null);
		}
		
		forceLayout();
	}
	
	protected GmContentView provideAndExchangeView(GmContentViewContext providerAndName, List<QuerySelection> querySelectionList) {
		GmContentView contentView = null;
		if (providerAndName != null) {
			contentView = providedContentViews.get(providerAndName);
			if (contentView == null) {
				contentView = providerAndName.getContentViewProvider().get();
				providedContentViews.put(providerAndName, contentView);
			}
			useCase = providerAndName.getUseCase();
		}
		
		if (contentView == null)
			contentView = getDefaultContentView();
		if (contentView.getGmSession() != gmSession)
			contentView.configureGmSession(gmSession);
		exchangeCurrentContentView(contentView, querySelectionList);
		
		return contentView;
	}
	
	protected void selectFirstResult() {
		new Timer() {
			@Override
			public void run() {
				List<ModelPath> currentSelection = currentContentView.getCurrentSelection();
				if (currentSelection == null || currentSelection.isEmpty())
					currentContentView.select(0, false);
			}
		}.schedule(100);
	}
	
	private GmViewportListener getViewportListener() {
		if (viewportListener == null)
			viewportListener = source -> handleViewPortWindowChanged(source, false);
		
		return viewportListener;
	}
	
	private GmSelectionListener getSelectionListener() {
		if (selectionListener != null)
			return selectionListener;
		
		selectionListener = gmSelectionSupport -> {
			if (selectionListeners != null) {
				List<GmSelectionListener> listenersCopy = new ArrayList<>(selectionListeners);
				listenersCopy.stream().forEach(l -> l.onSelectionChanged(gmSelectionSupport));
			}
		};
		
		return selectionListener;
	}
	
	private GmCheckListener getCheckListener() {
		if (checkListener == null) {
			checkListener = gmSelectionSupport -> {
				if (checkListeners != null)
					checkListeners.stream().forEach(l -> l.onCheckChanged(gmSelectionSupport));
			};
		}
		
		return this.checkListener;
	}
	
	private GmInteractionListener getInteractionListener() {
		if (interactionListener != null)
			return interactionListener;
		
		interactionListener = new GmInteractionListener() {
			@Override
			public void onDblClick(GmMouseInteractionEvent event) {
				if (interactionListeners != null)
					interactionListeners.stream().forEach(l -> l.onDblClick(event));
			}
			
			@Override
			public void onClick(GmMouseInteractionEvent event) {
				if (interactionListeners != null)
					interactionListeners.stream().forEach(l -> l.onClick(event));
			}
			
			@Override
			public boolean onBeforeExpand(GmMouseInteractionEvent event) {
				boolean canceled = false; 
				if (interactionListeners != null) {
					for (GmInteractionListener listener : interactionListeners) {
						boolean cancel = listener.onBeforeExpand(event);
						if (!canceled && cancel)
							canceled = true;
					}
				}
				
				return canceled;
			}
		};
		
		return this.interactionListener;
	}
	
	private GmExternalViewInitializationListener getExternalViewInitializationListener() {
		if (initializationListener == null) {
			initializationListener = instantiatedSupport -> {
				if (currentContentView instanceof GmListView)
					selectFirstResult();
			};
		}
		
		return initializationListener;
	}
	
	private GmViewActionBar getGmViewActionBar() {
		if (gmViewActionBar != null)
			return gmViewActionBar;
		
		gmViewActionBar = gmViewActionBarSupplier.get();
		return gmViewActionBar;
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
	
}
