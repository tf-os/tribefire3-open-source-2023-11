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
package com.braintribe.model.shiro.deployment;

import java.util.Map;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;

@Abstract
public interface ShiroClient extends UserInformation {

	final EntityType<ShiroClient> T = EntityTypes.T(ShiroClient.class);

	void setActive(Boolean active);
	Boolean getActive();

	void setName(String name);
	String getName();

	void setAdditionalIniParameters(Map<String, String> additionalIniParameters);
	Map<String, String> getAdditionalIniParameters();

	Resource getLoginIcon();
	void setLoginIcon(Resource loginIcon);

}
