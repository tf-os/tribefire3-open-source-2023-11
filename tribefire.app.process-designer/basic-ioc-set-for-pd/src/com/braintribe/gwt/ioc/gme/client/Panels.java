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
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.filter.lcd.pattern.CamelCasePatternMatcher;
import com.braintribe.filter.lcd.pattern.SubstringCheckingPatternMatcher;
import com.braintribe.gwt.gme.constellation.client.gima.GIMASelectionConstellation;
import com.braintribe.gwt.gme.constellation.client.gima.GIMASelectionContentView;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabActionMenu;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabPanel;
import com.braintribe.gwt.gme.workbench.client.Workbench;
import com.braintribe.gwt.gmview.action.client.InstantiationActionsProvider;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.gmview.client.TabbedGmEntityView;
import com.braintribe.gwt.gmview.client.TabbedGmEntityViewContext;
import com.braintribe.gwt.gmview.client.parse.SimpleTypeParser;
import com.braintribe.gwt.security.client.SessionScopedBeanProvider;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.resource.Icon;
import com.braintribe.provider.PrototypeBeanProvider;
import com.braintribe.provider.SingletonBeanProvider;

public class Panels {
	
	protected static Supplier<Workbench> workbenchProvider = new SessionScopedBeanProvider<Workbench>() {
		@Override
		public Workbench create() throws Exception {
			Workbench bean = publish(new Workbench());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			bean.setDataSession(Session.persistenceSession.get());
			bean.setTransientSession(Session.transientManagedSession.get());
			bean.setRootFolderName(Runtime.workbenchRootFolderName.get());
			return bean;
		}
	};
	
	protected static Supplier<VerticalTabPanel> verticalTabPanelProvider = new PrototypeBeanProvider<VerticalTabPanel>() {
		@Override
		public VerticalTabPanel create() throws Exception {
			VerticalTabPanel bean = new VerticalTabPanel();
			bean.setMaxNumberOfNonStaticElements(15);
			bean.setDisplayIconsForNonStaticElements(true);
			return bean;
		}
	};
	
	protected static Supplier<VerticalTabActionMenu> constellationActionBarProvider = new PrototypeBeanProvider<VerticalTabActionMenu>() {
		@Override
		public VerticalTabActionMenu create() throws Exception {
			VerticalTabActionMenu bean = new VerticalTabActionMenu();
			bean.setReuseWorkbenchActionContextTabElement(false);
			bean.setClosableItems(false);
			bean.setAlwaysFireElement(true);
			bean.setUseHorizontalTabs(true);
			return bean;
		}
	};
	
	protected static Supplier<Map<Class<?>, TraversingCriterion>> specialEntityTraversingCriterionMap = new SingletonBeanProvider<Map<Class<?>,TraversingCriterion>>() {
		@Override
		public Map<Class<?>, TraversingCriterion> create() throws Exception {
			Map<Class<?>, TraversingCriterion> bean = publish(new HashMap<>());
			bean.put(LocalizedString.class, TC.create().negation().joker().done());
			bean.put(Icon.class, TC.create().negation().joker().done());
			return bean;
		}
	};
	
	protected static Supplier<GIMASelectionConstellation> gimaSelectionConstellationSupplier = new PrototypeBeanProvider<GIMASelectionConstellation>() {
		@Override
		public GIMASelectionConstellation create() throws Exception {
			GIMASelectionConstellation bean = new GIMASelectionConstellation();
			bean.setSelectionContentView(selectionContentViewSupplier.get());
			return bean;
		}
	};
	
	protected static Supplier<SpotlightPanel> spotlightPanelWithoutTypesProvider = new PrototypeBeanProvider<SpotlightPanel>() {
		@Override
		public SpotlightPanel create() throws Exception {
			SpotlightPanel bean = spotlightPanelProvider.get();
			bean.setLoadTypes(false);
			bean.setDisplaySimpleQueryActions(false);
			bean.configureUseQueryActions(false);
			bean.configureEnableInstantiation(false);
			bean.setExpertUIMap(null);
			return bean;
		}
	};
	
	protected static Supplier<TabbedGmEntityView> tabbedSelectionPropertyPanelProvider = new PrototypeBeanProvider<TabbedGmEntityView>() {
		@Override
		public TabbedGmEntityView create() throws Exception {
			TabbedGmEntityView bean = new TabbedGmEntityView();
			bean.setTabbedGmEntityViewContexts(selectionPropertyPanelTabbedGmEntityViewContexts.get());
			return bean;
		}
	};
	
	protected static Supplier<SpotlightPanel> spotlightPanelNewInstanceProvider = new PrototypeBeanProvider<SpotlightPanel>() {
		@Override
		public SpotlightPanel create() throws Exception {
			SpotlightPanel bean = spotlightPanelProvider.get();
			bean.setShowTemplates(false);
			return bean;
		}
	};
	
	protected static Supplier<SpotlightPanel> spotlightPanelProvider = new PrototypeBeanProvider<SpotlightPanel>() {
		@Override
		public SpotlightPanel create() throws Exception {
			SpotlightPanel bean = new SpotlightPanel();
			bean.setSimpleTypesValuesProvider(simpleTypeParserProvider);
			bean.setGmSession(Session.persistenceSession.get());
			bean.setCodecRegistry(Codecs.renderersCodecRegistry.get());
			bean.setPatternMatchers(Arrays.asList(new SubstringCheckingPatternMatcher(), new CamelCasePatternMatcher()));
			bean.setIconProvider(Providers.typeIconProvider);
			bean.setUseCase(Runtime.quickAccessPanelUseCaseProvider.get());
			bean.setShowAbstractTypes(true);
			return bean;
		}
	};
	
	protected static Supplier<InstantiationActionsProvider> instantiationActionsProvider = new SessionScopedBeanProvider<InstantiationActionsProvider>() {
		@Override
		public InstantiationActionsProvider create() throws Exception {
			InstantiationActionsProvider bean = publish(new InstantiationActionsProvider());
			bean.setGmSession(Session.workbenchPersistenceSession.get());
			return bean;
		}
	};
	
	private static Supplier<GIMASelectionContentView> selectionContentViewSupplier = new PrototypeBeanProvider<GIMASelectionContentView>() {
		@Override
		public GIMASelectionContentView create() throws Exception {
			GIMASelectionContentView bean = new GIMASelectionContentView();
			bean.setSelectionConstellation(Constellations.selectionConstellationProvider.get());
			return bean;
		}
	};
	
	private static Supplier<List<TabbedGmEntityViewContext>> selectionPropertyPanelTabbedGmEntityViewContexts = new PrototypeBeanProvider<List<TabbedGmEntityViewContext>>() {
		@Override
		public List<TabbedGmEntityViewContext> create() throws Exception {
			List<TabbedGmEntityViewContext> bean = new ArrayList<>();
			return bean;
		}
	};
	
	private static Supplier<SimpleTypeParser> simpleTypeParserProvider = new SingletonBeanProvider<SimpleTypeParser>() {
		@Override
		public SimpleTypeParser create() throws Exception {
			SimpleTypeParser bean = new SimpleTypeParser();
			bean.setLocaleProvider(Startup.localeProvider.get());
			return bean;
		}
	};
	
}
