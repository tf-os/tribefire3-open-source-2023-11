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
package tribefire.extension.wopi.wire.space;

import java.io.File;
import java.util.Map;

import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.wopi.WopiWacClient;
import com.braintribe.model.processing.wopi.WopiWacClientImpl;
import com.braintribe.model.processing.wopi.WopiWacConnector;
import com.braintribe.model.processing.wopi.WopiWacConnectorImpl;
import com.braintribe.model.processing.wopi.app.WopiApp;
import com.braintribe.model.processing.wopi.app.WopiIntegrationExample;
import com.braintribe.model.processing.wopi.service.CleanupWopiSessionWorker;
import com.braintribe.model.processing.wopi.service.ExpireWopiSessionWorker;
import com.braintribe.model.processing.wopi.service.WacHealthCheckProcessor;
import com.braintribe.model.processing.wopi.service.WopiProcessing;
import com.braintribe.model.processing.wopi.service.WopiServiceProcessor;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.wopi.WopiMimeTypes;
import tribefire.module.wire.contract.TribefireWebPlatformContract;
import tribefire.module.wire.contract.WebPlatformResourcesContract;

/**
 *
 */
@Managed
public class DeployablesSpace implements WireSpace {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private WebPlatformResourcesContract resources;

	@Managed
	public WopiServiceProcessor wopiServiceProcessor(ExpertContext<com.braintribe.model.wopi.service.WopiServiceProcessor> context) {

		com.braintribe.model.wopi.service.WopiServiceProcessor deployable = context.getDeployable();

		File demoDocsWriteRootFolder = resources.publicResources("DemoDocsWrite/").asFile();
		File demoDocsReadRootFolder = resources.publicResources("DemoDocsRead/").asFile();
		File testDocsRootFolder = resources.publicResources("TestDocs/").asFile();

		WopiServiceProcessor bean = new WopiServiceProcessor();
		bean.setLocalSessionFactory(tfPlatform.requestUserRelated().sessionFactory());
		bean.setLocalSystemSessionFactory(tfPlatform.systemUserRelated().sessionFactory());
		bean.setDemoDocsWriteRootFolder(demoDocsWriteRootFolder);
		bean.setDemoDocsReadRootFolder(demoDocsReadRootFolder);
		bean.setTestDocsRootFolder(testDocsRootFolder);
		bean.setDeployable(deployable);
		bean.setLockManager(tfPlatform.locking().manager());
		bean.setTestDocCommand(deployable.getTestDocCommand());
		bean.setStreamPipeFactory(tfPlatform.resourceProcessing().streamPipeFactory());
		bean.setMarshaller(tfPlatform.marshalling().jsonMarshaller());

		return bean;
	}

	@Managed
	public WopiApp wopiApp(ExpertContext<com.braintribe.model.wopi.service.WopiApp> context) {
		com.braintribe.model.wopi.service.WopiApp deployable = context.getDeployable();

		// resolve expert
		WopiWacConnector wopiWacConnector = context.resolve(deployable.getWopiWacConnector(), com.braintribe.model.wopi.connector.WopiWacConnector.T);

		WopiApp bean = new WopiApp();

		bean.setRequestEvaluator(tfPlatform.requestUserRelated().evaluator());
		bean.setDeployable(deployable);
		bean.setWopiWacConnector(wopiWacConnector);
		bean.setWopiProcessing(wopiProcessing());

		return bean;
	}

	@Managed
	public WopiIntegrationExample wopiIntegrationExample(ExpertContext<com.braintribe.model.wopi.service.WopiIntegrationExample> context) {
		com.braintribe.model.wopi.service.WopiIntegrationExample deployable = context.getDeployable();

		WopiIntegrationExample bean = new WopiIntegrationExample();

		bean.setSessionSupplier(() -> tfPlatform.requestUserRelated().sessionFactory().newSession(deployable.getAccess().getExternalId()));
		bean.setDeployable(deployable);

		return bean;
	}

	@Managed
	public WopiProcessing wopiProcessing() {
		WopiProcessing bean = new WopiProcessing();
		bean.setLocalSessionFactory(tfPlatform.systemUserRelated().sessionFactory());
		bean.setLockManager(tfPlatform.locking().manager());
		bean.setStreamPipeFactory(tfPlatform.resourceProcessing().streamPipeFactory());
		return bean;
	}

	@Managed
	public WopiWacConnectorImpl wopiWacConnector(ExpertContext<? extends com.braintribe.model.wopi.connector.WopiWacConnector> context) {

		com.braintribe.model.wopi.connector.WopiWacConnector deployable = context.getDeployable();

		WopiWacClient wopiWacClient = wopiWacClient(deployable);

		WopiWacConnectorImpl bean = new WopiWacConnectorImpl();
		bean.setWopiWacClient(wopiWacClient);

		return bean;
	}

	@Managed
	public WopiWacClient wopiWacClient(com.braintribe.model.wopi.connector.WopiWacConnector deployable) {

		String wacDiscoveryEndpoint = deployable.getWacDiscoveryEndpoint();

		WopiWacClientImpl client = new WopiWacClientImpl();
		Map<String, String> mimetypeextensionmap = WopiMimeTypes.mimeTypeExtensionMap;
		client.setMimeTypeExtensionsMap(mimetypeextensionmap);
		client.setWacDiscoveryEndpoint(wacDiscoveryEndpoint);
		client.setDeployable(deployable);

		return client;
	}

	@Managed
	public WacHealthCheckProcessor wacHealthCheckProcessor(
			@SuppressWarnings("unused") ExpertContext<com.braintribe.model.wopi.service.WacHealthCheckProcessor> context) {
		WacHealthCheckProcessor bean = new WacHealthCheckProcessor();
		bean.setCortexSessionSupplier(tfPlatform.systemUserRelated().cortexSessionSupplier());
		return bean;
	}

	@Managed
	public CleanupWopiSessionWorker cleanupWopiSessionWorker(ExpertContext<com.braintribe.model.wopi.service.CleanupWopiSessionWorker> context) {

		com.braintribe.model.wopi.service.CleanupWopiSessionWorker deployable = context.getDeployable();

		CleanupWopiSessionWorker bean = new CleanupWopiSessionWorker();
		bean.setDeployable(deployable);
		bean.setSessionSupplier(() -> tfPlatform.requestUserRelated().sessionFactory().newSession(deployable.getAccess().getExternalId()));
		return bean;
	}

	@Managed
	public ExpireWopiSessionWorker expireWopiSessionWorker(ExpertContext<com.braintribe.model.wopi.service.ExpireWopiSessionWorker> context) {

		com.braintribe.model.wopi.service.ExpireWopiSessionWorker deployable = context.getDeployable();

		ExpireWopiSessionWorker bean = new ExpireWopiSessionWorker();
		bean.setDeployable(deployable);
		bean.setSessionSupplier(() -> tfPlatform.requestUserRelated().sessionFactory().newSession(deployable.getAccess().getExternalId()));
		return bean;
	}
}
