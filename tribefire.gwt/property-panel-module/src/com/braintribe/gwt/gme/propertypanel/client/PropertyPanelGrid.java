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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.ClickableInsideTriggerField;
import com.braintribe.gwt.gme.propertypanel.client.AbstractPropertyPanel.ValueRendering;
import com.braintribe.gwt.gme.propertypanel.client.field.SimplifiedEntityField;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelCss;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelResources;
import com.braintribe.gwt.gmview.action.client.ChangeInstanceAction;
import com.braintribe.gwt.gmview.client.GmInteractionListener;
import com.braintribe.gwt.gmview.client.GmMouseInteractionEvent;
import com.braintribe.gwt.gmview.client.IconAndType;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gmview.util.client.GMTypeInstanceBean;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ColumnConfigWithMaxWidth;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ListStoreWithStringKey;
import com.braintribe.gwt.gxt.gxtresources.extendedtrigger.client.ClickableTriggerField;
import com.braintribe.gwt.gxt.gxtresources.multieditor.client.MultiEditorGridInlineEditing;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.dataediting.api.GenericSnapshot;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.PropertyPathElement;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.proxy.AbstractProxyProperty;
import com.braintribe.model.generic.proxy.ProxyEnhancedEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.validation.ValidationKind;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.prompt.HideDetailsActions;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.data.shared.Converter;
import com.sencha.gxt.widget.core.client.form.IsField;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.tips.QuickTip;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;

/**
 * Implementation of the {@link Grid} used by the {@link PropertyPanel}.
 * @author michel.docouto
 *
 */
public class PropertyPanelGrid extends Grid<PropertyModel> {
	
	protected static PropertyModelProperties props = GWT.create(PropertyModelProperties.class);
	private static final String emptyStringImageString = AbstractImagePrototype.create(PropertyPanelResources.INSTANCE.nullIcon()).getHTML()
			.replaceFirst("style='", "qtip='" + LocalizedText.INSTANCE.empty() + "' style='");
	private static final String loadingImageString = AbstractImagePrototype.create(PropertyPanelResources.INSTANCE.loading()).getHTML()
			.replaceFirst("style='", "qtip='" + LocalizedText.INSTANCE.loadingAbsentProperty() + "' style='");
	
	protected MultiEditorGridInlineEditing<PropertyModel> gridInlineEditing;
	protected PropertyPanel propertyPanel;
	protected PropertyPanelRowExpander expander;
	private Object startValue;
	protected boolean editorsReady = false;
	protected boolean startEditingPending = false;
	
	public PropertyPanelGrid(final PropertyPanel propertyPanel, PropertyPanelRowExpander expander) {
		super(new ListStoreWithStringKey<>(props.normalizedPropertyName(), true), prepareColumnModel(propertyPanel, expander),
				new PropertyPanelGroupingView(propertyPanel));
		
		this.propertyPanel = propertyPanel;
		this.expander = expander;
		this.setBorders(false);
		this.addStyleName("gmePropertyPanel");
		this.setAllowTextSelection(true);
		this.setHideHeaders(true);
		
		prepareGridInlineEditing();
		expander.setHidden(true);
		expander.initPlugin(this);
		
		this.addCellClickHandler(event -> fireClickOrDoubleClick(true, new PropertyPanelMouseInteractionEvent(event, propertyPanel)));
		
		this.addCellDoubleClickHandler(event -> fireClickOrDoubleClick(false, new PropertyPanelMouseInteractionEvent(event, propertyPanel)));
		
		this.getSelectionModel().setLocked(true);
		
		getStore().setEnableFilters(true);
		
		QuickTip quickTip = new QuickTip(this);
		ToolTipConfig config = new ToolTipConfig();
		config.setMaxWidth(400);
		config.setDismissDelay(0);
		quickTip.update(config);
	}
	
	private static ColumnModel<PropertyModel> prepareColumnModel(PropertyPanel propertyPanel, PropertyPanelRowExpander expander) {
		ColumnConfig<PropertyModel, PropertyModel> displayNameColumn = prepareDisplayNameColumn(propertyPanel);
		ColumnConfig<PropertyModel, Object> valueColumn = prepareValueColumn(propertyPanel);
		
		propertyPanel.groupColumn = new ColumnConfig<>(new ValueProvider<PropertyModel, String>() {
			@Override
			public String getValue(PropertyModel object) {
				String groupIcon = object.getGroupIcon();
				if (groupIcon == null)
					return object.getGroupName();
				
				return "<img class='propertyGroupIcon' src='" + groupIcon + "' width='16px' height='16px' />" + object.getGroupName();
			}

			@Override
			public void setValue(PropertyModel object, String value) {
				object.setGroupName(value);
			}

			@Override
			public String getPath() {
				return "groupName";
			}
		});
		propertyPanel.groupColumn.setCellPadding(false);
		propertyPanel.groupColumn.setHidden(true);
		propertyPanel.groupColumn.setCell(new AbstractCell<String>() {
			@Override
			public void render(Context context, String value, SafeHtmlBuilder sb) {
				return;
			}
		});
		
		ColumnModel<PropertyModel> cm = new ColumnModel<>(Arrays.asList(expander, displayNameColumn, valueColumn, propertyPanel.groupColumn));
		return cm;
	}
	
