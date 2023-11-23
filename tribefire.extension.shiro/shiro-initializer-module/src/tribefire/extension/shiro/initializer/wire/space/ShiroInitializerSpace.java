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
package tribefire.extension.shiro.initializer.wire.space;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.extensiondeployment.check.CheckBundle;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.shiro.ShiroConstants;
import com.braintribe.model.processing.shiro.utils.ShiroInitializationTools;
import com.braintribe.model.shiro.deployment.Login;
import com.braintribe.model.shiro.deployment.SessionValidator;
import com.braintribe.model.shiro.deployment.ShiroAuthenticationConfiguration;
import com.braintribe.model.shiro.deployment.ShiroBootstrappingWorker;
import com.braintribe.model.shiro.deployment.ShiroServiceProcessor;
import com.braintribe.utils.StringTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.shiro.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.shiro.initializer.wire.contract.ShiroInitializerContract;
import tribefire.extension.shiro.initializer.wire.contract.ShiroRuntimePropertiesContract;
import tribefire.extension.shiro.templates.api.ShiroTemplateContext;
import tribefire.extension.shiro.templates.api.ShiroTemplateContextBuilder;
import tribefire.extension.shiro.templates.wire.contract.ShiroMetaDataContract;
import tribefire.extension.shiro.templates.wire.contract.ShiroTemplatesContract;

/**
 * @see {@link ShiroInitializerContract}
 */
@Managed
public class ShiroInitializerSpace extends AbstractInitializerSpace implements ShiroInitializerContract {

	private static final Logger logger = Logger.getLogger(ShiroInitializerSpace.class);

	@Import
	private ShiroRuntimePropertiesContract properties;

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private ShiroTemplatesContract templates;

	@Import
	private ShiroMetaDataContract metaData;

	@Managed
	protected ShiroTemplateContext defaultContext() {

		String publicServicesUrl = properties.SHIRO_PUBLIC_SERVICES_URL(TribefireRuntime.getPublicServicesUrl());
		if (!publicServicesUrl.endsWith("/")) {
			publicServicesUrl = publicServicesUrl + "/";
		}
		String callbackUrl = properties.SHIRO_CALLBACK_URL(publicServicesUrl + "component/" + ShiroConstants.PATH_IDENTIFIER + "/auth/callback");
		String redirectUrl = properties.SHIRO_REDIRECT_URL(publicServicesUrl);
		String unauthorizedUrl = properties.SHIRO_UNAUTHORIZED_URL(publicServicesUrl + "component/" + ShiroConstants.PATH_IDENTIFIER);

		String acceptListString = properties.SHIRO_LOGIN_ACCEPTLIST();
		String blockListString = properties.SHIRO_LOGIN_BLOCKLIST();

		Map<String, String> customParams = ShiroInitializationTools.decodeMap(properties.SHIRO_LOGIN_CUSTOM_PARAMS());

		//@formatter:off
		ShiroTemplateContextBuilder builder = ShiroTemplateContext.builder()
			.setObfuscateLogOutput(properties.SHIRO_OBFUSCATE_LOG_OUTPUT())
			.setGoogleEnabled(properties.SHIRO_ENABLE_GOOGLE())
			.setFacebookEnabled(properties.SHIRO_ENABLE_FACEBOOK())
			.setTwitterEnabled(properties.SHIRO_ENABLE_TWITTER())
			.setGithubEnabled(properties.SHIRO_ENABLE_GITHUB())
			.setAzureEnabled(properties.SHIRO_ENABLE_AZUREAD())
			.setCognitoEnabled(properties.SHIRO_ENABLE_COGNITO())
			.setOktaEnabled(properties.SHIRO_ENABLE_OKTA())
			.setInstagramEnabled(properties.SHIRO_ENABLE_INSTAGRAM())
			.setPublicServicesUrl(properties.SHIRO_PUBLIC_SERVICES_URL(TribefireRuntime.getPublicServicesUrl()))
			.setCallbackUrl(callbackUrl)
			.setRedirectUrl(redirectUrl)
			.setUnauthorizedUrl(unauthorizedUrl)
			.setUnauthenticatedUrl(properties.SHIRO_UNAUTHENTICATED_URL())
			.setLoginDomain(properties.SHIRO_LOGIN_DOMAIN())
			.setCustomParameters(customParams)
			.setAcceptList(ShiroInitializationTools.parseCollection(acceptListString))
			.setBlockList(ShiroInitializationTools.parseCollection(blockListString))
			.setCreateUsers(properties.SHIRO_LOGIN_CREATEUSERS())
			.setShowStandardLoginForm(properties.SHIRO_SHOW_STANDARD_LOGIN_FORM())
			.setShowTextLinks(properties.SHIRO_SHOW_TEXT_LINKS())
			.setAddSessionParameterOnRedirect(properties.SHIRO_ADD_SESSION_PARAMETER_ON_REDIRECT());
		
		configureGoogle(builder);
		configureFacebook(builder);
		configureTwitter(builder);
		configureGithub(builder);
		configureAzure(builder);
		configureCognito(builder);
		configureOkta(builder);
		configureInstagram(builder);
		
		configureMappedNewUserRoles(builder);
		
		ShiroTemplateContext context = builder
			.setIdPrefix("Shiro.Default")
			.setEntityFactory(super::create)
			.setModule(existingInstances.module())
			.setLookupFunction(super::lookup)
			.setLookupExternalIdFunction(super::lookupExternalId)
			.setName("Default")
			.build();
		//@formatter:on

		return context;

	}

