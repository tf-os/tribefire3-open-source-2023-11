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
package com.braintribe.codec.marshaller.stax.factory;

import org.xml.sax.Attributes;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.stax.decoder.Decoder;
import com.braintribe.codec.marshaller.stax.decoder.collection.ListDecoder;
import com.braintribe.codec.marshaller.stax.decoder.collection.MapDecoder;
import com.braintribe.codec.marshaller.stax.decoder.collection.SetDecoder;
import com.braintribe.codec.marshaller.stax.decoder.entity.EntityReferenceDecoder;
import com.braintribe.codec.marshaller.stax.decoder.scalar.BooleanDecoder;
import com.braintribe.codec.marshaller.stax.decoder.scalar.DateDecoder;
import com.braintribe.codec.marshaller.stax.decoder.scalar.DecimalDecoder;
import com.braintribe.codec.marshaller.stax.decoder.scalar.DoubleDecoder;
import com.braintribe.codec.marshaller.stax.decoder.scalar.EnumDecoder;
import com.braintribe.codec.marshaller.stax.decoder.scalar.FloatDecoder;
import com.braintribe.codec.marshaller.stax.decoder.scalar.IntegerDecoder;
import com.braintribe.codec.marshaller.stax.decoder.scalar.LongDecoder;
import com.braintribe.codec.marshaller.stax.decoder.scalar.NullDecoder;
import com.braintribe.codec.marshaller.stax.decoder.scalar.StringDecoder;

public class ObjectDecoderFactory extends DecoderFactory {
	public static final ObjectDecoderFactory INSTANCE = new ObjectDecoderFactory();

	@Override
	public Decoder newDecoder(DecoderFactoryContext context, String elementName, Attributes attributes) throws MarshallException {
		switch (elementName) {
		// simple values
		case "boolean": return new BooleanDecoder();
		case "string": return new StringDecoder();
		case "integer": return new IntegerDecoder();
		case "long": return new LongDecoder();
		case "float": return new FloatDecoder();
		case "double": return new DoubleDecoder();
		case "decimal": return new DecimalDecoder();
		case "date": return new DateDecoder();
		
		// collections
		case "list": return new ListDecoder();
		case "set": return new SetDecoder();
		case "map": return new MapDecoder();
		
		// null
		case "null": return new NullDecoder();
		
		// custom types
		case "enum": return new EnumDecoder();
		case "entity": return new EntityReferenceDecoder(false);
		
		default: 
			throw new MarshallException("Unsupported element type "+elementName);
		}
	}
}
