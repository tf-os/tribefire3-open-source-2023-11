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
package com.braintribe.model.access.smart;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.smart.query.planner.SmartQueryPlanPrinter;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.smartqueryplan.SmartQueryPlan;

/**
 * This exists to minimize the logging-related changes in normal logic code.
 * 
 * @author peter.gazdik
 */
public class SmartLogging {

	private static Logger smartAccessLogger = Logger.getLogger(SmartAccess.class);

	public static void selectQuery(SelectQuery query) {
		if (smartAccessLogger.isTraceEnabled())
			smartAccessLogger.trace("Planning smart select query: " + SmartQueryPlanPrinter.printSafe(query));
	}

	public static void propertyQuery(PropertyQuery query) {
		if (smartAccessLogger.isTraceEnabled())
			smartAccessLogger.trace("Smart PropetyQuery: " + SmartQueryPlanPrinter.print(query));
	}

	public static void queryPlan(SmartQueryPlan queryPlan) {
		if (smartAccessLogger.isTraceEnabled())
			smartAccessLogger.trace(SmartQueryPlanPrinter.printSafe(queryPlan));
	}

	public static void queryResult(SelectQueryResult result) {
		if (smartAccessLogger.isTraceEnabled())
			smartAccessLogger.trace("Query result rows count: " + result.getResults().size());
	}

}
