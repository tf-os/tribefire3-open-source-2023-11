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
package com.braintribe.gwt.gm.storage.impl.wb.form.setting;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.LocalizedStringField;
import com.braintribe.gwt.gm.storage.impl.wb.WbStorageRuntimeException;
import com.braintribe.gwt.gm.storage.impl.wb.form.setting.controls.CheckBoxControl;
import com.braintribe.gwt.gm.storage.impl.wb.form.setting.controls.GimaEntityFieldListener;
import com.braintribe.gwt.gm.storage.impl.wb.resources.LocalizedText;
import com.braintribe.gwt.gm.storage.impl.wb.resources.WbStorageTemplates;
import com.braintribe.gwt.gm.storage.impl.wb.resources.WbStorageUiResources;
import com.braintribe.gwt.gme.constellation.client.GIMADialog;
import com.braintribe.gwt.gme.propertypanel.client.field.SimplifiedEntityField;
import com.braintribe.gwt.gme.propertypanel.client.field.SimplifiedEntityFieldConfiguration;
import com.braintribe.gwt.gme.propertypanel.client.field.SimplifiedEntityFieldTabConfiguration;
import com.braintribe.gwt.gme.workbench.client.Workbench;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ClosableWindow;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.meta.data.prompt.AutoPagingSize;
import com.braintribe.model.meta.data.prompt.DefaultView;
import com.braintribe.model.meta.selector.KnownUseCase;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.api.transaction.TransactionException;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.template.meta.TemplateMetaData;
import com.braintribe.model.workbench.QueryAction;
import com.braintribe.model.workbench.TemplateBasedAction;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.LabelProviderSafeHtmlRenderer;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.dom.DomQuery;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.StringLabelProvider;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.IntegerPropertyEditor;
import com.sencha.gxt.widget.core.client.form.SimpleComboBox;
import com.sencha.gxt.widget.core.client.form.TriggerField;

/**
 * Dialog used for saving a query.
 *
 *
 */
public class WbSettingQueryDialog extends ClosableWindow implements InitializableBean, Consumer<WbSettingQueryDialogConfig>, ClickHandler, ResizeHandler {

	/********************************** Constants **********************************/

	private final String formDescriptionLabelId = hashCode() + "_formDescriptionLabel";
	private final String folderNameLabelId = hashCode() + "_folderNameLabel";
	private final String folderNameTableCellId = hashCode() + "_folderNameTableCell";
	private final String parentFolderLabelId = hashCode() + "_parentFolderLabel";
	private final String parentFolderTableCellId = hashCode() + "_parentFolderTableCell";
	private final String parentFolderButtonId = hashCode() + "_parentFolderButton";
	private final String iconLabelId = hashCode() + "_iconLabel";
	private final String iconImageId = hashCode() + "_iconImage";
	private final String iconChooseButtonId = hashCode() + "_iconChooseButton";
	private final String iconDeleteButtonId = hashCode() + "_iconDeleteButton";
	private final String contextLabelId = hashCode() + "_contextLabel";
	private final String contextTableCellId = hashCode() + "_contextTableCell";
	private final String contextChooseButtonId = hashCode() + "_contextChooseButton";
	private final String contextDeleteButtonId = hashCode() + "_contextDeleteButton";
	private final String multiSelectionLabelId = hashCode() + "_multiSelectionLabel";
	private final String multiSelectionTableCellId = hashCode() + "_multiSelectionTableCell";
	private final String forceFormLabelId = hashCode() + "_forceFormLabel";
	private final String forceFormTableCellId = hashCode() + "_forceFormTableCell";
	private final String autoPagingSizeLabelId = hashCode() + "_autoPagingSizeLabel";
	private final String autoPagingSizeTableCellId = hashCode() + "_autoPagingSizeTableCell";
	private final String defaultViewLabelId = hashCode() + "_defaultViewLabel";
	private final String defaultViewTableCellId = hashCode() + "_defaultViewTableCell";
	private final String okButtonTableId = hashCode() + "_okButtonTable";
	private final String okButtonImageId = hashCode() + "_okButtonImage";
	private final String okButtonLabelId = hashCode() + "_okButtonLabel";
	private final String cancelButtonTableId = hashCode() + "_cancelButtonTable";
	private final String cancelButtonImageId = hashCode() + "_cancelButtonImage";
	private final String cancelButtonLabelId = hashCode() + "_cancelButtonLabel";

