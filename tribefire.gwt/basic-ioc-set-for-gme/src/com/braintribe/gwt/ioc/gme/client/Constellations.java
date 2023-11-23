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
package com.braintribe.gwt.ioc.gme.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gwt.gm.storage.api.ColumnData;
import com.braintribe.gwt.gm.storage.api.StorageColumnInfo;
import com.braintribe.gwt.gme.constellation.client.BrowsingConstellation;
import com.braintribe.gwt.gme.constellation.client.BrowsingConstellationDialog;
import com.braintribe.gwt.gme.constellation.client.ChangesConstellation;
import com.braintribe.gwt.gme.constellation.client.ClipboardConstellationProvider;
import com.braintribe.gwt.gme.constellation.client.CustomizationConstellation;
import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.gme.constellation.client.GlobalActionsToolBar;
import com.braintribe.gwt.gme.constellation.client.HomeConstellation;
import com.braintribe.gwt.gme.constellation.client.HomeFoldersLoader;
import com.braintribe.gwt.gme.constellation.client.MasterDetailConstellation;
import com.braintribe.gwt.gme.constellation.client.QueryConstellation;
import com.braintribe.gwt.gme.constellation.client.SelectionConstellation;
import com.braintribe.gwt.gme.constellation.client.SelectionConstellationScopedBeanProvider;
import com.braintribe.gwt.gme.constellation.client.action.CancelGlobalActionsToolBar;
import com.braintribe.gwt.gme.constellation.client.action.GlobalActionPanel;
import com.braintribe.gwt.gme.constellation.client.expert.GIMASpecialViewHandler;
import com.braintribe.gwt.gme.constellation.client.expert.GlobalActionsHandler;
import com.braintribe.gwt.gme.servicerequestpanel.client.ServiceRequestExecutionConstellation;
import com.braintribe.gwt.gmview.action.client.FieldDialogOpenerAction;
import com.braintribe.gwt.ioc.gme.client.resources.CustomizationResources;
import com.braintribe.gwt.ioc.gme.client.resources.LocalizedText;
import com.braintribe.gwt.security.client.SessionScopedBeanProvider;
import com.braintribe.gwt.utils.client.FastMap;
import com.braintribe.gwt.validationui.client.ValidationConstellation;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.selector.KnownUseCase;
import com.braintribe.model.style.Color;
import com.braintribe.model.style.Font;
import com.braintribe.provider.PrototypeBeanProvider;
import com.braintribe.provider.SingletonBeanProvider;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;

import tribefire.extension.scripting.model.deployment.Script;

class Constellations {
	
	protected static Supplier<CustomizationConstellation> customizationConstellationProvider = new SessionScopedBeanProvider<CustomizationConstellation>() {
		{
			attach(Controllers.connectionController);
		}
		@Override
		public CustomizationConstellation create() throws Exception {
			CustomizationConstellation bean = publish(new CustomizationConstellation());
			bean.setAccessId(Runtime.accessId.get());
			bean.setUiTheme(Providers.uiThemeLoader.get());
			bean.setFavIcon(Providers.favIconLoader.get());
			bean.setTitle(Providers.titleLoader.get());
			
			bean.setApplicationId(Runtime.applicationId);
			bean.setAccessChoiceDialogSupplier(UiElements.accessChoiceDialog);
			bean.setExplorerConstellation(explorerConstellationProvider.get());
		
			bean.setPersistenceSession(Session.persistenceSession.get());
			bean.setSessionReadyLoader(Controllers.sessionReadyLoader.get());
			bean.addModelEnvironmentSetListener(homeConstellationProvider.get());
			bean.addModelEnvironmentSetListener(Providers.workbenchDataProvider.get());
			bean.addModelEnvironmentSetListener(Actions.advancedSaveAction.get());
			bean.addModelEnvironmentSetListener(Startup.localeProvider.get());
			bean.addModelEnvironmentSetListener(Controllers.webSocketExpert.get());
			bean.addModelEnvironmentSetListener(Controllers.jsUxModuleInitializer.get());
			
//			bean.addModelEnvironmentSetListener(Notification.loadAccessHandler.get());
//			bean.addModelEnvironmentSetListener(Notification.loadModelHandler.get());
//			bean.addModelEnvironmentSetListener(Notification.urlQueryHandler.get());
//			bean.addModelEnvironmentSetListener(Notification.showMapperHandler.get());
			
			boolean showHeader = Boolean.parseBoolean(Runtime.showHeader.get());
			bean.setShowHeader(showHeader);
			if (showHeader) {
				bean.setLogoImage(logoImageProvider.get());
				bean.setTopBanner(Panels.topBanner.get());
				bean.setHeaderBar(UiElements.defaulHeaderBar.get());
				bean.addModelEnvironmentSetListener(Panels.topBanner.get());
			}
			
			bean.setModelEnvironmentDrivenSessionUpdater(Controllers.modelEnvironmentDrivenSessionUpdater.get());
			bean.setAppendAccessToTitle(Runtime.appendAccessToTitle);
			bean.setLoginServletUrl(Runtime.loginServletUrlProvider.get());
			bean.setModelEnvironmentProvider(Providers.modelEnvironmentProvider.get());
			bean.setTransientSession(Session.transientManagedSession.get());
//			bean.setCustomizationErrorUI(Log.globalStateErrorUI.get());
			return bean;
		}
	};
	
