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
import tribefire.extension.xml.schemed.model.xsd.List;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.model.xsd.SimpleType;

public class ListExpert extends AbstractSchemaExpert {

	public static List read( Schema declaringSchema, XMLStreamReader reader) throws XMLStreamException {
		
		List list = List.T.create();
		attach( list, declaringSchema);
		
		Map<QName,String> attributes = readAttributes(reader);		
		list.setSimpleTypeReference(  QNameExpert.parse(attributes.get( new QName( ITEM_TYPE))));
		
		readAnyAttributes( list.getAnyAttributes(), attributes, ID, ITEM_TYPE);
		
		reader.next();
		
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case ANNOTATION:
							Annotation annotation = AnnotationExpert.read( declaringSchema, reader);
							list.setAnnotation(annotation);
							list.getNamedItemsSequence().add(annotation);
						break;
						case SIMPLE_TYPE:
							SimpleType simpleType = SimpleTypeExpert.read(declaringSchema, reader);
							list.setSimpleType( simpleType);
							list.getNamedItemsSequence().add( simpleType);
							break;							
						default:
							skip(reader);									
					}										
					break;				
				}
				case XMLStreamConstants.END_ELEMENT : {
					return list;
				}
				default: 
					break;
				}
			reader.next();
		}
		return list;
	}
	
	public static void write( XMLStreamWriter writer, Namespace namespace, List list) throws XMLStreamException {
		if (list == null)
			return;
		String prefix = namespace.getPrefix();
		writer.writeStartElement( prefix != null ? prefix + ":" + LIST : LIST);
		tribefire.extension.xml.schemed.model.xsd.QName simpleTypeReference = list.getSimpleTypeReference();
		if (simpleTypeReference != null) {
			writer.writeAttribute( ITEM_TYPE, QNameExpert.toString( simpleTypeReference));
		}
		writeAnyAttributes(writer, list.getAnyAttributes());
		
		for (GenericEntity ge : list.getNamedItemsSequence()) {
			if (ge instanceof Annotation) {
				AnnotationExpert.write(writer, namespace, (Annotation) ge);
			}
			else if (ge instanceof SimpleType) {
				SimpleTypeExpert.write(writer, namespace, (SimpleType) ge);
			}
			else {
				throw new IllegalStateException("unknown type [" + ge.getClass() + "] encountered");
			}
		}
		
		writer.writeEndElement();
	}
}
