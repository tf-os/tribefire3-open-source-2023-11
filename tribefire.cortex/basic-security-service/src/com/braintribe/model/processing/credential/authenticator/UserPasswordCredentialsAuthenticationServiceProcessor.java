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
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.UnsupportedOperation;
import com.braintribe.gm.model.security.reason.InvalidCredentials;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.securityservice.api.exceptions.InvalidCredentialsException;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.securityservice.AuthenticateCredentials;
import com.braintribe.model.securityservice.AuthenticateCredentialsResponse;
import com.braintribe.model.securityservice.credentials.UserPasswordCredentials;
import com.braintribe.utils.encryption.Cryptor;
import com.braintribe.utils.lcd.StringTools;

/**
 * <p>
 * This experts validates whether the provided password ({@link UserPasswordCredentials#getPassword()}) matches the
 * password known for the user given by {@link UserPasswordCredentials#getUserIdentification()}.
 * 
 */
public class UserPasswordCredentialsAuthenticationServiceProcessor extends PasswordBasedAuthenticationServiceProcessor<UserPasswordCredentials>
		implements UserIdentificationValidation {

	private static Logger log = Logger.getLogger(UserPasswordCredentialsAuthenticationServiceProcessor.class);

	private String decryptSecret = null; // TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_TRIBEFIRE_DECRYPT_SECRET)

	@Configurable
	public void setDecryptSecret(String decryptSecret) {
		this.decryptSecret = decryptSecret;
	}

	@Override
	protected Maybe<AuthenticateCredentialsResponse> authenticateCredentials(ServiceRequestContext context, AuthenticateCredentials request,
			UserPasswordCredentials credentials) {

		Reason reason = validateCredentialsAndDecrypt(credentials);

		if (reason != null)
			return reason.asMaybe();

		PersistenceGmSession authGmSession = authGmSessionProvider.get();

		return authenticate(authGmSession, credentials.getUserIdentification(), credentials.getPassword()) //
				.map(this::buildAuthenticatedUserFrom);
	}

	/**
	 * <p>
	 * Validates the given credentials
	 * 
	 * @param credentials
	 *            The credentials to be validated
	 * @throws InvalidCredentialsException
	 *             If the given credentials are invalid
	 */
	private Reason validateCredentialsAndDecrypt(UserPasswordCredentials credentials) throws InvalidCredentialsException {

		Reason identificationReason = validateUserIdentification(credentials.getUserIdentification());

		if (identificationReason != null)
			return identificationReason;

		String password = credentials.getPassword();
		if (password == null) {
			log.debug(() -> "Password is null in the given credentials: [ " + credentials + " ]");
			return Reasons.build(InvalidCredentials.T).text("Invalid credentials").toReason();
		}

		if (credentials.getPasswordIsEncrypted()) {
			if (decryptSecret == null)
				return Reasons.build(InvalidCredentials.T).text("Invalid credentials")
						.cause(Reasons.build(UnsupportedOperation.T).text("Credential password decryption not supported").toReason()).toReason();

			String encryptedPassword = password;
			if (password.startsWith("${decrypt(") && password.endsWith(")}")) {
				encryptedPassword = password.substring(10, password.length() - 2);
			}
			if ((encryptedPassword.startsWith("\"") && encryptedPassword.endsWith("\""))
					|| (encryptedPassword.startsWith("'") && encryptedPassword.endsWith("'"))) {
				encryptedPassword = encryptedPassword.substring(1, encryptedPassword.length() - 1);
			}
			if (log.isDebugEnabled()) {
				log.debug("Decrypting password " + StringTools.simpleObfuscatePassword(encryptedPassword) + " with secret "
						+ StringTools.simpleObfuscatePassword(decryptSecret));
			}

			try {
				String decryptedPassword = Cryptor.decrypt(decryptSecret, null, null, null, encryptedPassword);
				credentials.setPassword(decryptedPassword);
			} catch (Exception e) {
				log.debug(() -> "Error while trying to decrypt password " + StringTools.simpleObfuscatePassword(password), e);
				return Reasons.build(InvalidCredentials.T).text("Invalid credentials").toReason();
			}
		}

		return null;
	}

}
