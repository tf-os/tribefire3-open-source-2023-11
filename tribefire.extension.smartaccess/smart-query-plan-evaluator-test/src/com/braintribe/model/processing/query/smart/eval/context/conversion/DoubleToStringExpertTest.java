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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.junit.Test;

import com.braintribe.model.accessdeployment.smart.meta.conversion.DoubleToString;
import com.braintribe.model.query.smart.processing.eval.context.conversion.DoubleToStringExpert;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * @author peter.gazdik
 */
public class DoubleToStringExpertTest {

	private static final String PATTERN = "000.00";

	private final DoubleToStringExpert expert = expert();

	@Test
	public void decimalToString() throws Exception {
		DoubleToString conversion = DoubleToString.T.create();
		conversion.setPattern(PATTERN);

		test(conversion, 1.23d, "001,23", 1.23d);
		test(conversion, 1.2d, "001,20", 1.2d);
		test(conversion, 123456.781d, "123456,78", 123456.78d);

	}

	private void test(DoubleToString conversion, Double delegateValue, String expectedSmartValue, Double expectedDelegateValue) {
		String smartValue = (String) expert.convertValue(conversion, delegateValue, delegate2Smart);
		BtAssertions.assertThat(smartValue).isEqualTo(expectedSmartValue);

		Double convertedDelegateValue = (Double) expert.convertValue(conversion, smartValue, smart2Delegate);
		BtAssertions.assertThat(convertedDelegateValue).isEqualTo(expectedDelegateValue);
	}

	private DoubleToStringExpert expert() {
		return new DoubleToStringExpert() {

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
