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
import tribefire.extension.xml.schemed.model.xsd.ComplexType;
import tribefire.extension.xml.schemed.model.xsd.Element;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.model.xsd.SimpleType;

public class ElementExpert extends AbstractSchemaExpert {
	
	public static Element read( Schema declaringSchema, XMLStreamReader reader) throws XMLStreamException {
		// wind to next event
		Element element = Element.T.create();
		attach(element, declaringSchema);
		
		Map<QName, String> attributes = readAttributes(reader);
		element.setName( attributes.get( new QName(NAME)));
	
		String value = attributes.get( new QName(ABSTRACT));
		if (value != null) {
			element.setAbstract( Boolean.valueOf(value));
		}
		
		String typeReference = attributes.get( new QName(TYPE));
		element.setTypeReference( QNameExpert.parse(typeReference));
	
		String elementReference = attributes.get( new QName(REF));
		element.setElementReference(QNameExpert.parse(elementReference));
		
		value = attributes.get( new QName(MIN_OCCURS));
		if (value != null)
			element.setMinOccurs(Integer.valueOf(value));
	
		value = attributes.get( new QName(MAX_OCCURS));
		if (value != null) {
			if (value.equalsIgnoreCase( UNBOUNDED)) {
				element.setMaxOccurs(-1);
			}
			else {
				element.setMaxOccurs(Integer.valueOf(value));
			}
		}
		
		readAnyAttributes( element.getAnyAttributes(), attributes, ID, NAME, TYPE, MIN_OCCURS, MAX_OCCURS, ABSTRACT, REF);
		
		reader.next();
		
		while (reader.hasNext()) {
			
			switch (reader.getEventType()) {
			
				case XMLStreamConstants.START_ELEMENT :												
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case SIMPLE_TYPE:
							SimpleType simpleType = SimpleTypeExpert.read( declaringSchema, reader);
							element.setType( simpleType);
							element.getNamedItemsSequence().add( simpleType);
							break;
						case COMPLEX_TYPE:
							ComplexType complexType = ComplexTypeExpert.read(declaringSchema, reader);
							element.setType( complexType);
							element.getNamedItemsSequence().add(complexType);
							break;
						case ANNOTATION:
							Annotation annotation = AnnotationExpert.read( declaringSchema, reader);
							element.setAnnotation(annotation);
							element.getNamedItemsSequence().add( annotation);
							break;
						case UNIQUE:
						case KEY:
						case KEY_REF:					
							default:
								skip(reader);
							break;
					}				
				break;
				
				case XMLStreamConstants.END_ELEMENT : {
					return element;
				}		
			
				default: 
					break;
			}
			reader.next();
		}
		return element;
	}
		
	public static void write( XMLStreamWriter writer, Namespace namespace, Element element) throws XMLStreamException {
		if (element == null)
			return;
		String prefix = namespace.getPrefix();
		writer.writeStartElement( prefix != null ? prefix + ":" + ELEMENT : ELEMENT);
		
		String name = element.getName();
		if (name != null) {
			writer.writeAttribute( NAME, name);
		}
		else {
			tribefire.extension.xml.schemed.model.xsd.QName elementReference = element.getElementReference();
			if (elementReference != null) {
				writer.writeAttribute( REF, QNameExpert.toString(elementReference));
			}
		}
		tribefire.extension.xml.schemed.model.xsd.QName typeReference = element.getTypeReference();
		if (typeReference != null) {
			writer.writeAttribute(TYPE, QNameExpert.toString( typeReference));
		}
		
		// write attributes
		int min = element.getMinOccurs();
		if (min != 1 || element.getMinOccursSpecified()) {
			writer.writeAttribute( MIN_OCCURS, "" + min);
		}
		int max = element.getMaxOccurs();
		if (max < 0) {
			writer.writeAttribute( MAX_OCCURS, UNBOUNDED);
		}
		else if (max != 1 || element.getMaxOccursSpecified()) {
			writer.writeAttribute( MAX_OCCURS, "" + max);
		}
		
		writeAnyAttributes(writer, element.getAnyAttributes());
		
		for (GenericEntity ge : element.getNamedItemsSequence()) {
			if (ge instanceof ComplexType) {
				ComplexTypeExpert.write(writer, namespace, (ComplexType) ge);				
			}
			else if (ge instanceof SimpleType) {
				SimpleTypeExpert.write(writer, namespace, (SimpleType) ge);				
			}			
			else if (ge instanceof Annotation) {
				AnnotationExpert.write(writer, namespace, (Annotation) ge);
			}
			else {
				throw new IllegalStateException("unknown type [" + ge.getClass() + "] encountered");
			}
		}
						
		writer.writeEndElement();
	}
}
