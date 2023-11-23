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

import static com.braintribe.model.processing.session.api.common.GmSessions.getMetaData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.action.client.TriggerKnownProperties;
import com.braintribe.gwt.gme.assemblypanel.client.AssemblyPanel;
import com.braintribe.gwt.gme.assemblypanel.client.model.AbstractGenericTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.MapKeyAndValueTreeModel;
import com.braintribe.gwt.gme.constellation.client.BrowsingConstellationDialog.ValueDescriptionBean;
import com.braintribe.gwt.gme.constellation.client.action.ExchangeContentViewAction;
import com.braintribe.gwt.gme.tetherbar.client.TetherBar;
import com.braintribe.gwt.gme.tetherbar.client.TetherBar.TetherBarListener;
import com.braintribe.gwt.gme.tetherbar.client.TetherBarElement;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabActionMenu;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabElement;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabPanel.VerticalTabListener;
import com.braintribe.gwt.gmview.action.client.ActionGroup;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.action.client.WorkWithEntityExpert;
import com.braintribe.gwt.gmview.actionbar.client.ActionProviderConfiguration;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionProvider;
import com.braintribe.gwt.gmview.client.GmActionSupport;
import com.braintribe.gwt.gmview.client.GmAmbiguousSelectionSupport;
import com.braintribe.gwt.gmview.client.GmCheckListener;
import com.braintribe.gwt.gmview.client.GmCheckSupport;
import com.braintribe.gwt.gmview.client.GmContentSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewActionManager;
import com.braintribe.gwt.gmview.client.GmContentViewContext;
import com.braintribe.gwt.gmview.client.GmContentViewListener;
import com.braintribe.gwt.gmview.client.GmEntityView;
import com.braintribe.gwt.gmview.client.GmExchangeMasterViewListener;
import com.braintribe.gwt.gmview.client.GmInteractionListener;
import com.braintribe.gwt.gmview.client.GmInteractionSupport;
import com.braintribe.gwt.gmview.client.GmListView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.GmTreeView;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ParentModelPathSupplier;
import com.braintribe.gwt.gmview.client.ReloadableGmView;
import com.braintribe.gwt.gmview.client.ViewSituationResolver;
import com.braintribe.gwt.gmview.client.WorkWithEntityActionListener;
import com.braintribe.gwt.gmview.client.js.GmExternalViewInitializationListener;
import com.braintribe.gwt.gmview.client.js.GmExternalViewInitializationSupport;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedBorderLayoutContainer;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.enhance.EnhancedEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.path.EntryPointPathElement;
import com.braintribe.model.generic.path.ListItemPathElement;
import com.braintribe.model.generic.path.MapKeyPathElement;
import com.braintribe.model.generic.path.MapValuePathElement;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyPathElement;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.path.SetItemPathElement;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.prompt.DefaultNavigation;
import com.braintribe.model.meta.data.prompt.DefaultView;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.Query;
import com.braintribe.model.workbench.WorkbenchAction;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.resources.CommonStyles;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

