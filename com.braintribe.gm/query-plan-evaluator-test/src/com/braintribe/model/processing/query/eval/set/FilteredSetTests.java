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

import static com.braintribe.model.processing.query.eval.set.base.TupleSetBuilder.and;
import static com.braintribe.model.processing.query.eval.set.base.TupleSetBuilder.hashSetProjection;
import static com.braintribe.model.processing.query.eval.set.base.TupleSetBuilder.not;
import static com.braintribe.model.processing.query.eval.set.base.TupleSetBuilder.or;
import static com.braintribe.model.processing.query.eval.set.base.TupleSetBuilder.staticValue;
import static com.braintribe.model.processing.query.eval.set.base.TupleSetBuilder.tupleComponent;
import static com.braintribe.model.processing.query.eval.set.base.TupleSetBuilder.valueComparison;
import static com.braintribe.model.processing.query.eval.set.base.TupleSetBuilder.valueProperty;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.processing.query.eval.api.function.aspect.LocaleQueryAspect;
import com.braintribe.model.processing.query.eval.set.base.AbstractEvalTupleSetTests;
import com.braintribe.model.processing.query.test.model.Color;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.queryplan.filter.Condition;
import com.braintribe.model.queryplan.filter.ConditionType;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.SourceSet;
import com.braintribe.model.queryplan.set.StaticSet;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.provider.Holder;
import com.braintribe.utils.lcd.CollectionTools2;

/**
 * Tests filtered set with the underlying source-set (for {@link Person} entities).
 */
public class FilteredSetTests extends AbstractEvalTupleSetTests {

	private Person pA, pB;
	private SourceSet personSet;
	private Value personComponent;
	private Value personNameValue;

	@Before
	public void buildData() {
		pA = b.person("personA").create();
		pB = b.person("personB").create();

		personSet = builder.sourceSet(Person.class);
		personComponent = tupleComponent(personSet);
		personNameValue = valueProperty(personSet, "name");
	}

	// ##################################
	// ## . . . VALUE COMPARISONS . . .##
	// ##################################

	@Test
	public void equality() throws Exception {
		evaluate(filteredSet(valueComparison(personNameValue, staticValue("personA"), ConditionType.equality)));

		assertNextTuple(pA);
		assertNoMoreTuples();
	}

	@Test
	public void equalityWithReference() throws Exception {
		pA.setIndexedFriend(pA);
		pB.setIndexedFriend(pB);

		evaluate(filteredSet(valueComparison(valueProperty(personSet, "indexedFriend"), staticValue(reference(pA)), ConditionType.equality)));

		assertNextTuple(pA);
		assertNoMoreTuples();
	}

	@Test
	public void equalityWithEnum() throws Exception {
		pA.setEyeColor(Color.GREEN);
		pB.setEyeColor(Color.BLUE);

		evaluate(filteredSet(valueComparison(valueProperty(personSet, "eyeColor"), staticValue(Color.GREEN), ConditionType.equality)));

		assertNextTuple(pA);
		assertNoMoreTuples();
	}

	@Test
	public void unequality() throws Exception {
		evaluate(filteredSet(valueComparison(personNameValue, staticValue("personA"), ConditionType.unequality)));

		assertNextTuple(pB);
		assertNoMoreTuples();
	}

	@Test
	public void unequalityWithEntity() throws Exception {
		pA.setIndexedFriend(pA);
		pB.setIndexedFriend(pB);

		evaluate(filteredSet(valueComparison(valueProperty(personSet, "indexedFriend"), staticValue(pB), ConditionType.unequality)));

		assertNextTuple(pA);
		assertNoMoreTuples();
	}

	@Test
	public void greater() throws Exception {
		evaluate(filteredSet(valueComparison(personNameValue, staticValue("personA"), ConditionType.greater)));

		assertNextTuple(pB);
		assertNoMoreTuples();
	}

	@Test
	public void greaterOrEqual() throws Exception {
		evaluate(filteredSet(valueComparison(personNameValue, staticValue("personA"), ConditionType.greaterOrEqual)));

		assertContainsTuple(pA);
		assertContainsTuple(pB);
		assertNoMoreTuples();
	}

	@Test
	public void less() throws Exception {
		evaluate(filteredSet(valueComparison(personNameValue, staticValue("personB"), ConditionType.less)));

		assertNextTuple(pA);
		assertNoMoreTuples();
	}

	@Test
	public void lessOrEqual() throws Exception {
		evaluate(filteredSet(valueComparison(personNameValue, staticValue("personB"), ConditionType.lessOrEqual)));

		assertContainsTuple(pA);
		assertContainsTuple(pB);
		assertNoMoreTuples();
	}

	// ##################################
	// ## . . . . . STRINGS . . . . . .##
	// ##################################

	@Test
	public void like() throws Exception {
		evaluate(filteredSet(valueComparison(personNameValue, staticValue("person*"), ConditionType.like)));

		assertContainsTuple(pA);
		assertContainsTuple(pB);
		assertNoMoreTuples();
	}

	@Test
	public void likeNotMatching() throws Exception {
		evaluate(filteredSet(valueComparison(personNameValue, staticValue("person"), ConditionType.like)));

		assertNoMoreTuples();
	}

	@Test
	public void likeSpecific() throws Exception {
		evaluate(filteredSet(valueComparison(personNameValue, staticValue("personA*"), ConditionType.like)));

		assertNextTuple(pA);
		assertNoMoreTuples();
	}

