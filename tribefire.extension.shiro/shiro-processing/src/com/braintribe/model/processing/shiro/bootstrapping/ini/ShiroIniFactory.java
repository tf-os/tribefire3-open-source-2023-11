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
package com.braintribe.model.processing.shiro.bootstrapping.ini;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.GenericOAuth20Client;
import org.pac4j.oauth.client.GitHubClient;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.oidc.client.AzureAdClient;
import org.pac4j.oidc.client.GoogleOidcClient;
import org.pac4j.oidc.client.OidcClient;

import com.braintribe.logging.Logger;
import com.braintribe.model.shiro.deployment.ShiroAuthenticationConfiguration;
import com.braintribe.model.shiro.deployment.ShiroClient;
import com.braintribe.model.shiro.deployment.client.OAuth20ClientAuthenticationMethod;
import com.braintribe.model.shiro.deployment.client.ShiroAwsCognitoClient;
import com.braintribe.model.shiro.deployment.client.ShiroAzureAdClient;
import com.braintribe.model.shiro.deployment.client.ShiroFacebookClient;
import com.braintribe.model.shiro.deployment.client.ShiroGenericOAuth20Client;
import com.braintribe.model.shiro.deployment.client.ShiroGithubClient;
import com.braintribe.model.shiro.deployment.client.ShiroOAuth20Client;
import com.braintribe.model.shiro.deployment.client.ShiroOidcGoogleClient;
import com.braintribe.model.shiro.deployment.client.ShiroOpenIdClient;
import com.braintribe.model.shiro.deployment.client.ShiroOpenIdMetaData;
import com.braintribe.model.shiro.deployment.client.ShiroScope;
import com.braintribe.model.shiro.deployment.client.ShiroTwitterClient;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.CollectionTools2;
import com.braintribe.utils.velocity.VelocityTools;

/**
 * This class is responsible for creating the Shiro Web INI file based on the configuration.
 */
public class ShiroIniFactory implements Supplier<String> {

	private final static Logger logger = Logger.getLogger(ShiroIniFactory.class);

	private String iniTemplate;
	private VelocityEngine velocityEngine;
	private ShiroAuthenticationConfiguration configuration;
	private String loginServletPath;

	public ShiroIniFactory() {
		velocityEngine = VelocityTools.newVelocityEngine();
	}

