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
package com.braintribe.model.processing.deployment.access;

import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.testing.model.test.technical.features.security.ConstrainedEntity;
import com.braintribe.testing.model.test.technical.features.security.InvisibleEntity;
import com.braintribe.testing.model.test.technical.features.security.UninstantiableEntity;

/**
 * 
 */
public class ExpressiveBasicSecurityTest extends AbstractAccessTest {

	public static void main(String[] args) {
		accessId = "PeterAccess";

		new ExpressiveBasicSecurityTest().run();
	}

	private void run() {
		// createInstances();

		testUniqueViolation();
		testMandatoryViolation();
		testInstantiationDisabledViolation();

		listInstances();

		// deleteEverything();
	}

	protected void createInstances() {
		PersistenceGmSession session = createNewSession();

		InvisibleEntity invisible = session.create(InvisibleEntity.T);
		invisible.setName("INVISIBLE");

		ConstrainedEntity constrained = newConstrainedEntity(session);
		constrained.setName("Chocolate Factory");
		constrained.setMandatoryField("mandatory");
		constrained.setUniqueField("unique");
		constrained.setInvisibleField("THIS SHOULD NOT BE VISIBLE");
		constrained.setInvisibleEntityField(invisible);

		commit(session);
	}

	protected void testUniqueViolation() {
		{
			PersistenceGmSession session = createNewSession();

			ConstrainedEntity c1 = newConstrainedEntity(session);
			c1.setUniqueField("unique1");

			ConstrainedEntity c2 = newConstrainedEntity(session);
			c2.setUniqueField("unique1");

			assertFailedCommit("Unique key constraint should have been violated.", session);
		}
		{
			PersistenceGmSession session = createNewSession();

			if (queryEntityByProperty(session, ConstrainedEntity.class, "uniqueField", "unique2") == null) {
				ConstrainedEntity c1 = newConstrainedEntity(session);
				c1.setUniqueField("unique2");

				commit(session);

				session = createNewSession();
			}
			ConstrainedEntity c2 = newConstrainedEntity(session);
			c2.setUniqueField("unique2");

			assertFailedCommit("Unique key constraint should have been violated.", session);
		}
	}

	private void testMandatoryViolation() {
		PersistenceGmSession session = createNewSession();
		ConstrainedEntity c = newConstrainedEntity(session);
		c.setMandatoryField(null);
		assertFailedCommit("Mandatory property constraint should have been violated.", session);
	}

	protected void testInstantiationDisabledViolation() {
		PersistenceGmSession session = createNewSession();
		session.create(UninstantiableEntity.T);
		assertFailedCommit("Instantiation disabled constraint should have been violated.", session);
	}

	// ############################################################################
	// ## . . . . . . . . . . . . . HELPER METHODS . . . . . . . . . . . . . . . ##
	// ############################################################################

	private ConstrainedEntity newConstrainedEntity(PersistenceGmSession session) {
		ConstrainedEntity result = session.create(ConstrainedEntity.T);
		result.setMandatoryField("mandatory");
		result.setInvisibleField("THIS SHOULD NOT BE VISIBLE");

		return result;
	}

	protected void listInstances() {
		listAllEntities(ConstrainedEntity.class);

		try {
			listAllEntities(InvisibleEntity.class);

		} catch (Exception e) {
			System.out.println("InvisibleEntity will not be shown");
			return;
		}

		throw new RuntimeException(InvisibleEntity.class.getSimpleName() + " should not be queryable");
	}

	protected void deleteEverything() {
		deleteAllEntities(ConstrainedEntity.class);
	}

}
