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

import static com.braintribe.model.processing.query.eval.set.base.TupleSetBuilder.valueProperty;

import org.junit.Test;

import com.braintribe.model.processing.query.eval.set.base.AbstractEvalTupleSetTests;
import com.braintribe.model.processing.query.eval.set.base.ModelBuilder;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.set.SourceSet;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.value.Value;

/**
 * Tests filtered set with the underlying source-set (for {@link Person} entities).
 */
public class DistinctSetTests extends AbstractEvalTupleSetTests {

	private TupleSet distinctSet;

	@Test
	public void distinct() throws Exception {
		buildData();

		evaluate(distinctSet);

		assertContainsTuple("p1");
		assertContainsTuple("p2");
		assertNoMoreTuples();
	}

	private void buildData() {
		registerAtSmood(ModelBuilder.person("p1"));
		registerAtSmood(ModelBuilder.person("p1"));
		registerAtSmood(ModelBuilder.person("p2"));
		registerAtSmood(ModelBuilder.person("p2"));
		registerAtSmood(ModelBuilder.person("p2"));

		SourceSet personSet = builder.sourceSet(Person.class);
		Value personNameValue = valueProperty(personSet, "name");
		Projection personNameSet = builder.projection(personSet, personNameValue);
		distinctSet = builder.distinctSet(personNameSet);
	}

}
