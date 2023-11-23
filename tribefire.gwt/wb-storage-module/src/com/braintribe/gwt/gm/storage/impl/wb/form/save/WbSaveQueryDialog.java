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
package com.braintribe.gwt.gm.storage.impl.wb.form.save;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.LocalizedStringField;
import com.braintribe.gwt.gm.storage.impl.wb.resources.LocalizedText;
import com.braintribe.gwt.gm.storage.impl.wb.resources.WbStorageTemplates;
import com.braintribe.gwt.gm.storage.impl.wb.resources.WbStorageUiResources;
import com.braintribe.gwt.gme.propertypanel.client.field.SimplifiedEntityField;
import com.braintribe.gwt.gme.propertypanel.client.field.SimplifiedEntityFieldConfiguration;
import com.braintribe.gwt.gme.propertypanel.client.field.SimplifiedEntityFieldTabConfiguration;
import com.braintribe.gwt.gme.workbench.client.Workbench;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.api.transaction.TransactionException;
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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.DomQuery;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.form.TriggerField;

/**
 * Dialog used for saving a query.
 *
 *
 */
public class WbSaveQueryDialog extends Window implements InitializableBean, Consumer<WbSaveQueryDialogConfig>, ClickHandler, ResizeHandler {

	/********************************** Constants **********************************/

	private final String formDescriptionLabelId = hashCode() + "_formDescriptionLabel";
	private final String folderNameLabelId = hashCode() + "_folderNameLabel";
	private final String folderNameTableCellId = hashCode() + "_folderNameTableCell";
	private final String parentFolderLabelId = hashCode() + "_parentFolderLabel";
	private final String parentFolderTableCellId = hashCode() + "_parentFolderTableCell";
	private final String parentFolderButtonId = hashCode() + "_parentFolderButton";
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
	private final String selectOkButtonTable = "div[@id='" + okButtonTableId + "']";
	private final String selectOkButtonImage = "img[@id='" + okButtonImageId + "']";
	private final String selectOkButtonLabel = "label[@id='" + okButtonLabelId + "']";
	private final String selectCancelButtonTable = "div[@id='" + cancelButtonTableId + "']";
	private final String selectCancelButtonImage = "img[@id='" + cancelButtonImageId + "']";
	private final String selectCancelButtonLabel = "label[@id='" + cancelButtonLabelId + "']";

	private static final EntityType<Folder> folderEntityType = Folder.T;

	/********************************** Variables **********************************/

	private LabelElement formDescriptionLabel = null;
	private LabelElement folderNameLabel = null;
	private DivElement folderNameTableCell = null;
	private LabelElement parentFolderLabel = null;
	private DivElement parentFolderTableCell = null;
	private DivElement parentFolderButton = null;
	private DivElement okButtonTable = null;
	private ImageElement okButtonImage = null;
	private LabelElement okButtonLabel = null;
	private DivElement cancelButtonTable = null;
	private ImageElement cancelButtonImage = null;
	private LabelElement cancelButtonLabel = null;

	private Future<WbSaveQueryDialogResult> futureDialogResult = null;
	private PersistenceGmSession workbenchSession = null;
	private NestedTransaction nestedTransaction = null;
	private Workbench workbench = null;

	private LocalizedStringField folderNameField = null;
	private SimplifiedEntityField parentFolderField = null;

