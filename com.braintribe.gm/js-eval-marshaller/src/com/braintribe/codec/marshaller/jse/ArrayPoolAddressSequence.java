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
package com.braintribe.codec.marshaller.jse;


public class ArrayPoolAddressSequence extends PoolAddressSequence {
	public int count = 0;
	private char[] name;
	
	public ArrayPoolAddressSequence(char[] name) {
		this.name = name;
	}
	
	public char[] next() {
		StringBuilder builder = new StringBuilder();
		builder.append(name);
		builder.append('[');
		builder.append(count++);
		builder.append(']');
		int length = builder.length();
		char[] chars = new char[length];
		builder.getChars(0, length, chars, 0);
		return chars;
	}
	
	@Override
	public int getCount() {
		return count;
	}
}
