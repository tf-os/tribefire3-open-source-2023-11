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
package com.braintribe.model.service.api;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * InstanceId addresses a certain application on a node in a clustered runtime environment. It can be also used in a
 * wildcarded way using * for either applicationId or nodeId
 *
 */
@ToStringInformation("${applicationId}@${nodeId}")
public interface InstanceId extends GenericEntity {

	EntityType<InstanceId> T = EntityTypes.T(InstanceId.class);

	String applicationId = "applicationId";
	String nodeId = "nodeId";

	String getApplicationId();
	void setApplicationId(String applicationId);

	String getNodeId();
	void setNodeId(String nodeId);

	@Override
	default String stringify() {
		String appId = getApplicationId();
		String nodeId = getNodeId();
		if (appId == null) {
			return nodeId != null ? "<undefined>@".concat(nodeId) : "<undefined>";
		} else if (nodeId == null) {
			return appId.concat("@<undefined>");
		}
		return appId.concat("@").concat(nodeId);
	}

	static InstanceId parse(String stringified) {
		if (stringified == null) {
			return null;
		}
		int index = stringified.indexOf('@');
		if (index <= 0) {
			throw new IllegalArgumentException("Could not find a @ in " + stringified);
		}
		String appId = stringified.substring(0, index);
		if (appId.equals("<undefined>")) {
			appId = null;
		}
		String nodeId = stringified.substring(index + 1);
		if (nodeId.equals("<undefined>")) {
			nodeId = null;
		}
		return of(nodeId, appId);
	}

	static InstanceId of(String nodeId, String appId) {
		InstanceId bean = InstanceId.T.create();
		bean.setNodeId(nodeId);
		bean.setApplicationId(appId);
		return bean;
	}
}
