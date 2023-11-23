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
package com.braintribe.gwt.gmrpc.api.client.transport;

/**
 * Must be kept in sync with com.braintribe.model.processing.webrpc.api.RpcHeaders
 */
public enum RpcHeaders {
	
	/*
	 * indicates whether the request or response body contains an unmarshallable RPC body.
	 */
	rpcBody("gm-rpc-body"),
	
	/*
	 * a flag that controls if the call occurs reasoned or not (true|false)
	 */
	rpcReasoning("gm-rpc-reasoning"),

	/*
	 * indicates the type of the marshalled generic model entity 
	 * contained in the body of a RPC request or response.
	 */
	rpcBodyType("gm-rpc-body-type");

	
	private String headerName;
	
	private RpcHeaders(String headerName) {
		this.headerName = headerName;
	}
	
	public String getHeaderName() {
		return headerName;
	}

}