	protected static Supplier<ExplorerConstellation> explorerConstellationProvider = new SessionScopedBeanProvider<ExplorerConstellation>() {
		{
			attach(Controllers.explorerWorkbenchController);
		}
		@Override
		public ExplorerConstellation create() throws Exception {
			ExplorerConstellation bean = publish(new ExplorerConstellation());
			bean.setWorkbench(Panels.workbenchProvider.get());
			bean.setVerticalTabPanel(Panels.verticalTabPanelProvider.get());
			bean.setHomeConstellation(homeConstellationProvider.get());
			bean.setNotificationConstellation(Notifications.notificationsConstellationProvider.get());
			bean.setValidationConstellation(validationConstellationProvider.get());
			bean.setChangesConstellation(changesConstellationProvider);
			bean.setGmSession(Session.persistenceSession.get());
			bean.setTransientGmSession(Session.transientManagedSession.get());
			bean.setTransientSessionSupplier(Session.serviceRequestScopedTransientGmSession);
			bean.setUseCase(Runtime.assemblyPanelUseCaseProvider.get());
			bean.setBrowsingConstellationProvider(browsingConstellationWithActionBarProvider);
			bean.setViewSituationResolver(ViewSituationResolution.viewSituationResolver);
			bean.setReadOnlyMasterDetailConstellationProvider(clipboardMasterDetailConstellationProvider);
			bean.setClipboardConstellationProvider(clipboardConstellationProviderProvider.get());
			bean.setGIMADialogProvider(UiElements.gimaDialogProvider);
			bean.setTransientGimaDialogProvider(UiElements.transientGimaDialogProvider);
			bean.setTemplateGimaDialogSupplier(UiElements.templateGimaDialogProvider);
			bean.setFieldDialogOpenerActions(fieldDialogOpenerActionsMap.get());
			//bean.setTemplateEvaluationPanelProvider(Panels.templateEvaluationPanelProvider);
			bean.setTemplateEvaluationDialogProvider(Panels.templateEvaluationDialogProvider);
			//bean.setValidationLogRepresenationLogProvider(Panels.validationLogListPanel);
			bean.setWorkbenchActionHandlerRegistry(Actions.workbenchActionHandlerRegistry.get());
			bean.setUseWorkbenchWithinTab(Boolean.parseBoolean(Runtime.useWorkbenchWithinTab.get()));
			bean.setUseClipboardConstellation(true);
			bean.setGmViewActionBarProvider(Panels.gmViewActionBar);
			bean.setServiceRequestConstellationProvider(Constellations.serviceRequestExecutionConstellationSupplier);
			bean.setCommitAction(Actions.saveAction);
			bean.setWorkWithEntityExpert(Controllers.workWithEntityExpert.get());
			bean.setGlobalActionPanel(explroerGlobalActionPanel.get());
			bean.setConstellationDefaultModelActionList(Actions.constellationSimpleActions.get());
			return bean;
		}
	};
	
	protected static Supplier<Map<EntityType<?>, Supplier<? extends FieldDialogOpenerAction<?>>>> fieldDialogOpenerActionsMap =
			new SingletonBeanProvider<Map<EntityType<?>, Supplier<? extends FieldDialogOpenerAction<?>>>>() {
		@Override
		public Map<EntityType<?>, Supplier<? extends FieldDialogOpenerAction<?>>> create() throws Exception {
			Map<EntityType<?>, Supplier<? extends FieldDialogOpenerAction<?>>> bean = publish(new HashMap<>());
			bean.put(LocalizedString.T, Actions.localizedStringFieldDialogOpenerAction);
			bean.put(Color.T, Actions.colorFieldDialogOpenerAction);
			bean.put(Font.T, Actions.fontFieldDialogOpenerAction);
			bean.put(Script.T, Actions.gmScriptEditorDialogOpenerAction);
			//bean.put(Javascript.T, Actions.gmScriptEditorDialogOpenerAction);
			//bean.put(Beanshell.T, Actions.gmScriptEditorDialogOpenerAction);
			//bean.put(Groovy.T, Actions.gmScriptEditorDialogOpenerAction);
			return bean;
		}
	};
	
	private static Supplier<Image> logoImageProvider = new SingletonBeanProvider<Image>() {
		@Override
		public Image create() throws Exception {
			//Image bean = new Image(CustomizationResources.INSTANCE.logoBlack());
			Image bean = new Image(CustomizationResources.INSTANCE.tfLogo().getSafeUri().asString());
			return bean;
		}
	};
	
