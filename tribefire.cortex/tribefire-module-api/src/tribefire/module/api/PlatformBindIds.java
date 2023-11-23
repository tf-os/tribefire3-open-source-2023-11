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
package tribefire.module.api;

import static com.braintribe.utils.lcd.CollectionTools2.asLinkedSet;

import java.util.Set;

/**
 * Simply a list of constants of known Tribefire Platform bindIds for various system components (which end up being configured on
 * CortexConfiguration).
 * 
 * @author peter.gazdik
 */
public interface PlatformBindIds {

	// Vitals
	String TRIBEFIRE_LOCKING_BIND_ID = "tribefire-locking";
	String TRIBEFIRE_LOCK_DB_BIND_ID = "tribefire-locking-db";
	String TRIBEFIRE_LOCK_MANAGER_BIND_ID = "tribefire-lock-manager";
	String TRIBEFIRE_MQ_BIND_ID = "tribefire-mq";

	// Accesses
	String AUTH_DB_BIND_ID = "tribefire-auth-db";
	String USER_SESSIONS_DB_BIND_ID = "tribefire-user-sessions-db";
	String USER_STATISTICS_DB_BIND_ID = "tribefire-user-statistics-db";
	String TRANSIENT_MESSAGING_DATA_DB_BIND_ID = "tribefire-transient-messaging-data-db";

	// Other
	String RESOURCES_DB = "tribefire-resources-db";

	static boolean isPlatformBindId(String bindId) {
		return platformBindIds().contains(bindId);
	}

	static Set<String> platformBindIds() {
		return asLinkedSet( //
				TRIBEFIRE_LOCKING_BIND_ID, //
				TRIBEFIRE_LOCK_DB_BIND_ID, //
				TRIBEFIRE_LOCK_MANAGER_BIND_ID, //
				TRIBEFIRE_MQ_BIND_ID, //

				AUTH_DB_BIND_ID, //
				USER_SESSIONS_DB_BIND_ID, //
				USER_STATISTICS_DB_BIND_ID, //
				TRANSIENT_MESSAGING_DATA_DB_BIND_ID, //
				
				RESOURCES_DB //
		);
	}

}
