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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;

import tribefire.extension.xml.schemed.mapping.metadata.EnumConstantMappingMetaData;
import tribefire.extension.xml.schemed.mapping.metadata.EnumTypeMappingMetaData;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.SimpleType;
import tribefire.extension.xml.schemed.model.xsd.restrictions.Enumeration;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.ContextCommons;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.SchemaMappingContext;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.schema.QPath;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.schema.commons.AnalyzerCommons;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.handlers.type.TypeResolverResponse;

public class EnumerationResolver {

	public static TypeResolverResponse resolve(SchemaMappingContext context, List<Enumeration> enumerations) {
		SimpleType typeToMap = ContextCommons.getNextSimpleType( context);
		String name = typeToMap.getName();
		if (name == null) {  
			name = ContextCommons.getPossibleTypeNameForSimpleType(context);
		}
		//Restriction restriction = ContextCommons.getNextSchemaEntity( context.currentEntityStack, Restriction.T);
		
		name = AnalyzerCommons.assertNonCollidingTypeName( context, name);

		QPath qpath = context.qpathGenerator.generateQPathForSchemaEntity( typeToMap.getDeclaringSchema());
		
		GmEnumType enumType = context.mappingContext.typeMapper.generateGmEnumType( qpath, typeToMap, name);
		
		EnumTypeMappingMetaData enumMetaData = context.mappingContext.metaDataMapper.acquireMetaData(enumType);
		enumMetaData.setXsdName(name);
		Namespace targetNamespace = typeToMap.getDeclaringSchema().getTargetNamespace();
		if (targetNamespace != null) {
			enumMetaData.setNamespace( targetNamespace.getUri());
		}
		
		List<String> enumValues = new ArrayList<String>();
		for (Enumeration enumeration : enumerations) {
			String value = enumeration.getValue();
			enumValues.add(value);
		}
		Map<String,String> mappedValues = context.mappingContext.nameMapper.generateJavaCompatibleEnumValues( enumValues);
		for (Entry<String, String> entry : mappedValues.entrySet()) {
			GmEnumConstant constant = context.mappingContext.typeMapper.generateGmEnumConstant( entry.getKey(), enumType);
			EnumConstantMappingMetaData constantMetaData = context.mappingContext.metaDataMapper.acquireMetaData( constant);
			constantMetaData.setXsdName( entry.getValue());
			
			//context.mappingContext.metaDataMapper.mapEnumConstant(constant, entry.getValue());
		}
		
		TypeResolverResponse response = new TypeResolverResponse();
		response.setGmType( enumType);
		response.setApparentTypeName( name);
		response.setActualTypeName( name);
		return response;
	}

}
