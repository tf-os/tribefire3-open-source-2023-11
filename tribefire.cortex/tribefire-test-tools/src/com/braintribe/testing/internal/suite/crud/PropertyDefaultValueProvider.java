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
package com.braintribe.testing.internal.suite.crud;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;

public class PropertyDefaultValueProvider implements PropertyValueProvider {
	private GenericModelType getType(PropertyMdResolver propertyMd) {
		return propertyMd.getGmProperty().type();
	}

	@Override
	public Object getSimple(PropertyMdResolver propertyMd) {
		GenericModelType propertyType = getType(propertyMd);

		Map<GenericModelType, Object> defaultValues = new HashMap<>();
		defaultValues.put(SimpleType.TYPE_STRING, getString(propertyMd));
		defaultValues.put(SimpleType.TYPE_BOOLEAN, true);
		defaultValues.put(SimpleType.TYPE_DATE, new Date());
		defaultValues.put(SimpleType.TYPE_DECIMAL, new BigDecimal(2.4));
		defaultValues.put(SimpleType.TYPE_DOUBLE, 2.4d);
		defaultValues.put(SimpleType.TYPE_FLOAT, 2.4f);
		defaultValues.put(SimpleType.TYPE_INTEGER, getInteger(propertyMd));
		defaultValues.put(SimpleType.TYPE_LONG, 2l);

		// logger.debug("Set to " + defaultValues.get(propertyType));
		return defaultValues.get(propertyType);
	}

	@Override
	public Enum<?> getEnum(PropertyMdResolver propertyMd) {
		GenericModelType propertyType = getType(propertyMd);

		EnumType enumType = ((EnumType) propertyType);
		Enum<?> enumConstantJava = enumType.getEnumValues()[0];

		return enumConstantJava;
	}

	@Override
	public String getString(PropertyMdResolver propertyMd) {
		return "Hallo";
	}

	@Override
	public Integer getInteger(PropertyMdResolver propertyMd) {
		return 2;
	}

}
