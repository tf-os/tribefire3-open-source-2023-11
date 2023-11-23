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
package com.braintribe.model.processing.smart.query.planner;

import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.accessA.CompanyA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.Company;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.smart.query.planner.base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.set.CartesianProduct;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.smartqueryplan.set.DelegateQuerySet;

/**
 * 
 */
public class MultiSource_PrimitiveSelection_PlannerTests extends AbstractSmartQueryPlannerTests {

	@Test
	public void simpleQuery() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.from(Company.T, "c")
				.select("p", "nameA")
				.select("c", "nameA")
				.done()
		;
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(CartesianProduct.T)
				.whereProperty("operands").isListWithSize(2)
					.whereElementAt(0)
						.hasType(DelegateQuerySet.T)
						.whereProperty("delegateAccess").isDelegateAccess("accessA").close()
						.whereProperty("scalarMappings").isListWithSize(1).close()
						.whereDelegateQuery()
							.whereSelection(1)
								.whereElementAt(0).isPropertyOperand("nameA")
							.close(2)
							.whereProperty("froms").isListWithSize(1)
								.whereElementAt(0).isFrom(CompanyA.T)
						.endQuery()
					.close()
					.whereElementAt(1)
						.hasType(DelegateQuerySet.T)
						.whereProperty("delegateAccess").isDelegateAccess("accessA").close()
						.whereProperty("scalarMappings").isListWithSize(1).close()
						.whereDelegateQuery()
							.whereProperty("selections")
								.whereElementAt(0).isPropertyOperand("nameA")
							.close(2)
							.whereProperty("froms").isListWithSize(1)
								.whereElementAt(0).isFrom(PersonA.T)
						.endQuery()
					.close()
				.close()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).isTupleComponent_(1)
				.whereElementAt(1).isTupleComponent_(0)
		;
		// @formatter:on
	}

	@Test
	public void simpleDelegateQueryWithCartesianProduct() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.from(Company.T, "c")
				.select("p", "nameA")
				.select("c", "nameA")
				.where()
					.property("p", "companyNameA").eq().property("c", "nameA")
				.done()
				;
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQuerySet.T)
				.whereProperty("delegateAccess").isDelegateAccess("accessA").close()
				.whereProperty("scalarMappings").isListWithSize(2).close()
				.whereDelegateQuery()
					.whereProperty("selections")
						.whereElementAt(0).isPropertyOperand("nameA").close()
						.whereElementAt(1).isPropertyOperand("nameA").close()
					.close()
					.whereProperty("froms").isListWithSize(2)
						.whereElementAt(0).isFrom(CompanyA.T).close()
						.whereElementAt(1).isFrom(PersonA.T)
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).isTupleComponent_(1)
				.whereElementAt(1).isTupleComponent_(0)
		;
		// @formatter:on
	}

}
