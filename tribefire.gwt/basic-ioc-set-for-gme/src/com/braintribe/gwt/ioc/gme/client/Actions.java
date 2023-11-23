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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gm.model.uiaction.AddMetadataActionFolderContent;
import com.braintribe.gm.model.uiaction.EditTemplateScriptActionFolderContent;
import com.braintribe.gm.model.uiaction.ExecuteServiceRequestActionFolderContent;
import com.braintribe.gm.model.uiaction.RecordTemplateScriptActionFolderContent;
import com.braintribe.gm.model.uiaction.RefreshEntitiesActionFolderContent;
import com.braintribe.gm.model.uiaction.RefreshMetadataActionFolderContent;
import com.braintribe.gm.model.uiaction.RemoveMetadataActionFolderContent;
import com.braintribe.gm.model.uiaction.ResourceDownloadActionFolderContent;
import com.braintribe.gm.model.uiaction.SwitchToActionFolderContent;
import com.braintribe.gm.model.uiaction.SwitchToWebTerminalActionFolderContent;
import com.braintribe.gwt.accessdeploymentmodeluisupport.client.OpenGmeForAccessInNewTabAction;
import com.braintribe.gwt.accessdeploymentmodeluisupport.client.OpenGmeForWebTerminalInNewTabAction;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.customizationui.client.ShowWindowAction;
import com.braintribe.gwt.customizationui.client.ShowWindowKnownGlobalAction;
import com.braintribe.gwt.customizationui.client.security.LoginAction;
import com.braintribe.gwt.customizationui.client.security.LogoutAction;
import com.braintribe.gwt.gme.constellation.client.JsUxComponentOpenerActionHandler;
import com.braintribe.gwt.gme.constellation.client.JsUxPreviewOpenerActionHandler;
import com.braintribe.gwt.gme.constellation.client.ModelLinkActionHandler;
import com.braintribe.gwt.gme.constellation.client.PrototypeQueryActionHandler;
import com.braintribe.gwt.gme.constellation.client.RedoAction;
import com.braintribe.gwt.gme.constellation.client.SaveAction;
import com.braintribe.gwt.gme.constellation.client.ServiceRequestActionHandler;
import com.braintribe.gwt.gme.constellation.client.SimpleInstantiationActionHandler;
import com.braintribe.gwt.gme.constellation.client.SimpleQueryActionHandler;
import com.braintribe.gwt.gme.constellation.client.UndoAction;
import com.braintribe.gwt.gme.constellation.client.WidgetOpenerActionHandler;
import com.braintribe.gwt.gme.constellation.client.action.AboutAction;
import com.braintribe.gwt.gme.constellation.client.action.AdvancedSaveAction;
import com.braintribe.gwt.gme.constellation.client.action.ChangeAccessAction;
import com.braintribe.gwt.gme.constellation.client.action.ContentMenuAction;
import com.braintribe.gwt.gme.constellation.client.action.DetailsPanelVisibilityAction;
import com.braintribe.gwt.gme.constellation.client.action.ExchangeContentViewAction;
import com.braintribe.gwt.gme.constellation.client.action.MaximizeViewAction;
import com.braintribe.gwt.gme.constellation.client.action.ReloadSessionAction;
import com.braintribe.gwt.gme.constellation.client.action.SeparatorAction;
import com.braintribe.gwt.gme.constellation.client.action.SessionNotFoundExceptionMessageAction;
import com.braintribe.gwt.gme.constellation.client.action.ShowPackagingInfoAction;
import com.braintribe.gwt.gme.constellation.client.action.TestHeaderbarAction;
import com.braintribe.gwt.gme.constellation.client.expert.HyperLinkActionHandler;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gme.templateevaluation.client.expert.TemplateBasedNotificationListener;
import com.braintribe.gwt.gme.templateevaluation.client.expert.TemplateInstantationActionHandler;
import com.braintribe.gwt.gme.templateevaluation.client.expert.TemplateQueryActionHandler;
import com.braintribe.gwt.gme.templateevaluation.client.expert.TemplateServiceRequestActionHandler;
import com.braintribe.gwt.gmview.action.client.ActionFolderContentExpert;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.action.client.ExecuteServiceRequestAction;
import com.braintribe.gwt.gmview.action.client.FieldDialogOpenerAction;
import com.braintribe.gwt.gmview.action.client.InstantiateEntityAction;
import com.braintribe.gwt.gmview.action.client.InstantiateTransientEntityAction;
import com.braintribe.gwt.gmview.action.client.KnownActions;
import com.braintribe.gwt.gmview.action.client.RefreshEntitiesAction;
import com.braintribe.gwt.gmview.action.client.ResourceDownloadAction;
import com.braintribe.gwt.gmview.client.GmContentViewContext;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.ioc.gme.client.action.ShowUserProfileAction;
import com.braintribe.gwt.ioc.gme.client.resources.CustomizationResources;
import com.braintribe.gwt.menu.client.resources.MenuClientBundle;
import com.braintribe.gwt.security.client.SessionScopedBeanProvider;
import com.braintribe.gwt.templateeditor.client.action.EditTemplateScriptAction;
import com.braintribe.gwt.templateeditor.client.action.RecordTemplateScriptAction;
import com.braintribe.gwt.utils.client.RootKeyNavExpert;
import com.braintribe.gwt.workbenchaction.processing.client.WorkbenchActionHandlerRegistry;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionHandler;
import com.braintribe.model.style.Color;
import com.braintribe.model.style.Font;
import com.braintribe.model.workbench.HyperlinkAction;
import com.braintribe.model.workbench.ModelLinkAction;
import com.braintribe.model.workbench.PrototypeQueryAction;
import com.braintribe.model.workbench.ServiceRequestAction;
import com.braintribe.model.workbench.SimpleInstantiationAction;
import com.braintribe.model.workbench.SimpleQueryAction;
import com.braintribe.model.workbench.TemplateInstantiationAction;
import com.braintribe.model.workbench.TemplateQueryAction;
import com.braintribe.model.workbench.TemplateServiceRequestAction;
import com.braintribe.model.workbench.WidgetOpenerAction;
import com.braintribe.model.workbench.WorkbenchAction;
import com.braintribe.provider.PrototypeBeanProvider;
import com.braintribe.provider.SingletonBeanProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;

import tribefire.extension.js.model.deployment.JsUxComponentOpenerAction;
import tribefire.extension.js.model.deployment.JsUxPreviewOpenerAction;
import tribefire.extension.scripting.model.deployment.Script;

/**
 * This is the IoC configuration for Actions.
 * 
 * @author michel.docouto
 *
 */
class Actions {

	private static LocalizedText localizedText = (LocalizedText) GWT.create(LocalizedText.class);

	protected static Supplier<LogoutAction> logout = new SessionScopedBeanProvider<LogoutAction>() {
		@Override
		public LogoutAction create() throws Exception {
			LogoutAction bean = publish(new LogoutAction());
			bean.setIcon(MenuClientBundle.INSTANCE.logout());
			bean.setHoverIcon(MenuClientBundle.INSTANCE.logoutHover());
			bean.setSecurityService(Services.securityService.get());
			bean.setLogoutController(Controllers.logoutController.get());
			return bean;
		}
	};

