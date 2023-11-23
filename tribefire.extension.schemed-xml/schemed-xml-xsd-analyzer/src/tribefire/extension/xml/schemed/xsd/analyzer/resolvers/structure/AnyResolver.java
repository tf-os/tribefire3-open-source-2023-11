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
package tribefire.extension.xml.schemed.xsd.analyzer.resolvers.structure;

import com.braintribe.model.meta.GmCollectionType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;

import tribefire.extension.xml.schemed.mapping.metadata.PropertyMappingMetaData;
import tribefire.extension.xml.schemed.model.xsd.Any;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.SchemaMappingContext;

public class AnyResolver {

	public static GmProperty resolve(SchemaMappingContext context, Any any, boolean overrideMultiple) {
		context.currentEntityStack.push( any);
		String propertyName = "any";		
		
		GmType anyType = context.mappingContext.typeMapper.acquireGenericEntityType();
		try {		
			int maxOccurs = any.getMaxOccurs();
			boolean multiple = (maxOccurs > 1 || maxOccurs < 0); 
			GmProperty gmProperty;
			
			if  (multiple || overrideMultiple) {
				propertyName = context.mappingContext.nameMapper.generateCollectionName( propertyName);
				gmProperty = context.mappingContext.typeMapper.generateGmProperty( propertyName);
				// 
				GmCollectionType gmCollectionType = context.mappingContext.typeMapper.acquireCollectionType( anyType, false);				
				gmProperty.setType(gmCollectionType);
			}
			else {
				propertyName = context.mappingContext.nameMapper.generateJavaCompatiblePropertyName( propertyName);
				gmProperty = context.mappingContext.typeMapper.generateGmProperty( propertyName);
				gmProperty.setType( anyType);
			}
			// create a mapping for the entity 
			PropertyMappingMetaData propertyMappingMetaData = context.mappingContext.metaDataMapper.acquireMetaData(gmProperty);
			propertyMappingMetaData.setIsMultiple( multiple);
			propertyMappingMetaData.setActualXsdType( "anyType");
			propertyMappingMetaData.setApparentXsdType( "anyType");
			propertyMappingMetaData.setXsdName( "any");
			
			return gmProperty;
		}
		finally {
			context.currentEntityStack.pop();
		}
	}
}
