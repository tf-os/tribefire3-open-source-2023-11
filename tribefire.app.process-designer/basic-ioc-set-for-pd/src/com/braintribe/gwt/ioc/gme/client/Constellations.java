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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gwt.gm.storage.api.ColumnData;
import com.braintribe.gwt.gm.storage.api.StorageColumnInfo;
import com.braintribe.gwt.gme.constellation.client.ChangesConstellation;
import com.braintribe.gwt.gme.constellation.client.CustomizationConstellation;
import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.gme.constellation.client.HomeConstellation;
import com.braintribe.gwt.gme.constellation.client.MasterDetailConstellation;
import com.braintribe.gwt.gme.constellation.client.SelectionConstellation;
import com.braintribe.gwt.gme.constellation.client.SelectionConstellationScopedBeanProvider;
import com.braintribe.gwt.ioc.gme.client.resources.LocalizedText;
import com.braintribe.gwt.security.client.SessionScopedBeanProvider;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.provider.PrototypeBeanProvider;
import com.braintribe.provider.SingletonBeanProvider;
import com.braintribe.utils.i18n.I18nTools;

public class Constellations {
	
	protected static Supplier<CustomizationConstellation> customizationConstellationProvider = new SessionScopedBeanProvider<CustomizationConstellation>() {
		@Override
		public CustomizationConstellation create() throws Exception {
			CustomizationConstellation bean = publish(new CustomizationConstellation());
			bean.setAccessId(Runtime.accessId.get());
			bean.setApplicationId(Runtime.applicationId);
			bean.setExplorerConstellation(explorerConstellationProvider.get());
			bean.setPersistenceSession(Session.persistenceSession.get());
			bean.addModelEnvironmentSetListener(Providers.workbenchDataProvider.get());
			bean.addModelEnvironmentSetListener(Startup.localeProvider.get());
			bean.setShowHeader(false);
			bean.setModelEnvironmentProvider(Providers.modelEnvironmentProvider.get());
			bean.setTransientSession(Session.transientManagedSession.get());
			return bean;
		}
	};
	
	protected static Supplier<ExplorerConstellation> explorerConstellationProvider = new SessionScopedBeanProvider<ExplorerConstellation>() {
		@Override
		public ExplorerConstellation create() throws Exception {
			ExplorerConstellation bean = publish(new ExplorerConstellation());
			bean.setWorkbench(Panels.workbenchProvider.get());
			bean.setVerticalTabPanel(Panels.verticalTabPanelProvider.get());
			bean.setHomeConstellation(homeConstellationProvider.get());
			bean.setNotificationConstellation(Notifications.notificationsConstellationProvider.get());
			bean.setGmSession(Session.persistenceSession.get());
			bean.setTransientGmSession(Session.transientManagedSession.get());
			bean.setTransientSessionSupplier(Session.serviceRequestScopedTransientGmSession);
			bean.setUseCase(Runtime.assemblyPanelUseCaseProvider.get());
			bean.setViewSituationResolver(ViewSituationResolution.viewSituationResolver);
			bean.setWorkbenchActionHandlerRegistry(Actions.workbenchActionHandlerRegistry.get());
			bean.setUseWorkbenchWithinTab(Boolean.parseBoolean(Runtime.useWorkbenchWithinTab.get()));
			bean.setUseToolBar(false);
			return bean;
		}
	};
	
	protected static Supplier<SelectionConstellation> selectionConstellationProvider = new SelectionConstellationScopedBeanProvider<SelectionConstellation>() {
		@Override
		public SelectionConstellation create() throws Exception {
			SelectionConstellation bean = publish(new SelectionConstellation());
			bean.setVerticalTabPanel(Panels.verticalTabPanelProvider.get());
			bean.setHomeConstellation(selectionHomeConstellationProvider);
			bean.setChangesConstellation(changesConstellationSelectionProvider);
			bean.setUseCase(Runtime.selectionUseCaseProvider.get());
			bean.setWorkbenchActionHandlerRegistry(Actions.workbenchActionHandlerRegistry.get());
			bean.setSpotlightPanel(Panels.spotlightPanelWithoutTypesProvider.get());
			bean.setDetailPanelProvider(Panels.tabbedSelectionPropertyPanelProvider);
			bean.setTransientGmSession(Session.transientManagedSession.get());
			bean.setGmSession(Session.persistenceSession.get());
			bean.setParserWithPossibleValuesSupplier(Providers.parserWithPossibleValues);
			return bean;
		}
	};
	
	protected static Supplier<MasterDetailConstellation> errorMasterViewConstellationSupplier = new PrototypeBeanProvider<MasterDetailConstellation>() {
		@Override
		public MasterDetailConstellation create() throws Exception {
			MasterDetailConstellation bean = new MasterDetailConstellation();
			bean.configureReadOnly(true);
			bean.setExchangeContentViewAction(null);
			bean.configureGmSession(Session.transientManagedSession.get());
			bean.setColumnData(errorColumnData.get());
			return bean;
		}
	};
	
	private static Supplier<HomeConstellation> homeConstellationProvider = new SessionScopedBeanProvider<HomeConstellation>() {
		@Override
		public HomeConstellation create() throws Exception {
			HomeConstellation bean = publish(new HomeConstellation());
			bean.setDataSession(Session.persistenceSession.get());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			bean.setUserFullNameProvider(Providers.userFullNameProvider.get());
			bean.setEmptyTextMessage("");
			return bean;
		}
	};
	
	private static Supplier<HomeConstellation> selectionHomeConstellationProvider = new SelectionConstellationScopedBeanProvider<HomeConstellation>() {
		@Override
		public HomeConstellation create() throws Exception {
			HomeConstellation bean = publish(new HomeConstellation());
			bean.setDataSession(Session.persistenceSession.get());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			bean.setUserFullNameProvider(Providers.userFullNameProvider.get());
			return bean;
		}
	};
	
	private static Supplier<ChangesConstellation> changesConstellationSelectionProvider = new SelectionConstellationScopedBeanProvider<ChangesConstellation>() {
		@Override
		public ChangesConstellation create() throws Exception {
			ChangesConstellation bean = new ChangesConstellation();
			bean.setIgnoreTypes(ignoreTypes.get());
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
	
}
