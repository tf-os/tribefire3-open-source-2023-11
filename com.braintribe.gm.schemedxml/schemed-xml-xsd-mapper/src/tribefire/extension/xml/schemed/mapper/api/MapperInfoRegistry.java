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
package tribefire.extension.xml.schemed.mapper.api;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;

import tribefire.extension.xml.schemed.mapper.structure.MapperInfoForEntitytype;
import tribefire.extension.xml.schemed.mapper.structure.MapperInfoForEnumtype;
import tribefire.extension.xml.schemed.mapper.structure.MapperInfoForProperty;
import tribefire.extension.xml.schemed.mapper.structure.MapperInfoForType;
import tribefire.extension.xml.schemed.mapping.metadata.EntityTypeMappingMetaData;
import tribefire.extension.xml.schemed.mapping.metadata.EnumConstantMappingMetaData;
import tribefire.extension.xml.schemed.mapping.metadata.EnumTypeMappingMetaData;
import tribefire.extension.xml.schemed.mapping.metadata.ModelMappingMetaData;
import tribefire.extension.xml.schemed.mapping.metadata.Namespace;
import tribefire.extension.xml.schemed.mapping.metadata.PropertyMappingMetaData;
import tribefire.extension.xml.schemed.model.xsd.QName;

public interface MapperInfoRegistry {
	
	/**
	 * returns the type signature of a named type, both for enum or entity type
	 * @param domName - the full name (actually a QName)
	 * @param namespaceMap - 
	 * @return
	 */
	String getMappedTypeSignature( QName name, Map<String,String> namespaceMap);

	/**
	 * return the XML (XSD) name of the given {@link GmType}
	 * @param type - the {@link GmType}
	 * @return - its name in the XSD/XML world
	 */
	String getXmlNameForType(GmType type);

	/**
	 * get the {@link GmEntityType} with the given type signature
	 * @param signature - the type signature
	 * @return - the {@link GmEntityType} if any 
	 */
	GmEntityType getMatchingEntityType(String signature);

	/**
	 * get the {@link GmEnumType with the given type signature
	 * @param signature - the type signature 
	 * @return - the {@link GmEnumType} if any 
	 */
	GmEnumType getMatchingEnumType(String signature);

	/**
	 * get the type of the property of given type and name  
	 * @param signature - the type signature of the declaring {@link GmEntityType}
	 * @param tuple - the {@link QName} qualifying the {@link GmProperty}
	 * @return - the property type of the {@link GmProperty}
	 */
	String getTypeSignatureOfPropertyType(String signature, QName tuple, boolean attribute);
	
	/**
	 * get the element type of the multiple-style property of given type and name  
	 * @param signature - the type signature of the declaring {@link GmEntityType}
	 * @param tuple - the {@link QName} qualifying the {@link GmProperty}
	 * @return - the property type of the {@link GmProperty}
	 */
	String getTypeSignatureOfPropertyElementType(String signature, QName tuple);

	/**
	 * get the type signature of the main type's element's name
	 * @param name - the name of the toplevel elements 
	 * @return - the type signature of the corresponding type if any 
	 */
	String getMappedMainTypeSignature(String name);

	/**
	 * returns the name of the top level elements that stands in for the type 
	 * @param signature - the type signature of the {@link GmEntityType}
	 * @return - the name of the toplevel element
	 */
	String getMappedProtoNameForMainTypeSignature(String signature);

	/**
	 * return the attached {@link ModelMappingMetaData}
	 * @return
	 */
	ModelMappingMetaData getModelMappingMetaData();

	/**
	 * return the attached {@link EntityTypeMappingMetaData} of the passed {@link GmEntityType}
	 * @param entityType - the {@link GmEntityType}
	 * @return - the associated {@link EntityTypeMappingMetaData} if any 
	 */
	EntityTypeMappingMetaData getEntityTypeMetaData(GmEntityType entityType);

