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
package com.braintribe.model.processing.query.smart.eval.context.conversion;

import static com.braintribe.model.processing.smartquery.eval.api.ConversionDirection.delegate2Smart;
import static com.braintribe.model.processing.smartquery.eval.api.ConversionDirection.smart2Delegate;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.junit.Test;

import com.braintribe.model.accessdeployment.smart.meta.conversion.DecimalToString;
import com.braintribe.model.query.smart.processing.eval.context.conversion.DecimalToStringExpert;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * @author peter.gazdik
 */
public class DecimalToStringExpertTest {

	private static final String PATTERN = "000.00";

	private final DecimalToStringExpert expert = expert();

	@Test
	public void decimalToString() throws Exception {
		DecimalToString conversion = DecimalToString.T.create();
		conversion.setPattern(PATTERN);

		test(conversion, new BigDecimal("1.23"), "001,23", new BigDecimal("1.23"));
		test(conversion, new BigDecimal("1.2"), "001,20", new BigDecimal("1.2"));
		test(conversion, new BigDecimal("123456.781"), "123456,78", new BigDecimal("123456.78"));

	}

	private void test(DecimalToString conversion, BigDecimal delegateValue, String expectedSmartValue, BigDecimal expectedDelegateValue) {
		String smartValue = (String) expert.convertValue(conversion, delegateValue, delegate2Smart);
		BtAssertions.assertThat(smartValue).isEqualTo(expectedSmartValue);

		BigDecimal convertedDelegateValue = (BigDecimal) expert.convertValue(conversion, smartValue, smart2Delegate);
		BtAssertions.assertThat(convertedDelegateValue).isEqualTo(expectedDelegateValue);
	}

	private DecimalToStringExpert expert() {
		return new DecimalToStringExpert() {

			@Override
			protected DecimalFormat formatFor(String pattern) {
				DecimalFormatSymbols custom = new DecimalFormatSymbols();
				custom.setDecimalSeparator(',');

				DecimalFormat result = super.formatFor(pattern);
				result.setDecimalFormatSymbols(custom);
				return result;
			}

		};
	}

}
