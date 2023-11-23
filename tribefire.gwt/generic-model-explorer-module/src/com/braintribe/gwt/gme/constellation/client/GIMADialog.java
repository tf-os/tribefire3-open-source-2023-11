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
package com.braintribe.gwt.gme.constellation.client;

import static com.braintribe.model.processing.session.api.common.GmSessions.getMetaData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.gwt.action.adapter.gxt.client.ButtonActionAdapter;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.gima.ButtonConfiguration;
import com.braintribe.gwt.gme.constellation.client.gima.GIMAActionSelectionView;
import com.braintribe.gwt.gme.constellation.client.gima.GIMASelectionContentView;
import com.braintribe.gwt.gme.constellation.client.gima.GIMATypeSelectionView;
import com.braintribe.gwt.gme.constellation.client.gima.GIMAValidationLogView;
import com.braintribe.gwt.gme.constellation.client.gima.GIMAView;
import com.braintribe.gwt.gme.constellation.client.gima.MasterDetailGIMAView;
import com.braintribe.gwt.gme.constellation.client.gima.PropertyPanelGIMAView;
import com.braintribe.gwt.gme.constellation.client.gima.field.GIMAEntityFieldView;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gme.propertypanel.client.PropertyPanel;
import com.braintribe.gwt.gme.propertypanel.client.PropertyPanelListener;
import com.braintribe.gwt.gme.tetherbar.client.TetherBar;
import com.braintribe.gwt.gme.tetherbar.client.TetherBar.TetherBarListener;
import com.braintribe.gwt.gme.tetherbar.client.TetherBarElement;
import com.braintribe.gwt.gmview.action.client.IgnoreKeyConfigurationDialog;
import com.braintribe.gwt.gmview.action.client.InstantiationActionHandler;
import com.braintribe.gwt.gmview.action.client.ObjectAssignmentConfig;
import com.braintribe.gwt.gmview.client.ControllableView;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmEntityView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.InstantiatedEntityListener;
import com.braintribe.gwt.gmview.client.InstantiationData;
import com.braintribe.gwt.gmview.client.WorkWithEntityActionListener;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ClosableWindow;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ListItemPathElement;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyPathElement;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.path.SetItemPathElement;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.generic.validation.GmValidationSupport;
import com.braintribe.model.generic.validation.Validation;
import com.braintribe.model.generic.validation.ValidationKind;
import com.braintribe.model.generic.validation.expert.ValidationContext;
import com.braintribe.model.generic.validation.log.ValidationLog;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.api.transaction.TransactionException;
import com.braintribe.model.processing.session.api.transaction.TransactionFrameListener;
import com.braintribe.model.processing.session.impl.managed.history.BasicNestedTransaction;
import com.braintribe.model.workbench.InstantiationAction;
import com.braintribe.model.workbench.TemplateBasedAction;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.widget.core.client.button.SplitButton;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

/**
 * Dialog used for editing a ModelPathElement
 * @author michel.docouto
 *
 */
