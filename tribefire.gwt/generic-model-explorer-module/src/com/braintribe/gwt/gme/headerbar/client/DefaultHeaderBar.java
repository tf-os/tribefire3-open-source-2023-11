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
package com.braintribe.gwt.gme.headerbar.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gm.model.uiaction.ActionFolderContent;
import com.braintribe.gwt.action.adapter.gxt.client.MenuItemActionAdapter;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.KnownProperties;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.customizationui.client.ShowWindowAction;
import com.braintribe.gwt.customizationui.client.security.LogoutAction;
import com.braintribe.gwt.gme.constellation.client.SettingsMenu;
import com.braintribe.gwt.gme.constellation.client.action.AboutAction;
import com.braintribe.gwt.gme.constellation.client.action.ChangeAccessAction;
import com.braintribe.gwt.gme.constellation.client.action.GlobalAction;
import com.braintribe.gwt.gme.constellation.client.action.HasSubMenu;
import com.braintribe.gwt.gme.constellation.client.action.ReloadSessionAction;
import com.braintribe.gwt.gme.constellation.client.action.TestHeaderbarAction;
import com.braintribe.gwt.gme.constellation.client.expert.GlobalActionsHandler;
import com.braintribe.gwt.gme.constellation.client.expert.GlobalActionsListener;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gme.headerbar.client.action.HeaderBar;
import com.braintribe.gwt.gme.headerbar.client.action.HeaderBarConfig;
import com.braintribe.gwt.gme.headerbar.client.resources.HeaderBarResources;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessTriggerField;
import com.braintribe.gwt.gmresourceapi.client.GmImageResource;
import com.braintribe.gwt.gmview.action.client.ActionFolderContentExpert;
import com.braintribe.gwt.gmview.action.client.ActionWithMenu;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.htmlpanel.client.HtmlPanel;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.folder.FolderContent;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.workbench.HyperlinkAction;
import com.braintribe.model.workbench.WorkbenchAction;
import com.braintribe.model.workbench.WorkbenchConfiguration;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style.Anchor;
import com.sencha.gxt.core.client.Style.AnchorAlignment;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

/**
 * Display HeaderBar. Depending on workbench DB configuration display headerBar components. If not defined in DB, used previous old style from TopBanner.html
 *
 */
public class DefaultHeaderBar implements HeaderBar, GlobalActionsListener {
	private PersistenceGmSession gmSession;
	private Function<WorkbenchActionContext<?>, ModelAction> workbenchActionHandlerRegistry;
	private Folder rootFolder;
	private PersistenceGmSession workbenchSession;
	private HtmlPanel headerBarHtmlPanel;
	private Image oldLogoImage;          //used with old style
	private Widget oldTopBanner;         //used with old style 
	private int oldWestDataSize;         //used with old style
	private Supplier<? extends QuickAccessTriggerField> quickAccessTriggerFieldSupplier;
	private QuickAccessTriggerField quickAccessTriggerField;
	private Label globalStateSlot;
	//private Widget notificationSlot;
	private Menu settingsMenu;
	private Menu userMenu;
	private ImageResource defaultSettingsMenuIcon = null;
	private ImageResource defaultUserMenuIcon = null;
	private Supplier<Future<Image>> userIconProvider;
	private Supplier<String> tooltipUserProvider;
	//private Boolean preparedOld = false;
	private String userIconUrl = "";
	private String userToolTip = "";
	private List<HyperlinkAction> listExternHyperLinkAction = null;  //external list of Links which would be add to static/dynamic headbar links
	private List<HyperlinkAction> listLocalHyperLinkAction = new ArrayList<>(); 
	private String userImageServletUrl = null;
	
	private int headerCounter = 0;
	private int iconHeight = 32;
		
	//private ShowWindowAction showAction;
	private Map<String, Widget> widgetMap = new FastMap<>();
	private Map<String, Menu> menuMap = new FastMap<>();
	private Map<String, Action> actionsMap = new FastMap<>();
	private Map<String, ImageResource> imageResourceMap = new FastMap<>();
	private String defaultLogoSource = GWT.getModuleBaseURL() + "bt-resources/commons/defaultLogo.png";
	private boolean useGlobalSearchPanel;
	private Supplier<? extends Component> globalSearchPanelSupplier;
	private Component globalSearchPanel;
	private GlobalActionsHandler globalActionsHandler;
	private boolean globalActionsPrepared = false;
	private Menu lastShowedMenu;
	private Map<String, HeaderBarButton> mapHeaderBarButtons = new FastMap<>();
	private int headerBarButtonId = 1;
	private ActionFolderContentExpert actionFolderContentExpert = null;
	//private TestHeaderbarAction testHeaderbarAction;
	
	//DB Query for get folder structure of HeaderBar
	public DefaultHeaderBar() {
	}
	
	//set sessions
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	@Override
	public PersistenceGmSession getGmSession() {
		return this.gmSession;
	}
	
	/*
	 * Configures the registry for {@link WorkbenchAction}s handlers.
	 * @param workbenchActionHandlerRegistry
	 *
	 */
	@Required
	public void setWorkbenchActionHandlerRegistry(Function<WorkbenchActionContext<?>, ModelAction> workbenchActionHandlerRegistry) {
		this.workbenchActionHandlerRegistry = workbenchActionHandlerRegistry;
	}	
	
	/**
	 * Configures the required {@link QuickAccessTriggerField} used as the quick access field. Based on the WorkbenchConfiguration,
	 * either this or the component set via {@link #setGlobalSearchPanelSupplier(Supplier)} will be used.
	 */
	@Required
	public void setQuickAccessTriggerFieldSupplier(Supplier<? extends QuickAccessTriggerField> quickAccessTriggerFieldSupplier) {
		this.quickAccessTriggerFieldSupplier = quickAccessTriggerFieldSupplier;
	}
	
	/**
	 * Configures the required GlobalSearchPanel. Based on the WorkbenchConfiguration, either this or the component set via
	 * {@link #setQuickAccessTriggerFieldSupplier(Supplier)} will be used.
	 */
	@Required
	public void setGlobalSearchPanelSupplier(Supplier<? extends Component> globalSearchPanelSupplier) {
		this.globalSearchPanelSupplier = globalSearchPanelSupplier;
	}
	
	@Required
	public void setGlobalStateSlot(Label globalStateSlot) {
		this.globalStateSlot = globalStateSlot;
	}

	/*@Required
	public void setNotificationSlot(Widget notificationSlot) {
		this.notificationSlot = notificationSlot;
	}*/

	public void setOldTopBanner (Widget oldTopBanner) {
		this.oldTopBanner = oldTopBanner;
	}
	
	/**
	 * Configures the logo used when no folder is configured for the headerBar.
	 * If the image configured in {@link #setDefaultLogoSource(String)}, then this image is NOT used.
	 */
	@Configurable
	public void setOldLogoImage (Image oldLogoImage) {
		this.oldLogoImage = oldLogoImage;
	}
	
	@Configurable
	public void setGlobalActionsHandler(GlobalActionsHandler globalActionshandler) {
		this.globalActionsHandler = globalActionshandler;
		if (this.globalActionsHandler != null) {
			this.globalActionsHandler.addGlobalActionListener(this);
			this.globalActionsHandler.setDestinationPanelForWorkbenchAction(this);
		}
	}	
	
