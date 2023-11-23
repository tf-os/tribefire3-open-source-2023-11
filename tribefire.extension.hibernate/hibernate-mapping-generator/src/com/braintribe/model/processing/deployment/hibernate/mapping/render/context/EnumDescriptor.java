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
package com.braintribe.model.processing.deployment.hibernate.mapping.render.context;

import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.deployment.hibernate.mapping.HbmXmlGenerationContext;

public class EnumDescriptor extends PropertyDescriptor {

	public final String enumSqlType, enumClass;

	protected EnumDescriptor(HbmXmlGenerationContext context, EntityDescriptor descriptor, GmProperty gmProperty,
			PropertyDescriptorMetaData metaData) {

		super(context, descriptor, gmProperty, metaData);

		this.enumSqlType = "12";
		this.enumClass = resolveEnumClass(gmProperty);
	}

	public String getEnumSqlType() {
		return enumSqlType;
	}

	public String getEnumClass() {
		return enumClass;
	}

	private static String resolveEnumClass(GmProperty gmProperty) {
		GmType type = gmProperty.getType();
		if (type.isGmEnum())
			return ((GmEnumType) type).getTypeSignature();
		else
			return null;
	}

	@Override
	public boolean getIsEnumType() {
		return true;
	}
}
