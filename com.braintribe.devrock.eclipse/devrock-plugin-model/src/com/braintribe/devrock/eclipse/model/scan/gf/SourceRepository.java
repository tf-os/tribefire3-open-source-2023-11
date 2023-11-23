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
package com.braintribe.devrock.eclipse.model.scan.gf;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface SourceRepository extends GenericEntity {
		
	EntityType<SourceRepository> T = EntityTypes.T(SourceRepository.class);
	
	String name = "name";
	String url = "url";
	String user = "user";
	String password = "password";
	String phonyLocal = "phonyLocal";
	String mavenCompatible = "mavenCompatible";
	

	String getName();
	void setName(String value);
	
	String getUrl();
	void setUrl(String value);

	String getUser();
	void setUser(String value);
	
	String getPassword();
	void setPassword(String value);


	boolean getPhonyLocal();
	void setPhonyLocal(boolean value);


	boolean getMavenCompatible();
	void setMavenCompatible(boolean value);


}