	protected static Supplier<LoginAction> loginAction = new SingletonBeanProvider<LoginAction>() {
		@Override
		public LoginAction create() throws Exception {
			LoginAction bean = publish(new LoginAction());
			bean.setSecurityService(Services.securityService.get());
			bean.setUserNameTextField(UiElements.userNameTextField);
			bean.setPasswordTextField(UiElements.passwordTextField);
			bean.setErrorMessageLabel(UiElements.errorMessageLabel);
			bean.setParentPanel(Panels.newLoginPanel);
			return bean;
		}
	};

	private static Supplier<ShowWindowAction> showLogWindowAction = new SessionScopedBeanProvider<ShowWindowAction>() {
		{
			RootKeyNavExpert.addRootKeyNavListener(Log.logWindow.get());
		}
		@Override
		public ShowWindowAction create() throws Exception {
			ShowWindowAction bean = publish(new ShowWindowAction());
			bean.setId("settingsMenu_showLogWindowAction");
			bean.setName(localizedText.showLog());
			bean.setWindowProvider(Log.logWindow);
			bean.setTooltip(localizedText.log());
			bean.setIcon(CustomizationResources.INSTANCE.log());
			return bean;
		}
	};

	private static Supplier<ShowWindowAction> showAssetManagementDialogAction = new SessionScopedBeanProvider<ShowWindowAction>() {
		@Override
		public ShowWindowAction create() throws Exception {
			ShowWindowAction bean = publish(new ShowWindowAction());
			bean.setId("settingsMenu_showAssetManagementDialogAction");
			bean.setName(localizedText.platformAssetManagement());
			bean.setWindowProvider(UiElements.assetManagementDialogSupplier);
			bean.setIcon(CustomizationResources.INSTANCE.settings());
			return bean;
		}
	};

	protected static Supplier<FieldDialogOpenerAction<LocalizedString>> localizedStringFieldDialogOpenerAction = new SingletonBeanProvider<FieldDialogOpenerAction<LocalizedString>>() {
		@Override
		public FieldDialogOpenerAction<LocalizedString> create() throws Exception {
			FieldDialogOpenerAction<LocalizedString> bean = publish(new FieldDialogOpenerAction<>());
			bean.setEntityFieldDialogSupplier(UiElements.localizedStringDialog);
			return bean;
		}
	};

	protected static Supplier<FieldDialogOpenerAction<Color>> colorFieldDialogOpenerAction = new SingletonBeanProvider<FieldDialogOpenerAction<Color>>() {
		@Override
		public FieldDialogOpenerAction<Color> create() throws Exception {
			FieldDialogOpenerAction<Color> bean = publish(new FieldDialogOpenerAction<>());
			bean.setEntityFieldDialogSupplier(UiElements.colorDialog);
			return bean;
		}
	};

	protected static Supplier<FieldDialogOpenerAction<Font>> fontFieldDialogOpenerAction = new SingletonBeanProvider<FieldDialogOpenerAction<Font>>() {
		@Override
		public FieldDialogOpenerAction<Font> create() throws Exception {
			FieldDialogOpenerAction<Font> bean = publish(new FieldDialogOpenerAction<>());
			bean.setEntityFieldDialogSupplier(UiElements.fontDialog);
			return bean;
		}
	};

	protected static Supplier<FieldDialogOpenerAction<Script>> gmScriptEditorDialogOpenerAction = new SingletonBeanProvider<FieldDialogOpenerAction<Script>>() {
		@Override
		public FieldDialogOpenerAction<Script> create() throws Exception {
			FieldDialogOpenerAction<Script> bean = publish(new FieldDialogOpenerAction<>());
			bean.setEntityFieldDialogSupplier(UiElements.gmScriptEditorDialog);
			return bean;
		}
	};

	protected static Supplier<ShowPackagingInfoAction> showPackagingInfoAction = new SessionScopedBeanProvider<ShowPackagingInfoAction>() {
		@Override
		public ShowPackagingInfoAction create() throws Exception {
			ShowPackagingInfoAction bean = publish(new ShowPackagingInfoAction());
			bean.setId("settingsMenu_showPackagingInfoAction");
			bean.setBrowsingConstellationDialogProvider(Constellations.browsingConstellationDialog);
			bean.setParentMenu(UiElements.settingsMenu.get());
			bean.setAccessIdProvider(Runtime.accessId);
			bean.setToolBarSupplier(Constellations.cancelGlobalActionsToolBar);
			bean.setPackagingProvider(Providers.packagingProvider.get());
			return bean;
		}
	};
	
	protected static Supplier<AboutAction> showAboutAction = new SessionScopedBeanProvider<AboutAction>() {
		@Override
		public AboutAction create() throws Exception {
			AboutAction bean = publish(new AboutAction());
			bean.setPackagingProvider(Providers.packagingProvider.get());
			bean.setUserNameProvider(Providers.userNameProvider.get());
			//bean.setFolderLoader(Controllers.webReaderHeaderBarFolderLoader.get());
			bean.setSession(Session.workbenchPersistenceSession.get());
			bean.setShowPackagingInfoAction(showPackagingInfoAction.get());
			bean.setUsePackagingInfoAction(true);
			return bean;
		}
	};
	
	protected static Supplier<WorkbenchActionHandlerRegistry> workbenchActionHandlerRegistry = new SessionScopedBeanProvider<WorkbenchActionHandlerRegistry>() {
		@Override
		public WorkbenchActionHandlerRegistry create() throws Exception {
			WorkbenchActionHandlerRegistry bean = publish(new WorkbenchActionHandlerRegistry());
			bean.setWorkbenchActionHandlerRegistryMap(workbenchActionHandlerRegistryMap.get());
			bean.setGmSession(Session.workbenchPersistenceSession.get());
			bean.setUserNameProvider(Providers.userNameProvider.get());
			return bean;
		}
	};

	private static Supplier<Map<EntityType<? extends WorkbenchAction>, Supplier<? extends WorkbenchActionHandler<?>>>> workbenchActionHandlerRegistryMap = new SessionScopedBeanProvider<Map<EntityType<? extends WorkbenchAction>, Supplier<? extends WorkbenchActionHandler<?>>>>() {
		@Override
		public Map<EntityType<? extends WorkbenchAction>, Supplier<? extends WorkbenchActionHandler<?>>> create() throws Exception {
			Map<EntityType<? extends WorkbenchAction>, Supplier<? extends WorkbenchActionHandler<?>>> bean = publish(new HashMap<>());
			bean.put(SimpleQueryAction.T, simpleQueryActionHandler);
			bean.put(PrototypeQueryAction.T, queryActionHandler);
			bean.put(SimpleInstantiationAction.T, simpleInstantiationActionHandler);
			bean.put(WidgetOpenerAction.T, widgetOpenerActionHandler);
			bean.put(JsUxComponentOpenerAction.T, jsUxComponentOpenerActionHandler);
			bean.put(JsUxPreviewOpenerAction.T, jsUxPreviewOpenerActionHandler);
			bean.put(TemplateInstantiationAction.T, templateInstantiationActionHandler);
			bean.put(TemplateQueryAction.T, templateQueryActionHandler);
			bean.put(HyperlinkAction.T, hyperLinkActionHandler);
			bean.put(ModelLinkAction.T, modelLinkActionHandler);
			bean.put(ServiceRequestAction.T, serviceRequestActionHandler);
			bean.put(TemplateServiceRequestAction.T, templateServiceRequestActionHandler);
			return bean;
		}
	};

	protected static Supplier<SimpleQueryActionHandler> simpleQueryActionHandler = new SessionScopedBeanProvider<SimpleQueryActionHandler>() {
		@Override
		public SimpleQueryActionHandler create() throws Exception {
			SimpleQueryActionHandler bean = publish(new SimpleQueryActionHandler());
			bean.setQueryStorageExpert(Providers.wbQueryStorageExpertProvider);
			return bean;
		}
	};

