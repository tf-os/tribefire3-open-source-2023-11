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

import static com.braintribe.model.processing.session.api.common.GmSessions.getMetaData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.gwt.codec.date.client.ZonelessDateCodec;
import com.braintribe.gwt.genericmodelgxtsupport.client.PropertyFieldContext;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.TriggerFieldAction;
import com.braintribe.gwt.gm.storage.api.ColumnData;
import com.braintribe.gwt.gm.storage.api.StorageColumnInfo;
import com.braintribe.gwt.gme.assemblypanel.client.AssemblyUtil.ValueDescriptionBean;
import com.braintribe.gwt.gme.assemblypanel.client.action.ExchangeAssemblyPanelDisplayModeAction.DisplayMode;
import com.braintribe.gwt.gme.assemblypanel.client.model.AbstractGenericTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.EntityTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.PropertyEntryTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.TreePropertyModel;
import com.braintribe.gwt.gme.assemblypanel.client.resources.AssemblyPanelResources;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gxt.gxtresources.orangeflattab.client.WorkbenchTreeAppearance;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.constraint.Max;
import com.braintribe.model.meta.data.constraint.MaxLength;
import com.braintribe.model.meta.data.constraint.Min;
import com.braintribe.model.meta.data.constraint.MinLength;
import com.braintribe.model.meta.data.constraint.Pattern;
import com.braintribe.model.meta.data.display.Width;
import com.braintribe.model.meta.data.prompt.Embedded;
import com.braintribe.model.meta.data.prompt.EntityCompoundViewing;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.data.prompt.TimeZoneless;
import com.braintribe.model.meta.data.prompt.VirtualEnum;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.i18n.client.HasDirection.Direction;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.TextMetrics;
import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.data.shared.Converter;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.messages.client.DefaultMessages;
import com.sencha.gxt.widget.core.client.WindowManager;
import com.sencha.gxt.widget.core.client.form.IsField;
import com.sencha.gxt.widget.core.client.form.TriggerField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.GridView;
import com.sencha.gxt.widget.core.client.grid.GridView.GridAppearance;
import com.sencha.gxt.widget.core.client.grid.HeaderGroupConfig;
import com.sencha.gxt.widget.core.client.tips.QuickTip;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;
import com.sencha.gxt.widget.core.client.tree.Tree.TreeAppearance;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

public class AssemblyPanelTreeGrid extends TreeGrid<AbstractGenericTreeModel> implements DisposableBean {

	static {
		AssemblyPanelResources.INSTANCE.css().ensureInjected();
	}
	
	protected static final String MENU_CLASS = "assemblyTreeMenuClass";
	protected static final Logger logger = new Logger(AssemblyPanelTreeGrid.class);
	private static final String menuImageString = AbstractImagePrototype.create(AssemblyPanelResources.INSTANCE.blackMenu()).getHTML()
			.replaceFirst("style='", "class='" + MENU_CLASS + " " +
					AssemblyPanelResources.INSTANCE.css().pointerCursor() + "' style='margin-right: 6px !important; position: relative; margin-bottom: 0px;");
	
	private final AssemblyPanel assemblyPanel;
	protected Element menuImageElement;
	protected ExtendGridInlineEditing gridEditing;
	protected ColumnConfig<AbstractGenericTreeModel, AbstractGenericTreeModel> nodeColumn;
	private Timer windowChangedTimer;
	private Map<EntityType<?>, List<ColumnConfig<AbstractGenericTreeModel, ?>>> entityColumns;
	private Timer headerClickTimer;
	private int headerClickCount;
	private int columnHeaderClicked;
	protected AssemblyPanelSelectionModel selectionModel;
	private AssemblyPanelTreeGridView assemblyPanelTreeGridView;
	private Converter<Date, Date> timeZonelessConverter;
	
	public AssemblyPanelTreeGrid(ColumnConfig<AbstractGenericTreeModel, AbstractGenericTreeModel> nodeColumn, AssemblyPanel assemblyPanel) {
		super(prepareTreeStore(), prepareColumnModel(nodeColumn, assemblyPanel), nodeColumn, GWT.<GridAppearance> create(GridAppearance.class),
				GWT.<TreeAppearance> create(WorkbenchTreeAppearance.class));
		this.assemblyPanel = assemblyPanel;
		configureNodeColumn(nodeColumn);
		this.nodeColumn = nodeColumn;
		this.assemblyPanelTreeGridView = new AssemblyPanelTreeGridView(assemblyPanel);
		this.setView(assemblyPanelTreeGridView);
		this.addStyleName("assemblyPanelGrid");
		this.selectionModel = new AssemblyPanelSelectionModel(assemblyPanel);
		this.setSelectionModel(selectionModel);
		
		this.addDomHandler(new AssemblyPanelTreeGridMouseOverHandler(assemblyPanel, this), MouseOverEvent.getType());

		gridEditing = new ExtendGridInlineEditing(this, assemblyPanel, assemblyPanel.gmEditorSupport);
		if (assemblyPanel.showContextMenu) {
			prepareMenuImage();
			this.addDomHandler(event -> menuImageElement.getStyle().setDisplay(Display.NONE), MouseOutEvent.getType());
		}
		prepareQuickTip();
		configureTreeGrid();
	}