public class GIMADialog extends ClosableWindow implements WorkWithEntityActionListener, InstantiatedEntityListener, ControllableView, DisposableBean,
		GmValidationSupport, IgnoreKeyConfigurationDialog {
	
	private static final int CANCEL_BUTTONS_COUNT = 2;
	public static final String GIMA_MAIN_BUTTON = "gmeGimaMainButton";
	public static final String GIMA_ADDITIONAL_BUTTON = "gmeGimaAdditionalButton";
	protected static final String GIMA_CANCEL_BUTTON = "gmeGimaCancelButton";
	protected static final int TETHER_BAR_SIZE = 40;
	private static final Logger logger = new Logger(GIMADialog.class);
	
	protected TetherBar tetherBar;
	protected boolean initialized = false;
	protected TextButton applyAllButton;
	protected TextButton applyButton;
	protected TextButton cancelAllButton;
	protected TextButton cancelButton;
	protected PersistenceGmSession gmSession;
	protected Supplier<? extends GmEntityView> propertyPanelProvider;
	protected GmContentView currentContentView;
	protected String useCase;
	private GmSelectionListener selectionListener;
	private Supplier<MasterDetailConstellation> masterDetailConstellationProvider;
	protected List<ModelPath> modelPathList = new ArrayList<>();
	private Map<GmContentView, ModelPath> contentViewModelPathMap;
	private Timer manipulationListenerTimer;
	protected List<NestedTransaction> nestedTransactions = new ArrayList<>();
	private TransactionFrameListener transactionFrameListener;
	private ManipulationListener manipulationListener;
	private Supplier<? extends GmContentView> adaptedMasterDetailConstellationProvider;
	protected BorderLayoutContainer borderLayoutContainer;
	private boolean undoOnCancel = false;
	private ToolBar toolBar;
	private boolean closable = true;
	private Supplier<GIMATypeSelectionView> typeSelectionViewSupplier;
	private Supplier<GIMAActionSelectionView> actionSelectionViewSupplier;
	protected boolean isShown;
	protected GIMAView currentGimaView;
	private GIMAView lastViewConfiguredToolBar;
	private int cancelIndex;
	private InstantiationActionHandler instantiationActionHandler;
	private ButtonActionAdapter mainButtonActionAdapter;
	private boolean isApplyingAll;
	private boolean isCancelingAll;
	protected List<Future<? extends Object>> futureList = new ArrayList<>();
	protected Map<Future<? extends Object>, Object> futureResults = new HashMap<>();
	private List<GIMAView> viewsHandlingInstantiation = new ArrayList<>();
	private boolean hideTetherBar;
	private boolean applyAllWasUsed;
	protected Supplier<Validation> validationSupplier;
	private boolean ignoreValidation = false;
	
	public GIMADialog() {
		setDefaultSize();
		setOnEsc(true);
		setClosable(false);
		setModal(true);
		setMinWidth(450);
		setMinHeight(200);
		addStyleName("gimaDialog");
		addStyleName("gmeDialog");
		setBodyBorder(false);
		setBorders(false);
		getHeader().setHeight(20);
		getHeader().addStyleName("gimaHeader");
		
		borderLayoutContainer = new BorderLayoutContainer();
		borderLayoutContainer.addStyleName("gimaBody");
		setWidget(borderLayoutContainer);
	}
	
	@Required
	public void setTetherBar(TetherBar tetherBar) {
		this.tetherBar = tetherBar;
		
		tetherBar.addTetherBarListener(new TetherBarListener() {
			@Override
			public void onTetherBarElementsRemoved(List<TetherBarElement> tetherBarElementsRemoved) {
				boolean multiElements = isMultiElements() && isCurrentViewApplyAllHandler();
				applyAllButton.setVisible(multiElements);
				applyButton.setVisible(!multiElements);
				cancelAllButton.setVisible(multiElements);
				cancelButton.setVisible(!multiElements);
				
				updateApplyAllButton();
			}
			
			@Override
			public void onTetherBarElementSelected(TetherBarElement tetherBarElement) {
				configureCurrentContentView(tetherBarElement.getContentView());
			}
			
			@Override
			public void onTetherBarElementAdded(TetherBarElement tetherBarElementAdded) {
				boolean multiElements = isMultiElements() && isCurrentViewApplyAllHandler();
				applyAllButton.setVisible(multiElements);
				applyButton.setVisible(!multiElements);
				cancelAllButton.setVisible(multiElements);
				cancelButton.setVisible(!multiElements);
				//Scheduler.get().scheduleDeferred(() -> toolBar.forceLayout());
			}
		});
	}
	
	@Required
	public void setGmSession(final PersistenceGmSession gmSession) {
		if (this.gmSession != null) {
			this.gmSession.listeners().remove(getManipulationListener());
			if (manipulationListenerTimer != null)
				manipulationListenerTimer.cancel();
		}
		
		this.gmSession = gmSession;
		
		if (gmSession != null)
			gmSession.listeners().add(getManipulationListener());
	}
	
	@Required
	public void setPropertyPanelProvider(Supplier<? extends GmEntityView> propertyPanelProvider) {
		this.propertyPanelProvider = propertyPanelProvider;
	}
	
	@Required
	public void setMasterDetailConstellationProvider(Supplier<MasterDetailConstellation> masterDetailConstellationProvider) {
		this.masterDetailConstellationProvider = masterDetailConstellationProvider;
	}
	
	@Required
	public void setUseCase(String useCase) {
		this.useCase = useCase;
	}
	
	/**
	 * Configures the required handle that will handle InstantiationActions.
	 */
	@Required
	public void setInstantiationActionHandler(InstantiationActionHandler instantiationActionHandler) {
		this.instantiationActionHandler = instantiationActionHandler;
	}

	/**
	 * Configures the required {@link Validation} for checking whether the data filled is valid.
	 */
	@Required
	public void setValidation(Supplier<Validation> validationSupplier) {
		this.validationSupplier = validationSupplier;
	}

 	/**
	 * Configures the view for type selection.
	 */
	@Configurable
	public void setTypeSelectionViewSupplier(Supplier<GIMATypeSelectionView> typeSelectionViewSupplier) {
		this.typeSelectionViewSupplier = typeSelectionViewSupplier;
	}
	
	/**
	 * Configures the view for action selection.
	 */
	@Configurable
	public void setActionSelectionViewSupplier(Supplier<GIMAActionSelectionView> actionSelectionViewSupplier) {
		this.actionSelectionViewSupplier = actionSelectionViewSupplier;
	}
	
	/**
	 * Configures whether the validation should be ignored. Defaults to false.
	 */
	@Configurable
	public void setIgnoreValidation(boolean ignoreValidation) {
		this.ignoreValidation = ignoreValidation;
	}
	
	public Future<Boolean> showForModelPathElement(ModelPath modelPath) {
		return showForModelPathElement(modelPath, true, null);
	}
	
	public Future<Boolean> showForModelPathElement(ModelPath modelPath, boolean checkPropertyPanel,
			Supplier<? extends GmEntityView> propertyPanelProvider) {
		Future<Boolean> future = new Future<>();
		futureList.add(future);
		futureResults.put(future, true);
		prepareForModelPath(modelPath, false, checkPropertyPanel, true, propertyPanelProvider);
		
		return future;
	}
	
	public boolean showForInstantiation(InstantiationData instantiationData, Future<GenericEntity> instantiationFuture) {
		futureList.add(instantiationFuture);
		futureResults.put(instantiationFuture, null);

 		NestedTransaction nestedTransaction = getSessionForTransactionAndCMD().getTransaction().beginNestedTransaction();
		nestedTransactions.add(nestedTransaction);
		nestedTransaction.addTransactionFrameListener(getTransactionFrameListener());

		initializeNorthAndSouthWidgets();

		boolean typeSelectionShown = false;
 		EntityType<?> entityType = (EntityType<?>) instantiationData.getType();
 		if (entityType == null)
 			handleEntityTypeInstantiation(instantiationData.getPathElement().getValue(), instantiationData, false);
 		else {
 			if (hasTypesToSelect(instantiationData)) {
				prepareTypeSelectionView(instantiationData);
				typeSelectionShown = true;
 			} else
				handleEntityTypeInstantiation(instantiationData, entityType);
 			viewsHandlingInstantiation.add(currentGimaView);
 		}

 		String title = instantiationData.getGimaTitle();
		if (title != null)
			setHeading(title);

 		return typeSelectionShown;
	}
	
	/**
	 * Shows a new view in GIMA for selecting between the given actions.
	 */
	public void showForActionSelection(List<TemplateBasedAction> workbenchActions, Future<TemplateBasedAction> actionSelectionFuture) {
		futureList.add(actionSelectionFuture);
		futureResults.put(actionSelectionFuture, null);
		
		initializeNorthAndSouthWidgets();
		
		GIMAActionSelectionView view = actionSelectionViewSupplier.get();
 		view.configureGmSession(getSessionForTransactionAndCMD());
		prepareToolBar(view);
		view.apply(workbenchActions) //
				.andThen(result -> {
					futureResults.put(futureList.get(futureList.size() - 1), result);
					handleHideOrBack(false, false);
				}).onError(e -> {
					ErrorDialog.show(LocalizedText.INSTANCE.errorInstantiatingEntity(), e);
					e.printStackTrace();
				});

		TetherBarElement element = new TetherBarElement(null, LocalizedText.INSTANCE.chooseAction(), "", view);
		tetherBar.addTetherBarElement(element);

 		tetherBar.setSelectedThetherBarElement(element);

 		if (!isShown) {
			//RVE - set position out of the screen, because when resize it, than can flicker, later is centered on screen
			setPosition(-1000, -1000);
			show();
 			Scheduler.get().scheduleDeferred(this::center);
		}
	}
	
	private boolean hasTypesToSelect(InstantiationData instantiationData) {
		EntityType<?> entityType = (EntityType<?>) instantiationData.getType();
		return instantiationData.getInstantiationActions() != null || entityType.isAbstract()
				|| GMEUtil.getInstantiableSubTypesDefinedInMetaModel(entityType, getSessionForTransactionAndCMD()).size() > 1;
	}
	
	public void showForSelection() {
		initializeNorthAndSouthWidgets();

		if (!isShown) {
	 		//RVE - set position out of the screen, because when resize it, than can flicker, later is centered on screen
			setPosition(-1000, -1000);
			show();
	
	 		Scheduler.get().scheduleDeferred(this::center);
		}
	}
	
	public void setDefaultSize() {
		setSize("750px", "350px");
	}
	
	public void hideTetherBar() {
		hideTetherBar = true;
		if (initialized)
			tetherBar.removeFromParent();
	}
	
	public void showTetherBar() {
		hideTetherBar = false;
		if (initialized)
			borderLayoutContainer.setNorthWidget(tetherBar, new BorderLayoutData(TETHER_BAR_SIZE));
	}
	
	private void initializeNorthAndSouthWidgets() {
		if (!initialized) {
			if (!hideTetherBar)
				borderLayoutContainer.setNorthWidget(tetherBar, new BorderLayoutData(TETHER_BAR_SIZE));
			borderLayoutContainer.setSouthWidget(prepareToolBar(), new BorderLayoutData(61));
			initialized = true;
		}
	}
	
	private void handleEntityTypeInstantiation(InstantiationData instantiationData, EntityType<?> entityType) {
		handleEntityTypeInstantiation(getSessionForTransactionAndCMD().create(entityType), instantiationData, true);
	}
	
	private void handleEntityTypeInstantiation(GenericEntity instantiationEntity, InstantiationData instantiationData, boolean createNestedTr) {
		futureResults.put(futureList.get(futureList.size() - 1), instantiationEntity);
		
		ModelPath modelPath = new ModelPath();
		if (instantiationData.getPathElement() == null)
			modelPath.add(new RootPathElement(instantiationEntity.entityType(), instantiationEntity));
		else {
			ModelPathElement modelPathElement = instantiationData.getPathElement().copy();
			if (!instantiationData.isHandlingAdd()) {
				modelPathElement.setValue(instantiationEntity);
				modelPathElement.setType(instantiationEntity.entityType());
			}
			
			modelPath.add(modelPathElement);
			if (modelPathElement.getType().isCollection()) {
				PropertyPathElement propertyPathElement = (PropertyPathElement) modelPathElement;
				if (((CollectionType) modelPathElement.getType()).getCollectionKind().equals(CollectionKind.list)) {
					ListItemPathElement listItemPathElement = new ListItemPathElement(propertyPathElement.getEntity(),
							propertyPathElement.getProperty(), ((List<?>) modelPathElement.getValue()).size(), instantiationEntity.entityType(),
							instantiationEntity);
					modelPath.add(listItemPathElement);
				} else if (((CollectionType) modelPathElement.getType()).getCollectionKind().equals(CollectionKind.set)) {
					SetItemPathElement setItemPathElement = new SetItemPathElement(propertyPathElement.getEntity(), propertyPathElement.getProperty(),
							instantiationEntity.entityType(), instantiationEntity);
					modelPath.add(setItemPathElement);
				} //TODO: handle map?
			}
		}
		
		prepareForModelPath(modelPath, instantiationData.isFreeInstantiation(), instantiationData.isFreeInstantiation(), true, null,
				instantiationData.getNamePrefix(), instantiationData.isHandlingAdd(), createNestedTr);
	}
	
	@Override
	public void onWorkWithEntity(ModelPath modelPath, ModelPathElement selectedModelPathElement, String useCase, boolean forcePreferredUseCase,
			boolean readOnly) {
		showForModelPathElement(modelPath, false, null);
	}
	
	@Override
	public boolean isWorkWithAvailable(ModelPath modelPath) {
		return true;
	}
	
	@Override
	public void onEntityInstantiated(InstantiationData instantiationData) {
		showForInstantiation(instantiationData, new Future<GenericEntity>());
		undoOnCancel = true;
		//onEntityInstantiated(instantiationData.getPathElement(), instantiationData.isFreeInstantiation(), instantiationData.getNamePrefix());
	}
	
	@Override
	public void onEntityInstantiated(ModelPathElement pathElement, boolean showGima, boolean isFreeInstantiation, String namePrefix) {
		onEntityInstantiated(pathElement, isFreeInstantiation, namePrefix); //TODO: shouldn't we call showForInstantiation here?
	}

	private void onEntityInstantiated(ModelPathElement pathElement, boolean isFreeInstantiation, String namePrefix) {
		ModelPath currentModelPath = modelPathList.get(modelPathList.size() - 1);
		ModelPath modelPath = currentModelPath.copy();
		modelPath.add(pathElement);
		prepareForModelPath(modelPath, isFreeInstantiation, false, false, null, namePrefix, false, true);
		undoOnCancel = true;
	}
	
	@Override
	public void onEntityUninstantiated(ModelPathElement pathElement) {
		//NOP
	}
	
	@Override
	public void disableComponents() {
		closable = false;
		toolBar.setEnabled(false);
	}
	
	@Override
	public void enableComponents() {
		closable = true;
		toolBar.setEnabled(true);
	}
	
	@Override
	public void show() {
		super.show();
		isShown = true;
	}
	
	@Override
	public void setPosition(int left, int top) {
		if (top == -1) {
			logger.debug("Top was -1. Setting it to 0.");
			top = 0;
		}
		
		super.setPosition(left, top);
	}
	
	public PersistenceGmSession getSessionForTransactionAndCMD() {
		return gmSession;
	}
	
	public String getUseCase() {
		return useCase;
	}
	
	public ToolBar getToolBar() {
		return toolBar;
	}
	
	public void updateHeight() {
		//int toolbarHeight = 102 + getHeader().getOffsetHeight() + this.tetherBar.getOffsetHeight();		
		int toolbarHeight = getHeader().getOffsetHeight() + 35;
		if (borderLayoutContainer.getNorthWidget() != null)  
			toolbarHeight = toolbarHeight + borderLayoutContainer.getNorthWidget().getOffsetHeight();
		if (borderLayoutContainer.getSouthWidget() != null)  
			toolbarHeight = toolbarHeight + borderLayoutContainer.getSouthWidget().getOffsetHeight();
		
		int maxheight = Document.get().getClientHeight();

		if (getWidget() instanceof BorderLayoutContainer && ((BorderLayoutContainer) getWidget()).getWidget() instanceof PropertyPanel) {
			int documentHeight = ((PropertyPanel) ((BorderLayoutContainer) getWidget()).getWidget()).getGridContentHeight();
			int computedHeight;
			if (documentHeight == 0)
				computedHeight = 700;
			else {
				computedHeight = Math.max(documentHeight + toolbarHeight, 300);
				computedHeight = Math.min(maxheight, computedHeight);
			}
			setHeight(computedHeight);
			return;
		}

		int currentHeight = getOffsetHeight();
		int computedHeight = Math.min(Document.get().getClientHeight(), currentHeight);
		if (computedHeight != currentHeight)
			setHeight(computedHeight);
	}
	
	
	private void prepareForModelPath(ModelPath modelPath, boolean isFreeInstantiation, boolean checkPropertyPanelProperties, boolean showDialog,
			Supplier<? extends GmEntityView> propertyPanelProvider) {
		prepareForModelPath(modelPath, isFreeInstantiation, checkPropertyPanelProperties, showDialog, propertyPanelProvider, null, false, true);
	}
	
	private void prepareTypeSelectionView(InstantiationData instantiationData) {
		initializeNorthAndSouthWidgets();

 		GIMATypeSelectionView view = typeSelectionViewSupplier.get();
 		view.configureGmSession(getSessionForTransactionAndCMD());
		prepareToolBar(view);
		EntityType<?> entityType = (EntityType<?>) instantiationData.getType();
		GmType gmType = getSessionForTransactionAndCMD().getModelAccessory().getOracle().findGmType(entityType.getTypeSignature());
		ObjectAssignmentConfig config = new ObjectAssignmentConfig(gmType, null);
		view.apply(config) //
				.andThen(result -> {
					if (result == null) {
						handleFuture(true);
						return;
					}

	 				if (result.getType() instanceof GmEntityType) {
						EntityType<?> resultEntityType = GMF.getTypeReflection().getEntityType(result.getType().getTypeSignature());
						handleEntityTypeInstantiation(instantiationData, resultEntityType);
					} else if (result.getAction() instanceof InstantiationAction)
						instantiationActionHandler.handleInstantiationAction((InstantiationAction) result.getAction(), GIMADialog.this);
				}).onError(e -> {
					ErrorDialog.show(LocalizedText.INSTANCE.errorInstantiatingEntity(), e);
					e.printStackTrace();
				});

		String type = GMEMetadataUtil.getEntityNameMDOrShortName(entityType, getSessionForTransactionAndCMD().getModelAccessory().getMetaData(),
				useCase);

		TetherBarElement element = new TetherBarElement(null,
				instantiationData.getInstantiationActions() != null ? LocalizedText.INSTANCE.chooseAction() : LocalizedText.INSTANCE.chooseType(type),
				"", view);
		tetherBar.addTetherBarElement(element);

 		tetherBar.setSelectedThetherBarElement(element);

 		if (!isShown) {
			//RVE - set position out of the screen, because when resize it, than can flicker, later is centered on screen
			setPosition(-1000, -1000);
			show();

 			Scheduler.get().scheduleDeferred(this::center);
		}
	}

 	protected void prepareToolBar(GIMAView gimaView) {
 		if (lastViewConfiguredToolBar != null) {
			List<ButtonConfiguration> additionalButtons = lastViewConfiguredToolBar.getAdditionalButtons();
			if (additionalButtons != null)
				additionalButtons.forEach(buttonConfig -> toolBar.remove(buttonConfig.getButton()));
			if (lastViewConfiguredToolBar.getMainButton() != null)
				toolBar.remove(lastViewConfiguredToolBar.getMainButton());

 			cancelIndex = 1;
		}

 		lastViewConfiguredToolBar = gimaView;
 		currentGimaView = gimaView;

 		int index = cancelIndex;
		List<ButtonConfiguration> additionalButtons = gimaView.getAdditionalButtons();
		if (additionalButtons != null) {
			for (ButtonConfiguration buttonConfig : additionalButtons) {
				buttonConfig.getButton().addStyleName("gmeGima" + buttonConfig.getButton().getText().replaceAll("\\s","") + "Button");
				
				if (buttonConfig.isAfterCancel())
					toolBar.insert(buttonConfig.getButton(), index + CANCEL_BUTTONS_COUNT);
				else
					toolBar.insert(buttonConfig.getButton(), index);				
				index++;
				cancelIndex++;
			}
		}

		TextButton mainButton = gimaView.getMainButton();
 		if (mainButton == null) {
			applyButton.setText(LocalizedText.INSTANCE.apply());
			applyButton.setToolTip(LocalizedText.INSTANCE.applyDescription());
			applyAllButton.setText(LocalizedText.INSTANCE.apply());
			applyAllButton.setToolTip(LocalizedText.INSTANCE.applyAllDescription());
			Scheduler.get().scheduleDeferred(toolBar::forceLayout);
			return;
		}

 		Action action = new Action() {
			@Override
			public void perform(TriggerInfo triggerInfo) {
				mainButton.fireEvent(new SelectEvent());
			}
		};

 		mainButton.addDisableHandler(event -> action.setEnabled(mainButton.isEnabled()));
		mainButton.addEnableHandler(event -> action.setEnabled(mainButton.isEnabled()));

 		boolean multiElements = isMultiElements() && isCurrentViewApplyAllHandler();
		applyAllButton.setVisible(multiElements);
		applyButton.setVisible(!multiElements);
		cancelAllButton.setVisible(multiElements);
		cancelButton.setVisible(!multiElements);

		Scheduler.get().scheduleDeferred(() -> prepareApplyButtonLink(gimaView, action));
	}

 	private void prepareApplyButtonLink(GIMAView gimaView, Action action) {
 		if (mainButtonActionAdapter != null) {
			try {
				mainButtonActionAdapter.disposeBean();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
 		
 		TextButton theApplyButton = isMultiElements() && isCurrentViewApplyAllHandler() ? applyAllButton : applyButton;
		mainButtonActionAdapter = ButtonActionAdapter.linkActionToButton(action, theApplyButton, true);
		
		updateMainButton(gimaView);
	}
 	
 	public void updateMainButton(GIMAView gimaView) {
 		TextButton theApplyButton = isMultiElements() && isCurrentViewApplyAllHandler() ? applyAllButton : applyButton;
		TextButton mainButton = gimaView.getMainButton();
		theApplyButton.setText(mainButton.getText());
		theApplyButton.setMinWidth(mainButton.getMinWidth());
		if (mainButton.getToolTip() != null)
			theApplyButton.setToolTip(mainButton.getToolTip().getToolTipConfig().getBody());
		else
			theApplyButton.removeToolTip();
		theApplyButton.setEnabled(mainButton.isEnabled());
		theApplyButton.setIcon(mainButton.getIcon());
		
		Scheduler.get().scheduleDeferred(toolBar::forceLayout);
 	}
	
	private void prepareForModelPath(ModelPath modelPath, boolean isFreeInstantiation, boolean checkPropertyPanelProperties, boolean showDialog,
			Supplier<? extends GmEntityView> propertyPanelProvider, String namePrefix, boolean handlingAdd, boolean createNestedTr) {
		if (createNestedTr) {
			NestedTransaction nestedTransaction = getSessionForTransactionAndCMD().getTransaction().beginNestedTransaction();
			nestedTransactions.add(nestedTransaction);
			nestedTransaction.addTransactionFrameListener(getTransactionFrameListener());
		}
		
		modelPathList.add(modelPath);
		initializeNorthAndSouthWidgets();
		
		final TetherBarElement tetherBarElement;
		ModelPathElement last = modelPath.last();
		GenericModelType type = last.getType();
		
		Object value = last.getValue();
		ModelMdResolver modelMdResolver;
		if (value instanceof GenericEntity)
			modelMdResolver = getMetaData((GenericEntity) value);
		else
			modelMdResolver = getSessionForTransactionAndCMD().getModelAccessory().getMetaData();
		
		if (!type.isEntity()) {
			String name = getPropertyRelatedName(last);
			String description = ""; //TODO
			
			tetherBarElement = new TetherBarElement(modelPath, name, description, getMasterDetailConstellationProvider());
		} else {
			EntityType<?> entityType = (EntityType<?>) type;
			String name;
			String description = null;
			EntityReference reference = ((GenericEntity) value).reference();
			if (!(reference instanceof PreliminaryEntityReference) && last instanceof PropertyRelatedModelPathElement)
				name = getPropertyRelatedName(last);
			else {
				String displayInfo = GMEMetadataUtil.getEntityNameMDOrShortName(entityType, modelMdResolver, useCase);
				if (isFreeInstantiation)
					name = LocalizedText.INSTANCE.newType(displayInfo);
				else {
					String selectiveInformation = SelectiveInformationResolver.resolve(entityType, (GenericEntity) value, modelMdResolver, useCase/*, null*/);
					if (selectiveInformation != null && !selectiveInformation.trim().equals(""))
						name = selectiveInformation;
					else
						name = displayInfo;
					
					if (reference instanceof PreliminaryEntityReference)
						name += " *";
				}
			}
			
			description = GMEMetadataUtil.getEntityDescriptionMDOrShortName(entityType, modelMdResolver, useCase);
			if (name.equals(description))
				description = null;
			
			if (namePrefix != null)
				name = namePrefix + " " + name;
			
			tetherBarElement = new TetherBarElement(modelPath, name, description,
					getPropertyPanelProvider(propertyPanelProvider, isFreeInstantiation, handlingAdd));
		}
		
		tetherBar.insertTetherBarElement(tetherBar.getElementsSize(), tetherBarElement);
		tetherBar.setSelectedThetherBarElement(tetherBarElement);
		
		PropertyPanel propertyPanel = null;
		GmContentView contentViewIfProvided = tetherBarElement.getContentViewIfProvided();
		if (contentViewIfProvided instanceof PropertyPanel) 
			propertyPanel = (PropertyPanel) contentViewIfProvided;
		else if (contentViewIfProvided instanceof PropertyPanelGIMAView) 
			propertyPanel = ((PropertyPanelGIMAView) contentViewIfProvided).getPropertyPanel();
		PropertyPanel finalPropertyPanel = propertyPanel;	
				
		if (propertyPanel != null && propertyPanel.isWaitingForEditors()) {
			propertyPanel.setUseDialogSettings(true);
			propertyPanel.addPropertyPanelListener(new PropertyPanelListener() {
				@Override
				public void onEditorsReady() {
					if (checkPropertyPanelProperties)
						handleEditorsReady(checkPropertyPanelProperties, finalPropertyPanel);
					
					PropertyPanelListener listener = this;
					boolean adjustPosition;
					if (showDialog && !isShown) {
						//RVE - set position out of the screen, because when resize it, than can flicker, later is centered on screen
						setPosition(-1000, -1000);
						show();
						adjustPosition = true;
					} else
						adjustPosition = false;
					
					Scheduler.get().scheduleDeferred(() -> {
						finalPropertyPanel.removePropertyPanelListener(listener);
						if (adjustPosition) {
							updateHeight();
							center();
						}

						focus();  //RVE neeed this to use on Close Dialog with ESC when open 2nd or later Tether item
												
						if (!isFreeInstantiation)
							finalPropertyPanel.focus();
					});
				}

				@Override
				public void onEditingDone(boolean canceled) {
					// NOPE					
				}
			});
			return;
		}
		
		handleEditorsReady(checkPropertyPanelProperties, propertyPanel);
		
		if (showDialog && !isShown) {
			//RVE - set position out of the screen, because when resize it, than can flicker, later is centered on screen
			setPosition(-1000, -1000);
			show();
			
			Scheduler.get().scheduleDeferred(() -> {
				updateHeight();
				center();
			});
		}
	}

	private String getPropertyRelatedName(ModelPathElement last) {
		PropertyRelatedModelPathElement propertyPathElement = (PropertyRelatedModelPathElement) last;
		GenericEntity entity = propertyPathElement.getEntity();
		String propertyName = propertyPathElement.getProperty().getName();
		
		String propertyRelatedName = GMEMetadataUtil.getPropertyDisplay(propertyName,
				getMetaData(entity).entity(propertyPathElement.getEntity()).property(propertyName).useCase(useCase));
		
		if (last instanceof ListItemPathElement)
			return propertyRelatedName + " #" + (((ListItemPathElement) last).getIndex() + 1);
		
		return propertyRelatedName;
	}
	
	private void handleEditorsReady(boolean checkPropertyPanelProperties, PropertyPanel propertyPanel) {
		if (propertyPanel == null)
			return;
		
		if (checkPropertyPanelProperties && !propertyPanel.hasEditableProperty()) {
			Scheduler.get().scheduleDeferred(() -> performApply(false));
			return;
		}

		propertyPanel.startEditing();
	}
	
	private void configureCurrentContentView(GmContentView currentContentView) {
		if (this.currentContentView == currentContentView)
			return;
		
		if (this.currentContentView != null) {
			if (this.currentContentView.getView() instanceof Widget)
				remove((Widget) this.currentContentView.getView());
			this.currentContentView.removeSelectionListener(getSelectionListener());
		}
		
		if (this.currentContentView instanceof PropertyPanelGIMAView)
			((PropertyPanelGIMAView) this.currentContentView).saveScrollState();
		
		this.currentContentView = currentContentView;
		currentContentView.addSelectionListener(getSelectionListener());
		if (currentContentView.getView() instanceof Widget)
			borderLayoutContainer.setCenterWidget((Widget) currentContentView.getView());
		forceLayout();
		
		if (!modelPathList.isEmpty()) {
			if (contentViewModelPathMap == null)
				contentViewModelPathMap = new HashMap<>();
			ModelPath currentModelPath = contentViewModelPathMap.get(currentContentView);
			ModelPath newModelPath = modelPathList.get(modelPathList.size() - 1);
			if (currentModelPath != newModelPath)
				currentContentView.setContent(newModelPath);
			contentViewModelPathMap.put(currentContentView, newModelPath);
		}
		
		if (!(currentContentView instanceof PropertyPanelGIMAView))
			currentContentView.select(0, false);
		
		if (currentContentView instanceof PropertyPanelGIMAView)
			Scheduler.get().scheduleDeferred(((PropertyPanelGIMAView) currentContentView)::restoreScrollState);
	}
	
	private GmSelectionListener getSelectionListener() {
		if (selectionListener != null)
			return selectionListener;
		
		selectionListener = gmSelectionSupport -> {
			if (gmSelectionSupport.getView() instanceof PropertyPanel) {
				ModelPath modelPath = gmSelectionSupport.getFirstSelectedItem();
				prepareForModelPath(modelPath, false, false, false, null);
			}// else {
				//TODO: Do we need to handle this case?
			//}
		};
		
		return selectionListener;
	}
	
	private void refreshCurrentView() { //TODO: is this needed?
		if (currentContentView instanceof PropertyPanel)
			currentContentView.setContent(modelPathList.get(modelPathList.size() - 1));
	}
	
	protected ToolBar prepareToolBar() {
		toolBar = new ToolBar();
		toolBar.setBorders(false);
		toolBar.addStyleName("gimaToolbar");
		toolBar.addStyleName("gmeToolbar");
		
		toolBar.add(new FillToolItem());
		toolBar.add(prepareCancelButton());
		toolBar.add(prepareCancelAllButton());
		toolBar.add(prepareApplyButton());
		toolBar.add(prepareApplyAllButton());
		
		cancelIndex = 1;
		
		return toolBar;
	}
	
	public void performGoBack(boolean rollback) {
		performGoBack(rollback, false);
	}

 	/**
	 * Returns false iff keepDialogOpen is false and there is no more tether elements available.
	 */
	private boolean performGoBack(boolean rollback, boolean keepDialogOpen) {
		boolean ignoreFuture = currentGimaView instanceof GIMAEntityFieldView || currentGimaView instanceof GIMAValidationLogView;
		if (currentGimaView instanceof PropertyPanelGIMAView || currentGimaView instanceof MasterDetailGIMAView) {
			NestedTransaction nestedTransaction = nestedTransactions.remove(nestedTransactions.size() - 1);
			nestedTransaction.removeTransactionFrameListener(getTransactionFrameListener());
			try {
				if (rollback)
					nestedTransaction.rollback();
				else 									
					nestedTransaction.commit();
			} catch (TransactionException e) {
				ErrorDialog.show(LocalizedText.INSTANCE.errorRollingEditionBack(), e);
				e.printStackTrace();
			}
			
			modelPathList.remove(modelPathList.size() - 1);
		}
		
		handleViewHandlingInstantiation(currentGimaView, rollback);
		
		GIMAView previousGIMAView = currentGimaView;
		tetherBar.removeTetherBarElements(Collections.singletonList(tetherBar.getSelectedElement()));
		
		boolean result = true;
		if (!tetherBar.selectLastTetherBarElement())
			result = keepDialogOpen;
		else {
			GmContentView view = tetherBar.getSelectedElement().getContentViewIfProvided();
			if (view instanceof GIMATypeSelectionView) {
				currentGimaView = (GIMATypeSelectionView) view;
				handleViewHandlingInstantiation(currentGimaView, rollback);
				return performGoBack(rollback, keepDialogOpen);
			}
			
			if (view instanceof GIMAView)
				prepareToolBar((GIMAView) view);
			if (!(previousGIMAView instanceof GIMASelectionContentView) || !((GIMASelectionContentView) previousGIMAView).isAddingToMapKey())
				handleViewHandlingInstantiation(currentGimaView, rollback);
		}
		
		if (rollback && undoOnCancel) {
			performUndo();
			undoOnCancel = false;
		}
		
		if (!ignoreFuture)
			handleFuture(rollback);
		
		return result;
	}
	
	private void handleViewHandlingInstantiation(GIMAView gimaView, boolean rollback) {
		if (!viewsHandlingInstantiation.remove(gimaView))
			return;
		
		NestedTransaction nestedTransaction = nestedTransactions.remove(nestedTransactions.size() - 1);
		nestedTransaction.removeTransactionFrameListener(getTransactionFrameListener());

		if (rollback)
			nestedTransaction.rollback();
		else
			nestedTransaction.commit();
	}
	
	protected TextButton prepareApplyAllButton() {
		SplitButton applyAllButton = new SplitButton(LocalizedText.INSTANCE.apply());
		applyAllButton.setToolTip(LocalizedText.INSTANCE.applyDescription());
		applyAllButton.setIconAlign(IconAlign.TOP);
		applyAllButton.setScale(ButtonScale.LARGE);
		applyAllButton.setIcon(ConstellationResources.INSTANCE.finish());
		applyAllButton.setEnabled(false);
		applyAllButton.addSelectHandler(event -> performApplyAll());
		applyAllButton.addStyleName(GIMA_MAIN_BUTTON);
		
		Menu menu = new Menu();
		MenuItem applyAllItem = new MenuItem(LocalizedText.INSTANCE.applyAll());
		applyAllItem.setToolTip(LocalizedText.INSTANCE.applyAllDescription());
		applyAllItem.setIcon(ConstellationResources.INSTANCE.finishSmall());
		applyAllItem.addSelectionHandler(event -> handleApplyAllItem());
		menu.add(applyAllItem);
		applyAllButton.setMenu(menu);
		this.applyAllButton = applyAllButton;

		return applyAllButton;
	}
	
	private void handleApplyAllItem() {
		isApplyingAll = true;
		if (currentGimaView.getMainButton() != null)
			currentGimaView.getMainButton().fireEvent(new SelectEvent());
	}
	
	private void performApplyAll() {
		if (currentGimaView != null && currentGimaView.getMainButton() != null)
			return;
		
		handleHideOrBack(true, false);
	}
	
	public void handleHideOrBack(boolean commitTransaction, boolean undo) {
		if (isApplyingAll) {
			isApplyingAll = false;
			handleApplyAll();
			return;
		}

 		if (!isMultiElements()) {
			hide(undo);
 			return;
 		}
 		
 		if (commitTransaction) 		
 			nestedTransactions.get(nestedTransactions.size() - 1).commit();
 		performGoBack(undo);
 		undoOnCancel = false;
	}
	
	protected TextButton prepareApplyButton() {
		applyButton = new TextButton(LocalizedText.INSTANCE.apply());
		applyButton.setToolTip(LocalizedText.INSTANCE.applyDescription());
		applyButton.setIconAlign(IconAlign.TOP);
		applyButton.setScale(ButtonScale.LARGE);
		applyButton.setIcon(ConstellationResources.INSTANCE.finish());
		applyButton.setEnabled(true);
		applyButton.addSelectHandler(event -> performApply(true));
		applyButton.addStyleName(GIMA_MAIN_BUTTON);

		return applyButton;
	}
	
	public void performApply(boolean checkMainButton) {
		if (currentGimaView instanceof PropertyPanelGIMAView)
			((PropertyPanelGIMAView) currentGimaView).getPropertyPanel().completeEditing();
		
		//Using scheduler here to ensure that the value is always set when clicking the button after an edition
		Scheduler.get().scheduleDeferred(() -> {
			if (checkMainButton && currentGimaView != null && currentGimaView.getMainButton() != null)
				return;
			
			if (isApplyingAll) {
				isApplyingAll = false;
				handleApplyAll();
				return;
			}
			
			Future<Boolean> future = performValidation(ValidationKind.fail);						
			future.andThen(result -> {
				if (result) {
					if (!isMultiElements()) 
						hide(false);			
					else
						performGoBack(false);
				}
			}).onError(e -> {
				ErrorDialog.show(LocalizedText.INSTANCE.validationError(), e);
				e.printStackTrace();
			});			
		});
	}
	
	public Future<Boolean> performValidation(ValidationKind validationKind) {
		Future<Boolean> future = new Future<>();
		if (ignoreValidation || !(currentGimaView instanceof PropertyPanelGIMAView)) {
			future.onSuccess(true);
			return future;
		}
			
		
		GenericEntity entity = ((PropertyPanelGIMAView) currentGimaView).getPropertyPanel().getParentEntity();
		if (entity == null) {
			future.onSuccess(true);
			return future;
		}
		
		validateEntity(false, entity).onError(e -> {
			GlobalState.showError("Error while validating manipulations.", e);
			e.printStackTrace();
			future.onSuccess(false);
		}).andThen(result -> {
			if (result.isEmpty())
				future.onSuccess(true);
			else {						
				handleValidationPropertyFailure(result, validationKind, (PropertyPanelGIMAView) currentGimaView);
				future.onSuccess(false);
			}
		});
		
		return future;
	}

	protected TextButton prepareCancelAllButton() {
		SplitButton cancelAllButton = new SplitButton(LocalizedText.INSTANCE.cancel());
		cancelAllButton.setToolTip(LocalizedText.INSTANCE.cancelGimaDescription());
		cancelAllButton.setIconAlign(IconAlign.TOP);
		cancelAllButton.setScale(ButtonScale.LARGE);
		cancelAllButton.setIcon(ConstellationResources.INSTANCE.cancel());
		cancelAllButton.addSelectHandler(event -> performCancelAll());
		cancelAllButton.addStyleName(GIMA_CANCEL_BUTTON);
		
		Menu menu = new Menu();
		MenuItem cancellAllItem = new MenuItem(LocalizedText.INSTANCE.cancelAll());
		cancellAllItem.setToolTip(LocalizedText.INSTANCE.cancelAllGimaDescription());
		cancellAllItem.setIcon(ConstellationResources.INSTANCE.cancelSmall());
		cancellAllItem.addSelectionHandler(event -> handleCancelAll());
		menu.add(cancellAllItem);
		cancelAllButton.setMenu(menu);
		this.cancelAllButton = cancelAllButton;

		return cancelAllButton;
	}
	
	private void handleCancelAll() {
		isCancelingAll = true;
		performCancelAll();
	}
	
	private void performCancelAll() {
		currentGimaView.handleCancel();
		
		if (isCancelingAll) {
			while (isMultiElements())
				performGoBack(true);
			
			hide(true);
			return;
		}
		
		if (currentGimaView instanceof GIMATypeSelectionView && tetherBar.getElementsSize() > 1) {
			handleViewHandlingInstantiation(currentGimaView, true);
			
			tetherBar.removeTetherBarElements(Collections.singletonList(tetherBar.getSelectedElement()));
			
			if (tetherBar.selectLastTetherBarElement()) {
				GmContentView view = tetherBar.getSelectedElement().getContentViewIfProvided();
				if (view instanceof GIMAView)
					prepareToolBar((GIMAView) view);
				handleViewHandlingInstantiation(currentGimaView, true);
			}
			
			if (undoOnCancel) {
				performUndo();
				undoOnCancel = false;
			}
		} else {
			if (isMultiElements())
				performGoBack(true);
			else
				hide(true);
		}
	}
	
	private void handleApplyAll() {
		applyAllWasUsed = true;
		while (isMultiElements())
			performGoBack(false);
		
		if (isShown)
			hide(false);
		applyAllWasUsed = false;
	}
	
	public boolean isApplyingAll() {
		return applyAllWasUsed;
	}
	
	protected TextButton prepareCancelButton() {
		cancelButton = new TextButton(LocalizedText.INSTANCE.cancel());
		cancelButton.setToolTip(LocalizedText.INSTANCE.cancelGimaDescription());
		cancelButton.setIconAlign(IconAlign.TOP);
		cancelButton.setScale(ButtonScale.LARGE);
		cancelButton.setIcon(ConstellationResources.INSTANCE.cancel());
		cancelButton.addSelectHandler(event -> performCancelAll());
		cancelButton.addStyleName(GIMA_CANCEL_BUTTON);

		return cancelButton;
	}
	
	public void performUndo() {
		undoManipulation();
		refreshCurrentView();
	}
	
	public void performRedo() {
		try {
			nestedTransactions.get(nestedTransactions.size() - 1).redo(1);
		} catch (TransactionException e) {
			ErrorDialog.show(LocalizedText.INSTANCE.errorRedoing(), e);
			e.printStackTrace();
		}
		
		refreshCurrentView();
	}
	
	public void hide(boolean undoManipulations) {
		if (hidden)
			return;
		
		for (int i = nestedTransactions.size() - 1; i >= 0; i--) {
			NestedTransaction nestedTransaction = nestedTransactions.get(i);
			nestedTransaction.removeTransactionFrameListener(getTransactionFrameListener());
			if (!undoManipulations)
				nestedTransaction.commit();
			else {
				try {
					nestedTransaction.rollback();
				} catch (TransactionException e) {
					ErrorDialog.show(LocalizedText.INSTANCE.errorRollingEditionBack(), e);
					e.printStackTrace();
				}
			}
		}
		
		nestedTransactions.clear();
		hide();
		
		handleFuture(undoManipulations);
		
		disposeBean();
		
		undoOnCancel = false;
	}
	
	protected void handleFuture(boolean undoManipulations) {
		if (futureList.isEmpty())
			return;
		
		Future<Object> future = (Future<Object>) futureList.remove(futureList.size() - 1);
		Object result = futureResults.remove(future);
		if (result instanceof Boolean)
			result = !undoManipulations;
		else if (undoManipulations)
			result = null;
		future.onSuccess(result);
	}
	
	@Override
	public void hide() {
		if (!closable)
			return;
		
		super.hide();
		isShown = false;
	}
	
	private void undoManipulation() {
		try {
			nestedTransactions.get(nestedTransactions.size() - 1).undo(1);
		} catch (TransactionException e) {
			ErrorDialog.show(LocalizedText.INSTANCE.errorUndoing(), e);
			e.printStackTrace();
		}
	}
	
	private Timer getManipulationListenerTimer() {
		if (manipulationListenerTimer != null)
			return manipulationListenerTimer;
		
		manipulationListenerTimer = new Timer() {
			@Override
			public void run() {
				if (!nestedTransactions.isEmpty())
					updateApplyAllButton();
			}
		};
		
		return manipulationListenerTimer;
	}
	
	protected TransactionFrameListener getTransactionFrameListener() {
		if (transactionFrameListener == null)
			transactionFrameListener = transactionFrame -> updateApplyAllButton();
		
		return transactionFrameListener;
	}
	
	/**
	 * Adds a {@link GIMAEntityFieldView}
	 */
	public void addEntityFieldView(String title, GIMAView gimaView) {
		prepareToolBar(gimaView);
		
		TetherBarElement element = new TetherBarElement(null, title, null, gimaView);
		tetherBar.insertTetherBarElement(tetherBar.getElementsSize(), element);
		tetherBar.setSelectedThetherBarElement(element);
	}
	
	public Future<Void> addSelectionTether(String title, GIMAView gimaView) {
		prepareToolBar(gimaView);
		TetherBarElement element = new TetherBarElement(null, title, "", gimaView);
		
		tetherBar.insertTetherBarElement(tetherBar.getElementsSize(), element);
		tetherBar.setSelectedThetherBarElement(element);
		
		Future<Void> future = new Future<>();
		futureList.add(future);
		futureResults.put(future, null);
		
		return future;
	}
	
	private void updateApplyAllButton() {
		if (applyAllButton != null) {
			applyAllButton.setEnabled(
					isMultiElements() && nestedTransactions.stream().anyMatch(f -> hasDoneManipulations(f)) && isCurrentViewApplyAllHandler());
		}
	}
	
	private boolean hasDoneManipulations(NestedTransaction transaction) {
		if (transaction.canUndo())
			return true;
		
		if (transaction instanceof BasicNestedTransaction) {
			NestedTransaction childFrame = ((BasicNestedTransaction) transaction).getChildFrame();
			if (childFrame != null)
				return hasDoneManipulations(childFrame);
		}
		
		return false;
	}
	
	public ManipulationListener getManipulationListener() {
		if (manipulationListener == null)
			manipulationListener = manipulation -> getManipulationListenerTimer().schedule(400);
		
		return manipulationListener;
	}
	
	public List<NestedTransaction> getNestedTransactions() {
		return nestedTransactions;
	}
	
	protected Supplier<? extends GmEntityView> getPropertyPanelProvider(Supplier<? extends GmEntityView> provider, boolean isInstantiation,
			boolean handlingAdd) {
		return () -> {
			GmEntityView view = provider == null ? propertyPanelProvider.get() : provider.get();
			view.configureUseCase(useCase);
			view.configureGmSession(getSessionForTransactionAndCMD());
			
			PropertyPanelGIMAView finalView = new PropertyPanelGIMAView(view, this, isInstantiation, handlingAdd);
			
			prepareToolBar(finalView);
			return finalView;
		};
	}
	
	private Supplier<? extends GmContentView> getMasterDetailConstellationProvider() {
		if (adaptedMasterDetailConstellationProvider == null) {
			adaptedMasterDetailConstellationProvider = () -> {
				MasterDetailConstellation masterDetailConstellation = masterDetailConstellationProvider.get();
				masterDetailConstellation.configureGmSession(getSessionForTransactionAndCMD());
				masterDetailConstellation.configureUseCase(useCase);
				
				MasterDetailGIMAView finalView = new MasterDetailGIMAView(masterDetailConstellation, this);
				prepareToolBar(finalView);
				return finalView;
			};
		}
		
		return adaptedMasterDetailConstellationProvider;
	}
	
	protected boolean isMultiElements() {
		if (tetherBar.getElementsSize() == 1)
			return false;

 		int gimaElements = 0;
		for (int i = 0; i < tetherBar.getElementsSize(); i++) {
			TetherBarElement element = tetherBar.getElementAt(i);
			if (!(element.getContentViewIfProvided() instanceof GIMATypeSelectionView))
				gimaElements++;

 			if (gimaElements > 1)
				return true;
		}

 		return false;
	}
	
	private boolean isCurrentViewApplyAllHandler() {
		return currentGimaView.isApplyAllHandler();
	}
	
	@Override
	protected void onKeyPress(Event event) {
		if (isOnEsc() && event.getKeyCode() == KeyCodes.KEY_ESCAPE) {
			if (!wasEditionFinishedByEsc())
				handleCancelAll();
			return;
		}
		
		super.onKeyPress(event);
	}
	
	private boolean wasEditionFinishedByEsc() {
		if (currentContentView instanceof PropertyPanelGIMAView)
			return ((PropertyPanelGIMAView) currentContentView).getPropertyPanel().wasEditionFinishedByEsc();
		
		return false;
	}
	
	@Override
	public void disposeBean() {
		try {
			tetherBar.disposeBean();
			
			if (mainButtonActionAdapter != null)
				mainButtonActionAdapter.disposeBean();
		} catch (Exception e) {
			//Nothing to do
		}
		setGmSession(null);
		
		modelPathList.clear();
		modelPathList = null;
		
		nestedTransactions.clear();
		nestedTransactions = null;
		
		viewsHandlingInstantiation = null;
	}

	private void handleValidationPropertyFailure(ValidationLog validationLog, ValidationKind validationKind, PropertyPanelGIMAView view) {		
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
		if (view != null) {
			PropertyPanel propertyPanel = view.getPropertyPanel();
			propertyPanel.handleValidationLog(validationLog, validationKind);				
		}
	}
	
	private Future<ValidationLog> validateEntity(boolean allConfigured, GenericEntity entity) {			
		Validation validation = getValidation(entity);
		ValidationContext validationContext = new ValidationContext();
		validationContext.setGmSession((PersistenceGmSession) entity.session());
		validationContext.setGroupValidations(true);
		validationContext.setShortMessageStyle(true);			
		if (allConfigured) {
			validationContext.setCompleteConfiguredValidation(true);
			return validation.validateEntityWithContext(entity, validationContext);
		} else {	
			GlobalState.showSuccess(LocalizedText.INSTANCE.validating());
			return validation.validateEntityWithContext(entity, validationContext);
		}
	}
	
	@Override
	public Validation getValidation(GenericEntity entity) {
		Validation validation = validationSupplier.get();
		
		PersistenceGmSession theSession = gmSession;
		if (entity!= null && entity.session() instanceof PersistenceGmSession)
			theSession = (PersistenceGmSession) entity.session();

		validation.getPredator().setGmSession(theSession);		
		return validation;
	}
	
}
