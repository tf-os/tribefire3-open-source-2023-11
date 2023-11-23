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

import java.util.Date;

import org.junit.Test;

import com.braintribe.model.processing.query.test.model.Address;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.processing.query.test.repository.IndexConfiguration;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.filter.Conjunction;
import com.braintribe.model.queryplan.filter.GreaterThanOrEqual;
import com.braintribe.model.queryplan.filter.Like;
import com.braintribe.model.queryplan.index.GeneratedMetricIndex;
import com.braintribe.model.queryplan.index.RepositoryIndex;
import com.braintribe.model.queryplan.index.RepositoryMetricIndex;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.IndexRange;
import com.braintribe.model.queryplan.set.IndexSubSet;
import com.braintribe.model.queryplan.set.MergeLookupJoin;
import com.braintribe.model.queryplan.set.MergeRangeJoin;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.set.join.EntityJoin;
import com.braintribe.model.queryplan.set.join.IndexLookupJoin;
import com.braintribe.model.queryplan.set.join.IndexRangeJoin;
import com.braintribe.model.queryplan.value.ValueProperty;
import com.braintribe.model.queryplan.value.range.RangeIntersection;
import com.braintribe.model.queryplan.value.range.SimpleRange;

/**
 * 
 */
public class IndexJoinTests extends AbstractQueryPlannerTests {

	// ####################################
	// ## . . . . . Value Join . . . . . ##
	// ####################################

