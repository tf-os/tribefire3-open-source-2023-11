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
package tribefire.extension.okta.templates.wire.space;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.braintribe.logging.Logger;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.extension.okta.deployment.model.OktaAccess;
import tribefire.extension.okta.deployment.model.OktaAuthenticationSupplier;
import tribefire.extension.okta.deployment.model.OktaAuthorizationPreProcessor;
import tribefire.extension.okta.deployment.model.OktaClientSecretTokenAuthenticationSupplier;
import tribefire.extension.okta.deployment.model.OktaConfiguredTokenAuthenticationSupplier;
import tribefire.extension.okta.deployment.model.OktaOauthTokenAuthenticationSupplier;
import tribefire.extension.okta.templates.api.OktaTemplateContext;
import tribefire.extension.okta.templates.wire.contract.ExistingInstancesContract;
import tribefire.extension.okta.templates.wire.contract.OktaDbMappingsContract;
import tribefire.extension.okta.templates.wire.contract.OktaMetaDataContract;
import tribefire.extension.okta.templates.wire.contract.OktaModelsContract;
import tribefire.extension.okta.templates.wire.contract.OktaTemplatesContract;
import tribrefire.extension.okta.common.OktaCommons;

@Managed
public class OktaTemplatesSpace implements WireSpace, OktaTemplatesContract, OktaCommons {

	private static final Logger logger = Logger.getLogger(OktaTemplatesSpace.class);

	@Import
	private ExistingInstancesContract existing;

	@Import
	private OktaModelsContract models;

	@Import
	private CoreInstancesContract coreInstances;

	@Import
	private OktaDbMappingsContract dbMappings;

	@Import
	private OktaMetaDataContract metadata;

	private static AtomicInteger counter = new AtomicInteger(0);

	@Override
	public void configure(OktaTemplateContext context) {

		metadata.configureMetaData(context);

	}

	@Override
	@Managed
	public OktaAccess oktaAccess(OktaTemplateContext context) {
		OktaAccess bean = context.create(OktaAccess.T, InstanceConfiguration.currentInstance());
		bean.setExternalId(context.getIdPrefix() + "." + DEFAULT_OKTA_ACCESS_EXTERNALID);
		bean.setName(DEFAULT_OKTA_ACCESS_NAME + " (" + context.getContext() + ")");
		bean.setMetaModel(models.configuredOktaAccessModel(context));
		bean.setServiceModel(models.configuredOktaApiModel(context));
		return bean;
	}

	@Managed
	@Override
	public OktaAuthorizationPreProcessor authorizationPreProcessor(OktaTemplateContext context) {
		OktaAuthorizationPreProcessor bean = context.create(OktaAuthorizationPreProcessor.T, InstanceConfiguration.currentInstance());
		bean.setExternalId(context.getIdPrefix() + "." + OKTA_AUTHORIZATION_PREPROCESSOR_EXTERNALID);
		bean.setName(OKTA_AUTHORIZATION_PREPROCESSOR_NAME + " " + context.getContext());
		OktaAuthenticationSupplier oktaAuthenticationSupplier = context.getAccessAuthenticationSupplier();
		if (useOauthToken(oktaAuthenticationSupplier)) {
			bean.setAuthenticationSupplier(oauthAuthenticationSupplier(context, (OktaOauthTokenAuthenticationSupplier) oktaAuthenticationSupplier));
		} else if (useConfiguredToken(oktaAuthenticationSupplier)) {
			bean.setAuthenticationSupplier(
					configuredTokenAuthenticationSupplier(context, (OktaConfiguredTokenAuthenticationSupplier) oktaAuthenticationSupplier));
		} else if (useClientSecret(oktaAuthenticationSupplier)) {
			bean.setAuthenticationSupplier(
					clientSecretTokenAuthenticationSupplier(context, (OktaClientSecretTokenAuthenticationSupplier) oktaAuthenticationSupplier));
		} else {
			logger.error("Missing configuration.");
		}
		return bean;
	}

