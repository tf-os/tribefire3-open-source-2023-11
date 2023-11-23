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

import static com.braintribe.model.processing.query.eval.set.base.TupleSetBuilder.staticValue;
import static com.braintribe.model.processing.query.eval.set.base.TupleSetBuilder.valueComparison;
import static com.braintribe.model.processing.query.eval.set.base.TupleSetBuilder.valueProperty;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.processing.query.eval.set.base.AbstractEvalTupleSetTests;
import com.braintribe.model.processing.query.eval.set.base.ModelBuilder;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.queryplan.filter.ConditionType;
import com.braintribe.model.queryplan.set.SourceSet;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.value.Value;

/**
 * Tests filtered set with the underlying source-set (for {@link Person} entities).
 */
public class SetCombinationTests extends AbstractEvalTupleSetTests {

	private Person pA, pB;
	private SourceSet personSet;
	private TupleSet filteredSet;
	private Value personNameValue;

	@Before
	public void buildData() {
		registerAtSmood(pA = ModelBuilder.person("personA"));
		registerAtSmood(pB = ModelBuilder.person("personB"));

		personSet = builder.sourceSet(Person.class);
		personNameValue = valueProperty(personSet, "name");
		filteredSet = builder.filteredSet(personSet, valueComparison(personNameValue, staticValue("personA"), ConditionType.equality));
	}

	@Test
	public void union() throws Exception {
		evaluate(builder.union(personSet, filteredSet));

		assertContainsTuple(pA);
		assertContainsTuple(pB);
		assertNoMoreTuples();
	}

	@Test
	public void unionCommuted() throws Exception {
		evaluate(builder.union(filteredSet, personSet));

		assertContainsTuple(pA);
		assertContainsTuple(pB);
		assertNoMoreTuples();
	}

	@Test
	public void intersection() throws Exception {
		evaluate(builder.intersection(personSet, filteredSet));

		assertContainsTuple(pA);
		assertNoMoreTuples();
	}

	@Test
	public void intersectionCommuted() throws Exception {
		evaluate(builder.intersection(filteredSet, personSet));

		assertContainsTuple(pA);
		assertNoMoreTuples();
	}

}
