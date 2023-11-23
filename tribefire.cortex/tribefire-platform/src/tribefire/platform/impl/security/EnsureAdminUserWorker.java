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
package tribefire.platform.impl.security;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.tfconstants.TribefireConstants;
import com.braintribe.model.processing.worker.api.Worker;
import com.braintribe.model.processing.worker.api.WorkerContext;
import com.braintribe.model.processing.worker.api.WorkerException;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.user.Group;
import com.braintribe.model.user.Identity;
import com.braintribe.model.user.Role;
import com.braintribe.model.user.User;
import com.braintribe.model.user.worker.UserModelCollection;
import com.braintribe.utils.RandomTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.CollectionTools2;

import tribefire.module.api.EnvironmentDenotations;

public class EnsureAdminUserWorker implements Worker {

	private static final Logger logger = Logger.getLogger(EnsureAdminUserWorker.class);

	private Supplier<PersistenceGmSession> authSessionSupplier;
	private final Set<UserModelCollection> userModelCollectionSet = new HashSet<>();
	private Marshaller jsonMarshaller;
	private final ReentrantLock lock = new ReentrantLock();
	private EnvironmentDenotations environmentDenotations;

	@Required
	public void setEnvironmentDenotations(EnvironmentDenotations environmentDenotations) {
		this.environmentDenotations = environmentDenotations;
	}

	@Override
	public GenericEntity getWorkerIdentification() {
		User user = User.T.create();
		user.setId("EnsureAdminUserWorker");
		return user;
	}

	@Override
	public void start(WorkerContext workerContext) throws WorkerException {

		lock.lock();
		try {
			userModelCollectionSet.clear();

			Map<String, GenericEntity> map = environmentDenotations.find("tribefire\\-admin\\-user\\-.*");
			logger.debug(() -> "Found the following extended environment denotations: " + map.keySet());

			GenericEntity adminUserCollection = environmentDenotations.lookup("tribefire-admin-user");
			if (adminUserCollection == null) {
				UserModelCollection auc = createAdminUserModelCollection();
				if (auc != null) {
					userModelCollectionSet.add(auc);
				}
			} else {
				map.put("tribefire-admin-user", adminUserCollection);
			}

			UserModelCollection envUmc = readUserCollectionFromEnvironment();
			if (envUmc != null) {
				userModelCollectionSet.add(envUmc);
			}

			for (Map.Entry<String, GenericEntity> entry : map.entrySet()) {

				String bindId = entry.getKey();
				GenericEntity userCollection = entry.getValue();

				if (userCollection instanceof User) {

					User adminUser = (User) userCollection;

					logger.debug(() -> "A single user has been defined in [" + bindId + "] to be ensured: " + adminUser.getName());

					Set<Role> configuredRoles = adminUser.getRoles();
					if (configuredRoles.isEmpty()) {
						Role r = Role.T.create();
						r.setName("tf-admin");
						adminUser.getRoles().add(r);
					}

					UserModelCollection userModelCollection = UserModelCollection.T.create();
					userModelCollection.getIdentities().add(adminUser);
					userModelCollectionSet.add(userModelCollection);

				} else if (userCollection instanceof UserModelCollection) {

					logger.debug(() -> "A UserModelCollection has been defined in [" + bindId + "] to be ensured.");

					UserModelCollection userModelCollection = (UserModelCollection) userCollection;
					userModelCollectionSet.add(userModelCollection);

				} else {

					logger.warn(() -> "Unsupported user-collection type " + userCollection + " in [" + bindId + "]");

				}

			}

			if (userModelCollectionSet.isEmpty()) {
				logger.debug(() -> "No user set. Not ensuring that it exists in the auth access.");
				return;
			}

			this.overridePasswordFromEnvironment();

		} finally {
			lock.unlock();
		}

		logger.debug(() -> "Submitting worker task to ensure that initial user/group/roles exist in the auth access.");
		workerContext.submit(this::ensure);

	}

	private UserModelCollection readUserCollectionFromEnvironment() {
		String json = TribefireRuntime.getProperty("TRIBEFIRE_AUTH_USER_COLLECTION_JSON");
		if (json != null) {
			try {
				UserModelCollection umc = (UserModelCollection) jsonMarshaller.unmarshall(new ByteArrayInputStream(json.getBytes("UTF-8")));
				return umc;
			} catch (Exception e) {
				logger.warn(() -> "Could not decode content of TRIBEFIRE_AUTH_USER_COLLECTION_JSON: " + json, e);
			}
		} else {
			logger.debug(() -> "TRIBEFIRE_AUTH_USER_COLLECTION_JSON is not set.");
		}
		return null;
	}

