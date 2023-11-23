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
package tribefire.extension.okta.wire.space;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.RSAPrivateKeySpec;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.access.crud.CrudExpertAccess;
import com.braintribe.model.access.crud.CrudExpertResolver;
import com.braintribe.model.access.crud.api.read.EntityReader;
import com.braintribe.model.access.crud.api.read.PopulationReader;
import com.braintribe.model.access.crud.api.read.PropertyReader;
import com.braintribe.model.access.crud.support.query.preprocess.EntityCachingQueryPreProcessor;
import com.braintribe.model.access.crud.support.query.preprocess.QueryOrderingAndPagingAdapter;
import com.braintribe.model.access.crud.support.read.EmptyReader;
import com.braintribe.model.access.crud.support.resolver.RegistryBasedExpertResolver;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.core.expert.impl.ConfigurableGmExpertRegistry;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.provider.Holder;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.okta.deployment.model.OktaAccess;
import tribefire.extension.okta.deployment.model.jwt.OktaJwtTokenCredentialsAuthenticator;
import tribefire.extension.okta.deployment.model.OktaAuthenticationSupplier;
import tribefire.extension.okta.deployment.model.OktaClientSecretTokenAuthenticationSupplier;
import tribefire.extension.okta.deployment.model.OktaConfiguredTokenAuthenticationSupplier;
import tribefire.extension.okta.deployment.model.OktaOauthTokenAuthenticationSupplier;
import tribefire.extension.okta.model.OktaGroup;
import tribefire.extension.okta.model.OktaUser;
import tribefire.extension.okta.processing.auth.ClientSecretTokenAuthenticationSupplier;
import tribefire.extension.okta.processing.auth.ConfiguredTokenAuthenticationSupplier;
import tribefire.extension.okta.processing.auth.OAuthTokenAuthenticationSupplier;
import tribefire.extension.okta.processing.crud.OktaGroupReader;
import tribefire.extension.okta.processing.crud.OktaUserReader;
import tribefire.extension.okta.processing.jwt.OktaJwtTokenCredentialsAuthenticationServiceProcessor;
import tribefire.extension.okta.processing.service.OktaAuthorizationPreProcessor;
import tribefire.module.wire.contract.ModuleReflectionContract;
import tribefire.module.wire.contract.ModuleResourcesContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;
import tribrefire.extension.okta.common.OktaCommons;

@Managed
public class OktaDeployablesSpace implements WireSpace, OktaCommons {

	private static final Logger logger = Logger.getLogger(OktaDeployablesSpace.class);

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private ModuleResourcesContract moduleResources;

	@Import
	private ModuleReflectionContract module;

	// ***************************************************************************************************
	// Public Managed Beans
	// ***************************************************************************************************

	@Managed
	public CrudExpertAccess oktaAccess(ExpertContext<OktaAccess> context) {
		OktaAccess deployable = context.getDeployable();
		CrudExpertAccess bean = new CrudExpertAccess();
		GmMetaModel model = deployable.getMetaModel();

		bean.setAccessId(deployable.getExternalId());
		bean.setMetaModelProvider(new Holder<>(model));
		bean.setExpertResolver(oktaCrudExpertResolver(context));
		bean.setQueryPreProcessor(new EntityCachingQueryPreProcessor(QueryOrderingAndPagingAdapter.REMOVE_ORDERING_AND_ADAPT_PAGING_TO_FIRST_PAGE));

		//@formatter:off
		bean.configureDefaultTc()
			.addExclusionProperty(OktaUser.T, OktaUser.profile)
			.addExclusionProperty(OktaUser.T, OktaUser.credentials)
			.addExclusionProperty(OktaUser.T, OktaUser.type);
		//@formatter:on

		return bean;
	}

	@Managed
	public OktaAuthorizationPreProcessor oktaAuthorizationPreProcessor(
			ExpertContext<tribefire.extension.okta.deployment.model.OktaAuthorizationPreProcessor> context) {
		tribefire.extension.okta.deployment.model.OktaAuthorizationPreProcessor deployable = context.getDeployable();
		OktaAuthorizationPreProcessor bean = new OktaAuthorizationPreProcessor();
		OktaAuthenticationSupplier authenticationSupplier = deployable.getAuthenticationSupplier();
		if (authenticationSupplier instanceof OktaConfiguredTokenAuthenticationSupplier tokenSupplier) {
			bean.setAuthenticationSupplier(configuredTokenSupplier(tokenSupplier));
		} else if (authenticationSupplier instanceof OktaOauthTokenAuthenticationSupplier oauthSupplier) {
			bean.setAuthenticationSupplier(configuredOAuthSupplier(oauthSupplier));
		} else if (authenticationSupplier instanceof OktaClientSecretTokenAuthenticationSupplier secretSupplier) {
			bean.setAuthenticationSupplier(configuredClientSecretSupplier(secretSupplier));
		} else {
			logger.error("Configuration issue: the authentication supplier: " + authenticationSupplier + " is not supported.");
		}
		return bean;
	}

	private ClientSecretTokenAuthenticationSupplier configuredClientSecretSupplier(OktaClientSecretTokenAuthenticationSupplier secretSupplier) {
		ClientSecretTokenAuthenticationSupplier bean = new ClientSecretTokenAuthenticationSupplier();
		bean.setClientId(secretSupplier.getClientId());
		bean.setClientSecret(secretSupplier.getClientSecret());
		bean.setOktaDomainId(secretSupplier.getOktaDomainId());

		Set<String> scopes = secretSupplier.getScopes();
		if (scopes != null && !scopes.isEmpty()) {
			bean.setScopes(scopes);
		}
		bean.setEvaluator(tfPlatform.systemUserRelated().evaluator());

		return bean;
	}

