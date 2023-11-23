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

import static com.braintribe.utils.lcd.CollectionTools2.newIdentityMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import com.braintribe.gm.jdbc.api.GmColumn;
import com.braintribe.gm.jdbc.api.GmUpdateBuilder;
import com.braintribe.util.jdbc.JdbcTools;
import com.braintribe.util.jdbc.SqlStatement;

/**
 * @author peter.gazdik
 */
public class GmUpdateBuilderImpl implements GmUpdateBuilder {

	private final GmTableImpl table;
	private final Map<GmColumn<?>, Object> values;

	private final Map<GmColumn<?>, Integer> columnToSelectionPosition = newIdentityMap();

	private final SqlStatement st = new SqlStatement();

	private int index = 1;
	private int updated;

	public GmUpdateBuilderImpl(GmTableImpl table, Map<GmColumn<?>, Object> values) {
		this.table = table;
		this.values = values;
	}

	@Override
	public int where(String condition, Object... parameters) {
		writeQuery();
		addConditionToQuery(condition, parameters);

		JdbcTools.withManualCommitConnection(table.db.dataSource, this::describeTask, this::doUpdate);

		return updated;
	}

	private void writeQuery() {
		StringJoiner sjColumns = new StringJoiner(", ");

		for (GmColumn<?> column : values.keySet()) {
			List<String> sqlColumns = column.getSqlColumns();

			columnToSelectionPosition.put(column, index);
			index += sqlColumns.size();

			for (String sqlColumn : sqlColumns)
				sjColumns.add(sqlColumn + " = ?");
		}

		st.sql = "update " + table.getName() + " set " + sjColumns;
	}

	private void addConditionToQuery(String condition, Object... parameters) {
		st.sql += " where " + condition;
		st.parameters.addAll(Arrays.asList(parameters));
	}

	private String describeTask() {
		return "Updating rows in table '" + table.getName() + "'. Values: " + values;
	}

	private void doUpdate(Connection c) {
		GmDbTools.doUpdate(c, st.sql, this::bindAndExecute);
	}

	private void bindAndExecute(PreparedStatement ps, List<GmColumn<?>> boundColumns) throws SQLException {
		for (Entry<GmColumn<?>, Object> entry : values.entrySet()) {
			GmColumn<?> column = entry.getKey();
			Object value = entry.getValue();
			int index = columnToSelectionPosition.get(column);

			((GmColumn<Object>) column).bindParameter(ps, index, value);
			boundColumns.add(column);
		}

		GmDbTools.bindParameters(ps, st.parameters, index);

		this.updated = ps.executeUpdate();
	}

}
