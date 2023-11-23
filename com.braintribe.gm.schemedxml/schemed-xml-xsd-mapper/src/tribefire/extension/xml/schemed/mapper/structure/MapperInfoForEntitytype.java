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

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;

import tribefire.extension.xml.schemed.mapping.metadata.EntityTypeMappingMetaData;
import tribefire.extension.xml.schemed.mapping.metadata.PropertyMappingMetaData;
import tribefire.extension.xml.schemed.marshaller.commons.QNameExpert;
import tribefire.extension.xml.schemed.marshaller.commons.QNameWrapperCodec;
import tribefire.extension.xml.schemed.model.xsd.QName;

/**
 * the structure that contains the information about a {@link GmEntityType}
 * 
 * @author pit
 *
 */
public class MapperInfoForEntitytype extends MapperInfoForType {
	// property name (as QName) to MapperInfoForProperty map
	private Map<QName, MapperInfoForProperty> nameToPropertyInfoMap = CodingMap.createHashMapBased( new QNameWrapperCodec());
	private Map<QName, MapperInfoForProperty> nameToAttributePropertyInfoMap = CodingMap.createHashMapBased( new QNameWrapperCodec());
	// property name (as String) to MapperInfoForProperty map
	private Map<String, MapperInfoForProperty> propertyNameToInfoMap = new HashMap<String, MapperInfoForProperty>();
	// GmProperty to MapperInfoForProperty map 
	private Map<GmProperty, MapperInfoForProperty> propertyToInfoMap = new HashMap<GmProperty, MapperInfoForProperty>();
	// 
	private MapperInfoForProperty simpleContentProperty;
	private Boolean hasRelevantProperties;
	
	private EntityTypeMappingMetaData metaData;

	@Override
	public GmEntityType getType() {
		return (GmEntityType) type;
	}

	public void setType(GmEntityType type) {
		this.type = type;
	}

	public EntityTypeMappingMetaData getMetaData() {
		return metaData;
	}
	public void setMetaData(EntityTypeMappingMetaData metaData) {
		this.metaData = metaData;
	}
	
	/**
	 * get the {@link MapperInfoForProperty} for all properties of the type 
	 * @return - a {@link Collection} of {@link MapperInfoForProperty}
	 */
	public Collection<MapperInfoForProperty> getInfoForProperties() {
		return propertyToInfoMap.values();
	}	
	/**
	 * returns the {@link MapperInfoForProperty} for the given {@link GmProperty}
	 * @param property - the {@link GmProperty}
	 * @return - the {@link MapperInfoForProperty} if any
	 */
	public MapperInfoForProperty getInfoForProperty( GmProperty property) {
		return propertyToInfoMap.get(property);				
	}	
	/**
	 * returns the {@link MapperInfoForProperty} for the property as identified by name
	 * @param propertyName - the {@link String} with the name of the property 
	 * @return - the {@link MapperInfoForProperty} if any
	 */
	public MapperInfoForProperty getInfoForProperty( String propertyName) {
		return propertyNameToInfoMap.get(propertyName);				
	}
	/**
	 * returns the {@link MapperInfoForProperty} for the property as identified by the {@link QName}
	 * @param tuple - the {@link QName} that qualifies the property 
	 * @return - the {@link MapperInfoForProperty} if any 
	 */
	public MapperInfoForProperty getInfoForProperty( QName tuple) {			
		QName scanTuple = QNameExpert.parse( null, tuple.getLocalPart());
		return nameToPropertyInfoMap.get( scanTuple);
	}
	
	/**
	 * returns the {@link MapperInfoForProperty} for the property as identified by the {@link QName}
	 * @param tuple - the {@link QName} that qualifies the property 
	 * @return - the {@link MapperInfoForProperty} if any 
	 */
	public MapperInfoForProperty getInfoForAttributeProperty( QName tuple) {
		String localPart = tuple.getLocalPart();
		String prefix = tuple.getPrefix();
				
		// TODO : MARKER 'XML-CLUDGE'
		if (prefix != null && prefix.equals("xml")) {
			localPart = "xml_" + localPart;
		}
		QName scanTuple = QNameExpert.parse( null, localPart);
		return nameToAttributePropertyInfoMap.get( scanTuple);
	}
	
	
	public MapperInfoForProperty getInfoForSimpleContentProperty() {	
		return simpleContentProperty;	
	}
	
	/**
	 * returns true if there are properties that aren't attributes ? 
	 * @return - true if properties were found that arent' attributes.. 
	 */
	public boolean hasRelevantProperties() {
		if (hasRelevantProperties == null) {
			if (propertyToInfoMap.size() == 0) {
				hasRelevantProperties = false;
			}
			else {
				for (MapperInfoForProperty pInfo : propertyNameToInfoMap.values()) {
					if (!pInfo.getMetaData().getIsAttribute()) {
						hasRelevantProperties = true;
						return hasRelevantProperties;
					}
				}
				hasRelevantProperties = false;
			}
		}
		return hasRelevantProperties;
	}
	
	/**
	 * add a {@link MapperInfoForProperty} to the fold
	 * @param propertyInfo
	 */
	public void addProperty( MapperInfoForProperty propertyInfo) {
		// if it's virtual type, do that specially 
		PropertyMappingMetaData propertyMappingMetaData = propertyInfo.getMetaData();
		if (Boolean.TRUE.equals( propertyMappingMetaData.getIsVirtual())) {
			processVirtualType( propertyInfo);
			return;
		}
		GmProperty property = propertyInfo.getProperty();
		propertyToInfoMap.put( property, propertyInfo);
		String propertyName = property.getName();
		propertyNameToInfoMap.put( propertyName, propertyInfo);

		if (Boolean.TRUE.equals(propertyMappingMetaData.getIsBacklinkProperty())) {
			return;
		}
	
		// simple value type 
		if (Boolean.TRUE.equals(propertyMappingMetaData.getIsValue())) {
			simpleContentProperty = propertyInfo;
		} 		
		else  {
			// standard type 
			String name = propertyMappingMetaData.getXsdName();
			String namespace = propertyMappingMetaData.getNamespace();
			QName tuple = QNameExpert.parse( name);
			tuple.setNamespaceUri(namespace);
			if (propertyMappingMetaData.getIsAttribute()) {
				nameToAttributePropertyInfoMap.put( tuple, propertyInfo);	
			}
			else {			
				nameToPropertyInfoMap.put( tuple, propertyInfo);	
			}
		}
	}

	/**
	 * processes a virtual type ... a injected sequence for instance.. <br/>
	 * virtual means that its properties actually are properties of its parent type for the sake of mapping
	 * so it mus
	 * @param propertyInfo
	 */
	private void processVirtualType(MapperInfoForProperty propertyInfo) {
		GmProperty gmProperty = propertyInfo.getProperty();
		GmEntityType parentType = gmProperty.getDeclaringType();
		GmType propertyType = gmProperty.getType();
		GmProperty property = propertyInfo.getProperty();
		propertyToInfoMap.put( property, propertyInfo);
		String propertyName = property.getName();
		propertyNameToInfoMap.put( propertyName, propertyInfo);
	}
	
}
