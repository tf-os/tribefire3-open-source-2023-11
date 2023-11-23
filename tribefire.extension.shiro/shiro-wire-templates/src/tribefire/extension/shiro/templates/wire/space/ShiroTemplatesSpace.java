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
package tribefire.extension.shiro.templates.wire.space;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.extensiondeployment.check.CheckBundle;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.shiro.ShiroAuthenticationUrl;
import com.braintribe.model.processing.shiro.ShiroConstants;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.shiro.deployment.FieldEncoding;
import com.braintribe.model.shiro.deployment.FixedNewUserRoleProvider;
import com.braintribe.model.shiro.deployment.HealthCheckProcessor;
import com.braintribe.model.shiro.deployment.Login;
import com.braintribe.model.shiro.deployment.MappedNewUserRoleProvider;
import com.braintribe.model.shiro.deployment.SessionValidator;
import com.braintribe.model.shiro.deployment.ShiroAuthenticationConfiguration;
import com.braintribe.model.shiro.deployment.ShiroBootstrappingWorker;
import com.braintribe.model.shiro.deployment.ShiroClient;
import com.braintribe.model.shiro.deployment.ShiroServiceProcessor;
import com.braintribe.model.shiro.deployment.UserToRolesMapEntry;
import com.braintribe.model.shiro.deployment.client.OAuth20ClientAuthenticationMethod;
import com.braintribe.model.shiro.deployment.client.ShiroAwsCognitoClient;
import com.braintribe.model.shiro.deployment.client.ShiroAzureAdClient;
import com.braintribe.model.shiro.deployment.client.ShiroFacebookClient;
import com.braintribe.model.shiro.deployment.client.ShiroGithubClient;
import com.braintribe.model.shiro.deployment.client.ShiroInstagramOAuth20Client;
import com.braintribe.model.shiro.deployment.client.ShiroOidcGoogleClient;
import com.braintribe.model.shiro.deployment.client.ShiroOktaOpenIdClient;
import com.braintribe.model.shiro.deployment.client.ShiroTwitterClient;
import com.braintribe.utils.StringTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.model.check.CheckCoverage;
import tribefire.cortex.model.check.CheckWeight;
import tribefire.extension.shiro.templates.api.ShiroTemplateContext;
import tribefire.extension.shiro.templates.wire.contract.ShiroTemplatesContract;

@Managed
public class ShiroTemplatesSpace implements WireSpace, ShiroTemplatesContract {

	private static final Logger logger = Logger.getLogger(ShiroTemplatesSpace.class);

	@Import
	private ShiroMetaDataSpace shiroMetaData;

	@Managed
	@Override
	public ShiroAuthenticationConfiguration authenticationConfiguration(ShiroTemplateContext context) {
		ShiroAuthenticationConfiguration bean = context.create(ShiroAuthenticationConfiguration.T, InstanceConfiguration.currentInstance());

		if (context.getGoogleEnabled()) {
			bean.getClients().add(authenticationGoogle(context));
		}
		if (context.getFacebookEnabled()) {
			bean.getClients().add(authenticationFacebook(context));
		}
		if (context.getTwitterEnabled()) {
			bean.getClients().add(authenticationTwitter(context));
		}
		if (context.getGithubEnabled()) {
			bean.getClients().add(authenticationGithub(context));
		}
		if (context.getAzureEnabled()) {
			bean.getClients().add(authenticationAzureAd(context));
		}
		if (context.getCognitoEnabled()) {
			bean.getClients().add(authenticationCognito(context));
		}
		if (context.getOktaEnabled()) {
			bean.getClients().add(authenticationOkta(context));
		}
		if (context.getInstagramEnabled()) {
			bean.getClients().add(authenticationInstagram(context));
		}

		bean.setDefaultRedirectUrl(context.getPublicServicesUrl());
		bean.setCallbackUrl(context.getCallbackUrl());
		bean.setDefaultRedirectUrl(context.getRedirectUrl());
		bean.setUnauthorizedUrl(context.getUnauthorizedUrl());
		bean.setUnauthenticatedUrl(context.getUnauthenticatedUrl());
		bean.setFallbackUrl(context.getFallbackUrl());

		return bean;
	}

	@Managed
	private ShiroOidcGoogleClient authenticationGoogle(ShiroTemplateContext context) {
		ShiroOidcGoogleClient bean = context.create(ShiroOidcGoogleClient.T, InstanceConfiguration.currentInstance());
		bean.setName("Google");

		bean.setClientId(context.getGoogleClientId());
		bean.setSecret(context.getGoogleSecret());
		bean.setUsernamePatterns(asList("{email}"));
		bean.setUserIconUrl("{picture}");
		bean.setUserDescriptionPattern("{name}");
		bean.setUserMailField("{email}");
		bean.setFirstNamePattern("{given_name}");
		bean.setLastNamePattern("{family_name}");
		bean.setUseNonce(true);
		bean.setActive(true);

		Map<String, String> customParams = new HashMap<>();
		String loginDomain = context.getLoginDomain();
		if (!StringTools.isBlank(loginDomain)) {
			customParams.put("hd", loginDomain.trim());
		}
		Map<String, String> otherCustomParams = context.getCustomParameters();
		if (otherCustomParams != null) {
			customParams.putAll(otherCustomParams);
		}
		if (!customParams.isEmpty()) {
			bean.setCustomParams(customParams);
		}
		return bean;
	}

