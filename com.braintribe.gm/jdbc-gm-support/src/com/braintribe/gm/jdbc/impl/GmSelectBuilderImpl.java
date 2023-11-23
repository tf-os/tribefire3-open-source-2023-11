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
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.braintribe.exception.Exceptions;
import com.braintribe.gm.jdbc.api.GmColumn;
import com.braintribe.gm.jdbc.api.GmLobLoadingMode;
import com.braintribe.gm.jdbc.api.GmRow;
import com.braintribe.gm.jdbc.api.GmSelectBuilder;
import com.braintribe.gm.jdbc.api.GmSelectionContext;
import com.braintribe.util.jdbc.JdbcTools;
import com.braintribe.util.jdbc.SqlStatement;
import com.braintribe.util.jdbc.SqlStatement.SqlStatementBuilder;
import com.braintribe.util.jdbc.pagination.LimitHandler;
import com.braintribe.util.jdbc.pagination.RowSelection;
import com.braintribe.utils.lcd.CollectionTools;
import com.braintribe.utils.lcd.NullSafe;

/**
 * @author peter.gazdik
 */
public class GmSelectBuilderImpl implements GmSelectBuilder {

	private final GmDbQuery dbQuery = new GmDbQuery();

	private final GmTableImpl table;

	public GmSelectBuilderImpl(GmTableImpl table, Set<GmColumn<?>> selectedColumns) {
		this.table = table;
		this.dbQuery.selectedColumns = selectedColumns;
	}

	@Override
	public GmSelectBuilder when(boolean necessity) {
		return necessity ? this : new IgnoringNextConditionBuilder();
	}

	private class IgnoringNextConditionBuilder implements GmSelectBuilder {
		// @formatter:off
		@Override public GmSelectBuilder whereValues(String condition, List<?> params) { return GmSelectBuilderImpl.this; }
		@Override public GmSelectBuilder when(boolean necessity) { return GmSelectBuilderImpl.this; }
		@Override public GmSelectBuilder orderBy(String orderByClause) { return GmSelectBuilderImpl.this; }
		@Override public GmSelectBuilder limitAndOffset(int limit, int offset) { return GmSelectBuilderImpl.this; }
		@Override public GmSelectBuilder lobLoading(GmColumn<?> column, GmLobLoadingMode mode) { return GmSelectBuilderImpl.this; }		

		@Override public List<GmRow> rows() { return throwError(); }
		@Override public Map<Object, GmRow> rowsInBatchesOf(int batchSize) { return throwError(); }
		@Override public Map<Object, GmRow> rowsInBatchesOf(List<Object> ids, int batchSize) { return throwError(); }		

		private <T> T throwError() { throw new IllegalStateException("Cannot resolve 'rows' after 'when' method was used."); }
		// @formatter:on
	}

	@Override
	public GmSelectBuilder whereValues(String whereCondition, List<?> whereParams) {
		if (dbQuery.conditions == 0)
			dbQuery.whereCondition = whereCondition;
		else {
			if (dbQuery.conditions == 1)
				dbQuery.whereCondition = "(" + dbQuery.whereCondition + ")";

			dbQuery.whereCondition += " and (" + whereCondition + ")";
		}

		dbQuery.whereParams.addAll(whereParams);
		dbQuery.conditions++;

		return this;
	}

	@Override
	public GmSelectBuilder orderBy(String orderByClause) {
		dbQuery.orderByClause = orderByClause;
		return this;
	}

	@Override
	public GmSelectBuilder limitAndOffset(int limit, int offset) {
		dbQuery.limit = limit;
		dbQuery.offset = offset;
		return this;
	}

	@Override
	public GmSelectBuilder lobLoading(GmColumn<?> column, GmLobLoadingMode mode) {
		dbQuery.loadingModes.put(column, mode);
		return this;
	}

	// ###############################################
	// ## . . . . Multi-Threaded Querying . . . . . ##
	// ###############################################

	@Override
	public Map<Object, GmRow> rowsInBatchesOf(int batchSize) {
		List<Object> ids = selectIds();

		return rowsInBatchesOf(ids, batchSize);
	}

