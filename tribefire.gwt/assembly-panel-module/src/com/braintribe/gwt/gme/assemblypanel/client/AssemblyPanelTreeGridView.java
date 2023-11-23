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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.gm.storage.api.StorageColumnInfo;
import com.braintribe.gwt.gme.assemblypanel.client.model.AbstractGenericTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.CompoundTreePropertyModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.EntityTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.TreePropertyModel;
import com.braintribe.gwt.gme.assemblypanel.client.resources.AssemblyPanelCss;
import com.braintribe.gwt.gme.assemblypanel.client.resources.AssemblyPanelResources;
import com.braintribe.gwt.gmview.client.IconAndType;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.PropertyBean;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gxt.gxtresources.gridwithoutlines.client.GxtClearGridTemplates;
import com.braintribe.gwt.gxt.gxtresources.whitecolumnheader.client.WhiteColumnHeader;
import com.braintribe.gwt.logging.client.Profiling;
import com.braintribe.gwt.logging.client.ProfilingHandle;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.PropertyPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.data.prompt.VirtualEnum;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.session.api.common.GmSessions;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.i18n.client.HasDirection.Direction;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.messages.client.DefaultMessages;
import com.sencha.gxt.widget.core.client.event.CheckChangeEvent;
import com.sencha.gxt.widget.core.client.event.CheckChangeEvent.CheckChangeHandler;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid.GridCell;
import com.sencha.gxt.widget.core.client.grid.GridViewConfig;
import com.sencha.gxt.widget.core.client.grid.RowExpander;
import com.sencha.gxt.widget.core.client.menu.CheckMenuItem;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.tree.Tree.TreeNode;
import com.sencha.gxt.widget.core.client.treegrid.TreeGridView;

public class AssemblyPanelTreeGridView extends TreeGridView<AbstractGenericTreeModel> {
	
	protected static final String emptyStringImageString = AbstractImagePrototype.create(AssemblyPanelResources.INSTANCE.nullIcon()).getHTML()
			.replaceFirst("style='", "qtip='" + LocalizedText.INSTANCE.empty() + "' style='margin-left: 4px; ");
	
	private AssemblyPanel assemblyPanel;
	private boolean focusAlreadyPerformed;
	private Cell<Object> propertyColumnsCell;
	private int lastMainHeaderColumnWidth = 0;
	private List<Object> listLastShowedColumns = new ArrayList<>();
	
	public AssemblyPanelTreeGridView(AssemblyPanel assemblyPanel) {
		this.assemblyPanel = assemblyPanel;
		tpls = GWT.create(GxtClearGridTemplates.class);
		
		this.setViewConfig(new GridViewConfig<AbstractGenericTreeModel>() {
			@Override
			public String getRowStyle(AbstractGenericTreeModel model, int rowIndex) {
				return "";
			}
			
			@Override
			public String getColStyle(AbstractGenericTreeModel m, ValueProvider<? super AbstractGenericTreeModel, ?> vp, int row, int col) {
				return "gmeGridColumn";
			}
		});
		
		this.setShowDirtyCells(false);
	}

	/*
	 * Overriding to add the drag and drop listener to refreshed rows.
	 */
	@Override
	protected void refreshRow(int row) {
		super.refreshRow(row);
		AssemblyUtil.addDragAndDropListenerToElementIndex(row, assemblyPanel);
	}
	
	@Override
	public void focusRow(int rowIndex) {
		if (scroller == null)
			return;
		
		int left = scroller.getScrollLeft();
		Element focusedCell = getFocusedCell();
		int colIndex = 0;
		if (focusedCell != null)
			colIndex = findCellIndex(focusedCell, null);
		focusCell(rowIndex, colIndex, false);
		if (colIndex == 0)
			scroller.setScrollLeft(left);
	}
	
