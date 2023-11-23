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
package com.braintribe.model.processing.securityservice.ldap;

import java.util.Set;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.ldapaccessdeployment.LdapUserAccess;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.securityservice.api.AuthenticationService;
import com.braintribe.model.processing.securityservice.api.exceptions.AuthenticationException;
import com.braintribe.model.processing.securityservice.api.exceptions.InvalidCredentialsException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.securityservice.UserAuthenticationResponse;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.securityservice.credentials.TokenWithPasswordCredentials;
import com.braintribe.model.securityservice.credentials.UserPasswordCredentials;
import com.braintribe.model.securityservice.credentials.identification.UserIdentification;
import com.braintribe.model.securityservice.credentials.identification.UserNameIdentification;
import com.braintribe.model.securityservice.messages.AuthenticationStatusMessage;
import com.braintribe.model.securityservice.messages.InvalidCredentialsMessage;
import com.braintribe.model.securityservice.messages.UnsupportedCredentialsMessage;
import com.braintribe.model.user.User;
import com.braintribe.utils.lcd.StringTools;

/**
 * This implementation of {@link AuthenticationService} uses a configurable {@link LdapUserAccess} to authenticate
 * user/password credentials.
 * 
 * @author roman.kurmanowytsch
 */
public class LdapAuthenticationService implements AuthenticationService {

	protected static Logger logger = Logger.getLogger(LdapAuthenticationService.class);

	protected PersistenceGmSessionFactory sessionFactory = null;
	protected String ldapAccessId = null;
	protected boolean groupsAreRoles = true;

