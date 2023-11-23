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
package tribefire.extension.azure.processing;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Numbers;
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
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.stream.tracking.InputStreamTracker;
import com.braintribe.utils.stream.tracking.OutputStreamTracker;
import com.braintribe.utils.stream.tracking.data.StreamStatistics;
import com.braintribe.utils.stream.tracking.data.StreamTrackingCollection;

import tribefire.extension.azure.model.api.CheckConnection;
import tribefire.extension.azure.model.api.ConnectionStatus;
import tribefire.extension.azure.model.deployment.AzureBlobBinaryProcessor;

public class HealthCheckProcessor implements CheckProcessor {

	private static final Logger logger = Logger.getLogger(HealthCheckProcessor.class);

	private PersistenceGmSessionFactory sessionFactory;
	private InputStreamTracker downloadInputStreamTracker;
	private OutputStreamTracker downloadOutputStreamTracker;

	@Override
	public CheckResult check(ServiceRequestContext requestContext, CheckRequest request) {

		CheckResult response = CheckResult.T.create();

		PersistenceGmSession cortexSession = getCortexSession();

		List<CheckResultEntry> connectionChecks = checkConnections(cortexSession);

		List<CheckResultEntry> entries = response.getEntries();
		entries.addAll(connectionChecks);

		addStatistics(entries);

		return response;
	}

	private List<CheckResultEntry> checkConnections(PersistenceGmSession cortexSession) {

		List<CheckResultEntry> result = new ArrayList<>();

		List<AzureBlobBinaryProcessor> list = null;
		try {
			EntityQuery query = EntityQueryBuilder.from(AzureBlobBinaryProcessor.T).where().property(AzureBlobBinaryProcessor.deploymentStatus)
					.eq(DeploymentStatus.deployed).done();
			list = cortexSession.query().entities(query).list();
		} catch (Exception e) {
			logger.error("Could not get a list of all AzureBlobBinaryProcessor's.", e);
			CheckResultEntry current = CheckResultEntry.T.create();
			current.setName("Azure Blob Storage");
			current.setMessage("Could not query for Azure Blob Storage processors.");
			current.setCheckStatus(CheckStatus.fail);
			result.add(current);
			return result;
		}

		if (list.isEmpty()) {

			CheckResultEntry current = CheckResultEntry.T.create();
			current.setName("Azure Blob Storage");
			current.setDetails("No deployed processors found.");
			current.setCheckStatus(CheckStatus.ok);
			result.add(current);

		} else {

			CheckResultEntry entry = CheckResultEntry.T.create();
			entry.setName("Azure Blob Storage Connections");
			entry.setDetailsAsMarkdown(true);
			entry.setCheckStatus(CheckStatus.ok);
			result.add(entry);

			StringBuilder sb = new StringBuilder();
			sb.append("Account | Container | Check Duration | Access\n");
			sb.append("--- | --- | --- | ---\n");

			for (AzureBlobBinaryProcessor proc : list) {

				String containerName = proc.getContainerName();
				logger.debug(() -> "Checking Blob Storage container: " + containerName);

				try {
					CheckConnection cc = CheckConnection.T.create();
					cc.setAzureBlobBinaryProcessorExternalId(proc.getExternalId());
					ConnectionStatus connectionStatus = cc.eval(cortexSession).get();

					String accountName = connectionStatus.getAccountName();
					if (!StringTools.isBlank(accountName)) {
						sb.append(accountName + " | ");
					} else {
						sb.append("(not available) | ");
					}
					boolean ok = connectionStatus.getListBlobsSuccess();
					sb.append(containerName + " | ");
					sb.append(StringTools.prettyPrintDuration(connectionStatus.getDurationInMs(), true, ChronoUnit.MILLIS) + " | ");
					sb.append(ok + "\n");
					if (!ok) {
						entry.setCheckStatus(CheckStatus.fail);
					}

				} catch (Exception e) {
					sb.append("Account name not available | ");
					sb.append(containerName + " | ");
					sb.append("(not available) | (not available)\n");

					entry.setCheckStatus(CheckStatus.fail);
					entry.setMessage(e.getMessage());
					logger.error("Error while trying to check connection " + proc.getExternalId(), e);
				}
			}

			entry.setDetails(sb.toString());
		}

		return result;
	}

