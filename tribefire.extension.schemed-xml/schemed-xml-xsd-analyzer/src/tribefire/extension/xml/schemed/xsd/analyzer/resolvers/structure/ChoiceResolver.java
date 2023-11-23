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

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.MetaData;

import tribefire.extension.xml.schemed.model.xsd.Choice;
import tribefire.extension.xml.schemed.model.xsd.ComplexType;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.MappingContext;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.MappingMode;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.SchemaMappingContext;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.schema.QPath;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.ResolverCommons;

/**
 * a resolver for the {@link Choice}.<br/> 
 * depending on the settings {@link SchemaMappingContext} -> {@link MappingContext} -> choiceMappingMode {@link MappingMode}  (or the choice is multiple or not)
 * a choice will either generate group of types ( the virtual {@link GmProperty} has a virtual type (and a unique name), and all other properties (depending how they
 * are introduced) are attached to deriving types, or it will generate a simple list of {@link GmProperty} (which need to have {@link MetaData} that introduce
 * the XOR logic)
 * if called by a {@link ComplexType}'s expert, the virtual type may be remapped into the actual type, but this is not decided here. 
 * @author pit
 *
 */
public class ChoiceResolver {
	//private enum VirtualTypeKind {sequence, choice, group, element};
	
	/**
	 * @param context
	 * @param choice
	 * @return
	 */
	public static List<GmProperty> resolve(SchemaMappingContext context, Choice choice, boolean overrideMultiple) {
		
		context.currentEntityStack.push( choice);
		
		try {
			int maxOccurs = choice.getMaxOccurs();
			boolean multiple = (maxOccurs < 0 || maxOccurs > 1);
			
			GmEntityType polymorphicSuperType = null;		
			QPath qpath = context.qpathGenerator.generateQPathForSchemaEntity(choice.getDeclaringSchema());
			
			
			List<GmProperty> properties = new ArrayList<>();
																
			// properties from sequence 
			List<GmProperty> propertiesFromSequence = ResolverCommons.processSequences(context, choice, multiple || overrideMultiple);
			// properties from elements
			List<GmProperty> propertiesFromElements = ResolverCommons.processElements(context, choice, multiple || overrideMultiple);
			// properties from groups
			List<GmProperty> propertiesFromGroups = ResolverCommons.processGroups(context, choice, multiple || overrideMultiple);
			// properties from choices 
			List<GmProperty> propertiesFromChoices = ResolverCommons.processChoices(context, choice, multiple || overrideMultiple);
			
			
			// sequences
			if (!propertiesFromSequence.isEmpty()) {			
					properties.addAll( propertiesFromSequence);				
			}
			// elements
			if (!propertiesFromElements.isEmpty()) {				
				properties.addAll( propertiesFromElements);				
			}
			// groups 
			if (!propertiesFromGroups.isEmpty()) {				
				properties.addAll( propertiesFromGroups);				
			}
			// choices
			if (!propertiesFromChoices.isEmpty()) {				
				properties.addAll( propertiesFromChoices);				
			}
			
			return properties;
		}
		finally {
			context.currentEntityStack.pop();
		}		
	}
	