	private static ColumnConfig<PropertyModel, PropertyModel> prepareDisplayNameColumn(PropertyPanel propertyPanel) {
		ColumnConfigWithMaxWidth<PropertyModel, PropertyModel> displayNameColumn = new ColumnConfigWithMaxWidth<>(new IdentityValueProvider<>(),
				propertyPanel.propertyNameColumnWidth);
		displayNameColumn.setCellPadding(false);
		displayNameColumn.setCell(new AbstractCell<PropertyModel>("click") {
			
			private boolean handlesSelection = false;
			
			@Override
			public void render(com.google.gwt.cell.client.Cell.Context context, PropertyModel model, SafeHtmlBuilder sb) {
				if (model.isHideLabel())
					return;
				
				StringBuilder html = new StringBuilder();
				html.append("<div class='").append(GMEUtil.PROPERTY_NAME_CSS).append(" ")
						.append(PropertyPanelResources.INSTANCE.css().propertyName());
				
				if (model.getFlow()) {
					html.append(" ").append(PropertyPanelResources.INSTANCE.css().propertyNameFlow());
					Object value = model.getValue();
					if ((value instanceof GenericEntity
							&& !propertyPanel.specialFlowClasses.contains(((EntityType<?>) model.getValueElementType()).getJavaType()))
							|| (value != null && model.getValueElementType().isCollection())
							|| ((model.getValueElementType()).getJavaType().equals(String.class))) {
						if (propertyPanel.navigationEnabled)
							html.append(" ").append(PropertyPanelResources.INSTANCE.css().propertyNameFlowLink());
					}
				} else if (context.getIndex() == 0 || propertyPanel.propertyPanelGrid.getStore().get(context.getIndex() - 1).getFlow())
					html.append(" ").append(PropertyPanelResources.INSTANCE.css().propertyNameFirst());
				
				if (model.getMandatory())
					html.append(" ").append(PropertyPanelResources.INSTANCE.css().propertyNameMandatory());
				
				if (!model.isEditable())
					html.append(" ").append(PropertyPanelResources.INSTANCE.css().propertyNameReadOnly());
				
				html.append("'>");
				
				String icon = propertyPanel.propertyPanelGrid.getIconHtml(model);
				if (model.getFlow()) {
					StringBuilder span = new StringBuilder();
					span.append("<span class='propertyNameExpander ")
							.append(model.getFlowExpanded() ? PropertyPanelResources.INSTANCE.css().propertyNameFlowExpanderExpanded()
									: PropertyPanelResources.INSTANCE.css().propertyNameFlowExpanderCollapsed());
					span.append("'></span>");
					
					if (icon != null)
						span.append(icon);
					span.append(model.getDisplayName());
					
					boolean isClickableInsideTriggerField = isClickableInsideTriggerField(model, propertyPanel);
					//In case we are showing an inlined collection, the trigger icon will directly come from the editor
					String triggerClickIcon = model.isInline() && model.getValueElementType().isCollection() ? null
							: getTriggerClickIcon(model, propertyPanel);
					html.append(prepareMenuTable(model, span.toString(),
							prepareDescription(model.getDescription(), model.isBaseTyped(), model.getValue()), null, true, false, false,
							triggerClickIcon, false, model.getValue() == null, true, false, isClickableInsideTriggerField)).append("</div>");
				} else {
					html.append("<span style='margin-left: 11px;'");
					if (model.getDescription() != null) {
						html.append(" qtip='")
								.append(SafeHtmlUtils.htmlEscape(prepareDescription(model.getDescription(), model.isBaseTyped(), model.getValue())))
								.append("'");
					}
					html.append(">");
					if (icon != null)
						html.append(icon);
					html.append(model.getDisplayName()).append("</span></div>");
				}
				
				sb.appendHtmlConstant(html.toString());
			}
			
			private String prepareDescription(String description, boolean isBaseType, Object value) {
				if (!isBaseType || value == null)
					return description;
				
				GenericModelType actualType = GMF.getTypeReflection().getBaseType().getActualType(value);
				if (actualType.isBase())
					return description;
				
				StringBuilder fullDescription = new StringBuilder();
				if (description != null)
					fullDescription.append(description);
				fullDescription.append(" (");
				if (EnumReference.T.equals(actualType)) {
					EnumReference enumReference = (EnumReference) value;
					String typeSignature = enumReference.getTypeSignature();
					if (typeSignature.contains("."))
						typeSignature = typeSignature.substring(typeSignature.lastIndexOf(".") + 1);
					fullDescription.append("EnumReference<").append(typeSignature).append(">)");
				} else if (EntityReference.T.equals(actualType)) {
					EntityReference entityReference = (EntityReference) value;
					String typeSignature = entityReference.getTypeSignature();
					if (typeSignature.contains("."))
						typeSignature = typeSignature.substring(typeSignature.lastIndexOf(".") + 1);
					fullDescription.append("EntityReference<").append(typeSignature).append(">)");
				} else
					fullDescription.append(actualType.getTypeName()).append(")");
				return SafeHtmlUtils.htmlEscape(fullDescription.toString());
			}
			
			@Override
			public void onBrowserEvent(com.google.gwt.cell.client.Cell.Context context, com.google.gwt.dom.client.Element parent, PropertyModel model,
					NativeEvent event, ValueUpdater<PropertyModel> valueUpdater) {
				handlesSelection = false;
				EventTarget eventTarget = event.getEventTarget();
				if (!Element.is(eventTarget))
					return;
				
				String cls = Element.as(eventTarget).getClassName();
				boolean isSpecial = propertyPanel.specialUiElementsStyles.stream().filter(s -> cls.contains(s)).findAny().isPresent();
				
				if (isSpecial || isBooleanProperty(model)) {
					event.stopPropagation();
					event.preventDefault();
					handlesSelection = true;
				} else
					super.onBrowserEvent(context, parent, model, event, valueUpdater);
				handleColumnClick(cls, isSpecial, context, parent, model, event, propertyPanel);
			}
			
			@Override
			public boolean handlesSelection() {
				return handlesSelection;
			}
		});
		displayNameColumn.setMaxWidth(propertyPanel.propertyNameColumnWidth + 150);
		
		return displayNameColumn;
	}
	
