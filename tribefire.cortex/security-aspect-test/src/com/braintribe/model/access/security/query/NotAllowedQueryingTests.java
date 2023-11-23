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

import org.junit.Rule;
import org.junit.Test;

import com.braintribe.exception.AuthorizationException;
import com.braintribe.model.access.security.testdata.query.EntityWithProps;
import com.braintribe.model.access.security.testdata.query.NonQueryableEntity;
import com.braintribe.model.access.security.testdata.query.NonVisibleEntity;
import com.braintribe.model.processing.aop.api.interceptor.InterceptionException;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.PropertyQueryBuilder;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.utils.junit.core.rules.ThrowableChain;
import com.braintribe.utils.junit.core.rules.ThrowableChainRule;

/**
 * Tests that in all these cases the query will not be allowed.
 * <p>
 * Note that I am taking advantage of {@link ThrowableChainRule}, so hopefully the tests will make sense once you read
 * what it's about.
 */
public class NotAllowedQueryingTests extends AbstractQueryingTest {

	@Rule
	public ThrowableChainRule exceptionChainRule = new ThrowableChainRule(InterceptionException.class);

	@Test
	public void nonQueryableEntityNotOk() {
		EntityQuery query = EntityQueryBuilder.from(NonQueryableEntity.class).done();

		aopAccess.queryEntities(query);
	}

	@Test
	public void nonQueryableEntityPropertyQueryNotOk() {
		PropertyQuery query = PropertyQueryBuilder.forProperty(NonQueryableEntity.T, 1L, "id").done();

		aopAccess.queryProperty(query);
	}

	@Test
	@ThrowableChain({ AuthorizationException.class })
	public void nonVisibleEntityNotOk() {
		EntityQuery query = EntityQueryBuilder.from(NonVisibleEntity.class).done();

		aopAccess.queryEntities(query);
	}

	@Test
	@ThrowableChain({})
	public void visibleModelOk() throws Throwable {
		EntityQuery query = EntityQueryBuilder.from(EntityWithProps.class).done();

		aopAccess.queryEntities(query);
	}

	@Test
	@ThrowableChain({ AuthorizationException.class })
	public void nonVisibleModelNotOk() {
		setUserRoles(MODEL_IGNORER);

		EntityQuery query = EntityQueryBuilder.from(EntityWithProps.class).done();

		aopAccess.queryEntities(query);
	}
}
