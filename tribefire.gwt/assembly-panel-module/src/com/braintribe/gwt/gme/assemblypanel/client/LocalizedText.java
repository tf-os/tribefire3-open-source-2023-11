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
package com.braintribe.gwt.gme.assemblypanel.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface LocalizedText extends Messages {
	
	public static LocalizedText INSTANCE = ((LocalizedTextFactory) GWT.create(LocalizedTextFactory.class)).getLocalizedText();
	
	String absent();
	String addToList();
	String addToMap();
	String addToSet();
	String cancel();
	String change();
	String changeInstance();
	String clearText();
	String clearTextDescription();
	String copying();
	String copyType();
	String deepCopy();
	String empty();
	String entitiesCopiedClipboard(int number);
	String entityCopiedClipboard(String entityName);
	String errorLoadingAbsentProperty(String propertyName);
	String errorPreparingNewInstance();
	String errorRollingEditionBack();
	String errorRunningOnEditRequest();
	String invalidPaste();
	String itemSelected(int number);
	String keyValuePairs();
	String link();
	String move();
	String node();
	String noItemsToDisplay();
	String noOptionsAvailable();
	String ok();
	String onlyEntitiesCanBeCopied();
	String onlySameEntityTypeCanBeCopied();
	String pastedEntityNotInstantiable();
	String setNull();
	String setPropertyToNull();
	String simpleCopy();
	String shallowCopy();
	String toClipboard();
	String toClipboardDescription();
	String workWith();
	String workWithDescription();

}
