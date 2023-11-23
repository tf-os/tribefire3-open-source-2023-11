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
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.gwt.gm.storage.expert.impl.wb.WbQueryStorageExpert;
import com.braintribe.gwt.gme.constellation.client.expert.CurrentContentViewProvider;
import com.braintribe.gwt.gme.constellation.client.expert.EntityTypeInstantiationActionsProvider;
import com.braintribe.gwt.gme.constellation.client.expert.ModelEnvironmentProvider;
import com.braintribe.gwt.gme.constellation.client.expert.ModelEnvironmentUtil;
import com.braintribe.gwt.gme.constellation.client.expert.PackagingProvider;
import com.braintribe.gwt.gme.constellation.client.expert.SelectListEntryLoader;
import com.braintribe.gwt.gme.constellation.client.expert.UserProvider;
import com.braintribe.gwt.gme.cssresources.client.FavIconCssLoader;
import com.braintribe.gwt.gme.cssresources.client.TitleCssLoader;
import com.braintribe.gwt.gme.uitheme.client.UiThemeCssLoader;
import com.braintribe.gwt.gme.workbench.client.FolderIconProvider;
import com.braintribe.gwt.gme.workbench.client.FolderIconProvider.IconSize;
import com.braintribe.gwt.gmview.action.client.ObjectAssignmentActionDialog;
import com.braintribe.gwt.gmview.client.IconProvider;
import com.braintribe.gwt.gmview.client.parse.ParserWithPossibleValues;
import com.braintribe.gwt.gmview.util.client.MetaDataIconProvider;
import com.braintribe.gwt.gmview.util.client.MetaDataIconProvider.Mode;
import com.braintribe.gwt.gmview.util.client.TypeIconProvider;
import com.braintribe.gwt.ioc.gme.client.expert.FolderIconsRasterImageProvider;
import com.braintribe.gwt.ioc.gme.client.expert.bootstrapping.AvailableAccessesDataProvider;
import com.braintribe.gwt.ioc.gme.client.expert.bootstrapping.CurrentUserDataProvider;
import com.braintribe.gwt.ioc.gme.client.expert.bootstrapping.ModelEnvironmentDataProvider;
import com.braintribe.gwt.ioc.gme.client.expert.bootstrapping.WorkbenchDataProvider;
import com.braintribe.gwt.resourceuploadui.client.GmeDragAndDropSupportImpl;
import com.braintribe.gwt.resourceuploadui.client.resources.ResourceUploadResources;
import com.braintribe.gwt.security.client.RolesProvider;
import com.braintribe.gwt.security.client.SessionIdProvider;
import com.braintribe.gwt.security.client.SessionScopedBeanProvider;
import com.braintribe.gwt.security.client.UserFullNameProvider;
import com.braintribe.gwt.security.client.UserNameProvider;
import com.braintribe.gwt.utils.client.FastMap;
import com.braintribe.model.folder.Folder;
import com.braintribe.provider.PrototypeBeanProvider;
import com.braintribe.provider.SingletonBeanProvider;

public class Providers {
	
	protected static Supplier<UserNameProvider> userNameProvider = new SessionScopedBeanProvider<UserNameProvider>() {
		@Override
		public UserNameProvider create() throws Exception {
			UserNameProvider bean = new UserNameProvider();
			bean.setSecurityService(Services.securityService.get());
			return bean;
		}
	};
	
	protected static Supplier<UserFullNameProvider> userFullNameProvider = new SessionScopedBeanProvider<UserFullNameProvider>() {
		@Override
		public UserFullNameProvider create() throws Exception {
			UserFullNameProvider bean = new UserFullNameProvider();
			bean.setSecurityService(Services.securityService.get());
			return bean;
		}
	};
	
	protected static Supplier<RolesProvider> rolesProvider = new SessionScopedBeanProvider<RolesProvider>() {
		@Override
		public RolesProvider create() throws Exception {
			RolesProvider bean = publish(new RolesProvider());
			bean.setSecurityService(Services.securityService.get());
			return bean;
		}
	};
	
