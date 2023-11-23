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
package com.braintribe.gwt.gme.gmactionbar.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gm.model.uiaction.ActionFolderContent;
import com.braintribe.gwt.action.adapter.gxt.client.ActionButtonAdapter;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.ActionOrGroup;
import com.braintribe.gwt.action.client.KnownProperties;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.action.client.TriggerKnownProperties;
import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Loader;
import com.braintribe.gwt.gme.actionmenubuilder.client.resources.ActionMenuBuilderResources;
import com.braintribe.gwt.gme.constellation.client.GlobalActionsToolBar;
import com.braintribe.gwt.gme.constellation.client.JsUxComponentOpenerActionHandler;
import com.braintribe.gwt.gme.gmactionbar.client.resources.GmViewActionBarResources;
import com.braintribe.gwt.gme.propertypanel.client.PropertyPanel;
import com.braintribe.gwt.gmresourceapi.client.GmImageResource;
import com.braintribe.gwt.gmview.action.client.ActionFolderContentExpert;
import com.braintribe.gwt.gmview.action.client.ActionGroup;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.action.client.ActionWithoutContext;
import com.braintribe.gwt.gmview.action.client.IgnoreKeyConfigurationDialog;
import com.braintribe.gwt.gmview.actionbar.client.ActionProviderConfiguration;
import com.braintribe.gwt.gmview.actionbar.client.ActionProviderConfiguration.ActionProviderConfigurationListener;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionBar;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionProvider;
import com.braintribe.gwt.gmview.client.GmAmbiguousSelectionSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.GmSelectionSupport;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.client.ParentModelPathSupplier;
import com.braintribe.gwt.gmview.client.SplitAction;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedBorderLayoutContainer;
import com.braintribe.gwt.htmlpanel.client.HtmlPanel;
import com.braintribe.gwt.htmlpanel.client.HtmlPanelListener;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.Profiling;
import com.braintribe.gwt.logging.client.ProfilingHandle;
import com.braintribe.gwt.utils.client.RootKeyNavExpert.RootKeyNavListener;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.folder.FolderContent;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.enhance.EnhancedEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.processing.workbench.action.api.WorkbenchModelAction;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.workbench.KeyConfiguration;
import com.braintribe.model.workbench.TemplateBasedAction;
import com.braintribe.model.workbench.WorkbenchAction;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style.Anchor;
import com.sencha.gxt.core.client.Style.AnchorAlignment;
import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.fx.client.FxElement;
import com.sencha.gxt.fx.client.animation.Fx;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.HasSelectHandlers;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.tips.QuickTip;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;

/**
 * Default implementation of the GmViewActionBar.
 * @author michel.docouto
 *
 */
public class DefaultGmViewActionBar implements GmViewActionBar, InitializableBean, RootKeyNavListener {
	
	private static final String HOME_TETHER_ID = "DefaultGmViewActionBarHomeTether";
	private static final String MORE_MENU_ID ="DefaultGmViewActionBarMoreMenu";
	private static final String SEPARATOR_ID ="DefaultGmViewActionBarSeparator";
	private static final int FX_DELAY = 150;
	private static final int RESIZE_TIMER_DELAY = 50;
	private static int toolBarsCounter = 0;
	private static int externalActionsCounter = 0;
	public static final int GLOBAL_ACTION_WIDTH = 344;
	public static final int GLOBAL_ACTION_TRANSIENT_WIDTH = 452;
	
	private PersistenceGmSession workbenchSession;
	private PersistenceGmSession gmSession;
	private HtmlPanel toolBarView;
	private boolean enableFx = false;
	private BorderLayoutContainer borderLayoutContainer;
	private GlobalActionsToolBar globalActionsToolBar;
	private GmViewActionProvider currentContentView;
	private GmSelectionListener gmSelectionListener;
	private ActionProviderConfiguration currentActionProviderConfiguration;
	private boolean filterExternalActions;
	
	private final Map<String, ActionGroup> actionGroupsMap = new FastMap<>();
	private ActionGroup lastTetherGroup;
	private ImageResource emptyIcon = GmViewActionBarResources.INSTANCE.empty();
	private final Map<String, ActionGroup> tetherElementsMap = new FastMap<>();
	private final Map<ModelAction, String> externalActionsMap = new HashMap<>();
	private final Map<String, Action> preparedActions = new FastMap<>();
	private Function<WorkbenchActionContext<?>, ModelAction> workbenchActionHandlerRegistry;
	private Folder rootFolder;
	private ActionProviderConfigurationListener actionProviderConfigurationListener;
	private Set<GenericEntity> currentSelectedEntities;
	private ManipulationListener manipulationListener;
	private Timer manipulationTimer;
	private Timer resizeTimer;
	private ToolBarElement moreElement;
	private boolean showingMoreMenu = false;
	private final List<MoreMenuElement> moreMenuElements = new ArrayList<>();
	private final Menu moreMenu = new Menu();
	private Loader<Folder> rootFolderLoader;
	private Map<ActionTypeAndName, Boolean> actionAvailabilityMap;
	private BorderLayoutData globalLayoutData;
	private List<Action> actionsWithoutContext;
	private boolean hasActionWithContextVisible;
	private ModelPath rootModelPath;
	private boolean rootModelPathChecked;
	private List<Pair<Action, TemplateBasedAction>> templateBasedActions = new ArrayList<>();
	private Map<ModelAction, KeyConfiguration> actionsWithKeyConfiguration;
	private boolean toolBarVisible = true; 
	private ActionFolderContentExpert actionFolderContentExpert;
	
	/**
	 * Configures the required workbench session.
	 */
	@Override
	@Required
	public void setWorkbenchSession(PersistenceGmSession workbenchSession) {
		this.workbenchSession = workbenchSession;
	}
	
	/**
	 * Configures the required {@link PersistenceGmSession}.
	 */
	@Required
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	/**
	 * Configures the registry for {@link WorkbenchAction}s handlers.
	 */
	@Required
	public void setWorkbenchActionHandlerRegistry(Function<WorkbenchActionContext<?>, ModelAction> workbenchActionHandlerRegistry) {
		this.workbenchActionHandlerRegistry = workbenchActionHandlerRegistry;
	}
	
	/**
	 * Configures the required loader used for loading the root folder.
	 */
	@Required
	public void setRootFolderLoader(Loader<Folder> rootFolderLoader) {
		this.rootFolderLoader = rootFolderLoader;
	}
	
	/**
	 * Configures the required {@link ActionFolderContentExpert} which can additionally configure special action parameters
	 */	
	@Required
	public void setActionFolderContentExpert(ActionFolderContentExpert actionFolderContentExpert) {
		this.actionFolderContentExpert = actionFolderContentExpert;
	}
	
	/**
	 * Configures the GlobalActionsToolBar component, which holds the global actions (and is to be placed in the east side of the action bar).
	 */
	@Configurable
	public void setGlobalActionsToolBar(GlobalActionsToolBar globalActionsToolBar) {
		globalActionsToolBar.setId("gmGlobalActionsToolBar");
		this.globalActionsToolBar = globalActionsToolBar;
	}
	
	/**
	 * Configures whether the FX are enabled when switching between groups. Defaults to false.
	 */
	@Configurable
	public void setEnableFx(boolean enableFx) {
		this.enableFx = enableFx;
	}
	
	/**
	 * Configures an {@link ImageResource} to be used when the action has no icon.
	 */
	@Configurable
	public void setEmptyIcon(ImageResource emptyIcon) {
		this.emptyIcon = emptyIcon;
	}
	
	/**
	 * Configures if ActionBar is visible
	 */
	@Override
	public void setToolBarVisible(boolean toolBarVisible) {
		this.toolBarVisible = toolBarVisible;
		if (this.toolBarView != null)
			this.toolBarView.setVisible(toolBarVisible); 
	}	
	
	@Override
	public void intializeBean() throws Exception {
		borderLayoutContainer = new ExtendedBorderLayoutContainer();
		borderLayoutContainer.setBorders(false);
		
		if (globalActionsToolBar != null) {
			globalLayoutData = new BorderLayoutData(GLOBAL_ACTION_WIDTH);
			globalActionsToolBar.configureParentView(this);
			borderLayoutContainer.setEastWidget(globalActionsToolBar, globalLayoutData);
		}
		
		String toolBarId = "defaultGmViewActionBarToolBar" + toolBarsCounter++ + "@";
		toolBarView = new HtmlPanel();
		toolBarView.setId(toolBarId);
		QuickTip quickTip = new QuickTip(toolBarView);
		ToolTipConfig config = new ToolTipConfig();
		config.setDismissDelay(0);
		quickTip.update(config);
		
		toolBarView.setBorders(false);
		toolBarView.setBodyBorder(false);
		if (this.toolBarVisible)
			borderLayoutContainer.setCenterWidget(toolBarView);
		toolBarView.sinkEvents(Event.ONCLICK);
		toolBarView.addHandler(event -> {
			EventTarget target = event.getNativeEvent().getEventTarget();
			if (Element.is(target))
				handleToolBarClick(Element.as(target));
		}, ClickEvent.getType());
		
		toolBarView.addResizeHandler(event -> getResizeTimer().schedule(RESIZE_TIMER_DELAY));
		toolBarView.addStyleName("toolBarView");
		toolBarView.setBodyStyleName("toolBarViewBody");
		
		moreMenu.setEnableScrolling(false);
		moreMenu.setMinWidth(210);
	}
	
	@Override
	public void load(final AsyncCallback<Void> asyncCallback) {
		final ProfilingHandle ph = Profiling.start(DefaultGmViewActionBar.class, "Loading actionBar folders", true);
		if (rootFolderLoader == null)
			asyncCallback.onSuccess(null);
		else {
			rootFolderLoader.load(AsyncCallbacks.of( //
					result -> {
						if (actionAvailabilityMap != null)
							actionAvailabilityMap.clear();
						rootFolder = result;
						ph.stop();
						asyncCallback.onSuccess(null);
					}, e -> {
						rootFolder = null;
						ph.stop();
						asyncCallback.onSuccess(null);
					}));
		}
		
		//Since we know that at this place the model environment is already set, we then trigger the update visibility of the global actions
		//if (globalActionsToolBar != null)
		//	globalActionsToolBar.updateVisibility();
	}
	
