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
package com.braintribe.model.processing.query.smart.eval.set;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.processing.query.eval.api.EvalTupleSet;
import com.braintribe.model.processing.query.smart.eval.set.base.AbstractDelegateQueryJoinTest;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.query.smart.processing.eval.set.EvalDelegateQueryJoin;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;

public class DelegateQueryJoinTest extends AbstractDelegateQueryJoinTest {

	private int bulkSize = 100;

	@Before
	public void buildData() {
		for (int i = 0; i < 10; i++) {
			aPersons.add(bA.personA("a" + i).parentB("b" + i).create());
			bPersons.add(bA.personA("b" + i).companyNameA("c" + i).create());
		}
	}

	@Test
	public void simpleDqj() {
		DelegateQueryJoin dqj = buildDqj();
		evaluate(dqj);
		assertContainsAllPersonData();
	}

	@Test
	public void bulkDqj_BatchSizeDividesTotlaResultCount() {
		bulkSize = 2;
		simpleDqj();
	}

	@Test
	public void bulkDqj_General() {
		bulkSize = 3;
		simpleDqj();
	}

	@Override
	protected EvalTupleSet resolveTupleset(TupleSet tupleSet) {
		EvalTupleSet result = super.resolveTupleset(tupleSet);
		if (result instanceof EvalDelegateQueryJoin) {
			((EvalDelegateQueryJoin) result).setBulkSize(bulkSize);
		}

		return result;
	}

	// ###################################
	// ## . . . . . Asserts . . . . . . ##
	// ###################################

	private void assertContainsAllPersonData() {
		for (int i = 0; i < 10; i++) {
			PersonA a = aPersons.get(i);
			PersonA b = bPersons.get(i);

			assertContainsTuple(a.getNameA(), b.getNameA(), b.getCompanyNameA());
		}

		assertNoMoreTuples();
	}
}
