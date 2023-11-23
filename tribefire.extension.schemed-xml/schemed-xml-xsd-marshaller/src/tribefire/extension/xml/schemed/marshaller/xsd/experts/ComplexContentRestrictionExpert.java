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

import tribefire.extension.xml.schemed.model.xsd.All;
import tribefire.extension.xml.schemed.model.xsd.Annotation;
import tribefire.extension.xml.schemed.model.xsd.Attribute;
import tribefire.extension.xml.schemed.model.xsd.AttributeGroup;
import tribefire.extension.xml.schemed.model.xsd.Choice;
import tribefire.extension.xml.schemed.model.xsd.ComplexContentRestriction;
import tribefire.extension.xml.schemed.model.xsd.Group;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.model.xsd.Sequence;

public class ComplexContentRestrictionExpert extends AbstractSchemaExpert {

	public static ComplexContentRestriction read( Schema declaringSchema, XMLStreamReader reader) throws XMLStreamException {
		// wind to next event
		ComplexContentRestriction complexContentRestriction = ComplexContentRestriction.T.create();
		attach(complexContentRestriction, declaringSchema);
		
		Map<QName, String> attributes = readAttributes(reader);
		complexContentRestriction.setBase(attributes.get(new QName(BASE)));
		
		readAnyAttributes( complexContentRestriction.getAnyAttributes(), attributes, ID, BASE);

		reader.next();

		while (reader.hasNext()) {

			switch (reader.getEventType()) {

			case XMLStreamConstants.START_ELEMENT:
				String tag = reader.getName().getLocalPart();
				switch (tag) {
				case ALL:
					All all = AllExpert.read( declaringSchema, reader);
					complexContentRestriction.setAll(all);
					complexContentRestriction.getNamedItemsSequence().add( all);
					break;
				case GROUP:
					Group group = GroupExpert.read( declaringSchema, reader);
					complexContentRestriction.setGroup( group);
					complexContentRestriction.getNamedItemsSequence().add( group);
					break;
				case CHOICE:					
					Choice choice = ChoiceExpert.read( declaringSchema, reader);
					complexContentRestriction.setChoice( choice);
					complexContentRestriction.getNamedItemsSequence().add( choice);
					break;
				case SEQUENCE:				
					Sequence sequence = SequenceExpert.read( declaringSchema, reader);					
					complexContentRestriction.setSequence( sequence);
					complexContentRestriction.getNamedItemsSequence().add( sequence);
					break;
				case ATTRIBUTE:
					Attribute attribute = AttributeExpert.read( declaringSchema, reader);
					complexContentRestriction.getAttributes().add( attribute);
					complexContentRestriction.getNamedItemsSequence().add( attribute);
					break;
				case ATTRIBUTE_GROUP :
					AttributeGroup attributeGroup = AttributeGroupExpert.read( declaringSchema, reader);
					complexContentRestriction.getAttributeGroups().add( attributeGroup);
					complexContentRestriction.getNamedItemsSequence().add( attributeGroup);
					break;
				case ANNOTATION:
					Annotation annotation = AnnotationExpert.read( declaringSchema, reader);
					complexContentRestriction.setAnnotation(annotation);
					complexContentRestriction.getNamedItemsSequence().add( annotation);
					break;
				default:
					skip(reader);
					break;
				}
				break;

			case XMLStreamConstants.END_ELEMENT: {
				return complexContentRestriction;
			}

			default:
				break;
			}
			reader.next();
		}
		return complexContentRestriction;
	}

	public static void write(XMLStreamWriter writer, Namespace namespace, ComplexContentRestriction restriction) throws XMLStreamException {
		
		if (restriction == null)
			return;
		
		String prefix = namespace.getPrefix();
		writer.writeStartElement(prefix != null ? prefix + ":" + RESTRICTION : RESTRICTION);
		writer.writeAttribute(BASE, restriction.getBase());
		
		writeAnyAttributes(writer, restriction.getAnyAttributes());
		
		for (GenericEntity ge : restriction.getNamedItemsSequence()) {
			if (ge instanceof All) {
				AllExpert.write(writer, namespace, (All) ge);				
			}
			else if (ge instanceof Group) {
				GroupExpert.write(writer, namespace, (Group) ge);			
			}
			else if (ge instanceof Choice) {
				ChoiceExpert.write( writer, namespace, (Choice) ge);				
			}
			else if (ge instanceof Sequence) {
				SequenceExpert.write(writer, namespace, (Sequence) ge);				
			}
			else if (ge instanceof Attribute) {
				AttributeExpert.write(writer, namespace, (Attribute) ge);				
			}
			else if (ge instanceof AttributeGroup) {
				AttributeGroupExpert.write(writer, namespace, (AttributeGroup) ge);				
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
