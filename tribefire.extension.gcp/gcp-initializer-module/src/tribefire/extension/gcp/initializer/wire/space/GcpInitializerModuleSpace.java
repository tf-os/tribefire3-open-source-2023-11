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
package tribefire.extension.gcp.initializer.wire.space;

import com.braintribe.model.extensiondeployment.check.CheckBundle;
import com.braintribe.model.extensiondeployment.meta.BinaryProcessWith;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.gcp.deployment.GcpConnector;
import com.braintribe.model.gcp.deployment.GcpServiceProcessor;
import com.braintribe.model.gcp.deployment.GcpStorageBinaryProcessor;
import com.braintribe.model.gcp.deployment.HealthCheckProcessor;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.cortex.model.check.CheckCoverage;
import tribefire.cortex.model.check.CheckWeight;
import tribefire.extension.gcp.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.gcp.initializer.wire.contract.GcpInitializerModuleContract;
import tribefire.extension.gcp.initializer.wire.contract.RuntimePropertiesContract;
import tribefire.extension.gcp.templates.api.GcpBinaryProcessTemplateContext;
import tribefire.extension.gcp.templates.wire.contract.GcpTemplatesContract;

/**
 * @see GcpInitializerModuleContract
 */
@Managed
public class GcpInitializerModuleSpace extends AbstractInitializerSpace implements GcpInitializerModuleContract {

	@Import
	private GcpTemplatesContract gcpTemplates;

	@Import
	private RuntimePropertiesContract properties;

	@Import
	private ExistingInstancesContract existingInstances;

	@Override
	@Managed
	public GcpServiceProcessor serviceRequestProcessor() {
		GcpServiceProcessor bean = create(GcpServiceProcessor.T);
		bean.setModule(existingInstances.module());
		bean.setExternalId("gcp.serviceProcessor");
		bean.setName("GCP Service Processor");
		return bean;
	}

	@Override
	public GcpStorageBinaryProcessor gcpDefaultStorageBinaryProcessor() {
		GcpStorageBinaryProcessor bean = gcpTemplates.gcpStorageBinaryProcessor(defaultContext());
		return bean;
	}

	@Override
	public GcpConnector gcpDefaultConnector() {
		GcpConnector bean = gcpTemplates.connector(defaultContext());
		return bean;
	}

	@Managed
	private GcpBinaryProcessTemplateContext defaultContext() {
		GcpBinaryProcessTemplateContext context = GcpBinaryProcessTemplateContext.builder().setIdPrefix(initializerSupport.initializerId())
				.setBucketName(properties.GCP_STORAGE_BUCKETNAME()).setPathPrefix(properties.GCP_PATH_PREFIX())
				.setJsonCredentials(properties.GCP_JSON_CREDENTIALS()).setPrivateKeyId(properties.GCP_PRIVATE_KEY_ID())
				.setPrivateKey(properties.GCP_PRIVATE_KEY()).setClientId(properties.GCP_CLIENT_ID()).setClientEmail(properties.GCP_CLIENT_EMAIL())
				.setTokenServerUri(properties.GCP_TOKEN_SERVER_URI()).setProjectId(properties.GCP_PROJECT_ID())
				.setGcpModule(existingInstances.module()).setEntityFactory(super::create).build();
		return context;
	}

	@Override
	@Managed
	public CheckBundle functionalCheckBundle() {
		CheckBundle bean = create(CheckBundle.T);
		bean.setModule(existingInstances.module());
		bean.getChecks().add(healthCheckProcessor());
		bean.setName("GCP Checks");
		bean.setWeight(CheckWeight.under1s);
		bean.setCoverage(CheckCoverage.connectivity);
		bean.setIsPlatformRelevant(false);
		return bean;
	}

	@Managed
	public HealthCheckProcessor healthCheckProcessor() {
		HealthCheckProcessor bean = create(HealthCheckProcessor.T);
		bean.setModule(existingInstances.module());
		bean.setName("GCP Check Processor");
		bean.setExternalId("gcp.healthcheck");
		return bean;
	}

	@Override
	@Managed
	public BinaryProcessWith binaryProcessWith() {
		BinaryProcessWith bean = create(BinaryProcessWith.T);
		bean.setRetrieval(gcpDefaultStorageBinaryProcessor());
		bean.setPersistence(gcpDefaultStorageBinaryProcessor());
		return bean;
	}

	@Override
	@Managed
	public ProcessWith serviceProcessWith() {
		ProcessWith bean = create(ProcessWith.T);
		bean.setProcessor(serviceRequestProcessor());
		return bean;
	}
}