	protected static Supplier<PrototypeQueryActionHandler> queryActionHandler = new SessionScopedBeanProvider<PrototypeQueryActionHandler>() {
		@Override
		public PrototypeQueryActionHandler create() throws Exception {
			PrototypeQueryActionHandler bean = publish(new PrototypeQueryActionHandler());
			bean.setQueryStorageExpert(Providers.wbQueryStorageExpertProvider);
			return bean;
		}
	};

	protected static Supplier<SimpleInstantiationActionHandler> simpleInstantiationActionHandler = new SessionScopedBeanProvider<SimpleInstantiationActionHandler>() {
		@Override
		public SimpleInstantiationActionHandler create() throws Exception {
			SimpleInstantiationActionHandler bean = publish(new SimpleInstantiationActionHandler());
			bean.setNewInstanceProviderProvider(Providers.newInstanceProvider);
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			bean.setGmSession(Session.persistenceSession.get());
			bean.setTransientSession(Session.transientManagedSession.get());
			return bean;
		}
	};

	protected static Supplier<ServiceRequestActionHandler> serviceRequestActionHandler = new SessionScopedBeanProvider<ServiceRequestActionHandler>() {
		@Override
		public ServiceRequestActionHandler create() throws Exception {
			ServiceRequestActionHandler bean = publish(new ServiceRequestActionHandler());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			bean.setGmSession(Session.persistenceSession.get());
			bean.setTransientSessionProvider(Session.prototypeTransientManagedSession);
			bean.setNotificationFactory(Notifications.notificationFactory);
			return bean;
		}
	};

	protected static Supplier<WidgetOpenerActionHandler> widgetOpenerActionHandler = new SessionScopedBeanProvider<WidgetOpenerActionHandler>() {
		@Override
		public WidgetOpenerActionHandler create() throws Exception {
			WidgetOpenerActionHandler bean = publish(new WidgetOpenerActionHandler());
			return bean;
		}
	};
	
	protected static Supplier<JsUxComponentOpenerActionHandler> jsUxComponentOpenerActionHandler = new SessionScopedBeanProvider<JsUxComponentOpenerActionHandler>() {
		@Override
		public JsUxComponentOpenerActionHandler create() throws Exception {
			JsUxComponentOpenerActionHandler bean = publish(new JsUxComponentOpenerActionHandler());
			bean.setJsUxComponentWidgetSupplier(ViewSituationResolution.externalComponentWidgetSupplier.get());
			bean.setPersistenceSession(Session.persistenceSession.get());
			return bean;
		}
	};
	
	private static Supplier<JsUxPreviewOpenerActionHandler> jsUxPreviewOpenerActionHandler = new SessionScopedBeanProvider<JsUxPreviewOpenerActionHandler>() {
		@Override
		public JsUxPreviewOpenerActionHandler create() throws Exception {
			JsUxPreviewOpenerActionHandler bean = publish(new JsUxPreviewOpenerActionHandler());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			bean.setWorkWithEntityExpert(Controllers.workWithEntityExpert.get());
			bean.setJsUxComponentWidgetSupplier(ViewSituationResolution.externalComponentWidgetSupplier.get());
			bean.setGmSession(Session.persistenceSession.get());
			Notifications.refreshPreviewExpert.get().addPreviewPanel(bean);
			return bean;
		}
	};
	
	private static Supplier<TemplateInstantationActionHandler> templateInstantiationActionHandler = new SessionScopedBeanProvider<TemplateInstantationActionHandler>() {
		@Override
		public TemplateInstantationActionHandler create() throws Exception {
			TemplateInstantationActionHandler bean = publish(new TemplateInstantationActionHandler());
			bean.setListener(Constellations.explorerConstellationProvider.get());
			bean.setTemplateGIMADialogProvider(Panels.templateEvaluationDialogProvider);
			bean.setUserNameProvider(Providers.userNameProvider.get());
			bean.setTransientGmSession(Session.transientManagedSession.get());
			return bean;
		}
	};

	private static Supplier<TemplateQueryActionHandler> templateQueryActionHandler = new SessionScopedBeanProvider<TemplateQueryActionHandler>() {
		@Override
		public TemplateQueryActionHandler create() throws Exception {
			TemplateQueryActionHandler bean = publish(new TemplateQueryActionHandler());
			bean.setQueryStorageExpert(Providers.wbQueryStorageExpertProvider);
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			bean.setTemplateGIMADialogProvider(Panels.templateEvaluationDialogProvider);
			bean.setUserNameProvider(Providers.userNameProvider.get());
			return bean;
		}
	};

	private static Supplier<TemplateServiceRequestActionHandler> templateServiceRequestActionHandler = new SessionScopedBeanProvider<TemplateServiceRequestActionHandler>() {
		@Override
		public TemplateServiceRequestActionHandler create() throws Exception {
			TemplateServiceRequestActionHandler bean = publish(new TemplateServiceRequestActionHandler());
			bean.setListener(Constellations.explorerConstellationProvider.get());
			bean.setCurrentTransientSessionProvider(Constellations.explorerConstellationProvider.get());
			bean.setGmSession(Session.persistenceSession.get());
			bean.setTemplateGIMADialogProvider(Panels.templateEvaluationDialogProvider);
			bean.setTransientSessionProvider(Session.prototypeTransientManagedSession);
			bean.setNotificationFactory(Notifications.notificationFactory);
			bean.setUserNameProvider(Providers.userNameProvider.get());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			return bean;
		}
	};

	protected static Supplier<TemplateBasedNotificationListener> templateBasedNotificationListener = new SessionScopedBeanProvider<TemplateBasedNotificationListener>() {
		@Override
		public TemplateBasedNotificationListener create() throws Exception {
			TemplateBasedNotificationListener bean = publish(new TemplateBasedNotificationListener());
			bean.setGmSession(Session.notificationManagedSession);
			return bean;
		}
	};

	protected static Supplier<HyperLinkActionHandler> hyperLinkActionHandler = new SessionScopedBeanProvider<HyperLinkActionHandler>() {
		@Override
		public HyperLinkActionHandler create() throws Exception {
			HyperLinkActionHandler bean = publish(new HyperLinkActionHandler());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			bean.setMasterDetailConstellationProvider(Constellations.simpleMasterDetailConstellationProvider);
			bean.setSession(Session.workbenchPersistenceSession);
			return bean;
		}
	};

	protected static Supplier<ModelLinkActionHandler> modelLinkActionHandler = new SessionScopedBeanProvider<ModelLinkActionHandler>() {
		@Override
		public ModelLinkActionHandler create() throws Exception {
			ModelLinkActionHandler bean = publish(new ModelLinkActionHandler());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			return bean;
		}
	};

