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
package com.braintribe.model.wopi.connector;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Confidential;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Information for BasicAuthentication
 * 
 *
 */
public interface BasicAuthenticationCallbackConfiguration extends GenericEntity {

	final EntityType<BasicAuthenticationCallbackConfiguration> T = EntityTypes.T(BasicAuthenticationCallbackConfiguration.class);

	String user = "user";
	String password = "password";

	@Mandatory
	String getUser();
	void setUser(String user);

	@Confidential
	@Mandatory
	String getPassword();
	void setPassword(String password);

}
