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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.action.client.TriggerKnownProperties;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabActionMenu;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabElement;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabPanel.VerticalTabListener;
import com.braintribe.gwt.gmview.action.client.ActionGroup;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.actionbar.client.ActionProviderConfiguration;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionProvider;
import com.braintribe.gwt.gmview.client.ClipboardListener;
import com.braintribe.gwt.gmview.client.ClipboardSupport;
import com.braintribe.gwt.gmview.client.ClipboardViewHandler;
import com.braintribe.gwt.gmview.client.GmActionSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewActionManager;
import com.braintribe.gwt.gmview.client.GmListView;
import com.braintribe.gwt.gmview.client.GmSelectionCount;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.GmSelectionSupport;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedBorderLayoutContainer;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.MapValuePathElement;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.resources.CommonStyles;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

/**
 * Constellation that will handle items copied to the ClipBoard.
 * The user will have the possibility to clear the items, or remove one by one.
 * @author michel.docouto
 *
 */
public class ClipboardConstellation extends BorderLayoutContainer implements GmListView, ClipboardViewHandler, ClipboardSupport, GmActionSupport, GmViewActionProvider, GmSelectionSupport {
	private Supplier<MasterDetailConstellation> masterDetailConstellationProvider;
	private MasterDetailConstellation masterDetailConstellation;
	private GmListView listView;
	private Set<ClipboardListener> clipboardListeners;
	private Set<Object> itemsInClipboard = new LinkedHashSet<>();
	private HTML emptyPanel;
	//private Widget currentWidget;
	private boolean needsReset = false;
	private ActionProviderConfiguration actionProviderConfiguration;
	private boolean elementAlreadyAdded = false;
	private PersistenceGmSession gmSession;
	private int maxSelection;
	private List<MasterDetailConstellationProvidedListener> masterDetailConstellationProvidedListeners;
	private VerticalTabActionMenu verticalTabActionBar;
	private boolean verticalTabActionBarVisible = false;
	private BorderLayoutContainer topPanel;
	private int northDataSize = 32;
	private BorderLayoutData northData = new BorderLayoutData(0);
	private List<Action> listAction;
	private GmContentViewActionManager actionManager;
	private List<Pair<ActionTypeAndName, ModelAction>> externalActions = null;
	private Boolean saveCollapsed;

	public ClipboardConstellation() {
		//setBodyBorder(false);
		setBorders(false);
		//setHeaderVisible(false);
		exchangeWidget(getEmptyPanel());
		topPanel = new ExtendedBorderLayoutContainer();
		topPanel.setStyleName("clipboardConstellationTopPanel");
		setNorthWidget(topPanel, northData);
	}
	
	/**
	 * Configures the required {@link MasterDetailConstellation} which will hold the items copied to the ClipBoard.
	 */
	@Required
	public void setMasterDetailConstellationProvider(Supplier<MasterDetailConstellation> masterDetailConstellationProvider) {
		this.masterDetailConstellationProvider = masterDetailConstellationProvider;
	}
	
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		if (this.gmSession == gmSession)
			return;
		
