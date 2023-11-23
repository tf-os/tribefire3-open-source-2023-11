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

import com.braintribe.util.jdbc.pagination.AbstractLimitHandler;
import com.braintribe.util.jdbc.pagination.LimitHelper;
import com.braintribe.util.jdbc.pagination.RowSelection;

public class DB2LimitHandler extends AbstractLimitHandler {

	public static final DB2LimitHandler INSTANCE = new DB2LimitHandler();

	@Override
	public String processSql(String sql, RowSelection selection) {
		if (LimitHelper.hasFirstRow( selection )) {
			//nest the main query in an outer select
			return "select * from ( select inner2_.*, rownumber() over(order by order of inner2_) as rownumber_ from ( "
					+ sql + " fetch first " + getMaxOrLimit( selection ) + " rows only ) as inner2_ ) as inner1_ where rownumber_ > "
					+ selection.getFirstRow() + " order by rownumber_";
		}
		return sql + " fetch first " + getMaxOrLimit( selection ) +  " rows only";
	}

	@Override
	public boolean supportsLimit() {
		return true;
	}

	@Override
	public boolean useMaxForLimit() {
		return true;
	}

	@Override
	public boolean supportsVariableLimit() {
		return false;
	}

}
