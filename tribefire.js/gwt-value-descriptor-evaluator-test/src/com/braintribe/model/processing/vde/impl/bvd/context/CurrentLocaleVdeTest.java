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
package com.braintribe.model.processing.vde.impl.bvd.context;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.Test;

import com.braintribe.model.bvd.context.CurrentLocale;
import com.braintribe.model.processing.vde.evaluator.api.aspects.CurrentLocaleAspect;
import com.braintribe.model.processing.vde.test.VdeTest;

public class CurrentLocaleVdeTest extends VdeTest {

	@Test
	public void testCurrentLocale() throws Exception {
		CurrentLocale currentLocale = $.currentLocale();

		Object result = evaluateWith(CurrentLocaleAspect.class, () -> Locale.TAIWAN.toString(), currentLocale);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(String.class);
		assertThat(result).isEqualTo(Locale.TAIWAN.toString());
	}

}