	@Override
	public UserAuthenticationResponse authenticate(Credentials credentials) throws AuthenticationException {

		boolean debug = logger.isDebugEnabled();

		if (debug)
			logger.debug("LDAP authentication requested.");

		// Check whether the credentials are available; otherwise we throw an exception
		if (credentials == null) {
			throw new InvalidCredentialsException("The credentials are null.");
		}

		String username = null;
		String password = null;

		// Check for supported credential types. If the credentials are not supported,
		// a response returning the set of supported credential types is sent back.

		if (credentials instanceof UserPasswordCredentials) {
			if (debug)
				logger.debug("The credentials are of type UserPasswordCredentials");

			UserPasswordCredentials userPwdCredentials = (UserPasswordCredentials) credentials;
			UserIdentification userIdentification = userPwdCredentials.getUserIdentification();
			if (!(userIdentification instanceof UserNameIdentification)) {
				if (debug)
					logger.debug("Cannot understand UserIdentification type " + userIdentification.getClass());
				UserAuthenticationResponse response = this.createUnsupportedResponse();
				return response;
			}
			UserNameIdentification userNameIdentification = (UserNameIdentification) userIdentification;
			username = userNameIdentification.getUserName();
			password = userPwdCredentials.getPassword();

		} else if (credentials instanceof TokenWithPasswordCredentials) {
			if (debug)
				logger.debug("The credentials are of type TokenWithPasswordCredentials");

			TokenWithPasswordCredentials tokenWithPasswdCredentials = (TokenWithPasswordCredentials) credentials;
			username = tokenWithPasswdCredentials.getUserName();
			password = tokenWithPasswdCredentials.getPassword();

		} else {
			if (debug)
				logger.debug("Credentials of type " + credentials.getClass() + " are not supported.");
			UserAuthenticationResponse response = this.createUnsupportedResponse();
			return response;
		}

		if ((username == null) || (password == null)) {
			throw new InvalidCredentialsException(
					"Either username (" + username + ") or password (" + StringTools.simpleObfuscatePassword(password) + ") is null.");
		}
		if (debug)
			logger.debug("Authenticating LDAP user " + username + " with password " + StringTools.simpleObfuscatePassword(password));

		// Create a session to the LDAP User Access

		PersistenceGmSession ldapSession = null;
		try {
			ldapSession = this.sessionFactory.newSession(this.ldapAccessId);
			if (debug)
				logger.debug("Successfully got a session to the LDAP access: " + (ldapSession != null));
		} catch (Exception e) {
			throw new AuthenticationException("Error while trying to acquire a session to the LDAP access " + this.ldapAccessId, e);
		}
		if (ldapSession == null) {
			throw new AuthenticationException("Could not acquire a session to the LDAP access " + this.ldapAccessId);
		}

		// Query for the username and password in the LDAP User Access. This query is specially treated
		// in this access and is regarded as an authentication.

		EntityQuery query = EntityQueryBuilder.from(User.class).where().conjunction().property("name").eq(username).property("password").eq(password)
				.close().done();
		User user = null;
		try {
			if (debug)
				logger.debug("Executing query to authenticate username/password.");
			user = ldapSession.query().entities(query).unique();
			if (debug)
				logger.debug("Query to authenticate username/password finished.");
		} catch (Exception e) {
			logger.warn("Error while trying to query the LDAP Access for username " + username + " and password "
					+ StringTools.simpleObfuscatePassword(password), e);
			user = null;
		}

		// If no user with this username and password can be found, a negative response will be constructed
		if (user == null) {
			UserAuthenticationResponse response = UserAuthenticationResponse.T.create();
			response.setSuccessful(false);
			AuthenticationStatusMessage statusMessage = InvalidCredentialsMessage.T.create();
			statusMessage
					.setMessage("Could not find a user with name " + username + " and password " + StringTools.simpleObfuscatePassword(password));
			response.setStatusMessage(statusMessage);
			return response;
		}

		// Create the successful response containing the returned User object
		if (debug)
			logger.debug("Got a User object from the LDAP access.");

		// [manual lazy-loading] Get roles and groups of the user in order to return complete authorization package.
		// LdapUserAccess.getUser() specifically excludes the Groups and Roles from the Partial representation
		// so they are not obtainable with Transitive Criteria
		if (debug)
			logger.debug("Fetching groups/roles for " + username + "...");
		try {
			if (debug) {
				logger.debug("Found: " + user.getGroups().size() + " groups");
			} else {
				user.getGroups().size();
			}
			if (debug) {
				logger.debug("Found: " + user.getRoles().size() + " roles");
			} else {
				user.getRoles().size();
			}

		} catch (Exception e) {
			throw new AuthenticationException("Error while trying to fetch/groups of user " + user, e);
		}

		// Create the successful response containing the returned User object
		if (debug)
			logger.debug("Got a User object from the LDAP access: " + user);

		StandardCloningContext cloningContext = new StandardCloningContext();
		cloningContext.setAbsenceResolvable(false);
		User clonedUser = User.T.clone(new StandardCloningContext(), user, StrategyOnCriterionMatch.skip);

		UserAuthenticationResponse response = UserAuthenticationResponse.T.create();
		response.setUser(clonedUser);
		response.setSuccessful(true);

		return response;
	}

	/**
	 * In case unsupported credentials or user identifications are used, this method creates a
	 * {@link UserAuthenticationResponse} which contains information about the supported credentials and user
	 * identifications types.
	 * 
	 * @return A {@link UserAuthenticationResponse} that contains information about supported credentials and user
	 *         identification types. Furthermore, the successful flag of the response is set to false.
	 */
	protected UserAuthenticationResponse createUnsupportedResponse() {
		UserAuthenticationResponse unsupportedCredentialsResponse = UserAuthenticationResponse.T.create();
		unsupportedCredentialsResponse.setSuccessful(false);

		UnsupportedCredentialsMessage statusMessage = UnsupportedCredentialsMessage.T.create();
		unsupportedCredentialsResponse.setStatusMessage(statusMessage);

		Set<String> supportedCredentials = statusMessage.getSupportedCredentials();
		supportedCredentials.add(UserPasswordCredentials.class.toString());
		supportedCredentials.add(TokenWithPasswordCredentials.class.toString());

		Set<String> supportedUserIdentifications = statusMessage.getSupportedUserIdentifications();
		supportedUserIdentifications.add(UserNameIdentification.class.toString());

		return unsupportedCredentialsResponse;
	}

	@Configurable
	@Required
	public void setLdapAccessId(String ldapAccessId) {
		this.ldapAccessId = ldapAccessId;
	}
	@Configurable
	@Required
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	@Configurable
	public void setGroupsAreRoles(boolean groupsAreRoles) {
		this.groupsAreRoles = groupsAreRoles;
	}

}
