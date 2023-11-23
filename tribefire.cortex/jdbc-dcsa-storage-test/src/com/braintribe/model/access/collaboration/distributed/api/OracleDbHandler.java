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
package com.braintribe.model.access.collaboration.distributed.api;

import java.util.Date;

import javax.sql.DataSource;

import org.junit.Ignore;

import com.braintribe.model.processing.lock.db.impl.DbLockManager;
import com.braintribe.utils.DateTools;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Special DBHandler that connects to a local, running Oracle. This is for local test purposes only.
 */
@Ignore
public class OracleDbHandler implements DbHandler {

	protected DataSource dataSource = null;
	protected DbLockManager dbLockManager;

	@Override
	public void initialize() throws Exception {
		dataSource();
		lockManager();
	}

	@Override
	public DataSource dataSource() {
		if (dataSource == null) {
			HikariDataSource bean = new HikariDataSource();
			try {
				bean.setDriverClassName("oracle.jdbc.pool.OracleDataSource");
			} catch (Exception e) {
				throw new RuntimeException("Could not set driver class.", e);
			}
			bean.setJdbcUrl("jdbc:oracle:thin:@localhost:1521/orcl12c");
			bean.setUsername("C##DFOX5");
			bean.setPassword("developer");
			dataSource = bean;
		}
		return dataSource;
	}

	@Override
	public DbLockManager lockManager() {
		if (dbLockManager == null) {
			DbLockManager bean = new DbLockManager();
			bean.setDataSource(dataSource());
			bean.setAutoUpdateSchema(false);
			bean.postConstruct();
			dbLockManager = bean;
		}
		return dbLockManager;
	}

	/* package */ static void print(String text) {
		System.out.println(DateTools.encode(new Date(), DateTools.LEGACY_DATETIME_WITH_MS_FORMAT) + " [Master]: " + text);
	}

	@Override
	public void destroy() throws Exception {
		// Do nothing
	}

}