	private static Supplier<BrowsingConstellation> browsingConstellationProvider = new PrototypeBeanProvider<BrowsingConstellation>() {
		@Override
		public BrowsingConstellation create() throws Exception {
			BrowsingConstellation bean = new BrowsingConstellation();
			bean.setTetherBar(Panels.tetherBarProvider.get());
			bean.setMasterDetailConstellationProvider(masterDetailConstellationProvider);
			bean.setMasterViewProvider(Panels.assemblyPanelProvider);
			bean.setViewSituationResolver(ViewSituationResolution.viewSituationResolver);
			bean.setSpecialEntityTraversingCriterion(Panels.specialEntityTraversingCriterionMap.get());
			bean.setSpecialViewHandlers(specialViewHandlersMap.get());
			bean.setWorkWithEntityExpert(Controllers.workWithEntityExpert.get());
			return bean;
		}
	};
	
	private static Supplier<Map<String, Supplier<? extends BiConsumer<Widget, ModelPath>>>> specialViewHandlersMap =
			new SingletonBeanProvider<Map<String, Supplier<? extends BiConsumer<Widget, ModelPath>>>>() {
		@Override
		public Map<String, Supplier<? extends BiConsumer<Widget, ModelPath>>> create() throws Exception {
			Map<String, Supplier<? extends BiConsumer<Widget, ModelPath>>> bean = new FastMap<>();
			bean.put(KnownUseCase.gimaUseCase.getDefaultValue(), gimaSpecialViewHandler);
			return bean;
		}
	};
	
	private static Supplier<GIMASpecialViewHandler> gimaSpecialViewHandler = new SingletonBeanProvider<GIMASpecialViewHandler>() {
		@Override
		public GIMASpecialViewHandler create() throws Exception {
			GIMASpecialViewHandler bean = new GIMASpecialViewHandler();
			return bean;
		}
	};

	private static Supplier<BrowsingConstellation> browsingConstellationWithActionBarProvider = new PrototypeBeanProvider<BrowsingConstellation>() {
		@Override
		public BrowsingConstellation create() throws Exception {
			BrowsingConstellation bean = browsingConstellationProvider.get();
			bean.setBrowsingConstellationActionBar(Panels.constellationActionBarProvider.get());
			bean.setBrowsingConstellationDefaultModelActions(Actions.constellationDefaultActions.get());
			return bean;
		}
	};
	
	protected static Supplier<MasterDetailConstellation> masterDetailConstellationProvider = new PrototypeBeanProvider<MasterDetailConstellation>() {
		@Override
		public MasterDetailConstellation create() throws Exception {
			MasterDetailConstellation bean = new MasterDetailConstellation();
			bean.setDefaultMasterViewProvider(Panels.assemblyPanelProvider);
			bean.setDetailViewSupplier(Panels.tabbedPropertyPanelProvider);
			bean.setExchangeContentViewAction(Actions.exchangeContentViewAction.get());
			//bean.setMaximizeViewAction(Actions.maximizeViewAction.get());
			return bean;
		}
	};
	
	protected static Supplier<MasterDetailConstellation> selectMasterDetailConstellationProvider = new PrototypeBeanProvider<MasterDetailConstellation>() {
		@Override
		public MasterDetailConstellation create() throws Exception {
			MasterDetailConstellation bean = new MasterDetailConstellation();
			bean.setDefaultMasterViewProvider(Panels.selectResultPanelProvider);
			bean.setDetailViewSupplier(Panels.tabbedPropertyPanelProvider);
			bean.setExchangeContentViewAction(Actions.selectExchangeContentViewAction.get());
			return bean;
		}
	};
	
	protected static Supplier<MasterDetailConstellation> gimaMasterDetailConstellationProvider = new PrototypeBeanProvider<MasterDetailConstellation>() {
		@Override
		public MasterDetailConstellation create() throws Exception {
			MasterDetailConstellation bean = new MasterDetailConstellation();
			bean.setDefaultMasterViewProvider(Panels.gimaAssemblyPanelProvider);
			bean.setShowDetailView(false);
			bean.setExchangeContentViewAction(Actions.gimaExchangeContentViewAction.get());
			return bean;
		}
	};
	
	protected static Supplier<MasterDetailConstellation> simpleMasterDetailConstellationProvider = new PrototypeBeanProvider<MasterDetailConstellation>() {
		@Override
		public MasterDetailConstellation create() throws Exception {
			MasterDetailConstellation bean = new MasterDetailConstellation();
			bean.setDefaultMasterViewProvider(Panels.hyperLinkContentViewPanelProvider);
			bean.setDetailViewSupplier(Panels.tabbedPropertyPanelProvider);
			bean.setExchangeContentViewAction(Actions.simpleExchangeContentViewAction.get());
			return bean;
		}
	};
	
	/*private static Supplier<ServiceRequestConstellation> serviceRequestConstellationProvider = new PrototypeBeanProvider<ServiceRequestConstellation>() {
		@Override
		public ServiceRequestConstellation create() throws Exception {
			ServiceRequestConstellation bean = new ServiceRequestConstellation();
			bean.setServiceRequestPanel(Panels.serviceRequestPanel.get());
			bean.setDefaultContentView(masterDetailConstellationProvider);
			bean.setGmViewActionBar(Panels.gmViewActionBar);
			bean.configureUseCase(Runtime.assemblyPanelUseCaseProvider.get());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			return bean;
		}
	};*/
	
