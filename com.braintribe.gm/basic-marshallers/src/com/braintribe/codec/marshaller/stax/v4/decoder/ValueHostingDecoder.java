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
package com.braintribe.codec.marshaller.stax.v4.decoder;

import org.xml.sax.Attributes;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.stax.decoder.Decoder;
import com.braintribe.codec.marshaller.stax.factory.DecoderFactoryContext;
import com.braintribe.codec.marshaller.stax.v4.decoder.collection.ListDecoder;
import com.braintribe.codec.marshaller.stax.v4.decoder.collection.MapDecoder;
import com.braintribe.codec.marshaller.stax.v4.decoder.collection.SetDecoder;
import com.braintribe.codec.marshaller.stax.v4.decoder.entity.AbsenceInformationDecoder;
import com.braintribe.codec.marshaller.stax.v4.decoder.entity.EntityReferenceDecoder;
import com.braintribe.codec.marshaller.stax.v4.decoder.scalar.BooleanDecoder;
import com.braintribe.codec.marshaller.stax.v4.decoder.scalar.DateDecoder;
import com.braintribe.codec.marshaller.stax.v4.decoder.scalar.DecimalDecoder;
import com.braintribe.codec.marshaller.stax.v4.decoder.scalar.DoubleDecoder;
import com.braintribe.codec.marshaller.stax.v4.decoder.scalar.EnumDecoder;
import com.braintribe.codec.marshaller.stax.v4.decoder.scalar.FloatDecoder;
import com.braintribe.codec.marshaller.stax.v4.decoder.scalar.IntegerDecoder;
import com.braintribe.codec.marshaller.stax.v4.decoder.scalar.LongDecoder;
import com.braintribe.codec.marshaller.stax.v4.decoder.scalar.NullDecoder;
import com.braintribe.codec.marshaller.stax.v4.decoder.scalar.StringDecoder;

public class ValueHostingDecoder extends Decoder {
	protected boolean propertyDecorated;
	private static abstract class DecoderFactory {
		public abstract Decoder create();
	}
	private static DecoderFactory[] decoders = new DecoderFactory[128];
	
	static {
		// special values
		decoders['a'] = new DecoderFactory() { @Override public Decoder create() { return new AbsenceInformationDecoder(); } };

		// simple values
		decoders['b'] = new DecoderFactory() { @Override public Decoder create() { return new BooleanDecoder(); } };
		decoders['s'] = new DecoderFactory() { @Override public Decoder create() { return new StringDecoder(); } };
		decoders['i'] = new DecoderFactory() { @Override public Decoder create() { return new IntegerDecoder(); } };
		decoders['l'] = new DecoderFactory() { @Override public Decoder create() { return new LongDecoder(); } };
		decoders['f'] = new DecoderFactory() { @Override public Decoder create() { return new FloatDecoder(); } };
		decoders['d'] = new DecoderFactory() { @Override public Decoder create() { return new DoubleDecoder(); } };
		decoders['D'] = new DecoderFactory() { @Override public Decoder create() { return new DecimalDecoder(); } };
		decoders['T'] = new DecoderFactory() { @Override public Decoder create() { return new DateDecoder(); } };
		
		// collections
		decoders['L'] = new DecoderFactory() { @Override public Decoder create() { return new ListDecoder(); } };
		decoders['S'] = new DecoderFactory() { @Override public Decoder create() { return new SetDecoder(); } };
		decoders['M'] = new DecoderFactory() { @Override public Decoder create() { return new MapDecoder(); } };
		
		// null
		decoders['n'] = new DecoderFactory() { @Override public Decoder create() { return new NullDecoder(); } };
		
		// custom types
		decoders['e'] = new DecoderFactory() { @Override public Decoder create() { return new EnumDecoder(); } };
		decoders['r'] = new DecoderFactory() { @Override public Decoder create() { return new EntityReferenceDecoder(); } };
		
	}
	
	/**
	 * @param context
	 */
	public Decoder newDecoderTable(DecoderFactoryContext context, String _elementName, Attributes attributes) throws MarshallException {
		if (_elementName.length() > 1)
			throw new MarshallException("Unsupported element type "+_elementName);
		
		Decoder decoder = null;
		char c = _elementName.charAt(0);
		if (c < 128) {
			DecoderFactory factory = decoders[c];
			if (factory != null)
				decoder = factory.create();
			else
				throw new MarshallException("Unsupported element type "+_elementName);
		}
		else {
			throw new MarshallException("Unsupported element type "+_elementName);
		}

		if (propertyDecorated)
			decoder.propertyName = attributes.getValue("p");
		
		return decoder;
	}
	
	@Override
	public Decoder newDecoder(DecoderFactoryContext context, String _elementName, Attributes attributes) throws MarshallException {
		if (_elementName.length() > 1)
			throw new MarshallException("Unsupported element type "+_elementName);
		
		Decoder decoder = null;
		switch (_elementName.charAt(0)) {
		// special values
		case 'a': decoder = new AbsenceInformationDecoder(); break;
		
		// simple values
		case 'b': decoder = new BooleanDecoder(); break;
		case 's': decoder = new StringDecoder(); break;
		case 'i': decoder = new IntegerDecoder(); break;
		case 'l': decoder = new LongDecoder(); break;
		case 'f': decoder = new FloatDecoder(); break;
		case 'd': decoder = new DoubleDecoder(); break;
		case 'D': decoder = new DecimalDecoder(); break;
		case 'T': decoder = new DateDecoder(); break;
		
		// collections
		case 'L': decoder = new ListDecoder(); break;
		case 'S': decoder = new SetDecoder(); break;
		case 'M': decoder = new MapDecoder(); break;
		
		// null
		case 'n': decoder = new NullDecoder(); break;
		
		// custom types
		case 'e': decoder = new EnumDecoder(); break;
		case 'r': decoder = new EntityReferenceDecoder(); break;
		
		default: 
			throw new MarshallException("Unsupported element type "+_elementName);
		}
		
		if (propertyDecorated)
			decoder.propertyName = attributes.getValue("p");
		
		return decoder;
	}
}