	protected static Supplier<SessionIdProvider> sessionIdProvider = new SingletonBeanProvider<SessionIdProvider>() {
		@Override
		public SessionIdProvider create() throws Exception {
			SessionIdProvider bean = publish(new SessionIdProvider());
			bean.setSecurityService(Services.securityService.get());
			return bean;
		}
	};

	protected static Supplier<WbQueryStorageExpert> wbQueryStorageExpertProvider = new SingletonBeanProvider<WbQueryStorageExpert>() {
		@Override
		public WbQueryStorageExpert create() throws Exception {
			WbQueryStorageExpert bean = new WbQueryStorageExpert();
			return bean;
		}
	};

	protected static Supplier<TypeIconProvider> typeIconProvider = new PrototypeBeanProvider<TypeIconProvider>() {
		@Override
		public TypeIconProvider create() throws Exception {
			TypeIconProvider bean = new TypeIconProvider();
			bean.setDefaultProvider(largestMetaDataIconProvider.get());
			bean.setTypeProvidersMap(typeProvidersMap.get());
			return bean;
		}
	};
	
	protected static Supplier<TypeIconProvider> apTypeIconProvider = new PrototypeBeanProvider<TypeIconProvider>() {
		@Override
		public TypeIconProvider create() throws Exception {
			TypeIconProvider bean = new TypeIconProvider();
			bean.setDefaultProvider(largestMetaDataIconProvider.get());
			bean.setTypeProvidersMap(apTypeProvidersMap.get());
			return bean;
		}
	};
	
	protected static Supplier<TypeIconProvider> templateTypeIconProvider = new PrototypeBeanProvider<TypeIconProvider>() {
		@Override
		public TypeIconProvider create() throws Exception {
			TypeIconProvider bean = new TypeIconProvider();
			bean.setTypeProvidersMap(typeProvidersMap.get());
			return bean;
		}
	};
	
	protected static Supplier<TypeIconProvider> bigTypeIconProvider = new PrototypeBeanProvider<TypeIconProvider>() {
		@Override
		public TypeIconProvider create() throws Exception {
			TypeIconProvider bean = new TypeIconProvider();
			bean.setDefaultProvider(bigMetaDataIconProvider.get());
			bean.setTypeProvidersMap(bigTypeProvidersMap.get());
			return bean;
		}
	};
	
	protected static Supplier<TypeIconProvider> largestTypeIconProvider = new PrototypeBeanProvider<TypeIconProvider>() {
		@Override
		public TypeIconProvider create() throws Exception {
			TypeIconProvider bean = new TypeIconProvider();
			bean.setDefaultProvider(largestMetaDataIconProvider.get());
			bean.configureGmSession(Session.persistenceSession.get());
			bean.setTypeProvidersMap(bigTypeProvidersMap.get());
			return bean;
		}
	};
	
	protected static Supplier<Map<String, IconProvider>> typeProvidersMap = new PrototypeBeanProvider<Map<String, IconProvider>>() {
		@Override
		public Map<String, IconProvider> create() throws Exception {
			Map<String, IconProvider> bean = new FastMap<IconProvider>();
			bean.put(Folder.T.getTypeSignature(), folderIconProvider.get());
			//bean.put(Resource.T.getTypeSignature(), resourceIconProvider.get()); //GSC: that doesn't work properly, so better turn it off.
			return bean;
		}
	};
	
	private static Supplier<Map<String, IconProvider>> apTypeProvidersMap = new PrototypeBeanProvider<Map<String, IconProvider>>() {
		@Override
		public Map<String, IconProvider> create() throws Exception {
			Map<String, IconProvider> bean = new FastMap<IconProvider>();
			bean.put(Folder.T.getTypeSignature(), folderIconProvider.get());
			return bean;
		}
	};
	
