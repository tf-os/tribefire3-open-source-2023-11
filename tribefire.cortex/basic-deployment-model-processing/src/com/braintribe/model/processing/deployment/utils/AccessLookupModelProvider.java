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
package com.braintribe.model.processing.deployment.utils;

import java.util.function.Function;

import com.braintribe.cfg.Required;
import com.braintribe.model.access.AccessIdentificationLookup;
import com.braintribe.model.access.AccessService;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.meta.GmMetaModel;



public class AccessLookupModelProvider implements Function<IncrementalAccess, GmMetaModel>{
	
	private AccessService accessService;
	private AccessIdentificationLookup accessIdentificationLookup;

	@Required
	public void setAccessService(AccessService accessService) {
		this.accessService = accessService;
	}
	@Required
	public void setAccessIdentificationLookup(AccessIdentificationLookup accessIdentificationLookup) {
		this.accessIdentificationLookup = accessIdentificationLookup;
	}

	@Override
	public GmMetaModel apply(IncrementalAccess access) throws RuntimeException {
		try {
			String accessId = accessIdentificationLookup.lookupAccessId(access);
			return accessService.getModelEnvironment(accessId).getDataModel();

		} catch (Exception e) {
			throw new RuntimeException("Error while providing meta model for Acccess: "+access,e);
		}
	}

}
