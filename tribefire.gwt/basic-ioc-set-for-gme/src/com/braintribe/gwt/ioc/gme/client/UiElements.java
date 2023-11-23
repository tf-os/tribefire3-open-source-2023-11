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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.gwt.aceeditor.client.GmScriptEditor;
import com.braintribe.gwt.aceeditor.client.GmScriptEditorDialog;
import com.braintribe.gwt.aceeditor.client.GmScriptEditorView;
import com.braintribe.gwt.customizationui.client.MenuButton;
import com.braintribe.gwt.customizationui.client.PasswordTextField;
import com.braintribe.gwt.customizationui.client.UserNameTextField;
import com.braintribe.gwt.genericmodelgxtsupport.client.GMEditorSupport;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.ExtendedStringDialog;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.LocalizedStringField;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.LocalizedValuesDialog;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.color.ColorField;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.color.ColorPickerWindow;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.font.FontPicker;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.htmleditor.HtmlEditorDialog;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.keyconfiguration.KeyConfigurationField;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.time.TimeSpanField;
import com.braintribe.gwt.gm.storage.impl.wb.WbStorage;
import com.braintribe.gwt.gm.storage.impl.wb.form.save.WbSaveQueryDialog;
import com.braintribe.gwt.gm.storage.impl.wb.form.setting.WbSettingQueryDialog;
import com.braintribe.gwt.gme.actionmenubuilder.client.DefaultActionMenuBuilder;
import com.braintribe.gwt.gme.constellation.client.AccessChoiceDialog;
import com.braintribe.gwt.gme.constellation.client.GIMADialog;
import com.braintribe.gwt.gme.constellation.client.SettingsMenu;
import com.braintribe.gwt.gme.constellation.client.action.AdvancedSaveActionDialog;
import com.braintribe.gwt.gme.constellation.client.expert.ResourceExpertUI;
import com.braintribe.gwt.gme.constellation.client.gima.GIMAActionSelectionView;
import com.braintribe.gwt.gme.constellation.client.gima.GIMATypeSelectionView;
import com.braintribe.gwt.gme.constellation.client.gima.field.GIMAExtendedStringFieldDialog;
import com.braintribe.gwt.gme.constellation.client.gima.field.GIMALocalizedValuesDialog;
import com.braintribe.gwt.gme.constellation.client.gima.field.GIMAScriptEditorDialog;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gme.headerbar.client.DefaultHeaderBar;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessTriggerField;
import com.braintribe.gwt.gme.propertypanel.client.field.SimplifiedEntityField;
import com.braintribe.gwt.gme.propertypanel.client.field.SimplifiedEntityFieldTabConfiguration;
import com.braintribe.gwt.gme.propertypanel.client.field.SimplifiedEntityFieldsProvider;
import com.braintribe.gwt.gme.templateevaluation.client.TemplateGIMADialog;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.util.client.LocaleUtil;
import com.braintribe.gwt.gxt.gxtresources.components.client.BtDateTimePropertyEditor;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.DateTimeField;
import com.braintribe.gwt.gxt.gxtresources.extendedtrigger.client.ExtendedStringField;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.ioc.gme.client.expert.UserImageProvider;
import com.braintribe.gwt.ioc.gme.client.resources.CustomizationResources;
import com.braintribe.gwt.querymodeleditor.client.panels.autocompletion.AutoCompletionPanel;
import com.braintribe.gwt.querymodeleditor.client.panels.editor.QueryModelEditorAdvancedPanel;
import com.braintribe.gwt.querymodeleditor.client.panels.editor.QueryModelEditorPanel;
import com.braintribe.gwt.querymodeleditor.client.queryform.QueryFormDialog;
import com.braintribe.gwt.querymodeleditor.client.queryform.QueryFormTemplate;
import com.braintribe.gwt.security.client.SessionScopedBeanProvider;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.style.Color;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.workbench.KeyConfiguration;
import com.braintribe.provider.PrototypeBeanProvider;
import com.braintribe.provider.SingletonBeanProvider;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.widget.core.client.form.Field;

import tribefire.extension.scripting.model.deployment.Script;

/**
 * This is the IoC configuration for the UiElements.
 *
 */
class UiElements {

	/*
	 * private static boolean enableAccessExchange = true;
	 *
	 * public static void setEnableAccessExchange(boolean enableAccessExchange) { UiElements.enableAccessExchange = enableAccessExchange; }
	 */

