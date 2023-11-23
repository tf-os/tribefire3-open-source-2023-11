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
package com.braintribe.model.processing.query.planner.tools;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.processing.query.planner.condition.JoinPropertyType;
import com.braintribe.model.processing.query.tools.SourceTypeResolver;
import com.braintribe.model.query.Join;

/**
 * 
 */
public class JoinTypeResolver {

	public static JoinPropertyType resolveJoinPropertyType(Join join) {
		EntityType<?> sourceType = SourceTypeResolver.resolveType(join.getSource());

		return resolveJoinPropertyType(sourceType, join.getProperty());
	}

	public static JoinPropertyType resolveJoinPropertyType(EntityType<?> operandType, String propertyName) {
		GenericModelType propertyType = operandType.getProperty(propertyName).getType();

		switch (propertyType.getTypeCode()) {
			case listType:
				return JoinPropertyType.list;
			case mapType:
				return JoinPropertyType.map;
			case setType:
				return JoinPropertyType.set;
			case entityType:
				return JoinPropertyType.entity;
			default:
				return JoinPropertyType.simple;
		}
	}

}
