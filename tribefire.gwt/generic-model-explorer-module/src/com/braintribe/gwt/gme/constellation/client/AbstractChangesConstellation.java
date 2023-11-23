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
import java.util.Collections;
import java.util.HashSet;
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
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.processing.session.api.persistence.CommitListener;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.Transaction;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.resources.CommonStyles;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

public abstract class AbstractChangesConstellation extends BorderLayoutContainer implements GmListView, GmActionSupport, ManipulationListener, CommitListener, GmViewActionProvider, GmSelectionSupport {
	
	private Supplier<MasterDetailConstellation> masterDetailConstellationProvider;
	private MasterDetailConstellation masterDetailConstellation;
	private Set<GenericEntity> entitiesSet = new LinkedHashSet<>();
	private Set<GenericEntity> transientEntitiesSet = new LinkedHashSet<>();
	private Timer manipulationListenerTimer;
	private Timer transientManipulationListenerTimer;
	private HTML emptyPanel;
	private Widget currentWidget;
	private Set<Class<?>> ignoreTypes;
	private PersistenceGmSession gmSession;
	private TransientGmSession transientSession;
	private boolean needsReset = false;
	private ActionProviderConfiguration actionProviderConfiguration;
	private int maxSelection;
	private List<MasterDetailConstellationProvidedListener> masterDetailConstellationProvidedListeners;
	private List<ChangesConstellationListener> changesConstellationListeners;
	private boolean changesAdded;
	private Set<GenericEntity> removedEntries = new HashSet<>();
	private Set<GenericEntity> transientRemovedEntries = new HashSet<>();
	private VerticalTabActionMenu verticalTabActionBar;
	private boolean verticalTabActionBarVisible = false;
	private BorderLayoutContainer topPanel;
	private int northDataSize = 32;
	private BorderLayoutData northData = new BorderLayoutData(0);
	private List<Action> listAction;
	private GmContentViewActionManager actionManager;
	private List<Pair<ActionTypeAndName, ModelAction>> externalActions = null;
	
	public AbstractChangesConstellation() {
		//setBodyBorder(false);
		setBorders(false);
		//setHeaderVisible(false);
		exchangeWidget(getEmptyPanel());
		topPanel = new ExtendedBorderLayoutContainer();
		topPanel.setStyleName("changesConstellationTopPanel");
		setNorthWidget(topPanel, northData);
	}
	
	/**
	 * Configures the required {@link MasterDetailConstellation} which will hold the manipulated entities.
	 */
	@Required
	public void setMasterDetailConstellationProvider(Supplier<MasterDetailConstellation> masterDetailConstellationProvider) {
		this.masterDetailConstellationProvider = masterDetailConstellationProvider;
	}
	
	/**
	 * Configures types to be ignored (not added) in the {@link AbstractChangesConstellation}.
	 */
	@Configurable
	public void setIgnoreTypes(Set<Class<?>> ignoreTypes) {
		this.ignoreTypes = ignoreTypes;
	}
	