	protected static Supplier<ServiceRequestExecutionConstellation> serviceRequestExecutionConstellationSupplier = new PrototypeBeanProvider<ServiceRequestExecutionConstellation>() {
		@Override
		public ServiceRequestExecutionConstellation create() throws Exception {
			ServiceRequestExecutionConstellation bean = new ServiceRequestExecutionConstellation();
			bean.setDefaultContentView(masterDetailConstellationProvider);
			bean.setGmViewActionBar(Panels.gmViewActionBar);
			bean.setServiceRequestPanelSupplier(Panels.serviceRequestExecutionPanel);
			bean.setViewSituationResolver(ViewSituationResolution.viewSituationResolver);
			return bean;
		}
	};
	
	protected static Supplier<QueryConstellation> queryConstellationProvider = new PrototypeBeanProvider<QueryConstellation>() {
		@Override
		public QueryConstellation create() throws Exception {
			QueryConstellation bean = new QueryConstellation();
			bean.setQueryProviderView(UiElements.queryModelEditorPanelProvider.get());
			bean.setDefaultContentView(masterDetailConstellationProvider);
			bean.configureUseCase(Runtime.assemblyPanelUseCaseProvider.get());
			bean.setViewSituationResolver(ViewSituationResolution.viewSituationResolver);
			bean.setGmViewActionBar(Panels.gmViewActionBar);
			bean.setShowQueryView(!Runtime.useGlobalSearchPanel);
			bean.setTransientSession(Session.transientManagedSession.get());
			bean.setTransientSessionSupplier(Session.prototypeTransientManagedSession);
			//bean.setRendererCodecsProvider(Panels.gmRendererCodecsProvider.get());
			return bean;
		}
	};
	
	private static Supplier<QueryConstellation> selectionQueryConstellationProvider = new PrototypeBeanProvider<QueryConstellation>() {
		@Override
		public QueryConstellation create() throws Exception {
			QueryConstellation bean = new QueryConstellation();
			bean.setQueryProviderView(UiElements.queryModelEditorPanelProvider.get());
			bean.setDefaultContentView(Panels.selectionAssemblyPanelProvider);
			bean.configureUseCase(Runtime.selectionUseCaseProvider.get());
			bean.setViewSituationResolver(ViewSituationResolution.viewSituationResolver);
			bean.setGmViewActionBar(Panels.gmViewActionBar);
			bean.setTransientSession(Session.transientManagedSession.get());
			bean.setTransientSessionSupplier(Session.prototypeTransientManagedSession);
			bean.setSpecialEntityTraversingCriterionMap(specialEntityTraversingCriterionMap.get());
			//bean.setRendererCodecsProvider(Panels.gmRendererCodecsProvider.get());
			//bean.setActions(Arrays.asList((ActionOrGroup)Actions.noActionExchangeContentViewAction.get()));			
			return bean;
		}
	};
	
	private static Supplier<Map<EntityType<?>, TraversingCriterion>> specialEntityTraversingCriterionMap = new SingletonBeanProvider<Map<EntityType<?>, TraversingCriterion>>() {
		@Override
		public Map<EntityType<?>, TraversingCriterion> create() throws Exception {
			Map<EntityType<?>, TraversingCriterion> bean = new HashMap<>();
			bean.put(GmProperty.T, TC.create().negation().joker().done());
			return bean;
		}
	};
	
	private static Supplier<QueryConstellation> saveQueryDialogWorkbenchSelectionQueryConstellationProvider = new PrototypeBeanProvider<QueryConstellation>() {
		@Override
		public QueryConstellation create() throws Exception {
			QueryConstellation bean = new QueryConstellation();
			bean.setQueryProviderView(UiElements.queryModelEditorPanelProvider.get());
			bean.setDefaultContentView(Panels.saveQueryDialogWorkbenchSelectionAssemblyPanelProvider);
			bean.configureUseCase(Runtime.selectionUseCaseProvider.get());
			bean.setViewSituationResolver(ViewSituationResolution.viewSituationResolver);
			bean.setGmViewActionBar(Panels.gmViewActionBar);
			bean.setTransientSession(Session.transientManagedSession.get());
			bean.setTransientSessionSupplier(Session.prototypeTransientManagedSession);
			return bean;
		}
	};
	
	private static Supplier<MasterDetailConstellation> readOnlyMasterDetailConstellationProvider = new PrototypeBeanProvider<MasterDetailConstellation>() {
		@Override
		public MasterDetailConstellation create() throws Exception {
			MasterDetailConstellation bean = new MasterDetailConstellation();
			bean.configureReadOnly(true);
			bean.setDefaultMasterViewProvider(Panels.readOnlyAssemblyPanelProvider);
			bean.setDetailViewSupplier(Panels.tabbedReadOnlyPropertyPanelProvider);
			bean.setExchangeContentViewAction(Actions.changesExchangeContentViewAction.get());
			//bean.setMaximizeViewAction(Actions.maximizeViewAction.get());
			return bean;
		}
	};
	
