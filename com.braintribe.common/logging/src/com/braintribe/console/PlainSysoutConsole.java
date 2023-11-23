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
package com.braintribe.console;

import com.braintribe.console.output.ConsoleOutput;
import com.braintribe.console.output.ConsoleOutputContainer;
import com.braintribe.console.output.ConsoleText;

public class PlainSysoutConsole implements Console {
	public static PlainSysoutConsole INSTANCE = new PlainSysoutConsole();

	private static void _out(ConsoleOutput output, boolean linebreak) {
		_out(output);
		if (linebreak)
			System.out.println();
	}

	private static void _out(ConsoleOutput output) {
		switch (output.kind()) {
			case container:
				ConsoleOutputContainer container = (ConsoleOutputContainer) output;
				int size = container.size();
				for (int i = 0; i < size; i++) {
					_out(container.get(i));
				}
				break;
			case text:
				ConsoleText text = (ConsoleText) output;
				System.out.print(text.getText());
				break;
			default:
				break;

		}
	}

	@Override
	public Console print(ConsoleOutput output) {
		_out(output, false);
		return this;
	}

	@Override
	public Console print(String text) {
		System.out.print(text);
		return this;
	}

	@Override
	public Console println(String text) {
		System.out.println(text);
		return this;
	}

	@Override
	public Console println(ConsoleOutput output) {
		_out(output, true);
		return this;
	}
}
