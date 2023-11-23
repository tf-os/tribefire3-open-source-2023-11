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
package com.braintribe.model.processing.elasticsearch.service;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckRequest;
import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;
import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.elasticsearchdeployment.ElasticsearchConnector;
import com.braintribe.model.elasticsearchdeployment.indexing.ElasticsearchIndexingWorker;
import com.braintribe.model.processing.check.api.CheckProcessor;
import com.braintribe.model.processing.elasticsearch.indexing.ElasticsearchIndexingWorkerImpl;
import com.braintribe.model.processing.elasticsearch.status.WorkerStatus;
import com.braintribe.model.processing.elasticsearch.util.DeployableUtils;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.utils.StringTools;

public class HealthCheckProcessor implements CheckProcessor {

	private static final Logger logger = Logger.getLogger(HealthCheckProcessor.class);

	private Supplier<PersistenceGmSession> cortexSessionSupplier;
	private DeployableUtils deployableUtils;

	@Override
	public CheckResult check(ServiceRequestContext requestContext, CheckRequest request) {

		CheckResult response = CheckResult.T.create();

		List<CheckResultEntry> entries = getConnectionStatus();

		response.getEntries().addAll(entries);

		response.getEntries().addAll(getWorkerStatus());

		return response;
	}

	private List<CheckResultEntry> getWorkerStatus() {
		List<CheckResultEntry> result = new ArrayList<>();

		PersistenceGmSession cortexSession = cortexSessionSupplier.get();

		EntityQuery query = EntityQueryBuilder.from(ElasticsearchIndexingWorker.T).done();

		List<ElasticsearchIndexingWorker> list = cortexSession.query().entities(query).list();

		if (list != null && !list.isEmpty()) {

			CheckResultEntry entry = CheckResultEntry.T.create();
			entry.setName("Elastic Indexing Workers");
			entry.setDetailsAsMarkdown(true);
			entry.setCheckStatus(CheckStatus.ok);
			result.add(entry);

			StringBuilder sb = new StringBuilder();
			sb.append("Name | Index | Active Workers | Enqueued | Max. Active Runtime | Packages Indexed | Entities Indexed\n");
			sb.append("--- | --- | --- | --- | --- | --- | ---\n");

			for (ElasticsearchIndexingWorker workerDeployable : list) {

				if (workerDeployable.getDeploymentStatus() == DeploymentStatus.deployed) {

					try {
						ElasticsearchIndexingWorkerImpl workerImpl = deployableUtils.getElasticsearchWorker(workerDeployable);
						WorkerStatus status = workerImpl.getStatus();

						sb.append(workerDeployable.getExternalId() + " | ");
						sb.append(status.getIndex() + " | ");
						sb.append(status.getActiveWorker() + "/" + status.getWorkerCount() + " | ");
						sb.append(status.getEnqueued() + "/" + status.getQueueSize() + " | ");
						sb.append(StringTools.prettyPrintDuration(status.getMaxActiveRuntime(), true, ChronoUnit.MILLIS) + " | ");
						sb.append(status.getIndexedPackagesCount() + " | ");
						sb.append(status.getIndexedEntitiesCount() + "\n");

					} catch (Exception e) {
						logger.warn(() -> "Error while trying to worker " + workerDeployable.getExternalId(), e);

						sb.append(workerDeployable.getExternalId() + " | n/a | n/a | n/a | n/a | n/a | n/a\n");

						entry.setCheckStatus(CheckStatus.fail);
						entry.setMessage("Could not access status of worker " + workerDeployable.getExternalId());
					}

				}
			}

			entry.setDetails(sb.toString());

		}

		return result;
	}

	private List<CheckResultEntry> getConnectionStatus() {
		List<CheckResultEntry> result = new ArrayList<>();

		PersistenceGmSession cortexSession = cortexSessionSupplier.get();

		EntityQuery query = EntityQueryBuilder.from(ElasticsearchConnector.T).done();

		List<ElasticsearchConnector> list = cortexSession.query().entities(query).list();

		Set<String> alreadyChecked = new HashSet<>();

		if (list != null && !list.isEmpty()) {

			CheckResultEntry entry = CheckResultEntry.T.create();
			entry.setName("Check Elastic Service Availability");
			entry.setCheckStatus(CheckStatus.ok);
			entry.setDetailsAsMarkdown(true);
			result.add(entry);

			StringBuilder sb = new StringBuilder();
			sb.append("Name | Address | Connectivity\n");
			sb.append("--- | --- | ---\n");

			for (ElasticsearchConnector connector : list) {

				if (connector.getDeploymentStatus() == DeploymentStatus.deployed) {
					String host = connector.getHost();
					int port = connector.getPort();
					String key = "" + host + ":" + port;

					if (!alreadyChecked.contains(key)) {
						alreadyChecked.add(key);

						Socket socket = null;
						try {
							socket = new Socket();
							socket.connect(new InetSocketAddress(host, port), Numbers.MILLISECONDS_PER_SECOND * 10);

							sb.append(connector.getName() + " | ");
							sb.append(host + ":" + port + " | ");
							sb.append("true\n");

						} catch (Exception e) {
							logger.info(() -> "Error while trying to connector to " + host + ":" + port, e);

							sb.append(connector.getName() + " | ");
							sb.append(host + ":" + port + " | ");
							sb.append("false\n");

							entry.setCheckStatus(CheckStatus.fail);
							entry.setMessage("Could not connect to socket " + host + ":" + port);
						} finally {
							if (socket != null) {
								try {
									socket.close();
								} catch (Exception e) {
									logger.debug(() -> "Error while closing socket to " + host + ":" + port, e);
								}
							}
						}
					}
				}
			}

			entry.setDetails(sb.toString());

		}

		return result;
	}

	@Required
	@Configurable
	public void setCortexSessionSupplier(Supplier<PersistenceGmSession> cortexSessionSupplier) {
		this.cortexSessionSupplier = cortexSessionSupplier;
	}
	@Required
	@Configurable
	public void setDeployableUtils(DeployableUtils deployableUtils) {
		this.deployableUtils = deployableUtils;
	}

}
