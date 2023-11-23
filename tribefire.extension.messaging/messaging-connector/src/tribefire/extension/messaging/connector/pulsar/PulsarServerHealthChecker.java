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
package tribefire.extension.messaging.connector.pulsar;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.admin.PulsarAdminException;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.common.naming.TopicName;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;

import tribefire.extension.messaging.model.deployment.connector.properties.PulsarProperties;

public class PulsarServerHealthChecker {
	private static final Logger logger = Logger.getLogger(PulsarServerHealthChecker.class);
	private static final String NAMESPACE = TopicName.PUBLIC_TENANT + "/" + TopicName.DEFAULT_NAMESPACE;

	private ClassLoader moduleClassLoader;

	public CheckResultEntry checkServer(PulsarProperties properties) {
		CheckResultEntry result = CheckResultEntry.T.create();
		try (PulsarAdmin client = buildAdmin(properties)) {
			List<String> topics = client.topics().getList(NAMESPACE);
			List<String> clusters = client.clusters().getClusters();
			result.setCheckStatus(CheckStatus.ok);
			result.setDetails("Server access is ok. Clusters size: " + clusters.size() + "\n" + "Accessible topics: " + String.join(", ", topics));
			return result;
		} catch (PulsarClientException | PulsarAdminException | IllegalArgumentException e) {
			logger.error("Pulsar is not available.");
			result.setCheckStatus(CheckStatus.fail);
			result.setMessage("Pulsar cluster unreachable!");
			result.setDetails(e.getMessage());
			return result;
		}
	}

	private PulsarAdmin buildAdmin(PulsarProperties properties) throws PulsarClientException {
		ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			if (moduleClassLoader != null) {
				Thread.currentThread().setContextClassLoader(moduleClassLoader);
			}
		//@formatter:off
		return PulsarAdmin.builder()
				.serviceHttpUrl(properties.getWebServiceUrl())
				.connectionTimeout(properties.getConnectionTimeout(), TimeUnit.SECONDS)
				.requestTimeout(properties.getOperationTimeout(), TimeUnit.SECONDS)
				.build();
        //@formatter:on
		} finally {
			Thread.currentThread().setContextClassLoader(origClassLoader);
		}
	}

	@Configurable
	@Required
	public PulsarServerHealthChecker setModuleClassLoader(ClassLoader moduleClassLoader) {
		this.moduleClassLoader = moduleClassLoader;
		return this;
	}
}