	protected static String prepareMenuTable(PropertyModel model, String display, String description, String icon, boolean isFlow, boolean isEntity,
			boolean navigationEnabled, String clickableTriggerIcon, boolean showMenu, boolean isNull, boolean isPropertyName, boolean showReadOnly,
			boolean isClickableInsideTriggerField) {
		StringBuilder builder = new StringBuilder();
		builder.append("<table class='").append(PropertyPanelResources.INSTANCE.css().inheritFont()).append(" ");
		builder.append(PropertyPanelResources.INSTANCE.css().tableFixedLayout()).append(" ");		
		builder.append("' border='0' cellpadding='2' cellspacing='0'>\n");
		builder.append("   <tr class='").append(PropertyPanelResources.INSTANCE.css().inheritFont()).append("'>\n");
		
		if (icon != null && !isFlow)
			builder.append("      <td class='gxtReset' width='14px'>").append(icon).append("&nbsp;</td>\n");
		
		StringBuilder clickableTriggerIconString = null;
		if (clickableTriggerIcon != null) {
			clickableTriggerIconString = new StringBuilder();
			clickableTriggerIconString.append("      <td width='18px' class='gxtReset propertyPanelClickableTrigger ")
					.append(PropertyPanelResources.INSTANCE.css().clickableTrigger()).append("' style='background-image: url(")
					.append(clickableTriggerIcon).append(")'></td>\n");
		}
		
		if (clickableTriggerIconString != null && isNull)
			builder.append(clickableTriggerIconString.toString());
		
		String placeholder = model.getPlaceHolder();
		boolean usePlaceholder = isNull && !model.getAbsent() && placeholder != null && model.isUsePlaceholder();
		
		builder.append("      <td ");
		if (description != null || (placeholder != null && !usePlaceholder)) {
			String qtip = placeholder != null && !usePlaceholder ? placeholder : description;
			builder.append("qtip='").append(SafeHtmlUtils.htmlEscape(qtip)).append("' ");
		}
		builder.append("class='gxtReset ").append(PropertyPanelResources.INSTANCE.css().inheritFont()).append(" ")
				.append(PropertyPanelResources.INSTANCE.css().textOverflowNoWrap());
		if (isPropertyName)
			builder.append(" ").append(PropertyPanelResources.INSTANCE.css().propertyNameGroup());		
		if (navigationEnabled)
			builder.append(" ").append(PropertyPanelResources.INSTANCE.css().clickableElement());
		if (isClickableInsideTriggerField)
			builder.append(" ").append(PropertyPanelResources.INSTANCE.css().clickableInsideTrigger());				
		builder.append("' width='100%'>");
		
		if (usePlaceholder) {
			builder.append("<span class='").append(PropertyPanelCss.EXTERNAL_PROPERTY_VALUE_PLACEHOLDER).append("'>");
			builder.append(model.getPlaceHolder());
			builder.append("</span>");
		} else {
			if (isEntity && !display.isEmpty())
				builder.append("<span class='").append(PropertyPanelResources.INSTANCE.css().propertyEntity()).append("'>");

			builder.append(display);
			if (isEntity && !display.isEmpty())
				builder.append("</span>");
		}
		builder.append("</td>\n");
		
		if (clickableTriggerIconString != null && !isNull)
			builder.append(clickableTriggerIconString.toString());

		if (showReadOnly) {
			builder.append("      <td width='16px' ");
			builder.append("class='gxtReset ").append(PropertyPanelCss.EXTERNAL_PROPERTY_VALUE_READ_ONLY).append("' ");
			builder.append("style='height: 16px;' qtip='").append(LocalizedText.INSTANCE.readOnly()).append("'");
			builder.append("></td>\n");
		}		
		
		if (showMenu) {												
			builder.append("      <td width='14px' ");
			builder.append("class='gxtReset ").append(PropertyPanelCss.EXTERNAL_PROPERTY_MENU).append("' ");
			builder.append("style='height: 14px;");
			builder.append(isFlow ? " padding-right: 13px;" : " padding-right: 9px;").append("'></td>\n");
		}
		
		builder.append("   </tr>\n</table>");
		return builder.toString();
	}
	
	protected void startEditing() {
		if (!editorsReady) {
			startEditingPending = true;
			return;
		}
		
		startEditingPending = false;
		int firstEditableRow = getFirstEditableRow(true);
		
		//RVE - set Scheduler.get().scheduleDeferred instead of Timer.schedule(1000) -> faster show/edit
		//was needed for TepmlateGIMADialog (for example Create New Model Action)
		Scheduler.get().scheduleDeferred(() -> Scheduler.get().scheduleDeferred(() -> {
			if (firstEditableRow == -1)
				propertyPanel.focus();
			else {
				gridInlineEditing.ignoreClickableTriggerFieldTrigger();
				gridInlineEditing.startEditing(new GridCell(firstEditableRow, PropertyPanel.VALUE_INDEX));
			}
		}));
	}
	
	protected int getFirstEditableRow() {
		return getFirstEditableRow(false);
	}
	
