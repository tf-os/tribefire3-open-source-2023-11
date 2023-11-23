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
package com.braintribe.setup.tools;

import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;

import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.output.ConsoleOutput;

/**
 * Utility class for printing in the console using {@link ConsoleOutputs}. This class keeps track of the indentation and is thus suited for printing
 * out nested structures.
 * 
 * @author peter.gazdik
 */
public class TfsConsolePrinter {

	private static final String DEFAULT_TAB = "    ";

	private final String tab;
	private final boolean strict;

	private String indent = "";

	public TfsConsolePrinter() {
		this(DEFAULT_TAB);
	}

	public TfsConsolePrinter(String tab) {
		this(tab, true);
	}

	public TfsConsolePrinter(String tab, boolean strict) {
		this.tab = tab;
		this.strict = strict;
	}

	public TfsConsolePrinter up() {
		indent += tab;
		return this;
	}

	public TfsConsolePrinter down() {
		if (indent.isEmpty())
			if (strict)
				throw new IllegalStateException("Cannot decrease an already empty indent.");
			else
				return this;

		indent = indent.substring(tab.length());
		return this;
	}

	public TfsConsolePrinter newLine() {
		ConsoleOutputs.println("\n");
		return this;
	}

	public TfsConsolePrinter out(ConsoleOutput output) {
		ConsoleOutputs.println(sequence( //
				text(indent), //
				output //
		));
		return this;
	}

	public TfsConsolePrinter out(String text) {
		ConsoleOutputs.println(indent + text);
		return this;
	}

}
