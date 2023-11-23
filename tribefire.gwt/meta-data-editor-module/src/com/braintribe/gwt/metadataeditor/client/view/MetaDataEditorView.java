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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.async.client.MultiLoader;
import com.braintribe.gwt.codec.registry.client.CodecRegistry;
import com.braintribe.gwt.genericmodelgxtsupport.client.GMEditorSupport;
import com.braintribe.gwt.genericmodelgxtsupport.client.PropertyFieldContext;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.TriggerFieldAction;
import com.braintribe.gwt.gme.propertypanel.client.field.SimplifiedEntityField;
import com.braintribe.gwt.gme.propertypanel.client.field.SimplifiedEntityFieldConfiguration;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelCss;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelResources;
import com.braintribe.gwt.gmview.action.client.ChangeInstanceAction;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.GmSelectionSupport;
import com.braintribe.gwt.gmview.client.GmSessionHandler;
import com.braintribe.gwt.gmview.client.InstanceSelectionData;
import com.braintribe.gwt.gmview.client.SelectionConfig;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedColumnHeader;
import com.braintribe.gwt.gxt.gxtresources.gridwithoutlines.client.GridWithoutLinesAppearance;
import com.braintribe.gwt.gxt.gxtresources.multieditor.client.MultiEditorGridInlineEditing;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.metadataeditor.client.MetaDataEditorPanel;
import com.braintribe.gwt.metadataeditor.client.MetaDataEditorUtil;
import com.braintribe.gwt.metadataeditor.client.experts.DeclaredOverviewExpert;
import com.braintribe.gwt.metadataeditor.client.experts.DeclaredPropertyOverviewExpert;
import com.braintribe.gwt.metadataeditor.client.experts.EffectiveOverviewExpert;
import com.braintribe.gwt.metadataeditor.client.experts.MetaDataEditorBaseExpert;
import com.braintribe.gwt.metadataeditor.client.experts.MetaDataEditorExpert;
import com.braintribe.gwt.metadataeditor.client.experts.MetaDataEditorExpertResultType;
import com.braintribe.gwt.metadataeditor.client.listeners.SelectionListeners;
import com.braintribe.gwt.metadataeditor.client.resources.MetaDataEditorResources;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
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
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
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
import com.braintribe.model.meta.data.constraint.MaxLength;
import com.braintribe.model.meta.data.constraint.Pattern;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.data.prompt.VirtualEnum;
import com.braintribe.model.meta.info.GmCustomTypeInfo;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmEnumConstantInfo;
import com.braintribe.model.meta.info.GmEnumTypeInfo;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.meta.override.GmCustomTypeOverride;
import com.braintribe.model.meta.override.GmEntityTypeOverride;
import com.braintribe.model.meta.override.GmEnumConstantOverride;
import com.braintribe.model.meta.override.GmEnumTypeOverride;
import com.braintribe.model.meta.override.GmPropertyOverride;
import com.braintribe.model.meta.selector.AccessSelector;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.meta.selector.RoleSelector;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.api.transaction.TransactionException;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedCollection;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.selector.Selector;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreFilter;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.event.RowClickEvent.RowClickHandler;
import com.sencha.gxt.widget.core.client.form.IsField;
import com.sencha.gxt.widget.core.client.form.TriggerField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid.GridCell;
import com.sencha.gxt.widget.core.client.grid.GridView;
import com.sencha.gxt.widget.core.client.grid.GridViewConfig;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;
import com.sencha.gxt.widget.core.client.treegrid.TreeGridSelectionModel;
import com.sencha.gxt.widget.core.client.treegrid.TreeGridView;

public class MetaDataEditorView extends ContentPanel implements InitializableBean, MetaDataEditorProvider, ManipulationListener, GmSelectionSupport {

	// private static final PropertyPanelCss css = PropertyPanelResources.INSTANCE.css();
	private static final MetaDataEditorModelProperties props = GWT.create(MetaDataEditorModelProperties.class);

	private final SelectionListeners gmSelectionListeners = new SelectionListeners(this);

	private PersistenceGmSession gmSession;
	private PersistenceGmSession workbenchSession;
	private String useCase;
	private String caption;
	private MetaDataEditorExpert modelExpert;
	private GMEditorSupport gmEditorSupport;
	private List<String> baseTypes;
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
	private MetaDataResolverProvider metaDataResolverProvider;
	private HTML emptyPanel;
	private final String emptyTextMessage = LocalizedText.INSTANCE.noItemsToDisplay();
	private Widget currentWidget = null;
	private Widget gridWidget = null;
	
	private TreeStore<MetaDataEditorModel> store;
	private TreeGrid<MetaDataEditorModel> grid;
	private MultiEditorGridInlineEditing<MetaDataEditorModel> editorGridInline;
	private StoreFilter<MetaDataEditorModel> storeVisibleFilter;
	private ModelPath lastModelPath;
	private ModelPathElement lastModelPathElement;
	private Boolean needUpdate = true;
	private SortDir sortDirAscii = null;
	private SortDir sortDirDependency = null;
	private GmMetaModel declaringGmMetaModel = null;
	private GmMetaModel editGmMetaModel = null;
	private GmEntityType declaringGmEntityType = null;
	private GmEnumType declaringGmEnumType = null;
	private GmEntityType editGmEntityType = null;
	private GmEnumType editGmEnumType = null;
	private GenericEntity editGenericEntity = null;
	private int modelId = 0;
	private MetaDataEditorPanel panel = null;
	private String searchText;
	private Boolean useSearchDeclaredTypesOnly;
	private Boolean isSearchMode = false;
	private ChangeInstanceAction shortcutChangeInstanceAction;
	protected Supplier<? extends Function<SelectionConfig, ? extends Future<InstanceSelectionData>>> selectionFutureProviderProvider;
	private ColumnConfig<MetaDataEditorModel, MetaDataEditorModel> ccName = null;
	private ColumnConfig<MetaDataEditorModel, Object> ccValue = null;
	private ColumnConfig<MetaDataEditorModel, MetaDataSelector> ccSelector = null;
	private ColumnConfig<MetaDataEditorModel, Double> ccConflictPriority = null;
	private ColumnConfig<MetaDataEditorModel, MetaDataEditorModel> ccModelName = null;
	private ColumnConfig<MetaDataEditorModel, MetaDataEditorModel> ccOwnerName = null;
    private ColumnModel<MetaDataEditorModel> cm;
	private Set<Class<?>> specialFlowClasses = new HashSet<Class<?>>();
    
		//private Set<String> lastListUseCase;
	//private Set<String> lastListRole;
	//private Set<String> lastListAccess;
	Timer updateTimer = new Timer() {			
		@Override
		public void run() {
			//RVE need rebuild resolver, because can be send from external, and Model can have new EntityTypes or EnumTypes
			updateTimer.cancel();
			metaDataResolverProvider.rebuildResolver();
			setContentGrid(MetaDataEditorView.this.lastModelPath);
		}
	};		
	Timer refreshTimer = new Timer() {			
		@Override
		public void run() {
			//RVE need rebuild resolver, because can be send from external, and Model can have new EntityTypes or EnumTypes
			refreshTimer.cancel();
			MetaDataEditorView.this.grid.getView().refresh(false);
			updateFilteredModels();
		}
	};

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
		