	protected static Supplier<MenuButton> showSettingsMenuAction = new SessionScopedBeanProvider<MenuButton>() {
		@Override
		public MenuButton create() throws Exception {
			final MenuButton bean = publish(new MenuButton());
			bean.setMenu(settingsMenu.get());
			bean.setName("");
			bean.setIcon(ConstellationResources.INSTANCE.settings32());
			bean.setHoverIcon(ConstellationResources.INSTANCE.settings32());
			return bean;
		}
	};

	protected static Supplier<SettingsMenu> settingsMenu = new SessionScopedBeanProvider<SettingsMenu>() {
		@Override
		public SettingsMenu create() throws Exception {
			final SettingsMenu bean = publish(new SettingsMenu());
			bean.setMenuActions(Actions.settingsMenuActions.get());
			return bean;
		}
	};

	protected static Supplier<MenuButton> showUserMenuAction = new SessionScopedBeanProvider<MenuButton>() {
		@Override
		public MenuButton create() throws Exception {
			final MenuButton bean = publish(new MenuButton());
			bean.setMenu(userMenu.get());
			bean.setName("");
			bean.setIcon(CustomizationResources.INSTANCE.user());
			bean.setHoverIcon(CustomizationResources.INSTANCE.user());
			bean.setIconProvider(userImageProvider.get());
			bean.setTooltipProvider(Providers.userFullNameProvider.get());
			return bean;
		}
	};

	private static Supplier<UserImageProvider> userImageProvider = new SessionScopedBeanProvider<UserImageProvider>() {
		@Override
		public UserImageProvider create() throws Exception {
			final UserImageProvider bean = publish(new UserImageProvider());
			bean.setServicesUrl(Runtime.tribefireServicesUrl.get());
			return bean;
		}
	};

	private static Supplier<SettingsMenu> userMenu = new SessionScopedBeanProvider<SettingsMenu>() {
		@Override
		public SettingsMenu create() throws Exception {
			SettingsMenu bean = publish(new SettingsMenu());
			bean.setMenuActions(Actions.userMenuActions.get());
			return bean;
		}
	};

	protected static Supplier<UserNameTextField> userNameTextField = new SingletonBeanProvider<UserNameTextField>() {
		@Override
		public UserNameTextField create() throws Exception {
			UserNameTextField bean = publish(new UserNameTextField());
			bean.getCell().getInputElement(bean.getElement()).setAttribute("placeholder", "username");
			bean.setName("username");
			return bean;
		}
	};

	protected static Supplier<PasswordTextField> passwordTextField = new SingletonBeanProvider<PasswordTextField>() {
		@Override
		public PasswordTextField create() throws Exception {
			PasswordTextField bean = publish(new PasswordTextField());
			bean.getCell().getInputElement(bean.getElement()).setAttribute("placeholder", "password");
			return bean;
		}
	};

	protected static Supplier<Label> errorMessageLabel = new SingletonBeanProvider<Label>() {
		@Override
		public Label create() throws Exception {
			Label bean = publish(new Label());
			bean.addStyleName("errorMessage");
			return bean;
		}
	};

	/**
	 * A DateField used for date editors in the FormPropertyPanel.
	 */
	private static Supplier<DateTimeField> gxtDateField = new PrototypeBeanProvider<DateTimeField>() {
		@Override
		public DateTimeField create() throws Exception {
			DateTimeField bean = new DateTimeField();
			bean.setPropertyEditor(dateTimePropertyEditor.get());
			return bean;
		}
	};

	/**
	 * Contains the format to be used by the date field in {@link #gxtDateField}.
	 */
	private static Supplier<BtDateTimePropertyEditor> dateTimePropertyEditor = new SingletonBeanProvider<BtDateTimePropertyEditor>() {
		@Override
		public BtDateTimePropertyEditor create() throws Exception {
			BtDateTimePropertyEditor bean = new BtDateTimePropertyEditor(DateTimeFormat.getFormat(LocaleUtil.getDateTimeFormat()));
			return bean;
		}
	};

	private static Supplier<LocalizedStringField> localizedStringField = new PrototypeBeanProvider<LocalizedStringField>() {
		@Override
		public LocalizedStringField create() throws Exception {
			LocalizedStringField bean = new LocalizedStringField();
			bean.setLocalizedValuesDialog(localizedStringDialog);
			bean.setInternalField(new ExtendedStringField());
			bean.setGmSession(Session.persistenceSession.get());
			return bean;
		}
	};

	protected static Supplier<LocalizedValuesDialog> localizedStringDialog = new SingletonBeanProvider<LocalizedValuesDialog>() {
		@Override
		public LocalizedValuesDialog create() throws Exception {
			LocalizedValuesDialog bean = publish(new GIMALocalizedValuesDialog());
			return bean;
		}
	};
	
