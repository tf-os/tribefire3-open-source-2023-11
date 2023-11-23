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

import tribefire.extension.xml.schemed.marshaller.xsd.experts.restrictions.EnumerationExpert;
import tribefire.extension.xml.schemed.marshaller.xsd.experts.restrictions.NumericRestrictionValueExpert;
import tribefire.extension.xml.schemed.marshaller.xsd.experts.restrictions.PatternExpert;
import tribefire.extension.xml.schemed.marshaller.xsd.experts.restrictions.WhiteSpaceRestrictionValueExpert;
import tribefire.extension.xml.schemed.model.xsd.Annotation;
import tribefire.extension.xml.schemed.model.xsd.Attribute;
import tribefire.extension.xml.schemed.model.xsd.AttributeGroup;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.model.xsd.SimpleContentRestriction;
import tribefire.extension.xml.schemed.model.xsd.restrictions.Enumeration;
import tribefire.extension.xml.schemed.model.xsd.restrictions.FractionDigits;
import tribefire.extension.xml.schemed.model.xsd.restrictions.Length;
import tribefire.extension.xml.schemed.model.xsd.restrictions.MaxExclusive;
import tribefire.extension.xml.schemed.model.xsd.restrictions.MaxInclusive;
import tribefire.extension.xml.schemed.model.xsd.restrictions.MaxLength;
import tribefire.extension.xml.schemed.model.xsd.restrictions.MinExclusive;
import tribefire.extension.xml.schemed.model.xsd.restrictions.MinInclusive;
import tribefire.extension.xml.schemed.model.xsd.restrictions.MinLength;
import tribefire.extension.xml.schemed.model.xsd.restrictions.Pattern;
import tribefire.extension.xml.schemed.model.xsd.restrictions.TotalDigits;
import tribefire.extension.xml.schemed.model.xsd.restrictions.Whitespace;

public class SimpleContentRestrictionExpert extends AbstractSchemaExpert {
	public static SimpleContentRestriction read( Schema declaringSchema, XMLStreamReader reader) throws XMLStreamException {
		// wind to next event
		SimpleContentRestriction simpleContentRestriction = SimpleContentRestriction.T.create();
		attach(simpleContentRestriction, declaringSchema);
		Map<QName, String> attributes = readAttributes(reader);
		simpleContentRestriction.setBase(attributes.get(new QName(BASE)));
		
		readAnyAttributes( simpleContentRestriction.getAnyAttributes(), attributes, ID, BASE);
		

		reader.next();

		while (reader.hasNext()) {

			switch (reader.getEventType()) {

			case XMLStreamConstants.START_ELEMENT:
				String tag = reader.getName().getLocalPart();
				switch (tag) {
				case MIN_EXCLUSIVE:
					MinExclusive minExclusive = MinExclusive.T.create();
					attach(minExclusive, declaringSchema);
					NumericRestrictionValueExpert.read(declaringSchema, minExclusive, reader);
					simpleContentRestriction.setMinExclusive(minExclusive);
					simpleContentRestriction.getNamedItemsSequence().add( minExclusive);
					break;
				case MIN_INCLUSIVE:
					MinInclusive minInclusive = MinInclusive.T.create();
					attach(minInclusive, declaringSchema);
					NumericRestrictionValueExpert.read(declaringSchema, minInclusive, reader);
					simpleContentRestriction.setMinInclusive(minInclusive);
					simpleContentRestriction.getNamedItemsSequence().add( minInclusive);
					break;
				case MAX_EXCLUSIVE:
					MaxExclusive maxExclusive = MaxExclusive.T.create();
					attach(maxExclusive, declaringSchema);
					NumericRestrictionValueExpert.read(declaringSchema, maxExclusive, reader);
					simpleContentRestriction.setMaxExclusive(maxExclusive);
					simpleContentRestriction.getNamedItemsSequence().add( maxExclusive);
					break;
				case MAX_INCLUSIVE:
					MaxInclusive maxInclusive = MaxInclusive.T.create();
					attach(maxInclusive, declaringSchema);
					NumericRestrictionValueExpert.read(declaringSchema, maxInclusive, reader);
					simpleContentRestriction.setMaxInclusive(maxInclusive);
					simpleContentRestriction.getNamedItemsSequence().add( maxInclusive);
					break;
				case LENGTH:
					Length length = Length.T.create();
					attach(length, declaringSchema);
					NumericRestrictionValueExpert.read(declaringSchema, length, reader);
					simpleContentRestriction.setLength(length);
					simpleContentRestriction.getNamedItemsSequence().add( length);
					break;
				case MIN_LENGTH:
					MinLength minlength = MinLength.T.create();
					attach(minlength, declaringSchema);
					NumericRestrictionValueExpert.read(declaringSchema, minlength, reader);
					simpleContentRestriction.setMinLength(minlength);
					simpleContentRestriction.getNamedItemsSequence().add( minlength);
					break;
				case MAX_LENGTH:
					MaxLength maxlength = MaxLength.T.create();
					attach(maxlength, declaringSchema);
					NumericRestrictionValueExpert.read(declaringSchema, maxlength, reader);
					simpleContentRestriction.setMaxLength(maxlength);
					simpleContentRestriction.getNamedItemsSequence().add( maxlength);
					break;
				case TOTAL_DIGITS:
					TotalDigits totalDigits = TotalDigits.T.create();
					attach(totalDigits, declaringSchema);
					NumericRestrictionValueExpert.read(declaringSchema, totalDigits, reader);
					simpleContentRestriction.setTotalDigits(totalDigits);
					simpleContentRestriction.getNamedItemsSequence().add( totalDigits);
					break;
				case FRACTION_DIGITS:
					FractionDigits fractionDigits = FractionDigits.T.create();
					attach(fractionDigits, declaringSchema);
					NumericRestrictionValueExpert.read(declaringSchema, fractionDigits, reader);
					simpleContentRestriction.setFractionDigits(fractionDigits);
					simpleContentRestriction.getNamedItemsSequence().add( fractionDigits);
					break;
				case WHITE_SPACE:
					Whitespace whitespace = WhiteSpaceRestrictionValueExpert.read(declaringSchema, reader);					
					simpleContentRestriction.setWhitespace(whitespace);
					simpleContentRestriction.getNamedItemsSequence().add( whitespace);
					break;
				case PATTERN:					
					Pattern pattern = PatternExpert.read( declaringSchema, reader);					
					simpleContentRestriction.setPattern( pattern);
					simpleContentRestriction.getNamedItemsSequence().add( pattern);
					break;
				case ENUMERATIION:					
					Enumeration enumeration = EnumerationExpert.read( declaringSchema, reader);					
					simpleContentRestriction.getEnumerations().add( enumeration);					
					simpleContentRestriction.getNamedItemsSequence().add( enumeration);
					break;
				case ATTRIBUTE:
					Attribute attribute = AttributeExpert.read( declaringSchema, reader);
					simpleContentRestriction.getAttributes().add( attribute);
					simpleContentRestriction.getNamedItemsSequence().add( attribute);
					break;
				case ATTRIBUTE_GROUP :
					AttributeGroup attributeGroup = AttributeGroupExpert.read( declaringSchema, reader);
					simpleContentRestriction.getAttributeGroups().add( attributeGroup);
					simpleContentRestriction.getNamedItemsSequence().add( attributeGroup);					
					break;
				case ANNOTATION:
					Annotation annotation = AnnotationExpert.read( declaringSchema, reader);
					simpleContentRestriction.setAnnotation(annotation);
					simpleContentRestriction.getNamedItemsSequence().add(annotation);
					break;
				default:
					skip(reader);
					break;
				}
				break;

			case XMLStreamConstants.END_ELEMENT: {
				return simpleContentRestriction;
			}

			default:
				break;
			}
			reader.next();
		}
		return simpleContentRestriction;
	}

