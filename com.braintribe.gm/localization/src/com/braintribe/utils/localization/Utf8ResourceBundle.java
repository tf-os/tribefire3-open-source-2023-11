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

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This makes sure that we can read UTF8 property files.
 */
public abstract class Utf8ResourceBundle {

	protected static Utf8Control utf8Control = new Utf8Control(); 

	public static ResourceBundle getBundle(String baseName, Locale locale, ClassLoader loader) {
		return ResourceBundle.getBundle(baseName, locale, loader, utf8Control);
	}

	public static ResourceBundle getBundle(URL url, Locale locale, ClassLoader loader) {
		String baseName = url.getPath();
		// Removing .properties from the name
		baseName = baseName.replace(".properties", "");
		return ResourceBundle.getBundle(baseName, locale, loader, utf8Control);
	}

}
