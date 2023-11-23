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
package com.braintribe.codec.marshaller.yaml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.Test;

import com.braintribe.model.generic.reflection.EnumTypes;
import com.braintribe.model.generic.reflection.ScalarType;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.testing.model.test.technical.features.SimpleEnum;

public class ScalarParsersTest {
	@Test
	public void testSimpleCannonical() {
		assertResult(SimpleType.TYPE_BOOLEAN, "true", true);
		assertResult(SimpleType.TYPE_BOOLEAN, "false", false);

		assertResult(SimpleType.TYPE_DOUBLE, "1", 1d);
		assertResult(SimpleType.TYPE_FLOAT, "1.23", 1.23f);
		assertResult(SimpleType.TYPE_INTEGER, "1", 1);
		assertResult(SimpleType.TYPE_LONG, "1", 1l);
		assertResult(SimpleType.TYPE_DECIMAL, "1", new BigDecimal(1));
	}

	@Test
	public void testAdvancedCannonical() {
		assertResult(SimpleType.TYPE_FLOAT, "0.964", 0.964f);
		assertResult(SimpleType.TYPE_FLOAT, ".964", 0.964f);
		assertResult(SimpleType.TYPE_DOUBLE, "-0.00000001", -0.00000001d);
		assertResult(SimpleType.TYPE_DOUBLE, "-.00000001", -0.00000001d);

		assertResult(SimpleType.TYPE_INTEGER, "-100", -100);
		assertResult(SimpleType.TYPE_LONG, "-5223372036854775607", -5223372036854775607l);
		assertResult(SimpleType.TYPE_DECIMAL, "1234567890.123456789012345678901234567890",
				new BigDecimal("1234567890.123456789012345678901234567890"));

		assertResult(EnumTypes.T(SimpleEnum.class), "FIVE", SimpleEnum.FIVE);
	}

	@Test
	public void testNonCannonical() {
		assertResult(SimpleType.TYPE_BOOLEAN, "True", true);
		assertResult(SimpleType.TYPE_BOOLEAN, "False", false);
		assertResult(SimpleType.TYPE_BOOLEAN, "TRUE", true);
		assertResult(SimpleType.TYPE_BOOLEAN, "FALSE", false);

		assertResult(SimpleType.TYPE_INTEGER, "0xBFF", 0xBFF);
		assertResult(SimpleType.TYPE_INTEGER, "0o777", 0777);
		assertResult(SimpleType.TYPE_INTEGER, "+42", 42);
		assertResult(SimpleType.TYPE_LONG, "0xBFF", 0xBFFl);
		assertResult(SimpleType.TYPE_LONG, "0o777", 0777l);
		assertResult(SimpleType.TYPE_LONG, "+42", 42l);
	}

	@Test
	public void testSpecialFloatingpointValues() {
		assertResult(SimpleType.TYPE_DOUBLE, ".nan", Double.NaN);
		assertResult(SimpleType.TYPE_DOUBLE, ".NaN", Double.NaN);
		assertResult(SimpleType.TYPE_DOUBLE, ".NAN", Double.NaN);
		assertResult(SimpleType.TYPE_FLOAT, ".nan", Float.NaN);
		assertResult(SimpleType.TYPE_FLOAT, ".NaN", Float.NaN);
		assertResult(SimpleType.TYPE_FLOAT, ".NAN", Float.NaN);

		assertResult(SimpleType.TYPE_DOUBLE, ".inf", Double.POSITIVE_INFINITY);
		assertResult(SimpleType.TYPE_DOUBLE, ".Inf", Double.POSITIVE_INFINITY);
		assertResult(SimpleType.TYPE_DOUBLE, ".INF", Double.POSITIVE_INFINITY);
		assertResult(SimpleType.TYPE_DOUBLE, "+.inf", Double.POSITIVE_INFINITY);
		assertResult(SimpleType.TYPE_DOUBLE, "+.Inf", Double.POSITIVE_INFINITY);
		assertResult(SimpleType.TYPE_DOUBLE, "+.INF", Double.POSITIVE_INFINITY);

		assertResult(SimpleType.TYPE_FLOAT, ".inf", Float.POSITIVE_INFINITY);
		assertResult(SimpleType.TYPE_FLOAT, ".Inf", Float.POSITIVE_INFINITY);
		assertResult(SimpleType.TYPE_FLOAT, ".INF", Float.POSITIVE_INFINITY);
		assertResult(SimpleType.TYPE_FLOAT, "+.inf", Float.POSITIVE_INFINITY);
		assertResult(SimpleType.TYPE_FLOAT, "+.Inf", Float.POSITIVE_INFINITY);
		assertResult(SimpleType.TYPE_FLOAT, "+.INF", Float.POSITIVE_INFINITY);

		assertResult(SimpleType.TYPE_DOUBLE, "-.inf", Double.NEGATIVE_INFINITY);
		assertResult(SimpleType.TYPE_DOUBLE, "-.Inf", Double.NEGATIVE_INFINITY);
		assertResult(SimpleType.TYPE_DOUBLE, "-.INF", Double.NEGATIVE_INFINITY);
		assertResult(SimpleType.TYPE_FLOAT, "-.inf", Float.NEGATIVE_INFINITY);
		assertResult(SimpleType.TYPE_FLOAT, "-.Inf", Float.NEGATIVE_INFINITY);
		assertResult(SimpleType.TYPE_FLOAT, "-.INF", Float.NEGATIVE_INFINITY);
	}