	@Override
	protected void onMouseDown(Event ge) {
		if (ge.getEventTarget() == null) {
			super.onMouseDown(ge);
			return;
		}
		
		Element eventTargetElement = (Element) ge.getEventTarget().cast();
		String cls = eventTargetElement.getClassName();
		int rowIndex = findRowIndex(eventTargetElement);
		int colIndex = findCellIndex(eventTargetElement, null);
		AbstractGenericTreeModel model = assemblyPanel.editorTreeGrid.getStore().get(rowIndex);
		
		if (model == null) {
			super.onMouseDown(ge);
			return;
		}
		
		if (ge.getAltKey()) {
			if (assemblyPanel.checkUncondenseLocalEnablement())
				assemblyPanel.uncondenseLocal();
			else if (assemblyPanel.getCondensendProperty() != null)
				assemblyPanel.condenseLocal();
		}
		
		if (GMEUtil.containsCssDefinitions(cls, Arrays.asList(AssemblyPanelResources.INSTANCE.css().checkedValue(), AssemblyPanelResources.INSTANCE.css().uncheckedValue(),
				AssemblyPanelResources.INSTANCE.css().checkNullValue()))) {
			EntityTreeModel entityTreeModel = model.getDelegate().getEntityTreeModel();
			if (!assemblyPanel.readOnly && entityTreeModel != null) {
				TreePropertyModel propertyModel = getTreePropertyModelByIndex(colIndex, entityTreeModel);
				if (propertyModel != null && !propertyModel.isReadOnly()) {
					Boolean startValue = (Boolean) propertyModel.getValue();
					boolean newValue = startValue == null ? true : !startValue;
					entityTreeModel.getElementType().getProperty(propertyModel.getPropertyName()).set((GenericEntity) entityTreeModel.getModelObject(),
							newValue);
				}
			}
		} else if (cls.contains(AssemblyPanelTreeGrid.MENU_CLASS)) {
			if (ge.getButton() != NativeEvent.BUTTON_RIGHT && assemblyPanel.showContextMenu) {
				if (assemblyPanel.helperMenuPropertyModel != null) //Hiding the previous menu before displaying a new one is needed due to some flickering affecting the client
					assemblyPanel.getHelperMenu().hide();
				if (assemblyPanel.actionsContextMenu != null)
					assemblyPanel.actionsContextMenu.hide();
				
				Scheduler.get().scheduleDeferred(() -> {
					EntityTreeModel entityTreeModel = model.getDelegate().getEntityTreeModel();
					if (colIndex == 0) { 
						assemblyPanel.updateEmptyMenuItem();
						assemblyPanel.actionsContextMenu.showAt(ge.getClientX(), ge.getClientY());
					} else if (entityTreeModel != null) {
						TreePropertyModel propertyModel = getTreePropertyModelByIndex(colIndex, entityTreeModel);
						if (propertyModel != null) {
							assemblyPanel.helperMenuPropertyModel = propertyModel;
							assemblyPanel.helperMenuGridCell = new GridCell(rowIndex, colIndex);
							assemblyPanel.getHelperMenu().showAt(ge.getClientX(), ge.getClientY());
						}
					}
				});
			}
		} else if (cls.contains(AssemblyPanelNodeColumnCell.LINK_CLASS)) {
			if (assemblyPanel.navigationEnabled) {
				ModelAction action = assemblyPanel.actionManager.getWorkWithEntityAction(assemblyPanel);
				if (action != null) {
					action.updateState(Collections.singletonList(Collections.singletonList(AssemblyUtil.getModelPath(model, assemblyPanel.rootModelPath))));
					action.perform(null);
				}
			}
		}
		
		super.onMouseDown(ge);
	}
	
	@Override
	protected void onRowSelect(int rowIndex) {
		if (assemblyPanel.enableRowSelection) {
			super.onRowSelect(rowIndex);
			Element row = getRow(rowIndex);
		    if (row != null)
		    	row.addClassName("x-grid3-row-selected");
		}
	}
	
	@Override
	protected void onRowDeselect(int rowIndex) {
		super.onRowDeselect(rowIndex);
		Element row = getRow(rowIndex);
	    if (row != null)
	    	row.removeClassName("x-grid3-row-selected");
	}
	
