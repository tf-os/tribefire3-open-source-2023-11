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
package com.braintribe.gwt.modeller.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.browserfeatures.client.UrlParameters;
import com.braintribe.gwt.gmview.action.client.ActionGroup;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.gmview.actionbar.client.ActionProviderConfiguration;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionProvider;
import com.braintribe.gwt.gmview.client.GmActionSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewActionManager;
import com.braintribe.gwt.gmview.client.GmDetailViewListener;
import com.braintribe.gwt.gmview.client.GmDetailViewSupport;
import com.braintribe.gwt.gmview.client.GmDetailViewSupportContext;
import com.braintribe.gwt.gmview.client.GmEntityView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.HasAddtionalWidgets;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.TabbedWidgetContext;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.modeller.client.action.GmModellerActions;
import com.braintribe.gwt.modeller.client.animation.StandardModelGraphStateAnimator;
import com.braintribe.gwt.modeller.client.filter.DefaultFiltering;
import com.braintribe.gwt.modeller.client.filter.GmModellerFilterPanel;
import com.braintribe.gwt.modeller.client.history.GmModellerHistory;
import com.braintribe.gwt.modeller.client.manipulation.GmModellerManipulations;
import com.braintribe.gwt.modeller.client.typesoverview.GmModellerTypesOverviewPanel;
import com.braintribe.gwt.modeller.client.view.GmModellerViewPanel;
import com.braintribe.gwt.smartmapper.client.SmartMapper;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.modellerfilter.meta.DefaultModellerView;
import com.braintribe.model.modellerfilter.view.ModellerView;
import com.braintribe.model.modellergraph.condensed.CondensedModel;
import com.braintribe.model.modellergraph.graphics.Edge;
import com.braintribe.model.modellergraph.graphics.ModelGraphState;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.extended.ModelMdDescriptor;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.modellergraph.CondensedModelBuilder;
import com.braintribe.model.processing.modellergraph.GmModellerMode;
import com.braintribe.model.processing.modellergraph.ModelGraphAnimationContext;
import com.braintribe.model.processing.modellergraph.ModelGraphConfigurationsNew;
import com.braintribe.model.processing.modellergraph.ModelGraphStateBuilderNew;
import com.braintribe.model.processing.modellergraph.animation.ModelGraphStateAnimationListener;
import com.braintribe.model.processing.modellergraph.common.BezierTools;
import com.braintribe.model.processing.modellergraph.common.Complex;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.managed.EntityQueryResultConvenience;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceManipulationListenerRegistry;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.processing.async.api.AsyncCallback;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;

