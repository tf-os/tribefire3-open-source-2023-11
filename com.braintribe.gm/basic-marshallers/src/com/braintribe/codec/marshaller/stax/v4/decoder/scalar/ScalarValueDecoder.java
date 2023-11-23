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
package com.braintribe.codec.marshaller.stax.v4.decoder.scalar;

import org.xml.sax.Attributes;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.stax.DecodingContext;
import com.braintribe.codec.marshaller.stax.decoder.Decoder;
import com.braintribe.codec.marshaller.stax.factory.DecoderFactoryContext;

public abstract class ScalarValueDecoder extends Decoder {
	private StringBuilder builder = new StringBuilder();
	
	
	protected abstract Object decode(DecodingContext context, String text) throws MarshallException;
	
	@Override
	public void end() throws MarshallException {
		String text = builder.toString();
		
		Object value = null;
		try {
			value = decode(decodingContext, text);
		} catch (Exception e) {
			throw new MarshallException("error while decoding value", e);
		}
		parent.notifyValue(this, value);
	}
	
	@Override
	public void appendCharacters(char[] characters, int s, int l) {
		builder.append(characters, s, l);
	}
	
	@Override
	public Decoder newDecoder(DecoderFactoryContext context, String _elementName, Attributes attributes) throws MarshallException {
		throw new MarshallException("no child element " + _elementName + " is allowed for scalar value elements");
	}
}
