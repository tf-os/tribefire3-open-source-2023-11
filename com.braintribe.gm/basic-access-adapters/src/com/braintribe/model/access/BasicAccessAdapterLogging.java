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
package com.braintribe.model.access;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.query.tools.QueryPlanPrinter;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.QueryPlan;

/**
 * @author peter.gazdik
 */
public class BasicAccessAdapterLogging {

	private static Logger baaLogger = Logger.getLogger(BasicAccessAdapter.class);

	public static void selectQuery(SelectQuery query) {
		if (baaLogger.isTraceEnabled()) {
			baaLogger.trace("Planning select query: " + QueryPlanPrinter.printSafe(query));
		}
	}

	public static void propertyQuery(PropertyQuery query) {
		if (baaLogger.isTraceEnabled()) {
			baaLogger.trace("Smart PropetyQuery: " + QueryPlanPrinter.print(query));
		}
	}

	public static void queryPlan(QueryPlan queryPlan) {
		if (baaLogger.isTraceEnabled()) {
			baaLogger.trace(QueryPlanPrinter.printSafe(queryPlan));
		}
	}

	public static void selectQueryEvaluationFinished() {
		if (baaLogger.isTraceEnabled()) {
			baaLogger.trace("Query evaluation finished!");
		}
	}
}