	/**
	 * @param context
	 * @param choice
	 * @return
	 */
	/*
	public static List<GmProperty> resolveX(SchemaMappingContext context, Choice choice) {
		
		context.currentEntityStack.push( choice);
		
		try {
			int maxOccurs = choice.getMaxOccurs();
			boolean multiple = (maxOccurs < 0 || maxOccurs > 1);
			
			GmEntityType polymorphicSuperType = null;		
			QPath qpath = context.qpathGenerator.generateQPathForSchemaEntity(choice.getDeclaringSchema());
			
			
			List<GmProperty> properties = new ArrayList<>();
						
						
			// if multiple or structured mode, any element/sequence/group/choice is a derived type of the main polymorphic type
		
			if (multiple || context.mappingContext.choiceMappingMode == MappingMode.structured) {
			
				String name = context.mappingContext.nameMapper.generateJavaCompatibleTypeNameForVirtualChoiceType( null);
				name = AnalyzerCommons.assertNonCollidingTypeName( context, name);
				String propertyName = context.mappingContext.nameMapper.generateJavaCompatiblePropertyName( name);				
				polymorphicSuperType = context.mappingContext.typeMapper.generateGmEntityType(qpath, null, name);
				
				EntityTypeMappingMetaData typeMappingMetaData = context.mappingContext.metaDataMapper.acquireMetaData(polymorphicSuperType);
				typeMappingMetaData.setIsVirtual(true);				
				
				if (!multiple)  {
					GmProperty property = context.mappingContext.typeMapper.generateGmProperty(propertyName);
					PropertyMappingMetaData propertyMappingMetaData = context.mappingContext.metaDataMapper.acquireMetaData(property);
					propertyMappingMetaData.setIsVirtual(true);					
					property.setType(polymorphicSuperType);
					properties.add(property);					
					
				}
				else {
					String collectionPropertyName = context.mappingContext.nameMapper.generateCollectionName(propertyName);
					GmProperty property = context.mappingContext.typeMapper.generateGmProperty( collectionPropertyName);
					PropertyMappingMetaData propertyMappingMetaData = context.mappingContext.metaDataMapper.acquireMetaData(property);
					propertyMappingMetaData.setIsVirtual(true);
					propertyMappingMetaData.setIsMultiple(true);
					propertyMappingMetaData.setElementType(polymorphicSuperType);
					// TODO : possible override as GmSetType  
					GmCollectionType gmCollectionType = context.mappingContext.typeMapper.acquireCollectionType( polymorphicSuperType);
					property.setType(gmCollectionType);
					properties.add(property);
				}
				
			}
		
			
			// properties from sequence 
			List<GmProperty> propertiesFromSequence = ResolverCommons.processSequences(context, choice);
			// properties from elements
			List<GmProperty> propertiesFromElements = ResolverCommons.processElements(context, choice);
			// properties from groups
			List<GmProperty> propertiesFromGroups = ResolverCommons.processGroups(context, choice);
			// properties from choices 
			List<GmProperty> propertiesFromChoices = ResolverCommons.processChoices(context, choice);
			
			
			// sequences
			if (!propertiesFromSequence.isEmpty()) {
				if (polymorphicSuperType != null) {
					boolean propagating = false;
					// if a single type's returned, it might a type that we can propagate (to avoid to create a new sub-type)
					if (propertiesFromSequence.size() == 1 ) {
						GmProperty propertyToPropagate = propertiesFromSequence.get(0);
						GmType declaringType = propertyToPropagate.getType();
						// must be a GmEntitytype, as SimpleTypes cannot have a GmEntityType super type
						if (declaringType instanceof GmEntityType) {
							((GmEntityType)declaringType).getSuperTypes().add(polymorphicSuperType);
							propagating = true;
						}
					}
					// not propagating, create your owm type 
					if (propagating == false) {
						String name = context.mappingContext.nameMapper.generateJavaCompatibleTypeNameForVirtualChoiceSequenceType();
						GmEntityType virtualElementType = context.mappingContext.typeMapper.generateGmEntityType(qpath, null, name);
						EntityTypeMappingMetaData virtualEntityTypeMetaData = context.mappingContext.metaDataMapper.acquireMetaData(virtualElementType);
						virtualEntityTypeMetaData.setIsVirtual(true);
						virtualElementType.getSuperTypes().add(polymorphicSuperType);
						for (GmProperty property : propertiesFromSequence) {
							virtualElementType.getProperties().add(property);
							property.setDeclaringType(virtualElementType);
						}					 		
					}
				}
				else {
					properties.addAll( propertiesFromSequence);
				}
			}
			// elements
			if (!propertiesFromElements.isEmpty()) {
				if (polymorphicSuperType != null) {
					for (GmProperty property : propertiesFromElements) {
						String name = context.mappingContext.nameMapper.generateJavaCompatibleTypeNameForVirtualChoiceElementType( property.getName());
						GmEntityType virtualElementType = context.mappingContext.typeMapper.generateGmEntityType(qpath, null, name);
						EntityTypeMappingMetaData virtualEntityTypeMetaData = context.mappingContext.metaDataMapper.acquireMetaData(virtualElementType);
						virtualEntityTypeMetaData.setIsVirtual(true);
						virtualElementType.getSuperTypes().add(polymorphicSuperType);
						virtualElementType.getProperties().add(property);
						property.setDeclaringType(virtualElementType);
					}					 				
				}
				else {
					properties.addAll( propertiesFromElements);
				}
			}
			// groups 
			if (!propertiesFromGroups.isEmpty()) {
				if (polymorphicSuperType != null) {
					String name = context.mappingContext.nameMapper.generateJavaCompatibleTypeNameForVirtualChoiceGroupType();
					GmEntityType virtualElementType = context.mappingContext.typeMapper.generateGmEntityType(qpath, null, name);
					virtualElementType.getSuperTypes().add(polymorphicSuperType);
					EntityTypeMappingMetaData virtualEntityTypeMetaData = context.mappingContext.metaDataMapper.acquireMetaData(virtualElementType);
					virtualEntityTypeMetaData.setIsVirtual(true);
					for (GmProperty property : propertiesFromGroups) {
						virtualElementType.getProperties().add(property);
						property.setDeclaringType(virtualElementType);
					}					 				
				}
				else {
					properties.addAll( propertiesFromGroups);
				}
			}
			// choices
			if (!propertiesFromChoices.isEmpty()) {
				if (polymorphicSuperType != null) {
					boolean propagating = false;
					// if a single type's returned, it might a type that we can propagate (to avoid to create a new sub-type)
					if (propertiesFromChoices.size() == 1 ) {
						GmProperty propertyToPropagate = propertiesFromChoices.get(0);
						GmType declaringType = propertyToPropagate.getType();
						// must be a GmEntitytype, as SimpleTypes cannot have a GmEntityType super type
						if (declaringType instanceof GmEntityType) {
							((GmEntityType)declaringType).getSuperTypes().add(polymorphicSuperType);
							propagating = true;
						}
					}
					if (propagating == false) {
						String name = context.mappingContext.nameMapper.generateJavaCompatibleTypeNameForVirtualChoiceChoiceType();
						GmEntityType virtualElementType = context.mappingContext.typeMapper.generateGmEntityType(qpath, null, name);
						EntityTypeMappingMetaData virtualEntityTypeMetaData = context.mappingContext.metaDataMapper.acquireMetaData(virtualElementType);
						virtualEntityTypeMetaData.setIsVirtual(true);
						virtualElementType.getSuperTypes().add(polymorphicSuperType);
						for (GmProperty property : propertiesFromChoices) {
							virtualElementType.getProperties().add(property);
							property.setDeclaringType(virtualElementType);
						}					 		
					}
				}
				else {
					properties.addAll( propertiesFromChoices);
				}
			}
			
			return properties;
		}
		finally {
			context.currentEntityStack.pop();
		}		
	}
	*/
	/*
	private void handleSubTypeCreation( SchemaMappingContext context, QPath qpath, GmEntityType polymorphicSuperType, List<GmProperty> properties, VirtualTypeKind kind) {
		boolean propagating = false;
		// if a single type's returned, it might a type that we can propagate (to avoid to create a new subtype)
		if (properties.size() == 1 ) {
			GmProperty propertyToPropagate = properties.get(0);
			GmType declaringType = propertyToPropagate.getType();
			// must be a GmEntitytype, as SimpleTypes cannot have a GmEntityType super type
			if (declaringType instanceof GmEntityType) {
				((GmEntityType)declaringType).getSuperTypes().add(polymorphicSuperType);
				propagating = true;
			}
		}
		// not propagating, create your owm type 
		if (propagating == false) {
			String name = null;
			switch (kind) {
				case choice:
					name = context.mappingContext.nameMapper.generateJavaCompatibleTypeNameForVirtualChoiceChoiceType();
					break;
				case element:
					break;
				case group:
					name = context.mappingContext.nameMapper.generateJavaCompatibleTypeNameForVirtualChoiceGroupType();
					break;
				case sequence:
					name = context.mappingContext.nameMapper.generateJavaCompatibleTypeNameForVirtualChoiceSequenceType();
					break;
				default:
					break;
			
			}
			GmEntityType virtualElementType = context.mappingContext.typeMapper.generateGmEntityType(qpath, null, name);
			virtualElementType.getSuperTypes().add(polymorphicSuperType);
			for (GmProperty property : properties) {
				virtualElementType.getProperties().add(property);
				property.setDeclaringType(virtualElementType);
			}					 		
		}
	}
	*/
		
}
