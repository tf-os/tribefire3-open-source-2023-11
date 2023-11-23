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
package com.braintribe.gwt.processdesigner.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.vectomatic.dom.svg.OMSVGDocument;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.utils.OMSVGParser;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.action.client.ActionGroup;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.gmview.actionbar.client.ActionProviderConfiguration;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionProvider;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.GmActionSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewActionManager;
import com.braintribe.gwt.gmview.client.GmEntityView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.HasAddtionalWidgets;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.TabbedWidgetContext;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.gwt.processdesigner.client.action.ProcessDesignerActions;
import com.braintribe.gwt.processdesigner.client.element.AbstractProcessSvgElement;
import com.braintribe.gwt.processdesigner.client.element.ProcessElementStylingUtil;
import com.braintribe.gwt.processdesigner.client.event.EdgeKindChoice.EdgeKind;
import com.braintribe.gwt.processdesigner.client.resources.LocalizedText;
import com.braintribe.gwt.processdesigner.client.vector.Point;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processdefrep.EdgeRepresentation;
import com.braintribe.model.processdefrep.NodeRepresentation;
import com.braintribe.model.processdefrep.ProcessDefinitionRepresentation;
import com.braintribe.model.processdefrep.ProcessElementRepresentation;
import com.braintribe.model.processdefrep.SwimLaneRepresentation;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;

import jsinterop.annotations.JsFunction;
import tribefire.extension.process.model.deployment.ConditionProcessor;
import tribefire.extension.process.model.deployment.ConditionalEdge;
import tribefire.extension.process.model.deployment.DecoupledInteraction;
import tribefire.extension.process.model.deployment.Edge;
import tribefire.extension.process.model.deployment.HasErrorNode;
import tribefire.extension.process.model.deployment.HasOverdueNode;
import tribefire.extension.process.model.deployment.Node;
import tribefire.extension.process.model.deployment.ProcessDefinition;
import tribefire.extension.process.model.deployment.ProcessElement;
import tribefire.extension.process.model.deployment.RestartNode;
import tribefire.extension.process.model.deployment.StandardNode;

