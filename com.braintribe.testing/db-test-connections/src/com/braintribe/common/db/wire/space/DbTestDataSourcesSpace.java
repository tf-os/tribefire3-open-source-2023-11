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
package com.braintribe.common.db.wire.space;

import static com.braintribe.wire.api.scope.InstanceConfiguration.currentInstance;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.braintribe.common.db.DbTestConstants;
import com.braintribe.common.db.DbVendor;
import com.braintribe.common.db.wire.contract.DbTestDataSourcesContract;
import com.braintribe.wire.api.annotation.Managed;
import com.zaxxer.hikari.HikariDataSource;

@Managed
public class DbTestDataSourcesSpace implements DbTestDataSourcesContract, DbTestConstants {

	@Override
	public DataSource dataSource(DbVendor dbVendor) {
		switch (dbVendor) {
			case derby:
				return derby();
			case mssql:
				return mssql();
			case mysql:
				return mySql();
			case oracle:
				return oracle();
			case postgres:
				return postgres();
			default:
				throw new IllegalArgumentException("Unknown db vendor: " + dbVendor);
		}
	}

	@Override
	@Managed
	public DataSource derby() {
		HikariDataSource bean = newHikariDataSource();

		bean.setDriverClassName(derbyDriver);
		bean.setJdbcUrl(derbyUrl);

		currentInstance().onDestroy(bean::close);

		validate(bean, "derby");

		return bean;
	}

	@Override
	@Managed
	public DataSource mssql() {
		HikariDataSource bean = newHikariDataSource();

		bean.setDriverClassName(mssqlDriver);
		bean.setJdbcUrl(mssqlUrl);

		currentInstance().onDestroy(bean::close);

		validate(bean, "mssql");

		return bean;
	}

	@Override
	@Managed
	public DataSource mySql() {
		HikariDataSource bean = newHikariDataSource();

		bean.setDriverClassName(mysqlDriver);
		bean.setJdbcUrl(mysqlUrl);

		currentInstance().onDestroy(bean::close);

		validate(bean, "mySql");

		return bean;
	}

	@Override
	@Managed
	public DataSource oracle() {
		HikariDataSource bean = newHikariDataSource();

		bean.setDriverClassName(oracleDriver);
		bean.setJdbcUrl(oracleUrl);

		currentInstance().onDestroy(bean::close);

		validate(bean, "oracle");

		return bean;
	}

	@Override
	@Managed
	public DataSource postgres() {
		HikariDataSource bean = newHikariDataSource();

		bean.setDriverClassName(postgresDriver);
		bean.setJdbcUrl(postgresUrl);

		currentInstance().onDestroy(bean::close);

		validate(bean, "postgres");

		return bean;
	}

	private HikariDataSource newHikariDataSource() {
		HikariDataSource bean = new HikariDataSource();
		bean.setUsername(dbTestUsername);
		bean.setPassword(dbTestPassword);

		bean.setConnectionTimeout(10_000L);
		bean.setMaximumPoolSize(10); // small number for tests

		return bean;
	}

	private HikariDataSource validate(HikariDataSource bean, String vendorName) {
		try (Connection c = bean.getConnection()) {
			c.toString();

		} catch (SQLException e) {
			throw new RuntimeException("Could not connect to " + vendorName + ". Make sure the corresponding docker container is running."
					+ " Scripts for running the container is in github repository called 'docker-databases'.", e);
		}

		return bean;
	}

}
