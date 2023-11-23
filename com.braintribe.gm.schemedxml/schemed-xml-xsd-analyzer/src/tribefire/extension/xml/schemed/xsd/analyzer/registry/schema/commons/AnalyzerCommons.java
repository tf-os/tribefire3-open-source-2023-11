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
package tribefire.extension.xml.schemed.xsd.analyzer.registry.schema.commons;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.MetaData;

import tribefire.extension.xml.schemed.mapping.metadata.EntityTypeMappingMetaData;
import tribefire.extension.xml.schemed.mapping.metadata.EnumTypeMappingMetaData;
import tribefire.extension.xml.schemed.mapping.metadata.PropertyMappingMetaData;
import tribefire.extension.xml.schemed.marshaller.commons.QNameExpert;
import tribefire.extension.xml.schemed.model.xsd.Attribute;
import tribefire.extension.xml.schemed.model.xsd.AttributeGroup;
import tribefire.extension.xml.schemed.model.xsd.ComplexType;
import tribefire.extension.xml.schemed.model.xsd.Element;
import tribefire.extension.xml.schemed.model.xsd.Group;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.QName;
import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.model.xsd.SimpleType;
import tribefire.extension.xml.schemed.model.xsd.Type;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.SchemaMappingContext;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.schema.QPath;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.handlers.type.TypeResolverResponse;
import tribefire.extension.xml.schemed.xsd.api.analyzer.SchemaLookupResult;
import tribefire.extension.xml.schemed.xsd.api.analyzer.SchemaRegistry;
import tribefire.extension.xml.schemed.xsd.api.mapper.name.HasTokens;
import tribefire.extension.xml.schemed.xsd.mapper.metadata.BaseTypeInfoMetaData;

/**
 * helper functions  
 * @author pit
 *
 */
public class AnalyzerCommons implements HasTokens{
	
	/*
	 * RETRIEVAL FUNCTIONS 
	 */
	
	/**
	 * ensure a {@link Group}, i.e. by following the {@link Group#getRef()} or be the {@link Group} itself
	 * @param context - the {@link SchemaMappingContext}
	 * @param originalGroup - the {@link Group} to ensure 
	 * @return - the actual {@link Group}
	 */
	public static Group retrieveGroup( SchemaMappingContext context, Group originalGroup) {
		
		QName qname = originalGroup.getRef();
		
		// check if the type is a reference
		if (qname != null) {
			SchemaRegistry backingRegistry = context.analyzerRegistry.getBackingRegistryForSchema( originalGroup.getDeclaringSchema());
			if (backingRegistry == null) {
				throw new IllegalArgumentException( "no backing registry found, cannot scan for type with reference [" + QNameExpert.toString( qname) + "] in [" + context.print() + "]");
			}
			// could be a complex type 
			SchemaLookupResult<Group> result = backingRegistry.lookupGroup(qname);
			if (result != null) {
				return result.getFound();
			}						
			throw new IllegalArgumentException( "type [" + QNameExpert.toString(qname) + "] of element [" + originalGroup.getName() + "] cannot be found ");
		}
		else {
			// direct definition type (complex / simple)
			return originalGroup;				
		}				
	}

