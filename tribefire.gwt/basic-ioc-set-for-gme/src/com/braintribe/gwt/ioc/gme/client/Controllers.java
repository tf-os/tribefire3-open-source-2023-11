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
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.gwt.customizationui.client.security.SessionController;
import com.braintribe.gwt.gme.constellation.client.GmEditionViewControllerImpl;
import com.braintribe.gwt.gme.constellation.client.action.GlobalActionsToolBarManager;
import com.braintribe.gwt.gme.constellation.client.expert.ConnectionController;
import com.braintribe.gwt.gme.constellation.client.expert.ExternalModuleInitializer;
import com.braintribe.gwt.gme.constellation.client.expert.ModelEnvironmentDrivenSessionUpdater;
import com.braintribe.gwt.gme.constellation.client.expert.SessionReadyLoader;
import com.braintribe.gwt.gme.constellation.client.js.JsUxModuleInitializer;
import com.braintribe.gwt.gme.headerbar.client.action.DefaultHeaderBarManager;
import com.braintribe.gwt.gme.verticaltabpanel.client.action.DefaultVerticalTabManager;
import com.braintribe.gwt.gme.websocket.client.WebSocketExpert;
import com.braintribe.gwt.gmview.action.client.DefaultGmContentViewActionManager;
import com.braintribe.gwt.gmview.action.client.WorkWithEntityExpert;
import com.braintribe.gwt.gmview.actionbar.client.ModelEnvironmentFolderLoader;
import com.braintribe.gwt.gmview.client.GmEditionViewController;
import com.braintribe.gwt.gmview.ddsarequest.client.confirmation.ConfirmationExpert;
import com.braintribe.gwt.gmview.ddsarequest.client.message.MessageExpert;
import com.braintribe.gwt.ioc.gme.client.expert.ExplorerWorkbenchController;
import com.braintribe.gwt.ioc.gme.client.expert.LogoutController;
import com.braintribe.gwt.security.client.SessionScopedBeanProvider;
import com.braintribe.model.notification.Notify;
import com.braintribe.model.processing.service.impl.ConfigurableDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.ConfigurableServiceRequestEvaluator;
import com.braintribe.model.workbench.KnownWorkenchPerspective;
import com.braintribe.provider.PrototypeBeanProvider;
import com.braintribe.provider.SingletonBeanProvider;

/**
 * This is the IoC configuration for Controllers.
 * 
 */
class Controllers {

	protected static Supplier<SessionController> sessionController = new SingletonBeanProvider<SessionController>() {
		@Override
		public SessionController create() throws Exception {
			SessionController bean = publish(new SessionController());
			bean.setMainPanelProvider(Runtime.mainPanelProvider);
			bean.setHandleInitializationUI(Runtime.handleInitializationUI);
			bean.setSessionCreatedProviders(sessionCreatedProviders.get());
			bean.setExternalModuleInitializerSupplier(externalModuleInitializer);
			Services.securityService.get().addSessionListener(bean);
			ConfirmationExpert.setStaticConfirmationDialogSupplier(Notifications.staticConfirmationDialog);
			ConfirmationExpert.setDynamicConfirmationDialogSupplier(Notifications.dynamicConfirmationDialog);
			MessageExpert.setMessageDialogSupplier(Notifications.messageDialog);
			return bean;
		}
	};
	
	private static Supplier<ExternalModuleInitializer> externalModuleInitializer = new SingletonBeanProvider<ExternalModuleInitializer>() {
		@Override
		public ExternalModuleInitializer create() throws Exception {
			ExternalModuleInitializer bean = publish(new ExternalModuleInitializer());
			bean.setAccessId(Runtime.accessId.get());
			bean.setModelEnvironmentProvider(Providers.modelEnvironmentProvider.get());
			bean.setTransientSession(Session.transientManagedSession.get());
			bean.setPersistenceSession(Session.persistenceSession.get());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			bean.setModelEnvironmentDrivenSessionUpdater(modelEnvironmentDrivenSessionUpdater.get());
			return bean;
		}
	};

