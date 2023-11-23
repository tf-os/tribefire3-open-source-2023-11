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
package com.braintribe.model.processing.itw.synthesis.gm.experts.template;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.processing.itw.synthesis.gm.experts.PropertyPathResolver;

public class VariableTemplatePart extends TemplatePart {
	private final String[] encodedPropertyPath;
	private Property propertyPath[];

	public VariableTemplatePart(String[] encodedPropertyPath) {
		this.encodedPropertyPath = encodedPropertyPath;
	}

	@Override
	public void append(StringBuilder builder, GenericEntity entity, EntityType<?> entityType) {
		if (propertyPath == null) {
			Property propertyPath[] = new Property[encodedPropertyPath.length];

			for (int i = 0; i < encodedPropertyPath.length; i++) {
				String propertyName = encodedPropertyPath[i];

				Property property = entityType.getProperty(propertyName);

				propertyPath[i] = property;
				GenericModelType type = property.getType();

				if (type.getTypeCode() == TypeCode.entityType) {
					entityType = type.cast();
				} else {
					entityType = null;
				}
			}

			this.propertyPath = propertyPath;
		}

		builder.append(PropertyPathResolver.resolvePropertyPath(entity, propertyPath));
	}
}
