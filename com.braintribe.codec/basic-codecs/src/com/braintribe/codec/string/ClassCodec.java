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

@SuppressWarnings("rawtypes")
public class ClassCodec implements Codec<Class, String> {

	@Override
	public Class decode(String strValue) throws CodecException {
		try {
			return strValue==null || strValue.trim().length()==0 ? null : 
				Class.forName(strValue); //TODO: ClassLoader, etc...
		} catch (ClassNotFoundException e) {
			throw new CodecException(e);
		}
	}

	@Override
	public String encode(Class obj) {
		return obj==null ? "" : obj.getName();
	}
	
	@Override
	public Class<Class> getValueClass() {
	    return Class.class;
	}
}
