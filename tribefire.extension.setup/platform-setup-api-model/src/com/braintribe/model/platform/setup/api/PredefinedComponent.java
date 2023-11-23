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
package com.braintribe.model.platform.setup.api;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

public enum PredefinedComponent implements EnumBase {
	USER_SESSIONS_DB("tribefire-user-sessions-db"),
    USER_SESSION_STATISTICS_DB("tribefire-user-statistics-db"),
    AUTH_DB("tribefire-auth-db"),
    LOCKING_DB("tribefire-locking-db"),
    LEADERSHIP_DB("tribefire-leadership-db"),
    TRANSIENT_MESSAGING_DATA_DB("tribefire-transient-messaging-data-db"),
    DCSA_SHARED_STORAGE("tribefire-dcsa-shared-storage"),
	DEFAULT_DB("tribefire-default-db"),
	MQ("tribefire-mq"),
	ADMIN_USER("tribefire-admin-user");
    
	public static final EnumType T = EnumTypes.T(PredefinedComponent.class);
	
	private PredefinedComponent(String bindId) {
		this.bindId = bindId;
	}

	private String bindId;

	@Override
	public EnumType type() {
		return T;
	}
	
	public String getBindId() {
		return bindId;
	}
}