	protected static Supplier<ExtendedStringDialog> extendedStringDialog = new PrototypeBeanProvider<ExtendedStringDialog>() {
		@Override
		public ExtendedStringDialog create() throws Exception {
			ExtendedStringDialog bean = new GIMAExtendedStringFieldDialog();
			return bean;
		}
	};
	
	private static Supplier<HtmlEditorDialog> htmlEditorDialog = new PrototypeBeanProvider<HtmlEditorDialog>() {
		@Override
		public HtmlEditorDialog create() throws Exception {
			HtmlEditorDialog bean = new HtmlEditorDialog();
			return bean;
		}
	};

	private static Supplier<GmScriptEditor> gmScriptEditor = new PrototypeBeanProvider<GmScriptEditor>() {
		@Override
		public GmScriptEditor create() throws Exception {
			GmScriptEditor bean = new GmScriptEditor();
			bean.setScriptEditorDialog(gmScriptEditorDialog.get());
			return bean;
		}
	};

	protected static Supplier<GmScriptEditorDialog> gmScriptEditorDialog = new PrototypeBeanProvider<GmScriptEditorDialog>() {
		@Override
		public GmScriptEditorDialog create() throws Exception {
			GmScriptEditorDialog bean = new GIMAScriptEditorDialog();
			return bean;
		}
	};
		
	protected static Supplier<GmContentView> gmScriptEditorView = new PrototypeBeanProvider<GmContentView>() {
		@Override
		public GmContentView create() throws Exception {
			GmScriptEditorView bean = new GmScriptEditorView();
			bean.configureGmSession(Session.persistenceSession.get());
			bean.setActionManager(Controllers.actionManager.get());
			return bean;
		}
	};

	private static Supplier<TimeSpanField> timeField = new SingletonBeanProvider<TimeSpanField>() {
		@Override
		public TimeSpanField create() throws Exception {
			TimeSpanField bean = new TimeSpanField();
			bean.setGmSession(Session.persistenceSession.get());
			return bean;
		}
	};	

	private static Supplier<KeyConfigurationField> keyConfigurationField = new PrototypeBeanProvider<KeyConfigurationField>() {
		@Override
		public KeyConfigurationField create() throws Exception {
			KeyConfigurationField bean = new KeyConfigurationField();
			bean.setGmSession(Session.persistenceSession.get());
			return bean;
		}
	};
	
	private static Supplier<ColorField> colorField = new PrototypeBeanProvider<ColorField>() {
		@Override
		public ColorField create() throws Exception {
			ColorField bean = new ColorField();
			bean.setGmSession(Session.persistenceSession.get());
			return bean;
		}
	};

	protected static Supplier<ColorPickerWindow> colorDialog = new SingletonBeanProvider<ColorPickerWindow>() {
		@Override
		public ColorPickerWindow create() throws Exception {
			ColorPickerWindow bean = publish(new ColorPickerWindow());
			return bean;
		}
	};
	
	protected static Supplier<FontPicker> fontDialog = new SingletonBeanProvider<FontPicker>() {
		@Override
		public FontPicker create() throws Exception {
			FontPicker bean = publish(new FontPicker());
			return bean;
		}
	};	

	private static Supplier<SimplifiedEntityField> simplifiedEntityFieldProvider = new PrototypeBeanProvider<SimplifiedEntityField>() {
		@Override
		public SimplifiedEntityField create() throws Exception {
			SimplifiedEntityField bean = new SimplifiedEntityField(Codecs.simplifiedEntityRendererCodec.get());
			//bean.setSelectionConstellationDialogProvider(Panels.selectionConstellationDialogProvider);
			bean.setSelectionConstellationDialogProvider(Panels.gimaSelectionConstellationSupplier);
			bean.setQuickAccessPanel(Panels.entityCollectionSpotlightPanel);
			return bean;
		}
	};

	private static Supplier<SimplifiedEntityField> saveQueryDialogWorkbenchSimplifiedEntityFieldProvider = new PrototypeBeanProvider<SimplifiedEntityField>() {
		@Override
		public SimplifiedEntityField create() throws Exception {
			SimplifiedEntityField bean = new SimplifiedEntityField(Codecs.simplifiedEntityRendererCodec.get());
			bean.setSelectionConstellationDialogProvider(Panels.saveQueryGimaSelectionConstellationSupplier);
			//bean.setSelectionConstellationDialogProvider(Panels.saveQueryDialogWorkbenchSelectionConstellationDialogProvider);
			return bean;
		}
	};

