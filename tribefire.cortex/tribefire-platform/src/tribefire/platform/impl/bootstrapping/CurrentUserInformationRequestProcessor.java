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
package tribefire.platform.impl.bootstrapping;

import static com.braintribe.utils.lcd.CollectionTools2.first;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.bapi.CurrentUserInformationRequest;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.tools.PreparedTcs;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceProcessorException;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSession;

/**
 * TODO: Move to the upcoming SecurityServiceProcessor
 */
public class CurrentUserInformationRequestProcessor implements ServiceProcessor<CurrentUserInformationRequest, User> {
	private Supplier<IncrementalAccess> authAccessSupplier;
	private IncrementalAccess authAccess;
	private Supplier<UserSession> userSessionProvider;
	private ReentrantLock authAccessLock = new ReentrantLock();

	@Required
	public void setAuthAccessSupplier(Supplier<IncrementalAccess> authAccessSupplier) {
		this.authAccessSupplier = authAccessSupplier;
	}

	@Required
	@Configurable
	public void setUserSessionProvider(Supplier<UserSession> userSessionProvider) {
		this.userSessionProvider = userSessionProvider;
	}

	@Override
	public User process(ServiceRequestContext requestContext, CurrentUserInformationRequest request) throws ServiceProcessorException {
		List<User> users = queryUsersByName(requestContext);

		UserSession userSession = userSessionProvider.get();
		User sessionUser = userSession.getUser();

		switch (users.size()) {
			case 0:
				return sessionUser;

			case 1:
				User authDbUser = first(users);
				override(sessionUser, authDbUser);
				return authDbUser;

			default:
				throw new ServiceProcessorException("Found ambigious users for the same name: " + requestContext.getRequestorUserName());
		}
	}

	private List<User> queryUsersByName(ServiceRequestContext requestContext) {
		EntityQuery userQuery = EntityQueryBuilder.from(User.class).where().property("name").eq(requestContext.getRequestorUserName())
				.tc(PreparedTcs.everythingTc).done();

		try {
			EntityQueryResult result = getAuthAccess().queryEntities(userQuery);
			return (List<User>) (List<?>) result.getEntities();

		} catch (ModelAccessException e) {
			throw new ServiceProcessorException("Error while querying for the user: " + requestContext.getRequestorUserName());
		}
	}

	private IncrementalAccess getAuthAccess() {
		if (authAccess == null)
			loadAuthAccessSync();

		return authAccess;
	}

	private void loadAuthAccessSync() {
		if (authAccess == null) {
			authAccessLock.lock();
			try {
				if (authAccess == null) {
					authAccess = authAccessSupplier.get();
				}
			} finally {
				authAccessLock.unlock();
			}
		}
	}

	private void override(User source, User target) {
		for (Property property : User.T.getProperties()) {
			Object value = property.get(source);

			if (value != null && property.get(target) == null)
				property.set(target, value);
		}
	}
}
