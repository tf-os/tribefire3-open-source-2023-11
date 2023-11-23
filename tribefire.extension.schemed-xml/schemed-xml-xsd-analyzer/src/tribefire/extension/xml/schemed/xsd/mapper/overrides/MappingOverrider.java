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

import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.MappingOverride;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemaAddress;


public class MappingOverrider  {
	private Map<String,String> xsdNamesToOveride = new HashMap<String, String>();

	public MappingOverrider(MappingOverride ... overrides) {
		for (MappingOverride override : overrides) {
			SchemaAddress address = override.getSchemaAddress();
			String property = address.getElement();
			String parent = address.getParent();
			String key;
			if (property == null) {
				key = parent;			
			}
			else {
				 key = parent + "." + property;
			}
			xsdNamesToOveride.put(key,  override.getNameOverride());
		}		
	}
	

	public String getOverride(String schemaAddress) throws RuntimeException {
		return (xsdNamesToOveride.get( schemaAddress));
	}
}
