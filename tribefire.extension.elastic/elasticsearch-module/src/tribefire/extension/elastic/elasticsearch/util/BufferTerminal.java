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
package tribefire.extension.elastic.elasticsearch.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.elasticsearch.cli.Terminal;

// TODO: check migration from 2.2.1 - changed class completely
public class BufferTerminal extends Terminal {

	protected StringWriter stringWriter = new StringWriter();
	protected PrintWriter printWriter = new PrintWriter(this.stringWriter);

	public BufferTerminal() {
		super(System.lineSeparator());
	}

	protected BufferTerminal(String lineSeparator) {
		super(lineSeparator);
	}

	public String getText() {
		return this.stringWriter.toString();
	}

	@Override
	public String readText(String arg0) {
		return null;
	}

	@Override
	public char[] readSecret(String arg0) {
		return null;
	}

	@Override
	public PrintWriter getWriter() {
		return printWriter;
	}

}