	@Override
	public void collapse(TreeNode<AbstractGenericTreeModel> node) {
		assemblyPanel.ignoreSelection = true;
		super.collapse(node);
		assemblyPanel.ignoreSelection = false;
	}
	
	
	@Override
	protected Menu createContextMenu(int colIndex) {		
		//Menu menu = new Menu();
		//Menu menu = super.createContextMenu(colIndex);	
		//RVE - copy from GridView.java -> need fix hide of main column - workaround to set min width
		if (cm.isMenuDisabled(colIndex)) {
		    return null;
		}
		
		if (listLastShowedColumns.isEmpty())
			saveLastShowedColumns();
		
		final Menu menu = new Menu();
		
		if (cm.isSortable(colIndex)) {
		    MenuItem item = new MenuItem();
		    item.setText(DefaultMessages.getMessages().gridView_sortAscText());
		    item.setIcon(header.getAppearance().sortAscendingIcon());
		    item.addSelectionHandler(new SelectionHandler<Item>() {
		      @Override
		      public void onSelection(SelectionEvent<Item> event) {
		        doSort(colIndex, SortDir.ASC);
		      }
		    });
		    menu.add(item);
		
		    item = new MenuItem();
		    item.setText(DefaultMessages.getMessages().gridView_sortDescText());
		    item.setIcon(header.getAppearance().sortDescendingIcon());
		    item.addSelectionHandler(new SelectionHandler<Item>() {
		      @Override
		      public void onSelection(SelectionEvent<Item> event) {
		        doSort(colIndex, SortDir.DESC);
		      }
		    });
		    menu.add(item);
		}
	
		boolean isFirstVisibleColumn = checkFirstVisibleColumn(colIndex);
		if (!isFirstVisibleColumn) {
			MenuItem menuItem = new MenuItem(com.braintribe.gwt.gxt.gxtresources.text.LocalizedText.INSTANCE.hide());
			menuItem.setIcon(AssemblyPanelResources.INSTANCE.checkNull());
			menuItem.setItemId("hide");
			menuItem.addSelectionHandler(event -> {
				Scheduler.get().scheduleDeferred(() -> {
					cm.setHidden(colIndex, true);
				});				
			});
			menu.add(menuItem);				
		} else {
			MenuItem columns = new MenuItem();
			columns.setText(DefaultMessages.getMessages().gridView_columnsText());
			columns.setIcon(header.getAppearance().columnsIcon());
			columns.setData("gxt-columns", "true");
				
			final Menu columnMenu = new Menu();
			int cols = cm.getColumnCount();
			for (int i = 0; i < cols; i++) {
			    ColumnConfig<AbstractGenericTreeModel, AbstractGenericTreeModel> config = cm.getColumn(i);
			    // ignore columns that can't be hidden or shouldn't appear in the column list (e.g. row expanders)
				if (!config.isHideable() || config instanceof RowExpander) {
				  continue;
				}
				final int fcol = i;
				final CheckMenuItem check = new CheckMenuItem();
				check.setHideOnClick(false);
				check.setHTML(cm.getColumnHeader(i));
				if (fcol == 0)
					check.setChecked(cm.getColumnWidth(fcol) > 1);	        	
				else
					check.setChecked(!cm.isHidden(i));
				check.setData("gxt-column-index", i);
			    check.addCheckChangeHandler(new CheckChangeHandler<CheckMenuItem>() {
				      @Override
				      public void onCheckChange(CheckChangeEvent<CheckMenuItem> event) {
						Scheduler.get().scheduleDeferred(() -> {  
					    	if (fcol == 0) {
					    		if (cm.getColumnWidth(fcol) <= 1) 
					    			if (lastMainHeaderColumnWidth == 0)
					    				cm.setColumnWidth(fcol, 200);
					    			else
					    				cm.setColumnWidth(fcol, lastMainHeaderColumnWidth);	        				
					    		else {
					    			lastMainHeaderColumnWidth = cm.getColumnWidth(fcol);
					    			cm.setColumnWidth(fcol, 0);
					    		}
					    	} else {	        	  
					    		cm.setHidden(fcol, !cm.isHidden(fcol));
					    	}
					        restrictMenu(cm, columnMenu);
						});
				      }
			    });
			    columnMenu.add(check);
			}
			restrictMenu(cm, columnMenu);
			columns.setEnabled(columnMenu.getWidgetCount() > 0);
			columns.setSubMenu(columnMenu);
			menu.add(columns);
			
			boolean showLastUsedColumns = false;
			if (!isShowingAllColumns()) {
				MenuItem menuItem = new MenuItem(com.braintribe.gwt.gxt.gxtresources.text.LocalizedText.INSTANCE.showAllColumns());
				//menuItem.setIcon(AssemblyPanelResources.INSTANCE.checked());
				menuItem.setItemId("showAll");
				menuItem.addSelectionHandler(event -> {
					Scheduler.get().scheduleDeferred(() -> {  
						saveLastShowedColumns();
						boolean visibilityChanged = false;
						for (int i = 0; i < assemblyPanel.editorTreeGrid.getColumnModel().getColumnCount(); i++) {
							boolean changed = updateColumnVisibility(i, true);
							visibilityChanged = visibilityChanged || changed;
						}
						if (visibilityChanged)
							refreshColumns();
					});
				});
				menu.add(menuItem);
			} else {
				showLastUsedColumns = true;
			}
			
			if (!isHidingAllColumns()) {
				MenuItem menuItem = new MenuItem(com.braintribe.gwt.gxt.gxtresources.text.LocalizedText.INSTANCE.hideAllColumns());
				//menuItem.setIcon(AssemblyPanelResources.INSTANCE.checkNull());
				menuItem.setItemId("hideAll");
				menuItem.addSelectionHandler(event -> {
					Scheduler.get().scheduleDeferred(() -> {  
						saveLastShowedColumns();
						boolean visibilityChanged = false;
						for (int i = 0; i < assemblyPanel.editorTreeGrid.getColumnModel().getColumnCount(); i++) {
							boolean changed = false;
							if (i == 0)
								changed = updateColumnVisibility(i, true);	//RVE - 1st column is always Visible				
							else	
								changed = updateColumnVisibility(i, false);	
							visibilityChanged = visibilityChanged || changed;	
						}
						if (visibilityChanged)
							refreshColumns();
					});
				});
				menu.add(menuItem);
			} else {
				showLastUsedColumns = true;
			}
			
			if (showLastUsedColumns) {
				MenuItem menuItem = new MenuItem(com.braintribe.gwt.gxt.gxtresources.text.LocalizedText.INSTANCE.showConfiguredColumns());
				//menuItem.setIcon(AssemblyPanelResources.INSTANCE.checked());
				menuItem.setItemId("showStandard");
				menuItem.addSelectionHandler(event -> {
					Scheduler.get().scheduleDeferred(() -> {  
						if (listLastShowedColumns.isEmpty())
							return;
						
						boolean visibilityChanged = false;
						for (int i = 0; i < assemblyPanel.editorTreeGrid.getColumnModel().getColumnCount(); i++) {
							boolean changed = false;
							if (listLastShowedColumns.contains(i))
								changed = updateColumnVisibility(i, true);	//RVE - 1st column is always Visible				
							else	
								changed = updateColumnVisibility(i, false);	
							visibilityChanged = visibilityChanged || changed;	
						}
						if (visibilityChanged)
							refreshColumns();
					});
				});
				menu.add(menuItem);				
			}
		}
		return menu;
	}
	