	@Managed
	private ShiroAzureAdClient authenticationAzureAd(ShiroTemplateContext context) {
		ShiroAzureAdClient bean = context.create(ShiroAzureAdClient.T, InstanceConfiguration.currentInstance());
		bean.setName("AzureAd");

		bean.setClientId(context.getAzureClientId());
		bean.setSecret(context.getAzureSecret());
		bean.setUsernamePatterns(asList("{email}"));
		bean.setUserDescriptionPattern("{name}");
		bean.setUserMailField("{email}");
		bean.setFirstNamePattern("{given_name}");
		bean.setLastNamePattern("{family_name}");
		bean.setUseNonce(true);
		bean.setActive(true);
		bean.setTenant(context.getAzureTenant());

		Map<String, String> customParams = new HashMap<>();
		String loginDomain = context.getLoginDomain();
		if (!StringTools.isBlank(loginDomain)) {
			customParams.put("domain_hint", loginDomain.trim());
		}
		Map<String, String> otherCustomParams = context.getCustomParameters();
		if (otherCustomParams != null) {
			customParams.putAll(otherCustomParams);
		}
		if (!customParams.isEmpty()) {
			bean.setCustomParams(customParams);
		}
		return bean;
	}

	@Managed
	private ShiroAwsCognitoClient authenticationCognito(ShiroTemplateContext context) {
		ShiroAwsCognitoClient bean = context.create(ShiroAwsCognitoClient.T, InstanceConfiguration.currentInstance());
		bean.setName("Cognito");

		bean.setClientId(context.getCognitoClientId());
		bean.setSecret(context.getCognitoSecret());
		bean.setUsernamePatterns(asList("{email}"));
		bean.setUserDescriptionPattern("{username}");
		bean.setUserMailField("{email}");
		bean.setUseNonce(true);
		bean.setActive(true);
		bean.setRegion(context.getCognitoRegion());
		bean.setUserPoolId(context.getCognitoUserPoolId());
		bean.setRolesField("{cognito:roles}");
		bean.setRolesFieldEncoding(FieldEncoding.JSON);
		bean.setExclusiveRoleProvider(context.getCognitoExclusiveRoleProvider());

		Map<String, String> customParams = new HashMap<>();
		String loginDomain = context.getLoginDomain();
		if (!StringTools.isBlank(loginDomain)) {
			customParams.put("domain_hint", loginDomain.trim());
		}
		Map<String, String> otherCustomParams = context.getCustomParameters();
		if (otherCustomParams != null) {
			customParams.putAll(otherCustomParams);
		}
		if (!customParams.isEmpty()) {
			bean.setCustomParams(customParams);
		}
		return bean;
	}

	@Managed
	private ShiroOktaOpenIdClient authenticationOkta(ShiroTemplateContext context) {
		ShiroOktaOpenIdClient bean = context.create(ShiroOktaOpenIdClient.T, InstanceConfiguration.currentInstance());
		bean.setName("Okta");

		bean.setClientId(context.getOktaClientId());
		bean.setSecret(context.getOktaSecret());
		bean.setDiscoveryUri(context.getOktaDiscoveryUrl());
		bean.setUsernamePatterns(asList("{preferred_username}", "{email}"));
		bean.setUserDescriptionPattern("{name}");
		bean.setUserMailField("{email}");
		bean.setFirstNamePattern("{given_name}");
		bean.setLastNamePattern("{family_name}");
		bean.setUseNonce(true);
		bean.setActive(true);
		bean.setExclusiveRoleProvider(context.getOktaExclusiveRoleProvider());
		bean.setRolesField(context.getOktaRolesField());
		bean.setRolesFieldEncoding(context.getOktaRolesFieldEncoding());

		Map<String, String> customParams = new HashMap<>();
		Map<String, String> otherCustomParams = context.getCustomParameters();
		if (otherCustomParams != null) {
			customParams.putAll(otherCustomParams);
		}
		if (!customParams.isEmpty()) {
			bean.setCustomParams(customParams);
		}
		return bean;
	}

