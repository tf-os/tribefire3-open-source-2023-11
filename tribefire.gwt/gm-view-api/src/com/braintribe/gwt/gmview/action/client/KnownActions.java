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
package com.braintribe.gwt.gmview.action.client;

public enum KnownActions {
	
	WORK_WITH_ENTITY("workWithEntity", LocalizedText.INSTANCE.workWithEntity()),
	GIMA_OPENER("gimaOpener", LocalizedText.INSTANCE.editEntity()),
	GIMA_OPENER_FOR_DETAILS("gimaOpenerForDetails", LocalizedText.INSTANCE.details()),
	INSTANTIATE_ENTITY("instantiateEntity", LocalizedText.INSTANCE.newEntity()),
	DELETE_ENTITY("deleteEntity", LocalizedText.INSTANCE.deleteEntity("")),
	CHANGE_INSTANCE("changeInstance", LocalizedText.INSTANCE.assign()),
	CLEAR_ENTITY_TO_NULL("clearEntityToNull", LocalizedText.INSTANCE.setNull()),
	ADD_TO_COLLECTION("addToCollection", LocalizedText.INSTANCE.add()),
	INSERT_BEFORE_TO_LIST("insertBeforeToList", LocalizedText.INSTANCE.insertBefore()),
	REMOVE_FROM_COLLECTION("removeFromCollection", LocalizedText.INSTANCE.removeFromCollection()),
	CLEAR_COLLECTION("clearCollection", LocalizedText.INSTANCE.clearCollection()),
	EXCHANGE_CONTENT_VIEW("exchangeContentView", LocalizedText.INSTANCE.exchangeView()),
	CONDENSE_ENTITY("condenseEntity", LocalizedText.INSTANCE.condensation()),
	ADD_TO_CLIPBOARD("addToClipboard", LocalizedText.INSTANCE.toClipboard()),
	DISPLAY_MODE("displayMode", LocalizedText.INSTANCE.listView()),
	OPEN_GME_FOR_ACCESS_NEW_TAB("openGmeForAccessInNewTab", LocalizedText.INSTANCE.switchTo()),
	REFRESH_ENTITIES("refreshEntities", LocalizedText.INSTANCE.refresh()),
	DETAILS_PANEL_VISIBILITY("showDetailsPanel", LocalizedText.INSTANCE.showHideDetails()),
	RESOURCE_DOWNLOAD("ResourceDownload", LocalizedText.INSTANCE.downloadResource()),
	RECORD_TEMPLATE_SCRIPT("recordTemplateScript", LocalizedText.INSTANCE.record()),
	EDIT_TEMPLATE_SCRIPT("editTemplateScript", LocalizedText.INSTANCE.editTemplate()),
	ADD_METADATA_EDITOR("addMetaDataEditorAction", LocalizedText.INSTANCE.addMetaData()),
	REFRESH_METADATA_EDITOR("refreshMetaDataEditorAction", LocalizedText.INSTANCE.refreshMetaData()),
	REMOVE_METADATA_EDITOR("removeMetaDataEditorAction", LocalizedText.INSTANCE.removeMetaData()),
	OPEN_GME_FOR_WEB_TERMINAL_NEW_TAB("openGmeForAccessWebTerminalInNewTab", LocalizedText.INSTANCE.switchTo()),
	COPY_TEXT_TO_CLIPBOARD("copyTextToClipboard", LocalizedText.INSTANCE.copyText()),
	COPY_ID_TO_CLIPBOARD("copyIdToClipboard", LocalizedText.INSTANCE.copyId()),
	MAXIMIZE("maximize", LocalizedText.INSTANCE.maximize()),
	EXECUTE_SERVICE_REQUEST("executeServiceRequest", LocalizedText.INSTANCE.execute());

	
	KnownActions(String name, String displayName) {
		this.name = name;
		this.displayName = displayName;
	}
	
	private String name;
	private String displayName;
	
	public String getName() {
		return name;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
}
