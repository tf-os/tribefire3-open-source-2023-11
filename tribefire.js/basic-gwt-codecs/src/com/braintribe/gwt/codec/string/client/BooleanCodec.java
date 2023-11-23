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
package com.braintribe.gwt.codec.string.client;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.ioc.client.Configurable;

public class BooleanCodec implements Codec<Boolean, String> {
	
	private String encodedTrue = Boolean.TRUE.toString();
	private String encodedFalse = Boolean.FALSE.toString();

	public BooleanCodec() {
	}
	
	public BooleanCodec(String encodedTrue, String encodedFalse) {
		super();
		this.encodedTrue = encodedTrue;
		this.encodedFalse = encodedFalse;
	}

	@Configurable
	public void setEncodedFalse(String encodedFalse) {
		this.encodedFalse = encodedFalse;
	}
	
	public void setEncodedTrue(String encodedTrue) {
		this.encodedTrue = encodedTrue;
	}
	
	@Override
	public Boolean decode(String strValue) throws CodecException  {
		if (strValue == null || strValue.trim().length() == 0) {
			return null;
		}
		else if (encodedTrue.equals(strValue)) {
			return Boolean.TRUE;
		}
		else if (encodedFalse.equals(strValue)) {
			return Boolean.FALSE;
		}
		else throw new CodecException("invalid encoded boolean value: " + strValue);
	}

	@Override
	public String encode(Boolean obj) throws CodecException {
		if (obj == null) return "";
		else return obj? encodedTrue: encodedFalse;
	}

	@Override
	public Class<Boolean> getValueClass() {
		return Boolean.class;
	}
}