public class GmModeller extends BorderLayoutContainer implements GmEntityView, ManipulationListener, InitializableBean, DisposableBean,
		HasAddtionalWidgets, ModelGraphStateAnimationListener, GmViewActionProvider, GmActionSupport, RequiresResize, GmDetailViewListener {

	private int size = 600;

	private PersistenceGmSession session;

	private ModelPath modelPath;
	private ModelPath selectedModelPath;
	private GmType selectedType;

	private boolean fireSelectionChange = false;
	private GmMetaModel model;
	private ModellerView view;

	private CondensedModel condensedModel;
	private ModelOracle modelOracle;
	private CmdResolver modelResolver;
	private ModelMetaDataEditor modelMetaDataEditor;

	private GmModellerPanel modellerPanel;
	private GmModellerTypesOverviewPanel typesOverviewPanel;
	private GmModellerFilterPanel filterPanel;
	private GmModellerViewPanel viewPanel;

	private SmartMapper smartMapper;

	private ModelGraphState currentModelGraphState;
	private DefaultFiltering defaultFiltering = new DefaultFiltering();
	private BezierTools bezierTools = new BezierTools();

	private ModelGraphConfigurationsNew configurations;
	private ModelGraphStateBuilderNew modelGraphStateBuilder;
	private GmModellerManipulations manipulations = new GmModellerManipulations();
	private GmModellerActions actions = new GmModellerActions();
	private GmModellerRenderer renderer;
	private StandardModelGraphStateAnimator animator;
	private GmModellerHistory history;
	private GmModellerTypeSource typeSource;

	private List<TabbedWidgetContext> tabbedWidgetContexts;
	private Supplier<SpotlightPanel> quickAccessPanelProvider;
	private List<GmSelectionListener> gmSelectionListeners;
	private GmContentViewActionManager actionManager;
	private List<Pair<ActionTypeAndName, ModelAction>> externalActions = new ArrayList<>();
	private ActionProviderConfiguration actionProviderConfiguration;
	protected List<Pair<String, TextButton>> buttonsList;

	private Timer renderRequest;

	private boolean readOnly = false;
	boolean offline = UrlParameters.getInstance().getParameter("offline") != null;

	public GmModeller() {
		// init();
		setCenterWidget(getModellerPanel());
	}

	public void setFilterPanel(GmModellerFilterPanel filterPanel) {
		this.filterPanel = filterPanel;
	}

	public void setViewPanel(GmModellerViewPanel viewPanel) {
		this.viewPanel = viewPanel;
		this.viewPanel.setModeller(this);
	}

	public void setTypesOverviewPanel(GmModellerTypesOverviewPanel typesOverviewPanel) {
		this.typesOverviewPanel = typesOverviewPanel;
		this.typesOverviewPanel.setModeller(this);
	}

	public void setQuickAccessPanelProvider(Supplier<SpotlightPanel> quickAccessPanelProvider) {
		this.quickAccessPanelProvider = quickAccessPanelProvider;
	}

	public void setSmartMapper(SmartMapper smartMapper) {
		this.smartMapper = smartMapper;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public void setOffline(boolean offline) {
		this.offline = offline;
	}

	public GmModellerPanel getModellerPanel() {
		if (modellerPanel == null) {
			modellerPanel = new GmModellerPanel();

			modellerPanel.addKeyDownHandler(new KeyDownHandler() {
				@Override
				public void onKeyDown(KeyDownEvent event) {
					if (event.isMetaKeyDown()) {
						// animator.pauseOrResume();
						actions.perform(event);
					}
				}
			});
			modellerPanel.addKeyUpHandler(new KeyUpHandler() {

				@Override
				public void onKeyUp(KeyUpEvent event) {
					// animator.pauseOrResume();
				}
			});
		}
		return modellerPanel;
	}

	private void init() {
		configurations = new ModelGraphConfigurationsNew();
		configurations.setViewPortDimension(new Complex(size, size));

		typesOverviewPanel.setQuickAccessPanelProvider(quickAccessPanelProvider);

		filterPanel.setTypesOverviewPanel(typesOverviewPanel);
		filterPanel.setModelGraphConfigurations(configurations);
		viewPanel.setFilterPanel(filterPanel);

		manipulations.setModeller(this);
		manipulations.setConfigurations(configurations);

		history = new GmModellerHistory();
		history.setModeller(this);
		history.addListener(actions);

		actions.setConfigurations(configurations);
		actions.setHistory(history);
		actions.setModeller(this);
		actions.setSmartMapper(smartMapper);
		actions.setReadOnly(readOnly);

		typeSource = new GmModellerTypeSource();
		typeSource.setModeller(this);
		typeSource.setConfiguration(configurations);
		typeSource.setQuickAccessPanelProvider(quickAccessPanelProvider);

		modelGraphStateBuilder = new ModelGraphStateBuilderNew();
		modelGraphStateBuilder.setModelGraphConfigurations(configurations);
		modelGraphStateBuilder.setBezierTools(bezierTools);

		renderer = new GmModellerRenderer();
		renderer.setConfig(configurations);
		renderer.setModeller(this);
		renderer.setModellerPanel(getModellerPanel());
		renderer.setModelGraphConfigurations(configurations);
		renderer.setTypeSource(typeSource);
		renderer.setQuickAccessPanelProvider(quickAccessPanelProvider);
		renderer.ensureView();

		bezierTools.setAtmoshperesRadii(configurations.atmoshperesRadii);

		animator = new StandardModelGraphStateAnimator();
		animator.setDurationInMillies(500);
		animator.setIntendedAnimationRate(250);
		animator.addListener(this);
		animator.addListener(renderer);

		modellerPanel.setActions(actions);
		modellerPanel.setConfigurations(configurations);
		modellerPanel.setModeller(this);
		modellerPanel.adaptSize(size);

		if (actionManager != null)
			actionManager.connect(this);
	}

	@Override
	protected void onAttach() {
		super.onAttach();

	}

	private void tryMask() {
		XElement.as(RootPanel.getBodyElement()).mask("Loading...");
		// if(isRendered()) {
		// getElement().mask("Loading...");
		// }
	}

	private void tryUnMask() {
		XElement.as(RootPanel.getBodyElement()).unmask();
		// if(isRendered()) {
		// getElement().unmask();
		// }
	}

	@Override
	public void intializeBean() throws Exception {
		init();
	}

	@Override
	public void disposeBean() throws Exception {
		if (view != null)
			unregisterManipulationListeners(view);
		else if (model != null)
			unregisterManipulationListeners(model);
	}

	@Override
	public ModelPath getContentPath() {
		return modelPath;
	}

	protected void initializeActions() {
		this.externalActions.addAll(actions.getActions());
		if (actionManager != null) {
			actionManager.addExternalActions(this, this.externalActions);
		}

		/* Map<String, Boolean> actionsAvailability = null; if (buttonsList == null) { buttonsList = new ArrayList<>();
		 * 
		 * if (externalActions != null) { actionsAvailability = new FastMap<>(); List<Pair<String, ModelAction>> externalActionsChecked = new
		 * ArrayList<>(externalActions); for (Pair<String, ModelAction> pair : externalActions) { String actionName = pair.getFirst(); boolean
		 * actionAvailable = actionManager.isActionAvailable(actionName); actionsAvailability.put(actionName, actionAvailable); if (!actionAvailable)
		 * externalActionsChecked.remove(pair);
		 * 
		 * }
		 * 
		 * List<Pair<String, TextButton>> externalButtons = GMEUtil.prepareExternalActionButtons(externalActionsChecked); if (externalButtons != null)
		 * buttonsList.addAll(externalButtons); }
		 * 
		 * buttonsList.addAll(GMEUtil.prepareExternalActionButtons(actions.getActions())); } */
	}

	@Override
	public void configureActionGroup(ActionGroup actionGroup) {
		initializeActions();
	}

	@Override
	public void configureExternalActions(List<Pair<ActionTypeAndName, ModelAction>> externalActions) {
		if (externalActions == null)
			this.externalActions.clear();
		else {
			this.externalActions.clear();
			this.externalActions.addAll(externalActions);
			for (Pair<ActionTypeAndName, ModelAction> pair : externalActions) {
				if (!actionManager.isActionAvailable(pair.getFirst()))
					this.externalActions.remove(pair);
			}
		}

		if (actionProviderConfiguration != null)
			actionProviderConfiguration.addExternalActions(this.externalActions);
		// if (actionsContextMenu != null) //Already initialized
		// actionManager.addExternalActions(this, this.externalActions);
	}

	@Override
	public List<Pair<ActionTypeAndName, ModelAction>> getExternalActions() {
		return this.externalActions;
	}

	@Override
	public void setActionManager(GmContentViewActionManager actionManager) {
		this.actionManager = actionManager;
	}

	@Override
	public GmContentViewActionManager getGmContentViewActionManager() {
		return this.actionManager;
	}

	@Override
	public ActionProviderConfiguration getActions() {
		if (actionProviderConfiguration == null) {
			initializeActions();

			actionProviderConfiguration = new ActionProviderConfiguration();
			actionProviderConfiguration.setGmContentView(this);

			List<Pair<ActionTypeAndName, ModelAction>> knownActions = null;
			if (actionManager != null)
				knownActions = actionManager.getKnownActionsList(this);
			if (knownActions != null || externalActions != null) {
				List<Pair<ActionTypeAndName, ModelAction>> allActions = new ArrayList<>();
				if (knownActions != null)
					allActions.addAll(knownActions);
				if (externalActions != null)
					allActions.addAll(externalActions);

				actionProviderConfiguration.addExternalActions(allActions);
			}

			if (buttonsList != null)
				actionProviderConfiguration.setExternalButtons(new ArrayList<>(buttonsList));
		}

		return actionProviderConfiguration;
	}

	@Override
	public boolean isFilterExternalActions() {
		return false;
	}

	@Override
	public void setContent(ModelPath modelPath) {
		// unregister manipulation listener

		if (this.modelPath != modelPath) {
			if (this.modelPath != null) {
				if (this.view != null) {
					unregisterManipulationListeners(view);
					view = null;
					model = null;
				} else {
					if (this.model != null) {
						unregisterManipulationListeners(model);
						model = null;
					}
				}
				this.modelPath = null;
			}

			if (modelPath != null) {
				tryMask();
				this.modelPath = modelPath;
				GenericEntity modelPathValue = modelPath.last().getValue();
				if (offline) {
					fetchView((GmMetaModel) modelPathValue);
				} else
					refresh(modelPathValue.entityType(), modelPathValue);
			} else {
				clearModeler();
			}
		}

	}

	private void clearModeler() {
		currentModelGraphState = null;
		selectedModelPath = null;
		fireGmSelectionListeners();
		filterPanel.setGmMetaModel(null);
		filterPanel.setModellerView(null);
		typesOverviewPanel.setGmMetaModel(null);
		renderer.clear();
	}

	private void refresh(EntityType<?> type, GenericEntity entity) {
		Object id = type.getIdProperty().get(entity);
		EntityQuery query = EntityQueryBuilder.from(type).where().property(type.getIdProperty().getName()).eq(id).tc().negation().joker().done();
		session.query().entities(query).result(Future.async(this::error, this::handleResult));
	}

	private void handleResult(EntityQueryResultConvenience conv) {
		GenericEntity entity = conv.first();
		if (ModellerView.T.isValueAssignable(entity)) {
			handleView((ModellerView) entity);
		} else if (GmMetaModel.T.isValueAssignable(entity)) {
			fetchView((GmMetaModel) entity);
		}
	}

	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		if (sl != null) {
			if (gmSelectionListeners == null) {
				gmSelectionListeners = new ArrayList<GmSelectionListener>();
			}
			gmSelectionListeners.add(sl);
			// if(smartMapper != null)
			// smartMapper.addSelectionListener(sl);
		}
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		if (gmSelectionListeners != null) {
			gmSelectionListeners.remove(sl);
			// if(smartMapper != null)
			// smartMapper.removeSelectionListener(sl);
			if (gmSelectionListeners.isEmpty())
				gmSelectionListeners = null;
		}
	}

	@Override
	public ModelPath getFirstSelectedItem() {
		return selectedModelPath;
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		List<ModelPath> modelPaths = null;
		if (selectedModelPath != null) {
			modelPaths = new ArrayList<ModelPath>();
			modelPaths.add(selectedModelPath);
		}
		return modelPaths;
	}

	@Override
	public boolean isSelected(Object element) {
		return false;
	}

	@Override
	public void select(int index, boolean keepExisting) {
		// NOP
	}

	public void resetSelection() {
		renderer.deselectNodes(null);
		selectedModelPath = new ModelPath();
		RootPathElement modelElement = new RootPathElement(GmMetaModel.T, model);
		selectedModelPath.add(modelElement);
		// selectedModelPath = null;
		fireGmSelectionListeners();
		// updateActions();
	}

	public void fireSelectionChanged(String typeSignature, boolean fireDeferred) {
		fireSelectionChange = fireDeferred;
		if (typeSignature != null && !typeSignature.equals("") && !typeSignature.equals("?")) {

			if (typeSignature.endsWith(configurations.doubleTypeSuffix))
				typeSignature = typeSignature.substring(0, typeSignature.indexOf(configurations.doubleTypeSuffix));

			selectedModelPath = new ModelPath();
			GmType gmType = getType(typeSignature);
			selectedType = gmType;

			RootPathElement modelElement = new RootPathElement(GmMetaModel.T, model);
			selectedModelPath.add(modelElement);

			RootPathElement typeElement = new RootPathElement(GmType.T, gmType);
			selectedModelPath.add(typeElement);

		} else {
			selectedType = null;
			selectedModelPath = new ModelPath();
			RootPathElement modelElement = new RootPathElement(GmMetaModel.T, model);
			selectedModelPath.add(modelElement);

			// selectedModelPath = null;
		}

		if (!fireDeferred) {
			fireGmSelectionListeners();
			// updateActions();
		}
		/* Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
		 * 
		 * @Override public boolean execute() { fireGmSelectionListeners(); updateActions(); return false; } }, 250); */
	}

	public void fireGmSelectionListeners() {
		if (gmSelectionListeners != null) {
			for (GmSelectionListener listener : gmSelectionListeners) {
				listener.onSelectionChanged(this);
			}
		}
	}

	@Override
	public GmContentView getView() {
		// return null;
		return this;
	}

	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.session = gmSession;
		filterPanel.setSession(this.session);
		viewPanel.setSession(this.session);
		typeSource.setSession(this.session);
		typesOverviewPanel.setSession(this.session);
		renderer.setSession(this.session);
		smartMapper.configureGmSession(this.session);
	}

	@Override
	public PersistenceGmSession getGmSession() {
		return session;
	}

	@Override
	public void configureUseCase(String useCase) {
		// NOP
	}

	@Override
	public String getUseCase() {
		return null;
	}

	@Override
	public void configureAdditionalWidgets(List<TabbedWidgetContext> additionalWidgets) {
		// NOP
	}

	@Override
	public List<TabbedWidgetContext> getTabbedWidgetContexts() {
		if (tabbedWidgetContexts == null) {
			tabbedWidgetContexts = new ArrayList<TabbedWidgetContext>();
			TabbedWidgetContext typesContext = new TabbedWidgetContext("Types", "Types", () -> typesOverviewPanel);
			tabbedWidgetContexts.add(typesContext);

			TabbedWidgetContext filterContext = new TabbedWidgetContext("Filter", "Filter", () -> filterPanel);
			tabbedWidgetContexts.add(filterContext);

			TabbedWidgetContext viewContext = new TabbedWidgetContext("Views", "Views", () -> viewPanel);
			tabbedWidgetContexts.add(viewContext);

		}
		return tabbedWidgetContexts;
	}

	private void registerManipulationListeners(ModellerView modellerView) {
		// System.err.println("register listeners for modellerView");
		PersistenceManipulationListenerRegistry reg = session.listeners();

		reg.entity(modellerView.getExcludesFilterContext()).add(this);
		reg.entity(modellerView.getIncludesFilterContext()).add(this);
		reg.entity(modellerView.getRelationshipKindFilterContext()).add(this);
		reg.entity(modellerView.getSettings()).add(this);
		reg.entity((modellerView)).add(typesOverviewPanel);

		// registerManipulationListeners(modellerView.getMetaModel());
	}

	private void unregisterManipulationListeners(ModellerView modellerView) {
		// System.err.println("unregister listeners for modellerView");
		PersistenceManipulationListenerRegistry reg = session.listeners();

		reg.entity(modellerView.getExcludesFilterContext()).remove(this);
		reg.entity(modellerView.getIncludesFilterContext()).remove(this);
		reg.entity(modellerView.getRelationshipKindFilterContext()).remove(this);
		reg.entity(modellerView.getSettings()).remove(this);
		reg.entity((modellerView)).remove(typesOverviewPanel);

		unregisterManipulationListeners(modellerView.getMetaModel());
	}

	private void registerManipulationListeners(GmMetaModel model) {
		// long time = System.currentTimeMillis();
		PersistenceManipulationListenerRegistry reg = session.listeners();

		reg.entity(model).add(this);
		// reg.entity(model).add(typeOverviewPanel);

		model.entityTypeSet().forEach(entityType -> {
			// System.err.println("register listeners for type " + entityType.getTypeSignature());
			reg.entity(entityType).add(this);
		});

		model.entityOverrideSet().forEach(override -> {
			reg.entity(override).add(this);
		});

		model.enumTypeSet().forEach(enumType -> {
			// System.err.println("register listeners for type " + enumType.getTypeSignature());
			reg.entity(enumType).add(this);
		});

		model.enumOverrideSet().forEach(override -> {
			reg.entity(override).add(this);
		});

		model.getDependencies().stream().forEach(dep -> {
			registerManipulationListeners(dep);
		});
		// System.err.println("register listeners for model " + model.getName() + " " + (System.currentTimeMillis() - time) + "ms");
	}

	private void unregisterManipulationListeners(GmMetaModel model) {
		if (model != null) {
			// System.err.println("unregister listeners for model " + model.getName());
			PersistenceManipulationListenerRegistry reg = session.listeners();

			reg.entity(model).remove(this);
			// reg.entity(model).remove(typeOverviewPanel);

			model.entityTypeSet().forEach(entityType -> {
				// System.err.println("unregister listeners for type " + entityType.getTypeSignature());
				reg.entity(entityType).remove(this);
			});

			model.entityOverrideSet().forEach(override -> {
				reg.entity(override).remove(this);
			});

			model.enumTypeSet().forEach(enumType -> {
				// System.err.println("unregister listeners for type " + enumType.getTypeSignature());
				reg.entity(enumType).remove(this);
			});

			model.enumOverrideSet().forEach(override -> {
				reg.entity(override).remove(this);
			});

			model.getDependencies().stream().forEach(dep -> {
				unregisterManipulationListeners(dep);
			});
		}
	}

	public void register(GenericEntity entity) {
		PersistenceManipulationListenerRegistry reg = session.listeners();
		reg.entity(entity).add(this);
	}

	public void unregister(GenericEntity entity) {
		PersistenceManipulationListenerRegistry reg = session.listeners();
		reg.entity(entity).remove(this);
	}

	@Override
	public void noticeManipulation(Manipulation manipulation) {
		manipulations.noticeManipulation(manipulation);
	}

	private void fetchView(GmMetaModel model) {
		this.model = model;
		initMetaModelTools();

		DefaultModellerView defaultModellerView = null; // modelResolver.getMetaData().meta(DefaultModellerView.T).exclusive();
		List<ModelMdDescriptor> metaDataList = modelResolver.getMetaData().meta(DefaultModellerView.T).listExtended();

		for (ModelMdDescriptor desc : metaDataList) {
			// if(!desc.getInherited()) {
			defaultModellerView = (DefaultModellerView) desc.getResolvedValue();
			if (defaultModellerView.getDefaultView().getMetaModel() == model)
				break;
			else
				defaultModellerView = null;
			// }
		}

		if (defaultModellerView == null) {
			defaultFiltering.prepareDefaultModellerView(session, model, AsyncCallback.of(future -> handleView(future.getDefaultView()), this::error),
					offline);
		} else
			handleView(defaultModellerView.getDefaultView());
	}

	public void handleView(ModellerView view) {
		this.view = view;
		registerManipulationListeners(view);
		filterPanel.setModellerView(view);
		typesOverviewPanel.setModellerView(view);

		String typeName = UrlParameters.getInstance().getParameter("typeName");
		if (typeName == null)
			typeName = view.getFocusedType() != null ? view.getFocusedType().getTypeSignature() : null;
		if (typeName != null)
			configurations.currentFocusedType = typeName;
		else
			configurations.currentFocusedType = "?";

		configurations.modellerView = view;
		handleModel(view.getMetaModel());
	}

	private void handleModel(GmMetaModel model) {
		this.model = model;
		initMetaModelTools();
		registerManipulationListeners(this.model);
		typeSource.setGmMetaModel(this.model);
		typesOverviewPanel.setGmMetaModel(this.model);
		filterPanel.setGmMetaModel(this.model);
		viewPanel.setGmMetaModel(this.model);
		smartMapper.setGmMetaModel(this.model);
		focus(configurations.currentFocusedType, true);
		tryUnMask();
	}

	public GmMetaModel getModel() {
		return model;
	}

	public void initMetaModelTools() {
		long time = System.currentTimeMillis();

		condensedModel = CondensedModelBuilder.build(model);
		modelGraphStateBuilder.setCondensedModel(condensedModel);

		modelOracle = new BasicModelOracle(model);
		modelResolver = new CmdResolverImpl(modelOracle);
		modelMetaDataEditor = BasicModelMetaDataEditor.create(model).withSession(session).done();

		if (smartMapper != null) {
			smartMapper.initMetaModelTools();
		}

		System.err.println("init meta model tools " + (System.currentTimeMillis() - time) + "ms");
	}

	public void select(String typeSig) {
		renderer.select(typeSig, true, true);
	}

	public void rerender() {
		if (configurations.modellerMode != GmModellerMode.mapping) {
			if (configurations.modellerMode == GmModellerMode.detailed) {
				detail(configurations.currentLeftDetailType, configurations.currentRightDetailType, true);
			} else {
				if (configurations.currentFocusedType != null)
					focus(configurations.currentFocusedType, false);
			}
		}
	}

	public void focus(String typeName, boolean addToHistory) {
		System.err.println("rendering request");
		if (renderRequest != null) {
			System.err.println("rendering cancelled");
			renderRequest.cancel();
		}
		renderRequest = new Timer() {
			@Override
			public void run() {
				System.err.println("rendering...");
				modelGraphStateBuilder.setRelationshipFilter(filterPanel.getFilter());
				ModelGraphAnimationContext mgac = modelGraphStateBuilder.focus(typeName, currentModelGraphState);
				currentModelGraphState = mgac.to;
				animator.animate(mgac.transitions);
			}
		};
		if (addToHistory)
			history.add(typeName);
		renderRequest.schedule(50);
	}

	public void detail(String fromTypeName, String toTypeName, boolean addToHistory) {
		System.err.println("rendering request");
		if (renderRequest != null) {
			System.err.println("rendering cancelled");
			renderRequest.cancel();
		}
		renderRequest = new Timer() {
			@Override
			public void run() {
				System.err.println("rendering...");
				modelGraphStateBuilder.setRelationshipFilter(filterPanel.getFilter());
				ModelGraphAnimationContext mgac = modelGraphStateBuilder.detail(fromTypeName, toTypeName, currentModelGraphState);
				if (mgac.to.getEdges().isEmpty())
					previous();
				else {
					currentModelGraphState = mgac.to;
					animator.animate(mgac.transitions);
				}
				// renderer.renderModelGraphState(currentModelGraphState);
			}
		};
		if (addToHistory)
			history.add(fromTypeName, toTypeName);
		renderRequest.schedule(50);
	}

	public void detail(Edge edge, boolean addToHistory) {
		modelGraphStateBuilder.setRelationshipFilter(filterPanel.getFilter());
		String fromTypeName = edge.getFromNode().getTypeSignature();
		String toTypeName = edge.getToNode().getTypeSignature();
		if (addToHistory)
			history.add(fromTypeName, toTypeName);
		ModelGraphAnimationContext mgac = modelGraphStateBuilder.detail(fromTypeName, toTypeName, currentModelGraphState);
		currentModelGraphState = mgac.to;
		animator.animate(mgac.transitions);
		// renderer.renderModelGraphState(currentModelGraphState);
	}

	public void showMapper(GmEntityType type) {
		configurations.modellerMode = GmModellerMode.mapping;

		// GmEntityType gmEntityType = (GmEntityType) getType(typeName);
		ModelPath modelPath = new ModelPath();
		RootPathElement rootPathElement = new RootPathElement(type);
		modelPath.add(rootPathElement);
		smartMapper.setContent(modelPath);

		fireSelectionChanged(type.getTypeSignature(), true);

		animator.animate(modelGraphStateBuilder.getDisappearanceTransitions(currentModelGraphState));
		// renderer.renderModelGraphState(currentModelGraphState);
	}

	public void showModeller() {
		setCenterWidget(modellerPanel);

		configurations.modellerMode = GmModellerMode.condensed;

		fireSelectionChanged(configurations.currentFocusedType, true);

		animator.animate(modelGraphStateBuilder.getInitialTransitions(currentModelGraphState));
	}

	@Override
	public void onAnimationFinished() {
		if (renderRequest != null)
			renderRequest.cancel();
		renderRequest = null;
		if (fireSelectionChange) {
			fireGmSelectionListeners();
			fireSelectionChange = false;
		}
		if (configurations.modellerMode == GmModellerMode.mapping) {
			setCenterWidget(smartMapper);
			forceLayout();
		}
	}

	@Override
	public void render(List<GenericEntity> entitiesToRender) {
		// NOP
	}

	// public void addType(GmType type) {
	// model.getTypes().add(type);
	// session.listeners().entity(type).add(this);
	// addType(type.getTypeSignature());
	// }

	public void addType(String typeName) {
		// configurations.addedTypes.add(typeName);
		configurations.modellerView.getIncludesFilterContext().getAddedTypes().add(typeName);
		if (configurations.currentFocusedType.equals("?")) {
			configurations.currentFocusedType = typeName;
			focus(typeName, true);
		} else {
			focus(configurations.currentFocusedType, false);
		}
	}

	public void removeType(GmType type) {
		NestedTransaction nt = session.getTransaction().beginNestedTransaction();
		modelOracle.getTypes().onlyEntities().asGmTypes().forEach(customType -> {
			GmEntityType entityType = (GmEntityType) customType;
			new ArrayList<>(entityType.getProperties()).forEach(gmProperty -> {
				removeProperty(gmProperty, entityType, type);
			});
		});
		model.getTypes().remove(type);
		session.deleteEntity(type);
		// configurations.addedTypes.remove(type.getTypeSignature());
		configurations.modellerView.getIncludesFilterContext().getAddedTypes().remove(type.getTypeSignature());
		if (configurations.currentFocusedType.equals(type.getTypeSignature()))
			configurations.currentFocusedType = "?";
		nt.commit();

		if (type == selectedType) {
			selectedType = null;
			fireSelectionChanged(null, true);
		}
	}

	public void removeType(String typeName) {
		// configurations.addedTypes.remove(typeName);
		configurations.modellerView.getIncludesFilterContext().getAddedTypes().remove(typeName);
		if (configurations.currentFocusedType.equals(typeName))
			configurations.currentFocusedType = "?";
		focus(configurations.currentFocusedType, false);
	}

	public void removeDependeny(GmMetaModel model) {
		NestedTransaction nt = session.getTransaction().beginNestedTransaction();
		model.getTypes().forEach(type -> {
			this.model.getTypes().forEach(directType -> {
				if (directType.isGmEntity()) {
					GmEntityType directEntityType = (GmEntityType) directType;
					new ArrayList<>(directEntityType.getProperties()).forEach(gmProperty -> {
						removeProperty(gmProperty, directEntityType, type);
					});
				}
			});
		});
		this.model.getDependencies().remove(model);
		nt.commit();
	}

	private void removeProperty(GmProperty property, GmEntityType entityType, GmType propertyType) {
		if (isPropertyType(property.getType(), propertyType)) {
			entityType.getProperties().remove(property);
		}
	}

	private boolean isPropertyType(GmType type1, GmType type2) {
		if (type1 instanceof GmLinearCollectionType) {
			type1 = ((GmLinearCollectionType) type1).getElementType();
		}
		return type1 == type2;
	}

	public GmType getType(String typeSignature) {
		if (typeSignature.endsWith("---"))
			typeSignature = typeSignature.substring(0, typeSignature.indexOf("---"));
		return modelOracle.findGmType(typeSignature);
	}

	public ModelOracle getOracle() {
		return modelOracle;
	}

	public ModelMetaDataEditor getEditor() {
		return modelMetaDataEditor;
	}

	public ModelMdResolver getResolver() {
		return modelResolver.getMetaData();
	}

	private void error(Throwable t) {
		t.printStackTrace();
	}

	public void resetTypes() {
		typeSource.reset();
		typesOverviewPanel.adaptOverview();
	}

	public void adapType(GmType type) {
		typesOverviewPanel.adaptType(type);
	}

	public void previous() {
		history.previous(true);
	}

	public ModelGraphState getCurrentModelGraphState() {
		return currentModelGraphState;
	}

	@Override
	public String getDefaultActiveTabbedWidget() {
		return "Types";
	}

	@Override
	public GmDetailViewSupportContext onDetailViewEvent(String methodName, GmDetailViewSupport support) {
		return null;
	}

}
