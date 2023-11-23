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
package tribefire.extension.xml.schemed.marshaller.xsd.experts.restrictions;

import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringEscapeUtils;

import tribefire.extension.xml.schemed.marshaller.xsd.experts.AbstractSchemaExpert;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.model.xsd.restrictions.Pattern;

public class PatternExpert extends AbstractSchemaExpert {
	public static Pattern read( Schema declaringSchema, XMLStreamReader reader) throws XMLStreamException {
		
		Pattern pattern = Pattern.T.create();
		attach( pattern, declaringSchema);
		
		Map<QName,String> attributes = readAttributes(reader);		
		pattern.setValue( attributes.get( new QName( VALUE)));		
		reader.next();
		
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						default:
						skip(reader);									
					}										
					break;				
				}
				case XMLStreamConstants.END_ELEMENT : {
					return pattern;
				}
				default: 
					break;
				}
			reader.next();
		}
		return pattern;
	}
	
	public static void write( XMLStreamWriter writer, Namespace namespace, Pattern pattern) throws XMLStreamException {
		if (pattern == null)
			return;
		String prefix = namespace.getPrefix();
		writer.writeStartElement( prefix != null ? prefix + ":" + PATTERN : PATTERN);
		writer.writeAttribute( VALUE, "" + StringEscapeUtils.escapeXml(pattern.getValue()));
		writer.writeEndElement();
	}
}
