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
package tribefire.extension.spreadsheet.processing.importing.common;

import java.util.Comparator;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleType;

public class DistinctionComparator implements Comparator<GenericEntity> {
	private GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	@Override
	public int compare(GenericEntity o1, GenericEntity o2) {
		EntityType<GenericEntity> et1 = typeReflection.getType(o1);
		EntityType<GenericEntity> et2 = typeReflection.getType(o2);
		
		if (et1 != et2) {
			return et1.getTypeSignature().compareTo(et2.getTypeSignature());
		}
		else {
			for (Property property: et1.getProperties()) {
				if (property.getType() instanceof SimpleType) {
					Object v1 = property.get(o1);
					Object v2 = property.get(o2);
					
					int retValue;
					
					if (v1 == v2) 
						retValue = 0;
					else if (v1 == null)
						retValue = -1;
					else if (v2 == null)
						retValue = 1;
					else
						retValue = compareObjects(v1, v2);
					
					if (retValue != 0)
						return retValue;
				}
			}
			
			return 0;
		}
	}

	@SuppressWarnings("unchecked")
	private int compareObjects(Object v1, Object v2) {
		return ((Comparable<Object>)v1).compareTo(v2);
	}
}