	private static Supplier<Map<String, IconProvider>> bigTypeProvidersMap = new PrototypeBeanProvider<Map<String, IconProvider>>() {
		@Override
		public Map<String, IconProvider> create() throws Exception {
			Map<String, IconProvider> bean = new FastMap<IconProvider>();
			bean.put(Folder.T.getTypeSignature(), bigFolderIconProvider.get());
			return bean;
		}
	};
	
	private static Supplier<FolderIconProvider> folderIconProvider = new PrototypeBeanProvider<FolderIconProvider>() {
		@Override
		public FolderIconProvider create() throws Exception {
			FolderIconProvider bean = new FolderIconProvider();
			return bean;
		}
	};
	
	private static Supplier<FolderIconProvider> bigFolderIconProvider = new PrototypeBeanProvider<FolderIconProvider>() {
		@Override
		public FolderIconProvider create() throws Exception {
			FolderIconProvider bean = new FolderIconProvider();
			bean.setIconSizes(Arrays.asList(IconSize.medium, IconSize.small));
			return bean;
		}
	};
	
	private static Supplier<MetaDataIconProvider> bigMetaDataIconProvider = new PrototypeBeanProvider<MetaDataIconProvider>() {
		@Override
		public MetaDataIconProvider create() throws Exception {
			MetaDataIconProvider bean = new MetaDataIconProvider();
			bean.setMaxHeight(32);
			return bean;
		}
	};
	
	private static Supplier<MetaDataIconProvider> largestMetaDataIconProvider = new PrototypeBeanProvider<MetaDataIconProvider>() {
		@Override
		public MetaDataIconProvider create() throws Exception {
			MetaDataIconProvider bean = new MetaDataIconProvider();
			bean.configureGmSession(Session.persistenceSession.get());
			bean.setMode(Mode.largest);
			return bean;
		}
	};
	
	protected static Supplier<FolderIconsRasterImageProvider> folderIconsRasterImageProvider = new SessionScopedBeanProvider<FolderIconsRasterImageProvider>() {
		@Override
		public FolderIconsRasterImageProvider create() throws Exception {
			FolderIconsRasterImageProvider bean = publish(new FolderIconsRasterImageProvider());
			return bean;
		}
	};
	
	protected static Supplier<ObjectAssignmentActionDialog> newInstanceProvider = new SessionScopedBeanProvider<ObjectAssignmentActionDialog>() {
		@Override
		public ObjectAssignmentActionDialog create() throws Exception {
			ObjectAssignmentActionDialog bean = publish(new ObjectAssignmentActionDialog());
			bean.setSpotlightPanel(Panels.spotlightPanelNewInstanceProvider.get());
			return bean;
		}
	};
	
	//GuiSettingProvider	
	protected static Supplier<UiThemeCssLoader> uiThemeLoader = new SingletonBeanProvider<UiThemeCssLoader>() {
		@Override
		public UiThemeCssLoader create() throws Exception {
			UiThemeCssLoader bean = publish(new UiThemeCssLoader());
			bean.setUiThemeUrl(Runtime.uiThemeUrlRuntime.get());
			return bean;
		}
	};
	
	// FavIconProvider
	protected static Supplier<FavIconCssLoader> favIconLoader = new SingletonBeanProvider<FavIconCssLoader>() {
		@Override
		public FavIconCssLoader create() throws Exception {
			FavIconCssLoader bean = publish(new FavIconCssLoader());
			bean.setFavIconUrl(Runtime.gmeFavIconUrlRuntime.get());
			return bean;
		}
	};

	// TitleProvider
	protected static Supplier<TitleCssLoader> titleLoader = new SingletonBeanProvider<TitleCssLoader>() {
		@Override
		public TitleCssLoader create() throws Exception {
			TitleCssLoader bean = publish(new TitleCssLoader());
			bean.setTitleUrl(Runtime.gmeTitleUrlRuntime.get());
			return bean;
		}
	};
	