	private void saveLastShowedColumns() {
		if ((isHidingAllColumns() || isShowingAllColumns()) && !listLastShowedColumns.isEmpty())
			return;
		
		listLastShowedColumns.clear();
		for (int i = 0; i < assemblyPanel.editorTreeGrid.getColumnModel().getColumnCount(); i++) {
			if (!assemblyPanel.editorTreeGrid.getColumnModel().getColumn(i).isHidden())
				listLastShowedColumns.add(i);
		}
		
	}

	private boolean isHidingAllColumns() {
		int visibleCount = 0;
		for (int i = 0; i < assemblyPanel.editorTreeGrid.getColumnModel().getColumnCount(); i++) {
			if (!assemblyPanel.editorTreeGrid.getColumnModel().getColumn(i).isHidden()) {
				visibleCount++;
			    if (visibleCount > 1)
			    	break;			    	 
			}
		}
		return (visibleCount == 1); //1 column is always visible
	}

	private boolean isShowingAllColumns() {
		boolean result = true;
		for (int i = 0; i < assemblyPanel.editorTreeGrid.getColumnModel().getColumnCount(); i++) {
			if (assemblyPanel.editorTreeGrid.getColumnModel().getColumn(i).isHidden()) {
				result = false;
			    break;
			}
		}
		return result;
	}

	private boolean checkFirstVisibleColumn(int colIndex) {
		boolean result = true;
		int i = colIndex - 1;
		
		while (i >= 0) {
			if (!assemblyPanel.editorTreeGrid.getColumnModel().getColumn(i).isHidden()) {
				result = false;
				break;
			}
			
			i--;
		}
		
		return result;
	}

