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
package com.braintribe.tribefire.cartridge.elasticsearch.integration.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.aspect.AspectConfiguration;
import com.braintribe.model.accessdeployment.smood.SmoodAccess;
import com.braintribe.model.deployment.Cartridge;
import com.braintribe.model.elasticsearchdeployment.ElasticsearchService;
import com.braintribe.model.elasticsearchdeployment.IndexedElasticsearchConnector;
import com.braintribe.model.elasticsearchdeployment.aspect.ExtendedFulltextAspect;
import com.braintribe.model.elasticsearchdeployment.indexing.ElasticsearchIndexingMetaData;
import com.braintribe.model.elasticsearchdeployment.indexing.ElasticsearchIndexingWorker;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.SimpleIcon;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.product.rat.imp.ImpApiFactory;
import com.braintribe.product.rat.imp.ImpException;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;
import com.braintribe.transport.http.DefaultHttpClientProvider;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;

/**
 * checks if all expected deployables are present and deployed, as well as expected demo entities are present
 *
 */
@Category(KnownIssue.class) // Not functional in the cloud
public class ElasticsearchIntegrationTest extends AbstractTribefireQaTest {

	private static Logger log = Logger.getLogger(ElasticsearchIntegrationTest.class);
	private static final String CARTRIDGE_ID = "tribefire.extension.elastic.elasticsearch-cartridge";

	private static PersistenceGmSession userSession = null;
	private static PersistenceGmSession userSession2 = null;
	private static PersistenceGmSession smoodFtSession = null;
	private static PersistenceGmSession smoodFtSession2 = null;

	private final static String PATH_IDENTIFIER = "elastic.service";
	private final static String INDEX = "users";
	private final static String FT_INDEX = "fulltext";

	private static DefaultHttpClientProvider httpClientProvider = new DefaultHttpClientProvider();

	private static String cartridgeUri;

	private static File dataPath;

	@BeforeClass
	public static void initialize() throws Exception {

		log.info("Making sure that all expected deployables are there and deployed...");

		ImpApiFactory apiFactory = apiFactory();
		ImpApi imp = apiFactory.build();

		imp.deployable(ElasticsearchService.T, "elasticsearch.service");
		imp.deployable(IndexedElasticsearchConnector.T, "indexed-elasticsearch-connector.elastic.default.elastic-templates-space.indexed-connector");
		// imp.deployable(ElasticsearchConnector.T,
		// "elasticsearch-connector.elastic.default.elastic-templates-space.connector");
		imp.deployable(ExtendedFulltextAspect.T, "extended-fulltext-aspect.elastic.default.elastic-templates-space.fulltext-aspect");
		imp.deployable(ElasticsearchIndexingWorker.T, "elasticsearch-indexing-worker.elastic.default.elastic-templates-space.worker");

		Cartridge cartridge = null;
		try {
			cartridge = imp.cartridge(CARTRIDGE_ID).get();
		} catch (ImpException ie) {
			log.info("Could not find the Elastic Cartridge.");
		}
		com.braintribe.model.deployment.Module module = null;
		try {
			EntityQuery query = EntityQueryBuilder.from(com.braintribe.model.deployment.Module.T).where()
					.property(com.braintribe.model.deployment.Module.globalId).eq("module://tribefire.extension.elastic:elasticsearch-module").done();
			module = imp.session().query().entities(query).first();
		} catch (ImpException ie) {
			log.info("Could not find the GCP Module.");
		}
		if (cartridge == null && module == null) {
			fail("Could not find a Cartridge or a Module");
		}

		if (cartridge != null) {
			cartridgeUri = cartridge.getUri();
		} else {
			cartridgeUri = apiFactory.getURL();
		}

		GmMetaModel userModel = createUserModel(imp);

		ensureService(imp);
		ensureSmoodAccessWithFulltextAspect(imp, userModel);

		// This is currently necessary because otherwise the indexing worker would not get started. This has some
		// issues, too. So I opted for the moment to disable the test instead.
		// TomcatHelper tomcatHelper = new TomcatHelper(baseUrl.substring(0, baseUrl.lastIndexOf("/")), user, password);
		// tomcatHelper.applyCommandOnContainer("stop", "tribefire-elasticsearch-cartridge");
		// tomcatHelper.applyCommandOnContainer("stop", TribefireConstants.DEFAULT_TF_SERVICESE_URL_NAME);
		// Thread.sleep(5_000L);
		// tomcatHelper.applyCommandOnContainer("start", "tribefire-elasticsearch-cartridge");
		// tomcatHelper.applyCommandOnContainer("start", TribefireConstants.DEFAULT_TF_SERVICESE_URL_NAME);

		userSession = imp.switchToAccess("demo.document.elastic.access").session();
		userSession2 = imp.switchToAccess("demo.document.elastic.access").session();
		smoodFtSession = imp.switchToAccess("test.user.smood.access").session();
		smoodFtSession2 = imp.switchToAccess("test.user.smood.access").session();

		log.info("Test preparation finished successfully!");
	}

