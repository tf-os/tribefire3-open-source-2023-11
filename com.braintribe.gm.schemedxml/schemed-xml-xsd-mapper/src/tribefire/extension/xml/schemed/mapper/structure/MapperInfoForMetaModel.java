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
package tribefire.extension.xml.schemed.mapper.structure;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;

import tribefire.extension.xml.schemed.mapping.metadata.ModelMappingMetaData;
import tribefire.extension.xml.schemed.mapping.metadata.Namespace;
import tribefire.extension.xml.schemed.model.xsd.QName;

/**
 * the main data structure for the meta model itself 
 * @author pit
 *
 */
public class MapperInfoForMetaModel {
	// type to MapperInfoForType (GmEntityType and GmEnumType) 
	private Map<GmType, MapperInfoForType> typeToInfoMap = new HashMap<GmType, MapperInfoForType>();
	// main container (top level elements) map 
	private Map<String, MapperInfoForType> containerToTypeInfoMap = new HashMap<String, MapperInfoForType>();
	// top level types to element names 
	private Map<GmType, String> typeToContainerMap = new HashMap<GmType, String>();
	// type signature to type map 
	private Map<String, GmType> signatureToTypeMap = new HashMap<String, GmType>();
	// prefix to namespace map 
	private Map<String, String> prefixToNamespaceMap = new HashMap<String, String>();
	// namespace to prefix map
	private Map<String, String> namespaceToPrefixMap = new HashMap<String, String>();
	// metadata it was setup with  
	private ModelMappingMetaData metaData;
	private Map<String, Namespace> namespaces;
	

	/**
	 * returns the {@link ModelMappingMetaData} it was setup with
	 * @return
	 */
	public ModelMappingMetaData getMetaData() {
		return metaData;
	}

	/**
	 * adds (and analyzes the {@link ModelMappingMetaData} passed
	 * @param metaData - the {@link ModelMappingMetaData}
	 */
	public void setMetaData(ModelMappingMetaData metaData) {
		this.metaData = metaData;		
		
		namespaces = metaData.getNamespaces();
		int i = 1;
		for (Namespace namespace : namespaces.values()) {
			String prefix = namespace.getPrefix();
			if (prefix == null) {
				prefix = "ns" + i++;
			}
			String uri = namespace.getUri();
			prefixToNamespaceMap.put(prefix, uri);
			namespaceToPrefixMap.put(uri, prefix);
		}
				
	}
	
	/**
	 * adds an information about a {@link GmEntityType}
	 * @param typeInfo - {@link MapperInfoForEntitytype}
	 */
	public void addTypeInfo( MapperInfoForEntitytype typeInfo){
		GmEntityType type = typeInfo.getType();
		typeToInfoMap.put( type, typeInfo);
		signatureToTypeMap.put( type.getTypeSignature(), type);
	}
	
	/**
	 * adds an information about a {@link GmEnumType}
	 * @param typeInfo - the {@link MapperInfoForEnumtype}
	 */
	public void addTypeInfo( MapperInfoForEnumtype typeInfo){
		GmEnumType type = typeInfo.getType();
		typeToInfoMap.put( type, typeInfo);
		signatureToTypeMap.put( type.getTypeSignature(), type);
	}
	
	/**
	 * sets up the container info, i.e. the top level elements (and their corresponding type)
	 */
	public void setupContainerMap() {
		Map<String, GmType> mainTypeMap = metaData.getMainTypeMap();
		for (Entry<String, GmType> entry : mainTypeMap.entrySet()) {
			MapperInfoForType infoForType = typeToInfoMap.get( entry.getValue());
			containerToTypeInfoMap.put( entry.getKey(), infoForType);
			typeToContainerMap.put( entry.getValue(), entry.getKey());
		}
	}
	
	/**
	 * returns the XSD name of a {@link GmType}
	 * @param type - the {@link GmType}
	 * @return - the names is it was in XSD
	 */
	public String getXmlNameForType( GmType type) {
		MapperInfoForType mapperInfo = typeToInfoMap.get(type);
		if (mapperInfo == null)
			return null;
		if (mapperInfo instanceof MapperInfoForEntitytype) {
			return ((MapperInfoForEntitytype) mapperInfo).getMetaData().getXsdName();
		}
		else {
			return ((MapperInfoForEnumtype) mapperInfo).getMetaData().getXsdName();
		}
	}

	/**
	 * returns the type signature of a property of the given type with the given name 
	 * @param type - the {@link GmEntityType} that contains the property 
	 * @param tuple - the {@link QName} that names the property 
	 * @return
	 */
	public String getTypeSignatureOfPropertyType(GmEntityType type, QName tuple, boolean attribute) {		
		MapperInfoForEntitytype typeInfo = (MapperInfoForEntitytype) typeToInfoMap.get(type);
		MapperInfoForProperty propertyInfo = typeInfo.getInfoForProperty(tuple);
		if (propertyInfo == null) {
				return null;			
		}
		return propertyInfo.getMetaData().getProperty().getType().getTypeSignature();
	}
	/**
	 * returns the type signature of the element type of a multiple-style property of the given type with the give name
	 * @param type - the {@link GmEntityType}
	 * @param tuple - the {@link QName} that identifies the property by its name
	 * @return
	 */
	public String getTypeSignatureOfPropertyElementType(GmEntityType type, QName tuple) {
		MapperInfoForEntitytype info = (MapperInfoForEntitytype) typeToInfoMap.get(type);		
		MapperInfoForProperty infoForProperty = info.getInfoForProperty( tuple);
		if (infoForProperty == null)
			return null;
		
		return infoForProperty.getMetaData().getElementType().getTypeSignature();
	}
	
