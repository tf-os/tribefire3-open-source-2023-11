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
package com.braintribe.gwt.gme.propertypanel.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.gwt.gme.propertypanel.client.AbstractPropertyPanel.ValueRendering;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelCss;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelResources;
import com.braintribe.gwt.gmview.action.client.AddExistingEntitiesToCollectionAction;
import com.braintribe.gwt.gmview.action.client.ChangeInstanceAction;
import com.braintribe.gwt.gxt.gxtresources.multieditor.client.MultiEditorGridInlineEditing;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.display.Group;
import com.braintribe.model.meta.data.prompt.HideDetailsActions;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.DomQuery;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.core.shared.FastSet;
import com.sencha.gxt.widget.core.client.ComponentHelper;
import com.sencha.gxt.widget.core.client.event.RowClickEvent;
import com.sencha.gxt.widget.core.client.grid.Grid.GridCell;
import com.sencha.gxt.widget.core.client.grid.GridView;
import com.sencha.gxt.widget.core.client.grid.RowExpander;

public class PropertyPanelRowExpander extends RowExpander<PropertyModel> {
	
	private static int lastId = 0;
	
	private PropertyPanel propertyPanel;
	private boolean runTimer = true;
	private Map<String, Widget> widgets;
	
	public PropertyPanelRowExpander(PropertyPanel propertyPanel) {
		super(new PropertyPanelRowExpanderCell(propertyPanel));
		
		addCollapseHandler(event -> removeWidget(event.getItem()));
		
		this.propertyPanel = propertyPanel;
	}
	
	/**
	 * Refreshes the expander given row.
	 */
	public void refreshRow(int rowIndex) {
		if (!grid.isRendered())
			return;
		
		collapseRow(rowIndex);
		expandRow(rowIndex);
	}
	
	private void addWidget(String key, Widget widget, int rowIndex) {
		Element rowElement = grid.getView().getRow(rowIndex);
		if (rowElement == null)
			return;
		
		if (widgets == null)
			widgets = new FastMap<>();
		
		boolean handleAttach = true;
		Widget oldWidget = widgets.get(key);
		if (oldWidget == widget)
			handleAttach = false;
		else
			ComponentHelper.doDetach(oldWidget);
		
		if (handleAttach) {
			widgets.put(key, widget);
			ComponentHelper.doAttach(widget);
		}
		
		XElement row = XElement.as(rowElement);

		// insert the element into the row expanding div id cell
		PropertyPanelRowExpanderCell cell = (PropertyPanelRowExpanderCell) getContentCell();
		Element item = row.select("#re_" + cell.getRowExpanderId() + "_" + key).getItem(0);
		if (item != null && !item.hasChildNodes())
			item.appendChild(widget.getElement());
	}
	
	protected void removeWidget(PropertyModel model) {
		String key = PropertyPanelGrid.props.normalizedPropertyName().getKey(model);
		if (widgets == null)
			return;
		
		Widget widget = widgets.remove(key);
		ComponentHelper.doDetach(widget);
	}
	
	@Override
	protected void collapseRow(XElement row) {
		super.collapseRow(row);
		
		Element flowExpanderSpan = DomQuery.selectNode("span." + PropertyPanelResources.INSTANCE.css().propertyNameFlowExpanderExpanded(), row);
		if (flowExpanderSpan == null)
			return;
		
		flowExpanderSpan.removeClassName(PropertyPanelResources.INSTANCE.css().propertyNameFlowExpanderExpanded());
		flowExpanderSpan.addClassName(PropertyPanelResources.INSTANCE.css().propertyNameFlowExpanderCollapsed());
		
		int idx = row.getPropertyInt("rowindex");
		propertyPanel.propertyPanelGrid.getStore().get(idx).setFlowExpanded(false);
	}
	
