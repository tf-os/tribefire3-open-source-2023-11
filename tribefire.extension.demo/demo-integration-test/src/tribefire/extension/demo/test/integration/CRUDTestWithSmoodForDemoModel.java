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

import static org.assertj.core.api.Assertions.in;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.constraint.Unique;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.webrpc.client.ErroneusMultipartDataCapture;
import com.braintribe.model.processing.webrpc.client.RemoteCapture;
import com.braintribe.model.processing.webrpc.client.RemoteCaptureAspect;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.product.rat.imp.impl.utils.QueryHelper;
import com.braintribe.testing.internal.suite.crud.AccessTester;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.utils.lcd.CommonTools;

import tribefire.extension.demo.test.integration.utils.DemoConstants;

/**
 * Generically loads all entity types directly declared by the demo-model and checks if create, read & update functions for the DemoAccess
 *
 * for more details check log output or log.info lines in testCRUD method
 *
 * @author Neidhart
 *
 */
public class CRUDTestWithSmoodForDemoModel extends AbstractTribefireQaTest implements DemoConstants {

	private QueryHelper queryHelper;

	private PersistenceGmSession demoAccessSession;
	private PersistenceGmSessionFactory factory;
	private GmMetaModel demoModel;

	@Before
	public void initLocal() {
		logger.info("Preparing DevQa Test for tribefire demo cartridge");

		ImpApi imp = apiFactory().build();
		factory = apiFactory().buildSessionFactory();

		demoModel = imp.model(CONFIGURED_DEMO_MODEL_ID).get();

		String newAccessId = nameWithTimestamp("test");
		imp.deployable().access().createCsa(newAccessId, newAccessId, demoModel).commitAndDeploy();

		demoAccessSession = factory.newSession(newAccessId);
		queryHelper = new QueryHelper(demoAccessSession);
	}

	/**
	 * Leave for later bugfixing ...which will is never gonna happen but the idea is nice.
	 */
	private boolean customSkipPropertyPredicate(Property property, GenericEntity entity, PersistenceGmSession session) {
		PropertyMdResolver propertyMeta = session.getModelAccessory().getMetaData().entity(entity).property(property);

		boolean relevantType = property.getDeclaringType().equals(entity.entityType()) || propertyMeta.is(Mandatory.T);

		Set<String> problematicPropertyNames = CommonTools.getSet(
		// "approvalStatus",
		// "paperworkByCategory",
		// "picture",
		// "averageRevenue",
		// "anything",
		// "birthday",
		// "children",
		// "father",
		// "gender",
		// "mother",
		// "departments",
		// "employees",
		// "manager",
		// "numberOfEmployees",
		// "profitable"
		///// Mandatories ////////
		// "firstName",
		// "name",
		// "ceo",
		// "lastName",
		);

		boolean problematicType = problematicPropertyNames.contains(property.getName()) || propertyMeta.is(Unique.T);
		return relevantType && !problematicType;
	}

	@Test
	public void testCRUD() {
		logger.info("Testing CRUD actions for all entities of the demo model...");
		logger.info("Initializing...");

		String testedAccessId = demoAccessSession.getAccessId();
		AccessTester accessTester = new AccessTester(testedAccessId, factory);
		
		AttributeContext context = AttributeContexts.peek().derive() //
				.set(RemoteCaptureAspect.class, RemoteCapture.response) //
				.set(ErroneusMultipartDataCapture.class, ip -> {
			String filename = "capture/mp-capture-testCRUDwSmood-" + UUID.randomUUID().toString() + ".txt";
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
			accessTester.executeTests();
		} finally {
			AttributeContexts.pop();
		}

	}
}
