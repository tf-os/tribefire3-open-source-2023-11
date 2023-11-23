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

import static com.braintribe.model.processing.query.eval.set.base.TupleSetBuilder.tupleComponent;
import static com.braintribe.model.processing.query.eval.set.base.TupleSetBuilder.valueProperty;
import static com.braintribe.model.queryplan.value.AggregationFunctionType.avg;
import static com.braintribe.model.queryplan.value.AggregationFunctionType.count;
import static com.braintribe.model.queryplan.value.AggregationFunctionType.countDistinct;
import static com.braintribe.model.queryplan.value.AggregationFunctionType.max;
import static com.braintribe.model.queryplan.value.AggregationFunctionType.min;
import static com.braintribe.model.queryplan.value.AggregationFunctionType.sum;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.processing.query.eval.set.base.AbstractEvalTupleSetTests;
import com.braintribe.model.processing.query.eval.set.base.ModelBuilder;
import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.queryplan.set.SourceSet;
import com.braintribe.model.queryplan.value.AggregateFunction;
import com.braintribe.model.queryplan.value.AggregationFunctionType;
import com.braintribe.model.queryplan.value.Value;

/**
 * Tests filtered set with the underlying source-set (for {@link Person} entities).
 */
public class AggregateProjectionTests extends AbstractEvalTupleSetTests {

	private SourceSet personSet;
	private SourceSet ownerSet;
	private Value personComponent;
	private Value personNameValue;
	private Value personCompanyNameValue;
	private Value personAgeValue;
	private Value personBirthDateValue;
	private Value ownerAgeValue;

	@Before
	public void buildData() {
		registerAtSmood(ModelBuilder.person("personA1", "companyA", 10));
		registerAtSmood(ModelBuilder.person("personA2", "companyA", 20));
		registerAtSmood(ModelBuilder.person("personB1", "companyB", 30));
		registerAtSmood(ModelBuilder.person("personB2", "companyB", 40));

		personSet = builder.sourceSet(Person.class);
		personComponent = tupleComponent(personSet);
		personNameValue = valueProperty(personSet, "name");
		personCompanyNameValue = valueProperty(personSet, "companyName");
		personAgeValue = valueProperty(personSet, "age");
		personBirthDateValue = valueProperty(personSet, "birthDate");

		ownerSet = builder.sourceSet(Owner.class);
		ownerAgeValue = valueProperty(ownerSet, "age");
	}

	// ###################################
	// ## . . . . . . Count . . . . . . ##
	// ###################################

	@Test
	public void countAll() throws Exception {
		evaluate(builder.aggregateProjection(personSet, aggregate(personComponent, count)));

		assertContainsTuple(4L);
		assertNoMoreTuples();
	}

	@Test
	public void countGoups() throws Exception {
		evaluate(builder.aggregateProjection(personSet, aggregate(personComponent, count), personCompanyNameValue));

		assertContainsTuple(2L, "companyA");
		assertContainsTuple(2L, "companyB");
		assertNoMoreTuples();
	}

	@Test
	public void countSkipsNulls() throws Exception {
		registerAtSmood(ModelBuilder.person(null, "companyA", 40));
		registerAtSmood(ModelBuilder.person(null, "companyB", 40));
		registerAtSmood(ModelBuilder.person(null, "companyB", 40));
		registerAtSmood(ModelBuilder.person(null, "companyC", 40));

		evaluate(builder.aggregateProjection(personSet, aggregate(personNameValue, count), personCompanyNameValue));

		assertContainsTuple(2L, "companyA");
		assertContainsTuple(2L, "companyB");
		assertContainsTuple(0L, "companyC");
		assertNoMoreTuples();
	}

	@Test
	public void countEmptySet() throws Exception {
		evaluate(builder.aggregateProjection(ownerSet, aggregate(tupleComponent(ownerSet), count)));

		assertContainsTuple(0L);
		assertNoMoreTuples();
	}

	@Test
	public void countDistinct() throws Exception {
		registerAtSmood(ModelBuilder.person("personA1", "companyA", 40)); // same as existing
		registerAtSmood(ModelBuilder.person(null, "companyA", 40)); // null -> to be ignored
		registerAtSmood(ModelBuilder.person("personB3", "companyB", 40)); // new
		registerAtSmood(ModelBuilder.person("personB3", "companyB", 40)); // same as previous

		evaluate(builder.aggregateProjection(personSet, aggregate(personNameValue, countDistinct), personCompanyNameValue));

		assertContainsTuple(2L, "companyA");
		assertContainsTuple(3L, "companyB");
		assertNoMoreTuples();
	}

