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
package com.braintribe.model.processing.rpc.test.wire.contract;

import java.util.List;

import javax.servlet.Filter;

import com.braintribe.model.processing.webrpc.server.GmWebRpcServer;
import com.braintribe.wire.api.context.WireContext;

public interface WebRpcTestContract extends RpcTestContract {

	static WireContext<WebRpcTestContract> context() {
		// @formatter:off
		WireContext<WebRpcTestContract> wireContext = 
				com.braintribe.wire.api.Wire
					.context(WebRpcTestContract.class)
						.bindContracts(WebRpcTestContract.class.getName().replace(".contract."+WebRpcTestContract.class.getSimpleName(), ""))
					.build();
		return wireContext;
		// @formatter:on
	}
	
	GmWebRpcServer server();

	List<Filter> filters();

}
