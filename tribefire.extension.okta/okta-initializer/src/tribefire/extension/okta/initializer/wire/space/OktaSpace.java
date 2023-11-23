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
package tribefire.extension.okta.initializer.wire.space;

import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContext;

import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.okta.deployment.model.OktaAccess;
import tribefire.extension.okta.deployment.model.OktaAuthenticationSupplier;
import tribefire.extension.okta.deployment.model.OktaClientSecretTokenAuthenticationSupplier;
import tribefire.extension.okta.deployment.model.OktaConfiguredTokenAuthenticationSupplier;
import tribefire.extension.okta.deployment.model.OktaOauthTokenAuthenticationSupplier;
import tribefire.extension.okta.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.okta.initializer.wire.contract.OktaContract;
import tribefire.extension.okta.initializer.wire.contract.RuntimePropertiesContract;
import tribefire.extension.okta.templates.api.OktaTemplateContext;
import tribefire.extension.okta.templates.wire.contract.OktaTemplatesContract;
import tribrefire.extension.okta.common.OktaCommons;

@Managed
public class OktaSpace extends AbstractInitializerSpace implements OktaContract, OktaCommons {

	private static final Logger logger = Logger.getLogger(OktaSpace.class);

	@Import
	WireContext<?> wireContext;

	@Import
	RuntimePropertiesContract runtime;

	@Import
	ExistingInstancesContract existing;

	@Import
	OktaTemplatesContract templates;

	// ***************************************************************************************************
	// Contract
	// ***************************************************************************************************

	@Managed
	@Override
	public OktaAccess oktaAccess() {
		OktaTemplateContext context = defaultContext();
		return templates.oktaAccess(context);
	}

	@Override
	public void configure() {
		OktaTemplateContext context = defaultContext();
		templates.configure(context);
	}

	@Managed
	@Override
	public OktaTemplateContext defaultContext() {

		//@formatter:off
		OktaTemplateContext context = OktaTemplateContext.builder()
			.setContext("Default")
			.setServiceBaseUrl(runtime.OKTA_SERVICE_BASE_URL())
			.setAccessAuthenticationSupplier(accessAuthSupplier())
			.setDefaultAuthenticationSupplier(defaultAuthSupplier())
			.setModule(existing.oktaModule())
			.setName("Okta")
			.setIdPrefix("default")
			.setEntityFactory(super::create)
			.setLookupFunction(super::lookup)
			.setLookupExternalIdFunction(super::lookupExternalId)
			.build();
		//@formatter:on
		return context;
	}

	private OktaAuthenticationSupplier accessAuthSupplier() {
		if (useOauthToken()) {
			return oauthAuthenticationSupplier();
		} else if (useConfiguredToken()) {
			return configuredTokenAuthenticationSupplier();
		} else {
			logger.error("Missing configuration: Neither OAuth or SSWS token is configured.");
		}
		return null;
	}

	private OktaAuthenticationSupplier defaultAuthSupplier() {
		if (useClientSecret()) {
			return clientSecretTokenAuthenticationSupplier();
		} else {
			logger.error("Missing configuration: No Client Secret is configured.");
		}
		return null;
	}

	private boolean useOauthToken() {
		if (!StringTools.isAllBlank(runtime.OKTA_HTTP_OAUTH_KEY_MODULUS_N_ENCRYPTED(), runtime.OKTA_HTTP_OAUTH_PRIVATE_EXPONENT_D_ENCRYPTED())) {
			return true;
		}
		return false;
	}
	private boolean useConfiguredToken() {
		if (!StringTools.isBlank(runtime.OKTA_HTTP_AUTHORIZATION_TOKEN())) {
			return true;
		}
		return false;
	}
	private boolean useClientSecret() {
		if (!StringTools.isAnyBlank(runtime.OKTA_CLIENT_SECRET_TOKEN_URL(), runtime.OKTA_CLIENT_ID(), runtime.OKTA_CLIENT_SECRET())) {
			return true;
		}
		return false;
	}

	private OktaOauthTokenAuthenticationSupplier oauthAuthenticationSupplier() {
		OktaOauthTokenAuthenticationSupplier bean = OktaOauthTokenAuthenticationSupplier.T.create();

		String clientId = runtime.OKTA_HTTP_OAUTH_CLIENT_ID();
		String audience = runtime.OKTA_HTTP_OAUTH_AUDIENCE();
		if (StringTools.isBlank(audience)) {
			String serviceBaseUrl = runtime.OKTA_SERVICE_BASE_URL();
			if (!StringTools.isBlank(serviceBaseUrl)) {
				audience = serviceBaseUrl.replace("/api/v1", "/oauth2/v1/token");
			}
		}
		Integer expirationInS = runtime.OKTA_HTTP_OAUTH_EXPIRATION_S();
		Set<String> scopes = runtime.OKTA_HTTP_OAUTH_SCOPES();

		bean.setKeyModulusN(runtime.OKTA_HTTP_OAUTH_KEY_MODULUS_N_ENCRYPTED());
		bean.setPrivateExponentD(runtime.OKTA_HTTP_OAUTH_PRIVATE_EXPONENT_D_ENCRYPTED());
		bean.setClientId(clientId);
		bean.setAudience(audience);
		bean.setOktaDomainId("default.access.okta");

		if (expirationInS != null && expirationInS >= 0) {
			bean.setExpirationDurationInSeconds(expirationInS);
		}
		if (scopes != null && !scopes.isEmpty()) {
			bean.setScopes(scopes);
		}

		return bean;
	}

	private OktaClientSecretTokenAuthenticationSupplier clientSecretTokenAuthenticationSupplier() {
		OktaClientSecretTokenAuthenticationSupplier bean = OktaClientSecretTokenAuthenticationSupplier.T.create();

		String clientId = runtime.OKTA_CLIENT_ID();
		String clientSecret = runtime.OKTA_CLIENT_SECRET();
		Set<String> scopes = runtime.OKTA_CLIENT_SCOPES();

		bean.setClientId(clientId);
		bean.setClientSecret(clientSecret);
		if (scopes != null && !scopes.isEmpty()) {
			bean.setScopes(scopes);
		}
		bean.setTokenUrl(runtime.OKTA_CLIENT_SECRET_TOKEN_URL());

		return bean;
	}

	private OktaConfiguredTokenAuthenticationSupplier configuredTokenAuthenticationSupplier() {
		OktaConfiguredTokenAuthenticationSupplier bean = OktaConfiguredTokenAuthenticationSupplier.T.create();
		bean.setAuthorizationScheme(runtime.OKTA_HTTP_AUTHORIZATION_SCHEME());
		bean.setAuthorizationToken(runtime.OKTA_HTTP_AUTHORIZATION_TOKEN());
		return bean;
	}

}