	@Override
	public Map<Object, GmRow> rowsInBatchesOf(List<Object> ids, int batchSize) {
		if (ids.isEmpty())
			return emptyMap();

		if (ids.size() < batchSize)
			batchSize = ids.size();

		List<Future<Map<Object, GmRow>>> futures = submitBatchQueries(ids, batchSize);

		return buildResultFromFutures(ids, futures);
	}

	private List<Object> selectIds() {
		GmSelectBuilderImpl gsb = new GmSelectBuilderImpl(table, singleton(table.primaryKeyColumn));
		gsb.dbQuery.loadFrom(dbQuery);

		return gsb.rows().stream() //
				.map(row -> row.getValue(table.primaryKeyColumn)) //
				.collect(Collectors.toList());
	}

	private List<Future<Map<Object, GmRow>>> submitBatchQueries(List<Object> ids, int batchSize) {
		SqlStatement batchStatement = getBatchStatement(batchSize);

		ExecutorService executor = table.db.getExecutor();

		List<List<Object>> batches = CollectionTools.splitList(ids, batchSize);
		int batchCount = batches.size();

		List<Future<Map<Object, GmRow>>> futures = newList(batchCount);

		for (int i = 0; i < batchCount; i++) {
			List<Object> batch = ensureSize(batches.get(i), batchSize);

			int batchId = i;
			futures.add(executor.submit(() -> batchRows(batchId, batchCount, batchStatement, batch)));
		}

		return futures;
	}

	private List<Object> ensureSize(List<Object> batch, int batchSize) {
		if (batch.size() < batchSize) {
			// we need a copy because the batch we have is a sub-list of the original ids list
			batch = newList(batch);
			while (batch.size() < batchSize)
				batch.add(null);
		}

		return batch;
	}

	private SqlStatement getBatchStatement(int batchSize) {
		dbQuery.selectedColumns.add(table.primaryKeyColumn);

		SqlStatementBuilder ssb = selectFromWhereStatement();
		ssb.sb.append(dbQuery.whereCondition == null ? " where " : " and ");
		ssb.sb.append(table.primaryKeyColumn.getSingleSqlColumn()) //
				.append(" in ") //
				.append(JdbcTools.questionMarks(batchSize));

		return ssb.build();
	}

	private Map<Object, GmRow> batchRows(int i, int batchCount, SqlStatement st, List<Object> ids) {
		List<GmRow> rowList = newList();

		SqlStatement currentSt = st.clone();
		currentSt.parameters.addAll(ids);

		JdbcTools.withManualCommitConnection(table.db.dataSource,
				() -> "Retrieving rows for table: " + table.getName() + ". " + i + " /" + batchCount, c -> {

					JdbcTools.withPreparedStatement(c, currentSt, () -> "", ps -> {
						GmDbTools.bindParameters(ps, currentSt.parameters, 1);

						executeQuery(ps, rowList);
					});

				});

		return rowList.stream() //
				.collect(Collectors.toMap( //
						row -> row.getValue(table.primaryKeyColumn), //
						row -> row));
	}

	private Map<Object, GmRow> buildResultFromFutures(List<Object> ids, List<Future<Map<Object, GmRow>>> futures) {
		Map<Object, GmRow> result = new LinkedHashMap<>();
		for (Object id : ids)
			result.put(id, null);

		for (Future<Map<Object, GmRow>> f : futures) {
			try {
				Map<Object, GmRow> batchResult = f.get();
				result.putAll(batchResult);

			} catch (InterruptedException e) {
				throw new RuntimeException("Interrupted while waiting for batch queries to finish.", e);

			} catch (Exception e) {
				throw Exceptions.unchecked(e, "Error while waiting for batch queries to finish.");
			}
		}

		return result;
	}

	// ###############################################
	// ## . . . . Single Threaded Querying . . . . .##
	// ###############################################

