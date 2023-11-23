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

import org.junit.Test;

import com.braintribe.model.processing.query.test.model.Address;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.functions.value.Concatenation;
import com.braintribe.model.queryplan.filter.Equality;
import com.braintribe.model.queryplan.index.RepositoryIndex;
import com.braintribe.model.queryplan.set.CartesianProduct;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.MergeLookupJoin;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.set.SourceSet;
import com.braintribe.model.queryplan.set.join.EntityJoin;
import com.braintribe.model.queryplan.set.join.IndexLookupJoin;
import com.braintribe.model.queryplan.value.QueryFunctionValue;
import com.braintribe.model.queryplan.value.ValueProperty;

/**
 * 
 */
public class CustomFunctionsTests extends AbstractQueryPlannerTests {

	@Test
	public void customFunctionSimpleSelection() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.select().concatenate()
					.property("p", "name")
					.value(":")
					.property("p", "companyName")
				.close()
				.from(Person.T, "p")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
				.whereOperand().isSourceSet_(Person.T)
				.whereProperty("values")
					.isListWithSize(1)
					.whereFirstElement()
						.hasType(QueryFunctionValue.T)
						.whereProperty("queryFunction").hasType(Concatenation.T).close()
						.whereProperty("operandMappings").isMapWithSize(2).close()
					.close()
		;
		// @formatter:on
	}

	@Test
	public void customFunctionSimpleCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.concatenate()
						.property("p", "name")
						.value(":")
						.property("p", "companyName")
					.close().eq("John Smith:Microsoft")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(FilteredSet.T)
					.whereOperand().isSourceSet_(Person.T)
					.whereProperty("filter")
						.hasType(Equality.T)
						.whereProperty("leftOperand")
							.hasType(QueryFunctionValue.T)
							.whereProperty("queryFunction").hasType(Concatenation.T).close()
							.whereProperty("operandMappings").isMapWithSize(2).close()
						.close()
						.whereProperty("rightOperand").isStaticValue_("John Smith:Microsoft")
		;
		// @formatter:on
	}

	@Test
	public void customFunctionReferencingThreeSources() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Company.T, "c")
				.from(Address.T, "a")
				.where()
					.concatenate()
						.property("p", "name")
						.property("c", "name")
						.property("a", "name")
					.close().eq("John Smith:Microsoft:LA, CA")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(FilteredSet.T)
					.whereOperand()
						.hasType(CartesianProduct.T)
						.whereProperty("operands")
							.isListWithSize(3)
							.whereElementAt(0).hasType(SourceSet.T).close()
							.whereElementAt(1).hasType(SourceSet.T).close()
							.whereElementAt(2).hasType(SourceSet.T).close()
						.close()
					.close()
					.whereProperty("filter")
							.hasType(Equality.T)
							.whereProperty("leftOperand")
								.hasType(QueryFunctionValue.T)
								.whereProperty("queryFunction").hasType(Concatenation.T).close()
							.close()
							.whereProperty("rightOperand").isStaticValue_("John Smith:Microsoft:LA, CA")
		;
		// @formatter:on
	}

	@Test
	public void indexJoinSupportedForFunctionOperand() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Company.T, "c")
				.where()
					.concatenate()
						.property("p", "companyName")
						.value(" LLC")
					.close().eq().property("c", "indexedName")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(IndexLookupJoin.T)
				.whereOperand().isSourceSet_(Person.T)
				.whereProperty("lookupValue")
					.hasType(QueryFunctionValue.T)
					.whereProperty("queryFunction").hasType(Concatenation.T).close()
				.close()
				.whereProperty("lookupIndex").hasType(RepositoryIndex.T)
		;
		// @formatter:on
	}

	/**
	 * The query tested here is: <tt>from Person p, Address a, Company c where CONCAT(p.companyName, ":", a.country) = c.indexedName</tt>
	 * 
	 * This desired plan is to first create a {@link CartesianProduct} of <tt>p</tt> and <tt>a</tt>, and then an {@link IndexLookupJoin} with
	 * <tt>c</tt>, using <tt>c.indexedName</tt> being indexed.
	 */
	@Test
	public void indexJoinSupportedForMultiSourceFunctionOperand() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Address.T, "a")
				.from(Company.T, "c")
				.where()
					.concatenate()
						.property("p", "companyName")
						.property("a", "country")
					.close().eq().property("c", "indexedName")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(IndexLookupJoin.T)
				.whereOperand().hasType(CartesianProduct.T).close()
				.whereProperty("lookupValue")
					.hasType(QueryFunctionValue.T)
					.whereProperty("queryFunction").hasType(Concatenation.T).close()
				.close()
				.whereProperty("lookupIndex").hasType(RepositoryIndex.T)
		;
		// @formatter:on
	}

	/**
	 * Similar to {@link #indexJoinSupportedForMultiSourceFunctionOperand()}, but the multi-source function is applied on explicitly joined sources.
	 */
	@Test
	public void indexJoinSupportedForMultiSourceFunctionOperand_PropertyJoin() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
					.join("p", "company", "c1")
				.from(Company.T, "c2")
				.where()
					.concatenate()
						.property("p", "name")
						.property("c1", "name")
					.close().eq().property("c2", "indexedName")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(IndexLookupJoin.T)
				.whereOperand().hasType(EntityJoin.T).close()
				.whereProperty("lookupValue")
					.hasType(QueryFunctionValue.T)
					.whereProperty("queryFunction").hasType(Concatenation.T).close()
				.close()
				.whereProperty("lookupIndex").hasType(RepositoryIndex.T)
		;
		// @formatter:on
	}

	@Test
	public void mergeJoinSupportedForSimpleFunctionOperand() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Company.T, "c")
				.where()
					.concatenate()
						.property("p", "companyName")
						.value(" LLC")
					.close().eq().property("c", "name")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(MergeLookupJoin.T)
				.whereOperand().hasType(SourceSet.T).close()
				.whereValue()
					.hasType(QueryFunctionValue.T)
					.whereProperty("queryFunction").hasType(Concatenation.T).close()
				.close()
				.whereProperty("otherOperand").hasType(SourceSet.T).close()
				.whereProperty("otherValue")
					.hasType(ValueProperty.T).isValueProperty_("name")
		;
		// @formatter:on
	}

	@Test
	public void mergeJoinSupportedForMultiSourceFunctionOperand() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Address.T, "a")
				.from(Company.T, "c")
				.where()
					.concatenate()
						.property("p", "companyName")
						.property("a", "country")
					.close().eq().property("c", "name")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(MergeLookupJoin.T)
				.whereOperand().hasType(CartesianProduct.T).close()
				.whereValue()
					.hasType(QueryFunctionValue.T)
					.whereProperty("queryFunction").hasType(Concatenation.T).close()
				.close()
				.whereProperty("otherOperand").hasType(SourceSet.T).close()
				.whereProperty("otherValue")
					.hasType(ValueProperty.T).isValueProperty_("name")
		;
		// @formatter:on
	}

}
