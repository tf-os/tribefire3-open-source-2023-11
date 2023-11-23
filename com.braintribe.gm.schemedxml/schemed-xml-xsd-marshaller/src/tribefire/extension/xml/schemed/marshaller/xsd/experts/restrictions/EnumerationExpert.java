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
package tribefire.extension.xml.schemed.marshaller.xsd.experts.restrictions;

import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import tribefire.extension.xml.schemed.marshaller.xsd.experts.AbstractSchemaExpert;
import tribefire.extension.xml.schemed.marshaller.xsd.experts.AnnotationExpert;
import tribefire.extension.xml.schemed.model.xsd.Annotation;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.model.xsd.restrictions.Enumeration;

public class EnumerationExpert extends AbstractSchemaExpert {
	public static Enumeration read( Schema declaringSchema, XMLStreamReader reader) throws XMLStreamException {
		
		Enumeration enumeration = Enumeration.T.create();		
		attach( enumeration, declaringSchema);
		Map<QName,String> attributes = readAttributes(reader);		
		enumeration.setValue(attributes.get( new QName( VALUE)));		
		reader.next();
		
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case ANNOTATION:
						Annotation annotation = AnnotationExpert.read(declaringSchema, reader);
						enumeration.setAnnotation(annotation);					
						break;
						default:
						skip(reader);									
					}										
					break;				
				}
				case XMLStreamConstants.END_ELEMENT : {
					return enumeration;
				}
				default: 
					break;
				}
			reader.next();
		}
		return enumeration;
	}
	
	public static void write( XMLStreamWriter writer, Namespace namespace, Enumeration enumeration) throws XMLStreamException {
		if (enumeration == null)
			return;		
			String prefix = namespace.getPrefix();
			writer.writeStartElement( prefix != null ? prefix + ":" + ENUMERATIION : ENUMERATIION);
			writer.writeAttribute( VALUE, "" + enumeration.getValue());
			AnnotationExpert.write(writer, namespace, enumeration.getAnnotation());
			writer.writeEndElement();			
	}
	
}