	protected static Supplier<GMEditorSupport> gmEditorSupport = new SessionScopedBeanProvider<GMEditorSupport>() {
		@Override
		public GMEditorSupport create() throws Exception {
			GMEditorSupport bean = publish(new GMEditorSupport());
			GMEditorSupport.setAdditionalFieldsProviders(additionalFieldsProviders.get());
			bean.setDynamicEntriesLoader(Providers.dynamicEntriesLoader.get());
			bean.setValueSelectionFutureSupplier(Panels.gimaSelectionConstellationSupplier);
			bean.setExtendedStringFieldDialogProvider(extendedStringDialog);
			bean.setHtmlEditorDialog(htmlEditorDialog);
			return bean;
		}
	};

	private static Supplier<SimplifiedEntityFieldsProvider> additionalFieldsProviders = new SingletonBeanProvider<SimplifiedEntityFieldsProvider>() {
		@Override
		public SimplifiedEntityFieldsProvider create() throws Exception {
			SimplifiedEntityFieldsProvider bean = publish(new SimplifiedEntityFieldsProvider());
			bean.setFieldsProvidersMap(gmEditorsMap.get());
			bean.setSimplifiedEntityFieldProvider(simplifiedEntityFieldProvider);
			bean.setQuickAccessTriggerFieldSupplier(entityCollectionField);
			return bean;
		}
	};
	
	private static Supplier<QuickAccessTriggerField> entityCollectionField = new PrototypeBeanProvider<QuickAccessTriggerField>() {
		@Override
		public QuickAccessTriggerField create() throws Exception {
			QuickAccessTriggerField bean = new QuickAccessTriggerField();
			bean.setQuickAccessPanel(Panels.entityCollectionSpotlightPanel);
			bean.setUseNavigationButtons(false);
			bean.setEmptyText(LocalizedText.INSTANCE.typeToShowValues());
			bean.setUseAsModal(false);
			bean.setAddingToCollection(true);
			return bean;
		}
	};

	private static Supplier<Map<Class<?>, Supplier<? extends Field<?>>>> gmEditorsMap = new PrototypeBeanProvider<Map<Class<?>, Supplier<? extends Field<?>>>>() {
		@Override
		public Map<Class<?>, Supplier<? extends Field<?>>> create() throws Exception {
			Map<Class<?>, Supplier<? extends Field<?>>> bean = new HashMap<>();
			bean.put(Date.class, gxtDateField);
			bean.put(LocalizedString.class, localizedStringField);
			bean.put(Color.class, colorField);
			bean.put(Script.class, gmScriptEditor);
			bean.put(TimeSpan.class, timeField);
			bean.put(KeyConfiguration.class, keyConfigurationField);
			return bean;
		}
	};

	protected static Supplier<Label> globaleStateLabel = new SessionScopedBeanProvider<Label>() {
		@Override
		public Label create() throws Exception {
			Label bean = publish(new Label());
			bean.setWidth("350px");

			// initialize the "global state" functionality
			GlobalState.setNotificationFactory(Notifications.notificationFactory);
			GlobalState.setDetailsText(LocalizedText.INSTANCE.details());
			GlobalState.setMaskComponent(Constellations.explorerConstellationProvider.get());
			// initialize some singletons
			Notifications.notificationBar.get();
			Notifications.notificationsConstellationProvider.get();

			return bean;
		}
	};
	
	protected static Supplier<Label> webReaderGlobalStateLabel = new SessionScopedBeanProvider<Label>() {
		@Override
		public Label create() throws Exception {
			Label bean = publish(new Label());
			bean.setWidth("350px");

			// initialize the "global state" functionality
			GlobalState.setNotificationFactory(Notifications.notificationFactory);
			GlobalState.setDetailsText(LocalizedText.INSTANCE.details());
			// initialize some singletons
			Notifications.webReaderNotificationBar.get();

			return bean;
		}
	};

	protected static Supplier<QueryModelEditorPanel> queryModelEditorPanelProvider = new PrototypeBeanProvider<QueryModelEditorPanel>() {
		@Override
		public QueryModelEditorPanel create() throws Exception {
			QueryModelEditorPanel bean = new QueryModelEditorPanel();
			bean.setWidth("100%");
			bean.setHeaderVisible(false);
			bean.setBorders(false);
			bean.setBodyBorder(false);
			bean.setPageSize(30);
			bean.setUsePaging(false);
			bean.setQueryFormDialog(queryFormDialog.get());
			bean.setOtherModeQueryProviderView(UiElements.queryModelEditorAdvancedPanelProvider);
			return bean;
		}
	};
	