	private static Supplier<MasterDetailConstellation> clipboardMasterDetailConstellationProvider = new PrototypeBeanProvider<MasterDetailConstellation>() {
		@Override
		public MasterDetailConstellation create() throws Exception {
			MasterDetailConstellation bean = new MasterDetailConstellation();
			bean.configureReadOnly(true);
			bean.setDefaultMasterViewProvider(Panels.clipboardAssemblyPanelProvider);
			bean.setDetailViewSupplier(Panels.tabbedReadOnlyPropertyPanelProvider);
			bean.setExchangeContentViewAction(Actions.clipboardExchangeContentViewAction.get());
			return bean;
		}
	};
	
	private static Supplier<MasterDetailConstellation> changesSelectionMasterDetailConstellationProvider = new PrototypeBeanProvider<MasterDetailConstellation>() {
		@Override
		public MasterDetailConstellation create() throws Exception {
			MasterDetailConstellation bean = new MasterDetailConstellation();
			bean.configureReadOnly(true);
			bean.setDefaultMasterViewProvider(Panels.selectionAssemblyPanelProvider);
			bean.setExchangeContentViewAction(Actions.changesSelectionExchangeContentViewAction.get());
			bean.setShowDetailView(false);
			return bean;
		}
	};
	
	private static Supplier<MasterDetailConstellation> selectionMasterDetailConstellationProvider = new PrototypeBeanProvider<MasterDetailConstellation>() {
		@Override
		public MasterDetailConstellation create() throws Exception {
			MasterDetailConstellation bean = new MasterDetailConstellation();
			bean.configureReadOnly(true);
			bean.setDefaultMasterViewProvider(Panels.selectionAssemblyPanelProvider);
			bean.setExchangeContentViewAction(Actions.selectionExchangeContentViewAction.get());
			bean.setShowDetailView(false);
			return bean;
		}
	};
	
	private static Supplier<MasterDetailConstellation> localModeMasterDetailConstellationProvider = new PrototypeBeanProvider<MasterDetailConstellation>() {
		@Override
		public MasterDetailConstellation create() throws Exception {
			MasterDetailConstellation bean = new MasterDetailConstellation();
			bean.configureReadOnly(true);
			bean.setDefaultMasterViewProvider(Panels.localModeAssemblyPanelProvider);
			bean.setDetailViewSupplier(Panels.tabbedLocalModePropertyPanelProvider);
			bean.setExchangeContentViewAction(Actions.localModeExchangeContentViewAction.get());
			return bean;
		}
	};
	
	protected static Supplier<CancelGlobalActionsToolBar> cancelGlobalActionsToolBar = new PrototypeBeanProvider<CancelGlobalActionsToolBar>() {
		@Override
		public CancelGlobalActionsToolBar create() throws Exception {
			CancelGlobalActionsToolBar bean = new CancelGlobalActionsToolBar();
			return bean;
		}
	};

	protected static Supplier<ValidationConstellation> validationConstellationProvider = new SessionScopedBeanProvider<ValidationConstellation>() {
		@Override
		public ValidationConstellation create() throws Exception {
			ValidationConstellation bean = publish(new ValidationConstellation());
			bean.setValidationLogListPanelProvider(Panels.validationLogListPanel);
			return bean;
		}
	};	
	
	protected static Supplier<HomeConstellation> homeConstellationProvider = new SessionScopedBeanProvider<HomeConstellation>() {
		@Override
		public HomeConstellation create() throws Exception {
			HomeConstellation bean = publish(new HomeConstellation());
			bean.setGmListViewProvider(Panels.thumbnailPanelProvider);
			bean.setDataSession(Session.persistenceSession.get());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			bean.setHomeFoldersProvider(homeFoldersProvider.get());
			bean.setUserFullNameProvider(Providers.userFullNameProvider.get());
			bean.setEmptyTextMessage("");
			bean.setBrowsingConstellationProvider(browsingConstellationProvider);
			return bean;
		}
	};
	
	private static Supplier<HomeFoldersLoader> homeFoldersProvider = new SessionScopedBeanProvider<HomeFoldersLoader>() {
		@Override
		public HomeFoldersLoader create() throws Exception {
			HomeFoldersLoader bean = publish(new HomeFoldersLoader());
			bean.setGmSession(Session.workbenchPersistenceSession.get());
			bean.setWorkbenchPerspectiveFutureProvider(Providers.workbenchDataProvider.get());
			return bean;
		}
	};
	
	private static Supplier<HomeConstellation> selectionHomeConstellationProvider = new SelectionConstellationScopedBeanProvider<HomeConstellation>() {
		@Override
		public HomeConstellation create() throws Exception {
			HomeConstellation bean = publish(new HomeConstellation());
			bean.setDataSession(Session.persistenceSession.get());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			bean.setHomeFoldersProvider(homeFoldersProvider.get());
			bean.setGmListViewProvider(Panels.selectionThumbnailPanelProvider);
			bean.setUserFullNameProvider(Providers.userFullNameProvider.get());
			
			Scheduler.get().scheduleDeferred(() -> customizationConstellationProvider.get().addModelEnvironmentSetListener(bean));
			return bean;
		}
	};
	
