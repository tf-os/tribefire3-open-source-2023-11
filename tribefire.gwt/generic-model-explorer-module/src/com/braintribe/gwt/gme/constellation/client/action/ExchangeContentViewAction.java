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
package com.braintribe.gwt.gme.constellation.client.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.action.client.TriggerKnownProperties;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.assemblypanel.client.AssemblyPanel;
import com.braintribe.gwt.gme.assemblypanel.client.action.ExchangeAssemblyPanelDisplayModeAction;
import com.braintribe.gwt.gme.assemblypanel.client.action.ExchangeAssemblyPanelDisplayModeAction.DisplayMode;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gme.constellation.client.MasterDetailConstellation;
import com.braintribe.gwt.gme.constellation.client.QueryConstellation;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationCss;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gmview.action.client.ActionMenuProvider;
import com.braintribe.gwt.gmview.action.client.ActionWithoutContext;
import com.braintribe.gwt.gmview.action.client.KnownActions;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionBar;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionProvider;
import com.braintribe.gwt.gmview.client.DoubleStateAction;
import com.braintribe.gwt.gmview.client.GmCondensationView;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewContext;
import com.braintribe.gwt.gmview.client.GmListView;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.client.ViewSituationResolver;
import com.braintribe.gwt.htmlpanel.client.HtmlPanel;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style.Anchor;
import com.sencha.gxt.core.client.Style.AnchorAlignment;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.menu.CheckMenuItem;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.menu.SeparatorMenuItem;
import com.sencha.gxt.widget.core.client.tree.Tree.CheckState;

/**
 * Action responsible for displaying all available views, plus view and condensation options.
 * @author michel.docouto
 *
 */
@SuppressWarnings("unusable-by-js")
public class ExchangeContentViewAction extends ModelAction implements ActionMenuProvider, ActionWithoutContext, DoubleStateAction {
	
	private MasterDetailConstellation masterDetailConstellation;
	private List<GmContentViewContext> externalContentViewContexts;
	private Supplier<ViewSituationResolver<GmContentViewContext>> viewSituationResolverSupplier;
	private int currentIndex = 0;
	private ModelPath currentModelPath;
	private Supplier<GmViewActionBar> gmViewActionBarSupplier;
	private List<Menu> menusToUpdate = new ArrayList<>();
	private static int idCounter = 0;
	private static final int ELEMENT_HEIGHT = 57;
	private static final int ELEMENT_WIDTH = 74;
	private ExchangeAssemblyPanelDisplayModeAction exchangeAssemblyPanelDisplayModeAction;
	private HtmlPanel viewHtmlPanel;
	private CheckMenuItem simpleItemContextMenu;
	private CheckMenuItem detailedItemContextMenu;
	private CheckMenuItem flatItemContextMenu;
	private List<Item> optionsMenuItems;
	private Map<GmCondensationView, CondensateEntityAction> condensateEntityActions;
	private List<Item> condensationMenuItems;
	private Element toolTipElement;
	private String preferredUseCase;
	private boolean forcePreferredUseCase;
	private VerticalLayoutContainer condensationsContainer;
	private Window menuWindow;
	private Boolean useCondensation = false;
	private Boolean useOptions = false;
	private String condensationTag = "$condensation";
	private String optionTag = "$option";
	private boolean useAsMenu = false;
	private ImageResource iconDefault;
	private ImageResource iconDynamic;
	private String descriptionDefault;
	private String descriptionDynamic;
	private boolean useIconAndDescriptionDefault = true;
	