	@Override
	protected boolean hasChildren(AbstractGenericTreeModel model) {
		if (!assemblyPanel.isAllowExpandNodes())
			return false;
		
		if (isModelNotCompleteOrAbsent(model))
			return true;
		
		return super.hasChildren(model);
	}
	
	//We needed to override this method due to a bug within GXT 3.1.1
	@Override
	public void reconfigure(TreeStore<AbstractGenericTreeModel> store, ColumnModel<AbstractGenericTreeModel> cm, ColumnConfig<AbstractGenericTreeModel, ?> treeColumn) {
		if (isLoadMask())
			mask(DefaultMessages.getMessages().loadMask_msg());
		this.store.clear();

		nodes.clear();
		nodesByDomId.clear();

		this.store = createListStore();

		if (storeHandlerRegistration != null)
			storeHandlerRegistration.removeHandler();

		treeStore = store;
		if (treeStore != null)
			storeHandlerRegistration = treeStore.addStoreHandlers(storeHandler);

		if (isOrWasAttached())
			initGridViewData(treeGridView, this.store, cm);

		this.cm = cm;
		setTreeColumn(treeColumn);
		// rebind the sm
		setSelectionModel(sm);
		if (isViewReady())
			view.refresh(true);

		if (isLoadMask())
			unmask();
	}
	
	protected AssemblyPanelTreeStore getAssemblyPanelTreeStore() {
		return (AssemblyPanelTreeStore) treeStore;
	}
	
	protected ColumnConfig<AbstractGenericTreeModel, AbstractGenericTreeModel> getNodeColumn() {
		return nodeColumn;
	}
	
	protected void configureAutoExpand() {
		((AssemblyPanelTreeGridView) this.getView()).configureAutoExpand(nodeColumn);
	}
	
	private static TreeStore<AbstractGenericTreeModel> prepareTreeStore() {
		return new AssemblyPanelTreeStore(item -> item.getId().toString());
	}
	
	private static ColumnModel<AbstractGenericTreeModel> prepareColumnModel(ColumnConfig<AbstractGenericTreeModel, AbstractGenericTreeModel> nodeColumn, AssemblyPanel assemblyPanel) {
		List<ColumnConfig<AbstractGenericTreeModel, ?>> columns = new ArrayList<>();
		columns.add(nodeColumn);
		
		ColumnModel<AbstractGenericTreeModel> cm = new ColumnModel<>(columns);
		prepareColumnHiddenChangeHandler(nodeColumn, assemblyPanel, cm);
		return cm;
	}

	private static void prepareColumnHiddenChangeHandler(ColumnConfig<AbstractGenericTreeModel, AbstractGenericTreeModel> nodeColumn,
			AssemblyPanel assemblyPanel, ColumnModel<AbstractGenericTreeModel> cm) {
		cm.addColumnHiddenChangeHandler(event -> handleColumnChangesEvents(event.getSource(), nodeColumn, assemblyPanel, false));
		cm.addColumnWidthChangeHandler(
				event -> handleColumnChangesEvents(event.getSource(), nodeColumn, assemblyPanel, event.getColumnConfig() == nodeColumn));
		cm.addColumnMoveHandler(event -> handleColumnChangesEvents(event.getSource(), nodeColumn, assemblyPanel, false));
	}
	
	private static void handleColumnChangesEvents(ColumnModel<?> columnModel, ColumnConfig<?, ?> nodeColumn, AssemblyPanel assemblyPanel,
			boolean changedNodeWidth) {
		boolean displayNode = !nodeColumn.isHidden();
		Integer nodeWidth = null;
		if (changedNodeWidth)
			nodeWidth = nodeColumn.getWidth();
		List<StorageColumnInfo> columnsVisible = new ArrayList<>();
		columnModel.getColumns().forEach(c -> {
			if (c != nodeColumn && !c.isHidden()) {
				StorageColumnInfo ci = StorageColumnInfo.T.create();
				ci.setPath(c.getPath());
				ci.setWidth(c.getWidth());
				columnsVisible.add(ci);
			}
		});
		
		assemblyPanel.fireColumnChanged(displayNode, nodeWidth, columnsVisible);
	}
	
	private void configureNodeColumn(ColumnConfig<AbstractGenericTreeModel, AbstractGenericTreeModel> nodeColumn) {
		nodeColumn.setWidth(300);
		nodeColumn.setCellPadding(false);
		nodeColumn.setSortable(false);
		nodeColumn.setCell(new AssemblyPanelNodeColumnCell(assemblyPanel));
		nodeColumn.setColumnHeaderClassName(GMEUtil.PROPERTY_NAME_CSS);
	}
	
