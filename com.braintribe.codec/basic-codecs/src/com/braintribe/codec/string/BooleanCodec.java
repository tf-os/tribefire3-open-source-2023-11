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

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;

public class BooleanCodec implements Codec<Boolean, String> {
	
	private String trueStr;
	private String falseStr;
	
	public BooleanCodec() {
		this(Boolean.TRUE.toString(), Boolean.FALSE.toString());
	}
	public BooleanCodec(String trueStr, String falseStr) {
		this.trueStr = trueStr;
		this.falseStr = falseStr;
	}

	@Override
	public Boolean decode(String strValue) throws CodecException {
		if (strValue == null || strValue.trim().length() == 0) return null;
		else {
			if (strValue.equalsIgnoreCase(trueStr)) return Boolean.TRUE;
			else if (strValue.equalsIgnoreCase(falseStr)) return Boolean.FALSE;
			else throw new CodecException("invalid encoded boolean " + strValue);
		}
	}

	@Override
	public String encode(Boolean obj) {
		if (obj != null) 
			return obj? trueStr: falseStr;
		else 
			return "";
	}
	
	@Override
	public Class<Boolean> getValueClass() {
	    return Boolean.class;
	}
}