	private void restrictMenu(ColumnModel<AbstractGenericTreeModel> cm, Menu columns) {
	  	    int count = 0;
	  	    for (int i = 0, len = cm.getColumnCount(); i < len; i++) {
	  	      ColumnConfig<AbstractGenericTreeModel, AbstractGenericTreeModel> cc = cm.getColumn(i);
	  	      if (cc.isHidden() || !cc.isHideable() || cc instanceof RowExpander) {
	  	        continue;
	  	      }
	  	      count++;
	  	    }

	  	    if (count == 1) {
	  	      for (int i = 0, len = columns.getWidgetCount(); i < len; i++) {
	  	        CheckMenuItem ci = (CheckMenuItem) columns.getWidget(i);
	  	        if (ci.isChecked()) {
	  	          ci.disable();
	  	        }
	  	      }
	  	    } else {
	  	      for (int i = 0, len = columns.getWidgetCount(); i < len; i++) {
	  	        CheckMenuItem item = (CheckMenuItem) columns.getWidget(i);
	  	        int col = item.getData("gxt-column-index");
	  	        ColumnConfig<AbstractGenericTreeModel, AbstractGenericTreeModel> config = cm.getColumn(col);
	  	        if (config.isHideable()) {
	  	          item.enable();
	  	        }
	  	      }
	  	    }
	  	  }
	
	protected Cell<Object> preparePropertyColumnsCell() {
		if (propertyColumnsCell == null) {
			propertyColumnsCell = new AbstractCell<Object>() {
				@Override
				public void render(com.google.gwt.cell.client.Cell.Context context, Object object, SafeHtmlBuilder sb) {
					AbstractGenericTreeModel model = assemblyPanel.editorTreeGrid.getTreeStore().findModelWithKey((String) context.getKey());
					preparePropertyColumnRenderer(context, sb, model);
				}
			};
		}
		
		return propertyColumnsCell;
	}
	
	private void refreshColumns() {
		ProfilingHandle ph = Profiling.start(getClass(), "Refreshing columns", false, true);
		scroller.setScrollLeft(0);
		header.refresh();
		assemblyPanel.editorTreeGrid.getView().layout();
		
		ColumnConfig<AbstractGenericTreeModel, AbstractGenericTreeModel> nodeColumn = assemblyPanel.editorTreeGrid.getNodeColumn();
		boolean displayNode = !nodeColumn.isHidden();
		
		List<StorageColumnInfo> columnsVisible = new ArrayList<>();
		assemblyPanel.editorTreeGrid.getColumnModel().getColumns().forEach(col -> {
			if (col != nodeColumn && !col.isHidden()) {
				StorageColumnInfo ci = StorageColumnInfo.T.create();
				ci.setPath(col.getPath());
				ci.setWidth(col.getWidth());
				columnsVisible.add(ci);
			}
		});
		
		assemblyPanel.fireColumnChanged(displayNode, null, columnsVisible);
		ph.stop();
	}
	
	@Override
	public TreeNode<AbstractGenericTreeModel> findNode(AbstractGenericTreeModel m) {
		return super.findNode(m);
	}
	
	/**
	 * Configures the auto expand for this column.
	 */
	public void configureAutoExpand(ColumnConfig<AbstractGenericTreeModel, ?> autoExpandColum) {
		this.setAutoExpandMax(900);
		this.setAutoExpandMin(200);
		this.setAutoExpandColumn(autoExpandColum);
	}
	
	private TreePropertyModel getTreePropertyModelByIndex(int index, EntityTreeModel entityTreeModel) {
		if (index != -1) {
			String propertyName = assemblyPanel.editorTreeGrid.getColumnModel().getColumn(index).getPath();
			return entityTreeModel.getTreePropertyModel(propertyName);
		}
		
		return null;
	}
	
	private boolean updateColumnVisibility (int columnIndex, Boolean showColumn) {
		boolean visibilityChanged = false;
		ColumnConfig<AbstractGenericTreeModel, ?> column = this.assemblyPanel.editorTreeGrid.getColumnModel().getColumn(columnIndex);
		if (column != null && column.isHidden() != !showColumn) {
			column.setHidden(!showColumn);
			visibilityChanged = true;
		}
		
		return visibilityChanged;
	}
	
