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
package com.braintribe.gwt.gmview.client.js;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.fileapi.client.FileList;
import com.braintribe.gwt.gmview.action.client.ActionGroup;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.actionbar.client.ActionProviderConfiguration;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionProvider;
import com.braintribe.gwt.gmview.client.ContentSpecification;
import com.braintribe.gwt.gmview.client.ContentSpecificationListener;
import com.braintribe.gwt.gmview.client.GmActionSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewActionManager;
import com.braintribe.gwt.gmview.client.GmContentViewWindow;
import com.braintribe.gwt.gmview.client.GmEntityView;
import com.braintribe.gwt.gmview.client.GmListView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.GmViewport;
import com.braintribe.gwt.gmview.client.GmViewportListener;
import com.braintribe.gwt.gmview.client.GmeDragAndDropView;
import com.braintribe.gwt.gmview.client.HasFullscreenSupport;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.WebSocketSupport;
import com.braintribe.gwt.gmview.client.js.api.JsPersistenceSessionFactoryImpl;
import com.braintribe.gwt.gmview.client.js.interop.GmContentViewInterop;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.processing.service.api.ProcessorRegistry;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.workbench.TemplateBasedAction;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ContentPanel;

import tribefire.extension.js.model.deployment.JsUxComponent;

/**
 * Class for using external components. This class is both an {@link Widget} and a {@link GmContentView}.
 * It receives the {@link JsUxComponent} and its related {@link JavaScriptObject} module.
 * From that, it loads its contract and then calls createComponent for getting the JS {@link GmContentView}.
 * The UI {@link Element} of the external component is embedded into an {@link IFrameElement} if it is not an {@link Widget}.
 * @author michel.docouto
 *
 */
