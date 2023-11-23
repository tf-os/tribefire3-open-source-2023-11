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
package com.braintribe.model.processing.credential.authenticator;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.crypto.Cryptor;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.gm.model.security.reason.InvalidCredentials;
import com.braintribe.logging.Logger;
import com.braintribe.model.meta.data.crypto.PropertyCrypting;
import com.braintribe.model.processing.crypto.provider.CryptorProvider;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceError;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.securityservice.credentials.UserPasswordCredentials;
import com.braintribe.model.securityservice.credentials.identification.UserIdentification;
import com.braintribe.model.user.User;

/**
 * <p>
 * The password comparison maybe influenced by cryptography related meta data (e.g.: {@link PropertyCrypting})
 * associated with the {@link User}'s <b>password</b> property.
 * 
 *
 * @param <T>
 *            The type of {@link Credentials} the expert handles.
 */
// TODO is this abstraction level really needed
public abstract class PasswordBasedAuthenticationServiceProcessor<T extends UserPasswordCredentials>
		extends BasicAuthenticateCredentialsServiceProcessor<T> {

	private static final Logger log = Logger.getLogger(PasswordBasedAuthenticationServiceProcessor.class);

	private Codec<String, String> passwordEncoder;
	private CryptorProvider<? extends Cryptor, PropertyCrypting> cryptorProvider;

	/**
	 * <p>
	 * Sets the {@link #passwordEncoder} property.
	 * 
	 * <p>
	 * The {@link Codec} configured to this property is used, when the {@link User} {@code password} property is not marked
	 * with a {@link PropertyCrypting} metadata, to encode the user provided password before forming the authentication
	 * query which includes password in its conditions.
	 * 
	 * <p>
	 * If the {@link User} {@code password} property is marked with a {@link PropertyCrypting} metadata, the
	 * {@link #passwordEncoder} property is not used.
	 * 
	 * @param passwordEncoder
	 *            the {@link Codec} to set
	 */
	@Configurable
	public void setPasswordEncoder(Codec<String, String> passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * <p>
	 * Sets the {@link #cryptorProvider} property.
	 * 
	 * <p>
	 * The {@link CryptorProvider} configured to this property is used, when the {@link User} {@code password} property is
	 * marked with a {@link PropertyCrypting} metadata, to provide a {@link Cryptor} capable of comparing the user provided
	 * password with the encrypted password from the {@link User} instance.
	 * 
	 * @param cryptorProvider
	 *            The {@link CryptorProvider} to be set
	 */
	@Configurable
	public void setCryptorProvider(CryptorProvider<? extends Cryptor, PropertyCrypting> cryptorProvider) {
		this.cryptorProvider = cryptorProvider;
	}

	/**
	 * @return the {@link #passwordEncoder} property
	 */
	public Codec<String, String> getPasswordEncoder() {
		return passwordEncoder;
	}

	/**
	 * <p>
	 * Encodes the given password, if the {@link #passwordEncoder} property is configured to this expert.
	 * 
	 * @param password
	 *            Password to be encoded, if the {@link #passwordEncoder} property is configured to this expert.
	 * @return The encoded password, if the {@link #passwordEncoder} property is configured to this expert.
	 * @throws SecurityServiceError
	 *             if the {@code password} encoding fails
	 */
	protected String encodeUserPassword(String password) {

		if (passwordEncoder == null) {
			return password;
		}

		String hashedPwd = null;
		try {
			hashedPwd = passwordEncoder.encode(password);
		} catch (CodecException e) {
			throw new SecurityServiceError(String.format("Unable to encode user password: %s", e.getMessage()), e);
		}
		return hashedPwd;
	}

	protected Maybe<User> authenticate(PersistenceGmSession authGmSession, UserIdentification userIdentification, String password) {

		if (password == null) {
			return Reasons.build(InvalidArgument.T).text("Missing password").toMaybe();
		}

		Cryptor userPasswordCryptor = getUserPasswordCryptor(authGmSession);

		// TODO: correlation UUID logging

		if (userPasswordCryptor == null) {

			Maybe<User> userMaybe = retrieveUser(authGmSession, userIdentification, encodeUserPassword(password));

			if (userMaybe.isUnsatisfiedBy(NotFound.T)) {
				log.debug("Authentication failure caused by: " + userMaybe.whyUnsatisfied().stringify());
				return Reasons.build(InvalidCredentials.T).text("Invalid Credentials").toMaybe();
			}

			return userMaybe;

		} else {

			Maybe<User> userMaybe = retrieveUser(authGmSession, userIdentification);

			if (userMaybe.isUnsatisfiedBy(NotFound.T)) {
				log.debug("Authentication failure caused by: " + userMaybe.whyUnsatisfied().stringify());
				return Reasons.build(InvalidCredentials.T).text("Invalid Credentials").toMaybe();
			}

			User user = userMaybe.get();

			if (user.getPassword() == null) {
				log.debug("Authentication failure caused by null password on user: " + user.getId());

				return Reasons.build(InvalidCredentials.T).text("Invalid Credentials").toMaybe();
			}

			boolean passwordMatches = false;

			try {
				passwordMatches = userPasswordCryptor.is(password).equals(user.getPassword());
			} catch (Exception e) {
				// TODO: really internal or wrong password still?
				log.error("Password cryptor failure", e);
				return InternalError.from(e, "Internal Error").asMaybe();
			}

			if (!passwordMatches) {
				log.debug("Authentication failure by password mismatch for user: " + user.getId());
				return Reasons.build(InvalidCredentials.T).text("Invalid Credentials").toMaybe();
			}

			return userMaybe;
		}

	}

	protected Cryptor getUserPasswordCryptor(PersistenceGmSession authGmSession) {

		if (authGmSession.getModelAccessory() == null) {
			log.debug(() -> "Skippig resolution of metadata as the gm session has no model accessory");
			return null;
		}

		CmdResolver metaDataResolver = authGmSession.getModelAccessory().getCmdResolver();

		if (metaDataResolver == null) {
			log.debug(() -> "Skippig resolution of metadata as the gm session's model accessory has no metadata resolver");
			return null;
		}

		return getUserPasswordCryptor(metaDataResolver);

	}

	protected Cryptor getUserPasswordCryptor(CmdResolver metaDataResolver) {

		PropertyCrypting propertyCrypting = metaDataResolver.getMetaData().entityClass(User.class).property(User.password).meta(PropertyCrypting.T)
				.exclusive();

		if (propertyCrypting == null) {
			return null;
		}

		if (cryptorProvider == null) {
			throw new SecurityServiceError("The " + this.getClass().getName() + " expert is not properly configured to handle "
					+ PropertyCrypting.class.getName() + " marked password. A " + CryptorProvider.class.getName() + " must be configured.");
		}

		Cryptor cryptor = null;
		try {
			cryptor = cryptorProvider.provideFor(propertyCrypting);
		} catch (Exception e) {
			throw new SecurityServiceError(e.getMessage(), e);
		}

		return cryptor;

	}

}
