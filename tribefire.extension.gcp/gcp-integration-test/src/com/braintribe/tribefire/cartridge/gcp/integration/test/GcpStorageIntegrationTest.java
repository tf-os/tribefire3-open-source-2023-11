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
package com.braintribe.tribefire.cartridge.gcp.integration.test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.smood.SmoodAccess;
import com.braintribe.model.deployment.Cartridge;
import com.braintribe.model.extensiondeployment.meta.BinaryProcessWith;
import com.braintribe.model.gcp.deployment.GcpConnector;
import com.braintribe.model.gcp.deployment.GcpStorageBinaryProcessor;
import com.braintribe.model.gcp.resource.GcpStorageSource;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.resource.ResourceAccess;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.product.rat.imp.ImpException;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;
import com.braintribe.utils.IOTools;


/**
 * checks if all expected deployables are present and deployed, as well as expected demo entities are present
 *
 */
public class GcpStorageIntegrationTest extends AbstractTribefireQaTest {

	private static Logger log = Logger.getLogger(GcpStorageIntegrationTest.class);

	private static PersistenceGmSession userSession = null;
	private static PersistenceGmSession userSession2 = null;

	private final static String BUCKETNAME = "playground-rku";


	@BeforeClass
	public static void initialize() throws Exception {

		log.info("Making sure that all expected deployables are there and deployed...");

		ImpApi imp = apiFactory().build();

		GmMetaModel gcpUserModel = imp.model().find("tribefire.extension.gcp:gcp-user-model").orElseGet(() -> {
			return imp.model().create("tribefire.extension.gcp:gcp-user-model", "com.braintribe.gm:user-model", "tribefire.extension.gcp:gcp-model").get();
		});
		
		Cartridge cartridge = null;
		try {
			cartridge = imp.cartridge("tribefire.extension.gcp.gcp-cartridge").get();
		} catch(ImpException ie) {
			log.info("Could not find the GCP Cartridge.");
		}
		com.braintribe.model.deployment.Module module = null;
		try {
			EntityQuery query = EntityQueryBuilder.from(com.braintribe.model.deployment.Module.T).where().property(com.braintribe.model.deployment.Module.globalId).eq("module://tribefire.extension.gcp:gcp-module").done();
			module = imp.session().query().entities(query).first();
		} catch(ImpException ie) {
			log.info("Could not find the GCP Module.");
		}
		if (cartridge == null && module == null) {
			fail("Could not find a Cartridge or a Module");
		}
		
		
		
		PersistenceGmSession session = imp.session();
		
		SmoodAccess access = session.create(SmoodAccess.T);
		access.setExternalId("test.gcp.storage");
		access.setGlobalId("test.gcp.storage");
		access.setMetaModel(gcpUserModel);
		access.setName("GCP Storage Test Smood");
		
		GcpConnector connector = session.create(GcpConnector.T);
		InputStream inputStream = GcpStorageIntegrationTest.class.getClassLoader().getResourceAsStream("com/braintribe/tribefire/cartridge/gcp/integration/test/gcp-test-service-account.json");
		String jsonCredentials = IOTools.slurp(inputStream, "UTF-8");
		connector.setJsonCredentials(jsonCredentials);
		connector.setName("Test GCP Connector");
		connector.setExternalId("test.gcp.connector");
		connector.setGlobalId("test.gcp.connector");
		connector.setCartridge(cartridge);
		connector.setModule(module);
		
		GcpStorageBinaryProcessor binaryProcessor = session.create(GcpStorageBinaryProcessor.T);
		binaryProcessor.setExternalId("test.gcp.binary.processor");
		binaryProcessor.setBucketName(BUCKETNAME);
		binaryProcessor.setName("Test GCP Binary Processor");
		binaryProcessor.setConnector(connector);
		binaryProcessor.setGlobalId("test.gcp.binary.processor");
		binaryProcessor.setCartridge(cartridge);
		binaryProcessor.setModule(module);

		BinaryProcessWith binaryProcessWith = session.create(BinaryProcessWith.T);
		binaryProcessWith.setRetrieval(binaryProcessor);
		binaryProcessWith.setPersistence(binaryProcessor);
		binaryProcessWith.setGlobalId("test.gcp.binaryProcessWith");

		session.commit();

		
		BasicModelMetaDataEditor modelEditor = BasicModelMetaDataEditor.create(gcpUserModel).withSession(session).done();
	
		modelEditor.onEntityType(GcpStorageSource.T)
			.addMetaData(binaryProcessWith);
		modelEditor.onEntityType(Resource.T)
			.addMetaData(binaryProcessWith);

		session.commit();
		
		imp.deployable(connector).redeploy();
		imp.deployable(binaryProcessor).redeploy();
		imp.deployable(access).redeploy();


		userSession = imp.switchToAccess("test.gcp.storage").session();
		userSession2 = imp.switchToAccess("test.gcp.storage").session();

		log.info("Test preparation finished successfully!");
	}

	@Test
	public void testBinaryProcessor() throws Exception {

		// First, we use one session to upload a file

		Resource resource = null;
		String text = "Hello, world!";
		try (ByteArrayInputStream bais = new ByteArrayInputStream(text.getBytes("UTF-8"))) {
			ResourceAccess resources = userSession.resources();
			resource = resources.create().sourceType(GcpStorageSource.T).name("hello.txt").store(bais);
		}
		System.out.println("Stored resource "+resource);

		// Lastly, we download this resource using a different session and check the content

		EntityQuery query = EntityQueryBuilder.from(Resource.T).where().property(Resource.id).eq(resource.getId()).done();
		Resource downloadResource = userSession2.query().entities(query).unique();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (InputStream in = userSession2.resources().retrieve(downloadResource).stream()) {
			IOTools.pump(in, baos);
		}
		String downloadedText = new String(baos.toByteArray(), "UTF-8");

		assertThat(downloadedText).isEqualTo(text);
	}
}