	private static Supplier<List<Supplier<?>>> sessionCreatedProviders = new SingletonBeanProvider<List<Supplier<?>>>() {
		@Override
		public List<Supplier<?>> create() throws Exception {
			List<Supplier<?>> bean = publish(new ArrayList<Supplier<?>>());
			if (Runtime.useNotififactionPoll)
				bean.add(Notification.gmUrlNotificationPoll);
			return bean;
		}
	};

	protected static Supplier<LogoutController> logoutController = new SingletonBeanProvider<LogoutController>() {
		@Override
		public LogoutController create() throws Exception {
			LogoutController bean = publish(new LogoutController());
			if (Runtime.useCommit)
				bean.setSaveActionProvider(Actions.saveAction);
			return bean;
		}
	};

	private static Supplier<DefaultHeaderBarManager> headerBarManager = new SessionScopedBeanProvider<DefaultHeaderBarManager>() {
		@Override
		public DefaultHeaderBarManager create() throws Exception {
			DefaultHeaderBarManager bean = publish(new DefaultHeaderBarManager());
			bean.setPersistenceSession(Session.persistenceSession.get());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			bean.setHeaderBar(UiElements.defaulHeaderBar);
			bean.setFolderLoader(headerBarFolderLoader.get());
			return bean;
		}
	};

	protected static Supplier<GlobalActionsToolBarManager> globalActionsToolBarManager = new SingletonBeanProvider<GlobalActionsToolBarManager>() {
		@Override
		public GlobalActionsToolBarManager create() throws Exception {
			GlobalActionsToolBarManager bean = publish(new GlobalActionsToolBarManager());
			bean.setPersistenceSession(Session.persistenceSession.get());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			// bean.setGlobalActionsToolBar(Constellations.globalActionsToolBar.get());
			bean.setFolderLoader(globalActionsToolBarFolderLoader.get());
			return bean;
		}
	};

	// protected static Supplier<DefaultVerticalTabManager> verticalTabManager = new
	// SessionScopedBeanProvider<DefaultVerticalTabManager>() {
	protected static Supplier<DefaultVerticalTabManager> verticalTabManager = new SingletonBeanProvider<DefaultVerticalTabManager>() {
		@Override
		public DefaultVerticalTabManager create() throws Exception {
			DefaultVerticalTabManager bean = publish(new DefaultVerticalTabManager());
			bean.setPersistenceSession(Session.persistenceSession.get());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			// bean.setVerticalTabPanel(Panels.verticalTabPanelProvider.get());
			bean.setFolderLoader(verticalTabFolderLoader.get());
			return bean;
		}
	};

	protected static Supplier<DefaultVerticalTabManager> browsingConstellationActionBarManager = new SingletonBeanProvider<DefaultVerticalTabManager>() {
		@Override
		public DefaultVerticalTabManager create() throws Exception {
			DefaultVerticalTabManager bean = publish(new DefaultVerticalTabManager());
			bean.setPersistenceSession(Session.persistenceSession.get());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			// bean.setVerticalTabPanel(Panels.verticalTabPanelProvider.get());
			bean.setFolderLoader(browsingConstellationActionbarFolderLoader.get());
			return bean;
		}
	};

	private static Supplier<ModelEnvironmentFolderLoader> headerBarFolderLoader = new SessionScopedBeanProvider<ModelEnvironmentFolderLoader>() {
		@Override
		public ModelEnvironmentFolderLoader create() throws Exception {
			ModelEnvironmentFolderLoader bean = publish(new ModelEnvironmentFolderLoader());
			bean.setKnownWorkbenchPerspective(KnownWorkenchPerspective.headerBar);
			bean.setWorkbenchPerspectiveFutureProvider(Providers.workbenchDataProvider.get());
			return bean;
		}
	};

