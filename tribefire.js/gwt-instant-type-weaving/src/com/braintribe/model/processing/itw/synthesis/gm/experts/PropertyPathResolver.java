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
package com.braintribe.model.processing.itw.synthesis.gm.experts;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.utils.i18n.I18nTools;

public class PropertyPathResolver {

	public static String getLocale(GenericEntity entity) {
		GmSession gmSession = entity.session();
		if (gmSession != null) {
			// return gmSession.getLocale;
			// TODO this should be finished once GmSession supports the 'getLocale()' method.
		}

		return GMF.getLocale();
	}

	public static Object resolvePropertyPath(GenericEntity entity, Property propertyChain[]) {
		Object value = entity;
		for (int i = 0; value != null && i < propertyChain.length; i++) {
			value = propertyChain[i].get((GenericEntity) value);
		}

		if (value instanceof LocalizedString) {

			String locale = getLocale(entity);
			LocalizedString ls = (LocalizedString) value;
			return I18nTools.get(ls, locale);
		} else
			return value != null ? value : "";
	}
}