	@Override
	protected void expandRow(XElement row) {
		super.expandRow(row);
		
		Element flowExpanderSpan = DomQuery.selectNode("span." + PropertyPanelResources.INSTANCE.css().propertyNameFlowExpanderCollapsed(), row);
		if (flowExpanderSpan == null)
			return;
		
		flowExpanderSpan.removeClassName(PropertyPanelResources.INSTANCE.css().propertyNameFlowExpanderCollapsed());
		flowExpanderSpan.addClassName(PropertyPanelResources.INSTANCE.css().propertyNameFlowExpanderExpanded());
		
		int idx = row.getPropertyInt("rowindex");
		propertyPanel.propertyPanelGrid.getStore().get(idx).setFlowExpanded(true);
	}
	
	@Override
	protected void onMouseDown(RowClickEvent event) {
		Event e = event.getEvent();
	    XElement target = e.getEventTarget().cast();
	    String cls = target.getClassName();
	    int rowIndex = event.getRowIndex();
	    PropertyModel model = propertyPanel.propertyPanelGrid.getStore().get(event.getRowIndex());
	    
	    boolean isSpecial = false;
		for (String specialStyle : propertyPanel.specialUiElementsStyles) {
			if (cls.contains(specialStyle)) {
				isSpecial = true;
				break;
			}
		}
		
		int index = 0;
		int index2 = -1; //RVE need index which define nothing selected, if is 0 is still the first index at Collection
		if (cls.contains(PropertyPanel.FLOW_COLLECTION_INDEX)) {
			for (String style : cls.split(" ")) {
				if (style.contains(PropertyPanel.FLOW_COLLECTION_INDEX)) {
					index = Integer.parseInt(style.substring(PropertyPanel.FLOW_COLLECTION_INDEX.length()));
					index2 = index;
					break;
				}
			}
		}		
		
		MultiEditorGridInlineEditing<PropertyModel> gridInlineEditing = propertyPanel.propertyPanelGrid.gridInlineEditing;
	    
	    PropertyPanelCss css = PropertyPanelResources.INSTANCE.css();
		if (cls.contains(PropertyPanelCss.EXTERNAL_PROPERTY_FLOW_DISPLAY) && model.isEditable() && !model.getValueElementType().isCollection()) {
	    	if (cls.contains(css.propertyEntity()) && model.getValue() instanceof GenericEntity)
				propertyPanel.fireEntityPropertySelected(model);
	    	else
	    		gridInlineEditing.startEditing(new GridCell(event.getRowIndex(), PropertyPanel.VALUE_INDEX));
	    } else if (cls.contains(PropertyPanelCss.EXTERNAL_PROPERTY_VALUE_COLLECTION_ADD)) {
			if (gridInlineEditing.isEditing())
				gridInlineEditing.completeEditing();
			AddExistingEntitiesToCollectionAction addAction = propertyPanel.getAddExistingEntitiesToCollectionAction();
			addAction.updateState(propertyPanel.transformSelection(Collections.singletonList(PropertyPanel.getModelPath(model))));
			addAction.perform(null);
	    } else if (cls.contains(PropertyPanelCss.EXTERNAL_PROPERTY_VALUE_ENTITY_ASSIGN)) {
			if (gridInlineEditing.isEditing())
				gridInlineEditing.completeEditing();
			ChangeInstanceAction changeInstanceAction = propertyPanel.getShortcutChangeInstanceAction();
			changeInstanceAction.updateState(propertyPanel.transformSelection(Collections.singletonList(PropertyPanel.getModelPath(model))));
			changeInstanceAction.perform(null);
		} else if (cls.contains(PropertyPanelCss.EXTERNAL_FLOW_COLLECTION_ELEMENT) || cls.contains(css.flowMapKeyElement())
				|| cls.contains(css.flowMapValueElement())) {
			if (!cls.contains(PropertyPanelCss.EXTERNAL_FLOW_COLLECTION_ELEMENT))
				propertyPanel.fireMapElementSelected(index, cls.contains(css.flowMapKeyElement()), model);
			else {
				if (!cls.contains(PropertyPanel.FLOW_COLLECTION_INDEX))
					propertyPanel.fireCollectionPropertySelected(model);
				else {
					if (model.getValueElementType().isCollection()
							&& ((CollectionType) model.getValueElementType()).getCollectionElementType().getJavaType().equals(String.class)) {
						propertyPanel.fireStringPropertySelected(model, index2);
					} else
						propertyPanel.fireCollectionElementSelected(index, model);
				}
			}
		} else if (cls.contains(PropertyPanelCss.EXTERNAL_PROPERTY_MENU))
			PropertyPanelGrid.handlePropertyMenuClicked(e, model, propertyPanel);
		else if (cls.contains(css.propertyCollectionItemMenu()))
			PropertyPanelGrid.handlePropertyCollectionItemMenuClicked(e, model, propertyPanel, index);
		else if (!isSpecial && propertyPanel.checkPropertyNameFlow(target, css)) {
			if (model.getValueElementType().isEntity() && model.getValue() != null)
				propertyPanel.fireEntityPropertySelected(model);
			else if (model.getValueElementType().isCollection())
				propertyPanel.fireCollectionPropertySelected(model);
			else if (model.getValueElementType().getJavaType().equals(String.class)) 
				propertyPanel.fireStringPropertySelected(model, index2);
	    } else if (model.getValueElementType().getJavaType().equals(String.class) && !model.isEditable()) 
			propertyPanel.fireStringPropertySelected(model, index2);
	    else if (cls.contains(PropertyPanelCss.EXTERNAL_PROPERTY_VALUE_OPERATION))
	    	gridInlineEditing.startEditing(new GridCell(rowIndex, PropertyPanel.VALUE_INDEX));
		
		super.onMouseDown(event);
	}
	
