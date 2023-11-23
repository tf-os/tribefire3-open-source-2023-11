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

import com.braintribe.logging.Logger;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;

import tribefire.extension.xml.schemed.mapping.metadata.EntityTypeMappingMetaData;
import tribefire.extension.xml.schemed.model.xsd.All;
import tribefire.extension.xml.schemed.model.xsd.AttributeGroup;
import tribefire.extension.xml.schemed.model.xsd.Choice;
import tribefire.extension.xml.schemed.model.xsd.ComplexType;
import tribefire.extension.xml.schemed.model.xsd.Group;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.Sequence;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.ContextCommons;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.SchemaMappingContext;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.schema.QPath;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.schema.commons.AnalyzerCommons;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.ResolverCommons;
import tribefire.extension.xml.schemed.xsd.api.mapper.name.HasTokens;

/**
 * an expert for {@link ComplexType}.. <br/>
 * it can either have a {@link Sequence} or {@link Choice} or {@link Group} or {@link All}, and any attribute or {@link AttributeGroup}<br/>
 * if either {@link Sequence} or {@link Choice} return only a single {@link GmProperty}, it may be that this reflects a virtual type.
 * in this case, this type may replace the actual type we would generate here (by renaming the type's signature and changing the mapping)
 * 
 * @author pit
 *
 */
public class ComplexTypeResolver implements HasTokens{
	private static Logger log = Logger.getLogger(ComplexTypeResolver.class);
	private static boolean lenientVirtualNaming = true;
	
	public static TypeResolverResponse analyze( SchemaMappingContext context, ComplexType complexType) {
		context.currentEntityStack.push( complexType);	
		try {
			String typeName = ensureTypeName(context, complexType.getName());
			
			// as we might have a recursive situation, we must create a type here. we might redefine it later, but still.. 
			GmEntityType producedGmEntityType = acquireEntityType(context, complexType);
			

			if (complexType.getComplexContent() != null) {
				TypeResolverResponse complexContentResponse = ComplexContentResolver.acquireEntityType( context, complexType.getComplexContent());
				// remap
				GmEntityType existingGmEntityType = (GmEntityType) complexContentResponse.getGmType();
				GmEntityType remappedType = context.mappingContext.typeMapper.remapGmEntityType(complexType, existingGmEntityType, typeName);			
				remappedType.getMetaData().addAll( existingGmEntityType.getMetaData());
				complexContentResponse.setGmType(remappedType);				
				complexContentResponse.setActualTypeName(typeName);
				return complexContentResponse;
				
			}
			if (complexType.getSimpleContent() != null) {
				TypeResolverResponse simpleContentResponse =  SimpleContentResolver.acquireEntityType( context, complexType.getSimpleContent());
				GmEntityType existingGmEntityType = (GmEntityType) simpleContentResponse.getGmType();
				GmEntityType remappedType = context.mappingContext.typeMapper.remapGmEntityType(complexType, existingGmEntityType, typeName);
				simpleContentResponse.setGmType(remappedType);
				remappedType.getMetaData().addAll( existingGmEntityType.getMetaData());
				simpleContentResponse.setActualTypeName(typeName);
				return simpleContentResponse;
			}

						
			// extract all properties
			// attributes can always be here 
			List<GmProperty> attributes = ResolverCommons.processAttributes( context, complexType);
			// attributes from groups can always be here 
			List<GmProperty> attributesFromGroups = ResolverCommons.processAttributeGroups( context, complexType);
			
			/*
			 * SEQUENCE 
			 */
			
			// only one of the following can be here 
			// if only one property exists in the properties from the sequence, it *might* be a virtual type, 
			// so we must look at it's name to find out. 
			// 
			List<GmProperty> propertiesFromSequence = ResolverCommons.processSequence(context, complexType, false);
			if (!propertiesFromSequence.isEmpty()) {
				GmEntityType virtualSequenceType = TypeResolver.getStructuralTypeFromProperties(propertiesFromSequence);
				if (virtualSequenceType != null) {
					// 
					context.mappingContext.typeMapper.unmapGmEntityType(complexType, producedGmEntityType);
					producedGmEntityType = context.mappingContext.typeMapper.remapGmEntityType(complexType, virtualSequenceType, typeName);		
					producedGmEntityType.getMetaData().addAll( virtualSequenceType.getMetaData());
				}		
				else {
					ResolverCommons.attachPropertiesToGmEntityType(context, producedGmEntityType, propertiesFromSequence);
				}
				
				// attach any attributes 
				ResolverCommons.attachPropertiesToGmEntityType(context, producedGmEntityType, attributes);
				// attach any grouped attributes
				ResolverCommons.attachPropertiesToGmEntityType(context, producedGmEntityType, attributesFromGroups);
				// build response 
				TypeResolverResponse response = buildResponse(producedGmEntityType, ensureTypeName(context, typeName));
				return response;
			}
			
			/*
			 * CHOICE
			 */
			
			// if only one property exists in the properties from the choice, it *might* be a virtual type, 
			// so we must look at it's name to find out. 
			List<GmProperty> propertiesFromChoice = ResolverCommons.processChoice(context, complexType, false);
			if (!propertiesFromChoice.isEmpty()) {
				GmEntityType virtualChoiceType = TypeResolver.getStructuralTypeFromProperties( propertiesFromChoice);
				if (virtualChoiceType != null) {
					context.mappingContext.typeMapper.unmapGmEntityType(complexType, producedGmEntityType);
					producedGmEntityType = context.mappingContext.typeMapper.remapGmEntityType(complexType, virtualChoiceType, typeName);
					producedGmEntityType.getMetaData().addAll( virtualChoiceType.getMetaData());
				}				 									
				else {
					ResolverCommons.attachPropertiesToGmEntityType(context, producedGmEntityType, propertiesFromChoice);
				}
				
				// attach any attributes 
				ResolverCommons.attachPropertiesToGmEntityType(context, producedGmEntityType, attributes);
				// attach any grouped attributes
				ResolverCommons.attachPropertiesToGmEntityType(context, producedGmEntityType, attributesFromGroups);
				// build response 
				TypeResolverResponse response = buildResponse(producedGmEntityType, ensureTypeName(context, typeName));
				return response;		
			}
			//
			// no type remapping took place, as neither sequence nor choice has been found 
			//
			//producedGmEntityType = acquireEntityType(context, complexType);
			// attach any attributes 
			ResolverCommons.attachPropertiesToGmEntityType(context, producedGmEntityType, attributes);
			// attach any grouped attributes
			ResolverCommons.attachPropertiesToGmEntityType(context, producedGmEntityType, attributesFromGroups);
			
			TypeResolverResponse response = buildResponse(producedGmEntityType, typeName);
			
			// simply properties from the group
			List<GmProperty> propertiesFromGroup = ResolverCommons.processGroup(context, complexType, false);
			if (!propertiesFromGroup.isEmpty()) {
				ResolverCommons.attachPropertiesToGmEntityType(context, producedGmEntityType, propertiesFromGroup);
				return response;
			}
			// simply properties from the all 
			List<GmProperty> propertiesFromAll = ResolverCommons.processAll( context, complexType, false);
			if (!propertiesFromAll.isEmpty()) {
				ResolverCommons.attachPropertiesToGmEntityType(context, producedGmEntityType, propertiesFromAll);
				return response;
			}
			
			if (attributes.isEmpty() && attributesFromGroups.isEmpty()) {			
				return response;
			}
			return response;
		}
		finally {
			context.currentEntityStack.pop();
		}
	}