	private int getFirstEditableRow(boolean checkSimplifiedEditor) {
		if (propertyPanel.readOnly || getStore().getAll().isEmpty())
			return -1;
		
		int rowIndex = 0;
		for (PropertyModel model : getStore().getAll()) {
			if (model.isEditable()) {
				Object editor = gridInlineEditing.getEditor(getColumnModel().getColumn(PropertyPanel.VALUE_INDEX), rowIndex);
				if (editor != null && (checkSimplifiedEditor ? !(editor instanceof SimplifiedEntityField) : true))
					return rowIndex;
			}
			rowIndex++;
		}
		
		return -1;
	}
	
	protected String getIconHtml(GenericEntity entity, EntityType<GenericEntity> entityType) {
		ModelPath modelPath = new ModelPath();
		modelPath.add(new RootPathElement(entityType, entity));
		ImageResource icon = null;
		IconAndType iconAndType = propertyPanel.iconProvider.apply(modelPath);
		if (iconAndType != null) {
			icon = iconAndType.getIcon();
			if (icon != null)
				return AbstractImagePrototype.create(icon).getHTML().replaceFirst("style='", "style='margin-right: 6px !important; height: 14px;");
		}
		
		return null;
	}
	
	protected String getIconHtml(PropertyModel propertyModel) {
		if (propertyModel.getVirtualEnum() != null)
			return null;
		
		IconAndType iconAndType = propertyPanel.iconProvider.apply(PropertyPanel.getModelPath(propertyModel));
		if (iconAndType != null && !iconAndType.isEntityIcon()) {
			ImageResource icon = iconAndType.getIcon();
			if (icon != null)
				return AbstractImagePrototype.create(icon).getHTML().replaceFirst("style='", "style='margin-right: 3px !important; height: 14px;");
		}
		
		return null;
	}
	
	public GridCell getActiveCell() {
		return gridInlineEditing.getActiveCell();
	}
	
	public void markAsFinishedByEnter() {
		gridInlineEditing.markAsFinishedByEnter();
	}
	
	public void cancelEditing() {
		if (gridInlineEditing.isEditing())
			gridInlineEditing.cancelEditing();
	}
	
	protected void completeEditing() {
		if (gridInlineEditing.isEditing())
			gridInlineEditing.completeEditing();
	}
	
	protected void finishEditing() {
		if (gridInlineEditing.isEditing())
			gridInlineEditing.getEditor(getColumnModel().getColumn(gridInlineEditing.getActiveCell().getCol())).finishEditing();
	}
	
	protected int getContentHeight() {
		if (getStore().getAll().isEmpty())
			return 0;
		
		int height = 0;
		
		XElement viewEl = getView().getScroller();
		if (viewEl != null)
			height = viewEl.getScrollHeight();
		
		return height;
	}
	
	private static void handleColumnClick(String cls, boolean isSpecial, com.google.gwt.cell.client.Cell.Context context,
			com.google.gwt.dom.client.Element parent, PropertyModel model, NativeEvent event, PropertyPanel propertyPanel) {
		PropertyPanelCss css = PropertyPanelResources.INSTANCE.css();
		GenericModelType valueElementType = model.getValueElementType();
		PropertyPanelGrid grid = propertyPanel.propertyPanelGrid;
		MultiEditorGridInlineEditing<PropertyModel> gridInlineEditing = grid.gridInlineEditing;
		if ((cls.contains(PropertyPanelCss.EXTERNAL_PROPERTY_FLOW_DISPLAY) || cls.contains(css.propertyEntity())) && valueElementType.isEntity()
				&& model.getValue() != null && !propertyPanel.specialFlowClasses.contains(((EntityType<?>) valueElementType).getJavaType())) {				
			if (cls.contains(css.clickableInsideTrigger())) {
				ClickableInsideTriggerField editor = (ClickableInsideTriggerField) gridInlineEditing
						.getEditor(grid.getColumnModel().getColumn(PropertyPanel.VALUE_INDEX), context.getIndex());
				if (editor.canFireTrigger()) {
					editor.fireTriggerClick(event);
					return;
				}
			}
			Scheduler.get().scheduleDeferred(() -> Scheduler.get().scheduleDeferred(() -> {
				if (gridInlineEditing.isEditing())
					gridInlineEditing.cancelEditing();
			}));
			propertyPanel.fireEntityPropertySelected(model);
		} else {
			if (cls.contains(PropertyPanelCss.EXTERNAL_PROPERTY_FLOW_DISPLAY) && model.isEditable() && !(valueElementType.isCollection())) {
				gridInlineEditing.startEditing(new GridCell(context.getIndex(), PropertyPanel.VALUE_INDEX));
			} else if (cls.contains(PropertyPanelCss.EXTERNAL_PROPERTY_VALUE_ENTITY_ASSIGN)) {
				if (gridInlineEditing.isEditing())
					gridInlineEditing.completeEditing();
				ChangeInstanceAction changeInstanceAction = propertyPanel.getShortcutChangeInstanceAction();
				changeInstanceAction.updateState(propertyPanel.transformSelection(Collections.singletonList(PropertyPanel.getModelPath(model))));
				changeInstanceAction.perform(null);
			} else if (cls.contains(css.propertyNameFlowExpanderCollapsed())) {
				PropertyPanel.clearSelection();
				grid.expander.expandRow(context.getIndex());
			} else if (cls.contains(css.propertyNameFlowExpanderExpanded())) {
				PropertyPanel.clearSelection();
				grid.expander.collapseRow(context.getIndex());
			} else if (cls.contains(PropertyPanelCss.EXTERNAL_PROPERTY_MENU)) {
				handlePropertyMenuClicked(event, grid.getStore().get(context.getIndex()), propertyPanel);
			} else if (cls.contains(css.propertyCollectionItemMenu())) {
				handlePropertyCollectionItemMenuClicked(event, grid.getStore().get(context.getIndex()), propertyPanel, context.getIndex());
			} else if (cls.contains(css.clickableTrigger())) {
				gridInlineEditing.startEditing(new GridCell(context.getIndex(), PropertyPanel.VALUE_INDEX));
				/*Scheduler.get().scheduleDeferred(() -> {
					ClickableTriggerField editor = (ClickableTriggerField) gridInlineEditing
							.getEditor(grid.getColumnModel().getColumn(PropertyPanel.VALUE_INDEX), context.getIndex());
					editor.fireTriggerClick(event);
				});*/
			} else if (cls.contains(css.checkedValue()) || cls.contains(css.uncheckedValue()) || cls.contains(css.checkNullValue())) {
				if (!propertyPanel.readOnly && model.isEditable()) {
					propertyPanel.localManipulation = true;
					Boolean startValue = (Boolean) model.getValue();
					boolean newValue = startValue == null ? true : !startValue;
					if (model.getParentEntity() == null)
						propertyPanel.instantiateEmbeddedParent(model);
					model.getParentEntityType().getProperty(model.getPropertyName()).set(model.getParentEntity(), newValue);
				}
			} else if (!isSpecial && propertyPanel.checkPropertyNameFlow(parent, css)) {
				if (valueElementType.isEntity() && model.getValue() != null)
					propertyPanel.fireEntityPropertySelected(model);
				else if (valueElementType.isCollection())
					propertyPanel.fireCollectionPropertySelected(model);
			}
		}
	}
	
