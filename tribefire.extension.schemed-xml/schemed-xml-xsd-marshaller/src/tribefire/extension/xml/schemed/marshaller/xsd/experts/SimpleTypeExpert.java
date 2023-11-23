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
import tribefire.extension.xml.schemed.model.xsd.List;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.model.xsd.SimpleType;
import tribefire.extension.xml.schemed.model.xsd.SimpleTypeRestriction;
import tribefire.extension.xml.schemed.model.xsd.Union;

public class SimpleTypeExpert extends AbstractSchemaExpert {
	
	public static SimpleType read( Schema declaringSchema, XMLStreamReader reader) throws XMLStreamException {
		// wind to next event
		SimpleType simpleType = SimpleType.T.create();
		attach(simpleType, declaringSchema);
		Map<QName, String> attributes = readAttributes(reader);
		simpleType.setName( attributes.get( new QName(NAME)));		
		
		readAnyAttributes( simpleType.getAnyAttributes(), attributes, ID, NAME);
		reader.next();
		
		while (reader.hasNext()) {
			
			switch (reader.getEventType()) {
			
				case XMLStreamConstants.START_ELEMENT :												
					String tag = reader.getName().getLocalPart();
					switch (tag) {
					case RESTRICTION:					
						SimpleTypeRestriction simpleTypeRestriction = SimpleTypeRestrictionExpert.read( declaringSchema, reader);
						simpleType.setRestriction( simpleTypeRestriction);
						simpleType.getNamedItemsSequence().add( simpleTypeRestriction);
						// back pointer
						simpleTypeRestriction.setSimpleType(simpleType);
						break;
					case LIST:
						List list = ListExpert.read(declaringSchema, reader);
						simpleType.setList( list);
						simpleType.getNamedItemsSequence().add( list);
						break;
					case UNION:
						Union union = UnionExpert.read( declaringSchema, reader);
						simpleType.setUnion( union);
						simpleType.getNamedItemsSequence().add( union);
						break;
					case ANNOTATION:
						Annotation annotation = AnnotationExpert.read( declaringSchema, reader);
						simpleType.setAnnotation(annotation);
						simpleType.getNamedItemsSequence().add( annotation);
						break;
						default:
							skip(reader);
						break;
					}				
				break;
				
				case XMLStreamConstants.END_ELEMENT : {
					return simpleType;
				}		
			
				default: 
					break;
			}
			reader.next();
		}
		return simpleType;
	}


	public static void write(XMLStreamWriter writer, Namespace namespace, SimpleType simpleType) throws XMLStreamException{
		if (simpleType == null)
			return;
		String prefix = namespace.getPrefix();
		writer.writeStartElement( prefix != null ? prefix + ":" + SIMPLE_TYPE : SIMPLE_TYPE);
		
		String name = simpleType.getName();
		if (name != null) {
			writer.writeAttribute( NAME, name);
		}
		writeAnyAttributes( writer, simpleType.getAnyAttributes());
		for (GenericEntity ge : simpleType.getNamedItemsSequence()) {
			if (ge instanceof SimpleTypeRestriction) {
				SimpleTypeRestrictionExpert.write(writer, namespace, (SimpleTypeRestriction) ge);				
			}
			else if (ge instanceof List) {
				ListExpert.write(writer, namespace,  (List) ge);						
			}
			else if (ge instanceof Union) {
				UnionExpert.write( writer, namespace, (Union) ge);				
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
