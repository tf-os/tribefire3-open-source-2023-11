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
package com.braintribe.gwt.gme.verticaltabpanel.client;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.ActionOrGroup.PropertyListener;
import com.braintribe.gwt.gme.constellation.client.action.ContentMenuAction;
import com.braintribe.gwt.gmresourceapi.client.GmImageResource;
import com.braintribe.gwt.gmview.action.client.ActionGroup;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.client.GmActionSupport;
import com.braintribe.gwt.gmview.client.GmAmbiguousSelectionSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewActionManager;
import com.braintribe.gwt.gmview.client.GmContentViewActionManagerHandler;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.GmSelectionSupport;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.Resource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

/**
 * ActionBar based on {@link VerticalTabPanel}
 *
 */
public class VerticalTabActionMenu extends VerticalTabPanel implements GmSelectionListener {
	private Map<VerticalTabElement, Menu> selectionViewElementMenuMap = new HashMap<>();
	private Map<Action, VerticalTabElement> selectionViewActionElementMap = new HashMap<>(); 
	private Map<VerticalTabElement, Menu> staticElementMenuMap = new HashMap<>();
	private Map<Action, VerticalTabElement> staticActionElementMap = new HashMap<>(); 
	private GmContentViewActionManager currentActionManager;	
	private PropertyListener actionPropertyListener;
	private boolean showingOrderFromTop = true;
	private ActionGroup currentActionGroup;
	private List<VerticalTabElement> staticTabElements = new ArrayList<>();
	private List<VerticalTabElement> selectionViewTabElements = new ArrayList<>();
	private boolean staticElementsFromTop = false;
	private boolean showDynamicTabElements = true;
	private Supplier<? extends Action> separatorActionProvider;
	private Supplier<? extends Action> contentMenuActionProvider;
	private boolean useSeparatorForDynamic = true;
	private boolean useContentMenuAction = false;
	private Action dynamicActionSeparator;
	private Action contentMenuAction;
	
	public VerticalTabActionMenu() {
		super();
		addStyleName("VerticalTabMenu");
		
		actionPropertyListener = (source, property) -> {
			VerticalTabElement element = selectionViewActionElementMap.get(source);
			if (element == null)
				element = staticActionElementMap.get(source);
			if (element == null)
				return;
							
			//if(Action.PROPERTY_ENABLED.equals(property))
			if (Action.PROPERTY_HIDDEN.equals(property)) {					
				element.setSystemVisible(!source.getHidden());
				refresh();
			}
		}; 			
	}
	
	@Configurable
	public void setSeparatorActionProvider(Supplier<? extends Action> separatorActionProvider) {
		this.separatorActionProvider = separatorActionProvider;
	}
	
	@Configurable
	public void setContentMenuActionProvider(Supplier<? extends Action> contentMenuActionProvider) {
		this.contentMenuActionProvider = contentMenuActionProvider;
	}
	
	
	@Configurable
	public void setUseSeparatorForDynamic(boolean useSeparator) {
		this.useSeparatorForDynamic = useSeparator;
	}
	
	@Configurable
	public void setUseContentMenuAction(boolean useContentMenuAction) {
		this.useContentMenuAction = useContentMenuAction;		
	}	
	
	public void setShowDynamicTabElements(boolean show) {
		this.showDynamicTabElements = show; 
	}
	
