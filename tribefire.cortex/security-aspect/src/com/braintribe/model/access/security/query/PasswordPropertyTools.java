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
package com.braintribe.model.access.security.query;

import com.braintribe.logging.Logger;
import com.braintribe.model.access.security.query.QueryOperandTools.EntityTypeProperty;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.meta.data.prompt.Confidential;
import com.braintribe.model.processing.meta.cmd.CascadingMetaDataException;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;

/**
 * 
 */
class PasswordPropertyTools {

	public static final String HIDDEN_PASSWORD = "*****";

	private static final Logger log = Logger.getLogger(PasswordPropertyTools.class);

	public static Object getValueToReplacePassword(Property passwordProperty) {
		return getValueToReplacePassword(passwordProperty.getType());
	}

	public static Object getValueToReplacePassword(GenericModelType pawwordPropertyType) {
		if (pawwordPropertyType.getTypeCode() == TypeCode.stringType) {
			return HIDDEN_PASSWORD;

		} else {
			return null;
		}
	}

	public static boolean isPasswordProperty(EntityTypeProperty etp, ModelMdResolver mdResolver) {
		return isPasswordProperty(etp.entityType, etp.propertyName, mdResolver);
	}

	public static boolean isPasswordProperty(EntityType<?> et, String property, ModelMdResolver mdResolver) {
		return isPasswordProperty(property, mdResolver.entityType(et));
	}

	private static boolean isPasswordProperty(String property, EntityMdResolver mdResolver) {
		try {
			return mdResolver.property(property).is(Confidential.T);

		} catch (CascadingMetaDataException e) {
			log.warn("Error while resolving PasswordProperty meta data. ", e);

			return false;
		}
	}

}
