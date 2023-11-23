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
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.KnownProperties;
import com.braintribe.gwt.gme.constellation.client.action.GlobalAction;
import com.braintribe.gwt.gme.constellation.client.expert.GlobalActionsHandler;
import com.braintribe.gwt.gme.constellation.client.expert.GlobalActionsListener;
import com.braintribe.gwt.gme.constellation.client.expert.GlobalToolBarButtonAdapter;
import com.braintribe.gwt.gme.gmactionbar.client.DefaultGmViewActionBar;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabElement;
import com.braintribe.gwt.gmview.action.client.InstantiateEntityAction;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.FixedSplitButton;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.FixedSplitButtonCell;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.FixedTextButton;
import com.braintribe.gwt.gxt.gxtresources.whitebutton.client.SplitArrowUpWhiteButtonCellAppearance;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.reflection.EntityType;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.cell.core.client.SplitButtonCell;
import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.SplitButton;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class GlobalActionsToolBar extends ContentPanel implements InitializableBean, GlobalActionsListener, Supplier<InstantiateEntityAction> {
	//private static EntityType<GenericEntity> DEFAULT_ENTITY_TYPE = GenericEntity.T;
	
	private Map<String, ImageResource> mapDefaultDualSectionImageResourceIcon = new FastMap<>();
	private Map<String, ImageResource> mapDefaultDualSectionImageResourceDualIcon = new FastMap<>();
	private Map<String, String> mapDefaultDualSectionDescription = new FastMap<>();
	private List<DualSectionButton> externalDualSectionButtons;
	private InstantiateEntityAction contextInstantiateEntityAction;
	private ExplorerConstellation explorerConstellation;
	private SplitButton newSplitButton;
	private Menu newSplitMenu;
	private List<SplitButton> splitButtons;
	private ToolBar toolBar = null;
	//private MenuItem instantiationMenuItem;
	private DefaultGmViewActionBar parentView;
	private ButtonScale buttonScale = ButtonScale.LARGE;
	private IconAlign iconAlign = IconAlign.TOP;
	private boolean useButtonText = true;
	private GlobalActionsHandler globalActionsHandler;
	private Map<Action, ImageResource> mapImageResourceIcon = new HashMap<>();
	private Map<Action, ImageResource> mapImageResourceHoverIcon = new HashMap<>();
	private Map<Action, String> mapDescription = new HashMap<>();	
	
	public GlobalActionsToolBar() {
		this.setBodyBorder(false);
		this.setBorders(false);
		this.setHeaderVisible(false);
	}
	
	@Override
	public void intializeBean() throws Exception {		
		add(initializeToolBar());
		
		this.addAttachHandler(event -> {
			if (!event.isAttached())
				return;
			
			globalActionsHandler.prepareListeners();
			//addExplorerConstellationVerticalTabPanelListener();
			
			Scheduler.get().scheduleDeferred(() -> {
				if (newSplitButton != null)
					newSplitButton.setMenu(null);
				
				if (splitButtons != null)
					for (SplitButton splitButton : splitButtons)
						splitButton.setMenu(null);
			});
		});
	}
				
	/**
	 * Configures the required {@link GlobalActionsHandler}.
	 */
	@Required
	public void setGlobalActionsHandler(GlobalActionsHandler globalActionsHandler) {
		this.globalActionsHandler = globalActionsHandler;
		this.globalActionsHandler.addGlobalActionListener(this);
		this.globalActionsHandler.setDestinationPanelForWorkbenchAction(this);
	}	
		
	public void setExternalDualSectionButtons(List<DualSectionButton> externalDualSectionButtons) {
		this.externalDualSectionButtons = externalDualSectionButtons;
		
		if (this.externalDualSectionButtons != null) {
			for (DualSectionButton dualSectionButton : this.externalDualSectionButtons) {
				mapDefaultDualSectionDescription.put(dualSectionButton.getName(), dualSectionButton.getDescription());
				mapDefaultDualSectionImageResourceIcon.put(dualSectionButton.getName(), dualSectionButton.getIcon());
				mapDefaultDualSectionImageResourceDualIcon.put(dualSectionButton.getName(), dualSectionButton.getDualIcon());
			}
		}
	}
		
	/**
	 * Configures the {@link ExplorerConstellation}.
	 */
	@Configurable
	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
	}
			
	/**
	 * Configures the Size of the buttons at toolbar - {@link ButtonScale} 
	 */
	@Configurable
	public void setButtonScale(ButtonScale buttonScale) {
		this.buttonScale = buttonScale;
	}		

	/**
	 * Configures the Position of the Icon at the button - {@link IconAlign} 
	 */
	@Configurable
	public void setIconAlign(IconAlign iconAlign) {
		this.iconAlign = iconAlign;
	}			
	
	/**
	 * Configures if use button text
	 */
	@Configurable
	public void setUseButtonText(boolean useButtonText) {
		this.useButtonText = useButtonText;
	}			
			
	/*
	@Override
	public Future<Void> apply(Folder folder) throws RuntimeException {
		this.rootFolder = folder;
		folderInitialized = true;
		//RVE prepare Actions from configured workbench Folder ..set Icons + Descriptions
		prepareToolBar();
		return new Future<>(null);
	}
	*/
	
	@Override
	public InstantiateEntityAction get() {
		return contextInstantiateEntityAction;
	}
	
	public void configureParentView(DefaultGmViewActionBar parentView) {
		this.parentView = parentView;
	}
	
	/*
	public void updateVisibility() {
		if (globalActionsHandler != null)
			globalActionsHandler.updateState();
	}
	*/
	
	/**
	 * Returns the {@link EntityType} associated with the given view.
	 */
	public EntityType<?> getViewContextEntityType(GmContentView view) {
		return globalActionsHandler.getViewContextEntityType(view);
	}

	/*
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
		
		contextInstantiateEntityAction.configureGmContentView(lastSelectedView);
		
		if (entityType == null)
			entityType = GlobalActionsHandler.DEFAULT_ENTITY_TYPE;
		
		if (entityType != currentEntityType)
			setCurrentEntityType(entityType);
	}
	*/
	
	private void setCurrentEntityType(/*EntityType<?> entityType*/) {
		//currentEntityType = entityType;
		//globalActionsHandler.setCurrentEntityType(entityType);
		//contextInstantiateEntityAction.configureEntityType(currentEntityType);
		handleNewSplitMenuVisibility();
	}
	
	/*
	private void configurePossibleBrowsingConstellationListener() {
		BrowsingConstellation browsingConstellation = getParentBrowsingConsetllation((Widget) lastSelectedView);
		if (browsingConstellation != lastBrowsingConstellation) {
			if (lastBrowsingConstellation != null)
				lastBrowsingConstellation.getTetherBar().removeTetherBarListener(getTetherBarListener());
			
			lastBrowsingConstellation = browsingConstellation;
			
			if (lastBrowsingConstellation != null)
				lastBrowsingConstellation.getTetherBar().addTetherBarListener(getTetherBarListener());
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
				EntityType<?> entityType = viewEntityTypes.computeIfAbsent(finalView, GlobalActionsToolBar.this::getContextEntityPath);
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
	*/

	private void handleNewSplitMenuVisibility() {
		if (contextInstantiateEntityAction.getHidden() || contextInstantiateEntityAction.getEntityType() == GlobalActionsHandler.DEFAULT_ENTITY_TYPE) {
			if (newSplitButton != null) {
				Action instantiateTransientEntityAction = globalActionsHandler.getInstantiateTransientEntityAction(); 
				if (instantiateTransientEntityAction != null && !instantiateTransientEntityAction.getEnabled())
					newSplitButton.setMenu(null);
				//else
					//instantiationMenuItem.setVisible(false);
			}
		} else {
			if (newSplitButton != null) {
				//instantiationMenuItem.setVisible(true);
				newSplitButton.setMenu(newSplitMenu);
			}
		}
	}
	
	/*
	private void handleVerticalTabElementsRemoved(List<VerticalTabElement> verticalTabElements) {
		for (VerticalTabElement verticalTabElement : verticalTabElements) {
			Widget widget = verticalTabElement.getWidget();
			if (!(widget instanceof GmContentView))
				continue;
			
			viewEntityTypes.remove(widget);
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
						contextInstantiateEntityAction.configureGmContentView(contentView);
						setCurrentEntityType(entityType);
					}
				});
			}
		}
	}
	*/
	
	private ToolBar initializeToolBar() {
		toolBar = new ToolBar();		
		//toolBar.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
		toolBar.setStyleName("globalActionToolBarBase");
		toolBar.setBorders(false);
		//RVE - for OverFlow menuItems need show small Icons
		toolBar.addOverflowHandler(event -> {
			for (int i = 0; i < event.getMenu().getWidgetCount(); i++) {
				Widget widget = event.getMenu().getWidget(i);
				if (widget == null || !(widget instanceof MenuItem))
					continue;
				
				MenuItem menuItem = (MenuItem) widget;
				ImageResource imageResource = menuItem.getIcon();
				if (mapImageResourceIcon.containsValue(imageResource)) {
					mapImageResourceIcon.keySet().stream().filter(a -> mapImageResourceIcon.get(a).equals(imageResource)).forEach(action -> {
						ImageResource icon = mapImageResourceHoverIcon.get(action);
						if (icon != null)
							menuItem.setIcon(icon);
					});
				}
			}
		});
		return toolBar;
	}
	
	private void prepareToolBar() {
		//RVE prepare Actions from configured workbench Folder ..set Icons + Descriptions
		mapImageResourceIcon.clear();
		mapImageResourceHoverIcon.clear();
		mapDescription.clear();
		
		List<GlobalAction> orderGlobalActionList = globalActionsHandler.getConfiguredActions(false); 
		List<Action> externalToolBarActions = globalActionsHandler.getExternalToolBarActions();
		List<Action> modelActions = globalActionsHandler.getModelActions();

		if (toolBar != null) {
			toolBar.clear();
			//toolBar.add(new FillToolItem());
			
			for (GlobalAction globalAction : orderGlobalActionList) {				
				Action action = globalAction.getAction();
				if (globalAction.getDescription() != null)
					mapDescription.put(action, globalAction.getDescription());
				if (globalAction.getIcon() != null)
					mapImageResourceIcon.put(action, globalAction.getIcon());
				if (globalAction.getHoverIcon() != null)
					mapImageResourceHoverIcon.put(action, globalAction.getHoverIcon());					
				
				if (action.equals(globalActionsHandler.getContextInstantiateEntityAction())) {
					try {
						toolBar.add(createInstantiateEntityActionButton());
						continue;
					} catch (RuntimeException e) { //TODO: Please comment when adding something like this! Why is this?
						e.printStackTrace();
					}
				}
	
				if (action instanceof RedoAction || action instanceof UndoAction || action instanceof SaveAction) {
					toolBar.add(createSplitButton(action));
					continue;
				}
				
				if (externalToolBarActions != null) {
					boolean buttonCreated = false;
					for (Action externalAction : externalToolBarActions) {
						if (action.equals(externalAction)) {
							toolBar.add(createButton(externalAction));
							buttonCreated = true;
							break;
						}
					}
					
					if (buttonCreated)
						continue;
				}
				
				if (externalDualSectionButtons != null) {
					boolean buttonCreated = false;
					for (DualSectionButton dualSectionButton : externalDualSectionButtons) {
						//set default values
						String name = dualSectionButton.getName();
						if (mapDefaultDualSectionDescription.containsKey(name))					
							dualSectionButton.setDescription(mapDefaultDualSectionDescription.get(name));
						if (mapDefaultDualSectionImageResourceIcon.containsKey(name))					
							dualSectionButton.setIcon(mapDefaultDualSectionImageResourceIcon.get(name));
						if (mapDefaultDualSectionImageResourceDualIcon.containsKey(name))					
							dualSectionButton.setDualIcon(mapDefaultDualSectionImageResourceDualIcon.get(name));
						
						
						dualSectionButton.setIconAlign(iconAlign);
						dualSectionButton.setScale(buttonScale);
						dualSectionButton.setUseButtonText(useButtonText);
						if (globalAction.getKnownName().equals(dualSectionButton.getName())) {
							if (mapImageResourceIcon.containsKey(action)) {
								ImageResource icon = mapImageResourceIcon.get(action);
								if (icon != null)
									dualSectionButton.setIcon(icon);
							}
							if (mapDescription.containsKey(action)) 
								dualSectionButton.setDescription(mapDescription.get(action));
							
							dualSectionButton.addStyleName("globalActionBarDualButton");
							
							toolBar.add(dualSectionButton);
							buttonCreated = true;
							break;
						}
					}
					
					if (buttonCreated)
						continue;
				}
				
				if (modelActions != null && modelActions.contains(action))
					modelActions.stream().filter(a -> action.equals(a)).forEach(modelAction -> toolBar.add(createButton(modelAction)));
			}
		}
		
		//This needs to be deferred because if not, we end up with troubles when first displaying those actions
		Scheduler.get().scheduleDeferred(this::handleTransientActionsVisibility);
	}
	
		
	private TextButton createButton(Action action) {
		FixedTextButton button = new FixedTextButton();
		button.addStyleName("globalActionBarTextButton");
		button.setScale(buttonScale);
		button.setIconAlign(iconAlign);
				
		if (useButtonText)
			GlobalToolBarButtonAdapter.linkActionToButton(false, action, button);
		else
			GlobalToolBarButtonAdapter.linkActionToButton(action, button, true);			

		if (mapImageResourceIcon.containsKey(action)) {
			ImageResource icon = mapImageResourceIcon.get(action);
			if (icon != null)
			   button.setIcon(icon);
		}
		if (mapDescription.containsKey(action)) 
			if (useButtonText)
				button.setText(mapDescription.get(action));
		
		return button;
	}
	
	private SplitButton createSplitButton(final Action action) {
		final FixedSplitButton splitButton = new FixedSplitButton(new FixedSplitButtonCell(new SplitArrowUpWhiteButtonCellAppearance<>()));
		splitButton.setScale(buttonScale);
		splitButton.setIconAlign(iconAlign);
		splitButton.addStyleName("globalActionBarFixedSplitButton");
		
		if (useButtonText)
			GlobalToolBarButtonAdapter.linkActionToButton(false, action, splitButton);
		else
			GlobalToolBarButtonAdapter.linkActionToButton(action, splitButton, true);			
		
		if (mapImageResourceIcon.containsKey(action)) {
			ImageResource icon = mapImageResourceIcon.get(action);
			if (icon != null)
			   splitButton.setIcon(icon);
		}
		if (mapDescription.containsKey(action)) 
			if (useButtonText)			
				splitButton.setText(mapDescription.get(action));
					
		final Menu splitMenu = globalActionsHandler.getActionMenu(action);
		/*
		final Menu splitMenu = new Menu();
		
		MenuItem item;
		if (action instanceof SaveAction) {
			item = new MenuItem();
			MenuItemActionAdapter.linkActionToMenuItem(advancedSaveAction, item);
		} else {
			item = new MenuItem(LocalizedText.INSTANCE.performAll(action.getName()), action.getHoverIcon());
			item.addSelectionHandler(event -> {
				if (action instanceof RedoAction)
					((RedoAction) action).redoAllManipulations();
				else if (action instanceof UndoAction)
					((UndoAction) action).undoAllManipulations();
			});
		}
		*/
		
		if (action instanceof RedoAction) {
			((RedoAction) action).addRedoActionListener(manipulationsToRedo -> {
				if (manipulationsToRedo > 1)
					splitButton.setMenu(splitMenu);
				else
					splitButton.setMenu(null);
				
				handleTransientActionsVisibility();
			});
		} else if (action instanceof UndoAction) {
			((UndoAction) action).addUndoActionListener(manipulationsToUndo -> {
				if (manipulationsToUndo > 1)
					splitButton.setMenu(splitMenu);
				else
					splitButton.setMenu(null);
				
				handleTransientActionsVisibility();
			});
		} else if (action instanceof SaveAction) {
			globalActionsHandler.getAdvancedSaveAction().addPropertyListener((source, property) -> {
				if (KnownProperties.PROPERTY_ENABLED.equals(property)) {
					if (globalActionsHandler.getAdvancedSaveAction().getEnabled())
						splitButton.setMenu(splitMenu);
					else 
						splitButton.setMenu(null);
				}
			});
		}
		
		//splitMenu.add(item);
		splitButton.setMenu(splitMenu);
		
		if (splitButtons == null)
			splitButtons = new ArrayList<>();
		splitButtons.add(splitButton);
		
		return splitButton;
	}

	private void handleTransientActionsVisibility() {
		UndoAction transientUndoAction = globalActionsHandler.getTransientUndoAction();
		RedoAction transientRedoAction = globalActionsHandler.getTransientRedoAction();
		
		if (transientUndoAction.getEnabled()) {
			if (parentView != null)
				parentView.getGlobalLayoutData().setSize(DefaultGmViewActionBar.GLOBAL_ACTION_TRANSIENT_WIDTH);
			transientRedoAction.setHidden(false);
			transientUndoAction.setHidden(false);
			if (parentView != null)
				parentView.layout();
		} else {
			transientRedoAction.setHidden(true);
			transientUndoAction.setHidden(true);
			if (parentView != null) {
				parentView.getGlobalLayoutData().setSize(DefaultGmViewActionBar.GLOBAL_ACTION_WIDTH);
				parentView.layout();
			}
		}
		//setting the size and calling layout is needed due to a bug in GXT toolBar with fill
	}
	
	private TextButton createInstantiateEntityActionButton() {
		newSplitButton = new SplitButton(new SplitButtonCell(new SplitArrowUpWhiteButtonCellAppearance<String>()));
		newSplitButton.setScale(buttonScale);
		newSplitButton.setIconAlign(iconAlign);
		newSplitButton.addStyleName("globalActionBarSplitButton");
		
		//defaultInstantiateEntityAction = instantiateEntityActionProvider.provide();
		//configureInstantiateEntityAction(defaultInstantiateEntityAction);
		//List<Action> actionList = new ArrayList<Action>();

		InstantiateEntityAction defaultInstantiateEntityAction = globalActionsHandler.getDefaultInstantiateEntityAction();
		//InstantiateTransientEntityAction instantiateTransientEntityAction = globalActionsHandler.getInstantiateTransientEntityAction();
		contextInstantiateEntityAction = globalActionsHandler.getContextInstantiateEntityAction();
				
		/*
		contextInstantiateEntityAction =  globalActionsHandler.getInstantiateEntityActionProvider().get();
		contextInstantiateEntityAction.setDisplayEntityNameInAction(true);
		globalActionsHandler.configureInstantiateEntityAction(contextInstantiateEntityAction);
		contextInstantiateEntityAction.configureEntityType(DEFAULT_ENTITY_TYPE);
		*/
		
		if (useButtonText)
			if (mapDescription.containsKey(defaultInstantiateEntityAction))
				newSplitButton.setText(mapDescription.get(defaultInstantiateEntityAction));
			else
				newSplitButton.setText(defaultInstantiateEntityAction.getName());
		
		if (mapImageResourceIcon.containsKey(contextInstantiateEntityAction)) {
			newSplitButton.setIcon(mapImageResourceIcon.get(contextInstantiateEntityAction));
		}
		else {
			newSplitButton.setIcon(defaultInstantiateEntityAction.getIcon());
		}
		
		newSplitMenu = globalActionsHandler.getActionMenu(contextInstantiateEntityAction);
		/*
		newSplitMenu = new Menu();
		
		instantiationMenuItem = new MenuItem(defaultInstantiateEntityAction.getName() + "...");
		ImageResource icon = mapImageResourceHoverIcon.get(defaultInstantiateEntityAction);
		if (icon == null)
			icon = defaultInstantiateEntityAction.getHoverIcon();
		instantiationMenuItem.setIcon(icon);
		instantiationMenuItem.addSelectionHandler(event -> performDefaultInstantiateEntityAction(defaultInstantiateEntityAction));
		newSplitMenu.add(instantiationMenuItem);
		
		instantiateTransientEntityAction.setName(instantiateTransientEntityAction.getName() + "...");
		MenuItem transientItem = new MenuItem();
		icon = mapImageResourceHoverIcon.get(instantiateTransientEntityAction);
		if (icon == null)
			icon = instantiateTransientEntityAction.getHoverIcon();
		transientItem.setIcon(icon);
		MenuItemActionAdapter.linkActionToMenuItem(true, instantiateTransientEntityAction, transientItem);
		newSplitMenu.add(transientItem);
		*/
		
		newSplitButton.setMenu(newSplitMenu);
		
		newSplitButton.addSelectHandler(event -> {
			if (contextInstantiateEntityAction.getHidden()) {
				globalActionsHandler.performDefaultInstantiateEntityAction();
			} else {
				VerticalTabElement selectedElement = explorerConstellation.getVerticalTabPanel().getSelectedElement();
				if (selectedElement != null) {
					Widget selectedWidget = selectedElement.getWidget();
					if (selectedWidget instanceof GmContentView)
						contextInstantiateEntityAction.configureGmContentView(getFinalView((GmContentView) selectedWidget));
				}
				
				contextInstantiateEntityAction.perform(null);
			}
		});
		
		return newSplitButton;
	}
		
	/*private void performInstantiateTransientEntityAction() {
		if (instantiateTransientEntityAction.getEntityType() == null)
			instantiateTransientEntityAction.configureEntityType(DEFAULT_ENTITY_TYPE);
		instantiateTransientEntityAction.perform(null);
	}*/

	/*
	private EntityType<?> getContextEntityType(Object modelObject) {
		if (!(modelObject instanceof Template) && !(modelObject instanceof Query))
			return null;
		
		Query query = (Query) (modelObject instanceof Template ? ((Template) modelObject).getPrototype() : modelObject);
		String entityTypeSignature = null;
		if (query instanceof EntityQuery)
			entityTypeSignature = ((EntityQuery) query).getEntityTypeSignature();
		else if (query instanceof SelectQuery)
			entityTypeSignature = GMEUtil.getSingleEntityTypeSignatureFromSelectQuery((SelectQuery) query);
		
		if (entityTypeSignature != null)
			return GMF.getTypeReflection().findEntityType(entityTypeSignature);
		
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
	*/
	
	private GmContentView getFinalView(GmContentView parentView) {
		if (parentView instanceof BrowsingConstellation)
			return getFinalView(((BrowsingConstellation) parentView).getCurrentContentView());
		
		if (parentView instanceof QueryConstellation)
			return getFinalView(((QueryConstellation) parentView).getView());
		
		if (parentView instanceof MasterDetailConstellation)
			return getFinalView(((MasterDetailConstellation) parentView).getCurrentMasterView());
		
		return parentView;
	}
	
	/*
	private BrowsingConstellation getBrowsingConstellation(Widget widget) {
		if (widget instanceof BrowsingConstellation)
			return (BrowsingConstellation) widget;
		
		if (widget == null)
			return null;
		
		return getBrowsingConstellation(widget.getParent());
	}
	*/

	@Override
	public void onGlobalActionsPrepared() {
		prepareToolBar();
	}

	@Override
	public void onEntityTypeChanged(EntityType<?> entityType) {
		setCurrentEntityType(/*entityType*/);
	}

	
}
