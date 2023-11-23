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
package com.braintribe.gwt.genericmodelgxtsupport.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface LocalizedText extends Messages {
	
	public static LocalizedText INSTANCE = GWT.create(LocalizedText.class);
	
	public String add();
	public String addLocaleDescription();
	public String addLocalization();
	public String addLocalizationDescription();
	public String apply();
	public String cancel();
	public String cancelDescription();
	public String changeColor();
	public String changeColorDescription();
	public String chooseColor();
	public String clear();
	public String clearLocalizationsDescription();
	public String closeDescription();
	public String color();
	public String fontDialog();
	public String fontName();
	public String fontSize();
	public String fontStyle();
	public String fontWeight();
	String htmlEditor();
	public String invalidColor();
	public String invalidFormat();
	public String invalidTime();
	public String locale();
	public String localeExistsAlready();
	public String localizedValues();
	public String of();
	public String ok();
	public String remove();
	public String removeLocalizationDescription();
	public String value();
	String hourMinuteSecondRegex();
	String hourMinuteSecondMillisecondRegex();
	String multiline();
	String multilineDescription();
	
}
