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
package com.braintribe.spring.support.converter;

import java.util.HashMap;

import org.springframework.core.convert.converter.Converter;

import com.braintribe.model.generic.i18n.LocalizedString;

public class StringToLocalizedStringConverter implements Converter<String, LocalizedString>{
	
	private String locale = "default";
	
	public void setLocale(String locale) {
		this.locale = locale;
	}
	
	@Override
	public LocalizedString convert(String source) {
		LocalizedString lc = LocalizedString.T.create();
		lc.setLocalizedValues(new HashMap<String, String>());
		lc.getLocalizedValues().put(locale, source);
		return lc;
	}
	
	

}