	@Override
	protected void onFocus(Event event) {
		if (!focusAlreadyPerformed) {
			super.onFocus(event);
			focusAlreadyPerformed = true;
		}
	}
	
	private void preparePropertyColumnRenderer(com.google.gwt.cell.client.Cell.Context context, SafeHtmlBuilder sb, AbstractGenericTreeModel model) {
		EntityTreeModel entityTreeModel = model.getDelegate().getEntityTreeModel();
		if (entityTreeModel == null)
			return;
		
		GenericEntity entity = entityTreeModel.getModelObject();
		EntityType<?> entityType = entityTreeModel.getElementType();
		
		//if (!assemblyPanel.entityTypeForProperties.equals(entityType) && !assemblyPanel.entityTypeForProperties.isAssignableFrom(entityType))
			//return;
		
		boolean visible = true;
		String propertyName = assemblyPanel.editorTreeGrid.getColumnModel().getColumn(context.getColumn()).getPath();
		if (!propertyName.contains(".")) {
			EntityMdResolver entityContextBuilder;
			if (entity != null)
				entityContextBuilder = getMetaData(entity).entity(entity);
			else
				entityContextBuilder = assemblyPanel.gmSession.getModelAccessory().getMetaData().lenient(true).entityType(entityType);
			visible = GMEMetadataUtil.isPropertyVisible(entityContextBuilder.lenient(true).property(propertyName).useCase(assemblyPanel.useCase));
		}
		
		if (visible) {
			ColumnConfig<AbstractGenericTreeModel, ?> columnConfig = assemblyPanel.editorTreeGrid.getColumnModel().getColumn(context.getColumn());
			TreePropertyModel propertyModel = entityTreeModel.getTreePropertyModel(propertyName);
			if (propertyModel == null)
				return;
			
			String editableBoxCss = "";
			if (!assemblyPanel.readOnly && !propertyModel.isReadOnly())
				editableBoxCss = " " + AssemblyPanelResources.INSTANCE.css().editableBox();

			IconAndType iconAndType = assemblyPanel.getIconProvider().apply(getModelPath(propertyModel));
			String icon = iconAndType != null ? prepareIconHtml(iconAndType) : "";
			sb.appendHtmlConstant("<div style='display:flex;align-items:center' class='" + GMEUtil.PROPERTY_VALUE_CSS + " " +
					AssemblyPanelCss.EXTERNAL_PROPERTY_VALUE + editableBoxCss + "'>" + icon + prepareValueRendererString(propertyName, model,
					HorizontalAlignmentConstant.endOf(Direction.LTR).equals(columnConfig.getHorizontalAlignment())) + "</div>");
		}
	}
	
	private ModelPath getModelPath(TreePropertyModel propertyModel)  {
		if (propertyModel == null)
			return null;
		
		GenericEntity modelParentEntity = propertyModel.getParentEntity();
		if (modelParentEntity == null)
			return null;
		
		ModelPath modelPath = new ModelPath();
		EntityType<GenericEntity> modelParentEntityType = modelParentEntity.entityType();
		RootPathElement rootPathElement = new RootPathElement(modelParentEntityType, modelParentEntity);
		modelPath.add(rootPathElement);
		
			
		VirtualEnum ve = propertyModel.getVirtualEnum();
		if (ve != null) {					
			PropertyPathElement veElement = new PropertyPathElement(ve, ve.entityType().getProperty("constants"), propertyModel.getValue());
			modelPath.add(veElement);
		} else {
			Property property = modelParentEntityType.findProperty(propertyModel.getPropertyName());
			if (property == null)
				return modelPath;
			
			PropertyPathElement propertyPathElement = new PropertyPathElement(modelParentEntity, property, propertyModel.getValue());
			modelPath.add(propertyPathElement);
		}
		
		return modelPath;
	}
	
	private String prepareIconHtml(IconAndType iconAndType) {
		return "<div class='" + AssemblyPanelCss.EXTERNAL_PROPERTY_ICON_GROUP + "'><img class='" + AssemblyPanelCss.EXTERNAL_PROPERTY_ICON + "' src='" + iconAndType.getIcon().getSafeUri().asString() + "'/></div>";
	}
	
