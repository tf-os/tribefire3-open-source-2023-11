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
package com.braintribe.model.processing.query.test;

import org.junit.Rule;
import org.junit.rules.TestName;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.query.planner.QueryPlanner;
import com.braintribe.model.processing.query.test.check.QueryPlanAssemblyChecker;
import com.braintribe.model.processing.query.test.debug.TupleSetViewer;
import com.braintribe.model.processing.query.test.model.Address;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.processing.query.test.repository.IndexConfiguration;
import com.braintribe.model.processing.query.test.repository.RepositoryMock;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.set.TupleSet;

/**
 * 
 */
public class AbstractQueryPlannerTests {

	private static QueryPlanner standardQueryPlanner;

	protected QueryPlanner queryPlanner = getQueryPlanner();

	protected TupleSet queryPlan;

	static {
		IndexConfiguration indexConfiguration = new IndexConfiguration();

		// Person (metric)
		indexConfiguration.addMetricIndex(Person.T, "id");
		indexConfiguration.addMetricIndex(Person.T, "indexedName");
		indexConfiguration.addMetricIndex(Person.T, "indexedInteger");
		indexConfiguration.addMetricIndex(Person.T, "indexedDate");
		// Person (lookup)
		indexConfiguration.addLookupIndex(Person.T, "indexedCompany");

		// ############################################################################################################

		// Company (metric)
		indexConfiguration.addMetricIndex(Company.T, "id");
		indexConfiguration.addMetricIndex(Company.T, "indexedDate");
		// Company (lookup)
		indexConfiguration.addLookupIndex(Company.T, "indexedName");

		indexConfiguration.addMetricIndex(Address.T, "id");

		standardQueryPlanner = new QueryPlanner(new RepositoryMock(indexConfiguration));
	}

	protected QueryPlanner getQueryPlanner() {
		return standardQueryPlanner;
	}

	protected SelectQueryBuilder query() {
		return new SelectQueryBuilder();
	}

	protected void runTest(SelectQuery selectQuery) {
		queryPlan = queryPlanner.buildQueryPlan(selectQuery).getTupleSet();
		TupleSetViewer.view(getTestName(), queryPlan);
	}

	protected QueryPlanAssemblyChecker assertQueryPlan() {
		return new QueryPlanAssemblyChecker(queryPlan);
	}

	protected <T extends GenericEntity> T instance(EntityType<T> entityType, long id) {
		return instance(entityType, id, null);
	}

	protected <T extends GenericEntity> T instance(EntityType<T> entityType, long id, String partition) {
		T result = entityType.create();
		result.setId(id);
		result.setPartition(partition);

		return result;
	}

	@Rule
	public TestName testName = new TestName();

	private String getTestName() {
		return testName.getMethodName();
	}

}