	private void overridePasswordFromEnvironment() {

		String envUsername = TribefireRuntime.getProperty("TRIBEFIRE_AUTH_ADMIN_USERNAME");
		if (StringTools.isEmpty(envUsername)) {
			return;
		}
		String envPassword = TribefireRuntime.getProperty("TRIBEFIRE_AUTH_ADMIN_PASSWORD");
		if (StringTools.isEmpty(envPassword)) {
			return;
		}

		userModelCollectionSet.forEach(c -> {
			c.getIdentities().stream().filter(i -> i instanceof User).map(i -> (User) i).forEach(u -> {
				String userName = u.getName();
				if (userName.equalsIgnoreCase(envUsername)) {
					String currentPwd = u.getPassword();
					if (StringTools.isBlank(currentPwd) || !currentPwd.equals(envPassword)) {
						logger.debug(
								() -> "Setting/overriding password " + StringTools.simpleObfuscatePassword(envPassword) + " for user " + envUsername);
						u.setPassword(envPassword);
					}
				}
			});
		});
	}

	protected UserModelCollection createAdminUserModelCollection() {

		String username = TribefireRuntime.getProperty("TRIBEFIRE_AUTH_ADMIN_USERNAME", TribefireConstants.USER_CORTEX_NAME);

		logger.debug(() -> "The admin user " + username + " may have to be ensured.");

		String password = TribefireRuntime.getProperty("TRIBEFIRE_AUTH_ADMIN_PASSWORD");
		if (StringTools.isBlank(password) && username.equalsIgnoreCase(TribefireConstants.USER_CORTEX_NAME)) {
			password = TribefireConstants.USER_CORTEX_DEFAULT_PASSWORD;
		}
		if (!StringTools.isBlank(password)) {

			String rolesString = TribefireRuntime.getProperty("TRIBEFIRE_AUTH_ADMIN_ROLES");
			Set<String> roles = null;
			if (!StringTools.isBlank(rolesString)) {
				String[] rolesArray = StringTools.splitCommaSeparatedString(rolesString, true);
				if (rolesArray != null && rolesArray.length > 0) {
					roles = new HashSet<>();
					for (String role : rolesArray) {
						roles.add(role);
					}
				}
			} else {
				roles = CollectionTools2.asSet("tf-admin");
			}

			User adminUser = User.T.create();
			adminUser.setName(username);
			adminUser.setPassword(password);

			for (String role : roles) {
				Role r = Role.T.create();
				r.setName(role);
				adminUser.getRoles().add(r);
			}

			UserModelCollection userModelCollection = UserModelCollection.T.create();
			userModelCollection.getIdentities().add(adminUser);

			if (logger.isDebugEnabled())
				logger.debug("The admin user " + username + " (pw: " + StringTools.simpleObfuscatePassword(password) + ") may have to be ensured.");

			return userModelCollection;
		}

		return null;
	}

