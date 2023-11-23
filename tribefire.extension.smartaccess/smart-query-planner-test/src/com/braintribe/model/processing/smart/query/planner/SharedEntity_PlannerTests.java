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

import static com.braintribe.model.generic.GenericEntity.globalId;
import static com.braintribe.model.generic.GenericEntity.partition;

import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.accessA.shared.SourceOwnerA;
import com.braintribe.model.processing.query.smart.test.model.shared.SharedSource;
import com.braintribe.model.processing.query.smart.test.model.smart.shared.SmartSourceOwnerA;
import com.braintribe.model.processing.smart.query.planner.base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.set.Concatenation;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;

/**
 * Tests for such queries which need to be split into multiple smart queries, cause the hierarchy rooted at given source
 * entity is not mapped to exactly one delegate hierarchy. The most simple example of such query is
 * <tt>select ge from GenericEntity ge</tt>.
 */
public class SharedEntity_PlannerTests extends AbstractSmartQueryPlannerTests {

	/** Splitting the query and concatenating. */
	@Test
	public void selectSharedEntity() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("s")
				.from(SharedSource.T, "s")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Concatenation.T)
			.whereProperty("firstOperand")
				.isDelegateQueryAsIs("accessA").close()
			.whereProperty("secondOperand")
				.isDelegateQueryAsIs("accessB").close()
		;
		// @formatter:on
	}

	/** This should be the same as any other normal join use-case. */
	@Test
	public void selectPropertyWhichIsSharedEntity() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("s", "sharedSource")
				.from(SmartSourceOwnerA.T, "s")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereFroms(1).whereElementAt(0).isFrom(SourceOwnerA.T)
						.whereProperty("joins").isSetWithSize(1).whereFirstElement().isJoin("sharedSource")
					.whereSelection(5)
						.whereElementAt(i=0).isPropertyOperand(globalId).whereSource().isJoin("sharedSource").close(2)
						.whereElementAt(++i).isPropertyOperand("id").whereSource().isJoin("sharedSource").close(2)
						.whereElementAt(++i).isPropertyOperand(partition).whereSource().isJoin("sharedSource").close(2)
						.whereElementAt(++i).isPropertyOperand("sourceLocation").whereSource().isJoin("sharedSource").close(2)
						.whereElementAt(++i).isPropertyOperand("uuid").whereSource().isJoin("sharedSource").close(2)
				.endQuery()
				.whereProperty("scalarMappings").isListWithSize(5).close()
			.close()
			.whereProperty("values").isListWithSize(1)
		;
		// @formatter:on
	}

	/** Should do query on itself; */
	@Test
	public void selectSharedProperty_kpaEntity() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("s", "kpaSharedSource")
				.from(SmartSourceOwnerA.T, "s")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand").hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(SourceOwnerA.T)
						.whereSelection(1)
							.whereElementAt(0).isPropertyOperand("kpaSharedSourceUuid").whereSource().isFrom(SourceOwnerA.T).close(2)
					.endQuery()
					.whereProperty("scalarMappings").isListWithSize(1).close()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessS")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(SharedSource.T)
						.whereSelection(5)
							.whereElementAt(i=0).isPropertyOperand(globalId).whereSource().isFrom(SharedSource.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(SharedSource.T).close(2)
							.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(SharedSource.T).close(2)
							.whereElementAt(++i).isPropertyOperand("sourceLocation").whereSource().isFrom(SharedSource.T).close(2)
							.whereElementAt(++i).isPropertyOperand("uuid").whereSource().isFrom(SharedSource.T).close(2)
					.endQuery()
					.whereProperty("scalarMappings").isListWithSize(5).close()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
		;
		// @formatter:on
	}

	/** Should do query on itself; */
	@Test
	public void selectSharedProperty_kpaSet() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("s", "kpaSharedSourceSet")
				.from(SmartSourceOwnerA.T, "s")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand").hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(SourceOwnerA.T)
							.whereProperty("joins").isSetWithSize(1).whereFirstElement().isJoin("kpaSharedSourceUuidSet")
						.whereSelection(1)
							.whereElementAt(0).isSourceOnlyPropertyOperand().whereSource().isJoin("kpaSharedSourceUuidSet").close(2)
					.endQuery()
					.whereProperty("scalarMappings").isListWithSize(1).close()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessS")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(SharedSource.T)
						.whereSelection(5)
							.whereElementAt(i=0).isPropertyOperand(globalId).whereSource().isFrom(SharedSource.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(SharedSource.T).close(2)
							.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(SharedSource.T).close(2)
							.whereElementAt(++i).isPropertyOperand("sourceLocation").whereSource().isFrom(SharedSource.T).close(2)
							.whereElementAt(++i).isPropertyOperand("uuid").whereSource().isFrom(SharedSource.T).close(2)
					.endQuery()
					.whereProperty("scalarMappings").isListWithSize(5).close()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
		;
		// @formatter:on
	}
}
