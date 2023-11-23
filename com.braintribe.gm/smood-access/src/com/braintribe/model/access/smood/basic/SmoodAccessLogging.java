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
package com.braintribe.model.access.smood.basic;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.query.tools.QueryPlanPrinter;
import com.braintribe.model.query.SelectQuery;

/**
 * @author peter.gazdik
 */
public class SmoodAccessLogging {

	private static Logger smoodAccessLogger = Logger.getLogger(SmoodAccess.class);

	public static void selectQuery(SelectQuery query) {
		if (smoodAccessLogger.isTraceEnabled()) {
			smoodAccessLogger.trace("Planning select query: " + QueryPlanPrinter.printSafe(query));
		}
	}

	public static void selectQueryEvaluationFinished() {
		if (smoodAccessLogger.isTraceEnabled()) {
			smoodAccessLogger.trace("Query evaluation finished!");
		}
	}
}
