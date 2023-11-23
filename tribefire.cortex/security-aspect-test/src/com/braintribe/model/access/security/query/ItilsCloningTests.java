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
package com.braintribe.model.access.security.query;

import static com.braintribe.model.access.security.query.PasswordPropertyTools.HIDDEN_PASSWORD;
import static com.braintribe.utils.lcd.CollectionTools2.first;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.braintribe.model.access.security.testdata.query.EntityWithProps;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.query.tools.PreparedTcs;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.record.ListRecord;
import com.braintribe.utils.junit.assertions.BtAssertions;
import com.braintribe.utils.junit.core.rules.ThrowableChainRule;

/**
 * 
 */
public class ItilsCloningTests extends AbstractQueryingTest {

	private EntityWithProps entity;

	@Rule
	public ThrowableChainRule exceptionChainRule = new ThrowableChainRule();

	@Override
	protected void prepareData() {
		entity = delegateSession.create(EntityWithProps.T);
		entity.setPassword("doesn't matter, this will not be retrieved");
	}

	@Test
	public void selectQuery_passwordSelectionAmongOthers_Tc() throws Exception {
		SelectQuery query = new SelectQueryBuilder() //
				.select("e", "id") //
				.select("e", "partition") //
				.select("e", "password") //
				.from(EntityWithProps.class, "e") //
				.tc(PreparedTcs.scalarOnlyTc) //
				.done();
		List<?> queryResult = query(query);

		BtAssertions.assertThat(queryResult).hasSize(1);

		ListRecord lr = first(queryResult);

		BtAssertions.assertThat(lr.getValues().get(0)).isNotNull();
		BtAssertions.assertThat(lr.getValues().get(1)).isNotNull();
		BtAssertions.assertThat(lr.getValues().get(2)).isEqualTo(HIDDEN_PASSWORD);
	}

}