	public void setOldWestDataSize (int westDataSize) {
		this.oldWestDataSize = westDataSize;
	}
	public void setHyperlinkActions (List<HyperlinkAction> list) {
		this.listExternHyperLinkAction = list;
	}
	
	/**
	 * Configures the User icon set by Servlet Url
	 */
	@Configurable
	public void setUserImageServletUrl (String userImageServletUrl) {
		this.userImageServletUrl = userImageServletUrl;
	}
		
	@Required
	public void setSettingsMenu(Menu settingsMenu) {
		this.settingsMenu = settingsMenu;
		
		if (!(settingsMenu instanceof SettingsMenu))
			return;
		
		//map Actions in menu - to be able configure which Item to show		 
		SettingsMenu menu = (SettingsMenu) settingsMenu;			
		List<Action> actionItems = menu.getMenuActions();					
		for (Action actionItem : actionItems) {
			//SettingsMenu actions Map
			if (actionItem instanceof ChangeAccessAction)
				actionsMap.put("switchTo", actionItem);	  
			else if (actionItem instanceof ReloadSessionAction)
				actionsMap.put("reloadSession", actionItem);
	//		else if (actionItem instanceof PersistActions)
	//			actionsMap.put("persistActionGroup", actionItem);	  
	//		else if (actionItem instanceof PersistHeaderBarAction)
	//			actionsMap.put("persistHeaderBar", actionItem);	
	//		else if (actionItem instanceof ShowPackagingInfoAction)
	//			actionsMap.put("showAbout", actionItem);	
			else if (actionItem instanceof AboutAction)
				actionsMap.put("showAbout", actionItem);	
			else if (actionItem instanceof ShowWindowAction) {
				String actionID = ((ShowWindowAction) actionItem).getId();
				if (actionID != null && !actionID.isEmpty()) { //Defined by Action.setID - all settingsMenu action should have set this
					if (actionID.toLowerCase().contains("showlogwindowaction"))
						actionsMap.put("showLog", actionItem);
					else if (actionID.toLowerCase().contains("showassetmanagementdialogaction"))
						actionsMap.put("showAssetManagementDialog", actionItem);					
				} else {					
					String name = ((ShowWindowAction) actionItem).getName(); 
					if (name.toLowerCase().contains("log"))
						actionsMap.put("showLog", actionItem);
					else if (name.toLowerCase().contains("platform"))
						actionsMap.put("showAssetManagementDialog", actionItem);
				}
			} else if (actionItem.getName() != null && actionItem.getName().equals("Settings")) 
				actionsMap.put("showSettings", actionItem);						
		}
	}

	@Required
	public void setUserMenu(Menu userMenu) {
		this.userMenu = userMenu;
		
		//map Actions in menu - to be able configure which Item to show
		if (!(userMenu instanceof SettingsMenu))
			return;
		
		for (Action actionItem : ((SettingsMenu) userMenu).getMenuActions()) {
			//UserMenu Actions Map
			if (actionItem.getName().equals("Profile"))
				actionsMap.put("showUserProfile", actionItem);	
			if (actionItem instanceof LogoutAction)
				actionsMap.put("showLogout", actionItem);												
		}			
	}
			
	@Configurable
	public void setActionFolderContentExpert(ActionFolderContentExpert actionFolderContentExpert) {
	   this.actionFolderContentExpert = actionFolderContentExpert;
	}	
	
	/**	 
	 * set defaultIcon for SettingsMenu
	 */	
	
	public void setDefaultSettingsMenuIcon (ImageResource icon) {
		this.defaultSettingsMenuIcon = icon;
	}
	
	/**	 
	 * set defaultIcon for UserMenu
	 */	
	public void setDefaultUserMenuIcon (ImageResource icon) {
		this.defaultUserMenuIcon = icon;
	}
	
	public void setUserIconProvider (Supplier<Future<Image>> iconProvider) {
		this.userIconProvider = iconProvider;	
	}
	
	public void setUserTooltipProvider(Supplier<String> tooltipProvider) {
		this.tooltipUserProvider = tooltipProvider;	
	}

	public void setIconHeight(int iconHeight) {
		this.iconHeight = iconHeight; 
	}
		
	/**
	 * Configures the required workbench session.
	 */
	@Required
	@Override
	public void setWorkbenchSession(PersistenceGmSession workbenchSession) {
		this.workbenchSession = workbenchSession;
	}
	
	/**
	 * Configures the url for the default logo to be used when no folder is available for the headerBar.
	 * If the source is not found, then {@link #setOldLogoImage(Image)} is used.
	 */
	@Configurable
	public void setDefaultLogoSource(String defaultLogoSource) {
		this.defaultLogoSource = defaultLogoSource;
	}
	
	@Override
	public Future<Void> apply(Folder folder) {
		try {
			if (tooltipUserProvider != null)	
				userToolTip = tooltipUserProvider.get();
			
			if (userIconProvider != null) {
				userIconProvider.get().onError(ex -> ex.printStackTrace()).andThen(image -> {
					if (image != null)
						userIconUrl = image.getUrl();
				});
			}
		} catch (Exception e) {
			//nope
		}
				
		WorkbenchConfiguration workbenchConfiguration = null;
		if (gmSession instanceof ModelEnvironmentDrivenGmSession)
			workbenchConfiguration = ((ModelEnvironmentDrivenGmSession) gmSession).getModelEnvironment().getWorkbenchConfiguration();
		useGlobalSearchPanel = workbenchConfiguration == null ? false : workbenchConfiguration.getUseGlobalSearch();
				
		rootFolder = folder;
		if (rootFolder == null)
			prepareOldHeaderBarStyle(headerBarHtmlPanel);
		else
			prepareHeaderBarHtml(headerBarHtmlPanel);
		
		if (globalActionsHandler != null)
			globalActionsHandler.prepareListeners();
				
		return new Future<>(null);
	}
	
	/**
	 * Disable some UI elements for removing access from the User.
	 */
	public void disableUI() {
		if (useGlobalSearchPanel && globalSearchPanel != null)
			globalSearchPanel.disable();
		else if (quickAccessTriggerField != null)
			quickAccessTriggerField.disable();
		userMenu.disable();
		settingsMenu.disable();
	}
	
	/**
	 * Enables some UI elements for restoring access for the User.
	 */
	public void enableUI() {
		if (useGlobalSearchPanel && globalSearchPanel != null)
			globalSearchPanel.enable();
		else if (quickAccessTriggerField != null)
			quickAccessTriggerField.enable();
		userMenu.enable();
		settingsMenu.enable();
	}
	