	protected void expandAllRows() {
		if (!runTimer) {
			handleExpandAllRows();
			return;
		}
		
		new Timer() {
			@Override
			public void run() {
				runTimer = false;
				handleExpandAllRows();
			}
		}.schedule(10);
	}
	
	private void handleExpandAllRows() {
		Set<String> groupsToCollapse = new FastSet();
		for (int i = 0; i < propertyPanel.propertyPanelGrid.getStore().getAll().size(); i++) {
			PropertyModel propertyModel = propertyPanel.propertyPanelGrid.getStore().getAll().get(i);
			if (propertyModel.getFlow() || propertyPanel.isExtendedInlineFieldAvailable(propertyModel))
				expandRow(i);
			
			Group propertyGroup = propertyModel.getPropertyGroup();
			if (propertyGroup != null) {
				String groupName = propertyGroup.getLocalizedName() == null ? propertyGroup.getName() : I18nTools.getLocalized(propertyGroup.getLocalizedName());
				if (propertyGroup.getCollapsed() && !isGroupPresentInState(groupName))
					groupsToCollapse.add(groupName);
			}
		}
		
		boolean checkVBar = true;
		for (String groupToCollapse : groupsToCollapse)
			checkVBar = !((PropertyPanelGroupingView) propertyPanel.propertyPanelGrid.getView()).collapseGroup(groupToCollapse);
		
		if (checkVBar) //Needed for the Vertical bar to be displayed correctly when the PP is used within a dialog.
			((PropertyPanelGroupingView) propertyPanel.propertyPanelGrid.getView()).calculateVBar(false);
	}
	
	private boolean isGroupPresentInState(String groupName) {
		GridView<?> view = propertyPanel.propertyPanelGrid.getView();
		if (!(view instanceof PropertyPanelGroupingView))
			return false;
		
		return ((PropertyPanelGroupingView) view).isGroupInState(groupName);
	}

	protected static boolean isCollectionPropertyEditable(PropertyModel propertyModel, PropertyPanel propertyPanel) {
		if (propertyPanel.readOnly || !propertyModel.isEditable())
			return false;
		
		List<List<ModelPath>> modelPaths = propertyPanel.transformSelection(Collections.singletonList(PropertyPanel.getModelPath(propertyModel)));
		AddExistingEntitiesToCollectionAction addExistingEntitiesToCollectionAction = propertyPanel.getAddExistingEntitiesToCollectionAction();
		return addExistingEntitiesToCollectionAction.canAddForModelPaths(modelPaths);
		//addExistingEntitiesToCollectionAction.updateState(modelPaths);
		//return !addExistingEntitiesToCollectionAction.getHidden();
	}
	
	public static class PropertyPanelRowExpanderCell extends AbstractCell<PropertyModel> {
		
