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
package com.braintribe.gwt.gme.servicerequestpanel.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.gme.servicerequestpanel.client.resources.LocalizedText;
import com.braintribe.gwt.gme.templateevaluation.client.expert.TemplateQueryActionHandler.TemplateQueryOpener;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabElement;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.GmEntityView;
import com.braintribe.gwt.gmview.client.GmPropertyView;
import com.braintribe.gwt.gmview.client.ModelPathNavigationListener;
import com.braintribe.gwt.gmview.client.TemplateSupport;
import com.braintribe.gwt.gmview.ddsarequest.client.DdsaRequestExecution;
import com.braintribe.gwt.gmview.ddsarequest.client.DdsaRequestExecution.RequestExecutionData;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.accessapi.QueryAndSelect;
import com.braintribe.model.accessapi.QueryEntities;
import com.braintribe.model.accessapi.QueryProperty;
import com.braintribe.model.accessapi.QueryRequest;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.proxy.DynamicEntityType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.generic.validation.Validation;
import com.braintribe.model.generic.validation.ValidationKind;
import com.braintribe.model.generic.validation.expert.ValidationContext;
import com.braintribe.model.generic.validation.log.ValidationLog;
import com.braintribe.model.generic.validation.log.ValidationLogView;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.pagination.HasPagination;
import com.braintribe.model.processing.notification.api.NotificationFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.TransientPersistenceGmSession;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluationContext;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.template.Template;
import com.braintribe.model.workbench.ExecutionType;
import com.braintribe.model.workbench.TemplateServiceRequestAction;
import com.braintribe.model.workbench.TemplateServiceRequestBasedAction;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

/**
 * UI used for displaying the properties of a {@link ServiceRequest} to be executed. Additionally, it also supports
 * {@link TemplateServiceRequestAction} and, in such cases, the properties displayed will be template variables instead
 * of the properties of the {@link ServiceRequest} itself.
 * 
 * @author michel.docouto
 */