public class ProcessDesigner extends BorderLayoutContainer
		implements InitializableBean, DisposableBean, GmEntityView, GmActionSupport, HasAddtionalWidgets, GmViewActionProvider {
	
	private static Logger logger = new Logger(ProcessDesigner.class);
	
	private ProcessDesignerConfigurator processDesignerConfigurator = new ProcessDesignerConfigurator();
	private ProcessDesignerConfiguration processDesignerConfiguration;
	private ProcessDesignerRenderer processDesignerRenderer = new ProcessDesignerRenderer();
	private ProcessDesignerActions processDesignerActions = new ProcessDesignerActions();
	//private ProcessDesignerTest processDesignerTest = new ProcessDesignerTest();
	
	private FocusPanel svgWrapperPanel;
	private FlowPanel statusPanel;
	private OMSVGDocument doc = OMSVGParser.currentDocument();
	private OMSVGSVGElement svg = doc.createSVGSVGElement();
	private ProcessDesignerConfigurationPanel processDesignerConfigurationPanel;
	
	private PersistenceGmSession session;
	private PersistenceGmSession workbenchSession;
	private ProcessDefinition processDefinition;
	
	private List<TabbedWidgetContext> tabbedWidgetContexts;
	private List<GmSelectionListener> gmSelectionListeners = new ArrayList<>();
	
	private GmContentViewActionManager actionManager;
	private ActionProviderConfiguration actionProviderConfiguration;
	private List<Pair<ActionTypeAndName, ModelAction>> externalActions  = new ArrayList<>();
	
	private Supplier<SpotlightPanel> quickAccessPanelProvider;
	private FlowPanel wrapperPanel;
	private Function<String, Future<Boolean>> accessIdChanger;
	private boolean waitingForModelEnvironment;
	private Consumer<Object> externalSessionConsumer;
	
	public ProcessDesigner() {
		setTabIndex(0);
		setBorders(false);
		setSize("100%", "100%");
		
		svg.setAttribute("version", "1.1");
		svg.setAttribute("xmlns", "http://www.w3.org/2000/svg");
		svg.setAttribute("style", "position: absolute; z-index: 1; top:0; left: 0; overflow: visible; width: 9999px; height: 9999px");

		wrapperPanel = new FlowPanel();
		wrapperPanel.setSize("100%", "100%");
		wrapperPanel.addStyleName("processDesignerWrappePanel");		
		wrapperPanel.add(getSvgWrapperPanel());
		wrapperPanel.add(getStatusPanel());
		add(wrapperPanel);
		
		showMode(ProcessDesignerMode.selecting);
		disableContextMenu(true);
		
		addAttachHandler(event -> {
			if (processDesignerRenderer.maskingTriggered)
				processDesignerRenderer.mask(processDesignerRenderer.maskingTextString);
		});
		
		forceLayout();
		
		addInsertNodeHandler(getElement(), () -> Scheduler.get().scheduleDeferred(() -> {
			if (!ProcessDesigner.this.isAttached())
				ProcessDesigner.this.onAttach();
			wrapperPanel.setSize("100%", "100%");
		}));
	}
	
	/**
	 * Configures the expert used for changing the accessId, in case the received session has a different access.
	 */
	@Required
	public void setAccessIdChanger(Function<String, Future<Boolean>> accessIdChanger) {
		this.accessIdChanger = accessIdChanger;
	}
	
	@Configurable
	public void setExternalSessionConsumer(Consumer<Object> externalSessionConsumer) {
		this.externalSessionConsumer = externalSessionConsumer;
	}
	
	public void setWorkbenchSession(PersistenceGmSession workbenchSession) {
		this.workbenchSession = workbenchSession;
	}
	
	public void setProcessDesignerConfigurationPanel(ProcessDesignerConfigurationPanel processDesignerConfigurationPanel) {
		this.processDesignerConfigurationPanel = processDesignerConfigurationPanel;
	}
	
	public void setQuickAccessPanelProvider(Supplier<SpotlightPanel> quickAccessPanelProvider) {
		this.quickAccessPanelProvider = quickAccessPanelProvider;
	}
	
	public ProcessDefinition getProcessDefinition() {
		return processDefinition;
	}
	
	public ProcessDesignerRenderer getProcessDesignerRenderer() {
		return processDesignerRenderer;
	}
	
	public ProcessDesignerConfiguration getProcessDesignerConfiguration() {
		return processDesignerConfiguration;
	}
	
	@Override
	protected void onResize(int width, int height) {
		super.onResize(width, height);
		
		Point defaultStartingPoint = Point.T.create();
		defaultStartingPoint.setX(0 + processDesignerConfiguration.getDefaultOffset());
		defaultStartingPoint.setY(0 + processDesignerConfiguration.getDefaultOffset());
		processDesignerConfiguration.setDefaultStartingPoint(defaultStartingPoint);
		
		svg.setAttribute("width", "100%");
		svg.setAttribute("height", "100%");
		
		processDesignerRenderer.render();
		processDesignerRenderer.adaptMenu();
	}
	
	@Override
	protected void onAttach() {
		super.onAttach();
		wrapperPanel.setSize("100%", "100%");
		processDesignerRenderer.render();
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if(visible)
			processDesignerRenderer.render();
	}
	
	FlowPanel flowPanel = new FlowPanel();
	ScrollPanel scrollPanel;
	public FocusPanel getSvgWrapperPanel() {
		if (svgWrapperPanel != null)
			return svgWrapperPanel;
		
		flowPanel.setWidth("9999px");
		flowPanel.setHeight("9999px");
		
		flowPanel.getElement().appendChild(svg.getElement());
		flowPanel.add(getNodeWrapperPanel());
		
		scrollPanel = new ScrollPanel(flowPanel);
		scrollPanel.setWidth("100%");
		scrollPanel.setHeight("100%");
		
		svgWrapperPanel = new FocusPanel(scrollPanel);
		svgWrapperPanel.setSize("100%", "100%");
		
		svgWrapperPanel.addKeyDownHandler(event -> {
			String p = Window.Navigator.getPlatform();
			boolean ctrlKeyDown = false;
			if (p.toLowerCase().startsWith("win"))
				ctrlKeyDown = event.isControlKeyDown();
			else if (p.toLowerCase().startsWith("mac"))
				ctrlKeyDown = event.isMetaKeyDown();
			
			if(event.getNativeKeyCode() == KeyCodes.KEY_DELETE){
				removeElements(getCurrentSelection());
			}else if(ctrlKeyDown){					
				if(event.getNativeKeyCode() == KeyCodes.KEY_ONE) {
					processDesignerActions.getAddStandardNode().perform();
					event.preventDefault();
					event.stopPropagation();
				}else if(event.getNativeKeyCode() == KeyCodes.KEY_TWO) {
					processDesignerActions.getAddRestartNode().perform();
					event.preventDefault();
					event.stopPropagation();
				}else if(event.getNativeKeyCode() == 187) {
					processDesignerActions.getZoomIn().perform();
					event.preventDefault();
					event.stopPropagation();
				}else if(event.getNativeKeyCode() == 189) {
					processDesignerActions.getZoomOut().perform();
					event.preventDefault();
					event.stopPropagation();
				}else if(event.getNativeKeyCode() == KeyCodes.KEY_A) {
					processDesignerRenderer.selectAll();
					event.preventDefault();
					event.stopPropagation();
				}else {
					processDesignerConfiguration.setProcessDesignerMode(ProcessDesignerMode.connecting);
					processDesignerRenderer.showMode(ProcessDesignerMode.connecting);
				}
			}else {
				if(event.getNativeKeyCode() == KeyCodes.KEY_UP) {
					scrollPanel.setVerticalScrollPosition(scrollPanel.getVerticalScrollPosition()-50);
				}else if(event.getNativeKeyCode() == KeyCodes.KEY_DOWN) {
					scrollPanel.setVerticalScrollPosition(scrollPanel.getVerticalScrollPosition()+50);
				}else if(event.getNativeKeyCode() == KeyCodes.KEY_LEFT) {
					scrollPanel.setHorizontalScrollPosition(scrollPanel.getHorizontalScrollPosition()-50);
				}else if(event.getNativeKeyCode() == KeyCodes.KEY_RIGHT) {
					scrollPanel.setHorizontalScrollPosition(scrollPanel.getHorizontalScrollPosition()+50);
				}
			}
		});
		
		svgWrapperPanel.addKeyUpHandler(event -> {
			System.err.println("onKeyUp");
			processDesignerConfiguration.setProcessDesignerMode(ProcessDesignerMode.selecting);
			processDesignerRenderer.hidePotentialEdgeGroup();
			processDesignerRenderer.showMode(ProcessDesignerMode.selecting);
		});
		
		scrollPanel.addScrollHandler(event -> processDesignerRenderer.adaptMenu());
		return svgWrapperPanel;
	}
	
	FlowPanel nodeWrapperPanel;
	public FlowPanel getNodeWrapperPanel() {
		if(nodeWrapperPanel == null) {
			nodeWrapperPanel = new FlowPanel();
			nodeWrapperPanel.addStyleName("processDesigner-nodesWrapperPanel");			
		}
		return nodeWrapperPanel;
	}
	
	public FlowPanel getStatusPanel() {
		if(statusPanel == null) {
			statusPanel = new FlowPanel();
			statusPanel.addStyleName("processDesignerStatusPanel");
		}
		return statusPanel;
	}
	
	@Override
	public ModelPath getContentPath() {
		return null;
	}
	
	@Override
	public ActionProviderConfiguration getActions() {
		if (actionProviderConfiguration == null) {
			initializeActions();

			actionProviderConfiguration = new ActionProviderConfiguration();
			actionProviderConfiguration.setGmContentView(this);

			if (externalActions != null) {
				List<Pair<ActionTypeAndName, ModelAction>> allActions = new ArrayList<>();
				if (externalActions != null)
					allActions.addAll(externalActions);

				actionProviderConfiguration.addExternalActions(allActions);
			}
		}

		return actionProviderConfiguration;
	}
	
	@Override
	public boolean isFilterExternalActions() {
		return false;
	}

	@Override
	public void configureActionGroup(ActionGroup actionGroup) {
		initializeActions();
	}
	
	protected void initializeActions() {
		this.externalActions.addAll(processDesignerActions.getActions());
	}
	
	@Override
	public void configureExternalActions(List<Pair<ActionTypeAndName, ModelAction>> externalActions) {
		if (externalActions == null)
			this.externalActions.clear();
		else {
			this.externalActions.clear();
			this.externalActions.addAll(externalActions);
			for (Pair<ActionTypeAndName, ModelAction> pair : externalActions) {
				if (actionManager != null && !actionManager.isActionAvailable(pair.getFirst()))
					this.externalActions.remove(pair);
			}
		}
		
		if (actionProviderConfiguration != null)
			actionProviderConfiguration.addExternalActions(this.externalActions);
	}

	@Override
	public List<Pair<ActionTypeAndName, ModelAction>> getExternalActions() {		
		return externalActions;
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
	public void setContent(ModelPath modelPath) {
		if (modelPath == null || modelPath.last() == null || modelPath.last().getType() == null
				|| !ProcessDefinition.T.getTypeSignature().equals(modelPath.last().getType().getTypeSignature())) {
			processDesignerRenderer.reset();
			return;
		}
		
		processDesignerRenderer.initialize();
		GlobalState.mask(LocalizedText.INSTANCE.loadingProcessDefinition());
		
		new Timer() {
			@Override
			public void run() {
				if (waitingForModelEnvironment) {
					this.schedule(200);
					return;
				}
				
				ProcessDefinition pd = modelPath.last().getValue();
				session.merge().suspendHistory(true).adoptUnexposed(true).doFor(pd);
				handleProcessDefinition(pd);
				fireSelectionChanged();
			}
		}.schedule(500);
	}
	
	private void handleQueryError(Throwable e) {
		GlobalState.unmask();
		ErrorDialog.show("Error while querying for processDefinition", e);
	}
	
	private void transferRepresentations(ProcessDefinition definition, ProcessDefinitionRepresentation representation){
		if (definition.getElements() != null && !definition.getElements().isEmpty())
			definition.getElements().clear();
		
		if (representation.getProcessElements() == null || representation.getProcessElements().isEmpty())
			return;
		
		Set<ConditionalEdge> conditionalEdges = new HashSet<>();
		for(Entry<ProcessElement, ProcessElementRepresentation> representations : representation.getProcessElements().entrySet()){
			ProcessElement processElementToAdd = null;
			if(representations.getKey() instanceof StandardNode){
				StandardNode standardNode = (StandardNode) representations.getKey();
				if(standardNode.getConditionalEdges() != null && !standardNode.getConditionalEdges().isEmpty()){
					for(ConditionalEdge conditionalEdge : standardNode.getConditionalEdges()){
						conditionalEdges.add(conditionalEdge);
					}
				}
				processElementToAdd = standardNode;
			}else if(!(representations.getKey() instanceof ConditionalEdge))
				processElementToAdd = representations.getKey();
			
			if(processElementToAdd != null){
				definition.getElements().add(representations.getKey());	
				if(representation.getProcessElementRepresentations() == null)
					representation.setProcessElementRepresentations(new HashSet<>());
				representation.getProcessElementRepresentations().add(representations.getValue());
			}
		}
		
		for(ConditionalEdge conditionalEdge : conditionalEdges)
			definition.getElements().add(conditionalEdge);
		
		representation.getProcessElements().clear();
	}
	
	private void handleProcessDefinition(final ProcessDefinition processDefinition){
		if(this.processDefinition != null)
			session.listeners().entity(processDefinition).remove(processDesignerRenderer);
		
		this.processDefinition = processDefinition;
		
		AsyncCallback<ProcessDefinitionRepresentation> callback = AsyncCallback.of(representation -> {
			if(representation == null){
				NestedTransaction nestedTransaction = session.getTransaction().beginNestedTransaction();
				representation = session.create(ProcessDefinitionRepresentation.T);
				representation.setName(processDefinition.getName());
				representation.setProcessDefinition(processDefinition);
				representation.setProcessElements(new HashMap<ProcessElement, ProcessElementRepresentation>());
				processDefinition.getMetaData().add(representation);
				nestedTransaction.commit();
			}
			if(representation.getProcessDefinition() == null && ProcessDesigner.this.processDefinition != null)
				representation.setProcessDefinition(ProcessDesigner.this.processDefinition);
			if(representation.getProcessElements() != null && !representation.getProcessElements().isEmpty())
				transferRepresentations(processDefinition, representation);
			
			session.listeners().entity(processDefinition).add(processDesignerRenderer);
			session.listeners().entity(representation).add(processDesignerRenderer);
			
			processDesignerRenderer.renderProcessDefinition(representation, true);
			GlobalState.unmask();
		}, e -> {
			NestedTransaction nestedTransaction = session.getTransaction().beginNestedTransaction();
			ProcessDefinitionRepresentation representation = session.create(ProcessDefinitionRepresentation.T);
			representation.setName(processDefinition.getName());
			representation.setProcessDefinition(processDefinition);
			representation.setProcessElements(new HashMap<ProcessElement, ProcessElementRepresentation>());
			processDefinition.getMetaData().add(representation);
			nestedTransaction.commit();
			processDesignerRenderer.renderProcessDefinition(representation, true);
			GlobalState.unmask();
		});
		
		getProcessDefinitionRepresentation(processDefinition, callback);
	}
	
	private void getProcessDefinitionRepresentation(ProcessDefinition processDefinition, final AsyncCallback<ProcessDefinitionRepresentation> callback){
		EntityQuery representationQuery = EntityQueryBuilder.from(ProcessDefinitionRepresentation.class)
				.where()
				.property("name").eq(processDefinition.getName()).
				tc().negation().joker().done();
		try {
			ProcessDefinitionRepresentation representation = session.queryCache().entities(representationQuery).first();
			if (representation != null)
				updateProcessDefinitionRepresentation(representationQuery, callback);
			else
				updateProcessDefinitionRepresentation(representationQuery, callback);
		} catch (Exception e) {
			updateProcessDefinitionRepresentation(representationQuery, callback);
		}
		
	}
	
	private void updateProcessDefinitionRepresentation(EntityQuery representationQuery, final AsyncCallback<ProcessDefinitionRepresentation> callback){
		session.query().entities(representationQuery).result(AsyncCallback.of(future -> {
			try {
				ProcessDefinitionRepresentation representation = future.first();
				callback.onSuccess(representation);
			} catch (GmSessionException e) {
				callback.onFailure(e);
			}
		}, callback::onFailure));
	}

	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		if(sl != null)
			gmSelectionListeners.add(sl);
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		if(sl != null)
			gmSelectionListeners.remove(sl);
	}

	@Override
	public ModelPath getFirstSelectedItem() {
		if(getCurrentSelection() != null && !getCurrentSelection().isEmpty()) {
			ModelPath path = getCurrentSelection().get(0);
			return path;
		}
		
		return null;
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		List<ModelPath> selection = new ArrayList<ModelPath>();
		if(processDesignerRenderer.getSelectedElements() != null && !processDesignerRenderer.getSelectedElements().isEmpty()){
			for(AbstractProcessSvgElement<?> processSvgElement : processDesignerRenderer.getSelectedElements()){
				ModelPath modelPath = new ModelPath();
				if(processSvgElement.getEntity() != null){				
					if(processSvgElement.getEntity() instanceof NodeRepresentation){
						Node node = ((NodeRepresentation)processSvgElement.getEntity()).getNode();
						if(node.getState() != null)
							modelPath.add(new RootPathElement(Node.T, node));
						else
							modelPath.add(new RootPathElement(ProcessDefinition.T, this.processDefinition));
					}else if(processSvgElement.getEntity() instanceof EdgeRepresentation){
						EdgeRepresentation edgeRepresentation = (EdgeRepresentation) processSvgElement.getEntity();
						Edge edge = edgeRepresentation.getEdge();
						modelPath.add(new RootPathElement(Edge.T, edge));
					}else if(processSvgElement.getEntity() instanceof SwimLaneRepresentation){
						SwimLaneRepresentation swimLaneRepresentation = ((SwimLaneRepresentation)processSvgElement.getEntity());
						modelPath.add(new RootPathElement(SwimLaneRepresentation.T, swimLaneRepresentation));
					}else if(processSvgElement.getEntity() instanceof ConditionProcessor){
						ConditionProcessor condition = (ConditionProcessor) processSvgElement.getEntity();
						modelPath.add(new RootPathElement(ConditionProcessor.T, condition));
					}else if(processSvgElement.getEntity() instanceof DecoupledInteraction){
						DecoupledInteraction di = (DecoupledInteraction) processSvgElement.getEntity();
						modelPath.add(new RootPathElement(DecoupledInteraction.T, di));
					}else if(processSvgElement.getEntity() instanceof ProcessDefinitionRepresentation){
						ProcessDefinitionRepresentation pdr = (ProcessDefinitionRepresentation) processSvgElement.getEntity();
						ProcessDefinition pd = pdr.getProcessDefinition();
						modelPath.add(new RootPathElement(ProcessDefinition.T, pd));
					}else if(processSvgElement.getEntity() instanceof Node){
						Node node = (Node) processSvgElement.getEntity();
						modelPath.add(new RootPathElement(Node.T, node));
					}else if(processSvgElement.getEntity() instanceof Edge){
						Edge edge = (Edge) processSvgElement.getEntity();
						modelPath.add(new RootPathElement(Edge.T, edge));
					}
					if(!modelPath.isEmpty())
						selection.add(modelPath);
				}			
			}	
		}else{
			ModelPath modelPath = new ModelPath();
			modelPath.add(new RootPathElement(ProcessDefinition.T, this.processDefinition));
			selection.add(modelPath);
			
		}
		return selection;
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
		if (session == null) {
			session = gmSession;
			return;
		} else {
			if (externalSessionConsumer != null)
				externalSessionConsumer.accept(gmSession);
		}
		
		String newAccessId = gmSession.getAccessId();
		if (session.getAccessId() == null || !session.getAccessId().equals(newAccessId)) {
			if (newAccessId == null)
				newAccessId = "cortex";
			waitingForModelEnvironment = true;
			accessIdChanger.apply(newAccessId).andThen(result -> {
				if (actionManager != null)
					actionManager.connect(this);
				waitingForModelEnvironment = false;
			});
		} else if (actionManager != null)
			actionManager.connect(this);
	}

	@Override
	public PersistenceGmSession getGmSession() {
		return session;
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
	public void configureAdditionalWidgets(List<TabbedWidgetContext> additionalWidgets) {
		//NOP
	}
	
	@Override
	public List<TabbedWidgetContext> getTabbedWidgetContexts() {
		if(tabbedWidgetContexts == null){
			tabbedWidgetContexts = new ArrayList<>();
			
			if(processDesignerConfigurationPanel != null){
				TabbedWidgetContext filterContext = new TabbedWidgetContext(LocalizedText.INSTANCE.configuration(),
						LocalizedText.INSTANCE.configuration(), () -> processDesignerConfigurationPanel);
				tabbedWidgetContexts.add(filterContext);
			}
		}
		return tabbedWidgetContexts;
	}

	@Override
	public void disposeBean() throws Exception {
		if (workbenchSession != null)
			workbenchSession.listeners().entity(processDesignerConfiguration).remove(processDesignerConfigurator);
		session.listeners().entity(processDefinition).remove(processDesignerRenderer);		
		processDesignerRenderer.reset();		
	}

	@Override
	public void intializeBean() throws Exception {
		if (workbenchSession != null)
			processDesignerConfiguration = workbenchSession.create(ProcessDesignerConfiguration.T);
		else
			processDesignerConfiguration = ProcessDesignerConfiguration.T.create();
		processDesignerConfiguration.setDefaultStartingPoint(Point.T.create());
		
		processDesignerRenderer.setProcessDesigner(this);
		processDesignerRenderer.setScrollPanel(scrollPanel);
		processDesignerRenderer.setSvg(svg);
		processDesignerRenderer.setNodeWrapperPanel(getNodeWrapperPanel());
		processDesignerRenderer.setProcessDesignerConfiguration(processDesignerConfiguration);
		processDesignerRenderer.setProcessDesignerActions(processDesignerActions);
		processDesignerRenderer.setSession(session);
		
		
		processDesignerActions.setProcessDesigner(this);
		processDesignerActions.setProcessDesignerRenderer(processDesignerRenderer);
		processDesignerActions.setProcessDesignerConfiguration(processDesignerConfiguration);
		processDesignerActions.setSession(workbenchSession);
		processDesignerActions.setSvg(svg);
		processDesignerActions.setQuickAccessPanelProvider(quickAccessPanelProvider);
		
		//processDesignerTest.setSession(session);
		
		if (workbenchSession != null)
			workbenchSession.listeners().entity(processDesignerConfiguration).add(processDesignerConfigurator);
		
		processDesignerConfigurator.setProcessDesignerConfiguration(processDesignerConfiguration);
		
		if(processDesignerConfigurationPanel != null){
			processDesignerConfigurationPanel.setProcessDesignerRenderer(processDesignerRenderer);
			processDesignerConfigurationPanel.setProcessDesignerConfiguration(processDesignerConfiguration);
		}
		
		ProcessElementStylingUtil.getInstance().setPdc(processDesignerConfiguration);
	}
	
	boolean isFiring = false;
	public void fireSelectionChanged(){
		Scheduler.get().scheduleDeferred(() -> {
			if (isFiring)
				return;
			
			try{
				isFiring = true;
				List<Object> objectListeners = new ArrayList<>(gmSelectionListeners);
				for (Object obj : objectListeners) {
					if (obj instanceof GmSelectionListener)
						((GmSelectionListener) obj).onSelectionChanged(ProcessDesigner.this);
				}
			}finally{
				isFiring = false;
			}
		});		
	}
	
	public void addNode(Object state, String name, EntityType<? extends Node> nodeType){
		Node nullNode = null;
		Node newNode = null;
		NestedTransaction nestedTransaction = null;
		
		if(processDefinition.getElements() != null && !processDefinition.getElements().isEmpty()){
			for(ProcessElement processElement : processDefinition.getElements()){
				if(processElement instanceof Node){
					Node candidate = (Node)processElement;
					if(candidate.getState() != null && candidate.getState().equals(state)){
						newNode = candidate;
					}
				}
			}
		}else{
			//no nodes or edges are defined. therefore first added node will be init node
			nestedTransaction = session.getTransaction().beginNestedTransaction();			
			nullNode = session.create(StandardNode.T);
			nullNode.setState(null);
		}
		
		if(newNode == null){
			if(nestedTransaction == null)
				nestedTransaction = session.getTransaction().beginNestedTransaction();
			
			newNode = session.create(nodeType);
			newNode.setName(I18nTools.createLs(session, name));
			newNode.setState(state);
			
			if(processDefinition.getElements() == null)
				processDefinition.setElements(new HashSet<>());
			
			if(nullNode != null){
				processDefinition.getElements().add(nullNode);
			}
			
			processDefinition.getElements().add(newNode);
			
			nestedTransaction.commit();
		}else
			processDesignerRenderer.renderNode(newNode, false);
		
		processDesignerRenderer.selectElement(newNode);
		
		if(newNode.getName() != null)
			session.listeners().entity(newNode.getName()).add(processDesignerRenderer);
		session.listeners().entity(newNode).add(processDesignerRenderer);
	}
	
	public void addRelation(GenericEntity from, GenericEntity to, EdgeKind edgeKind) {
		if (edgeKind == null)
			return;
		
		switch(edgeKind){
		case conditional: case normal:
			addEdge((Node)from,(Node)to , edgeKind);
			break;
		case overdue: case error:
			setOverdueOrErorNode(from, to, edgeKind);
			break;
		case restart:
			setRestartEdge((RestartNode)from, (Edge)to);
			break;
		}
	}
	
	public void setRestartEdge(RestartNode from, Edge to) {
		NestedTransaction nestedTransaction = session.getTransaction().beginNestedTransaction();
		
		from.setRestartEdge(to);
		
		nestedTransaction.commit();
	}
	
	public void setOverdueOrErorNode(GenericEntity from, GenericEntity to, EdgeKind edgeKind){
		NestedTransaction nestedTransaction = session.getTransaction().beginNestedTransaction();
		
		if(edgeKind == EdgeKind.overdue){
			((HasOverdueNode) from).setOverdueNode((Node) to);
		}else{
			((HasErrorNode) from).setErrorNode((Node) to);
		}
		
		nestedTransaction.commit();
	}
	
	public void addEdge(Node from, Node to, EdgeKind edgeKind){
		boolean invalid = false;
		for(ProcessElement processElement : processDefinition.getElements()){
			if(processElement instanceof Edge && edgeKind == EdgeKind.normal){
				Edge edge = (Edge)processElement;
				if(edge.getTo() == to && edge.getFrom() == from){
					invalid = true;
					break;
				}
			}
		}
		if(!invalid){
			NestedTransaction nestedTransaction = session.getTransaction().beginNestedTransaction();
			
			StandardNode standardFrom = (StandardNode)from;
			
			EntityType<? extends Edge> entityType = EdgeKind.conditional.equals(edgeKind) ? ConditionalEdge.T : Edge.T;
			Edge newEdge = session.create(entityType);
			
			newEdge.setFrom(standardFrom);
			
			if(newEdge instanceof ConditionalEdge){
				if(standardFrom.getConditionalEdges() == null)
					standardFrom.setConditionalEdges(new ArrayList<ConditionalEdge>());
				if(!standardFrom.getConditionalEdges().contains(newEdge))
					standardFrom.getConditionalEdges().add((ConditionalEdge) newEdge);
			}
			
			newEdge.setTo(to);
			
			if(processDefinition.getElements() == null)
				processDefinition.setElements(new HashSet<ProcessElement>());
			
			processDefinition.getElements().add(newEdge);
			
			nestedTransaction.commit();
			
			session.listeners().entity(newEdge).add(processDesignerRenderer);
		}
	}
	
	public void removeElements(List<ModelPath> selection){
		NestedTransaction nestedTransaction = session.getTransaction().beginNestedTransaction();
		
		for(ModelPath modelPath : selection){
			boolean canBeRemoved = true;
			GenericEntity genericEntity = modelPath.last().getValue();
			
			if(genericEntity instanceof ProcessElement){
				if(genericEntity instanceof Node){
					Node node = (Node)genericEntity;
					canBeRemoved = node.getState() != null;
					if(canBeRemoved){						
						
						if(node.getName() != null)
							session.listeners().entity(node.getName()).remove(processDesignerRenderer);
						
						for(Edge edge : getEdgesPerNode(node)){
							processDefinition.getElements().remove(edge);
							
							for(RestartNode restartNode : getRestartNodes(edge)){
								restartNode.setRestartEdge(null);
							}
							
							if(edge.getFrom() != null && edge.getFrom().getConditionalEdges() != null && edge.getFrom().getConditionalEdges().contains(edge))
								edge.getFrom().getConditionalEdges().remove(edge);
							
							processDesignerRenderer.removeRelatedElements(edge);
							
							session.listeners().entity(edge).remove(processDesignerRenderer);							
						}
						
						for(HasOverdueNode hasOverdueNode : getHasOverdueNodes(node)){
							hasOverdueNode.setOverdueNode(null);
						}
						
						for(HasErrorNode hasErrorNode : getHasErrorNodes(node)){
							hasErrorNode.setErrorNode(null);
						}
						
						processDesignerRenderer.removeRelatedElements(node);		
						
					}
				}		
				if(genericEntity instanceof Edge){
					Edge edge = (Edge) genericEntity;					
					
					for(RestartNode restartNode : getRestartNodes(edge)){
						restartNode.setRestartEdge(null);
					}
				
					if(edge.getFrom() != null && edge.getFrom().getConditionalEdges() != null && edge.getFrom().getConditionalEdges().contains(edge))
						edge.getFrom().getConditionalEdges().remove(edge);
					
					processDesignerRenderer.removeRelatedElements(edge);										
				}
				if(canBeRemoved){
					processDefinition.getElements().remove(genericEntity);
					session.listeners().entity(genericEntity).remove(processDesignerRenderer);
				}
				
			}else if(genericEntity instanceof SwimLaneRepresentation){
				processDesignerRenderer.removeSwimLane((SwimLaneRepresentation)genericEntity);
			}
		}		
		processDesignerRenderer.clearSelection();
		nestedTransaction.commit();		
	}
	
	public Set<Edge> getEdgesPerNode(Node node){
		Set<Edge> edges = new HashSet<>();
		if(processDefinition.getElements() != null && !processDefinition.getElements().isEmpty()){
			for(ProcessElement processElement : processDefinition.getElements()){
				if(processElement instanceof Edge){
					Edge edge = (Edge)processElement;
					if(edge.getTo() == node || edge.getFrom() == node)
						edges.add(edge);
				}
			}
			if(node instanceof StandardNode){
				StandardNode standardNode = (StandardNode) node;
				if(standardNode.getConditionalEdges() != null && !standardNode.getConditionalEdges().isEmpty()){
					for(ConditionalEdge conditionalEdge : standardNode.getConditionalEdges())
						edges.add(conditionalEdge);
				}
			}
		}
		return edges;
	}
	
	public Set<HasOverdueNode> getHasOverdueNodes(Node node){
		Set<HasOverdueNode> hasOverdueNodes = new HashSet<>();
		if(processDefinition.getElements() != null && !processDefinition.getElements().isEmpty()){
			for(ProcessElement processElement : processDefinition.getElements()){
				if(processElement instanceof HasOverdueNode){
					HasOverdueNode hasOverdueNode = (HasOverdueNode) processElement;
					if(hasOverdueNode.getOverdueNode() == node)
						hasOverdueNodes.add(hasOverdueNode);
				}
					
			}
		}
		if(processDefinition.getOverdueNode() == node)
			hasOverdueNodes.add(processDefinition);
		return hasOverdueNodes;
	}
	
	public Set<HasErrorNode> getHasErrorNodes(Node node){
		Set<HasErrorNode> hasErrorNodes = new HashSet<>();
		if(processDefinition.getElements() != null && !processDefinition.getElements().isEmpty()){
			for(ProcessElement processElement : processDefinition.getElements()) {
				if (processElement.getErrorNode() == node)
					hasErrorNodes.add(processElement);
			}
		}
		if(processDefinition.getErrorNode() == node)
			hasErrorNodes.add(processDefinition);
		return hasErrorNodes;
	}
	
	public Set<RestartNode> getRestartNodes(Edge edge){
		Set<RestartNode> restartNodes = new HashSet<>();
		if(processDefinition.getElements() != null && !processDefinition.getElements().isEmpty()){
			for(ProcessElement processElement : processDefinition.getElements()){
				if(processElement instanceof RestartNode){
					RestartNode restartNode = (RestartNode) processElement;
					if(restartNode.getRestartEdge() == edge)
						restartNodes.add(restartNode);
				}
			}
		}
		return restartNodes;
	}
	
	public Edge getInitEdge(Node to){
		for(ProcessElement processElement : processDefinition.getElements()){
			if(processElement instanceof Edge && ((Edge) processElement).getFrom().getState() == null && ((Edge) processElement).getTo() == to){
				return (Edge) processElement;
			}
		}
		return null;
	}
	
	public boolean isDrainNode(Node node){
		if(node.getState() != null){
			if(processDefinition != null && processDefinition.getElements() != null){
				for(ProcessElement processElement : processDefinition.getElements()){
					if(processElement instanceof Edge){
						Edge candidate = (Edge) processElement;
						if(candidate.getFrom() == node){
							return false;
						}						
					}
				}
				return true;
			}
		}
		return false;
	}
	
	public void showMode(ProcessDesignerMode mode) {
		getStatusPanel().getElement().setInnerText(LocalizedText.INSTANCE.mode(capitalize(mode.name())));
	}
	
	private String capitalize(final String line) {
		return Character.toUpperCase(line.charAt(0)) + line.substring(1);
	}

	public void setFocus(boolean b) {
		getSvgWrapperPanel().setFocus(b);
	}
	
	@Override
    public Object getUxElement() {
		Scheduler.get().scheduleDeferred(() -> {
			if (!ProcessDesigner.this.isAttached())
				ProcessDesigner.this.onAttach();
		});
		
    	return this.getElement();
    }
	
	@JsFunction
	public interface OnInsertNodeHandler {
		public void handleEvent();
	}
	
	private native void addInsertNodeHandler(Element el, OnInsertNodeHandler handler) /*-{
		el.addEventListener('DOMNodeInserted', handler);
	}-*/;
	
}
