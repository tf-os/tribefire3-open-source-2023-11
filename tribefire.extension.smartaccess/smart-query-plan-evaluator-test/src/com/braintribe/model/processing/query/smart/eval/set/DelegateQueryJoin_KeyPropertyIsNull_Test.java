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

import org.junit.Test;

import com.braintribe.model.processing.query.smart.eval.set.base.AbstractDelegateQueryJoinTest;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;

public class DelegateQueryJoin_KeyPropertyIsNull_Test extends AbstractDelegateQueryJoinTest {

	/**
	 * We had a bug where we did not check for such a case before and we would make a query with an empty condition. Such query would return
	 * everything, and the correlation based on the <tt>null</tt> value would actually lead to a non-empty result of evaluation.
	 */
	@Test
	public void simpleDqjWrongData() {
		// These should not correlate
		aPersons.add(bA.personA("a").parentB(null).create());
		bPersons.add(bA.personA(null).companyNameA("c").create());

		DelegateQueryJoin dqj = buildDqj();
		evaluate(dqj);

		assertNoMoreTuples();
	}

}
