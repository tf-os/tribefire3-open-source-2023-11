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
package com.braintribe.model.generic.tools;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmType;

/**
 * @author peter.gazdik
 */
public class GmModelTools {

	public static boolean areEntitiesReachable(GmType type) {
		switch (type.typeKind()) {
			case BASE:
			case ENTITY:
				return true;

			case LIST:
			case SET:
				return areEntitiesReachable(((GmLinearCollectionType) type).getElementType());

			case MAP: {
				GmMapType mapType = (GmMapType) type;
				return areEntitiesReachable(mapType.getKeyType()) || areEntitiesReachable(mapType.getValueType());
			}

			default:
				return false;
		}
	}

	public static <T extends GenericEntity> T createShallow(EntityType<T> entityType) {
		T entity = entityType.create();
		
		for (Property p : entityType.getProperties()) {
			if (!p.isIdentifying())
				p.setAbsenceInformation(entity, GMF.absenceInformation());
		}
		
		return entity;
	}
}
