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
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.itw.analysis.JavaTypeAnalysis;

import tribefire.extension.xml.schemed.mapping.metadata.EntityTypeMappingMetaData;
import tribefire.extension.xml.schemed.marshaller.commons.QNameExpert;
import tribefire.extension.xml.schemed.model.xsd.Extension;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.SchemaMappingContext;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.schema.QPath;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.schema.commons.AnalyzerCommons;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.ResolverCommons;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.handlers.type.TypeResolver;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.handlers.type.TypeResolverResponse;

/**
 * a resolver for {@link Extension}<br/>
 * base can be either a {@link GmSimpleType} or a {@link GmEntityType}. If the base is a {@link GmSimpleType}, an {@link GmEntityType} is automatically
 * created from it. If base is a {@link GmEntityType} a new {@link GmEntityType} is created that derives from the base.
 * 
 * @author pit
 *
 */
public class ExtensionResolver {

	public static TypeResolverResponse acquireEntityType(SchemaMappingContext context, Extension extension) {
		context.currentEntityStack.push(extension);
		try {
					
			String base = extension.getBase();
			if (base == null) {
				throw new IllegalStateException("a QNAME base must be defined");
			}
			
			TypeResolverResponse baseResponse = TypeResolver.acquireType(context, extension.getDeclaringSchema(), QNameExpert.parse(base));
			GmType baseType = baseResponse.getGmType();
			
			String virtualTypeName = context.mappingContext.nameMapper.generateJavaCompatibleTypeNameForVirtualComplexExtendedType(base);
			virtualTypeName = AnalyzerCommons.assertNonCollidingTypeName( context, virtualTypeName);
			QPath qpath = context.qpathGenerator.generateQPathForSchemaEntity( extension.getDeclaringSchema());
		
			GmEntityType gmEntityType;
			
			if (baseType instanceof GmSimpleType) {
				TypeResolverResponse expertResponse = AnalyzerCommons.buildEntityTypeOutofSimpleType(context, extension.getDeclaringSchema(), null, virtualTypeName, baseType, QNameExpert.parse(base));
				gmEntityType = (GmEntityType) expertResponse.getGmType();
			}
			else if (baseType instanceof GmEnumType){
				TypeResolverResponse expertResponse = AnalyzerCommons.buildEntityTypeOutofEnumType(context, extension.getDeclaringSchema(), null, virtualTypeName, baseType, QNameExpert.parse(base));
				gmEntityType = (GmEntityType) expertResponse.getGmType();
			}
			else {
				gmEntityType = context.mappingContext.typeMapper.generateGmEntityType(qpath, null, virtualTypeName);
				gmEntityType.getSuperTypes().add( (GmEntityType) baseType);
				EntityTypeMappingMetaData entityTypeMappingMetaData = context.mappingContext.metaDataMapper.acquireMetaData(gmEntityType);
				entityTypeMappingMetaData.setIsVirtual(true);
				Namespace targetNamespace = extension.getDeclaringSchema().getTargetNamespace();
				if (targetNamespace != null) {
					entityTypeMappingMetaData.setNamespace( targetNamespace.getUri());
				}
				
			}
			List<GmProperty> properties = new ArrayList<>();
			
			List<GmProperty> propertiesFromSequence = ResolverCommons.processSequence(context, extension, false);
			List<GmProperty> propertiesFromChoice = ResolverCommons.processChoice(context, extension, false);
			List<GmProperty> propertiesFromGroup = ResolverCommons.processGroup(context, extension, false);
			List<GmProperty> propertiesFromAll = ResolverCommons.processAll(context, extension, false);
			
			List<GmProperty> propertiesFromAttributes = ResolverCommons.processAttributes(context, extension);
			List<GmProperty> propertiesFromAttributeGroups = ResolverCommons.processAttributeGroups(context, extension);
			
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
