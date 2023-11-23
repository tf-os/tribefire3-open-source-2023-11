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
package com.braintribe.model.processing.locking.db.impl;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.model.processing.locking.db.impl.DbLocking.DbRwLock;
import com.braintribe.util.jdbc.JdbcTools;
import com.braintribe.utils.CollectionTools;

/**
 * @author peter.gazdik
 */
/* package */ class DbLockRefresher {

	private static final int UPDATE_BATCH_SIZE = 100;

	private final DbLocking dbLocking;

	private final List<DbRwLock> s_locks = newList();
	private volatile int nLocks = 0;

	public DbLockRefresher(DbLocking dbLocking) {
		this.dbLocking = dbLocking;
	}

	public synchronized void startRefreshing(DbRwLock rwLock) {
		s_locks.add(rwLock);
		nLocks++;
	}

	public synchronized void stopRefreshing(DbRwLock rwLock) {
		s_locks.remove(rwLock);
		nLocks--;
	}

	public void refreshLockedLocks() {
		if (nLocks == 0)
			return;

		List<DbRwLock> locksToRefresh = locksToRefresh();
		if (locksToRefresh.isEmpty())
			return;

		refresh(locksToRefresh);
	}

	private synchronized List<DbRwLock> locksToRefresh() {
		return newList(s_locks);
	}

	private void refresh(List<DbRwLock> locksToRefresh) {
		Set<String> lockIds = locksToRefresh.stream() //
				.map(dbRwLock -> dbRwLock.id) //
				.collect(Collectors.toSet());

		long current = System.currentTimeMillis();
		long expires = current + dbLocking.lockExpirationInMs;

		Timestamp expiresTs = new Timestamp(expires);

		List<List<String>> lockIdBatches = CollectionTools.split(lockIds, UPDATE_BATCH_SIZE);

		JdbcTools.withConnection(dbLocking.dataSource, true, () -> "Refreshing locks " + lockIds, c -> {

			for (List<String> lockIdBatch : lockIdBatches) {
				String sql = "update TF_LOCKS set expires = ? where id in " + JdbcTools.questionMarks(lockIdBatch.size());

				JdbcTools.withPreparedStatement(c, sql, () -> "", ps -> {
					ps.setTimestamp(1, expiresTs);

					int i = 2;
					for (String lockId : lockIdBatch)
						ps.setString(i++, lockId);

					ps.executeUpdate();
				});
			}

		});
	}

}
