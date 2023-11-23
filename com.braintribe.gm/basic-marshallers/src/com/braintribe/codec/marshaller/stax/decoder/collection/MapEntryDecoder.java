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

import java.util.Map;

import org.xml.sax.Attributes;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.stax.EntityRegistrationListener;
import com.braintribe.codec.marshaller.stax.decoder.Decoder;
import com.braintribe.codec.marshaller.stax.factory.DecoderFactory;
import com.braintribe.codec.marshaller.stax.factory.DecoderFactoryContext;
import com.braintribe.model.generic.GenericEntity;

public class MapEntryDecoder extends Decoder {
	private Object key;
	private Object value;
	private Map<Object, Object> map;
	private int partsReceived = 0;
	private int decodersReturned = 0;
	private DecoderFactory keyDecoderFactory;
	private DecoderFactory valueDecoderFactory;
	
	public MapEntryDecoder(Map<Object, Object> map, DecoderFactory keyDecoderFactory, DecoderFactory valueDecoderFactory) {
		super();
		this.map = map;
		this.keyDecoderFactory = keyDecoderFactory;
		this.valueDecoderFactory = valueDecoderFactory;
	}
	
	@Override
	public Decoder newDecoder(DecoderFactoryContext context, String _elementName, Attributes attributes) throws MarshallException {
		switch (decodersReturned++) {
		case 0: return new MapKeyDecoder(keyDecoderFactory);
		case 1: return new MapValueDecoder(valueDecoderFactory);
		default:
			throw new MarshallException("invalid child element count for entry element");
		}
	}
	
	@Override
	public void notifyValue(Decoder origin, Object partValue) {
		MapEntryPartDecoder partDecoder = (MapEntryPartDecoder) origin;
		if (partDecoder.isKeyDecoder) {
			key = partValue;
		}
		else {
			value = partValue;
		}
		
		if (++partsReceived == 2) {
			map.put(key, value);
		}
	}
	
	@Override
	public void notifyForwardEntity(final Decoder origin, String referenceId) {
		decodingContext.addEntityRegistrationListener(referenceId, new EntityRegistrationListener() {
			
			@Override
			public void onEntityRegistered(GenericEntity entity) throws MarshallException {
				notifyValue(origin, entity);
			}
		});
	}
	
}
