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
package com.braintribe.model.extensiondeployment;

import java.util.Set;

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@Description("Denotes a web terminal that is optionally authorized.")
public interface AuthorizableWebTerminal extends WebTerminal {

	EntityType<AuthorizableWebTerminal> T = EntityTypes.T(AuthorizableWebTerminal.class);
	
	String roles = "roles";
	
	@Description("Configures the roles that are allowed to access the web terminal. If empty any role is accepted.")
	Set<String> getRoles();
	void setRoles(Set<String> roles);
}