public class ExternalWidgetGmContentView extends ContentPanel implements GmContentView, GmEntityView, GmListView, GmActionSupport,
		GmViewActionProvider, GmExternalViewInitializationSupport, GmViewport, GmeDragAndDropView, HasFullscreenSupport, ContentSpecification {
	
	private static int counter = 0;
	private static final Logger logger = new Logger(ExternalWidgetGmContentView.class);
	public static String EXTERNAL_COMPONENT_ID_PREFIX = "gmeExternalComponent";
	
	private GmContentView externalGmContentView;
	private Widget theWidget;
	private List<GmExternalViewInitializationListener> instantiatedContentViewListener;
	private boolean isAttachedAsElement;
	private Element componentShell;
	private String headHtml;
	private GmContentViewActionManager actionManager;
	private boolean bodyLoaded;
	private ComponentCreateContext context;
	private JsUxComponentWidgetSupplier supplier;
	private PersistenceGmSession availableGmSession;
	private boolean configureSetContent;
	private boolean configureAddContent;
	private ModelPath contentToSet;
	private List<ModelPath> contentsToAdd;
	private String servicesUrl;
	private Supplier<ModelEnvironmentDrivenGmSession> rawSessionFactory;
	private ProcessorRegistry processorRegistry;
	private Evaluator<ServiceRequest> localEvaluator;
	private String clientId;
	private String sessionId;
	private WebSocketSupport webSocketSupport;
	private GmContentViewWindow window;
	//private ComponentParentMode componentParentMode;
	//private Element shadowChildElement;
	//private boolean isUsingIframe;
	
	public ExternalWidgetGmContentView(JsUxComponentWidgetSupplier supplier) {
		setBorders(false);
		setBodyBorder(false);
		setHeaderVisible(false);
		//mask(LocalizedText.INSTANCE.loadingExternalComponent());
		this.supplier = supplier;
	}
	
	public void setWindow(GmContentViewWindow window) {
		this.window = window;
	}
	
	public void setExternalModule(JsUxComponent jsUxComponent, JavaScriptObject externalModule) {
		if (externalModule == null)
			return;
		
		String componentShellId = EXTERNAL_COMPONENT_ID_PREFIX + counter++;
		/*if (jsUxComponent != null)
			this.componentParentMode = jsUxComponent.getComponentParentMode();
		isUsingIframe = componentParentMode == ComponentParentMode.iframe;
		if (isUsingIframe)
			componentShell = Document.get().createIFrameElement();
		else
			componentShell = Document.get().createDivElement();*/
		componentShell = Document.get().createDivElement();
		componentShell.setId(componentShellId);
		componentShell.addClassName(EXTERNAL_COMPONENT_ID_PREFIX);
		Style style = componentShell.getStyle();
		style.setBorderWidth(0, Unit.PX);
		style.setWidth(100, Unit.PCT);
		style.setHeight(100, Unit.PCT);
		style.setMargin(0, Unit.PX);
		style.setPadding(0, Unit.PX);
		style.setOverflow(Overflow.AUTO);
		
		/*if (componentParentMode == ComponentParentMode.shadow) {
			Element shadowRootElement = Document.get().createDivElement();
			shadowRootElement.setId("js-shadow-root" + (counter - 1));
			componentShell.appendChild(shadowRootElement);
			Element shadowElement = createShadow(shadowRootElement);
			shadowChildElement = Document.get().createDivElement();
			shadowChildElement.setId("js-shadow-child" + (counter - 1));
			shadowElement.appendChild(shadowChildElement);
		}*/
		
		theWidget = new Widget() {
			{
				setElement(componentShell);
			}
		};
		
		theWidget.addAttachHandler(event -> {
			if (!event.isAttached()) {
				if (isAttachedAsElement) {
					externalGmContentView.detachUxElement();
					if (supplier != null)
						supplier.handleDetachCssStyles(context);
				}
				bodyLoaded = false;
				return;
			}
			
			if (externalGmContentView != null) {
				if (isAttachedAsElement && supplier != null)
					supplier.handleAttachCssStyles(context);
				/*if (isUsingIframe && isAttachedAsElement) {
					((IFrameElement) componentShell).getContentDocument().getHead().setInnerHTML(headHtml);
					
					Element componentElement = externalGmContentView.getUxElement();
					((IFrameElement) componentShell).getContentDocument().getBody().appendChild(componentElement);
				}*/
				return;
			}
			
			Future<GmContentView> viewFuture = new Future<>();
			if (jsUxComponent != null)
				prepareExternalGmContentViewFromContract(jsUxComponent, externalModule, viewFuture);
			else
				prepareExternalGmContentView(externalModule, viewFuture, this);
			
			viewFuture.andThen(view -> {
				if (view == null) {
					//unmask();
					ErrorDialog.show("Error while preparing the external view.", new RuntimeException("null was returned as view"));
					return;
				}
				
				if (view instanceof GmContentViewInterop)
					((GmContentViewInterop) view).setViewWidget(ExternalWidgetGmContentView.this);
				
				externalGmContentView = view;
				Object externalWidget = externalGmContentView.getUxWidget();
				if (externalWidget instanceof Widget) {
					add((Widget) externalWidget);
					//unmask();
					
					handleInitialization();
					return;
				}
				
				if (supplier != null)
					supplier.handleAttachCssStyles(context);
				
				Scheduler.get().scheduleFixedDelay(() -> {
					if (!bodyLoaded) {
						/*BodyElement body = null;
						if (isUsingIframe) {
							body = ((IFrameElement) componentShell).getContentDocument().getBody();
							if (body == null)
								return true;
						}*/
						
						isAttachedAsElement = true;
						Object uxElement = externalGmContentView.getUxElement();
						Element componentElement = Element.as((JavaScriptObject) uxElement);
						/*if (body != null)
							body.appendChild(componentElement);
						else {
							if (componentParentMode == ComponentParentMode.shadow)
								shadowChildElement.appendChild(componentElement);
							else
								componentShell.appendChild(componentElement);
						}*/
						componentShell.appendChild(componentElement);
						//RVE resize to window size
						componentElement.getStyle().setHeight(100, Unit.PCT);
						componentElement.getStyle().setWidth(100, Unit.PCT);
					
						bodyLoaded = true;
					}
					
					if (!externalGmContentView.isViewReady())
						return true;
					
					//unmask();
					
					handleInitialization();
					doLayout();
					forceLayout();
					return false;
				}, 100);
			}).onError(e -> {
				//unmask();
				ErrorDialog.show("Error while preparing the external view.", e);
			});
		});
		
		add(theWidget);
		
		//if (isUsingIframe)
			//inject(componentShell.cast());
	}
	
	/*@Override
	protected void onDetach() {
		if (isAttachedAsElement && isUsingIframe)
			headHtml = ((IFrameElement) componentShell).getContentDocument().getHead().getInnerHTML();
		super.onDetach();
	}*/
	
	@Override
	public GmContentView getView() {
		return this;
	}
	
	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		if (externalGmContentView != null)
			externalGmContentView.addSelectionListener(sl);
	}
	
	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		if (externalGmContentView != null)
			externalGmContentView.removeSelectionListener(sl);
	}
	
	@Override
	public ModelPath getFirstSelectedItem() {
		return externalGmContentView != null ? externalGmContentView.getFirstSelectedItem() : null;
	}
	
	@Override
	public int getFirstSelectedIndex() {
		return externalGmContentView != null ? externalGmContentView.getFirstSelectedIndex() : -1;
	}
	
	@Override
	public List<ModelPath> getCurrentSelection() {
		return externalGmContentView != null ? externalGmContentView.getCurrentSelection() : null;
	}
	
	@Override
	public boolean isSelected(Object element) {
		return externalGmContentView != null ? externalGmContentView.isSelected(element) : false;
	}
	
	@Override
	public void select(int index, boolean keepExisting) {
		if (externalGmContentView != null)
			externalGmContentView.select(index, keepExisting);
	}
	
	@Override
	public boolean select(Element element, boolean keepExisting) {
		if (externalGmContentView != null)
			return externalGmContentView.select(element, keepExisting);
		
		return false;
	}
	
	@Override
	public void deselectAll() {
		if (externalGmContentView != null)
			externalGmContentView.deselectAll();
	}
	
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		if (externalGmContentView == null)
			availableGmSession = gmSession;
		else {
			availableGmSession = null;
			externalGmContentView.configureGmSession(gmSession);
		}
	}
	
	@Override
	public PersistenceGmSession getGmSession() {
		return externalGmContentView != null ? externalGmContentView.getGmSession() : null;
	}
	
	@Override
	public void configureUseCase(String useCase) {
		if (externalGmContentView != null)
			externalGmContentView.configureUseCase(useCase);
	}
	
	@Override
	public String getUseCase() {
		return externalGmContentView != null ? externalGmContentView.getUseCase() : null;
	}
	
	@Override
	public ModelPath getContentPath() {
		return externalGmContentView != null ? externalGmContentView.getContentPath() : null;
	}
	
	@Override
	public void setContent(ModelPath modelPath) {
		if (externalGmContentView != null) {
			configureSetContent = false;
			contentToSet = null;
			externalGmContentView.setContent(modelPath);
		} else {
			configureSetContent = true;
			contentToSet = modelPath;
		}
	}
	
	@Override
	public void sendUxMessage(String messageType, String message, Object messageContext) {
		if (externalGmContentView != null) 
			externalGmContentView.sendUxMessage(messageType, message, messageContext);
	}
	
	@Override
	public Future<Boolean> waitReply() {
		if (externalGmContentView != null)
			return externalGmContentView.waitReply();
		
		return null;
	}
	
	private void handleInitialization() {
		if (externalGmContentView instanceof GmActionSupport)
			((GmActionSupport) externalGmContentView).setActionManager(actionManager);
		
		if (availableGmSession != null ) {
			externalGmContentView.configureGmSession(availableGmSession);
			availableGmSession = null;
		}
		
		if (configureSetContent) {
			configureSetContent = false;
			externalGmContentView.setContent(contentToSet);
			contentToSet = null;
		}
		
		if (configureAddContent) {
			configureAddContent = false;
			if (externalGmContentView instanceof GmListView)
				contentsToAdd.forEach(path -> ((GmListView) externalGmContentView).addContent(path));
			contentsToAdd = null;
		}
		
		fireInitializedListener();
	}
	
	@Override
	public void configureTypeForCheck(GenericModelType typeForCheck) {
		if (externalGmContentView instanceof GmListView)
			((GmListView) externalGmContentView).configureTypeForCheck(typeForCheck);
	}

	@Override
	public void addContent(ModelPath modelPath) {
		if (externalGmContentView instanceof GmListView) {
			configureAddContent = false;
			contentsToAdd = null;
			((GmListView) externalGmContentView).addContent(modelPath);
		} else if (externalGmContentView == null) {
			configureAddContent = true;
			if (contentsToAdd == null)
				contentsToAdd = new ArrayList<>();
			contentsToAdd.add(modelPath);
		}
	}
	
	@Override
	public List<ModelPath> getAddedModelPaths() {
		return externalGmContentView instanceof GmListView ? ((GmListView) externalGmContentView).getAddedModelPaths() : null;
	}
	
	@Override
	public boolean isReadOnly() {
		return externalGmContentView != null ? externalGmContentView.isReadOnly() : false;
	}
	
	@Override
	public void setReadOnly(boolean readOnly) {
		if (externalGmContentView != null)
			externalGmContentView.setReadOnly(readOnly);
	}
	
	//GmActionSupport methods
	@Override
	public void configureActionGroup(ActionGroup actionGroup) {
		if (externalGmContentView instanceof GmActionSupport)
			((GmActionSupport) externalGmContentView).configureActionGroup(actionGroup);
	}
	
	@Override
	public void configureExternalActions(List<Pair<ActionTypeAndName, ModelAction>> externalActions) {
		if (externalGmContentView instanceof GmActionSupport)
			((GmActionSupport) externalGmContentView).configureExternalActions(externalActions);
	}
	
	@Override
	public List<Pair<ActionTypeAndName, ModelAction>> getExternalActions() {
		return externalGmContentView instanceof GmActionSupport ? ((GmActionSupport) externalGmContentView).getExternalActions() : null;
	}
	
	@Override
	public void setActionManager(GmContentViewActionManager actionManager) {
		this.actionManager = actionManager;
		
		if (externalGmContentView instanceof GmActionSupport)
			((GmActionSupport) externalGmContentView).setActionManager(actionManager);
	}
	//End of GmActionSupport methods
	
	//GmViewActionProvider methods
	@Override
	public ActionProviderConfiguration getActions() {
		return externalGmContentView instanceof GmViewActionProvider ? ((GmViewActionProvider) externalGmContentView).getActions() : null;
	}
	
	@Override
	public boolean isFilterExternalActions() {
		return externalGmContentView instanceof GmViewActionProvider ? ((GmViewActionProvider) externalGmContentView).isFilterExternalActions() : false;
	}
	//End of GmViewActionProvider methods
	
	private void prepareExternalGmContentViewFromContract(JsUxComponent jsUxComponent, JavaScriptObject module, Future<GmContentView> future) {
		String modulePath = JsUxComponentWidgetSupplier.getNormalizedPath(jsUxComponent.getModule().getPath());
		
		if (!modulePath.startsWith("http")) {
			//RVE - relative path - need add SERVICES address
			modulePath = servicesUrl + modulePath; 
		}		
		
		TribefireUxModuleContract contract = null;
		//Added those try catch blocks because the methods are implemented by JS code. By catching them we have a better exception message
		try {
			contract = getContract(module);
		} catch (Exception ex) {
			logger.info("A contract is not available for the given module: " + modulePath, ex);
		}
		
		if (contract != null) {
			PersistenceGmSession session = getGmSession();
			String accessId = session != null ? session.getAccessId() : (availableGmSession != null ? availableGmSession.getAccessId() : null);
			Evaluator<ServiceRequest> remoteEvaluator = session;
			
			context = new ComponentCreateContext();
			context.setModulePath(modulePath);
			context.setCssStyles(new ArrayList<>());
			context.setPersistenceSessionFactory(new JsPersistenceSessionFactoryImpl(remoteEvaluator, rawSessionFactory));
			context.setAccessId(accessId);
			context.setRootUrl(Location.getProtocol() + "//" + Location.getHost() + "/");
			context.setServicesUrl(servicesUrl);
			//if (componentParentMode != null)
				//context.setComponentParentMode(componentParentMode.name());
			
			try {
				ServiceBindingContext serviceBindingContext = new ServiceBindingContext();
				serviceBindingContext.setProcessorRegistry(processorRegistry);
				serviceBindingContext.setLocalEvaluator(localEvaluator);
				serviceBindingContext.setRemoteEvaluator(remoteEvaluator);
				serviceBindingContext.setClientId(clientId);
				serviceBindingContext.setSessionId(sessionId);
				serviceBindingContext.setWebSocketImpl(webSocketSupport);
				if (bindServiceProcessorsAvailable(contract))
					contract.bindServiceProcessors(serviceBindingContext);
				if (bindWindowAvailable(contract))
					contract.bindWindow(window);
				
				GmContentView contentView = null;
				if (createComponentAvailable(contract))
					contentView = contract.createComponent(context, jsUxComponent);
				if (contentView == null) {
					Future<? extends GmContentView> contractFuture = contract.createComponentAsync(context, jsUxComponent);
					if (contractFuture != null) {
						contractFuture.andThen(future::onSuccess).onError(future::onFailure);
						return;
					}
				}
				
				GmContentView finalView = contentView;
				Scheduler.get().scheduleDeferred(() -> future.onSuccess(finalView));
			} catch (Exception ex) {
				future.onFailure(ex);
				return;
			}
			
			return;
		}
		
		try {
			prepareExternalGmContentView(module, future, this);
		} catch (Exception ex) {
			future.onFailure(ex);
		}
	}
	
	private native TribefireUxModuleContract getContract(JavaScriptObject module) /*-{
		return module.contract;
	}-*/;
	
	private native boolean bindServiceProcessorsAvailable(TribefireUxModuleContract contract) /*-{
		return (typeof contract.bindServiceProcessors === "function");
	}-*/;
	
	private native boolean bindWindowAvailable(TribefireUxModuleContract contract) /*-{
		return (typeof contract.bindWindow === "function");
	}-*/;
	
	private native boolean createComponentAvailable(TribefireUxModuleContract contract) /*-{
		return (typeof contract.createComponent === "function");
	}-*/;

	private native void prepareExternalGmContentView(JavaScriptObject module, Future<GmContentView> future, ExternalWidgetGmContentView view) /*-{
		var promise = module.createComponent();
		promise.then(
			function(loadedView) {
				future.@com.braintribe.gwt.async.client.Future::onSuccess(Ljava/lang/Object;)(loadedView);
			},
			function(err) {
				view.@com.braintribe.gwt.gmview.client.js.ExternalWidgetGmContentView::
						returnPrepareException(Lcom/braintribe/gwt/async/client/Future;Ljava/lang/Object;)(future, err);
			}
		);
	}-*/;
	
	private void returnPrepareException(Future<?> future, Object javascriptException) {
		JavaScriptException jsException = new JavaScriptException(javascriptException, "Error while preparing ExternalWidgetGmContentView");
		future.onFailure(jsException);
	}
	
	@Override
	public boolean isViewReady() {
		return externalGmContentView == null ? false : externalGmContentView.isViewReady();
	}

	@Override
	public void addInitializationListener(GmExternalViewInitializationListener listener) {
		if (instantiatedContentViewListener == null)
			instantiatedContentViewListener = new ArrayList<>();
		instantiatedContentViewListener.add(listener);
	}

	@Override
	public void removeInitializationListener(GmExternalViewInitializationListener listener) {
		if (instantiatedContentViewListener == null)
			return;
			
		instantiatedContentViewListener.remove(listener);		
	}

	private void fireInitializedListener() {
		if (instantiatedContentViewListener != null) {
			for (GmExternalViewInitializationListener listener : instantiatedContentViewListener)
				listener.onExternalViewInitialized(this);
		}
	}
	
	//Start of GmViewport methods
	@Override
	public void addGmViewportListener(GmViewportListener vl) {
		if (externalGmContentView instanceof GmViewport)
			((GmViewport) externalGmContentView).addGmViewportListener(vl);
	}
	
	@Override
	public void removeGmViewportListener(GmViewportListener vl) {
		if (externalGmContentView instanceof GmViewport)
			((GmViewport) externalGmContentView).removeGmViewportListener(vl);
	}
	
	@Override
	public boolean isWindowOverlappingFillingSensorArea() {
		if (externalGmContentView instanceof GmViewport)
			return ((GmViewport) externalGmContentView).isWindowOverlappingFillingSensorArea();
		
		return false;
	}
	//End of GmViewport methods
	
	//GmeDragAndDropView methods
	@Override
	public WorkbenchActionContext<TemplateBasedAction> getDragAndDropWorkbenchActionContext() {
		if (externalGmContentView instanceof GmeDragAndDropView)
			return ((GmeDragAndDropView) externalGmContentView).getDragAndDropWorkbenchActionContext();
		
		return null;
	}
	
	@Override
	public int getMaxAmountOfFilesToUpload() {
		if (externalGmContentView instanceof GmeDragAndDropView)
			return ((GmeDragAndDropView) externalGmContentView).getMaxAmountOfFilesToUpload();
		
		return 0;
	}
	
	@Override
	public void handleDropFileList(FileList fileList) {
		if (externalGmContentView instanceof GmeDragAndDropView)
			((GmeDragAndDropView) externalGmContentView).handleDropFileList(fileList);
	}
	//End of GmeDragAndDropView
	
	//HasFullscreenSupport methods
	@Override
	public void onMaximize() {
		if (externalGmContentView instanceof HasFullscreenSupport)
			((HasFullscreenSupport) externalGmContentView).onMaximize();
	}
	
	@Override
	public void onMinimize() {
		if (externalGmContentView instanceof HasFullscreenSupport)
			((HasFullscreenSupport) externalGmContentView).onMinimize();
	}
	//End of HasFullscreenSupport methods
	
	@Override
	public void onResize() {
		if (externalGmContentView instanceof RequiresResize)
			((RequiresResize) externalGmContentView).onResize();
		super.onResize();
	}
	
	//ContentSpecification methods
	@Override
	public void addContentSpecificationListener(ContentSpecificationListener listener) {
		if (externalGmContentView instanceof ContentSpecification)
			((ContentSpecification) externalGmContentView).addContentSpecificationListener(listener);
	}
	
	@Override
	public void removeContentSpecificationListener(ContentSpecificationListener listener) {
		if (externalGmContentView instanceof ContentSpecification)
			((ContentSpecification) externalGmContentView).removeContentSpecificationListener(listener);
	}
	//End of ContentSpecification methods

	public void setServicesUrl(String servicesUrl) {
		this.servicesUrl = servicesUrl;		
	}
	
	public void setRawSessionFactory(Supplier<ModelEnvironmentDrivenGmSession> rawSessionFactory) {
		this.rawSessionFactory = rawSessionFactory;
	}
	
	public void setProcessorRegistry(ProcessorRegistry processorRegistry) {
		this.processorRegistry = processorRegistry;
	}
	
	public void setLocalEvaluator(Evaluator<ServiceRequest> localEvaluator) {
		this.localEvaluator = localEvaluator;
	}
	
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public void setWebSocketSupport(WebSocketSupport webSocketSupport) {
		this.webSocketSupport = webSocketSupport;
	}
	
	/*private native void inject(IFrameElement el) /*-{
		el.contentWindow.$T = $wnd.$T;
		el.contentWindow.$tf = $wnd.$tf;
	}-*;
	
	private native Element createShadow(Element shadowRoot) /*-{
		return shadowRoot.attachShadow({mode: 'open'});
	}-*/
	
}