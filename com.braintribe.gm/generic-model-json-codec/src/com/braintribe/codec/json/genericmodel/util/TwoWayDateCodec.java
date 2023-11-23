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
package com.braintribe.codec.json.genericmodel.util;

import java.util.Date;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.string.DateCodec;

public class TwoWayDateCodec implements Codec<Date, String> {
	private static String oldPattern = "yyyy.MM.dd HH:mm:ss";
	private static DateCodec dateCodec1 = new DateCodec(oldPattern);
	private static DateCodec dateCodec2 = new DateCodec("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	@Override
	public Date decode(String encodedValue) throws CodecException {
		if (encodedValue.length() == oldPattern.length()) {
			return dateCodec1.decode(encodedValue);
		}
		else {
			return dateCodec2.decode(encodedValue);
		}
	}
	
	@Override
	public String encode(Date value) throws CodecException {
		return dateCodec2.encode(value);
	}
	
	@Override
	public Class<Date> getValueClass() {
		return Date.class;
	}
}
