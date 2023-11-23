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

import static com.braintribe.model.processing.query.eval.set.base.TupleSetBuilder.localize;
import static com.braintribe.model.processing.query.eval.set.base.TupleSetBuilder.tupleComponent;
import static com.braintribe.model.processing.query.eval.set.base.TupleSetBuilder.valueProperty;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.processing.query.eval.set.base.AbstractEvalTupleSetTests;
import com.braintribe.model.processing.query.eval.set.base.ModelBuilder;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.queryplan.set.SourceSet;
import com.braintribe.model.queryplan.value.Value;

/**
 * Tests filtered set with the underlying source-set (for {@link Person} entities).
 */
public class ProjectionTests extends AbstractEvalTupleSetTests {

	private Person pA, pB;
	private SourceSet personSet;
	private Value personComponent;
	private Value personNameValue;
	private Value personCompanyNameValue;
	private Value personAgeValue;

	@Before
	public void buildData() {
		registerAtSmood(pA = ModelBuilder.person("personA", "companyA", 10));
		registerAtSmood(pB = ModelBuilder.person("personB", "companyB", 20));

		personSet = builder.sourceSet(Person.class);
		personComponent = tupleComponent(personSet);
		personNameValue = valueProperty(personSet, "name");
		personCompanyNameValue = valueProperty(personSet, "companyName");
		personAgeValue = valueProperty(personSet, "birthDate");
	}

	@Test
	public void theInstanceItself() throws Exception {
		evaluate(builder.projection(personSet, personComponent));

		assertContainsTuple(pA);
		assertContainsTuple(pB);
		assertNoMoreTuples();
	}

	@Test
	public void justOneProperty() throws Exception {
		evaluate(builder.projection(personSet, personNameValue));

		assertContainsTuple("personA");
		assertContainsTuple("personB");
		assertNoMoreTuples();
	}

	@Test
	public void moreProperties() throws Exception {
		evaluate(builder.projection(personSet, personNameValue, personComponent, personCompanyNameValue, personAgeValue));

		assertContainsTuple("personA", pA, "companyA", pA.getBirthDate());
		assertContainsTuple("personB", pB, "companyB", pB.getBirthDate());
		assertNoMoreTuples();
	}

	@Test
	public void localizedProperty() throws Exception {
		pA.setLocalizedString(localizedString("en", "yes", "pt", "sim"));
		pB.setLocalizedString(localizedString("en", "good", "pt", "bom"));

		Object operand = new Object(); // for evaluator tests this does not really matter
		evaluate(builder.projection(personSet, localize(operand, "pt", valueProperty(personSet, "localizedString"))));

		assertContainsTuple("sim");
		assertContainsTuple("bom");
		assertNoMoreTuples();
	}

}
