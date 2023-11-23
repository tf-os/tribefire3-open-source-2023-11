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
package tribefire.cortex.assets.darktheme_wb_initializer.wire.contract;

import com.braintribe.model.resource.Resource;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.initializer.support.impl.lookup.GlobalId;
import tribefire.cortex.initializer.support.impl.lookup.InstanceLookup;

@InstanceLookup(lookupOnly=true, globalIdPrefix=DarkthemeWbResourceContract.GLOBAL_ID_PREFIX)
public interface DarkthemeWbResourceContract extends WireSpace {

	String RESOURCE_ASSET_NAME = "tribefire.cortex.assets:tribefire-standard-wb-resources";
	String GLOBAL_ID_PREFIX = "asset-resource://" + RESOURCE_ASSET_NAME + "/";
	
	@GlobalId("run-16.png")
	Resource run16Png();

	@GlobalId("run-32.png")
	Resource run32Png();
	
	@GlobalId("logo.png")
	Resource logoPng();

	@GlobalId("home-16.png")
	Resource home16Png();
	@GlobalId("home-32.png")
	Resource home32Png();
	@GlobalId("home-64.png")
	Resource home64Png();

	@GlobalId("changes-16.png")
	Resource changes16Png();
	@GlobalId("changes-32.png")
	Resource changes32Png();
	@GlobalId("changes-64.png")
	Resource changes64Png();

	@GlobalId("clipboard-16.png")
	Resource clipboard16Png();
	@GlobalId("clipboard-32.png")
	Resource clipboard32Png();
	@GlobalId("clipboard-64.png")
	Resource clipboard64Png();

	@GlobalId("notification-16.png")
	Resource notification16Png();
	@GlobalId("notification-32.png")
	Resource notification32Png();
	@GlobalId("notification-64.png")
	Resource notification64Png();

	@GlobalId("magnifier-16.png")
	Resource magnifier16Png();

	@GlobalId("explorer-style-template.css")
	Resource explorerStyleTemplateCss();

	@GlobalId("new-16.png")
	Resource new16Png();
	
	@GlobalId("new-32.png")
	Resource new32Png();

	@GlobalId("upload-32.png")
	Resource upload32Png();

	@GlobalId("undo-32.png")
	Resource undo32Png();

	@GlobalId("redo-32.png")
	Resource redo32Png();

	@GlobalId("commit-32.png")
	Resource commit32Png();
}
