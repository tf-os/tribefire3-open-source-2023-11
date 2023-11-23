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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelCss;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelResources;
import com.braintribe.gwt.gmview.action.client.ChangeInstanceAction;
import com.braintribe.gwt.gmview.client.GmSessionHandler;
import com.braintribe.gwt.gmview.client.IconAndType;
import com.braintribe.gwt.gmview.client.PropertyBean;
import com.braintribe.gwt.gmview.codec.client.HtmlRenderer;
import com.braintribe.gwt.gmview.codec.client.PropertyRelatedCodec;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ColumnConfigWithMaxWidth;
import com.braintribe.gwt.gxt.gxtresources.gridwithoutlines.client.GxtClearGridTemplates;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.PropertyPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.BooleanType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.display.Icon;
import com.braintribe.model.meta.data.prompt.EditAsHtml;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.data.prompt.VirtualEnum;
import com.braintribe.model.meta.data.prompt.VirtualEnumConstant;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.sencha.gxt.core.client.Style.ScrollDirection;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.Point;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.data.shared.event.StoreAddEvent;
import com.sencha.gxt.data.shared.event.StoreClearEvent;
import com.sencha.gxt.data.shared.event.StoreDataChangeEvent;
import com.sencha.gxt.data.shared.event.StoreFilterEvent;
import com.sencha.gxt.data.shared.event.StoreHandlers;
import com.sencha.gxt.data.shared.event.StoreRecordChangeEvent;
import com.sencha.gxt.data.shared.event.StoreRemoveEvent;
import com.sencha.gxt.data.shared.event.StoreSortEvent;
import com.sencha.gxt.data.shared.event.StoreUpdateEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.GridViewConfig;
import com.sencha.gxt.widget.core.client.grid.GroupingView;

public class PropertyPanelGroupingView extends GroupingView<PropertyModel> {
	
	private static final Logger logger = new Logger(PropertyPanelGroupingView.class);
	
	private PropertyPanel propertyPanel;
	private Point savedScrollState;
	
	public PropertyPanelGroupingView(PropertyPanel propertyPanel) {
		super(new PropertyPanelGroupingViewAppearance());
		tpls = GWT.create(GxtClearGridTemplates.class);
		setTrackMouseOver(false);
		setForceFit(true);
		setShowDirtyCells(false);
		this.propertyPanel = propertyPanel;
		
		setViewConfig(new GridViewConfig<PropertyModel>() {
			@Override
			public String getRowStyle(PropertyModel model, int rowIndex) {
				return "";
			}
			
			@Override
			public String getColStyle(PropertyModel model, ValueProvider<? super PropertyModel, ?> valueProvider, int rowIndex, int colIndex) {
				return "gmeGridColumn";
			}
		});
	}
	
	/**
	 * Returns true when the group was toggled. False, otherwise.
	 */
	public boolean collapseGroup(String groupToCollapse) {
		NodeList<Element> groups = getGroups();
		for (int i = 0, len = groups.getLength(); i < len; i++) {
			com.google.gwt.dom.client.Element group = groups.getItem(i);
			NodeList<Element> divs = group.getElementsByTagName("div");
			for (int j = 0; j < divs.getLength(); j++) {
				Element div = divs.getItem(j);
				if ("propertyGroupRuler".equals(div.getClassName())) {
					String groupName = div.getInnerText();
					if (groupToCollapse.equals(groupName)) {
						toggleGroup(group, false);
						return true;
					}
					break;
				}
			}
		}
		
		return false;
	}
	
	protected boolean isGroupInState(String groupName) {
		return state.containsKey(groupName);
	}
	
	@Override
	protected void afterRender() {
		super.afterRender();
		if (propertyPanel.readOnly)
			return;
		
		propertyPanel.prepareEditors() //
				.andThen(result -> {
					propertyPanel.propertyPanelGrid.editorsReady = true;
					propertyPanel.fireEditorsReady();
					if (propertyPanel.propertyPanelGrid.startEditingPending)
						propertyPanel.startEditing();
				}).onError(Throwable::printStackTrace);
	}
	