	private static Supplier<Set<Class<?>>> ignoreTypes = new SingletonBeanProvider<Set<Class<?>>>() {
		@Override
		public Set<Class<?>> create() throws Exception {
			Set<Class<?>> bean = new HashSet<Class<?>>();
			bean.add(LocalizedString.class);
			return bean;
		}
	};
	
	private static Supplier<ChangesConstellation> changesConstellationProvider = new PrototypeBeanProvider<ChangesConstellation>() {
		@Override
		public ChangesConstellation create() throws Exception {
			ChangesConstellation bean = new ChangesConstellation();
			bean.setMasterDetailConstellationProvider(readOnlyMasterDetailConstellationProvider);
			bean.setIgnoreTypes(ignoreTypes.get());
			bean.setVerticalTabActionBar(Panels.constellationActionBarProvider.get());
			bean.setActionManager(Controllers.actionManager.get());
			return bean;
		}
	};
	
	protected static Supplier<ClipboardConstellationProvider> clipboardConstellationProviderProvider = new SessionScopedBeanProvider<ClipboardConstellationProvider>() {
		@Override
		public ClipboardConstellationProvider create() throws Exception {
			ClipboardConstellationProvider bean = publish(new ClipboardConstellationProvider());
			bean.setVerticalTabActionBar(Panels.constellationActionBarProvider.get());
			bean.setActionManagerProvider(Controllers.actionManager);
			return bean;
		}
	};
	
	private static Supplier<ChangesConstellation> changesConstellationSelectionProvider = new SelectionConstellationScopedBeanProvider<ChangesConstellation>() {
		@Override
		public ChangesConstellation create() throws Exception {
			ChangesConstellation bean = new ChangesConstellation();
			bean.setMasterDetailConstellationProvider(changesSelectionMasterDetailConstellationProvider);
			bean.setIgnoreTypes(ignoreTypes.get());
			return bean;
		}
	};
	
	protected static Supplier<SelectionConstellation> selectionConstellationProvider = new SelectionConstellationScopedBeanProvider<SelectionConstellation>() {
		@Override
		public SelectionConstellation create() throws Exception {
			SelectionConstellation bean = publish(new SelectionConstellation());
			//bean.setWorkbench(Panels.selectionWorkbenchProvider.get());
			bean.setVerticalTabPanel(Panels.verticalTabPanelProvider.get());
			bean.setHomeConstellation(selectionHomeConstellationProvider);
			bean.setChangesConstellation(changesConstellationSelectionProvider);
			bean.setUseCase(Runtime.selectionUseCaseProvider.get());
			bean.setBrowsingConstellationProvider(browsingConstellationProvider);
			bean.setQueryConstellationProvider(selectionQueryConstellationProvider);
			bean.setMasterDetailConstellationProvider(selectionMasterDetailConstellationProvider);
			bean.setClipboardConstellationProvider(clipboardConstellationProviderProvider.get());
			bean.setWorkbenchActionHandlerRegistry(Actions.workbenchActionHandlerRegistry.get());
			bean.setSpotlightPanel(Panels.spotlightPanelWithoutTypesProvider.get());
			bean.setTemplateEvaluationDialogProvider(Panels.templateEvaluationDialogProvider);
			bean.setCodecRegistry(Codecs.renderersCodecRegistry.get());
			bean.setDetailPanelProvider(Panels.tabbedSelectionPropertyPanelProvider);
			bean.setTransientGmSession(Session.transientManagedSession.get());
			bean.setGmSession(Session.persistenceSession.get());
			bean.setParserWithPossibleValuesSupplier(Providers.parserWithPossibleValues);
			bean.setExpertUIMap(Panels.expertUIMapProvider.get());
			return bean;
		}
	};
	
	protected static Supplier<SelectionConstellation> saveQueryDialogWorkbenchSelectionConstellationProvider = new SelectionConstellationScopedBeanProvider<SelectionConstellation>() {
		@Override
		public SelectionConstellation create() throws Exception {
			SelectionConstellation bean = publish(new SelectionConstellation());
			//bean.setWorkbench(Panels.selectionWorkbenchProvider.get());
			bean.setVerticalTabPanel(Panels.verticalTabPanelProvider.get());
			bean.setHomeConstellation(selectionHomeConstellationProvider);
			bean.setChangesConstellation(changesConstellationSelectionProvider);
			bean.setUseCase(Runtime.selectionUseCaseProvider.get());
			bean.setBrowsingConstellationProvider(browsingConstellationProvider);
			bean.setQueryConstellationProvider(saveQueryDialogWorkbenchSelectionQueryConstellationProvider);
			bean.setMasterDetailConstellationProvider(selectionMasterDetailConstellationProvider);
			bean.setClipboardConstellationProvider(clipboardConstellationProviderProvider.get());
			bean.setWorkbenchActionHandlerRegistry(Actions.workbenchActionHandlerRegistry.get());
			//bean.setSpotlightPanel(Panels.spotlightPanelProvider.get());
			bean.setSpotlightPanel(Panels.workbenchSpotlightPanelProvider.get());
			bean.setTemplateEvaluationDialogProvider(Panels.templateEvaluationDialogProvider);
			bean.setCodecRegistry(Codecs.renderersCodecRegistry.get());
			bean.setDetailPanelProvider(Panels.tabbedSelectionPropertyPanelProvider);
			bean.setTransientGmSession(Session.transientManagedSession.get());
			bean.setGmSession(Session.workbenchPersistenceSession.get());
			return bean;
		}
	};
	
