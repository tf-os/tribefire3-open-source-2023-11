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
package tribefire.extension.xml.schemed.xsd.api.analyzer;

import java.util.Map;
import java.util.Set;

import com.braintribe.model.meta.GmType;

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
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.MappingContext;

/**
 * a {@link SchemaRegistry} is the functional expert that backs a {@link Schema}
 * @author pit
 *
 */
public interface SchemaRegistry {
	
	/**
	 * look up a {@link ComplexType} with the {@link Schema} structure, current {@link Schema} as starting point
	 * @param qname - the {@link QName} that identifies the type
	 * @return - the {@link ComplexType} if any
	 */
	SchemaLookupResult<ComplexType> lookupComplexType(QName qname);
	/**
	 * look up a {@link SimpleType} with the {@link Schema} structure, current {@link Schema} as starting point
	 * @param qname - the {@link QName} that identifies the type
	 * @return - the {@link SimpleType} if any
	 */
	SchemaLookupResult<SimpleType> lookupSimpleType(QName qname);
	/**
	 * look up an {@link Element} with the {@link Schema} structure, current {@link Schema} as starting point
	 * @param qname - the {@link QName} that identifies the element
	 * @return - the {@link Element} if any
	 */
	SchemaLookupResult<Element> lookupElement(QName qname);
	/**
	 * look up a {@link Group} with the {@link Schema} structure, current {@link Schema} as starting point
	 * @param qname - the {@link QName} that identifies the group
	 * @return - the {@link Group} if any
	 */
	SchemaLookupResult<Group> lookupGroup(QName qname);
	/**
	 * look up a {@link AttributeGroup} with the {@link Schema} structure, current {@link Schema} as starting point
	 * @param qname - the {@link QName} that identifies the attribute group
	 * @return - the {@link AttributeGroup} if any
	 */
	SchemaLookupResult<AttributeGroup> lookupAttributeGroup( QName qname);
	/**
	 * look up a {@link Attribute} with the {@link Schema} structure, current {@link Schema} as starting point
	 * @param qname - the {@link QName} that identifies the attribute
	 * @return - the {@link Attribute} if any
	 */
	SchemaLookupResult<Attribute> lookupAttribute( QName qname);
	
	/**
	 * gets the {@link Schema} the {@link SchemaRegistry} is backing 
	 * @return - the {@link Schema}
	 */
	Schema getSchema();
	
	/**
	 * gets the parent {@link SchemaRegistry} (if this is a included/imported one)
	 * @return - the parent {@link SchemaRegistry}
	 */
	SchemaRegistry getParentSchemaRegistry();

	/**
	 * map (process) the {@link Schema} and all its attached {@link Schema}, 
	 * all toplevel {@link Element}, {@link ComplexType} and {@link SimpleType}
	 * @param context - the {@link MappingContext}
	 * @return - a {@link Set} of {@link GmType}
	 */
	Set<GmType> map(MappingContext context);
	
	/**
	 * creates a {@link QName} that is as qualified as possible, i.e. with prefix and URI 
	 * @param name - the name as found in the XSD
	 * @return - a {@link QName}
	 */
	QName generateQName( Schema schema, String name);
	
	/**
	 * makes sure that the {@link QName} passed is as qualified as possible, i.e. with prefix and URI
	 * @param qname - the {@link QName} to ensure 
	 * @return - the same, yet qualified {@link QName}
	 */
	QName ensureQName( Schema schema, QName qname);

	/**
	 * get all {@link Schema} that are imported
	 * @return - a {@link Map} of Namespace to {@link SchemaRegistry}
	 */
	Map<String, SchemaRegistry> getImportedSchemata();
	
	/**
	 * get all simple types of the {@link Schema}
	 * @return - a {@link Map} of {@link QName} to {@link SimpleType}
	 */
	Map<QName, SimpleType> getSimpleTypes();
	
	/**
	 * gets all complex types of the {@link Schema}
	 * @return - a {@link Map} of {@link QName} to {@link ComplexType}
	 */
	Map<QName, ComplexType> getComplexTypes();
	/**
	 * gets all top level elements of the {@link Schema}
	 * @return - a {@link Map} of {@link QName} to {@link Element}
	 */
	Map<QName, Element> getTopLevelElements();
	
	/**
	 * adopt a virtual type, i.e. a type that has been created 
	 * @param type
	 */
	void adoptType( Type type);
	
	/**
	 * assign an {@link Element} (top level) with the backing type 
	 * @param element - the {@link Element}
	 * @param type - the {@link GmType}
	 */
	void associateElementWithType( Element element, GmType type);
	/**
	 * @param element - the {@link Element}
	 * @return - true if it already has been processed 
	 */
	boolean isElementMarkedAsAlreadyProcessed( Element element);
	
	/**
	 * @param sourceSchema
	 * @return
	 */
	String getNamespaceForSchema(Schema sourceSchema);
	
	/**
	 * get the target namespace of the {@link Schema}
	 * @return - the {@link Namespace}
	 */
	Namespace getTargetNamespace();
	
}
