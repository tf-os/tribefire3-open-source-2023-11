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
package com.braintribe.gwt.genericmodel.client;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.i18n.client.LocaleInfo;

public class CompoundPropertyRetrieval {

	public static Object retrieveCompoundProperty(Object entity, Property propertyChain[], boolean selective) {
		for (int i = 0; entity != null && i < propertyChain.length; i++) {
			entity = propertyChain[i].get((GenericEntity) entity);

			if (entity instanceof List<?>) {
				List<?> list = (List<?>) entity;
				entity = list.isEmpty() ? null : list.get(0);
			}
		}

		if (entity == null)
			return "";

		if (!(entity instanceof GenericEntity))
			return entity;

		if (entity instanceof LocalizedString) {
			String locale = LocaleInfo.getCurrentLocale().getLocaleName();
			LocalizedString ls = (LocalizedString) entity;
			return I18nTools.get(ls, locale);
		}

		return selective ? ((GenericEntity) entity).toSelectiveInformation() : entity;
	}

}
