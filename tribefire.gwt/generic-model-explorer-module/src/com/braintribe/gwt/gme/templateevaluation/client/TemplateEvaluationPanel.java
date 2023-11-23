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
package com.braintribe.gwt.gme.templateevaluation.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.BrowsingConstellation;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gme.propertypanel.client.PropertyPanel;
import com.braintribe.gwt.gme.tetherbar.client.TetherBarElement;
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
import com.braintribe.gwt.gmview.client.TemplateSupport;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.qc.api.client.QueryProviderContext;
import com.braintribe.gwt.qc.api.client.QueryProviderView;
import com.braintribe.gwt.qc.api.client.QueryProviderViewListener;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.proxy.DynamicEntityType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluationContext;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.QueryResult;
import com.braintribe.model.template.Template;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class TemplateEvaluationPanel extends BorderLayoutContainer implements GmEntityView, GmListView, GmCheckSupport, GmInteractionSupport,
		GmActionSupport, GmContentSupport, InitializableBean, ManipulationListener, QueryProviderView<Template> {

	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	
	private BrowsingConstellation browsingConstellation;
	private PropertyPanel propertyPanel;
	private TextButton evaluateButton;
	private TextButton cancelButton;
	private ModelEnvironmentDrivenGmSession workbenchPersistenceSession;
	
	private TemplateEvaluationContext templateEvaluationContext;
	private final List<QueryProviderViewListener> queryProviderViewListeners = new ArrayList<>();
	private QueryProviderContext queryProviderContext;
	
	private Map<String, Variable> variableCache;
	private Future<Object> future;
	
	public TemplateEvaluationPanel() {
		//setHeaderVisible(false);
		//setBodyBorder(false);
//		setLayout(new RowLayout(Orientation.VERTICAL));
	}
	
	@Override
	public void setOtherModeQueryProviderView(Supplier<? extends QueryProviderView<Template>> otherModelQueryProviderView) {
		//Nothing to do
	}
	
	@Override
	public void modeQueryProviderViewChanged() {
		//Nothing to do
	}
	
	@Required
	public void setPropertyPanel(PropertyPanel propertyPanel) {
		this.propertyPanel = propertyPanel;
		this.propertyPanel.setMaxCollectionSize(Integer.MAX_VALUE);
		//this.propertyPanel.setActionManager(null);
		this.browsingConstellation.configureCurrentContentView(propertyPanel);
	}
	
	public void setBrowsingConstellation(BrowsingConstellation browsingConstellation) {
		this.browsingConstellation = browsingConstellation;
	}
	
	public void setTemplateEvaluationContext(TemplateEvaluationContext templateEvaluationContext) {
		this.templateEvaluationContext = templateEvaluationContext;
		initialize();
	}
	
	public TextButton getEvaluateButton() {
		if (evaluateButton != null)
			return evaluateButton;
		
		evaluateButton = new TextButton(LocalizedText.INSTANCE.ok());
		evaluateButton.setToolTip(LocalizedText.INSTANCE.executeDescription());
		evaluateButton.setIconAlign(IconAlign.TOP);
		evaluateButton.setScale(ButtonScale.LARGE);
		evaluateButton.setIcon(ConstellationResources.INSTANCE.finish());
		evaluateButton.setEnabled(true);
		evaluateButton.addSelectHandler(event -> {
			templateEvaluationContext.evaluateTemplate() //
					.onError(e -> ErrorDialog.show(LocalizedText.INSTANCE.errorEvaluatingTemplate(), e)) //
					.andThen(future::onSuccess);
		});
		return evaluateButton;
	}
	
	public TextButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new TextButton(LocalizedText.INSTANCE.cancel());
			cancelButton.setToolTip(LocalizedText.INSTANCE.cancelDescription());
			cancelButton.setIconAlign(IconAlign.TOP);
			cancelButton.setScale(ButtonScale.LARGE);
			cancelButton.setIcon(ConstellationResources.INSTANCE.cancel());
			cancelButton.setEnabled(true);
			cancelButton.addSelectHandler(event -> future.onFailure(new TemplateEvaluationCancelledException("template evaluation was cancelled")));
		}
		return cancelButton;
	}
	
	@Override
	public void configureGmSession(PersistenceGmSession workbenchPersistenceSession) {
		if (workbenchPersistenceSession instanceof ModelEnvironmentDrivenGmSession)
			this.workbenchPersistenceSession = (ModelEnvironmentDrivenGmSession) workbenchPersistenceSession;
		else throw new RuntimeException("The configured session must be an instance of ModelEnvironmentDrigemGmSession.");
	}
	
	@Override
	public void configureUseCase(String useCase) {
		// Nothing to do here?
	}
			
	@Override
	public void intializeBean() throws Exception {
		propertyPanel.configureGmSession(workbenchPersistenceSession);
		browsingConstellation.configureGmSession(workbenchPersistenceSession);
		/*
		add(getTemplateTechnicalNameField(), new RowData(1, -1, new Margins(4)));
		add(getTemplateNameField(), new RowData(1, -1, new Margins(4)));
		add(getTemplateDescriptionField(), new RowData(1, -1, new Margins(4)));
		add(getProtoTypeSignatureField(), new RowData(1, -1, new Margins(4)));
		*/
		//add(propertyPanel, new RowData(1, 1, new Margins(4)));
//		add(getEvaluateButton());
		
//		setCenterWidget(propertyPanel);
		setCenterWidget(browsingConstellation);
		
		ToolBar toolBar = new ToolBar();
		toolBar.setBorders(false);
		toolBar.getElement().getStyle().setBackgroundColor("white");
		//toolBar.setAlignment(HorizontalAlignment.RIGHT);
		toolBar.add(new FillToolItem());
		toolBar.add(getEvaluateButton());
		toolBar.add(getCancelButton());
		
//		addButton(getEvaluateButton());
//		addButton(getCancelButton());
		
		setSouthWidget(toolBar, new BorderLayoutData(85));
		forceLayout();
//		layout(true);
	}
	
	private void initialize() {
		Template template = templateEvaluationContext.getTemplate();
		Set<Variable> variables = templateEvaluationContext.getTemplateEvaluation().collectVariables();
		
		
		// Create dynamic type for the template, create an instance of it, and set it's display info
		DynamicEntityType variableWrapperEntityType = TemplateSupport.createTypeForTemplate(template, variables, getClass());
		//TemplateSupport.addNameMd(variableWrapperEntityType, template);
		
		GenericEntity variableWrapperEntity = workbenchPersistenceSession.create(variableWrapperEntityType);
		
		
		variableCache = new FastMap<>();
		try {
			for (Variable variable : variables) {
				variableCache.put(variable.getName(), variable);
				
				Object value = templateEvaluationContext.getTemplatePreprocessing().getVariableValues().get(variable.getName());
				
				GenericModelType variableType = GMF.getTypeReflection().getType(variable.getTypeSignature());
				if (variableType.isCollection()) {
					Object collection = ((CollectionType) variableType).createPlain();
					if (value instanceof Collection)
						((Collection<Object>) collection).addAll((Collection<Object>) value);
					else if (value instanceof Map)
						((Map<Object, Object>) collection).putAll((Map<Object, Object>) value);
					value = collection;
				}
				
				variableWrapperEntityType.getProperty(variable.getName()).set(variableWrapperEntity, value);
			}
		} catch(Exception ex) {
			ErrorDialog.show("Error while initializing variable wrapper", ex);
			ex.printStackTrace();
		}
		
		workbenchPersistenceSession.listeners().entity(variableWrapperEntity).add(this);
		
		ModelPath modelPath = new ModelPath();
		modelPath.add(new RootPathElement(variableWrapperEntityType, variableWrapperEntity));

		//ValueDescriptionBean bean = getValueDescriptionBean(variableWrapperEntity, variableWrapperEntityType, null);
		//TetherBarElement entityElement = new TetherBarElement(modelPath, bean.getValue(), bean.getDescription(), browsingConstellation.getCurrentContentView());

		ModelMdResolver modelMdResolver = workbenchPersistenceSession.getModelAccessory().getMetaData();
		String displayName = GMEMetadataUtil.getEntityNameMDOrShortName(variableWrapperEntityType, modelMdResolver, null);
		String displayDescription = GMEMetadataUtil.getEntityDescriptionMDOrShortName(variableWrapperEntityType, modelMdResolver, null);
		
		
		TetherBarElement entityElement = new TetherBarElement(modelPath, displayName, displayDescription, browsingConstellation.getCurrentContentView());
		browsingConstellation.getTetherBar().insertTetherBarElement(0, entityElement);
		browsingConstellation.getTetherBar().setSelectedThetherBarElement(entityElement);
		browsingConstellation.setContent(modelPath);
	}
	
	@Override
	public PersistenceGmSession getGmSession() {
		return workbenchPersistenceSession;
	}
	
	@Override
	public String getUseCase() {
		return null;
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		if (!(manipulation instanceof PropertyManipulation))
			return;
		
		Owner owner = ((PropertyManipulation) manipulation).getOwner();
		if (!(owner instanceof LocalEntityProperty))
			return;
			
		String propertyName = ((LocalEntityProperty) owner).getPropertyName();
		Variable variable = variableCache.get(propertyName);
		if (variable == null)
			return;
		
		if (manipulation instanceof ChangeValueManipulation)
			templateEvaluationContext.getTemplatePreprocessing().getVariableValues().put(variable.getName(), ((ChangeValueManipulation) manipulation).getNewValue());
		else if (manipulation instanceof AddManipulation) {
			AddManipulation bulkInsertToCollectionManipulation = (AddManipulation) manipulation;
			Object oldValues = templateEvaluationContext.getTemplatePreprocessing().getVariableValues().get(variable.getName());
			GenericModelType type = typeReflection.getType(variable.getTypeSignature());
			Collection<Object> newValues = (Collection<Object>) ((CollectionType) type).createPlain();
			if (oldValues instanceof Collection)
				((Collection<Object>) oldValues).forEach(object -> newValues.add(object));
			
			for (Object item : bulkInsertToCollectionManipulation.getItemsToAdd().values()) {
				if (!newValues.contains(item))
					newValues.add(item);		
			}
			templateEvaluationContext.getTemplatePreprocessing().getVariableValues().put(variable.getName(), newValues);
		}
		//variable.setDefaultValue(((ChangeValueManipulation) manipulation).getNewValue());
	}
	
	public Future<Object> getEvaluatedPrototype(){
		future = new Future<>();
		future.andThen(result -> {
			if (result instanceof Query)
				fireOnQueryPerform();
		});
		
		return future;
	}
	
	@Override
	public ModelPath getContentPath() {
		return browsingConstellation.getContentPath();
	}
	
	@Override
	public void configureExternalActions(List<Pair<ActionTypeAndName, ModelAction>> actions) {
		browsingConstellation.configureExternalActions(actions);
	}
	
	@Override
	public List<Pair<ActionTypeAndName, ModelAction>> getExternalActions() {
		return browsingConstellation.getExternalActions();
	}
	
	@Override
	public void configureActionGroup(ActionGroup actionGroup) {
		browsingConstellation.configureActionGroup(actionGroup);
	}

	@Override
	public void setActionManager(GmContentViewActionManager actionManager) {
		browsingConstellation.setActionManager(actionManager);
	}

	@Override
	public GmContentViewActionManager getGmContentViewActionManager() {
		return browsingConstellation.getGmContentViewActionManager();
	}
	
	@Override
	public void setContent(ModelPath modelPath) {
		browsingConstellation.setContent(modelPath);
	}
	
	@Override
	public void addContent(ModelPath modelPath) {
		browsingConstellation.addContent(modelPath);
	}
	
	@Override
	public List<ModelPath> getAddedModelPaths() {
		return browsingConstellation.getAddedModelPaths();
	}
	
	@Override
	public void configureTypeForCheck(GenericModelType typeForCheck) {
		browsingConstellation.configureTypeForCheck(typeForCheck);
	}

	@Override
	public void addGmContentViewListener(GmContentViewListener listener) {
		browsingConstellation.addGmContentViewListener(listener);
	}

	@Override
	public void removeGmContentViewListener(GmContentViewListener listener) {
		browsingConstellation.removeGmContentViewListener(listener);
	}

	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		browsingConstellation.addSelectionListener(sl);
	}
	
	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		browsingConstellation.removeSelectionListener(sl);
	}

	@Override
	public ModelPath getFirstSelectedItem() {
		return browsingConstellation.getFirstSelectedItem();
	}
	
	@Override
	public GmContentView getView() {
		return browsingConstellation;
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		return browsingConstellation.getCurrentSelection();
	}

	@Override
	public boolean isSelected(Object element) {
		return browsingConstellation.isSelected(element);
	}

	@Override
	public void select(int index, boolean keepExisting) {
		browsingConstellation.select(index, keepExisting);
	}

	@Override
	public void addInteractionListener(GmInteractionListener il) {
		browsingConstellation.addInteractionListener(il);
	}

	@Override
	public void removeInteractionListener(GmInteractionListener il) {
		browsingConstellation.removeInteractionListener(il);
	}

	@Override
	public void addCheckListener(GmCheckListener cl) {
		browsingConstellation.addCheckListener(cl);
	}

	@Override
	public void removeCheckListener(GmCheckListener cl) {
		browsingConstellation.removeCheckListener(cl);	
	}

	@Override
	public ModelPath getFirstCheckedItem() {
		return browsingConstellation.getFirstCheckedItem();
	}

	@Override
	public List<ModelPath> getCurrentCheckedItems() {
		return browsingConstellation.getCurrentCheckedItems();
	}

	@Override
	public boolean isChecked(Object element) {
		return browsingConstellation.isChecked(element);
	}
	
	@Override
	public boolean uncheckAll() {
		return browsingConstellation.uncheckAll();
	}

	@Override
	public Widget getWidget() {
		return this;
	}

	@Override
	public QueryProviderContext getQueryProviderContext() {
		return queryProviderContext;
	}

	@Override
	public void notifyQueryPerformed(QueryResult queryResult, QueryProviderContext queryProviderContext) {
		this.queryProviderContext = queryProviderContext;
	}

	@Override
	public void setEntityContent(Template entityContent) {
		templateEvaluationContext.setTemplate(entityContent);
		initialize();
	}

	@Override
	public void addQueryProviderViewListener(QueryProviderViewListener listener) {
		queryProviderViewListeners.add(listener);
	}

	@Override
	public void removeQueryProviderViewListener(QueryProviderViewListener listener) {
		queryProviderViewListeners.remove(listener);
	}
	
	@Override
	public void focusEditor() {
		// noop
	}
	
	private void fireOnQueryPerform() {
		queryProviderViewListeners.forEach(listener -> listener.onQueryPerform(getQueryProviderContext()));
	}
	
}