	private static Supplier<ModelEnvironmentFolderLoader> verticalTabFolderLoader = new SessionScopedBeanProvider<ModelEnvironmentFolderLoader>() {
		@Override
		public ModelEnvironmentFolderLoader create() throws Exception {
			ModelEnvironmentFolderLoader bean = publish(new ModelEnvironmentFolderLoader());
			bean.setKnownWorkbenchPerspective(KnownWorkenchPerspective.tabActionBar);
			bean.setWorkbenchPerspectiveFutureProvider(Providers.workbenchDataProvider.get());
			return bean;
		}
	};

	private static Supplier<ModelEnvironmentFolderLoader> browsingConstellationActionbarFolderLoader = new SessionScopedBeanProvider<ModelEnvironmentFolderLoader>() {
		@Override
		public ModelEnvironmentFolderLoader create() throws Exception {
			ModelEnvironmentFolderLoader bean = publish(new ModelEnvironmentFolderLoader());
			bean.setKnownWorkbenchPerspective(KnownWorkenchPerspective.viewActionBar);
			bean.setWorkbenchPerspectiveFutureProvider(Providers.workbenchDataProvider.get());
			return bean;
		}
	};

	private static Supplier<ModelEnvironmentFolderLoader> globalActionsToolBarFolderLoader = new SessionScopedBeanProvider<ModelEnvironmentFolderLoader>() {
		@Override
		public ModelEnvironmentFolderLoader create() throws Exception {
			ModelEnvironmentFolderLoader bean = publish(new ModelEnvironmentFolderLoader());
			bean.setKnownWorkbenchPerspective(KnownWorkenchPerspective.globalActionBar);
			bean.setWorkbenchPerspectiveFutureProvider(Providers.workbenchDataProvider.get());
			return bean;
		}
	};

	public static Supplier<ModelEnvironmentFolderLoader> webReaderFolderLoader = new SessionScopedBeanProvider<ModelEnvironmentFolderLoader>() {
		@Override
		public ModelEnvironmentFolderLoader create() throws Exception {
			ModelEnvironmentFolderLoader bean = publish(new ModelEnvironmentFolderLoader());
			bean.setKnownWorkbenchPerspective(KnownWorkenchPerspective.webReader);
			bean.setWorkbenchPerspectiveFutureProvider(Providers.workbenchDataProvider.get());
			return bean;
		}
	};

	public static Supplier<ModelEnvironmentFolderLoader> webReaderHeaderBarFolderLoader = new SessionScopedBeanProvider<ModelEnvironmentFolderLoader>() {
		@Override
		public ModelEnvironmentFolderLoader create() throws Exception {
			ModelEnvironmentFolderLoader bean = publish(new ModelEnvironmentFolderLoader());
			bean.setKnownWorkbenchPerspective(KnownWorkenchPerspective.webReaderHeaderbar);
			bean.setWorkbenchPerspectiveFutureProvider(Providers.workbenchDataProvider.get());
			return bean;
		}
	};

	private static Supplier<DefaultGmContentViewActionManager> abstractActionManager = new PrototypeBeanProvider<DefaultGmContentViewActionManager>() {
		{
			setAbstract(true);
		}

		@Override
		public DefaultGmContentViewActionManager create() throws Exception {
			DefaultGmContentViewActionManager bean = new DefaultGmContentViewActionManager();
			bean.setInstanceSelectionFutureProvider(Panels.gimaSelectionConstellationSupplier);
			bean.setActionMenuBuilder(UiElements.defaultActionMenuBuilder);
			bean.setViewSituationResolver(ViewSituationResolution.viewSituationResolver);
			bean.setExternalContentViewContexts(Actions.contentViewContexts.get());
			bean.setFolderLoader(actionBarFolderLoader.get());
			bean.setWorkWithEntityExpert(workWithEntityExpert.get());
			return bean;
		}
	};

