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
package tribefire.platform.wire.space.common;

import com.braintribe.logging.ThreadRenamer;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

@Managed
public class RuntimeSpace implements WireSpace {

	@Managed
	public ThreadRenamer threadRenamer() {
		boolean enabled = Boolean.valueOf(TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_THREAD_RENAMING, "true"));
		ThreadRenamer bean = new ThreadRenamer(enabled);
		return bean;
	}

}
