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
package tribefire.extension.xml.schemed.model.xsd;

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.xml.schemed.model.xsd.archetypes.HasAttributeGroups;
import tribefire.extension.xml.schemed.model.xsd.archetypes.HasAttributes;

public interface Schema extends Annoted, SequenceAware, HasAttributes, HasAttributeGroups {
		
	final EntityType<Schema> T = EntityTypes.T(Schema.class);

	
	String getSchemaPrefix();
	void setSchemaPrefix( String prefix);
	
	Qualification getAttributeFormDefault();
	void setAttributeFormDefault( Qualification qualification);
	
	boolean getAttributeFormDefaultSpecified();
	void setAttributeFormDefaultSpecified( boolean specified);
	
	Qualification getElementFormDefault();
	void setElementFormDefault( Qualification qualification);
	
	boolean getElementFormDefaultSpecified();
	void setElementFormDefaultSpecified( boolean specified);	

	List<Namespace> getNamespaces();
	void setNamespaces( List<Namespace> namespaces);
	
	Namespace getSchemaNamespace();
	void setSchemaNamespace( Namespace namespace);
	
	Namespace getTargetNamespace();
	void setTargetNamespace( Namespace namespace);
	
	Namespace getDefaultNamespace();
	void setDefaultNamespace( Namespace namespace);
	
	String getXmlNs();
	void setXmlNs( String xmlNs);
	
	List<SchemaEntity> getEntities();
	void setEntities( List<SchemaEntity> entities);

	List<Include> getIncludes();
	void setIncludes( List<Include> includes);
	
	List<Import> getImports();
	void setImports( List<Import> imports);
	
	List<Element> getToplevelElements();
	void setToplevelElements( List<Element> elements);

	List<ComplexType> getComplexTypes();
	void setComplexTypes( List<ComplexType> types);
	
	List<SimpleType> getSimpleTypes();
	void setSimpleTypes( List<SimpleType> types);
		
	List<Group> getGroups();
	void setGroups( List<Group> groups);
	

	
		
	
	
}
