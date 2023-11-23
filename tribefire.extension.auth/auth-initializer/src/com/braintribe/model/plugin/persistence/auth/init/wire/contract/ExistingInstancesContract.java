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
package com.braintribe.model.plugin.persistence.auth.init.wire.contract;

import com.braintribe.gm.persistence.initializer.support.impl.lookup.GlobalId;
import com.braintribe.gm.persistence.initializer.support.impl.lookup.InstanceLookup;
import com.braintribe.model.deployment.Cartridge;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.auth.AuthConstants;
import com.braintribe.wire.api.space.WireSpace;

@InstanceLookup(lookupOnly=true)
public interface ExistingInstancesContract extends WireSpace {

	@GlobalId("model:" + AuthConstants.DEPLOYMENT_MODEL_QUALIFIEDNAME)
	GmMetaModel deploymentModel();

	@GlobalId("model:" + AuthConstants.SERVICE_MODEL_QUALIFIEDNAME)
	GmMetaModel serviceModel();
	
	@GlobalId("model:" + AuthConstants.DATA_MODEL_QUALIFIEDNAME)
	GmMetaModel dataModel();
		
	@GlobalId(AuthConstants.CARTRIDGE_GLOBAL_ID)
	Cartridge cartridge();
	
}

