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
package com.braintribe.gm.jdbc.api;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.List;
import java.util.Map;

import com.braintribe.util.jdbc.JdbcTools;

/**
 * @see GmDb
 * 
 * @author peter.gazdik
 */
public interface GmSelectBuilder {

	List<GmRow> rows();

	/**
	 * Returns a linked map containing the results in correct order, after doing a two phase loading with multiple threads. In the returned map the
	 * key is the table's primary key, and the value is the corresponding {@link GmRow}.
	 * <p>
	 * This makes sense if many rows are retrieved and selected columns store data in LOBs, i.e. the data won't be retrieved with a single query
	 * statement, but a new connection has to be created per LOB. This is done in parallel, with each thread loading {@code batchSize} number of
	 * elements.
	 * <p>
	 * LOADING PROCESS: In phase 1, all ids are retrieved in a single query, using the original condition, ordering and pagination (if provided). In
	 * phase 2 id's are split into batches of up to {@code batchSize} elements, and for each batch the relevant rows are retrieved. This second query
	 * therefore doesn't use pagination nor ordering.
	 * <p>
	 * IMPORTANT: Because this loading doesn't happen as a single JDBC transaction, it is possible that the row for some ids won't be found, and the
	 * value can thus be null. This would be the case if the corresponding row was deleted between phase 1 and 2. It is up to caller to decide what to
	 * do in such a situation (repeat the query, throw an exception, silently accept the result or try to avoid by some locking mechanism).
	 */
	Map<Object, GmRow> rowsInBatchesOf(int batchSize);

	/**
	 * Similar to {@link #rowsInBatchesOf(int)}, but instead of loading ids based on current query the ids given here are used. This means that any
	 * condition, ordering or pagination are ignored.
	 */
	Map<Object, GmRow> rowsInBatchesOf(List<Object> ids, int batchSize);

	/**
	 * A helper method to allow building the whereClause based on certain conditions without breaking the fluent API.
	 * <p>
	 * If <tt>true</tt> is passed, this method returns this select builder, so the overall effect is if is this "when" method wasn't even called.
	 * <p>
	 * If <tt>false</tt> is passed , this method returns a special select builder which ignores the next "where" method, so the overall effect is as
	 * if this "when" method and the subsequent "where" method weren't even called.
	 */
	GmSelectBuilder when(boolean necessity);

	/**
	 * Shortcut for {@code where(column.getSingleSqlColumn() + " = ?", value)}. The condition can only be applied on {@link GmColumn}s backed by a
	 * single column in the underlying DB.
	 * 
	 * @see #where(String, Object...)
	 */
	default <T> GmSelectBuilder whereColumn(GmColumn<T> column, T value) {
		return where(column.getSingleSqlColumn() + " = ?", value);
	}

	default <T> GmSelectBuilder whereColumnIn(GmColumn<T> column, T... params) {
		return whereIn(column.getSingleSqlColumn(), params);
	}

	default <T> GmSelectBuilder whereColumnInValues(GmColumn<T> column, List<T> params) {
		return whereInValues(column.getSingleSqlColumn(), params);
	}

	default <T> GmSelectBuilder whereIn(String expression, T... params) {
		return whereInValues(expression, asList(params));
	}

	default <T> GmSelectBuilder whereInValues(String expression, List<T> params) {
		return whereValues(expression + " in " + JdbcTools.questionMarks(params.size()), params);
	}

	/**
	 * Adds a condition and its parameter values to the query.
	 * <p>
	 * See documentation on individual column types, like {@link GmDb#shortString255(String)} to find out if given column guarantees a single SQL DB
	 * column.
	 * <p>
	 * Example: {@code where("country = ? and population > ?", "Japan", 1_000_000)}
	 */
	default GmSelectBuilder where(String condition, Object... params) {
		return whereValues(condition, asList(params));
	}

	GmSelectBuilder whereValues(String condition, List<?> params);

	/**  */
	GmSelectBuilder orderBy(String orderByClause);

	default GmSelectBuilder limit(int limit) {
		return limitAndOffset(limit, 0);
	}

	GmSelectBuilder limitAndOffset(int limit, int offset);

	/** Specifies the {@link GmLobLoadingMode} for given {@link GmColumn} */
	GmSelectBuilder lobLoading(GmColumn<?> column, GmLobLoadingMode mode);

}