	private final String selectFormDescriptionLabel = "label[@id='" + formDescriptionLabelId + "']";
	private final String selectFolderNameLabel = "label[@id='" + folderNameLabelId + "']";
	private final String selectFolderNameTableCell = "div[@id='" + folderNameTableCellId + "']";
	private final String selectParentFolderLabel = "label[@id='" + parentFolderLabelId + "']";
	private final String selectParentFolderTableCell = "div[@id='" + parentFolderTableCellId + "']";
	private final String selectParentFolderButton = "div[@id='" + parentFolderButtonId + "']";
	private final String selectIconLabel = "label[@id='" + iconLabelId + "']";
	private final String selectIconImage = "img[@id='" + iconImageId + "']";
	private final String selectIconChooseButton = "div[@id='" + iconChooseButtonId + "']";
	private final String selectIconDeleteButton = "img[@id='" + iconDeleteButtonId + "']";
	private final String selectContextLabel = "label[@id='" + contextLabelId + "']";
	private final String selectContextTableCell = "div[@id='" + contextTableCellId + "']";
	private final String selectContextChooseButton = "div[@id='" + contextChooseButtonId + "']";
	private final String selectContextDeleteButton = "img[@id='" + contextDeleteButtonId + "']";
	private final String selectMultiSelectionLabel = "label[@id='" + multiSelectionLabelId + "']";
	private final String selectMultiSelectionTableCell = "div[@id='" + multiSelectionTableCellId + "']";
	private final String selectForceFormLabel = "label[@id='" + forceFormLabelId + "']";
	private final String selectForceFormTableCell = "div[@id='" + forceFormTableCellId + "']";
	private final String selectAutoPagingSizeLabel = "label[@id='" + autoPagingSizeLabelId + "']";
	private final String selectAutoPagingSizeTableCell = "div[@id='" + autoPagingSizeTableCellId + "']";
	private final String selectDefaultViewLabel = "label[@id='" + defaultViewLabelId + "']";
	private final String selectDefaultViewTableCell = "div[@id='" + defaultViewTableCellId + "']";
	private final String selectOkButtonTable = "div[@id='" + okButtonTableId + "']";
	private final String selectOkButtonImage = "img[@id='" + okButtonImageId + "']";
	private final String selectOkButtonLabel = "label[@id='" + okButtonLabelId + "']";
	private final String selectCancelButtonTable = "div[@id='" + cancelButtonTableId + "']";
	private final String selectCancelButtonImage = "img[@id='" + cancelButtonImageId + "']";
	private final String selectCancelButtonLabel = "label[@id='" + cancelButtonLabelId + "']";

	private static final String checkBoxClass = "settingQueryDialogFieldCheckBox";

	/********************************** Variables **********************************/

	private LabelElement formDescriptionLabel = null;
	private LabelElement folderNameLabel = null;
	private DivElement folderNameTableCell = null;
	private LabelElement parentFolderLabel = null;
	private DivElement parentFolderTableCell = null;
	private DivElement parentFolderButton = null;
	private LabelElement iconLabel = null;
	private ImageElement iconImage = null;
	private DivElement iconChooseButton = null;
	private ImageElement iconDeleteButton = null;
	private LabelElement contextLabel = null;
	private DivElement contextTableCell = null;
	private DivElement contextChooseButton = null;
	private ImageElement contextDeleteButton = null;
	private LabelElement multiSelectionLabel = null;
	private DivElement multiSelectionTableCell = null;
	private CheckBoxControl multiSelectionCheckBox = null;
	private LabelElement forceFormLabel;
	private DivElement forceFormTableCell;
	private CheckBoxControl forceFormCheckBox;
	private LabelElement autoPagingSizeLabel;
	private DivElement autoPagingSizeTableCell;
	private NumberField<Integer> autoPagingSizeField;
	private LabelElement defaultViewLabel = null;
	private DivElement defaultViewTableCell = null;
	private SimpleComboBox<String> defaultViewComboBox = null;
	private DivElement okButtonTable = null;
	private ImageElement okButtonImage = null;
	private LabelElement okButtonLabel = null;
	private DivElement cancelButtonTable = null;
	private ImageElement cancelButtonImage = null;
	private LabelElement cancelButtonLabel = null;