	@Test
	public void testZeroes() {
		assertResult(SimpleType.TYPE_INTEGER, "0", 0);
		assertResult(SimpleType.TYPE_LONG, "0", 0l);
		assertResult(SimpleType.TYPE_FLOAT, "0", 0f);
		assertResult(SimpleType.TYPE_DOUBLE, "0", 0d);

		assertResult(SimpleType.TYPE_INTEGER, "+0", 0);
		assertResult(SimpleType.TYPE_LONG, "+0", 0l);
		assertResult(SimpleType.TYPE_FLOAT, "+0", 0f);
		assertResult(SimpleType.TYPE_DOUBLE, "+0", 0d);

		assertResult(SimpleType.TYPE_INTEGER, "-0", 0);
		assertResult(SimpleType.TYPE_LONG, "-0", 0l);
		assertResult(SimpleType.TYPE_FLOAT, "-0", -0f);
		assertResult(SimpleType.TYPE_DOUBLE, "-0", -0d);

		assertResult(SimpleType.TYPE_INTEGER, "00", 0);
		assertResult(SimpleType.TYPE_LONG, "00", 0l);
		assertResult(SimpleType.TYPE_FLOAT, "00", 0f);
		assertResult(SimpleType.TYPE_DOUBLE, "00", 0d);

		assertResult(SimpleType.TYPE_INTEGER, "+00", 0);
		assertResult(SimpleType.TYPE_LONG, "+00", 0l);
		assertResult(SimpleType.TYPE_FLOAT, "+00", 0f);
		assertResult(SimpleType.TYPE_DOUBLE, "+00", 0d);

		assertResult(SimpleType.TYPE_INTEGER, "-00", 0);
		assertResult(SimpleType.TYPE_LONG, "-00", 0l);
		assertResult(SimpleType.TYPE_FLOAT, "-00", -0f);
		assertResult(SimpleType.TYPE_DOUBLE, "-00", -0d);

		assertResult(SimpleType.TYPE_INTEGER, "0042", 42);
		assertResult(SimpleType.TYPE_LONG, "+002345", 2345l);
		assertResult(SimpleType.TYPE_FLOAT, "00.123", 0.123f);
		assertResult(SimpleType.TYPE_DOUBLE, "-00123.455", -123.455d);
	}

