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
package com.braintribe.qa.tribefire.qatests.deployables.access;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.extensiondeployment.IdGenerator;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.idgendeployment.IdGeneratorAssignment;
import com.braintribe.model.idgendeployment.NumericUidGenerator;
import com.braintribe.model.idgendeployment.UuidGenerator;
import com.braintribe.model.idgendeployment.UuidMode;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.modelnotification.OnModelChanged;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.qa.tribefire.test.Child;
import com.braintribe.testing.category.KnownIssue;

/**
 * Tests NumericUidGenerator and UuidGenerator with all possible modes on a SMOOD access using the QaTest Family model
 */
@Ignore
public class IdGeneratorAspectTest extends AbstractPersistenceTest {

	PersistenceGmSessionFactory factory;
	ImpApi imp;

	NumericUidGenerator numGenerator;
	UuidGenerator uuidGenerator, uuidGeneratorCompact, uuidGeneratorCompactWithTimestamp, uuidGeneratorStandard;

	@Before
	public void init() {
		logger.info("Starting DevQA-test: assigning Id Generators...");

		imp = apiFactory().build();
		factory = apiFactory().buildSessionFactory();
		PersistenceGmSession session = imp.session();

		eraseTestEntities();

		numGenerator = session.create(NumericUidGenerator.T);
		numGenerator.setExternalId(nameWithTimestamp("numericGen"));
		numGenerator.setName("numericGen");

		uuidGenerator = session.create(UuidGenerator.T);
		uuidGenerator.setExternalId(nameWithTimestamp("uuidGen"));
		uuidGenerator.setName("uuidGen Default");

		uuidGeneratorCompact = session.create(UuidGenerator.T);
		uuidGeneratorCompact.setExternalId(nameWithTimestamp("uuidGenCompact"));
		uuidGeneratorCompact.setName("uuidGen Compact");
		uuidGeneratorCompact.setMode(UuidMode.compact);

		uuidGeneratorCompactWithTimestamp = session.create(UuidGenerator.T);
		uuidGeneratorCompactWithTimestamp.setExternalId(nameWithTimestamp("uuidGenCompactTimestamp"));
		uuidGeneratorCompactWithTimestamp.setName("uuidGen Compact Timestamp");
		uuidGeneratorCompactWithTimestamp.setMode(UuidMode.compactWithTimestampPrefix);

		uuidGeneratorStandard = session.create(UuidGenerator.T);
		uuidGeneratorStandard.setExternalId(nameWithTimestamp("uuidGenStandard"));
		uuidGeneratorStandard.setName("uuidGen Standard");
		uuidGeneratorStandard.setMode(UuidMode.standard);

		imp.commit();
		imp.service().deployRequest(numGenerator, uuidGenerator, uuidGeneratorStandard, uuidGeneratorCompact, uuidGeneratorCompactWithTimestamp)
				.callAndPrintMessages();

	}

	@After
	public void cleanUp() {
		eraseTestEntities();
	}

	/**
	 * This is the way how it should work in production. Just a redeployment of the access should switch the used id
	 * generator
	 */
	@Category(KnownIssue.class)
	// Ticket number DEVCX-1002
	@Test
	public void testWithRedeploy() {
		CollaborativeSmoodAccess familyAccess = createAndDeployFamilyAccessWithTimestamp(imp);

		// numeric id generator creates really long numbers. This is to make sure, we don't have the standard id gen
		// here
		Long generatedNumericId = testIdGen(numGenerator, Long.class, familyAccess);
		assertThat(generatedNumericId).as("Unexpected Id").isGreaterThan(100);

		// standard id generator assigns ascending integers starting with the highest current value (the long one from
		// before) + 1
		Long generatedDefaultId = testIdGen(null, Long.class, familyAccess);
		assertThat(generatedDefaultId).as("Unexpected Id").isEqualTo(generatedNumericId + 1);

		// numeric id generator again
		// make sure it doesn't just increase number like default idgen does
		Long secondGeneratedNumericId = testIdGen(numGenerator, Long.class, familyAccess);
		assertThat(secondGeneratedNumericId).as("Unexpected Id").isNotEqualTo(generatedDefaultId + 1);

		// standard id generator again
		// As we already have many different ids this makes sure, the _highest_ one is increased
		Long secondGeneratedDefaultId = testIdGen(null, Long.class, familyAccess);
		assertThat(secondGeneratedDefaultId).as("Unexpected Id").isEqualTo(Math.max(generatedDefaultId, secondGeneratedNumericId) + 1);

		// hex number separated by dashes - the default option should do the same as "standard"
		String generatedHexIdDefault = testIdGen(uuidGenerator, String.class, familyAccess);
		assertStandardHexId(generatedHexIdDefault);

		// hex number separated by dashes
		String generatedHexIdStandard = testIdGen(uuidGeneratorStandard, String.class, familyAccess);
		assertStandardHexId(generatedHexIdStandard);

		// hex number without dashes
		String generatedHexIdCompact = testIdGen(uuidGeneratorCompact, String.class, familyAccess);
		assertCompactHexId(generatedHexIdCompact);

		// hex number without dashes starting with a long number
		String generatedHexIdTimestamp = testIdGen(uuidGeneratorCompactWithTimestamp, String.class, familyAccess);
		assertTimestampHexId(generatedHexIdTimestamp);

		imp.service().undeployRequest(familyAccess).callAndPrintMessages();
		imp.session().deleteEntity(familyAccess);
		imp.commit();
	}

