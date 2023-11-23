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

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gm.storage.api.ColumnData;
import com.braintribe.gwt.gme.constellation.client.action.ExchangeContentViewAction;
import com.braintribe.gwt.gme.propertypanel.client.PropertyPanel;
import com.braintribe.gwt.gmview.action.client.ActionGroup;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.actionbar.client.ActionProviderConfiguration;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionBar;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionProvider;
import com.braintribe.gwt.gmview.client.GmActionSupport;
import com.braintribe.gwt.gmview.client.GmAmbiguousSelectionSupport;
import com.braintribe.gwt.gmview.client.GmCheckListener;
import com.braintribe.gwt.gmview.client.GmCheckSupport;
import com.braintribe.gwt.gmview.client.GmCondensationView;
import com.braintribe.gwt.gmview.client.GmContentContext;
import com.braintribe.gwt.gmview.client.GmContentSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewActionManager;
import com.braintribe.gwt.gmview.client.GmContentViewContext;
import com.braintribe.gwt.gmview.client.GmContentViewListener;
import com.braintribe.gwt.gmview.client.GmDetailViewListener;
import com.braintribe.gwt.gmview.client.GmDetailViewSupport;
import com.braintribe.gwt.gmview.client.GmEntityView;
import com.braintribe.gwt.gmview.client.GmExchangeMasterViewListener;
import com.braintribe.gwt.gmview.client.GmInteractionListener;
import com.braintribe.gwt.gmview.client.GmInteractionSupport;
import com.braintribe.gwt.gmview.client.GmListView;
import com.braintribe.gwt.gmview.client.GmMainDetailView;
import com.braintribe.gwt.gmview.client.GmMouseInteractionEvent;
import com.braintribe.gwt.gmview.client.GmResetableActionsContentView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.GmTemplateMetadataViewSupport;
import com.braintribe.gwt.gmview.client.GmTreeView;
import com.braintribe.gwt.gmview.client.GmViewChangeListener;
import com.braintribe.gwt.gmview.client.GmViewIdProvider;
import com.braintribe.gwt.gmview.client.GmViewport;
import com.braintribe.gwt.gmview.client.GmViewportListener;
import com.braintribe.gwt.gmview.client.HasAddtionalWidgets;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.QuerySelectionHandler;
import com.braintribe.gwt.gmview.client.js.GmExternalViewInitializationListener;
import com.braintribe.gwt.gmview.client.js.GmExternalViewInitializationSupport;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedBorderLayoutContainer;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.data.prompt.AutoExpand;
import com.braintribe.model.meta.data.prompt.CondensationMode;
import com.braintribe.model.processing.query.api.stringifier.QuerySelection;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;

/**
 * Constellation which wires up a {@link GmContentView} and another detail {@link GmContentView} - which is likely to be a PropertyPanel - together.
 * @author michel.docouto
 *
 */
