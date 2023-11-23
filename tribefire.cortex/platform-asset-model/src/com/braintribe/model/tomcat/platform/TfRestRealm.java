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
package com.braintribe.model.tomcat.platform;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@Description("Denotes a tomcat realm that uses tribefire authentication as delegate for tomcat authentication.")
public interface TfRestRealm extends TomcatAuthenticationRealm {
	EntityType<TfRestRealm> T = EntityTypes.T(TfRestRealm.class);
	
	@Description("The tribefire-services url of the tribefire to which the authentications of this realm should be delegated.")
	String getTfsUrl();
	void setTfsUrl(String tfsUrl);
	
	@Description("The name of the role in the tribefire delegate that represents the tomcat roles: manager-gui, tomcat, manager-script.")
	@Initializer("'tf-admin'")
	String getFullAccessAlias();
	void setFullAccessAlias(String fullAccessAlias);
}
