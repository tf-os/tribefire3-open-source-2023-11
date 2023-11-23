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
package tribefire.platform.wire.space.security;

import static com.braintribe.wire.api.util.Sets.set;

import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.crypto.Cryptor;
import com.braintribe.model.extensiondeployment.HardwiredServiceProcessor;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.crypto.PropertyCrypting;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.credential.authenticator.ExistingSessionCredentialsAuthenticationServiceProcessor;
import com.braintribe.model.processing.credential.authenticator.GrantedCredentialsAuthenticationServiceProcessor;
import com.braintribe.model.processing.credential.authenticator.TrustedCredentialsAuthenticationServiceProcessor;
import com.braintribe.model.processing.credential.authenticator.UserPasswordCredentialsAuthenticationServiceProcessor;
import com.braintribe.model.processing.cryptor.basic.provider.BasicCryptorProvider;
import com.braintribe.model.processing.deployment.utils.QueryingModelProvider;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceError;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.securityservice.credentials.ExistingSessionCredentials;
import com.braintribe.model.securityservice.credentials.GrantedCredentials;
import com.braintribe.model.securityservice.credentials.TrustedCredentials;
import com.braintribe.model.securityservice.credentials.UserPasswordCredentials;
import com.braintribe.model.user.User;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.space.WireSpace;
import com.braintribe.wire.api.util.Maps;

import tribefire.platform.impl.deployment.HardwiredComponent;
import tribefire.platform.wire.space.common.CryptoSpace;
import tribefire.platform.wire.space.common.HttpSpace;
import tribefire.platform.wire.space.cortex.GmSessionsSpace;
import tribefire.platform.wire.space.cortex.accesses.CortexAccessSpace;
import tribefire.platform.wire.space.security.accesses.AuthAccessSpace;
import tribefire.platform.wire.space.security.services.UserSessionServiceSpace;

@Managed
public class AuthenticatorsSpace implements WireSpace {

	@Import
	private AuthAccessSpace authAccess;

	@Import
	private CortexAccessSpace cortexAccess;

	@Import
	private UserSessionServiceSpace userSessionService;

	@Import
	private AuthContextSpace authContext;

	@Import
	private CryptoSpace crypto;

	@Import
	private HttpSpace http;

	@Import
	private GmSessionsSpace gmSessions;

	@Import
	private WireContext<?> wireContext;

	private HardwiredComponent<HardwiredServiceProcessor, ServiceProcessor<?, ?>> hardwiredAuthenticator(
			Supplier<ServiceProcessor<?, ?>> processorSupplier) {
		return new HardwiredComponent<>(processorSupplier, HardwiredServiceProcessor.T, wireContext.currentInstancePath().firstElement());
	}

	@Managed
	public Map<EntityType<? extends Credentials>, HardwiredComponent<HardwiredServiceProcessor, ServiceProcessor<?, ?>>> hardwiredComponents() {
		// @formatter:off
		return Maps.map(
				Maps.entry(ExistingSessionCredentials.T, existingSessionCredentials()), 
				Maps.entry(UserPasswordCredentials.T, userPasswordCredentials()), 
				Maps.entry(TrustedCredentials.T, trustedCredentials()), 
				Maps.entry(GrantedCredentials.T, grantedCredentials())
		);
		// @formatter:on
	}

	@Managed
	public HardwiredComponent<HardwiredServiceProcessor, ServiceProcessor<?, ?>> existingSessionCredentials() {
		return hardwiredAuthenticator(this::existingSessionCredentialsAuthenticator);
	}

	@Managed
	public HardwiredComponent<HardwiredServiceProcessor, ServiceProcessor<?, ?>> trustedCredentials() {
		return hardwiredAuthenticator(this::trustedCredentialsAuthenticator);
	}

	@Managed
	public HardwiredComponent<HardwiredServiceProcessor, ServiceProcessor<?, ?>> grantedCredentials() {
		return hardwiredAuthenticator(this::grantedCredentialsAuthenticator);
	}

	@Managed
	public HardwiredComponent<HardwiredServiceProcessor, ServiceProcessor<?, ?>> userPasswordCredentials() {
		return hardwiredAuthenticator(this::userPasswordCredentialsAuthenticator);
	}

	@Managed
	public ExistingSessionCredentialsAuthenticationServiceProcessor existingSessionCredentialsAuthenticator() {
		ExistingSessionCredentialsAuthenticationServiceProcessor bean = new ExistingSessionCredentialsAuthenticationServiceProcessor();
		bean.setAuthGmSessionProvider(authAccess.sessionProvider());
		return bean;
	}

	@Managed
	public TrustedCredentialsAuthenticationServiceProcessor trustedCredentialsAuthenticator() {

		TrustedCredentialsAuthenticationServiceProcessor bean = new TrustedCredentialsAuthenticationServiceProcessor();
		bean.setAuthGmSessionProvider(authAccess.sessionProvider());
		return bean;
	}

	@Managed
	public GrantedCredentialsAuthenticationServiceProcessor grantedCredentialsAuthenticator() {

		GrantedCredentialsAuthenticationServiceProcessor bean = new GrantedCredentialsAuthenticationServiceProcessor();
		bean.setAuthGmSessionProvider(authAccess.sessionProvider());
		bean.setGrantingRoles(set("tf-admin", "tf-locksmith", "tf-internal"));

		return bean;

	}

	@Managed
	public UserPasswordCredentialsAuthenticationServiceProcessor userPasswordCredentialsAuthenticator() {
		UserPasswordCredentialsAuthenticationServiceProcessor bean = new UserPasswordCredentialsAuthenticationServiceProcessor();
		bean.setAuthGmSessionProvider(authAccess.sessionProvider());
		bean.setDecryptSecret(TribefireRuntime.getProperty(TribefireRuntime.DEFAULT_TRIBEFIRE_DECRYPTION_SECRET));
		bean.setCryptorProvider(crypto.cryptorProvider());
		return bean;
	}

	@Managed
	public Cryptor standardCryptor() {

		String modelName = authAccess.modelName();

		QueryingModelProvider modelProvider = new QueryingModelProvider();
		modelProvider.setSessionProvider(authAccess.sessionProvider());
		modelProvider.setModelName(modelName);

		GmMetaModel metaModel = modelProvider.get();

		BasicModelOracle oracle = new BasicModelOracle(metaModel);
		CmdResolverImpl cmdResolver = new CmdResolverImpl(oracle);

		Cryptor bean = getUserPasswordCryptor(cmdResolver);
		return bean;
	}

	protected Cryptor getUserPasswordCryptor(CmdResolver metaDataResolver) {
		PropertyCrypting propertyCrypting = metaDataResolver.getMetaData() //
				.entityType(User.T) //
				.property(User.password) //
				.meta(PropertyCrypting.T) //
				.exclusive();

		if (propertyCrypting == null) {
			return null;
		}
		BasicCryptorProvider cryptorProvider = crypto.cryptorProvider();

		try {
			return cryptorProvider.provideFor(propertyCrypting);

		} catch (Exception e) {
			throw new SecurityServiceError(e.getMessage(), e);
		}
	}
}