	/**
	 * ensure an {@link AttributeGroup}, i.e. either by following the link {@link AttributeGroup#getRef()} or by the {@link AttributeGroup} itself
	 * @param context - {@link SchemaMappingContext}
	 * @param originalAttributeGroup - the {@link AttributeGroup} to ensure
	 * @return - the actual {@link AttributeGroup}
	 */
	public static AttributeGroup retrieveAttributeGroup( SchemaMappingContext context, AttributeGroup originalAttributeGroup) {
		
		
		QName qname = originalAttributeGroup.getRef();
		if (qname == null) {
			return originalAttributeGroup;
		}
		
		SchemaRegistry backingRegistry = context.analyzerRegistry.getBackingRegistryForSchema( originalAttributeGroup.getDeclaringSchema());
		if (backingRegistry == null) {
			throw new IllegalArgumentException( "no backing registry found, cannot scan for type with reference [" + QNameExpert.toString( qname) + "] in [" + context.print() + "]");
		}
		// could be a complex type 
		SchemaLookupResult<AttributeGroup> result = backingRegistry.lookupAttributeGroup(qname);
		if (result == null) {
			throw new IllegalArgumentException( "type [" + QNameExpert.toString(qname) + "] of element [" + originalAttributeGroup.getName() + "] cannot be found ");
		}						
		return result.getFound();								
	}
	/**
	 * ensure an {@link Attribute}, either by following the {@link Attribute#getRef()} or the {@link Attribute} itself
	 * @param context - the {@link SchemaMappingContext}
	 * @param originalAttribute - the {@link Attribute}
	 * @return - the actual {@link Attribute}
	 */
	public static Attribute retrieveAttribute( SchemaMappingContext context, Attribute originalAttribute) {				
		QName qname = originalAttribute.getRef();
		if (qname == null) {
			return originalAttribute;
		}
		
		SchemaRegistry backingRegistry = context.analyzerRegistry.getBackingRegistryForSchema( originalAttribute.getDeclaringSchema());
		if (backingRegistry == null) {
			throw new IllegalArgumentException( "no backing registry found, cannot scan for type with reference [" + QNameExpert.toString( qname) + "] in [" + context.print() + "]");
		}
		// could be a complex type 
		SchemaLookupResult<Attribute> result = backingRegistry.lookupAttribute(qname);
		if (result == null) {
			throw new IllegalArgumentException( "type [" + QNameExpert.toString(qname) + "] of element [" + originalAttribute.getName() + "] cannot be found ");
		}						
		return result.getFound();								
	}
	
	/**
	 * get the real element, i.e. the actual one or the one where the reference holder (the virtual) is pointing to 
	 * @param originalElement - the {@link Element}, real or virtual 
	 * @return - the real {@link Element}
	 */
	public static Element retrieveElement(SchemaMappingContext context, Element originalElement) {
		QName reference = originalElement.getElementReference();
		if (reference == null) {
			return originalElement;
		}
		SchemaRegistry backingRegistry = context.analyzerRegistry.getBackingRegistryForSchema( originalElement.getDeclaringSchema());
		if (backingRegistry == null) {
			throw new IllegalArgumentException( "no backing registry element, cannot scan for[" + QNameExpert.toString(reference) + "] in [" + context.print() + "]");
		}
		SchemaLookupResult<Element> retrieved = backingRegistry.lookupElement( reference);
		if (retrieved == null) {
			throw new IllegalArgumentException( "no element found for reference [" + QNameExpert.toString(reference) + "] in [" + context.print() + "]");
		}
		return retrieveElement( context, retrieved.getFound());
	}
	
	/**
	 * @param context
	 * @param attribute
	 * @return
	 */
	public static Type retrieveAttributeType( SchemaMappingContext context, Attribute attribute) {
		Type type = null;
		QName qname = attribute.getTypeReference();
		
		SchemaRegistry backingRegistry = context.analyzerRegistry.getBackingRegistryForSchema( attribute.getDeclaringSchema());
		if (backingRegistry == null) {
			throw new IllegalArgumentException( "no backing registry found, cannot scan for type with reference [" + QNameExpert.toString( qname) + "] in [" + context.print() + "]");
		}
		// check if the type is a reference
		if (qname != null) {
			// could be a simple type 
			SchemaLookupResult<SimpleType> simpleTypeResult = backingRegistry.lookupSimpleType(qname);
			if (simpleTypeResult != null) {
				return simpleTypeResult.getFound();
			}			
			// could not be a complex type
			
			SchemaLookupResult<ComplexType> complexTypeResult = backingRegistry.lookupComplexType(qname);
			if (complexTypeResult != null) {
				return complexTypeResult.getFound();
			}
			
			throw new IllegalArgumentException( "type [" + QNameExpert.toString(qname) + "] of attribute [" + attribute.getName() + "] cannot be found ");
		}
		else {
			// direct definition type (complex / simple)
			type = attribute.getType();
			if (type != null) {
				return type;
			}
			throw new IllegalArgumentException( "attribute [" + attribute.getName() + "] has no type reference nor a type itself ");	
		}				
	}
	
