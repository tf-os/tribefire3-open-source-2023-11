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
package com.braintribe.codec.marshaller.stax;

import javax.xml.stream.XMLStreamReader;

import org.xml.sax.Attributes;

public class XMLStreamReaderAttributes implements Attributes {

	private XMLStreamReader reader;
	
	public XMLStreamReaderAttributes(XMLStreamReader reader) {
		super();
		this.reader = reader;
	}


	@Override
	public int getLength() {
		return reader.getAttributeCount();
	}

	@Override
	public String getURI(int index) {
		return reader.getAttributeNamespace(index);
	}

	@Override
	public String getLocalName(int index) {
		return reader.getAttributeLocalName(index);
	}

	@Override
	public String getQName(int index) {
		return reader.getAttributePrefix(index) + reader.getAttributeLocalName(index);
	}

	@Override
	public String getType(int index) {
		return reader.getAttributeType(index);
	}

	@Override
	public String getValue(int index) {
		return reader.getAttributeValue(index);
	}

	@Override
	public int getIndex(String uri, String localName) {
		int count = reader.getAttributeCount();
		for (int i = 0; i < count; i++) {
			String ns = reader.getAttributeNamespace(i);
			String name = reader.getAttributeLocalName(i);
			if (ns.equals(uri) && name.equals(localName))
				return i;
		}
		
		return -1;
	}

	@Override
	public int getIndex(String qName) {
		int count = reader.getAttributeCount();
		for (int i = 0; i < count; i++) {
			String name = /*reader.getAttributePrefix(i) + */reader.getAttributeLocalName(i);
			if (name.equals(qName))
				return i;
		}
		
		return -1;
	}

	@Override
	public String getType(String uri, String localName) {
		int i = getIndex(uri, localName);
		return i != -1? reader.getAttributeType(i): null;
	}

	@Override
	public String getType(String qName) {
		int i = getIndex(qName);
		return i != -1? reader.getAttributeType(i): null;
	}

	@Override
	public String getValue(String uri, String localName) {
		int i = getIndex(uri, localName);
		return i != -1? reader.getAttributeValue(i): null;
	}

	@Override
	public String getValue(String qName) {
		int i = getIndex(qName);
		return i != -1? reader.getAttributeValue(i): null;
	}

}
