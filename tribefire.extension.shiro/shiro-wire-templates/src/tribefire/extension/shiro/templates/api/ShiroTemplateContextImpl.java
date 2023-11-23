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

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.shiro.ShiroConstants;
import com.braintribe.model.shiro.deployment.FieldEncoding;
import com.braintribe.utils.lcd.CollectionTools2;

import tribefire.extension.templates.api.TemplateContextImpl;

public class ShiroTemplateContextImpl extends TemplateContextImpl<ShiroTemplateContext> implements ShiroTemplateContext, ShiroTemplateContextBuilder {

	private static final Logger logger = Logger.getLogger(ShiroTemplateContextImpl.class);

	private boolean googleEnabled = false;
	private String googleClientId = null;
	private String googleSecret = null;

	private boolean azureEnabled = false;
	private String azureClientId = null;
	private String azureSecret = null;
	private String azureTenant = null;

	private boolean twitterEnabled = false;
	private String twitterKey = null;
	private String twitterSecret = null;

	private boolean facebookEnabled = false;
	private String facebookKey = null;
	private String facebookSecret = null;

	private boolean githubEnabled = false;
	private String githubKey = null;
	private String githubSecret = null;

	private boolean cognitoEnabled = false;
	private String cognitoClientId = null;
	private String cognitoSecret = null;
	private String cognitoRegion = null;
	private String cognitoUserPoolId = null;
	private boolean cognitoExclusiveRoleProvider;

	private boolean oktaEnabled = false;
	private String oktaClientId = null;
	private String oktaSecret = null;
	private String oktaDiscoveryUrl = null;
	private boolean oktaExclusiveRoleProvider;
	private String oktaRolesField;
	private FieldEncoding oktaRolesFieldEncoding;

	private boolean instagramEnabled;
	private String instagramClientId;
	private String instagramSecret;
	private String instagramAuthUrl = "https://api.instagram.com/oauth/authorize";
	private String instagramTokenUrl = "https://api.instagram.com/oauth/access_token";
	private String instagramProfileUrl = "https://graph.instagram.com";
	private String instagramUserInformationUrl = "https://www.instagram.com/{username}/?__a=1";
	private String instagramUsernamePattern = "{username}";
	private String instagramScope = "user_profile";

	private List<String> userRolesMapField = CollectionTools2.asList("email");
	private Map<Set<String>, Set<String>> userRolesMap = null;

	private Set<String> acceptList = null;
	private Set<String> blockList = null;

	private boolean createUsers = true;
	private String publicServicesUrl = TribefireRuntime.getPublicServicesUrl();
	private String callbackUrl = null;
	private String unauthorizedUrl = null;
	private String unauthenticatedUrl = null;
	private String redirectUrl = null;
	private String fallbackUrl = null;
	private boolean addSessionParameterOnRedirect = false;

	private String loginDomain = null;
	private Map<String, String> customParameters;
	private boolean showStandardLoginForm = true;
	private boolean showTextLinks = false;

	private boolean obfuscateLogOutput = true;

	@Override
	public boolean getGoogleEnabled() {
		return googleEnabled;
	}
	@Override
	public String getGoogleClientId() {
		return googleClientId;
	}
	@Override
	public String getGoogleSecret() {
		return googleSecret;
	}
	@Override
	public boolean getAzureEnabled() {
		return azureEnabled;
	}
	@Override
	public String getAzureClientId() {
		return azureClientId;
	}
	@Override
	public String getAzureSecret() {
		return azureSecret;
	}
	@Override
	public String getAzureTenant() {
		return azureTenant;
	}
	@Override
	public boolean getTwitterEnabled() {
		return twitterEnabled;
	}
	@Override
	public String getTwitterKey() {
		return twitterKey;
	}
	@Override
	public String getTwitterSecret() {
		return twitterSecret;
	}
	@Override
	public boolean getFacebookEnabled() {
		return facebookEnabled;
	}
	@Override
	public String getFacebookKey() {
		return facebookKey;
	}
	@Override
	public String getFacebookSecret() {
		return facebookSecret;
	}
	@Override
	public boolean getGithubEnabled() {
		return githubEnabled;
	}
	@Override
	public String getGithubKey() {
		return githubKey;
	}
	@Override
	public String getGithubSecret() {
		return githubSecret;
	}
	@Override
	public boolean getCognitoEnabled() {
		return cognitoEnabled;
	}
	@Override
	public String getCognitoClientId() {
		return cognitoClientId;
	}
	@Override
	public String getCognitoSecret() {
		return cognitoSecret;
	}
	@Override
	public String getCognitoRegion() {
		return cognitoRegion;
	}
	@Override
	public String getCognitoUserPoolId() {
		return cognitoUserPoolId;
	}
	@Override
	public ShiroTemplateContextBuilder setCognitoExclusiveRoleProvider(boolean cognitoExclusiveRoleProvider) {
		this.cognitoExclusiveRoleProvider = cognitoExclusiveRoleProvider;
		return this;
	}
	@Override
	public boolean getCognitoExclusiveRoleProvider() {
		return cognitoExclusiveRoleProvider;
	}