	private OAuthTokenAuthenticationSupplier configuredOAuthSupplier(OktaOauthTokenAuthenticationSupplier oauthSupplier) {
		OAuthTokenAuthenticationSupplier bean = new OAuthTokenAuthenticationSupplier();
		bean.setClientId(oauthSupplier.getClientId());
		Integer expirationDurationInSeconds = oauthSupplier.getExpirationDurationInSeconds();
		if (expirationDurationInSeconds != null && expirationDurationInSeconds >= 0) {
			bean.setExpirationDuration(Duration.of(expirationDurationInSeconds.longValue(), ChronoUnit.SECONDS));
		}
		bean.setAudience(oauthSupplier.getAudience());
		Set<String> scopes = oauthSupplier.getScopes();
		if (scopes != null && !scopes.isEmpty()) {
			bean.setScopes(scopes);
		}
		bean.setEvaluator(tfPlatform.systemUserRelated().evaluator());
		bean.setOktaDomainId(oauthSupplier.getOktaDomainId());

		String keyModulusN = oauthSupplier.getKeyModulusN();
		String privateExponentD = oauthSupplier.getPrivateExponentD();
		try {
			//@formatter:off
			RSAPrivateKeySpec rsaPrivateKeySpec = new RSAPrivateKeySpec(
					new BigInteger(1, Base64.getUrlDecoder().decode(keyModulusN)),
					new BigInteger(1, Base64.getUrlDecoder().decode(privateExponentD))
			);
			//@formatter:on
			KeyFactory factory = KeyFactory.getInstance("RSA");
			PrivateKey privateKey = factory.generatePrivate(rsaPrivateKeySpec);

			bean.setPrivateKey(privateKey);
		} catch (Exception e) {
			logger.error("Error while trying to get private key from N " + StringTools.simpleObfuscatePassword(keyModulusN) + " and D "
					+ StringTools.simpleObfuscatePassword(privateExponentD), e);
		}
		return bean;
	}

	private ConfiguredTokenAuthenticationSupplier configuredTokenSupplier(OktaConfiguredTokenAuthenticationSupplier tokenSupplier) {
		ConfiguredTokenAuthenticationSupplier bean = new ConfiguredTokenAuthenticationSupplier();
		bean.setAuthenticationScheme(tokenSupplier.getAuthorizationScheme());
		final String token = tokenSupplier.getAuthorizationToken();
		bean.setAuthenticationTokenSupplier(() -> token);
		return bean;
	}

	@Managed
	public OktaJwtTokenCredentialsAuthenticationServiceProcessor jwtCredentialsAuthenticator(
			ExpertContext<OktaJwtTokenCredentialsAuthenticator> context) {
		OktaJwtTokenCredentialsAuthenticator deployable = context.getDeployable();
		OktaJwtTokenCredentialsAuthenticationServiceProcessor bean = new OktaJwtTokenCredentialsAuthenticationServiceProcessor();
		bean.setAudience(deployable.getAudience());
		bean.setIssuer(deployable.getIssuer());
		bean.setPropertiesClaims(deployable.getPropertiesClaims());
		bean.setConnectionTimeoutMs(deployable.getConnectionTimeoutMs());
		bean.setClaimRolesAndPrefixes(deployable.getClaimRolesAndPrefixes());
		bean.setUsernameClaim(deployable.getUsernameClaim());
		bean.setDefaultRoles(deployable.getDefaultRoles());
		bean.setInvalidateTokenCredentialsOnLogout(deployable.getInvalidateTokenCredentialsOnLogout());

		return bean;
	}

	// ***************************************************************************************************
	// Internal Managed Beans
	// ***************************************************************************************************

	@Managed
	private CrudExpertResolver oktaCrudExpertResolver(ExpertContext<OktaAccess> context) {
		RegistryBasedExpertResolver resolver = new RegistryBasedExpertResolver();
		//@formatter:off
		resolver.setRegistry(
				new ConfigurableGmExpertRegistry()
				// User (Readers)
				.add(EntityReader.class, OktaUser.class, userReader(context))
				.add(PopulationReader.class, OktaUser.class, userReader(context))
				// Group (Readers)
				.add(EntityReader.class, OktaGroup.class, groupReader(context))
				.add(PopulationReader.class, OktaGroup.class, groupReader(context))
				// Default
				.add(EntityReader.class, GenericEntity.class, EmptyReader.instance())
				.add(PopulationReader.class, GenericEntity.class, EmptyReader.instance())
				.add(PropertyReader.class, GenericEntity.class, EmptyReader.instance()));
		//@formatter:on
		return resolver;
	}

	@Managed
	private OktaUserReader userReader(ExpertContext<OktaAccess> context) {
		OktaAccess access = context.getDeployable();

		OktaUserReader bean = new OktaUserReader();
		bean.setEvaluator(tfPlatform.requestUserRelated().evaluator());
		bean.setDomainId(access.getExternalId());
		return bean;
	}

	@Managed
	private OktaGroupReader groupReader(ExpertContext<OktaAccess> context) {
		OktaAccess access = context.getDeployable();

		OktaGroupReader bean = new OktaGroupReader();
		bean.setEvaluator(tfPlatform.requestUserRelated().evaluator());
		bean.setDomainId(access.getExternalId());
		return bean;
	}

}