	@Test
	public void simpleValueJoin() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Company.T, "c")
				.where()
					.property("p", "companyName").eq().property("c", "indexedName")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(IndexLookupJoin.T)
				.whereOperand().isSourceSet_(Person.T)
				.whereProperty("lookupValue").isValueProperty_("companyName")
		;
		// @formatter:on
	}

	@Test
	public void valueJoinWithConditionOnSource() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Company.T, "c")
				.where()
				.conjunction()
					.property("p", "name").like("%") // condition on join-Source
					.property("p", "companyName").eq().property("c", "indexedName")
				.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(IndexLookupJoin.T)
				.whereOperand()
					.hasType(FilteredSet.T)
					.whereOperand().isSourceSet_(Person.T)
		;
		// @formatter:on
	}

	@Test
	public void valueJoinWithIndexedConditionOnSource() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Company.T, "c")
				.where()
					.conjunction()
						.property("p", "indexedName").eq("John Doe") // condition on join-Source
						.property("p", "companyName").eq().property("c", "indexedName")
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(IndexLookupJoin.T)
					.whereOperand()
					.hasType(IndexSubSet.T)
						.whereProperty("lookupIndex")
						.hasType(RepositoryIndex.T)
		;
		// @formatter:on
	}

	@Test
	public void valueJoinWithConditionOnJoinedFrom() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Company.T, "c")
				.where()
					.conjunction()
						.property("c", "name").like("%") // condition on joined From
						.property("p", "companyName").eq().property("c", "indexedName")
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(FilteredSet.T)
				.whereOperand()
					.hasType(IndexLookupJoin.T)
					.whereOperand().isSourceSet_(Person.T)
					.whereProperty("lookupValue")
						.hasType(ValueProperty.T)
						.whereProperty("propertyPath").is_("companyName")
					.close()
				.close()
				.whereProperty("filter")
					.hasType(Like.T)
		;
		// @formatter:on
	}

	/**
	 * The important thing is, that the indexName condition with "SomeCompany" is not chose, but we chose a simple from
	 * for Person and then do the indexed-join.
	 */
	@Test
	public void valueJoinWithConditionOnJoinedFromAndAnExtreJoin() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Company.T, "c")
				.where()
					.conjunction()
						.property("c", "name").like("%") // condition on joined From
						.property("p", "companyName").eq().property("c", "indexedName")
						.property("c", "indexedName").eq().value("SomeCompany")
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(FilteredSet.T)
				.whereOperand()
					.hasType(IndexLookupJoin.T)
					.whereOperand().isSourceSet_(Person.T)
					.whereProperty("lookupValue")
						.hasType(ValueProperty.T)
						.whereProperty("propertyPath").is_("companyName")
					.close()
				.close()
				.whereProperty("filter")
					.hasType(Conjunction.T)
		;
		// @formatter:on
	}

	// ####################################
	// ## . . . . . Range Join . . . . . ##
	// ####################################

	@Test
	public void simpleRangeJoin() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Company.T, "c")
				.where()
					.property("p", "birthDate").ge().property("c", "indexedDate")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(IndexRangeJoin.T)
				.whereOperand().isSourceSet_(Person.T)
				.whereProperty("range").hasType(RangeIntersection.T).close()
				.whereProperty("metricIndex")
					.hasType(RepositoryMetricIndex.T)
					.whereProperty("indexId").is_(IndexConfiguration.indexId(Company.T, "indexedDate"))
				.close()
		;
		// @formatter:on
	}

	@Test
	public void rangeJoinWithExtraRangeCondition() {
		final Date dateParam = new Date(365 * 24 * 3600 * 1000L);

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Company.T, "c")
				.where()
					.conjunction()
						.property("p", "birthDate").ge().property("c", "indexedDate")
						.property("c", "indexedDate").ge().value(dateParam)
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(IndexRangeJoin.T)
				.whereOperand().isSourceSet_(Person.T)
				.whereProperty("range")
					.hasType(RangeIntersection.T)
					.whereProperty("ranges").isSetWithSize(2).close()
				.close()
				.whereProperty("metricIndex")
					.hasType(RepositoryMetricIndex.T)
					.whereProperty("indexId").is_(IndexConfiguration.indexId(Company.T, "indexedDate"))
				.close()
		;
		// @formatter:on
	}

	@Test
	public void rangeJoinWithExtraLookupCondition() {
		final Date dateParam = new Date(365 * 24 * 3600 * 1000L);

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Company.T, "c")
				.where()
					.conjunction()
						.property("p", "birthDate").ge().property("c", "indexedDate")
						.property("c", "indexedDate").eq().value(dateParam)
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(IndexRangeJoin.T)
				.whereOperand().isSourceSet_(Person.T)
				.whereProperty("range")
					.hasType(RangeIntersection.T)
					.whereProperty("ranges").isSetWithSize(3).close()
				.close()
				.whereProperty("metricIndex")
					.hasType(RepositoryMetricIndex.T)
					.whereProperty("indexId").is_(IndexConfiguration.indexId(Company.T, "indexedDate"))
				.close()
		;
		// @formatter:on
	}

	@Test
	public void rangeJoinWithExtraConditionOnBothSides() {
		final Date dateParam = new Date(100000L);

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Company.T, "c")
				.where()
					.conjunction()
						.property("p", "birthDate").ge().property("c", "indexedDate")
						.property("c", "indexedDate").ge().value(dateParam)
						.property("p", "indexedName").ge().value("wtf")						
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(IndexRangeJoin.T)
				.whereOperand().hasType(IndexRange.T).close()
				.whereProperty("range")
					.hasType(RangeIntersection.T)
					.whereProperty("ranges").isSetWithSize(2).close()
				.close()
				.whereProperty("metricIndex")
					.hasType(RepositoryMetricIndex.T)
					.whereProperty("indexId").is_(IndexConfiguration.indexId(Company.T, "indexedDate"))
		;
		// @formatter:on
	}

	// ####################################
	// ## . . Generated Value Join . . . ##
	// ####################################

	@Test
	public void mergeLookupJoin() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Company.T, "c")
				.where()
					.property("p", "companyName").eq().property("c", "name")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(MergeLookupJoin.T)
					.whereOperand().isSourceSet_(Person.T)
					.whereValue()
						.hasType(ValueProperty.T)
						.whereProperty("propertyPath").is_("companyName")
					.close()
					.whereProperty("otherOperand").isSourceSet_(Company.T)
					.whereProperty("otherValue").isValueProperty_("name")
		;
		// @formatter:on
	}

	/**
	 * Real world example - there was a bug that the {@link MergeLookupJoin} was done first, and the {@link EntityJoin}
	 * later (or not at all).
	 */
	@Test
	public void mergeLookupJoinWithJoinOperand() {
		// @formatter:off
		SelectQuery selectQuery = query()
			.select("c")
		   	.from(Address.T, "a")
		   	.from(Company.T, "c")
		   		.join("c", "address", "cA") 
		   	.where()
		   		// this condition seems artificial, but it would make sense if type of cA (i.e. Company.address) was a superType of "a" (Address)
		   		// this would then effectively limit us to such companies where address is assignable to given type 
		   		.entity("a").eq().entity("cA")
		   	.done();		
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(MergeLookupJoin.T)				
				.whereOperand().isSourceSet_(Address.T)
				.whereProperty("otherOperand").hasType(EntityJoin.T)
					.whereProperty("valueProperty").isValueProperty_("address")
					.whereOperand().isSourceSet_(Company.T)
				.close()
			.close()
		;
		// @formatter:on
	}

	@Test
	public void mergeRangeJoin() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Company.T, "c")
				.where()
					.property("p", "companyName").ge().property("c", "name")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(MergeRangeJoin.T)
					.whereOperand().isSourceSet_(Person.T)
					.whereProperty("range")
						.hasType(RangeIntersection.T)
					.close()
					.whereProperty("index")
						.hasType(GeneratedMetricIndex.T)
						.whereOperand().isSourceSet_(Company.T)
						.whereProperty("indexKey").isValueProperty_("name")
		;
		// @formatter:on
	}

	//@Test // commented out as this fails due to unstable order
	public void mergeRangeJoin_Multi() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Company.T, "c")
				.where()
					.conjunction()
						.property("p", "companyName").ge().property("c", "name")
						.property("p", "companyName").ge().property("c", "description")
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(FilteredSet.T)
				.whereProperty("filter")
					.hasType(GreaterThanOrEqual.T) // Person.companyName >= Company.description
					.whereProperty("leftOperand")
						.isValueProperty_("companyName")
					.whereProperty("rightOperand")
						.isValueProperty_("description")
				.close()
				.whereOperand()
					.hasType(MergeRangeJoin.T) // Merge Persons with Companies, using Company.name index with condition Company.name <= Person.companyName 
						.whereOperand().isSourceSet_(Person.T)
						.whereProperty("range")
							.hasType(RangeIntersection.T)
							.whereProperty("ranges")
								.isSetWithSize(1)
								.whereFirstElement()
									.hasType(SimpleRange.T)
									.whereProperty("upperBound")
										.isValueProperty_("companyName") // Person.companyName
								.close()
							.close()
						.close()
						.whereProperty("index")
							.hasType(GeneratedMetricIndex.T)
							.whereOperand().isSourceSet_(Company.T)
							.whereProperty("indexKey").isValueProperty_("name") // Company.name
		;
		// @formatter:on
	}

	/**
	 * Similar to {@link #mergeLookupJoinWithJoinOperand()}, but with inequality.
	 */
	@Test
	public void mergeRangeJoinWithJoinOperand() {
		// @formatter:off
		SelectQuery selectQuery = query()
			.select("c")
		   	.from(Address.T, "a")
		   	.from(Company.T, "c")
		   		.join("c", "address", "cA") 
		   	.where()
				.property("a", "name").ge().property("cA", "name")
		   	.done();		
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(MergeRangeJoin.T)				
				.whereOperand().isSourceSet_(Address.T)
				.whereProperty("index").hasType(GeneratedMetricIndex.T)
					.whereOperand().hasType(EntityJoin.T)
						.whereProperty("valueProperty").hasType(ValueProperty.T)
							.whereProperty("propertyPath").is_("address")
						.close()
						.whereOperand().isSourceSet_(Company.T)
					.close()
				.close()
			.close()
		;
		// @formatter:on
	}

}
