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
package com.braintribe.model.processing.rpc.commons.api;

public interface RpcConstants {

	final String RPC_META_SESSIONID = "sessionId";
	final String RPC_META_THREADNAME = "threadName";
	final String RPC_META_NDC = "ndc";
	final String RPC_META_NODEID = "nodeId";

	final String RPC_MAPKEY_REQUEST  = "rpc-request";
	final String RPC_MAPKEY_RESPONSE = "rpc-response";
	final String RPC_MAPKEY_REQUESTED_ENDPOINT = "rpc-requestedEndpoint";
	final String RPC_MAPKEY_REQUESTOR_ADDRESS = "rpc-requestorAddress";

	final String RPC_LOGSTEP_UNMARSHALL_REQUEST = "Unmarshalling of request";
	final String RPC_LOGSTEP_MARSHALL_REQUEST = "Marshalling of request";
	final String RPC_LOGSTEP_UNMARSHALL_RESPONSE = "Unmarshalling of response";
	final String RPC_LOGSTEP_MARSHALL_RESPONSE = "Marshalling of response";
	final String RPC_LOGSTEP_KEY_IMPORT = "Importing request key";
	final String RPC_LOGSTEP_INITIAL = "Request";
	final String RPC_LOGSTEP_FINAL = "Requested";

}
