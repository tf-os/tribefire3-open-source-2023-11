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
package com.braintribe.gwt.processdesigner.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface LocalizedText extends Messages {
	public static final LocalizedText INSTANCE = GWT.create(LocalizedText.class);
	
	String add();
	String cancel();
	String color();
	String configuration();
	String connect();
	String defineInitNode();
	String deleteConfirmation();
	String deleteElements();
	String group();
	String loadingProcessDefinition();
	String mask();
	String mode(String mode);
	String print();
	String remove();
	String render();
	String restartNode();
	String select();
	String standardNode();
	String undefineInitNode();
	String zoom();
	String zoomIn();
	String zoomOut();

}