	private void prepareMenuImage() {
		menuImageElement = DOM.createTD();
		menuImageElement.getStyle().setWidth(10, Unit.PX);
		menuImageElement.setInnerHTML(menuImageString);
		DOM.sinkEvents(menuImageElement, Event.ONMOUSEOVER | Event.ONMOUSEOUT);
		DOM.setEventListener(menuImageElement, event -> {
			if (event.getTypeInt() == Event.ONMOUSEOVER)
				menuImageElement.getStyle().clearDisplay();
			else if (event.getTypeInt() == Event.ONMOUSEOUT)
				menuImageElement.getStyle().setDisplay(Display.NONE);
		});
	}
	
	private void prepareQuickTip() {
		QuickTip quickTip = new QuickTip(this);
		ToolTipConfig config = new ToolTipConfig();
		config.setMaxWidth(400);
		config.setDismissDelay(0);
		quickTip.update(config);
	}
	
	private void configureTreeGrid() {
		this.addDomHandler(event -> assemblyPanel.fireClickOrDoubleClick(true, new AssemblyPanelMouseInteractionEvent(event, assemblyPanel)), ClickEvent.getType());
		
		//addBeforeShowContextMenuHandler(event -> assemblyPanel.fireClickOrDoubleClick(true, new AssemblyPanelMouseInteractionEvent(event, assemblyPanel)));
		
		addDomHandler(event -> assemblyPanel.fireClickOrDoubleClick(true, new AssemblyPanelMouseInteractionEvent(event, assemblyPanel)), ContextMenuEvent.getType());
		
		this.addRowDoubleClickHandler(event -> assemblyPanel.fireClickOrDoubleClick(false, new AssemblyPanelMouseInteractionEvent(event, assemblyPanel)));
		
		this.addBeforeExpandHandler(event -> event.setCancelled(assemblyPanel.fireBeforeExpand(new AssemblyPanelMouseInteractionEvent(event, assemblyPanel))));
		
		this.addBodyScrollHandler(event -> fireWindowChanged());
		
		this.addResizeHandler(event -> fireWindowChanged());
		
		this.addAttachHandler(event -> fireWindowChanged());
		
		this.setIconProvider(model -> getIcon(model));
		
		this.addExpandHandler(event -> {
			AbstractGenericTreeModel model = event.getItem();
			if (isModelNotCompleteOrAbsent(model)) {
				//getSelectionModel().select(model, true);
				assemblyPanel.loadAbsentOrIncompleteModel(model);
				EntityTreeModel entityTreeModel = model.getDelegate().getEntityTreeModel();
				if (entityTreeModel != null && entityTreeModel.isExpandPending()) {
					entityTreeModel.clearExpandPending();
					setExpanded(model, true);
				}
				return;
			}
			assemblyPanel.ensureAllChildrenAreVisible(model);
		});
		
		this.addCollapseHandler(event -> {
			fireWindowChanged();
			AbstractGenericTreeModel model = event.getItem(); //Added this for fixing problem with GXT losing focus
			//https://www.sencha.com/forum/showthread.php?355295
			//Do not focus the grid in case we have some active window
			if (model != null && WindowManager.get().getActive() == null)
				getTreeView().focusRow(getStore().indexOf(event.getItem()));
		});
		
		//Double click handler does not work. Thus, we hacked it
		this.addHeaderClickHandler(event -> {
			headerClickCount++;
			columnHeaderClicked = event.getColumnIndex();
			getHeaderClickTimer().schedule(500);
		});
		
		setColumnReordering(true);
	}
	
	private ImageResource getIcon(AbstractGenericTreeModel model) {
		ImageResource icon = model.getIcon();
		if (icon != null)
			return icon;
		
		icon = assemblyPanel.assemblyUtil.prepareIcon(model, !(model instanceof PropertyEntryTreeModel), true);
		model.setIcon(icon);
		
		return icon;
	}
	
	private Timer getHeaderClickTimer() {
		if (headerClickTimer != null)
			return headerClickTimer;
		
		headerClickTimer = new Timer() {
			@Override
			public void run() {
				if (headerClickCount == 2)
					resizeColumnToFit(columnHeaderClicked);
				
				headerClickCount = 0;
			}
		};
		
		return headerClickTimer;
	}
	
	protected void fireWindowChanged() {
		if (windowChangedTimer == null) {
			windowChangedTimer = new Timer() {
				@Override
				public void run() {
					if (assemblyPanel.isVisible() && assemblyPanel.gmViewportListeners != null)
						assemblyPanel.gmViewportListeners.forEach(listener -> listener.onWindowChanged(assemblyPanel));
				}
			};
		}
		
		windowChangedTimer.schedule(200);
	}
	
	protected boolean isModelNotCompleteOrAbsent(AbstractGenericTreeModel model) {
		if (AssemblyUtil.isNotComplete(model))
			return true;
		
		return AssemblyUtil.isModelAbsent(model) != null ? true : false;
	}
	
