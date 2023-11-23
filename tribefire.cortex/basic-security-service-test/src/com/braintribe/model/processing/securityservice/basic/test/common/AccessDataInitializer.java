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
package com.braintribe.model.processing.securityservice.basic.test.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.user.Group;
import com.braintribe.model.user.Role;
import com.braintribe.model.user.User;

public class AccessDataInitializer implements LifecycleAware {

	private PersistenceGmSession authGmSession;
	private PersistenceGmSession clientsGmSession;

	private Map<String, Group> groups = new HashMap<String, Group>();
	private Map<String, Role> roles = new HashMap<String, Role>();
	private Map<String, User> users = new HashMap<String, User>();
	private Map<String, Set<String>> expectedEffectiveRoles = new HashMap<String, Set<String>>();

	public AccessDataInitializer() {
	}

	@Required
	public void setAuthGmSession(PersistenceGmSession persistenceGmSession) {
		authGmSession = persistenceGmSession;
	}

	@Required
	public void setClientsGmSession(PersistenceGmSession clientsGmSession) {
		this.clientsGmSession = clientsGmSession;
	}

	@Override
	public void postConstruct() {
		persistTestData();
	}

	@Override
	public void preDestroy() {
		deleteTestData();
	}

	/**
	 * Returns the expected effective roles for a given user identified by {@code userId}
	 * 
	 * @return the expectedEffectiveRoles
	 */
	public Set<String> expectedEffectiveRoles(String userId) {
		return expectedEffectiveRoles.get(userId);
	}

	public User getUser(String userId) {
		return users.get(userId);
	}

	public Set<String> getUserIds() {
		return users.keySet();
	}

	/**
	 * Populates the {@link #expectedEffectiveRoles} Map with the expected effective roles for the given {@code user}
	 */
	private void collectEffectiveRoles(User user) {

		Set<String> effectiveRoles = new HashSet<String>();

		if (user.getRoles() != null) {
			for (Role role : user.getRoles()) {
				effectiveRoles.add(role.getName());
			}
		}

		if (user.getGroups() != null) {
			for (Group group : user.getGroups()) {
				if (group.getRoles() != null) {
					for (Role role : group.getRoles()) {
						effectiveRoles.add(role.getName());
					}
				}
				// add group as dynamic role
				effectiveRoles.add("$group-" + group.getName());
			}
		}

		// add remaining dynamic roles
		effectiveRoles.add("$user-" + user.getName());
		effectiveRoles.add("$all");

		expectedEffectiveRoles.put(user.getName(), effectiveRoles);
	}

	private void persistTestData() {
		createGroups();
		createRoles();
		createUsers();
		commitGmSessions();
	}

	private void deleteTestData() {
		deleteUsers();
		deleteRoles();
		deleteGroups();
		commitGmSessions();
	}

	private void commitGmSessions() {
		try {
			authGmSession.commit();
		} catch (GmSessionException e) {
			throw new RuntimeException("Unable to commit auth session: " + e.getMessage(), e);
		}
		try {
			clientsGmSession.commit();
		} catch (GmSessionException e) {
			throw new RuntimeException("Unable to commit clients session: " + e.getMessage(), e);
		}
	}

	private void createGroups() {
		createGroup("admins");
		createGroup("operators");
		createGroup("guests");
	}

	private void createGroup(String name) {
		Group group = authGmSession.create(Group.T);
		group.setId(UUID.randomUUID().toString());
		group.setName(name);
		groups.put(name, group);
	}

