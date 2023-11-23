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
package com.braintribe.utils.localization;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class LocalizationInvocationHandler<T extends Localizable> implements InvocationHandler {
  
	private ResourceBundle resourceBundle;
	private Class<T> interfaceClass;
	
	public LocalizationInvocationHandler(Class<T> interfaceClass) {
		this.interfaceClass = interfaceClass;
	}
	
	public ResourceBundle getResourceBundle() {
		if (resourceBundle == null) {
			Locale locale = Locale.getDefault();
			resourceBundle = Utf8ResourceBundle.getBundle(interfaceClass.getName(), locale, interfaceClass.getClassLoader());
		}

		return resourceBundle;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		
		String key = method.getName();
		String value = null; 
		
		try {
			value = getResourceBundle().getString(key); 
		} catch (MissingResourceException e) {
			Default def =  method.getAnnotation(Default.class);
			if (def != null) {
				value = def.value();
			}
			else {
				value = key;
			}
		}

		if (args != null && args.length != 0) {
			value = MessageFormat.format(value, args);
		}
		
		return value;
	}
}
