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
package com.braintribe.gwt.gme.assemblypanel.client;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gm.model.uiaction.ExchangeContentViewActionFolderContent;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.codec.registry.client.CodecRegistry;
import com.braintribe.gwt.gm.storage.api.ColumnData;
import com.braintribe.gwt.gm.storage.api.StorageColumnInfo;
import com.braintribe.gwt.gme.assemblypanel.client.action.ExchangeAssemblyPanelDisplayModeAction.DisplayMode;
import com.braintribe.gwt.gme.assemblypanel.client.model.AbstractGenericTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.CondensedEntityTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.EntityTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.factory.ModelFactory;
import com.braintribe.gwt.gmview.action.client.ActionGroup;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.action.client.CopyTextAction;
import com.braintribe.gwt.gmview.action.client.KnownActions;
import com.braintribe.gwt.gmview.action.client.WorkbenchActionSelectionHandler;
import com.braintribe.gwt.gmview.actionbar.client.ActionProviderConfiguration;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionBar;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionProvider;
import com.braintribe.gwt.gmview.client.ClipboardListener;
import com.braintribe.gwt.gmview.client.ClipboardSupport;
import com.braintribe.gwt.gmview.client.GmActionSupport;
import com.braintribe.gwt.gmview.client.GmAmbiguousSelectionSupport;
import com.braintribe.gwt.gmview.client.GmCheckListener;
import com.braintribe.gwt.gmview.client.GmCheckSupport;
import com.braintribe.gwt.gmview.client.GmCondensationView;
import com.braintribe.gwt.gmview.client.GmContentContext;
import com.braintribe.gwt.gmview.client.GmContentSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewActionManager;
import com.braintribe.gwt.gmview.client.GmContentViewActionManagerHandler;
import com.braintribe.gwt.gmview.client.GmContentViewListener;
import com.braintribe.gwt.gmview.client.GmEditionView;
import com.braintribe.gwt.gmview.client.GmEditionViewController;
import com.braintribe.gwt.gmview.client.GmEntityView;
import com.braintribe.gwt.gmview.client.GmInteractionListener;
import com.braintribe.gwt.gmview.client.GmInteractionSupport;
import com.braintribe.gwt.gmview.client.GmListView;
import com.braintribe.gwt.gmview.client.GmMapView;
import com.braintribe.gwt.gmview.client.GmMouseInteractionEvent;
import com.braintribe.gwt.gmview.client.GmResetableActionsContentView;
import com.braintribe.gwt.gmview.client.GmSelectionCount;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.GmSessionHandler;
import com.braintribe.gwt.gmview.client.GmTemplateMetadataViewSupport;
import com.braintribe.gwt.gmview.client.GmTreeView;
import com.braintribe.gwt.gmview.client.GmViewChangeListener;
import com.braintribe.gwt.gmview.client.GmViewIdProvider;
import com.braintribe.gwt.gmview.client.GmViewport;
import com.braintribe.gwt.gmview.client.GmViewportListener;
import com.braintribe.gwt.gmview.client.GmeDragAndDropView;
import com.braintribe.gwt.gmview.client.IconProvider;
import com.braintribe.gwt.gmview.client.IndexedGmListView;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.metadata.client.MetaDataReevaluationHelper;
import com.braintribe.gwt.gxt.gxtresources.gridwithoutlines.client.GridWithoutLinesAppearance.GridWithoutLinesResources;
import com.braintribe.gwt.gxt.gxtresources.gridwithoutlines.client.GridWithoutLinesAppearance.GridWithoutLinesStyle;
import com.braintribe.gwt.gxt.gxtresources.gridwithoutlines.client.TreeWithoutJointAppearance.TreeWithoutJointResources;
import com.braintribe.gwt.gxt.gxtresources.gridwithoutlines.client.TreeWithoutJointAppearance.TreeWithoutJointStyle;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.MapValuePathElement;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.meta.data.prompt.AutoCommit;
import com.braintribe.model.meta.data.prompt.CondensationMode;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.notification.api.NotificationFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceManipulationListenerRegistry;
import com.braintribe.model.processing.session.impl.persistence.TransientPersistenceGmSession;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedSet;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.Point;
import com.sencha.gxt.dnd.core.client.TreeGridDropTarget;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.menu.Menu;

/**
 * Abstract class, which is extended by the {@link AssemblyPanel}, and have implementations for all its extended interfaces.
 * @author michel.docouto
 *
 */
