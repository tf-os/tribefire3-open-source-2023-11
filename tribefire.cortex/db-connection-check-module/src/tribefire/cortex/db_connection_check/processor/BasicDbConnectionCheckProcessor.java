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
package tribefire.cortex.db_connection_check.processor;

import java.sql.Connection;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Supplier;

import javax.sql.DataSource;

import com.braintribe.cfg.Required;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckRequest;
import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;
import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.processing.check.api.CheckProcessor;
import com.braintribe.model.processing.deployment.api.DeployedComponentResolver;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.JunctionBuilder;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;

/**
 * 
 * 
 * @author peter.gazdik
 */
public class BasicDbConnectionCheckProcessor implements CheckProcessor {

	private static final Logger log = Logger.getLogger(BasicDbConnectionCheckProcessor.class);

	private String dbConnectionExternalidPattern;
	private Supplier<PersistenceGmSession> cortexSessionSupplier;
	private DeployedComponentResolver deployedComponentResolver;

	@Required
	public void setCortexSessionSupplier(Supplier<PersistenceGmSession> cortexSessionSupplier) {
		this.cortexSessionSupplier = cortexSessionSupplier;
	}

	@Required
	public void setDbConnectionExternalidPattern(String dbConnectionExternalidPattern) {
		this.dbConnectionExternalidPattern = dbConnectionExternalidPattern;
	}

	@Required
	public void setDeployedComponentResolver(DeployedComponentResolver deployedComponentResolver) {
		this.deployedComponentResolver = deployedComponentResolver;
	}

	@Override
	public CheckResult check(ServiceRequestContext requestContext, CheckRequest request) {
		return new DbConnectionCheckTask().run();
	}

	private class DbConnectionCheckTask {

		public CheckResult run() {
			EntityQuery query = connectionPoolsQuery();

			List<DatabaseConnectionPool> connectionPools = queryConnectionPools(query);

			return checkCps(connectionPools);
		}

		private List<DatabaseConnectionPool> queryConnectionPools(EntityQuery query) {
			return cortexSessionSupplier.get().query().entities(query).list();
		}

		private EntityQuery connectionPoolsQuery() {
			JunctionBuilder<EntityQueryBuilder> cb = EntityQueryBuilder.from(DatabaseConnectionPool.T) //
					.where().conjunction() //
					.property(DatabaseConnectionPool.deploymentStatus).eq(DeploymentStatus.deployed);

			if (dbConnectionExternalidPattern != null)
				cb = cb.property(DatabaseConnectionPool.externalId).like(dbConnectionExternalidPattern);

			return cb.close().done();
		}

		private DatabaseConnectionPool currentCp;
		private CheckResultEntry currentEntry;
		private DataSource currentDataSource;

		private CheckResult checkCps(List<DatabaseConnectionPool> connectionPools) {
			CheckResult result = CheckResult.T.create();

			for (DatabaseConnectionPool cp : connectionPools) {
				currentCp = cp;
				result.getEntries().add(checkCurrentCp());
			}

			return result;
		}

		private CheckResultEntry checkCurrentCp() {
			currentEntry = CheckResultEntry.T.create();
			currentEntry.setName(currentCp.getExternalId());

			resolveDataSource();

			if (currentDataSource != null)
				checkDataSource();

			return currentEntry;
		}

		private void resolveDataSource() {
			try {
				currentDataSource = deployedComponentResolver.resolve(currentCp, DatabaseConnectionPool.T);
			} catch (Exception e) {
				markFailure(e);
				currentDataSource = null;
			}
		}

		private void checkDataSource() {
			currentEntry.setCheckStatus(CheckStatus.ok);
			currentEntry.setDetailsAsMarkdown(true);

			StringBuilder sb = new StringBuilder();
			sb.append("Valid | Check duration\n");
			sb.append("--- | ---\n");

			Connection c = null;
			String duration = "";
			boolean valid = false;
			try {
				c = currentDataSource.getConnection();
				Instant start = NanoClock.INSTANCE.instant();
				valid = c.isValid(10);
				duration = StringTools.prettyPrintDuration(start, true, ChronoUnit.MILLIS);

				if (!valid)
					currentEntry.setCheckStatus(CheckStatus.fail);

			} catch (Exception e) {
				markFailure(e);

			} finally {
				IOTools.closeCloseable(c, log);
			}
			sb.append(valid + " | " + duration + "\n");

			currentEntry.setDetails(sb.toString());
		}

		private void markFailure(Exception e) {
			log.error("Error while trying to check DatabaseConnectionPool " + currentCp, e);
			currentEntry.setCheckStatus(CheckStatus.fail);
			currentEntry.setMessage(Exceptions.getRootCause(e).getMessage());
		}

	}

}
