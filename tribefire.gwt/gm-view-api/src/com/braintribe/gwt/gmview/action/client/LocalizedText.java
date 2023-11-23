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

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface LocalizedText extends Messages {
	
	public static LocalizedText INSTANCE = ((LocalizedTextFactory) GWT.create(LocalizedTextFactory.class)).getLocalizedText();
	
	String actions();
	String add();
	String addingMapKey();
	String addingMapValue();
	String addMetaData();
	String addProperty(String propertyName, String entityInstance);
	String assign();
	String assignProperty(String propertyName, String entityInstance);
	String back();
	String backDescription();
	String cancel();
	String cannotUploadFolders();
	String clearCollection();
	String clearCollectionProperty();
	String close();
	String condensation();
	String confirmDeletionText(String entityDisplayInfo, String selectiveInfo);
	String confirmDeletionTitle();
	String confirmMultipleDeletionText();
	String copyId();
	String copyText();
	String createEntity(String entityName);
	String deleteEntity(String entityName);
	String details();
	String differentAmountOfKeysAndValues(int amountKeys, int amountValues);
	String downloadResource();
	String entityIdCopiedClipboard(String ids);
	String editEntity();
	String editTemplate();
	String errorAddingEntries();
	String errorChangingProperty();
	String errorInstantiatingEntity();
	String errorLoadingEntities();
	String errorRollingBack();
	String exchangeView();
	String execute();
	String filter();
	String filterDescription();
	String insertBefore();
	String less();
	String listView();
	String loadingActions();
	String loadingExternalComponent();
	String maximize();
	String more();
	String newEntity();
	String newTransientEntity();
	String newType(String type);
	String okDescription();
	String openWebReader();
	String record();
	String refresh();
	String refreshing();
	String refreshMetaData();
	String reInstantiate();
	String removeFromCollection();
	String removeFromCollectionProperty();
	String removeMetaData();
	String restore();
	String select();
	String selectType(String type);
	String selectTypeOrValue();
	String serviceRequests();
	String setNull();
	String showHideDetails();
	String switchTo();
	String toClipboard();
	String toMapKey();
	String toMapValue();
	String types();
	String values();
	String workWithEntity();

}
