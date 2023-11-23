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
import tribefire.extension.xml.schemed.model.xsd.AppInfo;
import tribefire.extension.xml.schemed.model.xsd.Documentation;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.Schema;

public class AnnotationExpert extends AbstractSchemaExpert {

	public static Annotation read( Schema declaringSchema, XMLStreamReader reader) throws XMLStreamException {
		Annotation annotation = Annotation.T.create();
		attach(annotation, declaringSchema);
		// wind to next event
		Map<QName, String> attributes = readAttributes(reader);
		reader.next();
		
		annotation.setId( attributes.get( new QName(ID)));
		
		readAnyAttributes( annotation.getAnyAttributes(), attributes, ID);
		
		attach(annotation, declaringSchema);
		
		while (reader.hasNext()) {
			
			switch (reader.getEventType()) {
			
				case XMLStreamConstants.START_ELEMENT :
												
					String tag = reader.getName().getLocalPart(); 
				
					switch ( tag) {
						case APP_INFO:
							AppInfo appinfo = AppInfoExpert.read( declaringSchema, reader);
							annotation.getNamedItemsSequence().add(appinfo);
							annotation.getAppInfo().add(appinfo);
							break;
						case DOCUMENTATION:
							Documentation documentation = DocumentationExpert.read( declaringSchema, reader);
							annotation.getNamedItemsSequence().add( documentation);
							annotation.getDocumentation().add(documentation);
							break;
						default:
							skip(reader);
					}				
				
				break;
				case XMLStreamConstants.END_ELEMENT : {
					return annotation;
				}		
			
				default: 
					break;
			}
			reader.next();
		}
		return annotation;
	}
	
	public static void write( XMLStreamWriter writer, Namespace namespace, Annotation annotation) throws XMLStreamException { 
		if (annotation == null)
			return;
		String prefix = namespace.getPrefix();		
		writer.writeStartElement( prefix != null ? prefix + ":" + ANNOTATION : ANNOTATION);
	
		writeAnyAttributes(writer, annotation.getAnyAttributes());
		
		for (GenericEntity ge : annotation.getNamedItemsSequence()) {
			if (ge instanceof AppInfo) {
				AppInfoExpert.write(writer, namespace, (AppInfo) ge);				
			}
			else if (ge instanceof Documentation) {
				DocumentationExpert.write( writer, namespace, (Documentation) ge);				
			}
			else {
				throw new IllegalStateException("unknown type [" + ge.getClass() + "] encountered");
			}
		}
		writer.writeEndElement();
	}
}