	private static String ensureTypeName(SchemaMappingContext context, String name) {
		if (name == null) {
			name = ContextCommons.getCurrentNamingProposalForTypes(context);
			// mark empty names as "virtual"
			if (name != null && !lenientVirtualNaming) {				
				name = VIRTUAL_TYPE_PREFIX + name;
			}
		}
		if (name != null) {
			String proposedName = context.mappingContext.nameMapper.generateJavaCompatibleTypeName(name);
			proposedName = AnalyzerCommons.assertNonCollidingTypeName( context, proposedName);
			return proposedName;
		}
		return null;
	}

	private static TypeResolverResponse buildResponse(GmEntityType producedGmEntityType, String typeName) {
		TypeResolverResponse response = new TypeResolverResponse();
		response.setGmType(producedGmEntityType);				
		response.setActualTypeName( typeName);
		response.setApparentTypeName( typeName);
		return response;
	}

	private static GmEntityType acquireEntityType( SchemaMappingContext context, ComplexType complexType) {		
		QPath qpath = context.qpathGenerator.generateQPathForSchemaEntity( complexType.getDeclaringSchema());
		
		String name = complexType.getName();
		if (name == null) {
			name = ensureTypeName(context, name);
		}
		else {
			String overridingName = context.mappingContext.nameMapper.getOverridingName(name, null);
			if (overridingName != null) { 
				name = overridingName;
				log.debug("overriding complex type name [" + name + "] by [" + overridingName + "]");
			}
		}
		
		
		
		// intercept here for substitutions
		GmEntityType entityType = context.mappingContext.typeMapper.getSubstitutingType(name); 
		if (entityType == null) {
			entityType = context.mappingContext.typeMapper.generateGmEntityType(qpath, complexType, name);
		}
		EntityTypeMappingMetaData entityTypeMappingMetaData = context.mappingContext.metaDataMapper.acquireMetaData(entityType);
		Namespace targetNamespace = complexType.getDeclaringSchema().getTargetNamespace();
		if (targetNamespace != null) {
			entityTypeMappingMetaData.setNamespace( targetNamespace.getUri());
		}
		if (complexType.getName() != null) {
			entityTypeMappingMetaData.setXsdName( complexType.getName());
		}
		else {
			entityTypeMappingMetaData.setIsVirtual(true);
		}
		return entityType;
	}
}
