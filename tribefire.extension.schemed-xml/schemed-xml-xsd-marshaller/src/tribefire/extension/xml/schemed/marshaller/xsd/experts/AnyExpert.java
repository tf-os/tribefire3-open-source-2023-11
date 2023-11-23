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

import tribefire.extension.xml.schemed.model.xsd.Annotation;
import tribefire.extension.xml.schemed.model.xsd.Any;
import tribefire.extension.xml.schemed.model.xsd.ComplexType;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.model.xsd.SimpleType;

public class AnyExpert extends AbstractSchemaExpert {

	public static Any read( Schema declaringSchema, XMLStreamReader reader) throws XMLStreamException {
		Any any = Any.T.create();
		
		attach(any, declaringSchema);
		
		Map<QName, String> attributes = readAttributes(reader);
		
		String value = attributes.get( new QName(MIN_OCCURS));
		if (value != null)
			any.setMinOccurs(Integer.valueOf(value));
	
		value = attributes.get( new QName(MAX_OCCURS));
		if (value != null) {
			if (value.equalsIgnoreCase( UNBOUNDED)) {
				any.setMaxOccurs(-1);
			}
			else {
				any.setMaxOccurs(Integer.valueOf(value));
			}
		}
		
		readAnyAttributes( any.getAnyAttributes(), attributes, ID, NAME, TYPE, MIN_OCCURS, MAX_OCCURS, ABSTRACT, REF);
		
		reader.next();
		
		while (reader.hasNext()) {
			
			switch (reader.getEventType()) {
			
				case XMLStreamConstants.START_ELEMENT :												
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case ANNOTATION:
							Annotation annotation = AnnotationExpert.read( declaringSchema, reader);
							any.setAnnotation(annotation);
							break;									
						default:
							skip(reader);
							break;
					}				
				break;
				
				case XMLStreamConstants.END_ELEMENT : {
					return any;
				}		
			
				default: 
					break;
			}
			reader.next();
		}
		
		return any;
	}

	public static void write( XMLStreamWriter writer, Namespace namespace, Any any) throws XMLStreamException {
		if (any == null) {
			return;
		}
		String prefix = namespace.getPrefix();
		writer.writeStartElement( prefix != null ? prefix + ":" + ANY : ANY);
		
		// write attributes
		int min = any.getMinOccurs();
		if (min != 1 || any.getMinOccursSpecified()) {
			writer.writeAttribute( MIN_OCCURS, "" + min);
		}
		int max = any.getMaxOccurs();
		if (max < 0) {
			writer.writeAttribute( MAX_OCCURS, UNBOUNDED);
		}
		else if (max != 1 || any.getMaxOccursSpecified()) {
			writer.writeAttribute( MAX_OCCURS, "" + max);
		}
		
		writeAnyAttributes(writer, any.getAnyAttributes());
		
		Annotation annotation = any.getAnnotation();
		if (annotation != null) {
			AnnotationExpert.write(writer, namespace, annotation);
		}
		
		writer.writeEndElement();
			
	}
}