	protected static Supplier<DefaultGmContentViewActionManager> standAloneWebReaderActionManager = new SessionScopedBeanProvider<DefaultGmContentViewActionManager>() {
		@Override
		public DefaultGmContentViewActionManager create() throws Exception {
			DefaultGmContentViewActionManager bean = new DefaultGmContentViewActionManager();
			bean.setPrepareKnownActions(false);
			bean.setInstanceSelectionFutureProvider(Panels.gimaSelectionConstellationSupplier);
			bean.setActionMenuBuilder(UiElements.defaultActionMenuBuilder);
			bean.setViewSituationResolver(ViewSituationResolution.viewSituationResolver);
			bean.setExternalContentViewContexts(Actions.contentViewContexts.get());
			bean.setFolderLoader(actionBarFolderLoader.get());
			// bean.setWorkbenchActionHandlerRegistry(WebReaderIoc.standAloneWorkbenchActionHandlerRegistry.get());
			bean.setPersistenceSession(Session.persistenceSession.get());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			bean.setExternalActionProviders(Actions.externalActionProviders.get());
			// bean.setObjectAssignmentActionDialogProvider(UiElements.objectAssignmentActionDialogProvider);
			bean.setInstantiationActionsProvider(Providers.entityTypeInstantiationActionsProvider.get());
			return bean;
		}
	};
	
	protected static Supplier<DefaultGmContentViewActionManager> webReaderActionManager = new SessionScopedBeanProvider<DefaultGmContentViewActionManager>() {
		@Override
		public DefaultGmContentViewActionManager create() throws Exception {
			DefaultGmContentViewActionManager bean = new DefaultGmContentViewActionManager();
			bean.setPrepareKnownActions(false);
			bean.setInstanceSelectionFutureProvider(Panels.gimaSelectionConstellationSupplier);
			bean.setActionMenuBuilder(UiElements.defaultActionMenuBuilder);
			bean.setViewSituationResolver(ViewSituationResolution.viewSituationResolver);
			bean.setExternalContentViewContexts(Actions.contentViewContexts.get());
			bean.setFolderLoader(actionBarFolderLoader.get());
			bean.setPersistenceSession(Session.persistenceSession.get());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			bean.setExternalActionProviders(Actions.externalActionProviders.get());
			return bean;
		}
	};

	protected static Supplier<DefaultGmContentViewActionManager> actionManager = new SessionScopedBeanProvider<DefaultGmContentViewActionManager>() {
		@Override
		public DefaultGmContentViewActionManager create() throws Exception {
			DefaultGmContentViewActionManager bean = publish(abstractActionManager.get());
			bean.setPersistenceSession(Session.persistenceSession.get());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			bean.setExternalActionProviders(Actions.externalActionProviders.get());
			// bean.setObjectAssignmentActionDialogProvider(UiElements.objectAssignmentActionDialogProvider);
			bean.addActionPeformanceListener(Constellations.explorerConstellationProvider.get());
			bean.setInstantiationActionHandler(Constellations.explorerConstellationProvider.get());
			bean.setInstantiationActionsProvider(Providers.entityTypeInstantiationActionsProvider.get());
			return bean;
		}
	};

	protected static Supplier<DefaultGmContentViewActionManager> modellerActionManager = new SingletonBeanProvider<DefaultGmContentViewActionManager>() {
		@Override
		public DefaultGmContentViewActionManager create() throws Exception {
			DefaultGmContentViewActionManager bean = publish(abstractActionManager.get());
			bean.setPersistenceSession(Session.persistenceSession.get());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			bean.setExternalActionProviders(Actions.externalActionProviders.get());
			bean.setPrepareDeleteEntityAction(false);
			bean.addActionPeformanceListener(Constellations.explorerConstellationProvider.get());
			bean.setInstantiationActionHandler(Constellations.explorerConstellationProvider.get());
			bean.setInstantiationActionsProvider(Providers.entityTypeInstantiationActionsProvider.get());
			return bean;
		}
	};

