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

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.itw.analysis.JavaTypeAnalysis;

import tribefire.extension.xml.schemed.mapping.metadata.EntityTypeMappingMetaData;
import tribefire.extension.xml.schemed.marshaller.commons.QNameExpert;
import tribefire.extension.xml.schemed.model.xsd.ComplexContentRestriction;
import tribefire.extension.xml.schemed.model.xsd.ComplexType;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.SchemaMappingContext;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.schema.QPath;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.schema.commons.AnalyzerCommons;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.ResolverCommons;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.handlers.type.TypeResolver;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.handlers.type.TypeResolverResponse;

/**
 * processes a {@link ComplexContentRestriction} on a {@link ComplexType} <br/>
 * contrary to all you'd expect, the result type DOES NOT derive from the base, as the restriction can only repeat the 
 * same structure as the base type does, and just modify modifiers such as "use", "default" and "fixed". ITW would not support 
 * that at it leads to redefinition of properties in its view. However, they are needed as meta data are attached (exactly to reflect the modifiers)
 *  
 * @author pit
 *
 */
public class ComplexTypeRestrictionResolver {

	public static TypeResolverResponse acquireEntityType(SchemaMappingContext context, ComplexContentRestriction restriction) {
		context.currentEntityStack.push(restriction);
		try {
			
			
			String base = restriction.getBase();
			if (base == null) {
				throw new IllegalStateException("a QNAME base must be defined");
			}
			
			TypeResolverResponse baseResponse = TypeResolver.acquireType(context, restriction.getDeclaringSchema(), QNameExpert.parse(base));
			GmType baseType = baseResponse.getGmType();
			
			if (baseType instanceof GmEntityType == false) {
				throw new IllegalStateException("base type of a complex type restriction can only be a complex type");
			}

			
			
			// 
			List<GmProperty> properties = new ArrayList<>();
			
			List<GmProperty> propertiesFromSequence = ResolverCommons.processSequence(context, restriction, false);
			List<GmProperty> propertiesFromChoice = ResolverCommons.processChoice(context, restriction, false);
			List<GmProperty> propertiesFromGroup = ResolverCommons.processGroup(context, restriction, false);
			List<GmProperty> propertiesFromAll = ResolverCommons.processAll(context, restriction, false);
			
			List<GmProperty> propertiesFromAttributes = ResolverCommons.processAttributes(context, restriction);
			List<GmProperty> propertiesFromAttributeGroups = ResolverCommons.processAttributeGroups(context, restriction);
			
			
			
			// create type 
			String virtualTypeName = context.mappingContext.nameMapper.generateJavaCompatibleTypeNameForVirtualComplexRestrictedType(base);
			virtualTypeName = AnalyzerCommons.assertNonCollidingTypeName( context, virtualTypeName);
			QPath qpath = context.qpathGenerator.generateQPathForSchemaEntity( restriction.getDeclaringSchema());
			GmEntityType gmEntityType = context.mappingContext.typeMapper.generateGmEntityType(qpath, null, virtualTypeName);
			EntityTypeMappingMetaData entityTypeMappingMetaData = context.mappingContext.metaDataMapper.acquireMetaData(gmEntityType);
			entityTypeMappingMetaData.setIsVirtual(true);
			Namespace targetNamespace = restriction.getDeclaringSchema().getTargetNamespace();
			if (targetNamespace != null) {
				entityTypeMappingMetaData.setNamespace( targetNamespace.getUri());
			}
			
			// cannot add the base as super type, as the restricted type may only repeat the same definition, just allowed to set
			// additional data such as "fixed", etc			
			/*
			GmEntityType baseGmEntityType = (GmEntityType) baseType;
			gmEntityType.getSuperTypes().add( baseGmEntityType);
			*/

			properties = ResolverCommons.combine( 	propertiesFromSequence.stream(), 
													propertiesFromChoice.stream(), 
													propertiesFromGroup.stream(), 
													propertiesFromAll.stream(),
													propertiesFromAttributes.stream(),
													propertiesFromAttributeGroups.stream()
												);
			
			// post process : only now the declaring type is known and attached
			gmEntityType.getProperties().addAll(properties);
			for (GmProperty property : properties) {
				property.setDeclaringType(gmEntityType);
				property.setGlobalId( JavaTypeAnalysis.propertyGlobalId( gmEntityType.getTypeSignature(), property.getName()));
			}
			TypeResolverResponse response = new TypeResolverResponse();
			response.setGmType(gmEntityType);
			response.setActualTypeName(virtualTypeName);
			response.setApparentTypeName(virtualTypeName);
			
			return response;
		}
		finally {
			context.currentEntityStack.pop();
		}
	}

}