	private void createRoles() {
		createRole("tf-admin");
		createRole("admin.role.a", "admins");
		createRole("admin.role.a", "admins");
		createRole("admin.role.b", "admins");
		createRole("admin.role.c", "admins");
		createRole("operator.role.a", "operators");
		createRole("operator.role.b", "operators");
		createRole("operator.role.c", "operators");
		createRole("guest.role.a", "guests");
		createRole("guest.role.b", "guests");
		createRole("guest.role.c", "guests");
		createRole("john.smith.role.a", null);
		createRole("john.smith.role.b", null);
		createRole("john.smith.role.c", null);
		createRole("mary.williams.role.a", null);
		createRole("mary.williams.role.b", null);
		createRole("mary.williams.role.c", null);
		createRole("robert.taylor.role.a", null);
		createRole("robert.taylor.role.b", null);
		createRole("robert.taylor.role.c", null);
		createRole("steven.brown.role.a", null);
		createRole("steven.brown.role.b", null);
		createRole("steven.brown.role.c", null);
	}

	private void createRole(String name, String groupId) {
		Role role = authGmSession.create(Role.T);
		role.setId(UUID.randomUUID().toString());
		role.setName(name);
		addRoleToGroup(role, groups.get(groupId));
		roles.put(name, role);
	}

	private void createRole(String name) {
		Role role = authGmSession.create(Role.T);
		role.setId(UUID.randomUUID().toString());
		role.setName(name);
		roles.put(name, role);
	}

	private static void addRoleToGroup(Role role, Group group) {
		if (group == null)
			return;
		if (group.getRoles() == null)
			group.setRoles(new HashSet<Role>());
		group.getRoles().add(role);
	}

	private static void addRoleToUser(Role role, User user) {
		if (user.getRoles() == null)
			user.setRoles(new HashSet<Role>());
		user.getRoles().add(role);
	}

	private static void addGroupToUser(Group group, User user) {
		if (user.getGroups() == null)
			user.setGroups(new HashSet<Group>());
		if (group.getUsers() == null)
			group.setUsers(new HashSet<User>());
		user.getGroups().add(group);
		group.getUsers().add(user);
	}

	private void createUsers() {
		createUser("cortex", "", "Cortex", "cortex", new String[] {}, new String[] { "tf-admin" });
		createUser("john.smith", "John", "Smith", "1111", new String[] { "admins", "operators", "guests" }, "john.smith.role.a", "john.smith.role.b",
				"john.smith.role.c");
		createUser("mary.williams", "Mary", "Williams", "2222", new String[] { "operators", "guests" }, "mary.williams.role.a",
				"mary.williams.role.b", "mary.williams.role.c");
		createUser("robert.taylor", "Robert", "Taylor", "3333", new String[] { "guests" }, "robert.taylor.role.a", "robert.taylor.role.b",
				"robert.taylor.role.c");
		createUser("steven.brown", "Steven", "Brown", "4444", new String[] {}, "steven.brown.role.a", "steven.brown.role.b", "steven.brown.role.c");
	}

	private void createUser(String name, String firstName, String lastName, String password, String[] groupIds, String... roleIds) {
		User user = authGmSession.create(User.T);
		user.setId(UUID.randomUUID().toString());
		user.setName(name);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setPassword(password);
		user.setEmail(name + "@braintribe.com");
		user.setRoles(new HashSet<Role>());

		for (String groupId : groupIds) {
			addGroupToUser(groups.get(groupId), user);
		}
		for (String roleId : roleIds) {
			addRoleToUser(roles.get(roleId), user);
		}

		users.put(name, user);

		collectEffectiveRoles(user);
	}

	private void deleteAll(PersistenceGmSession gmSession, Class<? extends GenericEntity> entityClass) {
		try {
			List<? extends GenericEntity> entities = gmSession.query().entities(EntityQueryBuilder.from(entityClass).done()).list();
			for (GenericEntity entity : entities) {
				gmSession.deleteEntity(entity);
			}
		} catch (GmSessionException e) {
			throw new RuntimeException("Unable to clean up test data: " + e.getMessage(), e);
		}
	}

	private void deleteUsers() {
		deleteAll(authGmSession, User.class);
	}

	private void deleteRoles() {
		deleteAll(authGmSession, Role.class);
	}

	private void deleteGroups() {
		deleteAll(authGmSession, Group.class);
	}

}
