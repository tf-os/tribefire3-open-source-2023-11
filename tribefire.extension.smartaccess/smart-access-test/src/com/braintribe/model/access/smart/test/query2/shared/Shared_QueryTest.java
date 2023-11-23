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
package com.braintribe.model.access.smart.test.query2.shared;

import static com.braintribe.model.processing.query.smart.test2.shared.model.shared.SharedSource.uuid;

import org.junit.Test;

import com.braintribe.model.access.smart.test.query2._base.AbstractSmartQueryTests;
import com.braintribe.model.processing.query.smart.test2._common.SmartModelTestSetup;
import com.braintribe.model.processing.query.smart.test2.shared.SharedSmartSetup;
import com.braintribe.model.processing.query.smart.test2.shared.model.shared.SharedSource;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 * @author peter.gazdik
 */
public class Shared_QueryTest extends AbstractSmartQueryTests {

	@Override
	protected SmartModelTestSetup getSmartModelTestSetup() {
		return SharedSmartSetup.SHARED_SETUP;
	}

	@Test
	public void singleSimpleEntity() throws Exception {
		SharedSource ss1 = bA.make(SharedSource.T).set(uuid, "s1").done();
		SharedSource ss2 = bB.make(SharedSource.T).set(uuid, "s2").done();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("s")
				.from(SharedSource.T, "s")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		SharedSource sss1 = acquireSmart(ss1);
		SharedSource sss2 = acquireSmart(ss2);
		
		Assertions.assertThat(sss1).isNotSameAs(sss2);
		
		assertResultContains(sss1);
		assertResultContains(sss2);
		assertNoMoreResults();
	}

}
