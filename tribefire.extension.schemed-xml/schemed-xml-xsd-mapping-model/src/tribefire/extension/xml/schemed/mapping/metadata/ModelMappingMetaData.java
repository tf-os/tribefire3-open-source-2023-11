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

import java.util.Map;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.ModelMetaData;

/**
 * a mapping meta data for the {@link GmMetaModel}<br/>
 * holds data (probably XML specific) data about the main types<br/>
 * i.e. an XML file, there must be a main type, but it may be one 
 * of a set of types. These types are stored in the list of mainTypes.
 *  
 * @author pit
 *
 */

public interface ModelMappingMetaData extends ModelMetaData, MappingMetaData {
	
	final EntityType<ModelMappingMetaData> T = EntityTypes.T(ModelMappingMetaData.class);
	
	/**
	 * back link to the model itself
	 * @return
	 */
	GmMetaModel getModel();
	void setModel( GmMetaModel model);

	/**
	 * a map that contains all mapped types, name {@link String} type signature to {@link MappingMetaData} 
	 * @return
	 */
	Map<String, MappingMetaData> getMappingMetaDataMap();
	void setMappingMetaDataMap( Map<String, MappingMetaData> mappingMetaDataMap);

	/**
	 * the {@link Map} that links the container types with their Element-names (aka the {@link GmType} of the top level elements)
	 * @return
	 */
	Map<String, GmType> getMainTypeMap();
	void setMainTypeMap( Map<String, GmType> elementToMainTypeMap);

	/* (non-Javadoc)
	 * @see com.braintribe.model.schemedXml.metadata.mapping.MappingMetaData#getXsdName()
	 */
	String getXsdName();
	void setXsdName(String xsdName);
	
	/**
	 * return the target namespace the XSD had (if any) 
	 * @return
	 */
	String getTargetNamespace();
	void setTargetNamespace(String targetNamespace);

	
	/**
	 * returns all the relevant name spaces 
	 * @return
	 */
	Map<String, Namespace> getNamespaces();
	void setNamespaces( Map<String, Namespace> namespaces);

	/**
	 * 
	 * @return
	 */
	boolean getElementFormIsQualified();
	void setElementFormIsQualified(boolean elementFormQualified);
	
	/**
	 * @return
	 */
	boolean getAttributeFormIsQualified();
	void setAttributeFormIsQualified(boolean attributeFormQualified);
}
