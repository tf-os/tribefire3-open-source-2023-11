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

import tribefire.extension.xml.schemed.marshaller.xsd.HasSchemaTokens;
import tribefire.extension.xml.schemed.model.xsd.Annotation;
import tribefire.extension.xml.schemed.model.xsd.Include;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.Schema;

public class IncludeExpert extends AbstractSchemaExpert implements HasSchemaTokens {

	public static Include read(Schema declaringSchema,  XMLStreamReader reader) throws XMLStreamException {
		Include include = Include.T.create();
		attach(include, declaringSchema);
		
		Map<QName,String> attributes = readAttributes(reader);
		include.setSchemaLocation( attributes.get( new QName(SCHEMA_LOCATION)));
		include.setId( attributes.get( new QName(ID)));
		
		readAnyAttributes( include.getAnyAttributes(), attributes, ID, SCHEMA_LOCATION);

		reader.next();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {
					
					String tag = reader.getName().getLocalPart();
					switch (tag) {
					case ANNOTATION:
						Annotation annotation = AnnotationExpert.read( declaringSchema, reader);
						include.setAnnotation(annotation);					
						break;
					default:
						skip(reader);
					}					
				}
				break;
				case XMLStreamConstants.END_ELEMENT : {
					return include;
				}
				default: 
					break;
				}
			reader.next();
		}
		return include;
	}
	
	public static void write( XMLStreamWriter writer, Namespace namespace, Include suspect) throws XMLStreamException {
		if (suspect == null)
			return;
		String prefix = namespace.getPrefix();
		writer.writeStartElement( prefix != null ? prefix + ":" + INCLUDE : INCLUDE);
		writer.writeAttribute( SCHEMA_LOCATION, suspect.getSchemaLocation());
		writeAnyAttributes(writer, suspect.getAnyAttributes());
		AnnotationExpert.write(writer, namespace, suspect.getAnnotation());
		writer.writeEndElement();
	}

}