	@Managed
	private ShiroInstagramOAuth20Client authenticationInstagram(ShiroTemplateContext context) {
		ShiroInstagramOAuth20Client bean = context.create(ShiroInstagramOAuth20Client.T, InstanceConfiguration.currentInstance());
		bean.setName("Instagram");

		bean.setKey(context.getInstagramClientId());
		bean.setSecret(context.getInstagramSecret());
		bean.setAuthUrl(context.getInstagramAuthUrl());
		bean.setTokenUrl(context.getInstagramTokenUrl());
		bean.setProfileUrl(context.getInstagramProfileUrl());
		bean.setUserInformationUrl(context.getInstagramUserInformationUrl());
		bean.setUsernamePatterns(asList(context.getInstagramUsernamePattern()));
		bean.setClientAuthenticationMethod(OAuth20ClientAuthenticationMethod.requestBody);
		bean.setUsePathUrlResolver(true);
		bean.setActive(true);

		Map<String, String> customParams = new HashMap<>();
		String instagramScope = context.getInstagramScope();
		if (!StringTools.isBlank(instagramScope)) {
			customParams.put("scope", instagramScope);
		}

		Map<String, String> otherCustomParams = context.getCustomParameters();
		if (otherCustomParams != null) {
			customParams.putAll(otherCustomParams);
		}
		if (!customParams.isEmpty()) {
			bean.setCustomParams(customParams);
		}
		return bean;
	}

	@Managed
	private ShiroTwitterClient authenticationTwitter(ShiroTemplateContext context) {
		ShiroTwitterClient bean = context.create(ShiroTwitterClient.T, InstanceConfiguration.currentInstance());
		bean.setName("Twitter");
		bean.setKey(context.getTwitterKey());
		bean.setSecret(context.getTwitterSecret());
		bean.setUsernamePatterns(asList("@{screen_name}"));
		bean.setUserIconUrl("{profile_image_url_https}");
		bean.setUserDescriptionPattern("{name}");
		bean.setActive(true);
		return bean;
	}

	@Managed
	private ShiroFacebookClient authenticationFacebook(ShiroTemplateContext context) {
		ShiroFacebookClient bean = context.create(ShiroFacebookClient.T, InstanceConfiguration.currentInstance());
		bean.setName("Facebook");
		bean.setKey(context.getFacebookKey());
		bean.setSecret(context.getFacebookSecret());
		bean.setUsernamePatterns(asList("{email}"));
		bean.setUserDescriptionPattern("{name}");
		bean.setUserMailField("{email}");
		bean.setFirstNamePattern("{first_name}");
		bean.setLastNamePattern("{last_name}");
		bean.setScope("public_profile,email");
		bean.setUserIconUrl("https://graph.facebook.com/{id}/picture?type=large");
		bean.setActive(true);
		return bean;
	}

	@Managed
	private ShiroGithubClient authenticationGithub(ShiroTemplateContext context) {
		ShiroGithubClient bean = context.create(ShiroGithubClient.T, InstanceConfiguration.currentInstance());
		bean.setName("Github");
		bean.setKey(context.getGithubKey());
		bean.setSecret(context.getGithubSecret());
		bean.setScope("user, user:email");
		bean.setUsernamePatterns(asList("{email}", "{login}@github.com"));
		bean.setUserIconUrl("{avatar_url}");
		bean.setUserDescriptionPattern("{login}");
		bean.setActive(true);
		return bean;
	}

	@Managed
	private FixedNewUserRoleProvider fixedNewUserRoleProvider(ShiroTemplateContext context) {
		FixedNewUserRoleProvider bean = context.create(FixedNewUserRoleProvider.T, InstanceConfiguration.currentInstance());
		bean.setName("Fixed Roles for New Users Provider");
		bean.setModule(context.getModule());
		bean.getRoles().add("tf-admin");
		return bean;
	}

	@Managed
	private MappedNewUserRoleProvider mappedNewUserRoleProvider(ShiroTemplateContext context) {
		MappedNewUserRoleProvider bean = context.create(MappedNewUserRoleProvider.T, InstanceConfiguration.currentInstance());
		bean.setName("Mapped Roles for New Users Provider");
		bean.setModule(context.getModule());
		bean.setFields(context.getUserRolesMapField());

		Map<Set<String>, Set<String>> userRolesMap = context.getUserRolesMap();
		if (userRolesMap != null && !userRolesMap.isEmpty()) {
			for (Map.Entry<Set<String>, Set<String>> entry : userRolesMap.entrySet()) {
				Set<String> userSpecsSet = entry.getKey();
				Set<String> rolesSpecsSet = entry.getValue();

				if (userSpecsSet != null && !userSpecsSet.isEmpty() && rolesSpecsSet != null && !rolesSpecsSet.isEmpty()) {
					UserToRolesMapEntry e = userToRolesMapEntry(context);
					e.setUsernameSpecifications(userSpecsSet);
					e.setRoles(rolesSpecsSet);
					bean.getMapping().add(e);
				}
			}
		}

		return bean;
	}

