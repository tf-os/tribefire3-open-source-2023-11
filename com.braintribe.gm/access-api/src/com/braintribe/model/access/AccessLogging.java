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
import com.braintribe.logging.Logger.LogLevel;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processing.query.stringifier.BasicQueryStringifier;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.SelectQuery;

/**
 * Needs {@link BasicQueryStringifier} <br>
 * Needs com.braintribe.model.processing.manipulation.marshaller.ManipulationStringifier <br>
 * 
 * @author peter.gazdik
 */
public class AccessLogging {

	protected Logger queryLogger;
	protected Logger manipulationLogger;

	protected LogLevel entityQueryLevel = LogLevel.TRACE;
	protected LogLevel propertyQueryLevel = LogLevel.TRACE;
	protected LogLevel selectQueryLevel = LogLevel.TRACE;
	protected LogLevel manipulationLevel = LogLevel.TRACE;

	public AccessLogging(String loggerRoot, Class<?> clazz) {
		queryLogger = Logger.getLogger(loggerRoot + ".query", clazz);
		manipulationLogger = Logger.getLogger(loggerRoot + ".manipulation", clazz);
	}

	public void entityQuery(EntityQuery query) {
		queryLogger.log(entityQueryLevel, () -> printQuery(query));
	}

	public void propertyQuery(PropertyQuery query) {
		queryLogger.log(propertyQueryLevel, () -> printQuery(query));
	}

	public void selectQuery(SelectQuery query) {
		queryLogger.log(selectQueryLevel, () -> printQuery(query));
	}

	public void manipulation(ManipulationRequest mr) {
		manipulationLogger.log(manipulationLevel, () -> printManipulation(mr.getManipulation()));
	}

	protected String printQuery(Query query) {
		return query.stringify();
	}

	protected String printManipulation(Manipulation m) {
		return m.stringify();
	}
}
