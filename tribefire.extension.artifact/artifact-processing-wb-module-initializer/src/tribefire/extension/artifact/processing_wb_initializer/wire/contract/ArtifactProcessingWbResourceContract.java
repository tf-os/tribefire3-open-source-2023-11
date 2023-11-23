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
package tribefire.extension.artifact.processing_wb_initializer.wire.contract;


import com.braintribe.model.resource.Resource;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.initializer.support.impl.lookup.GlobalId;
import tribefire.cortex.initializer.support.impl.lookup.InstanceLookup;

@InstanceLookup(lookupOnly = true, globalIdPrefix = ArtifactProcessingWbResourceContract.GLOBAL_ID_PREFIX)
public interface ArtifactProcessingWbResourceContract extends WireSpace {

	String RESOURCE_ASSET_NAME = "tribefire.extension.artifact:artifact-processing-access-wb-resources";
	String GLOBAL_ID_PREFIX = "asset-resource://" + RESOURCE_ASSET_NAME + "/";
	
	@GlobalId("config_24x24.png")
	Resource config24Png();
	
	@GlobalId("config_32x32.png")
	Resource config32Png();
	
	@GlobalId("config_6x64.png")
	Resource config64Png();
	
}
