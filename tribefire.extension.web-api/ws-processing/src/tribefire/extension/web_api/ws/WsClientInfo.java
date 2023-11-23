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
package tribefire.extension.web_api.ws;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.usersession.UserSession;

public interface WsClientInfo extends GenericEntity {
	
	EntityType<WsClientInfo> T = EntityTypes.T(WsClientInfo.class);

	void setSessionId (String sessionId);
	String getSessionId();
	
	void setClientId(String clientId);
	String getClientId();
	
	/** Unique identifier for a client's web-socket connection. */
	String getPushChannelId();
	void setPushChannelId(String pushChannelId);

	void setAccept(String accept);
	String getAccept();
	
	void setUserSession(UserSession userSession);
	UserSession getUserSession();
}
