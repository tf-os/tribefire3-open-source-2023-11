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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class IndentingXMLStreamWriter implements XMLStreamWriter {
	private XMLStreamWriter delegate;
	private int depth = 0;
	private List<IndentMode> hasChildElement = new ArrayList<IndentMode>(10);
	private String indentString = " ";
	private static final String LINEFEED_CHAR = "\n";
	private List<String> indents = new ArrayList<String>();
	private enum IndentMode { none, indent, blocked }
	
	public IndentingXMLStreamWriter(XMLStreamWriter delegate) {
		super();
		this.delegate = delegate;
	}
	
	public void setIndentationSpaces(int spaces) {
		char spaceArray[] = new char[spaces];
		Arrays.fill(spaceArray, ' ');
		indentString = new String(spaceArray);
	}
	
	private void startElement(String elementName) throws XMLStreamException {
		boolean blockIndent = false;
		if (depth > 0) {
			blockIndent = hasChildElement.set(depth - 1, IndentMode.indent) == IndentMode.blocked;
			if (blockIndent)
				hasChildElement.set(depth - 1, IndentMode.blocked);
		}
		// reset state of current node
		if (elementName.equals("m")) {
			hasChildElement.add(IndentMode.blocked);
		}
		else {
			hasChildElement.add(IndentMode.none);
		}
		// indent for current depth
		if (!blockIndent) {
			delegate.writeCharacters(LINEFEED_CHAR);
			delegate.writeCharacters(getIndent(depth));
		}
		depth++;
	}

	private void endElement() throws XMLStreamException {
		depth--;
		if (hasChildElement.remove(depth) == IndentMode.indent) {
			delegate.writeCharacters(LINEFEED_CHAR);
			delegate.writeCharacters(getIndent(depth));
		}
	}

	private void emptyElement() throws XMLStreamException {
		// update state of parent node
		if (depth > 0) {
			if (hasChildElement.set(depth - 1, IndentMode.indent) == IndentMode.blocked)
				hasChildElement.set(depth - 1, IndentMode.blocked);
		}
		// indent for current depth
		delegate.writeCharacters(LINEFEED_CHAR);
		delegate.writeCharacters(getIndent(depth));
	}

	@Override
	public void writeStartElement(String localName) throws XMLStreamException {
		startElement(localName);
		delegate.writeStartElement(localName);
	}

	@Override
	public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
		startElement(localName);
		delegate.writeStartElement(namespaceURI, localName);
	}

	@Override
	public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		startElement(localName);
		delegate.writeStartElement(prefix, localName, namespaceURI);
	}

	@Override
	public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
		emptyElement();
		delegate.writeEmptyElement(namespaceURI, localName);
	}

	@Override
	public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		emptyElement();
		delegate.writeEmptyElement(prefix, localName, namespaceURI);
	}

	@Override
	public void writeEmptyElement(String localName) throws XMLStreamException {
		emptyElement();
		delegate.writeEmptyElement(localName);
	}

	@Override
	public void writeEndElement() throws XMLStreamException {
		endElement();
		delegate.writeEndElement();
	}

	@Override
	public void writeEndDocument() throws XMLStreamException {
		delegate.writeEndDocument();
	}

	@Override
	public void close() throws XMLStreamException {
		delegate.close();
	}

	@Override
	public void flush() throws XMLStreamException {
		delegate.flush();
	}

	@Override
	public void writeAttribute(String localName, String value) throws XMLStreamException {
		delegate.writeAttribute(localName, value);
	}

	@Override
	public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
		delegate.writeAttribute(prefix, namespaceURI, localName, value);
	}

	@Override
	public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
		delegate.writeAttribute(namespaceURI, localName, value);
	}

	@Override
	public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
		delegate.writeNamespace(prefix, namespaceURI);
	}

	@Override
	public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
		delegate.writeDefaultNamespace(namespaceURI);
	}

	@Override
	public void writeComment(String data) throws XMLStreamException {
		delegate.writeComment(data);
	}

	@Override
	public void writeProcessingInstruction(String target) throws XMLStreamException {
		delegate.writeProcessingInstruction(target);
	}

	@Override
	public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
		delegate.writeProcessingInstruction(target, data);
	}

	@Override
	public void writeCData(String data) throws XMLStreamException {
		delegate.writeCData(data);
	}

	@Override
	public void writeDTD(String dtd) throws XMLStreamException {
		delegate.writeDTD(dtd);
	}

	@Override
	public void writeEntityRef(String name) throws XMLStreamException {
		delegate.writeEntityRef(name);
	}

	@Override
	public void writeStartDocument() throws XMLStreamException {
		delegate.writeStartDocument();
	}

	@Override
	public void writeStartDocument(String version) throws XMLStreamException {
		delegate.writeStartDocument(version);
	}

	@Override
	public void writeStartDocument(String encoding, String version) throws XMLStreamException {
		delegate.writeStartDocument(encoding, version);
	}

	@Override
	public void writeCharacters(String text) throws XMLStreamException {
		delegate.writeCharacters(text);
	}

	@Override
	public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
		delegate.writeCharacters(text, start, len);
	}

	@Override
	public String getPrefix(String uri) throws XMLStreamException {
		return delegate.getPrefix(uri);
	}

	@Override
	public void setPrefix(String prefix, String uri) throws XMLStreamException {
		delegate.setPrefix(prefix, uri);
	}

	@Override
	public void setDefaultNamespace(String uri) throws XMLStreamException {
		delegate.setDefaultNamespace(uri);
	}

	@Override
	public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
		delegate.setNamespaceContext(context);
	}

	@Override
	public NamespaceContext getNamespaceContext() {
		return delegate.getNamespaceContext();
	}

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		return delegate.getProperty(name);
	}

	private String getIndent(int d) {
		if (d == indents.size()) {
			String indent;
			if (d > 0) {
				StringBuilder builder = new StringBuilder();
				builder.append(indents.get(d - 1));
				builder.append(indentString);
				indent = builder.toString();
			}
			else {
				indent = "";
			}

			indents.add(indent);
			return indent;
		}
		else
			return indents.get(d);
	}

}