	@Override
	public void onRootKeyPress(NativeEvent evt) {
		if (actionsWithKeyConfiguration == null || !(currentContentView instanceof GmContentView))
			return;
		
		if (isIgnoreActionWithKeyConfiguration(evt))
			return;
		
		for (Map.Entry<ModelAction, KeyConfiguration> entry : actionsWithKeyConfiguration.entrySet()) {
			KeyConfiguration keyConfiguration = entry.getValue();
			if (evt.getKeyCode() != keyConfiguration.getKeyCode() || evt.getAltKey() != keyConfiguration.getAlt()
					|| evt.getShiftKey() != keyConfiguration.getShift() || evt.getCtrlKey() != keyConfiguration.getCtrl()
					|| evt.getMetaKey() != keyConfiguration.getMeta())
				continue;
			
			ModelAction modelAction = entry.getKey();
			modelAction.updateState(((GmContentView) currentContentView).transformSelection(((GmContentView) currentContentView).getCurrentSelection()));
			if (modelAction.getHidden())
				continue;
			
			modelAction.perform(null);
			evt.stopPropagation();
			evt.preventDefault();
			return;
		}
	}
	
	@Override
	public Widget getView() {
		return borderLayoutContainer;
	}
	
	public BorderLayoutData getGlobalLayoutData() {
		return globalLayoutData;
	}
	
	public void layout() {
		layout(borderLayoutContainer);
	}
	
	/**
	 * Checks if we should ignore the action received via a KeyConfiguration event.
	 * For example, we ignore when we are in an input element, or if certain dialogs (such as some error dialog) are being shown.
	 */
	public static boolean isIgnoreActionWithKeyConfiguration(NativeEvent evt) {
		EventTarget eventTarget = evt.getEventTarget();
		if (!Element.is(eventTarget))
			return false;
		
		Element element = eventTarget.cast();
		String tagName = element.getTagName();
		if ("input".equalsIgnoreCase(tagName) || "textarea".equalsIgnoreCase(tagName))
			return true;
		
		String contentEditable = element.getAttribute("contenteditable");
		if (contentEditable != null && "true".equalsIgnoreCase(contentEditable.toLowerCase()))
			return true;
		
		EventListener eventListener = DOM.getEventListener(element);
		if (eventListener instanceof IgnoreKeyConfigurationDialog)
			return true;
		
		Element externalElementWindow = Document.get().getElementById(JsUxComponentOpenerActionHandler.EXTERNAL_COMPONENT_ID_WINDOW_PREFIX);
		if (externalElementWindow != null)
			return true;
		
		/*Element elementToCheck = getHostIfShadow(element, Document.get());
		while (elementToCheck != null) {
			String id = elementToCheck.getId();
			if (id != null && id.startsWith(ExternalWidgetGmContentView.EXTERNAL_COMPONENT_ID_PREFIX))
				return true;
			
			elementToCheck = elementToCheck.getParentElement();
		}*/
		
		return false;
	}
	
	@Override
	public void prepareActionsForView(GmViewActionProvider view) {
		if (currentContentView == view)
			return;
		
		if (currentContentView instanceof GmSelectionSupport)
			((GmSelectionSupport) currentContentView).removeSelectionListener(getGmSelectionListener());
		
		if (currentActionProviderConfiguration != null) {
			currentActionProviderConfiguration.removeActionProviderConfigurationListener(getActionProviderConfigurationListener());
			currentActionProviderConfiguration = null;
		}
		
		currentContentView = view;
		
		if (currentContentView instanceof GmSelectionSupport)
			((GmSelectionSupport) currentContentView).addSelectionListener(getGmSelectionListener());
		
		if (currentContentView == null) {
			prepareToolBarHtml(null, null);
			return;
		}
		
		if (actionsWithKeyConfiguration != null)
			actionsWithKeyConfiguration.clear();
		else
			actionsWithKeyConfiguration = new HashMap<>();
		
		currentActionProviderConfiguration = currentContentView.getActions();
		filterExternalActions = view.isFilterExternalActions();
		if (currentActionProviderConfiguration != null) {
			currentActionProviderConfiguration.addActionProviderConfigurationListener(getActionProviderConfigurationListener());
			currentActionProviderConfiguration.setActionGroup(prepareRootActionGroup());
		}
	
		if (view instanceof GmSelectionSupport)
			resetToolBar(true);
		else if (currentActionProviderConfiguration != null) {
			Map<String, TextButton> eb = null;
			if (currentActionProviderConfiguration.getExternalButtons() != null)
				eb = prepareMap(currentActionProviderConfiguration.getExternalButtons());
			prepareToolBarHtml(eb, currentActionProviderConfiguration.getActionGroup());
		}
		
		if (currentActionProviderConfiguration != null)
			prepareActionsWithKeyConfigurationMap();
	}
	
	@Override
	public void navigateToAction(ActionTypeAndName actionTypeAndName) {
		if (currentActionProviderConfiguration == null)
			return;
		
		ActionGroup actionGroup = currentActionProviderConfiguration.getActionGroup();
		
		ActionTypeAndName checkActionTypeAndName = new ActionTypeAndName(actionTypeAndName);
		checkActionTypeAndName.setActionName("$" + checkActionTypeAndName.getActionName());
		ActionGroup parentActionGroup = getParentActionGroup(actionGroup, null, checkActionTypeAndName);
		if (parentActionGroup != null && parentActionGroup != actionGroup)
			handleFolderClickedEvent(parentActionGroup);
	}
	
	@Override
	public void navigateToAction(Action action) {
		if (currentActionProviderConfiguration == null)
			return;
		
		ActionGroup actionGroup = currentActionProviderConfiguration.getActionGroup();
		ActionGroup parentActionGroup = getParentActionGroup(actionGroup, null, action);
		if (parentActionGroup != null && parentActionGroup != actionGroup)
			handleFolderClickedEvent(parentActionGroup);
	}
	
	@Override
	public List<TemplateBasedAction> get() {
		List<TemplateBasedAction> availableTemplateBasedActions = new ArrayList<>();
		for (Pair<Action, TemplateBasedAction> pairs : templateBasedActions) {
			Action modelAction = pairs.getFirst();
			if (modelAction.getEnabled() && !modelAction.getHidden())
				availableTemplateBasedActions.add(pairs.getSecond());
		}
		
		return availableTemplateBasedActions;
	}
	
	private void handleToolBarClick(Element target) {
		Element actionFolderElement = getActionElement(target, 2, "actionbar-stack");
		if (actionFolderElement != null) {
			ActionGroup actionGroup = actionGroupsMap.get(actionFolderElement.getId());
			if (actionGroup != null && actionGroup.isComplete())
				handleFolderClicked(actionGroup);
			return;
		}
		
		Element actionElement = getActionElement(target, 6, "actionbar-folder-action enabled");
		if (actionElement != null) {
			handleActionClick(target, actionElement);
			return;
		}
		
		handleActionTetherClick(target);
	}

	private void handleActionTetherClick(Element target) {
		Element tetherElement = getActionElement(target, 3, "actionbar-tether");
		if (tetherElement == null)
			return;
		
		ActionGroup actionGroup = tetherElementsMap.get(tetherElement.getId());
		if (actionGroup == null || !actionGroup.isComplete()) {
			resetToolBar(true);
			return;
		}
		
		if (lastTetherGroup == actionGroup)
			return;
		
		lastTetherGroup = actionGroup;
		Element toolBarDom = toolBarView.getElement();
		NodeList<Element> nodeList = toolBarDom.getElementsByTagName("ul");
		Element ulElement = nodeList.getItem(0);
		int tetherIndex = 0;
		int size = ulElement.getChildCount();
		for (int i = 0; i < size; i++) {
			 Element element = (Element) ulElement.getChild(i);
			 if (tetherElement.getId().equals(((Element) element.getFirstChild()).getId())) {
				tetherIndex = i;
				break;
			 }
		}

		for (int i = size - 1; i >= tetherIndex; i--)
			ulElement.removeChild(ulElement.getChild(i));
		
		Map<String, Widget> widgetMap = new FastMap<>();
		ToolBarElement toolBarElement = prepareFolderElements(actionGroup, toolBarView.getId(),
				prepareMap(currentActionProviderConfiguration.getExternalButtons()), new ArrayList<>(), widgetMap);
		
		if (toolBarElement != null)
			toolBarElement.getElements().forEach(e -> ulElement.appendChild(e));
		
		if (!widgetMap.isEmpty())
			toolBarView.addWidgetsAfterRender(widgetMap);
	}

	private void handleActionClick(Element target, Element actionElement) {
		if (actionElement.getId().contains(MORE_MENU_ID)) {
			moreMenu.show(actionElement, new AnchorAlignment(Anchor.TOP_LEFT, Anchor.BOTTOM_LEFT));
			return;
		}
		
		ActionGroup actionGroup = actionGroupsMap.get(actionElement.getId());
		if (actionGroup == null || !actionGroup.isComplete())
			return;
		
		if (actionGroup.getAction() == null) {
			handleFolderClicked(actionGroup);
			return;
		}
		
		TriggerInfo triggerInfo = new TriggerInfo();
		triggerInfo.put(TriggerKnownProperties.PROPERTY_CLICKEDELEMENT, actionElement);
		if (target.getClassName().contains("splitAction"))
			triggerInfo.put(SplitAction.SPLIT_CLICKED_PROPERTY, true);
		actionGroup.getAction().perform(triggerInfo);
		if (!target.getClassName().contains("splitAction") && lastTetherGroup != null) {
			ActionGroup group = lastTetherGroup;
			boolean fxEnabled = enableFx;
			enableFx = false;
			resetToolBar(true);
			handleFolderClicked(group);
			enableFx = fxEnabled;
		}
	}
	
