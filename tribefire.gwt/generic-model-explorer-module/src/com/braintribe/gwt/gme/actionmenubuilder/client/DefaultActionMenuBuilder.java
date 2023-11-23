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
package com.braintribe.gwt.gme.actionmenubuilder.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gm.model.uiaction.ActionFolderContent;
import com.braintribe.gwt.action.adapter.gxt.client.MenuItemActionAdapter;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.ActionMenu;
import com.braintribe.gwt.action.client.ActionOrGroup;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.action.SeparatorAction;
import com.braintribe.gwt.gmresourceapi.client.GmImageResource;
import com.braintribe.gwt.gmview.action.client.ActionFolderContentExpert;
import com.braintribe.gwt.gmview.action.client.ActionGroup;
import com.braintribe.gwt.gmview.action.client.ActionMenuBuilder;
import com.braintribe.gwt.gmview.action.client.ActionMenuProvider;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.action.client.ActionWithoutContext;
import com.braintribe.gwt.gmview.client.GmAmbiguousSelectionSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmSelectionSupport;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.client.ParentModelPathSupplier;
import com.braintribe.gwt.gmview.client.SplitAction;
import com.braintribe.gwt.gmview.codec.client.KeyConfigurationRendererCodec;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gxt.gxtresources.orangemenuitem.client.GroupSeparatorMenuItemAppearance;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.workbench.WorkbenchAction;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.shared.FastSet;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.event.BeforeShowEvent;
import com.sencha.gxt.widget.core.client.event.BeforeShowEvent.BeforeShowHandler;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.menu.SeparatorMenuItem;

public class DefaultActionMenuBuilder implements ActionMenuBuilder {
	private static int externalActionsCounter = 0;
	
	private PersistenceGmSession gmSession;
	private Function<WorkbenchActionContext<?>, ModelAction> workbenchActionHandlerRegistry;
	private Folder rootFolder;
	private PersistenceGmSession workbenchSession;
	private final Map<Menu, Set<String>> menuExternalItemNames = new HashMap<>();
	private final Map<MenuItem, Menu> actionFolderMenus = new HashMap<>();
	private Map<MenuItem, SplitAction> splitMenuItems;
	private Map<ActionTypeAndName, Boolean> actionAvailabilityMap;
	private ModelPath rootModelPath;
	private boolean rootModelPathChecked;
	private Supplier<? extends Action> separatorActionProvider;
	private ActionFolderContentExpert actionFolderContentExpert;
	
	public DefaultActionMenuBuilder() {
	}

	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	@Override
	public PersistenceGmSession getGmSession() {
		return gmSession;
	}

	/**
	 * Configures the registry for {@link WorkbenchAction}s handlers.
	 */
	@Required
	public void setWorkbenchActionHandlerRegistry(Function<WorkbenchActionContext<?>, ModelAction> workbenchActionHandlerRegistry) {
		this.workbenchActionHandlerRegistry = workbenchActionHandlerRegistry;
	}
	
	/**
	 * Configures the required workbench session.
	 */
	@Required
	@Override
	public void setWorkbenchSession(PersistenceGmSession workbenchSession) {
		this.workbenchSession = workbenchSession;
	}
	
	@Required
	public void setSeparatorActionProvider(Supplier<? extends Action> separatorActionProvider) {
		this.separatorActionProvider = separatorActionProvider;
	}	
	
	/**
	 * Configures the required {@link ActionFolderContentExpert} which can additionally configure special action parameters
	 */	
	@Required
	public void setActionFolderContentExpert(ActionFolderContentExpert actionFolderContentExpert) {
		this.actionFolderContentExpert = actionFolderContentExpert;
	}
		
	@Override
	public Future<Void> apply(Folder folder) throws RuntimeException {
		rootFolder = folder;
		return new Future<>(null);
	}
	
	private void handleIncompleteActionGroups(List<ActionGroup> actionGroupList, boolean isRoot, Map<String, ? extends Widget> externalComponents,
			List<? extends Widget> externalComponentsList) {
		if (actionGroupList == null)
			return;
		
		for (ActionGroup actionGroup : actionGroupList) {
			handleIncompleteActionGroups(actionGroup.getActionList(), false, externalComponents, externalComponentsList);
			
			if (!isRoot && !actionGroup.isComplete() && externalComponents != null) {
				String name = actionGroup.getActionName().substring(1);
				Widget component = externalComponents.remove(name);
				if (component != null)
					externalComponentsList.remove(component);
			}
		}
	}
	
