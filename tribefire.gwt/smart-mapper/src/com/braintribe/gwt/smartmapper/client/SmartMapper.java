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
package com.braintribe.gwt.smartmapper.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmEntityView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.smartmapper.client.action.SmartMapperActionMenu;
import com.braintribe.gwt.smartmapper.client.util.TypeAndPropertyInfo;
import com.braintribe.model.accessdeployment.smart.meta.PropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.QualifiedEntityAssignment;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.meta.override.GmCustomTypeOverride;
import com.braintribe.model.meta.override.GmEntityTypeOverride;
import com.braintribe.model.meta.override.GmPropertyOverride;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.GlobalIdFactory;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.managed.EntityQueryResultConvenience;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.GXT;
import com.sencha.gxt.core.client.util.KeyNav;
import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;

public class SmartMapper extends ContentPanel implements InitializableBean, DisposableBean, GmEntityView, ManipulationListener{
	
	private ModelEnvironmentDrivenGmSession session;
	private ModelPath modelPath = null;
	
	private Supplier<SpotlightPanel> spotlightPanelProvider;
	
	private GmEntityTypeInfo entityType;
	private QualifiedEntityAssignment entityAssignment;
	
	protected GmMetaModel gmMetaModel;
	protected ModelOracle modelOracle;
	public ModelMdResolver cmdResolver;
	protected ModelMetaDataEditor modelMetaDataEditor;
	
	private FlowPanel topPanel;
	private Label entityTypeLabel;
	private PropertyAssignmentAccessInput incrementalAccessTextBox;
	private PropertyAssignmentTypeInput mappedToEntityTypeBox;
	
	private FlowPanel mainPanel;
		
	private final List<GmSelectionListener> selectionListeners = new ArrayList<>();
	
	private final Map<String, PropertyMappingEntry> propertyMappingEntries = new FastMap<>();
	
	protected List<Pair<String, ModelAction>> smapperActions;
	protected SmartMapperActionMenu actionMenu;
	
	//private String selectorGlobalId = "selector:useCase:cortex";
	
	ModelAction assignAllAsIsAction;
	
	@SuppressWarnings("unused")
	public SmartMapper() {
		setTabIndex(0);
		setHeaderVisible(false);
		setBodyBorder(false);
		setBorders(false);
		setBodyStyle("background: white");
		addStyleName("smartMapper");
		
		BorderLayoutContainer blc = new BorderLayoutContainer();
		blc.setNorthWidget(getTopPanel(), new BorderLayoutData(50));
		blc.setCenterWidget(getMainPanel());
		
		add(blc);
		
		new KeyNav(this) {
			@Override
			public void onKeyPress(NativeEvent evt) {
				if ((evt.getCtrlKey() || (GXT.isMac() && evt.getMetaKey()))){					
					if(evt.getKeyCode() == KeyCodes.KEY_N) {
						System.err.println("new smart");
						evt.stopPropagation();
						evt.preventDefault();
					}
				}
			}
		};
	}
	
	/*private void promise(){
		Promise<Object> p = Promises.supplyAsync("promise", (value) -> {
			
		});		
	}*/
	
	public void setSession(ModelEnvironmentDrivenGmSession session) {
		this.session = session;
	}	
	
	public void setSpotlightPanelProvider(Supplier<SpotlightPanel> spotlightPanelProvider) {
		this.spotlightPanelProvider = spotlightPanelProvider;	
	}
	
	public void setGmMetaModel(GmMetaModel gmMetaModel) {
		if(this.gmMetaModel != null && this.gmMetaModel != gmMetaModel){
			session.listeners().entity(this.gmMetaModel).remove(this);			
			this.gmMetaModel = null;
		}
		
		if(this.gmMetaModel == null){
			this.gmMetaModel = gmMetaModel;
			session.listeners().entity(this.gmMetaModel).add(this);
		}
		
		initMetaModelTools();
	}	
	
	public void initMetaModelTools(){
		if (this.gmMetaModel != null) {
			modelOracle = new BasicModelOracle(this.gmMetaModel);
			cmdResolver = new CmdResolverImpl(modelOracle).getMetaData();

			modelMetaDataEditor = BasicModelMetaDataEditor.create(this.gmMetaModel).withSession(session)
					.withGlobalIdFactory(GlobalIdFactory.noGlobalId).done();

			getActionMenu().setModelMetaDataEditor(modelMetaDataEditor);
		}
	}
	
