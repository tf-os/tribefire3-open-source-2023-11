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
 * Tests for {@link EvalIndexOrderedSet}.
 */
public class IndexOrderedSetTests extends AbstractEvalTupleSetTests {

	private static final int PERSON_COUNT = 4;
	private final Person[] persons = new Person[PERSON_COUNT];

	private static final boolean ASCENDING = false;
	private static final boolean DESCENDING = !ASCENDING;

	@Before
	public void buildData() {
		for (int i = 0; i < PERSON_COUNT; i++)
			registerAtSmood(persons[i] = ModelBuilder.person("person" + String.valueOf((char) (i + 65))));
	}

	@Test
	public void ascendingOrder() throws Exception {
		runTest(ASCENDING);
	}

	@Test
	public void descendingOrder() throws Exception {
		runTest(DESCENDING);
	}

	private void runTest(boolean reverseOrder) throws Exception {
		evaluate(builder.indexOrderedSet(Person.class, "indexedName", nameIndex(), reverseOrder));

		assertCorrectOrder(reverseOrder);
	}

	/* package */ static MetricIndex nameIndex() {
		RepositoryMetricIndex index = RepositoryMetricIndex.T.create();
		index.setIndexId(SmoodIndexTools.indexId(Person.class.getName(), "indexedName"));

		return index;
	}

	private void assertCorrectOrder(boolean reverseOrder) {
		for (int i = 0; i < PERSON_COUNT; i++) {
			int j = reverseOrder ? PERSON_COUNT - 1 - i : i;
			assertNextTuple(persons[j]);
		}
	}

}
