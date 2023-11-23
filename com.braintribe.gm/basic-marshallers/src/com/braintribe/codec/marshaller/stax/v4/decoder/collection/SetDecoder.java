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
package com.braintribe.codec.marshaller.stax.v4.decoder.collection;

import java.util.HashSet;
import java.util.Set;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.stax.decoder.Decoder;
import com.braintribe.codec.marshaller.stax.v4.decoder.ValueHostingDecoder;

public class SetDecoder extends ValueHostingDecoder {
	private Set<Object> set = new HashSet<Object>();
	
	@Override
	public void end() throws MarshallException {
		parent.notifyValue(this, set);
	}
	
	@Override
	public void notifyValue(Decoder origin, Object value) {
		set.add(value);
	}

}
