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
package tribefire.extension.shiro.templates.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.shiro.deployment.FieldEncoding;

import tribefire.extension.templates.api.TemplateContext;

public interface ShiroTemplateContext extends TemplateContext {

	static ShiroTemplateContextBuilder builder() {
		return new ShiroTemplateContextImpl();
	}

	boolean getGoogleEnabled();
	String getGoogleClientId();
	String getGoogleSecret();

	boolean getAzureEnabled();
	String getAzureClientId();
	String getAzureSecret();
	String getAzureTenant();

	boolean getTwitterEnabled();
	String getTwitterKey();
	String getTwitterSecret();

	boolean getFacebookEnabled();
	String getFacebookKey();
	String getFacebookSecret();

	boolean getGithubEnabled();
	String getGithubKey();
	String getGithubSecret();

	boolean getCognitoEnabled();
	String getCognitoClientId();
	String getCognitoSecret();
	String getCognitoRegion();
	String getCognitoUserPoolId();
	boolean getCognitoExclusiveRoleProvider();

	boolean getOktaEnabled();
	String getOktaClientId();
	String getOktaSecret();
	String getOktaDiscoveryUrl();
	boolean getOktaExclusiveRoleProvider();
	String getOktaRolesField();
	FieldEncoding getOktaRolesFieldEncoding();

	boolean getInstagramEnabled();
	String getInstagramClientId();
	String getInstagramSecret();
	String getInstagramAuthUrl();
	String getInstagramTokenUrl();
	String getInstagramProfileUrl();
	String getInstagramUserInformationUrl();
	String getInstagramUsernamePattern();
	String getInstagramScope();

	List<String> getUserRolesMapField();
	Map<Set<String>, Set<String>> getUserRolesMap();
	Set<String> getAcceptList();
	Set<String> getBlockList();
	boolean getCreateUsers();
	String getPublicServicesUrl();
	String getCallbackUrl();
	String getUnauthorizedUrl();
	String getUnauthenticatedUrl();
	String getFallbackUrl();
	String getRedirectUrl();
	boolean getAddSessionParameterOnRedirect();
	String getLoginDomain();
	Map<String, String> getCustomParameters();
	boolean getShowStandardLoginForm();
	boolean getShowTextLinks();

	Boolean getObfuscateLogOutput();

}