	/**
	 * This is the "brutal" way which should always succeed. The model is created new and deleted again afterwards. This
	 * test exists to test id generator functionality even when the metadata re-assignment does not work properly
	 */
	@Test
	public void testWithResetFamilyModel() {
		// numeric id generator creates really long numbers. This is to make sure, we don't have the standard id gen
		// here
		Long generatedNumericId = testIdGenAndResetFamilyModel(numGenerator, Long.class);
		assertThat(generatedNumericId).as("Unexpected Id").isGreaterThan(100);

		// standard id generator assigns ascending integers starting with 1
		Long generatedDefaultId = testIdGenAndResetFamilyModel(null, Long.class);
		assertThat(generatedDefaultId).as("Unexpected Id").isEqualTo(1);

		// hex number separated by dashes - the default option should do the same as "standard"
		String generatedHexIdDefault = testIdGenAndResetFamilyModel(uuidGenerator, String.class);
		assertStandardHexId(generatedHexIdDefault);

		// hex number separated by dashes
		String generatedHexIdStandard = testIdGenAndResetFamilyModel(uuidGeneratorStandard, String.class);
		assertStandardHexId(generatedHexIdStandard);

		// hex number without dashes
		String generatedHexIdCompact = testIdGenAndResetFamilyModel(uuidGeneratorCompact, String.class);
		assertCompactHexId(generatedHexIdCompact);

		// hex number without dashes starting with a long number
		String generatedHexIdTimestamp = testIdGenAndResetFamilyModel(uuidGeneratorCompactWithTimestamp, String.class);
		assertTimestampHexId(generatedHexIdTimestamp);

		// standard id generator assigns ascending integers starting with 1
		// Notice, that we used it already above but as the family model was reset,
		// the idgenerator should be reset as well and again return 1
		Long secondGeneratedDefaultId = testIdGenAndResetFamilyModel(null, Long.class);
		assertThat(secondGeneratedDefaultId).as("Unexpected Id").isEqualTo(1);
	}

	/**
	 * creates a new family model and access, calls {@link #testIdGen(IdGenerator, Class, IncrementalAccess)} and cleans
	 * up again after itself
	 */
	private <T> T testIdGenAndResetFamilyModel(IdGenerator idGenerator, Class<T> idObjectClass) {
		// create a new access
		CollaborativeSmoodAccess familyAccess = createAndDeployFamilyAccessWithTimestamp(imp);

		// test
		T generatedId = testIdGen(idGenerator, idObjectClass, familyAccess);

		// remove model an access again
		eraseFamilyAccessesAndModel();

		return generatedId;
	}

	/**
	 * assigns the passed id generator to the Child entity type of the family model via meta data. then creates a new
	 * Child entity and checks if the generated Id has the passed expected class
	 */
	private <T> T testIdGen(IdGenerator idGenerator, Class<T> idObjectClass, IncrementalAccess familyAccess) {
		GmEntityType childGmType = imp.model().entityType(Child.T).get();

		EntityType<Child> childEntityType = GMF.getTypeReflection().getEntityType(childGmType.getTypeSignature());
		assertThat(childEntityType).as("Child entity type not found").isNotNull();

		// remove previous ID generator assignments
		childGmType.getMetaData().removeIf(IdGeneratorAssignment.T::isInstance);

		if (idGenerator != null) {
			logger.info("Testing " + idGenerator.getExternalId() + " and expecting an ID of type " + idObjectClass.getName());

			IdGeneratorAssignment idGenAssignment = imp.session().create(IdGeneratorAssignment.T);
			idGenAssignment.setGenerator(idGenerator);

			// add our new ID generator assignment
			childGmType.getMetaData().add(idGenAssignment);

		} else {
			logger.info("Testing default id generator and expecting an ID of type " + idObjectClass.getName());
		}
		imp.commit();

		OnModelChanged onModelChangedRequest = OnModelChanged.T.create();
		onModelChangedRequest.setModelName(childGmType.declaringModel().getName());

		imp.service(onModelChangedRequest).call();

		// redeploy access to make changes effective
		imp.service().redeployRequest(familyAccess).callAndPrintMessages();

		PersistenceGmSession familySession = factory.newSession(familyAccess.getExternalId());

		Child child = familySession.create(childEntityType);
		familySession.commit();

		Object generatedId = child.getId();
		assertThat(generatedId).as("Generated ID instance has wrong class").isOfAnyClassIn(idObjectClass);

		logger.info("Generated Id: " + generatedId);

		return (T) generatedId;
	}

	private void assertStandardHexId(String id) {
		assertThat(id).as("Unexpected Id - should be hex with dashes").matches("[0-9a-f-]+").hasSize(36);
	}

	private void assertCompactHexId(String id) {
		assertThat(id).as("Unexpected Id - should be plain hex without dashes").matches("[0-9a-f]+").hasSize(32);
	}

	private void assertTimestampHexId(String id) {
		assertThat(id).as("Unexpected Id - should be long number (timestamp) - then plain hex").matches("[0-9]{15}[0-9a-f]+").hasSize(32);
	}

}
