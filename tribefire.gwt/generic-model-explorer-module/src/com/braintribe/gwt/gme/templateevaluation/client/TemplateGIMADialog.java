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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.BrowsingConstellationDialog.ValueDescriptionBean;
import com.braintribe.gwt.gme.constellation.client.GIMADialog;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gme.constellation.client.gima.PropertyPanelGIMAView;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gme.propertypanel.client.PropertyPanel;
import com.braintribe.gwt.gme.tetherbar.client.TetherBar.TetherBarListener;
import com.braintribe.gwt.gme.tetherbar.client.TetherBarElement;
import com.braintribe.gwt.gmview.action.client.IgnoreKeyConfigurationDialog;
import com.braintribe.gwt.gmview.client.AlternativeGmSessionHandler;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmEntityView;
import com.braintribe.gwt.gmview.client.InstanceSelectionData;
import com.braintribe.gwt.gmview.client.SelectionConfig;
import com.braintribe.gwt.gmview.client.TemplateSupport;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMTypeInstanceBean;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.proxy.DynamicEntityType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.generic.validation.Validation;
import com.braintribe.model.generic.validation.ValidationKind;
import com.braintribe.model.generic.validation.expert.ValidationContext;
import com.braintribe.model.generic.validation.log.ValidationLog;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluationContext;
import com.braintribe.model.template.Template;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.core.client.util.KeyNav;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;

public class TemplateGIMADialog extends GIMADialog implements IgnoreKeyConfigurationDialog {

	private static final Logger logger = new Logger(TemplateGIMADialog.class);
	
	private Map<String, Variable> variableCache;
	private ModelEnvironmentDrivenGmSession workbenchPersistenceSession;	
	private Future<Object> futureObject;
	//private final List<QueryProviderViewListener> queryProviderViewListeners = new ArrayList<>();
	//private QueryProviderContext queryProviderContext;
	private TemplateEvaluationContext templateEvaluationContext = null;
	private Supplier<? extends Function<SelectionConfig, Future<InstanceSelectionData>>> selectionFutureSupplier;
	private Set<String> singleTypesToShowGIMA;
	private GenericEntity variableWrapperEntity;
	//private boolean validationInitialized = false;
	private ManipulationListener templateManipulationListener;
	
	public TemplateGIMADialog() {
		/*
		this.addShowHandler(event -> {
			if (!validationInitialized) {
				handleInitValidation(tetherBar.getSelectedElement());
				validationInitialized = true;
			}
		});
		*/
	}
		
	@Override
	protected TextButton prepareCancelButton() {
		cancelButton = new TextButton(LocalizedText.INSTANCE.cancel());
		cancelButton.setToolTip(LocalizedText.INSTANCE.cancelDescription());
		cancelButton.setIconAlign(IconAlign.TOP);
		cancelButton.setScale(ButtonScale.LARGE);
		cancelButton.setIcon(ConstellationResources.INSTANCE.cancel());
		cancelButton.addStyleName(GIMADialog.GIMA_CANCEL_BUTTON);
		cancelButton.addSelectHandler(event -> {
			if (isMultiElements()) {
				performGoBack(true);
				return;
			}
			
			if (templateEvaluationContext != null)
				futureObject.onFailure(new TemplateEvaluationCancelledException("template evaluation was cancelled"));
			hide(true);
		});

		return cancelButton;
	}
	
	@Override
	public void performApply(boolean checkMainButton) {
		if (checkMainButton && currentGimaView != null && currentGimaView.getMainButton() != null)
			return;
		
		handleApply(false);
	}

	@Override	
	protected TextButton prepareApplyButton() {
		applyButton = new TextButton(LocalizedText.INSTANCE.apply());
		applyButton.setToolTip(LocalizedText.INSTANCE.applyDescription());
		applyButton.setIconAlign(IconAlign.TOP);
		applyButton.setScale(ButtonScale.LARGE);
		applyButton.setIcon(ConstellationResources.INSTANCE.finish());
		applyButton.setEnabled(true);
		applyButton.addSelectHandler(event -> handleApply(true));
		applyButton.addStyleName(GIMADialog.GIMA_MAIN_BUTTON);

		return applyButton;
	}
	
