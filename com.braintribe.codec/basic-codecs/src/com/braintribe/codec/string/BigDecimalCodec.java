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

import java.math.BigDecimal;

import com.braintribe.codec.Codec;

/**
 * A codec for {@link BigDecimal}s.
 * 
 * @author michael.lafite
 */
public class BigDecimalCodec implements Codec<BigDecimal, String> {

	@Override
	public BigDecimal decode(String valueAsString) {
		if (valueAsString != null) {
			String valueAsTrimmedString = valueAsString.trim();
			if (valueAsTrimmedString.length() > 0) {
				return new BigDecimal(valueAsTrimmedString);
			}
		}
		return null;
	}

	@Override
	public String encode(BigDecimal obj) {
		if (obj != null) {
			return obj.toString();
		}
		return "";
	}

	@Override
	public Class<BigDecimal> getValueClass() {
		return BigDecimal.class;
	}
}