	private Future<WbSettingQueryDialogResult> futureDialogResult = null;
	private PersistenceGmSession workbenchSession = null;
	private NestedTransaction nestedTransaction = null;
	private Workbench workbench = null;

	private LocalizedStringField folderNameField = null;
	private LocalizedString originalLocalizedString = null;

	private SimplifiedEntityField parentFolderField = null;
	private SimplifiedEntityField iconField = null;
	private SimplifiedEntityField contextField = null;

	private EntityQuery iconEntityQuery = null;
	private EntityQuery contextEntityQuery = null;
	private List<SimplifiedEntityFieldTabConfiguration> simplifiedEntityQueryTabs = null;

	private Supplier<GIMADialog> gimaDialogProvider;
	private String useCase = null;
	private boolean handleResult = false;
	private Supplier<? extends LocalizedStringField> localizedStringFieldSupplier;

	/********************************** WbSaveQueryDialog **********************************/

	/**
	 * Configures the required field used for choosing the parent folder.
	 */
	@Required
	public void setParentFolderField(final SimplifiedEntityField simplifiedEntityField) {
		parentFolderField = simplifiedEntityField;
		addWidgetToRootPanel(parentFolderField);

		parentFolderField.setDisableClickAction(true);
		parentFolderField.setValidateOnBlur(false);
		parentFolderField.setAutoValidate(false);
		parentFolderField.setHideTrigger(true);
		parentFolderField.setAllowBlank(false);
	}

	/**
	 * Configures a query to be used by the {@link SimplifiedEntityField} configured via {@link #setParentFolderField(SimplifiedEntityField)}. Please notice that it MUST be a query for the {@link Folder}
	 * type.
	 */
	@Required
	public void setParentFolderEntityQueryTabs(final List<SimplifiedEntityFieldTabConfiguration> simplifiedEntityQueryTabs) {
		this.simplifiedEntityQueryTabs = simplifiedEntityQueryTabs;
	}

	/**
	 * Configures the required field used for choosing the icon.
	 */
	@Required
	public void setIconField(final SimplifiedEntityField simplifiedEntityField) {
		iconField = simplifiedEntityField;
		addWidgetToRootPanel(this.iconField);

		iconField.setDisableClickAction(true);
		iconField.setValidateOnBlur(false);
		iconField.setAutoValidate(false);
		iconField.setHideTrigger(true);
		iconField.setAllowBlank(false);
		iconField.setVisible(false);

		iconField.addListener(new GimaEntityFieldListener(this.gimaDialogProvider) {
			@Override
			public void onValueChanged(final GenericEntity entity, final boolean isNewInstance) {
				if (entity == null) {// Delete the image of the iconImage
					iconImage.setSrc(WbStorageUiResources.INSTANCE.blank().getSafeUri().asString());
					return;
				}
				
				if (!(entity instanceof Icon))
					return;
				
				final Icon icon = (Icon) entity;
				if (!isNewInstance)
					setIconImage(icon);
				else {
					displayGIMA(workbenchSession, icon, AsyncCallbacks.of(result -> {
						if (!result) // Clear the value because the GIMA was canceled
							WbSettingQueryDialog.this.iconField.setValue(null);
						else
							setIconImage(icon);
					}, e -> {
						throw new WbStorageRuntimeException(e.getMessage(), e);
					}));
				}
			}

			private void setIconImage(final Icon icon) {
				// Try to get a nearly 16px image for the iconImage
				Resource image = GMEIconUtil.getImageFromIcon(icon, 14, 18);
				if (image == null) // No 16px image found, get any image for the iconImage
					image = GMEIconUtil.getImageFromIcon(icon, 0, Integer.MAX_VALUE);

				if (image != null) {
					// Set the image of the iconImage
					String imageSrc = workbenchSession.resources().url(image).asString();
					iconImage.setSrc(imageSrc);
				}
			}
		});
	}

	/**
	 * Configures a query to be used by the {@link SimplifiedEntityField} configured via
	 * {@link #setIconField(SimplifiedEntityField)}. Please notice that it MUST be a query for the {@link Icon} type.
	 */
	@Required
	public void setIconEntityQuery(final EntityQuery simplifiedEntityQuery) {
		this.iconEntityQuery = simplifiedEntityQuery;
	}