	@Override
	protected void fitColumns(boolean preventRefresh, boolean onlyExpand, int omitColumn) {
		int tw = getTotalWidth();
		int aw = grid.getElement().getWidth(true) - getScrollAdjust();
		if (aw <= 0)
			aw = grid.getElement().getComputedWidth();

		if (aw < 20 || aw > 2000) // not initialized, so don't screw up the default widths
			return;

		int extra = aw - tw;

		if (extra == 0)
			return;

		int colCount = cm.getColumnCount();
		Stack<Integer> cols = new Stack<>();
		int width = 0;
		int w;

		for (int i = 0; i < colCount; i++) {
			w = cm.getColumnWidth(i);
			if (!cm.isHidden(i) && !cm.isFixed(i) && i != omitColumn) {
				cols.push(i);
				cols.push(w);
				width += w;
			}
		}

		int widthReduced = 0;
		double frac = ((double) (extra)) / width;
		while (cols.size() > 0) {
			w = cols.pop();
			int i = cols.pop();
			int ww = Math.max(getHeader().getMinColumnWidth(), (int) Math.floor(w + w * frac));
			
			//This is the actual difference: we are checking if there is a maxWidth defined
			int maxWidth = 0;
			ColumnConfig<?,?> column = cm.getColumn(i);
			if (column instanceof ColumnConfigWithMaxWidth)
				maxWidth = ((ColumnConfigWithMaxWidth<?,?>) column).getMaxWidth();
			if (maxWidth > 0) {
				widthReduced = ww - maxWidth;
				ww = Math.min(ww, maxWidth);
			}
			
			cm.setColumnWidth(i, ww, true);
		}
		
		if (widthReduced > 0)
			cm.setColumnWidth(1, cm.getColumnWidth(1) + widthReduced, true);

		tw = getTotalWidth();
		if (tw > aw) {
			width = 0;
			for (int i = 0; i < colCount; i++) {
				w = cm.getColumnWidth(i);
				if (!cm.isHidden(i) && !cm.isFixed(i) && w > getHeader().getMinColumnWidth()) {
					cols.push(i);
					cols.push(w);
					width += w;
				}
			}
			frac = ((double) (aw - tw)) / width;
			while (cols.size() > 0) {
				w = cols.pop();
				int i = cols.pop();
				int ww = Math.max(getHeader().getMinColumnWidth(), (int) Math.floor(w + w * frac));
				cm.setColumnWidth(i, ww, true);
			}
		}

		if (!preventRefresh)
			updateAllColumnWidths();
	}
	
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
	public void calculateVBar(boolean force) {
		super.calculateVBar(force);
	}
	
	@Override
	protected <V> StoreSortInfo<PropertyModel> createStoreSortInfo(ColumnConfig<PropertyModel, V> column, SortDir sortDir) {
		return new StoreSortInfo<>(PropertyModel.getGroupPriorityComparator(), SortDir.ASC);
	}
	
	/**
	 * Overriding this to remove the record update listener
	 */
	@Override
	protected void initListeners() {
		super.initListeners();
		
		listener = new StoreHandlers<PropertyModel>() {
			@Override
			public void onAdd(StoreAddEvent<PropertyModel> event) {
				PropertyPanelGroupingView.this.onAdd(event.getItems(), event.getIndex());
			}

			@Override
			public void onClear(StoreClearEvent<PropertyModel> event) {
				PropertyPanelGroupingView.this.onClear(event);
			}

			@Override
			public void onDataChange(StoreDataChangeEvent<PropertyModel> event) {
				PropertyPanelGroupingView.this.onDataChanged(event);
			}

			@Override
			public void onFilter(StoreFilterEvent<PropertyModel> event) {
				PropertyPanelGroupingView.this.onDataChanged(null);
			}

			@Override
			public void onRecordChange(StoreRecordChangeEvent<PropertyModel> event) {
				PropertyModel m = event.getRecord().getModel();
				if (!m.getFlow()) //Running the update in this situation was bringing refresh problems
					PropertyPanelGroupingView.this.onUpdate(ds, Collections.singletonList(event.getRecord().getModel()));
			}

			@Override
			public void onRemove(StoreRemoveEvent<PropertyModel> event) {
				PropertyPanelGroupingView.this.onRemove(event.getItem(), event.getIndex(), false);
			}

			@Override
			public void onSort(StoreSortEvent<PropertyModel> event) {
				PropertyPanelGroupingView.this.onDataChanged(null);
			}

			@Override
			public void onUpdate(StoreUpdateEvent<PropertyModel> event) {
				List<PropertyModel> models = event.getItems();
				if (models.stream().filter(m -> m.getFlow()).count() == 0) //Running the update in this situation was bringing refresh problems
					PropertyPanelGroupingView.this.onUpdate(ds, models);
			}
		};
	}
	
