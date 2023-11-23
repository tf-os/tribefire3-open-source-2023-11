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
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static java.util.Collections.singleton;
import static tribefire.extension.jdbc.gmdb.dcsa.TemporaryJdbc2GmDbSharedStorage.OLD_TABLE_NAME;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import javax.sql.DataSource;

import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;
import com.braintribe.model.access.collaboration.distributed.api.JdbcDcsaStorage;
import com.braintribe.model.access.collaboration.distributed.api.JdbcDcsaStorage.JdbcDcsaIterable;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaOperation;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaStoreResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.util.jdbc.JdbcTools;
import com.braintribe.utils.lcd.StopWatch;
import com.braintribe.utils.lcd.StringTools;

import tribefire.extension.jdbc.gmdb.dcsa.GmDbDcsaSharedStorage;
import tribefire.extension.jdbc.gmdb.dcsa.TemporaryJdbc2GmDbSharedStorage;
import tribefire.extension.vitals.jdbc.model.migration.AccessMigrationState;
import tribefire.extension.vitals.jdbc.model.migration.SharedStorageStatus;

/**
 * @author peter.gazdik
 */
/* package */ class Jdbc2GmDbUpgrader implements Runnable {

	private final DataSource dataSource;
	private final JdbcDcsaStorage jdbcStorage;
	private final GmDbDcsaSharedStorage gmDbStorage;
	private final SharedStorageStatusReporter statusReporter;

	private static final Logger log = Logger.getLogger(Jdbc2GmDbUpgrader.class);

	private String currentAccessId;
	private final List<String> accessIds = newList();

	private final List<AccessMigrationState> migratedAccesses = newList();
	private AccessMigrationState migratee;

	private final StopWatch sw = new StopWatch();
	private final StopWatch swResDl = new StopWatch().pause();

	public Jdbc2GmDbUpgrader(TemporaryJdbc2GmDbSharedStorage sharedStorage, SharedStorageStatusReporter statusReporter) {
		this.dataSource = sharedStorage.dataSource;
		this.jdbcStorage = sharedStorage.jdbcStorage;
		this.gmDbStorage = sharedStorage.gmDbStorage;
		this.statusReporter = statusReporter;
	}

	@Override
	public void run() {
		Lock lock = jdbcStorage.getLock("dcsa-migration-lock");
		lock.lock();
		try {
			_doUpgrade();

		} finally {
			lock.unlock();
		}
	}

	private void _doUpgrade() {
		checkPreconditions();

		try {
			loadAccessNames();
			doMigration();
			statusReporter.onActionFinished(SharedStorageStatus.UPGRADED, null);

		} catch (Exception e) {
			statusReporter.onActionFinished(SharedStorageStatus.UPGRADING_ERROR, e);
			throw e;
		}
	}

	private void checkPreconditions() {
		try (Connection c = dataSource.getConnection()) {
			if (tableExists(c, GmDbDcsaSharedStorage.DEFAULT_OPS_TABLE_NAME) != null)
				throw new IllegalStateException(
						"Cannot upgrade DCSA storage as new table '" + GmDbDcsaSharedStorage.DEFAULT_OPS_TABLE_NAME + "' already exists!");

			if (tableExists(c, GmDbDcsaSharedStorage.DEFAULT_RES_TABLE_NAME) != null)
				throw new IllegalStateException(
						"Cannot upgrade DCSA storage as new table '" + GmDbDcsaSharedStorage.DEFAULT_RES_TABLE_NAME + "' already exists!");

			if (tableExists(c, OLD_TABLE_NAME) == null)
				throw new IllegalStateException("Cannot upgrade DCSA storage as old table '" + OLD_TABLE_NAME + "' does not exist!");

		} catch (SQLException e) {
			throw new RuntimeException("Error while checking which tables exist for migration of JDBC DCSA storage.", e);
		}
	}

	private void loadAccessNames() {
		try (Connection c = dataSource.getConnection()) {
			JdbcTools.withStatement(c, () -> "Retrieving access ids.", st -> {
				ResultSet rs = st.executeQuery("select distinct accessId from " + OLD_TABLE_NAME);
				while (rs.next()) {
					String accessId = rs.getString("accessId");
					accessIds.add(accessId);
				}

			});

		} catch (SQLException e) {
			throw new RuntimeException("Error while retrieving the list names.", e);
		}

		log("Old DCSA storage contained entries for the following accesses: " + accessIds);
	}

	private void doMigration() {
		statusReporter.onStartUpgrade(this);

		gmDbStorage.ensureTable();

		for (String accessId : accessIds)
			migrate(accessId);

		log("Successfully migrated all accesses.");
	}

	private void migrate(String accessId) {
		log("Migrating accesss ==> " + accessId + " <==");

		currentAccessId = accessId;
		loadMigratee(accessId);

		JdbcDcsaIterable dcsaIterable = jdbcStorage.readOperations(accessId, null);
		migratee.setTimeOpsDownload(StringTools.prettyPrintMilliseconds(sw.checkpoint("read"), true));

		int totalSize = dcsaIterable.operations.size();

		migratee.setOpsTotal(totalSize);
		log("Storing " + totalSize + " DCSA operations for: " + accessId);

		int count = 0;
		List<CsaOperation> opsBuffer = newList();

		for (CsaOperation csaOperation : dcsaIterable) {
			long size = ensureHasPayloadIfStoreResource(csaOperation);
			opsBuffer.add(csaOperation);

			count++;
			if (size > 0 || opsBuffer.size() == 500 || count == totalSize) {
				gmDbStorage.storeOperations(accessId, opsBuffer);
				opsBuffer.clear();
				migratee.setOpsDone(count);

				if (size > 0) {
					migratee.setResDone(1 + migratee.getResDone());
					migratee.setResSizeDone(size + migratee.getResSizeDone());
					freePayloadIfStoreResource(csaOperation);
				}
			}
		}
		sw.intermediate("write");

		Date end = new Date();
		migratee.setEnd(end);
		migratee.setTimeTotal(StringTools.prettyPrintMilliseconds(end.getTime() - migratee.getStart().getTime(), true));
		log("Access [" + accessId + "] - Migrating " + count + " operation took: " + sw.getElapsedTimesReport());
	}

	private void loadMigratee(String accessId) {
		migratee = AccessMigrationState.T.create();
		migratee.setAccessId(accessId);
		migratee.setStart(new Date());
		migratee.setTimeTotal("N/A");
		migratee.setTimeOpsDownload("N/A");
		migratee.setTimeResDownload("N/A");
		migratee.setOpsTotal(-1);

		synchronized (migratedAccesses) {
			migratedAccesses.add(migratee);
		}
	}

	private long ensureHasPayloadIfStoreResource(CsaOperation csaOperation) {
		if (!(csaOperation instanceof CsaStoreResource))
			return 0;

		CsaStoreResource sr = (CsaStoreResource) csaOperation;
		String path = sr.getResourceRelativePath();

		if (path == null) {
			log.warn("Resource in access [" + currentAccessId + "] has no relative path, so it is not possible to retrieve it's binary data: " + sr);
			return 0;
		}

		swResDl.resume();
		Map<String, Resource> resources = jdbcStorage.readResource(currentAccessId, singleton(path));
		swResDl.pause();
		migratee.setTimeResDownload(swResDl.getElapsedTimePretty());

		Resource r = resources.get(path);

		if (r == null) {
			log(LogLevel.WARN, "Resource on path [" + path + "] in [" + currentAccessId + "] has no payload.");
			return 0;
		}

		Long size = r.getFileSize();
		log("Read resource " + r.getName() + " [" + r.getMimeType() + ", size: " + size + "]  on path [" + path + "] in: " + currentAccessId);

		sr.setPayload(r);

		return size == null ? 1 : size;
	}

	private void freePayloadIfStoreResource(CsaOperation csaOperation) {
		if (csaOperation instanceof CsaStoreResource)
			((CsaStoreResource) csaOperation).setPayload(null);
	}

	private void log(String msg) {
		log(LogLevel.INFO, msg);
	}

	private void log(LogLevel level, String msg) {
		log.log(level, "[DCSA Upgrade] " + msg);
	}

	public List<AccessMigrationState> migratedAccesses() {
		synchronized (migratedAccesses) {
			return newList(migratedAccesses);
		}
	}

}
