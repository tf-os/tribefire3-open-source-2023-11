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
package com.braintribe.codec.marshaller.stax.decoder.entity;

import org.xml.sax.Attributes;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.stax.decoder.Decoder;
import com.braintribe.codec.marshaller.stax.factory.DecoderFactoryContext;
import com.braintribe.model.generic.GenericEntity;

public class EntityReferenceDecoder extends Decoder {
	private boolean nullAware;
	
	public EntityReferenceDecoder(boolean nullAware) {
		super();
		this.nullAware = nullAware;
	}

	@Override
	public void begin(Attributes attributes) throws MarshallException {
		String referenceId = attributes.getValue("ref");
		
		if (referenceId == null) {
			if (nullAware && elementName.equals("null"))
				parent.notifyValue(this, null);
			else 
				throw new MarshallException("entity element must have a ref attribute");
		}
		else {
			GenericEntity entity = decodingContext.lookupEntity(referenceId);
			
			if (entity != null) {
				parent.notifyValue(this, entity);
			}
			else {
				parent.notifyForwardEntity(this, referenceId);
			}
		}
	}
	
	@Override
	public Decoder newDecoder(DecoderFactoryContext context, String elementName, Attributes attributes) throws MarshallException {
		throw new MarshallException("no child element " + elementName + " is allowed for entity element with ref attribute");
	}
}
