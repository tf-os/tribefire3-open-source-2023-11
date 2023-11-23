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

public interface ComplexWithCollectionOfSimple extends GenericEntity {
    EntityType<ComplexWithCollectionOfSimple> T = EntityTypes.T(ComplexWithCollectionOfSimple.class);

    String name = "name";
    String listSimple = "listSimple";
    String mapSimple = "mapSimple";
    String setSimple = "setSimple";

    List<Simple> getListSimple();
    void setListSimple(List<Simple> listSimple);

    Map<String,Simple> getMapSimple();
    void setMapSimple(Map<String,Simple> mapSimple);

    Set<Simple> getSetSimple();
    void setSetSimple(Set<Simple> setSimple);
}