	protected static Supplier<List<Pair<ActionTypeAndName, Supplier<? extends ModelAction>>>> externalActionProviders = new SessionScopedBeanProvider<List<Pair<ActionTypeAndName, Supplier<? extends ModelAction>>>>() {
		@Override
		public List<Pair<ActionTypeAndName, Supplier<? extends ModelAction>>> create() throws Exception {
			List<Pair<ActionTypeAndName, Supplier<? extends ModelAction>>> bean = publish(new ArrayList<>());
			bean.add(new Pair<>(new ActionTypeAndName(SwitchToActionFolderContent.T, KnownActions.OPEN_GME_FOR_ACCESS_NEW_TAB.getName()),
					openGmeForAccessInNewTabAction));
			bean.add(new Pair<>(
					new ActionTypeAndName(SwitchToWebTerminalActionFolderContent.T, KnownActions.OPEN_GME_FOR_WEB_TERMINAL_NEW_TAB.getName()),
					openGmeForWebTerminalInNewTabAction));
			bean.add(new Pair<>(new ActionTypeAndName(RefreshEntitiesActionFolderContent.T, KnownActions.REFRESH_ENTITIES.getName()),
					refreshEntitiesAction));
			// bean.add(new Pair<>(KnownActions.DETAILS_PANEL_VISIBILITY.getName(), detailsPanelVisibilityAction));
			bean.add(new Pair<>(new ActionTypeAndName(ResourceDownloadActionFolderContent.T, KnownActions.RESOURCE_DOWNLOAD.getName()),
					resourceDownloadAction));
			bean.add(new Pair<>(new ActionTypeAndName(RecordTemplateScriptActionFolderContent.T, KnownActions.RECORD_TEMPLATE_SCRIPT.getName()),
					recordTemplateScriptAction));
			bean.add(new Pair<>(new ActionTypeAndName(EditTemplateScriptActionFolderContent.T, KnownActions.EDIT_TEMPLATE_SCRIPT.getName()),
					editTemplateScriptAction));
			bean.add(new Pair<>(new ActionTypeAndName(AddMetadataActionFolderContent.T, KnownActions.ADD_METADATA_EDITOR.getName()),
					MetaDataEditor.addDeclaredMetaDataEditorAction));
			bean.add(new Pair<>(new ActionTypeAndName(RefreshMetadataActionFolderContent.T, KnownActions.REFRESH_METADATA_EDITOR.getName()),
					MetaDataEditor.refreshMetaDataEditorAction));
			bean.add(new Pair<>(new ActionTypeAndName(RemoveMetadataActionFolderContent.T, KnownActions.REMOVE_METADATA_EDITOR.getName()),
					MetaDataEditor.removeMetaDataAction));
			bean.add(new Pair<>(new ActionTypeAndName("previousMetaDataEditorAction"), MetaDataEditor.previousMetaDataEditorAction));
			bean.add(new Pair<>(new ActionTypeAndName("nextMetaDataEditorAction"), MetaDataEditor.nextMetaDataEditorAction));
			// bean.add(new Pair<>("MetaModelEditor", metaModelEditorAction));
			// bean.add(new Pair<>(generateUmlCanvasAction.get()));
			bean.add(new Pair<>(new ActionTypeAndName(ExecuteServiceRequestActionFolderContent.T, KnownActions.EXECUTE_SERVICE_REQUEST.getName()),
					executeAction));
			return bean;
		}
	};

	protected static Supplier<DetailsPanelVisibilityAction> detailsPanelVisibilityAction = new PrototypeBeanProvider<DetailsPanelVisibilityAction>() {
		@Override
		public DetailsPanelVisibilityAction create() throws Exception {
			DetailsPanelVisibilityAction bean = new DetailsPanelVisibilityAction();
			return bean;
		}
	};

	private static Supplier<OpenGmeForAccessInNewTabAction> openGmeForAccessInNewTabAction = new PrototypeBeanProvider<OpenGmeForAccessInNewTabAction>() {
		@Override
		public OpenGmeForAccessInNewTabAction create() throws Exception {
			OpenGmeForAccessInNewTabAction bean = new OpenGmeForAccessInNewTabAction();
			bean.setSessionIdProvider(Providers.sessionIdProvider.get());
			bean.setGmSession(Session.persistenceSession.get());
			bean.setTribeFireExplorerURL(Runtime.tribefireExplorerUrl.get());
			return bean;
		}
	};
	private static Supplier<OpenGmeForWebTerminalInNewTabAction> openGmeForWebTerminalInNewTabAction = new PrototypeBeanProvider<OpenGmeForWebTerminalInNewTabAction>() {
		@Override
		public OpenGmeForWebTerminalInNewTabAction create() throws Exception {
			OpenGmeForWebTerminalInNewTabAction bean = new OpenGmeForWebTerminalInNewTabAction();
			bean.setGmSession(Session.persistenceSession.get());
			bean.setServicesUrlSupplier(Runtime.tribefireServicesUrl);
			return bean;
		}
	};

	protected static Supplier<RefreshEntitiesAction> refreshEntitiesAction = new PrototypeBeanProvider<RefreshEntitiesAction>() {
		@Override
		public RefreshEntitiesAction create() throws Exception {
			RefreshEntitiesAction bean = new RefreshEntitiesAction();
			bean.setUseMask(Runtime.useCommit);
			bean.setSpecialEntityTraversingCriterion(Panels.specialEntityTraversingCriterionMap.get());
			bean.setUseCase(Runtime.assemblyPanelUseCaseProvider.get());
			bean.setCurrentContentViewProvider(Providers.currentContentViewProvider.get());
			return bean;
		}
	};

	private static Supplier<ResourceDownloadAction> resourceDownloadAction = new PrototypeBeanProvider<ResourceDownloadAction>() {
		@Override
		public ResourceDownloadAction create() throws Exception {
			ResourceDownloadAction bean = new ResourceDownloadAction();
			bean.setGmSession(Session.persistenceSession.get());
			return bean;
		}
	};

	protected static Supplier<ShowWindowAction> showUploadDialogAction = new PrototypeBeanProvider<ShowWindowAction>() {
		@Override
		public ShowWindowAction create() throws Exception {
			ShowWindowKnownGlobalAction bean = (new ShowWindowKnownGlobalAction());
			bean.setKnownName("upload");
			bean.setName(LocalizedText.INSTANCE.upload());
			bean.setTooltip(LocalizedText.INSTANCE.upload());
			bean.setIcon(CustomizationResources.INSTANCE.upload());
			bean.setHoverIcon(CustomizationResources.INSTANCE.uploadSmall());
			bean.setWindowProvider(Panels.resourceUploadDialogProvider);
			return bean;
		}
	};

	private static Supplier<ExchangeContentViewAction> abstractExchangeContentViewAction = new PrototypeBeanProvider<ExchangeContentViewAction>() {
		{
			setAbstract(true);
		}

		@Override
		public ExchangeContentViewAction create() throws Exception {
			ExchangeContentViewAction bean = new ExchangeContentViewAction();
			bean.setViewSituationResolver(ViewSituationResolution.viewSituationResolver);
			bean.setGmViewActionBar(Panels.gmViewActionBar);
			return bean;
		}
	};

	protected static Supplier<ExchangeContentViewAction> exchangeContentViewAction = new PrototypeBeanProvider<ExchangeContentViewAction>() {
		@Override
		public ExchangeContentViewAction create() throws Exception {
			ExchangeContentViewAction bean = abstractExchangeContentViewAction.get();
			bean.setExternalContentViewContexts(contentViewContexts.get());
			return bean;
		}
	};
	
	protected static Supplier<ExchangeContentViewAction> browsingExchangeContentViewAction = new PrototypeBeanProvider<ExchangeContentViewAction>() {
		@Override
		public ExchangeContentViewAction create() throws Exception {
			ExchangeContentViewAction bean = exchangeContentViewAction.get();
			bean.setExternalContentViewContexts(contentViewContexts.get());
			bean.setUseAsMenu(true);
			return bean;
		}
	};	

