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

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.eval.api.repo.DelegatingRepository;
import com.braintribe.model.processing.query.eval.set.base.AbstractEvalTupleSetTests;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.queryplan.set.QuerySourceSet;

/**
 * 
 */
public class QuerySourceSetTests extends AbstractEvalTupleSetTests {

	private Person p1, p2;

	// ###############################################
	// ## . . . DelegatingRepo Initialization . . . ##
	// ###############################################

	@Override
	protected Smood newSmood() {
		return new EntityQuerySmood();
	}

	private class EntityQuerySmood extends Smood implements DelegatingRepository {
		public EntityQuerySmood() {
			super(EmptyReadWriteLock.INSTANCE);
		}

		@Override
		public Iterable<? extends GenericEntity> provideEntities(String ts, Condition condition, Ordering ordering) {
			return Person.class.getName().equals(ts) ? Arrays.asList(p1, p2) : Collections.<GenericEntity> emptySet();
		}

		@Override
		public boolean supportsFulltextSearch() {
			return false;
		}
	}

	// ###############################################
	// ## . . . . . . . Test methods . . . . . . . .##
	// ###############################################

	@Test
	public void emptySourceSet() throws Exception {
		QuerySourceSet set = builder.querySourceSet(Company.class);

		evaluate(set);

		assertNoMoreTuples();
	}

	@Test
	public void testEvaluateSourceSet() throws Exception {
		buildData();

		QuerySourceSet set = builder.querySourceSet(Person.class);
		evaluate(set);
		assertContainsTuple(p1);
		assertContainsTuple(p2);
	}

	private void buildData() {
		registerAtSmood(p1 = instantiate(Person.class));
		registerAtSmood(p2 = instantiate(Person.class));
	}
}
