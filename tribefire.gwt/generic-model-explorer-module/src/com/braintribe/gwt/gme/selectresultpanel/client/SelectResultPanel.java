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
package com.braintribe.gwt.gme.selectresultpanel.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.codec.registry.client.CodecRegistry;
import com.braintribe.gwt.gm.storage.api.ColumnData;
import com.braintribe.gwt.gm.storage.api.StorageColumnInfo;
import com.braintribe.gwt.gme.selectresultpanel.client.resources.LocalizedText;
import com.braintribe.gwt.gme.selectresultpanel.client.resources.SelectResultPanelCss;
import com.braintribe.gwt.gme.selectresultpanel.client.resources.SelectResultPanelResources;
import com.braintribe.gwt.gmview.action.client.ActionGroup;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.action.client.ClipboardUtil;
import com.braintribe.gwt.gmview.actionbar.client.ActionProviderConfiguration;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionProvider;
import com.braintribe.gwt.gmview.client.GmActionSupport;
import com.braintribe.gwt.gmview.client.GmContentSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewActionManager;
import com.braintribe.gwt.gmview.client.GmContentViewListener;
import com.braintribe.gwt.gmview.client.GmInteractionListener;
import com.braintribe.gwt.gmview.client.GmInteractionSupport;
import com.braintribe.gwt.gmview.client.GmListView;
import com.braintribe.gwt.gmview.client.GmMouseInteractionEvent;
import com.braintribe.gwt.gmview.client.GmResetableActionsContentView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.GmSessionHandler;
import com.braintribe.gwt.gmview.client.GmTemplateMetadataViewSupport;
import com.braintribe.gwt.gmview.client.GmViewport;
import com.braintribe.gwt.gmview.client.GmViewportListener;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.QuerySelectionHandler;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedColumnHeader;
import com.braintribe.gwt.gxt.gxtresources.gridwithoutlines.client.GridWithoutLinesAppearance.GridWithoutLinesResources;
import com.braintribe.gwt.gxt.gxtresources.gridwithoutlines.client.GridWithoutLinesAppearance.GridWithoutLinesStyle;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.gwt.logging.client.Profiling;
import com.braintribe.gwt.logging.client.ProfilingHandle;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.data.prompt.AutoExpand;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.processing.query.api.stringifier.QuerySelection;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.From;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.Source;
import com.braintribe.model.record.ListRecord;
import com.braintribe.model.record.MapRecord;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.GXT;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.KeyNav;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.grid.CellSelectionModel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridSelectionModel;
import com.sencha.gxt.widget.core.client.grid.GridView;
import com.sencha.gxt.widget.core.client.grid.GridViewConfig;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.selection.CellSelection;
import com.sencha.gxt.widget.core.client.tips.QuickTip;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;

/**
 * Panel used for displaying the results of a {@link SelectQuery}.
 * @author michel.docouto
 *
 */
