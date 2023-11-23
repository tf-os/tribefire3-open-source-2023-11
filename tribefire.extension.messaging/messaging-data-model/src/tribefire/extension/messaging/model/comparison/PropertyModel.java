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
package tribefire.extension.messaging.model.comparison;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface PropertyModel extends GenericEntity {
    EntityType<PropertyModel> T = EntityTypes.T(PropertyModel.class);
    String property = "property";
    String isIndex = "isIndex";
    String indexType = "indexType";

    String getProperty();
    void setProperty(String property);

    boolean getIsIndex();
    void setIsIndex(boolean index);

    CollectionType getIndexType();
    void setIndexType(CollectionType indexType);

    default String getIndex(){
        return this.getProperty().substring(1, this.getProperty().length()-1);
    }

    static PropertyModel indexProperty(String property, CollectionType type) {
        PropertyModel p = PropertyModel.T.create();
        p.setProperty(property);
        p.setIsIndex(true);
        p.setIndexType(type);
        return p;
    }

    static PropertyModel listIndexProperty(String property) {
        PropertyModel p = PropertyModel.T.create();
        p.setProperty(property);
        p.setIsIndex(true);
        p.setIndexType(CollectionType.MAP);
        return p;
    }

    static PropertyModel mapIndexProperty(String property) {
        PropertyModel p = PropertyModel.T.create();
        p.setProperty(property);
        p.setIsIndex(true);
        p.setIndexType(CollectionType.MAP);
        return p;
    }

    static PropertyModel regularProperty(String property) {
        PropertyModel p = PropertyModel.T.create();
        p.setProperty(property);
        p.setIsIndex(false);
        return p;
    }
}
