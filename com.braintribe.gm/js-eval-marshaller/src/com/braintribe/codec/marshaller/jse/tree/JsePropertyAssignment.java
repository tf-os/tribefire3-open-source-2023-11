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
package com.braintribe.codec.marshaller.jse.tree;

import java.io.IOException;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.jse.CountingWriter;

public class JsePropertyAssignment extends JseNode {
	private static final char[] functionStart = "$.s(_,".toCharArray();
	private static final char[] statementEnd = ");".toCharArray();
	
	private JseNode property;
	private JseNode value;
	
	public JsePropertyAssignment(JseNode property, JseNode value) {
		super();
		this.property = property;
		this.value = value;
	}

	@Override
	public void write(CountingWriter writer) throws MarshallException, IOException {
		writer.write(functionStart);
		property.write(writer);
		writer.write(',');
		value.write(writer);
		writer.write(statementEnd);
	}
}
