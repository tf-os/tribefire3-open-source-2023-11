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
import tribefire.extension.xml.schemed.model.xsd.ComplexContent;
import tribefire.extension.xml.schemed.model.xsd.ComplexContentRestriction;
import tribefire.extension.xml.schemed.model.xsd.Extension;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.Schema;

public class ComplexContentExpert extends AbstractSchemaExpert {
	
	public static ComplexContent read( Schema declaringSchema, XMLStreamReader reader) throws XMLStreamException {
		ComplexContent complexContent = ComplexContent.T.create();
		attach(complexContent, declaringSchema);
	
		Map<QName, String> attributes = readAttributes( reader);
		
		complexContent.setId( attributes.get( new QName(ID)));
		String mixedAsString = attributes.get( new QName( MIXED));
		if (mixedAsString != null) {
			complexContent.setMixed( Boolean.parseBoolean(mixedAsString));
			complexContent.setMixedSpecified(true);
			
		}
		readAnyAttributes( complexContent.getAnyAttributes(), attributes, ID, MIXED);
						
		reader.next();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case RESTRICTION:
							ComplexContentRestriction contentRestriction = ComplexContentRestrictionExpert.read(declaringSchema, reader);
							complexContent.setRestriction( contentRestriction);
							complexContent.getNamedItemsSequence().add( contentRestriction);
							break;							
						case EXTENSION:
							Extension contentExtension = ExtensionExpert.read(declaringSchema, reader);
							complexContent.setExtension( contentExtension);
							complexContent.getNamedItemsSequence().add( contentExtension);
							break;
						case ANNOTATION:
							Annotation annotation = AnnotationExpert.read(declaringSchema, reader);
							complexContent.setAnnotation(annotation);
							complexContent.getNamedItemsSequence().add( annotation);
							break;
						default:
							skip(reader);
					}
				}
				break;
				case XMLStreamConstants.END_ELEMENT : {
					return complexContent;
				}
				default: 
					break;
				}
			reader.next();
		}
		return complexContent;
	}

	public static void write( XMLStreamWriter writer, Namespace namespace, ComplexContent suspect) throws XMLStreamException {
		if (suspect == null)
			return;
		String prefix = namespace.getPrefix();
		writer.writeStartElement( prefix != null ? prefix + ":" + COMPLEX_CONTENT : COMPLEX_CONTENT);
		
		if (suspect.getMixed() || suspect.getMixedSpecified()) {
			writer.writeAttribute( MIXED, Boolean.toString( suspect.getMixed()));
		}
		
		writeAnyAttributes(writer, suspect.getAnyAttributes());
		
		for (GenericEntity ge : suspect.getNamedItemsSequence()) {
			if (ge instanceof ComplexContentRestriction) {
				ComplexContentRestrictionExpert.write(writer, namespace, (ComplexContentRestriction) ge);			
			}
			else if (ge instanceof Extension) {
				ExtensionExpert.write(writer, namespace, (Extension) ge);				
			}
			else {
				throw new IllegalStateException("unknown type [" + ge.getClass() + "] encountered");
			}
		}
		writer.writeEndElement();
	}
}