	@Override
	public void stop(WorkerContext workerContext) throws WorkerException {
		// Nothing to do
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	protected void ensure() {

		lock.lock();
		try {
			if (userModelCollectionSet.isEmpty()) {
				return;
			}

			PersistenceGmSession session = authSessionSupplier.get();

			String enforcePasswordString = TribefireRuntime.getProperty("TRIBEFIRE_AUTH_ADMIN_ENFORCEPASSWORD", "false");
			final boolean enforcePassword = enforcePasswordString.equalsIgnoreCase("true");

			for (UserModelCollection userModelCollection : userModelCollectionSet) {

				List<Identity> identities = userModelCollection.getIdentities();
				if (identities != null) {
					logger.debug(() -> "Identities to ensure: " + identities.size());

					for (Identity identity : identities) {
						if (identity != null) {
							if (identity instanceof User) {
								ensureUser((User) identity, enforcePassword, session);
							} else if (identity instanceof Group) {
								ensureGroup((Group) identity, enforcePassword, session);
							} else {
								logger.warn("Could not handle identity " + identity);
							}
						}
					}
				}
				Set<Role> roles = userModelCollection.getRoles();
				logger.debug(() -> "Roles to ensure: " + identities.size());
				ensureRoles(roles, session);

			}
		} finally {
			lock.unlock();
		}
	}

	private Set<Role> ensureRoles(Set<Role> templateRoles, PersistenceGmSession session) {

		if (templateRoles == null || templateRoles.isEmpty()) {
			return new HashSet<>();
		}

		Set<String> rolesToSet = templateRoles.stream().map(r -> r.getName()).collect(Collectors.toSet());
		Set<Role> result = new HashSet<>();

		if (!rolesToSet.isEmpty()) {

			EntityQuery rolesQuery = EntityQueryBuilder.from(Role.T).where().property(Role.name).in(rolesToSet).done();
			List<Role> existingRoles = session.query().entities(rolesQuery).list();
			Set<String> newRoles = new HashSet<>(rolesToSet);
			if (existingRoles != null) {
				existingRoles.forEach(er -> {

					logger.debug(() -> "Role " + er.getName() + " already exists. No need to create it.");

					newRoles.remove(er.getName());
					result.add(er);
				});
			}

			logger.debug(() -> "New roles to create: " + newRoles);

			newRoles.forEach(r -> {

				logger.debug(() -> "Creating new role " + r);

				Role role = session.create(Role.T);
				role.setId(RandomTools.newStandardUuid());
				role.setName(r);
				result.add(role);
			});

			session.commit();
		}

		return result;
	}

	private Group ensureGroup(Group identity, boolean enforcePassword, PersistenceGmSession session) {

		String groupName = identity.getName();

		EntityQuery query = EntityQueryBuilder.from(Group.T).where().property(Group.name).eq(groupName).done();
		Group group = session.query().entities(query).first();
		if (group == null) {

			logger.debug(() -> "Group " + groupName + " not found. Creating it now.");

			group = session.create(Group.T);
			group.setId(RandomTools.newStandardUuid());
			group.setName(groupName);

			session.commit();

		} else {
			logger.debug(() -> "Group " + groupName + " already exists.");
		}

		Set<Role> roles = ensureRoles(identity.getRoles(), session);
		group.getRoles().addAll(roles);

		session.commit();

		Set<User> templateUsers = identity.getUsers();
		for (User templateUser : templateUsers) {
			User user = ensureUser(templateUser, enforcePassword, session);
			group.getUsers().add(user);
		}

		session.commit();

		return group;
	}

	private User ensureUser(User identity, boolean enforcePassword, PersistenceGmSession session) {

		String username = identity.getName();

		EntityQuery query = EntityQueryBuilder.from(User.T).where().property(User.name).eq(username).done();
		User user = session.query().entities(query).first();
		String password = identity.getPassword();

		if (user == null) {

			logger.debug(() -> "User " + username + " not found. Creating it now (password: " + StringTools.simpleObfuscatePassword(password) + ").");

			user = session.create(User.T);
			user.setId(RandomTools.newStandardUuid());
			user.setName(username);
			user.setPassword(password);
			String firstName = identity.getFirstName();
			if (firstName == null) {
				firstName = username.substring(0, 1).toUpperCase().concat(".");
			}
			user.setFirstName(firstName);
			String lastName = identity.getLastName();
			if (lastName == null) {
				lastName = StringTools.capitalize(username);
			}
			user.setLastName(lastName);

			user.getRoles().addAll(ensureRoles(identity.getRoles(), session));

			session.commit();

		} else {

			if (enforcePassword) {
				logger.debug(() -> "User " + username + " already exists. Enforcing the password (" + StringTools.simpleObfuscatePassword(password)
						+ ") and ensuring the admin role.");

				user.setPassword(password);

				user.getRoles().addAll(ensureRoles(identity.getRoles(), session));

			} else {
				logger.debug(() -> "User " + username + " already exists. Nothing to do.");
			}

		}

		Set<Group> templateGroups = identity.getGroups();
		for (Group templateGroup : templateGroups) {
			Group userGroup = ensureGroup(templateGroup, enforcePassword, session);
			if (userGroup != null) {
				user.getGroups().add(userGroup);
				userGroup.getUsers().add(user);
			}
		}

		session.commit();

		return user;
	}

	@Configurable
	@Required
	public void setAuthSessionSupplier(Supplier<PersistenceGmSession> authSessionSupplier) {
		this.authSessionSupplier = authSessionSupplier;
	}
	@Configurable
	@Required
	public void setJsonMarshaller(Marshaller jsonMarshaller) {
		this.jsonMarshaller = jsonMarshaller;
	}

}
