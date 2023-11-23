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
import java.util.Date;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.jse.CountingWriter;
import com.braintribe.codec.marshaller.jse.tree.JseNode;

public class JseHostedModeDate extends JseNode {
	private static final char[] functionStart = "$.t('".toCharArray();
	private static final char[] functionEnd = "')".toCharArray();
	
	private Date value;
	
	public JseHostedModeDate(Date value) {
		super();
		this.value = value;
	}

	@Override
	public void write(CountingWriter writer) throws MarshallException, IOException {
		writer.write(functionStart);
		writer.write(String.valueOf(value.getTime()));
		writer.write(functionEnd);
	}
}