	private String prepareValueRendererString(String propertyName, AbstractGenericTreeModel model, boolean usePadding) {
		EntityTreeModel entityTreeModel = model.getDelegate().getEntityTreeModel();
		TreePropertyModel propertyModel = entityTreeModel.getTreePropertyModel(propertyName);
		if (propertyModel == null)
			return "";
		
		if (propertyModel instanceof CompoundTreePropertyModel)
			assemblyPanel.addManipulationListener(propertyModel.getParentEntity());
		else if (propertyModel.getElementType().isEntity())
			assemblyPanel.addManipulationListener(propertyModel.getValue());
		
		String valueIcon = (propertyModel.getValue() instanceof String && ((String) propertyModel.getValue()).isEmpty()) ? emptyStringImageString : null;
		
		StringBuilder builder = new StringBuilder();
		builder.append("<div class='").append(AssemblyPanelCss.EXTERNAL_PROPERTY_TEXT).append("'>\n");
		builder.append("<table class='").append(AssemblyPanelResources.INSTANCE.css().inheritFont()).append(" ")
				.append(AssemblyPanelResources.INSTANCE.css().tableFixedLayout());
		builder.append("' border='0' cellpadding='2' cellspacing='0'>\n");
		builder.append("   <tr class='").append(AssemblyPanelResources.INSTANCE.css().inheritFont()).append("'>\n");
		
		if (valueIcon != null)
			builder.append("      <td class='gxtReset' width='14px'>").append(valueIcon).append("&nbsp;</td>\n");
		
		builder.append("      <td class='gxtReset ").append(AssemblyPanelResources.INSTANCE.css().inheritFont()).append(" ")
				.append(AssemblyPanelResources.INSTANCE.css().textOverflowNoWrap());
		
		if (usePadding)
			builder.append(" ").append(AssemblyPanelResources.INSTANCE.css().propertyValueWithPadding()).append(" ").append(GMEUtil.PROPERTY_VALUE_CSS);
		
		builder.append("' width='100%' height='14px'");
		
		if (propertyModel.isAbsent())
			builder.append(" qtip='").append(LocalizedText.INSTANCE.absent()).append("'");
		
		builder.append(">").append(prepareValueDisplay(propertyModel)).append("</td>\n");
		builder.append("   </tr>\n</table>\n");
		builder.append("</div>");
		return builder.toString();
	}
	
	private String prepareValueDisplay(TreePropertyModel propertyModel) {
		if (propertyModel.isAbsent())
			return "&nbsp;";
		
		if (propertyModel.getLabel() != null)
			return propertyModel.getLabel();
		
		Object propertyValue = propertyModel.getValue();
		GenericModelType elementType = propertyModel.getElementType();
		if (elementType.getJavaType() == Boolean.class) {
			String booleanClass;
			
			AssemblyPanelCss css = AssemblyPanelResources.INSTANCE.css();
			if (assemblyPanel.readOnly || propertyModel.isReadOnly()) {
				if (propertyValue == null)
					booleanClass = css.checkNullReadOnlyValue();
				else
					booleanClass = ((Boolean) propertyValue) ? css.checkedReadOnlyValue() : css.uncheckedReadOnlyValue();
			} else {
				if (propertyValue == null)
					booleanClass = css.checkNullValue();
				else
					booleanClass = ((Boolean) propertyValue) ? css.checkedValue() : css.uncheckedValue();
			}
			
			String display = "<div class='" + booleanClass;
			if (propertyValue != null)
				display += " " + (((Boolean) propertyValue) ? "CHECKED" : "UNCHECKED");
			return display + "'/>";
		}
		
		String valueDisplay = prepareStringValue(elementType, propertyValue,
				new PropertyBean(propertyModel.getPropertyName(), propertyModel.getParentEntity(), null));
		
		if (propertyModel.isPassword())
			valueDisplay = GMEUtil.preparePasswordString(valueDisplay);
		else
			valueDisplay = SafeHtmlUtils.htmlEscape(valueDisplay);

		propertyModel.setLabel(valueDisplay);
		return valueDisplay;
	}
	
