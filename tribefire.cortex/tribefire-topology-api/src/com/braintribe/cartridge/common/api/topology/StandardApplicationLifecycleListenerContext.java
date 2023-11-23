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
package com.braintribe.cartridge.common.api.topology;

import java.util.function.Consumer;

/**
 * <p>
 * The standard {@link ApplicationLifecycleListenerContext} provided upon {@link ApplicationLifecycleListener} events.
 * 
 */
public class StandardApplicationLifecycleListenerContext implements ApplicationLifecycleListenerContext {

	private String applicationId;
	private String nodeId;
	private Consumer<String> unregisterCallback;

	public StandardApplicationLifecycleListenerContext(String applicationId, Consumer<String> unregisterCallback) {
		this(applicationId, null, unregisterCallback);
	}

	public StandardApplicationLifecycleListenerContext(String applicationId, String nodeId, Consumer<String> unregisterCallback) {
		super();
		this.applicationId = applicationId;
		this.nodeId = nodeId;
		this.unregisterCallback = unregisterCallback;
	}

	@Override
	public String applicationId() {
		return applicationId;
	}

	@Override
	public String nodeId() {
		return nodeId;
	}

	@Override
	public void unsubscribe() {
		unregisterCallback.accept(applicationId);
	}

}
