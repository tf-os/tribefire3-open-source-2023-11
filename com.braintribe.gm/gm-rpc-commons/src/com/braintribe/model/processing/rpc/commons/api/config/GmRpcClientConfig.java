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
package com.braintribe.model.processing.rpc.commons.api.config;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.model.processing.rpc.commons.api.authorization.RpcClientAuthorizationContext;
import com.braintribe.model.service.api.InstanceId;

public class GmRpcClientConfig {

	private Marshaller marshaller;
	private Supplier<Map<String, Object>> metaDataProvider;
	private RpcClientAuthorizationContext<Throwable> authorizationContext;
	protected Consumer<Set<String>> requiredTypesReceiver;
	private ExecutorService executorService;
	private InstanceId clientInstanceId;
	private String version = "1";

	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getVersion() {
		return version;
	}
	
	public Marshaller getMarshaller() {
		return marshaller;
	}

	public void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	public Supplier<Map<String, Object>> getMetaDataProvider() {
		return metaDataProvider;
	}

	public void setMetaDataProvider(Supplier<Map<String, Object>> metaDataProvider) {
		this.metaDataProvider = metaDataProvider;

	}

	/**
	 * @deprecated Cryptographic capabilities were removed from the RPC layer. This is now obsolete and will be removed
	 *             in a future version.
	 */
	@Deprecated
	public com.braintribe.model.processing.rpc.commons.api.crypto.RpcClientCryptoContext getCryptoContext() {
		return null;
	}

	/**
	 * @deprecated Cryptographic capabilities were removed from the RPC layer. This is now obsolete and will be removed
	 *             in a future version.
	 */
	@Deprecated
	public void setCryptoContext(@SuppressWarnings("unused") com.braintribe.model.processing.rpc.commons.api.crypto.RpcClientCryptoContext cryptoContext) {
		// no-op
	}

	public RpcClientAuthorizationContext<Throwable> getAuthorizationContext() {
		return authorizationContext;
	}

	public void setAuthorizationContext(RpcClientAuthorizationContext<Throwable> authorizationContext) {
		this.authorizationContext = authorizationContext;
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	public Consumer<Set<String>> getRequiredTypesReceiver() {
		return requiredTypesReceiver;
	}

	public void setRequiredTypesReceiver(Consumer<Set<String>> requiredTypesReceiver) {
		this.requiredTypesReceiver = requiredTypesReceiver;
	}

	public InstanceId getClientInstanceId() {
		return clientInstanceId;
	}

	public void setClientInstanceId(InstanceId clientInstanceId) {
		this.clientInstanceId = clientInstanceId;
	}
}