	private String prepareStringValue(GenericModelType valueType, Object propertyValue, PropertyBean propertyBean) {
		if (propertyValue == null)
			return "";

		String stringValue = null;
		Codec<Object, String> renderer = assemblyPanel.assemblyUtil.getRendererCodec(valueType, propertyBean);
		if (renderer != null) {
			try {
				stringValue = renderer.encode(propertyValue);
			} catch (CodecException e) {
				AssemblyPanel.logger.error("Error while getting value renderer value.", e);
				e.printStackTrace();
			}
		}
		
		if (stringValue == null) {
			ModelMdResolver modelMdResolver = propertyValue instanceof GenericEntity ? GmSessions.getMetaData((GenericEntity) propertyValue)
					: assemblyPanel.gmSession.getModelAccessory().getMetaData();
			if (valueType.isEntity()) {
				String selectiveInformation = SelectiveInformationResolver.resolve((EntityType<?>) valueType, (GenericEntity) propertyValue, modelMdResolver,
						assemblyPanel.useCase/* , null */);
				if (selectiveInformation != null && !selectiveInformation.trim().isEmpty())
					stringValue = selectiveInformation;
			} else if (propertyValue instanceof Enum) {
				String enumString = propertyValue.toString();
				Name nameMD = modelMdResolver.lenient(true).useCase(assemblyPanel.useCase).enumConstant((Enum<?>) propertyValue).meta(Name.T).exclusive();
				if (nameMD != null) {
					LocalizedString name = nameMD.getName();
					if (name != null)
						enumString = I18nTools.getLocalized(name);
				}
				stringValue = enumString;
			}
		}
		
		return stringValue != null ? stringValue : propertyValue.toString();
	}
	
	@Override
	protected void autoExpand(boolean preventUpdate) {
		    if (!cm.isUserResized() && getAutoExpandColumn() != null) {
		      int tw = cm.getTotalWidth(false);
		      int aw = grid.getOffsetWidth(true) - getScrollAdjust();
		      if (tw != aw) {
		        int ci = cm.indexOf(getAutoExpandColumn());
		        //assert ci != Style.DEFAULT : "auto expand column not found";
		        if (cm.isHidden(ci)) {
		          return;
		        }
		        int currentWidth = cm.getColumnWidth(ci);
		        //RVE - need prevent to auto resize the main column as is counted as Hidden
		        if (ci == 0 && currentWidth <= 1)
		        	return;
		        
		        int cw = Math.min(Math.max(((aw - tw) + currentWidth), getAutoExpandMin()), getAutoExpandMax());
		        if (cw != currentWidth) {
		          cm.setColumnWidth(ci, cw, true);

		          if (!preventUpdate) {
		            updateColumnWidth(ci, cw);
		          }
		        }
		      }
		    }
	}	
	
	/**
	 * Initializes the column header and saves reference for future use, creating one if it hasn't yet been set
	 */
	@Override
	protected void initHeader() {
	    if ((header == null) || !(header instanceof WhiteColumnHeader)) {
	      header = new WhiteColumnHeader<AbstractGenericTreeModel>(grid, cm);
	    }
	    super.initHeader();
	}	
	
	// @formatter:off
	private native Element getFocusedCell() /*-{
		return this.@com.sencha.gxt.widget.core.client.grid.GridView::focusedCell;
	}-*/;
	// @formatter:on

	/*
	 * GSC: 2019-03-17:
	 *  
	 * Tried to "hack" the backround-size style into the the icon-element. 
	 * This would enable the AP to display even bigger sized icons properly and delegate scaling to the browser.
	 * Unfortunately that jus partially worked and needs further investigation before enabling it.
	 * 
	 */
	/*
	@Override
	public void onIconStyleChange(TreeNode<AbstractGenericTreeModel> node, ImageResource icon) {
		
		Element iconElement = getIconElement(node);
	    if (iconElement != null) {
	      logger.info("Intercepting icon styling...");
	      XElement elem = getElement(node);
	      XElement treeIconElement = tree.getTreeAppearance().onIconChange(elem, iconElement.<XElement> cast(), icon);
	      iconElement.getStyle().setProperty("background-size", "100%");
	      treeIconElement.getStyle().setProperty("background-size", "100%");
	      node.setIconElement(treeIconElement);
	      logger.info("Intercepted icon styling on tree-element: "+treeIconElement.getId()+" taken from: "+iconElement.getId());
	    } else {
	    	logger.info("No icon element for interception found!");
	    }
	    
	}*/

}
