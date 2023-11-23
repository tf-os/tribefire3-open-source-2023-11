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
package com.braintribe.model.processing.vde.impl.bvd.string;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.braintribe.model.bvd.string.Localize;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;

public class LocalizeVdeTest extends AbstractStringVdeTest {

	@Test
	public void testExistingLocaleLocalized() throws Exception {
		Localize stringFunction = $.localize();

		LocalizedString localisedString = LocalizedString.T.create();
		localisedString.setLocalizedValues(getLocaleMap());

		stringFunction.setLocalizedString(localisedString);
		stringFunction.setLocale("DE");

		Object result = evaluate(stringFunction);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(String.class);
		assertThat(result).isEqualTo("Hallo");
	}

	@Test
	public void testNonExistingLocaleLocalized() throws Exception {
		Localize stringFunction = $.localize();

		LocalizedString localisedString = LocalizedString.T.create();
		localisedString.setLocalizedValues(getLocaleMap());

		stringFunction.setLocalizedString(localisedString);
		stringFunction.setLocale("FR");

		Object result = evaluate(stringFunction);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(String.class);
		assertThat(result).isEqualTo("Hi");
	}

	@Test(expected = VdeRuntimeException.class)
	public void testRandomLocalizedStringOperandLocalized() throws Exception {
		Localize stringFunction = $.localize();
		stringFunction.setLocalizedString(new Date());

		evaluate(stringFunction);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testRandomLocaleOperandLocalized() throws Exception {
		Localize stringFunction = $.localize();
		LocalizedString localisedString = LocalizedString.T.create();
		localisedString.setLocalizedValues(getLocaleMap());
		stringFunction.setLocalizedString(localisedString);
		stringFunction.setLocale(new Date());

		evaluate(stringFunction);
	}

	private Map<String, String> getLocaleMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("EN", "Hello");
		map.put("DE", "Hallo");
		map.put("default", "Hi");
		return map;
	}

}
