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
package com.braintribe.model.processing.session.impl.remote.copied;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.logging.ndc.mbean.NestedDiagnosticContext;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.rpc.commons.api.RpcConstants;

/**
 * Copied from tribefire-platform-commons as we had to get rid of the dependency. As this artifact is the only place where this class is used, outside
 * of legitimate tribefire-platform-commons dependers, it is not worth extracting, as we want to get rid of this artifact (remote-gm-session-factory)
 * anyway.
 */
public class GmWebRpcClientMetaDataProvider_Copied implements Supplier<Map<String, Object>> {

	private final static Logger logger = Logger.getLogger(GmWebRpcClientMetaDataProvider_Copied.class);

	protected boolean includeNdc = true;
	protected boolean includeThreadName = true;
	protected boolean includeNodeId = true;

	private Supplier<String> sessionIdProvider;

	@Required
	@Configurable
	public void setSessionIdProvider(Supplier<String> sessionIdProvider) {
		this.sessionIdProvider = sessionIdProvider;
	}

	@Override
	public Map<String, Object> get() throws RuntimeException {
		String sessionId = sessionIdProvider.get();
		Map<String, Object> metaData = new HashMap<String, Object>();
		metaData.put(RpcConstants.RPC_META_SESSIONID, sessionId);

		boolean trace = logger.isTraceEnabled();

		String threadName = null;
		if (this.includeThreadName) {
			try {
				threadName = Thread.currentThread().getName();
				if (threadName == null) {
					threadName = "unknown";
				}
				metaData.put(RpcConstants.RPC_META_THREADNAME, threadName);
			} catch (Exception e) {
				if (trace)
					logger.trace("Could not add thread name to meta data.", e);
			}
		}
		if (this.includeNdc) {
			try {
				Deque<String> ndc = NestedDiagnosticContext.getNdc();
				if (ndc != null && !ndc.isEmpty()) {
					boolean addNdc = true;
					if (ndc.size() == 1) {
						String ndcEntry = ndc.getFirst();
						if (ndcEntry != null && threadName.equals(ndcEntry)) {
							// The NDC just contains the thread name. No sense in sending it twice.
							addNdc = false;
						}
					}
					if (addNdc) {
						metaData.put(RpcConstants.RPC_META_NDC, ndc.toString());
					}
				}
			} catch (Exception e) {
				if (trace)
					logger.trace("Could not add NDC to meta data.", e);
			}
		}
		if (this.includeNodeId) {
			try {
				String nodeId = TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_NODE_ID);
				if (nodeId != null) {
					metaData.put(RpcConstants.RPC_META_NODEID, nodeId);
				}
			} catch (Exception e) {
				if (trace)
					logger.trace("Could not add the node Id to meta data.", e);
			}
		}

		return metaData;
	}

	@Configurable
	public void setIncludeNdc(boolean includeNdc) {
		this.includeNdc = includeNdc;
	}
	@Configurable
	public void setIncludeThreadName(boolean includeThreadName) {
		this.includeThreadName = includeThreadName;
	}
	@Configurable
	public void setIncludeNodeId(boolean includeNodeId) {
		this.includeNodeId = includeNodeId;
	}

}
