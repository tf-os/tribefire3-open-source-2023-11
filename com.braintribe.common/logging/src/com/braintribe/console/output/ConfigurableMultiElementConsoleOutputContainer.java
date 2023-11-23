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
package com.braintribe.console.output;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.console.ConsoleOutputs;

public class ConfigurableMultiElementConsoleOutputContainer implements ConfigurableConsoleOutputContainer {
	private List<ConsoleOutput> elements = new ArrayList<>();
	private int style;
	private boolean resetPosition;
	
	@Override
	public ConfigurableConsoleOutputContainer setStyle(int style) {
		this.style = style;
		return this;
	}
	
	@Override
	public ConfigurableConsoleOutputContainer append(ConsoleOutput output) {
		elements.add(output);
		return this;
	}
	
	@Override
	public ConfigurableConsoleOutputContainer append(CharSequence text) {
		return append(ConsoleOutputs.text(text));
	}
	
	@Override
	public ConfigurableConsoleOutputContainer resetPosition(boolean resetPosition) {
		this.resetPosition = resetPosition;
		return this;
	}

	@Override
	public int getStyle() {
		return style;
	}

	@Override
	public int size() {
		return elements.size();
	}

	@Override
	public ConsoleOutput get(int i) {
		return elements.get(i);
	}
	
	@Override
	public boolean resetPosition() {
		return resetPosition;
	}
}
