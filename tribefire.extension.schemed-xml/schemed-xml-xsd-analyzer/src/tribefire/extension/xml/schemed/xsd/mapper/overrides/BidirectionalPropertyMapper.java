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
package tribefire.extension.xml.schemed.xsd.mapper.overrides;

import java.util.HashMap;
import java.util.Map;

import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.BidirectionalLink;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemaAddress;


public class BidirectionalPropertyMapper {
	private Map<String, String> targetToInjectionMap = new HashMap<String, String>();
	
	public BidirectionalPropertyMapper( BidirectionalLink ...bidirectionalLinks) {	
		if (bidirectionalLinks != null) {			
			for (BidirectionalLink backlink: bidirectionalLinks) {
				SchemaAddress schemaAddress = backlink.getSchemaAddress();				
				String key = schemaAddress.getParent() + "." + schemaAddress.getElement();
				String target = backlink.getBacklinkProperty();				
				targetToInjectionMap.put( key, target);
			}
		}
	}

	public String getBidirectionalTarget(String schemaAddress) throws RuntimeException {	
		 return targetToInjectionMap.get( schemaAddress);
	}
	
	
}
