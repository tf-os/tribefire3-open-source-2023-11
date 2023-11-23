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
package com.braintribe.util.jdbc;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;

/**
 * @author peter.gazdik
 */
public class SqlStatement {

	public String sql;
	public List<Object> parameters;

	public SqlStatement() {
		this(null);
	}

	public SqlStatement(String sql) {
		this(sql, newList());
	}

	public SqlStatement(String sql, List<Object> parameters) {
		this.sql = sql;
		this.parameters = parameters;
	}

	@Override
	public SqlStatement clone() {
		return new SqlStatement(this.sql, newList(this.parameters));
	}

	public static class SqlStatementBuilder {
		public StringBuilder sb = new StringBuilder();
		public List<Object> parameters = newList();

		public SqlStatement build() {
			return new SqlStatement(sb.toString(), parameters);
		}
	}

}
