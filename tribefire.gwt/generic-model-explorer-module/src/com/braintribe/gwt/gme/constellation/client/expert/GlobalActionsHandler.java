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
package com.braintribe.gwt.gme.constellation.client.expert;

import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gm.model.uiaction.ActionFolderContent;
import com.braintribe.gwt.action.adapter.gxt.client.MenuItemActionAdapter;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.KnownProperties;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.BrowsingConstellation;
import com.braintribe.gwt.gme.constellation.client.DualSectionButton;
import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gme.constellation.client.MasterDetailConstellation;
import com.braintribe.gwt.gme.constellation.client.QueryConstellation;
import com.braintribe.gwt.gme.constellation.client.RedoAction;
import com.braintribe.gwt.gme.constellation.client.SaveAction;
import com.braintribe.gwt.gme.constellation.client.UndoAction;
import com.braintribe.gwt.gme.constellation.client.action.AdvancedSaveAction;
import com.braintribe.gwt.gme.constellation.client.action.GlobalAction;
import com.braintribe.gwt.gme.constellation.client.action.GlobalActions;
import com.braintribe.gwt.gme.constellation.client.action.GlobalActionsToolBarManager;
import com.braintribe.gwt.gme.tetherbar.client.TetherBar.TetherBarListener;
import com.braintribe.gwt.gme.tetherbar.client.TetherBarElement;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabElement;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabPanel.VerticalTabListener;
import com.braintribe.gwt.gmresourceapi.client.GmImageResource;
import com.braintribe.gwt.gmview.action.client.ActionFolderContentExpert;
import com.braintribe.gwt.gmview.action.client.InstantiateEntityAction;
import com.braintribe.gwt.gmview.action.client.InstantiateTransientEntityAction;
import com.braintribe.gwt.gmview.action.client.KnownGlobalAction;
import com.braintribe.gwt.gmview.client.GmContentSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmExchangeMasterViewListener;
import com.braintribe.gwt.gmview.client.GmListView;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ParentModelPathSupplier;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.folder.FolderContent;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.resource.ResourceAccess;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.template.Template;
import com.braintribe.model.workbench.WorkbenchAction;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

/**
 * This class prepare GlobalActions - Order, Icons, Captions depending on configured WorkbenchFolder
 *
 */

public class GlobalActionsHandler implements InitializableBean, GmExchangeMasterViewListener, GlobalActions {
	public static EntityType<GenericEntity> DEFAULT_ENTITY_TYPE = GenericEntity.T;

	private UndoAction undoAction;
	private UndoAction transientUndoAction;
	private RedoAction redoAction;
	private RedoAction transientRedoAction;
	private SaveAction saveAction;
	private AdvancedSaveAction advancedSaveAction;
	private ModelEnvironmentDrivenGmSession gmSession;
	private ModelEnvironmentDrivenGmSession workbenchSession;
	private Folder rootFolder;
	private Boolean folderInitialized = false;
	private GlobalActionsToolBarManager globalActionsToolBarManager;
	private List<Action> externalToolBarActions;
	private String useCase;
	private InstantiateEntityAction defaultInstantiateEntityAction = null;
	private InstantiateEntityAction contextInstantiateEntityAction = null;
	private Supplier<InstantiateEntityAction> instantiateEntityActionProvider;
	private Supplier<InstantiateTransientEntityAction> instantiateTransientEntityActionProvider;
	private InstantiateTransientEntityAction instantiateTransientEntityAction;
	private List<Action> modelActions = new ArrayList<>();
	private int iconHeight = 32;
	private Function<WorkbenchActionContext<?>, ModelAction> workbenchActionHandlerRegistry;
	private ExplorerConstellation explorerConstellation;
	private List<DualSectionButton> externalDualSectionButtons;
	//private Map<Action, ImageResource> mapImageResourceIcon = new HashMap<>();
	//private Map<Action, ImageResource> mapImageResourceHoverIcon = new HashMap<>();
	//private Map<Action, String> mapDescription = new HashMap<>();
	private List<GlobalActionsListener> globalActionsListeners = new ArrayList<>();	
	private Object destinationPanelForWorkbenchAction = null;
	private Map<Action, Menu> actionMenuMap = new HashMap<>();
	private GmContentView lastSelectedView;
	private BrowsingConstellation lastBrowsingConstellation;
	private TetherBarListener tetherBarListener;
	private Set<GmContentView> finalViewsWithListenersAdded = newSet();
	private GmContentView lastTetherBarView;
	private Map<GmContentView, EntityType<?>> viewEntityTypes = new HashMap<>();
	private List<GlobalAction> globalActionList = new ArrayList<>();
	private ActionFolderContentExpert actionFolderContentExpert = null;
	private EntityType<?> currentEntityType = null;
	private boolean useShortcuts = false;

	@Required
	public void setSaveAction(SaveAction saveAction) {
		this.saveAction = saveAction;
	}
	