	/**
	 * Configures the required field used for choosing the context.
	 */
	@Required
	public void setContextField(final SimplifiedEntityField simplifiedEntityField) {
		contextField = simplifiedEntityField;
		addWidgetToRootPanel(contextField);

		contextField.setDisableClickAction(true);
		contextField.setValidateOnBlur(false);
		contextField.setAutoValidate(false);
		contextField.setHideTrigger(true);
		contextField.setAllowBlank(true);

		contextField.addListener(new GimaEntityFieldListener(gimaDialogProvider) {
			@Override
			public void onValueChanged(final GenericEntity entity, final boolean isNewInstance) {
				if (!(entity instanceof TraversingCriterion) || !isNewInstance)
					return;
				
				displayGIMA(WbSettingQueryDialog.this.workbenchSession, entity, AsyncCallbacks.of(result -> {
					if (!result) // Clear the value because the GIMA was canceled
						contextField.setValue(null);
				}, e -> {
					throw new WbStorageRuntimeException(e.getMessage(), e);
				}));
			}
		});
	}

	/**
	 * Configures a query to be used by the {@link SimplifiedEntityField} configured via {@link #setContextField(SimplifiedEntityField)}. Please notice that it MUST be a query for the
	 * {@link TraversingCriterion} type.
	 */
	@Required
	public void setContextEntityQuery(final EntityQuery simplifiedEntityQuery) {
		this.contextEntityQuery = simplifiedEntityQuery;
	}
	
	/**
	 * Configures the supplier for the {@link LocalizedStringField}.
	 */
	@Required
	public void setLocalizedStringFieldSupplier(Supplier<? extends LocalizedStringField> localizedStringFieldSupplier) {
		this.localizedStringFieldSupplier = localizedStringFieldSupplier;
	}

	/**
	 * Configures the required useCase.
	 */
	@Configurable
	public void setUseCase(final String useCase) {
		this.useCase = useCase;
	}

	/**
	 * Configures the required provider for {@link GIMADialog}.
	 */
	@Required
	public void setGIMADialogProvider(final Supplier<GIMADialog> gimaDialogProvider) {
		this.gimaDialogProvider = gimaDialogProvider;
	}

	public WbSettingQueryDialog() {
		setHeaderVisible(false);
		setBodyBorder(false);
		setBorders(false);

		setResizable(false);
		setClosable(false);
		setOnEsc(false);
		setModal(true);

		setSize("400px", "320px");
		setBodyStyle("backgroundColor:white");

		// Needed to enable events
		addWidgetToRootPanel(this);
	}

	/****************************** InitializableBean ******************************/

