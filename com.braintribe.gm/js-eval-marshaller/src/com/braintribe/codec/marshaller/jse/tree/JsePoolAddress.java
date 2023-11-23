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
import com.braintribe.codec.marshaller.jse.PoolAddressSequence;

public class JsePoolAddress extends JseNode {
	private char address[];
	private PoolAddressSequence sequence;
	

	public JsePoolAddress(PoolAddressSequence sequence) {
		super();
		this.sequence = sequence;
	}
	
	@Override
	public void write(CountingWriter writer) throws MarshallException, IOException {
		if (address == null) {
			address = sequence.next();
		}
		writer.write(address);
	}
	
	public char[] getAddress() {
		if (address == null) {
			address = sequence.next();
		}
		return address;
	}
}