		this.addShowHandler(event -> {
			if (!MetaDataEditorView.this.grid.isVisible())
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {				
					@Override
					public void execute() {
						updateFilteredModels();
						MetaDataEditorView.this.grid.show();
						MetaDataEditorView.this.gmSelectionListeners.fireListeners();
					}
				});			
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
	
	/**
	 * Configure a provider which provides a selection future provider.
	 * If the read only property was not set to false via {@link #setReadOnly(Boolean)} then this is required.
	 */
	@Override
	@Configurable
	public void setSelectionFutureProvider(Supplier<? extends Function<SelectionConfig, ? extends Future<InstanceSelectionData>>> selectionFutureProvider) {
		this.selectionFutureProviderProvider = selectionFutureProvider;
	}

	/**
	 * Configures which classes should be handled specially when displayed in flow. There will be no link in the property name, and also none in the value.
	 * Defaults to a set containing only the {@link LocalizedString} class. Must not be null.
	 */
	@Override
	@Configurable
	public void setSpecialFlowClasses(Set<Class<?>> specialFlowClasses) {
		this.specialFlowClasses = specialFlowClasses;
	}
	
	//create cell grid - columns, store, editor
	public Widget prepareGrid()  {
		if (this.grid == null && this.editorGridInline == null) {

			this.store = new TreeStore<MetaDataEditorModel>(props.key());
						
			this.storeVisibleFilter = (store, parent, item) -> (item != null) ? item.getIsVisible() : false;
			
			this.store.addFilter(this.storeVisibleFilter);
			/*
			this.store.setEnableFilters(true);	
			*/
			
			//column Name
			ccName = new ColumnConfig<MetaDataEditorModel, MetaDataEditorModel>(props.model(), 200, LocalizedText.INSTANCE.labelName());
			ccName.setCellPadding(false);
			ccName.setSortable(false);
			ccName.setHideable(false);
			ccName.setCell(new AbstractCell<MetaDataEditorModel>() {
				@Override
				public void render(Cell.Context context, MetaDataEditorModel value, SafeHtmlBuilder sb) {					
					String itemName = getItemName(value);
					if (itemName != null) {
						//Boolean useGray = !MetaDataEditorUtil.canEditMetaData(lastModelPath, value.getMetaData(), modelExpert instanceof DeclaredPropertyOverviewExpert);
						Boolean useGray = !isModelEditable(value); 
						
						if (useGray)
							sb.appendHtmlConstant(MetaDataEditorUtil.appendStringCellMain(MetaDataEditorUtil.appendStringGray(itemName)));							
						else	
							sb.appendHtmlConstant(MetaDataEditorUtil.appendStringCellMain(itemName));
					}
       				//sb.appendEscaped(itemName);		
				}
			});
			//column Value
			ccValue = new ColumnConfig<MetaDataEditorModel, Object>(props.propertyValue(), 200, LocalizedText.INSTANCE.labelValue());
			ccValue.setCellPadding(false);
			ccValue.setSortable(false);
			ccValue.setCell(new AbstractCell<Object>("click") {
				
				@Override
				public void render(Cell.Context context, Object value, SafeHtmlBuilder sb) {
					MetaDataEditorModel model = MetaDataEditorView.this.store.findModelWithKey(context.getKey().toString());
					if (model == null)
						return;
					String cellText = null;
					//Boolean useGray = !MetaDataEditorUtil.canEditMetaData(lastModelPath, model.getMetaData(), modelExpert instanceof DeclaredPropertyOverviewExpert);
					Boolean useGray = !isModelEditable(model); 

					if (model.getProperty() != null && model.getMetaData() != null) {
						StringBuilder html = new StringBuilder();
						Property prop = model.getProperty();
						//Object objectVal;
						if (prop != null) {
							if (prop.get(model.getMetaData()) instanceof LocalizedString) {
								LocalizedString localizedText = (LocalizedString) prop.get(model.getMetaData());
								String text = I18nTools.getLocalized(localizedText);
								html.append(text);
							} else if (prop.get(model.getMetaData()) instanceof String) {
								html.append((String) prop.get(model.getMetaData()));
							} else {
								//objectVal = prop.getProperty(value);
								//if (objectVal != null) {
								//	val = objectVal.toString();
								//} else {
									GenericModelType propertyType = prop.getType();
									Object propertyValue = prop.get(model.getMetaData());
									
									//Declared use session Resolver, Effective use Filter Resolver
									if (MetaDataEditorView.this.modelExpert instanceof EffectiveOverviewExpert) {
										html.append(MetaDataEditorUtil.prepareStringValue(propertyValue, propertyType, codecRegistry,
												readOnly || model.isPredicateMetaData(), false, useCase));									
									} else {										
										html.append(MetaDataEditorUtil.prepareStringValue(propertyValue, propertyType, codecRegistry,
												readOnly || model.isPredicateMetaData(), false, useCase));																			
									}
								//}
							}
						}
						
						int count = MetaDataEditorView.this.store.getChildCount(model);
						if (count == 0 && context.getSubIndex() == 0)
							if (useGray)
								cellText = MetaDataEditorUtil.appendStringCell(MetaDataEditorUtil.appendStringGray(html.toString()));
							else	
								cellText = MetaDataEditorUtil.appendStringCell(html.toString());
     		   			    //sb.appendHtmlConstant(html.toString());
							//sb.appendEscaped(val);						
						
					} else if (model.getProperty() == null && MetaDataEditorView.this.store.getChildCount(model) == 0) {
						if (model.getEntityTypeValue() != null) {
							StringBuilder html = new StringBuilder();
							GenericModelType propertyType = null;
							Object propertyValue = null;
							if (Predicate.T.isAssignableFrom(model.getEntityTypeValue())) {
								EntityType<?> predicateType = MetaDataEditorUtil.getPredicateEntityType(model.getEntityTypeValue());								
								Boolean predicateValue = getPredicateValue(predicateType, editGenericEntity);
								if (PredicateErasure.T.isAssignableFrom(model.getEntityTypeValue()))
									predicateValue = !predicateValue;
								propertyValue = predicateValue;
							} else {
								propertyType = GMF.getTypeReflection().getType(model.getEntityTypeValue().getTypeSignature());
								propertyValue = getMetaDataValue((EntityType<? extends MetaData>) model.getEntityTypeValue(), editGenericEntity);
							}
							html.append(MetaDataEditorUtil.prepareStringValue(propertyValue, propertyType, codecRegistry,
									readOnly || model.isPredicateMetaData(), false, useCase));								
							
							int count = MetaDataEditorView.this.store.getChildCount(model);
							if (count == 0 && context.getSubIndex() == 0)
								if (useGray)
									cellText = MetaDataEditorUtil.appendStringCell(MetaDataEditorUtil.appendStringGray(html.toString()));									
								else	
									cellText = MetaDataEditorUtil.appendStringCell(html.toString());
				   			    //sb.appendHtmlConstant(html.toString());
						} else {						
							//no property exist
							StringBuilder html = new StringBuilder();
							html.append("<div class='MetaDataEmptyValue' style='color:grey'>");
							String empty = SafeHtmlUtils.htmlEscape("<"+ LocalizedText.INSTANCE.noProperty() +">");
							html.append(empty);						
							html.append("</div>");
							if (useGray)
								cellText = MetaDataEditorUtil.appendStringCell(MetaDataEditorUtil.appendStringGray(html.toString()));								
							else	
								cellText = MetaDataEditorUtil.appendStringCell(html.toString());
							//sb.appendHtmlConstant(html.toString());
						}
					} else if (model.getEntityTypeValue() != null && model.getProperty() != null) {
						//show default property value
						StringBuilder html = new StringBuilder();
						Property prop = model.getProperty();
						//Object objectVal;
						if (prop != null) {
							GenericModelType propertyType = prop.getType();
							Object propertyValue = null;
							if (MetaDataEditorView.this.modelExpert instanceof EffectiveOverviewExpert) {
								html.append(MetaDataEditorUtil.prepareStringValue(propertyValue, propertyType, codecRegistry,
										readOnly || model.isPredicateMetaData(), false, useCase));									
							}
						}
						int count = MetaDataEditorView.this.store.getChildCount(model);
						if (count == 0 && context.getSubIndex() == 0)
							if (useGray)
								cellText = MetaDataEditorUtil.appendStringCell(MetaDataEditorUtil.appendStringGray(html.toString()));								
							else	
								cellText = MetaDataEditorUtil.appendStringCell(html.toString());
			   			    //sb.appendHtmlConstant(html.toString());
					}									
					
					if (model.isPredicateMetaData() || MetaDataEditorView.this.store.getChildCount(model) > 0 || useGray) 
						prepareNonEditableValueField(cellText, sb);
					else
						prepareEditableValueField(cellText, sb);
				}
				
				@Override
				public void onBrowserEvent(Cell.Context context, Element parent, Object object, NativeEvent event,
						ValueUpdater<Object> valueUpdater) {
					if (modelExpert instanceof EffectiveOverviewExpert)
						return;
					
					MetaDataEditorModel model = MetaDataEditorView.this.store.findModelWithKey(context.getKey().toString());
					if (model == null || !isModelEditable(model))
						return;
					
					EventTarget eventTarget = event.getEventTarget();
					if (!Element.is(eventTarget))
						return;
					
					String cls = Element.as(eventTarget).getClassName();
					
					if ((!model.isPredicateMetaData()) && (model.isAssignablePropertyType()) && (model.getProperty() != null) && (model.getMetaData() != null) && 
							(!specialFlowClasses.contains(((EntityType<?>) model.getProperty().getType()).getJavaType())))						
						if (event.getButton() == NativeEvent.BUTTON_LEFT) {
							if (editorGridInline.isEditing())
								editorGridInline.completeEditing();
							ChangeInstanceAction changeInstanceAction = getShortcutChangeInstanceAction();
							changeInstanceAction.updateState(transformSelection(Collections.singletonList(
									getModelPath(model, model.getMetaData().entityType().getProperty(model.getProperty().getName()), null))));
							changeInstanceAction.perform(null);						
						}
					
					super.onBrowserEvent(context, parent, object, event, valueUpdater);		
					
					if (model.getProperty() == null || model.getMetaData() == null) 
						return;
					Property prop = model.getProperty();
					PropertyPanelCss css = PropertyPanelResources.INSTANCE.css();
					if (cls.contains(css.checkedValue()) || cls.contains(css.uncheckedValue()) || cls.contains(css.checkNullValue())) {
						Boolean startValue = null;
						if (cls.contains(css.checkedValue()))
							startValue = true;
						else if (cls.contains(css.uncheckedValue()))
							startValue = false;
						Boolean newValue = startValue == null ? true : !startValue;							
						model.getMetaData().entityType().getProperty(prop.getName()).set(model.getMetaData(), newValue);
						MetaDataEditorView.this.store.update(model);
					}					
				}				
			});
			
			//column Declaring Model - Name
			this.ccModelName = new ColumnConfig<MetaDataEditorModel, MetaDataEditorModel>(props.declaredModel(), 150, LocalizedText.INSTANCE.labelDeclaredModel());
			this.ccModelName.setCellPadding(false);
			this.ccModelName.setSortable(false);
			this.ccModelName.setHidden(this.modelExpert instanceof EffectiveOverviewExpert);
			this.ccModelName.setCell(new AbstractCell<MetaDataEditorModel>() {
				@Override
				public void render(Cell.Context context, MetaDataEditorModel value, SafeHtmlBuilder sb) {
					if (value == null)
						return;
					
					GenericEntity owner = value.getOwner();
					if (owner == null)
						return;
					
					GmMetaModel metaModel = null;
					metaModel = getParentModel(owner);
					
					//Boolean useGray = !MetaDataEditorUtil.canEditMetaData(lastModelPath, value.getMetaData(), modelExpert instanceof DeclaredPropertyOverviewExpert);
					Boolean useGray = !isModelEditable(value); 					
					
					if (metaModel != null) {
						EntityType<GenericEntity> entityType = metaModel.entityType();
						String selectiveInformation = SelectiveInformationResolver.resolve(entityType, metaModel, (ModelMdResolver) null, useCase/*, null*/);
						if (selectiveInformation != null && !selectiveInformation.trim().equals("")) {
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
			this.ccOwnerName = new ColumnConfig<MetaDataEditorModel, MetaDataEditorModel>(props.declaredOwner(), 150, LocalizedText.INSTANCE.labelDeclaredEntity());
			this.ccOwnerName.setCellPadding(false);
			this.ccOwnerName.setSortable(false);
			this.ccOwnerName.setHidden(this.modelExpert instanceof EffectiveOverviewExpert);
			this.ccOwnerName.setCell(new AbstractCell<MetaDataEditorModel>() {
				@Override
				public void render(Cell.Context context, MetaDataEditorModel value, SafeHtmlBuilder sb) {
					if (value == null)
						return;
					
					GenericEntity owner = value.getOwner();
					/*
					if (value.getValue() instanceof GmProperty) 
						owner = ((GmProperty) value.getValue()).getDeclaringType();
					if (value.getValue() instanceof GmEnumConstant) 
						owner = ((GmEnumConstant) value.getValue()).getDeclaringType();
					*/
					//Boolean useGray = !MetaDataEditorUtil.canEditMetaData(lastModelPath, value.getMetaData(), modelExpert instanceof DeclaredPropertyOverviewExpert);
					Boolean useGray = !isModelEditable(value); 					

					if (owner != null) {
						EntityType<GenericEntity> entityType = owner.entityType();
						String selectiveInformation = SelectiveInformationResolver.resolve(entityType, owner, (ModelMdResolver) null, useCase);
						if (selectiveInformation != null && !selectiveInformation.trim().equals("")) {
							if (useGray) {
							    sb.appendHtmlConstant(MetaDataEditorUtil.appendStringCell(MetaDataEditorUtil.appendStringGray(selectiveInformation)));
							} else {					
								sb.appendHtmlConstant(MetaDataEditorUtil.appendStringCell(selectiveInformation));
							}
						}					    
					}
				}
			});
			
			//column Selector
			ccSelector = new ColumnConfig<MetaDataEditorModel, MetaDataSelector>(props.selector(), 200, LocalizedText.INSTANCE.labelSelector());
			ccSelector.setCellPadding(false);
			ccSelector.setSortable(false);
			ccSelector.setHidden(this.modelExpert instanceof EffectiveOverviewExpert);
			ccSelector.setCell(new AbstractCell<MetaDataSelector>("click") {
				@Override
				public void render(Cell.Context context, MetaDataSelector value, SafeHtmlBuilder sb) {
					String text = null;
					MetaDataEditorModel model = MetaDataEditorView.this.store.findModelWithKey(context.getKey().toString());
					//Boolean useGray = !MetaDataEditorUtil.canEditMetaData(lastModelPath, model.getMetaData(), modelExpert instanceof DeclaredPropertyOverviewExpert);
					Boolean useGray = !isModelEditable(model); 

					if (value != null) {
						if (useGray)
							text = MetaDataEditorUtil.appendStringCell(MetaDataEditorUtil.appendStringGray(getSelectorDisplayValue(value)));
						else	
							text = MetaDataEditorUtil.appendStringCell(getSelectorDisplayValue(value));
					}
										
					if (useGray)
						prepareNonEditableValueField(text, sb);
					else
						prepareEditableValueField(text, sb);				
					
				}
				
				@Override
				public void onBrowserEvent(Cell.Context context, Element parent, MetaDataSelector object, NativeEvent event,
						ValueUpdater<MetaDataSelector> valueUpdater) {
					MetaDataEditorModel model = MetaDataEditorView.this.store.findModelWithKey(context.getKey().toString());
					if (model == null || model.getMetaData() == null || !isModelEditable(model))
						return;
															
					if (!MetaDataEditorUtil.canEditMetaData(lastModelPath, model.getMetaData(), modelExpert instanceof DeclaredPropertyOverviewExpert))
						return;						
						
					if (event.getButton() == NativeEvent.BUTTON_LEFT) {
						if (editorGridInline.isEditing())
							editorGridInline.completeEditing();
						ChangeInstanceAction changeInstanceAction = getShortcutChangeInstanceAction();
						changeInstanceAction.updateState(transformSelection(
								Collections.singletonList(getModelPath(model, model.getMetaData().entityType().getProperty("selector"), object))));
						changeInstanceAction.perform(null);						
					}
					
					super.onBrowserEvent(context, parent, object, event, valueUpdater);		
				}
			});
			//column priority
			ccConflictPriority = new ColumnConfig<MetaDataEditorModel, Double>(props.conflictPriority(), 100, LocalizedText.INSTANCE.labelPriority());
			ccConflictPriority.setCellPadding(false);
			ccConflictPriority.setSortable(false);
			ccConflictPriority.setHidden(this.modelExpert instanceof EffectiveOverviewExpert);
			ccConflictPriority.setCell(new AbstractCell<Double>() {
				@Override
				public void render(Cell.Context context, Double value, SafeHtmlBuilder sb) {
					String text = null;
					MetaDataEditorModel md = MetaDataEditorView.this.store.findModelWithKey(context.getKey().toString());
					//Boolean useGray = !MetaDataEditorUtil.canEditMetaData(lastModelPath, md.getMetaData(), modelExpert instanceof DeclaredPropertyOverviewExpert);
					Boolean useGray = !isModelEditable(md); 

					if (MetaDataEditorView.this.store.getRootItems().contains(md) && value != null) {
						if (useGray)
							text = MetaDataEditorUtil.appendStringCell(MetaDataEditorUtil.appendStringGray(value.toString()));						
						else
							text = MetaDataEditorUtil.appendStringCell(value.toString());
					}

					if (useGray)
						prepareNonEditableValueField(text, sb);
					else
						prepareEditableValueField(text, sb);				
				}
			});
			cm = new ColumnModel<>(Arrays.<ColumnConfig<MetaDataEditorModel, ?>> asList(ccName, ccModelName, ccOwnerName, ccValue, ccSelector, ccConflictPriority));

			this.grid = new TreeGrid<MetaDataEditorModel>(this.store, cm, ccName);
			
			this.grid.setView(new TreeGridView<MetaDataEditorModel>() {
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
			});
			grid.getView().setColumnHeader(new ExtendedColumnHeader<>(grid, grid.getColumnModel()));
			
			this.grid.getView().setForceFit(true);
			this.grid.setBorders(false);
			this.grid.setStyleName("gmePropertyPanel");
			this.grid.setAllowTextSelection(false);
			this.grid.setHideHeaders(!true);
			this.grid.getView().setSortingEnabled(true);
			this.grid.getView().setTrackMouseOver(false);   //show before selected line on mouse Move
			/*
			this.grid.addClickHandler(new HeaderClickHandler() {				
				@Override
				public void onHeaderClick(HeaderClickEvent event) {
					int x = event.getEvent().getClientX();
					MetaDataEditorUtil.setHeaderBarColumnAutoResize(x, event);
				}
			});
			*/
			this.grid.getView().setViewConfig(new GridViewConfig<MetaDataEditorModel>() {
				@Override
				public String getRowStyle(MetaDataEditorModel model, int rowIndex) {
					return "";
				}
				
				@Override
				public String getColStyle(MetaDataEditorModel model, ValueProvider<? super MetaDataEditorModel, ?> valueProvider,
						int rowIndex, int colIndex) {
					return "gmeGridColumn";
				}
			});

			this.grid.addExpandHandler(event -> prepareEditors());
			this.grid.addCollapseHandler(event -> prepareEditors());
			
			this.grid.getTreeStore().addStoreAddHandler(event -> {
				for (MetaDataEditorModel model : event.getItems())
					addManipulationListener(model);
			});
			
			this.grid.getTreeStore().addStoreRemoveHandler(event -> removeManipulationListener(event.getItem()));
			this.grid.addHeaderClickHandler(event -> {
				SortDir sortDir;
				switch  (event.getColumnIndex()) {
				case 0: MetaDataEditorView.this.store.clearSortInfo();
						sortDir = (MetaDataEditorView.this.sortDirAscii == null) ? SortDir.ASC : ((MetaDataEditorView.this.sortDirAscii == SortDir.ASC) ? SortDir.DESC : SortDir.ASC);
						doSortByAscii(sortDir);					        
						break;
				case 1: MetaDataEditorView.this.store.clearSortInfo();
						sortDir = (MetaDataEditorView.this.sortDirDependency == null) ? SortDir.ASC : ((MetaDataEditorView.this.sortDirDependency == SortDir.ASC) ? SortDir.DESC : SortDir.ASC);
						doSortByDependency(sortDir);					        
				break;
				default:
					break;
				}
			});			
			
			this.editorGridInline = new MultiEditorGridInlineEditing<MetaDataEditorModel>(this.grid);
			this.editorGridInline.addBeforeStartEditHandler(event -> {
				event.getEditCell();
							
				//RVE - commented out, because addBeforeStartEditHandler is called twice - first time when click into field and 2nd time after set value via GIMA dialog, onCompleteEdit is called only once
				//than problems with reset old value in edit field
				//if (MetaDataEditorView.this.editionNestedTransaction != null)
				//	rollbackTransaction();
				//MetaDataEditorView.this.editionNestedTransaction = MetaDataEditorView.this.gmSession.getTransaction().beginNestedTransaction();
				
				ColumnConfig<MetaDataEditorModel, Object> columnId =  MetaDataEditorView.this.grid.getColumnModel().getColumn(event.getEditCell().getCol()); 
				
				if (columnId.equals(ccSelector)) {
					MetaDataEditorView.this.startValue = MetaDataEditorView.this.grid.getStore().get(event.getEditCell().getRow()).getSelector();
				} else if (columnId.equals(ccValue)) {
					MetaDataEditorView.this.startValue = MetaDataEditorView.this.grid.getStore().get(event.getEditCell().getRow()).getPropertyValue();
				} else if (columnId.equals(ccConflictPriority)) {
					MetaDataEditorView.this.startValue = MetaDataEditorView.this.grid.getStore().get(event.getEditCell().getRow()).getConflictPriority();
				}										
			});
			this.editorGridInline.addCompleteEditHandler(event -> {
				//event.getEditCell();
				//event.getSource().getEditableGrid().getStore().rejectChanges();

				
				if (MetaDataEditorView.this.editionNestedTransaction != null)
					rollbackTransaction();
				MetaDataEditorView.this.editionNestedTransaction = MetaDataEditorView.this.gmSession.getTransaction().beginNestedTransaction();
				
				IsField<?> editor = event.getSource().getEditor(MetaDataEditorView.this.grid.getColumnModel().getColumn(event.getEditCell().getCol()));
				/*
				new Timer() {
					@Override
					public void run() {
						MetaDataEditorView.this.grid.getStore().rejectChanges();
					}
				}.schedule(500);
				*/
				//gridInlineEditing.setCurrentRow(event.getEditCell().getRow()); needed?
				if (!GMEUtil.isEditionValid(editor.getValue(), MetaDataEditorView.this.startValue, editor)) {
					rollbackTransaction();
					return;
				}
				
				MetaDataEditorModel model = MetaDataEditorView.this.grid.getStore().get(event.getEditCell().getRow());
				ColumnConfig<MetaDataEditorModel, Object> columnId =  MetaDataEditorView.this.grid.getColumnModel().getColumn(event.getEditCell().getCol()); 
				Property property = null;
				
				if (columnId.equals(ccSelector)) {
					List<Property> propList1 = MetaDataEditorUtil.preparePropertyList(model.getMetaData().entityType(), MetaDataEditorView.this.baseType, false);
					for (Property prop1: propList1) {
						    if (prop1.getType().getTypeSignature().equals((MetaDataSelector.class).getName())) {
						    	property = prop1;
						    	break;
						    }
					}														
				} else if (columnId.equals(ccValue)) {
					property = model.getProperty();
				} else if (columnId.equals(ccConflictPriority)) {
					List<Property> propList2 = MetaDataEditorUtil.preparePropertyList(model.getMetaData().entityType(), MetaDataEditorView.this.baseType, false);
					for (Property prop2 : propList2) {
					    if (prop2.getName().equals("conflictPriority")) {
					    	property = prop2;
					    	break;
					    }
					}														
				}						
				setMetaDataProperty(property, model.getMetaData(), editor.getValue());	
				if (editor.getValue() instanceof GenericEntity)
					addEntityListener((GenericEntity) editor.getValue());
				
				MetaDataEditorView.this.editionNestedTransaction.commit();
				MetaDataEditorView.this.editionNestedTransaction = null;					
				MetaDataEditorView.this.startValue = null;					
				MetaDataEditorView.this.store.update(model);
			});

			this.gmEditorSupport = new GMEditorSupport();

			this.grid.setSelectionModel(new TreeGridSelectionModel<MetaDataEditorModel>() {
				@Override
				protected void onAdd(List<? extends MetaDataEditorModel> models) {
					super.onAdd(models);
					//RVE - moved code to public void onAdd(StoreAddEvent<MetaDataEditorModel> event)
					//this onAdd is called couple of times, also when resorting and than been created more Editors for same line but for diff type!!!
				}
				@Override
				protected void onSelectChange(MetaDataEditorModel model, boolean select) {
					super.onSelectChange(model, select);
					if (MetaDataEditorView.this.isVisible())
						MetaDataEditorView.this.gmSelectionListeners.fireListeners();
				}				
			});
			
			this.grid.setIconProvider(model -> MetaDataEditorResources.INSTANCE.clear());
			
		}
		return this.grid;
	}

	protected ChangeInstanceAction getShortcutChangeInstanceAction() {
		if (shortcutChangeInstanceAction == null) {
			shortcutChangeInstanceAction = new ChangeInstanceAction();
			shortcutChangeInstanceAction.setInstanceSelectionFutureProvider(selectionFutureProviderProvider);
			shortcutChangeInstanceAction.configureGmContentView(this.getMetaDataEditorPanel());
		}
		
		return shortcutChangeInstanceAction;
	}	
	private SafeHtmlBuilder prepareEditableValueField(String value, SafeHtmlBuilder sb) {
		String editableBoxCss = " " + MetaDataEditorResources.INSTANCE.constellationCss().editableBox();
		//String textBoxCss = (value != null) ? AssemblyPanelResources.INSTANCE.css().propertyValue() : MetaDataEditorResources.INSTANCE.constellationCss().emptyBox();
		String textBoxCss = (value != null) ? "" : MetaDataEditorResources.INSTANCE.constellationCss().emptyBox();
		sb.appendHtmlConstant("<div class='" + GMEUtil.PROPERTY_VALUE_CSS + " " + textBoxCss +
				editableBoxCss + "'>");
		if (value != null)
			sb.appendHtmlConstant(value);
		
		sb.appendHtmlConstant("</div>");
		return sb;
	}

	private SafeHtmlBuilder prepareNonEditableValueField(String value, SafeHtmlBuilder sb) {
		String textBoxCss = (value != null) ? "" : MetaDataEditorResources.INSTANCE.constellationCss().emptyBox();
		sb.appendHtmlConstant("<div class='" + GMEUtil.PROPERTY_VALUE_CSS + " " + textBoxCss + "'>");
		if (value != null)
			sb.appendHtmlConstant(value);
		
		sb.appendHtmlConstant("</div>");
		return sb;
	}

	private GmMetaModel getParentModel(GenericEntity owner) {
		GmMetaModel metaModel = null;
		if (owner instanceof GmCustomTypeInfo)   //GmEntityType, GmEnumType
			metaModel = ((GmCustomTypeInfo) owner).getDeclaringModel();
		else if (owner instanceof GmPropertyInfo) 
			metaModel = ((GmPropertyInfo) owner).declaringModel();
		else if (owner instanceof GmEnumConstantInfo) 
			metaModel = ((GmEnumConstantInfo) owner).declaringModel();
		else if (owner instanceof GmMetaModel)
			metaModel = (GmMetaModel) owner;
		return metaModel;
	}
	
	protected void setMetaDataProperty(Property property, MetaData metaData, Object value) {
		if ((this.editGmMetaModel == null) || this.editGmMetaModel.equals(this.declaringGmMetaModel)) {
			//RVE - edit on base GmModel
			property.set(metaData, value);
			return;
		} else if ((this.editGmEntityType == null) || ((this.declaringGmEntityType != null) && (this.declaringGmEntityType.equals(this.editGmEntityType)))) {
			//RVE - edit on base GmEntityType
			property.set(metaData, value);
			return;			
		} else {
			//RVE - set MetaData on Editing GmModel/GmEntityType
			Object object = this.lastModelPathElement.getValue();
			if (object instanceof GmEntityType) {
				setGmEntityTypeMetaDataProperty(property, metaData, value, object);												
			} else if (object instanceof GmEnumType) {
				setGmEnumTypeMetaDataProperty(property, metaData, value, object);								
			} else if (object instanceof GmProperty) {
				setGmPropertyMetaDataProperty(property, metaData, value, object);												
			} else if (object instanceof GmEnumConstant) {
				//setGmEnumConstantMetaDataProperty(property, metaData, value, object);												
			} else {
				property.set(metaData, value);
			}			
		}
	}

	private void setGmPropertyMetaDataProperty(Property property, MetaData metaData, Object value, Object object) {
		GmPropertyOverride editingOverride = null;
		for (GmPropertyOverride propertyOverrides : this.editGmEntityType.getPropertyOverrides()) {
			if (object.equals(propertyOverrides.getProperty())) {
				editingOverride = propertyOverrides;
				break;
			}
		}
		if (editingOverride == null) {
			editingOverride = this.gmSession.create(GmPropertyOverride.T);					
			editingOverride.setProperty((GmProperty) object);
			editingOverride.setInitializer(this.editGmEntityType);
			this.editGmEntityType.getPropertyOverrides().add(editingOverride);
		}
		
		MetaData editingMetaData = null;		
		for (MetaData propertyMetaData : editingOverride.getMetaData()) {					
			if (GMF.getTypeReflection().getType(propertyMetaData).getTypeSignature().equals(GMF.getTypeReflection().getType(metaData).getTypeSignature())) {
				editingMetaData = propertyMetaData;
				break;
			}
		}

		if (editingMetaData == null)
			//RVE - change
			editingMetaData = metaData;
			//return;
		
		List<Property> propList = MetaDataEditorUtil.preparePropertyList(editingMetaData.entityType(), MetaDataEditorView.this.baseType, false);
		for (Property prop : propList) {
			if (prop.getName().equals(property.getName())) {
				prop.set(editingMetaData, value);
				break;
			}
		}
	}

	/*private void setGmEnumConstantMetaDataProperty(Property property, MetaData metaData, Object value, Object object) {
		//GmEnumConstantOverride editingOverride = null;
		
		//TODO - RVE - question where get the list of GmEnumConstantOverride for GmEnumType like   this.editGmEntityType.getPropertyOverrides()???
	}*/
	
	
	private void setGmEnumTypeMetaDataProperty(Property property, MetaData metaData, Object value, Object object) {
		GmEnumTypeOverride editingOverride = null;				
		for (GmCustomTypeOverride customOverride : this.editGmMetaModel.getTypeOverrides()) {
			if (customOverride.isGmEnumOverride() && object == customOverride.addressedType()) {
				editingOverride = (GmEnumTypeOverride) customOverride;
				break;
			}
		}
		if (editingOverride == null) {
			editingOverride = this.gmSession.create(GmEnumTypeOverride.T);					
			editingOverride.setEnumType((GmEnumType) object);
			editingOverride.setDeclaringModel(this.editGmMetaModel);
			this.editGmMetaModel.getTypeOverrides().add(editingOverride);
		}
		
		MetaData editingMetaData = null;		
		for (MetaData enumMetaData : editingOverride.getMetaData()) {					
				if (GMF.getTypeReflection().getType(enumMetaData).getTypeSignature().equals(GMF.getTypeReflection().getType(metaData).getTypeSignature())) {
					editingMetaData = enumMetaData;
					break;
				}
			}
		
		if (editingMetaData == null) {
			editingMetaData = metaData.clone(new StandardCloningContext());
			editingOverride.getMetaData().add(editingMetaData);
		}
		
		List<Property> propList = MetaDataEditorUtil.preparePropertyList(editingMetaData.entityType(), MetaDataEditorView.this.baseType, false);
		for (Property prop : propList) {
			if (prop.getName().equals(property.getName())) {
				prop.set(editingMetaData, value);
				break;
			}
		}
	}

	private void setGmEntityTypeMetaDataProperty(Property property, MetaData metaData, Object value, Object object) {
		GmEntityTypeOverride editingOverride = null;				
		for (GmCustomTypeOverride customOverride : this.editGmMetaModel.getTypeOverrides()) {
			if (customOverride.isGmEntityOverride() && object == customOverride.addressedType()) {
				editingOverride = (GmEntityTypeOverride) customOverride;
				break;
			}
		}
		if (editingOverride == null) {
			editingOverride = this.gmSession.create(GmEntityTypeOverride.T);					
			editingOverride.setEntityType((GmEntityType) object);
			editingOverride.setDeclaringModel(this.editGmMetaModel);
			editingOverride.setId(((GmEntityType) object).getId());
			this.editGmMetaModel.getTypeOverrides().add(editingOverride);
		}
		
		MetaData editingMetaData = null;		
		if (this.modelExpert instanceof DeclaredPropertyOverviewExpert) {
			for (MetaData entityMetaData : editingOverride.getPropertyMetaData()) {					
				if (GMF.getTypeReflection().getType(entityMetaData).getTypeSignature().equals(GMF.getTypeReflection().getType(metaData).getTypeSignature())) {
					editingMetaData = entityMetaData;
					break;
				}
			}					
		} else {
			for (MetaData entityMetaData : editingOverride.getMetaData()) {					
				if (GMF.getTypeReflection().getType(entityMetaData).getTypeSignature().equals(GMF.getTypeReflection().getType(metaData).getTypeSignature())) {
					editingMetaData = entityMetaData;
					break;
				}
			}
		}
		
		if (editingMetaData == null) {
			editingMetaData = metaData.clone(new StandardCloningContext());
			if (this.modelExpert instanceof DeclaredPropertyOverviewExpert) 
				editingOverride.getPropertyMetaData().add(editingMetaData);
			else
				editingOverride.getMetaData().add(editingMetaData);
		}
		
		List<Property> propList = MetaDataEditorUtil.preparePropertyList(editingMetaData.entityType(), MetaDataEditorView.this.baseType, false);
		for (Property prop : propList) {
			if (prop.getName().equals(property.getName())) {
				prop.set(editingMetaData, value);
				break;
			}
		}
	}

	@Override
	public String getCaption() {
		return this.caption;
	}

	@Override
	public void setCaption(String caption) {
		this.caption = caption;
	}

	public void setModelExpert(MetaDataEditorExpert modelExpert) {
		this.modelExpert = modelExpert;
	}
	
	public void setUseVisibleFilter(Boolean useFilter) {
		this.useVisibleFilter = useFilter;
	}
	
	@Override
	public boolean getUseSessionResolver() {
		return ((this.modelExpert instanceof DeclaredPropertyOverviewExpert) || (this.modelExpert instanceof DeclaredOverviewExpert));
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

	     updateFilteredModels();		
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
	// ----- MetaDataEditorProvider -----

	@Override
	public boolean getEditorVisible(ModelPathElement pathElement) {
		if (pathElement == null)
			return true;
		
		if (this.modelExpert instanceof EffectiveOverviewExpert) {
			if (pathElement.getValue() instanceof GmMetaModel)
				return true;
			else
				return false;						
		}
		
		if (!(this.modelExpert instanceof DeclaredPropertyOverviewExpert)) 
			return true;

		if (!(pathElement.getValue() instanceof GmEntityType) && !(pathElement.getValue() instanceof GmEntityTypeOverride)) 
			return false;
		
		return true;
	}
	
	@Override
	public void setContent(final ModelPath modelPath) {
		setContentGrid(modelPath);		
	}

	@Override
	public Boolean isSearchMode() {
		return this.isSearchMode;
	}
	
	@Override
	public void applySearchFilter(String searchText, Boolean useSearchDeclaredTypesOnly) {
		String lowerText = searchText.toLowerCase();
		this.searchText = searchText;
		this.useSearchDeclaredTypesOnly = useSearchDeclaredTypesOnly;
		this.isSearchMode = ((!searchText.isEmpty()) && (!searchText.equals("*")));

		this.store.setEnableFilters(false);	
		/*
		for (MetaDataEditorModel model : this.store.getAll()) {
			if (searchText == null)
				model.setIsVisible(true);
			else	
				model.setIsVisible(getItemName(model).toLowerCase().contains(searchText.toLowerCase()) || searchText.equals("*") || searchText.isEmpty());
			this.store.update(model);
		}
		*/
		int row = 0; 
		for (MetaDataEditorModel model : MetaDataEditorView.this.store.getAll()) {								
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
				model.setIsVisible(founded);
			}
			row = row + 1;
			//this.store.update(model);
		}			
		this.store.setEnableFilters(true);	
	}
	
	@Override
	public String getSearchDeclaredText() {
		return null;
	}	
	
	private String getItemName (MetaDataEditorModel model) {
		String itemName = null;
		MetaData metaData = model.getMetaData();
		if (metaData != null) {
			//CascadingMetaDataResolver cmdResolver = getGmSession().getModelAccessory().getCascadingMetaDataResolver();
			if (MetaDataEditorView.this.store.getRootItems().contains(model)) {
				Name name = getMetaData(metaData).entity(metaData).meta(Name.T).exclusive();
				if (name != null && name.getName() != null) {
					itemName = I18nTools.getLocalized(name.getName());
				} else if (model.getEntityTypeValue() != null) {
					//sb.appendEscaped(value.getEntityTypeValue().getTypeShortName());
					itemName = model.getEntityTypeValue().getShortName();
				} else {
					itemName = GMF.getTypeReflection().getEntityType(model.getType().getTypeSignature()).getShortName();
				}
			} else if (model.getProperty() != null) {
				itemName = model.getProperty().getName();
			}
		} else if (model.getEntityTypeValue() != null) {
			if (MetaDataEditorView.this.store.getRootItems().contains(model)) {
				itemName = model.getEntityTypeValue().getShortName();
			} else if (model.getProperty() != null) {
				itemName = model.getProperty().getName();
			}						
		}
		return itemName;
	}
	
	
	private void setContentGrid(final ModelPath modelPath) {
		//check if all metaData property are already loaded
		ModelPathElement pathElement = (modelPath != null) ? modelPath.last() : null;
		
		this.lastModelPath = modelPath;
		this.lastModelPathElement = pathElement;
		this.declaringGmMetaModel = null;
		this.editGmMetaModel = null;
		this.declaringGmEntityType = null;
		this.editGmEntityType = null;
		this.editGmEnumType = null;
		this.editGenericEntity = null;
		if (modelPath != null)
			for (ModelPathElement element : modelPath) {
				if (element.getValue() instanceof GmMetaModel) {
					this.editGmMetaModel = element.getValue();
				}
				else if (element.getValue() instanceof GmEntityType) {
					this.editGmEntityType = element.getValue();
				}
				else if (element.getValue() instanceof GmEnumType) {
					this.editGmEnumType = element.getValue();
				}
			}

		/*
		if (!needUpdate || !getActive()) {
			return;
		} else {
			needUpdate = false;
		}		
		*/
		if ((this.editorGridInline != null) && (this.editorGridInline.isEditing()))
			this.editorGridInline.completeEditing();
		
		if (this.editionNestedTransaction != null)
			rollbackTransaction();
		
		if (pathElement == null){
			return;
		}
		
		//lastListUseCase = listUseCase;
		//lastListRole = listRoles;
		//lastListAccess = listAccess;
				
		this.store.clear();	
		modelId = 0;		
		Object object = pathElement.getValue();
		
		this.grid.mask();
		setVisibleColumns(object);		
		
		if (object instanceof GmCustomTypeInfo) {
			//GmEntityType and GmEntityTypeOverride and GmEnumType and GmEnumTypeOverride
			GmCustomTypeInfo typeInfo = (GmCustomTypeInfo) object;
			Set<MetaData> metaData = typeInfo.getMetaData();
			this.declaringGmMetaModel = typeInfo.getDeclaringModel();
			if ((metaData != null) && (metaData instanceof EnhancedCollection)) {
				if (((EnhancedCollection) metaData).isIncomplete())  {
					new Timer() {
						@Override
						public void run() {
							setContent(modelPath);
						}
					}.schedule(100);
					return;
				} 
			}			

			if (!(MetaDataEditorView.this.modelExpert instanceof EffectiveOverviewExpert)) {
				this.gmSession.listeners().entity(typeInfo).remove(this);
				this.gmSession.listeners().entity(typeInfo).add(this);
			}
						
			if (object instanceof GmEnumTypeInfo) {
				this.baseType = EnumTypeMetaData.T;
				if (object instanceof GmEnumType)
					this.editGenericEntity = (GmEnumType) object;
				else if (object instanceof GmEnumTypeOverride)
					this.editGenericEntity = ((GmEnumTypeOverride) object).getEnumType();
			} else if (object instanceof GmEntityTypeInfo) {
				this.baseType = EntityTypeMetaData.T;
				if (object instanceof GmEntityType)
					this.editGenericEntity = (GmEntityType) object;
				else if (object instanceof GmEntityTypeOverride)
					this.editGenericEntity = ((GmEntityTypeOverride) object).getEntityType();
			}						
			
			if (this.modelExpert != null) {
				this.modelExpert.provide((GenericEntity) object, this.editGmMetaModel, null, new MetaDataEditorExpert.CallbackExpertResultType() {
					@Override
					public void onSuccess(Collection<MetaDataEditorExpertResultType> mdList) {					
						addModelsToGrid(mdList);
					}

				});	
			
			}		
		} else if (object instanceof GmPropertyInfo) {	
			//GmProperty and GmPropertyOverride
			GmPropertyInfo propertyInfo = (GmPropertyInfo) object;
			GmProperty property = null;
			Set<MetaData> metaData = propertyInfo.getMetaData();
			if (propertyInfo instanceof GmProperty) {
				property = (GmProperty) propertyInfo;
				this.declaringGmEntityType = ((GmProperty) propertyInfo).getDeclaringType();
			} else if (propertyInfo instanceof GmPropertyOverride) {
				property = ((GmPropertyOverride) propertyInfo).getProperty(); 
				this.declaringGmEntityType = property.getDeclaringType();				
			}
			this.declaringGmMetaModel = this.declaringGmEntityType.getDeclaringModel();
			this.editGenericEntity = property;
			
			if (metaData instanceof EnhancedCollection) {
				if (((EnhancedCollection) metaData).isIncomplete()) {
					new Timer() {
						@Override
						public void run() {
							setContent(modelPath);
						}
					}.schedule(100);
					return;
				}
			}			

			if (!(MetaDataEditorView.this.modelExpert instanceof EffectiveOverviewExpert)) {
				this.gmSession.listeners().entity(propertyInfo).remove(this);
				this.gmSession.listeners().entity(propertyInfo).add(this);
				if (property!= null && !property.equals(propertyInfo)) {
					this.gmSession.listeners().entity(property).remove(this);
					this.gmSession.listeners().entity(property).add(this);
				}
			}
			
			this.baseType = PropertyMetaData.T;
			
			if (this.modelExpert != null) {
				this.modelExpert.provide((GenericEntity) object, this.editGmMetaModel, this.editGmEntityType, new MetaDataEditorExpert.CallbackExpertResultType() {
					@Override
					public void onSuccess(Collection<MetaDataEditorExpertResultType> mdList) {					
						addModelsToGrid(mdList);
					}

				});
			}			
		} else if (object instanceof GmEnumConstantInfo) {	
			//GmEnumConstant and GmEnumConstantOverride
			GmEnumConstantInfo enumConstantInfo = (GmEnumConstantInfo) object;
			GmEnumConstant enumConstant = null;
			Set<MetaData> metaData = enumConstantInfo.getMetaData();
			if (enumConstantInfo instanceof GmEnumConstant) {
				enumConstant = (GmEnumConstant) enumConstantInfo;
				this.declaringGmEnumType = ((GmEnumConstant) enumConstantInfo).getDeclaringType();
			} else if (enumConstantInfo instanceof GmEnumConstantOverride) {
				enumConstant = ((GmEnumConstantOverride) enumConstantInfo).getEnumConstant(); 
				this.declaringGmEnumType = enumConstant.getDeclaringType();				
			}
			this.declaringGmMetaModel = this.declaringGmEnumType.getDeclaringModel();
			this.editGenericEntity = enumConstant;
			
			if (metaData instanceof EnhancedCollection) {
				if (((EnhancedCollection) metaData).isIncomplete()) {
					new Timer() {
						@Override
						public void run() {
							setContent(modelPath);
						}
					}.schedule(100);
					return;
				}
			}			

			if (!(MetaDataEditorView.this.modelExpert instanceof EffectiveOverviewExpert)) {
				this.gmSession.listeners().entity(enumConstantInfo).remove(this);
				this.gmSession.listeners().entity(enumConstantInfo).add(this);
				if (enumConstant!= null && !enumConstant.equals(enumConstantInfo)) {
					this.gmSession.listeners().entity(enumConstantInfo).remove(this);
					this.gmSession.listeners().entity(enumConstantInfo).add(this);
				}
			}
			
			this.baseType = EnumConstantMetaData.T;

			if (this.modelExpert != null) {
				this.modelExpert.provide((GenericEntity) object, this.editGmMetaModel, this.editGmEnumType, new MetaDataEditorExpert.CallbackExpertResultType() {
					@Override
					public void onSuccess(Collection<MetaDataEditorExpertResultType> mdList) {					
						addModelsToGrid(mdList);
					}

				});
			}			
		} else if (object instanceof GmMetaModel) {		
			this.declaringGmMetaModel = (GmMetaModel) object;
			this.editGmMetaModel = (GmMetaModel) object;
			this.editGenericEntity = (GmMetaModel) object;
			
			Set<MetaData> metaData = ((GmMetaModel) object).getMetaData();
			if (metaData instanceof EnhancedCollection) {
				if (((EnhancedCollection) metaData).isIncomplete())  {
					new Timer() {
						@Override
						public void run() {
							setContent(modelPath);
						}
					}.schedule(100);
					return;
				} 
			}			
			if (!(MetaDataEditorView.this.modelExpert instanceof EffectiveOverviewExpert)) {
				this.gmSession.listeners().entity((GmMetaModel) object).remove(this);
				this.gmSession.listeners().entity((GmMetaModel) object).add(this);
			}

			this.baseType = ModelMetaData.T;
			
			if (this.modelExpert != null) {
				this.modelExpert.provide((GenericEntity) object, new MetaDataEditorExpert.CallbackExpertResultType() {
					@Override
					public void onSuccess(Collection<MetaDataEditorExpertResultType> mdList) {					
						addModelsToGrid(mdList);
					}

				});
			}			
		}
		
		this.grid.unmask();

		if (!(MetaDataEditorView.this.modelExpert instanceof EffectiveOverviewExpert)) {
			if (this.editGmMetaModel != null) {
				addEntityListener(this.editGmMetaModel);
				addEntityPropertyListener(this.editGmMetaModel, "typeOverrides" );
				addEntityPropertyListener(this.editGmMetaModel, "types" );
				if (!this.editGmMetaModel.getTypeOverrides().isEmpty()) {
					for (GmCustomTypeOverride typeOverride : this.editGmMetaModel.getTypeOverrides()) {
						if (typeOverride instanceof GmEntityTypeOverride) {
							if (((GmEntityTypeOverride) typeOverride).getEntityType().equals(object) || typeOverride.equals(object) || 
							(this.editGmEntityType != null && ((GmEntityTypeOverride) typeOverride).getEntityType().equals(this.editGmEntityType))) {
								addEntityListener(typeOverride);
								//addEntityPropertyListener(typeOverride, "metaData" );						
								//addEntityPropertyListener(typeOverride, "properties" );		
								//addEntityPropertyListener(typeOverride, "propertyMetaData" );		
								//addEntityPropertyListener(typeOverride, "propertyOverrides" );		
								if (((GmEntityTypeOverride) typeOverride).getPropertyOverrides() != null && !((GmEntityTypeOverride) typeOverride).getPropertyOverrides().isEmpty())
									for (GmPropertyOverride propertyOverride : ((GmEntityTypeOverride) typeOverride).getPropertyOverrides()) {
								//		if (propertyOverride.getProperty().equals(object) || propertyOverride.equals(object))
											addEntityListener(propertyOverride);
											//addEntityPropertyListener(propertyOverride, "metaData" );
									}
							}
						} else if (typeOverride instanceof GmEnumTypeOverride) {			
							//if (((GmEnumTypeOverride) typeOverride).getEnumType().equals(object) || typeOverride.equals(object) ||
							//(this.editGmEnumType != null && ((GmEnumTypeOverride) typeOverride).getEnumType().equals(this.editGmEnumType)))
								addEntityListener(typeOverride);
								//addEntityPropertyListener(typeOverride, "metaData" );
						}
					}				
				}
			}
			if (this.editGmEntityType != null) {
				addEntityListener(this.editGmEntityType);
				//addEntityPropertyListener(this.editGmEntityType, "metaData" );
				//addEntityPropertyListener(this.editGmEntityType, "propertyOverrides" );
				//addEntityPropertyListener(this.editGmEntityType, "propertyMetaData" );
				//addEntityPropertyListener(this.editGmEntityType, "properties" );
				if (this.editGmEntityType.getPropertyOverrides() != null && !this.editGmEntityType.getPropertyOverrides().isEmpty()) {
					for (GmPropertyOverride propertyOverride : this.editGmEntityType.getPropertyOverrides()) {
				//		if (propertyOverride.getProperty().equals(object) || propertyOverride.equals(object))
							addEntityListener(propertyOverride);
							//addEntityPropertyListener(propertyOverride, "metaData" );
					}
				}
			}
			if (this.editGmEnumType != null) {
				addEntityListener(this.editGmEnumType);
				//addEntityPropertyListener(this.editGmEnumType, "metaData" );
				//addEntityPropertyListener(this.editGmEnumType, "constants" );
				//addEntityPropertyListener(this.editGmEnumType, "enumConstantMetaData" );
			}
		}
		
		
		if (!this.store.getAll().isEmpty()) {
			//if (this.modelExpert instanceof EffectiveOverviewExpert)
			if (ccModelName.isHidden())
				doSortByAscii(SortDir.ASC);				
			else				
				doSortByDependency(SortDir.ASC);
			selectFirstItem();
		}			
	}

	private void setVisibleColumns(Object object) {
		this.ccOwnerName.setHidden(object instanceof GmMetaModel || object instanceof GmEnumConstantInfo || object instanceof GmPropertyInfo || modelExpert instanceof EffectiveOverviewExpert);
		this.ccModelName.setHidden(object instanceof GmEnumConstantInfo || object instanceof GmPropertyInfo || modelExpert instanceof EffectiveOverviewExpert);				
		this.grid.reconfigure(this.grid.getTreeStore(), cm, this.ccName);		
	}

	private void prepareEditors() {
		this.editorGridInline.clearEditors();

		if (MetaDataEditorView.this.readOnly) 
			return;
		
		//for (MetaDataEditorModel model : this.store.getAll()) {
		//for (MetaDataEditorModel model : this.grid.getTreeStore().getAll()) {
		for (MetaDataEditorModel model : this.grid.getStore().getAll()) {
			
			if (!isModelEditable(model))
				continue;
			
			//int rowIndex = store.getAll().indexOf(model);
			//int rowIndex = this.grid.getTreeStore().getAll().indexOf(model);
			int rowIndex = this.grid.getStore().getAll().indexOf(model);
			if (model.getProperty() != null) {
				//edit property value
				if (!((model.getEntityTypeValue() != null) && Predicate.T.isAssignableFrom(model.getEntityTypeValue()))) 
					prepareCustomEditor(model, model.getProperty(), ccValue, rowIndex);
			}
															
			//edit Selector, ConflictPriority			
			if (model.getMetaData() != null) {
				List<Property> propList = MetaDataEditorUtil.preparePropertyList(model.getMetaData().entityType(), MetaDataEditorView.this.baseType, false);
				for (Property property : propList) {
					if (property.getType().getTypeSignature().equals((MetaDataSelector.class).getName())) 
				    	prepareCustomEditor( model, property, ccSelector, rowIndex);
					if (property.getName().equals("conflictPriority")) 
				    	prepareCustomEditor( model, property, ccConflictPriority, rowIndex);														    
				}
			}
		}
	}

	private void addEntityTypeModelsToGrid(Collection<EntityType<?>> mdList) {
		int id = 0;
		for (final EntityType<?> entityType : mdList) {						
			//fill GridStore with MetaData
			List<Property> propList = MetaDataEditorUtil.preparePropertyList(entityType, this.baseType, true);
			
			MetaDataEditorModel rootModel;
			if (propList.size() > 1) {
				rootModel = new MetaDataEditorModel(id++, null, entityType, null, null);
				//rootModel.setIsVisible(true);
				MetaDataEditorView.this.store.add(rootModel);
				for (Property prop : propList) {
					MetaDataEditorModel childModel = new MetaDataEditorModel(id++, null, entityType, prop, null);
					//childModel.setIsVisible(true);
					childModel.setChildLevel(1);
					MetaDataEditorView.this.store.add(rootModel, childModel);	
					//MetaDataEditorView.this.store.update(childModel);
				}							
				//MetaDataEditorView.this.store.update(rootModel);
			} else {
                if (!propList.isEmpty()) {	
                	for (Property prop : propList) {
						rootModel = new MetaDataEditorModel(id++, null, entityType, prop, null);
						//rootModel.setIsVisible(true);
						MetaDataEditorView.this.store.add(rootModel);
						//MetaDataEditorView.this.store.update(rootModel);
                	}
                } else {
					rootModel = new MetaDataEditorModel(id++, null, entityType, null, null);
					//rootModel.setIsVisible(true);
					MetaDataEditorView.this.store.add(rootModel);
					//MetaDataEditorView.this.store.update(rootModel);
                }
			}
		}
		updateFilteredModels();
		
		if (MetaDataEditorView.this.store.getRootCount() > 0)
			MetaDataEditorView.this.grid.getSelectionModel().select(0, true);
			 
	}		
	
	private void addModelsToGrid(Collection<MetaDataEditorExpertResultType> mdList) {
		for (final MetaDataEditorExpertResultType resultType : mdList) {	
			MetaData md = resultType.getMetaData();
			List<Property> propList = MetaDataEditorUtil.preparePropertyList(md.entityType(), this.baseType, false);	
			
			//addEntityListener(md);						
			//check if Values for MetaData are loaded, if not then load all absent property value
			final List<Property> absentProperties = new ArrayList<>();		
			for (Property prop : propList)  {							
				if (GMEUtil.isPropertyAbsent(md, prop))
					absentProperties.add(prop);									
			}		
			
			if (!absentProperties.isEmpty()) {
				EntityType<GenericEntity> entityType = md.entityType();							
				loadAbsentProperties(md, entityType, absentProperties) //
						.andThen(result -> {
							MetaDataEditorView.this.entitiesLoading.remove(md);
							MetaDataEditorView.this.store.setEnableFilters(false);
							for (MetaDataEditorModel model : MetaDataEditorView.this.store.getAll()) {
								if (model.getMetaData() == md) {
									MetaDataEditorView.this.store.update(model);
								}
							}
							MetaDataEditorView.this.store.setEnableFilters(true);
							if (MetaDataEditorView.this.entitiesLoading.isEmpty())
								updateFilteredModels();
						}).onError(e -> {
							MetaDataEditorView.this.entitiesLoading.remove(md);
							if (MetaDataEditorView.this.entitiesLoading.isEmpty())
								updateFilteredModels();

							ErrorDialog.show("Error load MetaData Editor Properties", e);
							e.printStackTrace();
						});
			}						
			
			//fill GridStore with MetaData
			final List<Object> propAndObjectList = MetaDataEditorUtil.preparePropertyAndPredicateList(md.entityType(), this.baseType, true);
			
			MetaDataEditorModel rootModel;
			MetaDataEditorModel childModel;
			if (propAndObjectList.size() > 1) {
				rootModel = new MetaDataEditorModel(modelId++, md, null, null, resultType.getOwner());
				//rootModel.setIsVisible(true);
				MetaDataEditorView.this.store.add(rootModel);	
				List<MetaDataEditorModel> childList = new ArrayList<>();
				for (Object prop : propAndObjectList) {
					if (prop instanceof Property) {
						//childModel = new MetaDataEditorModel(prop.hashCode(), md, prop);
						childModel = new MetaDataEditorModel(modelId++, md, null, (Property) prop, resultType.getOwner());
						childList.add(childModel);
						//MetaDataEditorView.this.store.add(rootModel, childModel);	
						//MetaDataEditorView.this.store.update(childModel);
					}
				}	
				if (!childList.isEmpty()) {
					MetaDataEditorView.this.store.add(rootModel, childList);	
					//MetaDataEditorView.this.store.update(rootModel);
				}
			} else {
                if (!propAndObjectList.isEmpty()) {	//size=1
                	Object object = propAndObjectList.get(0);
                	if (object instanceof Property)	{
						rootModel = new MetaDataEditorModel(modelId++, md, null, (Property) object, resultType.getOwner());
						//rootModel.setIsVisible(true);
						MetaDataEditorView.this.store.add(rootModel);
						//MetaDataEditorView.this.store.update(rootModel);
                	} else if (object instanceof EntityType<?>) {
						rootModel = new MetaDataEditorModel(modelId++, md, (EntityType<?>) object, null, resultType.getOwner());
						//rootModel.setIsVisible(true);
						MetaDataEditorView.this.store.add(rootModel);
						//MetaDataEditorView.this.store.update(rootModel);                		
                	}                	
                } else {
					rootModel = new MetaDataEditorModel(modelId++, md, null, null, resultType.getOwner());
					//rootModel.setIsVisible(true);
					MetaDataEditorView.this.store.add(rootModel); 
					//MetaDataEditorView.this.store.update(rootModel);
                }
			}
		}
		updateFilteredModels();
		
		this.grid.getView().refresh(true);
		
		if (MetaDataEditorView.this.store.getRootCount() > 0)
			MetaDataEditorView.this.grid.getSelectionModel().select(0, true);
			 
	}	
	
	private Future<Void> loadAbsentProperties(final GenericEntity entity, EntityType<GenericEntity> entityType, List<Property> absentProperties) {
		final Future<Void> future = new Future<Void>();
		
		MultiLoader multiLoader = new MultiLoader();
		multiLoader.setParallel(false);
		int i = 0;
		for (Property property : absentProperties) {
			multiLoader.add(Integer.toString(i++),
					GMEUtil.loadAbsentProperty(entity, entityType, property, gmSession, useCase, codecRegistry, specialEntityTraversingCriterion));
		}
		
		this.entitiesLoading.add(entity);
		
		multiLoader.load(AsyncCallbacks.of(result -> future.onSuccess(null), future::onFailure));
		return future;
	}
	
	private Boolean getPredicateValue(EntityType<?> predicateType, GenericEntity entity) {
		Boolean predicateValue = false;
		if (predicateType == null)
			return predicateValue;
		
		if (entity instanceof GmMetaModel)
			predicateValue = MetaDataEditorView.this.metaDataResolverProvider.getModelMetaDataContextBuilder().is((EntityType<? extends Predicate>) predicateType);
		else if (entity instanceof GmEntityType)
			predicateValue = MetaDataEditorView.this.metaDataResolverProvider.getModelMetaDataContextBuilder().entityType((GmEntityType) entity).is((EntityType<? extends Predicate>) predicateType);
		else if (entity instanceof GmEnumType)
			predicateValue = MetaDataEditorView.this.metaDataResolverProvider.getModelMetaDataContextBuilder().enumType((GmEnumType) entity).is((EntityType<? extends Predicate>) predicateType);
		else if (entity instanceof GmProperty) {
			if (this.editGmEntityType != null)
				predicateValue = MetaDataEditorView.this.metaDataResolverProvider.getModelMetaDataContextBuilder().entityType(this.editGmEntityType).property((GmProperty) entity).is((EntityType<? extends Predicate>) predicateType);
			else
				predicateValue = MetaDataEditorView.this.metaDataResolverProvider.getModelMetaDataContextBuilder().entityType(this.declaringGmEntityType).property((GmProperty) entity).is((EntityType<? extends Predicate>) predicateType);				
		} else if (entity instanceof GmEnumConstant) {
            if (this.editGmEnumType != null)
				predicateValue = MetaDataEditorView.this.metaDataResolverProvider.getModelMetaDataContextBuilder().enumType(this.editGmEnumType).constant((GmEnumConstant) entity).is((EntityType<? extends Predicate>) predicateType);            	
            else
				predicateValue = MetaDataEditorView.this.metaDataResolverProvider.getModelMetaDataContextBuilder().enumType(this.declaringGmEnumType).constant((GmEnumConstant) entity).is((EntityType<? extends Predicate>) predicateType);            	            	
		}
						
		return predicateValue;
	}	

	private Object getMetaDataValue(EntityType<? extends MetaData> metaData, GenericEntity entity) {
		Object metaDataValue = null;
		if (entity instanceof GmMetaModel)
			metaDataValue = MetaDataEditorView.this.metaDataResolverProvider.getModelMetaDataContextBuilder().meta(metaData).exclusive();			
		else if (entity instanceof GmEntityType)
			metaDataValue = MetaDataEditorView.this.metaDataResolverProvider.getModelMetaDataContextBuilder().entityType((GmEntityType) entity).meta(metaData).exclusive();
		else if (entity instanceof GmEnumType)
			metaDataValue = MetaDataEditorView.this.metaDataResolverProvider.getModelMetaDataContextBuilder().enumType((GmEnumType) entity).meta(metaData).exclusive();
		else if (entity instanceof GmProperty) {
			if (this.editGmEntityType != null)
				metaDataValue = MetaDataEditorView.this.metaDataResolverProvider.getModelMetaDataContextBuilder().entityType(this.editGmEntityType).property((GmProperty) entity).meta(metaData).exclusive();
			else
				metaDataValue = MetaDataEditorView.this.metaDataResolverProvider.getModelMetaDataContextBuilder().entityType(this.declaringGmEntityType).property((GmProperty) entity).meta(metaData).exclusive();				
		} else if (entity instanceof GmEnumConstant) {
            if (this.editGmEnumType != null)
            	metaDataValue = MetaDataEditorView.this.metaDataResolverProvider.getModelMetaDataContextBuilder().enumType(this.editGmEnumType).constant((GmEnumConstant) entity).meta(metaData).exclusive();            	
            else
            	metaDataValue = MetaDataEditorView.this.metaDataResolverProvider.getModelMetaDataContextBuilder().enumType(this.declaringGmEnumType).constant((GmEnumConstant) entity).meta(metaData).exclusive();            	            	
		}
						
		return metaDataValue;
	}	
	
	private void addEntityPropertyListener(GenericEntity entity, String property) {
		if (entity == null) 
           return;
		
    	this.gmSession.listeners().entityProperty(entity, property).remove(this);		
    	this.gmSession.listeners().entityProperty(entity, property).add(this);
	}
	
	private void addEntityListener(GenericEntity entity) {
		if (entity == null) 
           return;
		
    	this.gmSession.listeners().entity(entity).remove(this);		
    	this.gmSession.listeners().entity(entity).add(this);
	}
	
	private void addManipulationListener(MetaDataEditorModel model) {
		if (model != null && model.getMetaData() != null) {
			this.gmSession.listeners().entity(model.getMetaData()).add(this);
		}
	}
	
	private void removeManipulationListener(MetaDataEditorModel model) {
		if (model != null && model.getMetaData() != null) {
			this.gmSession.listeners().entity(model.getMetaData()).remove(this);
		}		
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
						//		MetaDataEditorView.this.gmSession.getModelAccessory().getCascadingMetaDataResolver(), MetaDataEditorView.this.useCase);
						final PropertyQuery propertyQuery = GMEUtil.getPropertyQuery((PersistentEntityReference) entityType.getReference(entity), property.getPropertyName(),
								null, getSpecialTraversingCriterion(property.getPropertyType().getJavaType()), false,
								MetaDataEditorView.this.gmSession.getModelAccessory().getCascadingMetaDataResolver(), MetaDataEditorView.this.useCase);
						
						final ProfilingHandle ph = Profiling.start(getClass(), "Querying property '" + property.getPropertyName() + "' in PP", true);
						MetaDataEditorView.this.gmSession.query().property(propertyQuery).result(new com.braintribe.processing.async.api.AsyncCallback<PropertyQueryResultConvenience>() {
							@Override
							public void onSuccess(PropertyQueryResultConvenience propertyQueryResult) {
								ph.stop();
								ProfilingHandle ph1 = Profiling.start(getClass(), "Handling property query in MDE", false);
								GmSessionException exception = null;
								try {
									PropertyQueryResult result = propertyQueryResult.result();
									MetaDataEditorView.this.gmSession.suspendHistory();
									Object value = result != null ? result.getPropertyValue() : null;
									value = GMEUtil.transformIfSet(value, property.getPropertyName(), entityType);
									
									if (value instanceof EnhancedCollection) {
										((EnhancedCollection) value).setIncomplete(result.getHasMore());
									}
									
									Function<Class<?>, Codec<Object, String>> codecsProvider = new Function<Class<?>, Codec<Object,String>>(MetaDataEditorView.this.valueRenderers);
									ProfilingHandle ph2 = Profiling.start(getClass(), "Setting new property value in the entity in MDE", false);
									entityType.setPropertyValue(entity, property.getPropertyName(), GMEUtil.sortIfSet(value, propertyQuery,
											MetaDataEditorView.this.gmSession, MetaDataEditorView.this.useCase, codecsProvider));
									ph2.stop();
								} catch (GmSessionException e) {
									exception = e;
								} finally {
									MetaDataEditorView.this.gmSession.resumeHistory();
									
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

	private TraversingCriterion getSpecialTraversingCriterion(Class<?> clazz) {
		if (specialEntityTraversingCriterion != null) {
			return specialEntityTraversingCriterion.get(clazz);
		}
		return null;
	}	
	*/
		
	@Override
	public boolean isSelected(Object element) {
		List<MetaDataEditorModel> selectedModels = this.grid.getSelectionModel().getSelectedItems();
		if (selectedModels == null || selectedModels.isEmpty()) 
			return false;

		for (MetaDataEditorModel selectedModel : selectedModels) {
			if (selectedModel.refersTo(element)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean isSelectionActive() {
		return this.grid.getSelectionModel().getSelectedItems().size() > 0;
	}	
	
    //used in Add Action for example - send Model, Entity or Property, but problbly better get MetaData of it	
	@Override
	public Set<ModelPath> getContent() {
		Set<ModelPath> modelPaths = new HashSet<ModelPath>();
				
		ModelPath modelPath = getMetaDataPropertyModelPath();					
		modelPaths.add(modelPath);
		
		if (!(this.modelExpert instanceof EffectiveOverviewExpert)) 
			return modelPaths;
			
		MetaDataEditorModel model = this.grid.getSelectionModel().getSelectedItem();
		if (model != null) {
			Object value = model.getMetaData();
			if (value != null) {
				modelPath = getModelPathForEditorModel(value);
				modelPaths.add(modelPath);
			}
		}			
		return modelPaths;
	}

	private ModelPath getModelPathForEditorModel(Object value) {
		ModelPath modelPath;
		//modelPath = new ModelPath();
		modelPath = lastModelPath.copy();
		
		//GMF.getTypeReflection().getTypeSignature(value)
		GenericModelType type = GMF.getTypeReflection().getType(value);
		if (type == null) {
		  throw new RuntimeException("This should not be reachable, if this code was really needed, there is some problem!");
		}
		RootPathElement pathElement;
		if (modelExpert instanceof EffectiveOverviewExpert) {	
			pathElement = new RootPathElement(type, null);
		} else {
			pathElement = new RootPathElement(type, value);
		}
		modelPath.add(pathElement);           //add MetaData
		return modelPath; 
	}
		
	//used in ResolutionView only
	@Override
	public ModelPath getExtendedSelectedItem() {
		return getExtendedSelectedModelPath(true);
	}
		
	//used for example in ActionBar for showing Actions
	@Override
	public ModelPath getFirstSelectedItem() {
		ModelPath modelPath = null;
		
		MetaDataEditorModel model = this.grid.getSelectionModel().getSelectedItem();
		if (model == null) {
			//set MetaData Collection into path
			if (this.modelExpert instanceof DeclaredOverviewExpert || this.modelExpert instanceof DeclaredPropertyOverviewExpert) {
				modelPath = getMetaDataPropertyModelPath();
				return modelPath;
			}
		} else {
			Object value = model.getMetaData();
			if (value != null) 
				modelPath = getModelPathForEditorModel(value);
		}		
				
		return modelPath;
	}

	private void selectFirstItem() {
		if (!this.store.getAll().isEmpty()) {
			new Timer() {
				@Override
				public void run() {
					if (MetaDataEditorView.this.isVisible() && MetaDataEditorView.this.grid.isVisible())
						grid.focus();
					grid.getSelectionModel().select(0, false);
				}
			}.schedule(100);			
		}
	}
	
	private ModelPath getMetaDataPropertyModelPath() {
		if (lastModelPathElement == null || !(lastModelPathElement.getValue() instanceof GenericEntity)) 
			return null;
		
		GenericEntity entity = (GenericEntity) lastModelPathElement.getValue();		
		Property property;		
		if ((this.modelExpert instanceof EffectiveOverviewExpert) || (this.modelExpert instanceof DeclaredOverviewExpert)) {
			property = entity.entityType().getProperty("metaData");			
		} else if (this.modelExpert instanceof DeclaredPropertyOverviewExpert) {
			property = entity.entityType().getProperty("propertyMetaData");
		} else {
			return null;
		}
		
		Object object = property.get(entity);		
		if (object == null)	
		  return null;

		ModelPath modelPath = lastModelPath.copy();
		
		//type = GMF.getTypeReflection().getGenericModelType(Object.getEntityType();
		PropertyPathElement pathElement = new PropertyPathElement(entity, property, object);
		modelPath.add(pathElement);     													
		return modelPath;
	}

	private ModelPath getExtendedSelectedModelPath(Boolean useExtendedModelPath) {		
		MetaDataEditorModel model = this.grid.getSelectionModel().getSelectedItem();
		if (model == null) 
			return null;
		
		ModelPath modelPath = new ModelPath();
		GenericModelType type = null;
		RootPathElement pathElement = null;

		//add MetaDataClass info
		//Class<? extends MetaData> metaDataClassType = null;
		//if (model.getMetaData() != null) {
		//	metaDataClassType = model.getMetaData().getClass();
		//}
		if (model.getMetaData() != null && useExtendedModelPath) {
			type = GMF.getTypeReflection().getType(model.getMetaData());
			if (type == null) {
				  throw new RuntimeException("This should not be reachable, if this code was really needed, there is some problem!");
			}
			
			if (modelExpert instanceof EffectiveOverviewExpert)
				//pathElement = new RootPathElement(type, model.getMetaData().getClass()); //in Effective tab there is only list of possible MetaData (MetaData physically doesnot exists)
				pathElement = new RootPathElement(type, model.getEntityTypeValue());
			else 
				pathElement = new RootPathElement(type, model.getMetaData()); 
			modelPath.add(pathElement);     
		}
		
		/*
		Object value = model.getMetaData();
		type = GMF.getTypeReflection().getGenericModelType(value.getEntityType();
		pathElement = new RootPathElement(type, value);
		modelPath.add(pathElement);           //add EntityMetaData
		*/

		if (this.lastModelPathElement == null) 
			return modelPath;
		
		//add actual used entity (GmEntityType, GmProperty, GmMetaModel...)
		
		for (ModelPathElement lastPathElement : this.lastModelPath) {
			modelPath.add(lastPathElement.copy());
		}
		
		/*
		type = GMF.getTypeReflection().getType(this.lastModelPathElement.<Object>getValue());
		if (type == null) {
			  throw new RuntimeException("This should not be reachable, if this code was really needed, there is some problem!");
		}
		pathElement = new RootPathElement(type, this.lastModelPathElement.getValue());
		modelPath.add(pathElement);  
		*/
		return modelPath;
	}

	@Override	
	public Boolean isActionManipulationAllowed() {
		if (this.modelExpert != null) {
			if (this.modelExpert instanceof EffectiveOverviewExpert) {
				return false;
			}
		}
		
		return true;
	}	
	
	@Override
	public HandlerRegistration addRowClickHandler(RowClickHandler handler) {
		return this.grid.addRowClickHandler(handler);
	}

	// ----- Internal Members -----

	/*
	private List<Property> preparePropertyList(Class<? extends MetaData> entityClass, Boolean useOnlyClassSpecial) {
		if (this.baseTypes == null) {
			this.baseTypes = new ArrayList<String>();
			for (Property property : this.baseTypeClass).getProperties())
				this.baseTypes.add(property.getPropertyName());
		}
		EntityType<GenericEntity> entityType = entityClass);
		List<Property> result = new ArrayList<Property>();
		for (Property property : entityType.getProperties()) {
			if (useOnlyClassSpecial) {
				//add only special property (not inherited from base class EntityTypeMetaData )
				if (this.baseTypes.indexOf(property.getPropertyName()) < 0) {
					result.add(property);
				} else {
					//special for DisplayInfo - get also Name from basic Types
					if (property != null && property.getPropertyName().equals("name")) {
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
	
	@SuppressWarnings("rawtypes")
	private void prepareCustomEditor(final MetaDataEditorModel model, Property property, ColumnConfig<MetaDataEditorModel, ?> columnConfig, int rowIndex) {
	    
		if (property == null || model == null || model.getMetaData() == null || readOnly)
			return;
		
		this.editorGridInline.setCurrentRow(rowIndex);
		PropertyFieldContext context = new PropertyFieldContext();		
		GenericModelType propertyType = property.getType();
		MetaData metaData = model.getMetaData();
		Object value = property.get(metaData);
		
		if (propertyType instanceof BaseType) {
			if (value != null)
				propertyType = ((BaseType) propertyType).getActualType(value);
		}
		if (propertyType instanceof EntityType) {
			EntityType<?> propertyEntityType = (EntityType<?>) propertyType;
			context.setModelType(propertyEntityType);
		} else {
			context.setModelType(propertyType);
		}
								
		int maxLength = -1;
		EntityMdResolver entityContextBuilder = getMetaData(metaData).entity(metaData).useCase(this.useCase);
		String propertyName = property.getName();
		PropertyMdResolver propertyMdResolver = entityContextBuilder.property(propertyName);
		MaxLength maxLengthMeta = propertyMdResolver.meta(MaxLength.T).exclusive();
		if (maxLengthMeta != null)
			maxLength = ((Long) maxLengthMeta.getLength()).intValue();

		//context.setPassword(propertyModel.getPassword());		
		context.setRegex(propertyMdResolver.meta(Pattern.T).exclusive());
		context.setUseAlternativeField(/* model.getFlow() */false);
		context.setUseCase(this.useCase);
		context.setGmSession(this.gmSession);
		context.setVirtualEnum(propertyMdResolver.meta(VirtualEnum.T).exclusive());
		context.setMaxLenght(maxLength);
		context.setParentEntity(model.getMetaData());
		//context.setParentEntityType(model.getParentEntityType());
		context.setPropertyName(propertyName);
		IsField<?> field = this.gmEditorSupport.providePropertyField(context);
		if (field == null)
			return;
		
		field.asWidget().addStyleName("metaDataField");
		
		if (field instanceof TriggerFieldAction) {
			if (field instanceof TriggerField<?>)
				((TriggerField<?>) field).setHideTrigger(true);
			((TriggerFieldAction) field).setGridInfo(this.editorGridInline, new GridCell(rowIndex, this.grid.getColumnModel().getColumns().indexOf(columnConfig)));
			// if (actionManager == null || !(field instanceof SimplifiedEntityField)) {
			// if (triggerFieldActionModelMap == null)
			// triggerFieldActionModelMap = new HashMap<MetaDataEditorModel, TriggerFieldAction>();
			// triggerFieldActionModelMap.put(model, (TriggerFieldAction) field);
			// }
		} 		
		if (field instanceof SimplifiedEntityField) {
			((SimplifiedEntityField) field).configureGmSession(this.gmSession);
			((SimplifiedEntityField) field).configureUseCase(this.useCase);
			((SimplifiedEntityField) field).configureGmContentView(this.getMetaDataEditorPanel());
			((SimplifiedEntityField) field).setEmptyText(LocalizedText.INSTANCE.assign());
			if (propertyType instanceof EntityType) {
				EntityQuery simplifiedEntityQuery = null;
				//EntityQuery simplifiedEntityQuery = EntityQueryBuilder.from(property.getEntityType().where().property("name").eq("root").done();
				EntityType<?> propertyEntityType = (EntityType<?>) propertyType;
				((SimplifiedEntityField) field).setInitialConfiguration(
						new SimplifiedEntityFieldConfiguration(propertyEntityType, simplifiedEntityQuery, this.gmSession, this.useCase, true,
								LocalizedText.INSTANCE.assignProperty(GMEMetadataUtil.getPropertyDisplay(propertyName, propertyMdResolver))));
			}
			// ((SimplifiedEntityField) field).configureGmContentView(this);
		}						
				
		this.editorGridInline.addEditor(columnConfig, (IsField) field, /* model.getFlow() */true);
	}
		
	private String getSelectorDisplayValue (MetaDataSelector selector) {
		if (selector == null) 
			return "";
		
		StringBuilder builder = new StringBuilder();
	    String defaultString = selector.toString();
		EntityType<?> type = selector.entityType();
		String selectiveInformation = SelectiveInformationResolver.resolve(type, selector, (ModelMdResolver) null,
				this.useCase/* , null */);
		if (selectiveInformation != null && !selectiveInformation.trim().equals("")) {
			defaultString =  selectiveInformation;
		}
					
		if (selector instanceof UseCaseSelector) {			
			builder.append(LocalizedText.INSTANCE.displayUseCase()).append(": ").append(((UseCaseSelector) selector).getUseCase()) ;	
		} else if (selector instanceof RoleSelector) {
			if (!builder.toString().isEmpty()) 
				builder.append(" && ");			
			builder.append(LocalizedText.INSTANCE.displayRole()).append(": ") .append(((RoleSelector) selector).getRoles());	
		} else if (selector instanceof AccessSelector) {
			if (!builder.toString().isEmpty()) 
				builder.append(" && ");
			builder.append(LocalizedText.INSTANCE.displayAccess()).append(": ").append(((AccessSelector) selector).getExternalId());	
		}
		
		if (builder.toString().isEmpty()) 
			builder.append(defaultString);
		
		return builder.toString();
	}
	
	private Boolean isModelEditable(MetaDataEditorModel model) {
		Boolean canEdit = MetaDataEditorUtil.canEditMetaData(lastModelPath, model.getMetaData(), modelExpert instanceof DeclaredPropertyOverviewExpert);		
		if (!canEdit)
			return canEdit;
		
		GenericEntity owner = model.getOwner();
		if (owner instanceof GmEntityTypeOverride)
			owner = ((GmEntityTypeOverride) owner).getEntityType();
		if (owner instanceof GmEnumTypeOverride)
			owner = ((GmEnumTypeOverride) owner).getEnumType();
		if (owner instanceof GmEnumConstantOverride)
			owner = ((GmEnumConstantOverride) owner).getEnumConstant();
		if (owner instanceof GmPropertyOverride)
			owner = ((GmPropertyOverride) owner).getProperty();
		
		return (editGenericEntity.equals(owner) && !(this.modelExpert instanceof EffectiveOverviewExpert) && canEdit);
	}
	
	private String getSelectorValueFromList (List<String> listString, MetaDataSelector selector) {		
		if (selector == null)
			return ""; 
		
		StringBuilder builder = new StringBuilder();
		if (listString != null) {
			int i = 0;
			for (String value : listString) {
				if (i == 0  && selector instanceof UseCaseSelector) {
					if (!value.isEmpty()) {					
						if (!builder.toString().isEmpty()) 
							builder.append(" && ");
						builder.append(value);
					} else 
						builder.append(getSelectorDisplayValue(selector));
				} else if (i == 1 && selector instanceof RoleSelector && !value.isEmpty()) {
					if (!value.isEmpty()) {					
						if (!builder.toString().isEmpty()) 
							builder.append(" && ");
						builder.append(value);
					} else 
						builder.append(getSelectorDisplayValue(selector));						
				} else if (i == 2 && selector instanceof AccessSelector && !value.isEmpty()) {
					if (!value.isEmpty()) {					
						if (!builder.toString().isEmpty()) 
							builder.append(" && ");
						builder.append(value);
					} else 
						builder.append(getSelectorDisplayValue(selector));	
				}				
				i++;
			}
		}		
		return builder.toString();
	}
	
	private void doSortByAscii(SortDir sortDir) {
		this.store.setEnableFilters(false);
		this.store.clearSortInfo();
		this.store.addSortInfo(new StoreSortInfo<MetaDataEditorModel>(props.model(), (arg1, arg2) -> {
			 //TODO  - RVE - need fix problem of sorting with grouping root childs (hierarchy)
							
			String name1 = "";
			String name2 = "";
						
			name1 = getComparatorName(arg1, name1);	
			name2 = getComparatorName(arg2, name2);				
			return name1.compareTo(name2);				
		}
		, sortDir));	
		this.sortDirAscii = sortDir;
		this.store.setEnableFilters(true);
		prepareEditors();
	}	
	
	private void doSortByDependency(SortDir sortDir) {
		this.store.setEnableFilters(false);
		this.store.clearSortInfo();
		this.store.addSortInfo(new StoreSortInfo<MetaDataEditorModel>(props.declaredModel(), (arg1, arg2) -> {
			if (arg1 == null) 
				return -1;
			 else if(arg2 == null) 
				return 1;
			
			GenericEntity entity1 = (arg1.getOwner() == null) ? editGmMetaModel : getParentModel(arg1.getOwner()); 
			GenericEntity entity2 = (arg2.getOwner() == null) ? editGmMetaModel : getParentModel(arg2.getOwner());
			if (entity1 == null) 
				return -1;
			else if(entity2 == null)
				return 1;
			
			Object obj = editGmMetaModel;
			
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
			name1 = getComparatorName(arg1, "");																
			name2 = getComparatorName(arg2, "");						
			return name1.compareTo(name2);					
		}
		, sortDir));		
		this.sortDirDependency = sortDir;
		this.store.setEnableFilters(true);
		prepareEditors();
	}
	
	private String getComparatorName(MetaDataEditorModel arg1, String name1) {
		if (arg1.getMetaData() != null) {
			//CascadingMetaDataResolver cmdResolver = getGmSession().getModelAccessory().getCascadingMetaDataResolver();
			if (MetaDataEditorView.this.store.getRootItems() != null && MetaDataEditorView.this.store.getRootItems().contains(arg1)) {
				Name name = getMetaData(arg1.getMetaData()).entity(arg1.getMetaData()).meta(Name.T).exclusive();
				if (name != null && name.getName() != null) {
					name1 = I18nTools.getLocalized(name.getName());
				} else {
					if (arg1.getEntityTypeValue() != null)
						name1 = arg1.getEntityTypeValue().getShortName();
					else
						name1 = getShortName(arg1.getType().getTypeName());
				}
			} else if (arg1.getProperty() != null) {
				name1 = arg1.getProperty().getName();
			}
		} else if (arg1.getEntityTypeValue() != null) {
			if (MetaDataEditorView.this.store.getRootItems() != null && MetaDataEditorView.this.store.getRootItems().contains(arg1)) {
				name1 = arg1.getEntityTypeValue().getShortName();
			} else if (arg1.getProperty() != null) {
				name1 =  arg1.getProperty().getName();
			}						
		}
		return name1;
	}								
	
	private String getShortName(String fullName) {
		int index = fullName.lastIndexOf(".");
		if (index > 0) {
			index ++;
			return fullName.substring(index);			
		} else 
			return fullName;
	}
	
	private void updateFilteredModels() {
	     if ( this.store.getAll().isEmpty()) {
	    	 //show empty info panel
	    	 exchangeWidget(getEmptyPanel());
	     } else {
	    	 //show list
	    	 exchangeWidget(this.gridWidget);	    	 
	     }
	     
		 //this.store.addFilter(this.storeVisibleFilter);			 		
		 //this.store.setEnableFilters(true);
	}
		
	private void exchangeWidget(Widget widget) {
		if (this.currentWidget != widget) {
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
		} catch (TransactionException e) {
			//ErrorDialog.show(LocalizedText.INSTANCE.errorRollingEditionBack(), e);
			e.printStackTrace();
		} catch (IllegalStateException ex) {
			//Nothing to do: the PP was used within some widget which rolled back the parent transaction already. This may happen within GIMA when canceling it while editing.
		}
		this.editionNestedTransaction = null;
	}
		
	@Override
	public void noticeManipulation(final Manipulation manipulation) {
		if (manipulation instanceof PropertyManipulation) {
			//Object parentObject = GMEUtil.getParentObject((PropertyManipulation) manipulation);
			GenericEntity entity = null;
			//String propertyName = null;
			Owner manipulationOwner = ((PropertyManipulation) manipulation).getOwner();
			if (manipulationOwner instanceof LocalEntityProperty) {
				entity = ((LocalEntityProperty) manipulationOwner).getEntity();
				//propertyName = ((LocalEntityProperty) manipulationOwner).getPropertyName();
			}
			
			if (entity == null)
				return; 
			
			//Removed the checks which made no sense...
			//if ((editGmMetaModel != null && (entity.equals(editGmMetaModel) || entity.equals(editGmMetaModel.getTypeOverrides()))) ||
					//(editGmEntityType != null && (entity.equals(editGmEntityType) || entity.equals(editGmEntityType.getPropertyOverrides()))) ||
					//entity instanceof GmPropertyOverride || entity instanceof GmCustomTypeOverride)	{
			if ((editGmMetaModel != null && entity.equals(editGmMetaModel)) || (editGmEntityType != null && entity.equals(editGmEntityType))
					|| entity instanceof GmPropertyOverride || entity instanceof GmCustomTypeOverride) {
				 //refreshRow(MetaDataEditorView.this.grid.getView(), indexInGrid);	
					switch (manipulation.manipulationType()) {
					case CHANGE_VALUE:
					case ADD:
					case REMOVE:
					case CLEAR_COLLECTION:
						doTimerUpdate();
						break;
					default:
						break;
					}								
			} else if (entity.equals(MetaDataEditorView.this.lastModelPathElement.getValue())) {
                    //is changed
					switch (manipulation.manipulationType()) {
					case CHANGE_VALUE:
					case ABSENTING:
						break;
					case ADD:
					case REMOVE:
					case CLEAR_COLLECTION:
						doTimerUpdate();
						break;
					default:
						break;
					}														
			} else if (entity instanceof MetaData || entity instanceof Selector) {
				//MetaData and Selector
				List<MetaDataEditorModel> listUpdateModel = new ArrayList<MetaDataEditorModel>();
				for (MetaDataEditorModel model : MetaDataEditorView.this.grid.getTreeStore().getAll()) {
				     if ((model.getMetaData() != null && model.getMetaData().equals(entity)) || (model.getSelector() != null && model.getSelector().equals(entity))) {
						 //int indexInGrid = MetaDataEditorView.this.grid.getStore().indexOf(model);
						 //refreshRow(MetaDataEditorView.this.grid.getView(), indexInGrid);	
							switch (manipulation.manipulationType()) {
							case CHANGE_VALUE:
							case ADD:
							case REMOVE:
							case CLEAR_COLLECTION:
								if (MetaDataEditorView.this.grid.getTreeStore().getParent(model) == null) {
									if (!listUpdateModel.contains(model))
										listUpdateModel.add(model);									
								} else {
									if (manipulationOwner.getPropertyName().equals(model.getProperty().getName()))
										if (!listUpdateModel.contains(model))
											listUpdateModel.add(model);																			
								}
								break;
							default:
								break;
							}														
						 
				    	 //MetaDataEditorView.this.grid.getTreeStore().update(model);
				     }
				}
				if (!listUpdateModel.isEmpty()) {
					doTimerRefresh();
					//RVE - MetaDataEditorView.this.grid.getTreeStore().update() call is very SLOW!!!!
										
					//for (MetaDataEditorModel model : listUpdateModel) {
					//	MetaDataEditorView.this.grid.getTreeStore().update(model);
					//}
					//updateFilteredModels();
				}
			} 
		} else if (ManipulationType.DELETE.equals(manipulation.manipulationType())) {
		    //is deleted
			MetaDataEditorView.this.grid.getTreeStore().clear();
			updateFilteredModels();
		}		
	}

	private void doTimerUpdate() {	
		if (!updateTimer.isRunning())
			updateTimer.schedule(10);			
	}

	private void doTimerRefresh() {	
		if (!refreshTimer.isRunning())
			refreshTimer.schedule(10);			
	}

	private native void refreshRow(GridView<MetaDataEditorModel> view, int row) /*-{
		view.@com.sencha.gxt.widget.core.client.grid.GridView::refreshRow(I)(row);
	}-*/;

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

	protected ModelPath getModelPath(MetaDataEditorModel model, Property property, Object value)  {
		if (model == null || model.getMetaData() == null)
			return null;
		
		ModelPath modelPath = new ModelPath();
		GenericEntity modelParentEntity = model.getMetaData();
		EntityType<GenericEntity> modelParentEntityType = model.getMetaData().entityType();
		RootPathElement rootPathElement = new RootPathElement(modelParentEntityType, modelParentEntity);
		modelPath.add(rootPathElement);
				
		if (property != null) {
			PropertyPathElement propertyPathElement = new PropertyPathElement(modelParentEntity, property, value);
			modelPath.add(propertyPathElement);
		}
		
		return modelPath;
	}
}