	@Managed
	@Override
	public OktaAuthorizationPreProcessor configuredAuthorizationPreProcessor(OktaTemplateContext context,
			OktaAuthenticationSupplier oktaAuthenticationSupplier) {
		OktaAuthorizationPreProcessor bean = context.create(OktaAuthorizationPreProcessor.T, InstanceConfiguration.currentInstance());
		int count = counter.incrementAndGet();
		bean.setExternalId(context.getIdPrefix() + "." + OKTA_AUTHORIZATION_PREPROCESSOR_EXTERNALID + "."
				+ oktaAuthenticationSupplier.entityType().getShortName() + "." + count);
		bean.setName(OKTA_AUTHORIZATION_PREPROCESSOR_NAME + " " + context.getContext() + " (" + oktaAuthenticationSupplier.entityType().getShortName()
				+ "/" + count + ")");

		if (useOauthToken(oktaAuthenticationSupplier)) {
			bean.setAuthenticationSupplier(oauthAuthenticationSupplier(context, (OktaOauthTokenAuthenticationSupplier) oktaAuthenticationSupplier));
		} else if (useConfiguredToken(oktaAuthenticationSupplier)) {
			bean.setAuthenticationSupplier(
					configuredTokenAuthenticationSupplier(context, (OktaConfiguredTokenAuthenticationSupplier) oktaAuthenticationSupplier));
		} else if (useClientSecret(oktaAuthenticationSupplier)) {
			bean.setAuthenticationSupplier(
					clientSecretTokenAuthenticationSupplier(context, (OktaClientSecretTokenAuthenticationSupplier) oktaAuthenticationSupplier));
		} else {
			logger.error("Missing auth configuration.");
		}

		return bean;
	}

	@Managed
	private OktaOauthTokenAuthenticationSupplier oauthAuthenticationSupplier(OktaTemplateContext context,
			OktaOauthTokenAuthenticationSupplier authConfig) {
		OktaOauthTokenAuthenticationSupplier bean = context.create(OktaOauthTokenAuthenticationSupplier.T, InstanceConfiguration.currentInstance());

		String clientId = authConfig.getClientId();
		String audience = authConfig.getAudience();
		if (StringTools.isBlank(audience)) {
			String serviceBaseUrl = context.getServiceBaseUrl();
			if (!StringTools.isBlank(serviceBaseUrl)) {
				audience = serviceBaseUrl.replace("/api/v1", "/oauth2/v1/token");
			}
		}
		Integer expirationInS = authConfig.getExpirationDurationInSeconds();
		Set<String> scopes = authConfig.getScopes();

		bean.setKeyModulusN(authConfig.getKeyModulusN());
		bean.setPrivateExponentD(authConfig.getPrivateExponentD());
		bean.setClientId(clientId);
		bean.setAudience(audience);
		bean.setOktaDomainId(authConfig.getOktaDomainId());
		if (expirationInS != null && expirationInS >= 0) {
			bean.setExpirationDurationInSeconds(expirationInS);
		}
		if (scopes != null && !scopes.isEmpty()) {
			bean.setScopes(scopes);
		}

		return bean;
	}

	@Managed
	private OktaClientSecretTokenAuthenticationSupplier clientSecretTokenAuthenticationSupplier(OktaTemplateContext context,
			OktaClientSecretTokenAuthenticationSupplier authConfig) {
		OktaClientSecretTokenAuthenticationSupplier bean = context.create(OktaClientSecretTokenAuthenticationSupplier.T,
				InstanceConfiguration.currentInstance());

		String clientId = authConfig.getClientId();
		String clientSecret = authConfig.getClientSecret();
		Set<String> scopes = authConfig.getScopes();

		bean.setClientId(clientId);
		bean.setClientSecret(clientSecret);
		bean.setOktaDomainId(authConfig.getOktaDomainId());
		if (scopes != null && !scopes.isEmpty()) {
			bean.setScopes(scopes);
		}
		bean.setTokenUrl(authConfig.getTokenUrl());

		return bean;
	}

	@Managed
	private OktaConfiguredTokenAuthenticationSupplier configuredTokenAuthenticationSupplier(OktaTemplateContext context,
			OktaConfiguredTokenAuthenticationSupplier authConfig) {
		OktaConfiguredTokenAuthenticationSupplier bean = context.create(OktaConfiguredTokenAuthenticationSupplier.T,
				InstanceConfiguration.currentInstance());
		bean.setAuthorizationScheme(authConfig.getAuthorizationScheme());
		bean.setAuthorizationToken(authConfig.getAuthorizationToken());
		return bean;
	}

	public boolean useOauthToken(OktaAuthenticationSupplier supplier) {
		if (supplier instanceof OktaOauthTokenAuthenticationSupplier oauth) {
			if (!StringTools.isAnyBlank(oauth.getKeyModulusN(), oauth.getPrivateExponentD())) {
				return true;
			}
		}
		return false;
	}
	public boolean useConfiguredToken(OktaAuthenticationSupplier supplier) {
		if (supplier instanceof OktaConfiguredTokenAuthenticationSupplier token) {
			if (!StringTools.isBlank(token.getAuthorizationToken())) {
				return true;
			}
		}
		return false;
	}
	public boolean useClientSecret(OktaAuthenticationSupplier supplier) {
		if (supplier instanceof OktaClientSecretTokenAuthenticationSupplier client) {
			if (!StringTools.isAnyBlank(client.getTokenUrl(), client.getClientId(), client.getClientSecret())) {
				return true;
			}
		}
		return false;
	}
}
