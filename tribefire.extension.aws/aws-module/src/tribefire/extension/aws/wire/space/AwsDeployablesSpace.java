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
package tribefire.extension.aws.wire.space;

import com.braintribe.logging.Logger;
import com.braintribe.model.aws.deployment.processor.S3BinaryProcessor;
import com.braintribe.model.cache.CacheOptions;
import com.braintribe.model.processing.aws.connect.S3ConnectionImpl;
import com.braintribe.model.processing.aws.connect.S3Connector;
import com.braintribe.model.processing.aws.service.AwsServiceProcessor;
import com.braintribe.model.processing.aws.service.HealthCheckProcessor;
import com.braintribe.model.processing.aws.service.S3BinaryProcessorImpl;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.utils.stream.tracking.InputStreamTracker;
import com.braintribe.utils.stream.tracking.OutputStreamTracker;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.module.wire.contract.ResourceProcessingContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class AwsDeployablesSpace implements WireSpace {

	private static Logger logger = Logger.getLogger(AwsDeployablesSpace.class);

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private ResourceProcessingContract resourceProcessing;

	@Managed
	public AwsServiceProcessor awsServiceProcessor() {
		AwsServiceProcessor bean = new AwsServiceProcessor();
		bean.setDeployRegistry(tfPlatform.deployment().deployRegistry());
		bean.setCortexSessionSupplier(tfPlatform.systemUserRelated().cortexSessionSupplier());
		bean.setSessionFactory(tfPlatform.systemUserRelated().sessionFactory());
		return bean;
	}

	@Managed
	public S3BinaryProcessorImpl binaryProcessor(ExpertContext<S3BinaryProcessor> context) {
		S3BinaryProcessor deployable = context.getDeployable();

		S3BinaryProcessorImpl bean = new S3BinaryProcessorImpl();
		String bucketName = deployable.getBucketName();
		if (StringTools.isEmpty(bucketName)) {
			logger.warn(() -> "Bucket name is not set");
			throw new IllegalStateException("The bucket name of the S3 connection must not be empty.");
		}
		bean.setBucketName(bucketName);

		CacheOptions cacheOptions = deployable.getCacheOptions();
		if (cacheOptions != null) {
			bean.setCacheType(cacheOptions.getType());
			bean.setCacheMaxAge(cacheOptions.getMaxAge());
			bean.setCacheMustRevalidate(cacheOptions.getMustRevalidate());
		}

		com.braintribe.model.aws.deployment.S3Connector connection = deployable.getConnection();
		String awsAccessKey = connection.getAwsAccessKey();
		String awsSecretAccessKey = connection.getAwsSecretAccessKey();
		if (StringTools.isAnyBlank(awsSecretAccessKey, awsSecretAccessKey)) {
			logger.warn(() -> "AWS key and secret must be present: " + StringTools.simpleObfuscatePassword(awsAccessKey) + ", "
					+ StringTools.simpleObfuscatePassword(awsSecretAccessKey));
			throw new IllegalStateException("Both the AWS Key and the AWS Secret Key must be set.");
		}

		S3Connector connector = context.resolve(connection, com.braintribe.model.aws.deployment.S3Connector.T);
		bean.setS3Connection(connector);
		bean.setPathPrefix(deployable.getPathPrefix());
		bean.setConnectionName(connection.getName());

		bean.setDownloadInputStreamTracker(downloadInputStreamTracker());
		bean.setDownloadOutputStreamTracker(downloadOutputStreamTracker());

		return bean;
	}

	@Managed
	private InputStreamTracker downloadInputStreamTracker() {
		InputStreamTracker bean = new InputStreamTracker();
		return bean;
	}
	@Managed
	private OutputStreamTracker downloadOutputStreamTracker() {
		OutputStreamTracker bean = new OutputStreamTracker();
		return bean;
	}

	@Managed
	public S3ConnectionImpl connector(ExpertContext<com.braintribe.model.aws.deployment.S3Connector> context) {
		com.braintribe.model.aws.deployment.S3Connector deployable = context.getDeployable();

		String awsAccessKey = deployable.getAwsAccessKey();
		String awsSecretAccessKey = deployable.getAwsSecretAccessKey();
		if (StringTools.isAnyBlank(awsSecretAccessKey, awsSecretAccessKey)) {
			logger.warn(() -> "AWS key and secret must be present: " + StringTools.simpleObfuscatePassword(awsAccessKey) + ", "
					+ StringTools.simpleObfuscatePassword(awsSecretAccessKey));
			throw new IllegalStateException("Both the AWS Key and the AWS Secret Key must be set.");
		}

		S3ConnectionImpl bean = new S3ConnectionImpl();

		bean.setS3ConnectorDeployable(deployable);
		bean.setHttpConnectionPoolSize(deployable.getHttpConnectionPoolSize());
		bean.setConnectionAcquisitionTimeout(deployable.getConnectionAcquisitionTimeout());
		bean.setConnectionTimeout(deployable.getConnectionTimeout());
		bean.setSocketTimeout(deployable.getSocketTimeout());

		return bean;
	}

	@Managed
	public HealthCheckProcessor healthCheckProcessor() {
		HealthCheckProcessor bean = new HealthCheckProcessor();
		bean.setSessionFactory(tfPlatform.systemUserRelated().sessionFactory());
		bean.setDownloadInputStreamTracker(downloadInputStreamTracker());
		bean.setDownloadOutputStreamTracker(downloadOutputStreamTracker());
		return bean;
	}
}
