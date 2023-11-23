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
package tribefire.extension.gcp.wire.space;

import com.braintribe.model.cache.CacheOptions;
import com.braintribe.model.gcp.deployment.GcpConnector;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.gcp.connect.GcpStorageConnector;
import com.braintribe.model.processing.gcp.connect.GcpStorageConnectorImpl;
import com.braintribe.model.processing.gcp.service.GcpServiceProcessor;
import com.braintribe.model.processing.gcp.service.GcpStorageBinaryProcessor;
import com.braintribe.model.processing.gcp.service.HealthCheckProcessor;
import com.braintribe.utils.stream.tracking.InputStreamTracker;
import com.braintribe.utils.stream.tracking.OutputStreamTracker;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.module.wire.contract.ResourceProcessingContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class GcpDeployablesSpace implements WireSpace {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private ResourceProcessingContract resourceProcessing;

	@Managed
	public GcpServiceProcessor gcpServiceProcessor() {
		GcpServiceProcessor bean = new GcpServiceProcessor();

		bean.setDeployRegistry(tfPlatform.deployment().deployRegistry());

		return bean;
	}

	@Managed
	public GcpStorageBinaryProcessor binaryProcessor(ExpertContext<com.braintribe.model.gcp.deployment.GcpStorageBinaryProcessor> context) {
		com.braintribe.model.gcp.deployment.GcpStorageBinaryProcessor deployable = context.getDeployable();

		GcpStorageBinaryProcessor bean = new GcpStorageBinaryProcessor();
		bean.setBucketName(deployable.getBucketName());

		CacheOptions cacheOptions = deployable.getCacheOptions();
		if (cacheOptions != null) {
			bean.setCacheType(cacheOptions.getType());
			bean.setCacheMaxAge(cacheOptions.getMaxAge());
			bean.setCacheMustRevalidate(cacheOptions.getMustRevalidate());
		}

		GcpConnector connectorDeployable = deployable.getConnector();
		GcpStorageConnector connector = context.resolve(connectorDeployable, com.braintribe.model.gcp.deployment.GcpConnector.T);

		bean.setConnector(connector);
		bean.setPathPrefix(deployable.getPathPrefix());

		bean.setConnectionName(connectorDeployable.getName());

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
	public GcpStorageConnectorImpl connector(ExpertContext<GcpConnector> context) {
		GcpConnector deployable = context.getDeployable();

		GcpStorageConnectorImpl bean = new GcpStorageConnectorImpl();
		bean.setConnector(deployable);

		return bean;
	}

	@Managed
	public HealthCheckProcessor healthCheckProcessor() {
		HealthCheckProcessor bean = new HealthCheckProcessor();
		bean.setSessionFactory(tfPlatform.systemUserRelated().sessionFactory());
		bean.setDeployRegistry(tfPlatform.deployment().deployRegistry());
		bean.setDownloadInputStreamTracker(downloadInputStreamTracker());
		bean.setDownloadOutputStreamTracker(downloadOutputStreamTracker());

		return bean;
	}
}