	protected static Supplier<ExchangeContentViewAction> selectExchangeContentViewAction = new PrototypeBeanProvider<ExchangeContentViewAction>() {
		@Override
		public ExchangeContentViewAction create() throws Exception {
			ExchangeContentViewAction bean = abstractExchangeContentViewAction.get();
			bean.setExternalContentViewContexts(selectContentViewContexts.get());
			return bean;
		}
	};

	protected static Supplier<ExchangeContentViewAction> simpleExchangeContentViewAction = new PrototypeBeanProvider<ExchangeContentViewAction>() {
		@Override
		public ExchangeContentViewAction create() throws Exception {
			ExchangeContentViewAction bean = abstractExchangeContentViewAction.get();
			bean.setExternalContentViewContexts(simpleContentViewContexts.get());
			return bean;
		}
	};

	protected static Supplier<ExchangeContentViewAction> changesExchangeContentViewAction = new PrototypeBeanProvider<ExchangeContentViewAction>() {
		@Override
		public ExchangeContentViewAction create() throws Exception {
			ExchangeContentViewAction bean = abstractExchangeContentViewAction.get();
			bean.setExternalContentViewContexts(changesContentViewContexts.get());
			return bean;
		}
	};

	protected static Supplier<ExchangeContentViewAction> clipboardExchangeContentViewAction = new PrototypeBeanProvider<ExchangeContentViewAction>() {
		@Override
		public ExchangeContentViewAction create() throws Exception {
			ExchangeContentViewAction bean = abstractExchangeContentViewAction.get();
			bean.setExternalContentViewContexts(clipboardContentViewContexts.get());
			return bean;
		}
	};

	protected static Supplier<ExchangeContentViewAction> changesSelectionExchangeContentViewAction = new PrototypeBeanProvider<ExchangeContentViewAction>() {
		@Override
		public ExchangeContentViewAction create() throws Exception {
			ExchangeContentViewAction bean = abstractExchangeContentViewAction.get();
			bean.setExternalContentViewContexts(selectionContentViewContexts.get());
			return bean;
		}
	};

	protected static Supplier<ExchangeContentViewAction> selectionExchangeContentViewAction = new PrototypeBeanProvider<ExchangeContentViewAction>() {
		@Override
		public ExchangeContentViewAction create() throws Exception {
			ExchangeContentViewAction bean = abstractExchangeContentViewAction.get();
			bean.setExternalContentViewContexts(selectionContentViewContexts.get());
			return bean;
		}
	};

	protected static Supplier<ExchangeContentViewAction> localModeExchangeContentViewAction = new PrototypeBeanProvider<ExchangeContentViewAction>() {
		@Override
		public ExchangeContentViewAction create() throws Exception {
			ExchangeContentViewAction bean = abstractExchangeContentViewAction.get();
			bean.setExternalContentViewContexts(localModeContentViewContexts.get());
			return bean;
		}
	};

	protected static Supplier<ExchangeContentViewAction> gimaExchangeContentViewAction = new PrototypeBeanProvider<ExchangeContentViewAction>() {
		@Override
		public ExchangeContentViewAction create() throws Exception {
			ExchangeContentViewAction bean = abstractExchangeContentViewAction.get();
			bean.setExternalContentViewContexts(gimaContentViewContexts.get());
			return bean;
		}
	};

	protected static Supplier<List<GmContentViewContext>> contentViewContexts = new PrototypeBeanProvider<List<GmContentViewContext>>() {
		@Override
		public List<GmContentViewContext> create() throws Exception {
			List<GmContentViewContext> bean = new ArrayList<>();

			GmContentViewContext assemblyPanelContext = new GmContentViewContext(Panels.assemblyPanelProvider, LocalizedText.INSTANCE.listView(),
					ConstellationResources.INSTANCE.list64(), ConstellationResources.INSTANCE.list64(), Runtime.assemblyPanelUseCaseProvider.get(),
					true);
			assemblyPanelContext.setDetailViewProvider(Panels.tabbedPropertyPanelProvider);
			bean.add(assemblyPanelContext);
			GmContentViewContext thumbnailPanelContext = new GmContentViewContext(Panels.editThumbnailPanelProvider,
					LocalizedText.INSTANCE.thumbnailView(), ConstellationResources.INSTANCE.grid64(), ConstellationResources.INSTANCE.grid64(),
					Runtime.thumbnailPanelUseCaseProvider.get(), true);
			thumbnailPanelContext.setDetailViewProvider(Panels.tabbedPropertyPanelProvider);
			bean.add(thumbnailPanelContext);

			return bean;
		}
	};

	private static Supplier<List<GmContentViewContext>> selectContentViewContexts = new PrototypeBeanProvider<List<GmContentViewContext>>() {
		@Override
		public List<GmContentViewContext> create() throws Exception {
			List<GmContentViewContext> bean = new ArrayList<>();

			GmContentViewContext selectResultPanelContext = new GmContentViewContext(Panels.selectResultPanelProvider,
					LocalizedText.INSTANCE.selectView(), ConstellationResources.INSTANCE.list64(), ConstellationResources.INSTANCE.list64(),
					Runtime.selectResultPanelUseCaseProvider.get(), true);
			selectResultPanelContext.setDetailViewProvider(Panels.tabbedPropertyPanelProvider);
			bean.add(selectResultPanelContext);

			GmContentViewContext assemblyPanelContext = new GmContentViewContext(Panels.assemblyPanelProvider, LocalizedText.INSTANCE.listView(),
					ConstellationResources.INSTANCE.list64(), ConstellationResources.INSTANCE.list64(), Runtime.assemblyPanelUseCaseProvider.get(),
					true);
			assemblyPanelContext.setDetailViewProvider(Panels.tabbedPropertyPanelProvider);
			bean.add(assemblyPanelContext);

			return bean;
		}
	};

	private static Supplier<List<GmContentViewContext>> gimaContentViewContexts = new PrototypeBeanProvider<List<GmContentViewContext>>() {
		@Override
		public List<GmContentViewContext> create() throws Exception {
			List<GmContentViewContext> bean = new ArrayList<>();
			bean.add(new GmContentViewContext(Panels.gimaAssemblyPanelProvider, LocalizedText.INSTANCE.listView(),
					ConstellationResources.INSTANCE.list64(), ConstellationResources.INSTANCE.list64(), Runtime.gimaUseCaseProvider.get(), true));
			bean.add(new GmContentViewContext(Panels.editThumbnailPanelProvider, LocalizedText.INSTANCE.thumbnailView(),
					ConstellationResources.INSTANCE.grid64(), ConstellationResources.INSTANCE.grid64(), Runtime.gimaUseCaseProvider.get(), true));
			return bean;
		}
	};

	private static Supplier<List<GmContentViewContext>> localModeContentViewContexts = new PrototypeBeanProvider<List<GmContentViewContext>>() {
		@Override
		public List<GmContentViewContext> create() throws Exception {
			List<GmContentViewContext> bean = new ArrayList<>();
			bean.add(new GmContentViewContext(Panels.localModeAssemblyPanelProvider, LocalizedText.INSTANCE.listView(),
					ConstellationResources.INSTANCE.list64(), ConstellationResources.INSTANCE.list64(), Runtime.assemblyPanelUseCaseProvider.get(),
					true));
			bean.add(new GmContentViewContext(Panels.editThumbnailPanelProvider, LocalizedText.INSTANCE.thumbnailView(),
					ConstellationResources.INSTANCE.grid64(), ConstellationResources.INSTANCE.grid64(), Runtime.thumbnailPanelUseCaseProvider.get(),
					true));
			return bean;
		}
	};