	protected void saveScrollState() {
		savedScrollState = getScroller() != null ? getScrollState() : null;
	}
	
	protected void restoreScrollState() {
		if (savedScrollState != null) {
			XElement scroller = getScroller();
			scroller.scrollTo(ScrollDirection.LEFT, savedScrollState.getX());
			scroller.scrollTo(ScrollDirection.TOP, savedScrollState.getY());
			savedScrollState = null;
		}
	}
	
	protected String prepareValueDisplay(Property property, PropertyModel propertyModel) {
		return prepareValueDisplay(property, propertyModel, true);
	}

	protected String prepareValueDisplay(Property property, PropertyModel propertyModel, boolean prepareNotSet) {
		GenericModelType propertyType = property == null ? null : property.getType();
		if (property != null && "initializer".equals(property.getName()) && propertyModel.getParentEntity() instanceof GmProperty
				&& ((GmProperty) propertyModel.getParentEntity()).getType() != null
				&& ((GmProperty) propertyModel.getParentEntity()).getType().getTypeSignature().equalsIgnoreCase("boolean")) {
			propertyType = BooleanType.TYPE_BOOLEAN;
		}
		
		if (property == null || propertyType.getJavaType() != Boolean.class) {
			String valueDisplay = prepareValueDisplayOrFlowValueDisplay(property, false, propertyModel, prepareNotSet);
			if (propertyModel.getPassword())
				return GMEUtil.preparePasswordString(valueDisplay);
			else
				return valueDisplay;
		}
		
		GenericEntity parentEntity = propertyModel.getParentEntity();
		Boolean propertyValue = parentEntity != null ? property.get(parentEntity) : null;
		String booleanClass;
		
		PropertyPanelCss css = PropertyPanelResources.INSTANCE.css();
		if (propertyPanel.readOnly || !propertyModel.isEditable()) {
			if (propertyValue == null)
				booleanClass = css.checkNullReadOnlyValue();
			else
				booleanClass = propertyValue ? css.checkedReadOnlyValue() : css.uncheckedReadOnlyValue();
		} else {
			if (propertyValue == null)
				booleanClass = css.checkNullValue();
			else
				booleanClass = propertyValue ? css.checkedValue() : css.uncheckedValue();
		}
		
		StringBuilder display = new StringBuilder();
		display.append("<div class='").append(booleanClass);
		if (propertyValue != null)
			display.append(" ").append(propertyValue ? "CHECKED" : "UNCHECKED");
		display.append("'></div>");
		
		String placeholder = propertyModel.getPlaceHolder();
		if (placeholder != null) {
			display.append("<div class='").append(PropertyPanelCss.EXTERNAL_PROPERTY_VALUE_PLACEHOLDER).append("'>");
			display.append(placeholder).append("</div>");
		}
		
		return display.toString();
	}
	
	protected String prepareFlowDisplay(Property property, PropertyModel propertyModel) {
		return prepareValueDisplayOrFlowValueDisplay(property, true, propertyModel);
	}
	
	private String prepareValueDisplayOrFlowValueDisplay(Property property, boolean prepareFlow, PropertyModel propertyModel) {
		return prepareValueDisplayOrFlowValueDisplay(property, prepareFlow, propertyModel, true);
	}
	
