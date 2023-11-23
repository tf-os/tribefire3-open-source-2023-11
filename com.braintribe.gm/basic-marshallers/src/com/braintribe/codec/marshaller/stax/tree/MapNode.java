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
package com.braintribe.codec.marshaller.stax.tree;

import java.io.IOException;
import java.io.Writer;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.stax.PrettinessSupport;

public class MapNode extends ValueStaxNode {
	private static final char[] startPropElement = "<M p='".toCharArray();
	private static final char[] startPropElementClose = "'>".toCharArray();
	private static final char[] startElement = "<M>".toCharArray();

	private static final char[] endElement = "</M>".toCharArray();

	private static final char[] emptyElement = "<M/>".toCharArray();

	private static final char[] startEntryElement = "<m>".toCharArray();
	private static final char[] endEntryElement = "</m>".toCharArray();

	private final ValueStaxNode[] nodes;

	public MapNode(ValueStaxNode[] nodes) {
		this.nodes = nodes;
	}

	@Override
	void write(Writer writer, PrettinessSupport prettinessSupport, String propertyName, int indent) throws IOException, MarshallException {
		writer.write(startPropElement);
		writer.write(propertyName);
		writer.write(startPropElementClose);

		finishElement(writer, prettinessSupport, indent);
	}

	@Override
	public void write(Writer writer, PrettinessSupport prettinessSupport, int indent) throws IOException, MarshallException {
		if (nodes.length == 0) {
			writer.write(emptyElement);

		} else {
			writer.write(startElement);

			finishElement(writer, prettinessSupport, indent);
		}
	}

	private void finishElement(Writer writer, PrettinessSupport prettinessSupport, int indent) throws IOException {
		int entryIndent = indent + 1;
		int l = nodes.length;
		for (int i = 0; i < l;) {
			prettinessSupport.writeLinefeed(writer, entryIndent);
			writer.write(startEntryElement);
			nodes[i++].write(writer, prettinessSupport, entryIndent);
			nodes[i++].write(writer, prettinessSupport, entryIndent);
			writer.write(endEntryElement);
		}

		prettinessSupport.writeLinefeed(writer, indent);
		writer.write(endElement);
	}

}