	private static Supplier<List<GmContentViewContext>> selectionContentViewContexts = new PrototypeBeanProvider<List<GmContentViewContext>>() {
		@Override
		public List<GmContentViewContext> create() throws Exception {
			List<GmContentViewContext> bean = new ArrayList<>();
			bean.add(new GmContentViewContext(Panels.selectionAssemblyPanelProvider, LocalizedText.INSTANCE.listView(),
					ConstellationResources.INSTANCE.list64(), ConstellationResources.INSTANCE.list64(), Runtime.selectionUseCaseProvider.get(),
					true));
			bean.add(new GmContentViewContext(Panels.editThumbnailPanelProvider, LocalizedText.INSTANCE.thumbnailView(),
					ConstellationResources.INSTANCE.grid64(), ConstellationResources.INSTANCE.grid64(), Runtime.selectionUseCaseProvider.get(),
					true));
			return bean;
		}
	};

	private static Supplier<List<GmContentViewContext>> changesContentViewContexts = new PrototypeBeanProvider<List<GmContentViewContext>>() {
		@Override
		public List<GmContentViewContext> create() throws Exception {
			List<GmContentViewContext> bean = new ArrayList<>();
			bean.add(new GmContentViewContext(Panels.readOnlyAssemblyPanelProvider, LocalizedText.INSTANCE.listView(),
					ConstellationResources.INSTANCE.list64(), ConstellationResources.INSTANCE.list64(), Runtime.assemblyPanelUseCaseProvider.get(),
					true));
			bean.add(new GmContentViewContext(Panels.editThumbnailPanelProvider, LocalizedText.INSTANCE.thumbnailView(),
					ConstellationResources.INSTANCE.grid64(), ConstellationResources.INSTANCE.grid64(), Runtime.thumbnailPanelUseCaseProvider.get(),
					true));
			return bean;
		}
	};

	private static Supplier<List<GmContentViewContext>> clipboardContentViewContexts = new PrototypeBeanProvider<List<GmContentViewContext>>() {
		@Override
		public List<GmContentViewContext> create() throws Exception {
			List<GmContentViewContext> bean = new ArrayList<>();
			bean.add(new GmContentViewContext(Panels.clipboardAssemblyPanelProvider, LocalizedText.INSTANCE.listView(),
					ConstellationResources.INSTANCE.list64(), ConstellationResources.INSTANCE.list64(), Runtime.assemblyPanelUseCaseProvider.get(),
					true));
			bean.add(new GmContentViewContext(Panels.editThumbnailPanelProvider, LocalizedText.INSTANCE.thumbnailView(),
					ConstellationResources.INSTANCE.grid64(), ConstellationResources.INSTANCE.grid64(), Runtime.thumbnailPanelUseCaseProvider.get(),
					true));
			return bean;
		}
	};

	private static Supplier<List<GmContentViewContext>> simpleContentViewContexts = new PrototypeBeanProvider<List<GmContentViewContext>>() {
		@Override
		public List<GmContentViewContext> create() throws Exception {
			List<GmContentViewContext> bean = new ArrayList<>();
			bean.add(new GmContentViewContext(Panels.hyperLinkContentViewPanelProvider, LocalizedText.INSTANCE.listView(),
					ConstellationResources.INSTANCE.list64(), ConstellationResources.INSTANCE.list64(), null));
			bean.add(new GmContentViewContext(Panels.editThumbnailPanelProvider, LocalizedText.INSTANCE.thumbnailView(),
					ConstellationResources.INSTANCE.grid64(), ConstellationResources.INSTANCE.grid64(), Runtime.thumbnailPanelUseCaseProvider.get(),
					true));
			return bean;
		}
	};

	protected static Supplier<SaveAction> saveAction = new SingletonBeanProvider<SaveAction>() {
		@Override
		public SaveAction create() throws Exception {
			SaveAction bean = publish(new SaveAction());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			bean.configureGwtPersistenceSession(Session.persistenceSession.get());
			bean.setValidationSupplier(Panels.validation);
			RootKeyNavExpert.addRootKeyNavListener(bean);
			return bean;
		}
	};

	protected static Supplier<RedoAction> redoAction = new SingletonBeanProvider<RedoAction>() {
		@Override
		public RedoAction create() throws Exception {
			RedoAction bean = publish(new RedoAction());
			bean.setGmSession(Session.persistenceSession.get());
			return bean;
		}
	};

	protected static Supplier<RedoAction> transientRedoAction = new SingletonBeanProvider<RedoAction>() {
		@Override
		public RedoAction create() throws Exception {
			RedoAction bean = publish(new RedoAction());
			bean.setGmSession(Session.transientManagedSession.get());
			bean.setName(localizedText.redoTransient());
			bean.setIcon(ConstellationResources.INSTANCE.redoBlack());
			bean.setHoverIcon(ConstellationResources.INSTANCE.redoBlackSmall());
			return bean;
		}
	};

	protected static Supplier<UndoAction> undoAction = new SingletonBeanProvider<UndoAction>() {
		@Override
		public UndoAction create() throws Exception {
			UndoAction bean = publish(new UndoAction());
			bean.setGmSession(Session.persistenceSession.get());
			return bean;
		}
	};

	protected static Supplier<UndoAction> transientUndoAction = new SingletonBeanProvider<UndoAction>() {
		@Override
		public UndoAction create() throws Exception {
			UndoAction bean = publish(new UndoAction());
			bean.setGmSession(Session.transientManagedSession.get());
			bean.setName(localizedText.undoTransient());
			bean.setIcon(ConstellationResources.INSTANCE.undoBlack());
			bean.setHoverIcon(ConstellationResources.INSTANCE.undoBlackSmall());
			return bean;
		}
	};

	private static Supplier<RecordTemplateScriptAction> recordTemplateScriptAction = new PrototypeBeanProvider<RecordTemplateScriptAction>() {
		@Override
		public RecordTemplateScriptAction create() throws Exception {
			RecordTemplateScriptAction bean = (new RecordTemplateScriptAction());
			bean.setSession(Session.persistenceSession.get());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			return bean;
		}
	};

	private static Supplier<EditTemplateScriptAction> editTemplateScriptAction = new PrototypeBeanProvider<EditTemplateScriptAction>() {
		@Override
		public EditTemplateScriptAction create() throws Exception {
			EditTemplateScriptAction bean = (new EditTemplateScriptAction());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			return bean;
		}
	};

	private static Supplier<ChangeAccessAction> changeAccessAction = new SingletonBeanProvider<ChangeAccessAction>() {
		@Override
		public ChangeAccessAction create() throws Exception {
			ChangeAccessAction bean = publish(new ChangeAccessAction());
			bean.setId("settingsMenu_changeAccessAction");
			bean.setUseHardwired(Runtime.useHardwiredAccesses);
			bean.setCustomizationConstellation(Constellations.customizationConstellationProvider.get());
			bean.setSessionIdProvider(Providers.sessionIdProvider.get());
			bean.setTribeFireExplorerURL(Runtime.tribefireExplorerUrl.get());
			bean.setModelEnvironmentProvider(Providers.modelEnvironmentProvider.get());
			bean.setAvailableAccessesDataFutureProvider(Providers.availableAccessesDataProvider.get());
			return bean;
		}
	};

