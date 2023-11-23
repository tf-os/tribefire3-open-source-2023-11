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
package com.braintribe.gm.jdbc.impl;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.braintribe.common.lcd.function.XBiConsumer;
import com.braintribe.gm.jdbc.api.GmColumn;
import com.braintribe.util.jdbc.JdbcTools;

/**
 * @author peter.gazdik
 */
public class GmDbTools {

	public static int bindParameters(PreparedStatement ps, List<?> parameters, int index) throws SQLException {
		for (Object param : parameters)
			bindParameter(ps, index++, param);

		return index;
	}

	private static void bindParameter(PreparedStatement ps, int index, Object parameter) throws SQLException {
		if (parameter instanceof Date)
			ps.setTimestamp(index, toTimeStamp((Date) parameter));
		else if (parameter instanceof Enum<?>)
			ps.setString(index, ((Enum<?>) parameter).name());
		else
			ps.setObject(index, parameter);
	}

	private static Timestamp toTimeStamp(Date d) {
		return d instanceof Timestamp ? (Timestamp) d : new Timestamp(d.getTime());
	}

	public static void doUpdate(Connection c, String sql, XBiConsumer<PreparedStatement, List<GmColumn<?>>> updateTask) {
		JdbcTools.withPreparedStatement(c, sql, () -> "", ps -> {
			List<GmColumn<?>> boundColumns = newList();

			try {
				updateTask.accept(ps, boundColumns);

			} finally {
				for (GmColumn<?> column : boundColumns)
					column.afterStatementExecuted(ps);
			}
		});
	}

}