	private String useCase = null;
	private List<SimplifiedEntityFieldTabConfiguration> simplifiedEntityQueryTabs = null;
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
	 * Configures an optional query to be used by the {@link SimplifiedEntityField} configured via {@link #setParentFolderField(SimplifiedEntityField)}. Please notice that it MUST be a query for the {@link Folder}
	 * type.
	 */
	@Required
	public void setParentFolderEntityQueryTabs(final List<SimplifiedEntityFieldTabConfiguration> simplifiedEntityQueryTabs) {
		this.simplifiedEntityQueryTabs = simplifiedEntityQueryTabs;
	}

	/**
	 * Configures the required useCase.
	 */
	@Required
	public void setUseCase(final String useCase) {
		this.useCase = useCase;
	}
	
	/**
	 * Configures the supplier for the {@link LocalizedStringField}.
	 */
	@Required
	public void setLocalizedStringFieldSupplier(Supplier<? extends LocalizedStringField> localizedStringFieldSupplier) {
		this.localizedStringFieldSupplier = localizedStringFieldSupplier;
	}

	public WbSaveQueryDialog() {
		setHeaderVisible(false);
		setBodyBorder(false);
		setBorders(false);

		setResizable(false);
		setClosable(false);
		setOnEsc(false);
		setModal(true);

		setSize("400px", "145px");
		setBodyStyle("backgroundColor:white");

		// Needed to enable events
		addWidgetToRootPanel(this);
	}

	/****************************** InitializableBean ******************************/

	@Override
	public void intializeBean() {
		// Get and initialize template Elements
		add(new HTML(WbStorageTemplates.INSTANCE.wbStorageSaveDialog(formDescriptionLabelId, folderNameLabelId, folderNameTableCellId,
				parentFolderLabelId, parentFolderTableCellId, parentFolderButtonId, okButtonTableId, okButtonImageId, okButtonLabelId,
				cancelButtonTableId, cancelButtonImageId, cancelButtonLabelId)));

		Element rootElement = getElement();
		
		formDescriptionLabel = DomQuery.selectNode(selectFormDescriptionLabel, rootElement).cast();
		formDescriptionLabel.setInnerText(LocalizedText.INSTANCE.saveFormDescription());

		folderNameLabel = DomQuery.selectNode(selectFolderNameLabel, rootElement).cast();
		folderNameLabel.setInnerText(LocalizedText.INSTANCE.folderName());

		folderNameTableCell = DomQuery.selectNode(selectFolderNameTableCell, rootElement).cast();
		folderNameTableCell.appendChild(createFolderNameField().getElement());

		parentFolderLabel = DomQuery.selectNode(selectParentFolderLabel, rootElement).cast();
		parentFolderLabel.setInnerText(LocalizedText.INSTANCE.parentFolder() + ":");

		parentFolderTableCell = DomQuery.selectNode(selectParentFolderTableCell, rootElement).cast();
		parentFolderTableCell.appendChild(parentFolderField.getElement());

		parentFolderButton = DomQuery.selectNode(selectParentFolderButton, rootElement).cast();
		okButtonTable = DomQuery.selectNode(selectOkButtonTable, rootElement).cast();
		cancelButtonTable = DomQuery.selectNode(selectCancelButtonTable, rootElement).cast();

		okButtonImage = DomQuery.selectNode(selectOkButtonImage, rootElement).cast();
		okButtonImage.setSrc(WbStorageUiResources.INSTANCE.apply().getSafeUri().asString());

		okButtonLabel = DomQuery.selectNode(selectOkButtonLabel, rootElement).cast();
		okButtonLabel.setInnerText(LocalizedText.INSTANCE.ok());

		cancelButtonImage = DomQuery.selectNode(selectCancelButtonImage, rootElement).cast();
		cancelButtonImage.setSrc(WbStorageUiResources.INSTANCE.cancel().getSafeUri().asString());

		cancelButtonLabel = DomQuery.selectNode(selectCancelButtonLabel, rootElement).cast();
		cancelButtonLabel.setInnerText(LocalizedText.INSTANCE.cancel());

		// Draw layout
		setGxtFieldsWidth();
		forceLayout();

		// Add Events to Element
		addDomHandler(this, ClickEvent.getType());
		addHandler(this, ResizeEvent.getType());
	}

	private LocalizedStringField createFolderNameField() {
		if (folderNameField != null)
			return folderNameField;
		
		folderNameField = localizedStringFieldSupplier.get();
		addWidgetToRootPanel(folderNameField);

		folderNameField.setValidateOnBlur(false);
		folderNameField.setUsedAsElement(true);
		folderNameField.setAutoValidate(false);
		folderNameField.setAllowBlank(false);
		folderNameField.setHideTrigger(true);

		return folderNameField;
	}

	private static void addWidgetToRootPanel(final Widget widget) {
		RootPanel.get().add(widget);
	}

	/******************************* Receiver Method *******************************/

	@Override
	public void accept(WbSaveQueryDialogConfig dialogConfig) {
		workbenchSession = dialogConfig.getWorkbenchSession();
		futureDialogResult = dialogConfig.getDialogResult();
		workbench = dialogConfig.getWorkbench();

		SimplifiedEntityFieldConfiguration parentFolderFieldConfig = new SimplifiedEntityFieldConfiguration(folderEntityType,
				simplifiedEntityQueryTabs, workbenchSession, useCase, true,
				LocalizedText.INSTANCE.assignProperty(LocalizedText.INSTANCE.parentFolder()));
		parentFolderFieldConfig.setDialogHeight(600);
		parentFolderField.setInitialConfiguration(parentFolderFieldConfig);
		folderNameField.setGmSession(workbenchSession);

		workbench.suspendHistoryListener();
		nestedTransaction = workbenchSession.getTransaction().beginNestedTransaction();

		// Clear old values
		folderNameField.clear();
		parentFolderField.clear();

		show();
	}

	/******************************** Event Methods ********************************/

	@Override
	public void onClick(final ClickEvent event) {
		NativeEvent nativeEvent = event.getNativeEvent();
		Element targetElement = nativeEvent.getEventTarget().cast();

		if (parentFolderButton.isOrHasChild(targetElement)) {
			TriggerInfo triggerInfo = new TriggerInfo();
			triggerInfo.put(TriggerInfo.PROPERTY_COMPONENTEVENT, event);
			triggerInfo.setWidget(this);

			parentFolderField.getTriggerFieldAction().perform(triggerInfo);
		} else if (isElementChild(okButtonTable, targetElement)) {
			if (!requiredFieldsValid())
				return;

			// Resume history listener
			nestedTransaction.commit();
			workbench.resumeHistoryListener();
			hide();

			// Set storage parameters to dialog result
			WbSaveQueryDialogResult dialogResult = new WbSaveQueryDialogResult();
			dialogResult.setParentFolder((Folder) parentFolderField.getValue());
			dialogResult.setFolderName(folderNameField.getValue());

			futureDialogResult.onSuccess(dialogResult);
		} else if (isElementChild(cancelButtonTable, targetElement)) {
			try {
				nestedTransaction.rollback();
			} catch (final TransactionException e) {
				GlobalState.showError(e.getMessage(), e);
			} finally {
				// Resume history listener
				workbench.resumeHistoryListener();
				hide();
			}

			futureDialogResult.onFailure(null);
		}
	}

	@Override
	public void onResize(ResizeEvent event) {
		// Draw layout
		setGxtFieldsWidth();
		forceLayout();
	}

	/******************************** Helper Methods *******************************/

	private void setGxtFieldsWidth() {
		// Set width and redraw trigger fields
		setTriggerFieldWidth(folderNameField, folderNameTableCell.getClientWidth());
		setTriggerFieldWidth(parentFolderField, parentFolderTableCell.getClientWidth());
	}

	private static void setTriggerFieldWidth(TriggerField<?> triggerField, int width) {
		if (triggerField != null) {
			triggerField.getCell().setWidth(width);
			triggerField.setWidth(width);
			triggerField.redraw();
		}
	}

	private static boolean isElementChild(Element element, Node child) {
		if (element != null && element != child)
			return element.isOrHasChild(child);

		return false;
	}

	private boolean requiredFieldsValid() {
		boolean isValid = true;

		folderNameField.clearInvalid();
		isValid &= folderNameField.isValid();

		parentFolderField.clearInvalid();
		isValid &= parentFolderField.isValid();

		return isValid;
	}
}