public class SelectResultPanel extends BorderLayoutContainer implements GmListView, InitializableBean, GmViewActionProvider,
		GmResetableActionsContentView, QuerySelectionHandler, GmViewport, GmInteractionSupport, GmActionSupport, GmContentSupport, GmTemplateMetadataViewSupport, DisposableBean {
	static {
		SelectResultPanelResources.INSTANCE.css().ensureInjected();
	}
	
	private static int idCounter = 0;
	private static GridDataProperties props = GWT.create(GridDataProperties.class);
	private static final String emptyStringImageString = AbstractImagePrototype.create(SelectResultPanelResources.INSTANCE.nullIcon()).getHTML()
			.replaceFirst("style='", "qtip='" + LocalizedText.INSTANCE.empty() + "' style='");
	private static final Logger logger = new Logger(SelectResultPanel.class);
	
	private HTML emptyPanel;
	private boolean contentSet = false;
	private final String emptyTextMessage = LocalizedText.INSTANCE.noItemsToDisplay();
	private Widget currentWidget;
	private boolean prepareToolBarActions = true;
	private List<Pair<String, TextButton>> buttonsList;
	private List<Pair<ActionTypeAndName, ModelAction>> externalActions;
	private ActionProviderConfiguration actionProviderConfiguration;
	private GmContentViewActionManager actionManager;
	private Menu actionsContextMenu;
	private boolean showContextMenu = true;
	private List<Pair<String, ? extends Widget>> menuItemsList;
	private MenuItem emptyMenuItem;
	private Grid<GridData> resultGrid;
	private List<ModelPath> addedModelPaths;
	private List<GmContentViewListener> contentViewListeners;
	private PersistenceGmSession gmSession;
	//private Set<GenericEntity> entitiesWithManipulationListeners = new HashSet<GenericEntity>();
	private String useCase;
	private CodecRegistry<String> specialRenderersRegistry;
	private CodecRegistry<String> codecRegistry;
	private List<GmSelectionListener> gmSelectionListeners;
	private List<GmInteractionListener> gmInteractionListeners;
	private CellSelectionModel<GridData> cellSelectionModel;
	private GridSelectionModel<GridData> gridSelectionModel;
	private List<String> specialUiElementsStyles;
	private List<QuerySelection> querySelectionList;
	private boolean filterExternalActions = true;
	private List<GmViewportListener> gmViewportListeners;
	private Timer windowChangedTimer;
	private Supplier<? extends Action> defaultContextMenuActionSupplier;
	private Action defaultContextMenuAction;
	private String defaultContextMenuActionName;
	private ColumnData columnData;
	private boolean cellSelectionActive = true;
	
	/**
	 * Configures whether we should prepare the actions to be displayed in the toolBar. Defaults to true.
	 */
	@Configurable
	public void setPrepareToolBarActions(boolean prepareToolBarActions) {
		this.prepareToolBarActions = prepareToolBarActions;
	}
	
	/**
	 * Configures whether to show the context menu.
	 * Defaults to true.
	 */
	@Configurable
	public void setShowContextMenu(boolean showContextMenu) {
		this.showContextMenu = showContextMenu;
	}
	
	/**
	 * Configures a {@link CodecRegistry} for rendering special classes.
	 */
	@Configurable
	public void setSpecialRenderersRegistry(CodecRegistry<String> specialRenderersRegistry) {
		this.specialRenderersRegistry = specialRenderersRegistry;
	}
	
	/**
	 * Configures the {@link CodecRegistry} used as renderers.
	 */
	@Configurable
	public void setCodecRegistry(CodecRegistry<String> codecRegistry) {
		this.codecRegistry = codecRegistry;
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
	
	public SelectResultPanel() {
		setBorders(false);
		specialUiElementsStyles = Arrays.asList(SelectResultPanelResources.INSTANCE.css().propertyMenu());
	}
	
	@Override
	public void intializeBean() throws Exception {
		prepareGrid();
		exchangeWidget(getEmptyPanel());
		
		if (prepareToolBarActions || showContextMenu)
			actionManager.connect(this);
		
		if (showContextMenu) {
			resultGrid.setContextMenu(actionsContextMenu);
			resultGrid.addShowContextMenuHandler(event -> updateEmptyMenuItem()); //checking items visibility each time the context menu is shown
		}
	}

	@Override
	public ModelPath getContentPath() {
		return null;
	}

	@Override
	public void configureActionGroup(ActionGroup actionGroup) {
		initializeActions();
		
	}

	@Override
	public void configureExternalActions(List<Pair<ActionTypeAndName, ModelAction>> externalActions) {
		if (externalActions == null) {
			this.externalActions = null;
			return;
		}
		
		this.externalActions = new ArrayList<>(externalActions);
		externalActions.stream().filter(p -> !actionManager.isActionAvailable(p.getFirst())).forEach(p -> this.externalActions.remove(p));
		
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
	public GmContentViewActionManager getGmContentViewActionManager() {
		return this.actionManager;
	}
	
	@Override
	public void setContent(ModelPath modelPath) {
		setContent(modelPath, true);
	}
	
	@Override
	public void setAutoExpand(AutoExpand autoExpand) {
		//NOP
	}
	
	@Override
	public void setColumnData(ColumnData columnData) {
		this.columnData = columnData;
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
		if (cellSelectionActive) {
			CellSelection<GridData> cell = cellSelectionModel.getSelectCell();
			if (cell != null)
				return getModelPath(cell.getModel(), cell.getCell());
		} else {
			GridData gridData = gridSelectionModel.getSelectedItem();
			if (gridData != null)
				return getModelPath(gridData, 0);
		}
		
		return null;
	}
	
	public ModelPath getModelPath(GridData gridData, int cellIndex) {
		ObjectAndType objectAndType = gridData.getObjectAndType(cellIndex);
		ModelPath modelPath = new ModelPath();
		modelPath.add(new RootPathElement(objectAndType.getType(), objectAndType.getObject()));
		return modelPath;
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		List<ModelPath> modelPaths = null;
		ModelPath modelPath = getFirstSelectedItem();
		if (modelPath != null) {
			modelPaths = new ArrayList<>();
			modelPaths.add(modelPath);
		}
		
		return modelPaths;
	}

	@Override
	public boolean isSelected(Object element) {
		if (cellSelectionActive) {
			CellSelection<GridData> cell = cellSelectionModel.getSelectCell();
			if (cell != null) {
				ObjectAndType objectAndType = cell.getModel().getObjectAndType(cell.getCell());
				if (element == objectAndType.getObject())
					return true;
			}
		} else {
			List<GridData> selectedItems = gridSelectionModel.getSelectedItems();
			if (selectedItems != null)
				return selectedItems.stream().anyMatch(gridData -> gridData.getObjectAndType(0).getObject() == element);
		}
		
		return false;
	}

	@Override
	public void select(int index, boolean keepExisting) {
		if (cellSelectionActive)
			cellSelectionModel.selectCell(index, 0);
		else
			gridSelectionModel.select(index, keepExisting);
	}

	@Override
	public GmContentView getView() {
		return this;
	}

	@Override
	public void addInteractionListener(GmInteractionListener il) {
		if (il != null) {
			if (gmInteractionListeners == null)
				gmInteractionListeners = new ArrayList<>();
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
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}

	@Override
	public PersistenceGmSession getGmSession() {
		return gmSession;
	}

	@Override
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
	}

	@Override
	public String getUseCase() {
		return useCase;
	}

	@Override
	public void configureTypeForCheck(GenericModelType typeForCheck) {
		//Nothing to do
	}

	@Override
	public void addContent(ModelPath modelPath) {
		setContent(modelPath, false);
	}

	@Override
	public List<ModelPath> getAddedModelPaths() {
		return addedModelPaths;
	}
	
	@Override
	public ActionProviderConfiguration getActions() {
		if (actionProviderConfiguration != null)
			return actionProviderConfiguration;
		
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
		//Check if the last page is visible.
		XElement viewEl = resultGrid.getView().getScroller();

		if (viewEl == null)
			return false;

		if (!viewEl.isScrollableY())
			return true;

		int visibleViewHeight = viewEl.getClientHeight();
		//Whole View - current scroll - visible view
		return viewEl.getScrollHeight() - resultGrid.getView().getScrollState().getY() - visibleViewHeight < visibleViewHeight;
	}
	
	@Override
	public void resetActions() {
		if (actionsContextMenu != null) {//Already initialized
			actionManager.resetActions(this);
			if (showContextMenu) {
				Widget actionMenu = actionManager.getActionMenu(this, menuItemsList, filterExternalActions);
				if (actionMenu instanceof Menu) {
					actionsContextMenu = (Menu) actionMenu;
					resultGrid.setContextMenu(actionsContextMenu);
				}
			}
		}
	}
	
	@Override
	public void configureQuerySelectionList(List<QuerySelection> querySelectionList) {
		this.querySelectionList = querySelectionList;
	}
	
	@Override
	public List<QuerySelection> getQuerySelectionList() {
		return querySelectionList;
	}
	
	private HTML getEmptyPanel() {
		if (emptyPanel == null)
			emptyPanel = new HTML(getEmptyPanelHtml());
		else if (contentSet)
			updateEmptyPanel();
		
		return emptyPanel;
	}
	
	private String getEmptyPanelHtml() {
		StringBuilder html = new StringBuilder();
		html.append("<div style='height: 100%; width: 100%; display: table;' class='emptyStyle'>");
		html.append("<div style='display: table-cell; vertical-align: middle'>").append(contentSet ? emptyTextMessage : "").append("</div></div>");
		
		return html.toString();
	}
	
	private void updateEmptyPanel() {
		emptyPanel.setHTML(getEmptyPanelHtml());
	}
	
	private void exchangeWidget(Widget widget) {
		if (currentWidget == widget)
			return;
		
		boolean doLayout = false;
		if (currentWidget != null) {
			this.remove(currentWidget);
			doLayout = true;
		}
		currentWidget = widget;
		this.setCenterWidget(widget);
		if (doLayout)
			this.doLayout();
	}
	
	private void initializeActions() {
		if (prepareToolBarActions && buttonsList == null) {
			buttonsList = new ArrayList<>();
			
			List<Pair<String, TextButton>> externalButtons = GMEUtil.prepareExternalActionButtons(externalActions);
			if (externalButtons != null)
				buttonsList.addAll(externalButtons);
		}
		
		if (showContextMenu && menuItemsList == null) {
			menuItemsList = new ArrayList<>();
			
			if (defaultContextMenuActionSupplier != null) {
				defaultContextMenuAction = defaultContextMenuActionSupplier.get();
				emptyMenuItem = new MenuItem(defaultContextMenuActionName);
				//emptyMenuItem.setIcon(defaultContextMenuAction.getHoverIcon());
				emptyMenuItem.addSelectionHandler(event -> defaultContextMenuAction.perform(null));
			} else {
				emptyMenuItem = new MenuItem(LocalizedText.INSTANCE.noOptionsAvailable());
				emptyMenuItem.setEnabled(false);
			}
			emptyMenuItem.setVisible(false);
			menuItemsList.add(new Pair<>("Not Known", emptyMenuItem));
			
			List<Pair<String, MenuItem>> externalMenuItems = GMEUtil.prepareExternalMenuItems(externalActions);
			if (externalMenuItems != null)
				menuItemsList.addAll(externalMenuItems);
			
			if (actionManager != null) {
				Widget actionMenu = actionManager.getActionMenu(this, menuItemsList, filterExternalActions);
				if (actionMenu instanceof Menu)
					actionsContextMenu = (Menu) actionMenu;
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void prepareGrid() {
		resultGrid = new Grid<>(new ListStore<GridData>(props.id()), new ColumnModel<>(new ArrayList<>()));
		cellSelectionModel = new CellSelectionModel<>();
		gridSelectionModel = new GridSelectionModel<>();
		gridSelectionModel.setSelectionMode(SelectionMode.MULTI);
		resultGrid.setSelectionModel(cellSelectionModel);
		
		resultGrid.setView(new GridView<GridData>() {
			@Override
			protected void onCellSelect(int row, int col) {
				super.onCellSelect(row, col);
				Element cell = getCell(row, col);
				if (cell != null)
					cell.addClassName("x-grid3-row-selected");
			}
			
			@Override
			protected void onRowSelect(int rowIndex) {
				super.onRowSelect(rowIndex);
				Element row = getRow(rowIndex);
				if (row != null)
					row.addClassName("x-grid3-row-selected");
			}
			
			@Override
			protected void onCellDeselect(int row, int col) {
				super.onCellDeselect(row, col);
				Element cell = getCell(row, col);
				if (cell != null)
					cell.removeClassName("x-grid3-row-selected");
			}
			
			@Override
			protected void onRowDeselect(int rowIndex) {
				super.onRowDeselect(rowIndex);
				Element row = getRow(rowIndex);
				if (row != null)
					row.removeClassName("x-grid3-row-selected");
			}
		});
		resultGrid.getView().setColumnHeader(new ExtendedColumnHeader<>(resultGrid, resultGrid.getColumnModel()));
		
		GridWithoutLinesStyle style = GWT.<GridWithoutLinesResources>create(GridWithoutLinesResources.class).css();
		style.ensureInjected();
		resultGrid.addStyleName(style.gridWithoutLines());
		QuickTip quickTip = new QuickTip(resultGrid);
		ToolTipConfig config = new ToolTipConfig();
		config.setMaxWidth(400);
		config.setDismissDelay(0);
		quickTip.update(config);
		
		resultGrid.getView().setViewConfig(new GridViewConfig<GridData>() {
			@Override
			public String getRowStyle(GridData model, int rowIndex) {
				return "";
			}
			
			@Override
			public String getColStyle(GridData model, ValueProvider<? super GridData, ?> valueProvider, int rowIndex, int colIndex) {
				return "gmeGridColumn";
			}
		});
		
		resultGrid.getStore().addStoreAddHandler(event -> exchangeWidget(resultGrid));
		
		resultGrid.getStore().addStoreRemoveHandler(event -> {
			if (resultGrid.getStore().size() == 0)
				exchangeWidget(getEmptyPanel());
		});
		
		resultGrid.getStore().addStoreClearHandler(event -> exchangeWidget(getEmptyPanel()));
		
		resultGrid.addCellClickHandler(event -> fireClickOrDoubleClick(true, new SelectResultPanelMouseInteractionEvent(event, SelectResultPanel.this)));
		
		resultGrid.addCellDoubleClickHandler(event -> {
			fireClickOrDoubleClick(false, new SelectResultPanelMouseInteractionEvent(event, SelectResultPanel.this));
			Scheduler.get().scheduleDeferred(this::handleDoubleClick);
		});
		
		new KeyNav(resultGrid) {
			@Override
			public void onEnter(NativeEvent evt) {
				handleDoubleClick();
			}
			
			@Override
			public void onKeyPress(NativeEvent evt) {
				if ((evt.getCtrlKey() || (GXT.isMac() && evt.getMetaKey())) && evt.getKeyCode() == KeyCodes.KEY_C)
					addSelectedModelsToClipBoard();
			}
		};
		
		cellSelectionModel.addCellSelectionChangedHandler(event -> Scheduler.get().scheduleDeferred(this::fireGmSelectionListeners));
		
		gridSelectionModel.addSelectionChangedHandler(event -> Scheduler.get().scheduleDeferred(this::fireGmSelectionListeners));
		
		resultGrid.addBodyScrollHandler(event -> fireWindowChanged());
		
		resultGrid.addResizeHandler(event -> fireWindowChanged());
		
		resultGrid.addAttachHandler(event -> fireWindowChanged());
	}
	
	private void addSelectedModelsToClipBoard() {
		if (cellSelectionActive) {
			CellSelection<GridData> cell = cellSelectionModel.getSelectCell();
			if (cell != null) {
				ObjectAndType objectAndType = cell.getModel().getObjectAndType(cell.getCell());
				if (!objectAndType.password)
					ClipboardUtil.copyTextToClipboard(prepareDisplayValue(objectAndType, false));
			}
			
			return;
		}
		
		List<GridData> selectedItems = resultGrid.getSelectionModel().getSelectedItems();
		if (selectedItems == null)
			return;
		
		StringBuilder text = new StringBuilder();
		boolean addLineBreak = false;
		for (GridData gridData : selectedItems) {
			ObjectAndType objectAndType = gridData.getObjectAndType(0);
			if (objectAndType.password)
				continue;
			
			if (addLineBreak)
				text.append("\n");
			text.append(prepareDisplayValue(objectAndType, false));
			
			addLineBreak = true;
		}
		
		ClipboardUtil.copyTextToClipboard(text.toString());
	}
	
	private void handleDoubleClick() {
		ModelPath modelPath = getFirstSelectedItem();
		if (modelPath != null && modelPath.last().getValue() instanceof GenericEntity)
			fireWorkWithEntity(modelPath);
	}
	
	private void setContent(ModelPath modelPath, boolean initialData) {
		ProfilingHandle ph = Profiling.start(getClass(), initialData ? "Setting content" : "Adding content", false);
		boolean addingInitialElements = initialData ? initialData : addedModelPaths == null || addedModelPaths.isEmpty();
		
		if (initialData) {
			if (addedModelPaths != null)
				addedModelPaths.clear();
		} else {
			if (addedModelPaths == null)
				addedModelPaths = new ArrayList<ModelPath>();
			addedModelPaths.add(modelPath);
		}
		
		if (modelPath != null) {
			ProfilingHandle ph2 = Profiling.start(getClass(), "Configuring top level object", false);
			configureTopLevelObject(modelPath.last().getValue(), /*modelPath.last().getType(),*/ addingInitialElements);
			ph2.stop();
			ProfilingHandle ph3 = Profiling.start(getClass(), "The rest in setContent", false);
			
			//if (modelPath.last() instanceof PropertyRelatedModelPathElement)
				//addManipulationListener((PropertyRelatedModelPathElement) modelPath.last());
			
			contentSet = true;
			if (addingInitialElements)
				fireContentSet();
			if (resultGrid.getStore().size() == 0)
				deferredShowEmptyPanel();
			ph3.stop();
		} else if (initialData) {
			if (resultGrid.getStore().size() > 0)
				resultGrid.getStore().clear();
			else
				deferredShowEmptyPanel();
		}
		ph.stop();
	}
	
	private void deferredShowEmptyPanel() {
		Scheduler.get().scheduleDeferred(() -> {
			if (resultGrid.getStore().size() == 0)
				exchangeWidget(getEmptyPanel());
		});
	}
	
	/**
	 * Configures the top level object, the one used for building up the SelectResultPanel models.
	 * This will be a list. There are 3 possible elements in the list: {@link ListRecord}s or {@link MapRecord}s.
	 * The last possible value is a direct list of simple elements.
	 * @param initialData - true when adding the initial data (either through setContent, or addContent).
	 */
	private void configureTopLevelObject(Object object, /*GenericModelType modelType, */boolean initialData) {
		if (initialData) {
			if (resultGrid.getStore().size() > 0)
				resultGrid.getStore().clear();
			Scheduler.get().scheduleDeferred(() -> {
				if (resultGrid.isVisible())
					resultGrid.getView().refresh(true);
			});
		}
		
		List<?> list;
		if (!(object instanceof List))
			return;
		else {
			list = (List<?>) object;
			if (list.isEmpty())
				return;
		}
		
		List<Boolean> isPasswordList = new ArrayList<>();
		List<String> columnNames = null;
		List<GridData> models = new ArrayList<>();
		for (Object element : list) {
			List<Object> values = null;
			if (element instanceof ListRecord)
				values = ((ListRecord) element).getValues();
			else if (element instanceof MapRecord) {
				values = new ArrayList<>();
				Map<String, Object> map = ((MapRecord) element).getValues();
				if (map != null && !map.isEmpty()) {
					if (columnNames == null) {
						columnNames = new ArrayList<>();
						columnNames.addAll(map.keySet());
						for (int i = 0; i < map.size(); i++)
							isPasswordList.add(false);
					}
					values.addAll(map.values());
				}
			} else {
				values = new ArrayList<>();
				values.add(element);
			}
			
			List<ObjectAndType> objectAndTypes = null;
			if (values != null && !values.isEmpty()) {
				objectAndTypes = new ArrayList<>();
				
				if (columnNames == null) {
					if (querySelectionList != null) {
						columnNames = querySelectionList.stream().map(s -> s.getAlias()).collect(Collectors.toList());
						isPasswordList.clear();
						
						for (int i = 0; i < querySelectionList.size(); i++) {
							Object operand = querySelectionList.get(i).getOperand();
							if (operand instanceof PropertyOperand) {
								PropertyOperand propertyOperand = (PropertyOperand) operand;
								Source source = propertyOperand.getSource();
								if (source instanceof From) {
									From from = (From) source;
									isPasswordList.add(isPropertyPassword(propertyOperand.getPropertyName(), from.getEntityTypeSignature()));
								} else
									isPasswordList.add(false);
							} else
								isPasswordList.add(false);
						}
						
					} else {
						columnNames = new ArrayList<>();
						isPasswordList.clear();
						for (int i = 0; i < values.size(); i++) {
							columnNames.add(LocalizedText.INSTANCE.column() + " " + i);
							isPasswordList.add(false);
						}
					}
				}
				
				int i = 0;
				for (Object value : values) {
					ObjectAndType objectAndType = new ObjectAndType(value, GMF.getTypeReflection().getType(value));
					objectAndType.password = isPasswordList.get(i);
					objectAndTypes.add(objectAndType);
					i++;
				}
			}
			models.add(new GridData(objectAndTypes));
		}
		
		if (initialData) {
			cellSelectionActive = columnNames.size() > 1;
			resultGrid.setSelectionModel(cellSelectionActive ? cellSelectionModel : gridSelectionModel);
			prepareColumns(columnNames);
		}
		resultGrid.getStore().addAll(models);
	}
	
	private boolean isPropertyPassword(String propertyName, String entityTypeSignature) {
		return GMEMetadataUtil.isPropertyPassword(gmSession.getModelAccessory().getMetaData().useCase(useCase).lenient(true)
				.entityTypeSignature(entityTypeSignature).property(propertyName));
	}

	private void prepareColumns(List<String> columnNames) {
		List<ColumnConfig<GridData, ?>> columns = new ArrayList<>();
		int columnIndex = 0;
		for (String columnName : columnNames) {
			Integer width = 250;
			StorageColumnInfo columnInfo = getColumnInfo(columnName);
			if (columnInfo != null) {
				if (columnInfo.getTitle() != null)
					columnName = I18nTools.getLocalized(columnInfo.getTitle());
				
				if (columnInfo.getWidth() != 0)
					width = columnInfo.getWidth();
			}
			
			ColumnConfig<GridData, GridData> column = new ColumnConfig<>(new IdentityValueProvider<>(), width, columnName);
			column.setCell(new AbstractCell<GridData>("click") {
				@Override
				public void render(com.google.gwt.cell.client.Cell.Context context, GridData model, SafeHtmlBuilder sb) {
					ObjectAndType objectAndType = model.getObjectAndType(context.getColumn());
					sb.appendHtmlConstant(prepareValueRendererString(objectAndType, prepareDisplayValue(objectAndType)));
				}
				
				@Override
				public void onBrowserEvent(com.google.gwt.cell.client.Cell.Context context, Element parent, GridData model, NativeEvent event,
						ValueUpdater<GridData> valueUpdater) {
					EventTarget eventTarget = event.getEventTarget();
					if (Element.is(eventTarget)) {
						String cls = Element.as(eventTarget).getClassName();
						boolean isSpecial = false;
						for (String specialStyle : specialUiElementsStyles) {
							if (cls.contains(specialStyle)) {
								isSpecial = true;
								break;
							}
						}
						
						if (isSpecial) {
							event.stopPropagation();
							event.preventDefault();
						} else
							super.onBrowserEvent(context, parent, model, event, valueUpdater);
						handleColumnClick(cls, event);
					}
				}
			});
			
			final int theColumnIndex = columnIndex;
			column.setComparator((o1, o2) -> {
				Object value = o1.getObjectAndType(theColumnIndex).getObject();
				if (value instanceof Comparable<?>)
					return ((Comparable<Object>) value).compareTo(o2.getObjectAndType(theColumnIndex).getObject());
				return 0;
			});
			
			columnIndex++;
			columns.add(column);
		}
		
		ColumnModel<GridData> columnModel = new ColumnModel<GridData>(columns);
		resultGrid.reconfigure(resultGrid.getStore(), columnModel);
	}
	
	private StorageColumnInfo getColumnInfo(String columnName) {
		if (columnData == null)
			return null;
		
		return columnData.getDisplayPaths().stream().filter(columnInfo -> columnName.equals(columnInfo.getPath())).findAny().orElse(null);
	}
	
	private void fireContentSet() {
		if (contentViewListeners != null)
			contentViewListeners.forEach(listener -> listener.onContentSet(this));
	}
	
	/*private void addManipulationListener(PropertyRelatedModelPathElement propertyPathElement) {
		GenericEntity parentEntity = propertyPathElement.getEntity();
		if (parentEntity instanceof SessionAttachable && ((SessionAttachable) parentEntity).accessSession() == null)
			((SessionAttachable) parentEntity).attachSession(getGmSession());
		
		if (entitiesWithManipulationListeners.add(parentEntity))
			gmSession.listeners().entity(parentEntity).add(this);
	}*/
	
	private String prepareValueRendererString(ObjectAndType objectAndType, String displayValue) {
		String valueIcon = null;
		if (objectAndType.getObject() instanceof String && ((String) objectAndType.getObject()).isEmpty())
			valueIcon = emptyStringImageString;
		
		String description = displayValue;
		if (displayValue.contains("<")) //is HTML
			description = null;
		
		return "<div class='" + SelectResultPanelResources.INSTANCE.css().propertyValue() + "'>" + prepareMenuTable(displayValue, description, valueIcon)  + "</div>";
	}
	
	private String prepareDisplayValue(ObjectAndType objectAndType) {
		return prepareDisplayValue(objectAndType, true);
	}
	
	private String prepareDisplayValue(ObjectAndType objectAndType, boolean escapeHtml) {
		if (objectAndType.getType().getJavaType() != Boolean.class) {
			String valueDisplay = prepareValueDisplayOrFlowValueDisplay(objectAndType, escapeHtml);
			if (objectAndType.password)
				return GMEUtil.preparePasswordString(valueDisplay);
			return valueDisplay;
		}
		
		Boolean propertyValue = (Boolean) objectAndType.getObject();
		String booleanClass;
		
		SelectResultPanelCss css = SelectResultPanelResources.INSTANCE.css();
		if (propertyValue == null)
			booleanClass = css.checkNullReadOnlyValue();
		else
			booleanClass = propertyValue ? css.checkedReadOnlyValue() : css.uncheckedReadOnlyValue();
			
		String display = "<div class='" + booleanClass;
		if (propertyValue != null)
			display += " " + (propertyValue ? "CHECKED" : "UNCHECKED");
		return display + "'/>";
	}
	
	private String prepareValueDisplayOrFlowValueDisplay(ObjectAndType objectAndType, boolean escapeHtml) {
		String valueDisplay = null;
		GenericModelType propertyType = objectAndType.getType();
		Object propertyValue = objectAndType.getObject();
		if (propertyValue != null) {
			if (specialRenderersRegistry != null) {
				Codec<Object, String> renderer = specialRenderersRegistry.getCodec(propertyType.getJavaType());
				if (renderer != null) {
					if (renderer instanceof GmSessionHandler)
						((GmSessionHandler) renderer).configureGmSession(gmSession);
					try {
						valueDisplay = renderer.encode(propertyValue);
					} catch (CodecException e) {
						logger.error("Error while getting renderer value.", e);
						e.printStackTrace();
					}
				}
			}
			
			if (valueDisplay == null)
				valueDisplay = prepareStringValue(propertyValue, propertyType, escapeHtml);
		}/* else if (propertyValue != null && propertyType instanceof EntityType) {
			valueDisplay = "<div class='" + PropertyPanelResources.INSTANCE.css().propertyEntity() + "'>" + propertyValue.toString() + "...</div>";			
		}*/
		
		return valueDisplay != null ? valueDisplay : (propertyValue != null ? propertyValue.toString() : "");
	}
	
	private String prepareStringValue(Object propertyValue, GenericModelType valueType, boolean escapeHtml) {
		if (propertyValue == null)
			return "";
		
		String stringValue = null;
		if (valueType == null)
			valueType = GMF.getTypeReflection().getType(propertyValue);
		if (codecRegistry != null) {
			Codec<Object, String> codec = codecRegistry.getCodec(valueType.getJavaType());
			if (codec != null) {
				try {
					stringValue = codec.encode(propertyValue);
				} catch (CodecException e) {
					logger.error("Error while getting value renderer value.", e);
					e.printStackTrace();
				}
			}
		}
		
		if (stringValue == null) {
			if (valueType.isEntity()) {
				String selectiveInformation = SelectiveInformationResolver.resolve((EntityType<?>) valueType, (GenericEntity) propertyValue,
						gmSession.getModelAccessory().getMetaData(), useCase/*, null*/);
				if (selectiveInformation != null && !selectiveInformation.trim().isEmpty())
					stringValue = selectiveInformation;
			} else if (valueType.isEnum()) {
				String enumString = propertyValue.toString();
				Name displayInfo = GMEMetadataUtil.getName(valueType, propertyValue, gmSession.getModelAccessory().getMetaData(), useCase);
				if (displayInfo != null && displayInfo.getName() != null)
					enumString = I18nTools.getLocalized(displayInfo.getName());
				stringValue = enumString;
			}
		}
		
		String returnValue = stringValue != null ? stringValue : propertyValue.toString();
		return escapeHtml ? SafeHtmlUtils.htmlEscape(returnValue) : returnValue;
	}
	
	private String prepareMenuTable(String display, String description, String icon) {
		return prepareMenuTable(display, description, icon, true);
	}
	
	private String prepareMenuTable(String display, String description, String icon, boolean showMenu) {
		StringBuilder builder = new StringBuilder();
		builder.append("<table class='").append(SelectResultPanelResources.INSTANCE.css().inheritFont()).append(" ").append(SelectResultPanelResources.INSTANCE.css().tableFixedLayout());
		builder.append("' border='0' cellpadding='2' cellspacing='0'>\n");
		builder.append("   <tr class='").append(SelectResultPanelResources.INSTANCE.css().inheritFont()).append("'>\n");
		if (icon != null)
			builder.append("      <td class='gxtReset' width='14px'>").append(icon).append("&nbsp;</td>\n");
		builder.append("      <td ");
		if (description != null)
			builder.append("qtip='").append(description).append("' ");
		builder.append("class='gxtReset ").append(SelectResultPanelResources.INSTANCE.css().inheritFont()).append(" ").append(SelectResultPanelResources.INSTANCE.css().textOverflowNoWrap());
		//if (isEntity && !display.isEmpty())
			//builder.append(" ").append(PropertyPanelResources.INSTANCE.css().propertyEntity());			 
		builder.append("' width='100%'>").append(display).append("</td>\n");
		builder.append("      <td width='14px' class='gxtReset ");
		if (showMenu)
			builder.append(SelectResultPanelResources.INSTANCE.css().propertyMenu());
		builder.append("' style='height: 14px;");
		builder.append(" padding-right: 9px;").append("'></td>\n");
		builder.append("   </tr>\n</table>");
		return builder.toString();
	}
	
	private void fireClickOrDoubleClick(boolean click, GmMouseInteractionEvent event) {
		if (gmInteractionListeners != null) {
			List<GmInteractionListener> listenersCopy = new ArrayList<>(gmInteractionListeners);
			listenersCopy.forEach(listener -> {
				if (click)
					listener.onClick(event);
				else
					listener.onDblClick(event);
			});
		}
	}
	
	private void fireGmSelectionListeners() {
		if (gmSelectionListeners != null) {
			List<GmSelectionListener> listenersCopy = new ArrayList<>(gmSelectionListeners);
			listenersCopy.forEach(l -> l.onSelectionChanged(this));
		}
	}
	
	private void updateEmptyMenuItem() {
		boolean emptyMenu = true;
		for (int i = 0; i < actionsContextMenu.getWidgetCount(); i++) {
			Widget menuItem = actionsContextMenu.getWidget(i);
			if (menuItem != emptyMenuItem && menuItem instanceof Component && !isComponentHidden((Component) menuItem)) {
				emptyMenu = false;
				break;
			}
		}
		
		emptyMenuItem.setVisible(emptyMenu);
	}
	
	private void fireWorkWithEntity(ModelPath modelPath) {
		ModelAction action = actionManager.getWorkWithEntityAction(this);
		if (action != null) {
			action.updateState(Collections.singletonList(Collections.singletonList(modelPath)));
			action.perform(null);
		}
	}
	
	private void handleColumnClick(String cls, final NativeEvent event) {
		if (cls.contains(SelectResultPanelResources.INSTANCE.css().propertyMenu()))
			Scheduler.get().scheduleDeferred(() -> {
				updateEmptyMenuItem();
				actionsContextMenu.showAt(event.getClientX(), event.getClientY());
			});
	}
	
	protected void fireWindowChanged() {
		if (windowChangedTimer == null) {
			windowChangedTimer = new Timer() {
				@Override
				public void run() {
					if (isVisible() && gmViewportListeners != null)
						gmViewportListeners.forEach(l -> l.onWindowChanged(SelectResultPanel.this));
				}
			};
		}
		
		windowChangedTimer.schedule(200);
	}
	
	private native boolean isComponentHidden(Component component) /*-{
		return component.@com.sencha.gxt.widget.core.client.Component::hidden;
	}-*/;
	
	interface GridDataProperties extends PropertyAccess<GridData> {
		ModelKeyProvider<GridData> id();
	}
	
	public static class GridData {
		private final int id;
		private final List<ObjectAndType> objectAndTypes;
		
		public GridData(List<ObjectAndType> objectAndTypes) {
			id = idCounter++;
			this.objectAndTypes = objectAndTypes;
		}
		
		public int getId() {
			return id;
		}
		
		public ObjectAndType getObjectAndType(int index) {
			if (objectAndTypes != null)
				return objectAndTypes.get(index);
			
			return null;
		}
	}
	
	private static class ObjectAndType {
		private final Object object;
		private final GenericModelType type;
		private boolean password;
		
		public ObjectAndType(Object object, GenericModelType type) {
			this.object = object;
			this.type = type;
		}

		public Object getObject() {
			return object;
		}

		public GenericModelType getType() {
			return type;
		}
	}
	
	@Override
	public void disposeBean() throws Exception {
		if (gmViewportListeners != null)
			gmViewportListeners.clear();
		
		if (buttonsList != null) {
			buttonsList.clear();
			buttonsList = null;
		}
		
		if (externalActions != null) {
			externalActions.clear();
			externalActions = null;
		}
		
		if (menuItemsList != null) {
			menuItemsList.clear();
			menuItemsList = null;
		}
		
		if (addedModelPaths!= null) {
			addedModelPaths.clear();
			addedModelPaths = null;
		}
		
		if (contentViewListeners != null) {
			contentViewListeners.clear();
			contentViewListeners = null;
		}
		
		if (gmSelectionListeners != null) {
			gmSelectionListeners.clear();
			gmSelectionListeners = null;
		}
		
		if (gmInteractionListeners != null) {
			gmInteractionListeners.clear();
			gmInteractionListeners = null;
		}
		
		specialUiElementsStyles = null;
		
		if (querySelectionList != null) {
			querySelectionList.clear();
			querySelectionList = null;
		}
	}

}
