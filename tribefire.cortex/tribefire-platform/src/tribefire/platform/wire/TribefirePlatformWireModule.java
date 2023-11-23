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
package tribefire.platform.wire;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.braintribe.cfg.ScopeContext;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.wire.api.context.ScopeContextHolders;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.module.loading.PropertyContractResolver;
import tribefire.platform.wire.contract.MainTribefireContract;
import tribefire.platform.wire.space.MainTribefireSpace;

/**
 * @author peter.gazdik
 */
public enum TribefirePlatformWireModule implements WireTerminalModule<MainTribefireContract>  {
	INSTANCE;

	private final Map<ScopeContext, ScopeContextHolders> deploymentScopeContextHolders = new ConcurrentHashMap<>();

	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		contextBuilder.bindContracts(new PropertyContractResolver());
		contextBuilder.bindContracts(MainTribefireSpace.class);
		contextBuilder.shareScopeContexts(getShareScopeContextsExpert());
	}

	public Function<ScopeContext, Map<ScopeContext, ScopeContextHolders>> getShareScopeContextsExpert() {
		return this::getScopeContextHoldersMap;
	}

	private Map<ScopeContext, ScopeContextHolders> getScopeContextHoldersMap(ScopeContext scopeContext) {
		if (scopeContext instanceof ExpertContext<?>) {
			return deploymentScopeContextHolders;
		} else {
			return null;
		}
	}
}
