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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.braintribe.logging.Logger;

import tribefire.extension.xml.schemed.marshaller.commons.QNameExpert;
import tribefire.extension.xml.schemed.marshaller.xsd.HasSchemaTokens;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.model.xsd.SchemaEntity;

/**
 * an abstract common base class for all experts 
 * @author pit
 *
 */
public abstract class AbstractSchemaExpert implements HasSchemaTokens  {
	private static Logger log = Logger.getLogger(AbstractSchemaExpert.class);
/**
	 * read the text content of a tag 
	 * @param reader
	 * @return
	 * @throws XMLStreamException
	 */
	protected static String extractString( XMLStreamReader reader) throws XMLStreamException {
		StringBuffer buffer = new StringBuffer();
		while (reader.hasNext()) {
			
			int eventType = reader.getEventType();
			switch (eventType) {
				case XMLStreamConstants.END_ELEMENT : {
					return buffer.toString();
				}
				
				case XMLStreamConstants.CHARACTERS : {
					buffer.append( reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
					break;
				}
			}
			reader.next();
		}
		return null;
	}
	
	protected static void readAnyAttributes(Map<tribefire.extension.xml.schemed.model.xsd.QName, String> anyAttributes, Map<QName, String> attributes, String ... reservedNames) {
		List<String> knownAttributes = new ArrayList<>(Arrays.asList( ELEMENT_FORM_DEFAULT, ATTRIBUTE_FORM_DEFAULT, TARGET_NAMESPACE)); 
		if (reservedNames != null) {
			for (String name : reservedNames) {
				knownAttributes.add( name);
			}
		}
		// transfer generic 
		for (Entry<QName, String> entry : attributes.entrySet()) {
			if (!knownAttributes.contains( entry.getKey().getLocalPart())) {
				anyAttributes.put( QNameExpert.parse( entry.getKey()), entry.getValue());
			}
		}	
	}
	
	protected static void writeAnyAttributes(XMLStreamWriter writer, Map<tribefire.extension.xml.schemed.model.xsd.QName, String> anyAttributes) throws XMLStreamException {
		for (Entry<tribefire.extension.xml.schemed.model.xsd.QName, String> entry : anyAttributes.entrySet()) {
			writer.writeAttribute( QNameExpert.toString( entry.getKey()), entry.getValue());
		}				
	}

	/**
	 * skip a tag - careful : this requires that the caller DOES call reader.next() before it actually starts processing,
	 * otherwise, this here will be called out of sequence and gobble-up any tags.. 
	 * @param reader
	 * @throws XMLStreamException
	 */
	protected static void skip( XMLStreamReader reader) throws XMLStreamException {
		if (log.isDebugEnabled()) {			
			log.debug("initial ->" + reader.getLocalName());
		}
		Stack<String> stack = new Stack<>();
		stack.push(reader.getLocalName());
		
		reader.next();
		while (reader.hasNext()) {
			int eventType = reader.getEventType();
			switch (eventType) {
				case XMLStreamConstants.START_ELEMENT: {				
					if (log.isDebugEnabled()) {			
						log.debug("sub tag " + reader.getLocalName());
					}
					stack.push(reader.getLocalName());
					break;
				}
			
				case XMLStreamConstants.END_ELEMENT : {
					stack.pop();
					if (stack.isEmpty())
						return; 
				}
				default:
					break;
			}
			reader.next();
		}
	}
	
	
	protected static Map<QName,String> readAttributes( XMLStreamReader reader) {
		Map<QName, String> attributes = new HashMap<>();
		int attributeCount = reader.getAttributeCount();
		for (int i = 0; i < attributeCount; i++) {
			//String namespace = reader.getAttributeNamespace(i);			
			QName name = reader.getAttributeName(i);
			String value = reader.getAttributeValue(i);
			attributes.put( name, value);
		}
		return attributes;
	}
	
	protected static String readSingleAttribute( XMLStreamReader reader, String tag) {
		Map<QName, String> attributes = readAttributes(reader);
		QName key = new QName(tag);
		return attributes.get( key);
	}
	
	protected static List<Namespace> getNamespaces( XMLStreamReader reader) {
		List<Namespace> result = new ArrayList<>();
		int namespaceCount = reader.getNamespaceCount();
		for (int i = 0; i < namespaceCount; i++) {
			Namespace namespace = Namespace.T.create();
			namespace.setPrefix( reader.getNamespacePrefix(i));
			namespace.setUri( reader.getNamespaceURI(i));			
			result.add( namespace);
		}
		return result;		
	}
	/**
	 * write a tag's value (currently not used)
	 * @param writer
	 * @param tag
	 * @param value
	 * @throws XMLStreamException
	 */
	protected static void write( XMLStreamWriter writer, String tag, String value) throws XMLStreamException {
		writer.writeStartElement( tag);
		writer.writeCharacters( value);
		writer.writeEndElement();
	}
		
	protected static void attach( SchemaEntity entity, Schema schema) {
		entity.setDeclaringSchema(schema);
		schema.getEntities().add(entity);
	}
}
