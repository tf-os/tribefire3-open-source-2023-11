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
package com.braintribe.model.access.security.query;

import static com.braintribe.model.access.security.testdata.acl.AclFactory.ALLOWED_ACL_USER;
import static com.braintribe.model.access.security.testdata.acl.AclFactory.UNKNOWN_ACL_USER;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.first;

import java.util.List;
import java.util.Map;

import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.access.security.testdata.acl.AclFactory;
import com.braintribe.model.access.security.testdata.acl.AclTestTools;
import com.braintribe.model.access.security.testdata.query.AclEntity;
import com.braintribe.model.access.security.testdata.query.AclPropsOwner;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.query.EntityQuery;

/**
 * The names of the tests use the same pattern as {@link Acl_DirectQueryingTests}.
 */
public class Acl_PropOwnerQueryingTests extends AbstractQueryingTest {

	@Override
	protected Map<Class<? extends GenericEntity>, TraversingCriterion> defaultTc() {
		return allTopLevelPropsTcMap();
	}

	private static final boolean ALLOW = true;
	private static final boolean DENY = false;

	private AclFactory acls;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		acls = new AclFactory(delegateSession);
	}

	private AclPropsOwner aclPropsOwner; // stores query result

	@Test
	public void anyAdmin_Admin_Allow() {
		runPropsTest(acls.create_AnyAdmin(), null, ADMIN_ROLE, ALLOW);
	}

	@Test
	public void anyAdmin_Volunteer_Deny() {
		runPropsTest(acls.create_AnyAdmin(), null, VOLUNTEER_ROLE, DENY);
	}

	@Test
	public void anyAdmin_CanAdministerHasAcl_Allow() {
		runPropsTest(acls.create_AnyAdmin(), null, ADMINISTERING_HAS_ACL_ROLE, ALLOW);
	}

	@Test
	public void justOwner_IsOwner_Allow() {
		runPropsTest(acls.create_JustOwner(), ALLOWED_ACL_USER, null, ALLOW);
	}

	@Test
	public void justOwner_NotOwner_Deny() {
		runPropsTest(acls.create_JustOwner(), UNKNOWN_ACL_USER, null, DENY);
	}

	/**
	 * If the ACL implementation relied on the underlying access to fill the ACL properties, using this TC we would not
	 * load them, thus having <code>"acl" = "owner" = null</code> and so the <tt>AclEntity.single</tt> property would be
	 * loaded.
	 */
	@Test
	public void justOwner_NotOwner_Deny_WhenTcOverridden() {
		runPropsTest(acls.create_JustOwner(), UNKNOWN_ACL_USER, null, DENY, queryPropsOwner_TcSaysNoAcl());
	}

	@Test
	public void justOwner_IsOwner_Allow_WhenTcOverridden() {
		runPropsTest(acls.create_JustOwner(), ALLOWED_ACL_USER, null, ALLOW, queryPropsOwner_TcSaysNoAcl());

		// assert that the TC is still respected
		AclEntity single = aclPropsOwner.getSingle();
		single.detach();

		Assertions.assertThat(single.getOwner()).isNull();

	}

	private EntityQuery queryPropsOwner_TcSaysNoAcl() {
		return EntityQueryBuilder.from(AclPropsOwner.T).tc(AclTestTools.IGNORE_ACL_PROPS).done();
	}

	@Test
	public void ownerOrAdmin_IsOwner_Allow() {
		runPropsTest(acls.create_ownerOrAdmin(), ALLOWED_ACL_USER, null, ALLOW);
	}

	@Test
	public void ownerOrAdmin_IsAdmin_Allow() {
		runPropsTest(acls.create_ownerOrAdmin(), null, ADMIN_ROLE, ALLOW);
	}

	@Test
	public void ownerOrAdmin_JustSomeVolunteer_Deny() {
		runPropsTest(acls.create_ownerOrAdmin(), UNKNOWN_ACL_USER, VOLUNTEER_ROLE, DENY);
	}

	private void runPropsTest(AclEntity aclEntity, String userName, String userRole, boolean expectAllow) {
		EntityQuery query = EntityQueryBuilder.from(AclPropsOwner.T).done();
		runPropsTest(aclEntity, userName, userRole, expectAllow, query);
	}

	private void runPropsTest(AclEntity aclEntity, String userName, String userRole, boolean expectAllow, EntityQuery query) {
		fillPropsWith(aclEntity);

		if (userName != null)
			setUserName(userName);
		if (userRole != null)
			setUserRoles(userRole);

		execute(query);

		if (expectAllow)
			assertAllowed();
		else
			assertDenied();
	}

	/**
	 * There was a bug where property operands were not handled correctly for entity queries (the default source, which
	 * is created while processing the query, made it's way to the modified query).
	 * <p>
	 * This also applies to {@link #conditionOnAclProperty_NoResultWhenDenied()}
	 */
	@Test
	public void conditionOnAclProperty_ResultWhenAllowed() {
		runConditionOnAcl(ADMIN_ROLE, 1);
	}

	@Test
	public void conditionOnAclProperty_NoResultWhenDenied() {
		runConditionOnAcl(VOLUNTEER_ROLE, 0);
	}

	private void runConditionOnAcl(String role, int expectedResults) {
		fillPropsWith(acls.create_AdminNotVolunteer("ID-1"));

		setUserRoles(role);

		// As the single property is not visible for USER_ROLE, the whole query returns an empty result.
		EntityQuery query = EntityQueryBuilder.from(AclPropsOwner.T).where().property("single.id").eq("ID-1").done();
		assertReturnedEntities(query, expectedResults);
	}

	private void fillPropsWith(AclEntity aclEntity) {
		AclPropsOwner propsOwner = delegateSession.create(AclPropsOwner.T);
		propsOwner.setSingle(aclEntity);
		propsOwner.setList(asList(aclEntity));
		propsOwner.setSet(asSet(aclEntity));
		propsOwner.setMap(asMap(aclEntity, aclEntity));
		commit();
	}

	private void execute(EntityQuery query) {
		List<GenericEntity> entities = assertReturnedEntities(query, 1);
		aclPropsOwner = first(entities);
	}

	private void assertAllowed() {
		Assertions.assertThat(aclPropsOwner.getSingle()).isNotNull();
		Assertions.assertThat(aclPropsOwner.getList()).hasSize(1);
		Assertions.assertThat(aclPropsOwner.getSet()).hasSize(1);
		Assertions.assertThat(aclPropsOwner.getMap()).hasSize(1);
	}

	private void assertDenied() {
		Assertions.assertThat(aclPropsOwner.getSingle()).isNull();
		Assertions.assertThat(aclPropsOwner.getList()).isEmpty();
		Assertions.assertThat(aclPropsOwner.getSet()).isEmpty();
		Assertions.assertThat(aclPropsOwner.getMap()).isEmpty();
	}

}