	private Menu prepareActionMenu(ActionMenu actionMenu) {
		Menu menu = new Menu();
		menu.setMinWidth(180);
		
		for (Action action : actionMenu.getActions()) {
			if (action instanceof SeparatorAction) {			
				SeparatorMenuItem separatorItem = prepareSeparatorMenuItemIcon(action.getName(), action.getIcon());
				menu.add(separatorItem);
			} else {
				MenuItem menuItem = new MenuItem();
				MenuItemActionAdapter.linkActionToMenuItem(action, menuItem, true);
				menu.add(menuItem);
			}
		}
		
		return menu;
	}
	
	private SeparatorMenuItem prepareSeparatorMenuItem(String text, Icon icon) {
	    if (icon == null)
	    	return prepareSeparatorMenuItemIcon(text, null);	
	   
     	GmImageResource iconResource = getImageResourceFromIcon(icon);
		return prepareSeparatorMenuItemIcon(text, iconResource);	
	}
	
	private SeparatorMenuItem prepareSeparatorMenuItemIcon(String text, ImageResource icon) {
		SeparatorMenuItem separatorItem;
		if (text == null && icon == null) {
			separatorItem = new SeparatorMenuItem();
		} else {
			GroupSeparatorMenuItemAppearance appearance;
			if (icon == null)
				appearance = new GroupSeparatorMenuItemAppearance(SafeHtmlUtils.fromString(text), null);
			else 
				appearance = new GroupSeparatorMenuItemAppearance(SafeHtmlUtils.fromString(text), icon.getSafeUri().asString());
				
			separatorItem = new SeparatorMenuItem(appearance);
		}
		return separatorItem;
	}
	
	private GmImageResource getImageResourceFromIcon(Icon icon) {
		if (icon == null)
			return null;
		
		GmImageResource iconResource = null;
		//RVE change - get as large image as possible, at menu is fixed with .css style - should show images more clearly at browsers without blur
		Resource imageResource = GMEIconUtil.getLargeImageFromIcon(icon);
		if (imageResource == null)
			imageResource = GMEIconUtil.getMediumImageFromIcon(icon);			
		if (imageResource == null)
			imageResource = GMEIconUtil.getSmallImageFromIcon(icon);
		if (imageResource != null)
			iconResource = new GmImageResource(imageResource, workbenchSession.resources().url(imageResource).asString());
		
		return iconResource;
	}		
	
	private Menu prepareActionMenu(ActionMenuProvider actionMenuProvider, GmContentView view) {
		return actionMenuProvider.getMenu(view);
	}

	@Override
	public Menu getContextMenu(final GmContentView view, List<Pair<String, ? extends Widget>> externalComponents, final ActionGroup actionGroup,
			boolean filterExternal) {
		final Menu contextMenu = new Menu();
		contextMenu.setEnableScrolling(false);
		contextMenu.setMinWidth(210);
		contextMenu.addBeforeShowHandler(event -> {
			handleSelectionChanged(actionGroup, view);
			handleSubMenuVisibility(contextMenu, view);
		});
		
		Map<String, Widget> externalComponentsMap = null;
		if (externalComponents != null) {
			externalComponentsMap = new LinkedHashMap<>();
			for (Pair<String, ? extends Widget> entry : externalComponents) {
				String actionName = entry.getFirst();
				if (!filterExternal || isActionAvailable(new ActionTypeAndName(actionName)))
					externalComponentsMap.put(actionName, entry.getSecond());
			}
		}
		
		List<Widget> widgetsList = null;
		if (externalComponentsMap != null)
			widgetsList = new ArrayList<>(externalComponentsMap.values());
		if (actionGroup != null) {
			addActionGroupToMenu(actionGroup, contextMenu, false, externalComponentsMap, widgetsList, contextMenu, view);
			handleIncompleteActionGroups(actionGroup.getActionList(), true, externalComponentsMap, widgetsList);
		}
		
		if (widgetsList != null)
			widgetsList.forEach(widget -> contextMenu.add(widget));
		
		contextMenu.addBeforeShowHandler(new BeforeShowHandler() {			
			@Override
			public void onBeforeShow(BeforeShowEvent event) {
				updateActionMenu(contextMenu);
			}
		});
		
		return contextMenu;
	}
	
