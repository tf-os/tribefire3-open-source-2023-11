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
package com.braintribe.model.deployment.connector.with;

import com.braintribe.model.generic.annotation.Abstract;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Holds account data, i.e. properties {@link #getUser() user} and {@link #getPassword() password}.
 * 
 * @author michael.lafite
 */
@Abstract
public interface WithAccount extends With {

	EntityType<WithAccount> T = EntityTypes.T(WithAccount.class);

	String getUser();
	void setUser(String user);

	String getPassword();
	void setPassword(String password);

}
