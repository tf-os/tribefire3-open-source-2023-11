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
package com.braintribe.gwt.templateeditor.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.gme.assemblypanel.client.AssemblyPanel;
import com.braintribe.gwt.gme.assemblypanel.client.LocalizedText;
import com.braintribe.gwt.gmview.action.client.ActionGroup;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.client.GmActionSupport;
import com.braintribe.gwt.gmview.client.GmCheckListener;
import com.braintribe.gwt.gmview.client.GmCheckSupport;
import com.braintribe.gwt.gmview.client.GmContentSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewActionManager;
import com.braintribe.gwt.gmview.client.GmContentViewListener;
import com.braintribe.gwt.gmview.client.GmEntityView;
import com.braintribe.gwt.gmview.client.GmInteractionListener;
import com.braintribe.gwt.gmview.client.GmInteractionSupport;
import com.braintribe.gwt.gmview.client.GmListView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.processing.am.AssemblyMonitoring;
import com.braintribe.model.processing.am.EntityMigrationListener;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.template.Template;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;

public class TemplateEditorPanel extends ContentPanel implements InitializableBean, ManipulationListener, EntityMigrationListener, GmEntityView,
		GmListView, GmCheckSupport, GmInteractionSupport, GmActionSupport, GmContentSupport, ManipulationRepresentationListener {
	
	private Supplier<AssemblyPanel> assemblyPanelProvider;
	private AssemblyPanel assemblyPanel;
	private HTML emptyPanel;
	private ContentPanel scriptPanelWrapper;
	private VerticalLayoutContainer scriptPanel;
	private ModelPath modelPath;
	//private ModelPath currentSelectedModelPath;
	
	private Template template;
	
	private List<Manipulation> postProcessedManipulations = new ArrayList<>();
	private PersistenceGmSession session;
	private String useCase;
	//private GmContentViewActionManager actionManager;
	private TemplateEditorManager templateEditorManager;
	private AssemblyMonitoring assemblyMonitoring;
	
	private List<GmSelectionListener> gmSelectionListeners = new ArrayList<>();
	private Map<Manipulation, ManipulationRepresentation<Manipulation>> manipulationRepresentations = new HashMap<>();
	private Map<GenericEntity, FlowPanel> entityElements = new HashMap<>();
	//private GenericEntity currentSelectedEntity;
	
	public TemplateEditorPanel() {
		setHeading("Template");
		setBorders(false);
		setBodyBorder(false);
		setHeaderVisible(false);
//		setLayout(new BorderLayout());
		
		templateEditorManager = new TemplateEditorManager();
	}
	
	public void setAssemblyPanelProvider(Supplier<AssemblyPanel> assemblyPanelProvider) {
		this.assemblyPanelProvider = assemblyPanelProvider;
	}
	
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}
	
	public ContentPanel getScriptPanelWrapper() {
		if(scriptPanelWrapper == null){
			scriptPanelWrapper = new ContentPanel();
			scriptPanelWrapper.setHeading("Script");
			scriptPanelWrapper.setBorders(false);
			scriptPanelWrapper.setBodyBorder(false);
			scriptPanelWrapper.setHeaderVisible(true);
//			scriptPanelWrapper.setLayout(new FitLayout());
			scriptPanelWrapper.getElement().applyStyles("overflowX: hidden; overflowY: scroll");
			scriptPanelWrapper.add(getEmptyPanel());
		}
		return scriptPanelWrapper;
	}
	
	public VerticalLayoutContainer getScriptPanel() {
		if(scriptPanel == null){
			scriptPanel = new VerticalLayoutContainer();
			scriptPanel.getElement().applyStyles("backgroundColor: white");
			scriptPanel.setWidth("100%");
		}
		return scriptPanel;
	}
	
	private HTML getEmptyPanel() {
		if (emptyPanel == null) {
			emptyPanel = new HTML();
			
			StringBuilder html = new StringBuilder();
			html.append("<div style='height: 100%; width: 100%; display: table;' class='emptyStyle'>");
			html.append("<div style='display: table-cell; vertical-align: middle'>").append(LocalizedText.INSTANCE.noItemsToDisplay()).append("</div></div>");
			emptyPanel.setHTML(html.toString());
			
//			emptyPanel.setBorders(false);
		}
		
		return emptyPanel;
	}
	
	@Override
	public void setContent(final ModelPath modelPath) {		
		clearRootPath();
		processSetContent(modelPath);
	}
	
	@Override
	public void addContent(ModelPath modelPath) {
		assemblyPanel.addContent(modelPath);
	}
	
	private void processSetContent(ModelPath modelPath) {
		this.modelPath = modelPath;
		
		if (modelPath == null) {
			assemblyPanel.setContent(null);
			return;
		}
		
		template = modelPath.last().getValue();
		//EntityType<Template> templateType = Template.T;
		
		Object prototype = template.getPrototype();	
		
		assemblyMonitoring = AssemblyMonitoring.newInstance().build(session, template);
		assemblyMonitoring.addEntityMigrationListener(this);
		assemblyMonitoring.addManpiulationListener(this);		

		if (prototype != null) {
			templateEditorManager.analyzePrototype(prototype);	
			
			ModelPath prototypeModelPath = new ModelPath();
			EntityType<GenericEntity> type = null;
			if (template.getPrototypeTypeSignature() != null)
				type = GMF.getTypeReflection().getEntityType(template.getPrototypeTypeSignature());
			else
				type = ((GenericEntity)prototype).entityType();
			RootPathElement rootPathElement = new RootPathElement(type, prototype);
			prototypeModelPath.add(rootPathElement);
			
			//session.listeners().entity((GenericEntity) prototype).add(this);
		}
		
		assemblyPanel.setContent(modelPath);
		processScript();
	}
	
	private void processScript(){
		if(template.getScript() != null){
			CompoundManipulation script = (CompoundManipulation) template.getScript();
			postProcessedManipulations.clear();
			if (!script.getCompoundManipulationList().isEmpty() && template.getScript() != null
					&& !((CompoundManipulation) template.getScript()).getCompoundManipulationList().isEmpty()) {
				postProcessedManipulations.addAll(((CompoundManipulation) template.getScript()).getCompoundManipulationList());
			}
			updateScriptPanel();
		}
	}
	
	@SuppressWarnings({ "rawtypes" })
	private List<Manipulation> updateScriptPanel(){		
		getScriptPanel().clear();
		
		if (postProcessedManipulations.isEmpty()) {
			if (getScriptPanel().getParent() != null)
				getScriptPanelWrapper().remove(getScriptPanel());
			getScriptPanelWrapper().add(getEmptyPanel());
		} else {
			if(getEmptyPanel().getParent() != null)
				getScriptPanelWrapper().remove(getEmptyPanel());
			
			getScriptPanelWrapper().add(getScriptPanel());
			
			for(final Manipulation manipulation : postProcessedManipulations){
				ManipulationRepresentation representation = null;
				if(manipulation instanceof ChangeValueManipulation)
					representation = new ChangeValueManipulationRepresentation();					
				else if(manipulation instanceof AddManipulation)
					representation = new AddManipulationRepresentation();
				
				if (representation != null) {
					final ManipulationRepresentation finalRepresentation = representation;
					representation.setSession(session);
					representation.setGmSelectionListeners(gmSelectionListeners);
					representation.setManipulationRepresentationListener(this);
					representation.renderManipulation(manipulation).andThen(result -> {
						manipulationRepresentations.put(manipulation, finalRepresentation);
						getScriptPanel().add((Widget) result);
						forceLayout();
					}).onError(e -> ErrorDialog.show("Error while getting entity", (Throwable) e));
				}
			}
		}
		
		forceLayout();
		
		return postProcessedManipulations;
	}


	@Override
	public ModelPath getContentPath() {
		return modelPath;
	}
	
	@Override
	public void configureExternalActions(List<Pair<ActionTypeAndName, ModelAction>> actions) {
		assemblyPanel.configureExternalActions(actions);
	}
	
	@Override
	public List<Pair<ActionTypeAndName, ModelAction>> getExternalActions() {
		return assemblyPanel.getExternalActions();
	}

	@Override
	public void configureActionGroup(ActionGroup actionGroup) {
		assemblyPanel.configureActionGroup(actionGroup);
	}

	@Override
	public void setActionManager(GmContentViewActionManager actionManager) {
		//this.actionManager = actionManager;
	}
	
	@Override
	public GmContentViewActionManager getGmContentViewActionManager() {
		//return this.actionManager;
		return null;
	}	
	
	@Override
	public List<ModelPath> getAddedModelPaths() {
		return assemblyPanel.getAddedModelPaths();
	}

	@Override
	public void configureTypeForCheck(GenericModelType typeForCheck) {
		assemblyPanel.configureTypeForCheck(typeForCheck);
	}

	private void clearRootPath() {
		getScriptPanel().clear();
	}

	@Override
	public void addGmContentViewListener(GmContentViewListener listener) {
		assemblyPanel.addGmContentViewListener(listener);
	}

	@Override
	public void removeGmContentViewListener(GmContentViewListener listener) {
		assemblyPanel.removeGmContentViewListener(listener);
	}

	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		gmSelectionListeners.add(sl);
		assemblyPanel.addSelectionListener(sl);
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		gmSelectionListeners.remove(sl);
		assemblyPanel.removeSelectionListener(sl);
	}

	@Override
	public ModelPath getFirstSelectedItem() {
		return null;
		//return currentSelectedModelPath;
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		return Arrays.asList(modelPath);
	}

	@Override
	public boolean isSelected(Object element) {
		return assemblyPanel.isSelected(element);
	}

	@Override
	public void select(int index, boolean keepExisting) {
		assemblyPanel.select(index, keepExisting);
	}

	@Override
	public GmContentView getView() {
		return this;
	}

	@Override
	public void addInteractionListener(GmInteractionListener il) {
		assemblyPanel.addInteractionListener(il);
	}

	@Override
	public void removeInteractionListener(GmInteractionListener il) {
		assemblyPanel.removeInteractionListener(il);
	}

	@Override
	public void addCheckListener(GmCheckListener cl) {
		assemblyPanel.addCheckListener(cl);
	}

	@Override
	public void removeCheckListener(GmCheckListener cl) {
		assemblyPanel.removeCheckListener(cl);
	}

	@Override
	public ModelPath getFirstCheckedItem() {
		return modelPath;
	}

	@Override
	public List<ModelPath> getCurrentCheckedItems() {
		return assemblyPanel.getCurrentCheckedItems();
	}

	@Override
	public boolean isChecked(Object element) {
		return assemblyPanel.isChecked(element);
	}

	@Override
	public boolean uncheckAll() {
		return assemblyPanel.uncheckAll();
	}

	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.session = gmSession;	
		assemblyPanel.configureGmSession(session);
	}

	@Override
	public PersistenceGmSession getGmSession() {
		return this.session;
	}

	@Override
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
		assemblyPanel.configureUseCase(useCase);
	}

	@Override
	public String getUseCase() {
		return useCase;
	}

	@Override
	public void onJoin(GenericEntity entity) {
		//NOP
	}

	@Override
	public void onLeave(GenericEntity entity) {
		//NOP
	}

	@Override
	public void noticeManipulation(Manipulation manipulation) {
		PropertyManipulation propertyManipulation = (PropertyManipulation) manipulation;
		LocalEntityProperty owner = (LocalEntityProperty) propertyManipulation.getOwner();
		String propertyName = owner.getPropertyName();
		GenericEntity entity = owner.getEntity();
		if(entity == template && propertyName.equals("script"))
			processScript();
		else if(entity == template.getScript() && propertyName.equals("compoundManipulationList"))
			processScript();
		else if(entity instanceof Manipulation && manipulation instanceof ChangeValueManipulation){
			ChangeValueManipulation changeValueManipulation = (ChangeValueManipulation) manipulation;
			ChangeValueManipulation inverseChangeValueManipulation = (ChangeValueManipulation) changeValueManipulation.getInverseManipulation();
			ManipulationRepresentation<?> manipulationRepresentation = manipulationRepresentations.get(entity);
			
			if(manipulationRepresentation != null)
				manipulationRepresentation.changeValue(inverseChangeValueManipulation.getNewValue(), changeValueManipulation.getNewValue());
		}
	}

	@Override
	public void intializeBean() throws Exception {		
		assemblyPanel = assemblyPanelProvider.get();
		assemblyPanel.setPrepareToolBarActions(false);
		BorderLayoutContainer borderLayoutContainer = new BorderLayoutContainer();
		borderLayoutContainer.setNorthWidget(assemblyPanel, new BorderLayoutData(350));
		borderLayoutContainer.setCenterWidget(getScriptPanelWrapper());
		add(borderLayoutContainer);
	}
	
	/*private void fireSelectionChanged(){
		for(GmSelectionListener sl : gmSelectionListeners)
			sl.onSelectionChanged(this);
	}*/

	@Override
	public boolean isElementSelected(GenericEntity entity) {
		//return entity == currentSelectedEntity;
		return false;
	}

	@Override
	public void setCurrentSelectedElement(GenericEntity entity) {	
		/*if(!isElementSelected(entity)){
			FlowPanel element = entityElements.get(entity);
			element.removeStyleName("selected");
		}
		currentSelectedEntity = entity;
		FlowPanel element = entityElements.get(entity);
		element.addStyleName("selected"); */
	}

	@Override
	public void putEntityElement(GenericEntity entity, FlowPanel element) {
		entityElements.put(entity, element);
	}
	
}