	public static void write(XMLStreamWriter writer, Namespace namespace, SimpleContentRestriction restriction) throws XMLStreamException {
		
		if (restriction == null)
			return;
		
		String prefix = namespace.getPrefix();
		writer.writeStartElement( prefix != null ? prefix + ":" + RESTRICTION : RESTRICTION);
		writer.writeAttribute(BASE, restriction.getBase());

		for (GenericEntity ge : restriction.getNamedItemsSequence()) {
			if (ge instanceof MinExclusive) {
				NumericRestrictionValueExpert.write(writer, namespace, (MinExclusive) ge, MIN_EXCLUSIVE);				
			}
			else if (ge instanceof MinInclusive) {
				NumericRestrictionValueExpert.write(writer, namespace, (MinInclusive) ge, MIN_INCLUSIVE);				
			}
			else if (ge instanceof MaxExclusive) {
				NumericRestrictionValueExpert.write(writer, namespace, (MaxExclusive) ge, MAX_EXCLUSIVE);				
			}
			else if (ge instanceof MaxInclusive) {
				NumericRestrictionValueExpert.write(writer, namespace, (MaxInclusive) ge, MAX_INCLUSIVE);				
			}
			else if (ge instanceof Length) {
				NumericRestrictionValueExpert.write(writer, namespace, (Length) ge, LENGTH);				
			}
			else if (ge instanceof MinLength) {
				NumericRestrictionValueExpert.write(writer, namespace, (MinLength)ge, MIN_LENGTH);			
			}
			else if (ge instanceof MaxLength) {
				NumericRestrictionValueExpert.write(writer, namespace, (MaxLength) ge, MAX_LENGTH);				
			}
			else if (ge instanceof TotalDigits) {
				NumericRestrictionValueExpert.write(writer, namespace, (TotalDigits) ge, TOTAL_DIGITS);				
			}
			else if (ge instanceof FractionDigits) {
				NumericRestrictionValueExpert.write(writer, namespace, (FractionDigits) ge, FRACTION_DIGITS);				
			}
			else if (ge instanceof Pattern) {
				PatternExpert.write(writer, namespace, (Pattern) ge);				
			}
			else if (ge instanceof Enumeration) {
				EnumerationExpert.write( writer, namespace, (Enumeration) ge);			
			}
			else if (ge instanceof Whitespace){
				WhiteSpaceRestrictionValueExpert.write(writer, namespace, restriction.getWhitespace());				
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
