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
package com.braintribe.util.jdbc.pagination;

/**
 * Taken from org.hibernate.dialect.pagination.LimitHelper.
 */
public class LimitHelper {
	/**
	 * Is a max row limit indicated?
	 *
	 * @param selection The row selection options
	 *
	 * @return Whether a max row limit was indicated
	 */
	public static boolean hasMaxRows(RowSelection selection) {
		return selection != null && selection.getMaxRows() != null && selection.getMaxRows() > 0;
	}

	/**
	 * Should limit be applied?
	 *
	 * @param limitHandler The limit handler
	 * @param selection The row selection
	 *
	 * @return Whether limiting is indicated
	 */
	public static boolean useLimit(LimitHandler limitHandler, RowSelection selection) {
		return limitHandler.supportsLimit() && hasMaxRows( selection );
	}

	/**
	 * Is a first row limit indicated?
	 *
	 * @param selection The row selection options
	 *
	 * @return Whether a first row limit in indicated
	 */
	public static boolean hasFirstRow(RowSelection selection) {
		return getFirstRow( selection ) > 0;
	}

	/**
	 * Retrieve the indicated first row for pagination
	 *
	 * @param selection The row selection options
	 *
	 * @return The first row
	 */
	public static int getFirstRow(RowSelection selection) {
		return ( selection == null || selection.getFirstRow() == null ) ? 0 : selection.getFirstRow();
	}

	private LimitHelper() {
	}
}
