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
package com.braintribe.model.access.smart.test.query2.basic;

import static com.braintribe.model.processing.query.smart.test2.basic.model.accessA.SimplePropertiesEntityA.date;
import static com.braintribe.model.processing.query.smart.test2.basic.model.accessA.SimplePropertiesEntityA.string;

import java.util.Date;

import org.junit.Test;

import com.braintribe.model.access.smart.test.query2._base.AbstractSmartQueryTests;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.smart.test2._common.SmartModelTestSetup;
import com.braintribe.model.processing.query.smart.test2.basic.BasicSmartSetup;
import com.braintribe.model.processing.query.smart.test2.basic.model.accessA.SimplePropertiesEntityA;
import com.braintribe.model.processing.query.smart.test2.basic.model.smart.SmartSimplePropertiesEntityA;
import com.braintribe.model.query.SelectQuery;

/**
 * @author peter.gazdik
 */
public class Basic_QueryTest extends AbstractSmartQueryTests {

	@Override
	protected SmartModelTestSetup getSmartModelTestSetup() {
		return BasicSmartSetup.BASIC_SETUP;
	}

	@Test
	public void noData() throws Exception {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("e", GenericEntity.id)
				.from(SmartSimplePropertiesEntityA.T, "e")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNoMoreResults();
	}

	@Test
	public void singleSimpleEntityProperties() throws Exception {
		bA.make(SimplePropertiesEntityA.T).set(string, "hello").set(date, new Date(0)).done();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("e", SmartSimplePropertiesEntityA.smartString)
				.select("e", SmartSimplePropertiesEntityA.smartDate)
				.from(SmartSimplePropertiesEntityA.T, "e")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("hello", new Date(0));
		assertNoMoreResults();
	}

	@Test
	public void singleSimpleEntity() throws Exception {
		SimplePropertiesEntityA e = bA.make(SimplePropertiesEntityA.T).set(string, "hello").set(date, new Date(0)).done();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("e")
				.from(SmartSimplePropertiesEntityA.T, "e")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		SmartSimplePropertiesEntityA se = acquireSmart(e, SmartSimplePropertiesEntityA.T);
		
		assertResultContains(se);
		assertNoMoreResults();
	}
}
