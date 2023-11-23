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
package tribefire.extension.demo.test.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.constraint.Unique;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.webrpc.client.ErroneusMultipartDataCapture;
import com.braintribe.model.processing.webrpc.client.RemoteCapture;
import com.braintribe.model.processing.webrpc.client.RemoteCaptureAspect;
import com.braintribe.product.rat.imp.impl.utils.QueryHelper;
import com.braintribe.testing.internal.suite.crud.tests.CreateEntitiesTest;
import com.braintribe.testing.internal.suite.crud.tests.MandatoryPropertyTest;
import com.braintribe.testing.internal.suite.crud.tests.UpdateToNullTest;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.collection.impl.AttributeContexts;

import tribefire.extension.demo.model.data.Department;
import tribefire.extension.demo.test.integration.utils.AbstractDemoTest;

/**
 * Generically loads all entity types directly declared by the demo-model and checks if create, read & update
 * functions for the DemoAccess
 *
 * for more details check log output or log.info lines in testCRUD method
 *
 * @author Neidhart
 *
 */
public class CRUDTest extends AbstractDemoTest {

	private QueryHelper queryHelper;

	@Before
	public void initLocal() {
		queryHelper = new QueryHelper(demoAccessSession);
	}

	/**
	 * skips inherited properties except when they are mandatory skips unique types as well as they are not handled
	 * correctly at the moment skips "approval" because respective property has an action that will throw an error
	 *
	 */
	private boolean customSkipPropertyPredicate(Property property, GenericEntity entity, PersistenceGmSession session) {
		PropertyMdResolver propertyMeta = session.getModelAccessory().getMetaData().entity(entity).property(property);

		boolean relevantType = property.getDeclaringType().equals(entity.entityType()) || propertyMeta.is(Mandatory.T);

		boolean problematicType = property.getName().equals("approvalStatus") || propertyMeta.is(Unique.T);
		return relevantType && !problematicType;
	}

	@Test
	public void testCRUD() {
		logger.info("Testing CRUD actions for all entities of the demo model...");
		logger.info("Initializing...");
		
		AttributeContext context = AttributeContexts.peek().derive() //
				.set(RemoteCaptureAspect.class, RemoteCapture.response) //
				.set(ErroneusMultipartDataCapture.class, ip -> {
			String filename = "capture/mp-capture-testCRUD-" + UUID.randomUUID().toString() + ".txt";
			File file = new File(filename);
			file.getParentFile().mkdirs();
			try (OutputStream out = new FileOutputStream(file); InputStream in = ip.openInputStream()) {
				IOTools.transferBytes(in, out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).build();

		AttributeContexts.push(context);

		try {
			doTest();
		} finally {
			AttributeContexts.pop();
		}


	}

	private void doTest() {
		String testedAccessId = demoAccessSession.getAccessId();

		GmMetaModel unconfiguredDemoModel = configuredDemoModel.getDependencies().get(0);
		Set<EntityType<?>> testedEntityTypes = new HashSet<>();
		testedEntityTypes.addAll(unconfiguredDemoModel.entityTypes().map(x -> GMF.getTypeReflection().getEntityType(x.getTypeSignature()))
				.collect(Collectors.toSet()));

		logger.info("Start Tests...");
		logger.info("1) Create all possible entities of the demo model...");
		logger.info("&) Read: check if they were created correctly...");

		CreateEntitiesTest createEntitiesTest = new CreateEntitiesTest(testedAccessId, globalCortexSessionFactory);
		createEntitiesTest.setTestedEntityTypes(testedEntityTypes);
		createEntitiesTest.setFilterPredicate(this::customSkipPropertyPredicate);

		Collection<GenericEntity> createdNow = createEntitiesTest.start();
		logger.info("Number of tested entities: " + createdNow.size());

		logger.info("2) Update: update Department entity...");
		testUpdate();

		logger.info("3) Testing update generically for all entities: set all nullable properties to null...");
		UpdateToNullTest updateToNullTest = new UpdateToNullTest(testedAccessId, globalCortexSessionFactory);
		updateToNullTest.setEntitiesToTest(createdNow);
		updateToNullTest.setFilterPredicate(this::customSkipPropertyPredicate);
		int numUpdatedEntities = updateToNullTest.start().size();
		logger.info("Succeeded! Num tested entities: " + numUpdatedEntities);

		logger.info("4) looking up all Mandatory properties and try to set them null...");
		MandatoryPropertyTest mandatoryTest = new MandatoryPropertyTest(testedAccessId, globalCortexSessionFactory);
		mandatoryTest.start();
		Set<String> mpNames = mandatoryTest.getMandatoryPropertyNames();
		String mpNamesPretty = mpNames.stream().reduce("", (old, next) -> old + "\n" + next);
		logger.info("Cool! the 'Mandatory' metadata was respected throughout. Names of found mandatory properties:\n" + mpNamesPretty);

		logger.info("Test finished successfully");
	}

	// hardcoded updating via api
	private void testUpdate() {
		final String departmentNewName = "Updated Department";

		AttributeContext context = AttributeContexts.peek().derive().set(ErroneusMultipartDataCapture.class, ip -> {
			String filename = "capture/mp-capture-testCRUD-" + UUID.randomUUID().toString() + ".txt";
			System.out.println("ErroneusMultipartDataCapture to: " + filename);
			logger.error("ErroneusMultipartDataCapture to: " + filename);
			File file = new File(filename);
			file.getParentFile().mkdirs();
			try (OutputStream out = new FileOutputStream(file); InputStream in = ip.openInputStream()) {
				IOTools.transferBytes(in, out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).build();

		AttributeContexts.push(context);

		try {
			Department department = queryHelper.findAny(Department.T);
			department.setName(departmentNewName);
			demoAccessSession.commit();

			logger.info("checking if the changes are persisted...");
			Department updatedDepartment = queryHelper.findById(Department.T, department.getId());
			assertThat(updatedDepartment.getName()).as("Failed to update Department").isEqualTo(department.getName()).isEqualTo(departmentNewName);

		} finally {
			AttributeContexts.pop();
		}

	}

}