	@Override
	public List<GmRow> rows() {
		initPagination();

		SqlStatement st = sqlStatement();

		List<GmRow> result = newList();

		JdbcTools.withManualCommitConnection(table.db.dataSource, () -> "Retrieving rows for table: " + table.getName(), c -> {
			JdbcTools.withPreparedStatement(c, st, () -> "", ps -> {
				int index = maybeBindLimitPramsAtStartOfQuery(ps);

				index = GmDbTools.bindParameters(ps, st.parameters, index);

				maybeBindLimitPramsAtEndOfQuery(ps, index);

				executeQuery(ps, result);
			});
		});

		return result;
	}

	private RowSelection selection;
	private LimitHandler limitHandler;

	private SqlStatement sqlStatement() {
		SqlStatementBuilder ssb = selectFromWhereStatement();

		if (dbQuery.orderByClause != null)
			ssb.sb.append(" order by " + dbQuery.orderByClause);

		SqlStatement result = ssb.build();

		if (hasPagination())
			result.sql = limitHandler.processSql(result.sql, selection);

		return result;
	}

	private SqlStatementBuilder selectFromWhereStatement() {
		SqlStatementBuilder ssb = new SqlStatementBuilder();

		addSelect(ssb.sb);
		addFrom(ssb.sb);
		addWhere(ssb);

		return ssb;
	}

	private void addSelect(StringBuilder sb) {
		sb.append("select ");

		for (GmColumn<?> column : dbQuery.selectedColumns)
			for (String sqlColumn : column.getSqlColumns()) {
				sb.append(sqlColumn);
				sb.append(", ");
			}

		sb.setLength(sb.length() - 2);
	}

	private void addFrom(StringBuilder sb) {
		sb.append(" from ");
		sb.append(table.getName());
	}

	private void addWhere(SqlStatementBuilder ssb) {
		if (dbQuery.whereCondition != null) {
			ssb.sb.append(" where " + dbQuery.whereCondition);
			ssb.parameters.addAll(dbQuery.whereParams);
		}
	}

	private void initPagination() {
		if (hasPagination()) {
			selection = new RowSelection(dbQuery.limit, dbQuery.offset);
			limitHandler = JdbcTools.getLimitHandler(table.db.dialect);
		}
	}

	private int maybeBindLimitPramsAtStartOfQuery(PreparedStatement ps) throws SQLException {
		return hasPagination() ? 1 + limitHandler.bindLimitParametersAtStartOfQuery(selection, ps, 1) : 1;
	}

	private void maybeBindLimitPramsAtEndOfQuery(PreparedStatement ps, int index) throws SQLException {
		if (hasPagination())
			limitHandler.bindLimitParametersAtEndOfQuery(selection, ps, index);
	}

	private boolean hasPagination() {
		return dbQuery.limit > 0;
	}

	private void executeQuery(PreparedStatement ps, List<GmRow> rowList) throws SQLException {
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next())
				rowList.add(newRow(rs));
		}
	}

	private GmRow newRow(ResultSet rs) {
		IdentityHashMap<GmColumn<?>, Object> rowValues = newIdentityMap();

		for (GmColumn<?> column : dbQuery.selectedColumns) {
			Object value = column.getValue(rs, dbQuery);

			rowValues.put(column, value);
		}

		return new GmRowImpl(rowValues);
	}

	/* package */ static class GmDbQuery implements GmSelectionContext {

		public Set<GmColumn<?>> selectedColumns;

		public String whereCondition;
		public List<Object> whereParams = newList();
		public int conditions = 0;

		public String orderByClause;

		public int limit;
		public int offset;

		public final Map<GmColumn<?>, GmLobLoadingMode> loadingModes = newMap();

		@Override
		public GmLobLoadingMode lobLoadingMode(GmColumn<?> column) {
			return NullSafe.get(loadingModes.get(column), GmLobLoadingMode.ALL);
		}

		public void loadFrom(GmDbQuery other) {
			whereCondition = other.whereCondition;
			whereParams = other.whereParams;
			conditions = other.conditions;
			orderByClause = other.orderByClause;
			limit = other.limit;
			offset = other.offset;
		}

	}
}