	@Override
	public void intializeBean() {
		// Get and initialize template Elements
		add(new HTML(WbStorageTemplates.INSTANCE.wbStorageSettingDialog(formDescriptionLabelId, folderNameLabelId, folderNameTableCellId,
				parentFolderLabelId, parentFolderTableCellId, parentFolderButtonId, iconLabelId, iconImageId, iconChooseButtonId, iconDeleteButtonId,
				contextLabelId, contextTableCellId, contextChooseButtonId, contextDeleteButtonId, multiSelectionLabelId, multiSelectionTableCellId,
				forceFormLabelId, forceFormTableCellId, autoPagingSizeLabelId, autoPagingSizeTableCellId,
				defaultViewLabelId, defaultViewTableCellId, okButtonTableId, okButtonImageId, okButtonLabelId, cancelButtonTableId,
				cancelButtonImageId, cancelButtonLabelId)));

		formDescriptionLabel = DomQuery.selectNode(selectFormDescriptionLabel, getElement()).cast();
		formDescriptionLabel.setInnerText(LocalizedText.INSTANCE.editFolder());

		folderNameLabel = DomQuery.selectNode(selectFolderNameLabel, getElement()).cast();
		folderNameLabel.setInnerText(LocalizedText.INSTANCE.folderName());

		folderNameTableCell = DomQuery.selectNode(selectFolderNameTableCell, getElement()).cast();
		folderNameTableCell.appendChild(createFolderNameField().getElement());

		parentFolderLabel = DomQuery.selectNode(selectParentFolderLabel, getElement()).cast();
		parentFolderLabel.setInnerText(LocalizedText.INSTANCE.parentFolder() + ":");

		parentFolderButton = DomQuery.selectNode(selectParentFolderButton, getElement()).cast();
		parentFolderTableCell = DomQuery.selectNode(selectParentFolderTableCell, getElement()).cast();
		parentFolderTableCell.appendChild(parentFolderField.getElement());

		iconLabel = DomQuery.selectNode(selectIconLabel, getElement()).cast();
		iconLabel.setInnerText(LocalizedText.INSTANCE.icon() + ":");

		iconImage = DomQuery.selectNode(selectIconImage, getElement()).cast();
		iconImage.setSrc(WbStorageUiResources.INSTANCE.blank().getSafeUri().asString());

		iconChooseButton = DomQuery.selectNode(selectIconChooseButton, getElement()).cast();
		iconDeleteButton = DomQuery.selectNode(selectIconDeleteButton, getElement()).cast();
		iconDeleteButton.setSrc(WbStorageUiResources.INSTANCE.delete().getSafeUri().asString());

		contextLabel = DomQuery.selectNode(selectContextLabel, getElement()).cast();
		contextLabel.setInnerText(LocalizedText.INSTANCE.context());

		contextTableCell = DomQuery.selectNode(selectContextTableCell, getElement()).cast();
		contextTableCell.appendChild(contextField.getElement());

		contextChooseButton = DomQuery.selectNode(selectContextChooseButton, getElement()).cast();
		contextDeleteButton = DomQuery.selectNode(selectContextDeleteButton, getElement()).cast();
		contextDeleteButton.setSrc(WbStorageUiResources.INSTANCE.delete().getSafeUri().asString());

		multiSelectionLabel = DomQuery.selectNode(selectMultiSelectionLabel, getElement()).cast();
		multiSelectionLabel.setInnerText(LocalizedText.INSTANCE.multiSelection());

		multiSelectionTableCell = DomQuery.selectNode(selectMultiSelectionTableCell, getElement()).cast();
		multiSelectionTableCell.appendChild(createMultiSelectionCheckBox().getElement());
		
		forceFormLabel = DomQuery.selectNode(selectForceFormLabel, getElement()).cast();
		forceFormLabel.setInnerText(LocalizedText.INSTANCE.forceForm());
		
		forceFormTableCell = DomQuery.selectNode(selectForceFormTableCell, getElement()).cast();
		forceFormTableCell.appendChild(createForceFormCheckBox().getElement());
		
		autoPagingSizeLabel = DomQuery.selectNode(selectAutoPagingSizeLabel, getElement()).cast();
		autoPagingSizeLabel.setInnerText(LocalizedText.INSTANCE.autoPagingSize());
		
		autoPagingSizeTableCell = DomQuery.selectNode(selectAutoPagingSizeTableCell, getElement()).cast();
		autoPagingSizeTableCell.appendChild(createAutoPagingSizeField().getElement());

		defaultViewLabel = DomQuery.selectNode(selectDefaultViewLabel, getElement()).cast();
		defaultViewLabel.setInnerText(LocalizedText.INSTANCE.defaultView());

		defaultViewTableCell = DomQuery.selectNode(selectDefaultViewTableCell, getElement()).cast();
		defaultViewTableCell.appendChild(createDefaultViewComboBox().getElement());

		okButtonTable = DomQuery.selectNode(selectOkButtonTable, getElement()).cast();
		cancelButtonTable = DomQuery.selectNode(selectCancelButtonTable, getElement()).cast();

		okButtonImage = DomQuery.selectNode(selectOkButtonImage, getElement()).cast();
		okButtonImage.setSrc(WbStorageUiResources.INSTANCE.apply().getSafeUri().asString());

		okButtonLabel = DomQuery.selectNode(selectOkButtonLabel, getElement()).cast();
		okButtonLabel.setInnerText(LocalizedText.INSTANCE.ok());

		cancelButtonImage = DomQuery.selectNode(selectCancelButtonImage, getElement()).cast();
		cancelButtonImage.setSrc(WbStorageUiResources.INSTANCE.cancel().getSafeUri().asString());

		cancelButtonLabel = DomQuery.selectNode(selectCancelButtonLabel, getElement()).cast();
		cancelButtonLabel.setInnerText(LocalizedText.INSTANCE.cancel());

		// Draw layout
		setGxtFieldsWidth();
		forceLayout();

		// Add Events to Element
		addDomHandler(this, ClickEvent.getType());
		addHandler(this, ResizeEvent.getType());
	}