	protected void prepareTreeGridColumns(GenericEntity entity, EntityType<GenericEntity> entityType, ModelMdResolver metaDataResolver,
			ColumnData columnData) {
		List<ColumnConfig<AbstractGenericTreeModel, ?>> columns = null;
		if (entityColumns != null)
			columns = entityColumns.get(entityType);
		else
			entityColumns = new HashMap<>();
		
		if (columns != null && getColumnModel().getColumns().containsAll(columns))
			return;
		
		if (columnData == null)
			nodeColumn.setHidden(false);
		else {
			if (columnData.getNodeWidth() != null)
				nodeColumn.setWidth(columnData.getNodeWidth());
			if (columnData.getNodeTitle() != null)
				nodeColumn.setHeader(I18nTools.getLocalized(columnData.getNodeTitle()));
			
			if (columnData.getDisableExpansion())
				assemblyPanel.setAllowExpandNodes(false);
			
			if (columnData.getDisplayNode())
				nodeColumn.setHidden(false);
			else {
				int index = getColumnModel().getColumns().indexOf(nodeColumn);
				//Hiding the first column brings troubles. If that is the case, we set the width to 0 instead of hiding
				if (index == 0)
					nodeColumn.setWidth(0);
				else	
					nodeColumn.setHidden(true);
			}
		}
		
		columns = new ArrayList<>();
		columns.add(nodeColumn);
		entityColumns.put(entityType, columns);
		
		Map<String, EntityCompoundViewing> entityCompoundViewings = GMEMetadataUtil.getEntityCompoundViewingsMap(metaDataResolver.entityType(entityType));
		Map<ColumnConfig<AbstractGenericTreeModel, ?>, List<String>> compoundColumnsConfigMap = new LinkedHashMap<>();
		List<PriorityColumnConfig> priorityColumnConfigs = new ArrayList<>();
		
		prepareTreeGridColumns(entityType.getProperties(), entity, entityType, priorityColumnConfigs, entityCompoundViewings,
				compoundColumnsConfigMap, metaDataResolver, columnData);
		
		Collections.sort(priorityColumnConfigs, PriorityColumnConfig.getPriorityComparator());
		
		if (columnData != null) {
			logger.debug("ColumnData is available within AP.");
			Map<String, ColumnConfig<AbstractGenericTreeModel,?>> map = new LinkedHashMap<>();
			for (PriorityColumnConfig pcc : priorityColumnConfigs) {
				ColumnConfig<AbstractGenericTreeModel,?> cc = pcc.getColumnConfig();
				map.put(cc.getPath(), cc);
			}
			
			for (StorageColumnInfo columnInfo : columnData.getDisplayPaths()) {
				ColumnConfig<AbstractGenericTreeModel, ?> columnConfig = map.remove(columnInfo.getPath());
				if (columnConfig != null ) {
					if (columnInfo.getWidth() != 0)
						columnConfig.setWidth(columnInfo.getWidth());
					columns.add(columnConfig);
					
					if (columnInfo.getTitle() != null)
						columnConfig.setHeader(I18nTools.getLocalized(columnInfo.getTitle()));

					if (columnInfo.getAutoExpand())
						((AssemblyPanelTreeGridView) this.getView()).configureAutoExpand(columnConfig);
				}
			}
			
			for (ColumnConfig<AbstractGenericTreeModel, ?> cc : map.values()) {
				cc.setHidden(true);
				columns.add(cc);
			}
		} else {
			for (PriorityColumnConfig priorityColumnConfig : priorityColumnConfigs)
				columns.add(priorityColumnConfig.getColumnConfig());
		}
		
		ColumnModel<AbstractGenericTreeModel> cm = new ColumnModel<>(columns);
		prepareColumnHiddenChangeHandler(nodeColumn, assemblyPanel, cm);
		
		prepareCompoundHeaders(columns, compoundColumnsConfigMap, cm);
		
		this.reconfigure(this.getTreeStore(), cm, nodeColumn);
		gridEditing.setEditableGrid(this);
		//Scheduler.get().scheduleDeferred(() -> getTreeView().refresh(true));
	}
	
	private void prepareTreeGridColumns(List<Property> properties, GenericEntity parentEntity, EntityType<?> parentEntityType,
			List<PriorityColumnConfig> priorityColumnConfigs, Map<String, EntityCompoundViewing> entityCompoundViewings,
			Map<ColumnConfig<AbstractGenericTreeModel, ?>, List<String>> compoundColumnsConfigMap, ModelMdResolver metaDataResolver, ColumnData columnData) {
		for (Property property : properties)
			prepareTreeGridColumn(property, parentEntity, parentEntityType, metaDataResolver, priorityColumnConfigs, entityCompoundViewings, compoundColumnsConfigMap, columnData);
		
		if (columnData != null) {
			columnData.getDisplayPaths().stream().filter(column -> column.getDeclaringTypeSignature() != null).forEach(column -> {
				EntityType<?> declaringType = GMF.getTypeReflection().getEntityType(column.getDeclaringTypeSignature());
				Property property = declaringType.getProperty(column.getPath());
				prepareTreeGridColumn(property, null, declaringType, metaDataResolver, priorityColumnConfigs, entityCompoundViewings,
						compoundColumnsConfigMap, columnData);
			});
		}
	}
	
