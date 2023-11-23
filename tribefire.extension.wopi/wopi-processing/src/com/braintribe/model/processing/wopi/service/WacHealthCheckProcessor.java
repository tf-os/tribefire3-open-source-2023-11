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
package com.braintribe.model.processing.wopi.service;

import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.GenericRuntimeException;
import com.braintribe.common.lcd.Pair;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckRequest;
import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;
import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.processing.check.api.CheckProcessor;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.wopi.connector.WopiWacConnector;
import com.braintribe.utils.StringTools;

/**
 * Health check to ping WOPI client endpoint (e.g. http://[HOST]:[PORT]/hosting/discovery) - this ensures that the WOPI
 * app is reachable
 * 
 *
 */
public class WacHealthCheckProcessor implements CheckProcessor {

	private static final Logger logger = Logger.getLogger(WacHealthCheckProcessor.class);

	private int connectionRequestTimeout = 2000;
	private int connectTimeout = 2000;
	private int socketTimeout = 2000;

	private Supplier<PersistenceGmSession> cortexSessionSupplier;

	@Override
	public CheckResult check(ServiceRequestContext requestContext, CheckRequest request) {
		PersistenceGmSession cortexSession = cortexSessionSupplier.get();

		CheckResult response = CheckResult.T.create();

		//@formatter:off
		EntityQuery query = EntityQueryBuilder.from(WopiWacConnector.T)
				.where()
					.property(WopiWacConnector.deploymentStatus).eq(DeploymentStatus.deployed)
				.done();
		//@formatter:on
		List<WopiWacConnector> wopiConnectors = cortexSession.query().entities(query).list();

		Pair<CheckResultEntry, List<CheckResultEntry>> pair = healthCheck(wopiConnectors);

		response.getEntries().add(pair.first());
		response.getEntries().addAll(pair.getSecond());

		return response;
	}

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

	private Pair<CheckResultEntry, List<CheckResultEntry>> healthCheck(List<WopiWacConnector> wopiConnectors) {
		CheckResultEntry overviewEntry = CheckResultEntry.T.create();
		overviewEntry.setName("WOPI connectors - OVERVIEW");
		overviewEntry.setDetailsAsMarkdown(true);
		overviewEntry.setCheckStatus(CheckStatus.ok);

		StringBuffer overviewDetails = new StringBuffer();
		overviewDetails.append("Name | Endpoint | Status\n");
		overviewDetails.append("--- | --- | ---\n");

		String header = overviewDetails.toString();

		Queue<CheckResultEntry> detailEntries = new ConcurrentLinkedQueue<>();

		wopiConnectors.stream().forEach(connector -> {
			Pair<String, CheckResultEntry> pair = singleHealthCheck(connector);
			String details = pair.getFirst();
			CheckResultEntry entry = pair.getSecond();

			overviewDetails.append(details);
			if (entry.getCheckStatus() == CheckStatus.fail) {
				overviewEntry.setCheckStatus(CheckStatus.fail);
			}

			entry.setDetails(header + details);
			detailEntries.add(entry);
		});

		overviewEntry.setDetails(overviewDetails.toString());

		return Pair.of(overviewEntry, detailEntries.stream().sorted(Comparator.comparing(CheckResultEntry::getName)).collect(Collectors.toList()));
	}

	private Pair<String, CheckResultEntry> singleHealthCheck(WopiWacConnector connector) {
		CheckResultEntry entry = CheckResultEntry.T.create();
		entry.setName("WOPI connectors - DETAILS");
		entry.setDetailsAsMarkdown(true);
		entry.setCheckStatus(CheckStatus.ok);

		StringBuilder sb = new StringBuilder();

		String wacDiscoveryEndpoint = connector.getWacDiscoveryEndpoint();
		try {
			String body;
			sb.append(connector.getExternalId() + " | ");
			sb.append(wacDiscoveryEndpoint + " | ");

			try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
				//@formatter:off
				RequestConfig requestConfig = RequestConfig.custom()
						.setConnectionRequestTimeout(connectionRequestTimeout)
						.setConnectTimeout(connectTimeout)
						.setSocketTimeout(socketTimeout)
					.build();
				//@formatter:on
				HttpGet httpGet = new HttpGet(wacDiscoveryEndpoint);
				httpGet.setConfig(requestConfig);
				CloseableHttpResponse httpGetResponse = client.execute(httpGet);
				HttpEntity entity = httpGetResponse.getEntity();
				try (InputStream is = entity.getContent()) {
					body = StringTools.fromInputStream(is);
				}
			}

			if (body.contains("doc") && body.contains("docx") && body.contains("xls") && body.contains("xlsx") && body.contains("ppt")
					&& body.contains("pptx")) {
				logger.debug(() -> "Successfully executed '" + WacHealthCheckProcessor.class.getSimpleName() + "' for wacDiscoveryEndpoint: '"
						+ wacDiscoveryEndpoint + "'");
			} else {
				throw new GenericRuntimeException("WacDiscoveryEndpoint: '" + wacDiscoveryEndpoint + "' is not available");
			}
			sb.append(" OK \n");
		} catch (Exception e) {
			entry.setCheckStatus(CheckStatus.fail);
			entry.setMessage("Failed to check WOPI endpoint");
			sb.append(e.getMessage() + " \n");
			logger.warn(() -> "Health check failed for WOPI connector: '" + connector + "' - " + e.getMessage() + " - Details see on debug log");
			logger.debug(e);
		}

		String details = sb.toString();
		Pair<String, CheckResultEntry> pair = Pair.of(details, entry);
		return pair;
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Required
	@Configurable
	public void setCortexSessionSupplier(Supplier<PersistenceGmSession> cortexSessionSupplier) {
		this.cortexSessionSupplier = cortexSessionSupplier;
	}
}