		private int rowExpanderId = 0;
		private PropertyPanel propertyPanel;
		
		public PropertyPanelRowExpanderCell(PropertyPanel propertyPanel) {
			rowExpanderId = lastId++;
			this.propertyPanel = propertyPanel;
		}

		@Override
		public void render(Context context, PropertyModel model, SafeHtmlBuilder sb) {
			boolean extendedInlineFieldAvailable = propertyPanel.isExtendedInlineFieldAvailable(model);
			boolean isCollectionNotEmpty = model.getValueElementType().isCollection() && !PropertyPanelGroupingView.isCollectionAndEmpty(model.getValue());
			if (extendedInlineFieldAvailable && !isCollectionNotEmpty) {
				prepareExtendedInlineFieldDisplay(context, sb);
				Scheduler.get()
						.scheduleDeferred(() -> propertyPanel.propertyPanelGrid.expander.addWidget(
								PropertyPanelGrid.props.normalizedPropertyName().getKey(model),
								model.getExtendedInlineField().getWidgetSupplier().get(), context.getIndex()));
				
				return;
			}
			
			if (!extendedInlineFieldAvailable && model.getExtendedInlineField() != null)
				propertyPanel.propertyPanelGrid.expander.removeWidget(model);
			
			String display = model.getFlowDisplay() != null && model.getFlowDisplay().isEmpty() ? "&nbsp;" : model.getFlowDisplay();
			Object value = model.getValue();
			GenericModelType type = model.getValueElementType();
			String css = ""; 
			boolean showOutline = ((propertyPanel.valueRendering != ValueRendering.none) && !(model.getValue() instanceof GenericEntity) && !(model.getValueElementType().isCollection()));			
			if (showOutline)
				css = PropertyPanelCss.EXTERNAL_PROPERTY_VALUE_WITH_GRID_LINES + " ";
			else if (!propertyPanel.readOnly && !propertyPanel.showGridLines && model.isEditable() && !type.isCollection())
				css = PropertyPanelResources.INSTANCE.css().editableBox() + " ";
			
			String iconHtml = null;
			if (type.isEntity())
				iconHtml = propertyPanel.propertyPanelGrid.getIconHtml((GenericEntity) value, (EntityType<GenericEntity>) type);
			
			StringBuilder html = new StringBuilder();
			html.append("<div class='").append(css).append(PropertyPanelCss.EXTERNAL_PROPERTY_FLOW_DISPLAY);
			if (propertyPanel.navigationEnabled && value instanceof GenericEntity
					&& !propertyPanel.specialFlowClasses.contains(((EntityType<?>) type).getJavaType())) {
				//html.append(" ").append(PropertyPanelCss.EXTERNAL_PROPERTY_FLOW_DISPLAY_LINE);
				html.append(" ").append(PropertyPanelResources.INSTANCE.css().propertyEntity()).append("'>");
			} else if (!type.isCollection())
				html.append("' style='height: 80px;'>");
			else
				html.append("'>");
			html.append("<div class='").append(css).append(PropertyPanelCss.EXTERNAL_PROPERTY_TEXT);
			if (!model.isEditable() && model.getValueElementType().getJavaType().equals(String.class))
				html.append(" ").append(PropertyPanelResources.INSTANCE.css().clickableElement());
			html.append("'>");			
			html.append(display);
			html.append("</div>");			
			
			html.append("<div class='").append(css).append(PropertyPanelCss.EXTERNAL_PROPERTY_ICONS).append("'>");			
			if (!model.isEditable()) {
				html.append("      <div ");
				html.append("class='").append(PropertyPanelCss.EXTERNAL_PROPERTY_VALUE_READ_ONLY).append("' ");
				html.append("style='width: 16px; height: 16px; float: right;' qtip='").append(LocalizedText.INSTANCE.readOnly()).append("'");
				html.append("></div>");
			}
			
			boolean showMenu = true;
			if (!propertyPanel.isSkipMetadataResolution() && propertyPanel.getMetaData() != null) {
				EntityMdResolver entityMdResolver;
				if (model.getParentEntity() == null)
					entityMdResolver = propertyPanel.getMetaData().lenient(true).entityType(model.getParentEntityType());
				else
					entityMdResolver = propertyPanel.getMetaData().lenient(true).entity(model.getParentEntity());
				HideDetailsActions hideDetailsActions = entityMdResolver.meta(HideDetailsActions.T).exclusive();
				showMenu = hideDetailsActions == null;
			}
			
			if (showMenu) {
				//RVE - show Menu inside the text area			
				html.append("      <div ");
				html.append("class='").append(PropertyPanelCss.EXTERNAL_PROPERTY_MENU).append("' ");
				html.append("style='width: 14px; height: 14px; float: right;");
				//html.append(model.getFlow() ? " padding-right: 13px;" : " padding-right: 9px;").append("'></td>\n");
				html.append(" padding-right: 9px;").append("'></div>");
			}
			html.append("</div>");			
			
			html.append("\n");
			
			boolean navigationEnabled = propertyPanel.navigationEnabled && value instanceof GenericEntity
					&& !propertyPanel.specialFlowClasses.contains(((EntityType<?>) type).getJavaType());
			
			boolean isClickableInsideTriggerField = PropertyPanelGrid.isClickableInsideTriggerField(model, propertyPanel);		
			//sb.appendHtmlConstant((PropertyPanelGrid.prepareMenuTable(html.toString(), null, iconHtml, model.getFlow(), type.isEntity(), navigationEnabled, null,
			//		false, false, false, !model.isEditable())));					
			sb.appendHtmlConstant((PropertyPanelGrid.prepareMenuTable(model, html.toString(), null, iconHtml, model.getFlow(), type.isEntity(), navigationEnabled, null,
					false, false, false, false, isClickableInsideTriggerField)));					
			
			GenericModelType propertyType = model.getValueElementType();
			if (!extendedInlineFieldAvailable && !model.isInline()
					&& (propertyType.isCollection() || isCollectionInitializer(model.getPropertyName(), model.getParentEntity()))
					&& isCollectionPropertyEditable(model, propertyPanel)) {
				sb.appendHtmlConstant("<div style='text-indent: 55px' class='"
						+ PropertyPanelCss.EXTERNAL_PROPERTY_VALUE_COLLECTION_ADD + "'>" + LocalizedText.INSTANCE.add() + "</div>");
			}
			sb.appendHtmlConstant("</div>");
			
			if (extendedInlineFieldAvailable) {
				prepareExtendedInlineFieldDisplay(context, sb);
				Scheduler.get()
						.scheduleDeferred(() -> propertyPanel.propertyPanelGrid.expander.addWidget(
								PropertyPanelGrid.props.normalizedPropertyName().getKey(model),
								model.getExtendedInlineField().getWidgetSupplier().get(), context.getIndex()));
			}
			
			if (!propertyPanel.isExtendedInlineFieldAvailable(model, true))
				return;
			
			new Timer() {
				@Override
				public void run() {
					Element rowElement = propertyPanel.propertyPanelGrid.getView().getRow(context.getIndex());
					if (rowElement == null)
						return;
					
					XElement row = XElement.as(rowElement);
					
					Element node = DomQuery.selectNode("div." + PropertyPanelCss.EXTERNAL_PROPERTY_FLOW_DISPLAY, row);
					if (node != null)
						model.getExtendedInlineField().prepareInlineElement(node);
				}
			}.schedule(1000);
		}
		
		public int getRowExpanderId() {
			return rowExpanderId;
		}
		
		// Blank cell to help identify where to insert the widget
		private void prepareExtendedInlineFieldDisplay(Context context, SafeHtmlBuilder sb) {
			sb.appendHtmlConstant("<div id=\"re_" + rowExpanderId + "_" +context.getKey() + "\"></div>");
		}
		
		private static boolean isCollectionInitializer(String propertyName, GenericEntity parentEntity) {
			return "initializer".equals(propertyName) && parentEntity instanceof GmProperty && ((GmProperty) parentEntity).getType() != null
					&& ((GmProperty) parentEntity).getType().isGmCollection();
		}
		
	}

}
