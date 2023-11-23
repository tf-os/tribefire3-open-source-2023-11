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

import java.util.Set;

import com.braintribe.model.extensiondeployment.WebTerminal;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface Login extends WebTerminal {

	final EntityType<Login> T = EntityTypes.T(Login.class);

	@Initializer("false")
	Boolean getCreateUsers();
	void setCreateUsers(Boolean createUsers);

	NewUserRoleProvider getNewUserRoleProvider();
	void setNewUserRoleProvider(NewUserRoleProvider newUserRoleProvider);

	ShiroAuthenticationConfiguration getConfiguration();
	void setConfiguration(ShiroAuthenticationConfiguration configuration);

	Set<String> getUserAcceptList();
	void setUserAcceptList(Set<String> userAcceptList);

	Set<String> getUserBlockList();
	void setUserBlockList(Set<String> userBlockList);

	/**
	 * @deprecated Use {@link #getUserAcceptList()} instead.
	 */
	@Deprecated
	Set<String> getUserWhiteList();
	/**
	 * @deprecated Use {@link #setUserAcceptList(Set)} instead.
	 */
	@Deprecated
	void setUserWhiteList(Set<String> userWhiteList);

	/**
	 * @deprecated Use {@link #getUserBlockList()} instead.
	 */
	@Deprecated
	Set<String> getUserBlackList();
	/**
	 * @deprecated Use {@link #setUserBlockList(Set)} instead.
	 */
	@Deprecated
	void setUserBlackList(Set<String> userBlackList);

	void setShowStandardLoginForm(boolean showStandardLoginForm);
	boolean getShowStandardLoginForm();

	void setShowTextLinks(boolean showTextLinks);
	boolean getShowTextLinks();

	void setAddSessionParameterOnRedirect(boolean addSessionParameterOnRedirect);
	boolean getAddSessionParameterOnRedirect();

	@Initializer("false")
	Boolean getObfuscateLogOutput();
	void setObfuscateLogOutput(Boolean obfuscateLogOutput);
}