	public ExchangeContentViewAction() {
		setHidden(false);
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar));
		setName(KnownActions.EXCHANGE_CONTENT_VIEW.getName());
 	    iconDefault = ConstellationResources.INSTANCE.view64();
        descriptionDefault = LocalizedText.INSTANCE.view();
		setIconsAndTooltip(true, null);
		//setIcon(ConstellationResources.INSTANCE.view64());
		//setHoverIcon(ConstellationResources.INSTANCE.view64());
	}
	
	/**
	 * Configures the required GmViewActionBar used for action navigation.
	 */
	@Required
	public void setGmViewActionBar(Supplier<GmViewActionBar> gmViewActionBarSupplier) {
		this.gmViewActionBarSupplier = gmViewActionBarSupplier;
	}
	
	@Required
	public void setExternalContentViewContexts(List<GmContentViewContext> contentViewContexts) {
		this.externalContentViewContexts = contentViewContexts;
	}
	
	
	@Required
	public void setMasterDetailConstellation(MasterDetailConstellation masterDetailConstellation) {
		this.masterDetailConstellation = masterDetailConstellation;
	}	
	
	@Configurable
	public void setViewSituationResolver(Supplier<ViewSituationResolver<GmContentViewContext>> viewSituationResolverSupplier) {
		this.viewSituationResolverSupplier = viewSituationResolverSupplier;
	}
	
	@Configurable
	public void setUseAsMenu(boolean useAsMenu) {
		this.useAsMenu = useAsMenu;
	}	
	
	public void configureCurrentModelPath(ModelPath currentModelPath) {
		this.currentModelPath = currentModelPath;
	}
	
	public void configurePreferredUseCase(String preferredUseCase, boolean force) {
		this.preferredUseCase = preferredUseCase;
		this.forcePreferredUseCase = force;
	}
	
	/**
	 * Configures if use Options config at Menu - default is False
	 */
	@Configurable
	public void setUseOptions(boolean useOptions) {
		this.useOptions= useOptions;
	}	

	/**
	 * Configures if use Cendensation config at Menu - default is False
	 */
	@Configurable
	public void setUseCondensation(boolean useCondensation) {
		this.useCondensation= useCondensation;
	}	
	
	public String getOptionTag() {
		return this.optionTag;
	}

	public String getConensationTag() {
		return this.condensationTag;
	}	
	
	@Override
	public void configureGmContentView(GmContentView gmContentView) {		
		super.configureGmContentView(gmContentView);
		masterDetailConstellation = getMasterDetailConstellation(gmContentView);
		if (masterDetailConstellation == null) {
			setIconsAndTooltip(true, null);		
			setHidden(true);
			return;
		}
			
		List<GmContentViewContext> contentViewContexts = getContentViewContexts();
		GmContentViewContext context = masterDetailConstellation.getCurrentContentViewContext();
		if (context != null && contentViewContexts != null && contentViewContexts.contains(context))
			currentIndex = contentViewContexts.indexOf(context);

		setIconsAndTooltip(false, context);		
		setHidden(false);
	}
	
	@Override
	protected void updateVisibility() {		
		if (masterDetailConstellation == null) {
			setHidden(true);
			return;
		}
		
		GmContentView gmContentView = masterDetailConstellation.getCurrentMasterView();
		
		List<GmContentViewContext> contentViewContexts = getContentViewContexts();
		int contentViewContextsSize = contentViewContexts.size();
		CondensateEntityAction condensateEntityAction = null;
		if (gmContentView instanceof GmCondensationView)
			condensateEntityAction = condensateEntityActions == null ? null : condensateEntityActions.get((GmCondensationView) gmContentView);
		boolean hidden = isCurrentViewEmpty() || (contentViewContextsSize <= 1 && exchangeAssemblyPanelDisplayModeAction == null && condensateEntityAction == null);
		setHidden(hidden);
		
		if (hidden)
			return;
		
		if (viewHtmlPanel != null) {
			viewHtmlPanel.setVisible(contentViewContextsSize > 1);
			
			for (Menu menu : menusToUpdate) {
				for (int i = 0; i < menu.getWidgetCount(); i++) {
					Widget widget = menu.getWidget(i);
					if (!(widget instanceof CheckMenuItem))
						continue;
					
					String itemId = ((CheckMenuItem) widget).getItemId();
					if (itemId.equals(String.valueOf(currentIndex))) {
						((CheckMenuItem) widget).setChecked(true, true);
						toggleRadios(((CheckMenuItem) widget));
						break;
					}
				}
			}
		}
		
		if (optionsMenuItems != null) {
			boolean optionsVisible = gmContentView instanceof AssemblyPanel && ((AssemblyPanel) gmContentView).isUseExchangeAssemblyPanelDisplayModeAction();
			optionsMenuItems.forEach(item -> item.setVisible(optionsVisible));
		}
		
		if (condensationMenuItems != null) {
			boolean optionsVisible = gmContentView instanceof GmCondensationView && ((GmCondensationView) gmContentView).isUseCondensationActions();
			condensationMenuItems.forEach(item -> item.setVisible(optionsVisible));
		}
		
		if (condensateEntityAction != null) {
			condensateEntityAction.updateVisibility(((GmCondensationView) gmContentView).getEntityTypeForProperties());
			if (condensateEntityAction.getHidden() && condensationMenuItems != null)
				condensationMenuItems.forEach(item -> item.setVisible(false));
		}
	}
	
	@Override
	public void perform(TriggerInfo triggerInfo) {
		final Element clickedElement = triggerInfo.get(TriggerKnownProperties.PROPERTY_CLICKEDELEMENT);
		if (clickedElement == null)
			return;
		
		if (useAsMenu) {
			//Menu menu = getAsMenu();
			GmContentView gmContentView = masterDetailConstellation.getCurrentMasterView();
			Menu menu = getMenu(gmContentView);
			menu.show(clickedElement, new AnchorAlignment(Anchor.TOP_LEFT, Anchor.BOTTOM_LEFT), 0, 0);
		} else {			
			Window menuWindow = getMenuWindow();
			menuWindow.show();
			menuWindow.alignTo(clickedElement, new AnchorAlignment(Anchor.TOP_LEFT, Anchor.BOTTOM_LEFT), 0, 0);
		}
	}
	
	private List<GmContentViewContext> getContentViewContexts() {
		List<GmContentViewContext> contentViewContexts = new ArrayList<>();
		boolean preparingForListView = false;
		
		if (viewSituationResolverSupplier != null) {
			ModelPath contentPath = masterDetailConstellation.getContentPath();
			
			if (contentPath == null && currentModelPath == null && masterDetailConstellation.getCurrentMasterView() instanceof GmListView) {
				List<ModelPath> addedModelPaths = ((GmListView) masterDetailConstellation.getCurrentMasterView()).getAddedModelPaths();
				if (addedModelPaths != null && !addedModelPaths.isEmpty()) {
					GenericModelType lastType = addedModelPaths.get(0).last().getType();
					if (lastType.isCollection()) {
						CollectionType collectionType = (CollectionType) lastType;
						if (collectionType.getCollectionElementType().isEntity()) {
							contentPath = new ModelPath();
							contentPath.add(new RootPathElement(collectionType.getCollectionElementType(), null));
						}
					}
				}
				
				preparingForListView = true;
			}
			
			if (contentPath != null && contentPath.last() != null) {
				ViewSituationResolver<GmContentViewContext> viewSituationResolver = viewSituationResolverSupplier.get();
				contentViewContexts = viewSituationResolver.getPossibleContentViews(contentPath.last(), preparingForListView);
			} else if (currentModelPath != null) {
				ModelPathElement last = currentModelPath.last();
				if (last != null) {
					ViewSituationResolver<GmContentViewContext> viewSituationResolver = viewSituationResolverSupplier.get();
					contentViewContexts = viewSituationResolver.getPossibleContentViews(last);
				}
			}
		}
		
		if (forcePreferredUseCase) {
			if (preparingForListView)
				contentViewContexts.addAll(0, externalContentViewContexts);
			else
				contentViewContexts.addAll(externalContentViewContexts);
			
			GmContentViewContext preferredContext = null;
			for (GmContentViewContext contentViewContext : contentViewContexts) {
				if (preferredUseCase.equals(contentViewContext.getUseCase())) {
					preferredContext = contentViewContext;
					contentViewContexts.remove(contentViewContext);
					break;
				}
			}
			
			if (preferredContext != null)
				contentViewContexts.add(0, preferredContext);
		} else {
			List<GmContentViewContext> listToAdd;
			if (preferredUseCase == null)
				listToAdd = externalContentViewContexts;
			else {
				List<GmContentViewContext> arrangedExternalContentViewContexts = new ArrayList<>(externalContentViewContexts);
				externalContentViewContexts.stream().filter(context -> preferredUseCase.equals(context.getUseCase())).findFirst().ifPresent(context -> {
					arrangedExternalContentViewContexts.remove(context);
					arrangedExternalContentViewContexts.add(0, context);
				});
				
				listToAdd = arrangedExternalContentViewContexts;
			}
			
			if (preparingForListView)
				contentViewContexts.addAll(0, listToAdd);
			else
				contentViewContexts.addAll(listToAdd);
		}
		
		return contentViewContexts;
	}
	
	private boolean isCurrentViewEmpty() {
		if (masterDetailConstellation == null)
			return true;
		
		GmContentView currentMasterView = masterDetailConstellation.getCurrentMasterView();
		if (!(currentMasterView instanceof GmListView))
			return currentMasterView.getContentPath() == null;
		
		boolean addedModelPathsEmpty = false;
		List<ModelPath> addedModelPaths = ((GmListView) currentMasterView).getAddedModelPaths();
		if (addedModelPaths == null || addedModelPaths.isEmpty())
			addedModelPathsEmpty = true;
		else {
			ModelPath modelPath = addedModelPaths.get(0);
			if (modelPath == null)
				addedModelPathsEmpty = true;
			else {
				Object value = modelPath.last().getValue();
				if (value instanceof Collection)
					addedModelPathsEmpty = ((Collection<?>) value).isEmpty();
			}
		}
		
		return currentMasterView.getContentPath() == null && addedModelPathsEmpty;
	}
	
	public void adapt(GmContentViewContext gmContentViewContext) {
		configurePreferredUseCase(gmContentViewContext.getUseCase(), false);
		List<GmContentViewContext> contentViewContexts = getContentViewContexts();
		currentIndex = contentViewContexts.indexOf(gmContentViewContext);
	}
	
	public void adapt(String useCase) {
		configurePreferredUseCase(useCase, true);
		handleChangeView(getDefaultContentView());
	}
	
	public GmContentViewContext getCurrentContentView() {
		return getContentViewContexts().get(currentIndex);
	}
	
	public GmContentViewContext getDefaultContentView() {
		List<GmContentViewContext> contentViewContexts = getContentViewContexts();
		return contentViewContexts.isEmpty() ? null : contentViewContexts.get(0);
	}

	public GmContentViewContext provideGmContentViewContext(ModelPath modelPath) {
		if (modelPath == null)
			return getDefaultContentView();
		
		ViewSituationResolver<GmContentViewContext> viewSituationResolver = viewSituationResolverSupplier.get();
		
		ModelPathElement last = modelPath.last();
		List<GmContentViewContext> gmContentViewContexts = viewSituationResolver.getPossibleContentViews(last);
		if (gmContentViewContexts != null && !gmContentViewContexts.isEmpty())
			return viewSituationResolver.getPossibleContentViews(last).get(0);
		
		return getDefaultContentView();
	}
		
	@Override
	public Menu getMenu(GmContentView gmContentView) {
		Menu menu = new Menu();
		List<GmContentViewContext> contentViewContexts = getContentViewContexts();
		int counter = 0;
		for (GmContentViewContext contentViewContext : contentViewContexts) {
			CheckMenuItem menuItem = new CheckMenuItem(contentViewContext.getName());
			menuItem.setGroup("exchangeContentView");
			menuItem.setItemId(String.valueOf(counter));
			menuItem.setIcon(contentViewContext.getHoverIcon());
			menuItem.addCheckChangeHandler(event -> {
				if (event.getChecked().equals(CheckState.CHECKED)) {
					List<GmContentViewContext> contentViewContexts1 = getContentViewContexts();
					int index = Integer.valueOf(event.getItem().getItemId());
					handleChangeView(contentViewContexts1.get(index));
				}
			});
			//if ( currentIndex == counter)
			//	menuItem.setChecked(true, true);
			
			menu.add(menuItem);
			counter++;
		}
		menusToUpdate.add(menu);
		
		if (gmContentView instanceof AssemblyPanel)
			 createAssemblyPanelMenuItem((AssemblyPanel) gmContentView, menu);
		
		if (!(gmContentView instanceof GmCondensationView) || !((GmCondensationView) gmContentView).isUseCondensationActions())
			return menu;
		
		GmCondensationView condensationView = (GmCondensationView) gmContentView;
		CondensateEntityAction condensateEntityAction = condensateEntityActions == null ? null : condensateEntityActions.get(condensationView);
		if (condensateEntityAction == null) {
			condensateEntityAction = new CondensateEntityAction(condensationView,
					condensationView.isLocalCondensationEnabled() ? new UncondenseLocalAction(condensationView) : null);
			if (condensateEntityActions == null)
				condensateEntityActions = new HashMap<>();
			condensateEntityActions.put(condensationView, condensateEntityAction);
		}
		
		MenuItem condensationMenuItem = new MenuItem(LocalizedText.INSTANCE.condensation());
		condensateEntityAction.addComponentToSetMenu(condensationMenuItem);
		condensateEntityAction.updateVisibility(((GmCondensationView) gmContentView).getEntityTypeForProperties());
		condensationMenuItem.setVisible(isSubItemsVisible(condensationMenuItem.getSubMenu()));
		
		//SeparatorMenuItem separator = new SeparatorMenuItem();
		//separator.setVisible(condensationMenuItem.isVisible());
		//menu.add(separator);
		menu.add(condensationMenuItem);
		
		condensationMenuItems = new ArrayList<>();
		condensationMenuItems.add(condensationMenuItem);
		//condensationMenuItems.add(separator);
		
		return menu;
	}

	private void updateNameAndIcons() {
		//setName(maximized ? LocalizedText.INSTANCE.restore() : LocalizedText.INSTANCE.maximize());
		//com.google.gwt.user.client.Window.alert("updateNameAndIcons");
		
		setTooltip(useIconAndDescriptionDefault ? getStateDescription1()  : getStateDescription2());
		setIcon(useIconAndDescriptionDefault ? getStateIcon1() : getStateIcon2());
		setHoverIcon(useIconAndDescriptionDefault ? getStateIcon1() : getStateIcon2());
		//setHoverIcon(maximized ? ConstellationResources.INSTANCE.restoreBig() : ConstellationResources.INSTANCE.maximizeBig());
	}
	
	private void setIconsAndTooltip(boolean useDefault,GmContentViewContext contentViewContext) {		
		if (!useDefault && useAsMenu && contentViewContext != null) { 
    	   useIconAndDescriptionDefault = false;
           iconDynamic = contentViewContext.getIcon();
           descriptionDynamic = contentViewContext.getName();
    	   
    	   setTooltip(descriptionDynamic);
    	   setIcon(iconDynamic);
    	   setHoverIcon(iconDynamic);
    	   //setHoverIcon(contentViewContext.getHoverIcon());
           return;
		}
		
 	    useIconAndDescriptionDefault = true;
		setTooltip(descriptionDefault);
		setIcon(iconDefault);
		setHoverIcon(iconDefault);
	}
	
	private boolean isSubItemsVisible(Menu menu) {
		boolean isSomethingVisible = false;
		if (menu == null)
			return isSomethingVisible;
			
		for (Widget item : menu) {			
			boolean isItemVisible = true;
			if (item instanceof MenuItem)
				isItemVisible = ((MenuItem) item).isVisible();
			
			isSomethingVisible = (isSomethingVisible || isItemVisible);
			if (isSomethingVisible)
				break;
		}		
		return isSomethingVisible;
	}
		
	private void createAssemblyPanelMenuItem(AssemblyPanel assemblyPanel, Menu menu) {
		if (!assemblyPanel.isUseExchangeAssemblyPanelDisplayModeAction())
			return;
		
		if (exchangeAssemblyPanelDisplayModeAction == null) {
			exchangeAssemblyPanelDisplayModeAction = new ExchangeAssemblyPanelDisplayModeAction();
			
			simpleItemContextMenu = new CheckMenuItem(LocalizedText.INSTANCE.simple());
			detailedItemContextMenu = new CheckMenuItem(LocalizedText.INSTANCE.detailed());
			flatItemContextMenu = new CheckMenuItem(LocalizedText.INSTANCE.flat());
			
			SelectionHandler<Item> menuItemSelectionHandler = event -> {
				TriggerInfo ti = new TriggerInfo();
				if (event.getSelectedItem() == simpleItemContextMenu)
					ti.put(ExchangeAssemblyPanelDisplayModeAction.DISPLAY_MODE, DisplayMode.Simple);
				else if (event.getSelectedItem() == detailedItemContextMenu)
					ti.put(ExchangeAssemblyPanelDisplayModeAction.DISPLAY_MODE, DisplayMode.Detailed);
				else
					ti.put(ExchangeAssemblyPanelDisplayModeAction.DISPLAY_MODE, DisplayMode.Flat);
				exchangeAssemblyPanelDisplayModeAction.perform(ti);
			};
			simpleItemContextMenu.setGroup(ExchangeAssemblyPanelDisplayModeAction.DISPLAY_MODE);
			detailedItemContextMenu.setGroup(ExchangeAssemblyPanelDisplayModeAction.DISPLAY_MODE);
			flatItemContextMenu.setGroup(ExchangeAssemblyPanelDisplayModeAction.DISPLAY_MODE);
			
			simpleItemContextMenu.addSelectionHandler(menuItemSelectionHandler);
			detailedItemContextMenu.addSelectionHandler(menuItemSelectionHandler);
			flatItemContextMenu.addSelectionHandler(menuItemSelectionHandler);
		}
		exchangeAssemblyPanelDisplayModeAction.configureAssemblyPanel(assemblyPanel);
		
		DisplayMode currentDisplayMode = exchangeAssemblyPanelDisplayModeAction.getCurrentDisplayMode();
		switch (currentDisplayMode) {
		case Detailed:
			detailedItemContextMenu.setChecked(true);
			break;
		case Flat:
			flatItemContextMenu.setChecked(true);
			break;
		case Simple:
			simpleItemContextMenu.setChecked(true);
		}

		MenuItem optionsMenuItem = new MenuItem(LocalizedText.INSTANCE.options());
		Menu subMenu = new Menu();
		subMenu.add(simpleItemContextMenu);
		subMenu.add(detailedItemContextMenu);
		subMenu.add(flatItemContextMenu);
		optionsMenuItem.setSubMenu(subMenu);
		SeparatorMenuItem separator = new SeparatorMenuItem();
		menu.add(separator);
		menu.add(optionsMenuItem);
		
		optionsMenuItems = new ArrayList<>();
		optionsMenuItems.add(optionsMenuItem);
		optionsMenuItems.add(separator);
	}
		
	private Window getMenuWindow() {
		menuWindow = new Window();
		menuWindow.setClosable(false);
		menuWindow.setAutoHide(true);
		menuWindow.setShadow(false);
		menuWindow.setHeaderVisible(false);
		menuWindow.setResizable(false);
		
		GmContentView gmContentView = masterDetailConstellation.getCurrentMasterView();
		HorizontalLayoutContainer layoutContainer = new HorizontalLayoutContainer();
		
		List<GmContentViewContext> contentViewContexts = getContentViewContexts();
		int contentViewsSize = contentViewContexts.size();
		boolean useToolTip = false;
		int panelsMaxHeight = 0;
		
		VerticalLayoutContainer optionsView = null;
		VerticalLayoutContainer condensationView = null;
		int condensationsContainerWidth = 0;
		int optionsContainerWidth = 0;
		boolean checkTop = false;
		if (gmContentView instanceof AssemblyPanel && useOptions) {
			toolTipElement = null;
			if (contentViewsSize <= 1)
				useToolTip = true;
			
			if (((AssemblyPanel) gmContentView).isUseExchangeAssemblyPanelDisplayModeAction()) {
				if (exchangeAssemblyPanelDisplayModeAction == null) {
					exchangeAssemblyPanelDisplayModeAction = new ExchangeAssemblyPanelDisplayModeAction();
					exchangeAssemblyPanelDisplayModeAction.configureAssemblyPanel((AssemblyPanel) gmContentView);
				}
			
				optionsView = getOptionsView(useToolTip);
				optionsContainerWidth = 150;
				useToolTip = false;
				panelsMaxHeight = 95;
			}
			checkTop = true;
		}
		
		if (gmContentView instanceof GmCondensationView) {
			if (((GmCondensationView) gmContentView).isUseCondensationActions() && this.useCondensation) {
				CondensateEntityAction condensateEntityAction = condensateEntityActions == null ? null
						: condensateEntityActions.get((GmCondensationView) gmContentView);
				if (condensateEntityAction == null) {
					condensateEntityAction = new CondensateEntityAction((GmCondensationView) gmContentView,
							((GmCondensationView) gmContentView).isLocalCondensationEnabled()
									? new UncondenseLocalAction((GmCondensationView) gmContentView)
									: null);
					if (condensateEntityActions == null)
						condensateEntityActions = new HashMap<>();
					condensateEntityActions.put((GmCondensationView) gmContentView, condensateEntityAction);
				}
				
				condensationView = getCondensationsView((GmCondensationView) gmContentView, condensateEntityAction, useToolTip);
				
				if (condensationView != null) {
					condensationsContainerWidth = 180;
					Menu menu = (Menu) condensationView.getWidget(1);
					condensateEntityAction.onBeforeShowMenu(menu);
					int itemsHeight = 0;
					for (int i = 0; i < menu.getWidgetCount(); i++) {
						Widget widget = menu.getWidget(i);
						if (widget instanceof MenuItem && !isComponentHidden((MenuItem) widget))
							itemsHeight += 24;
						else if (widget instanceof SeparatorMenuItem && !isComponentHidden((SeparatorMenuItem) widget))
							itemsHeight += 6;
					}
					int condensationViewHeight = itemsHeight + 23;
					panelsMaxHeight = Math.max(panelsMaxHeight, condensationViewHeight);
				}
			}
			checkTop = true;
		}
			
		if (checkTop && toolTipElement != null)
			toolTipElement.getStyle().setTop(panelsMaxHeight + 2, Unit.PX);
		
		int factor = contentViewsSize == 0 ? 1 : contentViewsSize % 3 == 0 ? contentViewsSize / 3 : (contentViewsSize / 3) + 1;
		int columns = contentViewsSize <= 1 ? 0 : contentViewsSize == 2 ? 2 : 3;
		if (contentViewsSize > 1) {
			viewHtmlPanel = getViewHtmlPanel(contentViewContexts, factor, panelsMaxHeight);
			layoutContainer.add(viewHtmlPanel, new HorizontalLayoutData(columns * ELEMENT_WIDTH + 8, -1));
		} else
			viewHtmlPanel = null;
		
		
		if (optionsView != null) {
			HorizontalLayoutData optionsContainerData = new HorizontalLayoutData(optionsContainerWidth, panelsMaxHeight + 2, new Margins(0, 2, 0, 2));
			layoutContainer.add(optionsView, optionsContainerData);
		}
		
		if (condensationView != null) {
			HorizontalLayoutData condensationsContainerData = new HorizontalLayoutData(condensationsContainerWidth, panelsMaxHeight + 2,
					new Margins(0, 0, 0, 2));
			layoutContainer.add(condensationView, condensationsContainerData);
		}
		
		menuWindow.add(layoutContainer);
		menuWindow.setWidth((columns * ELEMENT_WIDTH) + 22 + optionsContainerWidth + condensationsContainerWidth);
		menuWindow.setHeight(Math.max((factor * ELEMENT_HEIGHT) + 21, panelsMaxHeight + 17));
		return menuWindow;
	}
	
	private HtmlPanel getViewHtmlPanel(final List<GmContentViewContext> contentViewContexts, int factor, int panelsMaxHeight) {
		HtmlPanel viewHtmlPanel = new HtmlPanel();
		viewHtmlPanel.setBorders(false);
		viewHtmlPanel.setBodyBorder(false);
		
		StringBuilder htmlString = new StringBuilder();
		htmlString.append("<div class='").append(ConstellationResources.INSTANCE.css().toolBarParentStyle()).append("' style='height: ");
		htmlString.append(Math.max((factor * ELEMENT_HEIGHT) + 4, panelsMaxHeight)).append("px;'><ul class='gxtReset ");
		htmlString.append(ConstellationResources.INSTANCE.css().toolBarStyle());
		htmlString.append("'>");
		
		viewHtmlPanel.setHeight(Math.max((factor * ELEMENT_HEIGHT) + 13, panelsMaxHeight + 11));
		
		int counter = 0;
		for (GmContentViewContext contentViewContext : contentViewContexts) {
			htmlString.append("<li>");
			htmlString.append("<div class='").append(ConstellationResources.INSTANCE.css().toolBarElement());
			if (currentIndex == counter)
				htmlString.append(" ").append(ConstellationCss.EXTERNAL_TOOL_BAR_SELECTED);
			htmlString.append("' style='position: relative;'");
			htmlString.append(" id='").append(contentViewContext.getName()).append("gmContentViewContext").append(idCounter++).append("'");
			htmlString.append("><img src='").append(contentViewContext.getHoverIcon().getSafeUri().asString()).append("' class='");
			htmlString.append(ConstellationResources.INSTANCE.css().toolBarElementImage()).append("'/>");
			htmlString.append("<div class='").append(ConstellationResources.INSTANCE.css().toolBarElementText()).append("'");
			htmlString.append(">").append(contentViewContext.getName()).append("</div></div>");
			htmlString.append("</li>");
			counter++;
		}
		
		htmlString.append("</ul></div>");
		/*
		 * RVE commented out the small triangle to the position of clicked menu icon
		htmlString.append("<div class='").append(ConstellationResources.INSTANCE.css().toolTip()).append("' style='top: ");
		htmlString.append(Math.max((factor * ELEMENT_HEIGHT) + 7, panelsMaxHeight + 3));
		htmlString.append("px'></div>");
		*/
		
		viewHtmlPanel.setHtml(htmlString.toString());
		viewHtmlPanel.clearWidgets();
		viewHtmlPanel.init();
		
		viewHtmlPanel.sinkEvents(Event.ONCLICK);
		viewHtmlPanel.addHandler(event -> {
			EventTarget target = event.getNativeEvent().getEventTarget();
			if (!Element.is(target))
				return;
			
			Element actionElement = getActionElement(Element.as(target), 3, ConstellationResources.INSTANCE.css().toolBarElement());
			if (actionElement == null)
				return;
			
			for (GmContentViewContext contentViewContext : contentViewContexts) {
				if (!actionElement.getId().startsWith(contentViewContext.getName()))
					actionElement.removeClassName(ConstellationCss.EXTERNAL_TOOL_BAR_SELECTED);
				else {
					handleChangeView(contentViewContext);
					actionElement.addClassName(ConstellationCss.EXTERNAL_TOOL_BAR_SELECTED);
					menuWindow.hide();
					break;
				}
			}
		}, ClickEvent.getType());
		
		return viewHtmlPanel;
	}
	
	private VerticalLayoutContainer getOptionsView(boolean useToolTip) {
		VerticalLayoutContainer optionsContainer = new VerticalLayoutContainer();
		optionsContainer.setBorders(false);
		optionsContainer.addStyleName(ConstellationResources.INSTANCE.css().greyBorder());
		Label optionsLabel = new Label(LocalizedText.INSTANCE.options() + ":");
		optionsLabel.addStyleName(ConstellationResources.INSTANCE.css().centeredText());
		optionsContainer.add(optionsLabel);
		
		Menu menu = getOptionMenu();
		optionsContainer.add(menu);
		
		if (useToolTip) {
			Element divElement = DOM.createDiv();
			divElement.setClassName(ConstellationResources.INSTANCE.css().toolTip());
			menu.getElement().getParentElement().appendChild(divElement);
			toolTipElement = divElement;
		}
		
		return optionsContainer;
	}

	private Menu getOptionMenu() {
		Menu menu = new Menu();
		menu.setShadow(false);
		menu.setBorders(false);
		
		final CheckMenuItem simpleItem = new CheckMenuItem(LocalizedText.INSTANCE.simple());
		final CheckMenuItem detailedItem = new CheckMenuItem(LocalizedText.INSTANCE.detailed());
		CheckMenuItem flatItem = new CheckMenuItem(LocalizedText.INSTANCE.flat());
		
		SelectionHandler<Item> menuItemSelectionHandler = event -> {
			menuWindow.hide();
			TriggerInfo ti = new TriggerInfo();
			if (event.getSelectedItem() == simpleItem) {
				ti.put(ExchangeAssemblyPanelDisplayModeAction.DISPLAY_MODE, DisplayMode.Simple);
				if (simpleItemContextMenu != null) {
					simpleItemContextMenu.setChecked(true, true);
					detailedItemContextMenu.setChecked(false, true);
					flatItemContextMenu.setChecked(false, true);
				}
			} else if (event.getSelectedItem() == detailedItem) {
				ti.put(ExchangeAssemblyPanelDisplayModeAction.DISPLAY_MODE, DisplayMode.Detailed);
				if (detailedItemContextMenu != null) {
					detailedItemContextMenu.setChecked(true, true);
					simpleItemContextMenu.setChecked(false, true);
					flatItemContextMenu.setChecked(false, true);
				}
			} else {
				ti.put(ExchangeAssemblyPanelDisplayModeAction.DISPLAY_MODE, DisplayMode.Flat);
				if (flatItemContextMenu != null) {
					flatItemContextMenu.setChecked(true, true);
					simpleItemContextMenu.setChecked(false, true);
					detailedItemContextMenu.setChecked(false, true);
				}
			}
			exchangeAssemblyPanelDisplayModeAction.perform(ti);
		};
		simpleItem.setGroup(ExchangeAssemblyPanelDisplayModeAction.DISPLAY_MODE);
		detailedItem.setGroup(ExchangeAssemblyPanelDisplayModeAction.DISPLAY_MODE);
		flatItem.setGroup(ExchangeAssemblyPanelDisplayModeAction.DISPLAY_MODE);
		
		DisplayMode currentDisplayMode = exchangeAssemblyPanelDisplayModeAction.getCurrentDisplayMode();
		switch (currentDisplayMode) {
		case Detailed:
			detailedItem.setChecked(true);
			break;
		case Flat:
			flatItem.setChecked(true);
			break;
		case Simple:
			simpleItem.setChecked(true);
		}

		simpleItem.addSelectionHandler(menuItemSelectionHandler);
		detailedItem.addSelectionHandler(menuItemSelectionHandler);
		flatItem.addSelectionHandler(menuItemSelectionHandler);
		
		menu.add(simpleItem);
		menu.add(detailedItem);
		menu.add(flatItem);
		return menu;
	}
	
	private VerticalLayoutContainer getCondensationsView(GmCondensationView condensationView, CondensateEntityAction condensateEntityAction, boolean useToolTip) {
		if (this.condensationsContainer != null)
			return this.condensationsContainer;
		
		TextButton fakeButton = new TextButton();
		condensateEntityAction.addComponentToSetMenu(fakeButton);
		condensateEntityAction.updateVisibility(condensationView.getEntityTypeForProperties());
		
		Menu menu = fakeButton.getMenu();
		if (menu != null)
			condensationsContainer = new VerticalLayoutContainer();
		else
			return null;
		
		condensationsContainer.setBorders(false);
		condensationsContainer.addStyleName(ConstellationResources.INSTANCE.css().greyBorder());
		Label condensationLabel = new Label(LocalizedText.INSTANCE.condensation() + ":");
		condensationLabel.addStyleName(ConstellationResources.INSTANCE.css().centeredText());
		condensationsContainer.add(condensationLabel);
		
		menu.setShadow(false);
		menu.setBorders(false);
		condensationsContainer.add(menu);
		
		condensateEntityAction.configureAfterSelectionAction(new Action() {
			@Override
			public void perform(TriggerInfo triggerInfo) {
				menuWindow.hide();
			}
		});
		
		if (useToolTip) {
			Element divElement = DOM.createDiv();
			divElement.setClassName(ConstellationResources.INSTANCE.css().toolTip());
			condensationsContainer.getElement().appendChild(divElement);
			toolTipElement = divElement;
		}
		
		return this.condensationsContainer;
	}
	
	private Element getActionElement(Element clickedElement, int depth, String className) {
		if (depth > 0 && clickedElement != null) {
			if (className.equals(clickedElement.getClassName()))
				return clickedElement;
			
			return getActionElement(clickedElement.getParentElement(), --depth, className);
		}
		
		return null;
	}
	
	private void handleChangeView(GmContentViewContext contentViewContext) {
		int selectedIndex = masterDetailConstellation.getFirstSelectedIndex();
		
		Future<GmContentView> provideFuture = new Future<>();			
		masterDetailConstellation.provideAndExchangeView(contentViewContext, false, provideFuture);		
		
		provideFuture.andThen(view -> {
			selectEntry(view, selectedIndex);
			setIconsAndTooltip(false, contentViewContext);	
		});
	}

	protected MasterDetailConstellation getMasterDetailConstellation(Object view) {
		if (view == null)
			return null;
		
		if (view instanceof MasterDetailConstellation) 
			return (MasterDetailConstellation) view;
		
		if (view instanceof QueryConstellation) {
			GmContentView queryView = ((QueryConstellation) gmContentView).getView();
			return getMasterDetailConstellation(queryView);
		}
		
		if (view instanceof Widget)
			return getMasterDetailConstellation(((Widget) view).getParent());
		
		return null;
	}
	
	private void selectEntry(GmContentView contentView, int selectedIndex) {
		if (selectedIndex == -1)
			selectedIndex = 0;
		
		boolean navigateToAction = contentView instanceof GmViewActionProvider;
		if (navigateToAction)
			gmViewActionBarSupplier.get().prepareActionsForView((GmViewActionProvider) contentView);
		
		int finalIndex = selectedIndex;
		new Timer() {
			@Override
			public void run() {
				contentView.select(finalIndex, false);
				if (navigateToAction)
					Scheduler.get().scheduleDeferred(() -> gmViewActionBarSupplier.get().navigateToAction(ExchangeContentViewAction.this));
				else
					gmViewActionBarSupplier.get().prepareActionsForView(null);
			}
		}.schedule(500);
	}
	
	private static native void toggleRadios(CheckMenuItem menuItem) /*-{
		menuItem.@com.sencha.gxt.widget.core.client.menu.CheckMenuItem::toggleRadios()();
	}-*/;
	
	private native boolean isComponentHidden(Component component) /*-{
		return component.@com.sencha.gxt.widget.core.client.Component::hidden;
	}-*/;

	@Override
	public void setStateIcon1(ImageResource icon) {
		// NOP		
	}

	@Override
	public void setStateIcon2(ImageResource icon) {
		// NOP		
	}

	@Override
	public ImageResource getStateIcon1() {
		return iconDefault;
	}

	@Override
	public ImageResource getStateIcon2() {
		return iconDynamic;
	}

	@Override
	public void setStateDescription1(String description) {
		// NOP		
	}

	@Override
	public void setStateDescription2(String description) {
		// NOP		
	}

	@Override
	public String getStateDescription1() {
		return descriptionDefault;
	}

	@Override
	public String getStateDescription2() {
		return descriptionDynamic;
	}

	@Override
	public void updateState() {
		updateNameAndIcons();		
	}

	@Override
	public Boolean isDefaultState() {
		return useIconAndDescriptionDefault;
	}
	
}