	protected static Supplier<String> userAccessId = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {
			return "auth";
		}
	};
	
	public static Supplier<PackagingProvider> packagingProvider = new SingletonBeanProvider<PackagingProvider>() {
		@Override
		public PackagingProvider create() throws Exception {
			PackagingProvider bean = publish(new PackagingProvider());
			bean.setClientUrl(Runtime.tribefireExplorerUrl.get());
			return bean;
		}
	};
	
	protected static Supplier<ModelEnvironmentProvider> modelEnvironmentProvider = new SingletonBeanProvider<ModelEnvironmentProvider>() {
		@Override
		public ModelEnvironmentProvider create() throws Exception {
			ModelEnvironmentProvider bean = publish(new ModelEnvironmentProvider());
			bean.setModelEnvironmentFutureProvider(modelEnvironmentFutureProvider.get());
			return bean;
		}
	};
	
	protected static Supplier<UserProvider> userProvider = new SessionScopedBeanProvider<UserProvider>() {
		@Override
		public UserProvider create() throws Exception {
			UserProvider bean = publish(new UserProvider());
			bean.setCurrentUserInformationFutureProvider(currentUserInformationFutureProvider.get());
			return bean;
		}
	};
	
	protected static Supplier<AvailableAccessesDataProvider> availableAccessesDataProvider = new SessionScopedBeanProvider<AvailableAccessesDataProvider>() {
		@Override
		public AvailableAccessesDataProvider create() throws Exception {
			AvailableAccessesDataProvider bean = publish(new AvailableAccessesDataProvider());
			bean.setBootstrappingRequest(Requests.bootstrappingRequest.get());
			return bean;
		}
	};
	
	private static Supplier<CurrentUserDataProvider> currentUserInformationFutureProvider = new SessionScopedBeanProvider<CurrentUserDataProvider>() {
		@Override
		public CurrentUserDataProvider create() throws Exception {
			CurrentUserDataProvider bean = publish(new CurrentUserDataProvider());
			bean.setBootstrappingRequest(Requests.bootstrappingRequest.get());
			return bean;
		}
	};
	
	private static Supplier<ModelEnvironmentDataProvider> modelEnvironmentFutureProvider = new SessionScopedBeanProvider<ModelEnvironmentDataProvider>() {
		@Override
		public ModelEnvironmentDataProvider create() throws Exception {
			ModelEnvironmentDataProvider bean = publish(new ModelEnvironmentDataProvider());
			bean.setBootstrappingRequest(Requests.bootstrappingRequest.get());
			return bean;
		}
	};
	
	protected static Supplier<WorkbenchDataProvider> workbenchDataProvider = new SessionScopedBeanProvider<WorkbenchDataProvider>() {
		@Override
		public WorkbenchDataProvider create() throws Exception {
			WorkbenchDataProvider bean = publish(new WorkbenchDataProvider());
			bean.setGmSession(Session.persistenceSession.get());
			return bean;
		}
	};
	
	protected static Supplier<CurrentContentViewProvider> currentContentViewProvider = new SessionScopedBeanProvider<CurrentContentViewProvider>() {
		@Override
		public CurrentContentViewProvider create() throws Exception {
			CurrentContentViewProvider bean = publish(new CurrentContentViewProvider());
			if(Runtime.useCommit)
				bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			return bean;
		}
	};
	
	protected static Supplier<SelectListEntryLoader> dynamicEntriesLoader = new SessionScopedBeanProvider<SelectListEntryLoader>() {
		@Override
		public SelectListEntryLoader create() throws Exception {
			SelectListEntryLoader bean = publish(new SelectListEntryLoader());
			bean.setDataSession(Session.persistenceSession.get());
			bean.setTransientSession(Session.transientManagedSession.get());
			bean.setTransientSessionSupplier(Session.prototypeTransientManagedSession);
			bean.setNotificationFactory(Notifications.notificationFactory);
			return bean;
		}
	};
	
	protected static Supplier<ParserWithPossibleValues> parserWithPossibleValues = new SingletonBeanProvider<ParserWithPossibleValues>() {
		@Override
		public ParserWithPossibleValues create() throws Exception {
			ParserWithPossibleValues bean = new ParserWithPossibleValues();
			bean.setLocaleProvider(Startup.localeProvider.get());
			return bean;
		}
	};
	
	protected static Supplier<EntityTypeInstantiationActionsProvider> entityTypeInstantiationActionsProvider = new SessionScopedBeanProvider<EntityTypeInstantiationActionsProvider>() {
		@Override
		public EntityTypeInstantiationActionsProvider create() throws Exception {
			EntityTypeInstantiationActionsProvider bean = publish(new EntityTypeInstantiationActionsProvider());
			bean.setGmSession(Session.persistenceSession.get());
			bean.setInstantiationActionsSupplier(Panels.instantiationActionsProvider.get());
			return bean;
		}
	};
	
	protected static Supplier<GmeDragAndDropSupportImpl> gmeDragAndDropSupport = new SessionScopedBeanProvider<GmeDragAndDropSupportImpl>() {
		@Override
		public GmeDragAndDropSupportImpl create() throws Exception {
			GmeDragAndDropSupportImpl bean = publish(new GmeDragAndDropSupportImpl());
			bean.setResourceBuilder(Panels.restBasedResourceProcessingProvider.get());
			bean.setWorkbenchActionHandlerRegistry(Actions.workbenchActionHandlerRegistry.get());
			bean.setGlobalActionsToolBar(Constellations.globalActionsToolBar.get());
			bean.setTemplateActionsSupplier(Panels.gmViewActionBar.get());
			bean.setInstantiationActionsSupplier(entityTypeInstantiationActionsProvider.get());
			bean.setProgressParentContainer(Constellations.customizationConstellationProvider.get());
			bean.setCssResource(ResourceUploadResources.INSTANCE.css());
			bean.setWorkbenchActionSelectionHandler(Constellations.explorerConstellationProvider.get());
			return bean;
		}
	};

	public static Supplier<ModelEnvironmentUtil> modelEnvironmentUtil = new SessionScopedBeanProvider<ModelEnvironmentUtil>() {
		@Override
		public ModelEnvironmentUtil create() throws Exception {
			ModelEnvironmentUtil bean = publish(new ModelEnvironmentUtil());
			bean.setGmSession(Session.persistenceSession.get());
			bean.setTransientSession(Session.transientManagedSession.get());
			bean.setModelEnvironmentProvider(modelEnvironmentProvider.get());
			bean.setModelEnvironmentDrivenSessionUpdater(Controllers.modelEnvironmentDrivenSessionUpdater.get());
			return bean;
		}
	};
	
	/*
	private static Supplier<ResourceIconProvider> resourceIconProvider = new SingletonBeanProvider<ResourceIconProvider>() {
		@Override
		public ResourceIconProvider create() throws Exception {
			ResourceIconProvider bean = new ResourceIconProvider();
			return bean;
		}
	};
	
	protected static Supplier<NewInstanceProvider> newInstanceProvider = new SessionScopedBeanProvider<NewInstanceProvider>() {
		public NewInstanceProvider create() throws Exception {
			NewInstanceProvider bean = publish(new NewInstanceProvider());
			bean.setIconProvider(Providers.typeIconProvider.get());
			return bean;
		}
	};
	
	protected static Supplier<NewInstanceProvider> workbenchNewInstanceProvider = new SessionScopedBeanProvider<NewInstanceProvider>() {
		public NewInstanceProvider create() throws Exception {
			NewInstanceProvider bean = publish(new NewInstanceProvider());
			bean.setIconProvider(Providers.workbenchTypeIconProvider.get());
			return bean;
		}
	};
	
	private static Supplier<ObjectAssignmentActionDialog> workbenchNewInstanceProvider = new SessionScopedBeanProvider<ObjectAssignmentActionDialog>() {
		@Override
		public ObjectAssignmentActionDialog create() throws Exception {
			ObjectAssignmentActionDialog bean = publish(new ObjectAssignmentActionDialog());
			bean.setSpotlightPanel(Panels.workbenchSpotlightPanelNewInstanceProvider.get());
			return bean;
		}
	};*/
	
}
