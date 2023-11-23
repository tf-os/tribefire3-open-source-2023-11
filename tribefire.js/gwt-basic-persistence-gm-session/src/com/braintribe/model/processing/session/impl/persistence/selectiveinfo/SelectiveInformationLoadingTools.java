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
package com.braintribe.model.processing.session.impl.persistence.selectiveinfo;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static java.util.Collections.emptyList;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.display.SelectiveInformation;
import com.braintribe.model.processing.core.commons.SelectiveInformationSupport;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.utils.i18n.I18nTools;
import com.braintribe.utils.lcd.CommonTools;

/**
 * @author peter.gazdik
 */
public class SelectiveInformationLoadingTools {

	public static boolean needsLoading(GenericEntity entity) {
		PersistenceGmSession session = (PersistenceGmSession) entity.session();
		EntityType<GenericEntity> et = entity.entityType();

		List<Property[]> propertyChains = resolveSiPropertyChainsFor(et, session);

		return needsLoading(entity, propertyChains);
	}

	public static boolean needsLoading(GenericEntity entity, List<Property[]> propertyChains) {
		for (Property[] propertyChain : propertyChains)
			if (needsLoading(entity, propertyChain))
				return true;

		return false;
	}

	private static boolean needsLoading(GenericEntity entity, Property[] propertyChain) {
		Object propertyValue = null;
		for (Property property : propertyChain) {
			if (propertyValue != null)
				entity = (GenericEntity) propertyValue;

			if (property.getAbsenceInformation(entity) != null)
				return true;

			propertyValue = property.get(entity);
			if (propertyValue == null)
				return false;
		}
		return false;
	}

	public static List<Property[]> resolveSiPropertyChainsFor(EntityType<?> et, PersistenceGmSession session) {
		List<String[]> propertyNameChains = resolveSiPropertyNameChainsFor(et, session);
		return convertToPropertyChains(et, propertyNameChains);
	}

	public static List<String[]> resolveSiPropertyNameChainsFor(EntityType<?> et, PersistenceGmSession session) {
		SelectiveInformation si = session.getModelAccessory().getMetaData().lenient(true).entityType(et).meta(SelectiveInformation.T).exclusive();
		if (si == null)
			return emptyList();

		String siTemplate = I18nTools.getLocalized(si.getTemplate());
		if (CommonTools.isEmpty(siTemplate))
			return emptyList();

		List<String[]> propertyNameChains = SelectiveInformationSupport.extractPropertyChains(siTemplate);
		if (propertyNameChains.isEmpty())
			return emptyList();

		return propertyNameChains;
	}

	public static List<Property[]> convertToPropertyChains(EntityType<?> et, List<String[]> propertyNameChains) {
		List<Property[]> result = newList();
		for (String[] propertyNameChain : propertyNameChains)
			result.add(convertToPropertyChain(et, propertyNameChain));

		return result;
	}

	private static Property[] convertToPropertyChain(EntityType<?> et, String[] propertyNameChain) {
		Property[] result = new Property[propertyNameChain.length];

		Property property = null;
		for (int i = 0; i < propertyNameChain.length; i++) {
			String propertyName = propertyNameChain[i];

			if (property != null)
				et = (EntityType<?>) property.getType();

			property = et.getProperty(propertyName);
			result[i] = property;
		}

		return result;
	}

}