	public void setButtonsText(String applyText, String applyDescriptionText, String cancelDescriptionText) {
		if (applyText != null) {
			currentGimaView.getMainButton().setText(applyText);
			applyButton.setText(applyText);
		}
		
		if (applyDescriptionText != null) {
			currentGimaView.getMainButton().setToolTip(applyDescriptionText);
			applyButton.setToolTip(applyDescriptionText);
		}
		
		if (cancelDescriptionText != null)
			cancelButton.setToolTip(cancelDescriptionText);
	}
	
	private void handleApply(boolean checkMainButton) {
		if (checkMainButton && currentGimaView.getMainButton() != null)
			return;
		
		if (currentGimaView instanceof PropertyPanelGIMAView)
			((PropertyPanelGIMAView) currentGimaView).getPropertyPanel().completeEditing();		
		
		GenericEntity validateEntity = getWorkingEntity(tetherBar.getSelectedElement());
		boolean multiEl = isMultiElements();
		
		if (!multiEl)
			hide();
		//setPosition(-1000, -1000);
		//show();
		
		if (validateEntity == null) {
			handleValidationSuccess();
			return;
		}
		
		validateVariableWrapperEntity(validateEntity, false).onError(e -> {
			if (!multiEl) {
				updateHeight();
				center();									
				show();
			}
			GlobalState.showError("Error while validating manipulations.", e);
			e.printStackTrace();
		}).andThen(result -> {
			if (result.isEmpty()) {
				handleValidationSuccess();
				return;
			}
			
			if (!multiEl) {
				updateHeight();
				center();
				show();
			}
			
			handleValidationFailure(result, ValidationKind.fail);
		});
	}
	
	private void handleInitValidation(TetherBarElement element) {
		if (element == null)
			return;
		
		GenericEntity validateEntity = getWorkingEntity(element);
		if (validateEntity == null) 
			return;

		validateVariableWrapperEntity(validateEntity, true).andThen(result -> {
			if (result.isEmpty()) 
				return;
			
			handleValidationFailure(result, ValidationKind.info);
		});
		
	}
	
	private Future<ValidationLog> validateVariableWrapperEntity(GenericEntity validationEntity, boolean allConfigured) {			
		Validation validation = getValidation(validationEntity);
		ValidationContext validationContext = new ValidationContext();
		validationContext.setGmSession((PersistenceGmSession) validationEntity.session());
		validationContext.setGroupValidations(true);
		validationContext.setShortMessageStyle(true);			
		if (allConfigured) {
			validationContext.setCompleteConfiguredValidation(true);
			return validation.validateEntityWithContext(validationEntity, validationContext);
		} else {	
			GlobalState.showSuccess(LocalizedText.INSTANCE.validating());
			return validation.validateEntityWithContext(validationEntity, validationContext);
		}
	}
	
	@Override
	public Validation getValidation(GenericEntity entity) {
		Validation validation = validationSupplier.get();
		
		PersistenceGmSession theSession = workbenchPersistenceSession;
		if (entity!= null && entity.session() instanceof PersistenceGmSession)
			theSession = (PersistenceGmSession) entity.session();

		validation.getPredator().setGmSession(theSession);		
		return validation;
	}
	
	private void handleValidationSuccess() {
		if (isMultiElements()) {
			performGoBack(false);
			return;
		}
		
		if (templateEvaluationContext == null) {
			hide(false);
			return;
		}
		
		handleTemplateEvaluation();
	}
	
	private GenericEntity getWorkingEntity(TetherBarElement element) {
		if (element == null || element.getModelPath() == null)
			return null;
		
		ModelPathElement modelPathElement = element.getModelPath().last();
		Object object = modelPathElement.getValue();
		if (object instanceof GenericEntity)
			return (GenericEntity) object;
		
		
		return variableWrapperEntity;
	}
	
