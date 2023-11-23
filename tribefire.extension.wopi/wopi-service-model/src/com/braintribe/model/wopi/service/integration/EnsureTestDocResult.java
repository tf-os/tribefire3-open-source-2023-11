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
package com.braintribe.model.wopi.service.integration;

import java.util.List;

import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.wopi.WopiSession;

/**
 *
 */
public interface EnsureTestDocResult extends WopiResult {

	EntityType<EnsureTestDocResult> T = EntityTypes.T(EnsureTestDocResult.class);

	String wopiSession = "wopiSession";
	String commands = "commands";
	String accessToken = "accessToken";

	@Mandatory
	WopiSession getWopiSession();
	void setWopiSession(WopiSession wopiSession);

	@Mandatory
	List<String> getCommands();
	void setCommands(List<String> commands);

	@Mandatory
	String getAccessToken();
	void setAccessToken(String accessToken);

}