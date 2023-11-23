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
package tribefire.extension.cache.initializer.wire.space;

import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.cache.initializer.wire.contract.CacheInitializerContract;
import tribefire.extension.cache.initializer.wire.contract.CacheInitializerModelsContract;
import tribefire.extension.cache.initializer.wire.contract.ExistingInstancesContract;

@Managed
public class CacheInitializerSpace extends AbstractInitializerSpace implements CacheInitializerContract {

	@Import
	private CacheInitializerModelsContract models;

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private CoreInstancesContract coreInstances;

	@Override
	public void setupDefaultConfiguration() {
		// BasicModelMetaDataEditor serviceModelEditor = new BasicModelMetaDataEditor(existingInstances.serviceModel());

	}

	// -----------------------------------------------------------------------
	// PROCESSOR
	// -----------------------------------------------------------------------

	// 'CacheAspectAdminServiceProcessor' will be created by the user

	// 'CacheDemoProcessor' will be created by the user

	// -----------------------------------------------------------------------
	// ASPECT
	// -----------------------------------------------------------------------

	// 'CacheAspect' will be created by the user

	// -----------------------------------------------------------------------
	// META DATA - PROCESS WITH
	// -----------------------------------------------------------------------

	// will be created by the user

}
