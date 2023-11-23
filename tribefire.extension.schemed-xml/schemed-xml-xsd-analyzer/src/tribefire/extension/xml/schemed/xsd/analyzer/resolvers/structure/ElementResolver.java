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
import com.braintribe.model.meta.GmCollectionType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;

import tribefire.extension.xml.schemed.mapping.metadata.EntityTypeMappingMetaData;
import tribefire.extension.xml.schemed.mapping.metadata.PropertyMappingMetaData;
import tribefire.extension.xml.schemed.marshaller.commons.QNameExpert;
import tribefire.extension.xml.schemed.model.xsd.Element;
import tribefire.extension.xml.schemed.model.xsd.QName;
import tribefire.extension.xml.schemed.model.xsd.Type;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.SchemaMappingContext;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.schema.commons.AnalyzerCommons;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.ResolverCommons;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.handlers.type.TypeResolver;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.handlers.type.TypeResolverResponse;


/**
 * resolves an {@link Element}, i.e. turns into a single {@link GmProperty}
 * @author pit
 *
 */
public class ElementResolver {
	private static Logger log = Logger.getLogger(ElementResolver.class);
	
	public static GmProperty resolve(SchemaMappingContext context, Element element, boolean overrideMultiple) {
		Element actualElement = AnalyzerCommons.retrieveElement( context, element);
		context.currentEntityStack.push( actualElement);
		String propertyName = actualElement.getName();
		String parentTypeName = ResolverCommons.findParentName( context.currentEntityStack);

		GmEntityType substitutionType = context.mappingContext.typeMapper.getSubstitutingType(parentTypeName, propertyName);
		try {
				
			
			QName typeReference = actualElement.getTypeReference();
			Type type = actualElement.getType(); 

			TypeResolverResponse response;
			// intercept here for substitution
			if (substitutionType != null) {
				response = new TypeResolverResponse();
				response.setGmType(substitutionType);
				response.setAlreadyAcquired(true);
				response.setActualTypeName("n/a");
				response.setApparentTypeName("n/a");
				
			} 
			else {
			
				if (typeReference != null) {
					response = TypeResolver.acquireType(context, actualElement.getDeclaringSchema(), typeReference);
				}
				else if (type != null){
					response = TypeResolver.acquireType(context, type);
				}
				else {
					typeReference = QName.T.create();
					typeReference.setLocalPart( "anyType");
					response = TypeResolver.acquireType(context, actualElement.getDeclaringSchema(), typeReference);
					//throw new IllegalStateException("cannot find a type for element [" + actualElement + "]");
				}
			}
		
			GmProperty gmProperty;
		
			// the name of the property may be influenced by the overriding names
			String overridingName = context.mappingContext.nameMapper.getOverridingName( parentTypeName, propertyName);
			if (overridingName != null) {
				propertyName = overridingName;
				log.debug("overriding property [" + parentTypeName + ":" + propertyName + "] by [" + overridingName + "]");
			}
			
			
			String typeName = context.mappingContext.typeMapper.getMappedNameOfType( response.getGmType());
			boolean isId = false;
			if (typeName != null && typeName.equalsIgnoreCase( "ID")) {
				propertyName = "_" + propertyName;
				isId = true;
			}
			// maxoccurs comes from the element definition and not from a referenced element
			int maxOccurs = element.getMaxOccurs();
			boolean multiple = (maxOccurs > 1 || maxOccurs < 0); 
			
			if  (multiple || overrideMultiple) {
				propertyName = context.mappingContext.nameMapper.generateCollectionName( propertyName);
				gmProperty = context.mappingContext.typeMapper.generateGmProperty( propertyName);
				// check override.. 
				boolean asSet = context.mappingContext.typeMapper.getIsCollectionTypeOverridenAsSet(parentTypeName, actualElement.getName());
				// 
				GmCollectionType gmCollectionType = context.mappingContext.typeMapper.acquireCollectionType( response.getGmType(), asSet);				
				gmProperty.setType(gmCollectionType);
			}
			else {
				propertyName = context.mappingContext.nameMapper.generateJavaCompatiblePropertyName( propertyName);
				gmProperty = context.mappingContext.typeMapper.generateGmProperty( propertyName);
				gmProperty.setType( response.getGmType());
			}
			String bidirectionToInject = context.mappingContext.typeMapper.getBacklinkPropertyToInjectFor(parentTypeName, actualElement.getName());
			if (bidirectionToInject != null && response.getGmType() instanceof GmEntityType) {
				GmType parentType = ResolverCommons.findParentType(context);
				if (parentType instanceof GmEntityType) {
					injectProperty( context, bidirectionToInject, (GmEntityType) response.getGmType(), (GmEntityType) parentType);
				}
				else {
					log.error( "parent must be a GmEntityType, but [" + parentType.getTypeSignature() + "] isn't");
				}
			}
			
			// create a mapping for the entity 
			PropertyMappingMetaData propertyMappingMetaData = context.mappingContext.metaDataMapper.acquireMetaData(gmProperty);
			propertyMappingMetaData.setIsMultiple( multiple || overrideMultiple);
			propertyMappingMetaData.setActualXsdType( QNameExpert.toString(response.getActualTypeName()));
			propertyMappingMetaData.setApparentXsdType( QNameExpert.toString( response.getApparentTypeName()));
			propertyMappingMetaData.setXsdName( actualElement.getName());
			propertyMappingMetaData.setNamespaceOverrides( actualElement != element);
			if (isId) {
				propertyMappingMetaData.setIsIdProperty(true);
			}
									
			return gmProperty;
			
		}
		finally {
			context.currentEntityStack.pop();
		}		
	}

	private static void injectProperty(SchemaMappingContext context,  String propertyName, GmEntityType gmType, GmEntityType gmParentType) {
		GmProperty gmProperty = gmType.getProperties().stream().filter( p -> { return p.getName().equalsIgnoreCase( propertyName);}).findFirst().orElse(null);
		if (gmProperty != null) {
			if (gmProperty.getType() != gmParentType) {  																			  						
				log.error( "cannot insert backlink property [" + propertyName + "] as [" + gmType.getTypeSignature() + "] already contains such a property, but pointing to [" + gmProperty.getType().getTypeSignature() + "] instead of [" + gmParentType.getTypeSignature() + "]");
			}
			else {
				log.debug( "backlink property [" + propertyName + "] in [" + gmType.getTypeSignature() + "] pointing to [" +  gmParentType.getTypeSignature() + "]already exists");
			}
			return;
		}
		gmProperty = GmProperty.T.create();
		gmProperty.setDeclaringType(gmType);
		gmProperty.setType(gmParentType);
		gmProperty.setName( propertyName);
		
		gmType.getProperties().add(gmProperty);
		
		PropertyMappingMetaData propertyMappingMetaData = context.mappingContext.metaDataMapper.acquireMetaData(gmProperty);				
		propertyMappingMetaData.setIsBacklinkProperty(true);
		
		EntityTypeMappingMetaData typeMetaData = context.mappingContext.metaDataMapper.acquireMetaData( gmType);
		typeMetaData.setBacklinkProperty(gmProperty);
		
		
	}
		

}