	@Required
	public void setRedoAction(RedoAction redoAction) {
		this.redoAction = redoAction;
	}
	
	/**
	 * Configures the required transient RedoAction. It will be only visible when there is at least one transient entity created.
	 */
	@Required
	public void setTransientRedoAction(RedoAction transientRedoAction) {
		this.transientRedoAction = transientRedoAction;
	}
	
	public RedoAction getTransientRedoAction() {
		return this.transientRedoAction;
	}	
	
	@Required
	public void setUndoAction(UndoAction undoAction) {
		this.undoAction = undoAction;
	}
	
	/**
	 * Configures the required {@link AdvancedSaveAction}.
	 */
	@Required
	public void setAdvancedSaveAction(AdvancedSaveAction advancedSaveAction) {
		this.advancedSaveAction = advancedSaveAction;
	}
	
	/**
	 * Configures the required transientUndoAction. It will be only visible when there is at least one transient entity created.
	 */
	@Required
	public void setTransientUndoAction(UndoAction transientUndoAction) {
		this.transientUndoAction = transientUndoAction;
	}

	public UndoAction getTransientUndoAction() {
		return this.transientUndoAction;
	}	
	
	/**
	 * Configures the required {@link ModelEnvironmentDrivenGmSession}.
	 */
	@Required
	public void setGmSession(ModelEnvironmentDrivenGmSession gmSession) {
		this.gmSession = gmSession;
		undoAction.setGmSession(gmSession);
		redoAction.setGmSession(gmSession);
		saveAction.configureGwtPersistenceSession(gmSession);
	}
	
	/**
	 * Configures the registry for {@link WorkbenchAction}s handlers.
	 */
	@Required
	public void setWorkbenchActionHandlerRegistry(Function<WorkbenchActionContext<?>, ModelAction> workbenchActionHandlerRegistry) {
		this.workbenchActionHandlerRegistry = workbenchActionHandlerRegistry;
	}	
	
