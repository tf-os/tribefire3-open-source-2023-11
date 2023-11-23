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
package tribefire.extension.xml.schemed.xsd.api.mapper.type;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSetType;
import com.braintribe.model.meta.GmType;

import tribefire.extension.xml.schemed.model.xsd.Element;
import tribefire.extension.xml.schemed.model.xsd.QName;
import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.model.xsd.Type;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.schema.QPath;

/**
 * create types 
 * 
 * @author pit
 *
 */
public interface TypeMapper {
	
	public static final String INTERNAL_TYPE_PACKAGE = "com.braintribe.xml";
	
	/**
	 * needs to be called before mapping starts (injects simple types etc)
	 */
	void initializeTypeSystem();
	
	default List<String> getEntityTypesAsSimpleTypes() {
		String [] entityTypes = new String [] { 
				"TOKEN",
				"NMTOKEN",
				"ID",
				"IDREF",
				"language",
				"normalizedString",
				"NCName",
				
		};
		
		return Arrays.asList( entityTypes);
	}
	
	/**
	 * returns or creates a {@link CollectionType} for the given element {@link GmType}
	 * @param elementType - the {@link GmType} that is to be used for an element 
	 * @return - a matching {@link CollectionType}, default is a {@link GmListType}, but can also be a {@link GmSetType}
	 */
	GmLinearCollectionType acquireCollectionType( GmType elementType, boolean asSet);

	
	boolean getIsCollectionTypeOverridenAsSet( String parentName, String propertyName);
	
	String getBacklinkPropertyToInjectFor( String parentName, String propertyName);
	
	GmEntityType getSubstitutingType(String name);
	GmEntityType getSubstitutingType(String parentName, String propertyName);
	
	/**
	 * generate an entity type 
	 * @param qname - the {@link QName} from the XSD
	 * @param type - the {@link Type} from the {@link Schema}
	 * @param name - the name of the type 
	 * @return - the created {@link GmEntityType}
	 */
	GmEntityType generateGmEntityType( QPath qpath, Type type, String name);
	
	
	/**
	 * remaps an already existing type - i.e. rebuilds the type signature, and maps to a different {@link Type} 
	 * @param type - the {@link Type} that is {@link GmEntityType} should map to
	 * @param existingGmEntityType - the {@link GmEntityType} that needs to be changed 
	 * @param name - the new name of the {@link GmEntityType} (which is used to build the type signature)
	 * @return - the remapped {@link GmEntityType} (the same as input)
	 */
	GmEntityType remapGmEntityType(Type type, GmEntityType existingGmEntityType, String name);
	
	
	/**
	 * unmaps an already existing type - i.e. due to the acquire pattern (recursive type definition),
	 * a type will be created and mapped before the content is fully read. The type may be replaced
	 * later (via virtual types, sequence & choice, for instance)
	 * @param type - the {@link Type} it was mapped to 
	 * @param existingGmEntityType - the {@link GmEntityType} that was created earlier
	 */
	void unmapGmEntityType( Type type, GmEntityType existingGmEntityType);
	
	/**
	 * generate an enum type 
	 * @param qname - the {@link QName} from the XSD
	 * @param type - the {@link Type} from the {@link Schema}
	 * @param name - the name of the enum type
	 * @return - the {@link GmEnumType} created
	 */
	GmEnumType generateGmEnumType( QPath qname, Type type, String name);
	
	GmEntityType acquireGenericEntityType();
	GmType acquireAnyType();
	GmType acquireAnyAttributeType();
	
	/**
	 * @param propertyType
	 * @return
	 */
	GmEntityType generateGmEntityTypeForSimpleType(QPath reference, Type type, String name, GmType propertyType);
	
	/**
	 * generate an enum constant
	 * @param name - the name of the constant 
	 * @param declaringType - the {@link GmEnumType} that it belongs to 
	 * @return - the created {@link GmEnumConstant}
	 */
	GmEnumConstant generateGmEnumConstant( String name, GmEnumType declaringType);
	
	/**
	 * (partially) creates a property  - declaring type and global info is *not* set
	 * @param name - the name of the property 
	 * @return  - the create {@link GmProperty}
	 */
	GmProperty generateGmProperty( String name);
	
	
	
	/**
	 * find the mapped {@link GmType} for the passed {@link Type}
	 * @param type - the {@link Type} to find its mapped pendant
	 * @return - the {@link GmType} if any, or null
	 */
	GmType lookupType( Type type);
	
	/**
	 * find the mapped simple type (for standard types) 
	 * @param qname - the {@link QName} such as xs:string, xsd:int etc 
	 * @return - the mapped {@link GmType} that represents the simple type 
	 */
	GmType lookupStandardSimpleType( QName qname);
	
	/**
	 * returns the mapped name of type (for GmEntityTypes standing in as simple types, i.e. ID, IDREF, TOKEN etc)
	 * @param type - the {@link GmType}
	 * @return - the mapped name (or null if not mapped)
	 */
	String getMappedNameOfType( GmType type);
	
	/**
	 * returns the actual base type name for the type, 
	 * i.e. NCName -> string
	 * @param type - the {@link GmType}
	 * @return - the basic type 
	 */
	String getSimpleStringBase( GmType type);
	
	/**
	 * @return
	 */
	Collection<GmType> getExtractedTypes();
	
	Map<String, GmType> getTopLevelElementToTypeAssociation();

	/**
	 * receives the top level information of the container elements and their respective types
	 * @param topLevelElementToTypeMap - maps the names of the {@link Element} with associated {@link GmType}
	 */
	void acknowledgeToplevelElementToTypeAssociation( Map<String, GmType> topLevelElementToTypeMap);

	boolean isTypeNameAvailable( String name);
	String assertNonCollidingTypeName( String proposal);

}