	@Override
	public String get() {
		if (iniTemplate == null || configuration == null) {
			return null;
		}

		VelocityContext context = new VelocityContext();

		List<String> additionalCallbackUrlPostfixes = new ArrayList<>();

		List<AuthClient> authClients = new ArrayList<>();
		for (ShiroClient client : configuration.getClients()) {

			if (client.getActive() == null || !client.getActive()) {
				continue;
			}

			String name = client.getName();
			AuthClient authClient = new AuthClient();

			authClient.setClient(client);

			if (client instanceof ShiroOpenIdClient) {

				ShiroOpenIdClient soidc = (ShiroOpenIdClient) client;

				applyMetaData(authClient, name, soidc.getMetaData());

				if (client instanceof ShiroAzureAdClient) {

					ShiroAzureAdClient azureAdClient = (ShiroAzureAdClient) client;

					addConfiguration(authClient, "oidcConfig_" + name, "org.pac4j.oidc.config.AzureAdOidcConfiguration");
					addConfiguration(authClient, "oidcConfig_" + name + ".tenant", azureAdClient.getTenant());
					additionalCallbackUrlPostfixes.add("/AzureAd");

				} else {
					addConfiguration(authClient, "oidcConfig_" + name, "org.pac4j.oidc.config.OidcConfiguration");
				}

				if (client instanceof ShiroAwsCognitoClient) {
					ShiroAwsCognitoClient cc = (ShiroAwsCognitoClient) client;
					if (StringTools.isBlank(soidc.getDiscoveryUri())) {
						String region = cc.getRegion();
						String poolId = cc.getUserPoolId();
						soidc.setDiscoveryUri("https://cognito-idp." + region + ".amazonaws.com/" + poolId + "/.well-known/openid-configuration");
					}
				}
				if (soidc.getMetaData() != null) {
					addConfiguration(authClient, "oidcConfig_" + name + ".providerMetadata", "$oidcConfig_md_" + name);
				}

				addConfiguration(authClient, "oidcConfig_" + name + ".clientId", soidc.getClientId());
				addConfiguration(authClient, "oidcConfig_" + name + ".secret", soidc.getSecret());
				addConfiguration(authClient, "oidcConfig_" + name + ".useNonce", "" + soidc.getUseNonce());
				addConfiguration(authClient, "oidcConfig_" + name + ".discoveryURI", soidc.getDiscoveryUri());
				addConfigurationMap(authClient, "oidcConfig_" + name + ".customParams", soidc.getCustomParams());

				if (client instanceof ShiroScope) {
					ShiroScope sc = (ShiroScope) client;
					String scope = sc.getScope();
					if (!StringTools.isBlank(scope)) {
						addConfiguration(authClient, "oidcConfig_" + name + ".scope", scope);
					}
				}

				Class<?> clientClass = resolveImplementationClass(client);
				if (clientClass != null) {
					addConfiguration(authClient, name, clientClass.getName());
					addConfiguration(authClient, name + ".configuration", "$oidcConfig_" + name);
					addConfiguration(authClient, name + ".name", name);
				}

			} else if (client instanceof ShiroOAuth20Client) {

				ShiroOAuth20Client oauthClient = (ShiroOAuth20Client) client;

				Class<?> clientClass = resolveImplementationClass(client);

				if (clientClass != null) {
					addConfiguration(authClient, name, clientClass.getName());
					addConfiguration(authClient, name + ".name", name);
					addConfiguration(authClient, name + ".key", oauthClient.getKey());
					addConfiguration(authClient, name + ".secret", oauthClient.getSecret());
				}

				if (oauthClient instanceof ShiroGenericOAuth20Client) {

					ShiroGenericOAuth20Client goauthClient = (ShiroGenericOAuth20Client) oauthClient;

					addConfiguration(authClient, name + ".authUrl", goauthClient.getAuthUrl());
					addConfiguration(authClient, name + ".tokenUrl", goauthClient.getTokenUrl());
					addConfiguration(authClient, name + ".profileUrl", goauthClient.getProfileUrl());
					addConfiguration(authClient, name + ".profileNodePath", goauthClient.getProfileNodePath());

					addConfigurationMap(authClient, name + ".profileAttrs", goauthClient.getProfileAttrs());

					Boolean usePathUrlResolver = goauthClient.getUsePathUrlResolver();
					if (usePathUrlResolver != null && usePathUrlResolver) {
						addConfiguration(authClient, name + ".callbackUrlResolver", "$pathUrlResolver");
						additionalCallbackUrlPostfixes.add("/" + name);
					}
					OAuth20ClientAuthenticationMethod authenticationMethod = goauthClient.getClientAuthenticationMethod();
					if (authenticationMethod != null) {
						addConfiguration(authClient, name + ".clientAuthenticationMethod", authenticationMethod.name());
					}

					// Enums are not supported
				}

				if (client instanceof ShiroScope) {
					ShiroScope sc = (ShiroScope) client;
					String scope = sc.getScope();
					if (!StringTools.isBlank(scope)) {
						addConfiguration(authClient, name + ".scope", scope);
					}
				}

				addConfigurationMap(authClient, name + ".customParams", oauthClient.getCustomParams());

			} else {
				logger.error("Unsupported client: " + client);
			}

			Map<String, String> additionalIniParameters = client.getAdditionalIniParameters();
			if (additionalIniParameters != null && !additionalIniParameters.isEmpty()) {
				for (Map.Entry<String, String> entry : additionalIniParameters.entrySet()) {
					addConfiguration(authClient, name + "." + entry.getKey(), entry.getValue());
				}
			}

			authClient.getFilters().put(name + "_Filter", "io.buji.pac4j.filter.SecurityFilter");
			authClient.getFilters().put(name + "_Filter.config", "$config");
			authClient.getFilters().put(name + "_Filter.clients", name);

			authClient.setUrlPart("/component/" + loginServletPath + "/auth/" + client.getName().toLowerCase());

			authClients.add(authClient);
		}

		String callbackUrl = configuration.getCallbackUrl();
		if (StringTools.isBlank(callbackUrl)) {
			callbackUrl = "http://localhost:8080/tribefire-services/component/" + loginServletPath + "/auth/callback";
		}

		context.put("authClients", authClients);
		context.put("configuration", configuration);
		context.put("callbackUrl", callbackUrl);
		context.put("loginServletPath", loginServletPath);
		context.put("unauthorizedUrl", configuration.getUnauthorizedUrl());
		context.put("additionalCallbackUrlPostfixes", additionalCallbackUrlPostfixes);

		final String iniContent = VelocityTools.evaluate(velocityEngine, context, iniTemplate);
		logger.debug(() -> "Using ini: \n" + StringTools.asciiBoxMessage(obfuscateSecrets(iniContent), -1));
		return iniContent;
	}