	public void configureSessions(PersistenceGmSession gmSession, TransientGmSession transientGmSession) {
		boolean configureSessionInMaster = false;
		if (this.gmSession != gmSession) {
			if (this.gmSession != null) {
				this.gmSession.listeners().remove((ManipulationListener) this);
				this.gmSession.listeners().remove((CommitListener) this);
			}
			this.gmSession = gmSession;
			if (gmSession != null) {
				gmSession.listeners().add((ManipulationListener) this);
				gmSession.listeners().add((CommitListener) this);
			}
			
			configureSessionInMaster = true;
		}
		
		if (transientSession != transientGmSession) {
			if (transientSession != null) {
				transientSession.listeners().remove((ManipulationListener) this);
				transientSession.listeners().remove((CommitListener) this);
			}
			transientSession = transientGmSession;
			if (transientGmSession != null) {
				transientGmSession.listeners().add((ManipulationListener) this);
				transientGmSession.listeners().add((CommitListener) this);
			}
		}
		
		if (configureSessionInMaster && masterDetailConstellation != null)
			configureSessionInMaster();
		
		loadData();
	}
	
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		configureSessions(gmSession, transientSession);
	}
	
	public void configureMaxSelection(int maxSelection) {
		this.maxSelection = maxSelection;
		if (masterDetailConstellation != null && masterDetailConstellation.getCurrentMasterView() instanceof GmSelectionCount)
			((GmSelectionCount) masterDetailConstellation.getCurrentMasterView()).setMaxSelectCount(maxSelection);
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
	
	public void addChangesConstellationListener(ChangesConstellationListener listener) {
		if (listener != null) {
			if (changesConstellationListeners == null)
				changesConstellationListeners = new ArrayList<>();
			changesConstellationListeners.add(listener);
		}
	}
	
	public void removeChangesConstellationListener(ChangesConstellationListener listener) {
		if (changesConstellationListeners != null) {
			changesConstellationListeners.remove(listener);
			if (changesConstellationListeners.isEmpty())
				changesConstellationListeners = null;
		}
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		loadData();
	}
	
	@Override
	public void onBeforeCommit(PersistenceGmSession session, Manipulation manipulation) {
		//NOP
	}
	
	@Override
	public void onAfterCommit(PersistenceGmSession session, Manipulation manipulation, Manipulation inducedManipluation) {
		if (session == gmSession) {
			entitiesSet.clear();
			removedEntries.clear();
		} else {
			transientEntitiesSet.clear();
			transientRemovedEntries.clear();
		}
		
		addEntities();
	}
	
	@Override
	public void setActionManager(GmContentViewActionManager actionManager) {
		this.actionManager = actionManager;
	}
	
	@Override
	public GmContentViewActionManager getGmContentViewActionManager() {
		return actionManager;
	}	
	@Override
	public ActionProviderConfiguration getActions() {
		if (actionProviderConfiguration != null)
			return actionProviderConfiguration;
		
		ActionProviderConfiguration mdcActionProviderConfiguration = masterDetailConstellation != null ? masterDetailConstellation.getActions() : null;
		if (mdcActionProviderConfiguration != null)
			actionProviderConfiguration = mdcActionProviderConfiguration;
		if (actionProviderConfiguration == null)
			actionProviderConfiguration = new ActionProviderConfiguration();
		
		if (externalActions == null) {
			externalActions = new ArrayList<>();
			externalActions.add(new Pair<>(new ActionTypeAndName("removeChange"), prepareRemoveAction()));
		}
		actionProviderConfiguration.addExternalActions(externalActions);		
		
		return actionProviderConfiguration;
	}
	
	@Override
	public boolean isFilterExternalActions() {
		return masterDetailConstellation != null ? masterDetailConstellation.isFilterExternalActions() : false;
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
	
	protected abstract String getNoChangesMessage();
	
	private HTML getEmptyPanel() {
		if (emptyPanel == null) {
			StringBuilder html = new StringBuilder();
			html.append("<div style='height: 100%; width: 100%; display: table;' class='emptyStyle'>");
			html.append("<div style='display: table-cell; vertical-align: middle'>").append(LocalizedText.INSTANCE.noChanges()).append("</div></div>");
			emptyPanel = new HTML(html.toString());
		}
		
		return emptyPanel;
	}
	
	private void exchangeWidget(Widget widget) {
		if (currentWidget == widget)
			return;
			
		boolean doLayout = false;
		if (currentWidget != null) {
			remove(currentWidget);
			doLayout = true;
		}
		currentWidget = widget;
		add(widget);
		if (doLayout)
			doLayout();
	}
	
	protected void loadData() {
		if (gmSession != null)
			getManipulationListenerTimer(gmSession, entitiesSet, false).schedule(500);
		if (transientSession != null)
			getManipulationListenerTimer(transientSession, transientEntitiesSet, true).schedule(500);
	}
	
	private Timer getManipulationListenerTimer(PersistenceGmSession gmSession, Set<GenericEntity> entitiesSet, boolean isTransient) {
		if (!isTransient && manipulationListenerTimer != null)
			return manipulationListenerTimer;
		
		if (isTransient && transientManipulationListenerTimer != null)
			return transientManipulationListenerTimer;
		
		Timer timer = new Timer() {
			@Override
			public void run() {
				Set<GenericEntity> entities = new LinkedHashSet<>();
				Transaction transaction = gmSession.getTransaction();
				Set<LocalEntityProperty> entityProperties = transaction.getManipulatedProperties();
				if (entityProperties != null)
					entityProperties.forEach(entityProperty -> entities.add(entityProperty.getEntity()));
				
				List<Manipulation> allManipulations = new ArrayList<>();
				List<Manipulation> manipulationsDone = transaction.getManipulationsDone();
				if (manipulationsDone != null)
					allManipulations.addAll(manipulationsDone);
				manipulationsDone = transaction.getCurrentTransactionFrame().getManipulationsDone();
				if (manipulationsDone != null)
					allManipulations.addAll(manipulationsDone);
				if (!allManipulations.isEmpty()) {
					for (Manipulation manipulation : allManipulations) {
						List<GenericEntity> instantiatedEntities = getInstantiatedOrDeletedEntities(manipulation, true);
						if (instantiatedEntities != null)
							entities.addAll(instantiatedEntities);
						
						List<GenericEntity> deletedEntities = getInstantiatedOrDeletedEntities(manipulation, false);
						if (deletedEntities != null)
							entities.addAll(deletedEntities);
					}
				}
				
				List<GenericEntity> entitiesToAdd = new ArrayList<>();
				entities.stream().filter(entity -> !entitiesSet.contains(entity) && checkType(entity) && !removedEntries.contains(entity)
						&& !transientRemovedEntries.contains(entity)).forEach(entity -> {
					entitiesSet.add(entity);
					entitiesToAdd.add(entity);
				});
				
				List<GenericEntity> entitiesRemoved = new ArrayList<>();
				entitiesSet.stream().filter(entity -> !entities.contains(entity)).forEach(entity -> entitiesRemoved.add(entity));

				entitiesRemoved.forEach(entity -> entitiesSet.remove(entity));
				
				if (!entitiesToAdd.isEmpty() || !entitiesRemoved.isEmpty())
					addEntities();
			}
		};
		
		if (isTransient)
			transientManipulationListenerTimer = timer;
		else
			manipulationListenerTimer = timer;
		
		return timer;
	}
	
	private List<GenericEntity> getInstantiatedOrDeletedEntities(Manipulation manipulation, boolean instantiated) {
		if (instantiated && manipulation instanceof InstantiationManipulation)
			return Collections.singletonList(((InstantiationManipulation) manipulation).getEntity());
		
		if (!instantiated && manipulation instanceof DeleteManipulation)
			return Collections.singletonList(((DeleteManipulation) manipulation).getEntity());
		
		if (!(manipulation instanceof CompoundManipulation))
			return null;
		
		List<GenericEntity> instantiatedOrDeletedEntities = null;
		for (Manipulation childManipulation : ((CompoundManipulation) manipulation).getCompoundManipulationList()) {
			List<GenericEntity> instantiatedOrDeletedChildEntities = getInstantiatedOrDeletedEntities(childManipulation, instantiated);
			if (instantiatedOrDeletedChildEntities != null) {
				if (instantiatedOrDeletedEntities == null)
					instantiatedOrDeletedEntities = new ArrayList<>();
				instantiatedOrDeletedEntities.addAll(instantiatedOrDeletedChildEntities);
			}
		}
		
		return instantiatedOrDeletedEntities;
	}
	
	private boolean checkType(GenericEntity entity) {
		return ignoreTypes != null ? !ignoreTypes.contains(entity.entityType().getJavaType()) : true;
	}
	
	private void addEntities() {
		if (entitiesSet.isEmpty() && transientEntitiesSet.isEmpty()) {
			exchangeWidget(getEmptyPanel());
			if (changesAdded) {
				changesAdded = false;
				fireChangesAddedOrRemoved(false);
			}
				
			return;
		}
		
		MasterDetailConstellation masterDetailConstellation = getMasterDetailConstellation();
		handleContentChanged(masterDetailConstellation);
		exchangeWidget(masterDetailConstellation);
		
		changesAdded = true;
		fireChangesAddedOrRemoved(true);
		
		if (verticalTabActionBar != null) {
			Scheduler.get().scheduleDeferred(() -> {
				verticalTabActionBar.configureGmConentView(this);
				verticalTabActionBar.updateElements();
			});
		}		
	}
	
	private void handleContentChanged(MasterDetailConstellation masterDetailConstellation) {
		List<Object> listContent = new ArrayList<>(entitiesSet);
		listContent.addAll(transientEntitiesSet);
		ModelPath modelPath = new ModelPath();
		modelPath.add(new RootPathElement(CollectionType.TYPE_LIST, listContent));
		masterDetailConstellation.setContent(null);
		masterDetailConstellation.addContent(modelPath);
	}
	
	private void fireChangesAddedOrRemoved(boolean added) {
		if (changesConstellationListeners != null)
			changesConstellationListeners.forEach(listener -> listener.onChangesAddedOrRemoved(added));
	}
	
	private MasterDetailConstellation getMasterDetailConstellation() {
		if (masterDetailConstellation != null)
			return masterDetailConstellation;
		
		try {
			masterDetailConstellation = masterDetailConstellationProvider.get();
			if (gmSession != null)
				configureSessionInMaster();
			if (masterDetailConstellation.getCurrentMasterView() instanceof GmSelectionCount)
				((GmSelectionCount) masterDetailConstellation.getCurrentMasterView()).setMaxSelectCount(maxSelection);
			if (actionManager != null)
				actionManager.connect(masterDetailConstellation);		

			fireMasterDetailConstellationProvided();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		
		return masterDetailConstellation;
	}
	
	private void configureSessionInMaster() {
		masterDetailConstellation.configureGmSession(gmSession);
		List<Pair<ActionTypeAndName, ModelAction>> actions = new ArrayList<>();
		actions.add(new Pair<>(new ActionTypeAndName("removeChange"), prepareRemoveAction()));
		
		if (needsReset)
			masterDetailConstellation.resetActions();
		masterDetailConstellation.configureExternalActions(actions);
		
		needsReset = true;
	}
	
	private void fireMasterDetailConstellationProvided() {
		if (masterDetailConstellationProvidedListeners != null)
			masterDetailConstellationProvidedListeners.forEach(listener -> listener.onMasterDetailConstellationProvided(masterDetailConstellation));
	}
	
	private ModelAction prepareRemoveAction() {
		MasterDetailConstellation masterDetailConstellation = getMasterDetailConstellation();
		
		final ModelAction removeAction = new ModelAction() {
			@Override
			public void perform(TriggerInfo triggerInfo) {
				List<ModelPath> selection = masterDetailConstellation.getCurrentSelection();
				for (ModelPath model : selection) {
					Object value = model.last().getValue();
					if (!(value instanceof GenericEntity))
						continue;
					GenericEntity entity = (GenericEntity) value;
					boolean removed = entitiesSet.remove(entity);
					if (removed)
						removedEntries.add(entity);
					else {
						removed = transientEntitiesSet.remove(entity);
						if (removed)
							transientRemovedEntries.add(entity);
					}
					
					if (removed) {
						handleContentChanged(masterDetailConstellation);
						
						if (entitiesSet.isEmpty() && transientEntitiesSet.isEmpty()) {
							exchangeWidget(getEmptyPanel());
							changesAdded = false;
							fireChangesAddedOrRemoved(false);
						}
					}
				}
			}
			
			@Override
			protected void updateVisibility() {
				List<ModelPath> selection = masterDetailConstellation.getCurrentSelection();
				boolean useHidden = (selection == null || selection.size() == 0) ? true : false;									
				setHidden(useHidden);	
			}
		};
		
		masterDetailConstellation.addSelectionListener(gmSelectionSupport -> {
			removeAction.setHidden(gmSelectionSupport.getFirstSelectedItem() == null);
			Scheduler.get().scheduleDeferred(() -> {
				verticalTabActionBar.configureGmConentView(this);
				verticalTabActionBar.updateElements();
				verticalTabActionBar.refresh();
			});
		});
		
		removeAction.setName(LocalizedText.INSTANCE.removeFromChanges());
		removeAction.setIcon(ConstellationResources.INSTANCE.remove());
		removeAction.setHoverIcon(ConstellationResources.INSTANCE.removeBig());
		removeAction.put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
		removeAction.setHidden(true);
		
		return removeAction;
	}
	
	public void prepareLayout() {
		getMasterDetailConstellation();    //initialize masterDetailConstellation
		exchangeWidget(getEmptyPanel());
		prepareVerticalTabActionBar(topPanel);
	}
	
	private void prepareVerticalTabActionBar(BorderLayoutContainer topPanel) {
		if (verticalTabActionBar != null) {
			verticalTabActionBar.setShowDynamicTabElements(verticalTabActionBarVisible);
			verticalTabActionBar.setUseContentMenuAction(verticalTabActionBarVisible);
			verticalTabActionBar.setStaticActionGroup(listAction);
			
			verticalTabActionBar.addStyleName("browsingConstellationActionBar");
			verticalTabActionBar.addStyleName("changesConstellationActionBar");
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
	
	public void setConstellationDefaultModelActions(List<Action> list) {
		this.listAction = list;
	}	

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
