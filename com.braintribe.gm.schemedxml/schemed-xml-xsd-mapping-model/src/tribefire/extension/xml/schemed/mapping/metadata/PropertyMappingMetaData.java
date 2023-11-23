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
package tribefire.extension.xml.schemed.mapping.metadata;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.PropertyMetaData;

/**
 * a mapping meta data for a {@link GmProperty}<br/>
 * <br/>
 * it also contains additional data (probably only used in the XML environment?)<br/>
 * attributeFlag: true if the property has been imported from an XML attribute<br/>
 * multiplicityFlag : true if the property is actually a collection <br/>
 * in xml, you don't specify collections, you just repeat the elements, but in a model
 * that doesn't work, so we need a way to tag this property a collection property<br/>
 * the Mapper (com.braintribe.model.processing:Mapping#1.0) automatically creates a 
 * synthetic property name {@literal_'camel-cased-element-name'List} for it. 
 *  
 * 
 * @author pit
 *
 */

public interface PropertyMappingMetaData extends PropertyMetaData, MappingMetaData, HasNamespace, GenericEntity{
		
	final EntityType<PropertyMappingMetaData> T = EntityTypes.T(PropertyMappingMetaData.class);

	
	/**
	 * actual property 
	 * @return - the {@link GmProperty} it is attached to 
	 */
	GmProperty getProperty();
	void setProperty(GmProperty property);
	
	boolean getIsVirtual();
	void setIsVirtual( boolean isVirtual);
	
	/**
	 * true if the property is represented as an attribute 
	 * @return
	 */
	boolean getIsAttribute();
	void setIsAttribute(boolean attributeFlag);

	/**
	 * true if the property is a collection 
	 * @return
	 */
	boolean getIsMultiple();
	void setIsMultiple(boolean multiplicityFlag);

	/**
	 * if multiple, the this reflects the elemnt ype  
	 * @return
	 */
	GmType getElementType();
	void setElementType(GmType elementType);
	
	/**
	 * the index within a sequence 
	 * @return
	 */
	List<Integer> getIndex();
	void setIndex(List<Integer> index);
	
	/**
	 * the XSD name of the type that the property has (
	 * @return
	 */
	String getApparentXsdType();
	void setApparentXsdType(String xsdType);

	/**
	 * the XSD name of the actual type (the drilled down type)
	 * @return
	 */
	String getActualXsdType();
	void setActualXsdType(String drillDownXsdType);

	/**
	 * true if the property is the XSD ID type
	 * @return
	 */
	boolean getIsIdProperty();
	void setIsIdProperty(boolean idPropertyFlag);

	/**
	 * true if the property is a simple value 
	 * @return
	 */
	boolean getIsValue();
	void setIsValue(boolean valueFlag);
	
	/**
	 * the fixed value the property has 
	 * @return
	 */
	String getFixedValue();
	void setFixedValue(String fixedValue);

	/**
	 * the default value of the property - i.e. the value it has if it's not set
	 * @return
	 */
	String getDefaultValue();
	void setDefaultValue(String defaultValue);

	
	/**
	 * @return
	 */
	boolean getIsUndefined();
	void setIsUndefined(boolean undefined);
	
	/**
	 * true if its a synthetic property injected to reflect a backlink
	 * @return
	 */
	boolean getIsBacklinkProperty();
	void setIsBacklinkProperty( boolean backlink);
	
	// probably not even required
	/**
	 * the property this property is backlinking to
	 * @return
	 */
	GmProperty getBacklinkTargetProperty();
	void setBacklinkTargetProperty( GmProperty property);
	
	
	/**
	 * true if the child's namespace should override the parent's.
	 * true in constructs like container element or referenced element 
	 * @return - true if override's on, false otherwise (default)
	 */
	boolean getNamespaceOverrides();
	void setNamespaceOverrides( boolean overrides);

	
}
