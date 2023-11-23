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
package com.braintribe.model.processing.dmbrpc.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import com.braintribe.model.processing.rpc.commons.api.GmRpcException;

public interface GmDmbRpcInvoker {
	Map<String, Callable<InputStream>> call(
			Map<String, String> requestMetaData, 
			Map<String, Callable<InputStream>> inputs, 
			Map<String, Callable<OutputStream>> outputs,
			Consumer<Map<String, Callable<InputStream>>> resultsConsumer) throws GmRpcException;
}