	//create HTML for Menu (for menu is showed Icon, than later on click on this icon, menu is showed)
	private String prepareWidgetMenuFolder(Menu menu, String actionID, String imageUrl, String errorImageUrl, String toolTip, String displayName, Integer imageSize) {		
		StringBuilder builder = new StringBuilder();
		builder.append("<div class='headerbar-folder-menu enabled' style='position: relative;");
		builder.append("' id='").append(actionID).append("'");
		if (toolTip != null)
			builder.append(" title='").append(toolTip).append("'");
		this.menuMap.put(actionID, menu);		
		builder.append(">");
		
		//image
		if (!imageUrl.isEmpty()) {
			String style = "";
			if (imageSize > 0) 
				style = "style='width:"+ imageSize +"px;height:"+ imageSize +"px;'";
			
		    builder.append("<img src='").append(imageUrl).append("' class='headerbar-folder-menu-image' ").append(style);
			if (toolTip != null)
				builder.append(" title='").append(toolTip).append("'");
			if (errorImageUrl != null)
				builder.append("onError=\"this.onerror=null;this.src='" + errorImageUrl+ "';\" ");
			builder.append(" />");
		}
		
		if (!displayName.isEmpty()) {
			builder.append("<li class='headerbar-folder-menu enabled' id='").append(actionID)
					.append("'><a class='topBannerAnchor' href='javascript:void(0)'>");
			builder.append(displayName);
			if (toolTip != null)
				builder.append(" title='").append(toolTip).append("'");
			builder.append("</a></li>");
		}
		
		builder.append("<div class='headerbar-text'");
		builder.append(">").append("</div>");
		builder.append("</div>");
		String innerHtml = builder.toString();		
		Document.get().createLIElement().setInnerHTML(innerHtml);
					
		return innerHtml;
	}
			
	//create HTML for Menu (for menu is showed Icon, than later on click on this icon, menu is showed)
	private String prepareActionFolder(Action action, String actionID, String imageUrl, String toolTip, String displayName, boolean enabled) {
		HeaderBarActionButton button = new HeaderBarActionButton();
		button.setAction(action);
		button.addDisabledActionProperty(KnownProperties.PROPERTY_NAME);
		button.addDisabledActionProperty(KnownProperties.PROPERTY_HOVERICON);
		button.addDisabledActionProperty(KnownProperties.PROPERTY_TOOLTIP);
		button.linkActionWithButton();
		
		button.setId(actionID);
		button.setImageUrl(imageUrl);
		button.setTooltip(toolTip);
		button.setText(displayName);
		button.setEnabled(enabled);	
		button.setMenuImageUrl(ConstellationResources.INSTANCE.arrowDownSmall().getSafeUri().asString());
		if (action != null) {
			button.setData("action", action);
			Menu menu = menuMap.get(action.getId());
			if (menu != null) {
				menu.addStyleName("headerBarMenu");
				menu.setShadow(false);
				button.setMenu(menu);
				button.setUseMenuButton(true);
			}
		}
		
		button.addListener(new HeaderBarButtonListener() {			
			@Override
			public void onMouseOverButton(HeaderBarButton button) {
				//NOP
			}
			
			@Override
			public void onMouseOutButton(HeaderBarButton button) {
				//NOP				
			}
			
			@Override
			public void onClickButton(HeaderBarButton button) {
				Action action = button.getData("action");
				handleActionFolderClicked(action);
			}
		});
				
		StringBuilder builder = new StringBuilder();
		String buttonId = "headerbar-button-id-" + headerBarButtonId; 
		builder.append("<div id='").append(buttonId).append("'/>");		
		mapHeaderBarButtons.put(buttonId, button);
		headerBarButtonId++;
		return builder.toString();
		//return button.getHtml();
		
		/*
		StringBuilder builder = new StringBuilder();
		if (enabled) 
			builder.append("<div class='headerbar-folder-action enabled' style='position: relative;");
		else 
			builder.append("<div class='headerbar-folder-action disabled' style='position: relative;");
			
		builder.append("' id='").append(actionID).append("'");
		
		//Tootip - show only in case with just icon (without text button)
		if (toolTip != null && displayName.isEmpty())
			builder.append(" title='").append(toolTip).append("'");
		
		builder.append(">");		
		builder.append("<a class='topBannerAnchor' href='javascript:void(0)'>");
		builder.append("<div class='headerbar-folder-action enabled' id='" + actionID + "'>");
		
				
		//image
		if (!imageUrl.isEmpty())  
			builder.append("<img src='").append(imageUrl).append("' class='headerbar-folder-action-image'/>");
		
		if (!displayName.isEmpty()) 
			builder.append("<div class='headerbar-text' id='" + actionID + "'>").append(displayName).append("</div>");
		
		builder.append("</div>");
		builder.append("</a>");			
		builder.append("</div>");
		
		return  builder.toString();	
		*/
	}
	