	@Managed
	@Override
	public ShiroAuthenticationConfiguration authenticationConfiguration() {
		ShiroTemplateContext context = defaultContext();
		return templates.authenticationConfiguration(context);
	}

	private void configureGoogle(ShiroTemplateContextBuilder builder) {
		if (properties.SHIRO_ENABLE_GOOGLE()) {
			builder.setGoogleClientId(properties.SHIRO_GOOGLE_CLIENTID_ENCRYPTED());
			builder.setGoogleSecret(properties.SHIRO_GOOGLE_SECRET_ENCRYPTED());
		}
	}

	private void configureAzure(ShiroTemplateContextBuilder builder) {
		if (properties.SHIRO_ENABLE_AZUREAD()) {
			builder.setAzureClientId(properties.SHIRO_AZUREAD_CLIENTID_ENCRYPTED());
			builder.setAzureSecret(properties.SHIRO_AZUREAD_SECRET_ENCRYPTED());
			builder.setAzureTenant(properties.SHIRO_AZUREAD_TENANT());
		}
	}

	private void configureCognito(ShiroTemplateContextBuilder builder) {
		if (properties.SHIRO_ENABLE_COGNITO()) {
			builder.setCognitoClientId(properties.SHIRO_COGNITO_CLIENTID_ENCRYPTED());
			builder.setCognitoSecret(properties.SHIRO_COGNITO_SECRET_ENCRYPTED());
			builder.setCognitoRegion(properties.SHIRO_COGNITO_REGION());
			builder.setCognitoUserPoolId(properties.SHIRO_COGNITO_USERPOOL_ID());
			builder.setCognitoExclusiveRoleProvider(properties.SHIRO_COGNITO_EXCLUSIVE_ROLE_PROVIDER());
		}
	}

	private void configureOkta(ShiroTemplateContextBuilder builder) {
		if (properties.SHIRO_ENABLE_OKTA()) {
			builder.setOktaClientId(properties.SHIRO_OKTA_CLIENTID_ENCRYPTED());
			builder.setOktaSecret(properties.SHIRO_OKTA_SECRET_ENCRYPTED());
			builder.setOktaDiscoveryUrl(properties.SHIRO_OKTA_DISCOVERY_URL());
			builder.setOktaExclusiveRoleProvider(properties.SHIRO_OKTA_EXCLUSIVE_ROLE_PROVIDER());
			builder.setOktaRolesField(properties.SHIRO_OKTA_ROLES_FIELD());
			builder.setOktaRolesFieldEncoding(properties.SHIRO_OKTA_ROLES_FIELD_ENCODING());
		}
	}

