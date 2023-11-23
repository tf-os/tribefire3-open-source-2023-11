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

import com.braintribe.logging.Logger;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.itw.analysis.JavaTypeAnalysis;

import tribefire.extension.xml.schemed.mapping.metadata.PropertyMappingMetaData;
import tribefire.extension.xml.schemed.marshaller.commons.QNameExpert;
import tribefire.extension.xml.schemed.model.xsd.Attribute;
import tribefire.extension.xml.schemed.model.xsd.QName;
import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.model.xsd.SimpleType;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.SchemaMappingContext;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.schema.commons.AnalyzerCommons;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.ResolverCommons;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.handlers.type.TypeResolver;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.handlers.type.TypeResolverResponse;

public class AttributeResolver {
	private static Logger log = Logger.getLogger(AttributeResolver.class);

	public static GmProperty resolve(SchemaMappingContext context, Attribute attribute) {
		Attribute actualAttribute = AnalyzerCommons.retrieveAttribute(context, attribute);
		context.currentEntityStack.push(actualAttribute);
		
		
		String attributeName = actualAttribute.getName();
		// TODO : MARKER 'XML-CLUDGE'
		if (attributeName.contains(":")) {
			attributeName = attributeName.replace(':', '_');  
		}
		String name = context.mappingContext.nameMapper.generateJavaCompatiblePropertyName( attributeName);		
		String parentTypeName = ResolverCommons.findParentName( context.currentEntityStack);		
		
		
		// intercept here for substitution
		GmEntityType substitionType = context.mappingContext.typeMapper.getSubstitutingType(parentTypeName, name);
		
		Schema declaringSchema = actualAttribute.getDeclaringSchema();
		try {
			
			QName typeReference = actualAttribute.getTypeReference();
			SimpleType type = actualAttribute.getType();
			TypeResolverResponse response;		
			
			// intercept here for substitution
			if (substitionType != null) {
				response = new TypeResolverResponse();
				response.setGmType(substitionType);
				response.setAlreadyAcquired(true);
				response.setActualTypeName("n/a");
				response.setApparentTypeName("n/a");
			}
			else {
				if (typeReference != null) {
					response = TypeResolver.acquireType(context, declaringSchema, typeReference);
				}
				else if (type != null) {
					response = TypeResolver.acquireType(context, type);
				}
				else {
					throw new IllegalStateException("no type found for attribute");
				}
			}
		
			String overridingName = context.mappingContext.nameMapper.getOverridingName( parentTypeName, name);
			if (overridingName != null) {
				name = overridingName;
				log.debug("overriding attribute property [" + parentTypeName + ":" + name + "] by [" + overridingName + "]");
			}
			

			String typeName = context.mappingContext.typeMapper.getMappedNameOfType( response.getGmType());
			boolean isId = false;
			if (typeName != null && typeName.equalsIgnoreCase( "ID")) {
				name = "_" + name;
				isId = true;
			}
			GmProperty gmProperty = context.mappingContext.typeMapper.generateGmProperty( name);
			gmProperty.setType( response.getGmType());
			gmProperty.setGlobalId( JavaTypeAnalysis.propertyGlobalId( response.getGmType().getTypeSignature(), name));
			
			PropertyMappingMetaData propertyMappingMetaData = context.mappingContext.metaDataMapper.acquireMetaData(gmProperty);
			propertyMappingMetaData.setIsAttribute(true);
			propertyMappingMetaData.setActualXsdType( QNameExpert.toString(response.getActualTypeName()));
			propertyMappingMetaData.setApparentXsdType( QNameExpert.toString( response.getApparentTypeName()));
			// TODO : MARKER 'XML-CLUDGE'
			propertyMappingMetaData.setXsdName( attributeName);
			propertyMappingMetaData.setFixedValue( actualAttribute.getFixed());
			propertyMappingMetaData.setDefaultValue( actualAttribute.getDefault());
			if (isId) {
				propertyMappingMetaData.setIsIdProperty(true);
			}
		
			
			
											
			return gmProperty;		
		}
		finally {
			context.currentEntityStack.pop();
		}
		
	}

}
