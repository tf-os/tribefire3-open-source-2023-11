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
package com.braintribe.model.processing.smart.query.planner2.shared;

import org.junit.Test;

import com.braintribe.model.processing.query.smart.test2._common.SmartModelTestSetup;
import com.braintribe.model.processing.query.smart.test2.shared.DelegateAsIsSmartSetup;
import com.braintribe.model.processing.query.smart.test2.shared.model.shared.SharedEntity;
import com.braintribe.model.processing.query.smart.test2.shared.model.shared.SharedFile;
import com.braintribe.model.processing.query.smart.test2.shared.model.shared.SharedFileDescriptor;
import com.braintribe.model.processing.query.smart.test2.shared.model.shared.SharedSource;
import com.braintribe.model.processing.smart.query.planner2._base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.set.Concatenation;

/**
 * @author peter.gazdik
 */
public class Shared_DelegateAsIs_PlannerTest extends AbstractSmartQueryPlannerTests {

	@Override
	protected SmartModelTestSetup getSmartModelTestSetup() {
		return DelegateAsIsSmartSetup.DELEGATE_AS_IS_SETUP;
	}

	@Test
	public void selectAsIsEntity() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("s")
				.from(SharedSource.T, "s")
				.done();
		// @formatter:on

		runTest(selectQuery);

		assertQueryPlan(1).isDelegateQueryAsIs("accessA");
	}

	@Test
	public void selectAsIsOnUnmappedSuperType() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("s")
				.from(SharedEntity.T, "s")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(1).hasType(Concatenation.T)
			.whereProperty("firstOperand").isDelegateQueryAsIs("accessA")
				.whereDelegateQuery()
					.whereFroms(1).whereElementAt(0).isFrom(SharedFile.T)
				.endQuery()
			.close()
			.whereProperty("secondOperand").hasType(Concatenation.T)
				.whereProperty("firstOperand").isDelegateQueryAsIs("accessA")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(SharedFileDescriptor.T)
					.endQuery()
				.close()
				.whereProperty("secondOperand").isDelegateQueryAsIs("accessA")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(SharedSource.T)
					.endQuery()
				.close()
		;
		// @formatter:on
	}

	@Test
	public void selectAsIsEntity_CollectionCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("s")
				.from(SharedSource.T, "s")
				.where()
					.value("one").in().property("s", "stringSet")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(1)
			.isDelegateQueryAsIs("accessA")
			.whereDelegateQuery()
				.whereFroms(1)
					.whereFirstElement()
						.isFrom(SharedSource.T)
							.whereProperty("joins")
								.isNullOrEmptySet()
		;
		// @formatter:on
	}


}
