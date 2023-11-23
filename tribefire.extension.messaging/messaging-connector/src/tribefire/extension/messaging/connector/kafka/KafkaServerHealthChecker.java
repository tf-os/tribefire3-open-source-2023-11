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
package tribefire.extension.messaging.connector.kafka;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;

import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;
import com.braintribe.utils.RandomTools;

public class KafkaServerHealthChecker {
	private static final Logger logger = Logger.getLogger(KafkaServerHealthChecker.class);
	private static final int TIMEOUT = 5000;
	private final String id = "PROPERTIES_ID" + "_" + RandomTools.newStandardUuid();
	private final Set<String> metricsNames;
	private final Properties properties;
	private final Supplier<Map<MetricName, ? extends Metric>> metricsSupplier;

	public KafkaServerHealthChecker(Properties properties, Set<String> metricsNames, Supplier<Map<MetricName, ? extends Metric>> metricsSupplier) {
		this.properties = properties;
		this.metricsNames = metricsNames;
		this.metricsSupplier = metricsSupplier;
	}

	public CheckResultEntry checkServer(String objectName) {
		properties.put("client.id", id);
		CheckResultEntry result = CheckResultEntry.T.create();
		try (AdminClient client = AdminClient.create(properties)) {
			Collection<TopicListing> listings = client.listTopics(new ListTopicsOptions().timeoutMs(TIMEOUT)).listings().get();
			DescribeClusterResult cluster = client.describeCluster();

			result.setCheckStatus(CheckStatus.ok);
			result.setDetails("Server access is ok. Clusters size: " + cluster.nodes().get().size() + "\n" + "Accessible topics: "
					+ listings.stream().map(TopicListing::name).collect(Collectors.joining(", ")));

			if (result.getCheckStatus() == CheckStatus.ok) {
				Map<MetricName, ? extends Metric> metrics = metricsSupplier.get();
				Set<MetricName> names = metrics.keySet().stream().filter(k -> metricsNames.contains(k.name())).collect(Collectors.toSet());
				String collect = names.stream().map(metrics::get).map(m -> m.metricName().name() + ": " + m.metricValue()).distinct()
						.collect(Collectors.joining("; "));

				result.setDetails(result.getDetails() + "\n" + objectName + " metrics: " + collect);
			}
			return result;
		} catch (ExecutionException | InterruptedException e) {
			logger.error("Kafka is not available, timed out after {} ms " + TIMEOUT);
			result.setCheckStatus(CheckStatus.fail);
			result.setMessage("Kafka cluster unreachable!");
			result.setDetails(e.getClass() + ": " + e.getMessage());
			return result;
		}
	}
}
