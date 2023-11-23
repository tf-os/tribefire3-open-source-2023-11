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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.async.client.MultiLoader;
import com.braintribe.gwt.codec.registry.client.CodecRegistry;
import com.braintribe.gwt.genericmodelgxtsupport.client.GMEditorSupport;
import com.braintribe.gwt.genericmodelgxtsupport.client.PropertyFieldContext;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.TriggerFieldAction;
import com.braintribe.gwt.gme.propertypanel.client.field.SimplifiedEntityField;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelCss;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelResources;
import com.braintribe.gwt.gmview.client.GmInteractionListener;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
//import com.braintribe.gwt.gmview.client.IconAndType;
import com.braintribe.gwt.gmview.client.IconProvider;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gxt.gxtresources.gridwithoutlines.client.GridWithoutLinesAppearance.GridWithoutLinesResources;
import com.braintribe.gwt.gxt.gxtresources.gridwithoutlines.client.GridWithoutLinesAppearance.GridWithoutLinesStyle;
import com.braintribe.gwt.gxt.gxtresources.multieditor.client.MultiEditorGridInlineEditing;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
//import com.braintribe.gwt.metadataeditor.client.MetaDataEditorUtil;
import com.braintribe.gwt.metadataeditor.client.resources.MetaDataEditorResources;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.Predicate;
import com.braintribe.model.meta.data.constraint.Max;
import com.braintribe.model.meta.data.constraint.MaxLength;
import com.braintribe.model.meta.data.constraint.Min;
import com.braintribe.model.meta.data.constraint.MinLength;
import com.braintribe.model.meta.data.constraint.Pattern;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.data.prompt.VirtualEnum;
import com.braintribe.model.processing.meta.cmd.builders.ConstantMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.EnumMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.meta.cmd.extended.EntityRelatedMdDescriptor;
import com.braintribe.model.processing.meta.cmd.extended.EnumRelatedMdDescriptor;
import com.braintribe.model.processing.meta.cmd.extended.MdDescriptor;
import com.braintribe.model.processing.session.api.common.GmSessions;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.api.transaction.TransactionException;
import com.braintribe.model.selector.Selector;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
//import com.google.gwt.user.client.ui.HTML;
//import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.form.IsField;
import com.sencha.gxt.widget.core.client.form.TriggerField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.Grid.GridCell;
import com.sencha.gxt.widget.core.client.grid.GridViewConfig;
import com.sencha.gxt.widget.core.client.grid.GroupingView;
import com.sencha.gxt.widget.core.client.tips.QuickTip;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;

public class MetaDataEditorResolutionView extends ContentPanel implements ManipulationListener {

	private static final String emptyStringImageString = AbstractImagePrototype.create(PropertyPanelResources.INSTANCE.nullIcon()).getHTML()
			.replaceFirst("style='", "qtip='" + com.braintribe.gwt.gme.propertypanel.client.LocalizedText.INSTANCE.empty() + "' style='");
	private static final String loadingImageString = AbstractImagePrototype.create(PropertyPanelResources.INSTANCE.loading()).getHTML()
			.replaceFirst("style='", "qtip='" + com.braintribe.gwt.gme.propertypanel.client.LocalizedText.INSTANCE.loadingAbsentProperty() + "' style='");
	
	private IconProvider iconProvider;
	private PersistenceGmSession gmSession;
	private String useCase;
	//private GenericEntity parentEntity;
	//private EntityType<GenericEntity> parentEntityType;
	//private String defaultGroup;
	private Grid<MetaDataEditorResolutionModel> resolutionViewGrid;
	private boolean showGridLines = false;
	private boolean readOnly = false;
	private MultiEditorGridInlineEditing<MetaDataEditorResolutionModel> gridInlineEditing;
	private GMEditorSupport gmEditorSupport;
	private static final Integer VALUE_INDEX = 2;
	private boolean editorsReady = false;
	private boolean startEditingPending = false;
	private List<GmInteractionListener> gmInteractionListeners;
	private List<GmSelectionListener> gmSelectionListeners;
	private NestedTransaction editionNestedTransaction;
	private Object startValue;
	private Map<Class<?>, TraversingCriterion> specialEntityTraversingCriterion;
	
	interface ResolutionViewProperty extends PropertyAccess<MetaDataEditorResolutionModel> {
		ModelKeyProvider<MetaDataEditorResolutionModel> id();
		ValueProvider<MetaDataEditorResolutionModel, Object> value();
	}
	private static ResolutionViewProperty props = GWT.create(ResolutionViewProperty.class);	
		
	private final int propertyNameColumnWidth = 150;
	private final Set<Class<?>> specialFlowClasses;
	//private boolean prepareColspan = false;
	//private boolean removeTd = false;
	//private boolean forceGroups = true;
	private final List<String> specialUiElementsStyles;
	//private ColumnConfig<MetaDataEditorResolutionModel, String> groupColumn;
	private CodecRegistry<String> codecRegistry;
	private ModelPath lastModelPath = null;
	private ModelMdResolver lastModelContextBuilder = null;
	//private Set<String> listUseCase = null;
	//private Set<String> listRoles = null;
	//private Set<String> listAccess = null;
	/*
	private final Image iconImage = new Image();
	private final Label nameLabel = new Label();
	private final Label fullEntityTypeLabel = new Label();
	*/
	//private Label entityTypeLabel = new Label();
	//private ModelMetaDataContextBuilder modelMetaDataContextBuilder = null;
	private MetaDataResolverProvider metaDataResolverProvider = null;
	private static int gppId = 0;
	//private HTML emptyPanel;
	//private final String emptyTextMessage = LocalizedText.INSTANCE.noResolutionAvailable(); 
	//private Widget currentWidget = null;
	VerticalLayoutContainer container = null;
	
