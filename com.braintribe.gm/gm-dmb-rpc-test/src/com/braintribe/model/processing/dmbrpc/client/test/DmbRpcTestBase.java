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
package com.braintribe.model.processing.dmbrpc.client.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

import com.braintribe.model.processing.rpc.test.RpcTestBase;
import com.braintribe.model.processing.rpc.test.wire.contract.DmbRpcTestContract;
import com.braintribe.model.processing.rpc.test.wire.contract.RpcTestContract;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.wire.api.context.WireContext;

/**
 * {@link RpcTestBase} for DMB RPC tests.
 * 
 */
@Category(KnownIssue.class)
public abstract class DmbRpcTestBase extends RpcTestBase {

	protected static WireContext<DmbRpcTestContract> context;

	// ============================= //
	// ======== LIFE CYCLE ========= //
	// ============================= //

	@BeforeClass
	public static void initialize() throws Exception {
		context = DmbRpcTestContract.context();
	}

	@AfterClass
	public static void destroy() throws Exception {
		if (context != null) {
			context.shutdown();
		}
	}

	// ============================= //
	// =========== BEANS =========== //
	// ============================= //

	@Override
	public RpcTestContract rpcTestBeans() {
		return context.contract();
	}

}