	protected static Supplier<DefaultGmContentViewActionManager> standAloneModellerActionManager = new SingletonBeanProvider<DefaultGmContentViewActionManager>() {
		@Override
		public DefaultGmContentViewActionManager create() throws Exception {
			DefaultGmContentViewActionManager bean = publish(new DefaultGmContentViewActionManager());
			bean.setActionMenuBuilder(UiElements.modelerActionMenuBuilder);
			bean.setFolderLoader(actionBarFolderLoader.get());
			bean.setWorkWithEntityExpert(workWithEntityExpert.get());
			bean.setPersistenceSession(Session.persistenceSession.get());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			bean.setPrepareDeleteEntityAction(false);
			bean.setPrepareGimaOpenerAction(false);
			return bean;
		}
	};

	protected static Supplier<ModelEnvironmentFolderLoader> actionBarFolderLoader = new SessionScopedBeanProvider<ModelEnvironmentFolderLoader>() {
		@Override
		public ModelEnvironmentFolderLoader create() throws Exception {
			ModelEnvironmentFolderLoader bean = publish(new ModelEnvironmentFolderLoader());
			bean.setKnownWorkbenchPerspective(KnownWorkenchPerspective.actionBar);
			bean.setWorkbenchPerspectiveFutureProvider(Providers.workbenchDataProvider.get());
			return bean;
		}
	};
	
	protected static Supplier<ModelEnvironmentFolderLoader> gimaActionBarFolderLoader = new SessionScopedBeanProvider<ModelEnvironmentFolderLoader>() {
		@Override
		public ModelEnvironmentFolderLoader create() throws Exception {
			ModelEnvironmentFolderLoader bean = publish(new ModelEnvironmentFolderLoader());
			bean.setKnownWorkbenchPerspective(KnownWorkenchPerspective.gimaActionBar);
			bean.setWorkbenchPerspectiveFutureProvider(Providers.workbenchDataProvider.get());
			return bean;
		}
	};

	protected static Supplier<DefaultGmContentViewActionManager> gimaActionManager = new SessionScopedBeanProvider<DefaultGmContentViewActionManager>() {
		@Override
		public DefaultGmContentViewActionManager create() throws Exception {
			DefaultGmContentViewActionManager bean = publish(abstractActionManager.get());
			bean.setFolderLoaders(Arrays.asList(gimaActionBarFolderLoader.get(), actionBarFolderLoader.get()));
			bean.setActionMenuBuilder(UiElements.gimaActionMenuBuilder);
			bean.setPersistenceSession(Session.persistenceSession.get());
			bean.setPrepareGimaOpenerAction(false);
			bean.setPrepareInstantiateEntityAction(false);
			bean.setExternalActionProviders(Actions.externalActionProviders.get());
			bean.addActionPeformanceListener(Constellations.explorerConstellationProvider.get());
			bean.setInstantiationActionHandler(Constellations.explorerConstellationProvider.get());
			bean.setInstantiationActionsProvider(Providers.entityTypeInstantiationActionsProvider.get());
			return bean;
		}
	};

	protected static Supplier<DefaultGmContentViewActionManager> readOnlyActionManager = new SessionScopedBeanProvider<DefaultGmContentViewActionManager>() {
		@Override
		public DefaultGmContentViewActionManager create() throws Exception {
			DefaultGmContentViewActionManager bean = publish(abstractActionManager.get());
			bean.setPersistenceSession(Session.persistenceSession.get());
			bean.setReadOnlyMode(true);
			bean.setExternalActionProviders(Actions.externalActionProviders.get());
			bean.addActionPeformanceListener(Constellations.explorerConstellationProvider.get());
			bean.setInstantiationActionHandler(Constellations.explorerConstellationProvider.get());
			bean.setInstantiationActionsProvider(Providers.entityTypeInstantiationActionsProvider.get());
			return bean;
		}
	};

