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
package com.braintribe.codec.marshaller.stax.decoder.collection;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.stax.decoder.Decoder;
import com.braintribe.codec.marshaller.stax.factory.DecoderFactory;
import com.braintribe.codec.marshaller.stax.factory.DecoderFactoryContext;
import com.braintribe.codec.marshaller.stax.factory.ObjectDecoderFactory;

public class MapDecoder extends Decoder {
	private Map<Object, Object> map;
	private DecoderFactory keyDecoderFactory;
	private DecoderFactory valueDecoderFactory;
	
	public MapDecoder(DecoderFactory keyDecoderFactory, DecoderFactory valueDecoderFactory) {
		this.keyDecoderFactory = keyDecoderFactory;
		this.valueDecoderFactory = valueDecoderFactory;
	}
	
	public MapDecoder() {
		this.keyDecoderFactory = ObjectDecoderFactory.INSTANCE;
		this.valueDecoderFactory = ObjectDecoderFactory.INSTANCE;
	}

	@Override
	public Decoder newDecoder(DecoderFactoryContext context, String _elementName, Attributes attributes) throws MarshallException {
		if (map == null)
			map = new HashMap<Object, Object>();
		return new MapEntryDecoder(map, keyDecoderFactory, valueDecoderFactory);
	}
	
	@Override
	public void end() throws MarshallException {
		if (map == null) {
			if (elementName.equals("null"))
				parent.notifyValue(this, null);
			else
				parent.notifyValue(this, new HashMap<Object, Object>());
		}
		else
			parent.notifyValue(this, map);
	}
}