	/** In this special case we expect an empty result (there is no need to test this for every aggregate function) */
	@Test
	public void aggregateEmptySetWhenNonAggregateWasAlsoSelected() throws Exception {
		SourceSet ownerSet = builder.sourceSet(Owner.class);
		evaluate(builder.aggregateProjection(ownerSet, aggregate(tupleComponent(ownerSet), count), valueProperty(ownerSet, "name")));

		assertNoMoreTuples();
	}

	// ###################################
	// ## . . . . . . Sum . . . . . . . ##
	// ###################################

	@Test
	public void sumAll() throws Exception {
		evaluate(builder.aggregateProjection(personSet, aggregate(personAgeValue, sum)));

		assertContainsTuple(100);
		assertNoMoreTuples();
	}

	@Test
	public void sumGoups() throws Exception {
		evaluate(builder.aggregateProjection(personSet, aggregate(personAgeValue, sum), personCompanyNameValue));

		assertContainsTuple(30, "companyA");
		assertContainsTuple(70, "companyB");
		assertNoMoreTuples();
	}

	@Test
	public void sumEmptySet() throws Exception {
		evaluate(builder.aggregateProjection(ownerSet, aggregate(ownerAgeValue, sum)));

		assertContainsTuple((Object) null);
		assertNoMoreTuples();
	}

	// ###################################
	// ## . . . . . Average . . . . . . ##
	// ###################################

	@Test
	public void averageAll() throws Exception {
		evaluate(builder.aggregateProjection(personSet, aggregate(personAgeValue, avg)));

		assertContainsTuple(25);
		assertNoMoreTuples();
	}

	@Test
	public void averageGoups() throws Exception {
		evaluate(builder.aggregateProjection(personSet, aggregate(personAgeValue, avg), personCompanyNameValue));

		assertContainsTuple(15, "companyA");
		assertContainsTuple(35, "companyB");
		assertNoMoreTuples();
	}

	@Test
	public void averageGoupsNoValue() throws Exception {
		registerAtSmood(ModelBuilder.person("personC1", "companyC", 10, null));
		registerAtSmood(ModelBuilder.person("personC2", "companyC", 10, null));

		evaluate(builder.aggregateProjection(personSet, aggregate(personBirthDateValue, avg), personCompanyNameValue));

		assertContainsTuple(null, "companyC");
	}

	@Test
	public void averageEmptySet() throws Exception {
		evaluate(builder.aggregateProjection(ownerSet, aggregate(ownerAgeValue, avg)));

		assertContainsTuple((Object) null);
		assertNoMoreTuples();
	}

	// ###################################
	// ## . . . . . . Min . . . . . . . ##
	// ###################################

	@Test
	public void minAll() throws Exception {
		evaluate(builder.aggregateProjection(personSet, aggregate(personAgeValue, min)));

		assertContainsTuple(10);
		assertNoMoreTuples();
	}

	@Test
	public void minGoups() throws Exception {
		evaluate(builder.aggregateProjection(personSet, aggregate(personAgeValue, min), personCompanyNameValue));

		assertContainsTuple(10, "companyA");
		assertContainsTuple(30, "companyB");
		assertNoMoreTuples();
	}

	@Test
	public void minEmptySet() throws Exception {
		evaluate(builder.aggregateProjection(ownerSet, aggregate(ownerAgeValue, min)));

		assertContainsTuple((Object) null);
		assertNoMoreTuples();
	}

	// ###################################
	// ## . . . . . . Max . . . . . . . ##
	// ###################################

	@Test
	public void maxAll() throws Exception {
		evaluate(builder.aggregateProjection(personSet, aggregate(personAgeValue, max)));

		assertContainsTuple(40);
		assertNoMoreTuples();
	}

	@Test
	public void maxGoups() throws Exception {
		evaluate(builder.aggregateProjection(personSet, aggregate(personAgeValue, max), personCompanyNameValue));

		assertContainsTuple(20, "companyA");
		assertContainsTuple(40, "companyB");
		assertNoMoreTuples();
	}

	@Test
	public void maxEmptySet() throws Exception {
		evaluate(builder.aggregateProjection(ownerSet, aggregate(ownerAgeValue, max)));

		assertContainsTuple((Object) null);
		assertNoMoreTuples();
	}

	// ##############################################################################################################################

	private AggregateFunction aggregate(Value operand, AggregationFunctionType type) {
		AggregateFunction result = AggregateFunction.T.create();

		result.setAggregationFunctionType(type);
		result.setOperand(operand);

		return result;
	}

}
