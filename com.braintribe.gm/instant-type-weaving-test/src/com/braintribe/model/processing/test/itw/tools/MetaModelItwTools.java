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
package com.braintribe.model.processing.test.itw.tools;

import static com.braintribe.model.generic.builder.meta.MetaModelBuilder.entityType;
import static com.braintribe.model.generic.builder.meta.MetaModelBuilder.property;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Arrays;
import java.util.List;

import com.braintribe.model.generic.builder.meta.MetaModelBuilder;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;

/**
 * @author peter.gazdik
 */
public class MetaModelItwTools {

	public static GmEntityType newGmEntityType(String typeSignature, GmEntityType superType) {
		GmEntityType result = entityType(typeSignature, Arrays.asList(superType));
		result.setIsAbstract(false);
		return result;
	}

	public static GmProperty addProperty(GmEntityType entityType, String name, GmType type) {
		GmProperty p = property(entityType, name, type);

		List<GmProperty> properties = entityType.getProperties();
		if (properties == null)
			entityType.setProperties(properties = newList());

		properties.add(p);

		return p;
	}

	public static GmEnumType enumType(String typeSignature) {
		return MetaModelBuilder.enumType(typeSignature);
	}

	public static GmEnumConstant addConstant(GmEnumType enumType, String name) {
		GmEnumConstant c = enumConstant(enumType, name);

		List<GmEnumConstant> constants = enumType.getConstants();
		if (constants == null)
			enumType.setConstants(constants = newList());

		constants.add(c);

		return c;
	}

	public static GmEnumConstant enumConstant(GmEnumType enumType, String name) {
		return MetaModelBuilder.enumConstant(enumType, name);
	}

}