	private boolean updateActionMenu(Menu menu) {
		boolean isMenuVisible = false;
		boolean isItemAfterSeparatorVisible = false;
		SeparatorMenuItem separatorItem = null;
		for (Widget item : menu) {	
			boolean isItemHidden = true;
			if (item instanceof MenuItem) {								
				Action action = ((MenuItem) item).getData("action");
				if (action != null) {
					isItemHidden = action.getHidden();
					item.setVisible(!isItemHidden);
					if (separatorItem != null) 
						isItemAfterSeparatorVisible = isItemAfterSeparatorVisible || !isItemHidden;					
				} else if (((MenuItem) item).getSubMenu() != null) {
					//check when Item have subMenu		
					//check recursive the subMenu Items
					isItemHidden = !updateActionMenu(((MenuItem) item).getSubMenu());
					if (separatorItem != null) 
						isItemAfterSeparatorVisible = isItemAfterSeparatorVisible || !isItemHidden; 					
				} 
			} else if (item instanceof SeparatorMenuItem) {
				//check if show separator ..if exists some visible action behind the separator
				if (separatorItem != null) 
					separatorItem.setVisible(isItemAfterSeparatorVisible);
				
				isItemAfterSeparatorVisible = false;
				isItemHidden = true;
				separatorItem = (SeparatorMenuItem) item;				
			} 
			
			isMenuVisible = (isMenuVisible || !isItemHidden);
		}
		if (separatorItem != null) 
			separatorItem.setVisible(isItemAfterSeparatorVisible);
		
		return isMenuVisible;
	}	
	
	private void handleSubMenuVisibility(Menu menu, GmContentView view) {
		for (int i = 0; i < menu.getWidgetCount(); i++) {
			Widget menuItem = menu.getWidget(i);
			//RVE - commented out check for already hidden group Item - because next scenario wasnt wotk correctly
			//right mouse click on Item (TB or AP), than right mouse click on empty page (no item), again right mouse click on Item
			if (menuItem instanceof MenuItem /*& !isComponentHidden((MenuItem) menuItem)*/ && ((MenuItem) menuItem).getSubMenu() != null) {
				MenuItem item = (MenuItem) menuItem;
				Menu subMenu = item.getSubMenu();
				boolean hide = isHideSubMenu(subMenu);
				if (!actionFolderMenus.containsKey(menuItem)) {
					if (!isComponentHidden(item))
						menuItem.setVisible(!hide);
					else if (!item.isVisible() && !hide)
						menuItem.setVisible(true);
				} else {
					if (hide)
						item.setSubMenu(null);
					//else if (!hide && subMenu == null)
						//item.setSubMenu(actionFolderMenus.get(menuItem));
				}
				handleSubMenuVisibility(subMenu, view);
			}
			
			SplitAction splitAction = splitMenuItems != null ? splitMenuItems.get(menuItem) : null;
			if (splitAction != null) {
				MenuItem item = (MenuItem) menuItem;
				item.setSubMenu(splitAction.getMenu(view));
			}
		}
	}
	
	private boolean isHideSubMenu(Menu menu) {
		for (int i = 0; i < menu.getWidgetCount(); i++) {
			Widget menuItem = menu.getWidget(i);
			if (menuItem instanceof MenuItem && ((MenuItem) menuItem).getSubMenu() != null) {
				if (!isHideSubMenu(((MenuItem) menuItem).getSubMenu()))
					return false;
			} else if (menuItem instanceof Component && !isComponentHidden((Component) menuItem))
				return false;
		}
		
		return true;
	}
	
	private String getActionName(ActionGroup actionGroup) {
		String name;
		
		String actionGroupName = actionGroup.getActionName();
		String displayName = actionGroup.getDisplayName();
		Action action = actionGroup.getAction();
		if (actionGroupName != null && !actionGroupName.startsWith("$"))
			name = actionGroupName;
		else if (displayName != null)
			name = displayName;
		else if (action != null && action.getName() != null)
			name = action.getName();
		else
			name = "&nbsp;";
		
		if (action != null)
			action.setName(name);
		
		return name;
	}
	
