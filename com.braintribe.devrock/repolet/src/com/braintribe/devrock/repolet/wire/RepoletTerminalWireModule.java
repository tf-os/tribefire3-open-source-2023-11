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
package com.braintribe.devrock.repolet.wire;

import com.braintribe.devrock.repolet.wire.contract.RepoletLauncherContract;
import com.braintribe.devrock.repolet.wire.space.RepoletLauncherSpace;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireTerminalModule;

public enum RepoletTerminalWireModule implements WireTerminalModule<RepoletLauncherContract> {
	INSTANCE;
	
	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {	
		WireTerminalModule.super.configureContext(contextBuilder);
		contextBuilder.bindContract(RepoletLauncherContract.class, RepoletLauncherSpace.class);
	}
	
}