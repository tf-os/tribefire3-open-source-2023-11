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

import tribefire.extension.xml.schemed.model.xsd.Documentation;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.Schema;

public class DocumentationExpert extends AbstractSchemaExpert {
	
	public static Documentation read( Schema declaratingSchema, XMLStreamReader reader) throws XMLStreamException {
		StringBuffer buffer = new StringBuffer();

		Documentation documentation = Documentation.T.create();
		attach(documentation, declaratingSchema);
		
		Map<QName, String> attributes = readAttributes(reader);
		documentation.setSource( attributes.get(new QName(SOURCE)));
		documentation.setLang( attributes.get(new QName( LANGUAGE)));				
		
		// wind to next event
		reader.next();
		int i = 0;
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
		
				case XMLStreamConstants.START_ELEMENT :
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						default:
							skip(reader);
							break;					
					}
					i++;
					break;
				case XMLStreamConstants.CHARACTERS: {
					if (i == 1) {
						buffer.append( reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
					}
					break;
				}
				case XMLStreamConstants.END_ELEMENT : {
					
					documentation.setDocumentation( buffer.toString());
					return documentation;										
				}				
				default:
					break;
			}			
			reader.next();
		}
		return documentation;
	}
	
	public static void write( XMLStreamWriter writer, Namespace namespace, Documentation documentation) throws XMLStreamException { 
		if (documentation == null)
			return;
		String prefix = namespace.getPrefix();		
		writer.writeStartElement( prefix != null ? prefix + ":" + DOCUMENTATION : DOCUMENTATION);
		
		String source = documentation.getSource();
		if (source != null)
			writer.writeAttribute( SOURCE, source);
		String lang = documentation.getLang();
		if (lang != null)
			writer.writeAttribute( LANGUAGE, lang);
		
		writer.writeCharacters( documentation.getDocumentation());
		
		writer.writeEndElement();
	}
	
	
}
