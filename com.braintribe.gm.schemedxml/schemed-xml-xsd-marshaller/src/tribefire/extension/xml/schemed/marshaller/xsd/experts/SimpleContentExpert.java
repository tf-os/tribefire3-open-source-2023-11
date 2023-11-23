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
import tribefire.extension.xml.schemed.model.xsd.Extension;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.model.xsd.SimpleContent;
import tribefire.extension.xml.schemed.model.xsd.SimpleContentRestriction;

public class SimpleContentExpert extends AbstractSchemaExpert {
	
	public static SimpleContent read( Schema declaringSchema, XMLStreamReader reader) throws XMLStreamException {
		SimpleContent simpleContent = SimpleContent.T.create();
		attach(simpleContent, declaringSchema);
		Map<QName, String> attributes = readAttributes( reader);
		
		simpleContent.setId( attributes.get( new QName(ID)));
		readAnyAttributes( simpleContent.getAnyAttributes(), attributes, ID);
						
		reader.next();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case RESTRICTION:
							SimpleContentRestriction contentRestriction = SimpleContentRestrictionExpert.read(declaringSchema, reader);
							simpleContent.setRestriction( contentRestriction);
							simpleContent.getNamedItemsSequence().add( contentRestriction);
							break;							
						case EXTENSION:
							Extension contentExtension = ExtensionExpert.read(declaringSchema, reader);
							simpleContent.setExtension( contentExtension);
							simpleContent.getNamedItemsSequence().add( contentExtension);
							break;
						case ANNOTATION:
							Annotation annotation = AnnotationExpert.read(declaringSchema,reader);
							simpleContent.setAnnotation(annotation);
							simpleContent.getNamedItemsSequence().add(annotation);
						break;
						default:
							skip(reader);
					}
				}
				break;
				case XMLStreamConstants.END_ELEMENT : {
					return simpleContent;
				}
				default: 
					break;
				}
			reader.next();
		}
		return simpleContent;
	}

	public static void write( XMLStreamWriter writer, Namespace namespace, SimpleContent suspect) throws XMLStreamException {
		if (suspect == null)
			return;
		String prefix = namespace.getPrefix();
		writer.writeStartElement( prefix != null ? prefix + ":" + SIMPLE_CONTENT : SIMPLE_CONTENT);
		
		for (GenericEntity ge : suspect.getNamedItemsSequence()) {
			if (ge instanceof SimpleContentRestriction) {
				SimpleContentRestrictionExpert.write(writer, namespace, (SimpleContentRestriction) ge);				
			}
			else if (ge instanceof Extension) {
				ExtensionExpert.write(writer, namespace, (Extension) ge);				
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