public class ServiceRequestExecutionPanel extends BorderLayoutContainer
		implements InitializableBean, DisposableBean, TemplateQueryOpenerView, ServiceRequestAutoPagingView {
	
	private Supplier<? extends GmEntityView> propertyPanelSupplier;
	private GmEntityView propertyPanel;
	//Don't change this to ServiceRequest, keep as Object. This is required due to a bug with ITW failing to check instanceof HasPagination because that type has no known type within GME.
	private Object serviceRequest;
	private String useCase;
	private TextButton evaluateOrExecuteButton;
	private PersistenceGmSession dataSession;
	private TransientPersistenceGmSession transientSession;
	private Supplier<? extends TransientPersistenceGmSession> transientSessionSupplier;
	private Supplier<? extends NotificationFactory> notificationFactorySupplier;
	private VerticalTabElement verticalTabElementToRemove;
	private ExplorerConstellation explorerConstellation;
	private Validation validation;
	private TemplateEvaluationContext templateEvaluationContext;
	private GenericEntity variableWrapperEntity;
	private ManipulationListener manipulationListener;
	private Map<String, Variable> variableCache;
	private boolean firstExecution;
	private TextButton editQueryButton;
	private TemplateQueryOpener templateQueryOpener;
	private CheckBox autoPagingCheckBox;
	private boolean useAutoPaging = true;
	private boolean pagingEditable;
	private int currentQueryStartIndex;
	private int autoPagingSize = 10;
	private ServiceRequestInitialExecutionListener serviceRequestInitialExecutionListener;
	private boolean useTemplateAutoPaging;
	private Integer currentTemplateStartIndex;
	
	/**
	 * Configures the required useCase.
	 */
	@Required
	public void setUseCase(String useCase) {
		this.useCase = useCase;
	}
	
	/**
	 * Configures the required data session.
	 */
	@Required
	public void setDataSession(PersistenceGmSession dataSession) {
		this.dataSession = dataSession;
	}
	
	/**
	 * Configures the required transient session.
	 */
	@Required
	public void setTransientSession(TransientPersistenceGmSession transientSession) {
		this.transientSession = transientSession;
	}
	
	/**
	 * Configures the required supplier for a new transient session.
	 */
	@Required
	public void setTransientSessionSupplier(Supplier<? extends TransientPersistenceGmSession> transientSessionSupplier) {
		this.transientSessionSupplier = transientSessionSupplier;
	}
	
	/**
	 * Configures the required {@link NotificationFactory} supplier.
	 */
	@Required
	public void setNotificationFactory(Supplier<? extends NotificationFactory> notificationFactorySupplier) {
		this.notificationFactorySupplier = notificationFactorySupplier;
	}
	
	/**
	 * Configures the required {@link ExplorerConstellation}.
	 */
	@Required
	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
	}
	
	/**
	 * Configures the expert used for validation purposes.
	 */
	@Required
	public void setValidation(Validation validation) {
		this.validation = validation;
	}
	
	/**
	 * Configures the required PropertyPanel supplier.
	 */
	@Required
	public void setPropertyPanelSupplier(Supplier<? extends GmEntityView> propertyPanelSupplier) {
		this.propertyPanelSupplier = propertyPanelSupplier;
	}
	
	/**
	 * Configures the auto paging size. Defaults to 10.
	 */
	@Configurable
	public void setAutoPagingSize(int autoPagingSize) {
		this.autoPagingSize = autoPagingSize;
	}
	
	@Override
	public void intializeBean() throws Exception {
		final ToolBar toolBar = new ToolBar();
		toolBar.setBorders(false);
		toolBar.add(getEvaluateOrExecuteButton());
		toolBar.add(getEditQueryButton());
		toolBar.add(getAutoPagingCheckBox());
		setEastWidget(toolBar, new BorderLayoutData(500));

		/*addDomHandler(event -> {
			if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
				evaluateOrExecuteServiceRequest();
		}, KeyDownEvent.getType());*/

		setCenterWidget((Widget) getPropertyPanel());
	}
	
	@Override
	public void configureTemplateQueryOpener(TemplateQueryOpener templateQueryOpener) {
		this.templateQueryOpener = templateQueryOpener;
	}
	
	@Override
	public void configureAutoPaging(boolean autoPaging, boolean pagingEditable) {
		this.useAutoPaging = autoPaging;
		this.pagingEditable = pagingEditable;
	}
	
	public void configureRequestData(ServiceRequest requestData) {
		//We needed to clone this, because somehow we were getting the data present in the workbench
		requestData = requestData.clone(new StandardCloningContext() {
			@Override
			public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
				return transientSession.create(entityType);
			}
			
			@Override
			public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property,
					GenericEntity instanceToBeCloned, GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {
				if (property.isIdentifying() || property.isGlobalId())
					return false;
				
				return super.canTransferPropertyValue(entityType, property, instanceToBeCloned, clonedInstance, sourceAbsenceInformation);
			}
		});
		
		serviceRequest = requestData;
		ModelPath modelPath = new ModelPath();
		modelPath.add(new RootPathElement(requestData));
		if (requestData.session() instanceof PersistenceGmSession)
			propertyPanel.configureGmSession((PersistenceGmSession) requestData.session());
		else
			propertyPanel.configureGmSession(transientSession);
		propertyPanel.configureUseCase(useCase);
		propertyPanel.setContent(modelPath);
		
		if (!(requestData instanceof HasPagination))
			autoPagingCheckBox.setVisible(false);
		else {
			autoPagingCheckBox.setVisible(pagingEditable);
			autoPagingCheckBox.setValue(useAutoPaging, false);
			handleAutoPagingChanged(useAutoPaging);
		}
		
		getEditQueryButton().setVisible(isHandlingQuery());
	}
	
	public void configureTemplateEvaluationContext(TemplateEvaluationContext templateEvaluationContext, TemplateServiceRequestBasedAction action) {
		serviceRequest = null;
		currentTemplateStartIndex = null;
		this.templateEvaluationContext = templateEvaluationContext;
		
		Template template = templateEvaluationContext.getTemplate();
		Set<Variable> variables = templateEvaluationContext.getTemplateEvaluation().collectVariables();
		
		// Create dynamic type for the template, create an instance of it, and set it's display info
		DynamicEntityType variableWrapperEntityType = TemplateSupport.createTypeForTemplate(template, variables, getClass());
		
		variableCache = variables.stream().collect(Collectors.toMap(Variable::getName, v -> v));
		
		if (variableWrapperEntity != null)
			transientSession.listeners().entity(variableWrapperEntity).remove(getManipulationListener());
		
		variableWrapperEntity = transientSession.create(variableWrapperEntityType);
		
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
		
		transientSession.listeners().entity(variableWrapperEntity).add(getManipulationListener());
		
		ModelPath modelPath = new ModelPath();
		modelPath.add(new RootPathElement(variableWrapperEntityType, variableWrapperEntity));
		
		propertyPanel.configureGmSession(transientSession);
		propertyPanel.configureUseCase(useCase);
		propertyPanel.setContent(modelPath);
		
		getEditQueryButton().setVisible(isHandlingQuery()); //TODO: maybe use this button for (in case of serviceRequest), show the serviceRequest in a new tab
		
		useTemplateAutoPaging = action.getAutoPaging();
		
		ExecutionType actionExecutionType = action.getExecutionType();
		if (ExecutionType.auto.equals(actionExecutionType) || ExecutionType.autoEditable.equals(actionExecutionType))
			evaluateOrExecuteServiceRequest();
	}
	
	public void addServiceRequestInitialExecutionListener(ServiceRequestInitialExecutionListener listener) {
		serviceRequestInitialExecutionListener = listener;
	}
	
	private ManipulationListener getManipulationListener() {
		if (manipulationListener != null)
			return manipulationListener;
		
		manipulationListener = TemplateSupport.getManipulationListener(templateEvaluationContext, variableCache);
		return manipulationListener;
	}
	
	protected GmEntityView getPropertyPanel() {
		if (propertyPanel != null)
			return propertyPanel;
		
		propertyPanel = propertyPanelSupplier.get();
		return propertyPanel;
	}
	
	protected PersistenceGmSession getDataSession() {
		return dataSession;
	}
	
	private TextButton getEvaluateOrExecuteButton() {
		if (evaluateOrExecuteButton != null)
			return evaluateOrExecuteButton;
		
		evaluateOrExecuteButton = new TextButton(LocalizedText.INSTANCE.execute());
		evaluateOrExecuteButton.setIcon(GmViewActionResources.INSTANCE.okBig());
		evaluateOrExecuteButton.setWidth(100);
		evaluateOrExecuteButton.setIconAlign(IconAlign.TOP);
		evaluateOrExecuteButton.setScale(ButtonScale.LARGE);
		evaluateOrExecuteButton.setToolTip(LocalizedText.INSTANCE.execute());
		evaluateOrExecuteButton.addSelectHandler(event -> evaluateOrExecuteServiceRequest());

		return evaluateOrExecuteButton;
	}
	
	private TextButton getEditQueryButton() {
		if (editQueryButton != null)
			return editQueryButton;
		
		editQueryButton = new TextButton(LocalizedText.INSTANCE.editQuery());
		editQueryButton.setWidth(100);
		editQueryButton.setIconAlign(IconAlign.TOP);
		editQueryButton.setScale(ButtonScale.LARGE);
		editQueryButton.setIcon(GmViewActionResources.INSTANCE.editBig());
		editQueryButton.setVisible(false);
		editQueryButton.addSelectHandler(event -> editQuery());
		return editQueryButton;
	}
	
	private CheckBox getAutoPagingCheckBox() {
		if (autoPagingCheckBox != null)
			return autoPagingCheckBox;
		
		autoPagingCheckBox = new CheckBox();
		autoPagingCheckBox.setBoxLabel(LocalizedText.INSTANCE.autoPaging());
		autoPagingCheckBox.setValue(true);
		autoPagingCheckBox.setVisible(false);
		autoPagingCheckBox.addValueChangeHandler(checked -> {
			useAutoPaging = !useAutoPaging;
			handleAutoPagingChanged(useAutoPaging);
		});
		return autoPagingCheckBox;
	}
	
	protected void evaluateOrExecuteServiceRequest() {
		evaluateOrExecuteServiceRequest(true);
	}
	
	protected void evaluateOrExecuteServiceRequest(boolean firstExecution) {
		this.firstExecution = firstExecution;
		if (serviceRequest != null) {
			validateServiceRequest();
			return;
		}
		
		Integer configuredAutoPagingSize = GMEMetadataUtil.getAutoPagingSize(templateEvaluationContext.getTemplate(), null, dataSession, useCase);
		if (configuredAutoPagingSize != null)
			this.autoPagingSize = configuredAutoPagingSize;
		validateVariableWrapperEntity(false).andThen(result -> {
			if (result.isEmpty()) {
				templateEvaluationContext.evaluateTemplate().andThen(serviceRequest -> {
					if (serviceRequest instanceof Query)
						serviceRequest = prepareQueryRequest((Query) serviceRequest, dataSession.getAccessId());
					if (currentTemplateStartIndex != null && serviceRequest instanceof HasPagination && useAutoPaging) {
						HasPagination hasPagination = (HasPagination) serviceRequest;
						hasPagination.setPageOffset(currentTemplateStartIndex);
						hasPagination.setPageLimit(autoPagingSize);
					}
					this.serviceRequest = serviceRequest;
					executeServiceRequest(true);
				}).onError(e -> {
					GlobalState.showError("Error while evaluating the template.", e);
				});
				
				return;
			}
			
			handleValidationFailure(result, ValidationKind.fail);
		}).onError(e -> {
			GlobalState.showError("Error while validating manipulations.", e);
		});
	}
	
	/**
	 * Prepares a {@link QueryRequest} based on the given {@link Query}.
	 */
	private QueryRequest prepareQueryRequest(Query query, String accessId) {
		QueryRequest queryRequest;
		if (query instanceof EntityQuery) {
			QueryEntities queryEntities = QueryEntities.T.create();
			queryEntities.setQuery((EntityQuery) query);
			queryRequest = queryEntities;
		} else if (query instanceof PropertyQuery) {
			QueryProperty queryProperty = QueryProperty.T.create();
			queryProperty.setQuery((PropertyQuery) query);
			queryRequest = queryProperty;
		} else {
			QueryAndSelect queryAndSelect = QueryAndSelect.T.create();
			queryAndSelect.setQuery((SelectQuery) query);
			queryRequest = queryAndSelect;
		}
		
		queryRequest.setServiceId(accessId);
		
		return queryRequest;
	}
	
	protected Object mergeData(Object data) {
		if (!isHandlingQuery() || !(data instanceof EntityQueryResult))
			return data;
		
		EntityQueryResult result = (EntityQueryResult) data;
		
		List<GenericEntity> entities = new ArrayList<>(result.getEntities());
		
		entities = dataSession.merge().adoptUnexposed(true).suspendHistory(true).doFor(entities);
		result.setEntities(entities);
		
		return result;
	}
	
	private Future<ValidationLog> validateVariableWrapperEntity(boolean allConfigured) {			
		ValidationContext validationContext = new ValidationContext();
		validationContext.setGmSession((PersistenceGmSession) variableWrapperEntity.session());
		validationContext.setGroupValidations(true);
		validationContext.setShortMessageStyle(true);			
		if (allConfigured) {
			validationContext.setCompleteConfiguredValidation(true);
			return validation.validateEntityWithContext(variableWrapperEntity, validationContext);
		} else {	
			GlobalState.showSuccess(LocalizedText.INSTANCE.validating());
			return validation.validateEntityWithContext(variableWrapperEntity, validationContext);
		}
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
		
		if (propertyPanel instanceof ValidationLogView)
			((ValidationLogView) propertyPanel).handleValidationLog(validationLog, validationKind);
	}
	
	private void validateServiceRequest() {
		validation.validateManipulations() //
				.andThen(result -> {
					if (result.isEmpty()) {
						executeServiceRequest(false);
						return;
					}

					Action showValidationLogAction = new Action() {
						@Override
						public void perform(TriggerInfo triggerInfo) {
							explorerConstellation.prepareValidationLog(result, transientSession).onError(Throwable::printStackTrace)
									.andThen(result -> verticalTabElementToRemove = result);
						}
					};
					showValidationLogAction.setName(LocalizedText.INSTANCE.details());
					GlobalState.showWarning(LocalizedText.INSTANCE.validationError(), showValidationLogAction);
					showValidationLogAction.perform(null);
				}).onError(e -> {
					ErrorDialog.show("Error while validating manipulations.", e);
					e.printStackTrace();
				});
	}
	
	private void executeServiceRequest(boolean resetServiceRequest) {
		boolean handlingPaging = false;
		
		if (firstExecution && serviceRequestInitialExecutionListener != null && serviceRequest instanceof ServiceRequest)
			serviceRequestInitialExecutionListener.onInitialServiceRequestExecution((ServiceRequest) serviceRequest);
		
		if ((useTemplateAutoPaging && templateEvaluationContext != null) || (useAutoPaging && templateEvaluationContext == null)) {
			Integer configuredAutoPagingSize = GMEMetadataUtil.getAutoPagingSize(
					templateEvaluationContext != null ? templateEvaluationContext.getTemplate() : null, null, dataSession, useCase);
			if (configuredAutoPagingSize != null)
				this.autoPagingSize = configuredAutoPagingSize;
			if (serviceRequest instanceof HasPagination) {
				HasPagination hasPagination = (HasPagination) serviceRequest;
				hasPagination.setPageOffset(firstExecution ? 0 : hasPagination.getPageOffset() + hasPagination.getPageLimit());
				hasPagination.setPageLimit(autoPagingSize);
				if (useTemplateAutoPaging)
					currentTemplateStartIndex = hasPagination.getPageOffset();
				handlingPaging = true;
			} else if (serviceRequest instanceof QueryRequest) {
				Query query;
				if (serviceRequest instanceof QueryEntities)
					query = ((QueryEntities) serviceRequest).getQuery();
				else if (serviceRequest instanceof QueryProperty)
					query = ((QueryProperty) serviceRequest).getQuery();
				else
					query = ((QueryAndSelect) serviceRequest).getQuery();
				
				// Create restriction of query if missing
				Restriction restriction = query.getRestriction();
				if (restriction == null) {
					restriction = Restriction.T.create();
					query.setRestriction(restriction);
				}
				
				// Create paging of query if missing
				Paging paging = restriction.getPaging();
				if (paging == null) {
					paging = Paging.T.create();
					restriction.setPaging(paging);
				} else
					currentQueryStartIndex = paging.getStartIndex();
				
				// Set auto paging values
				currentQueryStartIndex = firstExecution ? 0 : currentQueryStartIndex + autoPagingSize;
				paging.setStartIndex(currentQueryStartIndex);
				paging.setPageSize(autoPagingSize);
				handlingPaging = true;
			}
		}
		
		ModelPathNavigationListener listener = getModelPathNavigationListener(ServiceRequestExecutionPanel.this.getParent());
		//This will always be an instaceof ServiceRequest...
		if (!(serviceRequest instanceof ServiceRequest))
			return;

		RequestExecutionData requestExecutionData = new RequestExecutionData((ServiceRequest) serviceRequest, dataSession, transientSession, listener,
				transientSessionSupplier, notificationFactorySupplier, handlingPaging);
		
		requestExecutionData.setNavigateEvenIfHasNotification(true);
		if (isHandlingQuery())
			requestExecutionData.setSendTransientEnvelope(false);
		
		DdsaRequestExecution.executeRequest(requestExecutionData) //
				.andThen(result -> {
					if (resetServiceRequest)
						serviceRequest = null;
					
					firstExecution = false;
					
					/*if (result == null) { //TODO: do I need this??
						listener.onOpenModelPath(null);
						return;
					}*/
				});

		if (verticalTabElementToRemove != null) {
			explorerConstellation.removeVerticalTabElement(verticalTabElementToRemove);
			verticalTabElementToRemove = null;
		}
	}
	
	protected boolean isFirstExecution() {
		return firstExecution;
	}
	
	private ModelPathNavigationListener getModelPathNavigationListener(Widget widget) {
		if (widget instanceof ModelPathNavigationListener)
			return (ModelPathNavigationListener) widget;
		
		if (widget.getParent() != null)
			return getModelPathNavigationListener(widget.getParent());
		
		return null;
	}
	
	protected boolean isHandlingQuery() {
		if (serviceRequest instanceof QueryRequest)
			return true;
		
		if (serviceRequest != null)
			return false;
		
		if (templateEvaluationContext == null)
			return false;
		
		return templateEvaluationContext.getTemplate().getPrototype() instanceof Query;
	}
	
	private void editQuery() {
		templateQueryOpener.openTemplateQuery();
	}
	
	private void handleAutoPagingChanged(boolean useAutoPaging) {
		if (propertyPanel instanceof GmPropertyView) {
			if (useAutoPaging)
				((GmPropertyView) propertyPanel).hideProperties(Arrays.asList("pageLimit", "pageOffset"));
			else
				((GmPropertyView) propertyPanel).unhideProperties();
		}
	}
	
	@Override
	public void disposeBean() throws Exception {
		if (propertyPanel instanceof DisposableBean)
			((DisposableBean) propertyPanel).disposeBean();
		
		if (variableCache != null) {
			variableCache.clear();
			variableCache = null;
		}
		
		if (variableWrapperEntity != null)
			transientSession.listeners().entity(variableWrapperEntity).remove(getManipulationListener());
	}
	
	public static interface ServiceRequestInitialExecutionListener {
		public void onInitialServiceRequestExecution(ServiceRequest serviceRequest);
	}

}
