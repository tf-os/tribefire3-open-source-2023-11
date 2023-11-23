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

import tribefire.extension.xml.schemed.model.xsd.AppInfo;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.Schema;

public class AppInfoExpert extends AbstractSchemaExpert {
	public static AppInfo read( Schema declaringSchema, XMLStreamReader reader) throws XMLStreamException {
		AppInfo appInfo = AppInfo.T.create();
		attach(appInfo, declaringSchema);

		Map<QName, String> attributes = readAttributes(reader);
		appInfo.setSource( attributes.get( new QName(SOURCE)));	
				
		reader.next();
		
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT :			
				break;
				case XMLStreamConstants.END_ELEMENT : {
					return appInfo;
				}				
				default: 
					break;
				}
				reader.next();
		}
		return appInfo;
	}
	
	public static void write( XMLStreamWriter writer, Namespace namespace, AppInfo appinfo) throws XMLStreamException {
		if (appinfo == null)
			return;
		String prefix = namespace.getPrefix();		
		writer.writeStartElement( prefix != null ? prefix + ":" + DOCUMENTATION : DOCUMENTATION);
		
		String source = appinfo.getSource();
		if (source != null)
			writer.writeAttribute( SOURCE, source);

		writer.writeCharacters( appinfo.getContents());
		
		writer.writeEndElement();
	}
}
