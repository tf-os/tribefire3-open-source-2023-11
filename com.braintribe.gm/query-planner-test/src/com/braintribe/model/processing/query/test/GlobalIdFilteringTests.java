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

import static com.braintribe.model.generic.GenericEntity.globalId;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import org.junit.Test;

import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.index.RepositoryIndex;
import com.braintribe.model.queryplan.set.IndexSubSet;
import com.braintribe.model.queryplan.set.Projection;

/**
 * 
 */
public class GlobalIdFilteringTests extends AbstractQueryPlannerTests {

	@Test
	public void globalIdEquality() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.property("p", globalId).eq("p-1")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(IndexSubSet.T)
				.whereProperty("typeSignature").is_(Person.class.getName())
				.whereProperty("propertyName").is_(globalId)
				.whereProperty("keys")
					.isStaticSet_("p-1")
				.whereProperty("lookupIndex")
					.hasType(RepositoryIndex.T)
				.close()
		;
		// @formatter:on
	}

	@Test
	public void globalIdEquality_ReverseOperandOrder() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
				 	.value("p-1").eq().property("p", globalId)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(IndexSubSet.T)
				.whereProperty("typeSignature").is_(Person.T.getTypeSignature())
				.whereProperty("propertyName").is_(globalId)
				.whereProperty("keys")
					.isStaticSet_("p-1")
				.whereProperty("lookupIndex")
					.hasType(RepositoryIndex.T)
				.close()
		;
		// @formatter:on
	}

	@Test
	public void globalIdEquality_InSet() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.property("p", globalId).in(asSet("p-1", "p-2"))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(IndexSubSet.T)
				.whereProperty("typeSignature").is_(Person.T.getTypeSignature())
				.whereProperty("propertyName").is_(globalId)
				.whereProperty("keys")
					.isStaticSet_("p-1", "p-2")
				.whereProperty("lookupIndex")
					.hasType(RepositoryIndex.T)
				.close()
		;
		// @formatter:on
	}

	@Test
	public void globalIdEquality_Disjunction() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
				.disjunction()
					.property("p", "globalId").eq("p-1")
					.property("p", "globalId").eq("p-2")
				.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereOperand()
				.hasType(IndexSubSet.T)
				.whereProperty("typeSignature").is_(Person.T.getTypeSignature())
				.whereProperty("propertyName").is_(globalId)
				.whereProperty("keys")
					.isStaticSet_("p-1", "p-2")
				.whereProperty("lookupIndex")
					.hasType(RepositoryIndex.T)
				.close()
		;
		// @formatter:on
	}

}