	private void prepareTreeGridColumn(Property property, GenericEntity parentEntity,
			EntityType<?> parentEntityType, ModelMdResolver metaDataResolver, List<PriorityColumnConfig> priorityColumnConfigs,
			Map<String, EntityCompoundViewing> entityCompoundViewings,
			Map<ColumnConfig<AbstractGenericTreeModel, ?>, List<String>> compoundColumnsConfigMap, ColumnData columnData) {
		GenericModelType propertyType = property.getType();
		if (propertyType.isCollection())
			return;
		
		String propertyName = property.getName();
		boolean isSimpleOrSimplified = true;
		
		EntityMdResolver entityMdResolver = parentEntity != null ? metaDataResolver.entity(parentEntity)
				: metaDataResolver.entityType(parentEntityType);
		PropertyMdResolver propertyMdResolver = entityMdResolver.property(propertyName);
		
		if (!propertyType.isEntity() && !propertyType.isBase())
			isSimpleOrSimplified = !AssemblyUtil.hasOutline(property, parentEntity, parentEntityType, metaDataResolver, assemblyPanel.modelFactory);
		else {
			if (propertyType.isEntity()) {
				EntityType<?> propertyEntityType = (EntityType<?>) propertyType;
				Embedded embedded = propertyMdResolver.useCase(assemblyPanel.useCase).meta(Embedded.T).exclusive();
				if (embedded == null)
					embedded = metaDataResolver.entityType(propertyEntityType).useCase(assemblyPanel.useCase).meta(Embedded.T).exclusive();
				
				if (embedded != null) {
					prepareEmbeddedTreeGridColumns(parentEntity, priorityColumnConfigs, entityCompoundViewings, compoundColumnsConfigMap,
							metaDataResolver, property, propertyEntityType, embedded, columnData);
					return;
				}
				
				EntityCompoundViewing entityCompoundViewing = entityCompoundViewings != null ? entityCompoundViewings.get(propertyName) : null;
				if (entityCompoundViewing != null) {
					compoundColumnsConfigMap.putAll(prepareCompoundViewingColumns(entityCompoundViewing, property, parentEntityType,
							metaDataResolver, priorityColumnConfigs));
					return;
				}
			}
			
			isSimpleOrSimplified = AssemblyUtil.isInline(property, parentEntity, parentEntityType, metaDataResolver, assemblyPanel.modelFactory);
		}
		
		if (!isSimpleOrSimplified)
			return;
		
		if (propertyMdResolver != null && !GMEMetadataUtil.isPropertyVisible(propertyMdResolver))
			return;
		
		Double priority = GMEMetadataUtil.getPropertyPriority(propertyMdResolver);
		
		Name propertyDisplayInfo = propertyMdResolver == null ? null : GMEMetadataUtil.getName(propertyMdResolver);
		String propertyDisplay = GMEMetadataUtil.getPropertyDisplay(propertyDisplayInfo, propertyName);
		
		Width width = propertyMdResolver == null ? null : propertyMdResolver.meta(Width.T).exclusive();
		Integer preferredWidth = width != null ? width.getWidth() : null;
		
		ColumnConfig<AbstractGenericTreeModel, Object> columnConfig = preparePropertyColumnConfig(property, propertyDisplay, preferredWidth);
		if (!assemblyPanel.readOnly) { //The editor doesn't need to be created right away. Postponing its creation
			Scheduler.get().scheduleDeferred(() -> {
				IsField<Object> editor = prepareEditor(property, parentEntity, parentEntityType);
				if (editor != null)
					gridEditing.addEditor(columnConfig, editor);
			});
		}
		
		priorityColumnConfigs.add(new PriorityColumnConfig(priority, columnConfig));
	}

	private void prepareEmbeddedTreeGridColumns(GenericEntity parentEntity, List<PriorityColumnConfig> priorityColumnConfigs,
			Map<String, EntityCompoundViewing> entityCompoundViewings,
			Map<ColumnConfig<AbstractGenericTreeModel, ?>, List<String>> compoundColumnsConfigMap, ModelMdResolver metaDataResolver,
			Property property, EntityType<?> propertyEntityType, Embedded embedded, ColumnData columnData) {
		GenericEntity embeddedEntity = parentEntity != null ? property.get(parentEntity) : null;
		
		List<String> embeddedProperties = new ArrayList<>();
		if (embedded.getIncludes().isEmpty())
			propertyEntityType.getProperties().forEach(p -> embeddedProperties.add(p.getName()));
		else
			embeddedProperties.addAll(embedded.getIncludes());
		
		embeddedProperties.removeAll(embedded.getExcludes());
		
		List<Property> subProperties = new ArrayList<>();
		embeddedProperties.forEach(embeddedProperty -> subProperties.add(propertyEntityType.getProperty(embeddedProperty)));
		
		prepareTreeGridColumns(subProperties, embeddedEntity, propertyEntityType, priorityColumnConfigs, entityCompoundViewings,
				compoundColumnsConfigMap, metaDataResolver, columnData);
	}
	
