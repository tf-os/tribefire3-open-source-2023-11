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
package tribefire.extension.modelling.common.wire.contract;

import com.braintribe.model.resource.Resource;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.initializer.support.impl.lookup.GlobalId;
import tribefire.cortex.initializer.support.impl.lookup.InstanceLookup;

@InstanceLookup(lookupOnly = true, globalIdPrefix = CommonResourceContract.GLOBAL_ID_PREFIX_RESOURCES)
public interface CommonResourceContract extends WireSpace {

	String RESOURCE_ASSET_NAME = "tribefire.extension.modelling:modelling-common-resources";
	String GLOBAL_ID_PREFIX_RESOURCES = "asset-resource://" + RESOURCE_ASSET_NAME + "/";

	@GlobalId("new-16.png")
	Resource new16Png();
	@GlobalId("new-32.png")
	Resource new32Png();
	@GlobalId("new-64.png")
	Resource new64Png();
	
	@GlobalId("delete-16.png")
	Resource delete16Png();
	@GlobalId("delete-32.png")
	Resource delete32Png();
	@GlobalId("delete-64.png")
	Resource delete64Png();
	
	@GlobalId("open-16.png")
	Resource open16Png();
	@GlobalId("open-32.png")
	Resource open32Png();
	@GlobalId("open-64.png")
	Resource open64Png();
	
	@GlobalId("info-16.png")
	Resource info16Png();
	@GlobalId("info-32.png")
	Resource info32Png();
	@GlobalId("info-64.png")
	Resource info64Png();
	
	@GlobalId("model-16.png")
	Resource model16Png();
	@GlobalId("model-32.png")
	Resource model32Png();
	@GlobalId("model-64.png")
	Resource model64Png();

}