	//sort root ActionGroup into separate Groups which can have Menu or can fire Action
	private List<ActionGroup> prepareMenuAsActionGroups(ActionGroup actionGroup) {
		List<ActionGroup> listActionGroup = new ArrayList<>();
		
		if (actionGroup == null)
			return listActionGroup;
		
		if (actionGroup.getActionList() == null || actionGroup.getActionList().isEmpty()) {
			listActionGroup.add(actionGroup);
			return listActionGroup;
		}
		
		//find global root group - to define Icon, Tooltip, Description
		ActionGroup globalGroup = null;
		int i = 0;
		while (i < actionGroup.getActionList().size()) {
			ActionGroup ag = actionGroup.getActionList().get(i);
			//skip Item action for Empty actions
			if (ag == null) {
				i++;
				continue;
			}

			String actionName = ag.getActionName();
			if (actionName != null && actionName.toLowerCase().equals("actionbar")) {
				globalGroup = ag;
				if (globalGroup.getActionList() == null)
					globalGroup.setActionList(new ArrayList<>());
				actionGroup.getActionList().remove(ag);
				break;
			}
		
			i++;		
		}			
		if (globalGroup == null)
			globalGroup = new ActionGroup(null, new ArrayList<>());
						
		i = 0;
		while (i < actionGroup.getActionList().size()) {
		//for (ActionGroup ag : actionGroup.getActionList()) {
			ActionGroup ag = actionGroup.getActionList().get(i);
			//skip Item action for Empty actions
			if (ag == null) {
				i++;
				continue;
			}

			String actionName = ag.getActionName();
			if (actionName != null && actionName.contains("Not Known")) {
				i++;
				continue;
			}
			
			if (ag.getTags().contains("actionBarTopLevel")) {
				//actionGroup.getActionList().remove(ag);
				//actionGroup.getActionList().remove(i);
				listActionGroup.add(ag);
				i++;
			} else {
				globalGroup.getActionList().add(ag);				
				i++;
			}
		}		
		
		//if (actionGroup.getActionList().size() > 0) {
    	if (globalGroup.getActionList().size() > 0) {
    		globalGroup.getTags().add("actionBarContentMenu");
			listActionGroup.add(globalGroup);
			globalGroup.setComplete(true);
		}
		
		return listActionGroup;
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
	
	public void configureActionGroupForView(ActionGroup actionGroup, GmContentView view ) {
        List<ActionGroup> listActionGroups = prepareMenuAsActionGroups(actionGroup);		
		
		this.clearSelectionViewElements();
		this.selectionViewElementMenuMap.clear();
		this.selectionViewActionElementMap.clear();
        
        for (ActionGroup ag : listActionGroups) {        	
        	Menu menu = null;
        	if (ag.getActionList() != null && !ag.getActionList().isEmpty()) {
    			//menu = this.currentActionManager.getActionMenuBuilder().getContextMenu(view, externalComponents, ag, filterExternal);
        		Widget widget = this.currentActionManager.getActionMenuByGroup(view, ag);
        		if (widget instanceof Menu)
    			    menu = (Menu) widget; 
        	}
        	
        	//set the global content Menu into ContentMenuAction - set also Icon and Description
			if (ag.getTags().contains("actionBarContentMenu") && (contentMenuAction != null) && (contentMenuAction instanceof ContentMenuAction)) {
				VerticalTabElement elementContentMenu = staticActionElementMap.get(contentMenuAction);
				if (elementContentMenu == null)
					elementContentMenu = selectionViewActionElementMap.get(contentMenuAction);
				if (elementContentMenu != null) {
					if (ag.getIcon() != null) {
						elementContentMenu.setIcon(getImageResourceFromIcon(ag.getIcon()));
						elementContentMenu.setDenyIconUpdate(true);
					}
					if (ag.getDisplayName() != null) {
						elementContentMenu.setDescription(ag.getDisplayName());
						elementContentMenu.setDenyDescriptionUpdate(true);
					}
				}

				if (menu != null) {
					if (elementContentMenu != null) 
						selectionViewElementMenuMap.put(elementContentMenu,  menu);
					((ContentMenuAction) contentMenuAction).setMenu(menu);
					
				}
        		continue;
        	}
        	
        	//create VerticalTabElements
    		ImageResource icon = getImageResourceFromIcon(ag.getIcon());
    		//String name = ag.getName();
    		String name = getActionName(ag);
    		String toolTip = ag.getDisplayName();
    	    		
    		Widget widget = menu;
        	Action modelAction = ag.getAction();
        	if (modelAction != null) {
        		ImageResource icon1 = modelAction.getIcon();
        		ImageResource icon2 = modelAction.getHoverIcon();

        		if (icon1 != null) {
        			if (icon2 != null && icon2.getWidth() > icon1.getWidth())
        				icon = icon2;
        			else
        				icon = icon1;
        		} else if (icon2 != null)
        			icon = icon2;
        		
        		if (modelAction.getTooltip() != null && !modelAction.getTooltip().isEmpty())
        			toolTip = modelAction.getTooltip();
        		
        		modelAction.removePropertyListener(actionPropertyListener);
        		modelAction.addPropertyListener(actionPropertyListener);       		
        	}
        	
    		if (toolTip == null || toolTip.isEmpty())
    			toolTip = name;
    		
    		name = name.replaceAll("\\s", "");
    		name = name.replaceAll(" ", "");    		
    		
			List<Pair<String, ? extends Widget>> externalComponents = this.currentActionManager.getExternalComponentsForView(view);
			if (externalComponents != null) 
				for (Pair<String, ? extends Widget> pair : externalComponents) {
					String findName = ag.getActionName();					
					if (findName != null && (findName.equals(pair.first) || findName.equals('$' + pair.first()))) {
						widget = pair.getSecond();
						    						
			    		if (icon == null && widget instanceof MenuItem) 
		    				icon = ((MenuItem) widget).getIcon();
						break;
					}
				}
    		
    		if (icon == null)
    			continue;
    		
    		VerticalTabElement element = new VerticalTabElement(name, "", toolTip, icon, modelAction, true, null, widget);
    		
    		if (this.showingOrderFromTop)
    			selectionViewTabElements.add(element);  //add to end
    		else	
    			selectionViewTabElements.add(0, element);   //add to start
    		    		
    		if (modelAction != null)
    			selectionViewActionElementMap.put(modelAction, element);
    		
    		if (menu != null)
    			selectionViewElementMenuMap.put(element,  menu);
        }
        
        prepareElementsOrder();
		updateElements();				
		this.refresh();                
	}

	public void setShowingOrderFromTop(boolean showingOrderFromTop) {
		this.showingOrderFromTop = showingOrderFromTop;
	}
	

	private void clearSelectionViewElements() {
		for (VerticalTabElement element : selectionViewTabElements)
			removeVerticalTabElement(element);
		selectionViewTabElements.clear();
	}
	
	private void clearStaticElements() {
		for (VerticalTabElement element : staticTabElements)
			removeVerticalTabElement(element);
		staticTabElements.clear();
	}
	
	public void setStaticActionGroup(List<Action> actionsList) {
		List<Action> actionList = new ArrayList<>();
		if (actionsList != null)
			actionList.addAll(actionsList);
		
		clearStaticElements();
		this.staticElementMenuMap.clear();
		this.staticActionElementMap.clear();
			
    	if (actionList.isEmpty())
			return;
    	
    	//add separator between dynamic and static actions
        if (this.useSeparatorForDynamic) {
           Action separatorAction = getDynamicSeparatorAction();
           if (separatorAction != null && !actionList.contains(separatorAction)) {
        	   if (staticElementsFromTop)         	   
        		   actionList.add(separatorAction);
        	   else 	   
        		   actionList.add(0, separatorAction);
           }
        }    	
    	
//        if (this.useContentMenuAction) {
        	Action contentMenuAction = getContentMenuAction();
        	if (contentMenuAction != null && !actionList.contains(contentMenuAction)) {
         	   if (staticElementsFromTop)         	   
        		   actionList.add(contentMenuAction);
        	   else 	   
        		   actionList.add(0, contentMenuAction);        		
        	}
//        }
        
		for (int i = 0; i < actionList.size(); i++) {
			Action action = actionList.get(i);
			if (action == null)
				continue;
			
			action.removePropertyListener(actionPropertyListener);
			action.addPropertyListener(actionPropertyListener);
			
			addStaticActionAndUpdate(action);
		}
				
		prepareElementsOrder();
		updateElements();				
		this.refresh();
	}
		
	//on Selection Change update Visible Items - update main vertical elements, update also menu items	
	@Override
	public void onSelectionChanged(GmSelectionSupport gmSelectionSupport) {
		GmContentView selectionView = gmSelectionSupport.getView();
				
		if (selectionView == null)
			return;
		
		if (this.currentActionGroup != null) {						
			//ActionGroup actionGroup =  this.currentActionManager.getActionGroup(selectionView);
			
			List<List<ModelPath>> modelPaths;
			if (gmSelectionSupport instanceof GmAmbiguousSelectionSupport)
				modelPaths = ((GmAmbiguousSelectionSupport) gmSelectionSupport).getAmbiguousSelection();
			else
				modelPaths = gmSelectionSupport.transformSelection(gmSelectionSupport.getCurrentSelection());
			
			handleSelectionChanged(this.currentActionGroup, modelPaths);
			updateElements();				
			this.refresh();        		
		}		
	}

	//get ActionManager and actions from current GmContentView
	private void prepareActionManager(GmContentView view) {
		currentActionManager  = null;
		currentActionGroup = null;
		
		if (!(view instanceof GmContentViewActionManagerHandler))
			return;
		
		currentActionManager = ((GmContentViewActionManagerHandler) view).getGmContentViewActionManager();
		//this.currentActionManager.resetActions(selectionView);
		List<Pair<ActionTypeAndName, ModelAction>> externalActions = null;
		if (view instanceof GmActionSupport) 
			externalActions = ((GmActionSupport) view).getExternalActions();
		
		if (this.currentActionManager != null) {
			currentActionGroup =  currentActionManager.getActionGroup(view);					
			configureActionGroupForView(currentActionGroup, view);
			
			if (externalActions != null) 
				currentActionManager.addExternalActions(view, externalActions);
		}
	}
	
	private void handleSelectionChanged(ActionGroup actionGroup, List<List<ModelPath>> modelPaths) {
		if (actionGroup == null)
			return;
		
		if (actionGroup.getAction() instanceof ModelAction)
			((ModelAction) actionGroup.getAction()).updateState(modelPaths);
	
		if (actionGroup.getActionList() != null && !actionGroup.getActionList().isEmpty())
			actionGroup.getActionList().forEach(ag -> handleSelectionChanged(ag, modelPaths));
	}
	
	private void addStaticActionAndUpdate(Action action) {
		String name = action.getName() != null ? action.getName() : "&nbsp;";

		String toolTip = action.getTooltip();
		if (toolTip == null || toolTip.isEmpty())
			toolTip = name;
		if (name == null || name.isEmpty())
			name = action.getStyleName();
		
		name = name.replaceAll("\\s", "");
		name = name.replaceAll(" ", "");
		ImageResource icon = action.getHoverIcon();
		if (icon == null)
			icon = action.getIcon();
		
		VerticalTabElement element = new VerticalTabElement(name, "", toolTip, action.get("actionSubMenu"), icon, action, true, null);
				
		if (this.showingOrderFromTop)
			staticTabElements.add(element);  //add to end
		else	
			staticTabElements.add(0, element);   //add to start
		
		staticActionElementMap.put(action, element);		
	}
	
	private void prepareElementsOrder() {
		clearElements();
		
		List<VerticalTabElement> allTabElements = new ArrayList<>(); 
		if (staticElementsFromTop) {	
			allTabElements.addAll(staticTabElements);
			allTabElements.addAll(selectionViewTabElements);
		} else {
			allTabElements.addAll(selectionViewTabElements);
			allTabElements.addAll(staticTabElements);			
		}
					
		for (VerticalTabElement element : allTabElements)
			insertVerticalTabElement(element, -1);
	}
	
	//update Visibility of VerticalTabElements depending on associated Action visibility
	public void updateElements() {
		boolean anyDynamicElementIsVisible = false;
		VerticalTabElement dynamicSeparatorElement = null;
		
		for (VerticalTabElement element : this.getTabElements()) {
			Object object = element.getModelObject();

			if (dynamicActionSeparator != null && object != null && object.equals(dynamicActionSeparator))
				dynamicSeparatorElement = element;						
				
			updateElement(element);
			
			if (selectionViewTabElements.contains(element) && element.isSystemVisible())
				anyDynamicElementIsVisible = true;
		}
		
		if (dynamicSeparatorElement != null) {
			dynamicSeparatorElement.setSystemVisible(anyDynamicElementIsVisible);
		}
	}

	public void updateElement(VerticalTabElement element) {
		Object object = element.getModelObject();
		
		boolean subMenuVisible = true;
		Menu menu = selectionViewElementMenuMap.get(element);
		if (menu == null)
			menu = staticElementMenuMap.get(element);
		if (menu != null) {
			subMenuVisible = updateActionMenu(menu);
		}
		
		if (object instanceof Action) {
			Action action = (Action) object;
			if (object.equals(contentMenuAction)) {
				boolean sysVisible = subMenuVisible && useContentMenuAction;
				action.setHidden(!sysVisible);
				element.setSystemVisible(sysVisible);
			} else {				
				boolean sysVisible = (!action.getHidden()) && subMenuVisible;
				element.setSystemVisible(sysVisible);
			}
			
			if (!element.getDenyDescriptionUpdate())
				if (element.getDescription() == null || (!element.getDescription().equals(action.getTooltip()) && action.getTooltip() != null)) 
					element.setDescription(action.getTooltip());					
				
			if (!element.getDenyIconUpdate()) 
				if (element.getIcon() != action.getIcon() && action.getIcon() != null) 
					element.setIcon(action.getIcon());		
		} 
		
	}

	//update visibility of items inside the menu
	//return boolean - TRUE if one of the actions in menu is visible, FALSE if all action are hidden
	private boolean updateActionMenu(Menu menu) {
		boolean isItemVisible = false;
		for (Widget item : menu) {			
			boolean isHidden = true;
			if (item instanceof MenuItem) {								
				Action action = ((MenuItem) item).getData("action");
				if (action != null) {
					isHidden = action.getHidden();
					item.setVisible(!isHidden);										
				}
			}
			
			isItemVisible = (isItemVisible || !isHidden);
			if (isItemVisible)
				break;
		}		
		return isItemVisible;
	}

    private List<List<ModelPath>> transformSelection(List<ModelPath> selection) {
    	if (selection == null)
    		return null;
    	
		List<List<ModelPath>> list = newList();
		
		for (ModelPath modelPath : selection) {
			List<ModelPath> singleList = newList();
			singleList.add(modelPath);
			list.add(singleList);
		}
		
		return list;
	}	
	
	public void configureGmConentView(GmContentView contentView) {		
		if (showDynamicTabElements)
			prepareActionManager(contentView);		
		configureStaticActions(contentView);
		
		List<List<ModelPath>> modelPaths = null;
		if (contentView != null)
			modelPaths = transformSelection(contentView.getCurrentSelection());
		handleSelectionChanged(this.currentActionGroup, modelPaths);
		updateElements();				
		this.refresh();        		
		
	}

	private void configureStaticActions(GmContentView contentView) {
		for (Entry<Action, VerticalTabElement> entry : staticActionElementMap.entrySet()) {		
			if (entry.getKey()  instanceof ModelAction)  
				((ModelAction) entry.getKey()).configureGmContentView(contentView);				
		}
	}
	
	private Action getDynamicSeparatorAction() {
		if (this.dynamicActionSeparator != null)
			return  this.dynamicActionSeparator;
		
		if (separatorActionProvider == null)
			return null;
		
		this.dynamicActionSeparator = separatorActionProvider.get(); 
		return this.dynamicActionSeparator;
	}		
	
	private Action getContentMenuAction() {
		if (this.contentMenuAction != null)
			return  this.contentMenuAction;
		
		if (contentMenuActionProvider == null)
			return null;
		
		this.contentMenuAction = contentMenuActionProvider.get(); 
		return this.contentMenuAction;
	}		
	/*
	private ContentMenuAction getContentMenuAction() {
		for (VerticalTabElement element : staticTabElements) {
			Object object = element.getModelObject();
			if (object instanceof ContentMenuAction)
				return (ContentMenuAction) object;			
		}
		
		return null;
	}
	*/
	
	
}