	private LocalizedStringField createFolderNameField() {
		if (folderNameField == null) {
			folderNameField = localizedStringFieldSupplier.get();
			addWidgetToRootPanel(folderNameField);

			folderNameField.setValidateOnBlur(false);
			folderNameField.setUsedAsElement(true);
			folderNameField.setAutoValidate(false);
			folderNameField.setAllowBlank(false);
			folderNameField.setHideTrigger(true);
		}

		return folderNameField;
	}

	private CheckBoxControl createMultiSelectionCheckBox() {
		if (multiSelectionCheckBox == null) {
			multiSelectionCheckBox = new CheckBoxControl();
			multiSelectionCheckBox.setCheckBoxClassName(checkBoxClass);
		}

		return multiSelectionCheckBox;
	}
	
	private CheckBoxControl createForceFormCheckBox() {
		if (forceFormCheckBox != null)
			return forceFormCheckBox;
		
		forceFormCheckBox = new CheckBoxControl();
		forceFormCheckBox.setCheckBoxClassName(checkBoxClass);
		
		return forceFormCheckBox;
	}
	
	private NumberField<Integer> createAutoPagingSizeField() {
		if (autoPagingSizeField != null)
			return autoPagingSizeField;
		
		autoPagingSizeField = new NumberField<>(new IntegerPropertyEditor());
		autoPagingSizeField.setAllowDecimals(false);
		addWidgetToRootPanel(autoPagingSizeField);
		
		return autoPagingSizeField;
	}

	private SimpleComboBox<String> createDefaultViewComboBox() {
		if (defaultViewComboBox != null)
			return defaultViewComboBox;
		
		ListStore<String> store = new ListStore<String>(item -> item);
		StringLabelProvider<String> labelProvider = new StringLabelProvider<>();
		
		LabelProviderSafeHtmlRenderer<String> renderer = new LabelProviderSafeHtmlRenderer<String>(labelProvider) {
			@Override
			public SafeHtml render(String string) {
				if (string.isEmpty()) {
					SafeHtmlBuilder sb = new SafeHtmlBuilder();
					sb.appendHtmlConstant("&nbsp;");
				    return sb.toSafeHtml();
				}
				
				return super.render(string);
			}
		};
		
		defaultViewComboBox = new SimpleComboBox<>(new ComboBoxCell<>(store, labelProvider, renderer));
		addWidgetToRootPanel(defaultViewComboBox);

		defaultViewComboBox.setEmptyText(LocalizedText.INSTANCE.emptyDefaultViewText());
		defaultViewComboBox.setTriggerAction(TriggerAction.ALL);
		defaultViewComboBox.setValidateOnBlur(false);
		defaultViewComboBox.setAutoValidate(false);
		defaultViewComboBox.setHideTrigger(false);
		defaultViewComboBox.setAllowBlank(true);
		defaultViewComboBox.setEditable(false);

		// Add selections to field widget
		defaultViewComboBox.add("");
		defaultViewComboBox.add(KnownUseCase.assemblyPanelUseCase.getDefaultValue());
		defaultViewComboBox.add(KnownUseCase.thumbnailPanelUseCase.getDefaultValue());

		return defaultViewComboBox;
	}

	private static void addWidgetToRootPanel(final Widget widget) {
		// Add to RootPanel
		RootPanel.get().add(widget);
	}

	/******************************* Receiver Method *******************************/

