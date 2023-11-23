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
package tribefire.extension.aws.initializer.wire.space;

import com.braintribe.model.aws.deployment.S3Connector;
import com.braintribe.model.aws.deployment.processor.AwsServiceProcessor;
import com.braintribe.model.aws.deployment.processor.HealthCheckProcessor;
import com.braintribe.model.aws.deployment.processor.S3BinaryProcessor;
import com.braintribe.model.extensiondeployment.check.CheckBundle;
import com.braintribe.model.extensiondeployment.meta.BinaryProcessWith;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.meta.data.prompt.Outline;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.cortex.model.check.CheckCoverage;
import tribefire.cortex.model.check.CheckWeight;
import tribefire.extension.aws.initializer.wire.contract.AwsInitializerModuleContract;
import tribefire.extension.aws.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.aws.initializer.wire.contract.RuntimePropertiesContract;
import tribefire.extension.aws.templates.api.S3BinaryProcessTemplateContext;
import tribefire.extension.aws.templates.wire.contract.AwsTemplatesContract;

/**
 * @see AwsInitializerModuleContract
 */
@Managed
public class AwsInitializerModuleSpace extends AbstractInitializerSpace implements AwsInitializerModuleContract {

	@Import
	private AwsTemplatesContract awsTemplates;

	@Import
	private RuntimePropertiesContract properties;

	@Import
	private ExistingInstancesContract existingInstances;

	@Override
	public S3BinaryProcessor s3DefaultStorageBinaryProcessor() {
		S3BinaryProcessor bean = awsTemplates.s3StorageBinaryProcessor(defaultContext());
		bean.setModule(existingInstances.module());
		return bean;
	}

	@Override
	public S3Connector s3DefaultConnector() {
		S3Connector bean = awsTemplates.connector(defaultContext());

		return bean;
	}

	@Managed
	private S3BinaryProcessTemplateContext defaultContext() {
		//@formatter:off
		S3BinaryProcessTemplateContext context = S3BinaryProcessTemplateContext.builder().setIdPrefix(initializerSupport.initializerId())
				.setLookupFunction(super::lookup)
				.setName("Default")
				.setBucketName(properties.S3_STORAGE_BUCKETNAME())
				.setPathPrefix(properties.S3_PATH_PREFIX())
				.setAwsAccessKey(properties.S3_ACCESS_KEY())
				.setAwsSecretAccessKey(properties.S3_SECRET_ACCESS_KEY())
				.setRegion(properties.S3_REGION()).setEntityFactory(super::create)
				.setAwsModule(existingInstances.module())
				.setHttpConnectionPoolSize(properties.S3_HTTP_CONNECTION_POOL_SIZE())
				.setConnectionAcquisitionTimeout(properties.S3_CONNECTION_ACQUISITION_TIMEOUT())
				.setConnectionTimeout(properties.S3_CONNECTION_TIMEOUT())
				.setSocketTimeout(properties.S3_SOCKET_TIMEOUT())
				.setUrlOverride(properties.S3_URL_OVERRIDE())
				.setCloudFrontBaseUrl(properties.S3_CLOUDFRONT_BASE_URL())
				.setCloudFrontKeyGroupId(properties.S3_CLOUDFRONT_KEYGROUP_ID())
				.setCloudFrontPrivateKey(properties.S3_CLOUDFRONT_PRIVATE_KEY())
				.setCloudFrontPublicKey(properties.S3_CLOUDFRONT_PUBLIC_KEY())
				.build();
		//@formatter:on

		return context;
	}

	@Override
	@Managed
	public AwsServiceProcessor serviceRequestProcessor() {
		AwsServiceProcessor bean = create(AwsServiceProcessor.T);
		bean.setModule(existingInstances.module());
		bean.setExternalId("aws.serviceProcessor");
		bean.setName("AWS Service Processor");

		return bean;
	}

	@Override
	@Managed
	public CheckBundle functionalCheckBundle() {
		CheckBundle bean = create(CheckBundle.T);
		bean.setModule(existingInstances.module());
		bean.getChecks().add(healthCheckProcessor());
		bean.setName("AWS Checks");
		bean.setWeight(CheckWeight.under1s);
		bean.setCoverage(CheckCoverage.connectivity);
		bean.setIsPlatformRelevant(false);

		return bean;
	}

	@Managed
	public HealthCheckProcessor healthCheckProcessor() {
		HealthCheckProcessor bean = create(HealthCheckProcessor.T);
		bean.setModule(existingInstances.module());
		bean.setName("AWS Check Processor");
		bean.setExternalId("aws.healthcheck");

		return bean;
	}

	@Override
	@Managed
	public BinaryProcessWith binaryProcessWith() {
		BinaryProcessWith bean = create(BinaryProcessWith.T);
		bean.setRetrieval(s3DefaultStorageBinaryProcessor());
		bean.setPersistence(s3DefaultStorageBinaryProcessor());

		return bean;
	}

	@Override
	@Managed
	public ProcessWith serviceProcessWith() {
		ProcessWith bean = create(ProcessWith.T);
		bean.setProcessor(serviceRequestProcessor());

		return bean;
	}

	@Managed
	@Override
	public Outline outline() {
		Outline bean = create(Outline.T);
		return bean;
	}
}
