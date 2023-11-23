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

import com.braintribe.gwt.gme.constellation.client.MasterDetailConstellation;
import com.braintribe.gwt.gme.constellation.client.expert.SessionReadyLoader;
import com.braintribe.gwt.gme.gmactionbar.client.DefaultGmViewActionBar;
import com.braintribe.gwt.gme.propertypanel.client.PropertyPanel;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionBar;
import com.braintribe.gwt.gmview.client.GmEntityView;
import com.braintribe.gwt.gmview.client.TabbedGmEntityView;
import com.braintribe.gwt.gmview.client.TabbedGmEntityViewContext;
import com.braintribe.gwt.gmview.util.client.TypeIconProvider;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.modeller.client.GmModeller;
import com.braintribe.gwt.modeller.client.filter.GmModellerFilterPanel;
import com.braintribe.gwt.modeller.client.standalone.StandAloneModeler;
import com.braintribe.gwt.modeller.client.typesoverview.GmModellerTypesOverviewPanel;
import com.braintribe.gwt.modeller.client.view.GmModellerViewPanel;
import com.braintribe.gwt.simplepropertypanel.client.SimplePropertyPanel;
import com.braintribe.provider.PrototypeBeanProvider;
import com.braintribe.provider.SingletonBeanProvider;

public class ModelerNew {
	
	protected static Supplier<GmModeller> modeller = new PrototypeBeanProvider<GmModeller>() {
		@Override
		public GmModeller create() throws Exception {
			GmModeller bean = new GmModeller();
			bean.setTypesOverviewPanel(gmModellerTypesOverviewPanel.get());
			bean.setFilterPanel(gmModellerFilterPanel.get());
			bean.setViewPanel(gmModellerViewPanel.get());
			bean.setSmartMapper(Panels.smartMapper.get());
			bean.setActionManager(Controllers.modellerActionManager.get());
			bean.setQuickAccessPanelProvider(Panels.spotlightPanelProvider);
			return bean;
		}
	};
	
	private static Supplier<GmModeller> gmStandAloneModellerPanel = new SingletonBeanProvider<GmModeller>() {
		@Override
		public GmModeller create() throws Exception {
			GmModeller bean = publish(new GmModeller());
			bean.setTypesOverviewPanel(gmModellerTypesOverviewPanel.get());
			bean.setFilterPanel(gmModellerFilterPanel.get());
			bean.setViewPanel(gmModellerViewPanel.get());
			bean.setSmartMapper(Panels.smartMapper.get());
			bean.setActionManager(Controllers.standAloneModellerActionManager.get());
			bean.setQuickAccessPanelProvider(Panels.spotlightPanelProvider);
			bean.setReadOnly(readOnly);
//			bean.setOffline(true);
			return bean;
		}
	};
	
	private static Supplier<GmModellerTypesOverviewPanel> gmModellerTypesOverviewPanel = new PrototypeBeanProvider<GmModellerTypesOverviewPanel>() {
		@Override
		public GmModellerTypesOverviewPanel create() throws Exception {
			GmModellerTypesOverviewPanel bean = new GmModellerTypesOverviewPanel();
			bean.setSession(Session.persistenceSession.get());
			bean.setQuickAccessPanelProvider(Panels.spotlightPanelProvider);
			bean.setReadOnly(readOnly);
			return bean;
		}
	};
	
	private static Supplier<GmModellerFilterPanel> gmModellerFilterPanel = new PrototypeBeanProvider<GmModellerFilterPanel>() {
		@Override
		public GmModellerFilterPanel create() throws Exception {
			GmModellerFilterPanel bean = new GmModellerFilterPanel();
			bean.setSession(Session.persistenceSession.get());
			bean.setQuickAccessPanelProvider(Panels.spotlightPanelProvider);
			return bean;
		}
	};
	
	private static Supplier<GmModellerViewPanel> gmModellerViewPanel = new PrototypeBeanProvider<GmModellerViewPanel>() {
		@Override
		public GmModellerViewPanel create() throws Exception {
			GmModellerViewPanel bean = new GmModellerViewPanel();
			bean.setSession(Session.persistenceSession.get());
			bean.setReadOnly(readOnly);
			return bean;
		}
	};
		
