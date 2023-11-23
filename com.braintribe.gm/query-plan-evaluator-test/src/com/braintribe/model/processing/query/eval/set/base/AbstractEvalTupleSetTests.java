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
package com.braintribe.model.processing.query.eval.set.base;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.Before;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.api.function.QueryFunctionAspect;
import com.braintribe.model.processing.query.eval.context.BasicQueryEvaluationContext;
import com.braintribe.model.processing.query.eval.tools.QueryEvaluationTools;
import com.braintribe.model.processing.query.eval.tuple.ArrayBasedTuple;
import com.braintribe.model.processing.query.support.QueryFunctionTools;
import com.braintribe.model.processing.query.test.builder.DataBuilder;
import com.braintribe.model.processing.query.test.model.MetaModelProvider;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.queryplan.QueryPlan;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.utils.junit.assertions.BtAssertions;
import com.braintribe.utils.lcd.CollectionTools2;

/**
 * 
 */
public abstract class AbstractEvalTupleSetTests {

	protected Map<Class<? extends QueryFunctionAspect<?>>, Supplier<?>> queryFunctionAspectProviders;
	protected DataBuilder b;
	protected TupleSetBuilder builder;

	private Smood smood;
	private QueryEvaluationContext context;
	private Set<Tuple> tuples;
	private Iterator<Tuple> tuplesIterator;

	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	@Before
	public void setup() {
		smood = newSmood();
		smood.setMetaModel(MetaModelProvider.provideEnrichedModel());

		builder = new TupleSetBuilder();
		b = new DataBuilder(smood);
	}

	protected Smood newSmood() {
		return new Smood(EmptyReadWriteLock.INSTANCE);
	}

	protected void registerAtSmood(GenericEntity entity) {
		smood.registerEntity(entity, true);
	}

	protected static LocalizedString localizedString(Object... os) {
		LocalizedString result = LocalizedString.T.create();
		result.setLocalizedValues(CollectionTools2.<String, String> asMap(os));

		return result;
	}

	protected void evaluate(TupleSet tupleSet) {
		context = new BasicQueryEvaluationContext(smood, toQueryPlan(tupleSet), QueryFunctionTools.functionExperts(null), queryFunctionAspectProviders);
		tuplesIterator = context.resolveTupleSet(tupleSet).iterator();
	}

	private QueryPlan toQueryPlan(TupleSet tupleSet) {
		QueryPlan result = QueryPlan.T.create();
		result.setTupleSet(tupleSet);

		return result;
	}

	protected void assertNoMoreTuples() {
		if (tuples == null) {
			BtAssertions.assertThat(tuplesIterator.hasNext()).as("No more tuples in the result set expected!").isFalse();

		} else {
			BtAssertions.assertThat(tuples).as("No more tuples in the result set expected!").isEmpty();
		}
	}

	protected void assertContainsTuple(Object... values) {
		BtAssertions.assertThat(values).as("Wrong number of values provided. Resulting tuples have different dimension!")
				.hasSize(context.resultComponentsCount());

		collectTuples();

		Tuple tuple = asTuple(values);

		if (!tuples.contains(tuple)) {
			throw new RuntimeException("Tuple not found in the result: " + Arrays.toString(values));
		}

		tuples.remove(tuple);
	}

	private void collectTuples() {
		if (tuples == null) {
			tuples = QueryEvaluationTools.tupleHashSet(context.resultComponentsCount());

			while (tuplesIterator.hasNext()) {
				Tuple tuple = tuplesIterator.next();
				tuples.add(tuple.detachedCopy());
			}
		}
	}

	protected void assertNextTuple(Object... values) {
		checkContainsWasNotCalledBefore();
		assertHasNextTuple();

		Tuple tuple = tuplesIterator.next();

		for (int i = 0; i < values.length; i++) {
			Object expected = values[i];
			Object evaluated = tuple.getValue(i);

			if (expected == null) {
				BtAssertions.assertThat(evaluated).isNull();

			} else if (expected instanceof GenericEntity) {
				BtAssertions.assertThat(evaluated).as("Evaluated is not same as expected!").isSameAs(expected);

			} else {
				BtAssertions.assertThat(evaluated).isEqualTo(expected);
			}
		}
	}

	private void checkContainsWasNotCalledBefore() {
		if (tuples != null)
			throw new RuntimeException("Illegal method called. Seems like 'assertContainsTuple' was already called!"
					+ " There should never be a test that uses both that and 'assertNextTuple'");
	}

	private void assertHasNextTuple() {
		BtAssertions.assertThat(tuplesIterator.hasNext()).as("No more tuples in the result set!").isTrue();
	}

	protected Tuple asTuple(Object... values) {
		return new ArrayBasedTuple(values);
	}

	protected EntityReference reference(GenericEntity ge) {
		return ge.reference();
	}

	protected static <T extends GenericEntity> T instantiate(Class<T> clazz) {
		return typeReflection.<T> getEntityType(clazz).create();
	}

}
