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
package com.braintribe.model.processing.securityservice.usersession.basic.test.wire.contract;

import com.braintribe.common.db.wire.contract.DbTestDataSourcesContract;
import com.braintribe.model.processing.securityservice.api.UserSessionService;
import com.braintribe.model.processing.securityservice.usersession.basic.test.common.TestConfig;
import com.braintribe.model.processing.securityservice.usersession.basic.test.wire.space.AccessBasedTestSpace;
import com.braintribe.model.processing.securityservice.usersession.basic.test.wire.space.DbBasedTestSpace;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.space.WireSpace;

public interface TestContract extends WireSpace {

	static WireContext<TestContract> context(boolean useRelationalDb) {
		return useRelationalDb ? dbBasedContext() : accessBasedContext();
	}

	static WireContext<TestContract> dbBasedContext() {
		return Wire.context(TestContract.class) //
				.bindContract(TestContract.class, DbBasedTestSpace.class) //
				.bindContracts(TestContract.class)
				// This doesn't work for whatever reason
				// .bindContract(DbTestDataSourcesContract.class, DbTestDataSourcesSpace.class) //
				.bindContracts(DbTestDataSourcesContract.class).build();
	}

	static WireContext<TestContract> accessBasedContext() {
		return Wire.context(TestContract.class) //
				.bindContract(TestContract.class, AccessBasedTestSpace.class) //
				// TODO, why TF is this needed?
				.bindContracts(TestContract.class).build();
	}

	TestConfig testConfig();

	UserSessionService userSessionService();
}
