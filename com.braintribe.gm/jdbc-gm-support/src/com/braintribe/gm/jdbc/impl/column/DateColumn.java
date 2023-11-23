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
package com.braintribe.gm.jdbc.impl.column;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.braintribe.gm.jdbc.api.GmSelectionContext;
import com.braintribe.gm.jdbc.impl.column.AbstractGmColumn.AbstractSingularGmColumn;
import com.braintribe.util.jdbc.dialect.JdbcDialect;

/**
 * @author peter.gazdik
 */
public class DateColumn extends AbstractSingularGmColumn<Date> {

	private final JdbcDialect dialect;

	public DateColumn(String name, JdbcDialect dialect) {
		super(name);
		this.dialect = dialect;
	}

	@Override
	protected String sqlType() {
		return dialect.timestampType();
	}

	@Override
	protected Class<Date> type() {
		return Date.class;
	}

	@Override
	protected Date tryGetValue(ResultSet rs, GmSelectionContext context) throws SQLException {
		Timestamp ts = rs.getTimestamp(name);
		return ts == null ? ts : new Date(ts.getTime());
	}

	@Override
	protected void tryBind(PreparedStatement ps, int index, Date value) throws SQLException {
		Timestamp ts = value == null ? null : new Timestamp(value.getTime());
		ps.setTimestamp(index, ts);
	}

}
