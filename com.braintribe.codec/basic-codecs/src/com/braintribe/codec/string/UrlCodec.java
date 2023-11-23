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
/**
 * 
 */
package com.braintribe.codec.string;

import java.net.MalformedURLException;
import java.net.URL;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;

public class UrlCodec implements Codec<URL, String> {
	private URL base;

	public UrlCodec(URL base) {
		if (base==null) throw new NullPointerException("base url must not be null");
		this.base = base;
	}
	
	@Override
	public URL decode(String strValue) throws CodecException {
		if (strValue == null || strValue.trim().length() == 0)
			return null;

		try {
			return new URL(base, strValue);
		} catch (MalformedURLException e) {
			throw new CodecException(e);
		}
	}
	
	@Override
	public String encode(URL obj) throws CodecException {
		if (obj==null) return null;
		
		return obj.toExternalForm();
	}
	
	@Override
	public Class<URL> getValueClass() {
	    return URL.class;
	}
}
