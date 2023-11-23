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
package tribefire.extension.xml.schemed.mapper.structure;

import com.braintribe.model.meta.GmProperty;

import tribefire.extension.xml.schemed.mapping.metadata.PropertyMappingMetaData;

/**
 * info for a {@link GmProperty}
 * @author pit
 *
 */
public class MapperInfoForProperty {
	private GmProperty property;
	private PropertyMappingMetaData metaData;

	public PropertyMappingMetaData getMetaData() {
		return metaData;
	}
	public void setMetaData(PropertyMappingMetaData metaData) {
		this.metaData = metaData;
	}

	public GmProperty getProperty() {
		return property;
	}
	public void setProperty(GmProperty property) {
		this.property = property;
	}
	
	
		
}
