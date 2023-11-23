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
package com.braintribe.gwt.gm.storage.impl.wb.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.sencha.gxt.core.client.XTemplates;

public interface WbStorageTemplates extends XTemplates {

	public static final WbStorageTemplates INSTANCE = GWT.create(WbStorageTemplates.class);

	@XTemplate(source = "wbStorageSaveDialog.html")
	SafeHtml wbStorageSaveDialog(String formDescriptionLabelId, String folderNameLabelId, String folderNameTableCellId, String parentFolderLabelId,
			String parentFolderTableCellId, String parentFolderButtonId, String okButtonTableId, String okButtonImageId, String okButtonLabelId,
			String cancelButtonTableId, String cancelButtonImageId, String cancelButtonLabelId);

	@XTemplate(source = "wbStorageSettingDialog.html")
	SafeHtml wbStorageSettingDialog(String formDescriptionLabelId, String folderNameLabelId, String folderNameTableCellId, String parentFolderLabelId,
			String parentFolderTableCellId, String parentFolderButtonId, String iconLabelId, String iconImageId, String iconChooseButtonId,
			String iconDeleteButtonId, String contextLabelId, String contextTableCellId, String contextChooseButtonId, String contextDeleteButtonId,
			String multiSelectionLabelId, String multiSelectionTableCellId, String forceFormLabelId, String forceFormTableCellId,
			String autoPagingSizeLabelId, String autoPagingSizeTableCellId, String defaultViewLabelId, String defaultViewTableCellId,
			String okButtonTableId, String okButtonImageId, String okButtonLabelId, String cancelButtonTableId, String cancelButtonImageId,
			String cancelButtonLabelId);
}
