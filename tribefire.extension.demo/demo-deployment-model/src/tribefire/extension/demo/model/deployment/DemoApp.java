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
package tribefire.extension.demo.model.deployment;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.extensiondeployment.WebTerminal;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface DemoApp extends WebTerminal {

	EntityType<DemoApp> T = EntityTypes.T(DemoApp.class);

	/*
	 * Constants for each property name.
	 */
	String access= "access";
	String user = "user";
	String password = "password";
	
	IncrementalAccess getAccess();
	void setAccess(IncrementalAccess access);

	String getUser();
	void setUser(String user);

	String getPassword();
	void setPassword(String password);

}
