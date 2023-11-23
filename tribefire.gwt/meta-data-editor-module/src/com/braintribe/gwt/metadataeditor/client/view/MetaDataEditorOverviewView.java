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
package com.braintribe.gwt.metadataeditor.client.view;

import static com.braintribe.model.processing.session.api.common.GmSessions.getMetaData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.async.client.MultiLoader;
import com.braintribe.gwt.async.client.MultiLoaderResult;
import com.braintribe.gwt.codec.registry.client.CodecRegistry;
import com.braintribe.gwt.genericmodelgxtsupport.client.GMEditorSupport;
import com.braintribe.gwt.gme.constellation.client.expert.UserProvider;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelResources;
import com.braintribe.gwt.gmresourceapi.client.GmImageResource;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.GmSelectionSupport;
import com.braintribe.gwt.gmview.client.GmSessionHandler;
import com.braintribe.gwt.gmview.client.InstanceSelectionData;
import com.braintribe.gwt.gmview.client.SelectionConfig;
import com.braintribe.gwt.gmview.client.WorkWithEntityActionListener;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedColumnHeader;
import com.braintribe.gwt.gxt.gxtresources.gridwithoutlines.client.GridWithoutLinesAppearance;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.metadataeditor.client.MetaDataEditorPanel;
import com.braintribe.gwt.metadataeditor.client.MetaDataEditorUtil;
import com.braintribe.gwt.metadataeditor.client.experts.MetaDataEditorBaseExpert;
import com.braintribe.gwt.metadataeditor.client.experts.MetaDataEditorExpert.CallbackEntityType;
import com.braintribe.gwt.metadataeditor.client.experts.MetaDataEditorOverviewExpert;
import com.braintribe.gwt.metadataeditor.client.experts.MetaDataTypesExpert;
import com.braintribe.gwt.metadataeditor.client.listeners.SelectionListeners;
import com.braintribe.gwt.metadataeditor.client.resources.MetaDataEditorResources;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.enhance.EnhancedEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.EntityTypeMetaData;
import com.braintribe.model.meta.data.EnumConstantMetaData;
import com.braintribe.model.meta.data.EnumTypeMetaData;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.ModelMetaData;
import com.braintribe.model.meta.data.Predicate;
import com.braintribe.model.meta.data.PredicateErasure;
import com.braintribe.model.meta.data.PropertyMetaData;
import com.braintribe.model.meta.data.UniversalMetaData;
import com.braintribe.model.meta.data.display.GroupAssignment;
import com.braintribe.model.meta.data.display.Icon;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.info.GmCustomTypeInfo;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmEnumConstantInfo;
import com.braintribe.model.meta.info.GmEnumTypeInfo;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.meta.override.GmEntityTypeOverride;
import com.braintribe.model.meta.override.GmEnumConstantOverride;
import com.braintribe.model.meta.override.GmEnumTypeOverride;
import com.braintribe.model.meta.override.GmPropertyOverride;
import com.braintribe.model.meta.selector.AccessSelector;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.meta.selector.RoleSelector;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.processing.meta.cmd.builders.ConstantMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.EnumMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.api.transaction.TransactionException;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedCollection;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.KeyNav;
import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.core.shared.FastSet;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.data.shared.Store.StoreFilter;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent.CellDoubleClickHandler;
import com.sencha.gxt.widget.core.client.event.CheckChangeEvent;
import com.sencha.gxt.widget.core.client.event.CheckChangeEvent.CheckChangeHandler;
import com.sencha.gxt.widget.core.client.event.HeaderClickEvent;
import com.sencha.gxt.widget.core.client.event.HeaderClickEvent.HeaderClickHandler;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.RowClickEvent.RowClickHandler;
import com.sencha.gxt.widget.core.client.event.ShowEvent;
import com.sencha.gxt.widget.core.client.event.ShowEvent.ShowHandler;
import com.sencha.gxt.widget.core.client.grid.CellSelectionModel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.GridView;
import com.sencha.gxt.widget.core.client.grid.GridViewConfig;
import com.sencha.gxt.widget.core.client.menu.CheckMenuItem;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.selection.CellSelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.CellSelectionChangedEvent.CellSelectionChangedHandler;
import com.sencha.gxt.widget.core.client.tree.Tree.CheckState;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;
import com.sencha.gxt.widget.core.client.treegrid.TreeGridView;

public class MetaDataEditorOverviewView extends ContentPanel implements InitializableBean, MetaDataEditorProvider, MetaDataOverviewProvider, ManipulationListener, GmSelectionSupport {

	// private static final PropertyPanelCss css = PropertyPanelResources.INSTANCE.css();
	private static final MetaDataEditorOverviewModelProperties props = GWT.create(MetaDataEditorOverviewModelProperties.class);

	private PersistenceGmSession gmSession;
	private PersistenceGmSession workbenchSession;
	private String useCase;
	private String caption;
	private MetaDataEditorOverviewExpert modelExpert;
	private MetaDataResolverProvider metaDataResolverProvider;
	private GMEditorSupport gmEditorSupport;
	private EntityType<? extends MetaData> baseType;
	private CodecRegistry<String> codecRegistry;
	private Map<Class<?>, TraversingCriterion> specialEntityTraversingCriterion;
	private final Set<GenericEntity> entitiesLoading = new HashSet<GenericEntity>();
	//private int maxCollectionSize = 10;
	private Boolean readOnly = true;
	private Boolean useVisibleFilter = false;
	private Set<String> lastFilter = null;
	private NestedTransaction editionNestedTransaction;
	private Object startValue;
	private HTML emptyPanel;
	private final String emptyTextMessage = LocalizedText.INSTANCE.noItemsToDisplay();
	private Widget currentWidget = null;
	private Widget gridWidget = null;

	private Boolean isEmptyStore = true;   //Need this because store.getAll() return only visible items...but if grid is refreshed and is not actually visible...items are also set invisible..!!!
	private TreeStore<MetaDataEditorOverviewModel> store;
	private TreeGrid<MetaDataEditorOverviewModel> grid;
	//private MultiEditorGridInlineEditing<MetaDataEditorOverviewModel> editorGridInline;
	private StoreFilter<MetaDataEditorOverviewModel> storeVisibleFilter;
	private ModelPathElement lastModelPathElement;
	private ModelPath lastModelPath;
	private GenericEntity editingEntity;
	//private Set<String> lastListUseCase;
	//private Set<String> lastListRole;
	//private Set<String> lastListAccess;
	private int lastSelectedColumn = -1;
	private Boolean isColumnsSet = false;
	private final SelectionListeners gmSelectionListeners = new SelectionListeners(this);
	private final Map<String, EntityType<? extends MetaData>> mapMetaDataForEntityType = new LinkedHashMap<>();
	private final Map<String, EntityType<? extends MetaData>> mapMetaDataForProperty = new LinkedHashMap<>();
	private final Map<String, EntityType<? extends MetaData>> mapMetaDataForEnumConstant = new LinkedHashMap<>();
	private final Map<String, ColumnConfig<MetaDataEditorOverviewModel, ?>> mapColumn = new LinkedHashMap<>();
	private final Map<String, Integer> mapColumnWidth = new FastMap<Integer>();
	private final Map<String, String> mapStringColumn = new FastMap<String>();
	private final Map<String, EntityType<? extends MetaData>> mapColumnsEntityType = new FastMap<EntityType<? extends MetaData>>();
	private List<Class<? extends GenericEntity>> listAllowTypes;
	private Collection<?> overridenCollection = null; 
	private Boolean needUpdate = true;
	private SortDir sortDirAscii = null;
	private SortDir sortDirDependency = null;
	private Folder rootFolder = null;
	private MetaDataEditorPanel panel = null;
	private String searchText;
	private Boolean useSearchDeclaredTypesOnly;
	private Boolean isSearchMode = false;
	private Boolean isColumnsChange = false;
	private Supplier<? extends Function<SelectionConfig, ? extends Future<InstanceSelectionData>>> selectionFutureProviderProvider;
	private Set<Class<?>> specialFlowClasses = new HashSet<Class<?>>();		

	//private ColumnConfig<MetaDataEditorOverviewModel, MetaData> ccDeletable;
	private ColumnConfig<MetaDataEditorOverviewModel, MetaDataEditorOverviewModel> ccItemName;
	private ColumnConfig<MetaDataEditorOverviewModel, MetaDataEditorOverviewModel> ccModelName;
	private ColumnConfig<MetaDataEditorOverviewModel, MetaDataEditorOverviewModel> ccOwnerName;

	private UserProvider userProvider;

	private String userName = null;


	private static final String COLUMN_MODEL_NAME = "ccModelName";
	private static final String COLUMN_OWNER_NAME = "ccOwnerName";
	
	private static final String ROOT_FOLDER_NAME_OVERVIEW = "MetaDataEditorOverview";
	private static final String ROOT_FOLDER_NAME_MODEL = "MetaDataEditorOverviewModelColumn";
	private static final String ROOT_FOLDER_NAME_ENTITY_TYPE = "MetaDataEditorOverviewEntityTypeColumn";
	private static final String ROOT_FOLDER_NAME_ENUM_TYPE = "MetaDataEditorOverviewEnumTypeColumn";	
	
	private static final int DEFAULT_DYNAMIC_COLUMN_WIDTH = 70;
	private static final int DEFAULT_PAGE_ITEMS_COUNT = 30;
	