	@Test
	public void likeIsCaseSensitive() throws Exception {
		evaluate(filteredSet(valueComparison(personNameValue, staticValue("Company*"), ConditionType.like)));

		assertNoMoreTuples();
	}

	@Test
	public void ilike() throws Exception {
		evaluate(filteredSet(valueComparison(personNameValue, staticValue("Person*"), ConditionType.ilike)));

		assertContainsTuple(pA);
		assertContainsTuple(pB);
		assertNoMoreTuples();
	}

	@Test
	public void ilikeSpecific() throws Exception {
		evaluate(filteredSet(valueComparison(personNameValue, staticValue("PersonA*"), ConditionType.ilike)));

		assertNextTuple(pA);
		assertNoMoreTuples();
	}

	@Test
	public void fullText() throws Exception {
		evaluate(filteredSet(valueComparison(personComponent, staticValue("personA"), ConditionType.fullText)));

		assertNextTuple(pA);
		assertNoMoreTuples();
	}

	@Test
	public void fullText_CaseInsensitive() throws Exception {
		evaluate(filteredSet(valueComparison(personComponent, staticValue("PERSONa"), ConditionType.fullText)));

		assertNextTuple(pA);
		assertNoMoreTuples();
	}

	@Test
	public void fullText_IncludesLocalizedStringProperty() throws Exception {
		pA.setLocalizedString(localizedString("en", "localized", "de", "lokalisiert"));
		queryFunctionAspectProviders = CollectionTools2.asMap(LocaleQueryAspect.class, new Holder<>("en"));

		evaluate(filteredSet(valueComparison(personComponent, staticValue("local"), ConditionType.fullText)));

		assertNextTuple(pA);
		assertNoMoreTuples();
	}

	@Test
	public void fullIdProperty() throws Exception {
		pA.setId(999_999_999L); // use a number that does not occur in the generated globalId of "pB" (so the evaluator
								// only returns "pA")

		evaluate(filteredSet(valueComparison(personComponent, staticValue(pA.getId().toString()), ConditionType.fullText)));

		assertNextTuple(pA);
		assertNoMoreTuples();
	}

	// ##################################
	// ## . . COLLECTION MEMBERSHIP . .##
	// ##################################

	@Test
	public void in() throws Exception {
		evaluate(filteredSet(valueComparison(personNameValue, staticValue(asSet("personA", "personC")), ConditionType.in)));

		assertNextTuple(pA);
		assertNoMoreTuples();
	}

	@Test
	public void in_Entity() throws Exception {
		evaluate(filteredSet(valueComparison(personComponent, staticValue(asSet(reference(pA))), ConditionType.in)));

		assertNextTuple(pA);
		assertNoMoreTuples();
	}

	@Test
	public void inUsingHashSetProjection() throws Exception {
		StaticSet staticSet = builder.staticSet("personA", "personC");
		Value rightInOperand = hashSetProjection(staticSet, tupleComponent(staticSet));

		evaluate(filteredSet(valueComparison(personNameValue, rightInOperand, ConditionType.in)));

		assertNextTuple(pA);
		assertNoMoreTuples();
	}

	@Test
	public void contains() throws Exception {
		pA.setNicknames(asSet("guyA", "dawgA", "mrA"));
		pB.setNicknames(asSet("guyB", "dawgB", "mrB"));

		evaluate(filteredSet(valueComparison(valueProperty(personSet, "nicknames"), staticValue("dawgA"), ConditionType.contains)));

		assertNextTuple(pA);
		assertNoMoreTuples();
	}

	// ##################################
	// ## . . . . . REFLECTION . . . . ##
	// ##################################

	@Test
	public void instanceOf() throws Exception {
		evaluate(filteredSet(valueComparison(personNameValue, staticValue("string"), ConditionType.instanceOf)));

		assertContainsTuple(pA);
		assertContainsTuple(pB);
		assertNoMoreTuples();
	}

	@Test
	public void instanceOfWrongClass() throws Exception {
		evaluate(filteredSet(valueComparison(personNameValue, staticValue("integer"), ConditionType.instanceOf)));

		assertNoMoreTuples();
	}

	// ##################################
	// ## . . . . . LOGIC . . . . . . .##
	// ##################################

	@Test
	public void negation() throws Exception {
		evaluate(filteredSet(not(valueComparison(personNameValue, staticValue("personB"), ConditionType.equality))));

		assertNextTuple(pA);
		assertNoMoreTuples();
	}

	@Test
	public void conjunction() throws Exception {
		Condition condition = and(valueComparison(personNameValue, staticValue("personA"), ConditionType.equality),
				valueComparison(personNameValue, staticValue("personB"), ConditionType.equality));

		evaluate(filteredSet(condition));

		assertNoMoreTuples();
	}

	@Test
	public void disjunction() throws Exception {
		Condition condition = or(valueComparison(personNameValue, staticValue("personA"), ConditionType.equality),
				valueComparison(personNameValue, staticValue("personB"), ConditionType.equality));

		evaluate(filteredSet(condition));

		assertContainsTuple(pA);
		assertContainsTuple(pB);
		assertNoMoreTuples();
	}

	private FilteredSet filteredSet(Condition filter) {
		return builder.filteredSet(personSet, filter);
	}

}
