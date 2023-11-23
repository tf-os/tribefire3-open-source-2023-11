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
package tribefire.extension.messaging.model.test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import java.util.List;
import java.util.Map;

/**
 * This object is for integration test purposes only!
 */
public interface TestObject extends GenericEntity {
    EntityType<TestObject> T = EntityTypes.T(TestObject.class);

    String name = "name";
    String embeddedObject = "embeddedObject";
    String objectMap = "objectMap";
    String objectList = "objectList";

    List<TestObject> getObjectList();
    void setObjectList(List<TestObject> objectList);

    Map<String,TestObject> getObjectMap();
    void setObjectMap(Map<String,TestObject> objectMap);

    TestObject getEmbeddedObject();
    void setEmbeddedObject(TestObject embeddedObject);

    String getName();
    void setName(String name);
}