	@Override
	public void intializeBean() throws Exception {
	    setHeaderVisible(false);
		setBorders(false);
		setBodyBorder(false);
		this.gridWidget = prepareGrid();
		add(this.gridWidget);
		this.currentWidget = this.gridWidget;
		GridWithoutLinesAppearance.GridWithoutLinesStyle style = GWT.<GridWithoutLinesAppearance.GridWithoutLinesResources> create(GridWithoutLinesAppearance.GridWithoutLinesResources.class).css();
		style.ensureInjected();
		this.grid.addStyleName(style.gridWithoutLines());
		addStyleName(PropertyPanelResources.INSTANCE.css().propertyPanelWithoutLines());
		//.grid .grid-rowSelected .cell .grid-cellSelected, .grid-rowSelected .rowWrap, .grid-cellSelected {
		//.grid {
		//	 {
		//	  background-color: #FFE6BA !important;
		//	}	
		
		//RVE - Workaround for faster switch between tabs if Store have hundreds of items
		this.addHideHandler(new HideHandler() {			
			@Override
			public void onHide(HideEvent event) {
				if (MetaDataEditorOverviewView.this.store.getAllItemsCount()  > 2*DEFAULT_PAGE_ITEMS_COUNT)
					MetaDataEditorOverviewView.this.grid.hide();
			}
		});
		this.addShowHandler(new ShowHandler() {			
			@Override
			public void onShow(ShowEvent event) {
				if (!MetaDataEditorOverviewView.this.grid.isVisible())
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {				
						@Override
						public void execute() {
							MetaDataEditorOverviewView.this.grid.show();
							updateFilteredModels();
							MetaDataEditorOverviewView.this.gmSelectionListeners.fireListeners();
						}
					});			
			}
		});
	}
	
	@Override
	public void setMetaDataEditorPanel(MetaDataEditorPanel panel) {
		this.panel = panel;		
	}

	@Override
	public MetaDataEditorPanel getMetaDataEditorPanel() {
		return this.panel;
	}
	
	//create cell grid - columns, store, editor
	@SuppressWarnings("unused")
	public Widget prepareGrid()  {
		if (this.grid != null) 
			return this.grid;

		this.store = new TreeStore<MetaDataEditorOverviewModel>(props.key());						
		
		
		this.storeVisibleFilter = new StoreFilter<MetaDataEditorOverviewModel>() {
            @Override
			public boolean select(Store<MetaDataEditorOverviewModel> store,	MetaDataEditorOverviewModel parent, MetaDataEditorOverviewModel model) {
               	return (model != null) ? model.getIsVisible() : false;
			}
        };
		this.store.addFilter(this.storeVisibleFilter);		
        /*
		this.store.setEnableFilters(true);
		*/
		
		//column Clickable Name
		this.ccItemName = new ColumnConfig<MetaDataEditorOverviewModel, MetaDataEditorOverviewModel>(props.model(), 200, LocalizedText.INSTANCE.labelName());
		this.ccItemName.setCellPadding(false);
		this.ccItemName.setSortable(false);
		this.ccItemName.setHideable(false);		
		this.ccItemName.setCell(new AbstractCell<MetaDataEditorOverviewModel>() {
			@Override
			public void render(Cell.Context context, MetaDataEditorOverviewModel value, SafeHtmlBuilder sb) {				
				
				ImageResource columnIcon = null;
				if (value.getValue() instanceof GmEntityType) 
					columnIcon = MetaDataEditorResources.INSTANCE.entityTypes16();
				if (value.getValue() instanceof GmEnumType) 
					columnIcon = MetaDataEditorResources.INSTANCE.enumTypes16();
				if (columnIcon != null) {
					String iconUrl = columnIcon.getSafeUri().asString();
					String iconHtml = "<img src=\"" + iconUrl + "\"/>";					
					sb.appendHtmlConstant(MetaDataEditorUtil.appendStringCellIcon(iconHtml)).appendEscaped("  ");
					
				}
							
				String itemName = getItemName(value);
				if (itemName != null) {
					Boolean useGray = (value.getGmDependencyLevel() >= 1);
					if (useGray) {
					    sb.appendHtmlConstant(MetaDataEditorUtil.appendStringCellMain(MetaDataEditorUtil.appendStringGray(itemName)));
					} else {
					    sb.appendHtmlConstant(MetaDataEditorUtil.appendStringCellMain(itemName));
					}
				}								
			}
		});		
		
		//column Declaring Model - Name
		this.ccModelName = new ColumnConfig<MetaDataEditorOverviewModel, MetaDataEditorOverviewModel>(props.declaredModel(), 150, LocalizedText.INSTANCE.labelDeclaredModel());
		this.ccModelName.setCellPadding(false);
		this.ccModelName.setSortable(false);
		this.ccModelName.setCell(new AbstractCell<MetaDataEditorOverviewModel>() {
			@Override
			public void render(Cell.Context context, MetaDataEditorOverviewModel value, SafeHtmlBuilder sb) {
				GmMetaModel metaModel = null;
				if (value.getValue() instanceof GmCustomTypeInfo)   //GmEntityType, GmEnumType
					metaModel = ((GmCustomTypeInfo) value.getValue()).getDeclaringModel();
				else if (value.getValue() instanceof GmProperty && ((GmProperty) value.getValue()).getDeclaringType() != null) 
					metaModel = ((GmProperty) value.getValue()).getDeclaringType().getDeclaringModel();
				else if (value.getValue() instanceof GmEnumConstant && ((GmEnumConstant) value.getValue()).getDeclaringType() != null) 
					metaModel = ((GmEnumConstant) value.getValue()).getDeclaringType().getDeclaringModel();
				
				if (metaModel != null) {
					EntityType<GenericEntity> entityType = metaModel.entityType();
					String selectiveInformation = SelectiveInformationResolver.resolve(entityType, metaModel, (ModelMdResolver) null, MetaDataEditorOverviewView.this.useCase/*, null*/);
					if (selectiveInformation != null && !selectiveInformation.trim().equals("")) {
						Boolean useGray = (value.getGmDependencyLevel() >= 1);
						if (useGray) {
						    sb.appendHtmlConstant(MetaDataEditorUtil.appendStringCell(MetaDataEditorUtil.appendStringGray(selectiveInformation)));
						} else {					
							//sb.appendEscaped(selectiveInformation);
							sb.appendHtmlConstant(MetaDataEditorUtil.appendStringCell(selectiveInformation));
						}
					}					    
				}
			}
		});
		
		//column Declaring Owner (GmEntityType) - Name			
		this.ccOwnerName = new ColumnConfig<MetaDataEditorOverviewModel, MetaDataEditorOverviewModel>(props.declaredOwner(), 150, LocalizedText.INSTANCE.labelDeclaredEntity());
		this.ccOwnerName.setCellPadding(false);
		this.ccOwnerName.setSortable(false);
		this.ccOwnerName.setCell(new AbstractCell<MetaDataEditorOverviewModel>() {
			@Override
			public void render(Cell.Context context, MetaDataEditorOverviewModel value, SafeHtmlBuilder sb) {
				GenericEntity owner = null;
				if (value.getValue() instanceof GmProperty) 
					owner = ((GmProperty) value.getValue()).getDeclaringType();
				if (value.getValue() instanceof GmEnumConstant) 
					owner = ((GmEnumConstant) value.getValue()).getDeclaringType();
				if (owner != null) {
					EntityType<GenericEntity> entityType = owner.entityType();
					String selectiveInformation = SelectiveInformationResolver.resolve(entityType, owner, (ModelMdResolver) null, MetaDataEditorOverviewView.this.useCase);
					if (selectiveInformation != null && !selectiveInformation.trim().equals("")) {
						Boolean useGray = (value.getGmDependencyLevel() >= 1);
						if (useGray) {
						    sb.appendHtmlConstant(MetaDataEditorUtil.appendStringCell(MetaDataEditorUtil.appendStringGray(selectiveInformation)));
						} else {					
							sb.appendHtmlConstant(MetaDataEditorUtil.appendStringCell(selectiveInformation));
						}
					}					    
				}
			}
		});
		
		ColumnModel<MetaDataEditorOverviewModel> cm = new ColumnModel<MetaDataEditorOverviewModel>(Arrays.<ColumnConfig<MetaDataEditorOverviewModel, ?>> asList(this.ccItemName,this.ccModelName, this.ccOwnerName));
		this.grid = new TreeGrid<MetaDataEditorOverviewModel>(this.store, cm, this.ccItemName);			
		this.grid.setView(new TreeGridView<MetaDataEditorOverviewModel>() {
			@Override
			protected void onRowSelect(int rowIndex) {
				super.onRowSelect(rowIndex);
				Element row = getRow(rowIndex);
			    if (row != null)
			    	row.addClassName("x-grid3-row-selected");
			}
			
			@Override
			protected void onRowDeselect(int rowIndex) {
				super.onRowDeselect(rowIndex);
				Element row = getRow(rowIndex);
			    if (row != null)
			    	row.removeClassName("x-grid3-row-selected");
			}
			
			@Override
			protected Menu createContextMenu(int colIndex) {
				/*
				if (colIndex == 0) {
					Menu menu = super.createContextMenu(0);
					//1 - Create MenuItem for adding a new column after the current one and add to the menu
					MenuItem menuItem = new MenuItem(LocalizedText.INSTANCE.addColumn());
					menuItem.setIcon(MetaDataEditorResources.INSTANCE.newInstance());
					Menu subMenu = createMenuItems(true);
					menuItem.setSubMenu(subMenu);
					menu.add(menuItem);	
					return menu;
				}
				*/
				Menu menu = new Menu();
				//1 - Create MenuItem for adding a new column after the current one
				MenuItem menuItem = new MenuItem(LocalizedText.INSTANCE.columns());
				//menuItem.setIcon(MetaDataEditorResources.INSTANCE.newInstance());
				
				menuItem.setIcon(header.getAppearance().columnsIcon());
				Menu subMenu = createMenuItems(true, false, colIndex);
				menuItem.setSubMenu(subMenu);
				menu.add(menuItem);	
				/*
				//2 - Create a MenuItem for removing the current column
				menuItem = new MenuItem(LocalizedText.INSTANCE.hideColumn());
				menuItem.setIcon(MetaDataEditorResources.INSTANCE.remove16());
				menuItem.addSelectionHandler(new SelectionHandler<Item>() {					
					@Override
					public void onSelection(SelectionEvent<Item> event) {
						hideColumn(colIndex, true);						
					}
				});
				menu.add(menuItem);	
				*/
				//3 - Create a MenuItem for changing this column to another one
				if (colIndex != 0) {
					menuItem = new MenuItem(LocalizedText.INSTANCE.changeColumn());
					menuItem.setIcon(MetaDataEditorResources.INSTANCE.change16());
					subMenu = createMenuItems(false, true, colIndex);
					menuItem.setSubMenu(subMenu);
					menu.add(menuItem);
				}
				return menu;
			}

		});
		grid.getView().setColumnHeader(new ExtendedColumnHeader<>(grid, grid.getColumnModel()));
		
		//this.grid.getView().setForceFit(true); //if true fit all columns to size of view area, prevent to use horizontal scrollbar
		this.grid.setBorders(false);
		this.grid.setStyleName("gmePropertyPanel");
		this.grid.setAllowTextSelection(false);
		this.grid.setHideHeaders(!true);
		this.grid.getView().setSortingEnabled(true);
		/*
		this.grid.addHeaderClickHandler(new HeaderClickHandler() {				
			@Override
			public void onHeaderClick(HeaderClickEvent event) {
				int x = event.getEvent().getClientX();
				MetaDataEditorUtil.setHeaderBarColumnAutoResize(x, event);
			}
		});	
		*/	
		this.grid.getView().setViewConfig(new GridViewConfig<MetaDataEditorOverviewModel>() {
			@Override
			public String getRowStyle(MetaDataEditorOverviewModel model, int rowIndex) {
				return "";
			}
			
			@Override
			public String getColStyle(MetaDataEditorOverviewModel model, ValueProvider<? super MetaDataEditorOverviewModel, ?> valueProvider,
					int rowIndex, int colIndex) {
				return "gmeGridColumn";
			}
		});
		
		this.grid.addCellDoubleClickHandler(new CellDoubleClickHandler() {
			
			@Override
			public void onCellClick(CellDoubleClickEvent event) {
				if (event.getCellIndex() == 0) {
					handleDoubleClick();					
				}
			}
		});
		
		new KeyNav(this.grid) {
		   @Override
		   public void onEnter(NativeEvent evt) {
			    handleDoubleClick();
		   }
		};
			  
		this.grid.addHeaderClickHandler(new HeaderClickHandler() {				
			@Override
			public void onHeaderClick(HeaderClickEvent event) {
				SortDir sortDir;
				switch  (event.getColumnIndex()) {
				case 0: MetaDataEditorOverviewView.this.store.clearSortInfo();
						sortDir = (MetaDataEditorOverviewView.this.sortDirAscii == null) ? SortDir.ASC : ((MetaDataEditorOverviewView.this.sortDirAscii == SortDir.ASC) ? SortDir.DESC : SortDir.ASC);
						doSortByAscii(sortDir);					        
						break;
				case 1: MetaDataEditorOverviewView.this.store.clearSortInfo();
				        sortDir = (MetaDataEditorOverviewView.this.sortDirDependency == null) ? SortDir.ASC : ((MetaDataEditorOverviewView.this.sortDirDependency == SortDir.ASC) ? SortDir.DESC : SortDir.ASC);
						doSortByDependency(sortDir);
				        break;
				default:
					break;
				}
			}
		});
		
		/*
		this.editorGridInline = new MultiEditorGridInlineEditing<MetaDataEditorOverviewModel>(this.grid);
		this.editorGridInline.addBeforeStartEditHandler(new BeforeStartEditEvent.BeforeStartEditHandler<MetaDataEditorOverviewModel>() {
			@Override
			public void onBeforeStartEdit(BeforeStartEditEvent<MetaDataEditorOverviewModel> event) {
				event.getEditCell();
				
				if (editionNestedTransaction != null)
					rollbackTransaction();
				editionNestedTransaction = gmSession.getTransaction().beginNestedTransaction();
				startValue = grid.getStore().get(event.getEditCell().getRow()).getPropertyValue();
			}
		});
		this.editorGridInline.addCompleteEditHandler(new CompleteEditEvent.CompleteEditHandler<MetaDataEditorOverviewModel>() {
			@Override
			public void onCompleteEdit(CompleteEditEvent<MetaDataEditorOverviewModel> event) {
				//event.getEditCell();
				//event.getSource().getEditableGrid().getStore().rejectChanges();
				MetaDataEditorOverviewModel model = grid.getStore().get(event.getEditCell().getRow());

				IsField<?> editor = event.getSource().getEditor(grid.getColumnModel().getColumn(event.getEditCell().getCol()));
				new Timer() {
					@Override
					public void run() {
						grid.getStore().rejectChanges();
					}
				}.schedule(500);
				//gridInlineEditing.setCurrentRow(event.getEditCell().getRow()); needed?
				if (!GMEUtil.isEditionValid(editor.getValue(), startValue, editor)) {
					rollbackTransaction();
					return;
				}
				EntityType<GenericEntity> entityType = (EntityType<GenericEntity>) model.getType();
				entityType.setPropertyValue((GenericEntity) model.getValue(), model.getProperty().getPropertyName(), editor.getValue());
				
				editionNestedTransaction.commit();
				editionNestedTransaction = null;
				
				startValue = null;
				
				store.update(model);
			}
		});

		this.gmEditorSupport = new GMEditorSupport();
		*/

		
		/*
		CellSelectionModel<MetaDataEditorOverviewModel> selectionModel = new CellSelectionModel<MetaDataEditorOverviewModel>() {
			@Override
			protected void onAdd(List<? extends MetaDataEditorOverviewModel> models) {
				super.onAdd(models);
				
				//RVE edit disabled
				//for (MetaDataEditorOverviewModel model : models) {
				//	int rowIndex = this.store.getAll().indexOf(model);
				//	if (model.getProperty() != null) {
						//edit property value
				//		prepareCustomEditor(model, model.getProperty(), ccValue, rowIndex);
				//	}
				//}
			}
		};
		*/
		
		CellSelectionModel<MetaDataEditorOverviewModel> selectionModel = new CellSelectionModel<MetaDataEditorOverviewModel>();
		selectionModel.addCellSelectionChangedHandler(new CellSelectionChangedHandler<MetaDataEditorOverviewModel>() {

			@Override
			public void onCellSelectionChanged(CellSelectionChangedEvent<MetaDataEditorOverviewModel> event) {
				//Set Visible, Emphasized, .. in Resolution View
				int i;
				if (!event.getSelection().isEmpty()) {
					 i = event.getSelection().get(0).getCell();
					 MetaDataEditorOverviewView.this.lastSelectedColumn = i;
					 
					 //DomEvent.fireNativeEvent(Document.get().createSelectElement(), MetaDataEditorOverviewModelProperties.this.grid);
					if (MetaDataEditorOverviewView.this.isVisible())
						MetaDataEditorOverviewView.this.gmSelectionListeners.fireListeners();
				}
			}
		});
		
		this.grid.setSelectionModel(selectionModel);
		
		return this.grid;
	}

	@Override
	public String getCaption() {
		return this.caption;
	}

	@Override
	public void setCaption(String caption) {
		this.caption = caption;
	}

	public void setModelExpert(MetaDataEditorOverviewExpert modelExpert) {
		this.modelExpert = modelExpert;
	}
	
	public void setUseVisibleFilter(Boolean useFilter) {
		this.useVisibleFilter = useFilter;
	}
	
	// ----- GmSessionHandler members -----

	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
		if (this.modelExpert instanceof GmSessionHandler)
			((GmSessionHandler) this.modelExpert).configureGmSession(gmSession);
	}

	@Override
	public PersistenceGmSession getGmSession() {
		return this.gmSession;
	}
	
	public void setUserProvider(UserProvider userProvider) {
		this.userProvider = userProvider;
	}	

	//----WorkbenchSession-----
	
	@Override
	public void configureWorkbenchSession(PersistenceGmSession workbenchSession) {
		this.workbenchSession = workbenchSession;
	}

	
	// ----- UseCaseHandler members -----

	@Override
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
	}

	@Override
	public String getUseCase() {
		return this.useCase;
	}

	@Override
	public void setFilter(Set<String> filter) {
	     this.lastFilter = filter;
	}
	
	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}
	public Boolean getReadOnly() {
		return this.readOnly;
	}

	@Override
	public MetaDataEditorBaseExpert getModelExpert() {
		return this.modelExpert;
	}	

	@Override
	public void setMetaDataResolverProvider(MetaDataResolverProvider metaDataResolverProvider) {
		this.metaDataResolverProvider = metaDataResolverProvider;
	}	
	
	@Override
	public boolean getUseSessionResolver() {
		return false;
	}	
	
	public Boolean getActive() {
		return isVisible();
	}
	
	@Override
	public void setNeedUpdate() {
		this.needUpdate = true;
	}

	@Override
	public void doRefresh() {
		setContent(this.lastModelPath);	
		if (this.isSearchMode)
			applySearchFilter(this.searchText, this.useSearchDeclaredTypesOnly);
	}
	
	/**
	 * Configures the {@link CodecRegistry} used as renderers.
	 */
	@Configurable
	public void setCodecRegistry(CodecRegistry<String> codecRegistry) {
		this.codecRegistry = codecRegistry;
	}
	
	/**
	 * Configures a map containing special traversing criterion for the given entities.
	 * This is used when loading an absent property. Special entities (such as {@link LocalizedString}) require some properties to be loaded.
	 */
	@Configurable
	public void setSpecialEntityTraversingCriterion(Map<Class<?>, TraversingCriterion> specialEntityTraversingCriterion) {
		this.specialEntityTraversingCriterion = specialEntityTraversingCriterion;
	}	
	
	@Override
	public void addMapMetaDataForProperty(String key, EntityType<? extends MetaData> value) {
		this.mapMetaDataForProperty.put(key, value);
	}
	@Override
	public void removeMapMetaDataForProperty(String key) {
		this.mapMetaDataForProperty.remove(key);
	}
	@Override
	public void removeAllMapMetaDataForProperty() {
		this.mapMetaDataForProperty.clear();
	}
	@Override
	public void addAllMapMetaDataForProperty(Map<String, EntityType<? extends MetaData>> value) {
		this.mapMetaDataForProperty.putAll(value);
	}
	@Override
	public void addMapMetaDataForEnumConstant(String key, EntityType<? extends MetaData> value) {
		this.mapMetaDataForEnumConstant.put(key, value);
	}
	@Override
	public void removeMapMetaDataForEnumConstant(String key) {
		this.mapMetaDataForEnumConstant.remove(key);
	}
	@Override
	public void removeAllMapMetaDataForEnumConstant() {
		this.mapMetaDataForEnumConstant.clear();
	}
	@Override
	public void addAllMapMetaDataForEnumConstant(Map<String, EntityType<? extends MetaData>> value) {
		this.mapMetaDataForEnumConstant.putAll(value);
	}
	@Override
	public void removeMapMetaDataForEntityType(String key) {
		this.mapMetaDataForEntityType.remove(key);
	}
	@Override
	public void addAllMapMetaDataForEntityType(Map<String, EntityType<? extends MetaData>> value) {
		this.mapMetaDataForEntityType.putAll(value);
	}
	@Override
	public void removeAllMapMetaDataForEntityType() {
		this.mapMetaDataForEntityType.clear();
	}	
	@Override
	public void addMapMetaDataForEntityType(String key, EntityType<? extends MetaData> value) {
		this.mapMetaDataForEntityType.put(key, value);
	}
	
	@Override
	public void setAllowType(List<Class<? extends GenericEntity>> list) {
		this.listAllowTypes = list;
	}
	
	@Override
	public List<Class<? extends GenericEntity>> getAllowType() {
		return this.listAllowTypes;
	}
	
	@Override
	public boolean getEditorVisible(ModelPathElement pathElement) {
		if (pathElement == null)
			return true;
		
		Boolean visibility = true;		
		if ((pathElement.getValue() instanceof GmProperty) || (pathElement.getValue() instanceof GmPropertyOverride)) {
			visibility = false;
		} else if ((pathElement.getValue() instanceof GmEnumConstant) || (pathElement.getValue() instanceof GmEnumConstantOverride)) {
			visibility = false;
		} else if ((pathElement.getValue() instanceof GmEntityType) || (pathElement.getValue() instanceof GmEntityTypeOverride)) {
			if (this.listAllowTypes != null) {
				visibility = this.listAllowTypes.contains(GmProperty.class);
			}
		} else if ((pathElement.getValue() instanceof GmEnumType) || (pathElement.getValue() instanceof GmEnumTypeOverride)) {
			if (this.listAllowTypes != null) {
				visibility = this.listAllowTypes.contains(GmEnumConstant.class);
			}
		} else if (pathElement.getValue() instanceof GmMetaModel) {
			if (this.listAllowTypes != null) {
				visibility = this.listAllowTypes.contains(GmEntityType.class) || this.listAllowTypes.contains(GmEnumType.class);					
			}
		}
		
		return visibility;
	}
	
	private String getItemName (MetaDataEditorOverviewModel model) {
		String itemName = null;
		if (model.getValue() instanceof MetaData) {
			Name name = null;
			if (model.getValue() != null) {
				name = getMetaData(model.getValue()).entity(model.getValue()).meta(Name.T).exclusive();
			}
			if (name != null && name.getName() != null)
				itemName = I18nTools.getLocalized(name.getName());
		} else {					
			EntityType<GenericEntity> entityType = model.getValue().entityType();
			itemName = SelectiveInformationResolver.resolve(entityType, model.getValue(), (ModelMdResolver) null, MetaDataEditorOverviewView.this.useCase);
		}
		return itemName;
	}
	
	private void handleDoubleClick() {
		//MetaDataEditorOverviewModel model = (MetaDataEditorOverviewModel) event.getSource().getSelectionModel().getSelectedItem();
		MetaDataEditorOverviewModel model = grid.getSelectionModel().getSelectedItem();
		if (model == null)
			return;
		
		ModelPath modelPath = null;							
		Object value = model.getValue();
		RootPathElement pathElement;
		if (value instanceof GmMetaModel) 
			modelPath = new ModelPath();
		else 
			modelPath = MetaDataEditorOverviewView.this.lastModelPath.copy();
								
		GenericModelType type = GMF.getTypeReflection().getType(value);
		pathElement = new RootPathElement(type, value);
		modelPath.add(pathElement);
		
		WorkWithEntityActionListener workWithEntityListener = GMEUtil.getWorkWithEntityActionListener(MetaDataEditorOverviewView.this);
		if (workWithEntityListener != null)
			workWithEntityListener.onWorkWithEntity(modelPath, null, MetaDataEditorOverviewView.this.useCase, false, false);
	}	
	// ----- MetaDataEditorProvider -----

	@Override
	public Boolean isSearchMode() {
		return this.isSearchMode;
	}
	
	@Override
	public void applySearchFilter(String searchText, Boolean useSearchDeclaredTypesOnly) {
		String lowerText = searchText.toLowerCase();
		this.searchText = searchText;
		this.useSearchDeclaredTypesOnly = useSearchDeclaredTypesOnly;
		this.isSearchMode = ((!searchText.isEmpty()) && (!searchText.equals("*"))) || useSearchDeclaredTypesOnly;
		
		
		this.store.setEnableFilters(false);
		int row = 0; 
		for (MetaDataEditorOverviewModel model : MetaDataEditorOverviewView.this.store.getAll()) {								
			if (useSearchDeclaredTypesOnly && model.getGmDependencyLevel() >= 1) {
				model.setIsVisible(false);
			} else {
				if (searchText.equals("*") || searchText.isEmpty())
					model.setIsVisible(true);
				else {
					Boolean founded = false;
					for (int i=0; i < this.grid.getColumnModel().getColumnCount(); i++) {  
						Element element = this.grid.getView().getCell(row, i);
						String celltext = element.getInnerText().toLowerCase();
						founded = celltext.contains(lowerText);
						if (founded)
							break;
					}
					//MetaDataEditorOverviewModel model = grid.getStore().get(event.getEditCell().getRow())
					model.setIsVisible(founded);
				}
				
			}
			row = row + 1;
			//this.store.update(model);
		}	
		this.store.setEnableFilters(true);
	}	
	
	@Override
	public String getSearchDeclaredText() {
		if (this.editingEntity == null)
			return null;
		
		if (this.editingEntity instanceof GmMetaModel) {
			return LocalizedText.INSTANCE.declaredTypesOnly();
		} else if (this.editingEntity instanceof GmEntityTypeInfo) {
			return LocalizedText.INSTANCE.declaredPropertiesOnly();
		}	
		return null;
	}
	
	@Override
	public void setContent(final ModelPath modelPath) {
		//check if all metaData property are already loaded
		ModelPathElement pathElement = (modelPath != null) ? modelPath.last() : null;
		this.lastModelPathElement = pathElement;
		this.lastModelPath = modelPath;
		this.editingEntity = null;
		
		/*
		if (!needUpdate || !getActive()) {
			return;
		} else {
			needUpdate = false;
		}	
		*/	
		
		/*
		if ((this.editorGridInline != null) && (this.editorGridInline.isEditing()))
			this.editorGridInline.completeEditing();
			*/
		
		if (this.editionNestedTransaction != null)
			rollbackTransaction();
		
		if (pathElement == null){
			return;
		}
		
		//lastListUseCase = listUseCase;
		//lastListRole = listRoles;
		//lastListAccess = listAccess;
		this.store.clear();
		isEmptyStore = true;
        
		if (!this.isColumnsSet) {
			this.mapColumn.clear();
			this.mapColumnsEntityType.clear();
			this.mapStringColumn.clear();
		}		

		Object pathValue = pathElement.getValue();
		if (pathValue instanceof GmPropertyInfo) {
			//RVE - Overview Tab not used for PropertyMetaData
			return;
		}
		if (pathValue instanceof GmEnumConstantInfo) {
			//RVE - Overview Tab not used for EnumConstantMetaData
			return;
		}
		if (pathValue instanceof GmEnumTypeInfo) {
			this.grid.mask();
			this.ccOwnerName.setHidden(false);
			this.setCaption(LocalizedText.INSTANCE.constants());
			
			final GmEnumType gmValue;   
			if (pathValue instanceof GmEnumType) {
				gmValue = (GmEnumType) pathValue;
			} else if (pathValue instanceof GmEnumTypeOverride) {
				gmValue = ((GmEnumTypeOverride) pathValue).getEnumType();	
			} else {
				return;
			}
			
			this.editingEntity = gmValue;
			
			//check - if all Property are loaded
			Property properties = gmValue.entityType().getProperty("constants");
			if (checkPropertyAbsense(gmValue, properties)) {
				EntityType<GenericEntity> entityType = gmValue.entityType();
				loadAbsentProperties(gmValue, entityType ,Arrays.asList(properties)).get(new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						MetaDataEditorOverviewView.this.entitiesLoading.remove(gmValue);
						ErrorDialog.show("Error load MetaData Editor Properties", caught);
						caught.printStackTrace();
					}

					@Override
					public void onSuccess(Void result) {
						MetaDataEditorOverviewView.this.entitiesLoading.remove(gmValue);
						new Timer() {
							@Override
							public void run() {
								setContent(modelPath);
							}
						}.schedule(100);
					}
				});
				return;
			}
			
			this.gmSession.listeners().entity(gmValue).remove(this);
			this.gmSession.listeners().entity(gmValue).add(this);
			this.baseType = EnumTypeMetaData.T;
			
			loadVisibleColumns();
			Map<String, EntityType<? extends MetaData>> mapList = new LinkedHashMap<>();
			mapList.putAll(this.mapMetaDataForEnumConstant);
			addDynamicColumns(mapList, -1);
			
			if ((this.listAllowTypes == null) || (this.listAllowTypes.contains(GmEnumConstant.class))) {
					this.overridenCollection = null;
					//if (gmValue.getPropertyOverrides() != null)
					//	collectionGmProperty.addAll(gmValue.getPropertyOverrides());
					//addPropertyAsModelsToGrid(gmValue.getProperties(), gmValue); 	
					addEnumConstantsAsModelsToGrid(getItemEnumConstants(gmValue), gmValue, this.metaDataResolverProvider.getModelMetaDataContextBuilder()); 
					updateFilteredModels();
			}
			this.grid.unmask();
		}
		if (pathValue instanceof GmEntityTypeInfo) {   
			this.grid.mask();
			this.ccOwnerName.setHidden(false);
			this.setCaption(LocalizedText.INSTANCE.properties());
			
			//TODO - simplify after MetaModel update  - >  gmValue = ((GmEntityTypeInfo) pathValue).relatedType();			
			final GmEntityType gmValue;   
			if (pathValue instanceof GmEntityType) {
				gmValue = (GmEntityType) pathValue;
			} else if (pathValue instanceof GmEntityTypeOverride) {
				gmValue = ((GmEntityTypeOverride) pathValue).getEntityType();	
			} else {
				return;
			}
			
			this.editingEntity = gmValue;
			
			//check - if all Property are loaded
			Property properties = gmValue.entityType().getProperty("properties");
			if (checkPropertyAbsense(gmValue, properties)) {
				EntityType<GenericEntity> entityType = gmValue.entityType();
				loadAbsentProperties(gmValue, entityType ,Arrays.asList(properties)).get(new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						MetaDataEditorOverviewView.this.entitiesLoading.remove(gmValue);
						ErrorDialog.show("Error load MetaData Editor Properties", caught);
						caught.printStackTrace();
					}

					@Override
					public void onSuccess(Void result) {
						MetaDataEditorOverviewView.this.entitiesLoading.remove(gmValue);
						new Timer() {
							@Override
							public void run() {
								setContent(modelPath);
							}
						}.schedule(100);
					}
				});
				return;
			}
			
			this.gmSession.listeners().entity(gmValue).remove(this);
			this.gmSession.listeners().entity(gmValue).add(this);
			this.baseType = EntityTypeMetaData.T;
			
			loadVisibleColumns();
			Map<String, EntityType<? extends MetaData>> mapList = new LinkedHashMap<>();
			mapList.putAll(this.mapMetaDataForProperty);
			addDynamicColumns(mapList, -1);
			
			if ((this.listAllowTypes == null) || (this.listAllowTypes.contains(GmProperty.class))) {
					this.overridenCollection = gmValue.getPropertyOverrides();
					//if (gmValue.getPropertyOverrides() != null)
					//	collectionGmProperty.addAll(gmValue.getPropertyOverrides());
					//addPropertyAsModelsToGrid(gmValue.getProperties(), gmValue); 	
					addPropertyAsModelsToGrid(getItemProperties(gmValue, true), gmValue, this.metaDataResolverProvider.getModelMetaDataContextBuilder(), true); 						
					updateFilteredModels();
			}
			this.grid.unmask();
			
			/*
			this.metaDataResolverProvider.provide(new Callback() {
				public void onSuccess(ModelMetaDataContextBuilder future) {
					addPropertyAsModelsToGrid(new ArrayList<GmProperty>(gmValue.getProperties()), gmValue, future);
				}
			});
			*/
		}
		
		if (pathValue instanceof GmMetaModel) {
			this.grid.mask();
			this.ccOwnerName.setHidden(true);
			this.setCaption(LocalizedText.INSTANCE.types());
			
			final GmMetaModel gmValue = (GmMetaModel) pathValue;
			this.editingEntity = gmValue;
			
			/*
			//check - if all Entity are loaded
			List<Property> propList = preparePropertyList(gmValue.getClass());
			final List<Property> absentProperties = new ArrayList<Property>();
			for (Property prop : propList) {
				if (prop.getPropertyName().equals("entityTypes"))
					//if (GMEUtil.checkPropertyAbsense((GmMetaModel) pathElement.getValue(), prop))
					if (checkPropertyAbsense(gmValue, prop))
					{
						absentProperties.add(prop);
					}
			}
			if (!absentProperties.isEmpty()) {
				//EntityType<GenericEntity> entityType = GMF.getTypeReflection().getEntityType(((GmMetaModel) pathElement.getValue()).getClass());							
				EntityType<GenericEntity> entityType = GMF.getTypeReflection().getEntityType(gmValue);			
				
				loadAbsentProperties(gmValue, entityType ,absentProperties).get(new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						entitiesLoading.remove(gmValue);
						if (entitiesLoading.isEmpty())
							 updateFilteredModels();
							 
						ErrorDialog.show("Error load MetaData Editor Properties", caught);
						caught.printStackTrace();
					}

					@Override
					public void onSuccess(Void result) {
						entitiesLoading.remove(gmValue);
						new Timer() {
							@Override
							public void run() {
								setContent(pathElement, modelPath, listUseCase, listRoles, listAccess);
							}
						}.schedule(100);
					}
				});
				return;
			}
			*/
			
			this.gmSession.listeners().entity(gmValue).remove(this);
			this.gmSession.listeners().entity(gmValue).add(this);
			this.baseType = ModelMetaData.T;
			
			loadVisibleColumns();
			Map<String, EntityType<? extends MetaData>> mapList = new LinkedHashMap<>();
			mapList.putAll(this.mapMetaDataForEntityType);
			
			if (this.listAllowTypes != null) {
				if (this.listAllowTypes.contains(GmEntityType.class)) {
					addDynamicColumns(mapList, -1);
					//setCaption(LocalizedText.INSTANCE.informationEntityOverview());
					this.overridenCollection = gmValue.entityOverrideSet();
					
					//if (gmValue.getEntityTypeOverrides() != null)
					//	collectionGmEntityType.addAll(gmValue.getEntityTypeOverrides());
					addEntityTypeAsModelsToGrid(getItemTypes(gmValue, true, true),	this.metaDataResolverProvider.getModelMetaDataContextBuilder(), true, false);
				}
				if (this.listAllowTypes.contains(GmEnumType.class)) {
					addDynamicColumns(mapList, -1);
					this.overridenCollection = gmValue.enumOverrideSet();
					//if (gmValue.getEnumTypeOverrides() != null)
					//	collectionGmEnumType.addAll(gmValue.getEnumTypeOverrides());
					addEnumTypeAsModelsToGrid(getItemTypes(gmValue, true, false), this.metaDataResolverProvider.getModelMetaDataContextBuilder(), true, false); 
				}
			} else {
				addDynamicColumns(mapList, -1);
				//setCaption(LocalizedText.INSTANCE.informationEntityOverview());
				this.overridenCollection = gmValue.entityOverrideSet();
				Collection<GenericEntity> collectionGmEntityType = new ArrayList<GenericEntity>();
				collectionGmEntityType.addAll(gmValue.entityTypeSet());
				collectionGmEntityType.addAll(gmValue.entityOverrideSet());
				addEntityTypeAsModelsToGrid(collectionGmEntityType,	this.metaDataResolverProvider.getModelMetaDataContextBuilder(), true, false); 			
			}
			updateFilteredModels();
		
			/*
			this.metaDataResolverProvider.provide(new Callback() {
				public void onSuccess(ModelMetaDataContextBuilder future) {
					addEntityTypeAsModelsToGrid(new ArrayList<GmEntityType>(gmValue.getEntityTypes()), gmValue, future);
				}
			});
			*/
			this.grid.unmask();
		}
		
		prepareSortAndFilter();
		this.grid.getView().refresh(true);
	}

	private void prepareSortAndFilter() {
		if (!isEmptyStore) {
			doSortByDependency(SortDir.ASC);		
			if (this.isSearchMode)
				applySearchFilter(this.searchText, this.useSearchDeclaredTypesOnly);
			selectFirstItem();
		}
	}
		
	private String getShortName(String fullName) {
		int index = fullName.lastIndexOf(".");
		if (index > 0) {
			index ++;
			return fullName.substring(index);			
		} else 
			return fullName;
	}
	
	private void doSortByAscii(SortDir sortDir) {
		this.store.setEnableFilters(false);
		this.store.clearSortInfo();
		this.store.addSortInfo(new StoreSortInfo<MetaDataEditorOverviewModel>(props.model(), new Comparator<MetaDataEditorOverviewModel>() {
			@Override
			public int compare(MetaDataEditorOverviewModel arg1, MetaDataEditorOverviewModel arg2) {
				if (arg1 == null) {
					return -1;
				} else if(arg2 == null) {
					return 1;
				} 
				
				String name1 = "";
				String name2 = "";
				GenericEntity entity1 = arg1.getValue(); 
				GenericEntity entity2 = arg2.getValue(); 
				if (entity1 != null) 
					name1 = getShortName(entity1.toSelectiveInformation());	
				if (entity2 != null)
					name2 = getShortName(entity2.toSelectiveInformation());						
				return name1.compareTo(name2);
			}								
		}
		, sortDir));	
		this.sortDirAscii = sortDir;
		this.store.setEnableFilters(true);
	}
	private void doSortByDependency(SortDir sortDir) {
		this.store.setEnableFilters(false);
		this.store.clearSortInfo();
		this.store.addSortInfo(new StoreSortInfo<MetaDataEditorOverviewModel>(props.declaredModel(), new Comparator<MetaDataEditorOverviewModel>() {
			@Override
			public int compare(MetaDataEditorOverviewModel arg1, MetaDataEditorOverviewModel arg2) {
				if (arg1 == null) 
					return -1;
				 else if(arg2 == null) 
					return 1;
				
				GenericEntity entity1 = (arg1.getGmDependencyEntityType() == null) ? arg1.getGmDependencyModel() : arg1.getGmDependencyEntityType(); 
				GenericEntity entity2 = (arg2.getGmDependencyEntityType() == null) ? arg2.getGmDependencyModel() : arg2.getGmDependencyEntityType();
				if (entity1 == null) 
					return -1;
				else if(entity2 == null)
					return 1;
				
				Object obj = MetaDataEditorOverviewView.this.lastModelPathElement.getValue();
				
				String name1 = entity1.toSelectiveInformation();
				String name2 = entity2.toSelectiveInformation();	
				
				//sort by dependency owner - 1st if is same dependency as owner, than depnding by owner name
				int compareDependency = name1.compareTo(name2); 
				if (compareDependency != 0)
					if (entity1.equals(obj))
						return -1;
					else if (entity2.equals(obj))
						return 1;
					else			
						return compareDependency;
				
				//if same dependency owner than sort by entity name
				name1 = arg1.getValue().toSelectiveInformation();																
				name2 = arg2.getValue().toSelectiveInformation();						
				return name1.compareTo(name2);					
			}								
		}
		, sortDir));		
		this.sortDirDependency = sortDir;
		this.store.setEnableFilters(true);
	}
	
	public static boolean checkPropertyAbsense(GenericEntity entity, Property property) {
		if (!(entity instanceof EnhancedEntity)) 
			return false;
		
		if (property.getType() instanceof CollectionType) {
			Object collection = property.get(entity);
			if (collection instanceof EnhancedCollection)
				return ((EnhancedCollection) collection).isIncomplete();
		} else if (property.getAbsenceInformation(entity) != null) {
			return true;
		}		
		return false;
	}
	
	//int indexColumn - if add as the last set=  -1
	private void addDynamicColumns(final Map<String, EntityType<? extends MetaData>> mapList, int indexColumn) {
		/*
		class ColumnGroupSetting {
			  String description = null;
			  int startColumn = 0;
			  int columnCount = 0;
			  public ColumnGroupSetting(String description, int startColumn, int columnCount) {
			    this.description = description;
			    this.startColumn = startColumn;
			    this.columnCount = columnCount;
			  }
		}
		*/
		
		if (mapList.size() == 0 || this.isColumnsSet) 
			return; 
		
		this.isColumnsSet = true;
		List<ColumnConfig<MetaDataEditorOverviewModel, ?>> listColumn = new ArrayList<ColumnConfig<MetaDataEditorOverviewModel, ?>>();
		listColumn.addAll(this.grid.getColumnModel().getColumns());
		ModelMdResolver modelMdResolver =  MetaDataEditorOverviewView.this.gmSession.getModelAccessory().getMetaData();
		//List<ColumnGroupSetting> groupColumnList = new ArrayList<ColumnGroupSetting>();
		//static add column header for DisplayInfo
		int columnCount = this.grid.getColumnModel().getColumnCount();
		for (Entry<String, EntityType<? extends MetaData>> entry : mapList.entrySet()) {
			String key = entry.getKey();
			final EntityType<?> entityType1 = entry.getValue();
			
			//List<Property> propList = MetaDataEditorUtil.preparePropertyList(entityType1, EntityTypeMetaData.T, true);
			List<Object> propAndPredicateList = MetaDataEditorUtil.preparePropertyAndPredicateList(entityType1, MetaData.T, true);
			int startColumn = -1;
			@SuppressWarnings("unused")
			int endColumn = -1;
			//RVE - here need entityTypeSignature - is used for DisplayInfo in Column name
			//EntityMdResolver emdContextBuilder = metaDataResolver.getMetaData().entityTypeSignature(entityType1.getTypeSignature()).useCase(this.useCase);
			EntityMdResolver emdContextBuilder = modelMdResolver.entityType(entityType1).useCase(this.useCase);
			//for (final Object object : propAndPredicateList) {
				//set Column Name
				//Get name for properties - from Session resolver!!!
				String columnName = entityType1.getShortName();
				Name entityName = emdContextBuilder.meta(Name.T).exclusive();
				if (entityName != null && entityName.getName() != null) {
					columnName = I18nTools.getLocalized(entityName.getName());
				}			
				
				ImageResource columnIcon = null;
				Icon icon = emdContextBuilder.meta(Icon.T).exclusive();
				if (icon != null && icon.getIcon() != null) {
					Resource resource = GMEIconUtil.getSmallImageFromIcon(icon.getIcon());
					columnIcon = new GmImageResource(resource, gmSession.getModelAccessory().getModelSession().resources().url(resource).asString());
				}
				
				GenericModelType genericModelType = GMF.getTypeReflection().getType(entityType1.getTypeSignature());  
				if (Predicate.T.isAssignableFrom(entityType1))
					genericModelType = GMF.getTypeReflection().getType(Boolean.class);
				
				final Object object = (propAndPredicateList.size() == 1) ? propAndPredicateList.iterator().next() : entityType1;
				
				/*
				if (object instanceof Property) {
					Property property = (Property) object;
					genericModelType = property.getType();
					columnName = property.getName();
					Name propertyName = emdContextBuilder.property(property).meta(Name.T).exclusive();
					if (propertyName != null && propertyName.getName() != null) {
						columnName = I18nTools.getLocalized(propertyName.getName());
					} else {
						Name entityName = emdContextBuilder.meta(Name.T).exclusive();
						if (entityName != null && entityName.getName() != null)
							columnName = I18nTools.getLocalized(entityName.getName());												
					}
				} else if (object instanceof EntityType<?> && Predicate.T.isAssignableFrom((EntityType<?>) object)) {
					Name entityName = emdContextBuilder.meta(Name.T).exclusive();
					//String entityName = ((EntityType<?>) object).getSelectiveInformation(null);
					columnName = entityType1.getShortName();					
					
					if (entityName != null && entityName.getName() != null)
						columnName = I18nTools.getLocalized(entityName.getName());
					genericModelType = GMF.getTypeReflection().getType(Boolean.class);
					
					
				} else 
					continue;
				*/
				
				//if (genericModelType == null)
				//	continue;
				
				final GenericModelType propAndPredicateModelType = genericModelType;
				
				SafeHtml columnHeader;
				if (columnIcon != null) {
					String iconUrl = columnIcon.getSafeUri().asString();
					String iconHtml = "<span style=\"background-image: url('" + iconUrl + "');\" qtip='" + columnName + "'>";
					columnHeader = SafeHtmlUtils.fromTrustedString(iconHtml);
				} else
					columnHeader = SafeHtmlUtils.fromString(columnName);
				
				int columnWidth = DEFAULT_DYNAMIC_COLUMN_WIDTH;
				if (this.mapColumnWidth.containsKey(key))
					columnWidth = this.mapColumnWidth.get(key);				
				
				//create column Dynamic
				final ColumnConfig<MetaDataEditorOverviewModel, MetaDataEditorOverviewModel> ccDynamicMetaData = new ColumnConfig<MetaDataEditorOverviewModel, MetaDataEditorOverviewModel>(props.model(), columnWidth, columnHeader);
				ccDynamicMetaData.setCellPadding(false);
				ccDynamicMetaData.setSortable(false);
				ccDynamicMetaData.setCell(new AbstractCell<MetaDataEditorOverviewModel>() {
					@Override
					public void render(Cell.Context context, MetaDataEditorOverviewModel value, SafeHtmlBuilder sb) {
						MetaDataEditorOverviewModel overviewModel = MetaDataEditorOverviewView.this.store.findModelWithKey(context.getKey().toString());
						if (!MetaDataEditorOverviewView.this.store.getRootItems().contains(overviewModel) || value == null) 
							return;
						
						Object objectPropertyValue = null;
						Boolean useGray = (overviewModel.getGmDependencyLevel() >= 1);
						Boolean valueSet = false;
						EntityType<? extends MetaData> entityType = entry.getValue();
						GenericEntity entity = value.getValue();

						if ((Predicate.T.isAssignableFrom(entityType)) && (entity != null)) {
							//get only Predicate, not PredicateErasure Type
							EntityType<?> predicateType = MetaDataEditorUtil.getPredicateEntityType(entityType);
							
							if (entity instanceof GmEntityType) {
								objectPropertyValue = metaDataResolverProvider.getModelMetaDataContextBuilder().entityType((GmEntityType) entity).is((EntityType<? extends Predicate>) predicateType);
								valueSet = true;
							} else if (entity instanceof GmEnumType) {
								objectPropertyValue = metaDataResolverProvider.getModelMetaDataContextBuilder().enumType((GmEnumType) entity).is((EntityType<? extends Predicate>) predicateType);
								valueSet = true;
							} else if (entity instanceof GmProperty && ((GmProperty) entity).getName() != null) {
								if (editingEntity instanceof GmEntityType) {
									objectPropertyValue = metaDataResolverProvider.getModelMetaDataContextBuilder().entityType((GmEntityType) editingEntity).property((GmProperty) entity).is((EntityType<? extends Predicate>) predicateType);
								} else {
									if (((GmProperty) entity).getDeclaringType() != null)
										objectPropertyValue = metaDataResolverProvider.getModelMetaDataContextBuilder().entityType(((GmProperty) entity).getDeclaringType()).property((GmProperty) entity).is((EntityType<? extends Predicate>) predicateType);
									else
										objectPropertyValue = metaDataResolverProvider.getModelMetaDataContextBuilder().property((GmProperty) entity).is((EntityType<? extends Predicate>) predicateType);									
								}
								valueSet = true;
							} else if (entity instanceof GmEnumConstant && ((GmEnumConstant) entity).getName() != null) {
								if (editingEntity instanceof GmEnumType) {
									objectPropertyValue = metaDataResolverProvider.getModelMetaDataContextBuilder().enumType((GmEnumType) editingEntity).constant((GmEnumConstant) entity).is((EntityType<? extends Predicate>) predicateType);
								} else {
									if (((GmEnumConstant) entity).getDeclaringType() != null)
										objectPropertyValue = metaDataResolverProvider.getModelMetaDataContextBuilder().enumType(((GmEnumConstant) entity).getDeclaringType()).constant((GmEnumConstant) entity).is((EntityType<? extends Predicate>) predicateType);
									else
										objectPropertyValue = metaDataResolverProvider.getModelMetaDataContextBuilder().enumConstant((GmEnumConstant) entity).is((EntityType<? extends Predicate>) predicateType);									
								}
								valueSet = true;
							}
							//objectPropertyValue = metaDataResolverProvider.getModelMetaDataContextBuilder().entity(entity).is((EntityType<? extends Predicate>) entityType);
						} else {
							
							if (entity != null) {
								MetaData valueMetaData = null;
								if (entity instanceof GmEntityType) {
									valueMetaData = metaDataResolverProvider.getModelMetaDataContextBuilder().entityType((GmEntityType) entity).meta(entityType).exclusive();
									valueSet = true;								
								} else if (entity instanceof GmEnumType) {
									valueMetaData = metaDataResolverProvider.getModelMetaDataContextBuilder().enumType((GmEnumType) entity).meta(entityType).exclusive();									
									valueSet = true;								
								} if (entity instanceof GmProperty && ((GmProperty) entity).getName() != null) {
									if (editingEntity instanceof GmEntityType) {
										valueMetaData = metaDataResolverProvider.getModelMetaDataContextBuilder().entityType((GmEntityType) editingEntity).property((GmProperty) entity).meta(entityType).exclusive();
									} else {
										if (((GmProperty) entity).getDeclaringType() != null)
											valueMetaData = metaDataResolverProvider.getModelMetaDataContextBuilder().entityType(((GmProperty) entity).getDeclaringType()).property((GmProperty) entity).meta(entityType).exclusive();
										else
											valueMetaData = metaDataResolverProvider.getModelMetaDataContextBuilder().property((GmProperty) entity).meta(entityType).exclusive();									
									}
									valueSet = true;																	
								} else if (entity instanceof GmEnumConstant && ((GmEnumConstant) entity).getName() != null) {
									if (editingEntity instanceof GmEnumType) {
										valueMetaData = metaDataResolverProvider.getModelMetaDataContextBuilder().enumType((GmEnumType) editingEntity).constant((GmEnumConstant) entity).meta(entityType).exclusive();
									} else {
										if (((GmEnumConstant) entity).getDeclaringType() != null)
											valueMetaData = metaDataResolverProvider.getModelMetaDataContextBuilder().enumType(((GmEnumConstant) entity).getDeclaringType()).constant((GmEnumConstant) entity).meta(entityType).exclusive();
										else
											valueMetaData = metaDataResolverProvider.getModelMetaDataContextBuilder().enumConstant((GmEnumConstant) entity).meta(entityType).exclusive();									
									}	
									valueSet = true;
								}
								if (valueMetaData != null) {
									if (!value.getMetaData().contains(valueMetaData)) {
										value.addMetaData(valueMetaData);
										addEntityListener(valueMetaData);
									}
									if (object instanceof Property) {
										objectPropertyValue = ((Property) object).get(valueMetaData);
									} else if (object instanceof EntityType<?>) {
										objectPropertyValue = SelectiveInformationResolver.resolve(entityType, valueMetaData,
												metaDataResolverProvider.getModelMetaDataContextBuilder(), useCase);
									}
								} 
							}							
						}
						
						if (valueSet) {
							StringBuilder html = new StringBuilder();
							String textValue = null;									
							if (objectPropertyValue instanceof LocalizedString) {
								LocalizedString localizedText = (LocalizedString) objectPropertyValue;
								textValue = I18nTools.getLocalized(localizedText);
							} else if (objectPropertyValue instanceof String) {
								textValue = (String) objectPropertyValue;
							} else {
								textValue = MetaDataEditorUtil.prepareStringValue(objectPropertyValue, propAndPredicateModelType, codecRegistry,
										readOnly, useGray, useCase);
							}
							if (useGray)
								html.append(MetaDataEditorUtil.appendStringCell(MetaDataEditorUtil.appendStringGray(textValue)));
							else
								html.append(MetaDataEditorUtil.appendStringCell(textValue));
			   			    sb.appendHtmlConstant(html.toString());													
						} else {							
							//if boolean
							if (propAndPredicateModelType.getJavaType() == Boolean.class) {
								StringBuilder html = new StringBuilder();
								html.append(MetaDataEditorUtil.prepareBooleanValue(null, readOnly, useGray));
								sb.appendHtmlConstant(MetaDataEditorUtil.appendStringCell(html.toString()));
							} else {
								StringBuilder html = new StringBuilder();
								html.append("<div class='MetaDataEmptyValue' style='color:grey'>");
								//String empty = SafeHtmlUtils.htmlEscape("<"+ LocalizedText.INSTANCE.notSet() +">");
								String empty = "";
								html.append(empty);
								html.append("</div>");
								sb.appendHtmlConstant(MetaDataEditorUtil.appendStringCell(html.toString()));
							}
						}
					}
				});
				//more columns can have same class as key, so use two maps to find it
				this.mapColumn.put(key+columnCount, ccDynamicMetaData);
				this.mapColumnsEntityType.put(key+columnCount, (EntityType<? extends MetaData>) entityType1);
				this.mapStringColumn.put(key+columnCount, key);
				if (indexColumn < 0 || indexColumn > (this.grid.getColumnModel().getColumnCount()-1))				
					listColumn.add(ccDynamicMetaData);   //add as the last column
				else
					listColumn.add(indexColumn, ccDynamicMetaData);   //add to direct position
				if (startColumn == -1) {
					startColumn = columnCount;
					endColumn = columnCount;
				} else {
					endColumn = columnCount;
				}
				columnCount++;
			//}
			/*	
			if (startColumn < endColumn) {
				//RVE - need getTypeSignature in Resolver - used for DisplayInfo in Column Group Name
				//String columnGroupName = entityType1.getTypeSignature();
				String columnGroupName = entityType1.getShortName();
				//EntityMdResolver entityContextBuilder = metaDataResolver.getMetaData().entityTypeSignature(columnGroupName).useCase(this.useCase);
				EntityMdResolver entityContextBuilder = modelMdResolver.entityType(entityType1).useCase(this.useCase);
				Name entityName = entityContextBuilder.meta(Name.T).exclusive();
				if (entityName != null && entityName.getName() != null) {
					columnGroupName = I18nTools.getLocalized(entityName.getName());
				}
				ColumnGroupSetting columnGroupSetting = new ColumnGroupSetting(columnGroupName, startColumn, endColumn-startColumn+1);
				groupColumnList.add(columnGroupSetting);
			}
			*/
			
		}
		//ColumnModel<MetaDataEditorOverviewModel> cm = new ColumnModel<MetaDataEditorOverviewModel>(Arrays.<ColumnConfig<MetaDataEditorOverviewModel, ?>> asList(ccDynamicMetaData));
		ColumnModel<MetaDataEditorOverviewModel> cm = new ColumnModel<MetaDataEditorOverviewModel>(listColumn);
		//String columnGroupName = "Display Information";
		//EntityMetaDataContextBuilder entityContextBuilder = metaDataResolver.getMetaData().entityTypeSignature(DisplayInfo.T.getTypeSignature()).useCase(this.useCase);
		//DisplayInfo entityDisplayInfo = entityContextBuilder.meta(DisplayInfo.T).exclusive();
		/*if (entityDisplayInfo != null) {
			columnGroupName = I18nTools.getLocalized(entityDisplayInfo.getName());
		}*/
		//ColumnGroupSetting columnGroupSetting = new ColumnGroupSetting(columnGroupName, 1, 2);
		//groupColumnList.add(columnGroupSetting);
			
		/*
		for (ColumnGroupSetting columnGroupSet : groupColumnList) {
			if (columnGroupSet != null)
				cm.addHeaderGroup(0, columnGroupSet.startColumn, new HeaderGroupConfig(columnGroupSet.description, 1, columnGroupSet.columnCount));  
		}
		*/
		
	    //cm.addHeaderGroup(0, 1, new HeaderGroupConfig(columnGroupName, 1, 2));
		
		this.grid.reconfigure(this.grid.getTreeStore(), cm, this.ccItemName);
	}

	private void addEnumConstantsAsModelsToGrid(Collection<? extends GenericEntity> mdList, GmEnumType gmEnumType, ModelMdResolver modelMdResolver) {
		int id = 0;
		final ModelMdResolver completeModelMdResolver = modelMdResolver;	
		for (final GenericEntity entity : mdList) {
			addEntityListener(entity);

			MetaDataEditorOverviewModel rootModel = null;
			GmEnumConstant enumConstant = null;
			
			if (entity instanceof GmEnumConstant) {
				enumConstant = (GmEnumConstant) entity;
				rootModel = new MetaDataEditorOverviewModel(id++, enumConstant);
			} else if (entity instanceof GmEnumConstantOverride) {
				enumConstant = ((GmEnumConstantOverride) entity).getEnumConstant();
				rootModel = new MetaDataEditorOverviewModel(id++, (GmEnumConstantOverride) entity);				
			}
			
			if ((rootModel == null) || (enumConstant == null)) 
				continue;
			
			rootModel.setIsVisible(true);
			if (enumConstant.getDeclaringType() == null || enumConstant.getDeclaringType().equals(gmEnumType))
				rootModel.setGmDependencyLevel(0);
			else
				rootModel.setGmDependencyLevel(1);
			
			if (enumConstant.getDeclaringType() != null && enumConstant.getDeclaringType().getDeclaringModel() != null) 
				rootModel.setGmDependencyModel(enumConstant.getDeclaringType().getDeclaringModel());
			else 
				rootModel.setGmDependencyModel(null);				 
			rootModel.setGmDependencyEnumType(enumConstant.getDeclaringType());
			
			getEnumConstantMetaDataFromResolver(completeModelMdResolver, enumConstant, gmEnumType, rootModel);
						
			MetaDataEditorOverviewView.this.store.add(rootModel);
			isEmptyStore = false;
			//MetaDataEditorOverviewView.this.store.update(rootModel);
			
 		}
		
		prepareSortAndFilter();
		this.grid.getView().refresh(true);
	}		
	
	private void addPropertyAsModelsToGrid(Collection<? extends GenericEntity> mdList, GmEntityType gmEntityType, ModelMdResolver modelMdResolver, Boolean canDelay) {
		if (!getActive() && canDelay) {		
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {				
				@Override
				public void execute() {
					addPropertyAsModelsToGrid(mdList, gmEntityType, modelMdResolver, false);
				}
			});									
			return;
		}

		int id = 0;
		int iCount = 0;
		List<GenericEntity> localList = new ArrayList<>(mdList);
		final ModelMdResolver completeModelMdResolver = modelMdResolver;	
		for (final GenericEntity entity : mdList) {
			addEntityListener(entity);
			MetaDataEditorOverviewModel rootModel = null;
			GmProperty property = null;
			
			localList.remove(entity);
			
			if (entity instanceof GmProperty) {
				property = (GmProperty) entity;
				rootModel = new MetaDataEditorOverviewModel(id++, property);
			} else if (entity instanceof GmPropertyOverride) {
				property = ((GmPropertyOverride) entity).getProperty();
				rootModel = new MetaDataEditorOverviewModel(id++, (GmPropertyOverride) entity);				
			}
			
			if ((rootModel == null) || (property == null)) 
				continue;
			
			iCount++;
			
			rootModel.setIsVisible(true);
			if (property.getDeclaringType() == null || property.getDeclaringType().equals(gmEntityType))
				rootModel.setGmDependencyLevel(0);
			else
				rootModel.setGmDependencyLevel(1);
			
			if (property.getDeclaringType() != null && property.getDeclaringType().getDeclaringModel() != null) 
				rootModel.setGmDependencyModel(property.getDeclaringType().getDeclaringModel());
			else 
				rootModel.setGmDependencyModel(null);				 
			rootModel.setGmDependencyEntityType(property.getDeclaringType());
			
			getPropertyMetaDataFromResolver(completeModelMdResolver, property, gmEntityType, rootModel);
						
			MetaDataEditorOverviewView.this.store.add(rootModel);
			isEmptyStore = false;
			//MetaDataEditorOverviewView.this.store.update(rootModel);
			
			if ((iCount >= DEFAULT_PAGE_ITEMS_COUNT) && (mdList.size() > (2*DEFAULT_PAGE_ITEMS_COUNT)))
			{
				//paging
				Scheduler.get().scheduleDeferred(() -> addPropertyAsModelsToGrid(localList, gmEntityType, modelMdResolver, false));
				return;
			}			
 		}	
		
		updateFilteredModels();
		prepareSortAndFilter();
		this.grid.getView().refresh(true);
	}	
	
	
	
	private void getPropertyMetaDataFromResolver(ModelMdResolver modelMdResolver, final GmProperty property, GmEntityType gmEntityType, MetaDataEditorOverviewModel rootModel) {
		if (property == null || property.getName() == null)
			return;
		
		PropertyMdResolver propertyMdResolver = modelMdResolver.entityType(gmEntityType).property(property);
		
		for (Map.Entry<String, EntityType<? extends MetaData>> entry : this.mapMetaDataForProperty.entrySet()) {
			EntityType<? extends MetaData> entityType1 = entry.getValue();
			rootModel.addEntityTypeMetaData(entityType1);

			
			MetaData metaDataPrompting = propertyMdResolver.meta(entityType1).exclusive();
			if (metaDataPrompting != null)
				rootModel.addMetaData(metaDataPrompting);			
		}
	}

	private void getEnumConstantMetaDataFromResolver(ModelMdResolver modelMdResolver, final GmEnumConstant enumConstant, GmEnumType gmEnumType, MetaDataEditorOverviewModel rootModel) {
		if (enumConstant == null || enumConstant.getName() == null)
			return;
		
		ConstantMdResolver constantMdResolver = modelMdResolver.enumType(gmEnumType).constant(enumConstant);
		
		for (Map.Entry<String, EntityType<? extends MetaData>> entry : this.mapMetaDataForEnumConstant.entrySet()) {
			EntityType<? extends MetaData> entityType1 = entry.getValue();
			rootModel.addEntityTypeMetaData(entityType1);
			
			
			MetaData metaDataPrompting = constantMdResolver.meta(entityType1).exclusive();
			if (metaDataPrompting != null)
				rootModel.addMetaData(metaDataPrompting);
		}
	}	
	
	private void addEntityTypeAsModelsToGrid(final Collection<GenericEntity> mdList, ModelMdResolver modelMdResolver, Boolean canTimeDelay, Boolean canSort) {
		if (!getActive() && canTimeDelay) {			
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {				
				@Override
				public void execute() {
					addEntityTypeAsModelsToGrid(mdList, modelMdResolver, false, true);
				}
			});	
			return;
		}
		
		int id = MetaDataEditorOverviewView.this.store.getAllItemsCount();
		int iCount = 0;
		List<GenericEntity> localList = new ArrayList<>(mdList);
		final ModelMdResolver completeModelMdResolver = modelMdResolver;	    
		for (final GenericEntity entity : mdList) {					
			addEntityListener(entity);
			MetaDataEditorOverviewModel rootModel = null;
			GmEntityType entityType = null;
			
			localList.remove(entity);
			
			if (entity instanceof GmEntityType) {
				entityType = (GmEntityType) entity;
				rootModel = new MetaDataEditorOverviewModel(id++, entityType);
			} else if (entity instanceof GmEntityTypeOverride) {
				entityType = ((GmEntityTypeOverride) entity).getEntityType();
				rootModel = new MetaDataEditorOverviewModel(id++, (GmEntityTypeOverride) entity);		
			}
						
			if (rootModel == null || entityType == null) 
				continue;
			
			iCount++;
			
			rootModel.setIsVisible(true);
			rootModel.setGmDependencyLevel(1);
 			if (entityType.getDeclaringModel() != null) {
				rootModel.setGmDependencyModel(entityType.getDeclaringModel());
				if (entityType.getDeclaringModel().equals(MetaDataEditorOverviewView.this.lastModelPathElement.getValue()))
					rootModel.setGmDependencyLevel(0);
 			} else {
 				rootModel.setGmDependencyLevel(0);
 			}
			
 			getMetaDataFromResolver(completeModelMdResolver, entityType, rootModel);
								
			MetaDataEditorOverviewView.this.store.add(rootModel);
			isEmptyStore = false;
			//MetaDataEditorOverviewView.this.store.update(rootModel);
			
			if ((iCount >= DEFAULT_PAGE_ITEMS_COUNT) && (mdList.size() > (2*DEFAULT_PAGE_ITEMS_COUNT)))
			{
				//paging
				
				new Timer() {
					@Override
					public void run() {
						addEntityTypeAsModelsToGrid(localList, modelMdResolver, false, true);
					}
				}.schedule(100);			
				
				//Scheduler.get().scheduleDeferred(() -> addEntityTypeAsModelsToGrid(localList, modelMdResolver));
				return;
			}
		}
		
		updateFilteredModels();
		if (canSort)
			prepareSortAndFilter();
		this.grid.getView().refresh(true);
	}

	private void getMetaDataFromResolver(ModelMdResolver modelMdResolver, final GmCustomTypeInfo gmCustomTypeInfo,	MetaDataEditorOverviewModel rootModel) {
		if (gmCustomTypeInfo == null)
			return;
		
		EntityMdResolver entitMdResolver = null;
		EnumMdResolver enumContextBuilder = null;
		//RVE must be used entityTypeSignature instead of entity
		if (gmCustomTypeInfo instanceof GmEntityType) {
			entitMdResolver = modelMdResolver.entityTypeSignature(((GmEntityType) gmCustomTypeInfo).getTypeSignature());
		} else if (gmCustomTypeInfo instanceof GmEnumType){
			enumContextBuilder = modelMdResolver.enumTypeSignature(((GmEnumType) gmCustomTypeInfo).getTypeSignature());			
		}
		
		rootModel.clearEntityTypeMetaData();
		rootModel.clearMetaData();
		
		for (Map.Entry<String, EntityType<? extends MetaData>> entry : this.mapMetaDataForEntityType.entrySet()) {
			EntityType<? extends MetaData> entityType1 = entry.getValue();
			rootModel.addEntityTypeMetaData(entityType1);
			
			MetaData metaDataPrompting = null;
			if (entitMdResolver != null)
				metaDataPrompting =	entitMdResolver.meta(entityType1).exclusive();
			else if (enumContextBuilder != null)
				metaDataPrompting = enumContextBuilder.meta(entityType1).exclusive();
			
			if (metaDataPrompting != null) {
				rootModel.addMetaData(metaDataPrompting);
				addEntityListener(metaDataPrompting);
			}
		}							
	}	

	private void addEnumTypeAsModelsToGrid(Collection<? extends GenericEntity> mdList, ModelMdResolver modelMdResolver, Boolean canTimeDelay, Boolean canSort) {		
		if (!getActive() && canTimeDelay) {		
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {				
				@Override
				public void execute() {
					addEnumTypeAsModelsToGrid(mdList, modelMdResolver, false, true);
				}
			});	
			return;
		}
		
		int id = MetaDataEditorOverviewView.this.store.getAllItemsCount();
		int iCount = 0;
		List<GenericEntity> localList = new ArrayList<>(mdList);
		
		final ModelMdResolver completeModelMdResolver = modelMdResolver;
	    
		for (final GenericEntity entity : mdList) {
			GmEnumType enumType = null;
			addEntityListener(entity);
			MetaDataEditorOverviewModel rootModel = null;
			
			localList.remove(entity);
			
			if (entity instanceof GmEnumTypeOverride) {
				rootModel = new MetaDataEditorOverviewModel(id++, (GmEnumTypeOverride) entity);
				enumType = ((GmEnumTypeOverride) entity).getEnumType();
			} else if (entity instanceof GmEnumType) {
				rootModel = new MetaDataEditorOverviewModel(id++, (GmEnumType) entity);
				enumType = (GmEnumType) entity;
			}
			
			if ((rootModel == null) || (enumType == null)) 
				continue;
			
			iCount++;			
			
			rootModel.setIsVisible(true);
			if (enumType.getDeclaringModel() == null || enumType.getDeclaringModel().equals(MetaDataEditorOverviewView.this.lastModelPathElement.getValue()))
				rootModel.setGmDependencyLevel(0);
			else
				rootModel.setGmDependencyLevel(1);
			rootModel.setGmDependencyModel(enumType.getDeclaringModel());
			
			getMetaDataFromResolver(completeModelMdResolver, enumType, rootModel);
			
			MetaDataEditorOverviewView.this.store.add(rootModel);
			isEmptyStore = false;
			//MetaDataEditorOverviewView.this.store.update(rootModel);
			
			if ((iCount >= DEFAULT_PAGE_ITEMS_COUNT) && (mdList.size() > (2*DEFAULT_PAGE_ITEMS_COUNT)))
			{
				//paging
				new Timer() {
					@Override
					public void run() {
						addEnumTypeAsModelsToGrid(localList, modelMdResolver, false, true);
					}
				}.schedule(100);			
				
				//Scheduler.get().scheduleDeferred(() -> addEnumTypeAsModelsToGrid(localList, modelMdResolver));
				return;
			}			
		}
		
		updateFilteredModels();
		if (canSort) 
			prepareSortAndFilter();
		this.grid.getView().refresh(true);
	}
			
	/*
	private void addModelsMetaDataToGrid(Collection<PropertyMetaData> mdList) {
		int id = 0;
		for (final PropertyMetaData md : mdList) {
			List<Property> propList = preparePropertyList(md.getClass(), false);
			addEntityListener(md);
			//check if Values for MetaData are loaded, if not then load all absent property value
			final List<Property> absentProperties = new ArrayList<Property>();
			for (Property prop : propList) {
					if (GMEUtil.checkPropertyAbsense(md, prop))
					{
						absentProperties.add(prop);
					}
			}		
			
			if (!absentProperties.isEmpty()) {
				EntityType<GenericEntity> entityType = GMF.getTypeReflection().getEntityType(md.getClass());
				loadAbsentProperties(md, entityType ,absentProperties).get(new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						entitiesLoading.remove(md);
						if (entitiesLoading.isEmpty())
							 updateFilteredModels();
							 
						ErrorDialog.show("Error load MetaData Editor Properties", caught);
						caught.printStackTrace();
					}

					@Override
					public void onSuccess(Void result) {
						entitiesLoading.remove(md);
						MetaDataEditorOverviewView.this.store.setEnableFilters(false);
						for (MetaDataEditorOverviewModel model : MetaDataEditorOverviewView.this.store.getAll()) {
							if (model.getValue() == md) {
								MetaDataEditorOverviewView.this.store.update(model);
							}
						}
						MetaDataEditorOverviewView.this.store.setEnableFilters(true);
						if (entitiesLoading.isEmpty())
							 updateFilteredModels();
					}
				});
			}
			
			//fill GridStore with MetaData
			propList = preparePropertyList(md.getClass(), true);
			
			MetaDataEditorOverviewModel rootModel;
			
            if (!propList.isEmpty()) {
            	for (Property prop : propList) {
					rootModel = new MetaDataEditorOverviewModel(id++, md, prop);
					rootModel.setIsVisible(true);
					
					//resolver Visibility, deletable, emphasized, simplified
					EntityMetaDataContextBuilder entityContextBuilder;
					entityContextBuilder = MetaDataEditorOverviewView.this.gmSession.getModelAccessory().getCascadingMetaDataResolver().getMetaData().entity(md).useCase(MetaDataEditorOverviewView.this.useCase);
					PropertySimplification propertySimplification = entityContextBuilder.property(prop).meta(PropertySimplification.T).exclusive();
					boolean simplified = (propertySimplification != null) ? propertySimplification.getSimplify() : false;
					
					PropertyVisibility propertyVisibility = entityContextBuilder.property(prop).meta(PropertyVisibility.T).exclusive();
					boolean visible = (propertyVisibility != null) ? propertyVisibility.getVisible() : false;
					
					PropertyEmphasis propertyEmphasis = entityContextBuilder.property(prop).meta(PropertyEmphasis.T).exclusive();
					boolean emphasized = (propertyEmphasis != null) ? propertyEmphasis.getEmphasized() : false;
					
					PropertyEditable propertyEditable = entityContextBuilder.property(prop).meta(PropertyEditable.T).exclusive();
					boolean editable = (propertyEditable != null) ? propertyEditable.getEditable() : false;
					
					rootModel.setVisibility(visible);
					rootModel.setEditable(editable);
					rootModel.setEmphasized(emphasized);
					rootModel.setSimplified(simplified);
					MetaDataEditorOverviewView.this.store.add(rootModel);
 					isEmptyStore = false;
           	}
            }
		}
		updateFilteredModels();
	}	
	*/
	
	private Future<Void> loadAbsentProperties(GenericEntity entity, EntityType<GenericEntity> entityType, List<Property> absentProperties) {
		Future<Void> future = new Future<>();
		
		MultiLoader multiLoader = new MultiLoader();
		multiLoader.setParallel(false);
		int i = 0;
		for (Property property : absentProperties) {
			multiLoader.add(Integer.toString(i++),
					GMEUtil.loadAbsentProperty(entity, entityType, property, gmSession, useCase, codecRegistry, specialEntityTraversingCriterion));
		}
		
		this.entitiesLoading.add(entity);
		
		multiLoader.load(new AsyncCallback<MultiLoaderResult>() {
			@Override
			public void onFailure(Throwable caught) {
				future.onFailure(caught);
			}

			@Override
			public void onSuccess(MultiLoaderResult result) {
				future.onSuccess(null);
			}
		});
		return future;
	}
		
	private void addEntityListener(GenericEntity entity) {
		if (entity == null) 
			return;
		
		this.gmSession.listeners().entity(entity).remove(this);
    	this.gmSession.listeners().entity(entity).add(this);
	}
	
	private void updateFilteredModels() {
		 if (isEmptyStore) {
	    	 //show empty info panel
	    	 exchangeWidget(getEmptyPanel());
	     } else {
	    	 //show list
	    	 exchangeWidget(gridWidget);
	     }
	     
		 //this.store.addFilter(this.storeVisibleFilter);
		 //this.store.setEnableFilters(true);
	}
	
	/*
	//load missing MetaData property values
	private Loader<Void> loadAbsentProperty(final GenericEntity entity, final EntityType<GenericEntity> entityType, final Property property, final boolean isRestricted) {
		return new Loader<Void>() {
			@Override
			public void load(final AsyncCallback<Void> asyncCallback) {
					EntityReference entityReference = entityType.getReference(entity);
					if (entityReference instanceof PersistentEntityReference) {
						//final PropertyQuery propertyQuery = GMEUtil.getPropertyQuery((PersistentEntityReference) entityType.getReference(entity), property.getPropertyName(),
						//		isRestricted ? maxCollectionSize + 1 : null, getSpecialTraversingCriterion(property.getPropertyType().getJavaType()), isRestricted,
						//				MetaDataEditorOverviewView.this.gmSession.getModelAccessory().getCascadingMetaDataResolver(), MetaDataEditorOverviewView.this.useCase);
						final PropertyQuery propertyQuery = GMEUtil.getPropertyQuery((PersistentEntityReference) entityReference, property.getPropertyName(),
								null, getSpecialTraversingCriterion(property.getPropertyType().getJavaType()), false,
										MetaDataEditorOverviewView.this.gmSession.getModelAccessory().getCascadingMetaDataResolver(), MetaDataEditorOverviewView.this.useCase);
						
						final ProfilingHandle ph = Profiling.start(getClass(), "Querying property '" + property.getPropertyName() + "' in PP", true);
						MetaDataEditorOverviewView.this.gmSession.query().property(propertyQuery).result(new com.braintribe.processing.async.api.AsyncCallback<PropertyQueryResultConvenience>() {
							@Override
							public void onSuccess(PropertyQueryResultConvenience propertyQueryResult) {
								ph.stop();
								ProfilingHandle ph1 = Profiling.start(getClass(), "Handling property query in MDE", false);
								GmSessionException exception = null;
								try {
									PropertyQueryResult result = propertyQueryResult.result();
									MetaDataEditorOverviewView.this.gmSession.suspendHistory();
									Object value = result != null ? result.getPropertyValue() : null;
									value = GMEUtil.transformIfSet(value, property.getPropertyName(), entityType);
									
									if (value instanceof EnhancedCollection) {
										((EnhancedCollection) value).setIncomplete(result.getHasMore());
									}
									
									IndexedProvider<Class<?>, Codec<Object, String>> codecsProvider = new MapIndexedProvider<Class<?>, Codec<Object,String>>(MetaDataEditorOverviewView.this.valueRenderers);
									ProfilingHandle ph2 = Profiling.start(getClass(), "Setting new property value in the entity in MDE", false);
									entityType.setPropertyValue(entity, property.getPropertyName(), GMEUtil.sortIfSet(value, propertyQuery,
											MetaDataEditorOverviewView.this.gmSession, MetaDataEditorOverviewView.this.useCase, codecsProvider));
									ph2.stop();
								} catch (GmSessionException e) {
									exception = e;
								} finally {
									MetaDataEditorOverviewView.this.gmSession.resumeHistory();
									
									ph1.stop();
									if (exception == null)
										asyncCallback.onSuccess(null);
									else
										onFailure(exception);
								}
							}
							
							@Override
							public void onFailure(Throwable t) {
								ph.stop();
								asyncCallback.onFailure(t);
							}
						});
					} else
						asyncCallback.onSuccess(null);
			}
		};
	}
	*/
	
	private TraversingCriterion getSpecialTraversingCriterion(Class<?> clazz) {
		if (this.specialEntityTraversingCriterion == null) 
			return null;
		
		return this.specialEntityTraversingCriterion.get(clazz);
	}	
			
	@Override
	public boolean isSelected(Object element) {
		List<MetaDataEditorOverviewModel> selectedModels = this.grid.getSelectionModel().getSelectedItems();
		if (selectedModels != null && !selectedModels.isEmpty()) {
			for (MetaDataEditorOverviewModel selectedModel : selectedModels) {
				if (selectedModel.refersTo(element)) {
					return true;
				}
			}
		}
		return false;
	}
		
	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		this.gmSelectionListeners.add(sl);
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		this.gmSelectionListeners.remove(sl);
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		return null;
	}

	@Override
	public void select(int index, boolean keepExisting) {
		//not yet needed
	}

	@Override
	public GmContentView getView() {
		return null;
	}
	
	@Override
	public ModelPath getExtendedSelectedItem() {
		return getSelectedModelPath(true);
	}
	
	@Override
	public ModelPath getFirstSelectedItem() {
		return getSelectedModelPath(false);
	}

	@Override
	public boolean isSelectionActive() {
		return this.grid.getSelectionModel().getSelectedItems().size() > 0;
	}	
	
	@Override
	public Set<ModelPath> getContent() {
		if (this.lastModelPathElement == null) 
			return null;
		
		GenericEntity entity = (GenericEntity) this.lastModelPathElement.getValue();
		if (entity == null || this.overridenCollection == null) 
			return null;

		Set<ModelPath> modelPaths = new HashSet<ModelPath>();
		ModelPath modelPath = new ModelPath();
		ModelPathElement modelPathElement = this.lastModelPathElement.copy();
		modelPath.add(modelPathElement);
		modelPaths.add(modelPath);

		PropertyPathElement pathElement = null;
		
		if (this.lastModelPathElement.getValue() instanceof GmMetaModel) {
			if (this.listAllowTypes.contains(GmEnumType.class)) {
				//pathElement = new PropertyPathElement(entity, entity.entityType().getProperty("enumTypeOverrides"), this.overridenCollection);						
				pathElement = new PropertyPathElement(entity, entity.entityType().getProperty("typeOverrides"), this.overridenCollection);						
			} else  {
				//pathElement = new PropertyPathElement(entity, entity.entityType().getProperty("entityTypeOverrides"), this.overridenCollection); 
				pathElement = new PropertyPathElement(entity, entity.entityType().getProperty("typeOverrides"), this.overridenCollection);
			}
		} else if (this.lastModelPathElement.getValue() instanceof GmEntityType) {
			//TODO RVE - question if use from  GmEntityType "propertyOverrides" o
			pathElement = new PropertyPathElement(entity, entity.entityType().getProperty("propertyOverrides"), this.overridenCollection);					
		}
		
		if (pathElement != null) {
			modelPath = new ModelPath();
			modelPath.add(pathElement);
			modelPaths.add(modelPath);
		}
		//return modelPaths;		
		return null;		
	}	
	
	private ModelPath getSelectedModelPath(Boolean useExtendedModelPath) {
		ModelPath modelPath = null;
		MetaDataEditorOverviewModel model = this.grid.getSelectionModel().getSelectedItem();
		
		
		if (model == null) 
			return modelPath;
					
		//default value
		//Object value = model.getValue();
		EntityType<? extends MetaData> metaDataClassType = null;
		MetaData metaData = null;
		
		//MetaData value from selected Dynamic columns
		ColumnConfig<MetaDataEditorOverviewModel, ?> column = this.grid.getColumnModel().getColumn(this.lastSelectedColumn);
		if (column != ccItemName && column != ccModelName && column != ccOwnerName)	{		
			for (Map.Entry<String, ColumnConfig<MetaDataEditorOverviewModel, ?>> entry : this.mapColumn.entrySet()) {
				if (entry.getValue().equals(column)) {
					//find classType from Map
					String key = entry.getKey();					
					EntityType<? extends MetaData> entityType = this.mapColumnsEntityType.get(key);
										
					//find selected metaData class and metaData
					if (entityType != null) {
						metaDataClassType = entityType;		
						
						if (model.getValue() instanceof GmEntityType) {
							metaData = metaDataResolverProvider.getModelMetaDataContextBuilder().entityType((GmEntityType) model.getValue()).meta(entityType).exclusive();
						} else if (model.getValue() instanceof GmEnumType) {
							metaData = metaDataResolverProvider.getModelMetaDataContextBuilder().enumType((GmEnumType) model.getValue()).meta(entityType).exclusive();																
						} else if (model.getValue() instanceof GmProperty) {
							if (editingEntity instanceof GmEntityType) {
								metaData = metaDataResolverProvider.getModelMetaDataContextBuilder().entityType((GmEntityType) editingEntity).property((GmProperty) model.getValue()).meta(entityType).exclusive();
							} else {
								if (((GmProperty) model.getValue()).getDeclaringType() != null)
									metaData = metaDataResolverProvider.getModelMetaDataContextBuilder().entityType(((GmProperty) model.getValue()).getDeclaringType()).property((GmProperty) model.getValue()).meta(entityType).exclusive();
								else
									metaData = metaDataResolverProvider.getModelMetaDataContextBuilder().property((GmProperty) model.getValue()).meta(entityType).exclusive();									
							}							
						} else if (model.getValue() instanceof GmEnumConstant) {
							if (editingEntity instanceof GmEnumType) {
								metaData = metaDataResolverProvider.getModelMetaDataContextBuilder().enumType((GmEnumType) editingEntity).constant((GmEnumConstant) model.getValue()).meta(entityType).exclusive();
							} else {
								if (((GmEnumConstant) model.getValue()).getDeclaringType() != null)
									metaData = metaDataResolverProvider.getModelMetaDataContextBuilder().enumType(((GmEnumConstant) model.getValue()).getDeclaringType()).constant((GmEnumConstant) model.getValue()).meta(entityType).exclusive();
								else
									metaData = metaDataResolverProvider.getModelMetaDataContextBuilder().enumConstant((GmEnumConstant) model.getValue()).meta(entityType).exclusive();									
							}																
						}
												
					}					
					break;
				}
			}
		}
		
		//send selected Entity or Property with selected metaData
		if (model.getValue() == null) 
			return modelPath;

		modelPath = new ModelPath();
		GenericModelType type = null;
		RootPathElement pathElement = null;
		
		if (useExtendedModelPath) {
			//show in Resolution Tab
			if (metaDataClassType != null) {
				type = metaDataClassType;
				pathElement = new RootPathElement(type, metaDataClassType);
				modelPath.add(pathElement);
			}
			
			type = model.getValue().entityType();
			pathElement = new RootPathElement(type, model.getValue());
			modelPath.add(pathElement);
		} else {
			if (metaDataClassType != null) {
				type = metaDataClassType;
				pathElement = new RootPathElement(type, metaData);
				modelPath.add(pathElement);
				//return null;
			} else {
				//to show selected cell entity in Detail and General Tab
				if (column == ccItemName) {
					type = model.getValue().entityType();
					pathElement = new RootPathElement(type, model.getValue());
					modelPath.add(pathElement);
				} else if (column == ccModelName) {
					if (model.getGmDependencyModel() != null) {
						type = model.getGmDependencyModel().entityType();
						pathElement = new RootPathElement(type, model.getGmDependencyModel());
						modelPath.add(pathElement);
					}
				} else if (column == ccOwnerName) {
					if (model.getGmDependencyEntityType() != null) {
						type = model.getGmDependencyEntityType().entityType();
						pathElement = new RootPathElement(type, model.getGmDependencyEntityType());
						modelPath.add(pathElement);
					} else if (model.getGmDependencyEnumType() != null) {
						type = model.getGmDependencyEnumType().entityType();
						pathElement = new RootPathElement(type, model.getGmDependencyEnumType());
						modelPath.add(pathElement);
					}
					
				}
			}
		} 
		
		return modelPath;
	}

	@Override
	public Boolean isActionManipulationAllowed() {
		return true;
	}
	
	@Override
	public HandlerRegistration addRowClickHandler(RowClickHandler handler) {
		return this.grid.addRowClickHandler(handler);
	}

	// ----- Internal Members -----

	/*
	private List<Property> preparePropertyList(Class<? extends MetaData> entityClass, Class<? extends MetaData> baseTypeClass, Boolean useOnlyClassSpecial) {
		if (this.baseTypes == null) {
			this.baseTypes = new ArrayList<String>();
			for (Property property : GMF.getTypeReflection().getEntityType(baseTypeClass).getProperties())
				this.baseTypes.add(property.getPropertyName());
		}
		EntityType<GenericEntity> entityType = GMF.getTypeReflection().getEntityType(entityClass);
		List<Property> result = new ArrayList<Property>();
		for (Property property : entityType.getProperties())
			if (useOnlyClassSpecial) {
				//add only special property (not inherited from base class EntityTypeMetaData )
				if (this.baseTypes.indexOf(property.getPropertyName()) < 0) {
					result.add(property);
				} else {
					//special for DisplayInfo - get also Name from basic Types
					if (property != null && (property.getPropertyName().equals("name") || property.getPropertyName().equals("description"))) {
						for (EntityType<?> entitySuperType : entityType.getSuperTypes()) {
							if (entitySuperType.getTypeSignature().equals((DisplayInfo.class).getName())) {
								result.add(property);
							}
						}
					}
				}
			} else {
				//add all property
				result.add(property);
			}
		return result;
	}
	*/

	/*
	private List<Property> preparePropertyList(Class<? extends GenericEntity> entityClass) {
		EntityType<GenericEntity> entityType = GMF.getTypeReflection().getEntityType(entityClass);
		List<Property> result = new ArrayList<Property>();
		for (Property property : entityType.getProperties()) {
				//add all property
				result.add(property);
		}
		return result;
	}	
	*/
	
	/*
	private String prepareStringValue(Object propertyValue, GenericModelType valueType) {
	    String stringValue = null;
		
		if (propertyValue != null) {
			if (valueType == null)
				valueType = GMF.getTypeReflection().getType(GMF.getTypeReflection().getTypeSignature(propertyValue));
			
			if (this.valueRenderers != null) {
				Codec<Object, String> renderer = this.valueRenderers.get(valueType.getJavaType());
				if (renderer != null) {
					try {
						stringValue = renderer.encode(propertyValue);
					} catch (CodecException e) {
						//logger.error("Error while getting value renderer value.", e);
						e.printStackTrace();
					}
				}
			}
			
			if (stringValue == null) {
				if (valueType instanceof EntityType || valueType instanceof EnumType) {
					String enumString = propertyValue.toString();
					DisplayInfo displayInfo = GMEUtil.getDisplayInfo(valueType, this.gmSession.getModelAccessory().getCascadingMetaDataResolver(), this.useCase);
					if (displayInfo != null && displayInfo.getName() != null) {
						enumString = I18nTools.getLocalized(displayInfo.getName());
					}
					stringValue = enumString;
				}
			}
			
	     	if (valueType.getJavaType() == Boolean.class) {
	     			stringValue = stringValue != null ? stringValue : (propertyValue != null ? propertyValue.toString() : "");
	     			
					String booleanClass = "";
					
					PropertyPanelCss css = PropertyPanelResources.INSTANCE.css();
					//if (readOnly || !propertyModel.isEditable()) {
					if (readOnly) {
						if (stringValue == null)
							booleanClass = css.checkNullReadOnlyValue();
						else
							booleanClass = (stringValue == "true") ? css.checkedReadOnlyValue() : css.uncheckedReadOnlyValue();
					} else {
						if (stringValue == null)
							booleanClass = css.checkNullValue();
						else
							booleanClass = (stringValue == "true") ? css.checkedValue() : css.uncheckedValue();
					}
					stringValue = "<div class='" + booleanClass + "'/>";
			}
			
		}
		
		return stringValue != null ? stringValue : (propertyValue != null ? propertyValue.toString() : "");
		//return SafeHtmlUtils.htmlEscape(stringValue != null ? stringValue : (propertyValue != null ? propertyValue.toString() : ""));
	}	
	*/
			
	private static String getSelectorDisplayValue (MetaDataSelector selector) {
		if (selector == null) 
			return ""; 

		StringBuilder builder = new StringBuilder();
		if (selector instanceof UseCaseSelector) {
			builder.append(LocalizedText.INSTANCE.displayUseCase()).append("=").append(((UseCaseSelector) selector).getUseCase()) ;	
		} else if (selector instanceof RoleSelector) {
			if (!builder.toString().isEmpty()) {
				builder.append(" && ");
			}
			builder.append(LocalizedText.INSTANCE.displayRole()).append("=") .append(((RoleSelector) selector).getRoles());	
		} else if (selector instanceof AccessSelector) {
			if (!builder.toString().isEmpty()) {
				builder.append(" && ");
			}
			builder.append(LocalizedText.INSTANCE.displayAccess()).append("=").append(((AccessSelector) selector).getExternalId());	
		}
		
		if (builder.toString().isEmpty())
			builder.append(selector.toString());
		
		return builder.toString();
	}
	
	private static String getSelectorValueFromList (List<String> listString, MetaDataSelector selector) {
		
		if (selector == null || listString == null)
			return "";
		
		StringBuilder builder = new StringBuilder();
		int i = 0;
		for (String value : listString) {
			if (i == 0  && selector instanceof UseCaseSelector) {
				if (!value.isEmpty()) {
					if (!builder.toString().isEmpty()) {
						builder.append(" && ");
					}	
					builder.append(value);
				} else {
					builder.append(getSelectorDisplayValue(selector));
				}
			} else if (i == 1 && selector instanceof RoleSelector && !value.isEmpty()) {
				if (!value.isEmpty()) {
					if (!builder.toString().isEmpty()) {
						builder.append(" && ");
					}
					builder.append(value);
				} else {
					builder.append(getSelectorDisplayValue(selector));
				}
			} else if (i == 2 && selector instanceof AccessSelector && !value.isEmpty()) {
				if (!value.isEmpty()) {
					if (!builder.toString().isEmpty()) {
						builder.append(" && ");
					}
					builder.append(value);
				} else {
					builder.append(getSelectorDisplayValue(selector));
				}
			}
			
			i++;
		}
		return builder.toString();
	}
	
	private Collection<GenericEntity> getItemTypes(GmMetaModel metaModel, boolean useDependency, Boolean useEntityType) {
		if (metaModel == null) 
			return null;
		
		Set<GenericEntity> types = new HashSet<>();
		if (useEntityType)
			types.addAll(metaModel.entityTypeSet());
		else
			types.addAll( metaModel.enumTypeSet());
		
		if (useDependency) {
			//RVE - fastest way...first get list of all dependency GmMetaModels recursively, than get items from that GmMetaModels
			Collection<GmMetaModel> gmMetaModelCollection = getGmMetaModelDependencies(metaModel, useDependency);
			
			for (GmMetaModel dependencyModel : gmMetaModelCollection) {				
				if (useEntityType)
					dependencyModel.entityTypes().collect(Collectors.toCollection(() -> types));
				else
					dependencyModel.enumTypes().collect(Collectors.toCollection(() -> types));
			}
		}
		
		List<GenericEntity> list = new ArrayList<>(types);
		
		doSort(list);
		//Collections.sort(collection,PriorityColumnConfig.getPriorityComparator(false));
		
		return list;
	}

	private Collection<GenericEntity> getItemProperties(GmEntityType entityType, boolean useDependency) {
		if (entityType == null) 
			return null;
		
		Set<GmProperty> types = new HashSet<>();
		types.addAll(entityType.getProperties());
		
		if (useDependency) {
			//RVE - fastest way...first get list of all dependency(super types) GmEntityType recursively, than get items from that GmEntityType
			Collection<GmEntityType> gmEntityTypeCollection = getGmEntityTypeSuperTypes(entityType, useDependency);
			
			for (GmEntityType dependencyEntity : gmEntityTypeCollection) {
				if (dependencyEntity.getProperties() != null)
					for (GmProperty property : dependencyEntity.getProperties()) 
						if (!types.contains(property))
							types.add(property);
			}
		}
		
		List<GenericEntity> list = new ArrayList<>(types);
		
		doSort(list);
		//Collections.sort(collection,PriorityColumnConfig.getPriorityComparator(false));
		
		return list;
	}

	private Collection<GenericEntity> getItemEnumConstants(GmEnumType enumType) {
		if (enumType == null) 
			return null;
		
		Set<GmEnumConstant> types = new HashSet<>();
		types.addAll(enumType.getConstants());
				
		List<GenericEntity> list = new ArrayList<>(types);
		
		doSort(list);
		//Collections.sort(collection,PriorityColumnConfig.getPriorityComparator(false));
		
		return list;
	}	
	
    protected void doSort(List<GenericEntity> collection) {
		Collections.sort(collection, new Comparator<GenericEntity>() {
			@Override
			public int compare(GenericEntity e1, GenericEntity e2) {
				if (e1 == null) {
					return -1;
				} else if(e2 == null) {
					return 1;
				} else {				
					String name1 = e1.toSelectiveInformation();
					String name2 = e2.toSelectiveInformation();
					return name1.compareTo(name2);
				}
			}
		});
		
		/*
		if (config.getSortInfo() != null && config.getSortInfo().getSortField() != null) {
			Collections.sort(models, new Comparator<HitListModel>() {
				public int compare(HitListModel m1, HitListModel m2) {
					return storeSorter.compare(null, m1, m2, config.getSortInfo().getSortField());
				}
			});
			if (config.getSortInfo().getSortDir() == SortDir.DESC) {
				Collections.reverse(models);
			}
		} 	
		*/	
    }	
    
    
	private Collection<GmMetaModel> getGmMetaModelDependencies(GmMetaModel metaModel, Boolean useDependency) {
		if (metaModel == null)
			return null;
		
		Collection<GmMetaModel> gmMetaModelCollection = new ArrayList<>();
		gmMetaModelCollection.addAll(metaModel.getDependencies());
		
		if ((gmMetaModelCollection.isEmpty()) || (metaModel.getDependencies().isEmpty())) 
			return gmMetaModelCollection;
		
		for (GmMetaModel model : metaModel.getDependencies()) {
			Collection<GmMetaModel> gmMetaModelDependencyCollection = getGmMetaModelDependencies(model, useDependency);
			if (gmMetaModelDependencyCollection != null)
				for (GmMetaModel dependencyModel : gmMetaModelDependencyCollection) {
					if (!gmMetaModelCollection.contains(dependencyModel))
						gmMetaModelCollection.add(dependencyModel); 
				}
		  }
		
		return gmMetaModelCollection;
	}
	
	private Collection<GmEntityType> getGmEntityTypeSuperTypes(GmEntityType entityType, Boolean useDependency) {
		if (entityType == null)
			return null;
				
		Collection<GmEntityType> gmEntityTypeCollection = new ArrayList<GmEntityType>();
		gmEntityTypeCollection.addAll(entityType.getSuperTypes());
		
		if ((gmEntityTypeCollection.isEmpty()) || (entityType.getSuperTypes().isEmpty())) 
			return gmEntityTypeCollection;
		
		for (GmEntityType entity : entityType.getSuperTypes()) {
			Collection<GmEntityType> gmEntityTypeDependencyCollection = getGmEntityTypeSuperTypes(entity, useDependency);
			if (gmEntityTypeDependencyCollection != null)
				for (GmEntityType dependencyEntity : gmEntityTypeDependencyCollection) {
					if (!gmEntityTypeCollection.contains(dependencyEntity))
						gmEntityTypeCollection.add(dependencyEntity); 
				}
		  }
		
		return gmEntityTypeCollection;
	}
	
	private void addColumn (String columnName, int columnIndex, EntityType<? extends MetaData> entityType, Boolean refresh) {
		if (mapColumnsEntityType.containsValue(entityType)) {
			//not need create column, only set it as Visible
			for (Entry<?, ?> entry : mapColumnsEntityType.entrySet()) {
				if (entry.getValue().equals(entityType)) {
					mapColumn.get(entry.getKey()).setHidden(false);
					if (refresh)
						this.grid.reconfigure(this.grid.getTreeStore(), this.grid.getColumnModel(), this.ccItemName);
					break;	
				}
			}			
			return;
		}		
		
		Map<String, EntityType<? extends MetaData>> mapList = new FastMap<>();
		mapList.put(columnName, entityType);
		this.isColumnsSet = false;
		this.isColumnsChange = true;
		addDynamicColumns(mapList, columnIndex);
	//	if (refresh)
	//		saveVisibleColumns();
	}

	private void addColumns (int columnIndex, List<EntityType<? extends MetaData>> listEntityType, Boolean refresh) {
		Map<String, EntityType<? extends MetaData>> mapList = new FastMap<>();
		for (EntityType<? extends MetaData> entityType : listEntityType) {
			if (mapColumnsEntityType.containsValue(entityType)) {
				//not need create column, only set it as Visible
				for (Entry<?, ?> entry : mapColumnsEntityType.entrySet()) {
					if (entry.getValue().equals(entityType)) {
						mapColumn.get(entry.getKey()).setHidden(false);
						if (refresh)
							this.grid.reconfigure(this.grid.getTreeStore(), this.grid.getColumnModel(), this.ccItemName);
						break;	
					}
				}			
				continue;
			}
			
			mapList.put(entityType.getShortName(), entityType);			
		}
		
		this.isColumnsSet = false;
		this.isColumnsChange = true;
		addDynamicColumns(mapList, columnIndex);
	}
	
	
	private void hideColumn (int columnIndex, Boolean refresh) {		
		ColumnConfig<MetaDataEditorOverviewModel, Object> column = this.grid.getColumnModel().getColumn(columnIndex);
		if (column != null) {
			column.setHidden(true);
			isColumnsChange = true;
		}
		if (refresh) {
			this.grid.reconfigure(this.grid.getTreeStore(), this.grid.getColumnModel(), this.ccItemName);
		//	saveVisibleColumns();
		}	
	}
	
	private void hideColumn (EntityType<? extends MetaData> entityType, Boolean refresh) {
		List<EntityType<? extends MetaData>> listEntityType = new ArrayList<>();
		listEntityType.add(entityType);
		hideColumns(listEntityType, refresh);		
	}
	private void hideColumns (List<EntityType<? extends MetaData>> listEntityType, Boolean refresh) {		
		for (EntityType<? extends MetaData> entityType : listEntityType) {		
			if (mapColumnsEntityType.containsValue(entityType)) {
				//not need create column, only set it as Visible
				for (Entry<?, ?> entry : mapColumnsEntityType.entrySet()) {
					if (entry.getValue().equals(entityType)) {
						hideColumn(this.grid.getColumnModel().getColumns().indexOf(mapColumn.get(entry.getKey())), refresh);
						break;	
					}
				}			
			}	
		}
	}

	private void changeColumn (String columnName, int columnIndex, EntityType<? extends MetaData> entityType) {
		hideColumn(columnIndex, false);
		addColumn(columnName, columnIndex, entityType, true);
	}
	
	private void switchColumn (int columnIndex, EntityType<? extends MetaData> entityType) {
		//ColumnConfig<MetaDataEditorOverviewModel, Object> column = this.grid.getColumnModel().getColumn(columnIndex);
		int oldIndex = -1;
		if (mapColumnsEntityType.containsValue(entityType)) {
			//not need create column, only set it as Visible
			for (Entry<?, ?> entry : mapColumnsEntityType.entrySet()) {
				if (entry.getValue().equals(entityType)) {
						oldIndex =  this.grid.getColumnModel().getColumns().indexOf(mapColumn.get(entry.getKey()));
				}
			}
		}
		if (columnIndex >= 0 &&  oldIndex >= 0) {
			hideColumn(columnIndex, false);
			this.grid.getColumnModel().moveColumn(oldIndex, columnIndex);
		}
	}
	
	private void addColumnGroup (CheckMenuItem menuItem, Boolean hide, Boolean refresh) {
	
		List<EntityType<? extends MetaData>> listEntityType = new ArrayList<>();
		
		for (int i=0; i<menuItem.getSubMenu().getWidgetCount(); i++) {
			if (!(menuItem.getSubMenu().getWidget(i) instanceof MenuItem)) 
				continue;
			
			MenuItem subMenuItem = (MenuItem) menuItem.getSubMenu().getWidget(i);
			Object data = subMenuItem.getData("entity");
			if (data != null) {
				EntityType<? extends MetaData> entity  = (EntityType<? extends MetaData>) data;			
				listEntityType.add(entity);
			}	
			
		}
		
		if (!listEntityType.isEmpty()) {
			if (hide)
				hideColumns(listEntityType, false);
			else
				addColumns(-1, listEntityType, false);
		}
		
		if (refresh) {
			this.grid.reconfigure(this.grid.getTreeStore(), this.grid.getColumnModel(), this.ccItemName);
		//	saveVisibleColumns();
		}	
	}
		
	private void setCheckedSubItems (CheckMenuItem menuItem, Boolean checked) {		
		for (int i=0; i<menuItem.getSubMenu().getWidgetCount(); i++) {
			if (!(menuItem.getSubMenu().getWidget(i) instanceof CheckMenuItem)) 
				continue;
			
			CheckMenuItem subMenuItem = (CheckMenuItem) menuItem.getSubMenu().getWidget(i);
			subMenuItem.setChecked(checked, true);
		}
	}
	
	private Menu createMenuItems(Boolean isAddType, Boolean asChange, int colIndex) {
		Menu subMenu = new Menu();
		subMenu.addHideHandler(new HideHandler() {			
			@Override
			public void onHide(HideEvent event) {
				saveVisibleColumns();
			}
		});
		
		MetaDataTypesExpert expertList = new MetaDataTypesExpert();
		Set<Class<? extends GenericEntity>> baseClass = new HashSet<>();
		//baseClass.add(Predicate.class);
		baseClass.add(UniversalMetaData.class);
		if (lastModelPathElement.getValue() instanceof GmMetaModel) {
			baseClass.add(EntityTypeMetaData.class);
			baseClass.add(EnumTypeMetaData.class);
		} else if (lastModelPathElement.getValue() instanceof GmEntityTypeInfo) {
			baseClass.add(PropertyMetaData.class);					
		} else if (lastModelPathElement.getValue() instanceof GmEnumTypeInfo) {
			baseClass.add(EnumConstantMetaData.class);
		}

		
		expertList.provide(baseClass, new CallbackEntityType() {					
			@Override
			public void onSuccess(Collection<EntityType<?>> future) {
				Set<MenuItem> othersSubMenuList = new HashSet<>();
				Boolean groupExists = false;
				for (EntityType<?> entity : future) {								
				   Boolean isHidden = true;	
				   if (mapColumnsEntityType.containsValue(entity)) 
					   for (Entry<?, ?> entry : mapColumnsEntityType.entrySet()) 
						   if ((entry.getValue().equals(entity)) && (!mapColumn.get(entry.getKey()).isHidden()))
							   isHidden = false; 
												
				   //RVE  - from Predicate show only "main" MetaData, not the PredicateErasure (Not possible to ask for value in PredicateErasures)
				   //if  (!(PredicateErasure.T.isAssignableFrom(entity)) && (!(mapColumnsEntityType.containsValue(entity)) || isHidden)) {
				   if  ((!(PredicateErasure.T.isAssignableFrom(entity)) && !asChange) || (!(PredicateErasure.T.isAssignableFrom(entity)) /*&& (!(mapColumnsEntityType.containsValue(entity)) || isHidden)*/ && asChange)){
					   
					   MenuItem menuItem;
					   if (!asChange) {					   
						   menuItem = new CheckMenuItem(entity.getShortName());
						   menuItem.setHideOnClick(false);
						   ((CheckMenuItem) menuItem).setChecked(!isHidden, true);
						   ((CheckMenuItem) menuItem).addCheckChangeHandler(new CheckChangeHandler<CheckMenuItem>() {
								@Override
								public void onCheckChange(CheckChangeEvent<CheckMenuItem> event) {
									if (isAddType) {
										if (event.getChecked().equals(CheckState.CHECKED))
											addColumn(entity.getShortName(), -1, (EntityType<? extends MetaData>) entity, true);	
										else	
											hideColumn((EntityType<? extends MetaData>) entity, true);									
									}
								}						   
						   });
					   } else {
						   Boolean useChangeColumn = (!(mapColumnsEntityType.containsValue(entity)) || isHidden);
						   menuItem = new MenuItem(entity.getShortName());  
						   menuItem.addSelectionHandler(new SelectionHandler<Item>() {					
								@Override
								public void onSelection(SelectionEvent<Item> event) {
									if (useChangeColumn) {
										//change column
										changeColumn(entity.getShortName(), colIndex, (EntityType<? extends MetaData>) entity);
									} else {
										//switch column
										switchColumn(colIndex, (EntityType<? extends MetaData>) entity);
									}
								}
						   });
					   }
					   menuItem.setData("entity", entity);
					   /*
					   */
					   
					   String groupName = getGroupAssignmentForMetaData(entity);
					   if (groupName == null || groupName.isEmpty()) {
						   othersSubMenuList.add(menuItem);
					   } else {
					   	   //find subMenu with that Name, if not exists create it and add this item there
						   MenuItem menuItem2 = null;
						   Menu subMenu2 = null;
						   for (int i=0; i<subMenu.getWidgetCount(); i++) {
							   if (subMenu.getWidget(i) instanceof MenuItem) {
								   MenuItem subMenuItem = (MenuItem) subMenu.getWidget(i);
								   if (subMenuItem.getText().equals(groupName)) {
									   menuItem2 = subMenuItem;
									   subMenu2 = menuItem2.getSubMenu();
									   break;
								   }
							   }
						   }
						   
						   if (menuItem2 == null || subMenu2 == null) {
							   if (!asChange) {
								   menuItem2 = new CheckMenuItem(groupName);
								   menuItem2.setHideOnClick(false);							   
								   ((CheckMenuItem) menuItem2).addCheckChangeHandler(new CheckChangeHandler<CheckMenuItem>() {
										@Override
										public void onCheckChange(CheckChangeEvent<CheckMenuItem> event) {
											//set checked/unchecked all subitems
											setCheckedSubItems(event.getItem(), event.getChecked().equals(CheckState.CHECKED));										
											//add columns
											addColumnGroup (event.getItem(), !event.getChecked().equals(CheckState.CHECKED), true);	
										}						   
								   });
							   } else {
								   menuItem2 = new MenuItem(groupName);
							   }
							   subMenu2 = new Menu();							  
							   menuItem2.setSubMenu(subMenu2);
							   subMenu.add(menuItem2);
							   groupExists = true;
						   }
						   addMenuItemAlphaSorted(subMenu2, menuItem, true);						   
					   }
					   
				   }
				}
				
				if (!othersSubMenuList.isEmpty()) {
					if (!groupExists) {
						//just put to flat list - no grouping
						for (MenuItem menuItem : othersSubMenuList) {
							   addMenuItemAlphaSorted(subMenu, menuItem, true);
						}
					} else {
						//create group "Others"
						MenuItem menuItem2;
						if (!asChange) {
							menuItem2 = new CheckMenuItem(LocalizedText.INSTANCE.others());
							menuItem2.setHideOnClick(false);
						   ((CheckMenuItem) menuItem2).addCheckChangeHandler(new CheckChangeHandler<CheckMenuItem>() {
								@Override
								public void onCheckChange(CheckChangeEvent<CheckMenuItem> event) {
									//set checked/unchecked all subitems
									setCheckedSubItems(event.getItem(), event.getChecked().equals(CheckState.CHECKED));										
									//add columns
									addColumnGroup (event.getItem(), !event.getChecked().equals(CheckState.CHECKED), true);	
								}						   
						   });
						} else {
							menuItem2 = new MenuItem(LocalizedText.INSTANCE.others());
						}
						Menu subMenu2 = new Menu();
						menuItem2.setSubMenu(subMenu2);
						subMenu.add(menuItem2);
						for (MenuItem menuItem : othersSubMenuList) {
						   addMenuItemAlphaSorted(subMenu2, menuItem, true);
						}
					}
				}
				
				//set check state for groups
				for (int i=0; i< subMenu.getWidgetCount(); i++) {
					Widget widget = subMenu.getWidget(i);
					if (!(widget instanceof CheckMenuItem))
						continue;
					
					CheckMenuItem menuItem = (CheckMenuItem) widget;
					if (menuItem.getSubMenu() == null || menuItem.getSubMenu().getWidgetCount() == 0)
						continue;
					
					Boolean checked = true;
					Boolean partlyChecked = false;
					for (int j=0; j< menuItem.getSubMenu().getWidgetCount(); j++) {
						Widget widget2 = menuItem.getSubMenu().getWidget(j);
						if (!(widget2 instanceof CheckMenuItem))
							continue;
						
						CheckMenuItem menuItem2 = (CheckMenuItem) widget2;
						if (!menuItem2.isChecked()) {
							checked = false;
						} else {
							partlyChecked = true;
						}
						
						if (!checked && partlyChecked)
							break;
					}
					
					if (!checked && partlyChecked) {
						menuItem.setChecked(true, true);
						menuItem.setIcon(MetaDataEditorResources.INSTANCE.checkGray());
						//menuItem.addStyleName(MetaDataEditorResources.INSTANCE.constellationCss().partlyChecked());
					} else {
						menuItem.setChecked(checked, true);
						//menuItem.removeStyleName(MetaDataEditorResources.INSTANCE.constellationCss().partlyChecked());
					}
				}
			}								
		}); 		
		
		/*
		for (Class<? extends GenericEntity> classValue : baseClass) {
			String className = classValue.getSimpleName() != null ? classValue.getSimpleName() : "";
			
			MenuItem menuItem2 = new MenuItem(className); 
			Menu subMenu2 = new Menu();
			menuItem2.setSubMenu(subMenu2);
			subMenu.add(menuItem2);
			
			expertList.provide(classValue, new CallbackEntityType() {					
				@Override
				public void onSuccess(Collection<EntityType<?>> future) {
					for (EntityType<?> entity : future) {								
					   Boolean isHidden = false;	
					   if (mapColumnsEntityType.containsValue(entity)) 
						   for (Entry<?, ?> entry : mapColumnsEntityType.entrySet()) 
							   if ((entry.getValue().equals(entity)) && (mapColumn.get(entry.getKey()).isHidden()))
								   isHidden = true; 
													
					   //RVE  - from Predicate show only "main" MetaData, not the PredicateErasure (Not possible to ask for value in PredicateErasures)
					   if  (!(PredicateErasure.T.isAssignableFrom(entity)) && (!(mapColumnsEntityType.containsValue(entity)) || isHidden)) {
						   MenuItem menuItem = new MenuItem(entity.getShortName()); 
						   menuItem.addSelectionHandler(new SelectionHandler<Item>() {					
								@Override
								@SuppressWarnings("unchecked")
								public void onSelection(SelectionEvent<Item> event) {
									if (isAddType)
										addColumn(entity.getShortName(), -1, (EntityType<? extends MetaData>) entity);	
									else	
										changeColumn(entity.getShortName(), colIndex, (EntityType<? extends MetaData>) entity);						
								}
						   });
						   
						   addMenuItemAlphaSorted(subMenu2, menuItem, true);								   
					   }
					}
					if (subMenu2.getWidgetCount() == 0)
						menuItem2.hide();
				}								
			}); 		
		}  */

		return subMenu;
	}
	
	private void addMenuItemAlphaSorted(Menu subMenu, MenuItem newMenuItem, Boolean useASC) {
		for (int i=0; i<subMenu.getWidgetCount(); i++) {
			if (!(subMenu.getWidget(i) instanceof MenuItem)) 
				continue;
			
			MenuItem menuItem = (MenuItem) subMenu.getWidget(i);
			if (!useASC) { 
				//DEScendant sort
				if (menuItem.getText().compareToIgnoreCase(newMenuItem.getText()) < 0) {
					subMenu.insert(newMenuItem, i);
					return;
				}		
			} else {
				//ASCendant sort
				if (menuItem.getText().compareToIgnoreCase(newMenuItem.getText()) > 0) {
					subMenu.insert(newMenuItem, i);
					return;
				}											
			}
		}															
		subMenu.add(newMenuItem);
	}
	
	private void selectFirstItem() {
		if (!isEmptyStore) {
			new Timer() {
				@Override
				public void run() {
					if (grid.getSelectionModel() instanceof CellSelectionModel)
						((CellSelectionModel<?>) grid.getSelectionModel()).selectCell(0, 0);
					else
						grid.getSelectionModel().select(0, false);  
				}
			}.schedule(100);			
		}
	}
	
    private String getGroupAssignmentForMetaData(EntityType<?> entity) {
    	if (entity == null)
    		return null;
    	
    	GroupAssignment groupAssignment = null;
    	
    	try {    	
    		groupAssignment = gmSession.getModelAccessory().getMetaData().entityTypeSignature(entity.getTypeSignature()).useCase(useCase).lenient(true).meta(GroupAssignment.T).exclusive();
    		if (groupAssignment == null || groupAssignment.getGroup() == null)
    			return null;
    	} catch (Exception e) {
    		return null;
		}
    			
    	return groupAssignment.getGroup().getName();
    }
	
    private String getUserName() {
    	if (userProvider == null)    	
    		return "";
    	
    	if (this.userName == null)
    		this.userName = userProvider.get().getResult().getName();
    	
    	return this.userName;
    }
    
	private void loadVisibleColumns() {
		if (this.workbenchSession == null) {
			return;
		}
		
		String folderName = null;
		if (this.lastModelPathElement.getValue() instanceof GmMetaModel)
			folderName = ROOT_FOLDER_NAME_MODEL;
		else if (this.lastModelPathElement.getValue() instanceof GmEntityTypeInfo)
			folderName = ROOT_FOLDER_NAME_ENTITY_TYPE;
		else if (this.lastModelPathElement.getValue() instanceof GmEnumTypeInfo)
			folderName = ROOT_FOLDER_NAME_ENUM_TYPE;
		
		if (folderName == null)
			return;
						
		folderName = '$' + folderName + "_" + getUserName();
		
		EntityQuery entityQuery = EntityQueryBuilder.from(Folder.class).tc(TC.create().negation().joker().done()).where().property("name").eq(folderName).done();
		EntityQueryResult queryResult = this.workbenchSession.query().entities(entityQuery).result();
		if (queryResult.getEntities() != null && !queryResult.getEntities().isEmpty()) {
			rootFolder = (Folder) queryResult.getEntities().get(0);
			addEntityListener(rootFolder);
		}
		
		if (rootFolder == null || rootFolder.getSubFolders() == null || rootFolder.getSubFolders().isEmpty())
			return;
		
		if (this.lastModelPathElement.getValue() instanceof GmMetaModel)
			this.mapMetaDataForEntityType.clear();
		else if (this.lastModelPathElement.getValue() instanceof GmEntityTypeInfo)
			this.mapMetaDataForProperty.clear();
		else if (this.lastModelPathElement.getValue() instanceof GmEnumTypeInfo)
			this.mapMetaDataForEnumConstant.clear();		
		
		this.mapColumnWidth.clear();
		
		for (Folder subFolder : rootFolder.getSubFolders()) {	
			Boolean columnVisible = false;
			addEntityListener(subFolder);
			String value =  subFolder.getName();
			String typeSignature = null;
			value = value.replace( "$" , "");
			int width = DEFAULT_DYNAMIC_COLUMN_WIDTH;
			
			for (String tagString : subFolder.getTags()) {
				if (tagString.equals("visible=1"))
					columnVisible = true;				
				else if (tagString.contains("width=")) {
					width = Integer.parseInt(tagString.replace("width=", ""));
				} else if (!tagString.contains("visible="))					
					typeSignature = tagString;
			}		
			
			if (typeSignature == null) {
				//set static columns visible/hidden  (e.g. Decalred Model, Decalred EntityType)
				if (value.equals(COLUMN_MODEL_NAME))
					this.ccModelName.setHidden(!columnVisible);
				if (value.equals(COLUMN_OWNER_NAME))
					this.ccOwnerName.setHidden(!columnVisible);	
				continue;
				
			}
			
			if (columnVisible) {
				if (this.lastModelPathElement.getValue() instanceof GmMetaModel) {
					addMapMetaDataForEntityType(value, GMF.getTypeReflection().getEntityType(typeSignature));
				} else if (this.lastModelPathElement.getValue() instanceof GmEntityTypeInfo) {
					addMapMetaDataForProperty(value, GMF.getTypeReflection().getEntityType(typeSignature));
				} else if (this.lastModelPathElement.getValue() instanceof GmEnumTypeInfo) {
					addMapMetaDataForEnumConstant(value, GMF.getTypeReflection().getEntityType(typeSignature));
				}
			}
			this.mapColumnWidth.put(value, width);
		}								
	}
    
	private void saveVisibleColumns() {
		if (this.workbenchSession == null || this.isColumnsChange == false) {
			return;
		}
		
		this.isColumnsChange = false;
		boolean hasChanges = false;
		List<Folder> subFolders = null;
		
		String folderName = null;
		if (this.lastModelPathElement.getValue() instanceof GmMetaModel)
			folderName = ROOT_FOLDER_NAME_MODEL;
		else if (this.lastModelPathElement.getValue() instanceof GmEntityTypeInfo)
			folderName = ROOT_FOLDER_NAME_ENTITY_TYPE;
		else if (this.lastModelPathElement.getValue() instanceof GmEnumTypeInfo)
			folderName = ROOT_FOLDER_NAME_ENUM_TYPE;
		
		if (folderName == null)
			return;
		
		//set User Specific Folder
		folderName = folderName + "_" + getUserName();
		
		EntityQuery entityQuery = EntityQueryBuilder.from(Folder.class).tc(TC.create().negation().joker().done()).where().property("name").eq(folderName).done();
		EntityQueryResult queryResult = this.workbenchSession.query().entities(entityQuery).result();
		if (queryResult.getEntities() != null && !queryResult.getEntities().isEmpty()) {
			rootFolder = (Folder) queryResult.getEntities().get(0);
		}

		this.editionNestedTransaction = this.workbenchSession.getTransaction().beginNestedTransaction();
		
		if (rootFolder == null) {
		   rootFolder = this.workbenchSession.create(Folder.T);
		   rootFolder.setName('$' + folderName);
		   LocalizedString displayName = this.workbenchSession.create(LocalizedString.T);
		   displayName.getLocalizedValues().put("default", folderName);				   
		   rootFolder.setDisplayName(displayName);
		   hasChanges = true;
		}
		
		subFolders = rootFolder.getSubFolders();
		if (subFolders == null) {
			subFolders = new ArrayList<Folder>();
			rootFolder.setSubFolders(subFolders);
			hasChanges = true;
		}
				
		int i = 0;
		
	    //save to Folder also if columns is visible - Tag "0" or "1"		
	    for (ColumnConfig<MetaDataEditorOverviewModel, ?> column : this.grid.getColumnModel().getColumns()) {
	       if (column.equals(this.ccItemName))
	    	   continue;   //this column is always showed	    	
	    	
		   Boolean columnVisible = !column.isHidden();
		   String tag = (columnVisible) ? "visible=1" : "visible=0";	
		   String stringName = "";
		   String typeSignature = null;
		   String width = "width=" + column.getWidth();
		   
		   if (column.equals(this.ccModelName))
			   stringName = COLUMN_MODEL_NAME;
		   else if (column.equals(this.ccOwnerName))
			   stringName = COLUMN_OWNER_NAME;
		   else {
               for (Entry<?, ?> entry : this.mapColumn.entrySet()) {
            	   String key = (String) entry.getKey();
            	   if (entry.getValue().equals(column)) {
            		   EntityType<? extends MetaData> metaDataType = this.mapColumnsEntityType.get(key);
            		   if (metaDataType != null) {
            			   stringName = metaDataType.getShortName();
            			   typeSignature = metaDataType.getTypeSignature();
            		   }            			   
            		   break;
            	   }
               }
		   }
		   
		   String stringKey = "$" + stringName;
		   Folder subFolder = null;
		   
		   for (Folder folder : rootFolder.getSubFolders()) {
			  if (folder.getName().equals(stringKey)) {
				  subFolder = folder;
				  break;
			  }					 
		   }
		   if (subFolder == null) {
			   subFolder = this.workbenchSession.create(Folder.T);
			   subFolder.setName(stringKey);
			   LocalizedString displayName = this.workbenchSession.create(LocalizedString.T);
			   displayName.getLocalizedValues().put("default", stringName);
			   subFolder.setDisplayName(displayName);
			   subFolder.setParent(rootFolder);
		   } else {
			   rootFolder.getSubFolders().remove(subFolder);
		   }

		   rootFolder.getSubFolders().add(i, subFolder);			   
		   Set<String> tags = new FastSet();
		   tags.add(tag);
		   tags.add(width);
		   if (typeSignature != null)
			   tags.add(typeSignature);
		   
		   subFolder.setTags(tags);
		   hasChanges = true;						   
		   i++;
		}
						
		if (hasChanges) {	
			this.editionNestedTransaction.commit();
			this.editionNestedTransaction = null;	
			
		} else {
			rollbackTransaction();
		}
	}
	
    
	private void exchangeWidget(Widget widget) {
		if (this.currentWidget == widget) 
			return;
		
		boolean doLayout = false;
		if (this.currentWidget != null) {
			this.remove(this.currentWidget);
			doLayout = true;
		}
		this.currentWidget = widget;
		//this.setCenterWidget(widget);
		this.setWidget(widget);
		if (doLayout)
			this.doLayout();
	}
	
	private HTML getEmptyPanel() {
		if (this.emptyPanel == null) {
			this.emptyPanel = new HTML(getEmptyPanelHtml());
		}
		
		return this.emptyPanel;
	}
	
	private String getEmptyPanelHtml() {
		StringBuilder html = new StringBuilder();
		html.append("<div style='height: 100%; width: 100%; display: table;' class='emptyStyle'>");
		html.append("<div style='display: table-cell; vertical-align: middle'>").append(this.emptyTextMessage).append("</div></div>");
		
		return html.toString();
	}
	
	private void updateEmptyPanel() {
		this.emptyPanel.setHTML(getEmptyPanelHtml());
	}
	
	private void rollbackTransaction() {
		try {
			if (this.editionNestedTransaction != null) {
				this.editionNestedTransaction.rollback();
			}
			this.editionNestedTransaction = null;
		} catch (TransactionException e) {
			//ErrorDialog.show(LocalizedText.INSTANCE.errorRollingEditionBack(), e);
			e.printStackTrace();
		} catch (IllegalStateException ex) {
			//Nothing to do: the PP was used within some widget which rolled back the parent transaction already. This may happen within GIMA when canceling it while editing.
		}
	}

	@Override
	public void noticeManipulation(final Manipulation manipulation) {
		if (ManipulationType.DELETE.equals(manipulation.manipulationType())) {
		    //is deleted
			MetaDataEditorOverviewView.this.grid.getTreeStore().clear();
			updateFilteredModels();
			return;
		} 
		if (!(manipulation instanceof PropertyManipulation)) 
			return;
		
		new Timer() {
			@Override
			public void run() {
				//Object parentObject = GMEUtil.getParentObject((PropertyManipulation) manipulation);
				GenericEntity entity = null;
				//String propertyName = null;
				Owner manipulationOwner = ((PropertyManipulation) manipulation).getOwner();
				if (manipulationOwner instanceof LocalEntityProperty) {
					entity = ((LocalEntityProperty) manipulationOwner).getEntity();
					//propertyName = ((LocalEntityProperty) manipulationOwner).getPropertyName();
				} else
					return;
					
				//types, typeOverrides, properties, propertyMetaData, propertyOverrides, constants
				if (entity.equals(MetaDataEditorOverviewView.this.lastModelPathElement.getValue())) {
					//GmPropertyInfo, GmEnumConstantInfo, GmEntityTypeInfo, GmEnumTypeInfo
					Property property = manipulationOwner.property();
					if (property.getName().equals("types") || property.getName().equals("typeOverrides") || property.getName().equals("properties")
						|| property.getName().equals("propertyMetaData") || property.getName().equals("propertyOverrides") || property.getName().equals("constants")) {
	                    //is changed
						switch (manipulation.manipulationType()) {
						case CHANGE_VALUE:
						case ABSENTING:
							break;
						case ADD:
						case REMOVE:
						case CLEAR_COLLECTION:
							//RVE need rebuild resolver, because can be send from external, and Model can have new EntityTypes or EnumTypes
							metaDataResolverProvider.rebuildResolver();
							setContent(MetaDataEditorOverviewView.this.lastModelPath);
							break;
						default:
							break;
						}
						return;
					}
				} 
				
				if (entity instanceof GmCustomTypeInfo || entity instanceof GmProperty || entity instanceof GmEnumConstant || entity instanceof MetaData)
				for (MetaDataEditorOverviewModel model : MetaDataEditorOverviewView.this.grid.getTreeStore().getAll()) {
				     if (model.getValue().equals(entity)) {
						switch (manipulation.manipulationType()) {
							case CHANGE_VALUE:
							case ADD:
							case REMOVE:
								if (entity instanceof GmCustomTypeInfo)
									getMetaDataFromResolver(metaDataResolverProvider.getModelMetaDataContextBuilder(), (GmCustomTypeInfo) entity, model);
								else if (entity instanceof GmProperty && editingEntity instanceof GmEntityType)
									getPropertyMetaDataFromResolver(metaDataResolverProvider.getModelMetaDataContextBuilder(), (GmProperty) entity, (GmEntityType) editingEntity, model);
								else if (entity instanceof GmEnumConstant && editingEntity instanceof GmEnumType)
									getEnumConstantMetaDataFromResolver(metaDataResolverProvider.getModelMetaDataContextBuilder(), (GmEnumConstant) entity, (GmEnumType) editingEntity, model);
								MetaDataEditorOverviewView.this.grid.getTreeStore().update(model);
								updateFilteredModels();
								break;
							default:
								break;
						}
						return;
						
				     } else if (entity instanceof MetaData) {
				    	 if (model.getMetaData().contains(entity)) {
							switch (manipulation.manipulationType()) {
								case INSTANTIATION:
								case MANIFESTATION:
								case ABSENTING:
								case CHANGE_VALUE:
								case ADD:
								case REMOVE:									
									if (model.getValue() instanceof GmCustomTypeInfo)
										getMetaDataFromResolver(metaDataResolverProvider.getModelMetaDataContextBuilder(), (GmCustomTypeInfo) model.getValue(), model);
									else if (model.getValue() instanceof GmProperty && editingEntity instanceof GmEntityType)
										getPropertyMetaDataFromResolver(metaDataResolverProvider.getModelMetaDataContextBuilder(), (GmProperty) model.getValue(), (GmEntityType) editingEntity, model);
									else if (model.getValue() instanceof GmEnumConstant && editingEntity instanceof GmEnumType)
										getEnumConstantMetaDataFromResolver(metaDataResolverProvider.getModelMetaDataContextBuilder(), (GmEnumConstant) model.getValue(), (GmEnumType) editingEntity, model);
									MetaDataEditorOverviewView.this.grid.getTreeStore().update(model);
									updateFilteredModels();
									break;
								default:
									break;
							}
							return;				    		 
				    	 }
				     }
				}
				
			}
		}.schedule(10); //needed, so the value in the entity was the correct one
	}

	private native void refreshRow(GridView<MetaDataEditorOverviewModel> view, int row) /*-{
		view.@com.sencha.gxt.widget.core.client.grid.GridView::refreshRow(I)(row);
	}-*/;

	@Override
	public void setSelectionFutureProvider(Supplier<? extends Function<SelectionConfig, ? extends Future<InstanceSelectionData>>> selectionFutureProvider) {
		this.selectionFutureProviderProvider = selectionFutureProvider;
	}

	@Override
	public void setSpecialFlowClasses(Set<Class<?>> specialFlowClasses) {
		this.specialFlowClasses = specialFlowClasses;
	}
}
