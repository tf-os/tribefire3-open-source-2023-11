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

import tribefire.extension.xml.schemed.marshaller.commons.QNameExpert;
import tribefire.extension.xml.schemed.model.xsd.Annotation;
import tribefire.extension.xml.schemed.model.xsd.Attribute;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.Qualification;
import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.model.xsd.SimpleType;
import tribefire.extension.xml.schemed.model.xsd.Use;

public class AttributeExpert extends AbstractSchemaExpert {
	public static Attribute read( Schema declaringSchema, XMLStreamReader reader) throws XMLStreamException {
		
		Attribute attribute = Attribute.T.create();
		attach(attribute, declaringSchema);
		
		Map<QName,String> attributes = readAttributes(reader);		
		attribute.setDefault( attributes.get( new QName( DEFAULT)));
		attribute.setFixed( attributes.get( new QName( FIXED)));
		attribute.setId( attributes.get( new QName( ID)));

		String qualificationAsString = attributes.get( new QName( FORM));
		if (qualificationAsString != null) {
			attribute.setForm( Qualification.valueOf(qualificationAsString));
		}
		else {
			attribute.setForm( Qualification.unqualified);
		}
		
		attribute.setName( attributes.get( new QName( NAME)));
		
		String typeReference = attributes.get( new QName( TYPE));
		if (typeReference != null) {
			attribute.setTypeReference( QNameExpert.parse( typeReference));
		}
		
		String attributeReference = attributes.get( new QName( REF));
		if (attributeReference != null) {
			attribute.setRef( QNameExpert.parse( attributeReference));
		}
		
		String useAsString = attributes.get(  new QName( USE));
		if (useAsString != null) {
			attribute.setUse( Use.valueOf(useAsString));
			attribute.setUseSpecified(true);
		}
		else {
			attribute.setUse( Use.optional);
			
		}
				
		readAnyAttributes( attribute.getAnyAttributes(), attributes, ID, DEFAULT,FIXED,FORM,NAME,REF,TYPE,USE);
		
		reader.next();
		
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case SIMPLE_TYPE :
							SimpleType simpleType = SimpleTypeExpert.read(declaringSchema, reader);
							attribute.setType( simpleType);
							attribute.getNamedItemsSequence().add( simpleType);
							break;
						case ANNOTATION:
							Annotation annotation = AnnotationExpert.read( declaringSchema, reader);
							attribute.setAnnotation(annotation);
							attribute.getNamedItemsSequence().add(annotation);
							break;
						default:
						skip(reader);									
					}										
					break;				
				}
				case XMLStreamConstants.END_ELEMENT : {
					return attribute;
				}
				default: 
					break;
				}
			reader.next();
		}
		return attribute;
	}
	
	public static void write( XMLStreamWriter writer, Namespace namespace, Attribute attribute) throws XMLStreamException {
		if (attribute == null)
			return;
		String prefix = namespace.getPrefix();
		writer.writeStartElement( prefix != null ? prefix + ":" + ATTRIBUTE : ATTRIBUTE);
		
		Qualification quali = attribute.getForm();
		if (quali != Qualification.unqualified) {
			writer.writeAttribute(FORM, quali.toString());
		}
		
		String defaultValue = attribute.getDefault();
		if (defaultValue != null) {
			writer.writeAttribute( DEFAULT, defaultValue);
		}
		
		String fixedValue = attribute.getFixed();
		if (fixedValue != null) {
			writer.writeAttribute( FIXED, fixedValue);
		}
		
		String name = attribute.getName();
		if (name != null) {
			writer.writeAttribute( NAME, name);
		}
		
		tribefire.extension.xml.schemed.model.xsd.QName id = attribute.getId();
		if (id != null) {
			writer.writeAttribute( ID, QNameExpert.toString( id));
		}
		
		tribefire.extension.xml.schemed.model.xsd.QName attributeReference = attribute.getRef();
		if (attributeReference != null) {
			writer.writeAttribute( REF, QNameExpert.toString( attributeReference));
		}
		tribefire.extension.xml.schemed.model.xsd.QName typeReference = attribute.getTypeReference();
		if (typeReference != null) {
			writer.writeAttribute( TYPE, QNameExpert.toString( typeReference));
		}
		
		Use use = attribute.getUse();
		if (use != Use.optional || attribute.getUseSpecified()) {
			writer.writeAttribute( USE, use.toString());
		}
		
		writeAnyAttributes(writer, attribute.getAnyAttributes());
		
		for (GenericEntity ge : attribute.getNamedItemsSequence()) {
			if (ge instanceof SimpleType) {
				SimpleTypeExpert.write(writer, namespace, (SimpleType) ge);				
			}
			else if (ge instanceof Annotation) {
				AnnotationExpert.write(writer, namespace, (Annotation) ge);
			}
			else  {
				throw new IllegalStateException("unknown type [" + ge.getClass() + "] encountered");
			}				
		}
				
		writer.writeEndElement();
	}
}
