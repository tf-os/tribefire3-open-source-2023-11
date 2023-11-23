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
package com.braintribe.model.access.security.query;

import java.util.List;

import org.fest.assertions.Assertions;

import com.braintribe.model.access.security.common.AbstractSecurityAspectTest;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;

public class AbstractQueryingTest extends AbstractSecurityAspectTest {

	// ####################################
	// ## . . . . . Assertions . . . . . ##
	// ####################################

	/** Executes given query and checks the number of entities returned. */
	protected List<GenericEntity> assertReturnedEntities(EntityQuery query, int expectedCount) {
		if (userRoles == null)
			throw new RuntimeException("Error in test - no user role defined.");

		List<GenericEntity> entities = queryEntities(query);
		Assertions.assertThat(entities).isNotNull().as("Wrong number of entities returned from the query").hasSize(expectedCount);
		return entities;
	}

	protected void assertResultIsEmpty(EntityQuery query) {
		List<GenericEntity> entities = queryEntities(query);
		Assertions.assertThat(entities).isNotNull().isEmpty();
	}

	protected List<GenericEntity> queryEntities(EntityQuery query) {
		EntityQueryResult queryResult = aopAccess.queryEntities(query);
		return queryResult.getEntities();
	}

	protected void assertQueriedPropertyNull(PropertyQuery query) {
		Object result = queryProperty(query);
		Assertions.assertThat(result).isNull();
	}

	protected void assertQueriedProperty(PropertyQuery query, Object value) {
		Object result = queryProperty(query);
		Assertions.assertThat(result).isEqualTo(value);
	}

	protected Object queryProperty(PropertyQuery query) {
		PropertyQueryResult result = aopAccess.queryProperty(query);
		return result.getPropertyValue();
	}

	protected List<?> query(SelectQuery query) {
		SelectQueryResult queryResult = aopAccess.query(query);
		return queryResult.getResults();
	}

}