	protected static Supplier<DefaultGmContentViewActionManager> propertyPanelActionManager = new SessionScopedBeanProvider<DefaultGmContentViewActionManager>() {
		@Override
		public DefaultGmContentViewActionManager create() throws Exception {
			DefaultGmContentViewActionManager bean = publish(abstractActionManager.get());
			bean.setPersistenceSession(Session.persistenceSession.get());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			if (Runtime.useCommit)
				bean.addActionPeformanceListener(Constellations.explorerConstellationProvider.get());
			bean.setPrepareInstantiateEntityAction(false);
			bean.setPrepareWorkWithEntityAction(false);
			return bean;
		}
	};
	
	protected static Supplier<DefaultGmContentViewActionManager> gimaPropertyPanelActionManager = new SessionScopedBeanProvider<DefaultGmContentViewActionManager>() {
		@Override
		public DefaultGmContentViewActionManager create() throws Exception {
			DefaultGmContentViewActionManager bean = publish(abstractActionManager.get());
			bean.setActionMenuBuilder(UiElements.gimaActionMenuBuilder);
			bean.setFolderLoaders(Arrays.asList(gimaActionBarFolderLoader.get(), actionBarFolderLoader.get()));
			bean.setPersistenceSession(Session.persistenceSession.get());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			if (Runtime.useCommit)
				bean.addActionPeformanceListener(Constellations.explorerConstellationProvider.get());
			bean.setPrepareInstantiateEntityAction(false);
			bean.setPrepareWorkWithEntityAction(false);
			//bean.setPrepareGimaOpenerAction(false);
			return bean;
		}
	};

	protected static Supplier<DefaultGmContentViewActionManager> workbenchPropertyPanelActionManager = new SessionScopedBeanProvider<DefaultGmContentViewActionManager>() {
		@Override
		public DefaultGmContentViewActionManager create() throws Exception {
			DefaultGmContentViewActionManager bean = publish(abstractActionManager.get());
			bean.setPersistenceSession(Session.workbenchPersistenceSession.get());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			bean.addActionPeformanceListener(Constellations.explorerConstellationProvider.get());
			bean.setPrepareInstantiateEntityAction(false);
			bean.setPrepareWorkWithEntityAction(false);
			return bean;
		}
	};

	protected static Supplier<ModelEnvironmentDrivenSessionUpdater> modelEnvironmentDrivenSessionUpdater = new SessionScopedBeanProvider<ModelEnvironmentDrivenSessionUpdater>() {
		@Override
		public ModelEnvironmentDrivenSessionUpdater create() throws Exception {
			ModelEnvironmentDrivenSessionUpdater bean = publish(new ModelEnvironmentDrivenSessionUpdater());
			bean.setWorkbenchSessions(Arrays.asList(Session.templateWorkbenchPersistenceSession.get(), Session.workbenchPersistenceSession.get()));
			return bean;
		}
	};

	protected static Supplier<ExplorerWorkbenchController> explorerWorkbenchController = new SessionScopedBeanProvider<ExplorerWorkbenchController>() {
		@Override
		public ExplorerWorkbenchController create() throws Exception {
			ExplorerWorkbenchController bean = publish(new ExplorerWorkbenchController());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			bean.setQuickAccessPanel(Panels.topBannerQuickAccessProvider.get());
			bean.setQuickAccessTriggerField(Panels.topBannerQuickAccessTriggerField.get());
			return bean;
		}
	};

	protected static Supplier<SessionReadyLoader> sessionReadyLoader = new SessionScopedBeanProvider<SessionReadyLoader>() {
		@Override
		public SessionReadyLoader create() throws Exception {
			SessionReadyLoader bean = publish(new SessionReadyLoader());
			bean.setLoaders(Arrays.asList(actionManager.get(), propertyPanelActionManager.get(), headerBarManager.get(), Panels.gmViewActionBar.get(),
					verticalTabManager.get(), browsingConstellationActionBarManager.get(), globalActionsToolBarManager.get(),
					gimaPropertyPanelActionManager.get(), workWithEntityExpert.get()));
			return bean;
		}
	};

