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
package com.braintribe.codec.marshaller.stax.decoder;

import org.xml.sax.Attributes;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.stax.EntityRegistrationListener;
import com.braintribe.codec.marshaller.stax.decoder.envelope.GmDataDecoder;
import com.braintribe.codec.marshaller.stax.factory.DecoderFactoryContext;
import com.braintribe.model.generic.GenericEntity;

public class RootDecoder extends Decoder {
	
	public Object value;
	
	public Object getValue() {
		return value;
	}

	@Override
	public Decoder newDecoder(DecoderFactoryContext context, String elementName, Attributes attributes) throws MarshallException {
		if (decodingContext.getVersion() > 3) {
			return new com.braintribe.codec.marshaller.stax.v4.decoder.envelope.GmDataDecoder();
		}
		else {
			return new GmDataDecoder();
		}
	}
	
	@Override
	public void notifyValue(Decoder origin, Object value) {
		this.value = value;
	}
	
	@Override
	public void notifyForwardEntity(final Decoder origin, final String referenceId) {
		// just postpone on normal notify value
		decodingContext.addEntityRegistrationListener(referenceId,  new EntityRegistrationListener() {
			
			@Override
			public void onEntityRegistered(GenericEntity entity) throws MarshallException {
				notifyValue(origin, entity);
			}
		});
	}
}