	public FlowPanel getTopPanel() {
		if(topPanel == null){
			topPanel = new FlowPanel();
			topPanel.addStyleName("smartMapperTopPanel");
			topPanel.add(getEntityTypeLabel());
			topPanel.add(getIncrementalAccessTextBox());
			topPanel.add(getMappedToEntityTypeBox());
		}
		return topPanel;
	}
	
	public Label getEntityTypeLabel() {
		if(entityTypeLabel == null){
			entityTypeLabel = new Label();
			entityTypeLabel.addStyleName("entityTypeName");
			entityTypeLabel.getElement().getStyle().setTextAlign(TextAlign.RIGHT);
			entityTypeLabel.getElement().setAttribute("placeholder", "Assign entityType...");
		}
		return entityTypeLabel;
	}
	
	public PropertyAssignmentAccessInput getIncrementalAccessTextBox() {
		if (incrementalAccessTextBox != null)
			return incrementalAccessTextBox;
		
		incrementalAccessTextBox = new PropertyAssignmentAccessInput(){
			@Override
			public GenericEntity prepareEntity() {
				if(entityAssignment == null){
//						NestedTransaction nt = session.getTransaction().beginNestedTransaction();
					QualifiedEntityAssignment newEntityAssignment = session.create(QualifiedEntityAssignment.T);
					modelMetaDataEditor.onEntityType(TypeAndPropertyInfo.getTypeSignature(entityType)).addMetaData(newEntityAssignment);
//						nt.commit();
					return newEntityAssignment;
				}		
				
				return entityAssignment;
			}
			
			@Override
			public void noticeManipulation(Manipulation manipulation) {
				if(entityAssignment != null)
					pac.mappedToEntityType = entityAssignment.getEntityType();
				super.noticeManipulation(manipulation);
			}
		};
		incrementalAccessTextBox.setPropertyNameOfAssignment("access");
		incrementalAccessTextBox.addStyleName("incrementalAccessName");
//		incrementalAccessTextBox.getElement().setAttribute("style", "background: none; border: none; font-size: 14pt; text-align: center");
//		incrementalAccessTextBox.getElement().setAttribute("placeholder", "Assign incremental access...");
		return incrementalAccessTextBox;
	}
	
	public PropertyAssignmentTypeInput getMappedToEntityTypeBox() {
		if(mappedToEntityTypeBox != null)
			return mappedToEntityTypeBox;
		
		mappedToEntityTypeBox = new PropertyAssignmentTypeInput(){
			@Override
			public GenericEntity prepareEntity() {
				if(entityAssignment == null){
//						NestedTransaction nt = session.getTransaction().beginNestedTransaction();
					QualifiedEntityAssignment newEntityAssignment = session.create(QualifiedEntityAssignment.T);
					modelMetaDataEditor.onEntityType(TypeAndPropertyInfo.getTypeSignature(entityType)).addMetaData(newEntityAssignment);
//						nt.commit();
					return newEntityAssignment;
				}		
				
				return entityAssignment;
			}
			
			@Override
			public void noticeManipulation(Manipulation manipulation) {
				if(entityAssignment != null) {
					pac.mappedToEntityType = entityAssignment.getEntityType();
					if(!propertyMappingEntries.isEmpty()){
						for(PropertyMappingEntry entry : propertyMappingEntries.values()){
							entry.getPropertyAssignmentContext().mappedToEntityType = entityAssignment.getEntityType();
						}
					}
				}
				super.noticeManipulation(manipulation);
			}
		};
		mappedToEntityTypeBox.setRequiresRefresh(true);
		mappedToEntityTypeBox.addStyleName("mappedEntityTypeName");
		mappedToEntityTypeBox.setPropertyNameOfAssignment("entityType");
		mappedToEntityTypeBox.getElement().setAttribute("placeholder", "Assign entityType...");
		mappedToEntityTypeBox.getElement().getStyle().setTextAlign(TextAlign.LEFT);
		
		return mappedToEntityTypeBox;
	}
	
	public FlowPanel getMainPanel() {
		if(mainPanel == null){	
			mainPanel = new FlowPanel();
			mainPanel.addStyleName("smartMapperMainPanel");
		}
		return mainPanel;
	}	
	
	public SmartMapperActionMenu getActionMenu() {
		if(actionMenu == null){
			actionMenu = new SmartMapperActionMenu();
			actionMenu.setModelMetaDataEditor(modelMetaDataEditor);
		}
		return actionMenu;
	}
	