	protected static Supplier<ShowUserProfileAction> showProfileAction = new SingletonBeanProvider<ShowUserProfileAction>() {
		@Override
		public ShowUserProfileAction create() throws Exception {
			ShowUserProfileAction bean = publish(new ShowUserProfileAction());
			bean.setName(LocalizedText.INSTANCE.profile());
			bean.setTooltip(LocalizedText.INSTANCE.profile());
			bean.setIcon(MenuClientBundle.INSTANCE.user());
			bean.setHoverIcon(MenuClientBundle.INSTANCE.user());

			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			bean.setModelEnvironmentProvider(Providers.modelEnvironmentProvider.get());
			bean.setUserProvider(Providers.userProvider.get());
			bean.setPropertyPanelProvider(Panels.userGimaPropertyPanelProvider);
			bean.setGmSession(Session.userSession.get());
			bean.setGimaDialogProvider(UiElements.userGimaDialogProvider);

			return bean;
		}
	};

	protected static Supplier<List<Action>> settingsMenuActions = new SessionScopedBeanProvider<List<Action>>() {
		@Override
		public List<Action> create() throws Exception {
			List<Action> bean = publish(new ArrayList<>());
			bean.add(changeAccessAction.get());
			bean.add(reloadSessionAction.get());
			// bean.add(uiThemeAction.get());
			bean.add(showLogWindowAction.get());
			// bean.add(persistActionGroupAction.get());
			// bean.add(persistActions.get());
			// bean.add(persistHeaderBarAction.get());
			// bean.add(persistVerticalTabAction.get());
			// bean.add(persistGlobalActionsToAction.get());

			bean.add(showAssetManagementDialogAction.get());
			//bean.add(showPackagingInfoAction.get());
			bean.add(showAboutAction.get());
			return bean;
		}
	};

	protected static Supplier<List<Action>> userMenuActions = new SessionScopedBeanProvider<List<Action>>() {
		@Override
		public List<Action> create() throws Exception {
			List<Action> bean = publish(new ArrayList<>());
			bean.add(showProfileAction.get());
			bean.add(logout.get());
			return bean;
		}
	};

	protected static Supplier<List<Action>> constellationDefaultActions = new SessionScopedBeanProvider<List<Action>>() {
		@Override
		public List<Action> create() throws Exception {
			List<Action> bean = publish(new ArrayList<>());
			bean.add(browsingExchangeContentViewAction.get());			
			bean.add(maximizeViewAction.get());
			bean.add(detailsPanelVisibilityAction.get());
			return bean;
		}
	};
	
	protected static Supplier<List<Action>> constellationSimpleActions = new SessionScopedBeanProvider<List<Action>>() {
		@Override
		public List<Action> create() throws Exception {
			List<Action> bean = publish(new ArrayList<>());
			bean.add(maximizeViewAction.get());
			bean.add(detailsPanelVisibilityAction.get());
			return bean;
		}
	};	

	protected static Supplier<ReloadSessionAction> reloadSessionAction = new SessionScopedBeanProvider<ReloadSessionAction>() {
		@Override
		public ReloadSessionAction create() throws Exception {
			ReloadSessionAction bean = publish(new ReloadSessionAction());
			bean.setId("settingsMenu_reloadSessionAction");
			bean.setName(localizedText.reload());
			bean.setTooltip(localizedText.reloadCurrentSession());
			bean.setIcon(MenuClientBundle.INSTANCE.refresh());
			bean.setHoverIcon(MenuClientBundle.INSTANCE.refresh());
			bean.setCustomizationConstellation(Constellations.customizationConstellationProvider.get());
			bean.setSettingsMenu(UiElements.settingsMenu.get());
			bean.setModelEnvironmentProvider(Providers.modelEnvironmentProvider.get());
			return bean;
		}
	};

	protected static Supplier<InstantiateEntityAction> instantiateEntityAction = new PrototypeBeanProvider<InstantiateEntityAction>() {
		@Override
		public InstantiateEntityAction create() throws Exception {
			InstantiateEntityAction bean = new InstantiateEntityAction();
			bean.setIcon(CustomizationResources.INSTANCE.addOrangeBig());
			bean.setHoverIcon(CustomizationResources.INSTANCE.addOrange());
			bean.setInstantiationActionHandler(Constellations.explorerConstellationProvider.get());
			bean.setInstantiationActionsProvider(Providers.entityTypeInstantiationActionsProvider.get());
			bean.setInstantiateTransientEntityActionProvider(instantiateTransientEntityAction);
			bean.setDefaultInstantiateEntityActionProvider(defaultInstantiateEntityAction);
			Scheduler.get().scheduleDeferred(() -> Constellations.customizationConstellationProvider.get().addModelEnvironmentSetListener(bean));
			return bean;
		}
	};

	protected static Supplier<InstantiateEntityAction> defaultInstantiateEntityAction = new PrototypeBeanProvider<InstantiateEntityAction>() {
		@Override
		public InstantiateEntityAction create() throws Exception {
			InstantiateEntityAction bean = new InstantiateEntityAction();
			bean.setIcon(CustomizationResources.INSTANCE.addOrangeBig());
			bean.setHoverIcon(CustomizationResources.INSTANCE.addOrange());
			bean.setInstantiationActionHandler(Constellations.explorerConstellationProvider.get());
			bean.setInstantiationActionsProvider(Providers.entityTypeInstantiationActionsProvider.get());
			bean.setIsDefaultInstantiateEntityAction(true);
			return bean;
		}
	};
	
	protected static Supplier<InstantiateTransientEntityAction> instantiateTransientEntityAction = new PrototypeBeanProvider<InstantiateTransientEntityAction>() {
		@Override
		public InstantiateTransientEntityAction create() throws Exception {
			InstantiateTransientEntityAction bean = new InstantiateTransientEntityAction();
			bean.setTransientSession(Session.transientManagedSession.get());
			bean.setIcon(CustomizationResources.INSTANCE.addOrangeBig());
			bean.setHoverIcon(CustomizationResources.INSTANCE.addOrange());
			bean.setInstantiationActionHandler(Constellations.explorerConstellationProvider.get());
			bean.setInstantiationActionsProvider(Providers.entityTypeInstantiationActionsProvider.get());
			return bean;
		}
	};

	protected static Supplier<MaximizeViewAction> maximizeViewAction = new PrototypeBeanProvider<MaximizeViewAction>() {
		@Override
		public MaximizeViewAction create() throws Exception {
			MaximizeViewAction bean = new MaximizeViewAction();
			return bean;
		}
	};
	
	protected static Supplier<SeparatorAction> separatorAction = new PrototypeBeanProvider<SeparatorAction>() {
		@Override
		public SeparatorAction create() throws Exception {
			SeparatorAction bean = new SeparatorAction();
			return bean;
		}
	};		
	
	protected static Supplier<ContentMenuAction> contentMenuAction = new PrototypeBeanProvider<ContentMenuAction>() {
		@Override
		public ContentMenuAction create() throws Exception {
			ContentMenuAction bean = new ContentMenuAction();
			//bean.setActionManager(Controllers.actionManager.get());
			return bean;
		}
	};	

	private static Supplier<ExecuteServiceRequestAction> executeAction = new PrototypeBeanProvider<ExecuteServiceRequestAction>() {
		@Override
		public ExecuteServiceRequestAction create() throws Exception {
			ExecuteServiceRequestAction bean = new ExecuteServiceRequestAction();
			bean.setDataSession(Session.persistenceSession.get());
			bean.setTransientSession(Session.transientManagedSession.get());
			bean.setTransientSessionProvider(Session.prototypeTransientManagedSession);
			bean.setNotificationFactory(Notifications.notificationFactory);
			return bean;
		}
	};