	private GmSelectionListener getGmSelectionListener() {
		if (gmSelectionListener == null)
			gmSelectionListener = gmSelectionSupport -> handleSelectionChanged(currentActionProviderConfiguration, true, gmSelectionSupport);
		
		return gmSelectionListener;
	}
	
	private void handleSelectionChanged(ActionProviderConfiguration actionProviderConfiguration, boolean reset, GmSelectionSupport gmSelectionSupport) {
		if (gmSelectionSupport instanceof PropertyPanel) //Ignore selections from the PP.
			return;
		
		if (currentSelectedEntities != null && !currentSelectedEntities.isEmpty()) {
			for (GenericEntity entity : currentSelectedEntities) {
				if (entity instanceof EnhancedEntity) { 
					GmSession session = ((EnhancedEntity) entity).session();
					if (session instanceof PersistenceGmSession)
						((PersistenceGmSession) session).listeners().remove(getManipulationListener());
				}
			}
			currentSelectedEntities.clear();
		}
		
		if (actionProviderConfiguration == null || gmSelectionSupport == null)
			return;
		
		if (reset && lastTetherGroup != null && toolBarView != null)
			resetToolBar(false);
		else if (showingMoreMenu)
			hideMoreMenu();
		
		List<List<ModelPath>> modelPaths;
		if (gmSelectionSupport instanceof GmAmbiguousSelectionSupport)
			modelPaths = ((GmAmbiguousSelectionSupport) gmSelectionSupport).getAmbiguousSelection();
		else
			modelPaths = gmSelectionSupport.transformSelection(gmSelectionSupport.getCurrentSelection());
		
		if (modelPaths != null && !modelPaths.isEmpty()) {
			for (List<ModelPath> modelPathList : modelPaths) {
				for (ModelPath modelPath : modelPathList) {
					if (modelPath == null || modelPath.size() == 0)
						continue;
					
					Object value = modelPath.last().getValue();
					if (!(value instanceof EnhancedEntity))
						continue;
					
					EnhancedEntity entity = (EnhancedEntity) value;
					GmSession session = entity.session();
					if (!(session instanceof PersistenceGmSession))
						continue;
					
					if (currentSelectedEntities == null)
						currentSelectedEntities = new HashSet<>();
					currentSelectedEntities.add(entity);
					((PersistenceGmSession) session).listeners().add(getManipulationListener());
				}
			}
		}
		
		hasActionWithContextVisible = false;
		String toolBarId = toolBarView != null ? toolBarView.getId() : null;
		handleSelectionChanged(actionProviderConfiguration.getActionGroup(), modelPaths, toolBarId);
		
		updateSeparatorVisibility(toolBarId);
	}

	private void updateSeparatorVisibility(String toolBarId) {
		Element separatorElement = Document.get().getElementById(SEPARATOR_ID + toolBarId);
		if (separatorElement == null)
			return;
		
		if (!hasActionWithContextVisible || actionsWithoutContext == null) {
			separatorElement.getStyle().setDisplay(Display.NONE);
			return;
		}
		
		boolean visible = false;
		for (Action actionWithoutContext : actionsWithoutContext) {
			if (actionWithoutContext.getEnabled()) {
				visible = true;
				break;
			}
		}
		
		if (visible)
			separatorElement.getStyle().clearDisplay();
		else
			separatorElement.getStyle().setDisplay(Display.NONE);
	}
	
	private void handleSelectionChanged(ActionGroup actionGroup, List<List<ModelPath>> modelPaths, String toolBarId) {
		if (actionGroup == null)
			return;
		
		Action action = actionGroup.getAction();
		if (action != null) {
			if (toolBarView != null) {
				String actionId = externalActionsMap.get(action);
				if (actionId != null)
					prepareActionAdapter(action, actionId);
				else
					prepareActionAdapter(action, toolBarId);
			}
			if (action instanceof ModelAction)
				((ModelAction) action).updateState(modelPaths);
			
			boolean hidden = action.getHidden();
			
			if (!hasActionWithContextVisible && !hidden && !(action instanceof ActionWithoutContext))
				hasActionWithContextVisible = true;
		}
		
		List<ActionGroup> actionList = actionGroup.getActionList();
		if (actionList == null || actionList.isEmpty())
			return;
		
		actionList.forEach(ag -> handleSelectionChanged(ag, modelPaths, toolBarId));
		
		if (action == null && toolBarId != null) {
			String folderId = prepareId(actionGroup, toolBarId, false);
			Element folderElement = Document.get().getElementById(folderId);
			if (folderElement != null) {
				if (isHideFolder(actionList))
					folderElement.getStyle().setDisplay(Display.NONE);
				else
					folderElement.getStyle().clearDisplay();
			}
		}
	}
	
	private boolean isHideFolder(List<ActionGroup> children) {
		if (children == null)
			return true;
		
		Map<String, TextButton> externalButtons = null;
		if (currentActionProviderConfiguration != null)
			externalButtons = prepareMap(currentActionProviderConfiguration.getExternalButtons());
		
		for (ActionGroup child : children) {
			Action action = child.getAction();
			if (action != null) {
				if (!action.getHidden())
					return false;
			} else {
				String childName = child.getActionName();
				if (!child.isComplete() && externalButtons != null && childName != null && !childName.isEmpty()) {
					TextButton externalButton = externalButtons.get(childName.substring(1));
					if (externalButton != null && !isComponentHidden(externalButton))
						return false;
				}
				
				if (!isHideFolder(child.getActionList()))
					return false;
			}
		}
		
		return true;
	}
	
	private Element getActionElement(Element clickedElement, int depth, String className) {
		if (depth <= 0 || clickedElement == null)
			return null;
		
		String elementClassName = clickedElement.getClassName();
		if (elementClassName != null && elementClassName.contains(className))
			return clickedElement;
		
		return getActionElement(clickedElement.getParentElement(), --depth, className);
	}
	
	private void handleFolderClicked(final ActionGroup actionGroup) {
		if (lastTetherGroup == actionGroup || actionGroup.getActionList() == null)
			return;
		
		if (!enableFx) {
			handleFolderClickedEvent(actionGroup);
			return;
		}
		
		Fx fx = new Fx(FX_DELAY);
		fx.addAfterAnimateHandler(event -> {
			handleFolderClickedEvent(actionGroup);
			Scheduler.get().scheduleDeferred(() -> toolBarView.getElement().<FxElement>cast().fadeToggle(new Fx(FX_DELAY)));
		});
		toolBarView.getElement().<FxElement>cast().fadeToggle(fx);
	}
	
	private void handleFolderClickedEvent(final ActionGroup actionGroup) {
		if (showingMoreMenu)
			hideMoreMenu();
		addHomeTether();
		int firstActionLiElementIndex = addFolderTether(actionGroup);
		lastTetherGroup = actionGroup;
		
		Element toolBarDom = toolBarView.getElement();
		NodeList<Element> nodeList = toolBarDom.getElementsByTagName("ul");
		final Element ulElement = nodeList.getItem(0);
		
		int size = ulElement.getChildCount();
		for (int i = size - 1; i > firstActionLiElementIndex; i--)
			ulElement.removeChild(ulElement.getChild(i));
		
		Map<String, Widget> widgetMap = new FastMap<>();
		ToolBarElement toolBarElement = prepareFolderElements(actionGroup, toolBarView.getId(),
				prepareMap(currentActionProviderConfiguration.getExternalButtons()), new ArrayList<>(), widgetMap);
		
		if (toolBarElement != null)
			toolBarElement.getElements().forEach(e -> ulElement.appendChild(e));
		
		for (ActionGroup childActionGroup : actionGroup.getActionList()) {
			if (childActionGroup.getAction() == null)
				continue;
			
			String actionId = externalActionsMap.get(childActionGroup.getAction());
			if (actionId != null)
				prepareActionAdapter(childActionGroup.getAction(), actionId);
			else
				prepareActionAdapter(childActionGroup.getAction(), toolBarView.getId());
		}
		
		if (!widgetMap.isEmpty())
			toolBarView.addWidgetsAfterRender(widgetMap);
		getResizeTimer().schedule(RESIZE_TIMER_DELAY);
	}
	