	/**
	 * returns the type signature of the type that is behind the top level element
	 * @param name - the name of the top level Element
	 * @return - the type signature of the type 
	 */
	public String getMappedMainTypeSignature( String name){
		MapperInfoForType info = containerToTypeInfoMap.get(name);
		if (info == null)
			return null;
		return info.getType().getTypeSignature();
	}
	/**
	 * returns the name of the element that corresponds to the top level type given 
	 * @param signature - the type signature of the top level type 
	 * @return - the element's name 
	 */
	public String getMappedXmlNameForMainTypeSignature( String signature) {
		GmType type = signatureToTypeMap.get( signature);
		return typeToContainerMap.get( type);		
	}
	
	/**
	 * returns either the {@link MapperInfoForEntitytype} or {@link MapperInfoForEnumtype} of the {@link GmType} passed
	 * @param type - the {@link GmType}, either a {@link GmEntityType} or a {@link GmEnumType}
	 * @return - T (if you get it wrong, it will complain)
	 */
	@SuppressWarnings("unchecked")
	public <T extends MapperInfoForType> T getTypeInfo( GmType type) {
		MapperInfoForType info = typeToInfoMap.get( type);
		if (info == null)
			return null;				
		return (T) info;		
	}
	
	/**
	 * returns all {@link MapperInfoForProperty} of the properties of a {@link GmEntityType}
	 * @param type - the {@link GmEntityType} to enumerate the properties 
	 * @return - a {@link Collection} of {@link MapperInfoForProperty}
	 */
	public Collection<MapperInfoForProperty> getPropertyInfosForType( GmEntityType type) {
		MapperInfoForEntitytype info = getTypeInfo(type);
		if (info == null)
			return null;
		return info.getInfoForProperties();
	}
			
	/**
	 * returns the {@link MapperInfoForProperty} of a property of the given {@link GmEntityType} of the given name
	 * @param type - the {@link GmEntityType}
	 * @param tuple - the {@link QName} describing its name 
	 * @return - the {@link MapperInfoForProperty} if any
	 */
	public MapperInfoForProperty getPropertyInfo( GmEntityType type, QName tuple, boolean attribute) {
		MapperInfoForEntitytype info = getTypeInfo(type);
		if (info == null)
			return null;
		if (!attribute)
			return info.getInfoForProperty( tuple);
		else
			return info.getInfoForAttributeProperty( tuple);
	}
	
	/**
	 * ?
	 * @param type
	 * @return
	 */
	public boolean hasRelevantProperties( GmEntityType type) {
		MapperInfoForEntitytype info = getTypeInfo( type);
		if (info == null)
			return false;
		return info.hasRelevantProperties();
	}
	
	/**
	 * returns the {@link MapperInfoForProperty} of a {@link GmProperty}
	 * @param property - the {@link GmProperty}
	 * @return - the {@link MapperInfoForProperty} attached
	 */
	public MapperInfoForProperty getPropertyInfo( GmProperty property) {
		MapperInfoForEntitytype info = getTypeInfo( property.getDeclaringType());
		return info.getInfoForProperty(property);
	}
	
	/**
	 * returns the {@link MapperInfoForProperty} of a {@link GmProperty} named as such 
	 * @param type - the {@link GmEntityType} containing the {@link GmProperty}
	 * @param propertyName - the name of the {@link GmProperty}
	 * @return - the {@link MapperInfoForProperty}
	 */
	public MapperInfoForProperty getPropertyInfo( GmEntityType type, String propertyName) {
		MapperInfoForEntitytype info = getTypeInfo( type);
		return info.getInfoForProperty(propertyName);
	}
	
	/**
	 * returns the associated prefix of the namespace passed 
	 * @param namespace - the namespace 
	 * @return - the prefix 
	 */
	public String getPrefixOfNamespace( String namespace) {
		return namespaceToPrefixMap.get(namespace);
	}
	
	/**
	 * returns the namespace associated with the passed prefix 
	 * @param prefix - the prefix 
	 * @return - the namespace 
	 */
	public String getNamespaceOfPrefix( String prefix) {
		return prefixToNamespaceMap.get(prefix);
	}
	
	public Map<String, String> getTargetNamespaces() {
		return prefixToNamespaceMap;
	}
	
	public Map<String,Namespace> getNamespaces(){
		return namespaces;
	}
	
	public boolean getElementQualificationForNamespace( String uri) {
		Namespace namespace = namespaces.get( uri);
		if (namespace == null) 
			return false;
		return namespace.getElementQualification();
	}
	public boolean getAttributeQualificationForNamespace( String uri) {
		Namespace namespace = namespaces.get( uri);
		if (namespace == null) 
			return false;
		return namespace.getAttributeQualification();
	}
}
