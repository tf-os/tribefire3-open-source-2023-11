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
package com.braintribe.model.generic.i18n;

import java.util.Map;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import jsinterop.annotations.JsMethod;


public interface LocalizedString extends GenericEntity {

	EntityType<LocalizedString> T = EntityTypes.T(LocalizedString.class);

	String LOCALE_DEFAULT = "default";

	String localizedValues = "localizedValues";

	// @formatter:off
	void setLocalizedValues(Map<String, String> localizedValues);
	Map<String, String> getLocalizedValues();
	// @formatter:on

	@JsMethod
	default LocalizedString put(String locale, String value) {
		getLocalizedValues().put(locale, value);
		return this;
	}
	
	@JsMethod
	default LocalizedString putDefault(String value) {
		getLocalizedValues().put(LOCALE_DEFAULT, value);
		return this;
	}
	
	@JsMethod(name="defaultValue")
	default String value() {
		return value(GMF.getLocale());
	}

	@JsMethod(name="value")
	default String value(String locale) {	
		Map<String, String> map = getLocalizedValues();

		if (map == null) {
			return null;
		}

		while (locale != null) {
			String localizedName = map.get(locale);

			if (localizedName != null)
				return localizedName;

			int index = locale.lastIndexOf('_');
			if (index != -1)
				locale = locale.substring(0, index);
			else
				locale = null;
		}

		return map.get(LOCALE_DEFAULT);
	}
	
	@JsMethod(name = "createLocalizedString", namespace = "$tf.i18n")
	static LocalizedString create(String defaultValue) {
		return T.create().putDefault(defaultValue);
	}

}