	private void addStatistics(List<CheckResultEntry> result) {

		TreeMap<String, StreamStatistics> statistics = downloadInputStreamTracker.getStatistics();
		if (!statistics.isEmpty()) {
			for (Map.Entry<String, StreamStatistics> stats : statistics.entrySet()) {

				CheckResultEntry entry = CheckResultEntry.T.create();
				entry.setName("Download Statistics (Get): " + stats.getKey().trim());
				entry.setDetailsAsMarkdown(true);
				entry.setCheckStatus(CheckStatus.ok);
				result.add(entry);

				StringBuilder sb = new StringBuilder();
				appendStreamStatistics(stats.getValue(), sb);
				entry.setDetails(sb.toString());

			}
		}
		TreeMap<String, StreamStatistics> outStatistics = downloadOutputStreamTracker.getStatistics();
		if (!outStatistics.isEmpty()) {
			for (Map.Entry<String, StreamStatistics> stats : outStatistics.entrySet()) {

				CheckResultEntry entry = CheckResultEntry.T.create();
				entry.setName("Download Statistics (Stream): " + stats.getKey().trim());
				entry.setDetailsAsMarkdown(true);
				entry.setCheckStatus(CheckStatus.ok);
				result.add(entry);

				StringBuilder sb = new StringBuilder();
				appendStreamStatistics(stats.getValue(), sb);
				entry.setDetails(sb.toString());

			}
		}

		List<StreamTrackingCollection> all = downloadInputStreamTracker.getAllStreamTrackingCollections();
		all.addAll(downloadOutputStreamTracker.getAllStreamTrackingCollections());
		if (!all.isEmpty()) {
			StreamStatistics combinedStatistics = StreamTrackingCollection.getCombinedStatistics(all);

			CheckResultEntry entry = CheckResultEntry.T.create();
			entry.setName("Combined Download Statistics");
			entry.setDetailsAsMarkdown(true);
			entry.setCheckStatus(CheckStatus.ok);
			result.add(entry);

			StringBuilder sb = new StringBuilder();
			appendStreamStatistics(combinedStatistics, sb);
			entry.setDetails(sb.toString());
		}
	}

	private void appendStreamStatistics(StreamStatistics ss, StringBuilder sb) {
		sb.append("Category | Value\n");
		sb.append("--- | ---\n");

		sb.append("Open streams | " + ss.getOpenConnections() + "\n");
		sb.append("Oldest open stream | "
				+ StringTools.prettyPrintDuration(ss.getOldestConnectionDuration() / Numbers.NANOSECONDS_PER_MILLISECOND, true, null) + "\n");
		sb.append("Average age of open streams | "
				+ StringTools.prettyPrintMilliseconds(ss.getAverageOpenConnectionsAge() / Numbers.NANOSECONDS_PER_MILLISECOND, true, null) + "\n");
		sb.append("Total streams opened | " + ss.getTotalStreamsOpened() + "\n");
		sb.append("Total streams closed | " + ss.getStreamsClosed() + "\n");
		sb.append("Total streams fully transferred | " + ss.getStreamsFullyTransferred() + "\n");
		sb.append("Total streams partially transferred | " + ss.getStreamsPartiallyTransferred() + "\n");

		sb.append("Total bytes read | " + StringTools.prettyPrintBytesDecimal(ss.getTotalBytesTransferred()) + "\n");
		sb.append("Average file size | " + StringTools.prettyPrintBytesDecimal((long) ss.getAverageFileSize()) + "\n");
		sb.append("Maximum duration | " + StringTools.prettyPrintMilliseconds(ss.getMaxDuration() / Numbers.NANOSECONDS_PER_MILLISECOND, true, null)
				+ "\n");
		sb.append("Maximum throughput | " + StringTools.prettyPrintBytesDecimal((long) ss.getMaxThroughput()) + " / s\n");
		sb.append("Minimum throughput | " + StringTools.prettyPrintBytesDecimal((long) ss.getMinThroughput()) + " / s\n");
		sb.append("Average throughput | " + StringTools.prettyPrintBytesDecimal((long) ss.getAverageThroughput()) + " / s\n");
		SortedMap<Integer, List<String>> topStreamers = ss.getTopStreamers();
		if (!topStreamers.isEmpty()) {
			sb.append("Current top streamers | ");
			for (Map.Entry<Integer, List<String>> e : topStreamers.entrySet()) {
				sb.append(e.getKey() + ": " + e.getValue() + "\n");
			}
		}
		ZonedDateTime lastActivity = ss.getLastActivity();
		if (lastActivity != null) {
			sb.append("Last activity | " + DateTools.ISO8601_DATE_WITH_MS_FORMAT_AND_Z.format(lastActivity) + "\n");
		} else {
			sb.append("Last activity | No activity yet recorded.\n");
		}
	}

	private PersistenceGmSession getCortexSession() {
		return sessionFactory.newSession("cortex");
	}

	@Required
	@Configurable
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	@Required
	@Configurable
	public void setDownloadInputStreamTracker(InputStreamTracker downloadInputStreamTracker) {
		this.downloadInputStreamTracker = downloadInputStreamTracker;
	}
	@Required
	@Configurable
	public void setDownloadOutputStreamTracker(OutputStreamTracker downloadOutputStreamTracker) {
		this.downloadOutputStreamTracker = downloadOutputStreamTracker;
	}
}
