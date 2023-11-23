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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gwt.gme.constellation.client.MasterDetailConstellation;
import com.braintribe.gwt.gme.constellation.client.action.ExchangeContentViewAction;
import com.braintribe.gwt.gme.notification.client.ClearNotificationsAction;
import com.braintribe.gwt.gme.notification.client.DynamicConfirmationDialog;
import com.braintribe.gwt.gme.notification.client.MessageDialog;
import com.braintribe.gwt.gme.notification.client.NotificationBar;
import com.braintribe.gwt.gme.notification.client.NotificationConstellation;
import com.braintribe.gwt.gme.notification.client.NotificationFactoryImpl;
import com.braintribe.gwt.gme.notification.client.NotificationView;
import com.braintribe.gwt.gme.notification.client.RemoveNotificationAction;
import com.braintribe.gwt.gme.notification.client.ShowNotificationsAction;
import com.braintribe.gwt.gme.notification.client.StaticConfirmationDialog;
import com.braintribe.gwt.gme.notification.client.expert.ActionResponseEventSourceExpert;
import com.braintribe.gwt.gme.notification.client.expert.ApplyManipulationExpert;
import com.braintribe.gwt.gme.notification.client.expert.CloseUrlCommandExpert;
import com.braintribe.gwt.gme.notification.client.expert.CommandExpertActionAdapter;
import com.braintribe.gwt.gme.notification.client.expert.CompoundCommandExpert;
import com.braintribe.gwt.gme.notification.client.expert.DefaultEventSourceExpert;
import com.braintribe.gwt.gme.notification.client.expert.DownloadResourceExpert;
import com.braintribe.gwt.gme.notification.client.expert.GotoModelPathCommandExpert;
import com.braintribe.gwt.gme.notification.client.expert.GotoUrlCommandExpert;
import com.braintribe.gwt.gme.notification.client.expert.InternalCommandExpert;
import com.braintribe.gwt.gme.notification.client.expert.RefreshPreviewExpert;
import com.braintribe.gwt.gme.notification.client.expert.ReloadViewExpert;
import com.braintribe.gwt.gme.notification.client.expert.RunQueryExpert;
import com.braintribe.gwt.gme.notification.client.expert.RunQueryStringExpert;
import com.braintribe.gwt.gme.notification.client.expert.RunWorkbenchActionExpert;
import com.braintribe.gwt.gme.notification.client.expert.UpdateWorkbenchFolderDisplayExpert;
import com.braintribe.gwt.gme.notification.client.resources.LocalizedText;
import com.braintribe.gwt.gme.notification.client.resources.NotificationResources;
import com.braintribe.gwt.gmview.client.GmContentViewContext;
import com.braintribe.gwt.logging.ui.gxt.client.GxtReasonErrorDialog;
import com.braintribe.gwt.security.client.SessionScopedBeanProvider;
import com.braintribe.model.command.CompoundCommand;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.notification.ActionResponseEventSource;
import com.braintribe.model.notification.InternalCommand;
import com.braintribe.model.notification.MessageNotification;
import com.braintribe.model.notification.NotificationEventSource;
import com.braintribe.model.processing.core.expert.api.GmExpertDefinition;
import com.braintribe.model.processing.core.expert.impl.ConfigurableGmExpertDefinition;
import com.braintribe.model.processing.core.expert.impl.ConfigurableGmExpertRegistry;
import com.braintribe.model.processing.notification.api.CommandExpert;
import com.braintribe.model.processing.notification.api.NotificationEventSourceExpert;
import com.braintribe.model.processing.notification.api.NotificationExpert;
import com.braintribe.model.uicommand.ApplyManipulation;
import com.braintribe.model.uicommand.CloseUrl;
import com.braintribe.model.uicommand.DownloadResource;
import com.braintribe.model.uicommand.GotoModelPath;
import com.braintribe.model.uicommand.GotoUrl;
import com.braintribe.model.uicommand.Refresh;
import com.braintribe.model.uicommand.RefreshPreview;
import com.braintribe.model.uicommand.Reload;
import com.braintribe.model.uicommand.ReloadView;
import com.braintribe.model.uicommand.RunQuery;
import com.braintribe.model.uicommand.RunQueryString;
import com.braintribe.model.uicommand.RunWorkbenchAction;
import com.braintribe.model.uicommand.UpdateWorkbenchFolderDisplay;
import com.braintribe.provider.PrototypeBeanProvider;
import com.braintribe.provider.SingletonBeanProvider;