	public MetaDataEditorResolutionView() {
		super();
		gppId++;
		setHeaderVisible(false);
		setBorders(false);
		setBodyBorder(false);
		
		container = new VerticalLayoutContainer();
		container.setHeight(100);
		
		StringBuilder html = new StringBuilder();
		html.append("<html><body><table style='margin-top: 16px; height: 40px;'><tr><td class='gxtReset' style='vertical-align: top;'><div id='generalPropertyPanelImage" + gppId + "' class='");
		html.append(PropertyPanelResources.INSTANCE.css().entityIcon()).append("'></div></td>");
		html.append("<td class='gxtReset'><table><tr><td class='gxtReset'><div id='generalPropertyPanelTitle" + gppId + "'></div></td></tr>");
		html.append("<tr><td class='gxtReset'><div id='generalPropertyPanelFullEntityType" + gppId + "' class='").append(PropertyPanelResources.INSTANCE.css().entityTypeLabel()).append("'></div></td></tr>");
		html.append("</table></td></tr></table></body></html>");
		
		/*
		 //header info - Icon, Entity basic info
		HTMLPanel panel = new HTMLPanel(html.toString());
		panel.add(this.iconImage, "generalPropertyPanelImage" + gppId);
		panel.add(this.nameLabel, "generalPropertyPanelTitle" + gppId);
		panel.add(this.fullEntityTypeLabel, "generalPropertyPanelFullEntityType" + gppId);
		
		this.fullEntityTypeLabel.addStyleName(PropertyPanelResources.INSTANCE.css().entityTypeLabel());
		this.nameLabel.addStyleName(PropertyPanelResources.INSTANCE.css().entityTitleLabel());
		this.fullEntityTypeLabel.setText("");
		this.nameLabel.setText("");
		container.add(panel, new VerticalLayoutData(1, 60, new Margins(0)));
		*/
		container.add(prepareGrid(), new VerticalLayoutData(1, 1, new Margins(0)));
		
		setWidget(container);
		
		if (!this.showGridLines) {
			GridWithoutLinesStyle style = GWT.<GridWithoutLinesResources>create(GridWithoutLinesResources.class).css();
			style.ensureInjected();
			this.resolutionViewGrid.addStyleName(style.gridWithoutLines());
			addStyleName(PropertyPanelResources.INSTANCE.css().propertyPanelWithoutLines());
		}
		PropertyPanelCss css = PropertyPanelResources.INSTANCE.css();
		this.specialUiElementsStyles = Arrays.asList(css.propertyNameFlowExpanderCollapsed(), css.propertyNameFlowExpanderExpanded(),
				PropertyPanelCss.EXTERNAL_PROPERTY_MENU, css.checkedValue(), css.uncheckedValue(), css.checkNullValue(),
				PropertyPanelCss.EXTERNAL_PROPERTY_VALUE_COLLECTION_ADD, PropertyPanelCss.EXTERNAL_PROPERTY_VALUE_ENTITY_ASSIGN);
		this.specialFlowClasses = new HashSet<Class<?>>();
		this.specialFlowClasses.add(LocalizedString.class);
	}
	
	/**
	 * Configures whether to show grid lines. Defaults to true.
	 * @param showGridLines
	 */
	@SuppressWarnings("javadoc")
	@Configurable
	public void setShowGridLines(boolean showGridLines) {
		this.showGridLines = showGridLines;
	}
	
	/**
	 * Configures if the PropertyPanel will be shown as readOnly.
	 * Defaults to false (users can also edit the properties).
	 * @param readOnly
	 */
	@SuppressWarnings("javadoc")
	@Configurable
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	/**
	 * Configures a map containing special traversing criterion for the given entities.
	 * This is used when loading an absent property. Special entities (such as {@link LocalizedString}) require some properties to be loaded.
	 */
	@Configurable
	public void setSpecialEntityTraversingCriterion(Map<Class<?>, TraversingCriterion> specialEntityTraversingCriterion) {
		this.specialEntityTraversingCriterion = specialEntityTraversingCriterion;
	}	
	
