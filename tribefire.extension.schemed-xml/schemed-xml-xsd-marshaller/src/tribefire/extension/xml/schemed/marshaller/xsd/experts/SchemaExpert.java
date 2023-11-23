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
package tribefire.extension.xml.schemed.marshaller.xsd.experts;

import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.braintribe.model.generic.GenericEntity;

import tribefire.extension.xml.schemed.model.xsd.Annotation;
import tribefire.extension.xml.schemed.model.xsd.Attribute;
import tribefire.extension.xml.schemed.model.xsd.AttributeGroup;
import tribefire.extension.xml.schemed.model.xsd.Comment;
import tribefire.extension.xml.schemed.model.xsd.ComplexType;
import tribefire.extension.xml.schemed.model.xsd.Element;
import tribefire.extension.xml.schemed.model.xsd.Group;
import tribefire.extension.xml.schemed.model.xsd.Import;
import tribefire.extension.xml.schemed.model.xsd.Include;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.Qualification;
import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.model.xsd.SimpleType;

/**
 * @author pit
 *
 */
public class SchemaExpert extends AbstractSchemaExpert {


	/**
	 * @param reader
	 * @return
	 * @throws XMLStreamException
	 */
	public static Schema read( XMLStreamReader reader) throws XMLStreamException {
		// Wind
		Schema schema = Schema.T.create();

		Map<QName, String> attributes = readAttributes(reader);
		String value = attributes.get( new QName(ATTRIBUTE_FORM_DEFAULT));
		if (value != null) {
			schema.setAttributeFormDefault( Qualification.valueOf(value));
			schema.setAttributeFormDefaultSpecified(true);
		} else {
			schema.setAttributeFormDefault( Qualification.unqualified);
		}
		
		value = attributes.get( new QName(ELEMENT_FORM_DEFAULT));
		if (value != null) {
			schema.setElementFormDefault( Qualification.valueOf(value));
			schema.setElementFormDefaultSpecified(true);
		} else {
			schema.setElementFormDefault( Qualification.unqualified);
		}
		
		schema.setId( attributes.get( new QName(ID)));
		
		String targetNamespaceUri = attributes.get( new QName(TARGET_NAMESPACE));	
		if (targetNamespaceUri != null) {
			Namespace targetNamespace = Namespace.T.create();
			targetNamespace.setUri(targetNamespaceUri);
			schema.setTargetNamespace(targetNamespace);
		}
		
		schema.setNamespaces( getNamespaces(reader));
		for (Namespace namespace : schema.getNamespaces()) {
			if (WWW_SCHEMA_DEF.equalsIgnoreCase( namespace.getUri())) {
				schema.setSchemaPrefix( namespace.getPrefix());
				schema.setSchemaNamespace(namespace);
			}
			else {
				if (namespace.getPrefix() == null) {
					schema.setDefaultNamespace( namespace);
				}
				else if (namespace.getUri().equalsIgnoreCase( targetNamespaceUri)) {
					schema.getTargetNamespace().setPrefix( namespace.getPrefix());
				}
			}
		}
		
		readAnyAttributes( schema.getAnyAttributes(), attributes, ID);
		
		

		reader.next();
				
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
			case XMLStreamConstants.START_ELEMENT :
																					
				String tag = reader.getName().getLocalPart();{
				switch (tag) {
					case ELEMENT: 
						Element toplevelElement = ElementExpert.read( schema, reader);
						toplevelElement.setToplevel(true);
						schema.getToplevelElements().add( toplevelElement);
						schema.getNamedItemsSequence().add( toplevelElement);
						break;
					case INCLUDE:
						Include include2 = IncludeExpert.read( schema, reader);
						schema.getIncludes().add( include2);
						schema.getNamedItemsSequence().add( include2);
						break;
					case IMPORT : 
						Import import2 = ImportExpert.read( schema, reader);
						schema.getImports().add( import2);
						schema.getNamedItemsSequence().add( import2);
						String importedNamespace = import2.getNamespace();
						if (importedNamespace.equalsIgnoreCase( "http://www.w3.org/XML/1998/namespace")) {
							Namespace xmlNamespace = Namespace.T.create();
							xmlNamespace.setUri( importedNamespace);
							xmlNamespace.setPrefix( "xml");
							schema.getNamespaces().add( xmlNamespace);
						}
						break;						
					case COMPLEX_TYPE:
						ComplexType complexType = ComplexTypeExpert.read( schema, reader);
						schema.getComplexTypes().add(complexType);
						schema.getNamedItemsSequence().add( complexType);						
						break;
					case SIMPLE_TYPE:
						SimpleType simpleType = SimpleTypeExpert.read( schema, reader);
						schema.getSimpleTypes().add( simpleType);
						schema.getNamedItemsSequence().add( simpleType);						
						break;
					case ATTRIBUTE_GROUP:
						AttributeGroup attributeGroup = AttributeGroupExpert.read( schema, reader);
						schema.getAttributeGroups().add( attributeGroup);
						schema.getNamedItemsSequence().add( attributeGroup);						
						break;
					case ATTRIBUTE : 
						Attribute attribute = AttributeExpert.read(schema, reader);
						schema.getAttributes().add( attribute);
						schema.getNamedItemsSequence().add( attribute);
						break;
					case GROUP:
						Group group = GroupExpert.read(schema, reader);
						schema.getGroups().add( group);
						schema.getNamedItemsSequence().add( group);						
						break;
					case ANNOTATION:
						Annotation annotation = AnnotationExpert.read(schema, reader);
						schema.setAnnotation(annotation);
						schema.getNamedItemsSequence().add(annotation);
						break;
				default:
					skip(reader);
				}
			}
			break;
			case XMLStreamConstants.COMMENT : {				
				Comment comment = CommentExpert.read( schema, reader);				
				schema.getComments().add( comment);
				schema.getNamedItemsSequence().add( comment);
				break;
			}
			case XMLStreamConstants.END_ELEMENT : {
				return schema;
			}
			case XMLStreamConstants.END_DOCUMENT: {
				return schema;
			}
			
			default: 
				break;
			}
			reader.next();
		}
		return null;
	}
	
	

	/**
	 * write a schema 
	 * @param schema
	 * @param writer
	 */
	public static void write( XMLStreamWriter writer, Schema schema) throws XMLStreamException {
		
		Namespace schemaNamespace = schema.getSchemaNamespace();
		if (schemaNamespace.getPrefix() != null) {		
			writer.writeStartElement( schemaNamespace.getPrefix(), SCHEMA, schemaNamespace.getUri());				
			writer.writeNamespace(schemaNamespace.getPrefix(), schemaNamespace.getUri());
		}
		else {
			writer.writeStartElement( SCHEMA);							
			writer.writeDefaultNamespace( schemaNamespace.getUri());
			writer.writeAttribute( "xmlns", schemaNamespace.getUri());
		}
		
		// attributes
		Qualification attributeFormDefault = schema.getAttributeFormDefault();
		if (attributeFormDefault != Qualification.unqualified || schema.getAttributeFormDefaultSpecified()) {
			writer.writeAttribute(ATTRIBUTE_FORM_DEFAULT, attributeFormDefault.toString());
		}
		
		Qualification elementFormDefault = schema.getElementFormDefault();
		if (elementFormDefault != Qualification.unqualified|| schema.getElementFormDefaultSpecified()) {
			writer.writeAttribute( ELEMENT_FORM_DEFAULT, elementFormDefault.toString());
		}
		
		// look at other attributes
		writeAnyAttributes( writer, schema.getAnyAttributes());
		
		
		
		// namespaces 
		for (Namespace namespace : schema.getNamespaces()) {
			if (namespace.equals( schemaNamespace))
				continue;
			
			String prefix = namespace.getPrefix();
			if (prefix != null) {
				writer.writeAttribute(XMLNS + ":" + prefix, namespace.getUri());
			}
			else {
				writer.writeAttribute(XMLNS, namespace.getUri());
			}
		}
		
		Namespace targetNamespace = schema.getTargetNamespace();
		if (targetNamespace != null) {
			writer.writeAttribute( TARGET_NAMESPACE, targetNamespace.getUri());
		}
		
		// types
		for (GenericEntity ge: schema.getNamedItemsSequence()) {
			if (ge instanceof Element) {				
				ElementExpert.write(writer, schemaNamespace, (Element) ge);
			}
			else if (ge instanceof Import) {
				ImportExpert.write( writer, schemaNamespace, (Import) ge);				
			}
			else if (ge instanceof Include) {				
				IncludeExpert.write( writer, schemaNamespace, (Include) ge);
			}
			else if (ge instanceof SimpleType) {
				SimpleTypeExpert.write(writer, schemaNamespace, (SimpleType) ge);
			}
			else if (ge instanceof ComplexType) {
				ComplexTypeExpert.write(writer, schemaNamespace, (ComplexType) ge); 
			}
			else if (ge instanceof AttributeGroup) {
				AttributeGroupExpert.write(writer, schemaNamespace, (AttributeGroup) ge);
			}
			else if (ge instanceof Group) {
				GroupExpert.write(writer, schemaNamespace, (Group) ge);
			}
			else if (ge instanceof Comment) {
				CommentExpert.write(writer, schemaNamespace, (Comment) ge);
			}
			else if (ge instanceof Annotation) {
				AnnotationExpert.write(writer, schemaNamespace, (Annotation) ge);
			}
			else if (ge instanceof Attribute) {
				AttributeExpert.write(writer, schemaNamespace, (Attribute) ge);
			}
			else {
				throw new IllegalStateException( "unkown type [" + ge.getClass() + "] encountered");
			}
				
		}
		
		writer.writeEndElement();
	}

	
}
