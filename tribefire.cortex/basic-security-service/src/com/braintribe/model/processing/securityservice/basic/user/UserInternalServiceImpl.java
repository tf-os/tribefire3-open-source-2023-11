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
package com.braintribe.model.processing.securityservice.basic.user;

import java.util.List;

import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.UnsupportedOperation;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceError;
import com.braintribe.model.processing.securityservice.api.exceptions.UserNotFoundException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.securityservice.credentials.identification.EmailIdentification;
import com.braintribe.model.securityservice.credentials.identification.UserIdentification;
import com.braintribe.model.securityservice.credentials.identification.UserNameIdentification;
import com.braintribe.model.user.Group;
import com.braintribe.model.user.User;

/**
 * <p>
 * Implementation of {@link UserInternalService} which relies on a given {@link PersistenceGmSession} instance.
 * 
 */
public class UserInternalServiceImpl implements UserInternalService {

	private PersistenceGmSession gmSession;

	private static final Logger log = Logger.getLogger(UserInternalServiceImpl.class);

	private static final String userQueryError = "Failed to query user by property \"%s\" value \"%s\": %s";

	// @formatter:off
	private static final TraversingCriterion userQueryTc = 
			TC.create()
				.negation()
					.disjunction()
						.typeCondition(TypeConditions.isKind(TypeKind.scalarType))
						.pattern()
							.entity(User.T)
							.disjunction()
								.property("groups")
								.property("roles")
							.close()
						.close()
						.pattern()
							.entity(Group.T)
							.property("roles")
						.close()
					.close()
				.done();
	// @formatter:on

	public UserInternalServiceImpl(PersistenceGmSession gmSession) {
		if (gmSession == null)
			throw new IllegalArgumentException("UserInternalServiceImpl requires a valid PersistenceGmSession");
		this.gmSession = gmSession;
	}

	@Override
	public User findUser(String propertyName, String propertyValue) {

		EntityQuery query = EntityQueryBuilder.from(User.class).where().property(propertyName).eq(propertyValue).tc(userQueryTc).done();

		try {
			List<User> list = gmSession.query().entities(query).list();

			switch (list.size()) {
				case 0:
					return null;
				case 1:
					return list.get(0);
				default:
					throw new SecurityServiceError(
							String.format(userQueryError, propertyName, propertyValue, "Query returned multiple results (" + list.size() + ")"));
			}

		} catch (GmSessionException e) {
			throw new SecurityServiceError(String.format(userQueryError, propertyName, propertyValue, e.getMessage()), e);
		}

	}

	@Override
	public User retrieveUser(String propertyName, String propertyValue) throws UserNotFoundException {
		User user = findUser(propertyName, propertyValue);
		if (user == null) {
			throw new UserNotFoundException(
					String.format("No user found by the %s \"%s\": %s", propertyName, propertyValue, "Query returned no results"));
		}
		return user;
	}

	@Override
	public User retrieveUser(UserIdentification userIdentification) throws UserNotFoundException {
		String userPropertyName = null, userPropertyValue = null;
		if (userIdentification instanceof UserNameIdentification) {
			userPropertyName = "name";
			userPropertyValue = ((UserNameIdentification) userIdentification).getUserName();
		} else if (userIdentification instanceof EmailIdentification) {
			userPropertyName = "email";
			userPropertyValue = ((EmailIdentification) userIdentification).getEmail();
		} else {
			throw new UnsupportedOperationException("Given UserIdentification is not yet implemented: " + userIdentification.getClass().getName());
		}
		return retrieveUser(userPropertyName, userPropertyValue);
	}

	@Override
	public User retrieveUser(UserIdentification userIdentification, String password) throws UserNotFoundException {

		String userPropertyName = null, userPropertyValue = null;
		if (userIdentification instanceof UserNameIdentification) {
			userPropertyName = "name";
			userPropertyValue = ((UserNameIdentification) userIdentification).getUserName();
		} else if (userIdentification instanceof EmailIdentification) {
			userPropertyName = "email";
			userPropertyValue = ((EmailIdentification) userIdentification).getEmail();
		} else {
			throw new UnsupportedOperationException("Given UserIdentification is not yet implemented: " + userIdentification.getClass().getName());
		}

		User user = findUser(userPropertyName, userPropertyValue, password);

		if (user == null) {
			throw new UserNotFoundException(String.format("Querying user by property \"%s\" value \"%s\" and password returned no values",
					userPropertyName, userPropertyValue));
		}

		return user;
	}

	@Override
	public String retrieveUserId(UserIdentification userIdentification) throws UserNotFoundException {
		if (userIdentification instanceof UserNameIdentification) {
			return ((UserNameIdentification) userIdentification).getUserName();
		} else if (userIdentification instanceof EmailIdentification) {
			return retrieveUserName("email", ((EmailIdentification) userIdentification).getEmail());
		} else {
			throw new UnsupportedOperationException("Given UserIdentification is not yet implemented: " + userIdentification.getClass().getName());
		}
	}

	@Override
	public String retrieveUserName(String propertyName, String propertyValue) throws UserNotFoundException {

		SelectQuery select = new SelectQueryBuilder().from(User.class, "u").select("u", "name").where().property("u", propertyName).eq(propertyValue)
				.done();

		try {
			List<String> list = gmSession.query().select(select).list();

			switch (list.size()) {
				case 0:
					throw new UserNotFoundException(String.format(userQueryError, propertyName, propertyValue, "Query returned no results"));
				case 1:
					return list.get(0);
				default:
					throw new SecurityServiceError(
							String.format(userQueryError, propertyName, propertyValue, "Query returned multiple results (" + list.size() + ")"));
			}

		} catch (GmSessionException e) {
			throw new SecurityServiceError(String.format(userQueryError, propertyName, propertyValue, e.getMessage()), e);
		}

	}

	/**
	 * Queries for a {@link User} taking the password into account.
	 * 
	 * @param propertyName
	 *            Name of the property to be used for fetching the {@link User}
	 * @param propertyValue
	 *            Value of the property to be used for fetching the {@link User}
	 * @param password
	 *            Password used for fetching the {@link User}
	 * @return The {@link User} if found, {@code null} if not found.
	 */
	protected User findUser(String propertyName, String propertyValue, String password) {

		// @formatter:off
		EntityQuery query = EntityQueryBuilder.from(User.class)
								.where()
									.conjunction()
										.property(propertyName).eq(propertyValue)
										.property("password").eq(password)
									.close()
								.tc(userQueryTc)
								.done();
		// @formatter:on

		try {
			List<User> list = gmSession.query().entities(query).list();

			switch (list.size()) {
				case 0:
					return null;
				case 1:
					return list.get(0);
				default:
					if (log.isDebugEnabled()) {
						log.debug(String.format("Querying user by property \"%s\" value \"%s\" and password returned multiple results (%s)",
								propertyName, propertyValue, list.size()));
					}
					throw new SecurityServiceError(
							String.format(userQueryError, propertyName, propertyValue, "Query returned multiple results (" + list.size() + ")"));
			}

		} catch (Exception e) {
			throw new SecurityServiceError(String.format(userQueryError, propertyName, propertyValue, e.getMessage()), e);
		}

	}

	@Override
	public Reason ensureUser(User user) {
		// User user = findUser(User.name, user.getName());
		return Reasons.build(UnsupportedOperation.T).text("Ensuring User persistence ist not supported").toReason();
	}
}
