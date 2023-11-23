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
package com.braintribe.model.access.sql.dialect;

/**
 * @author peter.gazdik
 */
public class DerbySqlDialect extends DefaultSqlDialect {

	@SuppressWarnings("hiding")
	public static final DerbySqlDialect INSTANCE = new DerbySqlDialect();

	private DerbySqlDialect() {
	}

	@Override
	public String showDatabasesLike(String databaseName) {
		throw new UnsupportedOperationException("'Show databases' is not supported with Derby dialect.");
	}

	@Override
	public String createDatabase(String databaseName) {
		throw new UnsupportedOperationException("'Create database' is not supported with Derby dialect.");
	}

	@Override
	public String showTablesLike(String tableName) {
		return String.format("select * from sys.systables where tabletype = 'T' AND tablename like '%s'", tableName);
	}

}
