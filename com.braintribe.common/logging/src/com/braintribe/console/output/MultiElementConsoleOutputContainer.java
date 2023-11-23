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

public class MultiElementConsoleOutputContainer implements ConsoleOutputContainer {
	private ConsoleOutput[] elements;
	private int style;
	private boolean resetPosition;

	public MultiElementConsoleOutputContainer(int style, ConsoleOutput... elements) {
		super();
		this.style = style;
		this.elements = elements;
	}
	
	public MultiElementConsoleOutputContainer(ConsoleOutput[] elements, int style, boolean resetPosition) {
		super();
		this.elements = elements;
		this.style = style;
		this.resetPosition = resetPosition;
	}

	public ConsoleOutput[] getElements() {
		return elements;
	}
	
	@Override
	public int getStyle() {
		return style;
	}

	@Override
	public int size() {
		return elements.length;
	}

	@Override
	public ConsoleOutput get(int i) {
		return elements[i];
	}
	
	@Override
	public boolean resetPosition() {
		return resetPosition;
	}
}