	//create dynamic menu/submenu
	private Menu createMenuItems (Folder menuFolder, SettingsMenu menu) {
		if (menuFolder == null || menuFolder.getSubFolders() == null || menuFolder.getSubFolders().isEmpty())
			return menu;
								
		if (menu == null) {
			menu = new SettingsMenu();
			menu.setId(menuFolder.getName());
			widgetMap.put(menuFolder.getName(), menu);
		} else {
			menu.clear();
			/*Widget widgetToDelete;
			while (menu.getWidgetCount() > 0) {
				
				widgetToDelete = menu.getWidget(0);
				menu.remove(widgetToDelete);
			}*/
 		}
		
		for (Folder subFolder : menuFolder.getSubFolders()) {
			String subFolderName = subFolder.getName();
			Menu subMenu = createMenuItems(subFolder, null);
			MenuItem menuItem;
			
			if (!subFolderName.startsWith("$")) {
				String displayName = I18nTools.getLocalized(subFolder.getDisplayName());
				menuItem = new MenuItem(displayName);				
				//Icon from Folder
				Icon icon = subFolder.getIcon();
				if (subMenu != null) 
					menuItem.setSubMenu(subMenu);
				else {
					//String folderLink = null;
					ModelAction workbenchAction = null;
					if (subFolder.getContent() != null && subFolder.getContent() instanceof WorkbenchAction) {
						workbenchAction = prepareModelAction((WorkbenchAction) subFolder.getContent(), subFolder);
						/*
						HyperlinkAction hyperlinkAction = (HyperlinkAction) subFolder.getContent();
						folderLink = hyperlinkAction.getUrl();
						//Icon from HyperLinkAction
						if (hyperlinkAction.getIcon() != null)
							icon = hyperlinkAction.getIcon();
						*/	
					}
					final ModelAction modelAction = workbenchAction;
					menuItem.addSelectionHandler(event -> {
						if (modelAction != null)
							modelAction.perform(null);
						//Window.open(openLink,"_blank","");						
					});
				}
				
				if (icon != null) {
					Resource resource = GMEIconUtil.getImageFromIcon(icon, iconHeight, iconHeight);
					if (resource != null) {
						ImageResource imageResource = new GmImageResource(resource, workbenchSession.resources().url(resource).asString()); 
						menuItem.setIcon(imageResource);
					}
				}
				menu.add(menuItem);									
			} else {
				subFolderName = subFolderName.replace("$", "");
				Action action = actionsMap.get(subFolderName);
				if (action == null)
					continue;
																
				if (action.getIcon() != null)
					menuItem = new MenuItem(action.getName(), action.getIcon());
				else
					menuItem = new MenuItem(action.getName());

				if (action instanceof HasSubMenu)							
					menuItem.setSubMenu(((HasSubMenu) action).getSubMenu());
				else {
					MenuItemActionAdapter.linkActionToMenuItem(action, menuItem);
					
					if (subMenu != null) {
						//RVE - in some cases this can be need
						//menuItem.addSelectionListener(new SelectionListener<MenuEvent>() {
						//	public void componentSelected(MenuEvent ce) {
						//		action.perform(null);
						//	}
						//});
						
						menuItem.setSubMenu(subMenu);												
					}							
				}
				
				menu.add(menuItem);					
			} 			
		}
		
		//this need only in case if we fill Menu Items via SettingsMenu.setMenuActions	
		//but need create menu items directly, because can be only menu item (without action) for sub menu items
		/*
		menu.setMenuActions(menuActions);		
		try {
			menu.intializeBean();
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
		return menu;
	}
	
	//set Menu items in Visible/Unvisible depending on Workbench structure
	/*private void prepareMenuItems(Widget widgetFolder, Folder menuFolder) {
		if ((menuFolder != null) && (widgetFolder != null) && (menuFolder.getSubFolders() != null)) {
			String subFolderName;
			SettingsMenu menu;
			Action mapAction;
			Boolean foundAction;
			int i;
		    
			if (widgetFolder instanceof SettingsMenu) {
			   menu = (SettingsMenu) widgetFolder;
			   
			   List<Action> actionItems = menu.getMenuActions();
			   //go through all items in menu
			   
			   i = 0;
			   for (Action actionItem : actionItems) {
			        //if item exists in Map, than check if is in workbench	
				    foundAction = false;
				    //if (itemMap.containsValue(actionItem)) {
						for (Folder subFolder : menuFolder.getSubFolders()) {	
							subFolderName = subFolder.getName();
																		
							if (subFolderName.startsWith("$")) {
								subFolderName = subFolderName.replace("$", "");
											
								mapAction = this.actionsMap.get(subFolderName);
								if (mapAction != null) {
									if (mapAction.equals(actionItem)) {
										foundAction = true;
										break;
									}
								} else {									
									if (!this.actionsMap.containsValue(actionItem)) {
									    foundAction = true;
									    break;
									}
								}
							}
						}
				    //} else {
				    //	foundAction = true;
				    //}
					menu.getWidget(i).setVisible(foundAction);
				    i++;    
			   }
			}
		}
	}*/
	
	//used OLD html base variant of headerBar in case of not defined HeaderBar at Workbench DB
	private void prepareOldHeaderBarStyle(HtmlPanel headerBar) {
		if (headerBar == null)
			return;
		
		//test externalLink
		/*
		HyperlinkAction hyperlinkTest;
		LocalizedString displayName;
		listHyperlinkAction = new ArrayList<HyperlinkAction>(); 
		
		hyperlinkTest = workbenchSession.create(HyperlinkAction.T);
		hyperlinkTest.setUrl("https://www.google.com/");
		displayName = workbenchSession.create(LocalizedString.T);
		displayName.getLocalizedValues().put("default", "Google test");
		hyperlinkTest.setDisplayName(displayName);
		listHyperlinkAction.add(hyperlinkTest);
		
		hyperlinkTest = workbenchSession.create(HyperlinkAction.T);
		hyperlinkTest.setUrl("https://www.braintribe.com/");
		displayName = workbenchSession.create(LocalizedString.T);
		displayName.getLocalizedValues().put("default", "Braintribe");
		hyperlinkTest.setDisplayName(displayName);						
		listHyperlinkAction.add(hyperlinkTest);
		*/
        //test end
		
		/*test
		Button button = new Button();
		button.setText("Test");
		button.addClickHandler(new ClickHandler() {			
			public void onClick(ClickEvent event) {
				FontPicker fontPicker = new FontPicker(FontPickerType.FONT_SIZE);
				fontPicker.show();
			}
		});
		*/
				
		mapHeaderBarButtons.clear();
		
		ContentPanel logoPanel = new ContentPanel();
		logoPanel.addStyleName(ConstellationResources.INSTANCE.css().bannerImage());
		//logoPanel.setBodyStyle("boder-right-width: 1px; border-right-style: dotted;");
		logoPanel.setBodyBorder(false);
		logoPanel.setBorders(false);
		logoPanel.setHeaderVisible(false);
		int logoWidth = 0;
		if (this.oldLogoImage != null) {
			final Image logoImage = new Image();
			logoImage.addErrorHandler(event -> {
				logoImage.setUrl(oldLogoImage.getUrl());
				DefaultHeaderBar.this.oldLogoImage = logoImage;
				Document.get().getElementById("logoPanel").getStyle().setWidth(200, Unit.PX);
			});
			//logoImage.addLoadHandler(event -> Document.get().getElementById("logoPanel").getStyle().setWidth(36 + logoImage.getWidth(), Unit.PX));
			logoImage.setUrl(defaultLogoSource);
			logoPanel.add(logoImage);
			logoWidth = logoImage.getWidth();
		}
		BorderLayoutData westData = new BorderLayoutData(oldWestDataSize);
		westData.setMargins(new Margins(0, 4, 0, 0));
		//headerBar.add(logoPanel, westData);	
					
		//final List<Action> listAction = new ArrayList<Action>();
							
		//RVE - Links in Menu
		HTML linkMenuHtml = new HTML();
		linkMenuHtml.addStyleDependentName("style='margin: 5px 0px 0px 0px'");

		StringBuilder menuHtmlString = getLinkMenuHtmlString(HeaderBarConfig.LINK_DYNAMIC_SLOT.getDisplayName());
		linkMenuHtml.setHTML(menuHtmlString.toString());
		
		ContentPanel bannerPanel = new ContentPanel();
		bannerPanel.setBodyBorder(false);
		bannerPanel.setBorders(false);
		bannerPanel.setHeaderVisible(false);
		bannerPanel.setStyleName("bannerPanel");
		bannerPanel.setBodyStyle("backgroundColor: white; width: auto");
		if (oldTopBanner != null) {
			if (oldTopBanner instanceof HtmlPanel) {
				String slotName = HeaderBarConfig.LINK_DYNAMIC_SLOT.getName();
				slotName = slotName.replace("$", "");
				((HtmlPanel) oldTopBanner).addWidget(slotName, linkMenuHtml);    //use Link Menu
				((HtmlPanel) oldTopBanner).init();
			}
			bannerPanel.add(this.oldTopBanner);
			//bannerPanel.add(button);  //test
		}
		
		//headerBar.add(bannerPanel, new BorderLayoutData(LayoutRegion.EAST));		
		
		StringBuilder htmlString = new StringBuilder();
		htmlString.append("<html><body>");
		htmlString.append("<div style='background-color:white;height:60px'>");
		htmlString.append("<ul class='gxtReset'>");	
		    
		htmlString.append("<li style='float:left'>");
		htmlString.append("<a href='").append(Window.Location.getHref()).append("'>");		  //link to Home page	
		htmlString.append("<div id='logoPanel' style='width:").append(logoWidth + 36).append("px; float:left'>");    // style='margin:4px'
		htmlString.append("</div>");
		htmlString.append("</a>");			
		htmlString.append("</li>");
		
		htmlString.append("<li style='width:auto'>");
		htmlString.append("<div id='bannerPanel' style='width:auto'>");    // style='margin:4px'
		htmlString.append("</div>");
		htmlString.append("</li>");

		htmlString.append("</ul>");    
		htmlString.append("</div>");		
		htmlString.append("</body></html>");	
						
		headerBar.setHtml(htmlString.toString());		
		headerBar.clearWidgets();
		
		headerBar.addWidget("logoPanel", logoPanel);
		headerBar.addWidget("bannerPanel", bannerPanel);
		
		for (String key : mapHeaderBarButtons.keySet()) {
			HeaderBarButton button = mapHeaderBarButtons.get(key);
			if (button != null) {
				headerBar.addWidget(key, button);
			}
		}
				
		headerBar.init();
	}

	private static StringBuilder getLinkMenuHtmlString(String tooltip) {
		ImageResource icon = ConstellationResources.INSTANCE.menu64();
		String imageUrl = null;
		int imageSize = 32;  //must be >0
		if (icon != null)
			imageUrl = icon.getSafeUri().asString();
		
		StringBuilder menuHtmlString = new StringBuilder();
		String style = "style='width:"+ imageSize +"px;height:"+ imageSize +"px;'";
		menuHtmlString.append("<a href='javascript:void(0)'>");
		menuHtmlString.append("<div id='headerbar-link-menu' style='margin: 5px 0px 0px 0px'>");						
		menuHtmlString.append("<img src='").append(imageUrl).append("' class='headerbar-folder-menu-image' ").append(style);
		if (tooltip != null)
			menuHtmlString.append(" title='").append(tooltip).append("'");
		menuHtmlString.append(" />");
		menuHtmlString.append("</div>");
		menuHtmlString.append("</a");
		
		return menuHtmlString;
	}
	
	private WorkbenchActionContext<WorkbenchAction> prepareWorkbenchActionContext(final WorkbenchAction workbenchAction, final Folder folder) {
		return new WorkbenchActionContext<WorkbenchAction>() {
			@Override
			public GmSession getGmSession() {
				return gmSession;
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
				return DefaultHeaderBar.this;
			}
			
			@Override
			@SuppressWarnings("unusable-by-js")
			public Folder getFolder() {
				return folder;
			}

			@Override
			public List<ModelPath> getModelPaths() {
				return null;
			}
		};
	}
			
	private ModelAction prepareModelAction(WorkbenchAction workbenchAction, Folder folder) {
		ModelAction modelAction = this.workbenchActionHandlerRegistry.apply(prepareWorkbenchActionContext(workbenchAction, folder));
		if (modelAction != null) {
			String folderName = folder.getName();
			if (folderName.startsWith("$"))
				folderName = folderName.replace("$", "");
			actionsMap.put(folderName, modelAction);				
			//modelAction.perform(null);
		}
		
		return modelAction;
	}	
	
	private void prepareGlobalActionsMap() {
		if (globalActionsHandler == null || globalActionsPrepared)
			return;
		
		List<GlobalAction> listGlobalAction = globalActionsHandler.getConfiguredActions(true);
		
		for (GlobalAction globalAction : listGlobalAction) {
			Action action = globalAction.getAction();
			String name = globalAction.getKnownName();				
			actionsMap.put(name, action);
			
			Menu menu = null;
			if (action instanceof ActionWithMenu) 
				menu = ((ActionWithMenu) action).getActionMenu();
			else 
				menu = globalActionsHandler.getActionMenu(action);
			if (menu != null)
				menuMap.put(action.getId(), menu);
			
			action.addPropertyListener((source, property) -> {
				if (KnownProperties.PROPERTY_ENABLED.equals(property)) {
					Element element  = Document.get().getElementById(source.getId());
					if (element == null)
						return;

					Element actionElement = getChildElement(element, 5, "headerbar-folder-action disabled");
					if (actionElement == null)
						actionElement = getChildElement(element, 5, "headerbar-folder-action enabled");
						
					if (actionElement != null) {
						if (source.getEnabled())
						   actionElement.setClassName("headerbar-folder-action enabled");
						else
						   actionElement.setClassName("headerbar-folder-action disabled");									
					}
				}
			});
		}
		
		globalActionsPrepared = true;
	}
	
	//create HeaderBar complete HTML string also with Widgets included
	private void prepareHeaderBarHtml(HtmlPanel headerBar) {
		if (headerBar == null)
			return;
		
		mapHeaderBarButtons.clear();
		
		prepareGlobalActionsMap();
				
		Map<String,String> usedWidgetMap = new FastMap<>();		
		listLocalHyperLinkAction.clear();
								
		StringBuilder htmlString = new StringBuilder();
		htmlString.append("<html><body>");
		//htmlString.append("<html><body><ul id='tf_nav' class='headerbar-toolbar'>");
		htmlString.append("<div style='background-color:white;height:72px'>");
		htmlString.append("<table border='0' align='left' cellpadding='0' cellspacing='0' class='headerbar-toolbar' style='width=100%'>");
		htmlString.append("<tr>");
		
		//RVE testHeaderbarAction
		/* Enable/Disable using test Action on Headerbar
		actionsMap.put("testAction", testHeaderbarAction);
		prepareNormalFolder(htmlString, "testAction", "testAction", null, "testAction", usedWidgetMap, null, "");
		*/
		
		if (rootFolder != null && rootFolder.getSubFolders() != null) {
			for (Folder subFolder : rootFolder.getSubFolders()) {
				String subFolderName = subFolder.getName();
				if (subFolderName == null)
					continue;
				
				//String folderName =  subFolderName;
				String folderDisplayName = I18nTools.getLocalized(subFolder.getDisplayName()).toString();
				String folderLink = "";
				String folderIcon = "";
				String folderTooltip = "";
				FolderContent folderContent = subFolder.getContent();
				if (folderContent != null) {
					folderDisplayName = I18nTools.getLocalized(folderContent.getDisplayName()); 
					
					if (folderContent instanceof HyperlinkAction ) {
						HyperlinkAction hyperlinkAction;
						hyperlinkAction = (HyperlinkAction) folderContent;
						folderLink = hyperlinkAction.getUrl();								
					}
					
					if (folderContent instanceof WorkbenchAction)
						prepareModelAction((WorkbenchAction) folderContent, subFolder);
					
					if (folderContent instanceof ActionFolderContent) {
						if (actionFolderContentExpert != null) {
							Action action = actionsMap.get(subFolderName.replace("$", ""));
							if (action != null && action instanceof ModelAction) {
								action = actionFolderContentExpert.getConfiguredAction((ActionFolderContent) folderContent, (ModelAction) action); 
							} else {
								action = actionFolderContentExpert.getConfiguredAction((ActionFolderContent) folderContent);
								if (action == null) 
									actionsMap.put(subFolderName.replace("$", ""), action);							
							}
						}						
					}					
				} 				
				
				//get Image, Logo, Icon for menu etc.
				ImageResource imageResource = null;
				Icon subFolderIcon = subFolder.getIcon();
				if (subFolderIcon != null) {
					Resource resource;
					if (subFolderName.equals(HeaderBarConfig.TF_LOGO.getName()))
						resource = GMEIconUtil.getLargeImageFromIcon(subFolderIcon);
					else
						resource = GMEIconUtil.getImageFromIcon(subFolderIcon, iconHeight, iconHeight);
					
					if (resource != null) {
						imageResource = new GmImageResource(resource, workbenchSession.resources().url(resource).asString());
						folderIcon = imageResource.getSafeUri().asString();
					}	
					//folderDisplayName = "";  //show only Icon without caption
				}
				folderTooltip = folderDisplayName;
											
				if (!subFolderName.startsWith("$") || subFolderName.equals(HeaderBarConfig.TF_TITLE.getName()))										
					folderIcon = prepareSpecialFolder(htmlString, subFolderName, folderDisplayName, folderLink, folderIcon, folderTooltip, subFolder, subFolderName);
				else {
					//add external dynamic links from listHyperlinkAction
					if (subFolderName.equals(HeaderBarConfig.LINK_DYNAMIC_SLOT.getName()))
						prepareLinkFolder(htmlString, folderDisplayName, subFolder);
					else {
						//add mapped widget (menus, slots, quickSearchField,..)								
						String folderName = subFolderName.replace("$", "");
						folderIcon = prepareNormalFolder(htmlString, folderName, folderDisplayName, imageResource, folderTooltip, usedWidgetMap, subFolder, subFolderName);
					}
				}
			}
		}
		
		htmlString.append("</tr>");
		htmlString.append("</table>");		
		htmlString.append("</div>");		
		htmlString.append("</body></html>");	
						
		headerBar.setHtml(htmlString.toString());
		headerBar.clearWidgets();
		
		//add only widgets which are really configured - otherwise error
		if (usedWidgetMap.containsKey("quickAccess-slot")) {
			if (useGlobalSearchPanel) {
				globalSearchPanel = globalSearchPanelSupplier.get();
				headerBar.addWidget("quickAccess-slot", globalSearchPanel);
			} else {
				quickAccessTriggerField = quickAccessTriggerFieldSupplier.get();
				headerBar.addWidget("quickAccess-slot", quickAccessTriggerField);
			}
		}
		
		if (usedWidgetMap.containsKey("globalState-slot"))
			headerBar.addWidget("globalState-slot", this.globalStateSlot);
		//if (usedWidgetMap.containsKey("notification-slot"))
		//	headerBar.addWidget("notification-slot", this.notificationSlot);
		
		for (String key : mapHeaderBarButtons.keySet()) {
			HeaderBarButton button = mapHeaderBarButtons.get(key);
			if (button != null) {
				headerBar.addWidget(key, button);
			}
		}		
		
		headerBar.init();			
	}

	private String prepareNormalFolder(StringBuilder htmlString, String folderName, String folderDisplayName, ImageResource imageResource, String folderTooltip,
			Map<String, String> usedWidgetMap, Folder subFolder, String subFolderName) {		
		
		String folderIcon = imageResource != null ? imageResource.getSafeUri().asString() : "";
		int imageWidth = imageResource != null ? imageResource.getWidth() : 0;
		
		if (actionsMap.containsKey(folderName)) {
			Action action = actionsMap.get(folderName);
			if (globalActionsHandler != null && imageResource != null)
				globalActionsHandler.updateMenuItems(action, imageResource);
				
			if ((folderIcon == "" || folderIcon == null) && action.getIcon() != null) {
				folderIcon = action.getIcon().getSafeUri().asString();
				if (folderTooltip == "")
					folderTooltip = folderDisplayName != "" ? folderDisplayName : action.getTooltip();
			}
			
		    //show Action directly at headerBar
			//htmlString.append("<td class='headerbar_action' align='right' valign='middle' style='width:100%'>");
			htmlString.append("<td class='gxtReset headerbar_action' id='").append(action.getId()).append("' align='right' style='width:100%'>");				
			htmlString.append("<ul class='gxtReset' id='headerbar_button'>");
			htmlString.append(prepareActionFolder(action, folderName, folderIcon, folderTooltip, folderDisplayName, action.getEnabled()));								  																				
			htmlString.append("</ul>");
			htmlString.append("</td>");
			return folderIcon;
		}
		
		if (!folderIcon.isEmpty()) {
			//show imaged buton -> on click the button than show widget (menus)
			//htmlString.append("<td class='headerbar_menu' align='center' valign='middle' style='width:"+imageWidth+"px'> ");																						
			htmlString.append("<td class='gxtReset headerbar_menu' align='center' style='width:").append(imageWidth).append("px'> ");																						
			htmlString.append("<a href='javascript:void(0)'>");
			
			Menu subMenu;
			Widget widget = widgetMap.get(folderName);
			if ((widget != null) && (widget instanceof Menu)) {
				//use existing system menus - SettingMenu, UserMenu
				subMenu = (Menu) widget;
			    //prepareMenuItems( widget, subFolder);
			    createMenuItems(subFolder, (SettingsMenu) subMenu );										    
			} else //create special new menu
				subMenu = createMenuItems(subFolder, null);
			
			if (subMenu != null) 
			  htmlString.append(prepareWidgetMenuFolder( subMenu, folderName, folderIcon, null, folderTooltip, "", 0));								  
			
			htmlString.append("</a>");
			htmlString.append("</td>");

			return folderIcon;
		}
		
		if (!widgetMap.containsKey(folderName))
			return folderIcon;
		
		Widget widget = widgetMap.get(folderName);
		if (!(widget instanceof Menu)) {
			//show widget directly (slot, quickSearch field,..)
			//htmlString.append("<td class='headerbar_widget_" + folderName + "' align='center' valign='middle' style='width:350px'>"); 
			if (subFolderName.equals(HeaderBarConfig.GLOBAL_SLOT.getName())) 
				htmlString.append("<td class='gxtReset headerbar_widget_").append(folderName).append("' align='center'>");
			else 
				htmlString.append("<td class='gxtReset headerbar_widget_").append(folderName).append("' align='left'>");
			usedWidgetMap.put(folderName, folderName);
			htmlString.append("<div id='").append(folderName).append("'>");    // style='margin:4px'
			htmlString.append("</div>");
			htmlString.append("</td>");			
		} else {
			folderTooltip = folderDisplayName;
			//htmlString.append("<td class='headerbar_menu' align='right' valign='middle' style='width:"+imageWidth+"px'> ");																						
			htmlString.append("<td class='gxtReset headerbar_menu' style='width:").append(imageWidth).append("px'> ");																						
			htmlString.append("<a href='javascript:void(0)'>");
		    //subMenu = createMenuItems(subFolder);
			Menu subMenu = (Menu) widget;
		    createMenuItems(subFolder, (SettingsMenu) subMenu );
		    //prepareMenuItems( widget, subFolder);
		    Integer size = 32;
		    String errorFolderIcon = null;
			if (imageResourceMap.containsKey(folderName)) {	
				 //use default Icons for standard menus (User menu, Setting menu)														  
				 ImageResource icon = imageResourceMap.get(folderName);
				 if (icon != null) 
					 folderIcon = icon.getSafeUri().asString(); 

				 //get UserMenu Icon
				 if (subMenu.equals(userMenu)) {
					 if (userImageServletUrl != null) {
						 errorFolderIcon = folderIcon;
						 folderIcon = userImageServletUrl;
						 if (!userIconUrl.isEmpty())
							 errorFolderIcon = userIconUrl;
					 } else if (!userIconUrl.isEmpty())
						folderIcon = userIconUrl;
					 
					 folderTooltip = userToolTip;
				 }
			}
			
			if (folderIcon.isEmpty()) 													     											
			      htmlString.append(prepareWidgetMenuFolder( subMenu, folderName, folderIcon, errorFolderIcon, folderTooltip, folderDisplayName, 0));
			else 													   
				  htmlString.append(prepareWidgetMenuFolder( subMenu, folderName, folderIcon, errorFolderIcon, folderTooltip, "", size));
			htmlString.append("</a>");
			htmlString.append("</td>");
		} 
			
		return folderIcon;
	}

	private void prepareLinkFolder(StringBuilder htmlString, String folderDisplayName, Folder subFolder) {
		/*
		final List<Action> listAction = new ArrayList<Action>();
		if (listHyperlinkAction != null) {
			for (HyperlinkAction hyperlinkAction : listHyperlinkAction) {
				final String actionFolderlink = hyperlinkAction.getUrl();
				
				folderDisplayName = I18nTools.getLocalized(hyperlinkAction.getDisplayName());
				htmlString.append("<td  align='right' class='headerbar-link'>");
				htmlString.append("<ul id='tf_nav'>");
				htmlString.append("<li><a class='topBannerAnchor' href='"+ actionFolderlink + "' target='_blank'>");
				htmlString.append(folderDisplayName); 
				htmlString.append("</a></li>");
				htmlString.append("</ul>");
				htmlString.append("</td>");	
															
				Action action = new Action() {						
					@Override
					public void perform(TriggerInfo triggerInfo) {
						Window.open(actionFolderlink,"_blank","");
					}
				};
				action.setName(folderDisplayName);
				action.setHoverIcon(AssemblyPanelResources.INSTANCE.viewBig());
				action.setIcon(AssemblyPanelResources.INSTANCE.viewBig());
				listAction.add(action);
				
			}
		}
		*/	
		htmlString.append("<td  align='right' class='gxtReset headerbar-link-menu'>");	
		htmlString.append(getLinkMenuHtmlString(folderDisplayName)) ;	
		htmlString.append("</td>");	
		//add static defined links to Dynamic slot Menu
		List<Folder> subFoldersList = subFolder.getSubFolders();
		if (subFoldersList == null || subFoldersList.isEmpty())
			return;
		
		for (Folder subFolder2 : subFoldersList) {
			Resource imageResource = null;
			Icon icon = subFolder2.getIcon();
			if (icon != null) 
				imageResource = GMEIconUtil.getImageFromIcon(icon, iconHeight, iconHeight);
			
			FolderContent folderContent = subFolder2.getContent();
			if (folderContent == null || !(folderContent instanceof HyperlinkAction))
				continue;
			
			HyperlinkAction hyperlinkAction = (HyperlinkAction) folderContent;
			listLocalHyperLinkAction.add(hyperlinkAction);	
			if (imageResource != null) {
				imageResourceMap.put(hyperlinkAction.getUrl(),
						new GmImageResource(imageResource, workbenchSession.resources().url(imageResource).asString()));
			}
		}
	}

	private String prepareSpecialFolder(StringBuilder htmlString, String folderName, String folderDisplayName, String folderLink, String folderIcon,
			String folderTooltip, Folder subFolder, String subFolderName) {
		//add link/no link, text, image
		Menu subMenu = createMenuItems(subFolder, null);

		//if (!folderIcon.isEmpty()) {
		//    htmlString.append("<td align='left' valign='middle'>");								    								    								    
		//} else {
		//	htmlString.append("<td align='right' valign='middle'>");							    	
		//}
											    
		if (subFolderName.equals(HeaderBarConfig.TF_LOGO.getName())) {
			if (folderIcon.isEmpty() && this.oldLogoImage != null) {
				//set default Logo Image
				folderIcon = this.oldLogoImage.getUrl();
			}
			//htmlString.append("<td  align='left' valign='middle'>");	
			htmlString.append("<td  align='left' class='gxtReset headerbar-logo'>");	
		} else if (subFolderName.equals(HeaderBarConfig.TF_TITLE.getName()))
			htmlString.append("<td  align='left' class='gxtReset headerbar-title'>");			
		else {
			//htmlString.append("<td  align='right' valign='middle'>");
			htmlString.append("<td  align='right' class='gxtReset headerbar-link'>");
		}
		
		if (folderLink.isEmpty()) {
			//images, plain text without link
			htmlString.append("<ul class='gxtReset' id='tf_nav'>");	
													
			if (subMenu != null)
				htmlString.append(prepareWidgetMenuFolder( subMenu, folderName, folderIcon, null, folderTooltip, folderDisplayName, 0));	
			else {    									
		        if (!folderIcon.isEmpty()) {
		        	//image without link - link only on logo to HOME page
		        	if (subFolderName.equals(HeaderBarConfig.TF_LOGO.getName()))
					   htmlString.append("<a href='").append(Window.Location.getHref()).append("'>");		  //link to Home page
		        	
		        	htmlString.append("<img class='headerbar-folder-image' src='").append(folderIcon).append("'/>");
		        	
		        	if (subFolderName.equals(HeaderBarConfig.TF_LOGO.getName()))
		        		htmlString.append("</a>");
		        } else {
		        	//text without link
		        	String addClass = "";
		        	if (subFolderName.equals(HeaderBarConfig.TF_TITLE.getName())) 
		        		addClass="class='tfTitle'";
		        	htmlString.append("<li ").append(addClass).append(">").append(folderDisplayName).append("</li>");
		        }									    									
			}
			
			htmlString.append("</ul>");									
		} else {	
			//link
			htmlString.append("<ul class='gxtReset' id='tf_nav'>");	
			
			if (subMenu != null) {
				//htmlString.append("<li class='headerbar-folder-menu enabled'><a class='topBannerAnchor' href='javascript:void(0)'>"+folderDisplayName);
				htmlString.append(prepareWidgetMenuFolder( subMenu, folderName, folderIcon, null, folderTooltip, folderDisplayName, 0));								  
		        //htmlString.append("</a></li>");
				htmlString.append("</ul>");                                    	
			} else {    								
			    htmlString.append("<li><a class='topBannerAnchor' href='").append(folderLink).append("' target='_blank'>");
		        if (!folderIcon.isEmpty()) {
		        	//image with link
					htmlString.append("<img src='").append(folderIcon).append("'/>");                                    	
		        } else {
		        	//text with link
		        	htmlString.append(folderDisplayName); 
		        }
		        
		        htmlString.append("</a></li>");
				htmlString.append("</ul>");                                    	
			}        							
		}
		
		htmlString.append("</td>");
		return folderIcon;
	}
		
	private static void handleMenuFolderClicked(final Menu menu, int x, int y) {
		menu.showAt( x, y);
	}
	
	private static void handleActionFolderClicked(Action action) {
		if (action != null)
			action.perform(null);
	}
	
	
	private Element getParentElement(Element element, int depth, String className) {
		if (depth <= 0 || element == null)
			return null;
		
		if (className.equals(element.getClassName()))
			return element;
		
		return getParentElement(element.getParentElement(), --depth, className);
	}	

	private Element getChildElement(Element element, int depth, String className) {
		if (depth <= 0 || element == null)
			return null;
		
		if (className.equals(element.getClassName()))
			return element;

		NodeList<Element> childList = element.getElementsByTagName("*");
		for (int i=0; i<childList.getLength(); i++) {
		   Element childElement = getChildElement(childList.getItem(i), --depth, className);
			
		   if (childElement != null)
			   return childElement;
		}
		
		return null;
	}	
	
	@Override
	public Widget getHeaderBar() {
		headerCounter++;
		final HtmlPanel headerBar;
		
		if (headerBarHtmlPanel != null)
			headerBar = headerBarHtmlPanel;
		else {
			headerBar = new HtmlPanel();
			headerBarHtmlPanel = headerBar;
			headerBar.setBorders(false);
			headerBar.setBodyBorder(false);		
			headerBar.setHeaderVisible(false);
			headerBar.setId("defaultHeaderBar" + "@" + headerCounter);
		} 
				
		//new QuickTip(headerBar);

		widgetMap.put("quickAccess-slot", quickAccessTriggerField);
		widgetMap.put("globalState-slot", globalStateSlot);
		//this.widgetMap.put("notification-slot", this.notificationSlot);
		widgetMap.put("settingsMenu", settingsMenu);
		widgetMap.put("userMenu", userMenu);
		
		imageResourceMap.put("settingsMenu", defaultSettingsMenuIcon);
		imageResourceMap.put("userMenu", defaultUserMenuIcon);
				
		headerBar.sinkEvents(Event.ONCLICK | Event.ONMOUSEOVER | Event.ONMOUSEOUT );
		
		//ONCLICK Event Handler
		headerBar.addHandler(event -> {
			EventTarget target = event.getNativeEvent().getEventTarget();
			if (!Element.is(target))
				return;
						
			Element actionFolderElement = getParentElement(Element.as(target), 2, "headerbar-folder-menu enabled");
			if (actionFolderElement != null) {
				//GlobalState.showSuccess("Element found2");						
				Menu menuFolder = menuMap.get(actionFolderElement.getId());										
				if (menuFolder != null) 
					handleMenuFolderClicked(menuFolder, actionFolderElement.getAbsoluteLeft(), actionFolderElement.getAbsoluteBottom());
				return;
			}
			
			actionFolderElement = getParentElement(Element.as(target), 3, "headerbar-folder-action enabled");
			if (actionFolderElement != null) {
				Action actionFolder = actionsMap.get(actionFolderElement.getId());
				if (actionFolder != null) {
					handleActionFolderClicked(actionFolder);
				}
				return;  
			}
						
			/*
			actionFolderElement = getParentElement(Element.as(target), 4, "headerbar-button-main enabled");
			if (actionFolderElement != null) {
				for (String key : mapHeaderBarButtons.keySet()) {
					HeaderBarButton button = mapHeaderBarButtons.get(key);
					if (button != null && button.getId().equals(actionFolderElement.getId())) {
						button.fireOnMouseClickListeners();
						break;
					}
				}				
				
				return;
			}
			
			actionFolderElement = getParentElement(Element.as(target), 4, "headerbar-button-menu enabled");
			if (actionFolderElement != null) {
				Menu menuButton = null;
				for (String key : mapHeaderBarButtons.keySet()) {
					HeaderBarButton button = mapHeaderBarButtons.get(key);
					if (button!= null && button.getId().equals(actionFolderElement.getId())) {
						menuButton = button.getMenu();	
					}
				}
				if (menuButton != null)
					menuButton.show(actionFolderElement, new AnchorAlignment(Anchor.TOP_LEFT, Anchor.BOTTOM_LEFT));
				return;
			}			
			*/
			
			actionFolderElement = getParentElement(Element.as(target), 3, "headerbar-folder-menu-image");
			if (actionFolderElement == null)
				return;
			
			//RVE - add extern and local(static) links all together and create all links menu  buttons
			List<HyperlinkAction> listHyperLinkAction = new ArrayList<>();
			for (HyperlinkAction hyperlinkAction1 : listExternHyperLinkAction)
				listHyperLinkAction.add(hyperlinkAction1);
			for (HyperlinkAction hyperlinkAction2 : listLocalHyperLinkAction)
				listHyperLinkAction.add(hyperlinkAction2);								

			final List<Action> listAction = new ArrayList<>();								
			for (HyperlinkAction hyperlinkAction3 : listHyperLinkAction) {
				final String folderLink = hyperlinkAction3.getUrl();
				String folderDisplayName = folderLink;
				LocalizedString displayName = hyperlinkAction3.getDisplayName();
				if (displayName != null)
					folderDisplayName = I18nTools.getLocalized(displayName);
																		
				Action action = new Action() {						
					@Override
					public void perform(TriggerInfo triggerInfo) {
						Window.open(folderLink,"_blank","");
					}
				};
				action.setName(folderDisplayName);
				ImageResource imageResource = null;
				//1. Icon from HyoerLinkAction
				Icon icon = hyperlinkAction3.getIcon();
				if (icon != null) {
					Resource resource = GMEIconUtil.getImageFromIcon(icon, iconHeight, iconHeight);
					if (resource != null)
						imageResource = new GmImageResource(resource, workbenchSession.resources().url(resource).asString()); 
				}
				//2. Icon from Folder
				if (imageResource == null && imageResourceMap.containsKey(folderLink))
					imageResource = imageResourceMap.get(folderLink);
				
				if (imageResource != null) {
					action.setHoverIcon(imageResource);
					action.setIcon(imageResource);										
				} else {
					//Default icon
					action.setHoverIcon(HeaderBarResources.INSTANCE.linkLogo());
					action.setIcon(HeaderBarResources.INSTANCE.linkLogo());									
				}
				listAction.add(action);
			}
						
			HeaderBarMenu headerbBarMenu = new HeaderBarMenu();
			headerbBarMenu.setActionList(listAction);
			headerbBarMenu.setItemSize(listAction.size());
			com.sencha.gxt.widget.core.client.Window headerbarMenu = headerbBarMenu.getMenuWindow();
			headerbarMenu.show();
			//headerbarMenu.alignTo(actionFolderElement, new AnchorAlignment(Anchor.BOTTOM_LEFT, Anchor.TOP_LEFT), 0, 0);								   
			headerbarMenu.alignTo(actionFolderElement, new AnchorAlignment(Anchor.TOP_LEFT, Anchor.BOTTOM_LEFT), 0, 12);
		}, ClickEvent.getType());
					
		//ONMOUSEOVER Event Handler
		headerBar.addHandler(event -> {
			EventTarget target = event.getNativeEvent().getEventTarget();
			if (!Element.is(target))
				return;	
			
			Element actionFolderElement = getParentElement(Element.as(target), 3, "headerbar-folder-action enabled");
			if (actionFolderElement != null) {
				Action action = DefaultHeaderBar.this.actionsMap.get(actionFolderElement.getId());
				if (action != null) {
					Menu menu = menuMap.get(action.getId());
					if (lastShowedMenu != null && lastShowedMenu != menu)
						lastShowedMenu.hide();					
					
					boolean hasVisibleItem = false;
					Iterator<Widget> widgetIterator = menu.iterator();
					while (widgetIterator.hasNext()) {
	                   Widget widget = widgetIterator.next();
	                   if (widget instanceof MenuItem) {
	                	   Action itemAction = ((MenuItem) widget).getData("action");
	                	   hasVisibleItem = itemAction == null ? true : !itemAction.getHidden();
	                	   if (hasVisibleItem)
	                		   break;
	                   }
					}
					
					if (menu.isEnabled() && hasVisibleItem) {
						menu.show(actionFolderElement, new AnchorAlignment(Anchor.TOP_LEFT, Anchor.BOTTOM_LEFT));
						lastShowedMenu = menu;
					}
				}
				return;  
			}
			
		}, MouseOverEvent.getType());

		//ONMOUSEOUT Event Handler
		/*headerBar.addHandler(event -> {
			
		}, MouseOutEvent.getType());*/
		
		return headerBar;
	}

	@Override
	public void onGlobalActionsPrepared() {
		// NOP		
	}

	@Override
	public void onEntityTypeChanged(EntityType<?> entityType) {
		// NOP		
	}

	public void setTestHeaderbarAction(@SuppressWarnings("unused") TestHeaderbarAction testRveAction) {
		//this.testHeaderbarAction = testRveAction;		
	}
}