	protected static void handlePropertyMenuClicked(NativeEvent event, PropertyModel model, PropertyPanel propertyPanel) {
		propertyPanel.setHelperMenuPropertyModel(model);
		propertyPanel.selectedModelPath = PropertyPanel.getModelPath(propertyPanel.getHelperMenuPropertyModel());
		if (propertyPanel.actionManager != null)
			propertyPanel.actionManager.onSelectionChanged(propertyPanel);
		
		hidePropertyPanelMenus(propertyPanel);
		Menu helperMenu = propertyPanel.getHelperMenu(true);
		Scheduler.get().scheduleDeferred(() -> helperMenu.showAt(event.getClientX(), event.getClientY()));
	}

	protected static void handlePropertyCollectionItemMenuClicked(NativeEvent event, PropertyModel model, PropertyPanel propertyPanel, int index) {
		propertyPanel.setCollectionItemMenuPropertyModel(model);
		propertyPanel.selectedModelPath = PropertyPanel.getModelPath(propertyPanel.getCollectionItemMenuPropertyModel());
		if (propertyPanel.actionManager != null)
			propertyPanel.actionManager.onSelectionChanged(propertyPanel);
		
		hidePropertyPanelMenus(propertyPanel);
		Menu menu = propertyPanel.getCollectionItemMenu(true);
		menu.setData("index", index);
		Scheduler.get().scheduleDeferred(() -> menu.showAt(event.getClientX(), event.getClientY()));
	}

	private static void hidePropertyPanelMenus(PropertyPanel propertyPanel) {
		//Hiding the previous menu before displaying a new one is needed due to some flickering affecting the client
		Menu menu = propertyPanel.getHelperMenu(false);
		if (menu != null)
			menu.hide();
		menu = propertyPanel.getCollectionItemMenu(false); 
		if (menu != null)
			menu.hide();
	}
	
