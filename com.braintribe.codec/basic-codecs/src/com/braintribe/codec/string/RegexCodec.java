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
package com.braintribe.codec.string;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;

public class RegexCodec<T> implements Codec<T, String>{
	
	private Codec<T,String> valueCodec;
	private String pattern;
	
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
	public void setValueCodec(Codec<T,String> valueCodec) {
		this.valueCodec = valueCodec;
	}

	@Override
	public T decode(String encodedValue) throws CodecException {
		if (validate(encodedValue))
			return valueCodec.decode(encodedValue);
		else
			throw new CodecException("faild ecoding string value "+encodedValue+". it doesn't match the pattern.");
	
	}

	@Override
	public String encode(T value) throws CodecException {
		return valueCodec.encode(value);
	}
	
	public boolean validate(String validationValue){
		return validationValue.matches(pattern);
	}

	@Override
	public Class<T> getValueClass() {
		return valueCodec.getValueClass();
	}
}