	protected static Supplier<QueryModelEditorAdvancedPanel> queryModelEditorAdvancedPanelProvider = new PrototypeBeanProvider<QueryModelEditorAdvancedPanel>() {
		@Override
		public QueryModelEditorAdvancedPanel create() throws Exception {
			QueryModelEditorAdvancedPanel bean = new QueryModelEditorAdvancedPanel();
			bean.setWidth("100%");
			bean.setHeaderVisible(false);
			bean.setBorders(false);
			bean.setBodyBorder(false);
			bean.setPageSize(30);
			bean.setUsePaging(false);
			bean.setStorage(wbStorageProvider.get());
			bean.setQueryFormDialog(queryFormDialog.get());
			bean.setAutoCompletionPanelProvider(autoCompletionPanelProvider);
			bean.setQuickAccessPanelProvider(Panels.querySpotlightPanelProvider);
			bean.setIconProvider(Providers.typeIconProvider.get());
			return bean;
		}
	};

	private static Supplier<AutoCompletionPanel> autoCompletionPanelProvider = new PrototypeBeanProvider<AutoCompletionPanel>() {
		@Override
		public AutoCompletionPanel create() throws Exception {
			AutoCompletionPanel bean = new AutoCompletionPanel();
			return bean;
		}
	};

	private static Supplier<WbStorage> wbStorageProvider = new PrototypeBeanProvider<WbStorage>() {
		@Override
		public WbStorage create() throws Exception {
			WbStorage bean = new WbStorage();
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			bean.setSettingQueryDialogProvider(settingQueryDialogProvider);
			bean.setSaveQueryDialogProvider(saveQueryDialogProvider);
			return bean;
		}
	};

	private static Supplier<List<SimplifiedEntityFieldTabConfiguration>> parentFolderEntityQueryTabsProvider = new PrototypeBeanProvider<List<SimplifiedEntityFieldTabConfiguration>>() {
		@Override
		public List<SimplifiedEntityFieldTabConfiguration> create() throws Exception {
			List<SimplifiedEntityFieldTabConfiguration> bean = new ArrayList<>();
			bean.add(new SimplifiedEntityFieldTabConfiguration(EntityQueryBuilder.from(Folder.class).where().property("name").eq("root").done(), "Root"));
			bean.add(new SimplifiedEntityFieldTabConfiguration(EntityQueryBuilder.from(Folder.class).where().property("name").eq("actionbar").done(), "ActionBar"));
			bean.add(new SimplifiedEntityFieldTabConfiguration(EntityQueryBuilder.from(Folder.class).where().property("name").eq("headerbar").done(), "HeaderBar"));
			bean.add(new SimplifiedEntityFieldTabConfiguration(EntityQueryBuilder.from(Folder.class).where().value("homeFolder").in().property("tags").done(), "HomeScreen"));
			return bean;
		}
	};

	private static Supplier<WbSaveQueryDialog> saveQueryDialogProvider = new PrototypeBeanProvider<WbSaveQueryDialog>() {
		@Override
		public WbSaveQueryDialog create() throws Exception {
			WbSaveQueryDialog bean = new WbSaveQueryDialog();
			bean.setUseCase(Runtime.selectionUseCaseProvider.get());
			bean.setParentFolderField(saveQueryDialogWorkbenchSimplifiedEntityFieldProvider.get());
			bean.setParentFolderEntityQueryTabs(parentFolderEntityQueryTabsProvider.get());
			bean.setLocalizedStringFieldSupplier(localizedStringField);
			return bean;
		}
	};

	private static Supplier<WbSettingQueryDialog> settingQueryDialogProvider = new PrototypeBeanProvider<WbSettingQueryDialog>() {
		@Override
		public WbSettingQueryDialog create() throws Exception {
			WbSettingQueryDialog bean = new WbSettingQueryDialog();
			bean.setGIMADialogProvider(workbenchGimaDialogProvider);
			bean.setUseCase(Runtime.selectionUseCaseProvider.get());
			bean.setIconField(saveQueryDialogWorkbenchSimplifiedEntityFieldProvider.get());
			bean.setParentFolderField(saveQueryDialogWorkbenchSimplifiedEntityFieldProvider.get());
			bean.setContextField(saveQueryDialogWorkbenchSimplifiedEntityFieldProvider.get());
			bean.setIconEntityQuery(EntityQueryBuilder.from(Icon.class).done());
			bean.setContextEntityQuery(EntityQueryBuilder.from(TraversingCriterion.class).done());
			bean.setParentFolderEntityQueryTabs(parentFolderEntityQueryTabsProvider.get());
			bean.setLocalizedStringFieldSupplier(localizedStringField);
			return bean;
		}
	};
	
