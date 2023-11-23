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

import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.query.eval.set.base.AbstractEvalTupleSetTests;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.queryplan.set.StaticSet;

/**
 * 
 */
public class StaticSetTests extends AbstractEvalTupleSetTests {

	@Test
	public void emptyStaticSet() throws Exception {
		StaticSet set = builder.staticSet();
		evaluate(set);
		assertNoMoreTuples();
	}

	@Test
	public void nonEmptyStaticSet() throws Exception {
		StaticSet set = builder.staticSet("a", "b");

		evaluate(set);

		assertContainsTuple("a");
		assertContainsTuple("b");
		assertNoMoreTuples();
	}

	@Test
	public void staticSetOfEntities() throws Exception {
		Person p1 = b.person("P1").create();
		Person p2 = b.person("P2").create();

		StaticSet set = builder.staticSet(p1.reference(), p2.reference());

		evaluate(set);

		assertContainsTuple(p1);
		assertContainsTuple(p2);
		assertNoMoreTuples();
	}

	@Test
	public void staticSetOfUnknownEntities() throws Exception {
		PersistentEntityReference ref = b.person("P0").create().reference();
		ref.setRefId("unknown");

		StaticSet set = builder.staticSet(ref);

		evaluate(set);

		assertNoMoreTuples();
	}

}
