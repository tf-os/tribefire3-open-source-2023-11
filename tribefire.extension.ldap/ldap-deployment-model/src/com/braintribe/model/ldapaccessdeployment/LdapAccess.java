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
package com.braintribe.model.ldapaccessdeployment;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.ldapconnectiondeployment.LdapConnection;


public interface LdapAccess extends IncrementalAccess {

	final EntityType<LdapAccess> T = EntityTypes.T(LdapAccess.class);
	
	void setBase(String base);
	@Initializer("'OU=<Base>,OU=<Organization>,DC=<Company>'")
	String getBase();
	
	void setLdapConnection(LdapConnection ldapConnection);
	LdapConnection getLdapConnection();
	
	void setSearchPageSize(int searchPageSize);
	@Initializer("20")
	int getSearchPageSize();

}