	private String prepareValueDisplayOrFlowValueDisplay(Property property, boolean prepareFlow, PropertyModel propertyModel, boolean prepareNotSet) {
		String valueDisplay = null;
		GenericModelType propertyType = propertyModel.getValueElementType();
		GenericEntity modelParentEntity = propertyModel.getParentEntity();
		EntityType<GenericEntity> modelParentEntityType = propertyModel.getParentEntityType();
		Object propertyValue = modelParentEntity != null && modelParentEntityType != null ? property.get(modelParentEntity) : null;
		boolean isEmptyCollection = isCollectionAndEmpty(propertyValue);
		if (propertyValue != null && !isEmptyCollection) {
			if (prepareFlow) {
				if (propertyPanel.specialFlowCodecRegistry != null) {
					Codec<Object, String> renderer = propertyPanel.specialFlowCodecRegistry.getCodec(propertyType.getJavaType());
					if (renderer != null) {
						if (renderer instanceof GmSessionHandler)
							((GmSessionHandler) renderer).configureGmSession(propertyPanel.gmSession);
						try {
							valueDisplay = renderer.encode(propertyValue);
						} catch (CodecException e) {
							logger.error("Error while getting flow renderer value.", e);
							e.printStackTrace();
						}
					}
				}
				
				if (valueDisplay == null && isPropertyCollection(propertyType, propertyModel.getPropertyName(), modelParentEntity))
					valueDisplay = prepareCollectionFlowDisplay(property, propertyValue, modelParentEntity, propertyModel.isInline());
			}
			
			if (valueDisplay == null) {
				if (isPropertyCollection(propertyType, propertyModel.getPropertyName(), modelParentEntity) && !prepareFlow
						&& propertyModel.isInline()) {
					if (PropertyPanelRowExpander.isCollectionPropertyEditable(propertyModel, propertyPanel))
						valueDisplay = "<div class='" + PropertyPanelCss.EXTERNAL_PROPERTY_VALUE_OPERATION + "'>" + LocalizedText.INSTANCE.add() + "</div>";
					else
						valueDisplay = "&nbsp;";
				} else
					valueDisplay = prepareStringValue(propertyValue, new PropertyBean(property.getName(), modelParentEntity, modelParentEntityType));
				if (prepareFlow && valueDisplay != null)
					valueDisplay = valueDisplay.replace("\n", "<br>");
			}
		} else if (propertyModel.isEditable() && !propertyPanel.readOnly) {
			List<List<ModelPath>> modelPaths = propertyPanel.transformSelection(Collections.singletonList(PropertyPanel.getModelPath(propertyModel)));
			if (isPropertyCollection(propertyType, propertyModel.getPropertyName(), modelParentEntity)) {
				if (!prepareFlow && propertyModel.isInline() && PropertyPanelRowExpander.isCollectionPropertyEditable(propertyModel, propertyPanel))
					valueDisplay = "<div class='" + PropertyPanelCss.EXTERNAL_PROPERTY_VALUE_OPERATION + "'>" + LocalizedText.INSTANCE.add() + "</div>";
				else
					valueDisplay = "&nbsp;";
				propertyModel.setUsePlaceholder(false);
			} else if (propertyType.isBase() || (propertyType.isEntity() && !propertyPanel.specialFlowClasses.contains(((EntityType<?>) propertyType).getJavaType()))) {
				ChangeInstanceAction changeInstanceAction = propertyPanel.getShortcutChangeInstanceAction();
				changeInstanceAction.updateState(modelPaths);
				if (!changeInstanceAction.getHidden() && isAssignValid(propertyModel)) {
					valueDisplay = "<div class='" + PropertyPanelCss.EXTERNAL_PROPERTY_VALUE_ENTITY_ASSIGN + "'>"
							+ LocalizedText.INSTANCE.assign() + "...</div>";
					propertyModel.setUsePlaceholder(false);
				}
			} else if (prepareFlow) {
				valueDisplay = "<div class='" + PropertyPanelCss.EXTERNAL_PROPERTY_VALUE_OPERATION + "'>"
						+ LocalizedText.INSTANCE.typeValue() + "</div>";
				propertyModel.setUsePlaceholder(false);
			}
		} else if (propertyValue != null && propertyType.isEntity())
			valueDisplay = "<div class='" + PropertyPanelResources.INSTANCE.css().propertyEntity() + "'>" + propertyValue.toString() + "...</div>";			
		
		if (valueDisplay == null && !propertyModel.isEditable()) {
			if (prepareNotSet) {
				propertyModel.setUsePlaceholder(false);
				return "<div class='" + PropertyPanelResources.INSTANCE.css().propertyNameReadOnly() + "'>" + LocalizedText.INSTANCE.notSet() + "</div>";
			}
			return "";
		}
		
		String theDisplay = valueDisplay != null ? valueDisplay : (propertyValue != null ? propertyValue.toString() : "");
		if (propertyModel.getPassword())
			return GMEUtil.preparePasswordString(theDisplay);
		else
			return theDisplay;
	}
	