	@Override
	public void accept(final WbSettingQueryDialogConfig dialogConfig) throws RuntimeException {
		workbenchSession = dialogConfig.getWorkbenchSession();
		futureDialogResult = dialogConfig.getDialogResult();
		workbench = dialogConfig.getWorkbench();

		// Set workbenchSession
		folderNameField.setGmSession(workbenchSession);
		iconField.setInitialConfiguration(new SimplifiedEntityFieldConfiguration(Icon.T, iconEntityQuery, workbenchSession, useCase, false,
				LocalizedText.INSTANCE.assignProperty(LocalizedText.INSTANCE.icon())));
		
		SimplifiedEntityFieldConfiguration parentFolderFieldConfig = new SimplifiedEntityFieldConfiguration(Folder.T, simplifiedEntityQueryTabs, workbenchSession,
				useCase, true, LocalizedText.INSTANCE.assignProperty(LocalizedText.INSTANCE.parentFolder()));
		parentFolderFieldConfig.setDialogHeight(600);
		
		parentFolderField.setInitialConfiguration(parentFolderFieldConfig);
		contextField.setInitialConfiguration(new SimplifiedEntityFieldConfiguration(TraversingCriterion.T, contextEntityQuery, workbenchSession,
				useCase, false, LocalizedText.INSTANCE.assignProperty(LocalizedText.INSTANCE.context())));

		workbench.suspendHistoryListener();
		nestedTransaction = workbenchSession.getTransaction().beginNestedTransaction();

		// Clear old values
		folderNameField.clear();
		parentFolderField.clear();
		iconField.clear();
		contextField.clear();
		multiSelectionCheckBox.setChecked(false);
		forceFormCheckBox.setChecked(false);
		autoPagingSizeField.clear();
		defaultViewComboBox.clear();

		final Folder queryFolder = dialogConfig.getQueryFolder();
		originalLocalizedString = queryFolder.getDisplayName();
		folderNameField.setValue(cloneLocalizedString(originalLocalizedString));
		parentFolderField.setValue(queryFolder.getParent());
		iconField.setValue(queryFolder.getIcon());

		// Check the Folder-Content type for QueryAction
		if (queryFolder.getContent() instanceof QueryAction) {
			final QueryAction queryAction = (QueryAction) queryFolder.getContent();

			// Set field values
			contextField.setValue(queryAction.getInplaceContextCriterion());
			multiSelectionCheckBox.setChecked(queryAction.getMultiSelectionSupport());

			// Enable fields
			enableQueryActionFields(true);
		} else {
			// Disable fields
			enableQueryActionFields(false);
		}
		
		
		if (!(queryFolder.getContent() instanceof TemplateBasedAction))
			enableTemplateActionFields(false);
		else {
			TemplateBasedAction action = (TemplateBasedAction) queryFolder.getContent();
			forceFormCheckBox.setChecked(action.getForceFormular());
			
			for (TemplateMetaData metadata : action.getTemplate().getMetaData()) {
				if (metadata instanceof AutoPagingSize)
					autoPagingSizeField.setValue(((AutoPagingSize) metadata).getSize());
				else if (metadata instanceof DefaultView)
					defaultViewComboBox.setValue(((DefaultView) metadata).getViewIdentification());
			}
			
			enableTemplateActionFields(true);
		}

		show();
		handleResult = true;
	}

	/******************************** Event Methods ********************************/

	@Override
	public void onClick(final ClickEvent event) {
		final NativeEvent nativeEvent = event.getNativeEvent();
		final Element targetElement = nativeEvent.getEventTarget().cast();

		if (parentFolderButton.isOrHasChild(targetElement))
			triggerFieldAction(parentFolderField, event);
		else if (iconChooseButton.isOrHasChild(targetElement))
			triggerFieldAction(iconField, event);
		else if (iconDeleteButton.isOrHasChild(targetElement))
			iconField.setValue(null);
		else if (contextChooseButton.isOrHasChild(targetElement)) {
			if (contextField.isEnabled())
				triggerFieldAction(contextField, event);
		} else if (contextDeleteButton.isOrHasChild(targetElement)) {
			if (contextField.isEnabled())
				contextField.setValue(null);
		} else if (isElementChild(okButtonTable, targetElement)) {
			if (!requiredFieldsValid())
				return;

			// Resume histery listener
			nestedTransaction.commit();
			workbench.resumeHistoryListener();
			super.hide();

			// Set storage parameters to dialog result
			final WbSettingQueryDialogResult dialogResult = new WbSettingQueryDialogResult();
			dialogResult.setParentFolder((Folder) parentFolderField.getValue());
			dialogResult.setFolderName(folderNameField.getValue());
			dialogResult.setIcon((Icon) iconField.getValue());
			dialogResult.setContext((TraversingCriterion) contextField.getValue());
			dialogResult.setMultiSelection(multiSelectionCheckBox.isChecked());
			dialogResult.setForceForm(forceFormCheckBox.isChecked());
			dialogResult.setAutoPagingSize(autoPagingSizeField.getValue());
			dialogResult.setDefaultView(defaultViewComboBox.getValue());

			futureDialogResult.onSuccess(dialogResult);
		} else if (isElementChild(cancelButtonTable, targetElement)) {
			try {
				nestedTransaction.rollback();
			} catch (final TransactionException e) {
				GlobalState.showError(e.getMessage(), e);
			} finally {
				// Resume histery listener
				workbench.resumeHistoryListener();
				super.hide();
			}

			futureDialogResult.onFailure(null);
		}
	}
	