	private static ColumnConfig<PropertyModel, Object> prepareValueColumn(PropertyPanel propertyPanel) {
		ColumnConfig<PropertyModel, Object> valueColumn = new ColumnConfig<>(props.value(), propertyPanel.propertyNameColumnWidth + 100);
		valueColumn.setCellPadding(false);
		valueColumn.setCell(new AbstractCell<Object>("click") {
			
			private boolean handlesSelection = false;
			
			@Override
			public void render(com.google.gwt.cell.client.Cell.Context context, Object value, SafeHtmlBuilder sb) {
				PropertyModel model = propertyPanel.propertyPanelGrid.getStore().get(context.getIndex());
								
				String groupClass = "gmePropertyValueGroup";
				if (model.getValidationKind().equals(ValidationKind.fail))
					groupClass = groupClass + " gmePropertyValidationFail";
				else if (model.getValidationKind().equals(ValidationKind.info))
					groupClass = groupClass + " gmePropertyValidationInfo"; 
				
				sb.appendHtmlConstant("<div class='" + groupClass +"'>");				
				if (propertyPanel.isExtendedInlineFieldAvailable(model, true)) {
					new Timer() {
						@Override
						public void run() {
							Element cell = propertyPanel.propertyPanelGrid.getView().getCell(context.getIndex(), context.getColumn());
							if (cell != null)
								model.getExtendedInlineField().prepareInlineElement(cell);
						}
					}.schedule(1000);
				}
				
				if (propertyPanel.valueRendering != ValueRendering.none) {
					renderValuesWithGridLines(context, sb, (propertyPanel.valueRendering == ValueRendering.gridlinesForEmptyValues));
					prepareValidationIcon(sb, model, context.getIndex());
					sb.appendHtmlConstant("</div>");
					return;
				}
				
				GenericModelType type = model.getValueElementType();
				Object modelValue = model.getValue();
				boolean isEmptyOrNull = (type.isSimple() || type.getJavaType() == LocalizedString.class) && !isBooleanProperty(model)
						&& (modelValue == null || (modelValue instanceof String && ((String) modelValue).isEmpty()));
				String editableCss = PropertyPanelResources.INSTANCE.css().editableBox() + " ";
				if (isEmptyOrNull || (model.getFlow() && model.getValueElementType().isCollection() && model.isInline()))
					editableCss += PropertyPanelResources.INSTANCE.css().emptyValue() + " ";
				
				String css = !propertyPanel.readOnly && !propertyPanel.showGridLines && model.isEditable() ? editableCss : "";
				css += GMEUtil.PROPERTY_VALUE_CSS + " ";
				
				if (model.getFlow()) {
					if (model.getValueElementType().isCollection() && model.isInline()) {
						sb.appendHtmlConstant("<div class='" + css
								+ (context.getIndex() == 0 || propertyPanel.propertyPanelGrid.getStore().get(context.getIndex() - 1).getFlow()
										? PropertyPanelResources.INSTANCE.css().propertyValueFirst()
										: PropertyPanelResources.INSTANCE.css().propertyValue())
								+ "'>" + prepareValueRendererString(model, model.getValueDisplay(), true, propertyPanel) + "</div>");
					} else {
						sb.appendHtmlConstant("<div class='" + PropertyPanelResources.INSTANCE.css().propertyValueFlow() + "'>"
								+ prepareValueRendererString(model, "", true, propertyPanel) + "</div>");
					}
					prepareValidationIcon(sb, model, context.getIndex());
					sb.appendHtmlConstant("</div>");
					
					return;
				}
				
				sb.appendHtmlConstant("<div class='" + css
						+ (context.getIndex() == 0 || propertyPanel.propertyPanelGrid.getStore().get(context.getIndex() - 1).getFlow()
								? PropertyPanelResources.INSTANCE.css().propertyValueFirst()
								: PropertyPanelResources.INSTANCE.css().propertyValue())
						+ "'>" + prepareValueRendererString(model, model.getValueDisplay(), false, propertyPanel) + "</div>");
				prepareValidationIcon(sb, model, context.getIndex());
				sb.appendHtmlConstant("</div>");
			}
			
			private void renderValuesWithGridLines(com.google.gwt.cell.client.Cell.Context context, SafeHtmlBuilder sb, boolean onlyEmptyValues) {
				PropertyModel model = propertyPanel.propertyPanelGrid.getStore().get(context.getIndex());
				if (model.getFlow()) {
					String css = PropertyPanelResources.INSTANCE.css().propertyValueFlow() + " ";
					if (model.isInline() && model.getValueElementType().isCollection())
						css += PropertyPanelCss.EXTERNAL_PROPERTY_VALUE_WITH_GRID_LINES;
					
					sb.appendHtmlConstant("<div class='" + css + "'>" + prepareValueRendererString(model, "&nbsp;", true, propertyPanel) + "</div>");
					return;
				}
				
				boolean isNotEntity = !model.getValueElementType().isEntity();
				boolean isNotBoolean = !isBooleanProperty(model);
				boolean isEmpty = model.getValue() == null;
				boolean render = isNotEntity && isNotBoolean;
				
				if (render && onlyEmptyValues)
					render = isEmpty;
				
				String css = "";
				if (render)
					css += PropertyPanelCss.EXTERNAL_PROPERTY_VALUE_WITH_GRID_LINES + " ";
				else
					css += PropertyPanelResources.INSTANCE.css().propertyValue() + " ";
				
				css += GMEUtil.PROPERTY_VALUE_CSS;// + " " + PropertyPanelResources.INSTANCE.css().propertyValue();
				sb.appendHtmlConstant(
						"<div class='" + css + "'>" + prepareValueRendererString(model, model.getValueDisplay(), false, propertyPanel) + "</div>");
			}
			
			@Override
			public void onBrowserEvent(com.google.gwt.cell.client.Cell.Context context, Element parent, Object value, NativeEvent event,
					ValueUpdater<Object> valueUpdater) {
				handlesSelection = false;
				EventTarget eventTarget = event.getEventTarget();
				if (!Element.is(eventTarget))
					return;
				
				String cls = Element.as(eventTarget).getClassName();
				boolean isSpecial = propertyPanel.specialUiElementsStyles.stream().filter(s -> cls.contains(s)).findAny().isPresent();
				
				PropertyModel model = propertyPanel.propertyPanelGrid.getStore().get(context.getIndex());
				
				if (isSpecial || isBooleanProperty(model)) {
					event.stopPropagation();
					event.preventDefault();
					handlesSelection = true;
				} else
					super.onBrowserEvent(context, parent, model, event, valueUpdater);
				
				handleColumnClick(cls, isSpecial, context, parent, model, event, propertyPanel);
			}
			
			@Override
			public boolean handlesSelection() {
				return handlesSelection;
			}
		});
		
		return valueColumn;
	}
	
	protected static void prepareValidationIcon(SafeHtmlBuilder sb, PropertyModel model, int index) {
		if (model.getValidationKind().equals(ValidationKind.fail)) {
			sb.appendHtmlConstant("<img src='" + PropertyPanelResources.INSTANCE.exclamation().getSafeUri().asString()
					+ "' id='propertyValidationIcon_" + index + "' class='failPropertyValidationIcon' style='width:16px;height:16px;'/>");
		} else if (model.getValidationKind().equals(ValidationKind.info)) {
			sb.appendHtmlConstant("<img src='" + PropertyPanelResources.INSTANCE.info().getSafeUri().asString() + "' id='propertyValidationIcon_"
					+ index + "' class='infoPropertyValidationIcon' style='width:16px;height:16px;'/>");
		}
	}

