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
package com.braintribe.model.processing.platformreflection.db;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static java.util.Collections.emptyList;

import java.net.URI;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.deployment.database.connector.DatabaseConnectionDescriptor;
import com.braintribe.model.deployment.database.pool.ConfiguredDatabaseConnectionPool;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.platformreflection.db.DatabaseConnectionInfo;
import com.braintribe.model.platformreflection.db.DatabaseConnectionPoolMetrics;
import com.braintribe.model.platformreflection.db.DatabaseInformation;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.utils.StringTools;

public class StandardDatabaseInformationProvider implements DatabaseInformationProvider {

	private static Logger logger = Logger.getLogger(StandardDatabaseInformationProvider.class);

	private Supplier<PersistenceGmSession> cortexSessionSupplier;

	@Required
	public void setCortexSessionSupplier(Supplier<PersistenceGmSession> cortexSessionSupplier) {
		this.cortexSessionSupplier = cortexSessionSupplier;
	}

	@Override
	public DatabaseInformation get() {
		logger.debug(() -> "Compiling database information.");

		try {
			DatabaseInformation result = DatabaseInformation.T.create();
			List<DatabaseConnectionInfo> cps = collectHikariCpConnectionPools();
			result.getConnectionPools().addAll(cps);

			List<DatabaseConnectionInfo> combined = combineConnectionPoolInfos(cps);
			result.getCombinedConnectionInfo().addAll(combined);

			return result;

		} finally {
			logger.debug(() -> "Done with database information.");
		}
	}

	private List<DatabaseConnectionInfo> collectHikariCpConnectionPools() {
		try {
			List<DatabaseConnectionPool> list = queryConnectionPools();

			return list.stream() //
					.map(this::toDbConnectionInfo) //
					.collect(Collectors.toList());

		} catch (Exception e) {
			logger.debug(() -> "Error while trying to retrieve DB connectors from Cortex.", e);
			return emptyList();
		}
	}

	private List<DatabaseConnectionPool> queryConnectionPools() {
		PersistenceGmSession session = cortexSessionSupplier.get();
		EntityQuery query = EntityQueryBuilder.from(DatabaseConnectionPool.T).where().property(Deployable.deploymentStatus)
				.eq(DeploymentStatus.deployed).done();

		return session.query().entities(query).list();
	}

	private DatabaseConnectionInfo toDbConnectionInfo(DatabaseConnectionPool cp) {
		DatabaseConnectionInfo result = DatabaseConnectionInfo.T.create();
		result.setName(cp.getExternalId());
		result.setConnectionDescription(descriptionFor(cp));

		return result;
	}

	private String descriptionFor(DatabaseConnectionPool cp) {
		if (!(cp instanceof ConfiguredDatabaseConnectionPool))
			return null;

		DatabaseConnectionDescriptor desc = ((ConfiguredDatabaseConnectionPool) cp).getConnectionDescriptor();
		if (desc == null)
			return null;

		return desc.describeConnection();
	}

	private List<DatabaseConnectionInfo> combineConnectionPoolInfos(List<DatabaseConnectionInfo> cps) {
		TreeMap<String, DatabaseConnectionInfo> map = new TreeMap<>();

		for (DatabaseConnectionInfo dcp : cps) {
			String key = createMapKeyFromConnectionPool(dcp);
			DatabaseConnectionInfo entry = map.get(key);
			if (entry == null) {
				entry = DatabaseConnectionInfo.T.create();
				entry.setName(key);
				entry.setConnectionDescription(dcp.getName());
				entry.setMaximumPoolSize(dcp.getMaximumPoolSize());
				entry.setMinimumPoolSize(dcp.getMinimumPoolSize());
				DatabaseConnectionPoolMetrics entryMetrics = DatabaseConnectionPoolMetrics.T.create();
				entry.setMetrics(entryMetrics);

				DatabaseConnectionPoolMetrics dcpMetrics = dcp.getMetrics();
				if (dcpMetrics != null) {
					entryMetrics.setActiveConnections(dcpMetrics.getActiveConnections());
					entryMetrics.setIdleConnections(dcpMetrics.getIdleConnections());
					entryMetrics.setLeaseCount(dcpMetrics.getLeaseCount());
					entryMetrics.setThreadsAwaitingConnections(dcpMetrics.getThreadsAwaitingConnections());
					entryMetrics.setTotalConnections(dcpMetrics.getTotalConnections());
				}
				map.put(key, entry);
			} else {
				String oldDesc = entry.getConnectionDescription();
				if (StringTools.isBlank(oldDesc)) {
					entry.setConnectionDescription(dcp.getName());
				} else {
					entry.setConnectionDescription(oldDesc + ", " + dcp.getName());
				}

				entry.setMaximumPoolSize(dcp.getMaximumPoolSize() + entry.getMaximumPoolSize());
				entry.setMinimumPoolSize(dcp.getMinimumPoolSize() + entry.getMinimumPoolSize());
				DatabaseConnectionPoolMetrics dcpMetrics = dcp.getMetrics();
				if (dcpMetrics != null) {
					DatabaseConnectionPoolMetrics entryMetrics = entry.getMetrics();

					entryMetrics.setActiveConnections(dcpMetrics.getActiveConnections() + entryMetrics.getActiveConnections());
					entryMetrics.setIdleConnections(dcpMetrics.getIdleConnections() + entryMetrics.getIdleConnections());
					entryMetrics.setLeaseCount(dcpMetrics.getLeaseCount() + entryMetrics.getLeaseCount());
					entryMetrics
							.setThreadsAwaitingConnections(dcpMetrics.getThreadsAwaitingConnections() + entryMetrics.getThreadsAwaitingConnections());
					entryMetrics.setTotalConnections(dcpMetrics.getTotalConnections() + entryMetrics.getTotalConnections());
				}
			}
		}

		return newList(map.values());
	}

	private static String createMapKeyFromConnectionPool(DatabaseConnectionInfo dcp) {
		String key = dcp.getName();
		String desc = dcp.getConnectionDescription();
		if (!StringTools.isBlank(desc) && desc.startsWith("jdbc:")) {
			try {
				String cleanURI = desc.substring(5);

				URI uri = URI.create(cleanURI);

				StringBuilder sb = new StringBuilder();
				sb.append(uri.getHost());
				int port = uri.getPort();
				if (port != -1) {
					sb.append(":");
					sb.append(port);
				}
				key = sb.toString();
			} catch (Exception e) {
				logger.debug(() -> "Could not parse JDBC URL " + desc, e);
			}
		}
		return key;
	}
}
