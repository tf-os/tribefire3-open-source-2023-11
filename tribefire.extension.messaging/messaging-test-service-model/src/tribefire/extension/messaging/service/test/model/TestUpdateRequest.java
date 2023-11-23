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
package tribefire.extension.messaging.service.test.model;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import tribefire.extension.messaging.model.service.MessagingRequest;

public interface TestUpdateRequest extends MessagingRequest {
    EntityType<TestUpdateRequest> T = EntityTypes.T(TestUpdateRequest.class);

    String relatedObjId = "relatedObjId";
    String newName = "newName";

    String getRelatedObjId();
    void setRelatedObjId(String relatedObjId);

    String getNewName();
    void setNewName(String newName);

    static TestUpdateRequest buildRequest(String relatedObjId, String newName){
        TestUpdateRequest request = TestUpdateRequest.T.create();
        request.setRelatedObjId(relatedObjId);
        request.setNewName(newName);
        return request;
    }

}
