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
package tribefire.extension.vitals.jdbc.jdbc_dcsa_storage.processor;

import static com.braintribe.util.jdbc.JdbcTools.tableExists;
import static tribefire.extension.jdbc.gmdb.dcsa.GmDbDcsaSharedStorage.DEFAULT_OPS_TABLE_NAME;
import static tribefire.extension.jdbc.gmdb.dcsa.GmDbDcsaSharedStorage.DEFAULT_RES_TABLE_NAME;
import static tribefire.extension.jdbc.gmdb.dcsa.TemporaryJdbc2GmDbSharedStorage.OLD_TABLE_NAME;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;

import javax.sql.DataSource;

import com.braintribe.logging.Logger;
import com.braintribe.model.access.collaboration.distributed.api.JdbcDcsaStorage;
import com.braintribe.util.jdbc.JdbcTools;

import tribefire.extension.jdbc.gmdb.dcsa.TemporaryJdbc2GmDbSharedStorage;
import tribefire.extension.vitals.jdbc.model.migration.SharedStorageStatus;

/**
 * @author peter.gazdik
 */
/* package */ class Jdbc2GmDbDowngrader {

	private final DataSource dataSource;
	private final JdbcDcsaStorage jdbcStorage;

	private static final Logger log = Logger.getLogger(Jdbc2GmDbDowngrader.class);
	private final SharedStorageStatusReporter statusReporter;

	public Jdbc2GmDbDowngrader(TemporaryJdbc2GmDbSharedStorage sharedStorage, SharedStorageStatusReporter statusReporter) {
		this.statusReporter = statusReporter;
		this.dataSource = sharedStorage.dataSource;
		this.jdbcStorage = sharedStorage.jdbcStorage;
	}

	public void doDowngrade() {
		Lock lock = jdbcStorage.getLock("dcsa-migration-lock");
		lock.lock();
		try {

			try {
				_doDowngrade();
				statusReporter.onActionFinished(SharedStorageStatus.DOWNGRADED, null);

			} catch (Exception e) {
				statusReporter.onActionFinished(SharedStorageStatus.DOWNGRADING_ERROR, e);
				throw e;
			}

		} finally {
			lock.unlock();
		}
	}

	private void _doDowngrade() {
		statusReporter.onStartDowngrade();

		try (Connection c = dataSource.getConnection()) {
			boolean opsExists = tableExists(c, DEFAULT_OPS_TABLE_NAME) != null;
			boolean resExists = tableExists(c, DEFAULT_RES_TABLE_NAME) != null;

			if (tableExists(c, OLD_TABLE_NAME) == null)
				throw new IllegalStateException("Cannot downgrade DCSA storage as old table '" + OLD_TABLE_NAME + "' does not exist!");

			if (opsExists)
				deleteTable(c, DEFAULT_OPS_TABLE_NAME);
			if (resExists)
				deleteTable(c, DEFAULT_RES_TABLE_NAME);

		} catch (SQLException e) {
			throw new RuntimeException("Error while checking which tables exist for migration of JDBC DCSA storage.", e);
		}
	}

	private void deleteTable(Connection c, String tableName) {
		JdbcTools.withStatement(c, () -> "Deleting " + tableName, st -> {
			String sql = "drop table " + tableName;
			int i = st.executeUpdate(sql);
			log("'" + sql + "' returned: " + i);
		});
	}

	private void log(String msg) {
		log.info("[DCSA Upgrade] " + msg);
	}

}