	@Override
	public void hide() {
		if (handleResult) {
			handleResult = false;
			try {
				nestedTransaction.rollback();
			} catch (final TransactionException e) {
				GlobalState.showError(e.getMessage(), e);
			} finally {
				// Resume histery listener
				workbench.resumeHistoryListener();
			}

			futureDialogResult.onFailure(null);
		}
		
		super.hide();
	}

	@Override
	public void onResize(final ResizeEvent event) {
		// Draw layout
		setGxtFieldsWidth();
		forceLayout();
	}

	/******************************** Helper Methods *******************************/

	private void setGxtFieldsWidth() {
		// Set width and redraw trigger fields
		setTriggerFieldWidth(folderNameField, folderNameTableCell.getClientWidth());
		setTriggerFieldWidth(parentFolderField, parentFolderTableCell.getClientWidth());
		setTriggerFieldWidth(contextField, contextTableCell.getClientWidth());
		setTriggerFieldWidth(autoPagingSizeField, autoPagingSizeTableCell.getClientWidth());
		setTriggerFieldWidth(defaultViewComboBox, defaultViewTableCell.getClientWidth());
	}

	private static void setTriggerFieldWidth(final TriggerField<?> triggerField, final int width) {
		if (triggerField != null) {
			triggerField.getCell().setWidth(width);
			triggerField.setWidth(width);
			triggerField.redraw();
		}
	}

	private void enableQueryActionFields(final boolean value) {
		// Disable context elements
		contextLabel.getStyle().setOpacity(value ? 1d : 0.5d);
		contextField.setEnabled(value);

		// Disable context buttons
		contextChooseButton.getStyle().setOpacity(value ? 1d : 0.5d);
		contextDeleteButton.getStyle().setOpacity(value ? 1d : 0.5d);

		// Disable multi selection elements
		this.multiSelectionLabel.getStyle().setOpacity(value ? 1d : 0.5d);
		this.multiSelectionCheckBox.setEnabled(value);
	}
	
	private void enableTemplateActionFields(boolean enable) {
		forceFormLabel.getStyle().setOpacity(enable ? 1d : 0.5d);
		forceFormCheckBox.setEnabled(enable);
	}

	private static boolean isElementChild(final Element element, final Node child) {
		if (element != null && element != child)
			return element.isOrHasChild(child);

		return false;
	}

	private void triggerFieldAction(final SimplifiedEntityField field, final ClickEvent event) {
		final TriggerInfo triggerInfo = new TriggerInfo();
		triggerInfo.put(TriggerInfo.PROPERTY_COMPONENTEVENT, event);
		triggerInfo.setWidget(this);

		field.getTriggerFieldAction().perform(triggerInfo);
	}

	private boolean requiredFieldsValid() {
		boolean isValid = true;

		this.folderNameField.clearInvalid();
		isValid &= folderNameField.isValid();

		this.parentFolderField.clearInvalid();
		isValid &= parentFolderField.isValid();

		this.contextField.clearInvalid();
		isValid &= contextField.isValid();

		/*if (this.defaultViewComboBox.isEditable() == true) {
			this.defaultViewComboBox.clearInvalid();
			isValid &= this.defaultViewComboBox.isValid();
		}*/

		return isValid;
	}

	/*************************** LocalizedString Methods ***************************/

	private static LocalizedString cloneLocalizedString(LocalizedString entity) {
		if (entity != null) {
			// Get and use EntityType to clone LocalizedString
			return (LocalizedString) LocalizedString.T.clone(entity, null, null);
		}

		return null;
	}
}