	private boolean isPropertyCollection(GenericModelType propertyType, String propertyName, GenericEntity parentEntity) {
		if (propertyType.isCollection())
			return true;
		
		return "initializer".equals(propertyName) && parentEntity instanceof GmProperty && ((GmProperty) parentEntity).getType() != null
				&& ((GmProperty) parentEntity).getType().isGmCollection();
	}
	
	private String prepareCollectionFlowDisplay(Property property, Object propertyValue, GenericEntity parentEntity, boolean inline) {
		PropertyPanelCss css = PropertyPanelResources.INSTANCE.css();
		int maxCollectionSize = propertyPanel.maxCollectionSize;	
		boolean propertyEditable = true;
			
		//RVE - allow to set how many items from collection should be showed, possible with Max MetaData on Collection GmEntityType
		//support String and Integer values, -1 = all
		ModelMdResolver modelMdResolver = propertyPanel.getMetaData();
		if (modelMdResolver != null)
			modelMdResolver = modelMdResolver.lenient(propertyPanel.lenient);		
		PropertyMdResolver propertyMdResolver = null;
		if (modelMdResolver != null && property != null) {
			EntityMdResolver entityMdResolver = modelMdResolver.entityType(property.getDeclaringType()).lenient(propertyPanel.lenient).useCase(propertyPanel.useCase);
			propertyMdResolver = entityMdResolver.property(property);
			if (propertyMdResolver != null) {
				if (parentEntity != null)
					propertyEditable = GMEMetadataUtil.isPropertyEditable(propertyMdResolver, parentEntity);				
				
				maxCollectionSize = GMEMetadataUtil.getMaxLimit(entityMdResolver, property, maxCollectionSize);
			}
		}
		
		StringBuilder builder = new StringBuilder();
		if (propertyValue instanceof List) {
			List<Object> list = (List<Object>) propertyValue;
			int listSize = list.size();
			if (maxCollectionSize > -1)
				listSize = Math.min(maxCollectionSize, list.size());
			
			if (inline)
				prepareCollectionInlineDisplay(maxCollectionSize, propertyEditable, builder, list, listSize);
			else {
				builder.append("<table>\n");
				for (int i = 0; i < listSize; i++) {
					builder.append("<tr><td class='gxtReset ").append(PropertyPanelCss.EXTERNAL_FLOW_COLLECTION_ELEMENT).append(" ");
					if (propertyPanel.navigationEnabled)
						builder.append(css.clickableElement()).append(" ");
					builder.append(PropertyPanel.FLOW_COLLECTION_INDEX).append(i).append(" ").append(css.flowListEntry()).append("'>").append(i + 1)
							.append(".</td><td class='gxtReset ").append(PropertyPanelCss.EXTERNAL_FLOW_COLLECTION_ELEMENT).append(" ")
							.append(PropertyPanel.FLOW_COLLECTION_INDEX).append(i).append("'>").append(prepareStringValue(list.get(i), null)).append("</td>");
					
					if (propertyEditable)
						preparePropertyCollectionItemMenu(builder, i, true);
					builder.append("</tr>\n");
				}
				if (list.size() > maxCollectionSize && maxCollectionSize > -1) {
					builder.append("<tr><td class='gxtReset ").append(PropertyPanelCss.EXTERNAL_FLOW_COLLECTION_ELEMENT).append(" ").append(css.flowListEntry())
							.append("'>").append(listSize + 1).append(".<td class='gxtReset ").append(PropertyPanelCss.EXTERNAL_FLOW_COLLECTION_ELEMENT)
							.append("'>...</td></tr>\n");
				}
				builder.append("</table>");
			}
		} else if (propertyValue instanceof Set) {
			Set<Object> set = (Set<Object>) propertyValue;
			List<Object> setAsList = new ArrayList<>();
			int setSize = set.size();
			if (maxCollectionSize > -1)
				setSize = Math.min(maxCollectionSize, set.size());
			
			if (inline)
				prepareCollectionInlineDisplay(maxCollectionSize, propertyEditable, builder, set, setSize);
			else {
				builder.append("<table>\n");
				int counter = 0;
				for (Object value : set) {
					builder.append("<tr><td class='gxtReset ").append(PropertyPanelCss.EXTERNAL_FLOW_COLLECTION_ELEMENT).append(" ").append(PropertyPanel.FLOW_COLLECTION_INDEX)
							.append(counter).append(" ").append(css.flowSetEntry()).append("'></td><td class='gxtReset ")
							.append(PropertyPanelCss.EXTERNAL_FLOW_COLLECTION_ELEMENT).append(" ").append(PropertyPanel.FLOW_COLLECTION_INDEX).append(counter)
							.append("'>").append(prepareStringValue(value, null)).append("</td>");
					if (propertyEditable)
						preparePropertyCollectionItemMenu(builder, counter, true);
					builder.append("</tr>\n");
	
					setAsList.add(value);
					counter++;
					if (counter == setSize)
						break;
				}
				if (set.size() > maxCollectionSize && maxCollectionSize > -1) {
					builder.append("<tr><td class='gxtReset ").append(PropertyPanelCss.EXTERNAL_FLOW_COLLECTION_ELEMENT).append(" ").append(css.flowSetEntry())
							.append("'>").append("<td class='gxtReset ").append(PropertyPanelCss.EXTERNAL_FLOW_COLLECTION_ELEMENT).append("'>...</td></tr>\n");
				}
				builder.append("</table>");
			}
		} else {
			Map<Object, Object> map = (Map<Object, Object>) propertyValue;
			List<Object> mapAsList = new ArrayList<>();
			int mapSize = map.size();
			if (maxCollectionSize > -1)
				mapSize = Math.min(maxCollectionSize, map.size());
			
			builder.append("<table>\n");
			int counter = 0;
			for (Map.Entry<Object, Object> entry : map.entrySet()) {
				builder.append("<tr><td class='gxtReset ").append(css.flowMapKeyElement()).append(" ");
				if (propertyPanel.navigationEnabled)
					builder.append(css.clickableElement()).append(" ");
				builder.append(PropertyPanel.FLOW_COLLECTION_INDEX).append(counter).append("'>")
						.append(prepareStringValue(entry.getKey(), null)).append("</td><td class='gxtReset ").append(css.flowMapEntry())
						.append("'></td><td class='gxtReset ").append(css.flowMapValueElement()).append(" ");
				if (propertyPanel.navigationEnabled)
					builder.append(css.clickableElement()).append(" ");
				builder.append(PropertyPanel.FLOW_COLLECTION_INDEX).append(counter).append("'>")
						.append(prepareStringValue(entry.getValue(), null)).append("</td>");
				
				if (propertyEditable)
					preparePropertyCollectionItemMenu(builder, counter, true);
				builder.append("</tr>\n");
				
				mapAsList.add(entry);
				counter++;
				if (counter == mapSize)
					break;
			}
			if (map.size() > maxCollectionSize && maxCollectionSize > -1) {
				builder.append("<tr><td class='gxtReset ").append(PropertyPanelCss.EXTERNAL_FLOW_COLLECTION_ELEMENT).append("'><td class='gxtReset ")
						.append(PropertyPanelCss.EXTERNAL_FLOW_COLLECTION_ELEMENT).append("'>...</td></tr>\n");
			}
			builder.append("</table>");
		}
		
		return builder.toString();
	}

