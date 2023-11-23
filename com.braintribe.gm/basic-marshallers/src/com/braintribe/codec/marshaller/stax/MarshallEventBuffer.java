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

import java.io.IOException;
import java.io.Writer;

public class MarshallEventBuffer extends MarshallEventSink {
	private static final char[] indent5 = new char[] {' ', ' ', ' ', ' ', ' '};
	private static final char[] indent4 = new char[] {' ', ' ', ' ', ' '};
	private static final char[] indent3 = new char[] {' ', ' ', ' '};
	private static final char[] indent2 = new char[] {' ', ' '};
	private MarshallEvent anchor = new AnchorEvent();
	private MarshallEvent last = anchor;
	private int indent = 2;
	
	@Override
	public void append(MarshallEvent event) throws IOException {
		last.next = event;
		last = event;
	}
	
	public void write(Writer writer) throws IOException {
		MarshallEvent event = anchor.next;
		while (event != null) {
			switch (event.mode){
			case MarshallEvent.MODE_FLOW:
				event.write(writer);
				break;
			case MarshallEvent.MODE_LF:
				writeLineFeed(writer);
				event.write(writer);
				break;
			case MarshallEvent.MODE_LF_INDENT:
				writeLineFeed(writer);
				indent++;
				event.write(writer);
				break;
			case MarshallEvent.MODE_UNINDENT_LF:
				indent--;
				writeLineFeed(writer);
				event.write(writer);
				break;
			}
			
			event = event.next;
		}
	}
	
	private void writeLineFeed(Writer writer) throws IOException {
		writer.write('\n');
		switch (indent) {
		case 0: break;
		case 1: writer.write(' '); break;
		case 2: writer.write(indent2); break;
		case 3: writer.write(indent3); break;
		case 4: writer.write(indent4); break;
		case 5: writer.write(indent5); break;
		default:
			for (int i = 0; i < indent; i++)
				writer.write(' ');
		}
		
	}

	private static class AnchorEvent extends MarshallEvent {
		@Override
		public void write(Writer writer) throws IOException {
			//Nothing to do here
		}
	}
}
