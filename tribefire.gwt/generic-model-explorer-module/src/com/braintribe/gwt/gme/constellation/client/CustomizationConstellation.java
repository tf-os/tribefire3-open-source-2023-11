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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.async.client.Loader;
import com.braintribe.gwt.async.client.MultiLoader;
import com.braintribe.gwt.gme.constellation.client.expert.ModelEnvironmentDrivenSessionUpdater;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gme.cssresources.client.FavIconCssLoader;
import com.braintribe.gwt.gme.cssresources.client.TitleCssLoader;
import com.braintribe.gwt.gme.headerbar.client.DefaultHeaderBar;
import com.braintribe.gwt.gme.uitheme.client.UiThemeCssLoader;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.ModelEnvironmentSetListener;
import com.braintribe.gwt.gmview.client.js.JsScriptLoader;
import com.braintribe.gwt.gxt.gxtresources.whitemask.client.MaskController;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.logging.client.ExtendedErrorUI;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.gwt.logging.client.Profiling;
import com.braintribe.gwt.logging.client.ProfilingHandle;
import com.braintribe.model.accessapi.ModelEnvironment;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GmfException;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.Transaction;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;

public class CustomizationConstellation extends BorderLayoutContainer implements InitializableBean {
	private static final int NORTH_SIZE = 60;
	private static Logger logger = new Logger(CustomizationConstellation.class);
	private static final GenericModelTypeReflection typeReflection =  GMF.getTypeReflection();
	private static final String TITLE_SEPARATOR = " - ";
	private static int PROGRESS_INITIAL_VALUE = 51;
	private static int PROGRESS_MAX_VALUE = 90;
	
	static {
		ConstellationResources.INSTANCE.css().ensureInjected();
	}
	
	private ExplorerConstellation explorerConstellation;
	private final BorderLayoutData centerData;
	private Image logoImage;   //used with old style headerBar
	private TopBannerConstellation topBanner;  //used with old style headerBar
	private DefaultHeaderBar headerBar;
	private ModelEnvironmentDrivenGmSession gmSession;	
	private BorderLayoutData westData;
	private String accessId;
	private UiThemeCssLoader uiThemeLoader;
	private FavIconCssLoader favIconLoader;
	private TitleCssLoader titleLoader;
	private JsScriptLoader jsScriptLoader;
	private Loader<Void> sessionReadyLoader;
	private List<ModelEnvironmentSetListener> modelEnvironmentListeners;
	private boolean modelEnvironmentSetOnce = false;
	private boolean showHeader = true;
	private ProfilingHandle initializationProfiling;
	private int westDataSize;
	private boolean adaptWestData = true;
	private boolean useItwAsync = false;
	private ModelEnvironmentDrivenSessionUpdater modelEnvironmentDrivenSessionUpdater;
	private Supplier<AccessChoiceDialog> accessChoiceDialogSupplier;
	private AccessChoiceDialog accessChoiceDialog;
	private boolean appendAccessToTitle = false;
	private String clientLogonUseCase = "clientLogon";
	private Function<String, Future<ModelEnvironment>> modelEnvironmentProvider;
	private BorderLayoutData northLayoutData;
	private TransientGmSession transientSession;
	private ExtendedErrorUI customizationErrorUI;
	private String loginServletUrl = "/tribefire-services/login";
    private String applicationId = null;
	
	public CustomizationConstellation() {
		setBorders(false);
		
		centerData = new BorderLayoutData();
		centerData.setMargins(new Margins(3, 0, 0, 3));
		
		addDomHandler(event -> {
			event.stopPropagation();
			event.preventDefault();
		}, DragOverEvent.getType());

		addDomHandler(event -> {
			event.stopPropagation();
			event.preventDefault();
		}, DropEvent.getType());
	}
	
	/**
	 * Configures the required {@link ExplorerConstellation}.
	 */
	@Required
	public void setExplorerConstellation(final ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
		addModelEnvironmentSetListener(explorerConstellation);
	}
	
	/**
	 * Configures the {@link Image} used as logo.
	 * If {@link #setShowHeader(boolean)} is true, then this is required.
	 */
	@Configurable
	public void setLogoImage(Image logoImage) {
		this.logoImage = logoImage;
	}
	
	/**
	 * Configures the required banner that will be placed in the top side.
	 * If {@link #setShowHeader(boolean)} is true, then this is required.
	 */
	@Configurable
	public void setTopBanner(TopBannerConstellation topBanner) {
		this.topBanner = topBanner;
	}
	
