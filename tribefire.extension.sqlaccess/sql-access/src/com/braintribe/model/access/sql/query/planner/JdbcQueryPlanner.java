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
package com.braintribe.model.access.sql.query.planner;

import com.braintribe.model.access.sql.query.oracle.JdbcQuery;
import com.braintribe.model.sql.plan.JdbcQueryPlan;

/**
 * @author peter.gazdik
 */
public class JdbcQueryPlanner {

	public static JdbcQueryPlan plan(JdbcQuery query, JdbcPlannerContext context) {
		return new JdbcQueryPlanner(query, context).plan();
	}

	private final JdbcQuery query;
	private final JdbcPlannerContext context;

	private JdbcQueryPlanner(JdbcQuery query, JdbcPlannerContext context) {
		this.query = query;
		this.context = context;
	}

	private JdbcQueryPlan plan() {
		throw new UnsupportedOperationException("Method 'JdbcQueryPlanner.plan' is not implemented yet!");
	}

}
