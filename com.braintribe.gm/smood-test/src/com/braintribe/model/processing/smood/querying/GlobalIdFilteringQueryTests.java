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
package com.braintribe.model.processing.smood.querying;

import static com.braintribe.model.generic.GenericEntity.globalId;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.eval.api.repo.IndexInfo;
import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.SelectQuery;

public class GlobalIdFilteringQueryTests extends AbstractSelectQueryTests {

	/** @see #globalIdEquality_ConcreteType() */
	@Test
	public void globalIdEquality_GenericEntity() {
		Person p;
		p = b.person("Jack").globalId("p-0").create();
		p = b.person("John").globalId("p-1").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(GenericEntity.T, "p")
				.where()
					.property("p", globalId).eq("p-1")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(p);
		assertNoMoreResults();
	}

	/**
	 * This is different from {@link #globalIdEquality_GenericEntity()} because here we are querying for a concrete
	 * type. This requires special handling from Smood, because internally it only implements one index for all
	 * entities, i.e. it is done on the {@link GenericEntity} level. This is relevant when resolving the
	 * {@link IndexInfo}.
	 */
	@Test
	public void globalIdEquality_ConcreteType() {
		Person p;
		p = b.person("Jack").globalId("p-0").create();
		p = b.person("John").globalId("p-1").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.property("p", globalId).eq("p-1")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(p);
		assertNoMoreResults();
	}

	@Test
	public void globalIdEquality_WrongType_NoResult() {
		b.person("Jack").globalId("p-0").create();
		b.person("John").globalId("p-1").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Owner.T, "p")
				.where()
					.property("p", globalId).eq("p-1")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNoMoreResults();
	}
}
