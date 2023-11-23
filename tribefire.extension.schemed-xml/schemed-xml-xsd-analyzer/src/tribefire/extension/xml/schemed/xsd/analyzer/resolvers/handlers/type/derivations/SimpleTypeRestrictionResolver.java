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
package tribefire.extension.xml.schemed.xsd.analyzer.resolvers.handlers.type.derivations;

import java.util.Collection;
import java.util.List;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.data.MetaData;

import tribefire.extension.xml.schemed.marshaller.commons.QNameExpert;
import tribefire.extension.xml.schemed.model.xsd.QName;
import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.model.xsd.SimpleType;
import tribefire.extension.xml.schemed.model.xsd.SimpleTypeRestriction;
import tribefire.extension.xml.schemed.model.xsd.restrictions.Enumeration;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.ContextCommons;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.SchemaMappingContext;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.schema.commons.AnalyzerCommons;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.handlers.type.TypeResolver;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.handlers.type.TypeResolverResponse;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.structure.EnumerationResolver;
import tribefire.extension.xml.schemed.xsd.mapper.metadata.MetaDataExpert;

public class SimpleTypeRestrictionResolver {

	public static TypeResolverResponse analyze(SchemaMappingContext context, SimpleTypeRestriction restriction) {
		context.currentEntityStack.push( restriction);
		try {
		TypeResolverResponse baseResponse = null;
		String base = restriction.getBase();
		Schema declaringSchema = restriction.getDeclaringSchema();
		if (base != null) {
			QName qBase = QNameExpert.parse(base);
			baseResponse = TypeResolver.acquireType(context, declaringSchema, qBase);
		}
		else {
			SimpleType simpleType = restriction.getSimpleType();
			if (simpleType != null) {
				baseResponse = TypeResolver.acquireType(context, simpleType);
			}
		}
		if (baseResponse == null) {
			throw new IllegalStateException("cannot find base type for restriction");
		}
		
		TypeResolverResponse response = null;
		// 
		// enumeration cannot just be handled by metadata, so it needs to be handled here 
		// enumeration is a GmEnumType regardless of the base, and cannot have any other data 
		List<Enumeration> enumerations = restriction.getEnumerations();
		if (!enumerations.isEmpty()) {
			response = EnumerationResolver.resolve( context, enumerations);
			response.setActualTypeName( baseResponse.getActualTypeName());
			response.setApparentTypeName(baseResponse.getApparentTypeName());
		}	
		else {		
			SimpleType typeToMap = ContextCommons.getNextSimpleType( context);
			String name = typeToMap.getName();
			if (name == null) {  
				name = ContextCommons.getPossibleTypeNameForSimpleType(context);
			}
			if (name == null) {
				name = context.mappingContext.nameMapper.generateJavaCompatibleTypeNameForVirtualRestrictionType(baseResponse.getActualTypeName().getLocalPart());
			}
			response  = AnalyzerCommons.buildEntityTypeOutofSimpleType(context, declaringSchema, typeToMap, name, baseResponse.getGmType(), baseResponse.getActualTypeName());
			Collection<MetaData> collectedMetaDataForTypeRestriction = MetaDataExpert.createMetaDataForSimpleTypeRestriction( restriction);
			if (!collectedMetaDataForTypeRestriction.isEmpty()) {
				((GmEntityType) response.getGmType()).getMetaData().addAll( collectedMetaDataForTypeRestriction);
			}
		}		 
		return response;
		}
		finally {
			context.currentEntityStack.pop();
		}
	}

}
