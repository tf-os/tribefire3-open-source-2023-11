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
package com.braintribe.codec.marshaller.stax.decoder.scalar;

import org.xml.sax.Attributes;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.stax.DecodingContext;
import com.braintribe.codec.marshaller.stax.decoder.Decoder;
import com.braintribe.codec.marshaller.stax.factory.DecoderFactoryContext;

public abstract class ScalarValueDecoder extends Decoder {
	private StringBuilder builder = new StringBuilder();
	private boolean nullAware = false;
	
	public ScalarValueDecoder(boolean nullAware) {
		this.nullAware = nullAware;
	}
	
	public ScalarValueDecoder() {
	}
	
	@Override
	public void end() throws MarshallException {
		String text = builder.toString();
		
		if (nullAware && text.isEmpty() && elementName.equals("null"))
			parent.notifyValue(this, null);
		else { 
			Object value = decode(decodingContext, text);
			parent.notifyValue(this, value);
		}
	}
	
	protected abstract Object decode(DecodingContext context, String text) throws MarshallException;
	
	@Override
	public void appendCharacters(char[] characters, int s, int l) {
		builder.append(characters, s, l);
	}
	@Override
	public Decoder newDecoder(DecoderFactoryContext context, String _elementName, Attributes attributes) throws MarshallException {
		throw new MarshallException("no child element " + _elementName + " is allowed for scalar value elements");
	}
}