	private void addActionGroupToMenu(ActionGroup ag, Menu parentMenu, boolean createSubMenu, Map<String, ? extends Widget> externalComponents,
			List<Widget> externalComponentsList, Menu contextMenu, GmContentView view) {
		if (!ag.isComplete()) {
			boolean dataAdd = false;
			String actionName = ag.getActionName().substring(1);
			if (externalComponents != null) {
				Widget component = externalComponents.get(actionName);
				if (component != null) {
					externalComponentsList.remove(component);
					parentMenu.add(component);
					
					Set<String> externalItemNames = menuExternalItemNames.get(contextMenu);
					if (externalItemNames == null) {
						externalItemNames = new FastSet();
						menuExternalItemNames.put(contextMenu, externalItemNames);
					}
					dataAdd = true;
					externalItemNames.add(actionName);
				}
			}
			
			if ((!dataAdd) && (ag.getAction() != null) && (ag.getAction() instanceof SeparatorAction)) {				
				SeparatorMenuItem separatorItem = prepareSeparatorMenuItem(ag.getDisplayName(), ag.getIcon());
				parentMenu.add(separatorItem);					
			}
			
			return;
		}
		
		GmImageResource iconResource = null;
		Icon icon = ag.getIcon();
		if (icon != null) {
			//RVE change - get as large image as possible, at menu is fixed with .css style - should show images more clearly at browsers without blur
			Resource imageResource = GMEIconUtil.getLargeImageFromIcon(icon);
			if (imageResource == null)
				imageResource = GMEIconUtil.getMediumImageFromIcon(icon);			
			if (imageResource == null)
				imageResource = GMEIconUtil.getSmallImageFromIcon(icon);
			if (imageResource != null)
				iconResource = new GmImageResource(imageResource, workbenchSession.resources().url(imageResource).asString());
		}
		
		String actionName = getActionName(ag);
		
		Item subItem = null;
		Action action = ag.getAction();
		if (action != null) {
			action.setName(actionName);
			if (iconResource != null)
				action.setIcon(iconResource);
			List<Item> list = addActionsToContextMenu(Collections.singletonList((ActionOrGroup) action), parentMenu, view);
			if (list != null && !list.isEmpty())
				subItem = list.get(0);
		}
		
		List<ActionGroup> actionList = ag.getActionList();
		if (actionList == null || actionList.isEmpty())
			return;
		
		if (subItem == null && createSubMenu) {
			subItem = new MenuItem(ag.getActionName());
			
			if (iconResource != null)
				((MenuItem) subItem).setIcon(iconResource);
			parentMenu.add(subItem);
		}

		Menu subMenu = subItem != null ? new Menu() : parentMenu;
		actionList.forEach(a -> addActionGroupToMenu(a, subMenu, true, externalComponents, externalComponentsList, contextMenu, view));
		if (subItem != null && subItem instanceof MenuItem) {
			subMenu.setMinWidth(180);
			((MenuItem) subItem).setSubMenu(subMenu);
			if (action != null)
				actionFolderMenus.put((MenuItem) subItem, subMenu);
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public List<Item> addExternalActionsToMenu(GmContentView view, Widget actionMenuWidget, List<ModelAction> externalActions) {
		return addActionsToContextMenu((Collection) externalActions, (Menu) actionMenuWidget, view);
	}
	
	private List<Item> addActionsToContextMenu(Collection<ActionOrGroup> actions, Menu contextMenu, GmContentView view) {
		if (actions == null)
			return null;
		
		
		List<Item> addedMenuItems = new ArrayList<>();
		actions.stream().filter(
				action -> !(action instanceof ActionWithoutContext) && ModelAction.actionBelongsToPosition(action, ModelActionPosition.ContextMenu))
				.forEach(action -> {
			MenuItem item = null;
			if (action instanceof ActionMenu) {
				item = new MenuItem();
				MenuItemActionAdapter.linkActionToMenuItem((Action) action, item);
				contextMenu.add(item);
				item.setSubMenu(prepareActionMenu((ActionMenu) action));
				addedMenuItems.add(item);
			} else if (action instanceof ActionMenuProvider) {
				item = new MenuItem();
				MenuItemActionAdapter.linkActionToMenuItem((Action) action, item);
				contextMenu.add(item);
				item.setSubMenu(prepareActionMenu((ActionMenuProvider) action, view));
				addedMenuItems.add(item);
			} else if (action instanceof SeparatorAction) {
				SeparatorMenuItem separatorItem = prepareSeparatorMenuItemIcon(action.getName(), action.getIcon());
				contextMenu.add(separatorItem);	
				addedMenuItems.add(separatorItem);
			} else	if (action instanceof Action) { //TODO: should I handle GroupAction?
				item = new MenuItem();
				MenuItemActionAdapter.linkActionToMenuItem((Action) action, item);
				contextMenu.add(item);
				addedMenuItems.add(item);
			}
			
			if (item != null && action instanceof SplitAction) {
				if (splitMenuItems == null)
					splitMenuItems = new HashMap<>();
				
				splitMenuItems.put(item, (SplitAction) action);
			}
		});
		
		return addedMenuItems;
	}
	
	@Override
	public List<Item> updateMenu(GmContentView view, Widget actionMenuWidget, List<Pair<ActionTypeAndName, ModelAction>> externalActions,
			ActionGroup actionGroup) {
		Set<String> externalComponentNames = menuExternalItemNames.get(actionMenuWidget);
		if (externalActions == null || actionMenuWidget == null)
			return null;
		
		List<Item> addedMenuItems = null;
		List<MenuConfiguration> menuConfigurations = new ArrayList<>();
		for (Pair<ActionTypeAndName, ModelAction> entry : externalActions) {
			ActionTypeAndName actionTypeAndName = new ActionTypeAndName(entry.getFirst());
			if (actionTypeAndName.getActionName() != null)
				actionTypeAndName.setActionName("$" + actionTypeAndName.getActionName());			
			List<ActionGroup> actionList = actionGroup == null ? null :  actionGroup.getActionList();			
			MenuConfiguration menuConfiguration = prepareMenuConfiguration((Menu) actionMenuWidget, actionTypeAndName, actionList,
					externalComponentNames);
			
			if ((menuConfiguration == null) && (entry.getSecond() != null) && (actionMenuWidget instanceof Menu)) 
				menuConfiguration = new MenuConfiguration((Menu) actionMenuWidget, entry.getSecond(), 0);
			
			if (menuConfiguration != null)
				menuConfigurations.add(menuConfiguration);
		}
		
		if (!menuConfigurations.isEmpty()) {
			addedMenuItems = new ArrayList<>();
		
			for (MenuConfiguration configuration : menuConfigurations) {
				MenuItem menuItem = new MenuItem();
				Action action = configuration.getAction();
				MenuItemActionAdapter.linkActionToMenuItem(action, menuItem);
				
				if (action instanceof ActionMenuProvider)
					menuItem.setSubMenu(prepareActionMenu((ActionMenuProvider) action, view));
				
				int index = configuration.getIndex();
				if (index > configuration.getParentMenu().getWidgetCount())
					configuration.getParentMenu().add(menuItem); //RVE - when insert with high Index as Widgets in map...error occours
				else
					configuration.getParentMenu().insert(menuItem, index);
				addedMenuItems.add(menuItem);
			}
		}
		
		return addedMenuItems;
	}
	
	private MenuConfiguration prepareMenuConfiguration(Menu menu, ActionTypeAndName actionTypeAndName, List<ActionGroup> actionGroups,
			Set<String> externalComponents) {
		if (actionGroups == null)
			return null;
		
		int index = 0;
		for (ActionGroup actionGroup : actionGroups) {
			ActionTypeAndName groupTypeAndName = actionGroup.getActionTypeAndName();
			if (actionTypeAndName.equals(groupTypeAndName)) {
				if (actionGroup.getAction() != null)
					return new MenuConfiguration(menu, actionGroup.getAction(), index);
				return null;
			}
			
			if (actionGroup.getActionList() != null && !actionGroup.getActionList().isEmpty()) {
				MenuConfiguration menuConfiguration = prepareMenuConfiguration(getChildMenu(menu, groupTypeAndName.getActionName()),
						actionTypeAndName, actionGroup.getActionList(), externalComponents);
				if (menuConfiguration != null)
					return menuConfiguration;
			}
			
			if (actionGroup.isComplete() || isMenuContainsItem(groupTypeAndName.getActionName(), externalComponents))
				index++;
		}
		
		return null;
	}
	
	private boolean isMenuContainsItem(String key, Set<String> externalComponents) {
		if (key != null && externalComponents != null)
			return externalComponents.contains(key.substring(1));
		
		return false;
	}
	
	private Menu getChildMenu(Menu parentMenu, String key) {
		for (int i = 0; i < parentMenu.getWidgetCount(); i++) {
			Widget widget = parentMenu.getWidget(i);
			if (widget instanceof MenuItem) {
				MenuItem menuItem = (MenuItem) widget;
				if (key != null && key.equals(menuItem.getText())) {
					Menu subMenu = menuItem.getSubMenu();
					if (subMenu == null) {
						Menu sub = new Menu();
						sub.setMinWidth(180);
						menuItem.setSubMenu(sub);
					}
					
					return menuItem.getSubMenu();
				}
			}
		}
		
		return null;
	}
	
	@Override
	public ActionGroup prepareActionGroup(List<Pair<ActionTypeAndName, ModelAction>> knownActions, GmContentView gmContentView) {
		return prepareRootActionGroup(rootFolder, knownActions, gmContentView);
	}
	
	@Override
	public Set<ActionTypeAndName> updateActionGroup(ActionGroup actionGroup, List<Pair<ActionTypeAndName, ModelAction>> externalActions) {
		Set<ActionTypeAndName> newActionSet = new LinkedHashSet<>();
		if (actionGroup == null || externalActions == null)
			return newActionSet;
		
		List<ActionGroup> actionList = actionGroup.getActionList();
		if (actionList == null) {
			actionList = new ArrayList<>();
			actionGroup.setActionList(actionList);
		}
		
		for (Pair<ActionTypeAndName, ModelAction> entry : externalActions) {
			if (entry == null)
				continue;
			
			ActionTypeAndName actionTypeAndName = entry.getFirst();
			ModelAction action = entry.getSecond();
			
			if (action == null)
				continue;
			
			if (action instanceof ActionWithoutContext || !ModelAction.actionBelongsToPosition(action, ModelActionPosition.ContextMenu))
				continue;
			
			ActionTypeAndName checkTypeAndName = new ActionTypeAndName(actionTypeAndName);
			if (checkTypeAndName.getActionName() != null && !checkTypeAndName.getActionName().startsWith("$"))
				checkTypeAndName.setActionName("$" + checkTypeAndName.getActionName());
			
			ActionGroup ag = getParentActionGroup(actionGroup, checkTypeAndName);
			if (ag != null) {
				ag.setAction(action);
				ag.setComplete(true);
			} else {
				actionList.add(new ActionGroup(action, null, null, checkTypeAndName));
				newActionSet.add(checkTypeAndName);
			}
		}
		
		return newActionSet;
	}
	
	private ActionGroup getParentActionGroup(ActionGroup actionGroup, ActionTypeAndName actionTypeAndName) {
		if (actionTypeAndName.equals(actionGroup.getActionTypeAndName()))
			return actionGroup;
		
		if (actionGroup.getActionList() != null) {
			for (ActionGroup childGroup : actionGroup.getActionList()) {
				ActionGroup parentAction = getParentActionGroup(childGroup, actionTypeAndName);
				if (parentAction != null)
					return parentAction;
			}
		}
		
		return null;
	}
	
	@Override
	public void onSelectionChanged(ActionGroup actionGroup, GmSelectionSupport gmSelectionSupport) {
		handleSelectionChanged(actionGroup, gmSelectionSupport);
	}
	
	private void handleSelectionChanged(ActionGroup actionGroup, GmSelectionSupport gmSelectionSupport) {
		if (actionGroup == null || gmSelectionSupport == null)
			return;
		
		List<List<ModelPath>> modelPaths;
		if (gmSelectionSupport instanceof GmAmbiguousSelectionSupport)
			modelPaths = ((GmAmbiguousSelectionSupport) gmSelectionSupport).getAmbiguousSelection();
		else
			modelPaths = gmSelectionSupport.transformSelection(gmSelectionSupport.getCurrentSelection());
		
		handleSelectionChanged(actionGroup, modelPaths);
	}
	
	private void handleSelectionChanged(ActionGroup actionGroup, List<List<ModelPath>> modelPaths) {
		if (actionGroup.getAction() instanceof ModelAction)
			((ModelAction) actionGroup.getAction()).updateState(modelPaths);
		
		if (actionGroup.getActionList() != null && !actionGroup.getActionList().isEmpty())
			actionGroup.getActionList().forEach(ag -> handleSelectionChanged(ag, modelPaths));
	}
	
	private ActionGroup prepareRootActionGroup(Folder folder, List<Pair<ActionTypeAndName, ModelAction>> knownActions, GmContentView gmContentView) {
		List<ModelAction> knownActionsUsed = new ArrayList<>();
		List<ActionGroup> actionList = new ArrayList<>();
		
		Map<ActionTypeAndName, ModelAction> knownActionsMap = new HashMap<>();
		if (knownActions != null)
			knownActions.forEach(entry -> knownActionsMap.put(entry.getFirst(), entry.getSecond()));
		
		if (folder != null) {
			ActionGroup actionGroup = prepareActionGroupFromRoot(folder);
			if (actionGroup != null)
				actionList.add(actionGroup);
			List<Folder> subFolders = folder.getSubFolders();
			if (subFolders != null) {
				for (Folder subFolder : subFolders) {
					actionGroup = prepareActionGroup(subFolder, knownActions, knownActionsMap, knownActionsUsed, gmContentView);
					if (actionGroup != null)
						actionList.add(actionGroup);
				}
			}
		}
		
		if (knownActions != null) {
			List<ModelAction> allKnownActions = new ArrayList<>();
			knownActions.forEach(entry -> allKnownActions.add(entry.getSecond()));
			allKnownActions.removeAll(knownActionsUsed);
			allKnownActions.forEach(modelAction -> actionList.add(new ActionGroup(modelAction)));
		}
		
		if (!actionList.isEmpty())
			return new ActionGroup(null, actionList);
		
		return null;
	}
	
	private ActionGroup prepareActionGroupFromRoot(Folder folder) {
		String folderName = folder.getName();
		EntityType<? extends ActionFolderContent> denotationType = null;
		if (folder.getContent() instanceof ActionFolderContent)
			denotationType = folder.getContent().entityType();
		Icon icon = folder.getIcon();
		LocalizedString displayName = folder.getDisplayName();
		String name = displayName != null ? I18nTools.getLocalized(displayName) : null;
		Set<String> tags = folder.getTags();
		
		ActionGroup actionGroup = new ActionGroup(null, null, icon, new ActionTypeAndName(denotationType, folderName), name, false);
		actionGroup.getTags().addAll(tags);
		return actionGroup;
	}
	
	private ActionGroup prepareActionGroup(Folder folder, List<Pair<ActionTypeAndName, ModelAction>> knownActions, Map<ActionTypeAndName, ModelAction> knownActionsMap,
			List<ModelAction> knownActionsUsed, GmContentView gmContentView) {
		String folderName = folder.getName();
		EntityType<? extends ActionFolderContent> denotationType = null;
		if (folder.getContent() instanceof ActionFolderContent)
			denotationType = folder.getContent().entityType();
		
		ActionTypeAndName actionTypeAndName = new ActionTypeAndName(denotationType, folderName);
		if (folder.getSubFolders().isEmpty() && !isActionAvailable(actionTypeAndName))
			return null;
		
		ModelAction action = null;
		List<ActionGroup> actionList = new ArrayList<>();
		Icon icon = folder.getIcon();
		LocalizedString displayName = folder.getDisplayName();
		String name = displayName != null ? I18nTools.getLocalized(displayName) : null;
		Set<String> tags = folder.getTags();
		if (folder.getContent() instanceof WorkbenchAction)
			action = prepareModelAction((WorkbenchAction) folder.getContent(), folder, gmContentView);
		
		if (action == null) {
			action = getDefaultAction(actionTypeAndName, knownActionsMap);
			if (action != null) {
				if (folder.getContent() instanceof ActionFolderContent && actionFolderContentExpert != null) 
					actionFolderContentExpert.getConfiguredAction((ActionFolderContent) folder.getContent(), action);
				
				knownActionsUsed.add(action);
			}
		}
		
		List<Folder> subFolders = folder.getSubFolders();
		if (subFolders != null) {
			for (Folder subFolder : subFolders) {
				Set<String> subTags = subFolder.getTags();
				ActionGroup actionGroup = prepareActionGroup(subFolder, knownActions, knownActionsMap, knownActionsUsed, gmContentView);
				if (actionGroup != null) {
					actionGroup.getTags().addAll(subTags);
					actionList.add(actionGroup);
				}
			}
		}
		
		if (action != null || !actionList.isEmpty()) {
			ActionGroup actionGroup = new ActionGroup(action, actionList, icon, new ActionTypeAndName(denotationType, name));
			actionGroup.getTags().addAll(tags);
			return actionGroup;
		}
		
		if (folderName != null && !folderName.isEmpty()) {
			if ((folderName.equals("$separator") || (folderName.equals("$separator-with-label"))) && separatorActionProvider != null) {
			    Action separatorAction = separatorActionProvider.get();
			    String separatorName = name;
			    if (folderName.equals("$separator"))
			    	separatorName = null;
			    separatorAction.setName(separatorName);
				ActionGroup actionGroup = new ActionGroup(separatorAction, null, icon, actionTypeAndName, separatorName, false);
				actionGroup.getTags().addAll(tags);
				return actionGroup;
			} else {
				ActionGroup actionGroup = new ActionGroup(null, null, icon, actionTypeAndName, name, false);
				actionGroup.getTags().addAll(tags);
				return actionGroup;
			}
		}
		
		return null;
	}
	
	private ModelAction getDefaultAction(ActionTypeAndName actionTypeAndName, Map<ActionTypeAndName, ModelAction> knownActions) {
		if (knownActions == null || actionTypeAndName == null)
			return null;
		
		ActionTypeAndName checkTypeAndName = new ActionTypeAndName(actionTypeAndName);
		String actionName = checkTypeAndName.getActionName();
		if (actionName != null && !actionName.isEmpty())
			checkTypeAndName.setActionName(actionName.substring(1));
		
		return knownActions.get(checkTypeAndName);
	}
	
	private ModelAction prepareModelAction(WorkbenchAction workbenchAction, Folder folder, GmContentView gmContentView) {
		ModelAction modelAction = workbenchActionHandlerRegistry.apply(prepareWorkbenchActionContext(workbenchAction, folder, gmContentView));
		if (modelAction != null) {
			modelAction.configureGmContentView(gmContentView);
			modelAction.setHidden(true);
			
			if (folder != null && folder.getContent() != null && ((WorkbenchAction) folder.getContent()).getKeyConfiguration() != null) {
				String stringKeyConfiguration = KeyConfigurationRendererCodec.encodeKeyConfiguration(((WorkbenchAction) folder.getContent()).getKeyConfiguration());
				modelAction.put("keyConfiguration", stringKeyConfiguration);
			}			
		}
		
		return modelAction;
	}
	
	private ModelPath getRootModelPath(GmContentView lastGmContentView, Object currentView) {
		if (rootModelPath != null || rootModelPathChecked)
			return rootModelPath;
		
		if (currentView instanceof ParentModelPathSupplier) {
			rootModelPath = ((ParentModelPathSupplier) currentView).apply(lastGmContentView);
			rootModelPathChecked = true;
			return rootModelPath;
		}
		
		if (currentView instanceof GmContentView)
			lastGmContentView = (GmContentView) currentView;
		
		if (currentView instanceof Widget)
			return getRootModelPath(lastGmContentView, ((Widget) currentView).getParent());
		
		rootModelPathChecked = true;
		return null;
	}
	
	private WorkbenchActionContext<?> prepareWorkbenchActionContext(WorkbenchAction workbenchAction, Folder folder, GmContentView view) {
		return new WorkbenchActionContext<WorkbenchAction>() {
			@Override
			public GmSession getGmSession() {
				return gmSession;
			}

			@Override
			public List<ModelPath> getModelPaths() {
				return view.getCurrentSelection();
			}
			
			@Override
			public ModelPath getRootModelPath() {
				GmContentView view = (GmContentView) getPanel();
				return DefaultActionMenuBuilder.this.getRootModelPath(view, view instanceof Widget ? (Widget) view : null);
			}

			@Override
			@SuppressWarnings("unusable-by-js")
			public WorkbenchAction getWorkbenchAction() {
				return workbenchAction;
			}

			@Override
			public Object getPanel() {
				return view;
			}

			@Override
			@SuppressWarnings("unusable-by-js")
			public Folder getFolder() {
				return folder;
			}
		};
	}
	
	private boolean isActionAvailable(ActionTypeAndName actionTypeAndName) {
		Boolean available = actionAvailabilityMap != null ? actionAvailabilityMap.get(actionTypeAndName) : null;
		if (available != null)
			return available;
		
		if (actionAvailabilityMap == null)
			actionAvailabilityMap = new HashMap<>();
		
		available = GMEUtil.isActionAvailable(actionTypeAndName, rootFolder);
		actionAvailabilityMap.put(actionTypeAndName, available);
		
		return available;
	}
	
	private native boolean isComponentHidden(Component component) /*-{
		return component.@com.sencha.gxt.widget.core.client.Component::hidden;
	}-*/;
	
	private static class MenuConfiguration {
		private final Menu parentMenu;
		private final Action action;
		private final int index;
		
		public MenuConfiguration(Menu parentMenu, Action action, int index) {
			this.parentMenu = parentMenu;
			this.action = action;
			this.index = index;
		}

		public Menu getParentMenu() {
			return parentMenu;
		}

		public Action getAction() {
			return action;
		}

		public int getIndex() {
			return index;
		}
	}
	
	@Override
	public void notifyDisposedView(Widget viewMenu) {
		if (viewMenu instanceof Menu) {
			Menu menu = (Menu) viewMenu;
			Set<String> set = menuExternalItemNames.remove(menu);
			if (set != null) {
				set.clear();
				set = null;
			}
			
			for (int i = 0; i < menu.getWidgetCount(); i++) {
				Widget menuItem = menu.getWidget(i);
				actionFolderMenus.remove(menuItem);
				
				if (splitMenuItems != null)
					splitMenuItems.remove(menuItem);
			}
		}
	}
	
}