	// ----- GmSessionHandler Members ---- //
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
		if (this.iconProvider != null)
			this.iconProvider.configureGmSession(gmSession);
	}

	public PersistenceGmSession getGmSession() {
		return this.gmSession;
	}

	@Required
	public void setIconProvider(IconProvider iconProvider) {
		this.iconProvider = iconProvider;
	}
	
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
		if (this.iconProvider != null)
			this.iconProvider.configureUseCase(useCase);
	}

	public String getUseCase() {
		return this.useCase;
	}
	
	public void addInteractionListener(GmInteractionListener il) {
		if (il != null) {
			if (this.gmInteractionListeners == null) {
				this.gmInteractionListeners = new ArrayList<GmInteractionListener>();
			}
			this.gmInteractionListeners.add(il);
		}
	}

	public void removeInteractionListener(GmInteractionListener il) {
		if (this.gmInteractionListeners != null) {
			this.gmInteractionListeners.remove(il);
			if (this.gmInteractionListeners.isEmpty())
				this.gmInteractionListeners = null;
		}
	}
	
	public void addSelectionListener(GmSelectionListener sl) {
		if (sl != null) {
			if (this.gmSelectionListeners == null) {
				this.gmSelectionListeners = new ArrayList<GmSelectionListener>();
			}
			this.gmSelectionListeners.add(sl);
		}
	}

	public void removeSelectionListener(GmSelectionListener sl) {
		if (this.gmSelectionListeners != null) {
			this.gmSelectionListeners.remove(sl);
			if (this.gmSelectionListeners.isEmpty())
				this.gmSelectionListeners = null;
		}
	}
    
    public void setMetaDataResolverProvider(MetaDataResolverProvider metaDataResolverProvider) {
    	this.metaDataResolverProvider = metaDataResolverProvider;
    }
    
	/**
	 * Configures the required {@link CodecRegistry} used as renderers.
	 */
	public void setCodecRegistry(CodecRegistry<String> codecRegistry) {
		this.codecRegistry = codecRegistry;
	}
	
	public boolean isSelected(Object element) {
		for (MetaDataEditorResolutionModel model : this.resolutionViewGrid.getSelectionModel().getSelectedItems()) {
			if (model.getValue() == element)
				return true;
		}
		
		return false;
	}
	
	public void select(int index, boolean keepExisting) {
		this.resolutionViewGrid.getSelectionModel().select(index, keepExisting);
	}
		
	public void setContent(final ModelPath modelPath) {
		    this.resolutionViewGrid.getStore().clear();	
		    		    
		    if (modelPath == null) {
		    	return;
		    } 
		    		    		    
		    fillResolutionData(modelPath, this.metaDataResolverProvider.getModelMetaDataContextBuilder());	
		    //updatePanel();
	}

	
	private void fillResolutionData(ModelPath modelPath, ModelMdResolver modelContextBuilder) {
		/*
		this.iconImage.setResource(PropertyPanelResources.INSTANCE.clear());
		this.fullEntityTypeLabel.setText("");
		this.nameLabel.setText("");
		*/
		
		this.lastModelPath = modelPath;
		this.lastModelContextBuilder = modelContextBuilder;
		//GmMetaModel resolvingGmMetaModel = null;
		GmEntityType resolvingGmEntityType = null;
		GmEnumType resolvingGmEnumType = null;
				    
		//String stringInherited = "";
		//String nameString = null;
		//String nameEntityString = null;
		//String typeSignatureString = null;
		
		//find MetaData.class for selected metaData				
		//EntityType<? extends MetaData> baseTypeClass = MetaData.T;
		EntityType<? extends MetaData> entityTypeClassMetaData = null;
		EntityType<? extends MetaData> entityTypeElementMetaData = null;
		
		if (modelPath == null || modelPath.isEmpty())
			return;
				
		MetaData elementMetaData = null;
		for (ModelPathElement pathElement : modelPath) {
			if (pathElement.getType() != null) {
				if (pathElement.getType() instanceof EntityType<?>) {
					entityTypeElementMetaData = pathElement.getType();
					
					/*
					if (MetaDataEditorUtil.isInstanceOf(EntityTypeMetaData.class, classMetaData)) {
						baseTypeClass = EntityTypeMetaData.T;
					} else if (MetaDataEditorUtil.isInstanceOf(ModelMetaData.class, classMetaData)) {
						baseTypeClass = ModelMetaData.T;
					} else if (MetaDataEditorUtil.isInstanceOf(PropertyMetaData.class, classMetaData)) {
						baseTypeClass = PropertyMetaData.T;
					} else if (MetaDataEditorUtil.isInstanceOf(EnumTypeMetaData.class, classMetaData)) {
						baseTypeClass = EnumTypeMetaData.T;
					}
					*/
				} else if (pathElement.getType() instanceof MetaData) {
					entityTypeElementMetaData = GMF.getTypeReflection().getEntityType(pathElement.getType().getTypeSignature());
				}			
			}
			if (pathElement.getValue() != null) {
				if (pathElement.getValue() instanceof EntityType<?>) {
					entityTypeClassMetaData = pathElement.getValue();
				} else if (pathElement.getValue() instanceof MetaData) {
					elementMetaData = (MetaData) pathElement.getValue();
					entityTypeClassMetaData = ((MetaData) pathElement.getValue()).entityType();										
				}
				
				/*
				if (pathElement.getValue() instanceof GmMetaModel)
					resolvingGmMetaModel = pathElement.getValue();
				else 
				*/
				if (pathElement.getValue() instanceof GmEntityType)
					resolvingGmEntityType = pathElement.getValue();
				if (pathElement.getValue() instanceof GmEnumType)
					resolvingGmEnumType = pathElement.getValue();								
			}
			
		}
		
		if (absentMetaData(elementMetaData)) {
			return;
		}		
		
		if (entityTypeClassMetaData == null)
			entityTypeClassMetaData = entityTypeElementMetaData;
		/*
		List<Property> propList = null;
		if (classMetaData != null) {
			propList = MetaDataEditorUtil.preparePropertyList(classMetaData, baseTypeClass, true);
			
			EntityType<? extends GenericEntity> entityType = classMetaData;
			
			nameString = classMetaData.getSimpleName();
			typeSignatureString = classMetaData.getName();
			nameString = entityType.getShortName();
			typeSignatureString = entityType.getTypeSignature();
		}
		*/
																														
		ModelPathElement pathElement = modelPath.last();
		//for (ModelPathElement pathElement : modelPath) {
			GenericEntity entity = null;
			if (pathElement.getValue() instanceof GenericEntity)
				entity = pathElement.getValue();

			if (entity != null) {
					this.gmSession.listeners().entity(entity).remove(this);				   
			    	this.gmSession.listeners().entity(entity).add(this);
					List<MetaDataEditorResolutionModel> metaDataModels = new ArrayList<>();
			    	
				    //Fill Grid - callEntity (MetaData as GenericEntity)			    	
					MetaData metaData = null;					
					
					//if (entity instanceof GmType) 
					//	if (typeSignatureString == null)
					//		typeSignatureString = ((GmType) entity).getTypeSignature();					
					
					//MetaData metaDataResolved = null;											
					//MetaDataDescriptor<MetaData> metaDataDesciptor = null;
					
					Boolean wasResolvedAsDefault = false;
					Boolean isInherited = false;
					Boolean useDescriptor = false;
					GmEntityType ownerType = null;
					
					if (entity instanceof GmEntityType) {
				    	EntityMdResolver entityMdResolver = modelContextBuilder.entityType((GmEntityType) entity);			    				    				    						
				    	EntityRelatedMdDescriptor mdDescriptor = null;
				    	if (entityTypeClassMetaData != null) {
							metaData = entityMdResolver.meta(entityTypeClassMetaData).exclusive();
				    		mdDescriptor = entityMdResolver.meta(entityTypeClassMetaData).exclusiveExtended();
				    	}
						//metaDataResolved =  (metaDataEntityTypeDesciptor != null) ? metaDataEntityTypeDesciptor.getResolvedValue() : null;	
						if (mdDescriptor != null) { 
							useDescriptor = true;
							wasResolvedAsDefault = mdDescriptor.getResolvedAsDefault();
							isInherited = mdDescriptor.isInherited();
							ownerType = mdDescriptor.getOwnerTypeInfo().addressedType();
						}
					}
					if (entity instanceof GmProperty) {
						if (resolvingGmEntityType == null)
							resolvingGmEntityType = ((GmProperty) entity).getDeclaringType();												
				    	PropertyMdResolver propertyMdResolver = modelContextBuilder.entityType(resolvingGmEntityType).property((GmProperty) entity);			    				    				    				    	

				    	EntityRelatedMdDescriptor mdDescriptor = null;
						if (entityTypeClassMetaData != null) { 
							metaData = propertyMdResolver.meta(entityTypeClassMetaData).exclusive();
							mdDescriptor = propertyMdResolver.meta(entityTypeClassMetaData).exclusiveExtended();
						}
						
						//metaDataResolved = (metaDataPropertyDesciptor != null) ? metaDataPropertyDesciptor.getResolvedValue() : null;	
						if (mdDescriptor != null) { 
							useDescriptor = true;
							wasResolvedAsDefault = mdDescriptor.getResolvedAsDefault();
							isInherited = mdDescriptor.isInherited();
							ownerType = mdDescriptor.getOwnerTypeInfo().addressedType();
						}
					}
					if (entity instanceof GmEnumType) {
						EnumMdResolver enumMdResolver = modelContextBuilder.enumType((GmEnumType) entity);						
						EnumRelatedMdDescriptor mdDescriptor = null;
						if (entityTypeClassMetaData != null) { 
							metaData = enumMdResolver.meta(entityTypeClassMetaData).exclusive();
							mdDescriptor = enumMdResolver.meta(entityTypeClassMetaData).exclusiveExtended();
						}
						
						//metaDataResolved =  (metaDataEnumTypeDesciptor != null) ? metaDataEnumTypeDesciptor.getResolvedValue() : null;							
						if (mdDescriptor != null) { 
							useDescriptor = true;
							wasResolvedAsDefault = mdDescriptor.getResolvedAsDefault();
						}
					}
					if (entity instanceof GmMetaModel) {
						if (entityTypeClassMetaData != null) { 
							ModelMdResolver modelMdResolver = modelContextBuilder;
							metaData = modelMdResolver.meta(entityTypeClassMetaData).exclusive();
							MdDescriptor mdDescriptor = modelMdResolver.meta(entityTypeClassMetaData).exclusiveExtended();
						
							//metaDataResolved = (metaDataModelDesciptor != null) ? metaDataModelDesciptor.getResolvedValue() : null;
							if (mdDescriptor != null) { 
								useDescriptor = true;
								wasResolvedAsDefault = mdDescriptor.getResolvedAsDefault();
							}
						}
					}
					if (entity instanceof GmEnumConstant) {
						if (resolvingGmEnumType == null)
							resolvingGmEnumType = ((GmEnumConstant) entity).getDeclaringType();												
				    	ConstantMdResolver constantMdResolver = modelContextBuilder.enumType(resolvingGmEnumType).constant((GmEnumConstant) entity);
				    	
				    	EnumRelatedMdDescriptor mdDesciptor = null;
						if (entityTypeClassMetaData != null) { 
							metaData = constantMdResolver.meta(entityTypeClassMetaData).exclusive();
							mdDesciptor = constantMdResolver.meta(entityTypeClassMetaData).exclusiveExtended();
						}
						
						//metaDataResolved = (metaDataPropertyDesciptor != null) ? metaDataPropertyDesciptor.getResolvedValue() : null;	
						if (mdDesciptor != null) { 
							useDescriptor = true;
							wasResolvedAsDefault = mdDesciptor.getResolvedAsDefault();
						}
					}					
										
					MetaDataEditorResolutionModel model;
					
					/*
					if (propList != null) {
						if (classMetaData == null)
							continue;
						
						String entitySignature = classMetaData.getTypeSignature();
						ModelMdResolver modelMdResolver = this.gmSession.getModelAccessory().getMetaData();
						EntityMdResolver emdContextBuilder = modelMdResolver.entityTypeSignature(entitySignature).useCase(this.useCase);
												
						for (Property prop : propList)  {		
							model = new MetaDataEditorResolutionModel();
							
							//Get name for properties - from Session resolver!!!
							String propertyName = prop.getPropertyName();					
							Name propName = emdContextBuilder.property(prop).meta(Name.T).exclusive();
							if (propName != null) {
								propertyName = I18nTools.getLocalized((LocalizedString) propName);
							} else {					
								Name entityName = emdContextBuilder.meta(Name.T).exclusive();
								if (entityName != null)
									propertyName = I18nTools.getLocalized((LocalizedString) entityName);
							}							
							
							model.setDisplayName(propertyName);
							model.setEditable(true);
							model.setValueDisplay("");
							model.setGroupName(LocalizedText.INSTANCE.properties());
							model.setGroupPriority(0.0);
							
							if (metaData != null) {								
								if (prop.getProperty(metaData) instanceof LocalizedString) {
									LocalizedString localizedText = (LocalizedString) prop.getProperty(metaData);
									model.setValueDisplay(I18nTools.getLocalized(localizedText));										
								} else 	if (prop.getProperty(metaData) instanceof String) {
									model.setValueDisplay((String )prop.getProperty(metaData));
								} else {
									GenericModelType propertyType = prop.getPropertyType();
									Object propertyValue = prop.getProperty(metaData);
									model.setValueDisplay(MetaDataEditorUtil.prepareStringValue(propertyValue, propertyType, modelContextBuilder, this.valueRenderers, this.readOnly));
								}
							} else {
								GenericModelType propertyType = prop.getPropertyType();
								Object propertyValue = null;
								model.setValueDisplay(MetaDataEditorUtil.prepareStringValue(propertyValue, propertyType, modelContextBuilder, this.valueRenderers, this.readOnly));									
							}
							metaDataModels.add(model);
						}
						
					}	
					*/				
					
					if (metaData != null) {																		
						//selector - regrding talk to Gregor - moved out - Selector is still showed on Detail tab
						/*
						model = new MetaDataEditorResolutionModel();
						model.setDisplayName(Selector.T.getShortName());						
						if (metaData.getSelector() != null) {
							model.setValueDisplay(getSelectorDisplayValue(metaData.getSelector()));	
						} else {
							model.setValueDisplay("");
						}
						metaDataModels.add(model);
						*/
					} else if ((Predicate.T.isAssignableFrom(entityTypeClassMetaData))) {
						//RVE - show info for Predicate ...that the value is Default Value
						model = new MetaDataEditorResolutionModel();
						model.setDisplayName(LocalizedText.INSTANCE.resolvedAsDefault());
						model.setValueDisplay("Yes");
						metaDataModels.add(model);						
					} else {
						model = new MetaDataEditorResolutionModel();
						model.setDisplayName(LocalizedText.INSTANCE.resolvedAsDefault());
						model.setValueDisplay("Yes");
						metaDataModels.add(model);												
					}
										
					if (useDescriptor) {
						//resolved As
						model = new MetaDataEditorResolutionModel();							
						model.setDisplayName(LocalizedText.INSTANCE.resolvedAsDefault());
						//model.setValueElementType(setJavaType(Boolean.class));					
						if (wasResolvedAsDefault) {
							model.setValueDisplay("Yes");
						} else {
							model.setValueDisplay("No");
						}
						model.setEditable(false);
						//model.setGroupName(LocalizedText.INSTANCE.resolver());
						//model.setGroupPriority(0.0);
						metaDataModels.add(model);						
						
						//Is inherited
						model = new MetaDataEditorResolutionModel();
						model.setDisplayName(LocalizedText.INSTANCE.isInherited());
						//model.setValueElementType(setJavaType(Boolean.class));					
						if (isInherited) {
							model.setValueDisplay("Yes");
						} else {
							model.setValueDisplay("No");
						}
						model.setEditable(false);
						//model.setGroupName(LocalizedText.INSTANCE.resolver());
						//model.setGroupPriority(0.0);
						metaDataModels.add(model);
						
						//Owner type
						
						model = new MetaDataEditorResolutionModel();
						model.setDisplayName(LocalizedText.INSTANCE.ownerType());
						if (ownerType != null) {
							String owner = ownerType.getTypeSignature();
							String selectiveInformation = SelectiveInformationResolver.resolve(ownerType.entityType(), ownerType, modelContextBuilder, useCase/* , null */);
							if (selectiveInformation != null && !selectiveInformation.trim().equals("")) {
								owner = selectiveInformation;
							} else {							
								Name name = modelContextBuilder.entityType(ownerType).meta(Name.T).exclusive();
								if (name != null && name.getName() != null)
									owner = I18nTools.getLocalized(name.getName());
							}
							model.setValueDisplay(owner);
						} else {
							model.setValueDisplay("");							
						}
						model.setEditable(false);						
						metaDataModels.add(model);
					}

					//model.setGroupName(LocalizedText.INSTANCE.resolver());
					//model.setGroupPriority(0.0);
					
					
					this.resolutionViewGrid.getStore().addAll(metaDataModels);
					/*
					 * RVE - save for using Groups Columns
					if (this.resolutionViewGrid.getView() instanceof MetaDataEditorResolutionGroupView) {					
						((MetaDataEditorResolutionGroupView) this.resolutionViewGrid.getView()).groupBy(this.groupColumn);
					}
					*/
									
					
			    	//Fill captions Texts and Icon
					//EntityType<?> entityType = pathElement.getType();
					//EntityType<GenericEntity> parentEntityType = modelPath != null ? (EntityType<GenericEntity>) modelPath.last().getType() : null;
					
					//set icon
					/*
					ImageResource icon = PropertyPanelResources.INSTANCE.defaultIcon();			
					try {
						//iconProvider.configureGmSession(this.gmSession);
						//iconProvider.configureUseCase(this.useCase);
						IconAndType iconAndType = this.iconProvider.provide(modelPath);
						if (iconAndType != null)
							icon = iconAndType.getIcon();
					} catch (ProviderException e) {
						e.printStackTrace();
					}									
					this.iconImage.setResource(icon);					
					if (icon == PropertyPanelResources.INSTANCE.defaultIcon())
						this.iconImage.getElement().getStyle().setOpacity(0.1);
					else
						this.iconImage.getElement().getStyle().clearOpacity();	
					this.iconImage.getElement().getStyle().setMarginTop(39 - icon.getHeight(), Unit.PX);
					this.iconImage.getElement().getStyle().setMarginLeft(32 - icon.getWidth(), Unit.PX);								
					//set text field					

					this.fullEntityTypeLabel.setText(typeSignatureString);
					// fullEntityTypeLabel.setText(entityType.getEntityTypeName());
					this.nameLabel.setText(nameString);
					*/

		    }			    	
			
		//}
	}
				
	private Boolean absentMetaData(MetaData metaData) {
		if (metaData == null)
			return false;
		
		Selector selector = metaData.getSelector();				
		if (selector != null) {	
			List<Property> absentProperties = new ArrayList<>();
			for (Property property : selector.entityType().getProperties()) {
				if (GMEUtil.isPropertyAbsent(selector, property))
					absentProperties.add(property);				
			}
			
			if (!absentProperties.isEmpty()) {
				loadAbsentProperties(selector, absentProperties) //
						.andThen(result -> fillResolutionData(lastModelPath, lastModelContextBuilder)).onError(e -> {
							ErrorDialog.show("Error load MetaData Editor Properties", e);
							e.printStackTrace();
						});
				return true;
			}
		}
		return false;
	}

	private Future<Void> loadAbsentProperties(GenericEntity parentEntity, List<Property> absentProperties) {
		final Future<Void> future = new Future<Void>();
		
		MultiLoader multiLoader = new MultiLoader();
		multiLoader.setParallel(false);
		int i = 0;
		for (Property property : absentProperties) {
			multiLoader.add(Integer.toString(i++), GMEUtil.loadAbsentProperty(parentEntity, parentEntity.entityType(), property, gmSession, useCase,
					codecRegistry, specialEntityTraversingCriterion));
		}
		
		multiLoader.load(AsyncCallbacks.of(result -> future.onSuccess(null), future::onFailure));
		return future;
	}
		
	private Grid<MetaDataEditorResolutionModel> prepareGrid() {
		ListStore<MetaDataEditorResolutionModel> store = new ListStore<MetaDataEditorResolutionModel>(props.id()) {
			//create Store
		};
		
		GroupingView<MetaDataEditorResolutionModel> groupingView = new MetaDataEditorResolutionGroupView() {
			@Override
			protected void afterRender() {
				super.afterRender();
				if (!MetaDataEditorResolutionView.this.readOnly)
					prepareCustomEditors(MetaDataEditorResolutionView.this.resolutionViewGrid.getStore().getAll());
				MetaDataEditorResolutionView.this.editorsReady = true;
				if (MetaDataEditorResolutionView.this.startEditingPending)
					startEditing();
			}
		};
		groupingView.setForceFit(true);
		groupingView.setShowDirtyCells(false);
		//groupingView.setCellSelectorDepth(8); //Increased due to the use of tables in the renderer
		//groupingView.setRowSelectorDepth(14); //Increased due to the use of tables in the renderer
		
		this.resolutionViewGrid = new Grid<MetaDataEditorResolutionModel>(store, prepareColumnModel(), groupingView);
		this.resolutionViewGrid.setBorders(false);
		this.resolutionViewGrid.addStyleName("gmePropertyPanel");
		this.resolutionViewGrid.setAllowTextSelection(true);
		this.resolutionViewGrid.setHideHeaders(true);
				
		this.resolutionViewGrid.getView().setViewConfig(new GridViewConfig<MetaDataEditorResolutionModel>() {
			@Override
			public String getRowStyle(MetaDataEditorResolutionModel model, int rowIndex) {
				return "";
			}
			
			@Override
			public String getColStyle(MetaDataEditorResolutionModel model, ValueProvider<? super MetaDataEditorResolutionModel, ?> valueProvider,
					int rowIndex, int colIndex) {
				return "gmeGridColumn";
			}
		});
		
		this.gridInlineEditing = new MultiEditorGridInlineEditing<MetaDataEditorResolutionModel>(this.resolutionViewGrid);
		//resolutionViewGrid.setStartEditingDelay(200); //Setting a delay because is refreshed after editing a value.
		
		this.gmEditorSupport = new GMEditorSupport();
		
		this.resolutionViewGrid.getSelectionModel().setLocked(true);
		
		if (!this.readOnly) {
			this.gridInlineEditing.addBeforeStartEditHandler(event -> {
				if (MetaDataEditorResolutionView.this.editionNestedTransaction != null)
					rollbackTransaction();
				MetaDataEditorResolutionView.this.editionNestedTransaction = MetaDataEditorResolutionView.this.gmSession.getTransaction().beginNestedTransaction();
				MetaDataEditorResolutionView.this.startValue = MetaDataEditorResolutionView.this.resolutionViewGrid.getStore().get(event.getEditCell().getRow()).getValue();
			});
			this.gridInlineEditing.addCompleteEditHandler(event -> {
				MetaDataEditorResolutionModel model = MetaDataEditorResolutionView.this.resolutionViewGrid.getStore().get(event.getEditCell().getRow());
				IsField<?> editor = event.getSource()
						.getEditor(MetaDataEditorResolutionView.this.resolutionViewGrid.getColumnModel().getColumn(event.getEditCell().getCol()));
				new Timer() {
					@Override
					public void run() {
						MetaDataEditorResolutionView.this.resolutionViewGrid.getStore().rejectChanges();
					}
				}.schedule(500);
				//gridInlineEditing.setCurrentRow(event.getEditCell().getRow()); needed?
				if (!GMEUtil.isEditionValid(editor.getValue(), MetaDataEditorResolutionView.this.startValue, editor)) {
					//rollbackTransaction();
					return;
				}
				model.getParentEntityType().getProperty(model.getPropertyName())
						.set(model.getParentEntity(), editor.getValue());
				MetaDataEditorResolutionView.this.editionNestedTransaction.commit();
				MetaDataEditorResolutionView.this.editionNestedTransaction = null;
				MetaDataEditorResolutionView.this.startValue = null;
			});
		}
		
		QuickTip quickTip = new QuickTip(this.resolutionViewGrid);
		ToolTipConfig config = new ToolTipConfig();
		config.setMaxWidth(400);
		config.setDismissDelay(0);
		quickTip.update(config);
		
		return this.resolutionViewGrid;
	}	
	
	private void prepareCustomEditors(List<MetaDataEditorResolutionModel> propertyModels) {
		this.gridInlineEditing.clearEditors();
		ColumnConfig<MetaDataEditorResolutionModel, ?> columnConfig = this.resolutionViewGrid.getColumnModel().getColumn(VALUE_INDEX);
		int rowIndex = 0;
		for (MetaDataEditorResolutionModel propertyModel : propertyModels) {
			prepareCustomEditor(propertyModel, columnConfig, rowIndex);
			rowIndex++;
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void prepareCustomEditor(final MetaDataEditorResolutionModel propertyModel, ColumnConfig<MetaDataEditorResolutionModel, ?> columnConfig, int rowIndex) {
		this.gridInlineEditing.setCurrentRow(rowIndex);
		
		if (propertyModel.isEditable()) {
			EntityMdResolver entityMdResolver;
			GenericEntity parentEntity = propertyModel.getParentEntity();
			if (parentEntity != null)
				entityMdResolver = GmSessions.getMetaData(parentEntity).entity(parentEntity).useCase(this.useCase);
			else
				entityMdResolver = this.gmSession.getModelAccessory().getMetaData().entityType(propertyModel.getParentEntityType()).useCase(this.useCase);
			
			PropertyMdResolver propertyMdResolver = entityMdResolver.property(propertyModel.getPropertyName());
			int minLenght = -1;
			int maxLenght = -1;
			MinLength minLengthMeta = propertyMdResolver.meta(MinLength.T).exclusive();
			if (minLengthMeta != null)
				minLenght = ((Long) minLengthMeta.getLength()).intValue();
			MaxLength maxLengthMeta = propertyMdResolver.meta(MaxLength.T).exclusive();
			if (maxLengthMeta != null)
				maxLenght = ((Long) maxLengthMeta.getLength()).intValue();
			
			Object minValue = null;
			Min min = propertyMdResolver.meta(Min.T).exclusive();
			if (min != null)
				minValue = min.getLimit();
			
			Object maxValue = null;
			Max max = propertyMdResolver.meta(Max.T).exclusive();
			if (max != null)
				maxValue = max.getLimit();
			
			PropertyFieldContext context = new PropertyFieldContext();
			context.setMandatory(propertyModel.getMandatory());
			context.setPassword(propertyModel.getPassword());
			context.setModelType(propertyModel.getValueElementType());
			context.setRegex(propertyMdResolver.meta(Pattern.T).exclusive());
			context.setUseAlternativeField(propertyModel.getFlow());
			context.setUseCase(this.useCase);
			context.setGmSession(this.gmSession);
			context.setVirtualEnum(propertyMdResolver.meta(VirtualEnum.T).exclusive());
			context.setMinLenght(minLenght);
			context.setMaxLenght(maxLenght);
			context.setParentEntity(parentEntity);
			context.setParentEntityType(propertyModel.getParentEntityType());
			context.setPropertyName(propertyModel.getPropertyName());
			context.setMinValue(minValue);
			context.setMaxValue(maxValue);
			
			IsField<?> field = this.gmEditorSupport.providePropertyField(context);
			if (field != null) {
				if (field instanceof TriggerFieldAction) {
					if (field instanceof TriggerField<?>)
						((TriggerField<?>) field).setHideTrigger(true);
					((TriggerFieldAction) field).setGridInfo(this.gridInlineEditing, new GridCell(rowIndex, this.resolutionViewGrid.getColumnModel().getColumns().indexOf(columnConfig)));
					
					//if (actionManager == null || !(field instanceof SimplifiedEntityField)) {
					//	if (triggerFieldActionModelMap == null)
					//		triggerFieldActionModelMap = new HashMap<PropertyModel, TriggerFieldAction>();
					//	triggerFieldActionModelMap.put(propertyModel, (TriggerFieldAction) field);
					//}
				}
				if (field instanceof SimplifiedEntityField) {
					((SimplifiedEntityField) field).configureGmSession(this.gmSession);
					((SimplifiedEntityField) field).configureUseCase(this.useCase);
					//((SimplifiedEntityField) field).configurePropertyModel(propertyModel);
					//((SimplifiedEntityField) field).configureGmContentView(this);
				}
								
				this.gridInlineEditing.addEditor(columnConfig, (IsField) field, propertyModel.getFlow());
				return;
			}
		}
	}
	
	/**
	 * Starts editing the first editable property, if any.
	 */
	public void startEditing() {
		if (this.editorsReady) {
			this.startEditingPending = false;
			if (!this.readOnly && !this.resolutionViewGrid.getStore().getAll().isEmpty()) {
				for (int rowIndex = 0; rowIndex < this.resolutionViewGrid.getStore().getAll().size(); rowIndex++) {
					MetaDataEditorResolutionModel propertyModel = this.resolutionViewGrid.getStore().get(rowIndex);
					if (propertyModel.isEditable()) {
						if (this.gridInlineEditing.getEditor(this.resolutionViewGrid.getColumnModel().getColumn(VALUE_INDEX), rowIndex) != null) {
							final int index = rowIndex;
							new Timer() {
								@Override
								public void run() {
									MetaDataEditorResolutionView.this.gridInlineEditing.startEditing(new GridCell(index, VALUE_INDEX));
								}
							}.schedule(1000);
							break;
						}
					}
				}
			}
		} else
			this.startEditingPending = true;
	}
	
	/*private void fireClickOrDoubleClick(boolean click, GmMouseInteractionEvent event) {
		if (gmInteractionListeners != null) {
			List<GmInteractionListener> listenersCopy = new ArrayList<GmInteractionListener>(gmInteractionListeners);
			for (GmInteractionListener listener : listenersCopy) {
				if (click)
					listener.onClick(event);
				else
					listener.onDblClick(event);
			}
		}
	}*/
	
	private void rollbackTransaction() {
		try {
			if (this.editionNestedTransaction != null) {
				this.editionNestedTransaction.rollback();
			}
			this.editionNestedTransaction = null;
		} catch (TransactionException e) {
			ErrorDialog.show(com.braintribe.gwt.gme.propertypanel.client.LocalizedText.INSTANCE.errorRollingEditionBack(), e);
			e.printStackTrace();
		} catch (IllegalStateException ex) {
			//Nothing to do: the PP was used within some widget which rolled back the parent transaction already. This may happen within GIMA when canceling it while editing.
		}
	}
	
	private ColumnModel<MetaDataEditorResolutionModel> prepareColumnModel() {
		ColumnConfig<MetaDataEditorResolutionModel, MetaDataEditorResolutionModel> displayNameColumn = new ColumnConfig<>(
				new IdentityValueProvider<MetaDataEditorResolutionModel>(), propertyNameColumnWidth);
		displayNameColumn.setCellPadding(false);
		displayNameColumn.setCell(new AbstractCell<MetaDataEditorResolutionModel>("click") {
			
			private boolean handlesSelection = false;
			
			@Override
			public void render(com.google.gwt.cell.client.Cell.Context context, MetaDataEditorResolutionModel model, SafeHtmlBuilder sb) {
				StringBuilder html = new StringBuilder();
				html.append("<div class='").append(GMEUtil.PROPERTY_NAME_CSS).append(" ").append(PropertyPanelResources.INSTANCE.css().propertyName());
				//prepareColspan = model.getFlow();
				
				if (model.getFlow()) {
					html.append(" ").append(PropertyPanelResources.INSTANCE.css().propertyNameFlow());
					if ((model.getValue() instanceof GenericEntity && !MetaDataEditorResolutionView.this.specialFlowClasses
							.contains(((EntityType<?>) model.getValueElementType()).getJavaType()))
							|| (model.getValue() != null && model.getValueElementType() instanceof CollectionType)) {
						html.append(" ").append(PropertyPanelResources.INSTANCE.css().propertyNameFlowLink());
					}
				} else if (context.getIndex() == 0 || MetaDataEditorResolutionView.this.resolutionViewGrid.getStore().get(context.getIndex() - 1).getFlow())
					html.append(" ").append(PropertyPanelResources.INSTANCE.css().propertyNameFirst());
				
				if (model.getMandatory())
					html.append(" ").append(PropertyPanelResources.INSTANCE.css().propertyNameMandatory());
				
				if (!model.isEditable())
					html.append(" ").append(PropertyPanelResources.INSTANCE.css().propertyNameReadOnly());
				
				html.append("'>");
				
				//String icon = getIconHtml(model);
				if (model.getFlow()) {
					StringBuilder span = new StringBuilder();
					span.append("<span class='").append(model.getFlowExpanded() ?
							PropertyPanelResources.INSTANCE.css().propertyNameFlowExpanderExpanded() : PropertyPanelResources.INSTANCE.css().propertyNameFlowExpanderCollapsed());
					span.append("'></span>");
					
					//if (icon != null)
						//span.append(icon);
					span.append(model.getDisplayName());
					html.append(prepareMenuTable(span.toString(), model.getDescription(), null, true, false, false)).append("</div>");
				} else {
					html.append("<span style='margin-left: 11px;'");
					if (model.getDescription() != null)
						html.append(" qtip='").append(model.getDescription()).append("'");
					html.append(">");
					//if (icon != null)
						//html.append(icon);
					html.append(model.getDisplayName()).append("</span></div>");
				}
				
				sb.appendHtmlConstant(html.toString());
			}
			
			@Override
			public void onBrowserEvent(com.google.gwt.cell.client.Cell.Context context, com.google.gwt.dom.client.Element parent, MetaDataEditorResolutionModel model, NativeEvent event,
					ValueUpdater<MetaDataEditorResolutionModel> valueUpdater) {
				this.handlesSelection = false;
				EventTarget eventTarget = event.getEventTarget();
				if (Element.is(eventTarget)) {
					String cls = Element.as(eventTarget).getClassName();
					boolean isSpecial = false;
					for (String specialStyle : MetaDataEditorResolutionView.this.specialUiElementsStyles) {
						if (cls.contains(specialStyle)) {
							isSpecial = true;
							break;
						}
					}
					
					if (isSpecial || isBooleanProperty(model)) {
						event.stopPropagation();
						event.preventDefault();
						this.handlesSelection = true;
					} else
						super.onBrowserEvent(context, parent, model, event, valueUpdater);
					//handleColumnClick(cls, isSpecial, context, parent, model, event);
				}
			}
			
			@Override
			public boolean handlesSelection() {
				return this.handlesSelection;
			}
		});
		displayNameColumn.setFixed(true);
		
		ColumnConfig<MetaDataEditorResolutionModel, Object> valueColumn = new ColumnConfig<>(props.value(), 100);
		valueColumn.setCellPadding(false);
		valueColumn.setCell(new AbstractCell<Object>("click") {
			
			private boolean handlesSelection = false;
			
			@Override
			public void render(com.google.gwt.cell.client.Cell.Context context, Object value, SafeHtmlBuilder sb) {
				MetaDataEditorResolutionModel model = MetaDataEditorResolutionView.this.resolutionViewGrid.getStore().get(context.getIndex());
				//removeTd = model.getFlow();
				if (model.getFlow()) {
					sb.appendHtmlConstant("<div class='" + PropertyPanelResources.INSTANCE.css().propertyValueFlow() + "'>" + prepareValueRendererString(model, "", true) + "</div>");
					return;
				}
				
				String css = !MetaDataEditorResolutionView.this.readOnly && !MetaDataEditorResolutionView.this.showGridLines && model.isEditable()
						? PropertyPanelResources.INSTANCE.css().editableBox() + " "
						: "";
				css += GMEUtil.PROPERTY_VALUE_CSS + " ";
				/*
				sb.appendHtmlConstant("<div class='" + css + (context.getIndex() == 0 || MetaDataEditorResolutionView.this.resolutionViewGrid.getStore().get(context.getIndex() - 1).getFlow() ?
						PropertyPanelResources.INSTANCE.css().propertyValueFirst() : PropertyPanelResources.INSTANCE.css().propertyValue()) +  "'>" + prepareValueRendererString(model,
								model.getValueDisplay(), false) + "</div>");
								*/
				sb.appendHtmlConstant(appendStringResolutionValue("<div class='" + css + PropertyPanelResources.INSTANCE.css().propertyValue() +  "'>" + prepareValueRendererString(model,
								model.getValueDisplay(), false) + "</div>"));
			}
			
			@Override
			public void onBrowserEvent(com.google.gwt.cell.client.Cell.Context context, Element parent, Object value, NativeEvent event,
					ValueUpdater<Object> valueUpdater) {
				this.handlesSelection = false;
				EventTarget eventTarget = event.getEventTarget();
				if (Element.is(eventTarget)) {
					String cls = Element.as(eventTarget).getClassName();
					boolean isSpecial = false;
					for (String specialStyle : MetaDataEditorResolutionView.this.specialUiElementsStyles) {
						if (cls.contains(specialStyle)) {
							isSpecial = true;
							break;
						}
					}
					
					MetaDataEditorResolutionModel model = MetaDataEditorResolutionView.this.resolutionViewGrid.getStore().get(context.getIndex());
					
					if (isSpecial || isBooleanProperty(model)) {
						event.stopPropagation();
						event.preventDefault();
						this.handlesSelection = true;
					} else
						super.onBrowserEvent(context, parent, model, event, valueUpdater);
					//handleColumnClick(cls, isSpecial, context, parent, model, event);
				}
			}
			
			@Override
			public boolean handlesSelection() {
				return this.handlesSelection;
			}
		});
		
		/*
		this.groupColumn = new ColumnConfig<MetaDataEditorResolutionModel, String>(new ValueProvider<MetaDataEditorResolutionModel, String>() {
			@Override
			public String getValue(MetaDataEditorResolutionModel object) {
				return object.getGroupName();
			}
			@Override
			public void setValue(MetaDataEditorResolutionModel object, String value) {
				object.setGroupName(value);
			}
			@Override
			public String getPath() {
				return "groupName";
			}
		});
		this.groupColumn.setCellPadding(false);
		this.groupColumn.setHidden(true);
		*/

		//ColumnModel<MetaDataEditorResolutionModel> cm = new ColumnModel<MetaDataEditorResolutionModel>(Arrays.<ColumnConfig<MetaDataEditorResolutionModel, ?>> asList(displayNameColumn, valueColumn, this.groupColumn));   //RVE save with Groups, setGroupName
		ColumnModel<MetaDataEditorResolutionModel> cm = new ColumnModel<MetaDataEditorResolutionModel>(Arrays.<ColumnConfig<MetaDataEditorResolutionModel, ?>> asList(displayNameColumn, valueColumn));
		
		//RVE					
		//ColumnModel<PropertyModel> cm = new ColumnModel<PropertyModel>(Arrays.asList((ColumnConfig<PropertyModel, ?>) prepareExpander(), displayNameColumn, valueColumn, groupColumn));
		//prepareExpander().setHidden(true);
		
		return cm;
	}

	private static String prepareMenuTable(String display, String description, String icon, boolean isFlow, boolean isEntity) {
		return prepareMenuTable(display, description, icon, isFlow, true, isEntity);
	}
	private static String prepareMenuTable(String display, String description, String icon, boolean isFlow, boolean showMenu, boolean isEntity) {
		StringBuilder builder = new StringBuilder();
		builder.append("<table class='").append(PropertyPanelResources.INSTANCE.css().inheritFont()).append(" ").append(PropertyPanelResources.INSTANCE.css().tableFixedLayout());
		builder.append("' border='0' cellpadding='2' cellspacing='0'>\n");
		builder.append("   <tr class='").append(PropertyPanelResources.INSTANCE.css().inheritFont()).append("'>\n");
		if (icon != null) {
			builder.append("      <td class='gxtReset' width='14px'>").append(icon).append("&nbsp;</td>\n");
		}
		builder.append("      <td class='gxtReset' ");
		if (description != null)
			builder.append("qtip='").append(description).append("' ");
		builder.append("class='").append(PropertyPanelResources.INSTANCE.css().inheritFont()).append(" ").append(PropertyPanelResources.INSTANCE.css().textOverflowNoWrap());
		builder.append("' width='100%'>");
		if (isEntity && !display.isEmpty())
			builder.append("<span class='").append(PropertyPanelResources.INSTANCE.css().propertyEntity()).append("'>");
		builder.append(display);
		if (isEntity && !display.isEmpty())
			builder.append("</span>");
		builder.append("</td>\n");
		builder.append("      <td width='14px' class='gxtReset ");
		if (showMenu)
			builder.append(PropertyPanelCss.EXTERNAL_PROPERTY_MENU);
		builder.append("' style='height: 14px;");
		builder.append(isFlow ? " padding-right: 13px;" : " padding-right: 9px;").append("'></td>\n");
		builder.append("   </tr>\n</table>");
		return builder.toString();
	}
	
	private static boolean isBooleanProperty(MetaDataEditorResolutionModel model) {
		if (model != null && model.getValueElementType() != null && model.getValueElementType().getJavaType()!= null)
			return model.getValueElementType().getJavaType().equals(Boolean.class);
		
		return false;
	}
	
	private String prepareValueRendererString(MetaDataEditorResolutionModel model, String displayValue, boolean isFlow) {
		String valueIcon = null;
		if (model.getValue() == null) {
			if (model.getAbsent())
				valueIcon = loadingImageString;
		} else if (!isFlow && model.getValue() instanceof String && ((String) model.getValue()).isEmpty())
			valueIcon = emptyStringImageString;
		
		String description = displayValue;
		if (displayValue.contains("<")) //is HTML
			description = null;
		
		boolean isEntity = model.getValue() instanceof GenericEntity && !this.specialFlowClasses.contains(model.getValueElementType().getJavaType());
		
		return prepareMenuTable(displayValue, description, valueIcon, isFlow, isEntity);
	}
		
	/*
	private String getSelectorDisplayValue (MetaDataSelector selector) {
		StringBuilder builder = new StringBuilder();
		if (selector != null) {
			
		    String defaultString = selector.toString();
		    EntityType<?> type = selector.entityType();
			String selectiveInformation = SelectiveInformationResolver.resolve(type, selector,
					(ModelMdResolver) null, this.useCase);
			if (selectiveInformation != null && !selectiveInformation.trim().equals("")) {
				defaultString =  selectiveInformation;
			}
						
			if (selector instanceof UseCaseSelector) {			
				builder.append(LocalizedText.INSTANCE.displayUseCase()).append(": ").append(((UseCaseSelector) selector).getUseCase()) ;	
			}
			if (selector instanceof RoleSelector) {
				if (!builder.toString().isEmpty()) {
					builder.append(" && ");
				}
				builder.append(LocalizedText.INSTANCE.displayRole()).append(": ") .append(((RoleSelector) selector).getRoles());	
			}
			if (selector instanceof AccessSelector) {
				if (!builder.toString().isEmpty()) {
					builder.append(" && ");
				}
				builder.append(LocalizedText.INSTANCE.displayAccess()).append(": ").append(((AccessSelector) selector).getExternalId());	
			}
			
			if (builder.toString().isEmpty()) 
				builder.append(defaultString);
			
		}
		return builder.toString();
	}
	*/

    private static String appendStringResolutionValue(String text) {
    	if (text == null)
    		return null;
    	
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<span class=\"").append(MetaDataEditorResources.INSTANCE.constellationCss().resolutionValue()).append("\">");
		stringBuilder.append(text);
	    stringBuilder.append("</span>");
    	
    	return stringBuilder.toString(); 
    }    
	
	
	/*
	private void updatePanel() {
	     if ( this.resolutionViewGrid.getStore().getAll().isEmpty()) {
	    	 //show empty info panel
	    	 exchangeWidget(getEmptyPanel());
	     } else {
	    	 //show list
	    	 exchangeWidget(this.container);	    	 
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
	*/
	
	/*
	private void updateEmptyPanel() {
		this.emptyPanel.setHTML(getEmptyPanelHtml());
	} 	
	*/
	
	@Override
	public void noticeManipulation(final Manipulation manipulation) {
		if (manipulation instanceof PropertyManipulation) {
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
					}	
					
					if (entity != null && MetaDataEditorResolutionView.this.lastModelPath != null) {
						Boolean useRefresh = false;
					    for (ModelPathElement pathElement : MetaDataEditorResolutionView.this.lastModelPath) {
					    	if (pathElement instanceof GenericEntity) {
							    GenericEntity entityFromPath = pathElement.getValue();						    
							    useRefresh = (entityFromPath.equals(entity)) ? true : useRefresh;
					    	}
					    }   
					    
					    if (useRefresh) {
					    	setContent(MetaDataEditorResolutionView.this.lastModelPath);
					    }						
					}					
				}
			}.schedule(10); //needed, so the value in the entity was the correct one
		}				
	}
}