	private void prepareToolBarHtml(Map<String, TextButton> externalButtons, ActionGroup actionGroup) {
		templateBasedActions.clear();
		
		StringBuilder htmlString = new StringBuilder();
		htmlString.append("<html><body><ul class='gxtReset actionbar-toolbar'>");
		
		List<TextButton> externalButtonsList = new ArrayList<>();
		Map<String, Widget> buttonsMap = null;
		if (externalButtons != null) {
			externalButtonsList.addAll(externalButtons.values());
			buttonsMap = new FastMap<>();
		}
		
		if (actionGroup != null) {
			handleIncompleteActionGroups(actionGroup.getActionList(), true, externalButtons, externalButtonsList);
			
			ToolBarElement folderElements = prepareFolderElements(actionGroup, toolBarView.getId(), externalButtons, externalButtonsList, buttonsMap);
			if (folderElements != null)
				htmlString.append(folderElements.getHtml());
		}
		
		if (externalButtons != null) {
			for (final TextButton button : externalButtonsList) {
				ModelAction modelAction = new ModelAction() {
					@Override
					public void perform(TriggerInfo triggerInfo) {
						button.fireEvent(new SelectEvent());
					}
					
					@Override
					protected void updateVisibility() {
						setHidden(isComponentHidden(button));
						getResizeTimer().schedule(RESIZE_TIMER_DELAY);
					}
				};
				modelAction.setHoverIcon(button.getIcon());
				modelAction.setIcon(ActionMenuBuilderResources.INSTANCE.empty());
				modelAction.setName(button.getText());
				modelAction.setHidden(isComponentHidden(button));
				modelAction.put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
				ActionGroup buttonGroup = new ActionGroup(modelAction);
				String id = "externalAction" + Integer.toString(externalActionsCounter++) + "@";
				externalActionsMap.put(modelAction, id);
				ToolBarElement externalButtonElement = prepareNormalAction(buttonGroup, id);
				htmlString.append(externalButtonElement.getHtml());
				
				ActionButtonAdapter.linkActionToButton(modelAction, button); //We needed this so the action is updated when the button visibility changes.
				
//				htmlString.append("<li><div id='").append(id).append("'/></li>");
//				buttonsMap.put(id, button);
			}
		}
		
		htmlString.append("</ul></body></html>");
		
		if (showingMoreMenu)
			hideMoreMenu();
		toolBarView.setHtml(htmlString.toString());
		toolBarView.clearWidgets();
		if (buttonsMap != null)
			toolBarView.setWidgetMap(buttonsMap);
		toolBarView.addHtmlPanelListener(new HtmlPanelListener() {
			@Override
			public void onLayoutBuilt() {
				getResizeTimer().schedule(RESIZE_TIMER_DELAY);
				final HtmlPanelListener listener = this;
				Scheduler.get().scheduleDeferred(() -> {
					toolBarView.removeHtmlPanelListener(listener);
					externalActionsMap.entrySet().forEach(entry -> prepareActionAdapter(entry.getKey(), entry.getValue()));
				});
			}
		});
		
		toolBarView.init();
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
	
	private void addHomeTether() {
		String html = toolBarView.getElement().getInnerHTML();
		if (html.contains(HOME_TETHER_ID))
			return;
		
		Element toolBarElement = toolBarView.getElement();
		NodeList<Element> nodeList = toolBarElement.getElementsByTagName("ul");
		Element ulElement = nodeList.getItem(0);
		
		LIElement liElement = Document.get().createLIElement();
		DivElement divElement = Document.get().createDivElement();
		divElement.setClassName("actionbar-tether");
		String id = HOME_TETHER_ID + toolBarView.getId();
		divElement.setId(id);
		
		DivElement childDivElement = Document.get().createDivElement();
		childDivElement.setInnerText(LocalizedText.INSTANCE.home());
		childDivElement.setClassName("actionbar-text");
		
		ImageElement imageElement = Document.get().createImageElement();
		imageElement.setSrc(GmViewActionBarResources.INSTANCE.home().getSafeUri().asString());
		imageElement.setClassName("actionbar-image");
		
		divElement.appendChild(imageElement);
		divElement.appendChild(childDivElement);
		liElement.appendChild(divElement);
		ulElement.insertFirst(liElement);
	}
	
	private int addFolderTether(ActionGroup actionGroup) {
		String name = getActionName(actionGroup);
		String toolTip = getActionToolTip(actionGroup);
		String iconUrl = getIconUrl(actionGroup);
		
		Element toolBarElement = toolBarView.getElement();
		NodeList<Element> nodeList = toolBarElement.getElementsByTagName("ul");
		Element ulElement = nodeList.getItem(0);
		
		Element firstActionLiElement = null;
		int firstActionLiElementIndex = 0;
		for (int i = 0; i < ulElement.getChildCount(); i++) {
			Element liElement = (Element) ulElement.getChild(i);
			Element divElement = (Element) liElement.getFirstChild();
			if (!divElement.getClassName().equals("actionbar-tether")) {
				firstActionLiElement = liElement;
				firstActionLiElementIndex = i;
				break;
			}
		}
		
		LIElement liElement = Document.get().createLIElement();
		DivElement divElement = Document.get().createDivElement();
		divElement.setClassName("actionbar-tether");
		String id = prepareTetherId(actionGroup, toolBarView.getId());
		divElement.setId(id);
		tetherElementsMap.put(id, actionGroup);
		
		DivElement childDivElement = Document.get().createDivElement();
		childDivElement.setInnerText(name);
		if (toolTip != null)
			childDivElement.setAttribute("qtip", toolTip);
		childDivElement.setClassName("actionbar-text");
		
		ImageElement imageElement = Document.get().createImageElement();
		imageElement.setSrc(iconUrl);
		imageElement.setClassName("actionbar-image");
		
		divElement.appendChild(imageElement);
		divElement.appendChild(childDivElement);
		liElement.appendChild(divElement);
		ulElement.insertBefore(liElement, firstActionLiElement);
		
		return firstActionLiElementIndex;
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
	
	private ImageResource getActionSmallIcon(ActionGroup actionGroup) {
		ImageResource icon = null;
		Icon actionGroupIcon = actionGroup.getIcon();
		if (actionGroupIcon != null) {
			Resource resource = GMEIconUtil.getSmallImageFromIcon(actionGroupIcon);
			if (resource != null)
				icon = new GmImageResource(resource, workbenchSession.resources().url(resource).asString());
		} else {
			Action action = actionGroup.getAction();
			if (action != null)
				icon = action.getIcon();
		}
		
		return icon;
	}
	
	private String getActionToolTip(ActionGroup actionGroup) {
		Action action = actionGroup.getAction();
		if (action != null)
			return SafeHtmlUtils.htmlEscape(action.getTooltip());
		
		return null;
	}
	
	private String getIconUrl(ActionGroup actionGroup) {
		String iconUrl = null;
		try {
			Icon icon = actionGroup.getIcon();
			if (icon != null) {
				Resource imageResource = GMEIconUtil.getLargestImageFromIcon(icon);
				if (imageResource != null) {
					Action action = actionGroup.getAction();
					GmImageResource gmImageResource = new GmImageResource(imageResource, workbenchSession.resources().url(imageResource).asString());
					if (action != null)
						action.setHoverIcon(gmImageResource);
					iconUrl = gmImageResource.getSafeUri().asString();
				}
			} else {
				Action action = actionGroup.getAction();
				if (action != null) {
					ImageResource hoverIcon = action.getHoverIcon();
					if (hoverIcon != null)
						iconUrl = hoverIcon.getSafeUri().asString();
				}
			}
		} catch (Exception ex) {
			iconUrl = null;
		}
		
		if (iconUrl == null)
			iconUrl = emptyIcon.getSafeUri().asString();
		
		return iconUrl;
	}
	
	private String prepareTetherId(ActionGroup actionGroup, String toolBarId) {
		String actionPartId;
		Action action = actionGroup.getAction();
		if (action != null)
			actionPartId = action.getClass().getName() + System.identityHashCode(action);
		else {
			String name = actionGroup.getActionName();
			actionPartId = (name == null ? "" : name) + actionGroup.getActionList().size();
		}
		
		actionPartId = actionPartId.replace("$", "");
		return "tether_" + actionPartId + "_" + toolBarId;
	}
	
	private ToolBarElement prepareFolderElements(ActionGroup actionGroup, String toolBarId, Map<String, TextButton> externalButtons,
			List<TextButton> externalButtonsList, Map<String, Widget> buttonsMap) {
		if (actionGroup == null)
			return null;
		
		List<ActionGroup> actionList = actionGroup.getActionList();
		if (actionList == null || actionList.isEmpty())
			return null;
		
		StringBuilder builder = new StringBuilder();
		
		List<Element> elements = new ArrayList<>();
		List<ToolBarElement> noContextElements = new ArrayList<>();
		
		if (actionsWithoutContext == null)
			actionsWithoutContext = new ArrayList<>();
		else
			actionsWithoutContext.clear();
		
		StringBuilder actionsWithContextBuilder = new StringBuilder();
		
		for (ActionGroup actionGroupChild : actionList) {
			ToolBarElement element = prepareToolBarElement(actionGroupChild, toolBarId, externalButtons, externalButtonsList, buttonsMap);
			if (element != null) {
				if (actionGroupChild.getAction() instanceof ActionWithoutContext) {
					noContextElements.add(element);
					actionsWithoutContext.add(actionGroupChild.getAction());
				} else {
					actionsWithContextBuilder.append(element.getHtml());
					elements.addAll(element.getElements());
				}
			}
		}
		
		if (!noContextElements.isEmpty()) {
			int index = 0;
			for (ToolBarElement element : noContextElements) {
				builder.append(element.getHtml());
				elements.addAll(index++, element.getElements());
			}
			
			ToolBarElement separatorToolBarElement = prepareSeparatorToolBarElement(toolBarId);
			builder.append(separatorToolBarElement.getHtml());
			elements.addAll(separatorToolBarElement.getElements());
		}
		
		builder.append(actionsWithContextBuilder.toString());
		
		if (builder.length() != 0)
			return new ToolBarElement(builder.toString(), elements);
		
		return null;
	}
	
	private ToolBarElement prepareToolBarElement(ActionGroup actionGroup, String toolBarId, Map<String, TextButton> externalButtons,
			List<TextButton> externalButtonsList, Map<String, Widget> buttonsMap) {
		ToolBarElement toolBarElement = null;
		if (actionGroup.isComplete()) {
			Action action = actionGroup.getAction();
			List<ActionGroup> actionList = actionGroup.getActionList();
			if (action != null) {
				if (ModelAction.actionBelongsToPosition(action, ModelActionPosition.ActionBar)) {
					if (actionList != null && !actionList.isEmpty())
						toolBarElement = prepareActionFolder(actionGroup, toolBarId);
					else
						toolBarElement = prepareNormalAction(actionGroup, toolBarId);
				}
			} else if (actionList != null && !actionList.isEmpty())
				toolBarElement = prepareNormalAction(actionGroup, toolBarId);
		} else if (externalButtons != null) {
			String actionName = actionGroup.getActionName().substring(1);
			TextButton button = externalButtons.get(actionName);
			if (button != null) {
				externalButtonsList.remove(button);
				StringBuilder builder = new StringBuilder();
				LIElement liElement = Document.get().createLIElement();
				String id = "externalAction" + Integer.toString(externalActionsCounter++) + "@";
				builder.append("<div id='").append(id).append("'/>");
				actionGroupsMap.put(id, actionGroup);
				buttonsMap.put(id, button);
				String innerHtml = builder.toString();
				liElement.setInnerHTML(innerHtml);
				
				toolBarElement = new ToolBarElement("<li>" + innerHtml + "</li>", Collections.singletonList((Element) liElement));
			}
		}
		
		return toolBarElement;
	}
	
	private ToolBarElement prepareActionFolder(ActionGroup actionGroup, String toolBarId) {
		StringBuilder builder = new StringBuilder();
		LIElement liElement = Document.get().createLIElement();
		builder.append("<div class='actionbar-folder-action");
		if (actionGroup.getAction() == null || actionGroup.getAction().getEnabled())
			builder.append(" enabled");
		builder.append("' style='position: relative;");
		if (actionGroup.getAction() != null) {
			if (actionGroup.getAction().getHidden())
				builder.append(" display: none;");
			else if (!actionGroup.getAction().getEnabled())
				builder.append(" opacity: 0.3;");
		}
		String id = prepareId(actionGroup, toolBarId, false);
		builder.append("' id='").append(id).append("'");
		actionGroupsMap.put(id, actionGroup);
		builder.append("><img src='").append(getIconUrl(actionGroup)).append("' class='actionbar-image'/>");
		builder.append("<div class='actionbar-text'");
		
		String qTip = getActionToolTip(actionGroup);
		if (qTip != null)
			builder.append(" qtip='").append(qTip).append("'");
		
		builder.append(">").append(getActionName(actionGroup)).append("</div>");
		builder.append("<div class='actionbar-stack' style='position: absolute; width: 100%; bottom: 0px; box-shadow: 2px 2px 5px #888;'");
		String folderId = prepareId(actionGroup, toolBarId, true);
		builder.append("' id='").append(folderId).append("'");
		actionGroupsMap.put(folderId, actionGroup);
		builder.append("/></div></div>");
		String innerHtml = builder.toString();
		liElement.setInnerHTML(innerHtml);
		
		Action action = actionGroup.getAction();
		if (action instanceof WorkbenchModelAction) {
			WorkbenchModelAction workbenchModelAction = (WorkbenchModelAction) action;
			WorkbenchAction workbenchAction = workbenchModelAction.getWorkbenchAction();
			if (workbenchAction instanceof TemplateBasedAction)
				templateBasedActions.add(new Pair<>(action, (TemplateBasedAction) workbenchAction));
		}
		
		return new ToolBarElement("<li>" + innerHtml + "</li>", Collections.singletonList((Element) liElement));
	}
	
	private ToolBarElement prepareSeparatorToolBarElement(String toolBarId) {
		StringBuilder builder = new StringBuilder();
		LIElement liElement = Document.get().createLIElement();
		builder.append("<div class='actionbar-separator' id='");
		builder.append(SEPARATOR_ID).append(toolBarId).append("'>");
		builder.append("<img src='").append(GmViewActionBarResources.INSTANCE.separator().getSafeUri().asString()).append("'/>");
		builder.append("</div>");
		
		String innerHtml = builder.toString();
		liElement.setInnerHTML(innerHtml);
		
		return new ToolBarElement("<li>" + innerHtml + "</li>", Collections.singletonList((Element) liElement));
	}
	
	private String prepareId(ActionGroup actionGroup, String toolBarId, boolean folder) {
		String actionPartId;
		Action action = actionGroup.getAction();
		if (action != null)
			actionPartId = action.getClass().getName() + System.identityHashCode(action);
		else {
			String name = actionGroup.getActionName();
			actionPartId = (name == null ? "" : name) + actionGroup.getActionList().size();
		}
		
		actionPartId = actionPartId.replace("$", "");
		return actionPartId + (folder ? "_folder_" : "_") + toolBarId;
	}
	
	private ToolBarElement prepareNormalAction(ActionGroup actionGroup, String toolBarId) {
		StringBuilder builder = new StringBuilder();
		LIElement liElement = Document.get().createLIElement();
		
		builder.append("<div class='actionbar-folder-action");
		Action action = actionGroup.getAction();
		if (action == null || action.getEnabled())
			builder.append(" enabled");
		if (action instanceof SplitAction)
			builder.append(" split");
		builder.append("' style='position: relative;");
		if (action == null || action.getHidden())
			builder.append(" display: none;");
		if (action != null && !action.getEnabled())
			builder.append(" opacity: 0.3;");
		builder.append("'");
		String id = prepareId(actionGroup, toolBarId, false);
		builder.append(" id='").append(id).append("'>");
		actionGroupsMap.put(id, actionGroup);
		if (action instanceof SplitAction) {
			builder.append("<table sytle='width: 100%;' cellspacing='0'><tr><td class='gxtReset' style='width: 58px'>");
			builder.append("<img src='").append(getIconUrl(actionGroup)).append("' class='actionbar-image'/>");
			builder.append("</td><td class='gxtReset splitAction'></td></tr><tr><td class='gxtReset'>");
			builder.append("<div class='actionbar-text actionbar-text-split'");
			String qTip = getActionToolTip(actionGroup);
			if (qTip != null)
				builder.append(" qtip='").append(qTip).append("'");
			
			builder.append(">").append(getActionName(actionGroup)).append("</div>");
			builder.append("</td><td class='gxtReset splitAction'></td></tr></table>");
		} else {
			builder.append("<img src='").append(getIconUrl(actionGroup)).append("' class='actionbar-image'/>");
			builder.append("<div class='actionbar-text'");
			String qTip = getActionToolTip(actionGroup);
			if (qTip != null)
				builder.append(" qtip='").append(qTip).append("'");
			
			builder.append(">").append(getActionName(actionGroup)).append("</div>");
		}
		builder.append("</div>");
		String innerHtml = builder.toString();
		liElement.setInnerHTML(innerHtml);
		
		if (action instanceof WorkbenchModelAction) {
			WorkbenchModelAction workbenchModelAction = (WorkbenchModelAction) action;
			WorkbenchAction workbenchAction = workbenchModelAction.getWorkbenchAction();
			if (workbenchAction instanceof TemplateBasedAction)
				templateBasedActions.add(new Pair<>(action, (TemplateBasedAction) workbenchAction));
		}
		
		return new ToolBarElement("<li>" + innerHtml + "</li>", Collections.singletonList((Element) liElement));
	}
	
	private void prepareActionAdapter(Action action, String toolBarId) {
		String actionElementId = prepareId(new ActionGroup(action), toolBarId, false);
		if (!preparedActions.containsKey(actionElementId)) {
			Element element = Document.get().getElementById(actionElementId);
			if (element != null) {
				preparedActions.put(actionElementId, action);
				linkActionToElement(action, element, actionElementId, emptyIcon);
			}
		}
	}
	
	private void resetToolBar(final boolean handleSelection) {
		if (!enableFx) {
			resetToolBarElements(handleSelection);
			return;
		}
		
		Fx fx = new Fx(FX_DELAY);
		fx.addAfterAnimateHandler(event -> {
			resetToolBarElements(handleSelection);
			Scheduler.get().scheduleDeferred(() -> toolBarView.getElement().<FxElement>cast().fadeToggle(new Fx(FX_DELAY)));
		});
		toolBarView.getElement().<FxElement>cast().fadeToggle(fx);
	}
	
	private void resetToolBarElements(boolean handleSelection) {
		lastTetherGroup = null;
		String toolBarId = toolBarView.getId();
		
		List<String> idsToRemove = new ArrayList<>();
		actionGroupsMap.keySet().stream().filter(id -> id.contains(toolBarId)).forEach(id -> idsToRemove.add(id));
		idsToRemove.forEach(id -> actionGroupsMap.remove(id));
		idsToRemove.clear();
		
		tetherElementsMap.keySet().stream().filter(id -> id.contains(toolBarId)).forEach(id -> idsToRemove.add(id));
		idsToRemove.forEach(id -> tetherElementsMap.remove(id));
		idsToRemove.clear();
		
		List<Action> actionsToAdapt = new ArrayList<>();
		preparedActions.entrySet().stream().filter(entry -> entry.getKey().contains(toolBarId)).forEach(entry -> {
			idsToRemove.add(entry.getKey());
			actionsToAdapt.add(entry.getValue());
		});

		idsToRemove.forEach(id -> preparedActions.remove(id));
		
		if (currentActionProviderConfiguration != null) {
			Map<String, TextButton> externalButtons = prepareMap(currentActionProviderConfiguration.getExternalButtons());
			prepareToolBarHtml(externalButtons, currentActionProviderConfiguration.getActionGroup());
		} else {
			//RVE - need reset toolbar as empty one 
			prepareToolBarHtml(null, null);
		}
		
		for (Action actionToAdapt : actionsToAdapt) {
			String actionId = externalActionsMap.get(actionToAdapt);
			if (actionId != null)
				prepareActionAdapter(actionToAdapt, actionId);
			else
				prepareActionAdapter(actionToAdapt, toolBarId);
		}
		
		if (handleSelection && currentContentView instanceof GmSelectionSupport)
			handleSelectionChanged(currentActionProviderConfiguration, false, (GmSelectionSupport) currentContentView);
		else
			updateSeparatorVisibility(toolBarId);			
	}
	
	private ActionGroup getParentActionGroup(ActionGroup currentActionGroup, ActionGroup parentActionGroup, ActionTypeAndName actionTypeAndName) {
		if (actionTypeAndName.equals(currentActionGroup.getActionTypeAndName()))
			return parentActionGroup;
		
		List<ActionGroup> actionList = currentActionGroup.getActionList();
		if (actionList != null) {
			for (ActionGroup childGroup : actionList) {
				ActionGroup parentAction = getParentActionGroup(childGroup, currentActionGroup, actionTypeAndName);
				if (parentAction != null)
					return parentAction;
			}
		}
		
		return null;
	}
	
	private ActionGroup getParentActionGroup(ActionGroup currentActionGroup, ActionGroup parentActionGroup, Action action) {
		if (action == currentActionGroup.getAction())
			return parentActionGroup;
		
		List<ActionGroup> actionList = currentActionGroup.getActionList();
		if (actionList != null) {
			for (ActionGroup childGroup : actionList) {
				ActionGroup parentAction = getParentActionGroup(childGroup, currentActionGroup, action);
				if (parentAction != null)
					return parentAction;
			}
		}
		
		return null;
	}
	
	private ActionGroup prepareRootActionGroup() {
		Map<ActionTypeAndName, ModelAction> knownActions = prepareActionMap(currentActionProviderConfiguration.getExternalActions());
		List<ModelAction> knownActionsUsed = new ArrayList<>();
		List<ActionGroup> actionList = new ArrayList<>();
		
		if (rootFolder != null) {
			List<Folder> subFolders = rootFolder.getSubFolders();
			if (subFolders != null) {
				for (Folder subFolder : subFolders) {
					ActionGroup actionGroup = prepareActionGroup(subFolder, knownActions, knownActionsUsed,
							currentActionProviderConfiguration.getGmContentView());
					if (actionGroup != null)
						actionList.add(actionGroup);
				}
			}
		}
		
		if (knownActions != null) {
			List<ModelAction> allKnownActions = new ArrayList<>(knownActions.values());
			allKnownActions.removeAll(knownActionsUsed);

			allKnownActions.forEach(modelAction -> actionList.add(new ActionGroup(modelAction)));
		}

		return actionList.isEmpty() ? null : new ActionGroup(null, actionList);
	}
	
	private ActionGroup prepareActionGroup(Folder folder, Map<ActionTypeAndName, ModelAction> knownActions, List<ModelAction> knownActionsUsed,
			GmContentView gmContentView) {
		ModelAction action = null;
		List<ActionGroup> actionList = new ArrayList<>();
		Icon icon = folder.getIcon();
		LocalizedString displayName = folder.getDisplayName();
		String name = displayName != null ? I18nTools.getLocalized(displayName) : null;
		FolderContent content = folder.getContent();
		if (content instanceof WorkbenchAction)
			action = prepareModelAction((WorkbenchAction) content, folder, gmContentView);
		
		EntityType<? extends ActionFolderContent> denotationType = null;
		if (folder.getContent() instanceof ActionFolderContent)
			denotationType = folder.getContent().entityType();
		
		ActionTypeAndName actionTypeAndName = new ActionTypeAndName(denotationType, folder.getName());
		if (action == null) {
			action = getDefaultAction(actionTypeAndName, knownActions);
			if (action != null) {
				knownActionsUsed.add(action);
				if (folder.getContent() instanceof ActionFolderContent && actionFolderContentExpert != null) { 
					actionFolderContentExpert.getConfiguredAction((ActionFolderContent) folder.getContent(), action);
					if  (((ActionFolderContent) folder.getContent()).getKeyConfiguration() != null)
						actionsWithKeyConfiguration.put(action, ((ActionFolderContent) folder.getContent()).getKeyConfiguration());
				}
			}
		}
		List<Folder> subFolders = folder.getSubFolders();
		if (subFolders != null) {
			for (Folder subFolder : subFolders) {
				ActionGroup actionGroup = prepareActionGroup(subFolder, knownActions, knownActionsUsed, gmContentView);
				if (actionGroup != null)
					actionList.add(actionGroup);
			}
		}
		
		if (action != null || !actionList.isEmpty())
			return new ActionGroup(action, actionList, icon, new ActionTypeAndName(denotationType, name));
		else {
			String folderName = folder.getName();
			if (folderName != null && !folderName.isEmpty())
				return new ActionGroup(null, null, icon, actionTypeAndName, name, false);
		}
		
		return null;
	}
	
	private ModelAction prepareModelAction(WorkbenchAction workbenchAction, Folder folder, GmContentView gmContentView) {
		ModelAction modelAction = workbenchActionHandlerRegistry.apply(prepareWorkbenchActionContext(workbenchAction, folder, gmContentView));
		if (modelAction != null) {
			modelAction.configureGmContentView(gmContentView);
			modelAction.setHidden(true);
		}
		
		return modelAction;
	}
	
	private ModelAction getDefaultAction(ActionTypeAndName folderTypeAndName, Map<ActionTypeAndName, ModelAction> knownActions) {
		if (knownActions == null || folderTypeAndName == null)
			return null;
		
		String actionName = folderTypeAndName.getActionName();
		ActionTypeAndName checkTypeAndName = new ActionTypeAndName(folderTypeAndName);
		if (actionName != null && !actionName.isEmpty())
			checkTypeAndName.setActionName(actionName.substring(1));
		return knownActions.get(checkTypeAndName);
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
	
	private WorkbenchActionContext<?> prepareWorkbenchActionContext(final WorkbenchAction workbenchAction, final Folder folder, final GmContentView view) {
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
				return DefaultGmViewActionBar.this.getRootModelPath(view, view);
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
	
	private ActionProviderConfigurationListener getActionProviderConfigurationListener() {
		if (actionProviderConfigurationListener != null)
			return actionProviderConfigurationListener;
		
		actionProviderConfigurationListener = newExternalActions -> {
			if (currentActionProviderConfiguration == null)
				return;
			
			Map<ActionTypeAndName, ModelAction> newExternalActionsMap = prepareActionMap(newExternalActions);
			Set<ActionTypeAndName> newActionSet = updateActionGroup(newExternalActions);
			
			Map<ActionTypeAndName, ModelAction> newActionMap = new LinkedHashMap<>();
			for (ActionTypeAndName newAction : newActionSet)
				newActionMap.put(newAction, newExternalActionsMap.get(newAction));
			
			if (!newActionMap.isEmpty()) {
				addExternalActionsToToolBar(newActionMap);
				if (newActionSet.size() != newExternalActions.size())
					resetToolBar(false);
			} else
				resetToolBar(false);
		};
		
		return actionProviderConfigurationListener;
	}
	
	public Set<ActionTypeAndName> updateActionGroup(List<Pair<ActionTypeAndName, ModelAction>> externalActions) {
		ActionGroup actionGroup = currentActionProviderConfiguration.getActionGroup();
		Set<ActionTypeAndName> newActionSet = new LinkedHashSet<>();
		List<ActionGroup> actionList = actionGroup.getActionList();
		if (actionList == null) {
			actionList = new ArrayList<ActionGroup>();
			actionGroup.setActionList(actionList);
		}
		
		for (Pair<ActionTypeAndName, ModelAction> entry : externalActions) {
			ActionTypeAndName checkTypeAndName = new ActionTypeAndName(entry.getFirst());
			checkTypeAndName.setActionName("$" + checkTypeAndName.getActionName());
			ModelAction action = entry.getSecond();
			ActionGroup ag = getParentActionGroup(actionGroup, checkTypeAndName);
			if (ag != null) {
				ag.setAction(action);
				ag.setComplete(true);
			} else {
				actionList.add(new ActionGroup(action, null, null, checkTypeAndName));
				newActionSet.add(entry.getFirst());
			}
		}
		
		return newActionSet;
	}
	
	private ActionGroup getParentActionGroup(ActionGroup actionGroup, ActionTypeAndName actionTypeAndName) {
		if (actionTypeAndName.equals(actionGroup.getActionTypeAndName()))
			return actionGroup;
		
		List<ActionGroup> actionList = actionGroup.getActionList();
		if (actionList != null) {
			for (ActionGroup childGroup : actionList) {
				ActionGroup parentAction = getParentActionGroup(childGroup, actionTypeAndName);
				if (parentAction != null)
					return parentAction;
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	private void addExternalActionsToToolBar(Map<ActionTypeAndName, ModelAction> externalActions) {
		addActionsToToolBar((Collection) externalActions.values());
	}
	
	private void addActionsToToolBar(Collection<ActionOrGroup> actions) {
		if (actions == null)
			return;
		
		String html = toolBarView.getHtml();
		StringBuilder htmlString = new StringBuilder();
		actions.stream().filter(action -> ModelAction.actionBelongsToPosition(action, ModelActionPosition.ActionBar)).forEach(action -> {
			ActionGroup buttonGroup = new ActionGroup((ModelAction) action);
			buttonGroup.setActionTypeAndName(new ActionTypeAndName(action.getName()));
			buttonGroup.setActionList(new ArrayList<>());
			ToolBarElement externalButtonElement = prepareNormalAction(buttonGroup, toolBarView.getId());
			htmlString.append(externalButtonElement.getHtml());
			prepareActionAdapter((ModelAction) action, toolBarView.getId());
		});
		
		if (htmlString.length() == 0)
			return;
		
		int index = html.indexOf("</ul>");
		html = html.substring(0, index) + htmlString.toString() + html.substring(index);
		toolBarView.setHtml(html);
		toolBarView.init();
		toolBarView.addHtmlPanelListener(new HtmlPanelListener() {
			@Override
			public void onLayoutBuilt() {
				getResizeTimer().schedule(RESIZE_TIMER_DELAY);
				final HtmlPanelListener listener = this;
				Scheduler.get().scheduleDeferred(() -> toolBarView.removeHtmlPanelListener(listener));
			}
		});
	}
	
	private ManipulationListener getManipulationListener() {
		if (manipulationListener == null)
			manipulationListener = manipulation -> getManipulationTimer().schedule(300);
		
		return manipulationListener;
	}
	
	private Timer getManipulationTimer() {
		if (manipulationTimer == null) {
			manipulationTimer = new Timer() {
				@Override
				public void run() {
					if (currentActionProviderConfiguration != null && currentContentView instanceof GmSelectionSupport)
						handleSelectionChanged(currentActionProviderConfiguration, false, (GmSelectionSupport) currentContentView);
				}
			};
		}
		
		return manipulationTimer;
	}
	
	private Timer getResizeTimer() {
		if (resizeTimer != null)
			return resizeTimer;
		
		resizeTimer = new Timer() {
			@Override
			public void run() {
				Element toolBar = Document.get().getElementById(toolBarView.getId());
				if (toolBar != null && toolBar.getElementsByTagName("ul").getLength() > 0) {
					Element divElement = toolBar.getElementsByTagName("ul").getItem(0).getParentElement().getParentElement();
					if (divElement != null && divElement.getScrollWidth() - divElement.getOffsetWidth() > 20)
						prepareToolBarWithMoreMenu(toolBar, divElement);
					else if (showingMoreMenu)
						hideOrReduceMoreMenu();
				}
			}
		};
		
		return resizeTimer;
	}
	
	private void prepareToolBarWithMoreMenu(Element toolBar, Element parentDivElement) {
		showingMoreMenu = true;
		Element ulElement = toolBar.getElementsByTagName("ul").getItem(0);
		prepareMoreMenuElement(ulElement);
		
		for (int i = ulElement.getChildCount() - 1; i >= 0; i--) {
			final Element divElement = (Element) ulElement.getChild(i).getFirstChild();
			if (divElement.getId().contains(MORE_MENU_ID) || getElementAlreadyInMoreMenu(divElement) != null)
				continue;
			
			if (externalActionsMap.containsValue(divElement.getId())) {
				externalActionsMap.entrySet().stream().filter(entry -> entry.getValue().equals(divElement.getId())).findFirst()
						.ifPresent(entry -> addExternalActionToMoreMenu(divElement, entry));
			} else {
				if (!isElementVisible(divElement))
					continue;
				addActionToMoreMenu(divElement);
			}
			
			if (parentDivElement.getScrollWidth() <= parentDivElement.getOffsetWidth())
				break;
		}
	}

	private boolean isElementVisible(final Element divElement) {
		boolean visible = false;
		boolean visibilitySet = false;
		Map<String, Widget> widgetMap = toolBarView.getWidgetMap();
		if (widgetMap != null) {
			Widget widget = widgetMap.get(divElement.getId());
			if (widget instanceof Component) {
				visible = !isComponentHidden((Component) widget);
				visibilitySet = true;
			}
		}
		
		if (!visibilitySet)
			visible = !Display.NONE.getCssName().equals(divElement.getStyle().getDisplay());
		return visible;
	}
	
	private void addActionToMoreMenu(final Element divElement) {
		final Action modelAction = preparedActions.get(divElement.getId());
		MoreMenuElement moreMenuElement = new MoreMenuElement(actionGroupsMap.get(divElement.getId()), divElement);
		moreMenuElements.add(moreMenuElement);
		divElement.getStyle().setDisplay(Display.NONE);
		
		String text = "";
		ImageResource icon = GmViewActionBarResources.INSTANCE.empty();
		if (modelAction != null) {
			text = modelAction.getName();
			icon = modelAction.getIcon();
		} else {
			ActionGroup actionGroup = actionGroupsMap.get(divElement.getId());
			if (actionGroup != null) {
				text = getActionName(actionGroup);
				icon = getActionSmallIcon(actionGroup);
				if (actionGroup.getAction() != null)
					icon = actionGroup.getAction().getIcon();
			} else {
				NodeList<Element> childDivs = divElement.getElementsByTagName("div");
				for (int j = 0; j < childDivs.getLength(); j++) {
					Element childDiv = childDivs.getItem(j);
					if (childDiv.getClassName().contains("actionbar-text")) {
						text = childDiv.getInnerText();
						break;
					}
				}
			}
		}
		
		MenuItem menuItem = new MenuItem(text, icon);
		menuItem.addSelectionHandler(event -> {
			moreMenu.hide();
			Map<String, Widget> widgetMap = toolBarView.getWidgetMap();
			if (widgetMap != null) {
				Widget widget = widgetMap.get(divElement.getId());
				if (widget instanceof HasSelectHandlers) {
					widget.fireEvent(new SelectEvent());
					return;
				}
			}
			handleToolBarClick(divElement);
		});
		moreMenuElement.setMenuItem(menuItem);
		moreMenu.insert(menuItem, 0);
	}

	private void addExternalActionToMoreMenu(final Element divElement, final Map.Entry<ModelAction, String> entry) {
		if (entry.getKey().getHidden())
			return;
		
		MoreMenuElement moreMenuElement = new MoreMenuElement(actionGroupsMap.get(divElement.getId()), divElement);
		moreMenuElements.add(moreMenuElement);
		divElement.getStyle().setDisplay(Display.NONE);
		
		MenuItem menuItem = new MenuItem(entry.getKey().getName(), entry.getKey().getHoverIcon());
		menuItem.addSelectionHandler(event -> {
			moreMenu.hide();
			TriggerInfo triggerInfo = new TriggerInfo();
			triggerInfo.put(TriggerKnownProperties.PROPERTY_CLICKEDELEMENT, divElement);
			entry.getKey().perform(triggerInfo);
		});
		moreMenuElement.setMenuItem(menuItem);
		moreMenu.insert(menuItem, 0);
	}
	
	private void hideOrReduceMoreMenu() {
		Element ulElement = moreElement.getElements().get(0).getParentElement();
		int availableWidth = getToolBarAvailableWidth(ulElement);
		
		for (int i = 0; i < ulElement.getChildCount(); i++) {
			Element divElement = (Element) ulElement.getChild(i).getFirstChild();
			
			MoreMenuElement moreMenuElement = getElementAlreadyInMoreMenu(divElement);
			if (moreMenuElement == null || !canElementFit(moreMenuElement, availableWidth))
				continue;
			
			if (isMoreMenuElementVisible(moreMenuElement)) {
				divElement.getStyle().clearDisplay();
				availableWidth -= moreMenuElement.getWidth();
			}
			moreMenuElements.remove(moreMenuElement);
			moreMenu.remove(moreMenuElement.getMenuItem());
			
			if (moreMenuElements.isEmpty()) {
				moreElement.getElements().get(0).getStyle().setDisplay(Display.NONE);
				showingMoreMenu = false;
				getResizeTimer().schedule(RESIZE_TIMER_DELAY);
				return;
			}
		}
	}
	
	private void hideMoreMenu() {
		moreMenuElements.stream().filter(el -> isMoreMenuElementVisible(el)).forEach(el -> el.getElement().getStyle().clearDisplay());
		moreMenuElements.clear();
		moreMenu.clear();
		moreElement.getElements().get(0).getStyle().setDisplay(Display.NONE);
		showingMoreMenu = false;
	}
	
	private ToolBarElement prepareMoreMenuElement(Element ulElement) {
		if (moreElement == null) {
			StringBuilder builder = new StringBuilder();
			LIElement liElement = Document.get().createLIElement();
			builder.append("<div class='actionbar-folder-action enabled actionbar-more' style='position: relative;'");
			String id = MORE_MENU_ID + "_" + toolBarView.getId();
			builder.append(" id='").append(id).append("'");
			builder.append("><img src='").append(GmViewActionBarResources.INSTANCE.more().getSafeUri().asString());
			builder.append("' class='actionbar-image actionbar-more-image'/>");
			builder.append("<div class='actionbar-text'>&nbsp;</div></div>");
			String innerHtml = builder.toString();
			liElement.setInnerHTML(innerHtml);

			ulElement.appendChild(liElement);
			moreElement = new ToolBarElement("<li>" + innerHtml + "</li>", Collections.singletonList((Element) liElement));
		} else {
			Element element = moreElement.getElements().get(0);
			if (element.getParentElement() != ulElement)
				ulElement.appendChild(element);
			element.getStyle().clearDisplay();
		}
		
		return moreElement;
	}
	
	private MoreMenuElement getElementAlreadyInMoreMenu(Element element) {
		return moreMenuElements.stream().filter(el -> element.getId().equals(el.getElement().getId())).findFirst().orElse(null);
	}
	
	private boolean canElementFit(MoreMenuElement moreMenuElement, int availableWidth) {
		if (moreMenuElement.getWidth() + 2 <= availableWidth)
			return true;
		
		return moreMenuElements.size() == 1 && moreMenuElement.getWidth() + 2 <= availableWidth + moreElement.getElements().get(0).getOffsetWidth();
	}
	
	private int getToolBarAvailableWidth(Element ulElement) {
		int usedWidth = 0;
		for (int i = 0; i < ulElement.getChildCount(); i++) {
			Element divElement = (Element) ulElement.getChild(i).getFirstChild();
			if (divElement.getId().contains(MORE_MENU_ID) || getElementAlreadyInMoreMenu(divElement) != null)
				continue;
			
			if (externalActionsMap.containsValue(divElement.getId())) {
				for (final Map.Entry<ModelAction, String> entry : externalActionsMap.entrySet()) {
					if (entry.getValue().equals(divElement.getId())) {
						if (!entry.getKey().getHidden())
							usedWidth += ((Element) ulElement.getChild(i)).getOffsetWidth();
						break;
					}
				}
			} else if (!Display.NONE.getCssName().equals(divElement.getStyle().getDisplay()))
				usedWidth += ((Element) ulElement.getChild(i)).getOffsetWidth();
		}
		
		if (moreMenuElements.size() > 1)
			usedWidth += moreElement.getElements().get(0).getOffsetWidth();
		
		return ulElement.getParentElement().getParentElement().getOffsetWidth() - usedWidth;
	}
	
	private boolean isMoreMenuElementVisible(MoreMenuElement moreMenuElement) {
		Map<String, Widget> widgetMap = toolBarView.getWidgetMap();
		if (widgetMap != null) {
			Widget widget = widgetMap.get(moreMenuElement.getElement().getId());
			if (widget instanceof Component)
				return !isComponentHidden((Component) widget);
		}
		
		if (moreMenuElement.getAction() != null)
			return !moreMenuElement.getAction().getHidden();
		
		return moreMenuElement.getActionGroup() != null ? !isHideFolder(moreMenuElement.getActionGroup().getActionList()) : false;
	}
	
	@SuppressWarnings("unused")
	private void linkActionToElement(Action action, Element element, String elementId, ImageResource emptyIcon) {
		new ElementActionAdapter(action, element, elementId, emptyIcon);
	}
	
	private Map<String, TextButton> prepareMap(List<Pair<String, ? extends Widget>> buttons) {
		Map<String, TextButton> buttonsMap = null;
		
		if (buttons != null) {
			buttonsMap = new LinkedHashMap<>();
			for (Pair<String, ? extends Widget> entry : buttons) {
				if (entry.getSecond() instanceof TextButton) {
					String actionName = entry.getFirst();
					if (!filterExternalActions || isActionAvailable(new ActionTypeAndName(actionName)))
						buttonsMap.put(actionName, (TextButton) entry.getSecond());
				}
			}
		}
		
		return buttonsMap;
	}
	
	private Map<ActionTypeAndName, ModelAction> prepareActionMap(List<Pair<ActionTypeAndName, ModelAction>> externalActions) {
		Map<ActionTypeAndName, ModelAction> actionsMap = null;
		
		if (externalActions != null) {
			actionsMap = new LinkedHashMap<>();
			for (Pair<ActionTypeAndName, ModelAction> entry : externalActions) {
				ActionTypeAndName actionTypeAndName = entry.getFirst();
				if (!filterExternalActions || isActionAvailable(actionTypeAndName))
					actionsMap.put(actionTypeAndName, entry.getSecond());
			}
		}
		
		return actionsMap;
	}
	
	private boolean isActionAvailable(ActionTypeAndName actionTypeAndName) {
		ActionTypeAndName checkTypeAndName = new ActionTypeAndName(actionTypeAndName);
		if (!checkTypeAndName.getActionName().startsWith("$"))
			checkTypeAndName.setActionName("$" + checkTypeAndName.getActionName());
		
		Boolean available = actionAvailabilityMap != null ? actionAvailabilityMap.get(checkTypeAndName) : null;
		if (available != null)
			return available;
		
		if (actionAvailabilityMap == null)
			actionAvailabilityMap = new HashMap<>();
		
		available = GMEUtil.isActionAvailable(checkTypeAndName, rootFolder);
		actionAvailabilityMap.put(checkTypeAndName, available);
		
		return available;
	}
	
	private void prepareActionsWithKeyConfigurationMap() {		
		if (rootFolder == null)
			return;
		
		for (Folder folder : rootFolder.getSubFolders())
			prepareActionsWithKeyConfigurationMap(folder);
	}
	
	private void prepareActionsWithKeyConfigurationMap(Folder folder) {
		if (folder.getContent() instanceof WorkbenchAction && ((WorkbenchAction) folder.getContent()).getKeyConfiguration() != null) {
			ModelAction action = prepareModelAction((WorkbenchAction) folder.getContent(), folder, currentActionProviderConfiguration.getGmContentView());
			if (action != null)
				actionsWithKeyConfiguration.put(action, ((WorkbenchAction) folder.getContent()).getKeyConfiguration());
		}
		
		if (folder.getSubFolders() != null && !folder.getSubFolders().isEmpty()) {
			for (Folder subFolder : folder.getSubFolders())
				prepareActionsWithKeyConfigurationMap(subFolder);
		}
	}

	private native boolean isComponentHidden(Component component) /*-{
		return component.@com.sencha.gxt.widget.core.client.Component::hidden;
	}-*/;
	
	private native void layout(BorderLayoutContainer container) /*-{
		container.@com.sencha.gxt.widget.core.client.container.BorderLayoutContainer::doLayout()();
	}-*/;
	
	/*private static native Element getHostIfShadow(Element element, Node theDoc) -{
		var getRootNodeFunction = new Function("element", "return element.getRootNode ? element.getRootNode() : null;");
		var theRoot = getRootNodeFunction(element);
		console.log("theRoot is: " + theRoot);
		//var theRoot = element.getRootNode();
		
		//if (!theRoot) {
			//console.log("Trying to get root node directly.");
			//theRoot = element.getRootNode();
		//}
		
		if (theRoot == theDoc) {
			console.log("theRoot is same as doc.");
			return element;
		}
			
		if (theRoot && theRoot.host) {
			var rootHost = theRoot.host;
			var hostRootNode = getRootNodeFunction(rootHost);
			while (hostRootNode != theDoc) {
				rootHost = hostRootNode.host;
				hostRootNode = getRootNodeFunction(rootHost);
			}
			
			return rootHost;
		}
			
		return element;
	}-*/
	
	private class ElementActionAdapter {
		private final Action action;
		private final String elementId;
		private final PropertyListenerImpl actionListener = new PropertyListenerImpl();
		private final ImageResource emptyImageResource;
		private final String initialName;
		private boolean changedName = false;
		private String initialIcon;
		private boolean changedIcon = false;
		
		private class PropertyListenerImpl implements Action.PropertyListener {
			@Override
			public void propertyChanged(ActionOrGroup source, String property) {
				Element element = Document.get().getElementById(elementId);
				if (element == null)
					return;
				
				switch (property) {
				case KnownProperties.PROPERTY_HIDDEN:
					updateVisibility(element);
					break;
				case KnownProperties.PROPERTY_NAME:
					updateName(element);
					break;
				case KnownProperties.PROPERTY_HOVERICON:
					updateIcon(element);
					break;
				case KnownProperties.PROPERTY_TOOLTIP:
					updateToolTip(element);
					break;
				case KnownProperties.PROPERTY_ENABLED:
					updateEnabled(element);
					break;
				}
			}
		}
		
		/**
		 * Creates a new adapter instance.
		 * @param action - the action to be adapted into the Element.
		 * @param element = the element to be initially adapted from the action.
		 * @param elementId - the unique id of the element to be adapted from the action.
		 * @param emptyIcon - the icon to be used when the action has no icon.
		 */
		protected ElementActionAdapter(Action action, Element element, String elementId, ImageResource emptyIcon) {
			this.action = action;
			this.elementId = elementId;
			this.emptyImageResource = emptyIcon;

			initialName = action.getName() == null ? "&nbsp;" : action.getName();
			changedName = true;
			if (action.getHoverIcon() != null)
				initialIcon = action.getHoverIcon().getSafeUri().asString();
			if (initialIcon == null)
				initialIcon = emptyIcon.getSafeUri().asString();
			changedIcon = true;
			action.addPropertyListener(actionListener);
			
			updateVisibility(element);
			updateName(element);
			updateIcon(element);
			updateToolTip(element);
			updateEnabled(element);
		}
		
		private void updateVisibility(Element element) {
			if (action.getHidden())
				element.getStyle().setDisplay(Display.NONE);
			else
				element.getStyle().clearDisplay();
			getResizeTimer().schedule(RESIZE_TIMER_DELAY);
		}
		
		private void updateName(Element element) {
			NodeList<Node> nodes = element.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.getItem(i);
				if (!(node instanceof Element) || !"actionbar-text".equals(((Element) node).getClassName()))
					continue;
				
				String name = action.getName() == null ? "&nbsp;" : action.getName();
				String currentName = ((Element) node).getInnerText();
				if (changedName || !initialName.equals(name)) {
					changedName = true;
					if (!name.equals(currentName))
						((Element) node).setInnerText(name);
					break;
				}
			}
		}
		
		private void updateToolTip(Element element) {
			if (action.getTooltip() != null)
				element.setAttribute("qtip", action.getTooltip());
			else
				element.removeAttribute("qtip");	
		}
		
		private void updateIcon(Element element) {
			NodeList<Node> nodes = element.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.getItem(i);
				if (!(node instanceof Element) || !"actionbar-image".equals(((Element) node).getClassName()))
					continue;
				
				ImageElement imageElement = ((ImageElement) node);
				
				String iconUrl = null;
				if (action.getHoverIcon() != null)
					iconUrl = action.getHoverIcon().getSafeUri().asString();
				if (iconUrl == null)
					iconUrl = emptyImageResource.getSafeUri().asString();
				
				if (changedIcon || !initialIcon.equals(iconUrl)) {
					changedIcon = true;
					if (!iconUrl.equals(imageElement.getSrc()))
						imageElement.setSrc(iconUrl);
				}
				break;
			}
		}
		
		private void updateEnabled(Element element) {
			if (action.getEnabled()) {
				element.addClassName("enabled");
				element.getStyle().setProperty("opacity", "1");
			} else {
				element.removeClassName("enabled");
				element.getStyle().setProperty("opacity", "0.3");
			}
		}
	}
	
	private static class ToolBarElement {
		
		private String html;
		private List<Element> elements;
		
		public ToolBarElement(String html, List<Element> elements) {
			setHtml(html);
			setElements(elements);
		}

		public String getHtml() {
			return html;
		}

		public void setHtml(String html) {
			this.html = html;
		}
		
		public List<Element> getElements() {
			return elements;
		}
		
		public void setElements(List<Element> elements) {
			this.elements = elements;
		}
		
	}
	
	private static class MoreMenuElement {
		
		private ActionGroup actionGroup;
		private Element element;
		private MenuItem menuItem;
		private int width;
		
		public MoreMenuElement(ActionGroup actionGroup, Element element) {
			setActionGroup(actionGroup);
			setElement(element);
			setWidth(element.getOffsetWidth());
		}
		
		public ActionGroup getActionGroup() {
			return actionGroup;
		}
		
		public void setActionGroup(ActionGroup actionGroup) {
			this.actionGroup = actionGroup;
		}
		
		public Action getAction() {
			return actionGroup == null ? null : actionGroup.getAction();
		}
		
		public Element getElement() {
			return element;
		}
		
		public void setElement(Element element) {
			this.element = element;
		}
		
		public MenuItem getMenuItem() {
			return menuItem;
		}
		
		public void setMenuItem(MenuItem menuItem) {
			this.menuItem = menuItem;
		}
		
		public void setWidth(int width) {
			this.width = width;
		}
		
		public int getWidth() {
			return width;
		}
	}

}
