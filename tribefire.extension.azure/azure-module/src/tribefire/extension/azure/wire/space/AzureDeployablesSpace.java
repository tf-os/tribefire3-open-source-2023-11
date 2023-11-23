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
package tribefire.extension.azure.wire.space;

import com.braintribe.logging.Logger;
import com.braintribe.model.cache.CacheOptions;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.utils.stream.tracking.InputStreamTracker;
import com.braintribe.utils.stream.tracking.OutputStreamTracker;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.azure.model.deployment.AzureBlobBinaryProcessor;
import tribefire.extension.azure.processing.AzureBlobBinaryProcessorImpl;
import tribefire.extension.azure.processing.AzureServiceProcessor;
import tribefire.extension.azure.processing.HealthCheckProcessor;
import tribefire.module.wire.contract.ResourceProcessingContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class AzureDeployablesSpace implements WireSpace {

	private static Logger logger = Logger.getLogger(AzureDeployablesSpace.class);

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private ResourceProcessingContract resourceProcessing;

	@Managed
	public AzureBlobBinaryProcessorImpl binaryProcessor(ExpertContext<AzureBlobBinaryProcessor> context) {
		AzureBlobBinaryProcessor deployable = context.getDeployable();

		AzureBlobBinaryProcessorImpl bean = new AzureBlobBinaryProcessorImpl();

		CacheOptions cacheOptions = deployable.getCacheOptions();
		if (cacheOptions != null) {
			bean.setCacheType(cacheOptions.getType());
			bean.setCacheMaxAge(cacheOptions.getMaxAge());
			bean.setCacheMustRevalidate(cacheOptions.getMustRevalidate());
		}

		bean.setPathPrefix(deployable.getPathPrefix());
		bean.setStorageConnectionString(deployable.getStorageConnectionString());
		bean.setContainerName(deployable.getContainerName());
		bean.setStreamPipeFactory(tfPlatform.resourceProcessing().streamPipeFactory());

		bean.setDownloadInputStreamTracker(downloadInputStreamTracker());
		bean.setDownloadOutputStreamTracker(downloadOutputStreamTracker());

		logger.debug(() -> "Created AzureBlobBinaryProcessorImpl with Connection String "
				+ StringTools.simpleObfuscatePassword(deployable.getStorageConnectionString()) + " and Container " + deployable.getContainerName());

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
	public HealthCheckProcessor healthCheckProcessor() {
		HealthCheckProcessor bean = new HealthCheckProcessor();
		bean.setSessionFactory(tfPlatform.systemUserRelated().sessionFactory());
		bean.setDownloadInputStreamTracker(downloadInputStreamTracker());
		bean.setDownloadOutputStreamTracker(downloadOutputStreamTracker());
		return bean;
	}

	@Managed
	public AzureServiceProcessor azureServiceProcessor() {
		AzureServiceProcessor bean = new AzureServiceProcessor();
		bean.setCortexSessionSupplier(tfPlatform.systemUserRelated().cortexSessionSupplier());
		return bean;
	}

}