	/*public void handleTypeOverride(GmEntityTypeOverride override){
		if(this.entityTypeOverride != null && this.entityTypeOverride != override){
			//setGmMetaModel(null);
			session.listeners().entity(entityTypeOverride).remove(this);			
			entityTypeOverride = null;
		}
		
		initMetaModelTools();
		
		if(this.entityTypeOverride == null){
			this.entityTypeOverride = override;
			session.listeners().entity(entityTypeOverride).add(this);
		}
		
		if(this.entityTypeOverride != null)
			handleType(this.entityTypeOverride.getEntityType());
	}*/
	
	public void handleType(GmEntityTypeInfo entityTypeToHandle){
		if(this.entityType != null && this.entityType != entityTypeToHandle){
			//setGmMetaModel(null);
			session.listeners().entity(entityType).remove(this);			
			entityType = null;
		}else
			initMetaModelTools();
		
		if(entityType == null){
			this.entityType = entityTypeToHandle;
			if(gmMetaModel == null)
				setGmMetaModel(this.entityType.getDeclaringModel());
			session.listeners().entity(entityType).add(this);
		}
		
		getEntityTypeLabel().setText(TypeAndPropertyInfo.getTypeName(entityType));
		
		List<QualifiedEntityAssignment> entityAssignments = new ArrayList<>();
		try{
			entityAssignments = cmdResolver.entityTypeSignature(TypeAndPropertyInfo.getTypeSignature(entityType)).meta(QualifiedEntityAssignment.T).list();
		}catch(Exception ex){
			//NOP
		}
		
		QualifiedEntityAssignment ass = !entityAssignments.isEmpty() ? entityAssignments.get(0) : null;
		handleEntityAssignment(ass);
		if(this.entityAssignment != null && this.entityAssignment.getEntityType() != null) {
			refresh(GmEntityType.T, ass.getEntityType(), this::handleMappedType);
		}else {								
			handleProperties();
		}
		
		//validate();
	}
	
	public void handleEntityAssignment(QualifiedEntityAssignment entityAssignmentToHandle){
		if(this.entityAssignment != null && this.entityAssignment != entityAssignmentToHandle){
//			session.listeners().entity(entityAssignment).remove(this);
			session.listeners().entity(entityAssignment).remove(getIncrementalAccessTextBox());
			session.listeners().entity(entityAssignment).remove(getMappedToEntityTypeBox());	
			this.entityAssignment = null;
		}
		
		if(this.entityAssignment == null && entityAssignmentToHandle != null){
			this.entityAssignment = entityAssignmentToHandle;
			if(assignAllAsIsAction != null)
				assignAllAsIsAction.setHidden(false);
//			session.listeners().entity(entityAssignment).add(this);
			session.listeners().entity(entityAssignment).add(getIncrementalAccessTextBox());
			session.listeners().entity(entityAssignment).add(getMappedToEntityTypeBox());	
		}
		
		PropertyAssignmentContext pac = preparePropertyAssignmentContext(entityAssignment,"", null);		
		getIncrementalAccessTextBox().setPropertyAssignmentContext(pac);		
		getMappedToEntityTypeBox().setPropertyAssignmentContext(pac);	
	}
	
	public void handleProperties(){		
		
		for(GmProperty gmProperty : TypeAndPropertyInfo.getAllProperties(entityType)){
			
			PropertyMappingEntry pme = propertyMappingEntries.get(gmProperty.getName());
			if(pme == null){
				pme = new PropertyMappingEntry();
				getMainPanel().add(pme);
				propertyMappingEntries.put(gmProperty.getName(), pme);
			}
			GmPropertyInfo gmPropertyInfo = TypeAndPropertyInfo.getDirectProperty(entityType, gmProperty.getName());			
			pme.setPropertyAssignmentContext(preparePropertyAssignmentContext(null, gmProperty.getName(), gmPropertyInfo));			
		}
		
		forceLayout();
	}
	
	public void validate(){
//		if(!propertyMappingEntries.isEmpty()){
//			for(PropertyMappingEntry pme : propertyMappingEntries){
//				pme.validate();
//			}
//		}
	}

	@Override
	public ModelPath getContentPath() {
		return null;
	}