	protected static Supplier<AdvancedSaveAction> advancedSaveAction = new SingletonBeanProvider<AdvancedSaveAction>() {
		@Override
		public AdvancedSaveAction create() throws Exception {
			AdvancedSaveAction bean = new AdvancedSaveAction();
			bean.setGmSession(Session.persistenceSession.get());
			bean.setAdvancedSaveDialogSupplier(UiElements.advancedSaveActionDialogSupplier);
			bean.setSettingsAdvancedSaveAction(showAssetManagementDialogAction.get());
			bean.setPlatformSetupSupported(Runtime.platformSetupSupported.get());
			return bean;
		}
	};
	
	protected static Supplier<SessionNotFoundExceptionMessageAction> sessionNotFoundExceptionMessageAction = new SingletonBeanProvider<SessionNotFoundExceptionMessageAction>() {
		@Override
		public SessionNotFoundExceptionMessageAction create() throws Exception {
			SessionNotFoundExceptionMessageAction bean = new SessionNotFoundExceptionMessageAction();
			bean.setLoginServletUrl(Runtime.loginServletUrlProvider.get());
			return bean;
		}
	};

	protected static Supplier<ActionFolderContentExpert> actionFolderContentExpert = new SingletonBeanProvider<ActionFolderContentExpert>() {
		@Override
		public ActionFolderContentExpert create() throws Exception {
			ActionFolderContentExpert bean = new ActionFolderContentExpert();
			bean.setInstantiateEntityActionProvider(instantiateEntityAction);
			return bean;
		}
		
	};
	
	public static Supplier<TestHeaderbarAction> testRveAction = new PrototypeBeanProvider<TestHeaderbarAction>() {
		@Override
		public TestHeaderbarAction create() throws Exception {
			TestHeaderbarAction bean = (new TestHeaderbarAction());
			bean.setName("TestAction");
			bean.setIcon(CustomizationResources.INSTANCE.upload());
			bean.setHoverIcon(CustomizationResources.INSTANCE.uploadSmall());
			//bean.setPreviewOpenerActionHandlerSupplier(previewOpenerActionHandler);
			return bean;
		}
	};	
	
	/* private static Supplier<PersistActions> persistActions = new SessionScopedBeanProvider<PersistActions>() {
	 * 
	 * @Override public PersistActions create() throws Exception { PersistActions bean = publish(new PersistActions());
	 * bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
	 * bean.setDataSession(Session.persistenceSession.get());
	 * bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
	 * bean.setPersistActions(Arrays.asList((Action) persistActionGroupAction.get(), persistVerticalTabAction.get(),
	 * persistGlobalActionsToAction.get())); return bean; } };
	 * 
	 * private static Supplier<PersistActionGroupAction> persistActionGroupAction = new
	 * SessionScopedBeanProvider<PersistActionGroupAction>() {
	 * 
	 * @Override public PersistActionGroupAction create() throws Exception { PersistActionGroupAction bean = publish(new
	 * PersistActionGroupAction()); bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
	 * bean.setDataSession(Session.persistenceSession.get());
	 * bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get()); return bean; } };
	 * 
	 * 
	 * private static Supplier<PersistHeaderBarAction> persistHeaderBarAction = new
	 * SessionScopedBeanProvider<PersistHeaderBarAction>() {
	 * 
	 * @Override public PersistHeaderBarAction create() throws Exception { PersistHeaderBarAction bean = publish(new
	 * PersistHeaderBarAction()); bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
	 * bean.setDataSession(Session.persistenceSession.get());
	 * bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get()); return bean; } };
	 * 
	 * private static Supplier<PersistVerticalTabAction> persistVerticalTabAction = new
	 * SessionScopedBeanProvider<PersistVerticalTabAction>() {
	 * 
	 * @Override public PersistVerticalTabAction create() throws Exception { PersistVerticalTabAction bean = publish(new
	 * PersistVerticalTabAction()); bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
	 * bean.setDataSession(Session.persistenceSession.get());
	 * bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get()); return bean; } };
	 * 
	 * private static Supplier<PersistGlobalActionsToolBarAction> persistGlobalActionsToAction = new
	 * SessionScopedBeanProvider<PersistGlobalActionsToolBarAction>() {
	 * 
	 * @Override public PersistGlobalActionsToolBarAction create() throws Exception { PersistGlobalActionsToolBarAction
	 * bean = publish(new PersistGlobalActionsToolBarAction());
	 * bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
	 * bean.setDataSession(Session.persistenceSession.get());
	 * bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get()); return bean; } };
	 * 
	 * private static Supplier<UiThemeAction> uiThemeAction = new SessionScopedBeanProvider<UiThemeAction>() {
	 * 
	 * @Override public UiThemeAction create() throws Exception { UiThemeAction bean = publish(new UiThemeAction());
	 * //bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
	 * bean.setWorkbenchSession(Session.persistenceSession.get());
	 * bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
	 * bean.setIcon(MenuClientBundle.INSTANCE.settings()); bean.setHoverIcon(MenuClientBundle.INSTANCE.settings());
	 * return bean; } };
	 * 
	 * private static Supplier<OpenModelAction> openModelAction = new SessionScopedBeanProvider<OpenModelAction>() {
	 * 
	 * @Override public OpenModelAction create() throws Exception { OpenModelAction bean = publish(new
	 * OpenModelAction()); bean.setBaseUrl(Runtime.modelViewBaseURL.get());
	 * bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
	 * bean.setMasterDetailConstellationProvider(Constellations.simpleMasterDetailConstellationProvider); return bean; }
	 * };
	 * 
	 * private static Supplier<CopyEntityToClipboardAction> copyEntityToClipboardAction = new
	 * SessionScopedBeanProvider<CopyEntityToClipboardAction>() { Override public CopyEntityToClipboardAction create()
	 * throws Exception { CopyEntityToClipboardAction bean = publish(new CopyEntityToClipboardAction());
	 * bean.setAssemblyPanel(Panels.assemblyPanelProvider.get()); return bean; } };
	 * 
	 * protected static Supplier<ExchangeContentViewAction> hiddenActionsExchangeContentViewAction = new
	 * PrototypeBeanProvider<ExchangeContentViewAction>() { Override public ExchangeContentViewAction create() throws
	 * Exception { ExchangeContentViewAction bean = abstractExchangeContentViewAction.get();
	 * bean.setContentViewContexts(hiddenActionsContentViewContexts.get()); return bean; } };
	 * 
	 * private static Supplier<List<GmContentViewContext>> hiddenActionsContentViewContexts = new
	 * PrototypeBeanProvider<List<GmContentViewContext>>() {
	 * 
	 * @SuppressWarnings({ "unchecked", "rawtypes" }) public List<GmContentViewContext> create() throws Exception {
	 * List<GmContentViewContext> bean = new ArrayList<GmContentViewContext>(); bean.add(new
	 * GmContentViewContext((Provider) Panels.hiddenActionsAssemblyPanelProvider, LocalizedText.INSTANCE.listView(),
	 * AssemblyPanelResources.INSTANCE.list64(), AssemblyPanelResources.INSTANCE.list64())); bean.add(new
	 * GmContentViewContext((Provider) Panels.editThumbnailPanelProvider, LocalizedText.INSTANCE.thumbnailView(),
	 * AssemblyPanelResources.INSTANCE.thumbs(), AssemblyPanelResources.INSTANCE.thumbsBig())); return bean; } }; */

}