	private void prepareCollectionInlineDisplay(int maxCollectionSize, boolean propertyEditable, StringBuilder builder, Collection<Object> collection,
			int collectionSize) {
		builder.append("<div class='collectionInlineContainer'><div class='collectionInlineRow'>");
		int counter = 0;
		for (Object value : collection) {
			builder.append("<div class='collectionInlineBlock'><div class='collectionInlineItem collectionInlineBackgroundBox'>");
			builder.append("<div class='collectionInlineLabelHorizontal ").append(PropertyPanelCss.EXTERNAL_FLOW_COLLECTION_ELEMENT);
			builder.append(" ").append(PropertyPanel.FLOW_COLLECTION_INDEX).append(counter).append("'>");
			
			builder.append(prepareStringValue(value, null));
			
			if (propertyEditable)
				preparePropertyCollectionItemMenu(builder, counter, false);
			
			builder.append("</div></div></div>");
			
			counter++;
			if (counter == collectionSize)
				break;
		}
		
		if (collection.size() > maxCollectionSize && maxCollectionSize > -1) {
			builder.append("<div class='collectionInlineBlock'><div class='collectionInlineItem collectionInlineBackgroundBox'>");
			builder.append("<div class='collectionInlineLabelHorizontal ").append(PropertyPanelCss.EXTERNAL_FLOW_COLLECTION_ELEMENT);
			builder.append("'>...</div></div></div>");
		}
		
		builder.append("</div></div>");
	}
	