	protected static Supplier<GIMATypeSelectionView> typeSelectionViewSupplier = new PrototypeBeanProvider<GIMATypeSelectionView>() {
		@Override
		public GIMATypeSelectionView create() throws Exception {
			GIMATypeSelectionView bean = new GIMATypeSelectionView();
			bean.setSpotlightPanel(Panels.spotlightPanelNewInstanceProvider.get());
			return bean;
		}
	};
	
	private static Supplier<GIMAActionSelectionView> actionSelectionViewSupplier = new PrototypeBeanProvider<GIMAActionSelectionView>() {
		@Override
		public GIMAActionSelectionView create() throws Exception {
			GIMAActionSelectionView bean = new GIMAActionSelectionView();
			bean.setSpotlightPanel(Panels.spotlightPanelNewInstanceProvider.get());
			return bean;
		}
	};

	protected static Supplier<GIMADialog> gimaDialogProvider = new PrototypeBeanProvider<GIMADialog>() {
		@Override
		public GIMADialog create() throws Exception {
			GIMADialog bean = new GIMADialog();
			bean.setTetherBar(Panels.unclickableTetherBarProvider.get());
			bean.setPropertyPanelProvider(Panels.gimaPropertyPanelProvider);
			bean.setMasterDetailConstellationProvider(Constellations.gimaMasterDetailConstellationProvider);
			bean.setGmSession(Session.persistenceSession.get());
			bean.setUseCase(Runtime.gimaUseCaseProvider.get());
			bean.setTypeSelectionViewSupplier(typeSelectionViewSupplier);
			bean.setActionSelectionViewSupplier(actionSelectionViewSupplier);
			bean.setInstantiationActionHandler(Constellations.explorerConstellationProvider.get());
			bean.setValidation(Panels.validation);
			return bean;
		}
	};
	
	protected static Supplier<GIMADialog> transientGimaDialogProvider = new PrototypeBeanProvider<GIMADialog>() {
		@Override
		public GIMADialog create() throws Exception {
			GIMADialog bean = new GIMADialog();
			bean.setTetherBar(Panels.unclickableTetherBarProvider.get());
			bean.setPropertyPanelProvider(Panels.gimaPropertyPanelProvider);
			bean.setMasterDetailConstellationProvider(Constellations.gimaMasterDetailConstellationProvider);
			bean.setGmSession(Session.transientManagedSession.get());
			bean.setUseCase(Runtime.gimaUseCaseProvider.get());
			bean.setTypeSelectionViewSupplier(typeSelectionViewSupplier);
			bean.setInstantiationActionHandler(Constellations.explorerConstellationProvider.get());
			bean.setValidation(Panels.transientValidation);
			return bean;
		}
	};
	
	private static Supplier<GIMADialog> workbenchGimaDialogProvider = new PrototypeBeanProvider<GIMADialog>() {
		@Override
		public GIMADialog create() throws Exception {
			GIMADialog bean = new GIMADialog();
			bean.setTetherBar(Panels.unclickableTetherBarProvider.get());
			bean.setPropertyPanelProvider(Panels.workbenchGimaPropertyPanelProvider);
			bean.setMasterDetailConstellationProvider(Constellations.gimaMasterDetailConstellationProvider);
			bean.setGmSession(Session.workbenchPersistenceSession.get());
			bean.setUseCase(Runtime.gimaUseCaseProvider.get());
			bean.setTypeSelectionViewSupplier(typeSelectionViewSupplier);
			bean.setValidation(Panels.workbenchValidation);
			return bean;
		}
	};

	protected static Supplier<GIMADialog> userGimaDialogProvider = new PrototypeBeanProvider<GIMADialog>() {
		@Override
		public GIMADialog create() throws Exception {
			GIMADialog bean = new GIMADialog();
			bean.setTetherBar(Panels.unclickableTetherBarProvider.get());
			bean.setPropertyPanelProvider(Panels.gimaPropertyPanelProvider);
			bean.setMasterDetailConstellationProvider(Constellations.gimaMasterDetailConstellationProvider);
			bean.setGmSession(Session.userSession.get());
			bean.setUseCase(Runtime.gimaUseCaseProvider.get());
			bean.setValidation(Panels.userValidation);
			return bean;
		}
	};

