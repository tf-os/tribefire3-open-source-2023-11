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
package tribefire.extension.aws.templates.wire.space;

import com.braintribe.model.aws.deployment.S3Connector;
import com.braintribe.model.aws.deployment.cloudfront.CloudFrontConfiguration;
import com.braintribe.model.aws.deployment.processor.S3BinaryProcessor;
import com.braintribe.model.aws.resource.S3Source;
import com.braintribe.model.extensiondeployment.meta.BinaryProcessWith;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.aws.util.AwsUtil;
import com.braintribe.model.processing.aws.util.Keys;
import com.braintribe.model.processing.resource.configuration.ExternalResourcesContext;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.scope.InstanceConfiguration;

import tribefire.extension.aws.templates.api.AwsConstants;
import tribefire.extension.aws.templates.api.S3BinaryProcessTemplateContext;
import tribefire.extension.aws.templates.wire.contract.AwsTemplatesContract;

@Managed
public class AwsTemplatesSpace implements AwsTemplatesContract {

	@Override
	@Managed
	public S3BinaryProcessor s3StorageBinaryProcessor(S3BinaryProcessTemplateContext context) {
		S3BinaryProcessor bean = context.create(S3BinaryProcessor.T, InstanceConfiguration.currentInstance());
		bean.setCartridge(context.getAwsCartridge());
		bean.setModule(context.getAwsModule());
		bean.setName("S3 Storage Binary Processor " + context.getBucketName());
		bean.setConnection(connector(context));
		bean.setBucketName(context.getBucketName());
		bean.setAutoDeploy(credentialsAvailable(context));
		bean.setPathPrefix(context.getPathPrefix());
		return bean;
	}

	@Override
	@Managed
	public S3Connector connector(S3BinaryProcessTemplateContext context) {
		S3Connector bean = context.create(S3Connector.T, InstanceConfiguration.currentInstance());
		bean.setCartridge(context.getAwsCartridge());
		bean.setModule(context.getAwsModule());
		bean.setName("S3 Connector " + context.getName());
		bean.setAwsAccessKey(context.getAwsAccessKey());
		bean.setAwsSecretAccessKey(context.getAwsSecretAccessKey());
		bean.setRegion(context.getRegion());
		bean.setAutoDeploy(credentialsAvailable(context));
		bean.setHttpConnectionPoolSize(context.getHttpConnectionPoolSize());
		bean.setConnectionAcquisitionTimeout(context.getConnectionAcquisitionTimeout());
		bean.setConnectionTimeout(context.getConnectionTimeout());
		bean.setSocketTimeout(context.getSocketTimeout());
		bean.setUrlOverride(context.getUrlOverride());

		String cloudFrontBaseUrl = context.getCloudFrontBaseUrl();
		if (!StringTools.isBlank(cloudFrontBaseUrl)) {
			CloudFrontConfiguration config = cloudFrontConfiguration(context);
			bean.setCloudFrontConfiguration(config);
		}

		return bean;
	}

	@Managed
	private CloudFrontConfiguration cloudFrontConfiguration(S3BinaryProcessTemplateContext context) {
		String cloudFrontBaseUrl = context.getCloudFrontBaseUrl();
		String privateKey = context.getCloudFrontPrivateKey();
		String publicKey = context.getCloudFrontPublicKey();
		String keyGroupId = context.getCloudFrontKeyGroupId();

		Keys keys;
		if (StringTools.isAnyBlank(privateKey, publicKey)) {
			keys = AwsUtil.generateKeyPair(2048);
		} else {
			keys = new Keys(publicKey, privateKey);
		}

		privateKey = keys.getPrivateKeyBase64();
		publicKey = keys.getPublicKeyBase64();
		String publicKeyPem = keys.getPublicKeyPem();

		CloudFrontConfiguration bean = context.create(CloudFrontConfiguration.T, InstanceConfiguration.currentInstance());
		bean.setBaseUrl(cloudFrontBaseUrl);
		bean.setPrivateKey(privateKey);
		bean.setPublicKey(publicKey);
		bean.setPublicKeyPem(publicKeyPem);
		bean.setKeyGroupId(keyGroupId);

		return bean;
	}

	private boolean credentialsAvailable(S3BinaryProcessTemplateContext context) {
		return !StringTools.isAnyBlank(context.getAwsAccessKey(), context.getAwsSecretAccessKey());
	}

	@Override
	@Managed
	public ExternalResourcesContext externalResourcesContext(S3BinaryProcessTemplateContext context) {
		ExternalResourcesContext bean = ExternalResourcesContext.builder().setBinaryProcessWith(binaryProcessWithAws(context))
				.setPersistenceDataModel((GmMetaModel) context.lookup("model:" + AwsConstants.DATA_MODEL_QUALIFIEDNAME))
				.setResourceSourceType(S3Source.T).build();
		return bean;
	}

	@Managed
	private BinaryProcessWith binaryProcessWithAws(S3BinaryProcessTemplateContext context) {
		BinaryProcessWith bean = context.create(BinaryProcessWith.T, InstanceConfiguration.currentInstance());

		S3BinaryProcessor processor = s3StorageBinaryProcessor(context);

		bean.setRetrieval(processor);
		bean.setPersistence(processor);
		return bean;
	}

}
