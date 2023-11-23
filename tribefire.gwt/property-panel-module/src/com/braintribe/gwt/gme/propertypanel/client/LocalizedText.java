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
package com.braintribe.gwt.gme.propertypanel.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface LocalizedText extends Messages {
	
	public static LocalizedText INSTANCE = ((LocalizedTextFactory) GWT.create(LocalizedTextFactory.class)).getLocalizedText();
	
	String add();
	String addDescription();
	String addToCollection();
	String applyDescription();
	String assign();
	String back();
	String backDescription();
	String base();
	String changeDescription();
	String changeNewExistingDescription();
	String clearCollection();
	String clearCollectionDescription();
	String clearText();
	String clearTextDescription();
	String close();
	String closeDescription();
	String dropFile();
	String empty();
	String errorChangingPropertyValue();
	String errorLoadingAbsentProperties();
	String errorRollingEditionBack();
	String errorRunningOnEditRequest();
	String execute();
	String executeDescription();
	String frontDescription();
	String instantiate();
	String instantiateDescription();
	String loadingAbsentProperty();
	String noOptionsAvailable();
	String notSet();
	String of();
	String ok();
	String open();
	String openDescription();
	String query();
	String queryDescription();
	String quickAccess();
	String readOnly();
	String select(String typeName);
	String selectDescription();
	String setNull();
	String setPropertyToNull();
	String subType();
	String typeToShowValues();
	String typeValue();
	String uploading();
	String workWith();
	String workWithDescription();
	

}
