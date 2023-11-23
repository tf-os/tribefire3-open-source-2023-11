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

import org.junit.Test;

import com.braintribe.model.processing.query.eval.set.base.AbstractEvalTupleSetTests;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.queryplan.set.SourceSet;

/**
 * 
 */
public class SourceSetTests extends AbstractEvalTupleSetTests {

	private Person p1, p2;

	@Test
	public void emptySourceSet() throws Exception {
		SourceSet set = builder.sourceSet(Person.class);
		evaluate(set);
		assertNoMoreTuples();
	}

	@Test
	public void testEvaluateSourceSet() throws Exception {
		buildData();

		SourceSet set = builder.sourceSet(Person.class);

		evaluate(set);
		assertContainsTuple(p1);
		assertContainsTuple(p2);
	}

	private void buildData() {
		registerAtSmood(p1 = instantiate(Person.class));
		registerAtSmood(p2 = instantiate(Person.class));
	}
}