	protected static Supplier<TemplateGIMADialog> templateGimaDialogProvider = new PrototypeBeanProvider<TemplateGIMADialog>() {
		@Override
		public TemplateGIMADialog create() throws Exception {
			TemplateGIMADialog bean = new TemplateGIMADialog();
			bean.setTetherBar(Panels.unclickableTetherBarProvider.get());
			bean.setPropertyPanelProvider(Panels.templateGimaPropertyPanelProvider);
			bean.setMasterDetailConstellationProvider(Constellations.gimaMasterDetailConstellationProvider);
			bean.setGmSession(Session.persistenceSession.get());
			bean.setWorkbenchSession(Session.templateWorkbenchPersistenceSession.get());
			//bean.setSelectionFutureSupplier(Panels.selectionConstellationDialogProvider);
			bean.setSelectionFutureSupplier(Panels.gimaSelectionConstellationSupplier);
			bean.setSingleTypesToShowGIMA(Panels.singleTypesToShowGIMA.get());
			bean.setUseCase(Runtime.templateEditorUseCaseProvider.get());
			bean.setTypeSelectionViewSupplier(typeSelectionViewSupplier);
			bean.setInstantiationActionHandler(Constellations.explorerConstellationProvider.get());
			bean.setValidation(Panels.templateGimaValidation);
			return bean;
		}
	};	
	
	private static Supplier<QueryFormDialog> queryFormDialog = new PrototypeBeanProvider<QueryFormDialog>() {
		@Override
		public QueryFormDialog create() throws Exception {
			QueryFormDialog bean = new QueryFormDialog();
			bean.setPropertyPanel(Panels.queryFormPropertyPanelProvider);
			bean.setQueryFormTemplate(queryFormTemplate.get());
			return bean;
		}
	};

	private static Supplier<QueryFormTemplate> queryFormTemplate = new PrototypeBeanProvider<QueryFormTemplate>() {
		@Override
		public QueryFormTemplate create() throws Exception {
			QueryFormTemplate bean = new QueryFormTemplate();
			bean.setWorkbenchPersistenceSession(Session.templateWorkbenchPersistenceSession.get());
			bean.setPersistenceSession(Session.persistenceSession.get());
			bean.setUserNameProvider(Providers.userNameProvider.get());
			return bean;
		}
	};

	protected static Supplier<AccessChoiceDialog> accessChoiceDialog = new PrototypeBeanProvider<AccessChoiceDialog>() {
		@Override
		public AccessChoiceDialog create() throws Exception {
			AccessChoiceDialog bean = new AccessChoiceDialog();
			bean.setAvailableAccessesProvider(Providers.availableAccessesDataProvider.get());
			return bean;
		}
	};

	protected static Supplier<DefaultActionMenuBuilder> defaultActionMenuBuilder = new SingletonBeanProvider<DefaultActionMenuBuilder>() {
		@Override
		public DefaultActionMenuBuilder create() throws Exception {
			DefaultActionMenuBuilder bean = new DefaultActionMenuBuilder();
			bean.setWorkbenchActionHandlerRegistry(Actions.workbenchActionHandlerRegistry.get());
			bean.setSeparatorActionProvider(Actions.separatorAction);
			bean.setActionFolderContentExpert(Actions.actionFolderContentExpert.get());
			return bean;
		}
	};
	
	protected static Supplier<DefaultActionMenuBuilder> gimaActionMenuBuilder = new SingletonBeanProvider<DefaultActionMenuBuilder>() {
		@Override
		public DefaultActionMenuBuilder create() throws Exception {
			DefaultActionMenuBuilder bean = new DefaultActionMenuBuilder();
			bean.setWorkbenchActionHandlerRegistry(Actions.workbenchActionHandlerRegistry.get());
			bean.setSeparatorActionProvider(Actions.separatorAction);
			bean.setActionFolderContentExpert(Actions.actionFolderContentExpert.get());
			return bean;
		}
	};
	
	protected static Supplier<DefaultActionMenuBuilder> modelerActionMenuBuilder = new SingletonBeanProvider<DefaultActionMenuBuilder>() {
		@Override
		public DefaultActionMenuBuilder create() throws Exception {
			DefaultActionMenuBuilder bean = new DefaultActionMenuBuilder();
			bean.setActionFolderContentExpert(Actions.actionFolderContentExpert.get());
			//bean.setSeparatorActionProvider(Actions.separatorAction);
			return bean;
		}
	};