	protected static Supplier<BrowsingConstellationDialog> browsingConstellationDialog = new PrototypeBeanProvider<BrowsingConstellationDialog>() {
		@Override
		public BrowsingConstellationDialog create() throws Exception {
			BrowsingConstellationDialog bean = new BrowsingConstellationDialog();
			bean.setBrowsingConstellationProvider(browsingConstellationProvider);
			bean.setLocalModeMasterDetailConstellationProvider(localModeMasterDetailConstellationProvider);
			bean.setGmSession(Session.persistenceSession.get());
			return bean;
		}
	};

	protected static Supplier<GlobalActionsHandler> abstractGlobalActionsHandler = new PrototypeBeanProvider<GlobalActionsHandler>() {
		@Override
		public GlobalActionsHandler create() throws Exception {
			GlobalActionsHandler bean = new GlobalActionsHandler();			
			bean.setWorkbenchActionHandlerRegistry(Actions.workbenchActionHandlerRegistry.get());
			bean.setSaveAction(Actions.saveAction.get());
			bean.setRedoAction(Actions.redoAction.get());
			bean.setTransientRedoAction(Actions.transientRedoAction.get());
			bean.setUndoAction(Actions.undoAction.get());
			bean.setAdvancedSaveAction(Actions.advancedSaveAction.get());
			bean.setTransientUndoAction(Actions.transientUndoAction.get());
			bean.setGmSession(Session.persistenceSession.get());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			bean.setInstantiateEntityActionProvider(Actions.instantiateEntityAction);
			bean.setTransientInstantiateEntityActionProvider(Actions.instantiateTransientEntityAction);
			bean.setGloabalActionsToolBarLoaderManeger(Controllers.globalActionsToolBarManager.get());
			bean.setUseCase(Runtime.globalActionsUseCaseProvider.get());
			bean.setExternalToolBarActions(Arrays.asList(Actions.showUploadDialogAction.get()));
			bean.setActionFolderContentExpert(Actions.actionFolderContentExpert.get());
			return bean;
		}
	};
	
	protected static Supplier<GlobalActionsHandler> globalActionsHandler = new PrototypeBeanProvider<GlobalActionsHandler>() {
		@Override
		public GlobalActionsHandler create() throws Exception {
			GlobalActionsHandler bean = abstractGlobalActionsHandler.get();
			bean.setUseShortcuts(true);
			return bean;
		}
	};	
	
	protected static Supplier<GlobalActionPanel> globalActionPanel = new PrototypeBeanProvider<GlobalActionPanel>() {
		@Override
		public GlobalActionPanel create() throws Exception {
			GlobalActionPanel bean = new GlobalActionPanel();			
			bean.setGlobalActionsHandler(abstractGlobalActionsHandler.get());
			return bean;
		}
	};	

	protected static Supplier<GlobalActionPanel> explroerGlobalActionPanel = new PrototypeBeanProvider<GlobalActionPanel>() {
		@Override
		public GlobalActionPanel create() throws Exception {
			GlobalActionPanel bean = new GlobalActionPanel();			
			bean.setGlobalActionsHandler(globalActionsHandler.get());
			return bean;
		}
	};	
	
	protected static Supplier<GlobalActionsToolBar> abstractGlobalActionsToolBar = new PrototypeBeanProvider<GlobalActionsToolBar>() {
		@Override
		public GlobalActionsToolBar create() throws Exception {
			GlobalActionsToolBar bean = new GlobalActionsToolBar();			
			bean.setGlobalActionsHandler(abstractGlobalActionsHandler.get());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			return bean;
		}
	};
	
	protected static Supplier<GlobalActionsToolBar> globalActionsToolBar = new SessionScopedBeanProvider<GlobalActionsToolBar>() {
		@Override
		public GlobalActionsToolBar create() throws Exception {
			GlobalActionsToolBar bean = publish(abstractGlobalActionsToolBar.get());			
			return bean;
		}
	};