	private void configureInstagram(ShiroTemplateContextBuilder builder) {
		if (properties.SHIRO_ENABLE_INSTAGRAM()) {
			builder.setInstagramClientId(properties.SHIRO_INSTAGRAM_CLIENTID_ENCRYPTED());
			builder.setInstagramSecret(properties.SHIRO_INSTAGRAM_SECRET_ENCRYPTED());
		}
	}

	@SuppressWarnings("deprecation")
	private void configureTwitter(ShiroTemplateContextBuilder builder) {
		if (properties.SHIRO_ENABLE_TWITTER()) {
			builder.setTwitterKey(properties.SHIRO_TWITTER_KEY_ENCRYPTED());
			builder.setTwitterSecret(properties.SHIRO_TWITTER_SECRET_ENCRYPTED());
		}
	}

	@SuppressWarnings("deprecation")
	private void configureFacebook(ShiroTemplateContextBuilder builder) {
		if (properties.SHIRO_ENABLE_FACEBOOK()) {
			builder.setFacebookKey(properties.SHIRO_FACEBOOK_KEY_ENCRYPTED());
			builder.setFacebookSecret(properties.SHIRO_FACEBOOK_SECRET_ENCRYPTED());
		}
	}

	@SuppressWarnings("deprecation")
	private void configureGithub(ShiroTemplateContextBuilder builder) {
		if (properties.SHIRO_ENABLE_GITHUB()) {
			builder.setGithubKey(properties.SHIRO_GITHUB_KEY_ENCRYPTED());
			builder.setGithubSecret(properties.SHIRO_GITHUB_SECRET_ENCRYPTED());
		}
	}

	public void configureMappedNewUserRoles(ShiroTemplateContextBuilder builder) {

		builder.setUserRolesMapField(properties.SHIRO_LOGIN_USERROLESMAP_FIELD());

		Map<Set<String>, Set<String>> map = new HashMap<>();

		String listString = properties.SHIRO_LOGIN_USERROLESMAP();
		if (!StringTools.isBlank(listString)) {
			String[] mapEntries = StringTools.splitSemicolonSeparatedString(listString, true);
			for (String mapEntry : mapEntries) {
				int index = mapEntry.indexOf('=');
				if (index != -1) {
					String userSpecString = mapEntry.substring(0, index).trim();
					String rolesSpecString = mapEntry.substring(index + 1).trim();

					Set<String> userSpecsSet = null;
					String[] userSpecs = StringTools.splitCommaSeparatedString(userSpecString, true);
					if (userSpecs != null && userSpecs.length > 0) {
						userSpecsSet = new HashSet<>(Arrays.asList(userSpecs));
					}

					Set<String> rolesSpecsSet = null;
					String[] rolesSpecs = StringTools.splitCommaSeparatedString(rolesSpecString, true);
					if (rolesSpecs != null && rolesSpecs.length > 0) {
						rolesSpecsSet = new HashSet<>(Arrays.asList(rolesSpecs));
					}

					if (userSpecsSet != null && rolesSpecsSet != null) {
						map.put(userSpecsSet, rolesSpecsSet);
					}
				}
			}
		}
		if (!map.isEmpty()) {
			builder.setUserRolesMap(map);
		}

	}

	@SuppressWarnings("deprecation")
	@Managed
	@Override
	public Login login() {
		ShiroTemplateContext context = defaultContext();
		return templates.login(context);
	}

	@Managed
	@Override
	public SessionValidator sessionValidator() {
		ShiroTemplateContext context = defaultContext();
		return templates.sessionValidator(context);
	}

	@Managed
	@Override
	public ShiroBootstrappingWorker bootstrappingWorker() {
		ShiroTemplateContext context = defaultContext();
		return templates.bootstrappingWorker(context);
	}

	@Managed
	@Override
	public ShiroServiceProcessor serviceRequestProcessor() {
		ShiroTemplateContext context = defaultContext();
		return templates.serviceRequestProcessor(context);
	}

	@Override
	public void metaData() {
		ShiroTemplateContext context = defaultContext();
		metaData.metaData(context);
	}

	@Managed
	@Override
	public CheckBundle functionalCheckBundle() {
		ShiroTemplateContext context = defaultContext();
		return templates.functionalCheckBundle(context);
	}
}
