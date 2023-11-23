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
package com.braintribe.model.query.parser.impl;

import org.junit.Test;

import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.processing.query.parser.QueryParser;
import com.braintribe.model.processing.query.parser.api.ParsedQuery;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.testing.model.test.technical.features.SimpleEnum;
import com.braintribe.utils.genericmodel.GMCoreTools;

public class EntityEnumTest extends AbstractQueryParserTest {

	@Test
	public void testPreliminaryEntityReference() throws Exception {
		PreliminaryEntityReference reference = PreliminaryEntityReference.T.create();
		reference.setTypeSignature(Person.class.getName());
		reference.setRefId(23);

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.entity(reference).ne().value(null)
				.done();
		// @formatter:on

		((ValueComparison) expectedQuery.getRestriction().getCondition()).setLeftOperand(reference);

		String queryString = "select * from " + Person.class.getName() + " p where reference(" + Person.class.getName() + ",23,false) != null";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testPersistentEntityReference() throws Exception {
		PersistentEntityReference reference = PersistentEntityReference.T.create();
		reference.setTypeSignature(Person.class.getName());
		reference.setRefId(23);
		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.entity(reference).ne().value(null)
				.done();
		// @formatter:on

		((ValueComparison) expectedQuery.getRestriction().getCondition()).setLeftOperand(reference);

		String queryString = "select * from " + Person.class.getName() + " p where reference(" + Person.class.getName() + ",23,true) != null";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testPersistentEntityReferenceWithPartition() throws Exception {
		PersistentEntityReference reference = PersistentEntityReference.T.create();
		reference.setTypeSignature(Person.class.getName());
		reference.setRefId(23);
		reference.setRefPartition("cortex");

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.T, "p")
				.where()
					.entity(reference).ne().value(null)
				.done();
		// @formatter:on

		((ValueComparison) expectedQuery.getRestriction().getCondition()).setLeftOperand(reference);

		String queryString = "select * from " + Person.class.getName() + " p where reference(" + Person.class.getName()
				+ ",23,'cortex',true) != null";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testEnumReference() throws Exception {
		EnumReference reference = EnumReference.T.create();
		reference.setTypeSignature(SimpleEnum.class.getName());
		reference.setConstant("TWO");

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.T, "p")
				.where()
					.entity(reference).ne().value(null)
				.done();
		// @formatter:on

		((ValueComparison) expectedQuery.getRestriction().getCondition()).setLeftOperand(reference);

		String queryString = "select * from " + Person.class.getName() + " p where enum(" + SimpleEnum.class.getName() + ", TWO) != null";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}
}