	private static String prepareValueRendererString(PropertyModel model, String displayValue, boolean isFlow, PropertyPanel propertyPanel) {
		String valueIcon = null;
		if (model.getValue() == null) {
			if (model.getAbsent())
				valueIcon = loadingImageString;
		} else if (!isFlow && model.getValue() instanceof String && ((String) model.getValue()).isEmpty())
			valueIcon = emptyStringImageString;
		
		String description = displayValue;
		if (displayValue.contains("<")) //is HTML
			description = null;
		
		boolean isEntity = model.getValue() instanceof GenericEntity
				&& !propertyPanel.specialFlowClasses.contains(model.getValueElementType().getJavaType());
		
		boolean isNull = model.isInline() && model.getValueElementType().isCollection() ? true : model.getValue() == null;
		
		boolean isClickableInsideTriggerField = isClickableInsideTriggerField(model, propertyPanel);
		
		boolean isMenuAvailable = isMenuAvailable(model, propertyPanel.getMetaData().useCase(propertyPanel.getUseCase()), isFlow);
		
		return prepareMenuTable(model, displayValue, description, valueIcon, isFlow, isEntity, propertyPanel.navigationEnabled,
				getTriggerClickIcon(model, propertyPanel), isMenuAvailable, isNull, false, !isFlow && !model.isEditable(),
				isClickableInsideTriggerField);
	}
	
	private static String getTriggerClickIcon(PropertyModel model, PropertyPanel propertyPanel) {
		if (propertyPanel.readOnly || !model.isEditable())
			return null;
		
		PropertyPanelGrid grid = propertyPanel.propertyPanelGrid;
		int row = grid.getStore().indexOf(model);
		IsField<?> editor = grid.gridInlineEditing.getEditor(grid.getColumnModel().getColumn(PropertyPanel.VALUE_INDEX), row);
		
		if (editor instanceof ClickableTriggerField)
			return ((ClickableTriggerField) editor).getImageResource().getSafeUri().asString();
			
		return null;
	}
	
	public static boolean isClickableInsideTriggerField(PropertyModel model, PropertyPanel propertyPanel) {
		PropertyPanelGrid grid = propertyPanel.propertyPanelGrid;
		int row = grid.getStore().indexOf(model);
		IsField<?> editor = grid.gridInlineEditing.getEditor(grid.getColumnModel().getColumn(PropertyPanel.VALUE_INDEX), row);
		
		if (editor instanceof ClickableInsideTriggerField)
		   return true;
		
		return false;
	}
	
	private static boolean isMenuAvailable(PropertyModel model, ModelMdResolver modelMdResolver, boolean isFlow) {
		if (isFlow/* || isBooleanProperty(model)*/)
			return false;
		
		if (modelMdResolver == null)
			return true;
		
		EntityMdResolver entityMdResolver = null;
		if (model.getParentEntity() != null)
			entityMdResolver = modelMdResolver.lenient(true).entity(model.getParentEntity());
		else if (model.getParentEntityType() != null)
			entityMdResolver = modelMdResolver.lenient(true).entityType(model.getParentEntityType());
		
		if (entityMdResolver == null)
			return true;
		
		return entityMdResolver.meta(HideDetailsActions.T).exclusive() == null;
	}
	
	private void prepareGridInlineEditing() {
		gridInlineEditing = new MultiEditorGridInlineEditing<>(this);
		
		if (propertyPanel.readOnly)
			return;
		
		gridInlineEditing.addBeforeStartEditHandler(event -> {
			gridInlineEditing.setUseDialogSettings(propertyPanel.getUseDialogSettings());
			propertyPanel.rollbackTransaction();
			propertyPanel.gmEditionViewControllerSupplier.get().registerAsCurrentEditionView(propertyPanel);
			propertyPanel.editionNestedTransaction = propertyPanel.gmSession.getTransaction().beginNestedTransaction();
			startValue = PropertyPanelGrid.this.getStore().get(event.getEditCell().getRow()).getValue();
		});
		
		gridInlineEditing.addCompleteEditHandler(event -> {
			GridCell editCell = event.getEditCell();
			IsField<?> editor = event.getSource().getEditor(PropertyPanelGrid.this.getColumnModel().getColumn(editCell.getCol()));
			Object value;
			if (editor instanceof ValueBaseField)
				value = ((ValueBaseField<?>) editor).getCurrentValue();
			else
				value = editor.getValue();
			
			Converter<Object, Object> converter = event.getSource().getConverter(getColumnModel().getColumn(editCell.getCol()));
			if (converter != null)
				value = converter.convertFieldValue(value);
			
			handleCompleteEdit(value, editCell, editor);
		});
		
		gridInlineEditing.addCancelEditHandler(event -> {
			propertyPanel.rollbackTransaction();
			propertyPanel.fireEditingDone(true);
		});
	}
	
	public IsField<?> getEditor(int col) {
		return gridInlineEditing.getEditor(PropertyPanelGrid.this.getColumnModel().getColumn(col));
	}

