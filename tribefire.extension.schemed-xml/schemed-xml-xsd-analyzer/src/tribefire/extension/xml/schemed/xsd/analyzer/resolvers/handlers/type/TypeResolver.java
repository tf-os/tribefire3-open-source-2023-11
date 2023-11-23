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
package tribefire.extension.xml.schemed.xsd.analyzer.resolvers.handlers.type;

import java.util.List;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;

import tribefire.extension.xml.schemed.marshaller.commons.QNameExpert;
import tribefire.extension.xml.schemed.model.xsd.ComplexType;
import tribefire.extension.xml.schemed.model.xsd.QName;
import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.model.xsd.SimpleType;
import tribefire.extension.xml.schemed.model.xsd.Type;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.SchemaMappingContext;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.schema.commons.AnalyzerCommons;
import tribefire.extension.xml.schemed.xsd.api.mapper.name.HasTokens;

public class TypeResolver implements HasTokens {
	
	public static TypeResolverResponse acquireType( SchemaMappingContext context, Schema declaringSchema, QName reference) {
		TypeResolverResponse response = new TypeResolverResponse();
		GmType gmType = null;
		if (reference.getLocalPart().equalsIgnoreCase("anyType")) {
			gmType = context.mappingContext.typeMapper.acquireAnyType();
		}
		else {
			gmType = context.mappingContext.typeMapper.lookupStandardSimpleType( reference);
		}
		if (gmType != null) {
			response.setGmType(gmType);
			response.setAlreadyAcquired(true);
			response.setActualTypeName( reference);		
			String apparentTypeName = context.mappingContext.typeMapper.getSimpleStringBase(gmType);
			response.setApparentTypeName( apparentTypeName != null ? QNameExpert.parse(apparentTypeName) :  reference);
			return response;
		}		
		Type type = AnalyzerCommons.retrieveType(context, declaringSchema, reference);
		if (type == null) {
			throw new IllegalStateException( "no type with name [" + QNameExpert.toString(reference) + "] can be found");
		}
		return acquireType(context, type);
	}
	
	public static TypeResolverResponse acquireType( SchemaMappingContext context, Type type) {		
		GmType gmType = context.mappingContext.typeMapper.lookupType(type);
		if (gmType != null) {
			TypeResolverResponse response = new TypeResolverResponse();
			response.setGmType(gmType);
			response.setAlreadyAcquired( true);			
			String name = gmType.getTypeSignature().substring( gmType.getTypeSignature().lastIndexOf('.')+1);
			response.setActualTypeName( name);
			response.setApparentTypeName( name);		
			return response;			
		}	
		if (type instanceof SimpleType) {
			return acquireSimpleType( context, (SimpleType) type);
		}
		else if (type instanceof ComplexType){
			return acquireComplexType( context, (ComplexType) type);
		}
		else {
			throw new IllegalStateException( "this kind of Type cannot be processed [" + type.getClass().getName() + "]");
		}
		
	}

	private static TypeResolverResponse acquireComplexType(SchemaMappingContext context, ComplexType type) {
		TypeResolverResponse response = ComplexTypeResolver.analyze(context, type);
		return response;
	}

	private static TypeResolverResponse acquireSimpleType(SchemaMappingContext context, SimpleType type) {
		TypeResolverResponse response  = SimpleTypeResolver.resolve(context, type);
		return response;
	}
	
	public static boolean isVirtualType( GmEntityType gmEntityType) {
		String signature = gmEntityType.getTypeSignature();
		String name = signature.substring( signature.lastIndexOf('.')+1);		
		if (name.startsWith(VIRTUAL_TYPE_PREFIX ))
			return true;
		return false;
	}
	
	public static GmEntityType getStructuralTypeFromProperties( List<GmProperty> properties) {
		if (properties.size() != 1)
			return null;
		GmType gmTypeFromSequence =  properties.get(0).getType();
		if (gmTypeFromSequence instanceof GmEntityType && TypeResolver.isVirtualType( (GmEntityType) gmTypeFromSequence)) { 
			return (GmEntityType) gmTypeFromSequence;
		}
		return null;
	}
	
}
