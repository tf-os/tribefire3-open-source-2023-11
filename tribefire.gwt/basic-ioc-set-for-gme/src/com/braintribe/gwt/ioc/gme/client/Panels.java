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
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.filter.lcd.pattern.CamelCasePatternMatcher;
import com.braintribe.filter.lcd.pattern.SubstringCheckingPatternMatcher;
import com.braintribe.gwt.gm.resource.streamingprocessing.client.RestBasedResourceProcessing;
import com.braintribe.gwt.gm.resource.streamingprocessing.client.StreamingBasedResourceProcessing;
import com.braintribe.gwt.gme.assemblypanel.client.AssemblyPanel;
import com.braintribe.gwt.gme.assemblypanel.client.action.ExchangeAssemblyPanelDisplayModeAction.DisplayMode;
import com.braintribe.gwt.gme.assemblypanel.client.model.AbstractGenericTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.ObjectAndType;
import com.braintribe.gwt.gme.assemblypanel.client.model.factory.CollectionTreeModelFactory;
import com.braintribe.gwt.gme.assemblypanel.client.model.factory.EntityTreeModelFactory;
import com.braintribe.gwt.gme.assemblypanel.client.model.factory.ModelFactory;
import com.braintribe.gwt.gme.constellation.client.NewLoginHtmlPanel;
import com.braintribe.gwt.gme.constellation.client.SelectionConstellationScopedBeanProvider;
import com.braintribe.gwt.gme.constellation.client.TopBannerConstellation;
import com.braintribe.gwt.gme.constellation.client.gima.GIMASelectionConstellation;
import com.braintribe.gwt.gme.constellation.client.gima.GIMASelectionContentView;
import com.braintribe.gwt.gme.gmactionbar.client.DefaultGmViewActionBar;
import com.braintribe.gwt.gme.propertypanel.client.AbstractPropertyPanel.ValueRendering;
import com.braintribe.gwt.gme.propertypanel.client.PropertyPanel;
import com.braintribe.gwt.gme.propertypanel.client.field.ExtendedInlineField;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessTriggerField;
import com.braintribe.gwt.gme.propertypanel.client.field.ResourcesExtendedInlineField;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelResources;
import com.braintribe.gwt.gme.selectresultpanel.client.SelectResultPanel;
import com.braintribe.gwt.gme.servicerequestpanel.client.ServiceRequestConstellationScopedBeanProvider;
import com.braintribe.gwt.gme.servicerequestpanel.client.ServiceRequestExecutionPanel;
import com.braintribe.gwt.gme.servicerequestpanel.client.ServiceRequestPanel;
import com.braintribe.gwt.gme.simplecontentview.client.HyperLinkContentViewPanel;
import com.braintribe.gwt.gme.templateevaluation.client.TemplateEvaluationDialog;
import com.braintribe.gwt.gme.tetherbar.client.TetherBar;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabActionMenu;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabPanel;
import com.braintribe.gwt.gme.workbench.client.Workbench;
import com.braintribe.gwt.gme.workbench.client.WorkbenchController;
import com.braintribe.gwt.gmview.action.client.EntitiesFutureProvider;
import com.braintribe.gwt.gmview.action.client.InstantiationActionsProvider;
import com.braintribe.gwt.gmview.action.client.QueryActionsProvider;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionBar;
import com.braintribe.gwt.gmview.client.ExpertUI;
import com.braintribe.gwt.gmview.client.GeneralPanel;
import com.braintribe.gwt.gmview.client.PreviewPanel;
import com.braintribe.gwt.gmview.client.TabbedGmEntityView;
import com.braintribe.gwt.gmview.client.TabbedGmEntityViewContext;
import com.braintribe.gwt.gmview.client.TabbedWidgetContext;
import com.braintribe.gwt.gmview.client.ViewSituationResolver;
import com.braintribe.gwt.gmview.client.ViewSituationSelector;
import com.braintribe.gwt.gmview.client.components.UiComponentSelectorSupplier;
import com.braintribe.gwt.gmview.client.parse.MultiDescriptionStringParser;
import com.braintribe.gwt.gmview.client.parse.SimpleTypeParser;
import com.braintribe.gwt.gmview.util.client.GmPreviewUtil;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.ioc.gme.client.expert.HomeActionTrigger;
import com.braintribe.gwt.menu.client.CustomMenu;
import com.braintribe.gwt.querymodeleditor.client.panels.editor.GlobalSearchPanel;
import com.braintribe.gwt.resourceuploadui.client.ResourceUploadDialog;
import com.braintribe.gwt.resourceuploadui.client.ResourceUploadPanel;
import com.braintribe.gwt.security.client.SessionScopedBeanProvider;
import com.braintribe.gwt.simplepropertypanel.client.SimplePropertyPanel;
import com.braintribe.gwt.smartmapper.client.SmartMapper;
import com.braintribe.gwt.templateeditor.client.TemplateEditorPanel;
import com.braintribe.gwt.thumbnailpanel.client.ThumbnailPanel;
import com.braintribe.gwt.thumbnailpanel.client.js.JsThumbnailPanel;
import com.braintribe.gwt.utils.client.FastMap;
import com.braintribe.gwt.utils.client.FastSet;
import com.braintribe.gwt.utils.client.RootKeyNavExpert;
import com.braintribe.gwt.validationui.client.ValidationLogListPanel;
import com.braintribe.gwt.validationui.client.ValidationLogRepresentation;
import com.braintribe.model.common.Color;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.validation.Validation;
import com.braintribe.model.generic.validation.expert.Predator;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.provider.PrototypeBeanProvider;
import com.braintribe.provider.SingletonBeanProvider;
import com.google.gwt.core.client.GWT;

import tribefire.extension.js.model.deployment.DetailWithUiComponent;
import tribefire.extension.js.model.deployment.JsUxComponent;
import tribefire.extension.js.model.deployment.KnownViewPositionUseCase;
import tribefire.extension.js.model.deployment.PropertyPanelUxComponent;

/**
 * This is the IoC configuration for the Panels (components).
 *
 */
public class Panels {
	
	public static boolean useSettingsMenu = true;
	
	public static void setUseSettingsMenu(boolean useSettingsMenu) {
		Panels.useSettingsMenu = useSettingsMenu;
	}
		
	protected static Supplier<TopBannerConstellation> topBanner = new SessionScopedBeanProvider<TopBannerConstellation>() {
		@Override
		public TopBannerConstellation create() throws Exception {
			TopBannerConstellation bean = publish(new TopBannerConstellation());
			bean.setQuickAccessFieldSupplier(topBannerQuickAccessTriggerField);
			bean.setManagerMenu(Panels.managerMenu.get());
			bean.setGlobalStateLabel(UiElements.globaleStateLabel.get());
			bean.setGlobalSearchPanelSupplier(globalSearchPanel);
			bean.setGmSession(Session.persistenceSession.get());
			//bean.setNotificationIcon(Notifications.notificationIcon.get());
			return bean;
		}
	};
	
	protected static Supplier<GlobalSearchPanel> globalSearchPanel = new SessionScopedBeanProvider<GlobalSearchPanel>() {
		@Override
		public GlobalSearchPanel create() throws Exception {
			GlobalSearchPanel bean = publish(new GlobalSearchPanel());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			bean.setQueryProviderView(UiElements.queryModelEditorPanelProvider.get());
			//bean.setQuickAccessPanel(topBannerQuickAccessProvider.get());
			bean.setQuickAccessTriggerField(topBannerQuickAccessTriggerField.get());
			return bean;
		}
	};
	
	protected static Supplier<QuickAccessTriggerField> topBannerQuickAccessTriggerField = new SessionScopedBeanProvider<QuickAccessTriggerField>() {
		@Override
		public QuickAccessTriggerField create() throws Exception {
			QuickAccessTriggerField bean = publish(new QuickAccessTriggerField());
			bean.setQuickAccessPanel(topBannerQuickAccessProvider);
			return bean;
		}
	};
	
	protected static Supplier<SpotlightPanel> topBannerQuickAccessProvider = new SessionScopedBeanProvider<SpotlightPanel>() {
		@Override
		public SpotlightPanel create() throws Exception {
			SpotlightPanel bean = new SpotlightPanel();
			bean.setSimpleTypesValuesProvider(simpleTypeParserProvider);
			bean.setGmSession(Session.persistenceSession.get());
			bean.setCodecRegistry(Codecs.renderersCodecRegistry.get());
			bean.setEntitiesFutureProvider(entitiesFutureProvider.get());
			bean.setPatternMatchers(Arrays.asList(new SubstringCheckingPatternMatcher(), new CamelCasePatternMatcher()));
			bean.setQueryActionsProvider(queryActionsProvider.get());
			bean.setInstantiationActionsProvider(instantiationActionsProvider.get());
			bean.setIconProvider(Providers.typeIconProvider);
			bean.setUseCase(Runtime.quickAccessPanelUseCaseProvider.get());
			bean.setShowTemplates(false);
			bean.setTransientGmSession(Session.transientManagedSession.get());
			return bean;
		}
	};
	
	protected static Supplier<NewLoginHtmlPanel> newLoginPanel = new SingletonBeanProvider<NewLoginHtmlPanel>() {
		@Override
		public NewLoginHtmlPanel create() throws Exception {
			NewLoginHtmlPanel bean = publish(new NewLoginHtmlPanel());
			bean.setAccessId(Runtime.accessId.get());
			bean.setUiThemeLoader(Providers.uiThemeLoader.get());
			bean.setFavIconLoader(Providers.favIconLoader.get());
			bean.setTitleLoader(Providers.titleLoader.get());
			
			//bean.setAutoHeight(false);
			bean.setBorders(false);
			bean.setBodyBorder(false);
			bean.setHtmlSourceUrl(GWT.getModuleBaseURL() + "bt-resources/commons/login/index.html");
			//bean.addWidget("languagelabel-slot", UiElements.languageLabel.get());
			//bean.addWidget("language-slot", UiElements.languageField.get());
			//bean.addWidget("usernamelabel-slot", UiElements.userNameLabel.get());
			bean.addWidget("username-slot", UiElements.userNameTextField.get());
			//bean.addWidget("passwordlabel-slot", UiElements.passwordLabel.get());
			bean.addWidget("password-slot", UiElements.passwordTextField.get());
			bean.addButtonAction("loginbutton-slot", Actions.loginAction.get());
			bean.addWidget("errormessage-slot", UiElements.errorMessageLabel.get());
			bean.init();
			return bean;
		}
	};
	