	@AfterClass
	public static void afterClass() {
		if (dataPath != null) {
			try {
				FileTools.deleteDirectoryRecursively(dataPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void ensureSmoodAccessWithFulltextAspect(ImpApi imp, GmMetaModel userModel) throws InterruptedException {

		ExtendedFulltextAspect aspect = imp
				.deployable(ExtendedFulltextAspect.T, "extended-fulltext-aspect.elastic.default.elastic-templates-space.fulltext-aspect").get();
		IndexedElasticsearchConnector connector = aspect.getElasticsearchConnector();
		connector.setIndex(FT_INDEX);
		imp.commit();
		imp.deployable(connector).redeploy();
		imp.deployable(aspect).redeploy();

		SmoodAccess smoodAccess = imp.deployable().find(SmoodAccess.T, "test.user.smood.access").orElseGet(() -> {
			SmoodAccess sa = imp.deployable().access()
					.createSmood("Smood User Access with Elastic Fulltext support", "test.user.smood.access", userModel).get();
			imp.commit();

			imp.service().setupAspectsRequest(sa, true).call();
			AspectConfiguration aspectConfiguration = sa.getAspectConfiguration();
			assertThat(aspectConfiguration).isNotNull();

			// Get rid of aspects
			aspectConfiguration.getAspects().clear();

			aspectConfiguration.getAspects().add(aspect);

			imp.commit();

			return sa;
		});

		imp.deployable(smoodAccess).redeploy();

		ElasticsearchIndexingWorker worker = imp
				.deployable(ElasticsearchIndexingWorker.T, "elasticsearch-indexing-worker.elastic.default.elastic-templates-space.worker").get();
		assertThat(worker).isNotNull();
		worker.setAccess(smoodAccess);
		imp.commit();
		imp.deployable(worker).redeploy();

		// Allow some time for the worker to start
		Thread.sleep(2000L);
	}

	@Ignore
	private static void print(String text) {
		System.out.println(DateTools.encode(new Date(), DateTools.LEGACY_DATETIME_WITH_MS_FORMAT) + " [Master]: " + text);
	}

	@Ignore
	private static ElasticsearchIndexingMetaData indexingMetaData(ImpApi imp) {
		ElasticsearchIndexingMetaData md = imp.session().create(ElasticsearchIndexingMetaData.T);
		md.setCascade(true);
		md.setInherited(true);
		return md;
	}

	@Ignore
	static GmMetaModel createUserModel(ImpApi imp) throws Exception {
		GmMetaModel elasticsearchUserModel = imp.model().find("tribefire.extension.elastic:elasticsearch-user-model").orElseGet(() -> {
			GmMetaModel esum = imp.model().create("tribefire.extension.elastic:elasticsearch-user-model", "com.braintribe.gm:user-model").get();

			assertThat(esum).isNotNull();

			GmMetaModel iconModel = imp.model("com.braintribe.gm:icon-model").get();
			imp.model(iconModel).metaDataEditor().onEntityType(Icon.T).addPropertyMetaData(indexingMetaData(imp));
			return esum;
		});

		return elasticsearchUserModel;
	}

	@Ignore
	private static void ensureService(ImpApi imp) throws Exception {

		// File resTemp = new File("res/temp");
		// resTemp.mkdirs();
		// dataPath = File.createTempFile("elastic-data", "folder", resTemp);
		// dataPath.delete();
		// dataPath.mkdirs();
		//
		// ElasticsearchService service = imp.deployable(ElasticsearchService.T, "elasticsearch.service").get();
		// service.setPathIdentifier(PATH_IDENTIFIER);
		// service.setDataPath(dataPath.getAbsolutePath());
		// imp.deployable(service).commitAndRedeploy();

		long maxWait = Numbers.MILLISECONDS_PER_MINUTE;
		long interval = Numbers.MILLISECONDS_PER_SECOND * 5;
		long start = System.currentTimeMillis();
		boolean success = false;

		String url = cartridgeUri + "/component/" + PATH_IDENTIFIER;

		while ((System.currentTimeMillis() - start) < maxWait) {

			// Wait for a bit and then check if the service responds on the expected URL
			Thread.sleep(interval);

			try (CloseableHttpClient client = httpClientProvider.provideHttpClient()) {
				print("Checking service at " + url);
				HttpGet get = new HttpGet(url);
				try (CloseableHttpResponse response = client.execute(get)) {
					StatusLine statusLine = response.getStatusLine();
					int statusCode = statusLine.getStatusCode();

					if (statusCode == 200) {

						HttpEntity responseEntity = response.getEntity();
						String responseContent = EntityUtils.toString(responseEntity);
						EntityUtils.consume(responseEntity);

						print("Received: " + responseContent);
						assertThat(responseContent.contains("You Know, for Search")).isTrue();

						success = true;
						break;
					} else {
						print("Received the status code " + statusCode + ". Will try again.");
					}
				}
			} catch (Exception e) {
				print("Error while trying to establish a connection to the Elastic service: " + e.getMessage());
			}
		}

		assertThat(success).as("Service has not started.").isTrue();

		deleteIndex(url, INDEX);
		deleteIndex(url, FT_INDEX);
	}

	protected static void deleteIndex(String url, String index) throws IOException, ClientProtocolException, Exception {
		try (CloseableHttpClient client = httpClientProvider.provideHttpClient()) {
			String delUrl = url + "/" + index;
			print("Deleting index " + index + " at " + delUrl);
			HttpDelete del = new HttpDelete(delUrl);
			try (CloseableHttpResponse response = client.execute(del)) {
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				assertThat(statusCode == 200 || statusCode == 404).isTrue();
			}
		}
	}

	@Test
	public void testFulltext() throws Exception {

		// Upload icon + resource (not directly a resource, as we want to see cascading ft working)

		SimpleIcon simpleIcon = userSession.create(SimpleIcon.T, UUID.randomUUID().toString());
		String fileContent = "The quick brown fox jumps over the lazy dog";
		Resource r = null;
		try (ByteArrayInputStream bais = new ByteArrayInputStream(fileContent.getBytes("UTF-8"))) {
			r = userSession.resources().create().name("fox.txt").store(bais);
		}
		simpleIcon.setName("wolf");
		simpleIcon.setImage(r);
		userSession.commit();

		// Wait a bit to make sure that the fulltext index is built up
		Thread.sleep(2_000L);

		// Search resource with fulltext (both index and content)

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(Icon.T).where().fullText(null, "brown").done();

		List<Icon> list = userSession2.query().entities(inputProcessQuery).list();

		for (Icon icon : list) {
			System.out.println("Icon: Name: " + icon.getName());
		}

		assertThat(list.size()).isEqualTo(1);

	}

	@Test
	public void testFulltextAspect() throws Exception {

		// Upload icon + resource (not directly a resource, as we want to see cascading ft working)

		SimpleIcon simpleIcon = smoodFtSession.create(SimpleIcon.T, UUID.randomUUID().toString());
		String fileContent = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
		Resource r = null;
		try (ByteArrayInputStream bais = new ByteArrayInputStream(fileContent.getBytes("UTF-8"))) {
			r = smoodFtSession.resources().create().name("lorem.txt").store(bais);
		}
		simpleIcon.setName("latin");
		simpleIcon.setImage(r);
		smoodFtSession.commit();

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(Icon.T).where().fullText(null, "consectetur").done();

		Instant before = NanoClock.INSTANCE.instant();

		long maxWait = 30_000L;
		long start = System.currentTimeMillis();
		List<Icon> list = null;
		do {

			list = smoodFtSession2.query().entities(inputProcessQuery).list();

			if (list.size() == 0) {
				System.out.println("Trying to perform a fulltext search was not successful. Waiting a bit before retrying.");

				// Wait a bit to make sure that the fulltext index is built up
				Thread.sleep(2_000L);
			}

		} while (list.size() == 0 && (System.currentTimeMillis() - start) < maxWait);

		Instant after = NanoClock.INSTANCE.instant();

		for (Icon icon : list) {
			System.out.println("Icon: Name: " + icon.getName());
		}

		assertThat(list.size()).isEqualTo(1);

		System.out.println("Waited " + StringTools.prettyPrintDuration(Duration.between(before, after), true, ChronoUnit.MILLIS)
				+ " for a fulltext search result.");
	}
}