	@Override
	public List<String> getUserRolesMapField() {
		return userRolesMapField;
	}
	@Override
	public Map<Set<String>, Set<String>> getUserRolesMap() {
		return userRolesMap;
	}
	@Override
	public Set<String> getAcceptList() {
		return acceptList;
	}
	@Override
	public Set<String> getBlockList() {
		return blockList;
	}
	@Override
	public boolean getCreateUsers() {
		return createUsers;
	}
	@Override
	public String getPublicServicesUrl() {
		return publicServicesUrl;
	}
	@Override
	public String getCallbackUrl() {
		if (callbackUrl == null) {
			String publicServicesUrl = getPublicServicesUrl();
			if (!publicServicesUrl.endsWith("/")) {
				publicServicesUrl = publicServicesUrl + "/";
			}
			callbackUrl = publicServicesUrl + "component/" + ShiroConstants.PATH_IDENTIFIER + "/auth/callback";
		}
		return callbackUrl;
	}
	@Override
	public String getUnauthorizedUrl() {
		if (unauthorizedUrl == null) {
			String publicServicesUrl = getPublicServicesUrl();
			if (!publicServicesUrl.endsWith("/")) {
				publicServicesUrl = publicServicesUrl + "/";
			}
			unauthorizedUrl = publicServicesUrl + "component/" + ShiroConstants.PATH_IDENTIFIER;
		}
		return unauthorizedUrl;
	}
	@Override
	public String getUnauthenticatedUrl() {
		return unauthenticatedUrl;
	}
	@Override
	public String getRedirectUrl() {
		if (redirectUrl == null) {
			redirectUrl = getPublicServicesUrl();
		}
		return redirectUrl;
	}
	@Override
	public String getFallbackUrl() {
		return fallbackUrl;
	}
	@Override
	public boolean getAddSessionParameterOnRedirect() {
		return addSessionParameterOnRedirect;
	}
	@Override
	public String getLoginDomain() {
		return loginDomain;
	}
	@Override
	public Map<String, String> getCustomParameters() {
		return customParameters;
	}
	@Override
	public boolean getShowStandardLoginForm() {
		return showStandardLoginForm;
	}
	@Override
	public boolean getShowTextLinks() {
		return showTextLinks;
	}

