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
package com.braintribe.gwt.gmview.client.parse.expert;

import java.util.function.Function;

import com.braintribe.gwt.codec.string.client.DoubleCodec;


/**
 * 
 */
public class DoubleParser implements Function<String, Double> {

	private DoubleCodec commaCodec = new DoubleCodec();
	private DoubleCodec dotCodec = new DoubleCodec();

	{
		commaCodec.setUseCommaAsDecimalSeparator(true);
		dotCodec.setUseCommaAsDecimalSeparator(false);
	}

	@Override
	public Double apply(String value) throws RuntimeException {
		if (ParserUtils.endsWithLetter(value) && !value.toLowerCase().endsWith("d")) {
			return null;
		}

		try {
			if (value.contains(",")) {
				return commaCodec.decode(value);
			} else {
				return dotCodec.decode(value);

			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