	/**
	 * Configures the headerBar that will be placed in the top side.
	 * If {@link #setShowHeader(boolean)} is true, then this is required.
	 */
	@Configurable
	public void setHeaderBar(DefaultHeaderBar headerBar) {
		this.headerBar = headerBar;
	}
	
	/**
	 * Configures the required {@link PersistenceGmSession}.
	 */
	@Required
	public void setPersistenceSession(ModelEnvironmentDrivenGmSession gmSession) {
		this.gmSession = gmSession;
	}

	/**
	 * Configures the provider used for loading the model environment.
	 */
	@Required
	public void setModelEnvironmentProvider(Function<String, Future<ModelEnvironment>> modelEnvironmentProvider) {
		this.modelEnvironmentProvider = modelEnvironmentProvider;
	}
	
	/**
	 * Configures the session that will be configured with the transient model, if available.
	 */
	@Required
	public void setTransientSession(TransientGmSession transientSession) {
		this.transientSession = transientSession;
	}
	
	/**
	 * Configures the initial accessId.
	 */
	@Configurable
	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}
	
	@Configurable
	public void setUiTheme(UiThemeCssLoader uiThemeLoader) {
		this.uiThemeLoader = uiThemeLoader;
	}

	@Configurable
	public void setFavIcon(FavIconCssLoader favIconLoader) {
		this.favIconLoader = favIconLoader;
	}
	
	@Configurable
	public void setTitle(TitleCssLoader titleLoader) {
		this.titleLoader = titleLoader; 
	}
		
	@Configurable
	public void setJsScriptLoader(JsScriptLoader jsScriptLoader) {
		this.jsScriptLoader = jsScriptLoader; 
	}

	@Configurable
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}	
	
	/**
	 * Configures an optional loader to be called after the session is prepared.
	 */
	@Configurable
	public void setSessionReadyLoader(Loader<Void> sessionReadyLoader) {
		this.sessionReadyLoader = sessionReadyLoader;
	}
	
	/**
	 * Configures whether we should use ITW asynchronously. Defaults to false.
	 */
	@Configurable
	public void setUseItwAsync(boolean useItwAsync) {
		this.useItwAsync = useItwAsync;
	}
	
	/**
	 * Configures the {@link ModelEnvironmentDrivenSessionUpdater} used for configuring external sessions with ModelEnvironment once the
	 * {@link ModelEnvironment} is changed.
	 */
	@Configurable
	public void setModelEnvironmentDrivenSessionUpdater(ModelEnvironmentDrivenSessionUpdater modelEnvironmentDrivenSessionUpdater) {
		this.modelEnvironmentDrivenSessionUpdater = modelEnvironmentDrivenSessionUpdater;
	}
	
	/**
	 * Configures if the accessId should be appended to the page title. Defaults to false.
	 */
	@Configurable
	public void setAppendAccessToTitle(boolean appendAccessToTitle) {
		this.appendAccessToTitle = appendAccessToTitle;
	}
	
	/**
	 * Configures the useCase used for checking the ModelVisibility metadata.
	 * Defaults to "clientLogon".
	 */
	@Configurable
	public void setClientLogonUseCase(String clientLogonUseCase) {
		this.clientLogonUseCase = clientLogonUseCase;
	}
	
	@Configurable
	public void setLoginServletUrl(String loginServletUrl) {
		this.loginServletUrl = loginServletUrl;
	}
	
	/**
	 * Configures the {@link ExtendedErrorUI} to be used after the {@link CustomizationConstellation} is initialized.
	 */
	@Configurable
	public void setCustomizationErrorUI(ExtendedErrorUI customizationErrorUI) {
		this.customizationErrorUI = customizationErrorUI;
	}
	
	public void setShowHeader(boolean showHeader) {
		this.showHeader = showHeader;
	}
	
	public void setWestDataSize(int westDataSize) {
		this.westDataSize = westDataSize;
	}
	
	public void setAdaptWestData(boolean adaptWestData) {
		this.adaptWestData = adaptWestData;
	}
	
	public void setAccessChoiceDialogSupplier(Supplier<AccessChoiceDialog> accessChoiceDialogSupplier) {
		this.accessChoiceDialogSupplier = accessChoiceDialogSupplier;
	}
	
	public void addModelEnvironmentSetListener(ModelEnvironmentSetListener listener) {
		if (modelEnvironmentListeners == null)
			modelEnvironmentListeners = new ArrayList<>();
		modelEnvironmentListeners.add(listener);
		
		if (modelEnvironmentSetOnce)
			listener.onModelEnvironmentSet();
	}
	
	public void removeModelEnvironmentSetListener(ModelEnvironmentSetListener listener) {
		if (modelEnvironmentListeners != null) {
			modelEnvironmentListeners.remove(listener);
			if (modelEnvironmentListeners.isEmpty())
				modelEnvironmentListeners = null;
		}
	}
	
	@Override
	public void intializeBean() throws Exception {
		setCenterWidget(explorerConstellation, centerData);
		if (showHeader) {
			northLayoutData = new BorderLayoutData(NORTH_SIZE);
			setNorthWidget(prepareNorthPanel(), northLayoutData);
		}

		
		MaskController.setProgressMask(true, PROGRESS_INITIAL_VALUE, PROGRESS_MAX_VALUE);
		mask(LocalizedText.INSTANCE.loadingMetaData());
		initializationProfiling = Profiling.start(CustomizationConstellation.class, "Initialization Profiling (async)", true, true);
		
		if (accessId != null)
			initializeSession();
		else {
			modelEnvironmentProvider.apply(accessId); //Needed for triggering the bootstrapping load
			showAccessChoiceDialog();
		}
		
		if (customizationErrorUI != null)
			ErrorDialog.setErrorUI(customizationErrorUI);
	}

	/**
	 * Returns the current accessId.
	 */
	public String getAccessId() {
		return accessId;
	}
	
	public void hideHeader() {
		if (showHeader)
			northLayoutData.setSize(0);
	}
	
	public void restoreHeader() {
		if (showHeader)
			northLayoutData.setSize(NORTH_SIZE);
	}
	
	/**
	 * When we use Mask, some UI components should be disabled.
	 */
	protected void disableUI() {
		if (headerBar != null)
			headerBar.disableUI();
		if (topBanner != null)
			topBanner.disableUI();
	}
	
	/**
	 * When we finish masking, some UI components should be enabled back.
	 */
	protected void enableUI() {
		if (headerBar != null)
			headerBar.enableUI();
		if (topBanner != null)
			topBanner.enableUI();
	}
	
	private boolean isModelVisible() {
		return gmSession.getModelAccessory().getMetaData().useCase(clientLogonUseCase).is(Visible.T);
	}
	
	private void initializeSession() {
		if (accessId != null) {
			if (uiThemeLoader != null)
				uiThemeLoader.loadUiThemeCss(accessId, applicationId);
			if (favIconLoader != null)
				favIconLoader.loadFavIcon(accessId, applicationId);			
			if (titleLoader != null)
				titleLoader.loadTitle(accessId, applicationId);
			if (jsScriptLoader != null)
				jsScriptLoader.loadScript();								
		}
		
		ProfilingHandle ph = Profiling.start(CustomizationConstellation.class, "Getting Model Environment (async)", true);
		
		modelEnvironmentProvider.apply(accessId) //
				.andThen(modelEnvironment -> {
					ph.stop();
					CustomizationConstellation.this.accessId = modelEnvironment.getDataAccessId();
					ProfilingHandle ensuringPH = Profiling.start(CustomizationConstellation.class, "Ensuring Model Types (async)", true, true);
					ensureModelTypes(modelEnvironment) //
							.andThen(v -> {
								ensuringPH.stop();
								handleSessionInitialized(ph, modelEnvironment);
							}).onError(e -> {
								ensuringPH.stop();
								initializationProfiling.stop();
								MaskController.maskScreenOpaque = false;
								progressUnmask();
								ErrorDialog.show(LocalizedText.INSTANCE.errorEnsuringModelTypes(), e);
								e.printStackTrace();
							});
				}).onError(e -> {
					ph.stop();
					initializationProfiling.stop();
					MaskController.maskScreenOpaque = false;
					progressUnmask();
					ErrorDialog.show(LocalizedText.INSTANCE.errorGettingModelEnvironment(), e, true);
					e.printStackTrace();
				});
	}
	
	private void handleSessionInitialized(final ProfilingHandle ph, final ModelEnvironment modelEnvironment) {
		ProfilingHandle ph1 = Profiling.start(CustomizationConstellation.class, "Configuring Model Environment within the session (async)", true,
				true);
		
		gmSession.configureModelEnvironment(modelEnvironment, com.braintribe.processing.async.api.AsyncCallback.of( //
				v -> {
					if (!isModelVisible()) {
						MessageBox messageBox = new AlertMessageBox(LocalizedText.INSTANCE.information(), LocalizedText.INSTANCE.notAllowedAccess());
						messageBox.addDialogHideHandler(event -> Window.Location.replace(loginServletUrl));
						messageBox.show();
						return;
					}

					if (appendAccessToTitle)
						appendAccessToTitle(accessId);
					ph1.stop();
					if (explorerConstellation.getWorkbench() != null)
						explorerConstellation.getWorkbench().configureModelEnvironment(modelEnvironment);
					if (modelEnvironmentDrivenSessionUpdater != null)
						modelEnvironmentDrivenSessionUpdater.updateModelEnvironment(modelEnvironment);
					handleModelEnvironmentSet();

					if (headerBar != null)
						headerBar.configureGmSession(gmSession);

					if (westData != null && adaptWestData) {
						double explorerWestBorderSize = explorerConstellation.getWestBorderLayoutData() == null ? 0
								: explorerConstellation.getWestBorderLayoutData().getSize();
						if (explorerWestBorderSize != 0 && explorerWestBorderSize != westData.getSize()) {
							westData.setSize(explorerWestBorderSize);
							doLayout();
						}
					}
				}, e -> {
					initializationProfiling.stop();
					ph.stop();
					ph1.stop();
					MaskController.maskScreenOpaque = false;
					progressUnmask();
					ErrorDialog.show("Error while configuring the ModelEnvironment within the session.", e);
					e.printStackTrace();
				}));
	}

	private void progressUnmask() {
		if (MaskController.progressBarInitialValue >= PROGRESS_INITIAL_VALUE && MaskController.progressBarInitialValue <= PROGRESS_MAX_VALUE)
			MaskController.progressBarInitialValue = null;
		unmask();
	}
	
	private void globalUnmask() {
		if (MaskController.progressBarInitialValue >= PROGRESS_INITIAL_VALUE && MaskController.progressBarInitialValue <= PROGRESS_MAX_VALUE)
			MaskController.progressBarInitialValue = null;
		GlobalState.unmask();
	}
	
	private Future<Void> prepareCompleteProgress() {
		MaskController.setProgressMask(true, 100, 100);
		mask(LocalizedText.INSTANCE.loadingMetaData());
		
		Future<Void> future = new Future<>();
		new Timer() {
			@Override
			public void run() {
				progressUnmask();
				future.onSuccess(null);
			}
		}.schedule(300);
		
		return future;
	}
	
	private Future<Boolean> handleModelEnvironmentSet() {
		final Future<Boolean> future = new Future<>();
		
		if (sessionReadyLoader == null) {
			fireModelEnvironmentSet();
			MaskController.maskScreenOpaque = false;
			progressUnmask();
			prepareCompleteProgress().andThen(V -> future.onSuccess(true));
			return future;
		}
		
		final ProfilingHandle ph = Profiling.start(CustomizationConstellation.class, "Running Session Ready Loader (async)", true, true);
		sessionReadyLoader.load(AsyncCallbacks.of( //
				result -> {
					initializationProfiling.stop();
					ph.stop();
					fireModelEnvironmentSet();
					progressUnmask();
					prepareCompleteProgress().andThen(V -> future.onSuccess(true));
				}, e -> {
					initializationProfiling.stop();
					ph.stop();
					fireModelEnvironmentSet();
					progressUnmask();
					ErrorDialog.show("Error while calling the session ready loader.", e);
					e.printStackTrace();
					future.onFailure(e);
				}));
		
		return future;
	}
	
	private void fireModelEnvironmentSet() {
		modelEnvironmentSetOnce = true;
		
		if (modelEnvironmentListeners != null)
			modelEnvironmentListeners.forEach(listener -> listener.onModelEnvironmentSet());
	}
	
	private Future<Void> ensureModelTypes(final ModelEnvironment modelEnvironment) {
		GmMetaModel serviceModel = modelEnvironment.getServiceModel();
		if (useItwAsync) {
			final Future<Void> future = new Future<>();
			MultiLoader itwMultiLoader = new MultiLoader();
			if (modelEnvironment.getDataModel() != null)
				itwMultiLoader.add("dataModel", ensureModel(modelEnvironment.getDataModel()));
			if (modelEnvironment.getWorkbenchModel() != null)
				itwMultiLoader.add("workbenchModel", ensureModel(modelEnvironment.getWorkbenchModel()));
			if (serviceModel != null)
				itwMultiLoader.add("transientModel", ensureModel(serviceModel));

			itwMultiLoader.load(AsyncCallbacks.of( //
					result -> {
						prepareTransientSession(serviceModel);
						future.onSuccess(null);
					}, future::onFailure));
			
			return future;
		}
		
		try {
			if (modelEnvironment.getDataModel() != null) {
				ProfilingHandle ph = Profiling.start(CustomizationConstellation.class, "Ensuring Data Model", false);
				typeReflection.deploy(modelEnvironment.getDataModel());
				ph.stop();
			}
			
			if (modelEnvironment.getWorkbenchModel() != null) {
				ProfilingHandle ph = Profiling.start(CustomizationConstellation.class, "Ensuring Workbench Model", false);
				typeReflection.deploy(modelEnvironment.getWorkbenchModel());
				ph.stop();
			}
			
			if (serviceModel == null)
				prepareTransientSession(null);
			else {
				ProfilingHandle ph = Profiling.start(CustomizationConstellation.class, "Ensuring Service Model", false);
				typeReflection.deploy(serviceModel);
				ph.stop();
				prepareTransientSession(serviceModel);
			}
		} catch (GmfException ex) {
			logger.error("Error while ensuring model types.", ex);
			ex.printStackTrace();
		}
		
		return new Future<Void>(null);
	}
	
	/**
	 * Exchanges the current ModelEnvironment, refreshing the panels and manipulations.
	 */
	public Future<Boolean> exchangeModelEnvironment(final ModelEnvironment modelEnvironment) {
		final Future<Boolean> future = new Future<>();
		this.accessId = modelEnvironment.getDataAccessId();
		ensureModelTypes(modelEnvironment) //
				.andThen(result -> {
					gmSession.configureModelEnvironment(modelEnvironment, com.braintribe.processing.async.api.AsyncCallback.of( //
							v -> {
								if (!isModelVisible()) {
									future.onSuccess(false);
									return;
								}

								explorerConstellation.cleanup();
								if (modelEnvironmentDrivenSessionUpdater != null)
									modelEnvironmentDrivenSessionUpdater.updateModelEnvironment(modelEnvironment);
								handleModelEnvironmentSet().get(future);

								if (appendAccessToTitle)
									appendAccessToTitle(accessId);
							}, future::onFailure));
				}).onError(future::onFailure);
		
		return future;
	}
	
	private ContentPanel prepareNorthPanel() {
		ContentPanel northPanel = new ContentPanel();
		northPanel.setBodyBorder(false);
		northPanel.setBorders(false);
		northPanel.setHeaderVisible(false);
		
		topBanner.addStyleName("topBanner");
		topBanner.setWidth("auto");
		headerBar.setOldLogoImage(logoImage);
		headerBar.setOldTopBanner(topBanner);
		headerBar.setOldWestDataSize(westDataSize);
		northPanel.add(headerBar.getHeaderBar());			
		return northPanel;
	}
	
	private void appendAccessToTitle(String accessId) {
		if (this.titleLoader != null && this.titleLoader.isTitleSet())
			return;   //RVE - the title is set via Server Request (Http Request), than not rewrite it
		
		Document document = Document.get();
		String currentTitle = document.getTitle();
		int index = currentTitle.lastIndexOf(TITLE_SEPARATOR);
		if (index != -1)
			currentTitle = currentTitle.substring(0, index);
		
		document.setTitle(currentTitle + TITLE_SEPARATOR + accessId);
	}
	
	private void showAccessChoiceDialog() {
		if (accessChoiceDialog == null)
			accessChoiceDialog = accessChoiceDialogSupplier.get();
		
		accessChoiceDialog.getAccessId() //
				.andThen(accessId -> {
					if (accessId != null) {
						if (topBanner != null)
							topBanner.getQuickAccessField().setVisible(true);
						CustomizationConstellation.this.accessId = accessId;
						initializeSession();
						return;
					}

					if (headerBar != null)
						headerBar.apply(null); // initialize headerBar (default, old style)
					globalUnmask();
					GlobalState.showSuccess(LocalizedText.INSTANCE.noAccessesToDisplay());
					explorerConstellation.getHomeConstellation().changeExistingEmptyMessage(LocalizedText.INSTANCE.noAccessesToDisplay());
					explorerConstellation.getWorkbench().configureModelEnvironment(null);
					if (topBanner != null)
						topBanner.getQuickAccessField().setVisible(false);
				}).onError(e -> ErrorDialog.show(LocalizedText.INSTANCE.errorFetchingAccesses(), e));
	}

	private Future<Void> ensureModel(GmMetaModel model) {
		Future<Void> future = new Future<>();
		typeReflection.deploy(model, future);
		return future;
	}
	
	private void prepareTransientSession(GmMetaModel transientModel) {
		Transaction transaction = transientSession.getTransaction();
		List<Manipulation> manipulations = transaction.getManipulationsDone();
		if (manipulations != null && !manipulations.isEmpty())
			transaction.undo(manipulations.size());
		
		transientSession.cleanup();
		GmMetaModel currentTransientGmMetaModel = transientSession.getTransientGmMetaModel();
		if (currentTransientGmMetaModel != transientModel)
			transientSession.configureGmMetaModel(transientModel);
	}

}
