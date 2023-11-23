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
package com.braintribe.codec.marshaller.jse.tree.value;

import java.io.IOException;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.jse.CountingWriter;
import com.braintribe.codec.marshaller.jse.tree.JseNode;

public class JseLong extends JseNode {
	protected static final int BITS = 22;
	protected static final int BITS01 = 2 * BITS;
	protected static final int BITS2 = 64 - BITS01;
	protected static final int MASK = (1 << BITS) - 1;
	protected static final int MASK_2 = (1 << BITS2) - 1;
	
	protected static final char[]  envelope1 = "$.l({l:".toCharArray();
	protected static final char[]  envelope2 = ",m:".toCharArray();
	protected static final char[]  envelope3 = ",h:".toCharArray();
	protected static final char[]  envelope4 = "})".toCharArray();
	private long value;

	public JseLong(long value) {
		super();
		this.value = value;
	}
	
	@Override
	public void write(CountingWriter writer) throws MarshallException, IOException {
		/**
		 * Return a triple of ints { low, middle, high } that concatenate
		 * bitwise to the given number.
		 */
		long l = value;
		writer.write(envelope1);
		writer.write(String.valueOf((int) (l & MASK)));
		writer.write(envelope2);
		writer.write(String.valueOf((int) ((l >> BITS) & MASK)));
		writer.write(envelope3);
		writer.write(String.valueOf((int) ((l >> BITS01) & MASK_2)));
		writer.write(envelope4);
	}
}
