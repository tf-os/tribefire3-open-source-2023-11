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
import tribefire.extension.xml.schemed.model.xsd.AttributeGroup;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.Schema;

public class AttributeGroupExpert extends AbstractSchemaExpert {
	
	public static AttributeGroup read( Schema declaringSchema, XMLStreamReader reader) throws XMLStreamException {
		// wind to next event
		AttributeGroup group = AttributeGroup.T.create();
		attach(group, declaringSchema);
		
		Map<QName, String> attributes = readAttributes(reader);
		group.setName( attributes.get( new QName(NAME)));
		
		group.setId( attributes.get( new QName( ID)));
		
		String elementReference = attributes.get( new QName(REF));
		group.setRef(QNameExpert.parse(elementReference));
		
		readAnyAttributes( group.getAnyAttributes(), attributes, ID, NAME, REF);
		
		reader.next();
		
		while (reader.hasNext()) {
			
			switch (reader.getEventType()) {
			
				case XMLStreamConstants.START_ELEMENT :												
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case ATTRIBUTE:
							Attribute attribute2 = AttributeExpert.read( declaringSchema, reader);
							group.getAttributes().add( attribute2);
							group.getNamedItemsSequence().add( attribute2);
							break;
						case ATTRIBUTE_GROUP:
							AttributeGroup attributeGroup = AttributeGroupExpert.read( declaringSchema, reader);
							group.getAttributeGroups().add( attributeGroup);
							group.getNamedItemsSequence().add( attributeGroup);
							break;											
						case ANNOTATION:
							Annotation annotation = AnnotationExpert.read(declaringSchema, reader);
							group.setAnnotation(annotation);
							group.getNamedItemsSequence().add( annotation);
							break;
						default:
							skip(reader);
							break;
					}				
				break;
				
				case XMLStreamConstants.END_ELEMENT : {
					return group;
				}		
			
				default: 
					break;
			}
			reader.next();
		}
		return group;
	}
		
	public static void write( XMLStreamWriter writer, Namespace namespace, AttributeGroup group) throws XMLStreamException {
		if (group == null)
			return;
		String prefix = namespace.getPrefix();
		writer.writeStartElement( prefix != null ? prefix + ":" + ATTRIBUTE_GROUP : ATTRIBUTE_GROUP);

		String name = group.getName();
		if (name != null) {
			writer.writeAttribute( NAME, name);
		}
		else {
			tribefire.extension.xml.schemed.model.xsd.QName elementReference = group.getRef();
			if (elementReference != null) {
				writer.writeAttribute( REF, QNameExpert.toString(elementReference));
			}
		}
		for (GenericEntity ge : group.getNamedItemsSequence()) {
			if (ge instanceof Attribute) {
				AttributeExpert.write(writer, namespace, (Attribute) ge);
			}
			else if (ge instanceof AttributeGroup) {
				AttributeGroupExpert.write(writer, namespace, (AttributeGroup) ge);
				
			}
			else {
				throw new IllegalStateException("unknown type [" + ge.getClass() + "] encountered");
			}
		}
		writer.writeEndElement();
	}
}