	private String prepareStringValue(Object propertyValue, PropertyBean propertyBean) {
		if (propertyValue == null)
			return "";
		
		ModelMdResolver modelMdResolver = propertyPanel.getMetaData();
		if (modelMdResolver != null)
			modelMdResolver = modelMdResolver.lenient(propertyPanel.lenient);
		PropertyMdResolver propertyMdResolver = null;
		if (modelMdResolver != null && propertyBean != null) {
			EntityMdResolver entityMdResolver;
			GenericEntity parentEntity = propertyBean.parentEntity;
			if (parentEntity != null)
				entityMdResolver = modelMdResolver.entity(parentEntity);
			else
				entityMdResolver = modelMdResolver.entityType(propertyBean.parentEntityType);
			propertyMdResolver = entityMdResolver.useCase(propertyPanel.useCase).lenient(propertyPanel.lenient).property(propertyBean.propertyName);
		}
		
		String stringValue = null;
		GenericModelType valueType = GMF.getTypeReflection().getType(propertyValue);
		if (propertyPanel.codecRegistry != null) {
			Codec<Object, String> renderer = propertyPanel.codecRegistry.getCodec(valueType.getJavaType());
			if (renderer != null) {
				if (renderer instanceof PropertyRelatedCodec) {
					PropertyRelatedCodec propertyRelatedCodec = (PropertyRelatedCodec) renderer;
					propertyRelatedCodec.configureModelMdResolver(modelMdResolver);
					propertyRelatedCodec.configureUseCase(propertyPanel.useCase);
					propertyRelatedCodec.configurePropertyBean(propertyBean);
				}
				
				try {
					stringValue = renderer.encode(propertyValue);
					if (stringValue != null && !(renderer instanceof HtmlRenderer))
						stringValue = SafeHtmlUtils.htmlEscape(stringValue);
				} catch (CodecException e) {
					logger.error("Error while getting value renderer value.", e);
					e.printStackTrace();
				}
			}
		}
		
		if (stringValue != null)
			return stringValue;
		
		String iconString = null;
		ModelPath modelPath = null;		
		if (valueType.isEntity()) {
			String selectiveInformation = null;
			if (((GenericEntity) propertyValue).session() != null) {
				selectiveInformation = SelectiveInformationResolver.resolve((EntityType<?>) valueType, (GenericEntity) propertyValue,
					modelMdResolver, propertyPanel.useCase/* , null */);
			} else if (modelMdResolver != null) {
				selectiveInformation = SelectiveInformationResolver.resolve((GenericEntity) propertyValue,
						modelMdResolver.entityType((EntityType<?>) valueType).useCase(propertyPanel.useCase));
			}
			
			if (selectiveInformation != null && !selectiveInformation.trim().isEmpty())
				stringValue = selectiveInformation;
			modelPath = new ModelPath();
			modelPath.add(new RootPathElement((GenericEntity) propertyValue));			
			
			
		} else if (valueType.isEnum()) {
			String enumString = propertyValue.toString();
			Name displayInfo = GMEMetadataUtil.getName(valueType, propertyValue, modelMdResolver, propertyPanel.useCase);
			if (displayInfo != null && displayInfo.getName() != null)
				enumString = I18nTools.getLocalized(displayInfo.getName());
			stringValue = SafeHtmlUtils.htmlEscape(enumString);			
			modelPath = new ModelPath();
			modelPath.add(new RootPathElement(valueType, propertyValue));
		} else {
			String propertyValueString = propertyValue.toString();
			if (propertyMdResolver != null) {
				VirtualEnum ve = propertyMdResolver.meta(VirtualEnum.T).exclusive();
				if (ve != null) {
					modelPath = new ModelPath();
					modelPath.add(new PropertyPathElement(ve, ve.entityType().getProperty("constants"), propertyValueString));

					for (VirtualEnumConstant virtualEnumConstant : ve.getConstants()) {
						if (virtualEnumConstant.getValue() == propertyValueString || virtualEnumConstant.getValue().equals(propertyValueString))
							propertyValueString = I18nTools.getLocalized(virtualEnumConstant.getDisplayValue());
						break;
					}

					propertyPanel.handleMetadataReevaluation(propertyMdResolver, VirtualEnum.T);
				}
			}
			
			boolean editAsHtml = false;
			if (propertyMdResolver != null) {
				if (propertyMdResolver.meta(EditAsHtml.T).exclusive() != null)
					editAsHtml = true;
			}
			
			if (!editAsHtml)
				stringValue = SafeHtmlUtils.htmlEscape(propertyValueString);
			else
				stringValue = propertyValueString;
		}
		
		IconAndType iconAndType = propertyPanel.iconProvider.apply(modelPath);
		String iconUrlString = null;
		if (iconAndType != null && iconAndType.getIcon() != null) {
			iconUrlString = iconAndType.getIcon().getSafeUri().asString();
		} else if (valueType.isEnum()) {
			//2nd choice for EnumTypes (VirtualEnums)
			Icon icon = propertyPanel.gmSession.getModelAccessory().getMetaData().lenient(true).enumConstant((Enum<?>) propertyValue).
					useCase(propertyPanel.getUseCase()).meta(Icon.T).exclusive();
			if (icon != null && icon.getIcon() != null) {
				iconUrlString = propertyPanel.gmSession.getModelAccessory().getModelSession().resources()
						.url(GMEIconUtil.getLargestImageFromIcon(icon.getIcon())).asString();
			}
		}
		
		if (iconUrlString != null && !iconUrlString.isEmpty()) {
			iconString = "<img src='" + iconUrlString + "' class='"
					+ PropertyPanelResources.INSTANCE.css().propertyIcon() + "'/>";
			iconString += SafeHtmlUtils.htmlEscape(stringValue != null ? stringValue : propertyValue.toString());
		} 
		stringValue = iconString == null ? stringValue : iconString;
		
		return stringValue != null ? stringValue : SafeHtmlUtils.htmlEscape(propertyValue.toString());
	}
	
