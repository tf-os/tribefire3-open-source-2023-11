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
package tribefire.extension.xml.schemed.marshaller.xml.api;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.model.generic.GenericEntity;

/**
 * the actual workhorse of the xml processing 
 * @author pit
 *
 */
public interface SchemedXmlProcessor {		

	/**
	 * reads the XML accessed via the {@link XMLStreamReader} and returns the content as a {@link GenericEntity}
	 * @param reader - the {@link XMLStreamReader}
	 * @return - the {@link GenericEntity} extracted 
	 * @throws XMLStreamException - if anything goes wrong
	 */
	GenericEntity read(XMLStreamReader reader, GmDeserializationOptions options) throws XMLStreamException;

	/**
	 * writes the {@link GenericEntity} to the XML accessed via the {@link XMLStreamWriter}
	 * @param writer - the {@link XMLStreamWriter}
	 * @param value - the {@link GenericEntity} to write
	 * @throws XMLStreamException
	 */
	void write( XMLStreamWriter writer, GenericEntity value, GmSerializationOptions options) throws XMLStreamException;

}