	protected static String obfuscateSecrets(String text) {
		Set<String> keywords = CollectionTools2.asSet("secret", "clientid", "key");
		List<String> lines = StringTools.getLines(text);
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			String lc = line.toLowerCase();
			boolean added = false;
			for (String keyword : keywords) {
				int idx = lc.indexOf(keyword + " = ");
				if (idx > 0) {
					int len = keyword.length() + 3;
					String secret = line.substring(idx + len);
					sb.append(line.substring(0, idx + len) + StringTools.simpleObfuscatePassword(secret));
					added = true;
					break;
				}
			}
			if (!added) {
				sb.append(line);
			}

			sb.append("\n");
		}
		return sb.toString();
	}

	private void applyMetaData(AuthClient authClient, String name, ShiroOpenIdMetaData metaData) {
		if (metaData == null) {
			return;
		}
		addConfiguration(authClient, "oidcConfig_md_" + name, "com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata");

		addConfiguration(authClient, "oidcConfig_md_" + name + ".issuer", metaData.getIssuer());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".authorization_endpoint", metaData.getAuthorizationEndpoint());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".token_endpoint", metaData.getTokenEndpoint());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".registration_endpoint", metaData.getRegistrationEndpoint());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".jwks_uri", metaData.getJwksUri());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".scopes_supported", metaData.getScopesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".response_types_supported", metaData.getResponseTypesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".response_modes_supported", metaData.getResponseModesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".grant_types_supported", metaData.getGrantTypesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".code_challenge_methods_supported", metaData.getCodeChallengeMethodsSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".token_endpoint_auth_methods_supported",
				metaData.getTokenEndpointAuthMethodsSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".token_endpoint_auth_signing_alg_values_supported",
				metaData.getTokenEndpointAuthSigningAlgValuesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".request_object_signing_alg_values_supported",
				metaData.getRequestObjectSigningAlgValuesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".request_object_encryption_alg_values_supported",
				metaData.getRequestObjectEncryptionAlgValuesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".request_object_encryption_enc_values_supported",
				metaData.getRequestObjectEncryptionEncValuesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".ui_locales_supported", metaData.getUiLocalesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".service_documentation", metaData.getServiceDocumentation());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".op_policy_uri", metaData.getOpPolicyUri());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".op_tos_uri", metaData.getOpTosUri());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".introspection_endpoint", metaData.getIntrospectionEndpoint());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".introspection_endpoint_auth_methods_supported",
				metaData.getIntrospectionEndpointAuthMethodsSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".introspection_endpoint_auth_signing_alg_values_supported",
				metaData.getIntrospectionEndpointAuthSigningAlgValuesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".revocation_endpoint", metaData.getRevocationEndpoint());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".revocation_endpoint_auth_methods_supported",
				metaData.getRevocationEndpointAuthMethodsSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".revocation_endpoint_auth_signing_alg_values_supported",
				metaData.getRevocationEndpointAuthSigningAlgValuesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".tls_client_certificate_bound_access_tokens",
				metaData.getTlsClientCertificateBoundAccessTokens());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".authorization_signing_alg_values_supported",
				metaData.getAuthorizationSigningAlgValuesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".authorization_encryption_alg_values_supported",
				metaData.getAuthorizationEncryptionAlgValuesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".authorization_encryption_enc_values_supported",
				metaData.getAuthorizationEncryptionEncValuesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".userinfo_endpoint", metaData.getUserinfoEndpoint());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".check_session_iframe", metaData.getCheckSessionIframe());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".end_session_endpoint", metaData.getEndSessionEndpoint());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".acr_values_supported", metaData.getAcrValuesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".subject_types_supported", metaData.getSubjectTypesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".id_token_signing_alg_values_supported",
				metaData.getIdTokenSigningAlgValuesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".id_token_encryption_alg_values_supported",
				metaData.getIdTokenEncryptionAlgValuesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".id_token_encryption_enc_values_supported",
				metaData.getIdTokenEncryptionEncValuesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".userinfo_signing_alg_values_supported",
				metaData.getUserinfoSigningAlgValuesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".userinfo_encryption_alg_values_supported",
				metaData.getUserinfoEncryptionAlgValuesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".userinfo_encryption_enc_values_supported",
				metaData.getUserinfoEncryptionEncValuesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".display_values_supported", metaData.getDisplayValuesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".claim_types_supported", metaData.getClaimTypesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".claims_supported", metaData.getClaimsSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".claims_locales_supported", metaData.getClaimsLocalesSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".claims_parameter_supported", metaData.getClaimsParameterSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".request_parameter_supported", metaData.getRequestParameterSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".request_uri_parameter_supported", metaData.getRequestUriParameterSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".require_request_uri_registration", metaData.getRequireRequestUriRegistration());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".backchannel_logout_supported", metaData.getBackchannelLogoutSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".backchannel_logout_session_supported",
				metaData.getBackchannelLogoutSessionSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".frontchannel_logout_supported", metaData.getFrontchannelLogoutSupported());
		addConfiguration(authClient, "oidcConfig_md_" + name + ".frontchannel_logout_session_supported",
				metaData.getFrontchannelLogoutSessionSupported());

	}

	private Class<?> resolveImplementationClass(ShiroClient client) {
		if (client instanceof ShiroOidcGoogleClient) {
			// return LoggingGoogleOidcClient.class; //Activate this for debug logging
			return GoogleOidcClient.class;
		} else if (client instanceof ShiroAzureAdClient) {
			return AzureAdClient.class;
		} else if (client instanceof ShiroFacebookClient) {
			return FacebookClient.class;
		} else if (client instanceof ShiroGithubClient) {
			return GitHubClient.class;
		} else if (client instanceof ShiroTwitterClient) {
			return TwitterClient.class;
		} else if (client instanceof ShiroGenericOAuth20Client) {
			return GenericOAuth20Client.class;
		} else if (client instanceof ShiroOpenIdClient) {
			return OidcClient.class;
		}

		return null;
	}

	private void addConfiguration(AuthClient authClient, String key, Object value) {
		if (value != null) {
			authClient.getConfiguration().put(key, value.toString());
		}
	}
	private void addConfigurationMap(AuthClient authClient, String key, Map<String, String> value) {
		if (value != null && !value.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, String> entry : value.entrySet()) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(entry.getKey());
				sb.append(":");
				sb.append(entry.getValue());
			}
			authClient.getConfiguration().put(key, sb.toString());
		}
	}

	public void setIniTemplate(String iniTemplate) {
		this.iniTemplate = iniTemplate;
	}

	public void setConfiguration(ShiroAuthenticationConfiguration configuration) {
		this.configuration = configuration;
	}
	public void setLoginServletPath(String loginServletPath) {
		this.loginServletPath = loginServletPath;
	}

}