public class MasterDetailConstellation extends BorderLayoutContainer
		implements InitializableBean, GmContentView, GmListView, GmViewport, GmViewActionProvider, QuerySelectionHandler, GmCheckSupport,
		GmInteractionSupport, GmActionSupport, GmContentSupport, GmTemplateMetadataViewSupport, DisposableBean, GmExternalViewInitializationListener,
		GmExternalViewInitializationSupport, GmAmbiguousSelectionSupport, GmCondensationView, GmMainDetailView {
	
	private static int masterIdCounter = 0;
	private static int detailIdCounter = 0;
	
	private Supplier<? extends GmContentView> defaultMasterViewProvider;
	private GmContentView defaultMasterView;
	private GmContentView currentMasterView;
	private Supplier<? extends GmEntityView> detailViewSupplier;
	private GmEntityView detailView;
	private Map<GmContentView, GmEntityView> masterViewToDetailView = new HashMap<>();
	private boolean showDetailViewCollapsed = false;
	private int detailViewInitialWidth = 400;
	private int lastDetailViewWidth = detailViewInitialWidth;
	private Map<GmContentViewContext, GmContentView> providedContentViews = new HashMap<>();
	private boolean showDetailView = true;
	private GmSelectionListener masterAndDetailLinker;
	private List<Pair<ActionTypeAndName, ModelAction>> externalActions;
	private BorderLayoutData eastData;
	private PersistenceGmSession gmSession;
	private GenericModelType typeForCheck;
	private Set<GmContentViewListener> contentViewListeners;
	private Set<GmViewportListener> viewportListeners;
	private GmViewportListener viewportListener;
	private Set<GmSelectionListener> selectionListeners;
	private GmSelectionListener selectionListener;
	private GmCheckListener checkListener;
	private Set<GmCheckListener> checkListeners;
	private Set<GmInteractionListener> interactionListeners;
	private Set<GmViewChangeListener> viewChangeListeners;
	private Set<GmExchangeMasterViewListener> exchangeMasterViewListeners;	
	private GmInteractionListener interactionListener;
	private GmViewChangeListener viewChangeListener;
	private BorderLayoutContainer wrapperDetailPanel;
	private ExchangeContentViewAction exchangeContentViewAction;
	private ModelPath modelPath;
	private List<ModelPath> addedModelPaths;
	private Map<GmContentView, ActionProviderConfiguration> actionsByView = new HashMap<>();
	private List<MasterDetailConstellationListener> masterDetailConstellationListeners;
	private List<QuerySelection> querySelectionList;
	private String useCase;
//	private MaximizeViewAction maximizeViewAction;
	private ColumnData columnData;
	private AutoExpand autoExpand;
	private GmContentViewContext currentContentViewContext;
	private boolean readOnly = false;
	private Map<GmExternalViewInitializationSupport, Future<GmContentView>> contentFutureMap = new HashMap<>();
	private List<GmExternalViewInitializationListener> contentViewInitializationListeners;
	private Map<GmContentViewContext, Future<GmContentView>> contentViewContextMap = new HashMap<>();
	private Map<GmContentViewContext, Boolean> clearContentMap = new HashMap<>();
	private boolean viewInitialized;
	private Widget currentWidget;
	private GmContentView oldCurrentMasterView;
	private boolean mainViewVisible = true;
	
	public MasterDetailConstellation() {
		this.setBorders(false);
		this.addStyleName("gmMasterDetailConstellation");
	}
	
	/**
	 * Configures the default {@link GmContentView} provider that should be used within the master position.
	 */
	@Required
	public void setDefaultMasterViewProvider(Supplier<? extends GmContentView> defaultMasterViewProvider) {
		this.defaultMasterViewProvider = defaultMasterViewProvider;
	}
	
	/**
	 * Configures whether the detail view must be shown.
	 * Defaults to true.
	 */
	@Configurable
	public void setShowDetailView(boolean showDetailView) {
		this.showDetailView = showDetailView;
		
		if (!showDetailView && viewInitialized && wrapperDetailPanel != null) {
			setEastWidget(null);
			doLayout();
		} else if (showDetailView && viewInitialized && detailView instanceof Widget) {
			if (wrapperDetailPanel == null) {
				wrapperDetailPanel = new ExtendedBorderLayoutContainer();
				wrapperDetailPanel.setBorders(false);			
				wrapperDetailPanel.setCenterWidget((Widget) detailView);
				
				XElement xe = XElement.as(wrapperDetailPanel.getElement());
				xe.getStyle().setBorderColor("#dfdfdf !important");
			}
			
			setEastWidget(wrapperDetailPanel, eastData);
		}
		
		fireDetailsVisibilityChanged(!showDetailView);
	}
	
	/**
	 * The {@link GmEntityView} which will display details of a given entity.
	 * If {@link #setShowDetailView(boolean)} is set to true (default), then this is required.
	 * It is a good idea to use a {@link PropertyPanel} here.
	 */
	@Configurable
	public void setDetailViewSupplier(Supplier<? extends GmEntityView> detailViewSupplier) {
		this.detailViewSupplier = detailViewSupplier;
	}
	
	/**
	 * Configures whether to show the detail view initially collapsed.
	 * Defaults to false.
	 * If {@link #setShowDetailView(boolean)} is set to false, then this is ignored.
	 */
	@Configurable
	public void setShowDetailViewCollapsed(boolean showDetailViewCollapsed) {
		boolean react = showDetailViewCollapsed != this.showDetailViewCollapsed;
		if (viewInitialized && react)
			collapseOrExpandDetailView(showDetailViewCollapsed);
		else
			this.showDetailViewCollapsed = showDetailViewCollapsed;
	}
	
	/**
	 * Configures the detail view initial width (in pixels). Defaults to 400px.
	 * If {@link #setShowDetailView(boolean)} is set to false, then this is ignored.
	 */
	@Configurable
	public void setDetailViewInitialWidth(int detailViewInitialWidth) {
		this.detailViewInitialWidth = detailViewInitialWidth;
	}
	
	/**
	 * Configures the action used for maximizing the view.
	 */
	/*
	@Configurable
	public void setMaximizeViewAction(MaximizeViewAction maximizeViewAction) {
		this.maximizeViewAction = maximizeViewAction;
	}
	*/
	
	public void addMasterDetailConstellationListener(MasterDetailConstellationListener listener) {
		if (masterDetailConstellationListeners == null)
			masterDetailConstellationListeners = new ArrayList<>();
		
		masterDetailConstellationListeners.add(listener);
		
		fireDetailsVisibilityChanged(!showDetailView);
	}
	
	public void removeMasterDetailConstellationListener(MasterDetailConstellationListener listener) {
		if (masterDetailConstellationListeners != null) {
			masterDetailConstellationListeners.remove(listener);
			
			if (masterDetailConstellationListeners.isEmpty())
				masterDetailConstellationListeners = null;
		}
	}
	
	public void setExchangeContentViewAction(ExchangeContentViewAction exchangeContentViewAction) {
		this.exchangeContentViewAction = exchangeContentViewAction;
		if (exchangeContentViewAction != null)
			this.exchangeContentViewAction.setMasterDetailConstellation(this);
	}
	
	public ExchangeContentViewAction getExchangeContentViewAction() {
		return exchangeContentViewAction;
	}
	
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
		if (detailView != null && showDetailView)
			detailView.configureGmSession(gmSession);
		
		if (exchangeContentViewAction == null)
			provideAndExchangeView(null, true, null);
		else {
			GmContentViewContext contentViewContext = exchangeContentViewAction.getDefaultContentView();
			if (contentViewContext != null)
				contentViewContext.setReadOnly(readOnly);
			provideAndExchangeView(contentViewContext, true, null);
		}
	}
	
	@Override
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
		if (currentMasterView != null)
			currentMasterView.configureUseCase(useCase);
		if (exchangeContentViewAction != null)
			exchangeContentViewAction.configurePreferredUseCase(useCase, false);
	}
	
	@Override
	public void configureQuerySelectionList(List<QuerySelection> querySelectionList) {
		this.querySelectionList = querySelectionList;
		
		if (currentMasterView instanceof QuerySelectionHandler)
			((QuerySelectionHandler) currentMasterView).configureQuerySelectionList(querySelectionList);
	}
	
	@Override
	public List<QuerySelection> getQuerySelectionList() {
		return querySelectionList;
	}
	
	@Override
	public void intializeBean() throws Exception {
		eastData = new BorderLayoutData(detailViewInitialWidth);
		eastData.setMinSize(200);
		eastData.setMaxSize(1000);
		eastData.setSplit(true);
		if (showDetailView) {
			initializeDetailView();
			
			if (detailView instanceof Widget) {
				wrapperDetailPanel = new ExtendedBorderLayoutContainer();
				wrapperDetailPanel.setBorders(false);			
				wrapperDetailPanel.setCenterWidget((Widget) detailView);
				
				XElement xe = XElement.as(wrapperDetailPanel.getElement());
				xe.getStyle().setBorderColor("#dfdfdf !important");
				
				//eastData.setMargins(new Margins(0, 0, 0, 4));
				this.setEastWidget(wrapperDetailPanel, eastData);
				if (showDetailViewCollapsed)
					collapseOrExpandDetailView(!showDetailViewCollapsed);
			}
		}
		
		viewInitialized = true;
	}
	
	@Override
	public void setMainViewVisibility(boolean visible) {
		if (!visible) {
			if (mainViewVisible && detailView instanceof Widget) {
				oldCurrentMasterView = currentMasterView;
				setEastWidget(null);
				currentMasterView = detailView;
				setCenterWidget(wrapperDetailPanel);
				doLayout();
			}
		} else if (oldCurrentMasterView instanceof Widget && detailView instanceof Widget) {
			if (!mainViewVisible) {
				setCenterWidget((Widget) oldCurrentMasterView);
				currentMasterView = oldCurrentMasterView;
				oldCurrentMasterView = null;
				setShowDetailView(true);
			}
		}
		
		mainViewVisible = visible;
	}
	
	@Override
	public void setDetailViewVisibility(boolean visible) {
		setShowDetailView(visible);
	}
	
	private void initializeDetailView() {
		detailView = detailViewSupplier.get();
		
		if (detailView.getGmSession() == null)
			detailView.configureGmSession(gmSession);
		
		detailView.addSelectionListener(getSelectionListener());
		if (detailView instanceof GmViewIdProvider) {
			String rootId = ((GmViewIdProvider) detailView).getRootId();
			if (!((GmViewIdProvider) detailView).getId().contains(rootId))
				((GmViewIdProvider) detailView).setId(rootId + detailIdCounter++);
		}
	}
	
	public void configureReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	public void resetActions() {
		if (currentMasterView instanceof GmResetableActionsContentView)
			((GmResetableActionsContentView) currentMasterView).resetActions();
		if (defaultMasterView instanceof GmResetableActionsContentView && currentMasterView != defaultMasterView)
			((GmResetableActionsContentView) defaultMasterView).resetActions();
	}
	
	@Override
	public void configureExternalActions(List<Pair<ActionTypeAndName, ModelAction>> externalActions) {
		this.externalActions = externalActions;
		if (currentMasterView instanceof GmActionSupport)
			((GmActionSupport) currentMasterView).configureExternalActions(externalActions);
		if (defaultMasterView instanceof GmActionSupport && currentMasterView != defaultMasterView)
			((GmActionSupport) defaultMasterView).configureExternalActions(externalActions);
	}
	
	@Override
	public List<Pair<ActionTypeAndName, ModelAction>> getExternalActions() {
		return externalActions;
	}
	
	@Override
	public String getUseCase() {
		return useCase;
	}
	
	@Override
	public void setContent(ModelPath modelPath) {
		configureTopLevelObject(modelPath, true);
	}
	
	@Override
	public void setColumnData(ColumnData columnData) {
		this.columnData = columnData;
		if (currentMasterView instanceof GmTemplateMetadataViewSupport)
			((GmTemplateMetadataViewSupport) currentMasterView).setColumnData(columnData);
	}
	
	@Override
	public void setAutoExpand(AutoExpand autoExpand) {
		this.autoExpand = autoExpand;
		if (currentMasterView instanceof GmTemplateMetadataViewSupport)
			((GmTemplateMetadataViewSupport) currentMasterView).setAutoExpand(autoExpand);
	}
	
	@Override
	public void addContent(ModelPath modelPath) {
		configureTopLevelObject(modelPath, false);
	}
	
	@Override
	public void setGmContentContext(GmContentContext context) {
		if (currentMasterView instanceof GmContentSupport)
			((GmContentSupport) currentMasterView).setGmContentContext(context);
	}
	
	@Override
	public GmContentContext getGmContentContext() {
		return currentMasterView instanceof GmContentSupport ? ((GmContentSupport) currentMasterView).getGmContentContext() : null;
	}
	
	@Override
	public List<ModelPath> getAddedModelPaths() {
		return addedModelPaths;
	}
	
	@Override
	public void configureTypeForCheck(GenericModelType typeForCheck) {
		this.typeForCheck = typeForCheck;
		if (currentMasterView instanceof GmListView)
			((GmListView) currentMasterView).configureTypeForCheck(typeForCheck);
	}
	
	/**
	 * Returns the {@link GmEntityView} used as detail view.
	 */
	public GmEntityView getDetailView() {
		return detailView;
	}
	
	public boolean collapseOrExpandDetailView() {
		return collapseOrExpandDetailView(!showDetailViewCollapsed);
	}
	
	public boolean isShowDetailViewCollapsed() {
		return showDetailViewCollapsed;
	}
	
	public boolean collapseOrExpandDetailView(boolean collapse) {
		return collapseOrExpandDetailView(collapse, false);
	}
	
	public boolean collapseOrExpandDetailView(boolean collapse, boolean skipLayout) {
		if (!showDetailView || showDetailViewCollapsed == collapse)
			return showDetailViewCollapsed;
		
		showDetailViewCollapsed = collapse;
		if (!collapse)
			eastData.setSize(lastDetailViewWidth);
		else {
			lastDetailViewWidth = ((Component) detailView).isRendered() && ((Component) detailView).getElement().getWidth(false) > 0
					? ((Component) detailView).getElement().getWidth(false) : detailViewInitialWidth;
			eastData.setSize(0);
		}
		
		if (!skipLayout)
			forceLayout();
		
		fireViewCollapsedOrExpanded();
		
		return showDetailViewCollapsed;
	}
	
	@Override
	public PersistenceGmSession getGmSession() {
		return gmSession;
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public GmContentView getCurrentMasterView() {
		Widget widget = getCenterWidget();
		if (widget == null)
			return this;
		
		if (widget.equals(currentMasterView))					
			return currentMasterView;
		
		if (widget instanceof GmContentView)
			return (GmContentView) widget;
		
		return this;
	}
	
	/**
	 * Configures the topLevel object, the one used for building up the {@link GmContentView} entries.
	 */
	private void configureTopLevelObject(ModelPath modelPath, boolean initialData) {
		if (initialData) {
			this.modelPath = modelPath;
			if (addedModelPaths != null)
				addedModelPaths.clear();
		} else {
			if (addedModelPaths == null)
				addedModelPaths = new ArrayList<>();
			addedModelPaths.add(modelPath);
		}
		
		if (currentMasterView == null) {
			if (exchangeContentViewAction == null)
				provideAndExchangeView(null, true, null);
			else {
				GmContentViewContext contentViewContext = exchangeContentViewAction.provideGmContentViewContext(modelPath);
				if (contentViewContext != null)
					contentViewContext.setReadOnly(readOnly);
				provideAndExchangeView(contentViewContext, true, null);	
			}
		} else {
			if (initialData) {
				currentMasterView.setContent(modelPath);
				currentMasterView.setReadOnly(readOnly);
			} else if (currentMasterView instanceof GmListView)
				((GmListView) currentMasterView).addContent(modelPath);
		}
		
		if (currentMasterView == null)
			return;

		/*		
		boolean containsExchangeContentView = false;
		boolean containsMaximizeView = false;
		if (externalActions == null)
			externalActions = new ArrayList<>();
		else {
			for (Pair<String, ModelAction> entry : externalActions) {
				if (entry.getFirst().equals(KnownActions.EXCHANGE_CONTENT_VIEW.getName())) {
					containsExchangeContentView = true;
				} else if (entry.getFirst().equals(KnownActions.MAXIMIZE.getName())) {
					containsMaximizeView = true;
				}
			}
		}
		
		boolean actionsAdded = false;
		if (exchangeContentViewAction != null && !containsExchangeContentView) {
			actionsAdded = true;
			externalActions.add(new Pair<String, ModelAction>(KnownActions.EXCHANGE_CONTENT_VIEW.getName(), exchangeContentViewAction));
		}
		
		if (!containsMaximizeView && maximizeViewAction != null) {
			actionsAdded = true;
			maximizeViewAction.configureGmContentView(this);
			externalActions.add(new Pair<String, ModelAction>(KnownActions.MAXIMIZE.getName(), maximizeViewAction));
		}
		
		if (actionsAdded)
			configureExternalActions(externalActions);
		*/
	}
	
	private void provideContentView(GmContentViewContext providerAndName, Future<GmContentView> provideFuture) {
		GmContentView contentView = null;
		
		if (providerAndName != null) {
			contentView = providedContentViews.get(providerAndName);
			if (contentView == null) {
				try {
					contentView = providerAndName.getContentViewProvider().get();
				} catch (Exception ex) { //An exception may be raised while getting the view
					provideFuture.onFailure(ex);
					return;
				}
			}
		}

		if (contentView == null)
			contentView = getDefaultMasterView();
		
		if (currentMasterView != null)
			this.remove((Widget) currentMasterView);
		
		if (contentView instanceof Widget)
			this.setCenterWidget((Widget) contentView);
		
		if (!(contentView instanceof GmExternalViewInitializationSupport) || contentView.isViewReady())
			provideFuture.onSuccess(contentView);
		else {
			((GmExternalViewInitializationSupport) contentView).addInitializationListener(this);
			contentFutureMap.put((GmExternalViewInitializationSupport) contentView, provideFuture);
		}
	}
	
	public void provideAndExchangeView(GmContentViewContext providerAndName, boolean clearContent, Future<GmContentView> providerFuture) {
		if (currentContentViewContext!= null && currentContentViewContext.equals(providerAndName))
			return;
		
		currentContentViewContext = providerAndName;
		
		Future<GmContentView> contentFuture = contentViewContextMap.get(providerAndName);
		if (contentFuture != null) {
			if (providerFuture != null)
				contentFuture.get(providerFuture);
			if (clearContent && !clearContentMap.get(providerAndName))
				clearContentMap.put(providerAndName, clearContent);
			return;
		}
		
		contentFuture = new Future<>();
		contentViewContextMap.put(providerAndName, contentFuture);
		clearContentMap.put(providerAndName, clearContent);
		
		provideContentView(providerAndName, contentFuture);
		contentFuture.onError(e -> ErrorDialog.show(e.getMessage(), e)).andThen(contentView -> {
			boolean configureView = providerAndName == null;
			boolean showDetails = true;
			if (providerAndName != null) {
				showDetails = providerAndName.getShowDetails();
				GmContentView checkContentView = providedContentViews.get(providerAndName);
				if (checkContentView == null) {
					configureView = true;
					if (contentView != null && contentView != defaultMasterView)
						providedContentViews.put(providerAndName, contentView);
				}
			}
			
			if (configureView) {
				contentView.configureGmSession(gmSession);
				contentView.configureUseCase(useCase);
				if (contentView instanceof GmActionSupport)
					((GmActionSupport) contentView).configureExternalActions(externalActions);
			}
			
			if (providerAndName != null && providerAndName.getUseCase() != null)
				contentView.configureUseCase(providerAndName.getUseCase());

			if (exchangeContentViewAction != null && providerAndName != null)
				exchangeContentViewAction.adapt(providerAndName);
		
			if (showDetailView) {
				if (wrapperDetailPanel != null && detailView instanceof Widget)
					wrapperDetailPanel.remove((Widget) detailView);
				
				GmEntityView newDetailView = masterViewToDetailView.get(contentView);
				if (newDetailView != null) {
					setDetailViewSupplier(() -> newDetailView);
					initializeDetailView();
				} else {
					if (providerAndName != null && providerAndName.getDetailViewProvider() != null) {
						setDetailViewSupplier(providerAndName.getDetailViewProvider());
						initializeDetailView();
						detailView.configureGmSession(gmSession);
					}
				}
				
				if (wrapperDetailPanel != null && detailView instanceof Widget)
					wrapperDetailPanel.setCenterWidget((Widget) detailView);
				collapseOrExpandDetailView(!showDetails);
			}
			
			boolean clearContentFromMap = clearContentMap.get(providerAndName);
			exchangeMasterView(contentView, clearContentFromMap);
			fireGmExchangeMasterViewListeners(contentView);
			
			if (providerFuture != null)
				providerFuture.onSuccess(contentView);
			
			contentViewContextMap.remove(providerAndName);
			clearContentMap.remove(providerAndName);
		});
	}
	
	private void exchangeMasterView(GmContentView masterView, boolean clearContent) {
		if (currentMasterView == masterView) {
			GMEUtil.setContent(modelPath, currentMasterView);
			return;
		}
		
		GmContentContext context = null;
		if (currentMasterView != null) {
			if (currentMasterView instanceof GmContentSupport)
				context = ((GmContentSupport) currentMasterView).getGmContentContext();
			if (clearContent)
				currentMasterView.setContent(null);
			if (showDetailView)
				currentMasterView.removeSelectionListener(getMasterAndDetailLinker());
			if (currentMasterView instanceof GmViewport)
				((GmViewport) currentMasterView).removeGmViewportListener(getViewportListener());
			currentMasterView.removeSelectionListener(getSelectionListener());
			if (currentMasterView instanceof GmCheckSupport)
				((GmCheckSupport) currentMasterView).removeCheckListener(getCheckListener());
			if (currentMasterView instanceof GmInteractionSupport)
				((GmInteractionSupport) currentMasterView).removeInteractionListener(getInteractionListener());
			if (currentMasterView instanceof GmContentSupport)
				((GmContentSupport) currentMasterView).removeGmViewChangeListener(getViewChangeListener());
			
			if (currentMasterView instanceof GmTreeView)
				((GmTreeView) currentMasterView).saveScrollState();
			
			if (detailView instanceof GmDetailViewSupport && currentMasterView instanceof GmDetailViewListener)
				((GmDetailViewSupport) detailView).removeDetailViewListener((GmDetailViewListener) currentMasterView);
			
			//if (currentMasterView instanceof Widget)
			//	this.remove((Widget) currentMasterView);
		}
		
		currentMasterView = masterView;
		if (currentMasterView instanceof GmViewIdProvider) {
			String rootId = ((GmViewIdProvider) currentMasterView).getRootId();
			if (!((GmViewIdProvider) currentMasterView).getId().contains(rootId))
				((GmViewIdProvider) currentMasterView).setId(rootId + masterIdCounter++);
		}
		
		if (currentMasterView != null) {
			if (currentMasterView instanceof GmContentSupport)
				((GmContentSupport) currentMasterView).setGmContentContext(context);
			if (currentMasterView instanceof GmTemplateMetadataViewSupport) {
				((GmTemplateMetadataViewSupport) currentMasterView).setColumnData(columnData);
				((GmTemplateMetadataViewSupport) currentMasterView).setAutoExpand(autoExpand);
			}
			
			if (showDetailView)
				currentMasterView.addSelectionListener(getMasterAndDetailLinker());
			if (currentMasterView instanceof GmViewport)
				((GmViewport) currentMasterView).addGmViewportListener(getViewportListener());
			currentMasterView.addSelectionListener(getSelectionListener());
			if (currentMasterView instanceof GmCheckSupport)
				((GmCheckSupport) currentMasterView).addCheckListener(getCheckListener());
			if (currentMasterView instanceof GmInteractionSupport)
				((GmInteractionSupport) currentMasterView).addInteractionListener(getInteractionListener());
			if (currentMasterView instanceof GmContentSupport)
				((GmContentSupport) currentMasterView).addGmViewChangeListener(getViewChangeListener());
			
			if (currentMasterView instanceof QuerySelectionHandler)
				((QuerySelectionHandler) currentMasterView).configureQuerySelectionList(querySelectionList);
			
			if (modelPath == null || currentMasterView.getContentPath() != modelPath)
				GMEUtil.setContent(modelPath, currentMasterView);
			if (addedModelPaths != null && currentMasterView instanceof GmListView) {
				GmListView gmListView = (GmListView) currentMasterView;
				List<ModelPath> currentAddedModelPaths = gmListView.getAddedModelPaths();
				List<ModelPath> modelPathsToAdd = new ArrayList<>(addedModelPaths);
				if (currentAddedModelPaths != null)
					modelPathsToAdd.removeAll(currentAddedModelPaths);
				modelPathsToAdd.forEach(modelPathToAdd -> ((GmListView) currentMasterView).addContent(modelPathToAdd));
			}
			
			if (detailView instanceof GmDetailViewSupport && currentMasterView instanceof GmDetailViewListener)
				((GmDetailViewSupport)detailView).addDetailViewListener((GmDetailViewListener) currentMasterView);
			
			//if (currentMasterView instanceof Widget)
			//	this.setCenterWidget((Widget) currentMasterView);
			if (currentMasterView instanceof GmListView)
				((GmListView) currentMasterView).configureTypeForCheck(typeForCheck);
			if (currentMasterView instanceof HasAddtionalWidgets && detailView instanceof HasAddtionalWidgets) {
				HasAddtionalWidgets hawDetail = ((HasAddtionalWidgets) detailView);
				HasAddtionalWidgets hawMaster = ((HasAddtionalWidgets) currentMasterView);
				hawDetail.configureAdditionalWidgets(hawMaster.getTabbedWidgetContexts());
				if (hawMaster.getDefaultActiveTabbedWidget() != null)
					hawDetail.setActiveTabbedWidget(hawMaster.getDefaultActiveTabbedWidget());
				
				hawDetail.setLink(hawMaster);
				hawMaster.setLink(hawDetail);
			}
		}
		
		if (currentMasterView != null && detailView != null)
			masterViewToDetailView.put(currentMasterView, detailView);
					
		forceLayout();
	}
	
	private GmSelectionListener getMasterAndDetailLinker() {
		if (masterAndDetailLinker != null)
			return masterAndDetailLinker;
		
		masterAndDetailLinker= gmSelectionSupport -> {
			ModelPath modelPath = null;
			if (!(gmSelectionSupport instanceof GmAmbiguousSelectionSupport))
				modelPath = gmSelectionSupport.getFirstSelectedItem();
			else {
				List<List<ModelPath>> modelPathsList = ((GmAmbiguousSelectionSupport) gmSelectionSupport).getAmbiguousSelection();
				if (modelPathsList != null && !modelPathsList.isEmpty()) {
					List<ModelPath> modelPaths = modelPathsList.get(0);
					modelPath = modelPaths.stream().filter(path -> path.last().getValue() instanceof GenericEntity).findFirst().orElse(null);
				}
			}
			
			if (modelPath == null || !modelPath.last().getType().isEntity() || modelPath.last().getValue() == null)
				detailView.setContent(null);
			else {
				final ModelPath selectedModelPath = modelPath;
				Scheduler.get().scheduleDeferred(() -> detailView.setContent(selectedModelPath));
			}
		};
		
		return masterAndDetailLinker;
	}
	
	@Override
	public ActionProviderConfiguration getActions() {
		ActionProviderConfiguration actionProviderConfiguration = actionsByView.get(currentMasterView);
		if (actionProviderConfiguration == null) {
			if (currentMasterView instanceof GmViewActionProvider)
				actionProviderConfiguration = ((GmViewActionProvider) currentMasterView).getActions();
			
			if (externalActions != null && actionProviderConfiguration == null) {
				actionProviderConfiguration = new ActionProviderConfiguration();
				actionProviderConfiguration.addExternalActions(externalActions);
			}
			
			if (currentMasterView != null)
				actionsByView.put(currentMasterView, actionProviderConfiguration);
		}
		
		return actionProviderConfiguration;
	}
	
	@Override
	public boolean isFilterExternalActions() {
		return currentMasterView instanceof GmViewActionProvider ? ((GmViewActionProvider) currentMasterView).isFilterExternalActions() : false;
	}
	
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

	@Override
	public void addGmViewportListener(GmViewportListener vl) {
		if (vl != null) {
			if (viewportListeners == null)
				viewportListeners = new LinkedHashSet<>();
			viewportListeners.add(vl);
		}
	}

	@Override
	public void removeGmViewportListener(GmViewportListener vl) {
		if (viewportListeners != null) {
			viewportListeners.remove(vl);
			if (viewportListeners.isEmpty())
				viewportListeners = null;
		}
	}

	@Override
	public boolean isWindowOverlappingFillingSensorArea() {
		return currentMasterView instanceof GmViewport ? ((GmViewport) currentMasterView).isWindowOverlappingFillingSensorArea() : false;
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

	@Override
	public ModelPath getFirstSelectedItem() {
		return currentMasterView != null ? currentMasterView.getFirstSelectedItem() : null;
	}
	
	@Override
	public int getFirstSelectedIndex() {
		return currentMasterView != null ? currentMasterView.getFirstSelectedIndex() : -1;
	}
	
	@Override
	public GmContentView getView() {
		return this;
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		return currentMasterView != null ? currentMasterView.getCurrentSelection() : null;
	}
	
	@Override
	public List<List<ModelPath>> getAmbiguousSelection() {
		return currentMasterView instanceof GmAmbiguousSelectionSupport ? ((GmAmbiguousSelectionSupport) currentMasterView).getAmbiguousSelection()
				: transformSelection(getCurrentSelection()); 
	}

	@Override
	public boolean isSelected(Object element) {
		return currentMasterView != null ? currentMasterView.isSelected(element) : false;
	}

	@Override
	public boolean selectVertical(Boolean next, boolean keepExisting) {
		return currentMasterView != null ? currentMasterView.selectVertical(next, keepExisting) : false;
	}
	
	@Override
	public boolean selectHorizontal(Boolean next, boolean keepExisting) {
		return currentMasterView != null ? currentMasterView.selectHorizontal(next, keepExisting) : false;
	}	
	
	@Override
	public void select(int index, boolean keepExisting) {
		if (currentMasterView != null)
			currentMasterView.select(index, keepExisting);
	}
	
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
	public List<ModelPath> getCurrentCheckedItems() {
		return currentMasterView instanceof GmCheckSupport ? ((GmCheckSupport) currentMasterView).getCurrentCheckedItems() : null;
	}
	
	@Override
	public ModelPath getFirstCheckedItem() {
		return currentMasterView instanceof GmCheckSupport ? ((GmCheckSupport) currentMasterView).getFirstCheckedItem() : null;
	}
	
	@Override
	public boolean isChecked(Object element) {
		return currentMasterView instanceof GmCheckSupport ? ((GmCheckSupport) currentMasterView).isChecked(element) : false;
	}
	
	@Override
	public boolean uncheckAll() {
		return currentMasterView instanceof GmCheckSupport ? ((GmCheckSupport) currentMasterView).uncheckAll() : false;
	}

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
	
	@Override
	public void addGmViewChangeListener(GmViewChangeListener listener) {
		if (listener != null) {
			if (viewChangeListeners == null)
				viewChangeListeners = new LinkedHashSet<>();
			viewChangeListeners.add(listener);
		}
	}
	
	@Override
	public void removeGmViewChangeListener(GmViewChangeListener listener) {
		if (viewChangeListeners != null) {
			viewChangeListeners.remove(listener);
			if (viewChangeListeners.isEmpty())
				viewChangeListeners = null;
		}
	}

	public void addGmExchangeMasterViewListener(GmExchangeMasterViewListener listener) {
		if (listener != null) {
			if (exchangeMasterViewListeners == null)
				exchangeMasterViewListeners = new LinkedHashSet<>();
			exchangeMasterViewListeners.add(listener);
		}
	}
	
	public void removeGmExchangeMasterViewListener(GmExchangeMasterViewListener listener) {
		if (exchangeMasterViewListeners != null) {
			exchangeMasterViewListeners.remove(listener);
			if (exchangeMasterViewListeners.isEmpty())
				exchangeMasterViewListeners = null;
		}
	}
	
	@Override
	public ModelPath getContentPath() {
		return currentMasterView != null ? currentMasterView.getContentPath() : modelPath;
	}
	
	@Override
	public void configureActionGroup(ActionGroup actionGroup) {
		//NOP
	}

	@Override
	public void setActionManager(GmContentViewActionManager actionManager) {
		//NOP
	}
	
	@Override
	public boolean checkUncondenseLocalEnablement() {
		return currentMasterView instanceof GmCondensationView ? ((GmCondensationView) currentMasterView).checkUncondenseLocalEnablement() : false;
	}
	
	@Override
	public void condense(String propertyName, CondensationMode condensationMode, EntityType<?> entityType) {
		if (!(currentMasterView instanceof GmCondensationView))
			return;
		
		((GmCondensationView) currentMasterView).condense(propertyName, condensationMode, entityType);
	}
	
	@Override
	public void condenseLocal() {
		if (!(currentMasterView instanceof GmCondensationView))
			return;
		
		((GmCondensationView) currentMasterView).condenseLocal();
	}
	
	@Override
	public String getCondensendProperty() {
		if (!(currentMasterView instanceof GmCondensationView))
			return null;
		
		return ((GmCondensationView) currentMasterView).getCondensendProperty();
	}
	
	@Override
	public String getCurrentCondensedProperty(EntityType<?> entityType) {
		if (!(currentMasterView instanceof GmCondensationView))
			return null;
		
		return ((GmCondensationView) currentMasterView).getCurrentCondensedProperty(entityType);
	}
	
	@Override
	public EntityType<GenericEntity> getEntityTypeForProperties() {
		if (!(currentMasterView instanceof GmCondensationView))
			return null;
		
		return ((GmCondensationView) currentMasterView).getEntityTypeForProperties();
	}
	
	@Override
	public GmViewActionBar getGmViewActionBar() {
		if (!(currentMasterView instanceof GmCondensationView))
			return null;
		
		return ((GmCondensationView) currentMasterView).getGmViewActionBar();
	}
	
	@Override
	public boolean isLocalCondensationEnabled() {
		return currentMasterView instanceof GmCondensationView ? ((GmCondensationView) currentMasterView).isLocalCondensationEnabled() : false;
	}
	
	@Override
	public boolean isUseCondensationActions() {
		return currentMasterView instanceof GmCondensationView ? ((GmCondensationView) currentMasterView).isUseCondensationActions() : false;
	}
	
	@Override
	public void uncondenseLocal() {
		if (!(currentMasterView instanceof GmCondensationView))
			return;
		
		((GmCondensationView) currentMasterView).uncondenseLocal();
	}
	
	public GmContentViewContext getCurrentContentViewContext() {
		return currentContentViewContext;
	}	
	
	private GmViewportListener getViewportListener() {
		if (viewportListener == null) {
			viewportListener = source -> {
				if (viewportListeners != null)
					viewportListeners.forEach(listener -> listener.onWindowChanged(source));
			};
		}
		
		return viewportListener;
	}
	
	private GmSelectionListener getSelectionListener() {
		if (selectionListener == null) {
			selectionListener = gmSelectionSupport -> {
				if (selectionListeners != null) {
					List<GmSelectionListener> listenersCopy = new ArrayList<>(selectionListeners);
					listenersCopy.forEach(listener -> listener.onSelectionChanged(gmSelectionSupport));
				}
			};
		}
		
		return selectionListener;
	}
	
	private GmCheckListener getCheckListener() {
		if (checkListener == null) {
			checkListener = gmSelectionSupport -> {
				if (checkListeners != null)
					checkListeners.forEach(listener -> listener.onCheckChanged(gmSelectionSupport));
			};
		}
		
		return checkListener;
	}
	
	private GmInteractionListener getInteractionListener() {
		if (interactionListener != null)
			return interactionListener;
			
		interactionListener = new GmInteractionListener() {
			@Override
			public void onDblClick(GmMouseInteractionEvent event) {
				if (interactionListeners != null)
					interactionListeners.forEach(listener -> listener.onDblClick(event));
			}
			
			@Override
			public void onClick(GmMouseInteractionEvent event) {
				if (interactionListeners != null)
					interactionListeners.forEach(listener -> listener.onClick(event));
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
		
		return interactionListener;
	}
	
	private GmViewChangeListener getViewChangeListener() {
		if (viewChangeListener != null)
			return viewChangeListener;
		
		viewChangeListener = (displayNode, nodeWidth, columnsVisible) -> {
			if (viewChangeListeners != null)
				viewChangeListeners.forEach(listener -> listener.onColumnsChanged(displayNode, nodeWidth, columnsVisible));
		};
		
		return viewChangeListener;
	}
	
	private GmContentView getDefaultMasterView() throws RuntimeException {
		if (defaultMasterView == null) {
			defaultMasterView = defaultMasterViewProvider.get();
			if (externalActions != null && defaultMasterView instanceof GmActionSupport)
				((GmActionSupport) defaultMasterView).configureExternalActions(externalActions);
		}
		
		return defaultMasterView;
	}
	
	private void fireViewCollapsedOrExpanded() {
		if (masterDetailConstellationListeners != null)
			masterDetailConstellationListeners.forEach(listener -> listener.onViewCollapsedOrExpanded());
	}
	
	private void fireDetailsVisibilityChanged(boolean detailsHidden) {
		if (masterDetailConstellationListeners != null)
			masterDetailConstellationListeners.forEach(listener -> listener.onDetailsVisibilityChanged(detailsHidden));
	}
	
	private void fireGmExchangeMasterViewListeners(GmContentView newContentView) {
		if (exchangeMasterViewListeners != null)
			exchangeMasterViewListeners.forEach(listener -> listener.onExchangeMasterView(this, newContentView));
	}	
	
	@Override
	public void disposeBean() throws Exception {
		if (defaultMasterView instanceof DisposableBean)
			((DisposableBean) defaultMasterView).disposeBean();
		
		if (providedContentViews != null) {
			for (GmContentView view : providedContentViews.values()) {
				if (view instanceof DisposableBean)
					((DisposableBean) view).disposeBean();
			}
			providedContentViews.clear();
			providedContentViews = null;
		}
		
		if (detailView instanceof DisposableBean)
			((DisposableBean) detailView).disposeBean();
		
		if (contentViewListeners != null) {
			contentViewListeners.clear();
			contentViewListeners = null;
		}
		
		if (viewportListeners != null) {
			viewportListeners.clear();
			viewportListeners = null;
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
		
		if (!masterViewToDetailView.isEmpty())
			masterViewToDetailView.clear();
		
		if (masterDetailConstellationListeners != null) {
			masterDetailConstellationListeners.clear();
			masterDetailConstellationListeners = null;
		}
		
		if (contentViewInitializationListeners != null) {
			contentViewInitializationListeners.clear();
			contentViewInitializationListeners = null;
		}
		
		actionsByView.clear();
		actionsByView = null;
		
		contentFutureMap.clear();
		contentFutureMap = null;
		
		contentViewContextMap.clear();
		contentViewContextMap = null;
		
		clearContentMap.clear();
		contentViewContextMap = null;
	}
	
	@Override
	public void onExternalViewInitialized(GmExternalViewInitializationSupport instantiatedSupport) {
        Future<GmContentView> future = contentFutureMap.get(instantiatedSupport);
        if (future != null) {		
        	future.onSuccess((GmContentView) instantiatedSupport);
        	contentFutureMap.remove(instantiatedSupport);
        	fireInstantiatedListeners(instantiatedSupport);
        }
	}
	
	@Override
	public void addInitializationListener(GmExternalViewInitializationListener listener) {
		if (contentViewInitializationListeners == null)
			contentViewInitializationListeners = new ArrayList<>();
		
		contentViewInitializationListeners.add(listener);
	}
	
	@Override
	public void removeInitializationListener(GmExternalViewInitializationListener listener) {
		if (contentViewInitializationListeners != null) {
			contentViewInitializationListeners.remove(listener);
			if (contentViewInitializationListeners.isEmpty())
				contentViewInitializationListeners = null;
		}
	}
	
	@Override
	public boolean isViewReady() {
		return currentMasterView == null ? false : currentMasterView.isViewReady();
	}
	
	private void fireInstantiatedListeners(GmExternalViewInitializationSupport instantiatedSupport) {
		if (contentViewInitializationListeners == null)
			return;
		
		for (GmExternalViewInitializationListener listener : new ArrayList<>(contentViewInitializationListeners))
			listener.onExternalViewInitialized(instantiatedSupport);
	}
	
	public interface MasterDetailConstellationListener {
		void onViewCollapsedOrExpanded();
		void onDetailsVisibilityChanged(boolean detailsHidden);
	}
	
	//set NULL to restore standard used Widget
	public void exchangeWidget(Widget widget) {	
		if (widget == null) {
			widget = currentWidget;
		} else {
			if (currentWidget == null)
				currentWidget = getCenterWidget();
		}
		
		if (getCenterWidget().equals(widget)) {
			return;
		}
	
		setCenterWidget(widget);
		doLayout();
	}
	
}
