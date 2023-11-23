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
package com.braintribe.codec.marshaller.dom.coder.scalar;

import java.util.Date;

import com.braintribe.codec.CodecException;
import com.braintribe.codec.marshaller.dom.DomDecodingContext;
import com.braintribe.codec.marshaller.dom.DomEncodingContext;
import com.braintribe.codec.marshaller.dom.coder.DomTextCoder;
import com.braintribe.codec.marshaller.stax.DateFormats;
import com.braintribe.utils.DateTools;

public class DateDomCoder extends DomTextCoder<Date> {
	
	public DateDomCoder() {
		super("T");
	}

	@Override
	protected Date decodeText(DomDecodingContext context, String text) throws CodecException {
		try {
			return DateTools.decode(text, DateFormats.dateFormat);
		} catch (Exception e) {
			throw new CodecException("error while parsing date", e);
		}
	}
	
	@Override
	protected String encodeText(DomEncodingContext context, Date value) throws CodecException {
		return DateTools.encode(value, DateFormats.dateFormat);
	}

}
