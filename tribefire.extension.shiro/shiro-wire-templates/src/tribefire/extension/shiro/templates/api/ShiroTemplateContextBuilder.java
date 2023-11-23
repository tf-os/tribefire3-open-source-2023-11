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

import tribefire.extension.templates.api.TemplateContextBuilder;

public interface ShiroTemplateContextBuilder extends TemplateContextBuilder<ShiroTemplateContext> {

	ShiroTemplateContextBuilder setGoogleEnabled(boolean googleEnabled);
	ShiroTemplateContextBuilder setGoogleClientId(String googleClientId);
	ShiroTemplateContextBuilder setGoogleSecret(String googleSecret);

	ShiroTemplateContextBuilder setAzureEnabled(boolean azureEnabled);
	ShiroTemplateContextBuilder setAzureClientId(String azureClientId);
	ShiroTemplateContextBuilder setAzureSecret(String azureSecret);
	ShiroTemplateContextBuilder setAzureTenant(String azureTenant);

	ShiroTemplateContextBuilder setTwitterEnabled(boolean twitterEnabled);
	ShiroTemplateContextBuilder setTwitterKey(String twitterKey);
	ShiroTemplateContextBuilder setTwitterSecret(String twitterSecret);

	ShiroTemplateContextBuilder setFacebookEnabled(boolean facebookEnabled);
	ShiroTemplateContextBuilder setFacebookKey(String facebookKey);
	ShiroTemplateContextBuilder setFacebookSecret(String facebookSecret);

	ShiroTemplateContextBuilder setGithubEnabled(boolean githubEnabled);
	ShiroTemplateContextBuilder setGithubKey(String githubKey);
	ShiroTemplateContextBuilder setGithubSecret(String githubSecret);

	ShiroTemplateContextBuilder setCognitoEnabled(boolean cognitoEnabled);
	ShiroTemplateContextBuilder setCognitoClientId(String cognitoClientId);
	ShiroTemplateContextBuilder setCognitoSecret(String cognitoSecret);
	ShiroTemplateContextBuilder setCognitoRegion(String cognitoRegion);
	ShiroTemplateContextBuilder setCognitoUserPoolId(String cognitoUserPoolId);
	ShiroTemplateContextBuilder setCognitoExclusiveRoleProvider(boolean cognitoExclusiveRoleProvider);

	ShiroTemplateContextBuilder setOktaEnabled(boolean oktaEnabled);
	ShiroTemplateContextBuilder setOktaClientId(String oktaClientId);
	ShiroTemplateContextBuilder setOktaSecret(String oktaSecret);
	ShiroTemplateContextBuilder setOktaDiscoveryUrl(String oktaDiscoveryUrl);
	ShiroTemplateContextBuilder setOktaExclusiveRoleProvider(boolean oktaExclusiveRoleProvider);
	ShiroTemplateContextBuilder setOktaRolesField(String oktaRolesField);
	ShiroTemplateContextBuilder setOktaRolesFieldEncoding(FieldEncoding oktaRolesFieldEncoding);

	ShiroTemplateContextBuilder setInstagramEnabled(boolean instagramEnabled);
	ShiroTemplateContextBuilder setInstagramClientId(String instagramClientId);
	ShiroTemplateContextBuilder setInstagramSecret(String instagramSecret);
	ShiroTemplateContextBuilder setInstagramAuthUrl(String instagramAuthUrl);
	ShiroTemplateContextBuilder setInstagramTokenUrl(String instagramTokenUrl);
	ShiroTemplateContextBuilder setInstagramProfileUrl(String instagramProfileUrl);
	ShiroTemplateContextBuilder setInstagramUserInformationUrl(String instagramUserInformationUrl);
	ShiroTemplateContextBuilder setInstagramUsernamePattern(String instagramUsernamePattern);
	ShiroTemplateContextBuilder setInstagramScope(String instagramScope);

	ShiroTemplateContextBuilder setUserRolesMapField(List<String> userRolesMapField);
	ShiroTemplateContextBuilder setUserRolesMap(Map<Set<String>, Set<String>> userRolesMap);
	ShiroTemplateContextBuilder setAcceptList(Set<String> acceptList);
	ShiroTemplateContextBuilder setBlockList(Set<String> blockList);
	ShiroTemplateContextBuilder setCreateUsers(boolean createUsers);
	ShiroTemplateContextBuilder setPublicServicesUrl(String publicServicesUrl);
	ShiroTemplateContextBuilder setCallbackUrl(String callbackUrl);
	ShiroTemplateContextBuilder setUnauthorizedUrl(String unauthorizedUrl);
	ShiroTemplateContextBuilder setUnauthenticatedUrl(String unauthenticatedUrl);
	ShiroTemplateContextBuilder setRedirectUrl(String redirectUrl);
	ShiroTemplateContextBuilder setFallbackUrl(String fallbackUrl);
	ShiroTemplateContextBuilder setAddSessionParameterOnRedirect(boolean addSessionParameterOnRedirect);
	ShiroTemplateContextBuilder setLoginDomain(String loginDomain);
	ShiroTemplateContextBuilder setCustomParameters(Map<String, String> customParameters);
	ShiroTemplateContextBuilder setShowStandardLoginForm(boolean showStandardLoginForm);
	ShiroTemplateContextBuilder setShowTextLinks(boolean showTextLinks);

	ShiroTemplateContextBuilder setObfuscateLogOutput(boolean obfuscateLogOutput);

}