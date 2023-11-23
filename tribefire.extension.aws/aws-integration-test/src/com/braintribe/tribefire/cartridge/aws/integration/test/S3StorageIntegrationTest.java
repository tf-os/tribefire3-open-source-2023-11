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
package com.braintribe.tribefire.cartridge.aws.integration.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.smood.SmoodAccess;
import com.braintribe.model.aws.deployment.S3Connector;
import com.braintribe.model.aws.deployment.S3Region;
import com.braintribe.model.aws.deployment.processor.S3BinaryProcessor;
import com.braintribe.model.aws.resource.S3Source;
import com.braintribe.model.aws.service.CreatePresignedUrlForResource;
import com.braintribe.model.aws.service.PresignedUrl;
import com.braintribe.model.extensiondeployment.meta.BinaryProcessWith;
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
import com.braintribe.transport.http.DefaultHttpClientProvider;
import com.braintribe.transport.http.util.HttpTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;

/**
 * checks if all expected deployables are present and deployed, as well as expected demo entities are present
 *
 */
public class S3StorageIntegrationTest extends AbstractTribefireQaTest {

	private static Logger log = Logger.getLogger(S3StorageIntegrationTest.class);

	private static PersistenceGmSession userSession = null;
	private static PersistenceGmSession userSession2 = null;

	private final static String BUCKETNAME = "playground-rku";

	@BeforeClass
	public static void initialize() throws Exception {

		log.info("Making sure that all expected deployables are there and deployed...");

		ImpApi imp = apiFactory().build();

		GmMetaModel awsUserModel = imp.model().find("tribefire.extension.aws:aws-user-model").orElseGet(() -> {
			return imp.model().create("tribefire.extension.aws:aws-user-model", "com.braintribe.gm:user-model", "tribefire.extension.aws:aws-model")
					.get();
		});

		com.braintribe.model.deployment.Module module = null;
		try {
			EntityQuery query = EntityQueryBuilder.from(com.braintribe.model.deployment.Module.T).where()
					.property(com.braintribe.model.deployment.Module.globalId).eq("module://tribefire.extension.aws:aws-module").done();
			module = imp.session().query().entities(query).first();
		} catch (ImpException ie) {
			log.info("Could not find the AWS Module.");
		}
		if (module == null) {
			fail("Could not the AWS Module");
		}

		PersistenceGmSession session = imp.session();

		EntityQuery query = EntityQueryBuilder.from(SmoodAccess.T).where().property(SmoodAccess.externalId).eq("test.s3.storage").done();
		SmoodAccess access = session.query().entities(query).first();
		if (access == null) {
			access = session.create(SmoodAccess.T);
			access.setExternalId("test.s3.storage");
			access.setGlobalId("test.s3.storage");
			access.setMetaModel(awsUserModel);
			access.setName("S3 Storage Test Smood");

			S3Connector connector = session.create(S3Connector.T);
			connector.setRegion(S3Region.eu_central_1);
			connector.setAwsAccessKey(AwsTestCredentials.getAccessKey());
			connector.setAwsSecretAccessKey(AwsTestCredentials.getSecretAccessKey());
			connector.setName("Test S3 Connector");
			connector.setExternalId("test.s3.connector");
			connector.setGlobalId("test.s3.connector");
			connector.setModule(module);

			S3BinaryProcessor binaryProcessor = session.create(S3BinaryProcessor.T);
			binaryProcessor.setExternalId("test.s3.binary.processor");
			binaryProcessor.setBucketName(BUCKETNAME);
			binaryProcessor.setName("Test S3 Binary Processor");
			binaryProcessor.setConnection(connector);
			binaryProcessor.setGlobalId("test.s3.binary.processor");
			binaryProcessor.setModule(module);

			BinaryProcessWith binaryProcessWith = session.create(BinaryProcessWith.T);
			binaryProcessWith.setRetrieval(binaryProcessor);
			binaryProcessWith.setPersistence(binaryProcessor);
			binaryProcessWith.setGlobalId("test.s3.binaryProcessWith");

			session.commit();

			BasicModelMetaDataEditor modelEditor = BasicModelMetaDataEditor.create(awsUserModel).withSession(session).done();

			modelEditor.onEntityType(S3Source.T).addMetaData(binaryProcessWith);
			modelEditor.onEntityType(Resource.T).addMetaData(binaryProcessWith);

			session.commit();

			imp.deployable(connector).redeploy();
			imp.deployable(binaryProcessor).redeploy();
			imp.deployable(access).redeploy();

		}

		userSession = imp.switchToAccess("test.s3.storage").session();
		userSession2 = imp.switchToAccess("test.s3.storage").session();

		log.info("Test preparation finished successfully!");
	}

	@Test
	public void testBinaryProcessor() throws Exception {

		// First, we use one session to upload a file

		Resource resource = null;
		String text = "Hello, world!";
		try (ByteArrayInputStream bais = new ByteArrayInputStream(text.getBytes("UTF-8"))) {
			ResourceAccess resources = userSession.resources();
			resource = resources.create().sourceType(S3Source.T).name("hello.txt").store(bais);
		}
		System.out.println("Stored resource " + resource);

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

	@Test
	public void testPresignedUrlGeneration() throws Exception {

		// First, we use one session to upload a file

		Resource resource = null;
		String text = "Hello, world! from " + S3StorageIntegrationTest.class.getName();
		try (ByteArrayInputStream bais = new ByteArrayInputStream(text.getBytes("UTF-8"))) {
			ResourceAccess resources = userSession.resources();
			resource = resources.create().sourceType(S3Source.T).name("hello.txt").store(bais);
		}
		System.out.println("Stored resource " + resource);

		String url = null;

		for (int i = 0; i < 10; ++i) {
			long start = System.nanoTime();
			String resourceId = resource.getId();
			CreatePresignedUrlForResource req = CreatePresignedUrlForResource.T.create();
			req.setResourceId(resourceId);
			req.setTimeToLiveInMs(5000L);
			req.setAccessId(userSession.getAccessId());

			PresignedUrl presignedUrl = req.eval(userSession).get();
			url = presignedUrl.getPreSignedUrl();
			long stop = System.nanoTime();
			long duration = (stop - start);
			Duration d = Duration.ofNanos(duration);
			System.out.println(StringTools.prettyPrintDuration(d, true, ChronoUnit.NANOS));
		}
		System.out.println(url);

		DefaultHttpClientProvider clientProvider = new DefaultHttpClientProvider();
		try (CloseableHttpClient client = clientProvider.provideHttpClient()) {
			HttpGet get = new HttpGet(url);
			try (CloseableHttpResponse response = client.execute(get)) {
				int statusCode = response.getStatusLine().getStatusCode();
				assertThat(statusCode).isGreaterThanOrEqualTo(200);
				assertThat(statusCode).isLessThan(300);
				HttpEntity entity = response.getEntity();
				try (InputStream in = entity.getContent()) {
					String content = IOTools.slurp(in, "UTF-8");
					assertThat(content).isEqualTo(text);
				}
				HttpTools.consumeResponse(url, response);
			}
		}

		// Let the link expire and check that as well
		Thread.sleep(6000L);

		try (CloseableHttpClient client = clientProvider.provideHttpClient()) {
			HttpGet get = new HttpGet(url);
			try (CloseableHttpResponse response = client.execute(get)) {
				int statusCode = response.getStatusLine().getStatusCode();
				assertThat(statusCode).isGreaterThanOrEqualTo(400);
				HttpTools.consumeResponse(url, response);
			}
		}

	}
}
