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
package com.braintribe.model.processing.smood;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.query.tools.QueryPlanPrinter;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.QueryPlan;

/**
 * @author peter.gazdik
 */
public class SmoodLogging {

	private static Logger smoodLogger = Logger.getLogger(Smood.class);

	public static void selectQuery(SelectQuery query) {
		if (smoodLogger.isTraceEnabled())
			smoodLogger.trace("Planning select query: " + QueryPlanPrinter.printSafe(query));
	}

	public static void propertyQuery(PropertyQuery query) {
		if (smoodLogger.isTraceEnabled())
			smoodLogger.trace("Smart PropetyQuery: " + QueryPlanPrinter.print(query));
	}

	public static void queryPlan(QueryPlan queryPlan) {
		if (smoodLogger.isTraceEnabled())
			smoodLogger.trace(QueryPlanPrinter.printSafe(queryPlan));
	}

	public static void selectQueryEvaluationFinished() {
		if (smoodLogger.isTraceEnabled())
			smoodLogger.trace("Query evaluation finished!");
	}
}