	protected static Supplier<DefaultHeaderBar> defaulHeaderBar = new SingletonBeanProvider<DefaultHeaderBar>() {
		@Override
		public DefaultHeaderBar create() throws Exception {
			DefaultHeaderBar bean = new DefaultHeaderBar();
			bean.setQuickAccessTriggerFieldSupplier(Panels.topBannerQuickAccessTriggerField);
			bean.setGlobalStateSlot(globaleStateLabel.get());
			//bean.setNotificationSlot(Notifications.notificationIcon.get());
			bean.setSettingsMenu(settingsMenu.get());
			bean.setUserMenu(userMenu.get());
			bean.setDefaultSettingsMenuIcon(ConstellationResources.INSTANCE.settings32());
			bean.setDefaultUserMenuIcon(CustomizationResources.INSTANCE.user());
			bean.setUserIconProvider(userImageProvider.get());
			bean.setUserTooltipProvider(Providers.userFullNameProvider.get());
			bean.setHyperlinkActions(IocExtensionPoints.INSTANCE.topBannerLinks().get());
			bean.setUserImageServletUrl(Runtime.userImageServletUrlProvider.get());
			bean.setWorkbenchActionHandlerRegistry(Actions.workbenchActionHandlerRegistry.get());
			bean.setGlobalSearchPanelSupplier(Panels.globalSearchPanel);
			bean.setGlobalActionsHandler(Constellations.abstractGlobalActionsHandler.get());
			bean.setActionFolderContentExpert(Actions.actionFolderContentExpert.get());
			bean.setTestHeaderbarAction(Actions.testRveAction.get());  //RVE - test action
			return bean;
		}
	};

	protected static Supplier<ResourceExpertUI> resourceExpertUIProvider = new PrototypeBeanProvider<ResourceExpertUI>() {
		@Override
		public ResourceExpertUI create() throws Exception {
			ResourceExpertUI bean = new ResourceExpertUI();
			bean.setResourceUploadView(Panels.noRefreshResourceUploadPanelProvider.get());
			return bean;
		}
	};
	
	protected static Supplier<ResourceExpertUI> workbenchResourceExpertUIProvider = new PrototypeBeanProvider<ResourceExpertUI>() {
		@Override
		public ResourceExpertUI create() throws Exception {
			ResourceExpertUI bean = new ResourceExpertUI();
			bean.setResourceUploadView(Panels.noRefreshWorkbenchResourceUploadPanelProvider.get());
			return bean;
		}
	};
	
	private static Supplier<AdvancedSaveActionDialog> abstractAdvancedSaveActionDialogSupplier = new PrototypeBeanProvider<AdvancedSaveActionDialog>() {
		{
			setAbstract(true);
		}
		@Override
		public AdvancedSaveActionDialog create() throws Exception {
			AdvancedSaveActionDialog bean = new AdvancedSaveActionDialog();
			bean.setModelPathNavigationListener(Constellations.explorerConstellationProvider.get());
			bean.setDataSession(Session.persistenceSession.get());
			bean.setTransientSession(Session.transientManagedSession.get());
			bean.setTransientSessionSupplier(Session.prototypeTransientManagedSession);
			bean.setNotificationFactory(Notifications.notificationFactory);
			return bean;
		}
	};
	
	protected static Supplier<AdvancedSaveActionDialog> advancedSaveActionDialogSupplier = new PrototypeBeanProvider<AdvancedSaveActionDialog>() {
		@Override
		public AdvancedSaveActionDialog create() throws Exception {
			AdvancedSaveActionDialog bean = abstractAdvancedSaveActionDialogSupplier.get();
			bean.setSaveAction(Actions.saveAction.get());
			return bean;
		}
	};
	
	protected static Supplier<AdvancedSaveActionDialog> assetManagementDialogSupplier = new PrototypeBeanProvider<AdvancedSaveActionDialog>() {
		@Override
		public AdvancedSaveActionDialog create() throws Exception {
			AdvancedSaveActionDialog bean = abstractAdvancedSaveActionDialogSupplier.get();
			bean.setDisableNoneOption(true);
			bean.configureAdvancedSetupSupport(Runtime.platformSetupSupported.get());
			return bean;
		}
	};
	
	/*private static Supplier<GIMATypeSelectionView> workbenchTypeSelectionViewSupplier = new PrototypeBeanProvider<GIMATypeSelectionView>() {
		@Override
		public GIMATypeSelectionView create() throws Exception {
			GIMATypeSelectionView bean = new GIMATypeSelectionView();
			bean.setSpotlightPanel(Panels.workbenchSpotlightPanelNewInstanceProvider.get());
			return bean;
		}
	};*/
}
