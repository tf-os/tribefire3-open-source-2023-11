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
package com.braintribe.util.jdbc.pagination.dialect;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.braintribe.util.jdbc.pagination.LimitHelper;
import com.braintribe.util.jdbc.pagination.RowSelection;


public class SQLServer2012LimitHandler extends SQLServer2005LimitHandler {

	@SuppressWarnings("hiding")
	public static final SQLServer2012LimitHandler INSTANCE = new SQLServer2012LimitHandler();

	// determines whether the limit handler used offset/fetch or 2005 behavior.
	private boolean usedOffsetFetch;

	public SQLServer2012LimitHandler() {

	}

	@Override
	public boolean supportsLimit() {
		return true;
	}

	@Override
	public boolean supportsVariableLimit() {
		return true;
	}

	@Override
	public String processSql(String sql, RowSelection selection) {
		// SQLServer mandates the following rules to use OFFSET/LIMIT
		//  * An 'ORDER BY' is required
		//  * The 'OFFSET ...' clause is mandatory, cannot use 'FETCH ...' by itself.
		//  * The 'TOP' clause isn't permitted with LIMIT/OFFSET.
		if ( hasOrderBy( sql ) ) {
			if ( !LimitHelper.useLimit( this, selection ) ) {
				return sql;
			}
			return applyOffsetFetch( selection, sql, getInsertPosition( sql ) );
		}
		return super.processSql( sql, selection );
	}

	@Override
	public boolean useMaxForLimit() {
		// when using the offset fetch clause, the max value is passed as-is.
		// SQLServer2005LimitHandler uses start + max values.
		return usedOffsetFetch ? false : super.useMaxForLimit();
	}

	@Override
	public int convertToFirstRowValue(int zeroBasedFirstResult) {
		// When using the offset/fetch clause, the first row is passed as-is
		// SQLServer2005LimitHandler uses zeroBasedFirstResult + 1
		if ( usedOffsetFetch ) {
			return zeroBasedFirstResult;
		}
		return super.convertToFirstRowValue( zeroBasedFirstResult );
	}

	@Override
	public int bindLimitParametersAtEndOfQuery(RowSelection selection, PreparedStatement statement, int index)
	throws SQLException {
		if ( usedOffsetFetch && !LimitHelper.hasFirstRow( selection ) ) {
			// apply just the max value when offset fetch applied
			statement.setInt( index, getMaxOrLimit( selection ) );
			return 1;
		}
		return super.bindLimitParametersAtEndOfQuery( selection, statement, index );
	}

	private String getOffsetFetch(RowSelection selection) {
		if ( !LimitHelper.hasFirstRow( selection ) ) {
			return " offset 0 rows fetch next ? rows only";
		}
		return " offset ? rows fetch next ? rows only";
	}

	private int getInsertPosition(String sql) {
		int position = sql.length() - 1;
		for ( ; position > 0; --position ) {
			char ch = sql.charAt( position );
			if ( ch != ';' && ch != ' ' && ch != '\r' && ch != '\n' ) {
				break;
			}
		}
		return position + 1;
	}

	private String applyOffsetFetch(RowSelection selection, String sql, int position) {
		usedOffsetFetch = true;

		StringBuilder sb = new StringBuilder();
		sb.append( sql.substring( 0, position ) );
		sb.append( getOffsetFetch( selection ) );
		if ( position > sql.length() ) {
			sb.append( sql.substring( position - 1 ) );
		}

		return sb.toString();
	}

	private boolean hasOrderBy(String sql) {
		int depth = 0;

		String lowerCaseSQL = sql.toLowerCase();

		for ( int i = lowerCaseSQL.length() - 1; i >= 0; --i ) {
			char ch = lowerCaseSQL.charAt( i );
			if ( ch == '(' ) {
				depth++;
			}
			else if ( ch == ')' ) {
				depth--;
			}
			if ( depth == 0 ) {
				if ( lowerCaseSQL.startsWith( "order by ", i ) ) {
					return true;
				}
			}
		}
		return false;
	}
}
