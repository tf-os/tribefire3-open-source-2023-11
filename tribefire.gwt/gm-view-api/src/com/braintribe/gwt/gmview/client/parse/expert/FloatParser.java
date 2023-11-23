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

import com.braintribe.gwt.codec.string.client.FloatCodec;


/**
 * 
 */
public class FloatParser implements Function<String, Float> {

	private FloatCodec commaCodec = new FloatCodec();
	private FloatCodec dotCodec = new FloatCodec();

	{
		commaCodec.setUseCommaAsDecimalSeparator(true);
		dotCodec.setUseCommaAsDecimalSeparator(false);
	}

	@Override
	public Float apply(String value) throws RuntimeException {
		if (ParserUtils.endsWithLetter(value) && !value.toLowerCase().endsWith("f")) {
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