	@Managed
	private UserToRolesMapEntry userToRolesMapEntry(ShiroTemplateContext context) {
		UserToRolesMapEntry bean = context.create(UserToRolesMapEntry.T, InstanceConfiguration.currentInstance());
		return bean;
	}

	@Managed
	@Override
	public Login login(ShiroTemplateContext context) {
		Login bean = context.create(Login.T, InstanceConfiguration.currentInstance());
		bean.setExternalId(ShiroConstants.SHIRO_LOGIN_EXTERNALID);
		bean.setName("Remote Login Terminal");
		bean.setModule(context.getModule());
		bean.setPathIdentifier(ShiroConstants.PATH_IDENTIFIER);
		bean.setConfiguration(authenticationConfiguration(context));
		if (context.getUserRolesMap() != null) {
			bean.setNewUserRoleProvider(mappedNewUserRoleProvider(context));
		} else {
			bean.setNewUserRoleProvider(fixedNewUserRoleProvider(context));
		}
		bean.setUserAcceptList(context.getAcceptList());
		bean.setUserBlockList(context.getBlockList());

		bean.setCreateUsers(context.getCreateUsers());

		bean.setShowStandardLoginForm(context.getShowStandardLoginForm());
		bean.setShowTextLinks(context.getShowTextLinks());
		bean.setAddSessionParameterOnRedirect(context.getAddSessionParameterOnRedirect());
		bean.setObfuscateLogOutput(context.getObfuscateLogOutput());
		return bean;
	}

	@Managed
	@Override
	public SessionValidator sessionValidator(ShiroTemplateContext context) {
		SessionValidator bean = context.create(SessionValidator.T, InstanceConfiguration.currentInstance());
		bean.setName("Session Validator Terminal");
		bean.setModule(context.getModule());
		bean.setPathIdentifier("session-validator");
		return bean;
	}

	@Managed
	@Override
	public ShiroBootstrappingWorker bootstrappingWorker(ShiroTemplateContext context) {
		ShiroBootstrappingWorker bean = context.create(ShiroBootstrappingWorker.T, InstanceConfiguration.currentInstance());
		bean.setName("Shiro Bootstrapping Worker");
		bean.setModule(context.getModule());
		bean.setConfiguration(authenticationConfiguration(context));
		bean.setLogin(login(context));
		bean.setAutoDeploy(true);
		return bean;
	}

	@Managed
	@Override
	public ShiroServiceProcessor serviceRequestProcessor(ShiroTemplateContext context) {

		ShiroServiceProcessor bean = context.create(ShiroServiceProcessor.T, InstanceConfiguration.currentInstance());
		bean.setName("Shiro Service Processor");
		bean.setModule(context.getModule());
		bean.setConfiguration(authenticationConfiguration(context));
		bean.setPathIdentifier(ShiroConstants.PATH_IDENTIFIER);

		return bean;
	}

	@Managed
	@Override
	public CheckBundle functionalCheckBundle(ShiroTemplateContext context) {

		CheckBundle bean = context.create(CheckBundle.T, InstanceConfiguration.currentInstance());
		bean.setModule(context.getModule());
		bean.getChecks().add(healthCheckProcessor(context));
		bean.setName("Shiro Checks");
		bean.setWeight(CheckWeight.under1s);
		bean.setCoverage(CheckCoverage.functional);
		bean.setIsPlatformRelevant(false);

		return bean;
	}

	@Managed
	private HealthCheckProcessor healthCheckProcessor(ShiroTemplateContext context) {
		HealthCheckProcessor bean = context.create(HealthCheckProcessor.T, InstanceConfiguration.currentInstance());
		bean.setModule(context.getModule());
		bean.setName("Shiro Check Processor");
		return bean;
	}

	@Override
	public List<ShiroAuthenticationUrl> getAuthenticationUrls(ShiroTemplateContext context) {
		String tfs = TribefireRuntime.getPublicServicesUrl();
		if (!tfs.endsWith("/")) {
			tfs = tfs + "/";
		}
		List<ShiroAuthenticationUrl> result = new ArrayList<>();
		ShiroAuthenticationConfiguration config = authenticationConfiguration(context);
		for (ShiroClient client : config.getClients()) {
			String clientName = client.getName();
			String authUrl = tfs + "component/" + ShiroConstants.PATH_IDENTIFIER + "/auth/" + clientName.toLowerCase();
			String imageUrl = tfs + ShiroConstants.STATIC_IMAGES_RELATIVE_PATH + clientName.toLowerCase() + ".png";
			ShiroAuthenticationUrl ctx = new ShiroAuthenticationUrl(authUrl, clientName, imageUrl);

			Resource loginIcon = client.getLoginIcon();
			if (loginIcon != null) {
				ctx.setIconResourceId(loginIcon.getId());
			}

			result.add(ctx);
		}
		return result;
	}
}
