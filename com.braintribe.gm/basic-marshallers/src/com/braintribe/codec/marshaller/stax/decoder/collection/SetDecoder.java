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

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.stax.EntityRegistrationListener;
import com.braintribe.codec.marshaller.stax.decoder.Decoder;
import com.braintribe.codec.marshaller.stax.factory.DecoderFactory;
import com.braintribe.codec.marshaller.stax.factory.DecoderFactoryContext;
import com.braintribe.codec.marshaller.stax.factory.ObjectDecoderFactory;
import com.braintribe.model.generic.GenericEntity;

public class SetDecoder extends Decoder {
	private Set<Object> set;
	private DecoderFactory elementDecoderFactory;
	private int childCount;
	
	public SetDecoder(DecoderFactory elementDecoderFactory) {
		this.elementDecoderFactory = elementDecoderFactory;
	}
	
	public SetDecoder() {
		this.elementDecoderFactory = ObjectDecoderFactory.INSTANCE;
	}
	
	@Override
	public Decoder newDecoder(DecoderFactoryContext context, String elementName, Attributes attributes) throws MarshallException {
		childCount++;
		return elementDecoderFactory.newDecoder(context, elementName, attributes);
	}

	@Override
	public void end() throws MarshallException {
		if (childCount == 0 && elementName.equals("null"))
			parent.notifyValue(this, null);
		else
			parent.notifyValue(this, getSet());
	}
	
	@Override
	public void notifyValue(Decoder origin, Object value) {
		getSet().add(value);
	}
	
	private Set<Object> getSet() {
		if (set == null) {
			set = new HashSet<Object>();
		}

		return set;
	}
	
	@Override
	public void notifyForwardEntity(Decoder origin, String referenceId) {
		final Set<Object> localSet = getSet();
		
		decodingContext.addEntityRegistrationListener(referenceId, new EntityRegistrationListener() {
			
			@Override
			public void onEntityRegistered(GenericEntity entity) throws MarshallException {
				localSet.add(entity);
			}
		});
	}
	

}
