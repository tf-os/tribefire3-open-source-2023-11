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

public class FloatCodec implements Codec<Float, String> {

	@Override
	public Float decode(String strValue) throws CodecException {
		return strValue==null || strValue.trim().length()==0 ? null : Float.parseFloat(strValue.trim());
	}

	@Override
	public String encode(Float obj) throws CodecException {
		return obj==null ? null : obj.toString();
	}
	
	@Override
	public Class<Float> getValueClass() {
	    return null;
	}
}