	/**
	 * return the attached {@link EnumTypeMappingMetaData} of the passed {@link GmEnumType}
	 * @param enumtype - the {@link GmEnumType}
	 * @return - the associated {@link EnumTypeMappingMetaData} if any
	 */
	EnumTypeMappingMetaData getEnumTypeMetaData(GmEnumType enumtype);

	/**
	 * return the attached {@link EnumConstantMappingMetaData} of the passed {@link GmEnumConstant}
	 * @param constant - the {@link GmEnumConstant}
	 * @return - {@link EnumConstantMappingMetaData} associated if any 
	 */
	EnumConstantMappingMetaData getEnumConstantMetaData(GmEnumConstant constant);

	/**
	 * return the {@link PropertyMappingMetaData} of the given {@link GmProperty}
	 * @param property - the {@link GmProperty}
	 * @return - the associated {@link PropertyMappingMetaData} if any 
	 */
	PropertyMappingMetaData getPropertyMappingMetaData(GmProperty property);

	/**
	 * return the {@link PropertyMappingMetaData} of the property 
	 * @param type - the declaring {@link GmEntityType}
	 * @param propertyName - the name of the property 
	 * @return - the {@link PropertyMappingMetaData} associated if any
	 */
	PropertyMappingMetaData getPropertyMappingMetaData(GmEntityType type, String propertyName);
	
	/**
	 * return the {@link PropertyMappingMetaData} of the property
	 * @param type - the declaring type {@link GmEntityType}
	 * @param qName - the {@link javax.xml.namespace.QName} as it's named in the XML 
	 * @param attribute - true if it's an attribute, false if it's an element
	 * @return - the {@link PropertyMappingMetaData} associated if any
	 */
	PropertyMappingMetaData getPropertyMappingMetaData(GmEntityType type, javax.xml.namespace.QName qName, boolean attribute);

	/**
	 * get all {@link MapperInfoForProperty} of a {@link GmEntityType}
	 * @param type - the {@link GmEntityType}
	 * @return - a {@link Collection} of all {@link MapperInfoForProperty} attached 
	 */
	Collection<MapperInfoForProperty> getPropertyInfosForType(GmEntityType type);

	/**
	 * get the single {@link MapperInfoForProperty} of a property 
	 * @param type - the declaring {@link GmEntityType}
	 * @param tuple - the {@link QName} that qualifies the property 
	 * @return - the associated {@link MapperInfoForProperty}
	 */
	MapperInfoForProperty getPropertyInfo(GmEntityType type, QName tuple, boolean attribute);

	/**
	 * get the matching {@link MapperInfoForType}
	 * @param type - the {@link GmType}
	 * @return - autocast to either {@link MapperInfoForEntitytype} or {@link MapperInfoForEnumtype}
	 */
	<T extends MapperInfoForType> T getTypeInfo(GmType type);

	/** 
	 * get get the {@link MapperInfoForProperty} assocated with a simple content {@link GmEntityType}
	 * @param entityType - the {@link GmEntityType}
	 * @return - the associated {@link MapperInfoForProperty}
	 */
	MapperInfoForProperty getSimpleContentPropertyInfoForType(GmEntityType entityType);

	/**
	 * get the prefix of the give namespace 
	 * @param namespace - the namespace 
	 * @return - the prefix 
	 */
	String getPrefixOfNamespace(String namespace);

	/**
	 * get the namespace associated with the prefix 
	 * @param prefix - the prefix 
	 * @return - the associated namespace
	 */
	String getNamespaceOfPrefix(String prefix);
	
	/**
	 * returns all target names spaces of the XSD context
	 * @return
	 */	
	Map<String,Namespace> getTargetNamespaces();
	
	boolean getElementQualification( String uri);
	boolean getAttributeQualification( String uri);

	/**
	 * returns whether the {@link GmEntityType} has relevant properties (any properties not attributes) 
	 * @param entitytype  - the {@link GmEntityType}
	 * @return
	 */
	boolean hasRelevantProperties(GmEntityType entitytype);
	
	Set<String> getUsedNamespaces();

}