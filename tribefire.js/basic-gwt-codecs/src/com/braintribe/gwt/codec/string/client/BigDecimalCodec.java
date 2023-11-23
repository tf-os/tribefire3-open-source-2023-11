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

import java.math.BigDecimal;

import com.braintribe.codec.Codec;
import com.braintribe.gwt.ioc.client.Configurable;

/**
 * A codec for {@link BigDecimal}s.
 * 
 * @author michael.lafite
 */
public class BigDecimalCodec implements Codec<BigDecimal, String> {
	
	private boolean useCommaAsDecimalSeparator = false;
	
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
		String result = "";
		if (obj != null) {
			result = obj.toString();
			int pointPosition = result.indexOf(".");
			if (pointPosition == -1) {
				result += ".";
				pointPosition = result.length() - 1;
			}
			
			int afterSeparatorSize = result.substring(pointPosition + 1).length();
			if (afterSeparatorSize == 0) {
				result += "00";
			} else if (afterSeparatorSize == 1) {
				result += "0";
			}
			
			if (useCommaAsDecimalSeparator) {
				result = result.replace('.', ',');
			}
		}
		
		return result;
	}

	@Override
	public Class<BigDecimal> getValueClass() {
		return BigDecimal.class;
	}
	
	/**
	 * If true, the Codec will use a comma as the decimal separator.
	 * Defaults to false.
	 */
	@Configurable
	public void setUseCommaAsDecimalSeparator(boolean useCommaAsDecimalSeparator) {
		this.useCommaAsDecimalSeparator = useCommaAsDecimalSeparator;
	}
}