	protected static Supplier<GlobalActionsToolBar> explorerGlobalActionsToolBar = new SessionScopedBeanProvider<GlobalActionsToolBar>() {
		@Override
		public GlobalActionsToolBar create() throws Exception {
			GlobalActionsToolBar bean = publish(abstractGlobalActionsToolBar.get());	
			bean.setButtonScale(ButtonScale.SMALL);
			bean.setIconAlign(IconAlign.LEFT);
			bean.setUseButtonText(false);
			return bean;
		}
	};
	
	protected static Supplier<MasterDetailConstellation> errorMasterViewConstellationSupplier = new PrototypeBeanProvider<MasterDetailConstellation>() {
		@Override
		public MasterDetailConstellation create() throws Exception {
			MasterDetailConstellation bean = new MasterDetailConstellation();
			bean.configureReadOnly(true);
			bean.setDefaultMasterViewProvider(Panels.errorAssemblyPanelProvider);
			bean.setDetailViewSupplier(Panels.errorPropertyPanelProvider);
			bean.setExchangeContentViewAction(null);
			bean.configureGmSession(Session.transientManagedSession.get());
			bean.setColumnData(errorColumnData.get());
			return bean;
		}
	};
	
	private static Supplier<ColumnData> errorColumnData = new SingletonBeanProvider<ColumnData>() {
		@Override
		public ColumnData create() throws Exception {
			ColumnData bean = ColumnData.T.create();
			bean.setDisplayNode(true);
			bean.setNodeWidth(200);
			bean.setNodeTitle(I18nTools.createLs(LocalizedText.INSTANCE.error()));
			
			StorageColumnInfo textColumn = StorageColumnInfo.T.create();
			textColumn.setPath(Reason.text);
			textColumn.setWidth(100);
			textColumn.setTitle(I18nTools.createLs(LocalizedText.INSTANCE.message()));
			textColumn.setAutoExpand(true);
			
			List<StorageColumnInfo> displayPaths = new ArrayList<>();
			displayPaths.add(textColumn);
			bean.setDisplayPaths(displayPaths);
			return bean;
		}
	};
	
//	protected static Supplier<ExplorerConstellation> toolBarLessExplorerConstellationProvider2 = new SessionScopedBeanProvider<ExplorerConstellation>() {
//	{
//		attach(Controllers.toolBarLessExplorerWorkbenchController);
//	} 
//	@Override
//	public ExplorerConstellation create() throws Exception {
//		ExplorerConstellation bean = publish(new ExplorerConstellation());
//		bean.setWorkbench(Panels.workbenchProvider.get());
//		bean.setVerticalTabPanel(Panels.verticalTabPanelProvider.get());
//		bean.setHomeConstellation(homeConstellationProvider.get());
//		bean.setChangesConstellation(changesConstellationProvider.get());
//		bean.setGmSession(Session.persistenceSession.get());
//		bean.setTransientGmSession(Session.transientManagedSession.get());
//		bean.setUseCase(Runtime.assemblyPanelUseCaseProvider.get());
//		bean.setBrowsingConstellationProvider(browsingConstellationProvider);
//		bean.setViewSituationResolver(ViewSituationResolution.explorerViewSituationResolver.get());
//		bean.setMasterDetailConstellationProvider(masterDetailConstellationProvider);
//		bean.setReadOnlyMasterDetailConstellationProvider(clipboardMasterDetailConstellationProvider);
//		bean.setClipboardConstellationProvider(clipboardConstellationProviderProvider.get());
//		bean.setGIMADialogProvider(UiElements.gimaDialogProvider);
//		bean.setFieldDialogOpenerActions(fieldDialogOpenerActionsMap.get());
//		//bean.setTemplateEvaluationPanelProvider(Panels.templateEvaluationPanelProvider);
//		bean.setTemplateEvaluationDialogProvider(Panels.templateEvaluationDialogProvider);
//		bean.setValidationLogRepresenationLogProvider(Panels.validationLogListPanel);
//		bean.setValidation(Panels.validation.get());
//		bean.setWorkbenchActionHandlerRegistry(Actions.workbenchActionHandlerRegistry.get());
//		bean.setUseWorkbenchWithinTab(Boolean.parseBoolean(Runtime.useWorkbenchWithinTab.get()));
//		bean.setUseClipboardConstellation(true);
////		bean.setGmViewActionBar(Panels.gmViewActionBar.get());
//		bean.setServiceRequestConstellationProvider(Constellations.serviceRequestConstellationProvider);
//		bean.setUseToolBar(false);
//		return bean;
//	}
//};
	
	/*private static Supplier<MasterDetailConstellation> hiddenActionMasterDetailConstellationProvider = new PrototypeBeanProvider<MasterDetailConstellation>() {
		public MasterDetailConstellation create() throws Exception {
			MasterDetailConstellation bean = new MasterDetailConstellation();
			bean.setDefaultMasterViewProvider(Panels.hiddenActionsAssemblyPanelProvider);
			bean.setViewSituationResolver(ViewResolution.masterDetailViewSituationResolver.get());
			bean.setDetailView(Panels.propertyPanelProvider.get());
			bean.setExchangeContentViewAction(Actions.hiddenActionsExchangeContentViewAction.get());
			return bean;
		}
	};*/

}