public class Notifications {

	// ------------------------- Registry -------------------------

	private static Supplier<ConfigurableGmExpertRegistry> complexExpertRegistry = new SessionScopedBeanProvider<ConfigurableGmExpertRegistry>() {
		@Override
		public ConfigurableGmExpertRegistry create() throws Exception {
			ConfigurableGmExpertRegistry bean = publish(new ConfigurableGmExpertRegistry());
			// add command experts
			bean.setExpertDefinitions(Arrays.<GmExpertDefinition> asList(new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(GotoModelPath.class);
					setExpertType(CommandExpert.class);
					setExpert(gotoModelPathCommandExpert.get());
				}
			}, new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(GotoUrl.class);
					setExpertType(CommandExpert.class);
					setExpert(gotoUrlCommandExpert.get());
				}
			}, new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(CloseUrl.class);
					setExpertType(CommandExpert.class);
					setExpert(closeUrlCommandExpert.get());
				}
			}, new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(Refresh.class);
					setExpertType(CommandExpert.class);
					setExpert(refreshCommandExpert.get());
				}
			}, new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(Reload.class);
					setExpertType(CommandExpert.class);
					setExpert(reloadCommandExpert.get());
				}
			}, new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(InternalCommand.class);
					setExpertType(CommandExpert.class);
					setExpert(internalCommandExpert.get());
				}
			}, new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(RunWorkbenchAction.class);
					setExpertType(CommandExpert.class);
					setExpert(runWorkbenchActionExpert.get());
				}
			}, new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(RunQuery.class);
					setExpertType(CommandExpert.class);
					setExpert(runQueryExpert.get());
				}
			}, new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(RunQueryString.class);
					setExpertType(CommandExpert.class);
					setExpert(runQueryStringExpert.get());
				}
			}, new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(CompoundCommand.class);
					setExpertType(CommandExpert.class);
					setExpert(compoundCommandExpert.get());
				}
			}, new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(ApplyManipulation.class);
					setExpertType(CommandExpert.class);
					setExpert(applyManipulationExpert.get());
				}
			}, new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(ReloadView.class);
					setExpertType(CommandExpert.class);
					setExpert(reloadViewExpert.get());
				}
			}, new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(DownloadResource.class);
					setExpertType(CommandExpert.class);
					setExpert(downloadResourceExpert.get());
				}
			}, new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(RefreshPreview.class);
					setExpertType(CommandExpert.class);
					setExpert(refreshPreviewExpert.get());
				}
			}, new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(UpdateWorkbenchFolderDisplay.class);
					setExpertType(CommandExpert.class);
					setExpert(updateWorkbenchFolderDisplayExpert.get());
				}
			}));
			// add message experts
			bean.setExpertDefinitions(Arrays.<GmExpertDefinition> asList(new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(MessageNotification.class);
					setExpertType(NotificationExpert.class);
					setExpert(null /* inhibit MessageWithCommand */);
				}
			}/* , new ConfigurableGmExpertDefinition() { { setDenotationType(CommandNotification.class);
				 * setExpertType(NotificationExpert.class); setExpert(commandListener.get()); } } */));
			// add event source experts
			bean.setExpertDefinitions(Arrays.<GmExpertDefinition> asList(new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(NotificationEventSource.class);
					setExpertType(NotificationEventSourceExpert.class);
					setExpert(defaultEventSourceExpert.get());
				}
			}, new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(ActionResponseEventSource.class);
					setExpertType(NotificationEventSourceExpert.class);
					setExpert(actionResponseEventSourceExpert.get());
				}
			}));
			// done
			return bean;
		}
	};

	public static Supplier<ConfigurableGmExpertRegistry> expertRegistry = complexExpertRegistry;

	private static Supplier<GotoModelPathCommandExpert> gotoModelPathCommandExpert = new SessionScopedBeanProvider<GotoModelPathCommandExpert>() {
		@Override
		public GotoModelPathCommandExpert create() throws Exception {
			GotoModelPathCommandExpert bean = publish(new GotoModelPathCommandExpert());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			bean.setSpecialTypeExperts(specialTypeExperts.get());
			return bean;
		}
	};
	
	private static Supplier<Map<EntityType<?>, Consumer<ModelPath>>> specialTypeExperts = new SessionScopedBeanProvider<Map<EntityType<?>, Consumer<ModelPath>>>() {
		@Override
		public Map<EntityType<?>, Consumer<ModelPath>> create() throws Exception {
			Map<EntityType<?>, Consumer<ModelPath>> bean = publish(new HashMap<>());
			bean.put(Reason.T, modelPath -> GxtReasonErrorDialog.accept(modelPath));
			return bean;
		}
	};

	private static Supplier<GotoUrlCommandExpert> gotoUrlCommandExpert = new SessionScopedBeanProvider<GotoUrlCommandExpert>() {
		@Override
		public GotoUrlCommandExpert create() throws Exception {
			GotoUrlCommandExpert bean = publish(new GotoUrlCommandExpert());
			bean.setMasterDetailConstellationProvider(Constellations.simpleMasterDetailConstellationProvider);
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			bean.setEnvironmentProperties(com.braintribe.gwt.customizationui.client.startup.TribefireRuntime.getEnvProps());
			return bean;
		}
	};
	
	private static Supplier<CloseUrlCommandExpert> closeUrlCommandExpert = new SessionScopedBeanProvider<CloseUrlCommandExpert>() {
		@Override
		public CloseUrlCommandExpert create() throws Exception {
			CloseUrlCommandExpert bean = publish(new CloseUrlCommandExpert());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			return bean;
		}
	};

	private static Supplier<CommandExpertActionAdapter<Refresh>> refreshCommandExpert = new SessionScopedBeanProvider<CommandExpertActionAdapter<Refresh>>() {
		@Override
		public CommandExpertActionAdapter<Refresh> create() throws Exception {
			CommandExpertActionAdapter<Refresh> bean = publish(new CommandExpertActionAdapter<>());
			bean.setActionProvider(Actions.refreshEntitiesAction);
			return bean;
		}
	};

	private static Supplier<CommandExpertActionAdapter<Reload>> reloadCommandExpert = new SessionScopedBeanProvider<CommandExpertActionAdapter<Reload>>() {
		@Override
		public CommandExpertActionAdapter<Reload> create() throws Exception {
			CommandExpertActionAdapter<Reload> bean = publish(new CommandExpertActionAdapter<>());
			bean.setActionProvider(Actions.reloadSessionAction);
			return bean;
		}
	};

	private static Supplier<InternalCommandExpert> internalCommandExpert = new SessionScopedBeanProvider<InternalCommandExpert>() {
		@Override
		public InternalCommandExpert create() throws Exception {
			InternalCommandExpert bean = publish(new InternalCommandExpert());
			bean.setNotificationFactory(notificationFactory);
			return bean;
		}
	};

	private static Supplier<RunWorkbenchActionExpert> runWorkbenchActionExpert = new SessionScopedBeanProvider<RunWorkbenchActionExpert>() {
		@Override
		public RunWorkbenchActionExpert create() throws Exception {
			RunWorkbenchActionExpert bean = publish(new RunWorkbenchActionExpert());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			return bean;
		}
	};

	private static Supplier<RunQueryExpert> runQueryExpert = new SessionScopedBeanProvider<RunQueryExpert>() {
		@Override
		public RunQueryExpert create() throws Exception {
			RunQueryExpert bean = publish(new RunQueryExpert());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			return bean;
		}
	};

	private static Supplier<RunQueryStringExpert> runQueryStringExpert = new SessionScopedBeanProvider<RunQueryStringExpert>() {
		@Override
		public RunQueryStringExpert create() throws Exception {
			RunQueryStringExpert bean = publish(new RunQueryStringExpert());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			return bean;
		}
	};
	
	private static Supplier<CompoundCommandExpert> compoundCommandExpert = new SessionScopedBeanProvider<CompoundCommandExpert>() {
		@Override
		public CompoundCommandExpert create() throws Exception {
			CompoundCommandExpert bean = publish(new CompoundCommandExpert());
			bean.setCommandRegistry(complexExpertRegistry);
			return bean;
		}
	};

	private static Supplier<ApplyManipulationExpert> applyManipulationExpert = new SessionScopedBeanProvider<ApplyManipulationExpert>() {
		@Override
		public ApplyManipulationExpert create() throws Exception {
			ApplyManipulationExpert bean = publish(new ApplyManipulationExpert());
			bean.setDataSession(Session.persistenceSession.get());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			bean.setGmEditionViewSupport(Controllers.gmEditionViewController);
			return bean;
		}
	};

	private static Supplier<ReloadViewExpert> reloadViewExpert = new SessionScopedBeanProvider<ReloadViewExpert>() {
		@Override
		public ReloadViewExpert create() throws Exception {
			ReloadViewExpert bean = publish(new ReloadViewExpert());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			return bean;
		}
	};

	/* private static Supplier<CommandListener> commandListener = new SessionScopedBeanProvider<CommandListener>() {
	 * 
	 * @Override public CommandListener create() throws Exception { CommandListener bean = publish(new
	 * CommandListener()); bean.setExpertRegistry(expertRegistry.get());
	 * bean.setGmSession(Session.notificationManagedSession.get()); return bean; } }; */

	private static Supplier<DownloadResourceExpert> downloadResourceExpert = new SessionScopedBeanProvider<DownloadResourceExpert>() {
		@Override
		public DownloadResourceExpert create() throws Exception {
			DownloadResourceExpert bean = publish(new DownloadResourceExpert());
			bean.setSession(Session.persistenceSession.get());
			return bean;
		}
	};

	protected static Supplier<RefreshPreviewExpert> refreshPreviewExpert = new SingletonBeanProvider<RefreshPreviewExpert>() {
		@Override
		public RefreshPreviewExpert create() throws Exception {
			RefreshPreviewExpert bean = publish(new RefreshPreviewExpert());
			// bean.setSession(Session.persistenceSession.get());
			return bean;
		}
	};
	
	private static Supplier<UpdateWorkbenchFolderDisplayExpert> updateWorkbenchFolderDisplayExpert = new SingletonBeanProvider<UpdateWorkbenchFolderDisplayExpert>() {
		@Override
		public UpdateWorkbenchFolderDisplayExpert create() throws Exception {
			UpdateWorkbenchFolderDisplayExpert bean = publish(new UpdateWorkbenchFolderDisplayExpert());
			bean.setWorkbench(Panels.workbenchProvider.get());
			return bean;
		}
	};
	
	private static Supplier<DefaultEventSourceExpert> defaultEventSourceExpert = new SessionScopedBeanProvider<DefaultEventSourceExpert>() {
		@Override
		public DefaultEventSourceExpert create() throws Exception {
			DefaultEventSourceExpert bean = publish(new DefaultEventSourceExpert());
			bean.setDisplayName(LocalizedText.INSTANCE.eventSourceDisplayName());
			return bean;
		}
	};

	private static Supplier<ActionResponseEventSourceExpert> actionResponseEventSourceExpert = new SessionScopedBeanProvider<ActionResponseEventSourceExpert>() {
		@Override
		public ActionResponseEventSourceExpert create() throws Exception {
			ActionResponseEventSourceExpert bean = publish(new ActionResponseEventSourceExpert());
			return bean;
		}
	};

	// ------------------------- Factory -------------------------	
	public static Supplier<NotificationFactoryImpl> defaultNotificationFactory = new SessionScopedBeanProvider<NotificationFactoryImpl>() {
		@Override
		public NotificationFactoryImpl create() throws Exception {
			NotificationFactoryImpl bean = publish(new NotificationFactoryImpl());
			bean.setSession(Session.notificationManagedSession.get());
			bean.setListener(Actions.templateBasedNotificationListener);
			return bean;
		}
	};
	
	public static Supplier<NotificationFactoryImpl> notificationFactory = defaultNotificationFactory;
	

	// ------------------------- UiElements -------------------------

	/* Contains the notification-icon as a symbol - usually at the right top.
	 *
	 * private static Supplier<NotificationIcon> notificationIcon = new SessionScopedBeanProvider<NotificationIcon>() {
	 * 
	 * @Override public NotificationIcon create() throws Exception { NotificationIcon bean = publish(new
	 * NotificationIcon()); bean.setIcon(NotificationResources.INSTANCE.notificationsBig());
	 * bean.setShowNotificationsAction(showNotificationsAction.get());
	 * bean.setGmSession(Session.notificationManagedSession.get()); return bean; } }; */

	/**
	 * Contains the notification-bar - linked to the "global state label"
	 */
	protected static Supplier<NotificationBar> notificationBar = new SessionScopedBeanProvider<NotificationBar>() {
		@Override
		public NotificationBar create() throws Exception {
			NotificationBar bean = publish(new NotificationBar());
			bean.setGlobalStateLabel(UiElements.globaleStateLabel.get());
			bean.setGmSession(Session.notificationManagedSession.get());
			bean.setShowNotificationsAction(showNotificationsAction);
			bean.setCommandRegistry(expertRegistry);
			bean.setNotificationFactory(notificationFactory);
			return bean;
		}
	};
	
	protected static Supplier<NotificationBar> webReaderNotificationBar = new SessionScopedBeanProvider<NotificationBar>() {
		@Override
		public NotificationBar create() throws Exception {
			NotificationBar bean = publish(new NotificationBar());
			bean.setGlobalStateLabel(UiElements.webReaderGlobalStateLabel.get());
			bean.setGmSession(Session.notificationManagedSession.get());
			bean.setNotificationFactory(notificationFactory);
			return bean;
		}
	};

	// ------------------------- Constellations -------------------------

	protected static Supplier<NotificationConstellation> notificationsConstellationProvider = new SessionScopedBeanProvider<NotificationConstellation>() {
		@Override
		public NotificationConstellation create() throws Exception {
			NotificationConstellation bean = publish(new NotificationConstellation());
			bean.setMasterDetailConstellationProvider(notificationMasterDetailConstellationProvider);
			bean.setGmSession(Session.notificationManagedSession.get());
			bean.setDataSession(Session.persistenceSession.get());
			bean.setVerticalTabActionBar(Panels.constellationActionBarProvider.get());
			bean.setConstellationDefaultModelActions(Actions.constellationSimpleActions.get());
			return bean;
		}
	};

	private static Supplier<MasterDetailConstellation> notificationMasterDetailConstellationProvider = new PrototypeBeanProvider<MasterDetailConstellation>() {
		@Override
		public MasterDetailConstellation create() throws Exception {
			MasterDetailConstellation bean = new MasterDetailConstellation();
			bean.setDefaultMasterViewProvider(notificationViewProvider);
			bean.setDetailViewSupplier(Panels.tabbedReadOnlyPropertyPanelProvider);
			// bean.setGlobalActionsToolBar(Constellations.globalActionsToolBar.get());
			bean.setExchangeContentViewAction(notificationECVA.get());
			// bean.setShowDetailView(false);
			return bean;
		}
	};

	// some fixes
	private static Supplier<ExchangeContentViewAction> notificationECVA = new PrototypeBeanProvider<ExchangeContentViewAction>() {
		@Override
		public ExchangeContentViewAction create() throws Exception {
			ExchangeContentViewAction bean = new ExchangeContentViewAction();
			// fix for .provideGmContentViewContext()
			bean.setViewSituationResolver(ViewSituationResolution.viewSituationResolver);
			// fix for .provideAndExchangeView()
			bean.setExternalContentViewContexts(Collections.singletonList(new GmContentViewContext(notificationViewProvider, null, null, true)));
			bean.setGmViewActionBar(Panels.gmViewActionBar);
			return bean;
		}
	};

	// ------------------------- Panels -------------------------

	private static Supplier<NotificationView> notificationViewProvider = new PrototypeBeanProvider<NotificationView>() {
		@Override
		public NotificationView create() throws Exception {
			NotificationView bean = new NotificationView();
			bean.setCommandRegistry(Notifications.expertRegistry);
			bean.setActionManager(Controllers.actionManager.get());
			bean.setRemoveNotificationAction(removeNotificationAction.get());
			bean.setClearNotificationsAction(clearNotificationsAction.get());
			return bean;
		}
	};
	
	protected static Supplier<StaticConfirmationDialog> staticConfirmationDialog = new SingletonBeanProvider<StaticConfirmationDialog>() {
		@Override
		public StaticConfirmationDialog create() throws Exception {
			StaticConfirmationDialog bean = publish(new StaticConfirmationDialog());
			bean.setResourceSession(Session.workbenchPersistenceSession.get());
			return bean;
		}
	};
	
	protected static Supplier<DynamicConfirmationDialog> dynamicConfirmationDialog = new SingletonBeanProvider<DynamicConfirmationDialog>() {
		@Override
		public DynamicConfirmationDialog create() throws Exception {
			DynamicConfirmationDialog bean = publish(new DynamicConfirmationDialog());
			bean.setResourceSession(Session.workbenchPersistenceSession.get());
			return bean;
		}
	};
	
	protected static Supplier<MessageDialog> messageDialog = new SingletonBeanProvider<MessageDialog>() {
		@Override
		public MessageDialog create() throws Exception {
			MessageDialog bean = publish(new MessageDialog());
			bean.setResourceSession(Session.workbenchPersistenceSession.get());
			return bean;
		}
	};

	// ------------------------- Actions -------------------------

	private static Supplier<ShowNotificationsAction> showNotificationsAction = new SingletonBeanProvider<ShowNotificationsAction>() {
		@Override
		public ShowNotificationsAction create() throws Exception {
			ShowNotificationsAction bean = publish(new ShowNotificationsAction());
			bean.setName("");
			bean.setTooltip(LocalizedText.INSTANCE.notifications());
			bean.setIcon(NotificationResources.INSTANCE.notificationsOrange());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			bean.setNotificationsConstellation(notificationsConstellationProvider.get());
			return bean;
		}
	};
	
	private static Supplier<RemoveNotificationAction> removeNotificationAction = new PrototypeBeanProvider<RemoveNotificationAction>() {
		@Override
		public RemoveNotificationAction create() throws Exception {
			RemoveNotificationAction bean = new RemoveNotificationAction();
			return bean;
		}
	};

	private static Supplier<ClearNotificationsAction> clearNotificationsAction = new PrototypeBeanProvider<ClearNotificationsAction>() {
		@Override
		public ClearNotificationsAction create() throws Exception {
			ClearNotificationsAction bean = new ClearNotificationsAction();
			return bean;
		}
	};
	
	// ------------------------- END -------------------------
}