public class BrowsingConstellation extends BorderLayoutContainer implements InitializableBean, WorkWithEntityActionListener, GmContentView,
		GmCheckSupport, GmInteractionSupport, GmListView, GmViewActionProvider, GmActionSupport, ParentModelPathSupplier, ReloadableGmView,
		GmContentSupport, DisposableBean, GmExchangeMasterViewListener, GmExternalViewInitializationSupport, GmAmbiguousSelectionSupport {
	private static final int TETHER_BAR_SIZE = 40;
	private static final int ACTION_BAR_SIZE = 250;
	private static int tetherBarIdCounter = 0;
	
	private TetherBar tetherBar;
	private GmContentView currentContentView;
	private PersistenceGmSession gmSession;
	//private GmSelectionListener detailSelectionListener;
	private Supplier<MasterDetailConstellation> masterDetailConstellationProvider;
	private Supplier<? extends GmContentView> masterViewProvider;
	private Map<Class<?>, TraversingCriterion> specialEntityTraversingCriterion;
	private Supplier<ViewSituationResolver<GmContentViewContext>> viewSituationResolverSupplier;
	private final Map<TetherBarElement, PropertyRelatedModelPathElement> tetherBarEntitiesMap = new HashMap<>();
	private ManipulationListener manipulationListener;
	private ManipulationListener deleteManipulationListener;
	private boolean insertOnlyLastPathElementToTether = true;
	private BorderLayoutData tetherBarLayoutData = new BorderLayoutData(TETHER_BAR_SIZE);
	private VerticalTabActionMenu verticalTabActionBar;
	private BorderLayoutContainer topPanel;
	private List<Action> listAction;
	private Map<String, Supplier<? extends BiConsumer<Widget, ModelPath>>> specialViewHandlers;
	private WorkWithEntityExpert workWithEntityExpert;
	private boolean tetherBarVisibleOnlyWithMultipleItems = false;
	private boolean tetherBarVisible = true;
	private boolean verticalTabActionBarVisible = false;
	private List<GmExchangeMasterViewListener> exchangeListeners = new ArrayList<>();
	
	public BrowsingConstellation() {
		this.setBorders(false);
	}
	
	/**
	 * Configures the required {@link TetherBar}, used for navigating through Constellations.
	 */
	@Required
	public void setTetherBar(TetherBar tetherBar) {
		tetherBar.getElement().setId("gmTetherBar" + tetherBarIdCounter++);
		tetherBar.addStyleName("browsingConstellationTetherBar");
		this.tetherBar = tetherBar;
	}
	
	/**
	 * Configures the required provider for {@link MasterDetailConstellation}s.
	 */
	@Required
	public void setMasterDetailConstellationProvider(final Supplier<MasterDetailConstellation> masterDetailConstellationProvider) {
		this.masterDetailConstellationProvider = masterDetailConstellationProvider;
	}
	
	/**
	 * Configures the required provider for {@link GmContentView}s used as master view.
	 */
	@Required
	public void setMasterViewProvider(Supplier<? extends GmContentView> masterViewProvider) {
		this.masterViewProvider = masterViewProvider;
	}
	
	/**
	 * Configures the required {@link ViewSituationResolver}.
	 */
	@Required
	public void setViewSituationResolver(Supplier<ViewSituationResolver<GmContentViewContext>> viewSituationResolverSupplier) {
		this.viewSituationResolverSupplier = viewSituationResolverSupplier;
	}
	
	/**
	 * Configures a map containing special traversing criterion for the given entities.
	 * This is used when loading an absent property. Special entities (such as {@link LocalizedString}) require some properties to be loaded.
	 */
	@Configurable
	public void setSpecialEntityTraversingCriterion(Map<Class<?>, TraversingCriterion> specialEntityTraversingCriterion) {
		this.specialEntityTraversingCriterion = specialEntityTraversingCriterion;
	}
	
	public void setBrowsingConstellationActionBar(VerticalTabActionMenu browsingConstellationActionBar) {
		this.verticalTabActionBar = browsingConstellationActionBar;
	}
	
	public void setVisibleBrowsingConstellationActionBar(boolean visible) {
		this.verticalTabActionBarVisible = visible;
		if (verticalTabActionBar != null) {
			verticalTabActionBar.setShowDynamicTabElements(verticalTabActionBarVisible);
			verticalTabActionBar.setUseContentMenuAction(verticalTabActionBarVisible);
		}
	}
	
	public void setBrowsingConstellationDefaultModelActions(List<Action> list) {
		this.listAction = list;
	}	
	
	/**
	 * Configures whether only the last path element should be added to the {@link TetherBar}, or all of the elements in the path.
	 * Defaults to true (only the last one is inserted).
	 */
	@Configurable
	public void setInsertOnlyLastPathElementToTether(boolean insertOnlyLastPathElementToTether) {
		this.insertOnlyLastPathElementToTether = insertOnlyLastPathElementToTether;
	}
	
	/**
	 * Configures special handlers for a given {@link DefaultView}.
	 */
	@Configurable
	public void setSpecialViewHandlers(Map<String, Supplier<? extends BiConsumer<Widget, ModelPath>>> specialViewHandlers) {
		this.specialViewHandlers = specialViewHandlers;
	}
	
	/**
	 * Configures the required provider for {@link WorkWithEntityExpert}s.
	 */
	@Configurable
	public void setWorkWithEntityExpert(WorkWithEntityExpert workWithEntityExpert) {
		this.workWithEntityExpert = workWithEntityExpert;
	}
	
	/**
	 * Configures whether the tetherBar is visible only when having more than one item.
	 * Defaults to false.
	 */
	@Configurable
	public void setTetherBarVisibleOnlyWithMultipleItems(boolean tetherBarVisibleOnlyWithMultipleItems) {
		this.tetherBarVisibleOnlyWithMultipleItems = tetherBarVisibleOnlyWithMultipleItems;
		if (tetherBarVisibleOnlyWithMultipleItems)
			hideTetherBar();
	}
	
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		if (this.gmSession != null && deleteManipulationListener != null)
			this.gmSession.listeners().remove(deleteManipulationListener);
		
		this.gmSession = gmSession;
		if (gmSession != null)
			gmSession.listeners().add(getDeleteManipulationListener());
	}
	
	public void configureTopPanelVisibility(boolean visible) {
		if (visible) {
			if (topPanel.getParent() == null)
				this.setNorthWidget(topPanel, tetherBarLayoutData);
		} else
			topPanel.removeFromParent();
	}
	
	public void addExchangeMasterViewListener(GmExchangeMasterViewListener listener) {
		this.exchangeListeners.add(listener);
	}

	public void removeExchangeMasterViewListener(GmExchangeMasterViewListener listener) {
		this.exchangeListeners.remove(listener);
	}
	
	@Override
	public void intializeBean() throws Exception {	
		prepareTopPanel();
	
		tetherBarLayoutData.setSize(TETHER_BAR_SIZE);
		this.setNorthWidget(topPanel, tetherBarLayoutData);
		
		tetherBar.addTetherBarListener(new TetherBarListener() {
			@Override
			public void onTetherBarElementSelected(TetherBarElement tetherBarElement) {
				GmContentView contentView = tetherBarElement.getContentView();
				configureCurrentContentView(contentView);
				
				if (contentView instanceof ReloadableGmView && ((ReloadableGmView) contentView).isReloadPending())
					((ReloadableGmView) contentView).reloadGmView();
			}

			@Override
			public void onTetherBarElementAdded(TetherBarElement tetherBarElementAdded) {
				if (tetherBarVisibleOnlyWithMultipleItems && tetherBar.getElementsSize() > 1)
					restoreTetherBar();
			}

			@Override
			public void onTetherBarElementsRemoved(List<TetherBarElement> tetherBarElementsRemoved) {
				if (tetherBarElementsRemoved != null)
					tetherBarElementsRemoved.forEach(element -> tetherBarEntitiesMap.remove(element));
				
				if (tetherBarVisibleOnlyWithMultipleItems && tetherBar.getElementsSize() == 1)
					hideTetherBar();
			}
		});
	}

	private void prepareTopPanel() {
		topPanel = new ExtendedBorderLayoutContainer();
		topPanel.setStyleName("browsingConstellationTopPanel");
		if (verticalTabActionBar != null) {
			verticalTabActionBar.setShowDynamicTabElements(verticalTabActionBarVisible);
			verticalTabActionBar.setUseContentMenuAction(verticalTabActionBarVisible);
			verticalTabActionBar.setStaticActionGroup(listAction);
			
			verticalTabActionBar.addStyleName("browsingConstellationActionBar");
			topPanel.setEastWidget(verticalTabActionBar, new BorderLayoutData(ACTION_BAR_SIZE));
			verticalTabActionBar.removeStyleName(CommonStyles.get().positionable());
			
			verticalTabActionBar.addVerticalTabListener(new VerticalTabListener() {
				@Override
				public void onVerticalTabElementSelected(VerticalTabElement previousVerticalTabElement, VerticalTabElement verticalTabElement) {
					if (verticalTabElement == null)
						return;
									
					Object object = verticalTabElement.getModelObject();
					Widget widget = verticalTabElement.getWidget();
										
					if (widget instanceof Menu) {
						int leftMenu = ((Menu) widget).getData("left");
						int topMenu = ((Menu) widget).getData("top");
						((Menu) widget).showAt(leftMenu, topMenu);
						return;
					} else if (widget instanceof MenuItem) {
						if (object == null)
							object = ((MenuItem) widget).getData("action");
					}
					
					if (!(object instanceof Action))
						return;
										
					Element element = verticalTabElement.getElement();
					TriggerInfo triggerInfo = new TriggerInfo(); 
					triggerInfo.put(TriggerKnownProperties.PROPERTY_CLICKEDELEMENT, element);						
					((Action) object).perform(triggerInfo);
					updateElement((Action) object);
					verticalTabActionBar.refresh();
				}
				
				@Override
				public void onVerticalTabElementAddedOrRemoved(int elements, boolean added, List<VerticalTabElement> verticalTabElements) {
					//NOP						
				}
				
				@Override
				public void onHeightChanged(int newHeight) {
					//NOP					
				}
			});
			
		}
		
		topPanel.setCenterWidget(tetherBar);
		tetherBar.removeStyleName(CommonStyles.get().positionable());
	}
	
	@Override
	public PersistenceGmSession getGmSession() {
		return gmSession;
	}
	
	@Override
	public void configureUseCase(String useCase) {
		//NOP
	}
	
	@Override
	public String getUseCase() {
		return null;
	}
	
	public TetherBar getTetherBar() {
		return tetherBar;
	}
	
	public void configureCurrentContentView(GmContentView currentContentView) {
		if (this.currentContentView == currentContentView)
			return;
		
		boolean doLayout = false;
		if (this.currentContentView != null) {
			GmTreeView gmTreeView = getGmTreeView(this.currentContentView);
			if (gmTreeView != null)
				gmTreeView.saveScrollState();
			//this.currentContentView.removeSelectionListener(getDetailSelectionListener());
			this.currentContentView.removeSelectionListener(verticalTabActionBar);

			/*
			AssemblyPanel ap = getChildAssemblyPanel(this.currentContentView);
			if (ap != null)
				ap.saveScrollState();
			*/	
			
			if (this.currentContentView instanceof Widget) {
				remove((Widget) this.currentContentView);
				doLayout = true;
			}
		}
				
		this.currentContentView = currentContentView;
		//currentContentView.addSelectionListener(getDetailSelectionListener());
		
		GmTreeView gmTreeView = getGmTreeView(currentContentView);
		if (gmTreeView != null)
			gmTreeView.limitAmountOfDataToRender();
		
		if (currentContentView instanceof Widget) {
			this.setCenterWidget((Widget) currentContentView);
			doLayout = true;
		}
		
		if (listAction != null && !listAction.isEmpty() && verticalTabActionBar != null) {
			updateVerticalTabActionBar(this.currentContentView);
		}
			
		//if (browsingConstellationActionBar != null)
			//browsingConstellationActionBar.refresh();
		
		if (doLayout)
			doLayout();
	}

	/*
	 * Updates the BrowsingConstellation's view ActionBar
	 * GmContentView contentView - if is null, than is used currentContentView
	 */
	public void updateVerticalTabActionBar(GmContentView contentView) {		
		GmContentView viewToUse;
		if (contentView == null)
			viewToUse = currentContentView;
		else
			viewToUse = contentView;
		
		MasterDetailConstellation masterView =  getChildMasterDetailConstellation(currentContentView);
		
		if (masterView != null) {
			masterView.removeGmExchangeMasterViewListener(this);
			masterView.addGmExchangeMasterViewListener(this);
		}
		
		Scheduler.get().scheduleDeferred(() -> handleUpdateBrowsingConstellationActionBar(viewToUse));
	}
	
	private void handleUpdateBrowsingConstellationActionBar(GmContentView contentView) {
		if (verticalTabActionBar == null)
			return;
		
		GmContentView bottomLevelView = getLatestChildGmContentView(contentView);			
		
		if (bottomLevelView != null)
			bottomLevelView.addSelectionListener(verticalTabActionBar);
		verticalTabActionBar.configureGmConentView(bottomLevelView);
		doLayout();
	}

	private void updateElement(Action action) {
		if (action == null)
			return;
		
		VerticalTabElement element = null;
		if (verticalTabActionBar != null) {
			element = verticalTabActionBar.getVerticalTabElementByModelObject(action);
			if (element == null)
				return;
			
			verticalTabActionBar.updateElement(element);
		}
	}
	
	public GmContentView getCurrentContentView() {
		return currentContentView;
	}
	
	@Override
	public void onWorkWithEntity(ModelPath modelPath, ModelPathElement selectedModelPathElement, String useCase, boolean forcePreferredUseCase, boolean readOnly) {		
		onWorkWithEntity(modelPath, null, selectedModelPathElement, useCase, forcePreferredUseCase, readOnly);
	}

	public void onWorkWithEntity(ModelPath modelPath, List<ModelPathElement> workWithModelPath, ModelPathElement selectedModelPathElement, String useCase, boolean forcePreferredUseCase) {		
		onWorkWithEntity(modelPath, workWithModelPath, selectedModelPathElement, useCase, forcePreferredUseCase, false);
	}		
	
	public void onWorkWithEntity(ModelPath modelPath, List<ModelPathElement> workWithModelPath, ModelPathElement selectedModelPathElement, String useCase, boolean forcePreferredUseCase, boolean readOnly) {		
		addModelPathToTether(modelPath, workWithModelPath, selectedModelPathElement, useCase, forcePreferredUseCase, insertOnlyLastPathElementToTether, readOnly);
	}	
	
	@Override
	public boolean isWorkWithAvailable(ModelPath modelPath) {
		ModelPathElement lastPath = modelPath.last();
		if (lastPath.getType().isCollection())
			return checkAddElementToTether(modelPath);
		
		if (!(lastPath.getValue() instanceof GenericEntity))
			return false;
		
		if (lastPath instanceof PropertyRelatedModelPathElement)
			return checkAddElementToTether(modelPath);
		
		if (lastPath instanceof RootPathElement)
			return checkAddEntityToPath(modelPath);
		
		return false;
	}
	
	@Override
	public ActionProviderConfiguration getActions() {
		if (currentContentView instanceof GmViewActionProvider)
			return ((GmViewActionProvider) currentContentView).getActions();
		return null;
	}
	
	@Override
	public boolean isFilterExternalActions() {
		if (currentContentView instanceof GmViewActionProvider)
			return ((GmViewActionProvider) currentContentView).isFilterExternalActions();
		return false;
	}
	
	@Override
	public ModelPath apply(GmContentView parentView) {
		TetherBarElement element;
		if (parentView != null)
			element = tetherBar.getElementByView(parentView);
		else
			element = tetherBar.getSelectedElement();
		
		return element == null ? null : element.getModelPath();
	}
	
	@Override
	public void reloadGmView() {
		GmContentView currentContentView = tetherBar.getSelectedElement().getContentViewIfProvided();
		if (currentContentView instanceof ReloadableGmView)
			((ReloadableGmView) currentContentView).reloadGmView();
	}
	
	@Override
	public boolean isReloadPending() {
		GmContentView currentContentView = tetherBar.getSelectedElement().getContentViewIfProvided();
		return currentContentView instanceof ReloadableGmView ? ((ReloadableGmView) currentContentView).isReloadPending() : false;
	}
	
	@Override
	public void setReloadPending(boolean reloadPending) {
		tetherBar.getTetherBarElements().stream().filter(el -> el.getContentViewIfProvided() instanceof ReloadableGmView)
				.forEach(el -> ((ReloadableGmView) el.getContentViewIfProvided()).setReloadPending(reloadPending));
	}
	
	public void hideTetherBar() {
		if (tetherBarVisible) {
			tetherBarLayoutData.setSize(0);
			tetherBar.getElement().getStyle().setVisibility(Visibility.HIDDEN);
			tetherBarVisible = false;
			forceLayout();
		}
	}
	
	public void restoreTetherBar() {
		if (!tetherBarVisible) {
			tetherBarLayoutData.setSize(TETHER_BAR_SIZE);
			tetherBar.getElement().getStyle().clearVisibility();
			tetherBarVisible = true;
			forceLayout();
		}
	}
	
	/*private GmSelectionListener getDetailSelectionListener() {
		if (detailSelectionListener == null) {
			detailSelectionListener = (gmSelectionSupport) -> {
				if (gmSelectionSupport.getView() instanceof PropertyPanel)
					addModelPathToTether(gmSelectionSupport.getFirstSelectedItem(), gmSelectionSupport.getView().getUseCase(), false);
			};
		}
		
		return detailSelectionListener;
	}*/
	
	private void addModelPathToTether(ModelPath modelPath, List<ModelPathElement> workWithModelPath, ModelPathElement selectedModelPathElement, String useCase, boolean forcePreferredUseCase, boolean insertOnlyLastPathElementToTether, boolean readOnly) {
		if (modelPath == null)
			return;
				
		if (selectedModelPathElement == null)
			selectedModelPathElement = modelPath.last();
		handleEntityNavigation(modelPath, useCase);
		
		int index = insertOnlyLastPathElementToTether ? modelPath.size() - 1 : 0;
		int tetherIndex = -1;
		while (index < modelPath.size() && modelPath.get(index) != null) {
			ModelPathElement modelPathElement = modelPath.get(index);
			Boolean select = insertOnlyLastPathElementToTether || (modelPathElement == selectedModelPathElement);
			index = index + 1;
			
			if (workWithModelPath != null&& workWithModelPath.contains(modelPathElement)) {
				WorkbenchAction actionToPerform = null;
				if (workWithEntityExpert != null /*&& workWithEntityExpert.checkWorkWithAvailable(modelPath, (Widget) currentContentView)*/)
					actionToPerform = workWithEntityExpert.getActionToPerform(modelPath);
				
				if (actionToPerform != null) {
					workWithEntityExpert.configureGmSession(gmSession);
					workWithEntityExpert.performAction(modelPath, actionToPerform, currentContentView, false);
					continue;
				}
			}
			
			Object value = modelPathElement.getValue();
			if (modelPathElement instanceof PropertyRelatedModelPathElement) {
				if (modelPathElement.getType().isCollection() || !(value instanceof GenericEntity) ) {
					tetherIndex = addCollectionPropertyToTether(modelPath, modelPathElement, select, useCase, forcePreferredUseCase, false, tetherIndex, readOnly) + 1;
					continue;
				}
			}
			if (value instanceof GenericEntity) {
				useCase = GMEMetadataUtil.getSpecialDefaultView(useCase, (GenericEntity) value);			
				if (!forcePreferredUseCase && specialViewHandlers != null) {
					Supplier<? extends BiConsumer<Widget, ModelPath>> viewHandlerSupplier = specialViewHandlers.get(useCase);
					if (viewHandlerSupplier != null) {
						viewHandlerSupplier.get().accept(this, modelPath);
						continue;
					}
				}
			}
			
			if (modelPathElement instanceof ListItemPathElement || modelPathElement instanceof SetItemPathElement || modelPathElement instanceof MapValuePathElement
					|| modelPathElement instanceof MapKeyPathElement) {
				tetherIndex = addCollectionElementToTether(modelPath, modelPathElement, select, useCase, forcePreferredUseCase, tetherIndex, readOnly) + 1;
			} else if (modelPathElement instanceof PropertyRelatedModelPathElement)
				tetherIndex = addEntityPropertyToTether(modelPath, modelPathElement, select, useCase, forcePreferredUseCase, tetherIndex, readOnly) + 1;
			else if (modelPathElement instanceof RootPathElement)
				tetherIndex = addEntityToPath(modelPath, modelPathElement, select, useCase, forcePreferredUseCase, tetherIndex) + 1;
		}
	}
	
	private boolean checkAddElementToTether(ModelPath modelPath) {
		int elementIndex = tetherBar.getSelectedElementIndex() + 1;
		TetherBarElement element = tetherBar.getElementAt(elementIndex);
		if (element == null || !element.getModelPathElement().equals(modelPath.last()))
			return true;
		
		return false;
	}
	
	private int addCollectionPropertyToTether(ModelPath originalModelPath, ModelPathElement originalPathElement, boolean select, String useCase, boolean forcePreferredUseCase, boolean removeElementFromPath, int tetherIndex, boolean readOnly) {
		int returnIndex = tetherIndex;
		
		ModelPath modelPath;		
		ModelPathElement collectionElementPath = null;
		
		if (originalPathElement == null)
			originalPathElement = originalModelPath.last();
		
		ModelPathElement modelElement = originalPathElement;
		if (removeElementFromPath) {
			collectionElementPath = originalPathElement;
			modelPath = new ModelPath();
			for (ModelPathElement element : originalModelPath) {
				if (element == originalPathElement)
					break;
				modelPath.add(element.copy());
			}
			modelElement = modelPath.last();
		} else
			modelPath = originalModelPath;
		
		if (!modelElement.getType().isCollection())
			return returnIndex;
		
		boolean isSimpleTypeCollection = true;
		CollectionType collectionType = modelElement.getType();
		for (GenericModelType type : collectionType.getParameterization()) {
			if (type.isEntity()) {
				isSimpleTypeCollection = false;
				break;
			}
		}
		
		final PropertyRelatedModelPathElement propertyPathElement = (PropertyRelatedModelPathElement) modelElement;
		int collectionIndex = (tetherIndex < 0) ? tetherBar.getSelectedElementIndex() + 1 : tetherIndex;
		GenericEntity parentEntity = propertyPathElement.getEntity();
		EntityType<GenericEntity> parentEntityType = parentEntity.entityType();
		String propertyName = propertyPathElement.getProperty().getName();
		String collectionDisplayName = GMEMetadataUtil.getPropertyDisplay(propertyName, getMetaData(parentEntity).entity(parentEntity).property(propertyName).useCase(useCase));
		ValueDescriptionBean valueDescriptionBean = getValueDescriptionBean(parentEntity, parentEntityType, useCase);
		if (!insertOnlyLastPathElementToTether) {
			TetherBarElement parentEntityElement = tetherBar.getTetherBarElementByModelPathElement(propertyPathElement.getPrevious());
			int entityIndex = tetherBar.getIndexOfElement(parentEntityElement);
			if (parentEntityElement == null || entityIndex < tetherBar.getSelectedElementIndex()) {
				ModelPath newModelPath = prepareNewModelPath(parentEntity, modelPath);
				TetherBarElement element = new TetherBarElement(newModelPath, valueDescriptionBean.getValue(), valueDescriptionBean.getDescription(),
						getMasterDetailConstellationProvider(masterDetailConstellationProvider, prepareNewModelPath(parentEntity, modelPath), useCase, forcePreferredUseCase, readOnly));
				entityIndex = (tetherIndex < 0) ? tetherBar.getSelectedElementIndex() + 1 : tetherIndex;
				tetherBar.insertTetherBarElement(entityIndex, element);
			}
			collectionIndex = entityIndex + 1;
		}
		
		TetherBarElement collectionElement = tetherBar.getElementAt(collectionIndex);
		Object collection = modelElement.getValue();
		if (collectionElement != null && collectionElement.getModelPathElement().equals(modelElement)) {
			if (select)
				tetherBar.setSelectedThetherBarElement(collectionElement);			
			return tetherBar.getIndexOfElement(collectionElement);
		}
		
		String tetherBarName = collectionDisplayName;
		if (insertOnlyLastPathElementToTether)
			tetherBarName = LocalizedText.INSTANCE.entityProperty(valueDescriptionBean.getValue(), collectionDisplayName);
		
		ModelPath newModelPath = prepareNewModelPath(collection, modelPath);
		if (isSimpleTypeCollection) {
			collectionElement = new TetherBarElement(newModelPath, tetherBarName, collectionDisplayName,
					getMasterViewProvider(masterViewProvider, newModelPath, collectionElementPath));
		} else {
			boolean isSet = collectionType.getCollectionKind().equals(CollectionKind.set);
			if (!isSet || !(parentEntity.reference() instanceof PersistentEntityReference)) {
				collectionElement = new TetherBarElement(newModelPath, tetherBarName, collectionDisplayName,
						getMasterDetailConstellationProvider(masterDetailConstellationProvider, newModelPath, useCase, forcePreferredUseCase, readOnly));
			} else {
				PropertyQuery propertyQuery = GMEUtil.getPropertyQuery((PersistentEntityReference) parentEntity.reference(), propertyName, null,
						getSpecialTraversingCriterion(collectionType.getCollectionElementType().getJavaType()), true, getMetaData(parentEntity), useCase);
				
				EntityType<?> queryEntityType = propertyQuery.entityType();
				List<GmContentViewContext> possibleContentViews = viewSituationResolverSupplier.get()
						.getPossibleContentViews(new EntryPointPathElement(queryEntityType, propertyQuery));
				
				Supplier<GmEntityView> entityViewProvider = null;
				if (!possibleContentViews.isEmpty()) {
					GmContentViewContext providerAndName = possibleContentViews.get(0);
					entityViewProvider = (Supplier<GmEntityView>) providerAndName.getContentViewProvider();
				}
				
				collectionElement = new TetherBarElement(newModelPath, tetherBarName, collectionDisplayName,
						getEntityViewProvider(entityViewProvider, propertyQuery, newModelPath, useCase));
			}
		}
		tetherBar.insertTetherBarElement(collectionIndex, collectionElement);
		if (select)
			tetherBar.setSelectedThetherBarElement(collectionElement);
		
		return collectionIndex;
	}
	
	private ModelPath prepareNewModelPath(Object modelObject, ModelPath modelPath) {
		if (modelPath == null)
			return null;
		
		ModelPath newModelPath = new ModelPath();
		for (ModelPathElement element : modelPath) {
			newModelPath.add(element.copy());
			if (element.getValue() == modelObject)
				break;
		}
		return newModelPath;
	}
	
	private int addCollectionElementToTether(ModelPath modelPath, ModelPathElement modelPathElement, boolean select, String useCase, boolean forcePreferredUseCase, int tetherIndex, boolean readOnly) {
		int returnIndex = tetherIndex;
		
		if (modelPathElement == null)
			modelPathElement = modelPath.last();
		
		PropertyRelatedModelPathElement collectionElementPath = (PropertyRelatedModelPathElement) modelPathElement;
		PropertyPathElement collectionPathElement = (PropertyPathElement) collectionElementPath.getPrevious();
		ModelPathElement entityElement = collectionPathElement.getPrevious();
		
		boolean isSimpleTypeCollection = !collectionElementPath.getType().isEntity();
		
		GenericEntity parentEntity = collectionElementPath.getEntity();
		EntityType<GenericEntity> parentEntityType = parentEntity.entityType();
		String propertyName = collectionElementPath.getProperty().getName();
		String collectionDisplayName = GMEMetadataUtil.getPropertyDisplay(propertyName, getMetaData(parentEntity).entity(parentEntity).property(propertyName).useCase(useCase));
		
		int collectionElementIndex = (tetherIndex < 0) ? tetherBar.getSelectedElementIndex() + 1: tetherIndex;
		if (!insertOnlyLastPathElementToTether) {
			TetherBarElement parentEntityElement = tetherBar.getTetherBarElementByModelPathElement(entityElement);
			int entityIndex = tetherBar.getIndexOfElement(parentEntityElement);
			if (parentEntityElement == null || entityIndex < tetherBar.getSelectedElementIndex() - 1) {
				ValueDescriptionBean valueDescriptionBean = getValueDescriptionBean(parentEntity, parentEntityType, useCase);
				ModelPath parentEntityModelPath = prepareNewModelPath(parentEntity, modelPath);
				TetherBarElement tetherElement = new TetherBarElement(parentEntityModelPath, valueDescriptionBean.getValue(),
						valueDescriptionBean.getDescription(),
						getMasterDetailConstellationProvider(masterDetailConstellationProvider, parentEntityModelPath, useCase, forcePreferredUseCase, readOnly));
				entityIndex = (tetherIndex < 0) ? tetherBar.getSelectedElementIndex() + 1 : tetherIndex;
				tetherBar.insertTetherBarElement(entityIndex, tetherElement);
				returnIndex = entityIndex;
			}
			
			int collectionIndex = entityIndex + 1;
			//PropertyPathElement collectionPathElement = (PropertyPathElement) modelPath.get(modelPath.size() - 2);
			Object collection = collectionPathElement.getValue();
			CollectionType collectionType = collectionPathElement.getType();
			TetherBarElement collectionElement = tetherBar.getElementAt(collectionIndex);
			if (collectionElement == null || !collectionElement.getModelPathElement().equals(collectionPathElement)) {
				ModelPath newModelPath = prepareNewModelPath(collection, modelPath);
				if (isSimpleTypeCollection) {
					collectionElement = new TetherBarElement(newModelPath, collectionDisplayName, collectionDisplayName,
							getMasterViewProvider(masterViewProvider, newModelPath, null));
				} else {
					boolean isSet = collectionType.getCollectionKind().equals(CollectionKind.set);
					if (!isSet) {
						collectionElement = new TetherBarElement(newModelPath, collectionDisplayName, collectionDisplayName,
								getMasterDetailConstellationProvider(masterDetailConstellationProvider, newModelPath, useCase, forcePreferredUseCase, readOnly));
					} else {
						PropertyQuery propertyQuery = GMEUtil.getPropertyQuery((PersistentEntityReference) parentEntity.reference(), propertyName, null,
								getSpecialTraversingCriterion(collectionType.getCollectionElementType().getJavaType()), true, getMetaData(parentEntity),
								useCase);
						
						EntityType<?> queryEntityType = propertyQuery.entityType();
						List<GmContentViewContext> possibleContentViews = viewSituationResolverSupplier.get()
								.getPossibleContentViews(new EntryPointPathElement(queryEntityType, propertyQuery));
						
						Supplier<GmEntityView> entityViewProvider = null;
						if (!possibleContentViews.isEmpty()) {
							GmContentViewContext providerAndName = possibleContentViews.get(0);
							entityViewProvider = (Supplier<GmEntityView>) providerAndName.getContentViewProvider();
						}
						
						collectionElement = new TetherBarElement(newModelPath, collectionDisplayName, collectionDisplayName,
								getEntityViewProvider(entityViewProvider, propertyQuery, newModelPath, useCase));
					}
				}
				tetherBar.insertTetherBarElement(collectionIndex, collectionElement);
				returnIndex = collectionIndex;
			}
			
			collectionElementIndex = collectionIndex + 1;
		}
		
		GenericEntity element = collectionElementPath.getValue();
		TetherBarElement collectionElementElement = tetherBar.getElementAt(collectionElementIndex);
		if (collectionElementElement == null || !collectionElementPath.equals(collectionElementElement.getModelPathElement())) {
			ModelPath newModelPath = prepareNewModelPath(element, modelPath);			
			
			//RVE - add additional (GmMetaModel and GmEntityType) Tether Element in case of GmProperty
			if (collectionElementPath.getValue() instanceof GmProperty || collectionElementPath.getValue() instanceof GmEnumConstant) {
				collectionElementIndex = addSpecialEntityElementsToTether(newModelPath, useCase, collectionElementIndex-1, collectionElementPath);
				//collectionElementIndex = collectionElementIndex + 1;
			}
			
			returnIndex = addElementToTether(modelPath, select, useCase, forcePreferredUseCase, element, collectionElementPath, collectionElementIndex, false, readOnly);
			//tetherBarEntitiesMap.put(collectionElementElement, collectionElementPath);
		} else {
			if (select)
				tetherBar.setSelectedThetherBarElement(collectionElementElement);
			returnIndex = tetherBar.getIndexOfElement(collectionElementElement);
		}
		
		return returnIndex;
	}
	
	private int addEntityPropertyToTether(ModelPath modelPath, ModelPathElement modelPathElement, boolean select, String useCase, boolean forcePreferredUseCase, int tetherIndex, boolean readOnly) {
		int returnIndex = tetherIndex;
		
		if (modelPathElement == null)
			modelPathElement = modelPath.last();
		
		PropertyRelatedModelPathElement propertyPathElement = (PropertyRelatedModelPathElement) modelPathElement;
		
		int entityIndex = (tetherIndex < 0) ? tetherBar.getSelectedElementIndex() + 1 : tetherIndex; 
		if (!insertOnlyLastPathElementToTether) {
			int parentEntityIndex = addEntityToPath(modelPath, modelPathElement, false, useCase, forcePreferredUseCase, entityIndex);
			entityIndex = parentEntityIndex + 1;
		}
		GenericEntity entity = propertyPathElement.getValue();
		EntityType<?> entityType = propertyPathElement.getType();
		TetherBarElement entityElement = tetherBar.getElementAt(entityIndex);
		
		if (entityElement == null || !entityElement.getModelPathElement().equals(propertyPathElement)) {
			String propertyName = propertyPathElement.getProperty().getName();
			GenericEntity parentEntity = propertyPathElement.getEntity();
			String entityPropertyDisplay = GMEMetadataUtil.getPropertyDisplay(propertyName,
					getMetaData(parentEntity).entity(parentEntity).property(propertyName).useCase(useCase));
			
			ValueDescriptionBean valueDescriptionBean = getValueDescriptionBean(entity, entityType, useCase);
			ModelPath newModelPath = prepareNewModelPath(entity, modelPath);
			
			String tetherBarName = entityPropertyDisplay + ": " + valueDescriptionBean.getValue();
			if (insertOnlyLastPathElementToTether) {
				ValueDescriptionBean parentDB = getValueDescriptionBean(parentEntity, propertyPathElement.getEntityType(), useCase);
				tetherBarName = LocalizedText.INSTANCE.entityProperty(parentDB.getValue(), entityPropertyDisplay);
			}
			
			entityElement = new TetherBarElement(newModelPath, tetherBarName, valueDescriptionBean.getDescription(),
					getMasterDetailConstellationProvider(masterDetailConstellationProvider, newModelPath, useCase, forcePreferredUseCase, readOnly));
			tetherBar.insertTetherBarElement(entityIndex, entityElement);
			returnIndex = entityIndex;
		}
		
		if (select)
			tetherBar.setSelectedThetherBarElement(entityElement);
		returnIndex = tetherBar.getIndexOfElement(entityElement);
		
		return returnIndex;
	}
	
	private boolean checkAddEntityToPath(ModelPath modelPath) {
		ModelPathElement modelPathElement;
		ModelPathElement last = modelPath.last();
		if (last instanceof PropertyRelatedModelPathElement)
			modelPathElement = last.getPrevious();
		else
			modelPathElement = last;
		
		TetherBarElement parentEntityElement = tetherBar.getTetherBarElementByModelPathElement(modelPathElement);
		int parentEntityIndex = tetherBar.getIndexOfElement(parentEntityElement);
		if (parentEntityElement == null || parentEntityIndex < tetherBar.getSelectedElementIndex())
			return true;
		
		return false;
	}
	
	private int addEntityToPath(ModelPath modelPath, ModelPathElement workingPathElement, boolean select, String useCase, boolean forcePreferredUseCase, int tetherIndex) {
		GenericEntity parentEntity = null;
		int parentEntityIndex = tetherIndex;
		ModelPathElement modelPathElement;
		ModelPathElement last = (workingPathElement == null) ? modelPath.last() : workingPathElement;
		if (last instanceof PropertyRelatedModelPathElement) {
			parentEntity = ((PropertyRelatedModelPathElement) last).getEntity();
			modelPathElement = last.getPrevious();
		} else {
			if (last.getValue() instanceof GenericEntity)
				parentEntity = last.getValue();
			modelPathElement = last;
		}

		parentEntityIndex = addSpecialEntityElementsToTether(modelPath, useCase, parentEntityIndex, modelPathElement);
		parentEntityIndex = addElementToTether(modelPath, select, useCase, forcePreferredUseCase, parentEntity, modelPathElement, parentEntityIndex, true, false);
		
		return parentEntityIndex;
	}

	private int addSpecialEntityElementsToTether(ModelPath modelPath, String useCase, int parentEntityIndex, ModelPathElement modelPathElement) {
		GenericEntity source = null;
		if (modelPathElement.getValue() instanceof GenericEntity)
			source = (GenericEntity) modelPathElement.getValue();
			
		//RVE - for GmEntityType, GmProperty and GmEnumType show in TetherBar also Declared Model, and Declared Entity
		GmMetaModel gmMetaModel = null;  
		GmEntityType gmEntityType = null;
		GmEnumType gmEnumType = null;
		if (source == null)
			return parentEntityIndex;
		
		if (source instanceof GmEntityType)
			gmMetaModel = ((GmEntityType) source).getDeclaringModel();
		else if (source instanceof GmProperty) {
			if (((GmProperty) source).getDeclaringType() != null)
				gmMetaModel = ((GmProperty) source).getDeclaringType().getDeclaringModel();
				gmEntityType = ((GmProperty) source).getDeclaringType();
		} else if (source instanceof GmEnumType)
			gmMetaModel = ((GmEnumType) source).getDeclaringModel();
		else if (source instanceof GmEnumConstant) {
			if (((GmEnumConstant) source).getDeclaringType() != null)
				gmMetaModel = ((GmEnumConstant) source).getDeclaringType().getDeclaringModel();
				gmEnumType = ((GmEnumConstant) source).getDeclaringType();
		}

		if (modelPath != null) {
			GmMetaModel pathGmMetaModel = null;
			for (ModelPathElement element : modelPath) {
				if (element.equals(modelPathElement))
					continue;
				
				if (element.getValue() instanceof GmMetaModel)
					pathGmMetaModel = element.getValue();
				else if (element.getValue() instanceof GmEntityType) {
					gmEntityType = element.getValue();
					gmMetaModel = gmEntityType.declaringModel();
				} else if (element.getValue() instanceof GmEnumType) {
					gmEnumType = element.getValue();
					gmMetaModel = gmEnumType.declaringModel();
				} 
			}
			
			if (pathGmMetaModel != null)
				gmMetaModel = pathGmMetaModel;
		}
		
		parentEntityIndex = addSpecialEntityElementToTether(useCase, parentEntityIndex, gmMetaModel);
		if (gmEntityType != null)
			parentEntityIndex = addSpecialEntityElementToTether(useCase, parentEntityIndex, gmEntityType);
		else if (gmEnumType != null)
			parentEntityIndex = addSpecialEntityElementToTether(useCase, parentEntityIndex, gmEnumType);
		
		return parentEntityIndex;
	}

	private int addSpecialEntityElementToTether(String useCase, int parentEntityIndex, GenericEntity value) {
		if (value == null)
			return parentEntityIndex;
		
		GenericModelType type = value.type();
		if (type == null)
		   type = GMF.getTypeReflection().getType(value.getClass());// TODO check if even reachable
		RootPathElement rootPathElement = new RootPathElement(type, value);
		ModelPath modelPath = new ModelPath();
		modelPath.add(rootPathElement);
		parentEntityIndex = addElementToTether(modelPath, false, useCase, false, value, rootPathElement, parentEntityIndex, false, false);
			
		return parentEntityIndex;
	}

	private int addElementToTether(ModelPath modelPath, boolean select,	String useCase, boolean forcePreferredUseCase, GenericEntity parentEntity,
			ModelPathElement modelPathElement, int insertAfterIndex, boolean canDuplicate, boolean readOnly) {
		TetherBarElement parentEntityElement = tetherBar.getTetherBarElementByModelPathElement(modelPathElement, !canDuplicate);
		int parentEntityIndex = tetherBar.getIndexOfElement(parentEntityElement);
		boolean exchangeView = false;
		if (parentEntityElement == null || (canDuplicate && parentEntityIndex < tetherBar.getSelectedElementIndex())) {
			ValueDescriptionBean valueDescriptionBean = null;
			ModelPath newModelPath = null;
			if (parentEntity == null) {
				valueDescriptionBean = new ValueDescriptionBean(GMEUtil.getTabName(modelPathElement, LocalizedText.INSTANCE.data()), null);				
				newModelPath = modelPath.copy();
			} else {
				EntityType<?> parentEntityType = parentEntity.entityType();
				valueDescriptionBean = getValueDescriptionBean(parentEntity, parentEntityType, useCase);
				newModelPath = prepareNewModelPath(parentEntity, modelPath);
			}
			parentEntityElement = new TetherBarElement(newModelPath, valueDescriptionBean.getValue(), valueDescriptionBean.getDescription(),
					getMasterDetailConstellationProvider(masterDetailConstellationProvider, newModelPath, useCase, forcePreferredUseCase, readOnly));
			if (insertAfterIndex < 0)
				parentEntityIndex = tetherBar.getSelectedElementIndex() + 1;				
			else
				parentEntityIndex = insertAfterIndex+1;
			tetherBar.insertTetherBarElement(parentEntityIndex, parentEntityElement);
		} else if (forcePreferredUseCase) {//We need to check if the forced useCase is already the one in place
			exchangeView = true;
			
			if (parentEntityIndex == tetherBar.getSelectedElementIndex())
				select = false;
		}
		
		if (select)
			tetherBar.setSelectedThetherBarElement(parentEntityElement);
		
		if (exchangeView)
			maybeExchangeContentView(parentEntityElement, useCase);
		
		return parentEntityIndex;
	}
	
	private void maybeExchangeContentView(TetherBarElement element, String useCase) {
		GmContentView view = element.getContentViewIfProvided();
		MasterDetailConstellation masterDetailConstellation = getParentBrowsingConstellation(view);
		
		if (masterDetailConstellation == null)
			return;
		
		ExchangeContentViewAction exchangeContentViewAction = masterDetailConstellation.getExchangeContentViewAction();
		if (exchangeContentViewAction == null)
			return;
		
		exchangeContentViewAction.adapt(useCase);
	}
	
	private MasterDetailConstellation getParentBrowsingConstellation(Object view) {
		if (view instanceof MasterDetailConstellation)
			return (MasterDetailConstellation) view;
		
		if (view instanceof Widget)
			return getParentBrowsingConstellation(((Widget) view).getParent());
		
		return null;
	}

	private Supplier<MasterDetailConstellation> getMasterDetailConstellationProvider(final Supplier<MasterDetailConstellation> originalProvider,
			final ModelPath modelPath, final String preferredUseCase, final boolean forcePreferredUseCase, final boolean readOnly) {
		return new Supplier<MasterDetailConstellation>() {
			@Override
			public MasterDetailConstellation get() throws RuntimeException {
				final MasterDetailConstellation masterDetailConstellation = originalProvider.get();
				/*masterDetailConstellation.addWorkWithEntityActionListener(getWorkWithEntityActionListener(browsingConstellation, masterDetailConstellation));
				masterDetailConstellation.addInstantiatedEntityListener(getInstantiatedEntityListener(masterDetailConstellation));*/
				ExchangeContentViewAction exchangeContentViewAction = masterDetailConstellation.getExchangeContentViewAction();
				if (exchangeContentViewAction != null) {
					exchangeContentViewAction.configureCurrentModelPath(modelPath);
					exchangeContentViewAction.configurePreferredUseCase(preferredUseCase, forcePreferredUseCase);
				}
				masterDetailConstellation.configureGmSession(gmSession);
				masterDetailConstellation.configureReadOnly(readOnly);
				
				masterDetailConstellation.setContent(modelPath);
				
				new Timer() {
					@Override
					public void run() {
						GmContentView currentMasterView = masterDetailConstellation.getCurrentMasterView();
						if (currentMasterView == null)
							return;
						
						int index = currentMasterView.getFirstSelectedIndex();
						if (index == -1)
							currentMasterView.select(0, false);
					}
				}.schedule(500);
				
				if (modelPath != null && !modelPath.isEmpty()) {
					Object value = modelPath.last().getValue();
					tetherBarEntitiesMap.values().stream().filter(e -> e.getValue() == value)
							.forEach(e -> listForCollectionElementRemoval(modelPath));
				}
				
				return masterDetailConstellation;
			}
		};
	}
	
	private void listForCollectionElementRemoval(ModelPath modelPath) {
		ModelPathElement lastElement = modelPath.last();
		if (lastElement.getValue() instanceof EnhancedEntity) {
			GmSession gmSession = ((EnhancedEntity) lastElement.getValue()).session();
			
			if (gmSession instanceof PersistenceGmSession) {
				PropertyRelatedModelPathElement propertyElement = (PropertyRelatedModelPathElement) lastElement;
				((PersistenceGmSession) gmSession).listeners().entity(propertyElement.getEntity()).property(propertyElement.getProperty())
						.add(getManipulationListener());
			}
		}
	}
	
	private ManipulationListener getDeleteManipulationListener() {
		if (deleteManipulationListener != null)
			return deleteManipulationListener;
		
		deleteManipulationListener = manipulation -> {
			if (!(manipulation instanceof DeleteManipulation))
				return;
			
			DeleteManipulation deleteManipulation = (DeleteManipulation) manipulation;
			GenericEntity deletedEntity = deleteManipulation.getEntity();
			
			RootPathElement rootElement = new RootPathElement(deletedEntity);
			TetherBarElement element = tetherBar.getTetherBarElementByModelPathElement(rootElement, true);
			if (element == null)
				return;
			
			tetherBar.removeTetherBarElementsFrom(tetherBar.getIndexOfElement(element));
		};
		
		return deleteManipulationListener;
	}
	
	private ManipulationListener getManipulationListener() {
		if (manipulationListener != null)
			return manipulationListener;
		
		manipulationListener = new ManipulationListener() {
			@Override
			public void noticeManipulation(final Manipulation manipulation) {
				if (!(manipulation instanceof RemoveManipulation) && !(manipulation instanceof ClearCollectionManipulation))
					return;
				
				final Owner owner = ((PropertyManipulation) manipulation).getOwner();
				if (!(owner instanceof LocalEntityProperty))
					return;
				
				//This must be the last listener to execute. We add a deferred call to guarantee that.
				Scheduler.get().scheduleDeferred(() -> {
					String propertyName = ((LocalEntityProperty) owner).getPropertyName();
					GenericEntity entity = ((LocalEntityProperty) owner).getEntity();
					tetherBarEntitiesMap.entrySet().stream().filter(entry -> entry.getValue().getEntity() == entity || entry.getValue().getProperty().getName().equals(propertyName)).forEach(entry -> {
						boolean updateModelPath = false;
						if (manipulation instanceof ClearCollectionManipulation)
							updateModelPath = true;
						else if (manipulation instanceof RemoveManipulation && ((RemoveManipulation) manipulation).getItemsToRemove() != null)
							updateModelPath = wasKeyRemoved(((RemoveManipulation) manipulation).getItemsToRemove().keySet(), entry.getValue());
						//TODO: check ChangeValueManipulation?
						if (updateModelPath) {
							if (entry.getValue().getType().isCollection())
								updateTetherBarElement(entry.getKey(), entry.getValue());
							else
								removeTetherBarElement(entry.getKey());
						}
					});
				});
			}
		};
		
		return manipulationListener;
	}
	
	private void updateTetherBarElement(TetherBarElement tetherBarElement, PropertyRelatedModelPathElement pathElement) {
		int index = tetherBar.getIndexOfElement(tetherBarElement);
		for (int i = index; i < tetherBar.getElementsSize(); i++) {
			TetherBarElement element = tetherBar.getElementAt(i);
			GmContentView contentView = element.getContentViewIfProvided();
			if (contentView != null) {
				ModelPath modelPath = contentView.getContentPath();
				if (modelPath != null)
					contentView.setContent(prepareNewModelPath(modelPath, pathElement));
			}
		}
	}
	
	private void removeTetherBarElement(TetherBarElement tetherBarElement) {
		tetherBar.removeTetherBarElements(Collections.singletonList(tetherBarElement));
		tetherBar.selectLastTetherBarElement();
	}
	
	private ModelPath prepareNewModelPath(ModelPath modelPath, PropertyRelatedModelPathElement pathElement) {
		int index = 0;
		Object value = pathElement.getValue();
		for (int i = 0; i < modelPath.size(); i++) {
			if (modelPath.get(i).getValue() == value) {
				index = i;
				break;
			}
		}
		
		ModelPath newModelPath = new ModelPath();
		newModelPath.add(new RootPathElement(pathElement.getType(), value));
		if (modelPath.size() > index + 1) {
			for (int i = index + 1; i < modelPath.size(); i++)
				newModelPath.add(modelPath.get(i).copy());
		}
		
		return newModelPath;
	}
	
	private boolean wasKeyRemoved(Collection<Object> removedKeys, PropertyRelatedModelPathElement pathElement) {
		for (Object removedKey : removedKeys) {
			if (pathElement instanceof ListItemPathElement) {
				if (((ListItemPathElement) pathElement).getIndex() == (Integer) removedKey)
					return true;
			} else if (pathElement instanceof SetItemPathElement) {
				if (((SetItemPathElement) pathElement).getValue() == removedKey)
					return true;
			} else if (pathElement instanceof MapKeyPathElement) {
				if (((MapKeyPathElement) pathElement).getValue() == removedKey)
					return true;
			} else if (pathElement instanceof MapValuePathElement) {
				if (((MapValuePathElement) pathElement).getKey() == removedKey)
					return true;
			}
		}
		
		return false;
	}
	
	private Supplier<? extends GmContentView> getMasterViewProvider(Supplier<? extends GmContentView> originalProvider, ModelPath modelPath, ModelPathElement collectionElementPath) {
		return new Supplier<GmContentView>() {
			@Override
			public GmContentView get() {
				GmContentView masterView = originalProvider.get();
				masterView.configureGmSession(gmSession);
				if (masterView instanceof GmActionSupport)
					((GmActionSupport) masterView).configureExternalActions(null);
				masterView.setContent(modelPath);
				
				new Timer() {
					@Override
					public void run() {
						if (collectionElementPath == null) {
							List<ModelPath> currentSelection = masterView.getCurrentSelection();
							if (currentSelection == null || currentSelection.isEmpty())
								masterView.select(0, false);
							return;
						}
						
						if (!(masterView instanceof AssemblyPanel))
							return;
						
						AssemblyPanel assemblyPanel = (AssemblyPanel) masterView;
						
						Object collectionElement = collectionElementPath.getValue();
						for (AbstractGenericTreeModel rootModel : assemblyPanel.getTreeGrid().getTreeStore().getRootItems()) {
							boolean found = false;
							if (rootModel instanceof MapKeyAndValueTreeModel) {
								if (((MapKeyAndValueTreeModel) rootModel).getMapKeyEntryTreeModel().refersTo(collectionElement)
										|| ((MapKeyAndValueTreeModel) rootModel).getMapValueEntryTreeModel().refersTo(collectionElement))
									found = true;
							} else if (rootModel.refersTo(collectionElement))
								found = true;
							
							if (found) {
								assemblyPanel.getTreeGrid().getSelectionModel().select(rootModel, false);
								assemblyPanel.getTreeGrid().setExpanded(rootModel, true);
								break;
							}
						}
					}
				}.schedule(200);
				
				return masterView;
			}
		};
	}
	
	private Supplier<GmEntityView> getEntityViewProvider(final Supplier<GmEntityView> originalProvider, final Query query, final ModelPath rootModelPath,
			final String useCase) {
		return new Supplier<GmEntityView>() {
			@Override
			public GmEntityView get() {
				GmEntityView entityView = originalProvider.get();
				entityView.configureUseCase(useCase);
				entityView.configureGmSession(gmSession);
				ModelPath modelPath = new ModelPath();
				modelPath.add(new RootPathElement(null, query));
				entityView.setContent(modelPath);
				if (entityView instanceof QueryConstellation) {
					((QueryConstellation) entityView).setRootModelPath(rootModelPath);
					((QueryConstellation) entityView).performSearch();
				}
				return entityView;
			}
		};
	}
	
	private ValueDescriptionBean getValueDescriptionBean(GenericEntity entity, EntityType<?> entityType, String useCase) {
		ModelMdResolver modelMdResolver;
		if (entity != null)
			modelMdResolver = getMetaData(entity);
		else
			modelMdResolver = gmSession.getModelAccessory().getMetaData();
		
		String value;
		String selectiveInformation = SelectiveInformationResolver.resolve(entityType, entity, modelMdResolver, useCase/*, null*/);
		GMEMetadataUtil.getEntityNameMDOrShortName(entityType, modelMdResolver, useCase);
		String displayInfo = GMEMetadataUtil.getEntityNameMDOrShortName(entityType, modelMdResolver, useCase);
		if (selectiveInformation != null && !selectiveInformation.trim().isEmpty())
			value = selectiveInformation;
		else
			value = displayInfo;
		
		return new ValueDescriptionBean(value, displayInfo);
	}
	
	private TraversingCriterion getSpecialTraversingCriterion(Class<?> clazz) {
		if (specialEntityTraversingCriterion != null)
			return specialEntityTraversingCriterion.get(clazz);
		else
			return null;
	}
	
	private GmTreeView getGmTreeView(GmContentView gmContentView) {
		if (gmContentView instanceof GmTreeView)
			return (GmTreeView) gmContentView;
		
		if (gmContentView instanceof QueryConstellation) {
			GmContentView view = ((QueryConstellation) gmContentView).getView();
			if (view != gmContentView)
				return getGmTreeView(view);
		}
		
		if (gmContentView instanceof MasterDetailConstellation) {
			GmContentView view = ((MasterDetailConstellation) gmContentView).getCurrentMasterView();
			if (view != gmContentView)
				return getGmTreeView(view);
		}
		
		return null;
	}

	private MasterDetailConstellation getChildMasterDetailConstellation(GmContentView gmContentView) {
		if (gmContentView == null)
			return null;
		
		if (gmContentView instanceof QueryConstellation) {
			GmContentView view = ((QueryConstellation) gmContentView).getView();
			if (view != gmContentView)
				return getChildMasterDetailConstellation(view);
		}
		
		if (gmContentView instanceof MasterDetailConstellation)
			return (MasterDetailConstellation) gmContentView;
		
		return null;
	}
	
	private GmContentView getLatestChildGmContentView(GmContentView gmContentView) {
		if (gmContentView instanceof QueryConstellation) {
			GmContentView view = ((QueryConstellation) gmContentView).getView();
			if (view != gmContentView)
				return getLatestChildGmContentView(view);
		}
		
		if (gmContentView instanceof MasterDetailConstellation) {
			GmContentView view = ((MasterDetailConstellation) gmContentView).getCurrentMasterView();
			if (view != gmContentView)
				return getLatestChildGmContentView(view);
		}
				
		return gmContentView;		
	}
	
	private void handleEntityNavigation(ModelPath modelPath, String useCase) {
		if (modelPath == null)
			return;
		
		ModelPathElement last = modelPath.last();
		Object value = last.getValue();
		if (!(value instanceof GenericEntity))
			return;
		
		GenericEntity entity = (GenericEntity) value;
		DefaultNavigation entityNavigation = getMetaData(entity).entity(entity).useCase(useCase).meta(DefaultNavigation.T).exclusive();
		if (entityNavigation != null) {
			EntityType<GenericEntity> entityType = last.getType();
			String propertyName = entityNavigation.getProperty().getName();
			Property property = entityType.getProperty(propertyName);
			Object propertyValue = property.get(entity);
			PropertyPathElement propertyPathElement = new PropertyPathElement(entity, property, propertyValue);
			modelPath.add(propertyPathElement);
		}
	}

	@Override
	public void addGmContentViewListener(GmContentViewListener listener) {
		if (currentContentView instanceof GmContentSupport)
			((GmContentSupport) currentContentView).addGmContentViewListener(listener);
	}

	@Override
	public void removeGmContentViewListener(GmContentViewListener listener) {
		if (currentContentView instanceof GmContentSupport)
			((GmContentSupport) currentContentView).removeGmContentViewListener(listener);
	}

	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		if (currentContentView != null)
			currentContentView.addSelectionListener(sl);
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		if (currentContentView != null)
		currentContentView.removeSelectionListener(sl);
	}

	@Override
	public ModelPath getFirstSelectedItem() {
		if (currentContentView == null)
			return null;
		
		return currentContentView.getFirstSelectedItem();
	}
	
	@Override
	public GmContentView getView() {
		return currentContentView;
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		if (currentContentView == null)
			return null;
		
		return currentContentView.getCurrentSelection();
	}
	
	@Override
	public List<List<ModelPath>> getAmbiguousSelection() {
		if (currentContentView instanceof GmAmbiguousSelectionSupport)
			return ((GmAmbiguousSelectionSupport) currentContentView).getAmbiguousSelection();
		
		return transformSelection(getCurrentSelection());
	}

	@Override
	public boolean isSelected(Object element) {
		if (currentContentView == null)
			return false;
		
		return currentContentView.isSelected(element);
	}

	@Override
	public boolean selectVertical(Boolean next, boolean keepExisting) {
		return currentContentView != null ? currentContentView.selectVertical(next, keepExisting) : false;
	}
	
	@Override
	public boolean selectHorizontal(Boolean next, boolean keepExisting) {
		return currentContentView != null ? currentContentView.selectHorizontal(next, keepExisting) : false;
	}	
	
	@Override
	public void select(int index, boolean keepExisting) {
		if (currentContentView != null)
			currentContentView.select(index, keepExisting);
	}
	
	@Override
	public int getFirstSelectedIndex() {
		if (currentContentView == null)
			return -1;
		
		return currentContentView.getFirstSelectedIndex();
	}

	@Override
	public void addInteractionListener(GmInteractionListener il) {
		if (currentContentView instanceof GmInteractionSupport)
			((GmInteractionSupport) currentContentView).addInteractionListener(il);
	}

	@Override
	public void removeInteractionListener(GmInteractionListener il) {
		if (currentContentView instanceof GmInteractionSupport)
			((GmInteractionSupport) currentContentView).removeInteractionListener(il);
	}

	@Override
	public void addCheckListener(GmCheckListener cl) {
		if (currentContentView instanceof GmCheckSupport)
			((GmCheckSupport) currentContentView).addCheckListener(cl);
	}

	@Override
	public void removeCheckListener(GmCheckListener cl) {
		if (currentContentView instanceof GmCheckSupport)
			((GmCheckSupport) currentContentView).removeCheckListener(cl);
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

	@Override
	public ModelPath getContentPath() {
		if (currentContentView == null)
			return null;
		
		return currentContentView.getContentPath();
	}
	
	@Override
	public void configureExternalActions(List<Pair<ActionTypeAndName, ModelAction>> actions) {
		if (currentContentView instanceof GmActionSupport)
			((GmActionSupport) currentContentView).configureExternalActions(actions);
	}
	
	@Override
	public List<Pair<ActionTypeAndName, ModelAction>> getExternalActions() {
		if (currentContentView instanceof GmActionSupport)
			return ((GmActionSupport) currentContentView).getExternalActions();
		
		return null;
	}
	
	@Override
	public void configureActionGroup(ActionGroup actionGroup) {
		if (currentContentView instanceof GmActionSupport)
			((GmActionSupport) currentContentView).configureActionGroup(actionGroup);
	}
	
	@Override
	public void setActionManager(GmContentViewActionManager actionManager) {
		if (currentContentView instanceof GmActionSupport)
			((GmActionSupport) currentContentView).setActionManager(actionManager);
	}

	@Override
	public void setContent(ModelPath modelPath) {
		if (currentContentView != null)
			currentContentView.setContent(modelPath);
		if (verticalTabActionBar != null)
			verticalTabActionBar.refresh();
	}
	
	@Override
	public void addContent(ModelPath modelPath) {
		if (currentContentView instanceof GmListView)
			((GmListView) currentContentView).addContent(modelPath);
	}
	
	@Override
	public List<ModelPath> getAddedModelPaths() {
		if (currentContentView instanceof GmListView)
			return ((GmListView) currentContentView).getAddedModelPaths();
		
		return null;
	}
	
	@Override
	public void configureTypeForCheck(GenericModelType typeForCheck) {
		if (currentContentView instanceof GmListView)
			((GmListView) currentContentView).configureTypeForCheck(typeForCheck);
	}

	@Override
	public void onExchangeMasterView(GmContentView parentContentView, GmContentView newContentView) {
		if (verticalTabActionBar == null)
			return;

		//AAA
		
		updateVerticalTabActionBar(newContentView);
		fireOnExchangeMasterView(parentContentView, newContentView);
	}
	
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
	
	@Override
	public boolean isViewReady() {
		if (currentContentView == null)
			return false;
		
		return currentContentView.isViewReady();
	}
	
	@Override
	public void disposeBean() throws Exception {
		if (deleteManipulationListener != null && gmSession != null)
			gmSession.listeners().remove(deleteManipulationListener);
		
		tetherBarEntitiesMap.clear();
		this.tetherBar.disposeBean();
	}

	public WorkWithEntityExpert getWorkWithEntityExpert() {
		return workWithEntityExpert;
	}

	private void fireOnExchangeMasterView(GmContentView parentContentView, GmContentView newContentView) {
		for (GmExchangeMasterViewListener listener : exchangeListeners) {
			listener.onExchangeMasterView(parentContentView, newContentView);
		}
	}
	
}
