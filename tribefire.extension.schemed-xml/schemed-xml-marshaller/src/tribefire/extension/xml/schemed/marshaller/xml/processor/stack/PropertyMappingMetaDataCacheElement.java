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
package tribefire.extension.xml.schemed.marshaller.xml.processor.stack;

import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;

import tribefire.extension.xml.schemed.mapping.metadata.PropertyMappingMetaData;


public class PropertyMappingMetaDataCacheElement {
	
	public String fixedValue;
	public String apparentXsdType;
	public String actualXsdType;
	public boolean isMultiple;
	public boolean isUndefined;
	public GmType gmPropertyType;
	public String gmPropertyName;
	public GenericModelType propertyType;
	public GenericModelType elementType;
	public boolean isBacklinkProperty;
	

	public PropertyMappingMetaDataCacheElement(PropertyMappingMetaData propertyMappingMetaData) {
		GmProperty gmProperty = propertyMappingMetaData.getProperty();
		this.gmPropertyType = gmProperty.getType();
		this.gmPropertyName = gmProperty.getName();
		this.fixedValue = propertyMappingMetaData.getFixedValue();
		this.apparentXsdType = propertyMappingMetaData.getApparentXsdType();
		this.actualXsdType = propertyMappingMetaData.getActualXsdType();
		this.isMultiple = Boolean.TRUE.equals(propertyMappingMetaData.getIsMultiple());
		this.propertyType = gmPropertyType.reflectionType();
		this.isUndefined = propertyMappingMetaData.getIsUndefined();
		this.isBacklinkProperty = propertyMappingMetaData.getIsBacklinkProperty();
		
		TypeCode typeCode = this.propertyType.getTypeCode();
		switch (typeCode) {
			case setType:
			case listType:
				this.elementType = ((CollectionType) this.propertyType).getCollectionElementType();
				break;
			default:
				break;
		}
	}
	
}