	private static Supplier<CustomMenu> managerMenu = new SessionScopedBeanProvider<CustomMenu>() {
		@Override
		public CustomMenu create() throws Exception {
			CustomMenu bean = publish(new CustomMenu());
			bean.setItemWidth(35);
			bean.setItemHeight(35);
			bean.setBottomActions(Arrays.asList(UiElements.showSettingsMenuAction.get(),UiElements.showUserMenuAction.get()));
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
	
	public static Supplier<Set<Class<?>>> simplifiedEntityTypes = new SingletonBeanProvider<Set<Class<?>>>() {
		@Override
		public Set<Class<?>> create() throws Exception {
			Set<Class<?>> bean = new HashSet<>();
			bean.add(LocalizedString.class);
			bean.add(Color.class);
			bean.add(TimeSpan.class);
			return bean;
		}
	};
	
	protected static Supplier<ModelFactory> modelFactory = new PrototypeBeanProvider<ModelFactory>() {
		@Override
		public ModelFactory create() throws Exception {
			ModelFactory bean = new ModelFactory();
			bean.setTypeSpecificFactories(typeSpecificFactories.get());
			bean.setSimplifiedEntityTypes(simplifiedEntityTypes.get());
			return bean;
		}
	};
	
	private static Supplier<Map<TypeCode, Function<ObjectAndType, ? extends AbstractGenericTreeModel>>> typeSpecificFactories = new PrototypeBeanProvider
			<Map<TypeCode, Function<ObjectAndType, ? extends AbstractGenericTreeModel>>>() {
		@Override
		public Map<TypeCode, Function<ObjectAndType, ? extends AbstractGenericTreeModel>> create() throws Exception {
			Map<TypeCode, Function<ObjectAndType, ? extends AbstractGenericTreeModel>> bean = new HashMap<>();
			CollectionTreeModelFactory ctmf = collectionTreeModelFactory.get();
			bean.put(TypeCode.entityType, entityTreeModelFactoryProvider.get());
			bean.put(TypeCode.listType, ctmf);
			bean.put(TypeCode.mapType, ctmf);
			bean.put(TypeCode.mapType, ctmf);
			bean.put(TypeCode.setType, ctmf);
			return bean;
		}
	};
	
	private static Supplier<EntityTreeModelFactory> entityTreeModelFactoryProvider = new PrototypeBeanProvider<EntityTreeModelFactory>() {
		@Override
		public EntityTreeModelFactory create() throws Exception {
			EntityTreeModelFactory bean = new EntityTreeModelFactory(Runtime.displayAllPropertiesInMultiplex.get());
			return bean;
		}
	};
	
	private static Supplier<CollectionTreeModelFactory> collectionTreeModelFactory = new PrototypeBeanProvider<CollectionTreeModelFactory>() {
		@Override
		public CollectionTreeModelFactory create() throws Exception {
			CollectionTreeModelFactory bean = new CollectionTreeModelFactory();
			return bean;
		}
	};
	
	protected static Supplier<TetherBar> tetherBarProvider = new PrototypeBeanProvider<TetherBar>() {
		@Override
		public TetherBar create() throws Exception {
			TetherBar bean = new TetherBar();
			return bean;
		}
	};
	
	protected static Supplier<TetherBar> unclickableTetherBarProvider = new PrototypeBeanProvider<TetherBar>() {
		@Override
		public TetherBar create() throws Exception {
			TetherBar bean = new TetherBar();
			bean.setClickable(false);
			bean.setMaxTetherBarElements(3);
			return bean;
		}
	};
	
	protected static Supplier<SelectResultPanel> selectResultPanelProvider = new PrototypeBeanProvider<SelectResultPanel>() {
		@Override
		public SelectResultPanel create() throws Exception {
			SelectResultPanel bean = new SelectResultPanel();
			bean.setCodecRegistry(Codecs.renderersCodecRegistry.get());
			bean.setSpecialRenderersRegistry(Codecs.specialFlowCodecRegistry.get());
			bean.setActionManager(Controllers.actionManager.get());
			bean.setDefaultContextMenuActionSupplier(Constellations.globalActionsToolBar.get(),
					com.braintribe.gwt.gme.constellation.client.LocalizedText.INSTANCE.newEntry());
			return bean;
		}
	};
	
	private static Supplier<AssemblyPanel> abstractAssemblyPanelProvider = new PrototypeBeanProvider<AssemblyPanel>() {
		{
			setAbstract(true);
		}
		@Override
		public AssemblyPanel create() throws Exception {
			AssemblyPanel bean = new AssemblyPanel();
			bean.configureUseCase(Runtime.assemblyPanelUseCaseProvider.get());
			bean.setCodecRegistry(Codecs.renderersCodecRegistry.get());
			bean.setModelFactory(modelFactory.get());
			bean.setSpecialEntityTraversingCriterion(specialEntityTraversingCriterionMap.get());
			bean.setActionManager(Controllers.actionManager.get());
			bean.addClipboardListener(Constellations.clipboardConstellationProviderProvider.get());
			bean.setIconProvider(Providers.apTypeIconProvider.get());
			bean.setSelectionFutureProvider(gimaSelectionConstellationSupplier);
			//bean.setSelectionFutureProvider(selectionConstellationDialogProvider);
			bean.setShowGridLines(false);
			bean.setGmViewActionBarProvider(gmViewActionBar);
			bean.setSimplifiedEntityTypes(simplifiedEntityTypes.get());
			bean.setTransientSession(Session.transientManagedSession.get());
			bean.setTransientSessionSupplier(Session.prototypeTransientManagedSession);
			bean.setNotificationFactory(Notifications.notificationFactory);
			bean.setGmEditionViewController(Controllers.gmEditionViewController);
			bean.setCommitAction(Actions.saveAction.get());
			bean.setWorkbenchActionHandlerRegistry(Actions.workbenchActionHandlerRegistry.get());
			return bean;
		}
	};
	
	/*
	// Js tests
	protected static Supplier<ExternalWidgetGmContentView> assemblyPanelProvider = new PrototypeBeanProvider<ExternalWidgetGmContentView>() {
		@Override
		public ExternalWidgetGmContentView create() throws Exception {
			//ExternalWidgetGmContentView bean = externalComponentWidgetSupplier.get().apply("http://localhost:8080/tfjs-vue-demo-models/tfmain.js");
			ExternalWidgetGmContentView bean = testExternalComponentWidgetSupplier.get().apply("http://localhost:8080/tfjs-vue-demo-assembly/tfmain.js");
			bean.setActionManager(Controllers.actionManager.get());
			return bean;
		}
	};
	
	private static Supplier<TestExternalComponentWidgetSupplier> testExternalComponentWidgetSupplier = new SingletonBeanProvider<TestExternalComponentWidgetSupplier>() {
		@Override
		public TestExternalComponentWidgetSupplier create() throws Exception {
			TestExternalComponentWidgetSupplier bean = new TestExternalComponentWidgetSupplier();
			bean.setJsScriptLoader(Providers.jsScriptLoader.get());
			return bean;
		}
	};
	*/
	
	protected static Supplier<AssemblyPanel> assemblyPanelProvider = new PrototypeBeanProvider<AssemblyPanel>() {
		@Override
		public AssemblyPanel create() throws Exception {
			AssemblyPanel bean = abstractAssemblyPanelProvider.get();
			bean.setGMEditorSupport(UiElements.gmEditorSupport.get());
			bean.setUseDragAndDrop(true);
			bean.setDefaultContextMenuActionSupplier(Constellations.globalActionsToolBar.get(),
					com.braintribe.gwt.gme.constellation.client.LocalizedText.INSTANCE.newEntry());
			//bean.setEntitySelectionFutureProvider(selectionConstellationDialogProvider);
			bean.setGmeDragAndDropSupport(Providers.gmeDragAndDropSupport.get());
			bean.setWorkbenchActionSelectionHandler(Constellations.explorerConstellationProvider.get());
			return bean;
		}
	};
	
	private static Supplier<AssemblyPanel> templateAssemblyPanelProvider = new PrototypeBeanProvider<AssemblyPanel>() {
		@Override
		public AssemblyPanel create() throws Exception {
			AssemblyPanel bean = abstractAssemblyPanelProvider.get();
			bean.setGMEditorSupport(UiElements.gmEditorSupport.get());
			bean.setPrepareToolBarActions(false);
			bean.setDefaultDisplayMode(DisplayMode.Simple);
			return bean;
		}
	};
	
	protected static Supplier<AssemblyPanel> gimaAssemblyPanelProvider = new PrototypeBeanProvider<AssemblyPanel>() {
		@Override
		public AssemblyPanel create() throws Exception {
			AssemblyPanel bean = abstractAssemblyPanelProvider.get();
			bean.setGMEditorSupport(UiElements.gmEditorSupport.get());
			//bean.setEntitySelectionFutureProvider(selectionConstellationDialogProvider);
			bean.setActionManager(Controllers.gimaActionManager.get());
			bean.setUseDragAndDrop(true);
			return bean;
		}
	};
	
	protected static Supplier<AssemblyPanel> readOnlyAssemblyPanelProvider = new PrototypeBeanProvider<AssemblyPanel>() {
		@Override
		public AssemblyPanel create() throws Exception {
			AssemblyPanel bean = abstractAssemblyPanelProvider.get();
			bean.setReadOnly(true);
			bean.setActionManager(Controllers.readOnlyActionManager.get());
			bean.setFilterExternalActions(false);
			return bean;
		}
	};
	
	protected static Supplier<AssemblyPanel> clipboardAssemblyPanelProvider = new PrototypeBeanProvider<AssemblyPanel>() {
		@Override
		public AssemblyPanel create() throws Exception {
			AssemblyPanel bean = abstractAssemblyPanelProvider.get();
			bean.setReadOnly(true);
			bean.setActionManager(Controllers.readOnlyActionManager.get());
			bean.setUseCopyEntityToClipboardAction(false);
			bean.setFilterExternalActions(false);
			return bean;
		}
	};
	
	protected static Supplier<AssemblyPanel> localModeAssemblyPanelProvider = new PrototypeBeanProvider<AssemblyPanel>() {
		@Override
		public AssemblyPanel create() throws Exception {
			AssemblyPanel bean = abstractAssemblyPanelProvider.get();
			bean.setReadOnly(true);
			bean.setPrepareToolBarActions(false);
			bean.setShowContextMenu(false);
			bean.setNavigationEnabled(false);
			bean.setActionManager(Controllers.readOnlyActionManager.get());
			return bean;
		}
	};
	
	protected static Supplier<AssemblyPanel> selectionAssemblyPanelProvider = new PrototypeBeanProvider<AssemblyPanel>() {
		@Override
		public AssemblyPanel create() throws Exception {
			AssemblyPanel bean = abstractAssemblyPanelProvider.get();
			bean.setReadOnly(true);
			bean.setActionManager(Controllers.readOnlyActionManager.get());
			bean.setPrepareToolBarActions(false);
			bean.setShowContextMenu(false);
			bean.setUseCheckBoxColumn(true);
			bean.configureUseCase(Runtime.selectionUseCaseProvider.get());
			return bean;
		}
	};
	
	protected static Supplier<AssemblyPanel> errorAssemblyPanelProvider = new PrototypeBeanProvider<AssemblyPanel>() {
		@Override
		public AssemblyPanel create() throws Exception {
			AssemblyPanel bean = abstractAssemblyPanelProvider.get();
			bean.setCodecRegistry(Codecs.gmErrorRendererCodecsProvider.get());
			bean.setReadOnly(true);
			bean.setActionManager(null);
			bean.setNavigationEnabled(false);
			bean.setPrepareToolBarActions(false);
			bean.setShowContextMenu(false);
			bean.configureGmSession(Session.transientManagedSession.get());
			return bean;
		}
	};
	
	protected static Supplier<AssemblyPanel> saveQueryDialogWorkbenchSelectionAssemblyPanelProvider = new PrototypeBeanProvider<AssemblyPanel>() {
		@Override
		public AssemblyPanel create() throws Exception {
			AssemblyPanel bean = abstractAssemblyPanelProvider.get();
			bean.setReadOnly(true);
			bean.setAutoExpandLevel(3);
			bean.setActionManager(Controllers.readOnlyActionManager.get());
			bean.setPrepareToolBarActions(false);
			bean.setShowContextMenu(false);
			bean.setUseCheckBoxColumn(true);
			bean.configureUseCase(Runtime.selectionUseCaseProvider.get());
			return bean;
		}
	};
	
	private static Supplier<GeneralPanel> generalPanel = new PrototypeBeanProvider<GeneralPanel>() {
		@Override
		public GeneralPanel create() throws Exception {
			GeneralPanel bean = new GeneralPanel();
			bean.setIconProvider(Providers.largestTypeIconProvider.get());
			bean.setDefaultIcon(PropertyPanelResources.INSTANCE.defaultIcon());
			bean.setAction(Actions.detailsPanelVisibilityAction.get());
			bean.configureGmSession(Session.persistenceSession.get());
			bean.setPreviewPanel(previewPanel.get());
			return bean;
		}
	};
	
	private static Supplier<PreviewPanel> previewPanel = new PrototypeBeanProvider<PreviewPanel>() {
		@Override
		public PreviewPanel create() throws Exception {
			PreviewPanel bean = new PreviewPanel();
			bean.setPreviewUtil(previewUtitl.get());
			Notifications.refreshPreviewExpert.get().addPreviewPanel(bean);
			return bean;
		}
	};
	
	private static Supplier<GmPreviewUtil> previewUtitl = new PrototypeBeanProvider<GmPreviewUtil>() {
		@Override
		public GmPreviewUtil create() throws Exception {
			GmPreviewUtil bean = new GmPreviewUtil();
			bean.setSessionIdProvider(Providers.sessionIdProvider.get());
			bean.setSession(Session.persistenceSession.get());
			bean.setServicesUrl(Runtime.tribefireServicesAbsoluteUrl.get());
			return bean;
		}
	};

	protected static Supplier<TabbedGmEntityView> tabbedPropertyPanelProvider = new PrototypeBeanProvider<TabbedGmEntityView>() {
		@Override
		public TabbedGmEntityView create() throws Exception {
			TabbedGmEntityView bean = new TabbedGmEntityView();
			bean.setTabbedGmEntityViewContexts(propertyPanelTabbedGmEntityViewContexts.get());
			bean.setHeaderPanel(generalPanel.get());
			bean.setTabbedGmEntityViewContextResolver(tabbedGmEntityViewContextResolver.get());
			//bean.setAction(Actions.detailsPanelVisibilityAction.get());
			return bean;
		}
	};
	
	private static Supplier<ViewSituationResolver<TabbedWidgetContext>> tabbedGmEntityViewContextResolver = new PrototypeBeanProvider<ViewSituationResolver<TabbedWidgetContext>>() {
		@Override
		public ViewSituationResolver<TabbedWidgetContext> create() throws Exception {
			ViewSituationResolver<TabbedWidgetContext> bean = new ViewSituationResolver<>();
			bean.setGmExpertRegistry(Runtime.gmExpertRegistry.get());
			bean.setViewSituationSelectorMap(tabbedWidgetContextViewSelectorMap.get());
			bean.setViewSituationSelectorSupplierMap(tabbedWidgetContextViewSelectorSupplierMap.get());
			return bean;
		}
	};
	
	private static Supplier<Map<ViewSituationSelector, TabbedWidgetContext>> tabbedWidgetContextViewSelectorMap = new PrototypeBeanProvider<Map<ViewSituationSelector, TabbedWidgetContext>>() {
		@Override
		public Map<ViewSituationSelector, TabbedWidgetContext> create() throws Exception {
			Map<ViewSituationSelector, TabbedWidgetContext> bean = new HashMap<>();
			bean.put(ViewSituationResolution.entityTypeViewSituationSelector.get(), simplePropertyTabbedWidgetContext.get());
			return bean;
		}
	};
	
	private static Supplier<Map<ViewSituationSelector, Function<ModelPathElement, List<TabbedWidgetContext>>>> tabbedWidgetContextViewSelectorSupplierMap =
			new PrototypeBeanProvider<Map<ViewSituationSelector, Function<ModelPathElement, List<TabbedWidgetContext>>>>() {
		@Override
		public Map<ViewSituationSelector, Function<ModelPathElement, List<TabbedWidgetContext>>> create() throws Exception {
			Map<ViewSituationSelector, Function<ModelPathElement, List<TabbedWidgetContext>>> bean = new HashMap<>();
			bean.put(uiComponentSelector.get(), uiComponentSelectorSupplier.get());
			return bean;
		}
	};
	
	private static Supplier<UiComponentSelectorSupplier> uiComponentSelectorSupplier = new SingletonBeanProvider<UiComponentSelectorSupplier>() {
		@Override
		public UiComponentSelectorSupplier create() throws Exception {
			UiComponentSelectorSupplier bean = publish(new UiComponentSelectorSupplier());
			bean.setGmSession(Session.persistenceSession.get());
			bean.setUseCases(new FastSet(Arrays.asList(KnownViewPositionUseCase.gmeViewRight.getDefaultValue())));
			bean.setWellKnownComponentsContextMap(wellKnownComponentsContextMap.get());
			bean.setExternalWidgetSupplier(ViewSituationResolution.externalComponentWidgetSupplier.get());
			return bean;
		}
	};
	
	private static Supplier<ViewSituationSelector> uiComponentSelector = new SingletonBeanProvider<ViewSituationSelector>() {
		@Override
		public ViewSituationSelector create() throws Exception {
			ViewSituationSelector bean = publish(ViewSituationSelector.T.create());
			bean.setConflictPriority(1.1);
			bean.setMetadataTypeSignature(DetailWithUiComponent.T.getTypeSignature());
			bean.setUseCases(new FastSet(Arrays.asList(KnownViewPositionUseCase.gmeViewRight.getDefaultValue())));
			return bean;
		}
	};
	
	private static Supplier<Map<EntityType<? extends JsUxComponent>, TabbedWidgetContext>> wellKnownComponentsContextMap =
			new PrototypeBeanProvider<Map<EntityType<? extends JsUxComponent>, TabbedWidgetContext>>() {
		@Override
		public Map<EntityType<? extends JsUxComponent>, TabbedWidgetContext> create() throws Exception {
			Map<EntityType<? extends JsUxComponent>, TabbedWidgetContext> bean = new HashMap<>();
			bean.put(PropertyPanelUxComponent.T, propertyPanelTabbedWidgetContext.get());
			return bean;
		}
	};
	
	private static Supplier<TabbedWidgetContext> propertyPanelTabbedWidgetContext = new PrototypeBeanProvider<TabbedWidgetContext>() {
		@Override
		public TabbedWidgetContext create() throws Exception {
			TabbedWidgetContext bean = new TabbedWidgetContext(LocalizedText.INSTANCE.details(),
					LocalizedText.INSTANCE.details(), propertyPanelProvider);
			return bean;
		}
	};
	
	private static Supplier<TabbedWidgetContext> simplePropertyTabbedWidgetContext = new PrototypeBeanProvider<TabbedWidgetContext>() {
		@Override
		public TabbedWidgetContext create() throws Exception {
			TabbedWidgetContext bean = new TabbedWidgetContext(LocalizedText.INSTANCE.properties(), LocalizedText.INSTANCE.properties(),
					simplePropertyPanel, 0);
			return bean;
		}
	};
	
	protected static Supplier<TabbedGmEntityView> tabbedReadOnlyPropertyPanelProvider = new PrototypeBeanProvider<TabbedGmEntityView>() {
		@Override
		public TabbedGmEntityView create() throws Exception {
			TabbedGmEntityView bean = new TabbedGmEntityView();
			bean.setTabbedGmEntityViewContexts(readOnlyPropertyPanelTabbedGmEntityViewContexts.get());
			bean.setHeaderPanel(generalPanel.get());
			//bean.setAction(Actions.detailsPanelVisibilityAction.get());
			return bean;
		}
	};
	
	protected static Supplier<TabbedGmEntityView> tabbedSelectionPropertyPanelProvider = new PrototypeBeanProvider<TabbedGmEntityView>() {
		@Override
		public TabbedGmEntityView create() throws Exception {
			TabbedGmEntityView bean = new TabbedGmEntityView();
			bean.setTabbedGmEntityViewContexts(selectionPropertyPanelTabbedGmEntityViewContexts.get());
			bean.setHeaderPanel(generalPanel.get());
			//bean.setAction(Actions.detailsPanelVisibilityAction.get());
			return bean;
		}
	};
	
	protected static Supplier<TabbedGmEntityView> tabbedLocalModePropertyPanelProvider = new PrototypeBeanProvider<TabbedGmEntityView>() {
		@Override
		public TabbedGmEntityView create() throws Exception {
			TabbedGmEntityView bean = new TabbedGmEntityView();
			bean.setTabbedGmEntityViewContexts(localModePropertyPanelTabbedGmEntityViewContexts.get());
			bean.setHeaderPanel(generalPanel.get());
			//bean.setAction(Actions.detailsPanelVisibilityAction.get());
			return bean;
		}
	};
	
	private static Supplier<List<TabbedGmEntityViewContext>> propertyPanelTabbedGmEntityViewContexts = new PrototypeBeanProvider<List<TabbedGmEntityViewContext>>() {
		@Override
		public List<TabbedGmEntityViewContext> create() throws Exception {
			List<TabbedGmEntityViewContext> bean = new ArrayList<>();
			TabbedGmEntityViewContext simplePropertyContext = new TabbedGmEntityViewContext(LocalizedText.INSTANCE.details(),
					LocalizedText.INSTANCE.details(), propertyPanelProvider.get());
			bean.add(simplePropertyContext);
			
			//RVE test for CommentPanel
			/*
			TabbedGmEntityViewContext commentContext = new TabbedGmEntityViewContext("Comment",
					"Comment", WebReaderIoc.commentComponent.get());
			bean.add(commentContext);
			TabbedGmEntityViewContext documentCommentContext = new TabbedGmEntityViewContext("Doc Comment",
					"Doc Comment", WebReaderIoc.documentCommentComponent.get());
			bean.add(documentCommentContext);
			*/
			return bean;
		}
	};
	
	private static Supplier<List<TabbedGmEntityViewContext>> readOnlyPropertyPanelTabbedGmEntityViewContexts = new PrototypeBeanProvider<List<TabbedGmEntityViewContext>>() {
		@Override
		public List<TabbedGmEntityViewContext> create() throws Exception {
			List<TabbedGmEntityViewContext> bean = new ArrayList<>();
			TabbedGmEntityViewContext simplePropertyContext = new TabbedGmEntityViewContext(LocalizedText.INSTANCE.details(),
					LocalizedText.INSTANCE.details(), readOnlyPropertyPanelWithGeneralProvider.get());
			bean.add(simplePropertyContext);
			return bean;
		}
	};
	
	private static Supplier<List<TabbedGmEntityViewContext>> selectionPropertyPanelTabbedGmEntityViewContexts = new PrototypeBeanProvider<List<TabbedGmEntityViewContext>>() {
		@Override
		public List<TabbedGmEntityViewContext> create() throws Exception {
			List<TabbedGmEntityViewContext> bean = new ArrayList<>();
			TabbedGmEntityViewContext simplePropertyContext = new TabbedGmEntityViewContext(LocalizedText.INSTANCE.details(),
					LocalizedText.INSTANCE.details(), selectionPropertyPanelWithGeneralProvider.get());
			bean.add(simplePropertyContext);
			return bean;
		}
	};
	
	private static Supplier<List<TabbedGmEntityViewContext>> localModePropertyPanelTabbedGmEntityViewContexts = new PrototypeBeanProvider<List<TabbedGmEntityViewContext>>() {
		@Override
		public List<TabbedGmEntityViewContext> create() throws Exception {
			List<TabbedGmEntityViewContext> bean = new ArrayList<>();
			TabbedGmEntityViewContext simplePropertyContext = new TabbedGmEntityViewContext(LocalizedText.INSTANCE.details(),
					LocalizedText.INSTANCE.details(), localModePropertyPanelProvider.get());
			bean.add(simplePropertyContext);
			return bean;
		}
	};
	
	protected static Supplier<PropertyPanel> abstractPropertyPanelProvider = new PrototypeBeanProvider<PropertyPanel>() {
		{
			setAbstract(true);
		}
		@Override
		public PropertyPanel create() throws Exception {
			PropertyPanel bean = new PropertyPanel();
			bean.configureUseCase(Runtime.propertyPanelUseCaseProvider.get());
			bean.setGMEditorSupport(UiElements.gmEditorSupport.get());
			bean.setSpecialEntityTraversingCriterion(specialEntityTraversingCriterionMap.get());
			bean.setCodecRegistry(Codecs.renderersCodecRegistry.get());
			bean.setIconProvider(Providers.typeIconProvider.get());
			bean.setSimplifiedEntityTypes(simplifiedEntityTypes.get());
			bean.setShowGridLines(false);
			bean.setSpecialFlowCodecRegistry(Codecs.specialFlowCodecRegistry.get());
			bean.setSpecialFlowClasses(specialFlowClasses.get());
			bean.setTransientSession(Session.transientManagedSession.get());
			bean.setTransientSessionSupplier(Session.prototypeTransientManagedSession);
			bean.setNotificationFactory(Notifications.notificationFactory);
			bean.setGmEditionViewController(Controllers.gmEditionViewController);
			if(Runtime.useCommit)
				bean.setCommitAction(Actions.saveAction.get());
			return bean;
		}
	};
	
	public static Supplier<Map<Class<?>, Supplier<? extends ExtendedInlineField>>> extendedInlineFields = new SingletonBeanProvider<Map<Class<?>, Supplier<? extends ExtendedInlineField>>>() {
		@Override
		public Map<Class<?>, Supplier<? extends ExtendedInlineField>> create() throws Exception {
			Map<Class<?>, Supplier<? extends ExtendedInlineField>> bean = new HashMap<>();
			bean.put(Resource.class, resourceExtendedInlineField);
			return bean;
		}
	};
	
	private static Supplier<Map<Class<?>, Supplier<? extends ExtendedInlineField>>> workbenchExtendedInlineFields = new SingletonBeanProvider<Map<Class<?>, Supplier<? extends ExtendedInlineField>>>() {
		@Override
		public Map<Class<?>, Supplier<? extends ExtendedInlineField>> create() throws Exception {
			Map<Class<?>, Supplier<? extends ExtendedInlineField>> bean = new HashMap<>();
			bean.put(Resource.class, workbenchResourceExtendedInlineField);
			return bean;
		}
	};
	
	private static Supplier<ResourcesExtendedInlineField> resourceExtendedInlineField = new PrototypeBeanProvider<ResourcesExtendedInlineField>() {
		@Override
		public ResourcesExtendedInlineField create() throws Exception {
			ResourcesExtendedInlineField bean = new ResourcesExtendedInlineField();
			bean.setResourceUploadView(smallResourceUploadPanelProvider.get());
			return bean;
		}
	};
	
	private static Supplier<ResourcesExtendedInlineField> workbenchResourceExtendedInlineField = new PrototypeBeanProvider<ResourcesExtendedInlineField>() {
		@Override
		public ResourcesExtendedInlineField create() throws Exception {
			ResourcesExtendedInlineField bean = new ResourcesExtendedInlineField();
			bean.setResourceUploadView(workbenchSmallResourceUploadPanelProvider.get());
			return bean;
		}
	};
	
	protected static Supplier<Set<Class<?>>> specialFlowClasses = new SingletonBeanProvider<Set<Class<?>>>() {
		@Override
		public Set<Class<?>> create() throws Exception {
			Set<Class<?>> bean = new HashSet<>();
			bean.add(LocalizedString.class);
			bean.add(Color.class);
			bean.add(TimeSpan.class);
			return bean;
		}
	};
	
	protected static Supplier<PropertyPanel> propertyPanelProvider = new PrototypeBeanProvider<PropertyPanel>() {
		@Override
		public PropertyPanel create() throws Exception {
			PropertyPanel bean = abstractPropertyPanelProvider.get();
			//bean.setSelectionFutureProvider(selectionConstellationDialogActionManagerProvider);
			bean.setSelectionFutureProvider(gimaSelectionConstellationSupplier);
			bean.setActionManager(Controllers.propertyPanelActionManager.get());
			bean.setExtendedInlineFields(extendedInlineFields.get());
//			bean.setNewInstanceProviderProvider(Providers.newInstanceProvider);
			return bean;
		}
	};

	public static Supplier<PropertyPanel> jsPropertyPanelProvider = new PrototypeBeanProvider<PropertyPanel>() {
		@Override
		public PropertyPanel create() throws Exception {
			PropertyPanel bean = propertyPanelProvider.get();
			bean.setStyleName("jsPropertyPanel");
			return bean;
		}		
	};	
	
	protected static Supplier<PropertyPanel> queryFormPropertyPanelProvider = new PrototypeBeanProvider<PropertyPanel>() {
		@Override
		public PropertyPanel create() throws Exception {
			PropertyPanel bean = propertyPanelProvider.get();
			//bean.setShowGridLines(true);
			//bean.setBorders(true);
			//bean.setNewInstanceProviderProvider(Providers.newInstanceProvider);
			bean.setValueRendering(ValueRendering.gridlines);
			return bean;
		}
	};
	
	private static Supplier<PropertyPanel> serviceRequestPropertyPanelProvider = new PrototypeBeanProvider<PropertyPanel>() {
		@Override
		public PropertyPanel create() throws Exception {
			PropertyPanel bean = propertyPanelProvider.get();
			//bean.setShowGridLines(true);
			//bean.setBorders(true);
			//bean.setNewInstanceProviderProvider(Providers.newInstanceProvider);
			bean.setValueRendering(ValueRendering.gridlines);
			return bean;
		}
	};
	
	private static Supplier<PropertyPanel> widerNamePropertyPanelProvider = new PrototypeBeanProvider<PropertyPanel>() {
		@Override
		public PropertyPanel create() throws Exception {
			PropertyPanel bean = propertyPanelProvider.get();
			bean.setPropertyNameColumnWidth(200);
			return bean;
		}
	};
	
	protected static Supplier<PropertyPanel> gimaPropertyPanelProvider = new PrototypeBeanProvider<PropertyPanel>() {
		@Override
		public PropertyPanel create() throws Exception {
			PropertyPanel bean = propertyPanelProvider.get();
			bean.setActionManager(Controllers.gimaPropertyPanelActionManager.get());
			bean.setSelectionFutureProvider(gimaSelectionConstellationSupplier);
			bean.setHideNonEditableProperties(true);
			bean.setHideNonSimpleProperties(false);
			bean.setValueRendering(ValueRendering.gridlines);
			bean.setNavigationEnabled(Runtime.enableGimaNavigation);
			bean.setEnableMandatoryFieldConfiguration(Runtime.enableGimaMandatoryFieldConfiguration);
			bean.setUseWorkWithEntityExpert(false);
			bean.setCommitAction(null);
			return bean;
		}
	};
	
	protected static Supplier<GIMASelectionConstellation> gimaSelectionConstellationSupplier = new PrototypeBeanProvider<GIMASelectionConstellation>() {
		@Override
		public GIMASelectionConstellation create() throws Exception {
			GIMASelectionConstellation bean = new GIMASelectionConstellation();
			bean.setSelectionContentView(selectionContentViewSupplier.get());
			bean.setGimaDialogSupplier(UiElements.gimaDialogProvider);
			return bean;
		}
	};
	
	protected static Supplier<GIMASelectionConstellation> saveQueryGimaSelectionConstellationSupplier = new PrototypeBeanProvider<GIMASelectionConstellation>() {
		@Override
		public GIMASelectionConstellation create() throws Exception {
			GIMASelectionConstellation bean = new GIMASelectionConstellation();
			bean.setSelectionContentView(saveQuerySelectionContentViewSupplier.get());
			bean.setGimaDialogSupplier(UiElements.gimaDialogProvider);
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
	
	private static Supplier<GIMASelectionContentView> saveQuerySelectionContentViewSupplier = new PrototypeBeanProvider<GIMASelectionContentView>() {
		@Override
		public GIMASelectionContentView create() throws Exception {
			GIMASelectionContentView bean = new GIMASelectionContentView();
			bean.setSelectionConstellation(Constellations.saveQueryDialogWorkbenchSelectionConstellationProvider.get());
			return bean;
		}
	};
	
	protected static Supplier<PropertyPanel> workbenchGimaPropertyPanelProvider = new PrototypeBeanProvider<PropertyPanel>() {
		@Override
		public PropertyPanel create() throws Exception {
			PropertyPanel bean = abstractPropertyPanelProvider.get();
			bean.setSelectionFutureProvider(saveQueryGimaSelectionConstellationSupplier);
			//bean.setSelectionFutureProvider(saveQueryDialogWorkbenchSelectionConstellationDialogProvider);
			bean.setActionManager(Controllers.workbenchPropertyPanelActionManager.get());
			bean.setHideNonEditableProperties(true);
			bean.setHideNonSimpleProperties(false);
			bean.setValueRendering(ValueRendering.gridlinesForEmptyValues);
			bean.setExtendedInlineFields(workbenchExtendedInlineFields.get());
			return bean;
		}
	};
	
	protected static Supplier<PropertyPanel> templateGimaPropertyPanelProvider = new PrototypeBeanProvider<PropertyPanel>() {
		@Override
		public PropertyPanel create() throws Exception {
			PropertyPanel bean = gimaPropertyPanelProvider.get();
			bean.setIconProvider(Providers.templateTypeIconProvider.get());
			return bean;
		}
	};
	
	protected static Supplier<PropertyPanel> userGimaPropertyPanelProvider = new PrototypeBeanProvider<PropertyPanel>() {
		@Override
		public PropertyPanel create() throws Exception {
			PropertyPanel bean = abstractPropertyPanelProvider.get();
			bean.setHideNonEditableProperties(true);
			bean.setHideNonSimpleProperties(false);
			bean.setReadOnly(true);
			bean.setSpecialFlowCodecRegistry(Codecs.userSpecialFlowCodecRegistry.get());
			return bean;
		}
	};
	
	private static Supplier<PropertyPanel> readOnlyPropertyPanelWithGeneralProvider = new PrototypeBeanProvider<PropertyPanel>() {
		@Override
		public PropertyPanel create() throws Exception {
			PropertyPanel bean = abstractPropertyPanelProvider.get();
			bean.setReadOnly(true);
			return bean;
		}
	};
	
	private static Supplier<PropertyPanel> selectionPropertyPanelWithGeneralProvider = new PrototypeBeanProvider<PropertyPanel>() {
		@Override
		public PropertyPanel create() throws Exception {
			PropertyPanel bean = abstractPropertyPanelProvider.get();
			bean.setReadOnly(true);
			bean.configureUseCase(Runtime.selectionUseCaseProvider.get());
			return bean;
		}
	};
	
	private static Supplier<PropertyPanel> localModePropertyPanelProvider = new PrototypeBeanProvider<PropertyPanel>() {
		@Override
		public PropertyPanel create() throws Exception {
			PropertyPanel bean = abstractPropertyPanelProvider.get();
			bean.setReadOnly(true);
			bean.setNavigationEnabled(false);
			return bean;
		}
	};
	
	protected static Supplier<PropertyPanel> errorPropertyPanelProvider = new PrototypeBeanProvider<PropertyPanel>() {
		@Override
		public PropertyPanel create() throws Exception {
			PropertyPanel bean = abstractPropertyPanelProvider.get();
			bean.setActionManager(null);
			bean.setReadOnly(true);
			bean.setNavigationEnabled(false);
			bean.configureGmSession(Session.transientManagedSession.get());
			return bean;
		}
	};
	
	protected static Supplier<VerticalTabPanel> verticalTabPanelProvider = new PrototypeBeanProvider<VerticalTabPanel>() {
		@Override
		public VerticalTabPanel create() throws Exception {
			VerticalTabPanel bean = new VerticalTabPanel();
			bean.setMaxNumberOfNonStaticElements(15);
			bean.setVerticalTabPanelLoaderManager(Controllers.verticalTabManager.get());
			bean.setReuseWorkbenchActionContextTabElement(Runtime.reuseWokbenchActionContextTabs.get());
			bean.setDisplayIconsForNonStaticElements(true);
			return bean;
		}
	};

	protected static Supplier<VerticalTabActionMenu> constellationActionBarProvider = new PrototypeBeanProvider<VerticalTabActionMenu>() {
		@Override
		public VerticalTabActionMenu create() throws Exception {
			VerticalTabActionMenu bean = new VerticalTabActionMenu();
			bean.setVerticalTabPanelLoaderManager(Controllers.browsingConstellationActionBarManager.get());
			//bean.setReuseWorkbenchActionContextTabElement(Runtime.reuseWokbenchActionContextTabs.get());
			bean.setReuseWorkbenchActionContextTabElement(false);
			bean.setClosableItems(false);
			bean.setAlwaysFireElement(true);
			bean.setUseHorizontalTabs(true);
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			bean.setSeparatorActionProvider(Actions.separatorAction);
			bean.setContentMenuActionProvider(Actions.contentMenuAction);			
			bean.setUseSeparatorForDynamic(true);
			return bean;
		}
	};
	
	protected static Supplier<Workbench> workbenchProvider = new SessionScopedBeanProvider<Workbench>() {
		@Override
		public Workbench create() throws Exception {
			Workbench bean = publish(new Workbench());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			bean.setDataSession(Session.persistenceSession.get());
			bean.setTransientSession(Session.transientManagedSession.get());
			bean.setRootFolderName(Runtime.workbenchRootFolderName.get());
			bean.setGIMADialogProvider(UiElements.gimaDialogProvider);
			bean.setFieldDialogOpenerActions(Constellations.fieldDialogOpenerActionsMap.get());
			bean.configureUseCase(Runtime.entryPointPanelUseCaseProvider.get());
			
			WorkbenchController controller = new WorkbenchController();
			controller.setWorkbench(bean);
			
			return bean;
		}
	};
	
	private static Supplier<ThumbnailPanel> abstractThumbnailPanelProvider = new PrototypeBeanProvider<ThumbnailPanel>() {
		{
			setAbstract(true);
		}
		@Override
		public ThumbnailPanel create() throws Exception {
			ThumbnailPanel bean = new ThumbnailPanel();
			bean.setPreviewUtil(previewUtitl.get());
			bean.setSimplifiedEntityTypes(simplifiedEntityTypes.get());
			bean.setGmViewActionBarProvider(gmViewActionBar);
			bean.setIconProvider(Providers.bigTypeIconProvider.get());
			bean.setMaxThumbnailSize(300);
			bean.setActionManager(Controllers.actionManager.get());
			bean.setUseModelSession(true);
			bean.setWorkbenchActionHandlerRegistry(Actions.workbenchActionHandlerRegistry.get());
			bean.setCodecRegistry(Codecs.renderersCodecRegistry.get());
			bean.setSpecialEntityTraversingCriterion(specialEntityTraversingCriterionMap.get());
			return bean;
		}
	};
	
	protected static Supplier<ThumbnailPanel> editThumbnailPanelProvider = new PrototypeBeanProvider<ThumbnailPanel>() {
		@Override
		public ThumbnailPanel create() throws Exception {
			ThumbnailPanel bean = abstractThumbnailPanelProvider.get();
			//bean.setRasterImageResourceProvider(Providers.folderIconsRasterImageProvider.get());
			bean.setThumbnailSize(150);
			bean.setDefaultContextMenuActionSupplier(Constellations.globalActionsToolBar.get(),
					com.braintribe.gwt.gme.constellation.client.LocalizedText.INSTANCE.newEntry());
			bean.setGmeDragAndDropSupport(Providers.gmeDragAndDropSupport.get());
			bean.setUseGroups(true);
			bean.setUseGroupTogether(false);
			bean.setWorkbenchActionSelectionHandler(Constellations.explorerConstellationProvider.get());
			Notifications.refreshPreviewExpert.get().addPreviewPanel(bean);

			return bean;
		}
	};
	
	public static Supplier<ThumbnailPanel> jsThumbnailPanelProvider = new PrototypeBeanProvider<ThumbnailPanel>() {
		@Override
		public ThumbnailPanel create() throws Exception {
			ThumbnailPanel bean = new JsThumbnailPanel();
			bean.setPreviewUtil(previewUtitl.get());
			bean.setSimplifiedEntityTypes(simplifiedEntityTypes.get());
			bean.setGmViewActionBarProvider(gmViewActionBar);
			bean.setIconProvider(Providers.bigTypeIconProvider.get());
			bean.setMaxThumbnailSize(300);
			bean.setActionManager(Controllers.actionManager.get());
			bean.setUseModelSession(true);
			bean.setWorkbenchActionHandlerRegistry(Actions.workbenchActionHandlerRegistry.get());
			bean.setCodecRegistry(Codecs.renderersCodecRegistry.get());
			bean.setSpecialEntityTraversingCriterion(specialEntityTraversingCriterionMap.get());						
			bean.configureUseCase(Runtime.thumbnailPanelUseCaseProvider.get());
			bean.setDefaultContextMenuActionSupplier(Constellations.globalActionsToolBar.get(),
					com.braintribe.gwt.gme.constellation.client.LocalizedText.INSTANCE.newEntry());
			bean.setRasterImageResourceProvider(Providers.folderIconsRasterImageProvider.get());
			bean.setGmeDragAndDropSupport(Providers.gmeDragAndDropSupport.get());
			bean.setThumbnailSize(150);
			bean.setUseGroups(true);
			bean.setUseGroupTogether(false);
			bean.setWorkbenchActionSelectionHandler(Constellations.explorerConstellationProvider.get());
			bean.setHeight("auto");
			bean.setWidth("auto");
			bean.setStyleName("jsThumbnailPanel");
			return bean;
		}
	};
	
	protected static Supplier<ThumbnailPanel> thumbnailPanelProvider = new PrototypeBeanProvider<ThumbnailPanel>() {
		@Override
		public ThumbnailPanel create() throws Exception {
			ThumbnailPanel bean = abstractThumbnailPanelProvider.get();
			bean.configureUseCase(Runtime.thumbnailPanelUseCaseProvider.get());
			bean.setRasterImageResourceProvider(Providers.folderIconsRasterImageProvider.get());
			bean.setThumbnailSize(150);
			bean.addInteractionListener(homeActionTrigger.get());
			bean.setPrepareToolBarActions(false);
			bean.setUnselectAfterClick(true);
			bean.setUseModelSession(false);
			return bean;
		}
	};
	
	protected static Supplier<ThumbnailPanel> selectionThumbnailPanelProvider = new SelectionConstellationScopedBeanProvider<ThumbnailPanel>() {
		@Override
		public ThumbnailPanel create() throws Exception {
			ThumbnailPanel bean = publish(abstractThumbnailPanelProvider.get());
			bean.configureUseCase(Runtime.selectionUseCaseProvider.get());
			bean.setRasterImageResourceProvider(Providers.folderIconsRasterImageProvider.get());
			bean.setThumbnailSize(300);
			bean.addInteractionListener(selectionHomeActionTrigger.get());
			bean.setPrepareToolBarActions(false);
			bean.setUnselectAfterClick(true);
			bean.setUseModelSession(false);
			return bean;
		}
	};
	
	private static Supplier<HomeActionTrigger> homeActionTrigger = new SessionScopedBeanProvider<HomeActionTrigger>() {
		@Override
		public HomeActionTrigger create() throws Exception {
			HomeActionTrigger bean = publish(new HomeActionTrigger());
			bean.setGmSession(Session.persistenceSession.get());
			bean.setParentPanel(Constellations.explorerConstellationProvider.get());
			bean.setWorkbenchActionHandlerRegistry(Actions.workbenchActionHandlerRegistry.get());
			bean.setHomeConstellation(Constellations.homeConstellationProvider.get());
			return bean;
		}
	};
	
	private static Supplier<HomeActionTrigger> selectionHomeActionTrigger = new SelectionConstellationScopedBeanProvider<HomeActionTrigger>() {
		@Override
		public HomeActionTrigger create() throws Exception {
			HomeActionTrigger bean = new HomeActionTrigger();
			bean.setGmSession(Session.persistenceSession.get());
			bean.setParentPanel(Constellations.selectionConstellationProvider.get());
			bean.setWorkbenchActionHandlerRegistry(Actions.workbenchActionHandlerRegistry.get());
			return bean;
		}
	};
	
	protected static Supplier<TemplateEditorPanel> templateEditorPanelProvider = new PrototypeBeanProvider<TemplateEditorPanel>() {
		@Override
		public TemplateEditorPanel create() throws Exception {
			TemplateEditorPanel bean = (new TemplateEditorPanel());
			bean.setAssemblyPanelProvider(templateAssemblyPanelProvider);
			bean.setActionManager(Controllers.actionManager.get());
			return bean;
		}
	};
	
	protected static Supplier<TemplateEvaluationDialog> templateEvaluationDialogProvider = new PrototypeBeanProvider<TemplateEvaluationDialog>() {
		@Override
		public TemplateEvaluationDialog create() throws Exception {
			TemplateEvaluationDialog bean = new TemplateEvaluationDialog();
			bean.setTetherBar(Panels.unclickableTetherBarProvider.get());
			bean.setPropertyPanelProvider(Panels.templateGimaPropertyPanelProvider);
			bean.setMasterDetailConstellationProvider(Constellations.gimaMasterDetailConstellationProvider);
			bean.setGmSession(Session.persistenceSession.get());
			bean.setWorkbenchSession(Session.templateWorkbenchPersistenceSession.get());
			bean.setUseCase(Runtime.templateEditorUseCaseProvider.get());
			//bean.setSelectionFutureSupplier(Panels.selectionConstellationDialogProvider);
			bean.setSelectionFutureSupplier(gimaSelectionConstellationSupplier);
			bean.setSingleTypesToShowGIMA(singleTypesToShowGIMA.get());
			bean.setValidation(templateGimaValidation);
			bean.setTypeSelectionViewSupplier(UiElements.typeSelectionViewSupplier);
			return bean;
		}
	};
	
	protected static Supplier<Set<String>> singleTypesToShowGIMA = new SingletonBeanProvider<Set<String>>() {
		@Override
		public Set<String> create() throws Exception {
			Set<String> bean = new FastSet();
			bean.add(Resource.T.getTypeSignature());
			bean.add(TimeSpan.T.getTypeSignature());
			return bean;
		}
	};
	
	protected static Supplier<ValidationLogRepresentation> validationLogListPanel = new SessionScopedBeanProvider<ValidationLogRepresentation>() {
		@Override
		public ValidationLogRepresentation create() throws Exception {
			ValidationLogListPanel bean = (ValidationLogListPanel) publish(new ValidationLogListPanel());
			bean.setEditEntityActionListener(Constellations.explorerConstellationProvider.get());
			bean.setUndoAction(Actions.undoAction.get());
			return bean;
		}
	};
	
	/*protected static Supplier<ValidationLogRepresentation> templateGimaValidationLogListPanel = new SessionScopedBeanProvider<ValidationLogRepresentation>() {
		@Override
		public ValidationLogRepresentation create() throws Exception {
			ValidationLogListPanel bean = (ValidationLogListPanel) publish(new ValidationLogListPanel());
			return bean;
		}
	};*/
	
	protected static Supplier<Validation> validation = new SessionScopedBeanProvider<Validation>() {		
		@Override
		public Validation create() throws Exception {
			Validation bean = publish(new Validation());
			bean.setPredator(predator.get());
			bean.setGmSession(Session.persistenceSession.get());
			return bean;
		}
	};

	protected static Supplier<Validation> workbenchValidation = new SessionScopedBeanProvider<Validation>() {		
		@Override
		public Validation create() throws Exception {
			Validation bean = publish(new Validation());
			bean.setPredator(workbenchPredator.get());
			bean.setGmSession(Session.workbenchPersistenceSession.get());
			return bean;
		}
	};
	
	protected static Supplier<Validation> userValidation = new SessionScopedBeanProvider<Validation>() {		
		@Override
		public Validation create() throws Exception {
			Validation bean = publish(new Validation());
			bean.setPredator(userPredator.get());
			bean.setGmSession(Session.userSession.get());
			return bean;
		}
	};

	protected static Supplier<Validation> templateGimaValidation = new SessionScopedBeanProvider<Validation>() {		
		@Override
		public Validation create() throws Exception {
			Validation bean = publish(new Validation());
			bean.setPredator(templateGimaPredator.get());
			return bean;
		}
	};

	protected static Supplier<Validation> transientValidation = new SessionScopedBeanProvider<Validation>() {		
		@Override
		public Validation create() throws Exception {
			Validation bean = publish(new Validation());
			bean.setPredator(transientPredator.get());
			bean.setGmSession(Session.transientManagedSession.get());
			return bean;
		}
	};
	
	private static Supplier<Validation> transienServiceRequestValidation = new ServiceRequestConstellationScopedBeanProvider<Validation>() {		
		@Override
		public Validation create() throws Exception {
			Validation bean = publish(new Validation());
			bean.setPredator(transientServiceRequestPredator.get());
			bean.setGmSession(Session.serviceRequestScopedTransientGmSession.get());
			return bean;
		}
	};
	
	private static Supplier<Predator> predator = new SessionScopedBeanProvider<Predator>() {
		@Override
		public Predator create() throws Exception {
			Predator bean = publish(new Predator());
			bean.setGmSession(Session.persistenceSession.get());
			bean.setExpertRegistry(Runtime.validatorExpertRegistry.get());
			return bean;
		}
	};
	
	private static Supplier<Predator> workbenchPredator = new SessionScopedBeanProvider<Predator>() {
		@Override
		public Predator create() throws Exception {
			Predator bean = publish(new Predator());
			bean.setGmSession(Session.workbenchPersistenceSession.get());
			bean.setExpertRegistry(Runtime.validatorExpertRegistry.get());
			return bean;
		}
	};

	private static Supplier<Predator> userPredator = new SessionScopedBeanProvider<Predator>() {
		@Override
		public Predator create() throws Exception {
			Predator bean = publish(new Predator());
			bean.setGmSession(Session.userSession.get());
			bean.setExpertRegistry(Runtime.validatorExpertRegistry.get());
			return bean;
		}
	};

	private static Supplier<Predator> templateGimaPredator = new SessionScopedBeanProvider<Predator>() {
		@Override
		public Predator create() throws Exception {
			Predator bean = publish(new Predator());
			bean.setExpertRegistry(Runtime.validatorExpertRegistry.get());
			return bean;
		}
	};

	private static Supplier<Predator> transientPredator = new SessionScopedBeanProvider<Predator>() {
		@Override
		public Predator create() throws Exception {
			Predator bean = publish(new Predator());
			bean.setGmSession(Session.transientManagedSession.get());
			bean.setExpertRegistry(Runtime.validatorExpertRegistry.get());
			return bean;
		}
	};

	private static Supplier<Predator> transientServiceRequestPredator = new ServiceRequestConstellationScopedBeanProvider<Predator>() {
		@Override
		public Predator create() throws Exception {
			Predator bean = publish(new Predator());
			bean.setGmSession(Session.serviceRequestScopedTransientGmSession.get());
			bean.setExpertRegistry(Runtime.validatorExpertRegistry.get());
			return bean;
		}
	};
	
	protected static Supplier<SpotlightPanel> workbenchSpotlightPanelProvider = new PrototypeBeanProvider<SpotlightPanel>() {
		@Override
		public SpotlightPanel create() throws Exception {
			SpotlightPanel bean = new SpotlightPanel();
			bean.setSimpleTypesValuesProvider(simpleTypeParserProvider);
			bean.setGmSession(Session.workbenchPersistenceSession.get());
			bean.setCodecRegistry(Codecs.renderersCodecRegistry.get());
			bean.setEntitiesFutureProvider(workbenchEntitiesFutureProvider.get());
			bean.setPatternMatchers(Arrays.asList(new SubstringCheckingPatternMatcher(), new CamelCasePatternMatcher()));
			bean.setQueryActionsProvider(queryActionsProvider.get());
			bean.setInstantiationActionsProvider(instantiationActionsProvider.get());
			bean.setIconProvider(Providers.typeIconProvider);
			bean.setUseCase(Runtime.quickAccessPanelUseCaseProvider.get());
			bean.setExpertUIMap(workbenchExpertUIMapProvider.get());
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
			bean.setEntitiesFutureProvider(entitiesFutureProvider.get());
			bean.setPatternMatchers(Arrays.asList(new SubstringCheckingPatternMatcher(), new CamelCasePatternMatcher()));
			bean.setQueryActionsProvider(queryActionsProvider.get());
			bean.setInstantiationActionsProvider(instantiationActionsProvider.get());
			bean.setIconProvider(Providers.typeIconProvider);
			bean.setUseCase(Runtime.quickAccessPanelUseCaseProvider.get());
			bean.setExpertUIMap(expertUIMapProvider.get());
			bean.setShowAbstractTypes(true);
			return bean;
		}
	};

	protected static Supplier<SpotlightPanel> spotlightPanelWithoutTypesProvider = new PrototypeBeanProvider<SpotlightPanel>() {
		@Override
		public SpotlightPanel create() throws Exception {
			SpotlightPanel bean = spotlightPanelProvider.get();
			//bean.setShowAbstractTypes(false);
			bean.setLoadTypes(false);
			bean.setDisplaySimpleQueryActions(false);
			//bean.setEnableGroups(false);
			bean.configureUseQueryActions(false);
			bean.configureEnableInstantiation(false);
			bean.setExpertUIMap(null);
			return bean;
		}
	};
		
	protected static Supplier<SpotlightPanel> querySpotlightPanelProvider = new PrototypeBeanProvider<SpotlightPanel>() {
		@Override
		public SpotlightPanel create() throws Exception {
			SpotlightPanel bean = new SpotlightPanel();
			bean.setSimpleTypesValuesProvider(simpleTypeParserProvider);
			bean.setGmSession(Session.persistenceSession.get());
			bean.setCodecRegistry(Codecs.renderersCodecRegistry.get());
			bean.setEntitiesFutureProvider(entitiesFutureProvider.get());
			bean.setPatternMatchers(Arrays.asList(new SubstringCheckingPatternMatcher(), new CamelCasePatternMatcher()));
			bean.setIconProvider(Providers.typeIconProvider);
			bean.setUseCase(Runtime.quickAccessPanelUseCaseProvider.get());
			bean.setLoadTypes(false);
			//bean.setShowTypesSection(false);
			return bean;
		}
	};
	
	public static Supplier<SpotlightPanel> spotlightMetaDataPanelProvider = new PrototypeBeanProvider<SpotlightPanel>() {
		@Override
		public SpotlightPanel create() throws Exception {
			SpotlightPanel bean = new SpotlightPanel();
			bean.setSimpleTypesValuesProvider(simpleTypeMetaDataTypeParserProvider);
			bean.setGmSession(Session.persistenceSession.get());
			bean.setCodecRegistry(Codecs.renderersCodecRegistry.get());
			bean.setEntitiesFutureProvider(entitiesFutureProvider.get());
			bean.setPatternMatchers(Arrays.asList(new SubstringCheckingPatternMatcher(), new CamelCasePatternMatcher()));
			bean.setQueryActionsProvider(queryActionsProvider.get());
			bean.setInstantiationActionsProvider(instantiationActionsProvider.get());
			bean.setIconProvider(Providers.typeIconProvider);
			bean.setUseCase(Runtime.quickAccessPanelUseCaseProvider.get());
			//bean.setExpertUIMap(expertUIMapProvider.get());			
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
	
	protected static Supplier<SpotlightPanel> workbenchSpotlightPanelNewInstanceProvider = new PrototypeBeanProvider<SpotlightPanel>() {
		@Override
		public SpotlightPanel create() throws Exception {
			SpotlightPanel bean = workbenchSpotlightPanelProvider.get();
			bean.setShowTemplates(false);
			return bean;
		}
	};
	
	public static Supplier<SpotlightPanel> entityCollectionSpotlightPanel = new PrototypeBeanProvider<SpotlightPanel>() {
		@Override
		public SpotlightPanel create() throws Exception {
			SpotlightPanel bean = new SpotlightPanel();
			bean.setSimpleTypesValuesProvider(simpleTypeParserProvider);
			bean.setGmSession(Session.persistenceSession.get());
			bean.setCodecRegistry(Codecs.queryRenderersCodecRegistry.get());
			bean.setEntitiesFutureProvider(entitiesFutureProvider.get());
			bean.setPatternMatchers(Arrays.asList(new SubstringCheckingPatternMatcher(), new CamelCasePatternMatcher()));
			bean.setIconProvider(Providers.typeIconProvider);
			bean.setUseCase(Runtime.quickAccessPanelUseCaseProvider.get());
			bean.setUseApplyButton(true);
			bean.setLoadTypes(false);
			return bean;
		}
	};
	
	protected static Supplier<Map<String, Supplier<? extends ExpertUI<?>>>> expertUIMapProvider = new SingletonBeanProvider<Map<String, Supplier<? extends ExpertUI<?>>>>() {
		@Override
		public Map<String, Supplier<? extends ExpertUI<?>>> create() throws Exception {
			Map<String, Supplier<? extends ExpertUI<?>>> bean = new FastMap<Supplier<? extends ExpertUI<?>>>();
			bean.put(Resource.T.getTypeSignature(), UiElements.resourceExpertUIProvider);
			return bean;
		}
	};
	
	private static Supplier<Map<String, Supplier<? extends ExpertUI<?>>>> workbenchExpertUIMapProvider = new SingletonBeanProvider<Map<String, Supplier<? extends ExpertUI<?>>>>() {
		@Override
		public Map<String, Supplier<? extends ExpertUI<?>>> create() throws Exception {
			Map<String, Supplier<? extends ExpertUI<?>>> bean = new FastMap<Supplier<? extends ExpertUI<?>>>();
			bean.put(Resource.T.getTypeSignature(), UiElements.workbenchResourceExpertUIProvider);
			return bean;
		}
	};
		
	private static Supplier<MultiDescriptionStringParser> simpleTypeMetaDataTypeParserProvider = new SingletonBeanProvider<MultiDescriptionStringParser>() {
        @Override
        public MultiDescriptionStringParser create() throws Exception {
        	MultiDescriptionStringParser bean = new MultiDescriptionStringParser("useCase", "role");
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
	
	private static Supplier<EntitiesFutureProvider> entitiesFutureProvider = new SessionScopedBeanProvider<EntitiesFutureProvider>() {
		@Override
		public EntitiesFutureProvider create() throws Exception {
			EntitiesFutureProvider bean = publish(new EntitiesFutureProvider());
			bean.setGmSession(Session.persistenceSession.get());
			return bean;
		}
	};
	
	private static Supplier<EntitiesFutureProvider> workbenchEntitiesFutureProvider = new SessionScopedBeanProvider<EntitiesFutureProvider>() {
		@Override
		public EntitiesFutureProvider create() throws Exception {
			EntitiesFutureProvider bean = publish(new EntitiesFutureProvider());
			bean.setGmSession(Session.workbenchPersistenceSession.get());
			return bean;
		}
	};
	
	private static Supplier<QueryActionsProvider> queryActionsProvider = new SessionScopedBeanProvider<QueryActionsProvider>() {
		@Override
		public QueryActionsProvider create() throws Exception {
			QueryActionsProvider bean = publish(new QueryActionsProvider());
			bean.setGmSession(Session.workbenchPersistenceSession.get());
			return bean;
		}
	};
	
	protected static Supplier<InstantiationActionsProvider> instantiationActionsProvider = new SessionScopedBeanProvider<InstantiationActionsProvider>() {
		@Override
		public InstantiationActionsProvider create() throws Exception {
			InstantiationActionsProvider bean = publish(new InstantiationActionsProvider());
			bean.setGmSession(Session.workbenchPersistenceSession.get());
			if(Runtime.useCommit)
				Constellations.customizationConstellationProvider.get().addModelEnvironmentSetListener(bean);
			return bean;
		}
	};
	
	protected static Supplier<ResourceUploadDialog> resourceUploadDialogProvider = new SessionScopedBeanProvider<ResourceUploadDialog>() {
		@Override
		public ResourceUploadDialog create() throws Exception {
			ResourceUploadDialog bean = publish(new ResourceUploadDialog());
			bean.setResourceUploadPanel(resourceUploadPanelProvider.get());
			return bean;
		}
	};
	
	private static Supplier<ResourceUploadPanel> abstractResourceUploadPanel = new PrototypeBeanProvider<ResourceUploadPanel>() {
		{
			setAbstract(true);
		}
		@Override
		public ResourceUploadPanel create() throws Exception {
			ResourceUploadPanel bean = new ResourceUploadPanel();
			bean.setResourceBuilder(restBasedResourceProcessingProvider.get());
			bean.setGmSession(Session.persistenceSession.get());
			return bean;
		}
	};
	
	private static Supplier<ResourceUploadPanel> resourceUploadPanelProvider = new PrototypeBeanProvider<ResourceUploadPanel>() {
		@Override
		public ResourceUploadPanel create() throws Exception {
			ResourceUploadPanel bean = abstractResourceUploadPanel.get();
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			return bean;
		}
	};
	
	private static Supplier<ResourceUploadPanel> abstractSmallResourceUploadPanelProvider = new PrototypeBeanProvider<ResourceUploadPanel>() {
		{
			setAbstract(true);
		}
		@Override
		public ResourceUploadPanel create() throws Exception {
			ResourceUploadPanel bean = abstractResourceUploadPanel.get();
			bean.setDragAndDropSize("250px", "20px");
			bean.setShowPanelAfterUpload(false);
			return bean;
		}
	};
	
	private static Supplier<ResourceUploadPanel> smallResourceUploadPanelProvider = new PrototypeBeanProvider<ResourceUploadPanel>() {
		@Override
		public ResourceUploadPanel create() throws Exception {
			ResourceUploadPanel bean = abstractSmallResourceUploadPanelProvider.get();
			return bean;
		}
	};
	
	private static Supplier<ResourceUploadPanel> workbenchSmallResourceUploadPanelProvider = new PrototypeBeanProvider<ResourceUploadPanel>() {
		@Override
		public ResourceUploadPanel create() throws Exception {
			ResourceUploadPanel bean = abstractSmallResourceUploadPanelProvider.get();
			bean.setGmSession(Session.workbenchPersistenceSession.get());
			return bean;
		}
	};
	
	protected static Supplier<ResourceUploadPanel> noRefreshResourceUploadPanelProvider = new PrototypeBeanProvider<ResourceUploadPanel>() {
		@Override
		public ResourceUploadPanel create() throws Exception {
			ResourceUploadPanel bean = abstractResourceUploadPanel.get();
			return bean;
		}
	};
	
	protected static Supplier<ResourceUploadPanel> noRefreshWorkbenchResourceUploadPanelProvider = new PrototypeBeanProvider<ResourceUploadPanel>() {
		@Override
		public ResourceUploadPanel create() throws Exception {
			ResourceUploadPanel bean = new ResourceUploadPanel();
			bean.setResourceBuilder(restBasedResourceProcessingProvider.get());
			bean.setGmSession(Session.workbenchPersistenceSession.get());
			return bean;
		}
	};
	
	protected static Supplier<StreamingBasedResourceProcessing> streamingBasedResourceProcessingProvider = new SessionScopedBeanProvider<StreamingBasedResourceProcessing>() {	
		@Override	
		public StreamingBasedResourceProcessing create() throws Exception {	
			StreamingBasedResourceProcessing bean = new StreamingBasedResourceProcessing();
			bean.setSessionProvider(Providers.sessionIdProvider.get());
			bean.setStreamingServletContext(Runtime.tribefireServicesUrl.get());	
			return bean;	
		}	
	};
	
	protected static Supplier<RestBasedResourceProcessing> restBasedResourceProcessingProvider = new SessionScopedBeanProvider<RestBasedResourceProcessing>() {	
		@Override	
		public RestBasedResourceProcessing create() throws Exception {	
			RestBasedResourceProcessing bean = new RestBasedResourceProcessing();
			bean.setSessionProvider(Providers.sessionIdProvider.get());
			bean.setStreamBaseUrl(Runtime.tribefireServicesUrl.get() + "api/v1/");
			return bean;	
		}	
	};	

	protected static Supplier<HyperLinkContentViewPanel> hyperLinkContentViewPanelProvider = new PrototypeBeanProvider<HyperLinkContentViewPanel>() {
		@Override
		public HyperLinkContentViewPanel create() throws Exception {
			HyperLinkContentViewPanel bean = new HyperLinkContentViewPanel();
			return bean;
		}
	};
	
	protected static Supplier<TabbedGmEntityView> modellerTabbedPropertyPanel = new PrototypeBeanProvider<TabbedGmEntityView>() {
		@Override
		public TabbedGmEntityView create() throws Exception {
			TabbedGmEntityView bean = new TabbedGmEntityView();
			bean.setTabbedGmEntityViewContexts(tabbedGmEntityViewContexts.get());
			//bean.setAction(Actions.detailsPanelVisibilityAction.get());
			return bean;
		}
	};
	
	private static Supplier<SimplePropertyPanel> simplePropertyPanel = new PrototypeBeanProvider<SimplePropertyPanel>() {
		@Override
		public SimplePropertyPanel create() throws Exception {
			SimplePropertyPanel bean = new SimplePropertyPanel();
			bean.setCommitAction(Actions.saveAction.get());
			return bean;
		}
	};
	
	private static Supplier<List<TabbedGmEntityViewContext>> tabbedGmEntityViewContexts = new PrototypeBeanProvider<List<TabbedGmEntityViewContext>>() {
		@Override
		public List<TabbedGmEntityViewContext> create() throws Exception {
			List<TabbedGmEntityViewContext> bean = new ArrayList<TabbedGmEntityViewContext>();
			
			TabbedGmEntityViewContext simplePropertyContext = new TabbedGmEntityViewContext("Properties", "Properties", simplePropertyPanel.get());
			TabbedGmEntityViewContext propertyPanelContext = new TabbedGmEntityViewContext(LocalizedText.INSTANCE.details(), LocalizedText.INSTANCE.details(), propertyPanelProvider.get());
			
			bean.add(propertyPanelContext);
			bean.add(simplePropertyContext);		
			
			return bean;
		}
	};
	
	protected static Supplier<TabbedGmEntityView> processDesignerPropertyPanel = new PrototypeBeanProvider<TabbedGmEntityView>() {
		@Override
		public TabbedGmEntityView create() throws Exception {
			TabbedGmEntityView bean = new TabbedGmEntityView();
			bean.setHeaderPanel(generalPanel.get());
			bean.setTabbedGmEntityViewContexts(processDesignerGmEntityViewContexts.get());
			//bean.setAction(Actions.detailsPanelVisibilityAction.get());
			return bean;
		}
	};
	
	private static Supplier<List<TabbedGmEntityViewContext>> processDesignerGmEntityViewContexts = new PrototypeBeanProvider<List<TabbedGmEntityViewContext>>() {
		@Override
		public List<TabbedGmEntityViewContext> create() throws Exception {
			List<TabbedGmEntityViewContext> bean = new ArrayList<TabbedGmEntityViewContext>();
			
			TabbedGmEntityViewContext propertyPanelContext = new TabbedGmEntityViewContext(LocalizedText.INSTANCE.details(), LocalizedText.INSTANCE.details(), widerNamePropertyPanelProvider.get());
			
			bean.add(propertyPanelContext);
			
			return bean;
		}
	};
	
	protected static Supplier<SmartMapper> smartMapper = new PrototypeBeanProvider<SmartMapper>() {
		@Override
		public SmartMapper create() throws Exception {
			SmartMapper bean = new SmartMapper();
			bean.setSession(Session.persistenceSession.get());
			bean.setSpotlightPanelProvider(spotlightPanelProvider);
//			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
//			bean.setInstanceSelectionFutureProviderProvider(selectionConstellationDialogProvider);
			return bean;
		}
	};
	
	protected static Supplier<GmViewActionBar> gmViewActionBar = new SessionScopedBeanProvider<GmViewActionBar>() {
		@Override
		public DefaultGmViewActionBar create() throws Exception {
			DefaultGmViewActionBar bean = (DefaultGmViewActionBar) publish(new DefaultGmViewActionBar());
			bean.setGlobalActionsToolBar(Constellations.globalActionsToolBar.get());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			bean.setGmSession(Session.persistenceSession.get());
			bean.setWorkbenchActionHandlerRegistry(Actions.workbenchActionHandlerRegistry.get());
			bean.setRootFolderLoader(Controllers.actionBarFolderLoader.get());
			bean.setActionFolderContentExpert(Actions.actionFolderContentExpert.get());
			RootKeyNavExpert.addRootKeyNavListener(bean);
			return bean;
		}
	};
		
	protected static Supplier<ServiceRequestPanel> serviceRequestPanel = new PrototypeBeanProvider<ServiceRequestPanel>() {
		@Override
		public ServiceRequestPanel create() throws Exception {
			ServiceRequestPanel bean = new ServiceRequestPanel();
			bean.setPropertyPanel(serviceRequestPropertyPanelProvider.get());
			bean.setDataSession(Session.persistenceSession.get());
			bean.setTransientSession(Session.transientManagedSession.get());
			bean.setTransientSessionProvider(Session.serviceRequestScopedTransientGmSession);
			bean.setNotificationFactory(Notifications.notificationFactory);
			bean.setUseCase(Runtime.serviceRequestPanelUseCaseProvider.get());
			bean.setValidation(transienServiceRequestValidation.get());
			return bean;
		}
	};
	
	protected static Supplier<ServiceRequestExecutionPanel> serviceRequestExecutionPanel = new PrototypeBeanProvider<ServiceRequestExecutionPanel>() {
		@Override
		public ServiceRequestExecutionPanel create() throws Exception {
			ServiceRequestExecutionPanel bean = new ServiceRequestExecutionPanel();
			bean.setUseCase(Runtime.serviceRequestPanelUseCaseProvider.get());
			bean.setDataSession(Session.persistenceSession.get());
			bean.setTransientSession(Session.transientManagedSession.get());
			bean.setTransientSessionSupplier(Session.serviceRequestScopedTransientGmSession);
			bean.setNotificationFactory(Notifications.notificationFactory);
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			bean.setValidation(transientValidation.get());
			bean.setPropertyPanelSupplier(serviceRequestPropertyPanelProvider);
			return bean;
		}
	};
	
//	private static Supplier<PropertyHeaderPanel> propertyHeaderPanel = new PrototypeBeanProvider<PropertyHeaderPanel>() {
//	@Override
//	public PropertyHeaderPanel create() throws Exception {
//		PropertyHeaderPanel bean = new PropertyHeaderPanel();
//		bean.setIconProvider(Providers.typeIconProvider.get());
//		bean.setBigIconProvider(Providers.bigTypeIconProvider.get());
//		bean.setAction(Actions.detailsPanelVisibilityAction.get());
//		return bean;
//	}
//};
	
	/*public static Supplier<SelectionConstellationDialog> saveQueryDialogWorkbenchSelectionConstellationDialogProvider = new SessionScopedBeanProvider<SelectionConstellationDialog>() {
		@Override
		public SelectionConstellationDialog create() throws Exception {
			SelectionConstellationDialog bean = publish(new SelectionConstellationDialog());
			bean.setSelectionConstellation(Constellations.saveQueryDialogWorkbenchSelectionConstellationProvider.get());
			return bean;
		}
	};
	
	public static Supplier<SelectionConstellationDialog> selectionConstellationDialogProvider = new SessionScopedBeanProvider<SelectionConstellationDialog>() {
		@Override
		public SelectionConstellationDialog create() throws Exception {
			SelectionConstellationDialog bean = publish(new SelectionConstellationDialog());
			bean.setSelectionConstellation(Constellations.selectionConstellationProvider.get());
			return bean;
		}
	};*/
	
}