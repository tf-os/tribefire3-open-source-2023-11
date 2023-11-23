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
package com.braintribe.model.processing.query.eval.set;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.processing.query.eval.set.base.AbstractEvalTupleSetTests;
import com.braintribe.model.processing.query.eval.set.base.ModelBuilder;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.processing.smood.population.SmoodIndexTools;
import com.braintribe.model.queryplan.index.MetricIndex;
import com.braintribe.model.queryplan.index.RepositoryMetricIndex;

/**
 * 
 */
public class IndexRangeTests extends AbstractEvalTupleSetTests {

	private static final int PERSON_COUNT = 10;
	private final Person[] persons = new Person[PERSON_COUNT]; // personA to personH

	@Before
	public void buildData() {
		for (int i = 0; i < PERSON_COUNT; i++)
			registerAtSmood(persons[i] = ModelBuilder.person("person" + i));
	}

	@Test
	public void bothOpened() throws Exception {
		evaluate(builder.indexRange(Person.class, "indexedName", nameIndex(), "person2", false, "person7", false));

		for (int i = 3; i <= 6; i++)
			assertNextTuple(persons[i]);
		assertNoMoreTuples();
	}

	@Test
	public void leftClosed() throws Exception {
		evaluate(builder.indexRange(Person.class, "indexedName", nameIndex(), "person2", true, "person7", false));

		for (int i = 2; i <= 6; i++)
			assertNextTuple(persons[i]);
		assertNoMoreTuples();
	}

	@Test
	public void rightClosed() throws Exception {
		evaluate(builder.indexRange(Person.class, "indexedName", nameIndex(), "person2", false, "person7", true));

		for (int i = 3; i <= 7; i++)
			assertNextTuple(persons[i]);
		assertNoMoreTuples();
	}

	@Test
	public void bothClosed() throws Exception {
		evaluate(builder.indexRange(Person.class, "indexedName", nameIndex(), "person2", true, "person7", true));

		for (int i = 2; i <= 7; i++)
			assertNextTuple(persons[i]);
		assertNoMoreTuples();
	}

	@Test
	public void upperUnlimitedInclusive() throws Exception {
		evaluate(builder.indexRange(Person.class, "indexedName", nameIndex(), "person2", true, null, null));

		for (int i = 2; i < PERSON_COUNT; i++)
			assertNextTuple(persons[i]);
		assertNoMoreTuples();
	}

	@Test
	public void upperUnlimitedExclusive() throws Exception {
		evaluate(builder.indexRange(Person.class, "indexedName", nameIndex(), "person2", true, null, null));

		for (int i = 2; i < PERSON_COUNT; i++)
			assertNextTuple(persons[i]);
		assertNoMoreTuples();
	}

	@Test
	public void lowerUnlimitedInclusive() throws Exception {
		evaluate(builder.indexRange(Person.class, "indexedName", nameIndex(), null, true, "person7", true));

		for (int i = 0; i <= 7; i++)
			assertNextTuple(persons[i]);
		assertNoMoreTuples();
	}

	@Test
	public void lowerUnlimitedExclusive() throws Exception {
		evaluate(builder.indexRange(Person.class, "indexedName", nameIndex(), null, false, "person7", true));

		for (int i = 0; i <= 7; i++)
			assertNextTuple(persons[i]);
		assertNoMoreTuples();
	}

	@Test
	public void overallUnlimited() throws Exception {
		evaluate(builder.indexRange(Person.class, "indexedName", nameIndex(), null, null, null, null));

		for (int i = 0; i < PERSON_COUNT; i++)
			assertNextTuple(persons[i]);
		assertNoMoreTuples();
	}

	private MetricIndex nameIndex() {
		RepositoryMetricIndex index = RepositoryMetricIndex.T.create();
		index.setIndexId(SmoodIndexTools.indexId(Person.class.getName(), "indexedName"));

		return index;
	}

}
