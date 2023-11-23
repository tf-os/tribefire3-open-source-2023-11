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

public class LongCodec implements Codec<Long, String> {
	
		@Override
		public Long decode(String strValue) throws CodecException {
			return strValue==null || strValue.trim().length()==0 ? null : new Long(strValue.trim());
		}
		
		@Override
		public String encode(Long obj) throws CodecException {
			return obj==null ? "" : obj.toString();
		}
		
		@Override
		public Class<Long> getValueClass() {
		    return Long.class;
		}
	
	}
