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
package tribefire.extension.messaging.test.comparison.model;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SimpleWithCollectionOfPrimitives extends GenericEntity {
    EntityType<SimpleWithCollectionOfPrimitives> T = EntityTypes.T(SimpleWithCollectionOfPrimitives.class);

    String name = "name";
    String listPrimitive = "listPrimitive";
    String mapPrimitive = "mapPrimitive";
    String setPrimitive = "setPrimitive";

    String getName();
    void setName(String name);

    List<Integer> getListPrimitive();
    void setListPrimitive(List<Integer> listComplex);

    Map<String,Integer> getMapPrimitive();
    void setMapPrimitive(Map<String,Integer> mapPrimitive);

    Set<Integer> getSetPrimitive();
    void setSetPrimitive(Set<Integer> setPrimitive);
}