	/**
	 * Configures the {@link ExplorerConstellation} where the action configured via {@link #setInstantiateEntityActionProvider(Supplier)} will take its context information.
	 * If such an action is configured, then this is required.
	 */
	@Configurable
	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
	}
	
	/**
	 * Configures the useCase to be used within the global actions.
	 */
	@Configurable
	public void setUseCase(String useCase) {
		this.useCase = useCase;
	}
	
	/**
	 * Configures an optional action provider that will use the current modelPath context in order to instantiate a new entity of the type of the entity in that context.
	 * If the context expose no entity type, then the {@link GenericEntity} will be used for this action.
	 */
	@Configurable
	public void setInstantiateEntityActionProvider(Supplier<InstantiateEntityAction> instantiateEntityActionProvider) {
		this.instantiateEntityActionProvider = instantiateEntityActionProvider;
	}
	
	@Configurable
	public void setActionFolderContentExpert(ActionFolderContentExpert actionFolderContentExpert) {
	   this.actionFolderContentExpert = actionFolderContentExpert;
	}	
	
	public Supplier<InstantiateEntityAction> getInstantiateEntityActionProvider() {
		return this.instantiateEntityActionProvider;
	}
	
	public void setExternalDualSectionButtons(List<DualSectionButton> externalDualSectionButtons) {
		this.externalDualSectionButtons = externalDualSectionButtons;		
	}
	
	/**
	 * Configures an optional action provider that will use the current modelPath context in order to instantiate a new entity of the type of the entity in that context.
	 * If the context expose no entity type, then the {@link GenericEntity} will be used for this action.
	 * The entity will be instantiated as transient.
	 */
	@Configurable
	public void setTransientInstantiateEntityActionProvider(Supplier<InstantiateTransientEntityAction> instantiateTransientEntityActionProvider) {
		this.instantiateTransientEntityActionProvider = instantiateTransientEntityActionProvider;
	}
	
	
	@Configurable
	public void setExternalToolBarActions(List<Action> externalToolBarActions) {
		this.externalToolBarActions = externalToolBarActions;
	}
	
	public void setDestinationPanelForWorkbenchAction (Object destinationPanelForWorkbenchAction) {
		this.destinationPanelForWorkbenchAction = destinationPanelForWorkbenchAction;
	}
	
	public List<Action> getExternalToolBarActions() {
		return this.externalToolBarActions;
	}
	
	public List<Action> getModelActions() {
		return this.modelActions; 
	}
	
	public InstantiateEntityAction getDefaultInstantiateEntityAction() {
		return this.defaultInstantiateEntityAction;
	}
		
	public InstantiateEntityAction getContextInstantiateEntityAction() {
		return this.contextInstantiateEntityAction;
	}	
	
	public SaveAction getSaveAction() {
		return saveAction;
	}
	
	public AdvancedSaveAction getAdvancedSaveAction() {
		return advancedSaveAction;
	}	
	
	public InstantiateTransientEntityAction getInstantiateTransientEntityAction() { 
	   return this.instantiateTransientEntityAction;
	}
		
	private void setCurrentEntityType(EntityType<?> entityType) {		
		this.currentEntityType = entityType;
		if (contextInstantiateEntityAction != null)
			contextInstantiateEntityAction.configureEntityType(currentEntityType);		
		updateContextInstantiateEntityMenu();		
	}

	private void updateContextInstantiateEntityMenu() {
		Menu menu = getActionMenu(contextInstantiateEntityAction);
		if (menu == null)
			return;
		MenuItem instantiationMenuItem = menu.getData("instantiationMenuItem");
		if (instantiationMenuItem != null) {
			if (contextInstantiateEntityAction.getHidden() || contextInstantiateEntityAction.getEntityType() == GlobalActionsHandler.DEFAULT_ENTITY_TYPE) {
				//if (!(instantiateTransientEntityAction != null && !instantiateTransientEntityAction.getEnabled())) {
				if (instantiateTransientEntityAction == null || instantiateTransientEntityAction.getHidden()) {	
				    instantiationMenuItem.setVisible(false);
				}
			} else { 
				instantiationMenuItem.setVisible(true);
			}
		}
	}
	
	/**
	 * Returns the {@link EntityType} associated with the given view.
	 */
	public EntityType<?> getViewContextEntityType(GmContentView view) {
		EntityType<?> entityType = viewEntityTypes.get(view);
		if (entityType != null)
			return entityType;
		
		if (!(view instanceof Widget))
			return null;
		
		BrowsingConstellation bc = getBrowsingConstellation(((Widget) view).getParent());
		if (bc == null)
			return null;
		
		return viewEntityTypes.get(bc);
	}	
	
	public Menu getActionMenu(Action action) {
		if (actionMenuMap.containsKey(action))
			return this.actionMenuMap.get(action);
		
		final Menu menu = new Menu();		
		MenuItem item = null;
		if (action instanceof SaveAction) {
			item = new MenuItem();
			MenuItemActionAdapter.linkActionToMenuItem(advancedSaveAction, item);
			menu.add(item);
		} else if ((action instanceof RedoAction) || (action instanceof UndoAction)) {
			item = new MenuItem(LocalizedText.INSTANCE.performAll(action.getName()), action.getHoverIcon());
			item.addSelectionHandler(event -> {
				if (action instanceof RedoAction)
					((RedoAction) action).redoAllManipulations();
				else if (action instanceof UndoAction)
					((UndoAction) action).undoAllManipulations();
			});
			menu.add(item);
		} else if (action.equals(contextInstantiateEntityAction)) {
			//((ActionWithMenu) action).setActionMenu(menu);
			
			//menu = (Menu) ((ActionWithMenu) action).getActionMenu();
			item = new MenuItem(defaultInstantiateEntityAction.getName() + "...");
			ImageResource icon = null;
			ImageResource hoverIcon = null;
			for (GlobalAction globalAction : globalActionList) {
				if (globalAction.getAction().equals(action)) {
					icon = globalAction.getIcon();
					hoverIcon = globalAction.getHoverIcon();
					
					if (icon != null) { 
						instantiateTransientEntityAction.setIcon(icon);
						contextInstantiateEntityAction.setItemDefaultIcon(icon);
					}
					if (hoverIcon != null) { 
						instantiateTransientEntityAction.setHoverIcon(hoverIcon);
						contextInstantiateEntityAction.setItemDefaultIcon(hoverIcon);
					}
					break;
				}
			}
			
			if (icon == null)
				icon = defaultInstantiateEntityAction.getHoverIcon();
			//if (icon == null)
			//	icon = defaultInstantiateEntityAction.getHoverIcon();
			item.setIcon(icon);
			item.setData("action", defaultInstantiateEntityAction);
			item.addSelectionHandler(event -> performDefaultInstantiateEntityAction());
			menu.add(item);

			if (!instantiateTransientEntityAction.getName().contains("..."))
				instantiateTransientEntityAction.setName(instantiateTransientEntityAction.getName() + "...");
			item = new MenuItem();
			//icon = defaultInstantiateEntityAction.getHoverIcon();
			if (icon == null)
				icon = instantiateTransientEntityAction.getHoverIcon();
			item.setIcon(icon);
			MenuItemActionAdapter.linkActionToMenuItem(true, instantiateTransientEntityAction, item);
			//item.setText(instantiateTransientEntityAction.getName() + "...");
			menu.add(item);
			menu.setData("instantiationMenuItem", item);
		} else if (externalDualSectionButtons != null) {
			for (DualSectionButton dualSectionButton : externalDualSectionButtons) {
			    if (action.equals(dualSectionButton.getPrimaryAction())) {
			    	if (dualSectionButton.getSecondaryAction() != null) {				    		
			    		item = new MenuItem();
			    		MenuItemActionAdapter.linkActionToMenuItem(true, dualSectionButton.getSecondaryAction(), item);
			    		menu.add(item);
			    	}
			    	break;
			    }
			}			
		}		
		
		if (action instanceof RedoAction) {
			menu.setEnabled(false);
			((RedoAction) action).addRedoActionListener(manipulationsToRedo -> {
				if (manipulationsToRedo > 1) 
					menu.setEnabled(true);
				else
					menu.setEnabled(false);
				
			});
		} else if (action instanceof UndoAction) {
			menu.setEnabled(false);
			((UndoAction) action).addUndoActionListener(manipulationsToUndo -> {
				if (manipulationsToUndo > 1) 
					menu.setEnabled(true);
				else  
					menu.setEnabled(false);
				
			});
		} else if (action instanceof SaveAction) {
			menu.setEnabled(advancedSaveAction.getEnabled());
			advancedSaveAction.addPropertyListener((source, property) -> {
				if (KnownProperties.PROPERTY_ENABLED.equals(property)) {
					if (advancedSaveAction.getEnabled())
						menu.setEnabled(true);
					else 
						menu.setEnabled(false);
				}
			});
		} else {
			menu.setEnabled(true);
		}
				
		if (item == null) {
			actionMenuMap.put(action, null);		
			return null;
		}
				
		actionMenuMap.put(action, menu);
		if (action.equals(contextInstantiateEntityAction)) 
			updateContextInstantiateEntityMenu();
		
		return menu;
	}
	
	public void updateMenuItems(Action action, ImageResource icon) {
		if (action.equals(contextInstantiateEntityAction)) {
			Menu menu = getActionMenu(action);
			if (menu == null)
				return;
			
			for (int i = 0; i < menu.getWidgetCount(); i++) {
				Widget widget = menu.getWidget(i);
				if (widget == null || !(widget instanceof MenuItem))
					continue;
				
				MenuItem menuItem = (MenuItem) widget;
				Action itemAction = menuItem.getData("action");
				contextInstantiateEntityAction.setItemDefaultIcon(icon);
				if (instantiateTransientEntityAction.equals(itemAction))
					menuItem.setIcon(icon);
				else if (defaultInstantiateEntityAction.equals(itemAction))
					menuItem.setIcon(icon);
			}
			
		}
	}
	
	public List<GlobalAction> getConfiguredActions(boolean useDefault) {		
		List<Action> actionDualSectionList = new ArrayList<>();
		if (externalDualSectionButtons != null) {
			for (DualSectionButton dualSectionButton : externalDualSectionButtons) {
				//set default values
				
				if (useDefault) {
					actionDualSectionList.add(dualSectionButton.getPrimaryAction());
				} else {
					String name = dualSectionButton.getName();				
					final Action actionDualSection = new Action() {								
						@Override
						public void perform(TriggerInfo triggerInfo) {
							//NOP
						}
					};
					actionDualSection.setIcon(dualSectionButton.getIcon());
					actionDualSection.setHoverIcon(dualSectionButton.getIcon());
					actionDualSection.setName(name);
					actionDualSectionList.add(actionDualSection);
				}
			}
		}
		
		List<Action> actionList = new ArrayList<>();
		actionList.add(transientUndoAction);
		actionList.add(transientRedoAction);
		actionList.add(contextInstantiateEntityAction);
		if (externalToolBarActions != null)
			externalToolBarActions.forEach(a -> actionList.add(a));
		actionList.addAll(actionDualSectionList);
		actionList.add(undoAction);
		actionList.add(redoAction);
		actionList.add(saveAction);						
		
		globalActionList.clear();
		for (Action action : actionList) {
			String knownName = action instanceof KnownGlobalAction ? ((KnownGlobalAction) action).getKnownName() : action.getName().toLowerCase();
			GlobalAction globalAction = new GlobalAction(knownName);
			globalAction.setAction(action);
			globalActionList.add(globalAction);
		}
		
		return prepareActionIcons(globalActionList, useDefault);
	}
	
	/**
	 * get Action Order, Text and Icon for configured Global Actions
	 */	
	private List<GlobalAction> prepareActionIcons(List<GlobalAction> actionList, boolean useDefaultOrder) {
		List<GlobalAction> orderActionList = new ArrayList<>();
		 
		if (actionList == null)
			return orderActionList;
		
		if (!folderInitialized && this.globalActionsToolBarManager != null && this.globalActionsToolBarManager.getRootFolder() != null) {
			this.rootFolder = this.globalActionsToolBarManager.getRootFolder();
			folderInitialized = true;
		}
		
		
		if (this.rootFolder != null && this.rootFolder.getSubFolders() != null) {
	        prepareIconsFromFolder(actionList, orderActionList, this.rootFolder);
		} else {
			actionList.stream().filter(a -> a != null).forEach(globalAction -> {
				Action action = globalAction.getAction();
				ImageResource icon = action.getIcon();
				if (icon != null) 
					globalAction.setIcon(icon);
        		ImageResource hoverIcon = action.getHoverIcon();
				if (hoverIcon != null) 
					globalAction.setHoverIcon(hoverIcon);
			});
		}
		
		if (orderActionList.isEmpty() && this.rootFolder == null)
			return actionList;
		
		if (useDefaultOrder) 
			return actionList;
		else
			return orderActionList;
	}
	
	@Override
	public Future<Void> apply(Folder folder) throws RuntimeException {
		this.rootFolder = folder;
		folderInitialized  = true;
		//RVE prepare Actions from configured workbench Folder ..set Icons + Descriptions
		fireGlobalActionsListers();
		updateState();
		return new Future<>(null);
	}
	
	public void setGloabalActionsToolBarLoaderManeger(GlobalActionsToolBarManager manager) {
		this.globalActionsToolBarManager = manager;
		manager.addToolBar(this);
	}

	@Override
	public void setWorkbenchSession(PersistenceGmSession workbenchSession) {
		this.workbenchSession = (ModelEnvironmentDrivenGmSession) workbenchSession;
	}
	
	public void addGlobalActionListener(GlobalActionsListener listener) {
		this.globalActionsListeners.add(listener);
	}
	
	public void removeGlobalActionListener(GlobalActionsListener listener) {
		this.globalActionsListeners.remove(listener);
	}
	
	private void fireGlobalActionsListers()	{
		for (GlobalActionsListener listener : this.globalActionsListeners) {
			if (listener != null)
				listener.onGlobalActionsPrepared();
		}
	}
	
	private boolean containsModelAction(List<GlobalAction> actionList, ModelAction modelAction) {
		boolean res = false;
		
		for (GlobalAction globalAction : actionList) {
			if (globalAction.getAction() == modelAction) {
				res = true;
				break;
			}
		}
		
		return res;
	}
	
	private void prepareIconsFromFolder(List<GlobalAction> actionList, List<GlobalAction> orderActionList, Folder folder) {
		modelActions.clear();

		if (folder == null)
			return;
		List<Folder> subFolders = folder.getSubFolders();
		if (subFolders == null)
			return;
		
		for (Folder subFolder : subFolders) {
			ModelAction modelAction = null;
			String folderName = subFolder.getName().replace("$", "").toLowerCase(); 
			
			FolderContent folderContent = subFolder.getContent();
			if (folderContent != null) {
				if (folderContent instanceof WorkbenchAction) {
					modelAction = prepareModelAction((WorkbenchAction) folderContent, subFolder);
					if (modelAction != null) {
						modelActions.add(modelAction);
						if (!containsModelAction(actionList, modelAction)) {
							String knownName = modelAction instanceof KnownGlobalAction ? ((KnownGlobalAction) modelAction).getKnownName() : modelAction.getName().toLowerCase();
							GlobalAction newGlobalAction = new GlobalAction(knownName);
							newGlobalAction.setAction(modelAction);
							actionList.add(newGlobalAction);
						}
						folderName = modelAction.getName().toLowerCase();
					}
				}
			}

			for (GlobalAction globalAction : actionList) {
				if (globalAction == null)
					continue;
				
				if (!folderName.equals(globalAction.getKnownName()))
					continue;
								
				Action action = globalAction.getAction();
				if (folderContent instanceof ActionFolderContent) {
					if (actionFolderContentExpert != null) {
						if (action != null && action instanceof ModelAction) 
							action = actionFolderContentExpert.getConfiguredAction((ActionFolderContent) folderContent, (ModelAction) action); 
					}						
				}					
				
	        	orderActionList.add(globalAction);
	        	
				String folderDisplayName = I18nTools.getDefault(subFolder.getDisplayName(), "").toString();
				ImageResource imageResourceIcon = null;
				ImageResource imageResourceHoverIcon = null;
				Icon icon = subFolder.getIcon();
				if (icon != null) {							
					//Resource resource = GMEUtil.getMediumImageFromIcon(icon);
					Resource resource = GMEIconUtil.getImageFromIcon(icon, iconHeight, iconHeight);
					ResourceAccess resources = this.workbenchSession.resources();
					if (resource != null)
						imageResourceIcon = new GmImageResource(resource, resources.url(resource).asString());
					resource =  GMEIconUtil.getSmallImageFromIcon(icon);
					if (resource != null)
						imageResourceHoverIcon = new GmImageResource(resource, resources.url(resource).asString());
				}
				
				if (imageResourceIcon == null)
					imageResourceIcon = globalAction.getIcon();
				if (imageResourceHoverIcon == null)
					imageResourceHoverIcon = globalAction.getHoverIcon();

				if (folderDisplayName != null && !folderDisplayName.isEmpty()) 
					globalAction.setDescription(folderDisplayName);
	        	if (imageResourceIcon != null) {
					globalAction.setIcon(imageResourceIcon);
	        	}
	    		if (imageResourceHoverIcon != null) { 
	    			globalAction.setHoverIcon(imageResourceHoverIcon);
		    		if (action != null && action.equals(contextInstantiateEntityAction))
		    			contextInstantiateEntityAction.setItemDefaultIcon(imageResourceHoverIcon);
	    		}
	    			    		
	    		break;
		    }
			
			List<Folder> folders = subFolder.getSubFolders();
			if (folders != null && !folders.isEmpty())
				prepareIconsFromFolder(actionList, orderActionList, subFolder); 			
		}
	}

	@Override
	public void intializeBean() throws Exception {
		if (instantiateEntityActionProvider != null) {
			defaultInstantiateEntityAction = instantiateEntityActionProvider.get();
			configureInstantiateEntityAction(defaultInstantiateEntityAction);
			defaultInstantiateEntityAction.configureDefaultEntityType();			
			
			contextInstantiateEntityAction = instantiateEntityActionProvider.get();
			contextInstantiateEntityAction.setUseShortcuts(useShortcuts);
			contextInstantiateEntityAction.setDisplayEntityNameInAction(true);
			configureInstantiateEntityAction(contextInstantiateEntityAction);			
			contextInstantiateEntityAction.configureDefaultEntityType();	
		}		
		
		if (instantiateTransientEntityActionProvider != null) {
			instantiateTransientEntityAction = instantiateTransientEntityActionProvider.get();
			configureInstantiateTransientEntityAction(instantiateTransientEntityAction);
		}		
	}	

	public void prepareListeners() {		
		addExplorerConstellationVerticalTabPanelListener();		
	}
	
	
	public void configureInstantiateEntityAction(InstantiateEntityAction instantiateEntityAction) {
		instantiateEntityAction.configureUseCase(useCase);
		instantiateEntityAction.configureGmSession(gmSession);
		instantiateEntityAction.configureInstantiatedEntityListener(explorerConstellation);
	}
	
	private void configureInstantiateTransientEntityAction(InstantiateTransientEntityAction instantiateTransientEntityAction) {
		instantiateTransientEntityAction.configureUseCase(useCase);
		instantiateTransientEntityAction.configureInstantiatedEntityListener(explorerConstellation);
	}

	public void updateState() {
		if (instantiateTransientEntityAction != null) {
			instantiateTransientEntityAction.updateState(null);
		}
	}
	
	private ModelAction prepareModelAction(WorkbenchAction workbenchAction, Folder folder) {
		return workbenchActionHandlerRegistry.apply(prepareWorkbenchActionContext(workbenchAction, folder));
	}
	
		
	//public void performDefaultInstantiateEntityAction(InstantiateEntityAction defaultInstantiateEntityAction) {
	public void performDefaultInstantiateEntityAction() {
		if (defaultInstantiateEntityAction.getEntityType() == null)
			defaultInstantiateEntityAction.configureDefaultEntityType();
		defaultInstantiateEntityAction.perform(null);
	}	
	
	private WorkbenchActionContext<WorkbenchAction> prepareWorkbenchActionContext(WorkbenchAction workbenchAction, Folder folder) {
		return new WorkbenchActionContext<WorkbenchAction>() {
			@Override
			public GmSession getGmSession() {
				return gmSession;
			}

			@Override
			public List<ModelPath> getModelPaths() {
				return Collections.emptyList();
			}

			@Override
			@SuppressWarnings("unusable-by-js")
			public WorkbenchAction getWorkbenchAction() {
				if (workbenchAction != null)
					return workbenchAction;
				
				return (WorkbenchAction) folder.getContent();
			}

			@Override
			public Object getPanel() {
				return destinationPanelForWorkbenchAction;
			}
			
			@Override
			@SuppressWarnings("unusable-by-js")
			public Folder getFolder() {
				return folder;
			}
		};
	}
	
	private void addExplorerConstellationVerticalTabPanelListener() {
		explorerConstellation.getVerticalTabPanel().addVerticalTabListener(new VerticalTabListener() {
			@Override
			public void onVerticalTabElementSelected(VerticalTabElement previousVerticalTabElement, VerticalTabElement verticalTabElement) {
				handleVerticalTabElementSelected(verticalTabElement);
			}

			@Override
			public void onVerticalTabElementAddedOrRemoved(int elements, boolean added, List<VerticalTabElement> verticalTabElements) {
				if (verticalTabElements == null || verticalTabElements.isEmpty())
					return;
				
				if (!added)
					handleVerticalTabElementsRemoved(verticalTabElements);
				else
					handleVerticalTabElementsAdded(verticalTabElements);
			}

			@Override
			public void onHeightChanged(int newHeight) {
				//NOP
			}
		});
	}
	
	private void handleVerticalTabElementSelected(VerticalTabElement verticalTabElement) {
		EntityType<?> entityType = null;
		Widget widget = verticalTabElement.getWidget();
		if (widget instanceof GmContentView) {
			lastSelectedView = (GmContentView) widget;
			entityType = getViewContextEntityType(lastSelectedView);
			
			configurePossibleBrowsingConstellationListener();
		} else
			lastSelectedView = null;
		
		//contextInstantiateEntityAction.configureGmContentView(lastSelectedView);
		if (contextInstantiateEntityAction != null)
			contextInstantiateEntityAction.configureGmContentView(getFinalView(lastSelectedView));

				
		if (entityType == null)
			entityType = GlobalActionsHandler.DEFAULT_ENTITY_TYPE;
		
		//if (entityType != currentEntityType) {
			setCurrentEntityType(entityType);
		//}
	}
	
	private void handleVerticalTabElementsRemoved(List<VerticalTabElement> verticalTabElements) {
		for (VerticalTabElement verticalTabElement : verticalTabElements) {
			Widget widget = verticalTabElement.getWidgetIfSupplied();
			if (widget instanceof GmContentView)
				viewEntityTypes.remove((GmContentView) widget);
		}
	}
	
	private void handleVerticalTabElementsAdded(List<VerticalTabElement> verticalTabElements) {
		for (VerticalTabElement verticalTabElement : verticalTabElements) {
			Widget widget = verticalTabElement.getWidget();
			if (!(widget instanceof GmContentView))
				continue;
			
			GmContentView parentContentView = ((GmContentView) widget);
			
			EntityType<?> contextEntityType = getContextEntityType(verticalTabElement.getModelObject());
			if (contextEntityType != null) {
				viewEntityTypes.put(parentContentView, contextEntityType);
				if (lastSelectedView == parentContentView) {
					if (contextInstantiateEntityAction != null)
						contextInstantiateEntityAction.configureGmContentView(parentContentView);
					setCurrentEntityType(contextEntityType);
				}
				continue;
			}
			
			GmContentView finalView = getFinalView(parentContentView);
			finalViewsWithListenersAdded.add(finalView);
			if (finalView instanceof GmContentSupport) {
				((GmContentSupport) finalView).addGmContentViewListener(contentView -> {
					EntityType<?> entityType = getContextEntityPath(contentView);
					viewEntityTypes.put(parentContentView, entityType);
					if (lastSelectedView == parentContentView && entityType != null) {
						if (contextInstantiateEntityAction != null)
							contextInstantiateEntityAction.configureGmContentView(contentView);
						setCurrentEntityType(entityType);
					}
				});
			}
		}
	}
	
	private EntityType<?> getContextEntityType(Object modelObject) {
		if (!(modelObject instanceof Template) && !(modelObject instanceof Query))
			return null;
		
		Query query = null;
		if (modelObject instanceof Query)
			query = (Query) modelObject;
		else {
			Object prototype = ((Template) modelObject).getPrototype();
			if (!(prototype instanceof Query))
				return null;
			
			query = (Query) prototype;
		}
		
		String entityTypeSignature = null;
		if (query instanceof EntityQuery)
			entityTypeSignature = ((EntityQuery) query).getEntityTypeSignature();
		else if (query instanceof SelectQuery)
			entityTypeSignature = GMEUtil.getSingleEntityTypeSignatureFromSelectQuery((SelectQuery) query);
		
		if (entityTypeSignature != null)
			return GMF.getTypeReflection().findEntityType(entityTypeSignature);
		
		return null;
	}
	
	private GmContentView getFinalView(GmContentView parentView) {
		if (parentView instanceof BrowsingConstellation) {
			GmContentView view = ((BrowsingConstellation) parentView).getCurrentContentView();
			if (view != parentView)
				return getFinalView(view);
		}
		
		if (parentView instanceof QueryConstellation) {
			GmContentView view = ((QueryConstellation) parentView).getView();
			if (view != parentView)
				return getFinalView(view);
		}
		
		if (parentView instanceof MasterDetailConstellation) {
			GmContentView view = ((MasterDetailConstellation) parentView).getCurrentMasterView();
			if (view != parentView)
				return getFinalView(view);
		}
		
		return parentView;
	}
	
	private void configurePossibleBrowsingConstellationListener() {
		BrowsingConstellation browsingConstellation = getParentBrowsingConsetllation((Widget) lastSelectedView);
		if (browsingConstellation != lastBrowsingConstellation) {
			if (lastBrowsingConstellation != null) {
				lastBrowsingConstellation.getTetherBar().removeTetherBarListener(getTetherBarListener());
				lastBrowsingConstellation.removeExchangeMasterViewListener(this);
			}						
			
			lastBrowsingConstellation = browsingConstellation;
			
			if (lastBrowsingConstellation != null) {
				lastBrowsingConstellation.getTetherBar().addTetherBarListener(getTetherBarListener());
				lastBrowsingConstellation.addExchangeMasterViewListener(this);
			}
		}
	}

	private TetherBarListener getTetherBarListener() {
		if (tetherBarListener != null)
			return tetherBarListener;
		
		tetherBarListener = new TetherBarListener() {
			@Override
			public void onTetherBarElementSelected(TetherBarElement tetherBarElement) {
				GmContentView contentView = tetherBarElement.getContentViewIfProvided();
				if (contentView == null)
					return;
				
				lastTetherBarView = contentView;
				if (contextInstantiateEntityAction != null)
					contextInstantiateEntityAction.configureGmContentView(contentView);
				
				final GmContentView finalView = getFinalView(contentView);
				if (finalViewsWithListenersAdded.add(finalView)) {
					if (finalView instanceof GmContentSupport) {
						((GmContentSupport) finalView).addGmContentViewListener(contentViewSet -> {
							if (contentViewSet == getFinalView(lastTetherBarView))
								checkAndConfigureEntityType(contentViewSet);
						});
					}
				} else
					checkAndConfigureEntityType(finalView);
			}

			private void checkAndConfigureEntityType(final GmContentView finalView) {
				EntityType<?> entityType = viewEntityTypes.computeIfAbsent(finalView, GlobalActionsHandler.this::getContextEntityPath);
				if (entityType == null)
					entityType = GlobalActionsHandler.DEFAULT_ENTITY_TYPE;
				
				setCurrentEntityType(entityType);
			}
			
			@Override
			public void onTetherBarElementsRemoved(List<TetherBarElement> tetherBarElementsRemoved) {
				//NOP
			}
			
			@Override
			public void onTetherBarElementAdded(TetherBarElement tetherBarElementAdded) {
				//NOP
			}
		};
		
		return tetherBarListener;
	}
	
	private BrowsingConstellation getParentBrowsingConsetllation(Widget widget) {
		if (widget instanceof BrowsingConstellation)
			return (BrowsingConstellation) widget;
		
		Widget parent = widget.getParent();
		if (parent != null)
			return getParentBrowsingConsetllation(parent);
		
		return null;	
	}	
	
	private EntityType<?> getContextEntityPath(GmContentView view) {
		if (view == null)
			return null;
		
		EntityType<?> entityType = getParentModelPathSupplierEntityPath(view);
		if (entityType != null)
			return entityType;
		
		ModelPath modelPath = view.getContentPath();
		if (modelPath != null)
			entityType = getContextEntityPath(modelPath);
		else if (view instanceof GmListView) {
			List<ModelPath> modelPaths = ((GmListView) view).getAddedModelPaths();
			if (modelPaths != null && !modelPaths.isEmpty())
				entityType = getContextEntityPath(modelPaths.get(0));
		}
		
		return entityType;
	}

	private EntityType<?> getParentModelPathSupplierEntityPath(Object view) {
		if (view instanceof ParentModelPathSupplier) {
			ModelPath modelPath = ((ParentModelPathSupplier) view).apply(null);
			if (modelPath != null && modelPath.last().getType().isEntity())
				return modelPath.last().getType();
		} else if (view instanceof Widget)
			return getParentModelPathSupplierEntityPath(((Widget) view).getParent());
		
		return null;
	}
	
	private EntityType<?> getContextEntityPath(ModelPath modelPath) {
		if (modelPath == null)
			return null;
		
		EntityType<?> entityType = null;
		GenericModelType type = modelPath.last().getType();
		if (type.isEntity())
			entityType = (EntityType<?>) type;
		else if (type.isCollection()) {
			CollectionType collectionType = (CollectionType) type;
			switch (collectionType.getCollectionKind()) {
			case list:
			case set:
				if (collectionType.getCollectionElementType().isEntity())
					entityType = (EntityType<?>) collectionType.getCollectionElementType();
				break;
			case map:
				if (collectionType.getCollectionElementType().isEntity())
					entityType = (EntityType<?>) collectionType.getCollectionElementType();
				else if (collectionType.getParameterization()[1].isEntity())
					entityType = (EntityType<?>) collectionType.getParameterization()[1];
				break;
			}
		}
		
		return entityType;
	}	
	
	private BrowsingConstellation getBrowsingConstellation(Widget widget) {
		if (widget instanceof BrowsingConstellation)
			return (BrowsingConstellation) widget;
		
		if (widget == null)
			return null;
		
		return getBrowsingConstellation(widget.getParent());
	}

	public boolean isUseShortcuts() {
		return useShortcuts;
	}

	public void setUseShortcuts(boolean useShortcuts) {
		this.useShortcuts = useShortcuts;
	}

	@Override
	public void onExchangeMasterView(GmContentView parentContentView, GmContentView newContentView) {
		if (defaultInstantiateEntityAction != null)
			defaultInstantiateEntityAction.configureGmContentView(newContentView);
		
		if (contextInstantiateEntityAction != null)
			contextInstantiateEntityAction.configureGmContentView(newContentView);
	}	
}