	/**
	 * get the {@link Type} of the {@link Element}, 
	 * @param context
	 * @param element
	 * @return
	 */
	public static Type retrieveElementType( SchemaMappingContext context, Element element) {
		Type type = null;
		QName qname = element.getTypeReference();
		
		SchemaRegistry backingRegistry = context.analyzerRegistry.getBackingRegistryForSchema( element.getDeclaringSchema());
		if (backingRegistry == null) {
			throw new IllegalArgumentException( "no backing registry found, cannot scan for type with reference [" + QNameExpert.toString( qname) + "] in [" + context.print() + "]");
		}
		// check if the type is a reference
		if (qname != null) {
			// could be a complex type 
			SchemaLookupResult<ComplexType> complexTypeResult = backingRegistry.lookupComplexType(qname);
			if (complexTypeResult != null) {
				return complexTypeResult.getFound();
			}
			// could be a simple type 
			SchemaLookupResult<SimpleType> simpleTypeResult = backingRegistry.lookupSimpleType(qname);
			if (simpleTypeResult != null) {
				return simpleTypeResult.getFound();
			}			
			throw new IllegalArgumentException( "type [" + QNameExpert.toString(qname) + "] of element [" + element.getName() + "] cannot be found ");
		}
		else {
			// direct definition type (complex / simple)
			type = element.getType();
			if (type != null) {
				return type;
			}
			throw new IllegalArgumentException( "element [" + element.getName() + "] has no type reference nor a type itself ");	
		}				
	}
	
	/**
	 * retrieves a type as it is referenced within a {@link Schema}
	 * @param context - the {@link SchemaMappingContext}
	 * @param declaringSchema - the {@link Schema} 
	 * @param reference - the {@link QName} that references the type 
	 * @return
	 */
	public static Type retrieveType( SchemaMappingContext context, Schema declaringSchema, QName reference) {
		SchemaRegistry backingRegistry = context.analyzerRegistry.getBackingRegistryForSchema( declaringSchema);
		if (backingRegistry == null) {
			throw new IllegalArgumentException( "no backing registry found, cannot scan for type with reference [" + QNameExpert.toString( reference) + "] in [" + context.print() + "]");
		}			
		// check if the type is a reference
		// could be a complex type 
		SchemaLookupResult<ComplexType> complexTypeResult = backingRegistry.lookupComplexType(reference);
		if (complexTypeResult != null) {
			return complexTypeResult.getFound();
		}
		// could be a simple type 
		SchemaLookupResult<SimpleType> simpleTypeResult = backingRegistry.lookupSimpleType(reference);
		if (simpleTypeResult != null) {
			return simpleTypeResult.getFound();
		}							
		throw new IllegalArgumentException( "type [" + QNameExpert.toString(reference) + "] cannot be found ");
	}

	
	/**
	 * creates {@link GmEntityType} out of a {@link SimpleType} instead of a {@link GmSimpleType}, required for restriction/extensions 
	 * @param context - the {@link SchemaMappingContext}
	 * @param declaringSchema 
	 * @param typeToMap - the {@link SimpleType} as referenced in the {@link Schema}
	 * @param name - the name of the type 
	 * @param baseType - the {@link GmType} which reflects the {@link SimpleType} 
	 * @param baseName - the {@link QName} which defines the type 
	 * @return - the {@link TypeExpertResponse} 
	 */
	public static TypeResolverResponse buildEntityTypeOutofSimpleType(SchemaMappingContext context, Schema declaringSchema, SimpleType typeToMap, String name, GmType baseType, QName baseName) {
		TypeResolverResponse response = new TypeResolverResponse();
		String typeName = context.mappingContext.nameMapper.generateJavaCompatibleTypeName( name);
		typeName = AnalyzerCommons.assertNonCollidingTypeName( context, typeName);
		QPath qpath = context.qpathGenerator.generateQPathForSchemaEntity( declaringSchema);
		GmEntityType gmEntityType = context.mappingContext.typeMapper.generateGmEntityType( qpath, typeToMap, typeName);
		
		EntityTypeMappingMetaData metadata = context.mappingContext.metaDataMapper.acquireMetaData(gmEntityType);
		metadata.setXsdName(name);
		metadata.setIsSimple(true);
		
		Namespace targetNamespace = declaringSchema.getTargetNamespace();
		if (targetNamespace != null) {
			metadata.setNamespace(targetNamespace.getUri());
		}
		
		response.setGmType( gmEntityType);
		response.setApparentTypeName( context.registry.generateQName( declaringSchema, name));
		response.setActualTypeName(baseName);
		GmType base = baseType;
		if (base instanceof GmEntityType == false) {			
			// attach basetype info to generated type as metadata 
			BaseTypeInfoMetaData infoMetaData = BaseTypeInfoMetaData.T.create();
			infoMetaData.setBaseType( QNameExpert.toString(response.getActualTypeName()));
			gmEntityType.getMetaData().add(infoMetaData);
			// if the type is not a GmEntityType, we must create a property, named value  			
			GmProperty property = context.mappingContext.typeMapper.generateGmProperty( VIRTUAL_VALUE_PROPERTY);
			property.setType(base);
			property.setDeclaringType(gmEntityType);
			gmEntityType.getProperties().add(property);
			PropertyMappingMetaData propertyMetaData = context.mappingContext.metaDataMapper.acquireMetaData(property);
			propertyMetaData.setIsValue(true);			
			propertyMetaData.setApparentXsdType( QNameExpert.toString(response.getActualTypeName()));
		}
		else {
			
			GmEntityType baseEntityType = (GmEntityType) base;
			for (MetaData metaData : baseEntityType.getMetaData()) {
				if (metaData instanceof BaseTypeInfoMetaData) {
					BaseTypeInfoMetaData baseTypeInfoMetaData = (BaseTypeInfoMetaData) metaData;
					response.setActualTypeName( baseTypeInfoMetaData.getBaseType());
					break;
				}
			}
			gmEntityType.getSuperTypes().add( baseEntityType);				
		}
		return response;
	}
	
