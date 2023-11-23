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
package tribefire.extension.xml.schemed.xsd.analyzer.registry.context;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

import tribefire.extension.xml.schemed.model.xsd.Attribute;
import tribefire.extension.xml.schemed.model.xsd.ComplexType;
import tribefire.extension.xml.schemed.model.xsd.Element;
import tribefire.extension.xml.schemed.model.xsd.SchemaEntity;
import tribefire.extension.xml.schemed.model.xsd.SimpleType;
import tribefire.extension.xml.schemed.model.xsd.Type;
import tribefire.extension.xml.schemed.model.xsd.archetypes.HasName;

public class ContextCommons {
	enum Naming { type, property};
	enum SchemaEntityType { complex, simple, element, attribute, restriction, extension, any};

	public static ComplexType getCurrentComplexType( SchemaMappingContext context) {
		return (ComplexType) getCurrentType( context.currentEntityStack, SchemaEntityType.complex);
	}
	public static SimpleType getCurrentSimpleType( SchemaMappingContext context) {
		return (SimpleType) getCurrentType( context.currentEntityStack, SchemaEntityType.simple);
	}
	
	public static Type getCurrentType( SchemaMappingContext context) {
		return (SimpleType) getCurrentType( context.currentEntityStack, SchemaEntityType.any);
	}
			
	public static Type getCurrentType( Stack<SchemaEntity> stack, SchemaEntityType typeType) {
		if (stack.isEmpty()) 
			return null;	
		int size = stack.size();
		for (int i = size-1; i >= 0; i--) {
			SchemaEntity suspect = stack.get(i);
			if (suspect instanceof Type == false) { 
				continue;
			}
			Type type = (Type) suspect;
			switch (typeType) {
				case complex:
					if (type instanceof ComplexType) {
						return (ComplexType) type;
					}
					break;
				case simple:
					if (type instanceof SimpleType) {
						return (SimpleType) type;
					}
				case any:			 
					return type;							
			}		
		}		
		return null;
	}
	
	private static String getCurrentNamingProposal( Stack<SchemaEntity> stack, Naming naming) {
		if (stack.isEmpty()) 
			return null;	
		int size = stack.size();
		for (int i = size-1; i >= 0; i--) {
			SchemaEntity suspect = stack.get(i);
			if (suspect instanceof HasName == false) {
				continue;
			}			
			HasName hasName = (HasName) suspect;
			switch (naming) {
				case property:
					if (hasName instanceof Element) {
						 return hasName.getName();
					}
					break;
				case type:
					if (hasName instanceof Type) {
						return hasName.getName();
					}
					break;			 
			}			
		}
		return null;		
	}
	public static String getCurrentNamingProposalForProperties( SchemaMappingContext context) {
		return getCurrentNamingProposal(context.currentEntityStack, Naming.property);
	}
	
	public static String getCurrentNamingProposalForTypes( SchemaMappingContext context) {
		String proposal = getCurrentNamingProposal(context.currentEntityStack, Naming.type);
		if (proposal == null) {
			proposal = getCurrentNamingProposal(context.currentEntityStack, Naming.property);
		}
		return proposal;
	}
	
	public static String getCurrentNamingProposalForProperties( Stack<SchemaEntity> stack) {
		return getCurrentNamingProposal(stack, Naming.property);
	}
	
	public static String getCurrentNamingProposalForTypes( Stack<SchemaEntity> stack) {
		return getCurrentNamingProposal(stack, Naming.type);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getNextSchemaEntity(Stack<SchemaEntity> stack, SchemaEntityType type ) {		
		if (stack.isEmpty()) 
			return null;	
		int size = stack.size();
		for (int i = size-1; i >= 0; i--) {
			SchemaEntity suspect = stack.get(i);
						
			switch ( type) {				
				case attribute:
					if (suspect instanceof Attribute) 
						return (T) suspect;					
				case complex:
					if (suspect instanceof ComplexType)
						return (T) suspect;
				case element:
					if (suspect instanceof Element)
						return (T) suspect;
				case simple:
					if (suspect instanceof SimpleType)
						return (T) suspect;
				case any:
				default:
					continue;				
			}			
		}
		return null;	
	}
	
	public static <T> T getNextSchemaEntity(Stack<SchemaEntity> stack, EntityType<? extends GenericEntity> type ) {		
		if (stack.isEmpty()) 
			return null;	
		int size = stack.size();
		for (int i = size-1; i >= 0; i--) {
			SchemaEntity suspect = stack.get(i);
			if (suspect.entityType() == type)
				return (T) suspect;
		}
		return null;	
	}
	
	private static SchemaEntityType getTypeOfSchemaEntity( SchemaEntity entity) {
		if (entity instanceof ComplexType)
			return SchemaEntityType.complex;
		if (entity instanceof SimpleType)
			return SchemaEntityType.simple;
		if (entity instanceof Attribute)
			return SchemaEntityType.attribute;
		if (entity instanceof Element)
			return SchemaEntityType.element;
		return SchemaEntityType.any;
	}
	
	public static HasName getNextNamedEntity( Stack<SchemaEntity> stack, SchemaEntityType ...entityTypes) {
		if (stack.isEmpty()) 
			return null;
		Set<SchemaEntityType> types = new HashSet<>( Arrays.asList( entityTypes));
		int size = stack.size();
		for (int i = size-1; i >= 0; i--) {
			SchemaEntity suspect = stack.get(i);
			SchemaEntityType type = getTypeOfSchemaEntity(suspect);
			if (types.contains(type) && suspect instanceof HasName)
				return (HasName) suspect;
		}
		return null;
	}
	
	public static SimpleType getNextSimpleType( SchemaMappingContext context) {
		return getNextSchemaEntity( context.currentEntityStack, SchemaEntityType.simple);
	}
	
	public static String getPossibleTypeNameForSimpleType( SchemaMappingContext context) {
		HasName suffixEntity = getNextNamedEntity( context.currentEntityStack, SchemaEntityType.attribute, SchemaEntityType.element);
		HasName prefixEntity = getNextNamedEntity( context.currentEntityStack, SchemaEntityType.complex, SchemaEntityType.element);
		
		if (suffixEntity == null || prefixEntity == null || suffixEntity.getName() == null || prefixEntity.getName() == null)
			return null; // cannot name need to generate virtual name
		if (!suffixEntity.equals(prefixEntity)) {					
			return context.mappingContext.nameMapper.generateJavaCompatibleTypeName( prefixEntity.getName() + suffixEntity.getName().substring(0, 1).toUpperCase() + suffixEntity.getName().substring(1));
		}
		else {
			return context.mappingContext.nameMapper.generateJavaCompatibleTypeName( suffixEntity.getName().substring(0, 1).toUpperCase() + suffixEntity.getName().substring(1));
		}
		
	}
		
	/*
	 * naming proposals 
	 * a) types
	 * 	needs to be called if type has no name.
	 * 	can walk down until other type is found
	 * 		within that range, a matching type wins
	 * 		if no type name is found, then attributes and elements can be used, and the prefix of the next complexType is taken
	 * 
	 * 		
	 */
		
}