	private Map<ColumnConfig<AbstractGenericTreeModel, ?>, List<String>> prepareCompoundViewingColumns(EntityCompoundViewing entityCompoundViewing,
			Property compoundProperty, EntityType<?> parentEntityType, ModelMdResolver metaDataResolver,
			List<PriorityColumnConfig> priorityColumnConfigs) {
		Map<ColumnConfig<AbstractGenericTreeModel, ?>, List<String>> compoundColumnConfigs = new HashMap<>();

		PropertyMdResolver propertyMetaDataContextBuilder = metaDataResolver.entityType(parentEntityType)
				.property(compoundProperty.getName()).useCase(assemblyPanel.useCase);

		Double priority = GMEMetadataUtil.getPropertyPriority(propertyMetaDataContextBuilder);
		if (priority == null)
			priority = Double.MIN_VALUE;

		String groupName = GMEMetadataUtil.getGroupName(propertyMetaDataContextBuilder);
		
		List<GmProperty> properties = entityCompoundViewing.getPropertyPath().getProperties();
		List<GmProperty> subProperties = properties.subList(1, properties.size());
		prepareCompoundViewingColumn(compoundProperty, metaDataResolver, priorityColumnConfigs, compoundColumnConfigs, propertyMetaDataContextBuilder,
				priority, groupName, subProperties);

		return compoundColumnConfigs;
	}

	private void prepareCompoundViewingColumn(Property compoundProperty, ModelMdResolver metaDataResolver,
			List<PriorityColumnConfig> priorityColumnConfigs, Map<ColumnConfig<AbstractGenericTreeModel, ?>, List<String>> compoundColumnConfigs,
			PropertyMdResolver propertyMetaDataContextBuilder, Double priority, String groupName, List<GmProperty> properties) {
		EntityType<?> compoundEntityType = (EntityType<?>) compoundProperty.getType();
		List<String> compoundNames = new ArrayList<>();
		String propertyName = compoundProperty.getName();
		String propertyDisplayName = GMEMetadataUtil.getPropertyDisplay(propertyName, propertyMetaDataContextBuilder);
		compoundNames.add(propertyDisplayName);

		EntityType<?> currentEntityType = compoundEntityType;
		if (properties.size() > 1)
			currentEntityType = (EntityType<?>) compoundProperty.getType();

		EntityMdResolver currentMetaDataContextBuilder = metaDataResolver.entityType(currentEntityType);
		for (int i = 0; i < properties.size() - 1; i++) {
			GmProperty property = properties.get(i);
			String currentPropertyName = property.getName();
			propertyName += "." + currentPropertyName;
			propertyDisplayName += ".";
			currentPropertyName = GMEMetadataUtil.getPropertyDisplay(currentPropertyName, currentMetaDataContextBuilder.property(currentPropertyName));
			compoundNames.add(currentPropertyName);
			propertyDisplayName += currentPropertyName;
			currentEntityType = GMF.getTypeReflection().getEntityType(property.getType().getTypeSignature());
		}

		String currentPropertyName = properties.get(properties.size() - 1).getName();
		Property currentProperty = currentEntityType.getProperty(currentPropertyName);
		propertyName += "." + currentPropertyName;
		propertyDisplayName += ".";
		currentPropertyName = GMEMetadataUtil.getPropertyDisplay(currentPropertyName, currentMetaDataContextBuilder.property(currentPropertyName));
		// compoundNames.add(currentPropertyName);
		propertyDisplayName += currentPropertyName;

		ColumnConfig<AbstractGenericTreeModel, Object> columnConfig = new ColumnConfig<>(
				new CompoundPropertyTreeModelValueProvider(compoundProperty, properties), 100, currentPropertyName);
		columnConfig.setCellPadding(false);
		columnConfig.setCell(assemblyPanelTreeGridView.preparePropertyColumnsCell());
		columnConfig.setSortable(false);
		columnConfig.setColumnHeaderClassName(GMEUtil.PROPERTY_NAME_CSS);
		if (currentProperty.getType().isSimple() && ((SimpleType) currentProperty.getType()).isNumber()) {
			columnConfig.setHorizontalAlignment(HorizontalAlignmentConstant.endOf(Direction.LTR));
			//columnConfig.setFixed(true);
		}
		if (!assemblyPanel.readOnly)
			gridEditing.addEditor(columnConfig, getConverter(currentProperty, currentEntityType), prepareEditor(currentProperty, null, currentEntityType));

		priorityColumnConfigs.add(new PriorityColumnConfig(priority, columnConfig));
		compoundColumnConfigs.put(columnConfig, groupName != null ? Collections.singletonList(groupName) : compoundNames);
	}
	
