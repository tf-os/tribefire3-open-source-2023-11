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
package com.braintribe.model.processing.vde.impl.bvd.convert;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.Test;

import com.braintribe.model.bvd.convert.ToDouble;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.convert.ToDoubleVde;
import com.braintribe.model.processing.vde.impl.VDGenerator;
import com.braintribe.model.processing.vde.test.VdeTest;

/**
 * Provides tests for {@link ToDoubleVde}.
 * 
 */
public class ToDoubleVdeTest extends VdeTest {
	
	public static VDGenerator $ = new VDGenerator(); 

	@Test
	public void testStringOperandNullFormatToDoubleConvert() throws Exception {

		ToDouble convert = $.toDouble();
		convert.setOperand("4");

		Object result = evaluate(convert);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Double.class);
		assertThat(result).isEqualTo(new Double(4));
	}
	
	@Test  (expected = VdeRuntimeException.class)
	public void testStringOperandRandomFormatToDoubleConvertFormatFail() throws Exception {

		ToDouble convert = $.toDouble();
		convert.setOperand("4");
		convert.setFormat(" "); // random format

		evaluate(convert);
	}
	
	@Test
	public void testBooleanOperandNullFormatToDoubleConvert() throws Exception {

		ToDouble convert = $.toDouble();
		convert.setOperand(true);

		Object result = evaluate(convert);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Double.class);
		assertThat(result).isEqualTo(new Double(1));
		
		convert.setOperand(false);

		result = evaluate(convert);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Double.class);
		assertThat(result).isEqualTo(new Double(0));
	}
	

	@Test (expected = VdeRuntimeException.class)
	public void testRandomOperandNullFormatToDoubleConvertTypeFail() throws Exception {
		
		ToDouble convert = $.toDouble();
		convert.setOperand(new Date()); // only string, boolean allowed
		evaluate(convert);
	}
	
}
