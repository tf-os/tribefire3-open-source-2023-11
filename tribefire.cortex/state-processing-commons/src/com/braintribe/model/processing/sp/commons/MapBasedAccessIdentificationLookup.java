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
package com.braintribe.model.processing.sp.commons;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.cfg.Required;
import com.braintribe.model.access.AccessIdentificationLookup;
import com.braintribe.model.access.IncrementalAccess;

/**
 * @author pit
 *
 */
public class MapBasedAccessIdentificationLookup implements AccessIdentificationLookup {
	
	private Map<String,IncrementalAccess> idToAccessMap;
	private Map<IncrementalAccess, String> accessToIdMap;
	
	@Required
	public void setIdToAccessMap(Map<String, IncrementalAccess> idToAccessMap) {
		this.idToAccessMap = idToAccessMap;
		accessToIdMap = new HashMap<IncrementalAccess, String>();
		for (Entry<String, IncrementalAccess> entry : idToAccessMap.entrySet()) {
			accessToIdMap.put( entry.getValue(), entry.getKey());
		}
	}

	@Override
	public String lookupAccessId(IncrementalAccess access) {
		return accessToIdMap.get( access);
	}

	@Override
	public IncrementalAccess lookupAccess(String id) {
		return idToAccessMap.get( id);
	}

}
