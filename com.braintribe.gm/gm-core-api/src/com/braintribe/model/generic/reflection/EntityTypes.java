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
package com.braintribe.model.generic.reflection;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;

/**
 * @author peter.gazdik
 */
public class EntityTypes {

	private static GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	public static <T extends GenericEntity> EntityType<T> T(Class<T> clazz) {
		return typeReflection.getEntityType(clazz);
	}

	public static EntityType<?> get(String typeSignature) {
		return typeReflection.getType(typeSignature);
	}

}