	@Override
	public void setContent(ModelPath modelPath) {
		try {
			disposeBean();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		GmEntityType entityType = modelPath.last().getValue();
		refresh(GmEntityType.T, entityType,this::handleResult);		
	}
	
	private void error(Throwable t) {
		t.printStackTrace();
	}
	
	private void handleResult(EntityQueryResultConvenience conv) {
		GmEntityType entityType = conv.first();
		modelMetaDataEditor.onEntityType(entityType).configure((typeInfo) -> {
			handleType(typeInfo);
		});	
	}
	
	/**
	 * @param conv - unneeded
	 */
	private void handleMappedType(EntityQueryResultConvenience conv) {
		//GmEntityType entityType = conv.first();				
		handleProperties();
	}
	
	private void refresh(EntityType<?> type, GenericEntity entity, Consumer<EntityQueryResultConvenience> consumer) {
		Object id = type.getIdProperty().get(entity);		
		EntityQuery query = EntityQueryBuilder.from(type).where().property(type.getIdProperty().getName()).eq(id).tc().negation().joker().done();
		session.query().entities(query).result(Future.async(this::error, consumer));
	}

	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		if(sl != null)
			selectionListeners.add(sl);
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		//NOP
	}

	@Override
	public ModelPath getFirstSelectedItem() {
		return modelPath;
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		return getFirstSelectedItem() != null ? Arrays.asList(getFirstSelectedItem()) : null;
	}

	@Override
	public boolean isSelected(Object element) {
		return false;
	}

	@Override
	public void select(int index, boolean keepExisting) {
		//NOP
	}