	private void preparePropertyCollectionItemMenu(StringBuilder builder, int index, boolean table) {
		if (propertyPanel.readOnly)
			return;
		
		if (table) {
			builder.append("      <td width='14px' ");
			builder.append("class='gxtReset ").append(PropertyPanelResources.INSTANCE.css().propertyCollectionItemMenu()).append(" ")
			.append(PropertyPanel.FLOW_COLLECTION_INDEX).append(index).append("' ");
			builder.append("style='height: 14px; padding-right: 13px;'></td>\n");
			return;
		}
		
		builder.append("      <span class='").append(PropertyPanelResources.INSTANCE.css().propertyCollectionItemMenu()).append(" ")
		.append(PropertyPanel.FLOW_COLLECTION_INDEX).append(index).append("' ");
		builder.append("style='height: 14px; padding-right: 13px;'></span>\n");
	}
	
	protected static boolean isCollectionAndEmpty(Object propertyValue) {
		if (!(propertyValue instanceof Collection) && !(propertyValue instanceof Map))
			return false;
		
		if (propertyValue instanceof Collection)
			return ((Collection<?>) propertyValue).isEmpty();
		else
			return ((Map<?,?>) propertyValue).isEmpty();
	}
	
	private boolean isAssignValid(PropertyModel propertyModel) {
		if (propertyModel.getSimplifiedAssignment() != null)
			return false;
		
		if (propertyModel.getExtendedInlineField() == null)
			return true;
		
		return propertyModel.isReferenceable();
	}
}