	@SuppressWarnings("rawtypes")
	private Converter getConverter(Property property, EntityType<?> entityType) {
		PropertyMdResolver propertyMdResolver = assemblyPanel.gmSession.getModelAccessory().getMetaData().entityType(entityType)
				.useCase(assemblyPanel.useCase).property(property);
		
		if (!propertyMdResolver.is(TimeZoneless.T))
			return null;
		
		if (timeZonelessConverter != null)
			return timeZonelessConverter;
		
		timeZonelessConverter = new Converter<Date, Date>() {
			@Override
			public Date convertModelValue(Date zonedDate) {
				return ZonelessDateCodec.INSTANCE.decode(zonedDate);
			}
			
			@Override
			public Date convertFieldValue(Date zonelessDate) {
				return ZonelessDateCodec.INSTANCE.encode(zonelessDate);
			}
		};
		
		return timeZonelessConverter;
	}
	
	@SuppressWarnings("rawtypes")
	private <N> IsField<N> prepareEditor(Property property, GenericEntity entity, EntityType<?> entityType) {
		ModelMdResolver modelMdResolver;
		EntityMdResolver entityMdResolver;
		if (entity != null) {
			modelMdResolver = getMetaData(entity);
			entityMdResolver = modelMdResolver.entity(entity);
		} else {
			modelMdResolver = assemblyPanel.gmSession.getModelAccessory().getMetaData();
			entityMdResolver = modelMdResolver.entityType(entityType);
		}
		
		String propertyName = property.getName();
		PropertyMdResolver propertyMdResolver = entityMdResolver.property(propertyName).useCase(assemblyPanel.useCase);
		
		boolean isPassword = GMEMetadataUtil.isPropertyPassword(propertyMdResolver);
		boolean useAlternativeField = AssemblyUtil.hasOutline(property, entity, entityType, modelMdResolver, assemblyPanel.modelFactory);
		
		MinLength minLenghtMetaData = propertyMdResolver.meta(MinLength.T).exclusive();
		int minLenght = minLenghtMetaData == null ? -1 : ((Long) minLenghtMetaData.getLength()).intValue();
		
		MaxLength maxLenghtMetaData = propertyMdResolver.meta(MaxLength.T).exclusive();
		int maxLenght = maxLenghtMetaData == null ? -1 : ((Long) maxLenghtMetaData.getLength()).intValue();
		
		Object minValue = null;
		Min min = propertyMdResolver.meta(Min.T).exclusive();
		if (min != null)
			minValue = min.getLimit();
		
		Object maxValue = null;
		Max max = propertyMdResolver.meta(Max.T).exclusive();
		if (max != null)
			maxValue = max.getLimit();
		
		PropertyFieldContext context = new PropertyFieldContext();
		context.setMandatory(propertyMdResolver.is(Mandatory.T));
		context.setPassword(isPassword);
		context.setModelType(property.getType());
		context.setRegex(propertyMdResolver.meta(Pattern.T).exclusive());
		context.setUseAlternativeField(useAlternativeField);
		context.setUseCase(assemblyPanel.useCase);
		context.setGmSession(assemblyPanel.gmSession);
		context.setVirtualEnum(propertyMdResolver.meta(VirtualEnum.T).exclusive());
		context.setMinLenght(minLenght);
		context.setMaxLenght(maxLenght);
		context.setParentEntity(entity);
		context.setParentEntityType(entityType);
		context.setPropertyName(propertyName);
		context.setMinValue(minValue);
		context.setMaxValue(maxValue);
		
		IsField<N> field = (IsField) assemblyPanel.gmEditorSupport.providePropertyField(context);
		
		if (field instanceof TriggerFieldAction) {
			if (field instanceof TriggerField<?>)
				((TriggerField<?>) field).setHideTrigger(true);
			
			if (assemblyPanel.propertyTriggerFieldActionMap == null)
				assemblyPanel.propertyTriggerFieldActionMap = new FastMap<>();
			assemblyPanel.propertyTriggerFieldActionMap.put(propertyName, (TriggerFieldAction) field);
		}
		
		return field;
	}
	
	@Override
	public void disposeBean() throws Exception {
		if (entityColumns != null)
			entityColumns.clear();
	}
	