		this.gmSession = gmSession;
		if (masterDetailConstellation != null)
			configureSessionInMaster();
	}
	
	public void configureMaxSelection(int maxSelection) {
		this.maxSelection = maxSelection;
		if (masterDetailConstellation != null && masterDetailConstellation.getCurrentMasterView() instanceof GmSelectionCount)
			((GmSelectionCount) masterDetailConstellation.getCurrentMasterView()).setMaxSelectCount(maxSelection);
	}
	
	public void prepareLayout() {
		getMasterDetailConstellation();    //initialize masterDetailConstellation
		exchangeWidget(getEmptyPanel());
		prepareVerticalTabActionBar(topPanel);
	}
	
	public void addModelsToClipboard(List<ModelPath> models) {
		List<Object> listContent = new ArrayList<>();
		
		if (models != null) {
			for (ModelPath model : models) {
				ModelPathElement last = model.last();
				if (last instanceof MapValuePathElement) {
					Object key = ((MapValuePathElement) last).getKey();
					if (key instanceof GenericEntity)
						itemsInClipboard.add(key);
				}
				Object value = last.getValue();
				if (value instanceof GenericEntity)
					itemsInClipboard.add(value);
			}
		}
		
		listContent.addAll(itemsInClipboard);
		
		if (masterDetailConstellation == null)
			getMasterDetailConstellation();
		
		if (!listContent.isEmpty()) {
			if (!elementAlreadyAdded && needsReset)
				masterDetailConstellation.resetActions();
			
			ModelPath modelPath = new ModelPath();
			modelPath.add(new RootPathElement(CollectionType.TYPE_LIST, listContent));
			masterDetailConstellation.setContent(null);
			masterDetailConstellation.addContent(modelPath);
		}

		masterDetailConstellation.select(0, false);
		
		if (listContent.isEmpty()) {
			exchangeWidget(getEmptyPanel());
		} else {
			exchangeWidget(masterDetailConstellation);
		}
		
		elementAlreadyAdded = true;
		
		fireItemsAdded(models);
		
		if (verticalTabActionBar != null) {
			Scheduler.get().scheduleDeferred(() -> {
				//verticalTabActionBar.configureGmConentView(masterDetailConstellation);
				//verticalTabActionBar.configureGmConentView(masterDetailConstellation.getCurrentMasterView());
				verticalTabActionBar.configureGmConentView(this);
				verticalTabActionBar.updateElements();
			});
		}

	}
	
	/**
	 * Clears all items in the constellation.
	 */
	public void cleanup() {
		if (itemsInClipboard.isEmpty()) {
			exchangeWidget(getEmptyPanel());			
			if (verticalTabActionBar != null) {
				Scheduler.get().scheduleDeferred(() -> {
					verticalTabActionBar.configureGmConentView(this);
					verticalTabActionBar.updateElements();
					verticalTabActionBar.refresh();	
				});
			}			
			return;
		}
		
		itemsInClipboard.clear();
		
		cleanupMasterDetailConstellation();
		
		exchangeWidget(getEmptyPanel());
		if (verticalTabActionBar != null) {
			Scheduler.get().scheduleDeferred(() -> {
				verticalTabActionBar.configureGmConentView(this);
				verticalTabActionBar.updateElements();
				verticalTabActionBar.refresh();		
			});
		}
	}

	private void cleanupMasterDetailConstellation() {
		if (masterDetailConstellation != null) {
			List<Object> listContent = new ArrayList<>();
			ModelPath modelPath = new ModelPath();
			modelPath.add(new RootPathElement(CollectionType.TYPE_LIST, listContent));
			masterDetailConstellation.setContent(null);
			masterDetailConstellation.addContent(modelPath);
			masterDetailConstellation.select(0, false);
		}
	}
	
	public void addMasterDetailConstellationProvidedListener(MasterDetailConstellationProvidedListener listener) {
		if (listener != null) {
			if (masterDetailConstellationProvidedListeners == null)
				masterDetailConstellationProvidedListeners = new ArrayList<>();
			masterDetailConstellationProvidedListeners.add(listener);
		}
	}
	
	public void removeMasterDetailConstellationProvidedListener(MasterDetailConstellationProvidedListener listener) {
		if (masterDetailConstellationProvidedListeners != null) {
			masterDetailConstellationProvidedListeners.remove(listener);
			if (masterDetailConstellationProvidedListeners.isEmpty())
				masterDetailConstellationProvidedListeners = null;
		}
	}
	
	@Override
	public void addClipboardListener(ClipboardListener listener) {
		if (listener != null) {
			if (clipboardListeners == null)
				clipboardListeners = new LinkedHashSet<>();
			clipboardListeners.add(listener);
		}
	}
	
	@Override
	public void removeClipboardListener(ClipboardListener listener) {
		if (clipboardListeners != null) {
			clipboardListeners.remove(listener);
			if (clipboardListeners.isEmpty())
				clipboardListeners = null;
		}
	}
	
	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		if (masterDetailConstellation != null)
			masterDetailConstellation.addSelectionListener(sl);
	}
	
	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		if (masterDetailConstellation != null)
			masterDetailConstellation.removeSelectionListener(sl);
	}
	
	@Override
	public List<ModelPath> getCurrentSelection() {
		return masterDetailConstellation != null ? masterDetailConstellation.getCurrentSelection() : null;
	}
	
	@Override
	public ModelPath getFirstSelectedItem() {
		return masterDetailConstellation != null ? masterDetailConstellation.getFirstSelectedItem() : null;
	}
	
	@Override
	public GmContentView getView() {
		return masterDetailConstellation != null ? masterDetailConstellation.getView() : null;
	}
	
	@Override
	public boolean isSelected(Object element) {
		return masterDetailConstellation != null ? masterDetailConstellation.isSelected(element) : false;
	}
	
	@Override
	public void select(int index, boolean keepExisting) {
		if (masterDetailConstellation != null)
			masterDetailConstellation.select(index, keepExisting);
	}
	
	@Override
	public void setActionManager(GmContentViewActionManager actionManager) {
		this.actionManager = actionManager;
	}
	
	@Override
	public ActionProviderConfiguration getActions() {
		if (actionProviderConfiguration != null)
			return actionProviderConfiguration;
		
		ActionProviderConfiguration mdcActionProviderConfig = masterDetailConstellation != null ? masterDetailConstellation.getActions() : null;
		if (mdcActionProviderConfig != null)
			actionProviderConfiguration = mdcActionProviderConfig;
		if (actionProviderConfiguration == null)
			actionProviderConfiguration = new ActionProviderConfiguration();

		if (externalActions == null) {
			externalActions = new ArrayList<>();
			externalActions.add(new Pair<>(new ActionTypeAndName("removeClipboard"), prepareRemoveAction()));
			externalActions.add(new Pair<>(new ActionTypeAndName("clearClipboard"), prepareClearAction()));
		}
		actionProviderConfiguration.addExternalActions(externalActions);
		
		return actionProviderConfiguration;
	}
	
	@Override
	public boolean isFilterExternalActions() {
		return masterDetailConstellation != null ? masterDetailConstellation.isFilterExternalActions() : false;
	}
	
	@Override
	public GmContentViewActionManager getGmContentViewActionManager() {
		return actionManager;
	}
	
	@Required
	public void setVerticalTabActionBar(VerticalTabActionMenu verticalTabActionBar) {
		this.verticalTabActionBar = verticalTabActionBar;
	}
	
	public void setVisibleVerticalTabActionBar(boolean visible) {
		this.verticalTabActionBarVisible = visible;
		if (verticalTabActionBar != null) {
			verticalTabActionBar.setShowDynamicTabElements(verticalTabActionBarVisible);
			verticalTabActionBar.setUseContentMenuAction(verticalTabActionBarVisible);
		}
		if (visible)
			northData.setSize(northDataSize);
		else
			northData.setSize(0);			
	}	
	
	public void setConstellationDefaultModelActions(List<Action> list) {
		this.listAction = list;
	}	
	
	private HTML getEmptyPanel() {
		if (emptyPanel == null) {
			StringBuilder html = new StringBuilder();
			html.append("<div style='height: 100%; width: 100%; display: table;' class='emptyStyle'>");
			html.append("<div style='display: table-cell; vertical-align: middle'>").append(LocalizedText.INSTANCE.noClipboardItems())
					.append("</div></div>");
			emptyPanel = new HTML(html.toString());
		}
		
		return emptyPanel;
	}
	
	private void exchangeWidget(Widget widget) {
		if (masterDetailConstellation == null) {
			setCenterWidget(widget);
			doLayout();
			return;
		}

		if (getCenterWidget() != masterDetailConstellation) {
			setCenterWidget(masterDetailConstellation);
			doLayout();
		}
		if (masterDetailConstellation.equals(widget)) {
			if (saveCollapsed != null)
				masterDetailConstellation.setShowDetailViewCollapsed(saveCollapsed);
			masterDetailConstellation.exchangeWidget(null);			
		} else {
			saveCollapsed = masterDetailConstellation.isShowDetailViewCollapsed();
			masterDetailConstellation.setShowDetailViewCollapsed(true);
			masterDetailConstellation.exchangeWidget(widget);
		}	
	}
	
	/*
	private void exchangeWidget(Widget widget) {
		if (currentWidget == widget)
			return;
		
		boolean doLayout = false;
		if (currentWidget != null) {
			remove(currentWidget);
			doLayout = true;
		}
		currentWidget = widget;
		setCenterWidget(widget);
		if (doLayout)
			doLayout();
	}
	*/
	
	public void updateVerticalTabActionBar(GmContentView contentView) {
		GmContentView viewToUse = contentView;
		if (viewToUse == null) {
			//viewToUse = masterDetailConstellation;
			//viewToUse = masterDetailConstellation.getCurrentMasterView();
			viewToUse = this;
		}
		
		verticalTabActionBar.configureGmConentView(viewToUse);
		doLayout();
	}	
	
	private ModelAction prepareRemoveAction() {
		final ModelAction removeAction = new ModelAction() {
			@Override
			public void perform(TriggerInfo triggerInfo) {
				if (listView != null) {
					List<ModelPath> pathsToRemove = listView.getCurrentSelection();
					pathsToRemove.forEach(model -> itemsInClipboard.remove(model.last().getValue()));
							
					if (itemsInClipboard.isEmpty()) {
						cleanupMasterDetailConstellation();
					}
					
					fireItemsRemoved(pathsToRemove);					
				}
					
				addModelsToClipboard(null);
			}
			
			@Override
			protected void updateVisibility() {
				boolean useHidden = true;
				if (listView != null) {
					List<ModelPath> pathsToRemove = listView.getCurrentSelection();
					useHidden = pathsToRemove == null ? true : pathsToRemove.isEmpty();
				}
					
				setHidden(useHidden);	
			}
		};
		
		if (listView != null)
			listView.addSelectionListener(gmSelectionSupport -> {				
				removeAction.setHidden(gmSelectionSupport.getFirstSelectedItem() == null);
				verticalTabActionBar.configureGmConentView(this);
				verticalTabActionBar.updateElements();
				verticalTabActionBar.refresh();			
			});
		
		removeAction.setName(LocalizedText.INSTANCE.removeFromClipboard());
		removeAction.setIcon(ConstellationResources.INSTANCE.remove());
		removeAction.setHoverIcon(ConstellationResources.INSTANCE.removeBig());
		removeAction.put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
		removeAction.setHidden(true);
		
		return removeAction;
	}
	
	private ModelAction prepareClearAction() {
		final ModelAction clearAction = new ModelAction() {
			@Override
			public void perform(TriggerInfo triggerInfo) {
				cleanup();
				fireClipboardCleared();
			}
			
			@Override
			protected void updateVisibility() {
				boolean isHidden = itemsInClipboard.isEmpty();
				setHidden(isHidden);
			}
		};
		clearAction.setName(LocalizedText.INSTANCE.clearClipboard());
		clearAction.setIcon(ConstellationResources.INSTANCE.clear());
		clearAction.setHoverIcon(ConstellationResources.INSTANCE.clearBig());
		clearAction.put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
		return clearAction;
	}
	
	private void fireClipboardCleared() {
		if (clipboardListeners != null)
			clipboardListeners.forEach(listener -> listener.onModelsInClipoboardCleared());
	}
	
	private void fireItemsRemoved(List<ModelPath> itemsRemoved) {
		if (clipboardListeners != null)
			clipboardListeners.forEach(listener -> listener.onModelsRemovedFromClipboard(itemsRemoved));
	}
	
	private void fireItemsAdded(List<ModelPath> itemsAdded) {
		if (clipboardListeners != null) {
			clipboardListeners.stream().filter(l -> !(l instanceof ClipboardConstellationProvider))
					.forEach(l -> l.onModelsAddedToClipboard(itemsAdded));
		}
	}
	
	private MasterDetailConstellation getMasterDetailConstellation() {
		if (masterDetailConstellation != null)
			return masterDetailConstellation;
		
		masterDetailConstellation = masterDetailConstellationProvider.get();
		if (gmSession != null)
			configureSessionInMaster();
		if (masterDetailConstellation.getCurrentMasterView() instanceof GmSelectionCount)
			((GmSelectionCount) masterDetailConstellation.getCurrentMasterView()).setMaxSelectCount(maxSelection);
		if (actionManager != null)
			actionManager.connect(masterDetailConstellation);		

		fireMasterDetailConstellationProvided();
		
		if (verticalTabActionBar != null)
			verticalTabActionBar.configureGmConentView(this);
			//verticalTabActionBar.configureGmConentView(masterDetailConstellation);
		
		return masterDetailConstellation;
	}
	
	private void configureSessionInMaster() {
		masterDetailConstellation.configureGmSession(gmSession);
		listView = ((GmListView) masterDetailConstellation.getCurrentMasterView());
		if (listView != null)
			listView.configureGmSession(gmSession);
		List<Pair<ActionTypeAndName, ModelAction>> actions = new ArrayList<>();
		actions.add(new Pair<>(new ActionTypeAndName("removeClipboard"), prepareRemoveAction()));
		actions.add(new Pair<>(new ActionTypeAndName("clearClipboard"), prepareClearAction()));
		
		if (needsReset)
			masterDetailConstellation.resetActions();
		masterDetailConstellation.configureExternalActions(actions);
		
		needsReset = true;
	}
	
	private void fireMasterDetailConstellationProvided() {
		if (masterDetailConstellationProvidedListeners != null)
			masterDetailConstellationProvidedListeners.forEach(listener -> listener.onMasterDetailConstellationProvided(masterDetailConstellation));
	}
	
	private void prepareVerticalTabActionBar(BorderLayoutContainer topPanel) {
		if (verticalTabActionBar != null) {
			verticalTabActionBar.setShowDynamicTabElements(verticalTabActionBarVisible);
			verticalTabActionBar.setUseContentMenuAction(verticalTabActionBarVisible);
			verticalTabActionBar.setStaticActionGroup(listAction);
			
			verticalTabActionBar.addStyleName("browsingConstellationActionBar");
			verticalTabActionBar.addStyleName("clipboardConstellationActionBar");
			if (verticalTabActionBarVisible)			
				northData.setSize(northDataSize);
			else
				northData.setSize(0);
				
			topPanel.setCenterWidget(verticalTabActionBar);
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

	@Override
	public void configureActionGroup(ActionGroup actionGroup) {
		//NOP		
	}

	@Override
	public void configureExternalActions(List<Pair<ActionTypeAndName, ModelAction>> externalActions) {
		// NOP		
	}

	@Override
	public List<Pair<ActionTypeAndName, ModelAction>> getExternalActions() {
		return externalActions;
	}

	//GmListView
	
	@Override
	public ModelPath getContentPath() {
		if (masterDetailConstellation != null)
			return masterDetailConstellation.getContentPath();
		return null;
	}

	@Override
	public void setContent(ModelPath modelPath) {
		if (masterDetailConstellation != null)
			masterDetailConstellation.setContent(modelPath);
	}

	@Override
	public PersistenceGmSession getGmSession() {
		if (masterDetailConstellation != null)
			return masterDetailConstellation.getGmSession();
		return null;
	}

	@Override
	public void configureUseCase(String useCase) {
		if (masterDetailConstellation != null)
			masterDetailConstellation.configureUseCase(useCase);
	}

	@Override
	public String getUseCase() {
		if (masterDetailConstellation != null)
			return masterDetailConstellation.getUseCase();
		return null;
	}

	@Override
	public void configureTypeForCheck(GenericModelType typeForCheck) {
		if (masterDetailConstellation != null)
			masterDetailConstellation.configureTypeForCheck(typeForCheck);
	}

	@Override
	public void addContent(ModelPath modelPath) {
		if (masterDetailConstellation != null)
			masterDetailConstellation.addContent(modelPath);
	}

	@Override
	public List<ModelPath> getAddedModelPaths() {
		if (masterDetailConstellation != null)
			return masterDetailConstellation.getAddedModelPaths();
		return null;
	}

}