	public static TypeResolverResponse buildEntityTypeOutofEnumType(SchemaMappingContext context, Schema declaringSchema, Type typeToMap, String name, GmType baseType, QName baseName) {
		TypeResolverResponse response = new TypeResolverResponse();
		String typeName = context.mappingContext.nameMapper.generateJavaCompatibleTypeName( name);
		typeName = AnalyzerCommons.assertNonCollidingTypeName( context, typeName);
		QPath qpath = context.qpathGenerator.generateQPathForSchemaEntity( declaringSchema);
		GmEntityType gmEntityType = context.mappingContext.typeMapper.generateGmEntityType( qpath, typeToMap, typeName);
		EntityTypeMappingMetaData metadata = context.mappingContext.metaDataMapper.acquireMetaData(gmEntityType);
		metadata.setXsdName(name);
		metadata.setIsSimple(true);
		Namespace targetNamespace = declaringSchema.getTargetNamespace();
		if (targetNamespace != null) {
			metadata.setNamespace( targetNamespace.getUri());
		}
		
		response.setGmType( gmEntityType);
		response.setApparentTypeName( context.registry.generateQName( declaringSchema, name));
			
		response.setActualTypeName( baseName);

		
		// if the type is not a GmEntityType, we must create a property, named value  			
		GmProperty property = context.mappingContext.typeMapper.generateGmProperty( VIRTUAL_VALUE_PROPERTY);
		property.setType(baseType);
		property.setDeclaringType(gmEntityType);
		gmEntityType.getProperties().add(property);
		PropertyMappingMetaData propertyMetaData = context.mappingContext.metaDataMapper.acquireMetaData(property);
		propertyMetaData.setIsValue(true);
		
		propertyMetaData.setApparentXsdType( QNameExpert.toString(baseName));

		return response;
	}
	
	/*
	 * PROCESSING STUFF 
	 */
	
	

	public static QName retrieveActualSimpleTypeName(SchemaMappingContext context, GmType alreadyAcquired) {
		
		if (alreadyAcquired instanceof GmEntityType) {
			EntityTypeMappingMetaData entityTypeMappingMetaData = context.mappingContext.metaDataMapper.acquireMetaData( (GmEntityType) alreadyAcquired);
			return QNameExpert.parse( entityTypeMappingMetaData.getXsdName());			
		}
		else if (alreadyAcquired instanceof GmEnumType) {
			EnumTypeMappingMetaData enumTypeMappingMetaData = context.mappingContext.metaDataMapper.acquireMetaData( (GmEnumType) alreadyAcquired);
			return QNameExpert.parse( enumTypeMappingMetaData.getXsdName());
		}
		else {		
			throw new IllegalStateException("the mapped type of [" + alreadyAcquired.getTypeSignature() + "] must be an GmEntitytype or a GmEnumType");
		}		
	}

	public static String assertNonCollidingTypeName( SchemaMappingContext context, String proposal) {
		return context.mappingContext.typeMapper.assertNonCollidingTypeName(proposal);
	}
	

}
