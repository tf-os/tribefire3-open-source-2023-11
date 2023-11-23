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
package com.braintribe.model.processing.securityservice.usersession.basic.test.wire.space;

import javax.sql.DataSource;

import com.braintribe.common.db.wire.contract.DbTestDataSourcesContract;
import com.braintribe.gm.jdbc.api.GmColumn;
import com.braintribe.gm.jdbc.api.GmDb;
import com.braintribe.gm.jdbc.api.GmTable;
import com.braintribe.model.processing.securityservice.api.UserSessionService;
import com.braintribe.model.processing.securityservice.usersession.service.JdbcUserSessionService;
import com.braintribe.util.jdbc.SchemaEnsuringDataSource;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class DbBasedTestSpace extends BaseTestSpace {

	@Import
	private DbTestDataSourcesContract dbTestDataSources;

	@Override
	public UserSessionService userSessionService() {
		return jdbcService();
	}

	@Managed
	public UserSessionService jdbcService() {
		JdbcUserSessionService bean = new JdbcUserSessionService();
		bean.setDataSource(hibernateEnhancedDataSource());
		bean.setSessionIdProvider(userSessionIdFactory());
		bean.setDefaultUserSessionMaxIdleTime(defaultMaxIdleTime());

		return bean;
	}

	@Managed
	private DataSource hibernateEnhancedDataSource() {
		PersistenceUserSessionEnsuringDataSource bean = new PersistenceUserSessionEnsuringDataSource();
		bean.setDelegate(dbTestDataSources.dataSource(testConfig().getDbVendor()));

		return bean;
	}

	private static class PersistenceUserSessionEnsuringDataSource extends SchemaEnsuringDataSource {

		@Override
		protected void updateSchema() {
			GmDb gmDb = GmDb.newDb(delegate).done();

			GmColumn<String> ID = gmDb.shortString("ID", 1000).primaryKey().notNull().done();
			GmColumn<String> USER_NAME = gmDb.shortString("USER_NAME", 1000).done();
			GmColumn<String> USER_FIRST_NAME = gmDb.shortString("USER_FIRST_NAME", 1000).done();
			GmColumn<String> USER_LAST_NAME = gmDb.shortString("USER_LAST_NAME", 1000).done();
			GmColumn<String> USER_EMAIL = gmDb.shortString("USER_EMAIL", 1000).done();
			GmColumn<String> CREATION_DATE = gmDb.shortString("CREATION_DATE", 1000).done();
			GmColumn<String> FIXED_EXPIRY_DATE = gmDb.shortString("FIXED_EXPIRY_DATE", 1000).done();
			GmColumn<String> EXPIRY_DATE = gmDb.shortString("EXPIRY_DATE", 1000).done();
			GmColumn<String> LAST_ACCESSED_DATE = gmDb.shortString("LAST_ACCESSED_DATE", 1000).done();
			GmColumn<String> MAX_IDLE_TIME = gmDb.shortString("MAX_IDLE_TIME", 1000).done();
			GmColumn<String> EFFECTIVE_ROLES = gmDb.shortString("EFFECTIVE_ROLES", 1000).done();
			GmColumn<String> SESSION_TYPE = gmDb.shortString("SESSION_TYPE", 1000).done();
			GmColumn<String> CREATION_INTERNET_ADDRESS = gmDb.shortString("CREATION_INTERNET_ADDRESS", 1000).done();
			GmColumn<String> CREATION_NODE_ID = gmDb.shortString("CREATION_NODE_ID", 1000).done();
			GmColumn<String> PROPERTIES = gmDb.shortString("PROPERTIES", 1000).done();
			GmColumn<String> ACQUIRATION_KEY = gmDb.shortString("ACQUIRATION_KEY", 1000).done();
			GmColumn<Boolean> BLOCKS_AUTHENTICATION_AFTER_LOGOUT = gmDb.booleanCol("BLOCKS_AUTHENTICATION_AFTER_LOGOUT").done();
			GmColumn<Boolean> CLOSED = gmDb.booleanCol("CLOSED").done();

			GmTable table = gmDb.newTable("TF_US_PERSISTENCE_USER_SESSION") //
					.withColumns( //
							ID, USER_NAME, //
							USER_FIRST_NAME, //
							USER_LAST_NAME, //
							USER_EMAIL, //
							CREATION_DATE, //
							FIXED_EXPIRY_DATE, //
							EXPIRY_DATE, //
							LAST_ACCESSED_DATE, //
							MAX_IDLE_TIME, //
							EFFECTIVE_ROLES, //
							SESSION_TYPE, //
							CREATION_INTERNET_ADDRESS, //
							CREATION_NODE_ID, //
							PROPERTIES, //
							ACQUIRATION_KEY, //
							BLOCKS_AUTHENTICATION_AFTER_LOGOUT, //
							CLOSED) //
					.done();

			table.ensure();
		}

	}

}