	protected static Supplier<WebSocketExpert> webSocketExpert = new SingletonBeanProvider<WebSocketExpert>() {
		@Override
		public WebSocketExpert create() throws Exception {
			WebSocketExpert bean = new WebSocketExpert();
			bean.setWebSocketUrlFunction(Runtime.webSocketUrlProvider.get());
			bean.setTypeEnsurer(GmRpc.typeEnsurer.get());
			//bean.setWebSocketHandlerRegistry(webSocketHandlerRegistry);
			bean.setEvaluator(localServiceRequestEvaluator.get());
			return bean;
		}
	};
	
	protected static Supplier<ConfigurableServiceRequestEvaluator> localServiceRequestEvaluator = new SingletonBeanProvider<ConfigurableServiceRequestEvaluator>() {
		@Override
		public ConfigurableServiceRequestEvaluator create() throws Exception {
			ConfigurableServiceRequestEvaluator bean = new ConfigurableServiceRequestEvaluator();
			bean.setServiceProcessor(dispatchingServiceProcessor.get());
			return bean;
		}
	};
	
	protected static Supplier<ConfigurableDispatchingServiceProcessor> dispatchingServiceProcessor = new SingletonBeanProvider<ConfigurableDispatchingServiceProcessor>() {
		@Override
		public ConfigurableDispatchingServiceProcessor create() throws Exception {
			ConfigurableDispatchingServiceProcessor bean = new ConfigurableDispatchingServiceProcessor();
			bean.bind(Notify.T, Notification.notifyServiceProcessor.get());
			return bean;
		}
	};

	protected static Supplier<GmEditionViewController> gmEditionViewController = new SingletonBeanProvider<GmEditionViewController>() {
		@Override
		public GmEditionViewController create() throws Exception {
			GmEditionViewController bean = new GmEditionViewControllerImpl();
			return bean;
		}
	};

	protected static Supplier<WorkWithEntityExpert> workWithEntityExpert = new SessionScopedBeanProvider<WorkWithEntityExpert>() {
		@Override
		public WorkWithEntityExpert create() throws Exception {
			WorkWithEntityExpert bean = publish(new WorkWithEntityExpert());
			bean.setFolderLoader(actionBarFolderLoader.get());
			bean.setWorkbenchActionHandlerRegistry(Actions.workbenchActionHandlerRegistry.get());
			return bean;
		}
	};

	public static Supplier<ConnectionController> connectionController = new SingletonBeanProvider<ConnectionController>() {
		@Override
		public ConnectionController create() throws Exception {
			ConnectionController bean = publish(new ConnectionController());
			bean.setTribefireServicesUrl(Runtime.tribefireServicesAbsoluteUrl.get());
			//bean.setSessionNotFoundExceptionMessageAction(Actions.sessionNotFoundExceptionMessageAction.get());
			//bean.setPersistenceSession(Session.persistenceSession.get());
			return bean;			
		}			
	};
	
	protected static Supplier<JsUxModuleInitializer> jsUxModuleInitializer = new SessionScopedBeanProvider<JsUxModuleInitializer>() {
		@Override
		public JsUxModuleInitializer create() throws Exception {
			JsUxModuleInitializer bean = publish(new JsUxModuleInitializer());
			bean.setGmSession(Session.persistenceSession.get());
			bean.setServicesUrl(Runtime.tribefireServicesAbsoluteUrl.get());
			return bean;
		}
	};
	
	// public static Supplier<ExplorerWorkbenchController> toolBarLessExplorerWorkbenchController2 = new
	// SessionScopedBeanProvider<ExplorerWorkbenchController>() {
	// @Override
	// public ExplorerWorkbenchController create() throws Exception {
	// ExplorerWorkbenchController bean = publish(new ExplorerWorkbenchController());
	// bean.setExplorerConstellation(Constellations.toolBarLessExplorerConstellationProvider.get());
	// bean.setQuickAccessPanel(Panels.topBannerQuickAccessProvider.get());
	// bean.setQuickAccessTriggerField(Panels.topBannerQuickAccessTriggerField.get());
	// return bean;
	// }
	// };
}
