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
package com.braintribe.codec.marshaller.stax.decoder.envelope;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.xml.sax.Attributes;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.stax.decoder.Decoder;
import com.braintribe.codec.marshaller.stax.factory.DecoderFactoryContext;

public class RequiredTypesDecoder extends Decoder {
	private Set<String> requiredTypes = null;
	
	@Override
	public Decoder newDecoder(DecoderFactoryContext context, String _elementName, Attributes attributes) throws MarshallException {
		return new TypeDecoder();
	}
	
	@Override
	public void begin(Attributes attributes) throws MarshallException {
		requiredTypes = new HashSet<String>();
	}
	
	@Override
	public void end() throws MarshallException {
		Consumer<Set<String>> requiredTypesReceiver = decodingContext.getRequiredTypesReceiver();
		if (requiredTypesReceiver != null) {
			try  {
				requiredTypesReceiver.accept(requiredTypes);
			}
			catch (Exception e) {
				throw new MarshallException("error while propagating required types to configured receiver", e);
			}
		}
	}
	@Override
	public void notifyValue(Decoder origin, Object value) {
		requiredTypes.add((String)value);
	}
	
	@Override
	public void notifyForwardEntity(Decoder origin, String referenceId) {
		// suppress
	}
}