public abstract class AbstractAssemblyPanel extends BorderLayoutContainer implements InitializableBean, ManipulationListener, GmEntityView,
		GmListView, GmMapView, GmViewport, GmViewActionProvider, ClipboardSupport, GmAmbiguousSelectionSupport, GmContentViewActionManagerHandler,
		IndexedGmListView, GmResetableActionsContentView, GmActionSupport, GmViewIdProvider, GmSelectionCount, GmCondensationView, GmEditionView,
		GmTreeView, GmeDragAndDropView, GmCheckSupport, GmInteractionSupport, GmContentSupport, GmTemplateMetadataViewSupport, DisposableBean {

	protected static final Logger logger = new Logger(AbstractAssemblyPanel.class);
	private static final String ASSEMBLY_PANEL_ROOT_ID = "gmAssemblyPanel";

	protected AssemblyUtil assemblyUtil;
	protected CodecRegistry<String> codecRegistry;
	private String treeRendererSeparator;
	private IconProvider iconProvider;
	protected String useCase;
	protected ModelFactory modelFactory;
	protected boolean prepareToolBarActions = true;
	protected boolean showContextMenu = true;
	protected boolean hideHeaders = false;
	protected GmContentViewActionManager actionManager;
	protected AssemblyPanelTreeGrid editorTreeGrid;
	protected Menu actionsContextMenu;
	protected boolean loadedAbsentWithRightClick = false;
	protected Point loadedAbsentWithRightClickPoint;
	private boolean autoExpandNodeColumn = true;
	protected Set<GenericEntity> entitiesWithManipulationListeners = new HashSet<>();
	protected DisplayMode currentDisplayMode = DisplayMode.Detailed;
	private boolean showGridLines = false;
	private boolean initialized = false;
	protected ModelPath rootModelPath;
	protected List<Pair<ActionTypeAndName, ModelAction>> externalActions;
	private ActionProviderConfiguration actionProviderConfiguration;
	private List<GmContentViewListener> contentViewListeners;
	private List<GmViewChangeListener> viewChangeListeners;
	protected List<GmSelectionListener> gmSelectionListeners;
	private List<GmInteractionListener> gmInteractionListeners;
	protected List<GmCheckListener> gmCheckListeners;
	protected PersistenceGmSession gmSession;
	protected ManipulationHandler manipulationHandler;
	private GmViewActionBar gmViewActionBar;
	private Supplier<? extends GmViewActionBar> gmViewActionBarProvider;
	private boolean useCondensationActions = true;
	protected EntityType<GenericEntity> entityTypeForProperties;
	protected int maxSelectionCount = 1;
	protected List<Pair<String, ? extends Widget>> menuItemsList;
	private Function<List<List<ModelPath>>, List<List<ModelPath>>> ambiguousModelPathProvider;
	private Set<ClipboardListener> clipboardListeners;
	protected List<Pair<String, TextButton>> buttonsList;
	protected List<GmViewportListener> gmViewportListeners;
	protected GenericModelType typeForCheck;
	protected List<ModelPath> addedModelPaths;
	protected Map<GenericEntity, ModelAndIndex> deletedEntities;
	protected EnhancedSet<?> rootEnhancedSet;
	protected boolean navigationEnabled = true;
	protected Function<AbstractGenericTreeModel, String> nodeRenderingStyleProvider = null;
	protected boolean disableNodesTooltip = false;
	protected boolean filterExternalActions = true;
	protected GmContentContext gmContentContext;
	protected Supplier<? extends Action> defaultContextMenuActionSupplier;
	protected String defaultContextMenuActionName;
	protected TransientPersistenceGmSession transientSession;
	protected Supplier<? extends TransientPersistenceGmSession> transientSessionSupplier;
	protected Supplier<? extends NotificationFactory> notificationFactorySupplier;
	protected Supplier<GmEditionViewController> gmEditionViewControllerSupplier;
	protected CopyTextAction copyTextAction;
	protected List<Object> currentSelectedItems;
	protected List<Object> currentExpandedItems;
	protected boolean autoCommit = false;
	protected Action commitAction;
	protected AssemblyPanelTreeGridDragSource treeGridDragSource;
	protected TreeGridDropTarget<AbstractGenericTreeModel> treeGridDropTarget;
	protected Function<WorkbenchActionContext<?>, ModelAction> workbenchActionHandlerRegistry;
	protected WorkbenchActionSelectionHandler workbenchActionSelectionHandler;
	protected ColumnData columnData;
	protected boolean disableViewportCheck;
	protected List<AbstractGenericTreeModel> allRootItemsForRenderLimit = new ArrayList<>();
	protected List<AbstractGenericTreeModel> selectionForRenderLimit = new ArrayList<>();
	protected List<AbstractGenericTreeModel> itemsWaitingForRendering;

	public AbstractAssemblyPanel() {
		this.setBorders(false);
		addStyleName("gmeAssemblyPanel");
//		XElement.as(getStyleElement()).applyStyles("backgroundColor: #efefef");
	}

	/**
	 * Configures the required {@link CodecRegistry} used as renderers.
	 */
	@Required
	public void setCodecRegistry(CodecRegistry<String> codecRegistry) {
		this.codecRegistry = codecRegistry;
	}
	
	/**
	 * Configures the required {@link ModelFactory}, used for building the tree model structure.
	 */
	@Required
	public void setModelFactory(ModelFactory modelFactory) {
		this.modelFactory = modelFactory;
		modelFactory.configureAssemblyPanel(this);
	}
	
	/**
	 * Configures the required provider which will provide icons.
	 */
	@Required
	public void setIconProvider(IconProvider iconProvider) {
		this.iconProvider = iconProvider;
	}
	
	/**
	 * Configures the required provider for GmViewActionBar used for action navigation.
	 */
	@Required
	public void setGmViewActionBarProvider(Supplier<? extends GmViewActionBar> gmViewActionBarProvider) {
		this.gmViewActionBarProvider = gmViewActionBarProvider;
	}
	
	/**
	 * Configures the required {@link TransientPersistenceGmSession} used for running the OnEditFired request.
	 */
	@Required
	public void setTransientSession(TransientPersistenceGmSession transientSession) {
		this.transientSession = transientSession;
	}
	
	/**
	 * Configures the required Supplier for transient sessions, used for running the OnEditFired request.
	 */
	@Required
	public void setTransientSessionSupplier(Supplier<? extends TransientPersistenceGmSession> transientSessionSupplier) {
		this.transientSessionSupplier = transientSessionSupplier;
	}
	
	/**
	 * Configures the required {@link NotificationFactory} used for running the OnEditFired request.
	 */
	@Required
	public void setNotificationFactory(Supplier<? extends NotificationFactory> notificationFactorySupplier) {
		this.notificationFactorySupplier = notificationFactorySupplier;
	}
	
	/**
	 * Configures the registry which contains action experts to be performed depending on the action type.
	 */
	@Required
	public void setWorkbenchActionHandlerRegistry(Function<WorkbenchActionContext<?>, ModelAction> workbenchActionHandlerRegistry) {
		this.workbenchActionHandlerRegistry = workbenchActionHandlerRegistry;
	}
	
	/**
	 * Configures whether we should prepare the actions to be displayed in the toolBar. Defaults to true.
	 */
	@Configurable
	public void setPrepareToolBarActions(boolean prepareToolBarActions) {
		this.prepareToolBarActions = prepareToolBarActions;
	}

	/**
	 * Configures whether to auto expand the node column. Defaults to true.
	 */
	@Configurable
	public void setAutoExpandNodeColumn(boolean autoExpandNodeColumn) {
		this.autoExpandNodeColumn = autoExpandNodeColumn;
	}
	
	/**
	 * Configures the separator used in the tree node renderer.
	 * Defaults to ":"
	 */
	@Configurable
	public void setTreeRendererSeparator(String treeRendererSeparator) {
		this.treeRendererSeparator = treeRendererSeparator;
	}
	
	/**
	 * Configures whether to show the context menu.
	 * Defaults to true.
	 */
	@Configurable
	public void setShowContextMenu(boolean showContextMenu) {
		this.showContextMenu = showContextMenu;
	}
	
	public void setHideHeaders(boolean hideHeaders) {
		this.hideHeaders = hideHeaders;
	}
	
	/**
	 * Configures whether to use the condensation related actions.
	 * Defaults to true.
	 */
	@Configurable
	public void setUseCondensationActions(boolean useCondensationActions) {
		this.useCondensationActions = useCondensationActions;
	}
	
	/**
	 * Configures what is the default {@link DisplayMode} used within this {@link AssemblyPanel}.
	 * Defaults to {@link DisplayMode#Detailed}.
	 * The modes are:
	 * - Simple (Multiplex Dimension + Recursion Dimension);
	 * - Detailed (Multiplex Dimension + Recursion Dimension + Detail Dimension)
	 * - Flat (Multiplex Dimension + Detail Dimension)
	 */
	@Configurable
	public void setDefaultDisplayMode(DisplayMode defaultDisplayMode) {
		this.currentDisplayMode = defaultDisplayMode;
	}
	
	/**
	 * Configures whether to show grid lines. Defaults to false.
	 */
	@Configurable
	public void setShowGridLines(boolean showGridLines) {
		this.showGridLines = showGridLines;
	}
	
	/**
	 * Configures an external provider which is responsible for preparing the ModelPath in case of an ambiguous selection.
	 */
	@Configurable
	public void setAmbiguousModelPathProvider(Function<List<List<ModelPath>>, List<List<ModelPath>>> ambiguousModelPathProvider) {
		this.ambiguousModelPathProvider = ambiguousModelPathProvider;
	}
	
	/**
	 * Configures whether navigation (either via double click, Enter, or the Open action) is enabled.
	 * Defaults to true.
	 */
	@Configurable
	public void setNavigationEnabled(boolean navigationEnabled) {
		this.navigationEnabled = navigationEnabled;
	}
	
	/**
	 * Configures an extra style to be used when rendering nodes.
	 */
	@Configurable
	public void setNodeRenderingStyleProvider(Function<AbstractGenericTreeModel, String> nodeRenderingStyleProvider) {
		this.nodeRenderingStyleProvider = nodeRenderingStyleProvider;
	}
	
	/**
	 * Configures whether tooltips should be disabled for the tree nodes.
	 * Defaults to false.
	 */
	@Configurable
	public void setDisableNodesTooltip(boolean disableNodesTooltip) {
		this.disableNodesTooltip = disableNodesTooltip;
	}
	
	/**
	 * Configures whether to filter the external actions based on the actions defined in the root folder.
	 * Defaults to true.
	 */
	@Configurable
	public void setFilterExternalActions(boolean filterExternalActions) {
		this.filterExternalActions = filterExternalActions;
	}
	
	/**
	 * Configures the action supplier which should be showed when no other action is available and there is no selection.
	 */
	@Configurable
	public void setDefaultContextMenuActionSupplier(Supplier<? extends Action> actionSupplier, String actionName) {
		this.defaultContextMenuActionSupplier = actionSupplier;
		this.defaultContextMenuActionName = actionName;
	}
	
	/**
	 * Configures the {@link Action} used for committing when {@link AutoCommit} is available.
	 */
	@Configurable
	public void setCommitAction(Action commitAction) {
		this.commitAction = commitAction;
	}
	
	/**
	 * Configures the {@link WorkbenchActionSelectionHandler} for selecting between actions to execute.
	 */
	@Configurable
	public void setWorkbenchActionSelectionHandler(WorkbenchActionSelectionHandler workbenchActionSelectionHandler) {
		this.workbenchActionSelectionHandler = workbenchActionSelectionHandler;
	}
	
	@Override
	public void setGmContentContext(GmContentContext context) {
		this.gmContentContext = context;
	}
	
	@Override
	public GmContentContext getGmContentContext() {
		return this.gmContentContext;
	}
	
	/**
	 * Returns the icon provider used within the AP.
	 */
	public IconProvider getIconProvider() {
		return iconProvider;
	}
	
	/**
	 * Returns the column data.
	 */
	public ColumnData getColumnData() {
		return columnData;
	}
	
	/**
	 * Returns the {@link AssemblyPanelTreeGrid} used in the panel.
	 */
	public AssemblyPanelTreeGrid getTreeGrid() {
		return editorTreeGrid;
	}
	
	/**
	 * Returns the {@link ModelFactory} used within the {@link AssemblyPanel}.
	 */
	public ModelFactory getModelFactory() {
		return modelFactory;
	}
	
	/**
	 * Returns the current display mode.
	 */
	public DisplayMode getCurrentDisplayMode() {
		return currentDisplayMode;
	}
	
	@Override
	public void intializeBean() throws Exception {
		prepareAssemblyUtil();
		iconProvider.configureUseCase(useCase);
		prepareTreeGrid();
		exchangeCenterWidget(getEmptyPanel());
		modelFactory.configureCanUncondense(prepareToolBarActions || showContextMenu);
		if (prepareToolBarActions || showContextMenu) {
			prepareDefaultActions();
			actionManager.connect(this);
		}
		prepareContextMenu();
		if (autoExpandNodeColumn)
			editorTreeGrid.configureAutoExpand();

		prepareTreeStoreListeners();

		ColumnModel<AbstractGenericTreeModel> columnModel = editorTreeGrid.getColumnModel();
		switch (currentDisplayMode) {
		case Flat:
			columnModel.setHidden(0, true);
			for (int i = 1; i < columnModel.getColumnCount(); i++)
				columnModel.setHidden(i, false);
			break;
		case Simple:
			columnModel.setHidden(0, false);
			for (int i = 1; i < columnModel.getColumnCount(); i++)
				columnModel.setHidden(i, true);
			break;
		case Detailed:
			break;
		}

		if (!showGridLines) {
			GridWithoutLinesStyle style = GWT.<GridWithoutLinesResources>create(GridWithoutLinesResources.class).css();
			style.ensureInjected();
			editorTreeGrid.addStyleName(style.gridWithoutLines());
		}
		editorTreeGrid.getView().setTrackMouseOver(false);
		
		TreeWithoutJointStyle style = GWT.<TreeWithoutJointResources>create(TreeWithoutJointResources.class).style();
		style.ensureInjected();
		editorTreeGrid.addStyleName(style.treeWithoutJoint());

		initialized = true;
	}

	@Override
	public ModelPath getContentPath() {
		return rootModelPath;
	}

	@Override
	public void configureActionGroup(ActionGroup actionGroup) {
		initializeActions();
	}

	@Override
	public void configureExternalActions(List<Pair<ActionTypeAndName, ModelAction>> externalActions) {
		if (externalActions == null)
			externalActions = new ArrayList<>();
		
		this.externalActions = new ArrayList<>(externalActions);
		externalActions.stream().filter(pair -> !actionManager.isActionAvailable(pair.getFirst())).forEach(pair -> this.externalActions.remove(pair));
		
		//this.externalActions.add(new Pair<>("copyText", copyTextAction));
		
		if (actionProviderConfiguration != null)
			actionProviderConfiguration.addExternalActions(this.externalActions);
		if (actionsContextMenu != null) //Already initialized
			actionManager.addExternalActions(this, this.externalActions);
	}

	@Override
	public List<Pair<ActionTypeAndName, ModelAction>> getExternalActions() {
		return externalActions;
	}

	@Override
	public void setActionManager(GmContentViewActionManager actionManager) {
		this.actionManager = actionManager;
	}

	@Override
	public void addGmContentViewListener(GmContentViewListener listener) {
		if (listener != null) {
			if (contentViewListeners == null)
				contentViewListeners = new ArrayList<>();
			contentViewListeners.add(listener);
		}
	}

	@Override
	public void removeGmContentViewListener(GmContentViewListener listener) {
		if (listener != null && contentViewListeners != null) {
			contentViewListeners.remove(listener);
			if (contentViewListeners.isEmpty())
				contentViewListeners = null;
		}
	}
	
	@Override
	public void addGmViewChangeListener(GmViewChangeListener listener) {
		if (listener != null) {
			if (viewChangeListeners == null)
				viewChangeListeners = new ArrayList<>();
			viewChangeListeners.add(listener);
		}
	}
	
	@Override
	public void removeGmViewChangeListener(GmViewChangeListener listener) {
		if (listener != null && viewChangeListeners != null) {
			viewChangeListeners.remove(listener);
			if (viewChangeListeners.isEmpty())
				viewChangeListeners = null;
		}
	}

	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		if (sl != null) {
			if (gmSelectionListeners == null)
				gmSelectionListeners = new ArrayList<>();
			gmSelectionListeners.add(sl);
		}
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		if (gmSelectionListeners != null) {
			gmSelectionListeners.remove(sl);
			if (gmSelectionListeners.isEmpty())
				gmSelectionListeners = null;
		}
	}

	@Override
	public ModelPath getFirstSelectedItem() {
		AbstractGenericTreeModel selectedModel = editorTreeGrid.getSelectionModel().getSelectedItem();
		if (selectedModel != null)
			return AssemblyUtil.getModelPath(selectedModel, rootModelPath);

		return rootModelPath;
	}
	
	/**
	 * Returns the index of the first select element, or -1 if none is selected
	 */
	@Override
	public int getFirstSelectedIndex() {
		AbstractGenericTreeModel selectedModel = editorTreeGrid.getSelectionModel().getSelectedItem();
		if (selectedModel != null)
			return editorTreeGrid.getStore().indexOf(selectedModel);
		
		return -1;
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		List<ModelPath> modelPaths = null;
		List<AbstractGenericTreeModel> selectedModels = editorTreeGrid.getSelectionModel().getSelectedItems();
		if (!selectedModels.isEmpty()) {
			modelPaths = new ArrayList<>();
			for (AbstractGenericTreeModel selectedModel : selectedModels)
				modelPaths.add(AssemblyUtil.getModelPath(selectedModel, rootModelPath));
		} else if (rootModelPath != null) {
			modelPaths = new ArrayList<>();
			modelPaths.add(rootModelPath);
		}
		return modelPaths;
	}

	@Override
	public boolean isSelected(Object element) {
		List<AbstractGenericTreeModel> selectedModels = editorTreeGrid.getSelectionModel().getSelectedItems();
		if (selectedModels != null && !selectedModels.isEmpty())
			return selectedModels.stream().anyMatch(selectedModel -> selectedModel.refersTo(element));
		
		return false;
	}

	@Override
	public boolean selectVertical(Boolean next, boolean keepExisting) {
		AbstractGenericTreeModel model = editorTreeGrid.getSelectionModel().getSelectedItem();
		if (model == null)
			return false;
		int index = editorTreeGrid.getTreeStore().getAll().indexOf(model);
		if (index < 0)
			return false;
		
		if (next)
			index++;
		else
			index--;
		
		if (index < 0 || index >= editorTreeGrid.getTreeStore().getAll().size())
			return false;
		
		editorTreeGrid.selectionModel.performSelect(index, keepExisting);
		return true;
	}
	
	@Override
	public boolean selectHorizontal(Boolean next, boolean keepExisting) {
		return selectVertical(next, keepExisting);
	}	
	
	@Override
	public void select(int index, boolean keepExisting) {
		editorTreeGrid.selectionModel.performSelect(index, keepExisting);
	}
	
	@Override
	public void selectRoot(int index, boolean keepExisting) {
		if (!isRendered())
			return;
		
		if (index >= 0 && index < editorTreeGrid.getTreeStore().getRootCount()) {
			AbstractGenericTreeModel model = editorTreeGrid.getTreeStore().getRootItems().get(index);
			editorTreeGrid.getSelectionModel().select(model, keepExisting);
		}
	}
	
	@Override
	public boolean select(Element element, boolean keepExisting) {
		int rowIndex = editorTreeGrid.getView().findRowIndex(element);
		if (rowIndex == -1)
			return false;

		editorTreeGrid.getSelectionModel().select(rowIndex, keepExisting);
		return true;
	}
	
	@Override
	public void deselectAll() {
		if (!isRendered())
			return;
		
		editorTreeGrid.getSelectionModel().deselectAll();
	}

	@Override
	public GmContentView getView() {
		return this;
	}

	@Override
	public void addInteractionListener(GmInteractionListener il) {
		if (il != null) {
			if (gmInteractionListeners == null) {
				gmInteractionListeners = new ArrayList<>();
			}
			gmInteractionListeners.add(il);
		}
	}

	@Override
	public void removeInteractionListener(GmInteractionListener il) {
		if (gmInteractionListeners != null) {
			gmInteractionListeners.remove(il);
			if (gmInteractionListeners.isEmpty())
				gmInteractionListeners = null;
		}
	}

	@Override
	public void addCheckListener(GmCheckListener cl) {
		if (cl != null) {
			if (gmCheckListeners == null)
				gmCheckListeners = new ArrayList<>();
			gmCheckListeners.add(cl);
		}
	}

	@Override
	public void removeCheckListener(GmCheckListener cl) {
		if (gmCheckListeners != null) {
			gmCheckListeners.remove(cl);
			if (gmCheckListeners.isEmpty())
				gmCheckListeners = null;
		}
	}

	@Override
	public ModelPath getFirstCheckedItem() {
		return editorTreeGrid.selectionModel.getFirstCheckedItem();
	}

	@Override
	public List<ModelPath> getCurrentCheckedItems() {
		return editorTreeGrid.selectionModel.getCurrentCheckedItems();
	}

	@Override
	public boolean isChecked(Object element) {
		return editorTreeGrid.selectionModel.isChecked(element);
	}

	@Override
	public boolean uncheckAll() {
		return editorTreeGrid.selectionModel.uncheckAll();
	}

	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;

		MetaDataReevaluationHelper.setGmSession(gmSession);
		
		if (actionManager instanceof GmSessionHandler)
			((GmSessionHandler) actionManager).configureGmSession(gmSession);

		if (iconProvider != null)
			iconProvider.configureGmSession(gmSession);

		if (gmSession != null) {
			if (manipulationHandler == null)
				manipulationHandler = new ManipulationHandler(gmSession);
			else
				manipulationHandler.configureGmSession(gmSession);
			
			ModelMdResolver mdResolver = gmSession.getModelAccessory().getMetaData();
			autoCommit = mdResolver == null ? false : mdResolver.is(AutoCommit.T);
		} else {
			manipulationHandler = null;
			autoCommit = false;
		}
	}

	@Override
	public PersistenceGmSession getGmSession() {
		return gmSession;
	}

	@Override
	public void configureUseCase(String useCase) {
		this.useCase = useCase;

		if (iconProvider != null)
			iconProvider.configureUseCase(useCase);
	}

	@Override
	public String getUseCase() {
		return useCase;
	}

	@Override
	public boolean isLocalCondensationEnabled() {
		return true;
	}

	@Override
	public boolean checkUncondenseLocalEnablement() {
		List<AbstractGenericTreeModel> models = editorTreeGrid.getSelectionModel().getSelectedItems();
		AbstractGenericTreeModel model = null;
		if (models != null && !models.isEmpty())
			model = models.get(models.size() - 1);
		if (model != null && (model instanceof CondensedEntityTreeModel || model.getDelegate() instanceof CondensedEntityTreeModel)) {
			CondensedEntityTreeModel condensedModel = model instanceof CondensedEntityTreeModel ? (CondensedEntityTreeModel) model :
				(CondensedEntityTreeModel) model.getDelegate();
			CondensationMode condensationMode = modelFactory.getCondensationMode(condensedModel.getEntityTreeModel().getElementType());
			if (condensationMode == null || !condensationMode.equals(CondensationMode.forced))
				return true;
		}
		return false;
	}

	@Override
	public String getCondensendProperty() {
		String condensedProperty = null;

		List<AbstractGenericTreeModel> models = editorTreeGrid.getSelectionModel().getSelectedItems();
		AbstractGenericTreeModel model = null;
		if (models != null && !models.isEmpty())
			model = models.get(models.size() - 1);

		if (model != null) {
			GenericEntity entity = null;
			EntityType<?> entityType = null;
			EntityTreeModel entityTreeModel = model.getEntityTreeModel();
			if (entityTreeModel != null) {
				entity = entityTreeModel.getModelObject();
				entityType = entityTreeModel.getElementType();
			} else if (model.getElementType().isEntity()) {
				if (model.getModelObject() instanceof GenericEntity)
					entity = model.getModelObject();
				entityType = (EntityType<?>) model.getElementType();
			}

			if (entityType != null)
				condensedProperty = modelFactory.getCondensedProperty(entity, entityType);
		}

		return condensedProperty;
	}

	@Override
	public String getCurrentCondensedProperty(EntityType<?> entityType) {
		return modelFactory.getCondensedProperty(null, entityType);
	}

	@Override
	public void uncondenseLocal() {
		AbstractGenericTreeModel model = editorTreeGrid.getSelectionModel().getSelectedItem();
		final int index = editorTreeGrid.getStore().indexOf(model);
		uncondenseSingleEntity(model, true, editorTreeGrid.isExpanded(model));

		Scheduler.get().scheduleDeferred(() -> editorTreeGrid.getSelectionModel().select(index, false));
	}

	@Override
	public void condenseLocal() {
		AbstractGenericTreeModel model = editorTreeGrid.getSelectionModel().getSelectedItem();
		final int index = editorTreeGrid.getStore().indexOf(model);
		String condensedProperty = getCondensendProperty();

		if (condensedProperty != null) {
			boolean expand = editorTreeGrid.isExpanded(model);
			AbstractGenericTreeModel parentModel = editorTreeGrid.getTreeStore().getParent(model);
			condenseSingleEntity(model, condensedProperty);
			if (parentModel != null && !editorTreeGrid.isExpanded(parentModel))
				editorTreeGrid.setExpanded(parentModel, true);
			if (expand)
				editorTreeGrid.setExpanded(editorTreeGrid.getStore().get(index), true);
		}

		Scheduler.get().scheduleDeferred(() -> editorTreeGrid.getSelectionModel().select(index, false));
	}

	@Override
	public void condense(String propertyName, CondensationMode condensationMode, EntityType<?> entityType) {
		AbstractGenericTreeModel selected = editorTreeGrid.getSelectionModel().getSelectedItem();
		if (!editorTreeGrid.getTreeStore().getRootItems().contains(selected))
			selected = null;
		final AbstractGenericTreeModel modelToselect = selected;

		condenseEntity(entityType, propertyName, condensationMode);

		Scheduler.get().scheduleDeferred(() -> {
			if (modelToselect != null)
				editorTreeGrid.getSelectionModel().select(modelToselect, false);

			getGmViewActionBar()
					.navigateToAction(new ActionTypeAndName(ExchangeContentViewActionFolderContent.T, KnownActions.EXCHANGE_CONTENT_VIEW.getName()));
		});
	}

	@Override
	public GmViewActionBar getGmViewActionBar() {
		if (gmViewActionBar == null) {
			try {
				gmViewActionBar = gmViewActionBarProvider.get();
			} catch (RuntimeException e) {
				logger.error("Error while providing GmViewActionBar", e);
				e.printStackTrace();
			}
		}
		return gmViewActionBar;
	}

	@Override
	public boolean isUseCondensationActions() {
		return useCondensationActions;
	}

	@Override
	public EntityType<GenericEntity> getEntityTypeForProperties() {
		return entityTypeForProperties;
	}

	@Override
	public void setMaxSelectCount(int maxCount) {
		this.maxSelectionCount = maxCount;
	}

	@Override
	public String getRootId() {
		return ASSEMBLY_PANEL_ROOT_ID;
	}

	@Override
	public void resetActions() {
		if (actionsContextMenu != null) {//Already initialized
			actionManager.resetActions(this);
			if (showContextMenu) {
				Widget actionMenu = actionManager.getActionMenu(this, menuItemsList, filterExternalActions);
				if (actionMenu instanceof Menu) {
					actionsContextMenu = (Menu) actionMenu;
					editorTreeGrid.setContextMenu(actionsContextMenu);
				}
			}
		}
	}

	@Override
	public int getRootElementIndex(Object element) {
		if (element != null) {
			int index = 0;
			for (AbstractGenericTreeModel rootModel : editorTreeGrid.getTreeStore().getRootItems()) {
				if (rootModel.refersTo(element))
					return index;
				index++;
			}
		}

		return -1;
	}

	@Override
	public GmContentViewActionManager getGmContentViewActionManager() {
		return actionManager;
	}

	@Override
	public List<List<ModelPath>> getAmbiguousSelection() {
		List<AbstractGenericTreeModel> selectedItems = editorTreeGrid.getSelectionModel().getSelectedItems();
		if (selectedItems.isEmpty() && rootModelPath == null)
			return null;
		
		List<List<ModelPath>> ambiguousSelection = newList();
		if (selectedItems.isEmpty()) {
			ambiguousSelection.add(Collections.singletonList(rootModelPath));
			return ambiguousSelection;
		}
		
		for (AbstractGenericTreeModel selectedItem : selectedItems) {
			List<ModelPath> modelPaths = AssemblyUtil.getAmbiguousModelPath(selectedItem, rootModelPath);
			ambiguousSelection.add(modelPaths);
		}
		
		if (ambiguousModelPathProvider != null) {
			try {
				return ambiguousModelPathProvider.apply(ambiguousSelection);
			} catch (RuntimeException e) {
				e.printStackTrace();
				logger.error("Error while providing the modelPath with the ambiguousModelPathProvider. Using the default provider instead.", e);
			}
		}
		
		return ambiguousSelection;
	}

	@Override
	public void addClipboardListener(ClipboardListener listener) {
		if (clipboardListeners == null) {
			clipboardListeners = new LinkedHashSet<>();
		}
		clipboardListeners.add(listener);
	}

	@Override
	public void removeClipboardListener(ClipboardListener listener) {
		if (clipboardListeners != null) {
			clipboardListeners.remove(listener);
			if (clipboardListeners.isEmpty()) {
				clipboardListeners = null;
			}
		}
	}

	@Override
	public ActionProviderConfiguration getActions() {
		if (actionProviderConfiguration == null) {
			initializeActions();

			actionProviderConfiguration = new ActionProviderConfiguration();
			actionProviderConfiguration.setGmContentView(this);

			List<Pair<ActionTypeAndName, ModelAction>> knownActions = null;
			if (actionManager != null)
				knownActions = actionManager.getKnownActionsList(this);
			if (knownActions != null || externalActions != null) {
				List<Pair<ActionTypeAndName, ModelAction>> allActions = new ArrayList<>();
				if (knownActions != null)
					allActions.addAll(knownActions);
				if (externalActions != null)
					allActions.addAll(externalActions);

				actionProviderConfiguration.addExternalActions(allActions);
			}

			if (buttonsList != null)
				actionProviderConfiguration.setExternalButtons(new ArrayList<>(buttonsList));
		}

		return actionProviderConfiguration;
	}
	
	@Override
	public boolean isFilterExternalActions() {
		return filterExternalActions;
	}

	@Override
	public void addGmViewportListener(GmViewportListener vl) {
		if (vl != null) {
			if (gmViewportListeners == null)
				gmViewportListeners = new ArrayList<>();
			gmViewportListeners.add(vl);
		}
	}

	@Override
	public void removeGmViewportListener(GmViewportListener vl) {
		if (gmViewportListeners != null) {
			gmViewportListeners.remove(vl);
			if (gmViewportListeners.isEmpty())
				gmViewportListeners = null;
		}
	}

	@Override
	public boolean isWindowOverlappingFillingSensorArea() {
		if (disableViewportCheck || !editorTreeGrid.isVisible())
			return false;
		
		//Check if the last page is visible.
		XElement viewEl = editorTreeGrid.getView().getScroller();

		if (viewEl == null)
			return false;

		if (!viewEl.isScrollableY())
			return true;

		int visibleViewHeight = viewEl.getClientHeight();
		//Whole View - current scroll - visible view
		return viewEl.getScrollHeight() - editorTreeGrid.getView().getScrollState().getY() - visibleViewHeight < visibleViewHeight;
	}

	/**
	 * Configures the {@link GenericModelType} that will have its elements available for being checked.
	 */
	@Override
	public void configureTypeForCheck(GenericModelType typeForCheck) {
		this.typeForCheck = typeForCheck;
	}

	@Override
	public List<ModelPath> getAddedModelPaths() {
		return addedModelPaths;
	}
	
	@Override
	public void stopEditing() {
		editorTreeGrid.gridEditing.cancelEditing();
	}
	
	@Override
	public void setGmEditionViewController(Supplier<GmEditionViewController> controllerSupplier) {
		this.gmEditionViewControllerSupplier = controllerSupplier;
	}
	
	@Override
 	public void showAllColumns() {
 		ColumnModel<AbstractGenericTreeModel> columnModel = editorTreeGrid.getColumnModel();
 		editorTreeGrid.getView().setForceFit(false);
		columnModel.setHidden(0, false);
		for (int i = 1; i < columnModel.getColumnCount(); i++)
			columnModel.setHidden(i, false);
 	}

  	@Override
 	public void showOnlyNodeColumn() {
 		ColumnModel<AbstractGenericTreeModel> columnModel = editorTreeGrid.getColumnModel();
 		editorTreeGrid.getView().setForceFit(false);
		columnModel.setHidden(0, false);
		for (int i = 1; i < columnModel.getColumnCount(); i++)
			columnModel.setHidden(i, true);
 	}

	@Override
	public void disposeBean() throws Exception {
		if (gmViewportListeners != null)
			gmViewportListeners.clear();

		if (gmSelectionListeners != null)
			gmSelectionListeners.clear();

		if (gmCheckListeners != null)
			gmCheckListeners.clear();

		if (gmInteractionListeners != null)
			gmInteractionListeners.clear();

		if (contentViewListeners != null)
			contentViewListeners.clear();

		if (clipboardListeners != null)
			clipboardListeners.clear();

		if (entitiesWithManipulationListeners != null) {
			PersistenceManipulationListenerRegistry listeners = gmSession.listeners();
			entitiesWithManipulationListeners.forEach(entity -> listeners.entity(entity).remove(AbstractAssemblyPanel.this));
			entitiesWithManipulationListeners.clear();
		}

		if (deletedEntities != null)
			deletedEntities.clear();

		if (actionManager != null)
			actionManager.notifyDisposedView(this);

		if (rootEnhancedSet != null)
			rootEnhancedSet.removeManipulationListener(getRootEnhancedSetManipulationListener());

		if (externalActions != null)
			externalActions.clear();

		if (menuItemsList != null)
			menuItemsList.clear();

		if (buttonsList != null)
			buttonsList.clear();

		if (addedModelPaths != null)
			addedModelPaths.clear();
		
		if (currentSelectedItems != null)
			currentSelectedItems.clear();
		
		if (currentExpandedItems != null)
			currentExpandedItems.clear();
		
		if (treeGridDragSource != null)
			treeGridDragSource.release();
		if (treeGridDropTarget != null)
			treeGridDropTarget.release();
		
		if (allRootItemsForRenderLimit != null)
			allRootItemsForRenderLimit.clear();
		if (selectionForRenderLimit != null)
			selectionForRenderLimit.clear();
		if (itemsWaitingForRendering != null)
			itemsWaitingForRendering.clear();
		
		editorTreeGrid.disposeBean();
	}

	protected void fireModelsAddedToClipboard(List<AbstractGenericTreeModel> models) {
		if (clipboardListeners == null || clipboardListeners.isEmpty())
			return;
		
		List<ModelPath> modelPaths = new ArrayList<>();
		for (AbstractGenericTreeModel selectedModel : models) {
			List<ModelPath> ambiguousSelection = AssemblyUtil.getAmbiguousModelPath(selectedModel, rootModelPath);
			ambiguousSelection.stream()
					.filter(modelPath -> modelPath.last().getValue() instanceof GenericEntity || modelPath.last() instanceof MapValuePathElement)
					.findFirst().ifPresent(modelPath -> modelPaths.add(modelPath));
		}

		clipboardListeners.forEach(listener -> listener.onModelsAddedToClipboard(modelPaths));
	}
	
	protected void fireClickOrDoubleClick(boolean click, AssemblyPanelMouseInteractionEvent event) {
		if (click)
			handleDeselectAll(event);
		
		if (gmInteractionListeners == null || gmInteractionListeners.isEmpty())
			return;
		
		for (GmInteractionListener listener : gmInteractionListeners) {
			if (click)
				listener.onClick(event);
			else
				listener.onDblClick(event);
		}
		
		if (click)
			return;
		
		ModelPath model = getFirstSelectedItem();
		if (navigationEnabled && model != null && model.last().getValue() != null) {
			ModelAction action = actionManager.getWorkWithEntityAction(this);
			if (action != null)
				action.perform(null);
		}
	}
	
	private void handleDeselectAll(AssemblyPanelMouseInteractionEvent event) {
		GwtEvent<?> gwtEvent = event.getGridEvent();
		if ((!(gwtEvent instanceof ClickEvent) && !(gwtEvent instanceof ContextMenuEvent)) || editorTreeGrid.gridEditing.isEditing())
			return;
		
		DomEvent<?> domEvent = (DomEvent<?>) gwtEvent;
		EventTarget target = domEvent.getNativeEvent().getEventTarget();
		if (!Element.is(target))
			return;
		
		Element targetElement = target.cast();
		
		if (editorTreeGrid.getTreeView().getHeader().getElement().isOrHasChild(targetElement))
			return;
		
		if (editorTreeGrid.getTreeView().findCell(targetElement) == null)
			editorTreeGrid.getSelectionModel().deselectAll();
	}

	protected boolean fireBeforeExpand(GmMouseInteractionEvent event) {
		boolean canceled = false;
		if (gmInteractionListeners != null) {
			for (GmInteractionListener listener : gmInteractionListeners) {
				boolean cancel = listener.onBeforeExpand(event);
				if (!canceled && cancel)
					canceled = true;
			}
		}
		
		return canceled;
	}
	
	protected void fireContentSet() {
		if (contentViewListeners != null)
			contentViewListeners.forEach(listener -> listener.onContentSet(this));
	}
	
	protected void fireColumnChanged(boolean displayNode, Integer nodeWidth, List<StorageColumnInfo> columnsVisible) {
		if (viewChangeListeners != null)
			viewChangeListeners.forEach(listener -> listener.onColumnsChanged(displayNode, nodeWidth, columnsVisible));
	}

	protected abstract void prepareTreeGrid();
	protected abstract HTML getEmptyPanel();
	protected abstract void exchangeCenterWidget(Widget widget);
	protected abstract void prepareDefaultActions();
	protected abstract void updateEmptyMenuItem();
	protected abstract void addManipulationListener(AbstractGenericTreeModel model);
	protected abstract void removeManipulationListener(AbstractGenericTreeModel model);
	protected abstract void removeManipulationListener(GenericEntity entity);
	protected abstract void initializeActions();
	protected abstract void clearCheckedItems();
	protected abstract AbstractGenericTreeModel uncondenseSingleEntity(AbstractGenericTreeModel model, boolean expandOthers, boolean expandMe);
	protected abstract AbstractGenericTreeModel condenseSingleEntity(AbstractGenericTreeModel model, String collectionPropertyName);
	protected abstract void condenseEntity(EntityType<?> entityType, String collectionPropertyName, CondensationMode condensationMode);
	protected abstract ManipulationListener getRootEnhancedSetManipulationListener();

	protected static class ModelAndIndex {
		protected AbstractGenericTreeModel model;
		protected int index;

		protected ModelAndIndex(AbstractGenericTreeModel model, int index) {
			this.model = model;
			this.index = index;
		}
	}
	
	private void prepareAssemblyUtil() {
		assemblyUtil = new AssemblyUtil();
		assemblyUtil.configureAssemblyPanel(this);
		assemblyUtil.setCodecRegistry(codecRegistry);
		if (treeRendererSeparator != null)
			assemblyUtil.setTreeRendererSeparator(treeRendererSeparator);
	}
	
	private void prepareContextMenu() {
		if (showContextMenu) {
			//RVE - show context directly on panel (instead of grid), to be able have menu also in case of empty list
			this.setContextMenu(actionsContextMenu);
			this.addShowContextMenuHandler(event -> { //checking items visibility each time the context menu is shown
				if (editorTreeGrid.selectionModel.selectedModel != null && AssemblyUtil.isModelAbsent(editorTreeGrid.selectionModel.selectedModel) != null) {
					loadedAbsentWithRightClick = true;
					loadedAbsentWithRightClickPoint = new Point(event.getMenu().getAbsoluteLeft(), event.getMenu().getAbsoluteTop());
					event.setCancelled(true);
					return;
				}

				updateEmptyMenuItem();
			});
		}
	}
	
	private void prepareTreeStoreListeners() {
		editorTreeGrid.getTreeStore().addStoreAddHandler(event -> {
			event.getItems().forEach(model -> addManipulationListener(model));
			exchangeCenterWidget(editorTreeGrid);
		});

		editorTreeGrid.getTreeStore().addStoreRemoveHandler(event -> {
			removeManipulationListener(event.getItem());
			if (editorTreeGrid.getTreeStore().getRootItems().isEmpty())
				exchangeCenterWidget(getEmptyPanel());
		});

		editorTreeGrid.getTreeStore().addStoreClearHandler(event -> {
			new ArrayList<>(entitiesWithManipulationListeners).forEach(entity -> removeManipulationListener(entity));
			exchangeCenterWidget(getEmptyPanel());
		});

		editorTreeGrid.getTreeStore().addStoreUpdateHandler(event -> event.getItems().forEach(model -> addManipulationListener(model)));
	}
	
	protected native boolean isComponentHidden(Component component) /*-{
		return component.@com.sencha.gxt.widget.core.client.Component::hidden;
	}-*/;

}
