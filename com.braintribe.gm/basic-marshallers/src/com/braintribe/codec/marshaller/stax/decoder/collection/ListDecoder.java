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

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.stax.EntityRegistrationListener;
import com.braintribe.codec.marshaller.stax.decoder.Decoder;
import com.braintribe.codec.marshaller.stax.factory.DecoderFactory;
import com.braintribe.codec.marshaller.stax.factory.DecoderFactoryContext;
import com.braintribe.codec.marshaller.stax.factory.ObjectDecoderFactory;
import com.braintribe.model.generic.GenericEntity;

public class ListDecoder extends Decoder {
	private List<Object> list;
	private DecoderFactory elementDecoderFactory;
	private int childCount;
	
	public ListDecoder(DecoderFactory elementDecoderFactory) {
		this.elementDecoderFactory = elementDecoderFactory;
	}
	
	public ListDecoder() {
		this.elementDecoderFactory = ObjectDecoderFactory.INSTANCE;
	}
	
	@Override
	public Decoder newDecoder(DecoderFactoryContext context, String _elementName, Attributes attributes) throws MarshallException {
		childCount++;
		return elementDecoderFactory.newDecoder(context, _elementName, attributes);
	}

	@Override
	public void end() throws MarshallException {
		if (childCount == 0 && elementName.equals("null"))
			parent.notifyValue(this, null);
		else
			parent.notifyValue(this, getList());
	}

	private List<Object> getList() {
		if (list == null) {
			list = new ArrayList<Object>();
		}

		return list;
	}
	@Override
	public void notifyValue(Decoder origin, Object value) {
		getList().add(value);
	}
	
	@Override
	public void notifyForwardEntity(Decoder origin, String referenceId) {
		// remember size as index for later update of the list position
		final List<Object> localList = getList();
		final int index = localList.size();
		
		// add null a place holder element to grow the list 
		localList.add(null);
		
		decodingContext.addEntityRegistrationListener(referenceId, new EntityRegistrationListener() {
			@Override
			public void onEntityRegistered(GenericEntity entity) throws MarshallException {
				// update the list when the actual value is delivered
				localList.set(index, entity);
			}
		});
	}
}