	private void handleValidationFailure(ValidationLog validationLog, ValidationKind validationKind) {		
		if (validationKind.equals(ValidationKind.fail)) {
			GlobalState.showWarning(LocalizedText.INSTANCE.validationError());
			new Timer() {
				@Override
				public void run() {
					GlobalState.clearState();
				}
			}.schedule(5000);
		}
		
		//RVE - validation should go to PP, which will select failure entries		
		TetherBarElement element = tetherBar.getSelectedElement();
		if (element.getContentView() != null) {
			if (element.getContentView() instanceof PropertyPanelGIMAView) {
				PropertyPanel propertyPanel = ((PropertyPanelGIMAView) element.getContentView()).getPropertyPanel();
				propertyPanel.handleValidationLog(validationLog, validationKind);				
			}			
		}
		
		/*
		ValidationLogRepresentation validationLogRepresentation = validationLogRepresenationSupplier.get();
		validationLogRepresentation.configureGmSession(workbenchPersistenceSession);
		validationLogRepresentation.setValidationLog(validationLog);
		
		GIMAValidationLogView gimaView = new GIMAValidationLogView(validationLogRepresentation, this);
		
		TetherBarElement validationElement = new TetherBarElement(null, LocalizedText.INSTANCE.validationLog(),
				LocalizedText.INSTANCE.validationLog(), gimaView);
		tetherBar.addTetherBarElement(validationElement);
 		tetherBar.setSelectedThetherBarElement(validationElement);
 		prepareToolBar(gimaView);
 		*/
	}
	
	private void handleTemplateEvaluation() {
		templateEvaluationContext.evaluateTemplate() //
				.andThen(futureObject::onSuccess) //
				.onError(Throwable::printStackTrace);
	}
	
	@Required
	public void setWorkbenchSession(ModelEnvironmentDrivenGmSession workbenchPersistenceSession) {
		if (this.workbenchPersistenceSession != null)
			this.workbenchPersistenceSession.listeners().remove(getManipulationListener());
		
		this.workbenchPersistenceSession = workbenchPersistenceSession;
		
		if (workbenchPersistenceSession != null)
			workbenchPersistenceSession.listeners().add(getManipulationListener());
	}
	
	/**
	 * Configures the required selection future supplier, used for cases where only one variable is needed.
	 */
	@Required
	public void setSelectionFutureSupplier(Supplier<? extends Function<SelectionConfig, Future<InstanceSelectionData>>> selectionFutureSupplier) {
		this.selectionFutureSupplier = selectionFutureSupplier;
	}
	
	/**
	 * Configures the set of type signatures where GIMA should be displayed no matter what.
	 * This means cases where only 1 variable of entityType exists, then GIMA will still be displayed.
	 * Basically, types which have an ExtendedInlineField should be addressed.
	 */
	@Configurable
	public void setSingleTypesToShowGIMA(Set<String> singleTypesToShowGIMA) {
		this.singleTypesToShowGIMA = singleTypesToShowGIMA;
	}
	
	@Override
	public PersistenceGmSession getSessionForTransactionAndCMD() {
		return workbenchPersistenceSession;
	}
	
	@Override
	protected Supplier<? extends GmEntityView> getPropertyPanelProvider(final Supplier<? extends GmEntityView> provider, boolean isInstantiation,
			boolean handlingAdd) {
		return () -> {
			GmEntityView view = provider == null ? propertyPanelProvider.get() : provider.get();
			view.configureUseCase(useCase);
			view.configureGmSession(getSessionForTransactionAndCMD());
			if (view instanceof AlternativeGmSessionHandler)
				((AlternativeGmSessionHandler) view).configureAlternativeGmSession(gmSession);
			
			PropertyPanelGIMAView finalView = new PropertyPanelGIMAView(view, this, isInstantiation, handlingAdd);
			if (finalView.getView() instanceof Widget) {
				KeyNav keyNav = new KeyNav() {
					@Override
					public void onEnter(NativeEvent evt) {
						PropertyPanel propertyPanel = finalView.getPropertyPanel();
						if (!propertyPanel.isEditing() && !propertyPanel.wasEditionFinishedByEnter())
							handleApply(false);
					}
				};
				keyNav.bind((Widget) finalView.getView());
			}
			
			prepareToolBar(finalView);
			return finalView;
		};
	}
	