	private ColumnConfig<AbstractGenericTreeModel, Object> preparePropertyColumnConfig(Property property, String propertyDisplay, Integer preferredWidth) {
		ColumnConfig<AbstractGenericTreeModel, Object> columnConfig = new ColumnConfig<>(new PropertyTreeModelValueProvider(property),
				preferredWidth != null ? preferredWidth : 100, propertyDisplay);

		columnConfig.setCellPadding(false);
		columnConfig.setCell(assemblyPanelTreeGridView.preparePropertyColumnsCell());
		columnConfig.setSortable(false);
		columnConfig.setHidden(DisplayMode.Simple.equals(assemblyPanel.currentDisplayMode));
		columnConfig.setColumnHeaderClassName(GMEUtil.PROPERTY_NAME_CSS);
		if (property.getType().isSimple() && ((SimpleType) property.getType()).isNumber()) {
			columnConfig.setHorizontalAlignment(HorizontalAlignmentConstant.endOf(Direction.LTR));
			//columnConfig.setFixed(true);
		}
		
		return columnConfig;
	}
	
	private void prepareCompoundHeaders(List<ColumnConfig<AbstractGenericTreeModel, ?>> columns, Map<ColumnConfig<AbstractGenericTreeModel, ?>,
			List<String>> compoundColumnsConfigMap, ColumnModel<AbstractGenericTreeModel> cm) {
		if (compoundColumnsConfigMap.isEmpty())
			return;
		
		Map<String, List<Integer>> headerMap = new LinkedHashMap<>();
		for (Map.Entry<ColumnConfig<AbstractGenericTreeModel, ?>, List<String>> entry : compoundColumnsConfigMap.entrySet()) {
			StringBuilder compoundName = new StringBuilder();
			for (String name : entry.getValue()) {
				if (compoundName.length() != 0)
					compoundName.append(".");
				compoundName.append(name);
			}
			String compoundNameString = compoundName.toString();
			List<Integer> indices = headerMap.get(compoundNameString);
			if (indices == null) {
				indices = new ArrayList<>();
				headerMap.put(compoundNameString, indices);
			}
			indices.add(columns.indexOf(entry.getKey()));
			entry.getKey().setWidth(130);
		}
		
		for (Map.Entry<String, List<Integer>> entry : headerMap.entrySet()) {
			Collections.sort(entry.getValue());
			cm.addHeaderGroup(0, entry.getValue().get(0), new HeaderGroupConfig(entry.getKey(), 1, entry.getValue().size()));
		}
	}
	
	private void resizeColumnToFit(int columnHeaderClicked) {
		ColumnModel<AbstractGenericTreeModel> columnModel = getColumnModel();
		final ColumnConfig<AbstractGenericTreeModel, ?> columnConfig = columnModel.getColumn(columnHeaderClicked);
		final TextMetrics textMetrics = TextMetrics.get();
		List<AbstractGenericTreeModel> rootItems = treeStore.getRootItems();
		if (!rootItems.isEmpty()) {
			textMetrics.bind(getView().getHeader().getAppearance().styles().head());
			int maxWidth = textMetrics.getWidth(columnConfig.getHeader().asString()) + 20;
			
			textMetrics.bind(getView().getCell(0, columnHeaderClicked));
			for (AbstractGenericTreeModel rootModel : rootItems) {
				String text;
				AbstractGenericTreeModel delegateModel = rootModel.getDelegate();
				if (columnHeaderClicked == 0) {
					ValueDescriptionBean label = rootModel.getLabel();
					if (label == null)
						continue;
					text = label.getValue();
				} else {
					TreePropertyModel treePropertyModel = null;
					ValueProvider<? super AbstractGenericTreeModel, ?> valueProvider = columnConfig.getValueProvider();
					if (valueProvider instanceof PropertyTreeModelValueProvider)
						treePropertyModel = delegateModel.getTreePropertyModel(((PropertyTreeModelValueProvider) valueProvider).getProperty());
					if (treePropertyModel == null)
						continue;
					
					if (treePropertyModel.getElementType().getJavaType() == Boolean.class)
						return;
					text = treePropertyModel.getLabel();
				}
				
				int width = textMetrics.getWidth(text) + (columnHeaderClicked == 0 ? 80 : 70);
				maxWidth = Math.max(maxWidth, width);
			}
			
			columnModel.setUserResized(true);
			columnModel.setColumnWidth(columnHeaderClicked, maxWidth);
		}
	}
	
	private static native void initGridViewData(GridView<AbstractGenericTreeModel> view, ListStore<AbstractGenericTreeModel> ds, ColumnModel<AbstractGenericTreeModel> cm) /*-{
		view.@com.sencha.gxt.widget.core.client.grid.GridView::initData(Lcom/sencha/gxt/data/shared/ListStore;Lcom/sencha/gxt/widget/core/client/grid/ColumnModel;)(ds, cm);
	}-*/;
	
	protected static class AssemblyPanelTreeStore extends TreeStore<AbstractGenericTreeModel> {
		
		public AssemblyPanelTreeStore(ModelKeyProvider<AbstractGenericTreeModel> keyProvider) {
			super(keyProvider);
		}
		
		protected boolean isWrapperExisting(AbstractGenericTreeModel model) {
			return getWrapper(model) != null;
		}
		
	}

}
