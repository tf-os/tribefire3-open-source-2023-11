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

public abstract class MarshallEvent {
	public static final int MODE_FLOW = 0;
	public static final int MODE_LF = 1;
	public static final int MODE_LF_INDENT = 2;
	public static final int MODE_UNINDENT_LF = 3;
	public MarshallEvent next;
	public int mode;
	public abstract void write(Writer writer) throws IOException ;
	
	public MarshallEvent(int mode) {
		this.mode = mode;
	}
	public MarshallEvent() {
	}
}	
	
