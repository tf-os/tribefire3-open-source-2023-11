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
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * The addressing information used for a {@link PushRequest}.
 */
@Abstract
public interface PushAddressing extends GenericEntity {

	EntityType<PushAddressing> T = EntityTypes.T(PushAddressing.class);

	/**
	 * ClientId is an identifier that identifies the type of the client application, i.e. all instances of the same client use the same value, e.g.
	 * "tribefire-explorer".
	 */
	void setClientIdPattern(String clientIdPattern);
	String getClientIdPattern();

	/**
	 * SessionId is the identifier for a session of a logged-in user. It is typically a random gibberish so using pattern matching here is
	 * questionable.
	 */
	void setSessionIdPattern(String sessionIdPattern);
	String getSessionIdPattern();

	void setRolePattern(String rolePattern);
	String getRolePattern();

	/**
	 * Unique identifier of the client's connection. In other words, this can be used to specifically target a concrete client, which contrasts with
	 * say sessionId, which is for example shared across all tabs in a browser.
	 */
	String getPushChannelId();
	void setPushChannelId(String pushChannelId);

	default void takeAddressingFrom(PushAddressing other) {
		setClientIdPattern(other.getClientIdPattern());
		setSessionIdPattern(other.getSessionIdPattern());
		setPushChannelId(other.getPushChannelId());
		setRolePattern(other.getRolePattern());
	}
}
