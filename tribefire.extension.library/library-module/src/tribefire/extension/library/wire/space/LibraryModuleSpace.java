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
package tribefire.extension.library.wire.space;

import com.braintribe.model.library.deployment.service.LibraryService;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefirePlatformContract;
import tribefire.module.wire.contract.WebPlatformBindersContract;

/**
 * This module's javadoc is yet to be written.
 */
@Managed
public class LibraryModuleSpace implements TribefireModuleContract {

	@Import
	private TribefirePlatformContract tfPlatform;

	@Import
	private WebPlatformBindersContract commonComponents;

	@Import
	private LibraryDeployablesSpace deployables;

	//
	// Deployables
	//

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		//@formatter:off
		bindings.bind(LibraryService.T)
			.component(commonComponents.accessRequestProcessor())
			.expertFactory(deployables::libraryProcessor);
		//@formatter:on
	}

}