	@Test
	public void testEdgeCases() {
		assertResult(SimpleType.TYPE_INTEGER, String.valueOf(Integer.MAX_VALUE), Integer.MAX_VALUE);
		assertResult(SimpleType.TYPE_INTEGER, String.valueOf(Integer.MIN_VALUE), Integer.MIN_VALUE);
		assertExceptionThrown(SimpleType.TYPE_INTEGER, String.valueOf(Integer.MAX_VALUE + 1l));
		assertExceptionThrown(SimpleType.TYPE_INTEGER, String.valueOf(Integer.MIN_VALUE - 1l));

		assertResult(SimpleType.TYPE_LONG, String.valueOf(Long.MAX_VALUE), Long.MAX_VALUE);
		assertResult(SimpleType.TYPE_LONG, String.valueOf(Long.MIN_VALUE), Long.MIN_VALUE);
		assertExceptionThrown(SimpleType.TYPE_LONG, "9,223,372,036,854,775,808");
		assertExceptionThrown(SimpleType.TYPE_LONG, "-9,223,372,036,854,775,807");

		assertResult(SimpleType.TYPE_DOUBLE, String.valueOf(Double.MAX_VALUE), Double.MAX_VALUE);
		assertResult(SimpleType.TYPE_DOUBLE, String.valueOf(Double.MIN_VALUE), Double.MIN_VALUE);

		assertResult(SimpleType.TYPE_FLOAT, String.valueOf(Float.MAX_VALUE), Float.MAX_VALUE);
		assertResult(SimpleType.TYPE_FLOAT, String.valueOf(Float.MIN_VALUE), Float.MIN_VALUE);
		assertResult(SimpleType.TYPE_FLOAT, "340282366638528870000000000000000000000.000000", Float.POSITIVE_INFINITY);
		assertResult(SimpleType.TYPE_FLOAT, "-340282366638528870000000000000000000000.000000", Float.NEGATIVE_INFINITY);
	}

	@Test
	public void testWrong() {
		assertExceptionThrown(SimpleType.TYPE_INTEGER, "a");
		assertExceptionThrown(SimpleType.TYPE_INTEGER, "12l");
		assertExceptionThrown(SimpleType.TYPE_INTEGER, "3.14");

		assertExceptionThrown(SimpleType.TYPE_LONG, "a");
		assertExceptionThrown(SimpleType.TYPE_LONG, "12l");
		assertExceptionThrown(SimpleType.TYPE_LONG, "3.14");

		assertExceptionThrown(SimpleType.TYPE_DOUBLE, "a");
		assertExceptionThrown(SimpleType.TYPE_DOUBLE, "12l");
		assertExceptionThrown(SimpleType.TYPE_DOUBLE, "-3.14l");
		assertExceptionThrown(SimpleType.TYPE_DOUBLE, "-.NaN");
		assertExceptionThrown(SimpleType.TYPE_DOUBLE, "NAN");

		assertExceptionThrown(SimpleType.TYPE_FLOAT, "-a");
		assertExceptionThrown(SimpleType.TYPE_FLOAT, "12l");
		assertExceptionThrown(SimpleType.TYPE_FLOAT, "3.14l");
		assertExceptionThrown(SimpleType.TYPE_FLOAT, "-.nan");
		assertExceptionThrown(SimpleType.TYPE_FLOAT, "nan");

		assertExceptionThrown(SimpleType.TYPE_BOOLEAN, "tRue");
		assertExceptionThrown(SimpleType.TYPE_BOOLEAN, "fALSE");
		assertExceptionThrown(SimpleType.TYPE_BOOLEAN, "on");
		assertExceptionThrown(SimpleType.TYPE_BOOLEAN, "yes");
		assertExceptionThrown(SimpleType.TYPE_BOOLEAN, "f");
		assertExceptionThrown(SimpleType.TYPE_BOOLEAN, "whatever");
		assertExceptionThrown(SimpleType.TYPE_BOOLEAN, "");

		assertExceptionThrown(EnumTypes.T(SimpleEnum.class), "five");
		assertExceptionThrown(EnumTypes.T(SimpleEnum.class), "whatever");
		assertExceptionThrown(EnumTypes.T(SimpleEnum.class), "");
	}

	private void assertExceptionThrown(ScalarType type, String value) {
		assertThatThrownBy(() -> ScalarParsers.parse(type, value)).isInstanceOf(Exception.class);
	}

	private void assertResult(ScalarType type, String value, Object expected) {
		assertThat(ScalarParsers.parse(type, value)).isEqualTo(expected);
	}
}