	@Override
	public ShiroTemplateContextBuilder setGoogleEnabled(boolean googleEnabled) {
		this.googleEnabled = googleEnabled;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setGoogleClientId(String googleClientId) {
		this.googleClientId = googleClientId;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setGoogleSecret(String googleSecret) {
		this.googleSecret = googleSecret;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setAzureEnabled(boolean azureEnabled) {
		this.azureEnabled = azureEnabled;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setAzureClientId(String azureClientId) {
		this.azureClientId = azureClientId;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setAzureSecret(String azureSecret) {
		this.azureSecret = azureSecret;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setAzureTenant(String azureTenant) {
		this.azureTenant = azureTenant;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setTwitterEnabled(boolean twitterEnabled) {
		this.twitterEnabled = twitterEnabled;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setTwitterKey(String twitterKey) {
		this.twitterKey = twitterKey;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setTwitterSecret(String twitterSecret) {
		this.twitterSecret = twitterSecret;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setFacebookEnabled(boolean facebookEnabled) {
		this.facebookEnabled = facebookEnabled;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setFacebookKey(String facebookKey) {
		this.facebookKey = facebookKey;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setFacebookSecret(String facebookSecret) {
		this.facebookSecret = facebookSecret;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setGithubEnabled(boolean githubEnabled) {
		this.githubEnabled = githubEnabled;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setGithubKey(String githubKey) {
		this.githubKey = githubKey;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setGithubSecret(String githubSecret) {
		this.githubSecret = githubSecret;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setCognitoEnabled(boolean cognitoEnabled) {
		this.cognitoEnabled = cognitoEnabled;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setCognitoClientId(String cognitoClientId) {
		this.cognitoClientId = cognitoClientId;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setCognitoSecret(String cognitoSecret) {
		this.cognitoSecret = cognitoSecret;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setCognitoRegion(String cognitoRegion) {
		this.cognitoRegion = cognitoRegion;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setCognitoUserPoolId(String cognitoUserPoolId) {
		this.cognitoUserPoolId = cognitoUserPoolId;
		return this;
	}

	@Override
	public boolean getOktaEnabled() {
		return oktaEnabled;
	}
	@Override
	public String getOktaClientId() {
		return oktaClientId;
	}
	@Override
	public String getOktaSecret() {
		return oktaSecret;
	}
	@Override
	public String getOktaDiscoveryUrl() {
		return oktaDiscoveryUrl;
	}
	@Override
	public ShiroTemplateContextBuilder setOktaEnabled(boolean oktaEnabled) {
		this.oktaEnabled = oktaEnabled;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setOktaClientId(String oktaClientId) {
		this.oktaClientId = oktaClientId;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setOktaSecret(String oktaSecret) {
		this.oktaSecret = oktaSecret;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setOktaDiscoveryUrl(String oktaDiscoveryUrl) {
		this.oktaDiscoveryUrl = oktaDiscoveryUrl;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setOktaExclusiveRoleProvider(boolean oktaExclusiveRoleProvider) {
		this.oktaExclusiveRoleProvider = oktaExclusiveRoleProvider;
		return this;
	}
	@Override
	public boolean getOktaExclusiveRoleProvider() {
		return oktaExclusiveRoleProvider;
	}
	@Override
	public ShiroTemplateContextBuilder setOktaRolesField(String oktaRolesField) {
		this.oktaRolesField = oktaRolesField;
		return this;
	}
	@Override
	public String getOktaRolesField() {
		return oktaRolesField;
	}
	@Override
	public ShiroTemplateContextBuilder setOktaRolesFieldEncoding(FieldEncoding oktaRolesFieldEncoding) {
		this.oktaRolesFieldEncoding = oktaRolesFieldEncoding;
		return this;
	}
	@Override
	public FieldEncoding getOktaRolesFieldEncoding() {
		return oktaRolesFieldEncoding;
	}

	@Override
	public ShiroTemplateContextBuilder setUserRolesMapField(List<String> userRolesMapField) {
		this.userRolesMapField = userRolesMapField;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setUserRolesMap(Map<Set<String>, Set<String>> userRolesMap) {
		this.userRolesMap = userRolesMap;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setAcceptList(Set<String> acceptList) {
		this.acceptList = acceptList;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setBlockList(Set<String> blockList) {
		this.blockList = blockList;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setCreateUsers(boolean createUsers) {
		this.createUsers = createUsers;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setPublicServicesUrl(String publicServicesUrl) {
		this.publicServicesUrl = publicServicesUrl;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setUnauthorizedUrl(String unauthorizedUrl) {
		this.unauthorizedUrl = unauthorizedUrl;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setUnauthenticatedUrl(String unauthenticatedUrl) {
		this.unauthenticatedUrl = unauthenticatedUrl;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setFallbackUrl(String fallbackUrl) {
		this.fallbackUrl = fallbackUrl;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setAddSessionParameterOnRedirect(boolean addSessionParameterOnRedirect) {
		this.addSessionParameterOnRedirect = addSessionParameterOnRedirect;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setLoginDomain(String loginDomain) {
		this.loginDomain = loginDomain;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setCustomParameters(Map<String, String> customParameters) {
		this.customParameters = customParameters;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setShowStandardLoginForm(boolean showStandardLoginForm) {
		this.showStandardLoginForm = showStandardLoginForm;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setShowTextLinks(boolean showTextLinks) {
		this.showTextLinks = showTextLinks;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setInstagramEnabled(boolean instagramEnabled) {
		this.instagramEnabled = instagramEnabled;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setInstagramClientId(String instagramClientId) {
		this.instagramClientId = instagramClientId;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setInstagramSecret(String instagramSecret) {
		this.instagramSecret = instagramSecret;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setInstagramAuthUrl(String instagramAuthUrl) {
		this.instagramAuthUrl = instagramAuthUrl;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setInstagramTokenUrl(String instagramTokenUrl) {
		this.instagramTokenUrl = instagramTokenUrl;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setInstagramProfileUrl(String instagramProfileUrl) {
		this.instagramProfileUrl = instagramProfileUrl;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setInstagramUsernamePattern(String instagramUsernamePattern) {
		this.instagramUsernamePattern = instagramUsernamePattern;
		return this;
	}
	@Override
	public ShiroTemplateContextBuilder setInstagramScope(String instagramScope) {
		this.instagramScope = instagramScope;
		return this;
	}
	@Override
	public boolean getInstagramEnabled() {
		return this.instagramEnabled;
	}
	@Override
	public String getInstagramClientId() {
		return this.instagramClientId;
	}
	@Override
	public String getInstagramSecret() {
		return this.instagramSecret;
	}
	@Override
	public String getInstagramAuthUrl() {
		return this.instagramAuthUrl;
	}
	@Override
	public String getInstagramTokenUrl() {
		return this.instagramTokenUrl;
	}
	@Override
	public String getInstagramProfileUrl() {
		return this.instagramProfileUrl;
	}
	@Override
	public String getInstagramUsernamePattern() {
		return this.instagramUsernamePattern;
	}
	@Override
	public String getInstagramScope() {
		return this.instagramScope;
	}
	@Override
	public ShiroTemplateContextBuilder setInstagramUserInformationUrl(String instagramUserInformationUrl) {
		this.instagramUserInformationUrl = instagramUserInformationUrl;
		return this;
	}
	@Override
	public String getInstagramUserInformationUrl() {
		return instagramUserInformationUrl;
	}
	@Override
	public ShiroTemplateContextBuilder setObfuscateLogOutput(boolean obfuscateLogOutput) {
		this.obfuscateLogOutput = obfuscateLogOutput;
		return this;
	}
	@Override
	public Boolean getObfuscateLogOutput() {
		return obfuscateLogOutput;
	}

}