	public boolean setTemplateEvaluationContext(TemplateEvaluationContext templateEvaluationContext, String title) {
		//validationInitialized = false;
		this.templateEvaluationContext = templateEvaluationContext;
				
		//NestedTransaction nestedTransaction = gmSession.getTransaction().beginNestedTransaction();
		NestedTransaction nestedTransaction = workbenchPersistenceSession.getTransaction().beginNestedTransaction();
		nestedTransactions.add(nestedTransaction);
		nestedTransaction.addTransactionFrameListener(getTransactionFrameListener());
		

		//getTemplateTechnicalNameField().setText(template.getTechnicalName());
		//getTemplateNameField().setText(template.getName() != null ? I18nTools.getLocalized(template.getName()) : "");
		//getTemplateDescriptionField().setText(template.getDescription() != null ? I18nTools.getLocalized(template.getDescription()) : "");		
		//getProtoTypeSignatureField().setText(template.getPrototypeTypeSignature());
		
		Template template = templateEvaluationContext.getTemplate();
		Set<Variable> variables = templateEvaluationContext.getTemplateEvaluation().collectVariables();
		
		int variablesSize = variables.size();
		logger.info("Found "+variablesSize+" variables in template.");
		
		// Create dynamic type for the template, create an instance of it, and set it's display info
		DynamicEntityType variableWrapperEntityType = TemplateSupport.createTypeForTemplate(template, variables, getClass());
		
		List<Variable> visibleVariables = TemplateSupport.extractVisibleVariables(variables, template);
		if (visibleVariables.size() == 1) {
			Variable variable = visibleVariables.get(0);
			GenericModelType variableType = GMF.getTypeReflection().getType(variable.getTypeSignature());
			if (variableType.isEntity() && (singleTypesToShowGIMA == null || !singleTypesToShowGIMA.contains(variable.getTypeSignature()))) {
				if (isVariableMandatory(variable, template)) {
					displaySelection((EntityType<?>) variableType, variable.getName(), variableWrapperEntityType, title);
					return false;
				}
			}
		}
		
		variableCache = variables.stream().collect(Collectors.toMap(Variable::getName, v -> v));
		
		if (variableWrapperEntity != null)
			workbenchPersistenceSession.listeners().entity(variableWrapperEntity).remove(getTemplateManipulationListener());
		
		workbenchPersistenceSession.suspendHistory();
		variableWrapperEntity = workbenchPersistenceSession.create(variableWrapperEntityType);
		//TemplateSupport.addNameMd(variableWrapperEntityType, template);
		
		try {
			for (Variable variable : variables) {
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
		} catch(Exception ex){
			ErrorDialog.show("Error while initializing variable wrapper", ex);
			ex.printStackTrace();
		}
		workbenchPersistenceSession.resumeHistory();
		
		workbenchPersistenceSession.listeners().entity(variableWrapperEntity).add(getTemplateManipulationListener());
		
		ModelPath modelPath = new ModelPath();
		modelPath.add(new RootPathElement(variableWrapperEntityType, variableWrapperEntity));

		modelPathList.add(modelPath);
		
		if (!initialized) {
			borderLayoutContainer.setNorthWidget(tetherBar, new BorderLayoutData(TETHER_BAR_SIZE));
			borderLayoutContainer.setSouthWidget(prepareToolBar(), new BorderLayoutData(61));
			
			tetherBar.addTetherBarListener(new TetherBarListener() {
				
				@Override
				public void onTetherBarElementsRemoved(List<TetherBarElement> tetherBarElementsRemoved) {
					// NOP
					
				}
				
				@Override
				public void onTetherBarElementSelected(TetherBarElement tetherBarElement) {
					if (tetherBarElement == null)
						return;
					
					handleInitValidation(tetherBarElement);
				}
				
				@Override
				public void onTetherBarElementAdded(TetherBarElement tetherBarElementAdded) {
					//NOP
				}
			});
			
			initialized = true;
		}		
		
		ValueDescriptionBean bean = getValueDescriptionBean(variableWrapperEntity, variableWrapperEntityType, null);
		TetherBarElement entityElement = new TetherBarElement(modelPath, bean.getValue(), bean.getDescription(),
				getPropertyPanelProvider(propertyPanelProvider, false, false));
		//tetherBar.insertTetherBarElement(0, entityElement);
		int entityIndex = tetherBar.getSelectedElementIndex() + 1;
		tetherBar.insertTetherBarElement(entityIndex, entityElement);
		tetherBar.setSelectedThetherBarElement(entityElement);

		GmContentView contentViewIfProvided = entityElement.getContentViewIfProvided();
		PropertyPanelGIMAView propertyPanelGIMAView = contentViewIfProvided instanceof PropertyPanelGIMAView ? (PropertyPanelGIMAView) contentViewIfProvided : null;
		if (propertyPanelGIMAView != null) {
			propertyPanelGIMAView.getPropertyPanel().setUseDialogSettings(true);
			propertyPanelGIMAView.startEditing();
		}
		
		return true;
	}
	
	private boolean isVariableMandatory(Variable variable, Template template) {
		List<Variable> mandatoryList = TemplateSupport.extractMandatoryVariables(Arrays.asList(variable), template);
		return !mandatoryList.isEmpty();
	}

	/**
	 * // PGA TODO later somehow handle property DisplayInfo (if at all possible, but probably not)
	 * @param entity - used for the Metadata evaluation
	 * @param useCase - used for the Metadata evaluation
	 */
	private ValueDescriptionBean getValueDescriptionBean(GenericEntity entity, EntityType<?> entityType, String useCase) {
		/*CascadingMetaDataResolver cascadingMetaDataResolver = workbenchPersistenceSession.getModelAccessory().getCascadingMetaDataResolver();
		String selectiveInformation = SelectiveInformationResolver.resolve(entityType, entity, cascadingMetaDataResolver, useCase/*, null);
		String displayInfo = GMEUtil.getEntityNameMDOrShortName(entityType, cascadingMetaDataResolver, useCase);
		if (selectiveInformation != null && !selectiveInformation.trim().isEmpty()) {
			value = selectiveInformation;
		} else {
			value = displayInfo;
		}*/
		
		ModelMdResolver modelMdResolver = workbenchPersistenceSession.getModelAccessory().getMetaData().lenient(true);
		String displayName = GMEMetadataUtil.getEntityNameMDOrShortName(entityType, modelMdResolver, null);
		return new ValueDescriptionBean(displayName, displayName);
	}
			
	/*public void addQueryProviderViewListener(QueryProviderViewListener listener) {
		queryProviderViewListeners.add(listener);
	}

	public void removeQueryProviderViewListener(QueryProviderViewListener listener) {
		queryProviderViewListeners.remove(listener);
	}
	
	private void fireOnQueryPerform(){
		for(QueryProviderViewListener queryProviderViewListener : queryProviderViewListeners){
			queryProviderViewListener.onQueryPerform(getQueryProviderContext());
		}
	}

	public QueryProviderContext getQueryProviderContext() {
		return this.queryProviderContext;
	}*/
	
	private void displaySelection(EntityType<?> entityType, String variableName, EntityType<?> parentEntityType, String title) {
		ModelMdResolver modelMdResolver = workbenchPersistenceSession.getModelAccessory().getMetaData().lenient(true);
		PropertyMdResolver propertyMdResolver = modelMdResolver
				.entityType(parentEntityType).property(variableName);
		SelectionConfig selectionConfig = new SelectionConfig(entityType, 1, null, workbenchPersistenceSession, workbenchPersistenceSession,
				propertyMdResolver, modelMdResolver, null);
		selectionConfig.setTitle(title);
		
		selectionFutureSupplier.get().apply(selectionConfig).andThen(instanceSelectionData -> {
			List<GMTypeInstanceBean> result = instanceSelectionData == null ? null : instanceSelectionData.getSelections();
			if (result == null || result.isEmpty() || templateEvaluationContext == null)
				return;
			
			templateEvaluationContext.getTemplateEvaluation().getVariableValues().put(variableName, result.get(0).getInstance());
			handleTemplateEvaluation();
		});
	}
	
	private Future<Object> getInternEvaluatedPrototype(){
		futureObject = new Future<>();
		futureObject.andThen(result -> { /*NOOP*/});
		return futureObject;
	}
	
	public Future<Object> getEvaluatedPrototype(){
		Future<Object> result = getInternEvaluatedPrototype();
		futureList.add(result);
		futureResults.put(result, null);

		result.andThen(object -> { /*hide(false)*/});
		return result;
	}
	
	private ManipulationListener getTemplateManipulationListener() {
		if (templateManipulationListener != null)
			return templateManipulationListener;
		
		templateManipulationListener = TemplateSupport.getManipulationListener(templateEvaluationContext, variableCache);
		return templateManipulationListener;
	}

	@Override
	public void disposeBean() {
		if (variableWrapperEntity != null)
			workbenchPersistenceSession.listeners().entity(variableWrapperEntity).remove(getTemplateManipulationListener());
		
		setWorkbenchSession(null);
		
		if (variableCache != null) {
			variableCache.clear();
			variableCache = null;
		}
		
		super.disposeBean();
	}
	
}