	@Override
	public GmContentView getView() {
		return this;
	}

	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.session = (ModelEnvironmentDrivenGmSession) gmSession;
		getActionMenu().setSession(this.session);
	}

	@Override
	public PersistenceGmSession getGmSession() {
		return this.session;
	}

	@Override
	public void configureUseCase(String useCase) {
		//NOP
	}

	@Override
	public String getUseCase() {
		return null;
	}

	@Override
	public void noticeManipulation(Manipulation manipulation) {
		/*
		if(manipulation instanceof PropertyManipulation){
			PropertyManipulation pm = (PropertyManipulation)manipulation;			
			Owner owner = pm.getOwner();
			
			if(GmEntityType.T.isAssignableFrom(owner.ownerEntityType())){
				Property property = owner.property();
				
				if(property.getName().equals("properties")){
					initMetaModelTools();
					
				}
			}
		}
		*/
		
		/*if(this.gmMetaModel != null && this.entityType != null){			
			Optional<GmCustomTypeOverride> op = this.gmMetaModel.getTypeOverrides().stream().filter((override) -> {
				return override.isGmEntityOverride() && ((GmEntityTypeOverride)override).getEntityType().getTypeSignature().equalsIgnoreCase(this.entityType.getTypeSignature());
			}).findAny();
			
			if(op.isPresent())
				handleTypeOverride((GmEntityTypeOverride) op.get());
			else
				handleType(this.entityType);
		}
		else */
		
		if(this.entityType != null)
			handleType(this.entityType);
		
		if(assignAllAsIsAction != null)
			assignAllAsIsAction.setHidden(entityAssignment == null);
	}

	@Override
	public void disposeBean() throws Exception {
		if(entityAssignment != null){
			session.listeners().entity(entityAssignment).remove(getIncrementalAccessTextBox());
			session.listeners().entity(entityAssignment).remove(getMappedToEntityTypeBox());	
			entityAssignment = null;
		}
		if(entityType != null){
			session.listeners().entity(entityType).remove(this);
			entityType = null;
		}
		if(gmMetaModel != null){
			session.listeners().entity(gmMetaModel).remove(this);
			gmMetaModel = null;
		}
		
		if(!propertyMappingEntries.isEmpty()){
			for(PropertyMappingEntry pme : propertyMappingEntries.values()){
				pme.dispose();
			}
			propertyMappingEntries.clear();
			getMainPanel().clear();
		}	
	}

	@Override
	public void intializeBean() throws Exception {
		//NOP
	}
	
	public List<Pair<String, ModelAction>> getSmapperActions() {
		if(smapperActions != null)
			return smapperActions;
		
		smapperActions = new ArrayList<>();
					
		assignAllAsIsAction = new ModelAction() {
			@Override
			public void perform(TriggerInfo triggerInfo) {
				List<PropertyAssignmentContext> assignmentContexts = new ArrayList<>();
				if(!propertyMappingEntries.isEmpty()){
					for(PropertyMappingEntry entry : propertyMappingEntries.values()){
						assignmentContexts.add(entry.getPropertyAssignmentContext());
					}
				}
				getActionMenu().assignAllAsIs(assignmentContexts);
			}
			
			@Override
			protected void updateVisibility() {
				setHidden(entityAssignment == null);
			}
		};
		assignAllAsIsAction.setHoverIcon(GmViewActionResources.INSTANCE.defaultActionIconLarge());
		assignAllAsIsAction.setIcon(GmViewActionResources.INSTANCE.defaultActionIconSmall());
		assignAllAsIsAction.setName("Assign All AsIs");
		assignAllAsIsAction.setHidden(false);
		assignAllAsIsAction.put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
		smapperActions.add(new Pair<>("Assign All AsIs", assignAllAsIsAction));
		
		ModelAction removeEntityAssignment = new ModelAction() {
			@Override
			public void perform(TriggerInfo triggerInfo) {
				modelMetaDataEditor.onEntityType(TypeAndPropertyInfo.getTypeSignature(entityType)).removeMetaData(md -> md == entityAssignment);
				List<PropertyAssignmentContext> assignmentContexts = new ArrayList<>();
				if(!propertyMappingEntries.isEmpty()){
					for(PropertyMappingEntry entry : propertyMappingEntries.values()){
						assignmentContexts.add(entry.getPropertyAssignmentContext());
					}
				}
				getActionMenu().removeAll(assignmentContexts);
				entityAssignment = null;
				getMappedToEntityTypeBox().pac.parentEntity = null;
				getMappedToEntityTypeBox().render();
			}
			
			@Override
			protected void updateVisibility() {
				//NOP
			}
		};
		removeEntityAssignment.setHoverIcon(GmViewActionResources.INSTANCE.defaultActionIconLarge());
		removeEntityAssignment.setIcon(GmViewActionResources.INSTANCE.defaultActionIconSmall());
		removeEntityAssignment.setName("Remove Mapping");
		removeEntityAssignment.setHidden(false);
		removeEntityAssignment.put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
		smapperActions.add(new Pair<>("Remove Mapping", removeEntityAssignment));
		
		return smapperActions;
	}
	
	public void showMenu(Widget widget, PropertyAssignmentContext pac){
		getActionMenu().showMenu(widget, pac);
	}
	
	public void showDetails(GenericEntity entity){
		if(entity != null){
			modelPath = new ModelPath();
			RootPathElement rootPathElement = new RootPathElement(entity);
			modelPath.add(rootPathElement);
		}else
			modelPath = null;
		fireGmSelectionListeners();
	}
	
	private void fireGmSelectionListeners() {
		if (selectionListeners != null) {
			for (GmSelectionListener listener : selectionListeners) {
				listener.onSelectionChanged(this);
			}
		}
	}

	private PropertyAssignmentContext preparePropertyAssignmentContext(GenericEntity parentEntity, String propertyName, GmPropertyInfo parentProperty){
		PropertyAssignmentContext pac = new PropertyAssignmentContext();
		
		pac.parentEntity = parentEntity;
		pac.propertyName = propertyName;
		pac.parentProperty = parentProperty;
		
		pac.smartMapper = this;
		pac.session = session;
		pac.spotlightPanelProvider = spotlightPanelProvider;
		pac.entityType = entityType;		
		pac.mappedToEntityType = entityAssignment != null ? entityAssignment.getEntityType() : null;
		
//		pac.inherited = inherited;
		
		return pac;
	}

	public String getMappedEntityTypeSignature() {
		return TypeAndPropertyInfo.getTypeSignature(entityType);
	}
	
	public PropertyAssignment fallbackResolving(String typeSignature, String propertyName){
		if (gmMetaModel == null)
			return null;
		
		Optional<GmCustomTypeOverride> overrideOp = this.gmMetaModel.getTypeOverrides().stream().filter((override) -> override.isGmEntityOverride()
				&& override.addressedType().getTypeSignature().equalsIgnoreCase(typeSignature.toLowerCase())).findAny();
		
		if (!overrideOp.isPresent())
			return null;
		
		GmEntityTypeOverride entityTypeOverride = (GmEntityTypeOverride) overrideOp.get();
		
		Optional<GmPropertyOverride> propertyOverrideOp = entityTypeOverride.getPropertyOverrides().stream()
			.filter((propertyOverride) -> propertyOverride.getProperty().getName().equalsIgnoreCase(propertyName)).findAny();
		
		if (!propertyOverrideOp.isPresent())
			return null;
		
		GmPropertyOverride propertyOverride = propertyOverrideOp.get();
		Optional<MetaData> propertyMetaDataOp = propertyOverride.getMetaData().stream()
			.filter((metaData) -> metaData instanceof PropertyAssignment).findFirst();
		
		return propertyMetaDataOp.isPresent() ? (PropertyAssignment) propertyMetaDataOp.get() : null;
	}
}