	/**
	 * Handles the completion of an edition.
	 */
	public void handleCompleteEdit(Object value, GridCell editCell, IsField<?> editor) {
		if (propertyPanel.editionNestedTransaction == null)
			propertyPanel.editionNestedTransaction = propertyPanel.gmSession.getTransaction().beginNestedTransaction();
		
		PropertyModel model = PropertyPanelGrid.this.getStore().get(editCell.getRow());
		new Timer() {
			@Override
			public void run() {
				PropertyPanelGrid.this.getStore().rejectChanges();
				propertyPanel.fireEditingDone(false);
			}
		}.schedule(500);
		
		String propertyName = model.getPropertyName();
		Property property = model.getParentEntityType().getProperty(propertyName);
		if (model.getParentEntity() == null)
			propertyPanel.instantiateEmbeddedParent(model);
		GenericEntity parentEntity = model.getParentEntity();
		Object oldValue = property.get(parentEntity);
		
		boolean isHandlingCollection = model.isInline() && model.getValueElementType().isCollection();
		if (isHandlingCollection || !GMEUtil.isEditionValid(value, startValue, editor)) {
			propertyPanel.rollbackTransaction();
			
			if (isHandlingCollection && GMEUtil.isEditionValid(value, null, editor))
				handleCollectionEdition(value, model, editCell);
			return;
		}
		
		if (oldValue != value)
			property.set(parentEntity, value);
		else if (!property.getType().isSimple())
			propertyPanel.updatePropertyModelValue(model, propertyName, value, model.getParentEntityType(), null, false);
		
		List<Manipulation> manipulationsDone = propertyPanel.editionNestedTransaction.getManipulationsDone();
		Manipulation triggerManipulation = manipulationsDone.stream().filter(m -> m.manipulationType().equals(ManipulationType.CHANGE_VALUE))
				.findFirst().orElse(null);
		
		propertyPanel.editionNestedTransaction.commit();
		propertyPanel.editionNestedTransaction = null;
		propertyPanel.gmEditionViewControllerSupplier.get().unregisterAsCurrentEditionView(propertyPanel);
		startValue = null;
		
		ModelMdResolver mdResolver = propertyPanel.getMetaData();
		if (mdResolver == null)
			return;
		
		PropertyMdResolver propertyMdResolver = mdResolver.useCase(propertyPanel.getUseCase()).entity(parentEntity).property(propertyName);
		
		PersistenceGmSession dataSession = propertyPanel.gmSession;
		if (parentEntity instanceof ProxyEnhancedEntity) { //when handling proxy types, we should use the actual data session
			dataSession = propertyPanel.alternativeGmSession;
			
			GenericSnapshot genericSnapshot = GenericSnapshot.T.create();
			genericSnapshot.setTypeSignature(parentEntity.entityType().getTypeSignature());
			
			Map<String, Object> map = new HashMap<>();
			for (Map.Entry<AbstractProxyProperty, Object> entry : ((ProxyEnhancedEntity) parentEntity).properties().entrySet())
				map.put(entry.getKey().getName(), entry.getValue());
			
			genericSnapshot.setProperties(map);
			parentEntity = genericSnapshot;
		}
		
		Future<Void> future = GMEUtil.fireOnEditRequest(parentEntity, triggerManipulation, propertyMdResolver, dataSession,
				propertyPanel.transientSession, null, propertyPanel.transientSessionSupplier, propertyPanel.notificationFactorySupplier);
		
		propertyPanel.lastEditedPropertyModel = model;
		propertyPanel.localManipulation = true;
		
		if (future == null) {
			propertyPanel.handleAutoCommit();
			return;
		}
		
		propertyPanel.mask();
		future.andThen(result -> {
			propertyPanel.unmask();
			propertyPanel.handleAutoCommit();
		}).onError(e -> {
			propertyPanel.unmask();
			ErrorDialog.show(LocalizedText.INSTANCE.errorRunningOnEditRequest(), e);
			e.printStackTrace();
		});
	}
	
	public void handleCollectionEdition(Object value, PropertyModel model, GridCell editCell) {
		if (!PropertyPanelRowExpander.isCollectionPropertyEditable(model, propertyPanel))
			return;
		
		GenericEntity parentEntity = model.getParentEntity();
		String propertyName = model.getPropertyName();
		Property property = model.getParentEntityType().getProperty(propertyName);
		Object oldValue = property.get(parentEntity);
		
		PropertyRelatedModelPathElement collectionElement = new PropertyPathElement(parentEntity, property, oldValue);
		GMTypeInstanceBean bean = new GMTypeInstanceBean(GMF.getTypeReflection().getType(value), value);
		GMEUtil.insertToListOrSet(collectionElement, Arrays.asList(bean), -1);
		
		new Timer() {
			@Override
			public void run() {
				Scheduler.get().scheduleDeferred(() -> gridInlineEditing.startEditing(editCell));
			}
		}.schedule(500);
		
		propertyPanel.localManipulation = true;
	}
	
	private void fireClickOrDoubleClick(boolean click, GmMouseInteractionEvent event) {
		if (propertyPanel.gmInteractionListeners == null)
			return;
		
		List<GmInteractionListener> listenersCopy = new ArrayList<>(propertyPanel.gmInteractionListeners);
		for (GmInteractionListener listener : listenersCopy) {
			if (click)
				listener.onClick(event);
			else
				listener.onDblClick(event);
		}
	}
	
	private static boolean isBooleanProperty(PropertyModel model) {
		if (model != null) {
			if (model.getValueElementType().getJavaType().equals(Boolean.class))
				return true;
			
			if ("initializer".equals(model.getPropertyName()) && model.getParentEntity() instanceof GmProperty
					&& ((GmProperty) model.getParentEntity()).getType() != null) {
				return ((GmProperty) model.getParentEntity()).getType().getTypeSignature().equalsIgnoreCase("boolean");
			}
		}
		
		return false;
	}

}