	private static Supplier<MasterDetailConstellation> modellerMasterDetailConstellationProvider = new SingletonBeanProvider<MasterDetailConstellation>() {
		@Override
		public MasterDetailConstellation create() throws Exception {
			MasterDetailConstellation bean = publish(new MasterDetailConstellation());
			bean.setDefaultMasterViewProvider(gmStandAloneModellerPanel);
			bean.setDetailViewSupplier(tabbedPropertyPanelProvider);
			return bean;
		}
	};
	
	private static Supplier<TabbedGmEntityView> tabbedPropertyPanelProvider = new SingletonBeanProvider<TabbedGmEntityView>() {
		@Override
		public TabbedGmEntityView create() throws Exception {
			TabbedGmEntityView bean = publish(new TabbedGmEntityView());
//			bean.setGeneralPanel(Panels.generalPanel.get());
			bean.setTabbedGmEntityViewContexts(tabbedGmEntityViewContexts.get());
			//bean.setAction(Actions.detailsPanelVisibilityAction.get());
			return bean;
		}
	};
	
	private static Supplier<List<TabbedGmEntityViewContext>> tabbedGmEntityViewContexts = new SingletonBeanProvider<List<TabbedGmEntityViewContext>>() {
		@Override
		public List<TabbedGmEntityViewContext> create() throws Exception {
			List<TabbedGmEntityViewContext> bean = publish(new ArrayList<TabbedGmEntityViewContext>());
			
			TabbedGmEntityViewContext simplePropertyContext = new TabbedGmEntityViewContext("Properties", "Properties", simplePropertyPanel.get());
			TabbedGmEntityViewContext propertyPanelContext = new TabbedGmEntityViewContext(LocalizedText.INSTANCE.details(), LocalizedText.INSTANCE.details(), propertyPanelProvider.get());
			
			bean.add(propertyPanelContext);
			bean.add(simplePropertyContext);		
			
			return bean;
		}
	};
	
	private static Supplier<GmEntityView> simplePropertyPanel = new SingletonBeanProvider<GmEntityView>() {
		@Override
		public SimplePropertyPanel create() throws Exception {
			SimplePropertyPanel bean = (SimplePropertyPanel) publish(new SimplePropertyPanel());			
			if(Runtime.useCommit)
				bean.setCommitAction(Actions.saveAction.get());
			return bean;
		}
	};
	
	private static Supplier<PropertyPanel> propertyPanelProvider = new SingletonBeanProvider<PropertyPanel>() {
		@Override
		public PropertyPanel create() throws Exception {
			PropertyPanel bean = publish(Panels.abstractPropertyPanelProvider.get());
			//bean.setSelectionFutureProvider(Panels.gimaSelectionConstellationSupplier);
			bean.setIconProvider(typeIconProvider.get());
			bean.configureGmSession(Session.persistenceSession.get());
			bean.setSkipMetadataResolution(true);
			bean.setReadOnly(true);
			return bean;
		}
	};
	
	private static Supplier<TypeIconProvider> typeIconProvider = new PrototypeBeanProvider<TypeIconProvider>() {
		@Override
		public TypeIconProvider create() throws Exception {
			TypeIconProvider bean = new TypeIconProvider();
			bean.setDefaultProvider(null);
			bean.setTypeProvidersMap(Providers.typeProvidersMap.get());
			return bean;
		}
	};
		
	public static Supplier<StandAloneModeler> standAloneModeler = new SingletonBeanProvider<StandAloneModeler>() {
		@Override
		public StandAloneModeler create() throws Exception {
			StandAloneModeler bean = publish(new StandAloneModeler());
			bean.setMasterDetailConstellation(modellerMasterDetailConstellationProvider.get());
			bean.setActionBar(gmViewActionBar.get());
			return bean;
		}
	};
	
	private static Supplier<GmViewActionBar> gmViewActionBar = new SingletonBeanProvider<GmViewActionBar>() {
		@Override
		public DefaultGmViewActionBar create() throws Exception {
			DefaultGmViewActionBar bean = (DefaultGmViewActionBar) publish(new DefaultGmViewActionBar());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			bean.setGmSession(Session.persistenceSession.get());
			return bean;
		}
	};
	
	protected static Supplier<SessionReadyLoader> sessionReadyLoader = new SingletonBeanProvider<SessionReadyLoader>() {
		@Override
		public SessionReadyLoader create() throws Exception {
			SessionReadyLoader bean = publish(new SessionReadyLoader());
			bean.setLoaders(Arrays.asList(Controllers.standAloneModellerActionManager.get(), gmViewActionBar.get()));
			return bean;
		}
	};

	public static boolean readOnly;

}
